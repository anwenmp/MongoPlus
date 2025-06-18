package com.mongoplus.mapping;

import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class的一些信息和操作
 *
 * @author JiaChaoYang
 */
public class SimpleTypeInformation<T> implements TypeInformation {

    /**
     * 实例
     *
     */
    private T instance;

    /**
     * 实例的Class
     *
     */
    private final Class<?> clazz;

    private Type[] types;

    private final Map<String, FieldInformation> fieldMap = new HashMap<>();

    /**
     * 实例的所有Field
     *
     */
    private final List<FieldInformation> fieldList = new ArrayList<>();

    /**
     * 实例的所有Field,不包括父类
     *
     */
    private final List<FieldInformation> thisFieldList = new ArrayList<>();

    /**
     * 简单类型
     */
    private SimpleTypeHolder simpleTypeHolder = SimpleCache.getSimpleTypeHolder();

    /**
     * 实例的某个注解的Field
     *
     */
    private final Map<Class<? extends Annotation>, List<FieldInformation>> annotationFieldMap = new HashMap<>();

    /**
     * 实例的某个注解的Field
     *
     */
    private final Map<Class<? extends Annotation>, List<FieldInformation>> annotationThisFieldMap = new HashMap<>();

    private SimpleTypeInformation(T instance, Class<?> clazz) {
        this.instance = instance;
        this.clazz = clazz;
    }

    protected SimpleTypeInformation(T instance, Type[] types) {
        this.instance = instance;
        this.clazz = getInstanceClass(instance);
        this.types = types;
    }

    private static Class<?> getInstanceClass(Object instance) {
        Class<?> instanceClass = instance.getClass();
        if (ClassTypeUtil.isAnonymousClass(instanceClass)) {
            instanceClass = instanceClass.getSuperclass();
        }
        return instanceClass;
    }

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public Boolean isMap() {
        return ClassTypeUtil.isTargetClass(Map.class, clazz);
    }

    @Override
    public Boolean isCollection() {
        return ClassTypeUtil.isTargetClass(Collection.class, clazz);
    }

    @Override
    public Boolean isSimpleType() {
        return SimpleCache.getSimpleTypeHolder().isSimpleType(clazz);
    }

    @Override
    public Type[] getType() {
        if (ArrayUtils.isEmpty(types)) {
            types = clazz.getTypeParameters();
        }
        return types;
    }

    @Override
    public T getInstance() {
        return instance;
    }

    public static <T> TypeInformation of(T instance) {
        return new SimpleTypeInformation<>(instance, getInstanceClass(instance));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setInstance(Object instance) {
        this.instance = (T) instance;
    }

    @Override
    public List<FieldInformation> getFields() {
        if (CollUtil.isEmpty(fieldList)) {
            Class<?> enclosingClass = clazz.getEnclosingClass();
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) {
                    return;
                }
                if (field.getType().equals(enclosingClass)) {
                    return;
                }
                fieldList.add(new SimpleFieldInformation<>(instance, field));
            });
            getSupperFields(clazz.getSuperclass());
        }
        return this.fieldList;
    }

    @Override
    public List<FieldInformation> getThisFields() {
        if (CollUtil.isEmpty(thisFieldList)) {
            Arrays.stream(clazz.getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                thisFieldList.add(new SimpleFieldInformation<>(instance, field));
            });
        }
        return this.thisFieldList;
    }

    @Override
    public FieldInformation getField(String fieldName) {
        if (!fieldMap.containsKey(fieldName)) {
            FieldInformation fieldInformation = getFields().stream()
                    .filter(field ->
                            Objects.equals(field.getCamelCaseName(), fieldName))
                    .findFirst()
                    .orElse(null);
            fieldMap.put(fieldName, fieldInformation);
        }
        return fieldMap.get(fieldName);
    }

    @Override
    public FieldInformation getFieldNotException(String fieldName) {
        if (!fieldMap.containsKey(fieldName)) {
            try {
                fieldMap.put(fieldName, new SimpleFieldInformation<>(instance, clazz.getDeclaredField(fieldName)));
            } catch (Exception e) {
                return null;
            }
        }
        return fieldMap.get(fieldName);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    @Override
    public List<FieldInformation> getAnnotationFields(Class<? extends Annotation> annotationClass) {
        if (!annotationFieldMap.containsKey(annotationClass)) {
            annotationFieldMap.put(
                    annotationClass,
                    getFields()
                            .stream()
                            .filter(field -> field.getField().getAnnotation(annotationClass) != null)
                            .collect(Collectors.toList()));
        }
        return annotationFieldMap.get(annotationClass);
    }

    @Override
    public List<FieldInformation> getAnnotationThisFields(Class<? extends Annotation> annotationClass) {
        if (!annotationThisFieldMap.containsKey(annotationClass)) {
            annotationThisFieldMap.put(
                    annotationClass,
                    getThisFields()
                            .stream()
                            .filter(field -> field.getField().getAnnotation(annotationClass) != null)
                            .collect(Collectors.toList()));
        }
        return annotationThisFieldMap.get(annotationClass);
    }

    @Override
    public FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass, String nullMessage) {
        List<FieldInformation> fieldList = getAnnotationFields(annotationClass);
        if (CollUtil.isEmpty(fieldList)) {
            throw new MongoPlusFieldException(nullMessage);
        }
        return fieldList.get(0);
    }

    @Override
    public FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass) {
        List<FieldInformation> fieldList = getAnnotationFields(annotationClass);
        if (CollUtil.isEmpty(fieldList)) {
            return null;
        }
        return fieldList.get(0);
    }

    @Override
    public FieldInformation getAnnotationThisField(Class<? extends Annotation> annotationClass) {
        List<FieldInformation> fieldList = getAnnotationThisFields(annotationClass);
        if (CollUtil.isEmpty(fieldList)) {
            return null;
        }
        return fieldList.get(0);
    }

    @Override
    public Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass, String nullMessage) {
        return getAnnotationField(annotationClass, nullMessage).getValue();
    }

    @Override
    public Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass) {
        return getAnnotationField(annotationClass).getValue();
    }

    private void getSupperFields(Class<?> clazz){
        if (clazz != null && !clazz.equals(Object.class)){
            Arrays.asList(clazz.getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                if (Modifier.isStatic(field.getModifiers())) {
                    return;
                }
                fieldList.add(new SimpleFieldInformation<>(instance, field));
            });
            getSupperFields(clazz.getSuperclass());
        }
    }

}
