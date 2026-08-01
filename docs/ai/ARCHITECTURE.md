# 全局架构

> 审计日期：2026-08-01。以下只保留已由 CodeGraph 定位并由入口源码或 POM 复核的总体关系，不展开每个 CRUD 的易变细节。

## 分层与用户入口

1. `mongo-plus-annotation` 提供集合、字段、Mapper、事务等注解和相关枚举。
2. `mongo-plus-core` 提供用户 API：`BaseMapper`、`MongoMapper`、`IRepository`、`IService`，以及 Wrapper、Aggregate 和 Index API。
3. core 内部由映射转换、字段处理器、监听器和两类拦截器提供横切能力。
4. `AbstractBaseMapper`、`ExecutorFactory` 与 `Execute` 实现负责执行；`MongoPlusClient`、`CollectionManager` 和 `MongoClientFactory` 管理 Driver 资源。
5. 最终 I/O 由 MongoDB Java Driver 的 `MongoCollection<Document>`、`MongoDatabase` 和 `MongoClient` 完成。
6. Boot 3、Boot 4、Solon 负责容器启动；sharding 和 sensitive-word 是可选扩展。

`IService<T>` 本身只继承 `IRepository<T>`，默认实现位于 `ServiceImpl`/`RepositoryImpl`。链式构造的工具入口类名是 [`ChainWrappers`](../../mongo-plus-core/src/main/java/com/mongoplus/toolkit/ChainWrappers.java)，具体类型包括 `QueryChainWrapper`、`UpdateChainWrapper`、Lambda 变体和聚合链；不存在名为 `ChainWrapper` 的单一入口类。

## CRUD 到 Driver 的总体路径

```text
IService / IRepository / BaseMapper / MongoMapper proxy
    -> DefaultBaseMapperImpl
    -> AbstractBaseMapper
       -> Wrapper/Aggregate 生成查询、更新或管道所需 BSON
       -> MongoConverter 完成实体与 Document/目标类型转换
       -> MongoPlusClient -> CollectionManager -> ConnectMongoDB
       -> ExecutorFactory 提供被代理的 Execute
       -> DefaultExecute
       -> MongoCollection<Document> 的 insert/find/update/delete/aggregate 等 Driver API
```

已复核的关键事实：

- `DefaultBaseMapperImpl` 继承 `AbstractBaseMapper`；前者补齐基于实体类型的数据库/集合解析，后者持有 `MongoPlusClient`、`MongoConverter` 和 `Execute`。
- `AnnotationOperate` 从 `@CollectionName` 或类名规则解析数据库/集合名；`CollectionManager` 缓存 `MongoCollection<Document>`，首次创建时通过 `ConnectMongoDB.open()` 调用 `MongoDatabase.getCollection(...)`，并写入 `MongoEntityMappingRegistry` 的命名空间到实体映射。
- `MongoConverter` 提供保存、更新和读取方向的实体/`Document` 转换。
- `DefaultExecute` 直接调用 `MongoCollection` 的 `insertOne/insertMany/find/updateOne/updateMany/deleteOne/deleteMany/aggregate` 等方法。

精确到每个重载的调用链、字段处理顺序和异常语义仍待专题验证。

## Wrapper、映射与拦截器

- Wrapper 保存结构化条件；Mapper 执行时将其转换为 BSON。Wrapper 不负责取得连接。
- 元数据并非一个统一的“EntityClassCache”。当前已确认的相关入口是 `AnnotationOperate`（集合/数据库注解解析）、`MongoEntityMappingRegistry`（集合命名空间与实体映射）和 `MongoConverter`（字段/类型转换）。
- Wrapper 的真实类型关系、操作符 BSON、逻辑分组及租户/逻辑删除/动态集合边界见 [`architecture/QUERY_WRAPPER.md`](architecture/QUERY_WRAPPER.md)。
- 实体与 Document 双向路径、TypeHandler/MappingStrategy/ConversionStrategy 优先级及 Map 模式差异见 [`architecture/ENTITY_MAPPING.md`](architecture/ENTITY_MAPPING.md)。
- `InterceptorChain` 保存普通 `Interceptor`；`ExecutorProxy` 在方法调用层使用该链及执行策略。
- `AdvancedInterceptorChain` 将 `AdvancedInterceptor` 逐层代理到 `Execute` 上，`ExecutorFactory` 返回包装后的执行器。两条链不能合并描述为同一个阶段。
- 字段 Handler、Listener 与两类执行拦截器不是同一条链；已核实的阶段边界和当前实现顺序见 [`architecture/EXTENSION_PIPELINE.md`](architecture/EXTENSION_PIPELINE.md)。未被源码固定的同 order、跨容器注册及分片/异步组合顺序不作稳定承诺。

## 启动入口

### 无容器

[`Configuration`](../../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java) 是 core 的装配入口，构造 `MongoPlusClient`、`BaseMapper`、转换器、执行器和扩展链。

### Spring Boot 3 / 4

两个 starter 的 `AutoConfiguration.imports` 当前均列出 9 个配置类，包括 `MongoPlusConfiguration`、`MongoPlusAutoConfiguration`、覆盖配置、属性、事务和可选敏感词配置。`MongoPlusConfiguration` 提供核心 Bean；Mapper 扫描器把接口 BeanDefinition 改为 `MongoMapperFactoryBean`，后者从容器取得 `BaseMapper` 并调用 `MapperProxy.wrap(...)`。Boot 3 与 Boot 4 是平行源码模块，不是同一构件的 profile。

### Solon

`META-INF/solon/anwen.mongo.config.properties` 声明 `solon.plugin=com.mongoplus.config.XPluginAuto`。`XPluginAuto.start(...)` 创建 `MongoPlusConfiguration` 和属性/切面 Bean，并注册 Mapper 注入器；符合条件的接口通过 `MapperProxy.wrap(baseMapper, mapperInterface)` 注入。

## 模块依赖边界

依赖方向以 [`MODULES.md`](MODULES.md) 和各模块 POM 为准。核心约束是 annotation/Driver 基础被 core 使用，容器与可选扩展依赖 core；core 不依赖 Boot、Solon、sharding 或 sensitive-word。
