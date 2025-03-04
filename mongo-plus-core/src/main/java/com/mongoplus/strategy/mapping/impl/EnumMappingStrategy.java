package com.mongoplus.strategy.mapping.impl;

import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.strategy.mapping.MappingStrategy;

import static com.mongoplus.toolkit.EnumUtil.getFieldInformation;

/**
 * 枚举映射策略
 *
 * @author anwen
 */
@SuppressWarnings({"rawtypes"})
public class EnumMappingStrategy implements MappingStrategy<Enum> {

    @Override
    public Object mapping(Enum fieldValue) throws IllegalAccessException {
        FieldInformation fieldInformation = getFieldInformation(fieldValue);
        if (fieldInformation == null) {
            return fieldValue.name();
        }
        EnumValue enumValue = fieldInformation.getAnnotation(EnumValue.class);
        if (enumValue.valueStore()){
            return fieldInformation.getValue(fieldValue);
        }
        return fieldValue.name();
    }
}
