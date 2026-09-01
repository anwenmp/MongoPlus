# MongoDB Command → MongoPlus Wrapper 转换服务：第一阶段设计

> 设计基线：MongoPlus `2.2.0` 当前源码，2026-08-21。  
> 本阶段只完成架构与能力核验，不修改 MongoPlus 生产代码，不创建完整后台项目。

## 0. 结论先行

方案可行，且当前 MongoPlus 已经提供本地验证所需的公开读取路径：

- `QueryWrapper.buildCondition()` 返回 filter、projection、sort。
- `AggregateWrapper.getAggregateConditionList()` 返回按调用顺序保存的 pipeline。
- 两者都可以离线构造和转成 BSON，不需要连接 MongoDB，也不需要动态编译。

但当前源码存在一个必须在编码前确认的能力边界：

- `QueryWrapper` 没有 `skip()` 和 `limit()`。
- `skip/limit` 只存在于 `AggregateWrapper`；普通查询分页位于 Mapper/Chain 终结层。
- 因此需求示例中的 `new QueryWrapper<>().skip(20).limit(10)` 在 MongoPlus 2.2.0 中不是合法代码。

准确性优先时，第一版不能伪造这两个方法。本设计把普通 find 的 `skip/limit` 解析进 IR，但在没有后续产品决策前返回 `UNSUPPORTED_QUERY_PAGINATION`。无分页的 find/findOne 和受支持的 aggregate 可以完成端到端 `VERIFIED`。

## 1. 需求理解

服务只做四件事：

1. 用受限语法解析器把不可信 Mongo Shell 子集转换成带 BSON 类型的 IR。
2. 把 IR 和与 operation 对应的最小 API Catalog 发给 DeepSeek，由模型选择真实 MongoPlus 调用。
3. Java 通过白名单 Registry 调用真实 `QueryWrapper`/`AggregateWrapper`，将实际 BSON/Pipeline 与 IR 期望值比较。
4. 只从通过验证的 Call Plan 渲染 Java；只有 `VERIFIED` 结果允许复制。

服务不连接 MongoDB，不执行用户 JavaScript，不执行模型生成的 Java，不动态编译，不把原始 Mongo Shell 交给模型。

## 2. 当前 MongoPlus 事实

关键源码：

- `mongo-plus-core/src/main/java/com/mongoplus/conditions/query/QueryWrapper.java`
- `mongo-plus-core/src/main/java/com/mongoplus/conditions/query/QueryChainWrapper.java`
- `mongo-plus-core/src/main/java/com/mongoplus/conditions/AbstractChainWrapper.java`
- `mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java`
- `mongo-plus-core/src/main/java/com/mongoplus/model/BaseConditionResult.java`
- `mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java`
- `mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java`
- `mongo-plus-core/src/main/java/com/mongoplus/aggregate/AggregateWrapper.java`

确认结果：

- `QueryWrapper<T>` 可直接无参构造，字符串字段名不会依赖用户 Entity。
- `buildCondition()` 的实际结果是 `BaseConditionResult(condition, projection, sort)`。
- `eq` 生成显式 `{field: {$eq: value}}`，不是 `{field: value}` 简写。
- `order(String, Integer)` 能保持混合方向的多字段排序调用顺序。
- `projectDisplay`/`projectNone` 支持字符串字段以及 `_id` 显隐。
- 当前没有 `AggregateChainWrapper`；构建类型是 `AggregateWrapper`，执行链类型是 `LambdaAggregateChainWrapper`。
- Aggregate stage 在调用时立即变成 `Bson` 并追加到 `CopyOnWriteArrayList`，stage 顺序就是调用顺序。
- Query 和 Aggregate Wrapper 都是可变对象；每次转换必须创建新实例，绝不跨请求共享。

## 3. QueryWrapper 能力矩阵

“首版”表示本服务第一批允许进入 AI Catalog 的范围，不等于 MongoPlus 的全部能力。

| MongoDB | 当前 MongoPlus 真实 API | 表达准确性 | 首版 |
|---|---|---:|---:|
| 字段直接值 / `$eq` | `eq(String, Object)` | 准确；比较前把简写规范化为显式 `$eq` | 支持 |
| `$ne` | `ne(String, Object)` | 准确 | 支持 |
| `$gt/$gte/$lt/$lte` | 同名字符串重载 | 准确 | 支持 |
| `$in/$nin` | `in/nin(String, Collection)` | 准确；保留元素 BSON 类型 | 支持 |
| `$all` | `all(String, Collection)` | 准确 | 支持 |
| `$exists` | `exists(String, Boolean)` | 准确 | 支持 |
| `$size` | `size(String, Integer)` | 准确 | 支持 |
| `$elemMatch` | `elemMatch(String, QueryChainWrapper)` | 可由受控子 Wrapper 表达 | 支持 |
| `$and/$or/$nor` | 同名方法接收子 Wrapper/function | 可表达；逻辑数组按支持子集比较 | 支持 |
| 字段级 `$not` | `not(QueryChainWrapper)` | 只接受单字段、单操作符子条件 | 有限支持 |
| `$regex` | `regex(String,Object,RegexOptions)` | 仅能表达单个 `i/m/x/s/u`；无 options 默认仍生成 `i`，多 flag 不能直译 | 有限支持 |
| `$type` | `type(String, TypeEnum/String/Integer)` | API 存在 | 第二批 |
| `$mod` | `mod(...)` | API 存在 | 第二批 |
| `$expr` | `expr(...)` | API 存在但当前构造边界不覆盖任意表达式 | 不支持 |
| `$jsonSchema` | 对应接口为空 | 无可用公开构造 | 不支持 |
| `$where` | MongoPlus 有入口 | 会执行 JavaScript，服务层永久禁用且不进入 Catalog | 拒绝 |
| bitwise / geo / text | MongoPlus 有对应接口 | 未纳入首版验证矩阵 | 第二批 |
| projection include/exclude | `projectDisplay/projectNone` | 准确；禁止除 `_id` 外混用 1/0 | 支持 |
| sort | `order/orderByAsc/orderByDesc` | 准确；字段顺序保留 | 支持 |
| find skip/limit | `QueryWrapper` 无此 API | 无法由 Wrapper 本体准确表达 | 暂不支持 |

`custom(Bson)` 虽然存在，但首版 Query Catalog 不开放通用 custom。否则模型可以退化成“直接生成 BSON”，削弱方法选择约束。只有经过单独登记、参数 Schema 明确、并有 Golden Case 的特定 Bson 重载才可逐项开放。

## 4. AggregateWrapper 能力矩阵

| MongoDB stage | 当前 MongoPlus 真实 API | 表达准确性 | 首版 |
|---|---|---:|---:|
| `$match` | `match(QueryChainWrapper)` / `match(Bson)` | Query 子集可准确表达 | 支持 |
| `$project` | `projectDisplay/projectNone/project(Bson)` | 纯 include/exclude 首批支持 | 支持 |
| `$sort` | `sort(String,Integer)`、`sortAsc/Desc`、`sort(Bson)` | 单字段直接支持；混合方向需一个受控完整 sort stage | 支持 |
| `$skip/$limit` | `skip(int/long)`、`limit(int/long)` | 准确，且 stage 顺序保留 | 支持 |
| `$group` | `group(id, BsonField...)` | 搭配受控 `Accumulators` Factory Catalog 可准确表达 | 支持 `$sum` |
| `$unwind` | `unwind(String[,UnwindOption])` | 简单 path 首批支持 | 支持简单形式 |
| `$lookup` | `lookup(from,localField,foreignField,as)` 等 | 四字段基础形式准确 | 支持基础形式 |
| `$addFields/$set` | 同名方法接收 Field/Bson | 简单 literal/field reference 可表达 | 第二批 |
| `$unset` | `unset(String...)` | 准确 | 第二批 |
| `$count` | `count([field])` | 准确 | 第二批 |
| `$facet/$unionWith` | 对应 Aggregate 或 pipeline API | 需要递归 pipeline Schema | 第二批 |
| pipeline `$lookup` + let | 对应重载存在 | 需要变量和子 pipeline 严格建模 | 第二批 |
| `$bucket/$bucketAuto/$graphLookup` | API 存在 | 尚未建立 Golden Case | 不支持首版 |
| `$out/$merge` | API 存在并改变执行结果行为 | 写入型 stage，不符合只读工具边界 | 拒绝 |
| `$function/$accumulator` | 可嵌入原生 BSON | 需要执行 JavaScript | 拒绝 |

首版 Aggregate Catalog 还需要一个很小的辅助 Catalog，例如 `Accumulators.sum(String,Object)`；它只用于构造 `group` 所需的 `BsonField`，同样以当前 MongoDB Driver 5.4.0 和 MongoPlus 2.2.0 实际签名为准。

## 5. Java、Spring Boot、LangChain4j 与 DeepSeek 版本

最终选择：

| 项目 | 选择 | 原因 |
|---|---|---|
| Java | 21 LTS | LangChain4j 最低 17；Boot 与 MongoPlus core 均可运行在 21；虚拟线程和长期部署更合适 |
| Spring Boot | 3.5.16 | 当前稳定线，生态成熟；满足 LangChain4j 官方要求的 Boot 3.5+，降低 Boot 4 初期迁移面 |
| LangChain4j | 1.19.0 BOM + `langchain4j-open-ai` | 官方当前稳定版本；直接配置模型 Bean |
| LangChain4j starter | 不使用首版 beta starter | 1.19.0 对应 starter 仍是 `beta29`；本服务配置面很小，手动 Bean 更稳定 |
| DeepSeek | `deepseek-v4-pro`（配置化） | 准确性优先；`deepseek-v4-flash` 可作为以后低延迟选项 |
| MongoPlus | `com.mongoplus:mongo-plus-core:2.2.0` | 与当前源码和 Catalog 绑定 |
| MongoDB Driver | 固定 5.4.0 | 与当前 MongoPlus POM 对齐，防止 Boot BOM 改变实际 BSON 行为 |

新项目建议目录名 `mongo-plus-wrapper-converter`，放在当前仓库根目录下，但：

- 不加入根 `pom.xml` 的 `<modules>`。
- 自己使用 `spring-boot-starter-parent`，绝不以 `com.mongoplus:mongo-plus` 为 parent。
- 只通过普通 Maven dependency 使用 `mongo-plus-core:2.2.0`。

依据：

- [LangChain4j Get Started](https://docs.langchain4j.dev/get-started/)
- [LangChain4j Spring Boot Integration](https://docs.langchain4j.dev/tutorials/spring-boot-integration/)
- [Spring Boot System Requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [DeepSeek Chat Completions API](https://api-docs.deepseek.com/api/create-chat-completion)

## 6. Spring Boot 与 SSE 方案

选择 Spring MVC + `SseEmitter`，不引入 WebFlux：

- LangChain4j streaming 本身是回调式，和 `SseEmitter` 直接匹配。
- 服务没有响应式数据库或其他必须使用 Reactor 的链路。
- Controller 只做 request 校验、创建 emitter、委托 Service；所有 AI 与转换逻辑都在 Service/AI 层。
- emitter 的 completion/timeout/error 回调只关闭本请求上下文；后续模型 delta 被丢弃，不再写已关闭连接。

接口：

```text
POST /api/converter/mongo-to-wrapper
Content-Type: application/json
Accept: text/event-stream

{"command":"db.user.find({status:'ACTIVE'})"}
```

## 7. Parser 设计

实现专用 Lexer + 递归下降 Parser，不使用 JS 引擎，也不把 shell 文本交给 `Document.parse` 猜测。

首版 grammar：

```text
command    := "db" "." collection "." operation "(" arguments? ")" modifier* ";"? EOF
operation  := "find" | "findOne" | "aggregate"
modifier   := ".sort(" object ")" | ".skip(" integer ")" | ".limit(" integer ")"
value      := object | array | string | number | true | false | null
            | ObjectId(string) | ISODate(string)
            | NumberInt(string?) | NumberLong(string?) | NumberDecimal(string)
            | regexLiteral
```

解析器负责：

- 语法完整消费到 EOF，不接受尾随脚本、赋值、函数调用或链外表达式。
- 只接受允许的 operation、modifier、operator、stage 和 literal constructor。
- 在进入 AI 前拒绝 `$where`、`$function`、`$accumulator`、`mapReduce`、`$out`、`$merge`。
- 校验 ObjectId 为 24 位十六进制、日期为 ISO-8601、数值范围及 projection 规则。
- 保留对象键插入顺序，尤其是 sort 和 pipeline。
- 返回定位明确的错误：code、line、column、JSON pointer/语法上下文。

类型规则：

- 普通无小数整数：在 int32 范围为 `INT32`，否则为 `INT64`；溢出拒绝。
- 普通小数/指数：`DOUBLE`。
- `NumberInt/NumberLong/NumberDecimal`：分别固定为 `INT32/INT64/DECIMAL128`。
- `ObjectId`、`ISODate`、regex 使用专用 typed value，不退化成 String。
- 不能可靠解析的 constructor 返回 `UNSUPPORTED_LITERAL`。

## 8. MongoCommandIR

内部使用不可变 record，并同时保存语义 IR 与期望 BSON：

```java
record MongoCommandIR(
        int schemaVersion,
        MongoOperation operation,
        String collection,
        QueryIR query,
        List<PipelineStageIR> pipeline,
        BsonExpectation expected) {}

record QueryIR(
        BsonDocument filter,
        BsonDocument projection,
        BsonDocument sort,
        long skip,
        Long limit) {}
```

发送给 AI 时使用 Canonical Extended JSON；ObjectId、Date、Int64、Decimal128、Regex 都带明确类型标记。原始 Mongo Shell 不进入 prompt。

## 9. AI 层与 Prompt

`MongoWrapperAiService` 封装 `OpenAiStreamingChatModel`，配置：

- `baseUrl=https://api.deepseek.com`
- model 默认 `deepseek-v4-pro`
- `response_format=json_object`
- thinking 默认 enabled、reasoning effort high；thinking 模式下不设置 temperature，因为 DeepSeek 官方说明该参数不会生效
- API key 只来自 `${DEEPSEEK_API_KEY}` 或外部 secret
- max output、模型 timeout、总请求 timeout 均由 `AiProperties` 配置

System prompt 只包含稳定规则：输出 JSON、只能使用 Catalog methodId、不得创造方法、不得改变 literal 类型、不得输出 Java/Markdown。User message 只包含：

```json
{
  "ir": { "...": "canonical extended json" },
  "catalog": { "...": "operation-specific catalog" },
  "planSchema": { "...": "compact schema and one example" }
}
```

IR 中所有字符串都被声明为不可信数据，不能作为模型指令。

## 10. Streaming 与 Structured Output

当前不能把“流式 + DeepSeek + LangChain4j POJO JSON Schema”当成已保证能力：

- DeepSeek 当前文档保证的是 `json_object`，不是 response JSON Schema。
- LangChain4j 文档明确说明 OpenAI JSON Schema Structured Output 尚不支持 streaming。

因此首版采用：

1. `OpenAiStreamingChatModel` 接收 content delta。
2. 增量只追加到有长度上限的 buffer，并向前端发送生成进度；不使用 regex 截 JSON。
3. 完成后检查 finish reason，要求 `STOP`。
4. Jackson 开启 unknown-property rejection、duplicate-key detection 和 stream constraints，严格反序列化为 `MongoPlusCallPlan`。
5. 继续做 Catalog/参数/调用次数/类型校验。

这仍是结构化输出，只是最终 Schema 裁决由 Java 完成。官方参考：[LangChain4j Structured Outputs](https://docs.langchain4j.dev/tutorials/structured-outputs/) 与 [DeepSeek JSON Output](https://api-docs.deepseek.com/guides/json_mode/)。

## 11. MongoPlusCallPlan

计划只引用稳定 methodId，不接受 className 或任意 Java 类型：

```java
record MongoPlusCallPlan(
        int schemaVersion,
        WrapperKind wrapper,
        String mongoPlusVersion,
        List<WrapperCall> calls) {}

record WrapperCall(
        String methodId,
        String field,
        List<PlanValue> args,
        List<List<WrapperCall>> groups,
        List<AccumulatorCall> accumulators) {}

record PlanValue(BsonValueKind type, JsonNode value) {}
record AccumulatorCall(String factoryId, String outputField, List<PlanValue> args) {}
```

示例：

```json
{
  "schemaVersion": 1,
  "wrapper": "QUERY_WRAPPER",
  "mongoPlusVersion": "2.2.0",
  "calls": [
    {"methodId":"query.gte.string-object","field":"age","args":[{"type":"INT32","value":18}],"groups":[],"accumulators":[]},
    {"methodId":"query.eq.string-object","field":"status","args":[{"type":"STRING","value":"ACTIVE"}],"groups":[],"accumulators":[]},
    {"methodId":"query.order-desc.string","field":"create_time","args":[],"groups":[],"accumulators":[]}
  ]
}
```

所有字段都要求出现，未知字段拒绝；`null` 仅在 method descriptor 明确允许时可用。

## 12. MongoPlusApiCatalog

应用启动时构建一次不可变 Catalog：

1. 对 `QueryWrapper.class.getMethods()` 与 `AggregateWrapper.class.getMethods()` 反射。
2. 只保留受信 package、返回类型和 `AllowedMethodPolicy` 中明确列出的 descriptor。
3. 给每个重载生成稳定 methodId；methodId 同时包含 wrapper、语义名和参数种类。
4. 若预期方法缺失、签名漂移或出现歧义，应用启动失败，而不是请求时猜测。
5. 计算 `catalogHash`，与 MongoPlus 版本一起进入响应和缓存 key。

Catalog 条目：

```java
record CatalogMethod(
        String id,
        String javaName,
        WrapperKind owner,
        List<ParameterKind> parameters,
        boolean varArgs,
        int maxArguments) {}
```

不进入 Catalog：getter、`clear`、`lambdaQuery`、任意 `Class`/`SFunction` 参数、写入型 aggregate stage、通用反射/custom 入口以及不在首版能力矩阵中的方法。

## 13. WrapperMethodRegistry

Registry 在启动时由 Catalog 生成 `Map<methodId, RegisteredMethod>`，其中保存已解析的 `MethodHandle` 和参数转换器。

受控调用规则：

- target 只能是本请求新建的 `QueryWrapper<Object>` 或 `AggregateWrapper`。
- 不使用 `Class.forName`，不接受 LLM 给出的类名/方法名/参数类型。
- `PlanValue` 只能转换为固定 BSON/Java 类型；ObjectId、Date、Decimal128、Regex 单独校验。
- nested group 只能递归构建新的 QueryWrapper；总深度和总 call 数受限。
- `group` accumulator 只能来自单独白名单 Factory Registry。
- 每次调用后验证返回对象仍是同一允许的 wrapper。
- 未找到 methodId、参数数量不符、类型不符、字段为空或 invocation 异常，计划立即无效。

Java 没有 `$gte -> gte` 的 operator switch；哪个 methodId 被选中完全来自 AI。Java 只执行白名单签名和验证结果。

## 14. BSON / Pipeline 验证

### Query

```java
BaseConditionResult actual = queryWrapper.buildCondition();
actual.getCondition();
actual.getProjection();
actual.getSort();
```

三部分分别转成 `BsonDocument` 后与 IR 比较。`skip/limit` 是独立 execution semantics；当前 QueryWrapper 无法提供时，不能忽略它们并标记 VERIFIED。

### Aggregate

```java
List<Bson> actualPipeline = aggregateWrapper.getAggregateConditionList();
```

逐 stage 使用固定 CodecRegistry 转成 `BsonDocument`。stage 个数、顺序和值必须一致。

### Canonical 规则

- 普通 Document key order 忽略；sort 的字段顺序保留。
- pipeline stage 顺序绝对保留。
- 标量字段简写与显式 `$eq` 统一为 `$eq`。
- `$and/$or/$nor` 子项按 canonical bytes 作为多重集合比较。
- `$in/$nin/$all` 元素顺序忽略，但不改变普通 literal array 的顺序。
- projection 按字段和值比较，并先校验 include/exclude 合法性。
- BSON numeric type 精确比较，不把 int32/int64/double/decimal128 混同。
- regex 比较 pattern 和规范化后的 options；不忽略 flags。
- 不做分配律、stage 合并、表达式化简等一般 MongoDB 语义证明。

Diff 只输出有界 JSON pointer 列表，例如 `expected /filter/age/$gte INT32:18, actual STRING:"18"`。

## 15. 自动修正与重试

整个转换最多两个 AI attempt：

- attempt 1：IR + Catalog 生成计划。
- 若 Schema、Catalog 或 BSON 比较失败，attempt 2 收到精简的 expected/actual/diff 和失败 code，生成一次修正版。
- attempt 2 仍失败即 `REJECTED`。

传输失败和语义修正共享总 attempt budget，避免一次请求放大为多次重试。429、5xx、连接中断只能在预算内重试；用户取消、4xx 参数错误、明确安全拒绝不重试。

## 16. Java Renderer

Renderer 只消费 Schema 合法且已完成受控调用的 Call Plan。最终可复制代码只取自 `VERIFIED` 计划。

职责：

- 使用 `QueryWrapper<Object>` / `AggregateWrapper` 的真实构造形式。
- 根据 methodId 渲染已登记的方法名和参数顺序。
- 正确转义 Java String、字符和 Unicode。
- 分别渲染 `ObjectId`、`Instant/Date`、`Decimal128`、List、Document、RegexOptions。
- group 使用受控 `Accumulators` helper。
- 不生成 Entity getter、用户不存在的类型或 import。

Renderer 自己不决定 operator→method；它只格式化已验证 methodId。

## 17. SSE 协议与状态机

统一 envelope：

```json
{
  "requestId":"uuid",
  "sequence":4,
  "type":"VERIFYING",
  "status":"DRAFT",
  "data":{},
  "timestamp":"2026-08-21T00:00:00Z"
}
```

正常状态：

```text
PARSED -> GENERATING -> GENERATED -> CODE_DELTA(DRAFT) -> VERIFYING -> VERIFIED
```

终止分支：

- 语法非法或禁止能力：`REJECTED`
- 语法合法但当前能力无法准确表达：`UNSUPPORTED`
- AI/网络/内部异常：`ERROR`
- Catalog/比较失败且修正仍失败：`REJECTED`

模型 streaming 期间发送 `GENERATING` 进度（delta 字符数/attempt），不把模型原始文本当 Java。完整计划通过 Schema 后，Renderer 可发送后端生成的 `CODE_DELTA`，但标记 `DRAFT` 且 `copyAllowed=false`。最终 `VERIFIED` 事件携带完整代码、MongoPlus 版本、Catalog hash 和 `copyAllowed=true`。

## 18. 安全限制

建议首版默认值，全部可外部配置：

| 限制 | 默认值 |
|---|---:|
| command UTF-8 长度 | 16 KiB |
| BSON 最大深度 | 32 |
| 单数组元素 | 1,000 |
| 单对象字段 | 500 |
| pipeline stage | 50 |
| Call Plan calls | 100 |
| nested group 深度 | 16 |
| LLM 输出 | 4,096 tokens / 256 KiB buffer |
| AI timeout | 45 s |
| 总请求 timeout | 60 s |
| AI attempt 总数 | 2 |

其他规则：

- 禁止 eval、ScriptEngine、Nashorn、GraalJS、动态编译和任意代码执行。
- API key 不入日志、不入响应、不提交；生产环境关闭完整 prompt/response 日志。
- 日志只记录 requestId、输入 hash、operation、状态、耗时、token usage 和错误 code。
- SSE 数据始终 JSON 编码；前端必须按纯文本展示代码，不能 `innerHTML`。
- 不接受任意 URL、文件路径、className 或反射 target。

## 19. 缓存设计

先定义 `VerifiedConversionCache` 接口，首个编码迭代可使用 no-op；有重复流量证据后再启用 Caffeine，不直接引入 Redis。

key：

```text
SHA-256(canonicalIR + mongoPlusVersion + catalogHash + converterSchemaVersion + promptVersion + model)
```

只缓存 `VERIFIED`，不缓存 `ERROR`，也不把未验证草稿作为命中结果。缓存命中仍通过 SSE 发送 `PARSED -> VERIFIED`，并标记 `cacheHit=true`。

## 20. Golden Cases

每个 fixture 包含 input、expected IR、固定 Call Plan、expected BSON/Pipeline、expected Java、expected status。

| Case | 内容 | 期望 |
|---|---|---|
| Q1 | scalar eq | VERIFIED |
| Q2 | `$gte` int32 | VERIFIED |
| Q3 | gte + eq | VERIFIED |
| Q4 | `$or` 两个范围 | VERIFIED |
| Q5 | `$in` + 多字段 sort | VERIFIED |
| Q6 | find projection include，默认保留 `_id` | VERIFIED |
| Q7 | find projection include + `_id:0` | VERIFIED |
| Q8 | findOne + ObjectId | VERIFIED |
| Q9 | regex 单 option `i` | VERIFIED |
| Q10 | regex 无 option / 多 options | UNSUPPORTED（当前 direct API 不精确） |
| Q11 | find + skip/limit | UNSUPPORTED_QUERY_PAGINATION |
| A1 | `$match` | VERIFIED |
| A2 | match + group sum + sort | VERIFIED |
| A3 | basic lookup | VERIFIED |
| A4 | unwind + skip + limit | VERIFIED |
| S1 | `$where` | REJECTED |
| S2 | `$function/$accumulator` | REJECTED |
| S3 | trailing JavaScript / assignment | REJECTED |
| S4 | over-depth / over-size | REJECTED |
| P1 | unknown methodId / extra JSON field | REJECTED after one repair |
| P2 | BSON type changed by plan | REJECTED after compare |

绝大多数测试完全离线；真实 DeepSeek integration test 使用独立 Maven profile/tag，只有存在 `DEEPSEEK_API_KEY` 时运行。

## 21. 推荐 package 结构

```text
com.mongoplus.converter
├─ MongoPlusConverterApplication
├─ controller
│  └─ MongoWrapperConvertController
├─ service
│  └─ MongoWrapperConvertService
├─ parser
│  ├─ MongoCommandLexer
│  ├─ MongoCommandParser
│  └─ ParserLimits
├─ ir
│  ├─ MongoCommandIR
│  ├─ MongoOperation
│  ├─ QueryIR
│  └─ PipelineStageIR
├─ ai
│  ├─ MongoWrapperAiService
│  ├─ DeepSeekConfiguration
│  └─ PromptFactory
├─ catalog
│  ├─ MongoPlusApiCatalog
│  ├─ AllowedMethodPolicy
│  ├─ WrapperMethodRegistry
│  └─ AccumulatorMethodRegistry
├─ plan
│  ├─ MongoPlusCallPlan
│  ├─ WrapperCall
│  └─ PlanValue
├─ verify
│  ├─ MongoPlusWrapperVerifier
│  ├─ BsonCanonicalizer
│  ├─ BsonComparator
│  └─ BsonDiff
├─ render
│  └─ MongoPlusJavaRenderer
├─ stream
│  ├─ ConvertEvent
│  ├─ ConvertEventType
│  └─ SseEventSink
├─ cache
│  └─ VerifiedConversionCache
└─ config
   ├─ AiProperties
   └─ ConverterProperties
```

## 22. 端到端示例

输入：

```javascript
db.user.find({
  age: {$gte: 18},
  status: "ACTIVE"
}).sort({create_time: -1}).limit(10)
```

Parser IR（缩写）：

```json
{
  "operation":"FIND",
  "collection":"user",
  "filter":{"age":{"$gte":{"$numberInt":"18"}},"status":{"$eq":"ACTIVE"}},
  "projection":{},
  "sort":{"create_time":-1},
  "skip":0,
  "limit":10
}
```

服务在 AI 调用前即可确定：QueryWrapper 2.2.0 无 limit 能力。为避免生成不存在的 `.limit(10)`，首版结果是：

```text
PARSED
UNSUPPORTED(code=UNSUPPORTED_QUERY_PAGINATION)
```

不会调用 DeepSeek，不会返回可复制代码。这是准确性原则对原始示例的正确裁决。

去掉 `.limit(10)` 后，发送给 DeepSeek 的内容只有规范化 IR、Query Catalog 和 Call Plan Schema。可接受计划为：

```json
{
  "schemaVersion":1,
  "wrapper":"QUERY_WRAPPER",
  "mongoPlusVersion":"2.2.0",
  "calls":[
    {"methodId":"query.gte.string-object","field":"age","args":[{"type":"INT32","value":18}],"groups":[],"accumulators":[]},
    {"methodId":"query.eq.string-object","field":"status","args":[{"type":"STRING","value":"ACTIVE"}],"groups":[],"accumulators":[]},
    {"methodId":"query.order-desc.string","field":"create_time","args":[],"groups":[],"accumulators":[]}
  ]
}
```

Registry 调用真实 API：

```java
QueryWrapper<Object> wrapper = new QueryWrapper<Object>()
        .gte("age", 18)
        .eq("status", "ACTIVE")
        .orderByDesc("create_time");
```

`buildCondition()` 实际得到：

```json
filter     = {"age":{"$gte":18},"status":{"$eq":"ACTIVE"}}
projection = {}
sort       = {"create_time":-1}
```

Canonical compare 全部相等后才发送 `VERIFIED(copyAllowed=true)`。

## 23. 最复杂的三个技术点

1. **受限 Shell Parser 与 BSON 类型保真**：既要接受官网常见输入，又不能执行脚本；ObjectId、Date、数值宽度、Decimal128、Regex 必须从第一步就带类型。
2. **Call Plan 白名单调用**：Reflection Catalog、稳定 methodId、重载解析、nested wrapper 和 accumulator factory 必须同时做到可扩展且不能变成任意反射执行器。
3. **Canonical 等价比较**：过滤条件可以忽略部分顺序并规范化 eq/logical array，但 sort 与 pipeline 顺序不能忽略；必须在有限支持范围内精确，而不是宣称解决所有 MongoDB 语义等价。

## 24. 是否需要修改 MongoPlus 与下一阶段顺序

### 是否需要修改 MongoPlus

- 为 filter/projection/sort/pipeline 的本地读取与验证：**不需要**。当前公开 API 足够。
- 为完全满足 `find().skip().limit()` → `QueryWrapper`：**当前需要产品决策**。可选方案：
  1. 首版明确不支持普通 find 分页（本设计默认）。
  2. 允许生成 Mapper/Chain 的分页终结代码；只能准确覆盖可映射的 skip/limit，且输出不再是纯 Wrapper。
  3. 经单独批准后给 MongoPlus 增加公开 skip/limit 能力。

本阶段不修改 MongoPlus。

### 下一阶段编码顺序

1. 新建独立 Maven/Spring Boot 骨架，锁定 Java/Boot/LangChain4j/MongoPlus/Driver 版本。
2. 先写 Parser + typed IR + 安全限制及测试。
3. 写 Catalog 启动快照测试与 Registry 固定计划测试。
4. 写实际 Query/Aggregate BSON 提取、Canonicalizer、Comparator 和 Golden Cases。
5. 写 Renderer 测试。
6. 写 DeepSeek streaming adapter，以 fake model 完成离线测试。
7. 串联 Service、SseEmitter 状态机与 MockMvc SSE 测试。
8. 最后才运行少量真实 DeepSeek integration test，并根据失败样本收紧 prompt/catalog。

