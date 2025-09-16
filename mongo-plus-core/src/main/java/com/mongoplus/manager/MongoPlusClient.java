package com.mongoplus.manager;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.domain.MongoPlusDsException;
import com.mongoplus.factory.DefaultMongoClientFactory;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.factory.MongoClientFactoryRegistry;
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
    private Map<String,Map<String,CollectionManager>> collectionManagers;

    /**
     * mongoClientFactory
     */
    private final MongoClientFactory mongoClientFactory = MongoClientFactoryRegistry.getFactory();

    /**
     * 获取所有连接管理器
     * @return 连接管理器
     */
    public Map<String,Map<String,CollectionManager>> getCollectionManagers() {
        return collectionManagers;
    }

    /**
     * 获取集合，根据class
     * @param clazz 实体类
     * @return 集合
     */
    public MongoCollection<Document> getCollection(Class<?> clazz) {
        return getCollectionManager(clazz).getCollection(clazz);
    }

    /**
     * 获取集合，根据class获取数据源，但是单独指定数据库
     * @param clazz 实体类
     * @param collectionName 集合名称
     * @return 集合
     */
    public MongoCollection<Document> getCollection(Class<?> clazz,String collectionName) {
        return getCollectionManager(clazz).getCollection(collectionName);
    }

    /**
     * 获取集合，根据database和collectionName
     * @param database 数据库名称
     * @param collectionName 集合名称
     * @return 集合
     */
    public MongoCollection<Document> getCollection(String database,String collectionName) {
        return getCollectionManager(database).getCollection(collectionName);
    }

    /**
     * 获取集合，根据dataSource和database和collectionName
     * @param dataSource 数据源名称
     * @param database 数据库名称
     * @param collectionName 集合名称
     * @return 集合
     */
    public MongoCollection<Document> getCollection(String dataSource,String database,String collectionName){
        return getCollectionManager(dataSource,database).getCollection(dataSource,collectionName);
    }

    /**
     * 获取集合，根据database和clazz
     * @param database 数据库名称
     * @param clazz 集合对应的实体类
     * @return 集合
     */
    public MongoCollection<Document> getCollection(String database,Class<?> clazz){
        return getCollectionManager(database).getCollection(clazz);
    }

    /**
     * 获取集合管理器，根据class
     * @param clazz 集合对应的实体类
     * @return 集合管理器
     */
    public CollectionManager getCollectionManager(Class<?> clazz){
        return getCollectionManager(getDatabase(clazz));
    }

    /**
     * 获取集合管理器，根据dataSource和class
     * @param dataSource 数据源名称
     * @param clazz 集合对应的实体类
     * @return 集合管理器
     */
    public CollectionManager getCollectionManager(String dataSource,Class<?> clazz){
        return getCollectionManager(dataSource,getDatabase(clazz));
    }

    /**
     * 获取集合管理器，根据database
     * @param database 数据库名称
     * @return 集合管理器
     */
    public CollectionManager getCollectionManager(String database){
        return getCollectionManager(DataSourceNameCache.getDataSource(),database);
    }

    /**
     * 获取集合管理器，根据dataSource和database
     * @param dataSource 数据源名称
     * @param database 数据库名称
     * @return 集合管理器
     */
    public CollectionManager getCollectionManager(String dataSource,String database){
        Map<String, CollectionManager> managerMap = getCollectionManagers().get(dataSource);
        if (StringUtils.isBlank(database)){
            database = managerMap.keySet().stream().findFirst().orElseThrow(() ->
                    new MongoPlusDsException("database is null"));
        }
        if (null == managerMap || null == managerMap.get(database)){
            CollectionManager collectionManager = new CollectionManager(database);
            getMongoDatabase().add(getMongoClient().getDatabase(database));
            String finalDatabase = database;
            getCollectionManagers().put(dataSource,new ConcurrentHashMap<String,CollectionManager>(){{
                put(finalDatabase, collectionManager);
            }});
        }
        return getCollectionManagers().get(dataSource).get(database);
    }

    /**
     * 获取数据库名称，根据class
     * @param clazz 集合对应的实体类
     * @return 数据库名称
     */
    public String getDatabase(Class<?> clazz){
        String database = DataSourceNameCache.getDatabase();
        if (database.contains(",")){
            database = Arrays.stream(database.split(",")).collect(Collectors.toList()).get(0);
        }
        String annotationDatabase = AnnotationOperate.getDatabase(clazz);
        if (StringUtils.isNotBlank(annotationDatabase)){
            database = annotationDatabase;
        }
        Map<String, CollectionManager> managerMap = getCollectionManagers().get(DataSourceNameCache.getDataSource());
        if (StringUtils.isBlank(database)){
            database = managerMap.keySet().stream().findFirst().orElseThrow(() ->
                    new MongoPlusDsException("database is null"));
        }
        return database;
    }

    /**
     * 获取数据库名称，根据dataSource和class
     * @param dataSource 数据源名称
     * @param clazz 集合对应的实体类
     * @return 数据库名称
     */
    public String getDatabase(String dataSource,Class<?> clazz){
        String database = DataSourceNameCache.getDatabase(dataSource);
        if (database.contains(",")){
            database = Arrays.stream(database.split(",")).collect(Collectors.toList()).get(0);
        }
        String annotationDatabase = AnnotationOperate.getDatabase(clazz);
        if (StringUtils.isNotBlank(annotationDatabase)){
            database = annotationDatabase;
        }
        Map<String, CollectionManager> managerMap = getCollectionManagers().get(dataSource);
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

    /**
     * 获取数据源名称列表
     * @return {@link java.util.List<java.lang.String>}
     * @author anwen
     */
    public List<String> getDataSourceNameList(){
        return new ArrayList<>(getCollectionManagers().keySet());
    }

    /**
     * 获取集合名称，根据class
     * @param clazz 集合对应的实体类
     * @return 集合名称
     */
    public String getCollectionName(Class<?> clazz){
        return AnnotationOperate.getCollectionName(clazz);
    }

    /**
     * 设置集合管理器，根据database
     * @param database 数据库名称
     */
    public void setCollectionManagerMap(String database) {
        CollectionManager collectionManager = new CollectionManager(database);
        getMongoDatabase().add(getMongoClient().getDatabase(database));
        getCollectionManagers().put(DataSourceNameCache.getDataSource(),new ConcurrentHashMap<String,CollectionManager>(){{
            put(database, collectionManager);
        }});
    }

    public void setCollectionManagers(Map<String,Map<String,CollectionManager>> collectionManagerMap) {
        this.collectionManagers = collectionManagerMap;
    }

    public BaseProperty getBaseProperty() {
        return baseProperty;
    }

    public void setBaseProperty(BaseProperty baseProperty) {
        this.baseProperty = baseProperty;
    }

    public MongoClient getMongoClient() {
        return mongoClientFactory.getMongoClient();
    }

    public MongoClient getMongoClient(String dataSource) {
        return mongoClientFactory.getMongoClient(dataSource);
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
                ", collectionManagers=" + collectionManagers +
                '}';
    }
}
