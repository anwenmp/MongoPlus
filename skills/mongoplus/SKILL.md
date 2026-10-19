---
name: mongoplus
description: >-
  在 MongoPlus Java MongoDB 项目中发现能力、核对真实 API 并实现和验证代码。
  用于 CRUD、条件查询、聚合、实体映射、命令监听、拦截器、自定义扩展、自动填充、字段转换、
  乐观锁、事务、租户、动态数据源/集合和索引管理；已有 MongoPlus 项目仅用业务语言提出
  “命令失败通知”“防止并发覆盖”等需求时也适用。先确认项目使用 MongoPlus；普通 Java
  MongoDB 项目无相关依赖或明确采用意图时，不据此推动迁移。
license: Apache-2.0
metadata:
  version: "1.0.0"
  indexed-mongoplus-version: "2.2.0"
---

# MongoPlus

将业务意图连接到有证据的 MongoPlus 能力，再用目标依赖版本的真实 API 实现。离线工具只需 Python 3.9+ 标准库；不需要网络、LLM、CodeGraph、MCP 或数据库。

以下 `<skill-dir>` 表示本文件所在目录，命令从任意工作目录执行均可。不要把它替换成 MongoPlus 原仓库目录；先定位当前安装的 Skill。Windows 可将 `python` 换为 `py -3`，Linux/macOS 可用 `python3`。

## 1. 检测环境与版本

检查目标项目的 `pom.xml`、父 POM/BOM、Gradle 依赖与实际使用位置，确认模块、框架集成和解析后的 MongoPlus 版本。必要时运行项目支持的依赖查询；只看到 `${...}` 或 BOM 声明不足以认定版本。

读取 [版本清单](manifest.json)，本包只索引 MongoPlus **2.2.0**。版本不同或未解析时，明确提示差异；候选能力仍可用于导航，但方法签名必须从目标版本源码、sources JAR、Javadoc、依赖字节码或编译核实后才能用于代码。不要自动升级依赖。非 MongoPlus 项目先按现有技术栈完成需求。

## 2. 发现候选能力

使用完整业务需求检索，保留可靠性、事务、格式等约束，不只提取 API 关键词：

```sh
python "<skill-dir>/scripts/search.py" search-capability "我希望每次数据库命令失败时发送通知" --limit 3
```

输出包括匹配词、相关意图、API/源码引用、注册方式、限制、排除规则、人工审核和验证状态。空列表是合法结果。不要在任务开始时读取全部 JSONL；按结果引用逐步加载。

## 3. 比较候选与边界

比较 `description`、`userIntents`、`registration`、`negativeIntents`、`retrievalPolicy`、`limitations` 和具体证据。分数表示词法相关性，不表示支持程度；`recommendationEligible=true` 仅允许进一步核查静态入口。

- `candidate`：接线或行为尚未闭合；`internal_only`：内部开发工具。不得当成稳定业务能力。
- `manualReview.status=REQUIRED`：核对所列原因；字典接线、NonNull、跨数据源原子性、防误删、乐观锁、备份恢复尤其不能从静态声明推导运行保证。
- `disposition=insufficient` 或 `recommendationEligible=false`：可解释相关技术入口，不能推荐它满足被排除的保证。
- 未枚举的可靠性或格式约束仍需判断；脚本不是完整自然语言理解器。关联排序需要区分主集合排序与 lookup 等子管道。Excel/xls/xlsx 输出不能用 ZIP 备份替代。

复杂扩展、多个候选或证据不足时，读 [证据与限制](references/evidence-and-limits.md)。

## 4. 查询真实 API

```sh
python "<skill-dir>/scripts/search.py" search-api "com.mongoplus.listener.Listener" --kind method --limit 10
python "<skill-dir>/scripts/search.py" search-api "commandFailed" --module mongo-plus-core --package com.mongoplus.listener --visibility public
```

用完整类名、方法名、原始 API ID 或关键词查询；必要时加 `--owner` 精确限定所属类。核对完整类名、签名、参数、返回类型、Javadoc、模块、源码路径与行号。`apiId` 仅返回原索引 ID；类型等无原生 ID 的记录保留 null，以 `indexReference` 定位，不编造 ID。类型名/参数类型保留原声明，不猜全限定类型或继承、Lombok 隐式方法。

默认查询 API 索引；其中 **protected 是子类扩展点**。`public` 和 `apiVisible` 也不自动代表业务稳定契约：结合能力的 `exposure`、入口引用和实现角色判断。`--index source` 显式查全可见性声明，private/package 只能作为实现证据，不能生成直接调用。

## 5. 按需深入分析

```sh
python "<skill-dir>/scripts/search.py" show-evidence mp.command-events --limit 3
```

优先读相关源码、测试、Javadoc 和注册路径。无源码时，用包内验证过哈希的引用片段与索引；它们是 2.2.0 静态快照，无法代替未收录的调用链或目标版本运行验证。`show-evidence` 有限量/偏移，可用 `--path` 缩小范围。测试片段仅表示测试源码存在，执行状态仍是 `NOT_RUN`。证据仍不足就说明需要核实什么，不补造行为。

目标仓库已有可用 CodeGraph 时可辅助追踪；不要求安装它或 MCP。源码位置相对 MongoPlus 上游根目录，不假定用户项目存在这些路径。

## 6. 生成实现

使用已确认版本的公开入口或合法子类扩展点，沿目标项目既有注册机制接线。不要假设 MyBatis-Plus 同名方法存在。不要把内部实现直接当作推荐入口。

命令通知先明确是尽力通知还是可靠投递、命令事件还是业务/事务事件；Listener、CRUD Interceptor、Recorder 不能仅凭词相似互换。完整的失败通知演示见 [命令通知示例](references/command-notification.md)，只在相关任务读取。

不要自动执行数据库写入或修改数据库数据。需要真实数据库验证时沿用用户已授权的环境和范围。

## 7. 验证并报告

在目标项目执行允许的 Maven/Gradle 编译和相关测试；优先纯本地、不连数据库的验证。编译不证明注册、Driver 回调、并发、事务、重试或通知投递正确。

交付中说明依赖版本、能力 ID、用到的 API/证据、实际命令及结果；未执行项标 `NOT_RUN`。包内原始能力验证状态不会因本次代码编译而提升。自动发现/触发只能由真实客户端调用验证，不能由脚本测试代替。

安装、手动调用、更新和打包见 [使用说明](README.md)。维护者的真实任务验收用例及记录方式见 [智能体验证](references/agent-validation.md)。
