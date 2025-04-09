package com.mongoplus.strategy.mapping.impl;

import com.mongoplus.strategy.mapping.MappingStrategy;

import java.math.BigInteger;

/**
 * BigInteger映射器
 * @author anwen
 */
public class BigIntegerMappingStrategy implements MappingStrategy<BigInteger> {
    @Override
    public Object mapping(BigInteger fieldValue) throws IllegalAccessException {
        return fieldValue.longValue();
    }
}
