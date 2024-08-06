package com.anwen.mongo.transactional;

import com.anwen.mongo.context.MongoTransactionSpring;
import com.anwen.mongo.factory.MongoClientFactory;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

/**
 * 自定义事务管理器，只支持简单的开启事务，如需其他事务配置，可以继承此类，重写{@link #doGetTransaction()}方法，或者写事务管理器
 * @author JiaChaoYang
 **/
public class MongoPlusTransactionalManager extends AbstractPlatformTransactionManager {

    Log log = LogFactory.getLog(MongoPlusTransactionalManager.class);

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return MongoClientFactory.getInstance().getMongoClient().startSession(ClientSessionOptions.builder().causallyConsistent(true).build());
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        ClientSession clientSession = (ClientSession) transaction;
        TransactionSynchronizationManager.bindResource(Objects.requireNonNull(definition.getName()),clientSession);
        clientSession.startTransaction();
        MongoTransactionSpring.setResources(TransactionSynchronizationManager.getResourceMap());
        MongoTransactionSpring.setCurrentTransactionName(definition.getName());
        if (log.isDebugEnabled()){
            log.debug("begin transaction -> name: {} , sessionId: {}",definition.getName(), clientSession.getServerSession().getIdentifier());
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        ClientSession clientSession = (ClientSession) status.getTransaction();
        if (clientSession.hasActiveTransaction()) {
            clientSession.commitTransaction();
        }
        MongoTransactionSpring.clear();
        if (log.isDebugEnabled()){
            log.debug("commit transaction -> sessionId: {}",clientSession.getServerSession().getIdentifier());
        }
    }

    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        ClientSession clientSession = (ClientSession) status.getTransaction();
        if (clientSession.hasActiveTransaction()) {
            clientSession.abortTransaction();
        }
        MongoTransactionSpring.clear();
        if (log.isDebugEnabled()){
            log.debug("rollback transaction");
        }
    }
}
