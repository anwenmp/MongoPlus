package com.mongoplus.conditions.interfaces.query.logic.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * or逻辑
 * @author anwen
 * @mongodbOperator $or
 */
public interface Or<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 或者
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    default Children or(boolean condition , QueryChainWrapper<?,?> queryChainWrapper) {
        return condition ? or(queryChainWrapper) : typeThis();
    }

    /**
     * 或者
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    default Children or(QueryChainWrapper<?,?> queryChainWrapper) {
        return addCondition(getBaseCondition(queryChainWrapper));
    }

    /**
     * 或者
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children or(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return condition ? or(function) : typeThis();
    }


    /**
     * 或者
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    default Children or(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function) {
        return or(function.apply(new QueryWrapper<>()));
    }

}
