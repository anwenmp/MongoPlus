# MongoDB Search 字段映射配置

`MongoSearchIndex`、`MongoSearchField` 位于 `com.mongoplus.annotation.index.search`，
配置枚举位于 `com.mongoplus.enums`。本文依据 2026-10-20 核对的
[字段映射文档](https://www.mongodb.com/docs/search/indexes/define-field-mappings/)及各类型子文档。

**当前状态（2026-10-21）：`SearchParse` 已将注解转换为 Driver 的 `SearchIndexModel`，
可通过 `new Resolver().resolve(entityClass).getSearchIndexModels()` 取得解析结果。
该过程不访问数据库；现有 `AutoUtil` 自动创建链路仍只创建普通索引，搜索索引服务端验证为 `NOT_RUN`。**

## 类型与适用选项

`type` 默认为 `SearchFieldType.STRING`，不根据 Java 类型推断。数组填写元素的搜索类型，
没有 `ARRAY` 类型。各类型只应输出对应的选项，不能把整个注解的所有默认值序列化到每种类型中。

| 搜索类型 | 配置项 | 默认或约束 |
|---|---|---|
| `string` | `analyzer`、`customAnalyzer`、`searchAnalyzer`、`customSearchAnalyzer`、`indexOptions`、`store`、`ignoreAbove`、`norms`、`similarity`、`multi` | `offsets`、`true`、未设长度限制、`include`、`bm25` |
| `autocomplete` | `analyzer`、`customAnalyzer`、`tokenization`、`minGrams`、`maxGrams`、`foldDiacritics`、`similarity` | `edgeGram`、`2`、`15`、`true`、`bm25` |
| `token` | `normalizer` | `none`，可选 `lowercase` |
| `number` | `representation`、`indexIntegers`、`indexDoubles` | `double`、`true`、`true`；两种数值不能同时关闭 |
| `document` | `dynamic`、`dynamicTypeSet`、`fields` | 动态映射关闭时必须配置嵌套字段 |
| `embeddedDocuments` | `dynamic`、`dynamicTypeSet`、`fields`、`storedSource` | 对象数组按元素匹配；源字段存储独立配置 |
| `geo` | `indexShapes` | `false` 时只索引点 |
| `vector` | `numDimensions`、`vectorSimilarity`、`quantization`、`indexingMethod`、`hnswOptions` | 维数与相似度必填；默认 `none` 量化、`hnsw` 索引 |
| `boolean`、`date`、`objectId`、`uuid` | `type` | 不附带其他类型的专属参数 |

为表达旧索引，`SearchFieldType` 也保留标记为 `@Deprecated` 的 `DATE_FACET`、`NUMBER_FACET`、
`STRING_FACET`、`KNN_VECTOR`。新定义分别选 `DATE`、`NUMBER`、`TOKEN`、`VECTOR`。
旧 `numberFacet` 使用数值选项；旧 `knnVector` 使用 `dimensions` 和 `vectorSimilarity`。

## 同一字段声明多个类型

```java
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.enums.SearchFieldType;
import com.mongoplus.enums.SearchNormalizer;

@MongoSearchIndex(name = "catalog")
class Product {
    @MongoSearchField(name = "catalog", type = SearchFieldType.STRING)
    @MongoSearchField(name = "catalog", type = SearchFieldType.TOKEN,
            normalizer = SearchNormalizer.LOWERCASE)
    private String title;
}
```

此声明的字段映射契约为：

```json
{
  "title": [
    {"type": "string"},
    {"type": "token", "normalizer": "lowercase"}
  ]
}
```

静态映射会覆盖该字段的动态映射，所以需要两个类型时必须分别声明。
`@Repeatable` 的容器是 `MongoSearchFields`；解析器使用
`getAnnotationsByType(MongoSearchField.class)` 读取字段上的全部声明。

标注在实体字段上时，`fieldName=""` 表示使用映射后的 MongoDB 字段名。
`name` 表示所属索引，不属于字段定义 JSON。
在 `MongoSearchIndex.fields` 数组中使用时，`fieldName` 必填，`name` 留空。
实体字段上的 `MongoSearchField` 和 `MongoSearchSort` 共用以下索引归属规则：

| 类上 MongoSearchIndex 数量 | 字段注解的 name |
|---|---|
| 1 个 | 可省略，自动归属唯一索引；显式填写时必须匹配该索引 |
| 多个 | 必填，且必须匹配其中一个索引 |
| 0 个 | 按名称分组，各组使用默认配置，保留仅使用字段注解的能力 |

存在类级索引时，填错名称会直接报配置错误，不会额外生成默认配置索引。
索引名默认为 `default`；显式 `name=""` 省略传给 Driver 的名称，服务端仍使用 `default`。
查找和重名校验将两者视为同名，可用字段注解的 `name="default"` 引用空名索引。
继承字段也会收集，归属按当前实体类上的索引数量判断；隐式字段名遵循
`@CollectionField`、驼峰配置及 `@ID` 的 `_id` 映射。类级注解本身不自动继承。

## 同一实体声明多个搜索索引

`MongoSearchIndex` 支持重复声明，容器为 `MongoSearchIndexes`，解析器通过
`getAnnotationsByType(MongoSearchIndex.class)` 同时读取直接注解和容器中的声明。
名称必须唯一，空名和 `default` 并存也属于重名。
MongoDB 支持创建多个搜索索引，省略名称时使用 `default`，见
[官方索引管理文档](https://www.mongodb.com/docs/search/indexes/manage-indexes/)。

```java
@MongoSearchIndex(name = "catalog",
        storedSource = @MongoSearchStoredSource(include = {"title", "price"}))
@MongoSearchIndex(name = "suggestions")
class ProductSearch {
    @MongoSearchField(name = "catalog")
    @MongoSearchField(name = "suggestions", type = SearchFieldType.AUTOCOMPLETE)
    private String title;

    @MongoSearchField(name = "catalog", type = SearchFieldType.NUMBER)
    @MongoSearchSort(name = "catalog")
    private int price;
}
```

`catalog` 包含 title、price 及 price 预排序；`suggestions` 只包含 title 自动补全映射。
如果类上只保留 `catalog`，它的字段映射和排序注解就可以省略 `name`。

嵌套在 `MongoSearchIndex.fields` / `sort` 中的注解已由外层确定归属，`name` 仍须留空。
`storedSource`、`synonyms`、`analyzers`、`typeSets` 等配置彼此隔离；不同索引可各自声明同名的
同义词映射、分析器或类型集，但不能跨索引引用它们。`MongoSearchSynonym.name` 表示同义词映射名称，
不是索引名称。全部索引校验成功后才追加元数据，任意一个失败都不会留下部分结果。

解析器通过项目日志门面输出 DEBUG 级别的字段归属和解析数量；失败日志包含实体类和异常，
不输出完整定义。日志中的“解析完成”仅表示本地模型转换完成，不代表已在 MongoDB 创建索引。

## 类级配置、嵌套字段与动态类型集

```java
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.enums.SearchFieldType;

@MongoSearchIndex(
        name = "movies",
        dynamicTypeSet = "onlyStrings",
        typeSets = {
                "{\"name\":\"onlyStrings\",\"types\":[{\"type\":\"string\"}]}",
                "{\"name\":\"onlyNumbers\",\"types\":[{\"type\":\"number\"}]}"
        },
        fields = {
                @MongoSearchField(fieldName = "title", type = SearchFieldType.AUTOCOMPLETE),
                @MongoSearchField(fieldName = "awards", type = SearchFieldType.DOCUMENT,
                        dynamicTypeSet = "onlyNumbers"),
                @MongoSearchField(fieldName = "items", type = SearchFieldType.EMBEDDED_DOCUMENTS,
                        fields = "{\"name\":{\"type\":\"string\"},\"price\":{\"type\":\"number\"}}"),
                @MongoSearchField(fieldName = "internalNote", exclude = true)
        }
)
class Movie {
}
```

- `dynamicTypeSet` 表达 `dynamic: {"typeSet":"名称"}`；指定时布尔 `dynamic` 保持 `false`。
- `typeSets` 定义在索引级；相同类型集不能重复配置同一种类型，不能包含 `document`、
  `embeddedDocuments`、`vector` 或废弃类型。
- `fields` 是以实际 MongoDB 字段名为键的 JSON 对象，可嵌套任意层级。
  Java 不允许注解成员类型循环引用，因此这里没有使用递归的 `MongoSearchField[]`。
- 嵌套静态映射逐级声明父对象，不能写 `fieldName="awards.wins"` 代替嵌套结构。
- `exclude=true` 表达 `"internalNote":[]`，只使用名称参数，不输出其他选项；
  同一索引的同一字段不能同时排除和索引。

## 分析器与文本评分

内置分析器复用 `Analyzer`，自定义名称使用 `customAnalyzer` / `customSearchAnalyzer`，
并在索引级 `analyzers` 中定义。指定自定义名称时，对应枚举保持 `Analyzer.DEFAULT`。
`DEFAULT` 表示省略，不能调用 `getAnalyzer()` 后将 `lucene.default` 写入定义。

`string` 查询分析器的继承顺序是：字段 `analyzer` → 索引 `searchAnalyzer` →
索引 `analyzer` → `lucene.standard`。省略字段配置才能保留服务端的继承行为。

`multi` 是备用字符串映射对象，例如
`{"english":{"type":"string","analyzer":"lucene.english"}}`，与多个类型的映射数组不同。
`similarity=SearchSimilarity.STABLE_TFL` 表达
`"similarity":{"type":"stableTfl"}`。高亮需要 `store=true` 且 `indexOptions=OFFSETS`；
`store` 与索引级或嵌套文档级的 `storedSource` 含义不同。

## 向量配置

```java
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.enums.SearchFieldType;
import com.mongoplus.enums.SearchQuantization;
import com.mongoplus.enums.SearchVectorSimilarity;

@MongoSearchIndex(fields = {
        @MongoSearchField(fieldName = "embedding", type = SearchFieldType.VECTOR,
                numDimensions = 1536, vectorSimilarity = SearchVectorSimilarity.DOT_PRODUCT,
                quantization = SearchQuantization.SCALAR)
})
class EmbeddedMovie {
}
```

`vectorSimilarity` 对应字符串 `"similarity":"dotProduct"`，
与文本的 `similarity.type` 对象分开配置。向量维数为 1～8192，
自动二进制量化或 `int1` 向量要求维数为 8 的倍数。`hnswOptions` 是 JSON 对象，
只与 `HNSW` 配合，官方将其标为 Preview。

这里的 `vector` 是 Search 字段映射类型；不声明独立 `vectorSearch` 索引的创建能力。

## 索引预排序

`MongoSearchIndex.sort` 使用 `MongoSearchSort[]`，对应索引定义顶层的 `sort` 对象。
数组顺序决定排序优先级，每项的 `fieldName` 必填，`name` 留空。例如：

```java
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.enums.IndexDirection;
import com.mongoplus.enums.SearchFieldType;
import com.mongoplus.enums.SearchSortNoData;

@MongoSearchIndex(
        fields = {
                @MongoSearchField(fieldName = "price", type = SearchFieldType.NUMBER),
                @MongoSearchField(fieldName = "publishedAt", type = SearchFieldType.DATE)
        },
        sort = {
                @MongoSearchSort(fieldName = "price"),
                @MongoSearchSort(fieldName = "publishedAt", order = IndexDirection.DESC,
                        noData = SearchSortNoData.LOWEST)
        }
)
class SortedProduct {
}
```

也可以直接在字段上声明，`fieldName` 留空时使用映射后的 MongoDB 字段名：

```java
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSort;
import com.mongoplus.enums.IndexDirection;
import com.mongoplus.enums.SearchFieldType;
import com.mongoplus.enums.SearchSortNoData;
import java.util.Date;

@MongoSearchIndex(name = "catalog")
class FieldSortedProduct {
    @MongoSearchField(name = "catalog", type = SearchFieldType.NUMBER)
    @MongoSearchSort(name = "catalog", priority = 0)
    private double price;

    @MongoSearchField(name = "catalog", type = SearchFieldType.DATE)
    @MongoSearchSort(name = "catalog", priority = 1, order = IndexDirection.DESC,
            noData = SearchSortNoData.LOWEST)
    private Date publishedAt;
}
```

以上两种声明均表达 `{"price":1,"publishedAt":{"order":-1,"noData":"lowest"}}`。
`order` 默认 `ASC`；`noData` 默认 `DEFAULT`，表示省略该选项。
字段注解的 `name` 与所属索引名称匹配，空名称匹配未指定名称的索引。
`MongoSearchSorts` 支持同字段为不同索引重复声明。

字段注解按 `priority` 从小到大排列，同值按实际 MongoDB 字段路径字典序排列，
不依赖反射字段顺序。两种声明并用时先保留类级数组顺序，再追加字段配置；
数组元素的 `priority` 不参与排序。同一索引不能重复声明相同排序路径。
数组与字段均无配置时省略 `sort`。
这些排序顺序和冲突规则由解析器执行；排序路径还会校验是否具有所需的静态映射。

排序字段必须静态映射为 `boolean`、`date`、`number`、`objectId`、`token` 或 `uuid`，
仅有动态映射或 `typeSet` 不满足要求。多类型字段包含其中一种即可。
嵌套排序键允许点路径，但其父字段必须逐级具有静态 `document` 映射。

`noData` 的 `lowest` / `highest` 表示将 null、缺失值视为最低值 / 最高值，
并非固定“最前 / 最后”；例如降序时 `lowest` 排在末尾。
索引排序不支持 `searchScore`。查询仍需设置匹配索引排序或其前缀的 `$search.sort`，
是否利用预排序可通过 `explain` 的 `usesIndexSort` 检查。
详细要求见 [Sorted Index](https://www.mongodb.com/docs/search/indexes/sort/)。

## 源文档存储

索引级 `storedSource` 使用 `MongoSearchStoredSource` 嵌套注解，支持完整文档、仅包含指定字段、
排除指定字段三种配置。默认 `@MongoSearchStoredSource` 等价于 `storedSource: false`，不存储源文档。

```java
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;

@MongoSearchIndex(dynamic = true,
        storedSource = @MongoSearchStoredSource(include = {"title", "awards.wins"}))
class IncludedSourceMovie {
}
```

此配置表达 `"storedSource":{"include":["title","awards.wins"]}`，MongoDB 默认还存储 `_id`。
字段路径使用实际 MongoDB 名称，支持点路径，不执行 `$field` 转换。

```java
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchStoredSource;

@MongoSearchIndex(dynamic = true, storedSource = @MongoSearchStoredSource(true))
class FullSourceMovie {
}

@MongoSearchIndex(dynamic = true,
        storedSource = @MongoSearchStoredSource(exclude = {"_id", "content"}))
class ExcludedSourceMovie {
}
```

`value=true` 表示存储完整文档；非空 `include` 或 `exclude` 表示对象形式，此时 `value` 保持默认 false。
`include` 和 `exclude` 不能同时非空，也不能与 `value=true` 同用；字段路径不能为空白。
这替代了先前索引级 `storedSource` 的 JSON 字符串声明；`MongoSearchField.storedSource` 的嵌套文档配置仍使用 JSON。

存储源字段与索引映射是独立配置；要搜索字段仍需建立对应映射，查询时还需设置 `returnStoredSource`
才请求返回存储的源字段。该功能要求 MongoDB 7.0+。若索引包含 `vector` 字段，不得全量存储，
应使用 include 选择非向量字段，或用 exclude 排除向量字段；也需考虑选择或排除父对象的影响。
解析器执行转换、互斥检查及静态向量路径的存储限制检查，不执行数据库操作。
依据：[Stored Source 官方文档](https://www.mongodb.com/docs/search/indexes/stored-source-definition/)。

## 同义词映射与索引分区

`synonyms` 使用 `MongoSearchSynonym[]`，`numPartitions` 保持整数配置：

```java
import com.mongoplus.annotation.index.search.MongoSearchField;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.search.MongoSearchSynonym;
import com.mongoplus.enums.Analyzer;
import com.mongoplus.enums.SearchFieldType;

@MongoSearchIndex(
        fields = @MongoSearchField(fieldName = "title", type = SearchFieldType.STRING,
                analyzer = Analyzer.STANDARD),
        synonyms = @MongoSearchSynonym(name = "movieTerms", sourceCollection = "movie_synonyms"),
        numPartitions = 2
)
class SynonymPartitionedMovie {
}
```

该示例的同义词定义为
`{"name":"movieTerms","source":{"collection":"movie_synonyms"},"analyzer":"lucene.standard"}`，
分区定义为顶层 `"numPartitions":2`。
`name` 与 `sourceCollection` 必填，映射名称在索引内唯一。
来源集合必须与被索引集合位于同一数据库，集合内容由使用方准备；
注解不创建或写入同义词文档。`equivalent`（等价词）与 `explicit`（定向替换）
属于来源集合文档的 `mappingType`，不属于索引映射选项。

同义词的 `analyzer=DEFAULT` 且未指定 `customAnalyzer` 时，解析契约要求显式输出
`lucene.standard`，因为同义词的 analyzer 是必填属性。
自定义分析器引用索引级 `analyzers`，使用时 analyzer 保持 DEFAULT。
目标字段必须采用相同分析器；CJK、KUROMOJI 及部分自定义 tokenizer/token filter 不适用，
完整限制见注解 Javadoc。查询还需通过 `text` 或 `phrase` 的 synonyms 参数指定映射名称。
M0 集群每个索引只允许一个映射，来源集合最多一万条文档；
来源文档的合法变更由服务端异步更新到同义词映射。

`numPartitions` 只允许 `1/2/4`，默认 1；使用额外分区需要独立 Search Nodes。
容量按索引对象计算，每分区最多二十亿个对象，嵌套 embeddedDocuments 也会增加计数。
配置或更改分区数量会触发索引重建；多分区时不能移除所有 Search Nodes 并迁移为共节点部署。
分区查询由服务端分发和合并；查询级 `concurrent` 仍需单独配置。

解析器执行同义词定义转换、分析器引用和限制检查，以及分区取值校验。
来源集合操作、集群规格检查和同义词查询执行不在解析器职责内。
依据：[Synonym Mappings](https://www.mongodb.com/docs/search/indexes/synonyms/)、
[Index Partitions](https://www.mongodb.com/docs/search/indexes/index-partition/)。

## 解析实现边界

注解本身不执行校验；`SearchParse` 及包内字段转换、定义校验组件执行本地转换和基础结构校验：
重复映射合并、排除冲突、排序路径、自定义分析器/类型集引用、数值/自动补全/向量范围、
Stored Source 互斥和向量排除等。非法配置抛出 `MongoPlusConvertException`，消息包含配置路径。
所有分组验证成功后才追加搜索索引模型，不会在某分组失败时留下部分搜索结果。

空字符串与数值 `-1` 是省略标记，必填向量参数若仍为默认值则报错。
`Analyzer.DEFAULT` 保留服务端继承行为，同义词分析器按其必填契约显式输出默认值。
JSON 中未由本地校验覆盖的服务端选项继续保留，最终支持情况以实际 MongoDB 部署为准；
来源集合内容、版本和 Search Nodes 等远端条件不通过本地解析保证。

`Resolver` 按各解析器的 `activate()` 判断是否调用，普通索引与搜索索引保存在元数据的不同列表中。
本次没有把 `AutoUtil` / `IndexUtil` 改为创建搜索索引，添加注解不会自动提交 Search DDL。

主要依据：

- [字段映射与动态类型集](https://www.mongodb.com/docs/search/indexes/define-field-mappings/)
- [string](https://www.mongodb.com/docs/search/indexes/field-types/string-type/)、
  [autocomplete](https://www.mongodb.com/docs/search/indexes/field-types/autocomplete-type/)、
  [token](https://www.mongodb.com/docs/search/indexes/field-types/token-type/)
- [document](https://www.mongodb.com/docs/search/indexes/field-types/document-type/)、
  [embeddedDocuments](https://www.mongodb.com/docs/search/indexes/field-types/embedded-documents-type/)
- [number](https://www.mongodb.com/docs/search/indexes/field-types/number-type/)、
  [geo](https://www.mongodb.com/docs/search/indexes/field-types/geo-type/)、
  [vector](https://www.mongodb.com/docs/search/indexes/field-types/vector-type/)
