package com.mongoplus.context;

import com.mongodb.client.ClientSession;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MongoTransactionStatus {

    private final Log log = LogFactory.getLog(MongoTransactionStatus.class);

    /**
     * 持有的session
     */
    private final Map<String,ClientSession> clientSessionMap = new ConcurrentHashMap<>();

    /**
     * 引用嵌套计数器，表示当前事务方法的第几层
     */
    private long referenceCount;

    public MongoTransactionStatus(ClientSession clientSession) {
        this.clientSessionMap.put(DataSourceNameCache.getDataSource(),clientSession);
        this.referenceCount = 0;
    }

    public ClientSession getClientSession() {
        return this.clientSessionMap.get(DataSourceNameCache.getDataSource());
    }

    public void incrementReference() {
        log.debug("Reference increment");
        this.referenceCount++;
    }

    public void decrementReference() {
        log.debug("Reference decrement");
        this.referenceCount--;
    }

    public void clearReference() {
        log.debug("Reference clear");
        this.referenceCount = 0;
    }

    public boolean readyCommit() {
        return this.referenceCount == 0;
    }

    public boolean readyClose() {
        return this.referenceCount <= 0;
    }
}
