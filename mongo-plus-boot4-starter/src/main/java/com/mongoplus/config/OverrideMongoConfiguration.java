package com.mongoplus.config;

import com.mongodb.client.MongoClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * 覆盖MongoTemplate的MongoClient
 * <p>在Spring Boot 4中，MongoAutoConfiguration为final类且mongo()方法使用@ConditionalOnMissingBean，
 * 因此当不需要Spring自行创建MongoClient时，提前注册一个MongoClient Bean即可阻止自动配置创建新的。</p>
 * @author JiaChaoYang
 **/
@AutoConfiguration(beforeName = "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration")
@ConditionalOnProperty(prefix = "mongo-plus.spring", name = "overrideMongoClient", havingValue = "false")
public class OverrideMongoConfiguration {

    /**
     * 当 overrideMongoClient=false 时，将 MongoPlus 创建的 MongoClient 注册为 Primary Bean，
     * 使 MongoAutoConfiguration 的 @ConditionalOnMissingBean 条件不满足，从而阻止其创建新的 MongoClient。
     * 这样 MongoTemplate 将使用与 MongoPlus 相同的 MongoClient。
     */
    @Bean
    @Primary
    public MongoClient mongo(MongoClient mongoClient) {
        return mongoClient;
    }

}
