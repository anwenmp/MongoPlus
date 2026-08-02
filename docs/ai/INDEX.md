# MongoPlus AI 知识索引

## 使用方式

为普通任务选择 1 个主文件；复杂组合任务最多先读 2～3 个。实现位置、调用链和影响范围先用 CodeGraph，细节再沿主文件内部链接继续导航。`TESTING.md`、`COMPATIBILITY.md` 和 `OPEN_QUESTIONS.md` 是按需横切参考，不是每项任务的前置阅读。

## 高频入口

| 任务 | 主文件 | 可选补充（最多选 2 个） |
|---|---|---|
| 项目概览 | [PROJECT.md](PROJECT.md) | [CURRENT_STATE.md](CURRENT_STATE.md) |
| 模块结构 | [MODULES.md](MODULES.md) | [ARCHITECTURE.md](ARCHITECTURE.md) |
| 当前仓库与知识库状态 | [CURRENT_STATE.md](CURRENT_STATE.md) | [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| 架构总览 | [ARCHITECTURE.md](ARCHITECTURE.md) | [MODULES.md](MODULES.md) |
| 选择公开 API | [PUBLIC_API.md](PUBLIC_API.md) | 对应执行专题 |
| 查询配置 key、默认值或消费者 | [CONFIGURATION.md](CONFIGURATION.md) | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| 修改代码、注解或配置 | [CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) | 对应专题；[CONFIGURATION.md](CONFIGURATION.md) |
| 测试策略 | [TESTING.md](TESTING.md) | [COMPATIBILITY.md](COMPATIBILITY.md) |
| 新增模块、修改 POM、构建、版本升级或发布 | [BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md) | [MODULES.md](MODULES.md)；[COMPATIBILITY.md](COMPATIBILITY.md) |
| 调查缺陷、风险或未验证行为 | [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) | 条目所链接的专题 |

## 日常开发

| 任务 | 主文件 | 可选补充 |
|---|---|---|
| 修改 CRUD、Mapper、Service 或 Repository | [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) | [PUBLIC_API.md](PUBLIC_API.md)；[CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) |
| 修改 Query/Update Wrapper 或 Chain | [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) | [PUBLIC_API.md](PUBLIC_API.md)；[CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) |
| 修改 Aggregate | [architecture/AGGREGATION.md](architecture/AGGREGATION.md) | [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md)；[architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| 修改 Entity Mapping、TypeHandler 或字段映射 | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) | [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)；[CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) |
| 修改普通/高级 Interceptor、Listener 或 Handler | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) | [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)；[CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md) |
| 修改启动、扫描、注册或自动配置 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | [CONFIGURATION.md](CONFIGURATION.md)；[COMPATIBILITY.md](COMPATIBILITY.md) |
| 修改数据源或事务 | [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) | [features/TRANSACTION.md](features/TRANSACTION.md)；[architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |

## 功能专题

| 功能 | 主文件 | 可选补充 |
|---|---|---|
| Tenant | [features/TENANT.md](features/TENANT.md) | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Logic Delete | [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md) | [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) |
| Auto Fill | [features/AUTO_FILL.md](features/AUTO_FILL.md) | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| Dynamic Collection | [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) | [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| Optimistic Lock | [features/OPTIMISTIC_LOCK.md](features/OPTIMISTIC_LOCK.md) | [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md) |
| Index / Time Series | [features/INDEX_AND_TIMESERIES.md](features/INDEX_AND_TIMESERIES.md) | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| Sharding | [features/SHARDING.md](features/SHARDING.md) | [features/TRANSACTION.md](features/TRANSACTION.md)；[features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| Encryption | [features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md) | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| Desensitization | [features/DESENSITIZATION.md](features/DESENSITIZATION.md) | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| Sensitive Word | [features/SENSITIVE_WORD.md](features/SENSITIVE_WORD.md) | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Command Listener | [features/COMMAND_LISTENER.md](features/COMMAND_LISTENER.md) | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) |
| Data Change Recorder | [features/DATA_CHANGE_RECORDER.md](features/DATA_CHANGE_RECORDER.md) | [features/TRANSACTION.md](features/TRANSACTION.md)；[architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Async Multi Write | [features/ASYNC_MULTI_WRITE.md](features/ASYNC_MULTI_WRITE.md) | [features/TRANSACTION.md](features/TRANSACTION.md)；[architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Backup / Restore | [features/BACKUP_AND_RESTORE.md](features/BACKUP_AND_RESTORE.md) | [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |

## 文件清单

以下是 `docs/ai` 当前全部 Markdown；每个文件均已在上方直接路由或作为任务补充路由：

- 全局：[PROJECT.md](PROJECT.md)、[MODULES.md](MODULES.md)、[ARCHITECTURE.md](ARCHITECTURE.md)、[PUBLIC_API.md](PUBLIC_API.md)、[CONFIGURATION.md](CONFIGURATION.md)、[CHANGE_PLAYBOOK.md](CHANGE_PLAYBOOK.md)、[BUILD_AND_RELEASE.md](BUILD_AND_RELEASE.md)、[TESTING.md](TESTING.md)、[COMPATIBILITY.md](COMPATIBILITY.md)、[CURRENT_STATE.md](CURRENT_STATE.md)、[OPEN_QUESTIONS.md](OPEN_QUESTIONS.md)。
- 架构：[architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md)、[architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md)、[architecture/AGGREGATION.md](architecture/AGGREGATION.md)、[architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md)、[architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md)、[architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)。
- 功能：[features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)、[features/TRANSACTION.md](features/TRANSACTION.md)、[features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md)、[features/TENANT.md](features/TENANT.md)、[features/LOGIC_DELETE.md](features/LOGIC_DELETE.md)、[features/AUTO_FILL.md](features/AUTO_FILL.md)、[features/OPTIMISTIC_LOCK.md](features/OPTIMISTIC_LOCK.md)、[features/INDEX_AND_TIMESERIES.md](features/INDEX_AND_TIMESERIES.md)、[features/SHARDING.md](features/SHARDING.md)、[features/FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md)、[features/DESENSITIZATION.md](features/DESENSITIZATION.md)、[features/SENSITIVE_WORD.md](features/SENSITIVE_WORD.md)、[features/COMMAND_LISTENER.md](features/COMMAND_LISTENER.md)、[features/DATA_CHANGE_RECORDER.md](features/DATA_CHANGE_RECORDER.md)、[features/ASYNC_MULTI_WRITE.md](features/ASYNC_MULTI_WRITE.md)、[features/BACKUP_AND_RESTORE.md](features/BACKUP_AND_RESTORE.md)。
