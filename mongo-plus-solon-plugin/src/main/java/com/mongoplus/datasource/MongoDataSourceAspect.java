package com.mongoplus.datasource;

import com.mongoplus.annotation.datasource.MongoDs;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.toolkit.StringUtils;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;

import java.util.Optional;

/**
 * @author anwen
 */
public class MongoDataSourceAspect implements Interceptor {
    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        return Optional.ofNullable(inv.method().getAnnotation(MongoDs.class)).map(mongoDs -> {
            if (StringUtils.isBlank(mongoDs.value())) {
                throw new MongoPlusException("Data source not found");
            }
            DataSourceNameCache.setDataSource(mongoDs.value());
            try {
                return inv.invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                DataSourceNameCache.clear();
            }
        });
    }
}
