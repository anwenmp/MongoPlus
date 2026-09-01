package com.mongoplus.conditions.interfaces.query.bitwise.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * bitsAllClear操作
 *
 * @author anwen
 * @mongodbOperator $bitsAllClear
 */
public interface BitsAllClear<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配字段中所有位位置均清晰的所有文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllClear(boolean condition,SFunction<T,?> fieldName, long bitmask) {
        return condition ? bitsAllClear(fieldName, bitmask) : typeThis();
    }

    /**
     * 匹配字段中所有位位置均清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllClear(SFunction<T,?> fieldName, long bitmask) {
        return addCondition(getBaseCondition(fieldName,bitmask));
    }

    /**
     * 匹配字段中所有位位置均清晰的所有文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllClear(boolean condition,String fieldName, long bitmask) {
        return condition ? bitsAllClear(fieldName, bitmask) : typeThis();
    }

    /**
     * 匹配字段中所有位位置均清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAllClear(String fieldName, long bitmask) {
        return addCondition(getBaseCondition(fieldName,bitmask));
    }

}
