package com.mongoplus.handlers;

import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.support.ThrowableSupplier;

import static com.mongoplus.manager.MongoTransactionalManager.handleTransactionException;

/**
 * 事务处理器
 *
 * @author anwen
 */
public class TransactionHandler {

    /**
     * 事务执行
     * @return {@link java.lang.Object}
     * @author anwen
     */
    public Object transaction(ThrowableSupplier<Object> supplier, MongoTransactional mongoTransactional) throws Throwable {
        MongoTransactionalManager.startTransaction(mongoTransactional);
        try {
            Object result = supplier.get();
            MongoTransactionalManager.commitTransaction();
            return result;
        } catch (Throwable e) {
            handleTransactionException(mongoTransactional, e);
            throw e;
        } finally {
            MongoTransactionalManager.closeSession();
        }
    }

}
