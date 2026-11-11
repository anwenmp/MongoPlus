# 时序集合

`@TimeSeries`是集合创建配置，不是索引。完整7个属性、默认值、import与最小实体见[注解记录](../annotations/mapping.md#timeseries)。典型场景为设备测量：timeField是测量时间，metaField是稳定的设备/标签元数据，普通值字段保存读数。

自动入口是`auto-create-time-series`和扫描到的实体，Core也可调用`AutoUtil.autoCreateTimeSeries(Collection<Class<?>>, MongoPlusClient)`。timeField/metaField通过实体字段名核对；granularity默认SECONDS，bucketMaxSpan/bucketRounding按秒设置，expireAfter为集合保留秒数。自定义bucket选项与granularity的选择由AutoUtil的当前条件分支控制，不拼造对称属性名。

流程会先listCollections(type=timeseries)，已有同名时序集合则返回；已有普通集合不会迁移为时序集合。createCollection当前捕获MongoCommandException，所以“方法未抛异常”不能证明DDL成功，必须独立列举集合选项并插入/读取合法BSON Date样本。测试过期要考虑服务端后台清理延迟，不能假定到秒立即删除。

DDL、时间字段类型、metaField、bucket配置、保留期分别验证；运行记录见[验收报告](../audit/coverage-report.md)。与一般TTL索引区分见[索引](indexes.md)。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C091 自动时序集合**：`AutoUtil#autoCreateTimeSeries`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
