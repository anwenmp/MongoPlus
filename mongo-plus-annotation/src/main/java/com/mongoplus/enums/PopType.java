package com.mongoplus.enums;

/**
 * pop类型
 * @date 2024/8/2 下午4:26
 */
public enum PopType {

    /**
     * 删除数组第一个元素
     * @date 2024/8/2 下午4:27
     */
    FIRST(-1),

    /**
     * 删除数组最后一个元素
     * @date 2024/8/2 下午4:29
     */
    LAST(1),

    ;

    PopType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    private final Integer value;

}
