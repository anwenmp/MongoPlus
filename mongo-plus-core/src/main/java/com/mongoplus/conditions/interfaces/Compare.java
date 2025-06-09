package com.mongoplus.conditions.interfaces;

import com.mongodb.BasicDBObject;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.enums.TypeEnum;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static com.mongoplus.handlers.condition.BuildCondition.condition;

/**
 * 查询条件封装
 * @author JiaChaoYang
*/
@SuppressWarnings("unused")
public interface Compare<T,Children> extends Serializable {

    /**
     * 构建条件
     * @author anwen
     */
    default BaseConditionResult buildCondition(){
        return buildCondition(condition());
    }

    /**
     * 构建条件
     * @author anwen
     */
    BaseConditionResult buildCondition(Condition condition);

    /**
     * 判断是否为空
     */
    default boolean isNotEmpty(){
        return isNotEmpty(condition());
    }

    /**
     * 判断是否为空
     */
    boolean isNotEmpty(Condition condition);

    /**
     * 等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children eq(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children eq(SFunction<T,Object> column, Object value);

    /**
     * 等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children eq(boolean condition, String column, Object value);

    /**
     * 等于
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children eq(String column, Object value);

    /**
     * 不等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children ne(boolean condition , SFunction<T,Object> column, Object value);

    /**
     * 不等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children ne(SFunction<T,Object> column, Object value);

    /**
     * 不等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children ne(boolean condition , String column, Object value);

    /**
     * 等于
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children ne(String column, Object value);

    /**
     * 小于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lt(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 小于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lt(SFunction<T,Object> column, Object value);

    /**
     * 等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lt(boolean condition, String column, Object value);

    /**
     * 等于
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lt(String column, Object value);

    /**
     * 小于等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lte(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 小于等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lte(SFunction<T,Object> column, Object value);

    /**
     * 小于等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children lte(boolean condition, String column, Object value);

    /**
     * 小于等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("UnusedReturnValue")
    Children lte(String column, Object value);

    /**
     * 大于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gt(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 大于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gt(SFunction<T,Object> column, Object value);

    /**
     * 大于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gt(boolean condition, String column, Object value);

    /**
     * 大于
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gt(String column, Object value);
    /**
     * 大于等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gte(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 大于等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gte(SFunction<T,Object> column, Object value);

    /**
     * 大于等于
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gte(boolean condition, String column, Object value);

    /**
     * 大于等于
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children gte(String column, Object value);

    /**
     * 包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children like(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children like(SFunction<T,Object> column, Object value);

    /**
     * 包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children like(boolean condition, String column, Object value);

    /**
     * 包含（模糊查询）
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children like(String column, Object value);

    /**
     * 左包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeLeft(boolean condition , SFunction<T,Object> column, Object value);

    /**
     * 左包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeLeft(SFunction<T,Object> column, Object value);

    /**
     * 左包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeLeft(boolean condition , String column, Object value);

    /**
     * 左包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeLeft(String column, Object value);

    /**
     * 右包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeRight(boolean condition , SFunction<T,Object> column, Object value);

    /**
     * 右包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeRight(SFunction<T,Object> column, Object value);

    /**
     * 右包含（模糊查询）
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeRight(boolean condition , String column, Object value);

    /**
     * 右包含（模糊查询）
     * @param column 列名、字段名，lambda方式
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children likeRight(String column, Object value);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(boolean condition, SFunction<T,Object> column, Collection<?> valueList);

    /**
     * 多值查询
     * @param column 列名、字段名，lambda方式
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(SFunction<T,Object> column, Collection<?> valueList);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名，lambda方式
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(boolean condition, SFunction<T,Object> column, TItem... values);

    /**
     * 多值查询
     *
     * @param column 列名、字段名，lambda方式
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(SFunction<T,Object> column, TItem... values);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(boolean condition, String column, Collection<?> valueList);

    /**
     * 多值查询
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    Children in(String column, Collection<?> valueList);

    /**
     * 多值查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(boolean condition,String column,TItem... values);

    /**
     * 多值查询
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children in(String column,TItem... values);

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
    */
    Children nin(boolean condition , SFunction<T,Object> column , Collection<?> valueList);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
    */
    Children nin(SFunction<T,Object> column , Collection<?> valueList);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(boolean condition , SFunction<T,Object> column , TItem... values);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(SFunction<T,Object> column , TItem... values);

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
    */
    Children nin(boolean condition , String column , Collection<?> valueList);

    /**
     * 不包含
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(boolean condition , String column , TItem... values);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param values 值的集合
     * @return Children
     * @author JiaChaoYang
     */
    @SuppressWarnings("unchecked")
    <TItem> Children nin(String column , TItem... values);

    /**
     * 不包含
     * @param column 列名、字段名
     * @param valueList 值的集合
     * @return Children
     * @author JiaChaoYang
    */
    Children nin(String column , Collection<?> valueList);

    /**
     * and
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children and(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children and(boolean condition,QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * and
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children and(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);


    /**
     * and
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children and(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 或者
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children or(boolean condition , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 或者
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children or(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 或者
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children or(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);


    /**
     * 或者
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children or(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children nor(boolean condition , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 查询的文档必须不符合所有条件
     * @param queryChainWrapper 链式查询
     * @return Children
     * @author JiaChaoYang
     */
    Children nor(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 查询的文档必须不符合所有条件
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children nor(boolean condition, SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);


    /**
     * 查询的文档必须不符合所有条件
     * @param function 链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children nor(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 枚举值
     * @return Children
     * @author JiaChaoYang
    */
    Children type(SFunction<T,Object> column, TypeEnum value);

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 枚举值
     * @return Children
     * @author JiaChaoYang
     */
    Children type(String column, TypeEnum value);

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    Children type(SFunction<T,Object> column, String value);

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    Children type(String column, String value);

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    Children type(SFunction<T,Object> column, Integer value);

    /**
     * 指定查询的字段类型
     * @param column 列名、字段名
     * @param value 类型，参考{@link TypeEnum}的枚举
     * @return Children
     * @author JiaChaoYang
     */
    Children type(String column, Integer value);

    /**
     * 字段是否存在
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children exists(boolean condition,SFunction<T,Object> column,Boolean value);

    /**
     * 字段是否存在
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children exists(SFunction<T,Object> column,Boolean value);

    /**
     * 字段是否存在
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children exists(boolean condition,String column,Boolean value);

    /**
     * 字段是否存在
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children exists(String column,Boolean value);

    Children not(CompareCondition compareCondition);

    Children not(boolean condition,CompareCondition compareCondition);

    Children not(boolean condition,QueryChainWrapper<?,?> queryChainWrapper);

    Children not(QueryChainWrapper<?,?> queryChainWrapper);

    Children not(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    Children not(boolean condition,SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 进行计算的表达式
     * @author JiaChaoYang
    */
    Children expr(CompareCondition compareCondition);

    /**
     * 进行计算的表达式
     * @author JiaChaoYang
     */
    Children expr(boolean condition,CompareCondition compareCondition);

    /**
     * 进行计算的表达式
     * @author anwen
     */
    Children expr(boolean condition,QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 进行计算的表达式
     * @author anwen
     */
    Children expr(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 进行计算的表达式
     * @author anwen
     */
    Children expr(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 进行计算的表达式
     * @author anwen
     */
    Children expr(boolean condition,SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(boolean condition,SFunction<T,Object> column,long divide,long remain);

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(SFunction<T,Object> column,long divide,long remain);

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(boolean condition,SFunction<T,Object> column,Collection<Long> value);

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(SFunction<T,Object> column,Collection<Long> value);

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(boolean condition,String column , long divide,long remain);

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param divide 模数
     * @param remain 余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(String column , long divide,long remain);

    /**
     * 字段值符合余数
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(boolean condition,String column,Collection<Long> value);

    /**
     * 字段值符合余数
     * @param column 列名、字段名
     * @param value 传入集合，第一个值为模数，第二个值为余数
     * @return Children
     * @author JiaChaoYang
    */
    Children mod(String column,Collection<Long> value);

    /**
     * 匹配数组中的值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
    */
    Children elemMatch(boolean condition,SFunction<T,Object> column , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 匹配数组中的值
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
    */
    Children elemMatch(SFunction<T,Object> column , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 匹配数组中的值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
     */
    Children elemMatch(boolean condition,String column , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 匹配数组中的值
     * @param column 列名、字段名
     * @param queryChainWrapper 查询条件
     * @return Children
     * @author JiaChaoYang
     */
    Children elemMatch(String column , QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children all(boolean condition,SFunction<T,Object> column,Collection<?> value);

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children all(SFunction<T,Object> column,Collection<?> value);

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children all(boolean condition,String column,Collection<?> value);

    /**
     * 匹配数组中的值 必须同时包含指定的多个元素
     * @param column 列名、字段名
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children all(String column,Collection<?> value);

    /**
     * 正则表达式查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
    */
    Children regex(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 正则表达式查询
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
    */
    Children regex(SFunction<T,Object> column,Object value);

    /**
     * 正则表达式查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    Children regex(boolean condition,String column,Object value);

    /**
     * 正则表达式查询
     * @param column 列名、字段名
     * @param value 值（可传入{@link java.util.regex.Pattern}对象）
     * @return Children
     * @author JiaChaoYang
     */
    Children regex(String column,Object value);

    /**
     * 文本查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param value 值
     * @return Children
     * @author JiaChaoYang
    */
    Children text(boolean condition, Object value);

    /**
     * 文本查询
     * @param value 值
     * @return Children
     * @author JiaChaoYang
     */
    Children text(Object value);

    /**
     * 文本查询
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param value 值
     * @param textSearchOptions 搜索选项
     * @return {@link Children}
     * @author anwen
     */
    Children text(boolean condition, Object value,TextSearchOptions textSearchOptions);

    /**
     * 文本查询
     * @param value 值
     * @param textSearchOptions 搜索选项
     * @return {@link Children}
     * @author anwen
     */
    Children text(Object value,TextSearchOptions textSearchOptions);

    /**
     * 在。。。之间
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
    */
    Children between(boolean condition , SFunction<T,Object> column,Object gte,Object lte,boolean convertGtOrLt);

    /**
     * 在。。。之间
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    Children between(SFunction<T,Object> column,Object gte,Object lte,boolean convertGtOrLt);

    /**
     * 在。。。之间
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    Children between(boolean condition,String column,Object gte,Object lte,boolean convertGtOrLt);

    /**
     * 在。。。之间
     * @param column 列名、字段名
     * @param gte 大于等于
     * @param lte 小于等于
     * @param convertGtOrLt 设置为true，则转换为大于-小于，默认为大于等于和小于等于
     * @return Children
     * @author JiaChaoYang
     */
    Children between(String column,Object gte,Object lte,boolean convertGtOrLt);

    /**
     * 匹配给定表达式为 true 的所有文档
     * @param javaScriptExpression JavaScript 表达式
     * @return {@link Children}
     * @author anwen
     */
    Children where(String javaScriptExpression);

    /**
     * 匹配所有字段值为指定大小的数组的文档
     * @param fieldName 字段名
     * @param size 数组的大小
     * @return {@link Children}
     * @author anwen
     */
    Children size(SFunction<T,?> fieldName, int size);

    /**
     * 匹配所有字段值为指定大小的数组的文档
     * @param fieldName 字段名
     * @param size 数组的大小
     * @return {@link Children}
     * @author anwen
     */
    Children size(String fieldName, int size);

    /**
     * 匹配字段中所有位位置均清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAllClear(SFunction<T,?> fieldName, long bitmask);

    /**
     * 匹配字段中所有位位置均清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAllClear(String fieldName, long bitmask);

    /**
     * 匹配所有位位置均在字段中设置的所有文档。
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAllSet(SFunction<T,?> fieldName, long bitmask);

    /**
     * 匹配所有位位置均在字段中设置的所有文档。
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAllSet(String fieldName, long bitmask);

    /**
     * 匹配字段中任何位位置清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAnyClear(SFunction<T,?> fieldName, long bitmask);

    /**
     * 匹配字段中任何位位置清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAnyClear(String fieldName, long bitmask);

    /**
     * 匹配在字段中设置任何位位置的所有文档。
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAnySet(SFunction<T,?> fieldName, long bitmask);

    /**
     * 匹配在字段中设置任何位位置的所有文档。
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    Children bitsAnySet(String fieldName, long bitmask);

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(wrapper -> wrapper.eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(wrapper ->
     *     wrapper.custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 需要合并的链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children combine(boolean condition,SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(wrapper -> wrapper.eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(wrapper ->
     *     wrapper.custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @param function 需要合并的链式查询函数
     * @return {@link Children}
     * @author anwen
     */
    Children combine(SFunction<QueryChainWrapper<T,?>,QueryChainWrapper<T,?>> function);

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(wrapper -> wrapper.eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(wrapper ->
     *     wrapper.custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @param queryChainWrapper 链式查询
     * @return {@link Children}
     * @author anwen
     */
    Children combine(QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 合并
     * <p>combine中的条件，将会存在同一个对象中，常用于or,and等逻辑操作符中</p>
     * <p>如构建or条件：{@code or(wrapper -> wrapper.eq(User::getUserName,"张三").like(User::getUserName,"1"))}</p>
     * <p>该操作将会构建语句为：{@code {or:[{userName:{"eq":"张三"}},{userName:{"like":"1"}}]}}</p>
     * <p>如使用{@link #combine}构建：{@code or(wrapper ->
     *     wrapper.custom(customWrapper ->
     *     customWrapper.eq(User::getUserName,"张三").like(User::getUserName,"1")))}</p>
     * <p>则对应语句为：{@code {or:[{userName:{"eq":"张三","like":"1"}}]}}</p>
     * <p style='color: red'>在使用{@link #combine}方法时，请保证{@code combine}中条件的字段名一致</p>
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param queryChainWrapper 链式查询
     * @return {@link Children}
     * @author anwen
     */
    Children combine(boolean condition,QueryChainWrapper<?,?> queryChainWrapper);

    /**
     * 自定义语句
     * @param basicDBObject bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(BasicDBObject basicDBObject);

    /**
     * 自定义语句
     * @param bson bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(Bson bson);

    /**
     * 自定义语句
     * @param mongoPlusBasicDBObject bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(MongoPlusBasicDBObject mongoPlusBasicDBObject);

    /**
     * 自定义语句
     * @param function bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(SFunction<MongoPlusBasicDBObject,MongoPlusBasicDBObject> function);

    /**
     * 自定义语句
     * @param basicDBObjectList bson对象
     * @return {@link Children}
     * @author anwen
     */
    Children custom(List<BasicDBObject> basicDBObjectList);
}
