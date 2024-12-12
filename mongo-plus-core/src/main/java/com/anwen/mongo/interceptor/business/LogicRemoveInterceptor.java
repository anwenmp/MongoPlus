package com.anwen.mongo.interceptor.business;

import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.interceptor.AdvancedInterceptor;
import com.anwen.mongo.interceptor.Invocation;
import com.anwen.mongo.logic.LogicRemove;
import com.anwen.mongo.manager.LogicManager;
import com.anwen.mongo.support.AdvancedFunction;

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
