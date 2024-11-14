package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

import java.time.Instant;

/**
 * Instant类型转换器策略实现类
 *
 * @author JiaChaoYang
 **/
public class InstantConversionStrategy implements ConversionStrategy<Instant> {

    Log log = LogFactory.getLog(InstantConversionStrategy.class);

    @Override
    public Instant convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Instant value = null;
        try {
            value = Instant.ofEpochMilli(Long.parseLong(StringUtils.isNotBlankAndConvert(fieldValue)));
        } catch (Exception e){
            log.warn("Conversion to timestamp failed, exception message: {}",e.getMessage());
        }
        return value;
    }
}
