# 聚合表达式与累加器

表达式是阶段内部的值，不是完整pipeline stage。`$field`引用文档字段，`$$variable`引用已绑定变量；希望保留美元开头的字面值时使用`AggregateOperator.literal`。不要自动给任意String补`$`，也不要将输出别名当输入路径。

| 类别 | 当前MongoPlus公开入口 | 用法与限制 |
|---|---|---|
| 比较与逻辑 | AggregateOperator.eq/ne/gt/gte/lt/lte/and/or/not | 构造表达式；查询谓词eq与表达式eq不同 |
| 四则与数学 | AggregateOperator.subtract/divide；ConditionOperators.add/multiply/abs/sum | 区分表达式求和与group累加器，数值类型及除零由服务端处理 |
| 数组 | AggregateOperator.concatArrays/concatArraysExpressions；ConditionOperators.each系列 | concatArrays两个入口对参数形状不同，不推定可互换；each系列用于更新语境，非所有均是聚合表达式 |
| 字符串 | AggregateOperator.concat；ConditionOperators.substrBytes/toString | substrBytes是字节边界，非字符数；null及多字节需实测 |
| 日期 | AggregateOperator.dateTrunc；ConditionOperators.dateToString/dateFromString/toDate | 日期、格式、timezone、onError/onNull等各重载按签名核对 |
| 条件 | ConditionOperators.cond/condArray/ifNull | 对象式与数组式参数不能按名称互推契约 |
| 类型转换 | toBool/toDecimal/toDouble/toInt/toLong/toObjectId/toHashedIndexKey等 | 当前API参数语义按源码和实发命令确认；非法输入的结果/异常另测 |
| 对象合并 | mergeObjects/mergeObjectsOption及lambda变体 | 字段路径/表达式/字面文档的重载差异必须独立验证 |
| 分组累加 | Accumulators.sum/count/avg/min/max/first/last/push/addToSet/stdDevPop/stdDevSamp/mergeObjects/accumulator | 每个BsonField的名字为输出别名；first/last需要明确定义排序 |
| N值与top/bottom | firstN/lastN/minN/maxN/top/topN/bottom/bottomN | 服务端版本、排序体、n的表达式与返回数组形状分别核对 |
| 投影/排序帮助类 | Projections、Sorts、SimpleExpression | 不要混用同名Driver类；按实际返回类型组合 |

`com.mongoplus.conditions.operation.ConditionOperators`是当前实现入口，`com.mongoplus.conditions.interfaces.ConditionOperators`存在兼容API，优先核对Deprecated及转发关系，见[表达式签名](api/expressions.md)。不存在某个便捷表达式方法时，先以Document/Bson表达式接入框架阶段；不要编造MongoPlus支持MongoDB全部表达式的逐方法包装。

```java
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.aggregate.pipeline.AggregateOperator;
import org.bson.Document;

class PriceExpression {
    static AggregateWrapper projection() {
        return new AggregateWrapper().project(new Document("_id",0)
            .append("remaining",AggregateOperator.subtract("$budget","$spent"))
            .append("label",AggregateOperator.literal("$literal-label")));
    }
}
```

每次实验记录完整父阶段、表达式输入类型、实际pipeline、原生aggregate和完整结果。不能因为Bson编码成功就断言任意Java值均有codec或服务端类型合法。所有未独立执行的重载维持`CONTRACT_UNRESOLVED`；当前示例验证范围见[报告](../audit/coverage-report.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C048 聚合表达式日期数组数学条件转换**：`AggregateOperator`, `ConditionOperators`, `SimpleExpression`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
