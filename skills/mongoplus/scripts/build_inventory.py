"""Rebuild source-derived coverage data. Run SourceInventory first. Writes only this skill."""
import collections, hashlib, json, re, subprocess, xml.etree.ElementTree as ET
from pathlib import Path
S = Path(__file__).resolve().parents[1]
R = S.parent
D = json.loads((S/'audit/source-inventory.json').read_text(encoding='utf-8'))
T = D['types']
HEAD = subprocess.check_output(['git','rev-parse','HEAD'],cwd=R,text=True).strip()
def save(p, v):
    (S/p).parent.mkdir(parents=True,exist_ok=True)
    (S/p).write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def write(p,v):
    (S/p).parent.mkdir(parents=True,exist_ok=True)
    (S/p).write_text(v,encoding='utf-8')
def source(t):
    return f'https://github.com/anwenmp/MongoPlus/blob/{HEAD}/{t["path"]}#L{t["line"]}'

# Each row is a separately reviewable user capability, not a method-count proxy.
ROWS = '''接入Core与独立Java|integration|Configuration
Spring Boot 3接入与Mapper扫描|integration|MongoMapperScan MongoMapperFactoryBean
Spring Boot 4接入与差异|integration|MongoMapperScan MongoPlusAutoConfiguration
Solon接入与代理注入|integration|XPluginAuto CollectionScanner
BOM与版本选择|integration|@bom
实体Mapper访问层|crud|MongoMapper MongoMapperImpl
无绑定实体BaseMapper|crud|BaseMapper Mapper SuperMapper
Repository与Service|crud|IRepository RepositoryImpl IService ServiceImpl
单条保存与ID回写|crud|MongoMapper#save
批量保存与saveOrUpdate|crud|MongoMapper#saveBatch MongoMapper#saveOrUpdate
实体更新与按ID批改|crud|MongoMapper#updateById MongoMapper#updateBatchByIds
单条与批量删除|crud|BaseMapper#remove BaseMapper#removeOne
单条读取与存在性|crud|MongoMapper#one MongoMapper#exist
分页与计数|crud|PageParam PageResult BaseMapper#page BaseMapper#recentPageCount
批量WriteModel操作|crud|BaseMapper#bulkWrite
原生Document与BSON命令|crud|SuperMapper#command BaseMapper#queryCommand
distinct与replaceOne选型|driver-interop|@driver
findOneAndRemove与返回删除前文档|crud|BaseMapper#findOneAndRemove
查询Wrapper与链式终结|query|QueryWrapper LambdaQueryChainWrapper ChainWrappers
比较范围存在与类型|query|Compare DataType
and-or-nor-not逻辑组合|query|And Or Nor Not
嵌套字段与类型安全路径|query|FieldChain FreeFieldChain SFunction
数组all-size-elemMatch|query|All Size ElemMatch
正则like与大小写|query|Regex Like RegexOptions
投影排序分页选项|query|Projection Ordered QueryParam
传统文本查询|query|Text TextSearchOptions
地理空间查询与模型|query|GeoSpatial GeoNear Coordinate GeoBox GeoCenter
位运算与expr-jsonSchema-where|query|Bitwise Expr JsonSchema Where Mod
原生条件与Filters组合|query|Wrapper Filters
更新Wrapper与绑定更新链|update|UpdateWrapper LambdaUpdateChainWrapper
set-unset-rename-currentDate|update|Set Unset Rename CurrentDate
inc-mul-min-max与原子更新|update|Inc Mul Min Max
push-addToSet-pop-pull-pullAll|update|Push AddToSet Pop Pull PullAll PushOptions
updateOne-updateMany与Options|update|BaseMapper#updateOne BaseMapper#update
乐观锁版本与重试|update|OptimisticLockerInterceptor Retry UpdateRetryResult
空条件更新删除保护|update|BlockAttackInnerListener
聚合Wrapper与结果映射|aggregation|AggregateWrapper LambdaAggregateWrapper LambdaAggregateChainWrapper
聚合match-project-sort-skip-limit|aggregation|Aggregate#match Aggregate#project Aggregate#sort Aggregate#skip Aggregate#limit
聚合addFields-set-unset|aggregation|Aggregate#addFields Aggregate#set Aggregate#unset
聚合lookup关联与let|aggregation|Aggregate#lookup Variable
聚合facet与unionWith|aggregation|Aggregate#facet Aggregate#unionWith Facet
聚合graphLookup与unwind|aggregation|Aggregate#graphLookup Aggregate#unwind UnwindOption
聚合group与累加器|aggregation|Aggregate#group Accumulators
聚合bucket-bucketAuto-sortByCount-count|aggregation|Aggregate#bucket Aggregate#bucketAuto Aggregate#sortByCount Aggregate#count
聚合replaceRoot-replaceWith-sample|aggregation|Aggregate#replaceRoot Aggregate#replaceWith Aggregate#sample
聚合out与merge写阶段|aggregation|Aggregate#out Aggregate#merge
聚合窗口densify-fill|aggregation|Aggregate#setWindowFields Aggregate#densify Aggregate#fill FillField
聚合表达式日期数组数学条件转换|expressions|AggregateOperator ConditionOperators SimpleExpression
聚合自定义阶段与执行选项|aggregation|Aggregate#custom AggregateOptions
实体元数据与泛型结果|mapping|TypeInformation FieldInformation TypeReference SimpleTypeHolder
ID生成与ObjectId转换|mapping|IdGenerateHandler IdentifierGenerator IdWorker AutoIdGenerate
集合字段命名与null策略|mapping|AnnotationOperate CollectionNameHandler
枚举持久化|mapping|EnumMappingStrategy EnumConversionStrategy
TypeHandler字段双向转换|mapping|TypeHandler TypeHandlerFieldHandler
MappingStrategy写映射|mapping|MappingStrategy MappingCache
ConversionStrategy读转换|mapping|ConversionStrategy ConversionCache
MongoConverter整体替换|mapping|MongoConverter MappingMongoConverter
自动填充|mapping|MetaObjectHandler AutoFillMetaObject
FieldHandler写责任链|extensions|FieldHandler FieldHandlerChain
ReadHandler读取链与中断|extensions|ReadHandler HandlerCache
DBRef引用写读查询|mapping|DBRefHandler
ExtraFields额外字段|mapping|ExtraFieldsHolder ExtraField
FieldBind字典回写|mapping|DataBindHandler FieldBindHandler
字段加密与解密|field-security|Encryptor EncryptorUtil EncryptFieldHandler FieldEncryptApply
字段脱敏|field-security|DesensitizationHandler DesensitizationHandlerApply
敏感词局部全局及动态词库|sensitive-word|SensitiveWordManager SensitiveWordProperty LoadExtraWord SensitiveWordInterceptor
普通拦截器与权限注入|interceptors|Interceptor InterceptorChain
高级拦截器与执行改写|interceptors|AdvancedInterceptor Invocation AdvancedInterceptorChain
命令监听日志与错误通知|events|Listener BaseListener MongoPlusListener LogListener
ConditionHandler条件转换|extensions|ConditionHandler BuildCondition
Codec与JSR310类型|extensions|MongoPlusCodec Jsr310CodecProvider
Aware命名空间事件|extensions|Aware NamespaceAware
扩展对象注册与生命周期|extensions|Configuration ComponentRegistry
多数据源声明与动态新增|transactions-routing|DataSourceManager MongoClientFactory
动态物理集合|transactions-routing|DynamicCollectionNameInterceptor CollectionNameHandler
编程式MongoDB事务|transactions-routing|MongoTransactionalOperate MongoTransactionalManager
Spring事务与传播|transactions-routing|MongoPlusTransactionalManager MongoTransactionalAspect
事务读偏好与选项|transactions-routing|MongoTransactionalManager#startTransaction
数据源分片与路由策略|sharding|DataSourceShardingStrategy DataSourceShardingHandler
分片事务及Starter|sharding|ShardingTransactionalHandler MongoShardingConfiguration
多租户过滤|isolation|TenantHandler TenantInterceptor TenantManager
逻辑删除恢复与绕过|isolation|LogicRemove LogicDeleteHandler LogicManager
普通单字段唯一稀疏部分TTL索引|indexes|BaseIndex IndexParser
复合哈希地理传统文本索引|indexes|CompoundParser HashParser GeoParser TextParser
手工索引创建列举删除|indexes|BaseIndex Index SuperIndex
Search静态动态多索引|search-vector|SearchParse
Search分析器排序源字段同义词|search-vector|SearchParse
VectorSearch向量过滤HNSW量化|search-vector|VectorParse
SearchVector状态冲突与幂等创建|search-vector|AutoUtil#autoCreateIndexes Resolver
SearchVector查询与部署适配|search-vector|Aggregate#custom
自动时序集合|timeseries|AutoUtil#autoCreateTimeSeries
备份恢复与导出监听|backup-gridfs|BackupManager BackupListener
GridFS上传与Driver下载|backup-gridfs|GridFSBucketManager
连接池TLS认证UUID与URI|configuration|BaseProperty UrlJoint OverridableUuidRepresentationCodecProvider
配置模型优先级与绑定|configuration|MongoDBConnectProperty MongoDBConfigurationProperty MongoDBFieldProperty MongoSpringProperty
配置加密环境后处理|configuration|EncryptionPostProcessor
异步镜像多写|events|AsyncMultipleWriteInterceptor MultipleWriteHandler
数据变动记录|events|DataChangeRecorderInnerInterceptor
公开BSON工具与异常模型|utilities|MongoPlusDocument MongoPlusBasicDBObject MongoPlusException OperationResult
源码索引开发工具|tooling|MongoPlusIndexerMain ApiIndexer
外部持续变更流边界|driver-interop|@driver'''
SCENARIOS={
 'integration':('接入或升级框架、选择容器','选用对应模块；Core使用Configuration，Starter按容器自动配置','Boot3/Boot4不共存；Solon注解接线逐项核对'),
 'crud':('持久化实体、页面与批量任务','初始化BaseMapper；实体接口须绑定T与namespace','空批次、布尔结果语义、原始WriteModel不等于实体转换'),
 'query':('多条件筛选、嵌套对象数组与分页','QueryWrapper<T>或绑定查询链；方法引用依赖真实实体字段','String路径与lambda转换不同；正则默认、空集合与投影需核对'),
 'update':('原子抢占、库存与并发编辑','UpdateWrapper<T>与BaseMapper或绑定链；乐观锁需注册插件','区分单条/多条；版本插件与wrapper-only不是相同路径'),
 'aggregation':('统计、关联与整形结果','AggregateWrapper后交给Mapper或绑定聚合链终结','Bson重载可能收完整阶段；每个重载独立验证完整pipeline'),
 'expressions':('构造聚合阶段中的表达式','选用现有表达式API，缺项使用明确BSON表达式','表达式不自动成为阶段；字段、变量、字面量需区分'),
 'mapping':('历史字段、特殊类型、字典与自动填充','实体元数据及对应Handler/Strategy注册','实体/Document、save/update、Class/TypeReference路径不同'),
 'field-security':('受保护字段的存储与输出','配置密钥或定制Encryptor/DesensitizationHandler','默认BASE64不提供保密性；不可逆算法不能恢复原值'),
 'sensitive-word':('文本审核与动态黑白名单','可选模块；SensitiveWordProperty与Manager及LOCAL/GLOBAL注册','Core属性不代表可选模块已启用；wrapper与原生BSON单独核对'),
 'interceptors':('统一数据权限与执行流程扩展','Bean收集或Configuration/链注册；核对order','普通策略回写、bulk model、索引和异常路径分别核对'),
 'extensions':('扩展转换、条件、编解码及生命周期','按扩展类别显式注册；各容器接线有差异','静态缓存与链可跨上下文累积；不可假设所有Bean自动收集'),
 'events':('命令告警、审计与副本镜像','Listener、记录插件或高级多写拦截器分别注册','命令监听不等于数据库持续变更订阅；镜像无原子提交保证'),
 'transactions-routing':('事务、多源与按业务路由','合法副本集/分片部署；注册命名数据源与相应代理','session须同client；自调用、嵌套与线程切换需验证'),
 'sharding':('按策略选择数据源','可选分片模块及策略；Boot3可用分片Starter','无Boot4专用分片Starter；多源提交不等于分布式原子事务'),
 'isolation':('租户隔离、软删除与恢复','TenantHandler及拦截器；逻辑删除配置与实体注册','顶层增强不能替代lookup/facet/union子管道隔离'),
 'indexes':('唯一约束、TTL及查询性能','自动扫描与auto-create-index或手工Index API','DDL不原子；TTL异步；同名冲突不可静默删除重建'),
 'search-vector':('全文相关度、语义检索与索引管理','支持Search/Vector的部署；实体扫描或手工解析创建','索引声明不生成embedding；运行态与DDL异步就绪单独确认'),
 'timeseries':('时间测量数据','实体扫描及auto-create-time-series或AutoUtil','时间字段应为BSON Date；已有普通集合不自动迁移'),
 'backup-gridfs':('导出恢复和大文件','BackupManager配置路径/集合；GridFSBucketManager.create','备份无跨集合一致性快照保证；验证错误及资源关闭'),
 'configuration':('连接安全、池参数及运行配置','mongo-plus前缀或Core Configuration/BaseProperty','非空URI优先；已声明但无消费者的字段标UNVERIFIED'),
 'utilities':('BSON组合、分页与异常处理','按真实类型及公开方法使用','辅助类公开不等于推荐业务入口；见public-types逐项角色'),
 'tooling':('维护源码能力索引','独立工具模块，非运行依赖','不要求使用者安装索引或MCP；未复制旧JSONL'),
 'driver-interop':('框架缺少专用入口的数据库操作','从MongoPlus获得collection后明确Driver边界','Driver调用不自动经过MongoPlus执行拦截及实体转换'),
}
CAP=[]
for i,row in enumerate(ROWS.splitlines(),1):
    label,topic,apis=row.split('|'); found=[]
    for api in apis.split():
        name,_,method=api.partition('#')
        hits=[t for t in T if t['simpleName']==name and (not method or method in [m['name'] for m in t['methods']])]
        if not hits and not name.startswith('@'):raise ValueError('Unresolved public API: '+api)
        found.extend(t['path'] for t in hits)
    scenario,registration,limits=SCENARIOS[topic]
    CAP.append(dict(id=f'C{i:03}',name=label,topic=topic,document=f'references/{topic}.md',apis=apis.split(),modules=sorted({t['module'] for t in T if t['path'] in found}),sources=sorted(set(found)),scenario=scenario,registration=registration,limitations=limits,status='SOURCE_VERIFIED; runtime UNVERIFIED'))
ANNOTATION_BY_CAPABILITY={
'C001':['CollectionName','ID'],'C002':['Mongo','MongoMapperScan'],'C003':['Mongo','MongoMapperScan'],'C004':['Mongo','MongoTransactional'],
'C006':['Mongo','CollectionName','ID'],'C009':['ID','CollectionField'],'C011':['ID','CollectionField','Version'],
'C020':['CollectionField','ID'],'C035':['Version'],
'C050':['CollectionName','CollectionField','ID'],'C051':['ID'],'C052':['CollectionName','CollectionField'],'C053':['EnumValue'],
'C054':['CollectionField'],'C058':['CollectionField'],'C061':['DBRef'],'C062':['ExtraFields'],'C063':['FieldBind'],
'C064':['FieldEncrypt'],'C065':['Desensitization'],'C066':['SensitiveWord'],
'C074':['MongoDs'],'C077':['MongoTransactional'],'C078':['MongoReadPreference','MongoTransactional'],
'C081':['IgnoreTenant'],'C082':['CollectionLogic','IgnoreLogic'],'C083':['MongoIndex','MongoIndexDs'],
'C084':['MongoCompoundIndex','MongoCompoundIndexes','MongoHashIndex','MongoGeoIndex','MongoTextIndex'],
'C086':['MongoSearchIndex','MongoSearchIndexes','MongoSearchField','MongoSearchFields'],
'C087':['MongoSearchSort','MongoSearchSorts','MongoSearchStoredSource','MongoSearchSynonym'],
'C088':['MongoVectorIndex','MongoVectorIndexes','MongoVectorField','MongoVectorFields','MongoVectorHnswOptions','MongoVectorStoredSource'],
'C091':['TimeSeries']}
for c in CAP:c['annotations']=ANNOTATION_BY_CAPABILITY.get(c['id'],[])
save('audit/capabilities.json',CAP)

def annotation_topic(t):
    n=t['simpleName']
    if '.index.search.' in t['name']:return 'search'
    if '.index.vector.' in t['name']:return 'vector'
    if '.index.' in t['name']:return 'indexes'
    if n in ['MongoDs','MongoReadPreference','MongoTransactional','IgnoreTenant','IgnoreLogic']:return 'routing-isolation'
    if n in ['Mongo','MongoMapperScan']:return 'integration'
    if n in ['FieldEncrypt','Desensitization','NonNull','Nullable','SensitiveWord']:return 'field-processing'
    return 'mapping'
A=[]
for t in T:
    if t['kind']=='ANNOTATION_TYPE':
        A.append(dict(name=t['name'],simpleName=t['simpleName'],module=t['module'],source=t['path'],line=t['line'],document=f'annotations/{annotation_topic(t)}.md#{t["simpleName"].lower()}',example=f'validation/examples/annotations/{t["simpleName"]}Example.java',status='SOURCE_VERIFIED; runtime UNVERIFIED'))
save('audit/annotations.json',A)

def classify(t):
    n=t['simpleName'];p=t['name'];mod=t['module']
    for c in CAP:
        if any(n==a.split('#')[0] for a in c['apis']):return c['topic'],'PUBLIC_API','能力清单已关联的公开入口或协作类型'
    if t['kind']=='ANNOTATION_TYPE':return 'annotations','PUBLIC_ANNOTATION','含容器和嵌套配置；逐项有记录'
    if mod=='mongo-plus-indexer' or mod.startswith('tools/'):return 'tooling','DEVELOPER_TOOL','独立源码分析工具，不进入应用运行链'
    if 'starter' in mod or mod=='mongo-plus-solon-plugin':return 'integration','INTEGRATION_SUPPORT','容器配置、扫描或AOP实现，业务优先用公开注解与接口'
    if mod=='mongo-plus-sharding':return 'sharding','PUBLIC_SUPPORT','分片路由、上下文或策略实现'
    if mod=='mongo-plus-sensitive-word':return 'sensitive-word','PUBLIC_SUPPORT','敏感词注册、返回策略或异常模型'
    if mod=='mongo-plus-annotation':return 'utilities','PUBLIC_SUPPORT','公开枚举、选项或注解配置类型，随使用者文档查阅'
    for token,topic in [('.conditions.interfaces.update.','update'),('.conditions.','query'),('.aggregate.','aggregation'),('.strategy.mapping.','mapping'),('.strategy.conversion.','mapping'),('.handlers.','extensions'),('.codecs.','extensions'),('.aware.','extensions'),('.mapping.','mapping'),('.encryptor.','field-security'),('.index.','indexes'),('.parser.index.','search-vector'),('.listener.','events'),('.interceptor.','interceptors'),('.manager.','transactions-routing'),('.incrementer.','mapping'),('.logic.','isolation'),('.property.','configuration'),('.model.','utilities'),('.bson.','utilities'),('.function.','query'),('.options.','query')]:
        if token in p:return topic,'PUBLIC_SUPPORT','协作类型或默认实现；低层方法不单独写教程，主题提供用法边界'
    if any(z in p for z in ['.cache.','.proxy.','.execute.','.strategy.executor.','.scanner.','.parser.command.','.conn.','.context.','.meta.','.logging.','.constant.','.holder.','.registry.','.convert.']):return 'utilities','INTERNAL_PUBLIC','Java public的基础设施；保留可检索记录，不作为稳定业务扩展契约'
    return 'utilities','PUBLIC_SUPPORT','BSON、反射、ID、结果或异常辅助类型；仅按当前签名使用'
PT=[]
for t in T:
    topic,role,reason=classify(t)
    doc='annotations/index.md' if topic=='annotations' else f'references/{topic}.md'
    PT.append(dict(name=t['name'],module=t['module'],source=t['path'],line=t['line'],role=role,reason=reason,document=doc,methods=sorted(set(m['name'] for m in t['methods']))))
save('audit/public-types.json',PT)

# Snapshot predecessors BEFORE any rewrite. Only prose counts as prose coverage.
if not (S/'audit/predecessors.json').exists():
    stats={}
    for label,base in [('rejected-current',S),('legacy-10mb',R/'skills/mongoplus')]:
        md=[p for p in base.rglob('*.md') if 'audit' not in p.relative_to(base).parts and 'validation' not in p.relative_to(base).parts]
        prose='\n'.join(p.read_text(encoding='utf-8-sig') for p in md)
        annotations=sorted({a['simpleName'] for a in A if re.search(r'@'+a['simpleName']+r'\b',prose)})
        stats[label]={'path':str(base.relative_to(R)),'files':len(list(base.rglob('*'))),'bytes':sum(p.stat().st_size for p in base.rglob('*') if p.is_file()),'markdownFiles':[str(p.relative_to(base)).replace('\\','/') for p in md],'annotationMentions':annotations,'publicTypeMentions':sorted({t['name'] for t in T if re.search(r'\b'+re.escape(t['simpleName'])+r'\b',prose)}),'note':'Mentions only, NOT documentation/contract coverage. JSONL/source dumps excluded from prose.'}
    save('audit/predecessors.json',stats)

ns={'m':'http://maven.apache.org/POM/4.0.0'}
pom=ET.parse(R/'pom.xml').getroot()
reactor=[e.text for e in pom.findall('m:modules/m:module',ns)]
modules=[]
for p in sorted([*R.glob('mongo-plus-*/pom.xml'),R/'tools/mongoplus-api-indexer/pom.xml']):
    root=ET.parse(p).getroot();n=p.parent.relative_to(R).as_posix()
    modules.append({'module':n,'reactor':n in reactor,'packaging':root.findtext('m:packaging','jar',ns),'mainFiles':sum(x['path'].startswith(n+'/') for x in D['files']),'publicTypes':sum(t['module']==n for t in T),'testJavaFiles':len(list((p.parent/'src/test').rglob('*.java')))})
save('audit/modules.json',modules)
save('audit/provenance.json',{'head':HEAD,'sourceVersion':pom.findtext('m:version',namespaces=ns),'date':'2026-11-11','reactor':reactor,'sourceFiles':len(D['files']),'publicTypeDeclarations':len(T),'annotationDeclarations':len(A),'uniqueAnnotationNames':len({a['name'] for a in A}),'capabilities':len(CAP),'testSourceFiles':sum(m['testJavaFiles'] for m in modules),'oldIndex':'generated/api-index.jsonl and capability-index.jsonl read-only, not current authority','phase':'INVENTORY_COMPLETE_BEFORE_TOPIC_AUTHORING'})
write('audit/annotation-matrix.md','# 注解覆盖矩阵（源码盘点先行）\n\n'+f'基线 {HEAD}。{len(A)} 个声明，{len({a["name"] for a in A})} 个不同完整类名；MongoMapperScan 在 Boot3/4 各有一份。\n\n| 注解 | 源码位置 | 文档位置 | 示例 | 状态 |\n|---|---|---|---|---|\n'+''.join(f'| `{a["name"]}` ({a["module"]}) | [{a["source"]}:{a["line"]}]({source(next(t for t in T if t["path"]==a["source"]))}) | [记录](../{a["document"]}) | [编译夹具](../{a["example"]}) | {a["status"]} |\n' for a in A))
write('capabilities.md','# 完整能力盘点\n\n每行是独立业务能力，不以方法数量充当覆盖率。文档登记与运行验证分别计数。逐声明角色见 [公开类型矩阵](audit/public-types.md)，验证状态见 [验收报告](audit/coverage-report.md)。\n\n| 编号 / 功能 | 模块 | 核心公开 API | 注解 | 场景 | 配置或注册 | 限制 | 专题 | 验证 |\n|---|---|---|---|---|---|---|---|---|\n'+''.join(f'| {c["id"]} {c["name"]} | {", ".join(c["modules"]) or "Driver/BOM边界"} | `{"`, `".join(c["apis"])}` | {", ".join("@"+a for a in c["annotations"]) or "无专用注解"} | {c["scenario"]} | {c["registration"]} | {c["limitations"]} | [专题]({c["document"]}) | {c["status"]} |\n' for c in CAP))
write('audit/feature-matrix.md','# 功能覆盖矩阵\n\n判定口径：SOURCE_VERIFIED 表示当前源码可定位；runtime UNVERIFIED 不等于通过。完整场景/注册/限制见 [capabilities](../capabilities.md)。\n\n| 功能 | 主要公开 API | 文档位置 | 验证状态 |\n|---|---|---|---|\n'+''.join(f'| {c["id"]} {c["name"]} | `{"`, `".join(c["apis"])}` | [专题](../{c["document"]}) | {c["status"]} |\n' for c in CAP))
write('audit/public-types.md','# 全量公开类型对应关系\n\n逐个 public 声明核对；同名跨模块不合并。INTERNAL_PUBLIC 的排除仅指不提供业务教程，源码位置和角色仍保留。方法列表用于追踪，不代表方法逐个已经运行。\n\n| 公开类型 / 模块 | 源码 | 角色与边界理由 | 对应文档 |\n|---|---|---|---|\n'+''.join(f'| `{t["name"]}` / {t["module"]} | [{t["source"]}:{t["line"]}](https://github.com/anwenmp/MongoPlus/blob/{HEAD}/{t["source"]}#L{t["line"]}) | {t["role"]}: {t["reason"]} | [专题](../{t["document"]}) |\n' for t in PT))
print(json.dumps({'sourceFiles':len(D['files']),'types':len(T),'annotations':len(A),'uniqueAnnotations':len({a['name'] for a in A}),'capabilities':len(CAP),'modules':len(modules)},ensure_ascii=False))
