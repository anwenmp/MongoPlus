package com.mongoplus.cache.global;

import com.mongoplus.handlers.*;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.handlers.condition.EncryptorConditionHandler;
import com.mongoplus.handlers.field.DBRefHandler;
import com.mongoplus.handlers.field.EncryptFieldHandler;
import com.mongoplus.handlers.field.ObjectIdHandler;
import com.mongoplus.handlers.field.TypeHandlerFieldHandler;
import com.mongoplus.handlers.read.DesensitizationHandlerApply;
import com.mongoplus.handlers.read.FieldEncryptApply;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author anwen
 **/
public class HandlerCache {

    /**
     * 自动填充处理器，只应有一个
    */
    public static MetaObjectHandler metaObjectHandler;

    /**
     * 读取处理器，可多个
    */
    private static List<ReadHandler> readHandlerList = new ArrayList<>();

    /**
     * id生成处理器
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
        readHandlerList.add(new DBRefHandler());
        conditionHandlerList.add(new EncryptorConditionHandler());
        conditionHandlerList.add(new DBRefHandler());
        conditionHandlerList.add(new ObjectIdHandler());
        // 初始化字段处理器
        initFieldHandler();
    }

    /**
     * 获取所有读取处理器
     * @author anwen
     */
    public static List<ReadHandler> getReadHandler() {
        return readHandlerList;
    }

    /**
     * 设置读取处理器
     * @author anwen
     */
    public static void setReadHandler(ReadHandler readHandler){
        setReadHandler(Collections.singletonList(readHandler));
    }

    /**
     * 设置读取处理器
     * @author anwen
     */
    public static void setReadHandler(List<ReadHandler> readHandlers) {
        readHandlerList.addAll(readHandlers);
        readHandlerList = readHandlerList.stream()
                .sorted(Comparator.comparingInt(ReadHandler::order))
                .collect(Collectors.toList());
    }

    static void initFieldHandler() {
        fieldHandlers.add(new TypeHandlerFieldHandler());
        fieldHandlers.add(new EncryptFieldHandler());
        fieldHandlers.add(new DBRefHandler());
    }

}
