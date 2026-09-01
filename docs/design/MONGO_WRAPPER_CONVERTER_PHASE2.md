# MongoDB Command → MongoPlus Wrapper 转换服务：Phase 2 实现记录

> 实现基线：MongoPlus 2.2.0 当前源码，Java 21，Spring Boot 3.5.16。

## 项目位置与边界

实现位于仓库根目录下的独立 Maven 项目 `mongo-plus-wrapper-converter/`，未加入 MongoPlus 根 reactor。
它通过普通 Maven dependency 使用 `com.mongoplus:mongo-plus-core:2.2.0`。本阶段没有修改 MongoPlus
生产源码，没有 Controller、SSE、DeepSeek、LangChain4j、Redis、MongoDB 连接或动态 Java 编译。

## 离线闭环

```text
Mongo Shell subset
  -> MongoCommandParser
  -> MongoCommandIR
  -> 人工 MongoPlusCallPlan
  -> MongoPlusApiCatalog / WrapperMethodRegistry
  -> 真实 QueryWrapper / AggregateWrapper
  -> BSON / pipeline
  -> BsonCanonicalizer / BsonComparator
  -> VERIFIED / REJECTED / UNSUPPORTED / ERROR
  -> MongoPlusJavaRenderer（仅 VERIFIED）
```

Parser 是字符游标 + 递归下降实现，不执行 JavaScript，也不使用整句正则或 `split(',')`。
Catalog 在 Spring 启动时按允许策略逐个反射解析真实方法签名；签名漂移会使 Bean 构建失败。
Registry 在请求路径只查缓存后的 Method，并限制 wrapper 类型、参数、嵌套深度和调用总数。

## 当前比较规则

- filter 普通字段顺序忽略，标量简写规范成显式 `$eq`。
- `$and/$or/$nor` 分支与 `$in/$nin/$all` 元素按 canonical value 作为多重集合比较。
- projection 字段顺序忽略；sort 字段顺序严格保留。
- pipeline stage 顺序严格保留；`$match/$project/$sort` 使用各自上下文规则。
- 使用 Canonical Extended JSON 保留 BSON numeric type，`int32/int64/double/decimal128` 不混同。
- 不做分配律、stage 合并或一般表达式化简。

## 构建说明

当前阿里云 Maven mirror 无法取得 `mongo-plus-core:2.2.0`。本地验证先从当前源码安装：

```text
mvn -f mongo-plus-bom/pom.xml -DskipTests install
mvn -pl mongo-plus-core -am -DskipTests install
```

再独立执行 converter 的 `compile` 和 `test`。这只是本地构建准备，不改变 converter 的独立项目边界。
