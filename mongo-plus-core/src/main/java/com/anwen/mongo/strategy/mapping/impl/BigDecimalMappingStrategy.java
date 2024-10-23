package com.anwen.mongo.strategy.mapping.impl;

import com.anwen.mongo.strategy.mapping.MappingStrategy;
import org.bson.types.Decimal128;

import java.math.BigDecimal;

/**
 * bigDecimal映射器
 * @author anwen
 * @date 2024/10/21 16:45
 */
public class BigDecimalMappingStrategy implements MappingStrategy<BigDecimal> {
    @Override
    public Object mapping(BigDecimal fieldValue) throws IllegalAccessException {
        return new Decimal128(fieldValue);
    }
}
