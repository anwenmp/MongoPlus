# 组合筛选、数组与分页

“列出金额在半开区间内，且标题以指定文本开头或标为紧急的订单，只展示列表字段。” 用 `QueryWrapper<T>` 构造 find 条件，交给 `MongoMapper<T>.list(Wrapper<T>, Class<R>)`。用户输入是普通文本时用 `Pattern.quote` 保留字面含义。以下字符串都是库内字段名。

```java
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.enums.RegexOptions;
import com.mongoplus.mapper.MongoMapper;
import java.util.List;
import java.util.regex.Pattern;
import org.bson.Document;

class OrderFilters {
    static <T> QueryWrapper<T> shortlist(int low, int high, String prefix) {
        return new QueryWrapper<T>()
            .gte("amount", low).lt("amount", high)
            .or(q -> q.regex("title", "^" + Pattern.quote(prefix), RegexOptions.UNICODE)
                      .eq("urgent", true))
            .orderByDesc("amount").orderByAsc("_id")
            .projectDisplay("_id", "amount");
    }

    static <T> List<Document> rows(MongoMapper<T> orders, String prefix) {
        return orders.list(shortlist(10, 20, prefix), Document.class);
    }

    static <T> QueryWrapper<T> byLot(String site, String sku, int minimum) {
        QueryWrapper<Object> lot = new QueryWrapper<Object>()
            .eq("sku", sku).gte("qty", minimum);
        QueryWrapper<Object> shipment = new QueryWrapper<Object>()
            .eq("site", site).elemMatch("lots", lot);
        return new QueryWrapper<T>().elemMatch("shipments", shipment);
    }
}
```

该版本双参数 `regex(String,Object)` 默认启用 `i`，传入 Java `Pattern` 也只提取表达式文本。示例明确使用三参数重载与 `RegexOptions.UNICODE`（`u`），实现大小写敏感的字面前缀。用大小写仅差一位的记录检验要求。

“同一批发货里，同一条明细同时满足 SKU 和数量。” `elemMatch(String, Wrapper<?>)` 每次向内进入一个数组；字段名从该层元素起算。用下列原生条件作对照，再放入反例：一条发货 sku 正确但数量不足，另一条数量足够但 sku 不同；以及两个站点各满足一半。两个点路径比较可能命中不同元素。

```javascript
{shipments: {$elemMatch: {site: "A", lots: {$elemMatch: {sku: "X", qty: {$gte: 2}}}}}}
```

`or(q -> q.eq(...).eq(...))` 在该逻辑组中形成不同备选条件。`or` 内的复合分支要检查层次；不要把 Java 链式连续书写当作括号。相同字段范围留在同一组，并核对上下界。字段 getter 的 Lambda 适合已映射实体；深层元素示例显式使用物理字段名。

`mapper.page(filter, pageNum, pageSize)` 返回 `PageResult<T>`，从第 1 页开始；仅需当前页结果可用 `pageList(filter, pageNum, pageSize, Document.class)`。加唯一的次排序键，避免同值翻页漂移。投影可用 `projectDisplay` 和 `projectNone`；部分文档宜读为 DTO 或 `Document`，不要把残缺实体当作整行回写。每次操作新建 Wrapper，防止排序、投影及条件在请求间累积。

依据 · [QueryWrapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/query/QueryWrapper.java) · [ElemMatch](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/array/operations/ElemMatch.java) · [Or](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/logic/operations/Or.java) · [Project](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/project/Project.java) · [ConditionUtil](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/toolkit/ConditionUtil.java) · [MongoMapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapper.java) · [官方条件构造器](https://mongoplus.com/docs/conditionBuild.html)
<!-- COMPLETE_COVERAGE -->
## 条件族全景

当前实体Lambda查询使用`QueryWrapper<T>.lambdaQuery()`或`ChainWrappers.lambdaQueryChain`/Repository.lambdaQuery等真实入口；源码没有名为LambdaQueryWrapper的类。构造器与链均带可变状态，终结执行与构造条件是两步。

| 条件族 | 公开API/操作 | 核对点 |
|---|---|---|
| 比较 | eq/ne/gt/gte/lt/lte/in/nin | boolean condition=false不加入；null、空in、重复列需实际BSON验证 |
| 存在与类型 | exists/type | 缺字段与显式null不是同一场景；TypeEnum按BSON类型 |
| 逻辑 | and/or/nor/not | 同字段多范围与复合分支层级分开检查 |
| 数组 | all/size/elemMatch | elemMatch约束同一元素；嵌套数组逐层进入 |
| 字符串与表达式 | regex/like/mod/expr/jsonSchema/where | 正则选项、字段表达式、服务端禁用脚本等限制独立核对 |
| 位运算 | bitsAllClear/bitsAllSet/bitsAnyClear/bitsAnySet | 位位置、掩码及字段BSON类型按重载 |
| 全文 | text与TextSearchOptions | 传统text索引；Search另见专题 |
| 地理 | near/nearSphere/geoWithin/geoIntersects及box/center/polygon变体 | 对应2d/2dsphere索引、坐标系、单位和排序限制 |
| 投影 | projectDisplay/projectNone及Projection | 包含/排除通常不能随意混用；_id另查 |
| 排序/分页 | orderByAsc/orderByDesc、page/pageList | 明确排序键及页起点；复用会累积状态 |
| 自定义BSON | Wrapper.custom、Filters及addBasicDBObject等真实入口 | 物理字段路径、表达式与注解转换覆盖分别验证 |
| 类型安全嵌套 | FieldChain/FreeFieldChain、SFunction | build/buildOption的字段前缀不同，不把查询路径和表达式引用混用 |

公开方法族及import在[查询API](api/query.md)；地理模型也列入，不因业务使用少而排除。原始BSON不保证所有ConditionHandler元数据都存在。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C019 查询Wrapper与链式终结**：`QueryWrapper`, `LambdaQueryChainWrapper`, `ChainWrappers`。
- **C020 比较范围存在与类型**：`Compare`, `DataType`。
- **C021 and-or-nor-not逻辑组合**：`And`, `Or`, `Nor`, `Not`。
- **C022 嵌套字段与类型安全路径**：`FieldChain`, `FreeFieldChain`, `SFunction`。
- **C023 数组all-size-elemMatch**：`All`, `Size`, `ElemMatch`。
- **C024 正则like与大小写**：`Regex`, `Like`, `RegexOptions`。
- **C025 投影排序分页选项**：`Projection`, `Ordered`, `QueryParam`。
- **C026 传统文本查询**：`Text`, `TextSearchOptions`。
- **C027 地理空间查询与模型**：`GeoSpatial`, `GeoNear`, `Coordinate`, `GeoBox`, `GeoCenter`。
- **C028 位运算与expr-jsonSchema-where**：`Bitwise`, `Expr`, `JsonSchema`, `Where`, `Mod`。
- **C029 原生条件与Filters组合**：`Wrapper`, `Filters`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
