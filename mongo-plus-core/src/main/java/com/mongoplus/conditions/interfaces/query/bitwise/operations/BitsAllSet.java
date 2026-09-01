package com.mongoplus.conditions.interfaces.query.bitwise.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * bitsAllSet操作
 *
 * @author anwen
 * @mongodbOperator $bitsAllSet
 */
public interface BitsAllSet<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配所有位位置均在字段中设置的所有文档。
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllSet(boolean condition, SFunction<T,?> fieldName, long bitmask) {
        return condition ? bitsAllSet(fieldName, bitmask) : typeThis();
    }

    /**
     * 匹配所有位位置均在字段中设置的所有文档。
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllSet(SFunction<T,?> fieldName, long bitmask) {
        return addCondition(getBaseCondition(fieldName, bitmask));
    }

    /**
     * 匹配所有位位置均在字段中设置的所有文档。
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllSet(boolean condition,String fieldName, long bitmask) {
        return condition ? bitsAllSet(fieldName, bitmask) : typeThis();
    }

    /**
     * 匹配所有位位置均在字段中设置的所有文档。
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllSet(String fieldName, long bitmask) {
        return addCondition(getBaseCondition(fieldName, bitmask));
    }

}
