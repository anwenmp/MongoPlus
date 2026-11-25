package com.mongoplus.property;

import com.mongoplus.cache.global.OrderCache;

/**
 * @author JiaChaoYang
 * 日志属性
 * @since 2023-06-07 23:07
 **/
public class MongoDBLogProperty {

    /**
     * 打印为shell格式
     */
    private Boolean shell = true;

    /**
     * 是否异步打印命令日志
     */
    private Boolean logAsync = false;

    /**
     * 指定日志拦截器的order，默认为0
     */
    private int logOrder = 0;

    public Boolean getShell() {
        return shell;
    }

    public void setShell(Boolean shell) {
        this.shell = shell;
    }

    public Boolean getLogAsync() {
        return logAsync;
    }

    public void setLogAsync(Boolean logAsync) {
        this.logAsync = logAsync;
    }

    public int getLogOrder() {
        return logOrder;
    }

    public void setLogOrder(int logOrder) {
        OrderCache.LOG_ORDER = logOrder;
        this.logOrder = logOrder;
    }

    public MongoDBLogProperty() {
    }

    public MongoDBLogProperty(Boolean shell, Boolean logAsync, int logOrder) {
        this.shell = shell;
        this.logAsync = logAsync;
        this.logOrder = logOrder;
    }
}
