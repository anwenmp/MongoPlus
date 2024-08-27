package com.anwen.mongo.config;

import com.anwen.mongo.annotation.collection.CollectionName;
import com.anwen.mongo.annotation.collection.TimeSeries;
import com.anwen.mongo.aware.Aware;
import com.anwen.mongo.cache.global.*;
import com.anwen.mongo.domain.MongoPlusConvertException;
import com.anwen.mongo.handlers.CollectionNameHandler;
import com.anwen.mongo.handlers.MetaObjectHandler;
import com.anwen.mongo.handlers.TenantHandler;
import com.anwen.mongo.handlers.collection.AnnotationOperate;
import com.anwen.mongo.incrementer.IdentifierGenerator;
import com.anwen.mongo.incrementer.id.IdWorker;
import com.anwen.mongo.interceptor.Interceptor;
import com.anwen.mongo.interceptor.business.DynamicCollectionNameInterceptor;
import com.anwen.mongo.interceptor.business.TenantInterceptor;
import com.anwen.mongo.listener.Listener;
import com.anwen.mongo.listener.business.BlockAttackInnerListener;
import com.anwen.mongo.listener.business.LogListener;
import com.anwen.mongo.logging.Log;
import com.anwen.mongo.logging.LogFactory;
import com.anwen.mongo.logic.LogicNamespaceAware;
import com.anwen.mongo.manager.MongoPlusClient;
import com.anwen.mongo.mapper.BaseMapper;
import com.anwen.mongo.mapping.TypeInformation;
import com.anwen.mongo.model.IndexMetaObject;
import com.anwen.mongo.property.MongoDBCollectionProperty;
import com.anwen.mongo.property.MongoDBConfigurationProperty;
import com.anwen.mongo.property.MongoDBLogProperty;
import com.anwen.mongo.property.MongoLogicDelProperty;
import com.anwen.mongo.replacer.Replacer;
import com.anwen.mongo.service.IService;
import com.anwen.mongo.service.impl.ServiceImpl;
import com.anwen.mongo.strategy.conversion.ConversionStrategy;
import com.anwen.mongo.strategy.mapping.MappingStrategy;
import com.anwen.mongo.toolkit.CollUtil;
import com.anwen.mongo.toolkit.IndexUtil;
import com.anwen.mongo.toolkit.StringUtils;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.domain.EntityScanner;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.anwen.mongo.toolkit.ClassTypeUtil.getFieldNameAndCheck;

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
        setReplacer();
        setMapping();
        setIdGenerator();
        setTenantHandler();
        setDynamicCollectionHandler();
        setAware(applicationContext);
        collectionNameConvert();
        autoCreateTimeSeries();
        autoCreateIndexes();
    }

    @Override
    public void afterPropertiesSet() {
        Collection<IService> values = applicationContext.getBeansOfType(IService.class).values();
        values.forEach(s -> setExecute((ServiceImpl<?>) s, s.getGenericityClass()));
        setLogicFiled(values.stream().map(IService::getGenericityClass).toArray(Class[]::new));
    }

    /**
     * 设置感知类
     *
     * @author loser
     */
    public void setAware(ApplicationContext applicationContext) {

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

    private void setExecute(ServiceImpl<?> serviceImpl, Class<?> clazz) {
        serviceImpl.setClazz(clazz);
        serviceImpl.setBaseMapper(baseMapper);
    }


    /**
     * 从Bean中拿到转换器
     *
     * @author JiaChaoYang
     * @date 2023/10/19 12:49
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
     * @date 2023/11/21 12:18
     */
    private void setMetaObjectHandler() {
        applicationContext.getBeansOfType(MetaObjectHandler.class).values().forEach(metaObjectHandler -> HandlerCache.metaObjectHandler = metaObjectHandler);
    }

    /**
     * 从Bean中拿到监听器
     *
     * @author JiaChaoYang
     * @date 2023/11/22 18:39
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
     * @date 2024/3/17 0:30
     */
    private void setInterceptor() {
        Collection<Interceptor> interceptorCollection = applicationContext.getBeansOfType(Interceptor.class).values();
        if (CollUtil.isNotEmpty(interceptorCollection)) {
            interceptorCollection = interceptorCollection.stream().sorted(Comparator.comparing(Interceptor::order)).collect(Collectors.toList());
        }
        InterceptorCache.interceptors = new ArrayList<>(interceptorCollection);
    }

    /**
     * 从bean 容器中获取替换器
     *
     * @author loser
     */
    private void setReplacer() {
        Collection<Replacer> replacers = applicationContext.getBeansOfType(Replacer.class).values();
        if (CollUtil.isNotEmpty(replacers)) {
            replacers = replacers.stream().sorted(Comparator.comparing(Replacer::order)).collect(Collectors.toList());
        }
        ExecutorReplacerCache.replacers = new ArrayList<>(replacers);
    }

    /**
     * 从Bean中拿到映射器
     *
     * @author JiaChaoYang
     * @date 2024/3/17 0:30
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
     * @date 2024/5/30 下午1:35
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
     * @date 2024/6/27 下午12:44
     */
    private void setTenantHandler() {
        TenantHandler tenantHandler = null;
        try {
            tenantHandler = applicationContext.getBean(TenantHandler.class);
        } catch (Exception ignored) {
        }
        if (tenantHandler != null) {
            InterceptorCache.interceptors.add(new TenantInterceptor(tenantHandler));
        }
    }

    /**
     * 动态集合名拦截器
     *
     * @author anwen
     * @date 2024/6/27 下午3:47
     */
    private void setDynamicCollectionHandler() {
        CollectionNameHandler collectionNameHandler = null;
        try {
            collectionNameHandler = applicationContext.getBean(CollectionNameHandler.class);
        } catch (Exception ignored) {
        }
        if (collectionNameHandler != null) {
            InterceptorCache.interceptors.add(new DynamicCollectionNameInterceptor(
                    collectionNameHandler,
                    baseMapper.getMongoPlusClient()
            ));
        }
    }

    /**
     * 注册集合名转换器
     *
     * @author anwen
     * @date 2024/5/27 下午11:20
     */
    public void collectionNameConvert() {
        AnnotationOperate.setCollectionNameConvertEnum(mongodbCollectionProperty.getMappingStrategy());
    }

    /**
     * 自动创建时间序列
     * @date 2024/8/27 15:42
     */
    public void autoCreateTimeSeries(){
        if (mongoDBConfigurationProperty.getAutoCreateTimeSeries()){
            Set<Class<?>> collectionClassSet;
            try {
                collectionClassSet = new EntityScanner(applicationContext).scan(TimeSeries.class);
            } catch (ClassNotFoundException e) {
                collectionClassSet = Collections.emptySet();
            }
            if (CollUtil.isEmpty(collectionClassSet)) {
                return;
            }
            collectionClassSet.forEach(collectionClass -> {
                TimeSeries timeSeries = collectionClass.getAnnotation(TimeSeries.class);
                String dataSource = DataSourceNameCache.getDataSource();
                if (StringUtils.isNotBlank(timeSeries.dataSource())){
                    dataSource = timeSeries.dataSource();
                }
                MongoClient mongoClient = mongoPlusClient.getMongoClient(dataSource);
                MongoDatabase mongoDatabase = mongoClient.getDatabase(mongoPlusClient.getDatabase(collectionClass));
                Document paramDocument = new Document();
                paramDocument.put("listCollections",1);
                paramDocument.put("filter",new Document("type","timeseries"));
                Document document = mongoDatabase.runCommand(paramDocument);
                List<String> timeSeriesList = document.get("cursor", Document.class)
                        .getList("firstBatch", Document.class)
                        .stream().map(doc -> doc.getString("name"))
                        .collect(Collectors.toList());
                String collectionName = AnnotationOperate.getCollectionName(collectionClass);
                if (timeSeriesList.contains(collectionName)){
                    log.warn("The {} temporal collection already exists",collectionName);
                    return;
                }
                TypeInformation typeInformation = TypeInformation.of(collectionClass);
                TimeSeriesOptions options = new TimeSeriesOptions(getFieldNameAndCheck(typeInformation,timeSeries.timeField()));
                options.granularity(timeSeries.granularity());
                if (StringUtils.isNotBlank(timeSeries.metaField())){
                    options.metaField(getFieldNameAndCheck(typeInformation,timeSeries.metaField()));
                }
                if (timeSeries.bucketMaxSpan() > 0){
                    options.bucketMaxSpan(timeSeries.bucketMaxSpan(), TimeUnit.SECONDS);
                    options.metaField(null);
                }
                if (timeSeries.bucketRounding() > 0){
                    options.bucketRounding(timeSeries.bucketRounding(), TimeUnit.SECONDS);
                    options.metaField(null);
                }
                CreateCollectionOptions createCollectionOptions = new CreateCollectionOptions();
                createCollectionOptions.timeSeriesOptions(options);
                if (timeSeries.expireAfter() > 0){
                    createCollectionOptions.expireAfter(timeSeries.expireAfter(), TimeUnit.SECONDS);
                }
                try {
                    mongoDatabase.createCollection(
                            collectionName,
                            createCollectionOptions);
                } catch (MongoCommandException ignored){}
            });
        }
    }

    /**
     * 扫描索引并创建
     * @author anwen
     * @date 2024/8/18 19:59
     */
    public void autoCreateIndexes() {
        if (mongoDBConfigurationProperty.getAutoCreateIndex()) {
            Set<Class<?>> collectionClassSet;
            try {
                collectionClassSet = new EntityScanner(applicationContext).scan(CollectionName.class);
            } catch (ClassNotFoundException e) {
                collectionClassSet = Collections.emptySet();
            }
            if (CollUtil.isEmpty(collectionClassSet)) {
                return;
            }
            List<IndexMetaObject> indexMetaObjectList = IndexUtil.getIndex(collectionClassSet);
            if (CollUtil.isNotEmpty(indexMetaObjectList)) {
                indexMetaObjectList.forEach(indexMetaObject -> {
                    if (CollUtil.isNotEmpty(indexMetaObject.getIndexModels())){
                        String dataSource = DataSourceNameCache.getDataSource();
                        if (StringUtils.isNotBlank(indexMetaObject.getDataSource())){
                            dataSource = indexMetaObject.getDataSource();
                        }
                        Class<?> clazz = indexMetaObject.getTypeInformation().getClazz();
                        MongoCollection<Document> collection = mongoPlusClient.getCollectionManager(dataSource,clazz)
                                .getCollection(clazz);
                        collection.createIndexes(indexMetaObject.getIndexModels());
                    }
                });
            }
        }
    }

}
