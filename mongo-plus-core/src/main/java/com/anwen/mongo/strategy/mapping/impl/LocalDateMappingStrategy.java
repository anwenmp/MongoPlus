package com.anwen.mongo.strategy.mapping.impl;

import com.anwen.mongo.strategy.mapping.MappingStrategy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalDate映射，将{@link LocalDate}映射为{@link Date}
 * @author anwen
 */
public class LocalDateMappingStrategy implements MappingStrategy<LocalDate> {

    @Override
    public Object mapping(LocalDate fieldValue) throws IllegalAccessException {
        return Date.from(fieldValue.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
