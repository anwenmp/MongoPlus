package com.anwen.mongo.conditions.interfaces;

import com.anwen.mongo.enums.CurrentDateType;
import com.anwen.mongo.enums.PopType;
import com.anwen.mongo.support.SFunction;

import java.io.Serializable;
import java.util.List;

public interface Update<T , Children> extends Serializable {

    /**
     * 设置值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:46
     */
    Children set(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 设置值
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:46
     */
    Children set(SFunction<T,Object> column, Object value);

    /**
     * 设置值
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:46
     */
    Children set(boolean condition, String column, Object value);

    /**
     * 设置值
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:46
     */
    Children set(String column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:48
     */
    Children setOnInsert(boolean condition, SFunction<T,Object> column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:48
     */
    Children setOnInsert(SFunction<T,Object> column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:48
     */
    Children setOnInsert(boolean condition, String column, Object value);

    /**
     * 将指定值分配给文档中的字段
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:48
     */
    Children setOnInsert(String column, Object value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(boolean condition,SFunction<T,Object> column,Object value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(SFunction<T,Object> column,Object value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(boolean condition,String column,Object value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(String column,Object value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(boolean condition,SFunction<T,Object> column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(SFunction<T,Object> column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(boolean condition,String column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(String column,Object ... value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(boolean condition, SFunction<T,Object> column, List<?> value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(SFunction<T,Object> column, List<?> value);

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(boolean condition, String column, List<?> value);

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:49
     */
    Children push(String column, List<?> value);

    /**
     * 对指定值原子性的递增
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:51
     */
    Children inc(boolean condition,SFunction<T,Object> column,Number value);

    /**
     * 对指定值原子性的递增
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:51
     */
    Children inc(SFunction<T,Object> column,Number value);

    /**
     * 对指定值原子性的递增
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:51
     */
    Children inc(boolean condition,String column,Number value);

    /**
     * 对指定值原子性的递增
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:51
     */
    Children inc(String column,Number value);

    /**
     * 将字段的值设置为当前日期
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:52
     */
    Children currentDate(boolean condition,SFunction<T,Object> column);

    /**
     * 将字段的值设置为当前日期
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:52
     */
    Children currentDate(SFunction<T,Object> column);

    /**
     * 将字段的值设置为当前日期
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:52
     */
    Children currentDate(boolean condition,String column);

    /**
     * 将字段的值设置为当前日期
     * @param column 列名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:52
     */
    Children currentDate(String column);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:58
     */
    Children currentDate(boolean condition,SFunction<T,Object> column, CurrentDateType currentDateType);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:58
     */
    Children currentDate(SFunction<T,Object> column, CurrentDateType currentDateType);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:58
     */
    Children currentDate(boolean condition,String column, CurrentDateType currentDateType);

    /**
     * 将字段的值设置为当前日期或当前时间戳
     * @param column colum
     * @param currentDateType currentDate类型
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午10:58
     */
    Children currentDate(String column, CurrentDateType currentDateType);

    /**
     * 指定字段值小于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children min(boolean condition,SFunction<T,Object> column, Object value);

    /**
     * 指定字段值小于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children min(SFunction<T,Object> column, Object value);

    /**
     * 指定字段值小于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children min(boolean condition,String column, Object value);

    /**
     * 指定字段值小于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children min(String column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children max(boolean condition,SFunction<T,Object> column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children max(SFunction<T,Object> column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children max(boolean condition,String column, Object value);

    /**
     * 指定字段值大于输入值则更新
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 上午11:27
     */
    Children max(String column, Object value);

    /**
     * 将指定字段的值乘以value
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午12:13
     */
    Children mul(boolean condition,SFunction<T,Object> column,Number value);

    /**
     * 将指定字段的值乘以value
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午12:13
     */
    Children mul(SFunction<T,Object> column,Number value);

    /**
     * 将指定字段的值乘以value
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午12:13
     */
    Children mul(boolean condition,String column,Number value);

    /**
     * 将指定字段的值乘以value
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午12:13
     */
    Children mul(String column,Number value);

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:40
     */
    Children rename(boolean condition, String oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:40
     */
    Children rename(String oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:40
     */
    <O> Children rename(boolean condition, SFunction<O,Object> oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:40
     */
    <O> Children rename(SFunction<O,Object> oldFieldName,String newFieldName);

    /**
     * 更新字段名称
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:40
     */
    <O,N> Children rename(boolean condition, SFunction<O,Object> oldFieldName,SFunction<N,Object> newFieldName);

    /**
     * 更新字段名称
     * @param oldFieldName 旧字段名
     * @param newFieldName 新字段名
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:40
     */
    <O,N> Children rename(SFunction<O,Object> oldFieldName,SFunction<N,Object> newFieldName);


    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:55
     */
    @SuppressWarnings("unchecked")
    Children unset(SFunction<T,Object>... columns);

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:55
     */
    @SuppressWarnings("unchecked")
    Children unset(boolean condition,SFunction<T,Object>... columns);

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:55
     */
    Children unset(String... columns);

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:55
     */
    Children unset(boolean condition,String... columns);

    /**
     * 删除特定字段
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:55
     */
    Children unset(List<String> columns);

    /**
     * 删除特定字段
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param columns 字段
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午1:55
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
     * @date 2024/8/2 下午3:13
     */
    Children addToSet(boolean condition,SFunction<T,Object> column,Object value,boolean each);

    /**
     * 将值添加到数组中
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午3:13
     */
    Children addToSet(SFunction<T,Object> column,Object value,boolean each);

    /**
     * 将值添加到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午3:13
     */
    Children addToSet(boolean condition,String column,Object value,boolean each);

    /**
     * 将值添加到数组中
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午3:13
     */
    Children addToSet(String column,Object value,boolean each);

    /**
     * 删除数组中第一个或最后一个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午4:25
     */
    Children pop(boolean condition,SFunction<T,Object> column, PopType popType);

    /**
     * 删除数组中第一个或最后一个元素
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午4:25
     */
    Children pop(SFunction<T,Object> column, PopType popType);

    /**
     * 删除数组中第一个或最后一个元素
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午4:25
     */
    Children pop(boolean condition,String column, PopType popType);

    /**
     * 删除数组中第一个或最后一个元素
     * @param column 列名
     * @param popType pop类型枚举
     * @return {@link Children}
     * @author anwen
     * @date 2024/8/2 下午4:25
     */
    Children pop(String column, PopType popType);

}
