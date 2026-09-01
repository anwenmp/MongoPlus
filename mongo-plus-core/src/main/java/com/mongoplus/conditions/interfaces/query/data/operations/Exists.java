package com.mongoplus.conditions.interfaces.query.data.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * exists操作
 *
 * @author anwen
 * @mongodbOperator $exists
 */
public interface Exists<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 字段是否存在
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children exists(boolean condition, SFunction<T,Object> column, Boolean value) {
        return condition ? exists(column, value) : typeThis();
    }

    /**
     * 字段是否存在
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children exists(SFunction<T,Object> column,Boolean value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 字段是否存在
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children exists(boolean condition,String column,Boolean value) {
        return condition ? exists(column, value) : typeThis();
    }

    /**
     * 字段是否存在
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children exists(String column,Boolean value) {
        return addCondition(getBaseCondition(column, value));
    }

}
