# MongoDB Command → MongoPlus Wrapper Converter：Phase 4 实现记录

> 实现基线：Java 21、Spring Boot 3.5.16、LangChain4j 1.19.0、MongoPlus 2.2.0。
>
> 本阶段只增加 HTTP/SSE、请求生命周期和离线 Controller 回归；不增加前端，不修改 MongoPlus 生产源码。

## 1. 最终选择：Spring MVC

采用 Spring MVC + `SseEmitter`，没有引入 WebFlux：

- 当前应用是普通 Spring Boot，Phase 3 的 LangChain4j streaming 是 callback API，没有 Reactor 链路。
- `spring-boot-starter-web` 已能覆盖 JSON、Servlet async、SSE 和 MockMvc。
- Controller 只做浅层 HTTP 校验、创建 emitter 和委托；Parser、模型重试、Verifier、Renderer 仍由原转换服务负责。
- 应用使用一种 servlet 并发模型，不同时维护 MVC 与 WebFlux。

HTTP 层跟随 `converter.ai.enabled=true` 条件创建。AI 默认关闭时不会构造 DeepSeek model、转换服务或
Controller，也不会产生网络请求。

## 2. API 与建立 SSE 前的校验

```text
POST /api/converter/mongo-to-wrapper
Content-Type: application/json
Accept: text/event-stream

{"command":"db.user.find({age: {$gte: 18}})"}
```

Controller 只检查：

- `command` 必须存在、非 null、非空白；失败为 HTTP `400`。
- JSON 必须合法；失败为 HTTP `400`。
- command 的 UTF-8 长度不得超过 `converter.http.max-command-bytes`，默认 16 KiB；失败为 HTTP `413`。

Mongo 语法、operator、深度、pipeline 和安全能力仍交给现有 Parser。非空但语法非法的 command 已经建立
SSE，因此使用 `REJECTED` 事件，而不是尝试修改 HTTP status。

建连前错误固定写为 JSON，即使请求的 `Accept` 只有 `text/event-stream`；这避免内容协商把本应是
`400/413` 的输入错误改成 `406`。

## 3. SSE envelope

每个 SSE frame 同时使用 transport metadata 和 JSON envelope：

```text
id: <requestId>:<sequence>
event: <type>
data: <ConversionEvent JSON>
```

`ConversionEvent` 的稳定字段：

| 字段 | 类型 | 规则 |
|---|---|---|
| `requestId` | UUID string | 同一次请求内不变 |
| `sequence` | positive long | 从 1 开始，每次成功构造事件严格递增 |
| `type` | enum string | 见下表 |
| `message` | string | 可直接展示的中文状态，不用于客户端分支判断 |
| `data` | object | 类型相关数据；没有数据时是 `{}` |
| `copyAllowed` | boolean | 由后端按 type 强制派生，只有 `VERIFIED` 为 true |
| `timestamp` | ISO-8601 instant | 服务端产生事件的 UTC 时间 |

事件类型和 `data`：

| type | data | 终态 |
|---|---|---:|
| `STARTED` | `{}` | 否 |
| `PARSED` | `operation` | 否 |
| `GENERATING` | `attempt` | 否 |
| `VERIFYING` | `attempt` | 否 |
| `VERIFIED` | `operation`, `modelAttempts`, `javaCode`, `mongoPlusVersion`, `wrapperType` | 是 |
| `REJECTED` | 可选 `operation`, `errorCode`, `modelAttempts` | 是 |
| `UNSUPPORTED` | 可选 `operation`, `errorCode`, `modelAttempts` | 是 |
| `ERROR` | `errorCode`，以及已知时的 `operation`、`modelAttempts` | 是 |

第一版不发送 `CODE_DELTA`。DeepSeek streaming 内容是未完成的 Call Plan JSON，不是 Java；它只在后端有界
buffer 中累积并完成严格解析。最终 Java 仍只在完整计划通过 Catalog、真实 Wrapper 和 BSON/Pipeline
Verifier 后，由 Renderer 一次写入 `VERIFIED.data.javaCode`。

## 4. 状态机

正常流程：

```text
STARTED -> PARSED -> GENERATING(attempt=1) -> VERIFYING(attempt=1) -> VERIFIED
```

Phase 3 的唯一一次语义修正仍归核心转换服务所有：

```text
... -> VERIFYING(attempt=1) -> GENERATING(attempt=2)
    -> VERIFYING(attempt=2) -> VERIFIED | REJECTED
```

其他终止路径：

```text
STARTED -> REJECTED                         parser invalid
PARSED -> UNSUPPORTED                       local capability guard
GENERATING -> UNSUPPORTED | ERROR           model unsupported/stream failure
VERIFYING -> REJECTED                       local verification failed
任意活动态 -> ERROR                         overall timeout/internal failure
```

每个请求创建一个独立 `ConversionStateMachine`。状态转换、sequence 分配和 sink 写入在请求内串行化，避免
worker 与 timeout scheduler 产生乱序；两个并发请求不共享 requestId、state 或 sequence。终态后、取消后、
以及第二次 `VERIFYING -> GENERATING` 都会被拒绝。

## 5. Event sink 与分层

HTTP 无关接口 `ConversionEventSink` 只暴露：

```text
emit(ConversionEvent) -> boolean
heartbeat() -> boolean
complete()
error(Throwable)
```

`SseConversionEventSink` 是唯一依赖 `SseEmitter` 的适配器。核心链路为：

```text
MongoWrapperConversionController
  -> ConversionRequestCoordinator
  -> conversion ExecutorService
  -> MongoWrapperConversionService
  -> ConversionProgressListener
  -> ConversionStateMachine
  -> ConversionEventSink
  -> SseConversionEventSink
```

原 `MongoWrapperConversionService.convert(String)` 保留；新增 overload 接收
`ConversionProgressListener` 和 `ConversionCancellation`。因此旧 Phase 2/3 调用方不变，Controller 也没有
重复 Parser、AI、Verifier、Renderer 或 retry 编排。

## 6. 异步执行与 request state

- Controller 请求线程只完成校验、SSE session 建立和任务提交，不同步等待 AI 转换。
- 每个转换在 Java 21 virtual-thread executor 中运行；`ScheduledExecutorService` 管理整体超时。
- `ConversionRequestHandle` 保存 worker/timeout Future；取消时对 worker 执行 best-effort interrupt。
- `ConversionRequestCoordinator` 只在请求存活期间保存 `requestId -> handle`，正常终态、断连、timeout、提交失败
  都删除 state。
- UUID v4 由 `UUID.randomUUID()` 生成，作为事件、SSE id、日志、取消和排障关联键。

## 7. 断连与取消边界

取消来源：

- `SseEmitter.onCompletion` 的非服务端完成；
- `onError`；
- `onTimeout`；
- `send(...)` 抛 `IOException`/连接已关闭；
- overall timeout 或内部提交失败。

`ConversionCancellation` 是 request-scoped token。取消后：

- 后续事件立即丢弃；
- worker Future 被 interrupt；
- Translator 等待 future 被结束；
- 迟到 chunk/completion/error 被忽略；
- 不再进入下一次模型修正；
- Verifier 和 Renderer 前的 cancellation safe-point 会阻止无意义后续工作；
- request state 与 timeout task 被清理。

Servlet API 不能在没有读写活动时主动保证发现 TCP 断开。实际断连通常由 lifecycle callback 或下一次
`SseEmitter.send` 失败发现。服务每 10 秒发送一个不含业务数据、不消耗 sequence 的 SSE comment heartbeat，
以缩短 `GENERATING` 静默期的断连发现时间；MockMvc 仍不能模拟真实 TCP reset。因此这里保证的是发现断连
后的应用行为，不宣称能在浏览器关闭的同一瞬间停止所有工作。

### LangChain4j / DeepSeek 实际能取消到哪里

`StreamingChatModel.chat(...)` 返回 `void`，首个 content delta 之前没有公开 cancellation handle。
LangChain4j 1.19.0 在两参数 partial callback 中提供 `PartialResponseContext.streamingHandle()`；adapter 保存该
handle，token 取消后 best-effort 调用 `cancel()`。默认 JDK HTTP client 会停止本地 SSE parsing 并关闭响应
InputStream。

这只能证明客户端停止读取：

- 首个 content delta 前仍只有 cooperative cancellation 和 worker interrupt；
- 自定义 LangChain4j HTTP client 可能不支持底层取消；
- 不能证明 DeepSeek 服务端推理、token 生成或计费已经终止。

## 8. 三类 timeout

默认关系：

```text
AI timeout 45s < overall conversion timeout 60s < SSE timeout 65s
```

| timeout | 所有者 | 行为 |
|---|---|---|
| AI timeout | Phase 3 Translator/model | 终止本次模型等待并返回 `ERROR / MODEL_TIMEOUT` |
| overall timeout | Phase 4 coordinator | 在连接仍可写时发送 `ERROR / CONVERSION_TIMEOUT`，再取消 worker |
| SSE timeout | Servlet/SseEmitter | 连接生命周期兜底，只取消和清理；不保证还能写 ERROR |

HTTP/Phase 4 没有增加第二层 AI retry。模型最多仍由 Phase 3 执行初次 + 一次语义修正。

## 9. 响应与日志安全

- 终态事件只返回 requestId、稳定 errorCode、用户可理解 message 和受控 data。
- 不返回 stacktrace、API key、prompt、模型原始 JSON、反射 Method、内部 HTTP request、本地路径或 Maven 信息。
- INFO/WARN 日志以 requestId 关联，只记录 operation、status、errorCode、durationMs；不默认记录完整 command、
  prompt 或 DeepSeek response。
- 未预期异常只写服务端日志，客户端统一为 `INTERNAL_ERROR`。

## 10. 配置

```yaml
converter:
  http:
    max-command-bytes: 16384
    heartbeat-interval: 10s
    overall-timeout: 60s
    sse-timeout: 65s
  ai:
    timeout: 45s
    max-retries: 1
```

启动时要求 HTTP limit/timeout/heartbeat 为正数，并要求
`heartbeat-interval < overall-timeout < sse-timeout`。

## 11. Phase 5 前端接入契约

1. 使用 `POST` + `Accept: text/event-stream`，不要用原生只支持 GET 的 `EventSource`；前端应使用支持 POST
   streaming 的 fetch reader/polyfill。
2. 按 SSE `event` 或 JSON `type` 分支，按 `requestId` 隔离请求，并忽略小于等于已处理 sequence 的重复/
   乱序 frame；忽略以 `:` 开头的 transport heartbeat comment。
3. 所有 code/message 都以纯文本渲染，禁止把 `javaCode` 当作 HTML。
4. 只有收到 `VERIFIED` 且 `copyAllowed=true` 才启用复制；任何其他事件立即保持复制禁用。
5. `VERIFIED.data.javaCode` 是完整最终结果；不存在可拼接的 Java delta。
6. `REJECTED` 表示输入或生成计划未通过本地安全/语义校验；`UNSUPPORTED` 表示语法可理解但当前 Catalog/
   MongoPlus 版本不能精确表达；`ERROR` 表示模型、timeout 或内部暂时失败。
7. HTTP `400/413` 在 SSE 建立前处理；HTTP `200` 后只看终态 SSE，不等待另一个 HTTP status。
8. 页面离开时 abort 当前 fetch；服务端会在 callback 或后续写失败发现后执行上述 best-effort cancellation。

## 12. 本阶段验证

使用 Java 21 和 Maven 3.8.6 完成离线验证：

- `mvn -f mongo-plus-wrapper-converter/pom.xml -DskipTests compile`：成功，编译 58 个主源码文件。
- `mvn -f mongo-plus-wrapper-converter/pom.xml test`：成功；共 87 项测试，0 failure、0 error、1 skipped。
- Phase 4 新增 30 项测试；Phase 2/3 原有 57 项保持通过（其中真实 DeepSeek integration 仍默认 skipped）。
- 默认测试使用 fake/test double，不访问 DeepSeek，不连接 MongoDB。
- `git diff --check`：通过；另对尚未纳入 Git 跟踪的 converter/docs 源文件执行尾随空白检查。
- MongoPlus 各生产模块源码没有修改。

测试覆盖 Controller 正常与失败事件流、所有终态的复制权限、Parser/分页 guard、一次语义修正、模型流错误、
整体超时、SSE lifecycle/写失败/heartbeat、取消后迟到 callback 抑制、requestId/sequence 和并发隔离，以及
LangChain4j streaming handle 的 best-effort cancellation。整个测试过程不要求 API key、DeepSeek 或 MongoDB。
