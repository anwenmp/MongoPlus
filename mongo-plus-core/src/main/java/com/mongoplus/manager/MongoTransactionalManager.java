package com.mongoplus.manager;

import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongoplus.annotation.transactional.MongoReadPreference;
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.domain.InitMongoPlusException;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.ReadConcernEnum;
import com.mongoplus.enums.ReadPreferenceEnum;
import com.mongoplus.enums.WriteConcernEnum;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Mongo事务管理
 *
 * @author JiaChaoYang
 **/
public class MongoTransactionalManager {

    private static final Log log = LogFactory.getLog(MongoTransactionalManager.class);

    public static MongoClient getMongoClient() {

        MongoClientFactory mongoClientFactory = MongoClientFactory.getInstance();
        if (mongoClientFactory == null) {
            throw new InitMongoPlusException("Please initialize MongoClientFactory first");
        }
        return mongoClientFactory.getMongoClient();

    }

    public static ClientSession getTransaction(){
        return getTransaction(null);
    }

    public static ClientSession getTransaction(MongoTransactional transactional){
        //获取线程中的session
        ClientSession session = MongoTransactionContext.getClientSessionContext();
        if (session == null) {
            ClientSessionOptions.Builder builder = ClientSessionOptions.builder();
            builder.causallyConsistent(true);
            if (Objects.nonNull(transactional)) {
                config(transactional, builder);
            }
            session = getMongoClient().startSession(builder.build());
        }
        return session;
    }

    /**
     * 事务开启
     *
     * @author JiaChaoYang
     */
    public static void startTransaction() {
        startTransaction(getTransaction());
    }

    public static void startTransaction(TransactionOptions options) {
        startTransaction(getTransaction(),options);
    }

    public static void startTransaction(MongoTransactional transactional) {
        startTransaction(getTransaction(transactional));
    }

    public static void startTransaction(ClientSession session) {
        startTransaction(session,null);
    }

    public static MongoTransactionStatus getTransactionStatus(MongoTransactional transactional) {
        return getTransactionStatus(getTransaction(transactional),null);
    }

    public static MongoTransactionStatus getTransactionStatus(ClientSession session,TransactionOptions options) {
        if (options == null){
            options = TransactionOptions.builder().build();
        }
        if (!session.hasActiveTransaction()) {
            session.startTransaction(options);
        }
        MongoTransactionStatus status = MongoTransactionContext.getMongoTransactionStatus();
        if (status == null) {
            status = new MongoTransactionStatus(session);
        }
        return status;
    }

    public static void startTransaction(ClientSession session,TransactionOptions options) {
        MongoTransactionStatus status = getTransactionStatus(session,options);
        startTransaction(status);
    }

    public static void startTransaction(MongoTransactionStatus status) {
        ClientSession session = status.getClientSession();
        if (session == null) {
            throw new MongoPlusException("clientSession is null");
        }
        if (!session.hasActiveTransaction()){
            session.startTransaction(session.getTransactionOptions());
        }
        MongoTransactionContext.setTransactionStatus(status);
        // 每个被切到的方法都引用加一
        MongoTransactionContext.getMongoTransactionStatus().incrementReference();
        if (log.isDebugEnabled()) {
            log.debug("Mongo transaction created, Thread:{}, session hashcode:{}", Thread.currentThread().getName(), session.hashCode());
        }
    }

    /**
     * 存在事务注解则进行自定义配置
     *
     * @param transactional 事务注解
     * @param builder       回话配置对象
     */
    private static void config(MongoTransactional transactional, ClientSessionOptions.Builder builder) {

        builder.causallyConsistent(transactional.causallyConsistent())
                .snapshot(transactional.snapshot());
        ReadConcern readConcern = buildReadConcern(transactional.readConcern());
        WriteConcern writeConcern = buildWriteConcern(transactional.writeConcern());
        ReadPreference readPreference = buildReadPreference(transactional.preference());
        TransactionOptions.Builder tsPBuilder = TransactionOptions.builder();
        if (transactional.maxCommitTimeMS() > 0) {
            tsPBuilder.maxCommitTime(transactional.maxCommitTimeMS(), transactional.timeUnit());
        }
        Optional.ofNullable(readConcern).ifPresent(tsPBuilder::readConcern);
        Optional.ofNullable(writeConcern).ifPresent(tsPBuilder::writeConcern);
        Optional.ofNullable(readPreference).ifPresent(tsPBuilder::readPreference);
        builder.defaultTransactionOptions(tsPBuilder.build());

    }

    /**
     * ReadPreference 是一个枚举类型，用于指定数据从不同节点读取的偏好。
     *
     * @param preferences 配置参数
     * @return 选择的读取偏好设置
     * @author loser
     */
    private static ReadPreference buildReadPreference(MongoReadPreference[] preferences) {

        if (Objects.isNull(preferences) || preferences.length != 1) {
            return null;
        }
        MongoReadPreference preference = preferences[0];
        ReadPreferenceEnum preferenceEnum = preference.preferenceEnum();
        long maxStaleness = preference.maxStaleness();
        TimeUnit timeUnit = preference.timeUnit();
        if (maxStaleness > 0) {
            return getReadPreference(preferenceEnum, maxStaleness, timeUnit);
        } else {
            return getReadPreference(preferenceEnum);
        }

    }

    /**
     * 不带超时时间
     *
     * @author loser
     */
    private static ReadPreference getReadPreference(ReadPreferenceEnum preferenceEnum) {

        switch (preferenceEnum) {
            case PRIMARY:
                return ReadPreference.primary();
            case PRIMARY_PREFERRED:
                return ReadPreference.primaryPreferred();
            case SECONDARY:
                return ReadPreference.secondary();
            case SECONDARY_PREFERRED:
                return ReadPreference.secondaryPreferred();
            case NEAREST:
                return ReadPreference.nearest();
            default:
                return null;
        }

    }

    /**
     * 带超时时间
     *
     * @author loser
     */
    private static ReadPreference getReadPreference(ReadPreferenceEnum preferenceEnum, long maxStaleness, TimeUnit timeUnit) {

        switch (preferenceEnum) {
            case PRIMARY:
                return ReadPreference.primary();
            case PRIMARY_PREFERRED:
                return ReadPreference.primaryPreferred(maxStaleness, timeUnit);
            case SECONDARY:
                return ReadPreference.secondary(maxStaleness, timeUnit);
            case SECONDARY_PREFERRED:
                return ReadPreference.secondaryPreferred(maxStaleness, timeUnit);
            case NEAREST:
                return ReadPreference.nearest(maxStaleness, timeUnit);
            default:
                return null;
        }

    }

    /**
     * WriteConcern 用于定义写入操作的安全等级。 WriteConcern 决定了一个写入操作需要被多少个节点确认才算成功。这可以用于保证数据的一致性和可靠性，在面对事务性操作时尤其重要
     *
     * @param writeConcernEnum 写入操作的安全等级枚举
     * @return 写入操作的安全等级
     * @author loser
     */
    private static WriteConcern buildWriteConcern(WriteConcernEnum writeConcernEnum) {

        if (Objects.isNull(writeConcernEnum)) {
            return null;
        }
        switch (writeConcernEnum) {
            case ACKNOWLEDGED:
                return WriteConcern.ACKNOWLEDGED;
            case UNACKNOWLEDGED:
                return WriteConcern.UNACKNOWLEDGED;
            case MAJORITY:
                return WriteConcern.MAJORITY;
            case W1:
                return WriteConcern.W1;
            case W2:
                return WriteConcern.W2;
            case W3:
                return WriteConcern.W3;
            case JOURNALED:
                return WriteConcern.JOURNALED;
            default:
                return null;
        }

    }

    /**
     * readConcern 用于定义读取操作的一致性和隔离级别。 readConcern 可以在每个操作或每个会话的基础上进行设置。对于事务，必须设置 readConcern 为 majority 以确保事务期间的读取能反映在数据的大多数副本上的最新数据
     *
     * @param readConcernEnum 读取配置枚举
     * @return 读取配置
     * @author loser
     */
    private static ReadConcern buildReadConcern(ReadConcernEnum readConcernEnum) {

        if (Objects.isNull(readConcernEnum)) {
            return null;
        }
        switch (readConcernEnum) {
            case DEFAULT:
                return ReadConcern.DEFAULT;
            case LOCAL:
                return ReadConcern.LOCAL;
            case MAJORITY:
                return ReadConcern.MAJORITY;
            case LINEARIZABLE:
                return ReadConcern.LINEARIZABLE;
            case AVAILABLE:
                return ReadConcern.AVAILABLE;
            case SNAPSHOT:
                return ReadConcern.SNAPSHOT;
            default:
                return null;
        }

    }


    /**
     * 事务提交
     *
     * @author JiaChaoYang
     */
    public static void commitTransaction() {
        MongoTransactionStatus status = MongoTransactionContext.getMongoTransactionStatus();
        commitTransaction(status);
    }

    /**
     * 事务提交
     * @param status 事务
     * @author anwen
     */
    public static void commitTransaction(MongoTransactionStatus status){
        if (status == null) {
            log.warn("no session to commit.");
            return;
        }
        status.decrementReference();
        if (status.readyCommit()) {
            ClientSession clientSession = status.getClientSession();
            if (clientSession.hasActiveTransaction()) {
                clientSession.commitTransaction();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Mongo transaction committed, Thread:{}, session hashcode:{}",
                    Thread.currentThread().getName(), status.getClientSession().hashCode()
            );
        }
    }

    /**
     * 事务回滚
     *
     * @author JiaChaoYang
     */
    public static void rollbackTransaction() {
        MongoTransactionStatus status = MongoTransactionContext.getMongoTransactionStatus();
        rollbackTransaction(status);
    }

    /**
     * 事务回滚
     *
     * @author JiaChaoYang
     */
    public static void rollbackTransaction(MongoTransactionStatus status) {
        if (status == null) {
            log.warn("no session to rollback.");
            return;
        }
        // 清空计数器
        status.clearReference();
        ClientSession clientSession = status.getClientSession();
        if (clientSession.hasActiveTransaction()) {
            clientSession.abortTransaction();
        }
        if (log.isDebugEnabled()) {
            log.debug("Mongo transaction rolled back, Thread:{}, session hashcode:{}", Thread.currentThread().getName(), status.getClientSession().hashCode());
        }
    }

    public static void closeSession() {
        MongoTransactionStatus status = MongoTransactionContext.getMongoTransactionStatus();
        closeSession(status);
    }

    public static void closeSession(MongoTransactionStatus status) {
        if (status == null) {
            log.warn("no session to rollback.");
            return;
        }
        if (status.readyClose()) {
            try {
                ClientSession clientSession = status.getClientSession();
                if (clientSession.hasActiveTransaction()) {
                    clientSession.close();
                }
            } finally {
                // 确保清理线程变量时不会被打断
                MongoTransactionContext.clear();
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Mongo transaction closed, Thread:{}, session hashcode:{}", Thread.currentThread().getName(), status.getClientSession().hashCode());
        }
    }

    public static void handleTransactionException(MongoTransactional mongoTransactional, Throwable e) {
        Class<? extends Throwable> eClass = e.getClass();
        boolean finished = processRollback(mongoTransactional, eClass, true)
                || processRollback(mongoTransactional, eClass, false);
        if (!finished) {
            MongoTransactionalManager.rollbackTransaction();
        }
    }

    public static boolean processRollback(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass, boolean isRollback) {
        Class<? extends Throwable>[] exceptionList = isRollback ? mongoTransactional.rollbackFor() : mongoTransactional.noRollbackFor();
        if (ArrayUtils.isEmpty(exceptionList)) {
            return false;
        }
        for (Class<? extends Throwable> exceptionType : exceptionList) {
            if (ClassTypeUtil.isTargetClass(exceptionType, eClass)) {
                if (isRollback) {
                    MongoTransactionalManager.rollbackTransaction();
                } else {
                    MongoTransactionalManager.commitTransaction();
                }
                return true;
            }
        }
        return false;
    }

}
