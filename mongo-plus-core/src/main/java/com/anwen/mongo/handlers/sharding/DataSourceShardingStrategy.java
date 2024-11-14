package com.anwen.mongo.handlers.sharding;

import com.anwen.mongo.enums.ExecuteMethodEnum;

/**
 * 数据源分片策略
 * @author anwen
 */
public interface ShardingStrategy {

    /**
     * 自定义分片策略略
     * @param currentDataSource 当前所使用数据源
     * @param method 执行的方法
     * @param source 方法参数值
     * @return {@link java.lang.String}
     * @author anwen
     * @date 2024/11/14 15:47
     */
    String sharding(String currentDataSource, ExecuteMethodEnum method,Object[] source);

}
