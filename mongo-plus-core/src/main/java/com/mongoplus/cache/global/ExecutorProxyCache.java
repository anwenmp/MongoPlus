package com.mongoplus.cache.global;

import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import com.mongoplus.strategy.executor.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 方法执行器缓存
 *
 * @author loser
 */
public class ExecutorProxyCache {

    public static final Map<ExecuteMethodEnum, MethodExecutorStrategy> EXECUTOR_MAP = new HashMap<>(ExecuteMethodEnum.values().length);

    static {
        EXECUTOR_MAP.put(ExecuteMethodEnum.SAVE, new SaveExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.SAVE_ONE, new SaveOneExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.REMOVE, new RemoveExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.REMOVE_ONE, new RemoveOneExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.UPDATE, new UpdateExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.UPDATE_ONE, new UpdateOneExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.QUERY, new QueryExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.AGGREGATE, new AggregateExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.COUNT, new CountExecutorStrategy());
        EXECUTOR_MAP.put(ExecuteMethodEnum.BULK_WRITE, new BulkWriteExecutorStrategy());
    }

}
