package com.mongoplus.strategy.executor.impl;

import com.mongoplus.enums.ExecuteMethodEnum;

/**
 * 复用删除条件拦截，保留租户和逻辑未删除条件。
 * 使用独立操作类型，避免按 DeleteResult 处理返回的文档或触发逻辑删除替换。
 */
public class FindOneAndRemoveExecutorStrategy extends RemoveExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.FIND_ONE_AND_REMOVE;
    }
}
