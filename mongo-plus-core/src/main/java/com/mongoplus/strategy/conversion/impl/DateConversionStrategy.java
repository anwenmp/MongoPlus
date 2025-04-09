package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;

import java.util.Date;

/**
 * Date类型转换器策略实现
 * @author JiaChaoYang
 **/
public class DateConversionStrategy implements ConversionStrategy<Date> {

    @Override
    public Date convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Date date;
        if (fieldValue.getClass().equals(Long.class)){
            date = new Date((Long) fieldValue);
        }else {
            date = (Date) fieldValue;
        }
        return date;
    }
}
