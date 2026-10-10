# MongoPlus Indexer

## Pipeline 专用 Index

使用 `MongoPlusIndexerConfig.forPipelineProject(projectRoot)`，或执行：

```powershell
java -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain
```

输出 `mongo-plus-indexer/target/generated-resources/mongo-plus-pipeline-api-index.json`。
与 `MongoPlusIndexerMain` 一致，无参数时根据 `user.dir` 识别项目根目录或 `mongo-plus-indexer`
模块目录，可在 IDEA 中直接运行 main；仍支持可选的 `--project-root <目录>`。
该入口独立于原 Query CLI，复用 `SourceScanner`、`MongoPlusApiIndex` 和 `JsonWriter`。

### 最终 JSON 契约

沿用 schemaVersion `1.1`，版本字段使用既有 `mongoPlusVersion`。顶层字段为：

```text
schemaVersion, project, mongoPlusVersion, primaryScanModule, primaryPackages,
scanStatistics, wrappers, methodFamilies, types, specialTypes, concepts, entryType, expressionRoots
```

正式配置另输出 `constructionRoots`，记录显式 receiver/组合构造入口；扩展契约通过
`requiredCapabilities` 声明，均不赋予未标记的声明任何语义。

`entryType` 固定为 `com.mongoplus.aggregate.Aggregate`，`expressionRoots` 记录排序、去重后的表达式入口。
`primaryPackages` 仍表示 Stage 入口所属包，
不表示遍历该包。Pipeline 不支持 `generatedAt`。

`methodFamilies` 按 `apiCategory + name` 分组，category 为 `PIPELINE_STAGE` 或
`PIPELINE_EXPRESSION`；每族保留已有 description、mongoOperators、compositionSemantics、aliases、
overloads，并增加 `apiCategory`、`mongoStages`、`mongoExpressions`。
overload 保留原签名、返回类型、泛型、参数名/类型/语义/varargs、modifiers、annotations、deprecated、
declaredIn、availableIn，补充 name、parameterTypes 和 overload 自身的两类映射。
不同声明类型的相同签名不会折叠；未标记的同名重载不会借用其他重载的映射。

### 筛选与依赖边界

1. Stage 保持精确定位 Aggregate 及其源代码父类型；另精确定位下表六个 Expression/composition Root。
   `forPipelineProject` 配置这些默认根；手动 builder 可通过 `addExpressionRoot(qualifiedName)` 添加根。
   找不到已配置入口的公开源码类型时直接报错，不静默生成缺失的索引。
   只读取 Javadoc 自定义块标签 `@mongoStage`、
   `@mongoExpression`。类级标签适用于该类声明的方法，方法级标签用于对应方法。
   每个标签值必须完整匹配 `$` 加字母数字标识符，多个映射通过重复标签声明。
   不解析描述正文、方法名、包名或 MongoDB 能力列表。
2. Expression Root 进入同一依赖闭包，与已选择 Stage 方法的参数、返回值、泛型界限一起，
   按同包、显式 import、通配 import 精确定位源码类型。
   对可达依赖递归收集父类型、公开方法签名和 public 构造器参数；循环引用通过集合截断。
   依赖类型中有明确标签的方法可成为 expression/stage family。
   不沿方法体、普通 import、整个聚合包或任意 BSON 工厂反向搜索候选 API。
3. `types` 保留入口/父类型声明及可达依赖的 qualifiedName、kind、abstractType、泛型、annotations、
   imports、父类型、constructors、publicMethods 和枚举 constants。构造器包括可见性和 implicit，
   非 public 构造器仅作为不可直接调用的证据。父接口静态方法不作为 Aggregate 的继承方法。
   `wrappers` 保留模型字段，本入口不反向枚举所有实现类。
4. `SFunction`、`FieldChain` 的既有 specialTypes 仅在闭包引用它们时带入；nested-field concept
   仅随 FieldChain 带入。外部依赖没有本地源码时不编造构造器或方法；显式 import 的非 JDK
   外部引用记录在 `scanStatistics.externalTypeReferences`，imports 保留原始解析上下文。
   本扫描器使用源码语法树，不进行第三方 classpath 解析或方法体类型推导。
5. 文件路径、源码根、类型、方法族、overload、构造器、枚举常量、父类型、imports、modifiers、
   映射、availableIn 和引用集合均排序；参数及泛型参数保持 Java 声明顺序。

### Expression 入口选择

| 完整类型 | 面向用户的构造能力 | 收录 overload |
|---|---|---:|
| `com.mongoplus.aggregate.pipeline.Accumulators` | 构造 group/window 使用的累加表达式与 BsonField | 85 |
| `com.mongoplus.aggregate.pipeline.AggregateOperator` | concat、concatArrays、dateTrunc、双操作数 eq 等表达式 BSON | 8 |
| `com.mongoplus.aggregate.pipeline.Projections` | 已标记的 meta 投影表达式 | 9 |
| `com.mongoplus.aggregate.pipeline.Sorts` | 已标记的 metaTextScore 表达式 | 2 |
| `com.mongoplus.conditions.operation.ConditionOperators` | 条件、算术、日期等表达式 BSON | 54 |
| `com.mongoplus.toolkit.Filters` | expr 将表达式包装为查询 body；其他 Query 方法没有 Pipeline 映射 | 1 |

`conditions.interfaces.ConditionOperators` 实际也是 class，并非接口声明；它没有与现行类建立
extends/implements 关系，而是带 `@Deprecated`、Javadoc 指向 operation 包的旧工具类副本。
因此只选现行入口，旧包的 54 条标签不形成重复 evidence。不按简单类名或方法签名跨类型折叠，
例如 Accumulators.sum 与 ConditionOperators.sum 的真实不同调用证据仍保留。
根列表及闭包工作集去重，某根同时被签名引用时不会重复收录。

2026-09-07 正式生成：33 个 stage family（140 个 overload）、49 个 expression family
（157 个 overload，42 种显式 Expression 映射），methodFamilies 合计 82、overload 合计 297。
主动解析 7 个类型（Aggregate、父接口 Project、5 个 Expression Root），依赖解析 89 个类型。
未遍历整个项目，也未从方法体、返回同一 BSON 类型的工厂或内部实现类反向扩展入口。
详细映射和保守排除原则见 [聚合源码 evidence](../docs/ai/architecture/AGGREGATION.md#pipeline-javadoc-evidence)。

### Pipeline 表达式参数 evidence

只有方法 Javadoc 显式声明 `@mongoParam <参数名> PIPELINE_EXPRESSION VALUE|ELEMENT`，才为对应
参数写入聚合表达式语义。`VALUE` 表示整个值，`ELEMENT` 表示数组、varargs 或集合的每个元素；参数必须存在，
作用范围必须匹配，非法标记报错。类标签、泛型名 `TExpression`、参数名、描述、同名重载均不传播语义。
此标记不参与 Stage/Expression 方法筛选，不改变 Query 分类器或扫描闭包。

例如 `Accumulators.sum(String, TExpression)` 的第二个参数：

```json
{
  "name": "expression",
  "type": "TExpression",
  "semanticType": "PIPELINE_EXPRESSION",
  "semanticScope": "VALUE",
  "conceptRef": "PIPELINE_EXPRESSION_FIELD_REFERENCE",
  "semanticEvidence": {
    "source": "JAVADOC",
    "tag": "mongoParam",
    "value": "expression PIPELINE_EXPRESSION VALUE"
  }
}
```

沿用 `parameters` 和顶层 `concepts`，不增加 schema 层级。`declaredIn`、signature 和参数名定位
标记的真实源码声明；同一参数在 `types.publicMethods` 与 overload 中具有一致 evidence。
只有收录的公开方法存在参数标记，才输出 `PIPELINE_EXPRESSION_FIELD_REFERENCE` concept：

- `fieldReference`：Java `String`，前缀 `$`，排除 `$$`，`encoding=UNCHANGED`。
- `variableReference`：`semanticType=VARIABLE_REFERENCE`，前缀 `$$`，原样编码；正式拆出
  `referenceName` / `accessPath`。源码例子 `$$SEARCH_META` 仅证明表示，不声明 system variable。
  有显式作用域契约时关联 `VARIABLE_BINDING_SCOPE_V1`，引用使用点必须按该契约校验绑定。
- `plainStringValue`：不以 `$` 开头的字符串原样写入，不自动加字段前缀；不自动 literal 转义。
  `$` 开头的字符串如何强制表示 literal，记录为 `NOT_ESTABLISHED`，不虚构专用 `$literal` API。
- `sourceEvidence`：记录源码路径/符号及 Driver 5.4.0、默认 StringCodec 的实际编码依据。
- `literalValue`：`LITERAL/RUNTIME_CODEC`，需要参数 Java 类型兼容且具备实际 codec 证据；
  String 解释继续引用 `plainStringValue`。不因 Object 参数就承诺任意自定义值有 codec。
- `nestedExpression`：`PIPELINE_EXPRESSION` 的 Bson/document 表示，需要 Java 类型兼容以及
  子调用自身的正式 result semantic evidence；`STAGE_BODY_DOCUMENT` 不能充当 expression。

### Pipeline 表达式 shape evidence

只有当前方法 Javadoc 显式声明 `@mongoExpressionShape OBJECT|ARRAY`，才在对应 overload 写入
`expressionShape`。该字段不提升到 MethodFamily，也不从 Java 方法名、描述、参数类型或同名 overload
传播。当前 `$cond` 的四个实测声明分别为 `OBJECT`、`OBJECT`、`ARRAY`、`OBJECT`；其中动态参数版
`condArray(String, Collection, Object, Object)` 实际委托对象形式，因此不能按方法名标成 `ARRAY`。
  这是表示和传递契约，不保证任意表达式都符合具体操作符的服务端类型/作用域要求。

### Expression composition evidence

2026-10-08：复用既有 `mongoParam`、`mongoExpressionShape`、`mongoComposition` 和
`resultSemanticEvidence`，没有新增 composition DSL、schema 或 capability。

- `AggregateOperator.eq(Object left, Object right)` 按 `Arrays.asList(left, right)` 保持两个操作数顺序。
  两个参数分别声明 `PIPELINE_EXPRESSION VALUE`，shape 为 `ARRAY`，组合关系为
  `PIPELINE_EXPRESSION + PIPELINE_EXPRESSION -> PIPELINE_EXPRESSION`，返回 Java `Bson`。
- 既有 `Filters.expr(TExpression expression)` 原样编码 expression 到 `$expr` 值槽；参数为
  `PIPELINE_EXPRESSION VALUE`，组合关系为 `PIPELINE_EXPRESSION -> STAGE_BODY_DOCUMENT`，返回 `Bson`。
  `$expr` 在这里是查询 body 的组合映射；消费者必须使用 `resultSemanticType` 区分，不能只根据
  `apiCategory` 或 `Bson` 将其作为另一个 expression 的 operand。
- `Aggregate.match(Bson bson)` 消费 `STAGE_BODY_DOCUMENT VALUE`，固定包裹 `$match`，并通过现有
  `pipelineEffect=APPEND_STAGE` 契约追加一个 Stage；完整 Stage 不能再次作为 body 传入。
- `"$_id"` 复用 `fieldReference`（`$` 且排除 `$$`），`"$$userId"` 复用 `variableReference`。
  后者的 binding 继续由 `VARIABLE_BINDING_SCOPE_V1` 和调用处完整环境证据提供，eq 不声明变量。
  普通字符串、Integer literal 与 nested Bson 沿现有 codec 机制进入 Object 参数；嵌套调用仍须拥有
  自身 `PIPELINE_EXPRESSION` result evidence，美元前缀 literal 的转义能力仍未建立。

真实 API 链：

```java
Bson expression = AggregateOperator.eq("$_id", "$$userId");
Bson body = Filters.expr(expression);
Aggregate<?> aggregate = new AggregateWrapper();
aggregate.match(body);
```

补齐前，正式 Index 没有 `$eq`/`$expr` overload 映射；QueryWrapper/Filters 的查询 eq 不构造
`{$eq:[left,right]}`，不得借用为表达式。新增一个公开 eq 方法与一个明确 Filters 根，其他查询方法
保持无映射。当前正式 surface：33 Stage families/140 overloads，51 expression/composition
families/159 overloads，总计 84 families/299 overloads。

验证入口：`ExpressionCompositionEvidenceSelfTest` 从正式 Index 验证 E01～E03、operand 顺序和
Java 类型、嵌套结果、未绑定变量拒绝、缺失证据负例与无关名称夹具；变量绑定复用既有 scope 测试
consumer。`ExpressionCompositionEncodingTest` 验证真实 Java/BSON，包括 Int32、两侧嵌套 Bson、
match 单次追加及 receiver 顺序。未执行 MongoDB 服务端求值或 MCP/AI 消费者验证。

第一轮审计及标记范围：

| 源码 | 已标记参数 | 数量 |
|---|---|---:|
| `Aggregate` | 两个泛型 group.id、String group._id、replaceRoot/replaceWith.fieldName、两个 setWindowFields.partitionBy | 7 |
| `Accumulators` | sum/avg/first/last/max/min/push/addToSet/mergeObjects/stdDevPop/stdDevSamp 的两个泛型 expression 重载 | 22 |
| 现行 `ConditionOperators` | toDate/toBool/toDecimal/toDouble/toInt/toLong/toObjectId/toString.expression、两个 substrBytes.expression、sum/add 的 varargs 元素 | 12 |
| `Projections` | 两个 computed.expression，仅补 types.publicMethods evidence | 2 |

合计 43 个源码参数，其中 41 个出现在 methodFamilies，2 个仅在既有 type evidence 中。
所有 `@mongoStage`、`@mongoExpression` 保持原样，仍为 33 Stage family + 49 Expression family、297 overload。

审计边界：`Accumulators.count` 的泛型入参确实原样编码，但省略 expression 的便利方法使用空 Document；
当前证据不足以确认它接受任意字段引用表达式，故不赋予这条参数语义，原方法映射仍保留。
`lookup` 的 `List<Variable<TExpression>>` 是变量定义列表，不是可直接传字符串的参数。
`Field`/`Variable` 是承载值的构造类型，`SimpleExpression` 是编码实现，`FillField.value` 是 fill 输出配置；
检查其传值机制不意味着新增 root/family 或改动构造器 evidence。

第二轮检查全部 49 个 Expression family、157 个 overload、372 个参数，在 74 个 overload
中新增 127 个 `@mongoParam`（100 VALUE、27 ELEMENT）。Core 仅修改 Accumulators（61）、
现行 ConditionOperators（51）、AggregateOperator（15）的 Javadoc。完整逐参数决定、排除原因与
验证结果见 [Expression 参数审计](EXPRESSION_PARAMETER_AUDIT.md) 及其链接的 TSV 清单。
现有泛型、Object、String、Number、List/Collection 和数组各自保留 Java 类型限制；标记不使
`Number` 接受字段引用字符串，不使 `List<String>` 接受 Bson，不使 `concatArrays(List<?>...)`
接受 String/Bson 作为外层 vararg。Lambda 仍是 getter 参数，不标记为完整 expression value。

Indexer 仅补足既有范围校验：ELEMENT 允许数组以及明确的 `java.util.List`/`Collection`；
普通数组/集合可按实际含义声明 VALUE，varargs 保持 ELEMENT。只检查已声明的标签，不推断标签。
除参数 evidence 外，JSON 仅同步 concept 中 ELEMENT 的描述；schema、扫描范围、映射和 Stage
evidence 不变。全部源码参数标签合计 170 个，其中 methodFamilies 为 168 个（Expression 161、Stage 7），
另 2 个 computed 参数仅在 types.publicMethods；本轮 127 个标记在 overload/type 中各出现一次。

### Stage 参数审计

完成 33 个 Stage family、140 个 overload、291 个参数的逐源码审计：219 个参数补充 evidence，
7 个已有 expression evidence 保留，65 个专用类型/标量/条件回调参数无需新增标签。
另补可达 UnwindOption 的两个 includeArrayIndex setter 参数，不增加 Stage/Expression 入口。
完整清单、逐项原因、13 种新增语义及 15 个 concept、验证命令见 [Stage 参数审计](STAGE_PARAMETER_AUDIT.md)。

复用 `@mongoParam <parameter> <semanticType> VALUE|ELEMENT [conceptRef]`。
仅逐方法显式标签生效；可选第四段只允许选择与语义匹配的已审计 concept。
例如 graphLookup 的 connectFromField/connectToField 共享 FOREIGN_FIELD_NAME，分别显式引用
两个记录遍历方向的 concept。其他 Stage 标签使用默认 concept；原 PIPELINE_EXPRESSION 三段式不变。
名字、字段引用、子管道、内部 Stage 文档和排序文档的表示各自记录，不按 String/Bson 或同名方法继承。

Core 仅增加 Javadoc 参数标签；Indexer 没有扩大扫描范围或修改 API 映射。
正式 Index 仍为 schemaVersion 1.1、33 Stage + 49 Expression、82 families、297 overload。
Expression evidence 和原 concepts 保持不变。参数语义不放宽 Java 类型，也不承诺外部 opaque options 的构造能力。

### Stage body 元素与文档归约 evidence

`@mongoParam projections STAGE_BODY_DOCUMENT ELEMENT` 仅声明元素角色。Indexer 使用 Java Compiler
Tree API 校验一层数组/varargs 或 `java.util.List` 的元素；接受 `Bson`、List 的 `? extends Bson`
上界，并核对 import。无界/下界通配符、原始 List、嵌套容器均拒绝并说明原因。
新 Concept 为 `PIPELINE_PARAMETER_STAGE_BODY_DOCUMENT_ELEMENT`，不携带 Stage 包装或合并行为；
既有 VALUE Concept 和其他 API evidence 保持不变。

两个 `Projections.fields` overload 独立声明：

```java
@mongoParam projections STAGE_BODY_DOCUMENT ELEMENT
@mongoReduction projections -> STAGE_BODY_DOCUMENT operation=DOCUMENT_MERGE order=INPUT duplicateKeys=LAST_WINS depth=SHALLOW empty=EMPTY_DOCUMENT
```

`mongoReduction` 是独立标签，不扩展 `mongoComposition`。结果输出 `resultSemanticType`、
`reductionContract`、`resultSemanticEvidence` 和 `reductionEvidence`；输入语义只取同一方法的
显式参数标签。属性缺失/未知/重复、参数缺失、作用域或输入输出语义不合法均导致生成失败。
DOCUMENT_MERGE 当前契约为按输入顺序浅合并、同名键最后覆盖、空输入得到空文档。

只有输出中实际包含归约 evidence，顶层才增加 `requiredCapabilities: ["DOCUMENT_REDUCTION_V1"]`。
该批次不提供固定值契约，也不表示 Resolver 已支持归约调用树。新增独立自测：

```powershell
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineReductionEvidenceSelfTest .
```

### Stage 对象字段绑定 evidence

拆分参数构造 `{$stage:{field:value}}` 时，逐方法声明参数语义、绑定及来源，例如：

```java
@mongoParam size INTEGER_VALUE VALUE
@mongoObjectField size field=size encoding=INT32_EXACT minimum=1 maximum=2147483647
@mongoObjectFieldSource size path=... symbols=... mechanism=...
@mongoObjectFieldSource size artifact=... symbols=... mechanism=...
```

`mongoObjectField` 只统一要求 `field`；它证明对象字段对应哪个方法参数，编码约束由参数语义决定。
`INTEGER_VALUE VALUE` 仍必须同时声明 `encoding=INT32_EXACT`、minimum、maximum，上下界须为
Int32 范围内的精确整数且 minimum <= maximum。范围从标签读取，生成器不固定为正数；
`sample` 的正数下界由 MongoDB size 语义声明。`COLLECTION_NAME VALUE`、`PIPELINE VALUE` 等
非整数绑定只声明字段，不伪造整数编码/范围。每个绑定至少有一个同参数的来源，来源标签格式为参数名、
`path=` 或 `artifact=`、`symbols=`、`mechanism=`（最后的说明可含空格）。标签只在当前
方法生效；类、相邻 overload、方法名、字段名和描述均不能补全契约。

方法必须有唯一 Stage 映射，普通绑定参数必须显式声明 VALUE 语义及匹配 concept。
DOCUMENT entries 绑定可使用 ELEMENT，但必须同时有完整、已校验的 entryConstruction 和
typedContainerConstruction；INTEGER_VALUE 仍只接受 VALUE。
一个方法可绑定多个参数到不同单层字段；字段标识符为 `[A-Za-z_][A-Za-z0-9_]*`。
重复参数/字段/属性、未知或缺失属性、来源缺失/无对应绑定、错误语义/Java 表示均拒绝生成。
Java 数值标量仅校验表示兼容，不据此产生语义；import 或本地同名 Number/Integer 不冒充 JDK 类型。

输出复用 `parameters[].objectFieldBinding`（field、sourceEvidence，以及语义要求的 encoding、minimum、maximum）
和既有 semantic concept，两个 Index 视图通过同一个 evidence 方法生成。
只有存在实际绑定时才声明 `STAGE_OBJECT_FIELD_BINDING_V1`；保留既有 `DOCUMENT_REDUCTION_V1`。
schemaVersion 仍为 1.1，能力通过 requiredCapabilities 揭示，不增加扫描入口或 Stage 映射。

Core `sample(Number)` 仍调用 `Aggregates.sample(size.intValue())`，本次只修改 Javadoc。
新 evidence 仅承诺 1..2147483647 的精确整数可绑定为 BSON Int32；小数、超界、非精确转换、
自定义 Number 以及原 BSON Int64/Double/Decimal128 的类型等价性均未声明。Core 不新增运行时校验。
测试覆盖 1、5、100、2147483647 的实际 BSON、截断/溢出反例、删除真实源码标签后验收失败、
不同名称/Stage/范围、多个拆分字段、类标签/相邻重载隔离、非法契约及确定性生成。

```powershell
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineObjectFieldBindingSelfTest .
```

### Stage body 标量与 singleton evidence（P0-02）

复用 `mongoParam` 的 `INTEGER_VALUE VALUE`、`FIELD_NAME ELEMENT`、`PIPELINE_EXPRESSION VALUE`。
新增 `STAGE_VALUE_BINDING_V1` 只描述整个 Stage body 到单个参数的显式值绑定；
原 `mongoObjectField` 仍只描述对象内部字段，两个契约不互相猜测或覆盖。

```text
@mongoStageValue <parameter> encoding=INT32_EXACT minimum=<整数> maximum=<整数>
@mongoStageValue <parameter> encoding=UNCHANGED prefix=<前缀> minimumLength=<非负整数>
@mongoStageValue <parameter> encoding=SINGLETON_SCALAR_ELSE_ARRAY minimumSize=<非负整数> duplicates=REJECT|PRESERVE singleton=VALUE_TO_ELEMENT|FORBID
@mongoStageValueSource <parameter> path|artifact|reference=<位置> symbols=<符号> mechanism=<真实机制>
```

输出参数 `stageValueBinding`；ELEMENT 额外输出 `elementContainerBinding`，记录真实数组/varargs/List、
一层 AST 的 String 元素、调用形式、有序收集及显式 `singletonLifting`。没有 lifting 标签时不能
把 VALUE 当 ELEMENT。复用已有 AST 容器解析与 Int32 编码/范围校验，不引入 Stage 名称分支。
缺失/重复/未知属性、错误语义、原始或嵌套 List、无界或下界通配符、同名外部 String/List、
缺失语义/来源或矛盾绑定都会拒绝生成。capability 仅在实际使用时输出，两个 Index 视图一致。
候选保留逐 overload 的真实 Java 类型与编码；排列顺序不是选择 evidence。

- `skip(int)` 和 `skip(long)` 都绑定精确整数 0..2147483647，输出 BSON Int32；long 经
  `Math.toIntExact` 委托 int。MongoDB 8.0 接受非负 Int64，但 Core 超过 Int32 时抛异常。
  约束由绑定消费者执行，Core 仍能编码负数；不承诺保留输入 BSON Int64/Double/Decimal128 类型。
- `unset(String...)` 和 `unset(List<String>)` 将字符串标量显式提升为一个元素，将字符串数组
  收集为真实目标容器；一个元素输出字符串，其他数量输出数组。合法绑定要求非空、字符串元素、
  无重复；Core 仍能编码空数组和重复字段，MongoDB 拒绝。字段路径及父子路径冲突需服务端校验。
  getter overload 保留 FIELD_NAME/effect，字符串不会被自动构造成 getter；任意 Bson 透传仍未映射。
- `sortByCount(String)` 复用原字符串表达式语义和原值编码；显式要求 `$` 前缀及至少两个字符。
  `$` 字段路径和 `$$` 变量仍须原 Expression/Variable evidence 验证；不承诺对象表达式能绑定到 String。

receiver effect 独立于参数绑定；删任一证据都不能由另一证据恢复。补齐 unset 四个及 sortByCount
两个真实追加 overload 的 effect，沿用现有 `PIPELINE_CONSTRUCTION_V1` 的 factory/representation/extraction。
完整审计、实际验证、SHA 和 MCP 接入边界见 [P0-02 报告](p0-02-scalar-singleton-report.md)。

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineScalarSingletonEvidenceSelfTest .
```

### Nested PIPELINE 构造 evidence

正式配置增加两个 construction roots：`AggregateWrapper`、`com.mongoplus.aggregate.pipeline.Facet`。
手动 builder 使用 `addConstructionRoot(qualifiedName)`；与 Expression roots 共用签名依赖闭包，
不沿方法体或 Javadoc 链接反向扫描。构造器开始复用方法的 `mongoParam` / `mongoComposition` 提取。

```text
@mongoPipelineFactory receiver=NEW initial=EMPTY ownership=INDEPENDENT
@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
@mongoPipelineRepresentation source=RECEIVER semanticType=PIPELINE order=CALL_ORDER access=LIVE_VIEW
@mongoPipelineInput <parameter> extractor=<qualified-type>#<no-arg-method>()
@mongoComposition OUTPUT_FIELD_NAME + PIPELINE -> NAMED_PIPELINE
@mongoParam <entries> NAMED_PIPELINE ELEMENT
@mongoPipelineContainer <entries> operation=NAMED_PIPELINES result=STAGE_BODY_DOCUMENT order=INPUT invocation=SINGLE
```

分别输出 `pipelineFactory`、`pipelineEffect`、`pipelineRepresentation`、参数 `pipelineExtraction`、
既有结果语义及 `pipelineContainer`，每项保留当前声明的 `sourceEvidence`。复用 `PIPELINE` 与
`OUTPUT_FIELD_NAME`；`NAMED_PIPELINE` 只描述名称和完整管道的条目，不以 Java 类名定义语义。
ELEMENT 用 Compiler Tree 校验单层数组/varargs/List，记录 `elementJavaType`；外部元素要求完整类型
或显式 import。extractor 必须在正式闭包内具有显式 representation；缺失或矛盾契约拒绝生成。
effect/container 要求唯一显式 Stage 映射；不根据 Stage 名称分支，不传播类级或相邻 overload 标签。
能力为 `PIPELINE_CONSTRUCTION_V1`，消费者需实现后再接受该能力。

2026-10-08（P0-01）：逐 overload 审计 58 个常见 Stage 入口，为已有独立 Stage 映射且正常返回时
向当前 receiver 追加一次的 47 个声明补充 `mongoPipelineEffect`：project（含 Project 父接口）13、
group 6、count 3、unwind 4、addFields/set 各 6、skip 2、sample 1、replaceRoot/replaceWith 各 3。
不扩展契约、Stage 映射或 nested planner。五个任意 BSON 透传入口不能承诺具体单个 Stage；
六个未标记的 addFields/set Object/Collection overload 虽确认单次追加，但缺少各自的 Stage 映射，
现有 effect 契约不准入，也不借用相邻声明的标签。
逐声明的真实 Core 委托链及剩余边界见 [审计表](src/test/resources/pipeline-stage-effect-audit.tsv)，
本轮验证、冻结 SHA 和 MCP 复用验证边界见 [P0-01 报告](p0-01-stage-effect-report.md)。
effect 仅证明 receiver 状态变化；skip 编码/范围、projection 模式、Options、Field/BsonField entry
及 expression composition 仍需独立闭合。`PipelineStageEffectEvidenceSelfTest` 验证两个正式视图、
三个外层的 141 个 effect 组合及逐标签删除后的 141 个拒绝；Core `CommonStageEffectTest`
真实执行全部 47 overload，并比较 facet/lookup/unionWith 的顺序、隔离、追加次数及 BSON。

```powershell
mvn -pl mongo-plus-indexer -am -DskipTests test-compile
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineStageEffectEvidenceSelfTest .
mvn -pl mongo-plus-core -am '-Dtest=CommonStageEffectTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
java -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

`limit(int)`、`sort(String,Integer)` 和两个 entry 容器 overload 声明追加 effect。
真实构造链为独立 `AggregateWrapper` → receiver Stage 调用 → `Facet(String,Aggregate<?>)`
→ 一次 `facet(Facet...)`（另一个 List 容器也有 evidence）。

2026-10-01：六个正式 `unionWith` Stage overload 也逐声明追加 effect；四个双参数 overload
用 `mongoObjectField` 显式绑定 `coll` 到 collectionName/collection、`pipeline` 到 aggregate，
来源为当前 Core 委托及 Driver 5.4.0 `UnionWithStage.toBsonDocument` 的字段写入。
两个 `Aggregate<?>` overload 用同一 `mongoPipelineInput` 引用
`com.mongoplus.aggregate.Aggregate#getAggregateConditionList()`，完整路径为独立 receiver
→ sort/limit → ordered PIPELINE representation → Aggregate 参数 → 外层追加一个 Stage。
String 集合名用既有 `UNCHANGED` 表示；Class 参数用 `ANNOTATION_OPERATE_COLLECTION_NAME`，
需要 Java Class 值，Mongo 字符串不能直接供给。单参数 overload 是简化字符串 Stage，故不声明对象字段绑定。
List 参数保留完整有序 Bson 列表的消费证据，不声明 receiver extractor 或新增 Stage→Bson 构造能力。
不新增 Stage 专用契约或 planner，复用 `PIPELINE_CONSTRUCTION_V1` / `STAGE_OBJECT_FIELD_BINDING_V1`。

最小验证入口：`PipelineConstructionEvidenceSelfTest`（完整链、真实源码逐标签删除失败、
无关 API 名称夹具、非法契约、两次生成稳定）及 Core `FacetNestedPipelineTest`（仅两个目标 BSON）。

`PipelineUnionWithEvidenceSelfTest` 验收 U02/U03、String/Class Java 表示和六个追加 effect，
删除真实 coll/pipeline binding、extractor、factory、representation 或目标 effect 后同一验收失败；
Core `UnionWithNestedPipelineTest` 只验证 U02/U03 的 BSON。绑定泛化回归继续使用
`PipelineObjectFieldBindingSelfTest`，包括 `$sample` 所有严格整数负例及无关名称的非整数绑定。

2026-10-07：仅 `lookup(String,Aggregate<?>,String)` 补充三个通用对象字段绑定：
from→from（`COLLECTION_NAME`）、pipeline→aggregate（`PIPELINE`）、as→as（既有
`OUTPUT_FIELD_NAME`）。逐参数来源记录真实 Core 委托和 Driver 5.4.0 `LookupStage.toBsonDocument`；
Aggregate 参数引用同一 `getAggregateConditionList()` extractor，Stage 声明
`APPEND_STAGE/RECEIVER/ONE/CALL_ORDER`。复用既有独立 factory、ordered representation 和
sort/limit effect，不改通用提取器、不新增 capability，也不向其他 overload 传播标签。
不涉及 letList、变量或表达式。`PipelineLookupEvidenceSelfTest` 只验收 limit(1) 与
sort(createTime,-1)→limit(1) 两个输入的 evidence 链，`LookupNestedPipelineTest` 比较同两项实际 BSON。

```powershell
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineLookupEvidenceSelfTest .
mvn -pl mongo-plus-core -am '-Dtest=LookupNestedPipelineTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

```powershell
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineUnionWithEvidenceSelfTest .
mvn -pl mongo-plus-core -am '-Dtest=UnionWithNestedPipelineTest,SampleInt32EncodingTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
```

### 通用 entry / typed-container construction evidence

`ENTRY_CONTAINER_CONSTRUCTION_V1` 将构造单个 entry 与收集 typed List 分开表达，
不借用 `NAMED_PIPELINES`，也不根据方法、字段、Stage 或类名称生成语义。
当前支持单层 `List<E<G>>`，E 是真实 artifact 中具有一个 Object 上界类型参数的公开具体类，
构造器有两个参数（String key 与泛型 value，位置由标签指定）。G 可为无界类型变量或显式引用类型；
raw 类型、wildcard、嵌套容器、受限上界和以子类 List 替代父类 List 均拒绝生成。

```text
@mongoParam <entries> VARIABLE_DEFINITION ELEMENT
@mongoEntryConstruction <entries> constructor=<qualified-type> artifact=<group:artifact:version> key=0 value=1 keySemantic=VARIABLE_NAME valueSemantic=PIPELINE_EXPRESSION result=VARIABLE_DEFINITION
@mongoEntryConstructionSource <entries> artifact=<group:artifact:version> symbols=<audited-symbols> mechanism=<source-audit>
@mongoTypedContainer <entries> input=DOCUMENT_ENTRIES order=INPUT target=java.util.List
@mongoObjectField <entries> field=<document-field>
@mongoObjectFieldSource <entries> path|artifact=<source> symbols=<consumer> mechanism=<field-write>
```

三项 construction 标签必须逐参数配对，且结果匹配当前声明的 ELEMENT 语义及 concept。
`entryConstruction.constructor` 输出真实公开签名、类型参数与上界、key/value 参数位置和语义、
`resultSemanticType`、构造结果 Java 类型及 `genericBindings`。名称原值使用 `VARIABLE_NAME`；
表达式值引用既有 `PIPELINE_EXPRESSION_FIELD_REFERENCE`，不更改 `$` / 排除 `$$` 的字段引用表示。
key/value 的语义来自显式标签及逐声明来源，Java 类型仅用于验证，不能赋予 Mongo 语义。

`typedContainerConstruction` 输出 `elementType`、`genericArgument`、`targetContainerType`、
`elementAssignability`、`containerAssignability`、`genericArgumentInference`、
`ORDERED_ENTRIES / INPUT / inputOrderPreserved=true` 及 entry construction 的正式引用。
泛型使用结构化 PARAMETERIZED / TYPE_VARIABLE / REFERENCE 类型；构造器类型参数绑定到目标实参，
全部 entry 共用同一可赋值实参。类型变量从全部 value 推断公共可赋值类型；具体实参只检查 value
可赋值，不重新推断。`valueAssignability` 要求消费者在构造前校验值。单元素及最终 List 都以
替换后的类型一致性证明；List 泛型明确为 INVARIANT，不以擦除或 unchecked 转换冒充兼容。

目标 `lookup(String,List<Driver.Variable<TExpression>>,Aggregate<?>,String)` 独立声明四个对象字段绑定、
PIPELINE extractor 与追加 effect。entry 直接使用 Driver `Variable(String,TExpression)`，
不构造 MongoPlus 子类。生成时从显式 Maven 坐标精确读取 JAR，用 JDK reflection 校验泛型签名，
并验证 JAR 版本身份（pom.properties 或标准 manifest 版本）；无 artifact 或签名不符即拒绝生成。
本地仓库按以下优先级定位：builder 显式配置 `constructionArtifactRepository(Path)`、
`maven.repo.local` JVM 系统属性、用户 `${user.home}/.m2/settings.xml` 的 `localRepository`、
Maven 安装目录 `conf/settings.xml` 的 `localRepository`，最后回退到用户 `.m2/repository`。
Maven 安装目录依次读取 `maven.home` 系统属性、`MAVEN_HOME`、`M2_HOME` 环境变量；
settings 中支持系统属性与 `env.*` 插值，禁止 DTD/外部实体，不读取 profile 属性作为路径。
这与 [Maven settings 的用户/全局优先级及插值规则](https://maven.apache.org/settings.html) 一致。
IDEA 中自定义的 settings 路径不会自动传给普通 Java main；若它不在上述位置，可在 VM options
添加 `-Dmaven.repo.local=<实际仓库目录>`。例如本机仓库为 `D:\repository` 时使用
`-Dmaven.repo.local=D:\repository`。Indexer 不扫描仓库、不下载依赖，仍无第三方编译依赖。

`VariableEntryConstructionSelfTest` 覆盖单/多 entry 的 evidence 驱动 Java 生成及
`-Xlint:unchecked -Werror` 编译、实际名称/值顺序、泛型改名、具体 String/Object 实参、
无关 API/字段及独立 artifact 夹具、删标签和非法契约拒绝、相邻 overload/类标签隔离和生成稳定性。
Core `LookupVariableEntriesTest` 比较同两项实际 BSON，并显式断言 `userId → orderId` 顺序。
变量身份、作用域和引用绑定由下节的独立 `VARIABLE_BINDING_SCOPE_V1` 提供。

```powershell
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MavenLocalRepositorySelfTest
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.VariableEntryConstructionSelfTest .
mvn -pl mongo-plus-core -am '-Dtest=LookupVariableEntriesTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
java -cp 'mongo-plus-indexer/target/classes' com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

### 通用变量声明、作用域与绑定 evidence

`VARIABLE_BINDING_SCOPE_V1` 连接 `VARIABLE_DEFINITION → declaration identity → scope →
VARIABLE_REFERENCE → binding`，复用现有 entry/container、`VARIABLE_NAME` 及表达式中的
FIELD_REFERENCE / VARIABLE_REFERENCE representation。绑定执行由消费者根据输入结构完成；
Index 的 `bindingValidated=false` 表示静态 Index 没有替某一次输入执行绑定，
`bindingValidation=REQUIRED_AT_REFERENCE_USE` 明确要求引用使用点校验，`$$` 前缀不能证明已绑定。

逐方法声明（不传播类标签或相邻 overload 标签）：

```text
@mongoVariableScope declarations=<entry参数> body=<PIPELINE参数> parent=ENCLOSING initializer=PARENT inheritance=LEXICAL shadowing=NEAREST exit=RESTORE_PARENT
@mongoVariableEnvironment <PIPELINE参数> source=ENCLOSING inheritance=LEXICAL exit=RESTORE_PARENT
@mongoVariableScopeSource reference=<绝对URI> symbols=<来源符号> mechanism=<已核对的语义>
```

scope 输出方法 `variableScope`，声明参数 `variableDeclaration` 与 body 参数 `variableEnvironment`
通过同一个 `scopeRef` 关联。声明参数必须已有 ELEMENT `VARIABLE_DEFINITION`、有序
entry/container construction，且 ENTRY_KEY 已显式声明 `VARIABLE_NAME`；声明和 body 都必须已有
独立 objectFieldBinding。缺参数、缺依赖、重复 scope、未知属性或矛盾环境拒绝生成。
`mongoVariableEnvironment` 单独描述无本地声明的 body 继承边；同一个 body 不能同时指定新 scope
与直接继承环境。该标签也必须有独立 PIPELINE 参数、字段绑定和语义来源。

`declarationName.source=ENTRY_KEY`，名称原值精确比较；`declarationIdentity` 为结构化 tuple：
`{apiRef, ownerNodePath, declarationParameter, entryOrdinal}`。ownerNodePath 使用输入 owner 节点的
RFC 6901 JSON Pointer，entryOrdinal 使用 ORDERED_ENTRIES 的零基序号；相同输入身份稳定，
不同 owner 的同名声明身份不同，不以名称、Java 对象地址、生成时间或随机值作为身份。
`scopeIdentity` 使用前三个组件。entry/container 与身份规则均指向同一正式 construction evidence。

`variableReference.nameExtraction` 保留编码原值，移除 `$$` 后按第一个 `.` 拆分：
`$$userId → referenceName=userId, accessPath=null`；
`$$userId.name → referenceName=userId, accessPath=name`；更深的路径完整保留，不参与名称查找。
不 trim；空名称或空 accessPath 得到 `INVALID_VARIABLE_REFERENCE`。

`variableScope` 正式记录 declarationOwner、body、parentScope、initializerEnvironment、inheritance、
shadowing、scopeExit 及来源。initializer 使用 parent scope，当前全部声明对同级 initializer 均不可见；
body 使用新 declaration scope；查找顺序为最近环境再逐 parent 环境；退出恢复 parent，不导出到 outer/sibling。
无声明 body 的显式 environment 边继承同一词法链；每条 nested environment 边都必须有证据，
未声明的边不能根据方法名或 PIPELINE 类型自行补全。

当前目标 let lookup 与无 let lookup 的来源是已核对的 MongoDB r8.0.0
`DocumentSourceLookUp`（initializer 从父 parseState 解析，body 使用独立复制的变量环境）以及
`VariablesParseState.defineVariable/getVariable`（当前名称映射定位声明 ID），另附官方 lookup 语义说明。
Core 只新增 Javadoc metadata，公开 Java 签名和执行逻辑保持原行为。

顶层 `concepts[id=VARIABLE_BINDING_SCOPE_V1]` 声明环境/结果契约。消费者必须提供
`scopeGraphComplete`、`declarationsComplete`、`externalCatalogComplete`、`systemCatalogComplete`
及每项正式来源。external/system 目录仅接受显式 `{declarationName, declarationIdentity, sourceEvidence}`
条目；没有隐式 system 名单，空目录也必须有完整性证据。
最近可见匹配返回 `BOUND_VARIABLE + declarationIdentity`；完整 scope 链和全部目录均无匹配才返回
`UNBOUND_VARIABLE`；证据不完整返回 `INCOMPLETE_VARIABLE_ENVIRONMENT`，不能把缺证据当未绑定。

`VariableBindingScopeSelfTest` 消费正式 Index，覆盖正常绑定、accessPath、`$$missing`、同名 shadowing
及退出恢复、父 initializer、自身/同级声明隔离、outer/sibling 不可见、无声明 nested body 的显式继承、
四种完整性缺失及来源缺失、external/system 显式目录、无关 API/字段名夹具、非法标签及两视图/两次生成一致。
验收不包含 `$expr/$eq` API composition，也不代表 MCP 消费者或 MongoDB Server 已运行验证。

```powershell
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.VariableBindingScopeSelfTest .
java -cp 'mongo-plus-indexer/target/classes' com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

### 验证

```powershell
mvn -pl mongo-plus-indexer -am '-Dgpg.skip=true' test-compile
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MongoPlusIndexerSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MongoPlusPipelineIndexerSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineExpressionSemanticsSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineExpressionCoverageSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineStageSemanticsSelfTest .
```

五者是显式运行的可执行测试，不应把 Surefire 的 `Tests run: 0` 当作测试通过。
Pipeline 测试包含全部 Stage evidence 在增加 Expression 根前后保持一致、独立 Expression 根、
旧包副本排除、根顺序/重复配置/签名重复可达、继承/静态排除、同名未标记重载、枚举、
构造器循环闭包、函数接口、真实 FieldChain/SFunction/concept 及重复生成比较。
移除真实 Stage 或 Expression 入口标签、保留方法名和描述后，必须生成空映射。
参数测试从输入 group(sum/avg) → sort → limit 管道及 Index 中的映射、参数 semanticType、
concept 和返回类型证明字段引用可传递，不预置目标 Java 调用代码；另覆盖无标记不推断、
泛型改名、类标签/同名重载不传播、varargs 元素、非法参数标记拒绝及普通字符串/变量边界。

`mongo-plus-indexer` 使用 JDK Compiler Tree API 从 Java 源码生成 **MongoPlus API Index**，不依赖第三方解析器或 JSON 库。

默认扫描入口是 `mongo-plus-core` 的 `com.mongoplus.conditions` 包。参数或特殊语义引用仅按具体类型定位，不会遍历整个 MongoPlus 源码树。

```java
MongoPlusIndexerConfig config = MongoPlusIndexerConfig.builder()
        .addSourceRoot(coreSourceRoot)
        .addSourceRoot(annotationSourceRoot)
        .addPrimaryPackage("com.mongoplus.conditions")
        .mongoPlusVersion("2.2.0")
        .output(output)
        .build();

MongoPlusIndexer indexer = new MongoPlusIndexer(config);
MongoPlusApiIndex index = indexer.generate();
indexer.write(index);
```

在 MongoPlus 源码工程内可用 `MongoPlusIndexerConfig.forMongoPlusProject(projectRoot)` 自动读取根 POM 版本和默认路径。

`SFunction` 参数根据泛型上下文区分字段 getter、条件分组和文档构建 Lambda。
`FieldChain.publicMethods` 使用包含方法名、完整签名、返回类型和参数的结构化方法描述，可机械验证
`FieldChain.build()` 返回值与接收 `String` 字段名的条件方法是否兼容。

Schema `1.1` 为每个 Wrapper 记录 `kind`、`abstractType` 和源码派生的 `constructors`。每个 constructor
包含签名、可见性、是否为 Java 隐式构造器，以及真实参数名、类型和 varargs 标记；没有显式构造器的
public class 会按 Java 语言规则生成一条 `implicit=true` 的 public 无参构造证据。

方法 Javadoc 可通过重复声明 `@mongoComposition <value>` 提供源码级组合语义。Indexer 只读取该
自定义 Tag，不根据方法名或 description 推断，并在 MethodFamily 的 `compositionSemantics` 数组中
按稳定顺序聚合、去重；未声明组合语义的方法族保留空数组。`mongoOperators` 仍只表示 MongoDB Operator。

默认项目内生成位置：

```text
mongo-plus-indexer/target/generated-resources/mongo-plus-api-index.json
```

CLI 参数：

```text
MongoPlusIndexerMain --project-root <MongoPlus根目录>
```

## P0-03：有序文档条目与投影模式

`DOCUMENT_SHAPE_V1` 是源码显式声明的通用 document entry/input/policy 能力。消费者必须先识别
requiredCapabilities，逐 overload 核对参数语义、Java 表示、typed container 和原有 Stage effect。
此能力不选择多候选，不将 Bson 返回类型或方法名当作语义，也不新增 Core 运行时校验。

- `@mongoDocumentEntry key=<参数名|literal:固定键> value=<expression参数名|int32:固定值>
  result=<STAGE_BODY_DOCUMENT|SORT_SPECIFICATION> target=RESULT order=INPUT duplicatePosition=<FIRST|LAST>`
  声明实际键/值构造；RESULT 必须为真实 Bson 工厂且消费全部参数。字段数组/List 使用一层 AST
  和独立 FIELD_NAME/OUTPUT_FIELD_NAME evidence 验证；String 原值与 getter 映射不得混用。
- `@mongoDocumentInput parameter=<参数名> semantic=<文档角色> encoding=<WRAP_DECLARED_STAGE|UNCHANGED>`
  声明 body 包装或完整 Stage 透传。包装依赖独立 Stage effect；透传不建立 body→Stage 工厂。
- `@mongoDocumentPolicy input=FLAT_DOCUMENT numeric=ZERO_NONZERO_FLAGS boolean=FALSE_TRUE_FLAGS
  exceptionField=_id mixed=REJECT_NON_EXCEPTION_EXCLUSION empty=REJECT expressions=INDEPENDENT_EVIDENCE`
  声明投影模式规则。数字/bool literal 需要独立 opaque literal expression；内层 operand 数字
  不经过顶层模式分类。nested projection 与特殊 computed 排除的完整规则未建立。
- 每种声明须有独立 `@mongoDocumentSource <对应tag> path|reference=<位置> symbols=<符号>
  mechanism=<编码/规则来源>`，未知属性、冲突、缺参数/来源/effect 或非法泛型均拒绝生成。

既有 `DOCUMENT_REDUCTION_V1` 复用于 SORT_SPECIFICATION ELEMENT：输入与结果文档角色必须一致，
新增可选 `duplicatePosition=FIRST|LAST` 记录真实重键位置；旧 STAGE_BODY_DOCUMENT 输出保持兼容。
重复输入字段必须在消费者归约前拒绝；LAST_WINS 是 Core 行为事实，不授权覆盖原 Pipeline。

正式 fixed 工厂证据保留所有 String/getter、List/varargs overload。getter evidence 不创建从
字符串构造 getter 的能力；Order/Projection 专用对象和 driver Stage 工厂亦须独立构造契约。
`Aggregate.sort(Bson)` 仍是完整 Stage 透传，不能接收 Sorts.orderBy 的 body；混合方向的完整
Core Stage 由独立的 `Aggregate.sortSpecification(Bson specification)` 默认方法构造：
`SORT_SPECIFICATION VALUE → Driver Aggregates.sort(body) → custom(stage)`，追加到当前 receiver。
此入口的 `documentInputBinding` 声明 `WRAP_DECLARED_STAGE` 和 `DECLARED_STAGE_WRAPPER`，
与自身唯一 `$sort` 映射和 `APPEND_STAGE/RECEIVER/ONE/CALL_ORDER` effect 一起闭合 composition。
方法返回 `Children`，不能把此 body→Stage 关系误标为方法返回 Bson。缺 composition 或 effect
时必须拒绝绑定；旧透传入口、同名重载、裸 body 和 Bson Java 类型不能补齐缺失证据。

```java
Aggregate<?> aggregate = new AggregateWrapper();
aggregate.sortSpecification(Sorts.orderBy(
        Sorts.desc("createTime"), Sorts.asc("score"), Sorts.desc("id")));
```

默认实现保持公开接口的既有实现兼容，通过 custom hook 追加一次；null body 在追加前拒绝。
排序字段原序和 Int32 的 1/-1 由既有 entry/reduction evidence 及真实 codec 测试保证。
facet/lookup/unionWith 内层复用独立 receiver、representation、extraction/container 和 Stage effect，
没有 sort 专用 planner；两个 reducer 候选仍全部保留。

新增自测入口：`com.mongoplus.indexer.PipelineSortProjectionEvidenceSelfTest <项目根>`。
排序 Stage 正负自测：`com.mongoplus.indexer.PipelineSortStageEvidenceSelfTest <项目根>`。
原始审计见 [P0-03 报告](p0-03-sort-projection-report.md)，缺口补齐、正式生成与 MCP 验证边界见
[排序 Stage 报告](p0-03-sort-stage-report.md)。

## P0-04 第一阶段：条件化候选语义

`CANDIDATE_SEMANTICS_V1` 发布每个声明的 `candidateSemantics`，不创建候选优先级或选择器。
`@mongoCandidate operation=<构造操作> ...` 与当前方法独立的 `@mongoCandidateSource
path|reference=<位置> symbols=<真实符号> mechanism=<来源机制>` 必须成对存在。标签不从类级、
相邻 overload、方法名或返回 Bson 类型传播。未知属性/重复属性/错误 Java 容器/缺来源拒绝生成；
依赖的参数、composition 或 effect 缺失则输出 `proofStatus=NOT_ESTABLISHED`，且不输出 bsonTerm。

契约包含实际 `parameterBindings`（Java AST 类型、数组/List/Collection、泛型、VARARGS/SINGLE）、
`applicability`、`bsonTerm`、`receiverEffect`、`normalizations` 及来源。构造项使用通用的
Stage value、document entry/reduction/wrapping、固定值字段 Stage、expression array 和
prefixed named entries 操作；参数、Stage/expression 键、方向、保留键和重复键位置均来自显式
声明及原有契约。`FIELD_STAGE` 可声明 `allowedInt32=<逗号分隔精确整数>` 输入域；范围属于
绑定消费者，不能冒充 Core 运行时校验。`EXPRESSION_ARRAY` 另外声明真实结果 Java 表示和
实际 collection codec；不能把任意子节点的 BSON 直接替代其 runtime codec 输入。

两个完整调用树仅在共同的具体输入域、每条 Java 路线都能绑定、所有叶子与变量 scope 都闭合、
实际 codec 编码已证明、类型/值/文档 entry 顺序/数组顺序完全相同，且对应逻辑 receiver 节点的
Stage 数量和调用序相同的条件下等价。具体 receiver 允许按独立工厂节点对应，但 sibling、
inner/outer 隔离和变量绑定不能变更。中间文档角色仍须按原 binding/composition 独立准入。
`ESTABLISHED` 表示当前节点的条件化构造规则成立，不表示任意完整树已经闭合。

单元素 unset 的 `SINGLETON_SCALAR_ELSE_ARRAY` 是声明的 Java 输入构造规则，记录
`rawBsonRewriteAllowed=false`；原始 BSON String 和单元素 BSON Array 严格不等价。数字不按
数值相同忽略 Int32/Int64/Double/Boolean 区别。当前源码证明基于 Driver 5.4 默认 codec 或
独立的实际编码证明；同一个自定义 registry 可对不同 Collection 类选择不同编码器，不能据此
合并 multiply 的两条路线。可变集合必须固定同一快照；任意 BSON 对象 encodability 也需证明。

正式 Index 保留全部 overload，并在 requiredCapabilities/concepts 中声明新能力。消费者遇到
未知 capability 必须拒绝；此阶段没有 MCP 搜索、唯一选择、稳定 tie-break 或 Java planner。
自测入口为 `PipelineCandidateSemanticsSelfTest <项目根>`；完整关系表、构造验证、未闭合项和
正式生成 SHA 见 [P0-04 报告](p0-04-candidate-semantics-report.md)。

## P0-05 第一阶段：常见表达式工厂

正式扫描根不变，新增 AggregateOperator 的 `ne/gt/gte/lt/lte/subtract/divide(Object,Object)`、
`and/or(Object...)` 和 `not(Object)`。Filters 同名 Query Predicate 不作为表达式映射。
eq 仅增加实际 Document 结果表示及构造来源；其执行代码不变。

复用 ARRAY shape、逐参数 `PIPELINE_EXPRESSION VALUE|ELEMENT`、显式
`@mongoComposition ... -> PIPELINE_EXPRESSION` 及 `CANDIDATE_SEMANTICS_V1`。
通用声明 `EXPRESSION_ARGUMENTS` 独立验证这些依赖，固定 VALUE 参数按声明顺序产生
`ARRAY_ARGUMENTS_RUNTIME_CODEC.inputs`，Object varargs ELEMENT 参数产生既有
`ARRAY_RUNTIME_CODEC.input`。数量和 null 域在 applicability.expressionOperands 中；
not 的数组长度为一，and/or 支持零元素。没有按 operator 编写 planner。

所有来源由每个方法的 `@mongoCandidateSource` 独立声明：当前 Core、Driver 5.4 codec 与
MongoDB 官方聚合语法。实际容器/叶子 codec、嵌套方法的结果及 runtime Document 表示、
变量 scope 仍须递归证明；Object 参数和已有 BSON 不能替代这些义务。新规则依赖缺失则
NOT_ESTABLISHED 且无 bsonTerm；缺来源或非法标签拒绝生成。旧 EXPRESSION_ARRAY 分支不变。
消费者必须拒绝不认识的构造节点，不得跳过候选证据或把 expression 当完整 Stage。

新自测入口：`CommonExpressionEvidenceSelfTest <项目根>`。本轮全部 Indexer main 需显式运行；
`MavenLocalRepositorySelfTest` 使用零参数，不能统一传项目根。
参数审计表增加 17 个独立槽，正式 Expression 为 61 families/169 overloads；
总计 95 families/310 overloads，Stage 保持 34/141。报告、可闭合形态、验证边界及新 SHA
见 [P0-05 报告](p0-05-common-expression-report.md)。

## P0-06 第一阶段：Expression result、类型与数组组合

普通参数形式的 multiply、ifNull、cond/condArray、toDate/toBool/toDecimal/toDouble/
toHashedIndexKey/toInt/toLong/toObjectId/toString 和 mergeObjects 补齐当前 overload 的
shape、composition、result 与实际 Document 构造来源。复用 `@mongoParam`、
`@mongoComposition` 和 `CANDIDATE_SEMANTICS_V1`；原 52 条候选规则保持原义。

新增两个通用构造声明：`EXPRESSION_VALUE resultJava=org.bson.Document` 表示单个 VALUE
操作数经 runtime codec 编码，对应 `RUNTIME_CODEC_VALUE` 节点；
`EXPRESSION_OBJECT_ARGUMENTS resultJava=org.bson.Document fields=<有序字段>` 将固定参数按
声明顺序写入对象，对应 `ORDERED_DOCUMENT_ARGUMENTS` 节点。后者要求每个字段和参数一一对应，
独立 OBJECT shape、同数量的 expression composition 和逐参数语义。标量 shape 使用 `VALUE`。
缺 result/composition/shape/参数或 Java 类型证据不能闭合；缺独立来源拒绝生成。
未知节点消费者必须拒绝，不能仅因识别 V1 capability 而放行。

新增 `JAVA_TYPE_RELATIONS_V1` 只承担独立的 Java 类型事实。当前类级显式声明为：

```text
@mongoJavaTypeRelation artifact=org.mongodb:bson:5.4.0 subtype=org.bson.Document supertype=org.bson.conversions.Bson
```

Indexer 从配置的 Maven repository 精确加载该 artifact，不扫描仓库、不下载依赖；校验版本身份、
读取公开类型并计算 `supertype.isAssignableFrom(subtype)`、反向关系、直接 superclass/interfaces。
正式 `javaTypeRelations` 包含声明来源、artifact/class SHA-256 和 applicability；消费者须核对相同
binary type definitions。目标类型由声明决定，不按 Document/Bson 类名硬编码。
`semanticRoleImplied`、`runtimeCodecImplied`、`bsonEquivalenceImplied` 均为 false；泛型容器协变、
unchecked cast 和任意 Codec 等价不由该事实授予。缺 artifact、类型或版本身份拒绝生成。

`AggregateOperator.concatArraysExpressions(Object... operands)` 是新增独立入口，接受字段引用
和独立证明的 nested expression。旧 `concatArrays(List<?>... list)` 方法体与 BSON 不变；没有新增
同名 varargs overload。旧 List literal 形态仅补 shape/result，仍需数组 literal 自身的递归构造证明。

`mergeObjects(String)` 保存单值，Collection 保存原集合，Object varargs 经 stream 收集后保存数组；
三者都返回真正的 Document。String 单值与单元素数组严格不同。Accumulators 的 BsonField 角色
不在本轮普通 Expression 准入范围内。`replaceWith(Document)` 包装 Stage，`replaceWith(Bson)`
透传完整 Stage，不能因 Document 可赋值给 Bson 而交换这两条路线。

验证入口：`ExpressionResultTypeEncodingTest`（Core JUnit）和
`ExpressionResultTypeEvidenceSelfTest <项目根>`（Indexer 显式 main）。逐 overload 审计表、
实测结果及正式 Index SHA 见 [P0-06 报告](p0-06-expression-result-type-report.md)。
本轮不修改 MCP Resolver、Compact 或 AI Prompt，不宣称完整 Stage SELECTED。

### P0-06 A05：父级 Document 表达式的条件等价

`Aggregate.replaceWith(Document)` 与 `replaceWith(TExpression)` 各自声明通用
`STAGE_EXPRESSION_DOCUMENT runtimeJava=org.bson.Document runtimeCodec=org.bson.codecs.DocumentCodec
bsonDocumentCodec=org.bson.codecs.BsonDocumentCodec`。
复用 `CANDIDATE_SEMANTICS_V1` 的 `DOCUMENT → BUILDERS_HELPER_CODEC → INPUT` 构造项，
新增 `applicability.documentExpressionDomain` 守卫；缺独立参数、composition/result 或 effect
或缺本地委托/外部编码任一来源时仍为 `NOT_ESTABLISHED`。守卫要求 Document 来源和精确 runtime 类、真实静态参数及泛型/overload
绑定、完整 Expression 子树、Document/BsonDocument 中间层及所有容器/叶子 Codec、同一不可变输入快照、BSON 文档类型及
所有 entry/array 顺序、对应 receiver 的单次有序 Stage 追加。不能仅由 Object 或 Bson 可编码而准入。
String、任意 Bson、未知泛型对象、Document 子类、null、未知 Codec 或缺证据不属于已证明等价域。
实际 Java 21 编译中 `<Document>replaceWith(document)` 仍选 Document overload；泛型路线使用
`<Object>replaceWith((Object) provenDocument)` 并独立核验 Document 来源，不能把类型实参当作 overload 证据。
消费者须验证该输入域全部守卫；仅认识旧构造节点或 capability 名称不足以合并候选。
专项入口：Core `ReplaceWithDocumentEquivalenceTest`、Indexer `ReplaceWithDocumentEvidenceSelfTest <项目根>`。
