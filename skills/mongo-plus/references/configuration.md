# 配置、连接池、TLS与配置加密

Boot3/4的配置绑定在MongoPlusPropertyConfiguration；Core使用Configuration及BaseProperty，Solon由插件接线。配置字段的存在不等于所有容器都消费；默认值、URI优先级与运行状态分开核对。

| 前缀 | 绑定类型 | 用途 |
|---|---|---|
| mongo-plus.data.mongodb | MongoDBConnectProperty extends BaseProperty | 连接与slaveDataSource列表 |
| mongo-plus.configuration | MongoDBConfigurationProperty | 扫描、索引、时序、ID等 |
| mongo-plus.configuration.collection | MongoDBCollectionProperty | 集合命名、防空条件监听 |
| mongo-plus.configuration.field | MongoDBFieldProperty | 驼峰转换、null |
| mongo-plus.configuration.logic | MongoLogicDelProperty extends LogicProperty | 逻辑删除 |
| mongo-plus | MongoDBLogProperty | log/pretty/logOrder/format兼容配置 |
| mongo-plus.encryptor | MongoEncryptorProperty | 字段加密默认密钥 |
| mongo-plus.spring | MongoSpringProperty | transaction / overrideMongoClient |

`UrlJoint.jointMongoUrl()`遇到非空url直接返回，独立host/port/TLS/池字段不会再被拼接；不要同时设置两套后假定覆盖顺序。推荐在已有URI上明确所需Driver选项并核验MongoClientSettings。各BaseProperty字段以下逐一列出，不能把Driver所有URI能力都说成框架有独立配置。

TLS涉及tls/ssl、tlsInsecure、tlsAllowInvalidHostnames、clientKeyStore/jks/keyPassword等；实际证书加载与MongoClient创建在ConnectMongoDB，真实握手未执行就标NOT_RUN。读偏好、认证、压缩、retryWrites/Reads、uuidRepresentation、超时、连接池均需Driver和部署匹配。retryWrites不是业务层重试或乐观锁冲突重试。

环境配置加密由Starter EncryptionPostProcessor提供：读取mpw.key，检测`${ENC:密文}`，AES解密包装OriginTrackedMapPropertySource。它与@FieldEncrypt独立；不是任意PropertySource都被替换，也不自动隐藏日志中已经输出的值。密钥不写进参考文档。

已声明但消费受限：lazyDataSource未确认实际惰性连接消费者；SensitiveWordConfigurationProperty.ignoreChar当前未控制specialChars忽略器。这两项标UNVERIFIED，不提供假定生效的示例。

## 当前配置字段与默认值

以下由本次源码字段声明提取；`null（未初始化）`不是推荐生产值。

### BaseProperty

`import com.mongoplus.model.BaseProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/BaseProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `url` | `String` | `null（未初始化）` |
| `srv` | `boolean` | `false` |
| `host` | `String` | `null（未初始化）` |
| `port` | `String` | `null（未初始化）` |
| `database` | `String` | `null（未初始化）` |
| `username` | `String` | `null（未初始化）` |
| `password` | `String` | `null（未初始化）` |
| `authenticationDatabase` | `String` | `null（未初始化）` |
| `position` | `Integer` | `Integer.MIN_VALUE` |
| `minPoolSize` | `Integer` | `null（未初始化）` |
| `maxPoolSize` | `Integer` | `null（未初始化）` |
| `waitQueueTimeoutMS` | `Integer` | `null（未初始化）` |
| `serverSelectionTimeoutMS` | `Integer` | `null（未初始化）` |
| `localThresholdMS` | `Integer` | `null（未初始化）` |
| `heartbeatFrequencyMS` | `Integer` | `null（未初始化）` |
| `replicaSet` | `String` | `null（未初始化）` |
| `ssl` | `Boolean` | `null（未初始化）` |
| `clientKeyStore` | `String` | `null（未初始化）` |
| `jks` | `String` | `null（未初始化）` |
| `keyPassword` | `String` | `null（未初始化）` |
| `invalidHostNameAllowed` | `boolean` | `false` |
| `tls` | `Boolean` | `null（未初始化）` |
| `tlsInsecure` | `Boolean` | `null（未初始化）` |
| `tlsAllowInvalidHostnames` | `Boolean` | `null（未初始化）` |
| `connectTimeoutMS` | `Integer` | `null（未初始化）` |
| `socketTimeoutMS` | `Integer` | `null（未初始化）` |
| `maxIdleTimeMS` | `Integer` | `null（未初始化）` |
| `maxLifeTimeMS` | `Integer` | `null（未初始化）` |
| `journal` | `Boolean` | `null（未初始化）` |
| `w` | `String` | `null（未初始化）` |
| `wtimeoutMS` | `Integer` | `null（未初始化）` |
| `readPreference` | `String` | `null（未初始化）` |
| `readPreferenceTags` | `String` | `null（未初始化）` |
| `maxStalenessSeconds` | `Integer` | `null（未初始化）` |
| `authMechanism` | `String` | `null（未初始化）` |
| `authSource` | `String` | `null（未初始化）` |
| `authMechanismProperties` | `String` | `null（未初始化）` |
| `appName` | `String` | `null（未初始化）` |
| `compressors` | `String` | `null（未初始化）` |
| `zlibCompressionLevel` | `Integer` | `null（未初始化）` |
| `retryWrites` | `Boolean` | `null（未初始化）` |
| `retryReads` | `Boolean` | `null（未初始化）` |
| `uuidRepresentation` | `String` | `null（未初始化）` |
| `directConnection` | `Boolean` | `null（未初始化）` |
| `maxConnecting` | `Integer` | `null（未初始化）` |
| `srvServiceName` | `String` | `null（未初始化）` |

### MongoDBConnectProperty

`import com.mongoplus.property.MongoDBConnectProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoDBConnectProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `slaveDataSource` | `List<SlaveDataSource>` | `null（未初始化）` |

### MongoDBConfigurationProperty

`import com.mongoplus.property.MongoDBConfigurationProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoDBConfigurationProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `banner` | `Boolean` | `true` |
| `autoIdCollectionName` | `String` | `null（未初始化）` |
| `ikun` | `Boolean` | `false` |
| `autoConvertObjectId` | `Boolean` | `true` |
| `objectIdConvertType` | `Boolean` | `false` |
| `autoCreateIndex` | `Boolean` | `false` |
| `autoCreateTimeSeries` | `Boolean` | `false` |
| `autoScanPackages` | `List<String>` | `null（未初始化）` |
| `lazyDataSource` | `Boolean` | `false` |

### MongoDBCollectionProperty

`import com.mongoplus.property.MongoDBCollectionProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoDBCollectionProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `mappingStrategy` | `CollectionNameConvertEnum` | `CollectionNameConvertEnum.ALL_CHAR_LOWERCASE` |
| `blockAttackInner` | `Boolean` | `false` |
| `blockAttackInnerOrder` | `int` | `1` |

### MongoDBFieldProperty

`import com.mongoplus.property.MongoDBFieldProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoDBFieldProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `camelToUnderline` | `Boolean` | `false` |
| `ignoringNull` | `Boolean` | `true` |

### MongoLogicDelProperty

`import com.mongoplus.property.MongoLogicDelProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoLogicDelProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| 继承字段 | 见父配置类 | 见父配置类 |

### LogicProperty

`import com.mongoplus.model.LogicProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/LogicProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `open` | `Boolean` | `false` |
| `autoFill` | `Boolean` | `false` |
| `logicDeleteField` | `String` | `null（未初始化）` |
| `logicDeleteValue` | `String` | `"1"` |
| `logicNotDeleteValue` | `String` | `"0"` |
| `logicDataType` | `LogicDataType` | `LogicDataType.DEFAULT` |

### MongoEncryptorProperty

`import com.mongoplus.property.MongoEncryptorProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoEncryptorProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `privateKey` | `String` | `null（未初始化）` |
| `publicKey` | `String` | `null（未初始化）` |
| `key` | `String` | `null（未初始化）` |

### MongoSpringProperty

`import com.mongoplus.property.MongoSpringProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoSpringProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `transaction` | `Boolean` | `false` |
| `overrideMongoClient` | `Boolean` | `true` |

### MongoDBLogProperty

`import com.mongoplus.property.MongoDBLogProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/MongoDBLogProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `log` | `Boolean` | `false` |
| `pretty` | `Boolean` | `false` |
| `logOrder` | `int` | `0` |
| `format` | `Boolean` | `false` |

### SensitiveWordConfigurationProperty

`import com.mongoplus.property.SensitiveWordConfigurationProperty;`；[源码](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/property/SensitiveWordConfigurationProperty.java)。

| 字段（配置使用kebab-case） | Java类型 | 默认声明 |
|---|---|---|
| `sensitiveType` | `SensitiveType` | `SensitiveType.LOCAL` |
| `ignoreCase` | `boolean` | `true` |
| `ignoreWidth` | `boolean` | `true` |
| `ignoreNumStyle` | `boolean` | `true` |
| `ignoreChineseStyle` | `boolean` | `true` |
| `ignoreEnglishStyle` | `boolean` | `true` |
| `ignoreRepeat` | `boolean` | `false` |
| `ignoreChar` | `boolean` | `false` |
| `enableNumCheck` | `boolean` | `false` |
| `enableEmailCheck` | `boolean` | `false` |
| `enableUrlCheck` | `boolean` | `false` |
| `enableIpv4Check` | `boolean` | `false` |

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C094 连接池TLS认证UUID与URI**：`BaseProperty`, `UrlJoint`, `OverridableUuidRepresentationCodecProvider`。
- **C095 配置模型优先级与绑定**：`MongoDBConnectProperty`, `MongoDBConfigurationProperty`, `MongoDBFieldProperty`, `MongoSpringProperty`。
- **C096 配置加密环境后处理**：`EncryptionPostProcessor`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
