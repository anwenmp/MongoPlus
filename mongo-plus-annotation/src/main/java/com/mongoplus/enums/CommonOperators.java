package com.mongoplus.enums;

/**
 * 操作符
 * @author anwen
 * @date 2024/8/25 11:18
 */
public enum CommonOperators {

    OPTIONS("$options"),

    ABS("$abs"),

    TO_DATE("$toDate"),

    DATE_TO_STRING("$dateToString"),

    MULTIPLY("$multiply"),

    COND("$cond"),

    DATE_FROM_STRING("$dateFromString"),

    TO_BOOL("$toBool"),

    TO_DECIMAL("$toDecimal"),

    TO_DOUBLE("$toDouble"),

    TO_HASHED_INDEX_KEY("$toHashedIndexKey"),

    TO_INT("$toInt"),

    TO_LONG("$toLong"),

    TO_OBJECT_ID("$toObjectId"),

    TO_STRING("$toString"),

    SUBSTR_BYTES("$substrBytes"),

    IF_NULL("$ifNull"),

    SUM("$sum"),

    ADD("$add"),

    CONCAT_ARRAYS("$concatArrays"),

    CONCAT("$concat"),

    ;

    private final String operator;

    CommonOperators(String operator) {
        this.operator = operator;
    }

    public String getOperator() {
        return operator;
    }

}
