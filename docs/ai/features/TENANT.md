# 多租户

> 审计日期：2026-08-04。结论来自当前 `mongo-plus-core`、Boot 3/4 starter 与 Solon plugin 源码。执行代理见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)，条件构造见 [QUERY_WRAPPER.md](../architecture/QUERY_WRAPPER.md)。

## 公开入口与注册

`TenantHandler` 是唯一策略入口：`getTenantId()` 提供 `BsonValue`，`getTenantIdColumn()` 默认 `tenant_id`，并可按 collection、database、当前 datasource 忽略。框架没有默认 Handler；没有用户 Bean 就不注册租户能力。

Boot 3、Boot 4 都用 `ApplicationContext.getBean(TenantHandler.class)`，Solon 用 `AppContext.getBean`；异常被吞掉，成功后通过单个注册入口向全局静态 `InterceptorChain` 追加一个 `TenantInterceptor`。多 Bean 导致的选择/异常没有框架级合并规则；重复初始化会重复追加，链不去重。`TenantInterceptor.order()` 明确返回 0，属于普通拦截器，不是高级链或独立处理链。`Configuration.tenantHandler(...)` 也是单个注册入口。

三种容器都会先批量加入用户 `Interceptor` Bean，并在该启动方法中显式排序；随后内置 Tenant、Dynamic Collection 分别用单个注册入口加入，每次加入后再次排序。Boot 3/4 的调用顺序是用户链 → Tenant → Dynamic，Solon 是用户链 → Dynamic → Tenant；由于 order 分别是 Tenant 0、Dynamic 2，当前最终顺序仍是 Tenant 在前。core 的 `addInterceptors(List)` **自身不排序**，只有容器调用方随后显式 `sorted()`；不能把任意批量注册都描述为自动排序。

`Stream.sorted` 当前是稳定排序，同 order 元素在一次排序中保留进入列表时的相对顺序；但 Bean 枚举顺序、跨应用上下文静态累积顺序、用户调用 `addInterceptors` 后是否补排序都不是 MongoPlus 的公开稳定契约。因此“同 order 当前通常按插入顺序”是实现行为，不是可依赖的插件优先级保证。

## 条件与写入

| 操作 | 已确认行为 |
|---|---|
| save/insert one、many | Entity/Map 已先转换为 `Document`；`putIfAbsent(column, getTenantId())`。默认 `ignoreInsert` 在文档已有租户 key 时跳过整次写入，因此用户值保留；批量判断只取第一条文档的 key 集合。 |
| query/page | 在现有 BSON 顶层缺少租户 key 时原地追加 `column: BsonValue`。page 复用 query/count。 |
| update one/many | 只增强 filter；不检查也不限制 update document 修改租户字段。upsert 只依赖 filter，框架不另写 `$setOnInsert`。 |
| remove/delete | 增强 delete filter；若逻辑删除启用，增强后的 filter 随后在高级链转 update。 |
| count | 增强 filter；`estimatedDocumentCount` 没有参数策略且 Mapper 在有租户拦截器时禁止走快速估算。 |
| aggregate | 已有 `$match` 时给每一个 match document `putIfAbsent` 租户 key；否则在 pipeline 首位插入 match。 |
| bulkWrite | InsertOne 原地增强其 Document。UpdateMany 对 BSON update 与 pipeline update 都重建 model，把增强后的 filter 写入返回列表，并保留原顺序、update/pipeline 引用和 options；UpdateOne/DeleteOne/DeleteMany/ReplaceOne 仍不处理。 |
| replace、index | 没有租户专用证据；索引不进入普通参数策略。 |

`appendTenantFilter` 只检查 BSON 文档顶层是否已有租户 key。用户显式顶层条件优先，框架既不覆盖也不校验值；租户条件藏在 `$and`、`$or`、`$not`、`$expr` 内时仍会再追加顶层条件，因此最终语义是与整个用户表达式做隐式 AND，而不是改写内部节点。重复经过拦截器时，首次顶层 key 会阻止再次追加；多个不同 `TenantInterceptor` 使用不同列则都会追加。对于非 `Document/BSONObject` 的 Driver BSON，`BsonUtil.addToMap` 会产生新的 `BasicDBObject`，当前实现会接住并返回该对象，不再丢失转换结果。

Handler 在每次受支持操作的普通参数策略阶段调用。`getTenantId()` 返回 Java null 时，insert 的 `Document.putIfAbsent` 可保存 null；filter/aggregate 路径要求 `BsonValue`，后续 BSON 构造或编码的准确失败点需运行验证。Handler 抛异常直接中止，普通 after 不运行。字段名 null 时 Document/BsonDocument 路径可能失败，空字符串则继续下传；框架没有统一校验，准确异常需按操作与 Driver 版本测试。

## `@IgnoreTenant`

注解只允许方法级。Boot 3/4 的 `TenantAspect` 为 Spring AOP `@Around("@annotation(...)")`、`@Order(1)`；Solon 拦截器也只读取 `inv.method()` 的方法注解。不存在类级或 Mapper 接口级优先级。必须经过相应容器代理；同类内部调用、未被 AOP 管理的 Service/Repository/Mapper/Chain 调用不会自动生效。

切面进入时设置 `TenantManager` 的普通 `ThreadLocal<Boolean>`，`finally` remove，异常后会清理；也可使用 `withoutTenant`。它不是可嵌套计数/栈：内层调用结束会 remove 外层状态。`@IgnoreLogic` 使用另一 ThreadLocal；Spring 中 Logic aspect order 0、Tenant aspect order 1，二者同时标注时 Logic 外、Tenant 内，但两者独立，正常与异常路径均 finally 清理。Solon 注册先后见下节，源码未声明等价的 order 契约。

## 与其他能力的真实顺序

实体保存先完成 ID、字段映射、Auto Fill，再进入普通链；Tenant 在执行代理内给最终 Document `putIfAbsent`，所以租户字段不是 `MetaObjectHandler` 写入，也不会再经过 TypeHandler、加密或 DBRef。

普通链逐插件执行 `beforeExecute` 后立刻应用该插件的参数策略。内置 order 为 Tenant 0、Dynamic Collection 2、Collection Logic/Logic Auto Fill 默认最大值，因此当前已排序的内置链为 Tenant → Dynamic → Logic。Tenant 与 Logic 都是普通参数增强；逻辑 remove→update 另在高级 `LogicRemoveInterceptor`。用户 Bean、单个注册和容器批量注册在三种集成的正常启动路径最终都会排序；但 core 批量 API 本身不排序，同 order 只保留当时插入顺序，均不能视为跨注册方式/上下文的稳定契约。

动态集合替换 args 最后一项，但 `ExecutorProxy` 传给同次各专用策略的局部 collection 仍是原 collection；Tenant/Logic 的 ignore/registry 判断通常观察原 namespace，高级链和 Driver看到替换后的 collection。多数据源由取得原 collection 前的上下文决定；事务执行器也在动态替换前选择。详见 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md)、[MULTI_DATASOURCE.md](MULTI_DATASOURCE.md)、[TRANSACTION.md](TRANSACTION.md)。

Map/Document 插入没有实体 Auto Fill/字段注解，但 Tenant 仍可对最终 Document 写租户字段；显式 namespace 查询/update 同样可增强。索引不增强。

## 风险与最低测试

已确认缺口：bulk insert 仅覆盖 InsertOne，bulk update 仅覆盖 UpdateMany，其余 model 不覆盖；batch insert 以首文档决定全部是否跳过；update 可主动修改租户列；无租户值/列校验；ThreadLocal 忽略不支持嵌套恢复；全局链重复注册不去重。UpdateMany filter 未写回已于 2026-08-04 修复。

独立 `mongo-plus-test` 的 `TenantInterceptorBulkWriteTest` 覆盖 BSON update 与 pipeline update：确认重建后的 model 带 tenant filter，并保持混合 model 顺序、原 update/pipeline 和 `UpdateOptions`；2 项已运行通过。测试直接调用拦截器且不连接 MongoDB，ExecutorProxy 参数回写、Default/Session Driver 命令和其他 bulk model 仍属于集成验证范围。

至少验证 query、insert/batch、update/delete/count/aggregate、upsert/replace、各种 bulk model、显式租户条件及 AND/OR/NOT/EXPR、null Handler、IgnoreTenant 代理边界/嵌套/并发、Map/Document、动态集合、多数据源、事务、分片，以及 Boot 3/4/Solon 多 Bean和重复初始化。测试策略见 [TESTING.md](../TESTING.md)，未决项见 [OPEN_QUESTIONS.md](../OPEN_QUESTIONS.md)。

## 关键源码

- `handlers/TenantHandler.java`、`manager/TenantManager.java`、`interceptor/business/TenantInterceptor.java`
- Boot 3/4/Solon 的 `config/MongoPlusAutoConfiguration.java#setTenantHandler` 与 `tenant/TenantAspect.java`
- `proxy/ExecutorProxy.java`、`interceptor/InterceptorChain.java`
