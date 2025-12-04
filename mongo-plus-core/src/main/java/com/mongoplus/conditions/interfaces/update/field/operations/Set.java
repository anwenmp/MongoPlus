package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;

/**
 * set操作
 *
 * @author anwen
 */
public interface Set<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 设置值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children set(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? set(column,value) : typeThis();
    }

    /**
     * 设置值
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children set(SFunction<T,Object> column, Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

    /**
     * 设置值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children set(boolean condition, String column, Object value) {
        return condition ? set(column,value) : typeThis();
    }

    /**
     * 设置值
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children set(String column, Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value));
    }

    /**
     * 将字段值设置为null
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children setNull(boolean condition, SFunction<T,Object> column) {
        return condition ? setNull(column) : typeThis();
    }

    /**
     * 将字段值设置为null
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children setNull(SFunction<T,Object> column) {
        return set(column, Null.class);
    }

    /**
     * 将字段值设置为null
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children setNull(boolean condition, String column) {
        return condition ? setNull(column) : typeThis();
    }

    /**
     * 将字段值设置为null
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children setNull(String column) {
        return set(column, Null.class);
    }

}
