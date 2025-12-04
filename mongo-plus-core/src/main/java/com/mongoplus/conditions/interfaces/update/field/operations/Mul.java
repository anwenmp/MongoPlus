package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;

/**
 * mul操作
 *
 * @author anwen
 */
public interface Mul<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 将指定字段的值乘以value
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children mul(boolean condition, SFunction<T,Object> column, Number value) {
        return condition ? mul(column,value) : typeThis();
    }

    /**
     * 将指定字段的值乘以value
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children mul(SFunction<T,Object> column,Number value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

    /**
     * 将指定字段的值乘以value
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children mul(boolean condition,String column,Number value) {
        return condition ? mul(column,value) : typeThis();
    }

    /**
     * 将指定字段的值乘以value
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children mul(String column,Number value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

}
