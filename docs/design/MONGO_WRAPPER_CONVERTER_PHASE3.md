# MongoDB Command → MongoPlus Wrapper Converter：Phase 3 实现记录

> 实现基线：Java 21、Spring Boot 3.5.16、LangChain4j 1.19.0、Fastjson2 2.0.64、MongoPlus 2.2.0。  
> Phase 3 只接入 IR → DeepSeek → Call Plan；Phase 2 的 Parser、Catalog、Registry、真实 Wrapper、Verifier 和 Renderer 未重写。

## 1. 完整链路与边界

```text
Mongo Shell subset
  -> MongoCommandParser
  -> local capability guard
  -> MongoCommandIR
  -> DeepSeekMongoPlusPlanTranslator
  -> OpenAiStreamingChatModel (DeepSeek OpenAI-compatible endpoint)
  -> complete JSON text
  -> Fastjson2 parse
  -> MongoPlusCallPlanValidator
  -> MongoPlusApiCatalog / WrapperMethodRegistry
  -> real QueryWrapper / AggregateWrapper
  -> BsonComparator / MongoPlusPlanVerifier
  -> VERIFIED / REJECTED / UNSUPPORTED / ERROR
  -> MongoPlusJavaRenderer (VERIFIED only)
```

DeepSeek 只生成 `MongoPlusCallPlan`，不生成可信 Java。Parser、Verifier 和 Renderer 不依赖
LangChain4j。没有 Controller、HTTP SSE、前端、MongoDB 连接、动态编译、缓存或对 MongoPlus
生产源码的修改。

## 2. 实际依赖与 LangChain4j API

converter POM 通过 `langchain4j-bom:1.19.0` 管理并使用
`langchain4j-open-ai:1.19.0`。本地下载的实际 JAR 签名确认如下：

```java
OpenAiStreamingChatModel.builder()
    .baseUrl(...)
    .apiKey(...)
    .modelName(...)
    .temperature(...)
    .timeout(...)
    .maxTokens(...)
    .responseFormat("json_object")
    .customParameters(...)
    .build();
```

请求使用：

```java
StreamingChatModel.chat(ChatRequest, StreamingChatResponseHandler)
```

回调使用 `onPartialResponse(String)`、`onCompleteResponse(ChatResponse)` 和
`onError(Throwable)`。LangChain4j 1.19.0 的该 builder 没有 `maxRetries(...)`；项目配置中的
`converter.ai.max-retries` 明确定义为最多一次语义修正，不伪装成框架传输重试。

官方参考：

- <https://docs.langchain4j.dev/integrations/language-models/open-ai>
- <https://docs.langchain4j.dev/tutorials/response-streaming>

## 3. DeepSeek 当前配置

DeepSeek 当前 Chat Completions model id 是 `deepseek-v4-pro` 和
`deepseek-v4-flash`。默认配置选择 `deepseek-v4-pro`，但 `model-name` 完全外部化；不再使用
已到弃用期的 `deepseek-chat` / `deepseek-reasoner` 别名。

```yaml
converter:
  ai:
    enabled: false
    base-url: https://api.deepseek.com
    api-key: ${DEEPSEEK_API_KEY:}
    model-name: deepseek-v4-pro
    temperature: 0.0
    timeout: 45s
    max-output-tokens: 4096
    max-retries: 1
```

模型使用 `response_format={"type":"json_object"}`（LangChain4j 的
`responseFormat("json_object")`）。当前实现通过 `customParameters` 显式发送
`thinking.type=disabled`，使配置化 temperature 具有确定含义。API key 不写日志、不进入响应、
不提交实际值；AI 默认关闭。

官方参考：

- <https://api-docs.deepseek.com/api/list-models>
- <https://api-docs.deepseek.com/guides/json_mode/>
- <https://api-docs.deepseek.com/updates/>

## 4. Fastjson2 JSON 策略

converter 直接依赖 `com.alibaba.fastjson2:fastjson2:2.0.64`。业务代码没有直接使用
Jackson，也没有旧版 `com.alibaba:fastjson`。

### IR → JSON

`FastjsonMongoJsonSerializer` 把 `MongoCommandIR` 构造成 Fastjson2
`JSONObject` / `JSONArray`，最后调用 `JSON.toJSONString(...)`。BSON 类型使用 Canonical
Extended JSON 形状保存，例如：

```json
{"age":{"$gte":{"$numberInt":"18"}},"count":{"$numberLong":"2147483648"}}
```

ObjectId、DateTime、Decimal128、Double 和 Regex 也使用各自的 Extended JSON 标记，
不会退化为普通字符串。

### Catalog → 精简 JSON

`MongoPlusCatalogJsonCache` 在 Bean 构造时从现有 `MongoPlusApiCatalog` 生成 QUERY 和
AGGREGATE 两份 JSON 并缓存。每个方法只含：

```text
id + method + wrapperType + parameters(name/type/nullable)
```

Aggregate 额外包含受控 `accumulator.sum` factory。不会发送 `Method`、declaring class、
反射细节或 Java 实现。FIND/FIND_ONE 只选择 QUERY 文档，AGGREGATE 只选择 AGGREGATE
文档；请求路径不重新反射或重新序列化 Catalog。

### 模型 JSON → Call Plan

`MongoPlusCallPlanValidator` 在 streaming 正常完成后一次性处理完整字符串：

1. 要求整个响应恰好是一个 JSON object；Markdown、前后缀、注释和单引号拒绝。
2. `JSON.parseObject(...)` 开启 `DisableSingleQuote`、`ErrorOnNotSupportAutoType` 和
   `DuplicateKeyValueAsArray`。
3. 对 plan、call、accumulator、typed value 和 regex object 做 exact-field validation；
   未知字段和缺失字段都返回 `MODEL_OUTPUT_INVALID`。
4. 严格检查 wrapper enum、必需字符串、数组/对象形状和 typed value 的明显类型错误。
5. 只构造项目自己的 record 与固定 BSON value，不接受 className、Java type、任意 Method
   或 reflection target。
6. Schema 后仍继续执行 Catalog、Registry、真实 Wrapper 和 BSON/Pipeline Verifier。

Fastjson2 的 AutoType 默认关闭；本项目从不打开 `SupportAutoType` 或
`SupportClassForName`，并对 `@type` 使用 `ErrorOnNotSupportAutoType` fail closed。框架传递的
Jackson 依赖没有排除：实际依赖树中 LangChain4j 保留 `jackson-annotations 2.21`、
`jackson-core 2.21.4`、`jackson-databind 2.21.4`，它们属于框架内部实现。

## 5. Translator、Prompt 与 streaming

业务接口是：

```java
MongoPlusPlanTranslator.translate(MongoCommandIR, PlanRetryFeedback)
```

实现为 `DeepSeekMongoPlusPlanTranslator`。LangChain4j 被隔离在
`LangChain4jDeepSeekStreamingModel` 内，离线测试使用 `StreamingPlanModel` fake。

System prompt 少于 400 字符，只说明：输入已经是标准化 IR、只输出一个 JSON object、只能使用
Catalog method id、使用 MongoDB String field、禁止 Lambda/创造方法/近似表达、不能准确表达时
返回 unsupported。User prompt 是一个 Fastjson2 object，只含 operation-specific IR、精简
Catalog、Call Plan 字段规则及一个 operation-specific JSON 示例；测试把简单 Query prompt
限制在 8 KiB 内，并断言不含源码、`docs/ai`、README 或 Java package dump。

每个 content chunk 只追加到最多 262144 个 Java UTF-16 `char` 的有界 `StringBuilder`。半截 JSON 不解析；正常
`onCompleteResponse` 后才做一次严格解析。`onError`、同步启动异常、45 秒超时或输出超限都成为
`MODEL_STREAM_ERROR`。Phase 3 不把 token 直接转换为 Java。

## 6. Local unsupported 与一次修正

`MongoWrapperConversionService` 在模型前完成 parse 和本地 capability 判断。普通 find 的
skip/limit 直接返回 `UNSUPPORTED_QUERY_PAGINATION`，模型调用次数为 0；Parser/安全拒绝同样
不调用模型。

有效 plan 第一次经 Catalog/Registry/Verifier 成为 `REJECTED` 或 plan-level `ERROR` 时，最多
再调用模型一次。retry user JSON 只包含原始 IR、当前 Catalog、失败 code/reason、Expected、
Actual，并要求重新生成完整 plan；不发送异常堆栈。第二次仍不匹配统一终止为 `REJECTED`。
syntax、local unsupported、模型 streaming error 和模型 JSON 无法解析不做语义 retry。

## 7. 测试与验证结果

Phase 3 新增 31 个默认离线测试，覆盖 operation-specific Catalog、Fastjson2 IR/Catalog、合法与
非法/截断/Markdown JSON、未知 wrapper、未知/缺失/重复字段、错误 typed value、AutoType、fake
method、参数形状、Verifier rejection、一次修正、连续失败、local pagination、Parser failure、
streaming error、aggregate、显式 unsupported、默认 AI 关闭和 prompt 边界。

另有 1 个真实 `DeepSeekIntegrationTest`。它只有在同时满足以下条件时运行：

```text
mvn -Pdeepseek-integration test
DEEPSEEK_API_KEY 存在
```

默认 `mvn test` 不构造 AI model、不调用 DeepSeek、不连接 MongoDB。当前默认测试结果：

```text
Tests run: 57, Failures: 0, Errors: 0, Skipped: 1
```

其中原 Phase 2 的 25 个测试全部继续通过，唯一 skipped 是真实 DeepSeek integration。

## 8. Phase 4 Controller / SSE 前置接口

Phase 3 已有内部 `StreamingPlanModel` 的 chunk/completion/error 边界，但同步 Translator 只在完整
Call Plan 后返回。Phase 4 仍需增加：

1. request-scoped `ConversionProgressListener` / event sink，把 `GENERATING`、`VERIFYING`、
   `VERIFIED`、`REJECTED`、`UNSUPPORTED`、`ERROR` 映射为稳定 SSE envelope；
2. 可取消 handle 与 client disconnect/timeout 清理，确保断开后丢弃 chunk；
3. 异步执行边界，避免 Controller 线程阻塞等待完整模型输出；
4. request id、sequence、copyAllowed 和终止事件的一致状态机；
5. Controller request validation 与 MockMvc SSE 回归。

这些接口不改变核心安全边界：SSE 可以展示生成进度，但 Java 仍只能在完整 Call Plan 通过本地
Verifier 后以 `VERIFIED` 形式返回。

## 9. Aggregate 多字段排序能力补充

复杂 Aggregate 的单 stage 多字段 `$sort` 使用 `aggregate.sort.multi` 表达。该调用复用既有
`TypedValue.DOCUMENT`，document 字段顺序即排序键优先级；Registry 严格要求非空 document、非空字段名，
并且每个方向必须是 `INT32` 类型的 `1` 或 `-1`。

Registry 通过 MongoPlus 已公开的 `AggregateWrapper.sort(Bson)` 执行：先用 MongoDB Driver
`Aggregates.sort(Document)` 把有序 sort specification 构造成完整 stage，再调用真实 Wrapper API。
Renderer 使用完全相同的 `sort(Aggregates.sort(new Document().append(...)))` 形式，保证验证语义与输出 Java
一致。原有 `aggregate.sort` 继续只表示单字段排序，两个连续调用仍是两个 stage，不视为多字段排序。
