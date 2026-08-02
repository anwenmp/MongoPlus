# 待验证问题

## 字段加密、脱敏与敏感词（2026-08-02）

- **已确认缺陷：** decrypt 把注解 `publicKey` 作为第三参数；RSA/SM2 空值又回退全局 `publicKey`，内置路径不读取 `privateKey`；PBE decrypt 空 key 也不回退全局 key。
- **已确认安全缺陷 + 待运行影响：** 加解密异常被记录并返回原输入；写侧会把普通 String 原值继续放入 Document，读侧保留原始 Document 值。具体入口是否到达 Driver/数据库及日志后端渲染内容仍需覆盖坏 key/Base64/hex、长度、provider、拦截器与目标字段类型。
- **待验证：** AES/RSA 默认 transformation、RSA/SM2 相同明文随机性和长度边界、四种 PBE 在 JDK/provider 矩阵上的可用性；未运行前不承诺明文等值查询。
- **已确认安全缺陷：** LOCAL 敏感词 Handler 使用默认全激活并无条件返回原字段值；不只“未命中”，连没有 `@SensitiveWord` 的字段也会覆盖 TypeHandler/Encrypt/DBRef。启用 LOCAL 后加密字段会稳定把明文重新放入 Document。GLOBAL 则扫描最终 BSON/Document 的序列化文本，对已加密字段看到密文。
- **已确认缺口：** `ResultHandler.MASK` 未接入，`ignoreChar` boolean 未参与 builder；bulk 对非 InsertOne model 直接强转 UpdateMany。需覆盖全部 WriteModel、key 误命中和异常泄露。
- **待验证：** 第三方词库 Unicode/繁简/重叠词、动态词库并发与规模、重复上下文静态污染；Boot 3/4 显式依赖和 Solon 手工接入。
- **待验证：** 16 种脱敏对短值、非 String、容器、`CLEAR_TO_NULL` 的最终赋值，以及脱敏对象再次保存；TypeHandler/解密/脱敏/DBRef 全组合。

## 索引、时序集合与分片（2026-08-02）

- **待验证：** 自动 `createIndexes` 面对相同定义、同名不同 keys/options、同 keys 不同名称及批量部分失败时，不同 MongoDB Server 版本的准确错误、已创建状态和重试结果；MongoPlus 源码不做读取、比较、删除或补偿。
- **待验证：** 已有普通 collection、已有 options 不同的时序 collection、无权限或不支持时序的 Server 下，`AutoUtil` 吞掉 `MongoCommandException` 后的最终结构与启动外观；bucketMaxSpan/bucketRounding 组合及 metaField 被清空的 Server 版本行为。
- **高风险并发设计：** `DataSourceShardingInterceptor.sessionIsNotNull` 是 Boot 单例 Bean 上的普通 boolean，无 ThreadLocal、同步或 finally；它会切换到 `DefaultExecute` 并影响 session/执行器选择。需用双线程屏障固定交叉消费；运行前不写成必现缺陷。
- **已确认实现缺口 + 待集成验证：** 分片换源且目标配置 `replicaSet` 时会新建并启动 session，但没有把新状态加入 `ShardingTransactionContext`，当前 `SessionExecute` 又在路由前持有入口 session。需记录 Driver command session、commit/abort/close 次数与资源释放；commit/rollback/close 任一步异常还会中断 `HashMap.forEach`。源码不能支持“默认多数据源事务已闭合”的稳定承诺。
- **待验证：** 自定义分片策略返回 null、空、无匹配，position 为 0/负数，以及动态增加/移除数据源后 handler 匹配缓存的准确结果。

## 聚合与乐观锁（2026-08-02）

- 聚合：空 pipeline、null/custom stage 的 Driver 版本准确异常；无 match 时 Logic Delete 把 `$match` 追加到尾部是否为设计意图；Wrapper 重复执行被 Tenant/Logic 原地污染的兼容策略；lookup/facet/unionWith 子 pipeline 是否应递归增强；多个 match 是否应全部重复增强；Tenant 插入到 `$geoNear/$search/$vectorSearch` 之前及 Logic 追加到 `$out/$merge` 之后的 Driver/Server 准确异常。
- 聚合映射：多层泛型 lookup 数组、group `_id` 与别名、out/merge、cursor 消费和所有 AggregateOptions 的 Driver 兼容行为。`Class<Map>`/`TypeReference<Map<...>>` 的无限递归已确认为缺陷，不再仅列差异测试。
- 乐观锁：null/非 Integer 字段、多个 `@Version`、无 `$set`、retry 多次原地改写 BSON 的准确行为；matched=1/modified=0 是否应视为冲突；成功后是否应回写实体版本。用户 `$inc` 被顶层覆盖已是确认缺陷，不再仅列待验证。
- 组合：动态 namespace 为 `UnClassCollection` 时是否应继承原实体版本元数据；跨 datasource 同 namespace registry 冲突；分片 bulk 与事务回滚语义。LogicRemove/OptimisticLocker 的默认 order 与运行顺序已经确认，不再列注册顺序问题。

> 审计日期：2026-08-02。只记录已从当前源码观察到、尚缺行为验证的边界。除非状态明确写为“已确认缺陷”，这些条目都不是缺陷结论。统一状态值为：待验证 / 已确认行为 / 已确认缺陷 / 已解决。

## Tenant、Logic Delete、Auto Fill（2026-08-02）

- **已确认缺陷：** Tenant bulkWrite 的 UpdateMany 分支只修改临时 pair，不重建也不修改原 model，Tenant filter 不会回写；InsertOne 会原地写 Document，其余 model 未覆盖。仍需逐 model 集成测试固定最终命令。
- Tenant batch insert 以第一条 Document 的字段决定整批 `ignoreInsert`，混合有/无租户字段时的预期语义未定义。
- **待验证：** `TenantHandler` 返回 null/空字段名在 insert/filter/aggregate 的准确失败点，以及 Boot 3/4/Solon 多 Bean、重复初始化的实际容器行为。
- `@IgnoreTenant`/`@IgnoreLogic` 的嵌套调用、异步/子线程传播；Logic 使用 InheritableThreadLocal，Tenant 使用普通 ThreadLocal。
- **已确认缺陷：** `CollectionLogiceInterceptor.executeUpdate` 缺少 IgnoreLogic 判断，影响实体/Wrapper/BSON 的单条和多 pair update 过滤；不影响 bulk 的独立 Ignore 分支，也不影响高级删除转换的 Ignore 检查。修复前需明确兼容预期并补回归测试。
- **待验证：** Solon 启动只 `beanMake(MongoLogicIgnoreAspect.class)`，未见 `beanInterceptorAdd(IgnoreLogic.class, ...)`；需确认 `@IgnoreLogic` 是否实际绑定。若被全局挂接，还需验证其 `Optional` 返回包装和异常包装行为。Boot 3/4 的方法级切面绑定已由源码确认。
- 聚合没有 `$match` 时 Logic filter 被追加在 pipeline 末尾；对 group/project 后字段缺失的实际结果及预期顺序。
- 动态集合 delete 中普通 Logic 使用原 collection 元数据、高级 LogicRemove 使用替换后 collection，可能造成过滤与物理删除/逻辑更新分裂。
- **已确认行为：** LogicRemove(order `MAX_VALUE-1`) 默认先于 OptimisticLocker(order `MAX_VALUE`) 运行，并对内层 target 重调 executeUpdate；转换 `$set` 不含版本，乐观锁默认静默跳过，配置缺版本异常则删除失败。SessionExecute target 仍保留；真实事务/分片命令序列待集成验证。
- **已确认缺陷：** 乐观锁向 update 顶层 `putAll` 新 `$inc`，会覆盖用户整个原 `$inc`；非 Document/BSONObject Bson 的局部转换未写回 pair，改写可能丢失。各种 Driver Bson 实现与 retry 二次改写仍需参数化测试。
- **已确认行为 + 待验证设计：** Auto Fill 在实体字段映射后运行，fill Map 不再经过实体字段 TypeHandler/Encrypt/DBRef，重命名字段可能形成双字段；null、final/不可写字段和期望契约仍需测试/设计确认。
- Boot 3/4/Solon 多个 MetaObjectHandler 的最终选择依容器枚举最后项，顺序未形成契约；多应用上下文共享静态 HandlerCache 的污染需验证。

## 查询和 Wrapper

| 问题 | 状态 | 源码观察 | 暂不能下结论的原因 | 需要执行的验证 | 相关知识文档 | 源码位置 | 可能影响模块 |
|---|---|---|---|---|---|---|---|
| NOT 与 EXPR 共用 `Filters.expr` | 已解决 | 当前 `BuildCondition` 中 NOT 与 EXPR 是独立分支：NOT 调用 `Filters.not`/`Filters.nor`，EXPR 才调用 `Filters.expr`；该观察项不再符合当前源码 | 只能确认当前静态实现已分离；真实 MongoDB 命中语义仍需集成验证 | 保留 NOT/EXPR BSON 回归测试，并在真实 MongoDB 对照命中集 | `architecture/QUERY_WRAPPER.md`、`TESTING.md` | `mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:142-159` | core、所有 starter/plugin |
| NOT 多键只读取第一个键 | 已解决 | 当前 NOT 对单键使用 `Filters.not(notBasicDBObject)`，对多键把完整文档传给 `Filters.nor(notBasicDBObject)`；首键读取仅存在于后续 EXPR 分支 | 静态源码和工作区测试源码能确认不再丢弃后续键，但独立测试工程未纳入 reactor，本次也未执行其测试 | 将多键 NOT 测试纳入正式 reactor/CI，并在 MongoDB 验证 `$nor` 语义 | `architecture/QUERY_WRAPPER.md`、`TESTING.md` | `BuildCondition.java:142-159`；`BuildConditionNotTest.java` | core |
| RegexOptions 保存、读取与 BSON 写入 | 已确认行为 | `regex/like` 的字符串及 Lambda options 重载把枚举保存到 `ConditionMetaObject.extraValue`；共同 `REGEX/LIKE` 分支读取并把 flag 写入 `$options`。无 options 参数的重载先传 `CASE_INSENSITIVE`，显式 null 在构建器回退为 `i` | 源码链和现有 BSON 断言一致；不是“构建阶段未读取”，也没有证据表明 options 丢失 | 已完成定向源码追踪；待该测试模块纳入可运行 reactor 后持续回归 | `architecture/QUERY_WRAPPER.md` | `BaseQueryCondition.java:58-66,99-107`；`BuildCondition.java:99-107`；`Regex.java`；`Like.java`；`BuildConditionRegexTest.java` | annotation、core |
| RegexOptions 其余具体入口的运行覆盖 | 待验证 | 所有具体 Query/Update Wrapper 都继承同一 `Regex`/`Like` 默认方法和 `BuildCondition` 分支，源码层面均可传 options | 现有测试仅执行式描述了 `QueryWrapper` 与 `UpdateWrapper`；`LambdaQueryChainWrapper`、直接 `UpdateChainWrapper`、`LambdaUpdateChainWrapper`、`likeLeft/likeRight` 和全部枚举值尚无独立运行断言 | 增加参数化 BSON 测试覆盖这些具体类型与重载；必要时补真实 MongoDB 命中测试 | `architecture/QUERY_WRAPPER.md`、`TESTING.md` | `AbstractChainWrapper.java:22-23`；各 Query/Update Wrapper；`Regex.java`；`Like.java` | annotation、core |
| `regex/like(null)` | 待验证 | 构建阶段直接调用 `conditionMetaObject.getValue().toString()`，null 可触发 NPE；当前工作区已有测试源码断言构建期 NPE | 测试模块未进入根 reactor且默认跳过测试；本次未执行，变体也未全部覆盖 | 显式启用并运行现有测试，再补 `likeLeft/likeRight` 与 Lambda 变体并断言构建时机和异常类型 | `architecture/QUERY_WRAPPER.md`、`TESTING.md` | `BuildCondition.java:99-106`；`BuildConditionRegexTest.java` | core |
| `gt/ne/in` 等传 null | 待验证 | 比较操作把 null 传给 `Filters`；IN/NIN 强制转为 `Collection<?>` | Java 侧异常、BSON null 与 Driver/Server 语义因操作符不同 | 对每个操作符做 BSON 测试和真实查询，记录异常/命中集 | `architecture/QUERY_WRAPPER.md`、`COMPATIBILITY.md` | `BuildCondition.java:80-115`；`toolkit/Filters.java` | core、Driver 兼容 |
| 空集合行为 | 待验证 | IN/NIN 接收集合，逻辑 Wrapper 也可能构造空 BSON | Driver 编码与 Server 对 `$in:[]`、`$nin:[]`、空逻辑列表的语义未实测 | 覆盖空 IN/NIN/AND/OR/NOR、更新字段集合 | `architecture/QUERY_WRAPPER.md` | `BuildCondition.java:109-124`；`Filters.java` | core |
| 重复字段与 Wrapper 复用 | 待验证 | Wrapper 累积条件，构建结果写入 `BasicDBObject`/列表；同实例可重复 build/执行 | 覆盖、合并、残留与顺序尚无回归证据 | 同字段多操作符/同操作符，连续 build 和跨 CRUD 复用，比较 BSON | `architecture/QUERY_WRAPPER.md` | `conditions/AbstractChainWrapper.java`；`BuildCondition.java` | core |

## 映射

| 问题 | 状态 | 源码观察 | 暂不能下结论的原因 | 需要执行的验证 | 相关知识文档 | 源码位置 | 可能影响模块 |
|---|---|---|---|---|---|---|---|
| TypeHandler、加密、脱敏、DBRef 组合契约 | 待验证 | 这些能力分布在字段 Handler、转换器和 DBRefHandler，并非单一统一阶段 | 全组合优先级、重复转换和异常传播尚无测试 | 双向保存/读取矩阵，逐步启用及组合启用并记录调用顺序 | `architecture/ENTITY_MAPPING.md`、`architecture/EXTENSION_PIPELINE.md` | `AbstractMongoConverter.java`；`handlers/field/DBRefHandler.java`；相关字段 Handler | core、sensitive-word、starters |
| 多维集合与复杂反射 Type | 待验证 | TypeReference 仅经 `ClassTypeUtil.getClassFromType` 取得 class；转换路径处理集合/Map，但复杂 Type 的递归边界不清 | WildcardType、TypeVariable、GenericArrayType 与多维集合未见覆盖 | 为每种 Type 构造 Document→对象→Document 往返测试 | `architecture/ENTITY_MAPPING.md` | `mapping/TypeReference.java`；`AbstractMongoConverter.java`；`ClassTypeUtil.java` | core |
| 特殊集合具体实现反序列化 | 待验证 | converter 对 Collection 有通用分支，具体实现的实例化依赖类型工具/策略 | 不可变、排序、队列、自定义集合的选择和失败方式未实测 | 覆盖 Set/SortedSet/Queue/不可变及无无参构造集合 | `architecture/ENTITY_MAPPING.md` | `AbstractMongoConverter.java:139-143,280-288`；`ClassTypeUtil.java` | core |
| `Class<Map>` 与 `TypeReference<Map<...>>` | 已确认缺陷 | Mapper Class 重载先包装 TypeReference；`readInternal` 的 Map 分支再递归创建 `TypeReference<Map<String,Object>>`，raw class 仍为 Map，没有终止条件 | 静态控制流已闭合；运行测试只需固定最终 `StackOverflowError` 与受影响入口 | 覆盖 query/aggregate 的 Class Map 与 TypeReference Map；修复后再验证泛型值和 key 转换契约 | `architecture/ENTITY_MAPPING.md`、`architecture/AGGREGATION.md` | `AbstractBaseMapper.java:323-365`；`MongoConverter.java:146-149`；`AbstractMongoConverter.java:132-141`；`TypeReference.java` | core |

## 集合与多数据源

| 问题 | 状态 | 源码观察 | 暂不能下结论的原因 | 需要执行的验证 | 相关知识文档 | 源码位置 | 可能影响模块 |
|---|---|---|---|---|---|---|---|
| CollectionManager 仅以 collectionName 缓存 | 待验证 | `collectionMap` 键是 collectionName；`dsName` 仅在首次创建 MongoClient 时使用 | CollectionManager 的实际实例作用域可能提供额外隔离，尚未贯穿容器/多数据源验证 | 同一 manager 交替请求两个数据源同名集合，核对 namespace/client | `ARCHITECTURE.md`、`COMPATIBILITY.md` | `conn/CollectionManager.java:25,59-73` | core、sharding、starters |
| 跨数据源同名集合 | 待验证 | 后续同名请求直接返回首次缓存值 | 是否可由每数据源独立 manager 避免、分片路径是否共享尚未确认 | Boot/Solon/无容器分别搭两个数据源，交替读写同名集合 | `ARCHITECTURE.md`、`TESTING.md` | `CollectionManager.java:59-73`；`MongoClientFactory.java` | core、sharding、所有集成 |
| 动态集合首次切换后的 namespace 登记 | 待验证 | 仅首次创建集合时把 `mongoCollection.getNamespace().getFullName()` 与 clazz 登记 | 动态拦截器切换名称后实体登记是否更新、是否沿用首次 clazz 未实测 | 多实体、多动态名称交替访问并检查 registry 与映射结果 | `ARCHITECTURE.md`、`architecture/ENTITY_MAPPING.md` | `CollectionManager.java:62-70`；动态集合拦截器；`MongoEntityMappingRegistry.java` | core、starters/plugin |
| 动态集合与逻辑删除 | 待验证 | 动态集合属于普通拦截器路径，逻辑删除还依赖 namespace/实体元数据 | 两者顺序及切换后逻辑字段解析未形成稳定契约 | 对不同动态集合执行查询、更新、删除，检查最终 namespace 与逻辑条件 | `architecture/QUERY_WRAPPER.md`、`architecture/EXTENSION_PIPELINE.md` | 动态集合拦截器；logic 包；`CollectionManager.java` | core、sharding |
| `@MongoDs` 嵌套后不恢复外层 | 待验证 | Boot/Solon 均在进入时 set、finally 中直接 remove，没有栈；A→B 返回后源码会回退默认名称 | 尚无容器集成测试固定代理嵌套、自调用与异常路径 | Boot 3/4/Solon 分别覆盖 A→B→A、递归、自调用和异常，记录每次 Mapper 实际 client | `features/MULTI_DATASOURCE.md` | 各集成 `MongoDataSourceAspect`；`DataSourceNameCache.java` | core、所有集成 |
| Boot 空方法级 `@MongoDs` 且无类级注解 | 待验证 | `getMongoDsAnnotation` 因空 value 回退声明类；类上无注解时返回 null，调用方随后执行 `mongoDs.value()` | Spring AOP 暴露的准确异常包装和代理差异未运行验证 | Boot 3/4 分别调用空方法注解、无类注解的方法并固定异常类型；同时验证空方法值对非空类值的回退 | `features/MULTI_DATASOURCE.md` | Boot 3/4 `MongoDataSourceAspect.java:39-40,68-75` | Boot 3/4 starter |
| namespace→实体映射不含数据源 | 待验证 | registry key 是 `database.collection` 且 `putIfAbsent`，不含 datasource/client | 两数据源同 namespace 绑定不同实体时对逻辑删除/转换的实际影响未实测 | 两 client 使用相同 database/collection、不同实体，交替 CRUD 并检查 registry 和逻辑插件 | `features/MULTI_DATASOURCE.md`、`architecture/ENTITY_MAPPING.md` | `MongoEntityMappingRegistry.java`；`CollectionManager.java` | core、所有集成 |
| 动态覆盖数据源的旧资源 | 待验证 | `appendTempDataSource(..., true)` 替换 client 和 manager map，但不关闭旧 client；现有 collection 无失效通知 | 并发请求是否继续使用旧 collection、最终关闭覆盖 client 的责任尚无测试 | 覆盖前后保留 collection 引用并并发读写，统计 client close 与命令目标 | `features/MULTI_DATASOURCE.md` | `DataSourceManager.java`；`MongoClientFactory.java` | core、所有集成 |
| Lazy DataSource 配置未接入 | 待验证 | 找到 `lazyDataSource` 属性字段，但定向搜索未找到读取/分支/首次创建实现 | 可能存在索引遗漏或外部集成，本仓当前静态链不能证明该能力 | 各集成设置 true 启动，观察 client 构造与首次请求；确认配置元数据和文档契约 | `features/MULTI_DATASOURCE.md`、`architecture/STARTUP_LIFECYCLE.md` | 各集成 `MongoDBConfigurationProperty.java`；`MongoPlusConfiguration.java` | starters、Solon |
| 事务中切换数据源 | 待验证 | session 按事务开始时当前 client 创建；后续 collection 按最新 `DataSourceNameCache` 选择，工厂不校验二者同 client | Driver 的实际失败类型、Spring 事务切面顺序和嵌套语义未运行验证 | 对 `@MongoTransactional`、Spring `@Transactional`、手工 session 分别切换数据源并断言 client/session 绑定 | `features/MULTI_DATASOURCE.md`、`COMPATIBILITY.md` | `MongoTransactionalManager.java`；`ExecutorFactory.java`；各 `MongoDataSourceAspect` | core、Boot 3/4 |
| Solon `@MongoDs` 注册与能力边界 | 待验证 | 实现类只读方法注解，无类级/SpEL/handler；但 `XPluginAuto` 只为 `@MongoTransactional` 调用 `beanInterceptorAdd`，对 `@MongoDs` 仅创建 Bean，未见 `MongoDs.class` 到拦截器的绑定 | 需确认 Solon 是否存在仅凭 Bean 类型生效的约定；在注册链未确认前，不能把实现类分支当成已运行路由能力，也不能判定为缺陷 | 启动 Solon，先断言带 `@MongoDs` 方法是否进入拦截器，再覆盖方法/类/无注解/SpEL/handler/异常并记录返回值和清理行为 | `features/MULTI_DATASOURCE.md`、`architecture/STARTUP_LIFECYCLE.md` | Solon `MongoDataSourceAspect.java`；`XPluginAuto.java`；`MongoPlusConfiguration.java` | Solon plugin |

## 执行与兼容


| 问题 | 状态 | 源码观察 | 暂不能下结论的原因 | 需要执行的验证 | 相关知识文档 | 源码位置 | 可能影响模块 |
|---|---|---|---|---|---|---|---|
| SessionExecute count/estimated 语义差异 | 待验证 | DefaultExecute 的 estimated 调 `collection.estimatedDocumentCount()`；SessionExecute 同名方法调 `collection.countDocuments(clientSession)` | 名称、性能、一致性与事务可见性不同，但公共调用方期待尚未用 session 实测 | 同数据集在事务内外比较普通/session count 与 estimated，含未提交写入 | `architecture/CRUD_EXECUTION.md`、`COMPATIBILITY.md` | `DefaultExecute.java:102-104`；`SessionExecute.java:103-114`；`AbstractBaseMapper.java` | core、事务集成 |
| 事务完成后的 ClientSession 释放 | 已确认缺陷 | `closeSession` 仅在 `hasActiveTransaction()` 为 true 时调用 close；正常 commit/abort 后不 active，因此只 clear ThreadLocal 而不调用 `ClientSession.close()`；Spring 路径的 session A 也从未进入 close 生命周期 | “未显式关闭”可由源码确认；实际资源增长幅度仍无运行观测 | 监控 ClientSession，覆盖自有事务与 Boot 3/4 Spring 事务、commit/abort 失败及嵌套，断言 close 次数和上下文 | `features/TRANSACTION.md` | `MongoTransactionalManager.java`；Boot 3/4 `MongoPlusTransactionalManager.java` | core、所有事务集成 |
| Spring manager 创建两套 session | 已确认缺陷 | `doGetTransaction` 创建并返回 session A；`doBegin` 不把 A 传入 start，而是再次创建、启动并绑定 session B。A 无 close 路径；B 正常完成后也不 close | 两个 session 对象及关闭缺口可由源码确认；容器测试只用于固定次数和资源后果 | Boot 3/4 统计 startSession，覆盖 commit/rollback、manager 指定及与自有注解叠加 | `features/TRANSACTION.md` | Boot 3/4 `MongoPlusTransactionalManager.java`；`MongoTransactionalManager.java` | Boot 3/4 starter |
| 嵌套事务内层回滚后外层继续 | 已确认行为 | 内层 rollback 清零引用并 abort；finally clear 上下文，外层捕获后后续执行会因无状态改走 DefaultExecute；外层 commit/close 均因 status null而成为 no-op | 静态控制流已闭合；真实代理嵌套、Driver 命令序列和业务后果仍应测试 | 外层捕获后继续 CRUD，记录 session id、commit/abort/close 和命令 session | `features/TRANSACTION.md` | `TransactionHandler.java`；`MongoTransactionalManager.java`；`ExecutorFactory.java` | core、Boot 3/4、Solon |
| 动态集合登记 UnClassCollection 与逻辑能力 | 待验证 | 通常路径中动态拦截器用无实体重载首次登记 UnClassCollection，`putIfAbsent` 阻止后续真实实体覆盖；逻辑删除静态路径因此取不到原实体元数据 | registry 值与元数据选择是已确认行为；最终 BSON 和物理结果尚未测试 | 首次动态查询/更新/删除，检查 registry、最终 BSON 和物理结果；另测动态 namespace 预先由实体入口登记的相反顺序 | `features/DYNAMIC_COLLECTION.md`、`architecture/ENTITY_MAPPING.md` | `DynamicCollectionNameInterceptor.java`；`CollectionManager.java`；`LogicDeleteHandler.java` | core、所有集成 |
| 动态集合并发首次创建与缓存增长 | 待验证 | CollectionManager 使用 containsKey→open→put，非原子；动态名称无淘汰，数据源关闭也不清理 | 重复 wrapper 影响和长期资源占用未量化 | 并发同名和大量唯一名，统计 open/cache/registry；覆盖数据源覆盖与关闭 | `features/DYNAMIC_COLLECTION.md`、`features/MULTI_DATASOURCE.md` | `CollectionManager.java`；`MongoEntityMappingRegistry.java` | core、所有集成 |
| 动态集合与索引/时序/分片组合 | 待验证 | Handler 依赖普通参数策略，索引无 ExecuteMethodEnum；分片也替换 collection，时序创建位于独立生命周期 | 最终 namespace、插件顺序和动态结构未集成验证 | Boot 3/4/Solon 覆盖动态 CRUD 后索引、时序和分片路由 | `features/DYNAMIC_COLLECTION.md`、`architecture/EXTENSION_PIPELINE.md` | `ExecutorProxy.java`；`AbstractBaseIndex.java`；动态集合/分片拦截器 | core、sharding、所有集成 |
| Driver internal API 跨版本风险 | 待验证 | `FillField` 继承 `com.mongodb.internal.client.model.AbstractConstructibleBsonElement` | internal API 无稳定承诺，尚未在 Driver 4.x/多个 5.x 编译运行 | 建立 Driver 版本矩阵，编译并执行 `$fill` BSON/服务器行为测试 | `COMPATIBILITY.md`、`TESTING.md` | `aggregate/pipeline/FillField.java:3-17` | core |
| 核心执行路径缺少回归测试 | 待验证 | CodeGraph 对 Execute、CRUD、转换、拦截器等大量入口报告 no covering tests；reactor 测试目录为空 | 未检查外部 CI/外部测试仓库，不能断言项目完全无测试 | 将 CRUD、Wrapper、mapping、两类拦截器、事务的最小测试纳入 reactor 并在 CI 执行 | `TESTING.md`、`architecture/CRUD_EXECUTION.md` | `mapper/AbstractBaseMapper.java`；`execute/`；`mapping/`；`interceptor/` | 全部模块 |

## 命令监听与数据变更记录

| 问题 | 状态 | 源码观察 | 暂不能下结论的原因 | 需要执行的验证 | 相关知识文档 | 源码位置 | 可能影响模块 |
|---|---|---|---|---|---|---|---|
| Driver 命令回调线程、配对、重试/getMore/bulkWrite 事件数与 listener 异常后果 | 待验证 | MongoPlus 同步分发并重新抛出 listener `Exception`，无队列或隔离 | 最终调度与异常处理属于 Driver/Server 运行时 | 记录线程、requestId/operationId、started/终态，覆盖 retry、事务、getMore、bulkWrite、慢/异常 listener 与 close | `features/COMMAND_LISTENER.md`、`COMPATIBILITY.md` | `BaseListener.java`；`MongoPlusListener.java`；Driver 5.4.0 command event API | core、所有集成 |
| Recorder 在事务 commit/rollback、换源与分片中的记录归属 | 待验证 | 保存发生在普通 after、提交前；改写 datasource 且不恢复，无 afterCommit | session 与目标 client 是否一致、回滚结果依赖运行路径 | 覆盖普通/分片事务提交回滚、审计库同源/异源、动态集合与保存失败，核对集合和 session | `features/DATA_CHANGE_RECORDER.md`、`features/TRANSACTION.md`、`features/SHARDING.md` | `DataChangeRecorderInnerInterceptor.java`；`ExecutorFactory.java` | core、sharding、所有集成 |
| Recorder 递归、嵌套与 ThreadLocal 异常清理 | 已确认缺陷/后果待验证 | 保存目标默认会加入按 collection name 的忽略表，但用户覆盖列表可破坏防递归；单槽 ThreadLocal 只在保存成功后 remove，失败残留且 datasource 不恢复 | 污染持续时间、递归外观与线程复用后果无回归证据 | 构造嵌套 CRUD、移除保存目标忽略、Driver/保存异常和线程复用，断言覆盖、上下文与清理 | `features/DATA_CHANGE_RECORDER.md`、`TESTING.md` | `DataChangeRecorderInnerInterceptor.java` | core |
