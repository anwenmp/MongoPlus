# 管道聚合查询示例
> 通过该管道查询示例，您可以快速地熟悉MongoPlus管道查询的使用方法。
### 使用方式
- 执行com.anwen.aggregate.AggregateTest类即可

### 注意
- 该示例适用于2.1.4+版本，如较低版本，可能有些方法不兼容，请自行修改。
- 单元测试方法名既对应管道的各个阶段，如match、project等
- 示例中使用字符串进行构建条件，实际使用可以将字符串替换为Lambda，如: eq(User::getUserName)