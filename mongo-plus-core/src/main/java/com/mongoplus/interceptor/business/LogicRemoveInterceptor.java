package com.mongoplus.interceptor.business;

import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logic.LogicRemove;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.support.AdvancedFunction;

import static com.mongoplus.enums.ExecuteMethodEnum.REMOVE;
import static com.mongoplus.enums.ExecuteMethodEnum.REMOVE_ONE;

/**
 * 逻辑删除替换器
 *
 * @author loser
 */
public class LogicRemoveInterceptor implements AdvancedInterceptor {

    @Override
    public int order() {
        return AdvancedInterceptor.super.order()-1;
    }

    @Override
    public AdvancedFunction activate() {
        return (invocation) -> {
            String methodName = invocation.getMethod().getName();
            return LogicManager.open &&
                    (methodName.equals(REMOVE.getMethod()) || methodName.equals(REMOVE_ONE.getMethod()));
        };
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return LogicRemove.logic(invocation,invocation.getCollection());
    }
}
