package com.mongoplus.conditions.query;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.instance.DefaultExecute;
import com.mongoplus.execute.instance.SessionExecute;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.business.DynamicCollectionNameInterceptor;
import com.mongoplus.interceptor.business.TenantInterceptor;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.DefaultBaseMapperImpl;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.model.MutablePair;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 使用 Driver 接口替身验证原子调用、参数传递、映射与拦截器，不连接数据库。
 */
public class FindOneAndRemoveTest {

    @Test
    public void executorsShouldCallDriverExactlyOnceWithOrWithoutOptions() {
        ClientSession session = (ClientSession) Proxy.newProxyInstance(
                ClientSession.class.getClassLoader(), new Class<?>[]{ClientSession.class},
                (proxy, method, args) -> { throw new AssertionError(method.getName()); });
        for (boolean transactional : new boolean[]{false, true}) {
            Execute execute = transactional ? new SessionExecute(session) : new DefaultExecute();
            for (boolean withOptions : new boolean[]{false, true}) {
                for (boolean matched : new boolean[]{false, true}) {
                    DriverCall call = new DriverCall();
                    call.result = matched ? new Document("name", "Alice") : null;
                    Bson filter = new BasicDBObject("active", true);
                    FindOneAndDeleteOptions options = withOptions ? new FindOneAndDeleteOptions() : null;
                    assertSame(call.result, execute.executeFindOneAndRemove(filter, options, call.collection));
                    assertEquals(1, call.count);
                    int offset = transactional ? 1 : 0;
                    assertEquals(offset + (withOptions ? 2 : 1), call.args.length);
                    if (transactional) {
                        assertSame(session, call.args[0]);
                    }
                    assertSame(filter, call.args[offset]);
                    if (withOptions) {
                        assertSame(options, call.args[offset + 1]);
                    }
                }
            }
        }
    }

    @Test
    public void chainShouldPreserveFilterSortProjectionAndMapEntity() {
        DriverCall call = new DriverCall();
        call.result = new Document("name", "Alice");
        LambdaQueryChainWrapper<Person> chain = chain(call);
        chain.eq("active", true).orderByDesc("score").projectDisplay("name");

        assertEquals("Alice", chain.findOneAndRemove().getName());
        assertEquals(1, call.count);
        assertEquals(BsonDocument.parse("{active: {$eq: true}}"), bson((Bson) call.args[0]));
        FindOneAndDeleteOptions options = (FindOneAndDeleteOptions) call.args[1];
        assertEquals(BsonDocument.parse("{score: -1}"), bson(options.getSort()));
        assertEquals(BsonDocument.parse("{name: 1}"), bson(options.getProjection()));
        // 终结操作与 one/list 一样保留 Wrapper 状态。
        assertEquals(bson((Bson) call.args[0]), bson(chain.buildCondition().getCondition()));
    }

    @Test
    public void chainShouldMapAlternateResultTypeUsingEntityNamespace() {
        DriverCall call = new DriverCall();
        call.result = new Document("name", "Alice");
        NameView result = chain(call).findOneAndRemove(NameView.class);
        assertEquals("Alice", result.getName());
        assertEquals(1, call.count);
        assertTrue(bson((Bson) call.args[0]).isEmpty());
    }

    @Test
    public void chainShouldReturnNullWithoutRetryOnNoMatch() {
        DriverCall call = new DriverCall();
        assertNull(chain(call).eq("name", "missing").findOneAndRemove());
        assertEquals(1, call.count);
    }

    @Test
    public void callbackShouldPreserveWrapperDefaultsAndPassAdditionalOptions() {
        DriverCall call = new DriverCall();
        Collation collation = Collation.builder().locale("en").build();
        LambdaQueryChainWrapper<Person> chain = chain(call);
        chain.orderByDesc("score").projectDisplay("name");

        assertNull(chain.findOneAndRemove(options -> options
                .maxTime(3, TimeUnit.SECONDS)
                .collation(collation)
                .hintString("score_index")));

        FindOneAndDeleteOptions options = (FindOneAndDeleteOptions) call.args[1];
        assertEquals(3000L, options.getMaxTime(TimeUnit.MILLISECONDS));
        assertSame(collation, options.getCollation());
        assertEquals("score_index", options.getHintString());
        assertEquals(BsonDocument.parse("{score: -1}"), bson(options.getSort()));
        assertEquals(BsonDocument.parse("{name: 1}"), bson(options.getProjection()));
        assertEquals(1, call.count);
    }

    @Test
    public void callbackShouldOverrideProjectionAndSortAndMapAlternateType() {
        DriverCall call = new DriverCall();
        call.result = new Document("name", "Alice");
        LambdaQueryChainWrapper<Person> chain = chain(call);
        chain.orderByDesc("score").projectDisplay("score");
        BasicDBObject sort = new BasicDBObject("createdAt", 1);
        BasicDBObject projection = new BasicDBObject("name", 1);

        NameView result = chain.findOneAndRemove(options -> options
                .sort(sort).projection(projection), NameView.class);

        assertEquals("Alice", result.getName());
        FindOneAndDeleteOptions options = (FindOneAndDeleteOptions) call.args[1];
        assertSame(sort, options.getSort());
        assertSame(projection, options.getProjection());
        assertEquals(BsonDocument.parse("{score: -1}"), bson(chain.buildCondition().getSort()));
        assertEquals(BsonDocument.parse("{score: 1}"), bson(chain.buildCondition().getProjection()));
    }

    @Test
    public void eachExecutionShouldCreateIndependentOptions() {
        DriverCall call = new DriverCall();
        LambdaQueryChainWrapper<Person> chain = chain(call);
        chain.orderByDesc("score").projectDisplay("name");
        chain.findOneAndRemove(options -> options.sort(new Document("name", 1)).hintString("name_index"));
        FindOneAndDeleteOptions first = (FindOneAndDeleteOptions) call.args[1];

        chain.findOneAndRemove();
        FindOneAndDeleteOptions second = (FindOneAndDeleteOptions) call.args[1];

        assertNotSame(first, second);
        assertNull(second.getHintString());
        assertEquals(BsonDocument.parse("{score: -1}"), bson(second.getSort()));
        assertEquals(BsonDocument.parse("{name: 1}"), bson(second.getProjection()));
    }

    @Test
    public void callbackCanExplicitlyClearProjectionAndSort() {
        DriverCall call = new DriverCall();
        chain(call).orderByDesc("score").projectDisplay("name")
                .findOneAndRemove(options -> options.sort(null).projection(null));
        FindOneAndDeleteOptions options = (FindOneAndDeleteOptions) call.args[1];
        assertNull(options.getSort());
        assertNull(options.getProjection());
    }

    @Test
    public void callbackFailureShouldPreventDriverCall() {
        DriverCall call = new DriverCall();
        RuntimeException failure = new IllegalArgumentException("invalid options");
        try {
            chain(call).findOneAndRemove(options -> { throw failure; });
            fail("Expected callback failure");
        } catch (IllegalArgumentException exception) {
            assertSame(failure, exception);
        }
        assertEquals(0, call.count);
    }

    @Test
    public void proxyShouldApplyTenantAndDynamicCollectionAndReportOperation() {
        DriverCall original = new DriverCall();
        DriverCall routed = new DriverCall();
        routed.result = new Document("name", "Alice");
        List<Interceptor> previous = new ArrayList<>(InterceptorChain.getInterceptors());
        List<ExecuteMethodEnum> afterMethods = new ArrayList<>();
        try {
            InterceptorChain.getInterceptors().clear();
            InterceptorChain.addInterceptor(new TenantInterceptor(() -> new BsonString("tenant-a")));
            MongoPlusClient client = new MongoPlusClient() {
                @Override
                public MongoCollection<Document> getCollection(String database, String collectionName) {
                    assertEquals("people_archive", collectionName);
                    return routed.collection;
                }
            };
            InterceptorChain.addInterceptor(new DynamicCollectionNameInterceptor(
                    (method, args, namespace) -> "people_archive", client));
            InterceptorChain.addInterceptor(new Interceptor() {
                @Override
                public void afterExecute(ExecuteMethodEnum method, Object[] source, Object result,
                                         MongoCollection<Document> collection) {
                    afterMethods.add(method);
                    assertSame(routed.result, result);
                }
            });

            assertEquals("Alice", chain(original).eq("active", true).findOneAndRemove().getName());
            assertEquals(0, original.count);
            assertEquals(1, routed.count);
            assertEquals(BsonDocument.parse("{active: {$eq: true}, tenant_id: 'tenant-a'}"),
                    bson((Bson) routed.args[0]));
            assertEquals(1, afterMethods.size());
            assertEquals(ExecuteMethodEnum.FIND_ONE_AND_REMOVE, afterMethods.get(0));
        } finally {
            InterceptorChain.getInterceptors().clear();
            InterceptorChain.addInterceptors(previous);
        }
    }

    @Test
    public void chainShouldPropagateDriverFailureWithoutRetry() {
        DriverCall call = new DriverCall();
        call.failure = new IllegalStateException("driver failure");
        try {
            chain(call).findOneAndRemove();
            fail("Expected driver failure");
        } catch (IllegalStateException exception) {
            assertSame(call.failure, exception);
        }
        assertEquals(1, call.count);
    }

    private static LambdaQueryChainWrapper<Person> chain(DriverCall call) {
        MongoPlusClient client = new MongoPlusClient() {
            @Override
            public MongoCollection<Document> getCollection(String database, String collectionName) {
                assertEquals("test", database);
                assertEquals("people", collectionName);
                return call.collection;
            }
        };
        DefaultBaseMapperImpl mapper = new DefaultBaseMapperImpl(client, new MappingMongoConverter()) {
            @Override
            protected MutablePair<String, String> getNamespace(Class<?> clazz) {
                assertSame(Person.class, clazz);
                return new MutablePair<>("test", "people");
            }
        };
        return new LambdaQueryChainWrapper<>(mapper, Person.class);
    }

    private static BsonDocument bson(Bson value) {
        return value.toBsonDocument(Document.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    private static class DriverCall {
        private int count;
        private Object[] args;
        private Document result;
        private RuntimeException failure;

        @SuppressWarnings("unchecked")
        private final MongoCollection<Document> collection = (MongoCollection<Document>) Proxy.newProxyInstance(
                MongoCollection.class.getClassLoader(), new Class<?>[]{MongoCollection.class},
                (proxy, method, arguments) -> {
                    if ("getNamespace".equals(method.getName())) {
                        return new MongoNamespace("test", "people");
                    }
                    if ("findOneAndDelete".equals(method.getName())) {
                        count++;
                        args = arguments;
                        if (failure != null) {
                            throw failure;
                        }
                        return result;
                    }
                    throw new AssertionError("Unexpected Driver call: " + method.getName());
                });
    }

    public static class Person {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class NameView {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
