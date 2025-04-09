package com.mongoplus.mapping;

import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 对Field的简单封装
 * @author anwen
 **/
public interface FieldInformation {

    /**
     * 清空一些无关实例的缓存
     * @author anwen
     */
    void clear();

    /**
     * 清空一些无关实例的缓存，并设置实例
     * @author anwen
     */
    default void clearAndSetInstance(Object instance) {
        clear();
        setInstance(instance);
    }

    /**
     * 获取字段值
     * @return {@link Object}
     * @author anwen
     */
    Object getValue();

    /**
     * 获取字段值
     * @param instance 类的实例
     * @return {@link java.lang.Object}
     * @author anwen
     */
    Object getValue(Object instance);

    /**
     * 设置实例
     * @author anwen
     */
    void setInstance(Object instance);

    /**
     * 获取字段名，受{@link CollectionField}注解的影响
     * @return {@link java.lang.String}
     * @author anwen
     */
    String getName();

    /**
     * 获取字段名，受驼峰转下划线配置的影响
     * @return {@link java.lang.String}
     * @author anwen
     */
    String getCamelCaseName();

    /**
     * 获取id或字段名，受{@link ID}和驼峰转下划线配置的影响
     * @return {@link String}
     * @author anwen
     */
    String getIdOrCamelCaseName();

    /**
     * 是否是Map
     * @return {@link boolean}
     * @author anwen
     */
    boolean isMap();

    /**
     * 获取原始字段
     * @return {@link java.lang.reflect.Field}
     * @author anwen
     */
    Field getField();

    /**
     * 获取字段Type的Class
     * @return {@link java.lang.Class}
     * @author anwen
     */
    Class<?> getTypeClass();

    /**
     * 获取字段的Type
     * @return {@link java.lang.reflect.Type[]}
     * @author anwen
     */
    Type[] getType();

    /**
     * 将字段封装为TypeInformation
     * @return {@link TypeInformation}
     * @author anwen
     */
    TypeInformation getTypeInformation();

    /**
     * 如果是Map，则获取Map的Value类型
     * @return {@link java.lang.Class}
     * @author anwen
     */
    Class<?> mapValueType();

    /**
     * 如果是集合，则获取集合的泛型
     * @return {@link java.lang.Class}
     * @author anwen
     */
    Class<?> collectionValueType();

    /**
     * 是否是集合
     * @return {@link boolean}
     * @author anwen
     */
    boolean isCollection();

    /**
     * 是否是简单类型
     * @return {@link boolean}
     * @author anwen
     */
    boolean isSimpleType();

    /**
     * 是否跳过检查
     * @return {@link boolean}
     * @author anwen
     */
    boolean isSkipCheckField();

    /**
     * exits和@ID字段则跳过
     * @return {@link boolean}
     * @author anwen
     */
    boolean isSkipCheckFieldAndId();

    /**
     * 是否是id字段
     * @return {@link boolean}
     * @author anwen
     */
    boolean isId();

    /**
     * 获取ID字段注解
     * @return {@link com.mongoplus.annotation.ID}
     * @author anwen
     */
    ID getId();

    /**
     * 获取字段get方法
     * @return {@link java.lang.reflect.Method}
     * @author anwen
     */
    Method getMethod();

    /**
     * 获取字段set方法
     * @return {@link java.lang.reflect.Method}
     * @author anwen
     */
    Method setMethod();

    /**
     * 设置值
     * @param value 值
     * @author anwen
     */
    void setValue(Object value);

    /**
     * 设置值
     * @param instance 示例
     * @param value 值
     * @author anwen
     */
    void setValue(Object instance , Object value);

    /**
     * 获取字段的{@link CollectionField} 注解
     * @return {@link com.mongoplus.annotation.collection.CollectionField}
     * @author anwen
     */
    CollectionField getCollectionField();

    /**
     * 获取指定注解
     * @param annotationClass 注解Class
     * @return {@link Annotation}
     * @author anwen
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

    /**
     * 是否存在指定类型的注解
     * @param annotationClass 注解Class
     * @return {@link boolean}
     * @author anwen
     */
    boolean isAnnotation(Class<? extends Annotation> annotationClass);

    /**
     * 获取字段的genericType
     * @return {@link java.lang.reflect.Type}
     * @author anwen
     */
    Type getGenericType();

}
