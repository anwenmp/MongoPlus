# 按部门限制数据访问

“主管只能查看和修改自己部门的订单，即使知道其他部门的订单号也一样。” 把策略放进 MongoPlus `Interceptor`，从受信身份取得可见部门。下面的服务开放普通查询和单条备注修改，两条路径都把原过滤与权限谓词用 `$and` 叠加。示例假定固定数据库和集合，生产环境按实际 namespace 确定保护范围。

```java
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.QueryParam;
import com.mongoplus.model.MutablePair;
import com.mongoplus.toolkit.Filters;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import org.bson.Document;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

class DepartmentReads implements Interceptor {
    private final String collectionName;
    private final Supplier<Collection<String>> allowed;
    DepartmentReads(String collectionName, Supplier<Collection<String>> allowed) {
        this.collectionName = collectionName;
        this.allowed = allowed;
    }
    private boolean protects(MongoCollection<Document> c) {
        return collectionName.equals(c.getNamespace().getCollectionName());
    }
    public void beforeExecute(ExecuteMethodEnum op, Object[] args, MongoCollection<Document> c) {
        if (!protects(c) || op == ExecuteMethodEnum.QUERY) return;
        if (op != ExecuteMethodEnum.UPDATE_ONE) throw new SecurityException("Unsupported operation");
        UpdateOptions options = (UpdateOptions) args[1];
        if (options != null && options.isUpsert()) throw new SecurityException("Upsert denied");
    }
    private Bson restricted(Bson original) {
        Bson scope = Filters.in("department_id", Objects.requireNonNull(allowed.get()));
        return original == null ? scope : Filters.and(original, scope);
    }
    public QueryParam executeQuery(Bson original, BasicDBObject projection,
                                   BasicDBObject sort, MongoCollection<Document> c) {
        if (!protects(c)) return new QueryParam(original, projection, sort);
        return new QueryParam(restricted(original), projection, sort);
    }
    public MutablePair<Bson,Bson> executeUpdate(MutablePair<Bson,Bson> pair,
                                                MongoCollection<Document> c) {
        if (!protects(c)) return pair;
        BsonDocument update = pair.getRight().toBsonDocument(BsonDocument.class, c.getCodecRegistry());
        if (update.size() != 1 || !update.isDocument("$set")) throw new SecurityException("Invalid update");
        BsonDocument set = update.getDocument("$set");
        if (set.size() != 1 || !set.isString("note")) throw new SecurityException("Only note may change");
        return new MutablePair<>(restricted(pair.getLeft()), pair.getRight());
    }
}
```

把实例注册为 Starter 的 `Interceptor` Bean。权限集合为空时 `$in:[]` 不命中任何行；缺失身份应拒绝。若路由也会改集合名，同时核验原始与最终 namespace。原生 Driver、不经过 MongoPlus 的服务以及其他客户端需要自己的访问控制。

| 要开放的入口 | 应实现和验证的边界 |
|---|---|
| 分页、计数 | `executeCount(BasicDBObject, CountOptions, MongoCollection<Document>)` 使用相同范围，保留 options；在没有租户或逻辑删除等限制时，空 Wrapper 可能转到 estimatedDocumentCount，无法追加过滤条件。拒绝该入口，或由服务显式提供受信范围以走准确计数 |
| 更新、删除 | `executeUpdate` 单个/列表 `MutablePair<Bson,Bson>` 的 left 是过滤，right 是更新；同样处理 `executeRemove`、`executeRemoveOne` 并验证实际执行策略；限制写入对归属字段的修改 |
| 插入、批写 | 为每条写入设置或校验可信归属，逐个检查 bulk model 以及替换、upsert；缺少覆盖的操作保持拒绝 |
| 聚合、关联 | `executeAggregates(List<Bson>, MongoCollection<Document>)` 约束主集合，显式约束子管道和外表；直接在第一个阶段前追加过滤可能破坏有首阶段要求的算子 |

需要环绕执行、控制返回或短路时评估 `AdvancedInterceptor.intercept(Invocation) throws Throwable`，正常路径调用一次 `invocation.proceed()` 并返回结果。普通拦截器的成功回调不足以证明事务提交。测试至少包括同部门命中、伪造另一个部门、空权限、计数/分页和每条允许的写路径；不要用一次 `list` 通过来推断完整授权。

依据 · [Interceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/Interceptor.java) · [AdvancedInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/AdvancedInterceptor.java) · [Invocation](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/Invocation.java) · [QueryExecutorStrategy](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/executor/impl/QueryExecutorStrategy.java) · [CountExecutorStrategy](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/executor/impl/CountExecutorStrategy.java) · [MongoPlusAutoConfiguration](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/config/MongoPlusAutoConfiguration.java) · [官方拦截器](https://mongoplus.com/docs/interceptor.html)
<!-- COMPLETE_COVERAGE -->
## 注册、顺序与生命周期

普通链以Interceptor.order升序排序。beforeExecute接收执行枚举、参数数组与原集合；对应MethodExecutorStrategy调用executeQuery/executeUpdate等策略，再由Execute调用Driver；成功路径调用afterExecute。索引方法的执行枚举可能为null，不能在插件中无条件switch。批写model必须真正重建/回写到Driver参数，修改临时Pair不等于生效。

高级链通过AdvancedInterceptor.intercept(Invocation)环绕调用；order排序与包装方向共同决定进入/退出顺序。正常路径proceed一次，短路要提供与方法返回类型相容的值。LogicRemove和OptimisticLocker位于靠后order，不根据Bean声明顺序猜最终调用次序。异常是否走after、ThreadLocal是否清理及返回值传播要分别检验。

| 需求 | 选用接口 | 检查边界 |
|---|---|---|
| 改写filter/projection/update/collection | Interceptor | query/count/remove/removeOne/updateOne/updateMany/bulk/aggregate/index覆盖 |
| 环绕、短路、替换操作类型 | AdvancedInterceptor | Invocation.proceed/discontinue、session、返回值和异常 |
| 字段值转换 | FieldHandler/ReadHandler | 不绕到执行拦截器重复实现字段生命周期 |
| 命令观察 | Listener | 已是Driver命令层，不将日志回调当事务提交事件 |

注册使用Configuration.interceptor/advancedInterceptor或对应容器Bean；链是静态的，反复初始化可能追加。动态集合拦截器改参数数组最后的collection，较早插件持有的旧引用不会自动更新。完整签名见[扩展API](api/extensions.md)，跨容器接线见[集成](integration.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C067 普通拦截器与权限注入**：`Interceptor`, `InterceptorChain`。
- **C068 高级拦截器与执行改写**：`AdvancedInterceptor`, `Invocation`, `AdvancedInterceptorChain`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
