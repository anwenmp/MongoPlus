package com.mongoplus.mapping;

import org.bson.Document;

/**
 * Mongo对象映射为Bean
 * @author anwen
 */
public interface EntityRead {

    default <T> T read(Document document, Class<T> clazz){
        return read(document,new TypeReference<T>(clazz) {});
    }

    /**
     * 映射
     * @author anwen
     */
    default <T> T read(Object sourceObj,TypeReference<T> typeReference){
        return readInternal((Document) sourceObj,typeReference,true);
    }

    /**
     * 写内部对象
     * @author anwen
     */
    default <T> T readInternal(Document document, Class<T> clazz, boolean useIdAsFieldName){
        return readInternal(document,new TypeReference<T>(clazz) {},useIdAsFieldName);
    }

    /**
     * 写内部对象
     * @author anwen
     */
    <T> T readInternal(Document document, TypeReference<T> typeReference, boolean useIdAsFieldName);

}
