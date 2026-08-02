# MongoPlus AI 知识索引

## 使用方式

先选 1～3 份与任务最相关的文件。涉及实现位置、调用链或影响范围时先用 CodeGraph，再读取它未覆盖的 POM、资源、源码或测试。标记为“待生成”的专题当前不是可点击文件。

## 全局知识

| 任务 | 先读 | 可选补充 |
|---|---|---|
| 项目定位、已实现/计划边界 | [PROJECT.md](PROJECT.md) | [CURRENT_STATE.md](CURRENT_STATE.md) |
| Maven 模块、职责、依赖方向 | [MODULES.md](MODULES.md) | [ARCHITECTURE.md](ARCHITECTURE.md) |
| 全局分层、CRUD 总体路径、启动入口 | [ARCHITECTURE.md](ARCHITECTURE.md) | [MODULES.md](MODULES.md) |
| 当前版本、分支、依赖快照、待验证项 | [CURRENT_STATE.md](CURRENT_STATE.md) | [MODULES.md](MODULES.md) |
| 开发/API 约定 | `CONVENTIONS.md`（待生成） | — |
| 选择公开 CRUD、Service、Repository、Chain 或无实体入口 | [PUBLIC_API.md](PUBLIC_API.md) | 按链接补读一个实现专题 |
| 查询配置 key、默认值、消费者或集成差异 | [CONFIGURATION.md](CONFIGURATION.md) | 启动细节补读 [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| 修改代码前确定验证方式 | [TESTING.md](TESTING.md) | 按兼容矩阵需要补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 修改代码时确定最小影响面、顺序和遗漏矩阵 | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | 验证命令补读 [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) |
| 构建、安装、升级版本或发布 | [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) | 模块职责补读 [MODULES.md](MODULES.md)；测试边界补读 [TESTING.md](TESTING.md) |
| 升级 Java、Boot、Driver 或 Solon | [COMPATIBILITY.md](COMPATIBILITY.md) | 验证命令补读 [TESTING.md](TESTING.md) |
| 调查潜在缺陷或边界行为 | [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) | 按问题链接补读对应架构专题 |
| 已确认的架构决策 | `DECISIONS.md`（待生成） | — |

## 专题路由

| 任务 | 当前先读 | 专题状态 |
|---|---|---|
| 修改公开 CRUD API | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | 入口选择补读 [PUBLIC_API.md](PUBLIC_API.md)；执行链补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 新增 Wrapper 操作符 | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | 条件语义补读 [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) |
| 新增普通拦截器或高级拦截器 | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | 顺序与包装补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 新增字段注解或 Mapping Handler | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | 映射阶段补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 新增配置项 | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | key/消费者补读 [CONFIGURATION.md](CONFIGURATION.md) |
| 新增模块或修改 POM | [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) | 职责补读 [MODULES.md](MODULES.md)；Java/框架边界补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 构建单模块或整个 reactor | [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) | 测试保障补读 [TESTING.md](TESTING.md) |
| 升级版本或发布 Maven Central | [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) | 未决凭据/平台步骤补读 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| 检查 Boot 3/Boot 4/Solon 兼容性 | [COMPATIBILITY.md](COMPATIBILITY.md) | 发布接线清单补读 [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) |
| CRUD、BaseMapper、IService、Repository、Mapper 代理 | [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) | 扩展顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 选择 BaseMapper、IService 或 IRepository；调查公开 CRUD API | [PUBLIC_API.md](PUBLIC_API.md) | 深入链路再读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| Wrapper、Chain、DTO 或无实体 Map/Document 入口 | [PUBLIC_API.md](PUBLIC_API.md) | 条件细节补读 [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) |
| 配置单/多数据源、Mapper 扫描、自动索引/时序或查询 key 默认值 | [CONFIGURATION.md](CONFIGURATION.md) | 对应运行链按文内链接补读一个专题 |
| 对比 Boot 3、Boot 4、Solon 配置；调查声明但未消费字段 | [CONFIGURATION.md](CONFIGURATION.md) | 版本要求补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 自动配置、启动流程、MongoClient 初始化、Mapper 扫描或注册 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | 框架版本差异补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 多数据源、`@MongoDs`、SpEL、上下文、动态数据源 | [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) | 启动注册补读 [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)；运行未决组合补读 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| MongoPlus/Spring/Solon/分片事务、ClientSession、SessionExecute | [features/TRANSACTION.md](features/TRANSACTION.md) | 数据源切换补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)；执行器补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 动态集合、`CollectionNameHandler`、collection/registry 缓存 | [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) | 映射补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md)；顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 多租户、`TenantHandler`、`@IgnoreTenant` | [features/TENANT.md](features/TENANT.md) | 组合顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 逻辑删除、`@CollectionLogic`、`@IgnoreLogic` | [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md) | 动态集合元数据补读 [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) |
| 自动填充、`MetaObjectHandler`、`insertFill/updateFill` | [features/AUTO_FILL.md](features/AUTO_FILL.md) | 字段转换补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| Tenant + Logic Delete + Auto Fill 执行顺序 | [features/TENANT.md](features/TENANT.md) + [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md) + [features/AUTO_FILL.md](features/AUTO_FILL.md) | 代理细节按需补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Query/Update Wrapper 与链式 API | [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) | 执行前增强顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 聚合 Wrapper、stage、aggregate match、lookup、结果映射 | [architecture/AGGREGATION.md](architecture/AGGREGATION.md) | 条件边界补读 [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md)；映射细节补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 聚合中的 Tenant/Logic Delete、动态集合与事务 | [architecture/AGGREGATION.md](architecture/AGGREGATION.md) | 按问题补读 [features/TENANT.md](features/TENANT.md)、[features/LOGIC_DELETE.md](features/LOGIC_DELETE.md)、[features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) 或 [features/TRANSACTION.md](features/TRANSACTION.md) |
| 乐观锁、版本冲突、逻辑删除组合 | [features/OPTIMISTIC_LOCK.md](features/OPTIMISTIC_LOCK.md) | 动态 namespace 补读 [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md)；填充补读 [features/AUTO_FILL.md](features/AUTO_FILL.md) |
| 实体映射、类型转换、字段 Handler | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) | CRUD 转换边界补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 普通/高级拦截器、Listener、Handler 的分类与各自顺序 | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) | CRUD 汇合点补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| Spring Boot 3/4 启动集成 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | 版本矩阵补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| Solon 启动集成 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | 多数据源差异补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| 自动索引、索引注解、已有索引冲突、手工索引 API | [features/INDEX_AND_TIMESERIES.md](features/INDEX_AND_TIMESERIES.md) | 启动顺序补读 [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| 动态集合索引、时序集合及其初始化 | [features/INDEX_AND_TIMESERIES.md](features/INDEX_AND_TIMESERIES.md) | 动态 namespace 补读 [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md)；多数据源补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| 分片策略、数据源分片、集合分片边界、查询结果合并 | [features/SHARDING.md](features/SHARDING.md) | 执行链补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)；聚合补读 [architecture/AGGREGATION.md](architecture/AGGREGATION.md) |
| 分片事务及其与普通事务组合 | [features/SHARDING.md](features/SHARDING.md) | 普通事务补读 [features/TRANSACTION.md](features/TRANSACTION.md)；多数据源补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| MongoDB Driver 与版本兼容 | [CURRENT_STATE.md](CURRENT_STATE.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | `integrations/MONGODB_DRIVER.md`（待生成） |
| 修改字段加密、密钥或算法 | [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md) | 映射组合补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 调查加密字段查询 | [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md) | Wrapper 构建补读 [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) |
| 修改脱敏或调查脱敏对象再次保存 | [features/DESENSITIZATION.md](features/DESENSITIZATION.md) | 映射边界补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 修改敏感词过滤 | [features/SENSITIVE_WORD.md](features/SENSITIVE_WORD.md) | 执行顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 调查敏感词和加密顺序 | [features/SENSITIVE_WORD.md](features/SENSITIVE_WORD.md) + [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md) | 读取脱敏补 [features/DESENSITIZATION.md](features/DESENSITIZATION.md) |
| 调查 Map/Document/DTO/聚合安全能力差异 | [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md) + [features/DESENSITIZATION.md](features/DESENSITIZATION.md) + [features/SENSITIVE_WORD.md](features/SENSITIVE_WORD.md) | Map 映射缺陷补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 修改 MongoDB 命令监听、调查 commandStarted/commandSucceeded/commandFailed 或监听日志泄漏 | [features/COMMAND_LISTENER.md](features/COMMAND_LISTENER.md) | 注册补读 [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)；Driver 边界补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 修改数据变更记录、调查 before/after 快照、逻辑删除审计、事务回滚记录或 Recorder 递归 | [features/DATA_CHANGE_RECORDER.md](features/DATA_CHANGE_RECORDER.md) | 顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md)；事务问题补读 [features/TRANSACTION.md](features/TRANSACTION.md) |
| 比较 Command Listener 与 Data Change Recorder | [features/COMMAND_LISTENER.md](features/COMMAND_LISTENER.md) + [features/DATA_CHANGE_RECORDER.md](features/DATA_CHANGE_RECORDER.md) | — |
| 修改异步多数据源写入、调查上下文/主写失败/递归/线程池关闭 | [features/ASYNC_MULTI_WRITE.md](features/ASYNC_MULTI_WRITE.md) | 事务补读 [features/TRANSACTION.md](features/TRANSACTION.md)；运行风险补读 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| 修改本地备份、恢复或导入；调查类型保真/覆盖规则 | [features/BACKUP_AND_RESTORE.md](features/BACKUP_AND_RESTORE.md) | 数据源补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)；运行风险补读 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| 调查恢复与异步多写组合 | [features/BACKUP_AND_RESTORE.md](features/BACKUP_AND_RESTORE.md) + [features/ASYNC_MULTI_WRITE.md](features/ASYNC_MULTI_WRITE.md) | 拦截器顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |

## 路径状态

`CHANGE_PLAYBOOK.md` 与 `BUILD_AND_RELEASE.md` 已于 2026-08-02 完成独立审计并进入正式路由。

当前已完成审计的全局保障文件为 `TESTING.md`、`COMPATIBILITY.md`、`OPEN_QUESTIONS.md`；已完成审计的专题为 `architecture/CRUD_EXECUTION.md`、`architecture/EXTENSION_PIPELINE.md`、`architecture/QUERY_WRAPPER.md`、`architecture/ENTITY_MAPPING.md`、`architecture/STARTUP_LIFECYCLE.md`、`architecture/AGGREGATION.md`、`features/MULTI_DATASOURCE.md`、`features/TRANSACTION.md`、`features/DYNAMIC_COLLECTION.md`、`features/TENANT.md`、`features/LOGIC_DELETE.md`、`features/AUTO_FILL.md`、`features/OPTIMISTIC_LOCK.md`、`features/INDEX_AND_TIMESERIES.md`、`features/SHARDING.md`、`features/FIELD_ENCRYPTION.md`、`features/DESENSITIZATION.md`、`features/SENSITIVE_WORD.md`、`features/COMMAND_LISTENER.md`、`features/DATA_CHANGE_RECORDER.md`、`features/ASYNC_MULTI_WRITE.md` 和 `features/BACKUP_AND_RESTORE.md`。其余专题仍为待生成，创建并完成审计后才能改成链接。
