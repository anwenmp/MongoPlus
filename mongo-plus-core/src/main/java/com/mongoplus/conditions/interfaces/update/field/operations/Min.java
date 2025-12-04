package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;

/**
 * min操作
 *
 * @author anwen
 */
public interface Min<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 指定字段值小于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children min(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? min(column,value) : typeThis();
    }

    /**
     * 指定字段值小于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children min(SFunction<T,Object> column, Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

    /**
     * 指定字段值小于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children min(boolean condition,String column, Object value) {
        return condition ? min(column,value) : typeThis();
    }

    /**
     * 指定字段值小于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children min(String column, Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

}
