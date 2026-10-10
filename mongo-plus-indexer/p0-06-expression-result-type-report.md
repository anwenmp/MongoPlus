# P0-06 第一阶段：Expression Result、Java 类型关系与数组组合

工作范围：mongo-plus Core/Indexer。以当前源码、Driver 5.4.0 artifact 和真实编码为证据。
开始时正式 Index SHA-256 已实际核对为：

```text
78051E5FFB2B9334D32ECA867F97F54062B4FFF6C6573251839CB0A1186E250B
```

保留工作区已有 P0-01～P0-05 修改；未修改 MCP Resolver、Compact、AI Prompt 或 P0-07。
本报告的“闭合”仅指固定输入下的上游 Expression 子树，不表示服务器求值或完整 Stage SELECTED。

## 1. 各 Expression 的起始缺口

| Expression | 起始缺口 | 本轮结果 |
|---|---|---|
| multiply | Object varargs/Collection 已有数组构造候选规则，缺 shape、composition/result | 两条路线补 ARRAY 和独立 result/composition；原候选规则原样保留 |
| ifNull | List/Object varargs 有参数角色，缺 result/composition/runtime 构造来源 | 两条路线补 ARRAY、result、composition 和真实 Document/CollectionCodec 构造证据 |
| cond | 三参数对象与数组形式只有 shape/参数；四参数存在动态操作符键 | 三参数两条路线闭合；四参数保留未闭合；四参数 condArray 实际仍是 OBJECT |
| 类型转换 | 泛型 VALUE 参数已标记，但返回 Bson 不能证明结果及组合 | 九条非 getter 入口补 VALUE shape、result/composition 与单值 runtime codec 构造项 |
| concatArrays | List<?> varargs 不能直接接两个字段引用 | 增加独立命名的 Object operand 入口；旧 List[] 入口保留编码，仅补 shape/result/composition |
| mergeObjects | Document 返回类型缺正式赋值事实；缺 result/composition/构造证据 | String/Collection/Object varargs 三条路线补证据；类型关系从真实 artifact 读取 |

类型转换九条入口：toDate、toBool、toDecimal、toDouble、toHashedIndexKey、toInt、toLong、
toObjectId、toString。toHashedIndexKey 原 String 参数保持 String；没有扩成 Object。
转换工厂返回的是 Document 表达式，未在 Java 中执行转换，不把 toInt 的 Java 返回值标成 Integer。

逐 overload 审计见 [pipeline-expression-result-type-audit.tsv](src/test/resources/pipeline-expression-result-type-audit.tsv)。
共 39 条：34 条目标普通 Expression、3 条 accumulator、2 条未声明 Expression 映射的旧 getter API。
34 条中 19 条具备条件化的完整节点证据；旧 concatArrays 另有 result/composition，但 literal 子树未闭合。

## 2. 新增或补齐的正式 evidence

- 18 条现有非 getter overload 加一条新入口具备 resultSemanticType=PIPELINE_EXPRESSION、
  当前 overload 的 resultSemanticEvidence、composition、shape 和参数来源。原 concatArrays
  额外补 shape/result/composition，不把它升级成完整 literal 构造器。
- 复用 @mongoParam、@mongoComposition、EXPRESSION_ARRAY、EXPRESSION_ARGUMENTS、
  字段/变量引用 concept、VARIABLE_BINDING_SCOPE_V1 和 CANDIDATE_SEMANTICS_V1。
- 新增通用 EXPRESSION_VALUE / RUNTIME_CODEC_VALUE，表示一个原 VALUE 经 runtime codec 编码。
  参数仅接真实 Object、String 或显式无界方法类型变量；有界泛型不能据名字放行。
- 新增通用 EXPRESSION_OBJECT_ARGUMENTS / ORDERED_DOCUMENT_ARGUMENTS，显式记录固定参数
  与有序字段一一对应。cond 的字段为 if、then、else；未知/重复/数量错误字段拒绝。
- 标量 shape 使用 VALUE。新构造项需要独立 result、shape、composition 与逐参数证据。
  不识别新节点或 VALUE shape 的消费者必须拒绝，不能仅识别 V1 capability 名称就放行。
- candidateSemantics 记录 javaResultRepresentation=org.bson.Document、parameterBindings、
  逐叶子/容器 Codec 义务、immutable input snapshot、null 编码及来源。任意子节点已编码 BSON
  不能替代其实际 Java runtime 对象和 Codec 证明。

除 Java 类型事实没有新增 capability；未增加任何 operator 专用 planner 或候选选择规则。
EXPRESSION_ARRAY 的旧候选规则仍只是当前节点的条件化构造事实；完整树仍需独立 result/composition。

## 3. Java 类型事实与四类证明

新增 JAVA_TYPE_RELATIONS_V1，因为原契约没有外部 artifact 的一般 Java 类型关系载体。
类级显式 @mongoJavaTypeRelation 指定 artifact、subtype、supertype，Indexer 只精确读取声明的 jar，
通过独立 URLClassLoader（不初始化目标类）检查公开类型和实际 isAssignableFrom，不按类名映射。
来源包括 manifest/Maven 版本身份、jar SHA、两份 class SHA、当前源码声明。

正式事实：

```text
org.bson.Document extends java.lang.Object
implements java.io.Serializable, java.util.Map, org.bson.conversions.Bson
Bson.isAssignableFrom(Document) = true
Document.isAssignableFrom(Bson) = false
```

本地审计的 org.mongodb:bson:5.4.0 jar SHA-256：

```text
00E08C7EB1BD7DE8D1C590BCF886758927F952BEA67F770CA8F841705C2709F3
```

fact 的 applicability 要求消费端相同 binary type definitions，不授予泛型参数协变或 unchecked cast。
semanticRoleImplied、runtimeCodecImplied、bsonEquivalenceImplied 均为 false。
中性源码夹具另外声明 Document→java.util.Map 并通过，证明目标类型不按 Bson 名称硬编码。
缺 artifact、版本身份、类或来源时拒绝；缺 Index 事实时测试准入器不硬编码 Document→Bson。

四种判断必须分别完成：Java 声明类型可赋值；Expression 语义角色兼容；实际 runtime Codec
可证明；最终 BSON 类型、值和顺序严格等价。任何一项都不能替代另外三项。

## 4. Core API 兼容及真实构造链

唯一新增公开方法：

```java
public static Bson concatArraysExpressions(Object... operands)
```

`Object[] → Arrays.asList → new Document($concatArrays, operands)`。可直接接 `$items`、`$other`
和独立证明的 nested expression；不添加同名 varargs overload。
旧 `concatArrays(List<?>... list)` 仍是 `List<?>[] → Arrays.asList → Document`，保留每个内层数组，
原方法体未改；空参、null、List 实参的原编译绑定不变。新旧方法的 null varargs 容器仍会抛错，
显式 null 元素编码 BSON null。公开签名用 Java 8 release 的 javac 正负编译验证。

mergeObjects 的三条普通 Expression 路线：

```text
String → new Document($mergeObjects, value)                 // VALUE
Collection<?> → new Document($mergeObjects, values)        // 保留原 Collection，ELEMENT
Object[] → Arrays.stream.collect(toList) → new Document    // ELEMENT 数组
```

外层 runtime 都是精确 Document。String 单值与单元素数组不等价，不能相互归一化。
mergeObjects 的 Document 可进入 Object/无界泛型/已证明的 Bson 参数；实际函数调用仍需逐路线核对。
Accumulators.mergeObjects 返回 BsonField 命名累加器条目，未授予普通 Expression 角色。

角色另外交叉核对了 MongoDB r8.0.0 的
[独立 accumulator / Expression 注册](https://raw.githubusercontent.com/mongodb/mongo/r8.0.0/src/mongo/db/pipeline/accumulator_merge_objects.cpp)、
[ExpressionFromAccumulator 的继承/解析链](https://raw.githubusercontent.com/mongodb/mongo/r8.0.0/src/mongo/db/pipeline/expression.h)
和 [ExpressionNary.parseArguments](https://raw.githubusercontent.com/mongodb/mongo/r8.0.0/src/mongo/db/pipeline/expression.cpp)。
由这条源码链可确认普通 Expression 解析也接受未包数组的单个 operand；这不是借 accumulator
角色为 String overload 放行，也不把单值 BSON 与单元素数组判为严格等价。此项为源码核对，未执行服务端。

两条重要既有行为保留：

- cond(Object,Object,Object) 的内层是匿名 Document，键序 if、then、else；
  condArray(Object,Object,Object) 的内层是匿名 ArrayList，元素序相同。二者 BSON 字节不同。
  四参数 condArray 构造动态条件 Document 后委托 cond，所以仍输出对象形式。
- replaceWith(Document) 经 Driver Aggregates.replaceWith 包装完整 Stage；replaceWith(Bson)
  是 custom 的完整 Stage 透传。测试实际走 Document overload，不能因为可赋值就改走 Bson overload。

源码结构检查：去除 Javadoc 和唯一新方法后，两个 Core 文件的原可执行文本与开始快照一致。
正式 Index 中所有旧 publicMethods 字段、34 个 Stage families、扫描根、specialTypes 和原 concepts
均保留；原 52 条 candidateSemantics 逐项完全相同。没有扩大旧候选等价关系或按声明顺序选代表。

## 5. 正负 Java/BSON 验证

ExpressionResultTypeEncodingTest 包含 8 个 JUnit 测试，真实运行 Driver 编码并比较二进制 BSON：

| 输入 | 实际 Java/编码验证 |
|---|---|
| project total=multiply(toInt($price),$quantity) | 独立表达式递归及 Project 包装，字节一致 |
| match expr gt(toInt($score),60) | gt/转换/expr/Stage 真实组合，字节一致 |
| project value=ifNull($price,0) | String、Int32 及数组顺序一致 |
| project items=concatArrays($items,$other) | 使用新独立入口，字节一致 |
| replaceWith mergeObjects($defaults,$profile) | Document overload 包装；字段/数组序一致 |
| project result=cond[gt($amount,100),toString($amount),LOW] | condArray 三参数路线，字节一致 |

上述完整 Stage 是手工明确指定真实 API 的 Core 编译/编码测试，不是 MCP SELECTED 结果。
另测：普通字符串/字段/变量/null，Int32/Int64/Double/Decimal128/Boolean，嵌套返回值的 runtime
对象保留，List/varargs 两条路径，String 单值和数组差异，旧 literal API、空参/null 及编译负例。

真实 Codec 验证：

- Document 默认实际选择 DocumentCodec；Collection 默认实际选择 CollectionCodec，按序编码。
  null 按 BSON null 编码；默认已知叶子由实际 runtime 类型选择 Codec。
- 自定义 Integer Codec 将相同 Integer 编为 Int64，改变严格 BSON 类型与字节；自定义 Collection
  Codec 可把数组编成 String。Java 可赋值和相同 registry 身份不能证明编码等价。
- 默认 BsonCodecProvider/BsonCodec 可调用任意 Bson.toBsonDocument；匿名 Bson 编码成功的测试
  不为其授予 Expression 来源/result 证据。未知 Object 则在强制物化编码时缺 Codec 抛错。
- Core 可原样编码 `$$missing`；未绑定变量由正式 scope evidence 准入器拒绝，不能让 Codec 代替作用域。

ExpressionResultTypeEvidenceSelfTest 使用明确调用树、正式 Index 构造项和独立 Codec 条件递归验证，
不输入 raw BSON 冒充工厂，不选择 overload，不输出 MCP Java/BSON。
覆盖缺 result/composition/source/shape/type/parameter/Codec、未知对象、未绑定变量、深层缺 result、
raw BSON、accumulator 角色、错误 artifact/类型/泛型上界的拒绝。中性 $probe/assemble 工厂验证
通用单值构造能力，不按本轮 operator 名称编写 planner。

Driver 来源按 r5.4.0 源码核对：[Document](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/Document.java)、
[DocumentCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java)、
[CollectionCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java)、
[BsonCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/BsonCodec.java)。

## 6. 仍不能闭合的形态

1. 12 条正式 getter overload：multiply/multiplyLambda 两条、mergeObjectsOption/
   mergeObjectsLambdaOption 两条、八条类型转换 getter。源码输出 runtime Document 已审计，但
   getter→字段引用的完整规范化/构造和 Codec 契约尚未发布，本轮不借相邻普通 overload 的证据放行。
2. 两条动态四参数 cond/condArray：ifCondition 会补 `$` 构造任意 operator 键，不能仅因 String
   和 Collection 类型就证明内部 Expression 合法；四参数 condArray 的真实对象编码保持不变。
3. 旧 concatArrays 的递归数组 literal，以及新入口中的 List/对象 literal 子树：实际 Core 可编码，
   但尚缺独立、递归的 literal 构造准入证据，不能借 Object 参数泛放行。
4. 未映射的 mergeObjects getter plain 字段名路线、accumulator 角色、自定义 Codec、任意 Bson、
   未知 Java runtime 类型、未绑定变量或任一来源/result/composition 缺失的子树。
5. 服务端数组/文档类型约束、算术数值提升、转换失败、null 求值、变量实际绑定值；本轮未运行真实 MongoDB。
6. MCP 整 Stage 搜索、绑定、候选唯一性、Compact 输出、远端部署和真实 AI 调用。本轮未实施验证或适配。

## 7. 回归结果

Java 21 环境执行：

```powershell
$env:JAVA_HOME='D:/Java/java21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
& 'D:/apache-maven-3.8.6/bin/mvn.cmd' -pl mongo-plus-core,mongo-plus-indexer -am test
```

根/annotation/Core/Indexer BUILD SUCCESS；Core **95/95 JUnit 通过，0 失败/错误/跳过**。
Core 保持 Java 8 target；AggregateOperator class major version 为 52，公开新旧入口的 javac
正负编译显式使用 --release 8。Indexer Surefire Tests run:0 不计为 main 验证。

P0-04 自测：原 **35 个等价/结构检查、50 个不等价/拒绝检查**通过；候选事实总数由 52 增至 69，
旧 52 条规则原样保留。旧 common expression、eq/expr/match、Stage/变量/构造/排序/投影和 Query
回归按实际 main 清单运行，**22/22 个 main 显式执行后通过**。新增 evidence main 为
**174 个正验证、378 个拒绝检查**；旧 CommonExpressionEvidenceSelfTest 保持
**121 个正验证、210 个拒绝检查**。最终入口、返回码及修复后日志指针保存在
target/p0-06/regression-verified-result.json，原始每轮结果仍保留。

初次验证暴露并修复了测试/编译问题：switch 局部变量重名；CollectionCodec 非 public，改用实际
codec class name 验证；纠正默认 BsonCodec 可编码 Bson 实现及 replaceWith(Bson) 的透传假设；
旧 coverage 测试的 List-only 断言改为只约束原 concatArrays 入口，同时独立检查新入口。
原始失败日志和修复后日志均保留，没有修改原 Core 行为来迎合测试。coverage 源码修改发生在上一轮
testCompile 之后；发现旧 class 仍执行旧断言后，重新编译 Indexer 并独立重跑该 main，通过。
git diff --check 通过。

未运行其他模块或真实 MongoDB/MCP/AI，没有发布或提交。

## 8. 正式 Index 两次 CLI 独立生成

最终源码以 CLI 独立运行两次：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes `
  com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

generation-1.json、generation-2.json 与正式 JSON 逐 byte 相同，均为 **3,389,708 UTF-8 bytes**。
没有手工修改或后处理正式 JSON。伴随 .json.sha256 由实际 JSON 自动重算。
统计为 **96 families / 311 overloads**，其中 Stage 34/141、Expression 62/170；扫描根不变。
正式文件：target/generated-resources/mongo-plus-pipeline-api-index.json（相对于 indexer 模块）。

新 SHA-256：

```text
849E11788111E3C98DA294BA94F5266773E7A6DFD130204BF0FA9C93F870D500
```

## 9. 是否可以进入 MCP 通用验证

**可以对上述 19 条条件化闭合节点及六个目标 Expression 子树进入 MCP 通用验证。**
下游须固定本 SHA，识别 JAVA_TYPE_RELATIONS_V1、新通用构造节点及 VALUE shape；继续分别验证
result/composition、实际 Java 声明类型、runtime Codec、严格 BSON 与变量 scope，并保留全部合法候选。
不认识能力或遇到第 6 节未闭合形态应拒绝，不能新增 operator 专用 planner 或 raw BSON 逃逸。

完整 Stage 是否 SELECTED/complete=true 留给 MCP 阶段。本轮在 P0-06 上游边界停止。

## 10. 2026-10-10 A05 父级候选等价证据补充

已对 `replaceWith(Document)` / `replaceWith(TExpression)` 发布正式条件关系，复用
`CANDIDATE_SEMANTICS_V1`：两条路线均为 `Aggregates.replaceWith → ReplaceStage(value,true) →
BuildersHelper.encodeValue → replaceWith(Bson) → custom`，同一 receiver 追加一次并返回 typedThis。
通用 `STAGE_EXPRESSION_DOCUMENT` 声明要求独立本地/外部来源、Expression→Stage composition，
以及 Document 来源、真实 Java overload/泛型绑定、精确 runtime Document、DocumentCodec、
中间 BsonDocumentCodec、全部子树及容器/叶子 Codec、不可变快照、BSON 类型/值/顺序和 receiver/effect。
String、任意 Bson、未知泛型/Codec、Document 子类、null 或缺证据不进入此等价域。
Java 21 编译实证：`<Document>replaceWith(document)` 仍绑定 Document overload；实际泛型路线为
`<Object>replaceWith((Object) provenDocument)`，Object 拓宽必须保留独立 Document 来源证明。

验证：Core 六个相关专项 **44/44**（新增父级专项 6 项，补中间 Codec 反例后再次 6/6）；
Indexer **10/10 显式 main** 通过，新增父级专项 **8 正例/结构、148 拒绝/不等价检查**，含中性命名
源码夹具及缺参数/composition/effect/双源的真实扫描拒绝。旧变量作用域专项在并行编译窗口遇到类加载
异常，编译完成后独立重跑通过。去除 Javadoc 后 Aggregate 可执行文本与本轮开始快照一致；
原有 **69 条 candidateSemantics 完全相同**，只新增两条，96 families/311 overloads 保留。

正式 CLI 两个独立 Java 进程生成结果逐字节一致，**3,417,209 bytes**，新 SHA-256：

```text
8CFCD7FCD3E2E651A902313EA9FB4363CAD72AC7D48CFA5C1FAFD417BDE76F50
```

具备 MCP 消费所需上游证据，消费端必须执行 `documentExpressionDomain` 的全部守卫及完整树比较；
不支持守卫或证明缺失仍须保留 MULTIPLE_VALID。本轮未修改/运行 MCP、AI 或 P0-07，未运行全仓测试。
