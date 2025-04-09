package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

/**
 * AGGREGATE 策略执行器
 *
 * @author loser
 */
@SuppressWarnings("unchecked")
public class AggregateExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.AGGREGATE;
    }

    @Override
    public void invoke(Interceptor interceptor, Object[] args) {
        args[0] = interceptor.executeAggregates((List<Bson>) args[0]);
        args[0] = interceptor.executeAggregates((List<Bson>) args[0], (MongoCollection<Document>) args[args.length-1]);
    }

}
