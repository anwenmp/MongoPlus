package com.mongoplus.factory;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.FactoryBean;

/**
 * MongoClientBean工厂
 */
public class MongoClientFactoryBean implements FactoryBean<MongoClient> {

    private final MongoClientFactory mongoClientFactory;

    public MongoClientFactoryBean(MongoClientFactory mongoClientFactory) {
        this.mongoClientFactory = mongoClientFactory;
    }

    @Override
    public MongoClient getObject() throws Exception {
        return this.mongoClientFactory.getMongoClient();
    }

    @Override
    public Class<?> getObjectType() {
        return MongoClient.class;
    }
}
