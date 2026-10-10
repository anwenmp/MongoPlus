# P0-04 第一阶段：通用候选语义等价性 evidence

日期：2026-10-09。范围：Core Javadoc、Indexer 契约及验证。基线 Index SHA-256：
`B2BC752C28F0D81DE29925C4286E49250FFD286288C7462114F979AEF2D29E0F`。
工作区原有 P0-01～P0-03 和 expression 改动保留。Core 无可执行源码/公开 API 变更；未修改 MCP。

## 1. 通用契约结构

新增独立版本化 capability `CANDIDATE_SEMANTICS_V1`。源码逐方法的 `@mongoCandidate` 和
`@mongoCandidateSource` 发布 `candidateSemantics`，包含：

| 字段 | 可验证事实与消费者义务 |
| --- | --- |
| proofStatus | ESTABLISHED 是当前节点的条件化构造证明；依赖缺失为 NOT_ESTABLISHED，且没有 bsonTerm |
| parameterBindings | 真实 Java AST 类型/泛型声明、元素类型、ARRAY/List/Collection、VARARGS/SINGLE、逐参数语义与来源 |
| applicability | 各 Java 路线可绑定、全部参数消费、完整子树和语义/scope 独立准入、合法输入域、同一不可变输入快照 |
| bsonTerm | 可递归代入的带类型 BSON 构造：键、值、顺序、固定值、精确编码、文档合并及单次 Stage 包装 |
| receiverEffect | 复用 APPEND_STAGE/RECEIVER/ONE/CALL_ORDER；纯工厂为 NONE/ZERO；比较对应逻辑 receiver 节点 |
| normalizations | 仅来源声明的 Java 输入构造规则；禁止将规则自动应用于原始 BSON 重写 |
| sourceEvidence | 当前 Core 实现和固定版本 Driver/Server 源码位置、真实符号及构造机制 |

复用 STAGE_VALUE_BINDING_V1、DOCUMENT_SHAPE_V1、DOCUMENT_REDUCTION_V1、
PIPELINE_CONSTRUCTION_V1 和既有 expression/variable evidence，不复制方法名到操作符映射。
通用操作为 STAGE_VALUE、DOCUMENT_ENTRY、DOCUMENT_MERGE、STAGE_DOCUMENT_INPUT、
FIELD_STAGE、EXPRESSION_ARRAY、PREFIXED_ENTRIES_STAGE。

`FIELD_STAGE` 的整数输入域取自声明的 `allowedInt32`；排序实例声明 `-1,1`，不是在生成器按
操作符硬编码。EXPRESSION_ARRAY 显式记录真实结果 org.bson.Document、CollectionCodec 和
runtime codec 义务。中间 BSON 文档角色仍由原有 binding/composition 检查，不能仅凭 BSON
相同将 SORT_SPECIFICATION 与 STAGE_BODY_DOCUMENT 互相绑定。

等价关系是：在两个完整调用树共同的合法具体输入域内，验证每条 Java 路线及所有节点/叶子/
scope/codec，再比较精确 BSON 类型、值、文档 entry 顺序、数组顺序，以及 receiver、Stage
数量和调用序。没有静态 family 等价类 ID，也没有按方法声明顺序选择候选。

## 2. 已证明的候选关系

正式发布 **41 个条件化构造规则**：Aggregate 16、Sorts 10、Projections 13、现行
conditions.operation.ConditionOperators 2。现行与 deprecated ConditionOperators 是独立类；
本轮没有给旧包副本新增 evidence。

| 候选 | 已证明范围与真实委托链 |
| --- | --- |
| skip(int)/skip(long) | 共同合法整数域 0..2147483647；long→Math.toIntExact→int→Driver BsonInt32；一个 Stage、同一 receiver |
| unset(String...)/unset(List<String>) | 相同非空、可合法绑定的有序 String 元素；varargs收集后委托List；一个元素String，多个Array |
| Sorts.asc/desc 的四条字段路线 | String List/varargs 与实际 getter提取后的List/varargs；同序实际字段名与固定Int32值相同，不允许构造虚假getter |
| Sorts.orderBy 两个 Bson容器路线 | 相同完整有序 SORT_SPECIFICATION 子树；CompoundSort按序合并；重键最后值、首次位置 |
| 旧 String sort/sortAsc/sortDesc 与新 sortSpecification | 相同字段、方向和一个Stage时完整树相同；含List/varargs同方向多字段；混合方向只在完整有序body范围比较 |
| multiply(Collection<?>)/multiply(Object...) | 相同元素顺序、实际类型和值，已证明容器/叶子codec；varargs复制ArrayList，Collection原值写Document |
| Projections.include/exclude 各四条字段路线 | 相同实际键/顺序与固定Int32 1/0；getter实际提取独立；不合并include与exclude |
| Projections.fields 两个Bson容器路线 | 相同有序body子树；重键remove→append，最后值与最后位置 |
| computed 两条输出键路线 | 实际String/getter输出键相同且完整expression、scope与编码闭合时相同 |
| group(TExpression,BsonField.../List) | id实际编码、有序BsonField名称与值完全相同；Driver先写_id再写条目；可包括已证明的null id |
| group(String) 与generic group空条目 | 相同String id和零条目时完整Stage相同；跨不同API/参数数目的树等价 |

group 非空条目的构造规则成立，并不提供任意 accumulator 的对象构造或表达式结果证明。
额外的 BsonField/accumulator 叶子仍须独立闭合；本轮构造测试使用明确提供的真实对象验证规则。

## 3. 不能合并或仍未闭合

- skip越界long不能通过Math.toIntExact；负值、浮点/错误Java类型不进入共同合法域。
- BSON Int32/Int64/Double/Boolean即使数值或服务器标志含义相似，也不严格等价。
- 单元素unset原始String与Array不等价；不能为历史输入改写BSON或放宽原始比较。
- asc/desc、include/exclude不同固定值，字段/operand/条目换序，拆分/合并Stage，变量绑定或
  inner/outer/sibling receiver变化，均不能合并。
- Sorts.orderBy与Projections.fields在无重键范围可有相同原始BSON，但角色不同；有重键时
  FIRST/LAST位置差异可直接产生不同BSON。不能以一次相同结果证明无条件通用互换。
- Sorts.orderBy(Order...)、旧Project的Projection对象路线没有补齐独立对象构造；group getter
  路线未新增完整转换/equality规则；multiply getter路线也没有并入通用expression元素。
- 旧sort(Bson)追加完整入参，不能拿排序body替换sortSpecification；raw group(Bson)亦无新树证明。
- 缺candidate规则、来源、参数、子expression、codec、变量scope或pipeline composition/effect
  的树不闭合。特定候选有条件化规则不等于任意应用输入已有可生成的唯一调用树。

## 4. 跨 API 完整调用树

支持通过构造项递归代入表达跨API整树等价。验证覆盖：旧单字段sort对新Sorts body→Stage、
group单参对generic空条目、include→computed(multiply)→fields→project的所有容器路线。
facet/lookup/unionWith 复用已有独立factory、representation、extraction/container与effect，
验证inner树对应逻辑receiver、调用序和sibling隔离。返回Children始终表示receiver，不冒充Bson。

此阶段发布关系和验证消费逻辑；生产代码没有候选搜索、唯一调用树选择、tie-break或MCP planner。

## 5. BSON 类型、顺序与规范化边界

严格比较使用真实BSON编码bytes；不能依赖忽略字段顺序的BsonDocument.equals。
数字类型、数组顺序和每个字段entry顺序全部保留。singleton压缩只发生在声明的Java输入路线，
normalizations记录rawBsonRewriteAllowed=false，不建立任意服务器语义归一化。

codec前提是审计的Driver 5.4默认codec或独立精确编码证明，还须固定registry、document class、
encoder context和输入快照。**同一registry仍不足**：为ArrayList注册自定义codec后，multiply
varargs与LinkedList Collection真实输出不同，反例已验证。任意自定义容器、opaque对象或
仅有BSON结果但缺runtime Java表示/codec的expression不能代入数组编码树。

来源核验：[Driver Aggregates/GroupStage](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/Aggregates.java)、
[BuildersHelper](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/client/model/BuildersHelper.java)、
[CollectionCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodec.java)、
[CollectionCodecProvider](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/CollectionCodecProvider.java)、
[默认registry](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/driver-core/src/main/com/mongodb/MongoClientSettings.java)、
[DocumentCodec](https://raw.githubusercontent.com/mongodb/mongo-java-driver/r5.4.0/bson/src/main/org/bson/codecs/DocumentCodec.java)、
[排序方向来源](https://raw.githubusercontent.com/mongodb/mongo/v8.0/src/mongo/db/query/sort_pattern.cpp)。

## 6. 正负测试与回归

Java21运行，Core保持Java8编译target：

```powershell
$env:JAVA_HOME='D:/Java/java21'
$env:Path="$env:JAVA_HOME/bin;$env:Path"
& 'D:/apache-maven-3.8.6/bin/mvn.cmd' -pl mongo-plus-core,mongo-plus-indexer -am test
```

根/annotation/Core/Indexer BUILD SUCCESS，**Core 76/76通过**，新
CandidateEquivalenceEncodingTest 9/9。新PipelineCandidateSemanticsSelfTest实际main通过：
**41 source-backed rules，35等价/结构检查，50不等价/拒绝检查**。包括正常/部分/非等价、
类型和顺序、输入域、深层缺证据、scope/receiver、非法属性/缺来源、改名不影响规则和
反转overload后完整Index证据相同。

**Indexer 20/20 个main回归全部通过**，最终状态见target/p0-04各日志及regression-result.json。
Surefire Tests run:0不计为main自测成功；全部main须单独运行。
旧入口映射夹具同步移除新的依赖标签以隔离其原验证范围；sort Integer参数独立审计表更新为
INTEGER_VALUE/VALUE，新增参数累计227。原来的拒绝、候选数量及BSON行为断言未放宽。

结构核验相对指定基线：85 families/300 overload、34 Stage/51 Expression、10主动类型/
91引用类型和全部旧public method surface保持；只增加candidate事实、六个已审计String排序
effect及一个sort Integer参数的显式语义。其他旧Index事实全部一致。五个Core源码快照去除
Javadoc后可执行文本完全相同。git diff --check通过。

未运行MongoDB服务端、全仓其他模块、MCP/Remote/真实模型验证。源码/codec构造验证不声称
这些输入已经由服务器执行，也不替代既有projection模式/路径/变量规则。

## 7. 业务特判

无按操作符或方法名的候选合并分支；无overload顺序、签名字典序优先级。输入方向值、Stage/
expression键、保留键、重复键位置、singleton和codec均来自当前方法正式源码声明。
固定外部表示类型校验（String、BsonField、SFunction、List/Collection）用于实际Java AST
类型边界，不建立按业务操作符选择的方法。

## 8. 正式 Index 与 SHA-256

同一源码通过正式CLI独立生成两次，无JSON后处理：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

两次及正式文件均 **2,948,916 UTF-8 bytes**，逐byte一致。SHA-256：

```text
477C7EEE5A6A8092AB138DEEE196942A79DDE7E667F8CAC5615AAB9C74BF81B4
```

正式文件：target/generated-resources/mongo-plus-pipeline-api-index.json；sha256伴随文件从实际
JSON自动重算。冻结副本target/p0-04/generation-1.json、generation-2.json；指定基线、字节比较、
旧证据保持和Core可执行文本核验记录在target/p0-04/baseline.json、structural-check.json。

## 9. 能否进入 MCP 通用候选选择验证

**可以进入已闭合输入/调用树范围内的通用候选等价性消费与选择验证。** 下游须固定此SHA，
识别CANDIDATE_SEMANTICS_V1，递归验证Java/语义/类型/scope/codec/effect全部义务，按完整
具体树建立等价类。未知capability应拒绝；未闭合的Order/Projection/accumulator/variable/
codec不能借本能力恢复历史用例。非等价候选不能合并。

本能力没有规定等价类内唯一代表的选择政策；MCP若需输出唯一调用树仍须另行验证通用政策。
本次没有实现或运行MCP候选选择，也没有声称SELECTED/complete=true或远端Index已部署。
