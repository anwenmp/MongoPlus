package com.anwen.mongo.annotation.index;

import java.lang.annotation.*;

/**
 * 用来标识自动创建的索引应该在哪个数据源
 * @author anwen
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoIndexDs {

    /**
     * 数据源名称
     * @author anwen
     * @date 2024/8/27 11:15
     */
    String dataSource() default "";

}
