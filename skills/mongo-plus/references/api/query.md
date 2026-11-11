# 当前公开API签名：query

这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。

## com.mongoplus.conditions.interfaces.query.array.Array · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.array.Array;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/array/Array.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `All<T, Children>, ElemMatch<T, Children>, Size<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.array.operations.All;
import com.mongoplus.conditions.interfaces.query.array.operations.ElemMatch;
import com.mongoplus.conditions.interfaces.query.array.operations.Size;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.query.array.operations.All · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.array.operations.All;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/array/operations/All.java#L14)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children all(boolean condition, SFunction<T, Object> column, Collection<?> value);
default Children all(SFunction<T, Object> column, Collection<?> value);
default Children all(boolean condition, String column, Collection<?> value);
default Children all(String column, Collection<?> value);
```

## com.mongoplus.conditions.interfaces.query.array.operations.ElemMatch · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.array.operations.ElemMatch;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/array/operations/ElemMatch.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children elemMatch(boolean condition, SFunction<T, Object> column, Wrapper<?> queryWrapper);
default Children elemMatch(SFunction<T, Object> column, Wrapper<?> queryWrapper);
default Children elemMatch(boolean condition, String column, Wrapper<?> queryWrapper);
default Children elemMatch(String column, Wrapper<?> queryWrapper);
```

## com.mongoplus.conditions.interfaces.query.array.operations.Size · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.array.operations.Size;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/array/operations/Size.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children size(boolean condition, SFunction<T, ?> fieldName, int size);
default Children size(SFunction<T, ?> fieldName, int size);
default Children size(boolean condition, String fieldName, int size);
default Children size(String fieldName, int size);
```

## com.mongoplus.conditions.interfaces.query.BaseQueryCondition · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/BaseQueryCondition.java#L23)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.BaseCondition;
import com.mongoplus.model.Projection;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.model.Order;
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.ObjectIdUtil;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Children addCondition(ConditionMetaObject conditionMetaObject);
Children addCondition(List<Projection> projections);
Children addCondition(Order order);
default ConditionMetaObject getBaseCondition(String column, Object value);
default ConditionMetaObject getBaseConditionExtraValue(String column, Object value, Object extraValue);
default ConditionMetaObject getBaseCondition(String condition, String column, Object value, Class<?> clazz, Field field, Object extraValue);
default ConditionMetaObject getBaseCondition(SFunction<T, ?> column, Object value);
default ConditionMetaObject getBaseConditionExtraValue(SFunction<T, ?> column, Object value, Object extraValue);
default ConditionMetaObject getBaseCondition(String methodName, SFunction<T, ?> column, Object value);
default ConditionMetaObject getBaseCondition(Wrapper<?> queryWrapper);
default ConditionMetaObject getBaseCondition(Object value);
default ConditionMetaObject getBaseConditionExtraValue(Object value, Object extraValue);
```

## com.mongoplus.conditions.interfaces.query.bitwise.Bitwise · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.bitwise.Bitwise;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/bitwise/Bitwise.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BitsAllSet<T, Children>, BitsAnySet<T, Children>, BitsAllClear<T, Children>, BitsAnyClear<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllClear;
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllSet;
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnyClear;
import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnySet;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllClear · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllClear;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/bitwise/operations/BitsAllClear.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children bitsAllClear(boolean condition, SFunction<T, ?> fieldName, long bitmask);
default Children bitsAllClear(SFunction<T, ?> fieldName, long bitmask);
default Children bitsAllClear(boolean condition, String fieldName, long bitmask);
default Children bitsAllClear(String fieldName, long bitmask);
```

## com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllSet · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAllSet;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/bitwise/operations/BitsAllSet.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children bitsAllSet(boolean condition, SFunction<T, ?> fieldName, long bitmask);
default Children bitsAllSet(SFunction<T, ?> fieldName, long bitmask);
default Children bitsAllSet(boolean condition, String fieldName, long bitmask);
default Children bitsAllSet(String fieldName, long bitmask);
```

## com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnyClear · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnyClear;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/bitwise/operations/BitsAnyClear.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children bitsAnyClear(boolean condition, SFunction<T, ?> fieldName, long bitmask);
default Children bitsAnyClear(SFunction<T, ?> fieldName, long bitmask);
default Children bitsAnyClear(boolean condition, String fieldName, long bitmask);
default Children bitsAnyClear(String fieldName, long bitmask);
```

## com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnySet · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.bitwise.operations.BitsAnySet;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/bitwise/operations/BitsAnySet.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children bitsAnySet(boolean condition, SFunction<T, ?> fieldName, long bitmask);
default Children bitsAnySet(SFunction<T, ?> fieldName, long bitmask);
default Children bitsAnySet(boolean condition, String fieldName, long bitmask);
default Children bitsAnySet(String fieldName, long bitmask);
```

## com.mongoplus.conditions.interfaces.query.compare.Compare · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.Compare;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/Compare.java#L10)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `Eq<T, Children>, Gt<T, Children>, Gte<T, Children>, In<T, Children>, Lt<T, Children>, Lte<T, Children>, Ne<T, Children>, Nin<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.compare.operations.*;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children between(boolean condition, SFunction<T, Object> column, Object gte, Object lte, boolean convertGtOrLt);
default Children between(SFunction<T, Object> column, Object gte, Object lte, boolean convertGtOrLt);
default Children between(boolean condition, String column, Object gte, Object lte, boolean convertGtOrLt);
default Children between(String column, Object gte, Object lte, boolean convertGtOrLt);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Eq · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Eq;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Eq.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children eq(boolean condition, SFunction<T, Object> column, Object value);
default Children eq(SFunction<T, Object> column, Object value);
default Children eq(boolean condition, String column, Object value);
default Children eq(String column, Object value);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Gt · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Gt;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Gt.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children gt(boolean condition, SFunction<T, Object> column, Object value);
default Children gt(SFunction<T, Object> column, Object value);
default Children gt(boolean condition, String column, Object value);
default Children gt(String column, Object value);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Gte · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Gte;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Gte.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children gte(boolean condition, SFunction<T, Object> column, Object value);
default Children gte(SFunction<T, Object> column, Object value);
default Children gte(boolean condition, String column, Object value);
default Children gte(String column, Object value);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.In · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.In;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/In.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children in(boolean condition, SFunction<T, Object> column, Collection<?> valueList);
default Children in(SFunction<T, Object> column, Collection<?> valueList);
@SuppressWarnings("unchecked") default <TItem> Children in(boolean condition, SFunction<T, Object> column, TItem... values);
@SuppressWarnings("unchecked") default <TItem> Children in(SFunction<T, Object> column, TItem... values);
default Children in(boolean condition, String column, Collection<?> valueList);
default Children in(String column, Collection<?> valueList);
@SuppressWarnings("unchecked") default <TItem> Children in(boolean condition, String column, TItem... values);
@SuppressWarnings("unchecked") default <TItem> Children in(String column, TItem... values);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Lt · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Lt;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Lt.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children lt(boolean condition, SFunction<T, Object> column, Object value);
default Children lt(SFunction<T, Object> column, Object value);
default Children lt(boolean condition, String column, Object value);
default Children lt(String column, Object value);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Lte · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Lte;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Lte.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children lte(boolean condition, SFunction<T, Object> column, Object value);
default Children lte(SFunction<T, Object> column, Object value);
default Children lte(boolean condition, String column, Object value);
@SuppressWarnings("UnusedReturnValue") default Children lte(String column, Object value);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Ne · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Ne;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Ne.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children ne(boolean condition, SFunction<T, Object> column, Object value);
default Children ne(SFunction<T, Object> column, Object value);
default Children ne(boolean condition, String column, Object value);
default Children ne(String column, Object value);
```

## com.mongoplus.conditions.interfaces.query.compare.operations.Nin · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.compare.operations.Nin;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/compare/operations/Nin.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children nin(boolean condition, SFunction<T, Object> column, Collection<?> valueList);
default Children nin(SFunction<T, Object> column, Collection<?> valueList);
@SuppressWarnings("unchecked") default <TItem> Children nin(boolean condition, SFunction<T, Object> column, TItem... values);
@SuppressWarnings("unchecked") default <TItem> Children nin(SFunction<T, Object> column, TItem... values);
default Children nin(boolean condition, String column, Collection<?> valueList);
@SuppressWarnings("unchecked") default <TItem> Children nin(boolean condition, String column, TItem... values);
@SuppressWarnings("unchecked") default <TItem> Children nin(String column, TItem... values);
default Children nin(String column, Collection<?> valueList);
```

## com.mongoplus.conditions.interfaces.query.condition.Condition · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.condition.Condition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/condition/Condition.java#L7)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.model.Order;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public List<ConditionMetaObject> getCompareConditionList();
public List<Order> getOrderList();
public void setCompareConditionList(List<ConditionMetaObject> conditionMetaObjectList);
public void setOrderList(List<Order> orderList);
public boolean equals(Object o);
public int hashCode();
public String toString();
public Condition(List<ConditionMetaObject> conditionMetaObjectList, List<Order> orderList);
public Condition();
```

## com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/condition/ConditionMetaObject.java#L13)。类型：CLASS；类型参数 ``；extends `null`；implements `Serializable`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static CompareConditionBuilder builder();
public String getCondition();
public String getColumn();
public Object getValue();
@SuppressWarnings("unchecked") public <T> T getValue(Class<T> clazz);
public void setCondition(String condition);
public void setColumn(String column);
public void setValue(Object value);
public Class<?> getOriginalClass();
public void setOriginalClass(Class<?> originalClass);
public Field getOriginalField();
public void setOriginalField(Field originalField);
public Object getExtraValue();
@SuppressWarnings("unchecked") public <T> T getExtraValue(Class<T> clazz);
public void setExtraValue(Object extraValue);
@Override public boolean equals(Object object);
@Override public int hashCode();
@Override public String toString();
public ConditionMetaObject(String condition, String column, Object value, Class<?> originalClass, Field originalField);
public ConditionMetaObject(String condition, String column, Object value, Class<?> originalClass, Field originalField, Object extraValue);
public ConditionMetaObject(String condition, Object value, Class<?> originalClass, Field originalField);
public ConditionMetaObject(String condition, Object value, Class<?> originalClass, Field originalField, Object extraValue);
public ConditionMetaObject(String condition, List<ConditionMetaObject> conditionMetaObjectList);
public ConditionMetaObject();
```

## com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject.CompareConditionBuilder · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject.CompareConditionBuilder;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/condition/ConditionMetaObject.java#L175)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public CompareConditionBuilder condition(String condition);
public CompareConditionBuilder column(String column);
public CompareConditionBuilder value(Object value);
public CompareConditionBuilder originalClass(Class<?> originalClass);
public CompareConditionBuilder originalField(Field originalField);
public ConditionMetaObject build();
@Override public String toString();
```

## com.mongoplus.conditions.interfaces.query.data.DataType · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.data.DataType;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/data/DataType.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `Exists<T, Children>, Type<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.data.operations.Exists;
import com.mongoplus.conditions.interfaces.query.data.operations.Type;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.query.data.operations.Exists · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.data.operations.Exists;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/data/operations/Exists.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children exists(boolean condition, SFunction<T, Object> column, Boolean value);
default Children exists(SFunction<T, Object> column, Boolean value);
default Children exists(boolean condition, String column, Boolean value);
default Children exists(String column, Boolean value);
```

## com.mongoplus.conditions.interfaces.query.data.operations.Type · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.data.operations.Type;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/data/operations/Type.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.TypeEnum;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children type(boolean condition, SFunction<T, Object> column, TypeEnum value);
default Children type(SFunction<T, Object> column, TypeEnum value);
default Children type(boolean condition, String column, TypeEnum value);
default Children type(String column, TypeEnum value);
default Children type(boolean condition, SFunction<T, Object> column, String value);
default Children type(SFunction<T, Object> column, String value);
default Children type(boolean condition, String column, String value);
default Children type(String column, String value);
default Children type(boolean condition, SFunction<T, Object> column, Integer value);
default Children type(SFunction<T, Object> column, Integer value);
default Children type(boolean condition, String column, Integer value);
default Children type(String column, Integer value);
```

## com.mongoplus.conditions.interfaces.query.geo.GeoSpatial · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.GeoSpatial;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/GeoSpatial.java#L10)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `GeoIntersects<T, Children>, GeoWithin<T, Children>, GeoWithinBox<T, Children>, GeoWithinCenter<T, Children>, GeoWithinCenterSphere<T, Children>, GeoWithinPolygon<T, Children>, Near<T, Children>, NearSphere<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.geo.operations.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.query.geo.operations.GeoIntersects · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.GeoIntersects;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/GeoIntersects.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children geoIntersects(boolean condition, String fieldName, Geometry geometry);
default Children geoIntersects(String fieldName, Geometry geometry);
default Children geoIntersects(boolean condition, SFunction<T, ?> fieldName, Geometry geometry);
default Children geoIntersects(SFunction<T, ?> fieldName, Geometry geometry);
default Children geoIntersects(boolean condition, String fieldName, Bson geometry);
default Children geoIntersects(String fieldName, Bson geometry);
default Children geoIntersects(boolean condition, SFunction<T, ?> fieldName, Bson geometry);
default Children geoIntersects(SFunction<T, ?> fieldName, Bson geometry);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithin · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithin;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/GeoWithin.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Geometry;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children geoWithin(boolean condition, String fieldName, Geometry geometry);
default Children geoWithin(String fieldName, Geometry geometry);
default Children geoWithin(boolean condition, SFunction<T, ?> fieldName, Geometry geometry);
default Children geoWithin(SFunction<T, ?> fieldName, Geometry geometry);
default Children geoWithin(boolean condition, String fieldName, Bson geometry);
default Children geoWithin(String fieldName, Bson geometry);
default Children geoWithin(boolean condition, SFunction<T, ?> fieldName, Bson geometry);
default Children geoWithin(SFunction<T, ?> fieldName, Bson geometry);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinBox · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinBox;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/GeoWithinBox.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.GeoBox;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children geoWithinBox(boolean condition, String fieldName, double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY);
default Children geoWithinBox(String fieldName, double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY);
default Children geoWithinBox(boolean condition, SFunction<T, ?> fieldName, double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY);
default Children geoWithinBox(SFunction<T, ?> fieldName, double lowerLeftX, double lowerLeftY, double upperRightX, double upperRightY);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinCenter · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinCenter;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/GeoWithinCenter.java#L14)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.GeoCenter;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children geoWithinCenter(boolean condition, String fieldName, double x, double y, double radius);
default Children geoWithinCenter(String fieldName, double x, double y, double radius);
default Children geoWithinCenter(boolean condition, SFunction<T, ?> fieldName, double x, double y, double radius);
default Children geoWithinCenter(SFunction<T, ?> fieldName, double x, double y, double radius);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinCenterSphere · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinCenterSphere;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/GeoWithinCenterSphere.java#L14)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.GeoCenter;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children geoWithinCenterSphere(boolean condition, String fieldName, double x, double y, double radius);
default Children geoWithinCenterSphere(String fieldName, double x, double y, double radius);
default Children geoWithinCenterSphere(boolean condition, SFunction<T, ?> fieldName, double x, double y, double radius);
default Children geoWithinCenterSphere(SFunction<T, ?> fieldName, double x, double y, double radius);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinPolygon · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.GeoWithinPolygon;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/GeoWithinPolygon.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children geoWithinPolygon(boolean condition, String fieldName, List<List<Double>> points);
default Children geoWithinPolygon(String fieldName, List<List<Double>> points);
default Children geoWithinPolygon(boolean condition, SFunction<T, ?> fieldName, List<List<Double>> points);
default Children geoWithinPolygon(SFunction<T, ?> fieldName, List<List<Double>> points);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.Near · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.Near;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/Near.java#L16)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Point;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.Coordinate;
import com.mongoplus.model.geo.GeoNear;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children near(boolean condition, String fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(String fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(boolean condition, SFunction<T, ?> fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(SFunction<T, ?> fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(boolean condition, String fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(String fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(boolean condition, SFunction<T, ?> fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(SFunction<T, ?> fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(boolean condition, String fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(String fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(boolean condition, SFunction<T, ?> fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children near(SFunction<T, ?> fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
```

## com.mongoplus.conditions.interfaces.query.geo.operations.NearSphere · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.geo.operations.NearSphere;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/geo/operations/NearSphere.java#L16)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.geojson.Point;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.geo.Coordinate;
import com.mongoplus.model.geo.GeoNear;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children nearSphere(boolean condition, String fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(String fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(boolean condition, SFunction<T, ?> fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(SFunction<T, ?> fieldName, Point geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(boolean condition, String fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(String fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(boolean condition, SFunction<T, ?> fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(SFunction<T, ?> fieldName, Bson geometry, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(boolean condition, String fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(String fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(boolean condition, SFunction<T, ?> fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
default Children nearSphere(SFunction<T, ?> fieldName, double x, double y, @Nullable Double maxDistance, @Nullable Double minDistance);
```

## com.mongoplus.conditions.interfaces.query.logic.Logic · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.logic.Logic;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/logic/Logic.java#L16)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `And<T, Children>, Nor<T, Children>, Not<T, Children>, Or<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.logic.operations.And;
import com.mongoplus.conditions.interfaces.query.logic.operations.Nor;
import com.mongoplus.conditions.interfaces.query.logic.operations.Not;
import com.mongoplus.conditions.interfaces.query.logic.operations.Or;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children combine(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children combine(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children combine(Wrapper<?> queryWrapper);
default Children combine(boolean condition, Wrapper<?> queryWrapper);
```

## com.mongoplus.conditions.interfaces.query.logic.operations.And · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.logic.operations.And;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/logic/operations/And.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children and(Wrapper<?> queryWrapper);
default Children and(boolean condition, Wrapper<?> queryWrapper);
default Children and(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children and(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
```

## com.mongoplus.conditions.interfaces.query.logic.operations.Nor · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.logic.operations.Nor;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/logic/operations/Nor.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children nor(boolean condition, Wrapper<?> queryWrapper);
default Children nor(Wrapper<?> queryWrapper);
default Children nor(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children nor(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
```

## com.mongoplus.conditions.interfaces.query.logic.operations.Not · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.logic.operations.Not;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/logic/operations/Not.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children not(ConditionMetaObject conditionMetaObject);
default Children not(boolean condition, ConditionMetaObject conditionMetaObject);
default Children not(boolean condition, Wrapper<?> queryWrapper);
default Children not(Wrapper<?> queryWrapper);
default Children not(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children not(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
```

## com.mongoplus.conditions.interfaces.query.logic.operations.Or · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.logic.operations.Or;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/logic/operations/Or.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children or(boolean condition, Wrapper<?> queryWrapper);
default Children or(Wrapper<?> queryWrapper);
default Children or(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children or(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
```

## com.mongoplus.conditions.interfaces.query.order.Ordered · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.order.Ordered;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/order/Ordered.java#L13)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.OrderEnum;
import com.mongoplus.model.Order;
import com.mongoplus.support.SFunction;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children order(SFunction<T, Object> column, Integer order);
default Children order(String column, Integer order);
default Children orderByAsc(SFunction<T, Object> column);
default Children orderByDesc(SFunction<T, Object> column);
default Children orderByAsc(String column);
default Children orderByDesc(String column);
```

## com.mongoplus.conditions.interfaces.query.other.operations.Expr · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.operations.Expr;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/operations/Expr.java#L23)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.conditions.Wrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.enums.QueryOperatorEnum;
import com.mongoplus.support.SFunction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children expr(ConditionMetaObject conditionMetaObject);
default Children expr(boolean condition, ConditionMetaObject conditionMetaObject);
default Children expr(boolean condition, Wrapper<?> queryWrapper);
default Children expr(Wrapper<?> queryWrapper);
default Children expr(SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
default Children expr(boolean condition, SFunction<QueryWrapper<T>, QueryWrapper<T>> function);
```

## com.mongoplus.conditions.interfaces.query.other.operations.JsonSchema · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.operations.JsonSchema;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/operations/JsonSchema.java#L8)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.query.other.operations.Like · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.operations.Like;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/operations/Like.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.RegexOptions;
import com.mongoplus.support.SFunction;
import java.util.regex.Pattern;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children like(boolean condition, SFunction<T, Object> column, Object value);
default Children like(SFunction<T, Object> column, Object value);
default Children like(boolean condition, SFunction<T, Object> column, Object value, RegexOptions options);
default Children like(SFunction<T, Object> column, Object value, RegexOptions options);
default Children like(boolean condition, String column, Object value);
default Children like(String column, Object value);
default Children like(boolean condition, String column, Object value, RegexOptions options);
default Children like(String column, Object value, RegexOptions options);
default Children likeLeft(boolean condition, SFunction<T, Object> column, Object value);
default Children likeLeft(SFunction<T, Object> column, Object value);
default Children likeLeft(boolean condition, SFunction<T, Object> column, Object value, RegexOptions options);
default Children likeLeft(SFunction<T, Object> column, Object value, RegexOptions options);
default Children likeLeft(boolean condition, String column, Object value);
default Children likeLeft(String column, Object value);
default Children likeLeft(boolean condition, String column, Object value, RegexOptions options);
default Children likeLeft(String column, Object value, RegexOptions options);
default Children likeRight(boolean condition, SFunction<T, Object> column, Object value);
default Children likeRight(SFunction<T, Object> column, Object value);
default Children likeRight(boolean condition, SFunction<T, Object> column, Object value, RegexOptions options);
default Children likeRight(SFunction<T, Object> column, Object value, RegexOptions options);
default Children likeRight(boolean condition, String column, Object value);
default Children likeRight(String column, Object value);
default Children likeRight(boolean condition, String column, Object value, RegexOptions options);
default Children likeRight(String column, Object value, RegexOptions options);
```

## com.mongoplus.conditions.interfaces.query.other.operations.Mod · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.operations.Mod;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/operations/Mod.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.support.SFunction;
import java.util.Arrays;
import java.util.Collection;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children mod(boolean condition, SFunction<T, Object> column, long divide, long remain);
default Children mod(SFunction<T, Object> column, long divide, long remain);
default Children mod(boolean condition, SFunction<T, Object> column, Collection<Long> value);
default Children mod(SFunction<T, Object> column, Collection<Long> value);
default Children mod(boolean condition, String column, long divide, long remain);
default Children mod(String column, long divide, long remain);
default Children mod(boolean condition, String column, Collection<Long> value);
default Children mod(String column, Collection<Long> value);
```

## com.mongoplus.conditions.interfaces.query.other.operations.Regex · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.operations.Regex;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/operations/Regex.java#L15)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.enums.RegexOptions;
import com.mongoplus.support.SFunction;
import java.util.regex.Pattern;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children regex(boolean condition, SFunction<T, Object> column, Object value);
default Children regex(SFunction<T, Object> column, Object value);
default Children regex(boolean condition, String column, Object value);
default Children regex(String column, Object value);
default Children regex(boolean condition, SFunction<T, Object> column, Object value, RegexOptions options);
default Children regex(SFunction<T, Object> column, Object value, RegexOptions options);
default Children regex(boolean condition, String column, Object value, RegexOptions options);
default Children regex(String column, Object value, RegexOptions options);
```

## com.mongoplus.conditions.interfaces.query.other.operations.Where · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.operations.Where;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/operations/Where.java#L11)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children where(String javaScriptExpression);
```

## com.mongoplus.conditions.interfaces.query.other.Other · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.other.Other;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/other/Other.java#L10)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `Expr<T, Children>, JsonSchema<T, Children>, Like<T, Children>, Mod<T, Children>, Regex<T, Children>, Where<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.other.operations.*;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
(无直接声明的公开方法；检查继承接口)
```

## com.mongoplus.conditions.interfaces.query.project.Project · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.project.Project;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/project/Project.java#L18)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.model.Projection;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.enums.ProjectionEnum;
import com.mongoplus.support.SFunction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children project(Projection... projections);
default Children project(List<Projection> projectionList);
default Children projectDisplay(SFunction<T, Object>... columns);
default Children projectDisplay(String... columns);
default Children projectNone(SFunction<T, Object>... column);
default Children projectNone(String... columns);
default Children project(boolean displayId, Projection... projections);
default Children projectDisplay(boolean displayId, SFunction<T, Object>... columns);
default Children projectDisplay(boolean displayId, String... columns);
default Children projectNone(boolean displayId, SFunction<T, Object>... columns);
default Children projectNone(boolean displayId, String... column);
```

## com.mongoplus.conditions.interfaces.query.QueryCondition · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.QueryCondition;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/QueryCondition.java#L23)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `Array<T, Children>, Bitwise<T, Children>, Compare<T, Children>, DataType<T, Children>, GeoSpatial<T, Children>, Logic<T, Children>, Text<T, Children>, Other<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.bson.MongoPlusBasicDBObject;
import com.mongoplus.conditions.interfaces.query.array.Array;
import com.mongoplus.conditions.interfaces.query.bitwise.Bitwise;
import com.mongoplus.conditions.interfaces.query.compare.Compare;
import com.mongoplus.conditions.interfaces.query.data.DataType;
import com.mongoplus.conditions.interfaces.query.geo.GeoSpatial;
import com.mongoplus.conditions.interfaces.query.logic.Logic;
import com.mongoplus.conditions.interfaces.query.other.Other;
import com.mongoplus.conditions.interfaces.query.text.Text;
import com.mongoplus.support.SFunction;
import org.bson.conversions.Bson;
import java.util.List;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
Children custom(BasicDBObject basicDBObject);
Children custom(Bson bson);
Children custom(MongoPlusBasicDBObject mongoPlusBasicDBObject);
Children custom(SFunction<MongoPlusBasicDBObject, MongoPlusBasicDBObject> function);
Children custom(List<BasicDBObject> basicDBObjectList);
```

## com.mongoplus.conditions.interfaces.query.text.Text · mongo-plus-core

`import com.mongoplus.conditions.interfaces.query.text.Text;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/query/text/Text.java#L12)。类型：INTERFACE；类型参数 `T, Children`；extends `null`；implements `BaseQueryCondition<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.interfaces.query.BaseQueryCondition;
import com.mongoplus.options.TextSearchOptions;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default Children text(boolean condition, Object value);
default Children text(Object value);
default Children text(boolean condition, Object value, TextSearchOptions textSearchOptions);
default Children text(Object value, TextSearchOptions textSearchOptions);
```

## com.mongoplus.conditions.interfaces.TextSearchOptions · mongo-plus-core

`import com.mongoplus.conditions.interfaces.TextSearchOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/interfaces/TextSearchOptions.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Nullable public String getLanguage();
public TextSearchOptions language(@Nullable final String language);
@Nullable public Boolean getCaseSensitive();
public TextSearchOptions caseSensitive(@Nullable final Boolean caseSensitive);
@Nullable public Boolean getDiacriticSensitive();
public TextSearchOptions diacriticSensitive(@Nullable final Boolean diacriticSensitive);
public com.mongodb.client.model.TextSearchOptions to();
```

## com.mongoplus.conditions.query.LambdaQueryChainWrapper · mongo-plus-core

`import com.mongoplus.conditions.query.LambdaQueryChainWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/query/LambdaQueryChainWrapper.java#L15)。类型：CLASS；类型参数 `T`；extends `QueryChainWrapper<T, LambdaQueryChainWrapper<T>>`；implements `ChainQuery<T>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.client.model.FindOneAndDeleteOptions;
import com.mongoplus.mapper.BaseMapper;
import com.mongoplus.model.PageParam;
import com.mongoplus.model.PageResult;
import java.util.List;
import java.util.function.Consumer;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public LambdaQueryChainWrapper(BaseMapper baseMapper, Class<T> clazz);
@Override public List<T> list();
@Override public <R> List<R> list(Class<R> rClazz);
@Override public T one();
@Override public <R> R one(Class<R> rClazz);
@Override public T findOneAndRemove();
@Override public <R> R findOneAndRemove(Class<R> rClazz);
@Override public T findOneAndRemove(Consumer<FindOneAndDeleteOptions> optionsConfigurer);
@Override public <R> R findOneAndRemove(Consumer<FindOneAndDeleteOptions> optionsConfigurer, Class<R> rClazz);
@Override public PageResult<T> page(PageParam pageParam);
@Override public <R> PageResult<R> page(PageParam pageParam, Class<R> rClazz);
@Override public PageResult<T> page(Integer pageNum, Integer pageSize);
@Override public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Class<R> rClazz);
@Override public PageResult<T> page(PageParam pageParam, Integer recentPageNum);
@Override public <R> PageResult<R> page(PageParam pageParam, Integer recentPageNum, Class<R> rClazz);
@Override public PageResult<T> page(Integer pageNum, Integer pageSize, Integer recentPageNum);
@Override public <R> PageResult<R> page(Integer pageNum, Integer pageSize, Integer recentPageNum, Class<R> rClazz);
@Override public long count();
```

## com.mongoplus.conditions.query.QueryChainWrapper · mongo-plus-core

`import com.mongoplus.conditions.query.QueryChainWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/query/QueryChainWrapper.java#L13)。类型：CLASS；类型参数 `T, Children extends QueryChainWrapper<T, Children>`；extends `AbstractChainWrapper<T, Children>`；implements `Project<T, Children>, Ordered<T, Children>`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.conditions.AbstractChainWrapper;
import com.mongoplus.conditions.interfaces.query.order.Ordered;
import com.mongoplus.conditions.interfaces.query.project.Project;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Override public BaseConditionResult buildCondition(Condition condition);
```

## com.mongoplus.conditions.query.QueryWrapper · mongo-plus-core

`import com.mongoplus.conditions.query.QueryWrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/query/QueryWrapper.java#L8)。类型：CLASS；类型参数 `T`；extends `QueryChainWrapper<T, QueryWrapper<T>>`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.toolkit.ChainWrappers;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public QueryChainWrapper<T, QueryWrapper<T>> lambdaQuery();
```

## com.mongoplus.conditions.Wrapper · mongo-plus-core

`import com.mongoplus.conditions.Wrapper;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/conditions/Wrapper.java#L20)。类型：CLASS；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongoplus.conditions.interfaces.query.condition.ConditionMetaObject;
import com.mongoplus.handlers.condition.Condition;
import com.mongoplus.model.BaseConditionResult;
import com.mongoplus.model.ConditionModel;
import com.mongoplus.model.Order;
import com.mongoplus.model.Projection;
import java.util.List;
import static com.mongoplus.handlers.condition.BuildCondition.condition;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public List<ConditionMetaObject> getConditionMetaObjects();
public void addConditionMetaObject(ConditionMetaObject conditionMetaObject);
public List<Order> getOrderList();
public void addOrder(Order order);
public List<Projection> getProjectionList();
public void addProjection(List<Projection> projection);
public List<BasicDBObject> getBasicDBObjectList();
public void addBasicDBObject(BasicDBObject basicDBObject);
public void addBasicDBObject(List<BasicDBObject> basicDBObjects);
public synchronized void clear();
public abstract T getEntity();
public BaseConditionResult buildCondition();
public abstract BaseConditionResult buildCondition(Condition condition);
public boolean isNotEmpty();
public abstract boolean isNotEmpty(Condition condition);
public ConditionModel getConditionModel();
```

## com.mongoplus.function.FieldChain · mongo-plus-core

`import com.mongoplus.function.FieldChain;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/function/FieldChain.java#L11)。类型：CLASS；类型参数 `T`；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.FunctionUtil;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T, R> FieldChain<R> of(SFunction<T, R> root);
@SuppressWarnings("unchecked") public <R> FieldChain<R> then(SFunction<T, R> function);
public FieldChain<T> then(String fieldName);
public FieldChain<T> then(Class<?> from);
public String build();
public String build(boolean isOption);
public String buildOption();
```

## com.mongoplus.function.FreeFieldChain · mongo-plus-core

`import com.mongoplus.function.FreeFieldChain;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/function/FreeFieldChain.java#L10)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.support.SFunction;
import com.mongoplus.toolkit.FunctionUtil;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static FreeFieldChain create();
public <T> FreeFieldChain append(SFunction<T, ?> function);
public FreeFieldChain append(String fieldName);
public FreeFieldChain append(Class<?> from);
public String build();
public String build(boolean isOption);
public String buildOption();
```

## com.mongoplus.options.TextSearchOptions · mongo-plus-core

`import com.mongoplus.options.TextSearchOptions;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/options/TextSearchOptions.java#L11)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.toolkit.StringUtils;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
@Nullable public String getLanguage();
public TextSearchOptions language(@Nullable final String language);
@Nullable public Boolean getCaseSensitive();
public TextSearchOptions caseSensitive(@Nullable final Boolean caseSensitive);
@Nullable public Boolean getDiacriticSensitive();
public TextSearchOptions diacriticSensitive(@Nullable final Boolean diacriticSensitive);
public com.mongodb.client.model.TextSearchOptions to();
```

## com.mongoplus.support.SFunction · mongo-plus-core

`import com.mongoplus.support.SFunction;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/support/SFunction.java#L21)。类型：INTERFACE；类型参数 `T, R`；extends `null`；implements `Function<T, R>, Serializable`。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.annotation.ID;
import com.mongoplus.annotation.collection.CollectionField;
import com.mongoplus.cache.global.PropertyCache;
import com.mongoplus.constant.SqlOperationConstant;
import com.mongoplus.toolkit.StringUtils;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
default String getFieldName();
default String getFieldNameLineOption();
default String getFieldNameLine();
default String getFieldName(SFunction<T, ?> fn, String split);
default String getFieldName(SFunction<T, ?> fn, String split, Integer toType);
default Class<?> getImplClass();
default String getMethodName();
default Class<?> getFieldClass();
default SerializedLambda getSerializedLambda();
default SerializedLambda getSerializedLambdaOne(SFunction<T, ?> fn);
default Class<?> getReturnType();
default Field getField();
default Field getField(SFunction<T, ?> fn);
default String getFieldName(SFunction<T, ?> fn);
```

## com.mongoplus.toolkit.ChainWrappers · mongo-plus-core

`import com.mongoplus.toolkit.ChainWrappers;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/toolkit/ChainWrappers.java#L16)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongoplus.aggregate.LambdaAggregateChainWrapper;
import com.mongoplus.conditions.query.LambdaQueryChainWrapper;
import com.mongoplus.conditions.query.QueryChainWrapper;
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.LambdaUpdateChainWrapper;
import com.mongoplus.conditions.update.UpdateChainWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;
import com.mongoplus.mapper.BaseMapper;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <T> LambdaQueryChainWrapper<T> lambdaQueryChain(BaseMapper baseMapper, Class<T> clazz);
public static <T> LambdaAggregateChainWrapper<T> lambdaAggregatesChain(BaseMapper baseMapper, Class<T> clazz);
public static <T> LambdaUpdateChainWrapper<T> lambdaUpdateChain(BaseMapper baseMapper, Class<T> clazz);
public static <T> UpdateChainWrapper<T, UpdateWrapper<T>> lambdaUpdateChain();
public static <T> QueryChainWrapper<T, QueryWrapper<T>> lambdaQueryChain();
```

## com.mongoplus.toolkit.Filters · mongo-plus-core

`import com.mongoplus.toolkit.Filters;`

[声明源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/toolkit/Filters.java#L26)。类型：CLASS；类型参数 ``；extends `null`；implements ``。

参数类型的原声明import（含Driver值类型，通配符按对应包解析）：

```text
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.TextSearchOptions;
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.Point;
import com.mongoplus.annotation.comm.Nullable;
import com.mongoplus.enums.SpecialConditionEnum;
import org.bson.*;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;
import java.util.*;
import java.util.regex.Pattern;
import static com.mongodb.assertions.Assertions.notNull;
import static com.mongoplus.toolkit.BuildersHelper.encodeValue;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
```

公开声明签名（interface成员隐含public；static/default保留）：

```text
public static <TItem> Bson eq(@Nullable final TItem value);
public static <TItem> Bson eq(final String fieldName, @Nullable final TItem value);
public static <TItem> Bson ne(final String fieldName, @Nullable final TItem value);
public static <TItem> Bson gt(final String fieldName, final TItem value);
public static <TItem> Bson lt(final String fieldName, final TItem value);
public static <TItem> Bson gte(final String fieldName, final TItem value);
public static <TItem> Bson lte(final String fieldName, final TItem value);
@SafeVarargs @SuppressWarnings("varargs") public static <TItem> Bson in(final String fieldName, final TItem... values);
public static <TItem> Bson in(final String fieldName, final Iterable<TItem> values);
@SafeVarargs @SuppressWarnings("varargs") public static <TItem> Bson nin(final String fieldName, final TItem... values);
public static <TItem> Bson nin(final String fieldName, final Iterable<TItem> values);
public static Bson and(final Iterable<Bson> filters);
public static Bson and(final Bson... filters);
public static Bson or(final Iterable<Bson> filters);
public static Bson or(final Bson... filters);
public static Bson not(final Bson filter);
public static Bson nor(final Bson... filters);
public static Bson nor(final Iterable<Bson> filters);
public static Bson exists(final String fieldName);
public static Bson exists(final String fieldName, final boolean exists);
public static Bson type(final String fieldName, final BsonType type);
public static Bson type(final String fieldName, final String type);
public static Bson mod(final String fieldName, final long divisor, final long remainder);
public static Bson regex(final String fieldName, final String pattern);
public static Bson regex(final String fieldName, final String pattern, @Nullable final String options);
public static Bson regex(final String fieldName, final Pattern pattern);
public static Bson text(final String search);
public static Bson text(final String search, final TextSearchOptions textSearchOptions);
public static Bson where(final String javaScriptExpression);
public static <TExpression> Bson expr(final TExpression expression);
@SafeVarargs @SuppressWarnings("varargs") public static <TItem> Bson all(final String fieldName, final TItem... values);
public static <TItem> Bson all(final String fieldName, final Iterable<TItem> values);
public static Bson elemMatch(final String fieldName, final Bson filter);
public static Bson size(final String fieldName, final int size);
public static Bson bitsAllClear(final String fieldName, final long bitmask);
public static Bson bitsAllSet(final String fieldName, final long bitmask);
public static Bson bitsAnyClear(final String fieldName, final long bitmask);
public static Bson bitsAnySet(final String fieldName, final long bitmask);
public static Bson geoWithin(final String fieldName, final Geometry geometry);
public static Bson geoWithin(final String fieldName, final Bson geometry);
public static Bson geoWithinBox(final String fieldName, final double lowerLeftX, final double lowerLeftY, final double upperRightX, final double upperRightY);
public static Bson geoWithinPolygon(final String fieldName, final List<List<Double>> points);
public static Bson geoWithinCenter(final String fieldName, final double x, final double y, final double radius);
public static Bson geoWithinCenterSphere(final String fieldName, final double x, final double y, final double radius);
public static Bson geoIntersects(final String fieldName, final Bson geometry);
public static Bson geoIntersects(final String fieldName, final Geometry geometry);
public static Bson near(final String fieldName, final Point geometry, @Nullable final Double maxDistance, @Nullable final Double minDistance);
public static Bson near(final String fieldName, final Bson geometry, @Nullable final Double maxDistance, @Nullable final Double minDistance);
public static Bson near(final String fieldName, final double x, final double y, @Nullable final Double maxDistance, @Nullable final Double minDistance);
public static Bson nearSphere(final String fieldName, final Point geometry, @Nullable final Double maxDistance, @Nullable final Double minDistance);
public static Bson nearSphere(final String fieldName, final Bson geometry, @Nullable final Double maxDistance, @Nullable final Double minDistance);
public static Bson nearSphere(final String fieldName, final double x, final double y, @Nullable final Double maxDistance, @Nullable final Double minDistance);
public static Bson jsonSchema(final Bson schema);
public static Bson empty();
public static Bson fields(final Bson... filters);
public static Bson fields(final Iterable<Bson> filters);
```
