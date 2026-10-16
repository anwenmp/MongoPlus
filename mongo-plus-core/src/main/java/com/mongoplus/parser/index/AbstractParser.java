package com.mongoplus.parser.index;

import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.MongoCompoundIndex;
import com.mongoplus.annotation.index.MongoIndex;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;

/**
 * 抽象的解析器
 *
 * @author anwen
 */
public abstract class AbstractParser implements Parser {

    protected IndexOptions getIndexOptions(FieldInformation fieldInformation) {
        MongoIndex mongoIndex = fieldInformation.getAnnotation(MongoIndex.class);
        IndexOptions indexOptions = baseOptions(mongoIndex.name(),
                mongoIndex.unique(),
                mongoIndex.sparse(),
                mongoIndex.background());
        indexOptions.name(fieldInformation.getCamelCaseName());
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

    private IndexOptions baseOptions(String name, boolean unique, boolean sparse, boolean background) {
        IndexOptions indexOptions = new IndexOptions();
        if (StringUtils.isNotBlank(name)) {
            indexOptions.name(name);
        }
        if (unique) {
            indexOptions.unique(true);
        }
        if (sparse) {
            indexOptions.sparse(true);
        }
        if (background) {
            indexOptions.background(true);
        }
        return indexOptions;
    }

    protected IndexOptions getCompoundIndexOptions(TypeInformation typeInformation, MongoCompoundIndex mongoCompoundIndex) {
        IndexOptions indexOptions = baseOptions(mongoCompoundIndex.name(),
                mongoCompoundIndex.unique(),
                mongoCompoundIndex.sparse(),
                mongoCompoundIndex.background());
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

    protected TimeUnit getTimeUnit(String timeUnit) {
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
