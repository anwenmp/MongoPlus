package com.mongoplus.logic;

import com.mongoplus.annotation.logice.IgnoreLogic;
import com.mongoplus.cache.global.CollectionLogicDeleteCache;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;

import java.util.Optional;

/**
 * 忽略逻辑删除
 *
 * @author loser
 */
public class MongoLogicIgnoreAspect implements Interceptor {


    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        return Optional.ofNullable(inv.method().getAnnotation(IgnoreLogic.class)).map(ignoreLogic -> {
            try {
                CollectionLogicDeleteCache.setLogicIgnore(true);
                return inv.invoke();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                CollectionLogicDeleteCache.clear();
            }
        });
    }
}
