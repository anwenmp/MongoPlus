# Query / Update Wrapper 与 BSON

> 审计日期：2026-08-01。结论以 `mongo-plus-core` 当前源码为准。本文只描述条件构造、BSON 汇合及执行前增强；CRUD 总链见 [CRUD_EXECUTION.md](CRUD_EXECUTION.md)，执行拦截顺序见 [EXTENSION_PIPELINE.md](EXTENSION_PIPELINE.md)。

## 类型关系与职责边界

```text
AbstractChainWrapper<T, Children>
├─ QueryChainWrapper<T, Children>
│  ├─ QueryWrapper<T>
│  └─ LambdaQueryChainWrapper<T>  （增加 list/one/page/count 终结操作）
└─ UpdateChainWrapper<T, Children>
   ├─ UpdateWrapper<T>
   └─ LambdaUpdateChainWrapper<T> （增加 update/remove 终结操作）
```

- `AbstractChainWrapper` 实现 `QueryCondition`，持有查询条件、排序、投影和自定义 BSON 四组状态。
- `QueryChainWrapper` 只增加查询条件的构建实现；`QueryWrapper` 是可直接实例化的普通查询 Wrapper。
- 当前源码不存在独立的 `LambdaQueryWrapper` 类。所谓 Lambda 查询通过同一套 Wrapper 上接收 `SFunction` 的重载完成；带终结操作的具体类型是 `LambdaQueryChainWrapper`。
- `UpdateChainWrapper` 继承全部查询条件能力，另持有更新操作元对象和自定义更新 BSON。因此更新 Wrapper 的左侧仍是查询 filter，右侧才是 `$set/$inc/...` 更新文档。
- `UpdateWrapper` 是可直接实例化的更新 Wrapper。当前源码不存在独立的非 Chain `LambdaUpdateWrapper`；`LambdaUpdateChainWrapper` 才是可执行的 Lambda 更新链。
- `QueryWrapper.lambdaQuery()` 与 `UpdateWrapper.lambdaUpdate()` 调用 `ChainWrappers` 创建新的 chain，并不把当前 Wrapper 已积累的状态转换或复制过去。返回声明也仍是基类 `QueryChainWrapper` / `UpdateChainWrapper`。

## 条件如何成为 BSON

公开条件方法先创建 `ConditionMetaObject(condition, column, value, originalClass, originalField, extraValue)`，再由 `AbstractChainWrapper.addCondition` 按调用顺序保存。核心汇合为：

```text
eq/ne/.../and/or/not
  -> BaseQueryCondition.getBaseCondition(...)
  -> AbstractChainWrapper.conditionMetaObjects
  -> QueryChainWrapper.buildCondition()
  -> BuildCondition.condition().queryCondition(wrapper)
  -> AbstractCondition.queryCondition(List<ConditionMetaObject>)
  -> BuildCondition.queryCondition(single, MongoPlusBasicDBObject)
  -> BaseConditionResult.condition (BasicDBObject / Bson)
  -> AbstractBaseMapper -> Execute -> MongoDB Driver
```

`UpdateChainWrapper.buildUpdateCondition()` 则调用 `Condition.updateCondition(this)`，返回 `MutablePair<BasicDBObject,BasicDBObject>`：left 是上述查询 filter，right 按 `UpdateConditionEnum` 分组形成 `$set/$inc/$push/...`，并合并 `updateCustom` 保存的 BSON。实体 + 查询 Wrapper 更新不使用这组更新操作，而由 `ConditionUtil.getUpdateCondition` 把 converter 生成的 Document 包进 `$set`。

### 常用操作符

| API | 当前 BSON 形成方式 |
|---|---|
| `eq` | `{field: {$eq: value}}`；不是 Driver `Filters.eq` 的简写形态 |
| `ne/gt/gte/lt/lte` | 交给项目内 `Filters` 构造对应 `$ne/$gt/$gte/$lt/$lte` |
| `in/nin/all` | Collection 或 varargs（varargs 先转 `ArrayList`）进入 `$in/$nin/$all` |
| `regex/like` | `BuildCondition` 对两者都固定产生 `$regex` 并追加 `$options: "i"`；带 `RegexOptions` 的 API 将其保存到 `ConditionMetaObject.extraValue`，但 `REGEX/LIKE` 构建分支只读取 `value`，完全不读取 `extraValue` |
| 自定义 BSON | `custom(...)` 转为 `BasicDBObject` 存入独立列表，最终用 `putAll` 合并进 filter；同名键可能覆盖先前结果 |

构建每个查询条件前后都会按 `HandlerCache.conditionHandlerList` 调用 `ConditionHandler.beforeQueryCondition/afterQueryCondition`。当前内置顺序是字段加密、DBRef、ObjectId；这是静态列表插入顺序，未见排序步骤。

## 字段解析

- 字符串字段：原样写入 `ConditionMetaObject.column`；仅 `_id` 的值会在 `BaseQueryCondition.getBaseCondition` 中经 `ObjectIdUtil` 转换，Collection 会逐项转换。
- Lambda 字段：`SFunction` 从 `SerializedLambda` 取得实现类、getter 对应字段和反射 `Field`。字段名优先使用 `@CollectionField.value`，其次 `@ID -> _id`，否则按 `PropertyCache.camelToUnderline` 处理。
- Lambda 的原始类和字段会进入 `ConditionMetaObject`，使加密、DBRef、ObjectId 条件 Handler 能基于注解改值；纯字符串字段没有这份反射元数据。
- 嵌套字段没有自动的 Lambda 属性链解析。当前 Lambda 只解析一个 getter；MongoDB 点路径需调用者使用如 `"address.city"` 的字符串，或直接提供自定义 BSON。嵌套对象值的字段级序列化也不会自动调用 `MongoConverter`。

## AND、OR、NOT 与分组语义

- 顶层连续普通条件写入同一个 `MongoPlusBasicDBObject`，表现为 MongoDB 文档的隐式 AND；相同字段/键的覆盖行为取决于 `MongoPlusBasicDBObject.put`，不应把重复键当作稳定的显式分组 API。
- `and/or/nor(wrapper|function)` 保存一个子 `QueryChainWrapper`。构建时递归转换子 Wrapper 中的条件和自定义 BSON，再交给 `Filters.and/or/nor`，形成显式逻辑数组。
- function 重载创建新的 `QueryWrapper` 供回调填充，因此是实际的嵌套分组入口。
- `not(...)` 当前实现值得特别注意：它把子 Wrapper 保存为条件 `not`，但 `BuildCondition` 将 `NOT` 与 `EXPR` 放在同一分支，取子 filter 的第一个键并调用 `Filters.expr(...)`。这不是可直接等同于 MongoDB 字段级 `$not` 的实现；多键子条件也只取第一个键。应视为兼容风险，语义正确性待回归测试验证。
- `not(ConditionMetaObject)` 先把单条件构造成 BSON 列表再封装；其最终行为同样必须结合 `getBaseCondition(Object)` 记录的方法名和上述 `NOT` 分支看待，不能仅凭 API 名称推断。

## 空值、空集合与 condition=false

- 条件 API 的 boolean 重载均以 `condition ? ... : typeThis()` 短路；`condition=false` 时不创建 `ConditionMetaObject`，因而不解析 Lambda 字段、不转换 `_id` 值，也不校验值。
- `eq/ne/gt/gte/lt/lte` 不主动跳过 null。`eq(field, null)` 直接形成 `$eq: null`；其余操作符把 null 传给对应 Filters/codec，最终 Driver 编码结果与异常边界没有仓内测试，标记为待验证。`regex/like(field, null)` 则会在 `BuildCondition` 的 `value.toString()` 处抛 `NullPointerException`。
- `in/nin/all` 不主动跳过 null 或空集合。null Collection 在构建时强制转换后传给 Filters，具体异常待验证；空集合会保留为 `$in: []`、`$nin: []`、`$all: []`，不等价于“忽略条件”。
- `Condition.isEmpty/isNotEmpty` 只检查查询元对象列表和自定义 BSON 列表；排序、投影及 `UpdateChainWrapper` 的更新操作不计入“非空查询 Wrapper”。

## 多租户、逻辑删除与动态集合

Wrapper 构建本身不处理这三项。它先在 Mapper 层成为 BSON，随后外层普通执行代理逐插件执行 `beforeExecute` 与参数策略：

- `TenantInterceptor` 在 save 时补租户字段，在 remove/update/query/count/bulk filter 及 aggregate match 中补租户条件；已有租户键时通常不覆盖。是否忽略由 `TenantHandler`/`TenantManager` 决定。
- `CollectionLogiceInterceptor` 在 query/count/update/remove/bulk/aggregate 的 filter 或 match 中追加逻辑未删除条件；其实体识别依赖 collection namespace 在 `MongoEntityMappingRegistry` 中的映射。无实体集合映射时跳过。
- `DynamicCollectionNameInterceptor.beforeExecute` 最后把 Execute 参数数组末项的 `MongoCollection` 替换为动态集合。当前内置 order 为 tenant `0`、逻辑删除未覆写默认 order、动态集合 `2`；但注册入口和同 order 顺序不是统一稳定契约。且普通代理把进入时的原 collection 传给同次参数策略，动态替换后其他插件观察哪个集合存在已知边界，详见 `EXTENSION_PIPELINE.md`。

因此动态集合不改 Wrapper/BSON；租户与逻辑删除是在 Wrapper→BSON 之后、Driver I/O 之前增强 filter。动态集合若映射到尚未登记实体的 namespace，逻辑删除能否识别实体还取决于集合获取/注册时机，不应仅按类名推断。

## 兼容风险与测试缺口

- Wrapper 和大量默认接口方法是公开 API；改变自引用泛型、返回类型、`ChainWrappers` 新建语义或条件元对象结构会影响源码兼容和链式类型推断。
- `BuildCondition` 是所有 Wrapper 的共享 BSON 语义点；操作符形态、重复键合并、ConditionHandler 时序、枚举/ObjectId/加密转换改变会影响查询、更新 filter、聚合 match 和逻辑删除。
- Query/Update 边界尤其要覆盖实体更新（converter + `$set`）与 UpdateWrapper（独立更新操作 BSON）两条路径。
- CodeGraph 对核心 Wrapper 报告无覆盖测试；仓库知识库此前也确认没有 `src/test`。至少缺少：全部操作符 BSON 快照；null/空集合/false；字符串与 Lambda 字段注解；点路径；重复字段；AND/OR/NOR/NOT 多层分组；自定义 BSON 冲突；Wrapper clear/复用/并发；租户+逻辑删除+动态集合；实体更新与 UpdateWrapper 的差异。

## 关键源码

模块均为 `mongo-plus-core`：`conditions/AbstractChainWrapper.java`、`conditions/query/QueryChainWrapper.java`、`QueryWrapper.java`、`LambdaQueryChainWrapper.java`、`conditions/update/UpdateChainWrapper.java`、`UpdateWrapper.java`、`LambdaUpdateChainWrapper.java`、`conditions/interfaces/query/**`、`handlers/condition/AbstractCondition.java`、`BuildCondition.java`、`toolkit/Filters.java`、`ConditionUtil.java`、`support/SFunction.java`、`mapper/AbstractBaseMapper.java`、三类 business interceptor。
