# 当前公开API签名：expressions

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.aggregate.pipeline.Accumulators · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.Accumulators;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Accumulators.java#L26)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.BsonField;
import com.mongodb.lang.Nullable;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.support.SFunction;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.mongodb.assertions.Assertions.notNull;
import static java.util.stream.Collectors.toList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <TExpression> BsonField sum(final String fieldName, final TExpression expression);
public static <TExpression> BsonField sum();
public static <TExpression, T> BsonField sum(final SFunction<T, ?> fieldName, final TExpression expression);
public static <TExpression> BsonField count(final String fieldName, final TExpression expression);
public static <T, TExpression> BsonField count(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, TExpression> BsonField count(final SFunction<T, ?> fieldName);
public static <TExpression> BsonField count(final String fieldName);
public static <TExpression> BsonField avg(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField avg(final SFunction<T, ?> fieldName, final TExpression expression);
public static <TExpression> BsonField first(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField first(final SFunction<T, ?> fieldName, final TExpression expression);
public static <InExpression, NExpression> BsonField firstN(final String fieldName, final NExpression nExpression, final InExpression inExpression);
public static <InExpression, NExpression, T> BsonField firstN(final SFunction<T, ?> fieldName, final NExpression nExpression, final InExpression inExpression);
public static <NExpression, T, R> BsonField firstN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?> inExpression);
public static <NExpression, T> BsonField firstN(final String fieldName, final NExpression nExpression, final SFunction<T, ?> inExpression);
@SafeVarargs public static <NExpression, T> BsonField firstN(final String fieldName, final NExpression nExpression, final SFunction<T, ?>... inExpression);
public static <NExpression> BsonField firstN(final String fieldName, final NExpression nExpression, final String... inExpression);
public static <OutExpression> BsonField top(final String fieldName, final Bson sortBy, final OutExpression outExpression);
public static <T> BsonField top(final String fieldName, final Bson sortBy, final SFunction<T, ?> outExpression);
public static <OutExpression, T> BsonField top(final SFunction<T, ?> fieldName, final Bson sortBy, final OutExpression outExpression);
public static <T, R> BsonField top(final SFunction<T, ?> fieldName, final Bson sortBy, final SFunction<R, ?> outExpression);
@SafeVarargs public static <T, R> BsonField top(final SFunction<T, ?> fieldName, final Bson sortBy, final SFunction<R, ?>... outExpression);
public static <T> BsonField top(final SFunction<T, ?> fieldName, final Bson sortBy, final String... outExpression);
public static <OutExpression, NExpression> BsonField topN(final String fieldName, final Bson sortBy, final NExpression nExpression, final OutExpression outExpression);
public static <OutExpression, NExpression, T> BsonField topN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final OutExpression outExpression);
public static <NExpression, T, R> BsonField topN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final SFunction<R, ?> outExpression);
@SafeVarargs public static <NExpression, T, R> BsonField topN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final SFunction<R, ?>... outExpression);
public static <NExpression, T> BsonField topN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final String... outExpression);
public static <TExpression> BsonField last(final String fieldName, final TExpression expression);
public static <T, R> BsonField last(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
public static <TExpression, T> BsonField last(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T> BsonField last(final String fieldName, final SFunction<T, ?> expression);
public static <InExpression, NExpression> BsonField lastN(final String fieldName, final NExpression nExpression, final InExpression inExpression);
public static <InExpression, NExpression, T> BsonField lastN(final SFunction<T, ?> fieldName, final NExpression nExpression, final InExpression inExpression);
public static <NExpression, T, R> BsonField lastN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?> inExpression);
public static <NExpression, T> BsonField lastN(final SFunction<T, ?> fieldName, final NExpression nExpression, final String... inExpression);
@SafeVarargs public static <NExpression, T, R> BsonField lastN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?>... inExpression);
public static <OutExpression> BsonField bottom(final String fieldName, final Bson sortBy, final OutExpression outExpression);
public static <OutExpression, T> BsonField bottom(final SFunction<T, ?> fieldName, final Bson sortBy, final OutExpression outExpression);
public static <T, R> BsonField bottom(final SFunction<T, ?> fieldName, final Bson sortBy, final SFunction<R, ?> outExpression);
@SafeVarargs public static <T, R> BsonField bottom(final SFunction<T, ?> fieldName, final Bson sortBy, final SFunction<R, ?>... outExpression);
public static <T> BsonField bottom(final SFunction<T, ?> fieldName, final Bson sortBy, final String... outExpression);
public static <OutExpression, NExpression> BsonField bottomN(final String fieldName, final Bson sortBy, final NExpression nExpression, final OutExpression outExpression);
public static <OutExpression, NExpression, T> BsonField bottomN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final OutExpression outExpression);
public static <NExpression, T, R> BsonField bottomN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final SFunction<R, ?> outExpression);
@SafeVarargs public static <NExpression, T, R> BsonField bottomN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final SFunction<R, ?>... outExpression);
public static <NExpression, T> BsonField bottomN(final SFunction<T, ?> fieldName, final Bson sortBy, final NExpression nExpression, final String... outExpression);
public static <TExpression> BsonField max(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField max(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField max(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
public static <InExpression, NExpression> BsonField maxN(final String fieldName, final NExpression nExpression, final InExpression inExpression);
public static <InExpression, NExpression, T> BsonField maxN(final SFunction<T, ?> fieldName, final NExpression nExpression, final InExpression inExpression);
public static <NExpression, T, R> BsonField maxN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?> inExpression);
@SafeVarargs public static <NExpression, T, R> BsonField maxN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?>... inExpression);
public static <NExpression, T> BsonField maxN(final SFunction<T, ?> fieldName, final NExpression nExpression, final String... inExpression);
public static <TExpression> BsonField min(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField min(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField min(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
public static <InExpression, NExpression> BsonField minN(final String fieldName, final NExpression nExpression, final InExpression inExpression);
public static <InExpression, NExpression, T> BsonField minN(final SFunction<T, ?> fieldName, final NExpression nExpression, final InExpression inExpression);
public static <NExpression, T, R> BsonField minN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?> inExpression);
@SafeVarargs public static <NExpression, T, R> BsonField minN(final SFunction<T, ?> fieldName, final NExpression nExpression, final SFunction<R, ?>... inExpression);
public static <NExpression, T> BsonField minN(final SFunction<T, ?> fieldName, final NExpression nExpression, final String... inExpression);
public static <TExpression> BsonField push(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField push(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField push(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
@SafeVarargs public static <T, R> BsonField push(final SFunction<T, ?> fieldName, final SFunction<R, ?>... expression);
public static <TExpression> BsonField addToSet(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField addToSet(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField addToSet(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
public static <TExpression> BsonField mergeObjects(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField mergeObjects(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField mergeObjects(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
@SafeVarargs public static <T, R> BsonField mergeObjects(final SFunction<T, ?> fieldName, final SFunction<R, ?>... expression);
public static <TExpression> BsonField stdDevPop(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField stdDevPop(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField stdDevPop(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
public static <TExpression> BsonField stdDevSamp(final String fieldName, final TExpression expression);
public static <TExpression, T> BsonField stdDevSamp(final SFunction<T, ?> fieldName, final TExpression expression);
public static <T, R> BsonField stdDevSamp(final SFunction<T, ?> fieldName, final SFunction<R, ?> expression);
public static BsonField accumulator(final String fieldName, final String initFunction, final String accumulateFunction, final String mergeFunction);
public static <T> BsonField accumulator(final SFunction<T, ?> fieldName, final String initFunction, final String accumulateFunction, final String mergeFunction);
public static BsonField accumulator(final String fieldName, final String initFunction, final String accumulateFunction, final String mergeFunction, @Nullable final String finalizeFunction);
public static <T> BsonField accumulator(final SFunction<T, ?> fieldName, final String initFunction, final String accumulateFunction, final String mergeFunction, @Nullable final String finalizeFunction);
public static BsonField accumulator(final String fieldName, final String initFunction, @Nullable final List<String> initArgs, final String accumulateFunction, @Nullable final List<String> accumulateArgs, final String mergeFunction, @Nullable final String finalizeFunction);
public static <T> BsonField accumulator(final SFunction<T, ?> fieldName, final String initFunction, @Nullable final List<String> initArgs, final String accumulateFunction, @Nullable final List<String> accumulateArgs, final String mergeFunction, @Nullable final String finalizeFunction);
public static BsonField accumulator(final String fieldName, final String initFunction, final String accumulateFunction, final String mergeFunction, @Nullable final String finalizeFunction, final String lang);
public static <T> BsonField accumulator(final SFunction<T, ?> fieldName, final String initFunction, final String accumulateFunction, final String mergeFunction, @Nullable final String finalizeFunction, final String lang);
public static BsonField accumulator(final String fieldName, final String initFunction, @Nullable final List<String> initArgs, final String accumulateFunction, @Nullable final List<String> accumulateArgs, final String mergeFunction, @Nullable final String finalizeFunction, final String lang);
public static <T> BsonField accumulator(final SFunction<T, ?> fieldName, final String initFunction, @Nullable final List<String> initArgs, final String accumulateFunction, @Nullable final List<String> accumulateArgs, final String mergeFunction, @Nullable final String finalizeFunction, final String lang);
```

## com.mongoplus.aggregate.pipeline.AggregateOperator · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.AggregateOperator;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java#L19)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.enums.QueryOperatorEnum;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.List;
import static com.mongoplus.enums.CommonOperators.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Bson literal(Object value);
public static Bson eq(Object left, Object right);
public static Bson ne(Object left, Object right);
public static Bson gt(Object left, Object right);
public static Bson gte(Object left, Object right);
public static Bson lt(Object left, Object right);
public static Bson lte(Object left, Object right);
public static Bson subtract(Object left, Object right);
public static Bson divide(Object left, Object right);
public static Bson and(Object... expressions);
public static Bson or(Object... expressions);
public static Bson not(Object expression);
public static Bson concatArrays(List<?>... list);
public static Bson concatArraysExpressions(Object... operands);
public static Bson concat(Object... expression);
public static Bson concat(List<?> expressions);
public static Bson dateTrunc(SFunction<?, ?> field, String unit, Integer binSize, String startOfWeek, String timezone);
public static Bson dateTrunc(String field, String unit, Integer binSize, String startOfWeek, String timezone);
public static Bson dateTrunc(String field, String unit);
public static Bson dateTrunc(SFunction<?, ?> field, String unit);
```

## com.mongoplus.aggregate.pipeline.Facet · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.Facet;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Facet.java#L15)。类型：CLASS；类型参数 ``；extends `com.mongodb.client.model.Facet`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.aggregate.Aggregate;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.aggregate.LambdaAggregateWrapper;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public <T> Facet(SFunction<T, ?> name, List<? extends Bson> pipeline);
public <T> Facet(SFunction<T, ?> name, Bson... pipeline);
public Facet(String name, Aggregate<?> aggregateChainWrapper);
public Facet(String name, SFunction<LambdaAggregateWrapper<AggregateWrapper>, LambdaAggregateWrapper<AggregateWrapper>> function);
public <T> Facet(SFunction<T, ?> name, Aggregate<?> aggregateChainWrapper);
public <T, R> Facet(SFunction<T, ?> name, SFunction<LambdaAggregateWrapper<AggregateWrapper>, LambdaAggregateWrapper<AggregateWrapper>> function);
```

## com.mongoplus.aggregate.pipeline.FillField · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.FillField;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/FillField.java#L17)。类型：CLASS；类型参数 ``；extends `AbstractConstructibleBsonElement<FillField>`；implements `FillOutputField, ValueFillOutputField, LocfFillOutputField, LinearFillOutputField`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.WindowOutputFields;
import com.mongodb.client.model.fill.*;
import com.mongodb.internal.client.model.AbstractConstructibleBsonElement;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;
import static com.mongodb.assertions.Assertions.notNull;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public FillField(String name, Bson value);
public FillField(Bson baseElement, Bson appendedElementValue);
public FillField(Bson fill);
@Override public FillField newSelf(Bson baseElement, Bson appendedElementValue);
public static <TExpression> ValueFillOutputField value(final String field, TExpression expression);
public static <TExpression, T> ValueFillOutputField value(final SFunction<T, ?> field, TExpression expression);
public static LocfFillOutputField locf(final String field);
public static <T> LocfFillOutputField locf(final SFunction<T, ?> field);
public static LinearFillOutputField linear(final String field);
public static <T> LinearFillOutputField linear(final SFunction<T, ?> field);
public static FillField of(final Bson fill);
```

## com.mongoplus.aggregate.pipeline.Project · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.Project;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Project.java#L12)。类型：INTERFACE；类型参数 `Children`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.model.Projection;
import com.mongoplus.support.SFunction;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") <T, R> Children projectDisplay(SFunction<T, R>... column);
Children projectDisplay(String... column);
@SuppressWarnings("unchecked") <T, R> Children projectNone(SFunction<T, R>... column);
Children projectNone(String... column);
Children project(boolean displayId, Projection... projection);
Children project(Projection... projection);
Children project(boolean displayId, Collection<? extends Projection> projection);
Children project(Collection<? extends Projection> projection);
@SuppressWarnings("unchecked") <T, R> Children projectDisplay(boolean displayId, SFunction<T, R>... column);
Children projectDisplay(boolean displayId, String... column);
@SuppressWarnings("unchecked") <T, R> Children projectNone(boolean displayId, SFunction<T, R>... column);
Children projectNone(boolean displayId, String... column);
```

## com.mongoplus.aggregate.pipeline.Projections · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.Projections;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Projections.java#L30)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import com.mongodb.client.model.search.SearchCollector;
import com.mongodb.client.model.search.SearchCount;
import com.mongodb.client.model.search.SearchOperator;
import com.mongodb.client.model.search.SearchOptions;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.support.SFunction;
import org.bson.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.mongodb.assertions.Assertions.notNull;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
import static java.util.Arrays.asList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <TExpression> Bson computed(final String fieldName, final TExpression expression);
public static <TExpression, T> Bson computed(final SFunction<T, ?> fieldName, final TExpression expression);
public static Bson computedSearchMeta(final String fieldName);
public static <T> Bson computedSearchMeta(final SFunction<T, ?> fieldName);
public static Bson include(final String... fieldNames);
@SafeVarargs public static <T> Bson include(final SFunction<T, ?>... fieldNames);
public static Bson include(final List<String> fieldNames);
public static <T> Bson includeLambda(final List<SFunction<T, ?>> fieldNames);
public static Bson exclude(final String... fieldNames);
@SafeVarargs public static <T> Bson exclude(final SFunction<T, ?>... fieldNames);
public static Bson exclude(final List<String> fieldNames);
public static <T> Bson excludeLambda(final List<SFunction<T, ?>> fieldNames);
public static Bson excludeId();
public static Bson elemMatch(final String fieldName);
public static <T> Bson elemMatch(final SFunction<T, ?> fieldName);
public static Bson elemMatch(final String fieldName, final Bson filter);
public static Bson elemMatch(final String fieldName, final Wrapper<?> queryWrapper);
public static <T> Bson elemMatch(final SFunction<T, ?> fieldName, final Bson filter);
public static <T> Bson elemMatch(final SFunction<T, ?> fieldName, final Wrapper<?> queryWrapper);
public static Bson meta(final String fieldName, final String metaFieldName);
public static <T> Bson meta(final SFunction<T, ?> fieldName, final String metaFieldName);
public static <T, R> Bson meta(final SFunction<T, ?> fieldName, final SFunction<R, ?> metaFieldName);
public static Bson metaTextScore(final String fieldName);
public static <T> Bson metaTextScore(final SFunction<T, ?> fieldName);
public static Bson metaSearchScore(final String fieldName);
public static <T> Bson metaSearchScore(final SFunction<T, ?> fieldName);
public static Bson metaSearchHighlights(final String fieldName);
public static <T> Bson metaSearchHighlights(final SFunction<T, ?> fieldName);
public static Bson slice(final String fieldName, final int limit);
public static <T> Bson slice(final SFunction<T, ?> fieldName, final int limit);
public static Bson slice(final String fieldName, final int skip, final int limit);
public static <T> Bson slice(final SFunction<T, ?> fieldName, final int skip, final int limit);
public static Bson fields(final Bson... projections);
public static Bson fields(final List<? extends Bson> projections);
```

## com.mongoplus.aggregate.pipeline.SimpleExpression · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.SimpleExpression;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/SimpleExpression.java#L14)。类型：CLASS；类型参数 `TExpression`；extends `null`；implements `Bson`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SimpleExpression(final String name, final TExpression expression);
@Override public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry);
```

## com.mongoplus.aggregate.pipeline.Sorts · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.Sorts;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Sorts.java#L27)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.TextSearchOptions;
import com.mongoplus.model.Order;
import com.mongoplus.support.SFunction;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.mongodb.assertions.Assertions.notNull;
import static java.util.Arrays.asList;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Bson asc(final String... fieldNames);
@SafeVarargs public static <T> Bson asc(final SFunction<T, ?>... fieldNames);
public static <T> Bson ascLambda(final List<SFunction<T, ?>> fieldNames);
public static Bson asc(final List<String> fieldNames);
public static Bson desc(final String... fieldNames);
@SafeVarargs public static <T> Bson desc(final SFunction<T, ?>... fieldNames);
public static <T> Bson descLambda(final List<SFunction<T, ?>> fieldNames);
public static Bson desc(final List<String> fieldNames);
public static Bson metaTextScore(final String fieldName);
public static <T> Bson metaTextScore(final SFunction<T, ?> fieldName);
public static Bson orderBy(final Bson... sorts);
public static Bson orderBy(final Order... orders);
public static Bson orderBy(final List<? extends Bson> sorts);
```

## com.mongoplus.aggregate.pipeline.UnwindOption · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.UnwindOption;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/UnwindOption.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.lang.Nullable;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Nullable public Boolean isPreserveNullAndEmptyArrays();
public UnwindOption preserveNullAndEmptyArrays(@Nullable final Boolean preserveNullAndEmptyArrays);
@Nullable public String getIncludeArrayIndex();
public UnwindOption includeArrayIndex(@Nullable final String arrayIndexFieldName);
public <T> UnwindOption includeArrayIndex(@Nullable final SFunction<T, ?> arrayIndexFieldName);
@Override public String toString();
```

## com.mongoplus.aggregate.pipeline.Variable · mongo-plus-core

`import com.mongoplus.aggregate.pipeline.Variable;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Variable.java#L10)。类型：CLASS；类型参数 `TExpression`；extends `com.mongodb.client.model.Variable<TExpression>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") public <T, R> Variable(String name, SFunction<T, R> value);
@SuppressWarnings("unchecked") public Variable(String name, String value);
```

## com.mongoplus.conditions.interfaces.ConditionOperators · mongo-plus-core

`import com.mongoplus.conditions.interfaces.ConditionOperators;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/ConditionOperators.java#L23)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.enums.CommonOperators;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import static com.mongoplus.enums.CommonOperators.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Bson cond(Object ifValue, Object thenValue, Object elseValue);
public static Bson condArray(Object ifValue, Object thenValue, Object elseValue);
public static Bson cond(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue);
public static Bson condArray(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue);
public static Bson multiply(Object... values);
public static Bson multiply(SFunction<?, ?>... values);
public static Bson multiply(Collection<?> values);
public static Bson multiplyLambda(Collection<SFunction<?, ?>> values);
public static Bson dateToString(String date);
public static <T> Bson dateToString(SFunction<T, ?> date);
public static Bson dateToString(String format, String date);
public static <T> Bson dateToString(String format, SFunction<T, ?> date);
public static Bson dateToString(String format, String date, String timezone);
public static <T> Bson dateToString(String format, SFunction<T, ?> date, String timezone);
public static Bson dateToString(String format, String date, String timezone, Object onNull);
public static <T> Bson dateToString(String format, SFunction<T, ?> date, String timezone, Object onNull);
public static Document mergeObjects(String value);
public static <T> Document mergeObjects(SFunction<T, ?> value);
public static <T> Document mergeObjectsOption(SFunction<T, ?> value);
public static Document mergeObjects(Collection<?> values);
public static <T> Document mergeObjectsLambda(Collection<? extends SFunction<T, ?>> functions);
public static <T> Document mergeObjectsLambdaOption(Collection<? extends SFunction<T, ?>> functions);
public static <T> Document mergeObjects(Object... values);
public static Bson each(Object... values);
public static Bson eachPosition(Number position, Object... values);
public static Bson eachSlice(Number slice, Object... values);
public static Bson eachSort(Object sort, Object... values);
public static Bson each(Collection<?> values);
public static Bson eachPosition(Number position, Collection<?> values);
public static Bson eachSlice(Number slice, Collection<?> values);
public static Bson eachSort(Object sort, Collection<?> values);
public static Bson abs(Number value);
public static <TExpression> Bson toDate(TExpression expression);
public static Bson toDate(SFunction<?, ?> field);
public static Bson dateFromString(Object dateString, Object format, Object timezone, Object onError, Object onNull);
public static Bson dateFromString(Object dateString);
public static Bson dateFromString(SFunction<?, ?> field);
public static Bson dateFromString(SFunction<?, ?> dateString, Object timezone);
public static Bson dateFromString(SFunction<?, ?> dateString, SFunction<?, ?> timezone);
public static <TExpression> Bson toBool(TExpression expression);
public static <T> Bson toBool(SFunction<T, Object> field);
public static <TExpression> Bson toDecimal(TExpression expression);
public static <T> Bson toDecimal(SFunction<T, Object> field);
public static <TExpression> Bson toDouble(TExpression expression);
public static <T> Bson toHashedIndexKey(String key);
public static <T> Bson toHashedIndexKey(SFunction<T, Object> field);
public static <TExpression> Bson toInt(TExpression expression);
public static <T> Bson toInt(SFunction<T, Object> field);
public static <TExpression> Bson toLong(TExpression expression);
public static <T> Bson toLong(SFunction<T, Object> field);
public static <TExpression> Bson toObjectId(TExpression expression);
public static <T> Bson toObjectId(SFunction<T, Object> field);
public static <TExpression> Bson toString(TExpression expression);
public static <T> Bson toString(SFunction<T, Object> field);
public static <T> Bson substrBytes(SFunction<T, ?> field, Number index, Number count);
public static <TExpression> Bson substrBytes(TExpression expression, Number index, Number count);
public static <TExpression> Bson substrBytes(TExpression expression, Object index, Object count);
public static <T> Bson substrBytes(SFunction<T, ?> field, Object index, Object count);
public static Bson ifNull(List<?> inputExpressions);
public static Bson ifNull(Object... inputExpressions);
@SuppressWarnings("unchecked") public static <TExpression> Bson sum(TExpression... expressions);
@SuppressWarnings("unchecked") public static <T> Bson sum(SFunction<T, ?>... expressions);
public static Bson sum(List<?> expressions);
@SuppressWarnings("unchecked") public static <TExpression> Bson add(TExpression... expressions);
@SuppressWarnings("unchecked") public static <T> Bson add(SFunction<T, ?>... expressions);
public static Bson add(List<?> expressions);
```

## com.mongoplus.conditions.operation.ConditionOperators · mongo-plus-core

`import com.mongoplus.conditions.operation.ConditionOperators;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java#L25)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.enums.CommonOperators;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import static com.mongoplus.enums.CommonOperators.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Bson cond(Object ifValue, Object thenValue, Object elseValue);
public static Bson condArray(Object ifValue, Object thenValue, Object elseValue);
public static Bson cond(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue);
public static Bson condArray(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue);
public static Bson multiply(Object... values);
public static Bson multiply(SFunction<?, ?>... values);
public static Bson multiply(Collection<?> values);
public static Bson multiplyLambda(Collection<SFunction<?, ?>> values);
public static Bson dateToString(String date);
public static <T> Bson dateToString(SFunction<T, ?> date);
public static Bson dateToString(String format, String date);
public static <T> Bson dateToString(String format, SFunction<T, ?> date);
public static Bson dateToString(String format, String date, String timezone);
public static <T> Bson dateToString(String format, SFunction<T, ?> date, String timezone);
public static Bson dateToString(String format, String date, String timezone, Object onNull);
public static <T> Bson dateToString(String format, SFunction<T, ?> date, String timezone, Object onNull);
public static Document mergeObjects(String value);
public static <T> Document mergeObjects(SFunction<T, ?> value);
public static <T> Document mergeObjectsOption(SFunction<T, ?> value);
public static Document mergeObjects(Collection<?> values);
public static <T> Document mergeObjectsLambda(Collection<? extends SFunction<T, ?>> functions);
public static <T> Document mergeObjectsLambdaOption(Collection<? extends SFunction<T, ?>> functions);
public static <T> Document mergeObjects(Object... values);
public static Bson each(Object... values);
public static Bson eachPosition(Number position, Object... values);
public static Bson eachSlice(Number slice, Object... values);
public static Bson eachSort(Object sort, Object... values);
public static Bson each(Collection<?> values);
public static Bson eachPosition(Number position, Collection<?> values);
public static Bson eachSlice(Number slice, Collection<?> values);
public static Bson eachSort(Object sort, Collection<?> values);
public static Bson abs(Number value);
public static <TExpression> Bson toDate(TExpression expression);
public static Bson toDate(SFunction<?, ?> field);
public static Bson dateFromString(Object dateString, Object format, Object timezone, Object onError, Object onNull);
public static Bson dateFromString(Object dateString);
public static Bson dateFromString(SFunction<?, ?> field);
public static Bson dateFromString(SFunction<?, ?> dateString, Object timezone);
public static Bson dateFromString(SFunction<?, ?> dateString, SFunction<?, ?> timezone);
public static <TExpression> Bson toBool(TExpression expression);
public static <T> Bson toBool(SFunction<T, Object> field);
public static <TExpression> Bson toDecimal(TExpression expression);
public static <T> Bson toDecimal(SFunction<T, Object> field);
public static <TExpression> Bson toDouble(TExpression expression);
public static <T> Bson toHashedIndexKey(String key);
public static <T> Bson toHashedIndexKey(SFunction<T, Object> field);
public static <TExpression> Bson toInt(TExpression expression);
public static <T> Bson toInt(SFunction<T, Object> field);
public static <TExpression> Bson toLong(TExpression expression);
public static <T> Bson toLong(SFunction<T, Object> field);
public static <TExpression> Bson toObjectId(TExpression expression);
public static <T> Bson toObjectId(SFunction<T, Object> field);
public static <TExpression> Bson toString(TExpression expression);
public static <T> Bson toString(SFunction<T, Object> field);
public static <T> Bson substrBytes(SFunction<T, ?> field, Number index, Number count);
public static <TExpression> Bson substrBytes(TExpression expression, Number index, Number count);
public static <TExpression> Bson substrBytes(TExpression expression, Object index, Object count);
public static <T> Bson substrBytes(SFunction<T, ?> field, Object index, Object count);
public static Bson ifNull(List<?> inputExpressions);
public static Bson ifNull(Object... inputExpressions);
@SuppressWarnings("unchecked") public static <TExpression> Bson sum(TExpression... expressions);
@SuppressWarnings("unchecked") public static <T> Bson sum(SFunction<T, ?>... expressions);
public static Bson sum(List<?> expressions);
@SuppressWarnings("unchecked") public static <TExpression> Bson add(TExpression... expressions);
@SuppressWarnings("unchecked") public static <T> Bson add(SFunction<T, ?>... expressions);
public static Bson add(List<?> expressions);
```
