package com.mongoplus.transactional;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.manager.MongoTransactionalManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring 负责传播行为和物理事务边界，core 上下文只向执行器暴露同一份会话。
 */
@SuppressWarnings("NullableProblems")
public class MongoPlusTransactionalManager extends AbstractPlatformTransactionManager {

    private final TransactionOptions options;

    public MongoPlusTransactionalManager(TransactionOptions options) {
        this.options = options;
    }

    @Override
    protected Object doGetTransaction() {
        return new MongoTransactionObject(
                (MongoPlusResourceHolder) TransactionSynchronizationManager.getResource(this));
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) {
        MongoPlusResourceHolder holder = ((MongoTransactionObject) transaction).holder;
        return holder != null && holder.session.hasActiveTransaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        MongoTransactionObject object = (MongoTransactionObject) transaction;
        MongoTransactionStatus previous = MongoTransactionContext.getMongoTransactionStatus();
        ClientSession session = null;
        MongoPlusResourceHolder holder = null;
        boolean ownsSession = false;
        try {
            session = definition instanceof MongoTransactionDefinition
                    ? MongoTransactionalManager.getTransaction(
                            ((MongoTransactionDefinition) definition).getMongoTransactional())
                    : MongoTransactionalManager.getTransaction();
            ownsSession = previous == null || previous.getClientSession() != session;
            boolean ownsTransaction = !session.hasActiveTransaction();
            holder = new MongoPlusResourceHolder(session, previous, ownsSession, ownsTransaction);
            if (ownsTransaction) {
                int timeout = determineTimeout(definition);
                if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                    holder.setTimeoutInSeconds(timeout);
                }
                if (definition instanceof MongoTransactionDefinition || options == null) {
                    // 注解选项已经进入 session 的默认 TransactionOptions，不能被空通用选项覆盖。
                    session.startTransaction();
                } else {
                    session.startTransaction(options);
                }
                MongoTransactionContext.setTransactionStatus(holder.status);
            } else {
                // 保留在手工 core 事务内参与的兼容路径，不取得外层会话的关闭所有权。
                MongoTransactionalManager.startTransaction(holder.status);
            }
            object.holder = holder;
            TransactionSynchronizationManager.bindResource(this, holder);
        } catch (RuntimeException | Error failure) {
            if (holder != null && holder.ownsTransaction) {
                try {
                    if (session.hasActiveTransaction()) {
                        session.abortTransaction();
                    }
                } catch (RuntimeException | Error cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            if (ownsSession) {
                try {
                    session.close();
                } catch (RuntimeException | Error cleanupFailure) {
                    failure.addSuppressed(cleanupFailure);
                }
            }
            if (holder != null && TransactionSynchronizationManager.getResource(this) == holder) {
                TransactionSynchronizationManager.unbindResource(this);
            }
            restoreContext(previous);
            object.holder = null;
            throw failure;
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) {
        MongoPlusResourceHolder holder = holder(status);
        if (!holder.ownsTransaction) {
            MongoTransactionalManager.commitTransaction(holder.status);
            return;
        }
        try {
            holder.checkTimeout();
        } catch (TransactionTimedOutException timeout) {
            // 本地已到期且尚未发送 commit：明确 abort 后，通知 Spring 按 ROLLED_BACK 完成。
            // 不开启全局 rollbackOnCommitFailure，避免对未知提交结果作错误的回滚承诺。
            if (holder.session.hasActiveTransaction()) {
                holder.session.abortTransaction();
            }
            throw new UnexpectedRollbackException("MongoPlus transaction timed out before commit", timeout);
        }
        if (holder.session.hasActiveTransaction()) {
            try {
                holder.session.commitTransaction();
            } catch (RuntimeException failure) {
                // 交给 Spring 的提交失败分支处理，默认 UNKNOWN；不能把未知结果误报为已回滚。
                throw new TransactionSystemException("Could not commit MongoPlus transaction", failure);
            }
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) {
        MongoPlusResourceHolder holder = holder(status);
        if (!holder.ownsTransaction) {
            MongoTransactionalManager.rollbackTransaction(holder.status);
        } else if (holder.session.hasActiveTransaction()) {
            holder.session.abortTransaction();
        }
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) {
        holder(status).status.setRollbackOnly();
    }

    @Override
    protected Object doSuspend(Object transaction) {
        MongoTransactionObject object = (MongoTransactionObject) transaction;
        MongoPlusResourceHolder suspended = object.holder;
        TransactionSynchronizationManager.unbindResource(this);
        object.holder = null;
        MongoTransactionContext.clear();
        return suspended;
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) {
        MongoPlusResourceHolder suspended = (MongoPlusResourceHolder) suspendedResources;
        TransactionSynchronizationManager.bindResource(this, suspended);
        MongoTransactionContext.setTransactionStatus(suspended.status);
        if (transaction != null) {
            ((MongoTransactionObject) transaction).holder = suspended;
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        MongoTransactionObject object = (MongoTransactionObject) transaction;
        MongoPlusResourceHolder holder = object.holder;
        try {
            if (holder.ownsSession) {
                // Spring 参与式内层不会进入此处，物理事务完成后不再受 core 引用计数阻挡。
                holder.session.close();
            }
        } finally {
            if (TransactionSynchronizationManager.getResource(this) == holder) {
                TransactionSynchronizationManager.unbindResource(this);
            }
            if (MongoTransactionContext.getMongoTransactionStatus() == holder.status) {
                restoreContext(holder.previousContext);
            }
            holder.clear();
            object.holder = null;
        }
    }

    private static void restoreContext(MongoTransactionStatus previous) {
        if (previous == null) {
            MongoTransactionContext.clear();
        } else {
            MongoTransactionContext.setTransactionStatus(previous);
        }
    }

    private static MongoPlusResourceHolder holder(DefaultTransactionStatus status) {
        return ((MongoTransactionObject) status.getTransaction()).holder;
    }

    private static final class MongoTransactionObject implements SmartTransactionObject {
        private MongoPlusResourceHolder holder;

        private MongoTransactionObject(MongoPlusResourceHolder holder) {
            this.holder = holder;
        }

        @Override
        public boolean isRollbackOnly() {
            return holder != null && holder.status.isRollbackOnly();
        }

        @Override
        public void flush() {
            // MongoDB 没有独立的 ORM flush 阶段。
        }
    }
}
