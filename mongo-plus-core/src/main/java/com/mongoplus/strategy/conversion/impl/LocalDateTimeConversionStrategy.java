package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.InstantUtil;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * LocalDateTime类型策略实现类
 * @author JiaChaoYang
 **/
public class LocalDateTimeConversionStrategy implements ConversionStrategy<LocalDateTime> {

    @Override
    public LocalDateTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        return fieldValue.getClass().equals(Long.class) ?
                InstantUtil.convertTimestampToLocalDateTime((Long) fieldValue) :
                InstantUtil.convertTimestampToLocalDateTime8(((Date) fieldValue).toInstant());
    }
}
