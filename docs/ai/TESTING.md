# 构建与测试策略

> 审计日期：2026-08-02。测试结构来自定向目录检查和 POM；历史记录包含 core 及其 reactor 依赖的编译，本轮新增全 reactor `validate`，仍未执行测试、打包或完整校验。编译/validate 成功不作为行为或兼容性通过证据。

## 当前测试事实

根 reactor 的 8 个模块中，仅 `mongo-plus-core/src/test` 目录真实存在，但目录为空；其余 7 个模块均没有 `src/test`：

- `mongo-plus-annotation`
- `mongo-plus-boot-starter`
- `mongo-plus-boot4-starter`
- `mongo-plus-solon-plugin`
- `mongo-plus-sharding`
- `mongo-plus-sharding-boot-starter`
- `mongo-plus-sensitive-word`

仓库根目录当前还有未被 Git 跟踪、未被根 `<modules>` 聚合的独立工程 `mongo-plus-test`。其中存在 `BuildConditionRegexTest.java` 和 `BuildConditionNotTest.java`，POM 声明 JUnit 4.13.2，并有本机 surefire 输出。它不是 reactor 测试保障，状态变化后应重新确认。根及 reactor 模块 POM未声明 JUnit、TestNG 或专用测试插件；多个集成/扩展模块还声明了 `maven.test.skip=true`。

## Maven 命令

在仓库根目录使用：

| 目的 | 建议命令 | 说明 |
|---|---|---|
| reactor 编译 | `mvn -DskipTests compile` | 编译 8 个 reactor 模块；不验证行为 |
| reactor 测试 | `mvn test` | 当前 reactor 没有可执行测试；带 `maven.test.skip=true` 的模块不会提供测试保障 |
| reactor 完整校验 | `mvn verify` | 仍不自动等于行为正确；发布 profile 默认激活，执行前留意插件与环境 |
| 单模块及依赖 | `mvn -pl mongo-plus-core -am test` | 将模块名替换为目标模块；`-am` 同时构建项目内依赖 |
| 独立测试工程 | `mvn -f mongo-plus-test/pom.xml -Dmaven.test.skip=false test` | 仅当该未跟踪工程仍存在且所需 2.2.0 构件可解析时使用；必须显式覆盖其跳过属性 |
| BOM 校验 | `mvn -f mongo-plus-bom/pom.xml verify` | BOM 不在根 reactor，需单独执行 |

reactor 各模块均可用 `mvn -pl <模块> -am test`（或把 `test` 换成 `compile`/`verify`）定向构建；有效的 `<模块>` 是 `mongo-plus-annotation`、`mongo-plus-core`、`mongo-plus-boot-starter`、`mongo-plus-boot4-starter`、`mongo-plus-solon-plugin`、`mongo-plus-sharding`、`mongo-plus-sharding-boot-starter`、`mongo-plus-sensitive-word`。其中声明 `maven.test.skip=true` 的模块即使执行 `test` 也不能据此声称测试已运行；应先建立正式测试并重新审视该属性。`mongo-plus-bom` 不接受根 reactor 的 `-pl` 选择，使用上表独立命令。

2026-08-02 的已运行记录：

- `mvn -version`：Maven 3.9.2，运行 JDK 17.0.16。
- `mvn help:active-profiles`：首次因沙箱不能写本地仓库跟踪文件失败，授权后成功；当前项目 `release` profile 默认活动，settings 另有同名外部 profile。
- `mvn validate`：根与 8 个 JAR 模块全部成功；只覆盖 validate 阶段。
- `mvn -pl mongo-plus-core -am -DskipTests compile`：根聚合项目、annotation、core 编译成功；编译 58 个 annotation 源文件和 441 个 core 源文件，并出现 deprecated/unchecked 提示。
- `mvn -pl mongo-plus-boot-starter,mongo-plus-boot4-starter -am dependency:tree "-Dincludes=org.springframework:*,org.springframework.boot:*"`：依赖树解析成功；这是依赖解析证据，不是两个 starter 的编译、启动或行为测试。

**本轮没有执行 `test`、`package`、`verify`、Boot/Solon 启动或真实 MongoDB 行为验证。历史 compile 成功只覆盖当前 JDK 17 下 core 及 annotation 的编译；本轮 validate 只覆盖模型/validate 阶段，均不扩张为行为正确、JDK 8 可用、Driver 兼容或全 reactor 编译通过。**

## 修改区域的最低验证

| 区域 | 最低自动化验证 | 缺少自动化时的最小人工验证 |
|---|---|---|
| `BaseMapper` 与 CRUD | core 单元/集成测试覆盖增删改查、批量、分页、count、异常；再执行 `mvn -pl mongo-plus-core -am test` | 对真实 MongoDB 建表，逐项核对 BSON、返回值、受影响行数、实体回读和 session/非 session 路径 |
| `QueryWrapper` / `UpdateWrapper` | 对每个操作符、逻辑嵌套、null、空集合、重复字段、Wrapper 复用做 BSON 断言；至少覆盖修复分支 | 打印或解码最终 BSON，并在 MongoDB 上核对命中集和更新结果，不能只检查链式 API 不抛异常 |
| Entity Mapping | 保存与读取双向测试，覆盖嵌套对象、集合、Map、泛型、null、额外字段 | 插入已知 Document，再读为实体/`Class<Map>`/`TypeReference`；实体写回后逐字段比较 BSON 类型和值 |
| TypeHandler / MappingStrategy / ConversionStrategy | 分别覆盖优先级、注册覆盖、读写方向及组合；缺陷测试应固定输入 Document 与目标类型 | 构造最小实体逐一启用，再组合启用，记录实际调用顺序、输入输出和异常 |
| 普通拦截器 / 高级拦截器 | 验证前后顺序、短路、异常传播、多个同 order；分别覆盖 `InterceptorChain` 与 `AdvancedInterceptorChain` | 用可记录事件的拦截器跑一次 CRUD，核对事件序列及最终 Driver 调用 |
| Boot 3 Starter | JDK 17 下启动上下文，核对 9 个自动配置入口、Mapper 代理、属性、事务及可选敏感词 | 启动最小 Boot 3.4.2 应用，注入 Mapper，完成一次 CRUD 和一次事务回滚 |
| Boot 4 Starter | JDK 17 下独立启动测试，不能复用 Boot 3 结果；覆盖 Boot 4 专属依赖 | 启动最小 Boot 4.0.1 应用，完成同样的 Bean、Mapper、CRUD、事务检查 |
| Solon Plugin | 启动插件上下文，验证 `XPluginAuto`、Mapper 注入、属性和切面 | 启动最小 Solon 3.0.1 应用并完成 Mapper CRUD；确认 core 未反向依赖 Solon |
| Sharding | 策略路由、无匹配、事务、并发、多数据源同名集合测试 | 至少用两个数据源验证读写命中、事务边界与失败恢复 |
| Sensitive Word | 字段 Handler、拦截器、管理器及与映射/加密/脱敏组合测试 | 保存和读取包含命中/未命中词的实体，核对数据库值、返回值与启停行为 |

## 缺陷回归测试规则

每个缺陷修复至少新增一个先失败后通过的最小测试，并固定：公开入口、最小输入、最终 BSON/Document 或 Driver 可观察结果、预期异常类型（如适用）。Wrapper/转换纯逻辑优先放 core 单元测试；依赖容器、事务、分片或 MongoDB 语义的缺陷放对应模块集成测试。若暂时只能人工验证，必须在 `OPEN_QUESTIONS.md` 保留“待验证”，记录环境和可复现步骤，不能标成已解决。

## 发布前检查清单

- 工作树中的版本、BOM、8 个 reactor 模块及独立 BOM一致。
- 在声明支持的各 JDK/框架/Driver 组合中分别执行 clean verify，并保存命令与结果。
- 执行 core 行为回归、Boot 3/Boot 4/Solon 启动测试、分片与敏感词集成测试。
- 核对公开 API 二进制/源码兼容、自动配置元数据、可选依赖及示例。
- 核对 MongoDB Server 上的 CRUD、事务、索引、聚合、动态集合和多数据源路径。
- 检查 Javadoc、sources、BOM 与发布 profile；BOM 单独验证。
- 清理或明确未跟踪的 `mongo-plus-test` 是否应纳入正式保障。
- 审阅 `OPEN_QUESTIONS.md`：未完成行为验证的项目不得在发布说明中声称已支持或已修复。

编译只证明当前源码可由该编译器处理；它不证明查询语义、映射结果、自动配置、事务、Driver 行为或跨版本兼容正确。
