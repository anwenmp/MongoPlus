package com.mongoplus.config;

import com.mongoplus.annotation.mapper.Mongo;
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.datasource.MongoDataSourceAspect;
import com.mongoplus.logic.MongoLogicIgnoreAspect;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.property.MongoDBFieldProperty;
import com.mongoplus.property.MongoEncryptorProperty;
import com.mongoplus.proxy.MapperProxy;
import com.mongoplus.scanner.meta.MetadataReader;
import com.mongoplus.scanner.meta.MetadataReaderFactory;
import com.mongoplus.tenant.TenantAspect;
import com.mongoplus.transactional.MongoTransactionalAspect;
import com.mongodb.client.MongoClient;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.BeanInjector;
import org.noear.solon.core.Plugin;

/**
 * 使用插件处理配置
 * @author JiaChaoYang
 **/
public class XPluginAuto implements Plugin {
    @Override
    public void start(AppContext context) {
        //mongo-plus插件配置
        context.beanMake(MongoPlusConfiguration.class);
        context.getBeanAsync(MongoClient.class,bean -> context.beanInterceptorAdd(MongoTransactional.class,new MongoTransactionalAspect(bean)));
        context.beanMake(MongoDBFieldProperty.class);
        context.beanMake(MongoEncryptorProperty.class);
        context.beanMake(MongoDataSourceAspect.class);
        context.beanMake(TenantAspect.class);
        context.beanMake(MongoLogicIgnoreAspect.class);
        context.beanInjectorAdd(Inject.class,MongoMapper.class,registerMongoMapper());
    }

    BeanInjector<Inject> registerMongoMapper() {
        return (vh,annotation) -> {
            Class<?> mapperInterface = vh.getType();
            Object target = vh.context().getBean(mapperInterface);
            if (target == null && mapperInterface.isInterface()) {
                MetadataReader metadataReader = new MetadataReaderFactory(mapperInterface).getMetadataReader();
                Class<?> mongoMapper = metadataReader.getClassMetadata().getInterface(MongoMapper.class);
                Mongo mongo = metadataReader.getAnnotationMetadata().getAnnotation(Mongo.class);
                if (mongoMapper != null && mongo != null) {
                    vh.context().getBeanAsync(BaseMapper.class,baseMapper -> {
                        Object mongoMapperProxyInstance = MapperProxy.wrap(baseMapper, mapperInterface);
                        vh.context().wrapAndPut(mapperInterface,mongoMapperProxyInstance);
                        vh.setValue(mongoMapperProxyInstance);
                    });
                } else {
                    vh.context().getBeanAsync(mapperInterface,vh::setValue);
                }
            }
        };
    }

}
