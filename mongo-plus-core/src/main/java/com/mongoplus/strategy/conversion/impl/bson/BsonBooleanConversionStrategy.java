package com.mongoplus.strategy.conversion.impl.bson;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonBoolean;

/**
 * BsonBoolean转换器
 *
 * @author anwen
 */
public class BsonBooleanConversionStrategy implements ConversionStrategy<BsonBoolean> {
    @Override
    public BsonBoolean convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new BsonBoolean(Boolean.parseBoolean(fieldValue.toString()));
    }
}
