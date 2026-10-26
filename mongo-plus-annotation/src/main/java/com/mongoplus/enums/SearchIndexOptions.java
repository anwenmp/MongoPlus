package com.mongoplus.enums;

/**
 * string 字段的索引信息量。
 */
public enum SearchIndexOptions {

    /**
     * 只保存文档信息。
     */
    DOCS("docs"),

    /**
     * 额外保存词频。
     */
    FREQS("freqs"),

    /**
     * 额外保存词元位置，支持 phrase 和 span。
     */
    POSITIONS("positions"),

    /**
     * 额外保存字符偏移，支持 highlight。
     */
    OFFSETS("offsets");

    private final String value;

    SearchIndexOptions(String value) {
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
