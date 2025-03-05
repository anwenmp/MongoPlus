package com.mongoplus.enums;

/**
 * 敏感词校验类型
 *
 * @author anwen
 */
public enum SensitiveType {

    /**
     * 全局校验
     */
    GLOBAL,

    /**
     * 局部校验,通过注解指定需要校验的字段
     */
    LOCAL,

    ;

}
