# 动态集合

> 审计日期：2026-08-02。结论来自当前源码。映射见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)，执行器见 [CRUD_EXECUTION.md](../architecture/CRUD_EXECUTION.md)，代理见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)，事务组合见 [TRANSACTION.md](TRANSACTION.md)。

## 公开入口和适用范围

动态集合没有专用注解、配置项或线程上下文。公开扩展点是 `CollectionNameHandler`：`dynamicCollectionName(ExecuteMethodEnum, Object[] args, MongoNamespace originalNamespace)`。Handler 能看到操作类别、Execute 参数和原 namespace，但框架不直接传实体类型、Mapper 业务参数或业务上下文；租户/日期上下文及清理由用户负责。

Boot 3、Boot 4、Solon 均在启动后尝试从容器取得一个 `CollectionNameHandler` Bean，成功后向全局 `InterceptorChain` 注册 `DynamicCollectionNameInterceptor`；查找异常被忽略，未找到即不启用。三类集成的注册方式等价。

Mapper、Service、Repository 以及 CRUD/聚合 Chain API 最终进入具有参数策略的 Execute，因此适用。索引虽经过 Execute 代理，但当前无对应 `ExecuteMethodEnum`/参数策略，普通 before 不运行，故 Handler **不应用于索引**。框架有显式 database/collection 的 BaseMapper/MongoPlusClient 入口，但受支持操作仍可被全局动态 Handler 再次改名；没有本次调用跳过 Handler 的框架上下文。

## 实际解析链

假设的“AnnotationOperate → 动态处理 → 数据源 → CollectionManager → Registry → ExecutorFactory”与源码顺序不符。实体 CRUD 实际为：

```text
实体类型
 -> AnnotationOperate：@CollectionName 或类名转换
 -> DataSourceNameCache + 注解 database：选择数据源/数据库
 -> MongoPlusClient / CollectionManager：取得原 collection，首次登记真实实体
 -> ExecutorFactory：按事务上下文选执行器，包装高级和普通代理
 -> ExecutorProxy 确认存在参数策略
 -> DynamicCollectionNameInterceptor.beforeExecute
 -> Handler(operation, args, 原 namespace)
 -> MongoPlusClient.getCollection(原 database, 动态名称)
 -> CollectionManager：取得动态 collection，首次登记 UnClassCollection
 -> 替换 args 最后一项
 -> 后续普通插件 -> 高级代理 -> Driver
```

动态名称在每次受支持 Execute 调用解析，不在启动时固化；它在 `ExecutorFactory` 之后、Driver 之前确定。原 collection 已先取得并可能登记实体。拦截器 order 为 2，但普通链最终顺序还取决于注册入口/排序；同次 `ExecutorProxy` 的局部 `collection` 仍是原对象，后续专用参数策略和 after 不保证看到动态对象，详见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)。

Handler 返回值无校验/fallback：null 或空字符串直接进入 `MongoPlusClient.getCollection`。null 在 `CollectionManager.collectionMap.containsKey(null)` 处因 `ConcurrentHashMap` 不接受 null key 而失败；空值继续交给 Driver 创建 namespace/collection，准确异常类型按 Driver 版本待测。Handler 抛错会阻止 I/O，普通 after 不运行；框架无清理动作，因为未建立名称上下文。

## 并发和上下文

- 框架只持有 Handler 引用，不保存本次动态名；没有 ThreadLocal、栈、嵌套恢复或异常清理。
- Mapper/Service 和 Handler Bean 通常为单例；框架不串行、不复制、不保护 Handler 可变字段，线程安全由实现者负责。
- `collectionMap` 是 `ConcurrentHashMap`，但创建是 `containsKey` → open → put，不是原子 `computeIfAbsent`。并发首次访问可能重复创建 wrapper；cache 留下最后一次 put，registry 的 `putIfAbsent` 保留第一次类型。
- 嵌套 Mapper 调用分别重新执行 Handler，不存在恢复外层名称。

## CollectionManager

结构是 `datasourceName -> databaseName -> CollectionManager -> collectionName -> MongoCollection<Document>`。最内层真实 key 仅 `collectionName`；正常入口通过 manager 实例隔离 ds/database。若直接取得一个 manager 却传另一 ds 名，同名 cache 仍可能复用首次 collection。

每个动态名称形成独立 cache 项。没有单项删除、clear、容量或过期策略；大量租户/日期名称会持续增长。动态数据源覆盖/关闭不会清已有 wrapper，也不通知 registry。首次创建不是原子的。

## MongoEntityMappingRegistry

registry 是进程级单例 `ConcurrentHashMap<String, Class<?>>`：

- key 是 `MongoNamespace.getFullName()`，即 `database.collection`，不含 datasource/client。
- `putIfAbsent` 保留同 namespace 第一次实体；后来的不同实体不覆盖。
- 原 collection 在动态拦截前通常已登记真实实体；动态拦截器用无实体的 database/name 重载，动态 collection 首次登记 `UnClassCollection.class`。
- Map/Document 显式模式也登记 `UnClassCollection`，不是跳过 registry。
- registry 有 public remove/clear，但当前仓库没有生产调用方；collection cache 也无对应清除。外部若直接调用删除，之后访问已缓存 collection不会重新触发首次登记，不能视为完整重建 API。
- 数据源关闭/覆盖不自动清 registry。

逻辑删除和乐观锁按 namespace 反查实体。通常路径中动态 collection 第一次由无实体重载创建并先登记 `UnClassCollection`；`putIfAbsent` 使后续 `LogicNamespaceAware` 或真实实体登记无法覆盖，因此该 namespace 在 registry 中继续是 `UnClassCollection`。但“动态 namespace 永远无法登记真实实体”不成立：若同 namespace 在动态拦截前已由实体入口首次登记，真实实体会保留。逻辑删除拿到 `UnClassCollection` 后会按该类初始化逻辑字段，静态源码可确认它不会取得原实体的逻辑删除元数据；最终 BSON/物理行为仍应以组合测试固定。乐观锁及跨数据源同 namespace 的实际影响同样待测。

## 功能组合

| 组合 | 已确认顺序和边界 |
|---|---|
| 多数据源 | 原 collection 先按当前 ds 获取；动态拦截器保留原 database，却调用无显式 ds 的入口，因此按拦截时当前 `DataSourceNameCache` 再取 manager。非栈式上下文和中途切换待测。 |
| 事务/ClientSession | `ExecutorFactory` 先选 SessionExecute，动态拦截器后换 collection；无 session/client 一致性校验，见 [TRANSACTION.md](TRANSACTION.md)。 |
| 多租户 | Tenant 与动态集合都是普通插件；每个 before 后立即执行该操作参数策略。最终顺序依注册/排序；租户 Handler 不自动成为集合上下文。 |
| 逻辑删除 | 条件增强在普通/高级插件，实体识别依 registry；通常首次动态登记为 UnClassCollection 后，逻辑删除不会取得原实体元数据，这是已确认源码行为；最终查询/更新/删除 BSON 与物理结果待测。 |
| 自动填充/实体映射 | 实体转 Document 和填充在执行代理前；动态名不改 converter 实体类型。Driver 返回后仍按 Mapper 目标类型转换。 |
| 索引/时序 | Handler 不作用于索引 Execute；启动期索引/时序创建不会自动枚举运行时动态名称。组合能力未建立。 |
| 分片 | 两种插件都可替换 Execute 最后一项；分片另含高级 session 处理。完整 order 和最终 client/namespace 待测。 |
| Map 模式 | 显式 namespace 仍可被 Handler 覆盖；registry 为 UnClassCollection，读取差异见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)。 |

## 最低测试清单

- 默认集合、一个 Handler、两个动态名称；null/空/抛异常。
- Mapper、Service、Repository、Query/Update/Aggregate Chain、显式集合和索引不切换。
- 并发首次访问、单例 Handler 可变状态、嵌套和异常。
- 同名跨 database/datasource；同 namespace 不同实体和 registry 首次保留。
- 逻辑删除、多租户、自动填充、实体、Map/Document。
- MongoPlus/Spring 事务和 session/client 一致性。
- 索引、时序、分片、动态数据源关闭/覆盖。
- Boot 3/4/Solon Bean 发现、重复初始化和全局链污染。

## 关键源码

- core: `handlers/CollectionNameHandler.java`、`interceptor/business/DynamicCollectionNameInterceptor.java`、`proxy/ExecutorProxy.java`、`conn/CollectionManager.java`、`manager/MongoPlusClient.java`、`registry/MongoEntityMappingRegistry.java`
- core: `handlers/collection/AnnotationOperate.java`、`execute/ExecutorFactory.java`、`mapper/DefaultBaseMapperImpl.java`、`index/impl/AbstractBaseIndex.java`
- Boot 3/4/Solon: 各自 `config/MongoPlusAutoConfiguration.java#setDynamicCollectionHandler`
