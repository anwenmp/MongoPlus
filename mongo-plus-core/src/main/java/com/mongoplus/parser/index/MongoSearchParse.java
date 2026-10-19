package com.mongoplus.parser.index;

import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;

import java.util.function.Function;

/**
 * 搜索索引
 *
 * @author anwen
 */
public class MongoSearchParse extends AbstractParser {
    @Override
    public Function<TypeInformation, Boolean> activate() {
        return null;
    }

    @Override
    public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject) {

    }
}
