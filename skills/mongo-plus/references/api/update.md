# 当前公开API签名：update

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.conditions.interfaces.PushOptions · mongo-plus-core

`import com.mongoplus.conditions.interfaces.PushOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/PushOptions.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.Nullable;
import org.bson.Document;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Nullable public Integer getPosition();
public PushOptions position(@Nullable final Integer position);
@Nullable public Integer getSlice();
public PushOptions slice(@Nullable final Integer slice);
@Nullable public Integer getSort();
public PushOptions sort(@Nullable final Integer sort);
@Nullable public Document getSortDocument();
public PushOptions sortDocument(@Nullable final Document sortDocument);
@Override public boolean equals(final Object o);
@Override public int hashCode();
@Override public String toString();
```

## com.mongoplus.conditions.interfaces.update.array.Array · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.array.Array;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/array/Array.java#L10)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `AddToSet<T, Children>, Pop<T, Children>, Pull<T, Children>, PullAll<T, Children>, Push<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.array.operations.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.update.array.operations.AddToSet · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.array.operations.AddToSet;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/array/operations/AddToSet.java#L16)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ClassTypeUtil;
import java.util.Collection;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children addToSet(boolean condition, SFunction<T, Object> column, Object value, boolean each);
default Children addToSet(SFunction<T, Object> column, Object value, boolean each);
default Children addToSet(boolean condition, SFunction<T, Object> column, List<?> value);
default Children addToSet(SFunction<T, Object> column, List<?> value);
default Children addToSet(boolean condition, String column, Object value, boolean each);
default Children addToSet(String column, Object value, boolean each);
default Children addToSet(boolean condition, String column, List<?> value);
default Children addToSet(String column, List<?> value);
```

## com.mongoplus.conditions.interfaces.update.array.operations.Pop · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.array.operations.Pop;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/array/operations/Pop.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.enums.PopType;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children pop(boolean condition, SFunction<T, Object> column, PopType popType);
default Children pop(SFunction<T, Object> column, PopType popType);
default Children pop(boolean condition, String column, PopType popType);
default Children pop(String column, PopType popType);
```

## com.mongoplus.conditions.interfaces.update.array.operations.Pull · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.array.operations.Pull;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/array/operations/Pull.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children pull(boolean condition, SFunction<T, Object> column, Object value);
default Children pull(SFunction<T, Object> column, Object value);
default Children pull(boolean condition, Wrapper<?> queryWrapper);
default Children pull(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children pull(Wrapper<?> queryWrapper);
default Children pull(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children pull(boolean condition, String column, Object value);
default Children pull(String column, Object value);
```

## com.mongoplus.conditions.interfaces.update.array.operations.PullAll · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.array.operations.PullAll;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/array/operations/PullAll.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.model.MutablePair;
import com.mongoplus.support.SFunction;
import java.util.Arrays;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children pullAll(boolean condition, SFunction<T, Object> column, Object... values);
default Children pullAll(SFunction<T, Object> column, Object... values);
default Children pullAll(boolean condition, String column, Collection<?> values);
default Children pullAll(String column, Collection<?> values);
default Children pullAll(boolean condition, String column, Object... values);
default Children pullAll(String column, Object... values);
@SuppressWarnings("unchecked") default Children pullAll(boolean condition, MutablePair<String, Collection<?>>... pullAllPair);
@SuppressWarnings("unchecked") default Children pullAll(MutablePair<String, Collection<?>>... pullAllPair);
```

## com.mongoplus.conditions.interfaces.update.array.operations.Push · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.array.operations.Push;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/array/operations/Push.java#L19)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.domain.MongoPlusException;
import com.mongoplus.options.PushOptions;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ClassTypeUtil;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children push(boolean condition, SFunction<T, Object> column, Object value);
default Children push(SFunction<T, Object> column, Object value);
default Children push(boolean condition, SFunction<T, Object> column, Object value, boolean each);
default Children push(SFunction<T, Object> column, Object value, boolean each);
default Children push(boolean condition, String column, Object value);
default Children push(String column, Object value);
default Children push(boolean condition, String column, Object value, boolean each);
default Children push(String column, Object value, boolean each);
default Children push(boolean condition, SFunction<T, Object> column, Object... value);
default Children push(SFunction<T, Object> column, Object... value);
default Children push(boolean condition, SFunction<T, Object> column, PushOptions options, Object... value);
default Children push(boolean condition, SFunction<T, Object> column, PushOptions options, Collection<?> value);
default Children push(SFunction<T, Object> column, PushOptions options, Object... value);
default Children push(SFunction<T, Object> column, PushOptions options, Collection<?> value);
default Children push(boolean condition, String column, Object... value);
default Children push(String column, Object... value);
default Children push(boolean condition, String column, PushOptions options, Object... value);
default Children push(String column, PushOptions options, Object... value);
default Children push(boolean condition, String column, PushOptions options, Collection<?> value);
default Children push(String column, PushOptions options, Collection<?> value);
default Children push(boolean condition, SFunction<T, Object> column, Collection<?> value);
default Children push(SFunction<T, Object> column, Collection<?> value);
default Children push(boolean condition, SFunction<T, Object> column, List<?> value, PushOptions options);
default Children push(SFunction<T, Object> column, List<?> value, PushOptions options);
default Children push(boolean condition, String column, List<?> value);
default Children push(String column, List<?> value);
default Children push(boolean condition, String column, List<?> value, PushOptions options);
default Children push(String column, List<?> value, PushOptions options);
```

## com.mongoplus.conditions.interfaces.update.BaseUpdateCondition · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/BaseUpdateCondition.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.BaseCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Children addUpdateCondition(ConditionMetaObject conditionMetaObject);
default ConditionMetaObject getBaseUpdateCompare(SFunction<T, Object> column, Object value, Object extraValue);
default ConditionMetaObject getBaseUpdateCompare(SFunction<T, Object> column, Object value);
default ConditionMetaObject getBaseUpdateCompare(Object value);
default ConditionMetaObject getBaseUpdateCompare(Object value, Object extraValue);
default ConditionMetaObject getBaseUpdateCompare(String column, Object value);
default ConditionMetaObject getBaseUpdateCompare(String column, Object value, Object extraValue);
```

## com.mongoplus.conditions.interfaces.update.field.Field · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.Field;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/Field.java#L10)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `CurrentDate<T, Children>, Inc<T, Children>, Max<T, Children>, Min<T, Children>, Mul<T, Children>, Rename<T, Children>, Set<T, Children>, SetOnInsert<T, Children>, Unset<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.field.operations.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.update.field.operations.CurrentDate · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.CurrentDate;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/CurrentDate.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.enums.CurrentDateType;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children currentDate(boolean condition, SFunction<T, Object> column);
default Children currentDate(SFunction<T, Object> column);
default Children currentDate(boolean condition, String column);
default Children currentDate(String column);
default Children currentDate(boolean condition, SFunction<T, Object> column, CurrentDateType currentDateType);
default Children currentDate(SFunction<T, Object> column, CurrentDateType currentDateType);
default Children currentDate(boolean condition, String column, CurrentDateType currentDateType);
default Children currentDate(String column, CurrentDateType currentDateType);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Holder · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Holder;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Holder.java#L8)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static Object isNull(Object source);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Inc · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Inc;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Inc.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children inc(boolean condition, SFunction<T, Object> column, Number value);
default Children inc(SFunction<T, Object> column, Number value);
default Children inc(boolean condition, String column, Number value);
default Children inc(String column, Number value);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Max · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Max;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Max.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children max(boolean condition, SFunction<T, Object> column, Object value);
default Children max(SFunction<T, Object> column, Object value);
default Children max(boolean condition, String column, Object value);
default Children max(String column, Object value);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Min · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Min;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Min.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children min(boolean condition, SFunction<T, Object> column, Object value);
default Children min(SFunction<T, Object> column, Object value);
default Children min(boolean condition, String column, Object value);
default Children min(String column, Object value);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Mul · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Mul;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Mul.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children mul(boolean condition, SFunction<T, Object> column, Number value);
default Children mul(SFunction<T, Object> column, Number value);
default Children mul(boolean condition, String column, Number value);
default Children mul(String column, Number value);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Rename · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Rename;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Rename.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.model.MutablePair;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children rename(boolean condition, String oldFieldName, String newFieldName);
default Children rename(String oldFieldName, String newFieldName);
default <O> Children rename(boolean condition, SFunction<O, Object> oldFieldName, String newFieldName);
default <O> Children rename(SFunction<O, Object> oldFieldName, String newFieldName);
default <O, N> Children rename(boolean condition, SFunction<O, Object> oldFieldName, SFunction<N, Object> newFieldName);
default <O, N> Children rename(SFunction<O, Object> oldFieldName, SFunction<N, Object> newFieldName);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Set · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Set;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Set.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children set(boolean condition, SFunction<T, Object> column, Object value);
default Children set(SFunction<T, Object> column, Object value);
default Children set(boolean condition, String column, Object value);
default Children set(String column, Object value);
default Children setNull(boolean condition, SFunction<T, Object> column);
default Children setNull(SFunction<T, Object> column);
default Children setNull(boolean condition, String column);
default Children setNull(String column);
```

## com.mongoplus.conditions.interfaces.update.field.operations.SetOnInsert · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.SetOnInsert;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/SetOnInsert.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children setOnInsert(boolean condition, SFunction<T, Object> column, Object value);
default Children setOnInsert(SFunction<T, Object> column, Object value);
default Children setOnInsert(boolean condition, String column, Object value);
default Children setOnInsert(String column, Object value);
```

## com.mongoplus.conditions.interfaces.update.field.operations.Unset · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.field.operations.Unset;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/field/operations/Unset.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseUpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.BaseUpdateCondition;
import com.mongoplus.support.SFunction;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@SuppressWarnings("unchecked") default Children unset(SFunction<T, Object>... columns);
@SuppressWarnings("unchecked") default Children unset(boolean condition, SFunction<T, Object>... columns);
default Children unset(String... columns);
default Children unset(boolean condition, String... columns);
default Children unset(List<String> columns);
default Children unset(boolean condition, List<String> columns);
```

## com.mongoplus.conditions.interfaces.update.UpdateCondition · mongo-plus-core

`import com.mongoplus.conditions.interfaces.update.UpdateCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/update/UpdateCondition.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `Array<T, Children>, Field<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.update.array.Array;
import com.mongoplus.conditions.interfaces.update.field.Field;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Children updateCustom(Bson bson);
```

## com.mongoplus.conditions.update.LambdaUpdateChainWrapper · mongo-plus-core

`import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/update/LambdaUpdateChainWrapper.java#L7)。类型：CLASS；类型参数 `T`；extends `UpdateChainWrapper<T, LambdaUpdateChainWrapper<T>>`；implements `ChainUpdate`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.DeleteOptions;
import com.mongodb.client.model.UpdateOptions;
import com.mongoplus.mapper.BaseMapper;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public LambdaUpdateChainWrapper(BaseMapper baseMapper, Class<T> clazz);
@Override public boolean update(UpdateOptions options);
@Override public boolean updateOne(UpdateOptions options);
@Override public boolean remove(DeleteOptions options);
@Override public boolean removeOne(DeleteOptions options);
```

## com.mongoplus.conditions.update.UpdateChainWrapper · mongo-plus-core

`import com.mongoplus.conditions.update.UpdateChainWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/update/UpdateChainWrapper.java#L21)。类型：CLASS；类型参数 `T, Children extends UpdateChainWrapper<T, Children>`；extends `AbstractChainWrapper<T, Children>`；implements `UpdateCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.interfaces.update.UpdateCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.MutablePair;
import org.bson.conversions.Bson;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public List<Bson> getUpdateBson();
public List<ConditionMetaObject> getUpdateCompareList();
@Override public synchronized void clear();
@Override public Children addUpdateCondition(ConditionMetaObject conditionMetaObject);
public MutablePair<BasicDBObject, BasicDBObject> buildUpdateCondition();
public MutablePair<BasicDBObject, BasicDBObject> buildUpdateCondition(Condition condition);
@Override public Children updateCustom(Bson bson);
@Override public BaseConditionResult buildCondition(Condition condition);
```

## com.mongoplus.conditions.update.UpdateWrapper · mongo-plus-core

`import com.mongoplus.conditions.update.UpdateWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/update/UpdateWrapper.java#L8)。类型：CLASS；类型参数 `T`；extends `UpdateChainWrapper<T, UpdateWrapper<T>>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.toolkit.ChainWrappers;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public UpdateChainWrapper<T, UpdateWrapper<T>> lambdaUpdate();
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

## com.mongoplus.model.Retry · mongo-plus-core

`import com.mongoplus.model.Retry;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/Retry.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.interceptor.Invocation;
import java.util.function.BiFunction;
import java.util.function.Consumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static RetryBuilder builder();
public Integer getMaxRetryNum();
public Long getRetryInterval();
public Boolean getProcessIntercept();
public Integer getAutoVersionNum();
public Boolean getAsyncRetry();
public Consumer<UpdateRetryResult> getHitRetry();
public Consumer<UpdateRetryResult> getOnSuccess();
public Consumer<UpdateRetryResult> getOnFailure();
public BiFunction<UpdateRetryResult, Invocation, Object> getFallback();
public void setMaxRetryNum(final Integer maxRetryNum);
public void setRetryInterval(final Long retryInterval);
public void setProcessIntercept(final Boolean processIntercept);
public void setAutoVersionNum(final Integer autoVersionNum);
public void setAsyncRetry(final Boolean asyncRetry);
public void setHitRetry(final Consumer<UpdateRetryResult> hitRetry);
public void setOnSuccess(final Consumer<UpdateRetryResult> onSuccess);
public void setOnFailure(final Consumer<UpdateRetryResult> onFailure);
public void setFallback(final BiFunction<UpdateRetryResult, Invocation, Object> fallback);
public boolean equals(final Object o);
public int hashCode();
public String toString();
```

## com.mongoplus.model.UpdateRetryResult · mongo-plus-core

`import com.mongoplus.model.UpdateRetryResult;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/UpdateRetryResult.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public UpdateRetryResult(Object result, Integer currentRetryNum, Object[] args, Retry retry);
public Object getResult();
public Integer getCurrentRetryNum();
public Retry getRetry();
public Object[] getArgs();
public boolean isUpdateResult();
public boolean isBulkWriteResult();
public UpdateResult getUpdateResult();
public BulkWriteResult getBulkWriteResult();
public long getModifiedCount();
@Override public String toString();
```

## com.mongoplus.options.PushOptions · mongo-plus-core

`import com.mongoplus.options.PushOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/options/PushOptions.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.Nullable;
import org.bson.Document;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Nullable public Integer getPosition();
public PushOptions position(@Nullable final Integer position);
@Nullable public Integer getSlice();
public PushOptions slice(@Nullable final Integer slice);
@Nullable public Integer getSort();
public PushOptions sort(@Nullable final Integer sort);
@Nullable public Document getSortDocument();
public PushOptions sortDocument(@Nullable final Document sortDocument);
@Override public boolean equals(final Object o);
@Override public int hashCode();
@Override public String toString();
```
