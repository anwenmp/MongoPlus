# 开放问题

> 收口日期：2026-08-04。每个问题只归入一类。“已确认缺陷”要求当前源码控制流闭合；“高风险设计”不是缺陷结论；“运行验证”不得在测试前写成必现外部行为。已解决观察不再保留。

## A. 已确认缺陷（5 个未修复，6 个已修复）

1. **已修复：事务结束未关闭 session：** 2026-08-02 将最外层 cleanup 改为正常 commit/abort 后也无条件 `ClientSession.close()`；参与式内层不提前清理。[事务](features/TRANSACTION.md)
2. **已修复：Spring TransactionManager 泄漏两套 session：** 2026-08-02 改为 Spring transaction object、resource binding 和执行器上下文共享同一 status/session，并补充 `REQUIRED` rollback-only 与 `REQUIRES_NEW` 回归测试。[事务](features/TRANSACTION.md)
3. **已修复：`@IgnoreLogic` update 漏检：** 2026-08-04 在 `CollectionLogiceInterceptor.executeUpdate(MutablePair)` 访问 collection 元数据前增加 ignore 短路，单 pair 与委托它的多 pair update 均不再追加逻辑未删除条件；bulk 和高级删除转换仍按各自既有分支处理。独立 `mongo-plus-test` 新增 2 项回归。[Logic Delete](features/LOGIC_DELETE.md)
4. **已修复：Tenant `UpdateManyModel` filter 未写回：** 2026-08-04 bulk 分支重建 BSON update 与 pipeline update 两类 `UpdateManyModel`，把增强后的 filter 写入返回列表并保留原顺序、update/pipeline 和 options；同时接住非可变 BSON 的 `BsonUtil.addToMap` 返回值。InsertOne 仍原地增强，其他 model 仍不处理。独立 `mongo-plus-test` 新增 2 项回归。[Tenant](features/TENANT.md)
5. **已修复：LOCAL Sensitive Word 覆盖其他字段处理：** 2026-08-02 在 core FieldHandler 契约、`MappingMongoConverter.processFields` 与 sensitive-word Handler 范围内修复：写 Handler 按 order 执行并传递最新值，null 不替换累计值，LOCAL 最先检查且未拒绝时返回 null。回归测试位于 `mongo-plus-sensitive-word/src/test/java/com/mongoplus/handler/SensitiveWordFieldHandlerTest.java`，覆盖 order、最新值、旧实现兼容、TypeHandler→Encrypt、DBRef、拒绝和明文检查。[Sensitive Word](features/SENSITIVE_WORD.md)
6. **已修复：RSA/SM2 private key 接线错误：** 2026-08-04 将注解解密参数改为 `privateKey`，并将 RSA/SM2 空私钥回退改为全局 `PropertyCache.privateKey`；公钥仍只用于加密。独立 `mongo-plus-test` 新增注解参数、RSA 与 SM2 共 3 项回归；JDK/provider 兼容矩阵仍归 C6 运行验证。[Encryption](features/FIELD_ENCRYPTION.md)
7. **PBE 空 key 回退不一致：** encrypt 可回退全局 key，decrypt 的空 key 不回退。[Encryption](features/FIELD_ENCRYPTION.md)
8. **Map 读取无限递归：** `Class<Map>` 与 `TypeReference<Map<...>>` 进入 Map 分支后以 raw Map 再次递归，没有终止条件；影响 Mapper 查询及聚合结果映射。[Entity Mapping](architecture/ENTITY_MAPPING.md) / [Aggregate](architecture/AGGREGATION.md)
9. **Optimistic Lock 覆盖用户 `$inc`：** 拦截器向 update 顶层写入新的 `$inc`，覆盖整个原 `$inc`；部分非 Document/BSONObject Bson 的局部改写也未写回 pair。[Optimistic Lock](features/OPTIMISTIC_LOCK.md)
10. **Backup 多页 JSON 尾逗号：** 非最后分页在数组末文档后保留逗号，生成非法 JSON；另有恢复只处理首个 entry、导出/恢复编码不一致等闭合缺口。[Backup / Restore](features/BACKUP_AND_RESTORE.md)
11. **Recorder 上下文与 namespace 恢复缺陷：** 保存时改写 datasource/namespace 后不恢复；ThreadLocal 只在保存成功后移除，失败会残留；用户覆盖忽略列表还可破坏默认递归保护。[Data Change Recorder](features/DATA_CHANGE_RECORDER.md)

这些缺陷的外部异常类型、资源增长、数据库最终命令或并发后果仍须用测试固定；静态证据不等于所有环境下已有相同运行外观。

## B. 已确认实现行为或高风险设计（10）

1. **Registry key 不含 datasource：** namespace→实体 key 只有 `database.collection` 且 `putIfAbsent`；跨 client 同 namespace 可能共享首个实体元数据，需评估隔离契约。[Multi Datasource](features/MULTI_DATASOURCE.md) / [Entity Mapping](architecture/ENTITY_MAPPING.md)
2. **动态集合缓存无淘汰：** collection/registry 随动态名称增长，关闭或覆盖数据源不清理；需评估生命周期和上限。[Dynamic Collection](features/DYNAMIC_COLLECTION.md)
3. **时序创建吞 `MongoCommandException`：** 启动可继续但最终结构未知；需决定是否保留 fail-open 以及如何可观测。[Index / Time Series](features/INDEX_AND_TIMESERIES.md)
4. **分片并发共享普通 boolean：** `sessionIsNotNull` 位于 Boot 单例 Bean，无 ThreadLocal、同步或 finally，可能跨线程影响执行器选择；未运行前不标记必现缺陷。[Sharding](features/SHARDING.md)
5. **分片事务逐项结束且非原子：** 多 session 的 commit/rollback/close 经 `HashMap.forEach` 逐项执行，异常会中断后续；无法提供跨数据源原子承诺。[Sharding](features/SHARDING.md) / [Transaction](features/TRANSACTION.md)
6. **Async 在主写前提交镜像：** 没有 afterCommit、取消、重试或结果通道，Future 被丢弃，默认 executor 无 shutdown，目标不去重且参数共享可变引用；需评估一致性模型。[Async Multi Write](features/ASYNC_MULTI_WRITE.md)
7. **Restore 先 drop 后恢复：** drop 不带 session，insert 可带外部 session；失败和事务回滚不能恢复 drop，需评估破坏性边界。[Backup / Restore](features/BACKUP_AND_RESTORE.md)
8. **Listener 静态列表无生命周期清理：** 多上下文可能累积监听器；同步回调异常会重新抛出，需评估隔离和卸载契约。[Command Listener](features/COMMAND_LISTENER.md)
9. **Auto Fill 位于实体映射后：** fill Map 不再经过字段 TypeHandler/Encrypt/DBRef，重命名字段可能形成双字段；需定义组合契约。[Auto Fill](features/AUTO_FILL.md)
10. **Aggregate 只增强顶层 pipeline：** Tenant/Logic 不递归 lookup/facet/unionWith 子 pipeline，且无 match 时 Logic `$match` 追加到末尾；需评估 stage 合法性和安全边界。[Aggregate](architecture/AGGREGATION.md)

## C. 运行验证问题（15）

每项格式为“环境；最小验证；成功判定；专题”。

1. **事务 session 生命周期：** MongoDB replica set + Driver command/session 计数器 + Boot 2/3/4；覆盖 commit、rollback、嵌套和失败；每个创建的 session 恰好结束并关闭、上下文清空；当前仅有代理 session 单测，尚无真实 Server 证据；[事务](features/TRANSACTION.md)。
2. **跨 datasource 事务与分片 session：** 两个 replica set/client；事务中换源并记录 session id、commit/abort/close；命令使用预期 client/session且所有状态可解释、资源释放；[多数据源](features/MULTI_DATASOURCE.md) / [分片](features/SHARDING.md)。
3. **Driver/Server 异常类型：** 支持的 MongoDB Server 版本矩阵；对空/null 聚合 stage、索引冲突、时序 options、无权限分别触发；固定准确异常、部分成功状态和重试结果；[兼容性](COMPATIBILITY.md) / [聚合](architecture/AGGREGATION.md) / [索引](features/INDEX_AND_TIMESERIES.md)。
4. **BSON 往返保真：** MongoDB + 临时目录；备份/恢复 ObjectId、Date、Decimal128、Binary、UUID、Regex、Timestamp、DBRef、Min/MaxKey 和大整数；值与 BSON 类型均相同；[Backup / Restore](features/BACKUP_AND_RESTORE.md)。
5. **字段处理组合：** MongoDB + JDK/provider 矩阵；TypeHandler、加解密、脱敏、Sensitive Word、DBRef 单独及组合往返；顺序、存储值、读取值和异常策略符合明确契约且无明文意外覆盖；[Entity Mapping](architecture/ENTITY_MAPPING.md)。
6. **加密算法兼容：** JDK 8/17/21 与所需 provider；覆盖 AES/RSA/SM2/PBE、坏 key、长度边界和随机性；成功组合可往返、失败类型及 fail-open 外观被固定；[Encryption](features/FIELD_ENCRYPTION.md)。
7. **ThreadLocal 在线程池污染：** 固定大小线程池 + 屏障；依次触发 Tenant、Logic、datasource、Recorder、Async 的成功/异常路径；后续无关任务看不到旧上下文；[Extension Pipeline](architecture/EXTENSION_PIPELINE.md)。
8. **动态集合并发首次创建和增长：** Boot 3/4/Solon + 并发屏障；同名首次创建及大量唯一名称后检查 open/cache/registry；无错误绑定且资源增长符合选定策略；[Dynamic Collection](features/DYNAMIC_COLLECTION.md)。
9. **跨 datasource 同 namespace：** 两个 client 使用相同 database/collection、不同实体；交替 CRUD 并检查 registry、逻辑删除和映射；最终实体/namespace 隔离符合契约；[Multi Datasource](features/MULTI_DATASOURCE.md)。
10. **Wrapper 与映射边界：** BSON 单测 + MongoDB；覆盖 null、空集合、重复字段、Wrapper 复用、RegexOptions、复杂 Type/集合及 Map 修复回归；构建 BSON、异常和往返结果有稳定断言；[Query Wrapper](architecture/QUERY_WRAPPER.md) / [Entity Mapping](architecture/ENTITY_MAPPING.md)。
11. **聚合/乐观锁组合：** 多 MongoDB Server 版本；覆盖首 stage 限制、末尾 `$out/$merge`、重复执行、已有 `$inc`、matched/modified 差异；最终 pipeline/update 合法且冲突判定符合契约；[Aggregate](architecture/AGGREGATION.md) / [Optimistic Lock](features/OPTIMISTIC_LOCK.md)。
12. **Boot/Solon 注解绑定：** Boot 2、Boot 3、Boot 4、Solon 最小应用；调用 `@MongoDs`、`@IgnoreLogic`、事务注解的类级/方法级/嵌套/异常路径；拦截器确实进入且返回包装和清理一致；[Startup](architecture/STARTUP_LIFECYCLE.md)。
13. **Recorder 与 Async 的事务结果：** replica set + 审计库/镜像库；提交、回滚、主写失败、保存失败时记录命令顺序和库状态；只有选定一致性策略允许的副作用存在；[Recorder](features/DATA_CHANGE_RECORDER.md) / [Async](features/ASYNC_MULTI_WRITE.md)。
14. **Listener 运行外观：** MongoDB + retry/getMore/bulkWrite/事务；记录线程、requestId/operationId、started/终态和异常 listener；事件配对、数量及失败传播形成可重复断言；[Command Listener](features/COMMAND_LISTENER.md)。
15. **Java/Driver/框架兼容矩阵：** JDK 8/11/17/21、Driver 支持版本、Boot 2/3/4、Solon；执行最小编译与启动 smoke test；Java 与各 Boot 代际下限由实际结果支撑；[Compatibility](COMPATIBILITY.md)。

## D. 构建和发布问题（6）

1. 全 reactor `compile/test/package/verify/install/deploy`、独立 BOM `verify/deploy` 与 JDK 矩阵尚未执行；当前只有 `validate` 结果。[Build / Release](BUILD_AND_RELEASE.md)
2. 根项目导入 reactor 外 BOM，而 BOM 管理 8 个 reactor 构件；需用干净本地仓库和 Central 测试确认 BOM→reactor 的构建与发布顺序。[Build / Release](BUILD_AND_RELEASE.md)
3. `central-publishing-maven-plugin` 的 server id `central` 与 `distributionManagement` 的 `release` 如何组合，Central Portal bundle 是否需要人工 publish/close/release，尚无运行证据。[Build / Release](BUILD_AND_RELEASE.md)
4. source、javadoc、GPG 签名、`central`/`release` 凭据和 settings 的真实要求尚未验证；GPG execution 当前被注释，配置存在不等于 deploy 可用。[Build / Release](BUILD_AND_RELEASE.md)
5. 仓库内未发现 CI、Wrapper、Enforcer 或 toolchains；是否存在外部 CI、secret 和人工发布流程需由维护者或平台确认。[Testing](TESTING.md)
6. 发布产物需执行 Boot 2、Boot 3、Boot 4、Solon 以及必要 sharding starter 的最小消费应用 smoke test；当前未执行。[Compatibility](COMPATIBILITY.md)

## E. 设计决策（8）

1. 加解密、时序创建、监听器和 Recorder 异常应 fail-open 还是 fail-closed？
2. 是否支持动态集合自动索引，以及缓存/registry 是否需要上限、淘汰和关闭清理？
3. Async 是否改为 afterCommit，是否提供结果、重试、取消、去重、深拷贝和 executor shutdown？
4. namespace→实体 Registry key 是否加入 datasource/client，覆盖数据源时是否使旧 collection/registry 失效？
5. Query/Update Chain 的 `clear` 与 Aggregate 缺少 `reset` 是否要统一为公开复用契约？
6. Recorder 是否改为 afterCommit，并用栈式上下文和 finally 恢复 datasource/namespace？
7. Tenant/Logic 聚合增强是否递归子 pipeline、如何处理必须首位/末位的 stage？
8. 分片事务是否继续暴露逐项非原子语义，还是限制/重构为可明确承诺的事务模型？

维护者作出决策后，应更新对应专题；测试只能提供证据，不能代替产品和兼容性选择。
