package com.anwen.mongo.strategy.conversion.impl.bson;

import com.anwen.mongo.mapping.MongoConverter;
import com.anwen.mongo.strategy.conversion.ConversionStrategy;
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
