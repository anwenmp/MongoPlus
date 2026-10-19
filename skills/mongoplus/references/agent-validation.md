# 真实智能体集成验收

当前状态：**NOT_RUN**。环境提供 Codex CLI，但本任务禁止调用外部 LLM API；没有可确认完全本地、无外部模型调用的隔离智能体执行配置，Claude Code CLI 也未发现。没有启动新的模型会话。当前编写 Skill 的会话不等于新安装 Skill 的自动发现验证。

## 操作步骤

1. 准备隔离的使用者项目，只安装完整 Skill；让它无法访问 MongoPlus 原仓库的 generated/tools。配置目标依赖，记录客户端版本、模型、权限与启动目录。
2. 启动新会话，查看技能列表，记录发现情况。自动触发测试只发送业务提示，不写 `$mongoplus`、`/mongoplus` 或 Skill 路径，也不提前读取 SKILL.md。手动调用另开会话测试。
3. 每个任务清空前一会话上下文，保留工具轨迹和生成文件。核对是否读 Skill、调用能力脚本、查询真实 API、比较限制；不要仅依据回答提及 Skill 名称判成功。
4. 对适用任务编译生成的代码并运行纯本地测试。无匹配/不兼容任务可正确拒绝或继续核实，不强求产出代码；将不适用项记 N/A，并说明原因。
5. 数据库行为测试仅在获得测试环境和授权后执行；不把编译与 smoke 适配器测试记为真实 Driver 或可靠投递测试。

## 任务矩阵

所有任务详细输入、依赖前提、预期行为和分项状态保存在 [agent-cases.json](agent-cases.json)。预期用于人工评估，不传给被测智能体。

| ID | 类型 | 业务任务 | 关键验收 |
| --- | --- | --- | --- |
| A01 | 直接 API | QueryWrapper 查询成年用户并排序 | 查目标版本真实查询入口；代码编译 |
| A02 | 业务需求 | 每次命令失败通知 | command-events；Listener；不承诺可靠送达 |
| A03 | 源码确认 | 未声明字段收进扩展属性 Map | extra-fields；区分读写证据 |
| A04 | 比较扩展 | 执行前校验参数，失败统计 | 比较 Interceptor/AdvancedInterceptor/Listener/Recorder |
| A05 | 不支持 | 导出 Excel/xls/xlsx | 无匹配或无支持证据；不得推荐 ZIP 备份 |
| A06 | 候选 | 字典码回写名称 | dictionary-bind candidate/REQUIRED；不虚构接线 |
| A07 | 证据不足 | 事务提交后恰好一次可靠通知 | 技术入口可导航，全部 recommendationEligible=false |
| A08 | 版本不一致 | 2.1.9 项目实现通知 | 提示 2.2.0 快照差异，核实目标 API 后写代码 |
| A09 | 排除 | 普通 Driver 项目做查询 | 不强迫迁移 MongoPlus |
| A10 | 子管道 | 关联客户最近购买，先排序再 limit | nested-pipeline；不能只做主集合排序 |
| A11 | 人工审核 | 旧版本不能覆盖新数据 | optimistic-lock/REQUIRED；核对运行边界 |
| A12 | 人工审核 | 跨数据源一起提交，要原子性 | cross-source-atomic 候选/不足证据，不承诺分布式事务 |
| A13 | 候选 | NonNull 自动拒绝空字段 | nonnull-validation，核实运行接线 |
| A14 | 索引 | 已有实体创建复合索引 | 查询真实索引入口，不自动执行数据库变更 |

## 记录要求

每案分别记录 `discovered`、`automaticTrigger`、`retrievalScriptExecuted`、`apiRead`、`correctChoice`、`codeCompiles`、`runtimeTests`，值为 PASS/FAIL/NOT_RUN/N/A，附命令或轨迹路径。汇总结果不能由其他层级推导。冻结 66 条词法案例只属于离线检索层，与本矩阵无替代关系。
