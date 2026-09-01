package com.mongoplus.conditions.interfaces.query.compare.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * lt比较
 * @author anwen
 * @mongodbOperator $lt
 */
public interface Lt<T,Children> extends BaseQueryCondition<T, Children> {

    /**
     * 小于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children lt(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? lt(column,value) : typeThis();
    }

    /**
     * 小于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children lt(SFunction<T,Object> column, Object value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children lt(boolean condition, String column, Object value) {
        return condition ? lt(column,value) : typeThis();
    }

    /**
     * 等于
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children lt(String column, Object value) {
        return addCondition(getBaseCondition(column, value));
    }

}
