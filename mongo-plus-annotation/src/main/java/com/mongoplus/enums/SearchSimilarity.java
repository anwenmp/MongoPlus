package com.mongoplus.enums;

/**
 * string、autocomplete 的文本评分算法，对应 similarity.type。
 */
public enum SearchSimilarity {

    /**
     * 空的
     */
    NONE(""),

    /**
     * BM25 相关性评分。
     */
    BM25("bm25"),

    /**
     * 按词元是否匹配评分。
     */
    BOOLEAN("boolean"),

    /**
     * 稳定的词频与字段长度评分。
     */
    STABLE_TFL("stableTfl");

    private final String value;

    SearchSimilarity(String value) {
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
