package com.anwen.mongo.strategy.mapping.impl;

import com.anwen.mongo.annotation.comm.EnumValue;
import com.anwen.mongo.mapping.FieldInformation;
import com.anwen.mongo.strategy.mapping.MappingStrategy;

import static com.anwen.mongo.toolkit.EnumUtil.getFieldInformation;

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
