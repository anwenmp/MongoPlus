package com.mongoplus.enums;

/**
 * vector 字段的索引结构。
 */
public enum SearchIndexingMethod {

    /**
     * 空的
     */
    NONE(""),

    /**
     * 通过 HNSW 图组织向量。
     */
    HNSW("hnsw"),

    /**
     * 使用平面索引。
     */
    FLAT("flat");

    private final String value;

    SearchIndexingMethod(String value) {
        this.value = value;
    }

    /**
     * 返回 MongoDB 索引定义使用的值。
     * @return 配置值
     */
    public String getValue() {
        return value;
    }
}
