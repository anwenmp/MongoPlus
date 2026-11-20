package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;

/**
 * 复用替换条件和文档的拦截，同时保留独立的操作类型。
 */
public class FindOneAndReplaceExecutorStrategy extends ReplaceExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.FIND_ONE_AND_REPLACE;
    }

    @Override
    protected MutablePair<Bson, Document> interceptReplacement(Interceptor interceptor, Bson filter,
                                                                Document replacement, MongoCollection<Document> collection) {
        return interceptor.executeFindOneAndReplace(filter, replacement, collection);
    }
}
