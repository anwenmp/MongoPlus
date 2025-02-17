package com.mongoplus.cache.global;

import com.mongoplus.mapping.TypeInformation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InformationCache {

    private static final Map<Class<?>, TypeInformation> resources = new ConcurrentHashMap<>();

    public static void add(TypeInformation typeInformation) {
        resources.put(typeInformation.getClazz(),typeInformation);
    }

    public static TypeInformation get(Class<?> clazz) {
        return resources.get(clazz);
    }

}
