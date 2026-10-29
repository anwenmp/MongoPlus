package com.mongoplus.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.cache.global.MongoPlusClientCache;
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.cache.codec.MongoPlusCodecCache;
import com.mongoplus.codecs.MongoPlusCodec;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.domain.InitMongoPlusException;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.IdGenerateHandler;
import com.mongoplus.handlers.collection.AnnotationHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.manager.DataSourceManager;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.SimpleTypeHolder;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.model.BaseProperty;
import org.bson.Document;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * 使用 Driver 接口替身验证 core 的启动配置，不创建网络连接。
 */
public class ConfigurationBootstrapTest {

    private MongoClientFactory previousFactory;
    private MongoPlusClient previousClient;
    private Map<String, BaseProperty> previousProperties;
    private String previousDataSource;
    private SimpleTypeHolder previousSimpleTypes;
    private AnnotationHandler previousAnnotationHandler;
    private IdGenerateHandler previousIdGenerator;
    private List<MongoPlusCodec<?>> previousCodecs;

    @Before
    public void isolateGlobalState() {
        previousFactory = MongoClientFactory.getInstance();
        previousClient = MongoPlusClientCache.mongoPlusClient;
        previousProperties = new HashMap<>(DataSourceNameCache.getBasePropertyMap());
        previousDataSource = DataSourceNameCache.getDataSourceOrNull();
        previousSimpleTypes = SimpleCache.getSimpleTypeHolder();
        previousAnnotationHandler = AnnotationOperate.getAnnotationHandler();
        previousIdGenerator = HandlerCache.idGenerateHandler;
        previousCodecs = new ArrayList<>(MongoPlusCodecCache.getAllCodec());
        MongoClientFactory.getInstance(new HashMap<>());
        DataSourceNameCache.getBasePropertyMap().clear();
        DataSourceNameCache.clear();
        MongoPlusCodecCache.clear();
    }

    @After
    public void restoreGlobalState() throws Exception {
        Field factory = MongoClientFactory.class.getDeclaredField("mongoClientFactory");
        factory.setAccessible(true);
        factory.set(null, previousFactory);
        MongoPlusClientCache.mongoPlusClient = previousClient;
        SimpleCache.setSimpleTypeHolder(previousSimpleTypes);
        AnnotationOperate.setAnnotationHandler(previousAnnotationHandler);
        HandlerCache.idGenerateHandler = previousIdGenerator;
        MongoPlusCodecCache.clear();
        MongoPlusCodecCache.addAllCodec(previousCodecs);
        DataSourceNameCache.getBasePropertyMap().clear();
        DataSourceNameCache.getBasePropertyMap().putAll(previousProperties);
        if (previousDataSource == null) {
            DataSourceNameCache.clear();
        } else {
            DataSourceNameCache.setDataSource(previousDataSource);
        }
    }

    @Test
    public void existingClientShouldRegisterItsConnectionMetadata() {
        BaseProperty property = property("app");
        MongoClient client = client();
        MongoPlusClient result = Configuration.builder().initMongoPlusClient(client, property);
        assertSame(property, DataSourceNameCache.getBaseProperty("master"));
        assertEquals("app", result.getDatabase(Entity.class));
        assertSame(client, result.getMongoClient("master"));
    }

    @Test
    public void initializationShouldUseTheDefaultClientEvenWhenAnotherDataSourceIsSelected() {
        DataSourceNameCache.setDataSource("other");
        MongoClient client = client();
        MongoPlusClient result = Configuration.builder().initMongoPlusClient(client, property("app"));
        assertSame(client, result.getMongoClient("master"));
        assertEquals("app", result.getMongoDatabase().get(0).getName());
        assertEquals("other", DataSourceNameCache.getDataSource());
    }

    @Test
    public void clientMapperAndDataSourceManagerShouldReuseConfiguredObjects() {
        MongoClient client = client();
        MappingMongoConverter converter = new MappingMongoConverter();
        IdGenerateHandler generator = (type, info) -> "custom-id";
        Configuration configuration = Configuration.builder().connection("mongodb://example.invalid:27018")
                .database("app").mongoClient(client).mongoConverter(converter).idGenerateHandler(generator);
        MongoPlusClient result = configuration.getMongoPlusClient();
        assertSame(result, configuration.getMongoPlusClient());
        assertSame(generator, HandlerCache.idGenerateHandler);
        assertEquals("mongodb://example.invalid:27018", result.getBaseProperty().getUrl());
        BaseMapper mapper = configuration.getBaseMapper();
        assertSame(result, mapper.getMongoPlusClient());
        assertSame(converter, mapper.getMongoConverter());
        MappingMongoConverter explicitConverter = new MappingMongoConverter();
        assertSame(explicitConverter, configuration.getBaseMapper(explicitConverter).getMongoConverter());
        DataSourceManager manager = configuration.getDataSourceManager();
        assertEquals("master", manager.currentDataSourceName());
        assertSame(result, MongoPlusClientCache.mongoPlusClient);
    }

    @Test
    public void namedDataSourcesShouldRegisterClientsPropertiesAndAllDatabases() {
        MongoClient master = client();
        MongoClient reporting = client();
        BaseProperty reportingProperty = property("reporting,audit");
        Configuration configuration = Configuration.builder().database("app").mongoClient(master)
                .dataSource("analytics", reporting, reportingProperty);
        MongoPlusClient result = configuration.getMongoPlusClient();
        assertSame(reporting, result.getMongoClient("analytics"));
        assertSame(reportingProperty, DataSourceNameCache.getBaseProperty("analytics"));
        assertEquals(Arrays.asList("reporting", "audit"),
                new ArrayList<>(result.getCollectionManagers().get("analytics").keySet()));
        DataSourceManager manager = configuration.getDataSourceManager();
        manager.changeDataSource("analytics");
        assertSame(reporting, result.getMongoClient());
        assertEquals("reporting", result.getDatabase(Entity.class));
        manager.clearDataSource();
        assertSame(master, result.getMongoClient());
    }

    @Test
    public void customSimpleTypesShouldReachTheDefaultMapperConverter() {
        SimpleTypeHolder holder = new SimpleTypeHolder(Collections.singleton(Token.class), true);
        Configuration configuration = Configuration.builder().database("app").mongoClient(client())
                .simpleTypeHolder(holder);
        assertSame(holder, SimpleCache.getSimpleTypeHolder());
        Token token = new Token("value");
        Document mapped = new Document();
        configuration.getBaseMapper().getMongoConverter().write(new TokenEntity(token), mapped);
        assertSame(token, mapped.get("token"));
    }

    @Test
    public void annotationHandlerShouldResolveCollectionAndDatabaseProperties() {
        AnnotationHandler handler = new AnnotationHandler() {
            @Override
            @SuppressWarnings("unchecked")
            public <T, R> R getProperty(T annotation, Function<? super T, ? extends R> property) {
                return (R) String.valueOf(property.apply(annotation)).replace("${prefix}", "runtime");
            }
        };
        Configuration.builder().annotationHandler(handler);
        assertEquals("runtime_orders", AnnotationOperate.getCollectionName(DynamicEntity.class));
        assertEquals("runtime_app", AnnotationOperate.getDatabase(DynamicEntity.class));
    }

    @Test
    public void bannerShouldPrintOnlyWhenEnabledAndOnlyOnceForAReusedClient() throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PrintStream capture = new PrintStream(output, true, "UTF-8")) {
            System.setOut(capture);
            Configuration quiet = Configuration.builder().mongoClient(client()).database("app");
            quiet.getMongoPlusClient();
            assertEquals("", output.toString("UTF-8"));
            Configuration enabled = Configuration.builder().mongoClient(client()).database("app").banner(true);
            enabled.getMongoPlusClient();
            String firstBanner = output.toString("UTF-8");
            assertTrue(firstBanner.contains(" :: MongoPlus :: "));
            enabled.getMongoPlusClient();
            assertEquals(firstBanner, output.toString("UTF-8"));
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void missingDatabaseShouldFailBeforeCreatingClients() {
        Configuration configuration = Configuration.builder().connection("mongodb://example.invalid:27017");
        try {
            configuration.getMongoPlusClient();
            fail("database must be configured");
        } catch (InitMongoPlusException expected) {
            assertTrue(MongoClientFactory.getInstance().getMongoClientMap().isEmpty());
        }
        try {
            configuration.dataSource("analytics", new BaseProperty());
            fail("named data source database must be configured");
        } catch (InitMongoPlusException expected) {
            assertNull(DataSourceNameCache.getBaseProperty("analytics"));
        }
    }

    private static BaseProperty property(String database) {
        BaseProperty property = new BaseProperty();
        property.setDatabase(database);
        return property;
    }

    private static MongoClient client() {
        return (MongoClient) Proxy.newProxyInstance(MongoClient.class.getClassLoader(),
                new Class<?>[]{MongoClient.class}, (proxy, method, args) -> {
                    if ("getDatabase".equals(method.getName())) {
                        return Proxy.newProxyInstance(MongoDatabase.class.getClassLoader(),
                                new Class<?>[]{MongoDatabase.class}, (database, operation, arguments) -> {
                                    if ("getName".equals(operation.getName())) {
                                        return args[0];
                                    }
                                    throw new AssertionError(operation.getName());
                                });
                    }
                    throw new AssertionError(method.getName());
                });
    }

    public static class Entity {
    }

    @CollectionName(value = "${prefix}_orders", database = "${prefix}_app")
    public static class DynamicEntity {
    }

    public static class Token {
        private final String value;

        public Token(String value) {
            this.value = value;
        }
    }

    public static class TokenEntity {
        private final Token token;

        public TokenEntity(Token token) {
            this.token = token;
        }

        public Token getToken() {
            return token;
        }
    }

}
