package com.mongoplus.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongoplus.property.MongoSpringProperty;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;

/**
 * 覆盖MongoTemplate的MongoClient
 * @author JiaChaoYang
 **/
public class OverrideMongoConfiguration extends MongoAutoConfiguration {

    private final MongoClient mongoClient;

    private final MongoSpringProperty mongoSpringProperty;

    public OverrideMongoConfiguration(MongoClient mongoClient, MongoSpringProperty mongoSpringProperty){
        this.mongoClient = mongoClient;
        this.mongoSpringProperty = mongoSpringProperty;
    }

    @Override
    public MongoClient mongo(ObjectProvider<MongoClientSettingsBuilderCustomizer> builderCustomizers, MongoClientSettings settings) {
        if (mongoSpringProperty.getOverrideMongoClient()){
            return super.mongo(builderCustomizers,settings);
        }
        return this.mongoClient;
    }

}

