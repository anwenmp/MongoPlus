package com.mongoplus.enums;

/**
 * 结果处理
 */
public enum ResultHandler {

    /**
     * 拒绝，直接抛出异常
     */
    REJECT,

    /**
     * 替换为指定字符
     */
    MASK,
}
