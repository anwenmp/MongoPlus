package com.mongoplus.interceptor.business;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.annotation.collection.Version;
import com.mongoplus.domain.OptimisticLockerException;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.MutablePair;
import com.mongoplus.registry.MongoEntityMappingRegistry;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 乐观锁拦截器的参数处理回归测试（不需要连数据库）。
 *
 * 覆盖 issue #21 里报的四种情况：
 * 1. $set 是 BsonDocument 时不能抛 ClassCastException；
 * 2. 条件是不可变 Bson（Filters.eq）时版本条件要能真正写回去；
 * 3. 自动版本但没配 Retry 时不能 NPE；
 * 4. bulkWrite 的 UpdateManyModel 不可变，要把版本条件写进重建后的模型。
 *
 * 另外覆盖批写入口的两处边界：
 * 5. 调用方传不可变列表（singletonList / unmodifiableList）时不能抛 UnsupportedOperationException，
 *    且处理后的列表要写回实际执行参数（否者重建的模型不会交给驱动执行）；
 * 6. 配置了版本缺失异常时，无法提供版本保护的管道更新必须被拒绝，不能放行到底层 bulkWrite。
 *
 * @author yehuoshun
 */
public class OptimisticLockerInterceptorTest {

    private static final String DATABASE = "optimistic_lock_test";

    private static final String COLLECTION_NAME = "versioned_doc";

    private static final String FULL_NAME = DATABASE + "." + COLLECTION_NAME;

    /** 只有 @Version 字段参与乐观锁 */
    public static class VersionedDoc {
        @Version
        private Integer version;
        private Integer score;
    }

    private OptimisticLockerInterceptor interceptor;
    private FieldInformation versionField;

    /** 底层执行方法被调用的次数 */
    private static int bulkWriteInvocations;

    /** 底层执行方法真正收到的参数，用来断言最终交给驱动的是什么 */
    private static List<WriteModel<Document>> executedModels;

    @Before
    public void setUp() {
        interceptor = new OptimisticLockerInterceptor();
        versionField = TypeInformation.of(VersionedDoc.class).getAnnotationField(Version.class);
        assertNotNull("测试实体必须能解析出 @Version 字段", versionField);
        bulkWriteInvocations = 0;
        executedModels = null;
        MongoEntityMappingRegistry.getInstance().setMappingRelation(FULL_NAME, VersionedDoc.class);
    }

    @After
    public void tearDown() {
        MongoEntityMappingRegistry.getInstance().removeMappingRelation(FULL_NAME);
        bulkWriteInvocations = 0;
        executedModels = null;
    }

    /** 1. $set 由 Updates.combine 构造（内部是 BsonDocument） */
    @Test
    public void shouldAppendVersionWhenSetIsBsonDocument() {
        MutablePair<Bson, Bson> pair = MutablePair.of(
                Filters.eq("_id", 1),
                Updates.combine(Updates.set("score", 111), Updates.set("version", 1)));

        interceptor.handler(invocation("executeUpdateOne", MutablePair.class, pair), false, versionField);

        Document filter = toDocument(pair.getLeft());
        Document update = toDocument(pair.getRight());
        assertEquals(1, filter.get("version"));
        assertFalse(((Document) update.get("$set")).containsKey("version"));
        assertEquals(1, ((Document) update.get("$inc")).get("version"));
    }

    /** 1.1 $set 的值是 BasicDBObject（驱动旧 API 里很常见） */
    @Test
    public void shouldAppendVersionWhenSetIsBasicDBObject() {
        MutablePair<Bson, Bson> pair = MutablePair.of(
                new Document("_id", 1),
                new Document("$set", new BasicDBObject("score", 111).append("version", 1)));

        interceptor.handler(invocation("executeUpdateOne", MutablePair.class, pair), false, versionField);

        Document update = toDocument(pair.getRight());
        assertFalse(((Document) update.get("$set")).containsKey("version"));
        assertEquals(1, ((Document) update.get("$inc")).get("version"));
    }

    /** 2. 条件是不可变 Bson、update 是普通 Document */
    @Test
    public void shouldAppendVersionWhenFilterIsImmutable() {
        MutablePair<Bson, Bson> pair = MutablePair.of(
                Filters.eq("_id", 1),
                new Document("$set", new Document("score", 111).append("version", 1)));

        interceptor.handler(invocation("executeUpdateOne", MutablePair.class, pair), false, versionField);

        Document filter = toDocument(pair.getLeft());
        assertEquals(1, filter.get("version"));
    }

    /** 3. 自动版本但没配 Retry：版本号从 filter 里取，不能 NPE */
    @Test
    public void shouldNotThrowWhenRetryIsNull() {
        MutablePair<Bson, Bson> pair = MutablePair.of(
                new Document("_id", 1).append("version", 1),
                new Document("$set", new Document("score", 111)));

        interceptor.handler(invocation("executeUpdateOne", MutablePair.class, pair), true, versionField);

        Document filter = toDocument(pair.getLeft());
        Document update = toDocument(pair.getRight());
        assertEquals(2, filter.get("version"));
        assertEquals(1, ((Document) update.get("$inc")).get("version"));
    }

    /** 3.1 自动版本、但 filter 里也拿不到版本号：跳过，不硬塞条件 */
    @Test
    public void shouldSkipWhenOriginalVersionMissing() {
        MutablePair<Bson, Bson> pair = MutablePair.of(
                Filters.eq("_id", 1),
                new Document("$set", new Document("score", 111)));

        interceptor.handler(invocation("executeUpdateOne", MutablePair.class, pair), true, versionField);

        Document filter = toDocument(pair.getLeft());
        Document update = toDocument(pair.getRight());
        assertFalse(filter.containsKey("version"));
        assertFalse(update.containsKey("$inc"));
    }

    /** 4. bulkWrite 里的 UpdateManyModel 不可变，要重建模型并写回执行参数 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAppendVersionInBulkWrite() {
        List<WriteModel<Document>> models = new ArrayList<>();
        models.add(new UpdateManyModel<>(Filters.eq("_id", 1),
                new Document("$set", new Document("score", 111).append("version", 1))));

        Invocation invocation = invocation("executeBulkWrite", List.class, models);
        interceptor.handler(invocation, false, versionField);

        UpdateManyModel<Document> handled =
                (UpdateManyModel<Document>) ((List<WriteModel<Document>>) invocation.getArgs()[0]).get(0);
        Document filter = toDocument(handled.getFilter());
        Document update = toDocument(handled.getUpdate());
        assertEquals(1, filter.get("version"));
        assertEquals(1, ((Document) update.get("$inc")).get("version"));
    }

    /** 5. 聚合管道更新没法追加 $set，跳过并保持原样 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldSkipAggregatePipelineUpdate() {
        List<WriteModel<Document>> models = new ArrayList<>();
        models.add(new UpdateManyModel<>(Filters.eq("_id", 1),
                Collections.singletonList(Document.parse("{\"$set\": {\"score\": 111}}"))));

        Invocation invocation = invocation("executeBulkWrite", List.class, models);
        interceptor.handler(invocation, false, versionField);

        UpdateManyModel<Document> handled =
                (UpdateManyModel<Document>) ((List<WriteModel<Document>>) invocation.getArgs()[0]).get(0);
        assertNotNull(handled.getUpdatePipeline());
        assertEquals(1, handled.getUpdatePipeline().size());
        assertTrue(handled.getFilter().toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry()).get("version") == null);
    }

    /** 6.1 调用方传 singletonList：不能抛 UnsupportedOperationException，且重建后的模型要交给驱动 */
    @Test
    public void shouldHandleBulkWriteWhenModelListIsSingletonList() throws Throwable {
        List<WriteModel<Document>> models = Collections.singletonList(updateManyModelWithVersion());
        assertBulkWriteHandledEndToEnd(models);
    }

    /** 6.2 调用方传 unmodifiableList：同上 */
    @Test
    public void shouldHandleBulkWriteWhenModelListIsUnmodifiable() throws Throwable {
        List<WriteModel<Document>> models = Collections.unmodifiableList(
                new ArrayList<>(Collections.singletonList(updateManyModelWithVersion())));
        assertBulkWriteHandledEndToEnd(models);
    }

    /**
     * 走完整的 intercept 链路：拦截器处理的列表必须写回实际执行参数，
     * 底层执行方法收到的模型要带版本条件 + 版本自增，调用方的不可变列表不能被改写。
     */
    @SuppressWarnings("unchecked")
    private void assertBulkWriteHandledEndToEnd(List<WriteModel<Document>> models) throws Throwable {
        WriteModel<Document> original = models.get(0);

        interceptor.intercept(invocation("executeBulkWrite", List.class, models, collection()));

        assertEquals("被拒绝以外的路径都要真的执行底层 bulkWrite", 1, bulkWriteInvocations);
        assertNotNull("底层执行方法必须收到处理后的列表", executedModels);
        assertNotSame("不能直接把调用方的不可变列表交给驱动", models, executedModels);
        assertSame("调用方传入的模型不应该被改写", original, models.get(0));

        UpdateManyModel<Document> handled = (UpdateManyModel<Document>) executedModels.get(0);
        Document filter = toDocument(handled.getFilter());
        Document update = toDocument(handled.getUpdate());
        assertEquals("版本条件要写进 filter", 3, filter.get("version"));
        assertEquals("版本要在 $inc 里自增", 1, ((Document) update.get("$inc")).get("version"));
        assertFalse("$set 里不能再留版本字段", ((Document) update.get("$set")).containsKey("version"));
    }

    /** 7. 配置了版本缺失异常：无法提供版本保护的管道更新必须被拒绝，底层 bulkWrite 不能被执行 */
    @Test
    public void shouldRejectPipelineUpdateWhenVersionIsNullExceptionConfigured() {
        interceptor.setVersionIsNullException(new OptimisticLockerException("缺少乐观锁版本号"));

        List<WriteModel<Document>> models = new ArrayList<>();
        models.add(new UpdateManyModel<>(new Document("_id", 1),
                Collections.singletonList(Document.parse("{\"$set\": {\"score\": 111}}"))));

        try {
            interceptor.intercept(invocation("executeBulkWrite", List.class, models, collection()));
            fail("配置了版本缺失异常时，管道更新必须被拒绝");
        } catch (OptimisticLockerException e) {
            assertEquals("缺少乐观锁版本号", e.getMessage());
        } catch (Throwable e) {
            throw new AssertionError("应该抛出配置的 OptimisticLockerException，实际是 " + e, e);
        }

        assertEquals("被拒绝时不能调用底层 bulkWrite", 0, bulkWriteInvocations);
        assertEquals("拒绝时不能改写调用方的列表", 1, models.size());
    }

    /** 7.1 没配版本缺失异常时，管道更新保持旧行为：告警并原样放行 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldKeepPipelineUpdateWhenVersionIsNullExceptionAbsent() throws Throwable {
        List<WriteModel<Document>> models = new ArrayList<>();
        models.add(new UpdateManyModel<>(new Document("_id", 1),
                Collections.singletonList(Document.parse("{\"$set\": {\"score\": 111}}"))));

        interceptor.intercept(invocation("executeBulkWrite", List.class, models, collection()));

        assertEquals(1, bulkWriteInvocations);
        UpdateManyModel<Document> handled = (UpdateManyModel<Document>) executedModels.get(0);
        assertNotNull(handled.getUpdatePipeline());
        assertTrue(handled.getFilter().toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry()).get("version") == null);
    }

    /** 维护者给的样例：$set 里既带版本又带业务字段，且 $set 的值是 BasicDBObject */
    private static UpdateManyModel<Document> updateManyModelWithVersion() {
        return new UpdateManyModel<>(new Document("_id", 1),
                new BasicDBObject("$set", new Document("version", 3).append("name", "after")));
    }

    /** 假的 MongoCollection，只提供拦截器需要的 namespace */
    @SuppressWarnings("unchecked")
    private static MongoCollection<Document> collection() {
        MongoNamespace namespace = new MongoNamespace(DATABASE, COLLECTION_NAME);
        return (MongoCollection<Document>) Proxy.newProxyInstance(
                OptimisticLockerInterceptorTest.class.getClassLoader(),
                new Class<?>[]{MongoCollection.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getNamespace":
                            return namespace;
                        case "toString":
                            return "fake-collection:" + FULL_NAME;
                        case "hashCode":
                            return System.identityHashCode(proxy);
                        case "equals":
                            return proxy == args[0];
                        default:
                            return null;
                    }
                });
    }

    /** 构造 Invocation：方法名决定走了哪个执行分支，args[0] 是拦截器要改的参数，末尾必须是 MongoCollection */
    private Invocation invocation(String methodName, Class<?> firstParam, Object arg0) {
        return invocation(methodName, firstParam, arg0, null);
    }

    private Invocation invocation(String methodName, Class<?> firstParam, Object arg0,
                                  MongoCollection<Document> collection) {
        try {
            Method method = OptimisticLockerInterceptorTest.class
                    .getDeclaredMethod(methodName, firstParam, MongoCollection.class);
            // 桩方法是 private 的，Invocation.proceed() 从拦截器包反射调用需要开权限
            method.setAccessible(true);
            return new Invocation(null, null, method, new Object[]{arg0, collection});
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("unused")
    private static Object executeUpdateOne(MutablePair<Bson, Bson> pair, MongoCollection<Document> collection) {
        return null;
    }

    @SuppressWarnings("unused")
    private static Object executeBulkWrite(List<WriteModel<Document>> models, MongoCollection<Document> collection) {
        bulkWriteInvocations++;
        executedModels = models;
        return null;
    }

    private static Document toDocument(Bson bson) {
        if (bson == null) {
            return new Document();
        }
        return Document.parse(bson.toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry()).toJson());
    }

}
