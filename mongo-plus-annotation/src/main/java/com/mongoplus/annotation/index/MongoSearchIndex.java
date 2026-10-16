package com.mongoplus.annotation.index;

import java.lang.annotation.*;

/**
 * 搜索索引自动创建
 * @author anwen
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoSearchIndex {

    String name();

}
