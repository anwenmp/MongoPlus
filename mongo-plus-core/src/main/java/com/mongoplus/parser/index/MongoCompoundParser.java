package com.mongoplus.parser.index;

import com.mongodb.client.model.IndexModel;
import com.mongoplus.annotation.index.MongoCompoundIndex;
import com.mongoplus.annotation.index.MongoCompoundIndexes;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.Document;

import java.util.List;
import java.util.function.Function;

import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;

/**
 * 复合索引解析器
 * @author anwen
 */
public class MongoCompoundParser extends AbstractParser {

    @Override
    public Function<TypeInformation, Boolean> activate() {
        return type ->
                type.getAnnotation(MongoCompoundIndex.class) != null ||
                        type.getAnnotation(MongoCompoundIndexes.class) != null;
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
        MongoCompoundIndexes mongoCompoundIndexes;
        if ((mongoCompoundIndexes = typeInformation.getAnnotation(MongoCompoundIndexes.class)) != null) {
            MongoCompoundIndex[] mongoCompoundIndices = mongoCompoundIndexes.value();
            for (MongoCompoundIndex mongoCompoundIndex : mongoCompoundIndices) {
                compoundIndex(typeInformation, indexMetaObject.getIndexModels(), mongoCompoundIndex);
            }
        }
        MongoCompoundIndex mongoCompoundIndex;
        if ((mongoCompoundIndex = typeInformation.getAnnotation(MongoCompoundIndex.class)) != null) {
            compoundIndex(typeInformation, indexMetaObject.getIndexModels(), mongoCompoundIndex);
        }
    }

    public void compoundIndex(TypeInformation typeInformation, List<IndexModel> indexModelList, MongoCompoundIndex mongoCompoundIndex) {
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

}
