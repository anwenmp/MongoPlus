# 敏感词模块

使用`mongo-plus-sensitive-word`，其Core依赖为provided；Starter按条件装配，不能只有Core配置属性就认定词库已初始化。注解路径是`com.mongoplus.annotation.SensitiveWord`，不是annotation.comm子包。

| 场景 | API及生效方式 | 限制 |
|---|---|---|
| 只处理选定字段 | SensitiveType.LOCAL + @SensitiveWord + SensitiveWordFieldHandler | 注解需要字段链节点与Manager注册 |
| 全局写操作审核 | SensitiveType.GLOBAL + SensitiveWordInterceptor | save/update/bulk各策略分别核对，不能推定所有原生命令被拦截 |
| 添加黑名单/移除词 | SensitiveWordManager.addWordBlacklist / addWordBlackList / removeSensitiveWord | 两种拼写都存在，按真实重载使用 |
| 白名单 | addSensitiveWordAllow / removeSensitiveWordAllow | 词库变更并发及重建行为需核对 |
| 初始化外部词库 | LoadExtraWord.allow/deny | 实现houbb提供的接口要求，不编造Spring资源扫描契约 |
| 审核或显式替换文本 | SensitiveWordManager.handler/replace | handler命中抛SensitiveWordException；replace返回替换后的字符串。ResultHandler虽然公开，未发现消费点，不能说配置MASK即可使字段链回写掩码 |

配置模型`SensitiveWordConfigurationProperty`默认LOCAL、ignoreCase/Width/NumStyle/ChineseStyle/EnglishStyle为true，ignoreRepeat=false，各类数字/邮箱/URL/IP检测默认false。`SensitiveWordProperty.sensitiveWordBs`当前固定使用specialChars忽略器，虽然有ignoreChar字段，不应宣称其绑定值控制该行为。

Boot配置前缀是`mongo-plus.sensitive-word`，例如`sensitive-type: LOCAL`。SensitiveWordManager构造器会构建词库、注册自身到HandlerRegistry并调用initSensitiveType安装字段Handler或全局Interceptor；调用者不要再重复初始化。独立Core或需要显式接线的Solon可在应用启动时使用：

```java
import com.mongoplus.manager.SensitiveWordManager;
import com.mongoplus.property.SensitiveWordProperty;
import com.mongoplus.enums.SensitiveType;

class LocalWordSetup {
    static SensitiveWordManager initialize() {
        SensitiveWordProperty property = new SensitiveWordProperty();
        property.setSensitiveType(SensitiveType.LOCAL);
        SensitiveWordManager manager = new SensitiveWordManager(property);
        manager.addWordBlacklist("example-blocked-word");
        manager.addSensitiveWordAllow("example-allowed-word");
        return manager;
    }
}
```

LOCAL字段Handler检验当前值并在命中时拒绝；它返回null，不自动以replace结果替换字段值。需要持久化掩码时显式调用replace并用业务写入路径保存。可使用Manager的管理API维护词库；不要在请求里反复注册全局静态链。读取词库并发更新的完整契约仍需测试。

注解最小声明与完整import见[注解记录](../annotations/field-processing.md#sensitiveword)。方法签名在[可选模块API](api/optional-modules.md)。运行策略与并发词库更新本次未逐项执行时标`UNVERIFIED`，不提供假定通过的结果。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C066 敏感词局部全局及动态词库**：`SensitiveWordManager`, `SensitiveWordProperty`, `LoadExtraWord`, `SensitiveWordInterceptor`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
