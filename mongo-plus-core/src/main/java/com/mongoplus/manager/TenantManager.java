package com.mongoplus.manager;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.handlers.TenantHandler;
import org.bson.Document;

import java.util.function.Supplier;

/**
 * 多租户管理器
 *
 * @author anwen
 */
public class TenantManager {

    private TenantManager() {
    }

    private static final ThreadLocal<Boolean> ignoreTenant = new ThreadLocal<>();

    public static Boolean getIgnoreTenant() {
        return ignoreTenant.get();
    }

    /**
     * 忽略租户条件
     * @author anwen
     */
    public static <T> T withoutTenant(Supplier<T> supplier) {
        try {
            ignoreTenantCondition();
            return supplier.get();
        } finally {
            restoreTenantCondition();
        }
    }

    /**
     * 忽略 tenant 条件
     */
    public static void withoutTenant(Runnable runnable) {
        try {
            ignoreTenantCondition();
            runnable.run();
        } finally {
            restoreTenantCondition();
        }
    }

    /**
     * 忽略租户条件
     * @author anwen
     */
    public static void ignoreTenantCondition() {
        ignoreTenant.set(true);
    }

    /**
     * 恢复租户条件
     * @author anwen
     */
    public static void restoreTenantCondition() {
        ignoreTenant.remove();
    }

    /**
     * 是否忽略租户
     * <p>此处创建的TenantHandler为初始化行为，默认不忽略</p>
     * @param collection MongoCollection
     * @return {@link boolean}
     * @author anwen
     */
    public static boolean isTenantIgnored(MongoCollection<Document> collection) {
        return isTenantIgnored(collection, () -> null);
    }

    /**
     * 是否忽略租户
     * @param collection MongoCollection
     * @param tenantHandler TenantHandler
     * @return {@link boolean}
     * @author anwen
     */
    public static boolean isTenantIgnored(MongoCollection<Document> collection, TenantHandler tenantHandler) {
        MongoNamespace namespace = collection.getNamespace();
        String collectionName = namespace.getCollectionName();
        String databaseName = namespace.getDatabaseName();
        String dataSource = DataSourceNameCache.getDataSource();
        Boolean ignoreTenant = getIgnoreTenant();

        return ignoreTenant != null ?
                ignoreTenant :
                tenantHandler.ignoreCollection(collectionName) ||
                        tenantHandler.ignoreDatabase(databaseName) ||
                        tenantHandler.ignoreDataSource(dataSource);
    }

}
