# 注解：field-processing

[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。

本页：Desensitization、FieldEncrypt、NonNull、Nullable、SensitiveWord。

## Desensitization

`import com.mongoplus.annotation.comm.Desensitization;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：读取字段后按类型、区间或定制Handler脱敏。
- 生效条件：默认读取处理器；自定义类需实现DesensitizationHandler。
- 限制与常见错误：不是写入加密；默认CUSTOM的0..0不是有效业务脱敏设计；脱敏值回写会丢信息。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `type` | `DesensitizationTypeEnum` | `DesensitizationTypeEnum.CUSTOM` | 脱敏类型 |
| `startInclude` | `int` | `0` | 脱敏开始位置(包含)，`#type()`为`DesensitizationTypeEnum#CUSTOM`并且 `#desensitizationHandler()`为`Void`时，会使用这里指定的位置处理 |
| `endExclude` | `int` | `0` | 脱敏结束位置(不包含)，`#type()`为`DesensitizationTypeEnum#CUSTOM`并且 `#desensitizationHandler()`为`Void`时，会使用这里指定的位置处理 |
| `desensitizationHandler` | `Class<?>` | `Void.class` | 脱敏处理器，`#type()`为`DesensitizationTypeEnum#CUSTOM`时，会走这里配置的处理器 脱敏处理器需实现`com.mongoplus.handlers.DesensitizationHandler`接口 |

```java
import com.mongoplus.annotation.comm.Desensitization;
import com.mongoplus.enums.DesensitizationTypeEnum;

public class DesensitizationExample { @Desensitization(type=DesensitizationTypeEnum.MOBILE_PHONE) String phone; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Desensitization.java#L12)。后续实现与注册见[专题](../references/field-security.md)。

## FieldEncrypt

`import com.mongoplus.annotation.comm.FieldEncrypt;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：实体字段写入加密、读取按findDecrypt解密。
- 生效条件：默认EncryptFieldHandler/FieldEncryptApply；配置相应key/privateKey/publicKey或自定义Encryptor。
- 限制与常见错误：默认BASE64是编码；MD5/SM3不可逆；查询条件转换与实体/原始BSON路径不同；密钥不写进Skill。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |
|---|---|---|---|
| `algorithm` | `AlgorithmEnum` | `AlgorithmEnum.BASE64` | 加密类型 |
| `findDecrypt` | `boolean` | `true` | 查询是否解密 如果设置为false，那么在查询时不会进行解密 |
| `key` | `String` | `""` | 秘钥，优先于全局参数 |
| `privateKey` | `String` | `""` | 私钥，优先于全局参数 |
| `publicKey` | `String` | `""` | 公钥，优先于全局参数 |
| `encryptor` | `Class<?>` | `Void.class` | 加密处理器，需实现`com.mongoplus.encryptor.Encryptor`接口 |

```java
import com.mongoplus.annotation.comm.FieldEncrypt;
import com.mongoplus.enums.AlgorithmEnum;

public class FieldEncryptExample { @FieldEncrypt(algorithm=AlgorithmEnum.AES, key="0123456789abcdef") String secret; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/FieldEncrypt.java#L11)。后续实现与注册见[专题](../references/field-security.md)。

## NonNull

`import com.mongoplus.annotation.comm.NonNull;`

- 作用域：`@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：JSR-305非空类型提示。
- 生效条件：需编译期JSR-305元注解依赖；由工具解释。
- 限制与常见错误：无运行时必填校验器；不能替代业务参数验证。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.comm.NonNull;

public class NonNullExample { @NonNull String value; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/NonNull.java#L10)。后续实现与注册见[专题](../references/field-security.md)。

## Nullable

`import com.mongoplus.annotation.comm.Nullable;`

- 作用域：`@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：JSR-305可空类型提示。
- 生效条件：需JSR-305依赖；工具解释When.MAYBE。
- 限制与常见错误：不会使持久化自动接受或保留null；null写策略另查CollectionField。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.comm.Nullable;

public class NullableExample { @Nullable String value; }
```

依据：[mongo-plus-annotation源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-annotation/src/main/java/com/mongoplus/annotation/comm/Nullable.java#L11)。后续实现与注册见[专题](../references/field-security.md)。

## SensitiveWord

`import com.mongoplus.annotation.SensitiveWord;`

- 作用域：`@Target(ElementType.FIELD)`；保留策略：RUNTIME；不可重复（未声明Repeatable）。
- 实际用途：LOCAL模式下标记需要敏感词处理的字段。
- 生效条件：引入mongo-plus-sensitive-word，配置LOCAL并初始化SensitiveWordProperty与Manager/HandlerRegistry。
- 限制与常见错误：只有注解不生效；GLOBAL走拦截器；replace/exception/保留结果策略单独配置。
- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。

无成员属性（标记注解）。

```java
import com.mongoplus.annotation.SensitiveWord;

public class SensitiveWordExample { @SensitiveWord String body; }
```

依据：[mongo-plus-sensitive-word源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-sensitive-word/src/main/java/com/mongoplus/annotation/SensitiveWord.java#L8)。后续实现与注册见[专题](../references/field-security.md)。
