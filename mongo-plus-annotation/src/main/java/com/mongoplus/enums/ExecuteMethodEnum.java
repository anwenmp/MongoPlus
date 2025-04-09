package com.mongoplus.enums;

import java.util.Arrays;

/**
 * 执行器方法枚举
 *
 * @author JiaChaoYang
 **/
public enum ExecuteMethodEnum {

    /**
     * 新增方法
     */
    SAVE("executeSave"),

    /**
     * 删除方法
     */
    REMOVE("executeRemove"),

    /**
     * 修改方法
     */
    UPDATE("executeUpdate"),

    /**
     * 查询方法
     */
    QUERY("executeQuery"),

    /**
     * 管道执行方法
     */
    AGGREGATE("executeAggregate"),

    /**
     * 统计方法
     */
    COUNT("executeCount"),

    /**
     * 不接受任何参数的统计方法
     */
    ESTIMATED_DOCUMENT_COUNT("estimatedDocumentCount"),

    /**
     * 批量执行方法
     */
    BULK_WRITE("executeBulkWrite")

    ;

    private final String method;

    ExecuteMethodEnum(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    public static ExecuteMethodEnum getMethod(String method){
        return Arrays.stream(ExecuteMethodEnum.values())
                .filter(executeMethodEnumMethod -> executeMethodEnumMethod.getMethod().equals(method))
                .findFirst()
                .orElse(null);
    }

}
