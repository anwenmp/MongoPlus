package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.InstantUtil;

import java.time.LocalTime;
import java.util.Date;

/**
 * LocalTime转换策略实现类
 * @author JiaChaoYang
 **/
public class LocalTimeConversionStrategy implements ConversionStrategy<LocalTime> {

    @Override
    public LocalTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return fieldValue.getClass().equals(Long.class) ? InstantUtil.convertTimestampToLocalTime((Long) fieldValue) : InstantUtil.convertTimestampToLocalTime(((Date) fieldValue).toInstant());
    }
}
