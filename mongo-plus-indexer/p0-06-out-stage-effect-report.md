# P0-06 收尾：out Stage Effect 审计

日期：2026-10-10。范围仅为正式 `$out` 映射的 Stage 追加证据。

## 逐 overload 审计

声明均在 `Aggregate.java`，实现在 `LambdaAggregateWrapper.java`。

| 正式签名 | 真实委托链 | BSON | Effect |
|---|---|---|---|
| `out(String collectionName)` | `Aggregates.out(collectionName) → out(Bson) → custom` | `{$out: "orders"}` | 当前 receiver 按调用顺序追加一个 Stage |
| `out(Class<?> collection)` | `AnnotationOperate.getCollectionName(collection) → out(String) → out(Bson) → custom` | 注解集合名优先；没有有效注解值则使用已有类名转换策略，示例 `{$out: "archived_orders"}` | 同上 |
| `out(String databaseName, String collectionName)` | `Aggregates.out(databaseName, collectionName) → out(Bson) → custom` | `{$out: {db: "archive", coll: "orders"}}`，body 字段顺序为 `db`、`coll` | 同上 |

Driver 5.4.0 的 `Aggregates.out(String)` 直接构造 `BsonDocument("$out", BsonString)`；
双 String 版本构造含 `db`、`coll` 的 document。Class 路径只取集合名，不从注解补数据库名。
三条正常返回路径都只进入一次 `custom(Bson)`；该方法只执行一次
`aggregateConditionList.add(bson)` 并返回 `typedThis`。已有 Stage 不被替换或重排。

仅在这三条接口声明的 Javadoc 添加：

```text
@mongoPipelineEffect operation=APPEND_STAGE target=RECEIVER count=ONE order=CALL_ORDER
```

`out(Bson)` 另行核对为任意完整 BSON 透传：不会补 `$out` 包装，因而继续未映射、未加 Effect。
实现类的未映射 publicMethods 不传播接口标签。未修改任何 Core 方法体、Indexer 生产逻辑或既有测试预期。

## isSkip 的作用与证据边界

`LambdaAggregateWrapper.out(Bson)` 在调用 `custom(bson)` 前设置本 receiver 的 `isSkip=true`。
三条正式 overload 均到达这一入口；只有正常返回路径承诺单次追加。String/跨库 BSON 构造和
Class 集合名解析先发生，构造失败不能被解释为已追加 Stage。标志是当前 receiver 的状态，
不写入 BSON、不改变 Stage 数量、不重排调用、不阻止后续追加，也不会自动复位或传播给兄弟 receiver。

当前源码中，`AbstractBaseMapper.aggregateList/aggregateOne` 先取得管道、调用执行器并应用 options，
随后读取 `isSkip()`：为 true 时调用 `AggregateIterable.toCollection()`，list 返回空列表，one 返回 null；
不进入普通结果映射分支。`custom({$out: ...})` 本身不会设置该标志，因此不能替代真实 out API。

`PIPELINE_CONSTRUCTION_V1` 的 Effect 足以描述本轮 Stage 追加行为。直接生成上述 API 调用的 Java
自然执行原方法体并保留 isSkip 副作用，未发现该标志造成本轮构造正确性的独立阻断缺口。
现有 Effect 不表达执行时结果消费策略；这是独立的机器证据范围，本轮只记录，不扩展契约。
不据此宣称完整参数准入、nested Stage 合法性、服务器执行或结果消费语义已闭合。

## 本轮实际验证

1. 修改前正式 CLI 重生成得到用户给定 SHA
   `2AF29225C1424CB21D0F1BDD47EFB616FF50528A59647DBFB061C416A21C3D59`。
   新增 out Indexer 自测在补标签前因缺少逐 overload Effect 失败。
2. Java 21 下执行以下定向 Core 测试：

   ```powershell
   mvn -pl mongo-plus-core -am '-Dmaven.test.skip=false' '-Dtest=OutStageEffectTest,CommonStageEffectTest' '-Dsurefire.failIfNoSpecifiedTests=false' test
   ```

   共 11 项通过（新增 Out 6 项、既有 Common 5 项）。逐一覆盖三条正式调用的 receiver identity、
   单次追加、精确 BSON/类型、跨库字段顺序、兄弟 receiver 隔离、isSkip，以及透传边界、
   后续调用顺序和 null String 构造失败。首次新测试编译的辅助函数泛型约束错误已修正，预期未变。
3. `mvn -f mongo-plus-indexer/pom.xml '-Dmaven.test.skip=false' '-DskipTests' test-compile` 通过，
   并直接运行 main 自测（classpath 为 `target/classes;target/test-classes`，不是 Surefire 零测试结果）：
   - `PipelineOutStageEffectEvidenceSelfTest .`：3 overload、6 family/publicMethods 视图检查通过；
     6 次缺 Effect、24 次缺来源字段、6 次真实源码删标签后的视图门禁拒绝均通过。
     来源字段逐一覆盖 `sourceEvidence`、`source`、`tag`、`value`；临时夹具不修改正式源码或 API JSON。
   - `PipelineStageEffectEvidenceSelfTest . --graph-lookup-only`：6 overload、12 视图检查、
     18 个既有 Effect 组合检查、18 次缺 Effect、12 次缺来源拒绝通过。
   - `MongoPlusPipelineIndexerSelfTest .`：通过。
4. 完整 Index 前后结构对比仅有 6 处新增 `pipelineEffect`：三条 out 各出现在 family 和
   Aggregate type publicMethods。graphLookup、bucket 在两个视图中的完整记录（含 Effect 和来源）
   完全相等；其他字段、能力、family/overload 统计也不变。

上述拒绝为上游验收自测；本轮未运行或修改 MCP/AI，未进行数据库、SSE 或全仓测试。

## 正式生成与同步结论

专项通过后，在两个独立 Java 进程中各执行一次现有正式 CLI：

```powershell
& 'D:/Java/java21/bin/java.exe' '-Dfile.encoding=UTF-8' -cp 'mongo-plus-indexer/target/classes' com.mongoplus.indexer.cli.MongoPlusPipelineIndexerMain --project-root .
```

完整输出均为 3,425,943 bytes，直接比较字节完全一致。生成路径：
`mongo-plus-indexer/target/generated-resources/mongo-plus-pipeline-api-index.json`。
该文件由 CLI 生成，未手工编辑；沿用仓库对 target 构建产物的忽略规则。

新 SHA-256：`1E010831D1D8D5180A4FB8CB9D3A1974930411497C53CC86122AFBD4EB8285B3`。

可将该正式 Index 同步 MCP，用于补齐本轮 out Stage Effect 门禁证据；本结论不替代下游同步后的
实际回归，也不代表其他参数/构造证据自动闭合。本轮不执行同步。
