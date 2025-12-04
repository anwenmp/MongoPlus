package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;

/**
 * setOnInsert操作
 *
 * @author anwen
 */
public interface SetOnInsert<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 将指定值分配给文档中的字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children setOnInsert(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? setOnInsert(column,value) : typeThis();
    }

    /**
     * 将指定值分配给文档中的字段
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children setOnInsert(SFunction<T,Object> column, Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column, value));
    }

    /**
     * 将指定值分配给文档中的字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children setOnInsert(boolean condition, String column, Object value) {
        return condition ? setOnInsert(column,value) : typeThis();
    }

    /**
     * 将指定值分配给文档中的字段
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children setOnInsert(String column, Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column, value));
    }

}
