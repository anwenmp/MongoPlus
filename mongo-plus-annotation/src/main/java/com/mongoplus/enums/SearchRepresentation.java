package com.mongoplus.enums;

/**
 * 搜索数值字段的存储表示。
 */
public enum SearchRepresentation {

    /**
     * 以双精度浮点数表示。
     */
    DOUBLE("double"),

    /**
     * 以 64 位整数表示。
     */
    INT64("int64");

    private final String value;

    SearchRepresentation(String value) {
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
