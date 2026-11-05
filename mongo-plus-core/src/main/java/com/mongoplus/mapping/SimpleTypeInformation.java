package com.mongoplus.mapping;

import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

    /** 显式声明字段查询与映射字段查询使用独立缓存，避免过滤规则互相污染。 */
    private final Map<String, FieldInformation> declaredFieldMap = new HashMap<>();

    /**
     * 实例的所有Field
     *
     */
    private volatile List<FieldInformation> fieldList;

    /**
     * 实例的所有Field,不包括父类
     *
     */
    private volatile List<FieldInformation> thisFieldList;

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

    /**
     * 完整扫描用户类及其自定义父类的映射字段，成功后才发布缓存。
     * 静态与合成字段在设置可访问性之前过滤；JDK 父类作为继承扫描边界。
     *
     * @return 完整字段列表；空列表同样只初始化一次
     * @throws MongoPlusFieldException 根类型属于 JDK 或字段无法访问时抛出
     */
    @Override
    public List<FieldInformation> getFields() {
        List<FieldInformation> fields = fieldList;
        if (fields == null) {
            synchronized (this) {
                fields = fieldList;
                if (fields == null) {
                    assertUserType();
                    fields = new ArrayList<>();
                    addMappingFields(clazz, fields);
                    getSupperFields(clazz.getSuperclass(), fields);
                    fieldList = fields;
                }
            }
        }
        return fields;
    }

    /**
     * 获取当前用户类全部声明字段，保留 static/synthetic 的原有查询语义。
     * 该入口不用于映射过滤，且仅在全部字段可访问后发布缓存。
     *
     * @return 当前类声明字段，不包含父类
     * @throws MongoPlusFieldException 根类型属于 JDK 或字段无法访问时抛出
     */
    @Override
    public List<FieldInformation> getThisFields() {
        List<FieldInformation> fields = thisFieldList;
        if (fields == null) {
            synchronized (this) {
                fields = thisFieldList;
                if (fields == null) {
                    assertUserType();
                    fields = new ArrayList<>();
                    for (Field field : clazz.getDeclaredFields()) {
                        fields.add(new SimpleFieldInformation<>(instance, field));
                    }
                    thisFieldList = fields;
                }
            }
        }
        return fields;
    }

    /**
     * 按映射名称查询完整映射字段，包含自定义父类，且不受声明字段查询缓存影响。
     *
     * @param fieldName 映射名称
     * @return 匹配的字段，不存在时返回 null
     */
    @Override
    public synchronized FieldInformation getField(String fieldName) {
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

    /**
     * 显式查询当前类的声明字段，保留 static/synthetic 查询能力，不搜索父类。
     * 只有字段不存在返回 null，访问异常携带真实原因向上传播，失败不写入缓存。
     *
     * @param fieldName Java 声明字段名
     * @return 字段信息；字段不存在时返回 null
     * @throws MongoPlusFieldException 字段存在但无法设置可访问性时抛出
     */
    @Override
    public synchronized FieldInformation getFieldNotException(String fieldName) {
        if (!declaredFieldMap.containsKey(fieldName)) {
            try {
                declaredFieldMap.put(fieldName, new SimpleFieldInformation<>(instance, clazz.getDeclaredField(fieldName)));
            } catch (NoSuchFieldException e) {
                declaredFieldMap.put(fieldName, null);
                return null;
            }
        }
        return declaredFieldMap.get(fieldName);
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return clazz.getAnnotationsByType(annotationClass);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return clazz.isAnnotationPresent(annotationClass);
    }

    /**
     * 并发安全地缓存映射字段的注解查询结果；底层扫描失败不发布结果。
     *
     * @param annotationClass 注解类型
     * @return 匹配注解的映射字段
     */
    @Override
    public synchronized List<FieldInformation> getAnnotationFields(Class<? extends Annotation> annotationClass) {
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

    /**
     * 并发安全地缓存当前类声明字段的注解查询，保留原始声明字段语义。
     *
     * @param annotationClass 注解类型
     * @return 匹配注解的当前类声明字段
     */
    @Override
    public synchronized List<FieldInformation> getAnnotationThisFields(Class<? extends Annotation> annotationClass) {
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

    /**
     * 将自定义父类字段追加到本次局部扫描结果，在 JDK 实现类处终止。
     *
     * @param superclass 待扫描父类
     * @param fields 尚未发布的局部字段列表
     */
    private void getSupperFields(Class<?> superclass, List<FieldInformation> fields) {
        if (superclass != null && !Object.class.equals(superclass) && !isJdkType(superclass)) {
            addMappingFields(superclass, fields);
            getSupperFields(superclass.getSuperclass(), fields);
        }
    }

    /**
     * 先过滤无需映射的字段，再由字段信息构造器统一设置可访问性。
     * synthetic 标记区分编译器引用与用户显式声明的同类型 owner 字段。
     *
     * @param declaringClass 字段声明类，而不是字段值的类型
     * @param fields 尚未发布的局部字段列表
     */
    private void addMappingFields(Class<?> declaringClass, List<FieldInformation> fields) {
        for (Field field : declaringClass.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
                fields.add(new SimpleFieldInformation<>(instance, field));
            }
        }
    }

    /**
     * JDK 根对象不能按实体展开，也不能伪装成没有字段而静默保存为空对象。
     *
     * @throws MongoPlusFieldException 当前根类型属于 JDK 时抛出
     */
    private void assertUserType() {
        if (!Object.class.equals(clazz) && isJdkType(clazz)) {
            throw new MongoPlusFieldException("Cannot scan JDK implementation fields of " + clazz.getName());
        }
    }

    /**
     * 判断声明类是否位于平台实现边界，不依据实体字段值的类型过滤字段。
     * 使用类加载器和平台包名，兼容 Java 8 的 bootstrap/extension 加载方式。
     *
     * @param type 待检查的声明类
     * @return 是否属于平台实现类
     */
    private static boolean isJdkType(Class<?> type) {
        String name = type.getName();
        return type.getClassLoader() == null || name.startsWith("java.") || name.startsWith("javax.")
                || name.startsWith("jdk.") || name.startsWith("sun.") || name.startsWith("com.sun.");
    }

}
