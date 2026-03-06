package com.mongoplus.annotation.collection;

import java.lang.annotation.*;

/**
 * 字段数据绑定（字典回写）
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldBind {

    /**
     * 类型（用于区分不同业务）
     */
    String type();

    /**
     * 目标显示属性（待绑定属性，注意非数据库字段请排除）
     */
    String target();

}
