# 接入、保存与读取

“在已有订单服务加一个按 ID 获取订单的方法”“能否在现有 Mapper 上直接开更新链？” 先看注入变量的**声明类型**。本页以本次盘点的 2.2.0 源码为基线，精确文件哈希见audit/source-inventory.json。

| 项目已有入口 | 可直接调用 |
|---|---|
| `MongoMapper<T>` | 实体绑定的 `save(T)`、`getById(Serializable)`、`list(Wrapper<T>)`、`updateById(T)`、`removeById(Serializable)`、`page(...)`；扩展此接口的应用 Mapper 可由 Spring `@MongoMapperScan` 扫描 |
| `IRepository<T> extends MongoMapper<T>` | 另有 `lambdaQuery()`、`lambdaUpdate()`、`lambdaAggregate()` 和绑定实体的常规索引操作；实现为 `RepositoryImpl<T>` |
| `IService<T> extends IRepository<T>` | 保留上两层能力；`ServiceImpl<T>` 继承 `RepositoryImpl<T>`。项目可把业务子类注册为 Bean |
| `BaseMapper` | 非泛型接口；有显式实体、结果类型或数据库/集合重载。已有 typed Mapper 可用 `getBaseMapper()` 与 `getGenericityClass()` 进入对应重载 |

```java
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.mapper.Mongo;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.mapper.MongoMapper;

@CollectionName("orders")
public class OrderRecord {
    @ID private String id;
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}

@Mongo
interface OrderMapper extends MongoMapper<OrderRecord> {}

class OrderReader {
    private final OrderMapper orders;
    OrderReader(OrderMapper orders) { this.orders = orders; }
    OrderRecord byId(String id) { return orders.getById(id); }
}
```

在应用的扫描包中注册该 Mapper，按需将 `OrderReader` 注册为 Bean。当前Boot 3 的入口为 `mongo-plus-boot-starter`，Boot 4 为 `mongo-plus-boot4-starter`，Solon 为 `mongo-plus-solon-plugin`。连接配置位于 `mongo-plus.data.mongodb`。先沿用项目的 BOM、扫描范围和 ID 类型；字符串业务键与 BSON ObjectId 应按实际持久化类型匹配。

批量同类实体走 `saveBatch(Collection<T>)`，先处理空集合。`one(wrapper)` 可能返回 null；有唯一性要求时还要建立唯一索引。`saveOrUpdate(entity)` 根据 ID 选择路径，传入已有业务 ID 不能保证“库中不存在就插入”；有并发唯一约束的业务先定义唯一键、冲突和重试语义。`getCollection()` 返回 Driver 集合，直接读写会跳过 Mapper 的实体转换与执行拦截。

依据 · [MongoMapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapper.java) · [IRepository](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/repository/IRepository.java) · [IService](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/service/IService.java) · [MongoMapperImpl](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapperImpl.java) · [BaseMapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/BaseMapper.java) · [MongoMapperScan](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/annotation/MongoMapperScan.java) · [官方快速上手](https://mongoplus.com/docs/started.html)
<!-- COMPLETE_COVERAGE -->
## 完整访问能力

| 需求 | 优先入口 | 语义边界 |
|---|---|---|
| save单条/批量 | MongoMapper.save/saveBatch | InsertOneOptions为正常单条入口；单条save(T,InsertManyOptions)已弃用；空批次先处理 |
| 存在则更新 | saveOrUpdate、saveOrUpdateWrapper、saveOrUpdateBatch系列 | boolean isQueryDatabase重载与默认版本分别核对；不是唯一键原子upsert保证 |
| 实体修改 | updateById/updateByColumn/updateBatchByIds | entity转换、fill与版本插件路径；不是整文档替换 |
| 条件更新 | BaseMapper.updateOne/update，绑定lambdaUpdate | 单条/多条、Options、modifiedCount布尔包装要区分 |
| 删除 | removeOne/remove、按ID/列/batch | 逻辑删除拦截器可能改为update；物理删除不靠方法名猜测 |
| 读取 | one/list/getById/getByIds/getByColumn/exist | one无值为null；不是多值唯一性验证 |
| 分页与计数 | page/pageList/count/recentPageCount | page含总数，pageList仅列表；空条件可能走估算计数；分页稳定排序 |
| 原子删除并返回旧值 | findOneAndRemove | Options、投影与排序选中的记录按重载核对，不能按普通remove布尔结果处理 |
| 原始Document | BaseMapper显式database/collection重载 | 无实体namespace；必要时UnClassCollection标记元数据，隔离规则需显式配置 |
| 原生命令 | SuperMapper.command、queryCommand | queryCommand的结果类型及返回形状按签名；DDL命令不等于普通CRUD |
| 批量WriteModel | bulkWrite | Driver Insert/Update/Delete/Replace model不同；实体转换与拦截器模型回写不能默认等价 |
| distinct / replaceOne | 聚合group或[Driver互操作](driver-interop.md) | 当前没有专用MongoPlus方法，不向BaseMapper编造方法 |

所有实体Mapper、Repository和Service最终委托非泛型BaseMapper；MongoMapper不继承BaseMapper。Document/Map/DTO结果与实体集合类型分开选择，嵌套泛型用TypeReference，不能以Class<List>保留元素类型。精确重载在[访问API](api/data-access.md)，原始BSON会绕过哪些转换见[映射](mapping.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C006 实体Mapper访问层**：`MongoMapper`, `MongoMapperImpl`。
- **C007 无绑定实体BaseMapper**：`BaseMapper`, `Mapper`, `SuperMapper`。
- **C008 Repository与Service**：`IRepository`, `RepositoryImpl`, `IService`, `ServiceImpl`。
- **C009 单条保存与ID回写**：`MongoMapper#save`。
- **C010 批量保存与saveOrUpdate**：`MongoMapper#saveBatch`, `MongoMapper#saveOrUpdate`。
- **C011 实体更新与按ID批改**：`MongoMapper#updateById`, `MongoMapper#updateBatchByIds`。
- **C012 单条与批量删除**：`BaseMapper#remove`, `BaseMapper#removeOne`。
- **C013 单条读取与存在性**：`MongoMapper#one`, `MongoMapper#exist`。
- **C014 分页与计数**：`PageParam`, `PageResult`, `BaseMapper#page`, `BaseMapper#recentPageCount`。
- **C015 批量WriteModel操作**：`BaseMapper#bulkWrite`。
- **C016 原生Document与BSON命令**：`SuperMapper#command`, `BaseMapper#queryCommand`。
- **C018 findOneAndRemove与返回删除前文档**：`BaseMapper#findOneAndRemove`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
