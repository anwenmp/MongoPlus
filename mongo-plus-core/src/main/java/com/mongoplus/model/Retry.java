package com.mongoplus.model;

import com.mongoplus.interceptor.Invocation;

import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * 重试策略
 *
 * @author anwen
 */
public class Retry {

    /**
     * 最大重试次数，每次重试修改条件+1，默认5次
     */
    private Integer maxRetryNum;

    /**
     * 重试间隔时间，ms单位
     */
    private Long retryInterval;

    /**
     * 重试是否继续执行后续拦截器，默认为true
     * <p>如果后续的执行器中，进行了某些操作，请将该属性设置为false。比如在后续拦截器中做了修改其他表的操作，在重试时则会修改多次</p>
     */
    private Boolean processIntercept;

    /**
     * 每次重试增加version值，默认每次加1
     */
    private Integer autoVersionNum;

    /**
     * 开启异步重试，默认false
     * <p>开启异步后，会立即返回当前更新失败的响应，并且异步的重试，不受事务控制。可以通过回调配置查看最终结果</p>
     */
    private Boolean asyncRetry;

    /**
     * 命中重试回调
     */
    private Consumer<UpdateRetryResult> hitRetry;

    /**
     * 重试成功回调
     */
    private Consumer<UpdateRetryResult> onSuccess;

    /**
     * 重试失败回调
     */
    private Consumer<UpdateRetryResult> onFailure;

    /**
     * 达到最大重试次数失败后的备用逻辑
     */
    private BiFunction<UpdateRetryResult, Invocation, Object> fallback;

    Retry(final Integer maxRetryNum,
          final Long retryInterval,
          final Boolean processIntercept,
          final Boolean asyncRetry,
          final Integer autoVersionNum,
          final Consumer<UpdateRetryResult> hitRetry,
          final Consumer<UpdateRetryResult> onSuccess,
          final Consumer<UpdateRetryResult> onFailure,
          final BiFunction<UpdateRetryResult, Invocation, Object> fallback) {
        this.maxRetryNum = maxRetryNum;
        this.retryInterval = retryInterval;
        this.processIntercept = processIntercept;
        this.asyncRetry = asyncRetry;
        this.autoVersionNum = autoVersionNum;
        this.hitRetry = hitRetry;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.fallback = fallback;
    }

    public static RetryBuilder builder() {
        return new RetryBuilder();
    }

    public static class RetryBuilder {
        /**
         * 最大重试次数，每次重试修改条件+1，默认5次
         */
        private Integer maxRetryNum = 5;

        /**
         * 重试间隔时间，ms单位
         */
        private Long retryInterval = 500L;

        /**
         * 重试是否继续执行后续拦截器，默认为true
         * <p>如果后续的执行器中，进行了某些操作，请将该属性设置为false。比如在后续拦截器中做了修改其他表的操作，在重试时则会修改多次</p>
         */
        private Boolean processIntercept = true;

        /**
         * 每次重试增加version值，默认每次加1
         */
        private Integer autoVersionNum = 1;

        /**
         * 开启异步重试，默认false
         */
        private Boolean asyncRetry = false;

        /**
         * 命中重试回调
         */
        private Consumer<UpdateRetryResult> hitRetry;

        /**
         * 重试成功回调
         */
        private Consumer<UpdateRetryResult> onSuccess;

        /**
         * 重试失败回调
         */
        private Consumer<UpdateRetryResult> onFailure;

        /**
         * 达到最大重试次数失败后的备用逻辑
         */
        private BiFunction<UpdateRetryResult, Invocation, Object> fallback;

        RetryBuilder() {
        }

        /**
         * 最大重试次数
         *
         * @author anwen
         */
        public RetryBuilder maxRetryNum(final Integer maxRetryNum) {
            this.maxRetryNum = maxRetryNum;
            return this;
        }

        /**
         * 重试间隔时间
         *
         * @author anwen
         */
        public RetryBuilder retryInterval(final Long retryInterval) {
            this.retryInterval = retryInterval;
            return this;
        }

        /**
         * 重试是否继续执行后续拦截器，默认为true
         * <p>如果后续的执行器中，进行了某些操作，请将该属性设置为false。比如在后续拦截器中做了修改其他表的操作，在重试时则会修改多次</p>
         *
         * @author anwen
         */
        public RetryBuilder processIntercept(final Boolean processIntercept) {
            this.processIntercept = processIntercept;
            return this;
        }

        /**
         * 开启异步重试，默认false
         *
         * @author anwen
         */
        public RetryBuilder asyncRetry(final Boolean asyncRetry) {
            this.asyncRetry = asyncRetry;
            return this;
        }

        /**
         * 每次重试增加version值，默认每次加1
         *
         * @author anwen
         */
        public RetryBuilder autoVersionNum(final Integer autoVersionNum) {
            this.autoVersionNum = autoVersionNum;
            return this;
        }

        /**
         * 命中重试回调
         *
         * @author anwen
         */
        public RetryBuilder hitRetry(final Consumer<UpdateRetryResult> hitRetry) {
            this.hitRetry = hitRetry;
            return this;
        }

        /**
         * 重试成功回调
         *
         * @author anwen
         */
        public RetryBuilder onSuccess(final Consumer<UpdateRetryResult> onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        /**
         * 重试失败回调
         *
         * @author anwen
         */
        public RetryBuilder onFailure(final Consumer<UpdateRetryResult> onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        /**
         * 达到最大重试次数失败后的备用逻辑
         *
         * @author anwen
         */
        public RetryBuilder fallback(final BiFunction<UpdateRetryResult, Invocation, Object> fallback) {
            this.fallback = fallback;
            return this;
        }

        public Retry build() {
            return new Retry(this.maxRetryNum, this.retryInterval, this.processIntercept, this.asyncRetry, this.autoVersionNum, this.hitRetry, this.onSuccess, this.onFailure, this.fallback);
        }

        public String toString() {
            return "Retry.RetryBuilder(maxRetryNum=" +
                    this.maxRetryNum +
                    ", retryInterval=" +
                    this.retryInterval +
                    ", processIntercept=" +
                    this.processIntercept +
                    ", asyncRetry=" +
                    this.asyncRetry +
                    ", hitRetry=" +
                    this.hitRetry +
                    ", onSuccess=" +
                    this.onSuccess +
                    ", onFailure="
                    + this.onFailure +
                    ", fallback=" +
                    this.fallback + ")";
        }
    }

    public Integer getMaxRetryNum() {
        return this.maxRetryNum;
    }

    public Long getRetryInterval() {
        return this.retryInterval;
    }

    public Boolean getProcessIntercept() {
        return this.processIntercept;
    }

    public Integer getAutoVersionNum() {
        return this.autoVersionNum;
    }

    public Boolean getAsyncRetry() {
        return this.asyncRetry;
    }

    public Consumer<UpdateRetryResult> getHitRetry() {
        return this.hitRetry;
    }

    public Consumer<UpdateRetryResult> getOnSuccess() {
        return this.onSuccess;
    }

    public Consumer<UpdateRetryResult> getOnFailure() {
        return this.onFailure;
    }

    public BiFunction<UpdateRetryResult, Invocation, Object> getFallback() {
        return this.fallback;
    }

    public void setMaxRetryNum(final Integer maxRetryNum) {
        this.maxRetryNum = maxRetryNum;
    }

    public void setRetryInterval(final Long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public void setProcessIntercept(final Boolean processIntercept) {
        this.processIntercept = processIntercept;
    }

    public void setAutoVersionNum(final Integer autoVersionNum) {
        this.autoVersionNum = autoVersionNum;
    }

    public void setAsyncRetry(final Boolean asyncRetry) {
        this.asyncRetry = asyncRetry;
    }

    public void setHitRetry(final Consumer<UpdateRetryResult> hitRetry) {
        this.hitRetry = hitRetry;
    }

    public void setOnSuccess(final Consumer<UpdateRetryResult> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void setOnFailure(final Consumer<UpdateRetryResult> onFailure) {
        this.onFailure = onFailure;
    }

    public void setFallback(final BiFunction<UpdateRetryResult, Invocation, Object> fallback) {
        this.fallback = fallback;
    }

    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof Retry)) {
            return false;
        } else {
            Retry other = (Retry) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$maxRetryNum = this.getMaxRetryNum();
                Object other$maxRetryNum = other.getMaxRetryNum();
                if (this$maxRetryNum == null) {
                    if (other$maxRetryNum != null) {
                        return false;
                    }
                } else if (!this$maxRetryNum.equals(other$maxRetryNum)) {
                    return false;
                }

                Object this$retryInterval = this.getRetryInterval();
                Object other$retryInterval = other.getRetryInterval();
                if (this$retryInterval == null) {
                    if (other$retryInterval != null) {
                        return false;
                    }
                } else if (!this$retryInterval.equals(other$retryInterval)) {
                    return false;
                }

                Object this$processIntercept = this.getProcessIntercept();
                Object other$processIntercept = other.getProcessIntercept();
                if (this$processIntercept == null) {
                    if (other$processIntercept != null) {
                        return false;
                    }
                } else if (!this$processIntercept.equals(other$processIntercept)) {
                    return false;
                }

                Object this$asyncRetry = this.getAsyncRetry();
                Object other$asyncRetry = other.getAsyncRetry();
                if (this$asyncRetry == null) {
                    if (other$asyncRetry != null) {
                        return false;
                    }
                } else if (!this$asyncRetry.equals(other$asyncRetry)) {
                    return false;
                }

                Object this$autoVersionNum = this.getAutoVersionNum();
                Object other$autoVersionNum = other.getAutoVersionNum();
                if (this$autoVersionNum == null) {
                    if (other$autoVersionNum != null) {
                        return false;
                    }
                } else if (!this$autoVersionNum.equals(other$autoVersionNum)) {
                    return false;
                }

                Object this$hitRetry = this.getHitRetry();
                Object other$hitRetry = other.getHitRetry();
                if (this$hitRetry == null) {
                    if (other$hitRetry != null) {
                        return false;
                    }
                } else if (!this$hitRetry.equals(other$hitRetry)) {
                    return false;
                }

                Object this$onSuccess = this.getOnSuccess();
                Object other$onSuccess = other.getOnSuccess();
                if (this$onSuccess == null) {
                    if (other$onSuccess != null) {
                        return false;
                    }
                } else if (!this$onSuccess.equals(other$onSuccess)) {
                    return false;
                }

                Object this$onFailure = this.getOnFailure();
                Object other$onFailure = other.getOnFailure();
                if (this$onFailure == null) {
                    if (other$onFailure != null) {
                        return false;
                    }
                } else if (!this$onFailure.equals(other$onFailure)) {
                    return false;
                }

                Object this$fallback = this.getFallback();
                Object other$fallback = other.getFallback();
                if (this$fallback == null) {
                    return other$fallback == null;
                } else return this$fallback.equals(other$fallback);
            }
        }
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Retry;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $maxRetryNum = this.getMaxRetryNum();
        result = result * 59 + ($maxRetryNum == null ? 43 : $maxRetryNum.hashCode());
        Object $retryInterval = this.getRetryInterval();
        result = result * 59 + ($retryInterval == null ? 43 : $retryInterval.hashCode());
        Object $processIntercept = this.getProcessIntercept();
        result = result * 59 + ($processIntercept == null ? 43 : $processIntercept.hashCode());
        Object $asyncRetry = this.getAsyncRetry();
        result = result * 59 + ($asyncRetry == null ? 43 : $asyncRetry.hashCode());
        Object $autoVersionNum = this.getAutoVersionNum();
        result = result * 59 + ($autoVersionNum == null ? 43 : $autoVersionNum.hashCode());
        Object $hitRetry = this.getHitRetry();
        result = result * 59 + ($hitRetry == null ? 43 : $hitRetry.hashCode());
        Object $onSuccess = this.getOnSuccess();
        result = result * 59 + ($onSuccess == null ? 43 : $onSuccess.hashCode());
        Object $onFailure = this.getOnFailure();
        result = result * 59 + ($onFailure == null ? 43 : $onFailure.hashCode());
        Object $fallback = this.getFallback();
        result = result * 59 + ($fallback == null ? 43 : $fallback.hashCode());
        return result;
    }

    public String toString() {
        return "Retry(maxRetryNum=" + this.getMaxRetryNum() + ", retryInterval=" + this.getRetryInterval() + ", processIntercept=" + this.getProcessIntercept() + ", asyncRetry=" + this.getAsyncRetry() + ", autoVersionNum=" + this.getAutoVersionNum() + ", hitRetry=" + this.getHitRetry() + ", onSuccess=" + this.getOnSuccess() + ", onFailure=" + this.getOnFailure() + ", fallback=" + this.getFallback() + ")";
    }

}
