package com.mongoplus.strategy.conversion.impl;

import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.SimpleFieldInformation;
import com.mongoplus.strategy.conversion.ConversionStrategy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举的转换策略
 *
 * @author anwen
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class EnumConversionStrategy<T> implements ConversionStrategy<Enum> {

    /**
     * 缓存每个枚举类的值到枚举实例的映射关系
     *
     */
    private final Map<Class<? extends Enum>, Map<Object, Enum>> enumValueCache = new ConcurrentHashMap<>();

    @Override
    public Enum convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter) throws IllegalAccessException {
        Class<? extends Enum> enumType = (Class<? extends Enum>) fieldType;

        // 初始化缓存并获取值到枚举实例的映射
        Map<Object, Enum> valueToEnumMap = enumValueCache.computeIfAbsent(enumType, this::initializeEnumCache);

        if (valueToEnumMap != null) {
            if (valueToEnumMap.containsKey(fieldValue)){
                return valueToEnumMap.get(fieldValue);
            }
        }
        // 如果没有使用注解，或未通过注解标注的字段值找到对应的枚举常量，则先尝试使用valueOf方式，如果失败则抛出异常
        try {
            return Enum.valueOf(enumType, (String) fieldValue);
        } catch (Exception e){
            throw new IllegalArgumentException("No matching enum constant found for value: " + fieldValue);
        }
    }

    /**
     * 初始化枚举类的值到枚举实例的映射关系
     */
    private Map<Object, Enum> initializeEnumCache(Class<? extends Enum> enumType) {
        // 获取标注了 @EnumValue 的字段
        Field[] fields = enumType.getDeclaredFields();
        FieldInformation fieldInformation = null;
        for (Field field : fields) {
            EnumValue enumValue = field.getAnnotation(EnumValue.class);
            if (enumValue != null){
                fieldInformation = new SimpleFieldInformation<>(enumValue, field);
                break;
            }
        }

        if (fieldInformation == null) {
            return null;
        }
        Map<Object, Enum> valueToEnumMap = new ConcurrentHashMap<>();
        for (Enum enumConstant : enumType.getEnumConstants()) {
            Object value = fieldInformation.getValue(enumConstant);
            if (value != null) {
                valueToEnumMap.put(value, enumConstant);
            }
        }
        return valueToEnumMap;
    }
}
