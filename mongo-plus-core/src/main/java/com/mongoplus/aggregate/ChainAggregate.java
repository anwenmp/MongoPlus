package com.mongoplus.aggregate;

import java.util.List;

public interface ChainAggregate<T> {

    /**
     * 获取列表 返回T类型的List
     * @return {@link List<T>}
     * @author JiaChaoYang
     * @date 2023/7/20 23:13
     */
    List<T> list();

    /**
     * 获取列表，返回Class<R>类型的List
     * @param rClazz 返回类型
     * @return {@link List<R>}
     * @author anwen
     * @date 2024/6/19 下午11:37
     */
    <R> List<R> list(Class<R> rClazz);

    /**
     * 获取单个，返回T类型
     * @return {@link T}
     * @author anwen
     * @date 2024/10/30 14:27
     */
    T one();

    /**
     * 获取单个，返回Class<R>类型
     * @param rClazz 返回类型
     * @return {@link R}
     * @author anwen
     * @date 2024/10/30 14:28
     */
    <R> R one(Class<R> rClazz);

}
