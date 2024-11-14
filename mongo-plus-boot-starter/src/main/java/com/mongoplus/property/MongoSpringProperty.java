package com.mongoplus.property;

import com.mongoplus.cache.global.PropertyCache;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author JiaChaoYang
 * @date 2023-10-10 13:07
 **/
@ConfigurationProperties(prefix = "mongo-plus.spring")
public class MongoSpringProperty {

    /**
     * 是否启用基于spring的事务管理器
     * @date 2024/1/10 21:49
    */
    private Boolean transaction = false;

    /**
     * 是否覆盖MongoTemplate的配置
     * @date 2024/1/10 21:49
    */
    private Boolean overrideMongoClient = true;

    public Boolean getOverrideMongoClient() {
        return overrideMongoClient;
    }

    public void setOverrideMongoClient(Boolean overrideMongoClient) {
        this.overrideMongoClient = overrideMongoClient;
    }

    public Boolean getTransaction() {
        return transaction;
    }

    public void setTransaction(Boolean transaction) {
        PropertyCache.transaction = transaction;
        this.transaction = transaction;
    }

}
