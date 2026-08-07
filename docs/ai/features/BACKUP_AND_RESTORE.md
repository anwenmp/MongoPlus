# 本地备份、导出、导入与恢复

> 审计日期：2026-08-02。结论来自当前源码，未运行文件/Driver 集成测试。这里描述 `mongo-plus-core` 的 `BackupManager`；它不是 MongoDB Server 复制、快照、`mongodump/mongorestore` 或通用数据库恢复系统。异步镜像见 [ASYNC_MULTI_WRITE.md](ASYNC_MULTI_WRITE.md)。

## 公开入口和注册

唯一实现是用户手工 `new BackupManager(...)` 后调用 `export()` 或 `imports(zipPath)`。它是独立 manager，不是 Mapper/Service API，没有配置属性、自动配置、进度框架或默认启用。Boot 3、Boot 4、Solon 没有专用注册差异；都只需提供同一个 core `MongoPlusClient`。`BackupListener.export(path, collectionName, document)` 只在导出逐文档时同步回调，不是恢复回调，也不隔离异常。

## 已支持范围

| 能力 | 当前支持 |
|---|---|
| 单/多 collection | 用户显式传名称或实体 Class 列表；逐 collection 独立导出 ZIP |
| 整个/多 database、自动发现 collection | 不支持 |
| 多 datasource | 单个 manager 可 `setDataSourceName` 选择一个源；不自动遍历 |
| filter/projection/sort | 不支持；固定 `find()` |
| limit | `limit` 是每个分页文件的文档数，不是总条数限制 |
| 全量/增量 | 仅普通全量遍历；无增量位点 |
| 索引、collection options/validator、时序、view、shard metadata、users/roles | 不保存 |
| database/collection/datasource metadata | 每个 JSON 分页首元素保存名称和版本信息 |
| `_id` | Document 序列化包含 `_id`；恢复是否完全保真取决于 BSON JSON 解析 |

空 collection 不生成 ZIP，`export()` 的结果 map 对应值为 null。动态 collection 不经 handler 自动展开；调用者必须传最终名称。索引与时序边界见 [INDEX_AND_TIMESERIES.md](INDEX_AND_TIMESERIES.md)，数据源/动态名称见 [MULTI_DATASOURCE.md](MULTI_DATASOURCE.md) 和 [DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md)。

## 导出链路与文件格式

```text
BackupManager.export
 -> 以 dataSourceName + DataSourceNameCache.getDatabase(ds) + 显式 collection 取 MongoCollection
 -> estimatedDocumentCount
 -> find().skip(page*limit).limit(limit).batchSize(limit).iterator()
 -> Document.toJson(MapCodecCache.getDefaultCodec())
 -> BufferedWriter/FileWriter 写 JSON 数组
 -> ZipUtil.zipDirectory(collection临时目录, collection-timestamp.zip)
 -> 递归删除临时目录
```

这是原生 `MongoCollection` 读取，绕过 Mapper、Tenant、Logic Delete、Dynamic Collection、Entity Mapping 和事务 `SessionExecute`。因此会读取当前 collection 中包含逻辑删除/所有 tenant 的文档；没有 ClientSession、snapshot readConcern 或多 collection 同一时点保证。分页用 skip/limit，备份期间插入/删除可能造成重复或遗漏；`estimatedDocumentCount` 用于终止分页，不能称为一致性快照。

输出是每 collection 一个 ZIP，内部每页一个 `.json` entry；entry 遍历顺序来自 `DirectoryStream`，源码未排序。每页是 JSON 数组，首元素形如 `{"information":{...}}`，实际只包含 `origin`、`version`、`homepage`、`data_source`、`database`、`collection_name`、`date_time`、`time_stamp`，没有 count/page size/file name 字段；随后是文档。2026-08-07 已将文档之间的分隔符改为按当前页的局部位置写入，单个 entry 的最后一条文档后不再写逗号。扩展名由 `getFileName` 生成，ZIP 名使用秒级时间戳。没有 checksum、签名、加密、兼容 schema 或独立 manifest。

`FileWriter` 使用平台默认字符集和平台换行语义；恢复固定按 UTF-8 解码，非 UTF-8 默认平台存在不兼容风险。`ZipUtil` 使用默认 `ZipOutputStream` 压缩设置；没有压缩级别/算法选择。路径通过字符串拼接，构造器只补 `/`；`setPath` 不补分隔符。父级根目录不会显式创建，只尝试创建 `path + collectionName`。同名 ZIP 由 `FileOutputStream` 截断覆盖；没有临时 ZIP + 原子移动、路径净化、权限设置或 `../` 防护。压缩失败时可能留下部分 ZIP 和临时 JSON；压缩成功但临时目录删除失败时方法抛异常，成品 ZIP 仍可能保留。

### 类型保真

写侧用带 `MapCodecCache` codec 的 `Document.toJson`，读侧用 `BsonArray.parse` 后 `new Document(BsonDocument)`，不是同一个显式 codec 往返 API。源码不足以承诺 ObjectId、Date、Decimal128、Binary/UUID、Regex、Timestamp、DBRef、MinKey/MaxKey 和大整数全部无损；必须逐类型往返并比较 BSON type/value。null 可由 BSON 表达，但仍应纳入测试。没有 CSV、原始 BSON、Java 序列化或用户可选 Extended JSON 模式。

## 恢复链路和语义

```text
BackupManager.imports(zipPath)
 -> ZipInputStream 逐 entry
 -> 整个 entry 读入 ByteArrayOutputStream/String
 -> BsonArray.parse
 -> 读取 information，移除首元素
 -> 以 manager.dataSourceName + metadata.database/collection 取目标
 -> 第一个 ZIP entry 前 collection.drop()
 -> ExecutorFactory.getExecute().executeSave(documentList, null, collection)
 -> insertMany
```

metadata 中 `data_source` 被读取到局部变量但完全未使用；因此可以把 A 的文件导入到 manager 当前选择的 B datasource。database 和 collection 固定采用文件 metadata，没有映射 API。代码直接对 `information`、字符串字段和首数组元素调用类型访问器；缺失、空数组或类型不符通常在自定义“不是 MongoPlus 文件”检查之前就抛 Driver/BSON 异常。目标 collection 在首次访问/insert 时由 Server 正常创建；不会重建 options、validator、时序或索引。

恢复默认不是 replace/upsert：它对每个 entry 执行 ordered 默认 `insertMany`。没有 skip error、覆盖选项、batch size、结果返回、进度、事务、回滚、补偿或断点续传。duplicate `_id`/Driver 失败直接传播，当前 entry 在失败位置之前由 Driver 已接受的文档以及先前成功 entry 均不回滚。`imports` 只在整个 ZIP 的**第一个 entry**前 drop collection，而且 drop 发生在把 BSON 数组转换为 `List<Document>` 之前；因此首 entry 后续转换或 insert 失败时目标也已经被清空。若 ZIP 混入多个 collection，后续 collection 不 drop，而 entry 顺序也没有公开契约。

恢复通过 `ExecutorFactory.getExecute()`，所以会进入当前全局普通和高级拦截器链，而不是 Mapper/实体转换链：原始 Document 不经过 Mapper Auto Fill、实体映射、实体字段 Encryption 或 LOCAL Sensitive Word；普通 Tenant、LogicAutoFill、GLOBAL Sensitive Word、Dynamic Collection 等是否改写则取决于各自对 Document/collection 的分支。Async Multi Write 若已注册，会为每个恢复 entry 的 `executeSave` 按目标列表各提交一个镜像任务；drop 是直接 Driver 调用，不镜像。主 insert 失败时镜像仍可能已成功，镜像的裸 `DefaultExecute` 不再触发 Restore、Async 或 Recorder，因而不会形成恢复递归。Command Listener 会观察主 drop、主 insert 以及每个实际镜像 insert；具体事件数仍受 Driver retry/bulk 行为影响。组合顺序见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)、[COMMAND_LISTENER.md](COMMAND_LISTENER.md) 和 [DATA_CHANGE_RECORDER.md](DATA_CHANGE_RECORDER.md)。

若 `DataChangeRecorderInnerInterceptor` 已注册且未忽略目标 collection，恢复的每个 entry 产生一次 SAVE 记录。当前字段默认值 `displayCompleteData=true`，所以 `changedData` 是整个该页 `List<Document>.toString()`；默认 `batchUpdateLimit=1000`，超过阈值会在 Driver insert 和 Async 提交之前阻断该 entry。镜像写不再进入 Recorder，因此没有额外 Recorder 记录；但会产生额外 Driver Command Listener 事件。

`ExecutorFactory` 在每个 entry 写入前按当前 `MongoTransactionContext` 选 Default/Session Execute，因此若调用者外部已经建立 MongoPlus 事务，insert 可能使用该 session；但 `drop()` 在取得 Execute 之前直接调用原生 collection，不带 session。`imports` 自身不 start/commit/abort，不能承诺原子恢复，见 [TRANSACTION.md](TRANSACTION.md)。

## 资源、性能、失败与安全

- 导出按页使用 try-with-resources 关闭 `BufferedWriter` 和 `MongoCursor`；每页逐文档写，不一次装入该页所有文档。压缩完成后删除临时目录。
- 恢复用 try-with-resources 关闭 `ZipInputStream`，但每个 ZIP entry（包括目录或非预期 entry，并无筛选）都尝试全量读入字节数组、String、BsonArray 和 `List<Document>`，内存上界约为单 entry 大小的多份表示；没有取消/并行/大小限制。空 ZIP 会直接正常返回，目录/额外 entry 通常在 BSON 解析处失败。
- 导出只捕获 `IOException`，记录后 break，随后仍尝试压缩并返回 ZIP；半成品可能被包装成看似成功结果。Listener、Driver、codec、压缩或删除异常不统一处理。
- 恢复只把 `IOException` 改为无 message/cause 的 `RuntimeException`；解析、drop、insert 异常直接传播。流仍由 try-with-resources 关闭，没有失败清理或恢复报告。
- 没有磁盘空间预检、临时文件原子替换、完整性校验、文件加密、密码、脱敏、字段排除、权限、签名、保留期或自动删除。备份可能包含完整业务数据、tenant 数据、明文、密文、token、密码和 Binary；用户负责路径授权、加密传输和安全存储。
- `BackupManager` 不创建 executor/MongoClient，也没有容器关闭回调；同步调用期间由调用线程持有文件流，MongoClient 生命周期仍归数据源管理。

## 已确认缺陷

- **恢复 drop 缺陷：** 只按 ZIP 的全局 `num == 0` drop 第一个 entry 的 collection，而不是每个 collection；格式意外含多 collection 时覆盖规则错误。
- **编码缺陷：** 导出用平台默认编码，恢复强制 UTF-8。
- **错误表达缺陷：** 导出遇 IOException 后仍可能压缩/返回半成品；恢复 IOException 丢弃 cause；两边均无结构化部分成功结果。
- **metadata 缺陷：** 恢复读取 `data_source` 却不用，文件本身不能控制/验证目标 datasource。

## 待运行验证

全部 BSON 特殊类型往返；并发增删时的计数/格式；非 UTF-8 平台；文件损坏、磁盘不足和中断；duplicate `_id` 与 ordered 部分成功；外部事务、Tenant/Logic/Dynamic/Recorder/Async 组合；同 database/collection 不同 client；大 entry 内存；Windows/Linux 特殊 collection 名和路径穿越。空 collection 返回 null、分页 entry 的尾逗号修复、同名 ZIP 截断覆盖和未筛选 entry 已由控制流确认。

## 最低测试清单

单/多显式 collection、空 collection、多页/大文档；ObjectId/Date/Decimal128/Binary/UUID/其余 BSON 类型；逻辑删除、Tenant、Dynamic Collection、多 datasource；外部事务提交/回滚；重复 `_id`、已有目标、索引/时序缺失；损坏 ZIP/JSON、磁盘不足、覆盖和路径穿越；部分恢复、并发导出；Listener/Recorder/Async Multi Write；Boot 3、Boot 4、Solon；Windows/Linux 与非 UTF-8 默认编码。

## 关键源码

- `mongo-plus-core/.../manager/BackupManager.java`
- `mongo-plus-core/.../listener/BackupListener.java`
- `mongo-plus-core/.../toolkit/ZipUtil.java`
- `mongo-plus-core/.../execute/ExecutorFactory.java`
- `mongo-plus-core/.../cache/{codec/MapCodecCache,global/DataSourceNameCache}.java`
