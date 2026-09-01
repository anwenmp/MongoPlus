package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * and逻辑
 * @author anwen
 * @mongodbOperator $and
 */
public interface And<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * and
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    default Children and(QueryChainWrapper<?,?> queryChainWrapper) {
        return addCondition(getBaseCondition(queryChainWrapper));
    }

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    default Children and(boolean condition,QueryChainWrapper<?,?> queryChainWrapper) {
        return condition ? addCondition(getBaseCondition(queryChainWrapper)) : typeThis();
    }

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children and(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return condition ? addCondition(getBaseCondition(function)) : typeThis();
    }


    /**
     * and
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children and(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return and(function.apply(new QueryWrapper<>()));
    }

}
