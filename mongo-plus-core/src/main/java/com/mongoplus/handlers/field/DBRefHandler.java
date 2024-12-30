package com.mongoplus.handlers.field;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.DBRef;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.toolkit.Filters;
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
public class DBRefHandler implements FieldHandler, ReadHandler {

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
        Class<?> typeClass = fieldInformation.getTypeClass();
        DBRef dbRefAnnotation = fieldInformation.getAnnotation(DBRef.class);
        TypeInformation typeInformation = TypeInformation.of(typeClass);
        FieldInformation dbRefFieldInformation = typeInformation.getAnnotationField(CollectionName.class);
        String collectionName = AnnotationOperate.getCollectionName(typeClass);
        String database = AnnotationOperate.getDatabase(typeClass);
        if (database == null) {
            database = dbRefAnnotation.db();
        }
        FieldInformation annotationField = typeInformation.getAnnotationField(ID.class);
        if (annotationField == null) {
            throw new MongoPlusFieldException("@ID is null");
        }
        Object value = annotationField.getValue(fieldInformation.getValue());
        String valueStr;
        if ((dbRefAnnotation.autoConvertObjectId() || PropertyCache.autoConvertObjectId)
                && ObjectId.isValid((valueStr = value.toString()))) {
            value = new ObjectId(valueStr);
        }
        return new com.mongodb.DBRef(database, collectionName, value);
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

}
