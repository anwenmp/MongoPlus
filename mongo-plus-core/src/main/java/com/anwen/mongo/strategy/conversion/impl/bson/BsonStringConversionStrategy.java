package com.anwen.mongo.strategy.conversion.impl.bson;

import com.anwen.mongo.mapping.MongoConverter;
import com.anwen.mongo.strategy.conversion.ConversionStrategy;
import org.bson.BsonString;

/**
 * BsonString转换
 *
 * @author anwen
 */
public class BsonStringConversionStrategy implements ConversionStrategy<BsonString> {
    @Override
    public BsonString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new BsonString(fieldValue.toString());
    }
}
