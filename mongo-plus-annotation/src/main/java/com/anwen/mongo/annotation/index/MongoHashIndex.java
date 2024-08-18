package com.anwen.mongo.annotation.index;

import java.lang.annotation.*;

/**
 * 注解式哈希索引自动创建
 * @author anwen
 */
@Target({ElementType.ANNOTATION_TYPE,ElementType.FIELD})
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface MongoHashIndex {
}
