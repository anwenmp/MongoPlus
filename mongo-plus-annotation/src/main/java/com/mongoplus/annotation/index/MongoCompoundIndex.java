package com.mongoplus.annotation.index;

import java.lang.annotation.*;

/**
 * 注解式复合索引自动创建
 * @author anwen
 */
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MongoCompoundIndexes.class)
public @interface MongoCompoundIndex {

    /**
     * 复合索引值，这里需要传入json格式，例：
     * <p>
     *     {@code @CompoundIndex("{'field1':1,'field2':-1}")}
     * </p>
     * 同时也可使用{@code $}标识，该操作将会根据json值的key查找类中的该字段，不存在则直接使用json的key，例:
     * <p>
     *      {@code @CompoundIndex("{'$field1':1,'$field2':-1}")}
     * </p>
     * @author anwen
     * @date 2024/8/18 20:26
     */
    String value();

    /**
     * 索引名称
     * @author anwen
     * @date 2024/8/18 21:03
     */
    String name() default "";

    /**
     * 唯一复合索引
     * @author anwen
     * @date 2024/8/18 13:26
     */
    boolean unique() default false;

    /**
     * 稀疏复合索引
     * @author anwen
     * @date 2024/8/18 13:33
     */
    boolean sparse() default false;

    /**
     * 部分索引，和{@link MongoIndex#partialFilterExpression()}不同，该属性需手动指定字段
     * <p>例：{"field1" : {"$gt",5}}</p>
     * 同时也可使用{@code $}标识，该操作将会根据json值的key查找类中的该字段，不存在则直接使用json的key，例:
     * <p>例：{"$field1" : {"$gt",5}}</p>
     * @author anwen
     * @date 2024/8/18 15:17
     */
    String partialFilterExpression() default "";

    /**
     * 是否应该在后台创建索引
     * @author anwen
     * @date 2024/8/18 20:59
     */
    boolean background() default false;


}
