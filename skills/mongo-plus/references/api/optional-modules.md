# 当前公开API签名：optional-modules

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.domain.SensitiveWordException · mongo-plus-sensitive-word

`import com.mongoplus.domain.SensitiveWordException;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/domain/SensitiveWordException.java#L14)。类型：CLASS；类型参数 ``；extends `MongoPlusException`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.mapping.FieldInformation;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SensitiveWordException(@Nullable FieldInformation fieldInformation, @Nullable String fieldName, Collection<String> sensitiveWords);
public SensitiveWordException(@Nullable String fieldName, Collection<String> sensitiveWords);
public SensitiveWordException(Collection<String> sensitiveWords);
public Collection<String> getSensitiveWords();
@Nullable public String getFieldName();
@Nullable public FieldInformation getFieldInformation();
```

## com.mongoplus.enums.ResultHandler · mongo-plus-sensitive-word

`import com.mongoplus.enums.ResultHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/enums/ResultHandler.java#L6)。类型：ENUM；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
枚举项: REJECT, MASK
```

## com.mongoplus.handler.LoadExtraWord · mongo-plus-sensitive-word

`import com.mongoplus.handler.LoadExtraWord;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/LoadExtraWord.java#L13)。类型：INTERFACE；类型参数 ``；extends `null`；implements `IWordDeny, IWordAllow`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.github.houbb.sensitive.word.api.IWordAllow;
import com.github.houbb.sensitive.word.api.IWordDeny;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override default List<String> allow();
@Override default List<String> deny();
```

## com.mongoplus.handler.SensitiveWordFieldHandler · mongo-plus-sensitive-word

`import com.mongoplus.handler.SensitiveWordFieldHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/handler/SensitiveWordFieldHandler.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `FieldHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.SensitiveWord;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.registry.HandlerRegistry;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SensitiveWordFieldHandler();
@Override public Integer order();
@Override public Object handler(FieldInformation fieldInformation);
@Override public Object handler(FieldInformation fieldInformation, Object currentValue);
```

## com.mongoplus.interceptor.SensitiveWordInterceptor · mongo-plus-sensitive-word

`import com.mongoplus.interceptor.SensitiveWordInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java#L21)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.model.MutablePair;
import com.mongoplus.registry.HandlerRegistry;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SensitiveWordInterceptor();
@Override public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection);
@Override public Document executeSave(Document document, MongoCollection<Document> collection);
@Override public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList, MongoCollection<Document> collection);
@Override public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection);
@Override public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection);
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

## com.mongoplus.property.SensitiveWordProperty · mongo-plus-sensitive-word

`import com.mongoplus.property.SensitiveWordProperty;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java#L19)。类型：CLASS；类型参数 ``；extends `SensitiveWordConfigurationProperty`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.github.houbb.sensitive.word.bs.SensitiveWordBs;
import com.github.houbb.sensitive.word.support.allow.WordAllows;
import com.github.houbb.sensitive.word.support.deny.WordDenys;
import com.github.houbb.sensitive.word.support.ignore.SensitiveWordCharIgnores;
import com.mongoplus.enums.SensitiveType;
import com.mongoplus.handler.LoadExtraWord;
import com.mongoplus.handler.SensitiveWordFieldHandler;
import com.mongoplus.handlers.FieldHandlerChain;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.SensitiveWordInterceptor;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SensitiveWordBs sensitiveWordBs();
public static SensitiveWordBs sensitiveWordBs(SensitiveWordConfigurationProperty property, LoadExtraWord loadExtraWord);
public void setDynamicLoadWord(LoadExtraWord loadExtraWord);
public void initSensitiveType();
public static void initSensitiveType(SensitiveWordConfigurationProperty property);
```

## com.mongoplus.registry.HandlerRegistry · mongo-plus-sensitive-word

`import com.mongoplus.registry.HandlerRegistry;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/registry/HandlerRegistry.java#L9)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.HashMap;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") public static <T> T get(Class<T> clazz);
public static <T> void register(Class<T> clazz, T instance);
```

## com.mongoplus.context.ShardingTransactionContext · mongo-plus-sharding

`import com.mongoplus.context.ShardingTransactionContext;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L10)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.manager.MongoTransactionalManager;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Map<String, MongoTransactionStatus> getAllMongoTransactionStatus();
public static void removeResourcesTransactionStatus(String key);
public static MongoTransactionStatus getTransactionStatus(String key);
public static void addResourcesTransactionStatus(String key, MongoTransactionStatus mongoTransactionStatus);
public static void clear();
public static void commitCurrentAllTransaction();
public static void rollbackAllTransaction();
public static void closeAllSession();
```

## com.mongoplus.interceptor.DataSourceShardingInterceptor · mongo-plus-sharding

`import com.mongoplus.interceptor.DataSourceShardingInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L36)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor, AdvancedInterceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.context.ShardingTransactionContext;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.execute.instance.DefaultExecute;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.sharding.AbstractDataSourceShardingHandler;
import com.mongoplus.sharding.DataSourceShardingHandler;
import com.mongoplus.sharding.DataSourceShardingStrategy;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient);
public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient, AbstractDataSourceShardingHandler dataSourceShardingHandler);
@Override public int order();
@Override public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection);
@Override public Object intercept(Invocation invocation);
@Override public void afterExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, Object result, MongoCollection<Document> collection);
```

## com.mongoplus.sharding.AbstractDataSourceShardingHandler · mongo-plus-sharding

`import com.mongoplus.sharding.AbstractDataSourceShardingHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java#L25)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import static com.mongoplus.enums.ExecuteMethodEnum.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AbstractDataSourceShardingHandler();
public AbstractDataSourceShardingHandler(Map<String, List<ExecuteMethodEnum>> shardingStrategy);
public DataSourceShardingStrategy getDataSourceShardingStrategy();
public void setDataSourceShardingStrategy(DataSourceShardingStrategy dataSourceShardingStrategy);
public List<String> getHandleShardingStrategy(ExecuteMethodEnum method);
public Map<ExecuteMethodEnum, List<String>> getHandleShardingStrategy();
public List<String> handleDsName(String pattern, List<String> originalDsNameList);
public List<String> handleDsNameByRegex(String regex, List<String> originalDsNameList);
public abstract String loadBalance(List<String> dsNameList);
public abstract ClientSession handleTransactional(ClientSession currentClientSession, MongoClient mongoClient);
public static DataSourceShardingBuild builder();
public Map<String, List<ExecuteMethodEnum>> getShardingStrategy();
public void clearHandleDsNameCache();
public void clearRegexCache();
public boolean isValidRegex(String regex);
```

## com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild · mongo-plus-sharding

`import com.mongoplus.sharding.AbstractDataSourceShardingHandler.DataSourceShardingBuild;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/sharding/AbstractDataSourceShardingHandler.java#L267)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import static com.mongoplus.enums.ExecuteMethodEnum.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DataSourceShardingBuild setShardingMapping(String dsName, List<ExecuteMethodEnum> method);
public DataSourceShardingBuild setShardingMapping(String dsName, ExecuteMethodEnum... method);
public AbstractDataSourceShardingHandler build();
```

## com.mongoplus.sharding.DataSourceShardingHandler · mongo-plus-sharding

`import com.mongoplus.sharding.DataSourceShardingHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingHandler.java#L14)。类型：CLASS；类型参数 ``；extends `AbstractDataSourceShardingHandler`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.model.BaseProperty;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DataSourceShardingHandler();
public DataSourceShardingHandler(Map<String, List<ExecuteMethodEnum>> shardingStrategy);
@Override public String loadBalance(List<String> dsNameList);
@Override public ClientSession handleTransactional(ClientSession currentClientSession, MongoClient mongoClient);
```

## com.mongoplus.sharding.DataSourceShardingStrategy · mongo-plus-sharding

`import com.mongoplus.sharding.DataSourceShardingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/sharding/DataSourceShardingStrategy.java#L9)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.ExecuteMethodEnum;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String sharding(String currentDataSource, ExecuteMethodEnum method, Object[] source);
```

## com.mongoplus.sharding.ShardingTransactionalHandler · mongo-plus-sharding

`import com.mongoplus.sharding.ShardingTransactionalHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L17)。类型：CLASS；类型参数 ``；extends `TransactionHandler`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.ClientSession;
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.context.ShardingTransactionContext;
import com.mongoplus.handlers.TransactionHandler;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.support.ThrowableSupplier;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object transaction(ThrowableSupplier<Object> supplier, MongoTransactional mongoTransactional);
public static void handleTransactionException(MongoTransactional mongoTransactional, Throwable e);
public static boolean processRollback(MongoTransactional mongoTransactional, Class<? extends Throwable> eClass, boolean isRollback);
```

## com.mongoplus.config.MongoShardingConfiguration · mongo-plus-sharding-boot-starter

`import com.mongoplus.config.MongoShardingConfiguration;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements `InitializingBean`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.interceptor.DataSourceShardingInterceptor;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.sharding.ShardingTransactionalHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void afterPropertiesSet();
@Bean @ConditionalOnMissingBean public DataSourceShardingInterceptor dataSourceShardingInterceptor(MongoPlusClient mongoPlusClient);
```
