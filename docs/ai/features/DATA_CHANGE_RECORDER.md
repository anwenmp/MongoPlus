# 数据变更记录拦截器

> 审计日期：2026-08-02。`DataChangeRecorderInnerInterceptor` 记录 MongoPlus 普通执行链的请求参数；它不是 Change Stream、Driver `CommandListener`，也没有真实 before/after 文档快照。

## 入口、注册与结构

- `DataChangeRecorderInnerInterceptor implements Interceptor`；未覆盖 `order()`，默认 `Integer.MAX_VALUE`。
- `Configuration.dataChangeRecorder(instance)` 加入普通 `InterceptorChain`。
- Boot 3/Boot 4 自动发现 `Interceptor` Bean 后加入并排序；Solon 先排序本批 Bean 再追加。
- `OperationResult` 仅含 `id`、`operation`、`recordStatus`、`datasourceName`、`databaseName`、`collectionName`、`changedData`、`cost`。

没有专用属性类、Handler/Listener 回调、Recorder 专属操作枚举、忽略注解、success/failure/modifiedCount/exception/user/timestamp/session/request-context 字段或 afterCommit。它复用 `ExecuteMethodEnum`，但把 one/many 压成 `SAVE`、`UPDATE`、`REMOVE` 字符串。默认不启用，必须注册实例；多个实例全部执行且不去重。

实例 setter 可配置 `exceptionMessage`、`ignoredColumnList`、`batchUpdateLimit`（1000）、`displayCompleteData`、datasource/database/collection。源码把 `displayCompleteData` 初始化为 `true`，与注释“默认不开启”冲突。调用 `enableSaveDatabase(BaseMapper)` 才持久化；默认只同步写 info 日志。

## 支持操作

| Execute 类型 | 程度 | `changedData` |
|---|---|---|
| `SAVE_ONE` | 仅请求 | 单 Document 字符串；operation=`SAVE` |
| `SAVE` | 仅请求 | 全列表或数量；按列表条数限流 |
| `UPDATE_ONE` | 仅请求 | filter/update BSON；operation=`UPDATE` |
| `UPDATE` | 仅请求 | filter/update pair 列表或数量；按 pair 数限流 |
| `REMOVE_ONE`/`REMOVE` | 仅请求 | filter 或顶层字段数；只对 `$in` 数组做元素限流 |
| `BULK_WRITE` | 部分请求 | 整个 model list 形成一条记录；只展开 `InsertOneModel`、`UpdateManyModel`，其他 model 变成空字符串 |

query、aggregate、count、estimated count、索引不记录。当前 `BaseMapper` 没有独立 replace 入口；upsert 只是 `UpdateOptions`，因此仍记为 `UPDATE`/`UPDATE_ONE`，且记录文本不含 options。物理删除为 `REMOVE`；逻辑删除见顺序章节。bulkWrite 接受 Driver 的任意 `WriteModel<Document>`，但 Recorder 只识别 insert-one 与 update-many；不记录 `BulkWriteOptions` 的 ordered/unordered、部分成功或逐条结果，阈值也只分别统计 insert-one 与 update-many。

`UpdateManyModel` 的文本标签虽写作 `options=`，源码实际再次拼接 `getUpdatePipeline()`，没有输出真正的 model options；这是已确认的记录内容缺陷。mixed bulk 仍只有一个 `operation=BULK_WRITE`，未识别 model 以空项占位，无法从记录可靠还原批次语义。

## 实际“快照”链路

```text
实体映射、Auto Fill、字段 Handler
→ Mapper 构造 BSON 与 collection
→ 普通 Interceptor：beforeExecute → 专用参数方法
→ Recorder 把当时 args 转成 String
→ 高级拦截链 → DefaultExecute/SessionExecute → Driver
→ 仅正常返回：普通 afterExecute
→ Recorder 可选 BaseMapper.save(OperationResult)
```

Recorder 不查变更前/后数据，没有 before/after 快照、同 session 查询或 modifiedCount 判断。它记录执行前请求，而非结果。`recordStatus` 在 before 中无条件写成 `true`，不能解释为 Driver 成功、数据已修改或事务已提交。insert 是否含生成 `_id` 取决于进入代理前 Document 是否已有 ID。无匹配或 `modifiedCount=0` 只要正常返回，仍记录/保存。

`changedData` 是在 before 中立即由当时参数构造的字符串，不是实体、Document/BSON 快照；因此后续拦截器原地修改参数不会回写这段字符串。无字段过滤、自定义序列化或字节上限。`displayCompleteData=false` 只是改为数量/简单计数，并非脱敏。

## 与其他扩展的准确顺序

确定的普通 order：Tenant `0` → Dynamic Collection `2` → Recorder `Integer.MAX_VALUE`。Collection Logic、Logic Auto Fill、Sensitive Word GLOBAL、DataSource Sharding 与 Recorder 都是最大 order；同 order 相对顺序仅由注册顺序维持，不是稳定契约。

- Auto Fill、实体映射和字段级加密/敏感词处理在代理前，Recorder 看见处理后的 BSON。GLOBAL 敏感词与逻辑条件同 order，结果取决于注册顺序。
- Tenant 确定先修改参数。
- Dynamic 确定先替换 args 中的 collection；但 `ExecutorProxy` 传给 Recorder 的 `collection` 是入口捕获的原对象。因此 Driver 使用新 collection，`OperationResult` 却记录原 namespace，这是已确认缺陷。
- 逻辑 remove→update 由高级 `LogicRemoveInterceptor`（`Integer.MAX_VALUE - 1`）在普通 before 之后执行；Recorder 已记录为 `REMOVE`，看不到转换后的 update。
- Optimistic Locker/LogicRemove 属于高级链。Recorder before 在其前、after 在正常返回后；冲突若正常返回零修改仍记录，若抛异常则无 after。
- SessionExecute 位于普通 before/after 之间；持久化发生在业务方法返回及事务提交前，并非 afterCommit。
- Sharding 同时参与普通/高级链；普通分片与 Recorder 同 order，无法保证记录真实目标 datasource/namespace。
- `DataSourceShardingInterceptor.sessionIsNotNull` 只在高级阶段改为 `DefaultExecute`；Recorder 的 before 记录已在此前生成，after 仍会运行（前提是调用正常返回），所以不会修正已保存的入口 datasource/原 namespace。事务逃逸及最终写入结果仍需运行验证。

## 持久化、递归与失败

启用保存后，before 把 `OperationResult` 放入静态 `ThreadLocal`；正常返回后，after 决定 datasource/database 并同步调用用户 `BaseMapper.save`。无异步队列、重试、缓冲或自动审计仓库。

防递归仅按 collection name：默认忽略 `DATA_CHANGE_RECORD`，`setCollectionName` 会把新保存目标追加进忽略列表，所以正常 setter 配置的保存写入会跳过 Recorder。用户之后用 `setIgnoredColumnList` 覆盖掉目标名称时，保存写入会再次进入 Recorder；若设为空，before 直接抛错。同名但不同库/源也一并忽略。

- 阈值/BSON 转换异常在 Driver 前阻止业务操作。
- Driver/高级链失败时普通 after 不执行，也无失败记录。
- 保存失败发生在业务 Driver 已成功后，异常传播且无补偿。
- `ThreadLocal.remove()` 只在保存成功后执行，且不在 `finally`；Driver/高级链失败、after 中选源/选库失败或保存失败都会残留，这是已确认清理缺口。
- after 调用 `DataSourceNameCache.setDataSource` 后不恢复原 datasource。事务内还可能造成 session 与新 client/collection 不一致；提交/回滚结果需实测。
- 单槽 `ThreadLocal` 不是栈：同线程在外层 before 与 after 之间发生另一条被记录 CRUD，会覆盖外层值；内层成功保存并 remove 后，外层甚至可能保存 `null`。它只提供线程间槽位隔离，不传播到新线程；插件链、忽略列表和实例配置仍是共享可变状态。

## 安全、批量与资源

Tenant 条件确定已加入；Auto Fill 与字段映射也已发生。加密通常为映射后的值，但具体模式见对应专题；GLOBAL 敏感词与逻辑条件因同 order 不保证。记录仍可能包含密码、token、密钥或大二进制。

完整模式一次性字符串化列表/部分 bulk model，只有条数阈值，无字节上限/分页。remove 只限制 `$in`；普通大 filter 不限制。bulk 的 insert/update 分别比较阈值，不按总 model 数；大量其他 model 可绕过限制。

## 与 Command Listener 的区别

| 维度 | Recorder | Command Listener |
|---|---|---|
| 层级 | MongoPlus 普通 Interceptor | Driver CommandListener |
| 内容 | 请求参数字符串 | 实际命令、响应、异常 |
| 语义 | SAVE/UPDATE/REMOVE/BULK_WRITE | commandName，不能识别逻辑删除 |
| Driver 行为 | 不见 retry/getMore/commit/abort | 可见独立命令 |
| 结果 | 无 modifiedCount/失败 | response/throwable 可见 |

保存审计记录会再次产生命令事件。两者事件数不能一一配对。

## 测试与证据

当前仓库未发现 Recorder 回归测试。至少覆盖 save/update/remove、逻辑删除、乐观锁、replace/upsert 上层入口、各种 bulk model/部分成功、阈值、大文档、动态集合、多源、分片、事务提交回滚、保存/Driver 异常、递归嵌套、ThreadLocal、并发、加密/敏感词及三种集成。

关键源码：`DataChangeRecorderInnerInterceptor.java`、`OperationResult.java`、`Interceptor.java`、`InterceptorChain.java`、`ExecutorProxy.java`、`Configuration.java` 及三个集成模块的 `MongoPlusAutoConfiguration.java`。

相关文档：[CRUD](../architecture/CRUD_EXECUTION.md)、[扩展顺序](../architecture/EXTENSION_PIPELINE.md)、[事务](TRANSACTION.md)、[多数据源](MULTI_DATASOURCE.md)、[动态集合](DYNAMIC_COLLECTION.md)、[Tenant](TENANT.md)、[逻辑删除](LOGIC_DELETE.md)、[分片](SHARDING.md)、[测试](../TESTING.md)、[兼容性](../COMPATIBILITY.md)、[待验证问题](../OPEN_QUESTIONS.md)、[命令监听](COMMAND_LISTENER.md)。
