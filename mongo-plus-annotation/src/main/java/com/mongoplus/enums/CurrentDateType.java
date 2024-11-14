package com.mongoplus.enums;

/**
 * $currentDate操作符类型
 * @date 2024/8/2 上午11:01
 */
public enum CurrentDateType {

    /**
     * 当前时间
     * @date 2024/8/2 上午11:02
     */
    DATE("date"),

    /**
     * 当前时间戳
     * @date 2024/8/2 上午11:02
     */
    TIMESTAMP("timestamp"),

    ;

    private final String type;

    CurrentDateType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
