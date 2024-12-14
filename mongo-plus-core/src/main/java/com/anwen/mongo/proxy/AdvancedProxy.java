package com.anwen.mongo.proxy;

import com.anwen.mongo.interceptor.AdvancedInterceptor;
import com.anwen.mongo.interceptor.Invocation;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.support.AdvancedFunction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author anwen
 */
public class AdvancedProxy implements InvocationHandler {

    private final Log log = LogFactory.getLog(AdvancedProxy.class);

    private final Object target;

    private final AdvancedInterceptor advancedInterceptor;

    public AdvancedProxy(Object target, AdvancedInterceptor advancedInterceptor) {
        this.target = target;
        this.advancedInterceptor = advancedInterceptor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (log.isDebugEnabled()) log.info("advance proxy hit method: "+ method.getName());
        Invocation invocation = new Invocation(proxy, target, method, args);
        AdvancedFunction function = advancedInterceptor.activate();
        if (function.get(invocation)) {
            return advancedInterceptor.intercept(invocation);
        }
        try {
            return method.invoke(target,args);
        } catch (Throwable e) {
            throw e.getCause();
        }
    }
}
