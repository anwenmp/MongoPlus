package com.mongoplus.conditions.interfaces.logic.operations;

import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.support.SFunction;

/**
 * nor逻辑
 * @author anwen
 */
public interface Nor<T, Children> {

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children nor(boolean condition , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 查询的文档必须不符合所有条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children nor(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children nor(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);


    /**
     * 查询的文档必须不符合所有条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children nor(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

}
