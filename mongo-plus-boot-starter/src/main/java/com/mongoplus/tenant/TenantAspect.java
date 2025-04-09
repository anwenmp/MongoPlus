package com.mongoplus.tenant;

import com.mongoplus.manager.TenantManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

/**
 * 多租户切面
 *
 * @author anwen
 */
@Aspect
@Order(1)
public class TenantAspect {

    @Around("@annotation(com.mongoplus.annotation.tenant.IgnoreTenant)")
    public Object ignoreTenant(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            TenantManager.ignoreTenantCondition();
            return joinPoint.proceed();
        } finally {
            TenantManager.restoreTenantCondition();
        }
    }
}
