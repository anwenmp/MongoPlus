package com.mongoplus.annotation.index.vector;

import java.lang.annotation.*;

/** 可重复向量索引字段注解的容器。 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MongoVectorFields {
    /** 字段声明，使用 getAnnotationsByType 读取。 */
    MongoVectorField[] value();
}
