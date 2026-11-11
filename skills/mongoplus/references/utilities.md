# 公开辅助类型、默认实现和异常

框架中public不都等于业务首选API。全量声明的角色、排除理由和源码见[公开类型矩阵](../audit/public-types.md)。低层方法不逐一写业务教程，但每个类型都可检索到主题或明确角色。

| 类别 | 内容与决策 |
|---|---|
| BSON | MongoPlusDocument/MongoPlusBasicDBObject、ImmutableDocument、EmptyDocument：按可变/不可变语义选择，不能把名字当完整契约 |
| 结果与参数 | PageParam/PageResult、Pair/MutablePair、QueryParam、OperationResult、Retry/UpdateRetryResult等，按真实泛型及返回类型消费 |
| ID/路径工具 | IdWorker/ULID、FieldChain/FreeFieldChain等公开协作类型；优先从实体注解与高层API进入 |
| 几何、枚举与选项 | annotation模块枚举和Geo模型随对应功能使用；同名Driver或兼容包注意import |
| 异常 | MongoPlusException及映射/数据源/索引/写异常；MongoPlusInterceptorException继承Error，不能假设catch(Exception)覆盖所有框架异常 |
| 缓存/代理/执行器/扫描/日志/元信息 | 标为INTERNAL_PUBLIC，保留逐类型来源和角色；不作为业务稳定扩展契约的保证 |
| 默认策略实现 | 读写转换、executor strategy、内置拦截器等，说明支持类型和调用链；业务扩展依赖对应接口 |

没有因为“不常用”删除功能。排除的是逐个基础设施方法的教程化和不被当前版本提供的虚构入口。缺乏运行契约的组合保持UNVERIFIED，不因代码public便承诺所有上下文支持。精确的核心辅助签名见[辅助API](api/utilities.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C099 公开BSON工具与异常模型**：`MongoPlusDocument`, `MongoPlusBasicDBObject`, `MongoPlusException`, `OperationResult`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
