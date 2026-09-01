package com.mongoplus.conditions.interfaces.query.array.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.support.SFunction;

/**
 * elemMatch操作
 *
 * @author anwen
 * @mongodbOperator $elemMatch
 */
public interface ElemMatch<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配数组中的值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
     */
    default Children elemMatch(boolean condition, SFunction<T,Object> column ,
                               QueryChainWrapper<?,?> queryChainWrapper) {
        return condition ? elemMatch(column, queryChainWrapper) : typeThis();
    }

    /**
     * 匹配数组中的值
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
     */
    default Children elemMatch(SFunction<T,Object> column , QueryChainWrapper<?,?> queryChainWrapper) {
        return addCondition(getBaseCondition(column,queryChainWrapper));
    }

    /**
     * 匹配数组中的值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
     */
    default Children elemMatch(boolean condition,String column , QueryChainWrapper<?,?> queryChainWrapper) {
        return condition ? elemMatch(column, queryChainWrapper) : typeThis();
    }

    /**
     * 匹配数组中的值
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
     */
    default Children elemMatch(String column , QueryChainWrapper<?,?> queryChainWrapper) {
        return addCondition(getBaseCondition(column,queryChainWrapper));
    }

}
