package com.mongoplus.annotation.index;

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
     */
    String dataSource() default "";

}
