package com.mongoplus.enums;

/**
 * autocomplete 字段的 gram 生成策略。
 */
public enum SearchTokenization {

    /**
     * 空的
     */
    NONE(""),

    /**
     * 从词的左侧生成 gram。
     */
    EDGE_GRAM("edgeGram"),

    /**
     * 从词的右侧生成 gram。
     */
    RIGHT_EDGE_GRAM("rightEdgeGram"),

    /**
     * 使用滑动窗口生成 gram。
     */
    N_GRAM("nGram");

    private final String value;

    SearchTokenization(String value) {
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
