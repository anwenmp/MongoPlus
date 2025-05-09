package com.mongoplus.annotation.collection;

import java.lang.annotation.*;

/**
 * 指定表名，不使用此注解默认取实体类名
 * @author anwen
 */
@Target(ElementType.TYPE)
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface CollectionName {
    String value();

    /**
     * 选择数据库，可以写配置文件中没有的
     * @author JiaChaoYang
    */ 
    String database() default "";
}
