package com.mongoplus.enums;

/**
 * @author anwen
 */
public enum IndexDirection {

    ASC(1),

    DESC(-1)

    ;

    private final Integer value;

    public Integer getValue() {
        return value;
    }

    IndexDirection(Integer value) {
        this.value = value;
    }

}
