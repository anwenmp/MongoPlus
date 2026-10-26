package com.mongoplus.parser.index;

import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.MongoTextIndex;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;

import java.util.function.Function;

import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;

/**
 * 文本索引解析器
 *
 * @author anwen
 */
public class TextParser extends AbstractParser {
    @Override
    public Function<TypeInformation, Boolean> activate() {
        return type -> type.getAnnotation(MongoTextIndex.class) != null;
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
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
        indexMetaObject.getIndexModels().add(new IndexModel(
                indexDocument,
                indexOptions
        ));
    }
}
