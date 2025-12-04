package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;

/**
 * inc操作
 *
 * @author anwen
 */
public interface Inc<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 对指定值原子性的递增
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children inc(boolean condition, SFunction<T,Object> column, Number value) {
        return condition ? inc(column,value) : typeThis();
    }

    /**
     * 对指定值原子性的递增
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children inc(SFunction<T,Object> column,Number value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

    /**
     * 对指定值原子性的递增
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children inc(boolean condition,String column,Number value) {
        return condition ? inc(column,value) : typeThis();
    }

    /**
     * 对指定值原子性的递增
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children inc(String column,Number value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

}
