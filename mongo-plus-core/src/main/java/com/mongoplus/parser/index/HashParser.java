package com.mongoplus.parser.index;

import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.MongoHashIndex;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;

import java.util.List;
import java.util.function.Function;

/**
 * 哈希索引解析器
 *
 * @author anwen
 */
public class HashParser extends AbstractParser {
    @Override
    public Function<TypeInformation, Boolean> activate() {
        return type -> CollUtil.isNotEmpty(type.getAnnotationFields(MongoHashIndex.class));
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
        List<FieldInformation> fields = typeInformation.getAnnotationFields(MongoHashIndex.class);
        if (CollUtil.isNotEmpty(fields)) {
            fields.forEach(fieldInformation -> indexMetaObject.getIndexModels().add(new IndexModel(
                    new Document(
                            fieldInformation.getCamelCaseName(),
                            "hashed"
                    ),
                    new IndexOptions()
            )));
        }
    }
}
