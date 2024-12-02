package com.anwen.mongo.config;

import com.anwen.mongo.interceptor.DataSourceShardingInterceptor;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.transactional.MongoTransactionalAspect;
import com.anwen.mongo.transactional.ShardingTransactionalAspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class ShardingConfiguration {

    /**
     * 注册分片拦截器
     * @param mongoPlusClient mongoPlusClient
     * @author anwen
     */
    @Bean
    @ConditionalOnMissingBean
    public DataSourceShardingInterceptor dataSourceShardingInterceptor(MongoPlusClient mongoPlusClient) {
        return new DataSourceShardingInterceptor(mongoPlusClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public MongoTransactionalAspect mongoTransactionalAspect() {
        return new ShardingTransactionalAspect();
    }

}
