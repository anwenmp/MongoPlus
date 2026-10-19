# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 387 条完整源码记录（含内部实现）。

## 模块：mongo-plus-indexer

### 包：com.mongoplus.indexer

#### class com.mongoplus.indexer.MavenLocalRepository

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:21-127

```text
只读取 Maven 本地仓库位置，不解析依赖或访问远程仓库。
```

##### field PROPERTY_REFERENCE

```java
private static final Pattern PROPERTY_REFERENCE
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:22-22

##### constructor MavenLocalRepository

```java
private MavenLocalRepository()
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:24-24

##### method resolve

```java
static Path resolve()
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:27-51

```text
显式 JVM 属性优先，用户 settings 覆盖 Maven 安装目录的全局 settings。
```

##### method fromSettings

```java
private static Path fromSettings(Path settings)
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:54-97

```text
文件不存在或没有非空 localRepository 时返回 null，允许继续使用低优先级配置。
```

##### method interpolate

```java
private static Path interpolate(String value, Path settings)
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:100-117

```text
settings 仅支持系统属性和 env.* 插值，不使用 profile 内的属性推断路径。
```

##### method unresolved

```java
private static IllegalArgumentException unresolved(Path settings)
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:119-122

##### method nonBlank

```java
private static String nonBlank(String value)
```

所属类型：com.mongoplus.indexer.MavenLocalRepository；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MavenLocalRepository.java:124-126

#### class com.mongoplus.indexer.MongoPlusIndexer

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:14-54

```text
MongoPlus API Index 的公共生成入口。
```

##### field config

```java
private final MongoPlusIndexerConfig config
```

所属类型：com.mongoplus.indexer.MongoPlusIndexer；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:15-15

##### constructor MongoPlusIndexer

```java
public MongoPlusIndexer(MongoPlusIndexerConfig config)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexer；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:17-20

##### method generate

```java
public MongoPlusApiIndex generate() throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexer；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:23-29

```text
扫描源码并返回内存索引，不产生文件副作用。
```

##### method write

```java
public Path write(MongoPlusApiIndex index) throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexer；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:32-35

```text
将索引写入配置的输出路径。
```

##### method write

```java
public Path write(MongoPlusApiIndex index, Path output) throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexer；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:38-46

```text
将索引写入指定路径。
```

##### method generateAndWrite

```java
public MongoPlusApiIndex generateAndWrite() throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexer；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:49-53

```text
扫描并写入配置的输出路径。
```

#### class com.mongoplus.indexer.MongoPlusIndexerConfig

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:15-190

```text
* MongoPlus API Index 生成配置。
 *
 * <p>源码根目录仅用于按包名精确定位源码。扫描器不会遍历源码根目录中的全部 Java 文件。</p>
```

##### field DEFAULT_PRIMARY_PACKAGE

```java
public static final String DEFAULT_PRIMARY_PACKAGE
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:17-17

##### field DEFAULT_OUTPUT_FILE

```java
public static final String DEFAULT_OUTPUT_FILE
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:18-18

##### field PIPELINE_ENTRY_TYPE

```java
public static final String PIPELINE_ENTRY_TYPE
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:19-19

##### field PIPELINE_OUTPUT_FILE

```java
public static final String PIPELINE_OUTPUT_FILE
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:20-20

##### field PIPELINE_EXPRESSION_ROOTS

```java
public static final List<String> PIPELINE_EXPRESSION_ROOTS
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:23-29

```text
面向用户的表达式工厂；旧包 ConditionOperators 已废弃，不作为独立入口。
```

##### field PIPELINE_CONSTRUCTION_ROOTS

```java
public static final List<String> PIPELINE_CONSTRUCTION_ROOTS
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:32-34

```text
不在 Stage 签名中的公开 receiver/组合构造入口；加载类型不赋予语义。
```

##### field sourceRoots

```java
private final List<Path> sourceRoots
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:36-36

##### field primaryPackages

```java
private final List<String> primaryPackages
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:37-37

##### field output

```java
private final Path output
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:38-38

##### field mongoPlusVersion

```java
private final String mongoPlusVersion
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:39-39

##### field includeGeneratedAt

```java
private final boolean includeGeneratedAt
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:40-40

##### field pipeline

```java
private final boolean pipeline
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:41-41

##### field expressionRoots

```java
private final List<String> expressionRoots
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:42-42

##### field constructionRoots

```java
private final List<String> constructionRoots
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:43-43

##### field constructionArtifactRepository

```java
private final Path constructionArtifactRepository
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:44-44

##### constructor MongoPlusIndexerConfig

```java
private MongoPlusIndexerConfig(Builder builder)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:46-73

##### method builder

```java
public static Builder builder()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:75-77

##### method forMongoPlusProject

```java
public static Builder forMongoPlusProject(Path projectRoot) throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:85-93

```text
* 为 MongoPlus 源码工程创建默认配置，并从根 POM 自动读取版本。
     *
     * @param projectRoot MongoPlus 根目录
     * @return 已配置 core/annotation 源码根、conditions 主包和默认输出的构建器
```

##### method readProjectVersion

```java
private static String readProjectVersion(Path pom) throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:95-112

##### method getSourceRoots

```java
public List<Path> getSourceRoots()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:114-114

##### method getPrimaryPackages

```java
public List<String> getPrimaryPackages()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:115-115

##### method getOutput

```java
public Path getOutput()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:116-116

##### method getMongoPlusVersion

```java
public String getMongoPlusVersion()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:117-117

##### method isIncludeGeneratedAt

```java
public boolean isIncludeGeneratedAt()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:118-118

##### method isPipeline

```java
public boolean isPipeline()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:119-119

##### method getExpressionRoots

```java
public List<String> getExpressionRoots()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:120-120

##### method getConstructionRoots

```java
public List<String> getConstructionRoots()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:121-121

##### method getConstructionArtifactRepository

```java
public Path getConstructionArtifactRepository()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:122-122

##### method forPipelineProject

```java
public static Builder forPipelineProject(Path projectRoot) throws IOException
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:125-131

```text
创建聚合专用索引配置，复用项目版本和源码根。
```

#### class com.mongoplus.indexer.MongoPlusIndexerConfig.Builder

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:134-189

```text
配置构建器。
```

##### field sourceRoots

```java
private final List<Path> sourceRoots
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:135-135

##### field primaryPackages

```java
private final List<String> primaryPackages
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:136-136

##### field output

```java
private Path output
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:137-137

##### field mongoPlusVersion

```java
private String mongoPlusVersion
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:138-138

##### field includeGeneratedAt

```java
private boolean includeGeneratedAt
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:139-139

##### field pipeline

```java
private boolean pipeline
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:140-140

##### field expressionRoots

```java
private final List<String> expressionRoots
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:141-141

##### field constructionRoots

```java
private final List<String> constructionRoots
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:142-142

##### field constructionArtifactRepository

```java
private Path constructionArtifactRepository
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:143-143

##### method constructionArtifactRepository

```java
public Builder constructionArtifactRepository(Path repository)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:146-150

```text
只读取显式 construction 契约引用的 artifact；不扫描仓库、不下载依赖。
```

##### method addConstructionRoot

```java
public Builder addConstructionRoot(String qualifiedName)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:153-159

```text
添加精确构造类型入口；其语义仍须由当前声明的源码标签证明。
```

##### method addExpressionRoot

```java
public Builder addExpressionRoot(String qualifiedName)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:162-168

```text
添加精确的表达式 API 类型入口，不按包或标签反向扫描工厂。
```

##### method addSourceRoot

```java
public Builder addSourceRoot(Path sourceRoot)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:170-174

##### method addPrimaryPackage

```java
public Builder addPrimaryPackage(String primaryPackage)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:176-182

##### method output

```java
public Builder output(Path output)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:184-184

##### method mongoPlusVersion

```java
public Builder mongoPlusVersion(String version)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:185-185

##### method includeGeneratedAt

```java
public Builder includeGeneratedAt(boolean include)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:186-186

##### method pipeline

```java
public Builder pipeline(boolean pipeline)
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:187-187

##### method build

```java
public MongoPlusIndexerConfig build()
```

所属类型：com.mongoplus.indexer.MongoPlusIndexerConfig.Builder；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexerConfig.java:188-188

### 包：com.mongoplus.indexer.cli

#### class com.mongoplus.indexer.cli.MongoPlusIndexerMain

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/cli/MongoPlusIndexerMain.java:12-53

```text
MongoPlus API Index 命令行入口。
```

##### constructor MongoPlusIndexerMain

```java
private MongoPlusIndexerMain()
```

所属类型：com.mongoplus.indexer.cli.MongoPlusIndexerMain；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/cli/MongoPlusIndexerMain.java:13-14

##### method main

```java
public static void main(String[] args) throws Exception
```

所属类型：com.mongoplus.indexer.cli.MongoPlusIndexerMain；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/cli/MongoPlusIndexerMain.java:16-52

#### class com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/cli/MongoPlusPipelineIndexerMain.java:10-37

```text
聚合专用 API Index 命令行入口。
```

##### constructor MongoPlusPipelineIndexerMain

```java
private MongoPlusPipelineIndexerMain()
```

所属类型：com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/cli/MongoPlusPipelineIndexerMain.java:11-11

##### method main

```java
public static void main(String[] args) throws Exception
```

所属类型：com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/cli/MongoPlusPipelineIndexerMain.java:13-36

### 包：com.mongoplus.indexer.json

#### class com.mongoplus.indexer.json.JsonWriter

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:9-86

```text
不依赖第三方库的 JSON 序列化器。
```

##### constructor JsonWriter

```java
private JsonWriter()
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:10-10

##### method write

```java
public static void write(Object value, Writer writer) throws IOException
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:12-15

##### method writeValue

```java
private static void writeValue(Object value, Writer writer, int depth) throws IOException
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:17-35

##### method writeObject

```java
private static void writeObject(Map<?, ?> value, Writer writer, int depth) throws IOException
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:37-49

##### method writeArray

```java
private static void writeArray(Iterable<?> value, Writer writer, int depth) throws IOException
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:51-61

##### method writeString

```java
private static void writeString(String value, Writer writer) throws IOException
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:63-81

##### method indent

```java
private static void indent(Writer writer, int depth) throws IOException
```

所属类型：com.mongoplus.indexer.json.JsonWriter；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/json/JsonWriter.java:83-85

### 包：com.mongoplus.indexer.model

#### class com.mongoplus.indexer.model.MongoPlusApiIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:9-35

```text
MongoPlus 条件构造 API 的稳定、机器可读索引。
```

##### field data

```java
private final Map<String, Object> data
```

所属类型：com.mongoplus.indexer.model.MongoPlusApiIndex；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:10-10

##### constructor MongoPlusApiIndex

```java
public MongoPlusApiIndex(String mongoPlusVersion)
```

所属类型：com.mongoplus.indexer.model.MongoPlusApiIndex；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:12-24

##### method asMap

```java
public Map<String, Object> asMap()
```

所属类型：com.mongoplus.indexer.model.MongoPlusApiIndex；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:26-26

##### method list

```java
public List<Object> list(String name)
```

所属类型：com.mongoplus.indexer.model.MongoPlusApiIndex；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:28-29

```java
@SuppressWarnings("unchecked")
```

##### method object

```java
public Map<String, Object> object(String name)
```

所属类型：com.mongoplus.indexer.model.MongoPlusApiIndex；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:31-32

```java
@SuppressWarnings("unchecked")
```

##### method getMethodFamilies

```java
public List<Object> getMethodFamilies()
```

所属类型：com.mongoplus.indexer.model.MongoPlusApiIndex；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/model/MongoPlusApiIndex.java:34-34

### 包：com.mongoplus.indexer.scanner

#### class com.mongoplus.indexer.scanner.ArtifactConstructorEvidence

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ArtifactConstructorEvidence.java:22-103

```text
从声明指定的真实 Maven artifact 校验公开 entry 构造器，不用类名推断语义。
```

##### constructor ArtifactConstructorEvidence

```java
private ArtifactConstructorEvidence()
```

所属类型：com.mongoplus.indexer.scanner.ArtifactConstructorEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ArtifactConstructorEvidence.java:23-23

##### method inspect

```java
static Map<String, Object> inspect(Path repository, String artifact, String className, int keyIndex, int valueIndex)
```

所属类型：com.mongoplus.indexer.scanner.ArtifactConstructorEvidence；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ArtifactConstructorEvidence.java:25-102

#### class com.mongoplus.indexer.scanner.CandidateSemanticContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:20-721

```text
* 候选的条件化 BSON 构造项。这里只发布可递归代入的事实，不选候选，不按 API 名称归类。
 * 类型和顺序属于结果；Java 容器表示属于参数域，二者不能混为等价依据。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:21-21

##### field LITERAL_CAPABILITY

```java
static final String LITERAL_CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:22-22

##### field TAG

```java
private static final String TAG
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:23-23

##### field SOURCE

```java
private static final String SOURCE
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:24-24

##### field ESTABLISHED

```java
private static final String ESTABLISHED
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:25-25

##### constructor CandidateSemanticContract

```java
private CandidateSemanticContract()
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:27-27

##### method hasTags

```java
static boolean hasTags(Map<String, List<String>> tags)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:29-31

##### method apply

```java
static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method, Function<String, Tree> parameterTree, Function<String, String> resolveType, Map<String, Map<String, String>> generics)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:33-523

```java
@SuppressWarnings("unchecked")
```

##### method concept

```java
static Map<String, Object> concept()
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:525-541

##### method wrap

```java
private static Map<String, Object> wrap(Map<String, Object> method, Map<String, Object> body)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:543-546

##### method queryResult

```java
private static void queryResult(Map<String, Object> method, String relation)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:548-558

##### method mapping

```java
private static String mapping(Map<String, Object> method, String key)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:560-564

##### method semantic

```java
private static void semantic(Map<String, Object> parameter, String semantic)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:566-570

##### method dependency

```java
private static Map<String, Object> dependency(Map<String, Object> owner, String key)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:572-576

```java
@SuppressWarnings("unchecked")
```

##### method only

```java
private static Map<String, Object> only(List<Map<String, Object>> parameters)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:578-581

##### method parameter

```java
private static Map<String, Object> parameter(List<Map<String, Object>> parameters, String name)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:583-586

##### method input

```java
private static Map<String, Object> input(Map<String, Object> parameter)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:588-590

##### method elementRepresentation

```java
private static String elementRepresentation(Map<String, Object> contract, Map<String, Object> parameter)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:592-602

```java
@SuppressWarnings("unchecked")
```

##### method representation

```java
private static Map<String, Object> representation(String declaration, Tree tree, Function<String, String> resolveType, Map<String, Map<String, String>> generics)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:604-639

##### method type

```java
private static String type(Tree tree, Function<String, String> resolveType, Map<String, Map<String, String>> generics)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:641-648

##### method integer

```java
private static int integer(String declaration, String value)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:650-653

##### method attributes

```java
private static Map<String, String> attributes(String declaration, String raw)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:655-665

##### method keys

```java
private static void keys(String declaration, Map<String, String> attrs, String... keys)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:667-671

##### method sources

```java
private static List<Object> sources(String declaration, List<String> sources)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:673-697

##### method node

```java
private static Map<String, Object> node(String op, Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:699-703

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:705-709

##### method missing

```java
private static MissingEvidence missing(String reason)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:711-711

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:713-715

#### class com.mongoplus.indexer.scanner.CandidateSemanticContract.MissingEvidence

可见性：private；泛型：[]；extends：[IllegalArgumentException]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:717-720

##### field serialVersionUID

```java
private static final long serialVersionUID
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract.MissingEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:718-718

##### constructor MissingEvidence

```java
private MissingEvidence(String message)
```

所属类型：com.mongoplus.indexer.scanner.CandidateSemanticContract.MissingEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/CandidateSemanticContract.java:719-719

#### class com.mongoplus.indexer.scanner.DocumentReductionContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:8-92

```text
显式文档归约标签的封闭契约；不从名称、返回类型或旧 composition 推断缺失信息。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:9-9

##### field TAG

```java
private static final String TAG
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:10-10

##### field DOCUMENT

```java
private static final String DOCUMENT
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:11-11

##### field ATTRIBUTES

```java
private static final Map<String, String> ATTRIBUTES
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:12-12

##### constructor DocumentReductionContract

```java
private DocumentReductionContract()
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:14-14

##### method apply

```java
static void apply(String declaration, List<String> tags, Map<String, Object> method)
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:16-77

##### method attributes

```java
private static Map<String, String> attributes()
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:79-87

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.DocumentReductionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentReductionContract.java:89-91

#### class com.mongoplus.indexer.scanner.DocumentShapeContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:16-258

```text
文档条目、输入角色及模式规则的显式证据；不依据操作符、方法名或参数名推断。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:17-17

##### field ENTRY

```java
private static final String ENTRY
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:18-18

##### field INPUT

```java
private static final String INPUT
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:19-19

##### field POLICY

```java
private static final String POLICY
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:20-20

##### field SOURCE

```java
private static final String SOURCE
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:21-21

##### field DOCUMENTS

```java
private static final List<String> DOCUMENTS
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:22-22

##### constructor DocumentShapeContract

```java
private DocumentShapeContract()
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:24-24

##### method hasTags

```java
static boolean hasTags(Map<String, List<String>> tags)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:26-28

##### method apply

```java
static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method, Function<String, Tree> parameterTree, Function<String, String> resolveName)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:30-171

```java
@SuppressWarnings("unchecked")
```

##### method concept

```java
static Map<String, Object> concept()
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:173-182

##### method sources

```java
private static Map<String, List<Object>> sources(String declaration, Map<String, List<String>> tags)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:184-208

##### method result

```java
private static void result(String declaration, Map<String, Object> method, String semantic, String tag, String raw)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:210-217

##### method parameters

```java
private static List<Map<String, Object>> parameters(Map<String, Object> method)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:219-222

```java
@SuppressWarnings("unchecked")
```

##### method parameter

```java
private static Map<String, Object> parameter(String declaration, Map<String, Object> method, String name)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:224-229

##### method attributes

```java
private static Map<String, String> attributes(String declaration, String raw)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:231-241

##### method requireKeys

```java
private static void requireKeys(String declaration, Map<String, String> attrs, String... keys)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:243-247

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:249-253

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.DocumentShapeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/DocumentShapeContract.java:255-257

#### class com.mongoplus.indexer.scanner.EntryContainerConstructionContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:15-205

```text
通用 document entry 构造及有序 typed List 收集；不依赖方法名、字段名或 Stage 名。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:16-16

##### field ENTRY_TAG

```java
static final String ENTRY_TAG
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:17-17

##### field CONTAINER_TAG

```java
static final String CONTAINER_TAG
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:18-18

##### field SOURCE_TAG

```java
static final String SOURCE_TAG
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:19-19

##### field EXPRESSION_CONCEPT

```java
private static final String EXPRESSION_CONCEPT
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:20-20

##### constructor EntryContainerConstructionContract

```java
private EntryContainerConstructionContract()
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:22-22

##### method declares

```java
static boolean declares(Map<String, List<String>> tags, String parameter)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:24-27

##### method apply

```java
static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method, Function<String, Tree> parameterTree, Function<String, String> resolveName, Map<String, Map<String, String>> genericDeclarations, Path repository, Consumer<String> concept)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:29-156

```java
@SuppressWarnings("unchecked")
```

##### method role

```java
private static Map<String, Object> role(int index, String input, String semantic, String concept, String javaType)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:158-161

##### method attributes

```java
private static Map<String, Map<String, String>> attributes(String declaration, Map<String, List<String>> tags, String tag, String... keys)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:164-181

```text
每项一次、固定属性集合；mechanism 的尾部说明允许空格。
```

##### method argumentIndex

```java
private static int argumentIndex(String declaration, String value)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:183-186

##### method raw

```java
private static String raw(Map<String, List<String>> tags, String tag, String name)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:188-190

##### method source

```java
private static Map<String, Object> source(String tag, String raw)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:192-194

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:196-200

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.EntryContainerConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/EntryContainerConstructionContract.java:202-204

#### class com.mongoplus.indexer.scanner.JavaTypeRelationEvidence

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:19-117

```text
从显式声明的 artifact 读取类型关系；赋值事实不授予语义角色或 codec 等价。
```

##### field TAG

```java
static final String TAG
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:20-20

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:21-21

##### constructor JavaTypeRelationEvidence

```java
private JavaTypeRelationEvidence()
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:23-23

##### method inspect

```java
static List<Object> inspect(Path repository, String declaration, List<String> declarations)
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:25-41

##### method inspect

```java
private static Map<String, Object> inspect(Path repository, String declaration, String raw, Map<String, String> attrs)
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:43-104

##### method classHash

```java
private static String classHash(JarFile archive, Class<?> type) throws Exception
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:106-110

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.JavaTypeRelationEvidence；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/JavaTypeRelationEvidence.java:112-116

#### class com.mongoplus.indexer.scanner.ObjectFieldBindingContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:12-139

```text
对象字段到方法参数的显式契约；编码和范围仅在参数语义要求时声明。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:13-13

##### field TAG

```java
static final String TAG
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:14-14

##### field SOURCE_TAG

```java
static final String SOURCE_TAG
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:15-15

##### field ATTRIBUTES

```java
private static final List<String> ATTRIBUTES
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:16-16

##### constructor ObjectFieldBindingContract

```java
private ObjectFieldBindingContract()
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:18-18

##### method apply

```java
static boolean apply(String declaration, List<String> tags, List<String> sources, Map<String, Object> method)
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:20-91

```java
@SuppressWarnings("unchecked")
```

##### method attributes

```java
private static Map<String, String> attributes(String declaration, String[] tokens)
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:93-108

##### method integerEncoding

```java
static void integerEncoding(String declaration, Map<String, String> attributes, Map<String, Object> binding)
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:111-124

```text
对象字段和 Stage body 共用同一精确 Int32 编码/范围校验，既有输出结构不变。
```

##### method bound

```java
private static long bound(String declaration, String value)
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:126-134

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.ObjectFieldBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/ObjectFieldBindingContract.java:136-138

#### class com.mongoplus.indexer.scanner.PipelineConstructionContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:15-220

```text
receiver 状态及命名管道容器的显式契约；不从 API 名称或 Stage 名称生成语义。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:16-16

##### field FACTORY

```java
static final String FACTORY
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:17-17

##### field EFFECT

```java
static final String EFFECT
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:18-18

##### field REPRESENTATION

```java
static final String REPRESENTATION
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:19-19

##### field INPUT

```java
static final String INPUT
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:20-20

##### field CONTAINER

```java
static final String CONTAINER
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:21-21

##### field TAGS

```java
private static final List<String> TAGS
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:22-22

##### constructor PipelineConstructionContract

```java
private PipelineConstructionContract()
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:24-24

##### method hasTags

```java
static boolean hasTags(Map<String, List<String>> tags)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:26-28

##### method apply

```java
static boolean apply(String declaration, String owner, boolean constructor, boolean staticMethod, Map<String, List<String>> tags, Map<String, Object> value)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:30-86

##### method elementType

```java
static String elementType(Tree type, Function<String, String> resolveName)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:89-113

```text
ELEMENT 只解开一层 AST 容器；元素类型用于后续构造结果的赋值兼容校验，不推断语义。
```

##### method validateExtractions

```java
static void validateExtractions(List<Object> types)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:116-144

```text
引用必须指向本次正式闭包中具备 representation 标签的真实方法。
```

##### method instanceStage

```java
private static void instanceStage(String declaration, boolean constructor, boolean staticMethod, Map<String, Object> value)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:146-151

##### method fixed

```java
private static Map<String, Object> fixed(String declaration, Map<String, List<String>> tags, String tag, int skip, String... attributes)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:153-175

##### method single

```java
private static String single(String declaration, Map<String, List<String>> tags, String tag)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:177-181

##### method parameter

```java
private static Map<String, Object> parameter(String declaration, Map<String, Object> value, String name, String semantic, String scope)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:183-197

```java
@SuppressWarnings("unchecked")
```

##### method parameters

```java
private static List<?> parameters(Map<String, Object> value)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:199-199

##### method result

```java
private static void result(String declaration, Map<String, Object> value, String semantic, String tag, String raw)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:201-207

##### method source

```java
private static Map<String, Object> source(String tag, String raw)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:209-215

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.PipelineConstructionContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/PipelineConstructionContract.java:217-219

#### class com.mongoplus.indexer.scanner.SourceScanner

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:44-1296

```text
使用 JDK Compiler Tree API 扫描配置范围内的公开源码 API。
```

##### field config

```java
private final MongoPlusIndexerConfig config
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:45-45

##### field classifier

```java
private final TypeClassifier classifier
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:46-46

##### field primaryFiles

```java
private final Set<Path> primaryFiles
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:47-47

##### field referencedFiles

```java
private final Set<Path> referencedFiles
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:48-48

##### constructor SourceScanner

```java
public SourceScanner(MongoPlusIndexerConfig config)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:50-50

##### method scan

```java
public MongoPlusApiIndex scan() throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:52-61

##### method collectPrimaryFiles

```java
private void collectPrimaryFiles() throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:63-75

##### method resolveRequiredTypes

```java
private void resolveRequiredTypes(List<SourceType> types)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:77-94

##### method locateImportedType

```java
private Path locateImportedType(List<SourceType> types, String simpleName)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:96-105

##### method locateKnownType

```java
private Path locateKnownType(String simpleName)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:107-111

##### method locateQualifiedName

```java
private Path locateQualifiedName(String name)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:113-124

##### method parse

```java
private List<SourceType> parse(List<Path> files) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:126-143

##### method parseUnit

```java
private void parseUnit(CompilationUnitTree unit, DocTrees docTrees, List<SourceType> result)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:145-184

##### method addConstructor

```java
private void addConstructor(SourceType type, MethodTree constructor, CompilationUnitTree unit, DocTrees docs)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:186-197

##### method visibility

```java
private static String visibility(Set<Modifier> modifiers)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:199-204

##### method addMethod

```java
private void addMethod(SourceType type, MethodTree method, CompilationUnitTree unit, DocTrees docs)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:206-227

##### method addEnumConstant

```java
private void addEnumConstant(SourceType type, VariableTree variable, CompilationUnitTree unit, DocTrees docs)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:229-243

##### method buildIndex

```java
private MongoPlusApiIndex buildIndex(List<SourceType> types, List<SourceType> referencedTypes)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:245-277

##### method family

```java
private Map<String, Object> family(String name, List<MethodRecord> records, List<SourceType> types)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:279-303

##### method overload

```java
private Map<String, Object> overload(MethodRecord record, List<SourceType> types)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:305-333

##### method inherits

```java
private boolean inherits(SourceType type, String target, List<SourceType> types, Set<String> visited)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:335-346

##### method wrapper

```java
private Map<String, Object> wrapper(SourceType type)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:348-375

##### method enumType

```java
private Map<String, Object> enumType(SourceType type)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:377-381

##### method addSpecialTypes

```java
private void addSpecialTypes(MongoPlusApiIndex index, List<SourceType> referencedTypes)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:383-410

##### method publicMethod

```java
private Map<String, Object> publicMethod(SourceType type, SourceMethod method)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:412-434

##### method isWrapper

```java
private boolean isWrapper(SourceType type)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:436-436

##### method hasDeprecated

```java
private boolean hasDeprecated(MethodTree method)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:437-440

##### method packagePath

```java
private static Path packagePath(String packageName)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:441-447

##### method simpleTree

```java
private static String simpleTree(Tree tree)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:448-448

##### method unquote

```java
private static String unquote(String value)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:449-452

##### method object

```java
private static Map<String, Object> object()
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:453-453

##### method documentation

```java
private Documentation documentation(DocTrees trees, CompilationUnitTree unit, Tree tree)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:455-469

##### method tags

```java
private Map<String, List<String>> tags(DocTrees trees, CompilationUnitTree unit, Tree tree)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:471-483

##### method normalize

```java
private static String normalize(String value)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:485-503

#### class com.mongoplus.indexer.scanner.SourceScanner.Documentation

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1289-1291

##### field description

```java
String description
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.Documentation；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1290-1290

##### field returnDescription

```java
String returnDescription
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.Documentation；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1290-1290

##### field parameters

```java
final Map<String, String> parameters
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.Documentation；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1290-1290

#### class com.mongoplus.indexer.scanner.SourceScanner.MethodRecord

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1292-1295

##### field type

```java
final SourceType type
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.MethodRecord；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1293-1293

##### field method

```java
final SourceMethod method
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.MethodRecord；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1293-1293

##### constructor MethodRecord

```java
MethodRecord(SourceType type, SourceMethod method)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.MethodRecord；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1294-1294

#### class com.mongoplus.indexer.scanner.SourceScanner.PipelineScan

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:507-1224

```text
沿 Stage 继承链、显式 Expression 入口及公开签名收集依赖，不扫描包。
```

##### field EXPRESSION_SEMANTIC

```java
private static final String EXPRESSION_SEMANTIC
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:508-508

##### field FIELD_REFERENCE_CONCEPT

```java
private static final String FIELD_REFERENCE_CONCEPT
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:509-509

##### field PARAMETER_TAG

```java
private static final String PARAMETER_TAG
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:510-510

##### field EXPRESSION_SHAPE_TAG

```java
private static final String EXPRESSION_SHAPE_TAG
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:511-511

##### field hasExpressionParameterEvidence

```java
private boolean hasExpressionParameterEvidence
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:512-512

##### field requiredCapabilities

```java
private final Set<String> requiredCapabilities
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:513-513

##### field stageParameterSemantics

```java
private final Set<String> stageParameterSemantics
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:514-514

##### field loaded

```java
private final Map<String, SourceType> loaded
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:515-515

##### field hierarchy

```java
private final Set<String> hierarchy
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:516-516

##### field dependencies

```java
private final Set<String> dependencies
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:517-517

##### field external

```java
private final Set<String> external
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:518-518

##### field families

```java
private final Map<String, List<MethodRecord>> families
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:519-519

##### method scan

```java
MongoPlusApiIndex scan() throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:521-721

##### method collectHierarchy

```java
private void collectHierarchy(String name) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:723-731

##### method select

```java
private void select(SourceType type, SourceMethod method) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:733-743

##### method mapping

```java
private Set<String> mapping(SourceType type, SourceMethod method, String tag)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:745-755

##### method methodReferences

```java
private void methodReferences(SourceType owner, SourceMethod method) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:757-761

##### method references

```java
private void references(SourceType owner, String signatureType) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:763-770

##### method resolve

```java
private String resolve(SourceType owner, String token) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:772-793

##### method load

```java
private SourceType load(String name) throws IOException
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:795-804

##### method evidence

```java
private Map<String, Object> evidence(SourceType owner, SourceMethod method)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:806-899

##### method constructorSemantics

```java
private void constructorSemantics(SourceType owner, Map<String, Object> type)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:902-922

```java
@SuppressWarnings("unchecked")
```

```text
构造器与普通方法复用相同的逐参数和组合提取器；不传播类级或相邻声明的标签。
```

##### method compositionSemantics

```java
private void compositionSemantics(SourceType owner, SourceMethod method, Map<String, Object> evidence)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:925-950

```text
组合声明只属于当前 overload；箭头右侧显式声明结果，不能补全缺失的参数证据。
```

##### method expressionShape

```java
private void expressionShape(SourceType owner, SourceMethod method, Map<String, Object> evidence)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:953-962

```text
只接受当前 overload 的显式 Javadoc 标记，不从方法名、参数或 MethodFamily 推断。
```

##### method parameterSemantics

```java
private void parameterSemantics(SourceType owner, SourceMethod method, Map<String, Object> evidence)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:965-1054

```java
@SuppressWarnings("unchecked")
```

```text
仅消费逐参数显式声明；泛型名、参数名和描述均不能产生聚合语义。
```

##### method resolveNumericType

```java
private String resolveNumericType(SourceType owner, String name)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1057-1071

```text
精确数值契约只接受已登记的标量表示，不能把同名 Number/Integer 类当作 JDK 类型。
```

##### method resolveElementType

```java
private String resolveElementType(SourceType owner, String name)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1074-1088

```text
新结构化元素仅解析登记的表示类型；不把同名类或未确认的 import 当作 Bson。
```

##### method resolveConstructionElementType

```java
private String resolveConstructionElementType(SourceType owner, String name)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1091-1098

```text
外部命名元素要求完整类型或显式 import，不猜测通配 import 中的类名。
```

##### method resolveEntryType

```java
private String resolveEntryType(SourceType owner, String name)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1101-1110

```text
entry 的外部类型要求完整名或显式 import；java.lang 表示单独解析。
```

##### method entryGeneric

```java
private Map<String, String> entryGeneric(String declaration, String owner)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1112-1117

##### method isExpressionElementContainer

```java
private boolean isExpressionElementContainer(SourceType owner, Map<String, Object> parameter)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1120-1130

```text
只校验显式 ELEMENT 标签的容器结构；容器类型本身不产生任何语义。
```

##### method expressionParameterConcept

```java
private Map<String, Object> expressionParameterConcept()
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1133-1213

```text
固定的已审计表示契约；只随显式参数 evidence 输出，不增加任何扫描入口或映射。
```

##### method semanticSource

```java
private Map<String, Object> semanticSource(String path, String symbols, String mechanism)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1215-1221

##### method qualified

```java
private String qualified(SourceType type)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.PipelineScan；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1223-1223

#### class com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1243-1262

##### field visibility

```java
final String visibility
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1244-1244

##### field implicit

```java
final boolean implicit
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1244-1244

##### field parameters

```java
final List<SourceParameter> parameters
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1245-1245

##### field tags

```java
final Map<String, List<String>> tags
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1246-1246

##### field doc

```java
Documentation doc
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1247-1247

##### constructor SourceConstructor

```java
SourceConstructor(String visibility, boolean implicit)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1248-1250

##### method implicitPublicNoArg

```java
static SourceConstructor implicitPublicNoArg()
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1251-1251

##### method signature

```java
String signature()
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceConstructor；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1252-1261

#### class com.mongoplus.indexer.scanner.SourceScanner.SourceMethod

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1263-1281

##### field name

```java
final String name
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1264-1264

##### field returnType

```java
final String returnType
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1264-1264

##### field parameters

```java
final List<SourceParameter> parameters
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1264-1264

##### field modifiers

```java
final Set<Modifier> modifiers
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1265-1265

##### field annotations

```java
final List<String> annotations
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1266-1266

##### field typeParameters

```java
final List<String> typeParameters
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1267-1267

##### field tags

```java
final Map<String, List<String>> tags
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1268-1268

##### field doc

```java
Documentation doc
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1269-1269

##### field defaultMethod

```java
boolean defaultMethod
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1269-1269

##### field staticMethod

```java
boolean staticMethod
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1269-1269

##### field deprecated

```java
boolean deprecated
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1269-1269

##### constructor SourceMethod

```java
SourceMethod(String name, String returnType)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1270-1270

##### method signature

```java
String signature()
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceMethod；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1271-1280

#### class com.mongoplus.indexer.scanner.SourceScanner.SourceParameter

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1282-1288

##### field name

```java
final String name
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceParameter；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1283-1283

##### field type

```java
final String type
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceParameter；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1283-1283

##### field varargs

```java
final boolean varargs
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceParameter；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1283-1283

##### field typeTree

```java
final Tree typeTree
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceParameter；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1284-1284

##### constructor SourceParameter

```java
SourceParameter(String name, String type, boolean varargs, Tree typeTree)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceParameter；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1285-1287

#### class com.mongoplus.indexer.scanner.SourceScanner.SourceType

可见性：private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1226-1242

##### field packageName

```java
final String packageName
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1227-1227

##### field name

```java
final String name
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1227-1227

##### field kind

```java
final String kind
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1227-1227

##### field imports

```java
final Map<String, String> imports
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1227-1227

##### field doc

```java
final Documentation doc
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1228-1228

##### field tags

```java
final Map<String, List<String>> tags
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1228-1228

##### field abstractType

```java
final boolean abstractType
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1229-1229

##### field parents

```java
final List<String> parents
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1230-1230

##### field typeParameters

```java
final List<String> typeParameters
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1231-1231

##### field annotations

```java
final List<String> annotations
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1232-1232

##### field methods

```java
final List<SourceMethod> methods
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1233-1233

##### field constructors

```java
final List<SourceConstructor> constructors
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1234-1234

##### field enumConstants

```java
final List<Object> enumConstants
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1235-1235

##### constructor SourceType

```java
SourceType(String packageName, String name, String kind, Map<String, String> imports, Documentation doc, Map<String, List<String>> tags, boolean abstractType)
```

所属类型：com.mongoplus.indexer.scanner.SourceScanner.SourceType；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:1236-1241

#### class com.mongoplus.indexer.scanner.StageParameterConcepts

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:14-317

```text
已经逐 Core/Driver 实现审计的参数表示；只由显式 mongoParam 标签引用。
```

##### field STAGE_BODY_ELEMENT_CONCEPT

```java
static final String STAGE_BODY_ELEMENT_CONCEPT
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:15-15

##### field BSON_TYPE

```java
private static final String BSON_TYPE
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:16-16

##### field CORE

```java
private static final String CORE
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:17-17

##### field SEMANTICS

```java
private static final List<String> SEMANTICS
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:18-22

##### constructor StageParameterConcepts

```java
private StageParameterConcepts()
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:24-24

##### method contains

```java
static boolean contains(String semantic)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:26-26

##### method conceptId

```java
static String conceptId(String semantic)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:28-28

##### method conceptId

```java
static String conceptId(String semantic, String scope)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:30-32

##### method isStageBodyElement

```java
static boolean isStageBodyElement(String semantic, String scope)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:34-36

##### method acceptsConcept

```java
static boolean acceptsConcept(String semantic, String scope, String reference)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:39-42

```text
新元素契约不借用既有 VALUE Concept 中的 Stage 包装行为。
```

##### method validateStageBodyElement

```java
static void validateStageBodyElement(Tree type, Function<String, String> resolveName)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:45-79

```text
只解开一层真实 AST 容器；上界仅证明 Java 表示，语义仍必须由 mongoParam 声明。
```

##### method acceptsConcept

```java
static boolean acceptsConcept(String semantic, String reference)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:81-85

##### method accepts

```java
static boolean accepts(String semantic, String type, String scope)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:88-128

```text
校验显式标签与 Java 表示兼容，不依据类型产生标签。
```

##### method concept

```java
static Map<String, Object> concept(String reference)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:130-297

##### method getter

```java
private static Map<String, Object> getter(String encoding)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:299-301

##### method name

```java
private static Map<String, Object> name()
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:303-306

##### method bson

```java
private static Map<String, Object> bson(String encoding)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:308-310

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.StageParameterConcepts；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageParameterConcepts.java:312-316

#### class com.mongoplus.indexer.scanner.StageValueBindingContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:18-192

```text
整个 Stage body 的显式值绑定；语义、范围和 singleton lifting 均不从 API 名称推断。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:19-19

##### field TAG

```java
static final String TAG
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:20-20

##### field SOURCE_TAG

```java
static final String SOURCE_TAG
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:21-21

##### field STRING_TYPE

```java
private static final String STRING_TYPE
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:22-22

##### constructor StageValueBindingContract

```java
private StageValueBindingContract()
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:24-24

##### method apply

```java
static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method, Function<String, Tree> parameterType, Function<String, String> resolveType)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:26-113

```java
@SuppressWarnings("unchecked")
```

##### method concept

```java
static Map<String, Object> concept()
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:115-125

##### method attributes

```java
private static Map<String, String> attributes(String declaration, String[] tokens)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:127-140

##### method requireKeys

```java
private static void requireKeys(String declaration, Map<String, String> attributes, String... keys)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:142-146

##### method size

```java
private static int size(String declaration, String raw)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:148-156

##### method sources

```java
private static List<Object> sources(String declaration, String parameter, List<String> sources)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:158-181

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:183-187

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.StageValueBindingContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/StageValueBindingContract.java:189-191

#### class com.mongoplus.indexer.scanner.VariableBindingScopeContract

可见性：package-private；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:13-245

```text
从逐声明契约连接变量条目、声明身份和词法环境；不按 API、Stage 或变量名称分支。
```

##### field CAPABILITY

```java
static final String CAPABILITY
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:14-14

##### field SCOPE_TAG

```java
static final String SCOPE_TAG
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:15-15

##### field SOURCE_TAG

```java
static final String SOURCE_TAG
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:16-16

##### field ENVIRONMENT_TAG

```java
static final String ENVIRONMENT_TAG
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:17-17

##### field EXPRESSION_CONCEPT

```java
private static final String EXPRESSION_CONCEPT
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:18-18

##### field VARIABLES_REFERENCE

```java
private static final String VARIABLES_REFERENCE
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:19-20

##### constructor VariableBindingScopeContract

```java
private VariableBindingScopeContract()
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:22-22

##### method hasTags

```java
static boolean hasTags(Map<String, List<String>> tags)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:24-26

##### method apply

```java
static boolean apply(String declaration, Map<String, List<String>> tags, Map<String, Object> method)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:29-123

```text
scope 只能消费已证明的 entry/container 和 body 字段；缺少任何依赖都不补全。
```

##### method audit

```java
private static List<Object> audit(String declaration, List<String> sources)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:125-138

##### method describeReference

```java
static void describeReference(Map<String, Object> reference, boolean bindingAvailable)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:141-152

```text
原有 String/BSON 编码保持不变；解析结果不能替代引用使用点的绑定校验。
```

##### method declarationIdentity

```java
private static Map<String, Object> declarationIdentity()
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:155-160

```text
身份取自输入结构，不能使用变量名称、随机数、对象地址或生成时间充当声明身份。
```

##### method concept

```java
static Map<String, Object> concept()
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：package-private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:162-184

##### method parameter

```java
private static Map<String, Object> parameter(String declaration, Map<String, Object> method, String name, String semantic, String scope)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:186-197

```java
@SuppressWarnings("unchecked")
```

##### method role

```java
private static Map<?, ?> role(String declaration, Map<?, ?> constructor, String input)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:199-205

##### method field

```java
private static Object field(String declaration, Map<?, ?> parameter)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:207-209

##### method contract

```java
private static Map<?, ?> contract(String declaration, Map<?, ?> value, String key)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:211-214

##### method attributes

```java
private static Map<String, String> attributes(String declaration, String raw, boolean trailingText, String... keys)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:216-230

##### method source

```java
private static Map<String, Object> source(String tag, String raw)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:232-234

##### method object

```java
private static Map<String, Object> object(Object... pairs)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:236-240

##### method invalid

```java
private static IllegalArgumentException invalid(String declaration, String reason)
```

所属类型：com.mongoplus.indexer.scanner.VariableBindingScopeContract；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/VariableBindingScopeContract.java:242-244

### 包：com.mongoplus.indexer.type

#### class com.mongoplus.indexer.type.TypeClassifier

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/type/TypeClassifier.java:4-59

```text
对方法签名中的类型做有界分类，防止引用扫描无限扩张。
```

##### method classify

```java
public String classify(String type)
```

所属类型：com.mongoplus.indexer.type.TypeClassifier；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/type/TypeClassifier.java:5-7

##### method classify

```java
public String classify(String type, String methodName, String parameterName, String declaringType, String parameterDescription)
```

所属类型：com.mongoplus.indexer.type.TypeClassifier；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/type/TypeClassifier.java:15-40

```text
* 结合泛型和调用上下文分类参数语义。
     *
     * <p>SFunction 的用途由泛型边界决定：包装器入参与返回值表示条件分组 Lambda，
     * MongoPlusBasicDBObject 入参与返回值表示文档构建 Lambda，其余 getter 形态表示字段引用。</p>
```

##### method isJdkType

```java
private boolean isJdkType(String type)
```

所属类型：com.mongoplus.indexer.type.TypeClassifier；可见性：private

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/type/TypeClassifier.java:42-50

##### method simpleName

```java
public static String simpleName(String type)
```

所属类型：com.mongoplus.indexer.type.TypeClassifier；可见性：public

源码：mongo-plus-indexer/src/main/java/com/mongoplus/indexer/type/TypeClassifier.java:52-58

