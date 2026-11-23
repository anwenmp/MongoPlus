package com.mongoplus.transactional;

import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * AOP操作，实现声明式事务
 *
 * @author JiaChaoYang
 **/
@Aspect
@Order(1)
public class MongoTransactionalAspect {

    private final PlatformTransactionManager transactionManager;

    public MongoTransactionalAspect(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Pointcut("@annotation(com.mongoplus.annotation.transactional.MongoTransactional)")
    protected void markMongoTransactional() {
        // MongoTransactional注解的切入点方法
    }

    @Around(value = "markMongoTransactional() && @annotation(mongoTransactional)")
    public Object manageTransaction(ProceedingJoinPoint joinPoint, MongoTransactional mongoTransactional) throws Throwable {
        TransactionStatus status = transactionManager.getTransaction(new MongoTransactionDefinition(mongoTransactional));
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable throwable) {
            if (rollbackOn(mongoTransactional, throwable.getClass())) {
                transactionManager.rollback(status);
            } else {
                transactionManager.commit(status);
            }
            throw throwable;
        }
        transactionManager.commit(status);
        return result;
    }

    private boolean rollbackOn(MongoTransactional transactional, Class<? extends Throwable> throwableType) {
        if (matches(transactional.rollbackFor(), throwableType)) {
            return true;
        }
        if (matches(transactional.noRollbackFor(), throwableType)) {
            return false;
        }
        return true;
    }

    private boolean matches(Class<? extends Throwable>[] configured, Class<? extends Throwable> actual) {
        if (ArrayUtils.isEmpty(configured)) {
            return false;
        }
        for (Class<? extends Throwable> type : configured) {
            if (ClassTypeUtil.isTargetClass(type, actual)) {
                return true;
            }
        }
        return false;
    }
}

