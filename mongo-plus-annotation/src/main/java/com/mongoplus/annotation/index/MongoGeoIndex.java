package com.mongoplus.annotation.index;

import com.mongoplus.enums.GeoType;

import java.lang.annotation.*;

/**
 * 地理位置索引
 * @author anwen
 */
@Target({ElementType.ANNOTATION_TYPE,ElementType.FIELD})
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface MongoGeoIndex {

    /**
     * 索引类型
     * @return {@link com.mongoplus.enums.GeoType}
     * @author anwen
     */
    GeoType type();

}
