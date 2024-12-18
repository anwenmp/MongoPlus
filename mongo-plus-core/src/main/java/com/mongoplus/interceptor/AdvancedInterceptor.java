package com.mongoplus.interceptor;

import com.mongoplus.support.AdvancedFunction;

/**
 * 高级拦截器
 * @author anwen
 */
public interface AdvancedInterceptor {

    /**
     * 此拦截器在责任链中的位置，越小优先级越高
     * @return {@link int}
     * @author anwen
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

    /**
     * 拦截方法，调用{@link Invocation#proceed()}方法继续执行下一个拦截器
     * <p>返回自定义数据会终止责任链，但并不会认为是破坏责任链</p>
     * @param invocation 调用信息
     * @return {@link java.lang.Object}
     * @author anwen
     */
    Object intercept(Invocation invocation) throws Throwable;

    /**
     * 拦截器是否处于激活状态，未激活则不会触发{@link #intercept(Invocation)}方法
     * @return {@link com.mongoplus.support.AdvancedFunction}
     * @author anwen
     */
    default AdvancedFunction activate() {
        return invocation -> true;
    }

}
