package com.mongoplus.conditions.interfaces.update.array.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.enums.PopType;
import com.mongoplus.support.SFunction;

/**
 * pop操作
 *
 * @author anwen
 */
public interface Pop<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 删除数组中第一个或最后一个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    default Children pop(boolean condition, SFunction<T,Object> column, PopType popType) {
        return condition ? pop(column, popType) : typeThis();
    }

    /**
     * 删除数组中第一个或最后一个元素
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    default Children pop(SFunction<T,Object> column, PopType popType) {
        return addUpdateCondition(getBaseUpdateCompare(column, popType.getValue()));
    }

    /**
     * 删除数组中第一个或最后一个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    default Children pop(boolean condition,String column, PopType popType) {
        return condition ? pop(column, popType) : typeThis();
    }

    /**
     * 删除数组中第一个或最后一个元素
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    default Children pop(String column, PopType popType) {
        return addUpdateCondition(getBaseUpdateCompare(column, popType.getValue()));
    }

}
