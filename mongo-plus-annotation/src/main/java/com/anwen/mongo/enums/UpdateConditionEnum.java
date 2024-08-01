package com.anwen.mongo.enums;

/**
 * update条件枚举
 * @author anwen
 * @date 2024/8/1 下午2:10
 */
public enum UpdateConditionEnum {

    SET("$set"),

    PUSH("$push"),

    INC("$inc"),

    ;

    private final String condition;

    public String getCondition() {
        return condition;
    }

    public String getSubCondition(){
        return condition.substring(1);
    }

    UpdateConditionEnum(String operator) {
        this.condition = operator;
    }

}
