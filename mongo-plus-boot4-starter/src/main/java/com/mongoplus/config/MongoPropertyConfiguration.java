package com.mongoplus.config;

import com.mongoplus.property.MongoDBConnectProperty;
import com.mongoplus.toolkit.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * @author JiaChaoYang
 **/
@Configuration
@ConditionalOnClass(MongoProperties.class)
public class MongoPropertyConfiguration {

    private final MongoDBConnectProperty mongoDBConnectProperty;

    private final MongoProperties mongoProperties;

    public MongoPropertyConfiguration(MongoDBConnectProperty mongoDBConnectProperty, MongoProperties mongoProperties) {
        this.mongoDBConnectProperty = mongoDBConnectProperty;
        this.mongoProperties = mongoProperties;
        init();
    }

    public void init(){
        if (StringUtils.isNotBlank(mongoDBConnectProperty.getUrl())){
            mongoProperties.setUri(mongoDBConnectProperty.getUrl());
        }else {
            mongoProperties.setHost(Arrays.stream(mongoDBConnectProperty.getHost().split(",")).toList().get(0));
            mongoProperties.setPort(Integer.valueOf(Arrays.stream(mongoDBConnectProperty.getPort().split(",")).toList().get(0)));
            if (StringUtils.isNotBlank(mongoDBConnectProperty.getUsername())) {
                mongoProperties.setUsername(mongoDBConnectProperty.getUsername());
            }
            if (StringUtils.isNotBlank(mongoDBConnectProperty.getPassword())) {
                mongoProperties.setPassword(mongoDBConnectProperty.getPassword().toCharArray());
            }
            mongoProperties.setAuthenticationDatabase(mongoDBConnectProperty.getAuthenticationDatabase());
        }
        mongoProperties.setDatabase(Arrays.stream(mongoDBConnectProperty.getDatabase().split(",")).toList().get(0));
    }

}
