package com.mongoplus.conditions.interfaces.compare.operations;

import com.mongoplus.support.SFunction;

import java.util.Collection;

/**
 * nin比较
 * @author anwen
 */
public interface Nin<T,Children> {

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children nin(boolean condition , SFunction<T,Object> column , Collection<?> valueList);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children nin(SFunction<T,Object> column , Collection<?> valueList);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(boolean condition , SFunction<T,Object> column , TItem... values);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(SFunction<T,Object> column , TItem... values);

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children nin(boolean condition , String column , Collection<?> valueList);

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(boolean condition , String column , TItem... values);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(String column , TItem... values);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children nin(String column , Collection<?> valueList);

}
