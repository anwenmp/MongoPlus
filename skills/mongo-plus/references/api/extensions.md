# 当前公开API签名：extensions

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.aware.Aware · mongo-plus-core

`import com.mongoplus.aware.Aware;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aware/Aware.java#L8)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.aware.impl.NamespaceAware · mongo-plus-core

`import com.mongoplus.aware.impl.NamespaceAware;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aware/impl/NamespaceAware.java#L5)。类型：INTERFACE；类型参数 ``；extends `null`；implements `Aware`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.aware.Aware;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void nameSpaceAware(Namespace namespace);
```

## com.mongoplus.aware.impl.NamespaceAware.NamespaceBuild · mongo-plus-core

`import com.mongoplus.aware.impl.NamespaceAware.NamespaceBuild;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aware/impl/NamespaceAware.java#L9)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.aware.Aware;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static NamespaceBuild builder();
public NamespaceBuild dataBase(String dataBase);
public NamespaceBuild collectionName(String collectionName);
public NamespaceBuild entityClass(Class<?> entityClass);
public Namespace build();
```

## com.mongoplus.aware.impl.NamespaceAware.Namespace · mongo-plus-core

`import com.mongoplus.aware.impl.NamespaceAware.Namespace;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aware/impl/NamespaceAware.java#L41)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.aware.Aware;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public String getDataBase();
public void setDataBase(String dataBase);
public String getCollectionName();
public void setCollectionName(String collectionName);
public Class<?> getEntityClass();
public void setEntityClass(Class<?> entityClass);
```

## com.mongoplus.aware.MongoAwareUtils · mongo-plus-core

`import com.mongoplus.aware.MongoAwareUtils;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/aware/MongoAwareUtils.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.AwareHandlerCache;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T extends Aware> List<T> listHandlers(Class<T> clazz);
```

## com.mongoplus.cache.global.ConversionCache · mongo-plus-core

`import com.mongoplus.cache.global.ConversionCache;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/cache/global/ConversionCache.java#L25)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.*;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.conversion.impl.*;
import com.mongoplus.strategy.conversion.impl.bson.*;
import com.mongoplus.strategy.conversion.impl.geo.*;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static ConversionStrategy<?> getConversionStrategy(Class<?> clazz);
public static void putConversionStrategy(Class<?> clazz, ConversionStrategy<?> conversionStrategy);
```

## com.mongoplus.cache.global.HandlerCache · mongo-plus-core

`import com.mongoplus.cache.global.HandlerCache;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/cache/global/HandlerCache.java#L20)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.handlers.*;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.handlers.condition.EncryptorConditionHandler;
import com.mongoplus.handlers.field.DBRefHandler;
import com.mongoplus.handlers.field.ObjectIdHandler;
import com.mongoplus.handlers.read.DesensitizationHandlerApply;
import com.mongoplus.handlers.read.FieldEncryptApply;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static List<ReadHandler> getReadHandler();
public static void setReadHandler(ReadHandler readHandler);
public static void addReadHandler(ReadHandler readHandler);
public static void addReadHandler(List<ReadHandler> readHandlers);
public static void setReadHandler(List<ReadHandler> readHandlers);
public static void sortReadHandler();
```

## com.mongoplus.cache.global.MappingCache · mongo-plus-core

`import com.mongoplus.cache.global.MappingCache;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/cache/global/MappingCache.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.strategy.mapping.impl.BigIntegerMappingStrategy;
import com.mongoplus.strategy.mapping.impl.EnumMappingStrategy;
import com.mongoplus.strategy.mapping.impl.ObjectMappingStrategy;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static MappingStrategy<?> getMappingStrategy(Class<?> clazz);
public static void putMappingStrategy(Class<?> clazz, MappingStrategy<?> mappingStrategy);
```

## com.mongoplus.codecs.DateTimeBasedCodec · mongo-plus-core

`import com.mongoplus.codecs.DateTimeBasedCodec;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/DateTimeBasedCodec.java#L25)。类型：CLASS；类型参数 `T`；extends `null`；implements `MongoPlusCodec<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.codecs.configuration.CodecConfigurationException;
import static java.lang.String.format;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.codecs.InstantCodec · mongo-plus-core

`import com.mongoplus.codecs.InstantCodec;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/InstantCodec.java#L35)。类型：CLASS；类型参数 ``；extends `DateTimeBasedCodec<Instant>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import java.time.Instant;
import static java.lang.String.format;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Instant decode(final BsonReader reader, final DecoderContext decoderContext);
@Override public void encode(final BsonWriter writer, final Instant value, final EncoderContext encoderContext);
@Override public Class<Instant> getEncoderClass();
```

## com.mongoplus.codecs.Jsr310CodecProvider · mongo-plus-core

`import com.mongoplus.codecs.Jsr310CodecProvider;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/Jsr310CodecProvider.java#L39)。类型：CLASS；类型参数 ``；extends `null`；implements `CodecProvider`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import java.util.HashMap;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") @Override public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry);
@Override public String toString();
```

## com.mongoplus.codecs.LocalDateCodec · mongo-plus-core

`import com.mongoplus.codecs.LocalDateCodec;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/LocalDateCodec.java#L40)。类型：CLASS；类型参数 ``；extends `DateTimeBasedCodec<LocalDate>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import static java.lang.String.format;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LocalDate decode(final BsonReader reader, final DecoderContext decoderContext);
@Override public void encode(final BsonWriter writer, final LocalDate value, final EncoderContext encoderContext);
@Override public Class<LocalDate> getEncoderClass();
```

## com.mongoplus.codecs.LocalDateTimeCodec · mongo-plus-core

`import com.mongoplus.codecs.LocalDateTimeCodec;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/LocalDateTimeCodec.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `MongoPlusCodec<LocalDateTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LocalDateTime decode(BsonReader reader, DecoderContext decoderContext);
@Override public void encode(BsonWriter writer, LocalDateTime value, EncoderContext encoderContext);
@Override public Class<LocalDateTime> getEncoderClass();
```

## com.mongoplus.codecs.LocalTimeCodec · mongo-plus-core

`import com.mongoplus.codecs.LocalTimeCodec;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/LocalTimeCodec.java#L36)。类型：CLASS；类型参数 ``；extends `DateTimeBasedCodec<LocalTime>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LocalTime decode(final BsonReader reader, final DecoderContext decoderContext);
@Override public void encode(final BsonWriter writer, final LocalTime value, final EncoderContext encoderContext);
@Override public Class<LocalTime> getEncoderClass();
```

## com.mongoplus.codecs.MongoPlusCodec · mongo-plus-core

`import com.mongoplus.codecs.MongoPlusCodec;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/codecs/MongoPlusCodec.java#L9)。类型：INTERFACE；类型参数 `T`；extends `null`；implements `Codec<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.codecs.Codec;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.handlers.auto.AbstractAutoFillHandler · mongo-plus-core

`import com.mongoplus.handlers.auto.AbstractAutoFillHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AbstractAutoFillHandler.java#L28)。类型：CLASS；类型参数 ``；extends `null`；implements `AutoFillHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.bson.ImmutableDocument;
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.AutoFillMetaObject;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void handle(Document source, TypeInformation typeInformation, FieldFill fieldFill, MongoConverter converter);
```

## com.mongoplus.handlers.auto.AutoFillHandler · mongo-plus-core

`import com.mongoplus.handlers.auto.AutoFillHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/AutoFillHandler.java#L9)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.FieldFill;
import com.mongoplus.mapping.MappingMongoConverter;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.TypeInformation;
import org.bson.Document;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void handle(Document source, TypeInformation typeInformation, FieldFill fieldFill, MongoConverter converter);
```

## com.mongoplus.handlers.auto.DefaultAutoFillHandler · mongo-plus-core

`import com.mongoplus.handlers.auto.DefaultAutoFillHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/auto/DefaultAutoFillHandler.java#L11)。类型：CLASS；类型参数 ``；extends `AbstractAutoFillHandler`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.MetaObjectHandler;
import com.mongoplus.model.AutoFillMetaObject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DefaultAutoFillHandler();
```

## com.mongoplus.handlers.CollectionNameHandler · mongo-plus-core

`import com.mongoplus.handlers.CollectionNameHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/CollectionNameHandler.java#L11)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongoplus.enums.ExecuteMethodEnum;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String dynamicCollectionName(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoNamespace namespace);
```

## com.mongoplus.handlers.collection.AnnotationHandler · mongo-plus-core

`import com.mongoplus.handlers.collection.AnnotationHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationHandler.java#L9)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default <T, R> R getProperty(T obj, Function<? super T, ? extends R> func);
```

## com.mongoplus.handlers.collection.AnnotationOperate · mongo-plus-core

`import com.mongoplus.handlers.collection.AnnotationOperate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/collection/AnnotationOperate.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `AnnotationHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.enums.CollectionNameConvertEnum;
import com.mongoplus.toolkit.StringUtils;
import java.util.function.Function;
import static com.mongoplus.enums.CollectionNameConvertEnum.ALL_CHAR_LOWERCASE;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static AnnotationHandler getAnnotationHandler();
public static void setAnnotationHandler(AnnotationHandler annotationHandler);
public static void setCollectionNameConvertEnum(CollectionNameConvertEnum collectionNameConvertEnum);
public static String getCollectionName(Class<?> clazz);
public static <R> R getCollectionInfo(CollectionName collectionName, Function<? super CollectionName, ? extends R> func);
public static String getDatabase(Class<?> clazz);
```

## com.mongoplus.handlers.condition.AbstractCondition · mongo-plus-core

`import com.mongoplus.handlers.condition.AbstractCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/AbstractCondition.java#L30)。类型：CLASS；类型参数 ``；extends `null`；implements `Condition, UpdateCondition`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.enums.UpdateConditionEnum;
import com.mongoplus.model.BuildUpdate;
import com.mongoplus.model.MutablePair;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;
import org.bson.BsonDocument;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BasicDBObject queryCondition(List<ConditionMetaObject> conditionMetaObjectList);
@Override public BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject);
@Override public MutablePair<BasicDBObject, BasicDBObject> updateCondition(UpdateChainWrapper<?, ?> updateChainWrapper);
public BasicDBObject updateValue(UpdateConditionEnum updateConditionEnum, List<ConditionMetaObject> conditionMetaObjectList);
public abstract BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject, MongoPlusBasicDBObject mongoPlusBasicDBObject);
```

## com.mongoplus.handlers.condition.BuildCondition · mongo-plus-core

`import com.mongoplus.handlers.condition.BuildCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/BuildCondition.java#L46)。类型：CLASS；类型参数 ``；extends `AbstractCondition`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.options.PushOptions;
import com.mongoplus.options.TextSearchOptions;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.model.Order;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.*;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.BuildUpdate;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.geo.GeoBox;
import com.mongoplus.model.geo.GeoCenter;
import com.mongoplus.model.geo.GeoNear;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.Filters;
import org.bson.BsonDocument;
import org.bson.BsonType;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import static com.mongoplus.enums.QueryOperatorEnum.*;
import static com.mongoplus.enums.QueryOperatorEnum.EQ;
import static com.mongoplus.enums.QueryOperatorEnum.REGEX;
import static com.mongoplus.enums.SpecialConditionEnum.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Condition condition();
public static void setCondition(Condition condition);
public BuildCondition();
@Override @SuppressWarnings("unchecked") public BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject, MongoPlusBasicDBObject mongoPlusBasicDBObject);
@Override public BasicDBObject buildUpdateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override public BasicDBObject buildPushCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override public BasicDBObject buildCurrentDateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override @SuppressWarnings("unchecked") public BasicDBObject buildRenameCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override @SuppressWarnings("unchecked") public BasicDBObject buildUnsetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override public BasicDBObject buildAddToSetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override public BasicDBObject buildPullCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
@Override public BaseConditionResult queryCondition(Wrapper<?> wrapper);
public void logic(Wrapper<?> queryWrapper, MongoPlusBasicDBObject basicDBObject, Function<List<Bson>, Bson> function);
@SuppressWarnings("unchecked") public <T> void simpleUpdateLogic(List<Bson> bsonList, ConditionMetaObject conditionMetaObject, BiFunction<String, T, Bson> function);
public void simpleUpdateLogic(List<Bson> bsonList, ConditionMetaObject conditionMetaObject, Function<String, Bson> function);
```

## com.mongoplus.handlers.condition.Condition · mongo-plus-core

`import com.mongoplus.handlers.condition.Condition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/Condition.java#L20)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.model.Projection;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.toolkit.CollUtil;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
BasicDBObject queryCondition(List<ConditionMetaObject> conditionMetaObjectList);
BasicDBObject queryCondition(ConditionMetaObject conditionMetaObject);
MutablePair<BasicDBObject, BasicDBObject> updateCondition(UpdateChainWrapper<?, ?> updateChainWrapper);
default BasicDBObject projectionCondition(List<Projection> projectionList);
BaseConditionResult queryCondition(Wrapper<?> wrapper);
default boolean isEmpty(@Nullable AbstractChainWrapper<?, ?> wrapper);
default boolean isNotEmpty(@Nullable AbstractChainWrapper<?, ?> wrapper);
```

## com.mongoplus.handlers.condition.ConditionHandler · mongo-plus-core

`import com.mongoplus.handlers.condition.ConditionHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/ConditionHandler.java#L11)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default void beforeQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
default void afterQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
default void beforeUpdateCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
default void afterUpdateCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
```

## com.mongoplus.handlers.condition.EncryptorConditionHandler · mongo-plus-core

`import com.mongoplus.handlers.condition.EncryptorConditionHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/EncryptorConditionHandler.java#L20)。类型：CLASS；类型参数 ``；extends `null`；implements `ConditionHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.toolkit.EncryptorUtil;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void beforeQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
```

## com.mongoplus.handlers.condition.UpdateCondition · mongo-plus-core

`import com.mongoplus.handlers.condition.UpdateCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/condition/UpdateCondition.java#L13)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.model.BuildUpdate;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
BasicDBObject buildUpdateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
BasicDBObject buildPushCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
BasicDBObject buildCurrentDateCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
BasicDBObject buildRenameCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
BasicDBObject buildUnsetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
BasicDBObject buildAddToSetCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
BasicDBObject buildPullCondition(List<ConditionMetaObject> conditionMetaObjectList, BuildUpdate buildUpdate);
```

## com.mongoplus.handlers.DataBindHandler · mongo-plus-core

`import com.mongoplus.handlers.DataBindHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/DataBindHandler.java#L11)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.collection.FieldBind;
import com.mongoplus.mapping.FieldInformation;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void setMetaObject(FieldBind fieldBind, Object fieldValue, FieldInformation fieldInformation);
```

## com.mongoplus.handlers.DataSourceHandler · mongo-plus-core

`import com.mongoplus.handlers.DataSourceHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/DataSourceHandler.java#L7)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String getDataSource(String dsName);
```

## com.mongoplus.handlers.DesensitizationHandler · mongo-plus-core

`import com.mongoplus.handlers.DesensitizationHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/DesensitizationHandler.java#L12)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.DesensitizationTypeEnum;
import java.lang.reflect.Field;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
String desensitized(Field field, Object obj, int startInclude, int endExclude, DesensitizationTypeEnum desensitizedType);
```

## com.mongoplus.handlers.FieldHandler · mongo-plus-core

`import com.mongoplus.handlers.FieldHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandler.java#L11)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.FieldInformation;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Integer order();
default Function<FieldInformation, Boolean> activate();
@Deprecated Object handler(FieldInformation fieldInformation);
default Object handler(FieldInformation fieldInformation, Object currentValue);
```

## com.mongoplus.handlers.FieldHandlerChain · mongo-plus-core

`import com.mongoplus.handlers.FieldHandlerChain;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/FieldHandlerChain.java#L18)。类型：CLASS；类型参数 ``；extends `ArrayList<FieldHandler>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.handlers.field.DBRefHandler;
import com.mongoplus.handlers.field.EncryptFieldHandler;
import com.mongoplus.handlers.field.TypeHandlerFieldHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.UnaryOperator;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static FieldHandlerChain getInstance();
public void register(FieldHandler fieldHandler);
public void registerAll(Collection<? extends FieldHandler> fieldHandlers);
@Override public boolean add(FieldHandler fieldHandler);
@Override public void add(int index, FieldHandler element);
@Override public boolean addAll(Collection<? extends FieldHandler> fieldHandlers);
@Override public boolean addAll(int index, Collection<? extends FieldHandler> fieldHandlers);
@Override public FieldHandler set(int index, FieldHandler element);
@Override public void replaceAll(UnaryOperator<FieldHandler> operator);
```

## com.mongoplus.handlers.field.DBRefHandler · mongo-plus-core

`import com.mongoplus.handlers.field.DBRefHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/field/DBRefHandler.java#L40)。类型：CLASS；类型参数 ``；extends `null`；implements `FieldHandler, ReadHandler, ConditionHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionName;
import com.mongoplus.annotation.collection.DBRef;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.execute.Execute;
import com.mongoplus.execute.ExecutorFactory;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.handlers.collection.AnnotationOperate;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.SimpleFieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.registry.ComponentRegistry;
import com.mongoplus.toolkit.Filters;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Integer order();
@Override public Function<FieldInformation, Boolean> activate();
@Override public Function<Object, Boolean> discontinue();
@Override public Object handler(FieldInformation fieldInformation);
@Override public Object handler(FieldInformation fieldInformation, Object currentValue);
@Override public Object read(FieldInformation fieldInformation, Object source, MongoConverter mongoConverter);
@Override public void beforeQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
public com.mongodb.DBRef getDBRef(Class<?> typeClass, DBRef dbRefAnnotation, Object value);
```

## com.mongoplus.handlers.field.EncryptFieldHandler · mongo-plus-core

`import com.mongoplus.handlers.field.EncryptFieldHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/field/EncryptFieldHandler.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `FieldHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.EncryptorUtil;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Integer order();
@Override public Function<FieldInformation, Boolean> activate();
@Override public Object handler(FieldInformation fieldInformation);
@Override public Object handler(FieldInformation fieldInformation, Object currentValue);
```

## com.mongoplus.handlers.field.ObjectIdHandler · mongo-plus-core

`import com.mongoplus.handlers.field.ObjectIdHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/field/ObjectIdHandler.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements `ConditionHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.handlers.condition.ConditionHandler;
import com.mongoplus.toolkit.ObjectIdUtil;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void beforeQueryCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
@Override public void beforeUpdateCondition(ConditionMetaObject conditionMetaObject, BasicDBObject basicDBObject);
public void handler(ConditionMetaObject conditionMetaObject);
```

## com.mongoplus.handlers.field.TypeHandlerFieldHandler · mongo-plus-core

`import com.mongoplus.handlers.field.TypeHandlerFieldHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/field/TypeHandlerFieldHandler.java#L15)。类型：CLASS；类型参数 ``；extends `null`；implements `FieldHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.TypeHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.ClassTypeUtil;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Function<FieldInformation, Boolean> activate();
@Override public Object handler(FieldInformation fieldInformation);
@Override @SuppressWarnings({"rawtypes", "unchecked"}) public Object handler(FieldInformation fieldInformation, Object currentValue);
```

## com.mongoplus.handlers.IdGenerateHandler · mongo-plus-core

`import com.mongoplus.handlers.IdGenerateHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/IdGenerateHandler.java#L12)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.enums.IdTypeEnum;
import com.mongoplus.mapping.TypeInformation;
import java.io.Serializable;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Serializable generateId(IdTypeEnum idTypeEnum, TypeInformation typeInformation);
```

## com.mongoplus.handlers.MetaObjectHandler · mongo-plus-core

`import com.mongoplus.handlers.MetaObjectHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/MetaObjectHandler.java#L10)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.model.AutoFillMetaObject;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void insertFill(AutoFillMetaObject insertAutoFillMetaObject);
void updateFill(AutoFillMetaObject updateAutoFillMetaObject);
```

## com.mongoplus.handlers.ReadHandler · mongo-plus-core

`import com.mongoplus.handlers.ReadHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/ReadHandler.java#L13)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Integer order();
default Object getExtraValue(Object object);
default Function<FieldInformation, Boolean> activate();
default Function<Object, Boolean> discontinue();
default Object read(FieldInformation fieldInformation, Object source);
default Object read(FieldInformation fieldInformation, Object source, MongoConverter mongoConverter);
```

## com.mongoplus.handlers.read.DesensitizationHandlerApply · mongo-plus-core

`import com.mongoplus.handlers.read.DesensitizationHandlerApply;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/read/DesensitizationHandlerApply.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `ReadHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.Desensitization;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.handlers.DesensitizationHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.DesensitizedUtil;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Integer order();
@Override public Object read(FieldInformation fieldInformation, Object source);
```

## com.mongoplus.handlers.read.FieldBindHandler · mongo-plus-core

`import com.mongoplus.handlers.read.FieldBindHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldBindHandler.java#L18)。类型：CLASS；类型参数 ``；extends `null`；implements `ReadHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.collection.FieldBind;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.handlers.DataBindHandler;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.toolkit.StringUtils;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public FieldBindHandler(DataBindHandler dataBindHandler);
@Override public Function<FieldInformation, Boolean> activate();
@Override public Object read(FieldInformation fieldInformation, Object source);
```

## com.mongoplus.handlers.read.FieldEncryptApply · mongo-plus-core

`import com.mongoplus.handlers.read.FieldEncryptApply;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/read/FieldEncryptApply.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `ReadHandler`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.toolkit.EncryptorUtil;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Integer order();
@Override public Object read(FieldInformation fieldInformation, Object source);
```

## com.mongoplus.handlers.TenantHandler · mongo-plus-core

`import com.mongoplus.handlers.TenantHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/TenantHandler.java#L13)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.BsonValue;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
BsonValue getTenantId();
default String getTenantIdColumn();
default boolean ignoreCollection(String collectionName);
default boolean ignoreDatabase(String database);
default boolean ignoreDataSource(String dataSource);
default boolean ignoreInsert(List<String> columns, String tenantIdColumn);
```

## com.mongoplus.handlers.TransactionHandler · mongo-plus-core

`import com.mongoplus.handlers.TransactionHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/TransactionHandler.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.support.ThrowableSupplier;
import static com.mongoplus.manager.MongoTransactionalManager.handleTransactionException;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public Object transaction(ThrowableSupplier<Object> supplier, MongoTransactional mongoTransactional);
```

## com.mongoplus.handlers.TypeHandler · mongo-plus-core

`import com.mongoplus.handlers.TypeHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/TypeHandler.java#L9)。类型：INTERFACE；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Object setParameter(String fieldName, T obj);
T getResult(Object obj);
```

## com.mongoplus.handlers.write.MultipleWriteHandler · mongo-plus-core

`import com.mongoplus.handlers.write.MultipleWriteHandler;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/handlers/write/MultipleWriteHandler.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `MultipleWriteStrategy`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.enums.MultipleWrite;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.toolkit.CollUtil;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MultipleWriteHandler(MongoPlusClient mongoPlusClient);
public void addMultipleWriteStrategy(MultipleWrite multipleWrite, List<String> dsNameList);
@Override public List<String> multipleWrite(String currentDataSource, MongoNamespace namespace);
public List<String> getMultipleWrite(MultipleWrite multipleWrite, MongoNamespace namespace);
```

## com.mongoplus.interceptor.AdvancedInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.AdvancedInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/AdvancedInterceptor.java#L9)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.AdvancedFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default int order();
Object intercept(Invocation invocation);
default AdvancedFunction activate();
```

## com.mongoplus.interceptor.AdvancedInterceptorChain · mongo-plus-core

`import com.mongoplus.interceptor.AdvancedInterceptorChain;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/AdvancedInterceptorChain.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.execute.Execute;
import com.mongoplus.proxy.AdvancedProxy;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Execute wrap(Execute target);
public static void addInterceptor(AdvancedInterceptor interceptor);
public static void addInterceptors(List<AdvancedInterceptor> interceptorList);
public static List<AdvancedInterceptor> getInterceptors();
public static AdvancedInterceptor getInterceptor(Predicate<? super AdvancedInterceptor> predicate);
public static void sorted();
```

## com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.AsyncMultipleWriteInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java#L35)。类型：CLASS；类型参数 ``；extends `null`；implements `AdvancedInterceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.domain.MongoPlusDsException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.MultipleWrite;
import com.mongoplus.execute.instance.DefaultExecute;
import com.mongoplus.handlers.write.MultipleWriteHandler;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.logic.LogicRemove;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.model.MutablePair;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import static com.mongoplus.enums.MultipleWrite.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public void setExecute(DefaultExecute execute);
public void setMultipleWriteHandler(MultipleWriteHandler multipleWriteHandler);
public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient);
public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient, MultipleWriteHandler multipleWriteHandler);
public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient, ThreadPoolExecutor executor);
public AsyncMultipleWriteInterceptor(MongoPlusClient mongoPlusClient, ThreadPoolExecutor executor, MultipleWriteHandler multipleWriteHandler);
@Override public Object intercept(Invocation invocation);
```

## com.mongoplus.interceptor.business.CollectionLogiceInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.CollectionLogiceInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/CollectionLogiceInterceptor.java#L35)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import com.mongoplus.toolkit.BsonUtil;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import static com.mongoplus.enums.QueryOperatorEnum.EQ;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Bson executeRemove(Bson filter, MongoCollection<Document> collection);
@Override public Bson executeRemoveOne(Bson filter, MongoCollection<Document> collection);
@Override public QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond, MongoCollection<Document> collection);
@Override public MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic, CountOptions countOptions, MongoCollection<Document> collection);
@Override @SuppressWarnings("all") public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection);
@Override public List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection);
@Override public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection);
@Override public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList, MongoCollection<Document> collection);
```

## com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L37)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.SpecialConditionEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.OperationResult;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection);
@Override public void afterExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, Object result, MongoCollection<Document> collection);
public String getDatasourceName();
public void setDatasourceName(String datasourceName);
public Boolean getIsMasterDatasource();
public void isMasterDatasource(Boolean masterDatasource);
public String getDatabaseName();
public void setDatabaseName(String databaseName);
public String getCollectionName();
public void setCollectionName(String collectionName);
public void enableSaveDatabase(BaseMapper baseMapper);
public BaseMapper getBaseMapper();
public Boolean getDisplayCompleteData();
public void setDisplayCompleteData(Boolean displayCompleteData);
public String getExceptionMessage();
public void setExceptionMessage(String exceptionMessage);
public List<String> getIgnoredColumnList();
public void setIgnoredColumnList(List<String> ignoredColumnList);
public Integer getBatchUpdateLimit();
public void setBatchUpdateLimit(Integer batchUpdateLimit);
```

## com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor.DataUpdateLimitationException · mongo-plus-core

`import com.mongoplus.interceptor.business.DataChangeRecorderInnerInterceptor.DataUpdateLimitationException;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java#L384)。类型：CLASS；类型参数 ``；extends `MongoPlusException`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.constant.DataSourceConstant;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.SpecialConditionEnum;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.OperationResult;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DataUpdateLimitationException(String message);
```

## com.mongoplus.interceptor.business.DynamicCollectionNameInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.DynamicCollectionNameInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DynamicCollectionNameInterceptor.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.handlers.CollectionNameHandler;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.manager.MongoPlusClient;
import org.bson.Document;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DynamicCollectionNameInterceptor(CollectionNameHandler collectionNameHandler, MongoPlusClient mongoPlusClient);
@Override public int order();
@Override public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection);
```

## com.mongoplus.interceptor.business.LogicAutoFillInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.LogicAutoFillInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/LogicAutoFillInterceptor.java#L20)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.logic.LogicDeleteHandler;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.model.LogicDeleteResult;
import org.bson.Document;
import java.util.List;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection);
@Override public Document executeSave(Document document, MongoCollection<Document> collection);
@Override public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection);
```

## com.mongoplus.interceptor.business.LogicRemoveInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.LogicRemoveInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/LogicRemoveInterceptor.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements `AdvancedInterceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logic.LogicRemove;
import com.mongoplus.manager.LogicManager;
import com.mongoplus.support.AdvancedFunction;
import static com.mongoplus.enums.ExecuteMethodEnum.REMOVE;
import static com.mongoplus.enums.ExecuteMethodEnum.REMOVE_ONE;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public int order();
@Override public AdvancedFunction activate();
@Override public Object intercept(Invocation invocation);
```

## com.mongoplus.interceptor.business.OptimisticLockerInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.OptimisticLockerInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/OptimisticLockerInterceptor.java#L42)。类型：CLASS；类型参数 ``；extends `null`；implements `AdvancedInterceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.UpdateResult;
import com.mongoplus.annotation.collection.Version;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.domain.OptimisticLockerException;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.enums.UpdateConditionEnum;
import com.mongoplus.interceptor.AdvancedInterceptor;
import com.mongoplus.interceptor.Invocation;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.Retry;
import com.mongoplus.model.UpdateRetryResult;
import com.mongoplus.registry.MongoEntityMappingRegistry;
import com.mongoplus.toolkit.BsonUtil;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import static com.mongoplus.enums.ExecuteMethodEnum.*;
import static com.mongoplus.enums.UpdateConditionEnum.INC;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public void setAutoInc(Integer autoInc);
public void setVersionIsNullException(RuntimeException versionIsNullException);
public void setUpdateFailException(RuntimeException updateFailException);
public void enableRetry(Retry retry);
@Override public Object intercept(Invocation invocation);
public Object executor(Invocation invocation, boolean autoVersion);
```

## com.mongoplus.interceptor.business.TenantInterceptor · mongo-plus-core

`import com.mongoplus.interceptor.business.TenantInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/TenantInterceptor.java#L39)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.DeleteManyModel;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.aggregate.AggregateWrapper;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.enums.AggregateEnum;
import com.mongoplus.handlers.TenantHandler;
import com.mongoplus.interceptor.Interceptor;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import com.mongoplus.toolkit.BsonUtil;
import com.mongoplus.toolkit.CollUtil;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static com.mongoplus.enums.QueryOperatorEnum.EQ;
import static com.mongoplus.manager.TenantManager.isTenantIgnored;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public TenantInterceptor(TenantHandler tenantHandler);
@Override public int order();
@Override public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection);
@Override public Document executeSave(Document document, MongoCollection<Document> collection);
@Override public Bson executeRemove(Bson filter, MongoCollection<Document> collection);
@Override public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList, MongoCollection<Document> collection);
@Override public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection);
@Override public QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond, MongoCollection<Document> collection);
@Override public List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection);
@Override public MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic, CountOptions countOptions, MongoCollection<Document> collection);
@Override public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection);
```

## com.mongoplus.interceptor.Interceptor · mongo-plus-core

`import com.mongoplus.interceptor.Interceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/Interceptor.java#L22)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.model.MutablePair;
import com.mongoplus.model.QueryParam;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default int order();
default void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection);
default void afterExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, Object result, MongoCollection<Document> collection);
default List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection);
default Document executeSave(Document document, MongoCollection<Document> collection);
default Bson executeRemove(Bson filter, MongoCollection<Document> collection);
default Bson executeRemoveOne(Bson filter, MongoCollection<Document> collection);
default List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList, MongoCollection<Document> collection);
default MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection);
default QueryParam executeQuery(Bson queryBasic, BasicDBObject projectionList, BasicDBObject sortCond, MongoCollection<Document> collection);
default List<Bson> executeAggregates(List<Bson> aggregateConditionList, MongoCollection<Document> collection);
default MutablePair<BasicDBObject, CountOptions> executeCount(BasicDBObject queryBasic, CountOptions countOptions, MongoCollection<Document> collection);
default void executeEstimatedDocumentCount(MongoCollection<Document> collection);
default List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection);
```

## com.mongoplus.interceptor.InterceptorChain · mongo-plus-core

`import com.mongoplus.interceptor.InterceptorChain;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/InterceptorChain.java#L9)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static void addInterceptor(Interceptor interceptor);
public static void addInterceptors(List<Interceptor> interceptorList);
public static List<Interceptor> getInterceptors();
public static Interceptor getInterceptor(Predicate<? super Interceptor> predicate);
public static void sorted();
```

## com.mongoplus.interceptor.Invocation · mongo-plus-core

`import com.mongoplus.interceptor.Invocation;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/Invocation.java#L10)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.execute.ExecutorFactory;
import org.bson.Document;
import java.lang.reflect.Method;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") public Invocation(Object proxy, Object target, Method method, Object[] args);
public Object getProxy();
public Object getTarget();
public Method getMethod();
public Object[] getArgs();
public ExecutorFactory getExecutorFactory();
public MongoCollection<Document> getCollection();
public ExecuteMethodEnum getExecuteMethod();
public Object proceed();
public Object discontinue();
```

## com.mongoplus.listener.BackupListener · mongo-plus-core

`import com.mongoplus.listener.BackupListener;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/BackupListener.java#L5)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.Document;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void export(String path, String collectionName, Document document);
```

## com.mongoplus.listener.BaseListener · mongo-plus-core

`import com.mongoplus.listener.BaseListener;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `CommandListener`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import com.mongoplus.domain.MongoPlusInterceptorException;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void commandStarted(CommandStartedEvent event);
@Override public void commandSucceeded(CommandSucceededEvent event);
@Override public void commandFailed(CommandFailedEvent event);
```

## com.mongoplus.listener.business.BlockAttackInnerListener · mongo-plus-core

`import com.mongoplus.listener.business.BlockAttackInnerListener;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/business/BlockAttackInnerListener.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements `Listener`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.listener.Listener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import org.bson.BsonValue;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void commandStarted(CommandStarted commandStarted);
@Override public void commandSucceeded(CommandSucceeded commandSucceeded);
@Override public void commandFailed(CommandFailed commandFailed);
@Override public int getOrder();
```

## com.mongoplus.listener.business.LogListener · mongo-plus-core

`import com.mongoplus.listener.business.LogListener;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/business/LogListener.java#L19)。类型：CLASS；类型参数 ``；extends `null`；implements `Listener`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.OrderCache;
import com.mongoplus.listener.Listener;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import com.mongoplus.toolkit.MongoCommandBuildUtils;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public boolean isPretty();
public void setPretty(boolean pretty);
public LogListener(boolean pretty);
public LogListener();
@Override public void commandStarted(CommandStarted commandStarted);
@Override public void commandSucceeded(CommandSucceeded commandSucceeded);
@Override public void commandFailed(CommandFailed commandFailed);
@Override public int getOrder();
```

## com.mongoplus.listener.Listener · mongo-plus-core

`import com.mongoplus.listener.Listener;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/Listener.java#L11)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void commandStarted(CommandStarted commandStarted);
void commandSucceeded(CommandSucceeded commandSucceeded);
void commandFailed(CommandFailed commandFailed);
int getOrder();
```

## com.mongoplus.listener.MongoPlusListener · mongo-plus-core

`import com.mongoplus.listener.MongoPlusListener;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/MongoPlusListener.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `Listener`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.ListenerCache;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public void commandStarted(CommandStarted commandStarted);
@Override public void commandSucceeded(CommandSucceeded commandSucceeded);
@Override public void commandFailed(CommandFailed commandFailed);
@Override public int getOrder();
```

## com.mongoplus.registry.ComponentRegistry · mongo-plus-core

`import com.mongoplus.registry.ComponentRegistry;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/registry/ComponentRegistry.java#L9)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") public static <T> T get(Class<T> clazz);
public static <T> void register(Class<T> clazz, T instance);
@SuppressWarnings("unchecked") public static <T> void register(T instance);
```

## com.mongoplus.interceptor.SensitiveWordInterceptor · mongo-plus-sensitive-word

`import com.mongoplus.interceptor.SensitiveWordInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/interceptor/SensitiveWordInterceptor.java#L21)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateManyModel;
import com.mongodb.client.model.WriteModel;
import com.mongoplus.cache.codec.MapCodecCache;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.model.MutablePair;
import com.mongoplus.registry.HandlerRegistry;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SensitiveWordInterceptor();
@Override public List<Document> executeSave(List<Document> documentList, MongoCollection<Document> collection);
@Override public Document executeSave(Document document, MongoCollection<Document> collection);
@Override public List<MutablePair<Bson, Bson>> executeUpdate(List<MutablePair<Bson, Bson>> updatePairList, MongoCollection<Document> collection);
@Override public MutablePair<Bson, Bson> executeUpdate(MutablePair<Bson, Bson> updatePair, MongoCollection<Document> collection);
@Override public List<WriteModel<Document>> executeBulkWrite(List<WriteModel<Document>> writeModelList, MongoCollection<Document> collection);
```

## com.mongoplus.interceptor.DataSourceShardingInterceptor · mongo-plus-sharding

`import com.mongoplus.interceptor.DataSourceShardingInterceptor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sharding/src/main/java/com/mongoplus/interceptor/DataSourceShardingInterceptor.java#L36)。类型：CLASS；类型参数 ``；extends `null`；implements `Interceptor, AdvancedInterceptor`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.MongoNamespace;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongoplus.cache.global.DataSourceNameCache;
import com.mongoplus.context.MongoTransactionContext;
import com.mongoplus.context.MongoTransactionStatus;
import com.mongoplus.context.ShardingTransactionContext;
import com.mongoplus.enums.ExecuteMethodEnum;
import com.mongoplus.execute.instance.DefaultExecute;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.manager.MongoPlusClient;
import com.mongoplus.manager.MongoTransactionalManager;
import com.mongoplus.model.BaseProperty;
import com.mongoplus.sharding.AbstractDataSourceShardingHandler;
import com.mongoplus.sharding.DataSourceShardingHandler;
import com.mongoplus.sharding.DataSourceShardingStrategy;
import com.mongoplus.toolkit.CollUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient);
public DataSourceShardingInterceptor(MongoPlusClient mongoPlusClient, AbstractDataSourceShardingHandler dataSourceShardingHandler);
@Override public int order();
@Override public void beforeExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, MongoCollection<Document> collection);
@Override public Object intercept(Invocation invocation);
@Override public void afterExecute(ExecuteMethodEnum executeMethodEnum, Object[] source, Object result, MongoCollection<Document> collection);
```
