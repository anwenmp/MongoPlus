# 注解：routing-isolation

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：IgnoreLogic、IgnoreTenant、MongoDs、MongoReadPreference、MongoTransactional。

## IgnoreLogic

`import com.mongoplus.annotation.logice.IgnoreLogic;`

- 作用域：`@Target({ElementType.METHOD, ElementType.TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：方法或类型上请求忽略逻辑删除条件。
- 生效条件：配置逻辑删除并使相应AOP切面生效。
- 限制与常见错误：包名annotation.logice；各执行分支是否消费忽略标志需核验；不保证所有update/bulk路径相同。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.logice.IgnoreLogic;

@IgnoreLogic public class IgnoreLogicExample { public void maintenance(){} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/logice/IgnoreLogic.java#L6)。后续实现与注册见[专题](../references/transactions-routing.md)。

## IgnoreTenant

`import com.mongoplus.annotation.tenant.IgnoreTenant;`

- 作用域：`@Target(ElementType.METHOD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：在方法代理边界暂时忽略租户过滤。
- 生效条件：已注册TenantInterceptor及相应容器TenantAspect。
- 限制与常见错误：仅METHOD，不支持类；仅可信内部方法使用；嵌套上下文与异步恢复需验证，Solon额外核对绑定。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.tenant.IgnoreTenant;

public class IgnoreTenantExample { @IgnoreTenant public void maintenance(){} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/tenant/IgnoreTenant.java#L10)。后续实现与注册见[专题](../references/transactions-routing.md)。

## MongoDs

`import com.mongoplus.annotation.datasource.MongoDs;`

- 作用域：`@Target({ElementType.METHOD, ElementType.TYPE})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：选择预先注册的命名数据源，可交由DataSourceHandler。
- 生效条件：Boot切面或手工DataSourceManager；Solon不能仅因Aspect存在就认定绑定。
- 限制与常见错误：value必填；默认master；方法/类、嵌套、自调用、异步线程与事务同源需验证。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `String` | `必填（无默认值）` | 数据源名称 |
| `dsHandler` | `Class<?>` | `Void.class` | 数据源处理类，需要实现`com.mongoplus.handlers.DataSourceHandler`接口 |

```java
import com.mongoplus.annotation.datasource.MongoDs;

@MongoDs("archive") public class MongoDsExample { void query(){} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/datasource/MongoDs.java#L10)。后续实现与注册见[专题](../references/transactions-routing.md)。

## MongoReadPreference

`import com.mongoplus.annotation.transactional.MongoReadPreference;`

- 作用域：`@Target(ElementType.METHOD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：配置事务读取偏好，供MongoTransactional.preference消费。
- 生效条件：当前MongoTransactionalManager读取preference数组第一项。
- 限制与常见错误：虽然Target是METHOD，未发现独立方法切面；不提供单独标注即可路由读请求的示例；多项不表示多个偏好同时生效。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `preferenceEnum` | `ReadPreferenceEnum` | `ReadPreferenceEnum.PRIMARY` | 一种读取偏好设置，它指定所有的读操作都应该由哪个处理 |
| `maxStaleness` | `long` | `0` | maxStaleness选项是用来设置最大过时容忍度的。 这个选项在配置复制集的读取偏好时非常有用，它允许客户端读取已经过时的（不同步的）副本集成员的数据。maxStaleness的值越大，客户端可以容忍的过时时间就越长 |
| `timeUnit` | `TimeUnit` | `TimeUnit.MILLISECONDS` | 时间单位 |

```java
import com.mongoplus.annotation.transactional.MongoReadPreference;
import com.mongoplus.annotation.transactional.MongoTransactional;
import com.mongoplus.enums.ReadPreferenceEnum;

public class MongoReadPreferenceExample { @MongoTransactional(preference={@MongoReadPreference(preferenceEnum=ReadPreferenceEnum.PRIMARY)}) public void save(){} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoReadPreference.java#L13)。后续实现与注册见[专题](../references/transactions-routing.md)。

## MongoTransactional

`import com.mongoplus.annotation.transactional.MongoTransactional;`

- 作用域：`@Target(ElementType.METHOD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：声明MongoDB事务及session/事务选项与回滚规则。
- 生效条件：Boot事务切面和MongoPlusTransactionalManager，或Solon已绑定拦截器；MongoDB支持事务。
- 限制与常见错误：仅方法；代理自调用边界；rollbackFor/noRollbackFor非Java异常名称匹配；多client无分布式原子保证。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `causallyConsistent` | `boolean` | `true` | 使用会话的操作是否应该彼此一致。 |
| `snapshot` | `boolean` | `false` | 使用此会话的读取操作是否应全部共享同一个快照。 |
| `readConcern` | `ReadConcernEnum` | `ReadConcernEnum.DEFAULT` | 设置一致性读策略 |
| `writeConcern` | `WriteConcernEnum` | `WriteConcernEnum.ACKNOWLEDGED` | 设置写入安全的级别 |
| `maxCommitTimeMS` | `long` | `0` | 用于指定事务提交的最大时间限制以防止因为某些原因导致事务长时间无法提交。如果事务在指定的时间内没有提交，MongODB 会自动中止 (abort) 事务。 |
| `timeUnit` | `TimeUnit` | `TimeUnit.MILLISECONDS` | 时间单位 |
| `preference` | `MongoReadPreference[]` | `{}` | 长度为1则有效 配置读取偏好设置 |
| `rollbackFor` | `Class<? extends Throwable>[]` | `{}` | 捕获该异常并进行回滚 |
| `noRollbackFor` | `Class<? extends Throwable>[]` | `{}` | 捕获该异常不进行回滚 |

```java
import com.mongoplus.annotation.transactional.MongoTransactional;

public class MongoTransactionalExample { @MongoTransactional(rollbackFor=Exception.class) public void saveBoth(){} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/transactional/MongoTransactional.java#L14)。后续实现与注册见[专题](../references/transactions-routing.md)。
