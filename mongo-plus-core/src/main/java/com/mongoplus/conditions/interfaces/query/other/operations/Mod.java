package com.mongoplus.conditions.interfaces.query.other.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

import java.util.Arrays;
import java.util.Collection;

/**
 * mod操作
 *
 * @author anwen
 */
public interface Mod<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(boolean condition, SFunction<T,Object> column, long divide, long remain) {
        return condition ? mod(column, divide, remain) : typeThis();
    }

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(SFunction<T,Object> column,long divide,long remain) {
        return mod(column, Arrays.asList(divide,remain));
    }

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(boolean condition, SFunction<T,Object> column, Collection<Long> value) {
        return condition ? mod(column, value) : typeThis();
    }

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(SFunction<T,Object> column,Collection<Long> value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(boolean condition, String column, long divide, long remain) {
        return condition ? mod(column, divide, remain) : typeThis();
    }

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(String column, long divide, long remain) {
        return mod(column, Arrays.asList(divide,remain));
    }

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(boolean condition,String column,Collection<Long> value) {
        return condition ? mod(column, value) : typeThis();
    }

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
     */
    default Children mod(String column,Collection<Long> value) {
        return addCondition(getBaseCondition(column, value));
    }

}
