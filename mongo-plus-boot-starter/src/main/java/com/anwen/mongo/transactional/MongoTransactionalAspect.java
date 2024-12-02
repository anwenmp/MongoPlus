package com.anwen.mongo.transactional;

import com.anwen.mongo.annotation.transactional.MongoTransactional;
import com.anwen.mongo.manager.MongoTransactionalManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;

import static com.anwen.mongo.manager.MongoTransactionalManager.handleTransactionException;

/**
 * AOP操作，实现声明式事务
 *
 * @author JiaChaoYang
 **/
@Aspect
@Order(1)
public class MongoTransactionalAspect {

    @Pointcut("@annotation(com.anwen.mongo.annotation.transactional.MongoTransactional)")
    protected void markMongoTransactional() {
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
}

