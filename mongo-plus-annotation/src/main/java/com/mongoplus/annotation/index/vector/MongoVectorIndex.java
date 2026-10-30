package com.mongoplus.annotation.index.vector;

import java.lang.annotation.*;

/**
 * 独立 MongoDB Vector Search 索引，对应 type=vectorSearch、definition.fields。
 * <p>可重复声明多个索引；名称在同一集合的 Search/Vector Search 索引间必须唯一。
 * 每个索引至少包含一个 vector 字段，支持多个 vector 和 filter 字段。</p>
 * <p>需要支持 Vector Search 的部署；高级选项的可用性由服务端版本决定。
 * 此注解不配置普通 Search 向量映射，也不生成或查询向量。</p>
 * @see <a href="https://www.mongodb.com/docs/vector-search/indexes/vector-search-type/">Vector Search 定义</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MongoVectorIndexes.class)
public @interface MongoVectorIndex {
    /** 索引名；空字符串由 Driver 使用 default，与显式 default 视为同名。 */
    String name() default "default";

    /** 类级字段配置：fieldName 必填，name 必须留空；与实体字段注解合并。 */
    MongoVectorField[] fields() default {};

    /**
     * 向量所在的文档数组路径，默认省略。所有 vector 路径必须位于它之下；filter 可在顶层。
     * <p>使用实际 MongoDB 路径，或以 $ 开头的实体属性路径；不根据 Java 类型推断是否需要此配置。</p>
     */
    String nestedRoot() default "";

    /** 源存储仅支持 include/exclude 对象形式；未指定时省略，默认不存储源字段。 */
    MongoVectorStoredSource storedSource() default @MongoVectorStoredSource;
}
