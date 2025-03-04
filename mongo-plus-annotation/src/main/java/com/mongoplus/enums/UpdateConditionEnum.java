package com.mongoplus.enums;

/**
 * update条件枚举
 * @author anwen
 * @date 2024/8/1 下午2:10
 */
public enum UpdateConditionEnum {

    SET("$set"),

    PUSH("$push"),

    INC("$inc"),

    CURRENT_DATE("$currentDate"),

    MIN("$min"),

    MAX("$max"),

    MUL("$mul"),

    RENAME("$rename"),

    UNSET("$unset"),

    ADD_TO_SET("$addToSet"),

    POP("$pop"),

    PULL("$pull"),

    PULL_ALL("$pullAll"),

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
