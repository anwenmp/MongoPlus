package com.mongoplus.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * 处理器注册器
 */
public class HandlerRegistry {

    private static final Map<Class<?>,Object> registry = new HashMap<>();

    /**
     * 获取实例
     * @param clazz class
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) registry.get(clazz);
    }

    public static <T> void register(Class<T> clazz, T instance) {
        registry.put(clazz, instance);
    }

}
