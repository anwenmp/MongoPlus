# Maven 模块

> 审计日期：2026-08-01。模块与依赖以根 POM 和各模块 POM 为准；职责以公开入口源码为准。

## Reactor 与仓库内发布构件

根 [`pom.xml`](../../pom.xml) 是 `com.mongoplus:mongo-plus:2.2.0`、`packaging=pom` 的聚合父 POM，不是运行时库。其 `<modules>` 明确聚合 8 个模块：

| Reactor 模块 | 类型 | 直接项目内依赖 | 经源码确认的主要职责 |
|---|---|---|---|
| `mongo-plus-annotation` | JAR | 无 | 公共注解、枚举和注解相关基础类型 |
| `mongo-plus-core` | JAR | `mongo-plus-annotation` | Mapper/Repository/Service、条件与聚合、映射转换、执行器、连接管理和扩展链 |
| `mongo-plus-boot-starter` | JAR / Boot 3 适配 | `mongo-plus-core`；`mongo-plus-sensitive-word` 为 `provided` | Boot 自动配置、属性、Mapper 扫描与代理、事务和切面集成 |
| `mongo-plus-boot4-starter` | JAR / Boot 4 适配 | `mongo-plus-core`；`mongo-plus-sensitive-word` 为 `provided` | 与 Boot 3 模块平行的 Boot 4 自动配置与 Mapper 集成 |
| `mongo-plus-solon-plugin` | JAR / Solon 插件 | `mongo-plus-core` | Solon 插件启动、核心 Bean、切面和 Mapper 注入 |
| `mongo-plus-sharding` | JAR / 可选扩展 | `mongo-plus-core` | 数据源分片策略、处理器、拦截器及分片事务衔接 |
| `mongo-plus-sharding-boot-starter` | JAR / Boot 3 Starter | `mongo-plus-boot-starter`、`mongo-plus-sharding` | 将分片能力注册到 Boot 3 容器 |
| `mongo-plus-sensitive-word` | JAR / 可选扩展 | `mongo-plus-core` 为 `provided` | 敏感词注解、字段处理器、拦截器与管理器 |

仓库还包含 [`mongo-plus-bom/pom.xml`](../../mongo-plus-bom/pom.xml)。它是独立的 `packaging=pom` BOM，在 `dependencyManagement` 中锁定上述 8 个发布构件的版本；它不在根 `<modules>` 中，因此不是当前 reactor 的第 9 个模块。根 POM 反向导入了该 BOM。发布流程为何这样组织，待验证。

## 依赖方向

```text
mongo-plus-annotation <- mongo-plus-core <- mongo-plus-boot-starter <- mongo-plus-sharding-boot-starter
                                      ^             ^
                                      |             +-- mongo-plus-sharding
                                      +-- mongo-plus-boot4-starter
                                      +-- mongo-plus-solon-plugin
                                      +-- mongo-plus-sharding
                                      +-- mongo-plus-sensitive-word (core 为 provided)

mongo-plus-sensitive-word --provided--> boot-starter / boot4-starter
```

箭头指向被依赖方。`mongo-plus-core` 的 POM 不依赖 Spring Boot 或 Solon；容器适配没有反向进入 core。Boot 4 当前没有对应的 sharding starter。

## 关键源码依据

- Core 用户 API：[`BaseMapper`](../../mongo-plus-core/src/main/java/com/mongoplus/mapper/BaseMapper.java)、[`IService`](../../mongo-plus-core/src/main/java/com/mongoplus/service/IService.java)、[`ChainWrappers`](../../mongo-plus-core/src/main/java/com/mongoplus/toolkit/ChainWrappers.java)。
- Boot 3/4：各自的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`、`MongoPlusConfiguration`、`MongoPlusAutoConfiguration` 和 `MongoMapperFactoryBean`。
- Solon：[`anwen.mongo.config.properties`](../../mongo-plus-solon-plugin/src/main/resources/META-INF/solon/anwen.mongo.config.properties) 与 [`XPluginAuto`](../../mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/XPluginAuto.java)。
- 分片：[`DataSourceShardingInterceptor`](../../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java)、`DataSourceShardingStrategy`、`ShardingTransactionalHandler`；Boot 注册入口为 `MongoShardingConfiguration`。
- 敏感词：`SensitiveWord`、`SensitiveWordInterceptor`、`SensitiveWordFieldHandler`、`SensitiveWordManager`。

## 验证边界

CodeGraph（索引日期状态见 `CURRENT_STATE.md`）未找到 `org.junit`、`BaseMapperTest` 或 `IServiceTest`，并对核心入口报告无覆盖测试。因此本文件确认的是 POM 依赖和源码入口，不宣称各模块已通过独立或集成测试。
