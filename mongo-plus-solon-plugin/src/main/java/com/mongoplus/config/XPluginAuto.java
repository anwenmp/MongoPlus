package com.mongoplus.config;

import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.datasource.MongoDataSourceAspect;
import com.mongoplus.logic.MongoLogicIgnoreAspect;
import com.mongoplus.property.MongoDBFieldProperty;
import com.mongoplus.property.MongoEncryptorProperty;
import com.mongoplus.tenant.TenantAspect;
import com.mongoplus.transactional.MongoTransactionalAspect;
import com.mongodb.client.MongoClient;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/**
 * 使用插件处理配置
 * @author JiaChaoYang
 **/
public class XPluginAuto implements Plugin {
    @Override
    public void start(AppContext context) throws Throwable {
        //mongo-plus插件配置
        context.beanMake(MongoPlusConfiguration.class);
        context.getBeanAsync(MongoClient.class,bean -> context.beanInterceptorAdd(MongoTransactional.class,new MongoTransactionalAspect(bean)));
        context.beanMake(MongoDBFieldProperty.class);
        context.beanMake(MongoEncryptorProperty.class);
        context.beanMake(MongoDataSourceAspect.class);
        context.beanMake(TenantAspect.class);
        context.beanMake(MongoLogicIgnoreAspect.class);
    }
}
