package com.mongoplus.parser.index;

import com.mongoplus.annotation.index.MongoIndexDs;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 调度
 *
 * @author anwen
 */
public class Resolver {

    /**
     * 解析器列表
     */
    private final List<Parser> parsers = Arrays.asList(
            new IndexParser(),
            new CompoundParser(),
            new TextParser(),
            new HashParser(),
            new GeoParser(),
            new SearchParse()
    );

    /**
     * 解析索引
     *
     * @param entityClass 实体类
     * @return 索引元数据
     */
    public IndexMetaObject resolve(Class<?> entityClass) {

        TypeInformation type =
                TypeInformation.of(entityClass);

        IndexMetaObject meta = new IndexMetaObject();
        meta.setTypeInformation(type);
        meta.setIndexModels(new ArrayList<>());
        meta.setSearchIndexModels(new ArrayList<>());
        MongoIndexDs ds =
                entityClass.getAnnotation(MongoIndexDs.class);
        if (ds != null) {
            meta.setDataSource(ds.dataSource());
        }
        parsers.stream().filter(parser -> parser.activate().apply(type))
                .forEach(parser -> parser.parse(type, meta));
        return meta;
    }

}
