# MongoPlus 能力发现导航

先按用户需求选择能力，再沿真实 API、实现和测试证据核验重载。这里只做能力导航，不替代 API 索引。

[机器索引](capability-index.jsonl) · [分析报告](capability-review.md) · [官网未明确介绍项](undocumented-capabilities.md) · [离线检索验证](capability-retrieval-tests.md)

所有“核验”均指静态实现/测试源码阅读；本轮没有运行 MongoDB 或 Java 测试。候选/内部项不可直接作为支持方案。

| 一级 | 二级 | 数量 |
| --- | --- | ---: |
| 数据读写 | 实体访问 | 1 |
| 数据读写 | 条件查询 | 2 |
| 数据读写 | 分页与结果 | 2 |
| 数据读写 | 更新与批量 | 1 |
| 数据读写 | 原生访问 | 1 |
| 聚合计算 | 管道编排 | 1 |
| 聚合计算 | 关联与分支 | 1 |
| 聚合计算 | 表达式 | 1 |
| 聚合计算 | 编码边界 | 1 |
| 实体与字段映射 | 命名与标识 | 2 |
| 实体与字段映射 | 类型转换 | 3 |
| 实体与字段映射 | 字段增强 | 3 |
| 实体与字段映射 | 关联映射 | 1 |
| 安全隔离与一致性 | 租户与删除 | 2 |
| 安全隔离与一致性 | 并发与事务 | 3 |
| 安全隔离与一致性 | 字段保护 | 3 |
| 安全隔离与一致性 | 操作保护 | 2 |
| 扩展与事件 | 执行扩展 | 1 |
| 扩展与事件 | 命令事件 | 1 |
| 扩展与事件 | 字段扩展 | 2 |
| 扩展与事件 | 生命周期 | 1 |
| 连接路由与集成 | 连接配置 | 2 |
| 连接路由与集成 | 数据源路由 | 3 |
| 连接路由与集成 | 集合路由 | 1 |
| 连接路由与集成 | 容器接入 | 1 |
| 连接路由与集成 | 开发工具 | 1 |
| 集合运维 | 索引管理 | 1 |
| 集合运维 | 时序集合 | 1 |
| 集合运维 | 备份与镜像 | 2 |
| 集合运维 | 变动记录 | 1 |

## 数据读写

### 实体访问

<a id="mp.entity-crud"></a>
#### 实体 CRUD 与业务层入口 (`mp.entity-crud`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

实体 Mapper、Repository、Service 最终委托 BaseMapper 完成保存、读取、更新和删除；ServiceImpl 本身不新增事务语义。

典型需求：用实体对象完成增删改查；给业务服务提供通用持久层。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper`；`mongo-plus-core:com.mongoplus.repository.IRepository`；`mongo-plus-core:com.mongoplus.service.IService`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java:91](../mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java#L91)：实体 Mapper 委托 BaseMapper（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:84](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L84)：转换实体、执行写入、回写 ID（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/service/impl/ServiceImpl.java:11](../mongo-plus-core/src/main/java/com/mongoplus/service/impl/ServiceImpl.java#L11)：业务层继承入口（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[crud](https://mongoplus.com/docs/crud.html)；[mapper](https://mongoplus.com/docs/mapper.html)

启用：扫描继承 MongoMapper<T> 的接口，或注册 RepositoryImpl/ServiceImpl 子类；扫描接线见容器接入。

限制：

- 返回 Boolean 是框架对 Driver 结果的封装，不能代表所有业务语义。
- 实体 saveBatch 不接受空集合；DefaultBaseMapperImpl 直接读取首元素。


### 条件查询

<a id="mp.conditional-query"></a>
#### 条件查询、排序与投影 (`mp.conditional-query`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

QueryWrapper 与共享条件接口组合等值、范围、逻辑、数组、正则、文本和地理条件，构建 BSON；查询终结方法应用筛选、排序与投影。

典型需求：查找年龄在区间内且姓名匹配的用户；按时间倒序查询并只返回部分字段；查询数组元素和附近的位置。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)：按条件元对象分派构造操作符（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)：实际 BSON 筛选编码（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:281](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L281)：应用 Wrapper 并执行查询（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`：筛选 BSON 的类型、字段和嵌套数组顺序；不是数据库查询测试。

官网：[conditionBuild](https://mongoplus.com/docs/conditionBuild.html)

启用：通过 Wrappers/QueryWrapper 构建条件，传入 Mapper 或使用绑定 Mapper 的 chain。

限制：

- condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。
- Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。


<a id="mp.nested-fields"></a>
#### 嵌套文档字段路径 (`mp.nested-fields`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

FieldChain 串联字段引用，FunctionUtil 可生成普通或带 $ 的嵌套路径，供查询及聚合复用。

典型需求：不手写字符串访问订单中客户的姓名。

API：`mongo-plus-core:com.mongoplus.function.FieldChain`；`mongo-plus-core:com.mongoplus.toolkit.FunctionUtil`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/function/FieldChain.java:40](../mongo-plus-core/src/main/java/com/mongoplus/function/FieldChain.java#L40)：拼接字段链及可选 $ 前缀（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/FunctionUtil.java:21](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/FunctionUtil.java#L21)：解析 Lambda 字段及映射名称（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[nestedField](https://mongoplus.com/docs/nestedField.html)

启用：直接调用 FieldChain.of(...).then(...).build(...)；结果作为字段路径传入 API。

限制：

- 路径构建不验证数据库内的数据结构；不自动完成关联查询。


### 分页与结果

<a id="mp.pagination"></a>
#### 分页、计数与分页结果 (`mp.pagination`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

提供带总数的 PageResult 和只取页数据的 pageList，按页码/大小计算 skip 和 limit。

典型需求：每页显示二十条并返回总记录数；只查询当前页不要总数。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper#page`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#pageList`；`mongo-plus-core:com.mongoplus.model.PageResult`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:386](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L386)：统计总数并构造页结果（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:420](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L420)：计算偏移并读取页数据（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[crud](https://mongoplus.com/docs/crud.html)

启用：传入 Wrapper、pageNum/pageSize 或 PageParam。

限制：

- 属于偏移分页；不能据此承诺游标分页或高并发一致快照。
- 稳定翻页需要调用方提供稳定排序。


<a id="mp.result-mapping"></a>
#### 查询结果 DTO 与泛型接收 (`mp.result-mapping`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

查询和聚合允许操作实体与返回类型分离；Class<R> 接收 DTO，TypeReference<R> 携带泛型信息，converter 分派实体、Map 和集合读取。

典型需求：查询结果只映射到报表 DTO；把嵌套 Map 和 List 读成泛型结果。

API：`mongo-plus-core:com.mongoplus.mapping.TypeReference`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)：Document/Map/实体读取分支（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:236](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L236)：按 TypeReference 分派集合和 Map 转换（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[noEntity](https://mongoplus.com/docs/noEntity.html)

启用：向 list/page/聚合结果方法传入 Class<R> 或匿名 TypeReference<R>。

限制：

- 保留泛型信息不等于任意嵌套集合均受支持；convertCollection 有 ArrayList 转型路径。
- DTO 类型不自动定义查询的集合 namespace。


### 更新与批量

<a id="mp.update-bulk"></a>
#### 原子字段更新与批量写入 (`mp.update-bulk`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

UpdateWrapper/Update chain 组织 set、inc、数组等更新操作；bulkWrite 接收 Driver WriteModel 列表统一提交。

典型需求：库存原子减一；批量插入和修改多条文档；向数组追加元素。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)：提交 Driver models 并汇总结果（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)：BSON 更新执行与实体条件转换（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/conditions/update/UpdateChainWrapper.java:21](../mongo-plus-core/src/main/java/com/mongoplus/conditions/update/UpdateChainWrapper.java#L21)：构建更新 BSON（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[crud](https://mongoplus.com/docs/crud.html)；[conditionBuild](https://mongoplus.com/docs/conditionBuild.html)

启用：构造更新 Wrapper；批量混合操作使用 BaseMapper.bulkWrite 与 Driver models。

限制：

- wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。
- 批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。


### 原生访问

<a id="mp.raw-bson"></a>
#### 无实体 Document 与原始 BSON 访问 (`mp.raw-bson`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

BaseMapper 显式数据库/集合重载操作 Document/BSON；MongoPlusClient 可取得底层 MongoCollection。

典型需求：集合结构不固定不想定义实体；指定数据库和集合执行 BSON 查询。

API：`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.manager.MongoPlusClient#getCollection`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:281](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L281)：显式 namespace 查询和结果转换（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)：显式 namespace BSON 更新（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:39](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L39)：取得 Driver collection（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[noEntity](https://mongoplus.com/docs/noEntity.html)

启用：注入 BaseMapper 或从 Configuration 获取；传递 database/collection。

限制：

- 直接 Driver 操作与 Mapper 插件/转换链不同。
- 实体元数据依赖的逻辑删除、版本、填充不能无条件套用于无实体路径。

文档差异：官网无实体页混有旧 MongoPlusMapMapper 名称；当前索引使用 BaseMapper，不推荐旧名称。


## 聚合计算

### 管道编排

<a id="mp.pipeline"></a>
#### 聚合管道编排与统计 (`mp.pipeline`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

AggregateWrapper/LambdaAggregateWrapper 构建 match、group、bucket、排序、窗口等阶段；Mapper 的聚合读取入口提交管道。

典型需求：按部门分组统计销售额；对数据分桶并进行窗口计算。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)：构建分组阶段（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:176](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L176)：构建分桶阶段（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:319](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L319)：执行并转换聚合结果（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`：审计中的常用 Stage 重载仅追加一次、返回相同 receiver 并精确编码。

官网：[aggregate](https://mongoplus.com/docs/aggregate.html)

启用：建立独立管道，传入 Mapper 聚合入口，或使用绑定 chain。

限制：

- 部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。
- 阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。


### 关联与分支

<a id="mp.nested-pipeline"></a>
#### 关联查询与多分支子管道 (`mp.nested-pipeline`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

lookup/unionWith/facet 可以消费独立 Aggregate 子管道，保留内部阶段顺序；关联结果的排序和条数限制放在子管道内；lookup 还可传递 Variable 条目。

典型需求：关联集合后对子管道结果排序并限制条数；关联用户表并只保留最新一条；一次聚合同时输出明细和排行榜；合并历史集合的数据。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`；`mongo-plus-core:com.mongoplus.aggregate.Aggregate#facet`；`mongo-plus-core:com.mongoplus.aggregate.pipeline.Variable`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)：读取子 Aggregate 的完整 pipeline（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:568](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L568)：构造命名分支（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:668](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L668)：集合合并子管道（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`：lookup 内部排序后 limit，内外 receiver 独立。
- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`：两个独立 facet 分支有序组成一个 Stage。
- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`：unionWith 保留子管道顺序。

官网：[aggregate](https://mongoplus.com/docs/aggregate.html)；[otherCondition](https://mongoplus.com/docs/otherCondition.html)

启用：各子管道单独创建 Aggregate；再交给外层 lookup/facet/unionWith。

限制：

- 子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。
- 顶层租户/逻辑删除增强不能推定递归注入关联子管道。
- BSON 结构测试不证明关联字段或变量在服务器上有效。


### 表达式

<a id="mp.expressions"></a>
#### 聚合表达式与计算投影 (`mp.expressions`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

AggregateOperator、ConditionOperators、Accumulators 和 Projections 组合条件、数值、日期、数组和累加表达式，作为聚合阶段的参数。

典型需求：计算单价乘数量作为订单总额；根据条件返回不同等级；把美元开头的字符串当常量而不是字段引用。

API：`mongo-plus-core:com.mongoplus.aggregate.pipeline.AggregateOperator#literal`；`mongo-plus-core:com.mongoplus.conditions.operation.ConditionOperators#cond`；`mongo-plus-core:com.mongoplus.aggregate.pipeline.Projections#computed`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java:46](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java#L46)：保留值为 $literal 表达式（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java:41](../mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java#L41)：条件表达式编码（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Projections.java:49](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/Projections.java#L49)：计算字段 BSON（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java:106](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java#L106) `literalKeepsTypesValuesAndAllOrders`：literal 常量值、运行时 BSON 类型和顺序。
- [mongo-plus-core/src/test/java/com/mongoplus/conditions/operation/ConditionOperatorsCondBsonTest.java:56](../mongo-plus-core/src/test/java/com/mongoplus/conditions/operation/ConditionOperatorsCondBsonTest.java#L56) `condArrayWithFourArgumentsShouldAlsoEncodeObjectForm`：四参数 condArray 保持对象形式的历史行为。

官网：[otherCondition](https://mongoplus.com/docs/otherCondition.html)

启用：将表达式传给 computed/project、group 或对应 Stage。

限制：

- cond/condArray 的重载编码不完全相同；应选择明确重载。
- Java 可赋值性不等于 BSON codec 或表达式角色合法。


### 编码边界

<a id="mp.stage-boundaries"></a>
#### 聚合阶段体与完整阶段的编码边界 (`mp.stage-boundaries`)

状态：`source_verified`；暴露：`public_api`；运行验证：`NOT_RUN`。

project(Bson) 包装阶段体为 $project；旧 sort(Bson) 透传参数，sortSpecification(Bson) 明确包装为 $sort。此差异影响自然语言到 API 的选择。

典型需求：构建混合升降序的聚合排序；自定义投影 BSON 时如何避免重复包装阶段。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#sortSpecification`；`mongo-plus-core:com.mongoplus.aggregate.LambdaAggregateWrapper#sort`；`mongo-plus-core:com.mongoplus.aggregate.LambdaAggregateWrapper#project`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java:781](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java#L781)：通过 custom 包装 $sort（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:321](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L321)：旧 BSON 重载透传（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:276](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L276)：包装 $project（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java:61](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java#L61) `legacySortBsonStillPassesTheExactObjectThrough`：旧 sort 仍透传同一对象。
- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java:27](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java#L27) `mixedDirectionsPreserveOrderAndInt32InBothReducers`：新规格入口产生完整阶段并保留字段顺序。
- [mongo-plus-core/src/test/java/com/mongoplus/aggregate/ProjectBsonStageBodyTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/ProjectBsonStageBodyTest.java#L18) `projectShouldEncodeCondArrayComputedField`：project 包装计算投影阶段体。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：新代码需要排序规格体时使用 sortSpecification；保留旧 sort 的调用契约。

限制：

- 不能依据参数同为 Bson 就假定相同包装规则。
- 同一投影混用 include/exclude 的服务端合法性不由 Core 编码成功保证。

文档差异：官网 aggregate 列出 sort(Bson)/project(Bson)，未明确解释当前 sortSpecification 和透传差异。


## 实体与字段映射

### 命名与标识

<a id="mp.field-mapping"></a>
#### 集合/字段命名与持久化选择 (`mp.field-mapping`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

CollectionName、CollectionField 及全局命名配置控制集合/数据库、字段别名、排除字段、null 写入和 ObjectId 转换。

典型需求：Java 驼峰属性写入数据库下划线字段；排除临时计算属性不存库；指定实体使用的集合和数据库。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionName`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java:16](../mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java#L16)：解析实体 namespace 和命名策略（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)：应用字段别名和转换（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:150](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L150)：结合全局和注解决定 null 写入（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[annotation](https://mongoplus.com/docs/annotation.html)；[property](https://mongoplus.com/docs/property.html)

启用：标注实体/字段注解；配置 mongo-plus.configuration.field 与 collection。

限制：

- 字符串路径不具备 Lambda 原字段元数据的全部保障。
- 字段排除和默认 null 忽略会影响实际写入内容。


<a id="mp.id-generation"></a>
#### 主键生成与回写 (`mp.id-generation`)

状态：`documented`；暴露：`annotation_extension`；运行验证：`NOT_RUN`。

@ID 配合 IdGenerateHandler 提供当前枚举中的主键策略；实体保存前生成 _id，执行后回写实体。

典型需求：保存前自动分配主键；使用自定义的主键生成算法。

API：`mongo-plus-annotation:com.mongoplus.annotation.ID`；`mongo-plus-core:com.mongoplus.handlers.IdGenerateHandler`；`mongo-plus-annotation:com.mongoplus.enums.IdTypeEnum`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)：生成并写入 _id（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java:39](../mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java#L39)：按真实 IdType 分派生成（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:363](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L363)：容器接收自定义主键处理器（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[annotation](https://mongoplus.com/docs/annotation.html)；[preface](https://mongoplus.com/docs/preface.html)

启用：@ID 指定类型；可注册 IdGenerateHandler，ASSIGN_ID 的生成器另按 IdentifierGenerator 接线。

限制：

- 不依据官网旧策略数量约束当前枚举。
- 自增策略涉及计数集合；不要与变动记录插件无验证地组合。


### 类型转换

<a id="mp.enum-mapping"></a>
#### 枚举值存储与还原 (`mp.enum-mapping`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

EnumValue 标识存储值；默认 EnumMappingStrategy/EnumConversionStrategy 参与枚举写入和读取，未标记时使用枚举名称。

典型需求：数据库存状态码而 Java 使用枚举。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.EnumValue`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java:17](../mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java#L17)：选择注解值或 name（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java:18](../mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java#L18)：枚举还原（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java:12](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java#L12)：默认安装枚举映射（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[autoMappingEnum](https://mongoplus.com/docs/autoMappingEnum.html)

启用：在枚举值字段标注 @EnumValue；当前 core 默认已装配枚举策略。

限制：

- 官网示例手动注册映射器；当前实现已有默认值，可自定义覆盖。


<a id="mp.type-conversion"></a>
#### 自定义读写类型转换 (`mp.type-conversion`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

MappingStrategy 控制 Java 值写为 Mongo 类型，ConversionStrategy 控制读取目标类型；TypeHandler 在单个字段上覆盖两向转换。

典型需求：把特殊金额类型转换成可存储值；字段用 InputStream 但数据库存二进制；读取日期时转成 Java 时间类型。

API：`mongo-plus-core:com.mongoplus.strategy.mapping.MappingStrategy`；`mongo-plus-core:com.mongoplus.strategy.conversion.ConversionStrategy`；`mongo-plus-core:com.mongoplus.handlers.TypeHandler`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:123](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L123)：选择写入映射策略（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:236](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L236)：读取策略与泛型分派（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/field/TypeHandlerFieldHandler.java:15](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/TypeHandlerFieldHandler.java#L15)：字段写入 TypeHandler（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)：字段读取 TypeHandler（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:241](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L241)：收集映射器 Bean（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:156](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L156)：收集转换器 Bean（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[mapping](https://mongoplus.com/docs/mapping.html)；[converter](https://mongoplus.com/docs/converter.html)；[typeHandler](https://mongoplus.com/docs/typeHandler.html)

启用：全局策略注册为带泛型的 Bean；字段策略通过 @CollectionField(typeHandler=...) 指定。

限制：

- TypeHandler 返回 null 会回退框架转换。
- 同目标类型策略覆盖和泛型解析需要核对，不能随意给策略加代理。


<a id="mp.custom-codec"></a>
#### Driver BSON Codec 扩展 (`mp.custom-codec`)

状态：`source_verified`；暴露：`public_extension`；运行验证：`NOT_RUN`。

MongoPlusCodec 扩展 Driver Codec；容器创建工厂时先收集 codec，MongoUtil 将其与默认 registry 合并安装到新客户端。

典型需求：给自定义值类型注册 BSON 编解码器。

API：`mongo-plus-core:com.mongoplus.codecs.MongoPlusCodec`；`mongo-plus-core:com.mongoplus.cache.codec.MongoPlusCodecCache#addCodec`。

实现及接线：

- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:86](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L86)：创建客户端前收集 MongoPlusCodec Bean（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)：合并 CodecRegistries 并安装到 Driver（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：在客户端创建前注册 MongoPlusCodec Bean；core 可显式加入 cache 后创建客户端。

限制：

- 已创建客户端不会因后续注册自动重建 registry。
- Driver registry 与某些 BSON 构建测试使用的 registry 不同，不能承诺所有入口一致。


### 字段增强

<a id="mp.auto-fill"></a>
#### 创建/修改字段自动填充 (`mp.auto-fill`)

状态：`documented`；暴露：`annotation_extension`；运行验证：`NOT_RUN`。

实体插入/更新转换分别调用自动填充处理器，按 CollectionField.fill 的 INSERT、UPDATE、INSERT_UPDATE 策略调用 MetaObjectHandler。

典型需求：自动记录创建时间和修改时间；保存数据时写入当前用户编号。

API：`mongo-plus-core:com.mongoplus.handlers.MetaObjectHandler`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField#fill`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)：插入转换调用 fill（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L96)：更新转换调用 fill（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java:50](../mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java#L50)：按标注收集字段并执行填充（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:184](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L184)：Bean 注册（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[autoFill](https://mongoplus.com/docs/autoFill.html)

启用：标注 fill 策略，并提供一个 MetaObjectHandler Bean 或 core 配置。

限制：

- wrapper-only/原始 BSON 更新不触发实体自动填充。
- 填充值由用户实现提供，框架不会凭字段名自动选择时间。


<a id="mp.extra-fields"></a>
#### 接收实体未声明的额外字段 (`mp.extra-fields`)

状态：`source_verified`；暴露：`annotation`；运行验证：`NOT_RUN`。

读取实体时 @ExtraFields 的 Map 收集未被实体字段消费的文档键，支持忽略列表、键名转换和 Map 类型选择。

典型需求：保留 MongoDB 中实体没有定义的字段；把未知字段收进扩展属性 Map。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.ExtraFields`；`mongo-plus-core:com.mongoplus.holder.ExtraFieldsHolder`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)：追踪已用键并填充额外 Map（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:224](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L224)：创建受约束的 Map 实例（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：在实体的 Map 字段标注 @ExtraFields；由默认 converter 读取路径直接处理。

限制：

- 只确认读取聚合未知字段；不承诺 Map 展平写回。
- 非 Map 字段会抛异常；自定义 mapType 需可实例化的具体类。

待确认：缺少专用读写往返测试；人工确认与字段排除、_id、键名冲突的组合。


<a id="mp.dictionary-bind"></a>
#### 字典字段回写候选 (`mp.dictionary-bind`)

状态：`candidate`；暴露：`annotation_extension`；运行验证：`NOT_RUN`。

FieldBindHandler 能按 @FieldBind 将源值交给用户 DataBindHandler 写入目标属性；尚未确认无需手动接线的完整使用契约。

典型需求：查询后把状态码自动补成状态名称；根据字典给结果补充显示字段。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.FieldBind`；`mongo-plus-core:com.mongoplus.handlers.DataBindHandler`；`mongo-plus-core:com.mongoplus.handlers.read.FieldBindHandler`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java:34](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java#L34)：查找 target 并调用用户处理器（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)：按字段顺序执行读处理器（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)：可接收显式 ReadHandler Bean（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：未找到内置 FieldBindHandler 自动注册；候选接线为显式 new FieldBindHandler(dataBindHandler) Bean/读链注册，需验证。

限制：

- 不能仅注册 DataBindHandler 就假定自动生效。
- target 转下划线与属性查找、字段遍历后续覆盖之间的行为需要测试。

待确认：添加最小外部示例，验证目标字段命名、字段顺序和 Map/DTO 行为。

人工审核：`REQUIRED`；字典回写接线：确认 FieldBindHandler 显式注册、target 命名与字段顺序，验证 Map/DTO 回写；当前仅局部静态证据。


### 关联映射

<a id="mp.dbref"></a>
#### 单值 DBRef 引用映射 (`mp.dbref`)

状态：`source_verified`；暴露：`annotation`；运行验证：`NOT_RUN`。

DBRefHandler 把被引用对象的 @ID 写成 Driver DBRef，读取时按引用数据库/集合与 id 查询并转换。

典型需求：实体字段引用另一个集合的对象。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.DBRef`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:64](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L64)：对象主键转 DBRef（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:85](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L85)：额外查询还原单值引用（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java:18](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L18)：默认写链安装（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:20](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)：默认读链安装（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：在引用字段标注 @DBRef；被引用类型应含 @ID 及可解析 namespace。

限制：

- 仅静态确认单值路径；不承诺集合引用、懒加载、级联保存或删除。
- 读取会增加查询，跨源/事务组合未验证。


## 安全隔离与一致性

### 租户与删除

<a id="mp.tenant"></a>
#### 租户条件隔离与显式忽略 (`mp.tenant`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

TenantHandler 提供租户值/字段，TenantInterceptor 对覆盖的读写条件和顶层聚合追加租户信息；支持忽略策略和 TenantManager。

典型需求：根据当前租户隔离查询数据；管理任务临时忽略租户限制。

API：`mongo-plus-core:com.mongoplus.handlers.TenantHandler`；`mongo-plus-core:com.mongoplus.manager.TenantManager#withoutTenant`；`mongo-plus-annotation:com.mongoplus.annotation.tenant.IgnoreTenant`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java:93](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L93)：查询条件追加租户（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java:99](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L99)：只增强顶层 match（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:281](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L281)：Bean 触发注册插件（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[tenant](https://mongoplus.com/docs/tenant.html)

启用：注册 TenantHandler Bean；管理范围可使用 @IgnoreTenant/withoutTenant。

限制：

- 不是数据库权限机制；直接 Driver 与原始路径应另核验。
- lookup 子管道不自动递归注入租户；嵌套忽略及线程传播需核验。


<a id="mp.logic-delete"></a>
#### 逻辑删除与未删除过滤 (`mp.logic-delete`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

开启逻辑配置并登记实体后，相关拦截器给查询加未删除条件，将覆盖的删除入口改写为更新逻辑字段。

典型需求：删除记录但保留数据供恢复；正常查询排除已删除数据。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionLogic`；`mongo-plus-core:com.mongoplus.manager.LogicManager`；`mongo-plus-annotation:com.mongoplus.annotation.logice.IgnoreLogic`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/CollectionLogiceInterceptor.java:35](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/CollectionLogiceInterceptor.java#L35)：查询与相关执行路径增强（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/LogicRemoveInterceptor.java:17](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/LogicRemoveInterceptor.java#L17)：删除改写为更新（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:367](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L367)：按配置安装逻辑删除组件（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[logic](https://mongoplus.com/docs/logic.html)；[annotation](https://mongoplus.com/docs/annotation.html)

启用：配置 mongo-plus.configuration.logic；明确逻辑字段、删除值和实体登记；按需启用 auto-fill。

限制：

- 依赖集合到实体的元数据；无实体模式不能自动推定可用。
- IgnoreLogic 对不同执行路径的组合未在本轮运行验证；不能承诺任意入口都能绕过。


### 并发与事务

<a id="mp.optimistic-lock"></a>
#### 版本乐观锁与受控重试 (`mp.optimistic-lock`)

状态：`documented`；暴露：`annotation_extension`；运行验证：`NOT_RUN`。

OptimisticLockerInterceptor 在识别实体版本字段的更新中，把旧版本加入 filter 并生成增量；可配置失败异常和 Retry。

典型需求：防止并发更新覆盖其他人的修改；版本冲突时进行有限次数重试。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.Version`；`mongo-plus-core:com.mongoplus.interceptor.business.OptimisticLockerInterceptor`；`mongo-plus-core:com.mongoplus.model.Retry`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java:403](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L403)：取 Integer 版本、附加过滤并自增（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java:163](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L163)：异步分支仍调用 Future.get（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:228](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L228)：收集插件 Bean（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[optimisticLocker](https://mongoplus.com/docs/optimisticLocker.html)

启用：版本字段标注 @Version，注册 OptimisticLockerInterceptor；可配置 Retry。

限制：

- 当前处理路径用 Integer/getInteger，不承诺任意 Number 类型。
- 首次更新需在 $set 中携带旧版本；缺失时默认跳过版本增强，可配置异常。
- 必须确认 BSON 结构被真正回写；不要复用更新 Wrapper。
- 官网说异步重试立即返回，当前源码 supplyAsync(...).get() 会等待结果。

人工审核：`REQUIRED`；乐观锁运行边界：Integer 版本、缺少旧版本时的行为、BSON 回写和重试结果；本轮未运行并发或数据库验证。


<a id="mp.transaction"></a>
#### MongoDB 会话事务 (`mp.transaction`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

@MongoTransactional 和 MongoTransactionalManager 建立线程内会话，ExecutorFactory 按上下文选择 SessionExecute；Spring 事务管理器为条件装配。

典型需求：多个数据库操作要一起提交或回滚；使用 Spring 事务注解控制 Mongo 操作。

API：`mongo-plus-annotation:com.mongoplus.annotation.transactional.MongoTransactional`；`mongo-plus-core:com.mongoplus.manager.MongoTransactionalManager`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java:21](../mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java#L21)：事务执行、异常处理与关闭（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:41](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L41)：会话存在时选择 SessionExecute（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:14](../mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)：条件注册 Spring 事务管理器（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[transactional](https://mongoplus.com/docs/transactional.html)；[property](https://mongoplus.com/docs/property.html)

启用：使用受代理的 @MongoTransactional 方法或编程式 manager；Spring @Transactional 要开启 mongo-plus.spring.transaction。

限制：

- 依赖支持事务的 MongoDB 部署和版本；直接 Driver 调用须显式携带会话。
- 上下文基于当前线程，不承诺异步传播或跨 MongoClient 原子性。
- 官网编程式示例使用较旧 MongoTransactionalOperate；该类 JavaDoc 指向 manager。


<a id="mp.cross-source-atomic"></a>
#### 跨独立数据源原子事务候选 (`mp.cross-source-atomic`)

状态：`candidate`；暴露：`optional_module`；运行验证：`NOT_RUN`。

分片模块有多会话提交/回滚协调代码，但证据不能建立跨独立数据源的原子提交保证。

典型需求：两个独立 Mongo 数据源要么同时成功要么同时失败。

API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。

实现及接线：

- [mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)：调用全部会话提交/关闭（实现证据，不自动作为推荐入口）。
- [mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)：逐个提交会话（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：分片 Starter 更换 TransactionHandler 后可进入协调代码；不是已确认的原子事务启用方式。

限制：

- 逐个会话 commit 不能作为原子保证。
- 不得将此候选直接推荐为分布式事务解决方案。

待确认：需要设计者确认支持契约，并用部分提交失败、网络中断及补偿场景验证。

人工审核：`REQUIRED`；跨数据源原子性：逐会话提交不构成原子提交保证；须确认契约并验证部分提交失败、网络中断及补偿。

不能充分满足的意图：跨数据源原子提交已得到保证


### 字段保护

<a id="mp.field-encryption"></a>
#### 实体字段加密、解密与查询值转换 (`mp.field-encryption`)

状态：`source_verified`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

@FieldEncrypt 在实体写入链加密，读链按 findDecrypt 解密；有原字段元数据的查询条件路径会转换查询值。

典型需求：对敏感字段加密存储并在读取时解密；查询加密字段时自动处理查询值。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.FieldEncrypt`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L26)：写入字段加密（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java:20](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java#L20)：按配置解密（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/EncryptorConditionHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/EncryptorConditionHandler.java#L26)：Lambda 原字段条件的查询值加密（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:20](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)：默认条件/读取处理器注册（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[property](https://mongoplus.com/docs/property.html)

启用：标注 @FieldEncrypt 并提供算法所需密钥；默认链接线，检查注解和全局 encryptor 配置。

限制：

- 原始 BSON、字符串字段和 wrapper-only 更新不能假定有相同加密处理。
- 不承诺随机加密算法支持等值检索或范围检索。

文档差异：官网导航出现字段加密主题，但本次该链接落到首页，未取得字段加密专页正文；property 只确认全局密钥配置。


<a id="mp.desensitization"></a>
#### 读取字段脱敏 (`mp.desensitization`)

状态：`documented`；暴露：`annotation`；运行验证：`NOT_RUN`。

@Desensitization 使读取处理链应用内置规则或用户 DesensitizationHandler，处理展示值。

典型需求：手机号和邮箱展示时隐藏中间部分。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.Desensitization`；`mongo-plus-core:com.mongoplus.handlers.DesensitizationHandler`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/read/DesensitizationHandlerApply.java:23](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/DesensitizationHandlerApply.java#L23)：规则或自定义处理器处理读取值（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:20](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)：默认读取链安装（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[desensitization](https://mongoplus.com/docs/desensitization.html)

启用：在接收实体/DTO 字段标注注解，按需提供自定义处理器类。

限制：

- 属于读取结果转换，不能当作数据库加密或日志脱敏保证。


<a id="mp.sensitive-word"></a>
#### 敏感词校验、词库维护与显式替换 (`mp.sensitive-word`)

状态：`documented`；暴露：`optional_module`；运行验证：`NOT_RUN`。

可选模块按局部字段或全局拦截路径检查文本；Manager 支持黑白名单增删及显式 replace 调用。

典型需求：保存文本前拒绝敏感词；动态维护黑白名单词库；显式调用工具替换敏感词。

API：`mongo-plus-sensitive-word:com.mongoplus.annotation.SensitiveWord`；`mongo-plus-sensitive-word:com.mongoplus.manager.SensitiveWordManager`。

实现及接线：

- [mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:121](../mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java#L121)：选择局部字段链或全局拦截器（实现证据，不自动作为推荐入口）。
- [mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:190](../mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java#L190)：发现敏感词抛异常（实现证据，不自动作为推荐入口）。
- [mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:209](../mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java#L209)：提供显式替换方法（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java:14](../mongo-plus-boot-starter/src/main/java/com/mongoplus/word/SensitiveWordConfiguration.java#L14)：classpath 条件和 Bean 注册（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[sensitiveWord](https://mongoplus.com/docs/sensitiveWord.html)

启用：加入敏感词模块，配置 mongo-plus.sensitive-word；LOCAL 使用字段注解，词库可用 LoadExtraWord。

限制：

- 官网说暂不支持替换；当前仅明确存在手动 Manager.replace，不能推成保存时自动替换。
- GLOBAL 会增加处理量；无本轮数据库/性能测试。


### 操作保护

<a id="mp.nonnull-validation"></a>
#### NonNull 自动参数校验候选 (`mp.nonnull-validation`)

状态：`candidate`；暴露：`annotation`；运行验证：`NOT_RUN`。

@NonNull 是 JSR-305 类型限定别名；当前未找到把其自动接入数据库操作前运行时校验的消费者。

典型需求：只标注 NonNull 就自动拒绝空参数吗。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。

实现及接线：

- [mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)：类型限定注解声明（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)：当前转换路径无此注解校验（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：没有确认的自动启用入口；统一执行前校验应考虑公开执行拦截器。

限制：

- 不得将静态类型标记推荐成运行时 Bean Validation。

待确认：维护者确认是否仅供静态分析，以及是否计划提供运行时校验。

人工审核：`REQUIRED`；NonNull 运行时校验：当前只有类型限定证据，未确认自动校验消费者，不可作为运行时校验方案。


<a id="mp.block-attack"></a>
#### 空条件更新/删除防护入口 (`mp.block-attack`)

状态：`documented`；暴露：`configuration`；运行验证：`NOT_RUN`。

BlockAttackInnerListener 检查 update/delete 命令第一个操作的 q，空条件时抛出异常；配置可以安装此 Listener。

典型需求：防止忘记条件更新或删除整个集合。

API：`mongo-plus-core:com.mongoplus.listener.business.BlockAttackInnerListener`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/listener/business/BlockAttackInnerListener.java:21](../mongo-plus-core/src/main/java/com/mongoplus/listener/business/BlockAttackInnerListener.java#L21)：检查命令数组第一个 q（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:196](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L196)：读取 blockAttackInner 并添加监听器（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:20](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L20)：Driver 事件转发和异常处理（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[property](https://mongoplus.com/docs/property.html)；[preface](https://mongoplus.com/docs/preface.html)

启用：配置 mongo-plus.configuration.collection.block-attack-inner=true。

限制：

- 只检查第一个操作；不能宣称覆盖所有 bulk 子操作。
- 源码中的事件回调抛异常不足以证明 Driver 必然阻止网络执行；需要运行验证，不作为已验证的绝对安全屏障。

待确认：在实际 Driver 上验证监听器抛异常是否阻止命令，以及批量后续空 filter 行为。

人工审核：`REQUIRED`；防误删运行边界：Driver 是否阻止发送、批量后续空条件操作均待验证；不可作为已验证的绝对安全屏障。


## 扩展与事件

### 执行扩展

<a id="mp.execution-extension"></a>
#### 执行前校验、参数改写与执行控制 (`mp.execution-extension`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

普通 Interceptor 在受支持策略的执行前改写参数，执行成功返回后回调；AdvancedInterceptor 可包围调用、调用 proceed 或返回自定义结果。

典型需求：在执行数据库操作之前统一校验参数；为所有查询追加业务条件；自定义拦截器控制是否继续执行。

API：`mongo-plus-core:com.mongoplus.interceptor.Interceptor`；`mongo-plus-core:com.mongoplus.interceptor.AdvancedInterceptor`；`mongo-plus-core:com.mongoplus.interceptor.Invocation#proceed`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java:45](../mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java#L45)：按执行策略调用 before/参数替换和成功后的 after（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java:29](../mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java#L29)：激活时交给高级插件（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:26](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L26)：内高级外普通的包装路径（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:216](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L216)：收集普通 Bean（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:228](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L228)：收集高级 Bean（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[interceptor](https://mongoplus.com/docs/interceptor.html)

启用：注册相应接口 Bean 或通过 Configuration/Chain 显式注册，设置 order。

限制：

- 普通 before 取决于策略映射，异常路径不会进入普通 after。
- 直接 Driver 操作不自动经过 Execute 代理。
- 不要用命令成功通知替代此参数校验扩展。


### 命令事件

<a id="mp.command-events"></a>
#### 数据库命令事件、回调与日志 (`mp.command-events`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

MongoUtil 创建客户端时安装 BaseListener，转为 MongoPlus 事件并分发给按 getOrder 排序的 Listener。用户可以在成功回调中接入通知逻辑，也可配置内置 LogListener 记录命令与部分结果计数。

典型需求：每次数据库命令执行成功后发送通知；寻找可靠通知相关回调入口，但投递保证需另行设计；统计数据库命令耗时并记录失败；开启数据库命令日志及可执行语句格式化。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)：客户端安装 BaseListener（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)：从 Driver 命令成功事件构造框架事件（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/listener/MongoPlusListener.java:19](../mongo-plus-core/src/main/java/com/mongoplus/listener/MongoPlusListener.java#L19)：向注册监听器分发（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/listener/business/LogListener.java:19](../mongo-plus-core/src/main/java/com/mongoplus/listener/business/LogListener.java#L19)：打印命令、指定结果计数与失败日志（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:196](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L196)：Bean 注册并排序（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[listener](https://mongoplus.com/docs/listener.html)；[property](https://mongoplus.com/docs/property.html)

启用：实现 Listener 并注册 Bean；通知发送由项目自行实现；core 可用 Configuration.listener；内置命令日志通过 mongo-plus.log/pretty 等配置。

限制：

- 命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。
- 已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

不能充分满足的意图：事务提交后恰好一次可靠通知；数据库变更 CDC 订阅

检索约束：`{"unsupportedIntentGroups": [[["通知", "消息投递", "消息发送"], ["可靠", "恰好一次", "exactly once", "事务提交后", "提交后"]], [["CDC", "变更流"], ["订阅", "监听"]]]}`


### 字段扩展

<a id="mp.field-chain"></a>
#### 有序字段写入处理链 (`mp.field-chain`)

状态：`source_verified`；暴露：`public_extension`；运行验证：`NOT_RUN`。

FieldHandlerChain 在注册时按 order 排序；转换过程把前序非 null 结果作为 currentValue 传递给后序 FieldHandler。

典型需求：在加密前先标准化字段内容；多个字段处理器按顺序接收前一个处理结果。

API：`mongo-plus-core:com.mongoplus.handlers.FieldHandler`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#register`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#registerAll`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java:106](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L106)：注册后排序（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)：遍历已排序链并传递最新值（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java:36](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java#L36)：新双参接口向旧方法兼容委托（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：通过 FieldHandlerChain.getInstance().register/registerAll 显式注册；没有确认通用 FieldHandler Bean 自动收集。

限制：

- 旧单参数 handler 标记 Deprecated；双参数默认委托旧实现。
- null 返回值表示保留当前值；全局可变链应在初始化期注册。
- HandlerCache.fieldHandlers 是 Deprecated 兼容别名。


<a id="mp.read-chain"></a>
#### 读取字段加工与中止赋值 (`mp.read-chain`)

状态：`source_verified`；暴露：`public_extension`；运行验证：`NOT_RUN`。

ReadHandler 在字段赋值前按顺序读取转换值，activate 控制参与，discontinue 可以终止该字段的读取赋值路径。

典型需求：给查询结果字段做统一加工；特殊值出现时跳过属性赋值。

API：`mongo-plus-core:com.mongoplus.handlers.ReadHandler`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)：激活/中止逻辑及字段赋值（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:105](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L105)：按 order 排序（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)：Bean 自动收集（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：注册 ReadHandler Bean 或 HandlerCache.addReadHandler；保留框架默认解密/脱敏/DBRef 处理器。

限制：

- discontinue 终止的是当前字段 lambda，不能解释为中止整个查询。
- TypeHandler 和通用转换与 ReadHandler 的组合应按实际顺序核验。


### 生命周期

<a id="mp.namespace-aware"></a>
#### 命名空间生命周期感知内部接线 (`mp.namespace-aware`)

状态：`internal_only`；暴露：`internal_implementation`；运行验证：`NOT_RUN`。

Aware/NamespaceAware 注册到缓存；DefaultBaseIndexImpl 解析 namespace 时分发感知回调，逻辑删除实现使用该机制登记元数据。

典型需求：理解集合首次登记时如何初始化插件元数据。

API：`mongo-plus-core:com.mongoplus.aware.Aware`；`mongo-plus-core:com.mongoplus.aware.impl.NamespaceAware`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/cache/global/AwareHandlerCache.java:14](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/AwareHandlerCache.java#L14)：保存和查找感知处理器（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/logic/LogicNamespaceAware.java:11](../mongo-plus-core/src/main/java/com/mongoplus/logic/LogicNamespaceAware.java#L11)：内部逻辑删除接线（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/index/impl/DefaultBaseIndexImpl.java:95](../mongo-plus-core/src/main/java/com/mongoplus/index/impl/DefaultBaseIndexImpl.java#L95)：解析 namespace 时通知感知处理器（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：当前用于框架初始化；不推荐以内部静态缓存构建业务事件系统。

限制：

- public 可见不代表稳定的业务扩展契约；未找到专门使用测试。


## 连接路由与集成

### 连接配置

<a id="mp.encrypted-config"></a>
#### 连接配置密文解密 (`mp.encrypted-config`)

状态：`documented`；暴露：`configuration`；运行验证：`NOT_RUN`。

Boot EnvironmentPostProcessor 对受支持属性源中的 ${ENC:...} 形式配置使用 mpw.key 解密。

典型需求：配置文件不想明文保存数据库密码。

API：`mongo-plus-boot-starter:com.mongoplus.property.EncryptionPostProcessor`。

实现及接线：

- [mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:30](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L30)：处理 OriginTrackedMapPropertySource 的加密值（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:70](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L70)：检查完整前后缀（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/resources/META-INF/spring.factories:12](../mongo-plus-boot-starter/src/main/resources/META-INF/spring.factories#L12)：注册资源。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[security](https://mongoplus.com/docs/security.html)

启用：Boot spring.factories 注册后提供 mpw.key 和包裹的属性值；见注册资源引用。

限制：

- 只处理实现筛选的属性源，不能承诺所有环境变量/外部配置源均解密。
- 这是启动配置解密，与实体字段加密分开。


<a id="mp.connection-config"></a>
#### 连接池、URI 与 TLS 配置 (`mp.connection-config`)

状态：`documented`；暴露：`configuration`；运行验证：`NOT_RUN`。

BaseProperty/UrlJoint 将连接配置组织成 Driver ConnectionString；MongoUtil 可安装自定义 SSLContext 并创建客户端。

典型需求：配置连接超时和连接池大小；使用 URI 或客户端证书连接 MongoDB。

API：`mongo-plus-core:com.mongoplus.model.BaseProperty`；`mongo-plus-core:com.mongoplus.toolkit.UrlJoint`；`mongo-plus-boot-starter:com.mongoplus.property.MongoDBConnectProperty`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java:27](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java#L27)：组织 URI 参数（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)：连接字符串、SSL 和客户端构建（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:86](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L86)：连接配置创建客户端工厂（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[property](https://mongoplus.com/docs/property.html)

启用：配置 mongo-plus.data.mongodb；URI 及结构字段优先级按 UrlJoint；TLS 文件与密钥按 BaseProperty。

限制：

- 官网 Driver 参数默认值可能对应旧版本，本索引不将其当作当前运行保证。
- 已有 MongoClient Bean 与容器条件装配交互需实测。


### 数据源路由

<a id="mp.datasource-routing"></a>
#### 声明式多数据源选择 (`mp.datasource-routing`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

@MongoDs 与容器切面在方法执行期间设置数据源名，支持方法/类选择及 DataSourceHandler。

典型需求：根据租户或业务动态选择数据源；一个服务方法切换到另一个数据库连接。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现及接线：

- [mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)：选择源并在 finally 清上下文（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)：按数据源取得集合管理器（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:86](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L86)：创建从数据源客户端（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[dataSource](https://mongoplus.com/docs/dataSource.html)；[annotation](https://mongoplus.com/docs/annotation.html)

启用：配置命名 slave-data-source；在受代理的方法/类上标注 @MongoDs；动态值可交给 handler。

限制：

- ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。
- Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。


<a id="mp.runtime-datasource"></a>
#### 运行时追加命名数据源 (`mp.runtime-datasource`)

状态：`source_verified`；暴露：`public_api`；运行验证：`NOT_RUN`。

DataSourceManager.appendTempDataSource 创建客户端并按数据库名登记 collection managers，isOverride 决定是否覆盖已存在名称。

典型需求：系统运行时给新客户追加一个数据库连接。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)：创建客户端并更新映射（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)：容器提供管理器 Bean（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：注入 DataSourceManager 并显式调用 appendTempDataSource(dsName, baseProperty, isOverride)。

限制：

- 同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。
- 覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。


<a id="mp.sharding"></a>
#### 可选数据源分片路由 (`mp.sharding`)

状态：`source_verified`；暴露：`optional_module`；运行验证：`NOT_RUN`。

分片模块按执行种类选择目标数据源，支持用户策略与负载选择，并替换执行 collection；不是跨所有分片结果合并引擎。

典型需求：根据业务分片键选择目标数据源；给读写指定不同的数据源策略。

API：`mongo-plus-sharding:com.mongoplus.sharding.AbstractDataSourceShardingHandler`；`mongo-plus-sharding:com.mongoplus.sharding.DataSourceShardingStrategy`；`mongo-plus-sharding:com.mongoplus.interceptor.DataSourceShardingInterceptor`。

实现及接线：

- [mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77](../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L77)：策略和负载选择后替换集合（实现证据，不自动作为推荐入口）。
- [mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11](../mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)：创建插件并更换 TransactionHandler（实现证据，不自动作为推荐入口）。
- [mongo-plus-sharding-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports:1](../mongo-plus-sharding-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports#L1)：注册资源。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：加入 sharding 模块及相应 Starter 或显式注册插件；自定义 handler/strategy 按构造参数传入。

限制：

- 实现要求分片策略为最后拦截器；顺序与并发组合需验证。
- 一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。

不能充分满足的意图：所有分片结果自动合并；跨分片自动合并；全局分页


### 集合路由

<a id="mp.dynamic-collection"></a>
#### 运行时集合名选择 (`mp.dynamic-collection`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

CollectionNameHandler 根据执行种类、参数和 namespace 返回集合名，DynamicCollectionNameInterceptor 替换执行参数中的 MongoCollection。

典型需求：按月份把订单读写路由到不同集合；根据业务条件动态选择集合。

API：`mongo-plus-core:com.mongoplus.handlers.CollectionNameHandler`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java:32](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L32)：替换末尾 collection 参数（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:297](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L297)：Bean 触发插件注册（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[dynamicCollection](https://mongoplus.com/docs/dynamicCollection.html)

启用：注册 CollectionNameHandler Bean 或配置 DynamicCollectionNameInterceptor。

限制：

- 不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。
- handler 必须在不匹配时返回原集合名。


### 容器接入

<a id="mp.container-integration"></a>
#### Boot 与 Solon 容器接入 (`mp.container-integration`)

状态：`documented`；暴露：`container_integration`；运行验证：`NOT_RUN`。

独立 Boot/Boot4 Starter 和 Solon 插件通过资源入口安装配置、Mapper 扫描/代理与扩展 Bean 收集。

典型需求：在 Spring Boot 项目中自动注入 Mapper；在 Solon 中启用 MongoPlus。

API：`mongo-plus-boot-starter:com.mongoplus.annotation.MongoMapperScan`；`mongo-plus-boot-starter:com.mongoplus.config.MongoPlusConfiguration`；`mongo-plus-solon-plugin:com.mongoplus.config.XPluginAuto`。

实现及接线：

- [mongo-plus-boot-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java:18](../mongo-plus-boot-starter/src/main/java/com/mongoplus/registrar/MongoMapperScannerRegistrar.java#L18)：Mapper 扫描注册入口（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:56](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L56)：条件创建核心组件（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:54](../mongo-plus-boot4-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L54)：Boot4 独立配置（实现证据，不自动作为推荐入口）。
- [mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/XPluginAuto.java:28](../mongo-plus-solon-plugin/src/main/java/com/mongoplus/config/XPluginAuto.java#L28)：Solon Bean 构造和插件生命周期（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports:1](../mongo-plus-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports#L1)：注册资源。
- [mongo-plus-boot4-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports:1](../mongo-plus-boot4-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports#L1)：注册资源。
- [mongo-plus-solon-plugin/src/main/resources/META-INF/solon/anwen.mongo.config.properties:1](../mongo-plus-solon-plugin/src/main/resources/META-INF/solon/anwen.mongo.config.properties#L1)：注册资源。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[mapper](https://mongoplus.com/docs/mapper.html)；[started](https://mongoplus.com/docs/started.html)；[home](https://mongoplus.com/)

启用：选择对应模块；Boot 扫描 @MongoMapperScan，Solon 按 @Mongo 标注；资源接线见 registrationReferences。

限制：

- 容器模块是可选适配，不应将各自同名 FQCN 混用；引用必须包含 module。
- 本轮未验证 JDK/Driver/容器版本兼容矩阵。


### 开发工具

<a id="mp.developer-index"></a>
#### 源码 API 证据生成工具内部入口 (`mp.developer-index`)

状态：`internal_only`；暴露：`developer_tool`；运行验证：`NOT_RUN`。

现有 mongo-plus-indexer 提供源码 API/聚合语义证据生成；属于开发工具模块，不是 MongoDB 业务执行能力。

典型需求：追溯 API 的参数语义和源码证据。

API：`mongo-plus-indexer:com.mongoplus.indexer.MongoPlusIndexer`。

实现及接线：

- [mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java:14](../mongo-plus-indexer/src/main/java/com/mongoplus/indexer/MongoPlusIndexer.java#L14)：工具公开门面（实现证据，不自动作为推荐入口）。
- [mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java:44](../mongo-plus-indexer/src/main/java/com/mongoplus/indexer/scanner/SourceScanner.java#L44)：JDK compiler tree 扫描与证据生成（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：已检查范围未找到明确功能介绍，见文档检查清单。

启用：参照 mongo-plus-indexer/README.md；本任务没有调用或修改任何已有 API 索引生成器。

限制：

- 此处只导航现有工具；不能推荐为业务查询 API。
- 本次 capability 产物只消费 generated 下 JavaParser 声明索引。


## 集合运维

### 索引管理

<a id="mp.indexes"></a>
#### 注解自动建索引与手工索引管理 (`mp.indexes`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

IndexUtil 将普通、复合、文本、地理、哈希等注解转换为 IndexModel；启动扫描按配置创建；BaseIndex/Repository 提供手工管理入口。

典型需求：启动时创建唯一索引和 TTL 过期索引；按条件创建复合或地理索引；查看和删除现有索引。

API：`mongo-plus-annotation:com.mongoplus.annotation.index.MongoIndex`；`mongo-plus-core:com.mongoplus.index.BaseIndex`；`mongo-plus-core:com.mongoplus.repository.IRepository`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/IndexUtil.java:29](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/IndexUtil.java#L29)：注解转换为索引模型（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java:86](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L86)：将模型提交给对应数据源 collection（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:337](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L337)：启动扫描的配置开关（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/index/impl/AbstractBaseIndex.java:34](../mongo-plus-core/src/main/java/com/mongoplus/index/impl/AbstractBaseIndex.java#L34)：手工索引提交给执行器（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[autoCreateIndex](https://mongoplus.com/docs/autoCreateIndex.html)

启用：开启 auto-create-index 并确保 @CollectionName 实体在扫描包内；手工管理用 BaseMapper/BaseIndex 或 Repository。

限制：

- 普通 MongoMapper 不自动声明实体索引便利方法。
- 自动索引属启动期，不能承诺动态集合自动同步索引；服务端选项合法性待部署验证。


### 时序集合

<a id="mp.timeseries"></a>
#### 注解自动创建时序集合 (`mp.timeseries`)

状态：`documented`；暴露：`annotation_configuration`；运行验证：`NOT_RUN`。

扫描 @TimeSeries 并构造 TimeSeriesOptions，使用指定时间字段、元字段、粒度和过期等选项创建集合。

典型需求：保存设备时序数据并自动创建时间集合。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.TimeSeries`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java:34](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L34)：查询已有时序集合并构造/创建（实现证据，不自动作为推荐入口）。
- [mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:324](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L324)：扫描配置开关（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[autoCreateTimeSeries](https://mongoplus.com/docs/autoCreateTimeSeries.html)

启用：开启 auto-create-time-series，标注 @TimeSeries 并确保扫描范围覆盖实体。

限制：

- 服务端版本/选项兼容未运行验证。
- bucketMaxSpan/bucketRounding 分支会清除 metaField；捕获 MongoCommandException，不能凭启动继续就判定已创建。


### 备份与镜像

<a id="mp.backup"></a>
#### 集合本地 JSON/ZIP 备份与恢复 (`mp.backup`)

状态：`documented`；暴露：`public_api`；运行验证：`NOT_RUN`。

BackupManager 分页读取 Document 写出 JSON 后压缩为 ZIP，并按文件元信息重新导入；不生成 Excel 工作簿；导入路径包含删除集合。

典型需求：把指定集合导出成本地 JSON/ZIP 备份文件；从备份压缩包恢复集合。

API：`mongo-plus-core:com.mongoplus.manager.BackupManager#export`；`mongo-plus-core:com.mongoplus.manager.BackupManager#imports`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:214](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L214)：分页写出并通知 BackupListener（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:161](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L161)：读取 ZIP、首条目 drop、执行插入（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[dataBackup](https://mongoplus.com/docs/dataBackup.html)

启用：创建 BackupManager，设置路径/集合/数据源/页大小后显式 export/imports；按需添加 BackupListener。

限制：

- 只支持此处描述的 JSON/ZIP 备份入口；Excel/xls/xlsx 工作簿导出无能力证据。
- 导入首个 ZIP 条目前会 drop 目标集合；本任务仅阅读，没有执行备份恢复。
- 分页导出不是已证明的一致快照；未确认索引、类型及多集合恢复完整性。

人工审核：`REQUIRED`；备份恢复运行边界：恢复首条目前 drop、BSON 类型保真、索引与多集合完整性、一致快照均需人工复核和运行验证。

不能充分满足的意图：Excel 工作簿导出；xlsx 导出；xls 导出

检索约束：`{"requiredAny": ["备份", "恢复", "ZIP", "JSON", "BackupManager"], "excludedAny": ["Excel", "xlsx", "xls", "工作簿"], "noMatchIntentGroups": [[["Excel", "xlsx", "xls", "工作簿"], ["导出", "写成", "生成", "报表", "工作簿"]]]}`


<a id="mp.async-mirror"></a>
#### 异步多数据源镜像写 (`mp.async-mirror`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

AsyncMultipleWriteInterceptor 按 MultipleWriteHandler 的目标源列表提交后台写任务，然后继续主调用。

典型需求：写主数据源时同时给其他源备份一份。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)：提交镜像分支后 proceed（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)：线程池 submit 到目标数据源（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)：编程式安装高级插件（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[dataBackup](https://mongoplus.com/docs/dataBackup.html)

启用：注册高级插件 Bean 或 Configuration.asyncMultipleWrite，并设置目标数据源及线程池/策略。

限制：

- 任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。
- 重试、线程池关闭与异常处理需要调用方核验。


### 变动记录

<a id="mp.change-record"></a>
#### 数据变动记录与批量阈值 (`mp.change-record`)

状态：`documented`；暴露：`public_extension`；运行验证：`NOT_RUN`。

DataChangeRecorderInnerInterceptor 从调用参数构造变动记录，检查批量阈值，可在操作返回后保存到记录集合。

典型需求：记录数据修改历史；限制一次批量变动的条数。

API：`mongo-plus-core:com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor`。

实现及接线：

- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:101](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L101)：解析操作参数和阈值（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:126](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L126)：保存记录并恢复数据源上下文（实现证据，不自动作为推荐入口）。
- [mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:232](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L232)：更新参数记录数量判定（实现证据，不自动作为推荐入口）。

测试源码证据（本轮未执行）：

- 未找到专用测试；仅静态实现核验。

官网：[dataChangeRecorder](https://mongoplus.com/docs/dataChangeRecorder.html)

启用：注册普通拦截器；配置阈值/忽略集合，enableSaveDatabase(baseMapper) 启用记录落库。

限制：

- 记录源于操作参数，不是数据库 CDC 或完整前后镜像。
- 必须忽略记录集合以避免递归；不要与自增 ID 无验证地组合。
- 阈值按参数/models 计算，不等于 updateMany 实际匹配文档数量限制。

