# 租户边界与逻辑删除

“一个服务为多个客户共用集合，用户只能访问自己租户的数据。” 需求是单一租户判等时，可让 `TenantHandler` 提供可信身份，Starter 收集该 Bean 并构造 `TenantInterceptor`；纯 Core 用 `Configuration.tenantHandler(handler)`。默认字段为 `tenant_id`，返回 BSON 类型应与历史数据一致。示例由应用提供从认证上下文取当前租户的 Supplier。

```java
import com.mongoplus.handlers.TenantHandler;
import java.util.function.Supplier;
import org.bson.BsonString;
import org.bson.BsonValue;

class RequestTenant implements TenantHandler {
    private final Supplier<String> identity;
    RequestTenant(Supplier<String> identity) { this.identity = identity; }
    public BsonValue getTenantId() {
        String id = identity.get();
        if (id == null || id.trim().isEmpty()) throw new SecurityException("Tenant required");
        return new BsonString(id);
    }
}
```

2.2.0 的 `appendTenantFilter` 在已有顶层同名字段时保留原值；插入也有 `ignoreInsert` 和 `putIfAbsent` 分支。因此对外只接收业务 DTO，服务端掌控租户条件及写入值，拒绝任意过滤器、篡改 tenant 字段的 patch/replace/upsert。批量插入会查看首条已有的列，检验每一条。`executeRemoveOne`、各类 bulk、`findOneAndRemove` 都需独立核对；受保护操作只开放已验证的路由。多角色可见多个部门时转 [权限拦截器](interceptors.md)。

验证时放 A、B 两租户同业务键，检查 A 的读、页总数、更新及删除后 B 的原始文档；包括有意传入 B 的租户值、空上下文。异步线程需重新建立可信上下文。租户过滤只改顶层 pipeline，关联外表需在子管道内限制。`ignoreDatabase`、`ignoreCollection` 和绕过上下文用于显式特例，范围要尽量小。

“删除后可审计，但普通列表看不到。” 在 `@CollectionName` 实体的标记字段用 `@CollectionLogic`，在 Boot 配置 mongo-plus.configuration.logic.open=true 并注册实体元数据。下面的删除/未删除值与字段都采用整数。

```java
import com.mongoplus.annotation.collection.CollectionLogic;
import com.mongoplus.enums.LogicDataType;

class DeletionMarker {
    @CollectionLogic(value = "0", delval = "1", delType = LogicDataType.INTEGER)
    public Integer deleted = 0;
}
```

把字段放进实际持久化实体，配置前核对存量记录的字段类型和缺失值。普通删除可被高级拦截器改写为更新，普通读、计数再追加可见条件；验证原文档仍存在而业务列表已隐藏。物理删除、关联子管道、聚合阶段顺序和批量替换需要各自的断言。

依据 · [TenantHandler](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/TenantHandler.java) · [TenantInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java) · [CollectionLogic](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionLogic.java) · [LogicProperty](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/LogicProperty.java) · [LogicRemoveInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/LogicRemoveInterceptor.java) · [CollectionLogiceInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/CollectionLogiceInterceptor.java) · [官方多租户](https://mongoplus.com/docs/tenant.html)
<!-- COMPLETE_COVERAGE -->
## 注解与恢复边界

完整声明见[路由隔离注解](../annotations/routing-isolation.md)：IgnoreTenant仅METHOD，IgnoreLogic为METHOD/TYPE，CollectionLogic为字段/注解类型。TenantHandler需注册TenantInterceptor；逻辑删除需LogicProperty.open与实体元数据及对应拦截器，不能只加注解。

TenantManager提供withoutTenant、ignoreTenantCondition、restoreTenantCondition；LogicManager提供withoutLogic及对应忽略/恢复入口；恢复操作还可用LogicRemove/LogicDeleteHandler公开API，按实体或BSON重载核对。手动上下文应在finally恢复，嵌套和同线程复用须实际验证。

聚合顶层过滤不自动约束lookup/facet/unionWith子管道。Search/Vector等有首阶段要求时，插件插入match的位置必须检查。原始Driver、无实体Document、estimatedDocumentCount、各bulk model和逻辑删除改写后的update路径不能从一次查询通过外推隔离已覆盖。类型和签名见[管理API](api/management.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C081 多租户过滤**：`TenantHandler`, `TenantInterceptor`, `TenantManager`。
- **C082 逻辑删除恢复与绕过**：`LogicRemove`, `LogicDeleteHandler`, `LogicManager`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
