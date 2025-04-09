package com.mongoplus.aggregate;

import java.util.List;

public interface ChainAggregate<T> {

    /**
     * 获取列表 返回T类型的List
     * @return {@link List<T>}
     * @author JiaChaoYang
     */
    List<T> list();

    /**
     * 获取列表，返回Class<R>类型的List
     * @param rClazz 返回类型
     * @return {@link List<R>}
     * @author anwen
     */
    <R> List<R> list(Class<R> rClazz);

    /**
     * 获取单个，返回T类型
     * @return {@link T}
     * @author anwen
     */
    T one();

    /**
     * 获取单个，返回Class<R>类型
     * @param rClazz 返回类型
     * @return {@link R}
     * @author anwen
     */
    <R> R one(Class<R> rClazz);

}
