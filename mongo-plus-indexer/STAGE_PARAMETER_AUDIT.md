# Stage Parameter Semantic Audit

审计开始：2026-09-07；最终验证：2026-09-09。基线来自本轮重新编译后生成的 Pipeline Index；保留此前的 Expression 审计成果。

## 第一阶段：修改前完整清单

检查 **33 个 PIPELINE_STAGE MethodFamily、140 个 overload、291 个参数**。
这里的 33 是 MethodFamily 数量，并非 33 种不同 MongoDB Stage（projectDisplay/projectNone 等各占一个 family）。
无参 count() 计入 overload，参数数量为 0。

- [逐 overload 清单](src/test/resources/pipeline-stage-overload-audit.tsv)：包含全部 140 个声明，包括无参方法。
- [逐参数完整审计](src/test/resources/pipeline-stage-parameter-audit.tsv)：291 行，包含 Stage、family、完整 overload、parameter、Java type、修改前 semanticType、目标语义、scope、是否足够、是否需要 evidence、实现原因、原 Javadoc、声明类型、实现定位和最终 conceptRef。
- `ADD`：219 个已确认参数槽缺口；`EXISTING`：7 个现有 expression evidence；`NO_CHANGE`：65 个无需新增标签的参数。每个 overload 独立判断，不按方法族继承。

清单中的 `semantic` 对 ADD 行是审计拟议的结构化表达，`reason` 是实际代码语义。该清单在修改 Core/Indexer 前生成。
计数口径为参数槽，不把多个 overload 的同一角色合并为一个缺口。

## 33 个 MethodFamily 汇总

| Stage | MethodFamily | overload | 参数 | 缺口/新增 | 已有 | 无需新增 |
|---|---|---:|---:|---:|---:|---:|
| $addFields | addFields | 6 | 9 | 7 | 0 | 2 |
| $bucket | bucket | 5 | 11 | 9 | 0 | 2 |
| $bucketAuto | bucketAuto | 5 | 11 | 5 | 0 | 6 |
| $count | count | 3 | 2 | 2 | 0 | 0 |
| $densify | densify | 4 | 10 | 4 | 0 | 6 |
| $facet | facet | 5 | 8 | 6 | 0 | 2 |
| $fill | fill | 2 | 5 | 0 | 0 | 5 |
| $graphLookup | graphLookup | 6 | 33 | 30 | 0 | 3 |
| $group | group | 6 | 10 | 3 | 3 | 4 |
| $limit | limit | 2 | 2 | 0 | 0 | 2 |
| $lookup | lookup | 24 | 92 | 88 | 0 | 4 |
| $match | match | 3 | 3 | 1 | 0 | 2 |
| $merge | merge | 6 | 9 | 4 | 0 | 5 |
| $out | out | 3 | 4 | 4 | 0 | 0 |
| $project | project | 5 | 7 | 1 | 0 | 6 |
| $project | projectDisplay | 4 | 6 | 4 | 0 | 2 |
| $project | projectNone | 4 | 6 | 4 | 0 | 2 |
| $replaceRoot | replaceRoot | 3 | 3 | 2 | 1 | 0 |
| $replaceWith | replaceWith | 3 | 3 | 2 | 1 | 0 |
| $sample | sample | 1 | 1 | 0 | 0 | 1 |
| $set | set | 6 | 9 | 7 | 0 | 2 |
| $setWindowFields | setWindowFields | 2 | 7 | 2 | 2 | 3 |
| $skip | skip | 2 | 2 | 0 | 0 | 2 |
| $sort | sort | 2 | 4 | 2 | 0 | 2 |
| $sort | sortAsc | 5 | 5 | 5 | 0 | 0 |
| $sort | sortAscLambda | 1 | 1 | 1 | 0 | 0 |
| $sortByCount | sortByCount | 2 | 2 | 2 | 0 | 0 |
| $sort | sortDesc | 5 | 5 | 5 | 0 | 0 |
| $sort | sortDescLambda | 1 | 1 | 1 | 0 | 0 |
| $unionWith | unionWith | 6 | 10 | 10 | 0 | 0 |
| $unset | unset | 3 | 3 | 3 | 0 | 0 |
| $unset | unsetLambda | 1 | 1 | 1 | 0 | 0 |
| $unwind | unwind | 4 | 6 | 4 | 0 | 2 |

## 源码和 Driver 证据

主要执行路径是 [LambdaAggregateWrapper](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java)：
所有已选 overload 的委托、Lambda 转换和 helper 均逐一核对。
声明来自 [Aggregate](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java) 和
[Project](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Project.java)。
Driver 实现核对本机 `org.mongodb:mongodb-driver-core:5.4.0` sources.jar 的 Aggregates、BuildersHelper、Field、Facet、Variable 及 options。
这些是实际代码证据，没有从 MongoDB 官方语义倒推 MongoPlus 的能力。

| 路径 | 实际行为和结论 |
|---|---|
| unwind(String)，unwind(String,UnwindOption) | 分别进入 Driver `new BsonDocument("$unwind",new BsonString(fieldName))` 和 Core `path=new BsonString(fieldName)`。`$orders` 原样保存；`orders` 也原样保存，不补、不删 `$`。结合 Javadoc 的前缀要求，字段引用表示应显式标记；这不是运行时已经验证输入合法性的声明。 |
| unwind / bucket / bucketAuto / group / replaceRoot / replaceWith / sortByCount / graphLookup.startWith 的 getter | 各自实现调用 `getFieldNameLineOption()`，其实现明确为 `"$" + getFieldNameLine()`。限定为 getter 生成的字段引用，不冒充任意表达式 Java 参数。group 带累加器的两个 getter overload 允许 null，单 getter overload 不处理 null。 |
| lookup | 24 个 overload 分别核对。String from 原样写 from；Class from 经 AnnotationOperate.getCollectionName；localField 写当前输入字段名，foreignField 写外部集合字段名；as 写输出名。各 getter 使用 getFieldNameLine，不添加 `$`。 |
| graphLookup | startWith 的 Object 原样 encodeValue；getter startWith 添加 `$`。connectFromField/connectToField 都是外部集合中的字段名，getter 不加 `$`；from/as 分别为集合名/输出名。 |
| addFields / set | 三个已收录的 String-value overload：value 经 Field.of(String,value) 原样进入 Driver FieldsStage；输出 getter 不加 `$`。`(String value,SFunction... field)` 将 getter 名按顺序以点连接成**一个**输出路径，不能当多个输出字段。 |
| addFields / set 未收录 Object/Collection overload | 实现虽同名，但未声明 Stage 标签。本次只核对以避免误继承，不新增入口。`Field.of(SFunction,Object)` 会给名称加 `$`；Collection overload 构造特定 concatArrays 文档，不能借用 String-value overload 的语义。 |
| Field 构造器 | Field(Boolean,String,TExpression) 仅按 isField 改名称；value 原样交父类。已有 Field<?> 参数保持专用对象，不宣称 List<Field<?>> 接收表达式字符串。公开构造器的进一步内部语义不属于此次 Stage 参数槽修改。 |
| bucket / bucketAuto | groupBy 表达式与 boundaries 有序常量分开；Driver 虽然都 encodeValue，但 boundaries 是独立的边界数组。BucketOptions default/output 与 BucketAutoOptions output/granularity 保留专用类型。 |
| replaceRoot(Document) / replaceWith(Document) | Document 进入 Driver ReplaceStage.encodeValue，分别在 newRoot 或 $replaceWith 表达式槽；不外包成用户提供的完整 Stage，不强制 literal 文档解释。 |
| setWindowFields.sortBy | Driver 将 Bson 直接编码到 sortBy，需要内部排序 specification；不会去掉传入的 `$sort`。既有 partitionBy expression evidence 足够。 |
| project / projectDisplay / projectNone | Bson overload 外包 `$project`。Projection 专用对象经 buildProject→Condition.projectionCondition 读取 column/value；String/getter 列名是普通字段名，getter 不加 `$`。boolean displayId 只决定是否追加 _id:0。 |
| sort / sortAsc / sortDesc 及 Lambda-list 方法 | orderBy 将字段名写入 BsonDocument 键，方向写 BsonInt32，再 Aggregates.sort；集合是独立字段，getter 无 `$`。Integer 类型旧分类不理想不构成本次修改通用分类器的理由。 |
| facet / lookup / unionWith 子管道 | Aggregate 参数取 getAggregateConditionList；List/Bson varargs 按顺序编码完整 Stage 元素，不为元素外包操作符。Facet.name 是输出键；Facet 对象列表不是一条 pipeline。 |
| lookup.letList | List<Variable<TExpression>> 是变量定义对象列表；Driver 将 getName 写入 let 键，getValue 编码为表达式。既不是变量引用字符串列表，也不是任意 expression 列表。不给 Stage 参数虚构裸 variable-name 能力。 |
| out / merge / unionWith 集合 | String 原样写集合槽；Class 仅经 CollectionName 注解/配置的类名转换，不自动携带数据库。out(String,String) 的 db/coll 必须区分；MongoNamespace 自带 db/coll 结构，不标成单集合名。 |
| unset / unsetLambda | 名称原样写字符串或字符串数组；getter 无 `$`；一个元素时压成字符串，多个元素时数组，不改变字段名角色。 |
| densify / fill / merge / graphLookup options | 分别读取专用 range/options/output 对象的 API，再编码/合并文档。保持 Java 类型限制，不从外部 Driver 类型名称编造本地构造器或扩展扫描根。 |
| UnwindOption.includeArrayIndex | Core String setter 保存原值，getter setter 取无 `$` 字段名；unwind 再原样写 includeArrayIndex。这两个既有可达 publicMethods 是输出名，应另补 type evidence，不计入 291 个 Stage 参数。 |

## 最小表达方案

沿用 schemaVersion 1.1、parameters、semanticScope、conceptRef、semanticEvidence 和 concepts；JSON 顶层与参数字段结构不变。
保留三段式 `@mongoParam <parameter> <semanticType> VALUE|ELEMENT`，Stage 语义允许追加可选的第四段 `<conceptRef>`。
原实现只接受 PIPELINE_EXPRESSION，并固定引用一个 expression concept，无法表示名字/字段引用/完整管道/Stage 内部文档。
只开放本次逐源码审计得到的有限语义词汇，不增加新的字段层级、扫描范围、反推规则或 Resolver 产品代码。

第四段仅用于显式选择已注册、且与 semanticType 匹配的 concept；当前只有 graphLookup 的两个方向需要专用 concept。
只写 FOREIGN_FIELD_NAME 无法区分遍历来源和匹配目标，又不应为这两个普通字段名各造一个 semanticType，故做此最小标签语法扩展：

```java
@mongoParam connectFromField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_FROM_FIELD_NAME
@mongoParam connectToField FOREIGN_FIELD_NAME VALUE PIPELINE_PARAMETER_GRAPH_CONNECT_TO_FIELD_NAME
```

两个 concept 分别记录 `fieldRole=TRAVERSAL_SOURCE/TRAVERSAL_TARGET` 和 `bsonSlot=connectFromField/connectToField`。
不从参数名生成这些规则。未知语义、未知/错配 concept、非法范围、不兼容 Java 表示、同参数冲突标签均拒绝。
原 PIPELINE_EXPRESSION 标签仍使用原三段格式和原 concept，没有修改其契约。

| semanticType | 新增 Stage 参数槽 | 表示 |
|---|---:|---|
| BUCKET_BOUNDARY | 4 | 有序桶边界元素 |
| COLLECTION_NAME | 43 | String 集合名，或 Class 经已存在 helper 转换 |
| DATABASE_NAME | 1 | 数据库名 |
| FIELD_NAME | 30 | 普通字段名 |
| FIELD_REFERENCE | 16 | 字段引用；String 原样，getter 添加美元前缀 |
| FOREIGN_FIELD_NAME | 28 | 外部集合的普通字段名 |
| LOCAL_FIELD_NAME | 16 | 当前输入的普通字段名 |
| OUTPUT_FIELD_NAME | 39 | 输出键；另有 2 个 UnwindOption publicMethods 参数 |
| OUTPUT_FIELD_PATH_SEGMENT | 2 | 多 getter 按序点连接成一个输出路径 |
| PIPELINE | 14 | Aggregate 取完整管道，或 List 保存完整 Stage 顺序 |
| PIPELINE_EXPRESSION | 17 | 复用现有 expression concept，无新表达式机制 |
| PIPELINE_STAGE_DOCUMENT | 1 | varargs 的每个元素是一条完整 Stage |
| SORT_SPECIFICATION | 2 | 内部排序文档 |
| STAGE_BODY_DOCUMENT | 6 | 由该 overload 的显式 mongoStages 外包一次的内部文档 |

新增 **13 种 semanticType、15 个 concept**；PIPELINE_EXPRESSION 复用。13 个默认 concept 的 id 为 `PIPELINE_PARAMETER_<semanticType>`；另 2 个是上面的 graphLookup 方向 concept。
显式角色覆盖旧的通用分类，但 Java type/varargs 保留，getter 仍必须是 SFunction，Class 仍必须是 Class。
FIELD_NAME、LOCAL_FIELD_NAME、FOREIGN_FIELD_NAME 共享普通字段名的编码方式，concept 单独记录字段所属上下文。
这些概念不是新的 MongoDB 名称合法性校验器：名字槽中的美元字符不会被 Core 自动剥离或解释成引用；Resolver 必须先匹配调用者意图的语义角色，再应用表示证据。

65 个 NO_CHANGE 参数的逐项原因在完整清单中：专用 options/typed carrier、数值数量/方向、boolean、Wrapper/条件回调。
“无需标签”仅表示无需给该参数槽增加通用语义，不表示能凭 opaque 外部类型构造其所有内部配置。

## 验证记录

第一阶段完整清单生成后，在尚未补标签的旧生成器/源码上运行新增校验，按预期因参数 evidence 缺失失败。
随后补 219 个 Stage 标签（Aggregate 211、Project 8），另补 UnwindOption 两个 setter 标签。
逐文件比较已确认这三个 Core 文件只有 `@mongoParam` 行变化，没有执行代码、签名或 Stage/Expression 映射变化。

| 验证 | 2026-09-09 结果 |
|---|---|
| Core 及依赖模块 JDK 8 compile | PASS |
| Indexer JDK 21 test-compile | PASS |
| MongoPlusIndexerSelfTest | PASS |
| MongoPlusPipelineIndexerSelfTest | PASS，包含重复生成和入口/继承/未标记 overload 边界 |
| PipelineExpressionSemanticsSelfTest | PASS |
| PipelineExpressionCoverageSelfTest | PASS，49 family / 157 overload / 372 参数及嵌套表达式场景 |
| PipelineStageSemanticsSelfTest | PASS，逐参数及完整 overload 清单一致，type/overload 两份参数 evidence 一致 |
| 字段引用 / 普通字段名 / 集合名 / 输出名交叉负向矩阵 | PASS，另覆盖 local/foreign、graph 方向、缺 evidence/concept、scope 错误、getter/String 类型不兼容及 Bson body 角色 |
| `$group + accumulators` | 真实 Core/Driver BSON 对比 PASS |
| `$ifNull → $multiply → $project` | 真实 Core/Driver BSON 对比 PASS |
| `$cond → $project` | 真实 Core/Driver BSON 对比 PASS |
| `$dateToString → $project` | 真实 Core/Driver BSON 对比 PASS |
| `$lookup → $unwind → $group → $sort` | 真实 Core/Driver BSON 及 Stage 顺序对比 PASS |
| String / getter / Class / 子管道 / body / options 编码探针 | PASS，包括 unwind 不自动补/删美元前缀、Document overload、graph 两方向、嵌套输出路径、unset 单/多元素 |
| 与修改前 JSON 比较 | scanStatistics、Expression families、原 concepts 完全一致；其余变化仅参数 semantic 字段和新增 concepts |
| Surface | PIPELINE_STAGE=33；PIPELINE_EXPRESSION=49；methodFamilies=82；overload=297（Stage 140 + Expression 157） |
| 正式 CLI 连续生成两次 | SHA-256 完全一致，deterministic PASS |

本轮最终 SHA-256：`fe0dec3c14098d858bbbdcece1b6ba93012ba349d24cc35383baeabcac978f2c`。
正式输出：[mongo-plus-pipeline-api-index.json](target/generated-resources/mongo-plus-pipeline-api-index.json)。
完整变化比较：[verification.json](target/stage-audit/verification.json)。target 为本地生成目录，不作为源码提交内容。

验证边界：BSON 对比使用实际编译的 Core 和 Driver 5.4.0；没有连接数据库，不将这些结果描述为 MongoDB 服务端执行验证。
Stage 自测是 Index evidence 消费/拒绝测试，不是新建的生产 Resolver，也未改 Wrapper converter、Validator 或其他 Index 功能。

### 复跑

从仓库根目录执行；本机 JDK 21 在 `D:/Java/java21`，Core 使用 `D:/Java/jdk8`：

```powershell
$env:JAVA_HOME='D:/Java/java21'
mvn.cmd -pl mongo-plus-indexer -am '-Dgpg.skip=true' test-compile
$stageCp='mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes'
foreach ($stageTest in @('MongoPlusIndexerSelfTest','MongoPlusPipelineIndexerSelfTest','PipelineExpressionSemanticsSelfTest','PipelineExpressionCoverageSelfTest','PipelineStageSemanticsSelfTest')) {
    & "$env:JAVA_HOME/bin/java.exe" '-Dfile.encoding=UTF-8' -cp $stageCp "com.mongoplus.indexer.$stageTest" .
    if ($LASTEXITCODE -ne 0) { throw "$stageTest failed" }
}
& "$env:JAVA_HOME/bin/java.exe" -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain
```

独立编码探针放在 test resources，以便 Indexer 保持无 Core/Driver 运行时依赖：

```powershell
$env:JAVA_HOME='D:/Java/jdk8'
mvn.cmd -pl mongo-plus-core -am '-DskipTests' '-Dgpg.skip=true' compile
mvn.cmd -pl mongo-plus-core dependency:build-classpath '-Dmdep.outputFile=target/stage-audit-classpath.txt'
$stageCp='mongo-plus-core/target/classes;mongo-plus-annotation/target/classes;'+[IO.File]::ReadAllText((Join-Path $PWD 'mongo-plus-core/target/stage-audit-classpath.txt'))
New-Item -ItemType Directory -Force mongo-plus-indexer/target/stage-audit | Out-Null
& "$env:JAVA_HOME/bin/javac.exe" -encoding UTF-8 -cp $stageCp -d mongo-plus-indexer/target/stage-audit mongo-plus-indexer/src/test/resources/PipelineStageEncodingProbe.java
if ($LASTEXITCODE -ne 0) { throw 'Encoding probe compilation failed' }
& "$env:JAVA_HOME/bin/java.exe" '-Dfile.encoding=UTF-8' -cp ('mongo-plus-indexer/target/stage-audit;'+$stageCp) PipelineStageEncodingProbe
```
