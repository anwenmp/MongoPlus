package com.mongoplus.annotation.index.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 同一实体类上多个搜索索引的容器。
 * <p>使用 getAnnotationsByType(MongoSearchIndex.class) 同时读取单个和重复声明。</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoSearchIndexes {

    /**
     * 名称各不相同的搜索索引，按声明顺序解析。
     */
    MongoSearchIndex[] value();
}
