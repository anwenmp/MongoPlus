# 注解：indexes

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：MongoCompoundIndex、MongoCompoundIndexes、MongoGeoIndex、MongoHashIndex、MongoIndex、MongoIndexDs、MongoTextIndex。

## MongoCompoundIndex

`import com.mongoplus.annotation.index.MongoCompoundIndex;`

- 作用域：`@Target({ElementType.TYPE})`；保留策略：RUNTIME；@Repeatable(MongoCompoundIndexes.class)。
- 实际用途：以JSON键定义复合索引及选项，可重复。
- 生效条件：实体扫描与自动索引，或Resolver生成模型后创建。
- 限制与常见错误：value必填；JSON顺序影响键顺序；不要推断lambda字段映射规则。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `String` | `必填（无默认值）` | 复合索引值，这里需要传入json格式，例： `@CompoundIndex("{'field1':1,'field2':-1}")` 同时也可使用`$`标识，该操作将会根据json值的key查找类中的该字段，不存在则直接使用json的key，例: `@CompoundIndex("{'$field1':1,'$field2':-1}")` |
| `name` | `String` | `""` | 索引名称 |
| `unique` | `boolean` | `false` | 唯一复合索引 |
| `sparse` | `boolean` | `false` | 稀疏复合索引 |
| `partialFilterExpression` | `String` | `""` | 部分索引，和`MongoIndex#partialFilterExpression()`不同，该属性需手动指定字段 例：{"field1" : {"$gt",5}} 同时也可使用`$`标识，该操作将会根据json值的key查找类中的该字段，不存在则直接使用json的key，例: 例：{"$field1" : {"$gt",5}} |
| `background` | `boolean` | `false` | 是否应该在后台创建索引 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoCompoundIndex;

@CollectionName("example_mongocompoundindex") @MongoCompoundIndex(value="{\"tenant\":1,\"created_at\":-1}", name="tenant_time") public class MongoCompoundIndexExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java#L9)。后续实现与注册见[专题](../references/indexes.md)。

## MongoCompoundIndexes

`import com.mongoplus.annotation.index.MongoCompoundIndexes;`

- 作用域：`@Target({ElementType.TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：复合索引可重复容器。
- 生效条件：与MongoCompoundIndex相同。
- 限制与常见错误：容器不是新索引类型；每个子项仍需合法键定义。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `MongoCompoundIndex[]` | `必填（无默认值）` | 标记属性 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoCompoundIndex;
import com.mongoplus.annotation.index.MongoCompoundIndexes;

@CollectionName("example_mongocompoundindexes") @MongoCompoundIndexes({@MongoCompoundIndex("{\"a\":1}"),@MongoCompoundIndex("{\"b\":1}")}) public class MongoCompoundIndexesExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndexes.java#L10)。后续实现与注册见[专题](../references/indexes.md)。

## MongoGeoIndex

`import com.mongoplus.annotation.index.MongoGeoIndex;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：地理字段索引。
- 生效条件：扫描与GeoParser；type必须显式选择。
- 限制与常见错误：geo查询要求正确坐标类型；二维与球面索引不能互换。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `type` | `GeoType` | `必填（无默认值）` | 索引类型 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoGeoIndex;
import com.mongoplus.enums.GeoType;

@CollectionName("example_mongogeoindex") public class MongoGeoIndexExample { @MongoGeoIndex(type=GeoType.SPHERE) Object location; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoGeoIndex.java#L11)。后续实现与注册见[专题](../references/indexes.md)。

## MongoHashIndex

`import com.mongoplus.annotation.index.MongoHashIndex;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：字段哈希索引。
- 生效条件：扫描与HashParser。
- 限制与常见错误：不是加密字段；不把hash索引当作普通范围排序索引。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoHashIndex;

@CollectionName("example_mongohashindex") public class MongoHashIndexExample { @MongoHashIndex String key; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoHashIndex.java#L9)。后续实现与注册见[专题](../references/indexes.md)。

## MongoIndex

`import com.mongoplus.annotation.index.MongoIndex;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：字段普通索引，支持唯一、稀疏、TTL、partial及方向。
- 生效条件：auto-create-index与实体扫描，或手工Index API。
- 限制与常见错误：expireAfterSeconds=-1表示未设置；expireAfter表达式需核实解析；TTL要核对实际日期类型及异步过期。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `""` | 索引名称 |
| `unique` | `boolean` | `false` | 唯一索引 |
| `direction` | `IndexDirection` | `IndexDirection.ASC` | 索引排序方向 |
| `sparse` | `boolean` | `false` | 稀疏索引 |
| `expireAfterSeconds` | `long` | `-1` | 索引过期时间，以秒为单位 |
| `expireAfter` | `String` | `""` | 自定义类型的索引过期时间 d : 天 h : 时 m : 分 s : 秒 `@MongoIndex(expireAfter = "1d")` `@MongoIndex(expireAfter = "3h")` `@MongoIndex(expireAfter = "30m")` `@MongoIndex(expireAfter = "30s")` |
| `partialFilterExpression` | `String` | `""` | 部分索引 例：{"$gt",5} |
| `background` | `boolean` | `false` | 是否应该在后台创建索引 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoIndex;

@CollectionName("example_mongoindex") public class MongoIndexExample { @MongoIndex(name="email_unique", unique=true) String email; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java#L11)。后续实现与注册见[专题](../references/indexes.md)。

## MongoIndexDs

`import com.mongoplus.annotation.index.MongoIndexDs;`

- 作用域：`@Target({ElementType.TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：指定自动索引目标数据源。
- 生效条件：实体扫描且目标数据源已注册。
- 限制与常见错误：仅索引DDL目标；不替代MongoDs请求路由。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `dataSource` | `String` | `""` | 数据源名称 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoIndexDs;

@CollectionName("example_mongoindexds") @MongoIndexDs(dataSource="archive") public class MongoIndexDsExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndexDs.java#L9)。后续实现与注册见[专题](../references/indexes.md)。

## MongoTextIndex

`import com.mongoplus.annotation.index.MongoTextIndex;`

- 作用域：`@Target({ElementType.TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：类级传统text索引。
- 生效条件：扫描与TextParser；fields必填。
- 限制与常见错误：不是Search；language和textIndexVersion受服务端限制；字段名映射需对照模型。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `fields` | `String[]` | `必填（无默认值）` | 索引字段，传入多个值即为复合文本索引 可使用`$`标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如： `@MongoTextIndex({"field1","$field2"})` 通配符文本索引直接传入$**即可 |
| `name` | `String` | `""` | 索引名称 |
| `language` | `TextLanguages` | `TextLanguages.ENGLISH` | 文本语言，默认英语 |
| `textIndexVersion` | `int` | `-1` | 尽可能始终使用默认索引版本。 仅在出于兼容性原因需要时才覆盖默认版本 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoTextIndex;

@CollectionName("example_mongotextindex") @MongoTextIndex(fields={"title","body"}, name="article_text") public class MongoTextIndexExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoTextIndex.java#L11)。后续实现与注册见[专题](../references/indexes.md)。
