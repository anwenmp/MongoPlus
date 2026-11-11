# 证据等级、实验与重跑

覆盖登记、源码可达、编译和真实运行分别报告。SOURCE_VERIFIED表示当前声明/实现已定位；COMPILE_PASS只说明指定依赖/JDK下可编译；LIVE_PASS需要MongoPlus实发命令、原生对照、完整结果及测试断言同时成立。UNVERIFIED表示运行契约未确认，NOT_RUN表示未执行该项，CONTRACT_UNRESOLVED用于有歧义且实验未闭合的参数/重载。

## 本Skill验证入口

所有验证程序和产物写入本Skill的validation目录；源码、原有测试、其他Skill只读。脚本为维护与验收工具，日常使用Skill不强制安装Python、JDK或MongoDB。

1. 用JDK17编译scripts/SourceInventory.java到validation内，再运行`SourceInventory <repo-root> <skill>/audit/source-inventory.json`。JDK Compiler Tree API对当前src/main/java重新解析，记录public类型、注解、签名、路径、行号和SHA-256，不依赖旧索引。
2. `python scripts/build_inventory.py`生成能力、注解和公开类型矩阵，缺少指定API即失败。先完成盘点，再编写注解和主题；build_annotations.py / complete_topics.py为本次维护生成器，包含审阅过的语义材料。
3. `python scripts/compile_sources.py --jdk <JDK> --repository <Maven本地库>`直接javac从当前源码构建模块，产物放唯一build目录，不运行install、不覆盖模块target。Boot3/4分别使用对应依赖图。注解声明用release8编译，Starter源码用其所需JDK。
4. `python scripts/validate_skill.py --jdk <JDK>`校验相对链接/锚点、注解与当前源码定义差异、属性默认值、每个能力的API来源、类型角色、示例编译和源码漂移。skill-creator的quick_validate.py另行执行，结果在coverage-report中，不把格式检查当行为验收。
5. 真实MongoDB夹具读取仓库原有测试配置或由调用者提供连接，不输出/复制凭据。只用本次唯一命名集合，记录MongoDB/JDK版本、命令和完整结果；清理仅限本次集合。真实运行的授权与资源边界沿用户任务，不从Skill推导任意生产写入权限。

## API实验记录要求

记录API的完整签名和import、适用用途、各参数角色（物理字段、实体属性、变量、输出别名、阶段体/完整阶段）、所需注册配置、实际MongoPlus命令、原生对照命令、完整结果/最终文档、重载差异、正确用法、异常和边界。聚合捕获父子全部pipeline；事务检查独立客户端可见性；字段链比较落库和回读值。

本次逐项结果与未执行项见[覆盖报告](../audit/coverage-report.md)。全部公开注解已经登记不表示全部配置、部署、容器和组合运行通过。检索/Agent行为评估要给独立评估者真实业务需求与Skill入口，不提供预期API答案；其选型和依据另外审阅。

本环境Python没有PyYAML且本任务禁止安装；原quick_validate.py的直接运行因缺依赖失败。`run_creator_validation.py --validator <原脚本> --jdk <JDK> --snakeyaml <现有JAR>`使用已有SnakeYAML安全解析器适配safe_load，再执行未修改的原校验函数。适配运行结果与原生PyYAML未运行状态分别保留，不用伪造YAML字典跳过解析。Java/SnakeYAML不是Skill日常导航依赖。
