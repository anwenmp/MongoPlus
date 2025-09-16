package com.mongoplus.meta;

import com.mongodb.client.MongoClient;
import com.mongoplus.model.BaseProperty;

import java.util.function.Supplier;

/**
 * MongoClient元信息
 * @author anwen
 */
public class MongoClientMetaInfo {

    /**
     * 配置信息
     */
    private BaseProperty baseProperty;

    /**
     * supplier
     */
    private Supplier<MongoClient> supplier;

    public MongoClientMetaInfo(BaseProperty baseProperty, Supplier<MongoClient> supplier) {
        this.baseProperty = baseProperty;
        this.supplier = supplier;
    }

    public BaseProperty getBaseProperty() {
        return baseProperty;
    }

    public void setBaseProperty(BaseProperty baseProperty) {
        this.baseProperty = baseProperty;
    }

    public Supplier<MongoClient> getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier<MongoClient> supplier) {
        this.supplier = supplier;
    }
}
