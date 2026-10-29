package com.mongoplus.parser.index;

import com.mongoplus.annotation.index.MongoIndexDs;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 实体索引注解的统一解析入口，按激活条件调度普通索引与 Search 解析器。
 * <p>每次解析创建独立元数据，只生成 Driver 模型，不执行数据库操作。</p>
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
     * 解析一个实体的全部适用索引，普通模型与 Search 模型分别保存。
     *
     * @param entityClass 实体类
     * @return 本次独立创建的索引元数据，任一解析器失败时不返回部分结果
     */
    public IndexMetaObject resolve(Class<?> entityClass) {

        TypeInformation type =
                TypeInformation.of(entityClass);

        IndexMetaObject meta = new IndexMetaObject();
        meta.setTypeInformation(type);
        // 两种模型对应不同的 Driver DDL 接口，不能混入同一个创建列表。
        meta.setIndexModels(new ArrayList<>());
        meta.setSearchIndexModels(new ArrayList<>());
        MongoIndexDs ds =
                entityClass.getAnnotation(MongoIndexDs.class);
        if (ds != null) {
            meta.setDataSource(ds.dataSource());
        }
        // 解析器只能消费自身注解，未激活时不能调用 parse，避免读取不存在的注解或生成额外索引。
        parsers.stream().filter(parser -> parser.activate().apply(type))
                .forEach(parser -> parser.parse(type, meta));
        return meta;
    }

}
