# 多文档事务、数据源与动态集合

“订单和审计记录要一起落库，第二步失败必须撤销第一步。” 在支持事务的副本集或分片集群上，确保两次 Mapper 写入同一受管 MongoDB 会话。在 Spring 中可开启 `mongo-plus.spring.transaction=true`，明确选择 `mongoPlusTransactionalManager`。以下业务 Bean 由 Spring 容器创建，审计操作表示另一个已注入的、使用同一数据源 Mapper 的应用组件。

```java
import com.mongoplus.mapper.MongoMapper;
import org.springframework.transaction.annotation.Transactional;

class OrderTransaction<T> {
    private final MongoMapper<T> orders;
    private final java.util.function.Consumer<T> insertMongoAudit;
    OrderTransaction(MongoMapper<T> orders, java.util.function.Consumer<T> insertMongoAudit) {
        this.orders = orders;
        this.insertMongoAudit = insertMongoAudit;
    }
    @Transactional(transactionManager = "mongoPlusTransactionalManager", rollbackFor = Exception.class)
    public void place(T order) {
        if (!orders.save(order)) throw new IllegalStateException("Order write failed");
        insertMongoAudit.accept(order);
    }
}
```

通过容器代理调用 public 方法；同类自调用、异步线程或另一个客户端受不同上下文管理。MongoPlus 也提供方法注解 `@MongoTransactional`，按所用 Starter 的切面和 rollback 配置核验，不要叠加两个事务入口。验证时让第二次写失败，用独立客户端检查提交前、提交后、异常回滚后的两集合。单文档条件更新通常已满足原子性；多库、消息和 HTTP 副作用分别设计一致性。

## 数据分布在哪

“读归档数据源”“订单按业务单元分别落集合。” 前者切换 MongoClient，后者改变物理集合。声明式数据源用 `@MongoDs("archive")` 标在受代理方法或类型上；`DataSourceHandler.getDataSource(String)` 可解析注解中的路由键。配置中先定义目标数据源，再检查方法执行线程的实际 database/collection。

```java
import com.mongoplus.handlers.CollectionNameHandler;

class OrderRouting {
    static CollectionNameHandler forArchive() {
        return (operation, args, ns) -> "orders".equals(ns.getCollectionName())
            ? "orders_archive" : ns.getCollectionName();
    }
}
```

在目标 Starter 中把 `CollectionNameHandler` 注册为 Bean；`DynamicCollectionNameInterceptor` 把底层操作的集合换为返回名称。按日期分表时，读、更新和删除要能找到原分区；仅按当前日期路由会漏掉旧文档。若需求是按操作类型分发到多个数据源，另核对可选 mongo-plus-sharding 模块的 DataSourceShardingHandler 与业务分片策略；多源分发的结果和提交边界需单独验证。动态集合与 `lookup.from`、索引初始化分别解析，显式检查外表和每个物理分区的索引。数据源切换也不能自动跨线程传播或实现跨集群事务。对复杂嵌套切换核验还原行为；手动 `changeDataSource` 的调用方负责清理上下文。

依据 · [MongoTransactionManagerAutoConfiguration](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoTransactionManagerAutoConfiguration.java) · [MongoPlusTransactionalManager](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/transactional/MongoPlusTransactionalManager.java) · [MongoTransactional](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java) · [MongoDataSourceAspect](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/datasource/MongoDataSourceAspect.java) · [CollectionNameHandler](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/CollectionNameHandler.java) · [DynamicCollectionNameInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java) · [DataSourceManager](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/manager/DataSourceManager.java) · [官方事务](https://mongoplus.com/docs/transactional.html) · [官方动态数据源](https://mongoplus.com/docs/dataSource.html)
<!-- COMPLETE_COVERAGE -->
## 完整数据源与事务选型

| 需求 | API/配置 | 验证边界 |
|---|---|---|
| 声明命名数据源 | BaseProperty/SlaveDataSource、Configuration.dataSource | 默认master；客户端、database与collection管理一起注册 |
| 运行时新增或切换 | DataSourceManager.appendTempDataSource/changeDataSource/clearDataSource | 替换旧client、缓存、嵌套恢复和线程池复用 |
| 注解路由 | MongoDs与DataSourceHandler | 先注册数据源；Boot代理/类级方法级；Solon绑定另核对 |
| 动态集合 | CollectionNameHandler/DynamicCollectionNameInterceptor | 不递归改变外表、不自动创建每个分区索引 |
| 编程式事务 | MongoTransactionalOperate、MongoTransactionalManager | start/commit/rollback/close生命周期，异常后清理 |
| Spring传播 | MongoPlusTransactionalManager + @Transactional | REQUIRED/REQUIRES_NEW/NOT_SUPPORTED等按真实代理测试；不推定支持savepoint |
| MongoPlus声明事务 | MongoTransactional | rollbackFor/noRollbackFor、causallyConsistent/snapshot及事务options |
| 读偏好 | MongoTransactional.preference中的MongoReadPreference | 当前只消费第一项；独立方法标注未确认，不当作路由读副本的通用切面 |
| 数据源分片 | [分片专题](sharding.md) | 多资源事务不等于跨client原子事务 |

事务上下文与session绑定执行器选择：ExecutorFactory在事务中选择SessionExecute，非事务选择DefaultExecute。真实验证至少比较独立客户端在提交前不可见、提交后可见、回滚后不存在，检查同线程后续调用和session关闭。传播测试和容器绑定没有运行时标NOT_RUN，不能由编译或直接Driver事务测试代替。

Core编程式优先使用已管理事务上下文的MongoTransactionalManager。旧MongoTransactionalOperate创建/启动ClientSession，但这些方法自身没有把session装入MongoTransactionContext；其startTransaction(String dataSourceName)当前未消费该参数。不能仅调用它后就声称后续Mapper已加入指定数据源事务。这部分组合契约保留UNVERIFIED，不推荐靠参数名推断。

所有事务/路由注解属性见[完整记录](../annotations/routing-isolation.md)，方法重载见[管理API](api/management.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C074 多数据源声明与动态新增**：`DataSourceManager`, `MongoClientFactory`。
- **C075 动态物理集合**：`DynamicCollectionNameInterceptor`, `CollectionNameHandler`。
- **C076 编程式MongoDB事务**：`MongoTransactionalOperate`, `MongoTransactionalManager`。
- **C077 Spring事务与传播**：`MongoPlusTransactionalManager`, `MongoTransactionalAspect`。
- **C078 事务读偏好与选项**：`MongoTransactionalManager#startTransaction`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
