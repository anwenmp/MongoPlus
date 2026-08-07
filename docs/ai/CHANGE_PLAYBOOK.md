# 代码修改作战手册

本文用于把修改任务快速路由到最小阅读范围、真实执行链、跨模块接线和验证矩阵。它不是源码事实的替代品：每次修改仍须从当前工作树重新查询 CodeGraph，并直接核对 CodeGraph 未覆盖的 POM、资源和测试。

## 证据与边界

- 事实优先级：当前源码 → 当前测试 → POM/自动配置/元数据 → `docs/ai` → 用户文档。
- “静态确认”只表示当前控制流或声明存在；“已运行”必须给出本轮命令和结果；“推荐验证”不是已有测试。
- 只改完成任务所需的最小文件，禁止顺手重构、整理命名或扩大公开 API。
- 先确认 Maven 模块、公开入口和兼容面，再写代码。潜在缺陷先查 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md)，但必须重新核对当前源码。

## 通用修改流程

```text
任务分类 → INDEX 路由 → 阅读 1～3 个专题 → CodeGraph 定向查询
→ 确认公开入口 → 确认执行链位置 → 确认跨模块接线
→ 修改最小源码 → 增加/更新回归测试 → 构建受影响模块
→ 检查兼容性与用户文档 → 必要时更新 docs/ai
```

1. 从 [INDEX.md](INDEX.md) 选择 1～3 个文件，不遍历知识库。
2. 用 CodeGraph 同时查询功能名、已知符号、起点/终点、调用方和测试；输出不足再读取精确源码。
3. 写出公开入口、转换/代理/拦截位置和最终 Driver 边界；若改动触及 `Execute` 契约或执行器共享逻辑，再同时核对 `DefaultExecute`/`SessionExecute`。
4. 先判断能力实际覆盖哪些入口，再核对相关的实体、无实体 `Document`/Map、Wrapper/Chain、bulk/index/aggregate；不存在或不共享的路径明确排除，不为凑矩阵扩张修改。
5. 先判断是否触及容器接线，再按需核对 Boot 3、Boot 4、Solon；transaction、multi datasource、dynamic collection 也只在调用链相交时纳入。
6. 先建立最小失败测试；修改最少的生产文件；运行 [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) 中对应的最小验证。
7. 检查公开 API、配置、BOM/starter、用户文档和知识库是否发生事实变化。

以下变化才更新 `docs/ai`：模块职责/依赖、核心链路、公开 API/注解/配置、扩展机制、兼容策略或重要决策发生变化。用户可见 API、配置、默认值、返回语义或使用方式变化时同步公开用户文档。新发布构件或依赖接线变化时同步根 POM、BOM、相关 starter 和 [MODULES.md](MODULES.md)。

## 修改公开 CRUD API

先读 [PUBLIC_API.md](PUBLIC_API.md)、[architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)，涉及拦截顺序再读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md)。

- 真实拓扑不是一条继承链：`MongoMapper<T>` 不继承 `BaseMapper`；`MongoMapperImpl<T>` 实现 `MongoMapper<T>` 并持有、委托 `BaseMapper`。`BaseMapper` 的默认实现支路是 `DefaultBaseMapperImpl → AbstractBaseMapper implements BaseMapper`，再经 `ExecutorFactory` 选择 `DefaultExecute`/`SessionExecute`。
- 上层继承支路是 `IRepository<T> extends MongoMapper<T>`、`RepositoryImpl<T> extends MongoMapperImpl<T> implements IRepository<T>`、`IService<T> extends IRepository<T>`、`ServiceImpl<T> extends RepositoryImpl<T> implements IService<T>`；`ServiceImpl` 当前是空子类。方法放置取决于它是否需要实体绑定、Repository chain 便利或业务 Service 语义。
- 同步检查实体入口与 `BaseMapper` 显式 namespace/无实体入口、默认方法与实现委托、返回值的 acknowledged/matched/modified/deleted 语义。
- 按方法实际复用关系检查 Chain API、aggregate/index、普通/高级拦截器；只有接口发现、代理创建或 Bean 注入发生变化时，才扩展到 Boot 3/4/Solon Mapper 接线。
- 最小测试以被改方法及其真实入口为中心；若改动穿过共享 Execute 契约，才同时覆盖 session/非 session；若触及容器代理，才增加对应容器 smoke test。不要把完整 CRUD/批量/三容器矩阵伪装成每个小改动的固定最低要求。

重点防止：只改接口不改实现；只改 `MongoMapper` 不看 Repository/Service；遗漏 `SessionExecute`；三套容器代理只改一套；把 `modifiedCount` 当作“是否命中”。

## 修改 Query/Update Wrapper

先读 [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md)，按映射或执行问题补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) 或 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)。

- 最小影响面：条件接口/Wrapper → condition meta object → `BuildCondition`/`Filters` → chain 终结方法 → Mapper/aggregate match。
- `condition=false` 应在加入可变条件状态前短路。分别验证 Lambda 与字符串字段、单值与集合、null/空集合/空字符串、重复字段和 wrapper 复用。
- Lambda 路径可能参与字段名、ID/ObjectId 和 `EncryptorConditionHandler`；字符串路径不能自动假定同等元数据能力。
- 特殊语义单独固定：NOT/EXPR、嵌套 AND/OR/NOR、`$in`/`$nin`、custom BSON、update BSON 合并。
- 检查 Repository/Chain 是否因共享接口自动获得能力，以及聚合顶层 match 是否复用相同构建逻辑。
- Query Chain 的 `clear()` 清除查询条件、排序、投影和自定义 BSON；Update Chain 覆盖它并额外清除 update 条件/BSON。Aggregate Wrapper 当前没有 `clear()`/`reset()`；涉及复用语义时必须分别验证，不能套用 Query/Update 结论。
- 最小测试以最终 BSON 为断言；需要 Server 语义时再加真实 MongoDB 命中/更新测试。

## 修改 Aggregate API

先读 [architecture/AGGREGATION.md](architecture/AGGREGATION.md)，按条件或映射补读 [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) 或 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md)。

- 最小影响面：Aggregate Wrapper/stage → 可变 pipeline/options → Mapper → `DefaultExecute`/`SessionExecute` → `AggregateIterable<Document>` → DTO/Document/TypeReference 映射。
- Tenant 在顶层插入 `$match`，Logic Delete 在顶层追加 `$match`；不要扩张为 lookup/facet/union 子 pipeline 自动增强。
- Dynamic Collection 只替换主 collection；外部 collection、跨库/跨数据源 lookup 需单独设计和验证。
- 同时覆盖 stage 顺序、wrapper 复用、session/非 session、DTO、`Document`、Map。`Class<Map>` 与 `TypeReference<Map<...>>` 的顶层转换应以 converter 级回归固定，不能将其描述为递归缺陷。

## 新增或修改普通 Interceptor

先读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) 和 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)。

- 最小影响面：`Interceptor`/参数策略 → `InterceptorChain` → `ExecutorProxy` → Boot 3/4/Solon Bean 收集。
- 明确 `ExecuteMethodEnum` 覆盖面；索引方法的枚举值为 `null`。核对 before/after、短路、异常传播、collection 替换、args 写回和 batch model 重建。
- `ExecutorProxy` 的通用成功返回路径即使枚举值为 `null` 也会调用普通 `afterExecute`；异常路径是否清理资源必须按具体拦截器核对，不能由成功路径外推。
- 记录精确 `order()`。同 order、Bean 枚举顺序、静态链跨上下文累积不能写成稳定契约。
- 参数策略返回的新 pair/list 必须真正回写到 Driver 参数；仅修改临时对象不等于生效。
- Dynamic Collection 通过改写参数数组最后一个 collection 生效；同一次调用中，较早拦截器已经拿到的原 collection 引用不会随之自动替换，顺序和参数来源必须一起检查。
- 验证 after 是否覆盖每个成功返回路径，异常时 ThreadLocal 是否在 `finally` 清理。
- 最小测试：每种 ExecuteMethodEnum、index、bulk 各 model、同 order、多 Bean、异常；再组合多数据源、事务和动态集合。

## 新增或修改 AdvancedInterceptor

先读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md)，涉及删除/锁/分片再读对应 feature。

- 最小影响面：`AdvancedInterceptorChain` 包装方向、`order()`、`chain.proceed`、捕获的旧 Execute/session/collection 与最终 Driver 调用。
- 明确是否把 delete 转成 update、是否重新进入普通链、返回值与异常如何传播。
- 组合检查 LogicRemove、OptimisticLock、Async Multi Write、Sharding。当前 LogicRemove order 为 `Integer.MAX_VALUE - 1`，OptimisticLock 为 `Integer.MAX_VALUE`；每次仍须从源码复核。
- 最小测试记录进入/退出事件、最终操作类型、collection/session、结果与异常；覆盖事务、多数据源和动态集合。

## 新增字段注解或 Mapping Handler

先读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md)，按能力补读 [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md)、[features/DESENSITIZATION.md](features/DESENSITIZATION.md) 或 [features/SENSITIVE_WORD.md](features/SENSITIVE_WORD.md)。

- 最小影响面：annotation 模块 → 元数据读取 → write/read handler 或 converter → save/update/query/aggregate/bulk 各入口 → starter/plugin 注册。
- 当前写 Handler 不保证串联前一 Handler 的输出，后命中 Handler 可能覆盖前值；修改前固定输入输出契约。
- 检查 TypeHandler/ReadHandler、`discontinue`、字段重命名、嵌套对象、DBRef、Auto Fill、加密/脱敏/敏感词组合。
- 分开验证实体、DTO、Map/Document；save、entity update、wrapper-only update、bulk 和 aggregate 不共享完整转换链。
- 最小测试做 Document↔对象双向断言，并覆盖 null、集合、嵌套、异常及 Handler 组合顺序。

## 新增注解功能

先读 [PUBLIC_API.md](PUBLIC_API.md) 与消费该注解的 feature/architecture 专题。

- 最小影响面：annotation 模块 → core 消费点 → Boot 3 AOP → Boot 4 AOP → Solon interceptor 绑定 → 文档/配置。
- 分别检查类级/方法级 pointcut、方法优先级、空注解值与 class fallback、内部调用绕过。
- ThreadLocal 必须覆盖 set、嵌套恢复和 `finally` clear；单槽 remove 不是嵌套栈。
- Solon 中存在 Aspect 类不等于注解已绑定；必须核对 `beanInterceptorAdd` 或实际框架接线。
- 最小测试是三套容器独立启动，覆盖类/方法、空值、内部调用、嵌套和异常清理。

## 新增配置项

先读 [CONFIGURATION.md](CONFIGURATION.md)、[architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)，版本差异补读 [COMPATIBILITY.md](COMPATIBILITY.md)。

- 最小影响面：Boot 3 Properties、Boot 4 Properties、Solon Properties、默认值/getter/setter、实际消费者、元数据/示例和绑定测试。
- 三套配置 key、prefix、kebab-case 拼写和默认值必须逐项比对；字段存在但无消费者必须标记，不得写成可用能力。
- 不创建源码中不存在的“对称”配置类或字段；`ResultHandler` 是枚举而非配置字段，Driver URI 自带能力也不能写成框架独立配置项。特别检查 `lazy-data-source`、`ignore-char` 这类已声明但未发现消费者的字段。
- 若 `url` 非空白导致独立字段短路，必须记录优先级。Driver URI 能力不能冒充框架独立配置。
- 最小测试：缺省值、显式值、显式 null/空字符串（适用时）、URI 优先级和三套容器绑定。

## 修改数据源能力

先读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)、[architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)，事务组合补读 [features/TRANSACTION.md](features/TRANSACTION.md)。

- 最小影响面：`MongoClientFactory`/`MongoPlusClient` → `DataSourceManager`/`DataSourceNameCache` → `CollectionManager`/Registry → `@MongoDs`/handler → Boot 3/4/Solon。
- 检查默认 `master`、URI 优先级、动态覆盖后的旧 client/collection、嵌套上下文、线程池传播、session/client 是否同源。
- 最小测试：两个数据源同 database/collection、嵌套切换、异常、线程复用、覆盖/关闭、事务内换源和三套容器。

## 修改事务能力

先读 [features/TRANSACTION.md](features/TRANSACTION.md)、[architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)，多源/分片再补读相应专题。

- 最小影响面：编程式事务、Spring TransactionManager、Solon 注解绑定 → transaction context → `ExecutorFactory` → `SessionExecute` → ClientSession commit/abort/close。
- 检查嵌套引用计数、checked/unchecked/Throwable、commit/abort/close 任一步异常、内存对象回写状态。
- 多数据源必须保证 session/client 一致；分片事务不能默认等同分布式原子事务。
- 最小验证需要真实 replica set/sharded MongoDB，记录 session id、start/commit/abort/close 次数及 session/非 session 命令。

## 修改 Registry 或 CollectionManager

先读 [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md)、[architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md)，多源问题补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)。

- 最小影响面：datasource/database/collection 缓存层级、registry key、`putIfAbsent`、`UnClassCollection`、dynamic namespace，以及 Logic Delete/Optimistic Lock/自动索引的元数据读取。
- registry key 当前不含 datasource；并发首次创建、动态名称增长、数据源覆盖和清理都需组合验证。
- 最小测试：双数据源同 namespace/不同实体、并发首建、实体与无实体登记先后、动态集合、逻辑删除、乐观锁和索引。

## 修改模块、Starter 或 POM

先读 [MODULES.md](MODULES.md)、[BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md)、[COMPATIBILITY.md](COMPATIBILITY.md)。

- 最小影响面：根 `<modules>`、parent/version、dependencyManagement、独立 BOM、上游/下游依赖、optional/provided、Java target、发布 profile。
- Starter 还要检查 AutoConfiguration.imports、Spring 注解/属性；Solon 检查 plugin metadata；sharding 当前只有 Boot 3 starter。
- 新公开构件要同步 BOM；只有构件应由根聚合时才加入 `<modules>`。根 reactor 与 reactor 外 BOM 必须分别构建，正式发布批次与顺序作为独立问题验证。
- 最小验证按 [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) 的 POM/模块矩阵执行，不把依赖解析当启动或行为测试。

## 修复已确认缺陷

```text
OPEN_QUESTIONS 已确认缺陷 → 重新定位当前源码 → 编写最小失败测试
→ 最小修复 → 受影响模块测试 → 组合测试
→ 更新专题状态 → 必要时更新 CURRENT_STATE
```

没有回归测试不得从 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) 删除缺陷；修复时不得顺便改变无关公开 API。

## 常见遗漏检查表

- [ ] `DefaultExecute` / `SessionExecute`
- [ ] Boot 3 / Boot 4 / Solon
- [ ] 实体 / DTO / Map / Document / `UnClassCollection`
- [ ] 普通 / 高级拦截器及精确 order
- [ ] Query / Update / Delete / Aggregate / Bulk / Index
- [ ] Wrapper / Chain 的共享与复用状态
- [ ] 事务 / 多数据源 / 动态集合 / Registry
- [ ] Bean 多实例、静态链/缓存、ThreadLocal 清理
- [ ] 资源关闭、异常传播与并发首建
- [ ] 公开用户文档、`docs/ai`、BOM/starter
- [ ] 最小单元测试、容器测试、真实 MongoDB 组合测试

## 高频任务路由

| 修改任务 | 优先读取（1～3 份） |
|---|---|
| 新增 QueryWrapper 操作符 | [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md)、[architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 修改 updateFill | [features/AUTO_FILL.md](features/AUTO_FILL.md)、[architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 修复逻辑删除 | [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md)、[architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 新增 AdvancedInterceptor | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md)、[architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 新增配置项 | [CONFIGURATION.md](CONFIGURATION.md)、[architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)、[COMPATIBILITY.md](COMPATIBILITY.md) |
| 修改 `@MongoDs` | [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)、[architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| 修改事务 | [features/TRANSACTION.md](features/TRANSACTION.md)、[architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 修改聚合 lookup | [architecture/AGGREGATION.md](architecture/AGGREGATION.md)、[features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| 修改字段加密 | [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md)、[architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 修改自动索引 | [features/INDEX_AND_TIMESERIES.md](features/INDEX_AND_TIMESERIES.md)、[architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| 修改分片 | [features/SHARDING.md](features/SHARDING.md)、[features/TRANSACTION.md](features/TRANSACTION.md) |
| 修改备份恢复 | [features/BACKUP_AND_RESTORE.md](features/BACKUP_AND_RESTORE.md)、[features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
