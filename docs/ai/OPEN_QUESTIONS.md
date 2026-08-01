# 待验证问题

> 审计日期：2026-08-02。只记录已从当前源码观察到、尚缺行为验证的边界。除非状态明确写为“已确认缺陷”，这些条目都不是缺陷结论。统一状态值为：待验证 / 已确认行为 / 已确认缺陷 / 已解决。

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
| `Class<Map>` 与 `TypeReference<Map<...>>` | 待验证 | `Class<Map>` 路径退化为 `TypeReference<Map<String,Object>>`；显式 TypeReference 保留泛型 Type | 键/值嵌套类型、数字类型和实体值转换差异未行为验证 | 同一 Document 分别读取两种入口并比较运行时类型和值 | `architecture/ENTITY_MAPPING.md` | `AbstractMongoConverter.java:139-141`；`TypeReference.java` | core |

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
| Driver internal API 跨版本风险 | 待验证 | `FillField` 继承 `com.mongodb.internal.client.model.AbstractConstructibleBsonElement` | internal API 无稳定承诺，尚未在 Driver 4.x/多个 5.x 编译运行 | 建立 Driver 版本矩阵，编译并执行 `$fill` BSON/服务器行为测试 | `COMPATIBILITY.md`、`TESTING.md` | `aggregate/pipeline/FillField.java:3-17` | core |
| 核心执行路径缺少回归测试 | 待验证 | CodeGraph 对 Execute、CRUD、转换、拦截器等大量入口报告 no covering tests；reactor 测试目录为空 | 未检查外部 CI/外部测试仓库，不能断言项目完全无测试 | 将 CRUD、Wrapper、mapping、两类拦截器、事务的最小测试纳入 reactor 并在 CI 执行 | `TESTING.md`、`architecture/CRUD_EXECUTION.md` | `mapper/AbstractBaseMapper.java`；`execute/`；`mapping/`；`interceptor/` | 全部模块 |
