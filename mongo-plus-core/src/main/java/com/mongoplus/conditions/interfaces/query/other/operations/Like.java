package com.mongoplus.conditions.interfaces.query.other.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.RegexOptions;
import com.mongoplus.support.SFunction;

import java.util.regex.Pattern;

/**
 * like操作
 *
 * @author anwen
 * @mongodbOperator $regex
 */
public interface Like<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     * @aiAlias 正则
     * @aiAlias 正则表达式
     * @aiAlias 模糊查询
     */
    default Children like(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? like(column,value) : typeThis();
    }

    /**
     * 包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(SFunction<T,Object> column, Object value) {
        return like(column,value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(boolean condition, SFunction<T,Object> column, Object value, RegexOptions options) {
        return condition ? like(column,value,options) : typeThis();
    }

    /**
     * 包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(SFunction<T,Object> column, Object value, RegexOptions options) {
        if (value instanceof Pattern){
            value = ((Pattern) value).pattern();
        }
        return addCondition(getBaseConditionExtraValue(column,value,options));
    }

    /**
     * 包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(boolean condition, String column, Object value) {
        return condition ? like(column,value) : typeThis();
    }

    /**
     * 包含（模糊查询）
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(String column, Object value) {
        return like(column,value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(boolean condition, String column, Object value, RegexOptions options) {
        return condition ? like(column,value,options) : typeThis();
    }

    /**
     * 包含（模糊查询）
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children like(String column, Object value, RegexOptions options) {
        if (value instanceof Pattern){
            value = ((Pattern) value).pattern();
        }
        return addCondition(getBaseConditionExtraValue(column,value,options));
    }

    /**
     * 左包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(boolean condition , SFunction<T,Object> column, Object value) {
        return condition ? likeLeft(column,value) : typeThis();
    }

    /**
     * 左包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(SFunction<T,Object> column, Object value) {
        return likeLeft(column,value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 左包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(boolean condition, SFunction<T,Object> column, Object value, RegexOptions options) {
        return condition ? likeLeft(column,value,options) : typeThis();
    }

    /**
     * 左包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(SFunction<T,Object> column, Object value, RegexOptions options) {
        return like(column, "^"+value, options);
    }


    /**
     * 左包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(boolean condition , String column, Object value) {
        return condition ? likeLeft(column,value) : typeThis();
    }

    /**
     * 左包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(String column, Object value) {
        return likeLeft(column,value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 左包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(boolean condition , String column, Object value, RegexOptions options) {
        return condition ? likeLeft(column,value,options) : typeThis();
    }

    /**
     * 左包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeLeft(String column, Object value, RegexOptions options) {
        return like(column, "^"+value, options);
    }

    /**
     * 右包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(boolean condition , SFunction<T,Object> column, Object value) {
        return condition ? likeRight(column,value) : typeThis();
    }

    /**
     * 右包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(SFunction<T,Object> column, Object value) {
        return likeRight(column,value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 右包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(boolean condition , SFunction<T,Object> column, Object value, RegexOptions options) {
        return condition ? likeRight(column,value,options) : typeThis();
    }

    /**
     * 右包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(SFunction<T,Object> column, Object value, RegexOptions options) {
        return like(column, value+"$", options);
    }

    /**
     * 右包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(boolean condition , String column, Object value) {
        return condition ? likeRight(column,value) : typeThis();
    }

    /**
     * 右包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(String column, Object value) {
        return likeRight(column,value, RegexOptions.CASE_INSENSITIVE);
    }

    /**
     * 右包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(boolean condition , String column, Object value, RegexOptions options) {
        return condition ? likeRight(column,value,options) : typeThis();
    }

    /**
     * 右包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    default Children likeRight(String column, Object value, RegexOptions options) {
        return like(column, value+"$", options);
    }

}
