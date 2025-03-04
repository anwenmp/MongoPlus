package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

import java.math.BigDecimal;

/**
 * BigDecimal类型转换器策略实现类
 *
 * @author JiaChaoYang
 **/
public class BigDecimalConversionStrategy implements ConversionStrategy<BigDecimal> {

    private final Log log = LogFactory.getLog(BigDecimalConversionStrategy.class);

    @Override
    public BigDecimal convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        BigDecimal value = null;
        try {
            value = new BigDecimal(StringUtils.isNotBlankAndConvert(fieldValue));
        } catch (Exception e){
            log.warn("Convert fieldValue To BigDecimal Fail,Exception Message: {}",e.getMessage(),e);
        }
        return value;
    }
}
