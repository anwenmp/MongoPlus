package com.anwen.mongo.handlers;

import com.anwen.mongo.constant.DataSourceConstant;
import com.anwen.mongo.enums.ExecuteMethodEnum;
import com.anwen.mongo.toolkit.CollUtil;

import java.util.*;
import java.util.stream.Collectors;

import static com.anwen.mongo.enums.ExecuteMethodEnum.*;

/**
 * 分片处理器
 * @author anwen
 */
public class DataSourceShardingHandler {

    private static final String REGEX = "^(?!master$).*";

    /**
     * 分片策略
     * @date 2024/11/14 15:06
     */
    private Map<String, List<ExecuteMethodEnum>> shardingStrategy = new HashMap<>();

    /**
     * 默认的分片策略
     * <p>增删改使用master，查询使用从数据源</p>
     * @date 2024/11/14 15:06
     */
    public static final Map<String, List<ExecuteMethodEnum>> DEFAULT_SHARDING_STRATEGY = new HashMap<>();

    public Map<ExecuteMethodEnum,List<String>> handleShardingStrategy = new HashMap<>();

    static {
        DEFAULT_SHARDING_STRATEGY.put(DataSourceConstant.DEFAULT_DATASOURCE, Arrays.asList(
                SAVE,
                REMOVE,
                UPDATE,
                BULK_WRITE
        ));
        DEFAULT_SHARDING_STRATEGY.put(REGEX,Arrays.asList(
                QUERY,
                AGGREGATE,
                COUNT,
                ESTIMATED_DOCUMENT_COUNT
        ));
    }


    public DataSourceShardingHandler(){
        handle();
    }

    public DataSourceShardingHandler(Map<String, List<ExecuteMethodEnum>> shardingStrategy){
        this.shardingStrategy = shardingStrategy;
    }

    /**
     * 构建器
     * @return {@link com.anwen.mongo.handlers.DataSourceShardingHandler.DataSourceShardingBuild}
     * @author anwen
     * @date 2024/11/14 15:38
     */
    public static DataSourceShardingBuild builder(){
        return new DataSourceShardingBuild();
    }

    public Map<String, List<ExecuteMethodEnum>> getShardingStrategy() {
        return shardingStrategy;
    }

    void setShardingStrategy(Map<String, List<ExecuteMethodEnum>> shardingStrategy) {
        this.shardingStrategy = shardingStrategy;
    }

    void addShardingStrategy(String dsName,List<ExecuteMethodEnum> method){
        this.shardingStrategy.put(dsName,method);
    }

    void handle(){
        if (CollUtil.isEmpty(shardingStrategy)){
            shardingStrategy = DEFAULT_SHARDING_STRATEGY;
        }
        handleShardingStrategy = shardingStrategy.entrySet().stream()
                // 扁平化 entry 为 (ExecuteMethodEnum,String)对
                .flatMap(entry -> entry.getValue().stream()
                        .map(method -> new AbstractMap.SimpleEntry<>(method,entry.getKey())))
                // 分组并映射为目标格式
                .collect(Collectors.groupingBy(
                        // 按 ExecuteMethodEnum分组
                        Map.Entry::getKey,
                        // 收集每个 ExecuteMethodEnum 对应的 List<String>
                        Collectors.mapping(Map.Entry::getValue,Collectors.toList())
                ));
    }

    public static class DataSourceShardingBuild {

        DataSourceShardingHandler handler = new DataSourceShardingHandler();

        /**
         * 设置分片映射
         * @param dsName 数据源名称
         * @param method 所执行的操作
         * @return {@link com.anwen.mongo.handlers.DataSourceShardingHandler.DataSourceShardingBuild}
         * @author anwen
         * @date 2024/11/14 15:20
         */
        public DataSourceShardingBuild setShardingMapping(String dsName,List<ExecuteMethodEnum> method){
            handler.addShardingStrategy(dsName,method);
            return this;
        }

        /**
         * 设置分片映射
         * @param dsName 数据源名称
         * @param method 所执行的操作
         * @return {@link com.anwen.mongo.handlers.DataSourceShardingHandler.DataSourceShardingBuild}
         * @author anwen
         * @date 2024/11/14 15:20
         */
        public DataSourceShardingBuild setShardingMapping(String dsName,ExecuteMethodEnum... method){
            handler.addShardingStrategy(dsName, Arrays.stream(method).collect(Collectors.toList()));
            return this;
        }

        public DataSourceShardingHandler build(){
            handler.handle();
            return handler;
        }

    }

}
