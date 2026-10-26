package com.mongoplus.enums;

/**
 * MongoDB Search 索引排序中 null、缺失值的比较位置。
 */
public enum SearchSortNoData {

    /**
     * 省略 noData，使用服务端默认行为。
     */
    DEFAULT(""),

    /**
     * 视为最低值。
     */
    LOWEST("lowest"),

    /**
     * 视为最高值。
     */
    HIGHEST("highest");

    private final String value;

    SearchSortNoData(String value) {
        this.value = value;
    }

    /**
     * 返回 noData 配置值。
     * @return 配置值，DEFAULT 返回空字符串，解析时必须省略而非输出空字符串
     */
    public String getValue() {
        return value;
    }
}
