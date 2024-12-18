package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;

/**
 * String类型转换策略
 *
 * @author JiaChaoYang
 **/
public class StringConversionStrategy implements ConversionStrategy<String> {

    Log log = LogFactory.getLog(StringConversionStrategy.class);

    @Override
    public String convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        try {
            if (fieldValue == null) {
                return null;
            }
            if (fieldValue instanceof String) {
                return (String) fieldValue;
            }
            return String.valueOf(fieldValue);
        } catch (Exception e) {
            log.warn("Conversion to String failed, exception message: {}",e.getMessage());
        }
        return null;
    }
}
