package com.mongoplus.annotation.index.search;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可重复搜索排序注解的容器。
 * <p>读取字段配置时使用 getAnnotationsByType(MongoSearchSort.class)，兼容单个与重复声明。</p>
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoSearchSorts {

    /**
     * 同一实体字段在不同搜索索引中的排序配置。
     */
    MongoSearchSort[] value();
}
