package com.mongoplus.parser.index;

import com.mongodb.client.model.IndexModel;
import com.mongoplus.annotation.index.MongoGeoIndex;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;

import java.util.List;
import java.util.function.Function;

/**
 * 地理位置索引解析器
 *
 * @author anwen
 */
public class GeoParser extends AbstractParser {
    @Override
    public Function<TypeInformation, Boolean> activate() {
        return type -> CollUtil.isNotEmpty(type.getAnnotationFields(MongoGeoIndex.class));
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {
        List<FieldInformation> fields = typeInformation.getAnnotationFields(MongoGeoIndex.class);
        if (CollUtil.isNotEmpty(fields)) {
            fields.forEach(fieldInformation -> {
                MongoGeoIndex geoIndex = fieldInformation.getAnnotation(MongoGeoIndex.class);
                indexMetaObject.getIndexModels().add(new IndexModel(
                        new Document(fieldInformation.getCamelCaseName(), geoIndex.type().getType())
                ));
            });
        }
    }
}
