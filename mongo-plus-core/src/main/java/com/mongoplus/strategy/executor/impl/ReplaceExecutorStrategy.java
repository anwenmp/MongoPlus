package com.mongoplus.strategy.executor.impl;

import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.MutablePair;
import com.mongoplus.strategy.executor.MethodExecutorStrategy;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ReplaceExecutorStrategy implements MethodExecutorStrategy {

    @Override
    public ExecuteMethodEnum method() {
        return ExecuteMethodEnum.REPLACE;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Interceptor interceptor, Object[] args) {
        MutablePair<Bson, Document> replacement = interceptReplacement(interceptor,
                (Bson) args[0], (Document) args[1], (MongoCollection<Document>) args[args.length - 1]
        );
        args[0] = replacement.getLeft();
        args[1] = replacement.getRight();
    }

    protected MutablePair<Bson, Document> interceptReplacement(Interceptor interceptor, Bson filter,
                                                                Document replacement, MongoCollection<Document> collection) {
        return interceptor.executeReplace(filter, replacement, collection);
    }
}
