package com.mongoplus.factory;

import com.mongodb.client.MongoClient;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.InitMongoPlusException;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.toolkit.MongoUtil;

import java.util.Map;
import java.util.Set;

/**
 * MongoClient工厂
 *
 * @author JiaChaoYang
 **/
public class DefaultMongoClientFactory extends AbstractMongoClientFactory {

    @Override
    public void registerMongoClient(String ds, BaseProperty baseProperty) {
        DataSourceNameCache.setBaseProperty(ds,baseProperty);
        resources.put(ds, MongoUtil.getMongo(ds, baseProperty));
    }

    @Override
    public boolean existMongoClient(String ds) {
        return resources.containsKey(ds);
    }

    @Override
    public MongoClient getMongoClient(String ds) {
        MongoClient mongoClient;
        if ((mongoClient = resources.get(ds)) == null) {
            throw new InitMongoPlusException("No data source exists: " + ds);
        }
        return mongoClient;
    }

    @Override
    public Set<String> getDataSources() {
        return super.resources.keySet();
    }

}
