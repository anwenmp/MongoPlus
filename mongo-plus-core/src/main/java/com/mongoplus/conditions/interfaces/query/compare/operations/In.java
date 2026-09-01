package com.mongoplus.conditions.interfaces.query.compare.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * in比较
 * @author anwen
 * @mongodbOperator $in
 */
public interface In<T,Children> extends BaseQueryCondition<T, Children> {

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children in(boolean condition, SFunction<T,Object> column, Collection<?> valueList) {
        return condition ? in(column, valueList) : typeThis();
    }

    /**
     * 多值查询
     * @param column 列名、字段名，lambda方式
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children in(SFunction<T,Object> column, Collection<?> valueList) {
        return addCondition(getBaseCondition(column, valueList));
    }

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children in(boolean condition, SFunction<T,Object> column, TItem... values) {
        return condition ? in(column, values) : typeThis();
    }

    /**
     * 多值查询
     *
     * @param column 列名、字段名，lambda方式
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children in(SFunction<T,Object> column, TItem... values) {
        return addCondition(getBaseCondition(column, new ArrayList<>(Arrays.asList(values))));
    }

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children in(boolean condition, String column, Collection<?> valueList) {
        return condition ? in(column, valueList) : typeThis();
    }

    /**
     * 多值查询
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
     default Children in(String column, Collection<?> valueList) {
         return addCondition(getBaseCondition(column, valueList));
     }

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children in(boolean condition,String column,TItem... values) {
        return condition ? in(column, values) : typeThis();
    }

    /**
     * 多值查询
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children in(String column,TItem... values) {
        return addCondition(getBaseCondition(column, new ArrayList<>(Arrays.asList(values))));
    }

}
