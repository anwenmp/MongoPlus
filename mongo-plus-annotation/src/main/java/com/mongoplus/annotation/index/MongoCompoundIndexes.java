package com.mongoplus.annotation.index;

import java.lang.annotation.*;

/**
 * 多个复合索引
 *
 * @author anwen
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoCompoundIndexes {

    MongoCompoundIndex[] value();

}
