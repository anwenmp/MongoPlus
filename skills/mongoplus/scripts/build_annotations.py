"""Generate complete annotation records from current AST, with reviewed usage metadata."""
from build_inventory import S, R, T, A, HEAD, write, source
import re
def unescape(s):return re.sub(r'\\u([0-9a-fA-F]{4})',lambda m:chr(int(m[1],16)),s)
def clean(s):
    s=unescape(s or '')
    # Inline Javadoc may contain JSON with nested braces; preserve balanced content.
    while (match:=re.search(r'\{@(?:link|code|literal)\s+',s)):
        begin=match.start();pos=match.end();depth=1;end=pos
        while end<len(s) and depth:
            if s[end]=='{':depth+=1
            elif s[end]=='}':depth-=1
            end+=1
        inner=s[pos:end-1] if depth==0 else s[pos:]
        s=s[:begin]+'`'+inner+'`'+s[end:]
    s=re.sub(r'<[^>]+>',' ',s)
    s=re.split(r'\n\s*@(?:author|since|see|return)',s)[0]
    return re.sub(r'\s+',' ',s).strip().replace('|','\\|')
# purpose, activation, pitfalls, minimal declaration. These are source descriptions, not runtime assertions.
M={
'ID':('映射MongoDB _id并选择生成策略；saveField控制是否同时保留原属性名','通过MongoConverter实体写入；自定义策略注册IdGenerateHandler/IdentifierGenerator','默认OBJECT_ID；String/ObjectId/数值策略与Java字段类型必须相容；不把业务ID都当ObjectId','class IDExample { @ID String id; }'),
'CollectionName':('指定物理集合及可选database','实体访问或扫描元数据；Core也可显式选择namespace','value无默认值；database空值走当前配置；动态集合另由插件处理','@CollectionName("orders") class CollectionNameExample { String title; }'),
'CollectionField':('字段重命名、排除、填充、TypeHandler、null和ObjectId控制','实体Converter；fill还需要MetaObjectHandler，typeHandler需实现TypeHandler','当前只有6个属性，无convertCollect；wrapper-only更新不等于实体写转换','class CollectionFieldExample { @CollectionField(value="display_name", ignoreNull=false) String name; }'),
'CollectionLogic':('逻辑删除字段及未删除/已删除值类型','启用逻辑删除配置，并登记实体、普通/高级逻辑删除拦截器','logice包拼写勿改；注解本身不注册插件；物理删除与忽略上下文需独立核对','class CollectionLogicExample { @CollectionLogic(value="0", delval="1", delType=LogicDataType.INTEGER) Integer deleted; }'),
'Version':('标记实体整数版本字段，供乐观锁拦截器解析','注册OptimisticLockerInterceptor，走可识别实体更新入口','仅支持整数；空版本、自动递增、失败异常与重试配置分别核对；原生BSON和wrapper-only不自动获得全部契约','class VersionExample { @Version Integer version; }'),
'DBRef':('将关联对象写为Driver DBRef并在读取时查询引用对象','默认DBRefHandler参与写、读、条件链；被引用类型必须有@ID与可解析集合','不是自动级联保存、join或外键；可能额外查询；循环、缺引用、集合引用运行契约UNVERIFIED','class DBRefExample { @DBRef Target customer; @CollectionName("customers") public static class Target { @ID public String id; } }'),
'ExtraFields':('将文档中未声明属性收集到Map，支持忽略列表及名称转换','使用实体读取Converter及ExtraFieldsHolder','Map实现须可实例化；不是任意字段自动持久化契约；忽略_id及往返行为需实测','class ExtraFieldsExample { @ExtraFields java.util.Map<String,Object> extra; }'),
'FieldBind':('读取源字段后回调字典处理器，为target字段赋值','显式注册new FieldBindHandler(DataBindHandler)到ReadHandler链；Boot3可收集ReadHandler Bean','type是业务字典标识；目标找不到会异常；默认命名配置下target="statusName"实测抛出target field not found，命名契约标CONTRACT_UNRESOLVED，示例使用已实测的label；Boot4需显式注册ReadHandler','class FieldBindExample { @FieldBind(type="status", target="label") String status; @CollectionField(exist=false) String label; }'),
'EnumValue':('选择枚举持久化字段，valueStore控制存值或名称','枚举MappingStrategy与ConversionStrategy配合实体转换','标在枚举中的值字段；数据库值与枚举字段类型应一致；未知值行为需测试','enum EnumValueExample { OPEN(1); @EnumValue final int code; EnumValueExample(int code){this.code=code;} }'),
'TimeSeries':('声明时序集合创建选项','启用auto-create-time-series并扫描到实体，或调用AutoUtil.autoCreateTimeSeries','timeField必填且应为BSON Date；bucket参数单位秒；已有普通集合不自动迁移；创建捕获MongoCommandException不能当作成功','@CollectionName("measurements") @TimeSeries(timeField="time", metaField="sensor") class TimeSeriesExample { java.util.Date time; String sensor; }'),
'FieldEncrypt':('实体字段写入加密、读取按findDecrypt解密','默认EncryptFieldHandler/FieldEncryptApply；配置相应key/privateKey/publicKey或自定义Encryptor','默认BASE64是编码；MD5/SM3不可逆；查询条件转换与实体/原始BSON路径不同；密钥不写进Skill','class FieldEncryptExample { @FieldEncrypt(algorithm=AlgorithmEnum.AES, key="0123456789abcdef") String secret; }'),
'Desensitization':('读取字段后按类型、区间或定制Handler脱敏','默认读取处理器；自定义类需实现DesensitizationHandler','不是写入加密；默认CUSTOM的0..0不是有效业务脱敏设计；脱敏值回写会丢信息','class DesensitizationExample { @Desensitization(type=DesensitizationTypeEnum.MOBILE_PHONE) String phone; }'),
'NonNull':('JSR-305非空类型提示','需编译期JSR-305元注解依赖；由工具解释','无运行时必填校验器；不能替代业务参数验证','class NonNullExample { @NonNull String value; }'),
'Nullable':('JSR-305可空类型提示','需JSR-305依赖；工具解释When.MAYBE','不会使持久化自动接受或保留null；null写策略另查CollectionField','class NullableExample { @Nullable String value; }'),
'SensitiveWord':('LOCAL模式下标记需要敏感词处理的字段','引入mongo-plus-sensitive-word，配置LOCAL并初始化SensitiveWordProperty与Manager/HandlerRegistry','只有注解不生效；GLOBAL走拦截器；replace/exception/保留结果策略单独配置','class SensitiveWordExample { @SensitiveWord String body; }'),
'MongoDs':('选择预先注册的命名数据源，可交由DataSourceHandler','Boot切面或手工DataSourceManager；Solon不能仅因Aspect存在就认定绑定','value必填；默认master；方法/类、嵌套、自调用、异步线程与事务同源需验证','@MongoDs("archive") class MongoDsExample { void query(){} }'),
'MongoTransactional':('声明MongoDB事务及session/事务选项与回滚规则','Boot事务切面和MongoPlusTransactionalManager，或Solon已绑定拦截器；MongoDB支持事务','仅方法；代理自调用边界；rollbackFor/noRollbackFor非Java异常名称匹配；多client无分布式原子保证','class MongoTransactionalExample { @MongoTransactional(rollbackFor=Exception.class) public void saveBoth(){} }'),
'MongoReadPreference':('配置事务读取偏好，供MongoTransactional.preference消费','当前MongoTransactionalManager读取preference数组第一项','虽然Target是METHOD，未发现独立方法切面；不提供单独标注即可路由读请求的示例；多项不表示多个偏好同时生效','class MongoReadPreferenceExample { @MongoTransactional(preference={@MongoReadPreference(preferenceEnum=ReadPreferenceEnum.PRIMARY)}) public void save(){} }'),
'IgnoreTenant':('在方法代理边界暂时忽略租户过滤','已注册TenantInterceptor及相应容器TenantAspect','仅METHOD，不支持类；仅可信内部方法使用；嵌套上下文与异步恢复需验证，Solon额外核对绑定','class IgnoreTenantExample { @IgnoreTenant public void maintenance(){} }'),
'IgnoreLogic':('方法或类型上请求忽略逻辑删除条件','配置逻辑删除并使相应AOP切面生效','包名annotation.logice；各执行分支是否消费忽略标志需核验；不保证所有update/bulk路径相同','@IgnoreLogic class IgnoreLogicExample { public void maintenance(){} }'),
'Mongo':('标记需创建MongoMapper代理的接口','Boot扫描/工厂，Solon带@Inject接口注入','不是Spring Data @Document或MyBatis @Mapper；继承MongoMapper<T>并正确解析泛型','@Mongo interface MongoExample extends com.mongoplus.mapper.MongoMapper<MongoExample.Entity> { class Entity {} }'),
'MongoMapperScan':('为Boot应用扫描MongoMapper接口并注册FactoryBean','引入对应Boot3或Boot4 Starter，配置扫描包','两个模块提供同FQCN，不能同时依赖；Solon无此扫描注解契约','@MongoMapperScan("example.mapper") class MongoMapperScanExample {}'),
'MongoIndex':('字段普通索引，支持唯一、稀疏、TTL、partial及方向','auto-create-index与实体扫描，或手工Index API','expireAfterSeconds=-1表示未设置；expireAfter表达式需核实解析；TTL要核对实际日期类型及异步过期','class MongoIndexExample { @MongoIndex(name="email_unique", unique=true) String email; }'),
'MongoCompoundIndex':('以JSON键定义复合索引及选项，可重复','实体扫描与自动索引，或Resolver生成模型后创建','value必填；JSON顺序影响键顺序；不要推断lambda字段映射规则',r'@MongoCompoundIndex(value="{\"tenant\":1,\"created_at\":-1}", name="tenant_time") class MongoCompoundIndexExample {}'),
'MongoCompoundIndexes':('复合索引可重复容器','与MongoCompoundIndex相同','容器不是新索引类型；每个子项仍需合法键定义',r'@MongoCompoundIndexes({@MongoCompoundIndex("{\"a\":1}"),@MongoCompoundIndex("{\"b\":1}")}) class MongoCompoundIndexesExample {}'),
'MongoGeoIndex':('地理字段索引','扫描与GeoParser；type必须显式选择','geo查询要求正确坐标类型；二维与球面索引不能互换','class MongoGeoIndexExample { @MongoGeoIndex(type=GeoType.SPHERE) Object location; }'),
'MongoHashIndex':('字段哈希索引','扫描与HashParser','不是加密字段；不把hash索引当作普通范围排序索引','class MongoHashIndexExample { @MongoHashIndex String key; }'),
'MongoTextIndex':('类级传统text索引','扫描与TextParser；fields必填','不是Search；language和textIndexVersion受服务端限制；字段名映射需对照模型','@MongoTextIndex(fields={"title","body"}, name="article_text") class MongoTextIndexExample {}'),
'MongoIndexDs':('指定自动索引目标数据源','实体扫描且目标数据源已注册','仅索引DDL目标；不替代MongoDs请求路由','@MongoIndexDs(dataSource="archive") class MongoIndexDsExample {}'),
'MongoSearchIndex':('普通Search索引配置，可多次声明','Resolver/SearchParse及自动索引扫描；支持Search的部署','默认dynamic=false；静态字段或dynamic需明确；名称与Vector共享同一集合命名空间；创建是异步','@MongoSearchIndex(name="articles", fields={@MongoSearchField(fieldName="title")}) class MongoSearchIndexExample {}'),
'MongoSearchIndexes':('多Search索引容器','与MongoSearchIndex相同','索引名不能重复；隔离字段/分析器/排序等各自配置','@MongoSearchIndexes({@MongoSearchIndex(name="a",dynamic=true),@MongoSearchIndex(name="b",dynamic=true)}) class MongoSearchIndexesExample {}'),
'MongoSearchField':('Search静态字段映射及类型专属选项，可重复','SearchParse读取字段或MongoSearchIndex.fields','默认STRING不按Java类型推断；多索引需name；嵌套用fields JSON，不把点路径当嵌套映射；dimensions已弃用，见完整属性表','@MongoSearchIndex(name="articles") class MongoSearchFieldExample { @MongoSearchField String title; }'),
'MongoSearchFields':('多Search字段映射容器','与MongoSearchField相同','同一字段可多类型；排除映射不能与其他类型混用；读取需getAnnotationsByType','@MongoSearchIndex(name="articles") class MongoSearchFieldsExample { @MongoSearchFields({@MongoSearchField(type=SearchFieldType.STRING),@MongoSearchField(type=SearchFieldType.TOKEN)}) String title; }'),
'MongoSearchSort':('Search索引预排序字段，可重复','嵌入MongoSearchIndex.sort或字段；按name归属','不替代静态字段映射；重复路径与priority顺序需核对；高级功能依部署','@MongoSearchIndex(name="articles",fields={@MongoSearchField(fieldName="title",type=SearchFieldType.TOKEN)},sort={@MongoSearchSort(fieldName="title")}) class MongoSearchSortExample {}'),
'MongoSearchSorts':('多Search预排序配置容器','字段配置按索引名分组','同一索引不允许重复排序同一路径','@MongoSearchIndexes({@MongoSearchIndex(name="a",fields={@MongoSearchField(fieldName="title",type=SearchFieldType.TOKEN)}),@MongoSearchIndex(name="b",fields={@MongoSearchField(fieldName="title",type=SearchFieldType.TOKEN)})}) class MongoSearchSortsExample { @MongoSearchSorts({@MongoSearchSort(name="a"),@MongoSearchSort(name="b")}) String title; }'),
'MongoSearchStoredSource':('Search返回源字段存储配置','仅嵌入MongoSearchIndex.storedSource','@Target({})不能直接标类/字段；全量value和include/exclude互斥；不代替字段索引','@MongoSearchIndex(dynamic=true,storedSource=@MongoSearchStoredSource(include={"title"})) class MongoSearchStoredSourceExample {}'),
'MongoSearchSynonym':('引用已有同义词来源集合','仅嵌入MongoSearchIndex.synonyms；来源集合由业务准备','@Target({})；不会写入同义词文档；分析器及部署档位另确认','@MongoSearchIndex(dynamic=true,synonyms={@MongoSearchSynonym(name="terms",sourceCollection="synonyms")}) class MongoSearchSynonymExample {}'),
'MongoVectorIndex':('独立Vector Search索引，type=vectorSearch','VectorParse/Resolver及自动扫描；部署支持Vector Search','至少一个vector字段；不同于Search中的vector映射；nestedRoot与storedSource支持看目标部署','@MongoVectorIndex(name="vec",fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)}) class MongoVectorIndexExample {}'),
'MongoVectorIndexes':('多独立Vector Search索引容器','与MongoVectorIndex相同','Search和Vector索引同名会冲突；每个子索引须有向量字段','@MongoVectorIndexes({@MongoVectorIndex(name="a",fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)})}) class MongoVectorIndexesExample {}'),
'MongoVectorField':('声明独立vector或filter路径，可重复','字段配置或MongoVectorIndex.fields；多索引用name指定','vector维度与相似度必填；filter不能含向量选项；$属性路径可映射，裸路径是MongoDB字段','@MongoVectorIndex(name="vec") class MongoVectorFieldExample { @MongoVectorField(numDimensions=3,similarity=SearchVectorSimilarity.COSINE) java.util.List<Double> embedding; }'),
'MongoVectorFields':('多个Vector字段归属容器','同MongoVectorField','不是一个索引内重复同一路径；适于同字段归属多个不同索引','@MongoVectorIndexes({@MongoVectorIndex(name="a"),@MongoVectorIndex(name="b")}) class MongoVectorFieldsExample { @MongoVectorFields({@MongoVectorField(name="a",numDimensions=3,similarity=SearchVectorSimilarity.COSINE),@MongoVectorField(name="b",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)}) java.util.List<Double> embedding; }'),
'MongoVectorHnswOptions':('HNSW图构建可选参数','仅嵌入MongoVectorField.hnswOptions','@Target({})；-1省略；flat与HNSW选项互斥；范围检查不证明服务端支持','@MongoVectorIndex(fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE,hnswOptions=@MongoVectorHnswOptions(maxEdges=32,numEdgeCandidates=200))}) class MongoVectorHnswOptionsExample {}'),
'MongoVectorStoredSource':('独立Vector Search源字段存储配置','仅嵌入MongoVectorIndex.storedSource','@Target({})；include与exclude互斥；不是filter字段定义','@MongoVectorIndex(fields={@MongoVectorField(fieldName="embedding",numDimensions=3,similarity=SearchVectorSimilarity.COSINE)},storedSource=@MongoVectorStoredSource(include={"title"})) class MongoVectorStoredSourceExample {}'),
}
TOP={'mapping':'mapping','field-processing':'field-security','routing-isolation':'transactions-routing','indexes':'indexes','search':'search-vector','vector':'search-vector','integration':'integration'}
GROUP={}
all_imports=set();all_classes=[]
for name in sorted({a['simpleName'] for a in A}):
    t=next(t for t in T if t['simpleName']==name and t['kind']=='ANNOTATION_TYPE')
    purpose,activation,pitfalls,decl=M[name]
    # Entity metadata may instantiate the class; public classes have public implicit no-arg constructors.
    decl=re.sub(r'(?<!static )(?<!public )\b(class|interface|enum) '+name+r'Example\b',r'public \1 '+name+'Example',decl,count=1)
    if '.annotation.index.' in t['name']:
        decl='@CollectionName("example_'+name.lower()+'") '+decl
    # All referenced annotation imports are explicit; enums are resolved from current definitions.
    tokens=set(re.findall(r'@([A-Z]\w*)',decl)); imports=set()
    for token in tokens:
        imports.add(next(x['name'] for x in T if x['simpleName']==token and x['kind']=='ANNOTATION_TYPE'))
    for token in set(re.findall(r'\b([A-Z]\w*)\.',decl)):
        hits=[x for x in T if x['simpleName']==token and x['kind']=='ENUM']
        if hits:imports.add(hits[0]['name'])
    code='\n'.join('import '+x+';' for x in sorted(imports))+'\n\n'+decl+'\n'
    all_imports.update(imports);all_classes.append(name+'Example')
    write(f'validation/examples/annotations/{name}Example.java',code)
    group=next(a['document'].split('/')[1].split('.')[0] for a in A if a['simpleName']==name)
    declarations=[x for x in T if x['name']==t['name'] and x['kind']=='ANNOTATION_TYPE']
    attrs='| 属性 | 类型 | 默认值 | 源码注释摘要（不是运行证明） |\n|---|---|---|---|\n'
    for m in t['methods']:
        description=clean(m['javadoc']) if m['javadoc']!='null' else '标记属性'
        if name=='TimeSeries' and m['name'] in ['timeField','metaField']:
            description='实际字段名；以$开头时经实体属性核对/映射。注解应写timeField/metaField成员，不存在value成员。源码旧注释的@TimeSeries("...")简写无效。'
        attrs+=f'| `{m["name"]}` | `{m["return"]}` | `{m["default"] if m["default"] is not None else "必填（无默认值）"}` | {description} |\n'
    if not t['methods']:attrs='无成员属性（标记注解）。\n'
    target=next((x for x in t['annotations'] if x.startswith('@Target')),'未声明Target')
    repeat=next((x for x in t['annotations'] if x.startswith('@Repeatable')),'不可重复（未声明Repeatable）')
    sec=f'\n## {name}\n\n`import {t["name"]};`\n\n- 作用域：`{target}`；保留策略：RUNTIME；{repeat}。\n- 实际用途：{purpose}。\n- 生效条件：{activation}。\n- 限制与常见错误：{pitfalls}。\n- 状态：`SOURCE_VERIFIED`；下面是可编译的声明示例，运行验证按[报告](../audit/coverage-report.md)逐项记录，未执行部分为 `UNVERIFIED / NOT_RUN`。\n\n{attrs}\n```java\n{code}```\n\n依据：'+ '；'.join(f'[{x["module"]}源码]({source(x)})' for x in declarations)+f'。后续实现与注册见[专题](../references/{TOP[group]}.md)。\n'
    GROUP.setdefault(group,[]).append(sec)
write('validation/examples/AnnotationExamples.java','/** Names of independently compiled public annotation fixtures. */\npublic final class AnnotationExamples { public static final String[] TYPES = {'+','.join('"'+x+'"' for x in all_classes)+'}; }\n')
for group,sections in GROUP.items():
    write(f'annotations/{group}.md',f'# 注解：{group}\n\n[完整目录](index.md)。各属性来自本次JDK AST解析，`必填`表示没有Java默认值。源码说明与运行状态分开记录。\n\n本页：'+ '、'.join(re.search(r'## (\w+)',s)[1] for s in sections)+'。\n'+''.join(sections))
write('annotations/index.md','# 全部公开注解目录\n\n当前源码定义 **43种完整类名、44处声明**；两套Starter的MongoMapperScan同名。全部盘点，无遗漏、无按业务频率排除项。容器和`@Target({})`嵌套配置也列入，不能直接标类/字段的限制逐项记录。完整可核对矩阵见[注解覆盖审计](../audit/annotation-matrix.md)。\n\n| 注解 | 完整import | 模块 | 记录 | 示例 |\n|---|---|---|---|---|\n'+''.join(f'| @{name} | `{next(a["name"] for a in A if a["simpleName"]==name)}` | {", ".join(a["module"] for a in A if a["simpleName"]==name)} | [属性/注册/限制]({next(a["document"].split("/",1)[1] for a in A if a["simpleName"]==name)}) | 同一记录中的最小声明；[编译源](../validation/examples/AnnotationExamples.java) |\n' for name in sorted(M))+'\n特别状态：`MongoSearchField.dimensions`已弃用；`MongoReadPreference`未确认独立方法注解的消费契约，确认消费位置为事务注解的preference；`NonNull/Nullable`是类型提示，不执行非空校验。\n')
print('Generated records for',len(M),'unique annotations')
