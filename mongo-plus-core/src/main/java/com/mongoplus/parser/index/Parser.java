package com.mongoplus.parser.index;

import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;

import java.util.function.Function;

/**
 * 索引解析器
 *
 * @author anwen
 */
public interface Parser {

    /**
     * 激活解析器
     * @return 激活函数
     */
    Function<TypeInformation,Boolean> activate();

    /**
     * 解析器接口
     *
     * @param typeInformation   类信息
     * @param indexMetaObject 索引元对象
     */
    void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);

}
