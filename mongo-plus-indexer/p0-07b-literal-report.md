# P0-07B：Aggregation Literal 正式 API 与 Evidence

日期：2026-10-12。仅 Core/Indexer；初始工作树干净。未修改 MCP/AI、未调用 Qwen、
未运行全仓 Maven、未实现 P0-07C，未重新审计 P0-01～P0-06。

## 审计与最小缺口

- 正式基线文件 SHA-256 为 `CA62489532C2F04022FE43540FC0610EDCF992497FF35870DD12D38A3E0C5724`，本轮先校验并原样保存。
- 当前 AggregateOperator 没有公开 `$literal` API。普通 Expression 的 String 原样编码，
  `$amount` / `$$variable` 的引用含义由服务器解释；普通字符串排除美元前缀。
- 既有 Expression 的 `RUNTIME_CODEC_VALUE` / 数组项只证明编码，不证明 Literal 的不递归求值边界。
  不能直接复用 PIPELINE_EXPRESSION 或普通 EXPRESSION_VALUE 参数角色来声称闭合。
- `$project` 已有 computed(String,TExpression) 的结果/组合/文档条目证据、fields 的
  varargs/List 有序 reduction，以及 project(Bson) 的 body 包装和单次 receiver 追加证据。
  本轮复用这些入口，没有 raw Document Stage 路线，也没有改旧 Query 构造器。
- Driver 的 Document 构造/append 只保存 String 键和 Object 值；DocumentCodec / CollectionCodec
  按 runtime 类型编码，不把美元字符串或键作为表达式执行。Core 原来可表示 Document/List 数据，
  但缺少公开 Literal 包装，不能据此伪造原目标的 API 构造成功。

## 新增契约与证明域

新增唯一公开 `AggregateOperator.literal(Object value)`，直接返回 `new Document("$literal", value)`。
静态返回 Bson，真实结果为 Document；工厂无 receiver 副作用，不追加 Stage。

- `@mongoExpression $literal`、VALUE shape、`LITERAL_VALUE VALUE`、
  `LITERAL_VALUE -> PIPELINE_EXPRESSION` 均为当前 overload 的独立 Javadoc。
- 新能力 `LITERAL_VALUE_CONSTRUCTION_V1`，沿用 `CANDIDATE_SEMANTICS_V1`，增加
  `operation=LITERAL_VALUE` 和 `LITERAL_DATA_RUNTIME_CODEC` 构造项，不新增 Expression Resolver。
- `applicability.literalValueDomain` 的 `evaluation=OPAQUE_DATA`、fieldReferences/variableReferences=NONE、
  operatorInterpretation=NONE_RECURSIVELY、nestedExpressionCalls=FORBID 明确求值边界。
  递归检查的是数据构造及编码来源，不是内部表达式调用树。
- 标量域：默认 Driver 5.4 的 String、Integer、Long、Double、Boolean、Decimal128 及 null。
  容器域：精确 Document 的空/单键构造及 append、有来源的 Arrays.asList(T...) 数据树；
  Java 21 实测该 List 的 runtime 类为 java.util.Arrays$ArrayList。
- 数据构造描述记录真实参数类型、append 返回 receiver、字段插入顺序、数组参数顺序、无重复键/无 NUL 键。
  必须逐构造、逐参数和逐 Codec 独立证明，并保持同一输入快照；容器类型或相同 registry 不替代证明。
- 缺 result/composition/shape/参数/必要来源或未知 Codec 不发布完整 bsonTerm；
  全部来源缺失、非法参数标签直接拒绝生成。方法名、类标签和相邻 overload 不提供证据。

主要来源：当前 Core 源码/Javadoc；[Driver Document](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/Document.java)、
[DocumentCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java)、
[CollectionCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java)、
[AbstractCollectionCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/AbstractCollectionCodec.java)、
[ValueCodecProvider](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/ValueCodecProvider.java)、
[Java 21 Arrays.asList](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Arrays.html#asList(T...))、
[MongoDB Literal 语义](https://www.mongodb.com/docs/manual/reference/operator/aggregation/literal/)。
服务器的“不求值”依据官方语义及正确的 BSON 边界；本轮没有执行 MongoDB 服务器。

## L01～L05 实际验证

动态 javac 使用 Java 21、`--release 21 -Xlint:all -Werror`，读取 Trees 的 ExecutableElement / ExecutableType，
随后装载并执行生成的 class。BSON 使用 Driver BsonBinaryWriter / BsonDocumentCodec 比较完整字节。

| 输入 | 实际 Java 入口及绑定 | 严格 BSON 结果 |
|---|---|---|
| L01 | literal(Object)，computed(String,TExpression)，TExpression 实际为 Bson | `$literal:"$amount"` 完整保留；直接字段引用产生不同字节。 |
| L02 | 两次 literal(Object)；fields(Bson[]) / fields(List<? extends Bson>) 两条真实路线 | fixed 后 enabled；Int32 100、Boolean true；两条路线均与目标字节一致。 |
| L03 | literal(Object)；Document(String,Object)；Arrays.asList(T[]) | 内部 `$gt` 键、String `$amount`、Int32 100 为数据；完整文档字节一致。 |
| L04 | literal(Object)；Arrays.asList(T[])；Document(String,Object) | 外层数组及内层 `$add:[1,2]` 的类型、值、顺序均保持；未替换为求值结果 3。 |
| L05 | gt(Object,Object)，computed(String,TExpression) | 普通 `$gt` Expression 数组字节正确，参数角色保持 PIPELINE_EXPRESSION。 |

六条动态路线（L02 两条）均绑定 project(Bson)，在同一 Aggregate<?> receiver 上执行
skip(7) → project → limit(9)，恰好三个 Stage，返回当前 receiver，追加/排列正确。
额外核验 Document() / append(String,Object) 的实际绑定和 append 返回当前 Document。

补充检查：null、Int32/Int64/Double/Decimal128、Boolean、空 Document/数组、内层 null、
`$$unbound`、内层 `$literal`，以及包含 `_id` 的原始字段顺序。改变类型、字段顺序或数组顺序产生不同字节。
自定义 String Codec 真实改变嵌套值；可变 Document 后续修改真实改变编码，证明其边界必须独立守卫。
未知对象即便深藏于 Document/List 也在 materialization 时因缺 Codec 失败。

L03/L04 的 Core BSON 构造正确；Indexer 对上述已审计数据构造路线发布完整的条件化证据。
这不授予任意 Document/List 变量、raw Map/Bson 或未知来源输入完整性，也不代表 MCP 已消费成功。

## 专项结果与正式 Index

Java 21 下执行：

```powershell
mvn -pl mongo-plus-core,mongo-plus-indexer -am test '-Dtest=LiteralEncodingTest,CommonExpressionEncodingTest,ExpressionCompositionEncodingTest,QueryPredicateEncodingTest' '-Dsurefire.failIfNoSpecifiedTests=false' '-Dgpg.skip=true'
```

- Core 26/26：Literal 5、CommonExpression 11、ExpressionComposition 5、QueryPredicate 5；失败/错误/跳过均为 0。
- Indexer 显式运行 8 个 main：LiteralEvidenceSelfTest、QueryPredicateEvidenceSelfTest、CommonExpressionEvidenceSelfTest、
  PipelineCompositionEvidenceSelfTest、PipelineExpressionSemanticsSelfTest、PipelineCandidateSemanticsSelfTest、
  MongoPlusPipelineIndexerSelfTest、PipelineExpressionCoverageSelfTest，最终均通过。
- Literal：31 个正检查、47 个拒绝检查；Query：81/102；CommonExpression：121/210；
  Candidate：81 条来源支持的 recipe，35 个等价/结构及 50 个拒绝检查通过。
- 负例覆盖缺参数/result/composition/shape/source/Codec、将字段引用或 Query 工厂代替 Literal、
  内部操作符当子调用、未知类型、未知 Document/List 构造、重复键/NUL、相邻 overload 隔离和改名夹具。
- 首跑通用测试的固定统计断言过期，已更新并实际复跑通过。参数审计 TSV 只新增 Literal 一行，旧行不改。

两个独立 Java 进程调用官方入口：

```powershell
& D:/Java/java21/bin/java.exe '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

正式输出 `target/generated-resources/mongo-plus-pipeline-api-index.json`，每次只原样复制备份。
两次均为 3,564,876 字节，byte[] 完全相同；未修改、重写或后处理任何生成 API JSON。
只读比较确认：原 80 条 candidateSemantics、原 1148 条 publicMethods、所有旧 family/concept 内容完全不变；
新增候选仅 literal(Object value)。统计为 101 families / 318 overloads，34 Stage、63 Expression、4 Query families。

新 SHA-256：`6F9302A22F59E58F893E91EC672A287631DF24D2D0D70AC336660E59D622B322`。
日志及原样 Index 备份在 ignored `mongo-plus-indexer/target/p0-07b/`，不提交生成物或临时文件。

## MCP 下一步及未闭合边界

消费新 capability、独立 LITERAL_VALUE Concept、当前 overload 的参数/result/composition/source、
LITERAL_DATA_RUNTIME_CODEC 及 literalValueDomain。将 Literal 内部作为有序、带类型的数据构造树，
禁止按美元前缀或 `$gt`/`$add` 键生成内部 Expression nestedCalls；仍递归验证每个数据节点的构造和 Codec。
外层继续消费 computed/fields/project 的真实组合与 receiver Effect；不能只凭顶层 `$literal` 映射生成部分计划。

任意 Bson/Map、Document 子类或未知构造来源、其他 List 工厂/实现、未知 Java 类型、自定义 Codec、
变化的输入快照、重复键或 NUL 键继续未闭合。Query 的结构化值域没有扩大；本轮不证明 P0-07C、
MCP 候选选择/Java 输出、模型效果、远端部署或服务器执行结果。

## 本次提交文件

- mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java
- mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java
- mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java
- mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java
- mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java
- mongo-plus-indexer/src/test/java/com/mongoplus/indexer/LiteralEvidenceSelfTest.java
- mongo-plus-indexer/src/test/java/com/mongoplus/indexer/MongoPlusPipelineIndexerSelfTest.java
- mongo-plus-indexer/src/test/java/com/mongoplus/indexer/PipelineCandidateSemanticsSelfTest.java
- mongo-plus-indexer/src/test/java/com/mongoplus/indexer/PipelineExpressionCoverageSelfTest.java
- mongo-plus-indexer/src/test/resources/pipeline-expression-parameter-audit.tsv
- mongo-plus-indexer/README.md
- mongo-plus-indexer/p0-07b-literal-report.md
- docs/ai/architecture/AGGREGATION.md
