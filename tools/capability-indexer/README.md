# MongoPlus 能力发现索引辅助工具

仅消费已有声明 JSONL、人工能力清单和本地证据；不解析 Java、不修改业务模块、不修改或触发已有 API 生成器，不需要第三方依赖、网络、数据库或 LLM。Python 3.10+。

## 文件与入口

- [DESIGN.md](DESIGN.md)：生成前的分类设计、去重边界和实施依赖。
- [catalog.py](catalog.py)：人工审查的能力描述、状态、真实符号选择器、注册说明和边界，是内容维护源。
- [doc-review.json](doc-review.json)：本次已检查官网页面、章节、哈希、正文关键词排查和差异笔记；不是完整网站快照。
- [review-baseline.json](review-baseline.json)：已审查输入、源码、测试和注册资源哈希；用于阻止证据漂移后静默沿用旧结论。
- [query_index.py](query_index.py)：流式过滤现有 API/source 声明，并可定向显示源码。
- [generate.py](generate.py)：解析选择器、校验引用，生成能力产物和检索评估。默认全部离线。
- [validate.py](validate.py)：独立复核 JSONL、真实 indexLine/ID、源码摘要、测试方法、注册资源、状态、分类和 Markdown 链接。
- [search.py](search.py)：很小的离线词法检索基线，保留限制、候选/内部状态；相关分数不是支持程度。
- [retrieval-cases.json](retrieval-cases.json)：原有 32 条冻结需求保持不变。
- [retrieval-validation-cases.json](retrieval-validation-cases.json)：首批新增 18 条，首次独立运行 16/18；H03/H07 促成进一步修正后归为验证集。
- [retrieval-holdout-cases.json](retrieval-holdout-cases.json)：另存 16 条最终独立保留集，最终规则冻结后首次运行，未用于规则调整。
- [retrieval-evaluation-protocol.json](retrieval-evaluation-protocol.json)：保存首次独立结果和两轮冻结哈希，避免把调参样本计入最终保留集。检索器不读取用例 expected。
- [test_search.py](test_search.py)：原有/新增用例、全部排名证据安全、无副作用和旧字段兼容的离线测试。

从仓库根目录运行 PowerShell：

```powershell
$env:PYTHONIOENCODING = 'utf-8'
python tools/capability-indexer/query_index.py 'handlers.ReadHandler' --source --kind type --body --limit 1
python tools/capability-indexer/generate.py
python tools/capability-indexer/validate.py
python -m unittest discover -s tools/capability-indexer -p test_search.py -v
python tools/capability-indexer/search.py '每次数据库命令成功后通知我' --limit 3
```

只在人工重新核对变更源码、启用流程和文档后，显式记录新审查基线：

```powershell
python tools/capability-indexer/generate.py --record-review --review-date 2026-10-19
python tools/capability-indexer/validate.py
```

日期应填写实际人工复核日期，官网检查日期另外保留。这不会运行测试或验证行为，仅记录人工复核后的输入。当前脚本不会从源码自动发现、推导状态或改写描述。

## 产物

四个主要文件：`generated/capability-index.jsonl`、`capability-index.md`、`undocumented-capabilities.md`、`capability-review.md`。

辅助验收文件：`capability-retrieval-tests.jsonl/.md` 保存每条需求的前三命中、真实 API/源码/测试、人工评议与失败；`capability-manifest.json` 保存输入统计、产物哈希及验证摘要。既有 API 索引和模块 Markdown 保持原样。

## 数据契约 1.0

每行一个能力对象，UTF-8，无跨行引用前置要求：

| 字段 | 含义 |
| --- | --- |
| id | 人工维护的稳定 `mp.<slug>`；不含行号、版本或实现类名，后续重命名应保留迁移说明 |
| repositoryVersion / schemaVersion | 当前根 POM 版本与能力索引格式版本；提交和输入哈希另在 manifest 中 |
| category | primary/secondary，必须来自 DESIGN 中的同一分类表 |
| userIntents / keywords | 自然语言意图与同义词；不是方法列表 |
| apiReferences | 已有 API JSONL 的 module、recordType、qualifiedName、indexLine；方法/字段附真实 recordId 与 signature，类型没有虚构 ID |
| sourceReferences | 真实源码声明及行范围、作用、片段摘要；private/包级符号为 internalSymbol=true；所有实现引用 usage=implementation_evidence |
| testReferences | 当前真实测试文件、方法、起始行与验证范围；executionStatus=NOT_RUN，不冒充执行结果 |
| docReferences / documentationStatus | 已审查官网说明，及与功能证据独立的文档覆盖状态 |
| registration / registrationReferences | 如何启用；资源文件精确 anchor 与行号；Java 注册实现列在 sourceReferences 中 |
| limitations / openQuestions | 已确认边界、未闭合的保证与人工验证事项 |
| evidenceStatus | documented/source_verified/candidate/internal_only/deprecated；定义见报告，状态为人工判断 |
| exposure | public_api、public_extension、annotation、annotation_configuration、annotation_extension、configuration、optional_module、container_integration、internal_implementation、developer_tool；这是入口类型，不是稳定性保证 |
| confidence / verification | 针对所写结论的证据可信度、静态核验范围和测试未执行边界 |
| discoveryEligible | 仅 documented/source_verified 为 true；仍必须阅读 limitations，不代表用户的全部需求已满足 |
| negativeIntents / relatedCapabilities | 已知不能由该能力充分满足的需求及相关能力；不虚构缺失功能 |
| manualReview（可选） | status=REQUIRED、reasons；明确接线和运行边界必须人工审核，不提升静态证据为行为保证 |
| retrievalPolicy（可选） | requiredAny 前置锚点、excludedAny 条目排除、unsupportedIntentGroups 不足证据导航、noMatchIntentGroups 无支持需求；各 groups 内组间 AND、组内 OR |

## 验证与限制

生成时先校验所有选择器，再检查输入哈希；错误会失败退出，不会借旧行号生成成功报告。默认重复生成结果应完全一致。API `id` 在跨模块同名类之间必须和 module 一起解释。

检索保持 schemaVersion=1.0 和列表返回值，既有字段与稳定 ID 保留；新增字段可选。无合适能力可返回 `[]`。备份必须有 JSON/ZIP/备份恢复锚点；目录明确不支持的工作簿输出需求直接无匹配，避免其他能力部分命中。负意图可提升所属能力的导航排序，但全部结果 recommendationEligible=false；可靠通知入口因此只能作不足证据导航。

显式“不涉及/不需要”子句按标点截断后不参与匹配；这不是通用自然语言否定解析。中文近义表达、复合需求、双重否定和未枚举的强保证仍有限。负意图对整项复合需求保守阻断；工作簿输出排除也不证明所有其他未支持格式均会被识别。

`recommendationEligible` 只表示条目静态证据类别允许进一步核查，**不是对本次需求的支持判定**；manualReview=REQUIRED 必须继续核对边界。首次独立失败结果完整保留，后续 Skill 应读取限制、候选状态、负例及真实源码，不能把检索排名当执行许可或兼容承诺。

源码/配置/测试变更、官网修订、真实用户反馈都应触发人工增量复核。严禁用 `--record-review` 代替阅读源码，或仅因索引记录仍存在就沿用旧功能结论。
