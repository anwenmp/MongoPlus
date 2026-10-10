# P0-05 第一阶段：常见 Aggregation Expression API 与正式 Evidence

日期：2026-10-09。范围为 mongo-plus Core、Indexer、测试和文档。
基线正式 Index SHA-256：
`477C7EEE5A6A8092AB138DEEE196942A79DDE7E667F8CAC5615AAB9C74BF81B4`。
开始时已冻结正式文件和工作区状态到 target/p0-05；保留原有 P0-01～P0-04 未提交改动。
没有修改 MCP Resolver、AI Prompt，没有实施 P0-06/P0-07。

## 1. Core、扫描根与基线审计

先使用 CodeGraph 核对 AggregateOperator.eq、SourceScanner、配置和 Query 调用关系，
再定向读取实际源码、测试、POM，并核对基线正式 JSON。10 个目标表达式在基线中均没有
公开 Aggregation 工厂或 mongoExpressions 映射，不能借用同名 Query Predicate。

| Expression | 基线 Core 审计 | 新增公开签名，返回类型均为 Bson |
| --- | --- | --- |
| $ne | Filters.ne 是字段 Query Predicate，无 Aggregation 工厂 | AggregateOperator.ne(Object left, Object right) |
| $gt | Filters.gt 是字段 Query Predicate，无 Aggregation 工厂 | AggregateOperator.gt(Object left, Object right) |
| $gte | Filters.gte 是字段 Query Predicate，无 Aggregation 工厂 | AggregateOperator.gte(Object left, Object right) |
| $lt | Filters.lt 是字段 Query Predicate，无 Aggregation 工厂 | AggregateOperator.lt(Object left, Object right) |
| $lte | Filters.lte 是字段 Query Predicate，无 Aggregation 工厂 | AggregateOperator.lte(Object left, Object right) |
| $and | Filters.and 合并查询过滤器，无 Aggregation 工厂 | AggregateOperator.and(Object... expressions) |
| $or | Filters.or 合并查询过滤器，无 Aggregation 工厂 | AggregateOperator.or(Object... expressions) |
| $not | Filters.not 构造字段查询否定，无 Aggregation 工厂 | AggregateOperator.not(Object expression) |
| $subtract | 无公开工厂或正式映射 | AggregateOperator.subtract(Object left, Object right) |
| $divide | 无公开工厂或正式映射 | AggregateOperator.divide(Object left, Object right) |

Expression 扫描根仍为 Accumulators、AggregateOperator、Projections、Sorts、
现行 conditions.operation.ConditionOperators 和 Filters；Filters.expr 仍输出
STAGE_BODY_DOCUMENT。deprecated conditions.interfaces.ConditionOperators 不重复扫描。
扫描配置和 SourceScanner 均未修改，没有全项目遍历或扩张 Query Index。

## 2. 真实 BSON 编码

- 五个比较及 subtract/divide：new Document("$operator", Arrays.asList(left, right))，
  精确保留两个 operand 的顺序；subtract 的减数、divide 的除数始终在第二位。
- and/or：先拒绝 null Object[]，再将 Arrays.asList(expressions) 存入 Document。
  零、一、多个元素均保持数组；and((Object)null)/or((Object)null) 是单元素 null，
  and((Object[])null)/or((Object[])null) 则拒绝。
- not：new Document("$not", Collections.singletonList(expression))；
  单输入本身是集合时也保留内层数组，不使用 varargs 展开。
- 全部实际结果为 org.bson.Document，实现 Bson；不包装 Stage，不提前计算数值或真值。
  eq 仅新增真实 Document 构造来源及候选证据，其签名和执行语句完全不变。

官方语法逐项核对：
[$ne](https://www.mongodb.com/docs/manual/reference/operator/aggregation/ne/)、
[$gt](https://www.mongodb.com/docs/manual/reference/operator/aggregation/gt/)、
[$gte](https://www.mongodb.com/docs/manual/reference/operator/aggregation/gte/)、
[$lt](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lt/)、
[$lte](https://www.mongodb.com/docs/manual/reference/operator/aggregation/lte/)、
[$and](https://www.mongodb.com/docs/manual/reference/operator/aggregation/and/)、
[$or](https://www.mongodb.com/docs/manual/reference/operator/aggregation/or/)、
[$not](https://www.mongodb.com/docs/manual/reference/operator/aggregation/not/)、
[$subtract](https://www.mongodb.com/docs/manual/reference/operator/aggregation/subtract/)、
[$divide](https://www.mongodb.com/docs/manual/reference/operator/aggregation/divide/)。
and/or 的空数组和 not 的单元素数组均来自官方语法；Java 不代替服务器执行或类型提升。

Driver 固定版本为 POM 声明的 5.4.0。编码核验来源：
[DocumentCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java)、
[AbstractCollectionCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java)、
[CollectionCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java)、
[ValueCodecProvider](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java)。
Document 按实际类型委托 codec；Collection 基类按序写数组并单独写 null，
非 null 元素按 runtime Java 类型选择 codec。

## 3. 正式参数、result、composition 与来源证据

每个工厂独立声明 mongoExpression、mongoExpressionShape ARRAY 和 mongoParam。
固定参数使用 PIPELINE_EXPRESSION VALUE；变参使用 PIPELINE_EXPRESSION ELEMENT。
两个固定参数的 composition 为
`PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION`；
单参数/元素语义为 `PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION`。
resultSemanticType 及 JAVADOC/resultSemanticEvidence 均来自该方法的显式 composition，
不从 Bson 返回类型或相邻 overload 推断。

复用 CANDIDATE_SEMANTICS_V1，增加一个通用源码声明 operation=EXPRESSION_ARGUMENTS，
同时覆盖固定 Java 参数和 Object varargs。旧 EXPRESSION_ARRAY 等分支完全保留。
该通用声明独立校验参数语义、实际 Java 类型、ARRAY shape、composition/result、返回 Bson、
Document runtime 表示及审计的 CollectionCodec。

| 正式字段 | 事实和准入义务 |
| --- | --- |
| parameterBindings | Java AST 类型、SCALAR/ARRAY、SINGLE/VARARGS、逐参数语义及来源 |
| bsonTerm | DOCUMENT 的键来自该方法唯一映射；固定输入使用 ARRAY_ARGUMENTS_RUNTIME_CODEC.inputs，顺序为 DECLARATION；变参使用 ARRAY_RUNTIME_CODEC.input，顺序为 ITERATION |
| applicability.expressionOperands | 固定 minimumCount=maximumCount=参数数；变参 minimumCount=0、maximumCount=UNBOUNDED；nullValues=ENCODE_BSON_NULL；serverEvaluation=NOT_PERFORMED |
| javaResultRepresentation | 明确为 org.bson.Document，不由返回 Bson 推断 |
| runtimeCodecOperandSubstitution | 子节点 BSON 不足，必须独立证明实际 Java 表示及 codec |
| sourceEvidence | 每个方法单独声明当前 Core、Driver 5.4 四个 codec 源文件及官方聚合语法 |
| receiverEffect | NONE/ZERO，表达式工厂不追加 Stage |

复用 PIPELINE_EXPRESSION_FIELD_REFERENCE 的 fieldReference、variableReference、
plainStringValue、literalValue 和 nestedExpression；变量继续受 VARIABLE_BINDING_SCOPE_V1
约束。Object 不表示任意类型/codec 已获准。
缺独立依赖时为 NOT_ESTABLISHED 且无 bsonTerm，缺来源或非法标签拒绝生成。
消费者还须递归验证完整调用树；未知构造节点必须拒绝，不能忽略证据回退到 raw BSON。

## 4. Java/BSON 正负验证

CommonExpressionEncodingTest 的 11 个 JUnit 全部通过，覆盖：

- 10 个新增 API 的 operator key、数组包装、字段/普通字符串/变量前缀及双参数换序。
- 每个工厂的 null、负 Integer/Int32、Long/Int64、Double、Decimal128；Decimal128
  使用真实 org.bson.types.Decimal128，保留其值和 BSON 类型。
- 空/单/多元素 and/or、null 变参数组拒绝、not(null)、not 内部数组及多层嵌套。
- 用户两个最小输入通过真实 Aggregate<?>、Filters.expr、Projections.computed/fields 构造，
  Java 编译和最终 BSON 均精确匹配；这不是 MCP 整 Stage 验证。
- JavaCompiler 实际拒绝七个双参数 API 的少参/多参，以及 not 的零参/双参，共 16 个编译负例。
- new Object() 在默认 registry 强制编码时缺 codec 抛错；同名 Query Filters 的原 BSON 不变。
- divide(1,0) 和 subtract(1,2) 仅保存表达式，不提前求值或猜测运算结果类型。

Document.toBsonDocument 返回延迟 wrapper；测试显式物化后才判断 Codec 错误。
编译负例检查 javac 诊断码，不依赖中英文错误消息。

CommonExpressionEvidenceSelfTest 最终实际 main：
**121 个正验证、210 个拒绝检查通过**。逐个覆盖缺参数、shape、result、composition、
类型、来源、数量/null 域、容器 codec、未知 Java 类型、未绑定变量、深层缺 result、
raw BSON/完整 Stage 冒充表达式。中性 Factory.assemble/$probe 源码夹具证明没有按
这 10 个 operator 名称编写专用 planner；缺真实源码标签/错误 Java 类型不能闭合。
这些是上游证据准入自测，没有新增生产 MCP 选择器。

## 5. 已闭合形态和剩余边界

在审计的 Driver 5.4 默认 codec/固定输入快照下，已闭合：
字段引用、经既有 scope 证明的变量引用、普通字符串、Integer、Long、Double、
Decimal128、Boolean、null，以及本轮十个工厂和 eq 递归组成的 Document expression 树。
闭合指 Java 类型、编码和表达式角色，**不表示 arithmetic 的任意字面量组合在服务器合法**。

仍需独立证明或保持拒绝：

- 自定义 registry/容器/叶子 codec、opaque Java 对象、任意实现 Bson 的对象；
  仅有已编码 BSON 不能证明 runtime Document 替换等价。
- 普通数组/对象 literal、Date、其他数字 Java 表示和美元开头的字符串 literal 构造；
  not 内部集合已验证 BSON 包装，但没有自动授予通用集合 literal 的证据准入。
- 未绑定变量、其他 API 缺失的独立 result/composition/表示证据；本轮未补全全部
  multiply/cond overload 的组合义务，既有事实与行为保留。
- 服务器算术/日期约束、除零、类型提升和真值计算；未运行 MongoDB 服务端。
- MCP 整 Stage 搜索/绑定/overload 选择、远端部署及真实 AI 生成。

## 6. 回归、兼容和工作区保护

Java 21 环境执行：

```powershell
$env:JAVA_HOME='D:/Java/java21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
& 'D:/apache-maven-3.8.6/bin/mvn.cmd' -pl mongo-plus-core,mongo-plus-indexer -am test
```

根/annotation/Core/Indexer BUILD SUCCESS；**Core 87/87 JUnit 通过，无跳过**。
Core 编译 target 8，AggregateOperator class major version 为 52。
Indexer Surefire Tests run:0 不计为 main 自测；**全部 21/21 个 main 显式运行后通过**。
MavenLocalRepositorySelfTest 首次误传项目根触发用法拒绝，改为真实零参数入口后 19 cases 通过；
初始结果与重跑日志均保留。最终新增证据与候选 main 另行重跑通过。

P0-04 自测仍通过原 **35 个等价/结构检查、50 个不等价/拒绝检查**；候选声明数
从 41 增到 52（新增十个表达式及 eq 证据），原 41 个规则逐项完全相同。
旧 eq/multiply/cond/expr、Stage/变量/构造/排序/投影及 Query 自测均通过。
git diff --check 通过。

target/p0-05/structural-check.json 核对指定基线：
旧全部 Index 事实不变，仅 eq 增加 candidateSemantics、增加十个表达式及对应统计；
全部旧 publicMethods、Stage evidence、concepts、requiredCapabilities 和扫描根保持一致。
AggregateOperator 删除新增十个工厂并去除 Javadoc 后，既有可执行文本与开始快照一致；
CandidateSemanticContract 删除新通用分支后，与开始快照逐字一致。
没有扩大原 41 条候选的等价关系，没有按 overload 顺序选代表。

没有运行全仓其他模块、真实 MongoDB、MCP/Remote/AI 验证，也没有发布或提交。
完整日志和回归入口清单位于 target/p0-05/regression-result.json。

## 7. 正式 Index 两次 CLI 生成

最终源码使用 CLI 独立运行两次，没有 JSON 手工编辑或后处理：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

两次输出及正式文件逐 byte 完全一致，均为 **3,175,178 UTF-8 bytes**。
正式结构为 **95 families/310 overloads**：Stage 34/141、Expression 61/169；
主动类型 10、引用类型 91；参数审计增加 17 个槽，保留全部旧记录。
正式 JSON：
`target/generated-resources/mongo-plus-pipeline-api-index.json`。
冻结输出：target/p0-05/generation-1.json、generation-2.json；实际 SHA-256：

```text
78051E5FFB2B9334D32ECA867F97F54062B4FFF6C6573251839CB0A1186E250B
```

伴随 .json.sha256 从实际 JSON 自动重算；它不参与 JSON 生成。

## 8. 是否可以进入 MCP 通用消费验证

**可以进入上述已闭合输入和 expression 树的 MCP 通用消费验证。**
下游必须固定本 SHA，识别新的 ARRAY_ARGUMENTS_RUNTIME_CODEC 通用节点和显式数量域，
并继续独立验证 source/parameter/composition/result/Java type/codec/variable scope。
不认识节点或有缺失时拒绝，不能借旧 CANDIDATE_SEMANTICS_V1 capability 名称跳过检查。
原 ARRAY_RUNTIME_CODEC 和所有原候选规则保持原义。

本轮没有实现 MCP 消费/选择，也不声称两个最小完整 Stage 已获得 SELECTED/complete=true。
工作在 P0-05 上游边界停止。
