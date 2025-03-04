package com.mongoplus.scanner.meta;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author anwen
 */
public class MetadataReaderFactory {

    private final Class<?> clazz;

    public MetadataReaderFactory( Class<?> clazz) {
        this.clazz = clazz;
    }

    public MetadataReader getMetadataReader() {
        return new DefaultMetadataReader(this.clazz,getClassmetadata(),getAnnotationMetadata());
    }

    ClassMetadata getClassmetadata(){
        return new ClassMetadataImpl(
                clazz.getName(),
                clazz.getSuperclass(),
                clazz.getInterfaces(),
                clazz.isInterface(),
                Modifier.isAbstract(clazz.getModifiers()),
                Modifier.isFinal(clazz.getModifiers())
        );
    }

    AnnotationMetadata getAnnotationMetadata() {
        Map<Method, Annotation[]> methodAnnotations = new HashMap<>();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            methodAnnotations.put(method,method.getAnnotations());
        }
        return new AnnotationMetadataImpl(clazz.getAnnotations(),methodAnnotations);
    }

    static class DefaultMetadataReader implements MetadataReader {

        private final Class<?> clazz;

        private final ClassMetadata classMetadata;

        private final AnnotationMetadata annotationMetadata;

        DefaultMetadataReader(Class<?> clazz, ClassMetadata classMetadata, AnnotationMetadata annotationMetadata) {
            this.clazz = clazz;
            this.classMetadata = classMetadata;
            this.annotationMetadata = annotationMetadata;
        }

        @Override
        public Class<?> getClazz() {
            return clazz;
        }

        @Override
        public ClassMetadata getClassMetadata() {
            return classMetadata;
        }

        @Override
        public AnnotationMetadata getAnnotationMetadata() {
            return annotationMetadata;
        }
    }

}
