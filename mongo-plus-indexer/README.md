# MongoPlus Indexer

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
