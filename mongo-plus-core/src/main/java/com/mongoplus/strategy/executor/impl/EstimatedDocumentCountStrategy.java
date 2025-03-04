package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;

/**
 * 不接受任何参数的统计
 *
 * @author anwen
 */
public class EstimatedDocumentCountStrategy implements MethodExecutorStrategy {
    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.ESTIMATED_DOCUMENT_COUNT;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Interceptor interceptor, Object[] args) {
        interceptor.executeEstimatedDocumentCount((MongoCollection<Document>) args[0]);
    }
}
