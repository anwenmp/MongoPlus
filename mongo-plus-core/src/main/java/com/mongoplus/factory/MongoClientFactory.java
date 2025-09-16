package com.mongoplus.factory;

import com.mongodb.client.MongoClient;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.model.BaseProperty;

import java.util.Set;

/**
 * MongoClient工厂接口
 * @author anwen
 */
public interface MongoClientFactory {

    /**
     * 注册MongoClient
     * @param ds 数据源
     * @param baseProperty 属性配置
     */
    void registerMongoClient(String ds, BaseProperty baseProperty);

    /**
     * 此数据源是否注册MongoClient
     * @param ds 数据源
     * @return 是否注册
     */
    boolean existMongoClient(String ds);

    /**
     * 获取MongoClient
     * @param ds 数据源
     * @return MongoClient
     */
    MongoClient getMongoClient(String ds);

    /**
     * 获取所有数据源
     * @return 所有MongoClient
     */
    Set<String> getDataSources();

    /**
     * 获取MongoClient
     * @return MongoClient
     */
    default MongoClient getMongoClient() {
        return getMongoClient(DataSourceNameCache.getDataSource());
    }

}
