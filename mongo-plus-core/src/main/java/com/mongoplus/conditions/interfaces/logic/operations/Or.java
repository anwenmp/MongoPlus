package com.mongoplus.conditions.interfaces.logic.operations;

import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.support.SFunction;

/**
 * or逻辑
 * @author anwen
 */
public interface Or<T, Children> {

    /**
     * 或者
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children or(boolean condition , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 或者
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children or(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 或者
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children or(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);


    /**
     * 或者
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children or(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

}
