package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

/**
 * Double类型转换策略
 *
 * @author JiaChaoYang
 **/
public class DoubleConversionStrategy implements ConversionStrategy<Double> {

    Log log = LogFactory.getLog(DoubleConversionStrategy.class);

    @Override
    public Double convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Double value = null;
        try {
            if (fieldValue instanceof Double){
                value = (Double) fieldValue;
            }else {
                value = Double.parseDouble(StringUtils.isNotBlankAndConvert(fieldValue));
            }
        } catch (NumberFormatException e) {
            log.warn("Conversion to Double failed, exception message: {}",e.getMessage());
        }
        return value;
    }
}
