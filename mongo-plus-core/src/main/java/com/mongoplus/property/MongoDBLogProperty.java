package com.mongoplus.property;

import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.cache.global.PropertyCache;

/**
 * @author JiaChaoYang
 * 日志属性
 * @since 2023-06-07 23:07
 **/
public class MongoDBLogProperty {

    /**
     * 是否开启日志
     */
    private Boolean log = true;

    /**
     * 打印为shell格式
     */
    private Boolean shell = true;

    /**
     * 是否异步打印命令日志
     */
    private Boolean logAsync = false;

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

    /**
     * 指定日志拦截器的order，默认为0
     */
    private int logOrder = 0;

    public int getLogOrder() {
        return logOrder;
    }

    public void setLogOrder(int logOrder) {
        OrderCache.LOG_ORDER = logOrder;
        this.logOrder = logOrder;
    }

    /**
     * 是否打开格式化sql
     */
    private Boolean format = false;

    public Boolean getLog() {
        return this.log;
    }

    public Boolean getFormat() {
        return this.format;
    }

    public void setLog(final Boolean log) {
        PropertyCache.log = log;
        this.log = log;
    }

    public void setFormat(final Boolean format) {
        PropertyCache.format = format;
        this.format = format;
    }

    public MongoDBLogProperty(final Boolean log, final Boolean format) {
        this.log = log;
        this.format = format;
    }

    public MongoDBLogProperty() {
    }

}
