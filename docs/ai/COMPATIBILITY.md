# 兼容性策略

> 审计日期：2026-08-02。事实以当前 POM 和源码为准，不以 README 宣传语推断支持范围。本次没有完成任何兼容矩阵构建或运行测试。

## Java 声明与真实下限

根 POM 声明 `maven.compiler.source/target=8`。annotation、core、Boot 3、Solon、sharding、sharding Boot starter、sensitive-word 的模块 POM也声明 8；Boot 4 模块明确声明 17。这里的“真实下限”分两层：

| 模块 | 本项目源码目标 | 可确认的使用下限 |
|---|---:|---|
| annotation、core、sharding、sensitive-word | Java 8 | 源码/产物目标为 8；依赖组合尚未用 JDK 8 实际构建运行 |
| Boot 2/3 starter、sharding Boot starter | Java 8 | `mongo-plus-boot-starter` 以 Spring 5.3 / Java 8 API 为编译基线并保留 Boot 2 `spring.factories` 与 Boot 3 imports；当前 reactor 默认依赖 Boot 3.4.2，Boot 2.7 实际编译/启动仍需独立矩阵确认 |
| Boot 4 starter | Java 17 | 至少 Java 17；POM 与 Boot 4.0.1 依赖一致，未验证更高 JDK |
| Solon plugin | Java 8 | 本模块目标为 8；Solon 3.0.1 的完整运行矩阵未实测，暂不承诺仅凭 target=8 即可运行 |

## 框架与依赖版本来源

| 集成 | 当前依赖 | 版本来源与差异 |
|---|---|---|
| Boot 2/3 | `mongo-plus-boot-starter` 当前 reactor 用 autoconfigure/configuration processor 3.4.2 编译，事务 API 基线是 Spring TX 5.3.27 | 源码只使用 Spring 5.3 已有的 `AbstractPlatformTransactionManager`、`SmartTransactionObject` 和 `TransactionSynchronizationManager` API；消费应用应由自身 Boot BOM 统一 Spring 版本，Boot 2.7/3.4 启动矩阵尚未完成 |
| Boot 4 | autoconfigure、configuration processor、`spring-boot-mongodb`、`spring-boot-persistence` 4.0.1 | Boot 4 模块显式版本；实际依赖树解析出 Spring Framework 7.0.2，且模块显式 `spring-tx` 7.0.2、AspectJ Weaver 1.9.25.1，源码与 Boot 3 平行维护 |
| MongoDB Driver | core 直接依赖 `mongodb-driver-sync`；annotation 依赖 `mongodb-driver-core` | 根导入 `org.mongodb:mongodb-driver-bom:5.4.0`，因此当前受管版本为 5.4.0；本次依赖树未成功，不能声称已解析/运行验证 |
| Solon | `org.noear:solon` 3.0.1，scope=provided | 版本来自根 `${solon-api.version}`；边界限于 `mongo-plus-solon-plugin` 的插件启动、Bean/切面与 Mapper 注入，core 不依赖 Solon |

## Driver 4.x / 5.x 兼容代码与风险

- `mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java` 和 `com.mongoplus.toolkit.Filters` 构造 BSON，直接受 Driver model/codec 行为影响。
- `mongo-plus-core/src/main/java/com/mongoplus/execute/instance/DefaultExecute.java` 与 `SessionExecute.java` 是同步 Driver 调用边界；升级时重点核对 session 重载、count、bulk write、index 与 aggregate。
- `mongo-plus-core/src/main/java/com/mongoplus/bson/OverridableUuidRepresentationCodecProvider.java`、`codecs/Jsr310CodecProvider.java` 及转换策略是 codec 兼容重点。
- `mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/FillField.java` 直接继承 `com.mongodb.internal.client.model.AbstractConstructibleBsonElement`，这是 Driver internal API。它不属于稳定公共契约，4.x/5.x 或小版本升级都可能发生二进制/源码破坏，应以至少编译、聚合 BSON 测试和真实 `$fill` 行为测试验证。

当前源码没有独立的 Driver 4/5 适配模块或 profile；所谓 4.x/5.x 兼容只能通过上述公共 API 使用点与 `FillField` internal API 风险点做矩阵验证，不能由 BOM=5.4.0 反推 4.x 已支持。

## Starter 与可选模块边界

`mongo-plus-sharding-boot-starter` 直接依赖 `mongo-plus-boot-starter` 和 `mongo-plus-sharding`，因此它是 Boot 3 分片装配层；Boot 4 当前没有对应 sharding starter。普通 Boot 3/4 starter 都以 provided 方式关联 sensitive-word，使敏感词保持可选。Solon 插件只依赖 core，不经过 Spring starter。

## 已确认与未验证矩阵

### 已确认的静态组合

“已确认”仅指当前 POM/源码声明可组合，不代表构建或运行通过。

| Java | 集成 | 声明版本 | Driver 声明 | 证据 |
|---:|---|---:|---:|---|
| 17 | Boot 3 starter | 3.4.2 | BOM 5.4.0 | POM 静态声明；未运行 |
| 8/11/17 | Boot 2 starter | 2.7.x | BOM 5.4.0 | 兼容目标；本轮下载 Boot 2.7.18 依赖未获权限，尚未完成独立编译或启动验证 |
| 17 | Boot 4 starter | 4.0.1 | BOM 5.4.0 | 模块 target=17 与 POM 静态声明；未运行 |
| 8（源码目标） | Solon plugin | 3.0.1 | BOM 5.4.0 经 core | POM 静态声明；运行下限未验证 |
| 8（源码目标） | core/annotation | 无容器 | BOM 5.4.0 | POM 静态声明；未运行 |

### 仍未验证

- JDK 8 上 annotation/core/sharding/sensitive-word/Solon 的 clean build 与运行。
- JDK 8/11/17 下 Boot 2.7.x、JDK 17/21 下 Boot 3.4.2/Boot 4.0.1，以及 Solon 3.0.1 的启动与行为。
- Driver 4.x 任一版本、Driver 5.4.0 以外版本，以及各 Driver 对应的 MongoDB Server 版本。
- Boot 2/3 消费应用能否由各自 Boot BOM 完整改写 Spring TX 版本并启动；Boot 4 的 Spring 7.0.2 启动与事务行为。AspectJ 的实际织入行为也未验证。
- Boot 3 sharding starter 的多数据源/事务组合；Boot 4 没有该构件。
- native image、JPMS、不同 servlet/reactive 栈等未在 POM/测试中形成证据的组合。

## 升级检查区域

- Java：反射与泛型转换（`mapping/`、`toolkit/ClassTypeUtil`）、代理、日期/UUID codec、编译插件与 Javadoc。
- Boot/Spring：两个 starter 的自动配置入口、Mapper scanner/factory bean、属性绑定、事务、AOP、可选敏感词；Boot 2/3/4 必须分别验证。
- Driver：`DefaultExecute`、`SessionExecute`、`Filters`/`BuildCondition`、聚合 pipeline、codec、DBRef、事务与 `FillField` internal API。
- Solon：`XPluginAuto`、Solon 配置资源、Bean/切面注册与 Mapper 注入。
- 任一升级都应先更新静态版本表，再按 `TESTING.md` 执行矩阵；依赖解析或编译成功不能替代行为验证。
