package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * unset操作
 *
 * @author anwen
 */
public interface Unset<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default Children unset(SFunction<T,Object>... columns) {
        return unset(Arrays.stream(columns).map(SFunction::getFieldNameLine).collect(Collectors.toList()));
    }

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default Children unset(boolean condition,SFunction<T,Object>... columns) {
        return condition ? unset(columns) : typeThis();
    }

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    default Children unset(String... columns) {
        return unset(Arrays.asList(columns));
    }

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    default Children unset(boolean condition,String... columns) {
        return condition ? unset(columns) : typeThis();
    }

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    default Children unset(List<String> columns) {
        return addUpdateCondition(getBaseUpdateCompare(columns));
    }

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    default Children unset(boolean condition,List<String> columns) {
        return condition ? unset(columns) : typeThis();
    }

}
