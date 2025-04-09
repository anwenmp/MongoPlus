package com.mongoplus.transactional;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.cache.global.MongoPlusClientCache;

/**
 * Mongo事务
 * 声明式事务请使用{@link com.mongoplus.manager.MongoTransactionalManager}
 * @author JiaChaoYang
 **/
public class MongoTransactionalOperate {

    /**
     * 创建一个session
     *
     * @author JiaChaoYang
     */
    public static ClientSession createTransaction(){
        return MongoPlusClientCache.mongoPlusClient.getMongoClient().startSession(ClientSessionOptions.builder().causallyConsistent(true).build());
    }

    /**
     * 创建并开启一个事务
     * @author JiaChaoYang
    */
    public static ClientSession startTransaction(String dataSourceName){
        ClientSession clientSession = MongoPlusClientCache.mongoPlusClient.getMongoClient().startSession(ClientSessionOptions.builder().causallyConsistent(true).build());
        clientSession.startTransaction();
        return clientSession;
    }

    /**
     * 开启一个事务
     * @author JiaChaoYang
    */
    public static void startTransaction(ClientSession clientSession){
        clientSession.startTransaction();
    }

    /**
     * 提交一个事务
     * @author JiaChaoYang
    */
    public static void commitTransaction(ClientSession clientSession){
        clientSession.commitTransaction();
    }

    /**
     * 提交并关闭一个事务
     * @author JiaChaoYang
    */
    public static void commitAndCloseTransaction(ClientSession clientSession){
        clientSession.commitTransaction();
        closeTransaction(clientSession);
    }

    /**
     * 回滚一个事务
     * @author JiaChaoYang
    */
    public static void rollbackTransaction(ClientSession clientSession){
        clientSession.abortTransaction();
    }

    /**
     * 回滚并关闭一个事务
     * @author JiaChaoYang
     */
    public static void rollbackAndCloseTransaction(ClientSession clientSession){
        clientSession.abortTransaction();
        closeTransaction(clientSession);
    }

    /**
     * 关闭事务
     * @author JiaChaoYang
    */
    public static void closeTransaction(ClientSession clientSession){
        clientSession.close();
    }

}
