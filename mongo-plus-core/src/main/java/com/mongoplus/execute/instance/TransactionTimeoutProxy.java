package com.mongoplus.execute.instance;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import com.mongoplus.context.MongoTransactionStatus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * 仅为有截止时间的事务包装 Driver 边界，普通 core/无超时事务不创建此代理。
 * Iterable 的链式调用、延迟执行和后续游标批次都必须使用同一份事务状态。
 */
final class TransactionTimeoutProxy implements InvocationHandler {

    private final Object target;
    private final MongoTransactionStatus status;

    private TransactionTimeoutProxy(Object target, MongoTransactionStatus status) {
        this.target = target;
        this.status = status;
    }

    @SuppressWarnings("unchecked")
    static <T> MongoCollection<T> wrap(MongoCollection<T> collection, MongoTransactionStatus status) {
        return (MongoCollection<T>) decorate(collection, MongoCollection.class, status);
    }

    private static Object decorate(Object value, Class<?> declaredType, MongoTransactionStatus status) {
        Class<?> type;
        if (value instanceof MongoCollection) {
            type = MongoCollection.class;
        } else if (value instanceof MongoCursor) {
            type = MongoCursor.class;
        } else if (value instanceof MongoIterable) {
            type = declaredType.isInterface() && MongoIterable.class.isAssignableFrom(declaredType)
                    ? declaredType : MongoIterable.class;
        } else {
            return value;
        }
        if (Proxy.isProxyClass(value.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(value);
            if (handler instanceof TransactionTimeoutProxy && ((TransactionTimeoutProxy) handler).status == status) {
                return value;
            }
        }
        return Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                new TransactionTimeoutProxy(value, status));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            if ("equals".equals(method.getName())) {
                return proxy == args[0];
            }
            if ("hashCode".equals(method.getName())) {
                return System.identityHashCode(proxy);
            }
            return target.toString();
        }
        // 过期后也必须允许释放 cursor，不让超时检查阻挡资源清理。
        boolean closing = "close".equals(method.getName());
        if (!closing) {
            status.checkTimeout();
        }
        Object value;
        MongoCursor<?> openedCursor = null;
        if (target instanceof MongoIterable && "into".equals(method.getName())) {
            Collection<Object> destination = (Collection<Object>) Objects.requireNonNull(args[0]);
            forEach(destination::add);
            value = destination;
        } else if (target instanceof MongoIterable && "forEach".equals(method.getName())) {
            forEach((Consumer<Object>) Objects.requireNonNull(args[0]));
            value = null;
        } else if (target instanceof MongoIterable && "spliterator".equals(method.getName())) {
            MongoCursor<?> cursor = (MongoCursor<?>) decorate(((MongoIterable<?>) target).iterator(),
                    MongoCursor.class, status);
            openedCursor = cursor;
            value = Spliterators.spliteratorUnknownSize(cursor, Spliterator.ORDERED);
        } else {
            try {
                value = method.invoke(target, args);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
        }
        if (!closing) {
            // 操作开始时未到期，但返回时可能已经到期；禁止业务继续把该事务当作可提交。
            try {
                status.checkTimeout();
            } catch (RuntimeException | Error timeout) {
                // iterator() 可能已经打开 cursor，异常返回时调用方还拿不到它，必须在此释放。
                MongoCursor<?> cursor = value instanceof MongoCursor ? (MongoCursor<?>) value : openedCursor;
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (RuntimeException | Error cleanupFailure) {
                        timeout.addSuppressed(cleanupFailure);
                    }
                }
                throw timeout;
            }
        }
        return value == target ? proxy : decorate(value, method.getReturnType(), status);
    }

    private void forEach(Consumer<Object> consumer) {
        try (MongoCursor<?> cursor = (MongoCursor<?>) decorate(((MongoIterable<?>) target).iterator(),
                MongoCursor.class, status)) {
            while (cursor.hasNext()) {
                consumer.accept(cursor.next());
            }
        }
    }
}
