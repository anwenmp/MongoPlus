package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;

import java.util.List;

/**
 * BULK_WRITE 策略执行器
 *
 * @author loser
 */
@SuppressWarnings("unchecked")
public class BulkWriteExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.BULK_WRITE;
    }

    @Override
    public void invoke(Interceptor interceptor, Object[] args) {
        args[0] = interceptor.executeBulkWrite((List<WriteModel<Document>>) args[0]);
        args[0] = interceptor.executeBulkWrite((List<WriteModel<Document>>) args[0], (MongoCollection<Document>) args[args.length-1]);
    }

}
