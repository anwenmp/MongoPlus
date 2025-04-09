package com.mongoplus.enums;

/**
 * 排序枚举
 * @author JiaChaoYang
 */
public enum OrderEnum {

    ASC(1),

    DESC(-1)

    ;

    private final Integer value;

    public Integer getValue() {
        return value;
    }

    OrderEnum(Integer value) {
        this.value = value;
    }
}
