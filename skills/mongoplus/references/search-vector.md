# Search与Vector Search

先选择索引类型与部署能力。普通text索引使用`@MongoTextIndex`与`$text`，Search使用`@MongoSearchIndex`及`$search`，独立Vector Search使用`@MongoVectorIndex`及`$vectorSearch`；三者不是同一种索引。当前注解记录覆盖所有容器与嵌套选项：[Search](../annotations/search.md)、[Vector](../annotations/vector.md)。

| 能力 | 公开入口与配置 | 使用边界 |
|---|---|---|
| 多Search索引 | 可重复MongoSearchIndex / MongoSearchIndexes | 每个索引名唯一，各自字段、排序、源字段和同义词隔离 |
| 静态/动态映射 | fields、dynamic、dynamicTypeSet、typeSets | 默认dynamic=false；静态字段覆盖该字段动态映射 |
| 字段类型/多类型/排除 | MongoSearchField(s) | 默认STRING，不推断Java类型；exclude与其他定义冲突 |
| 嵌套文档/对象数组 | DOCUMENT、EMBEDDED_DOCUMENTS，fields JSON | 不把点路径写法当嵌套映射；数组按元素类型配置 |
| 分析器 | analyzer/customAnalyzer/searchAnalyzer/analyzers | DEFAULT为省略，不输出lucene.default；自定义名须存在 |
| 字符串/自动补全/token/数值/geo | MongoSearchField类型专属属性 | 只发送该类型适用选项；数值精度与gram范围单独核对 |
| Search向量字段 | SearchFieldType.VECTOR及numDimensions/vectorSimilarity | 不是独立Vector索引；旧KNN_VECTOR与dimensions属兼容路径 |
| 预排序/存储源/同义词 | MongoSearchSort(s)、StoredSource、Synonym | 存储源不替代字段索引；同义词来源数据需预先存在 |
| 独立向量/过滤字段 | MongoVectorField(s)，VectorFieldType.VECTOR/FILTER | vector须维度与相似度，filter不填向量选项 |
| HNSW/flat与量化 | indexingMethod、quantization、MongoVectorHnswOptions | flat与HNSW参数互斥，binary维度约束，依部署支持 |
| 多向量索引/嵌套根/存储源 | MongoVectorIndex(es)、nestedRoot、StoredSource | 同一集合Search/Vector名称共享空间 |
| 自动创建 | auto-create-index、实体扫描，或AutoUtil.autoCreateIndexes | Resolver先完成本地解析，远端DDL不具备跨索引原子性 |

`Resolver.resolve`产生普通IndexModel和SearchIndexModel；SearchParse与VectorParse是配置解析入口，不是检索结果API。自动流程的SearchIndexCreator是package-private内部实现，不向用户推荐直接调用。它列举现有索引、比较同名类型/定义/状态，仅创建缺失索引，不自动更新、删除或修复冲突。列举和创建之间存在并发窗口。请求受理后不等待READY，不保证立即可查询。

Search/Vector查询可通过MongoPlus聚合`custom(Bson)`注入完整阶段，再经Mapper执行与映射；框架没有生成embedding的公开服务。维度必须与模型输出一致。下面给出可编译构造形状，运行必须先有支持的部署和就绪索引：

```java
import com.mongoplus.aggregate.AggregateWrapper;
import org.bson.Document;
import java.util.Arrays;

class SemanticLookup {
    static AggregateWrapper pipeline() {
        Document body = new Document("index", "catalog_vec")
            .append("path", "embedding").append("queryVector", Arrays.asList(0.1,0.2,0.3))
            .append("numCandidates", 30).append("limit", 3);
        return new AggregateWrapper().custom(new Document("$vectorSearch", body));
    }
}
```

索引声明示例见[普通索引入口](indexes.md)与完整注解记录。真实测试要捕获createSearchIndexes/listSearchIndexes或相关命令、完整查询pipeline，等待queryable状态，并用原生aggregate比较完整结果和评分/投影。当前环境不支持时保留服务端错误分类，标`NOT_RUN`，不能用解析成功替代Search/Vector运行通过。

依据：Resolver、SearchParse、VectorParse、AutoUtil及内部SearchIndexCreator；对应路径见[公开类型矩阵](../audit/public-types.md)。部署能力依据[MongoDB Search官方文档](https://www.mongodb.com/docs/atlas/atlas-search/)与[Vector Search官方文档](https://www.mongodb.com/docs/atlas/atlas-vector-search/)，目标环境要再次核对。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C086 Search静态动态多索引**：`SearchParse`。
- **C087 Search分析器排序源字段同义词**：`SearchParse`。
- **C088 VectorSearch向量过滤HNSW量化**：`VectorParse`。
- **C089 SearchVector状态冲突与幂等创建**：`AutoUtil#autoCreateIndexes`, `Resolver`。
- **C090 SearchVector查询与部署适配**：`Aggregate#custom`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
