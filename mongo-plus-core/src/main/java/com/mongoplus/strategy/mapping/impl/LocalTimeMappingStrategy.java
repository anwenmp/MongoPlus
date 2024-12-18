package com.mongoplus.strategy.mapping.impl;

import com.mongoplus.strategy.mapping.MappingStrategy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * LocalTime映射，将{@link LocalTime}映射为{@link Date}
 * @author anwen
 */
public class LocalTimeMappingStrategy implements MappingStrategy<LocalTime> {

    @Override
    public Object mapping(LocalTime fieldValue) throws IllegalAccessException {
        return Date.from(fieldValue.atDate(LocalDate.ofEpochDay(0L))
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

}
