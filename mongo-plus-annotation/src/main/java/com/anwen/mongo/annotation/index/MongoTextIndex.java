package com.anwen.mongo.annotation.index;

import com.anwen.mongo.enums.TextLanguages;

import java.lang.annotation.*;

/**
 * 文本索引
 * @author anwen
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface MongoTextIndex {

    /**
     * 索引字段，传入多个值即为复合文本索引
     * <p>可使用{@code $}标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如：</p>
     * <p>
     *      {@code @MongoTextIndex({"field1","$field2"})}
     * </p>
     * <p>通配符文本索引直接传入$**即可</p>
     * @author anwen
     */
    String[] fields();

    /**
     * 索引名称
     * @author anwen
     */
    String name() default "";

    /**
     * 文本语言，默认英语
     * @author anwen
     */
    TextLanguages language() default TextLanguages.ENGLISH;

    /**
     * 尽可能始终使用默认索引版本。 仅在出于兼容性原因需要时才覆盖默认版本
     * @author anwen
     */
    int textIndexVersion() default -1;

}
