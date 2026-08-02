# 事务

> 审计日期：2026-08-02。结论来自当前源码；MongoDB 服务端前提另以官方文档核对。执行器见 [CRUD_EXECUTION.md](../architecture/CRUD_EXECUTION.md)，代理见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)，数据源见 [MULTI_DATASOURCE.md](MULTI_DATASOURCE.md)，动态集合见 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md)。

## 概念和入口

- `ClientSession` 只是 Driver 会话；`MongoTransactionalManager.getTransaction(...)` 只创建/复用 session，不启动事务，也不保存新 session。
- `startTransaction(...)` 才启动 Driver 事务并把 `MongoTransactionStatus` 放入 `MongoTransactionContext`。
- `SessionExecute` 只负责把上下文 session 传给 Driver API，不负责 start/commit/abort。仅有 session 包装不等于已开启事务。
- MongoPlus 自有事务和 Spring 事务入口最终都委托 `MongoTransactionalManager`；它们不是 Spring Data 的事务资源绑定。

| 入口 | 当前实现 |
|---|---|
| `@MongoTransactional` | 仅支持方法。Boot 3/4 的 AOP 委托全局 `HandlerCache.transactionHandler`；默认实现是 `TransactionHandler`。 |
| 编程式 API | `MongoTransactionalManager` 公开 get/start/commit/rollback/close；没有 callback/template 式公开 API，调用方自行保证 try/catch/finally。 |
| Spring `@Transactional` | `mongo-plus.spring.transaction=true` 时，Boot 3/4 可创建名为 `mongoPlusTransactionalManager` 的 `PlatformTransactionManager`。 |
| Solon | `XPluginAuto` 明确把 `@MongoTransactional` 绑定到 Solon `MongoTransactionalAspect`；有自有事务集成，没有 Spring manager。 |
| 分片 | `ShardingTransactionalHandler`、`ShardingTransactionContext` 提供按数据源保存状态的独立容器；默认入口只确定登记入口数据源状态，换源状态收集存在已确认接线缺口，不能视为已闭合的多源事务。 |

## 自有事务完整生命周期

```text
@MongoTransactional / 手工 start
 -> MongoClientFactory.getMongoClient()（当前 DataSourceNameCache）
 -> MongoClient.startSession(ClientSessionOptions)
 -> ClientSession.startTransaction(TransactionOptions)
 -> MongoTransactionContext.setTransactionStatus（ThreadLocal）
 -> ExecutorFactory 读取上下文并选 SessionExecute
 -> 普通代理 -> 高级代理 -> Driver API(session, ...)
 -> 成功 commit；异常按 rollbackFor/noRollbackFor commit 或 abort
 -> finally closeSession
 -> MongoTransactionContext.clear
```

上下文容器是 `ThreadLocal<MongoTransactionStatus>`，不是继承型 ThreadLocal。状态包含 `ConcurrentHashMap<String, ClientSession>` 和非原子 `long referenceCount`；普通事务仅登记创建时当前数据源的 session，`getClientSession()` 每次按当前数据源查 map。

`TransactionHandler` 的顺序是 start → supplier → commit；异常处理后原异常继续抛出；finally 调 close。默认两个异常数组都为空时，任何 `Throwable` 都 abort，不区分运行时/检查型异常。类型匹配允许子类；先查 `rollbackFor` 再查 `noRollbackFor`，重叠时 rollback 优先。

### 嵌套、失败和清理

- 内层入口复用上下文 session；已有 active transaction 时只增加引用。没有传播级别、挂起、savepoint 或 `REQUIRES_NEW`。
- 内层成功仅减引用；归零才 commit，正常嵌套近似加入外层。
- 内层默认回滚会清零引用并 abort；其 finally 随后清掉整个上下文。若外层捕获后继续 CRUD，后续会退回 `DefaultExecute`。
- commit/abort 失败直接传播；没有 commit 失败后 abort、`TransientTransactionError` 或 `UnknownTransactionCommitResult` 重试。外层 finally 仍调用 close。
- `closeSession` 在 `readyClose()` 时会在 finally 清 ThreadLocal，但只在 `hasActiveTransaction()` 为 true 时调用 `ClientSession.close()`。正常 commit/abort 返回后 transaction 已非 active，所以源码可确认该 session **不会被 MongoPlus 显式关闭**；commit/abort 只结束事务，不等价于关闭 `ClientSession`。这是已确认的生命周期缺陷。实际服务端/本地资源增长幅度仍需运行观测，不能仅凭静态源码量化为某种具体泄漏量。
- 引用仍大于零时既不 close 也不 clear，用于保留正常外层事务。

## SessionExecute 与 DefaultExecute

`ExecutorFactory` 对二者使用相同代理链：先包高级代理，再包外层普通代理。差异只在最内层 Driver 调用。

| 操作 | DefaultExecute | SessionExecute |
|---|---|---|
| find | `find(...)` | `find(session, ...)` |
| insert one/many | 无 session | 传 session |
| update one/many | 无 session | 传 session |
| delete one/many | 无 session | 传 session |
| count | `countDocuments(filter, options)` | `countDocuments(session, filter, options)` |
| estimated count | `estimatedDocumentCount()` | **`countDocuments(session)`**，并非 estimated API |
| aggregate | 无 session | 传 session |
| bulkWrite | 无 session | 传 session |
| index create/list/drop | 无 session | 所有重载均传 session |

除 estimated count 外，返回类型和多 update 的结果汇总代码相同。estimated 的名称、性能和事务可见性不同，运行结果待测。索引虽由 `SessionExecute` 传 session，但当前索引方法无 `ExecuteMethodEnum` 参数策略，普通 before 不运行；高级链仍运行。

## Spring 事务关系

Boot 3/4 实现相同。自动配置不注入也不创建 `MongoDatabaseFactory`；`MongoPlusTransactionalManager` 直接继承 `AbstractPlatformTransactionManager` 并委托 MongoPlus ThreadLocal。因此它不是 Spring Data `MongoTransactionManager`，但与 `@MongoTransactional` 共享 MongoPlus 上下文。

Bean 使用 `@ConditionalOnMissingBean(TransactionManager.class)`：存在任意用户 `TransactionManager` 时默认 Bean 不创建，用户可覆盖。框架不为每个数据源创建 manager；事务开始时绑定当时 `DataSourceNameCache` 对应 client。多 manager 的选择遵循 Spring 的 Bean 名/限定符规则。

当前 `doGetTransaction()` 得到未绑定上下文的 session A，并把 A 作为 Spring transaction object；`doBegin()` 只把 A 用于日志，随后调用无参 session 的 `startTransaction(options)`，再次经 `getTransaction()` 创建 session B、启动事务并放入 MongoPlus ThreadLocal。因而一次正常 Spring begin 后确实同时存在两个打开的 `ClientSession` 对象，但只有 B 是 active transaction；不能称为“两套活动事务”。`doCommit`/`doRollback` 从 Spring status 取 A 也只用于 Boot 3 的日志（Boot 4 rollback 连 A 都不取），真正 commit/abort 的是 ThreadLocal 中的 B；cleanup 同样只查 B。A 从未进入 MongoPlus status，也没有任何 close 路径；B 正常 commit/abort 后又因上述 `hasActiveTransaction()` 条件不被 close。Boot 3/4 的生命周期逻辑一致，差异只在 rollback 日志局部变量。

## 多数据源和分片

普通状态以“事务开始时的数据源名”为 map key 保存一个 session；`getClientSession()` 每次按**当前**数据源名查找。由此必须区分两条分支：

1. 在 Mapper 取得 collection 和 `ExecutorFactory.getExecute()` 之前已切换 `@MongoDs`：状态 map 对新名称返回 null，工厂明确回退 `DefaultExecute`，该操作在事务外执行；不会把旧 session 直接配给新 collection。
2. 工厂已按旧数据源选出 `SessionExecute`，随后普通/高级插件又把 Execute 参数中的 collection 换到另一 client（动态集合在当前数据源中再次取 collection，分片可显式换源）：已捕获的旧 session 仍会传入新 collection。源码无 MongoClient 一致性校验，这是 session/client 混用路径；具体 Driver 异常需运行测试确认。

数据源上下文 clear 后当前名称回到默认值；仅当 status map 恰有该名称时仍能取到 session，否则回退默认执行器。切回还受非栈式上下文影响。动态覆盖/关闭数据源不会重绑 session 或清 collection cache。详见 [MULTI_DATASOURCE.md](MULTI_DATASOURCE.md)。

分片机制另用 `ThreadLocal<Map<ds,status>>` 对**已登记状态**逐项提交/回滚/关闭。默认入口登记入口数据源；路由到配置 replicaSet 的新源时虽会派生并启动 session，但当前源码没有把新状态加入 sharding map，且 Execute 已在路由前持有入口 session；无 replicaSet 时共享 boolean 可令高级链改用 `DefaultExecute`。因此它不是普通 `@MongoDs` 的自动多源事务保证，也不能稳定承诺默认换源 CRUD 已纳入新 session。即使 map 中存在多个状态，逐项 commit 也没有两阶段提交或失败补偿。完整边界见 [SHARDING.md](SHARDING.md)。

## MongoDB 前提与 MongoPlus 能力

MongoDB 通用要求：standalone 不支持多文档事务，需副本集或分片集群；session 只能用于创建它的 client；纳入事务的每个操作必须显式传 session。参考 [Java Sync Driver transactions](https://www.mongodb.com/docs/drivers/java/sync/current/crud/transactions/) 和 [production considerations](https://www.mongodb.com/docs/manual/core/transactions-production-consideration/)。

MongoPlus 已把注解的 causal consistency、snapshot、ReadConcern、WriteConcern、ReadPreference、max commit time 写入 session 默认 `TransactionOptions`，编程入口也能传 options。未实现部署能力预检查、整体事务超时抽象、transient/unknown commit 重试或 Driver `withTransaction` callback；连接/Socket 超时由 MongoClient 配置负责。

## 最低测试清单

- 正常提交；运行时/检查型异常；rollback/noRollback 重叠；commit/abort/start 失败。
- commit/abort 后 session close 与 context clear；正常嵌套、内层回滚外层捕获；并发和线程池。
- 事务内切换/覆盖数据源、动态集合；普通/高级拦截器和索引。
- count/estimated 与未提交写可见性。
- Boot 3/4：MongoPlus 注解、指定 manager 的 Spring `@Transactional`、二者叠加、用户覆盖和多 manager。
- Solon；分片单/多源、部分 commit 失败、无 replicaSet 分支。

## 关键源码

- core: `manager/MongoTransactionalManager.java`、`context/MongoTransactionContext.java`、`MongoTransactionStatus.java`、`handlers/TransactionHandler.java`、`execute/ExecutorFactory.java`、`execute/instance/{DefaultExecute,SessionExecute}.java`
- annotation: `annotation/transactional/MongoTransactional.java`
- Boot 3/4: `transactional/MongoTransactionalAspect.java`、`MongoPlusTransactionalManager.java`、`MongoTransactionManagerAutoConfiguration.java`
- Solon: `transactional/MongoTransactionalAspect.java`、`config/XPluginAuto.java`
- sharding: `sharding/ShardingTransactionalHandler.java`、`context/ShardingTransactionContext.java`、`interceptor/DataSourceShardingInterceptor.java`
