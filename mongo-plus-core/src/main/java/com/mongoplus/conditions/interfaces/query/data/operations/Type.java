package com.mongoplus.conditions.interfaces.query.data.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.TypeEnum;
import com.mongoplus.support.SFunction;

/**
 * type操作
 *
 * @author anwen
 * @mongodbOperator $type
 */
public interface Type<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 指定查询的字段类型
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 枚举值
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(boolean condition, SFunction<T,Object> column, TypeEnum value) {
        return condition ? type(column, value) : typeThis();
    }

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 枚举值
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(SFunction<T,Object> column, TypeEnum value) {
        return addCondition(getBaseCondition(column, value.getTypeCode()));
    }

    /**
     * 指定查询的字段类型
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 枚举值
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(boolean condition, String column, TypeEnum value) {
        return condition ? type(column, value) : typeThis();
    }

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 枚举值
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(String column, TypeEnum value) {
        return addCondition(getBaseCondition(column, value.getTypeCode()));
    }

    /**
     * 指定查询的字段类型
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(boolean condition, SFunction<T,Object> column, String value) {
        return condition ? type(column, value) : typeThis();
    }

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(SFunction<T,Object> column, String value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 指定查询的字段类型
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(boolean condition, String column, String value){
        return condition ? type(column, value) : typeThis();
    }

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(String column, String value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 指定查询的字段类型
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(boolean condition, SFunction<T,Object> column, Integer value) {
        return condition ? type(column, value) : typeThis();
    }

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(SFunction<T,Object> column, Integer value) {
        return addCondition(getBaseCondition(column, value));
    }

    /**
     * 指定查询的字段类型
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(boolean condition, String column, Integer value) {
        return condition ? type(column, value) : typeThis();
    }

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    default Children type(String column, Integer value) {
        return addCondition(getBaseCondition(column, value));
    }

}
