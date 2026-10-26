# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 200 条完整源码记录（含内部实现）。

## 模块：mongo-plus-annotation

### 包：com.mongoplus.annotation

#### annotation com.mongoplus.annotation.ID

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/ID.java:12-33

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* @author JiaChaoYang
 * 标识这是MongoDB的ID属性
 * @since 2023-02-13 11:54
 *
```

##### method type

```java
IdTypeEnum type()
```

所属类型：com.mongoplus.annotation.ID；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/ID.java:24-24

```text
* 自动生成id配置
     * @author JiaChaoYang
```

##### method saveField

```java
boolean saveField()
```

所属类型：com.mongoplus.annotation.ID；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/ID.java:31-31

```text
* 是否将该字段存入库中
     * <p style="color: red">注意，查询时还是会根据_id进行查询</p>
     * @author JiaChaoYang
```

### 包：com.mongoplus.annotation.collection

#### annotation com.mongoplus.annotation.collection.CollectionField

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:11-55

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 指定列名，不指定默认取属性名
 * @author JiaChaoYang
 *
```

##### method value

```java
String value()
```

所属类型：com.mongoplus.annotation.collection.CollectionField；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:22-22

```text
* 字段映射值
     * @author JiaChaoYang
```

##### method exist

```java
boolean exist()
```

所属类型：com.mongoplus.annotation.collection.CollectionField；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:28-28

```text
* 是否为数据库表字段
     * @author JiaChaoYang
```

##### method fill

```java
FieldFill fill()
```

所属类型：com.mongoplus.annotation.collection.CollectionField；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:34-34

```text
* 自动填充策略
     * @author JiaChaoYang
```

##### method typeHandler

```java
Class<?> typeHandler()
```

所属类型：com.mongoplus.annotation.collection.CollectionField；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:40-40

```text
* 类型处理器，必须实现{@link com.mongoplus.handlers.TypeHandler}接口
     * @author anwen
```

##### method ignoreNull

```java
boolean ignoreNull()
```

所属类型：com.mongoplus.annotation.collection.CollectionField；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:47-47

```text
* 忽略null属性，默认为true
     * @return {@link boolean}
     * @author anwen
```

##### method isObjectId

```java
boolean isObjectId()
```

所属类型：com.mongoplus.annotation.collection.CollectionField；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionField.java:53-53

```text
* 是否是ObjectId
     * @author anwen
```

#### annotation com.mongoplus.annotation.collection.CollectionLogic

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionLogic.java:12-34

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
```

```text
* 表字段逻辑处理注解（逻辑删除）
 *
 * @author loser
```

##### method value

```java
String value()
```

所属类型：com.mongoplus.annotation.collection.CollectionLogic；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionLogic.java:20-20

```text
* 默认逻辑未删除值（该值可无、会自动获取全局配置）
```

##### method delval

```java
String delval()
```

所属类型：com.mongoplus.annotation.collection.CollectionLogic；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionLogic.java:25-25

```text
* 默认逻辑删除值（该值可无、会自动获取全局配置）
```

##### method delType

```java
LogicDataType delType()
```

所属类型：com.mongoplus.annotation.collection.CollectionLogic；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionLogic.java:32-32

```text
* 逻辑删除字段值类型
     * @return {@link Class}
     * @author anwen
```

#### annotation com.mongoplus.annotation.collection.CollectionName

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionName.java:9-23

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 指定表名，不使用此注解默认取实体类名
 * @author anwen
```

##### method value

```java
String value()
```

所属类型：com.mongoplus.annotation.collection.CollectionName；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionName.java:16-16

##### method database

```java
String database()
```

所属类型：com.mongoplus.annotation.collection.CollectionName；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/CollectionName.java:22-22

```text
* 选择数据库，可以写配置文件中没有的
     * @author JiaChaoYang
```

#### annotation com.mongoplus.annotation.collection.DBRef

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/DBRef.java:9-28

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* DBRef注解
 * @author anwen
```

##### method db

```java
String db()
```

所属类型：com.mongoplus.annotation.collection.DBRef；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/DBRef.java:19-19

```text
* 指定数据库，优先级比CollectionName的database高
     * @return {@link String}
     * @author anwen
```

##### method autoConvertObjectId

```java
boolean autoConvertObjectId()
```

所属类型：com.mongoplus.annotation.collection.DBRef；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/DBRef.java:26-26

```text
* 是否自动转换ObjectId
     * @return {@link boolean}
     * @author anwen
```

#### annotation com.mongoplus.annotation.collection.ExtraFields

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/ExtraFields.java:11-32

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 集合中的额外字段
 * @author anwen
```

##### method ignoredFields

```java
String[] ignoredFields()
```

所属类型：com.mongoplus.annotation.collection.ExtraFields；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/ExtraFields.java:19-19

```text
* 忽略的字段
```

##### method mapType

```java
Class<? extends Map> mapType()
```

所属类型：com.mongoplus.annotation.collection.ExtraFields；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/ExtraFields.java:25-25

```text
* map类型,如果不主动传值，那么将会根据类型自动创建，如JSONObject
     * <p style='color:red'>请传递类，而不是接口</p>
```

##### method underlineToCamel

```java
boolean underlineToCamel()
```

所属类型：com.mongoplus.annotation.collection.ExtraFields；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/ExtraFields.java:30-30

```text
* 下划线转驼峰
```

#### annotation com.mongoplus.annotation.collection.FieldBind

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/FieldBind.java:8-23

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 字段数据绑定（字典回写）
```

##### method type

```java
String type()
```

所属类型：com.mongoplus.annotation.collection.FieldBind；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/FieldBind.java:16-16

```text
* 类型（用于区分不同业务）
```

##### method target

```java
String target()
```

所属类型：com.mongoplus.annotation.collection.FieldBind；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/FieldBind.java:21-21

```text
* 目标显示属性（待绑定属性，注意非数据库字段请排除）
```

#### annotation com.mongoplus.annotation.collection.TimeSeries

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:11-59

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 指定为时间序列集合
 * @author anwen
```

##### method dataSource

```java
String dataSource()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:19-19

```text
* 数据源，指定时间序列所在的数据源
```

##### method timeField

```java
String timeField()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:28-28

```text
* 包含每个时间序列文档中的日期的字段的名称
     * <p>可使用{@code $}标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如：</p>
     * <p>
     *      {@code @TimeSeries("$field1"})}
     * </p>
```

##### method metaField

```java
String metaField()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:37-37

```text
* 包含每个时间序列文档中的元数据的字段的名称
     * <p>可使用{@code $}标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如：</p>
     * <p>
     *      {@code @TimeSeries(metaField="$field1"})}
     * </p>
```

##### method granularity

```java
TimeSeriesGranularity granularity()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:42-42

```text
* 指定时间序列的粒度
```

##### method bucketMaxSpan

```java
long bucketMaxSpan()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:47-47

```text
* 设置存储桶中测量之间的最大时间跨度，以秒为单位
```

##### method bucketRounding

```java
long bucketRounding()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:52-52

```text
* 指定确定新存储桶的起始时间戳的时间间隔，以秒为单位
```

##### method expireAfter

```java
long expireAfter()
```

所属类型：com.mongoplus.annotation.collection.TimeSeries；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/TimeSeries.java:57-57

```text
* 指定文档过期的秒数
```

#### annotation com.mongoplus.annotation.collection.Version

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/collection/Version.java:10-14

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
```

```text
* 乐观锁注解
 * <p>仅支持整数类型</p>
 * @author anwen
```

### 包：com.mongoplus.annotation.comm

#### annotation com.mongoplus.annotation.comm.Desensitization

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Desensitization.java:12-51

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 数据脱敏注解
 *
 * @author anwen
```

##### method type

```java
DesensitizationTypeEnum type()
```

所属类型：com.mongoplus.annotation.comm.Desensitization；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Desensitization.java:25-25

```text
* 脱敏类型
     * @return {@link DesensitizationTypeEnum}
     * @author anwen
```

##### method startInclude

```java
int startInclude()
```

所属类型：com.mongoplus.annotation.comm.Desensitization；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Desensitization.java:33-33

```text
* 脱敏开始位置(包含)，{@link #type()}为{@link DesensitizationTypeEnum#CUSTOM}并且
     * {@link #desensitizationHandler()}为{@link Void}时，会使用这里指定的位置处理
     * @return {@link int}
     * @author anwen
```

##### method endExclude

```java
int endExclude()
```

所属类型：com.mongoplus.annotation.comm.Desensitization；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Desensitization.java:41-41

```text
* 脱敏结束位置(不包含)，{@link #type()}为{@link DesensitizationTypeEnum#CUSTOM}并且
     * {@link #desensitizationHandler()}为{@link Void}时，会使用这里指定的位置处理
     * @return {@link int}
     * @author anwen
```

##### method desensitizationHandler

```java
Class<?> desensitizationHandler()
```

所属类型：com.mongoplus.annotation.comm.Desensitization；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Desensitization.java:49-49

```text
* 脱敏处理器，{@link #type()}为{@link DesensitizationTypeEnum#CUSTOM}时，会走这里配置的处理器
     * <p>脱敏处理器需实现{@link com.mongoplus.handlers.DesensitizationHandler}接口</p>
     * @return {@link Class<?>}
     * @author anwen
```

#### annotation com.mongoplus.annotation.comm.EnumValue

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/EnumValue.java:9-21

```java
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
```

```text
* 枚举映射
 * @author anwen
```

##### method valueStore

```java
boolean valueStore()
```

所属类型：com.mongoplus.annotation.comm.EnumValue；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/EnumValue.java:19-19

```text
* 是否以此注解标注的字段的值入库，默认true
     * @return {@link boolean}
     * @author anwen
```

#### annotation com.mongoplus.annotation.comm.FieldEncrypt

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:11-57

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 字段加密
 * @author anwen
```

##### method algorithm

```java
AlgorithmEnum algorithm()
```

所属类型：com.mongoplus.annotation.comm.FieldEncrypt；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:24-24

```text
* 加密类型
     * @return {@link com.mongoplus.enums.AlgorithmEnum}
     * @author anwen
```

##### method findDecrypt

```java
boolean findDecrypt()
```

所属类型：com.mongoplus.annotation.comm.FieldEncrypt；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:31-31

```text
* 查询是否解密
     * 如果设置为false，那么在查询时不会进行解密
     * @author anwen
```

##### method key

```java
String key()
```

所属类型：com.mongoplus.annotation.comm.FieldEncrypt；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:37-37

```text
* 秘钥，优先于全局参数
     * @author anwen
```

##### method privateKey

```java
String privateKey()
```

所属类型：com.mongoplus.annotation.comm.FieldEncrypt；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:43-43

```text
* 私钥，优先于全局参数
     * @author anwen
```

##### method publicKey

```java
String publicKey()
```

所属类型：com.mongoplus.annotation.comm.FieldEncrypt；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:49-49

```text
* 公钥，优先于全局参数
     * @author anwen
```

##### method encryptor

```java
Class<?> encryptor()
```

所属类型：com.mongoplus.annotation.comm.FieldEncrypt；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java:55-55

```text
* 加密处理器，需实现{@link com.mongoplus.encryptor.Encryptor}接口
     * @author anwen
```

#### annotation com.mongoplus.annotation.comm.NonNull

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java:10-16

```java
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull
@TypeQualifierNickname
```

```text
* 标注不可以为null
```

#### annotation com.mongoplus.annotation.comm.Nullable

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Nullable.java:11-17

```java
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Nonnull(when = When.MAYBE)
@TypeQualifierNickname
```

```text
* 标注可以为null
```

### 包：com.mongoplus.annotation.datasource

#### annotation com.mongoplus.annotation.datasource.MongoDs

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/datasource/MongoDs.java:10-30

```java
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 多数据源注解
 *
 * @author JiaChaoYang
 *
```

##### method value

```java
String value()
```

所属类型：com.mongoplus.annotation.datasource.MongoDs；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/datasource/MongoDs.java:22-22

```text
* 数据源名称
     * @author JiaChaoYang
```

##### method dsHandler

```java
Class<?> dsHandler()
```

所属类型：com.mongoplus.annotation.datasource.MongoDs；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/datasource/MongoDs.java:28-28

```text
* 数据源处理类，需要实现{@link com.mongoplus.handlers.DataSourceHandler}接口
     * @author JiaChaoYang
```

### 包：com.mongoplus.annotation.index

#### annotation com.mongoplus.annotation.index.MongoCompoundIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:9-62

```java
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MongoCompoundIndexes.class)
```

```text
* 注解式复合索引自动创建
 * @author anwen
```

##### method value

```java
String value()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:26-26

```text
* 复合索引值，这里需要传入json格式，例：
     * <p>
     *     {@code @CompoundIndex("{'field1':1,'field2':-1}")}
     * </p>
     * 同时也可使用{@code $}标识，该操作将会根据json值的key查找类中的该字段，不存在则直接使用json的key，例:
     * <p>
     *      {@code @CompoundIndex("{'$field1':1,'$field2':-1}")}
     * </p>
     * @author anwen
```

##### method name

```java
String name()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:32-32

```text
* 索引名称
     * @author anwen
```

##### method unique

```java
boolean unique()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:38-38

```text
* 唯一复合索引
     * @author anwen
```

##### method sparse

```java
boolean sparse()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:44-44

```text
* 稀疏复合索引
     * @author anwen
```

##### method partialFilterExpression

```java
String partialFilterExpression()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:53-53

```text
* 部分索引，和{@link MongoIndex#partialFilterExpression()}不同，该属性需手动指定字段
     * <p>例：{"field1" : {"$gt",5}}</p>
     * 同时也可使用{@code $}标识，该操作将会根据json值的key查找类中的该字段，不存在则直接使用json的key，例:
     * <p>例：{"$field1" : {"$gt",5}}</p>
     * @author anwen
```

##### method background

```java
boolean background()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndex.java:59-59

```text
* 是否应该在后台创建索引
     * @author anwen
```

#### annotation com.mongoplus.annotation.index.MongoCompoundIndexes

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndexes.java:10-17

```java
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
```

```text
* 多个复合索引
 *
 * @author anwen
```

##### method value

```java
MongoCompoundIndex[] value()
```

所属类型：com.mongoplus.annotation.index.MongoCompoundIndexes；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoCompoundIndexes.java:15-15

#### annotation com.mongoplus.annotation.index.MongoGeoIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoGeoIndex.java:11-26

```java
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 地理位置索引
 * @author anwen
```

##### method type

```java
GeoType type()
```

所属类型：com.mongoplus.annotation.index.MongoGeoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoGeoIndex.java:24-24

```text
* 索引类型
     * @return {@link com.mongoplus.enums.GeoType}
     * @author anwen
```

#### annotation com.mongoplus.annotation.index.MongoHashIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoHashIndex.java:9-16

```java
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 注解式哈希索引自动创建
 * @author anwen
```

#### annotation com.mongoplus.annotation.index.MongoIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:11-78

```java
@Target({ ElementType.ANNOTATION_TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 注解式索引自动创建
 * @author anwen
```

##### method name

```java
String name()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:23-23

```text
* 索引名称
     * @author anwen
```

##### method unique

```java
boolean unique()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:29-29

```text
* 唯一索引
     * @author anwen
```

##### method direction

```java
IndexDirection direction()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:35-35

```text
* 索引排序方向
     * @author anwen
```

##### method sparse

```java
boolean sparse()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:41-41

```text
* 稀疏索引
     * @author anwen
```

##### method expireAfterSeconds

```java
long expireAfterSeconds()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:47-47

```text
* 索引过期时间，以秒为单位
     * @author anwen
```

##### method expireAfter

```java
String expireAfter()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:63-63

```text
* 自定义类型的索引过期时间
     * <ul>
     * <li><b>d</b>: 天</li>
     * <li><b>h</b>: 时</li>
     * <li><b>m</b>: 分</li>
     * <li><b>s</b>: 秒</li>
     * </ul>
     * <p>{@code @MongoIndex(expireAfter = "1d")}</p>
     * <p>{@code @MongoIndex(expireAfter = "3h")}</p>
     * <p>{@code @MongoIndex(expireAfter = "30m")}</p>
     * <p>{@code @MongoIndex(expireAfter = "30s")}</p>
     * @author anwen
```

##### method partialFilterExpression

```java
String partialFilterExpression()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:70-70

```text
* 部分索引
     * <p>例：{"$gt",5}</p>
     * @author anwen
```

##### method background

```java
boolean background()
```

所属类型：com.mongoplus.annotation.index.MongoIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndex.java:76-76

```text
* 是否应该在后台创建索引
     * @author anwen
```

#### annotation com.mongoplus.annotation.index.MongoIndexDs

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndexDs.java:9-20

```java
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
```

```text
* 用来标识自动创建的索引应该在哪个数据源
 * @author anwen
```

##### method dataSource

```java
String dataSource()
```

所属类型：com.mongoplus.annotation.index.MongoIndexDs；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoIndexDs.java:18-18

```text
* 数据源名称
     * @author anwen
```

#### annotation com.mongoplus.annotation.index.search.MongoSearchIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoSearchIndex.java:9-16

```java
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
```

```text
* 搜索索引自动创建
 * @author anwen
```

##### method name

```java
String name()
```

所属类型：com.mongoplus.annotation.index.search.MongoSearchIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoSearchIndex.java:14-14

#### annotation com.mongoplus.annotation.index.MongoTextIndex

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoTextIndex.java:11-45

```java
@Target({ ElementType.TYPE })
@Documented
@Retention(RetentionPolicy.RUNTIME)
```

```text
* 文本索引
 * @author anwen
```

##### method fields

```java
String[] fields()
```

所属类型：com.mongoplus.annotation.index.MongoTextIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoTextIndex.java:25-25

```text
* 索引字段，传入多个值即为复合文本索引
     * <p>可使用{@code $}标识，该操作将会字段值查找类中的该字段，不存在则直接返回该值，如：</p>
     * <p>
     *      {@code @MongoTextIndex({"field1","$field2"})}
     * </p>
     * <p>通配符文本索引直接传入$**即可</p>
     * @author anwen
```

##### method name

```java
String name()
```

所属类型：com.mongoplus.annotation.index.MongoTextIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoTextIndex.java:31-31

```text
* 索引名称
     * @author anwen
```

##### method language

```java
TextLanguages language()
```

所属类型：com.mongoplus.annotation.index.MongoTextIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoTextIndex.java:37-37

```text
* 文本语言，默认英语
     * @author anwen
```

##### method textIndexVersion

```java
int textIndexVersion()
```

所属类型：com.mongoplus.annotation.index.MongoTextIndex；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/index/MongoTextIndex.java:43-43

```text
* 尽可能始终使用默认索引版本。 仅在出于兼容性原因需要时才覆盖默认版本
     * @author anwen
```

### 包：com.mongoplus.annotation.logice

#### annotation com.mongoplus.annotation.logice.IgnoreLogic

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/logice/IgnoreLogic.java:6-14

```java
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

### 包：com.mongoplus.annotation.mapper

#### annotation com.mongoplus.annotation.mapper.Mongo

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/mapper/Mongo.java:8-12

```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* @author anwen
```

### 包：com.mongoplus.annotation.tenant

#### annotation com.mongoplus.annotation.tenant.IgnoreTenant

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/tenant/IgnoreTenant.java:10-18

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 忽略多租户注解，优先级高
 *
 * @author anwen
```

### 包：com.mongoplus.annotation.transactional

#### annotation com.mongoplus.annotation.transactional.MongoReadPreference

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoReadPreference.java:13-34

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* 读取偏好设置
 *
 * @author loser
```

##### method preferenceEnum

```java
ReadPreferenceEnum preferenceEnum()
```

所属类型：com.mongoplus.annotation.transactional.MongoReadPreference；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoReadPreference.java:21-21

```text
* 一种读取偏好设置，它指定所有的读操作都应该由哪个处理
```

##### method maxStaleness

```java
long maxStaleness()
```

所属类型：com.mongoplus.annotation.transactional.MongoReadPreference；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoReadPreference.java:27-27

```text
* maxStaleness选项是用来设置最大过时容忍度的。
     * 这个选项在配置复制集的读取偏好时非常有用，它允许客户端读取已经过时的（不同步的）副本集成员的数据。maxStaleness的值越大，客户端可以容忍的过时时间就越长
```

##### method timeUnit

```java
TimeUnit timeUnit()
```

所属类型：com.mongoplus.annotation.transactional.MongoReadPreference；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoReadPreference.java:32-32

```text
* 时间单位
```

#### annotation com.mongoplus.annotation.transactional.MongoTransactional

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:14-84

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
```

```text
* Mongo事务注解
 *
 * @author JiaChaoYang
 *
```

##### method causallyConsistent

```java
boolean causallyConsistent()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:29-29

```text
* 使用会话的操作是否应该彼此一致。
     *
     * @author JiaChaoYang
```

##### method snapshot

```java
boolean snapshot()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:36-36

```text
* 使用此会话的读取操作是否应全部共享同一个快照。
     *
     * @author JiaChaoYang
```

##### method readConcern

```java
ReadConcernEnum readConcern()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:45-45

```text
* 设置一致性读策略
     *
     * @author JiaChaoYang
```

##### method writeConcern

```java
WriteConcernEnum writeConcern()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:52-52

```text
* 设置写入安全的级别
     *
     * @author anwen
```

##### method maxCommitTimeMS

```java
long maxCommitTimeMS()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:57-57

```text
* 用于指定事务提交的最大时间限制以防止因为某些原因导致事务长时间无法提交。如果事务在指定的时间内没有提交，MongODB 会自动中止 (abort) 事务。
```

##### method timeUnit

```java
TimeUnit timeUnit()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:62-62

```text
* 时间单位
```

##### method preference

```java
MongoReadPreference[] preference()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:68-68

```text
* 长度为1则有效
     * 配置读取偏好设置
```

##### method rollbackFor

```java
Class<? extends Throwable>[] rollbackFor()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:75-75

```text
* 捕获该异常并进行回滚
     *
     * @return 需要回滚的异常
```

##### method noRollbackFor

```java
Class<? extends Throwable>[] noRollbackFor()
```

所属类型：com.mongoplus.annotation.transactional.MongoTransactional；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java:82-82

```text
* 捕获该异常不进行回滚
     *
     * @return 不需要回滚的异常
```

### 包：com.mongoplus.enums

#### enum com.mongoplus.enums.AggregateEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateEnum.java:8-59

```text
* 管道枚举
 *
 * @author anwen
```

##### constructor AggregateEnum

```java
AggregateEnum(String value)
```

所属类型：com.mongoplus.enums.AggregateEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateEnum.java:50-52

##### field value

```java
private final String value
```

所属类型：com.mongoplus.enums.AggregateEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateEnum.java:54-54

##### method getValue

```java
public String getValue()
```

所属类型：com.mongoplus.enums.AggregateEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateEnum.java:56-58

#### enum com.mongoplus.enums.AggregateOptionsEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateOptionsEnum.java:8-53

```text
* @author JiaChaoYang
 *
```

##### field options

```java
private final String options
```

所属类型：com.mongoplus.enums.AggregateOptionsEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateOptionsEnum.java:34-34

##### method getOptions

```java
public String getOptions()
```

所属类型：com.mongoplus.enums.AggregateOptionsEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateOptionsEnum.java:36-38

##### constructor AggregateOptionsEnum

```java
AggregateOptionsEnum(String options)
```

所属类型：com.mongoplus.enums.AggregateOptionsEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateOptionsEnum.java:40-42

##### method getByOptions

```java
public static AggregateOptionsEnum getByOptions(String options)
```

所属类型：com.mongoplus.enums.AggregateOptionsEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AggregateOptionsEnum.java:44-51

#### enum com.mongoplus.enums.AlgorithmEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AlgorithmEnum.java:8-45

```text
* 加密类型
 *
 * @author anwen
```

##### field algorithm

```java
private final String algorithm
```

所属类型：com.mongoplus.enums.AlgorithmEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AlgorithmEnum.java:36-36

##### method getAlgorithm

```java
public String getAlgorithm()
```

所属类型：com.mongoplus.enums.AlgorithmEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AlgorithmEnum.java:38-40

##### constructor AlgorithmEnum

```java
AlgorithmEnum(String algorithm)
```

所属类型：com.mongoplus.enums.AlgorithmEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/AlgorithmEnum.java:42-44

#### enum com.mongoplus.enums.BannerType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/BannerType.java:3-7

#### enum com.mongoplus.enums.CollectionNameConvertEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CollectionNameConvertEnum.java:6-29

```text
* Collection名称映射枚举
```

#### enum com.mongoplus.enums.CommandEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandEnum.java:8-35

```text
* 命令方法枚举
 *
 * @author loser
```

##### field command

```java
private final String command
```

所属类型：com.mongoplus.enums.CommandEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandEnum.java:26-26

##### constructor CommandEnum

```java
CommandEnum(String command)
```

所属类型：com.mongoplus.enums.CommandEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandEnum.java:28-30

##### method getCommand

```java
public String getCommand()
```

所属类型：com.mongoplus.enums.CommandEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandEnum.java:32-34

#### enum com.mongoplus.enums.CommandOperate

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandOperate.java:10-35

```text
* 命令操作枚举
 * @author anwen
```

##### constructor CommandOperate

```java
CommandOperate(String operate)
```

所属类型：com.mongoplus.enums.CommandOperate；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandOperate.java:18-20

##### field operate

```java
private final String operate
```

所属类型：com.mongoplus.enums.CommandOperate；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandOperate.java:22-22

##### method getOperate

```java
public String getOperate()
```

所属类型：com.mongoplus.enums.CommandOperate；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandOperate.java:24-26

##### method getCommandOperate

```java
public static CommandOperate getCommandOperate(String operate)
```

所属类型：com.mongoplus.enums.CommandOperate；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommandOperate.java:28-33

#### enum com.mongoplus.enums.CommonOperators

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommonOperators.java:7-64

```text
* 操作符
 * @author anwen
```

##### field operator

```java
private final String operator
```

所属类型：com.mongoplus.enums.CommonOperators；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommonOperators.java:54-54

##### constructor CommonOperators

```java
CommonOperators(String operator)
```

所属类型：com.mongoplus.enums.CommonOperators；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommonOperators.java:56-58

##### method getOperator

```java
public String getOperator()
```

所属类型：com.mongoplus.enums.CommonOperators；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CommonOperators.java:60-62

#### enum com.mongoplus.enums.CurrentDateType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CurrentDateType.java:6-29

```text
* $currentDate操作符类型
```

##### field type

```java
private final String type
```

所属类型：com.mongoplus.enums.CurrentDateType；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CurrentDateType.java:20-20

##### constructor CurrentDateType

```java
CurrentDateType(String type)
```

所属类型：com.mongoplus.enums.CurrentDateType；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CurrentDateType.java:22-24

##### method getType

```java
public String getType()
```

所属类型：com.mongoplus.enums.CurrentDateType；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/CurrentDateType.java:26-28

#### enum com.mongoplus.enums.DesensitizationTypeEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/DesensitizationTypeEnum.java:7-91

```text
* 脱敏类型枚举
 * @author anwen
```

#### enum com.mongoplus.enums.ExecuteMethodEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ExecuteMethodEnum.java:10-86

```text
* 执行器方法枚举
 *
 * @author JiaChaoYang
 *
```

##### field method

```java
private final String method
```

所属类型：com.mongoplus.enums.ExecuteMethodEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ExecuteMethodEnum.java:69-69

##### constructor ExecuteMethodEnum

```java
ExecuteMethodEnum(String method)
```

所属类型：com.mongoplus.enums.ExecuteMethodEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ExecuteMethodEnum.java:71-73

##### method getMethod

```java
public String getMethod()
```

所属类型：com.mongoplus.enums.ExecuteMethodEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ExecuteMethodEnum.java:75-77

##### method getMethod

```java
public static ExecuteMethodEnum getMethod(String method)
```

所属类型：com.mongoplus.enums.ExecuteMethodEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ExecuteMethodEnum.java:79-84

#### enum com.mongoplus.enums.FieldFill

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/FieldFill.java:7-24

```text
* @author JiaChaoYang
 * @since quote from: MyBatisPlus
 *
```

#### enum com.mongoplus.enums.GeoType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/GeoType.java:7-28

```text
* 地理位置索引类型
 * @author anwen
```

##### field type

```java
private final String type
```

所属类型：com.mongoplus.enums.GeoType；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/GeoType.java:19-19

##### constructor GeoType

```java
GeoType(String type)
```

所属类型：com.mongoplus.enums.GeoType；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/GeoType.java:21-23

##### method getType

```java
public String getType()
```

所属类型：com.mongoplus.enums.GeoType；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/GeoType.java:25-27

#### enum com.mongoplus.enums.IdTypeEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IdTypeEnum.java:8-51

```text
* @author JiaChaoYang
 * id生成类型
 * @since 2023-02-13 15:59
 *
```

##### field key

```java
private final int key
```

所属类型：com.mongoplus.enums.IdTypeEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IdTypeEnum.java:42-42

##### method getKey

```java
public int getKey()
```

所属类型：com.mongoplus.enums.IdTypeEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IdTypeEnum.java:44-46

##### constructor IdTypeEnum

```java
IdTypeEnum(int key)
```

所属类型：com.mongoplus.enums.IdTypeEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IdTypeEnum.java:48-50

#### enum com.mongoplus.enums.IndexDirection

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexDirection.java:6-24

```text
* @author anwen
```

##### field value

```java
private final Integer value
```

所属类型：com.mongoplus.enums.IndexDirection；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexDirection.java:14-14

##### method getValue

```java
public Integer getValue()
```

所属类型：com.mongoplus.enums.IndexDirection；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexDirection.java:16-18

##### constructor IndexDirection

```java
IndexDirection(Integer value)
```

所属类型：com.mongoplus.enums.IndexDirection；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexDirection.java:20-22

#### enum com.mongoplus.enums.IndexType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/IndexType.java:6-32

```text
* 索引类型枚举
```

#### enum com.mongoplus.enums.LogicDataType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/LogicDataType.java:7-36

```text
* 逻辑删除所支持类型
 * @author anwen
```

#### enum com.mongoplus.enums.MultipleWrite

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/MultipleWrite.java:6-28

```text
* 多写枚举
```

#### enum com.mongoplus.enums.OrderEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/OrderEnum.java:7-24

```text
* 排序枚举
 * @author JiaChaoYang
```

##### field value

```java
private final Integer value
```

所属类型：com.mongoplus.enums.OrderEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/OrderEnum.java:15-15

##### method getValue

```java
public Integer getValue()
```

所属类型：com.mongoplus.enums.OrderEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/OrderEnum.java:17-19

##### constructor OrderEnum

```java
OrderEnum(Integer value)
```

所属类型：com.mongoplus.enums.OrderEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/OrderEnum.java:21-23

#### enum com.mongoplus.enums.PopType

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/PopType.java:6-30

```text
* pop类型
```

##### constructor PopType

```java
PopType(Integer value)
```

所属类型：com.mongoplus.enums.PopType；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/PopType.java:20-22

##### method getValue

```java
public Integer getValue()
```

所属类型：com.mongoplus.enums.PopType；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/PopType.java:24-26

##### field value

```java
private final Integer value
```

所属类型：com.mongoplus.enums.PopType；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/PopType.java:28-28

#### enum com.mongoplus.enums.ProjectionEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ProjectionEnum.java:8-27

```text
* 显隐字段枚举
 *
 * @author JiaChaoYang
 *
```

##### field value

```java
private final Integer value
```

所属类型：com.mongoplus.enums.ProjectionEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ProjectionEnum.java:18-18

##### method getValue

```java
public Integer getValue()
```

所属类型：com.mongoplus.enums.ProjectionEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ProjectionEnum.java:20-22

##### constructor ProjectionEnum

```java
ProjectionEnum(Integer value)
```

所属类型：com.mongoplus.enums.ProjectionEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ProjectionEnum.java:24-26

#### enum com.mongoplus.enums.QueryOperatorEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:7-117

```text
* 条件枚举
 * @author JiaChaoYang
```

##### field value

```java
private final String value
```

所属类型：com.mongoplus.enums.QueryOperatorEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:89-89

##### method getValue

```java
public String getValue()
```

所属类型：com.mongoplus.enums.QueryOperatorEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:91-93

##### method getOperatorValue

```java
public String getOperatorValue()
```

所属类型：com.mongoplus.enums.QueryOperatorEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:95-97

##### constructor QueryOperatorEnum

```java
QueryOperatorEnum(String value)
```

所属类型：com.mongoplus.enums.QueryOperatorEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:99-101

##### method isQueryOperator

```java
public static boolean isQueryOperator(String value)
```

所属类型：com.mongoplus.enums.QueryOperatorEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:103-106

##### method getQueryOperator

```java
public static QueryOperatorEnum getQueryOperator(String value)
```

所属类型：com.mongoplus.enums.QueryOperatorEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/QueryOperatorEnum.java:108-115

#### enum com.mongoplus.enums.ReadConcernEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ReadConcernEnum.java:7-38

```text
* 一致性读策略枚举
 * @author JiaChaoYang
```

#### enum com.mongoplus.enums.ReadPreferenceEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/ReadPreferenceEnum.java:8-34

```text
* 偏好设置节点选择策略
 *
 * @author loser
```

#### enum com.mongoplus.enums.RegexOptions

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/RegexOptions.java:6-42

```text
* 模式匹配选项
```

##### field flag

```java
private final String flag
```

所属类型：com.mongoplus.enums.RegexOptions；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/RegexOptions.java:33-33

##### constructor RegexOptions

```java
RegexOptions(String flag)
```

所属类型：com.mongoplus.enums.RegexOptions；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/RegexOptions.java:35-37

##### method getFlag

```java
public String getFlag()
```

所属类型：com.mongoplus.enums.RegexOptions；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/RegexOptions.java:39-41

#### enum com.mongoplus.enums.SpecialConditionEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/SpecialConditionEnum.java:3-56

##### field condition

```java
private final String condition
```

所属类型：com.mongoplus.enums.SpecialConditionEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/SpecialConditionEnum.java:43-43

##### method getCondition

```java
public String getCondition()
```

所属类型：com.mongoplus.enums.SpecialConditionEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/SpecialConditionEnum.java:45-47

##### method getSubCondition

```java
public String getSubCondition()
```

所属类型：com.mongoplus.enums.SpecialConditionEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/SpecialConditionEnum.java:49-51

##### constructor SpecialConditionEnum

```java
SpecialConditionEnum(String condition)
```

所属类型：com.mongoplus.enums.SpecialConditionEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/SpecialConditionEnum.java:53-55

#### enum com.mongoplus.enums.TextLanguages

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TextLanguages.java:7-99

```text
* 文本搜索语言，默认{@code ENGLISH}
 * @author anwen
```

##### field language

```java
private final String language
```

所属类型：com.mongoplus.enums.TextLanguages；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TextLanguages.java:89-89

##### constructor TextLanguages

```java
TextLanguages(String language)
```

所属类型：com.mongoplus.enums.TextLanguages；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TextLanguages.java:91-93

##### method getLanguage

```java
public String getLanguage()
```

所属类型：com.mongoplus.enums.TextLanguages；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TextLanguages.java:95-97

#### enum com.mongoplus.enums.TypeEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TypeEnum.java:6-60

```text
* @author JiaChaoYang
 *
```

##### field typeCode

```java
private final int typeCode
```

所属类型：com.mongoplus.enums.TypeEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TypeEnum.java:51-51

##### method getTypeCode

```java
public int getTypeCode()
```

所属类型：com.mongoplus.enums.TypeEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TypeEnum.java:53-55

##### constructor TypeEnum

```java
TypeEnum(int typeCode)
```

所属类型：com.mongoplus.enums.TypeEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/TypeEnum.java:57-59

#### enum com.mongoplus.enums.UpdateConditionEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/UpdateConditionEnum.java:7-51

```text
* update条件枚举
 * @author anwen
```

##### field condition

```java
private final String condition
```

所属类型：com.mongoplus.enums.UpdateConditionEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/UpdateConditionEnum.java:37-37

##### method getCondition

```java
public String getCondition()
```

所属类型：com.mongoplus.enums.UpdateConditionEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/UpdateConditionEnum.java:39-41

##### method getSubCondition

```java
public String getSubCondition()
```

所属类型：com.mongoplus.enums.UpdateConditionEnum；可见性：public

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/UpdateConditionEnum.java:43-45

##### constructor UpdateConditionEnum

```java
UpdateConditionEnum(String operator)
```

所属类型：com.mongoplus.enums.UpdateConditionEnum；可见性：private

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/UpdateConditionEnum.java:47-49

#### enum com.mongoplus.enums.WriteConcernEnum

可见性：public；泛型：[]；extends：[]；implements：[]

源码：mongo-plus-annotation/src/main/java/com/mongoplus/enums/WriteConcernEnum.java:7-44

```text
* 控制写入安全的级别
 * @author JiaChaoYang
```

