package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

/**
 * 无策略格式数据
 *
 * @author JiaChaoYang
 **/
public class DefaultConversionStrategy implements ConversionStrategy<Object> {

    @Override
    public Object convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        if (!SimpleCache.getSimpleTypeHolder().isSimpleType(fieldType) && fieldValue.getClass().equals(Document.class)){
            return mongoConverter.readInternal((Document) fieldValue,fieldType,false);
        }

        return fieldValue;
    }
}
