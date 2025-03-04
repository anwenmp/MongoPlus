package com.mongoplus.model;

import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;

/**
 * 更新重试结果
 *
 * @author anwen
 */
public class UpdateRetryResult {

    /**
     * 重试结果
     */
    private final Object result;

    /**
     * 当前重试次数
     */
    private final Integer currentRetryNum;

    /**
     * 参数
     */
    private final Object[] args;

    /**
     * 重试策略
     */
    private final Retry retry;

    public UpdateRetryResult(Object result, Integer currentRetryNum, Object[] args, Retry retry) {
        this.result = result;
        this.currentRetryNum = currentRetryNum;
        this.args = args;
        this.retry = retry;
    }

    public Object getResult() {
        return result;
    }

    public Integer getCurrentRetryNum() {
        return currentRetryNum;
    }

    public Retry getRetry() {
        return retry;
    }

    public Object[] getArgs() {
        return args;
    }

    /**
     * 是否是 UpdateResult
     * @author anwen
     */
    public boolean isUpdateResult() {
        return this.result instanceof UpdateResult;
    }

    /**
     * 是否是 BulkWriteResult
     * @author anwen
     */
    public boolean isBulkWriteResult() {
        return this.result instanceof BulkWriteResult;
    }

    /**
     * 获取为 UpdateResult 类型
     * @author anwen
     */
    public UpdateResult getUpdateResult() {
        return (UpdateResult) this.result;
    }

    /**
     * 获取为 BulkWriteResult 类型
     * @author anwen
     */
    public BulkWriteResult getBulkWriteResult() {
        return (BulkWriteResult) this.result;
    }

    /**
     * 获取更新数量
     * @return {@link long}
     * @author anwen
     */
    public long getModifiedCount() {
        return isUpdateResult() ? getUpdateResult().getModifiedCount() : getBulkWriteResult().getModifiedCount();
    }

    @Override
    public String toString() {
        return "UpdateRetryResult{" +
                "result=" + result +
                ", currentRetryNum=" + currentRetryNum +
                ", args.length=" + args.length +
                ", retry=" + retry +
                '}';
    }
}
