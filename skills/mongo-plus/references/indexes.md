# 唯一、过期、全文、向量和时序

“订单号不可重复”“流水保留 30 天”“按文本或 embedding 找相似商品。” 先确认查询形状、物理集合和部署能力。2.2.0 启动扫描的实体可声明索引，Boot 的 `mongo-plus.configuration.auto-create-index` 默认关闭；在已有库打开前核对重名定义、重复键、已有数据类型及权限。

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.index.MongoIndex;
import com.mongoplus.annotation.index.search.MongoSearchIndex;
import com.mongoplus.annotation.index.vector.MongoVectorField;
import com.mongoplus.annotation.index.vector.MongoVectorIndex;
import com.mongoplus.enums.SearchVectorSimilarity;
import java.util.List;

@CollectionName("catalog")
@MongoSearchIndex(name = "catalog_text", dynamic = true)
@MongoVectorIndex(name = "catalog_vec", fields = {
    @MongoVectorField(fieldName = "embedding", numDimensions = 3,
                      similarity = SearchVectorSimilarity.COSINE)
})
public class IndexedProduct {
    @MongoIndex(name = "sku_unique", unique = true)
    public String sku;
    public String title;
    public List<Double> embedding;
}
```

| 需求 | 入口和核对点 |
|---|---|
| 普通/唯一、稀疏、部分及 TTL | 字段 `@MongoIndex` 的 `unique`、`sparse`、`partialFilterExpression`、`expireAfterSeconds` 等；TTL 检查实际 BSON 日期字段及后台过期延迟 |
| 组合键、哈希、地理、传统文本 | `@MongoCompoundIndex`、`@MongoHashIndex`、`@MongoGeoIndex`、`@MongoTextIndex`；复合键顺序和排序方向影响可用查询 |
| 全文搜索 | 类级 `@MongoSearchIndex` 可重复声明，显式 `@MongoSearchField` 或启用 dynamic；搜索索引与传统 text index 使用不同命令与查询语法 |
| 独立 Vector Search 索引 | `@MongoVectorIndex` / `@MongoVectorField`；定义路径、维度与相似度，按需增加 filter 字段。核对部署支持、维度、索引构建状态和对应查询 |
| 时序测量 | `@TimeSeries(timeField="...")` 声明集合选项，另核对 mongo-plus.configuration.auto-create-time-series 开关、BSON Date、metaField 与保留期；它不是索引类型 |

手动管理常规索引可在 `IRepository<T>` 上用 `String createIndex(Bson keys, IndexOptions options)`、`List<Document> listIndexes()`；只有 `MongoMapper<T>` 时从 `getBaseMapper()` 找显式实体/namespace 的接口。使用已存在集合和受控测试数据验证唯一约束、列出索引定义，用 explain 判断查询是否受益。Search/Vector Search 在支持部署上另查 `$listSearchIndexes`、等待索引可查询后再发代表性搜索；声明编译或请求受理不能证明已可检索。

依据 · [MongoIndex](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java) · [MongoCompoundIndex](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java) · [MongoSearchIndex](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/search/MongoSearchIndex.java) · [MongoVectorIndex](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorIndex.java) · [MongoVectorField](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/vector/MongoVectorField.java) · [TimeSeries](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java) · [IRepository](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/repository/IRepository.java) · [AutoUtil](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java) · [官方自动创建索引](https://mongoplus.com/docs/autoCreateIndex.html)
<!-- COMPLETE_COVERAGE -->
## 注解清单与完整管理边界

普通索引7种注解（含复合容器）均在[完整记录](../annotations/indexes.md)：MongoIndex、MongoCompoundIndex、MongoCompoundIndexes、MongoGeoIndex、MongoHashIndex、MongoTextIndex、MongoIndexDs。Search的8种及Vector的6种另有[专题](search-vector.md)和注解页。

自动扫描由auto-scan-packages等配置决定，auto-create-index默认false。Resolver将实体注解解析为IndexMetaObject；AutoUtil先解析全部实体再执行DDL。普通createIndexes与Search管理调用分开，远端失败不会回滚先前创建的索引。不要承诺自动扫描可以修复所有已有同名冲突。

| 索引类型/能力 | 核验方法 |
|---|---|
| 唯一/稀疏/partial | listIndexes核对定义，用冲突和不匹配partial条件的数据验证约束 |
| TTL | BSON Date、expireAfterSeconds/表达式转换、后台清理延迟；不能只看到索引即声明已过期删除 |
| 复合 | 键顺序、方向、字段真实物理名、partial/unique |
| 哈希 | hashed值而非普通1/-1；范围/唯一等服务端约束另核对 |
| 地理 | 2d/2dsphere与坐标/几何值、地理查询 |
| text | fields、language、版本和$text行为；区别Search |
| 手动管理 | BaseIndex/Index/SuperIndex与Repository createIndex(s)/listIndexes/dropIndex(es)，重载显式指定namespace或实体 |
| 多数据源DDL | MongoIndexDs只定索引目标，不改变一般数据访问路由 |

索引状态、定义冲突、自动初始化幂等及Search异步就绪见[Search/Vector](search-vector.md)，时序集合见[时序](timeseries.md)，精确签名见[管理API](api/management.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C083 普通单字段唯一稀疏部分TTL索引**：`BaseIndex`, `IndexParser`。
- **C084 复合哈希地理传统文本索引**：`CompoundParser`, `HashParser`, `GeoParser`, `TextParser`。
- **C085 手工索引创建列举删除**：`BaseIndex`, `Index`, `SuperIndex`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
