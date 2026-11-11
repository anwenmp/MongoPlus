# 扩展注册、字段处理链与生命周期

检索词：TypeHandler、MappingStrategy、ConversionStrategy、MetaObjectHandler、FieldHandler、ReadHandler、ConditionHandler、Codec、Aware、ComponentRegistry。业务先选择现成注解/公开扩展接口，默认实现和静态缓存留作定位依据。

| 扩展 | 调用阶段与输入输出 | 注册入口 | 不应混淆 |
|---|---|---|---|
| MappingStrategy<T> | Java对象写为MongoDB可编码值 | Boot/Solon收集泛型Bean，或MappingCache.putMappingStrategy | ConversionStrategy用于读转换 |
| ConversionStrategy<T> | 数据库值转为目标Java类型 | Configuration.convert、ConversionCache；容器收集 | 全局转换不等于某字段TypeHandler |
| TypeHandler | 字段setParameter / getResult | `@CollectionField(typeHandler=...)` | 类须实现真实接口；不是MyBatis TypeHandler |
| MetaObjectHandler | insertFill/updateFill | Configuration.metaObjectHandler或容器Bean | wrapper-only/raw BSON不会自动走实体fill |
| FieldHandler | 实体写入字段值处理 | FieldHandlerChain.getInstance().register/registerAll | 不保证仅声明Bean就被收集 |
| ReadHandler | 类型转换后、字段赋值前读取处理 | HandlerCache.addReadHandler；Boot3也收集Bean | Boot4/Solon接线差异见集成页 |
| ConditionHandler | 查询/更新条件构建前后 | HandlerCache.conditionHandlerList等当前注册入口 | 不是执行器Interceptor |
| Interceptor / AdvancedInterceptor | Mapper执行边界/高级改写 | Configuration或容器Bean、对应链 | 顺序和短路见[拦截器](interceptors.md) |
| Listener | Driver命令开始/成功/失败 | Configuration.listener或容器Bean | 不观察其他客户端写入，见[事件](events.md) |
| MongoPlusCodec<T> | BSON编码解码 | Configuration.codec或Starter建客户端前收集 | 客户端已创建后追加不能假定立即替换CodecRegistry |
| Aware / NamespaceAware | 命名空间登记感知 | Configuration.aware或容器收集 | 不是按每条数据库变更触发 |
| MongoConverter | 整体write/read契约替换 | Configuration.mongoConverter或容器MongoConverter Bean | 替换后须保留ID、映射、类型、Handler行为 |

当前写链在注册时按`FieldHandler.order()`升序排序。默认节点是TypeHandlerFieldHandler、EncryptFieldHandler、DBRefHandler；`MappingMongoConverter`把前序产生的最新`currentValue`传给`handler(fieldInformation,currentValue)`。返回null表示不替换当前值。旧单参数`handler(FieldInformation)`已弃用，双参数默认实现仍委托它，旧实现可能仍从元数据读取原值。不要沿用旧文档中“所有节点都只收到原始值”的结论。

精确默认order：FieldHandler/TypeHandlerFieldHandler为0，EncryptFieldHandler为Integer.MAX_VALUE-1，DBRefHandler为Integer.MAX_VALUE；敏感词LOCAL节点为Integer.MIN_VALUE。自定义节点取10、20等值可在TypeHandler后、加密前串联；若必须在TypeHandler前用负数并检查敏感词顺序。相同order不建立跨容器稳定顺序。读取侧FieldEncryptApply为0、DesensitizationHandlerApply为1、DBRefHandler为Integer.MAX_VALUE。

```java
import com.mongoplus.handlers.FieldHandler;
import com.mongoplus.handlers.FieldHandlerChain;
import com.mongoplus.mapping.FieldInformation;
import java.util.function.Function;

class TrimField implements FieldHandler {
    public Integer order() { return 10; }
    public Function<FieldInformation,Boolean> activate() {
        return f -> f.getTypeClass() == String.class;
    }
    public Object handler(FieldInformation f) { return handler(f, f.getValue()); }
    public Object handler(FieldInformation f, Object value) {
        return value instanceof String ? ((String)value).trim() : value;
    }
    static void register() { FieldHandlerChain.getInstance().register(new TrimField()); }
}
```

ReadHandler默认order为Integer.MAX_VALUE，按升序调用；`activate`限定字段，`discontinue`决定停止。默认读取处理包括FieldEncryptApply、DesensitizationHandlerApply、DBRefHandler。`HandlerCache.setReadHandler`当前是追加而非清空替换，注册必须避免在每个请求反复执行。FieldBind要额外注册`new FieldBindHandler(dataBindHandler)`，不是内置默认链节点。

静态链、缓存、组件注册表可能跨应用上下文累积。只在启动注册，明确实例数量、顺序及关闭责任；相同order不能当作跨容器稳定顺序。跨类型组合（类型处理→加密→引用等）应比较中间值、最终BSON和回读对象，逐路径覆盖实体、DTO、Map/Document、batch、wrapper-only与aggregate，不能只测一个入口。

完整扩展类型与源码见[公开类型矩阵](../audit/public-types.md)，当前签名见[API分族目录](api/index.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C059 FieldHandler写责任链**：`FieldHandler`, `FieldHandlerChain`。
- **C060 ReadHandler读取链与中断**：`ReadHandler`, `HandlerCache`。
- **C070 ConditionHandler条件转换**：`ConditionHandler`, `BuildCondition`。
- **C071 Codec与JSR310类型**：`MongoPlusCodec`, `Jsr310CodecProvider`。
- **C072 Aware命名空间事件**：`Aware`, `NamespaceAware`。
- **C073 扩展对象注册与生命周期**：`Configuration`, `ComponentRegistry`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
