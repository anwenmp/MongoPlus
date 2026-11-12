package com.mongoplus.interceptor.business;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.annotation.collection.Version;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.MutablePair;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * 乐观锁拦截器的参数处理回归测试（不需要连数据库）。
 *
 * 覆盖 issue #21 里报的四种情况：
 * 1. $set 是 BsonDocument 时不能抛 ClassCastException；
 * 2. 条件是不可变 Bson（Filters.eq）时版本条件要能真正写回去；
 * 3. 自动版本但没配 Retry 时不能 NPE；
 * 4. bulkWrite 的 UpdateManyModel 不可变，要把版本条件写进重建后的模型。
 *
 * @author yehuoshun
 */
public class OptimisticLockerInterceptorTest {

    /** 只有 @Version 字段参与乐观锁 */
    public static class VersionedDoc {
        @Version
        private Integer version;
        private Integer score;
    }

    private OptimisticLockerInterceptor interceptor;
    private FieldInformation versionField;

    @Before
    public void setUp() {
        interceptor = new OptimisticLockerInterceptor();
        versionField = TypeInformation.of(VersionedDoc.class).getAnnotationField(Version.class);
        assertNotNull("测试实体必须能解析出 @Version 字段", versionField);
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

    /** 4. bulkWrite 里的 UpdateManyModel 不可变，要重建模型 */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldAppendVersionInBulkWrite() {
        List<WriteModel<Document>> models = new ArrayList<>();
        models.add(new UpdateManyModel<>(Filters.eq("_id", 1),
                new Document("$set", new Document("score", 111).append("version", 1))));

        interceptor.handler(invocation("executeBulkWrite", List.class, models), false, versionField);

        UpdateManyModel<Document> handled = (UpdateManyModel<Document>) models.get(0);
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

        interceptor.handler(invocation("executeBulkWrite", List.class, models), false, versionField);

        UpdateManyModel<Document> handled = (UpdateManyModel<Document>) models.get(0);
        assertNotNull(handled.getUpdatePipeline());
        assertEquals(1, handled.getUpdatePipeline().size());
        assertTrue(handled.getFilter().toBsonDocument(BsonDocument.class,
                MongoClientSettings.getDefaultCodecRegistry()).get("version") == null);
    }

    /** 构造 Invocation：方法名决定走了哪个执行分支，args[0] 是拦截器要改的参数，末尾必须是 MongoCollection */
    private Invocation invocation(String methodName, Class<?> firstParam, Object arg0) {
        try {
            Method method = OptimisticLockerInterceptorTest.class
                    .getDeclaredMethod(methodName, firstParam, MongoCollection.class);
            return new Invocation(null, null, method, new Object[]{arg0, null});
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
