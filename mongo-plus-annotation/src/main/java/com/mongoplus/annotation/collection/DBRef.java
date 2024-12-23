package com.mongoplus.annotation.collection;

import java.lang.annotation.*;

/**
 * DBRef注解
 * @author anwen
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DBRef {
    
    /**
     * 指定数据库，优先级比CollectionName的database高
     * @return {@link String}
     * @author anwen
     */
    String db() default "";
    
}
