# 数据源分片

这是MongoPlus的可选数据源分片模块，与MongoDB服务端sharded collection不同。`DataSourceShardingStrategy.sharding(currentDataSource, ExecuteMethodEnum, Object[] source)`返回数据源名，也可返回通配符/正则选择。`DataSourceShardingHandler`、`AbstractDataSourceShardingHandler`负责候选名称、缓存与负载选择；`DataSourceShardingInterceptor`同时参与普通和高级执行链。

```java
import com.mongoplus.sharding.DataSourceShardingStrategy;
import com.mongoplus.enums.ExecuteMethodEnum;

class ArchiveRouting implements DataSourceShardingStrategy {
    public String sharding(String current, ExecuteMethodEnum method, Object[] source) {
        return current;
    }
}
```

上例仅展示接口形状，不是完成路由策略的注册。业务应根据真实`source`签名选择分片键，先验证每个执行方法，而不是假设第一个参数永远为实体。命名数据源须已在MongoClientFactory/MongoPlusClient中登记。`AbstractDataSourceShardingHandler.builder()`及其setShardingMapping配置策略映射，具体泛型/映射类型查[可选模块API](api/optional-modules.md)。

Boot3分片Starter通过MongoShardingConfiguration安装DataSourceShardingInterceptor，并将HandlerCache.transactionHandler替换为ShardingTransactionalHandler。其他容器需显式注册，没有当前源码支持的Boot4专用Starter可直接推荐。

ShardingTransactionContext记录各资源的事务状态，提供逐资源commit/rollback/close。不能把多个MongoClient分别提交描述为分布式原子事务。验证要覆盖空匹配、多候选、读写一致性、策略异常、嵌套路由、动态集合、bulk、各资源提交失败和清理。未完成的组合为`UNVERIFIED`，不把设计风险自动定为产品缺陷。

依据：mongo-plus-sharding全部公开类型、MongoShardingConfiguration及POM，精确路径见[公开类型矩阵](../audit/public-types.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C079 数据源分片与路由策略**：`DataSourceShardingStrategy`, `DataSourceShardingHandler`。
- **C080 分片事务及Starter**：`ShardingTransactionalHandler`, `MongoShardingConfiguration`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
