package com.mongoplus.annotation.collection;

import com.mongoplus.enums.LogicDataType;

import java.lang.annotation.*;

/**
 * 表字段逻辑处理注解（逻辑删除）
 *
 * @author loser
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface CollectionLogic {

    /**
     * 默认逻辑未删除值（该值可无、会自动获取全局配置）
     */
    String value() default "";

    /**
     * 默认逻辑删除值（该值可无、会自动获取全局配置）
     */
    String delval() default "";

    /**
     * 逻辑删除字段值类型
     * @return {@link Class}
     * @author anwen
     */
    LogicDataType delType() default LogicDataType.DEFAULT;

}
