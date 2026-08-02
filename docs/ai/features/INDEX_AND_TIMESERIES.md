# 索引与时序集合

> 审计日期：2026-08-02。本文只描述当前源码可确认的 MongoPlus 行为；MongoDB Server 与 Java Driver 的通用能力不等于框架已经封装。启动背景见 [STARTUP_LIFECYCLE.md](../architecture/STARTUP_LIFECYCLE.md)，动态集合见 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md)，测试与兼容要求见 [TESTING.md](../TESTING.md) 和 [COMPATIBILITY.md](../COMPATIBILITY.md)。

## 公开入口与元数据

自动索引的字段入口是 `@MongoIndex`，类级复合入口是可重复的 `@MongoCompoundIndex`/容器 `@MongoCompoundIndexes`；当前源码**没有** `@MongoIndexes`。此外还有 `@MongoHashIndex`、`@MongoTextIndex`、`@MongoGeoIndex` 和用于指定自动索引数据源的 `@MongoIndexDs`。

`@MongoIndex` 公开 `name`、`unique`、`direction`、`sparse`、`expireAfterSeconds`、字符串 `expireAfter`、`partialFilterExpression` 和 `background`。复合索引公开 JSON `value`、`name`、`unique`、`sparse`、`partialFilterExpression`、`background`。`IndexUtil` 把 `$field` 转换为实体映射字段；无效 JSON 或无效过期单位在创建前抛转换/字段异常。

时序入口 `@TimeSeries` 公开 `dataSource`、必填 `timeField`、`metaField`、`granularity`、`bucketMaxSpan`、`bucketRounding` 和 `expireAfter`。集合名不在注解中，仍由 `@CollectionName`/类名映射取得。不存在运行时手工“创建时序集合”的 MongoPlus API；用户只能直接使用 Driver。

Boot 3/4 的 `MongoDBConfigurationProperty` 均有 `autoCreateIndex=false`、`autoCreateTimeSeries=false`、`autoScanPackages`。Solon 有同名属性，但扫描条件不同，见后文。

手工索引 API 由 `SuperIndex`、`Index`、`BaseIndex` 提供，最终被 `AbstractBaseMapper`、Mapper、Service/Repository 暴露；支持按实体或显式 database/collection 创建单个/多个索引、列出索引、按名称或 keys 删除以及删除全部。没有专用 Index Chain；CRUD/Aggregate Chain 不新增索引入口。

## 自动索引生命周期

Boot 3/4 的真实路径是：

```text
MongoPlusAutoConfiguration.init
 -> autoCreateTimeSeries
 -> autoCreateIndexes
 -> 配置开关判断
 -> MongoEntityScanner(getPackages()) 扫描实体
 -> IndexUtil.getIndex 解析注解
 -> @MongoIndexDs 或当前 DataSourceNameCache 选择数据源
 -> MongoPlusClient.getCollectionManager(ds, clazz).getCollection(clazz)
 -> MongoCollection.createIndexes(IndexModel list)
```

它发生在容器初始化回调内并同步执行，早于首个业务访问；创建异常没有 catch，会向上阻断初始化。Boot 3/4 的索引扫描条件是类级 `@CollectionName`，随后 `IndexUtil` 才解析该类的索引注解；只有字段 `@MongoIndex`、却没有 `@CollectionName` 的类不会被本轮自动扫描。扫描不依赖 Mapper 是否被 `@MongoMapperScan` 注册；Map/Document 无实体模式不参与。每个扫描实体执行一次本轮创建，没有静态“已初始化”标记；多应用上下文或重复初始化可重复调用。动态增加/覆盖数据源不会重触发扫描。

Solon 也在 `MongoPlusAutoConfiguration.init` 内先时序、后索引。其条件是“开关为真或 `autoScanPackages` 非空”，且仅遍历显式包，按 `@CollectionName` 选自动索引实体、按 `@TimeSeries` 选时序实体。因而配置了扫描包时，即使开关为 false 仍会执行；未配置包时循环为空。扫描异常包装为 `RuntimeException`。

### 已有索引处理

MongoPlus 自动索引**不调用 `listIndexes`，不比较名称、keys 或 options，也不删除、迁移或重建旧索引**；它把全部声明直接交给一次 Driver `createIndexes`。因此：

- `_id_` 没有框架特殊处理。
- 相同定义、同名不同定义、同 keys 不同名称、unique/TTL 变化的结果均由 MongoDB Server/Driver 决定。
- 若批量创建部分成功后失败，框架没有回滚、补偿或完成标记。
- 自动创建异常向上传播；手工 API 也不吞异常。

这些冲突的准确错误码和不同 Server 版本的批处理原子性需要真实环境验证，不能从框架源码推断。

## 索引能力边界

| 能力 | 注解自动创建 | 手工 MongoPlus API |
|---|---|---|
| 单字段、复合、升/降序、名称 | 已确认 | 通过公开 `Bson`/`IndexModel` |
| unique、sparse、background | 已确认 | 通过公开 Driver options |
| TTL/expireAfter | 单字段已确认 | 通过 `IndexOptions` |
| partialFilterExpression | 单字段和复合已确认 | 通过 `IndexOptions` |
| text、hashed、geo、wildcard `$**` | 分别有专用注解/工具支持；wildcard 可由 text 字段值表达 | `Bson` 可表达 |
| collation、hidden、storageEngine | 注解未发现 | 可由传入的 Driver `IndexOptions` 表达，但 MongoPlus 无专用抽象 |
| commitQuorum | 注解未发现 | `createIndexes(..., CreateIndexOptions)` 透传 Driver 对象 |

表中“透传 Driver 对象”只表示公开方法接受该对象，不表示 MongoPlus 对每个选项做兼容校验。

## 手工索引执行链

```text
Mapper/Service/Repository 或 BaseIndex
 -> DefaultBaseIndexImpl（实体入口解析 namespace，并发布 NamespaceAware）
 -> AbstractBaseIndex
 -> MongoPlusClient.getCollection
 -> ExecutorFactory.getExecute
 -> 普通 ExecutorProxy -> 高级 AdvancedProxy 链
 -> DefaultExecute / SessionExecute
 -> MongoCollection create/list/drop index API
```

索引方法不在 `ExecuteMethodEnum` 中，`ExecutorProxy` 查不到参数策略，因此普通拦截器的 `beforeExecute` 和参数改写方法不运行；Tenant、Dynamic Collection 和普通 Logic Delete 不会改写索引目标或参数。目标方法返回后，普通拦截器的 `afterExecute` **仍会逐个执行**，但收到的 `ExecuteMethodEnum` 为 `null`；高级拦截器也仍包裹目标调用，是否短路或改写取决于各实现。动态集合名不会影响目标；实体入口只经过 `NamespaceAware`。事务上下文存在时 `SessionExecute` 使用 Driver 的带 `ClientSession` 索引重载，框架不预检“事务内 DDL”服务端限制，也不校验 session 与 collection 的 client 一致性。

## 时序集合创建生命周期

Boot 3/4 与 Solon 最终共用 `AutoUtil.autoCreateTimeSeries`：

```text
扫描 @TimeSeries 实体
 -> 注解 dataSource 或当前 DataSourceNameCache
 -> MongoClient.getDatabase(实体 database)
 -> runCommand listCollections，filter type=timeseries
 -> 仅按集合名判断是否已存在时序集合
 -> 解析 time/meta 字段与 granularity/bucket 配置
 -> CreateCollectionOptions.timeSeriesOptions
 -> 可选 collection expireAfter
 -> MongoDatabase.createCollection
```

真实顺序中时序创建先于普通自动索引；创建后没有主动取得/写入 `CollectionManager` cache，后续索引或 CRUD 才会取得 collection。`expireAfter` 是 `CreateCollectionOptions` 的集合过期配置，不是普通 TTL index。

若服务端已列出同名时序集合，框架只记录 warn 并跳过，不读取或比较 granularity、time/meta、bucket 或 expireAfter。若同名是普通 collection，它不在过滤结果中，框架仍调用 `createCollection`；`MongoCommandException` 被无条件吞掉，所以普通集合冲突、版本不支持、权限或其他命令错误均不会阻断启动。字段解析、获取 client/database、`runCommand` 等非 `MongoCommandException` 仍可阻断初始化。框架不迁移、不删除、不重建集合，也不验证服务端现有 collection 类型/options。

`bucketMaxSpan > 0` 或 `bucketRounding > 0` 时源码调用 `options.metaField(null)`；两者可同时设置，框架未校验 Server 对二者配对和版本要求。

## 动态集合、多数据源与组合边界

- 动态集合 Handler 只在支持参数策略的 Execute 方法中运行；自动/手工索引以及启动时序创建不会应用它。运行时出现的新动态 namespace 不会自动复制索引或时序属性。
- 自动索引按 `@MongoIndexDs` 或启动线程当前数据源创建；时序按 `@TimeSeries.dataSource` 或当前数据源创建。它们不会遍历每个数据源。
- `CollectionManager` 按 datasource/database manager 隔离 collection cache，但实体 registry key 不含 datasource；组合风险见 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md) 和 [MULTI_DATASOURCE.md](MULTI_DATASOURCE.md)。
- 时序 collection 后续 CRUD、聚合、Tenant、Logic、Auto Fill 走普通执行链；MongoPlus 未针对服务端时序限制做额外校验。Transaction、Update/Delete、sharding 等是否被 Server 接受由部署版本决定。
- 无实体 Map/Document 模式可以手工索引/普通 CRUD，但不参与注解扫描，也无法声明 `@TimeSeries`。

## Boot 3、Boot 4、Solon

Boot 3/4 属性与核心实现平行且行为相同：开关为 true 才扫描，扫描包由 starter 的 `getPackages()` 合成；Boot 4 不是复用 Boot 3 源码，但共用 core `AutoUtil`。Solon 共用 `AutoUtil`，却以显式 `autoScanPackages` 扫描，并存在“非空包绕过 false 开关”的条件差异。三者初始化均为同步调用；时序 `MongoCommandException` 都被 core 吞掉，索引异常均向上传播。

## 最低测试清单

- 单字段/复合/unique/TTL/partial/text/hash/geo；无效 JSON 和过期单位。
- 相同索引重复创建、同名不同 keys/options、同 keys 不同名、`_id_`、批量部分失败。
- 多数据源、动态添加/覆盖、动态集合、并发/重复容器初始化。
- 时序首次创建、已有普通/时序集合、options 不同、bucket 组合、expireAfter、权限/版本错误。
- 事务中手工 create/list/drop；session/client 不一致。
- Boot 3/4/Solon 的开关、包扫描和失败外观；MongoDB Server 版本矩阵。

## 关键源码

- annotation: `annotation/index/{MongoIndex,MongoCompoundIndex,MongoCompoundIndexes,MongoIndexDs,MongoHashIndex,MongoTextIndex,MongoGeoIndex}.java`、`annotation/collection/TimeSeries.java`
- core: `toolkit/{IndexUtil,AutoUtil}.java`、`index/{SuperIndex,Index,BaseIndex}.java`、`index/impl/{AbstractBaseIndex,DefaultBaseIndexImpl}.java`
- core: `execute/instance/{DefaultExecute,SessionExecute}.java`、`conn/CollectionManager.java`
- Boot 3/4/Solon: 各自 `config/MongoPlusAutoConfiguration.java` 与 `property/MongoDBConfigurationProperty.java`
