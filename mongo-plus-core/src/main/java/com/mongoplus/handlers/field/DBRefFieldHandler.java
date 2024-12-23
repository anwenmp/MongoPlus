package com.mongoplus.handlers.field;

import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.DBRef;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * DBRef字段处理器
 * @author anwen
 */
public class DBRefFieldHandler implements FieldHandler {

    protected Map<Field,Boolean> dbRefFieldCache = new ConcurrentHashMap<>();

    @Override
    public Function<FieldInformation, Boolean> activate() {
        return fieldInformation ->
                dbRefFieldCache.computeIfAbsent(fieldInformation.getField(), k ->
                    fieldInformation.isAnnotation(DBRef.class)
                );
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
        return new com.mongodb.DBRef(database,collectionName,annotationField.getValue(fieldInformation.getValue()));
    }
}
