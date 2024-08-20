package com.anwen.mongo.toolkit;

import com.anwen.mongo.annotation.collection.CollectionField;
import com.anwen.mongo.annotation.index.MongoCompoundIndex;
import com.anwen.mongo.annotation.index.MongoCompoundIndexes;
import com.anwen.mongo.annotation.index.MongoHashIndex;
import com.anwen.mongo.annotation.index.MongoIndex;
import com.anwen.mongo.cache.codec.MapCodecCache;
import com.anwen.mongo.domain.MongoPlusConvertException;
import com.anwen.mongo.domain.MongoPlusFieldException;
import com.anwen.mongo.mapping.FieldInformation;
import com.anwen.mongo.mapping.TypeInformation;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 索引工具类
 *
 * @author anwen
 */
public class IndexUtil {

    public static Map<Class<?>, List<IndexModel>> getIndex(Set<? extends Class<?>> collectionEntityList) {
        Map<Class<?>, List<IndexModel>> indexModelMap = new ConcurrentHashMap<>();
        if (CollUtil.isNotEmpty(collectionEntityList)) {
            collectionEntityList.forEach(collectionClass -> {
                List<IndexModel> indexModelList = new ArrayList<>();
                TypeInformation typeInformation = TypeInformation.of(collectionClass);
                if (collectionClass.isAnnotationPresent(MongoCompoundIndex.class)) {
                    compoundIndex(typeInformation, indexModelList, collectionClass.getAnnotation(MongoCompoundIndex.class));
                }
                if (collectionClass.isAnnotationPresent(MongoCompoundIndexes.class)) {
                    MongoCompoundIndexes collectionClassAnnotation = collectionClass.getAnnotation(MongoCompoundIndexes.class);
                    MongoCompoundIndex[] mongoCompoundIndices = collectionClassAnnotation.value();
                    for (MongoCompoundIndex mongoCompoundIndex : mongoCompoundIndices) {
                        compoundIndex(typeInformation, indexModelList, mongoCompoundIndex);
                    }
                }
                List<FieldInformation> mongoIndexList = typeInformation.getAnnotationFields(MongoIndex.class);
                if (CollUtil.isNotEmpty(mongoIndexList)) {
                    mongoIndexList.forEach(fieldInformation -> index(fieldInformation, indexModelList));
                }
                List<FieldInformation> mongoHashIndexList = typeInformation.getAnnotationFields(MongoHashIndex.class);
                if (CollUtil.isNotEmpty(mongoHashIndexList)){
                    mongoHashIndexList.forEach(fieldInformation -> hashIndex(fieldInformation, indexModelList));
                }
                indexModelMap.put(collectionClass, indexModelList);
            });
        }
        return indexModelMap;
    }

    public static void index(FieldInformation fieldInformation, List<IndexModel> indexModelList) {
        MongoIndex mongoIndex = fieldInformation.getAnnotation(MongoIndex.class);
        indexModelList.add(new IndexModel(
                new Document(
                        fieldInformation.getCamelCaseName(),
                        mongoIndex.direction().getValue()
                ),
                getIndexOptions(fieldInformation)
        ));
    }

    public static void hashIndex(FieldInformation fieldInformation, List<IndexModel> indexModelList) {
        indexModelList.add(new IndexModel(
                new Document(
                        fieldInformation.getCamelCaseName(),
                        "hashed"
                ),
                new IndexOptions()
        ));
    }

    public static void compoundIndex(TypeInformation typeInformation, List<IndexModel> indexModelList, MongoCompoundIndex mongoCompoundIndex) {
        Document document;
        try {
            document = Document.parse(mongoCompoundIndex.value(), MapCodecCache.getDefaultCodec());
        } catch (Exception e) {
            throw new MongoPlusConvertException("The partialFilterExpression is not a valid JSON string", e);
        }
        Map<String, Object> modifiedEntries = document.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("$"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        modifiedEntries.keySet().forEach(document::remove);
        document.putAll(modifiedEntries.entrySet().stream().collect(Collectors.toMap(
                entry -> getFieldName(typeInformation, entry.getKey()),
                Map.Entry::getValue
        )));
        indexModelList.add(new IndexModel(
                document,
                getCompoundIndexOptions(typeInformation, mongoCompoundIndex)
        ));
    }

    public static IndexOptions getIndexOptions(FieldInformation fieldInformation) {
        MongoIndex mongoIndex = fieldInformation.getAnnotation(MongoIndex.class);
        IndexOptions indexOptions = new IndexOptions();
        indexOptions.name(fieldInformation.getCamelCaseName());
        if (StringUtils.isNotBlank(mongoIndex.name())) {
            indexOptions.name(mongoIndex.name());
        }
        if (mongoIndex.unique()) {
            indexOptions.unique(true);
        }
        if (mongoIndex.sparse()) {
            indexOptions.sparse(true);
        }
        if (mongoIndex.background()) {
            indexOptions.background(true);
        }
        if (mongoIndex.expireAfterSeconds() > 0) {
            indexOptions.expireAfter(mongoIndex.expireAfterSeconds(), TimeUnit.SECONDS);
        }
        if (StringUtils.isNotBlank(mongoIndex.expireAfter())) {
            String expireAfter = mongoIndex.expireAfter();
            indexOptions.expireAfter(
                    Long.valueOf(expireAfter.substring(0, expireAfter.length() - 1)),
                    getTimeUnit(expireAfter)
            );
        }
        if (StringUtils.isNotBlank(mongoIndex.partialFilterExpression())) {
            CollectionField collectionField = fieldInformation.getAnnotation(CollectionField.class);
            Document document;
            try {
                document = Document.parse(mongoIndex.partialFilterExpression(), MapCodecCache.getDefaultCodec());
            } catch (Exception e) {
                throw new MongoPlusConvertException("The partialFilterExpression is not a valid JSON string", e);
            }
            indexOptions.partialFilterExpression(new Document(fieldInformation.getCamelCaseName(), document));
        }
        return indexOptions;
    }

    public static IndexOptions getCompoundIndexOptions(TypeInformation typeInformation, MongoCompoundIndex mongoCompoundIndex) {
        Class<?> clazz = typeInformation.getClazz();
        IndexOptions indexOptions = new IndexOptions();
        if (StringUtils.isNotBlank(mongoCompoundIndex.name())) {
            indexOptions.name(mongoCompoundIndex.name());
        }
        if (mongoCompoundIndex.unique()) {
            indexOptions.unique(true);
        }
        if (mongoCompoundIndex.sparse()) {
            indexOptions.sparse(true);
        }
        if (mongoCompoundIndex.background()) {
            indexOptions.background(true);
        }
        if (StringUtils.isNotBlank(mongoCompoundIndex.partialFilterExpression())) {
            String partialFilterExpression = mongoCompoundIndex.partialFilterExpression();
            Document document;
            try {
                document = Document.parse(partialFilterExpression, MapCodecCache.getDefaultCodec());
            } catch (Exception e) {
                throw new MongoPlusConvertException("The partialFilterExpression is not a valid JSON string", e);
            }
            Map<String, Object> modifiedEntries = document.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("$"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            modifiedEntries.keySet().forEach(document::remove);
            document.putAll(modifiedEntries.entrySet().stream().collect(Collectors.toMap(
                    entry -> getFieldName(typeInformation, entry.getKey()),
                    Map.Entry::getValue
            )));
            indexOptions.partialFilterExpression(document);
        }
        return indexOptions;
    }

    public static TimeUnit getTimeUnit(String timeUnit) {
        timeUnit = timeUnit.toLowerCase();
        if (timeUnit.contains("d")) {
            return TimeUnit.DAYS;
        }
        if (timeUnit.contains("h")) {
            return TimeUnit.HOURS;
        }
        if (timeUnit.contains("m")) {
            return TimeUnit.MINUTES;
        }
        if (timeUnit.contains("s")) {
            return TimeUnit.SECONDS;
        }
        throw new MongoPlusFieldException(String.format("Time unit with value %s not found", timeUnit));
    }

    private static String getFieldName(TypeInformation typeInformation, String key) {
        key = key.substring(1);
        FieldInformation fieldInformation = typeInformation.getFieldNotException(key);
        if (fieldInformation != null) {
            key = fieldInformation.getCamelCaseName();
        }
        return key;
    }

}
