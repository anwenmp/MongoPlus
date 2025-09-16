package com.mongoplus.factory;

import com.mongodb.client.MongoClient;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 抽象的MongoClientFactory，定义MongoClient的缓存
 * @author anwen
 */
public abstract class AbstractMongoClientFactory implements MongoClientFactory, AutoCloseable {

    final Log log = LogFactory.getLog(AbstractMongoClientFactory.class);

    /**
     * MongoClient实例缓存
     */
    final Map<String, MongoClient> resources = new ConcurrentHashMap<>();

    /**
     * 关闭Mongo连接
     * @throws Exception 抛出异常
     */
    @Override
    public void close() throws Exception {
        if (log.isDebugEnabled()){
            log.debug("Destroy data source connection client");
        }
        resources.forEach((ds,mongoClient) -> mongoClient.close());
    }

}
