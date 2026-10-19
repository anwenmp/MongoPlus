# MongoPlus Agent Skill 1.0.0

一个通用 `mongoplus` Skill，面向 Codex、Claude Code 及兼容 Agent Skills 的智能体。当前快照仅支持 **MongoPlus 2.2.0**：48 个稳定能力 ID、5,236 条 public/protected API、6,678 条完整声明索引。34 项 documented、9 项 source_verified、3 项 candidate、2 项 internal_only；6 项要求人工审核。静态证据不等于运行验证。

## 安装

需要 Python 3.9+，只用标准库。获取发布 ZIP 后解压，保留整个 `mongoplus/`。从解压目录的父目录执行：

```sh
python mongoplus/scripts/distribute.py validate
python mongoplus/scripts/distribute.py install --client codex --project /path/to/your-project
python mongoplus/scripts/distribute.py install --client claude --project /path/to/your-project
```

将 `/path/to/your-project` 换成目标项目目录；Windows 可传带引号的相对路径并用 `py -3`。分别安装到目标项目的 `.agents/skills/mongoplus/` 和 `.claude/skills/mongoplus/`，内容来自同一个源包。目录已存在时拒绝覆盖：先审阅和保留旧目录，再安装新版本；不自动删除用户文件。

安装后在目标项目启动客户端的新会话，查看技能列表。Codex 手动调用 `$mongoplus`，Claude Code 手动调用 `/mongoplus`。自动发现由客户端决定，本包允许隐式调用，但并不保证每次自然语言提示都会触发。安装离线可完成；运行智能体本身遵循客户端的模型连接要求。

官方安装/规范来源：[Agent Skills 规范](https://agentskills.io/specification)、[OpenAI Skills 文档](https://learn.chatgpt.com/docs/build-skills)、[Claude Code Skills 文档](https://code.claude.com/docs/en/skills)。

## 触发与手动调用

已有 MongoPlus 项目中可直接提出：

- “我希望每次数据库命令失败时发送通知。”
- “联表取每位顾客最近购买，关联分支先排序再 limit。”
- “每次保存时填充当前操作者和时间，如何接入现有项目？”

显式调用示例：

```text
Codex: $mongoplus 用当前依赖版本实现命令失败通知，先比较扩展点并说明可靠性边界。
Claude Code: /mongoplus 检查当前项目版本，为订单实体实现条件查询并运行本地编译。
```

没有 MongoPlus 依赖的普通 MongoDB Java 项目不会仅因任务相似就被要求迁移。

## 离线检索

在本源包父目录执行；安装后将 `mongoplus` 替换为实际技能目录：

```sh
python mongoplus/scripts/search.py search-capability "我希望每次数据库命令失败时发送通知" --limit 1
python mongoplus/scripts/search.py search-api "commandFailed" --owner com.mongoplus.listener.Listener --kind method --visibility public
python mongoplus/scripts/search.py search-api "intercept" --module mongo-plus-core --package com.mongoplus.interceptor --kind method
python mongoplus/scripts/search.py search-api "doConvert" --index source --visibility protected
python mongoplus/scripts/search.py show-evidence mp.command-events --limit 2
```

查询输出为 UTF-8 JSON。能力排序原样复用上游检索器；API 查询是字面量、大小写不敏感的 AND 查询，匹配 ID、类型、名称、签名和 Javadoc。`--package` 包含子包；`--module`、`--owner` 为精确过滤。`--kind` 支持 type/method/constructor/field，`--visibility` 支持 public/protected/private/package-private。`--limit` 为 1–50；默认能力 3 项、API 10 项。无匹配返回 `[]` 且退出 0；参数错误或缺失文件退出非 0。详见 [工作流程](SKILL.md) 和 [证据边界](references/evidence-and-limits.md)。

## 验证状态

66 条既有离线案例复用并在本包再次执行；它们验证排序、排除和不足证据标记。结构、独立安装、打包和脚本测试的实际结果见仓库交付报告。

Codex/Claude 的真实发现、自动触发、检索工具调用、功能选择、生成代码编译、数据库运行测试均须分别记录。当前真实客户端集成测试为 **NOT_RUN**；不能用脚本通过替代。至少 10 个真实任务及逐项记录模板见 [智能体验证](references/agent-validation.md)。命令通知 [完整示例](references/command-notification.md) 是维护者按真实 API 编写的演示，其编译/本地回调验证与智能体自动调用是不同层次。

## 数据更新与重新分发

用户离线使用本包不需要原仓库。维护者在 MongoPlus 仓库根目录执行：

```sh
python tools/capability-indexer/validate.py
python tools/mongoplus-skill/update.py
python -m unittest discover -s tools/mongoplus-skill -p "test_*.py" -v
python skills/mongoplus/scripts/distribute.py pack --output target/mongoplus-skill-1.0.0.zip
```

打包输出必须是新文件，不能位于 Skill 目录内部。固定文件顺序、ZIP 时间、权限和压缩参数；相同输入及同一 Python/zlib 环境重复打包字节一致。更新只复制既有 generated 产物、原检索器、冻结案例与被引用的源码范围，不重新调用 LLM、不运行 JavaParser、不重建能力索引。上游引用、哈希、版本或稳定 ID 不一致会拒绝刷新。Skill 安装包不包含此维护者更新工具；更新需要取得新的完整发布包。

维护者只维护仓库 `skills/mongoplus/` 一份源包；不要分别手工改 Codex/Claude 副本。需要更新上游索引时，使用原索引工具自身的说明及审核流程，再执行上述复制和验证步骤。
