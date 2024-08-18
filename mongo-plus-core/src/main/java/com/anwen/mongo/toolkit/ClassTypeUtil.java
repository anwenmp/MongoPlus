package com.anwen.mongo.toolkit;

import com.anwen.mongo.annotation.ID;
import com.anwen.mongo.domain.MongoPlusException;
import com.anwen.mongo.domain.MongoPlusFieldException;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class工具类
 * @author anwen
 * @date 2024/8/2 上午1:04
 */
public class ClassTypeUtil {

    private static final Log log = LogFactory.getLog(ClassTypeUtil.class);

    // 内部缓存，存储已经处理过的对象类型及其字段的类型
    private static final Map<Class<?>, List<Class<?>>> cacheMap = new ConcurrentHashMap<>();

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private static volatile ClassTypeUtil instance;

    private static final Map<Class<?>,Set<Class<?>>> cacheClass = new ConcurrentHashMap<>();

    private static final Map<Class<?>, ConcurrentHashMap<Class<?>, Boolean>> isTargetClassMap = new ConcurrentHashMap<>();

    private static final Map<Class<?> , Boolean> isAnonymousClassMap = new ConcurrentHashMap<>();

    static {
        isTargetClassMap.put(Map.class, new ConcurrentHashMap<>());
        isTargetClassMap.put(Collection.class, new ConcurrentHashMap<>());
        isTargetClassMap.put(Enum.class, new ConcurrentHashMap<>());
    }

    private ClassTypeUtil() {
    }

    public static ClassTypeUtil getInstance() {
        if (instance == null) {
            synchronized (ClassTypeUtil.class) {
                if (instance == null) {
                    instance = new ClassTypeUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 获取对象的所有字段类型
     * @param clazz 待获取类型字段的class
     * @return 对象的所有字段类型列表
     */
    public static synchronized List<Class<?>> getAllFieldClasses(Class<?> clazz) {
        // 获取对象类型
        // 查找缓存中是否已有该类型对象的记录
        if (cacheMap.containsKey(clazz)) {
            // 如果已有记录，直接返回缓存中存储的结果
            return cacheMap.get(clazz);
        }
        // 如果缓存中没有记录，则使用反射获取对象类型及其所有字段的类型
        List<Class<?>> classList = new ArrayList<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            classList.add(getClassByFieldType(field));
        }
        // 将结果存储到缓存中
        cacheMap.put(clazz, classList);
        // 返回结果
        return classList;
    }

    /**
     * 获取field的类型
     * @author JiaChaoYang
     * @date 2023/8/9 22:04
    */
    public static Class<?> getClassByFieldType(Field field){
        Class<?> fieldType = field.getType();
        if (fieldType.isArray()) {
            // 如果字段类型为数组，获取数组元素类型并添加到列表中
            return fieldType.getComponentType();
        } else if (ClassTypeUtil.isTargetClass(Collection.class,fieldType)) {
            // 如果字段类型为集合，则获取集合元素的类型并添加到列表中
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericType;
                return (Class<?>) parameterizedType.getActualTypeArguments()[0];
            }
        }
        // 如果字段类型为普通类型，则直接添加到列表中
        return fieldType;
    }

    /**
     * 判断字段是否是自定义类型
     * @param field 字段
     * @return java.lang.Boolean
     * @author JiaChaoYang
     * @date 2023/8/9 22:05
    */
    public static Boolean isItCustomType(Field field){
        return CustomClassUtil.isCustomObject(getClassByFieldType(field));
    }

    /**
     * 获取对象的所有自定义类字段类型
     * @param clazz 待获取类型字段的class
     * @return 对象的所有字段类型列表
     */
    public static synchronized List<Class<?>> getAllCustomFieldClasses(Class<?> clazz){
        List<Class<?>> result = new ArrayList<>();
        List<Class<?>> fieldClasses = getAllFieldClasses(clazz);
        fieldClasses.parallelStream().forEach(field -> {
            if (CustomClassUtil.isCustomObject(field)){
                result.add(field);
                result.addAll(getAllCustomFieldClasses(field));
            }
        });
        return result;
    }

    public static <T> Object getClassFieldValue(T entity,String field){
        Field declaredField;
        try {
            declaredField = entity.getClass().getDeclaredField(field);
            declaredField.setAccessible(true);
            return declaredField.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取实体类中，ID注解的值的值
     * @author JiaChaoYang
     * @date 2023/8/30 22:05
    */
    public static <T> Object getIdByEntity(T entity,boolean exception){
        Optional<Field> fieldOptional = getFields(ClassTypeUtil.getClass(entity)).stream().peek(field -> field.setAccessible(true)).filter(field -> field.getAnnotation(ID.class) != null).findFirst();
        if (!fieldOptional.isPresent()){
            if (exception){
                return null;
            }
            throw new MongoPlusFieldException("_id undefined");
        }
        try {
            return fieldOptional.get().get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取类的所有字段，包括父类中的字段
     **/
    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = FIELD_CACHE.get(clazz);
        if (fields == null) {
            fields = new ArrayList<>();
            if (!clazz.equals(Object.class)){
                fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
                getSupperFields(fields,clazz.getSuperclass());
            }
            FIELD_CACHE.put(clazz, fields);
        }
        return fields;
    }

    private static void getSupperFields(List<Field> fieldList,Class<?> clazz){
        if (clazz != null && !clazz.equals(Object.class)){
            fieldList.addAll(Arrays.asList(clazz.getDeclaredFields()));
            getSupperFields(fieldList,clazz.getSuperclass());
        }
    }

    /**
     * 获取List的泛型
     * @author JiaChaoYang
     * @date 2023/11/10 14:54
    */
    public static Class<?> getListGenericType(Field field) {
        return getListGenericType(field.getGenericType());
    }

    public static Class<?> getListGenericType(Class<?> clazz){
        return getListGenericType(clazz.getGenericSuperclass());
    }

    public static Class<?> getListGenericType(Type genericType) {
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (actualTypeArguments.length > 0 && actualTypeArguments[0] instanceof Class) {
                return (Class<?>) actualTypeArguments[0];
            }
        }
        return Object.class;
    }

    public static Class<?> getListClass(List<?> list){
        ParameterizedType parameterizedType = (ParameterizedType) list.getClass().getGenericSuperclass();
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        return (Class<?>) typeArguments[0];
    }

    public static <T> Class<?> getClass(T entity){
        Class<?> entityClass = entity.getClass();
        if (entityClass.isAnonymousClass()){
            entityClass = entityClass.getSuperclass();
        }
        return entityClass;
    }

    /**
     * 获取类
     * @author JiaChaoYang
     * @date 2023/11/10 14:54
    */
    public static Class<?> getClassFromType(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClassFromType(componentType);
            return java.lang.reflect.Array.newInstance(componentClass, 0).getClass();
        } else if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            if (bounds.length > 0) {
                return getClassFromType(bounds[0]);
            }
        }
        throw new IllegalArgumentException("Type not supported: " + type);
    }

    public static <T> Object getInstanceByClass(Class<T> clazz){
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to create " + clazz.getName() +", message: {}", e.getMessage(), e);
            throw new MongoPlusException("Failed to create " + clazz.getName());
        }
    }

    public static Boolean isTargetClass(Class<?> targetClazz, Class<?> sourceClazz) {
        // 获取或初始化目标类的缓存映射
        ConcurrentHashMap<Class<?>, Boolean> classBooleanMap = isTargetClassMap.computeIfAbsent(targetClazz, k -> new ConcurrentHashMap<>());

        // 获取或计算源类是否是目标类的子类
        return classBooleanMap.computeIfAbsent(sourceClazz, targetClazz::isAssignableFrom);
    }

    public static Boolean isAnonymousClass(Class<?> clazz){
        return isAnonymousClassMap.computeIfAbsent(clazz, k -> clazz.isAnonymousClass());
    }

}

