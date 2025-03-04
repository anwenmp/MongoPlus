package com.mongoplus.transactional;

import com.mongodb.client.MongoClient;
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author JiaChaoYang
 **/
public class MongoTransactionalAspect implements Interceptor {

    private static final Log log = LogFactory.getLog(MongoTransactionalAspect.class);

    public MongoTransactionalAspect(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    private MongoClient mongoClient;

    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        if (mongoClient == null){
            mongoClient = MongoPlusClientCache.mongoPlusClient.getMongoClient();
        }
        AtomicReference<Object> invoke = new AtomicReference<>();
        Optional.ofNullable(inv.method().getAnnotation(MongoTransactional.class)).map(mongoTransactional -> {
            MongoTransactionalManager.startTransaction(mongoTransactional);
            try {
                invoke.set(inv.invoke());
                MongoTransactionalManager.commitTransaction();
                return invoke;
            } catch (Throwable e) {
                Class<? extends Throwable> eClass = e.getClass();
                boolean finish = doRollBack(mongoTransactional, eClass);
                if (!finish) {
                    finish = doUnRollBack(mongoTransactional, eClass);
                }
                if (!finish) {
                    MongoTransactionalManager.rollbackTransaction();
                }
                throw new RuntimeException(e);
            } finally {
                MongoTransactionalManager.closeSession();
            }
        });
        return invoke.get();
    }

    private static boolean doUnRollBack(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass) {

        Class<? extends Throwable>[] noRollBackList = mongoTransactional.noRollbackFor();
        if (ArrayUtils.isEmpty(noRollBackList)) {
            return false;
        }
        for (Class<? extends Throwable> eType : noRollBackList) {
            if (ClassTypeUtil.isTargetClass(eType,eClass)) {
                MongoTransactionalManager.commitTransaction();
                return true;
            }
        }
        return false;

    }

    private static boolean doRollBack(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass) {

        Class<? extends Throwable>[] rollBackList = mongoTransactional.rollbackFor();
        if (ArrayUtils.isEmpty(rollBackList)) {
            return false;
        }
        for (Class<? extends Throwable> eType : rollBackList) {
            if (ClassTypeUtil.isTargetClass(eType,eClass)) {
                MongoTransactionalManager.rollbackTransaction();
                return true;
            }
        }
        return false;

    }

}
