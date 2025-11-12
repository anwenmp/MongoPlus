package com.mongoplus.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongoplus.property.MongoDBConfigurationProperty;
import com.mongoplus.property.MongoSpringProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;

import java.util.function.Supplier;

/**
 * 覆盖MongoTemplate的MongoClient
 * @author JiaChaoYang
 **/
public class OverrideMongoConfiguration extends MongoAutoConfiguration {

    private final Supplier<MongoClient> mongoClientSupplier;

    private final MongoSpringProperty mongoSpringProperty;

    private final MongoDBConfigurationProperty mongoDBConfigurationProperty;

    public OverrideMongoConfiguration(
            Supplier<MongoClient> mongoClientSupplier,
            MongoSpringProperty mongoSpringProperty,
            MongoDBConfigurationProperty mongoDBConfigurationProperty){
        this.mongoClientSupplier = mongoClientSupplier;
        this.mongoSpringProperty = mongoSpringProperty;
        this.mongoDBConfigurationProperty = mongoDBConfigurationProperty;
    }

    @Override
    public MongoClient mongo(ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers, MongoClientSettings settings) {
        if (mongoDBConfigurationProperty.getLazyDataSource()) {
            return null;
        }
        if (mongoSpringProperty.getOverrideMongoClient()){
            return super.mongo(builderCustomizers,settings);
        }
        return this.mongoClientSupplier.get();
    }

}

