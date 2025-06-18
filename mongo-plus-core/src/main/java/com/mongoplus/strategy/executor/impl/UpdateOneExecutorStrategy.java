package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.MutablePair;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * 修改 策略执行器
 *
 * @author anwen
 */
public class UpdateOneExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.UPDATE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Interceptor interceptor, Object[] args) {
        args[0] = interceptor.executeUpdate((MutablePair<Bson,Bson>) args[0], (MongoCollection<Document>) args[args.length-1]);
    }

}