package com.mongoplus.annotation.collection;

import com.mongoplus.enums.FieldFill;

import java.lang.annotation.*;

/**
 * 指定列名，不指定默认取属性名
 * @author JiaChaoYang
 **/
@Target(ElementType.FIELD)
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface CollectionField {
    /**
     * 字段映射值
     * @author JiaChaoYang
    */
    String value() default "";

    /**
     * 是否为数据库表字段
     * @author JiaChaoYang
    */
    boolean exist() default true;

    /**
     * 自动填充策略
     * @author JiaChaoYang
    */
    FieldFill fill() default FieldFill.DEFAULT;

    /**
     * 类型处理器，必须实现{@link com.mongoplus.handlers.TypeHandler}接口
     * @author anwen
     */
    Class<?> typeHandler() default Void.class;

    /**
     * 忽略null属性，默认为true
     * @return {@link boolean}
     * @author anwen
     */
    boolean ignoreNull() default true;

    /**
     * 是否是ObjectId
     * @author anwen
     */
    boolean isObjectId() default false;

}
