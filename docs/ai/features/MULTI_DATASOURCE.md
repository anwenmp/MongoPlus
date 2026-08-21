# 多数据源

> 审计日期：2026-08-02。本文区分公开 API、内部实现和待验证能力。启动期装配见 [STARTUP_LIFECYCLE.md](../architecture/STARTUP_LIFECYCLE.md)，CRUD 阶段见 [CRUD_EXECUTION.md](../architecture/CRUD_EXECUTION.md)。

## 数据源运行时模型

MongoPlus 的一个命名数据源不是单一对象，而是以下关联状态：

```text
dsName
  -> DataSourceNameCache.basePropertyMap[dsName] = BaseProperty
  -> MongoClientFactory.mongoClientMap[dsName] = MongoClient
  -> MongoPlusClient.collectionManagers[dsName][databaseName] = CollectionManager
  -> CollectionManager.collectionMap[collectionName] = MongoCollection<Document>
```

默认名称由 `DataSourceConstant.DEFAULT_DATASOURCE` 决定；`DataSourceNameCache.getDataSource()` 在线程上下文为空时回退该名称。`MongoDatabase` 由 client + database name 取得，`MongoCollection` 再由 database + collection name 取得。静态配置的默认项和 `slaveDataSource` 在容器启动时经 `MongoUtil.getMongo(dsName, property)` 转为 client，同时该方法把 BaseProperty 写入 `DataSourceNameCache`。

注册表和缓存边界：

| 状态 | 维护者 | 并发/作用域 |
|---|---|---|
| 名称→`MongoClient` | 静态单例 `MongoClientFactory` 的 `ConcurrentHashMap` | JVM 全局 |
| 名称→`BaseProperty` | `DataSourceNameCache` 的普通 `HashMap` | JVM 全局，未见同步 |
| 当前名称 | `DataSourceNameCache` 的 `InheritableThreadLocal<String>` | 当前线程，创建子线程时可继承快照 |
| 数据源→数据库→manager | 单个 `MongoPlusClient` 的 map | 通常容器单例；外层启动时为 concurrent map，动态追加的内层 map 是 `LinkedHashMap` |
| 集合名→collection | 每个 `CollectionManager` 的 `ConcurrentHashMap` | manager 范围 |
| namespace→实体 | `MongoEntityMappingRegistry` 全局 `ConcurrentHashMap` | JVM 全局；key 不含 client/datasource |

## 配置、动态注册与 Lazy

静态数据源：Boot 3/4 和 Solon 都创建默认 client，再遍历 `slaveDataSource` 按 `slaveName` 注册。重复名称调用 `MongoClientFactory.addMongoClient` 时直接 `put`，会替换引用，不关闭旧 client。`MongoPlusClient` 为每个已注册名称和配置中的逗号分隔 database 建立独立 `CollectionManager`。

公开 Bean `DataSourceManager` 提供 `appendTempDataSource(dsName, BaseProperty, isOverride)`。它会先无条件创建新 `MongoClient`；名称不存在或 `isOverride=true` 时才加入工厂和 manager map。由此可见，名称已存在且不覆盖时，新建 client 没有被注册或关闭，是生命周期风险。覆盖也不关闭旧 client。没有删除 API；替换仅通过 `isOverride=true`，不能据此承诺无缝并发切换。

`MongoDBConfigurationProperty.lazyDataSource` 字段在部分集成属性类中存在，但定向源码搜索未发现消费它的初始化或首次使用路径。因此当前不能把 Lazy DataSource 写成已实现公开能力，也不给出使用示例。静态和动态注册都会立即调用 `MongoUtil.getMongo` 创建 Driver client；具体 socket/握手仍由 Driver 延迟。

创建失败发生在写入注册表之前时不会留下该 client 的 map 条目；但 `MongoUtil.getMongo`、BaseProperty cache 和 Driver 延迟错误之间的精确部分失败状态尚缺测试。`MongoClientFactory.close()` 关闭当前 map 中的 clients；被覆盖、未注册的临时 client 不在其关闭范围内。

## `@MongoDs` 路由

### Boot 3 / Boot 4

两套 `MongoDataSourceAspect` 当前相同，切面 `@Order(0)`，匹配类级或方法级注解：

1. 先用 `AnnotationUtils.findAnnotation(method, MongoDs.class)` 取方法注解。
2. 方法无注解或其 `value` 为空时，回退声明类注解；因此非空方法级覆盖类级，空方法级不会覆盖非空类级。若空方法注解存在但声明类没有 `@MongoDs`，回退结果为 `null`，随后 `mongoDs.value()` 会触发空指针，而不是进入最终的空名称校验；准确异常外观待容器测试。
3. value 包含 `#` 时，先移除所有 `#`，再以 `MethodBasedEvaluationContext(joinPoint, method, args, parameterNameDiscoverer)` 求值。参数和 join point root 可参与；返回值尚未产生，不能参与。
4. `dsHandler != Void.class` 时反射创建 handler，并用 `getDataSource(currentValue)` 替换名称。
5. 最终名称为空抛 `MongoPlusException("Data source not found")`。
6. `DataSourceNameCache.setDataSource(name)` 写入上下文后才进入 `try`，目标方法无论正常返回还是抛出都会在 `finally` 调用 `clear()`。注解查找、SpEL、handler 或空名称校验发生在 set/try 之前；这些失败不会写入新上下文，也不会由本次切面的 finally 清理调用前已有的上下文。

SpEL 解析错误直接阻止调用；表达式不是保留 `#var` 语法，而是先全局删除 `#`，实际可用表达式必须以当前实现验证。不存在 `condition` 属性。

### 嵌套、异常、并发和异步

上下文不是栈。A 数据源方法进入 B 数据源方法后，B 的 finally 直接 remove；返回 A 后得到默认数据源，而不是恢复 A。递归同理。异常路径会执行 finally，能清除当前线程值。同类内部调用若未经过 Spring AOP 代理，不触发切面。

`InheritableThreadLocal` 提供的是创建子线程时的值复制，不是线程池任务提交时的可靠传播：

- 普通并发线程各自有 ThreadLocal 隔离。
- 已存在的线程池、`CompletableFuture` common pool、Reactor 和异步事件没有框架级传播或清理实现，视为不支持自动传播。
- 新建子线程可能继承值，但子线程的清理不由父切面的 finally 代替；长生命周期子线程存在泄漏/陈旧值风险。
- 线程池复用时只要业务绕过切面手动调用 `changeDataSource` 而未 clear，就会污染后续任务。

### Solon

Solon 的 `MongoDataSourceAspect` 实现只读取 `inv.method().getAnnotation(MongoDs.class)`；类级注解、SpEL 和 `dsHandler` 没有实现。若该拦截器被调用，无方法注解时 `Optional.map` 返回空 `Optional` 而不是调用目标方法；有方法注解时，目标异常被包装为 `RuntimeException`，finally 清理上下文，且仍不支持栈式恢复。

但这只是实现类的静态行为：`XPluginAuto` 对 `@MongoTransactional` 显式调用 `beanInterceptorAdd`，对 `@MongoDs` 只 `beanMake(MongoDataSourceAspect.class)`，`MongoPlusConfiguration` 也只是声明同类型 Bean。当前仓库没有看到 `MongoDs.class` 与该拦截器的绑定入口。因此“Solon 支持方法级字面量路由”尚不能确认为运行能力；必须先用 Solon 集成测试确认拦截器是否会被调用及其注册约定，再验证方法级、类级、空值和异常行为。这是待验证集成边界，不在本文判定为缺陷。

## CollectionManager 的实际缓存键

实际调用路径为：

```text
Mapper/DefaultBaseMapperImpl
 -> MongoPlusClient.getCollection(...)
 -> getCollectionManager(dsName, databaseName)
 -> collectionManagers[dsName][databaseName]
 -> CollectionManager.getCollection(dsName, collectionName, clazz)
 -> collectionMap[collectionName]
 -> miss: MongoClientFactory[dsName].getDatabase(databaseName).getCollection(collectionName)
```

所以最内层 `collectionMap` 的真实 key **只有 `collectionName`**；`databaseName` 和 `datasourceName` 不进入该 map key，但在选择 `CollectionManager` 实例时构成外层两级 key。正常由 `MongoPlusClient.getCollectionManager(ds,database)` 进入时，不同 datasource/database 应得到不同 manager，从实例边界实现隔离。

仍有必须保留的边界：`MongoPlusClient.getCollection(dataSource,database,collection)` 最终显式把 ds 传给 manager；若调用方取得某个 manager 后直接以另一 ds 调用其公开 `getCollection(dsName, collectionName)`，同名缓存会返回首次 client 的 collection。分片/动态集合是否形成这种跨边界复用尚未完成行为验证，因此不判定为缺陷，见 [OPEN_QUESTIONS.md](../OPEN_QUESTIONS.md)。

动态集合拦截器发生在 Mapper 已取得原 collection 之后，通过普通执行代理替换 collection 参数；新动态名称由 `MongoPlusClient` 再取 collection，仍走上述缓存。`MongoEntityMappingRegistry` 的 key 是 `mongoCollection.getNamespace().getFullName()`，即 `database.collection`，不含 datasource/client，且 `putIfAbsent` 保留首次实体。正常多数据源模型下同 namespace 共用实体 metadata 是预期设计；实际数据访问由 datasource 对应的 client/manager/collection 隔离，因此不把 registry key 不含 datasource 视为实体串源问题。无实体重载仍会以 `UnClassCollection.class` 参与首次登记，因此不是“Map 模式跳过 registry”。

所有 collection/registry cache 都没有按数据源替换或关闭自动失效的代码。client 被覆盖后，既有 `MongoCollection` 仍指向旧 client。

## 事务与其他功能

- 数据源名称通常在 `@MongoDs` 切面进入业务方法时确定；Mapper 随后从上下文选择 `CollectionManager` 和 client。
- `MongoTransactionalManager.getMongoClient()` 在创建 `ClientSession` 时通过当前数据源名选 client，session 因此绑定当时的 client。`ExecutorFactory` 只检查 `MongoTransactionContext` 是否有 session，并不会校验随后取得的 collection 是否来自同一 client。
- 普通事务状态只以开始时的数据源名保存 session，`ExecutorFactory` 按调用当时的当前数据源名查询。若 `@MongoDs` 在取得执行器前已经切换到新名称，查询结果为 null并回退 `DefaultExecute`，该次操作逃逸事务，而不是直接复用旧 session。若执行器先捕获旧 session，之后动态集合/分片等插件再把 collection 改到另一 client，则会形成旧 session + 新 collection；源码没有 client 一致性校验。前者是已确认分支，后者的 Driver 异常外观待运行验证。
- Spring `MongoPlusTransactionalManager`/Spring `@Transactional` 的具体 thread-bound 状态与 `@MongoDs @Order(0)` 相对顺序尚未建立运行证据；不能由注解名推断。
- 同一次实体 CRUD 中，数据库/集合解析和 collection 获取发生在 `ExecutorFactory.getExecute()`/Driver 调用前；动态集合普通拦截器可在执行代理内再次替换 collection。
- 租户、逻辑删除、自动填充不选择数据源；它们作用于选定 collection 的参数或实体转换。Mapper 代理/`MongoPlusClient` 通常为单例，隔离依赖 ThreadLocal 与 manager 外层 key，而不是每数据源一套 Mapper。
- 普通/高级拦截器为全局静态链，跨数据源共享；高级异步多写和 sharding 的完整上下文/事务组合尚待验证。
- Recorder 保存审计记录时会临时切到配置的 datasource；2026-08-08 起它通过 `DataSourceNameCache.getDataSourceOrNull()` 保存原始 nullable 状态，并在 finally 中恢复原值或清空。该修复只覆盖 Recorder 局部保存，不改变 `@MongoDs` 嵌套上下文仍非栈式的事实。
- Boot 3 sharding starter 依赖普通 Boot 3 starter；Boot 4 没有对应 sharding starter。Solon 未发现分片集成入口。

## 公开用法（已由源码确认）

静态配置的精确属性层级应以 `MongoDBConnectProperty` 和配置元数据为准；本文避免复制可能漂移的 YAML。运行时用法：

```java
@MongoDs("archive")
public List<Order> findArchive() {
    return orderMapper.list();
}

@MongoDs("#tenant") // Boot 3/4：参数表达式；具体参数名需保留编译元数据
public List<Order> findByTenant(String tenant) {
    return orderMapper.list();
}

dataSourceManager.appendTempDataSource("archive", baseProperty, false);
```

Boot 3/4 的切面源码支持非空方法级、类级和参数 SpEL；默认数据源不写注解即可回退默认名称。Solon 只有一个“方法级字面名称”的拦截器实现，但注册链未闭合，暂不对外宣称路由能力。Lazy 用法不提供，因为属性未接入执行路径。动态 API 会立即创建 client，且调用方必须理解覆盖和关闭风险。

## 风险与最低回归测试

主要风险：非栈式上下文导致嵌套后回退默认；手动切换或子线程造成泄漏；异步不传播；同名 namespace/manager 越界复用；动态覆盖后旧 client/collection 残留；事务 session 与 collection 跨 client；SpEL 删除 `#` 后的解析失败；普通 HashMap 配置注册和动态 manager map 的并发；配置属性向后兼容；Solon 类级/无注解/异常包装差异。

最低回归矩阵：默认数据源、两个静态数据源、Boot 方法级覆盖类级、空方法值回退类级、Solon 方法级差异、嵌套 A→B→A（当前预期不会恢复）、异常清理、多线程隔离、线程池与子线程、两个数据源同 database/collection 的 client/collection 路由隔离、直接 manager 越界、动态添加/重复/覆盖/创建失败、Lazy 当前未生效、事务 session 绑定与禁止跨源、动态集合组合、Boot 3/Boot 4/Solon。测试策略见 [TESTING.md](../TESTING.md)。
