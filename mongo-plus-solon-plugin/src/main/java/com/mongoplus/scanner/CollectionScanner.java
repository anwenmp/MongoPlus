package com.mongoplus.scanner;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class CollectionScanner {

    /**
     * 存储每种注解对应的类集合
     * key：注解类型
     * value：被该注解标注的类集合
     */
    private static final Map<Class<? extends Annotation>, Set<Class<?>>> CACHE = new ConcurrentHashMap<>();

    /**
     * 添加一个类到指定注解类型的缓存中
     *
     * @param annotationClazz 注解类型
     * @param clazz           被注解的类
     */
    public static void addCollectionClass(Class<? extends Annotation> annotationClazz, Class<?> clazz) {
        Set<Class<?>> classSet = CACHE.get(annotationClazz);
        if (classSet == null) {
            classSet = new CopyOnWriteArraySet<>();
            Set<Class<?>> existing = CACHE.putIfAbsent(annotationClazz, classSet);
            if (existing != null) {
                classSet = existing;
            }
        }
        classSet.add(clazz);
    }

    /**
     * 获取指定注解类型的类集合
     */
    public static Set<Class<?>> getClasses(Class<? extends Annotation> annotationClazz) {
        return CACHE.getOrDefault(annotationClazz, Collections.emptySet());
    }

    /**
     * 判断某个类是否在指定注解类型的缓存中
     */
    public static boolean contains(Class<? extends Annotation> annotationClazz, Class<?> clazz) {
        Set<Class<?>> set = CACHE.get(annotationClazz);
        return set != null && set.contains(clazz);
    }

    /**
     * 获取所有注解类型及其对应类集合
     */
    public static Map<Class<? extends Annotation>, Set<Class<?>>> getAll() {
        return Collections.unmodifiableMap(CACHE);
    }

    /**
     * 清空所有缓存
     */
    public static void clear() {
        CACHE.clear();
    }

    private CollectionScanner() {
        // 工具类，禁止实例化
    }

}
