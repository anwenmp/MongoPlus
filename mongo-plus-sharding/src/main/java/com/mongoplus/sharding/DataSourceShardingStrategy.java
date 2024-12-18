package com.mongoplus.sharding;

import com.mongoplus.enums.ExecuteMethodEnum;

/**
 * 数据源分片策略
 * @author anwen
 */
public interface DataSourceShardingStrategy {

    /**
     * 自定义分片策略
     * @param currentDataSource 当前数据源
     * @param method 执行的方法
     * @param source 方法参数值
     * @return {@link String} 数据源名称，亦可返回带*号的通配符，或完整的正则表达式
     * @author anwen
     */
    String sharding(String currentDataSource, ExecuteMethodEnum method, Object[] source);

}
