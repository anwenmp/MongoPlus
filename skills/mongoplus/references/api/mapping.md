# 当前公开API签名：mapping

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

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

## com.mongoplus.holder.ExtraFieldsHolder · mongo-plus-core

`import com.mongoplus.holder.ExtraFieldsHolder;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/holder/ExtraFieldsHolder.java#L8)。类型：CLASS；类型参数 ``；extends `LinkedHashMap<String, ExtraField>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.model.ExtraField;
import java.util.LinkedHashMap;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public void putValue(String key, Object value);
public Object getValue(String key);
public <T> T getValue(String key, Class<T> clazz);
public <T> T getValue(String key, Function<Object, T> function);
public Class<?> getType(String key);
```

## com.mongoplus.incrementer.IdentifierGenerator · mongo-plus-core

`import com.mongoplus.incrementer.IdentifierGenerator;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/incrementer/IdentifierGenerator.java#L26)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.incrementer.id.IdWorker;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default boolean assignId(Object idValue);
Number nextId(Object entity);
default String nextUUID(Object entity);
```

## com.mongoplus.incrementer.id.IdWorker · mongo-plus-core

`import com.mongoplus.incrementer.id.IdWorker;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/incrementer/id/IdWorker.java#L32)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.incrementer.DefaultIdentifierGenerator;
import com.mongoplus.incrementer.IdentifierGenerator;
import com.mongoplus.toolkit.StringPool;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static long getId();
public static long getId(Object entity);
public static String getIdStr();
public static String getIdStr(Object entity);
public static String getMillisecond();
public static String getTimeId();
public static void initSequence(long workerId, long dataCenterId);
public static void setIdentifierGenerator(IdentifierGenerator identifierGenerator);
public static String get32UUID();
public static String get26ULID();
```

## com.mongoplus.mapping.AbstractMongoConverter · mongo-plus-core

`import com.mongoplus.mapping.AbstractMongoConverter;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/AbstractMongoConverter.java#L36)。类型：CLASS；类型参数 ``；extends `null`；implements `MongoConverter`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.annotation.collection.ExtraFields;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.cache.global.MappingCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusConvertException;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.domain.MongoPlusWriteException;
import com.mongoplus.enums.FieldFill;
import com.mongoplus.handlers.ReadHandler;
import com.mongoplus.handlers.TypeHandler;
import com.mongoplus.handlers.auto.AutoFillHandler;
import com.mongoplus.handlers.auto.DefaultAutoFillHandler;
import com.mongoplus.holder.ExtraFieldsHolder;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.toolkit.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import java.util.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AbstractMongoConverter();
@Override public void writeBySave(Object sourceObj, Document document);
@Override public void writeByUpdate(Object sourceObj, Document document);
@Override public void write(Object sourceObj, Bson bson);
@Override @SuppressWarnings({"unchecked", "rawtypes"}) public <T> T readInternal(Document document, TypeReference<T> typeReference, boolean useIdAsFieldName);
public abstract void write(Object sourceObj, Bson bson, TypeInformation typeInformation);
public abstract Bson writeMapInternal(Map<?, ?> obj, Bson bson);
@Override public ConversionStrategy<?> getConversionStrategy(Class<?> target);
public boolean checkObjectId(Object value);
```

## com.mongoplus.mapping.EntityRead · mongo-plus-core

`import com.mongoplus.mapping.EntityRead;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/EntityRead.java#L9)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.Document;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default <T> T read(Document document, Class<T> clazz);
default <T> T read(Object sourceObj, TypeReference<T> typeReference);
default <T> T readInternal(Document document, Class<T> clazz, boolean useIdAsFieldName);
<T> T readInternal(Document document, TypeReference<T> typeReference, boolean useIdAsFieldName);
```

## com.mongoplus.mapping.FieldInformation · mongo-plus-core

`import com.mongoplus.mapping.FieldInformation;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/FieldInformation.java#L15)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void clear();
default void clearAndSetInstance(Object instance);
Class<?> getBelongClass();
Object getInstance();
Object getValue();
Object getValue(Object instance);
void setInstance(Object instance);
String getName();
String getCamelCaseName();
String getIdOrCamelCaseName();
boolean isMap();
Field getField();
Class<?> getTypeClass();
Type[] getType();
TypeInformation getTypeInformation();
Class<?> mapValueType();
Class<?> collectionValueType();
boolean isCollection();
boolean isSimpleType();
boolean isSkipCheckField();
boolean isSkipCheckFieldAndId();
boolean isId();
ID getId();
Method getMethod();
Method setMethod();
void setValue(Object value);
void setValue(Object instance, Object value);
CollectionField getCollectionField();
<T extends Annotation> T getAnnotation(Class<T> annotationClass);
boolean isAnnotation(Class<? extends Annotation> annotationClass);
Type getGenericType();
```

## com.mongoplus.mapping.MappingMongoConverter · mongo-plus-core

`import com.mongoplus.mapping.MappingMongoConverter;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/MappingMongoConverter.java#L34)。类型：CLASS；类型参数 ``；extends `AbstractMongoConverter`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.conditions.interfaces.update.field.operations.Holder;
import com.mongoplus.domain.MongoPlusWriteException;
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.FieldHandlerChain;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.strategy.mapping.MappingStrategy;
import com.mongoplus.toolkit.BsonUtil;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.ObjectIdUtil;
import com.mongoplus.toolkit.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bson.types.ObjectId;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public MappingMongoConverter();
public MappingMongoConverter(List<Class<?>> ignoreType);
@Override public void write(Object sourceObj, Bson bson, TypeInformation typeInformation);
public Bson writeInternal(Object sourceObj, Bson bson);
@Override public Bson writeMapInternal(Map<?, ?> obj, Bson bson);
@Override public void write(Map<?, ?> map, Bson bson);
@Override public <T> T readInternal(Object sourceObj, TypeReference<T> typeReference);
public static Type getGenericTypeClass(ParameterizedType parameterizedType, int size);
@SuppressWarnings({"unchecked", "rawtypes"}) public Collection<?> convertCollection(Type type, Object fieldValue, Collection collection);
@SuppressWarnings({"rawtypes", "unchecked"}) public <V> Map<String, V> convertMap(Type type, Object fieldValue, Map map);
@SuppressWarnings("rawtypes") public Map createMapInstance(Class<?> mapClass);
```

## com.mongoplus.mapping.MongoConverter · mongo-plus-core

`import com.mongoplus.mapping.MongoConverter;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/MongoConverter.java#L19)。类型：INTERFACE；类型参数 ``；extends `null`；implements `MongoWriter, EntityRead`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.MongoIterable;
import com.mongoplus.annotation.ID;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.Document;
import java.util.*;
import static com.mongoplus.convert.Converter.convertKeysToCamelCase;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void writeBySave(Object sourceObj, Document document);
default Document writeBySave(Object sourceObj);
default Document write(Map<String, Object> map);
default List<Document> writeBatch(Collection<Map<String, Object>> sourceObjCollection, List<Document> documentList);
default List<Document> writeBatch(Collection<Map<String, Object>> sourceObjCollection);
default void writeBySaveBatch(Collection<?> sourceObjCollection, List<Document> documentList);
default List<Document> writeBySaveBatch(Collection<?> sourceObjCollection);
void writeByUpdate(Object sourceObj, Document document);
default Document writeByUpdate(Object sourceObj);
default void writeByUpdateBatch(Collection<?> sourceObjCollection, List<Document> documentList);
default List<Document> writeByUpdateBatch(Collection<?> sourceObjCollection);
default <T> T readInternal(Document document, Class<T> clazz);
<T> T readInternal(Object sourceObj, TypeReference<T> typeReference);
default <T> List<T> read(MongoIterable<Document> findIterable, Class<T> clazz);
default <T> List<T> read(MongoIterable<Document> findIterable, TypeReference<T> typeReference);
@SuppressWarnings("unchecked") default <T> T readDocument(MongoIterable<Document> findIterable, Class<?> clazz);
default <T> T readDocument(MongoIterable<Document> findIterable, TypeReference<T> typeReference);
@SuppressWarnings("unchecked") default <T> T convertDocument(Document document, Class<T> clazz);
@SuppressWarnings({"unchecked", "rawtypes"}) default void reSetIdValue(Object sourceObj, Document document);
default <T> void batchReSetIdValue(Collection<T> entityList, List<Document> documentList);
ConversionStrategy<?> getConversionStrategy(Class<?> target);
```

## com.mongoplus.mapping.MongoWriter · mongo-plus-core

`import com.mongoplus.mapping.MongoWriter;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/MongoWriter.java#L11)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import org.bson.conversions.Bson;
import java.util.Map;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
void write(Object sourceObj, Bson bson);
void write(Map<?, ?> map, Bson bson);
```

## com.mongoplus.mapping.SimpleFieldInformation · mongo-plus-core

`import com.mongoplus.mapping.SimpleFieldInformation;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/SimpleFieldInformation.java#L28)。类型：CLASS；类型参数 `T`；extends `null`；implements `FieldInformation`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.cache.global.FieldCache;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.StringUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Field getField();
@Override public Class<?> getTypeClass();
public Type[] getType();
@Override public TypeInformation getTypeInformation();
public SimpleFieldInformation(T instance, Field field);
@Override @SuppressWarnings("unchecked") public void setInstance(Object instance);
@Override public void clear();
@Override public Class<?> getBelongClass();
@Override public Object getInstance();
@Override public Object getValue();
@Override public Object getValue(Object instance);
@Override public String getName();
@Override public String getCamelCaseName();
@Override public String getIdOrCamelCaseName();
@Override public boolean isMap();
@Override public Class<?> mapValueType();
@Override public Class<?> collectionValueType();
@Override public boolean isCollection();
@Override public boolean isSimpleType();
@Override public boolean isSkipCheckField();
@Override public boolean isSkipCheckFieldAndId();
@Override public boolean isId();
@Override public ID getId();
@Override public Method getMethod();
@Override public Method setMethod();
@Override public void setValue(Object value);
@Override public void setValue(Object instance, Object value);
@Override public CollectionField getCollectionField();
@Override @SuppressWarnings("all") public <T extends Annotation> T getAnnotation(Class<T> annotationClass);
@Override public boolean isAnnotation(Class<? extends Annotation> annotationClass);
@Override public Type getGenericType();
```

## com.mongoplus.mapping.SimpleTypeHolder · mongo-plus-core

`import com.mongoplus.mapping.SimpleTypeHolder;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/SimpleTypeHolder.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.toolkit.Assert;
import com.mongoplus.toolkit.ClassTypeUtil;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public SimpleTypeHolder();
public SimpleTypeHolder(Set<? extends Class<?>> customSimpleTypes, boolean registerDefaults);
public SimpleTypeHolder(Set<? extends Class<?>> customSimpleTypes, SimpleTypeHolder source);
public boolean isMongoType(Class<?> type);
public boolean isSimpleType(Class<?> type);
```

## com.mongoplus.mapping.SimpleTypeInformation · mongo-plus-core

`import com.mongoplus.mapping.SimpleTypeInformation;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/SimpleTypeInformation.java#L21)。类型：CLASS；类型参数 `T`；extends `null`；implements `TypeInformation`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.toolkit.ArrayUtils;
import com.mongoplus.toolkit.ClassTypeUtil;
import com.mongoplus.toolkit.CollUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Class<?> getClazz();
@Override public Boolean isMap();
@Override public Boolean isCollection();
@Override public Boolean isSimpleType();
@Override public Type[] getType();
@Override public T getInstance();
public static <T> TypeInformation of(T instance);
@Override @SuppressWarnings("unchecked") public void setInstance(Object instance);
@Override public List<FieldInformation> getFields();
@Override public List<FieldInformation> getThisFields();
@Override public synchronized FieldInformation getField(String fieldName);
@Override public synchronized FieldInformation getFieldNotException(String fieldName);
@Override public <A extends Annotation> A getAnnotation(Class<A> annotationClass);
@Override public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass);
@Override public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
@Override public synchronized List<FieldInformation> getAnnotationFields(Class<? extends Annotation> annotationClass);
@Override public synchronized List<FieldInformation> getAnnotationThisFields(Class<? extends Annotation> annotationClass);
@Override public FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass, String nullMessage);
@Override public FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass);
@Override public FieldInformation getAnnotationThisField(Class<? extends Annotation> annotationClass);
@Override public Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass, String nullMessage);
@Override public Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass);
```

## com.mongoplus.mapping.TypeInformation · mongo-plus-core

`import com.mongoplus.mapping.TypeInformation;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/TypeInformation.java#L16)。类型：INTERFACE；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.InformationCache;
import com.mongoplus.domain.MongoPlusFieldException;
import com.mongoplus.toolkit.ClassTypeUtil;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
static TypeInformation of(Class<?> clazz);
static TypeInformation ofCache(Class<?> clazz);
static TypeInformation of(Object instance);
<T> T getInstance();
void setInstance(Object instance);
List<FieldInformation> getFields();
List<FieldInformation> getThisFields();
FieldInformation getField(String fieldName);
FieldInformation getFieldNotException(String fieldName);
<A extends Annotation> A getAnnotation(Class<A> annotationClass);
<A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass);
boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
List<FieldInformation> getAnnotationFields(Class<? extends Annotation> annotationClass);
List<FieldInformation> getAnnotationThisFields(Class<? extends Annotation> annotationClass);
FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass, String nullMessage);
FieldInformation getAnnotationField(Class<? extends Annotation> annotationClass);
FieldInformation getAnnotationThisField(Class<? extends Annotation> annotationClass);
Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass, String nullMessage);
Object getAnnotationFieldValue(Class<? extends Annotation> annotationClass);
Class<?> getClazz();
Boolean isMap();
Boolean isCollection();
Boolean isSimpleType();
Type[] getType();
```

## com.mongoplus.mapping.TypeReference · mongo-plus-core

`import com.mongoplus.mapping.TypeReference;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapping/TypeReference.java#L12)。类型：CLASS；类型参数 `T`；extends `null`；implements `Type`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.toolkit.ClassTypeUtil;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public TypeReference();
public TypeReference(Type type);
public TypeReference(Class<?> clazz);
public static TypeReference<Object> of(Type type);
public Type getType();
public void setType(Type type);
public Class<?> getClazz();
```

## com.mongoplus.model.AutoFillMetaObject · mongo-plus-core

`import com.mongoplus.model.AutoFillMetaObject;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/AutoFillMetaObject.java#L18)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.bson.MongoPlusDocument;
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.TypeInformation;
import com.mongoplus.support.SFunction;
import org.bson.Document;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public AutoFillMetaObject();
public AutoFillMetaObject(MongoPlusDocument document);
public AutoFillMetaObject(MongoPlusDocument document, TypeInformation typeInformation);
public AutoFillMetaObject(MongoPlusDocument document, ConcurrentSkipListMap<String, Object> autoFillDocument);
public ConcurrentSkipListMap<String, Object> getAllFillField();
public MongoPlusDocument getDocument();
public void getAllFillFieldAndClear(Document document);
public boolean isEmpty();
public void skipCurrentWriteBack();
public <T, R> void fillValue(SFunction<T, R> column, R value);
public <T, R> void forceFillValue(SFunction<T, R> column, R value);
public void fillValue(String column, Object value);
public void forceFillValue(String column, Object value);
public <T, R> boolean metaObjectExist(SFunction<T, R> column);
public boolean metaObjectExist(String column);
public <T, R> Object getMetaObjectValue(SFunction<T, R> column);
public Object getMetaObjectValue(String column);
public TypeInformation getTargetObject();
public void setTargetObject(TypeInformation targetObject);
```

## com.mongoplus.strategy.conversion.ConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.ConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/ConversionStrategy.java#L9)。类型：INTERFACE；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
T convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.BigDecimalConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.BigDecimalConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/BigDecimalConversionStrategy.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BigDecimal>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
import java.math.BigDecimal;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BigDecimal convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.BigIntegerConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.BigIntegerConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/BigIntegerConversionStrategy.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BigInteger>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
import java.math.BigInteger;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BigInteger convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.BooleanConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.BooleanConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/BooleanConversionStrategy.java#L15)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Boolean>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
import java.util.Objects;
import java.util.regex.Pattern;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Boolean convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonBooleanConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonBooleanConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonBooleanConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonBoolean>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonBoolean;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonBoolean convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonDateTimeConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonDateTimeConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonDateTimeConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonDateTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonDateTime;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonDateTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonDoubleConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonDoubleConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonDoubleConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonDouble>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonDouble;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonDouble convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonInt32ConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonInt32ConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonInt32ConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonInt32>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonInt32;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonInt32 convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonInt64ConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonInt64ConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonInt64ConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonInt64>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonInt64;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonInt64 convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.bson.BsonStringConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.bson.BsonStringConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/bson/BsonStringConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<BsonString>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.BsonString;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BsonString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.ByteArrayConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.ByteArrayConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/ByteArrayConversionStrategy.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<byte[]>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.types.Binary;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public byte[] convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.ByteConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.ByteConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/ByteConversionStrategy.java#L10)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Byte>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Byte convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.DateConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.DateConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/DateConversionStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Date>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Date convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.DefaultConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.DefaultConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/DefaultConversionStrategy.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Object>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.cache.global.SimpleCache;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.DocumentConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.DocumentConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/DocumentConversionStrategy.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Document>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Document convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.DoubleConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.DoubleConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/DoubleConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Double>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Double convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.EnumConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.EnumConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/EnumConversionStrategy.java#L18)。类型：CLASS；类型参数 `T`；extends `null`；implements `ConversionStrategy<Enum>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.mapping.SimpleFieldInformation;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Enum convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.FloatConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.FloatConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/FloatConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Float>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Float convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.GeometryCollectionConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.GeometryCollectionConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/GeometryCollectionConversionStrategy.java#L19)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<GeometryCollection>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.GeometryCollection;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import java.util.LinkedList;
import java.util.List;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override @SuppressWarnings("unchecked") public GeometryCollection convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.GeometryConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.GeometryConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/GeometryConversionStrategy.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Geometry>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.cache.global.ConversionCache;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import java.util.Map;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Geometry convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.GeometryUtil · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.GeometryUtil;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/GeometryUtil.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.*;
import org.bson.Document;
import java.util.*;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.strategy.conversion.impl.geo.LineStringConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.LineStringConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/LineStringConversionStrategy.java#L15)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<LineString>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.LineString;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPosition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LineString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.MultiLineStringConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.MultiLineStringConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/MultiLineStringConversionStrategy.java#L18)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<MultiLineString>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.MultiLineString;
import com.mongodb.client.model.geojson.Position;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import java.util.LinkedList;
import java.util.List;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public MultiLineString convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.MultiPointConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.MultiPointConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/MultiPointConversionStrategy.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<MultiPoint>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.MultiPoint;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPosition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public MultiPoint convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.MultiPolygonConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.MultiPolygonConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/MultiPolygonConversionStrategy.java#L20)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<MultiPolygon>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.MultiPolygon;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.CollUtil;
import org.bson.Document;
import java.util.List;
import java.util.stream.Collectors;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPolygonCoordinates;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override @SuppressWarnings("unchecked") public MultiPolygon convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.PointConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.PointConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/PointConversionStrategy.java#L17)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Point>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import java.util.List;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Point convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.geo.PolygonConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.geo.PolygonConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/geo/PolygonConversionStrategy.java#L25)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Polygon>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Polygon;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import org.bson.Document;
import java.util.List;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getCrs;
import static com.mongoplus.strategy.conversion.impl.geo.GeometryUtil.getPolygonCoordinates;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Polygon convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.InstantConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.InstantConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/InstantConversionStrategy.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Instant>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
import java.time.Instant;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Instant convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.IntegerConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.IntegerConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/IntegerConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Integer>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Integer convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.LocalDateConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.LocalDateConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/LocalDateConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<LocalDate>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.InstantUtil;
import java.time.LocalDate;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LocalDate convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.LocalDateTimeConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.LocalDateTimeConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/LocalDateTimeConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<LocalDateTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.InstantUtil;
import java.time.LocalDateTime;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LocalDateTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.LocalTimeConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.LocalTimeConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/LocalTimeConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<LocalTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.InstantUtil;
import java.time.LocalTime;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public LocalTime convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.LongConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.LongConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/LongConversionStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<Long>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Long convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.conversion.impl.StringConversionStrategy · mongo-plus-core

`import com.mongoplus.strategy.conversion.impl.StringConversionStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/conversion/impl/StringConversionStrategy.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `ConversionStrategy<String>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.logging.Log;
import com.mongoplus.logging.LogFactory;
import com.mongoplus.mapping.MongoConverter;
import com.mongoplus.strategy.conversion.ConversionStrategy;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public String convertValue(Object fieldValue, Class<?> fieldType, MongoConverter mongoConverter);
```

## com.mongoplus.strategy.mapping.impl.BigDecimalMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.BigDecimalMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/BigDecimalMappingStrategy.java#L12)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<BigDecimal>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
import org.bson.types.Decimal128;
import java.math.BigDecimal;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(BigDecimal fieldValue);
```

## com.mongoplus.strategy.mapping.impl.BigIntegerMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.BigIntegerMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/BigIntegerMappingStrategy.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<BigInteger>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
import java.math.BigInteger;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(BigInteger fieldValue);
```

## com.mongoplus.strategy.mapping.impl.EnumMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.EnumMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/EnumMappingStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<Enum>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.EnumValue;
import com.mongoplus.mapping.FieldInformation;
import com.mongoplus.strategy.mapping.MappingStrategy;
import static com.mongoplus.toolkit.EnumUtil.getFieldInformation;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(Enum fieldValue);
```

## com.mongoplus.strategy.mapping.impl.LocalDateMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.LocalDateMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/LocalDateMappingStrategy.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<LocalDate>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(LocalDate fieldValue);
```

## com.mongoplus.strategy.mapping.impl.LocalDateTimeMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.LocalDateTimeMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/LocalDateTimeMappingStrategy.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<LocalDateTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(LocalDateTime fieldValue);
```

## com.mongoplus.strategy.mapping.impl.LocalTimeMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.LocalTimeMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/LocalTimeMappingStrategy.java#L14)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<LocalTime>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(LocalTime fieldValue);
```

## com.mongoplus.strategy.mapping.impl.ObjectMappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.impl.ObjectMappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/impl/ObjectMappingStrategy.java#L10)。类型：CLASS；类型参数 ``；extends `null`；implements `MappingStrategy<Object>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.strategy.mapping.MappingStrategy;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public Object mapping(Object fieldValue);
```

## com.mongoplus.strategy.mapping.MappingStrategy · mongo-plus-core

`import com.mongoplus.strategy.mapping.MappingStrategy;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/strategy/mapping/MappingStrategy.java#L8)。类型：INTERFACE；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Object mapping(T fieldValue);
```
