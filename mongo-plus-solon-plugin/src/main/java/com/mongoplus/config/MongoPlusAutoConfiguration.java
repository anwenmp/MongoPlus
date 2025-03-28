package com.mongoplus.config;

import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.TimeSeries;
import com.mongoplus.cache.codec.MongoPlusCodecCache;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.cache.global.ListenerCache;
import com.mongoplus.cache.global.MappingCache;
import com.mongoplus.codecs.MongoPlusCodec;
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
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.mapper.MongoMapperImpl;
import com.mongoplus.property.MongoDBCollectionProperty;
import com.mongoplus.property.MongoDBConfigurationProperty;
import com.mongoplus.property.MongoDBLogProperty;
import com.mongoplus.property.MongoLogicDelProperty;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.toolkit.AutoUtil;
import com.mongoplus.toolkit.CollUtil;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MongoPlus自动注入配置
 * @author JiaChaoYang
 **/
public class MongoPlusAutoConfiguration {

    private final BaseMapper baseMapper;

    private final MongoDBLogProperty mongoDBLogProperty;

    private final MongoDBCollectionProperty mongoDBCollectionProperty;

    private final MongoLogicDelProperty mongoLogicDelProperty;

    private final MongoPlusClient mongoPlusClient;

    private final MongoDBConfigurationProperty mongoDBConfigurationProperty;

    Log log = LogFactory.getLog(MongoPlusAutoConfiguration.class);

    public MongoPlusAutoConfiguration(BaseMapper baseMapper,
                                      MongoDBLogProperty mongoDBLogProperty,
                                      MongoDBCollectionProperty mongoDBCollectionProperty,
                                      MongoLogicDelProperty mongoLogicDelProperty,
                                      MongoPlusClient mongoPlusClient,
                                      MongoDBConfigurationProperty mongoDBConfigurationProperty){
        mongoDBCollectionProperty = Optional.ofNullable(mongoDBCollectionProperty)
                .orElseGet(MongoDBCollectionProperty::new);
        mongoLogicDelProperty = Optional.ofNullable(mongoLogicDelProperty)
                .orElseGet(MongoLogicDelProperty::new);
        mongoDBConfigurationProperty = Optional.ofNullable(mongoDBConfigurationProperty)
                .orElseGet(MongoDBConfigurationProperty::new);
        this.mongoDBLogProperty = mongoDBLogProperty;
        this.mongoDBCollectionProperty = mongoDBCollectionProperty;
        this.baseMapper = baseMapper;
        this.mongoLogicDelProperty = mongoLogicDelProperty;
        this.mongoPlusClient = mongoPlusClient;
        this.mongoDBConfigurationProperty = mongoDBConfigurationProperty;
        AppContext context = Solon.context();
        context.subBeansOfType(MongoMapper.class, bean -> {
            if (bean instanceof MongoMapperImpl){
                MongoMapperImpl<?> mongoMapper = (MongoMapperImpl<?>) bean;
                Class<?> genericityClass = bean.getGenericityClass();
                mongoMapper.setClazz(genericityClass);
                mongoMapper.setBaseMapper(baseMapper);
                setLogicFiled(genericityClass);
            }
        });
        init(context);
    }

    public void init(AppContext context){
        // 拿到转换器
        setConversion(context);
        // 拿到自动填充处理器
        setMetaObjectHandler(context);
        // 拿到监听器
        setListener(context);
        // 拿到拦截器
        setInterceptor(context);
        // 拿到属性映射器
        setMapping(context);
        // 拿到自定义id生成
        setIdGenerator(context);
        // 初始化集合名称转换器
        collectionNameConvert();
        // 自动创建时间序列
        autoCreateTimeSeries(context);
        // 自动创建索引
        autoCreateIndexes(context);
        // 设置id生成器
        setIdGenerateHandler(context);
        // 设置编解码器
        setMongoPlusCodec(context);
        // 设置高级拦截器
        setAdvancedInterceptor(context);
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
     * @author JiaChaoYang
     * @date 2023/10/19 12:49
     */
    private void setConversion(AppContext context){
        context.getBeansOfType(ConversionStrategy.class).forEach(conversionStrategy -> {
            try {
                Type[] genericInterfaces = conversionStrategy.getClass().getGenericInterfaces();
                for (Type anInterface : genericInterfaces) {
                    ParameterizedType parameterizedType = (ParameterizedType) anInterface;
                    if (parameterizedType.getRawType().equals(ConversionStrategy.class)){
                        Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                        ConversionCache.putConversionStrategy(clazz,conversionStrategy);
                        break;
                    }
                }
            }catch (Exception e){
                log.error("Unknown converter type");
                throw new MongoPlusConvertException("Unknown converter type");
            }
        });
    }

    /**
     * 从Bean中拿到自动填充策略
     * @author JiaChaoYang
     * @date 2023/11/21 12:18
     */
    private void setMetaObjectHandler(AppContext context){
        context.getBeansOfType(MetaObjectHandler.class).forEach(metaObjectHandler -> HandlerCache.metaObjectHandler = metaObjectHandler);
    }

    /**
     * 从Bean中拿到监听器
     * @author JiaChaoYang
     * @date 2023/11/22 18:39
     */
    private void setListener(AppContext context){
        List<Listener> listeners = ListenerCache.listeners;
        if (mongoDBLogProperty.getLog()){
            listeners.add(new LogListener(mongoDBLogProperty.getPretty()));
        }
        if (mongoDBCollectionProperty.getBlockAttackInner()){
            listeners.add(new BlockAttackInnerListener());
        }
        List<Listener> listenerCollection = context.getBeansOfType(Listener.class);
        if (CollUtil.isNotEmpty(listenerCollection)){
            listeners.addAll(listenerCollection);
        }
        ListenerCache.sorted();

    }

    /**
     * 从Bean中拿到拦截器
     *
     * @author JiaChaoYang
     * @date 2024/3/17 0:30
     */
    private void setInterceptor(AppContext context) {
        List<Interceptor> beansOfType = context.getBeansOfType(Interceptor.class);
        if (CollUtil.isNotEmpty(beansOfType)) {
            beansOfType = beansOfType.stream().sorted(Comparator.comparing(Interceptor::order)).collect(Collectors.toList());
        }
        InterceptorChain.addInterceptors(beansOfType);
    }

    /**
     * 设置高级拦截器
     * @author anwen
     */
    private void setAdvancedInterceptor(AppContext context){
        List<AdvancedInterceptor> advancedInterceptors =
                context.getBeansOfType(AdvancedInterceptor.class);
        if (CollUtil.isNotEmpty(advancedInterceptors)) {
            AdvancedInterceptorChain.addInterceptors(advancedInterceptors);
        }
    }

    /**
     * 从Bean中拿到映射器
     *
     * @author JiaChaoYang
     * @date 2024/3/17 0:30
     */
    private void setMapping(AppContext context) {
        context.getBeansOfType(MappingStrategy.class).forEach(mappingStrategy -> {
            try {
                if (mappingStrategy.getClass().isInterface()){
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
     * @author anwen
     * @date 2024/5/30 下午1:35
     */
    private void setIdGenerator(AppContext context) {
        try {
            IdWorker.setIdentifierGenerator(context.getBean(IdentifierGenerator.class));
        } catch (Exception ignored){}
    }

    /**
     * 多租户拦截器
     * @author anwen
     * @date 2024/6/27 下午12:44
     */
    private void setTenantHandler(AppContext context) {
        TenantHandler tenantHandler = null;
        try {
            tenantHandler = context.getBean(TenantHandler.class);
        } catch (Exception ignored){}
        if (tenantHandler != null) {
            InterceptorChain.addInterceptor(new TenantInterceptor(tenantHandler));
        }
    }

    /**
     * 动态集合名拦截器
     * @author anwen
     * @date 2024/6/27 下午3:47
     */
    private void setDynamicCollectionHandler(AppContext context){
        CollectionNameHandler collectionNameHandler = null;
        try {
            collectionNameHandler = context.getBean(CollectionNameHandler.class);
        } catch (Exception ignored){}
        if (collectionNameHandler != null) {
            InterceptorChain.addInterceptor(new DynamicCollectionNameInterceptor(collectionNameHandler, baseMapper.getMongoPlusClient()));
        }
    }

    /**
     * 注册集合名转换器
     * @author anwen
     * @date 2024/5/27 下午11:20
     */
    public void collectionNameConvert(){
        AnnotationOperate.setCollectionNameConvertEnum(mongoDBCollectionProperty.getMappingStrategy());
    }

    /**
     * 自动创建时间序列
     * @author anwen
     * @date 2024/8/28 11:16
     */
    public void autoCreateTimeSeries(AppContext context){
        if (mongoDBConfigurationProperty.getAutoCreateTimeSeries()) {
            AutoUtil.autoCreateTimeSeries(new HashSet<Class<?>>(){{
                context.beanBuilderAdd(TimeSeries.class, (clz, bw, anno) -> add(bw.clz()));
            }}, mongoPlusClient);
        }
    }

    /**
     * 自动创建序列
     * @author anwen
     * @date 2024/8/28 11:16
     */
    public void autoCreateIndexes(AppContext context){
        if (mongoDBConfigurationProperty.getAutoCreateIndex()) {
            AutoUtil.autoCreateIndexes(new HashSet<Class<?>>(){{
                context.beanBuilderAdd(CollectionName.class, (clz, bw, anno) -> add(bw.clz()));
            }}, mongoPlusClient);
        }
    }

    /**
     * 设置id生成器
     *
     * @author anwen
     * @date 2024/5/30 下午1:35
     */
    public void setIdGenerateHandler(AppContext context) {
        IdGenerateHandler idGenerateHandler = new AbstractIdGenerateHandler(mongoPlusClient) {};
        try {
            IdGenerateHandler userHandler = context.getBean(IdGenerateHandler.class);
            if (userHandler != null) {
                idGenerateHandler = userHandler;
            }
        } catch (Exception ignored) {}
        HandlerCache.idGenerateHandler  = idGenerateHandler;
    }

    /**
     * 设置编解码器
     * @author anwen
     * @date 2024/11/7 17:15
     */
    public void setMongoPlusCodec(AppContext context){
        context.getBeansOfType(MongoPlusCodec.class).forEach(MongoPlusCodecCache::addCodec);
    }

}
