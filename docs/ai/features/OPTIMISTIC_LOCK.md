# 乐观锁

> 审计日期：2026-08-02。结论以当前 `mongo-plus-annotation`、`mongo-plus-core` 及 Boot 3/4、Solon 注册源码为准。执行代理顺序见 [EXTENSION_PIPELINE.md](../architecture/EXTENSION_PIPELINE.md)，实体映射见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)。

## 公开入口与注册

版本字段注解是 `@Version`（`com.mongoplus.annotation.collection.Version`），注释声明“仅支持整数类型”。实现是 `OptimisticLockerInterceptor implements AdvancedInterceptor`，没有独立 Handler 或 Ignore 注解。

它不是默认内置功能。用户必须创建实例并通过 core `Configuration.advancedInterceptor(...)`，或在 Boot 3、Boot 4、Solon 容器中声明 `AdvancedInterceptor` Bean。三个集成都只批量收集所有高级拦截器并加入全局静态 `AdvancedInterceptorChain`，没有专门的乐观锁开关或自动 Bean。重复初始化/重复 Bean 会重复加入，没有去重。

拦截器未覆盖 `order()`，所以使用 `Integer.MAX_VALUE`。高级链按 order 降序存储再逐层包装，最后包装者位于最外层，因此运行时较小 order 先进入。`LogicRemoveInterceptor` 明确返回 `Integer.MAX_VALUE - 1`，并非与乐观锁同 order：两者都注册时逻辑删除转换稳定先进入，默认乐观锁随后看到转换后的 update。批量/单个注册和 Bean 枚举只影响同 order 元素；这里的两个默认 order 不相同，注册顺序不会颠倒它们。

公开可配置项：`setAutoInc(Integer)`（默认 1）、`setVersionIsNullException(RuntimeException)`、`setUpdateFailException(RuntimeException)`、`enableRetry(Retry)`。

## 元数据发现

每次命中 save/update/bulkWrite 后，拦截器从 invocation 当前 collection 的 `database.collection` 到 `MongoEntityMappingRegistry` 反查实体类，再以 `TypeInformation.of(clazz).getAnnotationField(Version.class)` 找字段，并按实体类缓存 `FieldInformation`。

- 不从 Mapper 泛型、Wrapper 或 DTO 直接发现版本字段。
- 使用 `FieldInformation.getCamelCaseName()` 作为 BSON 字段名，因此 `@CollectionField` 重命名/全局命名转换参与。
- 字段值以 `Document.getInteger` 读取；实际可确认的是 Integer BSON 值。Long、Short、BigInteger、时间类型虽可能被注解标记，但当前更新算法并不支持，不能视为已支持。
- 多个 `@Version` 字段由 `TypeInformation.getAnnotationField` 的首个匹配行为决定，字段顺序/多字段语义没有契约。
- registry 无映射、`UnClassCollection` 或无 `@Version` 时跳过。缓存只保存非 null 结果；无注解类会重复扫描。

插入时若 Document 没有版本字段或值为 null，写入整数 0；非 null 用户值保留。Map/Document 无实体模式即使手工含版本字段也不会触发拦截器。

## Update 实际链路

```text
实体/Wrapper -> Mapper 生成 filter + update BSON
 -> 实体写映射与 updateFill（执行代理之前）
 -> ExecutorProxy 普通链：Tenant -> Dynamic Collection -> Logic（当前内置顺序）
 -> AdvancedInterceptorChain：OptimisticLockerInterceptor（若用户注册）
 -> 从 invocation 当前 collection 的 registry 找 @Version
 -> 只从 update.$set 读取当前版本
 -> filter 加 version = 当前版本
 -> 从 $set 删除 version
 -> update 顶层 putAll {$inc: {version: autoInc}}
 -> DefaultExecute/SessionExecute -> Driver
 -> 可选 retry / updateFailException
 -> Mapper 将 UpdateResult 转为自身公开返回语义
```

实体 `updateById` 先构建 `_id` Wrapper，再由 `writeByUpdate` 把实体当前版本写入 `$set`，因此可被乐观锁取得。Entity + Wrapper 同理。纯 Wrapper update、原始 BSON、逻辑删除转 update 若未在 `$set` 明确携带版本，首次执行 `autoVersion=false`：默认只记录 debug 并**不加版本条件、不加 `$inc`**；配置 `versionIsNullException` 才抛错。`$setOnInsert`、用户预置 `$inc.version`、filter 中已有版本都不是首次版本来源；replace 不在 `hitLock` 范围。

改写不是安全的深合并。`BsonUtil.addAllToMap(updateBson, {$inc:{version:autoInc}})` 对常见 `Document`/`BasicDBObject` 是顶层 `putAll`，会用新的 `$inc` 整体覆盖用户原有 `$inc`，其中其他字段增量也会丢失。对既非 `Document` 也非 `BSONObject` 的原始 Bson，工具方法在局部新建 `BasicDBObject` 却不把替代对象写回 pair，filter/update 改写可能不生效；准确覆盖哪些 Driver Bson 实现需参数化测试。retry 在没有 `$set` 时即使能从 filter 推版本，随后 `removeFrom(setDocument, ...)` 仍会因 `setDocument == null` 失败。

`hitLock` 只覆盖 SAVE、SAVE_ONE、UPDATE、UPDATE_ONE、BULK_WRITE。replace/物理 remove 不命中；saveOrUpdate 最终走到的实际 save/update 分支决定。BulkWrite 仅处理 `InsertOneModel` 与 `UpdateManyModel`；`UpdateOneModel`、replace/delete model 不处理。Sharding 是否复用取决于最终请求是否仍经过这一全局高级链，分片结果与 retry 的组合无专门实现。

## 版本变化与冲突语义

首次更新的原版本来自 `update.$set[versionField]`，不是数据库查询。实现把该值加入 filter，删除 `$set` 中版本，再追加 `$inc`；增量默认 1，可配置为任意 Integer。没有版本算法接口。

实体 Java 对象不会在 Driver 成功后回写新版本；Driver 前也没有修改实体字段。成功、matchedCount=0、异常或事务回滚后，调用方实体都保留旧版本，因此连续复用同一实体会再次携带旧版本。

默认冲突不会抛异常：Driver 的 `UpdateResult` 原样回到上层，最终 Boolean API 通常按影响数判断失败。只有配置 `updateFailException` 时，拦截器在 `modifiedCount <= 0` 抛该异常。它不检查 `matchedCount`，所以“条件命中但实际未修改”也被当作失败，无法与版本冲突区分。

启用 retry 后同样以 `modifiedCount >= 1` 判成功。首次 Driver 调用若直接抛异常，`beforeRetry` 不会执行，因此 Driver 异常不进入重试。只有拿到零 modified 结果才重试；它不重新读取数据库，而是再次原地处理已修改 BSON，`autoVersion=true` 从 filter 读版本并加 `retry.autoVersionNum`。循环最多再调用 Driver `maxRetryNum` 次；最后一次结果失败后进入 fallback（若有），否则原样返回。`processIntercept=true` 使用 original Execute，跳过剩余高级拦截器；否则复用 invocation 的链位置。异步 retry 最终仍 `.get()` 阻塞调用线程，异常被包装。

## 与其他功能的顺序

- Auto Fill/Entity Mapping：实体先转 Document，`updateFill` 后完成；之后才进入两类执行代理和乐观锁。因此 fill 若写版本字段，会成为 `$set` 中被当作“当前版本”的值，然后被乐观锁移除并改为 `$inc`。TypeHandler/加密也在映射阶段，可能令 `getInteger` 失败。
- Tenant：普通 order 0，在乐观锁前把 tenant 条件加入 filter。
- Dynamic Collection：普通 order 2，在乐观锁前替换 Execute args；高级 invocation 看动态 collection。动态 collection 首次常登记为 `UnClassCollection`，乐观锁因找不到实体版本元数据而跳过。
- Logic Delete：普通 Collection Logic 先增强 delete filter。高级 `LogicRemoveInterceptor` 的 order 是 `MAX_VALUE-1`，默认乐观锁是 `MAX_VALUE`，所以删除转换稳定先运行；`LogicRemove.logic` 随后对内层 target 调用 `executeUpdate`，会进入乐观锁。但转换出的 `$set` 只有逻辑删除字段，没有版本值，于是乐观锁默认静默跳过（配置 `versionIsNullException` 则删除抛错）。因此默认逻辑删除不带版本 filter、不递增版本；物理 remove 也不在 `hitLock` 范围。`@IgnoreLogic` 生效时继续原物理 delete，同样不应用乐观锁。
- Transaction：`ExecutorFactory` 先从事务上下文选择 `SessionExecute`，再包装高级/普通代理。乐观锁参数与冲突判断相同；事务回滚不会恢复实体版本（实体本来也未回写）。
- Multi Datasource：registry key 不含 datasource，只含 `database.collection`，同 namespace 不同实体存在首次登记冲突风险。

详见 [AUTO_FILL.md](AUTO_FILL.md)、[DYNAMIC_COLLECTION.md](DYNAMIC_COLLECTION.md)、[TENANT.md](TENANT.md)、[LOGIC_DELETE.md](LOGIC_DELETE.md) 与 [TRANSACTION.md](TRANSACTION.md)。

## Boot 3、Boot 4、Solon

三者都不默认启用乐观锁；都把容器中的 `AdvancedInterceptor` 批量加入同一个全局静态链并排序。Boot 3/4 使用 Spring `getBeansOfType(...).values()`；Solon 使用 `context.getBeansOfType(...)`。没有乐观锁专用 Ignore AOP。容器 Bean 枚举顺序、重复上下文和同 order 拦截器的最终相对顺序需要组合测试，不应依赖。

## 已确认缺陷、设计选择与运行风险

已确认缺陷/高风险行为：只用 `getInteger`；缺少版本时默认静默绕过；冲突只用 `modifiedCount` 判断；实体版本不回写；已有 `$inc` 被整体覆盖；非 Map 型 Bson 改写可能丢失；bulk 只覆盖两种 model；动态集合 registry 可能使锁静默跳过；默认逻辑删除因不携带版本而绕过锁。

当前设计选择：默认手工注册；初始版本 0；版本以 `$inc` 增长；允许用户配置增量、异常和 Retry；没有专用 Ignore。

需要运行验证：null/Long/Short/BigInteger/Decimal128 的准确异常；各种 Driver Bson 实现的改写是否丢失；retry 对原地已修改 BSON 的多次处理；matched=1/modified=0；逻辑删除配置异常；事务、分片和多数据源组合。Driver 异常不进入 retry、默认 LogicRemove 顺序已由控制流确认，不再列为顺序待验证。

## 测试清单

至少补：updateById 成功及实体不回写；版本冲突；null/Long/Short/BigInteger；重命名版本字段；用户显式版本；updateFill/TypeHandler/加密；matched=0 与 modified=0；Driver 异常；Tenant/Logic/Dynamic；事务回滚；Map/Document；BulkWrite 的 InsertOne/UpdateMany/UpdateOne；replace/remove；retry；Sharding；Boot 3/4/Solon 注册、重复注册和同 order。

## 关键源码

- `mongo-plus-annotation`: `annotation/collection/Version.java`
- `mongo-plus-core`: `interceptor/business/OptimisticLockerInterceptor.java`、`interceptor/AdvancedInterceptor.java`、`AdvancedInterceptorChain.java`
- `mongo-plus-core`: `mapping/TypeInformation.java`、`FieldInformation.java`、`registry/MongoEntityMappingRegistry.java`
- `mongo-plus-core`: `mapper/MongoMapperImpl.java`、`AbstractBaseMapper.java`、`mapping/AbstractMongoConverter.java`
- Boot 3/4/Solon: 各自 `config/MongoPlusAutoConfiguration.java#setAdvancedInterceptor`
