package com.mongoplus.enums;

/** 独立 Vector Search 索引支持的字段类型，不包含 autoEmbed。 */
public enum VectorFieldType {
    /** 预生成的向量。 */
    VECTOR,
    /** 查询前过滤字段。 */
    FILTER
}
