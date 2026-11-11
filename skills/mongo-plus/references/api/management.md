# 当前公开API签名：management

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.transactional.MongoPlusTransactionalManager · mongo-plus-boot-starter

`import com.mongoplus.transactional.MongoPlusTransactionalManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java#L21)。类型：CLASS；类型参数 ``；extends `AbstractPlatformTransactionManager`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.manager.MongoTransactionalManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoPlusTransactionalManager(TransactionOptions options);
```

## com.mongoplus.transactional.MongoTransactionalAspect · mongo-plus-boot-starter

`import com.mongoplus.transactional.MongoTransactionalAspect;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java#L19)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoTransactionalAspect(PlatformTransactionManager transactionManager);
@Around(value = "markMongoTransactional() && @annotation(mongoTransactional)") public Object manageTransaction(ProceedingJoinPoint joinPoint, MongoTransactional mongoTransactional);
```

## com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration · mongo-plus-boot-starter

`import com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.TransactionOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoTransactionManagerAutoConfiguration(TransactionOptions options);
@Bean("mongoPlusTransactionalManager") @ConditionalOnMissingBean(name = "mongoPlusTransactionalManager") @ConditionalOnProperty(name = "mongo-plus.spring.transaction", havingValue = "true") public PlatformTransactionManager mongoPlusTransactionalManager();
```

## com.mongoplus.transactional.MongoPlusTransactionalManager · mongo-plus-boot4-starter

`import com.mongoplus.transactional.MongoPlusTransactionalManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java#L21)。类型：CLASS；类型参数 ``；extends `AbstractPlatformTransactionManager`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.manager.MongoTransactionalManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoPlusTransactionalManager(TransactionOptions options);
```

## com.mongoplus.transactional.MongoTransactionalAspect · mongo-plus-boot4-starter

`import com.mongoplus.transactional.MongoTransactionalAspect;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java#L19)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoTransactionalAspect(PlatformTransactionManager transactionManager);
@Around(value = "markMongoTransactional() && @annotation(mongoTransactional)") public Object manageTransaction(ProceedingJoinPoint joinPoint, MongoTransactional mongoTransactional);
```

## com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration · mongo-plus-boot4-starter

`import com.mongoplus.transactional.MongoTransactionManagerAutoConfiguration;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot4-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.TransactionOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MongoTransactionManagerAutoConfiguration(TransactionOptions options);
@Bean("mongoPlusTransactionalManager") @ConditionalOnMissingBean(name = "mongoPlusTransactionalManager") @ConditionalOnProperty(name = "mongo-plus.spring.transaction", havingValue = "true") public PlatformTransactionManager mongoPlusTransactionalManager();
```

## com.mongoplus.config.Configuration · mongo-plus-core

`import com.mongoplus.config.Configuration;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L67)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
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
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Configuration builder();
public Configuration connection(String url);
public Configuration connection(BaseProperty baseProperty);
public Configuration database(String database);
public Configuration mongoClient(MongoClient mongoClient);
public Configuration dataSource(String dataSource, BaseProperty property);
public Configuration dataSource(String dataSource, MongoClient mongoClient, BaseProperty property);
public Configuration codec(MongoPlusCodec<?>... codecs);
public Configuration simpleTypeHolder(SimpleTypeHolder simpleTypeHolder);
public Configuration mongoConverter(MongoConverter mongoConverter);
public Configuration annotationHandler(AnnotationHandler annotationHandler);
public Configuration banner(boolean banner);
public Configuration collectionNameConvert(CollectionNameConvertEnum collectionNameConvertEnum);
public Configuration camelToUnderline(boolean camelToUnderline);
public Configuration ignoringNull(boolean ignoringNull);
public Configuration autoConvertObjectId(boolean autoConvertObjectId);
public Configuration objectIdConvertType(boolean objectIdConvertType);
public Configuration autoIdCollectionName(String autoIdCollectionName);
public Configuration ikun(boolean ikun);
public Configuration key(String key);
public Configuration publicKey(String publicKey);
public Configuration privateKey(String privateKey);
@SafeVarargs public final Configuration convert(Class<? extends ConversionStrategy<?>>... clazzConversions);
public Configuration metaObjectHandler(MetaObjectHandler metaObjectHandler);
public Configuration log();
public Configuration log(Boolean pretty);
public Configuration logOrder(int logOrder);
public Configuration blockAttackInner();
public Configuration blockAttackInnerOrder(int blockAttackInnerOrder);
@SafeVarargs public final Configuration listener(Class<? extends Listener>... listeners);
@SafeVarargs public final Configuration interceptor(Class<? extends Interceptor>... interceptors);
@SafeVarargs public final Configuration advancedInterceptor(Class<? extends AdvancedInterceptor>... interceptors);
public final Configuration advancedInterceptor(List<AdvancedInterceptor> interceptors);
public Configuration tenantHandler(TenantHandler tenantHandler);
public MongoPlusClient getMongoPlusClient();
public MongoPlusClient initMongoPlusClient();
public MongoPlusClient initMongoPlusClient(BaseProperty baseProperty);
public MongoPlusClient initMongoPlusClient(MongoClient mongoClient, BaseProperty baseProperty);
public DataSourceManager getDataSourceManager();
public void setOtherDataSource(Map<String, MongoClient> mongoClientMap);
public Configuration idGenerateHandler(IdGenerateHandler idGenerateHandler);
public Configuration dataChangeRecorder(DataChangeRecorderInnerInterceptor dataChangeRecorderInnerInterceptor);
public Configuration dynamicCollectionName(DynamicCollectionNameInterceptor dynamicCollectionNameInterceptor);
public Configuration asyncMultipleWrite(AsyncMultipleWriteInterceptor multipleWriteInterceptor);
public Configuration logic(LogicProperty logicProperty);
public Configuration setLogicFiled(Class<?>... collectionClasses);
public Configuration setLogicFiled(LogicProperty logicProperty, Class<?>... collectionClasses);
public static ExecutorFactory getExecutorFactory();
public Configuration aware(Aware aware);
public BaseMapper getBaseMapper();
public BaseMapper getBaseMapper(MongoConverter mongoConverter);
```

## com.mongoplus.factory.MongoClientFactory · mongo-plus-core

`import com.mongoplus.factory.MongoClientFactory;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/factory/MongoClientFactory.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements `AutoCloseable`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoClient;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static MongoClientFactory getInstance(String ds, MongoClient mongoClient);
public static MongoClientFactory getInstance(MongoClient mongoClient);
public static MongoClientFactory getInstance(Map<String, MongoClient> mongoClientMap);
public static MongoClientFactory getInstance();
public void addMongoClient(String ds, MongoClient mongoClient);
public MongoClient getMongoClient(String ds);
public Boolean containsMongoClient(String ds);
public MongoClient getMongoClient();
public Map<String, MongoClient> getMongoClientMap();
@Override public void close();
```

## com.mongoplus.grid.GridFSBucketManager · mongo-plus-core

`import com.mongoplus.grid.GridFSBucketManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/grid/GridFSBucketManager.java#L20)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.registry.ComponentRegistry;
import org.bson.types.ObjectId;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static GridFSBucketManager create(String database);
public static GridFSBucketManager create(String database, String bucketName);
public ObjectId upload(String filePath, String filename);
public ObjectId upload(String filePath, String filename, GridFSUploadOptions options);
public ObjectId upload(InputStream inputStream, String filename);
public ObjectId upload(InputStream inputStream, String filename, GridFSUploadOptions options);
public ObjectId upload(byte[] data, String filename);
public ObjectId upload(byte[] data, String filename, GridFSUploadOptions options);
public GridFSBucket getGridFSBucket();
public void setGridFSBucket(GridFSBucket gridFSBucket);
public MongoPlusClient getMongoPlusClient();
public void setMongoPlusClient(MongoPlusClient mongoPlusClient);
```

## com.mongoplus.index.BaseIndex · mongo-plus-core

`import com.mongoplus.index.BaseIndex;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/index/BaseIndex.java#L15)。类型：INTERFACE；类型参数 ``；extends `null`；implements `Index`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String createIndex(Bson bson, Class<?> clazz);
String createIndex(Bson bson, IndexOptions indexOptions, Class<?> clazz);
List<String> createIndexes(List<IndexModel> indexes, Class<?> clazz);
List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions, Class<?> clazz);
List<Document> listIndexes(Class<?> clazz);
void dropIndex(String indexName, Class<?> clazz);
void dropIndex(String indexName, DropIndexOptions dropIndexOptions, Class<?> clazz);
void dropIndex(Bson keys, Class<?> clazz);
void dropIndex(Bson keys, DropIndexOptions dropIndexOptions, Class<?> clazz);
void dropIndexes(Class<?> clazz);
void dropIndexes(DropIndexOptions dropIndexOptions, Class<?> clazz);
```

## com.mongoplus.index.impl.AbstractBaseIndex · mongo-plus-core

`import com.mongoplus.index.impl.AbstractBaseIndex;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/index/impl/AbstractBaseIndex.java#L21)。类型：CLASS；类型参数 ``；extends `null`；implements `BaseIndex`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.index.BaseIndex;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapper.AbstractBaseMapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AbstractBaseIndex(MongoPlusClient mongoPlusClient, ExecutorFactory factory);
@Override public String createIndex(String database, String collectionName, Bson bson);
@Override public String createIndex(String database, String collectionName, Bson bson, IndexOptions indexOptions);
@Override public List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes);
@Override public List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes, CreateIndexOptions createIndexOptions);
@Override public List<Document> listIndexes(String database, String collectionName);
@Override public void dropIndex(String database, String collectionName, String indexName);
@Override public void dropIndex(String database, String collectionName, String indexName, DropIndexOptions dropIndexOptions);
@Override public void dropIndex(String database, String collectionName, Bson keys);
@Override public void dropIndex(String database, String collectionName, Bson keys, DropIndexOptions dropIndexOptions);
@Override public void dropIndexes(String database, String collectionName);
@Override public void dropIndexes(String database, String collectionName, DropIndexOptions dropIndexOptions);
```

## com.mongoplus.index.impl.DefaultBaseIndexImpl · mongo-plus-core

`import com.mongoplus.index.impl.DefaultBaseIndexImpl;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/index/impl/DefaultBaseIndexImpl.java#L20)。类型：CLASS；类型参数 ``；extends `AbstractBaseIndex`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.aware.MongoAwareUtils;
import com.mongoplus.aware.impl.NamespaceAware;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DefaultBaseIndexImpl(MongoPlusClient mongoPlusClient, ExecutorFactory factory);
@Override public String createIndex(Bson bson, Class<?> clazz);
@Override public String createIndex(Bson bson, IndexOptions indexOptions, Class<?> clazz);
@Override public List<String> createIndexes(List<IndexModel> indexes, Class<?> clazz);
@Override public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions, Class<?> clazz);
@Override public List<Document> listIndexes(Class<?> clazz);
@Override public void dropIndex(String indexName, Class<?> clazz);
@Override public void dropIndex(String indexName, DropIndexOptions dropIndexOptions, Class<?> clazz);
@Override public void dropIndex(Bson keys, Class<?> clazz);
@Override public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions, Class<?> clazz);
@Override public void dropIndexes(Class<?> clazz);
@Override public void dropIndexes(DropIndexOptions dropIndexOptions, Class<?> clazz);
```

## com.mongoplus.index.Index · mongo-plus-core

`import com.mongoplus.index.Index;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/index/Index.java#L14)。类型：INTERFACE；类型参数 ``；extends `null`；implements `SuperIndex`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import static com.mongoplus.toolkit.StringPool.EMPTY;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default String createIndex(String collectionName, Bson bson);
default String createIndex(String collectionName, Bson bson, IndexOptions indexOptions);
default List<String> createIndexes(String collectionName, List<IndexModel> indexes);
default List<String> createIndexes(String collectionName, List<IndexModel> indexes, CreateIndexOptions createIndexOptions);
default List<Document> listIndexes(String collectionName);
default void dropIndex(String collectionName, String indexName);
default void dropIndex(String collectionName, String indexName, DropIndexOptions dropIndexOptions);
default void dropIndex(String collectionName, Bson keys);
default void dropIndex(String collectionName, Bson keys, DropIndexOptions dropIndexOptions);
default void dropIndexes(String collectionName);
default void dropIndexes(String collectionName, DropIndexOptions dropIndexOptions);
```

## com.mongoplus.index.SuperIndex · mongo-plus-core

`import com.mongoplus.index.SuperIndex;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/index/SuperIndex.java#L12)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String createIndex(String database, String collectionName, Bson bson);
String createIndex(String database, String collectionName, Bson bson, IndexOptions indexOptions);
List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes);
List<String> createIndexes(String database, String collectionName, List<IndexModel> indexes, CreateIndexOptions createIndexOptions);
List<Document> listIndexes(String database, String collectionName);
void dropIndex(String database, String collectionName, String indexName);
void dropIndex(String database, String collectionName, String indexName, DropIndexOptions dropIndexOptions);
void dropIndex(String database, String collectionName, Bson keys);
void dropIndex(String database, String collectionName, Bson keys, DropIndexOptions dropIndexOptions);
void dropIndexes(String database, String collectionName);
void dropIndexes(String database, String collectionName, DropIndexOptions dropIndexOptions);
```

## com.mongoplus.logic.LogicDeleteHandler · mongo-plus-core

`import com.mongoplus.logic.LogicDeleteHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/logic/LogicDeleteHandler.java#L32)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.config.Configuration;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.registry.MongoEntityMappingRegistry;
import com.mongoplus.toolkit.ChainWrappers;
import com.mongoplus.toolkit.Filters;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static com.mongoplus.enums.QueryOperatorEnum.EQ;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
static boolean close(MongoCollection<Document> collection);
static boolean close();
static Map<Class<?>, LogicDeleteResult> mapper();
@SuppressWarnings("all") static <T> Bson doBsonLogicDel(Bson query, Class<T> clazz);
static LogicDeleteResult getLogicDeletedResult(Class<?> clazz);
static <T> List<ConditionMetaObject> doWrapperLogicDel(Class<T> clazz);
static <T> List<ConditionMetaObject> doWrapperLogicDel(Wrapper<T> queryWrapper, Class<T> clazz);
static Class<?> getBeanClass(MongoCollection<Document> collection);
```

## com.mongoplus.logic.LogicRemove · mongo-plus-core

`import com.mongoplus.logic.LogicRemove;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/logic/LogicRemove.java#L24)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.enums.SpecialConditionEnum;
import com.mongoplus.execute.Execute;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Object logic(Invocation invocation, MongoCollection<Document> collection);
```

## com.mongoplus.manager.BackupManager · mongo-plus-core

`import com.mongoplus.manager.BackupManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L37)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.listener.BackupListener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.meta.MongoPlusVersion;
import com.mongoplus.toolkit.Assert;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import com.mongoplus.toolkit.ZipUtil;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public BackupManager(String path, List<String> collectionNames, MongoPlusClient mongoPlusClient);
public BackupManager(String path, MongoPlusClient mongoPlusClient);
public BackupManager(MongoPlusClient mongoPlusClient);
public void setCollectionNames(List<Class<?>> collectionNames);
public void setCollectionNamesStr(List<String> collectionNames);
public void setPath(String path);
public Map<String, String> export();
public void imports(String path);
public String getFileName(String collectionName);
public void writeCollectionInfo(MongoNamespace namespace, BufferedWriter writer);
public String getPath();
public List<String> getCollectionNames();
public String getDataSourceName();
public void setDataSourceName(String dataSourceName);
public MongoPlusClient getMongoPlusClient();
public void setMongoPlusClient(MongoPlusClient mongoPlusClient);
public Integer getLimit();
public void setLimit(Integer limit);
public void setBackupListeners(BackupListener... backupListeners);
```

## com.mongoplus.manager.DataSourceManager · mongo-plus-core

`import com.mongoplus.manager.DataSourceManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L18)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoClient;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.toolkit.MongoUtil;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DataSourceManager(MongoPlusClient mongoPlusClient, MongoClientFactory mongoClientFactory);
public void appendTempDataSource(String dsName, BaseProperty baseProperty, boolean isOverride);
public void changeDataSource(String dsName);
public void clearDataSource();
public String currentDataSourceName();
```

## com.mongoplus.manager.LogicManager · mongo-plus-core

`import com.mongoplus.manager.LogicManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/LogicManager.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.LogicProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T> T withoutLogic(Supplier<T> supplier);
public static void withoutLogic(Runnable runnable);
public static void ignoreLogicCondition();
public static boolean isIgnoreLogic();
public static void restoreLogicCondition();
```

## com.mongoplus.manager.MongoPlusClient · mongo-plus-core

`import com.mongoplus.manager.MongoPlusClient;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L24)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conn.CollectionManager;
import com.mongoplus.domain.MongoPlusDsException;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Map<String, Map<String, CollectionManager>> getCollectionManagers();
public MongoCollection<Document> getCollection(Class<?> clazz);
public MongoCollection<Document> getCollection(Class<?> clazz, String collectionName);
public MongoCollection<Document> getCollection(String database, String collectionName);
public MongoCollection<Document> getCollection(String dataSource, String database, String collectionName);
public MongoCollection<Document> getCollection(String database, Class<?> clazz);
public CollectionManager getCollectionManager(Class<?> clazz);
public CollectionManager getCollectionManager(String dataSource, Class<?> clazz);
public CollectionManager getCollectionManager(String database);
public CollectionManager getCollectionManager(String dataSource, String database);
public String getDatabase(Class<?> clazz);
public String getDatabase(String dataSource, Class<?> clazz);
public MongoDatabase getMongoDatabase(String database);
public List<String> getCollectionListByDatabase(String database);
public List<String> getDataSourceNameList();
public String getCollectionName(Class<?> clazz);
public void setCollectionManagers(String database);
public void setCollectionManagerMap(Map<String, Map<String, CollectionManager>> collectionManagerMap);
public BaseProperty getBaseProperty();
public void setBaseProperty(BaseProperty baseProperty);
public MongoClient getMongoClient();
public MongoClient getMongoClient(String dataSource);
public List<MongoDatabase> getMongoDatabase();
public void setMongoDatabase(List<MongoDatabase> mongoDatabase);
public void dropCollection(Class<?> clazz);
public void dropCollection(String database, String collectionName);
@Override public String toString();
```

## com.mongoplus.manager.MongoTransactionalManager · mongo-plus-core

`import com.mongoplus.manager.MongoTransactionalManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/MongoTransactionalManager.java#L30)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.*;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongoplus.annotation.transactional.MongoReadPreference;
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.domain.InitMongoPlusException;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.ReadConcernEnum;
import com.mongoplus.enums.ReadPreferenceEnum;
import com.mongoplus.enums.WriteConcernEnum;
import com.mongoplus.factory.MongoClientFactory;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static MongoClient getMongoClient();
public static ClientSession getTransaction();
public static ClientSession getTransaction(MongoTransactional transactional);
public static void startTransaction();
public static void startTransaction(TransactionOptions options);
public static void startTransaction(MongoTransactional transactional);
public static void startTransaction(ClientSession session);
public static MongoTransactionStatus getTransactionStatus(MongoTransactional transactional);
public static MongoTransactionStatus getTransactionStatus(ClientSession session, TransactionOptions options);
public static void startTransaction(ClientSession session, TransactionOptions options);
public static void startTransaction(MongoTransactionStatus status);
public static void commitTransaction();
public static void commitTransaction(MongoTransactionStatus status);
public static void rollbackTransaction();
public static void rollbackTransaction(MongoTransactionStatus status);
public static void closeSession();
public static void closeSession(MongoTransactionStatus status);
public static void handleTransactionException(MongoTransactional mongoTransactional, Throwable e);
public static boolean processRollback(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass, boolean isRollback);
```

## com.mongoplus.manager.TenantManager · mongo-plus-core

`import com.mongoplus.manager.TenantManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/TenantManager.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.handlers.TenantHandler;
import org.bson.Document;
import java.util.function.Supplier;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Boolean getIgnoreTenant();
public static <T> T withoutTenant(Supplier<T> supplier);
public static void withoutTenant(Runnable runnable);
public static void ignoreTenantCondition();
public static void restoreTenantCondition();
public static boolean isTenantIgnored(MongoCollection<Document> collection);
public static boolean isTenantIgnored(MongoCollection<Document> collection, TenantHandler tenantHandler);
```

## com.mongoplus.model.BaseProperty · mongo-plus-core

`import com.mongoplus.model.BaseProperty;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/BaseProperty.java#L6)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getHost();
public String getPort();
public String getDatabase();
public String getUsername();
public String getPassword();
public String getAuthenticationDatabase();
public Integer getMinPoolSize();
public Integer getMaxPoolSize();
public Integer getWaitQueueTimeoutMS();
public Integer getServerSelectionTimeoutMS();
public Integer getLocalThresholdMS();
public Integer getHeartbeatFrequencyMS();
public String getReplicaSet();
public Boolean getSsl();
public String getClientKeyStore();
public void setClientKeyStore(String clientKeyStore);
public String getJks();
public void setJks(String jks);
public String getKeyPassword();
public void setKeyPassword(String keyPassword);
public boolean isInvalidHostNameAllowed();
public void setInvalidHostNameAllowed(boolean invalidHostNameAllowed);
public Integer getPosition();
public void setPosition(Integer position);
public Boolean getTls();
public Boolean getTlsInsecure();
public Boolean getTlsAllowInvalidHostnames();
public Integer getConnectTimeoutMS();
public Integer getSocketTimeoutMS();
public Integer getMaxIdleTimeMS();
public Integer getMaxLifeTimeMS();
public Boolean getJournal();
public String getW();
public Integer getWtimeoutMS();
public String getReadPreference();
public String getReadPreferenceTags();
public Integer getMaxStalenessSeconds();
public String getAuthMechanism();
public String getAuthSource();
public String getAuthMechanismProperties();
public String getAppName();
public String getCompressors();
public Integer getZlibCompressionLevel();
public Boolean getRetryWrites();
public Boolean getRetryReads();
public String getUuidRepresentation();
public Boolean getDirectConnection();
public Integer getMaxConnecting();
public String getSrvServiceName();
public void setHost(String host);
public void setPort(String port);
public void setDatabase(String database);
public void setUsername(String username);
public void setPassword(String password);
public void setAuthenticationDatabase(String authenticationDatabase);
public void setMinPoolSize(Integer minPoolSize);
public void setMaxPoolSize(Integer maxPoolSize);
public void setWaitQueueTimeoutMS(Integer waitQueueTimeoutMS);
public void setServerSelectionTimeoutMS(Integer serverSelectionTimeoutMS);
public void setLocalThresholdMS(Integer localThresholdMS);
public void setHeartbeatFrequencyMS(Integer heartbeatFrequencyMS);
public void setReplicaSet(String replicaSet);
public void setSsl(Boolean ssl);
public void setTls(Boolean tls);
public void setTlsInsecure(Boolean tlsInsecure);
public void setTlsAllowInvalidHostnames(Boolean tlsAllowInvalidHostnames);
public void setConnectTimeoutMS(Integer connectTimeoutMS);
public void setSocketTimeoutMS(Integer socketTimeoutMS);
public void setMaxIdleTimeMS(Integer maxIdleTimeMS);
public void setMaxLifeTimeMS(Integer maxLifeTimeMS);
public void setJournal(Boolean journal);
public void setW(String w);
public void setWtimeoutMS(Integer wtimeoutMS);
public void setReadPreference(String readPreference);
public void setReadPreferenceTags(String readPreferenceTags);
public void setMaxStalenessSeconds(Integer maxStalenessSeconds);
public void setAuthMechanism(String authMechanism);
public void setAuthSource(String authSource);
public void setAuthMechanismProperties(String authMechanismProperties);
public void setAppName(String appName);
public void setCompressors(String compressors);
public void setZlibCompressionLevel(Integer zlibCompressionLevel);
public void setRetryWrites(Boolean retryWrites);
public void setRetryReads(Boolean retryReads);
public void setUuidRepresentation(String uuidRepresentation);
public void setDirectConnection(Boolean directConnection);
public void setMaxConnecting(Integer maxConnecting);
public void setSrvServiceName(String srvServiceName);
public String getUrl();
public void setUrl(String url);
public boolean isSrv();
public void setSrv(boolean srv);
public boolean equals(Object o);
public int hashCode();
public String toString();
public BaseProperty(String host, String port, String database, String username, String password, String authenticationDatabase, Integer minPoolSize, Integer maxPoolSize, Integer waitQueueTimeoutMS, Integer serverSelectionTimeoutMS, Integer localThresholdMS, Integer heartbeatFrequencyMS, String replicaSet, Boolean ssl, Boolean tls, Boolean tlsInsecure, Boolean tlsAllowInvalidHostnames, Integer connectTimeoutMS, Integer socketTimeoutMS, Integer maxIdleTimeMS, Integer maxLifeTimeMS, Boolean journal, String w, Integer wtimeoutMS, String readPreference, String readPreferenceTags, Integer maxStalenessSeconds, String authMechanism, String authSource, String authMechanismProperties, String appName, String compressors, Integer zlibCompressionLevel, Boolean retryWrites, Boolean retryReads, String uuidRepresentation, Boolean directConnection, Integer maxConnecting, String srvServiceName);
public BaseProperty();
```

## com.mongoplus.parser.index.AbstractParser · mongo-plus-core

`import com.mongoplus.parser.index.AbstractParser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/AbstractParser.java#L24)。类型：CLASS；类型参数 ``；extends `null`；implements `Parser`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.MongoCompoundIndex;
import com.mongoplus.annotation.index.MongoIndex;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.parser.index.CompoundParser · mongo-plus-core

`import com.mongoplus.parser.index.CompoundParser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/CompoundParser.java#L21)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexModel;
import com.mongoplus.annotation.index.MongoCompoundIndex;
import com.mongoplus.annotation.index.MongoCompoundIndexes;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.Document;
import java.util.List;
import java.util.function.Function;
import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
public void compoundIndex(TypeInformation typeInformation, List<IndexModel> indexModelList, MongoCompoundIndex mongoCompoundIndex);
```

## com.mongoplus.parser.index.GeoParser · mongo-plus-core

`import com.mongoplus.parser.index.GeoParser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/GeoParser.java#L19)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexModel;
import com.mongoplus.annotation.index.MongoGeoIndex;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;
import java.util.List;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.HashParser · mongo-plus-core

`import com.mongoplus.parser.index.HashParser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/HashParser.java#L20)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.MongoHashIndex;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;
import java.util.List;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.IndexParser · mongo-plus-core

`import com.mongoplus.parser.index.IndexParser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/IndexParser.java#L19)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexModel;
import com.mongoplus.annotation.index.MongoIndex;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;
import java.util.List;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.Parser · mongo-plus-core

`import com.mongoplus.parser.index.Parser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/Parser.java#L14)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Function<TypeInformation, Boolean> activate();
void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.Resolver · mongo-plus-core

`import com.mongoplus.parser.index.Resolver;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/Resolver.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.index.MongoIndexDs;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public IndexMetaObject resolve(Class<?> entityClass);
```

## com.mongoplus.parser.index.SearchParse · mongo-plus-core

`import com.mongoplus.parser.index.SearchParse;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/SearchParse.java#L36)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.Analyzer;
import com.mongoplus.enums.SearchSortNoData;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import org.bson.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.mongoplus.parser.index.SearchDefinitionValidator.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.SearchParseTemp · mongo-plus-core

`import com.mongoplus.parser.index.SearchParseTemp;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/SearchParseTemp.java#L31)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusIndexException;
import com.mongoplus.enums.*;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.*;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.TextParser · mongo-plus-core

`import com.mongoplus.parser.index.TextParser;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/TextParser.java#L20)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.annotation.index.MongoTextIndex;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import java.util.function.Function;
import static com.mongoplus.toolkit.ClassTypeUtil.getFieldName;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation typeInformation, IndexMetaObject indexMetaObject);
```

## com.mongoplus.parser.index.VectorParse · mongo-plus-core

`import com.mongoplus.parser.index.VectorParse;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/parser/index/VectorParse.java#L46)。类型：CLASS；类型参数 ``；extends `AbstractParser`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoClientSettings;
import com.mongodb.client.model.HnswSearchIndexOptions;
import com.mongodb.client.model.SearchIndexDefinition;
import com.mongodb.client.model.SearchIndexModel;
import com.mongodb.client.model.SearchIndexType;
import com.mongodb.client.model.VectorSearchIndexDefinition;
import com.mongodb.client.model.VectorSearchIndexFields;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorHnswOptions;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.annotation.index.vector.MongoVectorStoredSource;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.enums.SearchIndexingMethod;
import com.mongoplus.enums.SearchQuantization;
import com.mongoplus.enums.SearchVectorSimilarity;
import com.mongoplus.enums.VectorFieldType;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.SimpleFieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import static com.mongoplus.parser.index.SearchDefinitionValidator.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<TypeInformation, Boolean> activate();
@Override public void parse(TypeInformation type, IndexMetaObject meta);
```

## com.mongoplus.toolkit.AutoUtil · mongo-plus-core

`import com.mongoplus.toolkit.AutoUtil;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L31)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.TimeSeriesOptions;
import com.mongoplus.annotation.collection.TimeSeries;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.IndexMetaObject;
import com.mongoplus.parser.index.Resolver;
import org.bson.Document;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static com.mongoplus.toolkit.ClassTypeUtil.getFieldNameAndCheck;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static void autoCreateTimeSeries(Collection<Class<?>> classCollection, MongoPlusClient mongoPlusClient);
public static void autoCreateIndexes(Collection<Class<?>> classCollection, MongoPlusClient mongoPlusClient);
```

## com.mongoplus.transactional.MongoTransactionalOperate · mongo-plus-core

`import com.mongoplus.transactional.MongoTransactionalOperate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/transactional/MongoTransactionalOperate.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.registry.ComponentRegistry;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static ClientSession createTransaction();
public static ClientSession startTransaction(String dataSourceName);
public static void startTransaction(ClientSession clientSession);
public static void commitTransaction(ClientSession clientSession);
public static void commitAndCloseTransaction(ClientSession clientSession);
public static void rollbackTransaction(ClientSession clientSession);
public static void rollbackAndCloseTransaction(ClientSession clientSession);
public static void closeTransaction(ClientSession clientSession);
```

## com.mongoplus.manager.SensitiveWordManager · mongo-plus-sensitive-word

`import com.mongoplus.manager.SensitiveWordManager;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java#L28)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.domain.SensitiveWordException;
import com.mongoplus.enums.SensitiveType;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.property.SensitiveWordProperty;
import com.mongoplus.property.SensitiveWordConfigurationProperty;
import com.mongoplus.handler.LoadExtraWord;
import com.mongoplus.registry.HandlerRegistry;
import com.mongoplus.toolkit.StringUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SensitiveWordManager(SensitiveWordProperty sensitiveWordProperty);
public SensitiveWordManager(@Nullable SensitiveWordBs sensitiveWordBs, SensitiveWordProperty sensitiveWordProperty);
public SensitiveWordManager(SensitiveWordConfigurationProperty property, LoadExtraWord loadExtraWord);
public SensitiveWordBs sensitiveWordBs();
public SensitiveType getSensitiveType();
public void addWordBlacklist(String... words);
public void addWordBlacklist(Collection<String> words);
public void addWordBlackList(Path wordFile);
public void removeSensitiveWord(String... words);
public void removeSensitiveWord(Collection<String> words);
public void removeSensitiveWord(Path wordFile);
public void addSensitiveWordAllow(String... words);
public void addSensitiveWordAllow(Collection<String> words);
public void addSensitiveWordAllow(Path wordFile);
public void removeSensitiveWordAllow(String... words);
public void removeSensitiveWordAllow(Collection<String> words);
public void removeSensitiveWordAllow(Path wordFile);
public void handler(@Nullable String fieldName, String target);
public void handler(FieldInformation fieldInformation);
public void handler(String target);
public String replace(String word);
```

## com.mongoplus.transactional.MongoTransactionalAspect · mongo-plus-solon-plugin

`import com.mongoplus.transactional.MongoTransactionalAspect;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-solon-plugin/src/main/java/com/mongoplus/transactional/MongoTransactionalAspect.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object doIntercept(Invocation inv);
```
