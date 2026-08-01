# MongoPlus 项目定位

> 审计日期：2026-08-01。项目目标与已实现入口分开记录；README 宣传语不能单独作为实现证据。

## 项目目标

MongoPlus 面向 Java 应用，在 MongoDB Java Driver 之上提供接近 MyBatis-Plus 使用习惯的对象化数据访问 API，减少常见 CRUD、条件 BSON 和实体映射的重复代码。它不是 MongoDB 服务端、代理或 Java Driver 的替代品，也不承诺复制 MyBatis-Plus 的全部语义。

该定位由根 POM/模块划分以及 core 的 [`BaseMapper`](../../mongo-plus-core/src/main/java/com/mongoplus/mapper/BaseMapper.java)、[`IService`](../../mongo-plus-core/src/main/java/com/mongoplus/service/IService.java)、`IRepository`、`MongoMapper` 和 Wrapper API 支持。

## 已有实现入口

以下表示仓库中存在可追踪的公开 API 与执行实现，不等于所有边界均有自动化测试：

- 实体 CRUD、批量写、分页，以及 Mapper/Repository/Service API。
- Query/Update Wrapper、Lambda 与链式入口；聚合 API。
- 实体与 `Document` 转换、集合/数据库注解解析、无实体集合入口。
- 索引、时序集合相关入口。
- 逻辑删除、自动填充、租户、动态集合、多数据源和事务相关实现入口。
- 普通/高级拦截器、Listener、字段读写 Handler、加密与脱敏相关实现入口。
- Boot 3、Boot 4 与 Solon 的独立容器适配。
- 独立的 sharding 与 sensitive-word 可选模块。

证据入口：core 的 `mapper/repository/service`、`conditions`、`aggregate`、`mapping`、`index`、`interceptor`、`handlers`、`transactional` 包；各容器的自动配置/插件入口；分片和敏感词模块的 POM 与入口类。

## 可选能力与验证成熟度

- sharding、sensitive-word、Boot/Solon 适配是已存在的可选模块，不属于 core 的必选依赖。
- 当前 CodeGraph 未找到 JUnit 或核心 API 覆盖测试；因此不能把“存在实现”写成“已充分验证兼容性”。
- Boot 4 模块虽已实现，但根编译目标为 Java 8，而其实际运行 JDK 下限及与所有功能组合的构建矩阵尚未验证。
- MongoDB Server 版本矩阵、各容器版本组合、分片事务边界、扩展执行顺序等均需后续专项验证。

## 实验功能与计划功能

当前 POM、已读取入口源码和这 5 份知识文件没有给出可可靠归类为“实验功能”的明确标记，也没有可确认的功能路线图。因此不把任何未来设想列为已实现能力；未来计划在有 issue、设计决策或代码入口作为依据后再记录。

## 维护边界

1. 优先保持公开 Mapper、Service、注解、配置和扩展接口兼容。
2. 保持依赖方向：annotation/Driver 基础由 core 使用，容器适配和可选扩展依赖 core。
3. 行为变化应同步源码、测试、配置元数据、示例和知识文档。
4. 知识库保留稳定结论和验证入口；易变调用细节交由 CodeGraph 定位。
