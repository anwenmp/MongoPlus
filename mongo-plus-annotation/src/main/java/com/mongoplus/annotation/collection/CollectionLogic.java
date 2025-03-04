package com.mongoplus.annotation.collection;

import com.mongoplus.annotation.logice.IgnoreLogic;

import java.lang.annotation.*;

/**
 * 表字段逻辑处理注解（逻辑删除）
 *
 * @author loser
 * @date 2024/4/28
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface CollectionLogic {

    /**
     * 开启全局配置 并忽略该文档（任意补充在某一个字段上，建议跟@ID 相同）
     * <p>如需指定某个集合忽略逻辑删除，改为在实体类上标注{@link IgnoreLogic}注解</p>
     */
    @Deprecated
    boolean close() default false;

    /**
     * 默认逻辑未删除值（该值可无、会自动获取全局配置）
     */
    String value() default "";

    /**
     * 默认逻辑删除值（该值可无、会自动获取全局配置）
     */
    String delval() default "";

}
