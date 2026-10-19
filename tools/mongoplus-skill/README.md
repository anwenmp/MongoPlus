# MongoPlus Skill 交付与维护记录

验收日期：2026-10-19。开发范围：一个通用、可独立安装的 `mongoplus` Skill；没有修改 MongoPlus 业务源码、API 索引生成器或能力算法，也没有开发 MCP Server。

## 交付目录

```text
skills/mongoplus/                         唯一维护源包
├── SKILL.md                              完整工作流程与 YAML 元数据
├── README.md                             安装、自动/手动触发、查询、更新与边界
├── LICENSE
├── manifest.json                         版本、48 个稳定 ID、来源与逐文件哈希
├── agents/openai.yaml                    可选 Codex 展示信息，允许隐式调用
├── scripts/
│   ├── search.py                         search-capability / search-api / show-evidence
│   ├── distribute.py                     validate / install / pack
│   └── _vendor/search.py                 原检索器，逐字节相同
├── data/
│   ├── generated/
│   │   ├── api-index.jsonl
│   │   ├── source-index.jsonl
│   │   ├── capability-index.jsonl
│   │   └── capability-manifest.json
│   ├── evidence.jsonl                    227 段带来源与哈希的引用片段
│   └── regression/
│       ├── retrieval-cases.json           32 条原始用例
│       ├── retrieval-validation-cases.json 18 条验证用例
│       └── retrieval-holdout-cases.json   16 条保留用例
├── references/
│   ├── evidence-and-limits.md
│   ├── command-notification.md
│   ├── agent-validation.md
│   └── agent-cases.json                  14 个真实任务，分项状态 NOT_RUN
└── assets/command-notification/
    ├── pom.xml
    └── src/
        ├── main/java/example/CommandFailureListener.java
        ├── main/java/example/CommandNotificationConfiguration.java
        └── test/java/example/CommandFailureListenerSmokeTest.java

tools/mongoplus-skill/                    维护工具，不随 Skill 安装
├── README.md                             本交付记录
├── update.py                             从已审核产物确定性更新，不运行扫描器
├── test_skill.py                         结构、检索、API、独立安装和打包回归
├── verify_example.py                     使用显式依赖 classpath 编译/运行本地样例
└── example-retrieval.json                 两条实际 CLI 命令的完整 JSON 输出

target/mongoplus-skill-1.0.0.zip           可直接解压和安装的发布包
```

源目录另有 `.gitignore` 排除 Python 缓存；不进入发布包。发布 ZIP 共 24 个文件（23 个载荷 + manifest），757,414 字节，解压约 10.5 MB。源码索引与完整 API 数据只供脚本读取，智能体无需一次加载。

完整入口：[SKILL.md](../../skills/mongoplus/SKILL.md)。安装包 SHA-256：

```text
0003dbf13ddf24e5c944f39644a9b721d57b83d1a62b42c44971bef7b7013548
```

## 版本与覆盖

索引版本仅 **MongoPlus 2.2.0**，Skill 版本 1.0.0。上游审核提交为 `a93a37684f73394679e47b21157479eb0222d512`；它是来源元数据，不代表当时工作区干净。文件哈希与已审核生成产物才是本包快照的精确标识。

48 个稳定能力 ID 全部保留：34 documented、9 source_verified、3 candidate、2 internal_only；6 项 `manualReview.status=REQUIRED` 原样保留。5,236 条 API 声明、6,678 条完整声明，覆盖 9 个含源码模块。227 段证据包括现有能力引用的实现、注册/测试声明以及通知示例的 6 个明确补充声明；不会修改能力定义或排名。

本次开始时选定的 41 个文件（包括既有 generated/tools 产物及两处 Java 改动），逐文件 SHA-256 复核无变化。未提交、暂存、推送或自动安装到用户真实项目。

## 安装与调用

以下命令从 MongoPlus 仓库根目录执行；发布 ZIP 解压后用 `mongoplus/` 替换 `skills/mongoplus/`。使用 Python 3.9+ 标准库：

```sh
python skills/mongoplus/scripts/distribute.py validate
python skills/mongoplus/scripts/distribute.py install --client codex --project <目标项目目录>
python skills/mongoplus/scripts/distribute.py install --client claude --project <目标项目目录>
```

分别生成 `.agents/skills/mongoplus/` 和 `.claude/skills/mongoplus/`。完整复制，目录已存在则失败，不递归删除或覆盖用户文件。Windows 路径含空格时加引号；也可用 `py -3`。安装后在目标目录启动新会话，Codex 手动 `$mongoplus`，Claude Code 手动 `/mongoplus`。

自动发现示例：“我希望每次数据库命令失败时发送通知。”前提是已有 MongoPlus 项目；普通 Driver 项目不会被要求迁移。自动选择最终取决于客户端，需独立验收。

目录及发现规范依据：[OpenAI Skills 文档](https://learn.chatgpt.com/docs/build-skills)、[Claude Code Skills 文档](https://code.claude.com/docs/en/skills)、[Agent Skills 规范](https://agentskills.io/specification)。完整说明在 [Skill README](../../skills/mongoplus/README.md)。

## 实际检索输出

本次实际执行：

```sh
python skills/mongoplus/scripts/search.py search-capability "我希望每次数据库命令失败时发送通知" --limit 1
python skills/mongoplus/scripts/search.py search-api commandFailed --owner com.mongoplus.listener.Listener --kind method --visibility public
```

第一条真实输出的相关字段节选：

```json
{
  "id": "mp.command-events",
  "name": "数据库命令事件、回调与日志",
  "score": 35.8124,
  "evidenceStatus": "documented",
  "disposition": "scoped_static_evidence",
  "recommendationEligible": true,
  "matchedKeywords": ["通知"],
  "userIntents": [
    "每次数据库命令执行成功后发送通知",
    "寻找可靠通知相关回调入口，但投递保证需另行设计",
    "统计数据库命令耗时并记录失败",
    "开启数据库命令日志及可执行语句格式化"
  ],
  "limitations": [
    "命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。",
    "已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。"
  ]
}
```

第二条真实输出节选：

```json
{
  "apiId": "mongo-plus-core:com.mongoplus.listener.Listener#commandFailed(CommandFailed)",
  "qualifiedName": "com.mongoplus.listener.Listener",
  "signature": "void commandFailed(CommandFailed commandFailed)",
  "parameters": [{"name": "commandFailed", "type": "CommandFailed", "varArgs": false, "annotations": []}],
  "returnType": "void",
  "javadocSummary": "处理命令失败信息",
  "sourcePath": "mongo-plus-core/src/main/java/com/mongoplus/listener/Listener.java",
  "line": 44,
  "endLine": 44,
  "indexReference": {"indexPath": "generated/api-index.jsonl", "indexLine": 2714}
}
```

完整原始输出保存在 [example-retrieval.json](example-retrieval.json)，含 API 引用、人工审核、排除条件和验证状态。`recommendationEligible=true` 只表示静态入口可供进一步核实，不能据此承诺用户的可靠性要求。

## 验证记录

| 层次 | 本次实际执行 | 结果与边界 |
| --- | --- | --- |
| 上游引用 | `python tools/capability-indexer/validate.py` | PASS；48 能力、429 处引用、166 个已审核输入、66 案例；不运行 Java/数据库 |
| 上游回归 | `python -m unittest discover -s tools/capability-indexer -p test_search.py -v` | 5/5 测试通过，其中复用全部 66 案例 |
| Skill 回归 | `python -m unittest discover -s tools/mongoplus-skill -p "test_*.py" -v` | 12/12 通过；66 案例逐案保留排名/排除/状态行为，与原检索器输出一致 |
| YAML | JDK 21 + 已缓存 SnakeYAML 2.4 解析 frontmatter 与 openai.yaml | PASS；description 229 字符；系统 skill-creator quick_validate 因缺 PyYAML 未完成，未记作成功 |
| 独立安装 | 测试复制到临时 Codex/Claude 项目目录，以 `python -I`、外部 CWD 启动 | PASS；中文和空格路径可用；不读取原仓库 generated；重复安装拒绝覆盖 |
| 完整性 | manifest 校验 + 修改临时安装包数据后的拒绝校验 | PASS；源包与索引哈希一致，篡改副本可检测 |
| 可重复更新 | 连续执行 update.py，对比 manifest 与全部文件哈希 | PASS；没有重建上游索引 |
| 可重复打包 | 相同环境两次打包、解压校验 | PASS；ZIP SHA-256 一致，输出不包含缓存和 target |
| Java 示例 | JDK 21 `javac --release 8` 编译三个 Java 文件 | PASS；使用真实缓存的 MongoPlus core 2.2.0、Driver/BSON 5.13.0、SLF4J 2.0.13、Spring 6.2.2 |
| 本地回调 smoke | `CommandFailureListenerSmokeTest.main` | PASS；仅手工构造事件，验证载荷和适配器异常隔离；无数据库、无真实通知 |
| Maven 示例 | `mvn -o -f target/mongoplus-skill-example/pom.xml test-compile` | 未通过：实际缓存缺少 spring-jcl:6.2.2；改用上述直接 javac 编译完成验证 |
| Codex/Claude 集成 | 未启动新的模型会话 | NOT_RUN；Codex CLI 0.163.0-alpha.10.2 可用，Claude CLI 未发现；无可确认无外部 LLM 调用的隔离配置，本任务禁止外部 LLM API |
| Spring 启动、Driver 回调、真实通知、数据库行为 | 未执行 | NOT_RUN；未引入数据库或修改业务数据 |
| Linux/macOS 实机 | 未执行 | NOT_RUN；路径与脚本使用可移植标准库，当前只在 Windows 实测 |

Java 示例可通过维护工具复验：

```sh
python tools/mongoplus-skill/verify_example.py --java-home <JDK目录> --classpath <已解析依赖JAR路径串>
```

classpath 需包含表中版本的 core、mongodb-driver-core、bson、slf4j-api、spring-context、spring-beans、spring-core。Windows 用分号，Linux/macOS 用冒号。验证时 SLF4J 提示无 provider，并使用 NOP；没有验证日志后端。

`git diff --check` 通过；新增文本无行尾空白。未运行业务 reactor，因为业务源码没有改动。

14 个真实任务覆盖显式 API、业务需求、源码确认但文档不足、扩展点比较、不支持、运行证据不足和版本不一致。它们是 [手动验收方案](../../skills/mongoplus/references/agent-validation.md)，[分项状态](../../skills/mongoplus/references/agent-cases.json) 全部 NOT_RUN，不能当成已完成的自动触发验证。

## 通知完整示例

业务失败通知 → 环境确认 2.2.0 + Boot → 检索 `mp.command-events` → 比较 Listener/Interceptor/Recorder → 查询 Listener.commandFailed、CommandFailed.getThrowable、BaseCommand.getCommandName → 查看包内同步分发与 Bean 收集证据 → 实现 Listener 与 Spring Bean → javac 编译和本地回调 smoke。

完整说明及可复制源码见 [command-notification.md](../../skills/mongoplus/references/command-notification.md)。核心调用来自真实 API：

```java
@Override
public void commandFailed(CommandFailed commandFailed) {
    Throwable failure = commandFailed.getThrowable();
    String failureType = failure == null ? "unknown" : failure.getClass().getName();
    try {
        sink.notifyFailure(commandFailed.getCommandName(), failureType);
    } catch (RuntimeException notificationFailure) {
        LOGGER.warn("命令失败通知适配器异常，类型={}", notificationFailure.getClass().getName());
    }
}
```

`sink` 是使用者的业务通知适配器。没有编造 MongoPlus 通知发送 API；没有配置或实际发送外部消息。此方案支持尽力回调，不提供持久化、重试或可靠投递保证；更早 Listener 异常、同步阻塞、重复初始化等仍需核实。

## 更新和重新打包

```sh
python tools/capability-indexer/validate.py
python tools/mongoplus-skill/update.py
python -m unittest discover -s tools/mongoplus-skill -p "test_*.py" -v
python skills/mongoplus/scripts/distribute.py pack --output target/mongoplus-skill-next.zip
```

包路径必须是新文件；已有文件拒绝覆盖。update 消费既有 generated 和审核基线，保留 48 ID、复制原检索器、验证引用后切取明确来源范围；不调用 LLM、不扫描全仓库。若上游源码/审核基线漂移，应先按原工具流程人工复核，不能直接绕过验证。

## 已知限制

只索引 2.2.0；其他版本须核实，不自动升级。能力检索是冻结词法规则，分数不代表需求支持或运行保证。API 索引不做符号解析、不展开继承/Lombok 隐式方法；类型等没有原生 ID 时保留 null。公共实现声明也不能自动当成业务契约。

人工审核、候选和内部工具状态均保留。包内是有限静态证据，不能替代目标项目编译、容器接线、Driver/事务/并发/可靠通知运行验证。真实客户端集成、Linux/macOS 和数据库验证仍需后续按各自环境执行。
