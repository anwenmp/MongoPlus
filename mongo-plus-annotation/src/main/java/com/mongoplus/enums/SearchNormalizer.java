package com.mongoplus.enums;

/**
 * token 字段的规范化方式。
 */
public enum SearchNormalizer {

    /**
     * 保留原始大小写。
     */
    NONE("none"),

    /**
     * 转换为小写。
     */
    LOWERCASE("lowercase");

    private final String value;

    SearchNormalizer(String value) {
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
