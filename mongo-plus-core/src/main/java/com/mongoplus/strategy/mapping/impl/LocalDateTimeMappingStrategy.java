package com.mongoplus.strategy.mapping.impl;

import com.mongoplus.strategy.mapping.MappingStrategy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalDateTime映射，将{@link LocalDateTime}映射为{@link Date}
 * @author anwen
 */
public class LocalDateTimeMappingStrategy implements MappingStrategy<LocalDateTime> {

    @Override
    public Object mapping(LocalDateTime fieldValue) throws IllegalAccessException {
        return Date.from(fieldValue.atZone(ZoneId.systemDefault()).toInstant());
    }

}
