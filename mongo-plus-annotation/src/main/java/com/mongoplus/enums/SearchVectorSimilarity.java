package com.mongoplus.enums;

/**
 * 向量字段的相似度算法，对应 similarity 字符串。
 */
public enum SearchVectorSimilarity {

    /**
     * 未配置；此值不能直接写入 MongoDB 定义，向量字段必须显式选择算法。
     */
    DEFAULT(""),

    /**
     * 欧氏距离。
     */
    EUCLIDEAN("euclidean"),

    /**
     * 余弦相似度，不允许零模向量。
     */
    COSINE("cosine"),

    /**
     * 点积相似度。
     */
    DOT_PRODUCT("dotProduct");

    private final String value;

    SearchVectorSimilarity(String value) {
        this.value = value;
    }

    /**
     * 返回 MongoDB 索引定义使用的值。
     * @return 配置值，DEFAULT 返回空字符串表示未配置
     */
    public String getValue() {
        return value;
    }
}
