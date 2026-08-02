# MongoDB 命令监听

> 审计日期：2026-08-02。本文描述 `mongo-plus-core`、Boot 3、Boot 4 与 Solon 当前源码中的 MongoDB Driver `CommandListener` 接入。它是 Driver 命令事件机制，不是 MongoPlus CRUD `Interceptor`，也不是数据变更审计。

## 公开入口与真实类型

MongoPlus 对外暴露 `com.mongoplus.listener.Listener`，方法为 `commandStarted(CommandStarted)`、`commandSucceeded(CommandSucceeded)`、`commandFailed(CommandFailed)` 和 `getOrder()`。`BaseListener` 直接实现 Driver `CommandListener`，把原始事件包装后交给 `MongoPlusListener` 遍历全局 `ListenerCache.listeners`。

- 无容器：`Configuration.listener(Class...)`；`log()`、`blockAttackInner()` 追加内置 `LogListener`、`BlockAttackInnerListener`。
- Boot 3/Boot 4：发现全部 `Listener` Bean，按属性决定是否追加两个内置 listener。
- Solon：从 `AppContext.getBeansOfType(Listener.class)` 发现实例，内置项与 Boot 相同。
- 没有接收任意 Driver `CommandListener` Bean、现成 MongoPlus listener 实例或 `MongoClientSettings.Builder` customizer 的专用入口。

每个由 `MongoUtil.getMongo` 创建的 client 都默认安装一个框架 `BaseListener`。业务列表可以为空；日志和防攻击 listener 仅在显式配置后出现。

动态数据源追加也调用 `MongoUtil.getMongo`，因此自动携带 `BaseListener`。用户自行构造并注入、且未走该工具的 `MongoClient` 不会被 MongoPlus 自动补装 listener；若旧 client 被数据源覆盖，旧对象中的 `BaseListener` 仍可继续读取同一静态业务列表。

## 注册生命周期

```text
连接属性 → MongoUtil.getMongo
→ MongoClientSettings.Builder.commandListenerList([new BaseListener()])
→ MongoClients.create → MongoClientFactory 注册
→ 容器发现/追加业务 Listener → ListenerCache.sorted()
→ Driver 回调 BaseListener → MongoPlusListener 遍历全局列表
```

`BaseListener` 在 client 创建前写入 settings；当前源码不能给已创建 client 追加 Driver listener。业务 listener 不复制到各 client，而由所有 `BaseListener` 在回调时读取同一个静态列表，所以稍后发现的一个 Bean 会被所有这些 client 的后续事件看见。

静态主从数据源和 `DataSourceManager.appendTempDataSource` 都走 `MongoUtil.getMongo`，均携带 listener。覆盖数据源不关闭旧 client，仍持有旧引用的调用仍可能产生事件。目标已存在且 `isOverride=false` 时，方法仍先创建 client，却不注册也不关闭它，这是已确认资源缺口。

`ListenerCache` 是静态可变 `ArrayList`。Boot 3、Boot 4、Solon 都依次追加日志、防攻击、容器 Bean，再按 `getOrder()` 升序排序；Java 当前稳定排序使相同 order 保留当次追加顺序，但 Bean 枚举顺序、跨上下文累积顺序不是框架契约。无容器入口只追加、不排序。重复初始化不去重，可能重复回调；没有关闭钩子清空列表。多个应用上下文若共享 MongoPlus core 的同一类加载器，就共享此列表；不同 JVM/隔离类加载器不能据此宣称共享。

遍历与注册没有锁或快照：回调期间结构性增删可能抛 `ConcurrentModificationException`，并发读写还存在普通 `ArrayList` 的数据竞争，不能保证新 listener 在哪次事件开始可见。业务 listener Bean 实例本身也未复制，可能被不同 client 的 Driver 回调并发调用。

## 回调线程、顺序与异常

`MongoPlusListener` 在 Driver 的同一次回调栈中同步 `forEach`，没有队列、线程池、缓冲、丢弃策略或关闭回调。慢 listener 会延长回调。准确线程归属、started 与终态配对、retry/getMore/bulkWrite 的事件数量和 close 后在途回调由 Driver 运行时决定，不能从本仓库固化为契约。

三种回调使用同一结构：业务 listener 抛出 `Exception` 后，当前 `forEach` 终止，后续 listener 不再执行；`BaseListener` 记录并包装为 `MongoPlusInterceptorException` 再从 Driver 回调方法抛出。接口无返回值，MongoPlus 也没有异步队列、线程切换或事件副本。Driver 最终是否改变命令成功/失败、连接状态或调用方异常必须运行验证；`Error` 不在捕获范围内。

内置 `LogListener` 使用 `OrderCache.LOG_ORDER`，`BlockAttackInnerListener` 使用 `OrderCache.BLOCK_ATTACK_INNER_ORDER`。后者在 started 阶段检查 update/delete 第一项的 `q` 并可抛错；这是命令级保护，不是 CRUD 拦截器。

## 事件数据范围

| 回调 | MongoPlus 直接字段 | 保留的原始事件 |
|---|---|---|
| started | `commandName`、`commandDocument`、`commandDocument.toJson()` | `CommandStartedEvent` |
| succeeded | `commandName`、`response` | `CommandSucceededEvent` |
| failed | `commandName`、`throwable` | `CommandFailedEvent` |

按当前编译依赖的 Driver 5.4.0 API，原始事件还能取得 database name、request id、operation id、connection description、request context；成功/失败有 elapsed time。connection description 可继续取得 connection id、server address、service id。MongoPlus 未平铺这些字段，也未增加 datasource 或 `ClientSession` 字段。

同 database 的不同数据源不能靠包装 DTO 区分。原始连接信息可区分连接，但 MongoPlus 没有“事件 → datasource”注册表，不能稳定还原配置中的 datasource 名称。事务/session 可能体现在命令 BSON 或 request context，非 MongoPlus 稳定字段。

## 敏感数据、性能与资源

started BSON 是实际命令，可含完整写入/更新值、查询条件、pipeline，以及已由 Tenant/Logic、映射、加密或敏感词处理写入的最终值。动态集合体现在命令 namespace；逻辑删除在命令层只表现为 update，无法恢复业务语义。连接字符串不在命令 BSON 中，但业务文档可能包含密码、token、密钥或大型二进制。

MongoPlus listener 层没有字段排除、command/database 过滤、命令白/黑名单、脱敏、大小限制、截断或异步卸载。`LogListener` 会记录完整命令；`pretty=true` 还构建格式化文本。认证命令究竟是否经当前 Driver 回调、其中可见哪些认证数据，项目源码不能闭合，必须运行验证；不得由连接字符串不在普通 CRUD BSON 中反推认证信息一定不可见。用户必须自行承担日志泄漏、阻塞与内存成本。

## 多数据源、事务、分片与 Recorder

- 命令由哪个 client 发出，就由该 client 的 `BaseListener` 接收；分片换源后归目标 client。
- commit/abort、retry、getMore 是 Driver 命令，只有命令监听层直接可见；事件形态需实测。
- Recorder 若用 `BaseMapper.save` 持久化，会产生新的 Driver 命令事件。
- Command Listener 不见 Recorder 记录对象；Recorder 不见 Driver retry/getMore。一次业务操作可对应零个、一个或多个命令事件，不能按数量一一关联。

## 测试与证据

当前仓库未发现覆盖此链的回归测试。至少应覆盖 CRUD、aggregate/getMore、bulkWrite、索引、成功/失败、慢/异常 listener、多 listener/相同 order、重复初始化、多/动态数据源、旧 client、事务、分片、大 BSON、敏感字段及 Boot 3/Boot 4/Solon。

关键源码：`MongoUtil.java`，`BaseListener.java`，`Listener.java`，`MongoPlusListener.java`，`ListenerCache.java`，三个 command 模型，三个集成模块的 `MongoPlusAutoConfiguration.java`/`MongoPlusConfiguration.java`，`DataSourceManager.java`。

相关文档：[启动生命周期](../architecture/STARTUP_LIFECYCLE.md)、[扩展顺序](../architecture/EXTENSION_PIPELINE.md)、[事务](TRANSACTION.md)、[多数据源](MULTI_DATASOURCE.md)、[动态集合](DYNAMIC_COLLECTION.md)、[分片](SHARDING.md)、[兼容性](../COMPATIBILITY.md)、[测试](../TESTING.md)、[待验证问题](../OPEN_QUESTIONS.md)、[数据变更记录](DATA_CHANGE_RECORDER.md)。
