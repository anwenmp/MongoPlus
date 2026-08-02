# 敏感词

> 审计日期：2026-08-02。该能力来自可选 `mongo-plus-sensitive-word` jar 与第三方 `com.github.houbb:sensitive-word`，不是 core 默认的读写对称能力。

## 模块、入口与注册

模块 packaging 为 jar，自身以 `provided` 依赖 core、普通依赖第三方库，并属于根 reactor。core **不依赖** sensitive-word（不是 provided、optional 或普通依赖）；方向是 sensitive-word → core。Boot 3/4 starter 都以 `provided` 依赖该模块，因此 starter 不传递 jar、用户必须显式引入；jar 在 classpath 时，两者通过 `AutoConfiguration.imports` 自动发现条件配置 `SensitiveWordConfiguration`，前缀 `mongo-plus.sensitive-word`。Solon POM、源码和资源中均未找到依赖、配置类或注册入口；仍可由用户代码手工 new `SensitiveWordManager`，不能描述成存在框架 Solon 自动集成。

公开入口是 `@SensitiveWord`、`SensitiveWordProperty`、`LoadExtraWord`、`SensitiveWordManager` 和 `SensitiveWordException`。manager 可动态增删黑/白名单、显式 `replace`（替换字符和重叠处理由第三方 `SensitiveWordBs.replace` 决定）；自动路径固定 `findFirst` 后拒绝并抛异常。源码只有 `ResultHandler.REJECT/MASK` 两个枚举值，且整个枚举未被 property/manager 引用：没有 `THROW`/`REPLACE` 枚举，也没有 MASK 执行分支。异常 message 和 error 日志包含首个命中词。

Property 支持大小写、全半角、数字/中文/英文样式、重复字符，以及连续数字/email/URL/IPv4 检测；匹配、Unicode、繁简和复杂度来自第三方库，不能按 DFA/Trie 常识推断。明确缺口：`ignoreChar` boolean 未传给 builder，builder 无条件使用 `SensitiveWordCharIgnores.specialChars()`，其配置值不改变行为。

创建 manager 会写静态 `HandlerRegistry`，再按默认 `LOCAL` 追加字段 Handler，或按 `GLOBAL` 注册普通 Interceptor。重复上下文会累积静态 Handler/Interceptor，registry 后写覆盖而既有对象仍持旧 manager，无清理或上下文隔离。

## LOCAL 字段处理

```text
实体原字段 → SensitiveWordFieldHandler（order 最小，检查明文）
           → TypeHandler/自定义 Handler（传递最新值）
           → Encrypt → DBRef
           → Document → Auto Fill → 执行链
```

LOCAL Handler 没有覆盖 `activate()`，所以启用后仍会对**每个实体字段**运行；其 `order()` 为 `Integer.MIN_VALUE`。只有字段带 `@SensitiveWord` 且当前明文非 null 时才调用 manager 检查；检查把非 String、集合/Map/对象整体 `String.valueOf`，不递归。命中时抛 `SensitiveWordException` 并中止映射；未拒绝时返回 null，不提供替代值。后续 TypeHandler、自定义 Handler、Encrypt 与 DBRef 按 order 接收前序最新值，因此检查不会恢复原始 Java 值。

LOCAL 覆盖实体 save/insert、saveBatch、updateById、Entity+Wrapper/saveOrUpdate 的实体转换分支。Wrapper-only/BSON update、Map/Document、直接 bulk model、replace、逻辑删除 update、查询条件、aggregate 和返回值绕过。Auto Fill 与 Tenant 都发生在字段循环后，不参与 LOCAL。

修复位于共享 core 转换器与 sensitive-word Handler：Boot 3、Boot 4 使用同一 core 路径，Solon 用户手工注册 sensitive-word 时也使用该路径，不需要分别修改 starter/plugin。

## GLOBAL 执行拦截

GLOBAL `SensitiveWordInterceptor` 扫描映射后的**序列化字符串**，不是递归逐字段结构：save one/many 扫完整 Document JSON；update one/many 只扫 update BSON 右侧、不扫 filter；bulk 对 InsertOne 用 model 的 `toString`，其他 model 直接强转 `UpdateManyModel` 后扫 update/pipeline。UpdateOne、ReplaceOne、DeleteOne/Many 等因此会在强转处失败，而不是安全地视为“未命中”。

GLOBAL 对实体与 Map/Document save、Wrapper/BSON update 和 Mapper bulkWrite 生效；不扫 query condition、aggregate pipeline或读取返回值。它扫描整个序列化文本，字段名/operator 也可能命中。

实体加密早于 GLOBAL，因此 GLOBAL 看到密文，不能再承诺检测明文；Auto Fill 已合入会参与。Tenant order 0 先改参数，insert 的租户值参与，而 update 只扫右侧所以租户 filter 不参与。GLOBAL 默认最大 order，与 Logic 等同 order 的相对次序依注册和稳定排序，不能作为契约。

## 字典、三者顺序与结果差异

动态词库直接调用第三方 `add/remove`；框架不加锁、不限制规模、不做版本/事务/跨 manager 同步。线程安全和并发一致性需运行验证。

真实顺序：LOCAL 明文检测（`Integer.MIN_VALUE`）→ TypeHandler/默认自定义 Handler（`0`）→ Encrypt（`Integer.MAX_VALUE - 1`）→ DBRef（`Integer.MAX_VALUE`）→ Auto Fill → 执行链；GLOBAL 为实体映射/加密/DBRef → Auto Fill/Tenant 等 → 扫 BSON → Driver；读取为 Document → 解密 → 脱敏 → DBRef → Entity/DTO，敏感词完全不参与。Map/Document 绕过 LOCAL但可被 GLOBAL 扫；DTO/聚合结果只可能受解密/脱敏目标注解影响。

## 验证与关键源码

覆盖 LOCAL/GLOBAL、异常与显式 replace、null/空/非 String/嵌套/List/Map、saveBatch/updateById/Wrapper/bulk 全 model、Auto Fill/Tenant、加密/DBRef 字段、动态词库/并发/多上下文、多数据源、Boot 3/4 显式依赖和 Solon 手工集成。

回归测试：`mongo-plus-sensitive-word/src/test/java/com/mongoplus/handler/SensitiveWordFieldHandlerTest.java`，纯转换器级测试覆盖 order、最新值传递、旧 Handler 兼容、TypeHandler→Encrypt、无注解/未命中 Encrypt、命中拒绝、明文检测与 DBRef。

关键源码：模块 POM；`SensitiveWord`、`SensitiveWordProperty`、`SensitiveWordManager`、`SensitiveWordFieldHandler`、`SensitiveWordInterceptor`、`SensitiveWordException`；core `MappingMongoConverter.processFields`；Boot 3/4 `SensitiveWordConfiguration`、starter POM/imports。
