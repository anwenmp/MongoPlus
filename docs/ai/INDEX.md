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
| 构建与测试策略 | `TESTING.md`（待生成） | `COMPATIBILITY.md`（待生成） |
| 兼容性策略 | `COMPATIBILITY.md`（待生成） | [CURRENT_STATE.md](CURRENT_STATE.md) |
| 已确认的架构决策 | `DECISIONS.md`（待生成） | — |

## 专题路由

| 任务 | 当前先读 | 专题状态 |
|---|---|---|
| CRUD、BaseMapper、IService、Repository、Mapper 代理 | [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) | 扩展顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| Query/Update Wrapper 与链式 API | [architecture/QUERY_WRAPPER.md](architecture/QUERY_WRAPPER.md) | 执行前增强顺序补读 [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) |
| 实体映射、类型转换、字段 Handler | [architecture/ENTITY_MAPPING.md](architecture/ENTITY_MAPPING.md) | CRUD 转换边界补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 普通/高级拦截器、Listener、Handler 的分类与各自顺序 | [architecture/EXTENSION_PIPELINE.md](architecture/EXTENSION_PIPELINE.md) | CRUD 汇合点补读 [architecture/CRUD_EXECUTION.md](architecture/CRUD_EXECUTION.md) |
| 数据源、事务、分片 | [ARCHITECTURE.md](ARCHITECTURE.md) + [MODULES.md](MODULES.md) | `architecture/DATASOURCE_TRANSACTION.md`（待生成） |
| Spring Boot 3/4 | [MODULES.md](MODULES.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | `integrations/SPRING_BOOT.md`（待生成） |
| Solon | [MODULES.md](MODULES.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | `integrations/SOLON.md`（待生成） |
| MongoDB Driver 与版本兼容 | [CURRENT_STATE.md](CURRENT_STATE.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | `integrations/MONGODB_DRIVER.md`（待生成） |
| 聚合、逻辑删除、自动填充、租户、动态集合、索引/时序集合、加密/脱敏、敏感词 | [PROJECT.md](PROJECT.md) + [ARCHITECTURE.md](ARCHITECTURE.md) | 对应 `features/` 专题均待生成；按任务一次只创建一个 |

## 路径状态

当前已完成审计的专题为 `architecture/CRUD_EXECUTION.md`、`architecture/EXTENSION_PIPELINE.md`、`architecture/QUERY_WRAPPER.md` 和 `architecture/ENTITY_MAPPING.md`；其余专题仍为待生成，创建并完成审计后才能改成链接。
