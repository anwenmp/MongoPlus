package com.mongoplus.transactional;

import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoTransactionalManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

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
        return MongoTransactionalManager.getTransaction();
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        ClientSession clientSession = (ClientSession) transaction;
        MongoTransactionalManager.startTransaction(options);
        if (log.isDebugEnabled()){
            log.debug("begin transaction -> name: {} , sessionId: {}",definition.getName(), clientSession.getServerSession().getIdentifier());
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        ClientSession clientSession = (ClientSession) status.getTransaction();
        MongoTransactionalManager.commitTransaction();
        if (log.isDebugEnabled()){
            log.debug("commit transaction -> sessionId: {}",clientSession.getServerSession().getIdentifier());
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        ClientSession clientSession = (ClientSession) status.getTransaction();
        MongoTransactionalManager.rollbackTransaction();
        if (log.isDebugEnabled()){
            log.debug("rollback transaction");
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
         MongoTransactionalManager.closeSession();
    }

}
