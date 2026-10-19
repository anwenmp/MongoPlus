# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 63 条完整源码记录（含内部实现）。

## 模块：mongo-plus-sharding

### 包：com.mongoplus.context

#### class com.mongoplus.context.ShardingTransactionContext

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:10-78

##### field resources

```java
private static final ThreadLocal<Map<String, MongoTransactionStatus>> resources
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:15-15

```text
* 当前线程所持有的所有事务
```

##### method getAllMongoTransactionStatus

```java
public static Map<String, MongoTransactionStatus> getAllMongoTransactionStatus()
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:21-27

```text
* 获取当前线程持有的所有事务
     * @author anwen
```

##### method removeResourcesTransactionStatus

```java
public static void removeResourcesTransactionStatus(String key)
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:29-31

##### method getTransactionStatus

```java
public static MongoTransactionStatus getTransactionStatus(String key)
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:33-35

##### method addResourcesTransactionStatus

```java
public static void addResourcesTransactionStatus(String key, MongoTransactionStatus mongoTransactionStatus)
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:37-39

##### method clear

```java
public static void clear()
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:41-43

##### method commitCurrentAllTransaction

```java
public static void commitCurrentAllTransaction()
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49-53

```text
* 事务提交
     * @author anwen
```

##### method rollbackAllTransaction

```java
public static void rollbackAllTransaction()
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:60-65

```text
* 事务回滚
     *
     * @author JiaChaoYang
```

##### method closeAllSession

```java
public static void closeAllSession()
```

所属类型：com.mongoplus.context.ShardingTransactionContext；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:71-76

```text
* 关闭事务
     * @author anwen
```

### 包：com.mongoplus.interceptor

#### class com.mongoplus.interceptor.DataSourceShardingInterceptor

可见性：public；泛型：[]；extends：[]；implements：[Interceptor, AdvancedInterceptor]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:36-163

```text
* 分片拦截器
 *
 * @author anwen
```

##### field log

```java
private final Log log
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:38-38

##### field mongoPlusClient

```java
private final MongoPlusClient mongoPlusClient
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:40-40

##### field dataSourceShardingHandler

```java
private final AbstractDataSourceShardingHandler dataSourceShardingHandler
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:45-45

```text
* 分片处理器
```

##### constructor DataSourceShardingInterceptor

```java
public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient)
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:47-50

##### constructor DataSourceShardingInterceptor

```java
public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient, AbstractDataSourceShardingHandler dataSourceShardingHandler)
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:52-56

##### field sessionIsNotNull

```java
boolean sessionIsNotNull
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：package-private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:58-58

##### method order

```java
public int order()
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:67-70

```java
@Override
```

```text
* {@inheritDoc}
     * <p style='color:red'>要保证分片策略是最后一个拦截器</p>
     *
     * @return {@link int}
     * @author anwen
```

##### method beforeExecute

```java
public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77-142

```java
@Override
```

```text
* {@inheritDoc}
     *
     * @author anwen
```

##### method intercept

```java
public Object intercept(Invocation invocation) throws Throwable
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:144-154

```java
@Override
```

##### method afterExecute

```java
public void afterExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, Object result, MongoCollection<Document> collection)
```

所属类型：com.mongoplus.interceptor.DataSourceShardingInterceptor；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:156-162

```java
@Override
```

### 包：com.mongoplus.sharding

#### class com.mongoplus.sharding.AbstractDataSourceShardingHandler

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:25-308

```text
* 分片处理器
 * @author anwen
```

##### field handleDsNameCache

```java
private final Map<String, List<String>> handleDsNameCache
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:30-30

```text
* 处理数据源名称的缓存类
```

##### field regexCache

```java
private final Map<String, Boolean> regexCache
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:35-35

```text
* 判断是否是正则的缓存
```

##### field REGEX

```java
private static final String REGEX
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:40-40

```text
* 匹配除master外的数据源名称
```

##### field shardingStrategy

```java
private Map<String, List<ExecuteMethodEnum>> shardingStrategy
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:45-45

```text
* 分片策略
```

##### field DEFAULT_SHARDING_STRATEGY

```java
public static final Map<String, List<ExecuteMethodEnum>> DEFAULT_SHARDING_STRATEGY
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:51-51

```text
* 默认的分片策略
     * <p>增删改使用master，查询使用从数据源</p>
```

##### field dataSourceShardingStrategy

```java
private DataSourceShardingStrategy dataSourceShardingStrategy
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:56-56

```text
* 自定义的分片策略
```

##### field handleShardingStrategy

```java
private Map<ExecuteMethodEnum, List<String>> handleShardingStrategy
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:61-61

```text
* 处理后的分片策略
```

##### constructor AbstractDataSourceShardingHandler

```java
public AbstractDataSourceShardingHandler()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:82-84

##### constructor AbstractDataSourceShardingHandler

```java
public AbstractDataSourceShardingHandler(Map<String, List<ExecuteMethodEnum>> shardingStrategy)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:86-89

##### method getDataSourceShardingStrategy

```java
public DataSourceShardingStrategy getDataSourceShardingStrategy()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:91-93

##### method setDataSourceShardingStrategy

```java
public void setDataSourceShardingStrategy(DataSourceShardingStrategy dataSourceShardingStrategy)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:100-102

```text
* 设置自定义分片策略
     * @param dataSourceShardingStrategy 策略
     * @author anwen
```

##### method getHandleShardingStrategy

```java
public List<String> getHandleShardingStrategy(ExecuteMethodEnum method)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:110-112

```text
* 根据method获取对应的数据源
     * @param method 执行器方法
     * @return {@link List<String>}
     * @author anwen
```

##### method getHandleShardingStrategy

```java
public Map<ExecuteMethodEnum, List<String>> getHandleShardingStrategy()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:114-116

##### method handleDsName

```java
public List<String> handleDsName(String pattern, List<String> originalDsNameList)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:125-162

```text
* 处理数据源名称，可以使用*通配符和正则表达式
     * @param pattern 数据源名称
     * @param originalDsNameList 对应的所有数据源名称
     * @return {@link List<String>}
     * @author anwen
```

##### method handleDsNameByRegex

```java
public List<String> handleDsNameByRegex(String regex, List<String> originalDsNameList)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:171-175

```text
* 处理数据源名称，正则匹配
     * @param regex 正则
     * @param originalDsNameList 对应的所有数据源名称
     * @return {@link List<String>}
     * @author anwen
```

##### method loadBalance

```java
public abstract String loadBalance(List<String> dsNameList)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:184-184

```text
* 数据源负载均衡
     * <p>{@link DataSourceShardingHandler}默认实现，使用加权随机算法选择数据源</p>
     * @param dsNameList 数据源集合
     * @return {@link String}
     * @author anwen
```

##### method handleTransactional

```java
public abstract ClientSession handleTransactional(ClientSession currentClientSession, MongoClient mongoClient)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:192-192

```text
* 事务处理
     * @param currentClientSession 当前{@code session}
     * @param mongoClient 新数据源的客户端
     * @return {@link ClientSession} 命中新数据源后的{@code session}
```

##### method builder

```java
public static DataSourceShardingBuild builder()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:199-201

```text
* 构建器
     * @return {@link DataSourceShardingBuild}
     * @author anwen
```

##### method getShardingStrategy

```java
public Map<String, List<ExecuteMethodEnum>> getShardingStrategy()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:203-205

##### method setShardingStrategy

```java
void setShardingStrategy(Map<String, List<ExecuteMethodEnum>> shardingStrategy)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：package-private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:207-209

##### method addShardingStrategy

```java
void addShardingStrategy(String dsName, List<ExecuteMethodEnum> method)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：package-private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:211-213

##### method handle

```java
void handle()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：package-private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:219-234

```text
* 将分片策略进一步处理
     * @author anwen
```

##### method clearHandleDsNameCache

```java
public void clearHandleDsNameCache()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:240-244

```text
* 清空数据源名称处理缓存
     * @author anwen
```

##### method clearRegexCache

```java
public void clearRegexCache()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:250-254

```text
* 清空正则校验缓存
     * @author anwen
```

##### method isValidRegex

```java
public boolean isValidRegex(String regex)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:256-265

#### class com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:267-306

##### field handler

```java
DataSourceShardingHandler handler
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild；可见性：package-private

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:269-269

##### method setShardingMapping

```java
public DataSourceShardingBuild setShardingMapping(String dsName, List<ExecuteMethodEnum> method)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:278-284

```text
* 设置分片映射
         * @param dsName 数据源名称
         * @param method 所执行的操作
         * @return {@link DataSourceShardingBuild}
         * @author anwen
```

##### method setShardingMapping

```java
public DataSourceShardingBuild setShardingMapping(String dsName, ExecuteMethodEnum... method)
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:293-299

```text
* 设置分片映射
         * @param dsName 数据源名称
         * @param method 所执行的操作
         * @return {@link DataSourceShardingBuild}
         * @author anwen
```

##### method build

```java
public AbstractDataSourceShardingHandler build()
```

所属类型：com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java:301-304

#### class com.mongoplus.sharding.DataSourceShardingHandler

可见性：public；泛型：[]；extends：[AbstractDataSourceShardingHandler]；implements：[]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingHandler.java:14-67

##### constructor DataSourceShardingHandler

```java
public DataSourceShardingHandler()
```

所属类型：com.mongoplus.sharding.DataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingHandler.java:16-17

##### constructor DataSourceShardingHandler

```java
public DataSourceShardingHandler(Map<String, List<ExecuteMethodEnum>> shardingStrategy)
```

所属类型：com.mongoplus.sharding.DataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingHandler.java:19-21

##### method loadBalance

```java
public String loadBalance(List<String> dsNameList)
```

所属类型：com.mongoplus.sharding.DataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingHandler.java:26-57

```java
@Override
```

```text
* {@inheritDoc}
```

##### method handleTransactional

```java
public ClientSession handleTransactional(ClientSession currentClientSession, MongoClient mongoClient)
```

所属类型：com.mongoplus.sharding.DataSourceShardingHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingHandler.java:62-65

```java
@Override
```

```text
* {@inheritDoc}
```

#### interface com.mongoplus.sharding.DataSourceShardingStrategy

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingStrategy.java:9-21

```text
* 数据源分片策略
 * @author anwen
```

##### method sharding

```java
String sharding(String currentDataSource, ExecuteMethodEnum method, Object[] source)
```

所属类型：com.mongoplus.sharding.DataSourceShardingStrategy；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingStrategy.java:19-19

```text
* 自定义分片策略
     * @param currentDataSource 当前数据源
     * @param method 执行的方法
     * @param source 方法参数值
     * @return {@link String} 数据源名称，亦可返回带*号的通配符，或完整的正则表达式
     * @author anwen
```

#### class com.mongoplus.sharding.ShardingTransactionalHandler

可见性：public；泛型：[]；extends：[TransactionHandler]；implements：[]

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:17-65

```text
* @author anwen
```

##### method transaction

```java
public Object transaction(ThrowableSupplier<Object> supplier, MongoTransactional mongoTransactional) throws Throwable
```

所属类型：com.mongoplus.sharding.ShardingTransactionalHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19-36

```java
@Override
```

##### method handleTransactionException

```java
public static void handleTransactionException(MongoTransactional mongoTransactional, Throwable e)
```

所属类型：com.mongoplus.sharding.ShardingTransactionalHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:38-45

##### method processRollback

```java
public static boolean processRollback(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass, boolean isRollback)
```

所属类型：com.mongoplus.sharding.ShardingTransactionalHandler；可见性：public

源码：mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:47-63

