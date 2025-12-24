package com.mongoplus.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.annotation.collection.CollectionLogic;
import com.mongoplus.annotation.logice.IgnoreLogic;
import com.mongoplus.aware.Aware;
import com.mongoplus.cache.global.*;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.domain.InitMongoLogicException;
import com.mongoplus.domain.InitMongoPlusException;
import com.mongoplus.enums.CollectionNameConvertEnum;
import com.mongoplus.enums.LogicDataType;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.IdGenerateHandler;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.handlers.TenantHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.incrementer.id.AbstractIdGenerateHandler;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.AdvancedInterceptorChain;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.business.*;
import com.mongoplus.listener.Listener;
import com.mongoplus.listener.business.BlockAttackInnerListener;
import com.mongoplus.listener.business.LogListener;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.DefaultBaseMapperImpl;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.LogicProperty;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.MongoUtil;
import com.mongoplus.toolkit.StringUtils;
import com.mongoplus.toolkit.UrlJoint;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MongoPlus配置
 *
 * @author JiaChaoYang
 **/
@SuppressWarnings({"unused","UnusedReturnValue"})
public class Configuration {

    /**
     * MongoDB连接URL
     *
     */
    private String url;

    /**
     * 属性配置文件，url和baseProperty存在一个即可
     *
     */
    private BaseProperty baseProperty = new BaseProperty();

    /**
     * 逻辑删除配置
     */
    private LogicProperty logicProperty = new LogicProperty();


    /**
     * 获取一个空的Configuration
     *
     * @author JiaChaoYang
     */
    public static Configuration builder() {
        return new Configuration();
    }

    /**
     * 设置url
     *
     * @author JiaChaoYang
     */
    public Configuration connection(String url) {
        this.url = url;
        return this;
    }

    /**
     * 设置属性配置文件
     *
     * @author JiaChaoYang
     */
    public Configuration connection(BaseProperty baseProperty) {
        this.baseProperty = baseProperty;
        UrlJoint urlJoint = new UrlJoint(baseProperty);
        return connection(urlJoint.jointMongoUrl());
    }

    /**
     * 配置数据库
     *
     * @param database 数据库 多个库使用逗号隔开
     * @return com.mongoplus.config.Configuration
     * @author JiaChaoYang
     */
    public Configuration database(String database) {
        this.baseProperty.setDatabase(database);
        return this;
    }

    /**
     * 设置集合名称获取策略
     *
     * @author JiaChaoYang
     */
    public Configuration collectionNameConvert(CollectionNameConvertEnum collectionNameConvertEnum) {
        AnnotationOperate.setCollectionNameConvertEnum(collectionNameConvertEnum);
        return this;
    }

    /**
     * 设置转换器
     *
     * @param clazzConversions 转换器类
     * @return com.mongoplus.config.Configuration
     * @author JiaChaoYang
     */
    @SafeVarargs
    public final Configuration convert(Class<? extends ConversionStrategy<?>>... clazzConversions) {
        for (Class<? extends ConversionStrategy<?>> clazzConversion : clazzConversions) {
            Type[] genericInterfaces = clazzConversion.getGenericInterfaces();
            for (Type anInterface : genericInterfaces) {
                ParameterizedType parameterizedType = (ParameterizedType) anInterface;
                if (parameterizedType.getRawType().equals(ConversionStrategy.class)) {
                    Class<?> clazz = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                    ConversionCache.putConversionStrategy(clazz, (ConversionStrategy<?>)ClassTypeUtil.getInstanceByClass(clazzConversion));
                    break;
                }
            }
        }
        return this;
    }

    /**
     * 设置自动填充
     *
     * @param metaObjectHandler 元数据填充
     * @return com.mongoplus.config.Configuration
     * @author JiaChaoYang
     */
    public Configuration metaObjectHandler(MetaObjectHandler metaObjectHandler) {
        HandlerCache.metaObjectHandler = metaObjectHandler;
        return this;
    }

    /**
     * 开启日志打印
     *
     * @author JiaChaoYang
     */
    public Configuration log() {
        ListenerCache.listeners.add(new LogListener());
        return this;
    }

    /**
     * 开启日志打印
     *
     * @param pretty 是否将 mongo 语句格式化可执行语句
     * @author loser
     */
    public Configuration log(Boolean pretty) {
        ListenerCache.listeners.add(new LogListener(pretty));
        PropertyCache.log = true;
        return this;
    }

    /**
     * 开启防攻击
     *
     * @author JiaChaoYang
     */
    public Configuration blockAttackInner() {
        ListenerCache.listeners.add(new BlockAttackInnerListener());
        return this;
    }

    /**
     * 设置监听器
     *
     * @param listeners 监听器
     * @return com.mongoplus.config.Configuration
     * @author JiaChaoYang
     */
    @SafeVarargs
    public final Configuration listener(Class<? extends Listener>... listeners) {
        for (Class<? extends Listener> listener : listeners) {
            ListenerCache.listeners.add((Listener) ClassTypeUtil.getInstanceByClass(listener));
        }
        return this;
    }

    /**
     * 设置拦截器
     *
     * @param interceptors 拦截器
     * @return com.mongoplus.config.Configuration
     * @author JiaChaoYang
     */
    @SafeVarargs
    public final Configuration interceptor(Class<? extends Interceptor>... interceptors) {
        for (Class<? extends Interceptor> interceptor : interceptors) {
            InterceptorChain.addInterceptor((Interceptor) ClassTypeUtil.getInstanceByClass(interceptor));
        }
        return this;
    }

    /**
     * 设置高级拦截器
     * @param interceptors 拦截器类数组
     * @return {@link com.mongoplus.config.Configuration}
     * @author anwen
     */
    @SafeVarargs
    public final Configuration advancedInterceptor(Class<? extends AdvancedInterceptor>... interceptors){
        return advancedInterceptor(Arrays.stream(interceptors)
                .map(clazz ->
                        (AdvancedInterceptor) ClassTypeUtil.getInstanceByClass(clazz))
                .collect(Collectors.toList()));
    }

    /**
     * 设置高级拦截器
     * @param interceptors 拦截器类集合
     * @return {@link com.mongoplus.config.Configuration}
     * @author anwen
     */
    public final Configuration advancedInterceptor(List<AdvancedInterceptor> interceptors){
        AdvancedInterceptorChain.addInterceptors(interceptors);
        return this;
    }

    /**
     * 设置多租户处理器
     *
     * @author anwen
     */
    public Configuration tenantHandler(TenantHandler tenantHandler) {
        InterceptorChain.addInterceptor(new TenantInterceptor(tenantHandler));
        return this;
    }

    /**
     * 获取MongoPlusClient
     *
     * @author JiaChaoYang
     */
    public MongoPlusClient getMongoPlusClient() {
        if (StringUtils.isBlank(url)) {
            throw new InitMongoPlusException("Connection URL not configured");
        }
        if (StringUtils.isBlank(baseProperty.getDatabase())) {
            throw new InitMongoPlusException("Connection database not configured");
        }
        MongoPlusClient mongoPlusClient = initMongoPlusClient();
        idGenerateHandler(new AbstractIdGenerateHandler(mongoPlusClient) {});
        return mongoPlusClient;
    }

    public MongoPlusClient initMongoPlusClient() {
        return initMongoPlusClient(MongoUtil.getMongo(DataSourceConstant.DEFAULT_DATASOURCE,baseProperty),baseProperty);
    }

    public MongoPlusClient initMongoPlusClient(BaseProperty baseProperty) {
        return initMongoPlusClient(MongoUtil.getMongo(DataSourceConstant.DEFAULT_DATASOURCE, baseProperty), baseProperty);
    }

    public MongoPlusClient initMongoPlusClient(MongoClient mongoClient, BaseProperty baseProperty) {
        if (StringUtils.isBlank(baseProperty.getDatabase())) {
            throw new InitMongoPlusException("Connection database not configured");
        }
        MongoClientFactory.getInstance(mongoClient);
        MongoPlusClient mongoPlusClient = new MongoPlusClient();
        mongoPlusClient.setBaseProperty(baseProperty);
        List<MongoDatabase> mongoDatabaseList = new ArrayList<>();
        mongoPlusClient.setCollectionManagerMap(new ConcurrentHashMap<String, Map<String, CollectionManager>>() {{
            put(DataSourceConstant.DEFAULT_DATASOURCE, new LinkedHashMap<String, CollectionManager>() {{
                String database = mongoPlusClient.getBaseProperty().getDatabase();
                Arrays.stream(database.split(",")).collect(Collectors.toList()).forEach(db -> {
                    CollectionManager collectionManager = new CollectionManager(db);
                    MongoDatabase mongoDatabase = mongoPlusClient.getMongoClient().getDatabase(db);
                    mongoDatabaseList.add(mongoDatabase);
                    put(db, collectionManager);
                });
            }});
        }});
        mongoPlusClient.setMongoDatabase(mongoDatabaseList);
        MongoPlusClientCache.mongoPlusClient = mongoPlusClient;
        return mongoPlusClient;
    }

    /**
     * 设置数据源
     *
     * @author JiaChaoYang
     */
    public void setOtherDataSource(Map<String, MongoClient> mongoClientMap) {
        MongoClientFactory.getInstance(mongoClientMap);
    }

    /**
     * 设置id处理器
     * @author anwen
     */
    public Configuration idGenerateHandler(IdGenerateHandler idGenerateHandler){
        HandlerCache.idGenerateHandler = idGenerateHandler;
        return this;
    }

    /**
     * 设置数据变动记录
     * @return {@link com.mongoplus.config.Configuration}
     * @author anwen
     */
    public Configuration dataChangeRecorder(DataChangeRecorderInnerInterceptor dataChangeRecorderInnerInterceptor){
        InterceptorChain.addInterceptor(dataChangeRecorderInnerInterceptor);
        return this;
    }

    /**
     * 设置动态集合
     * @return {@link Configuration}
     * @author anwen
     */
    public Configuration dynamicCollectionName(DynamicCollectionNameInterceptor dynamicCollectionNameInterceptor){
        InterceptorChain.addInterceptor(dynamicCollectionNameInterceptor);
        return this;
    }

    /**
     * 设置异步多写
     * @return {@link com.mongoplus.config.Configuration}
     * @author anwen
     */
    public Configuration asyncMultipleWrite(AsyncMultipleWriteInterceptor multipleWriteInterceptor){
        AdvancedInterceptorChain.addInterceptor(multipleWriteInterceptor);
        return this;
    }

    /**
     * 配置逻辑删除
     *
     * @param logicProperty 逻辑删除配置
     * @return 全局配置对象
     * @author loser
     */
    public Configuration logic(LogicProperty logicProperty) {

        if (Objects.isNull(logicProperty)) {
            throw new InitMongoLogicException("Config logic logicProperty not null");
        }
        this.logicProperty = logicProperty;
        LogicManager.open = logicProperty.getOpen();
        LogicManager.logicProperty = logicProperty;
        if (logicProperty.getOpen()) {
            InterceptorChain.addInterceptor(new CollectionLogiceInterceptor());
            if (logicProperty.getAutoFill()) {
                InterceptorChain.addInterceptor(new LogicAutoFillInterceptor());
            }
            AdvancedInterceptorChain.addInterceptor(new LogicRemoveInterceptor());
        }
        return this;

    }

    /**
     * 注册逻辑删除 class
     *
     * @param collectionClasses 需要注册的 class 集合
     * @return 全局配置对象
     * @author loser
     */
    public Configuration setLogicFiled(Class<?>... collectionClasses) {
        return setLogicFiled(logicProperty, collectionClasses);
    }

    /**
     * 注册逻辑删除 class
     *
     * @param collectionClasses 需要注册的 class 集合
     * @return 全局配置对象
     * @author loser
     */
    public Configuration setLogicFiled(LogicProperty logicProperty, Class<?>... collectionClasses) {

        if (Objects.isNull(collectionClasses) || Objects.isNull(logicProperty) || !logicProperty.getOpen()) {
            return this;
        }
        Map<Class<?>, LogicDeleteResult> logicDeleteResultHashMap = LogicManager.logicDeleteResultHashMap;

        for (Class<?> clazz : collectionClasses) {
            if (logicDeleteResultHashMap.containsKey(clazz)) {
                continue;
            }
            TypeInformation typeInformation = TypeInformation.of(clazz);
            IgnoreLogic ignoreLogic = typeInformation.getAnnotation(IgnoreLogic.class);
            // 如果存在忽略逻辑删除注解
            if (Objects.nonNull(ignoreLogic)) {
                continue;
            }
            FieldInformation annotationField = typeInformation.getAnnotationField(CollectionLogic.class);
            // 优先使用每个对象自定义规则
            if (Objects.nonNull(annotationField)) {
                CollectionLogic annotation = annotationField.getAnnotation(CollectionLogic.class);
                LogicDeleteResult result = new LogicDeleteResult();
                String column = annotationField.getCamelCaseName();
                result.setColumn(column);
                result.setLogicDeleteValue(StringUtils.isNotBlank(annotation.delval()) ? annotation.delval() : logicProperty.getLogicDeleteValue());
                result.setLogicNotDeleteValue(StringUtils.isNotBlank(annotation.value()) ? annotation.value() : logicProperty.getLogicNotDeleteValue());
                result.setLogicDataType(annotation.delType() == LogicDataType.DEFAULT ? logicProperty.getLogicDataType() : annotation.delType());
                logicDeleteResultHashMap.put(clazz, result);
                continue;
            }

            // 其次使用全局配置规则
            if (StringUtils.isNotEmpty(logicProperty.getLogicDeleteField())
                    && StringUtils.isNotEmpty(logicProperty.getLogicDeleteValue())
                    && StringUtils.isNotEmpty(logicProperty.getLogicNotDeleteValue())) {
                LogicDeleteResult result = new LogicDeleteResult();
                result.setColumn(logicProperty.getLogicDeleteField());
                result.setLogicDeleteValue(logicProperty.getLogicDeleteValue());
                result.setLogicNotDeleteValue(logicProperty.getLogicNotDeleteValue());
                result.setLogicDataType(logicProperty.getLogicDataType() == LogicDataType.DEFAULT ? LogicDataType.STRING : logicProperty.getLogicDataType());
                logicDeleteResultHashMap.put(clazz, result);
                continue;
            }
            logicDeleteResultHashMap.put(clazz, null);
        }
        return this;

    }

    /**
     * 获取执行器工厂
     * @return {@link com.mongoplus.execute.ExecutorFactory}
     * @author anwen
     */
    public static ExecutorFactory getExecutorFactory(){
        return new ExecutorFactory();
    }

    /**
     * 设置感知类
     */
    public Configuration aware(Aware aware) {
        AwareHandlerCache.putAware(aware);
        return this;
    }

    /**
     * 获取BaseMapper
     *
     * @author JiaChaoYang
     */
    public BaseMapper getBaseMapper() {
        return new DefaultBaseMapperImpl(getMongoPlusClient(), new MappingMongoConverter());
    }

    public BaseMapper getBaseMapper(MongoConverter mongoConverter) {
        return new DefaultBaseMapperImpl(getMongoPlusClient(), mongoConverter);
    }

}
