# MongoPlus Indexer

## Pipeline 专用 Index

使用 `MongoPlusIndexerConfig.forPipelineProject(projectRoot)`，或执行：

```powershell
java -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

输出 `mongo-plus-indexer/target/generated-resources/mongo-plus-pipeline-api-index.json`。
该入口独立于原 Query CLI，复用 `SourceScanner`、`MongoPlusApiIndex` 和 `JsonWriter`。

### 最终 JSON 契约

沿用 schemaVersion `1.1`，版本字段使用既有 `mongoPlusVersion`。顶层字段为：

```text
schemaVersion, project, mongoPlusVersion, primaryScanModule, primaryPackages,
scanStatistics, wrappers, methodFamilies, types, specialTypes, concepts, entryType
```

`entryType` 固定为 `com.mongoplus.aggregate.Aggregate`。`primaryPackages` 仅表示入口所属包，
不表示遍历该包。Pipeline 不支持 `generatedAt`。

`methodFamilies` 按 `apiCategory + name` 分组，category 为 `PIPELINE_STAGE` 或
`PIPELINE_EXPRESSION`；每族保留已有 description、mongoOperators、compositionSemantics、aliases、
overloads，并增加 `apiCategory`、`mongoStages`、`mongoExpressions`。
overload 保留原签名、返回类型、泛型、参数名/类型/语义/varargs、modifiers、annotations、deprecated、
declaredIn、availableIn，补充 name、parameterTypes 和 overload 自身的两类映射。
不同声明类型的相同签名不会折叠；未标记的同名重载不会借用其他重载的映射。

### 筛选与依赖边界

1. 精确定位 Aggregate 及其源代码父类型；只读取 Javadoc 自定义块标签 `@mongoStage`、
   `@mongoExpression`。类级标签适用于该类声明的方法，方法级标签用于对应方法。
   每个标签值必须完整匹配 `$` 加字母数字标识符，多个映射通过重复标签声明。
   不解析描述正文、方法名、包名或 MongoDB 能力列表。
2. 从已选择方法的参数、返回值、泛型界限出发，按同包、显式 import、通配 import 精确定位源码类型。
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

2026-09-06 的 Core evidence 补充后，实际生成 33 个 stage family（140 个 overload）、
0 个 expression family，methodFamilies 合计 33。Core 另有 211 条显式 expression 标签，
覆盖 42 种 MongoDB expression；这些独立静态工厂没有进入 Aggregate 的签名依赖闭包，
因此不会被正式入口收录。这是既有扫描边界，不是合法 tag 解析失败；本次未修改生成逻辑。
详细映射和保守排除原则见 [聚合源码 evidence](../docs/ai/architecture/AGGREGATION.md#pipeline-javadoc-evidence)。

### 验证

```powershell
mvn -pl mongo-plus-indexer -am '-Dgpg.skip=true' test-compile
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MongoPlusIndexerSelfTest .
java -cp 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes' com.mongoplus.indexer.MongoPlusPipelineIndexerSelfTest .
```

两者是显式运行的可执行测试，不应把 Surefire 的 `Tests run: 0` 当作测试通过。
Pipeline 测试包含真实 Stage evidence、带标签夹具、继承/静态排除、同名未标记重载、枚举、
构造器循环闭包、函数接口、真实 FieldChain/SFunction/concept 及重复生成比较。
移除真实入口标签、保留方法名和描述后，必须生成空映射；仅在测试夹具中显式连接真实
表达式工厂时，验证其 211 条 expression evidence 可以解析。这不改变正式入口的依赖闭包。

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
