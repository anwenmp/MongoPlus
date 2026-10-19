# 自然语言需求离线检索验证

原有集 retrieval-cases.json 不变；首批 18 条独立用例首次 16/18，修正 H03/H07 后转为 retrieval-validation-cases.json。最终 16 条 retrieval-holdout-cases.json 在第二轮规则调整前保存，最终规则冻结后首次执行。排序不读取 expected。词法基线使用中文二元词片/英文词 IDF、关键词短语加权、前置/排除条件、负意图导航加权和显式否定范围处理，阈值 5；分数只表示相关性。不支持的工作簿输出返回空列表；负意图命中时全部结果禁止推荐。

共 66 条；首项合理 66，前三含预期 66，最终通过 66。所有 Java 测试均为源码关联，未执行。

原有集：32/32；增量验证集：18/18；最终独立保留集：16/16。
证据安全检查覆盖所有不足证据结果；候选首项必须仍是 candidate。无匹配用例要求结果为空。

## Q01：筛选年龄十八到三十的用户，按注册时间倒序只取姓名

测试集：`original`；预期：mp.conditional-query；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：筛选、排序、字段投影是同一查询需求；正则和范围重载需按字段选择。

### 1. 条件查询、排序与投影 (`mp.conditional-query`，26.2344)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

### 2. 有序字段写入处理链 (`mp.field-chain`，5.5797)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.FieldHandler`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#register`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#registerAll`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java:106](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L106)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java:36](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java#L36)。

测试：无专用行为测试关联。

边界：旧单参数 handler 标记 Deprecated；双参数默认委托旧实现。；null 返回值表示保留当前值；全局可变链应在初始化期注册。；HandlerCache.fieldHandlers 是 Deprecated 兼容别名。

## Q02：把每个部门的销售额分组求和

测试集：`original`；预期：mp.pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：分组统计应导航 Aggregate 与 group；尚未证明数据库结果。

### 1. 聚合管道编排与统计 (`mp.pipeline`，15.8755)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

## Q03：订单关联客户集合，关联结果按时间排序只取一条

测试集：`original`；预期：mp.nested-pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：lookup 的独立子管道，需提醒关联子管道租户条件未自动递归。

### 1. 关联查询与多分支子管道 (`mp.nested-pipeline`，25.1997)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 2. 条件查询、排序与投影 (`mp.conditional-query`，12.1412)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

### 3. 分页、计数与分页结果 (`mp.pagination`，6.4767)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper#page`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:386](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L386)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:393](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L393)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L441)。

测试：无专用行为测试关联。

边界：属于偏移分页；不能据此承诺游标分页或高并发一致快照。；稳定翻页需要调用方提供稳定排序。

## Q04：一条聚合同时返回明细列表和排行榜两个分支

测试集：`original`；预期：mp.nested-pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：facet 多分支，与普通分页有词语交集。

### 1. 关联查询与多分支子管道 (`mp.nested-pipeline`，28.0572)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 2. 聚合表达式与计算投影 (`mp.expressions`，8.125)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.pipeline.AggregateOperator#literal`；`mongo-plus-core:com.mongoplus.conditions.operation.ConditionOperators#cond`；`mongo-plus-core:com.mongoplus.aggregate.pipeline.Projections#computed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java:46](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java:41](../mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java:83](../mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java#L83)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java:106](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java#L106) `literalKeepsTypesValuesAndAllOrders`；[mongo-plus-core/src/test/java/com/mongoplus/conditions/operation/ConditionOperatorsCondBsonTest.java:56](../mongo-plus-core/src/test/java/com/mongoplus/conditions/operation/ConditionOperatorsCondBsonTest.java#L56) `condArrayWithFourArgumentsShouldAlsoEncodeObjectForm`。

边界：cond/condArray 的重载编码不完全相同；应选择明确重载。；Java 可赋值性不等于 BSON codec 或表达式角色合法。

### 3. 聚合管道编排与统计 (`mp.pipeline`，6.3458)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

## Q05：计算单价乘购买数量，再按条件返回会员等级

测试集：`original`；预期：mp.expressions；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：表达式和 computed 投影；condArray 历史重载不能靠名称猜。

### 1. 聚合表达式与计算投影 (`mp.expressions`，26.7997)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.pipeline.AggregateOperator#literal`；`mongo-plus-core:com.mongoplus.conditions.operation.ConditionOperators#cond`；`mongo-plus-core:com.mongoplus.aggregate.pipeline.Projections#computed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java:46](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/pipeline/AggregateOperator.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java:41](../mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java:83](../mongo-plus-core/src/main/java/com/mongoplus/conditions/operation/ConditionOperators.java#L83)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java:106](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LiteralEncodingTest.java#L106) `literalKeepsTypesValuesAndAllOrders`；[mongo-plus-core/src/test/java/com/mongoplus/conditions/operation/ConditionOperatorsCondBsonTest.java:56](../mongo-plus-core/src/test/java/com/mongoplus/conditions/operation/ConditionOperatorsCondBsonTest.java#L56) `condArrayWithFourArgumentsShouldAlsoEncodeObjectForm`。

边界：cond/condArray 的重载编码不完全相同；应选择明确重载。；Java 可赋值性不等于 BSON codec 或表达式角色合法。

### 2. 条件查询、排序与投影 (`mp.conditional-query`，5.1256)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

## Q06：扣减库存并往标签数组追加一个值

测试集：`original`；预期：mp.update-bulk；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：inc/push；不承诺该组合自动走实体填充。

### 1. 原子字段更新与批量写入 (`mp.update-bulk`，13.7096)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:155](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L155)。

测试：无专用行为测试关联。

边界：wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。；批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。

### 2. 运行时追加命名数据源 (`mp.runtime-datasource`，9.277)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。

测试：无专用行为测试关联。

边界：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。

### 3. 条件查询、排序与投影 (`mp.conditional-query`，5.5649)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

## Q07：列表每页二十条，要返回总数和当前页

测试集：`original`；预期：mp.pagination；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：PageResult 与 page，属于偏移分页。

### 1. 分页、计数与分页结果 (`mp.pagination`，26.4564)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper#page`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:386](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L386)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:393](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L393)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L441)。

测试：无专用行为测试关联。

边界：属于偏移分页；不能据此承诺游标分页或高并发一致快照。；稳定翻页需要调用方提供稳定排序。

### 2. 运行时集合名选择 (`mp.dynamic-collection`，5.1648)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.CollectionNameHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java:32](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L32)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:297](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L297)。

测试：无专用行为测试关联。

边界：不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。；handler 必须在不匹配时返回原集合名。

## Q08：文档结构经常变，没有 Java 实体也要查指定集合

测试集：`original`；预期：mp.raw-bson；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：BaseMapper 显式 namespace；实体插件能力另核验。

### 1. 无实体 Document 与原始 BSON 访问 (`mp.raw-bson`，10.135)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.manager.MongoPlusClient#getCollection`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:281](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L281)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:287](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L287)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:299](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L299)。

测试：无专用行为测试关联。

边界：直接 Driver 操作与 Mapper 插件/转换链不同。；实体元数据依赖的逻辑删除、版本、填充不能无条件套用于无实体路径。

### 2. 集合/字段命名与持久化选择 (`mp.field-mapping`，9.481)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionName`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java:16](../mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java#L16)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:150](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L150)。

测试：无专用行为测试关联。

边界：字符串路径不具备 Lambda 原字段元数据的全部保障。；字段排除和默认 null 忽略会影响实际写入内容。

### 3. 接收实体未声明的额外字段 (`mp.extra-fields`，8.8933)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.ExtraFields`；`mongo-plus-core:com.mongoplus.holder.ExtraFieldsHolder`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:224](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L224)。

测试：无专用行为测试关联。

边界：只确认读取聚合未知字段；不承诺 Map 展平写回。；非 Map 字段会抛异常；自定义 mapType 需可实例化的具体类。

## Q09：新增时写创建人和创建时间，修改时更新修改时间

测试集：`original`；预期：mp.auto-fill；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：MetaObjectHandler；仅实体转换路径可确认。

### 1. 创建/修改字段自动填充 (`mp.auto-fill`，40.7189)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.MetaObjectHandler`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField#fill`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L96)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java:50](../mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java#L50)。

测试：无专用行为测试关联。

边界：wrapper-only/原始 BSON 更新不触发实体自动填充。；填充值由用户实现提供，框架不会凭字段名自动选择时间。

### 2. 注解自动创建时序集合 (`mp.timeseries`，10.14)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.TimeSeries`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java:34](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L34)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:324](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L324)。

测试：无专用行为测试关联。

边界：服务端版本/选项兼容未运行验证。；bucketMaxSpan/bucketRounding 分支会清除 metaField；捕获 MongoCommandException，不能凭启动继续就判定已创建。

### 3. 原子字段更新与批量写入 (`mp.update-bulk`，8.9223)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:155](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L155)。

测试：无专用行为测试关联。

边界：wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。；批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。

## Q10：两个人同时编辑数据时，旧版本不要覆盖新版本

测试集：`original`；预期：mp.optimistic-lock；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：版本条件与自增；不得忽略 Integer/回写边界。

### 1. 版本乐观锁与受控重试 (`mp.optimistic-lock`，10.135)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.Version`；`mongo-plus-core:com.mongoplus.interceptor.business.OptimisticLockerInterceptor`；`mongo-plus-core:com.mongoplus.model.Retry`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java:403](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L403)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java:163](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L163)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:228](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L228)。

测试：无专用行为测试关联。

边界：当前处理路径用 Integer/getInteger，不承诺任意 Number 类型。；首次更新需在 $set 中携带旧版本；缺失时默认跳过版本增强，可配置异常。；必须确认 BSON 结构被真正回写；不要复用更新 Wrapper。；官网说异步重试立即返回，当前源码 supplyAsync(...).get() 会等待结果。

### 2. 跨独立数据源原子事务候选 (`mp.cross-source-atomic`，8.2824)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)；[mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)。

测试：无专用行为测试关联。

边界：逐个会话 commit 不能作为原子保证。；不得将此候选直接推荐为分布式事务解决方案。

### 3. 异步多数据源镜像写 (`mp.async-mirror`，5.0635)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)；[mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)。

测试：无专用行为测试关联。

边界：任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。；重试、线程池关闭与异常处理需要调用方核验。

## Q11：每个公司的账号只能查询自己租户的数据

测试集：`original`；预期：mp.tenant；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：TenantHandler 和条件插件；不是数据库权限。

### 1. 租户条件隔离与显式忽略 (`mp.tenant`，9.8198)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.TenantHandler`；`mongo-plus-core:com.mongoplus.manager.TenantManager#withoutTenant`；`mongo-plus-annotation:com.mongoplus.annotation.tenant.IgnoreTenant`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java:93](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L93)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java:99](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L99)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:281](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L281)。

测试：无专用行为测试关联。

边界：不是数据库权限机制；直接 Driver 与原始路径应另核验。；lookup 子管道不自动递归注入租户；嵌套忽略及线程传播需核验。

### 2. 关联查询与多分支子管道 (`mp.nested-pipeline`，6.8198)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 3. 声明式多数据源选择 (`mp.datasource-routing`，5.3317)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:63](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L63)。

测试：无专用行为测试关联。

边界：ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。；Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。

## Q12：读取用户时手机号和邮箱中间打星号

测试集：`original`；预期：mp.desensitization；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：读取脱敏，与存储加密不同。

### 1. 读取字段脱敏 (`mp.desensitization`，24.3686)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.Desensitization`；`mongo-plus-core:com.mongoplus.handlers.DesensitizationHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/DesensitizationHandlerApply.java:23](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/DesensitizationHandlerApply.java#L23)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:20](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)。

测试：无专用行为测试关联。

边界：属于读取结果转换，不能当作数据库加密或日志脱敏保证。

## Q13：身份证字段要以密文存储，读取时自动解密

测试集：`original`；预期：mp.field-encryption；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：FieldEncrypt；官网专页链接失效，仅以源码核验。

### 1. 实体字段加密、解密与查询值转换 (`mp.field-encryption`，27.9625)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.FieldEncrypt`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L26)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:31](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L31)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java:20](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java#L20)。

测试：无专用行为测试关联。

边界：原始 BSON、字符串字段和 wrapper-only 更新不能假定有相同加密处理。；不承诺随机加密算法支持等值检索或范围检索。

### 2. 连接配置密文解密 (`mp.encrypted-config`，8.6664)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-boot-starter:com.mongoplus.property.EncryptionPostProcessor`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:30](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L30)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:70](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L70)。

测试：无专用行为测试关联。

边界：只处理实现筛选的属性源，不能承诺所有环境变量/外部配置源均解密。；这是启动配置解密，与实体字段加密分开。

### 3. 创建/修改字段自动填充 (`mp.auto-fill`，6.0696)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.MetaObjectHandler`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField#fill`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L96)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java:50](../mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java#L50)。

测试：无专用行为测试关联。

边界：wrapper-only/原始 BSON 更新不触发实体自动填充。；填充值由用户实现提供，框架不会凭字段名自动选择时间。

## Q14：Mongo 命令成功以后回调我的通知逻辑

测试集：`original`；预期：mp.command-events；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：命令成功 Listener；通知发送自行实现，不代表事务提交。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，31.9806)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 2. 执行前校验、参数改写与执行控制 (`mp.execution-extension`，7.9631)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.Interceptor`；`mongo-plus-core:com.mongoplus.interceptor.AdvancedInterceptor`；`mongo-plus-core:com.mongoplus.interceptor.Invocation#proceed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java:45](../mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java#L45)；[mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java:29](../mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java#L29)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:26](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L26)。

测试：无专用行为测试关联。

边界：普通 before 取决于策略映射，异常路径不会进入普通 after。；直接 Driver 操作不自动经过 Execute 代理。；不要用命令成功通知替代此参数校验扩展。

## Q15：所有数据库操作执行之前先统一检查业务参数

测试集：`original`；预期：mp.execution-extension；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：执行拦截器校验，不能误当 Driver 命令成功回调。

### 1. 执行前校验、参数改写与执行控制 (`mp.execution-extension`，24.3676)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.Interceptor`；`mongo-plus-core:com.mongoplus.interceptor.AdvancedInterceptor`；`mongo-plus-core:com.mongoplus.interceptor.Invocation#proceed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java:45](../mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java#L45)；[mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java:29](../mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java#L29)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:26](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L26)。

测试：无专用行为测试关联。

边界：普通 before 取决于策略映射，异常路径不会进入普通 after。；直接 Driver 操作不自动经过 Execute 代理。；不要用命令成功通知替代此参数校验扩展。

### 2. NonNull 自动参数校验候选 (`mp.nonnull-validation`，10.3433)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。

实现：[mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)。

测试：无专用行为测试关联。

边界：不得将静态类型标记推荐成运行时 Bean Validation。

### 3. 可选数据源分片路由 (`mp.sharding`，9.239)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-sharding:com.mongoplus.sharding.AbstractDataSourceShardingHandler`；`mongo-plus-sharding:com.mongoplus.sharding.DataSourceShardingStrategy`；`mongo-plus-sharding:com.mongoplus.interceptor.DataSourceShardingInterceptor`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77](../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L77)；[mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11](../mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)。

测试：无专用行为测试关联。

边界：实现要求分片策略为最后拦截器；顺序与并发组合需验证。；一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。

## Q16：订单按月份写到不同的集合名

测试集：`original`；预期：mp.dynamic-collection；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：动态集合 handler，不能推定同时自动建索引。

### 1. 运行时集合名选择 (`mp.dynamic-collection`，22.2292)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.CollectionNameHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java:32](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L32)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:297](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L297)。

测试：无专用行为测试关联。

边界：不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。；handler 必须在不匹配时返回原集合名。

### 2. 集合/字段命名与持久化选择 (`mp.field-mapping`，13.6615)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionName`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java:16](../mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java#L16)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:150](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L150)。

测试：无专用行为测试关联。

边界：字符串路径不具备 Lambda 原字段元数据的全部保障。；字段排除和默认 null 忽略会影响实际写入内容。

### 3. 可选数据源分片路由 (`mp.sharding`，5.7838)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-sharding:com.mongoplus.sharding.AbstractDataSourceShardingHandler`；`mongo-plus-sharding:com.mongoplus.sharding.DataSourceShardingStrategy`；`mongo-plus-sharding:com.mongoplus.interceptor.DataSourceShardingInterceptor`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77](../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L77)；[mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11](../mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)。

测试：无专用行为测试关联。

边界：实现要求分片策略为最后拦截器；顺序与并发组合需验证。；一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。

## Q17：同一个服务的部分方法要切换到另一个命名数据源

测试集：`original`；预期：mp.datasource-routing；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：MongoDs；嵌套和异步线程上下文有限制。

### 1. 声明式多数据源选择 (`mp.datasource-routing`，29.7681)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:63](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L63)。

测试：无专用行为测试关联。

边界：ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。；Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。

### 2. 运行时追加命名数据源 (`mp.runtime-datasource`，17.1768)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。

测试：无专用行为测试关联。

边界：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。

### 3. 异步多数据源镜像写 (`mp.async-mirror`，6.1958)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)；[mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)。

测试：无专用行为测试关联。

边界：任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。；重试、线程池关闭与异常处理需要调用方核验。

## Q18：application.yml 的数据库密码不要放明文

测试集：`original`；预期：mp.encrypted-config；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：配置属性解密，不能混为实体字段加密。

### 1. 连接配置密文解密 (`mp.encrypted-config`，19.6432)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-boot-starter:com.mongoplus.property.EncryptionPostProcessor`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:30](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L30)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:70](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L70)。

测试：无专用行为测试关联。

边界：只处理实现筛选的属性源，不能承诺所有环境变量/外部配置源均解密。；这是启动配置解密，与实体字段加密分开。

### 2. 数据库命令事件、回调与日志 (`mp.command-events`，5.4866)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 3. 可选数据源分片路由 (`mp.sharding`，5.3317)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-sharding:com.mongoplus.sharding.AbstractDataSourceShardingHandler`；`mongo-plus-sharding:com.mongoplus.sharding.DataSourceShardingStrategy`；`mongo-plus-sharding:com.mongoplus.interceptor.DataSourceShardingInterceptor`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77](../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L77)；[mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11](../mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)。

测试：无专用行为测试关联。

边界：实现要求分片策略为最后拦截器；顺序与并发组合需验证。；一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。

## Q19：查询实体没有声明的未知键，都收进扩展属性 Map

测试集：`original`；预期：mp.extra-fields；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：ExtraFields 源码可证读取，写回和冲突还需专项测试。

### 1. 接收实体未声明的额外字段 (`mp.extra-fields`，45.0702)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.ExtraFields`；`mongo-plus-core:com.mongoplus.holder.ExtraFieldsHolder`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:224](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L224)。

测试：无专用行为测试关联。

边界：只确认读取聚合未知字段；不承诺 Map 展平写回。；非 Map 字段会抛异常；自定义 mapType 需可实例化的具体类。

### 2. 查询结果 DTO 与泛型接收 (`mp.result-mapping`，11.9307)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapping.TypeReference`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:236](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L236)。

测试：无专用行为测试关联。

边界：保留泛型信息不等于任意嵌套集合均受支持；convertCollection 有 ArrayList 转型路径。；DTO 类型不自动定义查询的集合 namespace。

### 3. 实体字段加密、解密与查询值转换 (`mp.field-encryption`，6.0975)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.FieldEncrypt`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L26)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:31](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L31)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java:20](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java#L20)。

测试：无专用行为测试关联。

边界：原始 BSON、字符串字段和 wrapper-only 更新不能假定有相同加密处理。；不承诺随机加密算法支持等值检索或范围检索。

## Q20：我想先规范字段内容，再让下一字段处理器收到前一个的结果

测试集：`original`；预期：mp.field-chain；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：有序 FieldHandler/currentValue；必须显式注册。

### 1. 有序字段写入处理链 (`mp.field-chain`，25.313)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.FieldHandler`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#register`；`mongo-plus-core:com.mongoplus.handlers.FieldHandlerChain#registerAll`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java:106](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L106)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java:36](../mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java#L36)。

测试：无专用行为测试关联。

边界：旧单参数 handler 标记 Deprecated；双参数默认委托旧实现。；null 返回值表示保留当前值；全局可变链应在初始化期注册。；HandlerCache.fieldHandlers 是 Deprecated 兼容别名。

### 2. 读取字段加工与中止赋值 (`mp.read-chain`，8.7403)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.ReadHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:105](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L105)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。

测试：无专用行为测试关联。

边界：discontinue 终止的是当前字段 lambda，不能解释为中止整个查询。；TypeHandler 和通用转换与 ReadHandler 的组合应按实际顺序核验。

### 3. 创建/修改字段自动填充 (`mp.auto-fill`，6.9824)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.MetaObjectHandler`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField#fill`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L96)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java:50](../mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java#L50)。

测试：无专用行为测试关联。

边界：wrapper-only/原始 BSON 更新不触发实体自动填充。；填充值由用户实现提供，框架不会凭字段名自动选择时间。

## Q21：服务运行中为新客户追加一条数据库连接，不重启

测试集：`original`；预期：mp.runtime-datasource；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：appendTempDataSource；资源释放和同名覆盖需人工审核。

### 1. 运行时追加命名数据源 (`mp.runtime-datasource`，28.3064)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。

测试：无专用行为测试关联。

边界：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。

### 2. 连接池、URI 与 TLS 配置 (`mp.connection-config`，18.6096)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.model.BaseProperty`；`mongo-plus-core:com.mongoplus.toolkit.UrlJoint`；`mongo-plus-boot-starter:com.mongoplus.property.MongoDBConnectProperty`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java:27](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java#L27)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)。

测试：无专用行为测试关联。

边界：官网 Driver 参数默认值可能对应旧版本，本索引不将其当作当前运行保证。；已有 MongoClient Bean 与容器条件装配交互需实测。

### 3. 声明式多数据源选择 (`mp.datasource-routing`，11.7456)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:63](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L63)。

测试：无专用行为测试关联。

边界：ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。；Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。

## Q22：聚合按价格升序、时间降序混合排序，用排序规格体如何构造

测试集：`original`；预期：mp.stage-boundaries；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：sortSpecification 包装排序体，旧 sort(Bson) 透传。

### 1. 聚合阶段体与完整阶段的编码边界 (`mp.stage-boundaries`，23.8971)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#sortSpecification`；`mongo-plus-core:com.mongoplus.aggregate.LambdaAggregateWrapper#sort`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java:781](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/Aggregate.java#L781)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:321](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L321)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:326](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L326)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java:61](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java#L61) `legacySortBsonStillPassesTheExactObjectThrough`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java:27](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/SortSpecificationStageTest.java#L27) `mixedDirectionsPreserveOrderAndInt32InBothReducers`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/ProjectBsonStageBodyTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/ProjectBsonStageBodyTest.java#L18) `projectShouldEncodeCondArrayComputedField`。

边界：不能依据参数同为 Bson 就假定相同包装规则。；同一投影混用 include/exclude 的服务端合法性不由 Core 编码成功保证。

### 2. 条件查询、排序与投影 (`mp.conditional-query`，8.9223)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

### 3. 聚合管道编排与统计 (`mp.pipeline`，8.4072)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

## Q23：查询状态码后根据字典自动补上状态名称

测试集：`original`；预期：mp.dictionary-bind；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：只应返回候选；DataBindHandler 本身无默认自动接线。

### 1. 字典字段回写候选 (`mp.dictionary-bind`，37.0022)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.FieldBind`；`mongo-plus-core:com.mongoplus.handlers.DataBindHandler`；`mongo-plus-core:com.mongoplus.handlers.read.FieldBindHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java:34](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java#L34)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。

测试：无专用行为测试关联。

边界：不能仅注册 DataBindHandler 就假定自动生效。；target 转下划线与属性查找、字段遍历后续覆盖之间的行为需要测试。

### 2. 枚举值存储与还原 (`mp.enum-mapping`，12.7314)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.EnumValue`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java:17](../mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java#L17)；[mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java:18](../mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java#L18)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java:12](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java#L12)。

测试：无专用行为测试关联。

边界：官网示例手动注册映射器；当前实现已有默认值，可自定义覆盖。

## Q24：两个独立数据源的写入必须原子提交，要么都成功要么都失败

测试集：`original`；预期：mp.cross-source-atomic；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：协调代码不能证明分布式原子性，不可推荐为已支持。

### 1. 跨独立数据源原子事务候选 (`mp.cross-source-atomic`，53.4532)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)；[mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)。

测试：无专用行为测试关联。

边界：逐个会话 commit 不能作为原子保证。；不得将此候选直接推荐为分布式事务解决方案。

### 2. 原子字段更新与批量写入 (`mp.update-bulk`，9.9763)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:155](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L155)。

测试：无专用行为测试关联。

边界：wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。；批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。

### 3. 异步多数据源镜像写 (`mp.async-mirror`，8.393)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)；[mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)。

测试：无专用行为测试关联。

边界：任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。；重试、线程池关闭与异常处理需要调用方核验。

## Q25：字段标注 NonNull 就能在运行时自动校验非空吗

测试集：`original`；预期：mp.nonnull-validation；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：注解只有类型限定证据；应标记候选并提示执行扩展替代入口。

### 1. NonNull 自动参数校验候选 (`mp.nonnull-validation`，34.1973)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。

实现：[mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)。

测试：无专用行为测试关联。

边界：不得将静态类型标记推荐成运行时 Bean Validation。

### 2. 运行时集合名选择 (`mp.dynamic-collection`，8.1299)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.CollectionNameHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java:32](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L32)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:297](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L297)。

测试：无专用行为测试关联。

边界：不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。；handler 必须在不匹配时返回原集合名。

### 3. 运行时追加命名数据源 (`mp.runtime-datasource`，8.1299)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。

测试：无专用行为测试关联。

边界：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。

## Q26：文本发现敏感词要拒绝保存，还要维护自定义词库

测试集：`original`；预期：mp.sensitive-word；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：检测和词库维护有源码接线；显式 replace 不等于保存自动替换。

### 1. 敏感词校验、词库维护与显式替换 (`mp.sensitive-word`，33.3339)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-sensitive-word:com.mongoplus.annotation.SensitiveWord`；`mongo-plus-sensitive-word:com.mongoplus.manager.SensitiveWordManager`。

实现：[mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java:121](../mongo-plus-sensitive-word/src/main/java/com/mongoplus/property/SensitiveWordProperty.java#L121)；[mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:190](../mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java#L190)；[mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java:197](../mongo-plus-sensitive-word/src/main/java/com/mongoplus/manager/SensitiveWordManager.java#L197)。

测试：无专用行为测试关联。

边界：官网说暂不支持替换；当前仅明确存在手动 Manager.replace，不能推成保存时自动替换。；GLOBAL 会增加处理量；无本轮数据库/性能测试。

### 2. 自定义读写类型转换 (`mp.type-conversion`，6.9072)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.strategy.mapping.MappingStrategy`；`mongo-plus-core:com.mongoplus.strategy.conversion.ConversionStrategy`；`mongo-plus-core:com.mongoplus.handlers.TypeHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:123](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L123)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:137](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L137)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:164](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L164)。

测试：无专用行为测试关联。

边界：TypeHandler 返回 null 会回退框架转换。；同目标类型策略覆盖和泛型解析需要核对，不能随意给策略加代理。

### 3. 主键生成与回写 (`mp.id-generation`，5.8532)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.ID`；`mongo-plus-core:com.mongoplus.handlers.IdGenerateHandler`；`mongo-plus-annotation:com.mongoplus.enums.IdTypeEnum`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java:39](../mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java#L39)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:363](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L363)。

测试：无专用行为测试关联。

边界：不依据官网旧策略数量约束当前枚举。；自增策略涉及计数集合；不要与变动记录插件无验证地组合。

## Q27：把集合导出 ZIP，然后从这个备份恢复

测试集：`original`；预期：mp.backup；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：备份恢复入口；恢复会 drop，不能实际执行此验证。

### 1. 集合本地 JSON/ZIP 备份与恢复 (`mp.backup`，33.4325)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.BackupManager#export`；`mongo-plus-core:com.mongoplus.manager.BackupManager#imports`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:214](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L214)；[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:161](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L161)。

测试：无专用行为测试关联。

边界：只支持此处描述的 JSON/ZIP 备份入口；Excel/xls/xlsx 工作簿导出无能力证据。；导入首个 ZIP 条目前会 drop 目标集合；本任务仅阅读，没有执行备份恢复。；分页导出不是已证明的一致快照；未确认索引、类型及多集合恢复完整性。

### 2. 异步多数据源镜像写 (`mp.async-mirror`，6.0521)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)；[mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)。

测试：无专用行为测试关联。

边界：任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。；重试、线程池关闭与异常处理需要调用方核验。

## Q28：保存数据修改历史，并限制一次批量更新参数的数量

测试集：`original`；预期：mp.change-record；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：操作参数记录和阈值，不代表完整前后镜像。

### 1. 数据变动记录与批量阈值 (`mp.change-record`，40.1913)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:101](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L101)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:126](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L126)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:232](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L232)。

测试：无专用行为测试关联。

边界：记录源于操作参数，不是数据库 CDC 或完整前后镜像。；必须忽略记录集合以避免递归；不要与自增 ID 无验证地组合。；阈值按参数/models 计算，不等于 updateMany 实际匹配文档数量限制。

### 2. 关联查询与多分支子管道 (`mp.nested-pipeline`，15.282)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 3. 原子字段更新与批量写入 (`mp.update-bulk`，13.2555)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:155](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L155)。

测试：无专用行为测试关联。

边界：wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。；批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。

## Q29：几个 Mapper 操作放到一个事务中，异常时回滚

测试集：`original`；预期：mp.transaction；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：会话事务，部署条件和代理边界需要提示。

### 1. MongoDB 会话事务 (`mp.transaction`，14.744)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.transactional.MongoTransactional`；`mongo-plus-core:com.mongoplus.manager.MongoTransactionalManager`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java:21](../mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java#L21)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:41](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L41)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:14](../mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)。

测试：无专用行为测试关联。

边界：依赖支持事务的 MongoDB 部署和版本；直接 Driver 调用须显式携带会话。；上下文基于当前线程，不承诺异步传播或跨 MongoClient 原子性。；官网编程式示例使用较旧 MongoTransactionalOperate；该类 JavaDoc 指向 manager。

### 2. 实体 CRUD 与业务层入口 (`mp.entity-crud`，14.1299)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper`；`mongo-plus-core:com.mongoplus.repository.IRepository`；`mongo-plus-core:com.mongoplus.service.IService`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java:91](../mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java#L91)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java#L96)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:84](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L84)。

测试：无专用行为测试关联。

边界：返回 Boolean 是框架对 Driver 结果的封装，不能代表所有业务语义。；实体 saveBatch 不接受空集合；DefaultBaseMapperImpl 直接读取首元素。

### 3. 跨独立数据源原子事务候选 (`mp.cross-source-atomic`，6.8982)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)；[mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)。

测试：无专用行为测试关联。

边界：逐个会话 commit 不能作为原子保证。；不得将此候选直接推荐为分布式事务解决方案。

## Q30：事务提交后恰好一次可靠通知

测试集：`original`；预期：mp.command-events；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；相关命令事件可作导航，但不足以满足提交后恰好一次通知；负例必须拒绝推荐。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，24.6566)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 2. MongoDB 会话事务 (`mp.transaction`，12.2622)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-annotation:com.mongoplus.annotation.transactional.MongoTransactional`；`mongo-plus-core:com.mongoplus.manager.MongoTransactionalManager`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java:21](../mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java#L21)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:41](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L41)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:14](../mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)。

测试：无专用行为测试关联。

边界：依赖支持事务的 MongoDB 部署和版本；直接 Driver 调用须显式携带会话。；上下文基于当前线程，不承诺异步传播或跨 MongoClient 原子性。；官网编程式示例使用较旧 MongoTransactionalOperate；该类 JavaDoc 指向 manager。

### 3. 跨独立数据源原子事务候选 (`mp.cross-source-atomic`，6.2622)

状态 `candidate`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)；[mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)。

测试：无专用行为测试关联。

边界：逐个会话 commit 不能作为原子保证。；不得将此候选直接推荐为分布式事务解决方案。

## Q31：把所有分片结果自动合并以后做全局分页

测试集：`original`；预期：mp.sharding；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；分片模块一次选一个源，没有已确认的分片结果合并；分页词汇可能误导到普通分页。

### 1. 可选数据源分片路由 (`mp.sharding`，32.927)

状态 `source_verified`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-sharding:com.mongoplus.sharding.AbstractDataSourceShardingHandler`；`mongo-plus-sharding:com.mongoplus.sharding.DataSourceShardingStrategy`；`mongo-plus-sharding:com.mongoplus.interceptor.DataSourceShardingInterceptor`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java:77](../mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L77)；[mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11](../mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java#L11)。

测试：无专用行为测试关联。

边界：实现要求分片策略为最后拦截器；顺序与并发组合需验证。；一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。

### 2. 分页、计数与分页结果 (`mp.pagination`，10.5911)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper#page`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:386](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L386)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:393](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L393)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L441)。

测试：无专用行为测试关联。

边界：属于偏移分页；不能据此承诺游标分页或高并发一致快照。；稳定翻页需要调用方提供稳定排序。

## Q32：自动把 Mongo 数据导出为 Excel 工作簿

测试集：`original`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；当前目录没有 Excel 导出能力证据；不得将 JSON/ZIP 备份误当 Excel。

无匹配：没有同时满足检索约束与分数阈值的能力。

## H01：联表查出每位顾客最近一次购买，子管道先排序再 limit

测试集：`validation`；预期：mp.nested-pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 关联查询与多分支子管道 (`mp.nested-pipeline`，24.6656)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 2. 聚合管道编排与统计 (`mp.pipeline`，9.3946)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

### 3. 条件查询、排序与投影 (`mp.conditional-query`，6.5614)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

## H02：使用 lookup 的独立聚合分支筛选外部集合

测试集：`validation`；预期：mp.nested-pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 关联查询与多分支子管道 (`mp.nested-pipeline`，22.5723)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 2. 聚合管道编排与统计 (`mp.pipeline`，6.3458)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

### 3. 条件查询、排序与投影 (`mp.conditional-query`，6.2189)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

## H03：主集合只按更新时间排序，不涉及关联或多分支

测试集：`validation`；预期：mp.conditional-query；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 条件查询、排序与投影 (`mp.conditional-query`，8.9223)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

### 2. 原子字段更新与批量写入 (`mp.update-bulk`，6.5614)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:155](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L155)。

测试：无专用行为测试关联。

边界：wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。；批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。

### 3. 注解自动创建时序集合 (`mp.timeseries`，5.4703)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.TimeSeries`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java:34](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L34)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:324](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L324)。

测试：无专用行为测试关联。

边界：服务端版本/选项兼容未运行验证。；bucketMaxSpan/bucketRounding 分支会清除 metaField；捕获 MongoCommandException，不能凭启动继续就判定已创建。

## H04：监听 commandSucceeded 来统计成功命令耗时

测试集：`validation`；预期：mp.command-events；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，37.6069)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 2. 聚合管道编排与统计 (`mp.pipeline`，7.3332)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

## H05：通知需要在提交后可靠投递，而且恰好一次

测试集：`validation`；预期：mp.command-events；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，24.6566)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 2. 异步多数据源镜像写 (`mp.async-mirror`，5.4161)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)；[mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)。

测试：无专用行为测试关联。

边界：任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。；重试、线程池关闭与异常处理需要调用方核验。

### 3. MongoDB 会话事务 (`mp.transaction`，5.1972)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-annotation:com.mongoplus.annotation.transactional.MongoTransactional`；`mongo-plus-core:com.mongoplus.manager.MongoTransactionalManager`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java:21](../mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java#L21)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:41](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L41)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:14](../mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)。

测试：无专用行为测试关联。

边界：依赖支持事务的 MongoDB 部署和版本；直接 Driver 调用须显式携带会话。；上下文基于当前线程，不承诺异步传播或跨 MongoClient 原子性。；官网编程式示例使用较旧 MongoTransactionalOperate；该类 JavaDoc 指向 manager。

## H06：要求可靠消息通知 exactly once delivery

测试集：`validation`；预期：mp.command-events；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，21.4378)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

## H07：把查询结果写成 xlsx 文件

测试集：`validation`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；独立保留集；按能力的静态证据范围和已知边界评议。

无匹配：没有同时满足检索约束与分数阈值的能力。

## H08：生成带工作表的 Excel 报表

测试集：`validation`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；独立保留集；按能力的静态证据范围和已知边界评议。

无匹配：没有同时满足检索约束与分数阈值的能力。

## H09：用 BackupManager 把集合备份成 ZIP 文件

测试集：`validation`；预期：mp.backup；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 集合本地 JSON/ZIP 备份与恢复 (`mp.backup`，46.9325)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.BackupManager#export`；`mongo-plus-core:com.mongoplus.manager.BackupManager#imports`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:214](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L214)；[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:161](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L161)。

测试：无专用行为测试关联。

边界：只支持此处描述的 JSON/ZIP 备份入口；Excel/xls/xlsx 工作簿导出无能力证据。；导入首个 ZIP 条目前会 drop 目标集合；本任务仅阅读，没有执行备份恢复。；分页导出不是已证明的一致快照；未确认索引、类型及多集合恢复完整性。

## H10：从 ZIP 恢复集合前需要检查哪些限制

测试集：`validation`；预期：mp.backup；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 集合本地 JSON/ZIP 备份与恢复 (`mp.backup`，22.8804)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.BackupManager#export`；`mongo-plus-core:com.mongoplus.manager.BackupManager#imports`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:214](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L214)；[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:161](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L161)。

测试：无专用行为测试关联。

边界：只支持此处描述的 JSON/ZIP 备份入口；Excel/xls/xlsx 工作簿导出无能力证据。；导入首个 ZIP 条目前会 drop 目标集合；本任务仅阅读，没有执行备份恢复。；分页导出不是已证明的一致快照；未确认索引、类型及多集合恢复完整性。

### 2. 数据变动记录与批量阈值 (`mp.change-record`，6.7393)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:101](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L101)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:126](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L126)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:232](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L232)。

测试：无专用行为测试关联。

边界：记录源于操作参数，不是数据库 CDC 或完整前后镜像。；必须忽略记录集合以避免递归；不要与自增 ID 无验证地组合。；阈值按参数/models 计算，不等于 updateMany 实际匹配文档数量限制。

## H11：根据当前租户隔离数据，提供 TenantHandler

测试集：`validation`；预期：mp.tenant；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 租户条件隔离与显式忽略 (`mp.tenant`，50.2462)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.TenantHandler`；`mongo-plus-core:com.mongoplus.manager.TenantManager#withoutTenant`；`mongo-plus-annotation:com.mongoplus.annotation.tenant.IgnoreTenant`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java:93](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L93)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java:99](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L99)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:281](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L281)。

测试：无专用行为测试关联。

边界：不是数据库权限机制；直接 Driver 与原始路径应另核验。；lookup 子管道不自动递归注入租户；嵌套忽略及线程传播需核验。

### 2. 声明式多数据源选择 (`mp.datasource-routing`，7.3932)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:63](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L63)。

测试：无专用行为测试关联。

边界：ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。；Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。

### 3. 分页、计数与分页结果 (`mp.pagination`，5.2572)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper#page`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:386](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L386)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:393](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L393)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L441)。

测试：无专用行为测试关联。

边界：属于偏移分页；不能据此承诺游标分页或高并发一致快照。；稳定翻页需要调用方提供稳定排序。

## H12：用 MetaObjectHandler 自动写入创建时间和修改时间

测试集：`validation`；预期：mp.auto-fill；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 创建/修改字段自动填充 (`mp.auto-fill`，54.2824)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.MetaObjectHandler`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField#fill`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L96)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java:50](../mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java#L50)。

测试：无专用行为测试关联。

边界：wrapper-only/原始 BSON 更新不触发实体自动填充。；填充值由用户实现提供，框架不会凭字段名自动选择时间。

### 2. 注解自动创建时序集合 (`mp.timeseries`，13.4858)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.TimeSeries`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java:34](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L34)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:324](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L324)。

测试：无专用行为测试关联。

边界：服务端版本/选项兼容未运行验证。；bucketMaxSpan/bucketRounding 分支会清除 metaField；捕获 MongoCommandException，不能凭启动继续就判定已创建。

### 3. 原子字段更新与批量写入 (`mp.update-bulk`，8.64)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.update.UpdateWrapper`；`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.conditions.update.UpdateChainWrapper`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:147](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L147)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:118](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L118)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:155](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L155)。

测试：无专用行为测试关联。

边界：wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。；批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。

## H13：数据库密码以 ENC 形式放在配置文件里

测试集：`validation`；预期：mp.encrypted-config；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 连接配置密文解密 (`mp.encrypted-config`，36.6721)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-boot-starter:com.mongoplus.property.EncryptionPostProcessor`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:30](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L30)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:70](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L70)。

测试：无专用行为测试关联。

边界：只处理实现筛选的属性源，不能承诺所有环境变量/外部配置源均解密。；这是启动配置解密，与实体字段加密分开。

### 2. 数据库命令事件、回调与日志 (`mp.command-events`，7.2445)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 3. 连接池、URI 与 TLS 配置 (`mp.connection-config`，5.7445)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.model.BaseProperty`；`mongo-plus-core:com.mongoplus.toolkit.UrlJoint`；`mongo-plus-boot-starter:com.mongoplus.property.MongoDBConnectProperty`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java:27](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java#L27)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)。

测试：无专用行为测试关联。

边界：官网 Driver 参数默认值可能对应旧版本，本索引不将其当作当前运行保证。；已有 MongoClient Bean 与容器条件装配交互需实测。

## H14：使用 MongoDs 注解选择命名数据源

测试集：`validation`；预期：mp.datasource-routing；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 声明式多数据源选择 (`mp.datasource-routing`，23.3605)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:63](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L63)。

测试：无专用行为测试关联。

边界：ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。；Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。

### 2. 运行时追加命名数据源 (`mp.runtime-datasource`，14.9796)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。

测试：无专用行为测试关联。

边界：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。

### 3. 集合/字段命名与持久化选择 (`mp.field-mapping`，10.2672)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionName`；`mongo-plus-annotation:com.mongoplus.annotation.collection.CollectionField`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java:16](../mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java#L16)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:150](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L150)。

测试：无专用行为测试关联。

边界：字符串路径不具备 Lambda 原字段元数据的全部保障。；字段排除和默认 null 忽略会影响实际写入内容。

## H15：NonNull 注解是否已经提供运行时校验

测试集：`validation`；预期：mp.nonnull-validation；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. NonNull 自动参数校验候选 (`mp.nonnull-validation`，29.9935)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。

实现：[mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)。

测试：无专用行为测试关联。

边界：不得将静态类型标记推荐成运行时 Bean Validation。

### 2. 运行时追加命名数据源 (`mp.runtime-datasource`，10.9631)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.DataSourceManager#appendTempDataSource`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java:37](../mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java#L37)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java:247](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusConfiguration.java#L247)。

测试：无专用行为测试关联。

边界：同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。；覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。

### 3. 运行时集合名选择 (`mp.dynamic-collection`，8.1299)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.CollectionNameHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java:32](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L32)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:297](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L297)。

测试：无专用行为测试关联。

边界：不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。；handler 必须在不匹配时返回原集合名。

## H16：FieldBind 和 DataBindHandler 能否自动接线回写字典字段

测试集：`validation`；预期：mp.dictionary-bind；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：独立保留集；按能力的静态证据范围和已知边界评议。

### 1. 字典字段回写候选 (`mp.dictionary-bind`，60.4447)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.FieldBind`；`mongo-plus-core:com.mongoplus.handlers.DataBindHandler`；`mongo-plus-core:com.mongoplus.handlers.read.FieldBindHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java:34](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java#L34)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。

测试：无专用行为测试关联。

边界：不能仅注册 DataBindHandler 就假定自动生效。；target 转下划线与属性查找、字段遍历后续覆盖之间的行为需要测试。

### 2. 主键生成与回写 (`mp.id-generation`，9.179)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.ID`；`mongo-plus-core:com.mongoplus.handlers.IdGenerateHandler`；`mongo-plus-annotation:com.mongoplus.enums.IdTypeEnum`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java:39](../mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java#L39)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:363](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L363)。

测试：无专用行为测试关联。

边界：不依据官网旧策略数量约束当前枚举。；自增策略涉及计数集合；不要与变动记录插件无验证地组合。

### 3. NonNull 自动参数校验候选 (`mp.nonnull-validation`，6.179)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。

实现：[mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)。

测试：无专用行为测试关联。

边界：不得将静态类型标记推荐成运行时 Bean Validation。

## H17：请提供邮件收件箱的垃圾邮件分类

测试集：`validation`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；独立保留集；按能力的静态证据范围和已知边界评议。

无匹配：没有同时满足检索约束与分数阈值的能力。

## H18：生成 PDF 发票并添加电子签章

测试集：`validation`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；独立保留集；按能力的静态证据范围和已知边界评议。

无匹配：没有同时满足检索约束与分数阈值的能力。

## N01：会员关联订单集合，在内层按金额倒序保留前两条

测试集：`holdout`；预期：mp.nested-pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 关联查询与多分支子管道 (`mp.nested-pipeline`，11.5076)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

### 2. 运行时集合名选择 (`mp.dynamic-collection`，5.6744)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.CollectionNameHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java:32](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L32)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:297](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L297)。

测试：无专用行为测试关联。

边界：不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。；handler 必须在不匹配时返回原集合名。

## N02：查询商品按售价升序并仅投影标题

测试集：`holdout`；预期：mp.conditional-query；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 条件查询、排序与投影 (`mp.conditional-query`，10.053)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.conditions.query.QueryWrapper`；`mongo-plus-core:com.mongoplus.conditions.interfaces.query.logic.Logic`；`mongo-plus-core:com.mongoplus.toolkit.Filters#eq`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java:46](../mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:41](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L41)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java:62](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L62)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java:101](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/QueryPredicateEncodingTest.java#L101) `numericTypesFieldOrderAndNestedArrayOrderRemainExact`。

边界：condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。；Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。

### 2. 关联查询与多分支子管道 (`mp.nested-pipeline`，6.207)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate#lookup`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:436](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L436)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:441](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L441)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:446](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L446)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java:19](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/LookupNestedPipelineTest.java#L19) `preservesSortThenLimit`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java:23](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/FacetNestedPipelineTest.java#L23) `twoIndependentEntriesBecomeOneFacetInOrder`；[mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java:18](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/UnionWithNestedPipelineTest.java#L18) `u03PreservesSortThenLimit`。

边界：子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。；顶层租户/逻辑删除增强不能推定递归注入关联子管道。；BSON 结构测试不证明关联字段或变量在服务器上有效。

## N03：数据库命令执行失败时记录日志和耗时

测试集：`holdout`；预期：mp.command-events；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，37.9177)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

### 2. 数据变动记录与批量阈值 (`mp.change-record`，9.029)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:101](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L101)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:126](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L126)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java:232](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L232)。

测试：无专用行为测试关联。

边界：记录源于操作参数，不是数据库 CDC 或完整前后镜像。；必须忽略记录集合以避免递归；不要与自增 ID 无验证地组合。；阈值按参数/models 计算，不等于 updateMany 实际匹配文档数量限制。

### 3. 声明式多数据源选择 (`mp.datasource-routing`，5.8324)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.datasource.MongoDs`；`mongo-plus-core:com.mongoplus.handlers.DataSourceHandler`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java:33](../mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java#L33)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:59](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L59)；[mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java:63](../mongo-plus-core/src/main/java/com/mongoplus/manager/MongoPlusClient.java#L63)。

测试：无专用行为测试关联。

边界：ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。；Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。

## N04：业务通知必须可靠，且每条消息只能投递一次

测试集：`holdout`；预期：mp.command-events；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 数据库命令事件、回调与日志 (`mp.command-events`，24.6566)

状态 `documented`；可作为限定范围静态入口：False；判定 `insufficient`。

API：`mongo-plus-core:com.mongoplus.listener.Listener#commandSucceeded`；`mongo-plus-core:com.mongoplus.listener.Listener#commandStarted`；`mongo-plus-core:com.mongoplus.listener.Listener#commandFailed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)；[mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java:30](../mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L30)。

测试：无专用行为测试关联。

边界：命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。；已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。

## N05：请导出 .xls 表格

测试集：`holdout`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

无匹配：没有同时满足检索约束与分数阈值的能力。

## N06：将集合生成 Excel 工作簿供下载

测试集：`holdout`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

无匹配：没有同时满足检索约束与分数阈值的能力。

## N07：想把状态码通过字典回写成中文名

测试集：`holdout`；预期：mp.dictionary-bind；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 字典字段回写候选 (`mp.dictionary-bind`，23.9374)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.FieldBind`；`mongo-plus-core:com.mongoplus.handlers.DataBindHandler`；`mongo-plus-core:com.mongoplus.handlers.read.FieldBindHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java:34](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java#L34)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。

测试：无专用行为测试关联。

边界：不能仅注册 DataBindHandler 就假定自动生效。；target 转下划线与属性查找、字段遍历后续覆盖之间的行为需要测试。

### 2. 枚举值存储与还原 (`mp.enum-mapping`，10.1664)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.EnumValue`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java:17](../mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java#L17)；[mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java:18](../mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java#L18)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java:12](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java#L12)。

测试：无专用行为测试关联。

边界：官网示例手动注册映射器；当前实现已有默认值，可自定义覆盖。

### 3. 主键生成与回写 (`mp.id-generation`，7.3332)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.ID`；`mongo-plus-core:com.mongoplus.handlers.IdGenerateHandler`；`mongo-plus-annotation:com.mongoplus.enums.IdTypeEnum`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:46](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L46)；[mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java:39](../mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/AbstractIdGenerateHandler.java#L39)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:363](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L363)。

测试：无专用行为测试关联。

边界：不依据官网旧策略数量约束当前枚举。；自增策略涉及计数集合；不要与变动记录插件无验证地组合。

## N08：两个独立数据源原子提交可以保证同时回滚吗

测试集：`holdout`；预期：mp.cross-source-atomic；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 跨独立数据源原子事务候选 (`mp.cross-source-atomic`，51.6169)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-sharding:com.mongoplus.sharding.ShardingTransactionalHandler`。

实现：[mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java:19](../mongo-plus-sharding/src/main/java/com/mongoplus/sharding/ShardingTransactionalHandler.java#L19)；[mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java:49](../mongo-plus-sharding/src/main/java/com/mongoplus/context/ShardingTransactionContext.java#L49)。

测试：无专用行为测试关联。

边界：逐个会话 commit 不能作为原子保证。；不得将此候选直接推荐为分布式事务解决方案。

### 2. MongoDB 会话事务 (`mp.transaction`，12.029)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.transactional.MongoTransactional`；`mongo-plus-core:com.mongoplus.manager.MongoTransactionalManager`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java:21](../mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java#L21)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:41](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L41)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java:14](../mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java#L14)。

测试：无专用行为测试关联。

边界：依赖支持事务的 MongoDB 部署和版本；直接 Driver 调用须显式携带会话。；上下文基于当前线程，不承诺异步传播或跨 MongoClient 原子性。；官网编程式示例使用较旧 MongoTransactionalOperate；该类 JavaDoc 指向 manager。

### 3. 异步多数据源镜像写 (`mp.async-mirror`，10.9579)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor`；`mongo-plus-core:com.mongoplus.handlers.write.MultipleWriteHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:92](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L92)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java:179](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L179)；[mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java:355](../mongo-plus-core/src/main/java/com/mongoplus/config/Configuration.java#L355)。

测试：无专用行为测试关联。

边界：任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。；重试、线程池关闭与异常处理需要调用方核验。

## N09：通过 Version 防止并发覆盖，版本冲突时进行重试

测试集：`holdout`；预期：mp.optimistic-lock；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 版本乐观锁与受控重试 (`mp.optimistic-lock`，68.5524)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.Version`；`mongo-plus-core:com.mongoplus.interceptor.business.OptimisticLockerInterceptor`；`mongo-plus-core:com.mongoplus.model.Retry`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java:403](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L403)；[mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java:163](../mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L163)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:228](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L228)。

测试：无专用行为测试关联。

边界：当前处理路径用 Integer/getInteger，不承诺任意 Number 类型。；首次更新需在 $set 中携带旧版本；缺失时默认跳过版本增强，可配置异常。；必须确认 BSON 结构被真正回写；不要复用更新 Wrapper。；官网说异步重试立即返回，当前源码 supplyAsync(...).get() 会等待结果。

## N10：需要将集合备份为 ZIP 压缩包保存在本地

测试集：`holdout`；预期：mp.backup；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 集合本地 JSON/ZIP 备份与恢复 (`mp.backup`，40.037)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.manager.BackupManager#export`；`mongo-plus-core:com.mongoplus.manager.BackupManager#imports`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:214](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L214)；[mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java:161](../mongo-plus-core/src/main/java/com/mongoplus/manager/BackupManager.java#L161)。

测试：无专用行为测试关联。

边界：只支持此处描述的 JSON/ZIP 备份入口；Excel/xls/xlsx 工作簿导出无能力证据。；导入首个 ZIP 条目前会 drop 目标集合；本任务仅阅读，没有执行备份恢复。；分页导出不是已证明的一致快照；未确认索引、类型及多集合恢复完整性。

### 2. 注解自动创建时序集合 (`mp.timeseries`，5.0553)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.TimeSeries`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java:34](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/AutoUtil.java#L34)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:324](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L324)。

测试：无专用行为测试关联。

边界：服务端版本/选项兼容未运行验证。；bucketMaxSpan/bucketRounding 分支会清除 metaField；捕获 MongoCommandException，不能凭启动继续就判定已创建。

## N11：给参数加 NonNull 是否就有空值校验

测试集：`holdout`；预期：mp.nonnull-validation；预期证据级别：`candidate`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. NonNull 自动参数校验候选 (`mp.nonnull-validation`，35.1675)

状态 `candidate`；可作为限定范围静态入口：False；判定 `candidate`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.NonNull`。

实现：[mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10](../mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:79](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L79)。

测试：无专用行为测试关联。

边界：不得将静态类型标记推荐成运行时 Bean Validation。

### 2. 执行前校验、参数改写与执行控制 (`mp.execution-extension`，10.3441)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.interceptor.Interceptor`；`mongo-plus-core:com.mongoplus.interceptor.AdvancedInterceptor`；`mongo-plus-core:com.mongoplus.interceptor.Invocation#proceed`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java:45](../mongo-plus-core/src/main/java/com/mongoplus/proxy/ExecutorProxy.java#L45)；[mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java:29](../mongo-plus-core/src/main/java/com/mongoplus/proxy/AdvancedProxy.java#L29)；[mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java:26](../mongo-plus-core/src/main/java/com/mongoplus/execute/ExecutorFactory.java#L26)。

测试：无专用行为测试关联。

边界：普通 before 取决于策略映射，异常路径不会进入普通 after。；直接 Driver 操作不自动经过 Execute 代理。；不要用命令成功通知替代此参数校验扩展。

## N12：使用 BaseMapper 直接读取 Document，不定义实体

测试集：`holdout`；预期：mp.raw-bson；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 无实体 Document 与原始 BSON 访问 (`mp.raw-bson`，39.3406)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.BaseMapper`；`mongo-plus-core:com.mongoplus.manager.MongoPlusClient#getCollection`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:281](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L281)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:287](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L287)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:299](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L299)。

测试：无专用行为测试关联。

边界：直接 Driver 操作与 Mapper 插件/转换链不同。；实体元数据依赖的逻辑删除、版本、填充不能无条件套用于无实体路径。

### 2. 实体 CRUD 与业务层入口 (`mp.entity-crud`，7.5521)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapper.MongoMapper`；`mongo-plus-core:com.mongoplus.repository.IRepository`；`mongo-plus-core:com.mongoplus.service.IService`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java:91](../mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java#L91)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java:96](../mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java#L96)；[mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java:84](../mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java#L84)。

测试：无专用行为测试关联。

边界：返回 Boolean 是框架对 Driver 结果的封装，不能代表所有业务语义。；实体 saveBatch 不接受空集合；DefaultBaseMapperImpl 直接读取首元素。

### 3. 接收实体未声明的额外字段 (`mp.extra-fields`，6.5647)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.ExtraFields`；`mongo-plus-core:com.mongoplus.holder.ExtraFieldsHolder`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:224](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L224)。

测试：无专用行为测试关联。

边界：只确认读取聚合未知字段；不承诺 Map 展平写回。；非 Map 字段会抛异常；自定义 mapType 需可实例化的具体类。

## N13：按区域分组统计订单销售额并求和

测试集：`holdout`；预期：mp.pipeline；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 聚合管道编排与统计 (`mp.pipeline`，23.2087)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.aggregate.Aggregate`；`mongo-plus-core:com.mongoplus.aggregate.AggregateWrapper`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:633](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L633)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:638](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L638)；[mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java:643](../mongo-plus-core/src/main/java/com/mongoplus/aggregate/LambdaAggregateWrapper.java#L643)。

测试：[mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java:35](../mongo-plus-core/src/test/java/com/mongoplus/aggregate/CommonStageEffectTest.java#L35) `everyAuditedOverloadAppendsOnceToSameReceiver`。

边界：部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。；阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。

## N14：用 TypeReference 把查询结果映射为 DTO

测试集：`holdout`；预期：mp.result-mapping；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 查询结果 DTO 与泛型接收 (`mp.result-mapping`，46.5966)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.mapping.TypeReference`；`mongo-plus-core:com.mongoplus.mapper.MongoMapper#list`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java:236](../mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L236)。

测试：无专用行为测试关联。

边界：保留泛型信息不等于任意嵌套集合均受支持；convertCollection 有 ArrayList 转型路径。；DTO 类型不自动定义查询的集合 namespace。

### 2. 读取字段加工与中止赋值 (`mp.read-chain`，6.0791)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.handlers.ReadHandler`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java:130](../mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L130)；[mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java:105](../mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L105)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java:374](../mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java#L374)。

测试：无专用行为测试关联。

边界：discontinue 终止的是当前字段 lambda，不能解释为中止整个查询。；TypeHandler 和通用转换与 ReadHandler 的组合应按实际顺序核验。

### 3. 单值 DBRef 引用映射 (`mp.dbref`，5.3489)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.collection.DBRef`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:64](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L64)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:69](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L69)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java:85](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L85)。

测试：无专用行为测试关联。

边界：仅静态确认单值路径；不承诺集合引用、懒加载、级联保存或删除。；读取会增加查询，跨源/事务组合未验证。

## N15：合成一段钢琴音乐保存为 mp3

测试集：`holdout`；预期：无证据应拒绝推荐；预期证据级别：`insufficient`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：False。

人工评议：证据不足，不能推荐；最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

无匹配：没有同时满足检索约束与分数阈值的能力。

## N16：使用 mpw.key 解密配置文件中的数据库密码

测试集：`holdout`；预期：mp.encrypted-config；预期证据级别：`scoped_static_evidence`。

首项合理：True；前三包含预期：True；证据安全：True；通过：True；人工允许按条目范围推荐：True。

人工评议：最终独立保留集；不用于规则调整，静态导航不等于功能运行验证。

### 1. 连接配置密文解密 (`mp.encrypted-config`，51.3281)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-boot-starter:com.mongoplus.property.EncryptionPostProcessor`。

实现：[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:30](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L30)；[mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java:70](../mongo-plus-boot-starter/src/main/java/com/mongoplus/property/EncryptionPostProcessor.java#L70)。

测试：无专用行为测试关联。

边界：只处理实现筛选的属性源，不能承诺所有环境变量/外部配置源均解密。；这是启动配置解密，与实体字段加密分开。

### 2. 实体字段加密、解密与查询值转换 (`mp.field-encryption`，8.3317)

状态 `source_verified`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-annotation:com.mongoplus.annotation.comm.FieldEncrypt`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:26](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L26)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java:31](../mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L31)；[mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java:20](../mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java#L20)。

测试：无专用行为测试关联。

边界：原始 BSON、字符串字段和 wrapper-only 更新不能假定有相同加密处理。；不承诺随机加密算法支持等值检索或范围检索。

### 3. 连接池、URI 与 TLS 配置 (`mp.connection-config`，7.5023)

状态 `documented`；可作为限定范围静态入口：True；判定 `scoped_static_evidence`。

API：`mongo-plus-core:com.mongoplus.model.BaseProperty`；`mongo-plus-core:com.mongoplus.toolkit.UrlJoint`；`mongo-plus-boot-starter:com.mongoplus.property.MongoDBConnectProperty`。

实现：[mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java:27](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/UrlJoint.java#L27)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:38](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L38)；[mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java:76](../mongo-plus-core/src/main/java/com/mongoplus/toolkit/MongoUtil.java#L76)。

测试：无专用行为测试关联。

边界：官网 Driver 参数默认值可能对应旧版本，本索引不将其当作当前运行保证。；已有 MongoClient Bean 与容器条件装配交互需实测。

