package com.mongoplus.proxy;

import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.support.AdvancedFunction;
import com.mongoplus.toolkit.ExceptionUtil;

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
            throw ExceptionUtil.unwrapThrowable(e);
        }
    }
}
