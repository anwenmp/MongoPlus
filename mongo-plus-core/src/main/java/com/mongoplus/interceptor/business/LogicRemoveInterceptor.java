package com.mongoplus.interceptor.business;

import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logic.LogicRemove;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.support.AdvancedFunction;

/**
 * 逻辑删除替换器
 *
 * @author loser
 * @date 2024/4/30
 */
public class LogicRemoveInterceptor implements AdvancedInterceptor {

    @Override
    public int order() {
        return AdvancedInterceptor.super.order()-1;
    }

    @Override
    public AdvancedFunction activate() {
        return (invocation) ->
                LogicManager.open && invocation.getMethod().getName().equals(ExecuteMethodEnum.REMOVE.getMethod());
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return LogicRemove.logic(invocation,invocation.getCollection());
    }
}
