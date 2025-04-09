package com.mongoplus.conn;

import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.logic.UnClassCollection;
import com.mongoplus.registry.MongoEntityMappingRegistry;
import org.bson.Document;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 连接管理器
 *
 * @author JiaChaoYang
 **/
public class CollectionManager {

    /**
     * 缓存mongoCollection
     *
     */
    private final Map<String, MongoCollection<Document>> collectionMap = new ConcurrentHashMap<>();


    private final String database;

    public CollectionManager(String database) {
        this.database = database;
    }

    /**
     * 设置一个连接
     *
     * @author JiaChaoYang
     */
    public void setCollectionMap(String key, MongoCollection<Document> value) {
        collectionMap.put(key, value);
    }

    public MongoCollection<Document> getCollection(Class<?> clazz) {
        return getCollection(AnnotationOperate.getCollectionName(clazz),clazz);
    }

    public MongoCollection<Document> getCollection(String collectionName){
        return getCollection(collectionName, UnClassCollection.class);
    }

    public MongoCollection<Document> getCollection(String dsName,String collectionName){
        return getCollection(dsName,collectionName, UnClassCollection.class);
    }

    public MongoCollection<Document> getCollection(String collectionName,Class<?> clazz) {
        return getCollection(DataSourceNameCache.getDataSource(),collectionName,clazz);
    }

    public MongoCollection<Document> getCollection(String dsName,String collectionName,Class<?> clazz) {
        MongoCollection<Document> mongoCollection;
        // 检查连接是否需要重新创建
        if (!this.collectionMap.containsKey(collectionName)) {
            mongoCollection = new ConnectMongoDB(
                    MongoClientFactory.getInstance().getMongoClient(dsName),
                    database,
                    collectionName
            ).open();
            this.collectionMap.put(collectionName, mongoCollection);
            MongoEntityMappingRegistry.getInstance()
                    .setMappingRelation(mongoCollection.getNamespace().getFullName(), clazz);
        } else {
            mongoCollection = this.collectionMap.get(collectionName);
        }
        return mongoCollection;
    }

}
