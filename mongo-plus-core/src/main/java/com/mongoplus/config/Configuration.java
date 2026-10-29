package com.mongoplus.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.annotation.collection.CollectionLogic;
import com.mongoplus.annotation.logice.IgnoreLogic;
import com.mongoplus.aware.Aware;
import com.mongoplus.cache.codec.MongoPlusCodecCache;
import com.mongoplus.cache.global.*;
import com.mongoplus.codecs.MongoPlusCodec;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.domain.InitMongoLogicException;
import com.mongoplus.domain.InitMongoPlusException;
import com.mongoplus.enums.CollectionNameConvertEnum;
import com.mongoplus.enums.BannerType;
import com.mongoplus.enums.LogicDataType;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.IdGenerateHandler;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.handlers.TenantHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.handlers.collection.AnnotationHandler;
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
import com.mongoplus.manager.DataSourceManager;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.mapper.DefaultBaseMapperImpl;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.mapping.SimpleTypeHolder;
import com.mongoplus.meta.MongoPlusBanner;
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
 * <p>全局属性和扩展注册在当前 JVM 中共享；连接、命名数据源、转换器和 Banner 由当前配置保存。
 * 应在首次使用实体映射、日志或客户端前完成配置。
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
     * 调用方提供的默认客户端；未设置时根据连接属性创建。
     */
    private MongoClient mongoClient;

    /**
     * 当前配置构建的客户端，供 Mapper 和数据源管理器复用。
     */
    private MongoPlusClient mongoPlusClient;

    /**
     * 自定义映射转换器，未设置时延迟创建默认实现。
     */
    private MongoConverter mongoConverter;

    private final Map<String, BaseProperty> dataSourceProperties = new LinkedHashMap<>();

    private final Map<String, MongoClient> dataSourceClients = new LinkedHashMap<>();

    /**
     * core 默认不打印 Banner，保留已有编程式启动行为。
     */
    private boolean banner;

    private boolean idGenerateHandlerConfigured;


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
        this.baseProperty.setUrl(url);
        this.mongoPlusClient = null;
        return this;
    }

    /**
     * 设置属性配置文件
     *
     * @author JiaChaoYang
     */
    public Configuration connection(BaseProperty baseProperty) {
        this.baseProperty = Objects.requireNonNull(baseProperty, "BaseProperty must not be null");
        UrlJoint urlJoint = new UrlJoint(baseProperty);
        this.url = urlJoint.jointMongoUrl();
        this.mongoPlusClient = null;
        return this;
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
        this.mongoPlusClient = null;
        return this;
    }

    /**
     * 使用调用方创建的默认 MongoClient，仍需配置业务 database，无需再提供连接 URL。
     * Codec 和命令监听器须由调用方在创建该客户端时配置。
     *
     * @param mongoClient 默认 MongoClient，不得为 null
     * @return 当前配置对象
     */
    public Configuration mongoClient(MongoClient mongoClient) {
        this.mongoClient = Objects.requireNonNull(mongoClient, "MongoClient must not be null");
        this.mongoPlusClient = null;
        return this;
    }

    /**
     * 配置命名数据源，在构建客户端时按连接属性创建 MongoClient。
     *
     * @param dataSource 数据源名，不得为空或使用默认名称 master
     * @param property 连接属性，必须配置业务 database
     * @return 当前配置对象
     */
    public Configuration dataSource(String dataSource, BaseProperty property) {
        if (StringUtils.isBlank(dataSource) || DataSourceConstant.DEFAULT_DATASOURCE.equals(dataSource)) {
            throw new IllegalArgumentException("Named data source must not be blank or master");
        }
        validateDatabase(property);
        dataSourceProperties.put(dataSource, property);
        dataSourceClients.remove(dataSource);
        this.mongoPlusClient = null;
        return this;
    }

    /**
     * 使用现有 MongoClient 配置命名数据源，同时登记数据库和连接属性。
     *
     * @param dataSource 数据源名，不得为空或使用默认名称 master
     * @param mongoClient 调用方创建的客户端，不得为 null
     * @param property 数据源属性，必须配置业务 database
     * @return 当前配置对象
     */
    public Configuration dataSource(String dataSource, MongoClient mongoClient, BaseProperty property) {
        Objects.requireNonNull(mongoClient, "MongoClient must not be null");
        dataSource(dataSource, property);
        dataSourceClients.put(dataSource, mongoClient);
        return this;
    }

    /**
     * 注册 Driver 编解码器，应在本配置创建 MongoClient 前调用。
     * 与 starter 共用全局 Codec 缓存，不会修改已创建的客户端。
     *
     * @param codecs 编解码器实例，不得包含 null
     * @return 当前配置对象
     */
    public Configuration codec(MongoPlusCodec<?>... codecs) {
        Objects.requireNonNull(codecs, "Codecs must not be null");
        Arrays.stream(codecs).forEach(codec -> Objects.requireNonNull(codec, "Codec must not be null"));
        MongoPlusCodecCache.addAllCodec(Arrays.asList(codecs));
        return this;
    }

    /**
     * 设置简单类型容器，必须在创建 MappingMongoConverter 前调用。
     *
     * @param simpleTypeHolder 简单类型容器，不得为 null
     * @return 当前配置对象
     */
    public Configuration simpleTypeHolder(SimpleTypeHolder simpleTypeHolder) {
        SimpleCache.setSimpleTypeHolder(Objects.requireNonNull(simpleTypeHolder, "SimpleTypeHolder must not be null"));
        return this;
    }

    /**
     * 设置当前配置创建 Mapper 时使用的映射转换器。
     * 显式调用 getBaseMapper(MongoConverter) 时，以该次传入的转换器为准。
     *
     * @param mongoConverter 转换器，不得为 null
     * @return 当前配置对象
     */
    public Configuration mongoConverter(MongoConverter mongoConverter) {
        this.mongoConverter = Objects.requireNonNull(mongoConverter, "MongoConverter must not be null");
        return this;
    }

    /**
     * 设置全局集合注解属性处理器，可用于动态解析集合名、数据库等注解属性。
     *
     * @param annotationHandler 注解属性处理器，不得为 null
     * @return 当前配置对象
     */
    public Configuration annotationHandler(AnnotationHandler annotationHandler) {
        AnnotationOperate.setAnnotationHandler(Objects.requireNonNull(annotationHandler,
                "AnnotationHandler must not be null"));
        return this;
    }

    /**
     * 设置构建客户端时是否打印 Banner，core 默认 false；样式沿用 ikun 配置。
     * starter 自行管理其 Banner，此开关仅用于 getMongoPlusClient() 的编程式启动。
     *
     * @param banner 是否打印 Banner
     * @return 当前配置对象
     */
    public Configuration banner(boolean banner) {
        this.banner = banner;
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
     * 设置字段名是否在写入时由驼峰转为下划线，读取时反向映射，默认 false。
     *
     * @param camelToUnderline 是否启用双向字段名转换
     * @return 当前配置对象
     */
    public Configuration camelToUnderline(boolean camelToUnderline) {
        PropertyCache.camelToUnderline = camelToUnderline;
        return this;
    }

    /**
     * 设置写入实体或 Map 时是否忽略 null 属性，默认 true。
     * 实体字段上的 {@code @CollectionField(ignoreNull = false)} 可单独保留 null。
     *
     * @param ignoringNull 是否忽略 null 属性
     * @return 当前配置对象
     */
    public Configuration ignoringNull(boolean ignoringNull) {
        PropertyCache.ignoringNull = ignoringNull;
        return this;
    }

    /**
     * 设置字段及关联值中的合法 ObjectId 字符串是否自动转换为 ObjectId，默认 true。
     *
     * @param autoConvertObjectId 是否自动转换 ObjectId
     * @return 当前配置对象
     */
    public Configuration autoConvertObjectId(boolean autoConvertObjectId) {
        PropertyCache.autoConvertObjectId = autoConvertObjectId;
        return this;
    }

    /**
     * 设置自动生成的合法 ObjectId 字符串是否以 BSON ObjectId 写入，默认 false。
     * 此开关沿用 ID 写入流程，不改变已有的实体 ID 回填规则。
     *
     * @param objectIdConvertType 是否转换自动生成的 ID
     * @return 当前配置对象
     */
    public Configuration objectIdConvertType(boolean objectIdConvertType) {
        PropertyCache.objectIdConvertType = objectIdConvertType;
        return this;
    }

    /**
     * 设置存放自增 ID 的集合名称，默认 {@code counters}。
     *
     * @param autoIdCollectionName 自增 ID 集合名称
     * @return 当前配置对象
     */
    public Configuration autoIdCollectionName(String autoIdCollectionName) {
        PropertyCache.autoIdCollectionName = autoIdCollectionName;
        return this;
    }

    /**
     * 设置是否开启小黑子日志模式，默认 false，应在日志实现初始化前设置。
     *
     * @param ikun 是否开启小黑子模式
     * @return 当前配置对象
     */
    public Configuration ikun(boolean ikun) {
        PropertyCache.ikun = ikun;
        return this;
    }

    /**
     * 设置全局对称加密密钥，与 starter 的 {@code mongo-plus.encryptor.key} 使用相同缓存。
     *
     * @param key 对称加密密钥
     * @return 当前配置对象
     */
    public Configuration key(String key) {
        PropertyCache.key = key;
        return this;
    }

    /**
     * 设置全局非对称加密公钥。
     *
     * @param publicKey 非对称加密公钥，沿用现有算法的十六进制编码
     * @return 当前配置对象
     */
    public Configuration publicKey(String publicKey) {
        PropertyCache.publicKey = publicKey;
        return this;
    }

    /**
     * 设置全局非对称加密私钥。
     *
     * @param privateKey 非对称加密私钥，沿用现有算法的十六进制编码
     * @return 当前配置对象
     */
    public Configuration privateKey(String privateKey) {
        PropertyCache.privateKey = privateKey;
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
        return log(false);
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
        ListenerCache.sorted();
        return this;
    }

    /**
     * 设置日志监听器顺序，默认 0，数值越小越先执行。
     * 已注册的监听器也会按新顺序重新排序。
     *
     * @param logOrder 日志监听器顺序
     * @return 当前配置对象
     */
    public Configuration logOrder(int logOrder) {
        OrderCache.LOG_ORDER = logOrder;
        ListenerCache.sorted();
        return this;
    }

    /**
     * 开启防攻击
     *
     * @author JiaChaoYang
     */
    public Configuration blockAttackInner() {
        ListenerCache.listeners.add(new BlockAttackInnerListener());
        ListenerCache.sorted();
        return this;
    }

    /**
     * 设置全集合更新、删除保护监听器顺序，默认 1，数值越小越先执行。
     * 已注册的监听器也会按新顺序重新排序。
     *
     * @param blockAttackInnerOrder 全集合更新、删除保护监听器顺序
     * @return 当前配置对象
     */
    public Configuration blockAttackInnerOrder(int blockAttackInnerOrder) {
        OrderCache.BLOCK_ATTACK_INNER_ORDER = blockAttackInnerOrder;
        ListenerCache.sorted();
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
        ListenerCache.sorted();
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
        if (mongoPlusClient != null) {
            return mongoPlusClient;
        }
        if (mongoClient == null && StringUtils.isBlank(url)) {
            throw new InitMongoPlusException("Connection URL not configured");
        }
        validateDatabase(baseProperty);
        // 先检查全部数据库配置，避免已经创建默认客户端后才发现命名数据源缺少 database。
        dataSourceProperties.values().forEach(Configuration::validateDatabase);
        MongoPlusClient initializedClient = initMongoPlusClient();
        dataSourceProperties.forEach((dataSource, property) -> {
            MongoClient client = dataSourceClients.get(dataSource);
            if (client == null) {
                client = MongoUtil.getMongo(dataSource, property);
            }
            MongoClientFactory.getInstance(dataSource, client);
            DataSourceNameCache.setBaseProperty(dataSource, property);
            Map<String, CollectionManager> managers = new LinkedHashMap<>();
            Arrays.stream(property.getDatabase().split(","))
                    .forEach(database -> managers.put(database, new CollectionManager(database)));
            initializedClient.getCollectionManagers().put(dataSource, managers);
        });
        if (!idGenerateHandlerConfigured) {
            HandlerCache.idGenerateHandler = new AbstractIdGenerateHandler(initializedClient) {};
        }
        MongoPlusBanner.printBanner(banner, PropertyCache.ikun ? BannerType.IKUN : BannerType.DEFAULT);
        this.mongoPlusClient = initializedClient;
        return mongoPlusClient;
    }

    public MongoPlusClient initMongoPlusClient() {
        validateDatabase(baseProperty);
        MongoClient client = mongoClient != null ? mongoClient
                : MongoUtil.getMongo(DataSourceConstant.DEFAULT_DATASOURCE, baseProperty);
        return initMongoPlusClient(client, baseProperty);
    }

    public MongoPlusClient initMongoPlusClient(BaseProperty baseProperty) {
        validateDatabase(baseProperty);
        return initMongoPlusClient(MongoUtil.getMongo(DataSourceConstant.DEFAULT_DATASOURCE, baseProperty), baseProperty);
    }

    public MongoPlusClient initMongoPlusClient(MongoClient mongoClient, BaseProperty baseProperty) {
        validateDatabase(baseProperty);
        Objects.requireNonNull(mongoClient, "MongoClient must not be null");
        MongoClientFactory.getInstance(mongoClient);
        // 外部传入的客户端未经过 MongoUtil，同样需要登记默认数据源的数据库元数据。
        DataSourceNameCache.setBaseProperty(DataSourceConstant.DEFAULT_DATASOURCE, baseProperty);
        MongoPlusClient mongoPlusClient = new MongoPlusClient();
        mongoPlusClient.setBaseProperty(baseProperty);
        List<MongoDatabase> mongoDatabaseList = new ArrayList<>();
        Map<String, Map<String, CollectionManager>> collectionManagers = new ConcurrentHashMap<>();
        Map<String, CollectionManager> defaultManagers = new LinkedHashMap<>();
        Arrays.stream(baseProperty.getDatabase().split(",")).forEach(database -> {
            defaultManagers.put(database, new CollectionManager(database));
            // 初始化默认数据源时直接使用传入客户端，不受当前线程切换的数据源影响。
            mongoDatabaseList.add(mongoClient.getDatabase(database));
        });
        collectionManagers.put(DataSourceConstant.DEFAULT_DATASOURCE, defaultManagers);
        mongoPlusClient.setCollectionManagerMap(collectionManagers);
        mongoPlusClient.setMongoDatabase(mongoDatabaseList);
        MongoPlusClientCache.mongoPlusClient = mongoPlusClient;
        return mongoPlusClient;
    }

    private static void validateDatabase(BaseProperty baseProperty) {
        Objects.requireNonNull(baseProperty, "BaseProperty must not be null");
        if (StringUtils.isBlank(baseProperty.getDatabase())) {
            throw new InitMongoPlusException("Connection database not configured");
        }
    }

    /**
     * 获取与当前配置的客户端配套的数据源管理器，复用已构建的客户端。
     *
     * @return 数据源管理器
     */
    public DataSourceManager getDataSourceManager() {
        return new DataSourceManager(getMongoPlusClient(), MongoClientFactory.getInstance());
    }

    /**
     * 设置数据源
     *
     * @author JiaChaoYang
     */
    public void setOtherDataSource(Map<String, MongoClient> mongoClientMap) {
        MongoClientFactory.getInstance(mongoClientMap);
        this.mongoPlusClient = null;
    }

    /**
     * 设置id处理器
     * @author anwen
     */
    public Configuration idGenerateHandler(IdGenerateHandler idGenerateHandler){
        HandlerCache.idGenerateHandler = idGenerateHandler;
        this.idGenerateHandlerConfigured = idGenerateHandler != null;
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
        return getBaseMapper(mongoConverter != null ? mongoConverter : new MappingMongoConverter());
    }

    public BaseMapper getBaseMapper(MongoConverter mongoConverter) {
        return new DefaultBaseMapperImpl(getMongoPlusClient(), mongoConverter);
    }

}
