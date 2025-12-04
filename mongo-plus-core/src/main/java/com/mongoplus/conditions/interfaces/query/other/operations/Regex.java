package com.mongoplus.conditions.interfaces.query.other.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.RegexOptions;
import com.mongoplus.support.SFunction;

import java.util.regex.Pattern;

/**
 * regex操作
 *
 * @author anwen
 */
public interface Regex<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 正则表达式查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? regex(column, value) : typeThis();
    }

    /**
     * 正则表达式查询
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(SFunction<T,Object> column,Object value) {
        return regex(column, value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 正则表达式查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(boolean condition,String column,Object value) {
        return condition ? regex(column, value) : typeThis();
    }

    /**
     * 正则表达式查询
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(String column,Object value) {
        return regex(column, value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 正则表达式查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(boolean condition, SFunction<T,Object> column, Object value, RegexOptions options) {
        return condition ? regex(column, value, options) : typeThis();
    }

    /**
     * 正则表达式查询
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(SFunction<T,Object> column,Object value, RegexOptions options) {
        if (value instanceof Pattern){
            value = ((Pattern) value).pattern();
        }
        return addCondition(getBaseConditionExtraValue(column,value,options));
    }

    /**
     * 正则表达式查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(boolean condition,String column,Object value, RegexOptions options) {
        return condition ? regex(column, value, options) : typeThis();
    }

    /**
     * 正则表达式查询
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    default Children regex(String column,Object value, RegexOptions options) {
        if (value instanceof Pattern){
            value = ((Pattern) value).pattern();
        }
        return addCondition(getBaseConditionExtraValue(column,value,options));
    }

}
