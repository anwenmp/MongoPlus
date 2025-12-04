package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.model.MutablePair;
import com.mongoplus.support.SFunction;

/**
 * rename操作
 * @author anwen
 */
public interface Rename<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    default Children rename(boolean condition, String oldFieldName,String newFieldName) {
        return condition ? rename(oldFieldName,newFieldName) : typeThis();
    }

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    default Children rename(String oldFieldName,String newFieldName) {
        return addUpdateCondition(getBaseUpdateCompare(new MutablePair<>(oldFieldName, newFieldName)));
    }

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    default <O> Children rename(boolean condition, SFunction<O,Object> oldFieldName, String newFieldName) {
        return condition ? rename(oldFieldName,newFieldName) : typeThis();
    }

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    default <O> Children rename(SFunction<O,Object> oldFieldName,String newFieldName) {
        return addUpdateCondition(getBaseUpdateCompare(new MutablePair<>(oldFieldName.getFieldNameLine(),newFieldName)));
    }

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    default <O,N> Children rename(boolean condition, SFunction<O,Object> oldFieldName,SFunction<N,Object> newFieldName) {
        return condition ? rename(oldFieldName,newFieldName) : typeThis();
    }

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    default <O,N> Children rename(SFunction<O,Object> oldFieldName,SFunction<N,Object> newFieldName) {
        return addUpdateCondition(getBaseUpdateCompare(
                new MutablePair<>(oldFieldName.getFieldNameLine(),newFieldName.getFieldNameLine())));
    }

}
