package com.mongoplus.factory;

/**
 * MongoClientFactory注册中心
 */
public class MongoClientFactoryRegistry {

    private static volatile MongoClientFactory mongoClientFactory;

    private MongoClientFactoryRegistry() {}

    /**
     * 获取MongoClient工厂实例
     * @return MongoClient工厂实例
     */
    public static MongoClientFactory getFactory() {
        return mongoClientFactory;
    }

    /**
     * 注册MongoClient工厂实例
     * @param factory 工厂实例
     */
    public static void registerFactory(MongoClientFactory factory) {
        if (mongoClientFactory != null) {
            throw new IllegalStateException("MongoClientFactory already registered");
        }
        synchronized (MongoClientFactoryRegistry.class) {
            if (mongoClientFactory == null) {
                mongoClientFactory = factory;
            }
        }
    }

    /**
     * 判断MongoClient工厂实例是否已注册
     * @return true:已注册
     */
    public static boolean isInitialized() {
        return mongoClientFactory != null;
    }

    /**
     * 清空MongoClient工厂实例
     */
    public static void clear() {
        mongoClientFactory = null;
    }

}
