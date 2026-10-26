package com.mongoplus.enums;

/**
 * string 字段评分时的长度归一化策略。
 */
public enum SearchNorms {

    /**
     * 评分时考虑字段长度。
     */
    INCLUDE("include"),

    /**
     * 评分时忽略字段长度。
     */
    OMIT("omit");

    private final String value;

    SearchNorms(String value) {
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
