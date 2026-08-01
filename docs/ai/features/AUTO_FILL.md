# 自动填充

> 审计日期：2026-08-02。Auto Fill 是实体写映射生命周期，不是执行拦截器。映射细节见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)。

## 入口与注册

字段通过 `@CollectionField(fill=FieldFill.INSERT|UPDATE|INSERT_UPDATE)` 进入元数据。用户实现单个 `MetaObjectHandler.insertFill/updateFill`；框架没有默认业务值 Handler，只有负责调度的 `DefaultAutoFillHandler`。

Boot 3/4 枚举 `ApplicationContext.getBeansOfType(MetaObjectHandler.class).values()`，Solon 枚举 `AppContext.getBeansOfType`，逐个覆盖静态 `HandlerCache.metaObjectHandler`；最终只有遍历到的最后一个生效，没有 order、合并或唯一性校验。Handler 通常是容器单例，但框架只保存静态引用，不创建副本、不加锁、不隔离应用上下文。重复初始化/多个上下文会相互覆盖。

## Insert 与 Update

实体 save/insert one/many 逐对象调用 `MongoConverter.writeBySave`：先处理/生成 ID 并写 `_id`，再完成普通字段映射，最后 `DefaultAutoFillHandler.handle(INSERT)`。batch 是逐实体转换和填充。实体 updateById、Entity+Wrapper 走 `writeByUpdate`：先复制已有 ID、映射字段，再 `handle(UPDATE)`；随后调用方移除 `_id` 并构造 `$set`。源码中没有位于 ID 与 fill 之间的独立实体生命周期 callback。

纯 Wrapper update、原始 BSON update、直接传入的 bulkWrite model、逻辑删除转出的 update 没有实体 `TypeInformation` 转换，因此不调用 updateFill。Map 保存分支提前返回，不填充；`Document` 是 Map 子类，同样走 Map 分支。框架 Execute/Mapper 没有独立 replace API；用户直接构造 `ReplaceOneModel` 不填充。`UpdateOptions.upsert(true)` 不触发额外 fill：实体更新入口此前已做 UPDATE fill，纯 Wrapper/BSON update 仍不做。`saveOrUpdate` 只是先 count 后选择实体 save 或实体 update；`saveOrUpdateBatch` 在构造 model 时分别调用 `writeBySave` 或 `ConditionUtil.getUpdate`，因此对应逐实体 INSERT/UPDATE fill，而不是 bulk 拦截器提供 fill。

Handler 收到的 `AutoFillMetaObject.document` 只含被标注的顶层字段及其当前值。`fillValue` 仅在字段存在于这份元对象时写入；`forceFillValue` 可写任意 key 到回写 map，但找不到实体字段时只记录日志，Document 仍会收到该 key。用户预设值是否覆盖完全由 Handler 是否调用 fill/force 决定，框架没有“非 null 自动跳过”。`ConcurrentSkipListMap` 不接受 null value，因此填充 null 会抛异常。异常直接传播并阻止执行。

填充默认会同步回写实体字段；`skipCurrentWriteBack` 仅跳过下一次 `forceFillValue` 的实体回写，不阻止 Document 写入。final/不可反射写字段的行为取决于 `FieldInformation.setValue`，需运行验证。只扫描顶层 `TypeInformation`，不会递归为嵌套对象执行独立填充。

## 映射顺序

实体插入真实顺序为：ID 生成/规范化 → 普通字段循环（TypeHandler → EncryptFieldHandler → DBRefHandler；未命中再 MappingStrategy/默认递归）→ Auto Fill Handler → `converter.write(fillMap, source)` → Tenant/Logic/Dynamic 等普通执行插件 → 高级链 → Driver → ID 回填实体。

Auto Fill 发生在普通字段映射**之后**，但填充值随后通过 `converter.write(Map, Document)` 合并。该 Map 写路径没有实体 `FieldInformation`，所以填充值**不会**再次经过实体字段 TypeHandler、EncryptFieldHandler 或 DBRefHandler；复杂值只走通用 Map/value 转换。Auto Fill 元对象以 `FieldInformation.getCamelCaseName()` 暴露字段，回写 Map 又按 Map key 规则写入；它没有重新读取 `@CollectionField.value`。因此字段重命名时可与先前已映射存储字段形成双字段；全局下划线转换对 Map key 的影响可由源码确认，最终重名/覆盖组合仍应做 BSON 测试。

生命周期没有独立 before/after 实体回调；执行代理只看到完成映射和填充后的 Document。Tenant 后续 `putIfAbsent`，所以若 Auto Fill 使用同一存储 key，填充值优先；但租户本身由 TenantHandler/TenantInterceptor 负责，不属于 MetaObjectHandler 职责。

更新真实顺序为：实体字段映射 → updateFill 合入 Document → 移除 `_id` → 构造 `$set` → 普通 Tenant/Logic filter 增强 → 高级链 → Driver。逻辑 delete 是高级阶段直接构造 BSON update，完全绕过此链，故不触发 updateFill。

## 并发、数据源与动态集合

字段清单 cache 是 `AbstractAutoFillHandler` 实例内的 ConcurrentHashMap；活动 Handler 引用是进程级静态可变字段。框架不保护 Handler 内部状态，并发安全由用户负责。多数据源、动态集合不参与 Handler 选择；实体在取得 collection/进入代理前已经填充，因此切换 namespace 不改变 Handler。Boot 3/4/Solon 共享同一 core 静态缓存机制。

Map/Document 无 Auto Fill，但后续 Tenant/Logic Auto Fill 普通插件仍可能直接改最终 Document；后者是逻辑删除默认值填充，不是 `MetaObjectHandler`。

## 风险与测试

已确认边界：最后枚举 Bean 覆盖且无排序；跨上下文静态污染；Map/Document、Wrapper/BSON update、直接 bulk model、逻辑删除不填充；null 值不被 ConcurrentSkipListMap 接受；填充值不再走实体字段 TypeHandler/加密/DBRef；嵌套对象不递归执行独立 fill。

至少验证 insert/update/INSERT_UPDATE、预设值、null、字段重命名、skip write-back、batch、updateById、Entity+Wrapper、Wrapper-only、upsert/replace、logic delete、TypeHandler/加密/DBRef、Map/Document、并发、多上下文、多数据源/动态集合，以及 Boot 3/4/Solon 多 Bean选择。测试策略见 [TESTING.md](../TESTING.md)，未决项见 [OPEN_QUESTIONS.md](../OPEN_QUESTIONS.md)。

## 关键源码

- `handlers/MetaObjectHandler.java`、`handlers/auto/AbstractAutoFillHandler.java`、`DefaultAutoFillHandler.java`
- `model/AutoFillMetaObject.java`、`annotation/collection/CollectionField.java`、`enums/FieldFill.java`
- `mapping/AbstractMongoConverter.java`、`MappingMongoConverter.java`
- Boot 3/4/Solon `config/MongoPlusAutoConfiguration.java#setMetaObjectHandler`
