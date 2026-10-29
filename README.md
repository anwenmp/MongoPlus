<p align="center">
  <a href="https://gitee.com/anwena/mongo-plus/blob/master/LICENSE">
    <img src="https://img.shields.io/hexpm/l/plug.svg" alt="License">
  </a>
<a href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
	<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
</a>
<a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
	<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
</a>
<a href='https://gitee.com/aizuda/mongo-plus/stargazers'>
  <img src='https://gitee.com/aizuda/mongo-plus/badge/star.svg?theme=gvp' alt='star'/>
</a>
<a href='https://gitee.com/aizuda/mongo-plus/members'>
  <img src='https://gitee.com/aizuda/mongo-plus/badge/fork.svg?theme=gvp' alt='fork'/>
</a>
<a href="https://gitcode.com/aizuda/mongo-plus/stargazers">
  <img src='https://gitcode.com/aizuda/mongo-plus/star/badge.svg' alt='star'/>
</a>
</p>
<p style="text-align: center;">
<img style="width: 200px;display: inline-block;" src="logo.png" alt="MongoPlusLogo">
</p>

### 介绍：

#### 🔥🔥🔥使用MyBatisPlus的方式，优雅的操作MongoDB
  
* #### 随风潜入夜
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可和现有Mongo框架结合使用，无侵入性
* #### 简化开发
  &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;只需简单配置，即可快速进行CRUD操作，链式调用更加便捷

### 优点：

* 无侵入：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑
* 损耗小：启动即会自动注入基本 CURD，性能基本无损耗，直接面向对象操作
* 强大的 CRUD 操作：通用 Service，仅仅通过少量配置即可实现单表大部分 CRUD 操作，更有强大的条件构造器，满足各类使用需求
* 支持 Lambda 形式调用：通过 Lambda 表达式，方便的编写各类查询条件，无需再担心字段写错
* 支持主键自动生成：支持多达 5 种主键策略（内含分布式唯一 ID 生成器 - Sequence），可自由配置，完美解决主键问题
* 支持无实体类情况下的操作
* 支持动态数据源
* 支持逻辑删除、防止全集合更新和删除、自动填充等等功能

### 快速开发

MyBatisPlus就是可以不用像MyBatis一样写过多的sql语句，Mongo-Plus也一样！

### 项目已接入墨菲安全
##### 点击查看检测报告
[![Security Status](https://www.murphysec.com/platform3/v31/badge/1810204695353073664.svg)](https://www.murphysec.com/console/report/1810204585500057600/1810204695353073664)

### 文档
文档地址： https://mongoplus.com/

### core 编程式配置

直接使用 `mongo-plus-core` 时，可通过现有 `Configuration` 链式配置全局行为：

```java
Configuration configuration = Configuration.builder()
        .camelToUnderline(true)
        .ignoringNull(true)
        .autoConvertObjectId(true)
        .objectIdConvertType(false)
        .autoIdCollectionName("counters")
        .logOrder(0)
        .blockAttackInnerOrder(1)
        .log(true)
        .blockAttackInner();
```

`Configuration` 的完整类名为 `com.mongoplus.config.Configuration`。`log(true)` 输出可执行语句，监听器按 order 从小到大执行。全局加密密钥可使用 `key(...)`、`publicKey(...)`、`privateKey(...)` 配置，日志模式可使用 `ikun(...)` 配置。以上设置在当前 JVM 中共享，应在首次映射、日志或客户端初始化前完成；新建 builder 不会重置它们。

`MongoPlusConfiguration` 中的核心扩展也可直接配置：`codec(...)` 注册 Driver 编解码器，`simpleTypeHolder(...)` 设置简单类型容器，`mongoConverter(...)` 设置 Mapper 转换器，`annotationHandler(...)` 设置集合注解属性解析器，`banner(true)` 开启启动 Banner（core 默认关闭）。

默认连接使用 `connection(url).database(database)`；已有 MongoClient 可用 `mongoClient(...)` 接入，命名数据源使用 `dataSource(name, property)` 或 `dataSource(name, client, property)`。例如以下客户端、属性和转换器由调用方提供：

```java
Configuration configuration = Configuration.builder()
        .mongoClient(existingClient)
        .database("app")
        .dataSource("reporting", reportingClient, reportingProperty)
        .mongoConverter(customConverter);
MongoPlusClient client = configuration.getMongoPlusClient();
BaseMapper mapper = configuration.getBaseMapper();
DataSourceManager manager = configuration.getDataSourceManager();
```

同一个配置在连接参数未改变时复用已构建的客户端，显式设置的 ID 生成器会保留。修改连接或命名数据源配置后，下次获取会重新构建；旧客户端不会自动关闭。Codec 只作用于之后由 MongoPlus 创建的 MongoClient；外部客户端的编解码器和命令监听器由调用方配置。工厂及全局缓存仍在 JVM 内共享。

### 联系我们
#### 在使用MongoPlus的过程中，有任何问题和想法或者加入群聊请联系我(卖服务器的不要加了)
#### v：anwen_529
#### 备注：MongoPlus
<img src="wx.png" alt="微信">

###  参与贡献

1.  Star and Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request
