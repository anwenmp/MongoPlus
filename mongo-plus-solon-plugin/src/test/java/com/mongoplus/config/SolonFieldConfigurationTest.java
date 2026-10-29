package com.mongoplus.config;

import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.property.MongoDBFieldProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.NvMap;
import org.noear.solon.core.Props;

import static org.junit.Assert.*;

/**
 * 使用真实 Solon 属性转换和变更事件验证配置引用，不启动插件或连接数据库。
 */
public class SolonFieldConfigurationTest {

    private Props properties;
    private AppContext context;
    private MongoPlusConfiguration configuration;
    private Boolean previousCamel;
    private Boolean previousIgnoringNull;
    private SolonApp previousApplication;

    @Before
    public void initializeContext() throws Exception {
        previousCamel = PropertyCache.camelToUnderline;
        previousIgnoringNull = PropertyCache.ignoringNull;
        previousApplication = Solon.app();
        ProbeApplication application = new ProbeApplication();
        SolonAccess.install(application);
        properties = application.cfg();
        context = application.context();
        configuration = new MongoPlusConfiguration();
    }

    @After
    public void restoreGlobalState() {
        PropertyCache.camelToUnderline = previousCamel;
        PropertyCache.ignoringNull = previousIgnoringNull;
        SolonAccess.install(previousApplication);
    }

    @Test
    public void missingConfigurationKeepsOriginalDefaults() {
        MongoDBFieldProperty bean = publish();
        assertFalse(bean.getCamelToUnderline());
        assertTrue(bean.getIgnoringNull());
        assertSame(bean, configuration.mongoDBFieldProperty());
    }

    @Test
    public void refreshUpdatesThePublishedBeanExistingConsumerAndCoreCaches() {
        properties.setProperty("mongo-plus.configuration.field.camelToUnderline", "false");
        properties.setProperty("mongo-plus.configuration.field.ignoringNull", "true");
        MongoDBFieldProperty bean = publish();
        ExistingConsumer consumer = new ExistingConsumer();
        context.beanInject(consumer);
        assertSame(bean, consumer.property);

        properties.setProperty("mongo-plus.configuration.field.camelToUnderline", "true");
        properties.setProperty("mongo-plus.configuration.field.ignoringNull", "false");
        assertSame(bean, context.getBean(MongoDBFieldProperty.class));
        assertSame(bean, configuration.mongoDBFieldProperty());
        assertSame(bean, consumer.property);
        assertTrue(consumer.property.getCamelToUnderline());
        assertFalse(consumer.property.getIgnoringNull());
        assertEquals(bean.getCamelToUnderline(), PropertyCache.camelToUnderline);
        assertEquals(bean.getIgnoringNull(), PropertyCache.ignoringNull);

        properties.setProperty("mongo-plus.configuration.field.camelToUnderline", "false");
        properties.setProperty("mongo-plus.configuration.field.ignoringNull", "true");
        assertSame(bean, context.getBean(MongoDBFieldProperty.class));
        assertFalse(consumer.property.getCamelToUnderline());
        assertTrue(consumer.property.getIgnoringNull());
        assertEquals(bean.getCamelToUnderline(), PropertyCache.camelToUnderline);
        assertEquals(bean.getIgnoringNull(), PropertyCache.ignoringNull);
    }

    @Test
    public void firstConfigurationAddedAfterPublicationUsesTheSameInstance() {
        MongoDBFieldProperty bean = publish();
        properties.setProperty("mongo-plus.configuration.field.camelToUnderline", "true");
        assertSame(bean, context.getBean(MongoDBFieldProperty.class));
        assertTrue(bean.getCamelToUnderline());
        assertEquals(Boolean.TRUE, PropertyCache.camelToUnderline);
    }

    @Test
    public void similarButUnrelatedPrefixDoesNotTriggerBinding() {
        properties.setProperty("mongo-plus.configuration.field.camelToUnderline", "false");
        MongoDBFieldProperty bean = publish();
        PropertyCache.camelToUnderline = true;
        properties.setProperty("mongo-plus.configuration.field-other.camelToUnderline", "true");
        assertFalse(bean.getCamelToUnderline());
        assertEquals(Boolean.TRUE, PropertyCache.camelToUnderline);
    }

    private MongoDBFieldProperty publish() {
        MongoDBFieldProperty bean = configuration.mongoDBFieldProperty(context);
        context.wrapAndPut(MongoDBFieldProperty.class, bean);
        return bean;
    }

    public static class ExistingConsumer {
        @Inject
        MongoDBFieldProperty property;
    }

    private static class ProbeApplication extends SolonApp {
        ProbeApplication() throws Exception {
            super(SolonFieldConfigurationTest.class, new NvMap());
        }
    }

    private static class SolonAccess extends Solon {
        static void install(SolonApp application) {
            appSet(application);
        }
    }
}
