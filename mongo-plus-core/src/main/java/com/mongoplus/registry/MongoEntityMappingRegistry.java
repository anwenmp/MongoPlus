package com.mongoplus.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 注册中心，用于管理 MongoDB 的实体映射关系
 * 支持单例模式与线程安全
 * @author anwen
 */
public class MongoEntityMappingRegistry {

    /**
     * 存储 Mongo 连接对象关联的实体 {"mongo连接fullName":"mongo集合实体class"}
     */
    private final Map<String, Class<?>> mappingResources = new ConcurrentHashMap<>();

    /**
     * 单例实例
     */
    private static final MongoEntityMappingRegistry INSTANCE = new MongoEntityMappingRegistry();

    /**
     * 私有化构造器，防止外部实例化
     * @author anwen
     */
    private MongoEntityMappingRegistry() {}

    /**
     * 获取单例实例
     * @author anwen
     * @return {@link MongoEntityMappingRegistry}
     */
    public static MongoEntityMappingRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * 添加映射关系
     * @author anwen
     * @param fullName Mongo 连接 fullName
     * @param clazz    实体类
     */
    public void setMappingRelation(String fullName, Class<?> clazz) {
        mappingResources.putIfAbsent(fullName, clazz);
    }

    /**
     * 获取映射结果资源
     * @author anwen
     * @param fullName 命名空间的 fullName
     * @return {@link java.lang.Class}
     */
    public Class<?> getMappingResource(String fullName) {
        return mappingResources.get(fullName);
    }

    /**
     * 移除一个映射关系
     * @author anwen
     * @param fullName 命名空间的 fullName
     */
    public void removeMappingRelation(String fullName) {
        mappingResources.remove(fullName);
    }

    /**
     * 清空所有映射关系
     * @author anwen
     */
    public void clearMappingRelations() {
        mappingResources.clear();
    }
}
