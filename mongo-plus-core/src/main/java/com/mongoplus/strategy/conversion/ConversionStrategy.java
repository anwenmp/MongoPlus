package com.mongoplus.strategy.conversion;

import com.mongoplus.mapping.MongoConverter;

/**
 * 转换器
 * @author anwen
 */
public interface ConversionStrategy<T> {

    /**
     * 转换方法
     * @param fieldValue 字段值
     * @param fieldType 字段的类型
     * @param mongoConverter 映射器
     * @return {@link T}
     * @author anwen
     */
    T convertValue(Object fieldValue, Class<?> fieldType ,MongoConverter mongoConverter) throws IllegalAccessException;

}
