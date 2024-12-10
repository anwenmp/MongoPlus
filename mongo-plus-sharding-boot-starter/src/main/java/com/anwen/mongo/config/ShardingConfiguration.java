package com.anwen.mongo.config;

import com.anwen.mongo.cache.global.HandlerCache;
import com.anwen.mongo.interceptor.DataSourceShardingInterceptor;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.sharding.ShardingTransactionalHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class ShardingConfiguration implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        HandlerCache.transactionHandler = new ShardingTransactionalHandler();
    }

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

}
