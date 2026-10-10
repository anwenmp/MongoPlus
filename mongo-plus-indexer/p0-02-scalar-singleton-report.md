# Pipeline V1 P0-02：标量与 singleton 绑定

2026-10-08。范围为 Core Javadoc、Indexer 通用 evidence、Java/BSON 与证据回归及文档。
没有改变 Core 运行时实现、公开方法签名、Stage/Expression 映射、MCP 或 planner；没有实施 P0-03。
任务开始时已有 P0-01、Expression、变量和本地仓库等未提交修改，均保留。下面基线来自这些
当前源码的正式生成，不将其他未提交修改计为本任务新增。

## 1. 审计的 overload 与真实语义

| overload | 真实委托/编码 | 可绑定输入与边界 |
|---|---|---|
| `skip(int)` | `Aggregates.skip(int)` → `BsonDocument("$skip", new BsonInt32(skip))` → `custom` | 精确整数 0..2147483647；Core 能编码负数，服务端拒绝 |
| `skip(long)` | `Math.toIntExact` → `skip(int)` | 与 int 相同 BSON/合法范围；超 Int32 抛 ArithmeticException，不追加；没有 Int64 编码路径 |
| `unset(String...)` | String 数组 → List → `unset(List)` | 字符串数组收集为真实 varargs 容器；字符串标量需要显式 VALUE→ELEMENT singleton lifting |
| `unset(List<String>)` | 一个元素 → BsonString；零个或多个 → BsonArray；最后 `unset(Bson)` → `custom` | 字符串数组收集为 List；字符串标量提升为单元素 List；不改顺序、前缀或重复元素 |
| `unset(SFunction<T, ?>...)` | 每个 getter 经 `getFieldNameLine` → String varargs | 可用真实 getter；没有字符串构造 getter 的 evidence |
| `unsetLambda(List<SFunction<T, ?>>)` | getter 列表取实际字段名 → `unset(List<String>)` | 同上；泛型不是字符串容器，不获得 lifting evidence |
| `unset(Bson)` | 任意完整 BSON 透传到 `custom` | 不具有独立 Stage 映射，不能承诺其内容为 unset，不准入 |
| `sortByCount(String)` | 原 String → Driver `SortByCountStage` → `BuildersHelper.encodeValue` | 原值字符串标量；服务端要求 `$` 前缀且后面有内容；对象表达式不能代替 String |
| `sortByCount(SFunction<T, ?>)` | `getFieldNameLineOption` → String overload | getter 加 `$` 的既有 FIELD_REFERENCE 表示；没有字符串到 getter 的构造能力 |

同时定向审计：`limit(int/long)` 也走 Int32/Math.toIntExact，但合法条件区别于 skip，本次不追加
未经逐参数闭合的标签；`count(String)` 是 OUTPUT_FIELD_NAME，需要独立输出字段合法性；
`replaceRoot(TExpression)` 的参数在 `newRoot` 对象字段内，不能套整个 body 标量槽；
`replaceWith(TExpression)` 的泛型运行时 codec 不能用 String 的证据覆盖；`out` 还改变 isSkip，
不能只凭字符串类型套用。本次只对已完整核对的 sortByCount String 复用新契约。

服务端事实核对的是 MongoDB 8.0 源码：

- [skip 解析与非负校验](https://github.com/mongodb/mongo/blob/v8.0/src/mongo/db/pipeline/document_source_skip.cpp)：
  `parseIntegerElementToNonNegativeLong` 与 `nToSkip >= 0`，零合法；服务端接受的非负 Int64 域大于 Core。
- [unset 解析](https://github.com/mongodb/mongo/blob/v8.0/src/mongo/db/pipeline/document_source_project.cpp)：
  字符串/数组类型检查 31002、非空检查 31119、字符串元素检查 31120，再构造 exclusion projection。
- [projection 路径校验](https://github.com/mongodb/mongo/blob/v8.0/src/mongo/db/query/projection_parser.cpp)：
  重复及父子路径碰撞触发 31250/31249，不能自动去重修复。
- [sortByCount 字符串校验](https://github.com/mongodb/mongo/blob/v8.0/src/mongo/db/pipeline/document_source_sort_by_count.cpp)：
  String 需要美元前缀和非空后续内容，否则 40148；对象表达式另有校验。
- [官方 unset 测试](https://github.com/mongodb/mongo/blob/v8.0/src/mongo/db/pipeline/document_source_project_test.cpp)
  固定了标量/数组正例及 31002/31119/31120 负例。本轮阅读这些源码，未执行官方 C++ 测试。

## 2. 新增与复用的通用 evidence

复用 `INTEGER_VALUE VALUE`、`FIELD_NAME ELEMENT`、`PIPELINE_EXPRESSION VALUE` 和它们的 concept。
复用 `ObjectFieldBindingContract.integerEncoding` 的精确 Int32 编码/上下界校验，以及
`PipelineConstructionContract.elementType` 的单层 AST 数组/varargs/List 结构解析。
原对象字段 JSON 结构及所有既有数值约束不变。

缺少的表达能力是“整个 Stage body 值槽”及“原始值到元素容器的显式提升”，因此新增通用
`StageValueBindingContract`，由逐 overload 的 `mongoStageValue` / `mongoStageValueSource` 驱动：

- `parameters[].stageValueBinding`：整个 Stage body 绑定、真实编码、输入形状、合法范围/大小/
  重复或字符串前缀/长度约束、独立来源。INTEGER_VALUE 记录 `EXACT_INTEGER_NO_ROUNDING`、
  输出 `INT32`，不承诺保留原 BSON 数值类型。
- `parameters[].elementContainerBinding`：真实 Java 容器/元素类型、单层有序收集、调用形式、
  显式 `singletonLifting`。单个元素仍按原 Core 压成标量，不承诺保留输入单元素数组的 BSON 形状。
- `STAGE_VALUE_BINDING_V1` capability/concept：消费者执行绑定约束，Core 没有隐式新增校验；
  候选顺序不能作为证据，多候选需要显式 Java 类型或已证明的等价性。schemaVersion 仍为 1.1。

不存在 skip/unset 名称分支。不同 Stage、方法、参数名、范围、前缀、容器大小、重复规则及禁用
lifting 的 synthetic fixture 同样按标签生成；类级或相邻 overload 标签不能补全当前参数。
缺失/非法契约失败关闭。effect 独立于绑定，不相互恢复。

为 unset 四个真实 overload 和 sortByCount 两个 overload 补齐已有单次追加 effect 格式，
仍消费既有 PIPELINE_CONSTRUCTION_V1 的 factory、representation、entry/container 和 extraction。

## 3. skip/unset 正负测试

| 输入 | Core Java/BSON 结果 | 正式 evidence 消费结果 |
|---|---|---|
| skip 2、0、2147483647 | int/long 两个 overload 全部输出 Int32 | 接受 |
| skip -1 / Integer.MIN_VALUE | 仍输出负 Int32，正常追加 | 拒绝负数，不能将 Core 可编码当作合法 |
| skip 2147483648、Long.MAX/MIN、9007199254740993L | long overload 的 Math.toIntExact 抛异常，之前的 Stage 不变 | 拒绝超范围，不截断或经浮点转换 |
| skip 9223372036854775808（超 long） | 没有该范围的 long 表示 | 大整数精确比较后拒绝 |
| skip 2.5 | JavaCompiler 确认没有可用 overload | 拒绝非整数；NaN 同样拒绝 |
| unset `"name"` | String varargs 调用合法，编码 BsonString | 两种字符串容器显式 singleton lifting 后可绑定 |
| unset `["name", "age"]` | String[]/List 均编码同序 BsonArray | 接受数组→目标容器 |
| unset `["name"]` | 两种容器均压成 BsonString | 接受；不保证数组 BSON 形状不变 |
| unset `[]` | Core 仍编码空 BsonArray | minimumSize=1 拒绝；MongoDB 源码为 31119 |
| unset 错误元素 | List<Integer>/Integer[] 无 Java overload；raw List 混合元素抛 ClassCastException 且不追加 | String 元素证据与输入类型校验拒绝；MongoDB 源码为 31120 |
| unset `["name", "name"]` | Core 保留重复数组 | duplicates=REJECT 拒绝；服务端 projection path collision |
| unset 字段路径 | Core 原样编码 `profile.name`、空串、美元字段或父子路径 | 正常 dotted path 表示可用；完整 fieldPathValidation 仍显式要求服务端校验 |
| unset null | BsonString(null) 抛 IllegalArgumentException；null 数组抛 NullPointerException；不追加 | 不声明 null 等价性 |

JavaCompiler 同时运行合法 skip int/long、unset 标量/数组/List 的编译正例，证明错误类型反例没有
因为缺 classpath 产生假阳性。真实 getter varargs、getter List、getter singleton 和 sortByCount
getter 的字段名/美元前缀编码也通过 Core 测试。

skip/unset 混合内部管道在 facet、lookup、unionWith 中逐 BSON 比较，保留 skip→unset scalar→unset
array 的调用序、独立 sibling/outer receiver 和原容器提取 evidence。新 Index 测试对 4 个内层
候选 × 3 个外层验证正式 factory/representation/entry/extraction/effect，共 12 个组合。

## 4. 数值精度、泛型与 singleton

数值消费测试用 BigDecimal→toBigIntegerExact→BigInteger 边界比较，不能使用 double 中间值。
INTEGER_VALUE 表示数学上精确的整数；不承诺任意 Number 子类或原 BSON Int64/Double/Decimal128
与输出 Int32 的类型等价。即使 skip(2L)，实际结果仍是 Int32，与 BsonInt64(2) 不相等。

容器只解开一层。具体 String 和 `? extends String` 的 List 上界可验证为 java.lang.String；
raw List、List<?>、`? super String`、List<List<String>>、错误元素、多维数组、同名外部 String/List
均被拒绝。泛型/类名不能生成 semanticType。

String scalar→一个 ELEMENT→目标 String[]/List 是显式 lifting；数组直接收集，Core 的 singleton
压缩另由 encoding 描述。两个 skip 候选具有不同正式 Java 参数类型、相同范围及 BSON 类型；
两个 unset 字符串候选具有不同容器/调用形式、相同 cardinality 编码。所有候选保留，未按排序选首个；
反转合法 overload 的源码顺序后完整 Index 不变。

## 5. 仍未闭合的候选或参数问题

- getter 构造、任意 Bson 透传、任意泛型 codec、完整字段路径合法性/父子碰撞、sortByCount 的
  对象表达式到 String、变量引用环境，不能由新的标量/容器契约自动补齐。
- 数值域超 Core Int32、原 BSON 数值类型或单元素数组形状严格保持，仍不被当前 API/evidence 保证。
- limit/count/out 等需要各自独立语义、约束及效果来源；本次未扩大映射数量。
- 多候选的下游决策必须消费正式类型/等价性 evidence；本轮未实现 MCP 选择策略或 planner。

## 6. Core/Indexer 回归与不回退核对

环境：Maven 3.8.6 / JDK 21.0.11，Core 维持 target 8，Indexer target 17。未运行 JDK 8 运行矩阵。

实际命令：

```powershell
$env:JAVA_HOME='D:/Java/java21'
& 'D:/apache-maven-3.8.6/bin/mvn.cmd' -pl mongo-plus-core,mongo-plus-indexer -am test -B
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp 'mongo-plus-indexer/target/classes;mongo-plus-indexer/target/test-classes' com.mongoplus.indexer.PipelineScalarSingletonEvidenceSelfTest .
```

Core 全量 **48 个 JUnit、0 failures、0 errors、0 skipped**；新增 ScalarSingletonEncodingTest
为 8 个，修改编译正例控制后再定向运行这 8 个全部通过。包含既有 reduction、project、Pipeline、
Variable、Expression 与 command listener 回归。首轮 getter 测试夹具缺真实字段而失败，补齐后通过；
首条定向 Maven 命令的 PowerShell -D 参数未完整引用，未进入测试，修正引用后执行成功。

Indexer Surefire 显示 0，不作为执行成功证据。显式运行 **17 个 main，全部通过**：
MongoPlusIndexerSelfTest、MongoPlusPipelineIndexerSelfTest、PipelineStageSemanticsSelfTest、
PipelineExpressionSemanticsSelfTest、PipelineExpressionCoverageSelfTest、PipelineCompositionEvidenceSelfTest、
PipelineReductionEvidenceSelfTest、PipelineObjectFieldBindingSelfTest、PipelineConstructionEvidenceSelfTest、
PipelineUnionWithEvidenceSelfTest、PipelineLookupEvidenceSelfTest、VariableEntryConstructionSelfTest、
VariableBindingScopeSelfTest、ExpressionCompositionEvidenceSelfTest、MavenLocalRepositorySelfTest、
PipelineStageEffectEvidenceSelfTest、PipelineScalarSingletonEvidenceSelfTest。

新标量/singleton 自测 **78 cases**；P0-01 effect 回归 **141** outer/inner 正例及 **141** 删除 effect 拒绝。
新证据测试在未修改标签时因 skip 缺 INTEGER_VALUE 失败，实施后通过。完善 sortByCount 标签时的
并行旧回归读取到了更新源码和旧 class，出现契约属性不匹配；重新编译、冻结源码后相关回归通过。

任务前正式 Index SHA：`0ADFD99BFB4A788E24E47C3CA6EC08C60685FF9690E45FE4C3F2CF21AC747088`。
逐结构比较：40 处新增、10 处变化、0 删除（数组下标比较把新 capability 插入引起的位移也计为变化）。
只增加两个视图各 5 个 stageValueBinding、2 个 elementContainerBinding、2 个 skip 参数语义及
6 个 receiver effect；另有 capability/concept 和 skip 描述变化。**308 处既有受保护构造/变量/
表达式/对象字段 evidence 完全相同，51 个 Expression family 完全相同**。扫描面保持
33 Stage / 51 Expression / 84 families / 299 overloads；Core Aggregate 去除注释后代码完全相同。

日志：`target/p0-02-*.log`；结构核对：`target/p0-02/structural-check.log`、`structural-diff.csv`；
Core 报告：`../mongo-plus-core/target/surefire-reports`。没有连接 MongoDB，服务端规则来自上述源码核对；
没有执行 MCP、Remote Index 或真实模型验收。

## 7. 正式生成与新 Index SHA-256

使用正式 CLI，未手工修改 JSON，执行两次：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

两次均为 **2,534,524 UTF-8 bytes**，逐 byte SequenceEqual 为 true，SHA-256 相同：

```text
1144690B0D0421C79D84C72C04A11391B0CAB6022BC36B67FC94110C7CC0F2FA
```

正式结果：`target/generated-resources/mongo-plus-pipeline-api-index.json`。
冻结副本：`target/p0-02/generation-1.json`、`generation-2.json`。

## 8. MCP 复用验证边界

**可以进入 MCP 复用验证**，冻结上述 Index/SHA，以通用 Stage body 值绑定及 singleton lifting
消费这些显式契约。下游必须识别并验证 `STAGE_VALUE_BINDING_V1`，保持数字精度、元素类型、
真实 Java 容器、候选决策及既有 receiver/effect，未知 capability 或缺 evidence 应拒绝。
“上游 ready”不表示现有 MCP 已支持新 capability，也不表示 Remote 部署或真实模型已验收。
P0-02 上游工作到此停止；不实施 P0-03。
