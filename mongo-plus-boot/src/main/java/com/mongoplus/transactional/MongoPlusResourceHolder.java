package com.mongoplus.transactional;

import com.mongodb.client.ClientSession;
import com.mongoplus.context.MongoTransactionStatus;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * 每个 Spring 物理事务持有固定 session、截止时间和清理所有权。
 * 不依赖完成时的数据源上下文重新寻找 session。
 */
final class MongoPlusResourceHolder extends ResourceHolderSupport {

    final ClientSession session;
    final MongoTransactionStatus previousContext;
    final MongoTransactionStatus status;
    final boolean ownsSession;
    final boolean ownsTransaction;

    MongoPlusResourceHolder(ClientSession session, MongoTransactionStatus previousContext,
                            boolean ownsSession, boolean ownsTransaction) {
        this.session = session;
        this.previousContext = previousContext;
        this.ownsSession = ownsSession;
        this.ownsTransaction = ownsTransaction;
        this.status = ownsTransaction ? new ManagedStatus(session) : previousContext;
    }

    void checkTimeout() {
        if (hasTimeout()) {
            getTimeToLiveInMillis();
        }
    }

    /**
     * core 只调用中立的扩展方法；Spring 的截止时间和异常留在 Starter 中。
     * 两个上下文读写同一份 rollback-only 状态，用户捕获超时也不能继续提交。
     */
    private final class ManagedStatus extends MongoTransactionStatus {
        private ManagedStatus(ClientSession session) {
            super(session);
            // 为内嵌手工 core 事务保留外层引用，防止它提前提交/关闭 Spring 持有的会话。
            // Spring 自己的最终提交和关闭不以此计数作为条件。
            incrementReference();
        }

        @Override
        public boolean hasTimeout() {
            return MongoPlusResourceHolder.this.hasTimeout();
        }

        @Override
        public void checkTimeout() {
            MongoPlusResourceHolder.this.checkTimeout();
        }

        @Override
        public boolean isRollbackOnly() {
            return MongoPlusResourceHolder.this.isRollbackOnly();
        }

        @Override
        public void setRollbackOnly() {
            MongoPlusResourceHolder.this.setRollbackOnly();
        }
    }
}
