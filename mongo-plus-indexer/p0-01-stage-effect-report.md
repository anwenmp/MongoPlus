# P0-01：内层常见 Stage effect evidence

日期：2026-10-08。参考 `mongo-plus-mcp-server/pipeline-v1-coverage-report.md` 的 G01 / P0-01。
范围仅为 mongo-plus 的 Core Javadoc、既有 Indexer 契约生成及验证；没有新增 nested planner，
没有实施其他 P0 任务，也没有修改 MCP、Compact、Prompt 或部署。

## 1. 审计与补齐数量

逐一核对 Aggregate / Project 的 58 个相关 overload 及当前 LambdaAggregateWrapper 委托链。
正式生成之前所有 47 个有独立 Stage 映射的目标声明均缺少 effect，本次全部补齐：

| Stage | 审计 overload | 新增 effect | 本次未补 |
| --- | ---: | ---: | ---: |
| project（含 projectDisplay / projectNone） | 13 | 13 | 0 |
| group | 7 | 6 | 1 |
| count | 3 | 3 | 0 |
| unwind | 5 | 4 | 1 |
| addFields | 9 | 6 | 3 |
| set | 9 | 6 | 3 |
| skip | 2 | 2 | 0 |
| sample | 2 | 1 | 1 |
| replaceRoot | 4 | 3 | 1 |
| replaceWith | 4 | 3 | 1 |
| 合计 | **58** | **47** | **11** |

完整逐 overload 台账见 [pipeline-stage-effect-audit.tsv](src/test/resources/pipeline-stage-effect-audit.tsv)。
表中的 Core 委托均指向 `mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java`。
最终落点 `custom(Bson)` 仅执行一次 `this.aggregateConditionList.add(bson)` 并返回当前 receiver。
projection helper 的循环只构建一个 Stage body；Field / accumulator 数量不会变成 Stage 数量。
effect 描述正常返回时的状态变化，参数转换或校验抛异常时不承诺追加。

## 2. 没有补的入口与仍未闭合的事实

五个任意 BSON 透传 overload：`group(Bson)`、`unwind(Bson)`、`sample(Bson)`、
`replaceRoot(Bson)`、`replaceWith(Bson)`。它们原样追加输入，源码没有保证输入是相应的单个完整 Stage，
也没有独立 `mongoStage`，因此不能从方法名宣称对应 Stage 的 effect。
与之不同，`project(Bson)`、`addFields(Bson)`、`set(Bson)` 将 body 包装在明确的 Stage key 下，
故本次能补 effect，但 body 的绑定与合法性仍须单独验证。

另外六个 overload 是 addFields/set 各自的 `(SFunction,Object)`、`(SFunction,Collection<?>)`、
`(String,Collection<?>)`。真实链已确认单次追加，但它们没有各自的 `mongoStage`。
现有 `PipelineConstructionContract.instanceStage` 要求唯一显式 Stage 映射；本次不扩展映射，
不借用相邻 overload 的映射，也不为绕过门禁放宽契约。Collection payload 的编码不在本轮认证范围内。

其余 47 个 **effect 已闭合**，不等于每个 BSON 输入形态都能进入 GENERATE。
skip 的精确整数编码/范围、projection flags/mode、unwind Options construction、Field/BsonField entry、
group 候选选择及嵌套 expression composition 等保持原状。
Indexer 新自测明确断言 skip 没有凭 effect 获得 semantic/object binding，projection flag 没有模式 evidence，
unwind options 没有 entry/object construction。不会使用手工构建 Core 参数来宣称这些缺口已解决。

## 3. 实现与正式生成

生产源码仅在 Aggregate / Project 的 47 条 Javadoc 中增加：

```text
@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
```

复用现有 `PIPELINE_CONSTRUCTION_V1`；本轮没有修改 Indexer 生产代码、公开签名或 Core 方法体。
与任务前脏工作树的快照比较，Core 增量只有这些标签；保留了此前的表达式、变量及其他改动。

正式入口实际运行两次：

```powershell
& 'D:/Java/java21/bin/java.exe' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

输出：`mongo-plus-indexer/target/generated-resources/mongo-plus-pipeline-api-index.json`。
没有 JSON 后处理。两次生成字节一致，SHA-256 均为：

```text
0ADFD99BFB4A788E24E47C3CA6EC08C60685FF9690E45FE4C3F2CF21AC747088
```

与任务前使用当前 Indexer 正式生成的 Index 逐字段比较，只有 `methodFamilies.overloads` 和
`types.publicMethods` 各新增 47 个 `pipelineEffect`，共 94 处；所有已有字段、参数、capability、
概念、composition、变量和表达式 evidence 完全一致。统计仍为 33 Stage、51 Expression、84 families、299 overload。

## 4. 本轮实际测试结果

编译环境为 Maven 3.8.6 / JDK 17.0.20.1；显式 Indexer 自测及正式生成使用 JDK 21.0.11。
Core 保持现有 target 8。本轮没有执行 JDK 8 运行兼容矩阵。

- 修改标签前，新 `PipelineStageEffectEvidenceSelfTest` 因缺少逐 overload pipelineEffect 失败。
- Indexer `test-compile` 成功后显式运行新自测：58 个审计声明、47 个 effect、两个视图一致；
  47 × 3 = **141** 个 facet/lookup/unionWith construction effect 组合通过。
  真实源码逐 overload 删除 effect 后同样 **141** 个 effect 验收拒绝；相邻声明保持原 effect。
- Core `CommonStageEffectTest`：**5 个 JUnit 测试通过**。实际执行全部 47 个 overload，
  各自返回当前 receiver、只追加一次；三种外层的 **141** 个完整 BSON 比较通过，
  包含前后 limit 的调用顺序、facet 分支顺序及 receiver 隔离。另验证 long skip 溢出不追加。
- Core 旧能力回归：SampleInt32EncodingTest 3、FacetNestedPipelineTest 2、UnionWithNestedPipelineTest 2、
  LookupNestedPipelineTest 2、LookupVariableEntriesTest 2、ExpressionCompositionEncodingTest 5，
  共 **16 个旧测试通过**。加本轮新测试合计 **21 个，零失败、零错误、零跳过**。
- 旧 Indexer 自测 **15/15 通过**：MongoPlusIndexerSelfTest、MongoPlusPipelineIndexerSelfTest、
  PipelineStageSemanticsSelfTest、PipelineExpressionSemanticsSelfTest、PipelineExpressionCoverageSelfTest、
  PipelineCompositionEvidenceSelfTest、PipelineReductionEvidenceSelfTest、PipelineObjectFieldBindingSelfTest、
  PipelineConstructionEvidenceSelfTest、PipelineUnionWithEvidenceSelfTest、PipelineLookupEvidenceSelfTest、
  VariableEntryConstructionSelfTest、VariableBindingScopeSelfTest、ExpressionCompositionEvidenceSelfTest、
  MavenLocalRepositorySelfTest。覆盖既有 sample 严格整数负例、容器/提取器、变量作用域和表达式 evidence。
  自测通过来自实际执行 main；没有将 Surefire 的零测试误报为执行成功。

日志位于 Indexer `target/p0-01-*.log`，Core JUnit 报告位于 `mongo-plus-core/target/surefire-reports`。
旧 MavenLocalRepositorySelfTest 首次误传项目参数，由用法检查拒绝；按无参数入口重跑通过 19 cases。
本轮 BSON 测试不连接 MongoDB。

## 5. MCP 复用验证边界

**可以进入 MCP 复用验证**，使用上述正式 Index 和冻结 SHA，复用现有 PIPELINE_CONSTRUCTION_V1。
应按具体输入分别检查 effect、参数和 composition；其余 evidence 不足时仍应保持 UNRESOLVED。
本轮的 141 个删除标签拒绝是上游 construction effect 验收，不是 MCP Resolver 的实测结论。
MCP/Compact、部署、真实模型和真实 MongoDB 验证尚未执行，不能据此宣称全部常见 nested 输入已支持。

P0-01 到此停止。
