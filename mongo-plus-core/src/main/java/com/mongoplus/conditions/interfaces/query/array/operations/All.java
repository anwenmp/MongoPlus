package com.mongoplus.conditions.interfaces.query.array.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

import java.util.Collection;

/**
 * all操作
 *
 * @author anwen
 */
public interface All<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children all(boolean condition, SFunction<T,Object> column, Collection<?> value) {
        return condition ? all(column, value) : typeThis();
    }

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children all(SFunction<T,Object> column,Collection<?> value) {
        return addCondition(getBaseCondition(column,value));
    }

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children all(boolean condition,String column,Collection<?> value) {
        return condition ? all(column, value) : typeThis();
    }

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children all(String column,Collection<?> value) {
        return addCondition(getBaseCondition(column,value));
    }

}
