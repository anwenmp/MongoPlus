package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;

/**
 * Byte转换策略
 * @author anwen
 */
public class ByteConversionStrategy implements ConversionStrategy<Byte> {
    @Override
    public Byte convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return Byte.valueOf(String.valueOf(fieldValue));
    }
}
