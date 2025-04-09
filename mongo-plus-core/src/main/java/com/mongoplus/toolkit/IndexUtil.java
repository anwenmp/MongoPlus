package com.mongoplus.toolkit;

import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.*;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.enums.IndexType;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;

/**
 * 索引工具类
 *
 * @author anwen
 */
public class IndexUtil {

    public static List<IndexMetaObject> getIndex(Collection<? extends Class<?>> collectionEntityList) {
        List<IndexMetaObject> indexList = new ArrayList<>();
        if (CollUtil.isNotEmpty(collectionEntityList)) {
            collectionEntityList.forEach(collectionClass -> {
                IndexMetaObject indexMetaObject = new IndexMetaObject();
                List<IndexModel> indexModelList = new ArrayList<>();
                TypeInformation typeInformation = TypeInformation.of(collectionClass);
                indexMetaObject.setTypeInformation(typeInformation);
                if (collectionClass.isAnnotationPresent(MongoIndexDs.class)){
                    indexMetaObject.setDataSource(collectionClass.getAnnotation(MongoIndexDs.class).dataSource());
                }
                if (collectionClass.isAnnotationPresent(MongoCompoundIndex.class)) {
                    compoundIndex(typeInformation, indexModelList, collectionClass.getAnnotation(MongoCompoundIndex.class));
                    indexMetaObject.setIndexType(IndexType.COMPOUND_INDEX);
                }
                if (collectionClass.isAnnotationPresent(MongoCompoundIndexes.class)) {
                    MongoCompoundIndexes collectionClassAnnotation = collectionClass.getAnnotation(MongoCompoundIndexes.class);
                    MongoCompoundIndex[] mongoCompoundIndices = collectionClassAnnotation.value();
                    for (MongoCompoundIndex mongoCompoundIndex : mongoCompoundIndices) {
                        compoundIndex(typeInformation, indexModelList, mongoCompoundIndex);
                    }
                    indexMetaObject.setIndexType(IndexType.COMPOUND_INDEX);
                }
                if (collectionClass.isAnnotationPresent(MongoTextIndex.class)){
                    textIndex(typeInformation,indexModelList);
                    indexMetaObject.setIndexType(IndexType.TEXT_INDEX);
                }
                List<FieldInformation> mongoIndexList = typeInformation.getAnnotationFields(MongoIndex.class);
                if (CollUtil.isNotEmpty(mongoIndexList)) {
                    mongoIndexList.forEach(fieldInformation -> index(fieldInformation, indexModelList));
                    indexMetaObject.setIndexType(IndexType.INDEX);
                }
                List<FieldInformation> mongoHashIndexList = typeInformation.getAnnotationFields(MongoHashIndex.class);
                if (CollUtil.isNotEmpty(mongoHashIndexList)){
                    mongoHashIndexList.forEach(fieldInformation -> hashIndex(fieldInformation, indexModelList));
                    indexMetaObject.setIndexType(IndexType.HASH_INDEX);
                }
                List<FieldInformation> mongoGeoIndexList = typeInformation.getAnnotationFields(MongoGeoIndex.class);
                if (CollUtil.isNotEmpty(mongoGeoIndexList)) {
                    mongoGeoIndexList.forEach(fieldInformation -> geoIndex(fieldInformation,indexModelList));
                    indexMetaObject.setIndexType(IndexType.GEO_INDEX);
                }
                indexMetaObject.setIndexModels(indexModelList);
                indexList.add(indexMetaObject);
            });
        }
        return indexList;
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

    public static void textIndex(TypeInformation typeInformation,List<IndexModel> indexModelList){
        MongoTextIndex mongoTextIndex  = typeInformation.getClazz().getAnnotation(MongoTextIndex.class);
        IndexOptions indexOptions = new IndexOptions();
        indexOptions.defaultLanguage(mongoTextIndex.language().getLanguage());
        if (mongoTextIndex.textIndexVersion() > -1) {
            indexOptions.textVersion(mongoTextIndex.textIndexVersion());
        }
        if (StringUtils.isNotBlank(mongoTextIndex.name())){
            indexOptions.name(mongoTextIndex.name());
        }
        Document document = new Document();
        for (String field : mongoTextIndex.fields()) {
            document.put(field,"text");
        }
        Document indexDocument = new Document();
        document.forEach((key, value) -> {
            String fieldName = key;
            if (key.startsWith("$") && !key.equals("$**")){
                fieldName = getFieldName(typeInformation, key);
            }
            indexDocument.put(fieldName,value);
        });
        indexModelList.add(new IndexModel(
                indexDocument,
                indexOptions
        ));
    }

    public static void compoundIndex(TypeInformation typeInformation, List<IndexModel> indexModelList, MongoCompoundIndex mongoCompoundIndex) {
        Document document;
        try {
            document = Document.parse(mongoCompoundIndex.value(), MapCodecCache.getDefaultCodec());
        } catch (Exception e) {
            throw new MongoPlusConvertException("The partialFilterExpression is not a valid JSON string", e);
        }
        Document indexDocument = new Document();
        document.forEach((key, value) -> {
            String fieldName = key;
            if (key.startsWith("$")){
                fieldName = getFieldName(typeInformation, key);
            }
            indexDocument.put(fieldName,value);
        });
        indexModelList.add(new IndexModel(
                indexDocument,
                getCompoundIndexOptions(typeInformation, mongoCompoundIndex)
        ));
    }

    public static void geoIndex(FieldInformation fieldInformation,List<IndexModel> indexModelList) {
        MongoGeoIndex geoIndex = fieldInformation.getAnnotation(MongoGeoIndex.class);
        indexModelList.add(new IndexModel(
                new Document(fieldInformation.getCamelCaseName(),geoIndex.type().getType())
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
        if (mongoIndex.expireAfterSeconds() >= 0) {
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
            Document indexDocument = new Document();
            document.forEach((key, value) -> {
                String fieldName = key;
                if (key.startsWith("$")){
                    fieldName = getFieldName(typeInformation, key);
                }
                indexDocument.put(fieldName,value);
            });
            indexOptions.partialFilterExpression(indexDocument);
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

}
