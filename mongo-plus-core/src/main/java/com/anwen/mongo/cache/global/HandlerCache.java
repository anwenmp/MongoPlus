package com.anwen.mongo.cache.global;

import com.anwen.mongo.handlers.MetaObjectHandler;
import com.anwen.mongo.handlers.ReadHandler;
import com.anwen.mongo.handlers.condition.ConditionHandler;
import com.anwen.mongo.handlers.condition.EncryptorConditionHandler;
import com.anwen.mongo.mapping.handler.DesensitizationHandlerApply;
import com.anwen.mongo.mapping.handler.FieldEncryptApply;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anwen
 * @date 2023-11-21 11:59
 **/
public class HandlerCache {

    /**
     * 自动填充处理器，只应有一个
     * @date 2023/11/23 12:53
    */
    public static MetaObjectHandler metaObjectHandler;

    /**
     * 读取处理器，可多个
     * @date 2023/11/23 12:54
    */
    public static List<ReadHandler> readHandlerList = new ArrayList<>();

    public static List<ConditionHandler> conditionHandlerList = new ArrayList<>();

    static {
        readHandlerList.add(new FieldEncryptApply());
        readHandlerList.add(new DesensitizationHandlerApply());
        conditionHandlerList.add(new EncryptorConditionHandler());
    }
}
