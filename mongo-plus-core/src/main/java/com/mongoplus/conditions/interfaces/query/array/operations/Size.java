package com.mongoplus.conditions.interfaces.query.array.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * size操作
 *
 * @author anwen
 * @mongodbOperator $size
 */
public interface Size<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配所有字段值为指定大小的数组的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param size 数组的大小
     * @return {@link Children}
     * @author anwen
     */
    default Children size(boolean condition, SFunction<T,?> fieldName, int size) {
        return condition ? size(fieldName, size) : typeThis();
    }

    /**
     * 匹配所有字段值为指定大小的数组的文档
     * @param fieldName 字段名
     * @param size 数组的大小
     * @return {@link Children}
     * @author anwen
     */
    default Children size(SFunction<T,?> fieldName, int size) {
        return addCondition(getBaseCondition(fieldName,size));
    }

    /**
     * 匹配所有字段值为指定大小的数组的文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param size 数组的大小
     * @return {@link Children}
     * @author anwen
     */
    default Children size(boolean condition, String fieldName, int size) {
        return condition ? size(fieldName, size) : typeThis();
    }

    /**
     * 匹配所有字段值为指定大小的数组的文档
     * @param fieldName 字段名
     * @param size 数组的大小
     * @return {@link Children}
     * @author anwen
     */
    default Children size(String fieldName, int size) {
        return addCondition(getBaseCondition(fieldName,size));
    }

}
