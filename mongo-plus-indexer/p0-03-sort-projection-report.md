# Pipeline V1 P0-03：多字段排序与投影组合

> 本文保存初次 P0-03 审计和验证快照。后续已按独立授权补齐排序 body→Stage 公开 API；
> 当前构造链、证据、测试和新 Index SHA-256 见 [排序 Stage 缺口补齐报告](p0-03-sort-stage-report.md)。

日期：2026-10-09。范围：mongo-plus Core Javadoc、Indexer 契约、正式生成、Java/BSON 和既有回归。
依据：覆盖率报告 G03/P0-03、当前工作树真实源码及 MongoDB 官方规则。保留工作区已有 P0-01/P0-02
和 expression 改动；未修改 MCP、AI、Resolver、候选选择或 Core 可执行代码，未手工修改 Index JSON。

**结论：投影的三种合法组合已建立正式构造/模式 evidence，非法混合可按证据拒绝；混合方向排序
body 已建立有序 evidence，但现有 Core 缺少公开的 body→完整 `$sort` 构造工厂。该场景不能报告
完整 Core Stage 支持。此缺口与 P0-04 多候选选择是两个独立阻塞。**

## 1. 真实 API、委托链与 BSON 编码

### Sorts / Aggregate

| 公开入口 | 当前实际委托 / 编码 | 返回或追加内容 |
| --- | --- | --- |
| Sorts.asc(String.../List<String>) | varargs→asList→List；逐字段 BsonDocument.append(field,BsonInt32(1)) | SORT_SPECIFICATION body |
| Sorts.desc(String.../List<String>) | 同上，固定 BsonInt32(-1) | SORT_SPECIFICATION body |
| Sorts.asc/desc(SFunction...)、ascLambda/descLambda(List<SFunction>) | 按序 getFieldNameLine→字符串入口，不加 `$` 前缀 | 同上；不构造 String→getter |
| Sorts.orderBy(Bson.../List<? extends Bson>) | CompoundSort 逐 body、逐 keySet 合并；重键值最后覆盖，位置保持首次 | 有序 body，无 `$sort` 外层 |
| Sorts.orderBy(Order...) | 每个 Order.column/type→BsonDocument/BsonInt32→CompoundSort | 同上；Order 专用条目构造未新增正式 evidence |
| Aggregate.sort(String,Integer)、getter 入口 | 私有 orderBy→BsonDocument→Driver Aggregates.sort→custom | 完整单字段 `$sort` Stage |
| Aggregate.sortAsc/sortDesc 各 String/getter/List/varargs 入口 | 同方向收集字段→私有 orderBy(List,Integer)→Driver→custom | 一个完整同方向多字段 Stage |
| Aggregate.sort(Bson) | 原 Bson→custom→aggregateConditionList.add | **透传完整 Stage，不包装 body** |

Core 路径：`aggregate/pipeline/Sorts.java` 的 orderBy/CompoundSort；
`aggregate/LambdaAggregateWrapper.java` 的 sort、orderBy、custom。排序 body 可以保持三字段独立
方向，但直接 `aggregate.sort(Sorts.orderBy(...))` 实际追加裸 body，不能当作目标 `$sort`。
连续调用三次单字段 sort 则是三个 Stage，不能替代一个有序三字段 Stage。

公开 Driver `com.mongodb.client.model.Aggregates.sort(body)` 可以包装；Core 测试仅记录其互操作
事实，正式 Index 未新增该外部工厂或 raw BSON 构造能力，也未新增/修改 Core API 来填平缺口。

### Projections / Project / Aggregate

| 公开入口 | 当前实际委托 / 编码 | 语义边界 |
| --- | --- | --- |
| Projections.include(String.../List)、include(getter...)、includeLambda(List) | combine，固定 BsonInt32(1)；getter取实际字段名 | 包含标志 body，四个独立 overload |
| Projections.exclude 四种对应入口 | combine，固定 BsonInt32(0) | 排除标志 body，四个独立 overload |
| Projections.excludeId() | BsonDocument("_id",BsonInt32(0)) | `_id` 抑制 body |
| Projections.computed(String/getter,TExpression) | String→SimpleExpression；getter→getFieldNameLine→String入口；值走 runtime codec | 单字段 body；顶层数字/bool仍受 Stage 模式规则 |
| Projections.fields(Bson.../List<? extends Bson>) | FieldsProjection 按序合并；重键remove→append | body；重键最后值及最后位置生效 |
| Aggregate.project(Bson) | new BasicDBObject("$project",bson)→custom | body外包一次，一个Stage；不能传已包裹Stage |
| Project.projectDisplay/projectNone 的 String/getter 与 displayId overload | buildProject→Projection(column,固定1/0)→Condition.projectionCondition→Driver Aggregates.project→custom | 完整Stage；displayId=false追加 `_id:0`，true不主动追加 `_id:1` |
| Project.project(Projection.../Collection<? extends Projection>) 及 boolean overload | 同一路径写 column/value；集合转数组；false追加 `_id:0` | 只接收专用Projection；不把普通Bson/List当Projection |

`Condition.projectionCondition` 按列表顺序 put 到 BasicDBObject；重复列最后值覆盖且位置保持首次。
旧 Project 的12个 Stage overload、所有 effect 和实际 Java 类型均保留；其 Projection entry 和
displayId 条件条目的通用构造契约此次未新增。合法目标已有 Projections body 组合路线，无需给
专用对象入口虚构 typed-entry evidence。

## 2. 新增与复用的通用 evidence

- **复用 DOCUMENT_REDUCTION_V1**：两个 Sorts.orderBy 的显式 SORT_SPECIFICATION ELEMENT
  输入归约为同角色输出。保持 DOCUMENT_MERGE / INPUT / LAST_WINS / SHALLOW / EMPTY_DOCUMENT；
  新增可选 duplicatePosition=FIRST|LAST。原两个 Projections.fields 的既有输出完全保留。
- **复用结构化 ELEMENT / Java AST 校验**：Bson数组/varargs/List及extends上界；拒绝raw、嵌套、
  无界/lower-bound/wrong-element；新增中性 PIPELINE_PARAMETER_SORT_SPECIFICATION_ELEMENT。
- **新增 DOCUMENT_SHAPE_V1**：mongoDocumentEntry、mongoDocumentInput、mongoDocumentPolicy 和
  对应 mongoDocumentSource，输出 documentEntryConstruction、documentInputBinding、
  documentModePolicy。字段容器记录实际 ARRAY/LIST、VARARGS/SINGLE、元素类型和 INPUT 顺序；
  结果必须来自当前声明的固定Int32/表达式参数，不能从 Bson 返回类型或名称推断。
- **复用 computed composition / expression 参数 / Stage effect**：两个 computed 的原
  mongoComposition及resultSemanticEvidence保留；project包装依赖原 APPEND_STAGE/RECEIVER/
  ONE/CALL_ORDER；sort(Bson)只增加普通 publicMethods 输入角色，不新增 Stage MethodFamily。
- **变量与 nested receiver 保持原契约**：不改 lookup variable scope、factory/representation/
  extraction/container 或 expression 源码证据。投影模式不能批准缺证据的内层 expression。

规则由 BINDING_CONSUMER 执行，coreRuntimeValidationImplied=false。拒绝重复输入键发生在
归约前；LAST_WINS 记录运行时事实，不授权修改输入。FLAT_DOCUMENT 策略不声称支持全部 nested
投影、find-only投影或特殊computed排除，后者仍须独立证据。无操作符/Java方法名特判。

## 3. 五个测试场景结果

| 场景 | Java/BSON 与正式 evidence | 判定 |
| --- | --- | --- |
| sort createTime:-1, score:1, id:-1 | 两个body归约 overload及Order路径编码正确；字段原序、Int32、方向全部正确；Core sort(Bson)不会包装 | **合法body通过；完整CoreStage GAP** |
| project name:1, age:1, _id:0 | include→fields→project，两个fields候选均保持原序；一次Stage包装 | 合法，include与_id例外 |
| project name:0, phone:0 | exclude→project；不额外引入_id | 合法，exclude-only |
| project name:1, total:multiply(price,quantity), _id:0 | include/computed/excludeId→fields→project；multiply Object...及Collection实际Java/BSON通过 | 合法，computed和operand原序；候选与expression tree仍须独立闭合 |
| project name:1, phone:0 | Core原样编码{1,0}；正式模式消费测试拒绝，拒绝后输入不变 | **非法，拒绝；无自动改写** |

## 4. 字段顺序、模式与数字验证

输入字段必须完整消费一次，不能因相同方向合并远隔字段而改变顺序。sort三字段验证明确比较
keySet列表与[-1,1,-1]；projection比较[name,age,_id]和[name,total,_id]，不能仅依赖BSON文档的
值相等判断。单个输入列表及跨entry重复字段在消费者测试中拒绝；Core实际覆盖位置另有测试。

顶层 numeric / boolean 是模式标志：zero/false排除，nonzero/true包含；固定工厂编码Int32 1/0。
`computed("name",true)`仍编码Boolean，computed 1L/0.5分别保持Int64/Double，不能把语义等价
当作原始 BSON 类型相同。普通非零数值标志不截断成整数；排序方向则必须匹配来源声明的精确
Int32固定值，不允许0、2、小数、boolean或数字字符串冒充方向。

`_id` flag不决定其他字段的模式：include+_id:0、exclude+_id:1、仅_id均验证；_id的computed
值不享受flag例外。普通exclude不能混合include/普通computed，路径碰撞和空project拒绝。
嵌套multiply中的数字1是expression operand，不是投影标志；要投影数值/bool常量需独立的
opaque literal expression evidence，本次没有给computed数字参数伪造这种能力。

MongoDB规则依据：[官方project文档](https://www.mongodb.com/docs/manual/reference/operator/aggregation/project/)
及[MongoDB v8.0 projection_parser.cpp](https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/query/projection_parser.cpp)
的 isInclusionOrExclusionType、parseInclusion、parseExclusion、parseLiteral、parseAndAnalyze。
本轮是源码/规则核验与codec验证，没有连接MongoDB执行。

## 5. 保留的候选与构造阻塞

两个fields、两个sort body reducer以及所有合法fixed工厂overload均保留；未按声明顺序取第一个。
源码夹具反转两个合法overload后Index一致。String/getter、List/varargs仍保留真实Java类型；
String输入不能假造getter，专用Order/Projection条目不能由普通document推断构造。

常用computed场景仍有fields List/varargs、multiply Object.../Collection等候选；泛型、语义等价
和完整tree的唯一选择不在本次建立。group/ifNull/concat的现有多候选亦未修改，正式结果仍需
P0-04及各自独立result/type evidence。没有声称本轮MCP已得到SELECTED/complete=true。

混合sort完整CoreStage缺口不能由候选选择解决。数值/bool opaque literal、nested projection、
特殊computed排除及专用对象entry的额外构造证据也未冒充已完成。

## 6. 实际回归结果

使用Java21，Core维持原Java8编译target：

```powershell
$env:JAVA_HOME='D:/Java/java21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
& 'D:/apache-maven-3.8.6/bin/mvn.cmd' -pl mongo-plus-core,mongo-plus-indexer -am test
```

根/annotation/Core/Indexer四模块BUILD SUCCESS。**Core 59/59 JUnit通过**，其中新增
SortProjectionCompositionTest 11/11，其余48个既有测试通过。覆盖实际String/getter、两个reducer、
固定值/codec类型、顺序、一次包装、receiver和非法输入原样编码边界。

**显式Indexer main自测18/18通过**，新 PipelineSortProjectionEvidenceSelfTest **104 cases、
0 failures**。其余17项：MongoPlusIndexer、MongoPlusPipelineIndexer、PipelineExpressionSemantics、
PipelineExpressionCoverage、PipelineStageSemantics、PipelineCompositionEvidence、
PipelineReductionEvidence、PipelineObjectFieldBinding、PipelineConstructionEvidence、
PipelineUnionWithEvidence、PipelineLookupEvidence、VariableEntryConstruction、VariableBindingScope、
ExpressionCompositionEvidence、PipelineStageEffectEvidence、PipelineScalarSingletonEvidence、
MavenLocalRepository SelfTest。MavenLocalRepositorySelfTest无参数，其余按各main传项目根。

StageEffect的141个outer/inner正例与141个缺effect拒绝通过。旧composition/effect/入口映射负测
隔离新依赖标签；新测试独立验证缺parameter/source/effect、非法属性、类型冲突、class/overload
隔离、改名、反转overload、body/Stage角色、非_id非法混合、数字标志及重复/路径冲突拒绝。
Surefire的Indexer Tests run:0不计作自测通过；18个main均实际单独运行。

结构核对：33 Stage / 51 Expression / 84 families / 299 overload，主动类型10、引用类型91不变。
相对本轮baseline，仅23个既有方法视图改变并新增sort(Bson)的一个普通type publicMethods视图；
所有旧Stage effect、variable、expression/其他非目标证据保留。Core三个修改文件去除Javadoc后
与HEAD可执行代码一致。git diff --check通过。

记录：target/p0-03-maven-final.log、target/p0-03各SelfTest.log、Core target/surefire-reports；
target/p0-03/structural-check.json、structural-diff.tsv及冻结baseline.json。未运行全仓其他模块、
MongoDB服务端、MCP、Remote部署或真实模型验收。

## 7. 正式Index生成与SHA-256

同一源码使用正式CLI生成两次，无JSON后处理：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

两次均为 **2,594,448 UTF-8 bytes**，逐byte SequenceEqual=true，SHA-256均为：

```text
EE3D9B4E3060040746E314A5997570B6AAA1657A68E39269732CE053227E42E6
```

正式输出：target/generated-resources/mongo-plus-pipeline-api-index.json；CLI只生成JSON，伴随sha256
文件原先为旧值，本轮按实际JSON自动重算更新，未修改JSON内容。
冻结副本：target/p0-03/generation-1.json、generation-2.json。

## 8. 是否可以进入MCP验证

**可以进入分项MCP验证，不能进入P0-03全场景放行。** 固定上述Index/SHA，下游须识别
DOCUMENT_SHAPE_V1，按通用document construction/reduction/container消费模式规则，保留原
Stage effect/变量/expression，核对全部字段与原序，拒绝非法混合、缺证据和多候选未闭合。
未知capability仍应拒绝加载，不能静默忽略规则。

投影合法/非法场景和sort body可验证；混合sort完整Stage必须保持未闭合，未经额外批准建立真实
CoreStage构造能力不能输出Java，不能自动包装raw BSON。此轮只完成上游evidence及缺口交付，
到此停止，未提前实施P0-04。
