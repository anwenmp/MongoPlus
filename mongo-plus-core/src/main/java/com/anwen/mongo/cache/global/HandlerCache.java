package com.anwen.mongo.cache.global;

import com.anwen.mongo.handlers.IdGenerateHandler;
import com.anwen.mongo.handlers.MetaObjectHandler;
import com.anwen.mongo.handlers.ReadHandler;
import com.anwen.mongo.handlers.TransactionHandler;
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

    /**
     * id生成处理器
     * @date 2024/9/24 00:30
     */
    public static IdGenerateHandler idGenerateHandler;

    /**
     * 条件处理器
     */
    public static List<ConditionHandler> conditionHandlerList = new ArrayList<>();

    /**
     * 事务处理器
     */
    public static TransactionHandler transactionHandler = new TransactionHandler();

    static {
        readHandlerList.add(new FieldEncryptApply());
        readHandlerList.add(new DesensitizationHandlerApply());
        conditionHandlerList.add(new EncryptorConditionHandler());
    }
}
