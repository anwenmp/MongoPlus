package com.mongoplus.scanner.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author anwen
 */
public interface AnnotationMetadata {

    /**
     * 返回类上声明的注解及其相关属性
     */
    Annotation[] getAnnotations();

    /**
     * 获取指定的注解
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    default <T> T getAnnotation(Class<T> clazz) {
        for (Annotation annotation : getAnnotations()) {
            if (clazz.equals(annotation.annotationType())) {
                return (T) annotation;
            }
        }
        return null;
    }

    /**
     * 获取方法级别的注解
     */
    Map<Method, Annotation[]> getMethodAnnotations();

}
