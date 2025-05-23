package com.mongoplus.config;

import com.mongoplus.property.MongoDBConnectProperty;
import com.mongoplus.toolkit.StringUtils;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author JiaChaoYang
 **/
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
            mongoProperties.setHost(Arrays.stream(mongoDBConnectProperty.getHost().split(",")).collect(Collectors.toList()).get(0));
            mongoProperties.setPort(Integer.valueOf(Arrays.stream(mongoDBConnectProperty.getPort().split(",")).collect(Collectors.toList()).get(0)));
            if (StringUtils.isNotBlank(mongoDBConnectProperty.getUsername())) {
                mongoProperties.setUsername(mongoDBConnectProperty.getUsername());
            }
            if (StringUtils.isNotBlank(mongoDBConnectProperty.getPassword())) {
                mongoProperties.setPassword(mongoDBConnectProperty.getPassword().toCharArray());
            }
            mongoProperties.setAuthenticationDatabase(mongoDBConnectProperty.getAuthenticationDatabase());
        }
        mongoProperties.setDatabase(Arrays.stream(mongoDBConnectProperty.getDatabase().split(",")).collect(Collectors.toList()).get(0));
    }

}
