package com.mongoplus.strategy.executor;

import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;

/**
 * 方法执行策略(解耦逻辑)
 *
 * @author loser
 * @date 2024/4/28
 */
public interface MethodExecutorStrategy {

    /**
     * 方法类型
     */
    ExecuteMethodEnum method();

    /**
     * 执行拦截方法
     */
    void invoke(Interceptor interceptor, Object[] args);

}
