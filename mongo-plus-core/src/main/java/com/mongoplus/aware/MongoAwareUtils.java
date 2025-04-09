package com.mongoplus.aware;

import com.mongoplus.cache.global.AwareHandlerCache;

import java.util.List;

/**
 * 感知工具类 获取对应的 class 处理类
 *
 * @author loser
 */
public class MongoAwareUtils {

    private MongoAwareUtils() {
    }

    public static <T extends Aware> List<T> listHandlers(Class<T> clazz) {
        return AwareHandlerCache.listHandlers(clazz);
    }

}
