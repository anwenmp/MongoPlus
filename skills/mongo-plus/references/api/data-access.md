# 当前公开API签名：data-access

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.mapper.AbstractBaseMapper · mongo-plus-core

`import com.mongoplus.mapper.AbstractBaseMapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L56)。类型：CLASS；类型参数 ``；extends `DefaultBaseIndexImpl`；implements `BaseMapper`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.enums.CommandOperate;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.index.impl.DefaultBaseIndexImpl;
import com.mongoplus.interceptor.InterceptorChain;
import com.mongoplus.interceptor.business.TenantInterceptor;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.manager.TenantManager;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import com.mongoplus.model.command.ParseCommand;
import com.mongoplus.parser.command.CommandParse;
import com.mongoplus.toolkit.*;
import com.mongoplus.toolkit.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import static com.mongoplus.constant.SqlOperationConstant._ID;
import static com.mongoplus.enums.SpecialConditionEnum.EQ;
import static com.mongoplus.enums.SpecialConditionEnum.SET;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AbstractBaseMapper(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter, ExecutorFactory factory);
@Override public MongoPlusClient getMongoPlusClient();
@Override public MongoConverter getMongoConverter();
@Override public Execute getExecute();
@Override @Deprecated public <T> boolean save(String database, String collectionName, T entity, InsertManyOptions options);
@Override public <T> boolean save(String database, String collectionName, T entity, InsertOneOptions options);
@Override public <T> Boolean saveBatch(String database, String collectionName, Collection<T> entityList, InsertManyOptions options);
@Override public Long update(String database, String collectionName, Bson queryBasic, Bson updateBasic, UpdateOptions options);
@Override public Long updateOne(String database, String collectionName, Bson queryBasic, Bson updateBasic, UpdateOptions options);
@Override public <T> Boolean updateOne(T entity, Wrapper<T> queryWrapper, UpdateOptions options);
@Override public Integer bulkWrite(String database, String collectionName, List<WriteModel<Document>> writeModelList, BulkWriteOptions options);
@Override public <T> Boolean update(String database, String collectionName, T entity, Wrapper<T> queryWrapper, UpdateOptions options);
@Override public boolean isExist(String database, String collectionName, Serializable id);
@Override public boolean isExist(String database, String collectionName, Wrapper<?> queryWrapper);
@Override public Boolean update(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, UpdateOptions options);
@Override public Boolean updateOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, UpdateOptions options);
@Override public Boolean remove(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, DeleteOptions options);
@Override public Boolean removeOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, DeleteOptions options);
@Override public Long remove(String database, String collectionName, Bson filter, DeleteOptions options);
@Override public Long removeOne(String database, String collectionName, Bson filter, DeleteOptions options);
@Override public long count(String database, String collectionName, Wrapper<?> queryWrapper);
@Override public long recentPageCount(String database, String collectionName, List<ConditionMetaObject> conditionMetaObjectList, Integer pageNum, Integer pageSize, Integer recentPageNum);
@Override public <R> List<R> list(String database, String collectionName, Class<R> rClazz);
@Override public <R> List<R> list(String database, String collectionName, TypeReference<R> typeReference);
@Override public <T, R> List<R> list(String database, String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
@Override public <T, R> List<R> list(String database, String collectionName, Wrapper<T> queryWrapper, TypeReference<R> typeReference);
@Override public <R> List<R> aggregateList(String database, String collectionName, Aggregate<?> aggregate, Class<R> rClazz);
@Override public <R> R aggregateOne(String database, String collectionName, Aggregate<?> aggregate, Class<R> rClazz);
@Override public <R> List<R> aggregateList(String database, String collectionName, Aggregate<?> aggregate, TypeReference<R> typeReference);
@Override public <R> R aggregateOne(String database, String collectionName, Aggregate<?> aggregate, TypeReference<R> typeReference);
@Override public <T, R> R findOneAndRemove(String database, String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
@Override public <T, R> R findOneAndRemove(String database, String collectionName, Wrapper<T> queryWrapper, Consumer<FindOneAndDeleteOptions> optionsConfigurer, Class<R> rClazz);
@Override public <T, R> R one(String database, String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
@Override public <T, R> R one(String database, String collectionName, Wrapper<T> queryWrapper, TypeReference<R> typeReference);
@Override public <T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public <T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
@Override public <T, R> List<R> pageList(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public <T, R> List<R> pageList(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
@Override public <T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
@Override public <T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
@Override public <R> List<R> getByIds(String database, String collectionName, Collection<? extends Serializable> ids, Class<R> rClazz);
@Override public <R> List<R> getByIds(String database, String collectionName, Collection<? extends Serializable> ids, TypeReference<R> typeReference);
@Override public <R> R getById(String database, String collectionName, Serializable id, Class<R> rClazz);
@Override public <R> R getById(String database, String collectionName, Serializable id, TypeReference<R> typeReference);
@Override public <R> List<R> queryCommand(String database, String collectionName, String command, Class<R> rClazz);
@Override public <R> List<R> queryCommand(String database, String collectionName, String command, TypeReference<R> typeReference);
@Override public <R> List<R> getByColumn(String database, String collectionName, String column, Object value, Class<R> rClazz);
@Override public <R> List<R> getByColumn(String database, String collectionName, String column, Object value, TypeReference<R> typeReference);
@Override public long count(String database, String collectionName);
@Override public <T> List<T> command(String database, String command, TypeReference<T> typeReference);
public <T> PageResult<T> getPageResult(FindIterable<Document> documentFindIterable, long totalSize, PageParam pageParams, TypeReference<T> typeReference, MongoConverter mongoConverter);
```

## com.mongoplus.mapper.BaseMapper · mongo-plus-core

`import com.mongoplus.mapper.BaseMapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/BaseMapper.java#L26)。类型：INTERFACE；类型参数 ``；extends `null`；implements `Mapper`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default <T> boolean save(T entity);
@Deprecated <T> boolean save(T entity, InsertManyOptions options);
<T> boolean save(T entity, InsertOneOptions options);
default <T> Boolean saveBatch(Collection<T> entityList);
<T> Boolean saveBatch(Collection<T> entityList, InsertManyOptions options);
default Integer bulkWrite(List<WriteModel<Document>> writeModelList, Class<?> clazz);
Integer bulkWrite(List<WriteModel<Document>> writeModelList, Class<?> clazz, BulkWriteOptions options);
default <T> Boolean update(T entity, Wrapper<T> queryWrapper);
<T> Boolean update(T entity, Wrapper<T> queryWrapper, UpdateOptions options);
boolean isExist(Serializable id, Class<?> clazz);
boolean isExist(Wrapper<?> queryWrapper, Class<?> clazz);
default Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz);
Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, UpdateOptions options);
Boolean updateOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, UpdateOptions options);
default Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz);
Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, DeleteOptions options);
default Boolean removeOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz);
Boolean removeOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, DeleteOptions options);
default Long remove(Bson filter, Class<?> clazz);
Long remove(Bson filter, Class<?> clazz, DeleteOptions options);
default Long removeOne(Bson filter, Class<?> clazz);
Long removeOne(Bson filter, Class<?> clazz, @Nullable DeleteOptions options);
long count(Wrapper<?> queryWrapper, Class<?> clazz);
long recentPageCount(List<ConditionMetaObject> conditionMetaObjectList, Class<?> clazz, Integer pageNum, Integer pageSize, Integer recentPageNum);
<T, R> List<R> list(Class<T> clazz, Class<R> rClazz);
default <T> List<T> list(Class<T> clazz);
<T, R> List<R> list(Class<T> clazz, TypeReference<R> typeReference);
<T, R> List<R> list(Wrapper<T> queryWrapper, Class<T> clazz, Class<R> rClazz);
@SuppressWarnings("unchecked") default <T, R> List<R> list(Wrapper<T> queryWrapper, Class<R> clazz);
<T, R> List<R> list(Wrapper<T> queryWrapper, Class<T> clazz, TypeReference<R> typeReference);
<T, R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz);
<T, R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz);
default <T> List<T> aggregateList(Aggregate<?> aggregate, Class<T> clazz);
default <T> T aggregateOne(Aggregate<?> aggregate, Class<T> clazz);
<T, R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference);
<T, R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference);
<T, R> R one(Wrapper<T> queryWrapper, Class<T> clazz, Class<R> rClazz);
<T, R> R findOneAndRemove(Wrapper<T> queryWrapper, Class<T> clazz, Class<R> rClazz);
<T, R> R findOneAndRemove(Wrapper<T> queryWrapper, Class<T> clazz, Consumer<FindOneAndDeleteOptions> optionsConfigurer, Class<R> rClazz);
default <T> T findOneAndRemove(Wrapper<T> queryWrapper, Class<T> clazz);
@SuppressWarnings("unchecked") default <T, R> R one(Wrapper<T> queryWrapper, Class<R> clazz);
<T, R> R one(Wrapper<T> queryWrapper, Class<T> clazz, TypeReference<R> typeReference);
<T, R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
default <T> PageResult<T> page(Integer pageNum, Integer pageSize, Class<T> clazz);
<T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
<T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, TypeReference<R> typeReference);
default <T, R> List<R> pageList(Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
default <T> List<T> pageList(Integer pageNum, Integer pageSize, Class<T> clazz);
<T, R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
<T, R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, TypeReference<R> typeReference);
default <T, R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, Class<R> rClazz);
default <T> PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz);
<T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, Class<R> rClazz);
<T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, TypeReference<R> typeReference);
<T, R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz, Class<R> rClazz);
default <T> List<T> getByIds(Collection<? extends Serializable> ids, Class<T> clazz);
<T, R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz, TypeReference<R> typeReference);
<T, R> R getById(Serializable id, Class<T> clazz, Class<R> rClazz);
default <T> T getById(Serializable id, Class<T> clazz);
<T, R> R getById(Serializable id, Class<T> clazz, TypeReference<R> typeReference);
<T, R> List<R> queryCommand(String command, Class<T> clazz, Class<R> rClazz);
default <T> List<T> queryCommand(String command, Class<T> clazz);
<T, R> List<R> queryCommand(String command, Class<T> clazz, TypeReference<R> typeReference);
<T, R> List<R> getByColumn(String column, Object value, Class<T> clazz, Class<R> rClazz);
default <T> List<T> getByColumn(String column, Object value, Class<T> clazz);
<T, R> List<R> getByColumn(String column, Object value, Class<T> clazz, TypeReference<R> typeReference);
long count(Class<?> clazz);
```

## com.mongoplus.mapper.DefaultBaseMapperImpl · mongo-plus-core

`import com.mongoplus.mapper.DefaultBaseMapperImpl;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/DefaultBaseMapperImpl.java#L28)。类型：CLASS；类型参数 ``；extends `AbstractBaseMapper`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DefaultBaseMapperImpl(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter);
public DefaultBaseMapperImpl(MongoPlusClient mongoPlusClient, MongoConverter mongoConverter, ExecutorFactory factory);
@Override public <T> boolean save(T entity, InsertManyOptions options);
@Override public <T> boolean save(T entity, InsertOneOptions options);
@Override public <T> Boolean saveBatch(Collection<T> entityList, InsertManyOptions options);
@Override public Integer bulkWrite(List<WriteModel<Document>> writeModelList, Class<?> clazz, BulkWriteOptions options);
@Override public <T> Boolean update(T entity, Wrapper<T> queryWrapper, UpdateOptions options);
@Override public <T, R> List<R> list(Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> list(Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> List<R> list(Wrapper<T> queryWrapper, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> list(Wrapper<T> queryWrapper, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> aggregateList(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> R aggregateOne(Aggregate<?> aggregate, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> R one(Wrapper<T> queryWrapper, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> R findOneAndRemove(Wrapper<T> queryWrapper, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> R findOneAndRemove(Wrapper<T> queryWrapper, Class<T> clazz, Consumer<FindOneAndDeleteOptions> optionsConfigurer, Class<R> rClazz);
@Override public <T, R> R one(Wrapper<T> queryWrapper, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> R getById(Serializable id, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> R getById(Serializable id, Class<T> clazz, TypeReference<R> typeReference);
@Override public boolean isExist(Serializable id, Class<?> clazz);
@Override public boolean isExist(Wrapper<?> queryWrapper, Class<?> clazz);
@Override public <T, R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> getByIds(Collection<? extends Serializable> ids, Class<T> clazz, TypeReference<R> typeReference);
@Override public Boolean update(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, UpdateOptions options);
@Override public Boolean updateOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, UpdateOptions options);
@Override public Boolean remove(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, DeleteOptions options);
@Override public Boolean removeOne(UpdateChainWrapper<?, ?> updateChainWrapper, Class<?> clazz, DeleteOptions options);
@Override public Long remove(Bson filter, Class<?> clazz, DeleteOptions options);
@Override public Long removeOne(Bson filter, Class<?> clazz, DeleteOptions options);
@Override public long count(Wrapper<?> queryWrapper, Class<?> clazz);
@Override public long recentPageCount(List<ConditionMetaObject> conditionMetaObjectList, Class<?> clazz, Integer pageNum, Integer pageSize, Integer recentPageNum);
@Override public long count(Class<?> clazz);
@Override public <T, R> List<R> queryCommand(String command, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> queryCommand(String command, Class<T> clazz, TypeReference<R> typeReference);
@Override public <T, R> List<R> getByColumn(String column, Object value, Class<T> clazz, Class<R> rClazz);
@Override public <T, R> List<R> getByColumn(String column, Object value, Class<T> clazz, TypeReference<R> typeReference);
```

## com.mongoplus.mapper.Mapper · mongo-plus-core

`import com.mongoplus.mapper.Mapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/Mapper.java#L20)。类型：INTERFACE；类型参数 ``；extends `null`；implements `SuperMapper`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.WriteModel;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import static com.mongoplus.toolkit.StringPool.EMPTY;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default <T> boolean save(String collectionName, T entity);
default <T> Boolean saveBatch(String collectionName, Collection<T> entityList);
default Long update(String collectionName, Bson queryBasic, Bson updateBasic);
default Integer bulkWrite(String collectionName, List<WriteModel<Document>> writeModelList);
default <T> Boolean update(String collectionName, T entity, Wrapper<T> queryWrapper);
default boolean isExist(String collectionName, Serializable id);
default boolean isExist(String collectionName, Wrapper<?> queryWrapper);
default Boolean update(String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper);
default Boolean remove(String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper);
default Long remove(String collectionName, Bson filter);
default long count(String collectionName, Wrapper<?> queryWrapper);
default long recentPageCount(String collectionName, List<ConditionMetaObject> conditionMetaObjectList, Integer pageNum, Integer pageSize, Integer recentPageNum);
default <R> List<R> list(String collectionName, Class<R> rClazz);
default <R> List<R> list(String collectionName, TypeReference<R> typeReference);
default <T, R> List<R> list(String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
default <T, R> List<R> list(String collectionName, Wrapper<T> queryWrapper, TypeReference<R> typeReference);
default <R> List<R> aggregateList(String collectionName, Aggregate<?> aggregate, Class<R> rClazz);
default <R> List<R> aggregateList(String collectionName, Aggregate<?> aggregate, TypeReference<R> typeReference);
default <T, R> R one(String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
default <T, R> R one(String collectionName, Wrapper<T> queryWrapper, TypeReference<R> typeReference);
default <T, R> PageResult<R> page(String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
default <T, R> PageResult<R> page(String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
default <T, R> List<R> pageList(String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
default <T, R> List<R> pageList(String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
default <T, R> PageResult<R> page(String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
default <T, R> PageResult<R> page(String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
default <R> List<R> getByIds(String collectionName, Collection<? extends Serializable> ids, Class<R> rClazz);
default <R> List<R> getByIds(String collectionName, Collection<? extends Serializable> ids, TypeReference<R> typeReference);
default <R> R getById(String collectionName, Serializable id, Class<R> rClazz);
default <R> R getById(String collectionName, Serializable id, TypeReference<R> typeReference);
default <R> List<R> queryCommand(String collectionName, String command, Class<R> rClazz);
default <R> List<R> queryCommand(String collectionName, String command, TypeReference<R> typeReference);
default <R> List<R> getByColumn(String collectionName, String column, Object value, Class<R> rClazz);
default <R> List<R> getByColumn(String collectionName, String column, Object value, TypeReference<R> typeReference);
default long count(String collectionName);
```

## com.mongoplus.mapper.MongoMapper · mongo-plus-core

`import com.mongoplus.mapper.MongoMapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapper.java#L24)。类型：INTERFACE；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.InsertManyOptions;
import com.mongodb.client.model.InsertOneOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
BaseMapper getBaseMapper();
Class<T> getGenericityClass();
MongoCollection<Document> getCollection();
default Boolean save(T entity);
@Deprecated Boolean save(T entity, InsertManyOptions options);
Boolean save(T entity, InsertOneOptions options);
default Boolean saveBatch(Collection<T> entityList);
Boolean saveBatch(Collection<T> entityList, InsertManyOptions options);
Boolean saveOrUpdate(T entity);
boolean saveOrUpdate(T entity, boolean isQueryDatabase);
Boolean saveOrUpdateWrapper(T entity, Wrapper<T> queryWrapper);
Boolean saveOrUpdateBatch(Collection<T> entityList);
boolean saveOrUpdateBatch(Collection<T> entityList, boolean isQueryDatabase);
Boolean saveOrUpdateBatchWrapper(Collection<T> entityList, Wrapper<T> queryWrapper);
default Boolean updateById(T entity);
Boolean updateById(T entity, UpdateOptions options);
Boolean updateBatchByIds(Collection<T> entityList);
default Boolean updateByColumn(T entity, SFunction<T, Object> column);
Boolean updateByColumn(T entity, SFunction<T, Object> column, UpdateOptions options);
default Boolean updateByColumn(T entity, String column);
Boolean updateByColumn(T entity, String column, UpdateOptions options);
default Boolean remove(UpdateChainWrapper<T, ?> updateChainWrapper);
Boolean remove(UpdateChainWrapper<T, ?> updateChainWrapper, DeleteOptions options);
default Boolean update(UpdateChainWrapper<T, ?> updateChainWrapper);
Boolean update(UpdateChainWrapper<T, ?> updateChainWrapper, UpdateOptions options);
default Boolean update(T entity, Wrapper<T> queryWrapper);
Boolean update(T entity, Wrapper<T> queryWrapper, UpdateOptions options);
default Boolean removeById(Serializable id);
Boolean removeById(Serializable id, DeleteOptions options);
default Boolean removeByColumn(SFunction<T, Object> column, Object value);
Boolean removeByColumn(SFunction<T, Object> column, Object value, DeleteOptions options);
default Boolean removeByColumn(String column, Object value);
Boolean removeByColumn(String column, Object value, DeleteOptions options);
default Boolean removeBatchByIds(Collection<? extends Serializable> idList);
Boolean removeBatchByIds(Collection<? extends Serializable> idList, DeleteOptions options);
List<T> list();
<R> List<R> list(Class<R> rClazz);
<R> List<R> list(TypeReference<R> typeReference);
List<T> list(Aggregate<?> aggregate);
<R> List<R> list(Aggregate<?> aggregate, Class<R> rClass);
<R> List<R> list(Aggregate<?> aggregate, TypeReference<R> typeReference);
T one(Wrapper<T> queryWrapper);
<R> R one(Wrapper<T> queryWrapper, Class<R> rClazz);
<R> R one(Wrapper<T> queryWrapper, TypeReference<R> typeReference);
T one(Aggregate<?> aggregate);
<R> R one(Aggregate<?> aggregate, Class<R> rClazz);
<R> R one(Aggregate<?> aggregate, TypeReference<R> typeReference);
List<T> list(Wrapper<T> queryWrapper);
<R> List<R> list(Wrapper<T> queryWrapper, Class<R> rClazz);
<R> List<R> list(Wrapper<T> queryWrapper, TypeReference<R> typeReference);
long count();
long count(Wrapper<T> queryWrapper);
PageResult<T> page(PageParam pageParam);
<R> PageResult<R> page(PageParam pageParam, Class<R> rClazz);
<R> PageResult<R> page(PageParam pageParam, TypeReference<R> typeReference);
PageResult<T> page(PageParam pageParam, Integer recentPageNum);
<R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, Class<R> rClazz);
<R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, TypeReference<R> typeReference);
PageResult<T> page(Integer pageNum, Integer pageSize);
<R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<R> rClazz);
<R> PageResult<R> page(Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum);
PageResult<T> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize);
PageResult<T> page(Wrapper<T> queryWrapper, PageParam pageParam);
PageResult<T> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum);
PageResult<T> page(Wrapper<T> queryWrapper, PageParam pageParam, Integer recentPageNum);
<R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
<R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
<R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
<R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
<R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, Class<R> rClazz);
<R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, TypeReference<R> typeReference);
<R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
<R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
<R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, Integer recentPageNum, Class<R> rClazz);
<R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, Integer recentPageNum, TypeReference<R> typeReference);
List<T> pageList(PageParam pageParam);
<R> List<R> pageList(PageParam pageParam, Class<R> rClazz);
<R> List<R> pageList(PageParam pageParam, TypeReference<R> typeReference);
List<T> pageList(Integer pageNum, Integer pageSize);
<R> List<R> pageList(Integer pageNum, Integer pageSize, Class<R> rClazz);
<R> List<R> pageList(Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
List<T> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize);
<R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
<R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
List<T> pageList(Wrapper<T> queryWrapper, PageParam pageParam);
<R> List<R> pageList(Wrapper<T> queryWrapper, PageParam pageParam, Class<R> rClazz);
<R> List<R> pageList(Wrapper<T> queryWrapper, PageParam pageParam, TypeReference<R> typeReference);
T getById(Serializable id);
<R> R getById(Serializable id, Class<R> rClazz);
<R> R getById(Serializable id, TypeReference<R> typeReference);
List<T> getByIds(Collection<? extends Serializable> ids);
<R> List<R> getByIds(Collection<? extends Serializable> ids, Class<R> rClazz);
<R> List<R> getByIds(Collection<? extends Serializable> ids, TypeReference<R> typeReference);
List<T> queryCommand(String command);
<R> List<R> queryCommand(String command, Class<R> rClazz);
<R> List<R> queryCommand(String command, TypeReference<R> typeReference);
List<T> getByColumn(SFunction<T, Object> field, Object fieldValue);
<R> List<R> getByColumn(SFunction<T, Object> field, Object fieldValue, Class<R> rClazz);
<R> List<R> getByColumn(SFunction<T, Object> field, Object fieldValue, TypeReference<R> typeReference);
List<T> getByColumn(String field, Object fieldValue);
<R> List<R> getByColumn(String field, Object fieldValue, Class<R> rClazz);
<R> List<R> getByColumn(String field, Object fieldValue, TypeReference<R> typeReference);
Boolean exist(Serializable id);
Boolean exist(Wrapper<T> queryWrapper);
```

## com.mongoplus.mapper.MongoMapperImpl · mongo-plus-core

`import com.mongoplus.mapper.MongoMapperImpl;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java#L35)。类型：CLASS；类型参数 `T`；extends `null`；implements `MongoMapper<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.annotation.ID;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BaseMapper getBaseMapper();
public void setBaseMapper(BaseMapper baseMapper);
public Class<T> getClazz();
public void setClazz(Class<?> clazz);
@Override public Class<T> getGenericityClass();
@Override public MongoCollection<Document> getCollection();
@Override public Boolean save(T entity, InsertManyOptions options);
@Override public Boolean save(T entity, InsertOneOptions options);
@Override public Boolean saveBatch(Collection<T> entityList, InsertManyOptions options);
@Override public Boolean saveOrUpdate(T entity);
@Override public boolean saveOrUpdate(T entity, boolean isQueryDatabase);
@Override public Boolean saveOrUpdateWrapper(T entity, Wrapper<T> queryWrapper);
@Override public Boolean saveOrUpdateBatch(Collection<T> entityList);
@Override public boolean saveOrUpdateBatch(Collection<T> entityList, boolean isQueryDatabase);
@Override public Boolean saveOrUpdateBatchWrapper(Collection<T> entityList, Wrapper<T> queryWrapper);
@Override @SuppressWarnings("rawtypes") public Boolean updateById(T entity, UpdateOptions options);
@Override public Boolean updateBatchByIds(Collection<T> entityList);
@Override public Boolean updateByColumn(T entity, SFunction<T, Object> column, UpdateOptions options);
@Override @SuppressWarnings("rawtypes") public Boolean updateByColumn(T entity, String column, UpdateOptions options);
@Override public Boolean remove(UpdateChainWrapper<T, ?> updateChainWrapper, DeleteOptions options);
@Override public Boolean update(UpdateChainWrapper<T, ?> updateChainWrapper, UpdateOptions options);
@Override public Boolean update(T entity, Wrapper<T> queryWrapper, UpdateOptions options);
@Override public Boolean removeById(Serializable id, DeleteOptions options);
@Override public Boolean removeByColumn(SFunction<T, Object> column, Object value, DeleteOptions options);
@Override public Boolean removeByColumn(String column, Object value, DeleteOptions options);
@Override public Boolean removeBatchByIds(Collection<? extends Serializable> idList, DeleteOptions options);
@Override public List<T> list(Aggregate<?> aggregate);
@Override public <R> List<R> list(Aggregate<?> aggregate, Class<R> rClass);
@Override public <R> List<R> list(Aggregate<?> aggregate, TypeReference<R> typeReference);
@Override public T one(Wrapper<T> queryWrapper);
@Override public <R> R one(Wrapper<T> queryWrapper, Class<R> rClazz);
@Override public <R> R one(Wrapper<T> queryWrapper, TypeReference<R> typeReference);
@Override public T one(Aggregate<?> aggregate);
@Override public <R> R one(Aggregate<?> aggregate, Class<R> rClazz);
@Override public <R> R one(Aggregate<?> aggregate, TypeReference<R> typeReference);
@Override public List<T> list();
@Override public <R> List<R> list(Class<R> rClazz);
@Override public <R> List<R> list(TypeReference<R> typeReference);
@Override public List<T> list(Wrapper<T> queryWrapper);
@Override public <R> List<R> list(Wrapper<T> queryWrapper, Class<R> rClazz);
@Override public <R> List<R> list(Wrapper<T> queryWrapper, TypeReference<R> typeReference);
@Override public long count();
@Override public long count(Wrapper<T> queryWrapper);
@Override public PageResult<T> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize);
@Override public PageResult<T> page(Wrapper<T> queryWrapper, PageParam pageParam);
@Override public PageResult<T> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum);
@Override public PageResult<T> page(Wrapper<T> queryWrapper, PageParam pageParam, Integer recentPageNum);
@Override public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
@Override public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, Class<R> rClazz);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, TypeReference<R> typeReference);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, Integer recentPageNum, Class<R> rClazz);
@Override public <R> PageResult<R> page(Wrapper<T> queryWrapper, PageParam pageParam, Integer recentPageNum, TypeReference<R> typeReference);
@Override public List<T> pageList(PageParam pageParam);
@Override public <R> List<R> pageList(PageParam pageParam, Class<R> rClazz);
@Override public <R> List<R> pageList(PageParam pageParam, TypeReference<R> typeReference);
@Override public List<T> pageList(Integer pageNum, Integer pageSize);
@Override public <R> List<R> pageList(Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public <R> List<R> pageList(Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
@Override public List<T> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize);
@Override public <R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public <R> List<R> pageList(Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
@Override public List<T> pageList(Wrapper<T> queryWrapper, PageParam pageParam);
@Override public <R> List<R> pageList(Wrapper<T> queryWrapper, PageParam pageParam, Class<R> rClazz);
@Override public <R> List<R> pageList(Wrapper<T> queryWrapper, PageParam pageParam, TypeReference<R> typeReference);
@Override public PageResult<T> page(PageParam pageParam);
@Override public <R> PageResult<R> page(PageParam pageParam, Class<R> rClazz);
@Override public <R> PageResult<R> page(PageParam pageParam, TypeReference<R> typeReference);
@Override public PageResult<T> page(PageParam pageParam, Integer recentPageNum);
@Override public <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, Class<R> rClazz);
@Override public <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, TypeReference<R> typeReference);
@Override public PageResult<T> page(Integer pageNum, Integer pageSize);
@Override public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public <R> PageResult<R> page(Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
@Override public PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum);
@Override public T getById(Serializable id);
@Override public <R> R getById(Serializable id, Class<R> rClazz);
@Override public <R> R getById(Serializable id, TypeReference<R> typeReference);
@Override public List<T> getByIds(Collection<? extends Serializable> ids);
@Override public <R> List<R> getByIds(Collection<? extends Serializable> ids, Class<R> rClazz);
@Override public <R> List<R> getByIds(Collection<? extends Serializable> ids, TypeReference<R> typeReference);
@Override public List<T> queryCommand(String command);
@Override public <R> List<R> queryCommand(String command, Class<R> rClazz);
@Override public <R> List<R> queryCommand(String command, TypeReference<R> typeReference);
@Override public List<T> getByColumn(SFunction<T, Object> field, Object fieldValue);
@Override public <R> List<R> getByColumn(SFunction<T, Object> field, Object fieldValue, Class<R> rClazz);
@Override public <R> List<R> getByColumn(SFunction<T, Object> field, Object fieldValue, TypeReference<R> typeReference);
@Override public List<T> getByColumn(String field, Object fieldValue);
@Override public <R> List<R> getByColumn(String field, Object fieldValue, Class<R> rClazz);
@Override public <R> List<R> getByColumn(String field, Object fieldValue, TypeReference<R> typeReference);
@Override public Boolean exist(Serializable id);
@Override public Boolean exist(Wrapper<T> queryWrapper);
```

## com.mongoplus.mapper.SuperMapper · mongo-plus-core

`import com.mongoplus.mapper.SuperMapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/SuperMapper.java#L30)。类型：INTERFACE；类型参数 ``；extends `null`；implements `BaseIndex`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.*;
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.execute.Execute;
import com.mongoplus.index.BaseIndex;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeReference;
import com.mongoplus.model.PageResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import static com.mongoplus.toolkit.StringPool.EMPTY;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
MongoPlusClient getMongoPlusClient();
MongoConverter getMongoConverter();
Execute getExecute();
default <T> boolean save(String database, String collectionName, T entity);
@Deprecated <T> boolean save(String database, String collectionName, T entity, InsertManyOptions options);
<T> boolean save(String database, String collectionName, T entity, @Nullable InsertOneOptions options);
default <T> Boolean saveBatch(String database, String collectionName, Collection<T> entityList);
<T> Boolean saveBatch(String database, String collectionName, Collection<T> entityList, InsertManyOptions options);
default Long update(String database, String collectionName, Bson queryBasic, Bson updateBasic);
Long update(String database, String collectionName, Bson queryBasic, Bson updateBasic, UpdateOptions options);
Long updateOne(String database, String collectionName, Bson queryBasic, Bson updateBasic, UpdateOptions options);
default <T> Boolean update(String database, String collectionName, T entity, Wrapper<T> queryWrapper);
<T> Boolean update(String database, String collectionName, T entity, Wrapper<T> queryWrapper, UpdateOptions options);
<T> Boolean updateOne(T entity, Wrapper<T> queryWrapper, UpdateOptions options);
default Boolean update(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper);
Boolean update(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, UpdateOptions options);
Boolean updateOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, UpdateOptions options);
default Integer bulkWrite(String database, String collectionName, List<WriteModel<Document>> writeModelList);
Integer bulkWrite(String database, String collectionName, List<WriteModel<Document>> writeModelList, BulkWriteOptions options);
boolean isExist(String database, String collectionName, Serializable id);
boolean isExist(String database, String collectionName, Wrapper<?> queryWrapper);
default Boolean remove(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper);
Boolean remove(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, DeleteOptions options);
default Boolean removeOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper);
Boolean removeOne(String database, String collectionName, UpdateChainWrapper<?, ?> updateChainWrapper, DeleteOptions options);
default Long remove(String database, String collectionName, Bson filter);
Long remove(String database, String collectionName, Bson filter, DeleteOptions options);
default Long removeOne(String database, String collectionName, Bson filter);
Long removeOne(String database, String collectionName, Bson filter, DeleteOptions options);
long count(String database, String collectionName, Wrapper<?> queryWrapper);
long recentPageCount(String database, String collectionName, List<ConditionMetaObject> conditionMetaObjectList, Integer pageNum, Integer pageSize, Integer recentPageNum);
<R> List<R> list(String database, String collectionName, Class<R> rClazz);
<R> List<R> list(String database, String collectionName, TypeReference<R> typeReference);
<T, R> List<R> list(String database, String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
<T, R> List<R> list(String database, String collectionName, Wrapper<T> queryWrapper, TypeReference<R> typeReference);
<R> List<R> aggregateList(String database, String collectionName, Aggregate<?> aggregate, Class<R> rClazz);
<R> R aggregateOne(String database, String collectionName, Aggregate<?> aggregate, Class<R> rClazz);
<R> List<R> aggregateList(String database, String collectionName, Aggregate<?> aggregate, TypeReference<R> typeReference);
<R> R aggregateOne(String database, String collectionName, Aggregate<?> aggregate, TypeReference<R> typeReference);
<T, R> R one(String database, String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
<T, R> R findOneAndRemove(String database, String collectionName, Wrapper<T> queryWrapper, Class<R> rClazz);
<T, R> R findOneAndRemove(String database, String collectionName, Wrapper<T> queryWrapper, Consumer<FindOneAndDeleteOptions> optionsConfigurer, Class<R> rClazz);
<T, R> R one(String database, String collectionName, Wrapper<T> queryWrapper, TypeReference<R> typeReference);
<T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
<T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
<T, R> List<R> pageList(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Class<R> rClazz);
<T, R> List<R> pageList(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, TypeReference<R> typeReference);
<T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
<T, R> PageResult<R> page(String database, String collectionName, Wrapper<T> queryWrapper, Integer pageNum, Integer pageSize, Integer recentPageNum, TypeReference<R> typeReference);
<R> List<R> getByIds(String database, String collectionName, Collection<? extends Serializable> ids, Class<R> rClazz);
<R> List<R> getByIds(String database, String collectionName, Collection<? extends Serializable> ids, TypeReference<R> typeReference);
<R> R getById(String database, String collectionName, Serializable id, Class<R> rClazz);
<R> R getById(String database, String collectionName, Serializable id, TypeReference<R> typeReference);
<R> List<R> queryCommand(String database, String collectionName, String command, Class<R> rClazz);
<R> List<R> queryCommand(String database, String collectionName, String command, TypeReference<R> typeReference);
<R> List<R> getByColumn(String database, String collectionName, String column, Object value, Class<R> rClazz);
<R> List<R> getByColumn(String database, String collectionName, String column, Object value, TypeReference<R> typeReference);
long count(String database, String collectionName);
default <T> List<T> command(String command, Class<T> clazz);
default <T> List<T> command(String command, TypeReference<T> typeReference);
default <T> List<T> command(String database, String command, Class<T> clazz);
<T> List<T> command(String database, String command, TypeReference<T> typeReference);
```

## com.mongoplus.repository.impl.RepositoryImpl · mongo-plus-core

`import com.mongoplus.repository.impl.RepositoryImpl;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/repository/impl/RepositoryImpl.java#L22)。类型：CLASS；类型参数 `T`；extends `MongoMapperImpl<T>`；implements `IRepository<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.aggregate.LambdaAggregateChainWrapper;
import com.mongoplus.conditions.query.LambdaQueryChainWrapper;
import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;
import com.mongoplus.mapper.MongoMapperImpl;
import com.mongoplus.repository.IRepository;
import com.mongoplus.toolkit.ChainWrappers;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public MongoCollection<Document> getCollection(String database);
@Override public String createIndex(Bson bson);
@Override public String createIndex(Bson bson, IndexOptions indexOptions);
@Override public List<String> createIndexes(List<IndexModel> indexes);
@Override public List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions);
@Override public List<Document> listIndexes();
@Override public void dropIndex(String indexName);
@Override public void dropIndex(String indexName, DropIndexOptions dropIndexOptions);
@Override public void dropIndex(Bson keys);
@Override public void dropIndex(Bson keys, DropIndexOptions dropIndexOptions);
@Override public void dropIndexes();
@Override public void dropIndexes(DropIndexOptions dropIndexOptions);
@Override public LambdaQueryChainWrapper<T> lambdaQuery();
@Override public LambdaAggregateChainWrapper<T> lambdaAggregate();
@Override public LambdaAggregateChainWrapper<T> lambdaAggregates();
@Override public LambdaUpdateChainWrapper<T> lambdaUpdate();
```

## com.mongoplus.repository.IRepository · mongo-plus-core

`import com.mongoplus.repository.IRepository;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/repository/IRepository.java#L17)。类型：INTERFACE；类型参数 `T`；extends `null`；implements `MongoMapper<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CreateIndexOptions;
import com.mongodb.client.model.DropIndexOptions;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.IndexOptions;
import com.mongoplus.aggregate.LambdaAggregateChainWrapper;
import com.mongoplus.conditions.query.LambdaQueryChainWrapper;
import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;
import com.mongoplus.mapper.MongoMapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String createIndex(Bson bson);
String createIndex(Bson bson, IndexOptions indexOptions);
List<String> createIndexes(List<IndexModel> indexes);
List<String> createIndexes(List<IndexModel> indexes, CreateIndexOptions createIndexOptions);
List<Document> listIndexes();
void dropIndex(String indexName);
void dropIndex(String indexName, DropIndexOptions dropIndexOptions);
void dropIndex(Bson keys);
void dropIndex(Bson keys, DropIndexOptions dropIndexOptions);
void dropIndexes();
void dropIndexes(DropIndexOptions dropIndexOptions);
MongoCollection<Document> getCollection(String database);
LambdaQueryChainWrapper<T> lambdaQuery();
LambdaAggregateChainWrapper<T> lambdaAggregate();
LambdaAggregateChainWrapper<T> lambdaAggregates();
LambdaUpdateChainWrapper<T> lambdaUpdate();
```

## com.mongoplus.service.impl.ServiceImpl · mongo-plus-core

`import com.mongoplus.service.impl.ServiceImpl;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/service/impl/ServiceImpl.java#L11)。类型：CLASS；类型参数 `T`；extends `RepositoryImpl<T>`；implements `IService<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.repository.impl.RepositoryImpl;
import com.mongoplus.service.IService;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.service.IService · mongo-plus-core

`import com.mongoplus.service.IService;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/service/IService.java#L9)。类型：INTERFACE；类型参数 `T`；extends `null`；implements `IRepository<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.repository.IRepository;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```
