package com.mongoplus.enums;

/**
 * $currentDate操作符类型
 */
public enum CurrentDateType {

    /**
     * 当前时间
     */
    DATE("date"),

    /**
     * 当前时间戳
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
