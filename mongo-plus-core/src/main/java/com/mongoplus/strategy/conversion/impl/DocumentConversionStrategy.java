package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;

/**
 * Document转换策略
 * @author anwen
 * @date 2024/5/20 下午10:17
 */
public class DocumentConversionStrategy implements ConversionStrategy<Document> {
    @Override
    public Document convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return (Document) fieldValue;
    }
}