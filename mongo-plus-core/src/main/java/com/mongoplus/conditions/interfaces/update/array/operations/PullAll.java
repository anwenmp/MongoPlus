package com.mongoplus.conditions.interfaces.update.array.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.model.MutablePair;
import com.mongoplus.support.SFunction;

import java.util.Arrays;
import java.util.Collection;

/**
 * pullAll操作
 *
 * @author anwen
 */
public interface PullAll<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pullAll(boolean condition, SFunction<T,Object> column, Object... values) {
        return condition ? pullAll(column, values) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pullAll(SFunction<T,Object> column, Object... values) {
        return addUpdateCondition(getBaseUpdateCompare(column, Arrays.asList(values)));
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pullAll(boolean condition,String column, Collection<?> values) {
        return condition ? pullAll(column, values) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pullAll(String column, Collection<?> values) {
        return addUpdateCondition(getBaseUpdateCompare(column, values));
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pullAll(boolean condition,String column, Object... values) {
        return condition ? pullAll(column, values) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    default Children pullAll(String column, Object... values) {
        return addUpdateCondition(getBaseUpdateCompare(column, Arrays.asList(values)));
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * <p>示例：{@code pullAll(MutablePair.of(User::getId, Arrays.asList(1,2,3)))}</p>
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param pullAllPair 条件
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default Children pullAll(boolean condition, MutablePair<String, Collection<?>>... pullAllPair) {
        return condition ? pullAll(pullAllPair) : typeThis();
    }

    /**
     * 删除数组中符合条件或符合指定值的实例
     * <p>示例：{@code pullAll(MutablePair.of(User::getId, Arrays.asList(1,2,3)))}</p>
     * @param pullAllPair 条件
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default Children pullAll(MutablePair<String, Collection<?>>... pullAllPair) {
        Arrays.stream(pullAllPair).forEach(pair ->
                pullAll(pair.getLeft(),pair.getValue())
        );
        return typeThis();
    }

}
