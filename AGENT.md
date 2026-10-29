# 本地开发测试规则

## 工程边界

- 开发测试工程位于仓库根目录的 `mongo-plus-test/`，是独立 Maven 工程，不是 MongoPlus reactor 子模块。
- parent 使用独立的 Spring Boot parent，`relativePath` 为空；不得继承 MongoPlus parent，不加入根 `<modules>`、BOM 或发布批次。
- 新增开发回归、框架集成和真实 MongoDB 测试集中放在该工程。既有其他模块测试保留，不为通过构建删除、弱化或跳过已有断言。
- Boot 3、Boot 4、Solon、纯 core 使用互斥 Maven profile，避免同包同名 Starter 类及不同 Spring 主版本同时进入 classpath。
- Boot 测试使用对应版本 BOM 对齐全部 Spring 依赖，默认命令不得依赖临时 `spring-tx.version` 参数。

## 本地文件与配置

- 整个 `mongo-plus-test/` 暂时仅本地保留，通过 `.git/info/exclude` 排除；不得暂存、提交、推送，也不得强制加入 Git。
- MongoDB 地址、三个端口、admin 认证库、账号和密码按用户要求直接保存在该工程的 `src/test/resources/application.yml`。
- YAML 使用真实 Starter 配置结构 `mongo-plus.data.mongodb`；Spring 事务开关使用 `mongo-plus.spring.transaction`。不得用自定义根节点的手工绑定测试替代 Starter 配置验证。
- 不把明文凭据复制到公共文档、日志、截图或异常摘要中。此规则文件只记录配置位置，不记录凭据。
- 测试产物放在独立工程的 `target/`，使用本次工作树构建的本地构件，不能把旧仓库 JAR 的结果当成本次验证。

## 执行与结果

- 先构建并安装待测的当前本地构件，再执行 `mvn -f mongo-plus-test/pom.xml test`。该默认命令运行 Boot 3 回归。
- Boot 4 使用 `-Pboot4`；Solon 使用 `-Psolon`；纯 core 使用 `-Pcore`。切换 profile 使用 `clean test`，避免残留 class 混入下一组测试。
- 默认单元/容器测试不连接数据库。真实 MongoDB 验证通过 `'-Pboot3,mongodb-live' verify` 显式启用。
- Spring 注解事务深度验证使用 `'-Pboot3,transaction-live' clean verify` 或 `'-Pboot4,transaction-live' clean verify`。切换 `mongodb-live` 和 `transaction-live` 也必须 `clean`，两者在同一框架 profile 下共享输出目录。
- 真实测试仅使用专用测试数据库和本次随机命名的测试集合，结束后只清理自己创建的记录/集合，不删除数据库，不接触业务数据。
- 事务测试必须经过真实 Spring 代理、待测管理器及 MongoPlus Mapper，并用独立客户端核对提交前不可见、提交后可见、回滚后不存在；直接 Driver 事务测试单独标为冒烟验证。
- 检查 `REQUIRED`、`REQUIRES_NEW`、`NOT_SUPPORTED` 等传播行为、回滚规则、线程复用、并发 session、异常清理及事务选项。`readOnly` 提示、自调用代理边界和不支持 savepoint 属于边界测试，不能直接记为缺陷。
- Driver 接口故障注入与真实 Driver/数据库场景分开统计；失败用例保持失败，不添加忽略失败参数或削弱断言来制造通过结果。测试清理代码即使在失败后执行，也不能掩盖管理器自身的资源泄漏。
- 配置兼容测试需验证旧类型构造器注入、新旧类型的对象身份、嵌套绑定、缺省值和用户覆盖条件；热刷新需验证容器 Bean、已有注入引用及核心缓存一起更新。
- 每次报告实际命令、执行数量、成功/失败/未运行状态。编译、模拟客户端和真实 MongoDB 验证分开报告；不得用局部通过掩盖全量失败。
- 测试工程本地安装可用于准备构件，不能把准备阶段跳过测试的成功当作测试通过；实际回归必须单独执行并保留报告。

## 提交代码
- 精确暂存修改文件。
- 执行 git diff --check。
- 检查 git diff --cached。
- 禁止 git add .、git add -A。
- 不 push。
- 不部署。
  Commit Message格式： `feat: XXXX`