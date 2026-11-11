"""Complete human-reviewed topic material and source-derived API-family navigation."""
import json,re
from pathlib import Path
S=Path(__file__).resolve().parents[1];R=S.parent
D=json.loads((S/'audit/source-inventory.json').read_text(encoding='utf8'));T=D['types']
CAP=json.loads((S/'audit/capabilities.json').read_text(encoding='utf8'))
P=json.loads((S/'audit/provenance.json').read_text(encoding='utf8'));HEAD=P['head']
def write(p,s):
    (S/p).parent.mkdir(parents=True,exist_ok=True);(S/p).write_text(s,encoding='utf8')
def append(p,section):
    original=(S/p).read_text(encoding='utf8');marker='\n<!-- COMPLETE_COVERAGE -->\n'
    original=original.split(marker)[0];write(p,original.rstrip()+marker+section.strip()+'\n')
ADDITIONS={
'crud':'''## 完整访问能力

| 需求 | 优先入口 | 语义边界 |
|---|---|---|
| save单条/批量 | MongoMapper.save/saveBatch | InsertOneOptions为正常单条入口；单条save(T,InsertManyOptions)已弃用；空批次先处理 |
| 存在则更新 | saveOrUpdate、saveOrUpdateWrapper、saveOrUpdateBatch系列 | boolean isQueryDatabase重载与默认版本分别核对；不是唯一键原子upsert保证 |
| 实体修改 | updateById/updateByColumn/updateBatchByIds | entity转换、fill与版本插件路径；不是整文档替换 |
| 条件更新 | BaseMapper.updateOne/update，绑定lambdaUpdate | 单条/多条、Options、modifiedCount布尔包装要区分 |
| 删除 | removeOne/remove、按ID/列/batch | 逻辑删除拦截器可能改为update；物理删除不靠方法名猜测 |
| 读取 | one/list/getById/getByIds/getByColumn/exist | one无值为null；不是多值唯一性验证 |
| 分页与计数 | page/pageList/count/recentPageCount | page含总数，pageList仅列表；空条件可能走估算计数；分页稳定排序 |
| 原子删除并返回旧值 | findOneAndRemove | Options、投影与排序选中的记录按重载核对，不能按普通remove布尔结果处理 |
| 原始Document | BaseMapper显式database/collection重载 | 无实体namespace；必要时UnClassCollection标记元数据，隔离规则需显式配置 |
| 原生命令 | SuperMapper.command、queryCommand | queryCommand的结果类型及返回形状按签名；DDL命令不等于普通CRUD |
| 批量WriteModel | bulkWrite | Driver Insert/Update/Delete/Replace model不同；实体转换与拦截器模型回写不能默认等价 |
| distinct / replaceOne | 聚合group或[Driver互操作](driver-interop.md) | 当前没有专用MongoPlus方法，不向BaseMapper编造方法 |

所有实体Mapper、Repository和Service最终委托非泛型BaseMapper；MongoMapper不继承BaseMapper。Document/Map/DTO结果与实体集合类型分开选择，嵌套泛型用TypeReference，不能以Class<List>保留元素类型。精确重载在[访问API](api/data-access.md)，原始BSON会绕过哪些转换见[映射](mapping.md)。
''',
'query':'''## 条件族全景

当前实体Lambda查询使用`QueryWrapper<T>.lambdaQuery()`或`ChainWrappers.lambdaQueryChain`/Repository.lambdaQuery等真实入口；源码没有名为LambdaQueryWrapper的类。构造器与链均带可变状态，终结执行与构造条件是两步。

| 条件族 | 公开API/操作 | 核对点 |
|---|---|---|
| 比较 | eq/ne/gt/gte/lt/lte/in/nin | boolean condition=false不加入；null、空in、重复列需实际BSON验证 |
| 存在与类型 | exists/type | 缺字段与显式null不是同一场景；TypeEnum按BSON类型 |
| 逻辑 | and/or/nor/not | 同字段多范围与复合分支层级分开检查 |
| 数组 | all/size/elemMatch | elemMatch约束同一元素；嵌套数组逐层进入 |
| 字符串与表达式 | regex/like/mod/expr/jsonSchema/where | 正则选项、字段表达式、服务端禁用脚本等限制独立核对 |
| 位运算 | bitsAllClear/bitsAllSet/bitsAnyClear/bitsAnySet | 位位置、掩码及字段BSON类型按重载 |
| 全文 | text与TextSearchOptions | 传统text索引；Search另见专题 |
| 地理 | near/nearSphere/geoWithin/geoIntersects及box/center/polygon变体 | 对应2d/2dsphere索引、坐标系、单位和排序限制 |
| 投影 | projectDisplay/projectNone及Projection | 包含/排除通常不能随意混用；_id另查 |
| 排序/分页 | orderByAsc/orderByDesc、page/pageList | 明确排序键及页起点；复用会累积状态 |
| 自定义BSON | Wrapper.custom、Filters及addBasicDBObject等真实入口 | 物理字段路径、表达式与注解转换覆盖分别验证 |
| 类型安全嵌套 | FieldChain/FreeFieldChain、SFunction | build/buildOption的字段前缀不同，不把查询路径和表达式引用混用 |

公开方法族及import在[查询API](api/query.md)；地理模型也列入，不因业务使用少而排除。原始BSON不保证所有ConditionHandler元数据都存在。
''',
'update':'''## 更新操作与执行保障

| 业务意图 | MongoPlus操作 | 特别边界 |
|---|---|---|
| 设置/清空/移除/更名 | set、unset、rename | set null和unset不同；实体ignoreNull会影响实体更新 |
| 当前日期 | currentDate | Date/Timestamp按CurrentDateType和重载检查 |
| 原子数值 | inc/mul/min/max | 上下限写在同一次filter里；$min/$max不是Java先读后算 |
| 数组追加/去重 | push/addToSet，ConditionOperators.each系列、PushOptions | each/position/slice/sort值体形状不同；两个PushOptions包名有兼容入口 |
| 数组弹出/删除 | pop/pull/pullAll | pull值与条件wrapper不互推；pullAll是多个精确元素而非任意谓词 |
| 自定义更新体 | updateCustom | 明确更新运算符文档，不能把pipeline更新数组冒充普通update文档 |
| 乐观锁 | @Version + OptimisticLockerInterceptor | 实体版本读取、空版本异常、失败异常、autoInc、enableRetry逐项配置 |
| 重试结果 | Retry/UpdateRetryResult | 自动重试不能掩盖用户旧快照冲突，也不替代业务幂等 |
| 防空条件 | BlockAttackInnerListener、block-attack-inner | Driver命令监听边界；不等于细粒度授权；启用后实测空filter update/delete |

乐观锁从实体元数据和更新内容识别版本，Wrapper-only、bulk原始model、直接Driver分别验证。空条件保护默认关闭；配置入口见[配置](configuration.md)，不要在Skill里悄悄给所有应用开启插件。精确重载在[更新API](api/update.md)。

真实库已验证多条件pull的两种写法。优先使用`pull(String,Object)`把目标数组路径与元素条件体分开；`pull(Wrapper<?>)`需要整个`{items:元素条件体}`形状，可用custom保留结构。把整个元素条件放进`eq("items",...)`会生成`items:{$eq:...}`，含$gte的文档将被当作相等值，不能实现下例谓词。

```java
import com.mongoplus.conditions.query.QueryWrapper;
import com.mongoplus.conditions.update.UpdateWrapper;
import org.bson.Document;

class RemoveQualifiedItems {
    static UpdateWrapper<Object> byField() {
        Document element = new Document("sku","X").append("qty",new Document("$gte",2));
        return new UpdateWrapper<Object>().eq("_id",2).pull("items",element);
    }
    static UpdateWrapper<Object> byWrapper() {
        Document element = new Document("sku","X").append("qty",new Document("$gte",2));
        QueryWrapper<Object> body = new QueryWrapper<Object>().custom(new Document("items",element));
        return new UpdateWrapper<Object>().eq("_id",2).pull(body);
    }
}
```

两者实发`u:{$pull:{items:{sku:"X",qty:{$gte:2}}}}`；原生对照updateOne采用同一$pull体。夹具中的qty=3元素被移除，其他文档/元素保留；完整命令与结果见[实测清单](../audit/runtime-results.md)。此结论仅覆盖这两个具体重载。
''',
'aggregation':'''## 所有主要阶段与BSON重载边界

| 阶段族 | MongoPlus入口 | 参数/限制 |
|---|---|---|
| 匹配、投影、排序、分页 | match/project/sort/sortSpecification/skip/limit | match/project的Bson为参数体；sort(Bson)为完整阶段，sortSpecification(Bson)为排序体 |
| 添加/修改/移除字段 | addFields/set/unset | addFields/set的Bson为参数体；unset(Bson)为完整阶段 |
| 分组、分桶、计数 | group/bucket/bucketAuto/sortByCount/count | group(Bson)完整阶段；bucket(Bson)、bucketAuto(Bson)为参数体；输出字段名和输入表达式区分 |
| 等值/管道关联 | lookup与let Variable | from物理集合或实体类转换；Driver Variable与同名MongoPlus类不随意互换 |
| 多分支和并集 | facet/unionWith | 子管道各自验证；外层tenant/logic过滤不自动递归 |
| 图遍历/展开 | graphLookup/unwind | connect字段、递归深度、preserveNullAndEmptyArrays和includeArrayIndex核对 |
| 替换文档/采样 | replaceRoot/replaceWith/sample | 字段引用与literal Document区别；sample不保证顺序 |
| 写出/合并 | out/merge | 标记isSkip；影响终结返回/取结果；会写库，需目标权限与独立验证 |
| 窗口 | setWindowFields | partitionBy、sortBy、WindowOutputField等Driver值类型，输出形状独立核对 |
| 补时间点/缺值 | densify/fill | range、bounds、partition及locf/linear要求，服务端支持需核对 |
| 任意完整阶段 | custom(Bson) | 原样追加，不自动包阶段名；可接Search/Vector |
| 执行选项 | allowDiskUse/batchSize/collation/maxTimeMS/maxAwaitTimeMS/bypassDocumentValidation/comment/hint/let | options不是pipeline阶段；全部实际发出值需CommandStartedEvent核对 |

`addFields/set/bucket/bucketAuto/match/project(Bson)`在当前实现包上对应操作符；`sort/lookup/facet/graphLookup/group/unionWith/unwind/out/merge/replaceRoot/replaceWith/sample/setWindowFields/densify/fill/unset/custom(Bson)`直接追加完整阶段。不要给前一组传`{$match:...}`等完整阶段，也不要给后一组只传内部参数。每个String、lambda、Bson、Collection重载单独看实现，不能用同名API推断包装规则。

所有阶段签名在[聚合API](api/aggregation.md)，表达式和所有累加器见[表达式专题](expressions.md)。验证以真实发出的完整pipeline和原生aggregate结果为准；仅构造Bson的检查不算运行通过。

**Java重载解析也影响含义**：`replaceRoot(Document)`及`replaceWith(Document)`选择更具体的Document表达式重载，自动包装成阶段。传完整阶段时显式将参数声明/转换为Bson，例如`replaceRoot((Bson) fullStage)`。本次分别实测了显式Bson完整阶段和普通Document表达式，两条路径均与相应原生pipeline一致；首轮把完整Document误传给Document表达式重载产生双层$replaceRoot/$replaceWith，记录保留在[运行审计](../audit/runtime-results.md)，不是据此认定框架缺陷。
''',
'mapping':'''## 完整映射能力与注册

| 场景 | 入口 | 条件与边界 |
|---|---|---|
| 元数据 | TypeInformation/FieldInformation | 继承字段、泛型、集合元素、缓存、反射可达性按当前版本 |
| 自定义主键 | IdGenerateHandler、IdentifierGenerator、IdWorker | ID策略、AUTO自增集合、UUID/ULID/雪花及ObjectId按字段类型选择 |
| 集合命名 | CollectionName、AnnotationOperate、CollectionNameHandler | 静态实体命名与动态物理路由不同 |
| 枚举存值 | EnumValue、EnumMappingStrategy/EnumConversionStrategy | 未知值、null和BSON数值宽度要有测试 |
| 单字段转换 | CollectionField.typeHandler、TypeHandler | 不把其他ORM接口导入本框架 |
| 写入全局映射 | MappingStrategy、MappingCache | 类型泛型注册；实体嵌套与Map值分开核对 |
| 读取全局转换 | ConversionStrategy、ConversionCache | Date/JSR310、BSON、数值、GeoJSON等已有默认策略 |
| 整体Converter | MongoConverter、MappingMongoConverter | writeBySave/writeByUpdate/readDocument与readInternal各入口区分 |
| 泛型结果 | TypeReference<T> | 嵌套List/Map使用真实Type，不用Class丢失泛型 |
| 扩展字段 | ExtraFields、ExtraFieldsHolder | 额外Map类型、忽略列表及命名转换 |
| 字典回写 | FieldBind、DataBindHandler、FieldBindHandler | 显式注册ReadHandler；目标字段必须存在 |
| 引用对象 | DBRef、DBRefHandler | 不承诺级联保存或无额外查询；缺引用/循环和数组引用未验证 |
| 字段安全 | FieldEncrypt、Desensitization、SensitiveWord | 读取脱敏与写入加密不同，分别路由专题 |

当前写链会把前序结果传给双参数FieldHandler，见[扩展生命周期](extensions.md)。默认DBRef/类型处理/加密的顺序和读链的activate/discontinue需要组合验证。Document/Map、实体save/update、wrapper-only和bulk不是一条相同路径。

完整映射注解见[注解页](../annotations/mapping.md)，映射/转换类、注册方法与泛型签名见[映射API](api/mapping.md)。

FieldBind读取回调的精确方法为`DataBindHandler.setMetaObject(FieldBind binding,Object source,FieldInformation target)`：binding是源字段上的注解，source是源字段当前读值，第三参数是通过target属性找到的**目标字段元数据**，应对它调用setValue写入翻译。显式注册示例（配合注解页中target="label"实体）：

```java
import com.mongoplus.cache.global.HandlerCache;
import com.mongoplus.handlers.read.FieldBindHandler;

class BindStatusNames {
    static void register() {
        HandlerCache.addReadHandler(new FieldBindHandler((binding,source,target) -> {
            if ("status".equals(binding.type())) target.setValue("Name-"+source);
        }));
    }
}
```

Boot4/Solon按应用启动生命周期显式调用一次，Boot3可收集ReadHandler Bean。HandlerCache有单个ReadHandler和List<ReadHandler>两个addReadHandler重载，返回void，精确static声明在[扩展API](api/extensions.md)。
''',
'interceptors':'''## 注册、顺序与生命周期

普通链以Interceptor.order升序排序。beforeExecute接收执行枚举、参数数组与原集合；对应MethodExecutorStrategy调用executeQuery/executeUpdate等策略，再由Execute调用Driver；成功路径调用afterExecute。索引方法的执行枚举可能为null，不能在插件中无条件switch。批写model必须真正重建/回写到Driver参数，修改临时Pair不等于生效。

高级链通过AdvancedInterceptor.intercept(Invocation)环绕调用；order排序与包装方向共同决定进入/退出顺序。正常路径proceed一次，短路要提供与方法返回类型相容的值。LogicRemove和OptimisticLocker位于靠后order，不根据Bean声明顺序猜最终调用次序。异常是否走after、ThreadLocal是否清理及返回值传播要分别检验。

| 需求 | 选用接口 | 检查边界 |
|---|---|---|
| 改写filter/projection/update/collection | Interceptor | query/count/remove/removeOne/updateOne/updateMany/bulk/aggregate/index覆盖 |
| 环绕、短路、替换操作类型 | AdvancedInterceptor | Invocation.proceed/discontinue、session、返回值和异常 |
| 字段值转换 | FieldHandler/ReadHandler | 不绕到执行拦截器重复实现字段生命周期 |
| 命令观察 | Listener | 已是Driver命令层，不将日志回调当事务提交事件 |

注册使用Configuration.interceptor/advancedInterceptor或对应容器Bean；链是静态的，反复初始化可能追加。动态集合拦截器改参数数组最后的collection，较早插件持有的旧引用不会自动更新。完整签名见[扩展API](api/extensions.md)，跨容器接线见[集成](integration.md)。
''',
'isolation':'''## 注解与恢复边界

完整声明见[路由隔离注解](../annotations/routing-isolation.md)：IgnoreTenant仅METHOD，IgnoreLogic为METHOD/TYPE，CollectionLogic为字段/注解类型。TenantHandler需注册TenantInterceptor；逻辑删除需LogicProperty.open与实体元数据及对应拦截器，不能只加注解。

TenantManager提供withoutTenant、ignoreTenantCondition、restoreTenantCondition；LogicManager提供withoutLogic及对应忽略/恢复入口；恢复操作还可用LogicRemove/LogicDeleteHandler公开API，按实体或BSON重载核对。手动上下文应在finally恢复，嵌套和同线程复用须实际验证。

聚合顶层过滤不自动约束lookup/facet/unionWith子管道。Search/Vector等有首阶段要求时，插件插入match的位置必须检查。原始Driver、无实体Document、estimatedDocumentCount、各bulk model和逻辑删除改写后的update路径不能从一次查询通过外推隔离已覆盖。类型和签名见[管理API](api/management.md)。
''',
'transactions-routing':'''## 完整数据源与事务选型

| 需求 | API/配置 | 验证边界 |
|---|---|---|
| 声明命名数据源 | BaseProperty/SlaveDataSource、Configuration.dataSource | 默认master；客户端、database与collection管理一起注册 |
| 运行时新增或切换 | DataSourceManager.appendTempDataSource/changeDataSource/clearDataSource | 替换旧client、缓存、嵌套恢复和线程池复用 |
| 注解路由 | MongoDs与DataSourceHandler | 先注册数据源；Boot代理/类级方法级；Solon绑定另核对 |
| 动态集合 | CollectionNameHandler/DynamicCollectionNameInterceptor | 不递归改变外表、不自动创建每个分区索引 |
| 编程式事务 | MongoTransactionalOperate、MongoTransactionalManager | start/commit/rollback/close生命周期，异常后清理 |
| Spring传播 | MongoPlusTransactionalManager + @Transactional | REQUIRED/REQUIRES_NEW/NOT_SUPPORTED等按真实代理测试；不推定支持savepoint |
| MongoPlus声明事务 | MongoTransactional | rollbackFor/noRollbackFor、causallyConsistent/snapshot及事务options |
| 读偏好 | MongoTransactional.preference中的MongoReadPreference | 当前只消费第一项；独立方法标注未确认，不当作路由读副本的通用切面 |
| 数据源分片 | [分片专题](sharding.md) | 多资源事务不等于跨client原子事务 |

事务上下文与session绑定执行器选择：ExecutorFactory在事务中选择SessionExecute，非事务选择DefaultExecute。真实验证至少比较独立客户端在提交前不可见、提交后可见、回滚后不存在，检查同线程后续调用和session关闭。传播测试和容器绑定没有运行时标NOT_RUN，不能由编译或直接Driver事务测试代替。

Core编程式优先使用已管理事务上下文的MongoTransactionalManager。旧MongoTransactionalOperate创建/启动ClientSession，但这些方法自身没有把session装入MongoTransactionContext；其startTransaction(String dataSourceName)当前未消费该参数。不能仅调用它后就声称后续Mapper已加入指定数据源事务。这部分组合契约保留UNVERIFIED，不推荐靠参数名推断。

所有事务/路由注解属性见[完整记录](../annotations/routing-isolation.md)，方法重载见[管理API](api/management.md)。
''',
'indexes':'''## 注解清单与完整管理边界

普通索引7种注解（含复合容器）均在[完整记录](../annotations/indexes.md)：MongoIndex、MongoCompoundIndex、MongoCompoundIndexes、MongoGeoIndex、MongoHashIndex、MongoTextIndex、MongoIndexDs。Search的8种及Vector的6种另有[专题](search-vector.md)和注解页。

自动扫描由auto-scan-packages等配置决定，auto-create-index默认false。Resolver将实体注解解析为IndexMetaObject；AutoUtil先解析全部实体再执行DDL。普通createIndexes与Search管理调用分开，远端失败不会回滚先前创建的索引。不要承诺自动扫描可以修复所有已有同名冲突。

| 索引类型/能力 | 核验方法 |
|---|---|
| 唯一/稀疏/partial | listIndexes核对定义，用冲突和不匹配partial条件的数据验证约束 |
| TTL | BSON Date、expireAfterSeconds/表达式转换、后台清理延迟；不能只看到索引即声明已过期删除 |
| 复合 | 键顺序、方向、字段真实物理名、partial/unique |
| 哈希 | hashed值而非普通1/-1；范围/唯一等服务端约束另核对 |
| 地理 | 2d/2dsphere与坐标/几何值、地理查询 |
| text | fields、language、版本和$text行为；区别Search |
| 手动管理 | BaseIndex/Index/SuperIndex与Repository createIndex(s)/listIndexes/dropIndex(es)，重载显式指定namespace或实体 |
| 多数据源DDL | MongoIndexDs只定索引目标，不改变一般数据访问路由 |

索引状态、定义冲突、自动初始化幂等及Search异步就绪见[Search/Vector](search-vector.md)，时序集合见[时序](timeseries.md)，精确签名见[管理API](api/management.md)。
''',
'events':'''## 框架自带记录与异步镜像

DataChangeRecorderInnerInterceptor在框架操作前后记录变更，支持setDatasourceName、setDatabaseName、setCollectionName、enableSaveDatabase、setDisplayCompleteData、setIgnoredColumnList、setBatchUpdateLimit等配置。核对字段过滤和批量更新上限，避免把完整文档审计默认当作合适的生产设置；成功回调不等于事务最终提交。它不覆盖外部客户端写入。

异步多写用Configuration.asyncMultipleWrite(MultipleWriteHandler)，或注册AsyncMultipleWriteInterceptor。MultipleWriteHandler构造时按操作维护目标数据源列表，addMultipleWriteStrategy可以改映射；multipleWrite(currentDataSource,namespace)可返回定制列表，getMultipleWrite选择最终目标。默认策略涉及已配置数据源，初始化前必须审阅目标范围。

该拦截器安排异步镜像后继续主调用；不提供外部消息持久位点、跨库原子提交或MySQL sink。验证需覆盖主写失败、事务回滚、镜像部分失败、重放、进程退出、Executor与client关闭。未做这些故障测试时标UNVERIFIED，不把一次主库成功写入当作镜像一致性通过。完整公开签名见[扩展API](api/extensions.md)。
'''
}
for topic,text in ADDITIONS.items():append(f'references/{topic}.md',text)

# Normalize provenance, preserving examples. Current HEAD alone cannot prove dirty source identity;
# source-inventory file hashes are the authoritative snapshot.
for p in (S/'references').glob('*.md'):
    text=p.read_text(encoding='utf8')
    text=text.replace('本页和其余参考示例基于 2.2.0 源码快照 f11e4b92。','本页以本次盘点的 2.2.0 源码为基线，精确文件哈希见audit/source-inventory.json。')
    text=text.replace('Boot 2/3 的入口','当前Boot 3 的入口').replace('https://mongoplus.com/docs/quickStart.html','https://mongoplus.com/docs/started.html')
    text=re.sub(r'https://github.com/anwenmp/MongoPlus/blob/[0-9a-f]{8,40}/',f'https://github.com/anwenmp/MongoPlus/blob/{HEAD}/',text)
    write(str(p.relative_to(S)),text)

write('references/configuration.md','''# 配置、连接池、TLS与配置加密

Boot3/4的配置绑定在MongoPlusPropertyConfiguration；Core使用Configuration及BaseProperty，Solon由插件接线。配置字段的存在不等于所有容器都消费；默认值、URI优先级与运行状态分开核对。

| 前缀 | 绑定类型 | 用途 |
|---|---|---|
| mongo-plus.data.mongodb | MongoDBConnectProperty extends BaseProperty | 连接与slaveDataSource列表 |
| mongo-plus.configuration | MongoDBConfigurationProperty | 扫描、索引、时序、ID等 |
| mongo-plus.configuration.collection | MongoDBCollectionProperty | 集合命名、防空条件监听 |
| mongo-plus.configuration.field | MongoDBFieldProperty | 驼峰转换、null |
| mongo-plus.configuration.logic | MongoLogicDelProperty extends LogicProperty | 逻辑删除 |
| mongo-plus | MongoDBLogProperty | log/pretty/logOrder/format兼容配置 |
| mongo-plus.encryptor | MongoEncryptorProperty | 字段加密默认密钥 |
| mongo-plus.spring | MongoSpringProperty | transaction / overrideMongoClient |

`UrlJoint.jointMongoUrl()`遇到非空url直接返回，独立host/port/TLS/池字段不会再被拼接；不要同时设置两套后假定覆盖顺序。推荐在已有URI上明确所需Driver选项并核验MongoClientSettings。各BaseProperty字段以下逐一列出，不能把Driver所有URI能力都说成框架有独立配置。

TLS涉及tls/ssl、tlsInsecure、tlsAllowInvalidHostnames、clientKeyStore/jks/keyPassword等；实际证书加载与MongoClient创建在ConnectMongoDB，真实握手未执行就标NOT_RUN。读偏好、认证、压缩、retryWrites/Reads、uuidRepresentation、超时、连接池均需Driver和部署匹配。retryWrites不是业务层重试或乐观锁冲突重试。

环境配置加密由Starter EncryptionPostProcessor提供：读取mpw.key，检测`${ENC:密文}`，AES解密包装OriginTrackedMapPropertySource。它与@FieldEncrypt独立；不是任意PropertySource都被替换，也不自动隐藏日志中已经输出的值。密钥不写进参考文档。

已声明但消费受限：lazyDataSource未确认实际惰性连接消费者；SensitiveWordConfigurationProperty.ignoreChar当前未控制specialChars忽略器。这两项标UNVERIFIED，不提供假定生效的示例。

## 当前配置字段与默认值

以下由本次源码字段声明提取；`null（未初始化）`不是推荐生产值。
''')
for name in ['BaseProperty','MongoDBConnectProperty','MongoDBConfigurationProperty','MongoDBCollectionProperty','MongoDBFieldProperty','MongoLogicDelProperty','LogicProperty','MongoEncryptorProperty','MongoSpringProperty','MongoDBLogProperty','SensitiveWordConfigurationProperty']:
    t=next(t for t in T if t['simpleName']==name)
    raw=(R/t['path']).read_text(encoding='utf8')
    rows=re.findall(r'^\s*private\s+(?!static)([\w<>?, .]+?)\s+(\w+)\s*(?:=\s*([^;]+))?;',raw,re.M)
    with (S/'references/configuration.md').open('a',encoding='utf8') as f:
        f.write(f'\n### {name}\n\n`import {t["name"]};`；[源码](https://github.com/anwenmp/MongoPlus/blob/{HEAD}/{t["path"]})。\n\n| 字段（配置使用kebab-case） | Java类型 | 默认声明 |\n|---|---|---|\n')
        for typ,n,val in rows:f.write(f'| `{n}` | `{typ}` | `{val or "null（未初始化）"}` |\n')
        if not rows:f.write('| 继承字段 | 见父配置类 | 见父配置类 |\n')

write('references/utilities.md','''# 公开辅助类型、默认实现和异常

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
''')
write('references/tooling.md','''# 仓库开发工具与索引边界

仓库内mongo-plus-indexer是独立Java17 POM，当前不在根8模块reactor内；BOM仍保留构件项。它提供MongoPlusIndexer、MongoPlusIndexerConfig及cli.MongoPlusIndexerMain/MongoPlusPipelineIndexerMain、SourceScanner、TypeClassifier等开发工具。

另有tools/mongoplus-api-indexer独立Maven工具（com.mongoplus.tools:mongoplus-api-indexer:0.1.0），公开CLI为com.mongoplus.tools.ApiIndexer，使用JavaParser/Jackson做声明索引，Java17构建。该工具也纳入源码/公开类型/编译盘点；不加入应用依赖，不以它生成的旧JSONL代替当前事实。

generated下的API/capability索引与tools/mongoplus-api-indexer可帮助定位，但生成时间、输入模块和当前源码不一定一致。本次重新解析646个生产Java文件，以当前声明和SHA-256建立可对账清单；没有复制完整JavaParser JSONL到Skill。

本Skill日常使用不依赖这些工具、不强制MCP或CodeGraph。目标项目提供源码图时可选用，缺失时用源码/JAR与编译器核对即可。工具公开类型仍纳入[类型矩阵](../audit/public-types.md)，开发工具方法不作为运行框架功能数量的替代。
''')

FAMILIES={
'data-access':lambda t:t['name'].split('.')[2] in ['mapper','repository','service'],
'query':lambda t:'.conditions.interfaces.query.' in t['name'] or t['simpleName'] in ['QueryWrapper','QueryChainWrapper','LambdaQueryChainWrapper','Wrapper','ChainWrappers','FieldChain','FreeFieldChain','SFunction','Filters','TextSearchOptions'],
'update':lambda t:'.conditions.interfaces.update.' in t['name'] or t['simpleName'] in ['UpdateWrapper','UpdateChainWrapper','LambdaUpdateChainWrapper','PushOptions','OptimisticLockerInterceptor','Retry','UpdateRetryResult'],
'aggregation':lambda t:'.aggregate.' in t['name'] and '.pipeline.' not in t['name'],
'expressions':lambda t:'.aggregate.pipeline.' in t['name'] or t['simpleName']=='ConditionOperators',
'mapping':lambda t:'.mapping.' in t['name'] or '.strategy.mapping.' in t['name'] or '.strategy.conversion.' in t['name'] or t['simpleName'] in ['TypeHandler','MetaObjectHandler','AutoFillMetaObject','IdGenerateHandler','IdentifierGenerator','IdWorker','ExtraFieldsHolder','DataBindHandler','FieldBindHandler','DBRefHandler'],
'extensions':lambda t:'.interceptor.' in t['name'] or '.listener.' in t['name'] or '.handlers.' in t['name'] or '.codecs.' in t['name'] or '.aware.' in t['name'] or t['simpleName'] in ['HandlerCache','MappingCache','ConversionCache','ComponentRegistry'],
'management':lambda t:'.manager.' in t['name'] or '.transactional.' in t['name'] or '.index.' in t['name'] or t['simpleName'] in ['Configuration','MongoClientFactory','LogicDeleteHandler','LogicRemove','GridFSBucketManager','BaseProperty','AutoUtil'],
'optional-modules':lambda t:t['module'] in ['mongo-plus-sensitive-word','mongo-plus-sharding','mongo-plus-sharding-boot-starter'],
'utilities':lambda t:'.bson.' in t['name'] or '.model.' in t['name'] or t['module']=='mongo-plus-annotation' and t['kind']=='ENUM'
}
for family,pred in FAMILIES.items():
    chosen=[t for t in T if pred(t) and t['kind']!='ANNOTATION_TYPE']
    lines=[f'# 当前公开API签名：{family}\n\n这些是本次AST提取的声明签名，用于import、泛型和重载选择；不是各方法已经运行的证明。不含方法体，不替代[业务专题](../verification.md)中的真实命令验证。\n']
    for t in chosen:
        lines.append(f'\n## {t["name"]} · {t["module"]}\n\n`import {t["name"]};`\n\n[声明源码](https://github.com/anwenmp/MongoPlus/blob/{HEAD}/{t["path"]}#L{t["line"]})。类型：{t["kind"]}；类型参数 `{", ".join(t.get("typeParameters",[]))}`；extends `{t["extends"]}`；implements `{", ".join(t["implements"])}`。\n\n参数类型的原声明import（含Driver值类型，通配符按对应包解析）：\n\n```text\n'+''.join(t.get('imports',[]))+'```\n\n公开声明签名（interface成员隐含public；static/default保留）：\n\n```text\n')
        for m in t['methods']:
            generic='<'+', '.join(m['typeParameters'])+'> ' if m['typeParameters'] else ''
            name=t['simpleName'] if m['name']=='<init>' else m['name']
            ret='' if m['return']=='null' else m['return']+' '
            annotation=' '.join(m['annotations'])+' ' if m['annotations'] else ''
            modifiers=' '.join(m.get('modifiers',[]))+' ' if m.get('modifiers') else ''
            lines.append(re.sub(r'\s+',' ',annotation+modifiers+generic+ret+name+'('+', '.join(m['parameters'])+');')+'\n')
        if not t['methods']:lines.append('(无直接声明的公开方法；检查继承接口)\n')
        if t['kind']=='ENUM':lines.append('枚举项: '+', '.join(f['name'] for f in t['fields'] if f['type']==t['simpleName'])+'\n')
        lines.append('```\n')
    write(f'references/api/{family}.md',''.join(lines))
write('references/api/index.md','# 公开API分族签名导航\n\n按主题读取一个签名文件，核对真实import、类型参数与重载。这里没有完整源码体或旧JSONL索引。覆盖业务决策以[能力目录](../../capabilities.md)为准，全部public类型与明确排除理由见[类型矩阵](../../audit/public-types.md)。\n\n'+''.join(f'- [{x}]({x}.md)\n' for x in FAMILIES))
for topic in sorted({c['topic'] for c in CAP}):
    path=f'references/{topic}.md';text=(S/path).read_text(encoding='utf8');marker='\n<!-- CAPABILITY_ROWS -->\n';text=text.split(marker)[0]
    text+='\n<!-- CAPABILITY_ROWS -->\n## 对应盘点项\n\n'
    for c in CAP:
        if c['topic']==topic:text+=f'- **{c["id"]} {c["name"]}**：`'+ '`, `'.join(c['apis'])+'`。\n'
    text+='\n源码位置和注册/限制见[能力矩阵](../capabilities.md)；精确重载见[分族签名](api/index.md)；未逐一运行的重载保持UNVERIFIED。\n'
    write(path,text)
print('Completed',len({c['topic'] for c in CAP}),'topics and',len(FAMILIES),'API families')
