package com.mongoplus.scanner.meta;

public interface MetadataReader {

    /**
     * 返回类
     */
    Class<?> getClazz();

    /**
     * 读取基础类的基本类元数据。
     */
    ClassMetadata getClassMetadata();

    /**
     * 读取底层类的完整注解元数据，
     * 包括带注解方法的元数据。
     */
    AnnotationMetadata getAnnotationMetadata();

}
