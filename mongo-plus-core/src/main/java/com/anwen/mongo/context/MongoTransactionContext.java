package com.anwen.mongo.context;

import com.mongodb.client.ClientSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * * @description TODO
 * * @author songguoxiang@yunquna.com
 * * @date 2023/09/17 17:42
 */
public class MongoTransactionContext {
    private static final ThreadLocal<MongoTransactionStatus> threadLocalHeaderMap = new ThreadLocal<>();

    /**
     * 当前线程所持有的所有事务
     */
    private static final ThreadLocal<Map<String,MongoTransactionStatus>> resources = new ThreadLocal<>();

    public MongoTransactionContext() {
    }

    public static ClientSession getClientSessionContext() {
        MongoTransactionStatus status = getMongoTransactionStatus();
        if (status != null) {
            return status.getClientSession();
        }
        return null;
    }

    public static MongoTransactionStatus getMongoTransactionStatus() {
        return threadLocalHeaderMap.get();
    }

    /**
     * 获取当前线程持有的所有事务
     * @author anwen
     */
    public static Map<String , MongoTransactionStatus> getAllMongoTransactionStatus() {
        return Optional.ofNullable(resources.get()).orElseGet(HashMap::new);
    }

    public static void removeResourcesTransactionStatus(String key){
        getAllMongoTransactionStatus().remove(key);
    }

    public static MongoTransactionStatus getResourcesTransactionStatus(String key){
        return getAllMongoTransactionStatus().get(key);
    }

    public static void addResourcesTransactionStatus(String key,MongoTransactionStatus mongoTransactionStatus){
        getAllMongoTransactionStatus().put(key,mongoTransactionStatus);
    }

    public static void setTransactionStatus(MongoTransactionStatus mongoTransactionStatus) {
        threadLocalHeaderMap.set(mongoTransactionStatus);
    }

    public static void clear() {
        threadLocalHeaderMap.remove();
    }

    public static void clearResources(){
        resources.remove();
    }

}
