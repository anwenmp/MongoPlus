package com.anwen.mongo.annotation.comm;

import java.lang.annotation.*;

/**
 * 枚举映射
 * @author anwen
 * @date 2024/11/18 15:18
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface EnumValue {

    /**
     * 是否以此注解标注的字段的值入库，默认true
     * @return {@link boolean}
     * @author anwen
     * @date 2024/11/18 23:41
     */
    boolean valueStore() default true;

}
