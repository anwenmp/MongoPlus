package com.anwen.mongo.strategy.conversion.impl.bson;

import com.anwen.mongo.mapping.MongoConverter;
import com.anwen.mongo.strategy.conversion.ConversionStrategy;
import org.bson.BsonDateTime;

import java.util.Date;

/**
 * BsonDateTime转换
 *
 * @author anwen
 */
public class BsonDateTimeConversionStrategy implements ConversionStrategy<BsonDateTime> {
    @Override
    public BsonDateTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return new BsonDateTime(fieldValue.getClass().equals(Long.class) ? (Long) fieldValue : ((Date)fieldValue).toInstant().toEpochMilli());
    }
}
