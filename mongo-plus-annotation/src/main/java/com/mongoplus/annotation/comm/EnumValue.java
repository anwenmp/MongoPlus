package com.mongoplus.annotation.comm;

import java.lang.annotation.*;

/**
 * 枚举映射
 * @author anwen
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface EnumValue {

    /**
     * 是否以此注解标注的字段的值入库，默认true
     * @return {@link boolean}
     * @author anwen
     */
    boolean valueStore() default true;

}
