# 全部公开注解目录

当前源码定义 **43种完整类名、44处声明**；两套Starter的MongoMapperScan同名。全部盘点，无遗漏、无按业务频率排除项。容器和`@Target({})`嵌套配置也列入，不能直接标类/字段的限制逐项记录。完整可核对矩阵见[注解覆盖审计](../audit/annotation-matrix.md)。

| 注解 | 完整import | 模块 | 记录 | 示例 |
|---|---|---|---|---|
| @CollectionField | `com.mongoplus.annotation.collection.CollectionField` | mongo-plus-annotation | [属性/注册/限制](mapping.md#collectionfield) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @CollectionLogic | `com.mongoplus.annotation.collection.CollectionLogic` | mongo-plus-annotation | [属性/注册/限制](mapping.md#collectionlogic) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @CollectionName | `com.mongoplus.annotation.collection.CollectionName` | mongo-plus-annotation | [属性/注册/限制](mapping.md#collectionname) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @DBRef | `com.mongoplus.annotation.collection.DBRef` | mongo-plus-annotation | [属性/注册/限制](mapping.md#dbref) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @Desensitization | `com.mongoplus.annotation.comm.Desensitization` | mongo-plus-annotation | [属性/注册/限制](field-processing.md#desensitization) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @EnumValue | `com.mongoplus.annotation.comm.EnumValue` | mongo-plus-annotation | [属性/注册/限制](mapping.md#enumvalue) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @ExtraFields | `com.mongoplus.annotation.collection.ExtraFields` | mongo-plus-annotation | [属性/注册/限制](mapping.md#extrafields) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @FieldBind | `com.mongoplus.annotation.collection.FieldBind` | mongo-plus-annotation | [属性/注册/限制](mapping.md#fieldbind) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @FieldEncrypt | `com.mongoplus.annotation.comm.FieldEncrypt` | mongo-plus-annotation | [属性/注册/限制](field-processing.md#fieldencrypt) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @ID | `com.mongoplus.annotation.ID` | mongo-plus-annotation | [属性/注册/限制](mapping.md#id) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @IgnoreLogic | `com.mongoplus.annotation.logice.IgnoreLogic` | mongo-plus-annotation | [属性/注册/限制](routing-isolation.md#ignorelogic) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @IgnoreTenant | `com.mongoplus.annotation.tenant.IgnoreTenant` | mongo-plus-annotation | [属性/注册/限制](routing-isolation.md#ignoretenant) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @Mongo | `com.mongoplus.annotation.mapper.Mongo` | mongo-plus-annotation | [属性/注册/限制](integration.md#mongo) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoCompoundIndex | `com.mongoplus.annotation.index.MongoCompoundIndex` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongocompoundindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoCompoundIndexes | `com.mongoplus.annotation.index.MongoCompoundIndexes` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongocompoundindexes) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoDs | `com.mongoplus.annotation.datasource.MongoDs` | mongo-plus-annotation | [属性/注册/限制](routing-isolation.md#mongods) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoGeoIndex | `com.mongoplus.annotation.index.MongoGeoIndex` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongogeoindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoHashIndex | `com.mongoplus.annotation.index.MongoHashIndex` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongohashindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoIndex | `com.mongoplus.annotation.index.MongoIndex` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongoindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoIndexDs | `com.mongoplus.annotation.index.MongoIndexDs` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongoindexds) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoMapperScan | `com.mongoplus.annotation.MongoMapperScan` | mongo-plus-boot-starter, mongo-plus-boot4-starter | [属性/注册/限制](integration.md#mongomapperscan) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoReadPreference | `com.mongoplus.annotation.transactional.MongoReadPreference` | mongo-plus-annotation | [属性/注册/限制](routing-isolation.md#mongoreadpreference) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchField | `com.mongoplus.annotation.index.search.MongoSearchField` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchfield) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchFields | `com.mongoplus.annotation.index.search.MongoSearchFields` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchfields) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchIndex | `com.mongoplus.annotation.index.search.MongoSearchIndex` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchIndexes | `com.mongoplus.annotation.index.search.MongoSearchIndexes` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchindexes) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchSort | `com.mongoplus.annotation.index.search.MongoSearchSort` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchsort) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchSorts | `com.mongoplus.annotation.index.search.MongoSearchSorts` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchsorts) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchStoredSource | `com.mongoplus.annotation.index.search.MongoSearchStoredSource` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchstoredsource) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoSearchSynonym | `com.mongoplus.annotation.index.search.MongoSearchSynonym` | mongo-plus-annotation | [属性/注册/限制](search.md#mongosearchsynonym) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoTextIndex | `com.mongoplus.annotation.index.MongoTextIndex` | mongo-plus-annotation | [属性/注册/限制](indexes.md#mongotextindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoTransactional | `com.mongoplus.annotation.transactional.MongoTransactional` | mongo-plus-annotation | [属性/注册/限制](routing-isolation.md#mongotransactional) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoVectorField | `com.mongoplus.annotation.index.vector.MongoVectorField` | mongo-plus-annotation | [属性/注册/限制](vector.md#mongovectorfield) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoVectorFields | `com.mongoplus.annotation.index.vector.MongoVectorFields` | mongo-plus-annotation | [属性/注册/限制](vector.md#mongovectorfields) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoVectorHnswOptions | `com.mongoplus.annotation.index.vector.MongoVectorHnswOptions` | mongo-plus-annotation | [属性/注册/限制](vector.md#mongovectorhnswoptions) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoVectorIndex | `com.mongoplus.annotation.index.vector.MongoVectorIndex` | mongo-plus-annotation | [属性/注册/限制](vector.md#mongovectorindex) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoVectorIndexes | `com.mongoplus.annotation.index.vector.MongoVectorIndexes` | mongo-plus-annotation | [属性/注册/限制](vector.md#mongovectorindexes) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @MongoVectorStoredSource | `com.mongoplus.annotation.index.vector.MongoVectorStoredSource` | mongo-plus-annotation | [属性/注册/限制](vector.md#mongovectorstoredsource) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @NonNull | `com.mongoplus.annotation.comm.NonNull` | mongo-plus-annotation | [属性/注册/限制](field-processing.md#nonnull) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @Nullable | `com.mongoplus.annotation.comm.Nullable` | mongo-plus-annotation | [属性/注册/限制](field-processing.md#nullable) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @SensitiveWord | `com.mongoplus.annotation.SensitiveWord` | mongo-plus-sensitive-word | [属性/注册/限制](field-processing.md#sensitiveword) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @TimeSeries | `com.mongoplus.annotation.collection.TimeSeries` | mongo-plus-annotation | [属性/注册/限制](mapping.md#timeseries) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |
| @Version | `com.mongoplus.annotation.collection.Version` | mongo-plus-annotation | [属性/注册/限制](mapping.md#version) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |

特别状态：`MongoSearchField.dimensions`已弃用；`MongoReadPreference`未确认独立方法注解的消费契约，确认消费位置为事务注解的preference；`NonNull/Nullable`是类型提示，不执行非空校验。
