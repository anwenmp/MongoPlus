package com.mongoplus.conditions.interfaces.query.compare.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * 大于等于比较
 * @author anwen
 */
public interface Gte<T,Children> extends BaseQueryCondition<T, Children> {

    /**
     * 大于等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children gte(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? gte(column,value) : typeThis();
    }

    /**
     * 大于等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children gte(SFunction<T,Object> column, Object value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 大于等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children gte(boolean condition, String column, Object value) {
        return condition ? gte(column,value) : typeThis();
    }

    /**
     * 大于等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children gte(String column, Object value) {
        return addCondition(getBaseCondition(column, value));
    }

}
