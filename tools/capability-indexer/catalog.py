"""Human-reviewed capability inventory. Selectors resolve ONLY against existing JSONL.

No Java parsing, API generation, network access, model calls, or support-level inference.
Names after # select an existing declaration family; generated references retain each real ID.
"""

CATEGORIES = {
    '数据读写': ['实体访问', '条件查询', '分页与结果', '更新与批量', '原生访问'],
    '聚合计算': ['管道编排', '关联与分支', '表达式', '编码边界'],
    '实体与字段映射': ['命名与标识', '类型转换', '字段增强', '关联映射'],
    '安全隔离与一致性': ['租户与删除', '并发与事务', '字段保护', '操作保护'],
    '扩展与事件': ['执行扩展', '命令事件', '字段扩展', '生命周期'],
    '连接路由与集成': ['连接配置', '数据源路由', '集合路由', '容器接入', '开发工具'],
    '集合运维': ['索引管理', '时序集合', '备份与镜像', '变动记录'],
}

MODULES = {'boot': 'mongo-plus-boot-starter', 'boot4': 'mongo-plus-boot4-starter',
           'solon': 'mongo-plus-solon-plugin', 'sharding': 'mongo-plus-sharding',
           'shardingboot': 'mongo-plus-sharding-boot-starter', 'word': 'mongo-plus-sensitive-word',
           'indexer': 'mongo-plus-indexer'}

CATALOG = []

def e(symbol, role):
    return {'selector': symbol, 'role': role}

def t(file, method, verifies):
    return {'path': 'mongo-plus-core/src/test/java/com/mongoplus/' + file + '.java',
            'method': method, 'verifies': verifies}

def c(id, name, category, description, intents, keywords, api, source, docs, registration,
      limitations, status='documented', exposure='public_api', tests=(), questions=(),
      doc_note='', negative=(), related=(), retrieval=None, manual_review=()):
    assert category[1] in CATEGORIES[category[0]]
    CATALOG.append(dict(id='mp.'+id, name=name, category={'primary': category[0], 'secondary': category[1]},
                        description=description, userIntents=intents, keywords=keywords.split('|'),
                        apiSelectors=api, sourceSelectors=source, docPages=docs,
                        registration=registration, limitations=limitations,
                        evidenceStatus=status, exposure=exposure, tests=list(tests),
                        openQuestions=list(questions), documentationNote=doc_note,
                        negativeIntents=list(negative), relatedCapabilities=['mp.'+x for x in related]))
    if retrieval:
        CATALOG[-1]['retrievalPolicy'] = retrieval
    if manual_review:
        CATALOG[-1]['manualReview'] = dict(status='REQUIRED', reasons=list(manual_review))

BOOT = 'boot:config.MongoPlusAutoConfiguration'

c('entity-crud', '实体 CRUD 与业务层入口', ('数据读写','实体访问'),
  '实体 Mapper、Repository、Service 最终委托 BaseMapper 完成保存、读取、更新和删除；ServiceImpl 本身不新增事务语义。',
  ['用实体对象完成增删改查','给业务服务提供通用持久层'], 'CRUD|增删改查|保存实体|Mapper|Repository|Service',
  ['mapper.MongoMapper','repository.IRepository','service.IService'],
  [e('mapper.MongoMapperImpl#save','实体 Mapper 委托 BaseMapper'),e('mapper.AbstractBaseMapper#save','转换实体、执行写入、回写 ID'),e('service.impl.ServiceImpl','业务层继承入口')],
  ['crud','mapper'], '扫描继承 MongoMapper<T> 的接口，或注册 RepositoryImpl/ServiceImpl 子类；扫描接线见容器接入。',
  ['返回 Boolean 是框架对 Driver 结果的封装，不能代表所有业务语义。','实体 saveBatch 不接受空集合；DefaultBaseMapperImpl 直接读取首元素。'])

c('conditional-query', '条件查询、排序与投影', ('数据读写','条件查询'),
  'QueryWrapper 与共享条件接口组合等值、范围、逻辑、数组、正则、文本和地理条件，构建 BSON；查询终结方法应用筛选、排序与投影。',
  ['查找年龄在区间内且姓名匹配的用户','按时间倒序查询并只返回部分字段','查询数组元素和附近的位置'],
  '条件查询|筛选|过滤|范围|正则|模糊|排序|投影|数组|地理|全文|eq|regex|geo|and|or',
  ['conditions.query.QueryWrapper','conditions.interfaces.query.logic.Logic','toolkit.Filters#eq'],
  [e('handlers.condition.BuildCondition','按条件元对象分派构造操作符'),e('toolkit.Filters#eq','实际 BSON 筛选编码'),e('mapper.AbstractBaseMapper#list','应用 Wrapper 并执行查询')],
  ['conditionBuild'], '通过 Wrappers/QueryWrapper 构建条件，传入 Mapper 或使用绑定 Mapper 的 chain。',
  ['condition=false 仅跳过相应条件；不能假设 null/空集合统一忽略。','Wrapper 可变，不应跨请求共享；地理/文本查询还依赖相应服务端索引。'],
  tests=[t('aggregate/QueryPredicateEncodingTest','numericTypesFieldOrderAndNestedArrayOrderRemainExact','筛选 BSON 的类型、字段和嵌套数组顺序；不是数据库查询测试')])

c('nested-fields', '嵌套文档字段路径', ('数据读写','条件查询'),
  'FieldChain 串联字段引用，FunctionUtil 可生成普通或带 $ 的嵌套路径，供查询及聚合复用。',
  ['不手写字符串访问订单中客户的姓名'], '嵌套字段|嵌套路径|子对象|点路径|FieldChain|FunctionUtil',
  ['function.FieldChain','toolkit.FunctionUtil'],
  [e('function.FieldChain#build','拼接字段链及可选 $ 前缀'),e('toolkit.FunctionUtil#getFieldName','解析 Lambda 字段及映射名称')],
  ['nestedField'], '直接调用 FieldChain.of(...).then(...).build(...)；结果作为字段路径传入 API。',
  ['路径构建不验证数据库内的数据结构；不自动完成关联查询。'])

c('pagination', '分页、计数与分页结果', ('数据读写','分页与结果'),
  '提供带总数的 PageResult 和只取页数据的 pageList，按页码/大小计算 skip 和 limit。',
  ['每页显示二十条并返回总记录数','只查询当前页不要总数'], '分页|页码|总数|pageList|page|PageResult|count',
  ['mapper.MongoMapper#page','mapper.MongoMapper#pageList','model.PageResult'],
  [e('mapper.AbstractBaseMapper#page','统计总数并构造页结果'),e('mapper.AbstractBaseMapper#pageList','计算偏移并读取页数据')],
  ['crud'], '传入 Wrapper、pageNum/pageSize 或 PageParam。',
  ['属于偏移分页；不能据此承诺游标分页或高并发一致快照。','稳定翻页需要调用方提供稳定排序。'])

c('update-bulk', '原子字段更新与批量写入', ('数据读写','更新与批量'),
  'UpdateWrapper/Update chain 组织 set、inc、数组等更新操作；bulkWrite 接收 Driver WriteModel 列表统一提交。',
  ['库存原子减一','批量插入和修改多条文档','向数组追加元素'], '更新|原子自增|库存|inc|set|push|批量写|bulkWrite|WriteModel',
  ['conditions.update.UpdateWrapper','mapper.BaseMapper','conditions.update.UpdateChainWrapper'],
  [e('mapper.AbstractBaseMapper#bulkWrite','提交 Driver models 并汇总结果'),e('mapper.AbstractBaseMapper#update','BSON 更新执行与实体条件转换'),e('conditions.update.UpdateChainWrapper','构建更新 BSON')],
  ['crud','conditionBuild'], '构造更新 Wrapper；批量混合操作使用 BaseMapper.bulkWrite 与 Driver models。',
  ['wrapper-only 或直接 BSON 更新不自动走实体自动填充/字段加密。','批量写不自动等同事务；批量返回计数不包含全部 Driver 结果维度。'])

c('raw-bson', '无实体 Document 与原始 BSON 访问', ('数据读写','原生访问'),
  'BaseMapper 显式数据库/集合重载操作 Document/BSON；MongoPlusClient 可取得底层 MongoCollection。',
  ['集合结构不固定不想定义实体','指定数据库和集合执行 BSON 查询'], '无实体|Document|BSON|动态结构|原始查询|BaseMapper',
  ['mapper.BaseMapper','manager.MongoPlusClient#getCollection'],
  [e('mapper.AbstractBaseMapper#list','显式 namespace 查询和结果转换'),e('mapper.AbstractBaseMapper#update','显式 namespace BSON 更新'),e('manager.MongoPlusClient#getCollection','取得 Driver collection')],
  ['noEntity'], '注入 BaseMapper 或从 Configuration 获取；传递 database/collection。',
  ['直接 Driver 操作与 Mapper 插件/转换链不同。','实体元数据依赖的逻辑删除、版本、填充不能无条件套用于无实体路径。'],
  doc_note='官网无实体页混有旧 MongoPlusMapMapper 名称；当前索引使用 BaseMapper，不推荐旧名称。')

c('result-mapping', '查询结果 DTO 与泛型接收', ('数据读写','分页与结果'),
  '查询和聚合允许操作实体与返回类型分离；Class<R> 接收 DTO，TypeReference<R> 携带泛型信息，converter 分派实体、Map 和集合读取。',
  ['查询结果只映射到报表 DTO','把嵌套 Map 和 List 读成泛型结果'], 'DTO|结果映射|泛型|TypeReference|Map|List|返回类型',
  ['mapping.TypeReference','mapper.MongoMapper#list'],
  [e('mapping.AbstractMongoConverter#readInternal','Document/Map/实体读取分支'),e('mapping.MappingMongoConverter#readInternal','按 TypeReference 分派集合和 Map 转换')],
  ['noEntity'], '向 list/page/聚合结果方法传入 Class<R> 或匿名 TypeReference<R>。',
  ['保留泛型信息不等于任意嵌套集合均受支持；convertCollection 有 ArrayList 转型路径。','DTO 类型不自动定义查询的集合 namespace。'])

c('pipeline', '聚合管道编排与统计', ('聚合计算','管道编排'),
  'AggregateWrapper/LambdaAggregateWrapper 构建 match、group、bucket、排序、窗口等阶段；Mapper 的聚合读取入口提交管道。',
  ['按部门分组统计销售额','对数据分桶并进行窗口计算'], '聚合|管道|分组|统计|group|bucket|窗口|Aggregate',
  ['aggregate.Aggregate','aggregate.AggregateWrapper','mapper.MongoMapper#list'],
  [e('aggregate.LambdaAggregateWrapper#group','构建分组阶段'),e('aggregate.LambdaAggregateWrapper#bucket','构建分桶阶段'),e('mapper.AbstractBaseMapper#aggregateList','执行并转换聚合结果')],
  ['aggregate'], '建立独立管道，传入 Mapper 聚合入口，或使用绑定 chain。',
  ['部分重载接收阶段体，部分透传完整 Stage；参见编码边界条目。','阶段是否可执行取决于 MongoDB Server 版本、集合和具体参数；本轮未连接数据库。'],
  tests=[t('aggregate/CommonStageEffectTest','everyAuditedOverloadAppendsOnceToSameReceiver','审计中的常用 Stage 重载仅追加一次、返回相同 receiver 并精确编码')])

c('nested-pipeline', '关联查询与多分支子管道', ('聚合计算','关联与分支'),
  'lookup/unionWith/facet 可以消费独立 Aggregate 子管道，保留内部阶段顺序；关联结果的排序和条数限制放在子管道内；lookup 还可传递 Variable 条目。',
  ['关联集合后对子管道结果排序并限制条数','关联用户表并只保留最新一条','一次聚合同时输出明细和排行榜','合并历史集合的数据'], '关联查询|关联|关联结果|lookup|连表|联表|子管道|facet|多分支|unionWith|合并集合|Variable',
  ['aggregate.Aggregate#lookup','aggregate.Aggregate#facet','aggregate.pipeline.Variable'],
  [e('aggregate.LambdaAggregateWrapper#lookup','读取子 Aggregate 的完整 pipeline'),e('aggregate.LambdaAggregateWrapper#facet','构造命名分支'),e('aggregate.LambdaAggregateWrapper#unionWith','集合合并子管道')],
  ['aggregate','otherCondition'], '各子管道单独创建 Aggregate；再交给外层 lookup/facet/unionWith。',
  ['子管道排序限制关联结果；外层 QueryWrapper 排序不能替代 lookup 内部阶段。','顶层租户/逻辑删除增强不能推定递归注入关联子管道。','BSON 结构测试不证明关联字段或变量在服务器上有效。'],
  tests=[t('aggregate/LookupNestedPipelineTest','preservesSortThenLimit','lookup 内部排序后 limit，内外 receiver 独立'),t('aggregate/FacetNestedPipelineTest','twoIndependentEntriesBecomeOneFacetInOrder','两个独立 facet 分支有序组成一个 Stage'),t('aggregate/UnionWithNestedPipelineTest','u03PreservesSortThenLimit','unionWith 保留子管道顺序')])

c('expressions', '聚合表达式与计算投影', ('聚合计算','表达式'),
  'AggregateOperator、ConditionOperators、Accumulators 和 Projections 组合条件、数值、日期、数组和累加表达式，作为聚合阶段的参数。',
  ['计算单价乘数量作为订单总额','根据条件返回不同等级','把美元开头的字符串当常量而不是字段引用'], '表达式|计算字段|条件分支|乘法|日期|数组|cond|multiply|literal|累加器',
  ['aggregate.pipeline.AggregateOperator#literal','conditions.operation.ConditionOperators#cond','aggregate.pipeline.Projections#computed'],
  [e('aggregate.pipeline.AggregateOperator#literal','保留值为 $literal 表达式'),e('conditions.operation.ConditionOperators#cond','条件表达式编码'),e('aggregate.pipeline.Projections#computed','计算字段 BSON')],
  ['otherCondition'], '将表达式传给 computed/project、group 或对应 Stage。',
  ['cond/condArray 的重载编码不完全相同；应选择明确重载。','Java 可赋值性不等于 BSON codec 或表达式角色合法。'],
  tests=[t('aggregate/LiteralEncodingTest','literalKeepsTypesValuesAndAllOrders','literal 常量值、运行时 BSON 类型和顺序'),t('conditions/operation/ConditionOperatorsCondBsonTest','condArrayWithFourArgumentsShouldAlsoEncodeObjectForm','四参数 condArray 保持对象形式的历史行为')])

c('stage-boundaries', '聚合阶段体与完整阶段的编码边界', ('聚合计算','编码边界'),
  'project(Bson) 包装阶段体为 $project；旧 sort(Bson) 透传参数，sortSpecification(Bson) 明确包装为 $sort。此差异影响自然语言到 API 的选择。',
  ['构建混合升降序的聚合排序','自定义投影 BSON 时如何避免重复包装阶段'], '阶段体|完整阶段|sortSpecification|混合排序|project(Bson)|编码边界',
  ['aggregate.Aggregate#sortSpecification','aggregate.LambdaAggregateWrapper#sort','aggregate.LambdaAggregateWrapper#project'],
  [e('aggregate.Aggregate#sortSpecification','通过 custom 包装 $sort'),e('aggregate.LambdaAggregateWrapper#sort','旧 BSON 重载透传'),e('aggregate.LambdaAggregateWrapper#project','包装 $project')],
  [], '新代码需要排序规格体时使用 sortSpecification；保留旧 sort 的调用契约。',
  ['不能依据参数同为 Bson 就假定相同包装规则。','同一投影混用 include/exclude 的服务端合法性不由 Core 编码成功保证。'],
  status='source_verified', tests=[t('aggregate/SortSpecificationStageTest','legacySortBsonStillPassesTheExactObjectThrough','旧 sort 仍透传同一对象'),t('aggregate/SortSpecificationStageTest','mixedDirectionsPreserveOrderAndInt32InBothReducers','新规格入口产生完整阶段并保留字段顺序'),t('aggregate/ProjectBsonStageBodyTest','projectShouldEncodeCondArrayComputedField','project 包装计算投影阶段体')],
  doc_note='官网 aggregate 列出 sort(Bson)/project(Bson)，未明确解释当前 sortSpecification 和透传差异。')

c('field-mapping', '集合/字段命名与持久化选择', ('实体与字段映射','命名与标识'),
  'CollectionName、CollectionField 及全局命名配置控制集合/数据库、字段别名、排除字段、null 写入和 ObjectId 转换。',
  ['Java 驼峰属性写入数据库下划线字段','排除临时计算属性不存库','指定实体使用的集合和数据库'], '字段映射|集合名|别名|驼峰|下划线|null|排除字段|ObjectId',
  ['annotation.collection.CollectionName','annotation.collection.CollectionField'],
  [e('handlers.collection.AnnotationOperate','解析实体 namespace 和命名策略'),e('mapping.MappingMongoConverter#processFields','应用字段别名和转换'),e('mapping.MappingMongoConverter#shouldIgnoreNull','结合全局和注解决定 null 写入')],
  ['annotation','property'], '标注实体/字段注解；配置 mongo-plus.configuration.field 与 collection。',
  ['字符串路径不具备 Lambda 原字段元数据的全部保障。','字段排除和默认 null 忽略会影响实际写入内容。'], exposure='annotation_configuration')

c('id-generation', '主键生成与回写', ('实体与字段映射','命名与标识'),
  '@ID 配合 IdGenerateHandler 提供当前枚举中的主键策略；实体保存前生成 _id，执行后回写实体。',
  ['保存前自动分配主键','使用自定义的主键生成算法'], '主键|ID|雪花|UUID|ULID|自增id|ObjectId|回写',
  ['annotation.ID','handlers.IdGenerateHandler','enums.IdTypeEnum'],
  [e('mapping.AbstractMongoConverter#writeBySave','生成并写入 _id'),e('incrementer.id.AbstractIdGenerateHandler#generateId','按真实 IdType 分派生成'),e(BOOT+'#setIdGenerateHandler','容器接收自定义主键处理器')],
  ['annotation','preface'], '@ID 指定类型；可注册 IdGenerateHandler，ASSIGN_ID 的生成器另按 IdentifierGenerator 接线。',
  ['不依据官网旧策略数量约束当前枚举。','自增策略涉及计数集合；不要与变动记录插件无验证地组合。'], exposure='annotation_extension')

c('auto-fill', '创建/修改字段自动填充', ('实体与字段映射','字段增强'),
  '实体插入/更新转换分别调用自动填充处理器，按 CollectionField.fill 的 INSERT、UPDATE、INSERT_UPDATE 策略调用 MetaObjectHandler。',
  ['自动记录创建时间和修改时间','保存数据时写入当前用户编号'], '自动填充|创建时间|修改时间|创建人|公共字段|MetaObjectHandler',
  ['handlers.MetaObjectHandler','annotation.collection.CollectionField#fill'],
  [e('mapping.AbstractMongoConverter#writeBySave','插入转换调用 fill'),e('mapping.AbstractMongoConverter#writeByUpdate','更新转换调用 fill'),e('handlers.auto.AbstractAutoFillHandler#handle','按标注收集字段并执行填充'),e(BOOT+'#setMetaObjectHandler','Bean 注册')],
  ['autoFill'], '标注 fill 策略，并提供一个 MetaObjectHandler Bean 或 core 配置。',
  ['wrapper-only/原始 BSON 更新不触发实体自动填充。','填充值由用户实现提供，框架不会凭字段名自动选择时间。'], exposure='annotation_extension')

c('enum-mapping', '枚举值存储与还原', ('实体与字段映射','类型转换'),
  'EnumValue 标识存储值；默认 EnumMappingStrategy/EnumConversionStrategy 参与枚举写入和读取，未标记时使用枚举名称。',
  ['数据库存状态码而 Java 使用枚举'], '枚举|状态码|EnumValue|enum|枚举还原',
  ['annotation.comm.EnumValue'],
  [e('strategy.mapping.impl.EnumMappingStrategy#mapping','选择注解值或 name'),e('strategy.conversion.impl.EnumConversionStrategy','枚举还原'),e('cache.global.MappingCache','默认安装枚举映射')],
  ['autoMappingEnum'], '在枚举值字段标注 @EnumValue；当前 core 默认已装配枚举策略。',
  ['官网示例手动注册映射器；当前实现已有默认值，可自定义覆盖。'], exposure='annotation_configuration')

c('type-conversion', '自定义读写类型转换', ('实体与字段映射','类型转换'),
  'MappingStrategy 控制 Java 值写为 Mongo 类型，ConversionStrategy 控制读取目标类型；TypeHandler 在单个字段上覆盖两向转换。',
  ['把特殊金额类型转换成可存储值','字段用 InputStream 但数据库存二进制','读取日期时转成 Java 时间类型'], '类型处理器|转换器|映射器|二进制|InputStream|MappingStrategy|ConversionStrategy|TypeHandler',
  ['strategy.mapping.MappingStrategy','strategy.conversion.ConversionStrategy','handlers.TypeHandler'],
  [e('mapping.MappingMongoConverter#writeProperties','选择写入映射策略'),e('mapping.MappingMongoConverter#readInternal','读取策略与泛型分派'),e('handlers.field.TypeHandlerFieldHandler','字段写入 TypeHandler'),e('mapping.AbstractMongoConverter#readInternal','字段读取 TypeHandler'),e(BOOT+'#setMapping','收集映射器 Bean'),e(BOOT+'#setConversion','收集转换器 Bean')],
  ['mapping','converter','typeHandler'], '全局策略注册为带泛型的 Bean；字段策略通过 @CollectionField(typeHandler=...) 指定。',
  ['TypeHandler 返回 null 会回退框架转换。','同目标类型策略覆盖和泛型解析需要核对，不能随意给策略加代理。'], exposure='public_extension')

c('extra-fields', '接收实体未声明的额外字段', ('实体与字段映射','字段增强'),
  '读取实体时 @ExtraFields 的 Map 收集未被实体字段消费的文档键，支持忽略列表、键名转换和 Map 类型选择。',
  ['保留 MongoDB 中实体没有定义的字段','把未知字段收进扩展属性 Map'], '额外字段|未知字段|扩展属性|动态字段|ExtraFields|Map兜底',
  ['annotation.collection.ExtraFields','holder.ExtraFieldsHolder'],
  [e('mapping.AbstractMongoConverter#readInternal','追踪已用键并填充额外 Map'),e('mapping.AbstractMongoConverter#createExtraMap','创建受约束的 Map 实例')],
  [], '在实体的 Map 字段标注 @ExtraFields；由默认 converter 读取路径直接处理。',
  ['只确认读取聚合未知字段；不承诺 Map 展平写回。','非 Map 字段会抛异常；自定义 mapType 需可实例化的具体类。'], status='source_verified', exposure='annotation',
  questions=['缺少专用读写往返测试；人工确认与字段排除、_id、键名冲突的组合。'])

c('dbref', '单值 DBRef 引用映射', ('实体与字段映射','关联映射'),
  'DBRefHandler 把被引用对象的 @ID 写成 Driver DBRef，读取时按引用数据库/集合与 id 查询并转换。',
  ['实体字段引用另一个集合的对象'], '引用对象|DBRef|关联映射|引用加载',
  ['annotation.collection.DBRef'],
  [e('handlers.field.DBRefHandler#handler','对象主键转 DBRef'),e('handlers.field.DBRefHandler#read','额外查询还原单值引用'),e('handlers.FieldHandlerChain','默认写链安装'),e('cache.global.HandlerCache','默认读链安装')],
  [], '在引用字段标注 @DBRef；被引用类型应含 @ID 及可解析 namespace。',
  ['仅静态确认单值路径；不承诺集合引用、懒加载、级联保存或删除。','读取会增加查询，跨源/事务组合未验证。'], status='source_verified', exposure='annotation')

c('dictionary-bind', '字典字段回写候选', ('实体与字段映射','字段增强'),
  'FieldBindHandler 能按 @FieldBind 将源值交给用户 DataBindHandler 写入目标属性；尚未确认无需手动接线的完整使用契约。',
  ['查询后把状态码自动补成状态名称','根据字典给结果补充显示字段'], '字典|回写|状态名称|FieldBind|DataBindHandler|字段绑定',
  ['annotation.collection.FieldBind','handlers.DataBindHandler','handlers.read.FieldBindHandler'],
  [e('handlers.read.FieldBindHandler#read','查找 target 并调用用户处理器'),e('mapping.AbstractMongoConverter#readInternal','按字段顺序执行读处理器'),e(BOOT+'#setReadHandler','可接收显式 ReadHandler Bean')],
  [], '未找到内置 FieldBindHandler 自动注册；候选接线为显式 new FieldBindHandler(dataBindHandler) Bean/读链注册，需验证。',
  ['不能仅注册 DataBindHandler 就假定自动生效。','target 转下划线与属性查找、字段遍历后续覆盖之间的行为需要测试。'],
  status='candidate', exposure='annotation_extension', questions=['添加最小外部示例，验证目标字段命名、字段顺序和 Map/DTO 行为。'],
  manual_review=['字典回写接线：确认 FieldBindHandler 显式注册、target 命名与字段顺序，验证 Map/DTO 回写；当前仅局部静态证据。'])

c('tenant', '租户条件隔离与显式忽略', ('安全隔离与一致性','租户与删除'),
  'TenantHandler 提供租户值/字段，TenantInterceptor 对覆盖的读写条件和顶层聚合追加租户信息；支持忽略策略和 TenantManager。',
  ['根据当前租户隔离查询数据','管理任务临时忽略租户限制'], '租户|多租户|租户隔离|tenant_id|TenantHandler|IgnoreTenant',
  ['handlers.TenantHandler','manager.TenantManager#withoutTenant','annotation.tenant.IgnoreTenant'],
  [e('interceptor.business.TenantInterceptor#executeQuery','查询条件追加租户'),e('interceptor.business.TenantInterceptor#executeAggregates','只增强顶层 match'),e(BOOT+'#setTenantHandler','Bean 触发注册插件')],
  ['tenant'], '注册 TenantHandler Bean；管理范围可使用 @IgnoreTenant/withoutTenant。',
  ['不是数据库权限机制；直接 Driver 与原始路径应另核验。','lookup 子管道不自动递归注入租户；嵌套忽略及线程传播需核验。'], exposure='public_extension')

c('logic-delete', '逻辑删除与未删除过滤', ('安全隔离与一致性','租户与删除'),
  '开启逻辑配置并登记实体后，相关拦截器给查询加未删除条件，将覆盖的删除入口改写为更新逻辑字段。',
  ['删除记录但保留数据供恢复','正常查询排除已删除数据'], '逻辑删除|软删除|未删除|CollectionLogic|IgnoreLogic',
  ['annotation.collection.CollectionLogic','manager.LogicManager','annotation.logice.IgnoreLogic'],
  [e('interceptor.business.CollectionLogiceInterceptor','查询与相关执行路径增强'),e('interceptor.business.LogicRemoveInterceptor','删除改写为更新'),e('config.Configuration#logic','按配置安装逻辑删除组件')],
  ['logic','annotation'], '配置 mongo-plus.configuration.logic；明确逻辑字段、删除值和实体登记；按需启用 auto-fill。',
  ['依赖集合到实体的元数据；无实体模式不能自动推定可用。','IgnoreLogic 对不同执行路径的组合未在本轮运行验证；不能承诺任意入口都能绕过。'], exposure='annotation_configuration')

c('optimistic-lock', '版本乐观锁与受控重试', ('安全隔离与一致性','并发与事务'),
  'OptimisticLockerInterceptor 在识别实体版本字段的更新中，把旧版本加入 filter 并生成增量；可配置失败异常和 Retry。',
  ['防止并发更新覆盖其他人的修改','版本冲突时进行有限次数重试'], '乐观锁|并发覆盖|版本冲突|Version|Retry|重试',
  ['annotation.collection.Version','interceptor.business.OptimisticLockerInterceptor','model.Retry'],
  [e('interceptor.business.OptimisticLockerInterceptor#updateParamHandler','取 Integer 版本、附加过滤并自增'),e('interceptor.business.OptimisticLockerInterceptor#beforeRetry','异步分支仍调用 Future.get'),e(BOOT+'#setAdvancedInterceptor','收集插件 Bean')],
  ['optimisticLocker'], '版本字段标注 @Version，注册 OptimisticLockerInterceptor；可配置 Retry。',
  ['当前处理路径用 Integer/getInteger，不承诺任意 Number 类型。','首次更新需在 $set 中携带旧版本；缺失时默认跳过版本增强，可配置异常。','必须确认 BSON 结构被真正回写；不要复用更新 Wrapper。','官网说异步重试立即返回，当前源码 supplyAsync(...).get() 会等待结果。'], exposure='annotation_extension',
  manual_review=['乐观锁运行边界：Integer 版本、缺少旧版本时的行为、BSON 回写和重试结果；本轮未运行并发或数据库验证。'])

c('transaction', 'MongoDB 会话事务', ('安全隔离与一致性','并发与事务'),
  '@MongoTransactional 和 MongoTransactionalManager 建立线程内会话，ExecutorFactory 按上下文选择 SessionExecute；Spring 事务管理器为条件装配。',
  ['多个数据库操作要一起提交或回滚','使用 Spring 事务注解控制 Mongo 操作'], '事务|提交|回滚|ClientSession|MongoTransactional|Transactional',
  ['annotation.transactional.MongoTransactional','manager.MongoTransactionalManager'],
  [e('handlers.TransactionHandler#transaction','事务执行、异常处理与关闭'),e('execute.ExecutorFactory#getOriginalExecute','会话存在时选择 SessionExecute'),e('boot:transactional.MongoTransactionManagerAutoConfiguration','条件注册 Spring 事务管理器')],
  ['transactional','property'], '使用受代理的 @MongoTransactional 方法或编程式 manager；Spring @Transactional 要开启 mongo-plus.spring.transaction。',
  ['依赖支持事务的 MongoDB 部署和版本；直接 Driver 调用须显式携带会话。','上下文基于当前线程，不承诺异步传播或跨 MongoClient 原子性。','官网编程式示例使用较旧 MongoTransactionalOperate；该类 JavaDoc 指向 manager。'], exposure='annotation_configuration')

c('cross-source-atomic', '跨独立数据源原子事务候选', ('安全隔离与一致性','并发与事务'),
  '分片模块有多会话提交/回滚协调代码，但证据不能建立跨独立数据源的原子提交保证。',
  ['两个独立 Mongo 数据源要么同时成功要么同时失败'], '跨库事务|分布式事务|跨数据源原子|两阶段提交|独立数据源',
  ['sharding:sharding.ShardingTransactionalHandler'],
  [e('sharding:sharding.ShardingTransactionalHandler#transaction','调用全部会话提交/关闭'),e('sharding:context.ShardingTransactionContext#commitCurrentAllTransaction','逐个提交会话')],
  [], '分片 Starter 更换 TransactionHandler 后可进入协调代码；不是已确认的原子事务启用方式。',
  ['逐个会话 commit 不能作为原子保证。','不得将此候选直接推荐为分布式事务解决方案。'], status='candidate', exposure='optional_module',
  questions=['需要设计者确认支持契约，并用部分提交失败、网络中断及补偿场景验证。'], negative=['跨数据源原子提交已得到保证'], related=['transaction','sharding'],
  manual_review=['跨数据源原子性：逐会话提交不构成原子提交保证；须确认契约并验证部分提交失败、网络中断及补偿。'])

c('field-encryption', '实体字段加密、解密与查询值转换', ('安全隔离与一致性','字段保护'),
  '@FieldEncrypt 在实体写入链加密，读链按 findDecrypt 解密；有原字段元数据的查询条件路径会转换查询值。',
  ['对敏感字段加密存储并在读取时解密','查询加密字段时自动处理查询值'], '字段加密|加密存储|解密|密文|FieldEncrypt|AES|RSA',
  ['annotation.comm.FieldEncrypt'],
  [e('handlers.field.EncryptFieldHandler#handler','写入字段加密'),e('handlers.read.FieldEncryptApply#read','按配置解密'),e('handlers.condition.EncryptorConditionHandler#beforeQueryCondition','Lambda 原字段条件的查询值加密'),e('cache.global.HandlerCache','默认条件/读取处理器注册')],
  ['property'], '标注 @FieldEncrypt 并提供算法所需密钥；默认链接线，检查注解和全局 encryptor 配置。',
  ['原始 BSON、字符串字段和 wrapper-only 更新不能假定有相同加密处理。','不承诺随机加密算法支持等值检索或范围检索。'], status='source_verified', exposure='annotation_configuration',
  doc_note='官网导航出现字段加密主题，但本次该链接落到首页，未取得字段加密专页正文；property 只确认全局密钥配置。')

c('desensitization', '读取字段脱敏', ('安全隔离与一致性','字段保护'),
  '@Desensitization 使读取处理链应用内置规则或用户 DesensitizationHandler，处理展示值。',
  ['手机号和邮箱展示时隐藏中间部分'], '脱敏|手机号掩码|邮箱隐藏|遮盖|Desensitization|mask',
  ['annotation.comm.Desensitization','handlers.DesensitizationHandler'],
  [e('handlers.read.DesensitizationHandlerApply#read','规则或自定义处理器处理读取值'),e('cache.global.HandlerCache','默认读取链安装')],
  ['desensitization'], '在接收实体/DTO 字段标注注解，按需提供自定义处理器类。',
  ['属于读取结果转换，不能当作数据库加密或日志脱敏保证。'], exposure='annotation')

c('sensitive-word', '敏感词校验、词库维护与显式替换', ('安全隔离与一致性','字段保护'),
  '可选模块按局部字段或全局拦截路径检查文本；Manager 支持黑白名单增删及显式 replace 调用。',
  ['保存文本前拒绝敏感词','动态维护黑白名单词库','显式调用工具替换敏感词'], '敏感词|违禁词|词库|黑白名单|SensitiveWord|文本校验|replace',
  ['word:annotation.SensitiveWord','word:manager.SensitiveWordManager'],
  [e('word:property.SensitiveWordProperty#initSensitiveType','选择局部字段链或全局拦截器'),e('word:manager.SensitiveWordManager#handler','发现敏感词抛异常'),e('word:manager.SensitiveWordManager#replace','提供显式替换方法'),e('boot:word.SensitiveWordConfiguration','classpath 条件和 Bean 注册')],
  ['sensitiveWord'], '加入敏感词模块，配置 mongo-plus.sensitive-word；LOCAL 使用字段注解，词库可用 LoadExtraWord。',
  ['官网说暂不支持替换；当前仅明确存在手动 Manager.replace，不能推成保存时自动替换。','GLOBAL 会增加处理量；无本轮数据库/性能测试。'], exposure='optional_module')

c('nonnull-validation', 'NonNull 自动参数校验候选', ('安全隔离与一致性','操作保护'),
  '@NonNull 是 JSR-305 类型限定别名；当前未找到把其自动接入数据库操作前运行时校验的消费者。',
  ['只标注 NonNull 就自动拒绝空参数吗'], 'NonNull|空值校验|非空校验|注解自动验证',
  ['annotation.comm.NonNull'],
  [e('annotation.comm.NonNull','类型限定注解声明'),e('mapping.MappingMongoConverter#processFields','当前转换路径无此注解校验')],
  [], '没有确认的自动启用入口；统一执行前校验应考虑公开执行拦截器。',
  ['不得将静态类型标记推荐成运行时 Bean Validation。'], status='candidate', exposure='annotation',
  questions=['维护者确认是否仅供静态分析，以及是否计划提供运行时校验。'], related=['execution-extension'],
  manual_review=['NonNull 运行时校验：当前只有类型限定证据，未确认自动校验消费者，不可作为运行时校验方案。'])

c('block-attack', '空条件更新/删除防护入口', ('安全隔离与一致性','操作保护'),
  'BlockAttackInnerListener 检查 update/delete 命令第一个操作的 q，空条件时抛出异常；配置可以安装此 Listener。',
  ['防止忘记条件更新或删除整个集合'], '全表更新|全集合删除|空条件|误删|防护|blockAttackInner',
  ['listener.business.BlockAttackInnerListener'],
  [e('listener.business.BlockAttackInnerListener#commandStarted','检查命令数组第一个 q'),e(BOOT+'#setListener','读取 blockAttackInner 并添加监听器'),e('listener.BaseListener#commandStarted','Driver 事件转发和异常处理')],
  ['property','preface'], '配置 mongo-plus.configuration.collection.block-attack-inner=true。',
  ['只检查第一个操作；不能宣称覆盖所有 bulk 子操作。','源码中的事件回调抛异常不足以证明 Driver 必然阻止网络执行；需要运行验证，不作为已验证的绝对安全屏障。'], exposure='configuration',
  questions=['在实际 Driver 上验证监听器抛异常是否阻止命令，以及批量后续空 filter 行为。'],
  manual_review=['防误删运行边界：Driver 是否阻止发送、批量后续空条件操作均待验证；不可作为已验证的绝对安全屏障。'])

c('encrypted-config', '连接配置密文解密', ('连接路由与集成','连接配置'),
  'Boot EnvironmentPostProcessor 对受支持属性源中的 ${ENC:...} 形式配置使用 mpw.key 解密。',
  ['配置文件不想明文保存数据库密码'], '配置加密|数据库密码|ENC|mpw.key|凭据',
  ['boot:property.EncryptionPostProcessor'],
  [e('boot:property.EncryptionPostProcessor#postProcessEnvironment','处理 OriginTrackedMapPropertySource 的加密值'),e('boot:property.EncryptionPostProcessor#detected','检查完整前后缀')],
  ['security'], 'Boot spring.factories 注册后提供 mpw.key 和包裹的属性值；见注册资源引用。',
  ['只处理实现筛选的属性源，不能承诺所有环境变量/外部配置源均解密。','这是启动配置解密，与实体字段加密分开。'], exposure='configuration')

c('execution-extension', '执行前校验、参数改写与执行控制', ('扩展与事件','执行扩展'),
  '普通 Interceptor 在受支持策略的执行前改写参数，执行成功返回后回调；AdvancedInterceptor 可包围调用、调用 proceed 或返回自定义结果。',
  ['在执行数据库操作之前统一校验参数','为所有查询追加业务条件','自定义拦截器控制是否继续执行'], '执行前|校验参数|统一校验|拦截器|参数改写|Interceptor|proceed|执行后',
  ['interceptor.Interceptor','interceptor.AdvancedInterceptor','interceptor.Invocation#proceed'],
  [e('proxy.ExecutorProxy#invoke','按执行策略调用 before/参数替换和成功后的 after'),e('proxy.AdvancedProxy#invoke','激活时交给高级插件'),e('execute.ExecutorFactory#getExecute','内高级外普通的包装路径'),e(BOOT+'#setInterceptor','收集普通 Bean'),e(BOOT+'#setAdvancedInterceptor','收集高级 Bean')],
  ['interceptor'], '注册相应接口 Bean 或通过 Configuration/Chain 显式注册，设置 order。',
  ['普通 before 取决于策略映射，异常路径不会进入普通 after。','直接 Driver 操作不自动经过 Execute 代理。','不要用命令成功通知替代此参数校验扩展。'], exposure='public_extension')

c('command-events', '数据库命令事件、回调与日志', ('扩展与事件','命令事件'),
  'MongoUtil 创建客户端时安装 BaseListener，转为 MongoPlus 事件并分发给按 getOrder 排序的 Listener。用户可以在成功回调中接入通知逻辑，也可配置内置 LogListener 记录命令与部分结果计数。',
  ['每次数据库命令执行成功后发送通知','寻找可靠通知相关回调入口，但投递保证需另行设计','统计数据库命令耗时并记录失败','开启数据库命令日志及可执行语句格式化'], '命令成功|通知|执行成功后通知|命令监听|失败事件|Listener|commandSucceeded|耗时|日志|语句格式化',
  ['listener.Listener#commandSucceeded','listener.Listener#commandStarted','listener.Listener#commandFailed'],
  [e('toolkit.MongoUtil#getMongo','客户端安装 BaseListener'),e('listener.BaseListener#commandSucceeded','从 Driver 命令成功事件构造框架事件'),e('listener.MongoPlusListener#commandSucceeded','向注册监听器分发'),e('listener.business.LogListener','打印命令、指定结果计数与失败日志'),e(BOOT+'#setListener','Bean 注册并排序')],
  ['listener','property'], '实现 Listener 并注册 Bean；通知发送由项目自行实现；core 可用 Configuration.listener；内置命令日志通过 mongo-plus.log/pretty 等配置。',
  ['命令成功不等于事务已提交或恰好一次消息投递；同一业务可能产生多个命令。','已有自建 MongoClient 是否安装此 Driver listener 需另查；回调同步与异常由 Driver 语义约束。'], exposure='public_extension',
  negative=['事务提交后恰好一次可靠通知','数据库变更 CDC 订阅'], related=['transaction','change-record'],
  retrieval={'unsupportedIntentGroups': [
      [['通知','消息投递','消息发送'], ['可靠','恰好一次','exactly once','事务提交后','提交后']],
      [['CDC','变更流'], ['订阅','监听']]]})

c('field-chain', '有序字段写入处理链', ('扩展与事件','字段扩展'),
  'FieldHandlerChain 在注册时按 order 排序；转换过程把前序非 null 结果作为 currentValue 传递给后序 FieldHandler。',
  ['在加密前先标准化字段内容','多个字段处理器按顺序接收前一个处理结果'], '字段处理链|FieldHandler|currentValue|处理顺序|字段标准化|前序值',
  ['handlers.FieldHandler','handlers.FieldHandlerChain#register','handlers.FieldHandlerChain#registerAll'],
  [e('handlers.FieldHandlerChain#sortHandlers','注册后排序'),e('mapping.MappingMongoConverter#processFields','遍历已排序链并传递最新值'),e('handlers.FieldHandler#handler','新双参接口向旧方法兼容委托')],
  [], '通过 FieldHandlerChain.getInstance().register/registerAll 显式注册；没有确认通用 FieldHandler Bean 自动收集。',
  ['旧单参数 handler 标记 Deprecated；双参数默认委托旧实现。','null 返回值表示保留当前值；全局可变链应在初始化期注册。','HandlerCache.fieldHandlers 是 Deprecated 兼容别名。'], status='source_verified', exposure='public_extension')

c('read-chain', '读取字段加工与中止赋值', ('扩展与事件','字段扩展'),
  'ReadHandler 在字段赋值前按顺序读取转换值，activate 控制参与，discontinue 可以终止该字段的读取赋值路径。',
  ['给查询结果字段做统一加工','特殊值出现时跳过属性赋值'], '读取处理器|ReadHandler|字段加工|停止赋值|discontinue',
  ['handlers.ReadHandler'],
  [e('mapping.AbstractMongoConverter#readInternal','激活/中止逻辑及字段赋值'),e('cache.global.HandlerCache#sortReadHandler','按 order 排序'),e(BOOT+'#setReadHandler','Bean 自动收集')],
  [], '注册 ReadHandler Bean 或 HandlerCache.addReadHandler；保留框架默认解密/脱敏/DBRef 处理器。',
  ['discontinue 终止的是当前字段 lambda，不能解释为中止整个查询。','TypeHandler 和通用转换与 ReadHandler 的组合应按实际顺序核验。'], status='source_verified', exposure='public_extension')

c('namespace-aware', '命名空间生命周期感知内部接线', ('扩展与事件','生命周期'),
  'Aware/NamespaceAware 注册到缓存；DefaultBaseIndexImpl 解析 namespace 时分发感知回调，逻辑删除实现使用该机制登记元数据。',
  ['理解集合首次登记时如何初始化插件元数据'], 'namespace生命周期|Aware|集合登记|元数据初始化',
  ['aware.Aware','aware.impl.NamespaceAware'],
  [e('cache.global.AwareHandlerCache','保存和查找感知处理器'),e('logic.LogicNamespaceAware','内部逻辑删除接线'),e('index.impl.DefaultBaseIndexImpl#getNamespace','解析 namespace 时通知感知处理器')],
  [], '当前用于框架初始化；不推荐以内部静态缓存构建业务事件系统。',
  ['public 可见不代表稳定的业务扩展契约；未找到专门使用测试。'], status='internal_only', exposure='internal_implementation')

c('connection-config', '连接池、URI 与 TLS 配置', ('连接路由与集成','连接配置'),
  'BaseProperty/UrlJoint 将连接配置组织成 Driver ConnectionString；MongoUtil 可安装自定义 SSLContext 并创建客户端。',
  ['配置连接超时和连接池大小','使用 URI 或客户端证书连接 MongoDB'], '连接池|连接超时|URI|TLS|SSL|证书|数据库连接',
  ['model.BaseProperty','toolkit.UrlJoint','boot:property.MongoDBConnectProperty'],
  [e('toolkit.UrlJoint#jointMongoUrl','组织 URI 参数'),e('toolkit.MongoUtil#getMongo','连接字符串、SSL 和客户端构建'),e('boot:config.MongoPlusConfiguration#mongoClientFactory','连接配置创建客户端工厂')],
  ['property'], '配置 mongo-plus.data.mongodb；URI 及结构字段优先级按 UrlJoint；TLS 文件与密钥按 BaseProperty。',
  ['官网 Driver 参数默认值可能对应旧版本，本索引不将其当作当前运行保证。','已有 MongoClient Bean 与容器条件装配交互需实测。'], exposure='configuration')

c('datasource-routing', '声明式多数据源选择', ('连接路由与集成','数据源路由'),
  '@MongoDs 与容器切面在方法执行期间设置数据源名，支持方法/类选择及 DataSourceHandler。',
  ['根据租户或业务动态选择数据源','一个服务方法切换到另一个数据库连接'], '动态数据源|切库|MongoDs|slave-data-source|多源路由|DataSourceHandler',
  ['annotation.datasource.MongoDs','handlers.DataSourceHandler'],
  [e('boot:datasource.MongoDataSourceAspect#manageDataSource','选择源并在 finally 清上下文'),e('manager.MongoPlusClient#getCollectionManager','按数据源取得集合管理器'),e('boot:config.MongoPlusConfiguration#mongoClientFactory','创建从数据源客户端')],
  ['dataSource','annotation'], '配置命名 slave-data-source；在受代理的方法/类上标注 @MongoDs；动态值可交给 handler。',
  ['ThreadLocal 采用清理而非恢复外层栈；嵌套与异步传播需核验。','Spring 表达式实现会去除 #，不承诺任意标准 SpEL 参数写法。'], exposure='annotation_configuration')

c('dynamic-collection', '运行时集合名选择', ('连接路由与集成','集合路由'),
  'CollectionNameHandler 根据执行种类、参数和 namespace 返回集合名，DynamicCollectionNameInterceptor 替换执行参数中的 MongoCollection。',
  ['按月份把订单读写路由到不同集合','根据业务条件动态选择集合'], '动态集合|分表|按月集合|CollectionNameHandler|集合路由',
  ['handlers.CollectionNameHandler'],
  [e('interceptor.business.DynamicCollectionNameInterceptor#beforeExecute','替换末尾 collection 参数'),e(BOOT+'#setDynamicCollectionHandler','Bean 触发插件注册')],
  ['dynamicCollection'], '注册 CollectionNameHandler Bean 或配置 DynamicCollectionNameInterceptor。',
  ['不能据此承诺索引管理也自动路由；原始 collection 与元数据登记可能不同。','handler 必须在不匹配时返回原集合名。'], exposure='public_extension')

c('runtime-datasource', '运行时追加命名数据源', ('连接路由与集成','数据源路由'),
  'DataSourceManager.appendTempDataSource 创建客户端并按数据库名登记 collection managers，isOverride 决定是否覆盖已存在名称。',
  ['系统运行时给新客户追加一个数据库连接'], '运行时追加|临时数据源|新增连接|appendTempDataSource|DataSourceManager',
  ['manager.DataSourceManager#appendTempDataSource'],
  [e('manager.DataSourceManager#appendTempDataSource','创建客户端并更新映射'),e('boot:config.MongoPlusConfiguration#dataSourceManager','容器提供管理器 Bean')],
  [], '注入 DataSourceManager 并显式调用 appendTempDataSource(dsName, baseProperty, isOverride)。',
  ['同类手动 changeDataSource/clearDataSource 的 JavaDoc 明确不推荐；优先声明式路由。','覆盖旧客户端、同名不覆盖时新建客户端的释放和并发安全需人工确认。'], status='source_verified')

c('container-integration', 'Boot 与 Solon 容器接入', ('连接路由与集成','容器接入'),
  '独立 Boot/Boot4 Starter 和 Solon 插件通过资源入口安装配置、Mapper 扫描/代理与扩展 Bean 收集。',
  ['在 Spring Boot 项目中自动注入 Mapper','在 Solon 中启用 MongoPlus'], '自动装配|Bean注册|Starter|Spring Boot|Boot4|Solon|Mapper扫描|MongoMapperScan',
  ['boot:annotation.MongoMapperScan','boot:config.MongoPlusConfiguration','solon:config.XPluginAuto'],
  [e('boot:registrar.MongoMapperScannerRegistrar','Mapper 扫描注册入口'),e('boot:config.MongoPlusConfiguration','条件创建核心组件'),e('boot4:config.MongoPlusConfiguration','Boot4 独立配置'),e('solon:config.XPluginAuto','Solon Bean 构造和插件生命周期')],
  ['mapper','started','home'], '选择对应模块；Boot 扫描 @MongoMapperScan，Solon 按 @Mongo 标注；资源接线见 registrationReferences。',
  ['容器模块是可选适配，不应将各自同名 FQCN 混用；引用必须包含 module。','本轮未验证 JDK/Driver/容器版本兼容矩阵。'], exposure='container_integration')

c('sharding', '可选数据源分片路由', ('连接路由与集成','数据源路由'),
  '分片模块按执行种类选择目标数据源，支持用户策略与负载选择，并替换执行 collection；不是跨所有分片结果合并引擎。',
  ['根据业务分片键选择目标数据源','给读写指定不同的数据源策略'], '分片|读写分离|负载均衡|sharding|路由策略',
  ['sharding:sharding.AbstractDataSourceShardingHandler','sharding:sharding.DataSourceShardingStrategy','sharding:interceptor.DataSourceShardingInterceptor'],
  [e('sharding:interceptor.DataSourceShardingInterceptor#beforeExecute','策略和负载选择后替换集合'),e('shardingboot:config.MongoShardingConfiguration','创建插件并更换 TransactionHandler')],
  [], '加入 sharding 模块及相应 Starter 或显式注册插件；自定义 handler/strategy 按构造参数传入。',
  ['实现要求分片策略为最后拦截器；顺序与并发组合需验证。','一次选择一个数据源，不代表跨分片查询合并；不提供已验证的分布式原子事务保证。'],
  status='source_verified', exposure='optional_module', negative=['所有分片结果自动合并','跨分片自动合并','全局分页'], related=['cross-source-atomic'])

c('custom-codec', 'Driver BSON Codec 扩展', ('实体与字段映射','类型转换'),
  'MongoPlusCodec 扩展 Driver Codec；容器创建工厂时先收集 codec，MongoUtil 将其与默认 registry 合并安装到新客户端。',
  ['给自定义值类型注册 BSON 编解码器'], 'BSON编解码|Codec|MongoPlusCodec|自定义编码',
  ['codecs.MongoPlusCodec','cache.codec.MongoPlusCodecCache#addCodec'],
  [e('boot:config.MongoPlusConfiguration#mongoClientFactory','创建客户端前收集 MongoPlusCodec Bean'),e('toolkit.MongoUtil#getMongo','合并 CodecRegistries 并安装到 Driver')],
  [], '在客户端创建前注册 MongoPlusCodec Bean；core 可显式加入 cache 后创建客户端。',
  ['已创建客户端不会因后续注册自动重建 registry。','Driver registry 与某些 BSON 构建测试使用的 registry 不同，不能承诺所有入口一致。'], status='source_verified', exposure='public_extension')

c('developer-index', '源码 API 证据生成工具内部入口', ('连接路由与集成','开发工具'),
  '现有 mongo-plus-indexer 提供源码 API/聚合语义证据生成；属于开发工具模块，不是 MongoDB 业务执行能力。',
  ['追溯 API 的参数语义和源码证据'], '开发索引|API证据|SourceScanner|Indexer|语义元数据',
  ['indexer:indexer.MongoPlusIndexer'],
  [e('indexer:indexer.MongoPlusIndexer','工具公开门面'),e('indexer:indexer.scanner.SourceScanner','JDK compiler tree 扫描与证据生成')],
  [], '参照 mongo-plus-indexer/README.md；本任务没有调用或修改任何已有 API 索引生成器。',
  ['此处只导航现有工具；不能推荐为业务查询 API。','本次 capability 产物只消费 generated 下 JavaParser 声明索引。'], status='internal_only', exposure='developer_tool')

c('indexes', '注解自动建索引与手工索引管理', ('集合运维','索引管理'),
  'IndexUtil 将普通、复合、文本、地理、哈希等注解转换为 IndexModel；启动扫描按配置创建；BaseIndex/Repository 提供手工管理入口。',
  ['启动时创建唯一索引和 TTL 过期索引','按条件创建复合或地理索引','查看和删除现有索引'], '索引|唯一索引|复合索引|TTL|过期|文本索引|地理索引|createIndex',
  ['annotation.index.MongoIndex','index.BaseIndex','repository.IRepository'],
  [e('toolkit.IndexUtil#getIndex','注解转换为索引模型'),e('toolkit.AutoUtil#autoCreateIndexes','将模型提交给对应数据源 collection'),e(BOOT+'#autoCreateIndexes','启动扫描的配置开关'),e('index.impl.AbstractBaseIndex#createIndex','手工索引提交给执行器')],
  ['autoCreateIndex'], '开启 auto-create-index 并确保 @CollectionName 实体在扫描包内；手工管理用 BaseMapper/BaseIndex 或 Repository。',
  ['普通 MongoMapper 不自动声明实体索引便利方法。','自动索引属启动期，不能承诺动态集合自动同步索引；服务端选项合法性待部署验证。'], exposure='annotation_configuration')

c('timeseries', '注解自动创建时序集合', ('集合运维','时序集合'),
  '扫描 @TimeSeries 并构造 TimeSeriesOptions，使用指定时间字段、元字段、粒度和过期等选项创建集合。',
  ['保存设备时序数据并自动创建时间集合'], '时序|时间序列|传感器|TimeSeries|timeField|granularity',
  ['annotation.collection.TimeSeries'],
  [e('toolkit.AutoUtil#autoCreateTimeSeries','查询已有时序集合并构造/创建'),e(BOOT+'#autoCreateTimeSeries','扫描配置开关')],
  ['autoCreateTimeSeries'], '开启 auto-create-time-series，标注 @TimeSeries 并确保扫描范围覆盖实体。',
  ['服务端版本/选项兼容未运行验证。','bucketMaxSpan/bucketRounding 分支会清除 metaField；捕获 MongoCommandException，不能凭启动继续就判定已创建。'], exposure='annotation_configuration')

c('backup', '集合本地 JSON/ZIP 备份与恢复', ('集合运维','备份与镜像'),
  'BackupManager 分页读取 Document 写出 JSON 后压缩为 ZIP，并按文件元信息重新导入；不生成 Excel 工作簿；导入路径包含删除集合。',
  ['把指定集合导出成本地 JSON/ZIP 备份文件','从备份压缩包恢复集合'], '备份|集合备份|JSON|恢复|ZIP|BackupManager',
  ['manager.BackupManager#export','manager.BackupManager#imports'],
  [e('manager.BackupManager#backupCollectionToJSON','分页写出并通知 BackupListener'),e('manager.BackupManager#imports','读取 ZIP、首条目 drop、执行插入')],
  ['dataBackup'], '创建 BackupManager，设置路径/集合/数据源/页大小后显式 export/imports；按需添加 BackupListener。',
  ['只支持此处描述的 JSON/ZIP 备份入口；Excel/xls/xlsx 工作簿导出无能力证据。','导入首个 ZIP 条目前会 drop 目标集合；本任务仅阅读，没有执行备份恢复。','分页导出不是已证明的一致快照；未确认索引、类型及多集合恢复完整性。'],
  negative=['Excel 工作簿导出','xlsx 导出','xls 导出'],
  retrieval={'requiredAny': ['备份','恢复','ZIP','JSON','BackupManager'],
             'excludedAny': ['Excel','xlsx','xls','工作簿'],
             'noMatchIntentGroups': [[['Excel','xlsx','xls','工作簿'], ['导出','写成','生成','报表','工作簿']]]},
  manual_review=['备份恢复运行边界：恢复首条目前 drop、BSON 类型保真、索引与多集合完整性、一致快照均需人工复核和运行验证。'])

c('async-mirror', '异步多数据源镜像写', ('集合运维','备份与镜像'),
  'AsyncMultipleWriteInterceptor 按 MultipleWriteHandler 的目标源列表提交后台写任务，然后继续主调用。',
  ['写主数据源时同时给其他源备份一份'], '异步多写|镜像写|多源备份|AsyncMultipleWrite|MultipleWriteHandler',
  ['interceptor.business.AsyncMultipleWriteInterceptor','handlers.write.MultipleWriteHandler'],
  [e('interceptor.business.AsyncMultipleWriteInterceptor#intercept','提交镜像分支后 proceed'),e('interceptor.business.AsyncMultipleWriteInterceptor#executeMultipleWrite','线程池 submit 到目标数据源'),e('config.Configuration#asyncMultipleWrite','编程式安装高级插件')],
  ['dataBackup'], '注册高级插件 Bean 或 Configuration.asyncMultipleWrite，并设置目标数据源及线程池/策略。',
  ['任务可先于主操作最终结果提交；不保证主从事务、成功一致性或持久可靠投递。','重试、线程池关闭与异常处理需要调用方核验。'], exposure='public_extension')

c('change-record', '数据变动记录与批量阈值', ('集合运维','变动记录'),
  'DataChangeRecorderInnerInterceptor 从调用参数构造变动记录，检查批量阈值，可在操作返回后保存到记录集合。',
  ['记录数据修改历史','限制一次批量变动的条数'], '变动记录|修改历史|审计|批量阈值|DataChangeRecorder|操作日志',
  ['interceptor.business.DataChangeRecorderInnerInterceptor'],
  [e('interceptor.business.DataChangeRecorderInnerInterceptor#beforeExecute','解析操作参数和阈值'),e('interceptor.business.DataChangeRecorderInnerInterceptor#afterExecute','保存记录并恢复数据源上下文'),e('interceptor.business.DataChangeRecorderInnerInterceptor#processUpdate','更新参数记录数量判定')],
  ['dataChangeRecorder'], '注册普通拦截器；配置阈值/忽略集合，enableSaveDatabase(baseMapper) 启用记录落库。',
  ['记录源于操作参数，不是数据库 CDC 或完整前后镜像。','必须忽略记录集合以避免递归；不要与自增 ID 无验证地组合。','阈值按参数/models 计算，不等于 updateMany 实际匹配文档数量限制。'], exposure='public_extension')

# Non-Java registration evidence: explicit text anchors, never treated as API records.
REGISTRATION_FILES = {
    'encrypted-config': [('mongo-plus-boot-starter/src/main/resources/META-INF/spring.factories','org.springframework.boot.env.EnvironmentPostProcessor')],
    'container-integration': [
        ('mongo-plus-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports','com.mongoplus.config.MongoPlusConfiguration'),
        ('mongo-plus-boot4-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports','com.mongoplus.config.MongoPlusConfiguration'),
        ('mongo-plus-solon-plugin/src/main/resources/META-INF/solon/anwen.mongo.config.properties','solon.plugin=')],
    'sharding': [('mongo-plus-sharding-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports','com.mongoplus.config.MongoShardingConfiguration')],
}
