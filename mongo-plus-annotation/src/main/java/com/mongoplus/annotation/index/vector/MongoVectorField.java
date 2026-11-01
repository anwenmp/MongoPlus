package com.mongoplus.annotation.index.vector;

import com.mongoplus.enums.SearchIndexingMethod;
import com.mongoplus.enums.SearchQuantization;
import com.mongoplus.enums.SearchVectorSimilarity;
import com.mongoplus.enums.VectorFieldType;

import java.lang.annotation.*;

/**
 * 独立 Vector Search 的 vector/filter 字段；可在同一属性上重复声明以归属不同索引。
 * <p>同一索引内不能重复索引相同路径；filter 不允许填写任何向量专属参数。</p>
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(MongoVectorFields.class)
public @interface MongoVectorField {
    /**
     * 所属索引。类上仅一个 MongoVectorIndex 时可省略，多个时必须明确指定。
     * 无类级索引时按此名称分组；留空创建 default。嵌套在 fields 中时必须留空。
     */
    String name() default "";

    /**
     * 实际 MongoDB 字段路径；以 $ 开头时按实体属性逐段转换，包括 ID、重命名和驼峰配置。
     * <p>直接标注实体字段时可省略，使用该字段的实际映射名称；类级 fields 中必填。</p>
     */
    String fieldName() default "";

    /** 字段类型，默认 vector。 */
    VectorFieldType type() default VectorFieldType.VECTOR;

    /** vector 必填，范围 1..8192；-1 表示未指定。binary 量化时必须是 8 的倍数。 */
    int numDimensions() default -1;

    /** vector 必填，无服务端默认值；支持 euclidean、cosine、dotProduct。 */
    SearchVectorSimilarity similarity() default SearchVectorSimilarity.DEFAULT;

    /** 自动量化方式；EMPTY 表示省略，由服务端采用 none。预量化向量应采用 none。 */
    SearchQuantization quantization() default SearchQuantization.EMPTY;

    /** NONE 表示省略，由服务端采用 hnsw；flat 与 hnswOptions 互斥。 */
    SearchIndexingMethod indexingMethod() default SearchIndexingMethod.NONE;

    /** HNSW 图构建配置；未指定成员时省略整个对象。 */
    MongoVectorHnswOptions hnswOptions() default @MongoVectorHnswOptions;
}
