# 逻辑删除

> 审计日期：2026-08-02。查询增强、删除转换与默认值填充是三段实现，不能概括为单个拦截器。执行链见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)。

## 元数据与注册

字段以 `@CollectionLogic` 标注，只允许 FIELD/ANNOTATION_TYPE。注解 `value`（未删除值）、`delval`（删除值）为空时回退全局 `LogicProperty`：默认 open=false、autoFill=false、删除值 `"1"`、未删除值 `"0"`、类型 `LogicDataType.DEFAULT`。实际类型转换由 `Configuration#setLogicFiled` 按 `LogicDataType` 和字段类型建立 `LogicDeleteResult`；多个逻辑字段的扫描选择顺序应由运行测试固定，不能作为 API 契约。

`Configuration.logic` 开启后注册普通 `CollectionLogiceInterceptor`，可选注册普通 `LogicAutoFillInterceptor`，并注册高级 `LogicRemoveInterceptor`。元数据保存在全局静态 `LogicManager.logicDeleteResultHashMap<Class<?>, LogicDeleteResult>`；按实体类缓存，未见隔离或自动清理。

## 查询和更新增强

| 操作 | 未删除条件 |
|---|---|
| query/page/count | 普通 `CollectionLogiceInterceptor` 按 collection→registry→实体元数据追加。 |
| aggregate | 每个已有 `$match` 用 `putIfAbsent` 追加；无 match 时把新 match 加到 pipeline **末尾**，不是首位。 |
| update one/many | 自动追加；当前实现未检查 `LogicManager.isIgnoreLogic()`，这是已确认缺陷。 |
| remove/delete | 普通阶段先追加未删除条件，高级阶段再转 update。 |
| bulkWrite | 仅重建 `UpdateManyModel` 并增强 filter；其他模型不处理。 |
| estimated count/index | 无增强；逻辑开启时 Mapper 避免 estimated count。 |

`doBsonLogicDel` 对 BasicDBObject/MPBson/BsonDocument 直接 `put/append` 同名字段，因此用户显式逻辑字段会被框架未删除值覆盖；null filter 则新建等值条件。Tenant order 0，Logic 默认最大 order，故内置排序链中先 Tenant 后 Logic，形成同一顶层文档的隐式 AND。复杂 `$or/$not/$expr` 不被重写，只在顶层追加。

## 删除转换

真实链路：用户 delete filter → 普通 Tenant → Dynamic Collection 替换 Execute 参数中的 collection → 普通 Collection Logic（仍按代理捕获的原 collection 查元数据）追加未删除 → 其他普通插件 → 高级 `LogicRemoveInterceptor`（从 invocation 取得替换后的 collection）→ `LogicRemove.logic` 构造 `{$set:{logicColumn:deleteValue}}` → 对当前高级 target 调用 `executeUpdate` → 剩余内层高级链/DefaultExecute 或 SessionExecute → Driver。

它不重新进入外层普通代理，所以 Tenant/Logic 不会第二次注入；也不走实体 `MongoConverter.writeByUpdate`，因此**不会调用 `MetaObjectHandler.updateFill`**，不会更新其他自动填充字段。返回值伪装为 `DeleteResult`，`getDeletedCount()` 实际返回 update 的 `modifiedCount`。已删除记录因普通阶段未删除条件而通常修改 0 条；忽略逻辑时直接物理 delete。没有专用恢复 API，但普通 update 可以自行改逻辑字段；该 update 默认又要求记录当前未删除。

SessionExecute 与普通路径共享外层代理和高级转换；`LogicRemove` 调用的内层 target 包裹同一个原始 SessionExecute，因此转换后的 update 仍调用带 session 的 Driver API，事务命令仍建议回归测试。默认乐观锁 order 为 `MAX_VALUE`，LogicRemove 为 `MAX_VALUE-1`：LogicRemove 先转 update，乐观锁随后进入，但 `$set` 不含版本，默认静默跳过；配置缺版本异常时删除失败。该组合不是同 order 注册顺序风险。

## `@IgnoreLogic`

注解允许 METHOD/TYPE，但 Boot 3/4 pointcut 仅为 `@annotation`，因此只匹配方法注解；类级声明虽可编译，当前 Spring 切面不会生效。必须经过容器代理，同类内部调用会绕过。切面/`withoutLogic` 使用 `InheritableThreadLocal<Boolean>` 并 finally remove；不是嵌套栈，子线程还可能继承忽略状态。

Solon 的 `MongoLogicIgnoreAspect` 实现也只读取 `inv.method()` 的方法注解，但 `XPluginAuto.start` 只 `beanMake(MongoLogicIgnoreAspect.class)`，没有像事务注解那样调用 `beanInterceptorAdd(IgnoreLogic.class, ...)`。仅凭当前仓库源码不能确认该 Interceptor Bean 会自动绑定到 `@IgnoreLogic`；所以不能写成 Boot 3、Boot 4、Solon 已确认一致，Solon 是否实际进入该拦截器必须运行启动测试。其实现还在无方法注解时返回 `Optional.empty()`、有注解时用 `Optional.map` 包装结果，并把被调用异常包成 `RuntimeException`；若它被全局挂接，返回/异常语义也需测试。

Spring Logic aspect `@Order(0)`，Tenant aspect `@Order(1)`；同时标注时 Logic 外层先设上下文，Tenant 内层再设。两者相互独立。已确认缺陷：`executeUpdate(MutablePair)` 未检查 Ignore，list update 逐项调用它，所以所有进入普通 update 参数策略的入口（updateById、Entity + Wrapper、纯 Wrapper/BSON 的 update one/many，以及直接 Execute list/single update）仍会追加未删除条件。bulkWrite 有独立 Ignore 检查，不受此缺陷影响。

该缺陷只影响 **update 的逻辑删除过滤**。query/count/aggregate/bulk/remove 普通分支都检查 Ignore；高级 `LogicRemove.logic` 也检查 Ignore，Ignore 为 true 时继续原 delete，因此已生效的 `@IgnoreLogic`/`withoutLogic` 删除路径会物理删除，而不是仍被转换为逻辑 update。Boot 3/4 可由切面绑定和控制流确认；Solon 要先验证切面是否实际绑定。

## Registry、动态集合与无实体模式

`LogicDeleteHandler.getBeanClass` 用 `database.collection` 查 `MongoEntityMappingRegistry`，不含 datasource；缺元数据时按类延迟初始化。动态 collection 通常由无实体重载首次登记 `UnClassCollection`，`putIfAbsent` 阻止原实体覆盖，因而取不到原实体逻辑字段；Map/Document 显式模式同样登记 UnClassCollection。若该 namespace 更早由真实实体登记，则首次实体保留。跨数据源相同 namespace 会共享首次登记结果。

动态拦截器虽先于 Logic order 执行，但普通代理给 Logic 专用策略的 collection 仍是原 collection；高级 `LogicRemove` 从 invocation 取的是替换后的 collection。于是删除路径可能出现“普通过滤按原 namespace 元数据、高级转换按动态 namespace registry”的分裂，必须以组合测试固定。详见 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md) 与 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)。registry remove/clear 没有生产调用方，且不会清 collection cache。

## 风险与测试

已确认缺陷/边界：IgnoreLogic 不作用于普通 update 分支；aggregate 无 match 时过滤放末尾，可能改变 `$group` 等 pipeline 的语义甚至字段可用性；bulk 只覆盖 UpdateMany；逻辑删除不触发 updateFill；静态 HashMap/全局开关、多上下文污染；Boot 3/4 类级 Ignore 注解不被切面匹配。Solon Ignore 的注解绑定未在启动源码中找到，保持待运行验证，不与 Spring 结论合并。

至少验证字段类型/默认值/多个字段、query/page/count/aggregate、update 已删除记录、逻辑与物理删除、重复删除和返回数、IgnoreLogic 方法/类/内部调用、Tenant 顺序、所有 bulk model、动态集合/跨数据源 registry、Map/Document、事务、乐观锁、唯一索引和分片。测试策略见 [TESTING.md](../TESTING.md)，未决项见 [OPEN_QUESTIONS.md](../OPEN_QUESTIONS.md)。

## 关键源码

- `annotation/collection/CollectionLogic.java`、`annotation/logice/IgnoreLogic.java`
- `logic/LogicDeleteHandler.java`、`LogicRemove.java`、`manager/LogicManager.java`
- `interceptor/business/CollectionLogiceInterceptor.java`、`LogicAutoFillInterceptor.java`、`LogicRemoveInterceptor.java`
- `config/Configuration.java#setLogicFiled`、`registry/MongoEntityMappingRegistry.java`
