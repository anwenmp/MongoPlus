# 配置参考

本文只记录当前源码声明的键、默认值和消费者。字段存在但未找到读取方时明确标注；通过 URI 交给 Driver 的能力不写成框架独立配置。

## 来源与集成

| 来源 | Boot 3 / Boot 4 | Solon |
|---|---|---|
| application YAML/properties | `@ConfigurationProperties`，两代属性前缀同构 | 配置对象/`@Inject("${mongo-plus...}")` |
| 系统属性、环境变量 | Spring Boot Binder 提供 | Solon 配置系统提供；MongoPlus 未另写解析器 |
| 注解 | `@MongoMapperScan`、`@MongoDs`、实体/索引/事务注解 | 对应插件扫描与 interceptor |
| Bean | handler、interceptor、listener、conversion、transaction options | 只以插件明确收集的类型为准 |
| 编程式 | core `Configuration`、动态 datasource、ThreadLocal 上下文 | 同 core 能力，容器接线不同 |

Boot 4 的 Java/框架要求见 [COMPATIBILITY.md](COMPATIBILITY.md)，完整启动接线见 [STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)。

## 主配置键

| key | 类型 / 默认值 | 消费者 / 阶段 |
|---|---|---|
| `mongo-plus.configuration.banner` | Boolean / `true` | 启动 banner |
| `mongo-plus.configuration.auto-id-collection-name` | String / `null` | setter 写 `PropertyCache`，自增 ID 使用 |
| `mongo-plus.configuration.ikun` | Boolean / `false` | setter 写全局 cache |
| `mongo-plus.configuration.auto-convert-object-id` | Boolean / `true` | 条件/映射 ObjectId 转换 |
| `mongo-plus.configuration.object-id-convert-type` | Boolean / `false` | 读取 ID 时按字段类型转换 |
| `mongo-plus.configuration.auto-create-index` | Boolean / `false` | 启动期自动索引 |
| `mongo-plus.configuration.auto-create-time-series` | Boolean / `false` | 启动期时序初始化 |
| `mongo-plus.configuration.auto-scan-packages` | List<String> / `null` | 自动索引/时序的补充实体包，不是 Mapper 扫描 |
| `mongo-plus.configuration.collection.mapping-strategy` | enum / `ALL_CHAR_LOWERCASE` | collection 名映射策略 |
| `mongo-plus.configuration.collection.block-attack-inner` | Boolean / `false` | 注册全集合更新/删除保护 |
| `mongo-plus.configuration.collection.block-attack-inner-order` | int / `1` | setter 写 `OrderCache` |
| `mongo-plus.configuration.field.camel-to-underline` | Boolean / `false` | 字段双向映射 cache |
| `mongo-plus.configuration.field.ignoring-null` | Boolean / `true` | 实体写 Document 时忽略 null |
| `mongo-plus.log` / `pretty` / `format` | Boolean / `false` | 日志 listener 与格式 cache |
| `mongo-plus.log-order` | int / `0` | `OrderCache.LOG_ORDER` |
| `mongo-plus.spring.transaction` | Boolean / `false` | 条件创建 Spring transaction manager |
| `mongo-plus.spring.override-mongo-client` | Boolean / `true` | Spring MongoClient 覆盖/适配配置 |

包装类型允许缺省绑定不等于消费者安全接受显式 null；源码没有统一 null 契约。

## 数据源配置

连接主前缀是 `mongo-plus.data.mongodb`，运行时默认数据源名是不可由属性改名的常量 `master`。项目并非只有一个总属性对象：全局行为另用 `mongo-plus.configuration`，日志用 `mongo-plus`，Spring 集成用 `mongo-plus.spring`，加密用 `mongo-plus.encryptor`，敏感词用 `mongo-plus.sensitive-word`。

| 相对 key | 类型 / Java 默认 | 状态 |
|---|---|---|
| `url` | String / `null` | 非空白时 `UrlJoint` 立即原样返回；不是先解析后再由独立字段覆盖 |
| `host`, `port` | String / `null` | 仅 `url` 为空白时拼接；框架没有 host/port 默认值 |
| `database` | String / `null` | 不拼入所选业务 database；独立用于 `MongoPlusClient`/`CollectionManager` 初始化，不能从 URI database 自动回填 |
| `username`, `password`, `authentication-database` | String / `null` | 仅 `url` 为空白时拼入 URI；用户名和密码必须同时非空白才加入，password 是 String 并被 URL encode；没有独立 `MongoCredential` 构建 |
| `position` | Integer / `Integer.MIN_VALUE` | 分片权重，越小优先级越高 |
| `min-pool-size`, `max-pool-size`, `max-connecting` | Integer / `null` | URI/Driver 连接池参数 |
| `wait-queue-timeout-ms`, `server-selection-timeout-ms`, `local-threshold-ms`, `heartbeat-frequency-ms` | Integer / `null` | URI/Driver 集群参数 |
| `connect-timeout-ms`, `socket-timeout-ms`, `max-idle-time-ms`, `max-life-time-ms` | Integer / `null` | URI/Driver timeout 参数 |
| `replica-set` | String / `null` | URI 副本集参数 |
| `ssl`, `tls`, `tls-insecure`, `tls-allow-invalid-hostnames` | Boolean / `null` | URI/Driver TLS |
| `client-key-store`, `jks`, `key-password`, `invalid-host-name-allowed` | String/String/String/boolean(false) | SSL context 相关 |
| `journal`, `w`, `wtimeout-ms` | Boolean/String/Integer / `null` | write concern URI 参数 |
| `read-preference`, `read-preference-tags`, `max-staleness-seconds` | String/String/Integer / `null` | read preference URI 参数 |
| `auth-mechanism`, `auth-source`, `auth-mechanism-properties` | String / `null` | 认证 URI 参数 |
| `app-name`, `compressors`, `zlib-compression-level` | String/String/Integer / `null` | Driver URI 参数 |
| `retry-writes`, `retry-reads` | Boolean / `null` | null 时由 Driver 默认决定 |
| `uuid-representation`, `direct-connection`, `srv-service-name` | String/Boolean/String / `null` | Driver URI 参数 |
| `slave-data-source` | List<SlaveDataSource> / `null` | 启动逐项注册；元素继承上述字段并增加 `slave-name` |

`UrlJoint` 的准确优先级是：非空白 `url` 整体胜出；否则才由 username/password、host/port、authentication-database 和其余 URI 选项拼接字符串，最后 `MongoUtil` 以单个 `ConnectionString` 调用 `MongoClientSettings.Builder.applyConnectionString`。因此 URI 与独立连接字段不是逐项覆盖关系。`database`、`position` 以及自定义 JKS SSL context 在这条 URI 选择之外单独消费；其中 `ssl=true` 还会触发 keystore 构建，须与 URI/TLS 配置谨慎组合。未发现独立 `MongoCredential`、通用 `MongoClientSettings` customizer 或事务重试属性键。

| 能力 | MongoPlus 独立字段 | 传递方式 |
|---|---|---|
| host/port/credential/pool/timeout/replicaSet/TLS/read/write concern/retry | 有，见上表 | 仅在 `url` 为空白时由 `UrlJoint` 拼成 URI；非空白 `url` 时相应字段不覆盖 URI |
| database 选择 | 有 `database` | MongoPlus 初始化 collection manager 使用，不从 URI 推导 |
| client JKS/SSLContext | 有 `client-key-store`、`jks`、`key-password`、`invalid-host-name-allowed` | `MongoUtil` 直接构造 `SslSettings` |
| 未声明的 Driver URI 选项 | 无逐项封装 | 可由调用方写入 `url`，是否被当前 Driver 接受由 Driver 决定 |

- datasource/database 不会由名称自动补齐；缺失会在初始化或首次访问暴露错误。
- 动态重名注册使用 map 覆盖；旧 client 是否关闭及并发边界见 [MULTI_DATASOURCE.md](features/MULTI_DATASOURCE.md)。
- Solon 声明 `mongo-plus.configuration.lazy-data-source=false`，本次检索覆盖 Solon 属性类、配置类、插件和全仓 Java 引用，只发现字段、getter/setter，未发现生产读取方；Boot 3/4 主属性类没有该字段。该结论是当前源码快照，不是永久无效承诺。

## Mapper 与实体扫描

- Mapper 用 `@MongoMapperScan`/starter registrar 扫描，不存在已确认的 application Mapper 包 key。
- `auto-scan-packages` 只补充自动索引、时序等实体元数据扫描，不能替代 Mapper 扫描。
- `@CollectionName` 是实体元数据，不单独触发全仓扫描。Mapper 泛型必须能从接口直接解析；间接泛型边界见 [STARTUP_LIFECYCLE.md](architecture/STARTUP_LIFECYCLE.md)。
- Boot 3/4 扫描实现同构；Solon 使用插件扫描和注入回调，不具备 Spring FactoryBean 的全部语义。

## 自动索引与时序

两开关默认均为 false。启用后在启动期扫描应用包及 `auto-scan-packages`，读取实体注解和 datasource；运行时动态数据源/动态集合不会自动枚举参与。索引异常可导致启动失败；时序部分异常路径会记录或吞掉。详见 [INDEX_AND_TIMESERIES.md](features/INDEX_AND_TIMESERIES.md)。

## Logic、Tenant、Auto Fill

`mongo-plus.configuration.logic` 绑定继承 core `LogicProperty` 的属性对象：

| key | 类型 / 默认值 | 含义 |
|---|---|---|
| `open` | Boolean / `false` | 全局逻辑删除开关 |
| `auto-fill` | Boolean / `false` | 是否自动补充逻辑字段元数据 |
| `logic-delete-field` | String / `null` | 全局逻辑字段名 |
| `logic-delete-value` | String / `"1"` | 删除值 |
| `logic-not-delete-value` | String / `"0"` | 未删除值 |
| `logic-data-type` | `LogicDataType` / `DEFAULT` | 值转换类型 |

实际实体元数据仍依赖 Mapper/实体发现时机；全局值与实体注解的选择见 [LOGIC_DELETE.md](features/LOGIC_DELETE.md)。
- Tenant 没有 application 总开关，通过 `TenantHandler` Bean/编程式注册和 `@IgnoreTenant` 控制。
- Auto Fill 没有 application 总开关，通过 `MetaObjectHandler` Bean/编程式注册和字段注解控制。
- 多个 handler 有两类语义：单值 cache 后注册覆盖，列表链追加并排序；不能统一描述为“多 Bean 全部执行”。

## 加密、脱敏与敏感词

| key | 默认值 | 消费状态 |
|---|---|---|
| `mongo-plus.encryptor.key` | `null` | setter 写全局对称密钥 cache |
| `mongo-plus.encryptor.public-key`, `private-key` | `null` | setter 写全局非对称密钥 cache；算法接线风险见 [FIELD_ENCRYPTION.md](features/FIELD_ENCRYPTION.md) |
| `mongo-plus.sensitive-word.sensitive-type` | `LOCAL` | LOCAL 注册字段 handler，GLOBAL 注册 interceptor |
| `ignore-case`, `ignore-width`, `ignore-num-style`, `ignore-chinese-style`, `ignore-english-style` | `true` | builder 已消费 |
| `ignore-repeat` | `false` | builder 已消费 |
| `enable-num-check`, `enable-email-check`, `enable-url-check`, `enable-ipv4-check` | `false` | builder 已消费 |
| `ignore-char` | `false` | 字段存在，但 builder 固定 `specialChars()`，未读取该值 |

额外词库使用 `LoadExtraWord` Bean，不是 application 列表。未发现脱敏的统一全局属性。敏感词模块须在 classpath，详见 [SENSITIVE_WORD.md](features/SENSITIVE_WORD.md)。密钥 cache 为 JVM 静态状态，多应用上下文可能覆盖。

## Bean 型扩展

`Interceptor`、`AdvancedInterceptor`、Collection/DataSource/Tenant Handler、`TypeHandler`、`MappingStrategy`、`ConversionStrategy`、`MetaObjectHandler`、Command Listener、Recorder、Async Multiple Write 和 Optimistic Locker 都是 Bean/编程式扩展，不是普通 key。Boot 自动收集或条件创建；列表型通常排序但不去重，单值 cache 可能后写覆盖。Solon 仅以插件显式收集清单为准。

## Backup、Restore、线程池与事务

- Backup/Restore 的 page size、path、文件名、charset、ZIP、mode 来自调用参数或固定实现，未发现统一 `mongo-plus.*` 属性对象。
- 异步多写线程池当前未发现 core/max/queue/rejection/shutdown application key，生命周期见 [ASYNC_MULTI_WRITE.md](features/ASYNC_MULTI_WRITE.md)。
- `mongo-plus.spring.transaction=true` 且缺少 `TransactionManager` 时，Boot 3/4 创建 `mongoPlusTransactionalManager`。
- `TransactionOptions` 是 Bean/Driver 对象，不是展开的 MongoPlus 属性；read/write concern、read preference、timeout 和事务重试没有统一键。用户 manager 抑制默认 manager；Solon 使用自有 aspect。见 [TRANSACTION.md](features/TRANSACTION.md)。

## Boot 3 / Boot 4 / Solon

| 能力 | Boot 3 | Boot 4 | Solon |
|---|---|---|---|
| 主前缀/数据源 | 相同 | 相同 | 基本复刻前缀，绑定类独立 |
| Mapper 扫描 | registrar/FactoryBean | 同构 | 插件扫描/注入回调 |
| 自动索引/时序 | 支持 | 支持 | 有对应字段与路径，异常语义按插件 |
| 多数据源/`@MongoDs` | Spring AOP 显式切点 | 同构 | aspect 实现存在，但 `XPluginAuto` 未显示注解到 interceptor 的显式绑定；运行接线待集成测试 |
| Spring transaction | 条件 manager | 条件 manager | 不适用；自有 aspect |
| Sensitive Word | 模块在 classpath 时条件配置 | 同构 | 不保证同等自动配置 |
| Sharding starter | 存在 Boot 3 专用模块 | 当前未发现对应 Boot 4 starter | 无同等 starter |
| 扩展 Bean 收集 | 类型较全 | 同构 | 只认插件明确类型 |

## 功能各自的优先级

- datasource：方法 `@MongoDs` > 类 `@MongoDs` > 当前 handler/ThreadLocal > `master`；嵌套恢复不是栈。
- database：显式 namespace、当前 datasource property、实体元数据按入口分别决定，无统一总规则。
- collection：动态 handler 可覆盖实体/显式 collection；实体默认名受 mapping strategy 影响。
- encryption key：注解参数与全局 cache 的关系按算法分别判断。
- index/time-series datasource：注解 datasource 优先于默认数据源，运行时 ThreadLocal 不参与启动扫描。
- transaction manager：用户 `TransactionManager` Bean 抑制默认 manager；core 自有事务是另一上下文。
- listener/interceptor：用户 Bean 与默认项进入静态链并排序，重复初始化不去重。

## 声明但未发现消费者

| key | 声明位置 | 当前结论 |
|---|---|---|
| `mongo-plus.configuration.lazy-data-source` | Solon `MongoDBConfigurationProperty` | 未发现读取方，不判定永久无效 |
| `mongo-plus.sensitive-word.ignore-char` | `SensitiveWordProperty` | getter/setter 和配置绑定可达，但 `sensitiveWordBs()` 固定调用 `specialChars()`，未读取字段值 |

`ResultHandler` 是敏感词模块中的独立枚举，不是 `SensitiveWordProperty` 字段，因此不存在合法的 application key；全仓 Java 定向检索也未发现执行路径引用该枚举。不要把它列成“声明但未消费的配置字段”。

## 最小示例

```yaml
mongo-plus:
  data:
    mongodb:
      url: mongodb://user:${MONGO_PASSWORD}@localhost:27017/app
      database: app
      slave-data-source:
        - slave-name: reporting
          url: mongodb://localhost:27018/reporting
          database: reporting
  configuration:
    auto-create-index: false
    auto-create-time-series: false
    auto-scan-packages:
      - com.example.domain
```

Mapper 扫描使用注解：

```java
@MongoMapperScan("com.example.mapper")
```

扩展使用 Bean，而非虚构 key：

```java
@Bean
MetaObjectHandler metaObjectHandler() {
    return new AppMetaObjectHandler();
}
```

## 排障

- 找不到默认 datasource：确认主前缀已绑定、`master` 已注册，且没有遗留 ThreadLocal 名称。
- database 为空：不要假设 URI database 与独立字段必然互补，追踪实际初始化参数。
- Mapper 未扫描：检查 `@MongoMapperScan` 包和接口直接泛型；`auto-scan-packages` 不能替代它。
- 自动索引/时序异常：检查开关、扫描包、注解 datasource、权限和已有索引；两者异常策略不同。
- `@MongoDs("")`、嵌套换源、线程池传播不是可靠默认行为。
- 多个 handler/listener：先确认是单值覆盖还是列表追加；静态 cache 可能跨上下文累积。
- 配置存在但不生效：核对“声明但未发现消费者”，再搜索 setter/读取方；metadata 和示例不是消费证据。
