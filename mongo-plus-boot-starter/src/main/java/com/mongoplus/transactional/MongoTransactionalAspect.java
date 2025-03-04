package com.mongoplus.transactional;

import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.cache.global.HandlerCache;
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

    @Pointcut("@annotation(com.mongoplus.annotation.transactional.MongoTransactional)")
    protected void markMongoTransactional() {
        // MongoTransactional注解的切入点方法
    }

    @Around(value = "markMongoTransactional() && @annotation(mongoTransactional)")
    public Object manageTransaction(ProceedingJoinPoint joinPoint, MongoTransactional mongoTransactional) throws Throwable {
        return HandlerCache.transactionHandler.transaction(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }, mongoTransactional);
    }
}

