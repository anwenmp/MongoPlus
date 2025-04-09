package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;

import java.util.List;

/**
 * SAVE 策略执行器
 *
 * @author loser
 */
@SuppressWarnings("unchecked")
public class SaveExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.SAVE;
    }

    @Override
    public void invoke(Interceptor interceptor, Object[] args) {
        args[0] = interceptor.executeSave((List<Document>) args[0]);
        args[0] = interceptor.executeSave((List<Document>) args[0], (MongoCollection<Document>) args[args.length-1]);
    }

}
