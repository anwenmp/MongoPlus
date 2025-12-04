package com.mongoplus.conditions.interfaces.update.array.operations;

import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ClassTypeUtil;

import java.util.Collection;
import java.util.List;

/**
 * addToSet操作
 *
 * @author anwen
 */
public interface AddToSet<T, Children> extends BaseUpdateCondition<T, Children> {

    /**
     * 将值添加到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(boolean condition, SFunction<T,Object> column, Object value, boolean each) {
        return condition ? addToSet(column, value, each) : typeThis();
    }

    /**
     * 将值添加到数组中
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(SFunction<T,Object> column,Object value,boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return addUpdateCondition(getBaseUpdateCompare(column,value,each));
    }

    /**
     * 将值添加到数组中,默认使用each
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(boolean condition, SFunction<T,Object> column, List<?> value) {
        return condition ? addToSet(column, value) : typeThis();
    }

    /**
     * 将值添加到数组中，默认使用each
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(SFunction<T,Object> column,List<?> value) {
        return addToSet(column,value,true);
    }

    /**
     * 将值添加到数组中
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(boolean condition,String column,Object value,boolean each) {
        return condition ? addToSet(column, value, each) : typeThis();
    }

    /**
     * 将值添加到数组中
     * @param column 字段
     * @param value 值
     * @param each 通过$each的方式插入
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(String column,Object value,boolean each) {
        if (each && !ClassTypeUtil.isTargetClass(Collection.class,value.getClass())){
            throw new MongoPlusException("$each requires data of Collection type");
        }
        return addUpdateCondition(getBaseUpdateCompare(column,value,each));
    }

    /**
     * 将值添加到数组中,默认使用each
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(boolean condition,String column,List<?> value) {
        return condition ? addToSet(column, value) : typeThis();
    }

    /**
     * 将值添加到数组中,默认使用each
     * @param column 字段
     * @param value 值
     * @return {@link Children}
     * @author anwen
     */
    default Children addToSet(String column,List<?> value) {
        return addToSet(column,value,true);
    }

}
