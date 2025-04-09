package com.mongoplus.property;

import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.enums.CollectionNameConvertEnum;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mongo-plus.configuration.collection")
public class MongoDBCollectionProperty {

    /**
     * collection名称映射策略
     */
    private CollectionNameConvertEnum mappingStrategy = CollectionNameConvertEnum.ALL_CHAR_LOWERCASE;

    /**
     * 防止整个集合更新和删除属性
    */
    private Boolean blockAttackInner = false;

    /**
     * 用来指定全集合更新删除的拦截器的order，默认1
    */
    private int blockAttackInnerOrder = 1;

    public int getBlockAttackInnerOrder() {
        return blockAttackInnerOrder;
    }

    public void setBlockAttackInnerOrder(int blockAttackInnerOrder) {
        OrderCache.BLOCK_ATTACK_INNER_ORDER = blockAttackInnerOrder;
        this.blockAttackInnerOrder = blockAttackInnerOrder;
    }

    public Boolean getBlockAttackInner() {
        return blockAttackInner;
    }

    public void setBlockAttackInner(Boolean blockAttackInner) {
        this.blockAttackInner = blockAttackInner;
    }

    public CollectionNameConvertEnum getMappingStrategy() {
        return mappingStrategy;
    }

    public void setMappingStrategy(CollectionNameConvertEnum mappingStrategy) {
        this.mappingStrategy = mappingStrategy;
    }

}
