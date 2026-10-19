# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 228 条完整源码记录（含内部实现）。

## 模块：mongo-plus-boot4-starter

### 包：com.mongoplus.annotation

#### annotation com.mongoplus.annotation.MongoMapperScan

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/MongoMapperScan.java:12-27

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(MongoMapperScannerRegistrar.class)
```

```text
* mapper层接口，只继承了{@link com.mongoplus.mapper.MongoMapper}接口，而且不想创建实现类，可通过注解扫描自动创建实现类
 * @author anwen
```

##### method value

```java
String[] value()
```

所属类型：com.mongoplus.annotation.MongoMapperScan；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/MongoMapperScan.java:23-23

```text
* 扫描路径
     * @return {@link String[]}
     * @author anwen
```

#### record com.mongoplus.annotation.SpelAnnotationHandler

可见性：public；泛型：[]；extends：[]；implements：[AnnotationHandler]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/SpelAnnotationHandler.java:20-67

```text
* Spel注解处理器
 *
 * @author anwen
```

##### field PARAMETER_NAME_DISCOVERER

```java
public static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER
```

所属类型：com.mongoplus.annotation.SpelAnnotationHandler；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/SpelAnnotationHandler.java:22-22

##### field EXPRESSION_PARSER

```java
public static final ExpressionParser EXPRESSION_PARSER
```

所属类型：com.mongoplus.annotation.SpelAnnotationHandler；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/SpelAnnotationHandler.java:24-24

##### method getProperty

```java
public <T, R> R getProperty(T obj, Function<? super T, ? extends R> func)
```

所属类型：com.mongoplus.annotation.SpelAnnotationHandler；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/SpelAnnotationHandler.java:26-38

```java
@Override
@SuppressWarnings("unchecked")
```

##### method needsSpelParsing

```java
private boolean needsSpelParsing(String value)
```

所属类型：com.mongoplus.annotation.SpelAnnotationHandler；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/SpelAnnotationHandler.java:47-49

```text
* 判断字符串是否需要解析Spel表达式
     *
     * @param value 注解值
     * @return {@link boolean}
     * @author anwen
```

##### method parseSpelExpression

```java
private String parseSpelExpression(String expression)
```

所属类型：com.mongoplus.annotation.SpelAnnotationHandler；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/SpelAnnotationHandler.java:58-65

```text
* 解析Spel表达式并返回结果
     *
     * @param expression 表达式
     * @return {@link String}
     * @author anwen
```

### 包：com.mongoplus.config

#### class com.mongoplus.config.MongoPlusAutoConfiguration

可见性：public；泛型：[]；extends：[]；implements：[InitializingBean]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:56-370

```text
* MongoPlus自动注入配置
 *
 * @author JiaChaoYang
 *
```

##### field applicationContext

```java
private final ApplicationContext applicationContext
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:58-58

##### field mongodbLogProperty

```java
private final MongoDBLogProperty mongodbLogProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:60-60

##### field mongodbCollectionProperty

```java
private final MongoDBCollectionProperty mongodbCollectionProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:62-62

##### field mongoLogicDelProperty

```java
private final MongoLogicDelProperty mongoLogicDelProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:64-64

##### field baseMapper

```java
private final BaseMapper baseMapper
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:66-66

##### field mongoDBConfigurationProperty

```java
private final MongoDBConfigurationProperty mongoDBConfigurationProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:68-68

##### field mongoPlusClient

```java
private final MongoPlusClient mongoPlusClient
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:70-70

##### field log

```java
Log log
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：package-private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:72-72

##### constructor MongoPlusAutoConfiguration

```java
public MongoPlusAutoConfiguration(MongoDBLogProperty mongodbLogProperty, MongoDBCollectionProperty mongodbCollectionProperty, MongoLogicDelProperty mongoLogicDelProperty, MongoDBConfigurationProperty mongoDBConfigurationProperty, BaseMapper baseMapper, MongoDBFieldProperty ignore, ApplicationContext applicationContext, MongoPlusClient mongoPlusClient)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:74-89

##### method init

```java
public void init()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:91-106

##### method afterPropertiesSet

```java
public void afterPropertiesSet()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:108-126

```java
@Override
@SuppressWarnings("rawtypes")
```

##### method setAware

```java
public void setAware()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:133-141

```text
* 设置感知类
     *
     * @author loser
```

##### method setLogicFiled

```java
private void setLogicFiled(Class<?>... collectionClasses)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:149-151

```text
* 配置逻辑删除
     *
     * @param collectionClasses 需要进行逻辑删除的 collection class 集合
     * @author loser
```

##### method setConversion

```java
private void setConversion()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:158-179

```text
* 从Bean中拿到转换器
     *
     * @author JiaChaoYang
```

##### method setMetaObjectHandler

```java
private void setMetaObjectHandler()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:186-188

```text
* 从Bean中拿到自动填充策略
     *
     * @author JiaChaoYang
```

##### method setListener

```java
private void setListener()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:195-208

```text
* 从Bean中拿到监听器
     *
     * @author JiaChaoYang
```

##### method setInterceptor

```java
private void setInterceptor()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:215-221

```text
* 从Bean中拿到拦截器
     *
     * @author JiaChaoYang
```

##### method setAdvancedInterceptor

```java
private void setAdvancedInterceptor()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:227-233

```text
* 设置高级拦截器
     * @author anwen
```

##### method setMapping

```java
private void setMapping()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:240-261

```text
* 从Bean中拿到映射器
     *
     * @author JiaChaoYang
```

##### method setIdGenerator

```java
private void setIdGenerator()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:268-273

```text
* 自定义id生成器
     *
     * @author anwen
```

##### method setTenantHandler

```java
private void setTenantHandler()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:280-289

```text
* 多租户拦截器
     *
     * @author anwen
```

##### method setDynamicCollectionHandler

```java
private void setDynamicCollectionHandler()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:296-308

```text
* 动态集合名拦截器
     *
     * @author anwen
```

##### method collectionNameConvert

```java
public void collectionNameConvert()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:315-317

```text
* 注册集合名转换器
     *
     * @author anwen
```

##### method autoCreateTimeSeries

```java
public void autoCreateTimeSeries()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:323-329

```text
* 自动创建时间序列
     *
```

##### method autoCreateIndexes

```java
public void autoCreateIndexes()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:336-342

```text
* 扫描索引并创建
     *
     * @author anwen
```

##### method getPackages

```java
public List<String> getPackages()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:344-355

##### method setIdGenerateHandler

```java
public void setIdGenerateHandler()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:362-368

```text
* 设置id生成器
     *
     * @author anwen
```

#### class com.mongoplus.config.MongoPlusConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:54-265

```java
@EnableConfigurationProperties(value = { MongoDBConnectProperty.class, MongoDBCollectionProperty.class, MongoDBConfigurationProperty.class, MongoDBLogProperty.class })
```

```text
* @author JiaChaoYang
 * 连接配置
 * @since 2023-02-09 14:27
 *
```

##### field log

```java
private final Log log
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:62-62

##### field mongoDBConnectProperty

```java
private final MongoDBConnectProperty mongoDBConnectProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:64-64

##### field mongoDBConfigurationProperty

```java
private final MongoDBConfigurationProperty mongoDBConfigurationProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:66-66

##### field mongoDBLogProperty

```java
protected final MongoDBLogProperty mongoDBLogProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:68-68

##### constructor MongoPlusConfiguration

```java
public MongoPlusConfiguration(MongoDBConnectProperty mongodbConnectProperty, MongoDBConfigurationProperty mongodbConfigurationProperty, MongoDBLogProperty mongoDBLogProperty)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:71-77

##### method mongoClientFactory

```java
public MongoClientFactory mongoClientFactory(ApplicationContext applicationContext)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:84-100

```java
@Bean
@ConditionalOnMissingBean
```

```text
* 注册MongoClient工厂
     * @return {@link MongoClientFactory}
     * @author anwen
```

##### method mongo

```java
public MongoClient mongo(MongoClientFactory mongoClientFactory)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:106-110

```java
@Bean
@ConditionalOnMissingBean
```

```text
* 这里将MongoClient注册为Bean，但是只是给MongoTemplate使用，master的client
     * @author JiaChaoYang
```

##### method mongoPlusClient

```java
public MongoPlusClient mongoPlusClient(MongoClient mongo, MongoClientFactory mongoClientFactory)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:119-143

```java
@Bean
@ConditionalOnMissingBean(MongoPlusClient.class)
```

```text
* MongoPlusClient注册
     * @param mongo MongoClient
     * @param mongoClientFactory MongoClient工厂
     * @return {@link com.mongoplus.manager.MongoPlusClient}
     * @author anwen
```

##### method simpleTypeHolder

```java
public SimpleTypeHolder simpleTypeHolder()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:150-156

```java
@Bean
@ConditionalOnMissingBean(SimpleTypeHolder.class)
```

```text
* 简单类型注册
     * @return {@link SimpleTypeHolder}
     * @author anwen
```

##### method mongoConverter

```java
public MongoConverter mongoConverter()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:163-167

```java
@Bean
@ConditionalOnMissingBean(MongoConverter.class)
```

```text
* 注册mongo转换器
     * @return {@link com.mongoplus.mapping.MongoConverter}
     * @author anwen
```

##### method mongoBaseMapper

```java
public BaseMapper mongoBaseMapper(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:176-180

```java
@Bean
@ConditionalOnMissingBean(BaseMapper.class)
```

```text
* baseMapper注册
     * @param mongoPlusClient mongoPlusClient
     * @param mongoConverter 转换器
     * @return {@link com.mongoplus.mapper.BaseMapper}
     * @author anwen
```

##### method mongoTransactionalAspect

```java
public MongoTransactionalAspect mongoTransactionalAspect(TransactionOptions transactionOptions, @Qualifier("mongoPlusTransactionalManager") ObjectProvider<PlatformTransactionManager> transactionManagerProvider)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:187-197

```java
@Bean("mongoTransactionalAspect")
@ConditionalOnMissingBean
```

```text
* 注册MongoPlus事务切面
     * @return {@link MongoTransactionalAspect}
     * @author anwen
```

##### method transactionOptions

```java
public TransactionOptions transactionOptions()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:199-203

```java
@Bean("transactionOptions")
@ConditionalOnMissingBean
```

##### method mongoDataSourceAspect

```java
public MongoDataSourceAspect mongoDataSourceAspect()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:210-214

```java
@Bean("mongoDataSourceAspect")
@ConditionalOnMissingBean
```

```text
* 注册mongoPlus多数据源切面
     * @return {@link MongoDataSourceAspect}
     * @author anwen
```

##### method mongoLogicIgnoreAspect

```java
public MongoLogicIgnoreAspect mongoLogicIgnoreAspect()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:222-226

```java
@Bean("mongoLogicIgnoreAspect")
@ConditionalOnMissingBean
```

```text
* 忽略逻辑删除
     *
     * @return {@link MongoLogicIgnoreAspect}
     * @author loser
```

##### method tenantAspect

```java
public TenantAspect tenantAspect()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:232-236

```java
@Bean("tenantAspect")
@ConditionalOnMissingBean
```

```text
* 忽略租户
     * @author anwen
```

##### method dataSourceManager

```java
public DataSourceManager dataSourceManager(MongoPlusClient mongoPlusClient, MongoClientFactory mongoClientFactory)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:245-250

```java
@Bean("dataSourceManager")
@ConditionalOnMissingBean
```

```text
* 数据源管理器
     * @param mongoPlusClient mongoPlus客户端
     * @param mongoClientFactory mongoClient工厂
     * @return {@link DataSourceManager}
     * @author anwen
```

##### method spelAnnotationHandler

```java
public SpelAnnotationHandler spelAnnotationHandler(ApplicationContext applicationContext)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:257-263

```java
@Bean
@ConditionalOnMissingBean
```

```text
* spel朱姐处理
     * @return {@link com.mongoplus.annotation.SpelAnnotationHandler}
     * @author anwen
```

#### class com.mongoplus.config.MongoPropertyConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPropertyConfiguration.java:14-45

```java
@Configuration
@ConditionalOnClass(MongoProperties.class)
```

```text
* @author JiaChaoYang
 *
```

##### field mongoDBConnectProperty

```java
private final MongoDBConnectProperty mongoDBConnectProperty
```

所属类型：com.mongoplus.config.MongoPropertyConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPropertyConfiguration.java:18-18

##### field mongoProperties

```java
private final MongoProperties mongoProperties
```

所属类型：com.mongoplus.config.MongoPropertyConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPropertyConfiguration.java:20-20

##### constructor MongoPropertyConfiguration

```java
public MongoPropertyConfiguration(MongoDBConnectProperty mongoDBConnectProperty, MongoProperties mongoProperties)
```

所属类型：com.mongoplus.config.MongoPropertyConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPropertyConfiguration.java:22-26

##### method init

```java
public void init()
```

所属类型：com.mongoplus.config.MongoPropertyConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPropertyConfiguration.java:28-43

#### class com.mongoplus.config.OverrideMongoConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/OverrideMongoConfiguration.java:16-32

```java
@AutoConfiguration(beforeName = "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration")
@ConditionalOnClass(name = "org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration")
@ConditionalOnProperty(prefix = "mongo-plus.spring", name = "overrideMongoClient", havingValue = "false")
```

```text
* 覆盖MongoTemplate的MongoClient
 * <p>在Spring Boot 4中，MongoAutoConfiguration为final类且mongo()方法使用@ConditionalOnMissingBean，
 * 因此当不需要Spring自行创建MongoClient时，提前注册一个MongoClient Bean即可阻止自动配置创建新的。</p>
 * @author JiaChaoYang
 *
```

##### method mongo

```java
public MongoClient mongo(MongoClient mongoClient)
```

所属类型：com.mongoplus.config.OverrideMongoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/OverrideMongoConfiguration.java:26-30

```java
@Bean
@Primary
```

```text
* 当 overrideMongoClient=false 时，将 MongoPlus 创建的 MongoClient 注册为 Primary Bean，
     * 使 MongoAutoConfiguration 的 @ConditionalOnMissingBean 条件不满足，从而阻止其创建新的 MongoClient。
     * 这样 MongoTemplate 将使用与 MongoPlus 相同的 MongoClient。
```

### 包：com.mongoplus.datasource

#### class com.mongoplus.datasource.MongoDataSourceAspect

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:29-78

```java
@Aspect
@Order(0)
```

```text
* 多数据源切面
 *
 * @author JiaChaoYang
 *
```

##### method manageDataSource

```java
public Object manageDataSource(ProceedingJoinPoint joinPoint) throws Throwable
```

所属类型：com.mongoplus.datasource.MongoDataSourceAspect；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33-66

```java
@Around("@within(com.mongoplus.annotation.datasource.MongoDs) || @annotation(com.mongoplus.annotation.datasource.MongoDs)")
```

##### method getMongoDsAnnotation

```java
private MongoDs getMongoDsAnnotation(Method method)
```

所属类型：com.mongoplus.datasource.MongoDataSourceAspect；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:68-76

### 包：com.mongoplus.logic

#### class com.mongoplus.logic.MongoLogicIgnoreAspect

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/logic/MongoLogicIgnoreAspect.java:16-37

```java
@Aspect
@Order(0)
```

```text
* 忽略逻辑删除
 *
 * @author loser
```

##### method markIgnoreLogic

```java
private void markIgnoreLogic()
```

所属类型：com.mongoplus.logic.MongoLogicIgnoreAspect；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/logic/MongoLogicIgnoreAspect.java:20-22

```java
@Pointcut("@annotation(com.mongoplus.annotation.logice.IgnoreLogic)")
```

##### method ignoreLogic

```java
public Object ignoreLogic(ProceedingJoinPoint joinPoint, IgnoreLogic ignoreLogic) throws Throwable
```

所属类型：com.mongoplus.logic.MongoLogicIgnoreAspect；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/logic/MongoLogicIgnoreAspect.java:24-34

```java
@Around(value = "markIgnoreLogic() && @annotation(ignoreLogic)")
```

### 包：com.mongoplus.property

#### class com.mongoplus.property.EncryptionPostProcessor

可见性：public；泛型：[]；extends：[]；implements：[EnvironmentPostProcessor, Ordered]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:19-82

```text
* 数据解密
 * @author anwen
 * @since mp
```

##### field PREFIX

```java
private final String PREFIX
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:24-24

```text
* 前缀
```

##### field SUFFIX

```java
private final String SUFFIX
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:29-29

```text
* 后缀
```

##### method postProcessEnvironment

```java
public void postProcessEnvironment(ConfigurableEnvironment environment, @NonNull SpringApplication application)
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:31-64

```java
@Override
```

##### method getOrder

```java
public int getOrder()
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:66-69

```java
@Override
```

##### method detected

```java
public boolean detected(String property)
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:71-73

##### method wrapper

```java
public String wrapper(String property)
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:75-77

##### method unWrapper

```java
public String unWrapper(String property)
```

所属类型：com.mongoplus.property.EncryptionPostProcessor；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:79-81

#### class com.mongoplus.property.MongoDBCollectionProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:7-50

```java
@ConfigurationProperties(prefix = "mongo-plus.configuration.collection")
```

##### field mappingStrategy

```java
private CollectionNameConvertEnum mappingStrategy
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:13-13

```text
* collection名称映射策略
```

##### field blockAttackInner

```java
private Boolean blockAttackInner
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:18-18

```text
* 防止整个集合更新和删除属性
```

##### field blockAttackInnerOrder

```java
private int blockAttackInnerOrder
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:23-23

```text
* 用来指定全集合更新删除的拦截器的order，默认1
```

##### method getBlockAttackInnerOrder

```java
public int getBlockAttackInnerOrder()
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:25-27

##### method setBlockAttackInnerOrder

```java
public void setBlockAttackInnerOrder(int blockAttackInnerOrder)
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:29-32

##### method getBlockAttackInner

```java
public Boolean getBlockAttackInner()
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:34-36

##### method setBlockAttackInner

```java
public void setBlockAttackInner(Boolean blockAttackInner)
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:38-40

##### method getMappingStrategy

```java
public CollectionNameConvertEnum getMappingStrategy()
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:42-44

##### method setMappingStrategy

```java
public void setMappingStrategy(CollectionNameConvertEnum mappingStrategy)
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:46-48

#### class com.mongoplus.property.MongoDBConfigurationProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:13-123

```java
@ConfigurationProperties(prefix = "mongo-plus.configuration")
```

```text
* configuration属性配置
 *
 * @author JiaChaoYang
 *
```

##### field banner

```java
private Boolean banner
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:19-19

```text
* banner打印
```

##### field autoIdCollectionName

```java
private String autoIdCollectionName
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:24-24

```text
* 存放自增id的集合
```

##### field ikun

```java
private Boolean ikun
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:29-29

```text
* 是否开启小黑子模式
```

##### field autoConvertObjectId

```java
private Boolean autoConvertObjectId
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:34-34

```text
* 自动转换ObjectId
```

##### field objectIdConvertType

```java
private Boolean objectIdConvertType
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:39-39

```text
* 是否将Id字段的ObjectId转换为字段的类型
```

##### field autoCreateIndex

```java
private Boolean autoCreateIndex
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:44-44

```text
* 自动创建索引
```

##### field autoCreateTimeSeries

```java
private Boolean autoCreateTimeSeries
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:49-49

```text
* 自动创建时间序列
```

##### field autoScanPackages

```java
private List<String> autoScanPackages
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:54-54

```text
* 自动创建相关操作所需配置的包路径,避免多模块下扫描不到
```

##### method getAutoScanPackages

```java
public List<String> getAutoScanPackages()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:56-58

##### method setAutoScanPackages

```java
public void setAutoScanPackages(List<String> autoScanPackages)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:60-62

##### method getAutoCreateTimeSeries

```java
public Boolean getAutoCreateTimeSeries()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:64-66

##### method setAutoCreateTimeSeries

```java
public void setAutoCreateTimeSeries(Boolean autoCreateTimeSeries)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:68-70

##### method getAutoCreateIndex

```java
public Boolean getAutoCreateIndex()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:72-74

##### method setAutoCreateIndex

```java
public void setAutoCreateIndex(Boolean autoCreateIndex)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:76-78

##### method getIkun

```java
public Boolean getIkun()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:80-82

##### method setIkun

```java
public void setIkun(Boolean ikun)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:84-87

##### method getAutoConvertObjectId

```java
public Boolean getAutoConvertObjectId()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:89-91

##### method setAutoConvertObjectId

```java
public void setAutoConvertObjectId(Boolean autoConvertObjectId)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:93-96

##### method getAutoIdCollectionName

```java
public String getAutoIdCollectionName()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:98-100

##### method setAutoIdCollectionName

```java
public void setAutoIdCollectionName(String autoIdCollectionName)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:102-105

##### method getObjectIdConvertType

```java
public Boolean getObjectIdConvertType()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:107-109

##### method setObjectIdConvertType

```java
public void setObjectIdConvertType(Boolean objectIdConvertType)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:111-114

##### method getBanner

```java
public Boolean getBanner()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:116-118

##### method setBanner

```java
public void setBanner(Boolean banner)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:120-122

#### class com.mongoplus.property.MongoDBConnectProperty

可见性：public；泛型：[]；extends：[BaseProperty]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:14-37

```java
@ConfigurationProperties(prefix = "mongo-plus.data.mongodb")
```

```text
* @author JiaChaoYang
 * 属性文件配置
 * @since 2023-02-09 14:29
 *
```

##### field slaveDataSource

```java
private List<SlaveDataSource> slaveDataSource
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:20-20

```text
* 从数据源
     *
```

##### method getSlaveDataSource

```java
public List<SlaveDataSource> getSlaveDataSource()
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:22-24

##### method setSlaveDataSource

```java
public void setSlaveDataSource(final List<SlaveDataSource> slaveDataSource)
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:26-28

##### constructor MongoDBConnectProperty

```java
public MongoDBConnectProperty(final List<SlaveDataSource> slaveDataSource)
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:30-32

##### constructor MongoDBConnectProperty

```java
public MongoDBConnectProperty()
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:34-35

#### class com.mongoplus.property.MongoDBFieldProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:9-41

```java
@ConfigurationProperties(prefix = "mongo-plus.configuration.field")
```

```text
* @author JiaChaoYang
 *
```

##### field camelToUnderline

```java
private Boolean camelToUnderline
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:17-17

```text
* 驼峰转下划线，将对象属性映射到数据库
     * 因为以前使用下划线转驼峰配置，存入数据库时不会转成下划线，只会查询时转为驼峰
     * 直接配置该配置，则实现映射到数据库会转下划线，映射回对象，则转驼峰
```

##### field ignoringNull

```java
private Boolean ignoringNull
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:22-22

```text
* 是否忽略null属性
```

##### method getIgnoringNull

```java
public Boolean getIgnoringNull()
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:24-26

##### method setIgnoringNull

```java
public void setIgnoringNull(Boolean ignoringNull)
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:28-31

##### method getCamelToUnderline

```java
public Boolean getCamelToUnderline()
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:33-35

##### method setCamelToUnderline

```java
public void setCamelToUnderline(Boolean camelToUnderline)
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:37-40

#### class com.mongoplus.property.MongoDBLogProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:12-78

```java
@ConfigurationProperties(prefix = "mongo-plus")
```

```text
* @author JiaChaoYang
 * 日志属性
 * @since 2023-06-07 23:07
 *
```

##### field log

```java
private Boolean log
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:18-18

```text
* 是否开启日志
```

##### field pretty

```java
private Boolean pretty
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:23-23

```text
* 是否格式化 mongo 语句为可执行语句
```

##### method getPretty

```java
public Boolean getPretty()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:25-27

##### method setPretty

```java
public void setPretty(Boolean pretty)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:29-31

##### field logOrder

```java
private int logOrder
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:36-36

```text
* 指定日志拦截器的order，默认为0
```

##### method getLogOrder

```java
public int getLogOrder()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:38-40

##### method setLogOrder

```java
public void setLogOrder(int logOrder)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:42-45

##### field format

```java
private Boolean format
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:50-50

```text
* 是否打开格式化sql
```

##### method getLog

```java
public Boolean getLog()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:52-54

##### method getFormat

```java
public Boolean getFormat()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:56-58

##### method setLog

```java
public void setLog(final Boolean log)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:60-63

##### method setFormat

```java
public void setFormat(final Boolean format)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:65-68

##### constructor MongoDBLogProperty

```java
public MongoDBLogProperty(final Boolean log, final Boolean format)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:70-73

##### constructor MongoDBLogProperty

```java
public MongoDBLogProperty()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:75-76

#### class com.mongoplus.property.MongoEncryptorProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:12-57

```java
@Configuration
@ConfigurationProperties(prefix = "mongo-plus.encryptor")
```

```text
* 加解密全局配置
 *
 * @author anwen
```

##### field privateKey

```java
private String privateKey
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:19-19

```text
* 私钥，非对称加密
```

##### field publicKey

```java
private String publicKey
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:24-24

```text
* 公钥，非对称加密
```

##### field key

```java
private String key
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:29-29

```text
* 秘钥，对称加密
```

##### method getPrivateKey

```java
public String getPrivateKey()
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:31-33

##### method setPrivateKey

```java
public void setPrivateKey(String privateKey)
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:35-38

##### method getPublicKey

```java
public String getPublicKey()
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:40-42

##### method setPublicKey

```java
public void setPublicKey(String publicKey)
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:44-47

##### method getKey

```java
public String getKey()
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:49-51

##### method setKey

```java
public void setKey(String key)
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:53-56

#### class com.mongoplus.property.MongoLogicDelProperty

可见性：public；泛型：[]；extends：[LogicProperty]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoLogicDelProperty.java:11-14

```java
@ConfigurationProperties(prefix = "mongo-plus.configuration.logic")
```

```text
* 逻辑删除配置
 *
 * @author loser
```

#### class com.mongoplus.property.MongoSpringProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:9-39

```java
@ConfigurationProperties(prefix = "mongo-plus.spring")
```

```text
* @author JiaChaoYang
 *
```

##### field transaction

```java
private Boolean transaction
```

所属类型：com.mongoplus.property.MongoSpringProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:15-15

```text
* 是否启用基于spring的事务管理器
```

##### field overrideMongoClient

```java
private Boolean overrideMongoClient
```

所属类型：com.mongoplus.property.MongoSpringProperty；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:20-20

```text
* 是否覆盖MongoTemplate的配置
```

##### method getOverrideMongoClient

```java
public Boolean getOverrideMongoClient()
```

所属类型：com.mongoplus.property.MongoSpringProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:22-24

##### method setOverrideMongoClient

```java
public void setOverrideMongoClient(Boolean overrideMongoClient)
```

所属类型：com.mongoplus.property.MongoSpringProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:26-28

##### method getTransaction

```java
public Boolean getTransaction()
```

所属类型：com.mongoplus.property.MongoSpringProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:30-32

##### method setTransaction

```java
public void setTransaction(Boolean transaction)
```

所属类型：com.mongoplus.property.MongoSpringProperty；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/property/MongoSpringProperty.java:34-37

### 包：com.mongoplus.registrar

#### class com.mongoplus.registrar.ClassPathMongoMapperScanner

可见性：public；泛型：[]；extends：[ClassPathBeanDefinitionScanner]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:22-92

```java
@SuppressWarnings("rawtypes")
```

```text
* @author anwen
```

##### field log

```java
private final Log log
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:25-25

##### field factoryBean

```java
private Class<? extends MongoMapperFactoryBean> factoryBean
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:27-27

##### field FACTORY_BEAN_OBJECT_TYPE

```java
static final String FACTORY_BEAN_OBJECT_TYPE
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：package-private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:29-29

##### constructor ClassPathMongoMapperScanner

```java
public ClassPathMongoMapperScanner(BeanDefinitionRegistry registry)
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:31-33

##### method setFactoryBean

```java
public void setFactoryBean(Class<? extends MongoMapperFactoryBean> factoryBean)
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:35-37

##### method doScan

```java
protected Set<BeanDefinitionHolder> doScan(@NonNull String... basePackages)
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:39-47

```java
@Override
@NonNull
```

##### method handleBeanDefinition

```java
public void handleBeanDefinition(Set<BeanDefinitionHolder> beanDefinitions)
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:49-85

##### method isCandidateComponent

```java
protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition)
```

所属类型：com.mongoplus.registrar.ClassPathMongoMapperScanner；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/ClassPathMongoMapperScanner.java:87-90

```java
@Override
```

#### class com.mongoplus.registrar.MongoMapperFactoryBean

可见性：public；泛型：[T]；extends：[]；implements：[FactoryBean<T>, BeanFactoryAware]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:13-57

```text
* @author anwen
```

##### field baseMapper

```java
private BaseMapper baseMapper
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:15-15

##### field mapperInterface

```java
private Class<T> mapperInterface
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:17-17

##### field instance

```java
private Object instance
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:19-19

##### method getMapperInterface

```java
public Class<T> getMapperInterface()
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:21-23

##### method setMapperInterface

```java
public void setMapperInterface(Class<T> mapperInterface)
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:25-27

##### constructor MongoMapperFactoryBean

```java
public MongoMapperFactoryBean(Class<T> mapperInterface)
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:29-31

##### constructor MongoMapperFactoryBean

```java
public MongoMapperFactoryBean()
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:33-33

##### method getObject

```java
public T getObject()
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:35-42

```java
@Override
@SuppressWarnings("unchecked")
```

##### method getObjectType

```java
public Class<?> getObjectType()
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:44-47

```java
@Override
```

##### method setBeanFactory

```java
public void setBeanFactory(BeanFactory beanFactory) throws BeansException
```

所属类型：com.mongoplus.registrar.MongoMapperFactoryBean；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperFactoryBean.java:49-55

```java
@Override
```

#### class com.mongoplus.registrar.MongoMapperScannerRegistrar

可见性：public；泛型：[]；extends：[]；implements：[ImportBeanDefinitionRegistrar]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java:18-55

```text
* MongoMapperScan扫描接口
 *
 * @author anwen
```

##### method registerBeanDefinitions

```java
public void registerBeanDefinitions(AnnotationMetadata metadata, @NonNull BeanDefinitionRegistry registry)
```

所属类型：com.mongoplus.registrar.MongoMapperScannerRegistrar；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java:20-35

```java
@Override
```

#### class com.mongoplus.registrar.MongoMapperScannerRegistrar.MongoMapperTypeFilter

可见性：public；泛型：[]；extends：[AssignableTypeFilter]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java:42-53

```text
* MongoMapper接口的扫描过滤器，如果实现的接口不是MongoMapper，则返回false,
     * AssignableTypeFilter类默认返回null，实现的接口不是MongoMapper也会扫描到
     * @author anwen
```

##### constructor MongoMapperTypeFilter

```java
public MongoMapperTypeFilter()
```

所属类型：com.mongoplus.registrar.MongoMapperScannerRegistrar.MongoMapperTypeFilter；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java:44-46

##### method matchTargetType

```java
protected Boolean matchTargetType(@NonNull String typeName)
```

所属类型：com.mongoplus.registrar.MongoMapperScannerRegistrar.MongoMapperTypeFilter；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java:48-52

```java
@Override
```

### 包：com.mongoplus.scanner

#### class com.mongoplus.scanner.MongoEntityScanner

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/scanner/MongoEntityScanner.java:17-50

```text
* @author anwen
```

##### field basePackages

```java
private final List<String> basePackages
```

所属类型：com.mongoplus.scanner.MongoEntityScanner；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/scanner/MongoEntityScanner.java:19-19

##### constructor MongoEntityScanner

```java
public MongoEntityScanner(List<String> basePackages)
```

所属类型：com.mongoplus.scanner.MongoEntityScanner；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/scanner/MongoEntityScanner.java:21-23

##### method scan

```java
public final Set<Class<?>> scan(Class<? extends Annotation>... annotations)
```

所属类型：com.mongoplus.scanner.MongoEntityScanner；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/scanner/MongoEntityScanner.java:25-48

```java
@SafeVarargs
```

### 包：com.mongoplus.tenant

#### class com.mongoplus.tenant.TenantAspect

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/tenant/TenantAspect.java:14-27

```java
@Aspect
@Order(1)
```

```text
* 多租户切面
 *
 * @author anwen
```

##### method ignoreTenant

```java
public Object ignoreTenant(ProceedingJoinPoint joinPoint) throws Throwable
```

所属类型：com.mongoplus.tenant.TenantAspect；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/tenant/TenantAspect.java:18-26

```java
@Around("@annotation(com.mongoplus.annotation.tenant.IgnoreTenant)")
```

### 包：com.mongoplus.transactional

#### class com.mongoplus.transactional.MongoPlusTransactionalManager

可见性：public；泛型：[]；extends：[AbstractPlatformTransactionManager]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:21-150

```java
@SuppressWarnings("NullableProblems")
```

```text
* 自定义事务管理器
 * @author JiaChaoYang
 *
```

##### field log

```java
Log log
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：package-private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:24-24

##### field options

```java
private final TransactionOptions options
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:26-26

##### constructor MongoPlusTransactionalManager

```java
public MongoPlusTransactionalManager(TransactionOptions options)
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:28-30

##### method doGetTransaction

```java
protected Object doGetTransaction() throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:32-36

```java
@Override
```

##### method isExistingTransaction

```java
protected boolean isExistingTransaction(Object transaction) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:38-46

```java
@Override
```

##### method doBegin

```java
protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:48-67

```java
@Override
```

##### method doCommit

```java
protected void doCommit(DefaultTransactionStatus status) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:69-77

```java
@Override
```

##### method doRollback

```java
protected void doRollback(DefaultTransactionStatus status) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:79-86

```java
@Override
```

##### method doSetRollbackOnly

```java
protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:88-91

```java
@Override
```

##### method doSuspend

```java
protected Object doSuspend(Object transaction) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:93-101

```java
@Override
```

##### method doResume

```java
protected void doResume(Object transaction, Object suspendedResources) throws TransactionException
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:103-109

```java
@Override
```

##### method doCleanupAfterCompletion

```java
protected void doCleanupAfterCompletion(Object transaction)
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:111-122

```java
@Override
```

#### class com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject

可见性：private；泛型：[]；extends：[]；implements：[SmartTransactionObject]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:124-148

##### field status

```java
private MongoTransactionStatus status
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:125-125

##### constructor MongoTransactionObject

```java
private MongoTransactionObject(MongoTransactionStatus status)
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:127-129

##### method getStatus

```java
private MongoTransactionStatus getStatus()
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:131-133

##### method setStatus

```java
private void setStatus(MongoTransactionStatus status)
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:135-137

##### method isRollbackOnly

```java
public boolean isRollbackOnly()
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:139-142

```java
@Override
```

##### method flush

```java
public void flush()
```

所属类型：com.mongoplus.transactional.MongoPlusTransactionalManager.MongoTransactionObject；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java:144-147

```java
@Override
```

#### class com.mongoplus.transactional.MongoTransactionDefinition

可见性：package-private；泛型：[]；extends：[DefaultTransactionDefinition]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionDefinition.java:6-18

##### field mongoTransactional

```java
private final MongoTransactional mongoTransactional
```

所属类型：com.mongoplus.transactional.MongoTransactionDefinition；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionDefinition.java:8-8

##### constructor MongoTransactionDefinition

```java
MongoTransactionDefinition(MongoTransactional mongoTransactional)
```

所属类型：com.mongoplus.transactional.MongoTransactionDefinition；可见性：package-private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionDefinition.java:10-13

##### method getMongoTransactional

```java
MongoTransactional getMongoTransactional()
```

所属类型：com.mongoplus.transactional.MongoTransactionDefinition；可见性：package-private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionDefinition.java:15-17

#### class com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:14-29

```text
* 事务配置类
 *
 * @author JiaChaoYang
 *
```

##### field options

```java
private final TransactionOptions options
```

所属类型：com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:16-16

##### constructor MongoTransactionManagerAutoConfiguration

```java
public MongoTransactionManagerAutoConfiguration(TransactionOptions options)
```

所属类型：com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:18-20

##### method mongoPlusTransactionalManager

```java
public PlatformTransactionManager mongoPlusTransactionalManager()
```

所属类型：com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:22-27

```java
@Bean("mongoPlusTransactionalManager")
@ConditionalOnMissingBean(name = "mongoPlusTransactionalManager")
@ConditionalOnProperty(name = "mongo-plus.spring.transaction", havingValue = "true")
```

#### class com.mongoplus.transactional.MongoTransactionalAspect

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:19-73

```java
@Aspect
@Order(1)
```

```text
* AOP操作，实现声明式事务
 *
 * @author JiaChaoYang
 *
```

##### field transactionManager

```java
private final PlatformTransactionManager transactionManager
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:23-23

##### constructor MongoTransactionalAspect

```java
public MongoTransactionalAspect(PlatformTransactionManager transactionManager)
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:25-27

##### method markMongoTransactional

```java
protected void markMongoTransactional()
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：protected

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:29-32

```java
@Pointcut("@annotation(com.mongoplus.annotation.transactional.MongoTransactional)")
```

##### method manageTransaction

```java
public Object manageTransaction(ProceedingJoinPoint joinPoint, MongoTransactional mongoTransactional) throws Throwable
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:34-50

```java
@Around(value = "markMongoTransactional() && @annotation(mongoTransactional)")
```

##### method rollbackOn

```java
private boolean rollbackOn(MongoTransactional transactional, Class<? extends Throwable> throwableType)
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:52-60

##### method matches

```java
private boolean matches(Class<? extends Throwable>[] configured, Class<? extends Throwable> actual)
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:62-72

### 包：com.mongoplus.word

#### class com.mongoplus.word.SensitiveWordConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java:14-39

```java
@Configuration
@ConditionalOnClass(SensitiveWordProperty.class)
@EnableConfigurationProperties(SensitiveWordConfiguration.WordProperty.class)
```

##### field wordProperty

```java
private final WordProperty wordProperty
```

所属类型：com.mongoplus.word.SensitiveWordConfiguration；可见性：private

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java:19-19

##### constructor SensitiveWordConfiguration

```java
public SensitiveWordConfiguration(WordProperty wordProperty, @Nullable LoadExtraWord loadExtraWord)
```

所属类型：com.mongoplus.word.SensitiveWordConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java:21-26

##### method sensitiveWordManager

```java
public SensitiveWordManager sensitiveWordManager()
```

所属类型：com.mongoplus.word.SensitiveWordConfiguration；可见性：public

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java:28-32

```java
@Bean
@ConditionalOnMissingBean(SensitiveWordManager.class)
```

#### class com.mongoplus.word.SensitiveWordConfiguration.WordProperty

可见性：public；泛型：[]；extends：[SensitiveWordProperty]；implements：[]

源码：mongo-plus-boot4-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java:34-37

```java
@ConfigurationProperties(prefix = "mongo-plus.sensitive-word")
```

