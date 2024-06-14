package com.anwen.mongo.mapping;

import com.anwen.mongo.annotation.collection.CollectionField;
import com.anwen.mongo.cache.global.ConversionCache;
import com.anwen.mongo.cache.global.HandlerCache;
import com.anwen.mongo.cache.global.PropertyCache;
import com.anwen.mongo.domain.MongoPlusConvertException;
import com.anwen.mongo.domain.MongoPlusWriteException;
import com.anwen.mongo.handlers.TypeHandler;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.strategy.conversion.ConversionStrategy;
import com.anwen.mongo.strategy.mapping.MappingStrategy;
import com.anwen.mongo.toolkit.BsonUtil;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 将对象映射为Document
 *
 * @author JiaChaoYang
 **/
public class MappingMongoConverter extends AbstractMongoConverter {

    private final Log log = LogFactory.getLog(MappingMongoConverter.class);

    private final SimpleTypeHolder simpleTypeHolder;

    public MappingMongoConverter(MongoPlusClient mongoPlusClient,SimpleTypeHolder simpleTypeHolder) {
        super(mongoPlusClient);
        this.simpleTypeHolder = simpleTypeHolder;
    }

    @Override
    public void write(Object sourceObj, Bson bson, TypeInformation typeInformation) {
        processFields(typeInformation.getFields(), bson, true);
    }

    /**
     * 写入内部对象
     * @param sourceObj 源对象
     * @param bson bson
     * @return {@link Bson}
     */
    public Bson writeInternal(Object sourceObj, Bson bson) {
        processFields(TypeInformation.of(sourceObj).getFields(), bson, false);
        return bson;
    }

    /**
     * 处理字段信息并写入 BSON
     * @param fields 字段信息列表
     * @param bson BSON 对象
     * @param filterId 是否过滤掉 ID 字段
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void processFields(List<FieldInformation> fields, Bson bson, boolean filterId) {
        fields.stream()
                .filter(fieldInformation -> !fieldInformation.isSkipCheckField() && (!filterId || !fieldInformation.isId()))
                .forEach(fieldInformation -> {
                    CollectionField collectionField = fieldInformation.getCollectionField();
                    Object obj = null;
                    if (collectionField != null && TypeHandler.class.isAssignableFrom(collectionField.typeHandler())) {
                        try {
                            TypeHandler typeHandler = (TypeHandler) collectionField.typeHandler().getDeclaredConstructor().newInstance();
                            obj = typeHandler.setParameter(fieldInformation.getName(),fieldInformation.getValue());
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            log.error("Failed to create TypeHandler, message: {}", e.getMessage(), e);
                            throw new MongoPlusWriteException("Failed to create TypeHandler, message: " + e.getMessage());
                        }
                    }
                    //如果类型处理器返回null，则继续走默认处理
                    if (obj != null) {
                        BsonUtil.addToMap(bson, fieldInformation.getName(), obj);
                    } else {
                        writeProperties(bson, fieldInformation.getName(), fieldInformation.getValue());
                    }
                });
    }


    /**
     * 属性写入Bson中
     * @param bson bson
     * @param key key
     * @param sourceObj 源对象
     * @author anwen
     * @date 2024/5/1 下午11:45
     */
    private void writeProperties(Bson bson,String key,Object sourceObj){
        if (PropertyCache.ignoringNull && sourceObj == null){
            return;
        }
        BsonUtil.addToMap(bson, key, writeProperties(sourceObj));
    }

    /**
     * 属性映射
     * @param sourceObj 源对象
     * @return {@link Object}
     * @author anwen
     * @date 2024/5/1 下午11:46
     */
    private Object writeProperties(Object sourceObj){
        Object resultObj;
        MappingStrategy<Object> mappingStrategy = null;
        if (sourceObj != null) {
            mappingStrategy = getMappingStrategy(sourceObj.getClass());
        }
        if (mappingStrategy != null){
            try {
                resultObj = mappingStrategy.mapping(sourceObj);
            } catch (IllegalAccessException e) {
                String error = String.format("Exception mapping %s to simple type", sourceObj.getClass().getName());
                log.error(error,e);
                throw new MongoPlusWriteException(error);
            }
        } else if (sourceObj == null || simpleTypeHolder.isSimpleType(sourceObj.getClass())) {
            resultObj = getPotentiallyConvertedSimpleWrite(sourceObj);
        } else if (Collection.class.isAssignableFrom(sourceObj.getClass()) || sourceObj.getClass().isArray()) {
            resultObj = writeCollectionInternal(BsonUtil.asCollection(sourceObj), new ArrayList<>());
        } else if (Map.class.isAssignableFrom(sourceObj.getClass())) {
            resultObj = writeMapInternal((Map<?, ?>) sourceObj,new Document());
        } else {
            resultObj = writeInternal(sourceObj,new Document());
        }
        return resultObj;
    }

    /**
     * map类型的处理
     * @param obj 源对象
     * @param bson bson
     * @return {@link Bson}
     * @author anwen
     * @date 2024/5/1 下午11:46
     */
    private Bson writeMapInternal(Map<?,?> obj,Bson bson){
        //循环map
        obj.forEach((k,v)->{
            //如果key是简单类型
            if (simpleTypeHolder.isSimpleType(k.getClass())){
                writeProperties(bson,String.valueOf(k),v);
            }else {
                throw new MongoPlusWriteException("Cannot use a complex object as a key value");
            }
        });
        return bson;
    }

    /**
     * 集合类型的处理
     * @param obj 源对象
     * @param sink 集合
     * @return {@link Collection<?>}
     * @author anwen
     * @date 2024/5/1 下午11:46
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
        //经过一下Document处理器
        if (HandlerCache.documentHandler != null && bson instanceof Document){
            HandlerCache.documentHandler.insertInvoke(Collections.singletonList((Document) bson));
        }
    }

    @Override
    public <T> T read(Object sourceObj, TypeReference<T> typeReference) {
        Class<?> clazz = typeReference.getClazz();
        ConversionStrategy<?> conversionStrategy = getConversionStrategy(clazz);

        try {
            if (Collection.class.isAssignableFrom(clazz)) {
                return handleCollectionType(sourceObj, typeReference, clazz, conversionStrategy);
            } else if (Map.class.isAssignableFrom(clazz)) {
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
        if (typeReference.getType() instanceof ParameterizedType) {
            return getGenericTypeClass((ParameterizedType) typeReference.getType(), index);
        }
        return Object.class;
    }

    /**
     * 集合单独处理
     * @author anwen
     * @date 2024/5/6 下午1:14
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
        } else if (Collection.class.isAssignableFrom(metaClass)) {
            // 如果泛型类型是集合类型，则递归处理
            // 获取集合的泛型类型
            Type collectionType = getGenericTypeClass((ParameterizedType) type, 0);
            Collection<?> collectionInstance = createCollectionInstance(metaClass);
            convertCollection(collectionType, fieldValue, collectionInstance);
            collection.add(collectionInstance);
        } else if (Map.class.isAssignableFrom(metaClass)){
            valueList.forEach(value -> collection.add(convertMap(getGenericTypeClass((ParameterizedType) type, 1),value,createMapInstance(metaClass))));
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
        } else if (Collection.class.isAssignableFrom(rawClass)){
            document.forEach((k,v) -> map.put(k,convertCollection(getGenericTypeClass((ParameterizedType) type, 0),v,createCollectionInstance(rawClass))));
        } else if (Map.class.isAssignableFrom(rawClass)){
            document.forEach((k,v) -> map.put(k,convertMap(getGenericTypeClass((ParameterizedType) type, 1),v,createMapInstance(rawClass))));
        } else {
            document.forEach((k,v) -> map.put(k,readInternal((Document) v, rawClass)));
        }
        return map;
    }

    /**
     * 获取泛型的原始类
     */
    private Class<?> getRawClass(Type type) {
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
        try {
            if (collectionClass.isInterface()){
                collection = new ArrayList();
            }else {
                collection = (Collection) collectionClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new MongoPlusConvertException("Failed to create Collection instance",e);
        }
        return collection;
    }

    @SuppressWarnings("rawtypes")
    public Map createMapInstance(Class<?> mapClass){
        Map map;
        try {
            if (mapClass.isInterface()){
                map = new HashMap();
            } else {
                map = (Map) mapClass.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new MongoPlusConvertException("Failed to create Map instance",e);
        }
        return map;
    }

    /**
     * 获取type的泛型
     * @author anwen
     * @date 2024/5/6 下午9:19
     */
    public static Type getGenericTypeClass(ParameterizedType parameterizedType,int size){
        return parameterizedType.getActualTypeArguments()[size];
    }

}
