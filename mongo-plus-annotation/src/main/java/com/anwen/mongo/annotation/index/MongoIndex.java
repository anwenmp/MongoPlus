package com.anwen.mongo.annotation.index;

import com.anwen.mongo.enums.IndexDirection;

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
     * @date 2024/8/18 13:25
     */
    String name() default "";

    /**
     * 唯一索引
     * @author anwen
     * @date 2024/8/18 13:26
     */
    boolean unique() default false;

    /**
     * 索引排序方向
     * @author anwen
     * @date 2024/8/18 13:32
     */
    IndexDirection direction() default IndexDirection.ASC;

    /**
     * 稀疏索引
     * @author anwen
     * @date 2024/8/18 13:33
     */
    boolean sparse() default false;

    /**
     * 索引过期时间，以秒为单位
     * @author anwen
     * @date 2024/8/18 13:34
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
     * @date 2024/8/18 13:34
     */
    String expireAfter() default "";

    /**
     * 部分索引
     * <p>例：{"$gt",5}</p>
     * @author anwen
     * @date 2024/8/18 15:17
     */
    String partialFilterExpression() default "";

    /**
     * 表示字段的类型的类中存在索引注解
     * @author anwen
     * @date 2024/8/18 15:20
     */
    boolean internal() default false;

    /**
     * 是否应该在后台创建索引
     * @author anwen
     * @date 2024/8/18 20:59
     */
    boolean background() default false;

}
