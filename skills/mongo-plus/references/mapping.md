# 物理字段、类型转换与写入审计时间

“Java 用驼峰，历史集合用下划线。状态存数值，每次写实体维护时间。” 将稳定的存储约定放到实体。`@CollectionName` 绑定集合，`@ID` 对应 `_id`，`@CollectionField(value=...)` 显式指定物理字段。`exist=false` 排除非持久化字段；`ignoreNull` 与业务的“清空字段”要区分（需要移除键可用更新操作 `unset`）。

```java
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.model.AutoFillMetaObject;

enum InvoiceState {
    OPEN(1), CLOSED(2);
    @EnumValue private final int code;
    InvoiceState(int code) { this.code = code; }
    public int getCode() { return code; }
}

@CollectionName("invoices")
public class Invoice {
    @ID public String id;
    public InvoiceState state;
    @CollectionField(value = "created_at", fill = FieldFill.INSERT)
    public Long createdAt;
    @CollectionField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    public Long updatedAt;
}

class AuditTimes implements MetaObjectHandler {
    public void insertFill(AutoFillMetaObject meta) {
        long now = System.currentTimeMillis();
        meta.fillValue("created_at", now);
        meta.fillValue("updated_at", now);
    }
    public void updateFill(AutoFillMetaObject meta) {
        meta.fillValue("updated_at", System.currentTimeMillis());
    }
}
```

Boot 中把 `AuditTimes` 注册为 `MetaObjectHandler` Bean；纯 Core 可用 `Configuration.metaObjectHandler(handler)`。`fillValue(String,Object)` 只填当前阶段声明的字段，示例键名与实体别名一致。用实体走 `save` 和实体更新，分别检查持久化 BSON 和回写到对象的值。INSERT 限定填充阶段；实体中已经非空的创建时间仍可能被实体更新写入。将客户端时间从可写 DTO 排除，或使用只含可改字段的更新。直接用更新操作符时，请显式 `set("updated_at", now)`，这条路径没有实体更新填充。

单字段的存储表示和读回类型不同，用 `@CollectionField(typeHandler = ...class)` 指向实现 `TypeHandler<T>` 的类，分别实现 `Object setParameter(String fieldName,T value)` 与 `T getResult(Object stored)`；返回 null 可回到框架处理。全局映射、转换或 `FieldHandler` / `ReadHandler` 的作用域更大，不适合只影响一个属性的需求。`@EnumValue` 标在枚举成员字段上，先核对现有值的 BSON 类型及未知历史值的读回策略。加密、脱敏和 DBRef 可涉及读写钩子；Wrapper、直接 BSON 和 DTO 投影要分别检验，尤其不要把脱敏后的返回值当成原值写回。需要展示时掩码可选 com.mongoplus.annotation.comm.Desensitization，手机号有 MOBILE_PHONE 类型。持久化加密需明确 FieldEncrypt 的算法与密钥来源；当前默认 BASE64 是可逆编码，不提供保密性。

依据 · [CollectionField](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java) · [EnumValue](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/EnumValue.java) · [TypeHandler](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/TypeHandler.java) · [FieldHandler](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java) · [MetaObjectHandler](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/MetaObjectHandler.java) · [AutoFillMetaObject](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/AutoFillMetaObject.java) · [DefaultAutoFillHandler](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/DefaultAutoFillHandler.java) · [官方枚举映射](https://mongoplus.com/docs/autoMappingEnum.html) · [官方自动填充](https://mongoplus.com/docs/autoFill.html)
<!-- COMPLETE_COVERAGE -->
## 完整映射能力与注册

| 场景 | 入口 | 条件与边界 |
|---|---|---|
| 元数据 | TypeInformation/FieldInformation | 继承字段、泛型、集合元素、缓存、反射可达性按当前版本 |
| 自定义主键 | IdGenerateHandler、IdentifierGenerator、IdWorker | ID策略、AUTO自增集合、UUID/ULID/雪花及ObjectId按字段类型选择 |
| 集合命名 | CollectionName、AnnotationOperate、CollectionNameHandler | 静态实体命名与动态物理路由不同 |
| 枚举存值 | EnumValue、EnumMappingStrategy/EnumConversionStrategy | 未知值、null和BSON数值宽度要有测试 |
| 单字段转换 | CollectionField.typeHandler、TypeHandler | 不把其他ORM接口导入本框架 |
| 写入全局映射 | MappingStrategy、MappingCache | 类型泛型注册；实体嵌套与Map值分开核对 |
| 读取全局转换 | ConversionStrategy、ConversionCache | Date/JSR310、BSON、数值、GeoJSON等已有默认策略 |
| 整体Converter | MongoConverter、MappingMongoConverter | writeBySave/writeByUpdate/readDocument与readInternal各入口区分 |
| 泛型结果 | TypeReference<T> | 嵌套List/Map使用真实Type，不用Class丢失泛型 |
| 扩展字段 | ExtraFields、ExtraFieldsHolder | 额外Map类型、忽略列表及命名转换 |
| 字典回写 | FieldBind、DataBindHandler、FieldBindHandler | 显式注册ReadHandler；目标字段必须存在 |
| 引用对象 | DBRef、DBRefHandler | 不承诺级联保存或无额外查询；缺引用/循环和数组引用未验证 |
| 字段安全 | FieldEncrypt、Desensitization、SensitiveWord | 读取脱敏与写入加密不同，分别路由专题 |

当前写链会把前序结果传给双参数FieldHandler，见[扩展生命周期](extensions.md)。默认DBRef/类型处理/加密的顺序和读链的activate/discontinue需要组合验证。Document/Map、实体save/update、wrapper-only和bulk不是一条相同路径。

完整映射注解见[注解页](../annotations/mapping.md)，映射/转换类、注册方法与泛型签名见[映射API](api/mapping.md)。

FieldBind读取回调的精确方法为`DataBindHandler.setMetaObject(FieldBind binding,Object source,FieldInformation target)`：binding是源字段上的注解，source是源字段当前读值，第三参数是通过target属性找到的**目标字段元数据**，应对它调用setValue写入翻译。显式注册示例（配合注解页中target="label"实体）：

```java
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.handlers.read.FieldBindHandler;

class BindStatusNames {
    static void register() {
        HandlerCache.addReadHandler(new FieldBindHandler((binding,source,target) -> {
            if ("status".equals(binding.type())) target.setValue("Name-"+source);
        }));
    }
}
```

Boot4/Solon按应用启动生命周期显式调用一次，Boot3可收集ReadHandler Bean。HandlerCache有单个ReadHandler和List<ReadHandler>两个addReadHandler重载，返回void，精确static声明在[扩展API](api/extensions.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C050 实体元数据与泛型结果**：`TypeInformation`, `FieldInformation`, `TypeReference`, `SimpleTypeHolder`。
- **C051 ID生成与ObjectId转换**：`IdGenerateHandler`, `IdentifierGenerator`, `IdWorker`, `AutoIdGenerate`。
- **C052 集合字段命名与null策略**：`AnnotationOperate`, `CollectionNameHandler`。
- **C053 枚举持久化**：`EnumMappingStrategy`, `EnumConversionStrategy`。
- **C054 TypeHandler字段双向转换**：`TypeHandler`, `TypeHandlerFieldHandler`。
- **C055 MappingStrategy写映射**：`MappingStrategy`, `MappingCache`。
- **C056 ConversionStrategy读转换**：`ConversionStrategy`, `ConversionCache`。
- **C057 MongoConverter整体替换**：`MongoConverter`, `MappingMongoConverter`。
- **C058 自动填充**：`MetaObjectHandler`, `AutoFillMetaObject`。
- **C061 DBRef引用写读查询**：`DBRefHandler`。
- **C062 ExtraFields额外字段**：`ExtraFieldsHolder`, `ExtraField`。
- **C063 FieldBind字典回写**：`DataBindHandler`, `FieldBindHandler`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
