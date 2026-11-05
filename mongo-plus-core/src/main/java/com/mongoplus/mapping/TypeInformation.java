package com.mongoplus.mapping;

import com.mongoplus.cache.global.InformationCache;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.toolkit.ClassTypeUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 对Class进行的简单封装，调用请求更便捷
 *
 * @author anwen
 */
public interface TypeInformation {

    /**
     * 根据Class构建一个TypeInformation
     *
     * @param clazz 类
     * @return {@link TypeInformation}
     * @author anwen
     */
    static TypeInformation of(Class<?> clazz) {
        return of(ClassTypeUtil.getInstanceByClass(clazz));
    }

    /**
     * 根据Class构建一个TypeInformation，并进行缓存，但是他们的实例是不一样的
     * <p style='color: red'>存在并发问题</p>
     * @param clazz 类
     * @return {@link TypeInformation}
     * @author anwen
     */
    static TypeInformation ofCache(Class<?> clazz) {
        TypeInformation typeInformation = InformationCache.get(clazz);
        if (typeInformation == null) {
            typeInformation = of(clazz);
            InformationCache.add(typeInformation);
        }
        Object instance = ClassTypeUtil.getInstanceByClass(clazz);
        typeInformation.setInstance(instance);
        typeInformation.getFields().forEach(fi -> fi.clearAndSetInstance(instance));
        return typeInformation;
    }

    /**
     * 根据实例构建一个TypeInformation
     *
     * @param instance 实例
     * @return {@link com.mongoplus.mapping.TypeInformation}
     * @author anwen
     */
    static TypeInformation of(Object instance) {
        return SimpleTypeInformation.of(instance);
    }

    /**
     * 获取实例
     *
     * @author anwen
     */
    <T> T getInstance();

    /**
     * 设置实例
     *
     * @param instance 实例
     * @author anwen
     */
    void setInstance(Object instance);

    /**
     * 获取用户类及自定义父类的映射字段，过滤 static/synthetic，在 JDK 父类处终止。
     * JDK 根类型不能作为实体展开，访问或扫描失败会抛出异常。
     *
     * @author anwen
     */
    List<FieldInformation> getFields();

    /**
     * 获取当前用户类的全部声明字段，不包含父类，保留 static/synthetic 字段。
     * 不用于映射字段过滤；JDK 根类型不能通过该入口展开。
     *
     * @author anwen
     */
    List<FieldInformation> getThisFields();

    /**
     * 按映射名称获取映射字段，包含自定义父类；不存在时返回 null。
     *
     * @param fieldName 字段名
     * @return {@link com.mongoplus.mapping.FieldInformation}
     * @author anwen
     */
    FieldInformation getField(String fieldName);

    /**
     * 按 Java 字段名查询当前类声明字段，保留 static/synthetic，不查询父类。
     * 字段不存在时返回 null；存在但访问失败时抛出保留原始原因的异常。
     *
     * @param fieldName 字段名
     * @return {@link com.mongoplus.mapping.FieldInformation}
     * @author anwen
     */
    FieldInformation getFieldNotException(String fieldName);

    /**
     * 获取类上的指定注解
     *
     * @return {@link java.util.List<java.lang.annotation.Annotation>}
     * @author anwen
     */
    <A extends Annotation> A getAnnotation(Class<A> annotationClass);

    /**
     * 获取类上的指定注解
     *
     * @return {@code A[]}
     * @author anwen
     */
    <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass);

    /**
     * 判断类上是否存在指定注解
     * @param annotationClass 注解类
     * @return {@code boolean}
     */
    boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);

    /**
     * 根据注解获取字段
     *
     * @param annotationClass 注解类
     * @return {@link java.util.List<com.mongoplus.mapping.FieldInformation>}
     * @author anwen
     */
    List<FieldInformation> getAnnotationFields(Class<? extends Annotation> annotationClass);

    /**
     * 根据注解获取字段，不包含父类
     *
     * @param annotationClass 注解类
     * @return {@link java.util.List<com.mongoplus.mapping.FieldInformation>}
     * @author anwen
     */
    List<FieldInformation> getAnnotationThisFields(Class<? extends Annotation> annotationClass);

    /**
     * 根据注解获取一个字段，不存在则抛出{@link MongoPlusFieldException}异常
     *
     * @param annotationClass 注解类
     * @param nullMessage     异常信息
     * @return {@link FieldInformation}
     * @author anwen
     */
    FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass, String nullMessage);

    /**
     * 根据注解获取一个字段，不存在则返回null
     *
     * @param annotationClass 注解类
     * @return {@link com.mongoplus.mapping.FieldInformation}
     * @author anwen
     */
    FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass);

    /**
     * 根据注解获取一个字段，不存在则返回null，不包括父类
     *
     * @param annotationClass 注解类
     * @return {@link com.mongoplus.mapping.FieldInformation}
     * @author anwen
     */
    FieldInformation getAnnotationThisField(Class<? extends Annotation> annotationClass);

    /**
     * 获取注解字段的字段值，不存在则抛出{@link MongoPlusFieldException}异常
     *
     * @param annotationClass 注解类
     * @param nullMessage     异常信息
     * @return {@link Object}
     * @author anwen
     */
    Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass, String nullMessage);

    /**
     * 获取注解字段的字段值，不存在则返回null
     *
     * @param annotationClass 注解类
     * @return {@link Object}
     * @author anwen
     */
    Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass);

    /**
     * 获取原始Class
     *
     * @return {@link Class}
     * @author anwen
     */
    Class<?> getClazz();

    /**
     * 是否是map
     *
     * @return {@link Boolean}
     * @author anwen
     */
    Boolean isMap();

    /**
     * 是否是集合
     *
     * @return {@link java.lang.Boolean}
     * @author anwen
     */
    Boolean isCollection();

    /**
     * 是否是简单类型
     *
     * @return {@link java.lang.Boolean}
     * @author anwen
     */
    Boolean isSimpleType();

    /**
     * 获取Type
     *
     * @return {@link Type[]}
     * @author anwen
     */
    Type[] getType();

}
