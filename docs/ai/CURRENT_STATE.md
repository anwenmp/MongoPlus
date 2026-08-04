# 当前状态

> 快照日期：2026-08-02。版本、工具和仓库状态会变化，后续任务必须重新核对当前工作树。

## 知识库完成度

知识库建设范围已经覆盖：

- 项目定位、模块结构、架构总览、CRUD、Wrapper、聚合、实体映射和启动生命周期。
- 公开 API、配置、修改手册、测试、兼容性、构建与发布。
- 多数据源、事务、动态集合、Tenant、Logic Delete、Auto Fill、Optimistic Lock、Index、Time Series 和 Sharding。
- Encryption、Desensitization、Sensitive Word、Command Listener、Data Change Recorder、Async Multi Write、Backup / Restore。

[`INDEX.md`](INDEX.md) 已为 `docs/ai` 中每个 Markdown 文件提供路由。覆盖完成不等于全部行为已验证；缺陷、风险、运行验证、构建发布和维护者决策统一收口到 [`OPEN_QUESTIONS.md`](OPEN_QUESTIONS.md)。

## 已验证工程状态

截至本次收口，只把实际执行结果记为已验证：

- `codegraph status`：603 files、10,908 nodes、22,786 edges，`[OK] Index is up to date`。
- `mvn -version`、`java -version`、`where.exe java`、`where.exe mvn`、`mvn help:active-profiles` 和 `mvn validate` 的本机结果已执行核对；具体工具版本和 profile 输出属于本机快照，不扩张为兼容性保证。
- 本次对允许修改的 Markdown 执行了相对链接、重复标题/anchor、尾随空白和 `git diff --check` 检查。

已执行 sensitive-word 定向回归，当前 9 个转换器级测试覆盖 FieldHandler order、最新值传递、旧实现兼容、TypeHandler→Encrypt 与 LOCAL 组合。core、Boot 2/3 starter、Boot 4 starter 定向 compile 已通过；reactor 外 `mongo-plus-test` 当前 19 项测试全部通过，其中事务 3 项、IgnoreLogic update 2 项为代理单测。未执行或未形成已验证结论：全 reactor `test/package/verify/deploy`、独立 BOM、真实 MongoDB 集成测试，以及 Boot 2、Boot 3、Boot 4、Solon 启动测试。

## 当前仓库边界

- 根 reactor 是 1 个根聚合项目加 8 个 JAR 模块；“8 个模块”与 Maven reactor 的 9 个 project 不冲突。
- `mongo-plus-bom` 是无 parent 的独立 Maven project，不在根 reactor。
- `mongo-plus-sensitive-word` 现有正式 tracked JUnit 4 转换器级回归；`mongo-plus-test` 仍是未跟踪目录且不在根 reactor，当前已运行其中 19 项测试，但不能视为 reactor CI 门禁。
- 仓库内未发现 CI、Maven Wrapper、Maven Enforcer 或 toolchains；这不证明外部平台不存在 CI。
- Boot 4 模块显式 target 17；其他模块主要沿用根 target 8。普通 starter 以 Spring 5.3/Java 8 API 为 Boot 2/3 兼容基线，但本轮 Boot 2.7 下载验证未获权限；Boot 3/4 应用仍至少需要 Java 17。
- core/Solon 在 Java 8 上的真实运行能力尚未执行验证。
- 当前公开 API 继承关系为：`MongoMapper` 不继承 `BaseMapper`，`IService extends IRepository`，`IRepository extends MongoMapper`。
- Query/Update 链有 `clear`；Aggregate 链没有同等 `reset`。`auto-scan-packages` 不负责 Mapper 扫描。
- `release` 的 `activeByDefault` 仍受 Maven profile 激活规则约束；`central` 与 `release` 是不同 server id，真实发布组合尚未验证。
- Solon 存在拦截器实现类不等于注解绑定已生效，相关项保留为运行验证问题。

## 后续工作方向

知识库建设阶段建议结束。后续从 [`OPEN_QUESTIONS.md`](OPEN_QUESTIONS.md) 选择单一条目：

1. 已确认缺陷先重新核对当前源码。
2. 编写能失败的最小测试并修复。
3. 运行最小测试和必要组合测试。
4. 更新对应专题，再更新 `OPEN_QUESTIONS.md` 与本状态快照。

高风险设计先由维护者评估，运行验证按条目给出的环境和成功判定执行；二者不能直接升级为缺陷。
