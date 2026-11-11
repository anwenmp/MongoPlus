# 原子写、版本冲突与批量修改

“只有待处理的订单才允许抢单”“两人打开同一单据，旧页面保存时应提示冲突。” 先确定要比较的旧值。抢单比较原状态，整份编辑比较客户端读取的版本；库存扣减比较库存下限并在同一次写中递减。用更新条件和更新操作组成一条命令。单纯给计数器加数可直接 `inc`。

```java
import com.mongoplus.conditions.update.UpdateWrapper;
import com.mongoplus.mapper.MongoMapper;
import com.mongoplus.repository.IRepository;

class OrderWrites {
    static <T> boolean claim(MongoMapper<T> orders, String id, String worker) {
        UpdateWrapper<T> change = new UpdateWrapper<T>()
            .eq("_id", id).eq("state", "OPEN")
            .set("state", "CLAIMED").set("worker", worker);
        return orders.getBaseMapper().updateOne(change, orders.getGenericityClass(), null);
    }

    static <T> boolean edit(IRepository<T> orders, String id, int seen, String title) {
        return orders.lambdaUpdate().eq("_id", id).eq("revision", seen)
            .set("title", title).inc("revision", 1).updateOne();
    }
}
```

这里 `revision` 是应用自己维护的整数，没有启用对该字段的版本插件。期望的原生更新为 `updateOne({_id:id, revision:seen}, {$set:{title:title}, $inc:{revision:1}})`。同一旧版本提交两次应只有一次生效，并保持其他字段原值。`BaseMapper.updateOne(UpdateChainWrapper<?,?>, Class<?>, UpdateOptions)` 和有绑定的更新链可限定单条。普通 `MongoMapper.update(wrapper)`、链上 `update()` 可能修改多条，用于批量操作时先核对筛选范围。包装后的布尔值取决于实际修改；普通同值写可能为 false，不能普遍解释为版本冲突。上例让每次成功都有状态或版本变化。

### 框架管理实体版本

若项目使用标准实体编辑流，可给字段加 `com.mongoplus.annotation.collection.Version`，注册 `OptimisticLockerInterceptor` 为高级拦截器。在 Boot 中可暴露一个 `OptimisticLockerInterceptor` Bean。如下在已注册、已识别的实体集合上提交**用户读到的旧版本**。调用时使用与库中一致的 ID 类型。

```java
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.Version;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.mapper.BaseMapper;

@CollectionName("drafts")
public class Draft {
    @ID public String id;
    @Version public Integer version;
    public String title;

    boolean saveEdit(BaseMapper mapper) {
        if (id == null || version == null) throw new IllegalArgumentException("ID and version required");
        return mapper.updateOne(this, new QueryWrapper<Draft>().eq("_id", id), null);
    }
}
```

本版本插件从更新的 `$set` 读取整数版本，将旧值移入过滤条件，用 `$inc` 推进版本。插入时可把缺失的版本初始化为 0。实体及其无参构造器须能被框架反射访问，示例将 Draft 声明为 public。缺失旧版本时，默认配置可跳过补版本谓词，业务入口应拒绝缺失版本，或配置 setVersionIsNullException。显式处理失败或配置的异常，重新读取、比较差异，让业务决定合并。不要在提交前把旧版本替换成最新值后原样重试。Wrapper-only、原始 BSON、不同 bulk model 是否受插件保护要逐条核验；手写 CAS 与插件不要同时管理同一字段。版本校验解决旧快照覆盖；同一业务请求重复扣款还要请求唯一键/幂等记录。多文档全成全败见 [事务与路由](transactions-routing.md)。

依据 · [UpdateWrapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/update/UpdateWrapper.java) · [LambdaUpdateChainWrapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/update/LambdaUpdateChainWrapper.java) · [AbstractBaseMapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/AbstractBaseMapper.java) · [ChainUpdate](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/update/ChainUpdate.java) · [OptimisticLockerInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java) · [Version](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/Version.java) · [官方乐观锁](https://mongoplus.com/docs/optimisticLocker.html)
<!-- COMPLETE_COVERAGE -->
## 更新操作与执行保障

| 业务意图 | MongoPlus操作 | 特别边界 |
|---|---|---|
| 设置/清空/移除/更名 | set、unset、rename | set null和unset不同；实体ignoreNull会影响实体更新 |
| 当前日期 | currentDate | Date/Timestamp按CurrentDateType和重载检查 |
| 原子数值 | inc/mul/min/max | 上下限写在同一次filter里；$min/$max不是Java先读后算 |
| 数组追加/去重 | push/addToSet，ConditionOperators.each系列、PushOptions | each/position/slice/sort值体形状不同；两个PushOptions包名有兼容入口 |
| 数组弹出/删除 | pop/pull/pullAll | pull值与条件wrapper不互推；pullAll是多个精确元素而非任意谓词 |
| 自定义更新体 | updateCustom | 明确更新运算符文档，不能把pipeline更新数组冒充普通update文档 |
| 乐观锁 | @Version + OptimisticLockerInterceptor | 实体版本读取、空版本异常、失败异常、autoInc、enableRetry逐项配置 |
| 重试结果 | Retry/UpdateRetryResult | 自动重试不能掩盖用户旧快照冲突，也不替代业务幂等 |
| 防空条件 | BlockAttackInnerListener、block-attack-inner | Driver命令监听边界；不等于细粒度授权；启用后实测空filter update/delete |

乐观锁从实体元数据和更新内容识别版本，Wrapper-only、bulk原始model、直接Driver分别验证。空条件保护默认关闭；配置入口见[配置](configuration.md)，不要在Skill里悄悄给所有应用开启插件。精确重载在[更新API](api/update.md)。

真实库已验证多条件pull的两种写法。优先使用`pull(String,Object)`把目标数组路径与元素条件体分开；`pull(Wrapper<?>)`需要整个`{items:元素条件体}`形状，可用custom保留结构。把整个元素条件放进`eq("items",...)`会生成`items:{$eq:...}`，含$gte的文档将被当作相等值，不能实现下例谓词。

```java
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;
import org.bson.Document;

class RemoveQualifiedItems {
    static UpdateWrapper<Object> byField() {
        Document element = new Document("sku","X").append("qty",new Document("$gte",2));
        return new UpdateWrapper<Object>().eq("_id",2).pull("items",element);
    }
    static UpdateWrapper<Object> byWrapper() {
        Document element = new Document("sku","X").append("qty",new Document("$gte",2));
        QueryWrapper<Object> body = new QueryWrapper<Object>().custom(new Document("items",element));
        return new UpdateWrapper<Object>().eq("_id",2).pull(body);
    }
}
```

两者实发`u:{$pull:{items:{sku:"X",qty:{$gte:2}}}}`；原生对照updateOne采用同一$pull体。夹具中的qty=3元素被移除，其他文档/元素保留；完整命令与结果见[实测清单](../audit/runtime-results.md)。此结论仅覆盖这两个具体重载。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C030 更新Wrapper与绑定更新链**：`UpdateWrapper`, `LambdaUpdateChainWrapper`。
- **C031 set-unset-rename-currentDate**：`Set`, `Unset`, `Rename`, `CurrentDate`。
- **C032 inc-mul-min-max与原子更新**：`Inc`, `Mul`, `Min`, `Max`。
- **C033 push-addToSet-pop-pull-pullAll**：`Push`, `AddToSet`, `Pop`, `Pull`, `PullAll`, `PushOptions`。
- **C034 updateOne-updateMany与Options**：`BaseMapper#updateOne`, `BaseMapper#update`。
- **C035 乐观锁版本与重试**：`OptimisticLockerInterceptor`, `Retry`, `UpdateRetryResult`。
- **C036 空条件更新删除保护**：`BlockAttackInnerListener`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
