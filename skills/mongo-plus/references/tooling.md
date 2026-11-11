# 仓库开发工具与索引边界

仓库内mongo-plus-indexer是独立Java17 POM，当前不在根8模块reactor内；BOM仍保留构件项。它提供MongoPlusIndexer、MongoPlusIndexerConfig及cli.MongoPlusIndexerMain/MongoPlusPipelineIndexerMain、SourceScanner、TypeClassifier等开发工具。

另有tools/mongoplus-api-indexer独立Maven工具（com.mongoplus.tools:mongoplus-api-indexer:0.1.0），公开CLI为com.mongoplus.tools.ApiIndexer，使用JavaParser/Jackson做声明索引，Java17构建。该工具也纳入源码/公开类型/编译盘点；不加入应用依赖，不以它生成的旧JSONL代替当前事实。

generated下的API/capability索引与tools/mongoplus-api-indexer可帮助定位，但生成时间、输入模块和当前源码不一定一致。本次重新解析646个生产Java文件，以当前声明和SHA-256建立可对账清单；没有复制完整JavaParser JSONL到Skill。

本Skill日常使用不依赖这些工具、不强制MCP或CodeGraph。目标项目提供源码图时可选用，缺失时用源码/JAR与编译器核对即可。工具公开类型仍纳入[类型矩阵](../audit/public-types.md)，开发工具方法不作为运行框架功能数量的替代。

<!-- CAPABILITY_ROWS -->
## 对应盘点项

- **C100 源码索引开发工具**：`MongoPlusIndexerMain`, `ApiIndexer`。

源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。
