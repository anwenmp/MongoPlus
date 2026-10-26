package com.mongoplus.enums;

/**
 * vector 字段的自动量化方式。
 */
public enum SearchQuantization {

    /**
     * 空的
     */
    EMPTY(""),

    /**
     * 不自动量化。
     */
    NONE("none"),

    /**
     * 标量量化为单字节整数。
     */
    SCALAR("scalar"),

    /**
     * 二进制量化为单个位。
     */
    BINARY("binary");

    private final String value;

    SearchQuantization(String value) {
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
