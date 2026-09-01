package com.mongoplus.conditions.interfaces.query.compare.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * nin比较
 * @author anwen
 * @mongodbOperator $nin
 */
public interface Nin<T,Children> extends BaseQueryCondition<T, Children> {

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children nin(boolean condition , SFunction<T,Object> column , Collection<?> valueList) {
        return condition ? nin(column,valueList) : typeThis();
    }

    /**
     * 不包含
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children nin(SFunction<T,Object> column , Collection<?> valueList) {
        return addCondition(getBaseCondition(column, valueList));
    }

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children nin(boolean condition , SFunction<T,Object> column , TItem... values) {
        return condition ? nin(column,values) : typeThis();
    }

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children nin(SFunction<T,Object> column , TItem... values) {
        return addCondition(getBaseCondition(column, new ArrayList<>(Arrays.asList(values))));
    }

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children nin(boolean condition , String column , Collection<?> valueList) {
        return condition ? nin(column,valueList) : typeThis();
    }

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children nin(boolean condition , String column , TItem... values) {
        return condition ? nin(column,values) : typeThis();
    }

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    default <TItem> Children nin(String column , TItem... values) {
        return addCondition(getBaseCondition(column, new ArrayList<>(Arrays.asList(values))));
    }

    /**
     * 不包含
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    default Children nin(String column , Collection<?> valueList) {
        return addCondition(getBaseCondition(column, valueList));
    }

}
