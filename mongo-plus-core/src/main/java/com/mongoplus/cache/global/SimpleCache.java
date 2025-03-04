package com.mongoplus.cache.global;

import com.mongoplus.mapping.SimpleTypeHolder;

/**
 * 简单类缓存
 *
 * @author anwen
 */
public class SimpleCache {

    private static SimpleTypeHolder simpleTypeHolder = new SimpleTypeHolder();

    public static SimpleTypeHolder getSimpleTypeHolder() {
        return simpleTypeHolder;
    }

    public static void setSimpleTypeHolder(SimpleTypeHolder simpleTypeHolder) {
        SimpleCache.simpleTypeHolder = simpleTypeHolder;
    }

}
