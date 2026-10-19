# MongoPlus JavaParser API 索引验证工具

独立 Maven 工程，不继承根 POM，也不加入根 reactor，不修改业务源码。JDK 17+ 构建和运行；本次使用 JDK 21。

依赖固定为 Maven Central 的正式发布版：

- [JavaParser Core 3.26.3](https://repo.maven.apache.org/maven2/com/github/javaparser/javaparser-core/3.26.3/)
- [Jackson Databind 2.18.3](https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.3/)
- 测试使用 JUnit Jupiter 5.11.4。

## 运行

在 MongoPlus 仓库根目录执行 PowerShell：

```powershell
$env:JAVA_HOME = 'D:/Java/java21'
$env:PATH = "$env:JAVA_HOME/bin;$env:PATH"
mvn.cmd -f tools/mongoplus-api-indexer/pom.xml clean package
java -jar tools/mongoplus-api-indexer/target/mongoplus-api-indexer.jar --root . --output generated
```

也可使用绝对路径指定任意仓库和输出目录：

```powershell
java -jar D:/idea_workspace/mongo-plus/tools/mongoplus-api-indexer/target/mongoplus-api-indexer.jar --root D:/idea_workspace/mongo-plus --output D:/idea_workspace/mongo-plus/generated
```

输出目录不存在时自动创建；再次运行覆盖对应索引文件。解析失败会向 stderr 输出文件路径和原因，仍生成成功文件的索引，最终退出码为 1；成功退出码为 0。参数/POM/输出读写错误直接终止。

输出布局：

```text
generated/
  index.md                       简洁总目录
  api-index.jsonl                 仅 public/protected API
  source-index.jsonl              所有可见性的源码记录
  api-index.md                    保留的完整 Markdown，包含内部实现
  modules/<Maven 模块路径>/index.md  按模块拆分的完整 Markdown
```

根模块有源码时使用 `modules/_root/index.md`。模块删除或改名后，旧模块 Markdown 不自动删除；总目录仅引用本次生成的模块。

## 扫描和字段语义

- 递归读取根 POM 及其直接 `<modules>`，扫描每个模块的 `src/main/java/**/*.java`，排除 `target`。不扫描测试、插件生成目录、工具自身或其他非 reactor 目录。
- 当前 9 个模块全部包含在结果中，包括原有 `mongo-plus-indexer` 的主源码。该模块只是扫描对象，不参与新索引的生成。`mongo-plus-bom` 只有 POM，没有主源码。
- 不解析 profile、Maven 属性插值、自定义 sourceDirectory 或外部父 POM；适用于当前仓库的实际结构。
- JavaParser 按 Java 21 语法解析，无 Symbol Solver、无编译或类加载，不访问 MongoDB，不调用 LLM/MCP/CodeGraph。
- 完整源码索引包含全部可见性的显式类型、方法、构造方法及字段。接口隐式 public 正确归入 `visibility`；`modifiers` 仅保留显式修饰符。枚举未显式指定可见性的构造方法记为 private。
- `apiVisible` 表示声明及所有外层类型均为 public/protected；API JSONL 只输出该字段为 true 的记录。不会把 private/包级类型内的 public 成员导出；不分析签名涉及的其他类型是否可访问，也不展开继承成员。
- `recordType` 是 `type`、`method`、`constructor` 或 `field`。成员的 `qualifiedName` 表示所属类型；方法的 `id` 为模块、所属类型和参数类型组成的重载标识。构造方法的 `returnType` 为 null。
- `signature` 包含源码显式修饰符、方法泛型、返回类型、参数及 throws，规范化空白，不带方法体或 JavaDoc。注解单独存储；参数注解也存入 parameters。
- `parameters.type` 保留源码类型，`varArgs` 单独表示可变参数；不将简单类型猜测为全限定名。类型泛型、extends 和 implements 也保留源码声明。
- 所有 `annotations` 仅从 JavaParser 的 AnnotationExpr 节点序列化，关闭附带注释的打印，保留注解参数中的字符串字面量；不通过正则删除注释文本。
- 多变量字段声明逐变量输出，保存各自名称和类型（包括不同数组维数）；`line/endLine` 是整条字段声明范围，`variableLine` 是变量起始行。字段不输出初始化表达式。
- `javadoc` 是注释内部原文（保留星号、换行和标签）；没有时为空字符串。源码路径相对仓库根目录；行号从 1 开始，起点为 AST 声明起点（有注解时可为注解行），终点包括方法体。
- 包含具名成员嵌套类型，使用 `Outer.Inner` 名称；排除局部类、匿名类及其方法。注解成员按 method 记录，另保存 `defaultValue`。额外识别 record 类型，但本版不生成 record 隐式成员或紧凑构造方法记录。
- 不展开继承方法、默认构造方法、Lombok 方法或 enum 隐式方法；未提取枚举常量。
- 输出按模块、包、全限定类型名、成员源码行排序；Markdown 按相同层级组织。没有时间戳，同一源码重复生成结果一致。

## 本次实际运行（2026-10-19）

| 模块 | Java 文件 |
| --- | ---: |
| mongo-plus-annotation | 59 |
| mongo-plus-boot-starter | 28 |
| mongo-plus-boot4-starter | 27 |
| mongo-plus-core | 452 |
| mongo-plus-indexer | 20 |
| mongo-plus-sensitive-word | 11 |
| mongo-plus-sharding | 7 |
| mongo-plus-sharding-boot-starter | 2 |
| mongo-plus-solon-plugin | 16 |

扫描 622，成功 622，失败 0；完整记录含类型 655、方法 4674（包含注解成员）、构造方法 336、字段 1013。

| 输出 | 记录数 | 字节数 |
| --- | ---: | ---: |
| api-index.jsonl | 5236 | 4395592 |
| source-index.jsonl | 6678 | 5362169 |
| api-index.md | 6678 | 2558048 |
| index.md | 9 个模块入口 | 954 |

4 个 JUnit 测试通过：覆盖重载、泛型、throws、可变参数、接口隐式可见性、所有外层类型的可见性过滤、各可见性的构造方法、字段/多变量数组声明、注解与普通注释分离、字符串参数保留、逐行 JSON 严格解析、Markdown 分文件、匿名类排除、扫描目录排除和解析失败。原索引的 5564 条声明均在 source-index.jsonl 中保留；新增 101 个非 public 构造方法和 1013 个字段。

按实际记录的 sourcePath/line/endLine 回读源码，对以下四个类型的全部 22 个方法逐一去除空白后比较声明，签名（含参数、返回类型和 throws）一致。类型名称、包和声明位置也已对照源码检查。

| 类型 | 声明行 | 显式方法数 | 代表性声明 |
| --- | ---: | ---: | --- |
| com.mongoplus.conditions.query.QueryWrapper | 8 | 1 | `public QueryChainWrapper<T, QueryWrapper<T>> lambdaQuery()`，14 行 |
| com.mongoplus.listener.Listener | 11 | 4 | `void commandStarted(CommandStarted commandStarted)`，30 行 |
| com.mongoplus.interceptor.Interceptor | 22 | 14 | `default List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection)`，65 行；同名 Document 重载在 75 行 |
| com.mongoplus.interceptor.AdvancedInterceptor | 9 | 3 | `Object intercept(Invocation invocation) throws Throwable`，27 行 |

这验证的是源码声明提取，不表示完整业务构建、跨模块类型解析或运行行为验证。

## 浏览实际输出

- [JSONL](../../generated/api-index.jsonl)
- [完整源码 JSONL](../../generated/source-index.jsonl)
- [Markdown](../../generated/api-index.md)
- [模块总目录](../../generated/index.md)

提取指定真实 API 的全部原始 JSONL 行：

```powershell
Get-Content generated/api-index.jsonl | Where-Object {
    $_ -match '"qualifiedName":"com\.mongoplus\.(conditions\.query\.QueryWrapper|listener\.Listener|interceptor\.(Interceptor|AdvancedInterceptor))"'
}
```
