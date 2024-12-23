package com.mongoplus.cache.global;

import com.mongoplus.handlers.*;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.handlers.condition.EncryptorConditionHandler;
import com.mongoplus.handlers.field.DBRefFieldHandler;
import com.mongoplus.handlers.field.EncryptFieldHandler;
import com.mongoplus.handlers.field.TypeHandlerFieldHandler;
import com.mongoplus.mapping.handler.DesensitizationHandlerApply;
import com.mongoplus.mapping.handler.FieldEncryptApply;

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

    /**
     * 字段处理器
     */
    public static List<FieldHandler> fieldHandlers = new ArrayList<>();

    static {
        readHandlerList.add(new FieldEncryptApply());
        readHandlerList.add(new DesensitizationHandlerApply());
        conditionHandlerList.add(new EncryptorConditionHandler());
        // 初始化字段处理器
        initFieldHandler();
    }

    static void initFieldHandler() {
        fieldHandlers.add(new TypeHandlerFieldHandler());
        fieldHandlers.add(new EncryptFieldHandler());
        fieldHandlers.add(new DBRefFieldHandler());
    }

}
