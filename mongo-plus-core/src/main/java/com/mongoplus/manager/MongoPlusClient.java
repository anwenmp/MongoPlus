package com.mongoplus.manager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.domain.MongoPlusDsException;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 连接管理器
 *
 * @author JiaChaoYang
 **/
public class MongoPlusClient {

    private BaseProperty baseProperty;

    private List<MongoDatabase> mongoDatabase;

    /**
     * 连接管理器
    */
    private Map<String,Map<String,CollectionManager>> collectionManagerMap;

    public Map<String,Map<String,CollectionManager>> getCollectionManagerMap() {
        return collectionManagerMap;
    }

    public MongoCollection<Document> getCollection(Class<?> clazz){
        return getCollectionManager(clazz).getCollection(clazz);
    }

    public MongoCollection<Document> getCollection(Class<?> clazz,String collectionName){
        return getCollectionManager(clazz).getCollection(collectionName);
    }

    public MongoCollection<Document> getCollection(String database,String collectionName){
        return getCollectionManager(database).getCollection(collectionName);
    }

    public MongoCollection<Document> getCollection(String dataSource,String database,String collectionName){
        return getCollectionManager(dataSource,database).getCollection(dataSource,collectionName);
    }

    public MongoCollection<Document> getCollection(String database,Class<?> clazz){
        return getCollectionManager(database).getCollection(clazz);
    }

    public CollectionManager getCollectionManager(Class<?> clazz){
        return getCollectionManager(getDatabase(clazz));
    }

    public CollectionManager getCollectionManager(String dataSource,Class<?> clazz){
        return getCollectionManager(dataSource,getDatabase(clazz));
    }

    public CollectionManager getCollectionManager(String database){
        return getCollectionManager(DataSourceNameCache.getDataSource(),database);
    }

    public CollectionManager getCollectionManager(String dataSource,String database){
        Map<String, CollectionManager> managerMap = getCollectionManagerMap().get(dataSource);
        if (StringUtils.isBlank(database)){
            database = managerMap.keySet().stream().findFirst().orElseThrow(() ->
                    new MongoPlusDsException("database is null"));
        }
        if (null == managerMap || null == managerMap.get(database)){
            CollectionManager collectionManager = new CollectionManager(database);
            getMongoDatabase().add(getMongoClient().getDatabase(database));
            String finalDatabase = database;
            getCollectionManagerMap().put(dataSource,new ConcurrentHashMap<String,CollectionManager>(){{
                put(finalDatabase, collectionManager);
            }});
        }
        return getCollectionManagerMap().get(dataSource).get(database);
    }

    public String getDatabase(Class<?> clazz){
        String database = DataSourceNameCache.getDatabase();
        if (database.contains(",")){
            database = Arrays.stream(database.split(",")).collect(Collectors.toList()).get(0);
        }
        String annotationDatabase = AnnotationOperate.getDatabase(clazz);
        if (StringUtils.isNotBlank(annotationDatabase)){
            database = annotationDatabase;
        }
        Map<String, CollectionManager> managerMap = getCollectionManagerMap().get(DataSourceNameCache.getDataSource());
        if (StringUtils.isBlank(database)){
            database = managerMap.keySet().stream().findFirst().orElseThrow(() ->
                    new MongoPlusDsException("database is null"));
        }
        return database;
    }

    public String getDatabase(String dataSource,Class<?> clazz){
        String database = DataSourceNameCache.getDatabase(dataSource);
        if (database.contains(",")){
            database = Arrays.stream(database.split(",")).collect(Collectors.toList()).get(0);
        }
        String annotationDatabase = AnnotationOperate.getDatabase(clazz);
        if (StringUtils.isNotBlank(annotationDatabase)){
            database = annotationDatabase;
        }
        Map<String, CollectionManager> managerMap = getCollectionManagerMap().get(dataSource);
        if (StringUtils.isBlank(database)){
            database = managerMap.keySet().stream().findFirst().orElseThrow(() ->
                    new MongoPlusDsException("database is null"));
        }
        return database;
    }

    /**
     * 获取database
     * @param database database名称
     * @return {@link com.mongodb.client.MongoDatabase}
     * @author anwen
     */
    public MongoDatabase getMongoDatabase(String database){
        return getMongoClient().getDatabase(database);
    }

    /**
     * 获取database下的所有集合名称
     * @param database database名称
     * @return {@link java.util.List<java.lang.String>}
     * @author anwen
     */
    public List<String> getCollectionListByDatabase(String database){
        return getMongoDatabase(database).listCollectionNames().into(new ArrayList<>());
    }

    public List<String> getDataSourceNameList(){
        return new ArrayList<>(getCollectionManagerMap().keySet());
    }

    public String getCollectionName(Class<?> clazz){
        return AnnotationOperate.getCollectionName(clazz);
    }

    public void setCollectionManagerMap(String database) {
        CollectionManager collectionManager = new CollectionManager(database);
        getMongoDatabase().add(getMongoClient().getDatabase(database));
        getCollectionManagerMap().put(DataSourceNameCache.getDataSource(),new ConcurrentHashMap<String,CollectionManager>(){{
            put(database, collectionManager);
        }});
    }

    public void setCollectionManagerMap(Map<String,Map<String,CollectionManager>> collectionManagerMap) {
        this.collectionManagerMap = collectionManagerMap;
    }

    public BaseProperty getBaseProperty() {
        return baseProperty;
    }

    public void setBaseProperty(BaseProperty baseProperty) {
        this.baseProperty = baseProperty;
    }

    public MongoClient getMongoClient() {
        return MongoClientFactory.getInstance().getMongoClient();
    }

    public MongoClient getMongoClient(String dataSource) {
        return MongoClientFactory.getInstance().getMongoClient(dataSource);
    }

    public List<MongoDatabase> getMongoDatabase() {
        return mongoDatabase;
    }

    public void setMongoDatabase(List<MongoDatabase> mongoDatabase) {
        this.mongoDatabase = mongoDatabase;
    }


    public void dropCollection(Class<?> clazz) {
        Optional.ofNullable(getCollectionManager(clazz).getCollection(clazz)).ifPresent(MongoCollection::drop);
    }

    public void dropCollection(String database, String collectionName) {
        Optional.ofNullable(getCollectionManager(database).getCollection(collectionName)).ifPresent(MongoCollection::drop);
    }

    @Override
    public String toString() {
        return "ConnectionManager{" +
                "baseProperty=" + baseProperty +
                ", mongoDatabase=" + mongoDatabase +
                ", collectionManager=" + collectionManagerMap +
                '}';
    }
}
