package com.anwen.mongo.context;

import com.anwen.mongo.manager.MongoTransactionalManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShardingTransactionContext {

    /**
     * 当前线程所持有的所有事务
     */
    private static final ThreadLocal<Map<String, MongoTransactionStatus>> resources = new ThreadLocal<>();

    /**
     * 获取当前线程持有的所有事务
     * @author anwen
     */
    public static Map<String , MongoTransactionStatus> getAllMongoTransactionStatus() {
        return Optional.ofNullable(resources.get()).orElseGet(() -> {
            Map<String,MongoTransactionStatus> map = new HashMap<>();
            resources.set(map);
            return map;
        });
    }

    public static void removeResourcesTransactionStatus(String key){
        getAllMongoTransactionStatus().remove(key);
    }

    public static MongoTransactionStatus getTransactionStatus(String key){
        return getAllMongoTransactionStatus().get(key);
    }

    public static void addResourcesTransactionStatus(String key,MongoTransactionStatus mongoTransactionStatus){
        getAllMongoTransactionStatus().put(key,mongoTransactionStatus);
    }

    public static void clear(){
        resources.remove();
    }

    /**
     * 事务提交
     * @author anwen
     */
    public static void commitCurrentAllTransaction(){
        Collection<MongoTransactionStatus> mongoTransactionStatuses =
                getAllMongoTransactionStatus().values();
        mongoTransactionStatuses.forEach(MongoTransactionalManager::commitTransaction);
    }

    /**
     * 事务回滚
     *
     * @author JiaChaoYang
     */
    public static void rollbackAllTransaction() {
        Collection<MongoTransactionStatus> mongoTransactionStatuses =
                getAllMongoTransactionStatus().values();
        mongoTransactionStatuses.forEach(MongoTransactionalManager::rollbackTransaction);

    }

    /**
     * 关闭事务
     * @author anwen
     */
    public static void closeAllSession() {
        Collection<MongoTransactionStatus> mongoTransactionStatuses =
                getAllMongoTransactionStatus().values();
        mongoTransactionStatuses.forEach(MongoTransactionalManager::closeSession);
        clear();
    }

}
