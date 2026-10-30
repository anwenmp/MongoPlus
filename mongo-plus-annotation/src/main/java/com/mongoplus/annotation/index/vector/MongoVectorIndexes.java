package com.mongoplus.annotation.index.vector;

import java.lang.annotation.*;

/** 可重复 Vector Search 索引注解的容器。 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoVectorIndexes {
    /** 各索引独立配置，不跨索引继承字段或选项。 */
    MongoVectorIndex[] value();
}
