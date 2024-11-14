package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

import java.math.BigInteger;

/**
 * BigInteger类型转换器策略实现类
 *
 * @author JiaChaoYang
 **/
public class BigIntegerConversionStrategy implements ConversionStrategy<BigInteger> {

    private final Log log = LogFactory.getLog(BigIntegerConversionStrategy.class);

    @Override
    public BigInteger convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        BigInteger value = null;
        try {
            value = new BigInteger(StringUtils.isNotBlankAndConvert(fieldValue));
        }catch (Exception e){
            log.warn("Convert fieldValue To BigDecimal Fail,Exception Message: {}",e.getMessage(),e);
        }
        return value;
    }
}
