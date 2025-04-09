package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.types.Binary;

/**
 * 转换为byte[]
 * @author anwen
 */
public class ByteArrayConversionStrategy implements ConversionStrategy<byte[]> {
    @Override
    public byte[] convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return ((Binary) fieldValue).getData();
    }
}
