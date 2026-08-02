# 分片

> 审计日期：2026-08-02。本文的“分片”指 MongoPlus 客户端数据源选择插件，不是 MongoDB Server 原生 sharding，也不是 core 的普通多数据源。普通执行、聚合与事务分别见 [CRUD_EXECUTION.md](../architecture/CRUD_EXECUTION.md)、[AGGREGATION.md](../architecture/AGGREGATION.md)、[TRANSACTION.md](TRANSACTION.md)。

## 模块与集成边界

根 reactor 包含 `mongo-plus-sharding` 与 `mongo-plus-sharding-boot-starter`，两者均为 jar、Java 8 编译。前者只依赖 `mongo-plus-core`；starter 依赖 Boot 3 `mongo-plus-boot-starter` 和 sharding 模块。当前没有 Boot 4 sharding starter，也没有 Solon sharding 集成。

该模块没有 MongoDB Server shard key、mongos、chunk、collection shard 命令或拓扑管理抽象。它读取 core 已注册的数据源，针对一次 Execute 调用选择**一个** datasource，并用相同 database/collection 名替换 `MongoCollection`。

## 公开入口与启动

真实公开类型包括：

- `DataSourceShardingStrategy`：`sharding(currentDataSource, ExecuteMethodEnum, Object[] source)` 返回一个数据源名称/匹配表达式。
- `AbstractDataSourceShardingHandler`、默认 `DataSourceShardingHandler`：维护“操作 -> 候选数据源表达式”、匹配通配符/正则并负载均衡。
- `DataSourceShardingInterceptor`：同时实现普通 `Interceptor` 与 `AdvancedInterceptor`。
- `ShardingTransactionalHandler`、`ShardingTransactionContext`：替换全局事务 Handler 并按数据源保存状态。

没有分片注解、shard-key 注解、集合/数据库分片策略、collection/table sharding interceptor、配置属性类或节点注册表。starter 的 `MongoShardingConfiguration.afterPropertiesSet` 直接把静态 `HandlerCache.transactionHandler` 改成 `ShardingTransactionalHandler`，并提供一个可覆盖的 `DataSourceShardingInterceptor` Bean。节点仍由 core `MongoClientFactory`/`DataSourceManager` 管理，client 关闭、动态增删和重复名称语义也归普通多数据源。

## 路由模型

默认 Handler 把 `ExecuteMethodEnum` 映射到候选数据源表达式；自定义 `DataSourceShardingStrategy` 则每次返回一个表达式。表达式可精确、`*` 通配或合法正则匹配当前 `DataSourceNameCache.getBasePropertyMap()` 的名称。候选结果先去重，再由默认加权随机 `loadBalance` 选一个数据源；权重使用 `1 / BaseProperty.position`。

因此当前实现始终单目标：没有 shard key 提取、Wrapper 条件分析、range/in/or 多路由、广播查询、跨目标 batch、并行执行、scatter-gather、结果去重/排序/分页或 count/aggregate 汇总。自定义策略返回 null 会把 null 传给 `ConcurrentHashMap.computeIfAbsent`，当前实现会抛空指针异常；策略返回无匹配表达式时只记录 error 并继续使用当前数据源；策略自身异常向上传播。候选在负载均衡前 `distinct` 去重。自定义 `loadBalance` 若返回 null/空名称，代码虽记录 error，仍会进入换源分支并在读取目标属性或 collection 时失败，框架没有回退保护。

默认映射覆盖 `ExecuteMethodEnum` 中的 SAVE/SAVE_ONE、REMOVE/REMOVE_ONE、UPDATE/UPDATE_ONE、QUERY、AGGREGATE、COUNT、ESTIMATED_DOCUMENT_COUNT、BULK_WRITE。索引方法不在枚举中，普通 before 不运行，因而不会分片。

## 单次 CRUD 与聚合链

```text
Mapper/Service/Repository/Chain
 -> 实体映射、Wrapper 构建与 Auto Fill
 -> ExecutorFactory 选择 DefaultExecute/SessionExecute
 -> 普通 ExecutorProxy
 -> Tenant(order 0) 等普通插件
 -> Dynamic Collection(order 2)
 -> DataSourceShardingInterceptor(order 默认最大值)
 -> 用选中数据源的同 database/collection 替换 args 最后一项
 -> 高级 AdvancedProxy 链（同一拦截器再次参与）
 -> Driver
```

分片拦截器注释要求最后执行，实际返回默认最大 order；这保证正常排序时 Tenant、Dynamic、普通 Logic 在其前执行，但 `DataSourceShardingStrategy` 看到的是完整 `source` 数组，是否理解其中的实体/Document/Wrapper 由用户实现。Auto Fill 和实体转 Document 在 Execute 代理前完成，所以可见于 source；Tenant/Logic 是否已经原地写入同一参数取决于各操作策略。Optimistic Lock 和逻辑删除高级转换在高级链中，可能发生于 datasource collection 已替换之后。

路由只替换 collection，不设置 `DataSourceNameCache`。显式 `@MongoDs` 先决定当前/回退数据源，但分片普通拦截器仍可为每次 Execute 选择不同目标，所以命中不同候选时分片结果覆盖该次 Driver collection；显式 database/collection API只要进入有 `ExecuteMethodEnum` 的 Execute 也会被替换，索引绕过。路由后不会重新调用 `ExecutorFactory`，只替换现有调用参数。聚合与普通 query 一样只路由到一个数据源，不合并结果。batch insert/bulkWrite 也只整体选择一个目标，不按条目拆分。

`DataSourceShardingInterceptor` 同时实现普通和高级接口。普通链负责选择并替换 `MongoCollection`；高级链通常只继续调用，但共享实例字段 `sessionIsNotNull` 为 true 时会直接对同一参数调用新的 `DefaultExecute`，绕过原先已选定的 `SessionExecute`。因此该字段会改变是否携带 session，不只是日志状态。

## 动态集合与其他功能

- Dynamic Collection 先替换名称，分片随后保留该 database/collection 名并换 datasource；不存在独立 collection 分片，所以不会二次改名。
- Tenant、普通 Logic 的正常 order 早于分片；策略看到的 source 是否已增强需按具体操作验证。高级 LogicRemove/Optimistic Lock 位于换源后的高级链。
- TypeHandler、加密和映射在分片前处理；返回值仍按原 Mapper 目标类型转换。
- 自动索引、时序创建和手工索引不进入分片路由，因此不会自动为所有候选源/collection 创建结构。
- Map/Document 模式可路由，因为选择依据是 ExecuteMethodEnum/source 而非实体注解。

## 分片事务的真实语义

starter 全局替换 `@MongoTransactional` Handler。入口创建**入口时当前数据源**的 session/status、启动事务，并把这一项放入 `ShardingTransactionContext` 的 `ThreadLocal<HashMap<ds,status>>`。路由换源时：若目标 `BaseProperty.replicaSet` 非空且普通事务上下文已有 session，拦截器从目标 client 新建 session 并调用 `MongoTransactionalManager.startTransaction`；若 replicaSet 为空但已有 session，则设置 `sessionIsNotNull`，随后高级拦截器改用 `DefaultExecute`，让该次操作不带 session。

这里存在已确认的状态接线缺口：换源分支没有调用 `ShardingTransactionContext.addResourcesTransactionStatus(dsName, status)`；并且 `ExecutorFactory` 在普通分片 `beforeExecute` 之前已经创建了持有原 session 的 `SessionExecute`。新 session 被写入普通 `MongoTransactionContext`，但当前调用的原执行器不会因此重建，`afterExecute` 又按未被路由修改的 `DataSourceNameCache` 取回入口数据源状态。因此源码不能证明换源 CRUD 使用了新 session，也不能证明新状态进入最终 commit/rollback/close 集合。这是已确认实现缺口；具体 Driver 命令携带哪个 session、泄漏规模和业务结果仍需集成测试。

成功后对**实际已登记**的 `HashMap.values()` 逐项 commit；异常按 `rollbackFor/noRollbackFor` 对已登记项逐项 rollback 或 commit；finally 逐项 `closeSession` 并 clear。关键语义：

- `HashMap` 不保证提交、回滚或关闭顺序。
- 第一个 commit/rollback/close 抛错会中断 `forEach`，后续状态可能不再处理；没有异常聚合。
- 没有 prepare、两阶段提交、补偿、幂等记录或重试；即使未来或自定义路径登记了多个本地状态，逐项提交也不是原子分布式事务，可能部分提交成功。当前默认换源路径又没有把新状态登记进 map，不能稳定承诺“多个本地事务均被协调”。
- 嵌套调用会从普通 `MongoTransactionContext` 取得已有 session/status 并增加引用计数，但 sharding context 本身没有栈或独立引用计数；内层 finally 会执行 `closeAllSession()` 并清空整张 sharding map，因此嵌套边界需运行验证。与普通 Handler 不是两个可独立选择的事务管理器，而是 starter 把静态全局 Handler 替换为分片实现。
- core `closeSession` 在事务已 commit/abort 后通常不显式关闭 session，已确认缺陷仍适用于分片状态。

准确表述只能是：当前组件提供“对 `ShardingTransactionContext` 中已登记的多个本地 MongoDB 事务状态做非原子的逐项协调”的机制；默认入口确定登记一个状态，而默认换源路径存在上述收集/执行接线缺口。不得把它简称为已闭合的分布式事务或已确认可用的多数据源事务。

## `sessionIsNotNull` 并发影响范围

该字段定义在 `DataSourceShardingInterceptor` 实例上；Boot starter 通过默认单例 `@Bean` 注册拦截器。它在普通 `beforeExecute` 的“已存在 session、目标源 `replicaSet` 为空”分支写 true，在高级 `intercept` 读取后先写 false，再以 `DefaultExecute` 执行。没有 ThreadLocal、同步、调用标识或 `finally` 恢复。

所以两个并发线程可以发生“线程 A 写 true，线程 B 先消费并清 false”的交叉。静态源码确认的影响范围是**执行器/session 选择**：错误消费的调用可能无 session 执行，而原调用可能继续使用原 `SessionExecute`；collection 已在各自普通链参数中替换，字段本身不决定 collection 名称或 datasource。是否出现、具体命令序列及事务可见性必须用带阻塞点的并发测试确认，当前记录为高风险并发设计，不写成必现错路由缺陷。

## Boot 与兼容性

只有 Boot 3 starter；Boot 4 和 Solon 用户没有自动注册入口。sharding core 依赖公开 Driver `MongoClient`/`ClientSession`/`MongoCollection` API，定向源码未发现 Driver internal API。Java/Driver 的最终版本由根 dependency management 与 core 决定，详见 [COMPATIBILITY.md](../COMPATIBILITY.md)。Server 是否为 replica set/sharded cluster、事务能力及跨拓扑限制都没有启动预检。

## 最低测试清单

- 精确/通配/正则候选、自定义策略 null/空/异常、重复候选、position 为 0/负数。
- 所有 ExecuteMethodEnum；batch/bulk 整体单目标；索引不路由。
- Tenant、Logic、Auto Fill、Dynamic Collection、Optimistic Lock、aggregate、Map/Document。
- 单/多数据源事务、无 replicaSet 分支、commit/rollback/close 任一步失败、部分提交、session/client 一致性。
- 并发验证共享 `sessionIsNotNull`；动态增加/覆盖/关闭数据源及候选缓存刷新。
- Boot 3 自动配置；Boot 4/Solon 无集成边界；不同 MongoDB Server 拓扑。

## 关键源码

- sharding: `sharding/{AbstractDataSourceShardingHandler,DataSourceShardingHandler,DataSourceShardingStrategy,ShardingTransactionalHandler}.java`
- sharding: `interceptor/DataSourceShardingInterceptor.java`、`context/ShardingTransactionContext.java`
- starter: `config/MongoShardingConfiguration.java`、自动配置 imports 资源、两模块 POM
- core dependencies: `cache/global/DataSourceNameCache.java`、`manager/{MongoPlusClient,MongoTransactionalManager}.java`、`proxy/{ExecutorProxy,AdvancedProxy}.java`
