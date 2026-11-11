# 注解：mapping

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：CollectionField、CollectionLogic、CollectionName、DBRef、EnumValue、ExtraFields、FieldBind、ID、TimeSeries、Version。

## CollectionField

`import com.mongoplus.annotation.collection.CollectionField;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：字段重命名、排除、填充、TypeHandler、null和ObjectId控制。
- 生效条件：实体Converter；fill还需要MetaObjectHandler，typeHandler需实现TypeHandler。
- 限制与常见错误：当前只有6个属性，无convertCollect；wrapper-only更新不等于实体写转换。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `String` | `""` | 字段映射值 |
| `exist` | `boolean` | `true` | 是否为数据库表字段 |
| `fill` | `FieldFill` | `FieldFill.DEFAULT` | 自动填充策略 |
| `typeHandler` | `Class<?>` | `Void.class` | 类型处理器，必须实现`com.mongoplus.handlers.TypeHandler`接口 |
| `ignoreNull` | `boolean` | `true` | 忽略null属性，默认为true |
| `isObjectId` | `boolean` | `false` | 是否是ObjectId |

```java
import com.mongoplus.annotation.collection.CollectionField;

public class CollectionFieldExample { @CollectionField(value="display_name", ignoreNull=false) String name; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java#L11)。后续实现与注册见[专题](../references/mapping.md)。

## CollectionLogic

`import com.mongoplus.annotation.collection.CollectionLogic;`

- 作用域：`@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：逻辑删除字段及未删除/已删除值类型。
- 生效条件：启用逻辑删除配置，并登记实体、普通/高级逻辑删除拦截器。
- 限制与常见错误：logice包拼写勿改；注解本身不注册插件；物理删除与忽略上下文需独立核对。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `String` | `""` | 默认逻辑未删除值（该值可无、会自动获取全局配置） |
| `delval` | `String` | `""` | 默认逻辑删除值（该值可无、会自动获取全局配置） |
| `delType` | `LogicDataType` | `LogicDataType.DEFAULT` | 逻辑删除字段值类型 |

```java
import com.mongoplus.annotation.collection.CollectionLogic;
import com.mongoplus.enums.LogicDataType;

public class CollectionLogicExample { @CollectionLogic(value="0", delval="1", delType=LogicDataType.INTEGER) Integer deleted; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionLogic.java#L12)。后续实现与注册见[专题](../references/mapping.md)。

## CollectionName

`import com.mongoplus.annotation.collection.CollectionName;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：指定物理集合及可选database。
- 生效条件：实体访问或扫描元数据；Core也可显式选择namespace。
- 限制与常见错误：value无默认值；database空值走当前配置；动态集合另由插件处理。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `String` | `必填（无默认值）` | 标记属性 |
| `database` | `String` | `""` | 选择数据库，可以写配置文件中没有的 |

```java
import com.mongoplus.annotation.collection.CollectionName;

@CollectionName("orders") public class CollectionNameExample { String title; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionName.java#L9)。后续实现与注册见[专题](../references/mapping.md)。

## DBRef

`import com.mongoplus.annotation.collection.DBRef;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：将关联对象写为Driver DBRef并在读取时查询引用对象。
- 生效条件：默认DBRefHandler参与写、读、条件链；被引用类型必须有@ID与可解析集合。
- 限制与常见错误：不是自动级联保存、join或外键；可能额外查询；循环、缺引用、集合引用运行契约UNVERIFIED。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `db` | `String` | `""` | 指定数据库，优先级比CollectionName的database高 |
| `autoConvertObjectId` | `boolean` | `true` | 是否自动转换ObjectId |

```java
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.DBRef;

public class DBRefExample { @DBRef Target customer; @CollectionName("customers") public static class Target { @ID public String id; } }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/DBRef.java#L9)。后续实现与注册见[专题](../references/mapping.md)。

## EnumValue

`import com.mongoplus.annotation.comm.EnumValue;`

- 作用域：`@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：选择枚举持久化字段，valueStore控制存值或名称。
- 生效条件：枚举MappingStrategy与ConversionStrategy配合实体转换。
- 限制与常见错误：标在枚举中的值字段；数据库值与枚举字段类型应一致；未知值行为需测试。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `valueStore` | `boolean` | `true` | 是否以此注解标注的字段的值入库，默认true |

```java
import com.mongoplus.annotation.comm.EnumValue;

public enum EnumValueExample { OPEN(1); @EnumValue final int code; EnumValueExample(int code){this.code=code;} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/EnumValue.java#L9)。后续实现与注册见[专题](../references/mapping.md)。

## ExtraFields

`import com.mongoplus.annotation.collection.ExtraFields;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：将文档中未声明属性收集到Map，支持忽略列表及名称转换。
- 生效条件：使用实体读取Converter及ExtraFieldsHolder。
- 限制与常见错误：Map实现须可实例化；不是任意字段自动持久化契约；忽略_id及往返行为需实测。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `ignoredFields` | `String[]` | `{}` | 忽略的字段 |
| `mapType` | `Class<? extends Map>` | `LinkedHashMap.class` | map类型,如果不主动传值，那么将会根据类型自动创建，如JSONObject 请传递类，而不是接口 |
| `underlineToCamel` | `boolean` | `true` | 下划线转驼峰 |

```java
import com.mongoplus.annotation.collection.ExtraFields;

public class ExtraFieldsExample { @ExtraFields java.util.Map<String,Object> extra; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/ExtraFields.java#L11)。后续实现与注册见[专题](../references/mapping.md)。

## FieldBind

`import com.mongoplus.annotation.collection.FieldBind;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：读取源字段后回调字典处理器，为target字段赋值。
- 生效条件：显式注册new FieldBindHandler(DataBindHandler)到ReadHandler链；Boot3可收集ReadHandler Bean。
- 限制与常见错误：type是业务字典标识；目标找不到会异常；默认命名配置下target="statusName"实测抛出target field not found，命名契约标CONTRACT_UNRESOLVED，示例使用已实测的label；Boot4需显式注册ReadHandler。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `type` | `String` | `必填（无默认值）` | 类型（用于区分不同业务） |
| `target` | `String` | `必填（无默认值）` | 目标显示属性（待绑定属性，注意非数据库字段请排除） |

```java
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.annotation.collection.FieldBind;

public class FieldBindExample { @FieldBind(type="status", target="label") String status; @CollectionField(exist=false) String label; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/FieldBind.java#L8)。后续实现与注册见[专题](../references/mapping.md)。

## ID

`import com.mongoplus.annotation.ID;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：映射MongoDB _id并选择生成策略；saveField控制是否同时保留原属性名。
- 生效条件：通过MongoConverter实体写入；自定义策略注册IdGenerateHandler/IdentifierGenerator。
- 限制与常见错误：默认OBJECT_ID；String/ObjectId/数值策略与Java字段类型必须相容；不把业务ID都当ObjectId。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `type` | `IdTypeEnum` | `IdTypeEnum.OBJECT_ID` | 自动生成id配置 |
| `saveField` | `boolean` | `false` | 是否将该字段存入库中 注意，查询时还是会根据_id进行查询 |

```java
import com.mongoplus.annotation.ID;

public class IDExample { @ID String id; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/ID.java#L12)。后续实现与注册见[专题](../references/mapping.md)。

## TimeSeries

`import com.mongoplus.annotation.collection.TimeSeries;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：声明时序集合创建选项。
- 生效条件：启用auto-create-time-series并扫描到实体，或调用AutoUtil.autoCreateTimeSeries。
- 限制与常见错误：timeField必填且应为BSON Date；bucket参数单位秒；已有普通集合不自动迁移；创建捕获MongoCommandException不能当作成功。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `dataSource` | `String` | `""` | 数据源，指定时间序列所在的数据源 |
| `timeField` | `String` | `必填（无默认值）` | 实际字段名；以$开头时经实体属性核对/映射。注解应写timeField/metaField成员，不存在value成员。源码旧注释的@TimeSeries("...")简写无效。 |
| `metaField` | `String` | `""` | 实际字段名；以$开头时经实体属性核对/映射。注解应写timeField/metaField成员，不存在value成员。源码旧注释的@TimeSeries("...")简写无效。 |
| `granularity` | `TimeSeriesGranularity` | `TimeSeriesGranularity.SECONDS` | 指定时间序列的粒度 |
| `bucketMaxSpan` | `long` | `-1` | 设置存储桶中测量之间的最大时间跨度，以秒为单位 |
| `bucketRounding` | `long` | `-1` | 指定确定新存储桶的起始时间戳的时间间隔，以秒为单位 |
| `expireAfter` | `long` | `-1` | 指定文档过期的秒数 |

```java
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.TimeSeries;

@CollectionName("measurements") @TimeSeries(timeField="time", metaField="sensor") public class TimeSeriesExample { java.util.Date time; String sensor; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java#L11)。后续实现与注册见[专题](../references/mapping.md)。

## Version

`import com.mongoplus.annotation.collection.Version;`

- 作用域：`@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：标记实体整数版本字段，供乐观锁拦截器解析。
- 生效条件：注册OptimisticLockerInterceptor，走可识别实体更新入口。
- 限制与常见错误：仅支持整数；空版本、自动递增、失败异常与重试配置分别核对；原生BSON和wrapper-only不自动获得全部契约。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.collection.Version;

public class VersionExample { @Version Integer version; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/Version.java#L10)。后续实现与注册见[专题](../references/mapping.md)。
