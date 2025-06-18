package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * REMOVE 策略执行器
 *
 * @author anwen
 */
public class RemoveOneExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.REMOVE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Interceptor interceptor, Object[] args) {
        args[0] = interceptor.executeRemoveOne((Bson) args[0], (MongoCollection<Document>) args[args.length-1]);
    }

}