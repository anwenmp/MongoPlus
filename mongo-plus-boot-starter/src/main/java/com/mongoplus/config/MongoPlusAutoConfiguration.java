package com.mongoplus.config;

import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.TimeSeries;
import com.mongoplus.aware.Aware;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.cache.global.ListenerCache;
import com.mongoplus.cache.global.MappingCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.handlers.CollectionNameHandler;
import com.mongoplus.handlers.IdGenerateHandler;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.handlers.TenantHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.incrementer.IdentifierGenerator;
import com.mongoplus.incrementer.id.AbstractIdGenerateHandler;
import com.mongoplus.incrementer.id.IdWorker;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.AdvancedInterceptorChain;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.business.DynamicCollectionNameInterceptor;
import com.mongoplus.interceptor.business.TenantInterceptor;
import com.mongoplus.listener.Listener;
import com.mongoplus.listener.business.BlockAttackInnerListener;
import com.mongoplus.listener.business.LogListener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.logic.LogicNamespaceAware;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.mapper.MongoMapperImpl;
import com.mongoplus.property.*;
import com.mongoplus.scanner.MongoEntityScanner;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.toolkit.AutoUtil;
import com.mongoplus.toolkit.CollUtil;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.domain.EntityScanPackages;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * MongoPlus自动注入配置
 *
 * @author JiaChaoYang
 **/
public class MongoPlusAutoConfiguration implements InitializingBean {

    private final ApplicationContext applicationContext;

    private final MongoDBLogProperty mongodbLogProperty;

    private final MongoDBCollectionProperty mongodbCollectionProperty;

    private final MongoLogicDelProperty mongoLogicDelProperty;

    private final BaseMapper baseMapper;

    private final MongoDBConfigurationProperty mongoDBConfigurationProperty;

    private final MongoPlusClient mongoPlusClient;

    Log log = LogFactory.getLog(MongoPlusAutoConfiguration.class);

    public MongoPlusAutoConfiguration(MongoDBLogProperty mongodbLogProperty,
                                      MongoDBCollectionProperty mongodbCollectionProperty,
                                      MongoLogicDelProperty mongoLogicDelProperty,
                                      MongoDBConfigurationProperty mongoDBConfigurationProperty,
                                      BaseMapper baseMapper,
                                      MongoDBFieldProperty ignore,
                                      ApplicationContext applicationContext, MongoPlusClient mongoPlusClient) {
        this.applicationContext = applicationContext;
        this.mongodbLogProperty = mongodbLogProperty;
        this.mongodbCollectionProperty = mongodbCollectionProperty;
        this.mongoLogicDelProperty = mongoLogicDelProperty;
        this.baseMapper = baseMapper;
        this.mongoDBConfigurationProperty = mongoDBConfigurationProperty;
        this.mongoPlusClient = mongoPlusClient;
        this.init();
    }

    public void init() {
        setConversion();
        setMetaObjectHandler();
        setListener();
        setInterceptor();
        setMapping();
        setIdGenerator();
        setTenantHandler();
        setDynamicCollectionHandler();
        setAware();
        collectionNameConvert();
        autoCreateTimeSeries();
        autoCreateIndexes();
        setIdGenerateHandler();
        setAdvancedInterceptor();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void afterPropertiesSet() {
        Collection<MongoMapper> values = applicationContext.getBeansOfType(MongoMapper.class).values();
        values.forEach(s -> {
            MongoMapperImpl<?> mongoMapper;
            if (s instanceof MongoMapperImpl){
                mongoMapper = (MongoMapperImpl<?>) s;
            } else {
                mongoMapper = (MongoMapperImpl<?>) AopProxyUtils.getSingletonTarget(s);
            }
            if (mongoMapper == null){
                return;
            }
            mongoMapper.setClazz(mongoMapper.getGenericityClass());
            mongoMapper.setBaseMapper(baseMapper);
        });
        setLogicFiled(values.stream().map(MongoMapper::getGenericityClass).toArray(Class[]::new));
    }

    /**
     * 设置感知类
     *
     * @author loser
     */
    public void setAware() {

        Configuration builder = Configuration.builder();
        builder.aware(new LogicNamespaceAware());
        for (Aware aware : applicationContext.getBeansOfType(Aware.class).values()) {
            builder.aware(aware);
        }

    }

    /**
     * 配置逻辑删除
     *
     * @param collectionClasses 需要进行逻辑删除的 collection class 集合
     * @author loser
     */
    private void setLogicFiled(Class<?>... collectionClasses) {
        Configuration.builder().logic(this.mongoLogicDelProperty).setLogicFiled(collectionClasses);
    }

    /**
     * 从Bean中拿到转换器
     *
     * @author JiaChaoYang
     */
    private void setConversion() {
        applicationContext.getBeansOfType(ConversionStrategy.class).values().forEach(conversionStrategy -> {
            try {
                if (conversionStrategy.getClass().isInterface()) {
                    ConversionCache.putConversionStrategy(conversionStrategy.getClass(), conversionStrategy);
                    return;
                }
                Type[] genericInterfaces = conversionStrategy.getClass().getGenericInterfaces();
                for (Type anInterface : genericInterfaces) {
                    ParameterizedType parameterizedType = (ParameterizedType) anInterface;
                    if (parameterizedType.getRawType().equals(ConversionStrategy.class)) {
                        Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        ConversionCache.putConversionStrategy(clazz, conversionStrategy);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Unknown converter type", e);
                throw new MongoPlusConvertException("Unknown converter type");
            }
        });
    }

    /**
     * 从Bean中拿到自动填充策略
     *
     * @author JiaChaoYang
     */
    private void setMetaObjectHandler() {
        applicationContext.getBeansOfType(MetaObjectHandler.class).values().forEach(metaObjectHandler -> HandlerCache.metaObjectHandler = metaObjectHandler);
    }

    /**
     * 从Bean中拿到监听器
     *
     * @author JiaChaoYang
     */
    private void setListener() {
        List<Listener> listeners = ListenerCache.listeners;
        if (mongodbLogProperty.getLog()) {
            listeners.add(new LogListener(mongodbLogProperty.getPretty()));
        }
        if (mongodbCollectionProperty.getBlockAttackInner()) {
            listeners.add(new BlockAttackInnerListener());
        }
        Collection<Listener> listenerCollection = applicationContext.getBeansOfType(Listener.class).values();
        if (CollUtil.isNotEmpty(listenerCollection)) {
            listeners.addAll(listenerCollection);
        }
        ListenerCache.sorted();
    }

    /**
     * 从Bean中拿到拦截器
     *
     * @author JiaChaoYang
     */
    private void setInterceptor() {
        Collection<Interceptor> interceptorCollection = applicationContext.getBeansOfType(Interceptor.class).values();
        if (CollUtil.isNotEmpty(interceptorCollection)) {
            InterceptorChain.addInterceptors(new ArrayList<>(interceptorCollection));
            InterceptorChain.sorted();
        }
    }

    /**
     * 设置高级拦截器
     * @author anwen
     */
    private void setAdvancedInterceptor(){
        Collection<AdvancedInterceptor> advancedInterceptors =
                applicationContext.getBeansOfType(AdvancedInterceptor.class).values();
        if (CollUtil.isNotEmpty(advancedInterceptors)) {
            AdvancedInterceptorChain.addInterceptors(new ArrayList<>(advancedInterceptors));
        }
    }

    /**
     * 从Bean中拿到映射器
     *
     * @author JiaChaoYang
     */
    private void setMapping() {
        applicationContext.getBeansOfType(MappingStrategy.class).values().forEach(mappingStrategy -> {
            try {
                if (mappingStrategy.getClass().isInterface()) {
                    MappingCache.putMappingStrategy(mappingStrategy.getClass(), mappingStrategy);
                    return;
                }
                Type[] genericInterfaces = mappingStrategy.getClass().getGenericInterfaces();
                for (Type anInterface : genericInterfaces) {
                    ParameterizedType parameterizedType = (ParameterizedType) anInterface;
                    if (parameterizedType.getRawType().equals(MappingStrategy.class)) {
                        Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        MappingCache.putMappingStrategy(clazz, mappingStrategy);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("Unknown Mapping type", e);
                throw new MongoPlusConvertException("Unknown converter type");
            }
        });
    }

    /**
     * 自定义id生成器
     *
     * @author anwen
     */
    private void setIdGenerator() {
        try {
            IdWorker.setIdentifierGenerator(applicationContext.getBean(IdentifierGenerator.class));
        } catch (Exception ignored) {
        }
    }

    /**
     * 多租户拦截器
     *
     * @author anwen
     */
    private void setTenantHandler() {
        TenantHandler tenantHandler = null;
        try {
            tenantHandler = applicationContext.getBean(TenantHandler.class);
        } catch (Exception ignored) {
        }
        if (tenantHandler != null) {
            InterceptorChain.addInterceptor(new TenantInterceptor(tenantHandler));
        }
    }

    /**
     * 动态集合名拦截器
     *
     * @author anwen
     */
    private void setDynamicCollectionHandler() {
        CollectionNameHandler collectionNameHandler = null;
        try {
            collectionNameHandler = applicationContext.getBean(CollectionNameHandler.class);
        } catch (Exception ignored) {
        }
        if (collectionNameHandler != null) {
            InterceptorChain.addInterceptor(new DynamicCollectionNameInterceptor(
                    collectionNameHandler,
                    baseMapper.getMongoPlusClient()
            ));
        }
    }

    /**
     * 注册集合名转换器
     *
     * @author anwen
     */
    public void collectionNameConvert() {
        AnnotationOperate.setCollectionNameConvertEnum(mongodbCollectionProperty.getMappingStrategy());
    }

    /**
     * 自动创建时间序列
     *
     */
    public void autoCreateTimeSeries() {
        if (mongoDBConfigurationProperty.getAutoCreateTimeSeries()) {
            MongoEntityScanner mongoEntityScanner = new MongoEntityScanner(getPackages());
            Set<Class<?>> collectionClassSet = mongoEntityScanner.scan(TimeSeries.class);
            AutoUtil.autoCreateTimeSeries(collectionClassSet, mongoPlusClient);
        }
    }

    /**
     * 扫描索引并创建
     *
     * @author anwen
     */
    public void autoCreateIndexes() {
        if (mongoDBConfigurationProperty.getAutoCreateIndex()) {
            MongoEntityScanner mongoEntityScanner = new MongoEntityScanner(getPackages());
            Set<Class<?>> collectionClassSet = mongoEntityScanner.scan(CollectionName.class);
            AutoUtil.autoCreateIndexes(collectionClassSet, mongoPlusClient);
        }
    }

    public List<String> getPackages() {
        List<String> packages = new LinkedList<>();
        if (CollUtil.isNotEmpty(mongoDBConfigurationProperty.getAutoScanPackages())) {
            packages.addAll(mongoDBConfigurationProperty.getAutoScanPackages());
        }
        List<String> packagesContext = EntityScanPackages.get(this.applicationContext).getPackageNames();
        if (packagesContext.isEmpty() && AutoConfigurationPackages.has(this.applicationContext)) {
            packagesContext = AutoConfigurationPackages.get(this.applicationContext);
        }
        packages.addAll(packagesContext);
        return packages;
    }

    /**
     * 设置id生成器
     *
     * @author anwen
     */
    public void setIdGenerateHandler() {
        IdGenerateHandler idGenerateHandler = new AbstractIdGenerateHandler(mongoPlusClient) {};
        try {
            idGenerateHandler = applicationContext.getBean(IdGenerateHandler.class);
        } catch (Exception ignored) {}
        HandlerCache.idGenerateHandler  = idGenerateHandler;
    }

}