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

1. Stage 保持精确定位 Aggregate 及其源代码父类型；另精确定位下表五个 Expression Root。
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
| `com.mongoplus.aggregate.pipeline.AggregateOperator` | concat、concatArrays、dateTrunc 等表达式 BSON | 7 |
| `com.mongoplus.aggregate.pipeline.Projections` | 已标记的 meta 投影表达式 | 9 |
| `com.mongoplus.aggregate.pipeline.Sorts` | 已标记的 metaTextScore 表达式 | 2 |
| `com.mongoplus.conditions.operation.ConditionOperators` | 条件、算术、日期等表达式 BSON | 54 |

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
- `variableReference`：前缀 `$$`，原样编码；源码例子为 `$$SEARCH_META`，不验证变量绑定。
- `plainStringValue`：不以 `$` 开头的字符串原样写入，不自动加字段前缀；不自动 literal 转义。
  `$` 开头的字符串如何强制表示 literal，记录为 `NOT_ESTABLISHED`，不虚构专用 `$literal` API。
- `sourceEvidence`：记录源码路径/符号及 Driver 5.4.0、默认 StringCodec 的实际编码依据。
  这是表示和传递契约，不保证任意表达式都符合具体操作符的服务端类型/作用域要求。

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

### 验证

```powershell
mvn -pl mongo-plus-indexer -am '-Dgpg.skip=true' test-compile
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MongoPlusIndexerSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MongoPlusPipelineIndexerSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineExpressionSemanticsSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.PipelineExpressionCoverageSelfTest .
```

四者是显式运行的可执行测试，不应把 Surefire 的 `Tests run: 0` 当作测试通过。
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
