package com.anwen.mongo.enums;

/**
 * 多写枚举
 */
public enum MultipleWrite {

    /**
     * 新增
     */
    SAVE,

    /**
     * 修改
     */
    UPDATE,

    /**
     * 删除
     */
    REMOVE,

    /**
     * 批量操作
     */
    BULK_WRITE

}
