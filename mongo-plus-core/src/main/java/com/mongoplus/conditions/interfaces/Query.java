package com.mongoplus.conditions.interfaces;

import com.mongoplus.support.SFunction;

public interface Query<T,Children> extends Project<T,Children> {

    Children order(SFunction<T,Object> column,Integer order);

    /**
     * 自定义排序
     * @param column 字段
     * @return {@link Children}
     * @author anwen
     */
    Children order(String column,Integer order);

    /**
     * 正序排序
     * @param column 列名、字段名，lambda方式
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    Children orderByAsc(SFunction<T, Object> column);

    /**
     * 倒序排序
     * @param column 列名、字段名，lambda方式
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    Children orderByDesc(SFunction<T,Object> column);

    /**
     * 正序排序
     * @param column 列名、字段名
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    Children orderByAsc(String column);

    /**
     * 倒序排序
     * @param column 列名、字段名，lambda方式
     * @return com.mongoplus.sql.query.LambdaQueryMongoWrapper<T>
     * @author JiaChaoYang
     */
    Children orderByDesc(String column);

}
