# 当前状态

> 快照日期：2026-08-01。版本值来自当日工作树中的 POM；分支来自 `git branch --show-current`。易变信息在升级后必须重新核对。

## 版本、分支与模块

- 当前分支：`dev`（来源：Git，2026-08-01）。
- 根坐标：`com.mongoplus:mongo-plus:2.2.0`，`packaging=pom`（来源：根 [`pom.xml`](../../pom.xml)）。
- 根 reactor 共 8 个模块：`mongo-plus-annotation`、`mongo-plus-core`、`mongo-plus-boot-starter`、`mongo-plus-solon-plugin`、`mongo-plus-sharding`、`mongo-plus-sharding-boot-starter`、`mongo-plus-sensitive-word`、`mongo-plus-boot4-starter`。
- `mongo-plus-bom:2.2.0` 是仓库内独立 BOM，管理上述 8 个构件，但未列入根 reactor。

不在本文件保存未提交文件列表、一次性任务记录或本机 Git 警告。

## Java 与主要依赖版本

| 项目 | 当前声明 | 来源与含义 |
|---|---:|---|
| Maven compiler source/target | 8 | 根 POM；表示项目统一声明的编译目标，不证明所有依赖可在 JDK 8 运行 |
| MongoDB Driver BOM | 5.4.0 | 根 POM；core 的 `mongodb-driver-sync` 由该 BOM 管理 |
| Spring Boot 3 | 3.4.2 | 根 POM；Boot 3 starter 的 autoconfigure/processor |
| Spring Boot 4 | 4.0.1 | 根属性及 Boot 4 模块 POM；Boot 4 模块显式引用相关构件 |
| Solon | 3.0.1 | 根 POM；Solon 插件的 `provided` 依赖 |
| Spring TX（Boot 3） | 5.3.27 | 根 POM / Boot 3 starter |
| Spring TX（Boot 4） | 7.0.2 | `mongo-plus-boot4-starter/pom.xml` 的显式覆盖 |
| AspectJ Weaver（Boot 3） | 1.9.7 | 根 POM / Boot 3 starter |
| AspectJ Weaver（Boot 4） | 1.9.25.1 | Boot 4 模块 POM 的显式覆盖 |
| sensitive-word | 0.25.0 | 根 POM / sensitive-word 模块 |
| Bouncy Castle | 1.78.1 | 根 POM；core 中为 `provided` |

## CodeGraph 与测试证据

- 2026-08-02 执行 `codegraph status`：603 files、10,908 nodes、22,786 edges，结果为 `[OK] Index is up to date`。
- 当前会话未暴露 CodeGraph MCP 工具，使用与 `codegraph_explore`/`codegraph_status` 对应的 CLI `codegraph explore`、`codegraph status`。
- CodeGraph 查询未找到 `org.junit`、`BaseMapperTest` 或 `IServiceTest`，并对关键入口报告 `no covering tests found`。这说明当前索引没有相应测试证据；不扩张为对索引外文件或外部 CI 的断言。
- 2026-08-02 执行 `mvn -pl mongo-plus-core -am -DskipTests compile`，根聚合项目、annotation 和 core 编译成功；没有执行测试、完整 reactor 构建或兼容矩阵，详见 `TESTING.md`。

## 待验证

- Java 8/17 与 Boot 3、Boot 4、Solon 各组合的实际构建和运行矩阵，尤其是 Boot 4 的实际 JDK 下限。
- MongoDB Server 兼容矩阵，以及 Driver 5.4.0 之外的已测试范围。
- `mongo-plus-bom` 不加入根 reactor 的发布设计与发布流水线。
- Boot 4 是否需要对应的 sharding starter。
- 全模块 clean build、Javadoc、发布 profile 和外部 CI 的当前结果。
- 主要功能的行为边界与回归保障；当前缺少已索引的自动化测试证据。
