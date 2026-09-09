# Expression 参数 semantic evidence 审计（2026-09-07）

后续补充：2026-09-09 的 [Accumulators 输出字段名专项审计](ACCUMULATOR_OUTPUT_FIELD_AUDIT.md)
为已收录 Accumulators 的 84 个输出名称参数补充 OUTPUT_FIELD_NAME，复用 Stage Audit concept。
下文统计为原 expression 语义审计结果；TSV 中这 84 行现标为 VALUE / OUTPUT_NAME_ADDED，
其余记录及原有 PIPELINE_EXPRESSION 语义保持不变。

以本轮开始时的工作树及正式 Pipeline Index 为基线，检查全部 **49 个 Expression MethodFamily、157 个 overload、372 个参数**。逐参数回看真实声明、委托和 BSON 构造；没有改动方法签名、执行逻辑、Stage/Expression 标签或扫描入口。

新增 **127 个 `@mongoParam`**，分布在 **74 个 overload**：**100 VALUE、27 ELEMENT**。原有 34 个 Expression 参数标签保留，当前 Expression 参数标签合计 161 个；其余 211 个参数保留原分类。无参 `Accumulators.sum()` 也已核查。

## 文件与数量

| Core 源码 | 新增 VALUE | 新增 ELEMENT | 合计 |
|---|---:|---:|---:|
| [Accumulators.java](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Accumulators.java) | 45 | 16 | 61 |
| [AggregateOperator.java](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java) | 12 | 3 | 15 |
| [ConditionOperators.java](../mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java) | 43 | 8 | 51 |

`Projections`（9 overload）与 `Sorts`（2 overload）已审计，无新增标签。现有 Stage 及 computed 参数标签不改动。

完整机器可读清单为 [pipeline-expression-parameter-audit.tsv](src/test/resources/pipeline-expression-parameter-audit.tsv)，373 行覆盖全部 372 个参数及 1 个无参 overload。列依次为 `declaredIn`、完整 signature、参数名、最终 scope（或 UNMARKED/NONE）、本轮状态（ADDED/EXISTING/EXCLUDED/NO_PARAMETER）、源码核查原因。该清单只用于审计与回归，Indexer 和 Pipeline 调用证明不读取它来推断 API。

## 源码判断与边界

- `multiply`、`ifNull`、`concat`、`add`、非累加器 `sum`：集合元素直接保存；varargs 先经 `Arrays.asList` 或流收集，再写入固定操作符的 BSON 操作数数组。已分别核对每个重载。`add/sum` 原有泛型 varargs ELEMENT 保留。
- `cond/condArray(Object,Object,Object)`：三个值分别原样写入 if/then/else 或三元数组，使用 VALUE。动态操作符版本只为 thenValue/elseValue 补 VALUE。
- `substrBytes`：四个 overload 的 index/count 都在操作数列表中占据独立位置，使用 VALUE；两个泛型 expression 已标记，Lambda field 保留 getter 分类。Object index/count 可以承载完整表达式；Number 版本仅能承载数值常量。
- `dateFromString`、`dateToString`、`dateTrunc`：沿各自便利重载到最终 Document，逐值核对 date/dateString、format、unit、timezone、startOfWeek、onError、onNull、binSize 的写入。`putIsNotNull` 仅省略 null，其余值原样保留。String/Integer 的声明类型限制仍有效。
- `firstN/lastN/maxN/minN`：经 `pickNAccumulator` 分别写入 input/n；`top/bottom/topN/bottomN` 经 `sortingPickAccumulator/sortingPickNAccumulator` 写入 output/n。泛型值是 VALUE，明确的 String varargs 每个元素是 ELEMENT。Lambda overload 的独立泛型 nExpression 仍可标记，但 Lambda 参数本身不标记。
- `accumulator` 的四个带参数数组 overload：initArgs/accumulateArgs 委托至最终 overload，逐 String 构造 BsonString 后写入 BsonArray，故 ELEMENT；类型仍限于 List<String>，不能宣称接受嵌套 Bson。这也符合 [MongoDB 的参数数组定义](https://www.mongodb.com/docs/manual/reference/operator/aggregation/accumulator/)。
- `concatArrays(List<?>... list)`：外层每个 vararg 是一个 List 数组表达式，ELEMENT 作用于这个外层元素，不是把 List 内的元素作为直接 Java 参数；不能传 String/Bson 作为外层 vararg。
- VALUE/ELEMENT 描述表达式位置，不放宽 Java 参数类型，也不保证具体操作符的服务端类型或上下文约束。`abs(Number)`、Number index/count 和 Integer binSize 的数值常量属于表达式值，但不能因此传入字段引用字符串。

## 未标记的参数及原因

| 参数范围 | 源码依据与决定 |
|---|---|
| 所有输出 fieldName（含 String 与 Lambda） | 用于 BsonField 名、投影键或排序键；不是表达式值。 |
| 所有 SFunction、SFunction varargs/集合 | 接收 getter 并调用 getFieldNameLineOption 转换；push varargs 还构造带输出键的 BasicDBObject。调用者传的是选择器，不能直接传 String/Bson，因此不赋予完整 expression value 语义。 |
| top/bottom/topN/bottomN 的 Bson sortBy | 写入 sortBy 排序规格，不是 expression。类型是 Bson 不构成标记依据。 |
| Projections.meta 的 metaFieldName | 被包装为 `$meta` 的 BsonString 选择符；metaTextScore/metaSearchScore/metaSearchHighlights 使用固定选择符。 |
| accumulator 的 initFunction/accumulateFunction/mergeFunction/finalizeFunction/lang | 写入 JavaScript 函数源码或语言配置。仅原样写入 BsonString 不足以证明是 expression。 |
| count 的两个泛型 expression | 虽走 accumulatorOperator，省略该参数的便利方法实际传空 Document。该槽对应无操作数的 `$count: {}`，不能给任意字段引用背书；参见 [MongoDB count accumulator](https://www.mongodb.com/docs/manual/reference/operator/aggregation/count-accumulator/)。 |
| cond/condArray 的 String ifCondition | 用来构造操作符键，必要时加 `$`，不是值。 |
| cond/condArray 动态版本的 Collection<?> ifValue | 整个集合是调用方选择的任意操作符的载荷；例如 ifCondition 为 `$literal` 时元素不求值。无法仅凭源码保证各元素都是 expression，因此不标记。其 then/else 独立确认后已补 VALUE。 |

已在既有映射阶段排除的错误委托重载（firstN 的 Lambda varargs、topN 的单 Lambda 输出等）不属于当前 157 个 overload，本轮不新增映射或参数标签。旧包 ConditionOperators 副本不改动。

## Index 与验证

| 项目 | 本轮前 | 本轮后 |
|---|---:|---:|
| PIPELINE_STAGE family / overload | 33 / 140 | 33 / 140 |
| PIPELINE_EXPRESSION family / overload | 49 / 157 | 49 / 157 |
| methodFamilies / 全部 overload | 82 / 297 | 82 / 297 |
| 显式 Stage / Expression 操作符种类 | 26 / 42 | 26 / 42 |

完整 Stage family/evidence 与 scanStatistics 深比较相等。新增的 127 个源码参数，在 overload 与 types.publicMethods 中各产生一次一致 evidence（共 254 个参数条目）。既有 semanticType 在这些条目上更新，并增加 semanticScope/conceptRef/semanticEvidence；其余参数不变。JSON 唯一的非参数差异是 concept 描述把 ELEMENT 从仅说明 varargs 补充为数组、varargs、集合；concept 规则、schema、映射、签名、可达闭包均不变。

原范围校验只能接受 varargs 的 ELEMENT。本轮最小修正允许数组及明确的 java.util.List/Collection（显式或通配 import、全限定名）。非 varargs 的数组/集合可以按实际结构标 VALUE；varargs 仍必须 ELEMENT。该校验只处理已经存在的标签，不按类型注入 evidence，未增加集合继承推导或外部 classpath 解析。

实际执行并通过：JDK 21 的 `mvn -pl mongo-plus-indexer -am -Dgpg.skip=true test-compile`，以及四个显式 self-test：MongoPlusIndexerSelfTest、MongoPlusPipelineIndexerSelfTest、PipelineExpressionSemanticsSelfTest、PipelineExpressionCoverageSelfTest。Core 三个文件去掉 Javadoc 后与本轮基线逐字相等，去掉 mongoParam 行后完整文件相等；git diff --check 通过。

两条输入 Pipeline 均 **PASS**：`$project.total.$multiply` 与 `$project.displayName.$concat/$ifNull`。分别只选集合和只选 varargs 重载验证；证明器从 Index 的操作符映射、逐参数标签、concept、声明类型、泛型及 BSON 返回类型组成参数兼容证据，不预置 Java 目标代码，不使用审计 TSV 选择方法。field reference、普通 String、nested Bson 三类输入均覆盖。移除各操作符参数 evidence、移除 concept、修改嵌套返回类型、把 ELEMENT 破坏成 VALUE 均使对应证明失败。另验证 List<String>/Number/concatArrays 的声明类型限制、无标记集合不推断及非法范围拒绝。

这是 Index 的调用参数兼容证明；未执行 MongoDB 服务端 Pipeline，也不声称已完成 Java 代码生成器。

正式文件已重新生成：[mongo-plus-pipeline-api-index.json](target/generated-resources/mongo-plus-pipeline-api-index.json)。连续两次生成逐字相同，SHA-256 均为 `E65674DF240AAF70F1EE5D57E6689DD47D9D922F1EFD0315830267F1D1C265B9`，deterministic **PASS**。本轮基线与比较结果保存在 `target/expression-audit/`，构建清理时可被删除。

## 新增标签逐 overload 清单

以下只列本轮增加的标签；全部已存在和排除项见上方 TSV。

### Accumulators

| 完整 overload | 新增 VALUE 参数 | 新增 ELEMENT 参数 |
|---|---|---|
| `accumulator(SFunction<T, ?> fieldName, String initFunction, List<String> initArgs, String accumulateFunction, List<String> accumulateArgs, String mergeFunction, String finalizeFunction)` | — | `initArgs`, `accumulateArgs` |
| `accumulator(SFunction<T, ?> fieldName, String initFunction, List<String> initArgs, String accumulateFunction, List<String> accumulateArgs, String mergeFunction, String finalizeFunction, String lang)` | — | `initArgs`, `accumulateArgs` |
| `accumulator(String fieldName, String initFunction, List<String> initArgs, String accumulateFunction, List<String> accumulateArgs, String mergeFunction, String finalizeFunction)` | — | `initArgs`, `accumulateArgs` |
| `accumulator(String fieldName, String initFunction, List<String> initArgs, String accumulateFunction, List<String> accumulateArgs, String mergeFunction, String finalizeFunction, String lang)` | — | `initArgs`, `accumulateArgs` |
| `bottom(SFunction<T, ?> fieldName, Bson sortBy, OutExpression outExpression)` | `outExpression` | — |
| `bottom(SFunction<T, ?> fieldName, Bson sortBy, String... outExpression)` | — | `outExpression` |
| `bottom(String fieldName, Bson sortBy, OutExpression outExpression)` | `outExpression` | — |
| `bottomN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, OutExpression outExpression)` | `nExpression`, `outExpression` | — |
| `bottomN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, SFunction<R, ?> outExpression)` | `nExpression` | — |
| `bottomN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, SFunction<R, ?>... outExpression)` | `nExpression` | — |
| `bottomN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, String... outExpression)` | `nExpression` | `outExpression` |
| `bottomN(String fieldName, Bson sortBy, NExpression nExpression, OutExpression outExpression)` | `nExpression`, `outExpression` | — |
| `firstN(SFunction<T, ?> fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `firstN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?> inExpression)` | `nExpression` | — |
| `firstN(String fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `firstN(String fieldName, NExpression nExpression, SFunction<T, ?> inExpression)` | `nExpression` | — |
| `firstN(String fieldName, NExpression nExpression, String... inExpression)` | `nExpression` | `inExpression` |
| `lastN(SFunction<T, ?> fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `lastN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?> inExpression)` | `nExpression` | — |
| `lastN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?>... inExpression)` | `nExpression` | — |
| `lastN(SFunction<T, ?> fieldName, NExpression nExpression, String... inExpression)` | `nExpression` | `inExpression` |
| `lastN(String fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `maxN(SFunction<T, ?> fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `maxN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?> inExpression)` | `nExpression` | — |
| `maxN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?>... inExpression)` | `nExpression` | — |
| `maxN(SFunction<T, ?> fieldName, NExpression nExpression, String... inExpression)` | `nExpression` | `inExpression` |
| `maxN(String fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `minN(SFunction<T, ?> fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `minN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?> inExpression)` | `nExpression` | — |
| `minN(SFunction<T, ?> fieldName, NExpression nExpression, SFunction<R, ?>... inExpression)` | `nExpression` | — |
| `minN(SFunction<T, ?> fieldName, NExpression nExpression, String... inExpression)` | `nExpression` | `inExpression` |
| `minN(String fieldName, NExpression nExpression, InExpression inExpression)` | `nExpression`, `inExpression` | — |
| `top(SFunction<T, ?> fieldName, Bson sortBy, OutExpression outExpression)` | `outExpression` | — |
| `top(SFunction<T, ?> fieldName, Bson sortBy, String... outExpression)` | — | `outExpression` |
| `top(String fieldName, Bson sortBy, OutExpression outExpression)` | `outExpression` | — |
| `topN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, OutExpression outExpression)` | `nExpression`, `outExpression` | — |
| `topN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, SFunction<R, ?>... outExpression)` | `nExpression` | — |
| `topN(SFunction<T, ?> fieldName, Bson sortBy, NExpression nExpression, String... outExpression)` | `nExpression` | `outExpression` |
| `topN(String fieldName, Bson sortBy, NExpression nExpression, OutExpression outExpression)` | `nExpression`, `outExpression` | — |

### AggregateOperator

| 完整 overload | 新增 VALUE 参数 | 新增 ELEMENT 参数 |
|---|---|---|
| `concat(List<?> expressions)` | — | `expressions` |
| `concat(Object... expression)` | — | `expression` |
| `concatArrays(List<?>... list)` | — | `list` |
| `dateTrunc(SFunction<?, ?> field, String unit)` | `unit` | — |
| `dateTrunc(SFunction<?, ?> field, String unit, Integer binSize, String startOfWeek, String timezone)` | `unit`, `binSize`, `startOfWeek`, `timezone` | — |
| `dateTrunc(String field, String unit)` | `field`, `unit` | — |
| `dateTrunc(String field, String unit, Integer binSize, String startOfWeek, String timezone)` | `field`, `unit`, `binSize`, `startOfWeek`, `timezone` | — |

### ConditionOperators

| 完整 overload | 新增 VALUE 参数 | 新增 ELEMENT 参数 |
|---|---|---|
| `abs(Number value)` | `value` | — |
| `add(List<?> expressions)` | — | `expressions` |
| `cond(Object ifValue, Object thenValue, Object elseValue)` | `ifValue`, `thenValue`, `elseValue` | — |
| `cond(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue)` | `thenValue`, `elseValue` | — |
| `condArray(Object ifValue, Object thenValue, Object elseValue)` | `ifValue`, `thenValue`, `elseValue` | — |
| `condArray(String ifCondition, Collection<?> ifValue, Object thenValue, Object elseValue)` | `thenValue`, `elseValue` | — |
| `dateFromString(Object dateString)` | `dateString` | — |
| `dateFromString(Object dateString, Object format, Object timezone, Object onError, Object onNull)` | `dateString`, `format`, `timezone`, `onError`, `onNull` | — |
| `dateToString(String date)` | `date` | — |
| `dateToString(String format, SFunction<T, ?> date)` | `format` | — |
| `dateToString(String format, SFunction<T, ?> date, String timezone)` | `format`, `timezone` | — |
| `dateToString(String format, SFunction<T, ?> date, String timezone, Object onNull)` | `format`, `timezone`, `onNull` | — |
| `dateToString(String format, String date)` | `format`, `date` | — |
| `dateToString(String format, String date, String timezone)` | `format`, `date`, `timezone` | — |
| `dateToString(String format, String date, String timezone, Object onNull)` | `format`, `date`, `timezone`, `onNull` | — |
| `ifNull(List<?> inputExpressions)` | — | `inputExpressions` |
| `ifNull(Object... inputExpressions)` | — | `inputExpressions` |
| `mergeObjects(Collection<?> values)` | — | `values` |
| `mergeObjects(Object... values)` | — | `values` |
| `mergeObjects(String value)` | `value` | — |
| `multiply(Collection<?> values)` | — | `values` |
| `multiply(Object... values)` | — | `values` |
| `substrBytes(SFunction<T, ?> field, Number index, Number count)` | `index`, `count` | — |
| `substrBytes(SFunction<T, ?> field, Object index, Object count)` | `index`, `count` | — |
| `substrBytes(TExpression expression, Number index, Number count)` | `index`, `count` | — |
| `substrBytes(TExpression expression, Object index, Object count)` | `index`, `count` | — |
| `sum(List<?> expressions)` | — | `expressions` |
| `toHashedIndexKey(String key)` | `key` | — |
