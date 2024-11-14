package com.mongoplus.strategy.conversion;

import com.mongoplus.mapping.MongoConverter;

/**
 * 转换器
 * @author anwen
 * @date 2024/5/4 下午12:26
 */
public interface ConversionStrategy<T> {

    T convertValue(Object fieldValue, Class<?> fieldType ,MongoConverter mongoConverter) throws IllegalAccessException;

}
