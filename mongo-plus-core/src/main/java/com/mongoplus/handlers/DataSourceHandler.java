package com.mongoplus.handlers;

/**
 * 动态数据源处理器
 * @author anwen
 */
public interface DataSourceHandler {

    /**
     * 获取数据源
     * @param dsName {@link com.mongoplus.annotation.datasource.MongoDs}注解的value的值
     * @author anwen
     */
    String getDataSource(String dsName);

}
