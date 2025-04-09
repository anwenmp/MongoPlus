package com.mongoplus.interceptor.business;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.handlers.CollectionNameHandler;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.manager.MongoPlusClient;
import org.bson.Document;

/**
 * 动态集合拦截器
 *
 * @author anwen
 */
public class DynamicCollectionNameInterceptor implements Interceptor {

    private final CollectionNameHandler collectionNameHandler;

    private final MongoPlusClient mongoPlusClient;

    public DynamicCollectionNameInterceptor(CollectionNameHandler collectionNameHandler,MongoPlusClient mongoPlusClient) {
        this.collectionNameHandler = collectionNameHandler;
        this.mongoPlusClient = mongoPlusClient;
    }

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection) {
        MongoNamespace namespace = collection.getNamespace();
        String collectionName = collectionNameHandler.dynamicCollectionName(executeMethodEnum,source,namespace);
        source[source.length-1] = mongoPlusClient.getCollection(namespace.getDatabaseName(),collectionName);
    }
}
