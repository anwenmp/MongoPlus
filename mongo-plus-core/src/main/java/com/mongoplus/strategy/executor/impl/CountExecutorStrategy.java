package com.mongoplus.strategy.executor.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.MutablePair;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;

/**
 * COUNT 策略执行器
 *
 * @author loser
 */
@SuppressWarnings("unchecked")
public class CountExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.COUNT;
    }

    @Override
    public void invoke(Interceptor interceptor, Object[] args) {
        MutablePair<BasicDBObject, CountOptions> basicDBObjectCountOptionsMutablePair = interceptor.executeCount((BasicDBObject) args[0], (CountOptions) args[1]);
        args[0] = basicDBObjectCountOptionsMutablePair.getLeft();
        args[1] = basicDBObjectCountOptionsMutablePair.getRight();
        basicDBObjectCountOptionsMutablePair = interceptor.executeCount((BasicDBObject) args[0], (CountOptions) args[1], (MongoCollection<Document>) args[args.length-1]);
        args[0] = basicDBObjectCountOptionsMutablePair.getLeft();
        args[1] = basicDBObjectCountOptionsMutablePair.getRight();
    }

}
