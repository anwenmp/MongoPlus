package com.mongoplus.annotation;

import java.lang.annotation.*;

/**
 * 关键字注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveWord {

}
