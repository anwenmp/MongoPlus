# Stage Parameter Semantic Audit

审计日期：2026-09-07。基线来自本轮重新编译后生成的 Pipeline Index；保留工作区此前的 Expression 审计改动。

## 第一阶段：修改前完整清单

检查 **33 个 PIPELINE_STAGE MethodFamily、140 个 overload、291 个参数**。
这里的 33 是 MethodFamily 数量，并非 33 种不同 MongoDB Stage（projectDisplay/projectNone 等各占一个 family）。
无参 count() 计入 overload，参数数量为 0。

- [逐 overload 清单](src/test/resources/pipeline-stage-overload-audit.tsv)：包含全部 140 个声明，包括无参方法。
- [逐参数完整审计](src/test/resources/pipeline-stage-parameter-audit.tsv)：291 行，包含 Stage、family、完整 overload、parameter、Java type、修改前 semanticType、目标语义、scope、是否足够、是否需要 evidence、实现原因、原 Javadoc、声明类型和实现定位。
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

沿用 schemaVersion 1.1、三段式 `@mongoParam <parameter> <semanticType> VALUE|ELEMENT`、parameters、semanticScope、conceptRef、semanticEvidence 和 concepts。
原实现只接受 PIPELINE_EXPRESSION，并固定引用一个 expression concept，无法表示名字/字段引用/完整管道/Stage 内部文档。
只开放本次逐源码审计得到的有限语义词汇，不增加新的字段层级、扫描范围、反推规则或 Resolver 产品代码。

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
| STAGE_DOCUMENT | 6 | 由该 overload 的显式 mongoStages 外包一次的内部文档 |

新增 13 种 semanticType 和对应 13 个 concept；PIPELINE_EXPRESSION 复用。显式角色覆盖旧的通用分类，但 Java type/varargs 保留，getter 仍必须是 SFunction，Class 仍必须是 Class。
FIELD_NAME、LOCAL_FIELD_NAME、FOREIGN_FIELD_NAME 共享普通字段名的编码方式，concept 单独记录字段所属上下文。
这些概念不是新的 MongoDB 名称合法性校验器：名字槽中的美元字符不会被 Core 自动剥离或解释成引用；Resolver 必须先匹配调用者意图的语义角色，再应用表示证据。

65 个 NO_CHANGE 参数的逐项原因在完整清单中：专用 options/typed carrier、数值数量/方向、boolean、Wrapper/条件回调。
“无需标签”仅表示无需给该参数槽增加通用语义，不表示能凭 opaque 外部类型构造其所有内部配置。

## 验证记录

待第二阶段验证完成后填写。第一阶段未因方法名、description 或同名 overload 推导任何 Index evidence。
