# 关联、子管道与表达式

“为每张订单找出同一客户费用最高的一条有效报价，把结果附到订单上。” 用 `AggregateWrapper` 表示各自的父、子管道。`lookup(from, localField, foreignField, as)` 适合单字段等值连接；每行需传参、筛选或排序时选 `lookup(String, List<Variable<TExpression>>, Aggregate<?>, String)`。2.2.0 该参数声明引用 Driver 的 `Variable` 值类型，执行仍走 MongoPlus Mapper。

```java
import com.mongodb.client.model.Variable;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.toolkit.Filters;
import java.util.Collections;
import java.util.List;
import org.bson.Document;

class QuoteJoin {
    static AggregateWrapper pipeline(String foreignCollection) {
        AggregateWrapper offers = new AggregateWrapper()
            .match(new QueryWrapper<>().eq("active", true))
            .match(Filters.expr(AggregateOperator.eq("$customer", "$$buyer")))
            .sortSpecification(new Document("fee", -1).append("_id", 1)).limit(1);
        return new AggregateWrapper()
            .match(new QueryWrapper<>().eq("state", "OPEN"))
            .lookup(foreignCollection, Collections.singletonList(
                new Variable<String>("buyer", "$customer")), offers, "quotes")
            .sortSpecification(new Document("_id", 1));
    }

    static <T> List<Document> list(MongoMapper<T> orders, String offersCollection) {
        return orders.list(pipeline(offersCollection), Document.class);
    }
}
```

`from` 是同库物理集合名，`as` 是结果中的数组字段（无匹配是空数组）。`let.buyer` 的值来自外层 `$customer`；子管道内 `$customer` 属于外表文档，`$$buyer` 读取变量。普通 `match(wrapper)` 是查询谓词，字段对变量比较需 `Filters.expr` 加表达式。下面是原生对照，保留顺序及并列费用的次排序键。

```javascript
[
  {$match: {state: "OPEN"}},
  {$lookup: {from: "offers", let: {buyer: "$customer"}, pipeline: [
    {$match: {active: true}},
    {$match: {$expr: {$eq: ["$customer", "$$buyer"]}}},
    {$sort: {fee: -1, _id: 1}}, {$limit: 1}
  ], as: "quotes"}},
  {$sort: {_id: 1}}
]
```

“按客户统计总费用”可用 `new AggregateWrapper().group("$customer", Accumulators.sum("total", "$fee"))`，其中 `Accumulators` 来自 `com.mongoplus.aggregate.pipeline`，结果字段为 `_id` 和 `total`。用 `Document` 或具有相应字段的 DTO 接收。`project(Bson)` 接受投影体，`sortSpecification(Bson)` 接受排序体；该基线的 `sort(Bson)` 和 `lookup(Bson)` 直接追加传入阶段。逐个确认重载的包装语义，避免多一层或少一层操作符。核对实际完整 pipeline、关联数组顺序、无匹配主行和费用相同的行。外层租户约束不会递归到外表，按外表隔离规则在子管道另加限制。

依据 · [Aggregate](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java) · [LambdaAggregateWrapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java) · [AggregateOperator](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java) · [Accumulators](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Accumulators.java) · [Filters](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java) · [MongoMapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapper.java) · [官方管道函数](https://mongoplus.com/docs/aggregate.html)
<!-- COMPLETE_COVERAGE -->
## 所有主要阶段与BSON重载边界

| 阶段族 | MongoPlus入口 | 参数/限制 |
|---|---|---|
| 匹配、投影、排序、分页 | match/project/sort/sortSpecification/skip/limit | match/project的Bson为参数体；sort(Bson)为完整阶段，sortSpecification(Bson)为排序体 |
| 添加/修改/移除字段 | addFields/set/unset | addFields/set的Bson为参数体；unset(Bson)为完整阶段 |
| 分组、分桶、计数 | group/bucket/bucketAuto/sortByCount/count | group(Bson)完整阶段；bucket(Bson)、bucketAuto(Bson)为参数体；输出字段名和输入表达式区分 |
| 等值/管道关联 | lookup与let Variable | from物理集合或实体类转换；Driver Variable与同名MongoPlus类不随意互换 |
| 多分支和并集 | facet/unionWith | 子管道各自验证；外层tenant/logic过滤不自动递归 |
| 图遍历/展开 | graphLookup/unwind | connect字段、递归深度、preserveNullAndEmptyArrays和includeArrayIndex核对 |
| 替换文档/采样 | replaceRoot/replaceWith/sample | 字段引用与literal Document区别；sample不保证顺序 |
| 写出/合并 | out/merge | 标记isSkip；影响终结返回/取结果；会写库，需目标权限与独立验证 |
| 窗口 | setWindowFields | partitionBy、sortBy、WindowOutputField等Driver值类型，输出形状独立核对 |
| 补时间点/缺值 | densify/fill | range、bounds、partition及locf/linear要求，服务端支持需核对 |
| 任意完整阶段 | custom(Bson) | 原样追加，不自动包阶段名；可接Search/Vector |
| 执行选项 | allowDiskUse/batchSize/collation/maxTimeMS/maxAwaitTimeMS/bypassDocumentValidation/comment/hint/let | options不是pipeline阶段；全部实际发出值需CommandStartedEvent核对 |

`addFields/set/bucket/bucketAuto/match/project(Bson)`在当前实现包上对应操作符；`sort/lookup/facet/graphLookup/group/unionWith/unwind/out/merge/replaceRoot/replaceWith/sample/setWindowFields/densify/fill/unset/custom(Bson)`直接追加完整阶段。不要给前一组传`{$match:...}`等完整阶段，也不要给后一组只传内部参数。每个String、lambda、Bson、Collection重载单独看实现，不能用同名API推断包装规则。

所有阶段签名在[聚合API](api/aggregation.md)，表达式和所有累加器见[表达式专题](expressions.md)。验证以真实发出的完整pipeline和原生aggregate结果为准；仅构造Bson的检查不算运行通过。

**Java重载解析也影响含义**：`replaceRoot(Document)`及`replaceWith(Document)`选择更具体的Document表达式重载，自动包装成阶段。传完整阶段时显式将参数声明/转换为Bson，例如`replaceRoot((Bson) fullStage)`。本次分别实测了显式Bson完整阶段和普通Document表达式，两条路径均与相应原生pipeline一致；首轮把完整Document误传给Document表达式重载产生双层$replaceRoot/$replaceWith，记录保留在[运行审计](../audit/runtime-results.md)，不是据此认定框架缺陷。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C037 聚合Wrapper与结果映射**：`AggregateWrapper`, `LambdaAggregateWrapper`, `LambdaAggregateChainWrapper`。
- **C038 聚合match-project-sort-skip-limit**：`Aggregate#match`, `Aggregate#project`, `Aggregate#sort`, `Aggregate#skip`, `Aggregate#limit`。
- **C039 聚合addFields-set-unset**：`Aggregate#addFields`, `Aggregate#set`, `Aggregate#unset`。
- **C040 聚合lookup关联与let**：`Aggregate#lookup`, `Variable`。
- **C041 聚合facet与unionWith**：`Aggregate#facet`, `Aggregate#unionWith`, `Facet`。
- **C042 聚合graphLookup与unwind**：`Aggregate#graphLookup`, `Aggregate#unwind`, `UnwindOption`。
- **C043 聚合group与累加器**：`Aggregate#group`, `Accumulators`。
- **C044 聚合bucket-bucketAuto-sortByCount-count**：`Aggregate#bucket`, `Aggregate#bucketAuto`, `Aggregate#sortByCount`, `Aggregate#count`。
- **C045 聚合replaceRoot-replaceWith-sample**：`Aggregate#replaceRoot`, `Aggregate#replaceWith`, `Aggregate#sample`。
- **C046 聚合out与merge写阶段**：`Aggregate#out`, `Aggregate#merge`。
- **C047 聚合窗口densify-fill**：`Aggregate#setWindowFields`, `Aggregate#densify`, `Aggregate#fill`, `FillField`。
- **C049 聚合自定义阶段与执行选项**：`Aggregate#custom`, `AggregateOptions`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
