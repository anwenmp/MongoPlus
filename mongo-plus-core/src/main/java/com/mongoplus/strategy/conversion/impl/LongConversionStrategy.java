package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

/**
 * Long类型转换策略实现
 *
 * @author JiaChaoYang
 **/
public class LongConversionStrategy implements ConversionStrategy<Long> {

    Log log = LogFactory.getLog(LongConversionStrategy.class);

    @Override
    public Long convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Long value = null;
        try {
            if (fieldValue instanceof Long){
                value = (Long) fieldValue;
            } else {
                value = Long.parseLong(StringUtils.isNotBlankAndConvert(fieldValue));
            }
        } catch (Exception e) {
            log.warn("Conversion to Long failed, exception message: {}",e.getMessage());
        }
        return value;
    }
}
