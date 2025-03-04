package com.mongoplus.annotation.collection;

import java.lang.annotation.*;

/**
 * 乐观锁注解
 * <p>仅支持整数类型</p>
 * @author anwen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface Version {
}
