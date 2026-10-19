# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 140 条完整源码记录（含内部实现）。

## 模块：mongo-plus-solon-plugin

### 包：com.mongoplus.config

#### class com.mongoplus.config.MongoPlusAutoConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:56-357

```text
* MongoPlus自动注入配置
 * @author JiaChaoYang
 *
```

##### field baseMapper

```java
private final BaseMapper baseMapper
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:58-58

##### field mongoDBLogProperty

```java
private final MongoDBLogProperty mongoDBLogProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:60-60

##### field mongoDBCollectionProperty

```java
private final MongoDBCollectionProperty mongoDBCollectionProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:62-62

##### field mongoLogicDelProperty

```java
private final MongoLogicDelProperty mongoLogicDelProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:64-64

##### field mongoPlusClient

```java
private final MongoPlusClient mongoPlusClient
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:66-66

##### field mongoDBConfigurationProperty

```java
private final MongoDBConfigurationProperty mongoDBConfigurationProperty
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:68-68

##### field log

```java
Log log
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：package-private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:70-70

##### constructor MongoPlusAutoConfiguration

```java
public MongoPlusAutoConfiguration(BaseMapper baseMapper, MongoDBLogProperty mongoDBLogProperty, MongoDBCollectionProperty mongoDBCollectionProperty, MongoLogicDelProperty mongoLogicDelProperty, MongoPlusClient mongoPlusClient, MongoDBConfigurationProperty mongoDBConfigurationProperty)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:72-101

##### method init

```java
public void init(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:103-130

##### method setLogicFiled

```java
private void setLogicFiled(Class<?>... collectionClasses)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:138-140

```text
* 配置逻辑删除
     *
     * @param collectionClasses 需要进行逻辑删除的 collection class 集合
     * @author loser
```

##### method setConversion

```java
private void setConversion(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:146-163

```text
* 从Bean中拿到转换器
     * @author JiaChaoYang
```

##### method setMetaObjectHandler

```java
private void setMetaObjectHandler(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:169-171

```text
* 从Bean中拿到自动填充策略
     * @author JiaChaoYang
```

##### method setListener

```java
private void setListener(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:177-191

```text
* 从Bean中拿到监听器
     * @author JiaChaoYang
```

##### method setInterceptor

```java
private void setInterceptor(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:198-204

```text
* 从Bean中拿到拦截器
     *
     * @author JiaChaoYang
```

##### method setAdvancedInterceptor

```java
private void setAdvancedInterceptor(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:210-216

```text
* 设置高级拦截器
     * @author anwen
```

##### method setMapping

```java
private void setMapping(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:223-244

```text
* 从Bean中拿到映射器
     *
     * @author JiaChaoYang
```

##### method setIdGenerator

```java
private void setIdGenerator(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:250-254

```text
* 自定义id生成器
     * @author anwen
```

##### method setTenantHandler

```java
private void setTenantHandler(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:260-268

```text
* 多租户拦截器
     * @author anwen
```

##### method setDynamicCollectionHandler

```java
private void setDynamicCollectionHandler(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:274-282

```text
* 动态集合名拦截器
     * @author anwen
```

##### method collectionNameConvert

```java
public void collectionNameConvert()
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:288-290

```text
* 注册集合名转换器
     * @author anwen
```

##### method autoCreateTimeSeries

```java
public void autoCreateTimeSeries(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:296-305

```text
* 自动创建时间序列
     * @author anwen
```

##### method autoCreateIndexes

```java
public void autoCreateIndexes(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:311-320

```text
* 自动创建索引
     * @author anwen
```

##### method getAutoOperationClass

```java
private Set<Class<?>> getAutoOperationClass(List<String> autoScanPackages, Class<? extends Annotation> annotation)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:322-339

##### method setIdGenerateHandler

```java
public void setIdGenerateHandler(AppContext context)
```

所属类型：com.mongoplus.config.MongoPlusAutoConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:346-355

```text
* 设置id生成器
     *
     * @author anwen
```

#### class com.mongoplus.config.MongoPlusConfiguration

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:45-197

```java
@Configuration
```

```text
* @author JiaChaoYang
 * 连接配置
 * @since 2023-02-09 14:27
 *
```

##### field mongoDBConnectProperty

```java
private MongoDBConnectProperty mongoDBConnectProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:48-49

```java
@Inject("${mongo-plus.data.mongodb}")
```

##### field mongoDBCollectionProperty

```java
private MongoDBCollectionProperty mongoDBCollectionProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:51-52

```java
@Inject(value = "${mongo-plus.configuration.collection}", required = false)
```

##### field mongoDBConfigurationProperty

```java
private MongoDBConfigurationProperty mongoDBConfigurationProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:54-55

```java
@Inject(value = "${mongo-plus.configuration}", required = false)
```

##### field mongoDBLogProperty

```java
private MongoDBLogProperty mongoDBLogProperty
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:57-58

```java
@Inject("${mongo-plus}")
```

##### method mongo

```java
public MongoClient mongo(MongoClientFactory mongoClientFactory)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:64-68

```java
@Bean
@Condition(onMissingBean = MongoClient.class)
```

```text
* 将MongoClient注册为Bean
     * @author JiaChaoYang
```

##### method mongoClientFactory

```java
public MongoClientFactory mongoClientFactory()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:70-87

```java
@Bean
@Condition(onMissingBean = MongoClientFactory.class)
```

##### method mongoPlusClient

```java
public MongoPlusClient mongoPlusClient(MongoClient mongo, MongoClientFactory mongoClientFactory)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:90-105

```java
@Bean
@Condition(onMissingBean = MongoPlusClient.class)
```

##### method simpleTypeHolder

```java
public SimpleTypeHolder simpleTypeHolder()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:107-113

```java
@Bean
@Condition(onMissingBean = SimpleTypeHolder.class)
```

##### method mongoConverter

```java
public MongoConverter mongoConverter()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:115-119

```java
@Bean
@Condition(onMissingBean = MongoConverter.class)
```

##### method mongoTransactionalAspect

```java
public MongoTransactionalAspect mongoTransactionalAspect()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:121-125

```java
@Bean("mongoTransactionalAspect")
@Condition(onMissingBean = MongoTransactionalAspect.class)
```

##### method mongoPlusAutoConfiguration

```java
public MongoPlusAutoConfiguration mongoPlusAutoConfiguration(@Inject BaseMapper baseMapper, @Inject(value = "${mongo-plus.configuration.logic}", required = false) MongoLogicDelProperty mongoLogicDelProperty, MongoPlusClient mongoPlusClient)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:127-142

```java
@Bean
```

##### method baseMapper

```java
public BaseMapper baseMapper(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:144-148

```java
@Bean
@Condition(onMissingBean = BaseMapper.class)
```

##### method dataSourceManager

```java
public DataSourceManager dataSourceManager(MongoPlusClient mongoPlusClient, MongoClientFactory mongoClientFactory)
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:157-162

```java
@Bean
@Condition(onMissingBean = DataSourceManager.class)
```

```text
* 数据源管理器
     * @param mongoPlusClient mongoPlus客户端
     * @param mongoClientFactory mongoClient工厂
     * @return {@link DataSourceManager}
     * @author anwen
```

##### method mongoDataSourceAspect

```java
public MongoDataSourceAspect mongoDataSourceAspect()
```

所属类型：com.mongoplus.config.MongoPlusConfiguration；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:169-173

```java
@Bean("mongoDataSourceAspect")
@Condition(onMissingBean = MongoDataSourceAspect.class)
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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:181-185

```java
@Bean("mongoLogicIgnoreAspect")
@Condition(onMissingBean = MongoLogicIgnoreAspect.class)
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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:191-195

```java
@Bean("tenantAspect")
@Condition(onMissingBean = TenantAspect.class)
```

```text
* 忽略租户
     * @author anwen
```

#### class com.mongoplus.config.XPluginAuto

可见性：public；泛型：[]；extends：[]；implements：[Plugin]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/XPluginAuto.java:28-63

```text
* 使用插件处理配置
 * @author JiaChaoYang
 *
```

##### method start

```java
public void start(AppContext context)
```

所属类型：com.mongoplus.config.XPluginAuto；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/XPluginAuto.java:29-40

```java
@Override
```

##### method registerMongoMapper

```java
BeanInjector<Inject> registerMongoMapper()
```

所属类型：com.mongoplus.config.XPluginAuto；可见性：package-private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/XPluginAuto.java:42-61

### 包：com.mongoplus.datasource

#### class com.mongoplus.datasource.MongoDataSourceAspect

可见性：public；泛型：[]；extends：[]；implements：[Interceptor]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:15-32

```text
* @author anwen
```

##### method doIntercept

```java
public Object doIntercept(Invocation inv) throws Throwable
```

所属类型：com.mongoplus.datasource.MongoDataSourceAspect；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:16-31

```java
@Override
```

### 包：com.mongoplus.logic

#### class com.mongoplus.logic.MongoLogicIgnoreAspect

可见性：public；泛型：[]；extends：[]；implements：[Interceptor]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/logic/MongoLogicIgnoreAspect.java:15-31

```text
* 忽略逻辑删除
 *
 * @author loser
```

##### method doIntercept

```java
public Object doIntercept(Invocation inv) throws Throwable
```

所属类型：com.mongoplus.logic.MongoLogicIgnoreAspect；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/logic/MongoLogicIgnoreAspect.java:18-30

```java
@Override
```

### 包：com.mongoplus.property

#### class com.mongoplus.property.MongoDBCollectionProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:6-48

##### field mappingStrategy

```java
private CollectionNameConvertEnum mappingStrategy
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:11-11

```text
* collection名称映射策略
```

##### field blockAttackInner

```java
private Boolean blockAttackInner
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:16-16

```text
* 防止整个集合更新和删除属性
```

##### field blockAttackInnerOrder

```java
private int blockAttackInnerOrder
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:21-21

```text
* 用来指定全集合更新删除的拦截器的order，默认1
```

##### method getBlockAttackInnerOrder

```java
public int getBlockAttackInnerOrder()
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:23-25

##### method setBlockAttackInnerOrder

```java
public void setBlockAttackInnerOrder(int blockAttackInnerOrder)
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:27-30

##### method getBlockAttackInner

```java
public Boolean getBlockAttackInner()
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:32-34

##### method setBlockAttackInner

```java
public void setBlockAttackInner(Boolean blockAttackInner)
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:36-38

##### method getMappingStrategy

```java
public CollectionNameConvertEnum getMappingStrategy()
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:40-42

##### method setMappingStrategy

```java
public void setMappingStrategy(CollectionNameConvertEnum mappingStrategy)
```

所属类型：com.mongoplus.property.MongoDBCollectionProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java:44-46

#### class com.mongoplus.property.MongoDBConfigurationProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:12-133

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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:17-17

```text
* banner打印
```

##### field ikun

```java
private Boolean ikun
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:19-19

##### field autoIdCollectionName

```java
private String autoIdCollectionName
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:24-24

```text
* 存放自增id的集合
```

##### field objectIdConvertType

```java
private Boolean objectIdConvertType
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:30-30

```text
* 是否将Id字段的ObjectId转换为字段的类型
```

##### field autoConvertObjectId

```java
private Boolean autoConvertObjectId
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:35-35

```text
* 自动转换ObjectId
```

##### field autoCreateIndex

```java
private Boolean autoCreateIndex
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:40-40

```text
* 自动创建索引
```

##### field autoCreateTimeSeries

```java
private Boolean autoCreateTimeSeries
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:45-45

```text
* 自动创建时间序列
```

##### field autoScanPackages

```java
private List<String> autoScanPackages
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:50-50

```text
* 自动创建包路径
```

##### field lazyDataSource

```java
private Boolean lazyDataSource
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:55-55

```text
* 是否懒加载数据源
```

##### method getLazyDataSource

```java
public Boolean getLazyDataSource()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:57-59

##### method setLazyDataSource

```java
public void setLazyDataSource(Boolean lazyDataSource)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:61-63

##### method getAutoCreateTimeSeries

```java
public Boolean getAutoCreateTimeSeries()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:65-67

##### method setAutoCreateTimeSeries

```java
public void setAutoCreateTimeSeries(Boolean autoCreateTimeSeries)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:69-71

##### method getAutoCreateIndex

```java
public Boolean getAutoCreateIndex()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:73-75

##### method setAutoCreateIndex

```java
public void setAutoCreateIndex(Boolean autoCreateIndex)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:77-79

##### method getAutoConvertObjectId

```java
public Boolean getAutoConvertObjectId()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:81-83

##### method setAutoConvertObjectId

```java
public void setAutoConvertObjectId(Boolean autoConvertObjectId)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:85-88

##### method getAutoIdCollectionName

```java
public String getAutoIdCollectionName()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:90-92

##### method setAutoIdCollectionName

```java
public void setAutoIdCollectionName(String autoIdCollectionName)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:94-97

##### method getIkun

```java
public Boolean getIkun()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:99-101

##### method setIkun

```java
public void setIkun(Boolean ikun)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:103-106

##### method getObjectIdConvertType

```java
public Boolean getObjectIdConvertType()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:109-111

##### method setObjectIdConvertType

```java
public void setObjectIdConvertType(Boolean objectIdConvertType)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:113-116

##### method getBanner

```java
public Boolean getBanner()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:118-120

##### method setBanner

```java
public void setBanner(Boolean banner)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:122-124

##### method getAutoScanPackages

```java
public List<String> getAutoScanPackages()
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:126-128

##### method setAutoScanPackages

```java
public void setAutoScanPackages(List<String> autoScanPackages)
```

所属类型：com.mongoplus.property.MongoDBConfigurationProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java:130-132

#### class com.mongoplus.property.MongoDBConnectProperty

可见性：public；泛型：[]；extends：[BaseProperty]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:14-37

```java
@Configuration
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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:20-20

```text
* 从数据源
     *
```

##### method getSlaveDataSource

```java
public List<SlaveDataSource> getSlaveDataSource()
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:22-24

##### method setSlaveDataSource

```java
public void setSlaveDataSource(final List<SlaveDataSource> slaveDataSource)
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:26-28

##### constructor MongoDBConnectProperty

```java
public MongoDBConnectProperty(final List<SlaveDataSource> slaveDataSource)
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:30-32

##### constructor MongoDBConnectProperty

```java
public MongoDBConnectProperty()
```

所属类型：com.mongoplus.property.MongoDBConnectProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java:34-35

#### class com.mongoplus.property.MongoDBFieldProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:10-45

```java
@Inject(value = "${mongo-plus.configuration.field}", required = false, autoRefreshed = true)
@Configuration
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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:19-19

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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:24-24

```text
* 是否忽略null属性
```

##### method getIgnoringNull

```java
public Boolean getIgnoringNull()
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:26-28

##### method setIgnoringNull

```java
public void setIgnoringNull(Boolean ignoringNull)
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:30-33

##### method getCamelToUnderline

```java
public Boolean getCamelToUnderline()
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:36-38

##### method setCamelToUnderline

```java
public void setCamelToUnderline(Boolean camelToUnderline)
```

所属类型：com.mongoplus.property.MongoDBFieldProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java:40-43

#### class com.mongoplus.property.MongoDBLogProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:11-77

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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:16-16

```text
* 是否开启日志
     *
```

##### field format

```java
private Boolean format
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:21-21

```text
* 是否打开格式化sql
```

##### field logOrder

```java
private int logOrder
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:26-26

```text
* 指定日志拦截器的order，默认为0
```

##### field pretty

```java
private Boolean pretty
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:31-31

```text
* 是否格式化 mongo 语句为可执行语句
```

##### method getPretty

```java
public Boolean getPretty()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:33-35

##### method setPretty

```java
public void setPretty(Boolean pretty)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:37-39

##### method getLogOrder

```java
public int getLogOrder()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:41-43

##### method setLogOrder

```java
public void setLogOrder(int logOrder)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:45-48

##### method getLog

```java
public Boolean getLog()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:51-53

##### method getFormat

```java
public Boolean getFormat()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:55-57

##### method setLog

```java
public void setLog(final Boolean log)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:59-62

##### method setFormat

```java
public void setFormat(final Boolean format)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:64-67

##### constructor MongoDBLogProperty

```java
public MongoDBLogProperty(final Boolean log, final Boolean format)
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:69-72

##### constructor MongoDBLogProperty

```java
public MongoDBLogProperty()
```

所属类型：com.mongoplus.property.MongoDBLogProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoDBLogProperty.java:74-75

#### class com.mongoplus.property.MongoEncryptorProperty

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:11-55

```java
@Configuration
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

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:17-17

```text
* 私钥，非对称加密
```

##### field publicKey

```java
private String publicKey
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:22-22

```text
* 公钥，非对称加密
```

##### field key

```java
private String key
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:27-27

```text
* 秘钥，对称加密
```

##### method getPrivateKey

```java
public String getPrivateKey()
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:29-31

##### method setPrivateKey

```java
public void setPrivateKey(String privateKey)
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:33-36

##### method getPublicKey

```java
public String getPublicKey()
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:38-40

##### method setPublicKey

```java
public void setPublicKey(String publicKey)
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:42-45

##### method getKey

```java
public String getKey()
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:47-49

##### method setKey

```java
public void setKey(String key)
```

所属类型：com.mongoplus.property.MongoEncryptorProperty；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java:51-54

#### class com.mongoplus.property.MongoLogicDelProperty

可见性：public；泛型：[]；extends：[LogicProperty]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/property/MongoLogicDelProperty.java:10-12

```text
* 逻辑删除配置
 *
 * @author loser
```

### 包：com.mongoplus.scanner

#### class com.mongoplus.scanner.CollectionScanner

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:8-68

##### field CACHE

```java
private static final Map<Class<? extends Annotation>, Set<Class<?>>> CACHE
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:15-15

```text
* 存储每种注解对应的类集合
     * key：注解类型
     * value：被该注解标注的类集合
```

##### method addCollectionClass

```java
public static void addCollectionClass(Class<? extends Annotation> annotationClazz, Class<?> clazz)
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:23-33

```text
* 添加一个类到指定注解类型的缓存中
     *
     * @param annotationClazz 注解类型
     * @param clazz           被注解的类
```

##### method getClasses

```java
public static Set<Class<?>> getClasses(Class<? extends Annotation> annotationClazz)
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:38-40

```text
* 获取指定注解类型的类集合
```

##### method contains

```java
public static boolean contains(Class<? extends Annotation> annotationClazz, Class<?> clazz)
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:45-48

```text
* 判断某个类是否在指定注解类型的缓存中
```

##### method getAll

```java
public static Map<Class<? extends Annotation>, Set<Class<?>>> getAll()
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:53-55

```text
* 获取所有注解类型及其对应类集合
```

##### method clear

```java
public static void clear()
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:60-62

```text
* 清空所有缓存
```

##### constructor CollectionScanner

```java
private CollectionScanner()
```

所属类型：com.mongoplus.scanner.CollectionScanner；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/scanner/CollectionScanner.java:64-66

### 包：com.mongoplus.tenant

#### class com.mongoplus.tenant.TenantAspect

可见性：public；泛型：[]；extends：[]；implements：[Interceptor]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/tenant/TenantAspect.java:15-30

```text
* 多租户切面
 *
 * @author anwen
```

##### method doIntercept

```java
public Object doIntercept(Invocation inv) throws Throwable
```

所属类型：com.mongoplus.tenant.TenantAspect；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/tenant/TenantAspect.java:17-29

```java
@Override
```

### 包：com.mongoplus.transactional

#### class com.mongoplus.transactional.MongoTransactionalAspect

可见性：public；泛型：[]；extends：[]；implements：[Interceptor]

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:16-76

```text
* @author JiaChaoYang
 *
```

##### method doIntercept

```java
public Object doIntercept(Invocation inv) throws Throwable
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：public

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:18-42

```java
@Override
```

##### method doUnRollBack

```java
private static boolean doUnRollBack(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass)
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:44-58

##### method doRollBack

```java
private static boolean doRollBack(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass)
```

所属类型：com.mongoplus.transactional.MongoTransactionalAspect；可见性：private

源码：mongo-plus-solon-plugin/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java:60-74

