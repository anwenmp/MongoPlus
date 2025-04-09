package com.mongoplus.enums;

/**
 * 一致性读策略枚举
 * @author JiaChaoYang
 */
public enum ReadConcernEnum {

    /**
     * 使用服务器默认的读取策略
     */
    DEFAULT,

    /**
     * 读取所有可用且属于当前分片的数据
     */
    LOCAL,

    /**
     * 读取在大多数节点上提交完成的数据
     */
    MAJORITY,

    /**
     * 可线性化读取文档
     */
    LINEARIZABLE,

    /**
     * 读取所有可用的数据
     */
    AVAILABLE,

    /**
     * 读取最近快照中的数据
     */
    SNAPSHOT;
}
