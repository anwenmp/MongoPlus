package com.mongoplus.scanner.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author anwen
 */
public class AnnotationMetadataImpl implements AnnotationMetadata {
    private final Annotation[] annotations;
    private final Map<Method, Annotation[]> methodAnnotations;

    public AnnotationMetadataImpl(Annotation[] annotations, Map<Method, Annotation[]> methodAnnotations) {
        this.annotations = annotations;
        this.methodAnnotations = methodAnnotations;
    }

    @Override
    public Annotation[] getAnnotations() {
        // 返回所有类级别的注解
        return annotations;
    }

    @Override
    public Map<Method, Annotation[]> getMethodAnnotations() {
        // 返回所有方法级别的注解
        return methodAnnotations;
    }
}
