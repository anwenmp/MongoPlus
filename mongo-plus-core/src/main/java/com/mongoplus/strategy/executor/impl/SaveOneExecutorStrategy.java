package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;

/**
 * SAVE_ONE 策略执行器
 *
 * @author anwen
 */
@SuppressWarnings("unchecked")
public class SaveOneExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.SAVE;
    }

    @Override
    public void invoke(Interceptor interceptor, Object[] args) {
        args[0] = interceptor.executeSave((Document) args[0], (MongoCollection<Document>) args[args.length-1]);
    }

}