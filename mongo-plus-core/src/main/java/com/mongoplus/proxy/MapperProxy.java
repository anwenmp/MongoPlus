package com.mongoplus.proxy;

import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.mapper.MongoMapperImpl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author anwen
 */
public class MapperProxy<T> implements InvocationHandler {

    private final BaseMapper baseMapper;
    private final Class<T> mapperInterface;
    private final Object target;
    private final Map<Method, MethodInvoker> methodCache = new ConcurrentHashMap<>();

    // Java9+ 方法
    private static final Method PRIVATE_LOOKUP_IN;

    static {
        Method method = null;
        try {
            method = MethodHandles.class.getMethod(
                    "privateLookupIn",
                    Class.class,
                    MethodHandles.Lookup.class
            );
        } catch (NoSuchMethodException ignored) {
        }
        PRIVATE_LOOKUP_IN = method;
    }

    public MapperProxy(BaseMapper baseMapper, Class<T> mapperInterface) {
        this.baseMapper = baseMapper;
        this.mapperInterface = mapperInterface;
        this.target = buildTarget();
    }

    public static Object wrap(BaseMapper baseMapper, Class<?> mapperInterface) {
        return Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                new MapperProxy<>(baseMapper, mapperInterface)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 处理 Object 自带方法
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }

        return cachedInvoker(method).invoke(proxy, args);
    }

    private MethodInvoker cachedInvoker(Method method) {
        return methodCache.computeIfAbsent(method, m -> {

            // 普通方法
            if (!m.isDefault()) {
                return new PlainMethodInvoker(target, m);
            }

            // default 方法
            try {
                return new DefaultMethodInvoker(getMethodHandle(m));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * default 方法核心实现
     */
    private MethodHandle getMethodHandle(Method method) throws Throwable {

        Class<?> declaringClass = method.getDeclaringClass();

        // Java9+
        if (PRIVATE_LOOKUP_IN != null) {

            MethodHandles.Lookup lookup =
                    (MethodHandles.Lookup) PRIVATE_LOOKUP_IN.invoke(
                            null,
                            declaringClass,
                            MethodHandles.lookup()
                    );

            return lookup.findSpecial(
                    declaringClass,
                    method.getName(),
                    MethodType.methodType(
                            method.getReturnType(),
                            method.getParameterTypes()
                    ),
                    declaringClass
            );
        }

        // java8
        Constructor<MethodHandles.Lookup> constructor =
                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);

        constructor.setAccessible(true);

        return constructor
                .newInstance(declaringClass,
                        MethodHandles.Lookup.PRIVATE
                                | MethodHandles.Lookup.PROTECTED
                                | MethodHandles.Lookup.PACKAGE
                                | MethodHandles.Lookup.PUBLIC)
                .unreflectSpecial(method, declaringClass);
    }

    /**
     * Invoker 抽象
     */
    interface MethodInvoker {
        Object invoke(Object proxy, Object[] args) throws Throwable;
    }

    // 普通方法执行
    static class PlainMethodInvoker implements MethodInvoker {

        private final Object target;
        private final Method method;

        PlainMethodInvoker(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        @Override
        public Object invoke(Object proxy, Object[] args) throws Throwable {
            return method.invoke(target, args);
        }
    }

    /**
     * default 方法执行
     */
    static class DefaultMethodInvoker implements MethodInvoker {

        private final MethodHandle methodHandle;

        DefaultMethodInvoker(MethodHandle methodHandle) {
            this.methodHandle = methodHandle;
        }

        @Override
        public Object invoke(Object proxy, Object[] args) throws Throwable {
            return methodHandle
                    .bindTo(proxy)
                    .invokeWithArguments(args == null ? new Object[0] : args);
        }
    }

    /**
     * 构建真实执行对象
     */
    private Object buildTarget() {
        MongoMapperImpl<T> mongoMapper = new MongoMapperImpl<>();
        mongoMapper.setBaseMapper(baseMapper);
        mongoMapper.setClazz(getGenericClass(mapperInterface));
        return mongoMapper;
    }

    private Class<?> getGenericClass(Class<?> clazz) {
        for (Type type : clazz.getGenericInterfaces()) {
            if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type rawType = parameterizedType.getRawType();
                if (rawType instanceof Class &&
                        MongoMapper.class.isAssignableFrom((Class<?>) rawType)) {
                    return (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }
}