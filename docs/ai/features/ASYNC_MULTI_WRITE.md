# 异步多数据源写入

> 审计日期：2026-08-02。结论来自当前源码，未运行 MongoDB/容器集成测试。本功能是客户端进程内的尽力而为镜像写，不是同步多写、MongoDB 副本复制、分片复制、本地文件备份或跨数据源原子事务。本地文件能力见 [BACKUP_AND_RESTORE.md](BACKUP_AND_RESTORE.md)。

## 入口、注册和模块边界

实现全部位于 `mongo-plus-core`：`AsyncMultipleWriteInterceptor`、`MultipleWriteHandler` 和 `MultipleWrite`。`MultipleWriteStrategy` 是包级接口，用户扩展点实际是继承公开抽象类 `MultipleWriteHandler` 并覆盖 `multipleWrite(currentDataSource, namespace)`。

它是 `AdvancedInterceptor`，默认 `order()` 为 `Integer.MAX_VALUE`，没有注解、配置属性、自动开关或专用 ignore。默认不启用，必须任选一种方式注册：

- 调用 `Configuration.asyncMultipleWrite(interceptor)`；
- 在 Boot 3、Boot 4 或 Solon 容器声明 `AdvancedInterceptor` Bean，由各自 `MongoPlusAutoConfiguration` 收集。

三个集成都没有替用户创建 `AsyncMultipleWriteInterceptor`、线程池或 handler。高级链是 JVM 静态列表，重复初始化/多个应用上下文会追加实例；相同 order 的多个实例依赖稳定排序和注册枚举次序，每个实例都会各自提交镜像任务，因此可能重复写。没有按容器清空或去重。

`AdvancedInterceptorChain` 先按 order 降序保存，再按该顺序逐层包装；后包装者在外层。因此较小 order 通常先执行。相同默认 order 时，后注册者在外层、先进入。普通拦截器代理包在高级代理之外，普通 before 已完成后才进入异步多写；具体组合顺序见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)。

## 支持范围

| 操作 | 结论 | 实际镜像调用 |
|---|---|---|
| save/saveBatch | 支持；对应 `SAVE` | `insertMany`，同一 `List<Document>` 与 options |
| saveOne | 支持；对应 `SAVE_ONE`，但策略枚举仍用 `SAVE` | `insertOne`，同一 Document 与 options |
| update | 支持 | 对同一 pair 列表逐项 `updateMany` |
| updateOne/updateById | 支持，最终命中 `UPDATE_ONE` 时 | `updateOne` |
| replace/upsert | 没有独立 replace Execute 分支；`ReplaceOneModel` 仅能随 bulk 原样镜像，update upsert 随 `UpdateOptions` 镜像 | options 对象按引用完整传给目标 Driver；非 null 时 ordered/upsert 等设置不被重建 |
| remove/removeOne | 支持；无逻辑删除转换时对应 `REMOVE` | 目标任务再次调用 `LogicRemove.logic(invocation, collection)`，最终可能 delete 或 update |
| 逻辑删除 | 支持，但常规链上不是在 `REMOVE` 阶段复制 | `LogicRemoveInterceptor`（`Integer.MAX_VALUE - 1`）先把 remove 转为对内层执行器的 `executeUpdate`，Async（`Integer.MAX_VALUE`）随后按 `UPDATE` 镜像 |
| 物理删除 | 支持；逻辑删除关闭、忽略或目标无逻辑元数据时最终可走 delete | Async 的 `REMOVE` 任务在目标 collection 上重新判断逻辑删除；源和目标元数据不同会改变结果 |
| bulkWrite | 支持整个原始 `List<WriteModel<Document>>` | 原生 `collection.bulkWrite(list, options)`，所以 Driver 接受的 model 类型均可传递 |
| aggregate/query/count | 明确不支持 | 无分支 |
| index、时序 collection 初始化 | 明确不支持 | 无分支 |

该拦截器只识别 `ExecuteMethodEnum` 的 SAVE、SAVE_ONE、REMOVE、REMOVE_ONE、UPDATE、UPDATE_ONE、BULK_WRITE。Mapper 高层方法名本身不是证明，应以最终 Execute 方法为准。索引和时序关系见 [INDEX_AND_TIMESERIES.md](INDEX_AND_TIMESERIES.md)。

## 主写与镜像写的真实链路

```text
Mapper 转换/Auto Fill
 -> 取得当前 datasource/database/collection
 -> 普通拦截器 before（Tenant、Dynamic Collection、Sensitive Word 等）
 -> 高级链：较小 order 的外层先进入
 -> 逻辑删除命中时 LogicRemoveInterceptor(MAX_VALUE-1) 先把 REMOVE 转成 UPDATE
 -> AsyncMultipleWriteInterceptor(MAX_VALUE) 按实际进入它的枚举提交任务
 -> 按本次操作取目标名称列表
 -> 每个目标调用 executor.submit(task)
 -> 当前线程继续 invocation.proceed()
 -> 后续高级拦截器 -> 主 Driver 写入

task
 -> 运行时按目标名称从 MongoPlusClient 取相同 database/collection
 -> DefaultExecute 或 LogicRemove 直接写目标 Driver
```

因此任务在主写**之前提交**。镜像可能在主写成功前完成；主写随后失败、回滚或被后续高级拦截器终止时，已提交任务仍可执行。调用者不等待镜像结果，主返回只代表主链结果，甚至线程池使用 `CallerRunsPolicy` 时任务可能同步占用调用线程。镜像失败不能回滚主写，主失败也不会取消镜像。

普通拦截器已经原地形成/增强的最终 Document、filter、update BSON、options、pair list 和 WriteModel list 被闭包直接引用；没有深拷贝、不可变快照或任务大小限制。主线程、后续高级拦截器或其他代码继续原地修改这些对象时，会与任务并发并影响镜像内容。insert `_id` 是否在提交前已生成取决于映射/Driver 时机；源码没有显式统一 `_id` 的步骤，必须运行验证。

Auto Fill、Tenant、Encryption、Sensitive Word 的既有参数结果通常被复用，镜像保存/更新不重新经过 Mapper、普通拦截器或完整高级链。逻辑删除命中源端元数据时，外层 `LogicRemoveInterceptor` 先构造 update，Async 复制该 update；只有 Async 实际收到 REMOVE 时，任务才重新调用静态 `LogicRemove.logic`，此时目标元数据、ignore 和 invocation 组合可能使目标执行 update 或 delete。动态 collection 已替换后的最终 namespace 被捕获并沿用；目标 database/collection 不支持映射。详情见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)、[DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md) 和 [MULTI_DATASOURCE.md](MULTI_DATASOURCE.md)。

## 目标选择、递归和重复

默认 `MultipleWriteHandler` 在构造时取得 `mongoPlusClient.getDataSourceNameList()`，把同一个当时列表分别登记给 SAVE/REMOVE/UPDATE/BULK_WRITE。自定义 `multipleWrite(...)` 返回非空列表时完全覆盖该操作默认列表；空/null 才回退。没有目标注解、目标 database/collection 映射、去重或排序。

任务执行时用 `if (!dsName.equals(DataSourceNameCache.getDataSource()))` 跳过“任务线程当前上下文名称”相同的目标，而不是与提交时主源的快照比较。没有设置/清理 ignore ThreadLocal。镜像使用字段中的裸 `DefaultExecute`，因此 save/update/bulk 不重新进入异步拦截器，A→B/B→A 不会由单次镜像写自动递归；删除的 `LogicRemove.logic` 也调用 invocation target，不会从工厂重建完整代理链。重复目标会重复提交，包含主源是否跳过受工作线程上下文影响。

默认目标列表是构造时由 `getDataSourceNameList()` 创建的 `ArrayList` 快照；之后新增/删除数据源不会自动更新它，除非用户通过 handler 策略覆盖。任务取得 collection 时才按名称查 client；排队期间替换 registry/client 时不持有提交时 client 快照。namespace 只含 database/collection，不含 datasource；同 namespace registry 风险见 [MULTI_DATASOURCE.md](MULTI_DATASOURCE.md)。分片路由后的 collection namespace 可被看到，但当前 datasource 上下文、session 与实际 client 的组合没有闭合保证，见 [SHARDING.md](SHARDING.md)。

## 线程池、上下文和关闭

默认每个拦截器实例创建一个 `ThreadPoolExecutor`：core=5、max=20、keepAlive=60 秒、`LinkedBlockingQueue(100)`、默认线程工厂、`CallerRunsPolicy`。核心线程默认不超时；默认线程工厂创建非 daemon 线程。用户只能通过构造器传完整 `ThreadPoolExecutor`，没有属性绑定或 Spring/Solon executor Bean 自动注入。

`submit` 返回的 Future 被丢弃。任务内部不捕获异常、不记录失败、不回调、不重试、无退避/死信/补偿/持久化/幂等检查，也不检查 update/delete count。由于 `submit` 先把任务包装为 `FutureTask`，即使 `CallerRunsPolicy` 在调用线程执行，任务异常也由这个无人读取的 Future 捕获，通常不会直接从 `submit` 抛回主链；框架日志只能看到“Executing...”开始日志，不能把它理解为成功。CallerRuns 仍会让镜像写在主 Driver 写前同步执行并阻塞主调用。各目标依列表顺序提交，但多线程执行/完成顺序不保证；一个目标失败不阻止已提交的其他任务。

没有显式复制 DataSourceNameCache、MongoTransactionContext、ShardingTransactionContext、MDC、身份或 Tenant 上下文。普通事务上下文是 ThreadLocal，不传播；数据源上下文是 InheritableThreadLocal，但线程池只在线程创建时继承，不能表示每次提交上下文，且任务不清理。线程复用可能看到创建线程时的陈旧数据源；CallerRunsPolicy 又会在提交线程直接运行，形成另一种上下文外观。没有框架级安全过滤、脱敏或 payload 上限；完整 Document、filter/update、Binary 和密文/明文引用都可能在队列中保留。

拦截器没有 `shutdown`、生命周期回调、等待时长或任务引用。Boot 3、Boot 4、Solon 都只收集 Bean，不关闭 executor。默认非 daemon 核心线程在首次任务后可能阻止 JVM 正常退出；多个上下文会创建/注册多个池。用户提供池时关闭责任也完全由用户承担，目标 MongoClient 仍由数据源生命周期负责。

## 事务、Listener 和 Recorder

任务不等待 afterCommit，也不传原 ClientSession，不为目标源开启事务。它始终使用字段中的 `DefaultExecute`；主事务回滚时镜像可能已提交，镜像失败也不会反馈主事务。这不是跨源原子事务，详见 [TRANSACTION.md](TRANSACTION.md)。

镜像直接调用目标 `MongoCollection`，所以 MongoDB Driver Command Listener 会看到真实目标命令；事件数取决于 Driver/bulk/retry，见 [COMMAND_LISTENER.md](COMMAND_LISTENER.md)。save/update/bulk 不重新经过普通 `DataChangeRecorderInnerInterceptor`，因此默认只记录主链，不记录镜像链；删除静态逻辑路径也未重建 recorder 代理。Listener 抛出的异常可能令目标 Driver 调用失败，但只留在被丢弃的 Future 中。Recorder 关系见 [DATA_CHANGE_RECORDER.md](DATA_CHANGE_RECORDER.md)。

## 已确认缺陷与运行风险

- **已确认缺陷：** 先提交镜像再执行主写，主写失败/回滚时镜像仍可能成功；这与一致性无关，且没有取消/补偿。
- **已确认缺陷：** 默认 executor 无关闭路径，Future 被丢弃导致任务异常不可观察。
- **已确认设计风险：** 同一可变参数对象并发复用，无深拷贝；源码确认存在共享可变引用，但是否实际形成错误结果需并发测试复现。
- **已确认缺陷：** 目标没有去重；多拦截器/多上下文静态注册也会放大写入。
- **待运行验证：** `_id` 生成时机、源/目标逻辑删除元数据不同的结果、队列满时 CallerRuns 上下文、默认线程继承污染、数据源被替换/关闭、分片/事务组合、Listener/Recorder 数量以及多个应用上下文退出行为。

## 最低测试清单

insert one/many、update one/many/upsert、delete one/many 的逻辑/物理分支、全部 WriteModel 与 ordered/unordered；动态集合、多数据源、分片；主成功/镜像失败、主失败/镜像行为、事务 commit/rollback；duplicate key、timeout、队列满、shutdown；重复/当前/循环目标和多个拦截器；可变参数竞态、上下文污染、并发与大 batch；Command Listener/Recorder；Boot 3、Boot 4、Solon 和多个应用上下文。

## 关键源码

- `mongo-plus-core/.../interceptor/business/AsyncMultipleWriteInterceptor.java`
- `mongo-plus-core/.../handlers/write/MultipleWriteHandler.java`
- `mongo-plus-core/.../config/Configuration.java`
- `mongo-plus-core/.../interceptor/{AdvancedInterceptorChain,Invocation}.java`
- `mongo-plus-core/.../execute/{ExecutorFactory,instance/DefaultExecute}.java`
- Boot 3/4 与 Solon 的 `config/MongoPlusAutoConfiguration.java`
