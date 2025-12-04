package com.mongoplus.conditions.interfaces.query.bitwise.operations;

import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;

/**
 * bitsAnyClear操作
 *
 * @author anwen
 */
public interface BitsAnyClear<T, Children> extends BaseQueryCondition<T, Children> {

    /**
     * 匹配字段中任何位位置清晰的所有文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAnyClear(boolean condition, SFunction<T,?> fieldName, long bitmask) {
        return condition ? bitsAnyClear(fieldName, bitmask) : typeThis();
    }

    /**
     * 匹配字段中任何位位置清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAnyClear(SFunction<T,?> fieldName, long bitmask) {
        return addCondition(getBaseCondition(fieldName,bitmask));
    }

    /**
     * 匹配字段中任何位位置清晰的所有文档
     * @param condition 判断如果为true，则加入此条件，可做判空，即不为空就加入这个条件
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAnyClear(boolean condition,String fieldName, long bitmask) {
        return condition ? bitsAnyClear(fieldName, bitmask) : typeThis();
    }


    /**
     * 匹配字段中任何位位置清晰的所有文档
     * @param fieldName 字段名
     * @param bitmask 位掩码
     * @return {@link Children}
     * @author anwen
     */
    default Children bitsAnyClear(String fieldName, long bitmask) {
        return addCondition(getBaseCondition(fieldName,bitmask));
    }

}
