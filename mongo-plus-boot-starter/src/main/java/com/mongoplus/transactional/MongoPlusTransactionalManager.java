package com.mongoplus.transactional;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoTransactionalManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 自定义事务管理器
 * @author JiaChaoYang
 **/
@SuppressWarnings("NullableProblems")
public class MongoPlusTransactionalManager extends AbstractPlatformTransactionManager {

    Log log = LogFactory.getLog(MongoPlusTransactionalManager.class);

    private final TransactionOptions options;

    public MongoPlusTransactionalManager(TransactionOptions options) {
        this.options = options;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new MongoTransactionObject(
                (MongoTransactionStatus) TransactionSynchronizationManager.getResource(this));
    }

    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        MongoTransactionStatus status = ((MongoTransactionObject) transaction).getStatus();
        if (status == null) {
            return false;
        }
        ClientSession session = status.getClientSession();
        return session != null && session.hasActiveTransaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        MongoTransactionObject transactionObject = (MongoTransactionObject) transaction;
        ClientSession clientSession;
        MongoTransactionStatus status;
        if (definition instanceof MongoTransactionDefinition) {
            clientSession = MongoTransactionalManager.getTransaction(
                    ((MongoTransactionDefinition) definition).getMongoTransactional());
            status = MongoTransactionalManager.getTransactionStatus(clientSession, null);
        } else {
            clientSession = MongoTransactionalManager.getTransaction();
            status = MongoTransactionalManager.getTransactionStatus(clientSession, options);
        }
        MongoTransactionalManager.startTransaction(status);
        transactionObject.setStatus(status);
        TransactionSynchronizationManager.bindResource(this, status);
        if (log.isDebugEnabled()){
            log.debug("begin transaction -> name: {} , sessionId: {}",definition.getName(), clientSession.getServerSession().getIdentifier());
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        MongoTransactionObject transactionObject = (MongoTransactionObject) status.getTransaction();
        ClientSession clientSession = transactionObject.getStatus().getClientSession();
        MongoTransactionalManager.commitTransaction(transactionObject.getStatus());
        if (log.isDebugEnabled()){
            log.debug("commit transaction -> sessionId: {}",clientSession.getServerSession().getIdentifier());
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        MongoTransactionObject transactionObject = (MongoTransactionObject) status.getTransaction();
        MongoTransactionalManager.rollbackTransaction(transactionObject.getStatus());
        if (log.isDebugEnabled()){
            log.debug("rollback transaction");
        }
    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        ((MongoTransactionObject) status.getTransaction()).getStatus().setRollbackOnly();
    }

    @Override
    protected Object doSuspend(Object transaction) throws TransactionException {
        MongoTransactionObject transactionObject = (MongoTransactionObject) transaction;
        MongoTransactionStatus suspended = transactionObject.getStatus();
        transactionObject.setStatus(null);
        TransactionSynchronizationManager.unbindResource(this);
        MongoTransactionContext.clear();
        return suspended;
    }

    @Override
    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        MongoTransactionStatus suspended = (MongoTransactionStatus) suspendedResources;
        ((MongoTransactionObject) transaction).setStatus(suspended);
        TransactionSynchronizationManager.bindResource(this, suspended);
        MongoTransactionContext.setTransactionStatus(suspended);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        MongoTransactionObject transactionObject = (MongoTransactionObject) transaction;
        try {
            MongoTransactionalManager.closeSession(transactionObject.getStatus());
        } finally {
            if (TransactionSynchronizationManager.hasResource(this)) {
                TransactionSynchronizationManager.unbindResource(this);
            }
            transactionObject.setStatus(null);
        }
    }

    private static final class MongoTransactionObject implements SmartTransactionObject {
        private MongoTransactionStatus status;

        private MongoTransactionObject(MongoTransactionStatus status) {
            this.status = status;
        }

        private MongoTransactionStatus getStatus() {
            return status;
        }

        private void setStatus(MongoTransactionStatus status) {
            this.status = status;
        }

        @Override
        public boolean isRollbackOnly() {
            return status != null && status.isRollbackOnly();
        }

        @Override
        public void flush() {
            // MongoDB has no separate ORM flush phase.
        }
    }

}
