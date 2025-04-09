package com.mongoplus.strategy.mapping.impl;

import com.mongoplus.strategy.mapping.MappingStrategy;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

/**
 * bigDecimal映射器
 * @author anwen
 */
public class BigDecimalMappingStrategy implements MappingStrategy<BigDecimal> {
    @Override
    public Object mapping(BigDecimal fieldValue) throws IllegalAccessException {
        return new Decimal128(fieldValue);
    }
}
