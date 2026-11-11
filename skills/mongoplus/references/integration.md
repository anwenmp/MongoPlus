# 模块选择、初始化与容器集成

本Skill以当前源码POM `2.2.0` 为基线；这不表示官网示例或Maven Central中任意同名版本具有当前工作树的全部方法。先核对目标应用实际依赖。完整模块盘点在[模块审计](../audit/modules.json)，全部能力在[能力目录](../capabilities.md)。

| 模块 | 用途与接入条件 | 边界 |
|---|---|---|
| mongo-plus-annotation | 注解、枚举和注解依赖类型 | 只有注解不产生执行能力 |
| mongo-plus-core | 独立Java、Mapper、条件、聚合、映射、管理及扩展链 | 不要求Spring、Solon、MCP或IDE |
| mongo-plus-boot-starter | Boot 3自动配置、Mapper代理、AOP、事务 | 对应Boot 3的Spring依赖图 |
| mongo-plus-boot4-starter | Boot 4适配 | 与Boot 3包名有重叠，不能两套同时引入 |
| mongo-plus-solon-plugin | Solon Plugin、Bean和接口注入 | 不推定所有Spring切面在Solon等价接线 |
| mongo-plus-sharding | 数据源分片策略、路由、事务上下文 | 可选模块 |
| mongo-plus-sharding-boot-starter | 分片Boot 3注册 | 当前无Boot 4专用分片Starter |
| mongo-plus-sensitive-word | 敏感词注解、词库、字段/全局处理 | core中的配置模型不等于已安装此模块 |
| mongo-plus-bom | 依赖版本管理 | 不在当前根reactor内；不产生运行代码 |
| mongo-plus-indexer | 仓库内源码索引开发工具 | 当前独立POM、Java 17，不在根reactor；应用无需依赖 |
| tools/mongoplus-api-indexer | 独立JavaParser/Jackson声明索引工具 | POM 0.1.0、Java 17，不在框架reactor；不作为运行依赖 |
| mongo-plus-test / test-boot3 / test-boot4 / test-solon | 仓库验证工程 | 不作为用户运行依赖或Skill前置条件 |

Core使用公开`com.mongoplus.config.Configuration`构造配置，通过`getBaseMapper()`取得非实体绑定的BaseMapper。关闭应用时按客户端所有权关闭MongoClient；`Configuration`也会修改若干全局缓存，不能把每次builder视为隔离的容器。

```java
import com.mongoplus.config.Configuration;
import com.mongoplus.mapper.BaseMapper;

class CoreSetup {
    static BaseMapper create(String uri, String database) {
        return Configuration.builder().connection(uri).database(database)
            .banner(false).getBaseMapper();
    }
}
```

Boot中用户接口直接继承`MongoMapper<Order>`，使用`@Mongo`及`@MongoMapperScan`扫描；不要凭MyBatis经验添加不存在的`BaseMapper<Order>`泛型。`IRepository<T>`、`IService<T>`不是按实体自动生成的Bean；自定义实现继承`RepositoryImpl<T>`/`ServiceImpl<T>`并由容器注册。参见[注解接入记录](../annotations/integration.md)和[CRUD](crud.md)。

Solon的`XPluginAuto`绑定了`MongoTransactional`拦截器，并为`@Inject MongoMapper<T>`创建代理。源码中创建`MongoDataSourceAspect`、`TenantAspect`、`MongoLogicIgnoreAspect`的Bean不等于这些注解已经通过`beanInterceptorAdd`绑定；未实际启动验证的组合标`UNVERIFIED`。

Boot3的`MongoPlusAutoConfiguration.init()`最后收集自定义ReadHandler Bean；当前Boot4与Solon没有对应收集步骤。需要跨容器一致时显式注册到`HandlerCache.addReadHandler`，不能把缺少接线说成注解本身不存在。FieldHandler应通过`FieldHandlerChain`注册，不能假设声明任意Handler Bean就会自动生效。注册细节见[扩展体系](extensions.md)。

依据：各模块POM、Starter `MongoPlusConfiguration` / `MongoPlusAutoConfiguration` / `MongoMapperScannerRegistrar`、Solon `XPluginAuto`，精确源码链接由[公开类型矩阵](../audit/public-types.md)逐项提供。[官方快速入门](https://mongoplus.com/docs/started.html)只作补充，源码差异保留在[验证与限制](verification.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C001 接入Core与独立Java**：`Configuration`。
- **C002 Spring Boot 3接入与Mapper扫描**：`MongoMapperScan`, `MongoMapperFactoryBean`。
- **C003 Spring Boot 4接入与差异**：`MongoMapperScan`, `MongoPlusAutoConfiguration`。
- **C004 Solon接入与代理注入**：`XPluginAuto`, `CollectionScanner`。
- **C005 BOM与版本选择**：`@bom`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
