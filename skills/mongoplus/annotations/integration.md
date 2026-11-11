# 注解：integration

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：Mongo、MongoMapperScan。

## Mongo

`import com.mongoplus.annotation.mapper.Mongo;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：标记需创建MongoMapper代理的接口。
- 生效条件：Boot扫描/工厂，Solon带@Inject接口注入。
- 限制与常见错误：不是Spring Data @Document或MyBatis @Mapper；继承MongoMapper<T>并正确解析泛型。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.mapper.Mongo;

@Mongo public interface MongoExample extends com.mongoplus.mapper.MongoMapper<MongoExample.Entity> { class Entity {} }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/mapper/Mongo.java#L8)。后续实现与注册见[专题](../references/integration.md)。

## MongoMapperScan

`import com.mongoplus.annotation.MongoMapperScan;`

- 作用域：`@Target(ElementType.TYPE)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：为Boot应用扫描MongoMapper接口并注册FactoryBean。
- 生效条件：引入对应Boot3或Boot4 Starter，配置扫描包。
- 限制与常见错误：两个模块提供同FQCN，不能同时依赖；Solon无此扫描注解契约。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `value` | `String[]` | `{}` | 扫描路径 |

```java
import com.mongoplus.annotation.MongoMapperScan;

@MongoMapperScan("example.mapper") public class MongoMapperScanExample {}
```

依据：[mongo-plus-boot-starter源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot-starter/src/main/java/com/mongoplus/annotation/MongoMapperScan.java#L12)；[mongo-plus-boot4-starter源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-boot4-starter/src/main/java/com/mongoplus/annotation/MongoMapperScan.java#L12)。后续实现与注册见[专题](../references/integration.md)。
