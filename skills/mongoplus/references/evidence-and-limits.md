# 证据解释与运行边界

按能力结果读取本页，不需要预加载全部数据。

## 证据链

`manifest.json` 固定 Skill 文件、上游输入、能力 ID 和版本。`data/generated/capability-manifest.json` 保留上游统计与审核来源；包内三个 JSONL 与上游逐字节相同。`scripts/_vendor/search.py` 是原检索器的逐字节副本，仅由包装器导入；其上游命令行入口仍依赖原目录布局，请通过 `scripts/search.py search-capability` 调用。

`apiReferences` 是候选入口声明，`sourceReferences` 是实现证据，`registrationReferences` 是注册资源，`testReferences` 只表示测试源码存在。所有 `indexPath` 均相对包内 `data/` 解析，例如 `generated/api-index.jsonl` 对应 `data/generated/api-index.jsonl`。`indexLine` 是 JSONL 行，`line/endLine` 是上游源码行，两者不能混用。

源码片段只打包已审核能力引用的范围及通知示例明确需要的少量声明，不是完整仓库。示例补充引用列在版本清单 `exampleReferences`，不改变能力索引。用 `show-evidence <能力ID> --path <路径片段> --limit 3 --offset 0` 获取正文及 SHA-256；类型引用可能包含完整类。测试引用没有方法结束行，仅保留声明行，不声称包含完整测试。可以依据上游版本查找源文件；不同版本不能只沿用旧行号。

API 索引不展开继承、Lombok 生成方法或隐式成员，简单参数类型也不做符号解析。未检索到方法不等于证明整个框架不支持需求；沿相关类型、父类、Javadoc 或目标依赖继续核实。反过来，public 实现类被收录不等于官方业务扩展契约。

## 状态判定

| 字段 | 使用方式 |
| --- | --- |
| documented | 已审核文档与静态实现的描述范围；不是运行证明 |
| source_verified | 已审核静态实现，官网记录范围有限；可沿真实入口核实 |
| candidate | 发现候选声明，但启用/行为未闭合；不能默认推荐 |
| internal_only | 内部生成/开发工具；不当业务功能推荐 |
| manualReview=REQUIRED | 必须核对 reasons，保留未解决问题，不能承诺相关运行行为 |
| recommendationEligible | 仅排序器的静态证据导航标记，非完整需求支持判断 |
| runtimeTestStatus / databaseValidationStatus | 上游能力均为 NOT_RUN；不因检索命中或代码编译而改变 |

人工审核可以先继续查看证据、设计验证并说明范围；字段本身不代表每次检索都要打断用户请求批准。遇到缺少实际运行证据的要求，说明需要的验证条件和未确认结论。

## 必须保留的限制

- 命令 Listener 的静态同步回调不证明可靠队列、持久化、自动重试、事务提交后触发或恰好一次。强可靠通知只能定位入口并说明证据不足。
- 字典回写接线、NonNull 校验、跨源原子性、防误删、乐观锁和备份恢复带人工审核要求。索引中的 limitations/reasons 是逐项判断依据。
- Wrapper-only 更新、实体映射、自动填充、加密等不能假定共享同一条路径；事务、多源、租户及子管道组合必须核对对应入口。
- 本地检索采用冻结的词法规则，不完整理解双重否定、未枚举保证或任意复合需求。保留原需求，用多个查询定位候选后由智能体比较。
- Excel/xls/xlsx 输出无支持匹配时应说明缺少能力证据；不得把 ZIP/JSON 备份包装成工作簿导出。

## 更新

维护者在原仓库执行现有索引工具及引用验证后，通过 `tools/mongoplus-skill/update.py` 复制已验证产物。Skill 不携带扫描器，不联网补建索引；版本清单与全部文件哈希一起更新。稳定 ID 变化会拒绝更新，需要明确的迁移处理。
