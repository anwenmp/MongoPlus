# P0-07A：普通 $match Query Predicate 正式证据

日期：2026-10-10。范围仅 Core/Indexer；没有进入 MCP/AI 仓库，没有调用 Qwen，没有运行全仓 Maven、
P0-06 专项套件或 P0-07B Literal 开发。初始工作树干净。

## 1. 修改前的 API / Evidence 审计

先通过 CodeGraph 定向核验 Aggregate、Filters、QueryWrapper、BuildCondition、BuildersHelper 和 Indexer。
在修改前使用正式 CLI 生成 baseline.json，SHA 精确等于用户给定的上一正式 Index：

`1E010831D1D8D5180A4FB8CB9D3A1974930411497C53CC86122AFBD4EB8285B3`

| 输入 | 已存在的真实公开能力 | 修改前的正式证据缺口 |
|---|---|---|
| Q01 隐式等值 + 比较 | Filters.eq(String,TItem) 生成裸字段值；Filters.gte(String,TItem) 生成字段下的 $gte。MongoPlusBasicDBObject.put(Bson) 可累积字段；QueryWrapper 也能累积条件，但 eq 输出显式 $eq，不是 Q01 的字节形式。 | eq/gte 无独立 Query 映射、参数/结果/composition 和候选项；普通多字段构造没有 Query 角色的纯组合证据。可变 MongoPlusBasicDBObject 路线还涉及对象状态和 MapCodecCache 全局 registry，不能自动视为完整构造。 |
| Q02 显式 AND | Filters.and(Bson...) 委托 and(Iterable<Bson>)；AndFilter 逐个编码子 Bson，保留 $and 数组，不做隐式 AND 化简。 | 两个真实 overload 的 Query 参数、元素表示、结果、组合和候选证据缺失。 |
| Q03 显式 OR | Filters.or(Bson...) 委托 or(Iterable<Bson>)；OrNorFilter 写有序 $or 数组。 | 同上；不能借 AggregateOperator.or 的 Expression 映射。 |
| Q04 $expr 比较 | AggregateOperator.gte(Object,Object) 生成聚合操作数数组；Filters.expr(TExpression) 有 PIPELINE_EXPRESSION 参数及 PIPELINE_EXPRESSION -> STAGE_BODY_DOCUMENT 组合。 | 它是既有 Expression 路线，不应发布普通 Query 映射或作为 Query 组合器的子条件。本轮未扩展 expr 的候选构造项。 |

Aggregate.match 有三个 overload：
- match(Wrapper<?>) 经 buildCondition().getCondition() 和 Aggregates.match；
- match(SFunction<QueryWrapper<?>,QueryWrapper<?>>) 创建 QueryWrapper 后委托；
- match(Bson) 实际执行 custom(new BasicDBObject("$match",bson))。

修改前 Bson overload 已声明 STAGE_BODY_DOCUMENT 参数和当前 receiver 的单次有序追加 effect，
但缺完整 documentInputBinding / candidateSemantics 及 Query 到 Stage 的显式桥接。
其余 Wrapper/Lambda 路线本轮未补正式 Query 构造证据，继续保持未闭合。

## 2. 最小实现与通用扩展

没有修改旧 eq/gte/and/or 方法体或任何无关 Stage。为了使 Q01 有可递归组合且不依赖隐藏全局
registry 的路线，新增 Filters.fields(Bson...) / fields(Iterable<Bson>) 两个真实公开工厂。
varargs 委托 Iterable；返回的 Bson 在编码时用调用方的 documentClass、registry 编码每个子条件，
按子条件及顶层字段的迭代顺序浅合并，不改写成 $and。重复顶层字段、空输入和 null 元素拒绝；
重复字段的失败发生在 BSON materialization 时。复杂同字段条件的自动合并不在本轮域内。

现有 Expression 参数角色不能表达 Query，故仅作以下通用扩展：
- QUERY_PREDICATE：已证明的查询 body，严格区别于 PIPELINE_EXPRESSION、STAGE_BODY_DOCUMENT 和完整 Stage。
- QUERY_VALUE：不解释字段/变量引用的查询比较值。美元字符串保持普通 String。
- 逐 overload 的 @mongoQuery；生成独立 PIPELINE_QUERY_PREDICATE family 和 mongoQueryPredicates。
  类级 Query 标签、方法名、返回 Bson 和邻接 overload 不提供此映射。
- 独立 capability QUERY_PREDICATE_CONSTRUCTION_V1。旧消费者不认识它必须拒绝，不可忽略。
- 复用 @mongoParam、@mongoComposition 和 CANDIDATE_SEMANTICS_V1：
  QUERY_FIELD key=<参数> value=<参数> operator=NONE|<显式操作符> 发布有序字段项和 BuildersHelper 编码；
  QUERY_SEQUENCE encoding=ARRAY operator=<显式操作符> 发布 BSON_DOCUMENT_ARRAY；
  QUERY_SEQUENCE encoding=MERGE_DISJOINT operator=NONE 发布 QUERY_DOCUMENT_MERGE。

QUERY_FIELD 的 NONE 表示隐式等值不写 $eq；mongoQuery $eq 是语义映射，不能把它强行写成显式 $eq。
字段必须为真实 String，值必须为 Object 或真实无界方法泛型；
序列必须为真实 Bson[]/varargs 或 Iterable<Bson>，逐参数记录 Java 表示、调用形式和语义来源。
结果必须由独立 composition/result 证明。来源、类型、角色或组合缺失不能发布完整构造项。
来源缺失拒绝生成；其他证据缺失为 NOT_ESTABLISHED 且没有 bsonTerm。

match(Bson) 补 DOCUMENT_SHAPE_V1 的 WRAP_DECLARED_STAGE 输入绑定、现有 STAGE_DOCUMENT_INPUT
候选构造项、QUERY_PREDICATE / STAGE_BODY_DOCUMENT 到 PIPELINE_STAGE_DOCUMENT 的显式组合。
原有 $expr body 可沿原角色进入 match；普通 Query 组合器不消费它。没有更改 match 实现。

## 3. 本轮实际验证

Java 21，受影响 reactor 限定 root/annotation/Core/Indexer，未运行全仓构建：

```powershell
$env:JAVA_HOME='D:/Java/java21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
& D:/apache-maven-3.8.6/bin/mvn.cmd -pl mongo-plus-core,mongo-plus-indexer -am test '-Dtest=QueryPredicateEncodingTest,ExpressionCompositionEncodingTest' '-Dsurefire.failIfNoSpecifiedTests=false'
```

Core：QueryPredicateEncodingTest 5/5、既有 ExpressionCompositionEncodingTest 5/5 通过，
失败/错误/跳过均为 0。Core 仍保持 Java 8 target；新增动态公开入口探针使用 --release 21，
通过 javac Trees 读取 ExecutableElement 和推导后的 ExecutableType（eq 的 String、gte 的 Integer
泛型绑定），随后装载执行生成的真实 Java 21 class。探针同时启用 -Xlint:all / -Werror。

| 输入 | 实际编译绑定 | Driver 严格 BSON / Stage 结果 |
|---|---|---|
| Q01 | eq(String,TItem)、gte(String,TItem)、fields(Bson[]) 和 fields(Iterable<Bson>)；两条路线均 match(Bson) | 两条路线与原始多字段 fixture 字节一致，隐式等值无 $eq；保留 status、amount 顺序。 |
| Q02 | and(Bson[]) / and(Iterable<Bson>)，子条件同上；match(Bson) | 两条路线与原始 $and fixture 字节一致，数组未化简、未重排。 |
| Q03 | or(Bson[]) / or(Iterable<Bson>)、eq(String,TItem)；match(Bson) | 两条路线与原始 $or fixture 字节一致，PAID、FINISHED 顺序不变。 |
| Q04 | AggregateOperator.gte(Object,Object)、Filters.expr(TExpression)、match(Bson) | 与 $expr:$gte 数组 fixture 字节一致；Indexer 拒绝把 Expression 或 expr body 当普通 Query 子条件。 |

七条动态编译路线均在同一 Aggregate<?> receiver 上执行 skip(7)、一次 match、limit(9)；
每条恰好三个 Stage，match 位于中间，返回原 receiver。比较使用 BsonBinaryWriter /
BsonDocumentCodec 的真实字节，不以 Map.equals 或 JSON 字符串代替 BSON 等价。

另验证 Integer/Long/Double/Decimal128、Boolean、null 的精确编码；美元字符串不变成引用；
改变数值类型、字段序或数组序产生不同字节；深层 AND/OR 保留结构；
调用方自定义 String Codec 真实改变子结果，说明共享 registry 或已知 Java 类型不能替代逐叶子 Codec 证明；
未知对象编码、重复字段、空合并和 null 元素拒绝。

Indexer main 均显式执行，未以 Surefire Tests run:0 充当验证：
- QueryPredicateEvidenceSelfTest：81 个正验证、102 个拒绝检查。
  覆盖逐参数/result/composition/source/候选项缺失、类级标签不能替代 overload、合法 overload 隔离、
  改名后证据仍有效、Expression/expr/raw Map/结构化 Literal/未知或未证明 Codec 拒绝，以及 Stage 桥接缺失。
- MongoPlusPipelineIndexerSelfTest：通过；更新三类别统计和删除 Expression 标签后的 Query 隔离断言。
- PipelineCompositionEvidenceSelfTest：通过。
- PipelineExpressionSemanticsSelfTest：通过。
- PipelineCandidateSemanticsSelfTest：80 个来源支持的 recipe，35 个等价/结构检查、50 个拒绝检查通过。
  原 71 条事实保留，新增 8 个查询工厂候选和一个 match 包装候选；没有提供选择策略。
  另直接对比修改前后 Index 的所有原 71 个 candidateSemantics，逐项内容完全一致。
- CommonExpressionEvidenceSelfTest：121 个正验证、210 个拒绝检查通过。

验证中通用回归的旧固定计数断言最初失败，随后按新增 Query 类别更新并实际复跑通过；
没有放宽旧 Expression/Stage 的角色边界。最终日志均在 mongo-plus-indexer/target/p0-07a/。

## 4. 正式完整 Index

正式入口（两次独立进程）：

```powershell
& D:/Java/java21/bin/java.exe '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

输出：mongo-plus-indexer/target/generated-resources/mongo-plus-pipeline-api-index.json。
每次原样复制为 target/p0-07a/index-first.json、index-second.json，使用 byte[] SequenceEqual
逐字节比较成功，均为 3,523,452 字节。没有手工编辑、重写或后处理任何生成的 API JSON。

两份 SHA-256：
`CA62489532C2F04022FE43540FC0610EDCF992497FF35870DD12D38A3E0C5724`

100 families / 317 overloads：34 Stage、62 Expression、4 Query families。
fields 无操作符映射，只在 Filters 的 publicMethods 中保留两个真实构造 overload；
故 8 个查询构造方法中有 6 个进入 Query families。生成物位于既有 ignored target 目录，未加入 Git。

## 5. MCP 下一步消费要求与未闭合语义

MCP 需要消费：
1. QUERY_PREDICATE_CONSTRUCTION_V1、QUERY_PREDICATE / QUERY_VALUE Concept、独立 Query family 和
   每个 overload 的 mongoQueryPredicates；不能以同名 Expression API 替代。
2. 参数语义/范围、实际 Bson 数组或 Iterable 表示、真实 Java overload / 方法泛型绑定、
   独立 result/composition 和 sourceEvidence。
3. QUERY_FIELD 的 DOCUMENT_ENTRIES / BUILDERS_HELPER_CODEC、QUERY_SEQUENCE 的
   BSON_DOCUMENT_ARRAY / QUERY_DOCUMENT_MERGE；完整递归证明每个子条件、比较值及 Codec。
4. match 的 Query -> 完整 Stage 组合、documentInputBinding、STAGE_DOCUMENT_INPUT 候选项、
   receiver 身份、单次追加与 CALL_ORDER。仅有顶层映射不等于完整计划。
5. 沿用既有条件等价：按实际输入代入全部构造树，比较带类型、带顺序 BSON 和 effect/scope。
   varargs 与 Iterable 是保留的合法候选，只有完整证明后才可能处于同一等价类，不按声明顺序强选。

以下仍为 UNRESOLVED / NOT_ESTABLISHED：
- P0-07B 数组/对象 Literal；任意 Document/Bson、自定义对象/Codec 或缺叶子证据不能自动放行。
- 未标记的其他 Query 操作符、Wrapper/Lambda match 的正式构造树、可变对象累积路线。
- 自动合并重复字段、空逻辑数组的服务器有效性、字段路径碰撞及复杂查询语法的全面准入。
- Q04 的既有 Expression 路线不获得普通 Query 角色；本轮没有新增 expr 的候选构造项。
- MCP 对具体输入的候选准入/SELECTED、真实模型输出、MongoDB 服务器命中/执行均未在本轮验证。
