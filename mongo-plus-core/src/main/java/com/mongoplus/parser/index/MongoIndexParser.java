package com.mongoplus.parser.index;

import com.mongodb.client.model.IndexModel;
import com.mongoplus.annotation.index.MongoIndex;
import com.mongoplus.enums.IndexType;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 普通索引解析器
 *
 * @author anwen
 */
public class MongoIndexParser extends AbstractParser {

    @Override
    public Function<TypeInformation, Boolean> activate() {
        return type -> CollUtil.isNotEmpty(type.getAnnotationFields(MongoIndex.class));
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
        List<FieldInformation> mongoIndexList = typeInformation.getAnnotationFields(MongoIndex.class);
        if (CollUtil.isNotEmpty(mongoIndexList)) {
            mongoIndexList.forEach(fieldInformation -> {
                MongoIndex mongoIndex = fieldInformation.getAnnotation(MongoIndex.class);
                indexMetaObject.getIndexModels().add(new IndexModel(
                        new Document(
                                fieldInformation.getCamelCaseName(),
                                mongoIndex.direction().getValue()
                        ),
                        getIndexOptions(fieldInformation)
                ));
            });
        }
    }
}
