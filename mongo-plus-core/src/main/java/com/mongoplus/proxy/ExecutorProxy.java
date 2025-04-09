package com.mongoplus.proxy;

import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.ExecutorProxyCache;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.execute.Execute;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import com.mongoplus.toolkit.ExceptionUtil;
import org.bson.Document;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 执行器代理
 *
 * @author JiaChaoYang
 **/
public class ExecutorProxy implements InvocationHandler {

    private final Execute target;

    public ExecutorProxy(Execute target) {
        this.target = target;
    }

    /**
     * 包装普通代理对象
     * @param execute 执行器
     * @return {@link com.mongoplus.execute.Execute}
     * @author anwen
     */
    public static Execute wrap(Execute execute){
        Class<? extends Execute> clazz = execute.getClass();
        return (Execute) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                clazz.getInterfaces(),
                new ExecutorProxy(execute)
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 参数替换拦截器
        ExecuteMethodEnum executeMethodEnum = ExecuteMethodEnum.getMethod(method.getName());
        MethodExecutorStrategy executor = ExecutorProxyCache.EXECUTOR_MAP.get(executeMethodEnum);
        MongoCollection<Document> collection = (MongoCollection<Document>) args[args.length - 1];
        if (Objects.nonNull(executor)) {
            InterceptorChain.getInterceptors().forEach(interceptor -> {
                interceptor.beforeExecute(executeMethodEnum, args, collection);
                executor.invoke(interceptor, args);
            });
        }

        Object invoke;
        try {
            invoke = method.invoke(target, args);
        } catch (Throwable e) {
            throw ExceptionUtil.unwrapThrowable(e);
        }
        InterceptorChain.getInterceptors().forEach(interceptor -> interceptor.afterExecute(executeMethodEnum,args,invoke, collection));
        return invoke;

    }

}
