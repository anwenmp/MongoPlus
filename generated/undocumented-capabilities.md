# 官网未明确介绍的能力及候选

范围限定：只针对 [文档检查清单](../tools/capability-indexer/doc-review.json) 已检查正文，不声称完成全站分析。字段加密导航链接不可用，单列为文档未核实。异步多写已在官网备份页介绍，不计为未记录。

## 源码已核验，支持契约仍需维护者确认

### 聚合阶段体与完整阶段的编码边界 (`mp.stage-boundaries`)

project(Bson) 包装阶段体为 $project；旧 sort(Bson) 透传参数，sortSpecification(Bson) 明确包装为 $sort。此差异影响自然语言到 API 的选择。

- 官网检查：`not_explicit_in_reviewed_pages`。官网 aggregate 列出 sort(Bson)/project(Bson)，未明确解释当前 sortSpecification 和透传差异。
- 真实 API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#sortSpecification`；`mongo-plus-core:com.mongoplus.aggregate.LambdaAggregateWrapper#sort`；`mongo-plus-core:com.mongoplus.aggregate.LambdaAggregateWrapper#project`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java:781](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java#L781)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:321](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L321)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:326](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L326)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:401](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L401)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:276](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L276)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:281](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L281)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:286](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L286)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:291](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L291)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:316](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L316)。
- 测试：有相关测试源码，未执行；见完整能力条目。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：新代码需要排序规格体时使用 sortSpecification；保留旧 sort 的调用契约。
- 人工确认：不能依据参数同为 Bson 就假定相同包装规则。；同一投影混用 include/exclude 的服务端合法性不由 Core 编码成功保证。
- [完整条目](capability-index.md#mp.stage-boundaries)。

### 接收实体未声明的额外字段 (`mp.extra-fields`)

读取实体时 @ExtraFields 的 Map 收集未被实体字段消费的文档键，支持忽略列表、键名转换和 Map 类型选择。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-annotation:com.mongoplus.annotation.collection.ExtraFields`；`mongo-plus-core:com.mongoplus.holder.ExtraFieldsHolder`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:224](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L224)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：在实体的 Map 字段标注 @ExtraFields；由默认 converter 读取路径直接处理。
- 人工确认：缺少专用读写往返测试；人工确认与字段排除、_id、键名冲突的组合。
- [完整条目](capability-index.md#mp.extra-fields)。

### 单值 DBRef 引用映射 (`mp.dbref`)

DBRefHandler 把被引用对象的 @ID 写成 Driver DBRef，读取时按引用数据库/集合与 id 查询并转换。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-annotation:com.mongoplus.annotation.collection.DBRef`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:64](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L64)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:69](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L69)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:85](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L85)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java:18](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L18)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:20](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：在引用字段标注 @DBRef；被引用类型应含 @ID 及可解析 namespace。
- 人工确认：仅静态确认单值路径；不承诺集合引用、懒加载、级联保存或删除。；读取会增加查询，跨源/事务组合未验证。
- [完整条目](capability-index.md#mp.dbref)。

### 实体字段加密、解密与查询值转换 (`mp.field-encryption`)

@FieldEncrypt 在实体写入链加密，读链按 findDecrypt 解密；有原字段元数据的查询条件路径会转换查询值。

- 官网检查：`navigation_link_unusable`。官网导航出现字段加密主题，但本次该链接落到首页，未取得字段加密专页正文；property 只确认全局密钥配置。
- 真实 API：`mongo-plus-annotation:com.mongoplus.annotation.comm.FieldEncrypt`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L26)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:31](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L31)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java:20](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java#L20)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/EncryptorConditionHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/EncryptorConditionHandler.java#L26)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:20](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：标注 @FieldEncrypt 并提供算法所需密钥；默认链接线，检查注解和全局 encryptor 配置。
- 人工确认：原始 BSON、字符串字段和 wrapper-only 更新不能假定有相同加密处理。；不承诺随机加密算法支持等值检索或范围检索。
- [完整条目](capability-index.md#mp.field-encryption)。

### 有序字段写入处理链 (`mp.field-chain`)

FieldHandlerChain 在注册时按 order 排序；转换过程把前序非 null 结果作为 currentValue 传递给后序 FieldHandler。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-core:com.mongoplus.handlers.FieldHandler`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#register`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#registerAll`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java:106](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L106)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java:36](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java#L36)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java:47](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java#L47)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：通过 FieldHandlerChain.getInstance().register/registerAll 显式注册；没有确认通用 FieldHandler Bean 自动收集。
- 人工确认：旧单参数 handler 标记 Deprecated；双参数默认委托旧实现。；null 返回值表示保留当前值；全局可变链应在初始化期注册。；HandlerCache.fieldHandlers 是 Deprecated 兼容别名。
- [完整条目](capability-index.md#mp.field-chain)。

### 读取字段加工与中止赋值 (`mp.read-chain`)

ReadHandler 在字段赋值前按顺序读取转换值，activate 控制参与，discontinue 可以终止该字段的读取赋值路径。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-core:com.mongoplus.handlers.ReadHandler`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:105](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L105)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：注册 ReadHandler Bean 或 HandlerCache.addReadHandler；保留框架默认解密/脱敏/DBRef 处理器。
- 人工确认：discontinue 终止的是当前字段 lambda，不能解释为中止整个查询。；TypeHandler 和通用转换与 ReadHandler 的组合应按实际顺序核验。
- [完整条目](capability-index.md#mp.read-chain)。

### 运行时追加命名数据源 (`mp.runtime-datasource`)

DataSourceManager.appendTempDataSource 创建客户端并按数据库名登记 collection managers，isOverride 决定是否覆盖已存在名称。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：注入 DataSourceManager 并显式调用 appendTempDataSource(dsName, baseProperty, isOverride)。
- 人工确认：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。
- [完整条目](capability-index.md#mp.runtime-datasource)。

### 可选数据源分片路由 (`mp.sharding`)

分片模块按执行种类选择目标数据源，支持用户策略与负载选择，并替换执行 collection；不是跨所有分片结果合并引擎。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-sharding:com.mongoplus.sharding.AbstractDataSourceShardingHandler`；`mongo-plus-sharding:com.mongoplus.sharding.DataSourceShardingStrategy`；`mongo-plus-sharding:com.mongoplus.interceptor.DataSourceShardingInterceptor`。
- 源码：[mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77](../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L77)；[mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11](../mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：加入 sharding 模块及相应 Starter 或显式注册插件；自定义 handler/strategy 按构造参数传入。
- 人工确认：实现要求分片策略为最后拦截器；顺序与并发组合需验证。；一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。
- [完整条目](capability-index.md#mp.sharding)。

### Driver BSON Codec 扩展 (`mp.custom-codec`)

MongoPlusCodec 扩展 Driver Codec；容器创建工厂时先收集 codec，MongoUtil 将其与默认 registry 合并安装到新客户端。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-core:com.mongoplus.codecs.MongoPlusCodec`；`mongo-plus-core:com.mongoplus.cache.codec.MongoPlusCodecCache#addCodec`。
- 源码：[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:86](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L86)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)。
- 测试：未找到专用行为测试。
- 外部可用性：存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。
- 启用：在客户端创建前注册 MongoPlusCodec Bean；core 可显式加入 cache 后创建客户端。
- 人工确认：已创建客户端不会因后续注册自动重建 registry。；Driver registry 与某些 BSON 构建测试使用的 registry 不同，不能承诺所有入口一致。
- [完整条目](capability-index.md#mp.custom-codec)。

## 证据不足的候选

### 字典字段回写候选 (`mp.dictionary-bind`)

FieldBindHandler 能按 @FieldBind 将源值交给用户 DataBindHandler 写入目标属性；尚未确认无需手动接线的完整使用契约。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-annotation:com.mongoplus.annotation.collection.FieldBind`；`mongo-plus-core:com.mongoplus.handlers.DataBindHandler`；`mongo-plus-core:com.mongoplus.handlers.read.FieldBindHandler`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java:34](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java#L34)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。
- 测试：未找到专用行为测试。
- 外部可用性：尚未确认完整使用契约。
- 启用：未找到内置 FieldBindHandler 自动注册；候选接线为显式 new FieldBindHandler(dataBindHandler) Bean/读链注册，需验证。
- 人工确认：添加最小外部示例，验证目标字段命名、字段顺序和 Map/DTO 行为。
- [完整条目](capability-index.md#mp.dictionary-bind)。

### 跨独立数据源原子事务候选 (`mp.cross-source-atomic`)

分片模块有多会话提交/回滚协调代码，但证据不能建立跨独立数据源的原子提交保证。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。
- 源码：[mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)；[mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)。
- 测试：未找到专用行为测试。
- 外部可用性：尚未确认完整使用契约。
- 启用：分片 Starter 更换 TransactionHandler 后可进入协调代码；不是已确认的原子事务启用方式。
- 人工确认：需要设计者确认支持契约，并用部分提交失败、网络中断及补偿场景验证。
- [完整条目](capability-index.md#mp.cross-source-atomic)。

### NonNull 自动参数校验候选 (`mp.nonnull-validation`)

@NonNull 是 JSR-305 类型限定别名；当前未找到把其自动接入数据库操作前运行时校验的消费者。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。
- 源码：[mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)。
- 测试：未找到专用行为测试。
- 外部可用性：尚未确认完整使用契约。
- 启用：没有确认的自动启用入口；统一执行前校验应考虑公开执行拦截器。
- 人工确认：维护者确认是否仅供静态分析，以及是否计划提供运行时校验。
- [完整条目](capability-index.md#mp.nonnull-validation)。

## 内部/开发工具，不推荐为业务 API

### 命名空间生命周期感知内部接线 (`mp.namespace-aware`)

Aware/NamespaceAware 注册到缓存；DefaultBaseIndexImpl 解析 namespace 时分发感知回调，逻辑删除实现使用该机制登记元数据。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-core:com.mongoplus.aware.Aware`；`mongo-plus-core:com.mongoplus.aware.impl.NamespaceAware`。
- 源码：[mongo-plus-core/src/main/java/com/mongoplus/cache/global/AwareHandlerCache.java:14](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/AwareHandlerCache.java#L14)；[mongo-plus-core/src/main/java/com/mongoplus/logic/LogicNamespaceAware.java:11](../mongo-plus-core/src/main/java/com/mongoplus/logic/LogicNamespaceAware.java#L11)；[mongo-plus-core/src/main/java/com/mongoplus/index/impl/DefaultBaseIndexImpl.java:95](../mongo-plus-core/src/main/java/com/mongoplus/index/impl/DefaultBaseIndexImpl.java#L95)。
- 测试：未找到专用行为测试。
- 外部可用性：主要为内部接线或开发工具，不建议业务依赖。
- 启用：当前用于框架初始化；不推荐以内部静态缓存构建业务事件系统。
- 人工确认：public 可见不代表稳定的业务扩展契约；未找到专门使用测试。
- [完整条目](capability-index.md#mp.namespace-aware)。

### 源码 API 证据生成工具内部入口 (`mp.developer-index`)

现有 mongo-plus-indexer 提供源码 API/聚合语义证据生成；属于开发工具模块，不是 MongoDB 业务执行能力。

- 官网检查：`not_explicit_in_reviewed_pages`。已检查正文未找到明确说明。
- 真实 API：`mongo-plus-indexer:com.mongoplus.indexer.MongoPlusIndexer`。
- 源码：[mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:14](../mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java#L14)；[mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:44](../mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java#L44)。
- 测试：未找到专用行为测试。
- 外部可用性：主要为内部接线或开发工具，不建议业务依赖。
- 启用：参照 mongo-plus-indexer/README.md；本任务没有调用或修改任何已有 API 索引生成器。
- 人工确认：此处只导航现有工具；不能推荐为业务查询 API。；本次 capability 产物只消费 generated 下 JavaParser 声明索引。
- [完整条目](capability-index.md#mp.developer-index)。

