package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

/**
 * Integer转换策略实现
 *
 * @author JiaChaoYang
 **/
public class IntegerConversionStrategy implements ConversionStrategy<Integer> {

    Log log = LogFactory.getLog(IntegerConversionStrategy.class);

    @Override
    public Integer convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Integer value = null;
        try {
            if (fieldValue instanceof Integer){
                value = (Integer) fieldValue;
            }else {
                value = Integer.parseInt(StringUtils.isNotBlankAndConvert(fieldValue));
            }
        } catch (Exception e) {
            log.warn("Conversion to number failed, exception message: {}",e.getMessage());
        }
        return value;
    }
}
