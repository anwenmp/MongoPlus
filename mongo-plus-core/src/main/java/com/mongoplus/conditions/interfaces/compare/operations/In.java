package com.mongoplus.conditions.interfaces.compare.operations;

import com.mongoplus.support.SFunction;

import java.util.Collection;

/**
 * in比较
 * @author anwen
 */
public interface In<T,Children> {

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(boolean condition, SFunction<T,Object> column, Collection<?> valueList);

    /**
     * 多值查询
     * @param column 列名、字段名，lambda方式
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(SFunction<T,Object> column, Collection<?> valueList);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(boolean condition, SFunction<T,Object> column, TItem... values);

    /**
     * 多值查询
     *
     * @param column 列名、字段名，lambda方式
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(SFunction<T,Object> column, TItem... values);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(boolean condition, String column, Collection<?> valueList);

    /**
     * 多值查询
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(String column, Collection<?> valueList);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(boolean condition,String column,TItem... values);

    /**
     * 多值查询
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(String column,TItem... values);

}
