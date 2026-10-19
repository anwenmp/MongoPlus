# MongoPlus API 源码索引

仅记录显式声明；类型名未做跨文件解析，不展开继承或 Lombok 成员。

全仓库扫描 622；成功 622；失败 0。本页 3 条完整源码记录（含内部实现）。

## 模块：mongo-plus-sharding-boot-starter

### 包：com.mongoplus.config

#### class com.mongoplus.config.MongoShardingConfiguration

可见性：public；泛型：[]；extends：[]；implements：[InitializingBean]

源码：mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:11-29

##### method afterPropertiesSet

```java
public void afterPropertiesSet() throws Exception
```

所属类型：com.mongoplus.config.MongoShardingConfiguration；可见性：public

源码：mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:13-16

```java
@Override
```

##### method dataSourceShardingInterceptor

```java
public DataSourceShardingInterceptor dataSourceShardingInterceptor(MongoPlusClient mongoPlusClient)
```

所属类型：com.mongoplus.config.MongoShardingConfiguration；可见性：public

源码：mongo-plus-sharding-boot-starter/src/main/java/com/mongoplus/config/MongoShardingConfiguration.java:23-27

```java
@Bean
@ConditionalOnMissingBean
```

```text
* 注册分片拦截器
     * @param mongoPlusClient mongoPlusClient
     * @author anwen
```

