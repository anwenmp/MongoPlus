# 命令失败告警与跨库同步

“命令失败时给值班台记一条告警。” 在 Starter 中注册 `com.mongoplus.listener.Listener` Bean。框架的 `BaseListener` 把 Driver 事件转成 `CommandStarted`、`CommandSucceeded`、`CommandFailed`。这里只依赖命令名与异常类型，可把计数器或非阻塞告警队列适配成回调。

```java
import com.mongoplus.listener.Listener;
import com.mongoplus.model.command.CommandFailed;
import com.mongoplus.model.command.CommandStarted;
import com.mongoplus.model.command.CommandSucceeded;
import java.util.function.BiConsumer;

class CommandAlarm implements Listener {
    private final BiConsumer<String, String> record;
    CommandAlarm(BiConsumer<String, String> record) { this.record = record; }
    public void commandStarted(CommandStarted event) {}
    public void commandSucceeded(CommandSucceeded event) {}
    public void commandFailed(CommandFailed event) {
        record.accept(event.getCommandName(), event.getThrowable().getClass().getSimpleName());
    }
    public int getOrder() { return LOWEST_PRECEDENCE; }
}
```

不要在 Driver 回调里同步发 HTTP 或抛出告警系统异常。原始 command 可能含业务数据。自建 MongoClient 时确认其 settings 有桥接监听器。测试用服务端拒绝的命令（例如测试管道使用未知阶段）；`ok:1` 的写响应带 `writeErrors` 仍可能产生成功命令事件。连接建立前或业务校验抛出的异常也不一定产生失败命令事件。

## 把订单增删改同步到 MySQL

| 数据来源与要求 | 可用机制和边界 |
|---|---|
| 只记录本进程 MongoPlus 发起的操作 | 普通拦截器或 `DataChangeRecorderInnerInterceptor` 可观察框架操作。操作参数与成功回调无法代表外部写入、最终文档或事务提交 |
| 在配置的 MongoDB 数据源之间异步镜像 | `AsyncMultipleWriteInterceptor` 会安排 Mongo 目标写，再调用 `proceed()`。主写失败、事务回滚或进程退出时需要单独考虑；它没有 MySQL sink 或持久消费位点 |
| 覆盖其他应用、脚本和数据库直接写入 | 在支持 Change Streams 的副本集或分片集群消费数据库事件。这里需要 Driver 的 `watch()`；租户拦截器不会过滤这条流，消费端单独限定集合、权限和数据范围 |
| 只同步应用内的业务事件，允许改造写路径 | 同一 MongoDB 事务内保存业务文档与 outbox，之后投递、重试、去重；外部绕过 outbox 的写入需另一条捕获路径 |

下面是从一个已保存位点恢复的集合订阅。`resumeAfter` 输入是原始 BSON token。调用方持有并关闭游标，消费循环应有可停止的生命周期。使用者负责 MySQL 表结构与投递实现。

```java
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoChangeStreamCursor;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import com.mongoplus.mapper.MongoMapper;
import org.bson.BsonDocument;
import org.bson.Document;

class OrderChanges {
    static MongoChangeStreamCursor<ChangeStreamDocument<Document>> open(
            MongoMapper<?> orders, BsonDocument checkpoint) {
        ChangeStreamIterable<Document> stream = orders.getCollection().watch()
            .fullDocument(FullDocument.UPDATE_LOOKUP);
        if (checkpoint != null) stream = stream.resumeAfter(checkpoint);
        return stream.cursor();
    }
}
```

对 insert/replace/update 用 documentKey 和业务版本做幂等 upsert，对 delete 用 documentKey 执行删除或墓碑。`UPDATE_LOOKUP` 可能看到后续版本或拿不到全文；有逐变更精确还原要求时核验部署的前/后镜像配置或使用业务事件。不同源数据类型要明确映射成稳定 MySQL 主键。

在同一 MySQL 事务内写业务行、去重事件和消费位点，提交后才推进内存游标；提交结果不确定时允许重放。按源顺序处理同一文档，多个 worker 需协调位点。空位点默认从现在开始，历史数据要有与增量流衔接的初始快照、水位与对账。遇到 token 过期、流失效或不可恢复的 schema 变化，进入补数/重建流程。这里能设计最终一致性；MongoPlus 单库事务不提供 MongoDB 与 MySQL 同时提交。验收要断网重连、重复投递、删除、消费崩溃恢复并比较两库内容。

依据 · [Listener](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/Listener.java) · [BaseListener](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/listener/BaseListener.java) · [CommandFailed](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/model/command/CommandFailed.java) · [DataChangeRecorderInnerInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/DataChangeRecorderInnerInterceptor.java) · [AsyncMultipleWriteInterceptor](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/interceptor/business/AsyncMultipleWriteInterceptor.java) · [MongoMapper](https://github.com/anwenmp/MongoPlus/blob/1aacff901e33d33797cb957d883388519ef5ae6f/mongo-plus-core/src/main/java/com/mongoplus/mapper/MongoMapper.java) · [官方命令监听器](https://mongoplus.com/docs/listener.html) · [MongoDB Change Streams](https://www.mongodb.com/docs/manual/changeStreams/)
<!-- COMPLETE_COVERAGE -->
## 框架自带记录与异步镜像

DataChangeRecorderInnerInterceptor在框架操作前后记录变更，支持setDatasourceName、setDatabaseName、setCollectionName、enableSaveDatabase、setDisplayCompleteData、setIgnoredColumnList、setBatchUpdateLimit等配置。核对字段过滤和批量更新上限，避免把完整文档审计默认当作合适的生产设置；成功回调不等于事务最终提交。它不覆盖外部客户端写入。

异步多写用Configuration.asyncMultipleWrite(MultipleWriteHandler)，或注册AsyncMultipleWriteInterceptor。MultipleWriteHandler构造时按操作维护目标数据源列表，addMultipleWriteStrategy可以改映射；multipleWrite(currentDataSource,namespace)可返回定制列表，getMultipleWrite选择最终目标。默认策略涉及已配置数据源，初始化前必须审阅目标范围。

该拦截器安排异步镜像后继续主调用；不提供外部消息持久位点、跨库原子提交或MySQL sink。验证需覆盖主写失败、事务回滚、镜像部分失败、重放、进程退出、Executor与client关闭。未做这些故障测试时标UNVERIFIED，不把一次主库成功写入当作镜像一致性通过。完整公开签名见[扩展API](api/extensions.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C069 命令监听日志与错误通知**：`Listener`, `BaseListener`, `MongoPlusListener`, `LogListener`。
- **C097 异步镜像多写**：`AsyncMultipleWriteInterceptor`, `MultipleWriteHandler`。
- **C098 数据变动记录**：`DataChangeRecorderInnerInterceptor`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
