# 字段加密

> 审计日期：2026-08-02。本文基于当前 `mongo-plus-annotation` 与 `mongo-plus-core` 源码；BASE64 是编码，MD5/SM3 是不可逆摘要。映射总链见 [ENTITY_MAPPING.md](../architecture/ENTITY_MAPPING.md)，未运行项见 [OPEN_QUESTIONS.md](../OPEN_QUESTIONS.md)。

## 入口、注册和全局状态

`@FieldEncrypt` 配置 `algorithm`（默认 `BASE64`）、`findDecrypt`（默认 true）、字段级 `key/privateKey/publicKey` 与自定义 `encryptor`。自定义类须实现 `Encryptor`，由反射创建并按 class 保存在静态 `EncryptorUtil.encryptorCache`，没有容器注入契约。

core 的静态 `HandlerCache` 固定注册写 `TypeHandlerFieldHandler → EncryptFieldHandler → DBRefHandler`、读 `FieldEncryptApply(order 0) → DesensitizationHandlerApply(order 1) → DBRefHandler`，以及查询 `EncryptorConditionHandler`；没有全局开关。Boot 3/4 的 `mongo-plus.encryptor` property 把 key 写入静态 `PropertyCache`；Solon 创建同类 property Bean。所有上下文和数据源共享、后写覆盖。Boot 3 会收集自定义 `ReadHandler` Bean；Boot 4/Solon 当前未见对等收集，但内置解密不依赖该步骤。

## 算法、格式与查询确定性

所有内置实现先接收 `String.valueOf(value)`；明文转 bytes 和解密 bytes 转 String 均调用无 charset 参数的 `getBytes()/new String()`，即平台默认字符集。成功输出都是 String。源码实际只有下列 12 个枚举，没有其他内置算法：

| 枚举 | 实现与输出 | key / salt / IV | 可逆性与确定性等值查询 |
|---|---|---|---|
| `BASE64` | `Base64Example`；JDK Base64 文本 | 不用 key、salt、IV | **编码而非加密**，可逆；同平台字符集下确定，可做字节等价的等值查询 |
| `MD5_16` | `MD5Example(16)`；MD5 hex 的 `[8,24)`，16 个小写 hex 字符 | 不用 key，无 salt/IV | 不可逆摘要；确定，可等值查询但有碰撞且不适合密码保护；读取原样返回摘要 |
| `MD5_32` | `MD5Example()`；32 个小写 hex 字符 | 不用 key，无 salt/IV | 不可逆摘要；确定，可等值查询但有碰撞且不适合密码保护；读取原样返回摘要 |
| `AES` | `AESExample`；password 的平台字符经 SHA-256 后取 16 bytes，密文小写 hex | 注解 key 空时回退 `PropertyCache.key`；代码不提供 IV，`Cipher("AES")` 的实际模式由 provider 决定 | 可逆；当前 provider 下是否稳定及 transformation 需运行确认，不能作为跨 provider 查询契约 |
| `RSA` | `RSAExample`；X.509 hex 公钥加密，密文小写 hex；预期 PKCS#8 hex 私钥解密 | 注解 publicKey 空时回退全局 publicKey；无显式 salt/IV/分段 | 算法可逆，但框架解密接线缺陷见下；padding、随机性、长度均由 `Cipher("RSA")` provider 决定，未运行前不承诺确定性查询 |
| `SM2` | `SM2Example`；BC `Cipher("SM2")`，X.509/PKCS#8 EC key，密文 hex | 注解 publicKey 空时回退全局 publicKey；无显式 salt/IV | 算法可逆，但框架解密接线缺陷见下；provider 内部随机性未测试，不承诺确定性查询 |
| `SM3` | `SM3Example`；BC SM3，64 个小写 hex 字符 | 不用 key，无 salt/IV | 不可逆摘要；确定，可等值查询但有碰撞语义；读取原样返回摘要 |
| `SM4` | `SM4Example`；BC `SM4/ECB/PKCS5Padding`，hex key、hex 密文 | 注解 key 空时错误回退 `PropertyCache.publicKey`；ECB，无 IV | 可逆且从代码结构看同 key/明文确定，但 provider 可用性和 key 长度需测试后才作为查询契约 |
| `PBEWithMD5AndDES` | `PBEExample`；JCE 同名算法，salt+cipher 小写 hex | 注解 key 空时加密回退全局 key；每次随机 8-byte salt，1000 次，无独立 IV | 可逆；随机 salt 使同明文密文不稳定，不能用重新加密值做等值查询 |
| `PBEWithMD5AndTripleDES` | 同上 | 同上 | 同上；具体 JDK/provider 可用性待测试 |
| `PBEWithSHA1AndDESede` | 同上 | 同上 | 同上；具体 JDK/provider 可用性待测试 |
| `PBEWithSHA1AndRC2_40` | 同上 | 同上 | 同上；具体 JDK/provider 可用性待测试 |

### 已确认密钥缺陷

- `EncryptorUtil.decrypt` 将 `fieldEncrypt.publicKey()` 作为 `Encryptor.decrypt` 第三个参数，完全不读取注解 `privateKey()`。
- RSA/SM2 decrypt 收到空值时又回退 `PropertyCache.publicKey`，并把它按 PKCS#8 私钥解析；全局 `PropertyCache.privateKey` 也没有进入该内置调用链。正常的“公钥加密、匹配私钥解密”配置无法闭合，这是已确认缺陷；不能简化成“公钥可反向解密”。
- PBE encrypt 的空注解 key 回退 `PropertyCache.key`，decrypt 没有相同回退，直接 `key.toCharArray()`；因此仅配置全局 key 时，加密可成功而读取进入异常回退，这是已确认缺陷。
- SM4 的空 key 回退 `PropertyCache.publicKey` 而非 `PropertyCache.key`。这是已确认配置接线缺陷；是否恰好能工作取决于该值是否是合法 SM4 key。

null 不调用算法；空字符串会调用。数字、日期、byte[]、集合/Map/对象若字段本身带注解，会整体字符串化，不逐元素处理。嵌套实体自己的注解仅在默认递归映射实际进入该实体时生效；给容器字段本身加注解不会递归逐项加密。

## 写入链与覆盖范围

```text
原 Java 字段
  → TypeHandler（读取原值）
  → EncryptFieldHandler（仍读取原值）
  → DBRefHandler（仍读取原值）
  → 重命名后的 Document key
  → Auto Fill Map 合并
  → 普通/高级执行链 → Driver
```

Handler 不是输出传给下一步的管道；后命中者覆盖 `obj`。所以 TypeHandler+Encrypt 加密原 Java 值而非 TypeHandler 输出，Encrypt+DBRef 最终为 DBRef。可选 LOCAL sensitive-word Handler 启用后追加在尾部、对所有字段激活并无条件返回原 Java 值，因此即使加密字段没有 `@SensitiveWord` 也会把密文覆盖成明文；命中敏感词抛异常时才会在覆盖前中止。字段名先确定，加密只处理值。Auto Fill 晚于字段循环，填充值不再走 TypeHandler/加密/DBRef/LOCAL。

实体 save/insert、saveBatch、updateById、Entity+Wrapper、saveOrUpdate 的实体转换分支会处理。Wrapper-only/BSON update、直接 bulk model、replace、逻辑删除生成 update 不处理；upsert 不增加额外处理。Map/Document 保存提前走 Map 分支，注解不可见。

## 查询与读取

Lambda query condition 有 `originalField` 时，`EncryptorConditionHandler` 对 `Collection` 逐元素、其他值整体加密；null 传入 `EncryptorUtil.encrypt` 后仍为 null。`eq`、`ne`、`in`、`nin`、`gt/gte/lt/lte`、`regex/like`、`all` 的 Lambda 重载都通过相同 `BaseCondition` 构建点，因此都会改写条件值；字符串列名重载没有 `originalField`，不处理。这里仅确认“值被加密”，不代表范围、正则或 like 对密文仍有明文语义。Lambda QueryWrapper 被用于 aggregate `$match` 时沿用这一构建结果；直接 BSON pipeline 不处理。UpdateWrapper 的 Lambda filter 可处理，`$set` 等 update value 走 `beforeUpdateCondition`，该 Handler 未实现，所以不加密。无独立查询值加密 API。

PBE 随机 salt，明文条件重加密不能匹配历史密文；其余当前无显式随机 salt/IV，但 RSA/SM2/provider 行为未运行前不能承诺确定性。regex/like 也不能据此宣称支持明文模式。

```text
Document raw value
  → TypeHandler.getResult（先保存）
  → 解密(order 0) → 脱敏(order 1) → DBRef
  → TypeHandler 非 null 时最终仍赋其结果；否则转换 Handler 后值
```

`findDecrypt=false` 保持密文；MD5/SM3 的 decrypt 只返回输入摘要，不会再次摘要。TypeHandler 非 null 时解密仍执行但其结果不用于最终赋值。`Document.class` 直接返回密文且不修改原 Document；Map 不按字段注解处理，且 Mapper Map 映射另有递归缺陷。实体/DTO/聚合映射到带注解目标字段并走 converter 时处理。

## 异常回退与安全分级

`EncryptorUtil.encrypt/decrypt` 捕获所有 `Exception`，日志写算法名、`e.getMessage()` 和完整异常对象，然后返回调用前的原输入。它不记录专门的 key/明文/密文字段，但 provider 异常 message/stack 是否携带参数不能一概保证。

- **已确认 fail-open：** 写侧加密异常后返回原 Java 字段值，后续映射会继续把该值放入 Document；读侧解密异常后保留进入解密器的原始 Document 值，后续脱敏/DBRef 仍可能继续处理。
- **已确认安全缺陷：** 框架没有阻断或失败标记，因此普通 String 明文在加密失败时会作为字段值继续流向执行链。
- **仍需运行验证的影响：** 每种入口最终是否真的到达 MongoDB、Driver codec 对非 String 原值的处理、拦截器是否中止，以及日志后端最终渲染内容。不能把静态路径扩大为所有场景均已实证“明文入库”。

## 验证清单与关键源码

覆盖全部算法、null/空/非 String/嵌套集合、TypeHandler/DBRef/Auto Fill、各 CRUD、Wrapper/BSON/bulk/upsert、Lambda/字符串条件、坏 key/密文、DTO/聚合/Map/Document、多上下文/并发。尤其固定异常吞掉、private-key、PBE 空 key、provider 与长度行为。

关键源码：`FieldEncrypt.java`、`AlgorithmEnum.java`；`EncryptFieldHandler`、`FieldEncryptApply`、`EncryptorConditionHandler`；`EncryptorUtil`、`encryptor/*Example`、`PropertyCache`、`HandlerCache`；三集成的 `MongoEncryptorProperty`。
