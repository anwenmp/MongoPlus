package com.mongoplus.tenant;

import com.mongoplus.annotation.tenant.IgnoreTenant;
import com.mongoplus.manager.TenantManager;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;

import java.util.Optional;

/**
 * 多租户切面
 *
 * @author anwen
 */
public class TenantAspect implements Interceptor {

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        return Optional.ofNullable(inv.method().getAnnotation(IgnoreTenant.class)).map(mongoDs -> {
            try {
                TenantManager.ignoreTenantCondition();
                return inv.invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                TenantManager.restoreTenantCondition();
            }
        });
    }
}
