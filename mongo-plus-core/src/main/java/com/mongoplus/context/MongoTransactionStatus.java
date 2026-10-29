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

    /**
     * 参与 Spring 事务的组件使用此标志将内部的回滚决策传播到外部事务边界。
     */
    private boolean rollbackOnly;

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

    public boolean isRollbackOnly() {
        return rollbackOnly;
    }

    /**
     * 是否需要在 Driver 执行边界检查超时；纯 core 事务默认不启用。
     */
    public boolean hasTimeout() {
        return false;
    }

    /**
     * 托管环境可以覆盖此方法，检查截止时间并标记回滚。
     * core 不依赖具体容器的超时异常或资源持有器。
     */
    public void checkTimeout() {
    }

    public void setRollbackOnly() {
        this.rollbackOnly = true;
    }
}
