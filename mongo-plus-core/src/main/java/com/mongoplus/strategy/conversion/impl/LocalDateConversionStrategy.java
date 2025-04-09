package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.InstantUtil;

import java.time.LocalDate;
import java.util.Date;

/**
 * LocalDate类型转换策略
 * @author JiaChaoYang
 **/
public class LocalDateConversionStrategy implements ConversionStrategy<LocalDate> {

    @Override
    public LocalDate convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return fieldValue.getClass().equals(Long.class) ? InstantUtil.convertTimestampToLocalDate((Long) fieldValue) : InstantUtil.convertTimestampToLocalDate(((Date) fieldValue).toInstant());
    }
}
