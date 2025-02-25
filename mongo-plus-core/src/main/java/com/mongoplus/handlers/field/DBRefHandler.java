package com.mongoplus.handlers.field;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.DBRef;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.conditions.interfaces.condition.CompareCondition;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.SimpleFieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.toolkit.Filters;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * DBRef字段处理器
 *
 * @author anwen
 */
public class DBRefHandler implements FieldHandler, ReadHandler, ConditionHandler {

    protected Map<Field, Boolean> dbRefFieldCache = new ConcurrentHashMap<>();

    protected boolean dbRefValueIsNull = false;

    @Override
    public Function<FieldInformation, Boolean> activate() {
        return fieldInformation ->
                dbRefFieldCache.computeIfAbsent(fieldInformation.getField(), k ->
                        fieldInformation.isAnnotation(DBRef.class)
                );
    }

    @Override
    public Function<Object,Boolean> discontinue() {
        return Objects::isNull;
    }

    @Override
    public Object handler(FieldInformation fieldInformation) {
        Object fieldValue = fieldInformation.getValue();
        if (fieldValue == null) {
            return null;
        }
        Class<?> typeClass = fieldInformation.getTypeClass();
        DBRef dbRefAnnotation = fieldInformation.getAnnotation(DBRef.class);
        TypeInformation typeInformation = TypeInformation.of(typeClass);
        FieldInformation annotationField = typeInformation.getAnnotationField(ID.class);
        if (annotationField == null) {
            throw new MongoPlusFieldException("@ID is null");
        }
        Object value = annotationField.getValue(fieldValue);
        return getDBRef(typeClass,dbRefAnnotation,value);
    }

    @Override
    public Object read(FieldInformation fieldInformation, Object source, MongoConverter mongoConverter) {
        MongoPlusClient mongoPlusClient = MongoPlusClientCache.mongoPlusClient;
        com.mongodb.DBRef dbRef = (com.mongodb.DBRef) source;
        MongoCollection<Document> collection = mongoPlusClient.getCollection(
                dbRef.getDatabaseName(),
                dbRef.getCollectionName()
        );
        Class<?> typeClass = fieldInformation.getTypeClass();
        Execute execute = new ExecutorFactory().getExecute();
        MongoIterable<Document> iterable = execute.executeQuery(
                Filters.eq(dbRef.getId()),
                null,
                null,
                Document.class,
                collection
        );
        return mongoConverter.readDocument(iterable, typeClass);
    }

    @Override
    public void beforeQueryCondition(CompareCondition compareCondition, BasicDBObject basicDBObject) {
        Field originalField = compareCondition.getOriginalField();
        if (originalField != null) {
            FieldInformation fieldInformation = new SimpleFieldInformation<>(null, originalField);
            Boolean existDBRef = dbRefFieldCache
                    .computeIfAbsent(originalField,field -> fieldInformation.isAnnotation(DBRef.class));
            if (!existDBRef){
                return;
            }
            com.mongodb.DBRef dbRef = getDBRef(
                    originalField.getType(),
                    fieldInformation.getAnnotation(DBRef.class),
                    compareCondition.getValue()
            );
            basicDBObject.put(compareCondition.getColumn(),dbRef);
        }
    }

    public com.mongodb.DBRef getDBRef(Class<?> typeClass,DBRef dbRefAnnotation,Object value) {
        TypeInformation typeInformation = TypeInformation.of(typeClass);
        FieldInformation dbRefFieldInformation = typeInformation.getAnnotationField(CollectionName.class);
        String collectionName = AnnotationOperate.getCollectionName(typeClass);
        String database = AnnotationOperate.getDatabase(typeClass);
        if (database == null) {
            database = dbRefAnnotation.db();
        }
        database = StringUtils.isBlank(database) ? null : database;
        FieldInformation annotationField = typeInformation.getAnnotationField(ID.class);
        if (annotationField == null) {
            throw new MongoPlusFieldException("@ID is null");
        }
        String valueStr;
        if ((dbRefAnnotation.autoConvertObjectId() || PropertyCache.autoConvertObjectId)
                && ObjectId.isValid((valueStr = value.toString()))) {
            value = new ObjectId(valueStr);
        }
        return new com.mongodb.DBRef(database, collectionName, value);
    }

}
