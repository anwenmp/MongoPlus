# 当前公开API签名：aggregation

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.aggregate.Aggregate · mongo-plus-core

`import com.mongoplus.aggregate.Aggregate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java#L25)。类型：INTERFACE；类型参数 `Children`；extends `null`；implements `Project<Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.*;
import com.mongodb.client.model.Facet;
import com.mongodb.client.model.Variable;
import com.mongodb.client.model.densify.DensifyOptions;
import com.mongodb.client.model.densify.DensifyRange;
import com.mongodb.client.model.fill.FillOptions;
import com.mongodb.client.model.fill.FillOutputField;
import com.mongoplus.aggregate.pipeline.Project;
import com.mongoplus.aggregate.pipeline.UnwindOption;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.model.aggregate.Field;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Collection;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
List<Bson> getAggregateConditionList();
default Bson getAggregateCondition();
default Bson getAggregateCondition(int index);
boolean isSkip();
BasicDBObject getAggregateOptions();
Children addFields(final String field, final String value);
<T> Children addFields(final SFunction<T, ?> field, final String value);
@SuppressWarnings("unchecked") <T> Children addFields(final String value, final SFunction<T, ?>... field);
<T> Children addFields(final SFunction<T, ?> field, final Object value);
<T> Children addFields(final SFunction<T, ?> field, final Collection<?> value);
Children addFields(final String field, final Collection<?> value);
Children addFields(final Field<?>... fields);
Children addFields(final List<Field<?>> fields);
Children addFields(final Bson bson);
Children set(final String field, final String value);
<T> Children set(final SFunction<T, ?> field, final String value);
@SuppressWarnings("unchecked") <T> Children set(final String value, final SFunction<T, ?>... field);
<T> Children set(final SFunction<T, ?> field, final Object value);
<T> Children set(final SFunction<T, ?> field, final Collection<?> value);
Children set(final String field, final Collection<?> value);
Children set(final Field<?>... fields);
Children set(final List<Field<?>> fields);
Children set(final Bson bson);
<Boundary, T> Children bucket(final SFunction<T, ?> groupBy, final List<Boundary> boundaries);
<Boundary> Children bucket(final Object groupBy, final List<Boundary> boundaries);
<Boundary, T> Children bucket(final SFunction<T, ?> groupBy, final List<Boundary> boundaries, BucketOptions options);
<Boundary> Children bucket(final Object groupBy, final List<Boundary> boundaries, BucketOptions options);
Children bucket(final Bson bson);
<T> Children bucketAuto(final SFunction<T, ?> groupBy, final Integer buckets);
Children bucketAuto(final Object groupBy, final Integer buckets);
<T> Children bucketAuto(final SFunction<T, ?> groupBy, final Integer buckets, BucketAutoOptions options);
Children bucketAuto(final Object groupBy, final Integer buckets, BucketAutoOptions options);
Children bucketAuto(final Bson bson);
Children count();
Children count(final String field);
<T> Children count(final SFunction<T, ?> field);
Children match(final Wrapper<?> queryWrapper);
Children match(final SFunction<QueryWrapper<?>, QueryWrapper<?>> function);
Children match(final Bson bson);
Children project(final Bson bson);
Children sort(final String field, final Integer value);
<T> Children sort(final SFunction<T, ?> field, final Integer value);
<T> Children sortAsc(final SFunction<T, ?> field);
Children sortAsc(final String field);
@SuppressWarnings("unchecked") <T> Children sortAsc(final SFunction<T, ?>... field);
Children sortAsc(final String... field);
<T> Children sortAscLambda(final List<SFunction<T, ?>> field);
Children sortAsc(final List<String> field);
<T> Children sortDesc(final SFunction<T, ?> field);
Children sortDesc(final String field);
@SuppressWarnings("unchecked") <T> Children sortDesc(final SFunction<T, ?>... field);
Children sortDesc(final String... field);
<T> Children sortDescLambda(final List<SFunction<T, ?>> field);
Children sortDesc(final List<String> field);
Children metaTextScore(final String field);
<T> Children metaTextScore(final SFunction<T, ?> field);
Children sort(final Bson bson);
default Children sortSpecification(final Bson specification);
Children sortByCount(final String field);
<T> Children sortByCount(final SFunction<T, ?> field);
Children skip(final long skip);
Children skip(final int skip);
Children limit(final long limit);
Children limit(final int limit);
Children lookup(final String from, final String localField, final String foreignField, final String as);
<T> Children lookup(final String from, final String localField, final String foreignField, final SFunction<T, ?> as);
Children lookup(final Class<?> from, final String localField, final String foreignField, final String as);
<T> Children lookup(final Class<?> from, final String localField, final String foreignField, final SFunction<T, ?> as);
<T, R> Children lookup(final String from, final SFunction<T, ?> localField, final SFunction<R, ?> foreignField, final String as);
<T, R, A> Children lookup(final String from, final SFunction<T, ?> localField, final SFunction<R, ?> foreignField, final SFunction<A, ?> as);
<T, R> Children lookup(final Class<?> from, final SFunction<T, ?> localField, final SFunction<R, ?> foreignField, final String as);
<T, R, A> Children lookup(final Class<?> from, final SFunction<T, ?> localField, final SFunction<R, ?> foreignField, final SFunction<A, ?> as);
<T> Children lookup(final String from, final SFunction<T, ?> localField, final String foreignField, final String as);
<T, A> Children lookup(final String from, final SFunction<T, ?> localField, final String foreignField, final SFunction<A, ?> as);
<T> Children lookup(final Class<?> from, final SFunction<T, ?> localField, final String foreignField, final String as);
<T, A> Children lookup(final Class<?> from, final SFunction<T, ?> localField, final String foreignField, final SFunction<A, ?> as);
<T> Children lookup(final String from, final String localField, final SFunction<T, ?> foreignField, final String as);
<T, A> Children lookup(final String from, final String localField, final SFunction<T, ?> foreignField, final SFunction<A, ?> as);
<T> Children lookup(final Class<?> from, final String localField, final SFunction<T, ?> foreignField, final String as);
<T, A> Children lookup(final Class<?> from, final String localField, final SFunction<T, ?> foreignField, final SFunction<A, ?> as);
<TExpression> Children lookup(final String from, final List<Variable<TExpression>> letList, final Aggregate<?> aggregate, final String as);
<TExpression, T> Children lookup(final String from, final List<Variable<TExpression>> letList, final Aggregate<?> aggregate, final SFunction<T, ?> as);
<TExpression> Children lookup(final Class<?> from, final List<Variable<TExpression>> letList, final Aggregate<?> aggregate, final String as);
<TExpression, T> Children lookup(final Class<?> from, final List<Variable<TExpression>> letList, final Aggregate<?> aggregate, final SFunction<T, ?> as);
Children lookup(final String from, final Aggregate<?> aggregate, final String as);
<T> Children lookup(final String from, final Aggregate<?> aggregate, final SFunction<T, ?> as);
Children lookup(final Class<?> from, final Aggregate<?> aggregate, final String as);
<T> Children lookup(final Class<?> from, final Aggregate<?> aggregate, final SFunction<T, ?> as);
Children lookup(final Bson bson);
Children facet(final String name, final Bson... pipeline);
Children facet(final String name, final List<? extends Bson> pipeline);
Children facet(final String name, final Aggregate<?> aggregate);
Children facet(final Facet... facets);
Children facet(final List<Facet> facets);
Children facet(final Bson bson);
Children graphLookup(final String from, final Object startWith, final String connectFromField, final String connectToField, final String as);
<T, R, U> Children graphLookup(final String from, final SFunction<T, ?> startWith, final SFunction<R, ?> connectFromField, final SFunction<U, ?> connectToField, final String as);
<T, R> Children graphLookup(final String from, final Object startWith, final SFunction<T, ?> connectFromField, final SFunction<R, ?> connectToField, final String as);
Children graphLookup(final String from, final Object startWith, final String connectFromField, final String connectToField, final String as, final GraphLookupOptions options);
<T, R, U> Children graphLookup(final String from, final SFunction<T, ?> startWith, final SFunction<R, ?> connectFromField, final SFunction<U, ?> connectToField, final String as, final GraphLookupOptions options);
<T, R> Children graphLookup(final String from, final Object startWith, final SFunction<T, ?> connectFromField, final SFunction<R, ?> connectToField, final String as, final GraphLookupOptions options);
Children graphLookup(final Bson bson);
Children group(final String _id);
<T> Children group(final SFunction<T, ?> _id);
<TExpression> Children group(@Nullable final TExpression id, final BsonField... fieldAccumulators);
<T, TExpression> Children group(@Nullable final SFunction<T, ?> id, final BsonField... fieldAccumulators);
<TExpression> Children group(@Nullable final TExpression id, final List<BsonField> fieldAccumulators);
<T, TExpression> Children group(@Nullable final SFunction<T, ?> id, final List<BsonField> fieldAccumulators);
Children group(final Bson bson);
Children unionWith(final String collectionName);
Children unionWith(final Class<?> collection);
Children unionWith(final String collectionName, final Aggregate<?> aggregate);
Children unionWith(final String collectionName, final List<? extends Bson> aggregate);
Children unionWith(final Class<?> collection, final Aggregate<?> aggregate);
Children unionWith(final Class<?> collection, final List<? extends Bson> aggregate);
Children unionWith(final Bson bson);
Children unwind(final String fieldName);
<T> Children unwind(final SFunction<T, ?> fieldName);
Children unwind(final String fieldName, final UnwindOption unwindOption);
<T> Children unwind(final SFunction<T, ?> fieldName, final UnwindOption unwindOption);
Children unwind(final Bson bson);
Children out(final String collectionName);
Children out(final Class<?> collection);
Children out(final String databaseName, final String collectionName);
Children out(final Bson bson);
Children merge(final String collectionName);
Children merge(final Class<?> collection);
Children merge(final MongoNamespace namespace);
Children merge(final String collectionName, final MergeOptions options);
Children merge(final Class<?> collection, final MergeOptions options);
Children merge(final MongoNamespace namespace, final MergeOptions options);
Children merge(final Bson bson);
<TExpression> Children replaceRoot(final TExpression fieldName);
Children replaceRoot(final Document value);
<T> Children replaceRoot(final SFunction<T, ?> fieldName);
Children replaceRoot(final Bson bson);
<TExpression> Children replaceWith(final TExpression fieldName);
Children replaceWith(final Document value);
<T> Children replaceWith(final SFunction<T, ?> fieldName);
Children replaceWith(final Bson bson);
Children sample(final Number size);
Children sample(final Bson bson);
<TExpression> Children setWindowFields(@Nullable final TExpression partitionBy, @Nullable final Bson sortBy, final WindowOutputField output, final WindowOutputField... moreOutput);
<TExpression> Children setWindowFields(@Nullable final TExpression partitionBy, @Nullable final Bson sortBy, final Iterable<? extends WindowOutputField> output);
Children setWindowFields(Bson bson);
Children densify(final String field, final DensifyRange range);
<T> Children densify(final SFunction<T, ?> field, final DensifyRange range);
Children densify(final String field, final DensifyRange range, final DensifyOptions options);
<T> Children densify(final SFunction<T, ?> field, final DensifyRange range, final DensifyOptions options);
Children densify(final Bson bson);
Children fill(final FillOptions options, final FillOutputField output, final FillOutputField... moreOutput);
Children fill(final FillOptions options, final Iterable<? extends FillOutputField> output);
Children fill(final Bson bson);
Children unset(final String... field);
@SuppressWarnings("unchecked") <T> Children unset(final SFunction<T, ?>... field);
Children unset(final List<String> fields);
<T> Children unsetLambda(final List<SFunction<T, ?>> fields);
Children unset(final Bson bson);
Children custom(final Bson bson);
```

## com.mongoplus.aggregate.AggregateOptions · mongo-plus-core

`import com.mongoplus.aggregate.AggregateOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/AggregateOptions.java#L10)。类型：INTERFACE；类型参数 `Children`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.CollationStrength;
import org.bson.BsonValue;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Children allowDiskUse(boolean allowDiskUse);
Children batchSize(Integer size);
Children collation(CollationStrength collationStrength);
Children maxTimeMS(long maxTime);
Children maxAwaitTimeMS(long maxAwaitTime);
Children bypassDocumentValidation(boolean bypassDocumentValidation);
Children comment(BsonValue comment);
Children comment(String comment);
Children hint(Bson hint);
Children hint(String hint);
Children let(Bson variables);
```

## com.mongoplus.aggregate.AggregateWrapper · mongo-plus-core

`import com.mongoplus.aggregate.AggregateWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/AggregateWrapper.java#L7)。类型：CLASS；类型参数 ``；extends `LambdaAggregateWrapper<AggregateWrapper>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AggregateWrapper();
```

## com.mongoplus.aggregate.ChainAggregate · mongo-plus-core

`import com.mongoplus.aggregate.ChainAggregate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/ChainAggregate.java#L5)。类型：INTERFACE；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
List<T> list();
<R> List<R> list(Class<R> rClazz);
T one();
<R> R one(Class<R> rClazz);
```

## com.mongoplus.aggregate.LambdaAggregateChainWrapper · mongo-plus-core

`import com.mongoplus.aggregate.LambdaAggregateChainWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateChainWrapper.java#L10)。类型：CLASS；类型参数 `T`；extends `LambdaAggregateWrapper<LambdaAggregateChainWrapper<T>>`；implements `ChainAggregate<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapper.BaseMapper;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public LambdaAggregateChainWrapper(BaseMapper baseMapper, Class<T> clazz);
@Override public List<T> list();
@Override public <R> List<R> list(Class<R> rClazz);
@Override public T one();
@Override public <R> R one(Class<R> rClazz);
```

## com.mongoplus.aggregate.LambdaAggregateWrapper · mongo-plus-core

`import com.mongoplus.aggregate.LambdaAggregateWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L33)。类型：CLASS；类型参数 `Children`；extends `null`；implements `Aggregate<Children>, AggregateOptions<Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.MongoNamespace;
import com.mongodb.client.model.*;
import com.mongodb.client.model.densify.DensifyOptions;
import com.mongodb.client.model.densify.DensifyRange;
import com.mongodb.client.model.fill.FillOptions;
import com.mongodb.client.model.fill.FillOutputField;
import com.mongoplus.aggregate.pipeline.UnwindOption;
import com.mongoplus.model.Projection;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.constant.AggregationOperators;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.enums.AggregateOptionsEnum;
import com.mongoplus.enums.OrderEnum;
import com.mongoplus.enums.ProjectionEnum;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.model.aggregate.Field;
import com.mongoplus.support.SFunction;
import org.bson.*;
import org.bson.conversions.Bson;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import static com.mongodb.assertions.Assertions.notNull;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public List<Bson> getAggregateConditionList();
@Override public boolean isSkip();
@Override public BasicDBObject getAggregateOptions();
@Override public Children addFields(String field, String value);
@Override public <T> Children addFields(SFunction<T, ?> field, String value);
@Override public <T> Children addFields(String value, SFunction<T, ?>... field);
@Override public <T> Children addFields(SFunction<T, ?> field, Object value);
@Override public <T> Children addFields(SFunction<T, ?> field, Collection<?> value);
@Override public Children addFields(String field, Collection<?> value);
@Override public Children addFields(Field<?>... fields);
@Override public Children addFields(List<Field<?>> fields);
@Override public Children addFields(Bson bson);
@Override public Children set(String field, String value);
@Override public <T> Children set(SFunction<T, ?> field, String value);
@Override public <T> Children set(String value, SFunction<T, ?>... field);
@Override public <T> Children set(SFunction<T, ?> field, Object value);
@Override public <T> Children set(SFunction<T, ?> field, Collection<?> value);
@Override public Children set(String field, Collection<?> value);
@Override public Children set(Field<?>... fields);
@Override public Children set(List<Field<?>> fields);
@Override public Children set(Bson bson);
@Override public <Boundary, T> Children bucket(SFunction<T, ?> groupBy, List<Boundary> boundaries);
@Override public <Boundary> Children bucket(Object groupBy, List<Boundary> boundaries);
@Override public <Boundary, T> Children bucket(SFunction<T, ?> groupBy, List<Boundary> boundaries, BucketOptions options);
@Override public <Boundary> Children bucket(Object groupBy, List<Boundary> boundaries, BucketOptions options);
@Override public Children bucket(Bson bson);
@Override public <T> Children bucketAuto(SFunction<T, ?> groupBy, Integer buckets);
@Override public Children bucketAuto(Object groupBy, Integer buckets);
@Override public <T> Children bucketAuto(SFunction<T, ?> groupBy, Integer buckets, BucketAutoOptions options);
@Override public Children bucketAuto(Object groupBy, Integer buckets, BucketAutoOptions options);
@Override public Children bucketAuto(Bson bson);
@Override public Children count();
@Override public Children count(String field);
@Override public <T> Children count(SFunction<T, ?> field);
@Override public Children match(Wrapper<?> queryWrapper);
@Override public Children match(final SFunction<QueryWrapper<?>, QueryWrapper<?>> function);
@Override public Children match(Bson bson);
@Override public <T, R> Children projectDisplay(SFunction<T, R>... column);
@Override public Children projectDisplay(String... column);
@Override public <T, R> Children projectNone(SFunction<T, R>... column);
@Override public Children projectNone(String... column);
@Override public Children project(boolean displayId, Projection... projection);
@Override public Children project(Projection... projection);
@Override public Children project(boolean displayId, Collection<? extends Projection> projection);
@Override public Children project(Collection<? extends Projection> projection);
@Override public <T, R> Children projectDisplay(boolean displayId, SFunction<T, R>... column);
@Override public Children projectDisplay(boolean displayId, String... column);
@Override public <T, R> Children projectNone(boolean displayId, SFunction<T, R>... column);
@Override public Children projectNone(boolean displayId, String... column);
@Override public Children project(Bson bson);
@Override public Children sort(String field, Integer value);
@Override public <T> Children sort(SFunction<T, ?> field, Integer value);
@Override public <T> Children sortAsc(SFunction<T, ?> field);
@Override public Children sortAsc(String field);
@Override public <T> Children sortAsc(SFunction<T, ?>... field);
@Override public Children sortAsc(String... field);
@Override public <T> Children sortAscLambda(List<SFunction<T, ?>> field);
@Override public Children sortAsc(List<String> field);
@Override public <T> Children sortDesc(SFunction<T, ?> field);
@Override public Children sortDesc(String field);
@Override public <T> Children sortDesc(SFunction<T, ?>... field);
@Override public Children sortDesc(String... field);
@Override public <T> Children sortDescLambda(List<SFunction<T, ?>> field);
@Override public Children sortDesc(List<String> field);
@Override public Children metaTextScore(String field);
@Override public <T> Children metaTextScore(SFunction<T, ?> field);
@Override public Children sort(Bson bson);
@Override public Children sortByCount(String field);
@Override public <T> Children sortByCount(SFunction<T, ?> field);
@Override public Children skip(long skip);
@Override public Children skip(int skip);
@Override public Children limit(long limit);
@Override public Children limit(int limit);
@Override public Children lookup(String from, String localField, String foreignField, String as);
@Override public <T> Children lookup(String from, String localField, String foreignField, SFunction<T, ?> as);
@Override public Children lookup(Class<?> from, String localField, String foreignField, String as);
@Override public <T> Children lookup(Class<?> from, String localField, String foreignField, SFunction<T, ?> as);
@Override public <T, R> Children lookup(String from, SFunction<T, ?> localField, SFunction<R, ?> foreignField, String as);
@Override public <T, R, A> Children lookup(String from, SFunction<T, ?> localField, SFunction<R, ?> foreignField, SFunction<A, ?> as);
@Override public <T, R> Children lookup(Class<?> from, SFunction<T, ?> localField, SFunction<R, ?> foreignField, String as);
@Override public <T, R, A> Children lookup(Class<?> from, SFunction<T, ?> localField, SFunction<R, ?> foreignField, SFunction<A, ?> as);
@Override public <T> Children lookup(String from, SFunction<T, ?> localField, String foreignField, String as);
@Override public <T, A> Children lookup(String from, SFunction<T, ?> localField, String foreignField, SFunction<A, ?> as);
@Override public <T> Children lookup(Class<?> from, SFunction<T, ?> localField, String foreignField, String as);
@Override public <T, A> Children lookup(Class<?> from, SFunction<T, ?> localField, String foreignField, SFunction<A, ?> as);
@Override public <T> Children lookup(String from, String localField, SFunction<T, ?> foreignField, String as);
@Override public <T, A> Children lookup(String from, String localField, SFunction<T, ?> foreignField, SFunction<A, ?> as);
@Override public <T> Children lookup(Class<?> from, String localField, SFunction<T, ?> foreignField, String as);
@Override public <T, A> Children lookup(Class<?> from, String localField, SFunction<T, ?> foreignField, SFunction<A, ?> as);
@Override @SuppressWarnings("rawtypes") public <TExpression> Children lookup(String from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, String as);
@Override public <TExpression, T> Children lookup(String from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, SFunction<T, ?> as);
@Override public <TExpression> Children lookup(Class<?> from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, String as);
@Override public <TExpression, T> Children lookup(Class<?> from, List<Variable<TExpression>> letList, Aggregate<?> aggregate, SFunction<T, ?> as);
@Override public Children lookup(String from, Aggregate<?> aggregate, String as);
@Override public <T> Children lookup(String from, Aggregate<?> aggregate, SFunction<T, ?> as);
@Override public Children lookup(Class<?> from, Aggregate<?> aggregate, String as);
@Override public <T> Children lookup(Class<?> from, Aggregate<?> aggregate, SFunction<T, ?> as);
@Override public Children lookup(Bson bson);
@Override public Children facet(String name, Bson... pipeline);
@Override public Children facet(String name, List<? extends Bson> pipeline);
@Override public Children facet(String name, Aggregate<?> aggregate);
@Override public Children facet(Facet... facets);
@Override public Children facet(List<Facet> facets);
@Override public Children facet(Bson bson);
@Override public Children graphLookup(String from, Object startWith, String connectFromField, String connectToField, String as);
@Override public <T, R, U> Children graphLookup(String from, SFunction<T, ?> startWith, SFunction<R, ?> connectFromField, SFunction<U, ?> connectToField, String as);
@Override public <T, R> Children graphLookup(String from, Object startWith, SFunction<T, ?> connectFromField, SFunction<R, ?> connectToField, String as);
@Override public Children graphLookup(String from, Object startWith, String connectFromField, String connectToField, String as, GraphLookupOptions options);
@Override public <T, R, U> Children graphLookup(String from, SFunction<T, ?> startWith, SFunction<R, ?> connectFromField, SFunction<U, ?> connectToField, String as, GraphLookupOptions options);
@Override public <T, R> Children graphLookup(String from, Object startWith, SFunction<T, ?> connectFromField, SFunction<R, ?> connectToField, String as, GraphLookupOptions options);
@Override public Children graphLookup(Bson bson);
@Override public Children group(String _id);
@Override public <T> Children group(SFunction<T, ?> _id);
@Override public <TExpression> Children group(TExpression id, BsonField... fieldAccumulators);
@Override public <T, TExpression> Children group(SFunction<T, ?> id, BsonField... fieldAccumulators);
@Override public <TExpression> Children group(TExpression id, List<BsonField> fieldAccumulators);
@Override public <T, TExpression> Children group(SFunction<T, ?> id, List<BsonField> fieldAccumulators);
@Override public Children group(Bson bson);
@Override public Children unionWith(String collectionName);
@Override public Children unionWith(Class<?> collection);
@Override public Children unionWith(String collectionName, Aggregate<?> aggregate);
@Override public Children unionWith(String collectionName, List<? extends Bson> aggregate);
@Override public Children unionWith(Class<?> collection, Aggregate<?> aggregate);
@Override public Children unionWith(Class<?> collection, List<? extends Bson> aggregate);
@Override public Children unionWith(Bson bson);
@Override public Children unwind(String fieldName);
@Override public <T> Children unwind(SFunction<T, ?> fieldName);
@Override public Children unwind(String fieldName, UnwindOption unwindOption);
@Override public <T> Children unwind(SFunction<T, ?> fieldName, UnwindOption unwindOption);
@Override public Children unwind(Bson bson);
@Override public Children out(String collectionName);
@Override public Children out(Class<?> collection);
@Override public Children out(String databaseName, String collectionName);
@Override public Children out(Bson bson);
@Override public Children merge(String collectionName);
@Override public Children merge(Class<?> collection);
@Override public Children merge(MongoNamespace namespace);
@Override public Children merge(String collectionName, MergeOptions options);
@Override public Children merge(Class<?> collection, MergeOptions options);
@Override public Children merge(MongoNamespace namespace, MergeOptions options);
@Override public Children merge(Bson bson);
@Override public <TExpression> Children replaceRoot(TExpression fieldName);
@Override public Children replaceRoot(Document value);
@Override public <T> Children replaceRoot(SFunction<T, ?> fieldName);
@Override public Children replaceRoot(Bson bson);
@Override public <TExpression> Children replaceWith(TExpression fieldName);
@Override public Children replaceWith(Document value);
@Override public <T> Children replaceWith(SFunction<T, ?> fieldName);
@Override public Children replaceWith(Bson bson);
@Override public Children sample(Number size);
@Override public Children sample(Bson bson);
@Override public <TExpression> Children setWindowFields(TExpression partitionBy, Bson sortBy, WindowOutputField output, WindowOutputField... moreOutput);
@Override public <TExpression> Children setWindowFields(TExpression partitionBy, Bson sortBy, Iterable<? extends WindowOutputField> output);
@Override public Children setWindowFields(Bson bson);
@Override public Children densify(String field, DensifyRange range);
@Override public <T> Children densify(SFunction<T, ?> field, DensifyRange range);
@Override public Children densify(String field, DensifyRange range, DensifyOptions options);
@Override public <T> Children densify(SFunction<T, ?> field, DensifyRange range, DensifyOptions options);
@Override public Children densify(Bson bson);
@Override public Children fill(FillOptions options, FillOutputField output, FillOutputField... moreOutput);
@Override public Children fill(FillOptions options, Iterable<? extends FillOutputField> output);
@Override public Children fill(Bson bson);
@Override public Children unset(String... field);
@Override public <T> Children unset(SFunction<T, ?>... field);
@Override public Children unset(List<String> fields);
@Override public <T> Children unsetLambda(List<SFunction<T, ?>> fields);
@Override public Children unset(Bson bson);
@Override public Children allowDiskUse(boolean allowDiskUse);
@Override public Children batchSize(Integer size);
@Override public Children collation(CollationStrength collationStrength);
@Override public Children maxTimeMS(long time);
@Override public Children maxAwaitTimeMS(long maxAwaitTime);
@Override public Children bypassDocumentValidation(boolean bypassDocumentValidation);
@Override public Children comment(BsonValue comment);
@Override public Children comment(String comment);
@Override public Children hint(Bson hint);
@Override public Children hint(String hint);
@Override public Children let(Bson variables);
@Override public Children custom(Bson bson);
```

## com.mongoplus.model.aggregate.Field · mongo-plus-core

`import com.mongoplus.model.aggregate.Field;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/aggregate/Field.java#L10)。类型：CLASS；类型参数 `TExpression`；extends `com.mongodb.client.model.Field<TExpression>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T, R, TExpression> Field<TExpression> of(final SFunction<T, R> name, final TExpression value);
public static <TExpression> Field<TExpression> of(final String name, final TExpression value);
public static <T, R, TExpression> Field<TExpression> of(final Boolean isField, final SFunction<T, R> name, final TExpression value);
public static <T, R, TExpression> Field<TExpression> ofNotField(final SFunction<T, R> name, final TExpression value);
public Field(final Boolean isField, String name, final TExpression value);
public Field(String name, final TExpression value);
```
