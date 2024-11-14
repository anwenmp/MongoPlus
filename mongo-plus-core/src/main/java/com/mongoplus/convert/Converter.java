package com.mongoplus.convert;

import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.toolkit.InstantUtil;
import com.mongoplus.toolkit.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class Converter {

    public static Map<String, Object> convertKeysToCamelCase(Map<String, Object> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> convertToCamelCaseIfNeeded(entry.getKey()),
                        entry -> convertValue(entry.getValue())
                ));
    }

    private static String convertToCamelCaseIfNeeded(String key) {
        return PropertyCache.camelToUnderline ? StringUtils.convertToCamelCase(key) : key;
    }

    private static Object convertValue(Object value) {
        if (value instanceof Date) {
            return InstantUtil.convertTimestampToLocalDateTime8(((Date) value).toInstant());
        }
        return value;
    }
}
