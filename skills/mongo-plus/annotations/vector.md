# 注解：vector

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：MongoVectorField、MongoVectorFields、MongoVectorHnswOptions、MongoVectorIndex、MongoVectorIndexes、MongoVectorStoredSource。

## MongoVectorField

`import com.mongoplus.annotation.index.vector.MongoVectorField;`

- 作用域：`@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})`；保留策略：RUNTIME；@Repeatable(MongoVectorFields.class)。
- 实际用途：声明独立vector或filter路径，可重复。
- 生效条件：字段配置或MongoVectorIndex.fields；多索引用name指定。
- 限制与常见错误：vector维度与相似度必填；filter不能含向量选项；$属性路径可映射，裸路径是MongoDB字段。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `""` | 所属索引。类上仅一个 MongoVectorIndex 时可省略，多个时必须明确指定。 无类级索引时按此名称分组；留空创建 default。嵌套在 fields 中时必须留空。 |
| `fieldName` | `String` | `""` | 实际 MongoDB 字段路径；以 $ 开头时按实体属性逐段转换，包括 ID、重命名和驼峰配置。 直接标注实体字段时可省略，使用该字段的实际映射名称；类级 fields 中必填。 |
| `type` | `VectorFieldType` | `VectorFieldType.VECTOR` | 字段类型，默认 vector。 |
| `numDimensions` | `int` | `-1` | vector 必填，范围 1..8192；-1 表示未指定。binary 量化时必须是 8 的倍数。 |
| `similarity` | `SearchVectorSimilarity` | `SearchVectorSimilarity.DEFAULT` | vector 必填，无服务端默认值；支持 euclidean、cosine、dotProduct。 |
| `quantization` | `SearchQuantization` | `SearchQuantization.EMPTY` | 自动量化方式；EMPTY 表示省略，由服务端采用 none。预量化向量应采用 none。 |
| `indexingMethod` | `SearchIndexingMethod` | `SearchIndexingMethod.NONE` | NONE 表示省略，由服务端采用 hnsw；flat 与 hnswOptions 互斥。 |
| `hnswOptions` | `MongoVectorHnswOptions` | `@MongoVectorHnswOptions` | HNSW 图构建配置；未指定成员时省略整个对象。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.enums.SearchVectorSimilarity;

@CollectionName("example_mongovectorfield") @MongoVectorIndex(name="vec") public class MongoVectorFieldExample { @MongoVectorField(numDimensions=3,similarity=SearchVectorSimilarity.COSINE) java.util.List<Double> embedding; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorField.java#L14)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoVectorFields

`import com.mongoplus.annotation.index.vector.MongoVectorFields;`

- 作用域：`@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：多个Vector字段归属容器。
- 生效条件：同MongoVectorField。
- 限制与常见错误：不是一个索引内重复同一路径；适于同字段归属多个不同索引。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `MongoVectorField[]` | `必填（无默认值）` | 字段声明，使用 getAnnotationsByType 读取。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorFields;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.annotation.index.vector.MongoVectorIndexes;
import com.mongoplus.enums.SearchVectorSimilarity;

@CollectionName("example_mongovectorfields") @MongoVectorIndexes({@MongoVectorIndex(name="a"),@MongoVectorIndex(name="b")}) public class MongoVectorFieldsExample { @MongoVectorFields({@MongoVectorField(name="a",numDimensions=3,similarity=SearchVectorSimilarity.COSINE),@MongoVectorField(name="b",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)}) java.util.List<Double> embedding; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorFields.java#L6)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoVectorHnswOptions

`import com.mongoplus.annotation.index.vector.MongoVectorHnswOptions;`

- 作用域：`@Target({})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：HNSW图构建可选参数。
- 生效条件：仅嵌入MongoVectorField.hnswOptions。
- 限制与常见错误：@Target({})；-1省略；flat与HNSW选项互斥；范围检查不证明服务端支持。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `maxEdges` | `int` | `-1` | 最大连接数，范围 16..64；未指定时服务端默认 16。 |
| `numEdgeCandidates` | `int` | `-1` | 构图候选节点数，范围 100..3200；未指定时服务端默认 100。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorHnswOptions;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.enums.SearchVectorSimilarity;

@CollectionName("example_mongovectorhnswoptions") @MongoVectorIndex(fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE,hnswOptions=@MongoVectorHnswOptions(maxEdges=32,numEdgeCandidates=200))}) public class MongoVectorHnswOptionsExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorHnswOptions.java#L6)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoVectorIndex

`import com.mongoplus.annotation.index.vector.MongoVectorIndex;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；@Repeatable(MongoVectorIndexes.class)。
- 实际用途：独立Vector Search索引，type=vectorSearch。
- 生效条件：VectorParse/Resolver及自动扫描；部署支持Vector Search。
- 限制与常见错误：至少一个vector字段；不同于Search中的vector映射；nestedRoot与storedSource支持看目标部署。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `"default"` | 索引名；空字符串由 Driver 使用 default，与显式 default 视为同名。 |
| `fields` | `MongoVectorField[]` | `{}` | 类级字段配置：fieldName 必填，name 必须留空；与实体字段注解合并。 |
| `nestedRoot` | `String` | `""` | 向量所在的文档数组路径，默认省略。所有 vector 路径必须位于它之下；filter 可在顶层。 使用实际 MongoDB 路径，或以 $ 开头的实体属性路径；不根据 Java 类型推断是否需要此配置。 |
| `storedSource` | `MongoVectorStoredSource` | `@MongoVectorStoredSource` | 源存储仅支持 include/exclude 对象形式；未指定时省略，默认不存储源字段。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.enums.SearchVectorSimilarity;

@CollectionName("example_mongovectorindex") @MongoVectorIndex(name="vec",fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)}) public class MongoVectorIndexExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorIndex.java#L13)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoVectorIndexes

`import com.mongoplus.annotation.index.vector.MongoVectorIndexes;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：多独立Vector Search索引容器。
- 生效条件：与MongoVectorIndex相同。
- 限制与常见错误：Search和Vector索引同名会冲突；每个子索引须有向量字段。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `MongoVectorIndex[]` | `必填（无默认值）` | 各索引独立配置，不跨索引继承字段或选项。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.annotation.index.vector.MongoVectorIndexes;
import com.mongoplus.enums.SearchVectorSimilarity;

@CollectionName("example_mongovectorindexes") @MongoVectorIndexes({@MongoVectorIndex(name="a",fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)})}) public class MongoVectorIndexesExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorIndexes.java#L6)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoVectorStoredSource

`import com.mongoplus.annotation.index.vector.MongoVectorStoredSource;`

- 作用域：`@Target({})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：独立Vector Search源字段存储配置。
- 生效条件：仅嵌入MongoVectorIndex.storedSource。
- 限制与常见错误：@Target({})；include与exclude互斥；不是filter字段定义。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `include` | `String[]` | `{}` | 仅存储这些字段（_id 默认保留）；支持实际 MongoDB 路径或 $实体属性路径。 |
| `exclude` | `String[]` | `{}` | 存储除这些字段之外的源字段；支持实际 MongoDB 路径或 $实体属性路径。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.annotation.index.vector.MongoVectorStoredSource;
import com.mongoplus.enums.SearchVectorSimilarity;

@CollectionName("example_mongovectorstoredsource") @MongoVectorIndex(fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)},storedSource=@MongoVectorStoredSource(include={"title"})) public class MongoVectorStoredSourceExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorStoredSource.java#L6)。后续实现与注册见[专题](../references/search-vector.md)。
