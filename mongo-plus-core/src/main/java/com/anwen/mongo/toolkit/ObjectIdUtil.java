package com.anwen.mongo.toolkit;

import com.anwen.mongo.cache.global.PropertyCache;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class ObjectIdUtil {

    /**
     * 转换单个ObjectId
     */
    public static <T> Object convertObjectId(T id) {
        if (id == null) {
            return null;
        }
        String strId = String.valueOf(id);
        if (ObjectId.isValid(strId)) {
            return new ObjectId(strId);
        }
        return id;
    }

    /**
     * 批量转换ObjectId
     */
    public static Collection<Object> convertObjectId(Collection<?> ids) {
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().map(ObjectIdUtil::convertObjectId).collect(Collectors.toList());
    }

    /**
     * 转换ObjectId，如果value类型为ObjectId，则直接返回
     * @param value 值
     * @return {@link Object}
     * @author anwen
     * @date 2024/8/8 下午3:29
     */
    public static Object getObjectIdValue(Object value) {
        if (value instanceof ObjectId){
            return value;
        }
        String convertValue = String.valueOf(value);
        return ObjectId.isValid(convertValue) && PropertyCache.autoConvertObjectId ? new ObjectId(convertValue) : value;
    }

}
