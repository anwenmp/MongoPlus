package com.anwen.mongo.handlers.sharding;

import com.anwen.mongo.enums.ExecuteMethodEnum;

import java.util.List;

/**
 * 数据源分片策略
 * @author anwen
 */
public interface DataSourceShardingStrategy {

    /**
     * 自定义分片策略
     * @param currentDataSource 当前method所映射的数据源
     * @param method 执行的方法
     * @param source 方法参数值
     * @return {@link String} 数据源名称，亦可返回带*号的通配符，或完整的正则表达式
     * @author anwen
     * @date 2024/11/14 15:47
     */
    String sharding(List<String> currentDataSource, ExecuteMethodEnum method, Object[] source);

}
