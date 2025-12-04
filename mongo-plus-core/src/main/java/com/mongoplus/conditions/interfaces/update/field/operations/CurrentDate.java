package com.mongoplus.conditions.interfaces.update.field.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.enums.CurrentDateType;
import com.mongoplus.support.SFunction;

/**
 * currentDate操作
 *
 * @author anwen
 */
public interface CurrentDate<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 将字段的值设置为当前日期
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(boolean condition, SFunction<T,Object> column) {
        return condition ? currentDate(column) : typeThis();
    }

    /**
     * 将字段的值设置为当前日期
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(SFunction<T,Object> column) {
        return currentDate(column,CurrentDateType.DATE);
    }

    /**
     * 将字段的值设置为当前日期
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(boolean condition,String column) {
        return condition ? currentDate(column) : typeThis();
    }

    /**
     * 将字段的值设置为当前日期
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(String column) {
        return currentDate(column,CurrentDateType.DATE);
    }

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(boolean condition,SFunction<T,Object> column, CurrentDateType currentDateType) {
        return condition ? currentDate(column,currentDateType) : typeThis();
    }

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(SFunction<T,Object> column, CurrentDateType currentDateType) {
        return addUpdateCondition(getBaseUpdateCompare(column,currentDateType));
    }

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(boolean condition,String column, CurrentDateType currentDateType) {
        return condition ? currentDate(column,currentDateType) : typeThis();
    }

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    default Children currentDate(String column, CurrentDateType currentDateType) {
        return addUpdateCondition(getBaseUpdateCompare(column,currentDateType));
    }

}
