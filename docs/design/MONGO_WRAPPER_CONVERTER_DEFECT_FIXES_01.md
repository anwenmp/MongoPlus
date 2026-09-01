# MongoPlus Wrapper Converter 缺陷修复记录 01

## P1

### Root cause

`WrapperMethodRegistry` 只校验方法参数的主要字段，没有按 `InvocationKind` 排除 `field`、
`nestedCalls` 等冗余结构。真实 Wrapper 执行时会忽略这些字段，但 Renderer 可能据此选择另一渲染分支，
导致 VERIFIED 的执行语义与生成 Java 不一致。

### Reproduction

为 `query.eq` 同时提供合法 `field/arguments` 和额外 `nestedCalls`。修复前 Registry 按
`FIELD_VALUE` 执行并得到 VERIFIED，Renderer 却输出 nested lambda 形式的 `eq` 调用。

### Minimal fix

Registry 在真实 Wrapper 执行前，以 Catalog 缓存的 `InvocationKind` 为判别字段校验 canonical shape。
每种调用类型只允许自身需要的 `field`、`arguments`、`nestedCalls` 和 `accumulators` 组合；Renderer 未修改。

### Regression tests

- 完整 Parser → Verifier → Registry → Renderer 路径拒绝带冗余 `nestedCalls` 的 `FIELD_VALUE`。
- 覆盖当前 7 种 `InvocationKind`，确认非 canonical 字段组合均在执行前拒绝。

## P2 Parser

### Root cause

Parser 的 `readValue`、document、array 和构造器参数存在递归路径，但此前没有主动深度或节点总量限制。
HTTP 字节限制不能保护同步调用等非 HTTP 入口。

### Limit strategy

- `maxDepth = 64`：每次递归进入 BSON value 增加一层，退出时恢复；document、array、scalar 和构造器参数
  共用同一规则。
- `maxNodes = 4096`：每个 BSON value 计一个节点，每个 document field/value 容器额外计一个节点；
  array 元素通过统一 `readValue` 计数。
- 每次 `parse` 创建独立 `ParseContext`，超限分别返回 `INPUT_TOO_DEEP` 和 `INPUT_TOO_COMPLEX`。

### Regression tests

覆盖 document、array、document/array 混合嵌套的 depth 64/65 边界，以及 document 和 array 的
节点上限边界。所有入口均通过 Parser 自身限制，不依赖 HTTP 层。

## P2 Buffer

### Root cause

streaming chunk 原先先写入 `StringBuilder`，再检查是否超过 262144 chars；单个超大 chunk 或累计溢出
都会短暂突破声明上限。

### Minimal fix

保持 Phase 3 的 Java UTF-16 `char` 数定义不变。在 `append` 前使用
`incomingLength > maxChars - currentLength` 判断剩余容量，避免加法溢出；超限复用现有终态、取消、
cleanup 和 `MODEL_STREAM_ERROR`。

### Regression tests

覆盖单个 `MAX+1` chunk、多个 chunk 累计溢出、恰好等于上限、超限后的迟到 chunk，以及既有
cancellation/error 行为。
