package com.mongoplus.strategy.conversion.impl.bson;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonDouble;

/**
 * BsonDouble转换
 *
 * @author anwen
 */
public class BsonDoubleConversionStrategy implements ConversionStrategy<BsonDouble> {
    @Override
    public BsonDouble convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new BsonDouble(Double.parseDouble(fieldValue.toString()));
    }
}
