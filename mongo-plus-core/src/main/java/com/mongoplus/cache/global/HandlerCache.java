package com.mongoplus.cache.global;

import com.mongoplus.handlers.*;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.handlers.condition.EncryptorConditionHandler;
import com.mongoplus.handlers.field.DBRefHandler;
import com.mongoplus.handlers.field.ObjectIdHandler;
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
     * 字段处理器兼容入口，新代码请直接使用 {@link FieldHandlerChain}。
     */
    @Deprecated
    public static List<FieldHandler> fieldHandlers = FieldHandlerChain.getInstance();

    static {
        readHandlerList.add(new FieldEncryptApply());
        readHandlerList.add(new DesensitizationHandlerApply());
        readHandlerList.add(new DBRefHandler());
        conditionHandlerList.add(new EncryptorConditionHandler());
        conditionHandlerList.add(new DBRefHandler());
        conditionHandlerList.add(new ObjectIdHandler());
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
     * 添加读取处理器
     * @param readHandler 读取处理器
     */
    public static void addReadHandler(ReadHandler readHandler){
        readHandlerList.add(readHandler);
        sortReadHandler();
    }

    /**
     * 添加读取处理器
     * @param readHandlers 读取处理器
     */
    public static void addReadHandler(List<ReadHandler> readHandlers){
        readHandlerList.addAll(readHandlers);
        sortReadHandler();
    }

    /**
     * 设置读取处理器
     * @author anwen
     */
    public static void setReadHandler(List<ReadHandler> readHandlers) {
        readHandlerList.addAll(readHandlers);
        sortReadHandler();
    }

    public static void sortReadHandler() {
        readHandlerList = readHandlerList.stream()
                .sorted(Comparator.comparingInt(ReadHandler::order))
                .collect(Collectors.toList());
    }

}
