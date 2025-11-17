package com.mongoplus.holder;

import com.mongoplus.model.ExtraField;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class ExtraFieldsHolder extends LinkedHashMap<String, ExtraField> {

    /**
     * 添加字段
     * @param key 字段名
     * @param value 字段值
     */
    public void putValue(String key, Object value) {
        this.put(key, new ExtraField(key, value, value != null ? value.getClass() : Object.class));
    }

    /**
     * 获取字段值
     * @param key 字段名
     * @return 字段值
     */
    public Object getValue(String key) {
        ExtraField extraField = this.get(key);
        return extraField != null ? extraField.getValue() : null;
    }

    /**
     * 获取字段值
     * @param key 字段名
     * @param clazz 字段值类型
     * @return 字段值
     */
    public <T> T getValue(String key, Class<T> clazz) {
        Object value = getValue(key);
        return value != null ? clazz.cast(value) : null;
    }

    /**
     * 获取字段值
     * @param key 字段名
     * @param function 字段值转换函数
     * @return 字段值
     */
    public <T> T getValue(String key, Function<Object, T> function) {
        Object value = getValue(key);
        return value != null ? function.apply(value) : null;
    }

    /**
     * 获取字段类型
     * @param key 字段名
     * @return 字段类型
     */
    public Class<?> getType(String key) {
        ExtraField extraField = this.get(key);
        return extraField != null ? extraField.getType() : null;
    }

}
