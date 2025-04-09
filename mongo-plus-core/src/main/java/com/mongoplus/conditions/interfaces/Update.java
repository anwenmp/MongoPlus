package com.mongoplus.conditions.interfaces;

import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.enums.CurrentDateType;
import com.mongoplus.enums.PopType;
import com.mongoplus.model.MutablePair;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface Update<T , Children> extends Serializable {

    /**
     * 将字段值设置为null
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children setNull(boolean condition, SFunction<T,Object> column);

    /**
     * 将字段值设置为null
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children setNull(SFunction<T,Object> column);

    /**
     * 将字段值设置为null
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children setNull(boolean condition, String column);

    /**
     * 将字段值设置为null
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children setNull(String column);

    /**
     * 设置值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children set(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 设置值
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children set(SFunction<T,Object> column, Object value);

    /**
     * 设置值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children set(boolean condition, String column, Object value);

    /**
     * 设置值
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children set(String column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children setOnInsert(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children setOnInsert(SFunction<T,Object> column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children setOnInsert(boolean condition, String column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children setOnInsert(String column, Object value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,SFunction<T,Object> column,Object value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column,Object value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,SFunction<T,Object> column,Object value,boolean each);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column,Object value,boolean each);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,String column,Object value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column,Object value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,String column,Object value,boolean each);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column,Object value,boolean each);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,SFunction<T,Object> column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,SFunction<T,Object> column,PushOptions options,Object ... value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,SFunction<T,Object> column,PushOptions options,Collection<?> value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column,PushOptions options,Object ... value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column,PushOptions options,Collection<?> value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,String column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,String column,PushOptions options,Object ... value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column,PushOptions options,Object ... value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition,String column,PushOptions options,Collection<?> value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column,PushOptions options,Collection<?> value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition, SFunction<T,Object> column, Collection<?> value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column, Collection<?> value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition, SFunction<T,Object> column, List<?> value,PushOptions options);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(SFunction<T,Object> column, List<?> value,PushOptions options);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition, String column, List<?> value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column, List<?> value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(boolean condition, String column, List<?> value,PushOptions options);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children push(String column, List<?> value,PushOptions options);

    /**
     * 对指定值原子性的递增
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children inc(boolean condition,SFunction<T,Object> column,Number value);

    /**
     * 对指定值原子性的递增
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children inc(SFunction<T,Object> column,Number value);

    /**
     * 对指定值原子性的递增
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children inc(boolean condition,String column,Number value);

    /**
     * 对指定值原子性的递增
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children inc(String column,Number value);

    /**
     * 将字段的值设置为当前日期
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(boolean condition,SFunction<T,Object> column);

    /**
     * 将字段的值设置为当前日期
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(SFunction<T,Object> column);

    /**
     * 将字段的值设置为当前日期
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(boolean condition,String column);

    /**
     * 将字段的值设置为当前日期
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(String column);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(boolean condition,SFunction<T,Object> column, CurrentDateType currentDateType);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(SFunction<T,Object> column, CurrentDateType currentDateType);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(boolean condition,String column, CurrentDateType currentDateType);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     */
    Children currentDate(String column, CurrentDateType currentDateType);

    /**
     * 指定字段值小于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children min(boolean condition,SFunction<T,Object> column, Object value);

    /**
     * 指定字段值小于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children min(SFunction<T,Object> column, Object value);

    /**
     * 指定字段值小于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children min(boolean condition,String column, Object value);

    /**
     * 指定字段值小于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children min(String column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children max(boolean condition,SFunction<T,Object> column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children max(SFunction<T,Object> column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children max(boolean condition,String column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children max(String column, Object value);

    /**
     * 将指定字段的值乘以value
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children mul(boolean condition,SFunction<T,Object> column,Number value);

    /**
     * 将指定字段的值乘以value
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children mul(SFunction<T,Object> column,Number value);

    /**
     * 将指定字段的值乘以value
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children mul(boolean condition,String column,Number value);

    /**
     * 将指定字段的值乘以value
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children mul(String column,Number value);

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    Children rename(boolean condition, String oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    Children rename(String oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    <O> Children rename(boolean condition, SFunction<O,Object> oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    <O> Children rename(SFunction<O,Object> oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    <O,N> Children rename(boolean condition, SFunction<O,Object> oldFieldName,SFunction<N,Object> newFieldName);

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     */
    <O,N> Children rename(SFunction<O,Object> oldFieldName,SFunction<N,Object> newFieldName);

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    Children unset(SFunction<T,Object>... columns);

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    Children unset(boolean condition,SFunction<T,Object>... columns);

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    Children unset(String... columns);

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    Children unset(boolean condition,String... columns);

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    Children unset(List<String> columns);

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     */
    Children unset(boolean condition,List<String> columns);

    /**
     * 将值添加到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(boolean condition,SFunction<T,Object> column,Object value,boolean each);

    /**
     * 将值添加到数组中
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(SFunction<T,Object> column,Object value,boolean each);

    /**
     * 将值添加到数组中,默认使用each
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(boolean condition,SFunction<T,Object> column,List<?> value);

    /**
     * 将值添加到数组中，默认使用each
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(SFunction<T,Object> column,List<?> value);

    /**
     * 将值添加到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(boolean condition,String column,Object value,boolean each);

    /**
     * 将值添加到数组中
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(String column,Object value,boolean each);

    /**
     * 将值添加到数组中,默认使用each
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(boolean condition,String column,List<?> value);

    /**
     * 将值添加到数组中,默认使用each
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children addToSet(String column,List<?> value);

    /**
     * 删除数组中第一个或最后一个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    Children pop(boolean condition,SFunction<T,Object> column, PopType popType);

    /**
     * 删除数组中第一个或最后一个元素
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    Children pop(SFunction<T,Object> column, PopType popType);

    /**
     * 删除数组中第一个或最后一个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    Children pop(boolean condition,String column, PopType popType);

    /**
     * 删除数组中第一个或最后一个元素
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     */
    Children pop(String column, PopType popType);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children pull(boolean condition,SFunction<T,Object> column,Object value);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children pull(SFunction<T,Object> column,Object value);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param wrapper 值 条件
     * @return {@link Children}
     * @author anwen
     */
    Children pull(boolean condition,QueryChainWrapper<?,?> wrapper);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param function 值 条件
     * @return {@link Children}
     * @author anwen
     */
    Children pull(boolean condition,SFunction<QueryChainWrapper<?,?>,QueryChainWrapper<?,?>> function);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param wrapper 值 条件
     * @return {@link Children}
     * @author anwen
     */
    Children pull(QueryChainWrapper<?,?> wrapper);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param function 值 条件
     * @return {@link Children}
     * @author anwen
     */
    Children pull(SFunction<QueryChainWrapper<?,?>,QueryChainWrapper<?,?>> function);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children pull(boolean condition,String column,Object value);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    Children pull(String column,Object value);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(boolean condition,SFunction<T,Object> column, Collection<?> values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(SFunction<T,Object> column, Collection<?> values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(boolean condition,SFunction<T,Object> column, Object... values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(SFunction<T,Object> column, Object... values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(boolean condition,String column, Collection<?> values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(String column, Collection<?> values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(boolean condition,String column, Object... values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * @param column 字段
     * @param values 值
     * @return {@link Children}
     * @author anwen
     */
    Children pullAll(String column, Object... values);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * <p>示例：{@code pullAll(MutablePair.of(User::getId, Arrays.asList(1,2,3)))}</p>
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param pullAllPair 条件
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    Children pullAll(boolean condition,MutablePair<String, Collection<?>>... pullAllPair);

    /**
     * 删除数组中符合条件或符合指定值的实例
     * <p>示例：{@code pullAll(MutablePair.of(User::getId, Arrays.asList(1,2,3)))}</p>
     * @param pullAllPair 条件
     * @return {@link Children}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    Children pullAll(MutablePair<String, Collection<?>>... pullAllPair);

    /**
     * 自定义修改
     * @param bson bson
     * @return {@link Children}
     * @author anwen
     */
    Children updateCustom(Bson bson);

}
