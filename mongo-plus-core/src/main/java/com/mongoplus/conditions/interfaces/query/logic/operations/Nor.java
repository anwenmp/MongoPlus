package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * nor逻辑
 * @author anwen
 */
public interface Nor<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    default Children nor(boolean condition , QueryChainWrapper<?,?> queryChainWrapper) {
        return condition ? nor(queryChainWrapper) : typeThis();
    }

    /**
     * 查询的文档必须不符合所有条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    default Children nor(QueryChainWrapper<?,?> queryChainWrapper) {
        return addCondition(getBaseCondition(queryChainWrapper));
    }

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children nor(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return condition ? nor(function) : typeThis();
    }


    /**
     * 查询的文档必须不符合所有条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children nor(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return nor(function.apply(new QueryWrapper<>()));
    }

}
