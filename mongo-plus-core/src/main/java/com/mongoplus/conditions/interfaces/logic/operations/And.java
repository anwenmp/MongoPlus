package com.mongoplus.conditions.interfaces.logic.operations;

import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.support.SFunction;

/**
 * and逻辑
 * @author anwen
 */
public interface And<T, Children> {

    /**
     * and
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children and(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children and(boolean condition,QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children and(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);


    /**
     * and
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children and(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

}
