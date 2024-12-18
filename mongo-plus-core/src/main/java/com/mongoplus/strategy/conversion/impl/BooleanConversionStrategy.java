package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Boolean类型转换策略
 *
 * @author JiaChaoYang
 **/
public class BooleanConversionStrategy implements ConversionStrategy<Boolean> {

    @Override
    public Boolean convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        String value;
        try {
            value = StringUtils.isNotBlankAndConvert(fieldValue);
        } catch (Exception e) {
            return null;
        }
        if (!Objects.equals(value, "true") && !Objects.equals(value, "false")){
            boolean matches = Pattern.compile("-?[0-9]+\\.?[0-9]*").matcher(value).matches();
            if (matches){
                value = String.valueOf(Integer.parseInt(value) > 0);
            }else {
                throw new IllegalAccessException("Not a Boolean type");
            }
        }
        return Boolean.parseBoolean(value);
    }
}
