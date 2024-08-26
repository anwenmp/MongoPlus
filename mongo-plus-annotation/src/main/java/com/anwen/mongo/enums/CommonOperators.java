package com.anwen.mongo.enums;

/**
 * 操作符
 * @author anwen
 * @date 2024/8/25 11:18
 */
public enum CommonOperators {

    ABS("$abs"),



    ;

    private final String operator;

    CommonOperators(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

}
