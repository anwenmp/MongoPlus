package com.mongoplus.config;

import com.mongoplus.property.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 直接绑定 core 配置模型；无参数的启用注解仅注册绑定基础设施。
 * 保留原有 Bean 名称，所有消费者使用同一个绑定完成的实例。
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
public class MongoPlusPropertyConfiguration {

    @Bean("mongo-plus.data.mongodb-com.mongoplus.property.MongoDBConnectProperty")
    @ConditionalOnMissingBean(MongoDBConnectProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.data.mongodb")
    public MongoDBConnectProperty mongoDBConnectProperty() {
        return new MongoDBConnectProperty();
    }

    @Bean("mongo-plus.configuration.collection-com.mongoplus.property.MongoDBCollectionProperty")
    @ConditionalOnMissingBean(MongoDBCollectionProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.configuration.collection")
    public MongoDBCollectionProperty mongoDBCollectionProperty() {
        return new MongoDBCollectionProperty();
    }

    @Bean("mongo-plus.configuration-com.mongoplus.property.MongoDBConfigurationProperty")
    @ConditionalOnMissingBean(MongoDBConfigurationProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.configuration")
    public MongoDBConfigurationProperty mongoDBConfigurationProperty() {
        return new MongoDBConfigurationProperty();
    }

    @Bean("mongo-plus-com.mongoplus.property.MongoDBLogProperty")
    @ConditionalOnMissingBean(MongoDBLogProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus")
    public MongoDBLogProperty mongoDBLogProperty() {
        return new MongoDBLogProperty();
    }

    @Bean("com.mongoplus.property.MongoDBFieldProperty")
    @ConditionalOnMissingBean(MongoDBFieldProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.configuration.field")
    public MongoDBFieldProperty mongoDBFieldProperty() {
        return new MongoDBFieldProperty();
    }

    @Bean("com.mongoplus.property.MongoLogicDelProperty")
    @ConditionalOnMissingBean(MongoLogicDelProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.configuration.logic")
    public MongoLogicDelProperty mongoLogicDelProperty() {
        return new MongoLogicDelProperty();
    }

    @Bean("mongoEncryptorProperty")
    @ConditionalOnMissingBean(MongoEncryptorProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.encryptor")
    public MongoEncryptorProperty mongoEncryptorProperty() {
        return new MongoEncryptorProperty();
    }

    @Bean("com.mongoplus.property.MongoSpringProperty")
    @ConditionalOnMissingBean(MongoSpringProperty.class)
    @ConfigurationProperties(prefix = "mongo-plus.spring")
    public MongoSpringProperty mongoSpringProperty() {
        return new MongoSpringProperty();
    }
}
