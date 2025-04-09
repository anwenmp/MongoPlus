package com.mongoplus.handlers;

import com.mongodb.MongoNamespace;
import com.mongoplus.enums.ExecuteMethodEnum;

/**
 * 动态集合处理器
 * @author anwen
 * @since by mybatis-plus
 */
public interface CollectionNameHandler {

    /**
     * 生成动态集合
     * @param executeMethodEnum 当前的操作
     * @param source 当前操作的参数
     * @param namespace 命名空间
     * @return {@link java.lang.String}
     * @author anwen
     */
    String dynamicCollectionName(ExecuteMethodEnum executeMethodEnum,Object[] source, MongoNamespace namespace);

}
