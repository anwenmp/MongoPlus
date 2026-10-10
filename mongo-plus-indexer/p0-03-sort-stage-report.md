# P0-03 遗留补齐：SORT_SPECIFICATION body → 完整 $sort Stage

日期：2026-10-09。范围：mongo-plus Core 公开 API / Javadoc、现有 Indexer 契约消费、正式生成、
正负测试和回归。在任务开始时的未提交 P0-01～P0-03 / expression 成果上增量修改，保留其他工作。
不修改 MCP、AI、Resolver、候选选择策略；不新增 sort 专用 planner；完成后停止，不实施 P0-04。

## 1. 新 API 签名与设计依据

```java
default Children sortSpecification(final Bson specification)
```

公开入口位于 `mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java`。
选独立方法名以明确接收 SORT_SPECIFICATION body，不改变旧 `sort(Bson)` 的完整 Stage 透传。
只新增一个默认方法，没有新增 List/varargs/Order 等 Stage overload；body 组合继续使用现有 Sorts。
默认方法通过已有 `custom(Bson)` hook 追加，使现有 Aggregate 实现不必增加抽象方法实现。
`LambdaAggregateWrapper`、`AggregateWrapper` 及链式 Wrapper 继承默认入口；不修改 Wrapper 实现。
null specification 在追加前抛出 NullPointerException，其余 body 由 Driver 原样包装和编码。

```java
Aggregate<?> aggregate = new AggregateWrapper();
aggregate.sortSpecification(Sorts.orderBy(
        Sorts.desc("createTime"), Sorts.asc("score"), Sorts.desc("id")));
```

编码结果是一个 Stage：`{$sort:{createTime:-1,score:1,id:-1}}`。
多 Stage 使用独立 `aggregate.stage(...)` 语句，测试不依赖 `Aggregate<?>` fluent chaining。

## 2. 真实 Core 构造链

| 入口 | 实际委托与编码 | 结果 |
| --- | --- | --- |
| Sorts.asc/desc(String... / List<String>) | varargs→List→私有 orderBy；BsonDocument 按字段顺序 append 固定 BsonInt32(1/-1) | 排序 body |
| Sorts.asc/desc 的 getter / getter List 入口 | 有序 getFieldNameLine→字符串入口 | 排序 body，不加 `$` 前缀 |
| Sorts.orderBy(Bson... / List<? extends Bson>) | CompoundSort 按输入顺序逐 body、逐 keySet append | 有序 body；重键最后值覆盖，首次位置保留 |
| Sorts.orderBy(Order...) | 每个 Order.column/type→BsonDocument/BsonInt32→CompoundSort | 排序 body；专用 Order 构造证据未扩张 |
| Aggregate.sort(String,Integer) 及字段/getter 升降序 | LambdaAggregateWrapper.orderBy→BsonDocument/BsonInt32→Driver Aggregates.sort→custom | 原有完整 Stage |
| Aggregate.sort(Bson) | LambdaAggregateWrapper.sort→custom(bson)→aggregateConditionList.add(bson) | 原对象透传；裸 body 仍为裸 body |
| **Aggregate.sortSpecification(Bson)** | 默认方法检查非 null→Driver Aggregates.sort(specification)→LambdaAggregateWrapper.custom(stage)→当前 aggregateConditionList.add(stage)→typedThis | **一个完整 $sort Stage** |

Driver 依赖由根 POM 固定为 5.4.0；本次核对了
[该版本 Aggregates.java 的原始源码](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java)。
`sort(Bson)` 构造 `SimplePipelineStage("$sort", body)`；其 toBsonDocument 用一个 BsonDocument 的
Stage 键包装 `body.toBsonDocument(documentClass, codecRegistry)`，不合并、不按方向重排、不转换值类型。
对 Sorts 的 body，最终字段原序和 Int32 值因此保留。没有修改 Driver 或 Sorts 可执行代码。

## 3. 新增正式 evidence

新方法自身逐 overload 声明以下 Javadoc：

```text
@mongoStage $sort
@mongoParam specification SORT_SPECIFICATION VALUE
@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
@mongoDocumentInput parameter=specification semantic=SORT_SPECIFICATION encoding=WRAP_DECLARED_STAGE
```

另有两个独立 `@mongoDocumentSource mongoDocumentInput`：Core 默认方法/custom 链，以及固定的
Driver 5.4.0 sort/SimplePipelineStage.toBsonDocument 源码。Indexer 从源码正式提取到
Stage MethodFamily overload 和 Aggregate publicMethods，两份 evidence 完全一致：

- 参数是 `SORT_SPECIFICATION / VALUE`，使用已有 `PIPELINE_PARAMETER_SORT_SPECIFICATION`。
- 参数的 `documentInputBinding` 是 composition：`encoding=WRAP_DECLARED_STAGE`、
  `bodyToStageConstruction=DECLARED_STAGE_WRAPPER`，附当前声明及两个来源证据。
- 方法只有一个自身 `$sort` 映射，effect 为 `APPEND_STAGE / RECEIVER / ONE / CALL_ORDER`。
- `defaultMethod=true`，Java returnType 仍为 Children。包装的是追加的 Stage，方法返回 receiver；
  不添加把 Children 虚构为 Bson 的 `resultSemanticType=PIPELINE_STAGE_DOCUMENT` 或返回值 composition。

完全复用 DOCUMENT_SHAPE_V1 的既有包装能力、DOCUMENT_REDUCTION_V1 的两个排序 reducer 和
PIPELINE_CONSTRUCTION_V1 的 Stage effect；没有新增 capability、schema、Indexer 生产分支或生成后处理。
原 Sorts fixed entry 保序/固定 Int32 证据、所有旧 effect/variable/expression evidence 均保留。

缺 composition 且移除对应来源时，Indexer 不生成 documentInputBinding，消费者测试必须拒绝。
缺 effect 但保留依赖包装声明时，现有 Indexer 校验拒绝生成；composition/effect 同时缺失时也不能
从方法名、Bson 类型、Stage 映射或相邻 overload 推断。裸 body 不会成为完整 Stage。
这些是上游及测试消费者的负证据结果，没有声称已运行生产 MCP Resolver。

## 4. Java / BSON 测试结果

Java 21 执行构建；Core 保持 Java 8 source/target，Indexer 保持 Java 17 source/target。
任务开始时 Core 基线 59/59 通过；最终 Core **67/67 JUnit 通过**，其中新增
`SortSpecificationStageTest` **8/8 通过**，原 59 个测试保持通过。

| 验证 | 实际结果 |
| --- | --- |
| 单字段 asc(score) / desc(createTime) | 各包装一个完整 Stage，方向为 Int32 1/-1 |
| 同方向多字段 | 字段原序、所有方向、Int32 类型和一次包装正确 |
| 混合 createTime:-1, score:1, id:-1 | Bson varargs / List 两个 reducer 都正确，字段原序保留 |
| receiver 和调用顺序 | limit(1)→sortSpecification(body)→limit(2)，仅当前 receiver 追加一次，返回同一对象 |
| body 不修改 | 编码前后 body 值一致，Stage 没有额外 sort 外层 |
| 默认方法 custom hook | 已有子类覆写 custom，调用次数为一 |
| null | 追加前拒绝，既有 receiver Stage 不变 |
| 旧 sort(Bson) | body 和完整 Stage 两种入参都继续透传原对象；没有自动包装 body |

缺证据/角色冲突在 Indexer 正负测试中拒绝，Core 本身不新增排序模式或服务器规则校验。
本次未连接 MongoDB 服务端；BSON codec 验证不宣称真实数据排序执行成功。

## 5. nested Stage effect 结果

新增 Core 测试分别执行 facet、lookup、unionWith 的真实 Wrapper/Driver 嵌套构造。
内层 receiver 为 limit(1)→一个混合方向 $sort→limit(2)，逐 Stage 比较 BSON、调用顺序、
字段原序和 Int32 类型；外层各只追加一个 Stage。facet 还验证 rows/other 分支顺序和 sibling
独立列表，lookup/unionWith 验证 inner/outer 列表隔离。

新增 `PipelineSortStageEvidenceSelfTest` **30 个 nested composition/effect 正例、23 个拒绝通过**：
两个排序 reducer × 五种单字段/多字段/混合 body × 三个 outer；另独立验证改名后仍按标签生成。
缺 documentInputBinding 或 pipelineEffect 时，三个 outer 全部拒绝；真实源码删标签、缺来源、
缺参数/Stage 映射、错误输入角色或透传编码也拒绝。复用 factory、representation、
pipelineExtraction、facet entry composition/container，不引入生产 nested planner。

既有 P0-01 `PipelineStageEffectEvidenceSelfTest` 仍通过：47 个旧 effect，141 个 outer/inner
正例、141 个缺 effect 拒绝、11 个 unmapped/raw 边界；未改写其旧审计范围来吸收新 API。

## 6. 兼容性与既有回归

```powershell
$env:JAVA_HOME='D:/Java/java21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
& 'D:/apache-maven-3.8.6/bin/mvn.cmd' -pl mongo-plus-core,mongo-plus-indexer -am test
```

根 / annotation / Core / Indexer 四模块 BUILD SUCCESS。Indexer Surefire `Tests run: 0` 不计为
main 自测成功；**19 个 main 均显式运行通过**，包括新增的 PipelineSortStageEvidenceSelfTest。
18 个既有 main：MongoPlusIndexer、MongoPlusPipelineIndexer、PipelineExpressionSemantics、
PipelineExpressionCoverage、PipelineStageSemantics、PipelineCompositionEvidence、
PipelineReductionEvidence、PipelineObjectFieldBinding、PipelineConstructionEvidence、
PipelineUnionWithEvidence、PipelineLookupEvidence、VariableEntryConstruction、VariableBindingScope、
ExpressionCompositionEvidence、PipelineStageEffectEvidence、PipelineScalarSingletonEvidence、
MavenLocalRepository、PipelineSortProjectionEvidence SelfTest。

结构化核对相对本次冻结 baseline：84 个旧 family、299 个旧 overload、1,134 个旧 type
publicMethods **全部原样保留**；101 个 type、concepts、requiredCapabilities、其他顶层证据不变。
只新增 sortSpecification 的一个 Stage family/overload 和 Aggregate publicMethods 的一个视图。
当前统计：**34 Stage family / 141 Stage overload，51 Expression family / 159 Expression
overload，共 85 family / 300 overload**；主动类型 10、引用类型 91 不变。

javap 比较仅新增 `public default Children sortSpecification(Bson)`；冻结 Aggregate 源码去除
Javadoc 和新方法后与当前可执行源码一致。一个旧版 Java 8 子类在修改前的 Core 字节码上编译，
随后使用新 Core 加载，成功继承并调用新默认方法，custom 只执行一次。旧实现无需重新编译。
两个 body reducer 和所有既有 overload 仍保留，没有按声明顺序或数值方向实施候选选择。

记录位于 `mongo-plus-indexer/target/p0-03-sort-stage/`：baseline-maven.log、maven-final.log、
各 SelfTest.log、compatibility.log、Aggregate.before/after.api.txt、structural-check.json。
Core JUnit 明细位于 `mongo-plus-core/target/surefire-reports/`。`git diff --check` 通过。
未运行全仓其他模块、MongoDB 服务端、Remote 部署或真实模型验收。

## 7. 正式 Index 生成与 SHA-256

同一源码通过正式 CLI 独立生成两次，无手写 Index、无 JSON 后处理：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

两份冻结输出各 **2,602,987 UTF-8 bytes**，SequenceEqual=true；两份及正式文件 SHA-256 均为：

```text
B2BC752C28F0D81DE29925C4286E49250FFD286288C7462114F979AEF2D29E0F
```

正式文件：`mongo-plus-indexer/target/generated-resources/mongo-plus-pipeline-api-index.json`。
冻结副本：`target/p0-03-sort-stage/generation-1.json` 和 generation-2.json；
generation-result.json 保存 byte 比较与三个 hash，structural-check.json 保存正式证据 diff 断言。
SHA 伴随文件从实际生成文件自动重算；没有修改 JSON 内容。

## 8. 是否可以进入 MCP 验证

**可以进入 SORT_SPECIFICATION body→完整 $sort Stage 的 MCP 分项验证。** Core 实现、正式
composition/effect、Java/BSON 和 nested 构造证据已经闭合；本次没有执行 MCP 验证。
下游应固定本 Index/SHA，消费其 DOCUMENT_REDUCTION_V1、DOCUMENT_SHAPE_V1 和
PIPELINE_CONSTRUCTION_V1，完整保留每个字段、方向、Int32 与 receiver 调用顺序；缺 composition
或 effect 必须拒绝，不能把裸 body 绑定到旧 sort(Bson) 的完整 Stage 透传入口。

两个 Sorts.orderBy 候选和其他多候选保持原状，若候选无法唯一闭合仍须保留 unresolved / ambiguity。
未知 capability 不能忽略；没有声称 MCP 已 SELECTED/complete=true、Remote 已部署或实模已通过。
P0-03 初次报告中的混合排序 Core Stage 缺口已补齐；P0-04 未实施，到此停止。
