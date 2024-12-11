package com.anwen.mongo.proxy;

import com.anwen.mongo.cache.global.ExecutorProxyCache;
import com.anwen.mongo.cache.global.ExecutorReplacerCache;
import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.execute.Execute;
import com.anwen.mongo.interceptor.InterceptorChain;
import com.anwen.mongo.replacer.Replacer;
import com.anwen.mongo.strategy.executor.MethodExecutorStrategy;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

/**
 * 执行器代理
 *
 * @author JiaChaoYang
 * @date 2024-02-05 09:15
 **/
public class ExecutorProxy implements InvocationHandler {

    private final Execute target;

    public ExecutorProxy(Execute target) {
        this.target = target;
    }

    /**
     * 包装普通代理对象
     * @param execute 执行器
     * @return {@link com.anwen.mongo.execute.Execute}
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

        // 方法替换执行器 执行首个命中执行器
        for (Replacer replacer : ExecutorReplacerCache.replacers) {
            if (replacer.supplier().get(proxy, target, method, args)) {
                return replacer.invoke(proxy, target, method, args);
            }
        }
        Object invoke = method.invoke(target, args);
        InterceptorChain.getInterceptors().forEach(interceptor -> interceptor.afterExecute(executeMethodEnum,args,invoke, collection));
        return invoke;

    }

}
