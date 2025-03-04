package com.mongoplus.config;

import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.interceptor.DataSourceShardingInterceptor;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.sharding.ShardingTransactionalHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class MongoShardingConfiguration implements InitializingBean {

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
