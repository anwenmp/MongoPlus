package com.mongoplus.annotation.index.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可重复搜索字段映射的容器。
 * <p>读取字段配置时应使用 getAnnotationsByType(MongoSearchField.class)，以同时读取单个与重复声明。</p>
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoSearchFields {

    /**
     * 字段映射定义；同一索引的同一字段可声明不同搜索类型。
     */
    MongoSearchField[] value();
}
