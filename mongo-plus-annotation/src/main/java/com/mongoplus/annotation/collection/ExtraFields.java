package com.mongoplus.annotation.collection;

import java.lang.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 集合中的额外字段
 * @author anwen
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExtraFields {

    /**
     * 忽略的字段
     */
    String[] ignoredFields() default {};

    /**
     * map类型,如果不主动传值，那么将会根据类型自动创建，如JSONObject
     */
    Class<? extends Map> mapType() default LinkedHashMap.class;

    /**
     * 下划线转驼峰
     */
    boolean underlineToCamel() default true;

}
