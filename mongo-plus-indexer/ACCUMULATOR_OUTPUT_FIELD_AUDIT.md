# Accumulators 输出字段名专项审计（2026-09-09）

范围仅为 `com.mongoplus.aggregate.pipeline.Accumulators` 已收录为 `PIPELINE_EXPRESSION`
的声明：21 个 MethodFamily、85 个 overload。确认并补齐 84 个输出名称参数槽，
其中 String 29 个、SFunction 55 个；无参 `sum()` 固定使用 `count`，没有参数槽。
统计按 overload 的 `declaredIn` 限定 Accumulators；同一 family 内其他声明类不计入。

## 真实实现依据

逐 overload 沿委托链核对 `fieldName` 到以下四个终点，均为 `new BsonField(fieldName, ...)`
的第一参数，而非内部表达式文档的值。String 原样传递；部分入口的 `notNull` 只校验非空。
55 个输出 getter 独立确认调用 `getFieldNameLine()` 后委托 String overload；该 helper
按实体映射取普通字段名，不调用添加美元前缀的 `getFieldNameLineOption()`。

| MethodFamily | Accumulators overload | String 输出槽 | Lambda 输出槽 | BsonField 构造 helper |
|---|---:|---:|---:|---|
| accumulator | 10 | 5 | 5 | accumulatorOperator |
| addToSet | 3 | 1 | 2 | accumulatorOperator |
| avg | 2 | 1 | 1 | accumulatorOperator |
| bottom | 5 | 1 | 4 | sortingPickAccumulator |
| bottomN | 5 | 1 | 4 | sortingPickNAccumulator |
| count | 4 | 2 | 2 | accumulatorOperator |
| first | 2 | 1 | 1 | accumulatorOperator |
| firstN | 5 | 3 | 2 | pickNAccumulator |
| last | 4 | 2 | 2 | accumulatorOperator |
| lastN | 5 | 1 | 4 | pickNAccumulator |
| max | 3 | 1 | 2 | accumulatorOperator |
| maxN | 5 | 1 | 4 | pickNAccumulator |
| mergeObjects | 3 | 1 | 2 | accumulatorOperator |
| min | 3 | 1 | 2 | accumulatorOperator |
| minN | 5 | 1 | 4 | pickNAccumulator |
| push | 4 | 1 | 3 | accumulatorOperator |
| stdDevPop | 3 | 1 | 2 | accumulatorOperator |
| stdDevSamp | 3 | 1 | 2 | accumulatorOperator |
| sum | 3 | 1 | 1 | accumulatorOperator；另有无参 sum() |
| top | 4 | 1 | 3 | sortingPickAccumulator |
| topN | 4 | 1 | 3 | sortingPickNAccumulator |
| 合计 | 85 | 29 | 55 | 84 个输出槽 |

每个已确认的声明独立添加 `@mongoParam fieldName OUTPUT_FIELD_NAME VALUE`。
通过既有机制得到 `semanticType=OUTPUT_FIELD_NAME`、`semanticScope=VALUE`、
`conceptRef=PIPELINE_PARAMETER_OUTPUT_FIELD_NAME` 及当前声明的 JAVADOC evidence。
复用 Stage Audit 的既有 semanticType 和 concept，未修改其定义或 Indexer 生产代码。

逐签名记录位于 [参数审计 TSV](src/test/resources/pipeline-expression-parameter-audit.tsv)，
本次 84 行状态为 `OUTPUT_NAME_ADDED`，分别记录 String/Lambda 路径及构造 helper。
原 Expression 审计的 127 行 `ADDED` 保留。该 TSV 仅为测试证据，不参与 Index 推断。

## 边界

- `count` 的输出名被标记，其两个泛型 expression 保持原分类；不新增 expression 语义。
- expression、inExpression、outExpression、nExpression、sortBy、JavaScript 函数源码、
  lang、initArgs/accumulateArgs 全部保持原证据。`push` 的 expression varargs 中构造
  BasicDBObject 的 getter 也不是本次 BsonField 输出名参数。
- 既有未收录的 `firstN(String,NExpression,SFunction...)` 和
  `topN(SFunction,Bson,NExpression,SFunction)` 没有新增映射或参数标签。
- Core 改动仅为 84 行 Javadoc 标签；方法签名和执行代码保持不变。
  未修改 MCP、Resolver、消歧、schemaVersion、Stage/Expression 映射、API surface、
  容器/varargs overload 策略、cond/condArray 或 limit。

## 本次实际验证

新增断言在补标签前分别因 avg 输出名、accumulator 输出名缺少 evidence 而失败；补标签后通过。
使用 JDK 21 执行：

```powershell
mvn.cmd -pl mongo-plus-indexer -am '-Dgpg.skip=true' test-compile -q
$cp = 'mongo-plus-indexer/target/test-classes;mongo-plus-indexer/target/classes'
foreach ($test in @('PipelineExpressionSemanticsSelfTest', 'PipelineExpressionCoverageSelfTest', 'PipelineStageSemanticsSelfTest', 'MongoPlusPipelineIndexerSelfTest', 'MongoPlusIndexerSelfTest')) {
    & D:/Java/java21/bin/java.exe '-Dfile.encoding=UTF-8' -cp $cp "com.mongoplus.indexer.$test" .
    if ($LASTEXITCODE -ne 0) { throw "$test failed" }
}
& D:/Java/java21/bin/java.exe '-Dfile.encoding=UTF-8' -cp mongo-plus-indexer/target/classes com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
git diff --check
```

五个可执行自测全部通过。Expression semantic 测试通过输入 group 的 total/$sum/$amount、
avg/$avg/$amount、max/$max/$amount、items/$push/$itemId 核对 String 输出名的
OUTPUT_FIELD_NAME / VALUE / concept / JAVADOC，以及第二参数保持 PIPELINE_EXPRESSION。
Coverage 测试检查全部 84 个输出槽及其他参数不误标，且与 types.publicMethods evidence 一致。

以本次修改前的工作树重新生成的 JSON 为基线，结构深比较确认：只有 84 个 overload 参数及其
84 个 type 声明副本的四项 semantic evidence 字段变化；恢复这四项后整个 JSON 完全相等。
因此原 expression 参数、全部 concepts、schema、映射、扫描统计和 API surface 都未变化：
33 Stage / 49 Expression / 82 MethodFamily / 297 overload。

正式 CLI 连续两次生成结果逐字节相等，deterministic 通过。
生成文件：[mongo-plus-pipeline-api-index.json](target/generated-resources/mongo-plus-pipeline-api-index.json)。
SHA-256：`486d8d1f0526401e19c9b4b598a7196a600ea964a95dc4b7d5ca166f18b0aca2`。
验证限于当前 Core 源码和 Index，不包含 MongoDB 服务端执行或外部 Resolver 运行。
