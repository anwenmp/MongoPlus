package com.mongoplus.conditions.interfaces.update.array.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.options.PushOptions;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ClassTypeUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * push操作
 *
 * @author anwen
 */
public interface Push<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, SFunction<T,Object> column, Object value) {
        return condition ? push(column,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column,Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column, value));
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,SFunction<T,Object> column,Object value,boolean each) {
        return condition ? push(column,value,each) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column,Object value,boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return each ? push(column, Collections.singletonList(value)) : push(column,value);
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,String column,Object value) {
        return condition ? push(column,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column,Object value) {
        return addUpdateCondition(getBaseUpdateCompare(column, value));
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,String column,Object value,boolean each) {
        return condition ? push(column,value,each) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column,Object value,boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return each ? push(column, Collections.singletonList(value)) : push(column,value);
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,SFunction<T,Object> column,Object ... value) {
        return condition ? push(column,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column,Object ... value) {
        return push(column,new PushOptions(),value);
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, SFunction<T,Object> column, PushOptions options, Object ... value) {
        return condition ? push(column, Arrays.stream(value).collect(Collectors.toList()),options) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, SFunction<T,Object> column, PushOptions options, Collection<?> value) {
        return condition ? push(column,options,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column,PushOptions options,Object ... value) {
        return push(column,options,Arrays.stream(value).collect(Collectors.toList()));
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column,PushOptions options,Collection<?> value) {
        return addUpdateCondition(getBaseUpdateCompare(column, value,options));
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,String column,Object ... value) {
        return condition ? push(column,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column,Object ... value) {
        return push(column,new PushOptions(),value);
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,String column,PushOptions options,Object ... value) {
        return condition ? push(column,value,options) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column,PushOptions options,Object ... value) {
        return push(column,options,Arrays.stream(value).collect(Collectors.toList()));
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition,String column,PushOptions options,Collection<?> value) {
        return condition ? push(column,options,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column,PushOptions options,Collection<?> value) {
        return addUpdateCondition(getBaseUpdateCompare(column,value,options));
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, SFunction<T,Object> column, Collection<?> value) {
        return condition ? push(column,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column, Collection<?> value) {
        return push(column,new PushOptions(),value);
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, SFunction<T,Object> column, List<?> value, PushOptions options) {
        return condition ? push(column,value,options) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(SFunction<T,Object> column, List<?> value,PushOptions options) {
        return addUpdateCondition(getBaseUpdateCompare(column,value,options));
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, String column, List<?> value) {
        return condition ? push(column,value) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column, List<?> value) {
        return push(column,value,new PushOptions());
    }

    /**
     * 将指定值push到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(boolean condition, String column, List<?> value,PushOptions options) {
        return condition ? push(column,value,options) : typeThis();
    }

    /**
     * 将指定值push到数组中
     * @param column 列名
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children push(String column, List<?> value,PushOptions options) {
        return addUpdateCondition(getBaseUpdateCompare(column,value,options));
    }

}
