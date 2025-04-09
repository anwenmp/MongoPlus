package com.mongoplus.handlers;

import org.bson.BsonValue;

import java.util.List;

/**
 * 多租户处理器
 *
 * @author anwen
 * @since by mybatis-plus
 */
public interface TenantHandler {

    /**
     * 获取租户
     * <p>示例：{@code new BsonString(tenantId)}<p/>
     * @author anwen
     */
    BsonValue getTenantId();

    /**
     * 获取租户字段
     * 默认字段名：tenant_id
     * @author anwen
     */
    default String getTenantIdColumn(){
        return "tenant_id";
    }

    /**
     * 根据集合名判断是否忽略租户
     * @param collectionName 集合名
     * @return {@link boolean}
     * @author anwen
     */
    default boolean ignoreCollection(String collectionName){
        return false;
    }

    /**
     * 根据数据库判断是否忽略租户
     * @param database 数据库
     * @author anwen
     */
    default boolean ignoreDatabase(String database){
        return false;
    }

    /**
     * 根据数据源判断是否忽略租户
     * @param dataSource 数据源
     * @author anwen
     */
    default boolean ignoreDataSource(String dataSource){
        return false;
    }

    /**
     * 忽略插入租户字段逻辑
     * @param columns 插入字段
     * @param tenantIdColumn 租户id字段
     * @return {@link boolean}
     * @author anwen
     */
    default boolean ignoreInsert(List<String> columns,String tenantIdColumn){
        return columns.stream().anyMatch(column -> column.equals(tenantIdColumn));
    }

}
