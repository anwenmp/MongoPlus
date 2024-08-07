package com.anwen.mongo.transactional;

import com.anwen.mongo.annotation.transactional.MongoTransactional;
import com.anwen.mongo.manager.MongoTransactionalManager;
import com.anwen.mongo.toolkit.ArrayUtils;
import com.anwen.mongo.toolkit.ClassTypeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

/**
 * AOP操作，实现声明式事务
 *
 * @author JiaChaoYang
 **/
@Aspect
@Order(1)
public class MongoTransactionalAspect {

    @Pointcut("@annotation(com.anwen.mongo.annotation.transactional.MongoTransactional)")
    private void markMongoTransactional() {
        // MongoTransactional注解的切入点方法
    }

    @Around(value = "markMongoTransactional() && @annotation(mongoTransactional)")
    public Object manageTransaction(ProceedingJoinPoint joinPoint, MongoTransactional mongoTransactional) throws Throwable {
        MongoTransactionalManager.startTransaction(mongoTransactional);
        try {
            Object result = joinPoint.proceed();
            MongoTransactionalManager.commitTransaction();
            return result;
        } catch (Exception e) {
            handleTransactionException(mongoTransactional, e);
            throw e;
        } finally {
            MongoTransactionalManager.closeSession();
        }
    }

    private void handleTransactionException(MongoTransactional mongoTransactional, Exception e) {
        Class<? extends Exception> eClass = e.getClass();
        boolean finished = processRollback(mongoTransactional, eClass, true)
                || processRollback(mongoTransactional, eClass, false);
        if (!finished) {
            MongoTransactionalManager.rollbackTransaction();
        }
    }

    private boolean processRollback(MongoTransactional mongoTransactional, Class<? extends Exception> eClass, boolean isRollback) {
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

