package com.mongoplus.mapping;

import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.conditions.interfaces.update.field.operations.Holder;
import com.mongoplus.domain.MongoPlusWriteException;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.FieldHandlerChain;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.toolkit.BsonUtil;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.ObjectIdUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 将对象映射为Document
 *
 * @author JiaChaoYang
 **/
public class MappingMongoConverter extends AbstractMongoConverter {

    private final Log log = LogFactory.getLog(MappingMongoConverter.class);

    private final SimpleTypeHolder simpleTypeHolder;

    private List<Class<?>> ignoreType = new ArrayList<>();

    private final Map<Type, Class<?>> typeClassCache = new ConcurrentHashMap<>();

    private final Map<Type, Type> genericTypeCache = new ConcurrentHashMap<>();

    /** 只记录当前线程正在递归的对象身份，允许兄弟路径复用同一子对象。 */
    private final ThreadLocal<Set<Object>> writePath = new ThreadLocal<>();

    public MappingMongoConverter() {
        this.simpleTypeHolder = SimpleCache.getSimpleTypeHolder();
        ignoreType.add(ObjectId.class);
        ignoreType.add(Binary.class);
    }

    public MappingMongoConverter(List<Class<?>> ignoreType){
        this.simpleTypeHolder = SimpleCache.getSimpleTypeHolder();
        this.ignoreType = ignoreType;
    }

    /**
     * 写入顶层实体字段，并在显式 owner 等业务回指形成循环时明确失败。
     *
     * @param sourceObj 源实体
     * @param bson 目标文档
     * @param typeInformation 实体字段元数据
     * @throws MongoPlusWriteException 存在递归循环引用时抛出
     */
    @Override
    public void write(Object sourceObj, Bson bson, TypeInformation typeInformation) {
        enterWrite(sourceObj);
        try {
            processFields(typeInformation.getFields(), bson, true);
        } finally {
            exitWrite(sourceObj);
        }
    }

    /**
     * 写入内部对象，在扫描其合法字段前登记递归身份，并在失败时清理路径。
     * @param sourceObj 源对象
     * @param bson bson
     * @return {@link Bson}
     */
    public Bson writeInternal(Object sourceObj, Bson bson) {
        enterWrite(sourceObj);
        try {
            processFields(TypeInformation.of(sourceObj).getFields(), bson, false);
        } finally {
            exitWrite(sourceObj);
        }
        return bson;
    }

    /**
     * 处理字段信息并写入 BSON
     * @param fields 字段信息列表
     * @param bson BSON 对象
     * @param filterId 是否过滤掉 ID 字段
     */
    private void processFields(List<FieldInformation> fields, Bson bson, boolean filterId) {
        fields.stream()
                .filter(fieldInformation -> !fieldInformation.isSkipCheckField() && (!filterId || !fieldInformation.isId()))
                .forEach(fieldInformation -> {
                    CollectionField collectionField = fieldInformation.getCollectionField();
                    Object obj = null;
                    String fieldName = fieldInformation.getName();
                    if ((collectionField == null || StringUtils.isBlank(collectionField.value()))
                            && PropertyCache.camelToUnderline){
                        fieldName = StringUtils.camelToUnderline(fieldName);
                    }
                    if (ignoreType.contains(fieldInformation.getTypeClass())){
                        obj = fieldInformation.getValue();
                    }
                    if (collectionField != null && collectionField.isObjectId()) {
                        obj = ObjectIdUtil.getObjectIdValue(fieldInformation.getValue());
                    }
                    Object currentValue = obj == null ? fieldInformation.getValue() : obj;
                    for (FieldHandler fieldHandler : FieldHandlerChain.getInstance()) {
                        if (fieldHandler.activate().apply(fieldInformation)) {
                            Object handlerResult = fieldHandler.handler(fieldInformation, currentValue);
                            if (handlerResult != null) {
                                obj = handlerResult;
                                currentValue = handlerResult;
                            }
                        }
                    }
                    // 如果字段处理器均未返回非null结果，则继续走默认处理
                    if (obj != null) {
                        BsonUtil.addToMap(bson, fieldName, obj);
                    } else {
                        writeProperties(bson, fieldName, fieldInformation.getValue(),collectionField);
                    }
                });
    }

    /**
     * 属性写入Bson中，并校验`CollectionField`注解的`ignoreNull`属性
     * @param bson bson
     * @param key key
     * @param sourceObj 源对象
     * @param collectionField 可选的 CollectionField 注解
     * @author anwen
     */
    private void writeProperties(Bson bson, String key, Object sourceObj, CollectionField collectionField) {
        if (shouldIgnoreNull(sourceObj, collectionField)) {
            return;
        }
        BsonUtil.addToMap(bson, key, writeProperties(sourceObj));
    }

    /**
     * 属性写入Bson中
     * @param bson bson
     * @param key key
     * @param sourceObj 源对象
     * @author anwen
     */
    private void writeProperties(Bson bson, String key, Object sourceObj) {
        if (shouldIgnoreNull(sourceObj, null)) {
            return;
        }
        BsonUtil.addToMap(bson, key, writeProperties(Holder.isNull(sourceObj)));
    }

    /**
     * 校验是否应该跳过该属性（基于null值和ignoreNull属性）
     * @param sourceObj 源对象
     * @param collectionField 可选的 CollectionField 注解
     * @return 是否应该跳过该属性
     */
    private boolean shouldIgnoreNull(Object sourceObj, CollectionField collectionField) {
        // 如果 collectionField 不为 null，且有 ignoreNull 属性，则基于该属性做判断
        if (collectionField != null && !collectionField.ignoreNull() && sourceObj == null) {
            return false;
        }
        return PropertyCache.ignoringNull && sourceObj == null;
    }

    /**
     * 按原有策略分派属性映射；集合/数组在转换容器前登记原对象身份。
     * @param sourceObj 源对象
     * @return {@link Object}
     * @author anwen
     */
    private Object writeProperties(Object sourceObj){
        Object resultObj;
        MappingStrategy<Object> mappingStrategy = null;
        if (sourceObj != null) {
            mappingStrategy = getMappingStrategy(sourceObj.getClass());
        }
        if (mappingStrategy != null) {
            try {
                resultObj = mappingStrategy.mapping(sourceObj);
            } catch (IllegalAccessException e) {
                String error = String.format("Exception mapping %s to simple type", sourceObj.getClass().getName());
                log.error(error,e);
                throw new MongoPlusWriteException(error);
            }
        } else if (sourceObj == null ||
                simpleTypeHolder.isSimpleType(sourceObj.getClass()) ||
                simpleTypeHolder.isMongoType(sourceObj.getClass())) {
            resultObj = getPotentiallyConvertedSimpleWrite(sourceObj);
        } else if (ClassTypeUtil.isTargetClass(Collection.class,sourceObj.getClass()) || sourceObj.getClass().isArray()) {
            enterWrite(sourceObj);
            try {
                resultObj = writeCollectionInternal(BsonUtil.asCollection(sourceObj), new ArrayList<>());
            } finally {
                exitWrite(sourceObj);
            }
        } else if (ClassTypeUtil.isTargetClass(Map.class,sourceObj.getClass())) {
            resultObj = writeMapInternal((Map<?, ?>) sourceObj,new Document());
        } else {
            resultObj = writeInternal(sourceObj,new Document());
        }
        return resultObj;
    }

    /**
     * 按原有 key/value 规则写入 Map，并保护其递归路径免于循环引用。
     * @param obj 源对象
     * @param bson bson
     * @return {@link Bson}
     * @author anwen
     */
    @Override
    public Bson writeMapInternal(Map<?,?> obj,Bson bson) {
        enterWrite(obj);
        try {
            //循环map
            obj.forEach((k,v) -> {
                //如果key是简单类型
                if (simpleTypeHolder.isSimpleType(k.getClass())){
                    String key = String.valueOf(k);
                    if (PropertyCache.camelToUnderline && !key.startsWith("$")){
                        key = StringUtils.camelToUnderline(key);
                    }
                    writeProperties(bson,key,v);
                }else {
                    throw new MongoPlusWriteException("Cannot use a complex object as a key value");
                }
            });
        } finally {
            exitWrite(obj);
        }
        return bson;
    }

    /**
     * 进入默认递归写路径；不使用 equals/hashCode，以免误判值相等的不同对象。
     *
     * @param source 当前待展开对象
     * @throws MongoPlusWriteException 当前路径已包含该对象时抛出
     */
    private void enterWrite(Object source) {
        Set<Object> path = writePath.get();
        if (path == null) {
            path = Collections.newSetFromMap(new IdentityHashMap<Object, Boolean>());
            writePath.set(path);
        }
        if (!path.add(source)) {
            throw new MongoPlusWriteException("Cyclic reference detected while mapping " + source.getClass().getName());
        }
    }

    /**
     * 无论成功或异常都移除当前对象，最外层退出后释放线程本地状态。
     *
     * @param source 当前结束展开的对象
     */
    private void exitWrite(Object source) {
        Set<Object> path = writePath.get();
        path.remove(source);
        if (path.isEmpty()) {
            writePath.remove();
        }
    }

    /**
     * 集合类型的处理
     * @param obj 源对象
     * @param sink 集合
     * @return {@link Collection}
     * @author anwen
     */
    @SuppressWarnings(value = "unchecked")
    private Collection<?> writeCollectionInternal(Collection<?> obj, Collection<?> sink){
        List<Object> collection = sink instanceof List ? (List<Object>) sink : new ArrayList<>(sink);
        obj.forEach(element -> collection.add(writeProperties(element)));
        return collection;
    }

    @Override
    public void write(Map<?, ?> map, Bson bson) {
        writeMapInternal(map,bson);
    }

    @Override
    public <T> T readInternal(Object sourceObj, TypeReference<T> typeReference){
        Class<?> clazz = typeReference.getClazz();
        ConversionStrategy<?> conversionStrategy = getConversionStrategy(clazz);

        try {
            if (ClassTypeUtil.isTargetClass(Collection.class,clazz)) {
                if (isMultiDimensionalCollection(typeReference)) {
                    return handleDefaultType(sourceObj, clazz, conversionStrategy);
                } else {
                    return handleCollectionType(sourceObj, typeReference, clazz, conversionStrategy);
                }
            } else if (ClassTypeUtil.isTargetClass(Map.class,clazz)) {
                return handleMapType(sourceObj, typeReference, clazz, conversionStrategy);
            } else {
                return handleDefaultType(sourceObj, clazz, conversionStrategy);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T handleCollectionType(Object sourceObj, TypeReference<T> typeReference, Class<?> clazz, ConversionStrategy<?> conversionStrategy) throws IllegalAccessException {
        if (conversionStrategy == null) {
            Type genericTypeClass = extractGenericType(typeReference, 0);
            return (T) convertCollection(genericTypeClass, sourceObj, createCollectionInstance(clazz));
        }
        return (T) conversionStrategy.convertValue(sourceObj, clazz, this);
    }

    @SuppressWarnings("unchecked")
    private <T> T handleMapType(Object sourceObj, TypeReference<T> typeReference, Class<?> clazz, ConversionStrategy<?> conversionStrategy) throws IllegalAccessException {
        if (conversionStrategy == null) {
            Type genericTypeClass = extractGenericType(typeReference, 1);
            return (T) convertMap(genericTypeClass, sourceObj, createMapInstance(clazz));
        }
        return (T) conversionStrategy.convertValue(sourceObj, clazz, this);
    }

    @SuppressWarnings("unchecked")
    private <T> T handleDefaultType(Object sourceObj, Class<?> clazz, ConversionStrategy<?> conversionStrategy) throws IllegalAccessException {
        if (conversionStrategy == null) {
            conversionStrategy = ConversionCache.getConversionStrategy(Object.class);
        }
        return (T) conversionStrategy.convertValue(sourceObj, clazz, this);
    }

    private Type extractGenericType(TypeReference<?> typeReference, int index) {
        Type referenceType = typeReference.getType();
        Type type = genericTypeCache.get(referenceType);
        if (type == null) {
            if (referenceType instanceof ParameterizedType) {
                type = getGenericTypeClass((ParameterizedType) referenceType, index);
            } else {
                type = Object.class;
            }
            genericTypeCache.put(referenceType,type);
        }
        return type;
    }

    /**
     * 获取type的泛型
     * @author anwen
     */
    public static Type getGenericTypeClass(ParameterizedType parameterizedType,int size){
        return parameterizedType.getActualTypeArguments()[size];
    }

    /**
     * 集合单独处理
     * @author anwen
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<?> convertCollection(Type type, Object fieldValue, Collection collection) {
        if (fieldValue == null){
            return collection;
        }
        // 如果fieldValue不是Collection类型，则将其转换为单元素的ArrayList
        if (!(fieldValue instanceof Collection<?>)) {
            Object finalFieldValue = fieldValue;
            fieldValue = new ArrayList<Object>() {{
                add(finalFieldValue);
            }};
        }
        //获取Type的Class
        Class<?> metaClass = getRawClass(type);
        // 处理集合元素
        List valueList = (ArrayList) fieldValue;
        if (simpleTypeHolder.isSimpleType(metaClass)) {
            // 如果泛型类型是简单类型，则直接添加到集合中
            valueList.forEach(value -> collection.add(convertValue(value, metaClass)));
        } else if (ClassTypeUtil.isTargetClass(Collection.class,metaClass)) {
            // 如果泛型类型是集合类型，则递归处理
            // 获取集合的泛型类型
            Type collectionType = getGenericTypeClass((ParameterizedType) type, 0);
            Collection<?> collectionInstance = createCollectionInstance(metaClass);
            valueList.forEach(value -> convertCollection(collectionType, value, collectionInstance));
            collection.add(collectionInstance);
        } else if (ClassTypeUtil.isTargetClass(Map.class,metaClass)){
            Type mapType;
            if (type instanceof ParameterizedType){
                mapType = getGenericTypeClass((ParameterizedType) type, 1);
            } else {
                // 如果没有类型，则默认为Object，像Document类，或JSONObject
                mapType = Object.class;
            }
            valueList.forEach(value -> collection.add(convertMap(mapType,value,createMapInstance(metaClass))));
        } else {
            valueList.forEach(value -> collection.add(readInternal((Document) value, metaClass)));
        }
        return collection;
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public <V> Map<String,V> convertMap(Type type, Object fieldValue, Map map){
        if(fieldValue == null){
            return map;
        }
        Document document = (Document) fieldValue;
        Class<?> rawClass = getRawClass(type);
        if (simpleTypeHolder.isSimpleType(rawClass)){
            document.forEach((k,v)-> map.put(k,convertValue(v,rawClass)));
        } else if (ClassTypeUtil.isTargetClass(Collection.class,rawClass)){
            document.forEach((k,v) -> map.put(k,convertCollection(getGenericTypeClass((ParameterizedType) type, 0),v,createCollectionInstance(rawClass))));
        } else if (ClassTypeUtil.isTargetClass(Map.class,rawClass)){
            Type mapType;
            if (type instanceof ParameterizedType){
                mapType = getGenericTypeClass((ParameterizedType) type, 1);
            } else {
                // 如果没有类型，则默认为Object，像Document类，或JSONObject
                mapType = Object.class;
            }
            document.forEach((k,v) -> map.put(k,convertMap(mapType,v,createMapInstance(rawClass))));
        } else {
            document.forEach((k,v) -> map.put(k,readInternal((Document) v, rawClass)));
        }
        return map;
    }

    private Class<?> getRawClass(Type type) {
        return typeClassCache.computeIfAbsent(type, this::computeRawClass);
    }

    private Class<?> computeRawClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else {
            throw new RuntimeException("Unknown type: " + type);
        }
    }

    /**
     * 创建指定类型的集合实例
     */
    @SuppressWarnings("rawtypes")
    private Collection<?> createCollectionInstance(Class<?> collectionClass) {
        Collection collection;
        if (collectionClass.isInterface()){
            collection = new ArrayList();
        }else {
            collection = (Collection) ClassTypeUtil.getInstanceByClass(collectionClass);
        }
        return collection;
    }

    @SuppressWarnings("rawtypes")
    public Map createMapInstance(Class<?> mapClass){
        Map map;
        if (mapClass.isInterface()){
            map = new HashMap();
        } else {
            map = (Map) ClassTypeUtil.getInstanceByClass(mapClass);
        }
        return map;
    }


    /**
     * 是否是多位数组
     */
    private boolean isMultiDimensionalCollection(TypeReference<?> typeRef) {
        // 1. 检查是否为参数化类型（带泛型）
        if (!(typeRef.getType() instanceof ParameterizedType)) {
            return false; // 非泛型类型，直接返回false
        }
        ParameterizedType paramType = (ParameterizedType) typeRef.getType();
        Type[] genericTypes = paramType.getActualTypeArguments();
        // 2. 检查每个泛型参数
        for (Type genericType : genericTypes) {
            if (genericType instanceof Class) {
                // 如果泛型参数是集合类
                if (Collection.class.isAssignableFrom((Class<?>) genericType)) {
                    return true;
                }
            } else if (genericType instanceof ParameterizedType) {
                ParameterizedType nestedType = (ParameterizedType) genericType;
                Type rawType = nestedType.getRawType();
                // 3. 如果泛型参数本身是集合类型
                if (rawType instanceof Class && Collection.class.isAssignableFrom((Class<?>) rawType)) {
                    return true;
                }
            }
        }
        return false;
    }

}
