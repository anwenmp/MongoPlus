package com.mongoplus.conditions.interfaces.update.array.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;

/**
 * pull操作
 *
 * @author anwen
 */
public interface Pull<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? pull(column,value) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(SFunction<T,Object> column,Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value,false));
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(boolean condition, Wrapper<?> queryWrapper) {
        return condition ? pull(queryWrapper) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 值 条件
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(boolean condition, SFunction<Wrapper<T>, Wrapper<T>> function) {
        return condition ? pull(function) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param queryWrapper 实体对象封装操作类 {@link com.mongoplus.conditions.query.QueryWrapper}
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(Wrapper<?> queryWrapper) {
        return addUpdateCondition(getBaseUpdateCompare(queryWrapper,true));
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param function 值 条件
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(SFunction<Wrapper<T>, Wrapper<T>> function) {
        return pull(function.apply(new QueryWrapper<>()));
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(boolean condition,String column,Object value) {
        return condition ? pull(column,value) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pull(String column,Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value,false));
    }

}
