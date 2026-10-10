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
Stage 根 `Aggregate` 加六个明确的 Expression/composition 工厂根，详见
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
  `AggregateOptions.let` 的源码契约还明确声明 `$$` 变量访问语法。引用表示中的名称/path 正式提取；
  声明和作用域绑定使用独立 `VARIABLE_BINDING_SCOPE_V1`，需要消费者提供完整环境证据。
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

`$cond` 另以逐方法 `@mongoExpressionShape OBJECT|ARRAY` 记录 BSON 表达式外形，生成字段只位于
overload。四个现行声明按实际 BSON/委托路径为 OBJECT、OBJECT、ARRAY、OBJECT；Indexer 不从
`cond`/`condArray` 名称、MethodFamily 或参数信息推断缺失 shape，Stage/Expression 映射保持不变。

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

## Stage body 文档归约 evidence

`Projections.fields(Bson...)` 与 `fields(List<? extends Bson>)` 已增加独立的
`STAGE_BODY_DOCUMENT ELEMENT` 参数 evidence 和 `mongoReduction` 标签：按输入顺序浅合并，
同名键最后覆盖，空输入返回空文档。中性元素 Concept 与原 Stage body VALUE Concept 分离；
Index 仅在使用归约时声明 `DOCUMENT_REDUCTION_V1`。详细 DSL 和验证入口见
[Indexer README](../../../mongo-plus-indexer/README.md#stage-body-元素与文档归约-evidence)。
本批次未增加 include 固定值契约；MCP 加载期认识能力不等于已经消费归约，调用树留待后续实现。

## Stage 对象字段绑定 evidence

`Aggregate.sample(Number size)` 显式声明 `INTEGER_VALUE VALUE` 和 `mongoObjectField`，绑定 `size`
到 `$sample.size`，编码 `INT32_EXACT`、范围 1..2147483647；逐参数 `mongoObjectFieldSource`
记录 Core、Driver 5.4.0 和 MongoDB 的来源。Indexer 通用提取字段、编码、范围和来源，两个 Index
视图同步；仅存在实际绑定时声明 `STAGE_OBJECT_FIELD_BINDING_V1`。标签语法和约束见
[Indexer README](../../../mongo-plus-indexer/README.md#stage-对象字段绑定-evidence)。

实际 Core 仍经 `size.intValue()` 调用 Driver `Aggregates.sample(int)`，后者写 `BsonInt32`。
metadata 仅承诺范围内精确整数，不能将小数截断、溢出、自定义 Number 或其他 BSON 数值类型
视为等价；不更改运行时校验或方法签名。

2026-10-01：对象字段绑定统一必需项为 field 和实现来源，参数仍必须显式声明 VALUE 语义及匹配
concept。`INTEGER_VALUE` 保留完整 Int32 编码/范围校验；非整数绑定无需 encoding/range。
四个双参数 `unionWith` overload 显式绑定 coll→collectionName/collection、pipeline→aggregate；
String 原值与需要 Java Class 的集合名表示仍由既有 concept 和参数类型区分。

## Nested PIPELINE 构造 evidence

正式 Index 增加 `AggregateWrapper` 和 Core `Facet` 两个显式 construction roots，构造器复用
`mongoParam` / `mongoComposition`；新增通用 factory、receiver append effect、ordered PIPELINE
representation、参数 extractor 引用及 named-entry container 标签。复用 `PIPELINE` / `OUTPUT_FIELD_NAME`，
新增中性 `NAMED_PIPELINE` 条目语义及 `PIPELINE_CONSTRUCTION_V1` 能力。
源码和契约详见 [Indexer README](../../../mongo-plus-indexer/README.md#nested-pipeline-构造-evidence)。
目标路径创建独立 inner receiver，以 `Facet(String,Aggregate<?>)` 保持分支名称和 Stage 顺序，
一次外层 `facet(Facet...)` 合并多个 entry；本轮仅 Index evidence 和两个 Core BSON smoke 测试，
不代表 MCP Resolver 已消费能力。Stage/Expression surface 仍为 33/49、297 overload。

2026-10-08，P0-01 在当前工作树逐 overload 审计 58 个常见 Stage 入口，仅为已有独立 Stage 映射且
追加链闭合的 47 个声明增加 `mongoPipelineEffect`；Core 的方法签名和方法体、Indexer 契约均未改动。
审计表记录每条委托链及未补的五个任意 BSON 透传、六个未映射 addFields/set Object/Collection 入口。
effect 不代替 skip 范围/编码、projection 模式、Options、Field/BsonField entry 或 expression composition。
Core 已执行全部 47 overload 及其 facet/lookup/unionWith 的 141 个嵌入 BSON 组合；Indexer 逐标签删除后
对应 141 个 construction effect 验收均拒绝。正式 Index 两次生成一致，相对任务前只新增两个视图的
94 个 effect 字段，其他 evidence 不变；当前 surface 为 33 Stage / 51 Expression / 299 overload。
完整结果和 MCP 复用验证边界见 [P0-01 报告](../../../mongo-plus-indexer/p0-01-stage-effect-report.md)。

`unionWith(String,Aggregate<?>)` 实际经 getAggregateConditionList→unionWith(String,List)
→ Driver Aggregates.unionWith→unionWith(Bson)→custom(Bson) 追加到外层 receiver；Class overload
先用 AnnotationOperate.getCollectionName 再委托 String overload。两个 Aggregate 参数用既有
pipelineExtraction 引用正式 PIPELINE representation，六个正式 Stage overload 显式声明
APPEND_STAGE/RECEIVER/ONE/CALL_ORDER。U02/U03 复用独立 factory→sort/limit→PIPELINE→参数的
`PIPELINE_CONSTRUCTION_V1` 链；List 消费已有 Bson 列表，不新增 Stage→Bson 构造 evidence。
验证入口为 PipelineUnionWithEvidenceSelfTest、PipelineObjectFieldBindingSelfTest 和 Core
UnionWithNestedPipelineTest / SampleInt32EncodingTest；MCP planner 复用仍须下游实测。

## Lookup 与跨集合边界

2026-10-07：正式 Index 仅为 `lookup(String,Aggregate<?>,String)` 增加无 let pipeline evidence。
Core 直接取 `aggregate.getAggregateConditionList()`→Driver `Aggregates.lookup(from,List,as)`
→`custom(Bson)` 追加一个 Stage；Driver 按序写 pipeline 数组，from/as 字符串原样写入。
三个通用 objectFieldBinding 分别为 from→from、pipeline→aggregate、as→as，语义沿用
`COLLECTION_NAME` / `PIPELINE` / `OUTPUT_FIELD_NAME`；Aggregate 参数引用既有 pipelineExtraction，
Stage 明确 `APPEND_STAGE/RECEIVER/ONE/CALL_ORDER`，复用 `PIPELINE_CONSTRUCTION_V1`。
最小验证入口为 `PipelineLookupEvidenceSelfTest` 与 Core `LookupNestedPipelineTest`，输入限于
limit(1) 和 sort(createTime,-1)→limit(1)；不覆盖 letList 或变量/表达式，不代表 MCP planner 已实测。

同日补齐目标 `lookup(String,List<Driver.Variable<TExpression>>,Aggregate<?>,String)` 的有序 let entry evidence。
`let→letList` 使用通用 objectFieldBinding，ELEMENT 参数同时提供 `entryConstruction` 与
`typedContainerConstruction`。新 capability 为 `ENTRY_CONTAINER_CONSTRUCTION_V1`，与
`PIPELINE_CONSTRUCTION_V1` 独立；没有把 Variable 条目解释为 `NAMED_PIPELINE`。
真实 Driver 5.4.0 `Variable(String,TExpression)` 的公开构造签名、类型参数及 Object 上界在生成时由
显式 artifact 的 JAR 校验；key→`VARIABLE_NAME`、value→既有表达式 FIELD_REFERENCE representation、
构造结果→`VARIABLE_DEFINITION` 均由逐声明 Javadoc 契约提供语义来源。
typed-container 结构化记录泛型元素、实参、完整目标 List 类型、替换后的单元素与容器类型一致性、
List 不变性和 DOCUMENT entries 的 INPUT 顺序。全部 entry 共用一个可赋值实参；不从 raw 类型或
unchecked 转换证明兼容。Core 实现与公开签名保持原有行为，仅增加元数据及明确的 Driver 类型 import。
目标同时显式声明 from/pipeline/as 绑定、已有 PIPELINE extractor 和追加 effect，其他 overload 不继承标签。
生成器默认从本地 Maven repository 读取真实 artifact，无依赖下载；配置及受支持泛型边界见 Indexer README。
验证入口为 `VariableEntryConstructionSelfTest`（evidence 驱动 Java 编译与泛型/标签/名称/artifact 夹具）
和 Core `LookupVariableEntriesTest`（单 entry、多 entry BSON，显式检查 `userId→orderId`）。
同日增加独立 `VARIABLE_BINDING_SCOPE_V1`：目标 let lookup 声明 owner、body、parent、initializer、
inheritance、shadowing 与 exit；无 let String/Aggregate/String overload 显式声明继承 environment 边。
来源为 MongoDB r8.0.0 `DocumentSourceLookUp` 的父 initializer/独立 body parseState，以及
`VariablesParseState.defineVariable/getVariable` 的名称到声明 ID 映射和官方 lookup 说明。
只新增逐方法 Javadoc metadata，Core 公开签名和执行逻辑保持原行为。

ENTRY_KEY 原值为 declarationName；稳定 declarationIdentity 使用结构化
`{apiRef, ownerNodePath, declarationParameter, entryOrdinal}`，owner 为输入 JSON Pointer，
entry 序号来自现有有序 construction。`$$name.path` 拆为 referenceName 与 accessPath，
accessPath 不参与绑定。initializer 排除当前全部声明、使用父环境；body 使用新 scope；
最近可见同名声明覆盖父声明，退出恢复父环境，outer/sibling 不接收声明。

formal concept 要求 scope graph、声明集、external/system catalog 的完整性及来源证据。
没有默认 external/system 名单；显式目录的名称、身份和来源才可参与查找。
完整环境无匹配返回 `UNBOUND_VARIABLE`，证据不完整返回 `INCOMPLETE_VARIABLE_ENVIRONMENT`。
`VariableBindingScopeSelfTest` 验证正常绑定、missing、shadowing/恢复、initializer/隔离、显式 inherited body
与目录、非法/缺证据契约、无关名称夹具及生成稳定性。
详细结构见 [Indexer 变量绑定契约](../../../mongo-plus-indexer/README.md#通用变量声明作用域与绑定-evidence)。
该轮未补充 `$expr/$eq` composition；MCP 消费者运行不属于上游 scope 验证。

- 基础 lookup 支持 `from/localField/foreignField/as`，`from` 可用字符串或实体类；实体类仅经 `AnnotationOperate.getCollectionName` 解析 collection 名。
- pipeline lookup 支持 `from + pipeline + as`，也支持 `let variables + pipeline`；子 pipeline 可由另一个 `Aggregate<?>` 提供，`expr` 可通过 Query/BSON stage 表达。
- 没有公开 lookup database 参数，也没有跨 datasource 路由入口。动态集合 Handler 只替换主 collection；foreign name 不经过 `CollectionNameHandler`。
- Tenant/Logic 只处理 executeAggregate 的顶层 List，不递归 lookup/facet/unionWith 子 pipeline。
- lookup 数组、嵌套对象、实体集合和 Map 的读取均走通用 `MongoConverter`；没有 lookup 专用映射或 DBRef 交互。字段名/泛型必须与目标 DTO/实体匹配。

## Expression composition：operand → expression → query body → Stage

2026-10-08：新增 `AggregateOperator.eq(Object left,Object right)`，按声明顺序原样构造
`{$eq:[left,right]}`；既有 `Filters.expr(TExpression)` 构造 `{$expr:expression}`，随后
`Aggregate<?>.match(Bson)` 包裹 `$match` 并追加一个 Stage。查询 Eq 接口/Filters.eq 的字段条件
没有此双 operand expression 语义，不能借用其 `$eq` 映射。

正式证据复用逐参数 `PIPELINE_EXPRESSION VALUE`、`mongoExpressionShape ARRAY`、
`mongoComposition` 及结果来源：`PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION`
与 `PIPELINE_EXPRESSION -> STAGE_BODY_DOCUMENT`；match 复用已有 `STAGE_BODY_DOCUMENT VALUE`
并补充 `APPEND_STAGE` receiver effect。没有新增 composition 模型、变量模型、schema 或 capability。
Filters 增加为第六个显式根，只有标记的 expr 进入 family；结果语义表明它是查询 body，不可当作
aggregation expression operand。正式 surface 增至 84 families/299 overloads，Stage surface 不变。

字段与变量引用继续由同一个既有 expression concept 提供，`$` 排除 `$$`，变量绑定继续引用
`VARIABLE_BINDING_SCOPE_V1`；字面量/嵌套 Bson 保持 codec 路径，不推断或声明业务变量。
既有 expression concept 追加结构化 `literalValue`（运行时 codec/Java 类型需证据）与
`nestedExpression`（Bson 表示/子调用结果需证据），原字段与变量契约保持不变。
E01～E03 由正式 Index evidence 测试及真实 Java/BSON 测试共同验证，Int32、operand 顺序、
缺失证据和未绑定变量拒绝均覆盖。生成仍使用正式 CLI，没有 JSON 后处理；MCP/服务器验证需下游完成。

### P0-02：标量与 singleton 绑定 evidence

2026-10-08：`skip(int/long)` 复用 `INTEGER_VALUE VALUE`；两个 overload 实际都输出 BSON Int32，
long 先 `Math.toIntExact`。合法绑定范围是 MongoDB 非负整数与 Core Int32 表示的交集
0..2147483647；负数仍能在 Core 编码，超 Int32 的 long 仍抛异常，不改变运行时行为。

原对象字段绑定和构造条目容器不能表达整个 Stage body 的值槽及 VALUE→ELEMENT 提升，因此新增
通用 `STAGE_VALUE_BINDING_V1`。逐参数 `mongoStageValue` / `mongoStageValueSource` 输出
`stageValueBinding`；ELEMENT 复用一层 AST 容器校验并输出 `elementContainerBinding`，
只在显式声明时提供 `singletonLifting`。不依据 `$skip/$unset`、方法名或 Java 类型猜语义。

`unset(String...)` / `unset(List<String>)` 的字符串标量提升为单元素容器，字符串数组收集为真实
String 容器；Core 将一个元素编码为字符串，其余长度为数组。非空、字符串元素、重复拒绝是
合法绑定约束；Core 对空数组和重复字段的既有编码保留，字段路径/父子冲突仍需服务端校验。
getter overload 不获得字符串到 getter 的构造能力；任意 Bson 透传不进入 Stage family。

`sortByCount(String)` 复用已有表达式语义并增加原值标量编码、`$` 前缀及至少两个字符的约束。
对象表达式不绑定到 String，变量仍需已有作用域证据。unset 四个和 sortByCount 两个 overload
补充真实单次追加 effect；嵌套继续使用原 factory/representation/extraction/container，不改 planner。
相同输入的多个合法 overload 保留正式类型和编码差异；方法顺序不能决定选择。

正式扫描面仍为 33 Stage / 51 Expression / 84 families / 299 overloads。本轮 Core 48 个 JUnit
及显式 Indexer 17 个 main 回归通过，正式生成两次字节一致；本轮没有 MongoDB/MCP 实机验证。
完整审计、未闭合项和冻结 SHA 见 [P0-02 报告](../../../mongo-plus-indexer/p0-02-scalar-singleton-report.md)。

### P0-03：多字段排序与投影组合 evidence

2026-10-09：Sorts 的八个 asc/desc 工厂逐 overload 声明固定 Int32、有序字段键和真实 String/getter
容器；两个 orderBy(Bson.../List) 复用 DOCUMENT_REDUCTION_V1 并保持 SORT_SPECIFICATION 角色。
Projections 的八个 include/exclude 工厂声明 Int32 1/0，excludeId 声明固定 `_id:0`，两个 computed
保留原 expression/composition evidence。新增 DOCUMENT_SHAPE_V1 从独立 Javadoc 标签消费
document entry、输入角色和 flat projection 模式；普通数值/bool 顶层值是标志，嵌套 expression
operand 的数字仍是表达式值。未知表达式、nested 模式和特殊 computed 排除必须另有证据。

合法的 include/exclude/computed body 可通过 fields 的两个已有归约 overload 和 project(Bson)
包装一次。非 `_id` 排除不得混合 include/普通 computed；消费者必须在归约前拒绝重复键，核对
全字段覆盖、原序和路径碰撞，不能借 LAST_WINS 改写输入。Core 原样编码，未新增运行时模式校验。

`Sorts.orderBy` 返回排序 body，而 `Aggregate.sort(Bson)` 是完整 Stage 透传；新输入 evidence
明确此差异，后者仍不进入 Stage MethodFamily。P0-03 后续补齐了独立默认方法
`Aggregate.sortSpecification(Bson specification)`：接受 `SORT_SPECIFICATION VALUE`，经 Driver
`Aggregates.sort` 包装一次，调用 `custom` 追加到当前 receiver。`LambdaAggregateWrapper` 及其
子类继承默认实现，不要求第三方 Aggregate 实现新增抽象方法；`Aggregate<?>` 可直接调用。
`DOCUMENT_SHAPE_V1` 的 `documentInputBinding` 提供 body→Stage composition，与独立
`APPEND_STAGE/RECEIVER/ONE/CALL_ORDER` effect 闭合；返回值仍是 receiver，没有虚构 Bson result。
缺输入 composition、参数、来源或 effect 必须拒绝；裸 body 不能经旧透传入口进入完整 Stage。
真实 BSON 验证单字段、多字段、混合方向、Int32 原类型以及 facet/lookup/unionWith 内层顺序与隔离。
null specification 在追加前拒绝，旧 `sort(Bson)` 的输入及透传行为保持不变。
同方向 sortAsc/sortDesc 的已有 Stage 构造和 effect 保留。Projection/Order 专用 entry 的通用
构造证据仍未补齐；所有既有 overload 均保留，P0-04 候选选择未实施。

完整链路、五个场景、Core/Indexer 回归、正式 Index SHA 和 MCP 准入范围见
[P0-03 报告](../../../mongo-plus-indexer/p0-03-sort-projection-report.md)；排序 Stage 缺口补齐见
[排序 Stage 报告](../../../mongo-plus-indexer/p0-03-sort-stage-report.md)。

### P0-04 第一阶段：候选语义等价 evidence

2026-10-09：新增独立 `CANDIDATE_SEMANTICS_V1`，以逐方法 Javadoc 声明的通用构造项复用已有
参数、Stage value、document entry/reduction/input 和 pipeline effect。正式证据描述每条路线的
Java 类型与容器、适用输入域、实际 BSON 类型/值/顺序、Stage 数量、逻辑 receiver 及来源。
41 个条件化构造规则涵盖 skip、unset、Sorts、String 排序 Stage、Projections、现行
ConditionOperators.multiply 和 group 的直接 expression/命名条目路线；公开 API 和 Core
可执行源码保持原样。不能从这些节点事实推断未知 expression、getter 或命名条目构造已经闭合。

等价比较须代入全部已证明子树，执行实际 Java/语义/scope/codec 准入，再比较完整有序且带类型
的 BSON 和 receiver effect。旧 sort(String,Integer) 与 sortSpecification(Sorts...) 可以在相同
字段/方向/一个 Stage 的输入域证明整树等价；拆成多个 Stage、改变字段/operand 顺序、数字类型、
变量绑定或 sibling receiver 不等价。单元素 unset 的 Java 容器压缩不能授权改写原始 BSON。
同一 registry 不保证不同 Collection runtime codec 相同；默认 Driver codec 或独立编码证明
是必要前提。Order/Projection 对象构造、group getter 转换和非空 accumulator 子树的额外
构造/作用域缺口仍保持未闭合。没有实现 MCP 候选选择。

结构、候选关系、正负测试及正式 Index SHA 见
[P0-04 报告](../../../mongo-plus-indexer/p0-04-candidate-semantics-report.md)。

### P0-05 第一阶段：常见 Aggregation Expression

`AggregateOperator` 新增 `ne/gt/gte/lt/lte/subtract/divide(Object,Object)`、
`and/or(Object...)` 和 `not(Object)`，均为表达式工厂。前七项保存两个有序 operand；
and/or 保留零个、一个或多个元素；not 使用 singletonList 保留外层单元素数组。
null operand 编码为 BSON null，null varargs 容器拒绝。工厂不提前求值或推断服务器结果类型。
同名 Filters 方法仍是 Query Predicate；不能借用其映射。eq 只增加实际 Document 构造证据。

复用 `@mongoParam`、ARRAY shape、composition/result、字段/变量引用及
`CANDIDATE_SEMANTICS_V1`。新增通用 `EXPRESSION_ARGUMENTS` 构造声明，分别发布
固定 Java 参数按序组数组的 `ARRAY_ARGUMENTS_RUNTIME_CODEC` 和变参的既有
`ARRAY_RUNTIME_CODEC`；显式记录数量域、null 和不提前求值。旧候选分支和 41 条规则不变。
方法结果、composition、类型/参数、来源或子节点 codec/scope 缺失时不能建立完整树；
未知构造节点的消费者必须拒绝。BSON expression 不等于 Stage 或可自由替换的 raw BSON。

已验证范围为 Driver 5.4 默认 codec 下的 String、Integer/Long/Double/Decimal128、Boolean、
null 和这批工厂（含 eq）的递归 Document 表达式；变量仍要求既有作用域证明。
任意自定义 codec、opaque Bson、数组/对象 literal、未补齐组合证据的其他工厂不自动闭合。
Core Java/BSON 测试不替代服务器运行或 MCP 整 Stage 验证。
完整审计、测试结果和正式 SHA 见 [P0-05 报告](../../../mongo-plus-indexer/p0-05-common-expression-report.md)。

### P0-06 第一阶段：Expression result、类型关系与数组

普通 Object/Collection 参数的 multiply、ifNull、mergeObjects，三参数 cond/condArray，以及
toDate/toBool/toDecimal/toDouble/toHashedIndexKey/toInt/toLong/toObjectId/toString 的非 getter
入口补齐逐 overload 的 shape、composition/result 与 runtime Document 构造来源。
单值使用 VALUE shape；cond 对象形式记录 if/then/else 的字段序。四参数 condArray 真实委托
对象形式 cond，保留原行为；不能按方法名视为数组形式。

`AggregateOperator.concatArraysExpressions(Object...)` 独立命名，直接保存有序 operand，支持
`$items`/`$other` 和已证明的嵌套表达式。旧 `concatArrays(List<?>...)` 行为保持不变；数组 literal
仍需独立递归构造/Codec 证据。Getter 规范化、动态条件键和未知/custom codec 不自动闭合。

`JAVA_TYPE_RELATIONS_V1` 从声明的 Maven artifact 实际读取 `Document → Bson` 关系及反向否定，
记录 artifact/class 指纹，要求消费端相同 binary definitions。此事实不证明语义角色、Codec 或
BSON 等价。普通 mergeObjects 返回 Document；Accumulators 的 mergeObjects 返回 BsonField，
不得混用。`replaceWith(Document)` 包装 Stage，`replaceWith(Bson)` 是完整 Stage 透传。

复用已有参数/composition/候选契约，新增通用单值/固定字段对象构造项，不增加 operator planner。
Core 测试验证公开 API 的真实字节编码；Indexer main 验证证据准入；两者均不替代 MCP Stage
选择或服务器求值。完整审计与结果见 [P0-06 报告](../../../mongo-plus-indexer/p0-06-expression-result-type-report.md)。

2026-10-10 A05 补充：`replaceWith(Document)` 与泛型包装路线都经
`Aggregates.replaceWith → ReplaceStage → BuildersHelper.encodeValue → replaceWith(Bson) → custom`，
向当前 receiver 追加一次并返回 typedThis。两条声明复用 `CANDIDATE_SEMANTICS_V1` 的
`STAGE_EXPRESSION_DOCUMENT` 构造规则及显式 Expression→Stage composition；条件域只允许
有完整来源的精确 runtime Document 表达式，并要求独立核验 Java 静态类型/真实 overload/泛型绑定、
每个 Codec、有序且带类型的 BSON、Stage 数量/调用序和 receiver/scope。Object 的显式拓宽仍需
Document 来源证明；String、任意 Bson、未知对象/Codec、子类和缺证据不能据此合并候选。
两条原实现和 Bson 透传行为保持原样；此上游关系不替代 MCP 对具体树的准入或 SELECTED 验收。

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
