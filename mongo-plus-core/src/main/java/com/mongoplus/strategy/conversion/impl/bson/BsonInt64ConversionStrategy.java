package com.mongoplus.strategy.conversion.impl.bson;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonInt64;

/**
 * BsonInt64转换
 *
 * @author anwen
 */
public class BsonInt64ConversionStrategy implements ConversionStrategy<BsonInt64> {
    @Override
    public BsonInt64 convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new BsonInt64(Long.parseLong(fieldValue.toString()));
    }
}
