# 实体与 Document 映射

> 审计日期：2026-08-01。结论以 `mongo-plus-core` 与 `mongo-plus-annotation` 当前源码为准。本文区分写保存、写更新、顶层读取和嵌套读取，避免把同名转换方法概括为单一流程。

## 核心职责

| 组件 | 已确认职责 | 不负责 |
|---|---|---|
| `MongoConverter` | 公开双向转换契约；批量循环、结果集转换、id 回填的默认实现 | 集合名/连接选择、Driver I/O |
| `AbstractMongoConverter` | 保存/更新生命周期、ID、自动填充、顶层实体读取字段循环、TypeHandler/ReadHandler/ConversionStrategy 协调 | 具体复杂值写分支 |
| `MappingMongoConverter` | 字段写循环、MappingStrategy/简单类型/集合/Map/嵌套对象分派，以及 TypeReference 泛型读取 | namespace 到实体登记 |
| `AnnotationOperate` | 从 `@CollectionName` 解析 database/collection；无显式 collection 时按配置由类名转换 | 字段映射、Document 转换 |
| `MongoEntityMappingRegistry` | 线程安全地保存 `MongoCollection.getNamespace().getFullName()` → entity class，供逻辑删除、乐观锁等按集合反查实体 | 实体字段元数据、序列化 |

`CollectionManager` 首次创建并缓存 collection 时登记 namespace→class；显式无实体模式登记 `UnClassCollection`。其缓存键当前仅是 `collectionName`，而方法接受 `dsName`，跨数据源同名集合复用的准确隔离语义属于待验证兼容风险。

## 实体到 Document

### 保存

```text
AbstractBaseMapper.writeBySave / batch
  -> MongoConverter.writeBySave(entity, Document)
  -> Map? writeMapInternal 后直接返回
  -> TypeInformation.of(entity)
  -> @ID：已有值规范化；否则 IdGenerateHandler 生成并按配置转换
  -> document["_id"]（@ID.saveField 可再保存实体字段名）
  -> MappingMongoConverter.write(...)
     -> processFields（顶层过滤 @ID）
     -> FieldHandler 链
     -> MappingStrategy / simple / collection / map / nested object
  -> DefaultAutoFillHandler.handle(INSERT)
  -> Execute.insert -> Driver
  -> reSetIdValue / batchReSetIdValue 回填实体或 Map
```

已有字符串 id 只要 `ObjectId.isValid` 且本身不是 `ObjectId` 就直接转成 `ObjectId`。自动生成 id 则先看 `PropertyCache.objectIdConvertType`，否则向实体字段类型转换。顶层普通字段循环过滤 `@ID`，因此 `_id` 由生命周期代码单独控制。

### 更新

`writeByUpdate` 要求实体存在 `@ID` 字段；值非 null 时先写 `_id`，随后字段映射，再执行 `DefaultAutoFillHandler.handle(UPDATE)`。不同调用方随后处理 `_id`：

- `ConditionUtil.getUpdateCondition(wrapperConditions, entity, converter)` 移除 `_id`，把其余 Document 包入 `$set`，filter 来自 Wrapper。
- `ConditionUtil.getUpdate(entity, converter)` 调用 `ExecuteUtil.getFilter(document)` 提取 `_id` filter；该方法同时从传入的 Document 原地删除 `_id`，之后才把剩余 Document 包入 `$set`。因此当前两条实体更新路径的 update document 都不包含 `_id`。
- `UpdateChainWrapper` 不把实体映射为更新文档，而从独立更新操作列表构建 BSON。

### 字段名和嵌套值

- `@CollectionField.value` 非空时作为存储字段名；否则可按 `PropertyCache.camelToUnderline` 转下划线。
- `@CollectionField.isObjectId` 在字段循环中优先把值交给 `ObjectIdUtil`。
- null 是否省略由全局 `PropertyCache.ignoringNull` 与字段 `ignoreNull` 共同决定。
- 复杂对象递归 `writeInternal(..., new Document())`，此时 `filterId=false`，所以嵌套对象的 `@ID` 不会按顶层规则过滤或生成 `_id`，而会作为按字段名映射的普通字段进入流程。
- 集合/数组逐元素递归；Map 只允许简单 key，key 字符串可下划线转换，但以 `$` 开头的 key 不转换；复杂 key 抛 `MongoPlusWriteException`。

## Document 到实体

```text
AbstractBaseMapper 收到 Driver MongoIterable<Document>
  -> MongoConverter.read/readDocument
  -> Map 目标：convertKeysToCamelCase(document)
     或实体目标：EntityRead.read(...)
  -> AbstractMongoConverter.readInternal(document, typeReference, useIdAsFieldName=true)
  -> TypeInformation.of(clazz)，逐字段取值
     -> TypeHandler.getResult（若配置）
     -> ReadHandler 链（解密/脱敏/DBRef）
     -> 无非 null TypeHandler 结果时，按 TypeReference 进入 MappingMongoConverter.readInternal
     -> ConversionStrategy / collection / map / nested entity
  -> @ExtraFields 收集未使用字段
  -> TypeInformation.getInstance()
```

顶层读取 `useIdAsFieldName=true`，`@ID` 从 `_id` 取值；嵌套实体由 `DefaultConversionStrategy` 递归时传 `false`，因此使用普通/注解字段名而不是 `_id` 特例。Document 目标直接原样返回。

集合和 Map 通过 `TypeReference` 的 `ParameterizedType` 解析泛型：Collection 使用第 0 个实参，Map 使用 value 的第 1 个实参；无参数类型退化为 `Object`。多维集合有单独分支，但递归实现对具体容器类型、数组及非 `ArrayList` Driver 值存在强制类型假设，完整边界待测试验证。

## TypeHandler 与 MappingStrategy 的真实优先级

不能把二者说成对称的同级插件。

### 写方向

1. 字段名、忽略字段、顶层 ID 过滤先确定。
2. `@CollectionField.isObjectId` 可先给 `obj` 赋值。
3. 依 `HandlerCache.fieldHandlers` 当前顺序运行所有激活 Handler：`TypeHandlerFieldHandler` → `EncryptFieldHandler` → `DBRefHandler`。后运行且激活的 Handler 会覆盖前一个 `obj`；没有“命中即停止”。
4. 最终 `obj != null` 时直接写 BSON，跳过 MappingStrategy 和默认递归；Handler 返回 null 则继续默认分支。
5. 默认分支中，`MappingStrategy` 最优先；命中后直接返回。未命中才依次判断 simple/Mongo type、集合/数组、Map、嵌套对象。

因此仅在没有后续 Handler 覆盖且 TypeHandler 返回非 null 时，TypeHandler 高于 MappingStrategy；加密/DBRef 可覆盖 TypeHandler。MappingStrategy 也只按精确 class 查缓存（enum 归一到 `Enum.class`），不是 assignable 匹配。

### 读方向

1. 对配置了 TypeHandler 的字段先计算 `resultObj = getResult(rawValue)`。
2. ReadHandler 仍会按 order 处理局部 `obj`；这一点不因 TypeHandler 已返回非 null 而短路。当前内置初始顺序是解密（order 0）→脱敏（order 1）→ DBRef（默认最大 order）；动态增加时会按 order 重排。
3. 若 TypeHandler 结果非 null 且 ReadHandler 链没有中止，最终赋值采用 TypeHandler 结果，ReadHandler 对 `obj` 的改写不会取代它。但任一 ReadHandler 的 `discontinue().apply(obj)` 返回 true 时会直接跳过当前字段的后续转换和赋值；当前 `DBRefHandler` 在处理结果为 null 时会触发该中止。
4. TypeHandler 返回 null 时，才把 ReadHandler 处理后的 `obj` 交给 `MappingMongoConverter.readInternal`；这里按目标 class 精确找 ConversionStrategy，找不到时 enum 使用 enum 策略，最终回退 Object/`DefaultConversionStrategy`。

结论：写方向的 `MappingStrategy` 与读方向的 `ConversionStrategy` 是两套接口；自定义 MappingStrategy 不会自动提供反向读取。所谓“双向 MappingStrategy”在当前源码中不存在，必须分别注册写、读策略或 TypeHandler。

## 类型行为

- 枚举写：MappingCache 内置 `EnumMappingStrategy` 优先于 simple fallback；查询条件另由 `AbstractCondition` 使用 `@EnumValue(valueStore=true)` 字段，否则 enum name。读由 `EnumConversionStrategy`。三处行为必须组合测试。
- 时间/数字：读缓存显式包含整数、浮点、BigDecimal/BigInteger、Date、Instant、LocalDate/Time/DateTime 等 ConversionStrategy；写侧除 BigInteger MappingStrategy 外主要依赖 simple type holder/Driver codec。不能由读缓存反推写格式。
- 集合/数组：写递归处理元素；byte[] 作为简单写特例保留。读集合按泛型递归，接口目标默认 `ArrayList`。
- Map：实体字段 Map 按 value 泛型转换；以 `Class<Map>` 调用 `MongoConverter.read/readDocument` 时，`convertDocument` 走 key 驼峰转换捷径。以 `TypeReference<Map<...>>` 读取则直接进入 `read(..., typeReference)` / `readInternal`，不经过该驼峰捷径；两者不应混为同一行为。
- 泛型：`TypeReference` 保留参数化类型；裸类型退化为 Object。通配符、TypeVariable、GenericArrayType 在 `getRawClass` 中没有分支，标记为待验证/可能不支持。
- ObjectId：保存 ID、字段注解、Lambda 查询 `_id` 以及 ConditionHandler 是不同转换点；修改其中之一需验证四条路径一致。

## 自动填充与字段加密

- 自动填充发生在普通字段映射之后、执行代理之前，只用于实体保存/更新；Map 写入提前返回，不执行实体自动填充。
- 写字段加密是 `EncryptFieldHandler`，位于字段 Handler 链；查询 Lambda 条件的加密由 `EncryptorConditionHandler`，纯字符串字段缺少反射注解信息；读取解密由 `FieldEncryptApply`。
- 若字段同时配置 TypeHandler、加密或 DBRef，写侧后 Handler 覆盖前 Handler，读侧 TypeHandler 非 null 结果优先于解密后的局部值，组合行为高风险且无回归测试。

## 实体模式与无实体 Map 模式

实体模式由 `DefaultBaseMapperImpl`/`MongoMapperImpl` 解析类对应 namespace，使用 TypeInformation、字段注解、ID、自动填充、加密和泛型映射。Map 保存直接 `writeMapInternal`：无实体 ID 生成、自动填充或字段注解；插入后只在缺少 `_id` 时向 Map 回填 Driver 生成值。

显式 collection 的无实体模式由 `CollectionManager` 关联 `UnClassCollection`。逻辑删除和乐观锁等依赖 registry 反查真实实体的能力可能跳过或不可用。`Class<Map>` 查询结果会调用 `convertKeysToCamelCase`，`TypeReference<Map<...>>` 不走该捷径；两者都不应与直接返回原始 `Document` 混淆。

## Driver 兼容代码与风险

- Driver API 边界在 `execute/instance/DefaultExecute.java`、`SessionExecute.java` 和 `conn/ConnectMongoDB.java`；codec 汇合在 `cache/codec/MapCodecCache.java` 及 `bson/OverridableUuidRepresentationCodecProvider.java`。
- Wrapper、自定义 BSON 和更新 BSON 多处调用 `toBsonDocument(..., MapCodecCache.getDefaultCodecRegistry())`，因此 codec registry 变化会同时影响条件和映射。
- `aggregate/pipeline/FillField.java` 直接导入 Driver internal 类 `com.mongodb.internal.client.model.AbstractConstructibleBsonElement`，这是明确的 Driver 升级风险点。
- 根 POM 当前由 MongoDB Driver BOM `5.4.0` 管理版本；本文未修改 POM。不同 Driver 版本的完整源码/二进制兼容矩阵不在本次范围，待兼容专题验证。

## 影响范围与测试缺口

修改映射逻辑至少验证：save/update/batch 与 id 回填；实体、Map、Document；顶层/嵌套 ID；字段重命名/下划线/null；ObjectId；TypeHandler+加密+DBRef；MappingStrategy+ConversionStrategy；枚举/时间/数字；集合/Map/多层泛型；自动填充；逻辑删除/租户/乐观锁通过 registry 的实体识别；事务与 Driver codec。

CodeGraph 对 `MappingMongoConverter`、`MappingStrategy` 等报告无覆盖测试，知识库此前确认仓库没有 `src/test`。尤其缺少策略优先级、Handler 返回 null、多个 Handler 同时激活、顶层/嵌套读取差异、裸/复杂泛型、Map 模式、跨数据源同名 collection registry/cache、Driver 升级的回归证据。

## 关键源码

- `mongo-plus-core`: `mapping/MongoConverter.java`、`EntityRead.java`、`AbstractMongoConverter.java`、`MappingMongoConverter.java`、`TypeInformation.java`、`FieldInformation.java`
- `mongo-plus-core`: `handlers/field/TypeHandlerFieldHandler.java`、`EncryptFieldHandler.java`、`handlers/read/FieldEncryptApply.java`、`cache/global/HandlerCache.java`、`MappingCache.java`、`ConversionCache.java`
- `mongo-plus-core`: `strategy/mapping/**`、`strategy/conversion/**`、`handlers/auto/**`、`handlers/collection/AnnotationOperate.java`、`registry/MongoEntityMappingRegistry.java`、`conn/CollectionManager.java`
- `mongo-plus-annotation`: `annotation/ID.java`、`annotation/collection/CollectionField.java`、`CollectionName.java`、`annotation/comm/FieldEncrypt.java`
