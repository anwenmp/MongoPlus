package com.mongoplus.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心
 */
public class ComponentRegistry {

    private static final Map<Class<?>,Object> resources = new ConcurrentHashMap<>();

    /**
     * 获取实例
     * @param clazz class
     */
    @SuppressWarnings("unchecked")
    public static <T> T get(Class<T> clazz) {
        return (T) resources.get(clazz);
    }

    /**
     * 注册实例
     * @param clazz 实例class
     * @param instance 实例
     */
    public static <T> void register(Class<T> clazz, T instance) {
        resources.put(clazz, instance);
    }

    /**
     * 注册实例
     * @param instance 实例
     * @param <T> 泛型
     */
    @SuppressWarnings("unchecked")
    public static <T> void register(T instance) {
        register((Class<T>) instance.getClass(), instance);
    }

}
