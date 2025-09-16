package com.mongoplus.factory;

import com.mongodb.client.MongoClient;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.meta.MongoClientMetaInfo;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.toolkit.MongoUtil;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 懒加载的MongoClient工厂
 */
public class LazyMongoClientFactory extends AbstractMongoClientFactory {

    /**
     * 未加载的MongoClient
     */
    private final Map<String, MongoClientMetaInfo> metaInfoResources = new ConcurrentHashMap<>();

    @Override
    public void registerMongoClient(String ds, BaseProperty baseProperty) {
        metaInfoResources.put(ds, new MongoClientMetaInfo(baseProperty, () -> MongoUtil.getMongo(ds, baseProperty)));
    }

    @Override
    public boolean existMongoClient(String ds) {
        return resources.containsKey(ds) || metaInfoResources.containsKey(ds);
    }

    @Override
    public MongoClient getMongoClient(String ds) {
        return resources.computeIfAbsent(ds, name -> {
            MongoClientMetaInfo mongoClientMetaInfo = metaInfoResources.get(name);
            if (mongoClientMetaInfo == null) {
                throw new MongoPlusException("No MongoClient supplier found for: " + name);
            }
            return mongoClientMetaInfo.getSupplier().get();
        });
    }

    @Override
    public Set<String> getDataSources() {
        Set<String> dataSources = metaInfoResources.keySet();
        dataSources.addAll(resources.keySet());
        return dataSources;
    }
}
