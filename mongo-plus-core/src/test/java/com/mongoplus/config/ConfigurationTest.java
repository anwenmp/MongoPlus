package com.mongoplus.config;

import com.mongodb.client.MongoCollection;
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.cache.global.ListenerCache;
import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.encryptor.AESExample;
import com.mongoplus.encryptor.RSAExample;
import com.mongoplus.handlers.IdGenerateHandler;
import com.mongoplus.incrementer.id.AbstractIdGenerateHandler;
import com.mongoplus.listener.Listener;
import com.mongoplus.listener.MongoPlusListener;
import com.mongoplus.listener.business.BlockAttackInnerListener;
import com.mongoplus.listener.business.LogListener;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.toolkit.ObjectIdUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * 编程式配置的本地回归；恢复全局状态，避免影响其他测试。
 */
public class ConfigurationTest {

    private List<Listener> listeners;
    private Boolean log;
    private Boolean camelToUnderline;
    private Boolean ignoringNull;
    private Boolean autoConvertObjectId;
    private Boolean objectIdConvertType;
    private String autoIdCollectionName;
    private String key;
    private String publicKey;
    private String privateKey;
    private int logOrder;
    private int blockAttackInnerOrder;
    private IdGenerateHandler idGenerateHandler;

    private static final List<Integer> LISTENER_EVENTS = new ArrayList<>();

    @Before
    public void saveGlobalState() {
        listeners = ListenerCache.listeners;
        log = PropertyCache.log;
        camelToUnderline = PropertyCache.camelToUnderline;
        ignoringNull = PropertyCache.ignoringNull;
        autoConvertObjectId = PropertyCache.autoConvertObjectId;
        objectIdConvertType = PropertyCache.objectIdConvertType;
        autoIdCollectionName = PropertyCache.autoIdCollectionName;
        key = PropertyCache.key;
        publicKey = PropertyCache.publicKey;
        privateKey = PropertyCache.privateKey;
        logOrder = OrderCache.LOG_ORDER;
        blockAttackInnerOrder = OrderCache.BLOCK_ATTACK_INNER_ORDER;
        idGenerateHandler = HandlerCache.idGenerateHandler;
        ListenerCache.listeners = new ArrayList<>();
        PropertyCache.log = false;
        LISTENER_EVENTS.clear();
    }

    @After
    public void restoreGlobalState() {
        ListenerCache.listeners = listeners;
        PropertyCache.log = log;
        PropertyCache.camelToUnderline = camelToUnderline;
        PropertyCache.ignoringNull = ignoringNull;
        PropertyCache.autoConvertObjectId = autoConvertObjectId;
        PropertyCache.objectIdConvertType = objectIdConvertType;
        PropertyCache.autoIdCollectionName = autoIdCollectionName;
        PropertyCache.key = key;
        PropertyCache.publicKey = publicKey;
        PropertyCache.privateKey = privateKey;
        OrderCache.LOG_ORDER = logOrder;
        OrderCache.BLOCK_ATTACK_INNER_ORDER = blockAttackInnerOrder;
        HandlerCache.idGenerateHandler = idGenerateHandler;
        LISTENER_EVENTS.clear();
    }

    @Test
    public void logShouldEnableTheSameFlagAsThePrettyOverload() {
        Configuration configuration = Configuration.builder();
        assertSame(configuration, configuration.log());
        assertTrue(PropertyCache.log);
        assertEquals(1, ListenerCache.listeners.size());
        assertFalse(((LogListener) ListenerCache.listeners.get(0)).isPretty());
    }

    @Test
    public void listenerOrdersShouldApplyBeforeAndAfterRegistration() {
        Configuration configuration = Configuration.builder();
        assertSame(configuration, configuration.logOrder(20).blockAttackInnerOrder(-20)
                .log(true).blockAttackInner().listener(LastListener.class, FirstListener.class));
        assertEquals(Arrays.asList(-30, -20, 20, 30), listenerOrders());
        assertTrue(ListenerCache.listeners.get(1) instanceof BlockAttackInnerListener);
        assertTrue(((LogListener) ListenerCache.listeners.get(2)).isPretty());

        configuration.logOrder(-40).blockAttackInnerOrder(40);
        assertEquals(Arrays.asList(-40, -30, 30, 40), listenerOrders());
    }

    @Test
    public void customListenersShouldDispatchInOrder() {
        Configuration.builder().listener(LastListener.class, FirstListener.class);
        new MongoPlusListener().commandStarted(new CommandStarted());
        assertEquals(Arrays.asList(-30, 30), LISTENER_EVENTS);
    }

    @Test
    public void fieldOptionsShouldControlWrittenAndReadDocuments() {
        Configuration configuration = Configuration.builder().camelToUnderline(true).ignoringNull(true);
        MappingMongoConverter converter = new MappingMongoConverter();
        FieldEntity entity = new FieldEntity();
        entity.setDisplayName("Alice");
        Document mapped = new Document();
        converter.write(entity, mapped);
        assertEquals("Alice", mapped.getString("display_name"));
        assertFalse(mapped.containsKey("displayName"));
        assertFalse(mapped.containsKey("optional_value"));
        assertTrue(mapped.containsKey("kept_null"));
        assertNull(mapped.get("kept_null"));
        assertEquals("Alice", converter.readInternal(mapped, FieldEntity.class).getDisplayName());

        configuration.camelToUnderline(false).ignoringNull(false);
        Document unconverted = new Document();
        converter.write(entity, unconverted);
        assertEquals("Alice", unconverted.getString("displayName"));
        assertTrue(unconverted.containsKey("optionalValue"));
        assertNull(unconverted.get("optionalValue"));
        assertTrue(unconverted.containsKey("kept_null"));
    }

    @Test
    public void automaticObjectIdConversionShouldRespectTheSwitch() {
        String id = "507f1f77bcf86cd799439011";
        Configuration configuration = Configuration.builder().autoConvertObjectId(false);
        assertEquals(id, ObjectIdUtil.getObjectIdValue(id));
        configuration.autoConvertObjectId(true);
        assertEquals(new ObjectId(id), ObjectIdUtil.getObjectIdValue(id));
        assertEquals("invalid-id", ObjectIdUtil.getObjectIdValue("invalid-id"));
    }

    @Test
    public void generatedIdShouldUseTheConfiguredBsonType() {
        String generatedId = "507f1f77bcf86cd799439011";
        Configuration configuration = Configuration.builder().idGenerateHandler((type, info) -> generatedId);
        MappingMongoConverter converter = new MappingMongoConverter();
        configuration.objectIdConvertType(false);
        assertEquals(generatedId, converter.writeBySave(new IdEntity()).get("_id"));
        configuration.objectIdConvertType(true);
        assertEquals(new ObjectId(generatedId), converter.writeBySave(new IdEntity()).get("_id"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void autoIdShouldUseTheConfiguredCounterCollection() {
        Configuration.builder().autoIdCollectionName("custom_counters");
        List<String> collectionNames = new ArrayList<>();
        MongoCollection<Document> collection = (MongoCollection<Document>) Proxy.newProxyInstance(
                MongoCollection.class.getClassLoader(), new Class<?>[]{MongoCollection.class},
                (proxy, method, args) -> {
                    if ("findOneAndUpdate".equals(method.getName())) {
                        return new Document(SqlOperationConstant.AUTO_NUM, 7);
                    }
                    throw new AssertionError(method.getName());
                });
        MongoPlusClient client = new MongoPlusClient() {
            @Override
            public MongoCollection<Document> getCollection(Class<?> clazz, String collectionName) {
                collectionNames.add(collectionName);
                return collection;
            }
        };
        assertEquals(Integer.valueOf(7), new AbstractIdGenerateHandler(client) { }
                .generateAutoId(TypeInformation.of(IdEntity.class)));
        assertEquals(Arrays.asList("custom_counters"), collectionNames);
    }

    @Test
    public void symmetricEncryptionShouldUseTheConfiguredFallbackKey() throws Exception {
        Configuration configuration = Configuration.builder();
        assertSame(configuration, configuration.key("configuration-test-key"));
        AESExample encryptor = new AESExample();
        String encrypted = encryptor.encrypt("hello", null, null);
        assertNotEquals("hello", encrypted);
        assertEquals("hello", encryptor.decrypt(encrypted, null, null));
    }

    @Test
    public void asymmetricEncryptionShouldUseTheConfiguredFallbackKeys() throws Exception {
        RSAExample encryptor = new RSAExample();
        KeyPair pair = encryptor.generateKeyPair();
        Configuration configuration = Configuration.builder();
        assertSame(configuration, configuration
                .publicKey(StringUtils.bytesToHex(pair.getPublic().getEncoded()))
                .privateKey(StringUtils.bytesToHex(pair.getPrivate().getEncoded())));
        String encrypted = encryptor.encrypt("hello", null, null);
        assertEquals("hello", encryptor.decrypt(encrypted, null, null));
    }

    private List<Integer> listenerOrders() {
        return ListenerCache.listeners.stream().map(Listener::getOrder).collect(Collectors.toList());
    }

    public static class FirstListener extends LogListener {
        @Override
        public int getOrder() {
            return -30;
        }

        @Override
        public void commandStarted(CommandStarted commandStarted) {
            LISTENER_EVENTS.add(getOrder());
        }
    }

    public static class LastListener extends FirstListener {
        @Override
        public int getOrder() {
            return 30;
        }
    }

    public static class FieldEntity {
        private String displayName;
        private String optionalValue;
        @CollectionField(value = "kept_null", ignoreNull = false)
        private String keptNull;

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getOptionalValue() {
            return optionalValue;
        }

        public void setOptionalValue(String optionalValue) {
            this.optionalValue = optionalValue;
        }

        public String getKeptNull() {
            return keptNull;
        }

        public void setKeptNull(String keptNull) {
            this.keptNull = keptNull;
        }
    }

    public static class IdEntity {
        @ID
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
