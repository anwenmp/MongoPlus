# MongoPlus Wrapper Converter Phase 4.5 真实 DeepSeek 验证

> 验证日期：2026-08-21
> 实际模型：`deepseek-v4-pro`
> 验收口径：只有真实 Wrapper 产物通过 BSON/Pipeline Comparator 并返回 `VERIFIED` 才算成功。

## 1. 最终验收结果

最终验收轮执行 5 个不同真实 AI Case，共发起 6 次模型请求（GTE + EQ 发生一次语义修正）。
5 个 Case 全部 `VERIFIED`，最终没有 `REJECTED` 或 `ERROR`。

| Case | operation | modelAttempts | retry | AI 耗时 | 总耗时 | 纯 JSON Object | 严格解析 | 最终状态 |
|---|---|---:|---:|---:|---:|---:|---:|---|
| EQ | FIND | 1 | 否 | 2325 ms | 2373 ms | 是 | 成功 | VERIFIED |
| GTE + EQ | FIND | 2 | 是 | 2872 + 2217 = 5089 ms | 5107 ms | 是 | 两次均成功 | VERIFIED |
| OR | FIND | 1 | 否 | 2338 ms | 2343 ms | 是 | 成功 | VERIFIED |
| IN + SORT | FIND | 1 | 否 | 2199 ms | 2204 ms | 是 | 成功 | VERIFIED |
| Aggregate | AGGREGATE | 1 | 否 | 3544 ms | 3552 ms | 是 | 成功 | VERIFIED |

- 每个 Case 的模型总耗时平均约 3.10 秒；按 6 次模型响应计算，单次平均约 2.58 秒。
- 每个 Case 的总转换耗时平均约 3.12 秒。
- 最终轮 6 份响应均为单一 JSON Object，没有 Markdown code fence、解释或前后缀。
- 没有 Fastjson2 JSON 语法解析失败，没有未知 Catalog method，也没有最终轮非法 Plan shape。
- GTE + EQ 的 attempt 1 被 Comparator 真实拦截；retry 后成功。
- 最终轮 Catalog/Registry 未拦截错误；此前诊断轮的 Aggregate 错误曾被 Registry 真实拦截。

## 2. 逐 Case 证据

### 2.1 EQ

Command：

```javascript
db.user.find({status: "ACTIVE"})
```

Call Plan：

```json
{"schemaVersion":1,"wrapperType":"QUERY","calls":[{"methodId":"query.eq","field":"status","arguments":[{"type":"STRING","value":"ACTIVE"}],"nestedCalls":[],"accumulators":[]}],"unsupported":false,"unsupportedReason":null}
```

Comparator 的 expected 与 actual 均为：

```text
filter={"status": {"$eq": "ACTIVE"}}; projection={}; sort={}
```

最终 Java：

```java
QueryWrapper<Object> wrapper = new QueryWrapper<>()
        .eq("status", "ACTIVE");
```

### 2.2 GTE + EQ

Command：

```javascript
db.user.find({age: {$gte: 18}, status: "ACTIVE"})
```

Attempt 1 生成 `query.and` 嵌套计划，真实 Wrapper 得到显式 `$and`；Parser IR 的期望是同一 document
内的并列字段。Comparator 返回 `REJECTED / filter differs`，触发现有一次语义修正。Attempt 2 的 Call Plan：

```json
{"schemaVersion":1,"wrapperType":"QUERY","calls":[{"methodId":"query.gte","field":"age","arguments":[{"type":"INT32","value":18}],"nestedCalls":[],"accumulators":[]},{"methodId":"query.eq","field":"status","arguments":[{"type":"STRING","value":"ACTIVE"}],"nestedCalls":[],"accumulators":[]}],"unsupported":false,"unsupportedReason":null}
```

Attempt 2 的 expected 与 actual 均为：

```text
filter={"age": {"$gte": {"$numberInt": "18"}}, "status": {"$eq": "ACTIVE"}}; projection={}; sort={}
```

最终 Java：

```java
QueryWrapper<Object> wrapper = new QueryWrapper<>()
        .gte("age", 18)
        .eq("status", "ACTIVE");
```

### 2.3 OR

Command：

```javascript
db.user.find({$or: [{age: {$lt: 18}}, {age: {$gt: 60}}]})
```

Call Plan：

```json
{"schemaVersion":1,"wrapperType":"QUERY","calls":[{"methodId":"query.or","field":null,"arguments":[],"nestedCalls":[{"methodId":"query.lt","field":"age","arguments":[{"type":"INT32","value":18}],"nestedCalls":[],"accumulators":[]},{"methodId":"query.gt","field":"age","arguments":[{"type":"INT32","value":60}],"nestedCalls":[],"accumulators":[]}],"accumulators":[]}],"unsupported":false,"unsupportedReason":null}
```

Comparator 的 expected 与 actual 均为：

```text
filter={"$or": [{"age": {"$gt": {"$numberInt": "60"}}}, {"age": {"$lt": {"$numberInt": "18"}}}]}; projection={}; sort={}
```

最终 Java：

```java
QueryWrapper<Object> wrapper = new QueryWrapper<>()
        .or(nested -> nested
            .lt("age", 18)
            .gt("age", 60));
```

### 2.4 IN + SORT

Command：

```javascript
db.user.find({status: {$in: ["A", "B"]}}).sort({create_time: -1})
```

Call Plan：

```json
{"schemaVersion":1,"wrapperType":"QUERY","calls":[{"methodId":"query.in","field":"status","arguments":[{"type":"ARRAY","value":[{"type":"STRING","value":"A"},{"type":"STRING","value":"B"}]}],"nestedCalls":[],"accumulators":[]},{"methodId":"query.order","field":"create_time","arguments":[{"type":"INT32","value":-1}],"nestedCalls":[],"accumulators":[]}],"unsupported":false,"unsupportedReason":null}
```

Comparator 的 expected 与 actual 均为：

```text
filter={"status": {"$in": ["A", "B"]}}; projection={}; sort={"create_time": {"$numberInt": "-1"}}
```

最终 Java：

```java
QueryWrapper<Object> wrapper = new QueryWrapper<>()
        .in("status", Arrays.asList("A", "B"))
        .order("create_time", -1);
```

### 2.5 Aggregate

Command：

```javascript
db.order.aggregate([
  {$match: {status: "PAID"}},
  {$group: {_id: "$user_id", total: {$sum: "$amount"}}},
  {$sort: {total: -1}}
])
```

Call Plan：

```json
{"schemaVersion":1,"wrapperType":"AGGREGATE","calls":[{"methodId":"aggregate.match","field":null,"arguments":[],"nestedCalls":[{"methodId":"query.eq","field":"status","arguments":[{"type":"STRING","value":"PAID"}],"nestedCalls":[],"accumulators":[]}],"accumulators":[]},{"methodId":"aggregate.group","field":"$user_id","arguments":[],"nestedCalls":[],"accumulators":[{"factoryId":"accumulator.sum","outputField":"total","arguments":[{"type":"STRING","value":"$amount"}]}]},{"methodId":"aggregate.sort","field":"total","arguments":[{"type":"INT32","value":-1}],"nestedCalls":[],"accumulators":[]}],"unsupported":false,"unsupportedReason":null}
```

真实 AggregateWrapper 保持 `$match → $group → $sort` 顺序。Pipeline Comparator 结果：

```text
Expected = [{"$match": {"status": {"$eq": "PAID"}}},{"$group": {"_id": "$user_id", "total": {"$sum": "$amount"}}},{"$sort": {"total": {"$numberInt": "-1"}}}]
Actual   = [{"$match": {"status": {"$eq": "PAID"}}},{"$group": {"_id": "$user_id", "total": {"$sum": "$amount"}}},{"$sort": {"total": {"$numberInt": "-1"}}}]
```

最终 Java：

```java
AggregateWrapper wrapper = new AggregateWrapper()
        .match(nested -> nested
            .eq("status", "PAID"))
        .group("$user_id", Accumulators.sum("total", "$amount"))
        .sort("total", -1);
```

## 3. 本地 pagination guard

```javascript
db.user.find({status: "ACTIVE"}).skip(20).limit(10)
```

| status | errorCode | modelAttempts | 实际 AI 调用数 |
|---|---|---:|---:|
| UNSUPPORTED | UNSUPPORTED_QUERY_PAGINATION | 0 | 0 |

该 Case 在 Parser 后、本地 capability guard 处终止，没有进入 LangChain4j 或 DeepSeek。

## 4. 真实诊断与最小 Prompt 修正

真实联调暴露的是 Prompt 输出契约不够显式，不是 Validator、Catalog、Registry 或 Comparator 缺陷。

| 真实证据 | 拒绝层 | 最小修正 |
|---|---|---|
| 首轮 5/5 JSON 都省略必需的 `unsupportedReason` | Call Plan exact-field schema，`MODEL_OUTPUT_INVALID` | 明确 `unsupported` 与 `unsupportedReason` 永远同时输出；false 时后者为 null |
| Aggregate 把 accumulator 当普通 call 输出 | Call Plan accumulator exact-field schema，`MODEL_OUTPUT_INVALID` | 明确 accumulator 的三个字段和 `accumulator.sum` 示例 |
| Aggregate 把 group id 放入 arguments；retry 又丢失 field/sum argument | Registry，`INVALID_PLAN_ARGUMENTS` | 明确 group id 映射到 `call.field`、call arguments 为空、sum 恰好一个参数 |

修正只发生在 `PlanPromptFactory.outputRules`；没有修改 Validator、Plan shape 校验、Catalog、Registry、
Comparator、Parser、Renderer、SSE 或 MongoPlus 生产能力。Prompt 规则修改后，最终验收轮 5/5 VERIFIED。

## 5. 验证命令

- `mvn -f mongo-plus-wrapper-converter/pom.xml -Pdeepseek-integration -Dtest=DeepSeekIntegrationTest test`：
  1 项真实 integration 测试通过，内部执行 5 个 Case、6 次模型请求。
- `mvn -f mongo-plus-wrapper-converter/pom.xml test`：97 tests，0 failures，0 errors，1 skipped；
  真实 DeepSeek integration 默认保持 skipped。
- `mvn -f mongo-plus-wrapper-converter/pom.xml -DskipTests compile`：BUILD SUCCESS。
- `git diff --check`：通过。

## 6. 复杂 Aggregate Prompt 回归（2026-08-21）

在人工复杂 Call Plan 已通过真实 Wrapper/Comparator 后，Aggregate Prompt 只补充以下已验证规则：
`aggregate.match` 的条件树必须放入 `nestedCalls`；`$in/$gte/$lt/$and/$or` 使用对应 query methodId；
同字段多个 operator 使用同级 calls；`DATE_TIME.value` 使用 epoch milliseconds JSON number；group 区分
`sum(field)` 与 `sum(1)`；多字段单 stage `$sort` 使用一次 `aggregate.sort.multi` 和有序 DOCUMENT。

本轮 `deepseek-v4-pro` 真实结果为 6/6 VERIFIED。复杂 Aggregate 首次响应即通过严格 JSON、Validator、
Catalog、Registry 和 Comparator，没有 retry；实际 pipeline 为 `$match → $group → $match → $sort`，且只有一个
`$sort` stage。Pagination 仍在本地返回 `UNSUPPORTED_QUERY_PAGINATION`，AI 调用数为 0。详细 attempt、
rawResponse、Expected/Actual、耗时和 Renderer 输出保存在 `target/live-validation-results.json`。
