---
name: mongoplus
description: 在已使用或明确准备采用 MongoPlus 的 Java 项目中，发现框架能力、核对注解与公开 API，并实现和验证数据库功能。适用于查询、更新、聚合、映射、扩展、隔离、事务、路由、索引及可选模块；业务描述没有提到 API 名时也适用。仅有 MongoDB Driver、Spring Data 或 MyBatis-Plus 不构成采用 MongoPlus 的依据。
---

# MongoPlus 开发入口

先看目标模块实际POM/Gradle/BOM、已解析构件版本和现有`com.mongoplus.*`调用。沿应用已有Mapper、Repository或Service选择入口。此知识以当前源码`2.2.0`为基线，具体提交、文件哈希和验证范围见[覆盖报告](audit/coverage-report.md)；其他版本须核对其源码/JAR，不能只看版本字符串。

## 按业务意图加载专题

只加载当前任务所需主题；不知道能力名称时搜索[完整能力目录](capabilities.md)。用户提到任意注解，先在[全部注解目录](annotations/index.md)查完整import、作用域、全部属性默认值、注册条件和限制，包含Search/Vector容器及嵌套配置。

| 需求或检索词 | 专题 |
|---|---|
| 接入、Boot 3/4、Solon、BOM、Mapper扫描 | [集成](references/integration.md) |
| 保存、批量、分页、计数、Document、bulkWrite、删除并返回 | [CRUD](references/crud.md) |
| 筛选、Lambda、嵌套数组、正则、投影、文本、地理、位运算 | [查询](references/query.md) |
| 原子更新、数组更新、乐观锁、重试、防空条件 | [更新](references/update.md) |
| lookup、facet、union、group、bucket、窗口、所有主要阶段 | [聚合](references/aggregation.md) |
| 日期、数组、数学、条件、类型转换、N值累加器 | [表达式](references/expressions.md) |
| ID、枚举、类型、TypeHandler、Strategy、填充、DBRef、额外字段、字典回写 | [映射](references/mapping.md) |
| 加密、解密、脱敏、非空/可空提示 | [字段处理](references/field-security.md) |
| 敏感词、黑白名单、局部/全局审核 | [敏感词模块](references/sensitive-word.md) |
| 数据权限、环绕执行、短路、拦截顺序 | [拦截器](references/interceptors.md) |
| Handler、Codec、Aware、注册及生命周期 | [扩展体系](references/extensions.md) |
| 命令告警、数据变动记录、异步镜像、持续同步 | [监听与同步](references/events.md) |
| 多源、动态集合、读偏好、声明式/编程式事务 | [事务与路由](references/transactions-routing.md) |
| 数据源分片、分片Starter、多资源事务 | [分片模块](references/sharding.md) |
| 租户、逻辑删除、忽略及恢复 | [隔离](references/isolation.md) |
| 唯一、TTL、复合、地理、哈希、传统text、手工管理 | [普通索引](references/indexes.md) |
| Search、向量索引、HNSW、量化、分析器、状态与冲突 | [Search/Vector](references/search-vector.md) |
| 时序数据、bucket、集合保留期 | [时序集合](references/timeseries.md) |
| 导出、恢复、GridFS文件 | [备份与文件](references/backup-gridfs.md) |
| 配置key/default、URI、池、TLS、配置加密 | [配置](references/configuration.md) |
| distinct、replaceOne、框架无专用入口、Driver边界 | [互操作](references/driver-interop.md) |
| 辅助类型、异常、低层public类型角色 | [辅助与公开边界](references/utilities.md) |
| 索引开发工具、旧能力索引 | [工具边界](references/tooling.md) |

精确重载、泛型和import按需查[API分族签名](references/api/index.md)，不要一次加载全部签名。源码缺失时可检查对应JAR的`javap`输出；不强制任何MCP、IDE、客户端私有工具或索引服务。

## API选择与验证

优先用适合业务的MongoPlus高层公开API：复杂筛选用QueryWrapper，原子写用UpdateWrapper/绑定更新链，实体并发先分析@Version机制，统一权限用Interceptor，字段处理先分析已有注解/Handler，聚合用AggregateWrapper及框架表达式。MongoMapper、IRepository、IService和非泛型BaseMapper能力不同，按变量真实声明类型选方法；当前没有独立LambdaQueryWrapper类。

表达不足时先考虑框架的BSON条件/阶段组合。确需Driver，说明缺少的框架入口及其转换、拦截器、路由和session边界。Search/Vector先核对部署与索引状态，不能以声明存在推定可以查询。

写代码前核对精确包名、方法签名、泛型、boolean返回含义和注册顺序；区分构建、执行和取结果。聚合同名Bson重载可能分别接受阶段体或完整阶段，按[聚合表](references/aggregation.md)逐项检查。

对契约有疑问的API，先跟到最终BSON构建处，再在授权的专用测试集合实际执行该重载，用Driver CommandListener捕获实发filter/update或完整pipeline；执行等价原生命令，比较完整结果及最终数据状态。各重载独立实验。冲突不确定时记录源码/文档/运行的差异，标`CONTRACT_UNRESOLVED`，不修改正确行为迎合测试。环境不具备时标`NOT_RUN`，只做过源码分析则`UNVERIFIED`。实验记录格式和本Skill实际验证见[验证说明](references/verification.md)。
