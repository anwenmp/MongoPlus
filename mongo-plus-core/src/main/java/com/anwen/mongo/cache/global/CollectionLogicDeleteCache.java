package com.anwen.mongo.cache.global;

import com.anwen.mongo.model.LogicDeleteResult;
import com.anwen.mongo.model.LogicProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 全局逻辑删除基础信息缓存
 *
 * @author loser
 * @date 2024/4/29
 */
public class CollectionLogicDeleteCache {

    private static final ThreadLocal<Boolean> logicIgnore = new InheritableThreadLocal<>();

    /**
     * 是否开启逻辑删除功能
     */
    public static Boolean open = false;

    /**
     * 逻辑删除配置
     */
    public static LogicProperty logicProperty = new LogicProperty();

    /**
     * 目标文档对应的逻辑删除字段
     */
    public static final Map<Class<?>, LogicDeleteResult> logicDeleteResultHashMap = new HashMap<>();

    public static void setLogicIgnore(boolean ignore) {
        logicIgnore.set(ignore);
    }

    public static boolean getLogicIgnore() {
        Boolean ignore = logicIgnore.get();
        return Objects.nonNull(ignore) && ignore;

    }

    public static void clear() {
        logicIgnore.remove();
    }

}
