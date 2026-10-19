# 从业务需求到命令失败通知

这是维护者依据真实 API 编写的演示，不是智能体自动触发的测试记录。

## 需求与环境

“我希望每次数据库命令失败时发送通知。”目标是已有 MongoPlus 2.2.0 + Boot starter 项目。先确认这是命令失败的尽力通知；若还要求事务提交后、不丢失、自动重试或恰好一次，本示例不能满足这些保证。

## 发现、比较、核对

从 Skill 的父目录执行：

```sh
python mongoplus/scripts/search.py search-capability "我希望每次数据库命令失败时发送通知" --limit 1
python mongoplus/scripts/search.py search-api "com.mongoplus.listener.Listener" --kind method
python mongoplus/scripts/search.py search-api "getThrowable" --owner com.mongoplus.model.command.CommandFailed
python mongoplus/scripts/search.py search-api "getCommandName" --owner com.mongoplus.model.command.BaseCommand
python mongoplus/scripts/search.py show-evidence mp.command-events --path MongoPlusListener --limit 3
python mongoplus/scripts/search.py show-evidence mp.command-events --path MongoPlusAutoConfiguration --limit 3
```

相关能力是 `mp.command-events`。保留结果中的 limitations/verification：命令事件不是业务操作或事务提交事件。CRUD `Interceptor` 适合执行边界参数处理；Recorder 是数据变更记录；此需求直接对应命令 `Listener`。

2.2.0 索引中的真实接口为 `com.mongoplus.listener.Listener`：

```java
void commandStarted(CommandStarted commandStarted);
void commandSucceeded(CommandSucceeded commandSucceeded);
void commandFailed(CommandFailed commandFailed);
int getOrder();
```

失败事件类型是 `com.mongoplus.model.command.CommandFailed`；`getThrowable()` 返回 `Throwable`，继承的 `BaseCommand.getCommandName()` 返回 `String`。`Configuration.listener(Class<? extends Listener>...)` 接受类而非实例；本例需要注入业务适配器，所以选择 Boot 的 Listener Bean 收集路径。不要编造 `addListener(instance)` 或把构造注入类交给反射无参实例化入口。

`MongoPlusListener` 同步遍历全局列表；异常可能终止后续回调。使用快速、线程安全的业务适配器，隔离其普通运行异常。该措施仍不提供队列持久化、关闭排空或投递保证。若存在更早抛异常的其他 Listener，当前 Listener 可能收不到事件；排序、重复初始化和 Driver 实际回调需运行测试。

## Java 实现

可复制的完整源文件是 [CommandFailureListener.java](../assets/command-notification/src/main/java/example/CommandFailureListener.java) 与 [CommandNotificationConfiguration.java](../assets/command-notification/src/main/java/example/CommandNotificationConfiguration.java)。使用者提供 `FailureNotificationSink` Bean，连接已有通知设施；该接口是示例业务代码，不是 MongoPlus API。通知适配器应在有界时间内返回；同步网络发送会延长 Driver 回调。

```java
@Bean
public Listener commandFailureListener(FailureNotificationSink sink) {
    return new CommandFailureListener(sink);
}
```

失败回调只转发命令名和异常类名，不转发命令 BSON、异常消息、查询条件或业务文档。适配器失败会记录异常类型并返回，通知可能丢失；这必须与用户可靠性要求一致。样例没有配置真实发送服务，也不会自动发送通知或写数据库。

## 验证

独立 [示例 POM](../assets/command-notification/pom.xml) 可在已有依赖缓存或允许获取依赖时执行 `mvn -f mongoplus/assets/command-notification/pom.xml test-compile`。Spring 6.2.2 对应此示例的 Boot 3.4.2 环境，需要 JDK 17+；请沿目标项目实际 starter 版本调整，不能把此依赖组合作为所有客户端项目模板。原生非 Spring 项目不复制 Spring 配置类。

[本地 smoke main](../assets/command-notification/src/test/java/example/CommandFailureListenerSmokeTest.java) 仅手动构造 `CommandFailed`，检查通知载荷与适配器异常隔离；Maven `test-compile` 只编译它，不自动运行 main。它不证明真实 Driver 回调、Spring Bean 收集、跨线程、事务、重试或通知送达。实际执行与未执行项见交付报告；真实数据库和真实智能体调用均单独记录为 `NOT_RUN`。
