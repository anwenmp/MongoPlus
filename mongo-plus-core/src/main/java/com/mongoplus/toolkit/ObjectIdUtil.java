package com.mongoplus.toolkit;

import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class ObjectIdUtil {

    private static Log log = LogFactory.getLog(ObjectIdUtil.class);

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
     */
    public static Object getObjectIdValue(Object value) {
        if (value instanceof Collection) {
            return getObjectIdList(value);
        }
        if (value instanceof ObjectId){
            return value;
        }
        String convertValue = String.valueOf(value);
        if (ObjectId.isValid(convertValue) && PropertyCache.autoConvertObjectId) {
            return new ObjectId(convertValue);
        }
        if (log.isDebugEnabled()) {
            log.debug("value '" + value+"' is not ObjectId");
        }
        return value;
    }

    public static Object getObjectIdList(Object value) {
        return ((Collection<?>) value).stream().map(ObjectIdUtil::getObjectIdValue).collect(Collectors.toList());
    }

}
