# 索引与时序集合

> 审计日期：2026-08-02。本文只描述当前源码可确认的 MongoPlus 行为；MongoDB Server 与 Java Driver 的通用能力不等于框架已经封装。启动背景见 [STARTUP_LIFECYCLE.md](../architecture/STARTUP_LIFECYCLE.md)，动态集合见 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md)，测试与兼容要求见 [TESTING.md](../TESTING.md) 和 [COMPATIBILITY.md](../COMPATIBILITY.md)。

## 公开入口与元数据

自动索引的字段入口是 `@MongoIndex`，类级复合入口是可重复的 `@MongoCompoundIndex`/容器 `@MongoCompoundIndexes`；当前源码**没有** `@MongoIndexes`。此外还有 `@MongoHashIndex`、`@MongoTextIndex`、`@MongoGeoIndex` 和用于指定自动索引数据源的 `@MongoIndexDs`。

`@MongoIndex` 公开 `name`、`unique`、`direction`、`sparse`、`expireAfterSeconds`、字符串 `expireAfter`、`partialFilterExpression` 和 `background`。复合索引公开 JSON `value`、`name`、`unique`、`sparse`、`partialFilterExpression`、`background`。`IndexUtil` 把 `$field` 转换为实体映射字段；无效 JSON 或无效过期单位在创建前抛转换/字段异常。

### MongoDB Search 注解与解析

`@MongoSearchIndex` 的参数对照 [MongoDB Search 索引定义](https://www.mongodb.com/docs/search/indexes/index-definitions/) 补全（2026-10-20 核对）：

- `name` 是创建索引的名称参数，不属于 `definition`。
- `analyzer`、`searchAnalyzer` 使用 `Analyzer` 枚举；`DEFAULT` 表示省略选项，不能写成 `lucene.default`。`customAnalyzer`、`customSearchAnalyzer` 引用 `analyzers` 中定义的自定义分析器名称，使用时对应枚举保持 `DEFAULT`。
- `dynamic=false` 与 `fields` 对应 `mappings.dynamic`、`mappings.fields`。`dynamicTypeSet` 表达 `mappings.dynamic` 的对象形式，与布尔形式互斥；类型集在 `typeSets` 中定义。未启用任何动态映射时必须指定静态字段。
- `fields` 为 `MongoSearchField[]`；`synonyms` 为 `MongoSearchSynonym[]`；`analyzers`、`typeSets` 为 JSON 对象字符串数组。JSON 中使用实际 MongoDB 字段名，未声明普通索引的 `$field` 转换能力。
- `sort` 为 `MongoSearchSort[]`，也可在实体字段上使用 `MongoSearchSort`（重复容器 `MongoSearchSorts`），
  两处均未配置时省略索引定义的顶层 `sort`。类级数组按声明顺序，字段配置按 `priority` 升序、
  同优先级按实际字段路径字典序追加；同一索引的重复排序路径应报错。
  `order` 复用 `IndexDirection`，`noData` 使用 `SearchSortNoData`，DEFAULT 表示省略，另外可选 LOWEST/HIGHEST；
  输出为 `1/-1` 或 `{order, noData}`，解析器执行排序合并和路径校验。
  排序字段必须静态映射为 boolean/date/number/objectId/token/uuid，嵌套路径的父字段必须具有静态 document 映射，
  不能仅依赖 dynamic/typeSet，也不能使用 searchScore。依据 [Sorted Index](https://www.mongodb.com/docs/search/indexes/sort/)。
- `numPartitions` 保持 int 类型，默认 `1`，允许 `1/2/4`，额外分区需要独立 Search Nodes。
  每分区容量按索引对象计数，embeddedDocuments 也会增加计数；配置或修改分区会重建索引，
  多分区部署不能移除所有 Search Nodes。它不是数据库分片数量，也不自动设置查询级 concurrent。
- `MongoSearchSynonym` 用必填的 name、sourceCollection 表达同义词名称与 source.collection，
  analyzer 为 DEFAULT 且无 customAnalyzer 时必须显式输出 lucene.standard；自定义分析器在索引级 analyzers 中定义。
  来源集合需与被索引集合位于同一数据库；查询目标字段必须使用相同分析器，text/phrase 查询需显式引用映射。
  M0 集群每索引最多一个映射，来源集合最多一万条文档。本地解析只检查映射定义，不执行来源集合管理、集群限额检查或查询。
- 索引级 `storedSource` 使用嵌套注解 `MongoSearchStoredSource`，默认不存储；`value=true` 表达全量存储，
  非空 `include` / `exclude` 表达对应对象形式，两者互斥，且不能与 `value=true` 同用。
  路径为实际 MongoDB 字段名或点路径，include 默认还存储 `_id`；该选项要求 MongoDB 7.0+。
  含 vector 类型的索引不能全量存储，include 不能包含向量字段，exclude 必须排除向量字段。
  解析器执行定义转换、互斥和静态向量路径检查，不执行源字段查询；嵌套字段的 `MongoSearchField.storedSource` 仍为 JSON 字符串。

`MongoSearchIndex`、`MongoSearchField` 位于 `com.mongoplus.annotation.index.search`。
`MongoSearchField` 可用于实体字段或索引的 `fields` 数组，通过可重复容器 `MongoSearchFields` 表达同字段多类型。
`type` 默认 `SearchFieldType.STRING`，不是 Java 类型自动推断；`exclude=true` 表达空数组排除映射。
`document` / `embeddedDocuments` 的嵌套 `fields` 和字符串 `multi` 使用 JSON 对象，
避免 Java 注解不允许的循环成员类型；动态类型集引用使用 `dynamicTypeSet`。
类型专属参数覆盖文本、自动补全、token、数值、geo、嵌套源字段存储和向量映射。
文本 `similarity` 输出 `similarity.type` 对象，`vectorSimilarity` 输出向量 `similarity` 字符串。
解析器按 `type` 选择选项，并省略空字符串、`-1`、`Analyzer.DEFAULT` 等标记，必填向量参数未配置时拒绝；
不能直接把注解的所有成员写入每一种字段定义。示例与完整边界见 [字段配置说明](../../MONGO_SEARCH_FIELD.md)。

2026-10-21：`SearchParse` 已实现注解到 `SearchIndexModel` 的转换。
`Resolver.resolve` 按 `activate()` 分发，并把搜索模型写入 `IndexMetaObject.searchIndexModels`。
类级 `MongoSearchIndex` 可重复声明，容器为 `MongoSearchIndexes`，激活和解析均通过
`getAnnotationsByType` 读取；不新增类级注解继承语义。字段的重复注解同样通过该方法收集，包括父类字段。
`MongoSearchField` 与 `MongoSearchSort` 共用归属规则：类上只有一个索引时 name 留空自动归属它；
多个索引时 name 必填。存在类级声明时，显式名称必须匹配已声明索引，未知名称直接报错。
没有类级声明时，保留字段注解按 name 分组、各组使用默认配置的能力。
索引名默认为 default；显式空名仍省略 Driver 名称，但查找和重名校验视为 default，字段可用 default 引用。
同一类的重复索引名（包括空名与 default 并存）报错。嵌套 fields/sort 的 name 仍须留空，归属外层索引；
storedSource、synonyms、analyzers、typeSets 等配置和引用校验按索引隔离。
字段名沿用 `@CollectionField`、驼峰及 ID 映射；显式 JSON 不执行 `$field` 转换。
结构、引用、互斥和范围错误抛 `MongoPlusConvertException`；所有分组成功后才追加模型。
包内 `SearchFieldParser` 负责按类型转换，`SearchDefinitionValidator` 负责本地定义校验。
使用顺序 Stream 保留声明顺序；通过项目 Log 门面记录归属和完成数量的 DEBUG 日志，失败记录实体和异常。
本次 Maven 3.8.6 / JDK 17 定向执行 `SearchParseTest,SearchRepeatableIndexTest`：24 项通过，0 失败/错误/跳过。

本次验证是本地 BSON/解析器回归，不包含 Search DDL 或数据库行为。
`AutoUtil` 仍使用旧的 `IndexUtil.getIndex` 和 `MongoCollection.createIndexes` 创建普通索引，
没有接通搜索索引自动创建；实际搜索索引创建、来源集合、部署版本及 Search Nodes 为 `NOT_RUN`。

### 其他入口

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
