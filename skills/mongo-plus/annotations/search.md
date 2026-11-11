# 注解：search

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：MongoSearchField、MongoSearchFields、MongoSearchIndex、MongoSearchIndexes、MongoSearchSort、MongoSearchSorts、MongoSearchStoredSource、MongoSearchSynonym。

## MongoSearchField

`import com.mongoplus.annotation.index.search.MongoSearchField;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；@Repeatable(MongoSearchFields.class)。
- 实际用途：Search静态字段映射及类型专属选项，可重复。
- 生效条件：SearchParse读取字段或MongoSearchIndex.fields。
- 限制与常见错误：默认STRING不按Java类型推断；多索引需name；嵌套用fields JSON，不把点路径当嵌套映射；dimensions已弃用，见完整属性表。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `""` | 索引名称 用于区分所属搜索索引，不属于字段映射 JSON 的属性。 实体类只有一个 `MongoSearchIndex` 时，留空自动归属该索引；有多个时必须填写。 类上声明了索引时，显式名称必须匹配其中一个索引；空索引名可用 default 引用。 未声明类级索引时，保留按名称分组创建默认配置的行为。 在 `MongoSearchIndex#fields()` 中配置时应留空，由外层索引确定归属。 |
| `fieldName` | `String` | `""` | 字段名称 标注在实体字段上时，空值表示使用该字段映射后的 MongoDB 字段名。 如果此注解写在`MongoSearchIndex#fields()`中, 则此字段必填 显式名称使用实际 MongoDB 字段名，不能以 $ 开头。 嵌套字段通过父字段的 `#fields()` 配置，不能使用点路径代替嵌套映射。 |
| `type` | `SearchFieldType` | `SearchFieldType.STRING` | 搜索字段类型，默认 string，不根据 Java 字段类型推断。 例如数值字段应显式指定 NUMBER，对象数组应根据查询语义选择 DOCUMENT 或 EMBEDDED_DOCUMENTS。 |
| `exclude` | `boolean` | `false` | 排除字段，对应 `"字段名": []`，可用于覆盖动态映射。 为 true 时只使用 name、fieldName，不输出 type 或其他选项； 同一索引的同一字段不能同时声明排除和其他映射。 |
| `dynamic` | `boolean` | `false` | document、embeddedDocuments：是否启用嵌套字段动态映射，默认 false。 未启用动态映射且未指定 dynamicTypeSet 时，必须通过 fields 指定嵌套字段。 |
| `dynamicTypeSet` | `String` | `""` | document、embeddedDocuments：动态类型集名称，对应 dynamic.typeSet。 引用 `MongoSearchIndex#typeSets()` 中的名称；指定时 dynamic 应保持 false， 输出对象形式而不是布尔形式。 |
| `fields` | `String` | `""` | document、embeddedDocuments：嵌套字段映射 JSON 对象，空字符串表示未配置。 使用实际 MongoDB 字段名；对象值可为单个定义、多个定义的数组或用于排除字段的空数组。 例：`{"title":{"type":"string"},"items":{"type":"document","dynamic":true}}` Java 注解不允许循环成员类型，因此使用 JSON 表达任意深度的嵌套映射。 |
| `analyzer` | `Analyzer` | `Analyzer.DEFAULT` | string、autocomplete：索引分析器，DEFAULT 表示省略，由 MongoDB 决定继承及默认行为。 不得输出 lucene.default。autocomplete 不支持 lucene.kuromoji。 |
| `customAnalyzer` | `String` | `""` | string、autocomplete：自定义索引分析器名称，引用 `MongoSearchIndex#analyzers()`。 指定时 analyzer 应保持 DEFAULT。autocomplete 不支持使用 nGram、edgeGram tokenizer， 以及 daitchMokotoffSoundex、nGram、edgeGram、shingle token filter 的自定义分析器。 |
| `searchAnalyzer` | `Analyzer` | `Analyzer.DEFAULT` | string：查询分析器，DEFAULT 表示省略，不得输出 lucene.default。 未指定时依次使用字段 analyzer、索引 searchAnalyzer、索引 analyzer、lucene.standard。 |
| `customSearchAnalyzer` | `String` | `""` | string：自定义查询分析器名称，引用 `MongoSearchIndex#analyzers()`。 指定时 searchAnalyzer 应保持 DEFAULT。 |
| `indexOptions` | `SearchIndexOptions` | `SearchIndexOptions.OFFSETS` | string：索引保存的信息量，默认 offsets。 phrase、span 至少需要 positions；highlight 需要 offsets 且 store=true。 |
| `store` | `boolean` | `true` | string：是否在搜索索引中保存原始文本，默认 true；highlight 要求 true。 与 storedSource 的源文档存储配置不同。 |
| `ignoreAbove` | `int` | `-1` | string：允许索引的字段值最大字符数，超过时不索引整个值；-1 表示未指定。 |
| `norms` | `SearchNorms` | `SearchNorms.INCLUDE` | string：是否在评分时考虑字段长度，默认 include。 |
| `similarity` | `SearchSimilarity` | `SearchSimilarity.NONE` | string、autocomplete：文本评分算法，对应对象 `similarity: {"type":"bm25"}`。 NONE 表示省略该选项，使用服务端默认 bm25；不能输出空的 similarity.type。 向量字段使用 `#vectorSimilarity()`，不能将文本算法写成向量 similarity 字符串。 |
| `multi` | `String` | `""` | string：备用字符串映射 JSON 对象，键为查询时使用的 multi 名称。 例：`{"english":{"type":"string","analyzer":"lucene.english"}}` 用于同一字符串的多分析器配置；不同字段类型应使用重复注解，不使用 multi。 |
| `tokenization` | `SearchTokenization` | `SearchTokenization.NONE` | autocomplete：分词策略，NONE 表示省略，服务端默认 edgeGram。 |
| `minGrams` | `int` | `2` | autocomplete：最小 gram 长度，默认 2，必须为正数且不大于 maxGrams。 |
| `maxGrams` | `int` | `15` | autocomplete：最大 gram 长度，默认 15。 较大值会增加索引开销，超过该长度的查询词元会被截断。 |
| `foldDiacritics` | `boolean` | `true` | autocomplete：是否折叠变音符号，默认 true，例如允许 cafe 匹配 café。 |
| `normalizer` | `SearchNormalizer` | `SearchNormalizer.NONE` | token：字符串规范化方式，默认 none；lowercase 用于忽略大小写。 |
| `representation` | `SearchRepresentation` | `SearchRepresentation.DOUBLE` | number、旧 numberFacet：数值表示方式，默认 double。 int64 可保留大整数精度，但会将 double 四舍五入为整数，且不能索引超出范围的大 double。 |
| `indexIntegers` | `boolean` | `true` | number、旧 numberFacet：是否索引 int32、int64，默认 true。 与 indexDoubles 至少一个为 true。 |
| `indexDoubles` | `boolean` | `true` | number、旧 numberFacet：是否索引 double，默认 true。 与 indexIntegers 至少一个为 true。 |
| `indexShapes` | `boolean` | `false` | geo：是否索引线、多边形等形状，默认 false，仅索引点。 |
| `storedSource` | `String` | `""` | embeddedDocuments：源字段存储配置，JSON 布尔值或 include/exclude 对象。 空字符串表示省略，MongoDB 默认 false；include 和 exclude 不能同时指定。 路径相对于当前嵌套文档，例：`{"include":["title"]}`。 |
| `numDimensions` | `int` | `-1` | vector：向量维数，使用该类型时必须指定 1～8192；-1 表示未配置。 binary 自动量化或 int1 向量要求维数为 8 的倍数；旧 knnVector 使用 dimensions。 |
| `dimensions` | `int` | `-1` | 旧 knnVector：向量维数，使用该类型时必须指定 1～8192；-1 表示未配置。 @deprecated 新定义使用 VECTOR 和 numDimensions。 |
| `vectorSimilarity` | `SearchVectorSimilarity` | `SearchVectorSimilarity.DEFAULT` | vector、旧 knnVector：向量相似度，对应 similarity 字符串。 DEFAULT 表示未配置，不能输出为空字符串；向量映射必须显式选择算法。 |
| `quantization` | `SearchQuantization` | `SearchQuantization.EMPTY` | vector：自动量化方式，EMPTY 表示省略，服务端默认 none；NONE 表示显式输出 none。 自动量化仅用于 float/double 向量。 预量化输入应使用 none；binary 要求 numDimensions 为 8 的倍数。 |
| `indexingMethod` | `SearchIndexingMethod` | `SearchIndexingMethod.NONE` | vector：索引结构，NONE 表示省略，服务端默认 hnsw；flat 使用平面索引，不配置 hnswOptions。 |
| `hnswOptions` | `String` | `""` | vector：HNSW 构建参数 JSON 对象，空字符串表示使用服务端默认值。 仅用于 hnsw；maxEdges 范围 16～64，默认 16； numEdgeCandidates 范围 100～3200，默认 100。 例：`{"maxEdges":32,"numEdgeCandidates":200}`。 此选项在官方文档中标为 Preview，需核对目标部署的支持情况。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;

@CollectionName("example_mongosearchfield") @MongoSearchIndex(name="articles") public class MongoSearchFieldExample { @MongoSearchField String title; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchField.java#L27)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchFields

`import com.mongoplus.annotation.index.search.MongoSearchFields;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：多Search字段映射容器。
- 生效条件：与MongoSearchField相同。
- 限制与常见错误：同一字段可多类型；排除映射不能与其他类型混用；读取需getAnnotationsByType。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `MongoSearchField[]` | `必填（无默认值）` | 字段映射定义；同一索引的同一字段可声明不同搜索类型。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchFields;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.enums.SearchFieldType;

@CollectionName("example_mongosearchfields") @MongoSearchIndex(name="articles") public class MongoSearchFieldsExample { @MongoSearchFields({@MongoSearchField(type=SearchFieldType.STRING),@MongoSearchField(type=SearchFieldType.TOKEN)}) String title; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchFields.java#L13)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchIndex

`import com.mongoplus.annotation.index.search.MongoSearchIndex;`

- 作用域：`@Target({ElementType.TYPE})`；保留策略：RUNTIME；@Repeatable(MongoSearchIndexes.class)。
- 实际用途：普通Search索引配置，可多次声明。
- 生效条件：Resolver/SearchParse及自动索引扫描；支持Search的部署。
- 限制与常见错误：默认dynamic=false；静态字段或dynamic需明确；名称与Vector共享同一集合命名空间；创建是异步。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `"default"` | 索引名称，默认 default；显式为空时不向 Driver 指定名称，服务端仍使用 default。 同一类中的索引名称必须唯一，空名称与 default 视为同名。 类上只有一个索引时，实体字段注解的 name 可省略；多个索引时必须填写所属索引名称。 索引名称属于创建索引的参数，不属于索引定义中的 definition。 |
| `analyzer` | `Analyzer` | `Analyzer.DEFAULT` | 指定在建立索引时应用于字符串字段的分析器 字段未单独指定分析器时使用该配置，字段级配置优先。 `Analyzer#DEFAULT` 表示不显式指定，由 MongoDB 使用 lucene.standard， 不应将 lucene.default 写入索引定义。 |
| `fields` | `MongoSearchField[]` | `{}` | 静态字段映射，对应 mappings.fields 可在此数组中配置，也可将 `MongoSearchField` 标注在实体字段上。 数组中的 fieldName 必填，name 留空；同名字段的多个定义组成映射数组。 嵌套映射使用 `MongoSearchField#fields()`。 |
| `sort` | `MongoSearchSort[]` | `{}` | 建立索引时的预排序配置，对应 definition.sort。 可在此数组中配置，也可将 `MongoSearchSort` 标注在实体字段上。 两处均未声明时省略 sort；数组中的 fieldName 必填，name 留空。 数组顺序决定排序优先级，忽略数组元素的 priority； 字段注解按 priority 升序、同优先级按实际字段路径字典序排列，追加到数组配置之后。 同一索引内重复的排序路径应报配置错误，不能覆盖或重复输出。 noData 未配置时输出字段对应的 1 或 -1； 显式配置时输出包含 order、noData 的对象。 排序字段必须静态映射为 boolean、date、number、objectId、token 或 uuid； 多类型映射至少包含其中一种。仅通过 dynamic 或 typeSet 映射的排序字段会导致索引构建失败。 嵌套路径可使用点号，但所有父字段必须具有静态 document 映射； 仅有 embeddedDocuments 映射不能满足排序要求。不能使用 searchScore 元表达式。 每个索引仅配置一组 sort，可包含多个字段；查询仍需指定匹配此配置或其前缀的 $search.sort 才能利用预排序，可通过 explain 中的 usesIndexSort 核实。 |
| `customAnalyzer` | `String` | `""` | 建立索引时使用的自定义分析器名称，为空时使用 `#analyzer()` 名称需与 `#analyzers()` 中定义的 name 一致。 使用该配置时，`#analyzer()` 应保持为 `Analyzer#DEFAULT`。 |
| `searchAnalyzer` | `Analyzer` | `Analyzer.DEFAULT` | 指定在搜索前应用于查询文本的分析器 `Analyzer#DEFAULT` 表示不显式指定，默认使用索引的 analyzer； 两者均未指定时使用 lucene.standard。 不应将 lucene.default 写入索引定义。 |
| `customSearchAnalyzer` | `String` | `""` | 查询文本使用的自定义分析器名称，为空时使用 `#searchAnalyzer()` 名称需与 `#analyzers()` 中定义的 name 一致。 使用该配置时，`#searchAnalyzer()` 应保持为 `Analyzer#DEFAULT`。 |
| `analyzers` | `String[]` | `{}` | 自定义分析器定义，每个值为一个 JSON 对象，默认不配置 每个对象必须包含 name 和 tokenizer，可选配置 charFilters、tokenFilters。 name 在索引内必须唯一，且不能以 lucene.、builtin. 或 mongodb. 开头。 例：`{"name":"myAnalyzer","tokenizer":{"type":"standard"}}` |
| `dynamic` | `boolean` | `false` | 是否动态映射字段，对应 mappings.dynamic，默认 false 设为 true 时递归索引所有支持动态映射的字段类型；设为 false 时不动态索引字段。 未配置 `#dynamicTypeSet()` 且该值为 false 时，必须通过 `#fields()` 或实体字段上的 `MongoSearchField` 指定静态索引字段。 `#fields()` 中显式指定的字段配置优先于动态映射。 |
| `dynamicTypeSet` | `String` | `""` | 动态映射使用的类型集名称，对应 mappings.dynamic.typeSet，默认不配置 名称需与 `#typeSets()` 中定义的 name 一致，用于递归索引类型集指定的字段类型。 该配置使用 mappings.dynamic 的对象形式，与布尔形式互斥；使用时 `#dynamic()` 应保持默认值。 |
| `numPartitions` | `int` | `1` | 索引分区数量，对应 definition.numPartitions，默认 1，仅支持 1、2、4。 1 表示单分区；2、4 分别最多划分为两个、四个分区，仍是同一个搜索索引。 使用额外分区需要独立部署的 Search Nodes；该配置不用于设置数据库分片数量。 每分区最多支持二十亿个索引对象；嵌套 embeddedDocuments 也会增加索引对象， 容量不能仅按集合的顶层文档数估算。 配置或修改分区数量会触发索引重建；存在多个分区时不能移除所有 Search Nodes 并切换到 mongod、mongot 同节点的部署模式。 分区查询由 MongoDB Search 分发并合并结果，查询级 concurrent 选项需在查询中另行设置。 |
| `storedSource` | `MongoSearchStoredSource` | `@MongoSearchStoredSource` | 源文档字段存储配置，对应 definition.storedSource，默认不存储源文档。 通过嵌套注解配置布尔形式或 include/exclude 对象形式，三种方式互斥。 例：`@MongoSearchStoredSource(include = {"title", "publishedAt"})`， 或 `@MongoSearchStoredSource(exclude = {"content"})`，全量存储使用 `@MongoSearchStoredSource(true)`。 存储字段不等于为字段建立索引，查询时可通过 returnStoredSource 返回存储的字段。 仅适用于 MongoDB 7.0 及以上版本；索引包含 vector 类型字段时，不能使用 true，需排除该字段。 |
| `synonyms` | `MongoSearchSynonym[]` | `{}` | 同义词映射定义，对应 definition.synonyms，空数组表示不配置。 每个映射必须包含唯一且非空的 name、同数据库的 source.collection 和 analyzer。 分析器配置及限制见 `MongoSearchSynonym`。 例：`@MongoSearchSynonym(name = "mySynonyms", sourceCollection = "synonyms")`。 免费 M0 集群中，一个索引最多配置一个映射，来源集合最多包含一万条文档。 Atlas Flex 同样使用单个映射；多映射需要 M10 或更高等级。 本地解析器不识别部署等级，具体配额由服务端检查。 此配置不创建来源集合或写入同义词；查询需通过 text 或 phrase 引用映射名称。 来源集合的合法变更由 MongoDB Search 异步更新到同义词映射。 |
| `typeSets` | `String[]` | `{}` | 动态映射类型集定义，每个值为一个 JSON 对象，默认不配置 每个对象必须包含 name 和 types，types 中每个对象必须通过 type 指定要动态索引的字段类型。 同一类型集不能重复定义相同 type；不能包含 document、embeddedDocuments、vector 或已废弃的类型。 通过 `#dynamicTypeSet()` 引用类型集，也可在 `#fields()` 的嵌套字段映射中引用。 例：`{"name":"myTypeSet","types":[{"type":"string"},{"type":"number"}]}` |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;

@CollectionName("example_mongosearchindex") @MongoSearchIndex(name="articles", fields={@MongoSearchField(fieldName="title")}) public class MongoSearchIndexExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchIndex.java#L14)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchIndexes

`import com.mongoplus.annotation.index.search.MongoSearchIndexes;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：多Search索引容器。
- 生效条件：与MongoSearchIndex相同。
- 限制与常见错误：索引名不能重复；隔离字段/分析器/排序等各自配置。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `MongoSearchIndex[]` | `必填（无默认值）` | 名称各不相同的搜索索引，按声明顺序解析。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchIndexes;

@CollectionName("example_mongosearchindexes") @MongoSearchIndexes({@MongoSearchIndex(name="a",dynamic=true),@MongoSearchIndex(name="b",dynamic=true)}) public class MongoSearchIndexesExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchIndexes.java#L13)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchSort

`import com.mongoplus.annotation.index.search.MongoSearchSort;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；@Repeatable(MongoSearchSorts.class)。
- 实际用途：Search索引预排序字段，可重复。
- 生效条件：嵌入MongoSearchIndex.sort或字段；按name归属。
- 限制与常见错误：不替代静态字段映射；重复路径与priority顺序需核对；高级功能依部署。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `""` | 所属搜索索引名称，与 MongoSearchIndex.name 匹配。 类上只有一个索引时可留空，自动归属该索引；多个索引时必须填写，且必须匹配已声明索引。 空索引名可用 default 引用；未声明类级索引时，按名称分组使用默认配置。 在 MongoSearchIndex.sort 中使用时应留空，由外层索引确定归属； 此参数不写入排序 JSON。 |
| `fieldName` | `String` | `""` | 实际 MongoDB 排序字段路径。 在 MongoSearchIndex.sort 中使用时必填；标注在实体字段上时， 空值表示使用该字段映射后的 MongoDB 字段名。 嵌套路径允许点号，但所有父字段必须具有静态 document 映射。 |
| `order` | `IndexDirection` | `IndexDirection.ASC` | 排序方向，默认升序；ASC 对应 1，DESC 对应 -1。 |
| `noData` | `SearchSortNoData` | `SearchSortNoData.DEFAULT` | null、缺失值的比较位置，DEFAULT 表示省略 noData，使用服务端默认行为。 LOWEST、HIGHEST 分别将其视为最低值、最高值，最终位置取决于升降序； 例如 DESC 配合 LOWEST 将其放在末尾。 |
| `priority` | `int` | `0` | 实体字段上的排序优先级，数值越小越靠前，默认 0；不写入排序 JSON。 同优先级按解析后的 MongoDB 字段路径字典序排列，避免依赖反射返回字段的顺序。 在 MongoSearchIndex.sort 数组内使用时忽略此参数，以数组顺序为准； 两种声明并用时先放数组中的配置，再追加按优先级排列的字段配置。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.enums.SearchFieldType;

@CollectionName("example_mongosearchsort") @MongoSearchIndex(name="articles",fields={@MongoSearchField(fieldName="title",type=SearchFieldType.TOKEN)},sort={@MongoSearchSort(fieldName="title")}) public class MongoSearchSortExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchSort.java#L21)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchSorts

`import com.mongoplus.annotation.index.search.MongoSearchSorts;`

- 作用域：`@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：多Search预排序配置容器。
- 生效条件：字段配置按索引名分组。
- 限制与常见错误：同一索引不允许重复排序同一路径。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `MongoSearchSort[]` | `必填（无默认值）` | 同一实体字段在不同搜索索引中的排序配置。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchIndexes;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.annotation.index.search.MongoSearchSorts;
import com.mongoplus.enums.SearchFieldType;

@CollectionName("example_mongosearchsorts") @MongoSearchIndexes({@MongoSearchIndex(name="a",fields={@MongoSearchField(fieldName="title",type=SearchFieldType.TOKEN)}),@MongoSearchIndex(name="b",fields={@MongoSearchField(fieldName="title",type=SearchFieldType.TOKEN)})}) public class MongoSearchSortsExample { @MongoSearchSorts({@MongoSearchSort(name="a"),@MongoSearchSort(name="b")}) String title; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchSorts.java#L13)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchStoredSource

`import com.mongoplus.annotation.index.search.MongoSearchStoredSource;`

- 作用域：`@Target({})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：Search返回源字段存储配置。
- 生效条件：仅嵌入MongoSearchIndex.storedSource。
- 限制与常见错误：@Target({})不能直接标类/字段；全量value和include/exclude互斥；不代替字段索引。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `boolean` | `false` | 是否存储完整源文档，对应 storedSource 的布尔形式，默认 false。 include、exclude 均为空时，true 表示存储所有字段，false 表示不存储源文档。 两者之一非空时使用对象形式，此值必须保持 false。 索引定义包含 vector 类型字段时不能设为 true，应通过 include 选择非向量字段， 或通过 exclude 排除向量字段。存储完整文档会增加索引和查询开销。 |
| `include` | `String[]` | `{}` | 仅存储指定源字段，对应 storedSource.include；默认不配置。 使用实际 MongoDB 字段名或点分隔路径，例如 title、awards.wins；不执行 $field 转换。 MongoDB 还会默认存储 _id。若仅需 _id，可显式配置 include={"_id"}。 不能与 exclude 或 value=true 同时配置；路径不能为 null、空字符串或空白字符串。 索引包含 vector 类型时，不能选择需要排除的向量字段或包含该字段的整个父对象。 |
| `exclude` | `String[]` | `{}` | 存储除指定字段外的其他源字段，对应 storedSource.exclude；默认不配置。 使用实际 MongoDB 字段名或点分隔路径；可显式排除 _id。 不能与 include 或 value=true 同时配置，路径不能为空白。 索引定义包含 vector 类型字段时，必须排除这些向量字段或其整个父对象。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;

@CollectionName("example_mongosearchstoredsource") @MongoSearchIndex(dynamic=true,storedSource=@MongoSearchStoredSource(include={"title"})) public class MongoSearchStoredSourceExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchStoredSource.java#L17)。后续实现与注册见[专题](../references/search-vector.md)。

## MongoSearchSynonym

`import com.mongoplus.annotation.index.search.MongoSearchSynonym;`

- 作用域：`@Target({})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：引用已有同义词来源集合。
- 生效条件：仅嵌入MongoSearchIndex.synonyms；来源集合由业务准备。
- 限制与常见错误：@Target({})；不会写入同义词文档；分析器及部署档位另确认。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `name` | `String` | `必填（无默认值）` | 同义词映射名称，对应 synonyms 中的 name，必填且不能为空白。 同一索引内必须唯一；该名称不是索引名称，也不是来源集合名。 |
| `sourceCollection` | `String` | `必填（无默认值）` | 同义词来源集合名，对应 source.collection，必填且不能为空白。 集合必须与被索引集合位于同一数据库；不能通过此参数指定其他数据库。 集合文档须符合官方 equivalent 或 explicit 同义词格式。 |
| `analyzer` | `Analyzer` | `Analyzer.DEFAULT` | 同义词分析器，对应必填的 analyzer。 DEFAULT 且未配置 customAnalyzer 时解析为 lucene.standard，并显式输出 analyzer； 不能省略该必填属性，也不能输出 lucene.default。 不支持 CJK、KUROMOJI。目标搜索字段必须使用相同分析器。 |
| `customAnalyzer` | `String` | `""` | 自定义同义词分析器名称，引用 `MongoSearchIndex#analyzers()` 中的定义。 指定时 analyzer 保持 DEFAULT，使用该名称作为必填 analyzer 的值。 不能使用 nGram、edgeGram tokenizer，或 daitchMokotoffSoundex、nGram、 edgeGram、shingle、wordDelimiterGraph token filter。 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSynonym;

@CollectionName("example_mongosearchsynonym") @MongoSearchIndex(dynamic=true,synonyms={@MongoSearchSynonym(name="terms",sourceCollection="synonyms")}) public class MongoSearchSynonymExample {}
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchSynonym.java#L21)。后续实现与注册见[专题](../references/search-vector.md)。
