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
| 修改代码前确定验证方式 | [TESTING.md](TESTING.md) | 按兼容矩阵需要补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 升级 Java、Boot、Driver 或 Solon | [COMPATIBILITY.md](COMPATIBILITY.md) | 验证命令补读 [TESTING.md](TESTING.md) |
| 调查潜在缺陷或边界行为 | [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) | 按问题链接补读对应架构专题 |
| 已确认的架构决策 | `DECISIONS.md`（待生成） | — |

## 专题路由

| 任务 | 当前先读 | 专题状态 |
|---|---|---|
| CRUD、BaseMapper、IService、Repository、Mapper 代理 | [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) | 扩展顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 自动配置、启动流程、MongoClient 初始化、Mapper 扫描或注册 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | 框架版本差异补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| 多数据源、`@MongoDs`、SpEL、上下文、动态数据源 | [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) | 启动注册补读 [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)；运行未决组合补读 [OPEN_QUESTIONS.md](OPEN_QUESTIONS.md) |
| MongoPlus/Spring/Solon/分片事务、ClientSession、SessionExecute | [features/TRANSACTION.md](features/TRANSACTION.md) | 数据源切换补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)；执行器补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 动态集合、`CollectionNameHandler`、collection/registry 缓存 | [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) | 映射补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md)；顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 多租户、`TenantHandler`、`@IgnoreTenant` | [features/TENANT.md](features/TENANT.md) | 组合顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 逻辑删除、`@CollectionLogic`、`@IgnoreLogic` | [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md) | 动态集合元数据补读 [features/DYNAMIC_COLLECTION.md](features/DYNAMIC_COLLECTION.md) |
| 自动填充、`MetaObjectHandler`、`insertFill/updateFill` | [features/AUTO_FILL.md](features/AUTO_FILL.md) | 字段转换补读 [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) |
| Tenant + Logic Delete + Auto Fill 执行顺序 | [features/TENANT.md](features/TENANT.md) + [features/LOGIC_DELETE.md](features/LOGIC_DELETE.md) + [features/AUTO_FILL.md](features/AUTO_FILL.md) | 代理细节按需补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Query/Update Wrapper 与链式 API | [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) | 执行前增强顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 实体映射、类型转换、字段 Handler | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) | CRUD 转换边界补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 普通/高级拦截器、Listener、Handler 的分类与各自顺序 | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) | CRUD 汇合点补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| Spring Boot 3/4 启动集成 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | 版本矩阵补读 [COMPATIBILITY.md](COMPATIBILITY.md) |
| Solon 启动集成 | [architecture/STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md) | 多数据源差异补读 [features/MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md) |
| MongoDB Driver 与版本兼容 | [CURRENT_STATE.md](CURRENT_STATE.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | `integrations/MONGODB_DRIVER.md`（待生成） |
| 聚合、索引/时序集合、加密/脱敏、敏感词 | [PROJECT.md](PROJECT.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | 对应 `features/` 专题待生成 |

## 路径状态

当前已完成审计的全局保障文件为 `TESTING.md`、`COMPATIBILITY.md`、`OPEN_QUESTIONS.md`；已完成审计的专题为 `architecture/CRUD_EXECUTION.md`、`architecture/EXTENSION_PIPELINE.md`、`architecture/QUERY_WRAPPER.md`、`architecture/ENTITY_MAPPING.md`、`architecture/STARTUP_LIFECYCLE.md`、`features/MULTI_DATASOURCE.md`、`features/TRANSACTION.md`、`features/DYNAMIC_COLLECTION.md`、`features/TENANT.md`、`features/LOGIC_DELETE.md` 和 `features/AUTO_FILL.md`。其余专题仍为待生成，创建并完成审计后才能改成链接。
