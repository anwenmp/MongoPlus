package com.mongoplus.strategy.executor.impl;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.QueryParam;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * QUERY 策略执行器
 *
 * @author loser
 */
@SuppressWarnings("unchecked")
public class QueryExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.QUERY;
    }

    @Override
    public void invoke(Interceptor interceptor, Object[] args) {
        QueryParam queryParam = interceptor.executeQuery((Bson) args[0], (BasicDBObject) args[1], (BasicDBObject) args[2]);
        args[0] = queryParam.getQuery();
        args[1] = queryParam.getProjection();
        args[2] = queryParam.getSort();
        queryParam = interceptor.executeQuery((Bson) args[0], (BasicDBObject) args[1], (BasicDBObject) args[2], (MongoCollection<Document>) args[args.length-1]);
        args[0] = queryParam.getQuery();
        args[1] = queryParam.getProjection();
        args[2] = queryParam.getSort();
    }

}
