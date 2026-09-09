# 聚合执行链

> 审计日期：2026-09-01。结论基于 `mongo-plus-core` 当前源码；MongoDB Driver 本身具备但 MongoPlus 未公开封装的能力不计为框架能力。普通查询条件见 [QUERY_WRAPPER.md](QUERY_WRAPPER.md)，执行代理见 [CRUD_EXECUTION.md](CRUD_EXECUTION.md) 与 [EXTENSION_PIPELINE.md](EXTENSION_PIPELINE.md)，结果转换见 [ENTITY_MAPPING.md](ENTITY_MAPPING.md)。

## 公开 API 与对象关系

- `Aggregate<Children>` 是 stage 契约，`AggregateOptions<Children>` 是执行选项契约；`LambdaAggregateWrapper<Children>` 保存可变 pipeline 和 options，`AggregateWrapper` 只是其具体便捷类型。
- 当前没有名为 `AggregateChainWrapper` 的类型。链式执行类型是 `LambdaAggregateChainWrapper<T>`，继承 `LambdaAggregateWrapper` 并实现 `ChainAggregate<T>`，公开 `list/one` 及指定 `Class<R>` 结果类型的重载。
- `Wrappers.lambdaAggregate()` 创建构建器，`ChainWrappers.lambdaAggregateChain(BaseMapper, Class)` 创建可执行链。
- `BaseMapper` 公开 `aggregateList/aggregateOne`，同时覆盖 `Class<R>` 和 `TypeReference<R>` 结果类型；实体 Mapper 的默认重载补入实体类对应的 database/collection。`IRepository`/`RepositoryImpl` 转发聚合 list/one；没有独立聚合分页 API。
- Map/DTO 可通过 `Class` 或 `TypeReference` 作为结果类型；`Document.class` 原样读取。显式 database/collection 的 `BaseMapper` 可用于无实体入口，但 collection registry 会登记 `UnClassCollection`，相关增强可能无法取得实体元数据。
- Wrapper 是公开可变对象；内部的 `CopyOnWriteArrayList<Bson>` 和 `BasicDBObject` 直接由 getter 暴露，属于执行构建状态，不应当作不可变值对象。

## Pipeline 构建与执行

```text
AggregateWrapper / LambdaAggregateChainWrapper 的 stage 调用
 -> LambdaAggregateWrapper.custom(Bson)：立即按调用顺序 append 到同一 List
 -> BaseMapper.aggregateList/aggregateOne 直接取得该 List
 -> ExecutorProxy 普通参数策略（Tenant -> Dynamic Collection -> Logic，按当前已排序内置链）
 -> AdvancedInterceptorChain
 -> DefaultExecute.executeAggregate
    或 SessionExecute.executeAggregate(clientSession, ...)
 -> MongoCollection.aggregate(pipeline, Document.class)
 -> AggregateUtil 把 Wrapper options 应用到 AggregateIterable
 -> out/merge: toCollection() 消费并返回空结果
    其他: MongoConverter.read/readDocument 立即消费并映射
```

Stage 在每次 Wrapper 调用时即构造成 BSON，不是执行时统一翻译；列表保持调用顺序。`custom(Bson)` 是直接传 BSON stage 的统一入口，若传 null，列表会接受 null，后续 BSON 转换、拦截或 Driver 调用可能失败，框架无校验。空 pipeline 会原样传给 Driver。

执行不会主动清空 Wrapper。普通策略把每轮返回值写回 `args[0]` 再交给下一插件：已有 match 时 Tenant/Logic 通过 stream 创建新 List，并把 match stage 编码成新的 `BsonDocument`；无 match 时才对当前 List 原地 `add`。两个插件处理同一参数槽中逐轮传递的引用，但它不保证始终是 Wrapper 暴露的原 List：

- 没有增强或只有“已有 match”分支时，重复执行通常保留 Wrapper 原 pipeline；
- 单独看 Tenant，无 match 会 `add(0, $match)` 并污染 Wrapper 原 List；单独看 Logic Delete，无 match 会 `add($match)` 并污染 Wrapper 原 List。两者同时启用且 Tenant 生效时，Tenant 先插入 match，Logic 随后进入“已有 match”分支并返回新 List，所以 Wrapper 原 List 只持久留下 Tenant match，本次 Driver 参数则同时含 Tenant 与 Logic 条件；重复执行会因这次污染而改变分支；
- `out/merge` 会永久设置 `isSkip=true`；后续再追加普通 stage 仍会按跳过结果处理。

## Stage 支持矩阵

聚合专用机器索引由 `mongo-plus-indexer` 的 `MongoPlusPipelineIndexerMain` 生成，入口为
Stage 根 `Aggregate` 加五个明确的 Expression 工厂根，详见
[Indexer 契约与生成方式](../../../mongo-plus-indexer/README.md)。索引映射仅接受
源码 `@mongoStage` / `@mongoExpression` 块标签；下述人工源码审计的支持矩阵不能作为自动映射来源。
2026-09-06 已按当前实现补充这两类标签；收录统计与边界见下方 Pipeline Javadoc evidence。

以下均为当前 `Aggregate`/`LambdaAggregateWrapper` 已确认公开支持：`match`、`project`、`sort`、`skip`、`limit`、`group`、`unwind`、`lookup`、`addFields`、`set`、`unset`、`replaceRoot`、`replaceWith`、`count`、`facet`、`unionWith`、`bucket`、`bucketAuto`、`graphLookup`、`sample`、`out`、`merge`。此外还封装了 `sortByCount`、`setWindowFields`、`densify`、`fill` 等。

多数 stage 同时提供字段/Lambda/Driver option/BSON 重载；BSON 重载并不总是补 stage 名，调用方必须按该方法实现传入完整 stage。`custom(Bson)` 可承载其他原生 stage，但这只表示透传入口，不表示 MongoPlus 为该 stage 提供语义、校验或兼容保证。

## Pipeline Javadoc evidence

2026-09-06 的 evidence 补充仅修改 Core Javadoc，不修改方法签名、业务逻辑。标签逐方法声明，
以 `LambdaAggregateWrapper` 的委托链、实际 BSON、Driver 5.4.0 源码及参数语义为依据；
未使用类级标签将映射批量传播给所有方法。

- `Aggregate` 和父接口 `pipeline.Project` 共 140 条 `@mongoStage`，覆盖 26 种 Stage；
  Index 按 category + Java 方法名分为 33 个 `PIPELINE_STAGE` family。
- `pipeline.Accumulators`、`AggregateOperator`、`Projections`、`Sorts`，以及现行和 deprecated
  `ConditionOperators` 共 211 条 `@mongoExpression`，覆盖 42 种 Expression（49 个 Java 方法名）。
- 2026-09-07 将 `Accumulators`、`AggregateOperator`、`Projections`、`Sorts` 和现行
  `conditions.operation.ConditionOperators` 配置为明确的 Expression 根，继续执行现有类型、
  public 构造器与 specialType 依赖闭包。正式 Index 为 33 个 Stage family（140 overload）和
  49 个 Expression family（157 overload、42 种映射），合计 82 family、297 overload。
  旧 `conditions.interfaces.ConditionOperators` 也是 class，与现行类无接口/实现关系；
  它已 deprecated 且 Javadoc 指向现行类，故不选为根，排除其 54 条重复副本 evidence。
  不扫描全项目或反向枚举返回同一 Driver 类型的工厂，内部实现也不因存在标签而成为根。
- Stage `Aggregate.count` 对应 `$count`；`Accumulators.count` 对应 `$count` accumulator，
  `Accumulators.sum()` 实际构造 `{count: {$sum: 1}}`，仍标记 `$sum`。
- `projectDisplay/projectNone` 对应 `$project`，`sortAsc/sortDesc` 及各自 Lambda 集合便利方法
  对应 `$sort`，`unsetLambda` 对应 `$unset`。`condArray` 对应 `$cond`，`multiplyLambda`
  对应 `$multiply`，带 Option 的 mergeObjects 字段便利方法对应 `$mergeObjects`。

保守排除：

- 仅 `custom(bson)` 的完整 BSON 透传 overload 不加 Stage 标签；`addFields/set/bucket/bucketAuto/
  match/project(Bson)` 会包裹固定 Stage，可加标签。
- `Aggregate.metaTextScore` 追加裸排序规范而未包裹 `$sort`，不标记；`Sorts` 的升降序及合并器、
  `Projections` 的字段组合/包含/排除、`Field/Facet/Variable/UnwindOption` 等构建数据也不作为 Stage。
  `Projections.meta*`、`Sorts.metaTextScore` 的 BSON 包含明确 `$meta` expression，标记为 Expression。
- `Projections.elemMatch/slice` 是查询投影形态，`computed` 接受任意表达式，`computedSearchMeta`
  使用 `$$SEARCH_META` 变量，均不能映射为同名聚合 Expression 或 `$project` Stage。
- `FillField.value/locf/linear/of` 构造 fill 输出配置，不构造 `$fill` Stage 或 `$locf/$linearFill` Expression。
  `AggregateOptions.let` 是命令选项；读取状态、执行 list/one 等方法也没有固定映射。
- `addFields/set` 的 Collection 值重载生成可疑的 concatArrays 操作数结构；`SFunction,Object`
  重载经 `Field.of` 为输出字段名添加 `$`。本轮不确认这些重载的完整语义，也不修复其实现。
- `Accumulators.firstN(String,NExpression,SFunction...)` 委托时交换 n/input；
  `topN(SFunction,Bson,NExpression,SFunction)` 未转换输出 Lambda；均不标记。
- 两份 `ConditionOperators` 中，不带 Option 的 mergeObjects 字段 Lambda 路径、丢弃 timezone
  的 dateFromString 两参数重载，以及描述为 each 更新修饰符而实际使用 MERGE_OBJECTS 常量的
  each 系列，均不标记。这些是本轮排除依据，不表示已做服务器行为验证或业务修复。

验证：Query/Pipeline 可执行自测覆盖移除真实入口标签后空映射、透传同名 overload 排除、
五个真实 Expression 根独立生成、旧包排除、重复根/依赖可达去重、依赖闭包及确定性。
新增根前后的全部 Stage family/evidence 必须相等；实际运行结果见当次任务报告。

## Match、Tenant 与 Logic Delete

`match(Wrapper<?>)` 调用 `buildCondition().getCondition()` 后交给 Driver `Aggregates.match`，因此 eq/in/regex/AND/OR/NOT/EXPR 与普通查询共享 Wrapper 条件构建及其已知边界。多个用户 match 保持为多个 stage；函数式重载声明为 `SFunction<QueryWrapper<?>, QueryWrapper<?>>`，内部以 `QueryWrapper<?>` 作为初始 Wrapper。

聚合增强发生在 pipeline 已构建之后、Driver 调用之前的 `ExecutorProxy` 普通参数策略中：

- Tenant（order 0）先运行。若 pipeline 任意位置存在 `$match`，它给**每一个** `$match` 的 document `putIfAbsent(tenantColumn, {$eq: tenantId})`；用户已写同名顶层字段时保留用户值。若没有 match，则在索引 0 插入 tenant match。
- Dynamic Collection（order 2）随后替换 Execute 参数末项的主 collection；它不改 lookup/unionWith 中的 foreign collection。
- Collection Logic（默认最大 order）后运行，但 `ExecutorProxy` 传给普通专用策略的仍是进入代理时捕获的原 collection。若 pipeline 有 match，它给**每一个** match `putIfAbsent(logicColumn, {$eq: notDeleted})`；没有 match 时追加到 pipeline 尾部，而不是插到开头。

这产生已确认结构风险：无用户 match 时，单独启用 Tenant 会把 match 放在用户首 stage 之前；若原首 stage 是 `$geoNear`、`$search`、`$vectorSearch` 等要求首位的 stage，框架最终发出的顺序可由源码确认，Driver/Server 的准确异常仍需集成测试。单独启用 Logic Delete 会把 match 放在尾部；若原末 stage 是 `$out`/`$merge`，框架会在其后追加 stage，准确失败同样由运行测试固定。对 project/group，尾部过滤还可能因字段已改变而产生错误语义。已有多个 match 时两个条件会注入每一个 match。Boot 3/4 方法级 Ignore 可影响 aggregate；Solon 的 `@IgnoreLogic` 绑定仍待启动测试，Ignore 注解都要求调用经过容器代理。动态 namespace 常登记为 `UnClassCollection`，但普通 Logic 本次仍观察原 collection；详见 [TENANT.md](../features/TENANT.md)、[LOGIC_DELETE.md](../features/LOGIC_DELETE.md) 与 [DYNAMIC_COLLECTION.md](../features/DYNAMIC_COLLECTION.md)。

## Pipeline 表达式参数语义

2026-09-07 的参数 evidence 仅增加逐方法 Javadoc `@mongoParam`，不修改业务实现或原有
`@mongoStage`/`@mongoExpression`。Index 中显式标记的参数为 `semanticType=PIPELINE_EXPRESSION`，
`semanticScope=VALUE|ELEMENT`，并关联 `PIPELINE_EXPRESSION_FIELD_REFERENCE` concept；
类型名、参数名、方法名或描述不能自动触发分类。完整覆盖列表和 JSON 契约见
[Indexer 参数 evidence](../../../mongo-plus-indexer/README.md#pipeline-表达式参数-evidence)。

- `Accumulators` 的已核对 expression 参数经过 `accumulatorOperator` 保存到 `SimpleExpression`；
  `SimpleExpression.encodeValue` 对 Bson 转 document，对其他非 null 值使用运行时 codec。
- `LambdaAggregateWrapper.group` 的泛型 id 原样交给 Driver `Aggregates.group`；Driver 5.4.0
  的 GroupStage 写 `_id` 时调用 BuildersHelper.encodeValue。默认 StringCodec 调用 writeString(value)。
- `ConditionOperators` 的已核对泛型值直接进入 Document 或操作数数组。Lambda 便利方法使用
  `SFunction.getFieldNameLineOption()`，其实现明确返回 `$` + 实际字段名，证明字符串字段引用表示。
- `Projections.computed` 原样保存值，`computedSearchMeta` 明确传 `$$SEARCH_META`；
  `AggregateOptions.let` 的源码契约还明确声明 `$$` 变量访问语法。变量是否已绑定不由 Index 保证。
- `"amount"` 与 `"$amount"` 都原样编码；前者不自动加字段前缀，后者可作为已标记参数的字段引用。
  当前未确认将 `$` 开头字符串强制解释为 literal 的专用 API，concept 记录 `NOT_ESTABLISHED`，
  不增加 `$literal` 映射。此证据不宣称已执行服务端表达式求值。

第一轮正式 methodFamilies 中增加 41 个参数标记，另有 Projections.computed 的 2 个参数仅进入既有
type evidence。随后逐参数核查全部 49 个 Expression family、157 overload、372 个参数，
在 74 个 overload 补充 127 个标签（100 VALUE、27 ELEMENT）。新增范围包含直接写入 BSON 的
Object/泛型值、日期 expression 槽、N 类累加器的 n/input/output，以及真实操作数数组/集合。
输出字段键、sortBy 排序规格、meta 选择符、函数源码/lang、count 占位和 getter 参数不赋予该语义；
动态 cond 的 ifValue 由调用方选定操作符解释，也不保证每个元素是 expression。
详见 [完整逐参数审计](../../../mongo-plus-indexer/EXPRESSION_PARAMETER_AUDIT.md)。

2026-09-09 专项核查已收录 Accumulators 的 21 个 family、85 个 overload，为 84 个 BsonField
输出名称槽（29 String、55 Lambda）补充 `@mongoParam fieldName OUTPUT_FIELD_NAME VALUE`。
Lambda 输出名经 `getFieldNameLine()`；复用既有 `PIPELINE_PARAMETER_OUTPUT_FIELD_NAME`，
无参 sum() 无参数可标记，其他 expression 参数证据不变。
详见 [输出字段名专项审计](../../../mongo-plus-indexer/ACCUMULATOR_OUTPUT_FIELD_AUDIT.md)。

ELEMENT 表示数组/varargs/集合的每个直接元素；VALUE 表示参数本身。标签不改变 Java 类型限制：
Number 仅承载数值常量，String 仅承载字符串表示，List<String> 不因此接受嵌套 Bson。
Indexer 仅补足数组和明确 java.util.List/Collection 的既有标签范围校验，concept 仍只关联显式标签。
Stage/Expression 收录仍为 33/49，overload 总数 297；不新增根、内部实现或实例化规则。

## Stage 参数结构化语义

最终 Stage 审计覆盖 33 个 family、140 个 overload、291 个参数：新增 219 个参数 evidence，
保留 7 个已有 expression evidence，65 个专用类型/标量/回调参数不增加语义标签；
另补 UnwindOption.includeArrayIndex 的 String/getter 两个 publicMethods 参数。
完整逐参数原因及验证记录见 [Stage 参数审计](../../../mongo-plus-indexer/STAGE_PARAMETER_AUDIT.md)。

`@mongoParam` 沿用 semanticType、VALUE/ELEMENT、conceptRef 和 semanticEvidence，增加 13 种已审计的
Stage 参数语义和 15 个 concept。可选第四段仅选择兼容的已注册 concept，当前用于区分 graphLookup
两个 FOREIGN_FIELD_NAME 参数的遍历来源/目标；不按参数名推断。原表达式标签和 concept 保持不变。

- unwind(String) 原样传递；带 options 的 String 路径直接写 path。只有 getter 路径明确加 `$`。
- lookup 的集合名、localField、foreignField、输出 as 分别记录；Class 只解析集合名，不携带数据库。
- project/addFields/set/bucket/bucketAuto/match 的 Bson 参数是被外包一次的 Stage body；
  setWindowFields.sortBy 是排序 body；facet/lookup/unionWith 子管道由完整 Stage 组成。
- addFields/set 的 `(String value,SFunction... field)` 将 getter 名按序点连接为一个输出路径，
  不能当成多个独立输出字段。没有标签的同名 Object/Collection overload 不借用这些 evidence。

本轮 Core 仅 Javadoc 变化，JDK 8 编译通过；5 个 Index 自测、5 个复杂 Pipeline 的真实 BSON 对比、
表示/角色负向测试和连续生成确定性通过。未运行 MongoDB 服务端查询。
最终 API surface 仍为 Stage 33、Expression 49、family 82、overload 297，扫描闭包未扩大。

## Lookup 与跨集合边界

- 基础 lookup 支持 `from/localField/foreignField/as`，`from` 可用字符串或实体类；实体类仅经 `AnnotationOperate.getCollectionName` 解析 collection 名。
- pipeline lookup 支持 `from + pipeline + as`，也支持 `let variables + pipeline`；子 pipeline 可由另一个 `Aggregate<?>` 提供，`expr` 可通过 Query/BSON stage 表达。
- 没有公开 lookup database 参数，也没有跨 datasource 路由入口。动态集合 Handler 只替换主 collection；foreign name 不经过 `CollectionNameHandler`。
- Tenant/Logic 只处理 executeAggregate 的顶层 List，不递归 lookup/facet/unionWith 子 pipeline。
- lookup 数组、嵌套对象、实体集合和 Map 的读取均走通用 `MongoConverter`；没有 lookup 专用映射或 DBRef 交互。字段名/泛型必须与目标 DTO/实体匹配。

## 结果映射与资源生命周期

执行器固定请求 `AggregateIterable<Document>`。`aggregateList` 调用 `MongoConverter.read(iterable, TypeReference)`；`aggregateOne` 调用 `readDocument`。实体/DTO 字段读取依次涉及 TypeHandler、ReadHandler（解密、脱敏、DBRef）及 ConversionStrategy；`_id`、嵌套对象、泛型集合、Map 规则与普通查询相同，详见 [ENTITY_MAPPING.md](ENTITY_MAPPING.md)。DTO 不要求登记 namespace；registry 用于 collection 相关增强，不用于目标 DTO 的实例化。

聚合的 `Class<R>` 重载先统一包装成 `TypeReference<R>`，因此 `Class<Map>` 不会进入 `MongoConverter.read(MongoIterable, Class)` 的 key 驼峰捷径。随后 `AbstractMongoConverter` 的 Document 三参数 Map 分支调用两参数 `readInternal(Object, TypeReference)`；该调用运行时分派到 `MappingMongoConverter` 的 Map 转换实现，再经 `handleMapType`/`convertMap` 返回结果。故 `Class<Map>`、`TypeReference<Map<...>>` 均可完成顶层 Map 转换，不存在此前记录的无限递归；`Document.class` 在 Map 判断前直接返回原 Document。group 后字段名不匹配目标字段时不会自动推断；应 project/alias 或使用匹配 DTO。lookup 数组依赖目标字段的泛型信息。

Driver 返回 iterable 是惰性的，但 Mapper 在返回前立即通过 converter 消费；`out/merge` 通过 `toCollection()` 消费。源码没有显式 cursor close；异常直接传播。事务仅使 `SessionExecute` 调用带 `ClientSession` 的 aggregate，其他流程相同，见 [TRANSACTION.md](../features/TRANSACTION.md)。

## 执行选项

Wrapper 已确认封装并由 `AggregateUtil` 应用：`allowDiskUse`、`batchSize`、`collation`、`maxTimeMS`、`maxAwaitTimeMS`、`bypassDocumentValidation`、BSON/String `comment`、BSON/String `hint`、`let`。选项在 aggregate 返回 iterable 后、消费前设置。

当前未发现 Wrapper 封装：readConcern、readPreference、explain。它们不能仅因 Driver 支持而记为 MongoPlus 聚合 API。聚合 `count()` 是 `$count` stage，返回结果文档；普通 `countDocuments` 是独立 Execute 方法。当前没有聚合 page/count 组合入口，也没有框架自动执行两次聚合请求。

## 功能组合与边界

- Multi Datasource 在取得主 collection 前决定；lookup 不切换数据源。
- Sharding、高级异步拦截器可包围 aggregate，但当前未发现聚合专用结果合并契约。
- Listener 位于 Driver command 级别；普通/高级拦截器分别位于 Execute 外/内层。
- 实体映射、Auto Fill 不改聚合 pipeline；TypeHandler/解密/脱敏/DBRef 只参与结果转换。
- Map/Document 模式可执行聚合，但依赖实体 registry 的 Logic Delete/乐观锁增强可能跳过或取得 `UnClassCollection`。

## 测试清单与已确认缺陷

上述 Index/参数/BSON 编码测试已覆盖部分基础 Stage 组合；本轮未验证数据库执行链。执行层后续仍需按任务覆盖：空/null/custom pipeline；多个 match；Wrapper 重复执行；Tenant/Logic 有/无 match、Ignore 与用户同名字段；动态集合；lookup 基础/pipeline/let、子 pipeline 不增强；事务 SessionExecute；Map/Document/DTO/泛型/lookup 数组/_id；out/merge；所有执行选项；聚合 count 与无分页入口。

已确认缺陷/高风险行为：Logic Delete 在无 match 时把 `$match` 追加到尾部；Tenant/Logic 的无 match 分支会原地污染当前 List；顶层增强不递归子 pipeline。首/末 stage 约束的最终服务器异常、空/null pipeline 的准确 Driver 行为仍需运行验证。是否调整属于后续设计选择，本次不修改源码。

## 关键源码

- `aggregate/Aggregate.java`、`AggregateOptions.java`、`LambdaAggregateWrapper.java`、`AggregateWrapper.java`、`LambdaAggregateChainWrapper.java`
- `mapper/BaseMapper.java`、`AbstractBaseMapper.java`、`repository/IRepository.java`、`RepositoryImpl.java`
- `strategy/executor/impl/AggregateExecutorStrategy.java`、`interceptor/business/TenantInterceptor.java`、`CollectionLogiceInterceptor.java`
- `execute/instance/DefaultExecute.java`、`SessionExecute.java`、`toolkit/AggregateUtil.java`
