package com.mongoplus.mapping;

import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.cache.global.MappingCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusWriteException;
import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.handlers.TypeHandler;
import com.mongoplus.handlers.auto.AutoFillHandler;
import com.mongoplus.handlers.auto.DefaultAutoFillHandler;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.toolkit.BsonUtil;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 抽象地映射处理器
 *
 * @author JiaChaoYang
 */
public abstract class AbstractMongoConverter implements MongoConverter {

    private final Log log = LogFactory.getLog(AbstractMongoConverter.class);

    private final AutoFillHandler autoFillHandler;

    public AbstractMongoConverter() {
        this.autoFillHandler = new DefaultAutoFillHandler();
    }

    @Override
    public void writeBySave(Object sourceObj, Document document) {
        // Map类型不需要再做下边的操作 因为它们只针对实体类
        if (ClassTypeUtil.isTargetClass(Map.class, sourceObj.getClass())) {
            write((Map<?, ?>) sourceObj, document);
            return;
        }
        //封装class信息
        TypeInformation typeInformation = TypeInformation.of(sourceObj);
        //拿到类中的@ID字段
        FieldInformation idFieldInformation = typeInformation.getAnnotationField(ID.class);
        if (idFieldInformation != null) {
            // 获取id值
            Object idValue = idFieldInformation.getValue();
            // 如果自定设置了id
            if (idValue != null) {
                // 如果自设置的值是ObjectId，并且传入的值不是ObjectId类型，则创建并写入
                if (ObjectId.isValid(String.valueOf(idValue)) && !idValue.getClass().equals(ObjectId.class)) {
                    idValue = new ObjectId(String.valueOf(idValue));
                }
            } else {
                // 没有自行设置id，则自动生成id
                idValue = HandlerCache.idGenerateHandler.generateId(idFieldInformation.getId().type(), typeInformation);
                // 没有生成id抛出异常
                if (idValue == null) {
                    throw new MongoPlusWriteException("The _id cannot be empty, please check the IdGenerateHandler or manually assign it");
                }
                // 如果值不是ObjectId
                if (!(idValue instanceof ObjectId)) {
                    // 如果开启了objectId转换类型，并且传入的值是ObjectId格式，则转换为ObjectId类型
                    if (PropertyCache.objectIdConvertType && ObjectId.isValid(String.valueOf(idValue))) {
                        idValue = new ObjectId(String.valueOf(idValue));
                    } else {
                        // 为满足ObjectId条件，转换为实体类字段类型
                        idValue = convertValue(idValue, idFieldInformation.getTypeClass());
                    }
                }
            }
            document.put(SqlOperationConstant._ID, idValue);
            //为自行设置id，需要在这里判断一下重入，自行设置checkTableField方法会进行处理
            if (idFieldInformation.getId().saveField()) {
                document.put(idFieldInformation.getName(), idValue);
            }
        }
        //映射到Document
        write(sourceObj, document);
        //添加自动填充字段
        autoFillHandler.handle(document,typeInformation,FieldFill.INSERT);
    }

    @Override
    public void writeByUpdate(Object sourceObj, Document document) {
        // Map类型不需要再做下边的操作 因为它们只针对实体类
        if (ClassTypeUtil.isTargetClass(Map.class, sourceObj.getClass())) {
            write((Map<?, ?>) sourceObj, document);
            return;
        }
        //封装class信息
        TypeInformation typeInformation = TypeInformation.of(sourceObj);
        //拿到类中的@ID字段
        FieldInformation idFieldInformation = typeInformation.getAnnotationField(ID.class, "@ID field not found");
        if (idFieldInformation.getValue() != null) {
            document.put(SqlOperationConstant._ID, idFieldInformation.getValue());
        }
        //映射到Document
        write(sourceObj, document);
        //添加自动填充字段
        autoFillHandler.handle(document,typeInformation,FieldFill.UPDATE);
    }

    @Override
    public void write(Object sourceObj, Bson bson) {
        if (null == sourceObj) {
            return;
        }
        //如果为空，则创建一个
        bson = bson != null ? bson : new Document();
        if (ClassTypeUtil.isTargetClass(Map.class, sourceObj.getClass())) {
            write((Map<?, ?>) sourceObj, bson);
        } else {
            write(sourceObj, bson, TypeInformation.of(sourceObj));
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> T readInternal(Document document, TypeReference<T> typeReference, boolean useIdAsFieldName) {
        Class<?> clazz = typeReference.getClazz();
        if (document == null) {
            return null;
        }
        if (ClassTypeUtil.isTargetClass(Document.class, clazz)) {
            return (T) document;
        } else if (ClassTypeUtil.isTargetClass(Map.class, clazz)) {
            return (T) readInternal(document, new TypeReference<Map<String, Object>>() {
            });
        } else if (ClassTypeUtil.isTargetClass(Collection.class, clazz)) {
            return (T) readInternal(document, new TypeReference<Collection<Object>>() {
            });
        }
        // 拿到class封装类
        TypeInformation typeInformation = TypeInformation.of(clazz);

        // 循环所有字段
        typeInformation.getFields().forEach(fieldInformation -> {
            String fieldName = useIdAsFieldName ? fieldInformation.getIdOrCamelCaseName() : fieldInformation.getCamelCaseName();
            if (fieldInformation.isSkipCheckField()) {
                return;
            }
            Object obj = document.get(fieldName);
            if (obj == null) {
                return;
            }
            CollectionField collectionField = fieldInformation.getCollectionField();
            Object resultObj = null;
            if (collectionField != null && ClassTypeUtil.isTargetClass(TypeHandler.class, collectionField.typeHandler())) {
                TypeHandler typeHandler = (TypeHandler) ClassTypeUtil.getInstanceByClass(collectionField.typeHandler());
                resultObj = typeHandler.getResult(obj);
            }
            if (CollUtil.isNotEmpty(HandlerCache.getReadHandler())) {
                List<ReadHandler> readHandlerList = HandlerCache.getReadHandler();
                for (ReadHandler readHandler : readHandlerList) {
                    if (readHandler.activate().apply(fieldInformation)) {
                        obj = readHandler.read(fieldInformation, obj,this);
                    }
                    if (readHandler.discontinue().apply(obj)) {
                        return;
                    }
                }
            }
            if (resultObj == null) {
                resultObj = readInternal(obj, TypeReference.of(fieldInformation.getGenericType()));
            }
            fieldInformation.setValue(resultObj);
        });

        return typeInformation.getInstance();
    }

    /**
     * 抽象的映射方法
     *
     * @param sourceObj       映射源对象
     * @param bson            映射对象
     * @param typeInformation 类信息
     * @author anwen
     */
    public abstract void write(Object sourceObj, Bson bson, TypeInformation typeInformation);

    /**
     * 抽象的map写入方法
     *
     * @param obj  map
     * @param bson bson
     * @return {@link org.bson.conversions.Bson}
     * @author anwen
     */
    public abstract Bson writeMapInternal(Map<?, ?> obj, Bson bson);

    /**
     * 将简单类型进行转换
     *
     * @param value 值
     * @return {@link Object}
     * @author anwen
     */
    protected Object getPotentiallyConvertedSimpleWrite(Object value) {

        if (value == null) {
            return null;
        }

        if (CollUtil.isArray(value)) {

            if (value instanceof byte[]) {
                return value;
            }
            return BsonUtil.asCollection(value);
        }

        return ClassTypeUtil.isTargetClass(Enum.class, value.getClass()) ? ((Enum<?>) value).name() : value;
    }

    /**
     * 调用该方法，肯定会走集合和map之外的转换器
     *
     * @param obj   值
     * @param clazz 类型
     * @return {@link T}
     * @author anwen
     */
    @SuppressWarnings("unchecked")
    protected <T> T convertValue(Object obj, Class<?> clazz) {
        ConversionStrategy<?> conversionStrategy = getConversionStrategy(clazz);
        if (conversionStrategy == null) {
            conversionStrategy = ConversionCache.getConversionStrategy(Object.class);
        }
        try {
            return (T) conversionStrategy.convertValue(obj, clazz, this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConversionStrategy<?> getConversionStrategy(Class<?> target) {
/*        if (target.isEnum()) {
            target = Enum.class;
        }

        // 获取并返回转换策略
        return ConversionCache.getConversionStrategy(target);*/

        // 先根据类型去获取
        ConversionStrategy<?> conversionStrategy = ConversionCache.getConversionStrategy(target);
        // 判断有没有获取到
        if (conversionStrategy == null) {
            // 如果没有找到再判断枚举类，这样的话几十种类型都能省略掉这个判断
            if (target.isEnum()) {
                // 假装这个是全局的缓存变量
                conversionStrategy = ConversionCache.enumConversion;
            }
        }
        return conversionStrategy;
    }

    @SuppressWarnings("unchecked")
    protected MappingStrategy<Object> getMappingStrategy(Class<?> target) {
        Class<?> clazz = target;
        if (target.isEnum()){
            clazz = Enum.class;
        }
        return (MappingStrategy<Object>) MappingCache.getMappingStrategy(clazz);
    }

    /**
     * 校验ObjectId
     * @param value 值
     * @return {@link boolean}
     * @author anwen
     */
    public boolean checkObjectId(Object value){
        return !(value instanceof ObjectId) && ObjectId.isValid(value.toString());
    }

}
