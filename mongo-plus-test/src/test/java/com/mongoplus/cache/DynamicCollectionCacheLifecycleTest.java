package com.mongoplus.cache;

import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.interceptor.business.DynamicCollectionNameInterceptor;
import com.mongoplus.logic.UnClassCollection;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.registry.MongoEntityMappingRegistry;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class DynamicCollectionCacheLifecycleTest {

    private static final String DATABASE = "dynamic_cache_test";

    private final MongoEntityMappingRegistry registry = MongoEntityMappingRegistry.getInstance();
    private final AtomicInteger getCollectionCalls = new AtomicInteger();
    private final AtomicInteger closeCalls = new AtomicInteger();
    private final AtomicReference<String> dynamicName = new AtomicReference<>();

    private CollectionManager collectionManager;
    private DynamicCollectionNameInterceptor interceptor;
    private MongoCollection<Document> originalCollection;

    @Before
    public void setUp() {
        registry.clearMappingRelations();
        DataSourceNameCache.clear();

        MongoClient mongoClient = createMongoClient();
        MongoClientFactory.getInstance(Collections.singletonMap(DataSourceConstant.DEFAULT_DATASOURCE, mongoClient));

        collectionManager = new CollectionManager(DATABASE);
        Map<String, CollectionManager> databaseManagers = new LinkedHashMap<>();
        databaseManagers.put(DATABASE, collectionManager);
        Map<String, Map<String, CollectionManager>> dataSourceManagers = new LinkedHashMap<>();
        dataSourceManagers.put(DataSourceConstant.DEFAULT_DATASOURCE, databaseManagers);

        MongoPlusClient mongoPlusClient = new MongoPlusClient();
        mongoPlusClient.setCollectionManagerMap(dataSourceManagers);
        mongoPlusClient.setMongoDatabase(new ArrayList<>());
        interceptor = new DynamicCollectionNameInterceptor(
                (method, args, namespace) -> dynamicName.get(), mongoPlusClient);
        originalCollection = createMongoCollection("user");
    }

    @After
    public void tearDown() {
        registry.clearMappingRelations();
        DataSourceNameCache.clear();
    }

    @Test
    public void repeatedDynamicNameReusesOneCollectionEntryAndInstance() throws Exception {
        dynamicName.set("user_202608");
        MongoCollection<Document> first = resolveDynamicCollection();

        for (int i = 0; i < 99; i++) {
            assertSame(first, resolveDynamicCollection());
        }

        assertEquals(1, collectionCache().size());
        assertEquals(1, getCollectionCalls.get());
        assertSame(UnClassCollection.class,
                registry.getMappingResource(DATABASE + ".user_202608"));
    }

    @Test
    public void uniqueDynamicNamesIncreaseCollectionCacheByTheirCardinality() throws Exception {
        int before = collectionCache().size();

        for (int i = 0; i < 100; i++) {
            dynamicName.set("user_dynamic_" + i);
            resolveDynamicCollection();
        }

        assertEquals(100, collectionCache().size() - before);
        assertEquals(100, getCollectionCalls.get());
    }

    @Test
    public void uniqueDynamicNamesRemainRegisteredAsUnClassCollection() {
        for (int i = 0; i < 100; i++) {
            dynamicName.set("registry_dynamic_" + i);
            resolveDynamicCollection();
        }

        for (int i = 0; i < 100; i++) {
            assertSame(UnClassCollection.class,
                    registry.getMappingResource(DATABASE + ".registry_dynamic_" + i));
        }
    }

    @Test
    public void registryExplicitRemoveAndClearDeleteMetadata() {
        dynamicName.set("registry_remove");
        resolveDynamicCollection();
        dynamicName.set("registry_clear");
        resolveDynamicCollection();

        registry.removeMappingRelation(DATABASE + ".registry_remove");
        assertNull(registry.getMappingResource(DATABASE + ".registry_remove"));
        assertSame(UnClassCollection.class,
                registry.getMappingResource(DATABASE + ".registry_clear"));

        registry.clearMappingRelations();
        assertNull(registry.getMappingResource(DATABASE + ".registry_clear"));
    }

    @Test
    public void clientFactoryCloseDoesNotClearCollectionOrRegistryCaches() throws Exception {
        for (int i = 0; i < 3; i++) {
            dynamicName.set("close_dynamic_" + i);
            resolveDynamicCollection();
        }

        MongoClientFactory.getInstance().close();

        assertEquals(1, closeCalls.get());
        assertEquals(3, collectionCache().size());
        for (int i = 0; i < 3; i++) {
            assertSame(UnClassCollection.class,
                    registry.getMappingResource(DATABASE + ".close_dynamic_" + i));
        }
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<Document> resolveDynamicCollection() {
        Object[] args = {originalCollection};
        interceptor.beforeExecute(ExecuteMethodEnum.QUERY, args, originalCollection);
        return (MongoCollection<Document>) args[args.length - 1];
    }

    @SuppressWarnings("unchecked")
    private Map<String, MongoCollection<Document>> collectionCache() throws Exception {
        Field field = CollectionManager.class.getDeclaredField("collectionMap");
        field.setAccessible(true);
        return (Map<String, MongoCollection<Document>>) field.get(collectionManager);
    }

    private MongoClient createMongoClient() {
        MongoDatabase mongoDatabase = (MongoDatabase) Proxy.newProxyInstance(
                MongoDatabase.class.getClassLoader(), new Class<?>[]{MongoDatabase.class},
                (proxy, method, args) -> {
                    if ("getCollection".equals(method.getName())) {
                        getCollectionCalls.incrementAndGet();
                        return createMongoCollection((String) args[0]);
                    }
                    return defaultValue(method.getReturnType());
                });

        return (MongoClient) Proxy.newProxyInstance(
                MongoClient.class.getClassLoader(), new Class<?>[]{MongoClient.class},
                (proxy, method, args) -> {
                    if ("getDatabase".equals(method.getName())) {
                        return mongoDatabase;
                    }
                    if ("close".equals(method.getName())) {
                        closeCalls.incrementAndGet();
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    @SuppressWarnings("unchecked")
    private MongoCollection<Document> createMongoCollection(String collectionName) {
        MongoNamespace namespace = new MongoNamespace(DATABASE, collectionName);
        return (MongoCollection<Document>) Proxy.newProxyInstance(
                MongoCollection.class.getClassLoader(), new Class<?>[]{MongoCollection.class},
                (proxy, method, args) -> {
                    if ("getNamespace".equals(method.getName())) {
                        return namespace;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        if (type == double.class) {
            return 0D;
        }
        if (type == char.class) {
            return '\0';
        }
        return null;
    }
}
