package com.mongoplus.annotation.index;

import com.mongoplus.enums.IndexDirection;

import java.lang.annotation.*;

/**
 * 注解式索引自动创建
 * @author anwen
 */
@Target({ElementType.ANNOTATION_TYPE,ElementType.FIELD})
//运行时注解
@Retention(RetentionPolicy.RUNTIME)
//表明这个注解应该被 javadoc工具记录
//生成文档
@Documented
public @interface MongoIndex {

    /**
     * 索引名称
     * @author anwen
     */
    String name() default "";

    /**
     * 唯一索引
     * @author anwen
     */
    boolean unique() default false;

    /**
     * 索引排序方向
     * @author anwen
     */
    IndexDirection direction() default IndexDirection.ASC;

    /**
     * 稀疏索引
     * @author anwen
     */
    boolean sparse() default false;

    /**
     * 索引过期时间，以秒为单位
     * @author anwen
     */
    long expireAfterSeconds() default -1;

    /**
     * 自定义类型的索引过期时间
     * <ul>
     * <li><b>d</b>: 天</li>
     * <li><b>h</b>: 时</li>
     * <li><b>m</b>: 分</li>
     * <li><b>s</b>: 秒</li>
     * </ul>
     * <p>{@code @MongoIndex(expireAfter = "1d")}</p>
     * <p>{@code @MongoIndex(expireAfter = "3h")}</p>
     * <p>{@code @MongoIndex(expireAfter = "30m")}</p>
     * <p>{@code @MongoIndex(expireAfter = "30s")}</p>
     * @author anwen
     */
    String expireAfter() default "";

    /**
     * 部分索引
     * <p>例：{"$gt",5}</p>
     * @author anwen
     */
    String partialFilterExpression() default "";

    /**
     * 是否应该在后台创建索引
     * @author anwen
     */
    boolean background() default false;

}
