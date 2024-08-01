package com.anwen.mongo.strategy.conversion.impl.bson;

import com.anwen.mongo.mapping.MongoConverter;
import com.anwen.mongo.strategy.conversion.ConversionStrategy;
import org.bson.BsonInt32;

/**
 * BsonInt32转换
 *
 * @author anwen
 */
public class BsonInt32ConversionStrategy implements ConversionStrategy<BsonInt32> {
    @Override
    public BsonInt32 convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new BsonInt32(Integer.parseInt(fieldValue.toString()));
    }
}
