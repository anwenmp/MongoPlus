package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

/**
 * Float类型转换策略
 *
 * @author JiaChaoYang
 **/
public class FloatConversionStrategy implements ConversionStrategy<Float> {

    Log log = LogFactory.getLog(FloatConversionStrategy.class);

    @Override
    public Float convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Float value = null;
        try {
            value = Float.parseFloat(StringUtils.isNotBlankAndConvert(fieldValue));
        } catch (Exception e) {
            log.warn("Conversion to Float failed, exception message: {}",e.getMessage());
        }
        return value;
    }
}
