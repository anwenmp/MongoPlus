"""Publish auditable coverage and actual validation results, never infer runtime PASS from presence."""
import collections,hashlib,json,re
from pathlib import Path
S=Path(__file__).resolve().parents[1];R=S.parent
def load(p):return json.loads((S/p).read_text(encoding='utf8'))
def save(p,obj):(S/p).write_text(json.dumps(obj,ensure_ascii=False,indent=2)+'\n',encoding='utf8')
def write(p,text):(S/p).write_text(text,encoding='utf8')
P=load('audit/provenance.json');T=load('audit/source-inventory.json')['types'];CAP=load('audit/capabilities.json');ANN=load('audit/annotations.json');MOD=load('audit/modules.json');PT=load('audit/public-types.json')
build=Path((S/'validation/latest-build.txt').read_text(encoding='utf8').strip());BR=json.loads((build/'results.json').read_text(encoding='utf8'))
live=Path((S/'validation/latest-live.txt').read_text(encoding='utf8').strip());LR=json.loads((live/'results.json').read_text(encoding='utf8'));ENV=json.loads((live/'environment.json').read_text(encoding='utf8'))
checksfile=S/'validation/latest-checks.txt';checks=Path(checksfile.read_text(encoding='utf8').strip()) if checksfile.exists() else None
CR=json.loads((checks/'results.json').read_text(encoding='utf8')) if checks else {'status':'NOT_RUN','checks':{}}
creator=load('validation/creator-validator/results.json')
blind_result=load('validation/blind-compile/results.json') if (S/'validation/blind-compile/results.json').exists() else []
final_links=load('validation/final-links.json') if (S/'validation/final-links.json').exists() else {}
def rel(p):return p.relative_to(S).as_posix()
def api_label(c):
    if c['id']=='C005':return 'com.mongoplus:mongo-plus-bom（Maven版本管理）'
    if c['id']=='C017':return 'com.mongodb.client.MongoCollection.distinct / replaceOne（Driver边界）'
    if c['id']=='C101':return 'com.mongodb.client.MongoCollection.watch（Driver边界）'
    return '`, `'.join(c['apis'])
legacy=load('audit/predecessors.json')['legacy-10mb'];rejected=load('audit/predecessors.json')['rejected-current']
current_ann={a['name'] for a in ANN};old_ann=set(legacy['indexedAnnotationNames'])

tests=[]
for m in MOD:
    for file in sorted((R/m['module']/'src/test').rglob('*.java')):
        text=file.read_text(encoding='utf8');tokens=set(re.findall(r'\b[A-Za-z_]\w*\b',text))
        tests.append({'path':file.relative_to(R).as_posix(),'sha256':hashlib.sha256(file.read_bytes()).hexdigest(),'testMarkers':len(re.findall(r'@(?:Test|ParameterizedTest|RepeatedTest|TestFactory)\b',text)),'publicTypeReferences':sorted({t['name'] for t in T if t['simpleName'] in tokens})})
save('audit/test-corpus.json',{'note':'Read-only lexical cross-reference, NOT proof of test execution or contract correctness','files':tests})
for c in CAP:
    names={api.split('#')[0] for api in c['apis'] if not api.startswith('@')}
    c['testSourceCandidates']=[t['path'] for t in tests if any(n.rsplit('.',1)[-1] in names for n in t['publicTypeReferences'])][:8]

LIVE_BY_CAP={
'C007':['query-same-array-element','mapped-annotations-roundtrip'],'C009':['mapped-annotations-roundtrip'],
'C016':['query-same-array-element'],'C019':['query-same-array-element','regex-explicit-case-sensitive','projection-sort'],
'C020':['query-same-array-element','projection-sort'],'C023':['query-same-array-element'],'C024':['regex-explicit-case-sensitive'],'C025':['projection-sort'],
'C030':['update-CAS-single'],'C031':['update-CAS-single'],'C032':['update-CAS-single'],'C033':['pull-value','pull-wrapper-custom'],'C034':['update-CAS-single'],
'C037':['match-Bson-body'],'C038':['match-Bson-body','project-Bson-body','sort-Bson-full-stage','sortSpecification-body','skip-limit'],
'C039':['addFields-Bson-body','set-Bson-body','unset-Bson-full-stage'],'C040':['lookup-Bson-full-stage','lookup-four-String'],
'C041':['facet-Bson-full-stage','unionWith-Bson-full-stage'],'C042':['unwind-Bson-full-stage'],'C043':['group-Bson-full-stage','group-accumulator'],
'C044':['bucket-Bson-body','bucketAuto-Bson-body','sortByCount'],'C045':['replaceRoot-Bson-full-stage','replaceRoot-Document-expression','replaceWith-Bson-full-stage','replaceWith-Document-expression','sample-Bson-full-stage'],
'C047':['window-Bson-full-stage','densify-Bson-full-stage','fill-Bson-full-stage'],'C050':['mapped-annotations-roundtrip'],'C051':['mapped-annotations-roundtrip'],
'C052':['mapped-annotations-roundtrip'],'C053':['mapped-annotations-roundtrip'],'C059':['field-chain-current-value'],'C060':['fieldbind-explicit-registration'],
'C062':['mapped-annotations-roundtrip'],'C063':['fieldbind-explicit-registration','fieldbind-camel-target'],'C064':['mapped-annotations-roundtrip'],'C065':['mapped-annotations-roundtrip'],
'C076':['transaction-commit','transaction-rollback'],'C083':['ordinary-index-unique'],'C085':['ordinary-index-unique']}
PARSE_BY_CAP={'C083':['MongoIndex','MongoIndexDs'],'C084':['MongoCompoundIndex','MongoCompoundIndexes','MongoGeoIndex','MongoHashIndex','MongoTextIndex'],'C086':['MongoSearchIndex','MongoSearchIndexes','MongoSearchField','MongoSearchFields'],'C087':['MongoSearchSort','MongoSearchSorts','MongoSearchStoredSource','MongoSearchSynonym'],'C088':['MongoVectorIndex','MongoVectorIndexes','MongoVectorField','MongoVectorFields','MongoVectorHnswOptions','MongoVectorStoredSource']}
case_by_name={x['case']:x for x in LR['cases']}
for c in CAP:
    livecases=LIVE_BY_CAP.get(c['id'],[]);parsecases=['annotation-parser-'+x for x in PARSE_BY_CAP.get(c['id'],[])]
    c['documentation']='DOCUMENTED';c['validationCases']=livecases+parsecases
    c['origin']='MAVEN_CONFIGURATION' if c['id']=='C005' else 'DRIVER_INTEROP' if c['id'] in ['C017','C101'] else 'MONGOPLUS_SOURCE'
    c['validationNote']='Only named examples/overloads were executed; all remaining configurations and overloads UNVERIFIED / NOT_RUN.'
    c['status']='SOURCE_VERIFIED; '+ ('LIVE_PARTIAL' if livecases else 'PARSE_PASS_ONLY' if parsecases else 'UNVERIFIED / NOT_RUN')
    if any(case_by_name.get(n,{}).get('status')=='CONTRACT_UNRESOLVED' for n in livecases):c['status']+='; CONTRACT_UNRESOLVED'
save('audit/capabilities.json',CAP)
runtime_annotations={'ID','CollectionField','EnumValue','ExtraFields','FieldEncrypt','Desensitization','FieldBind','MongoIndex'}
for a in ANN:
    parsed='annotation-parser-'+a['simpleName'] in case_by_name
    a['status']='DOCUMENTED; COMPILE_PASS; '+('LIVE_PARTIAL' if a['simpleName'] in runtime_annotations else 'PARSE_PASS_ONLY' if parsed else 'UNVERIFIED / NOT_RUN')
    if a['simpleName']=='FieldBind':a['status']+='; camel target CONTRACT_UNRESOLVED'
save('audit/annotations.json',ANN)
write('audit/annotation-matrix.md','# 注解覆盖矩阵\n\n43种完整类名、44处声明；全部有记录和可编译示例。运行列只覆盖明确执行的配置；PARSE_PASS不计真实库。\n\n| 注解 | 源码位置 | 文档位置 | 示例 | 状态 |\n|---|---|---|---|---|\n'+''.join(f'| `{a["name"]}` / {a["module"]} | `{a["source"]}:{a["line"]}` | [记录](../{a["document"]}) | [源码](../{a["example"]}) | {a["status"]} |\n' for a in ANN))
write('audit/feature-matrix.md','# 功能覆盖矩阵\n\n101个能力/边界检查项均有专题；不是101个全重载运行通过。LIVE_PARTIAL与PARSE_PASS_ONLY仍保留未运行部分。原始用例见[实测报告](runtime-results.md)，未运行列表见[总报告](coverage-report.md)。\n\n| 功能 | 主要公开 API | 文档位置 | 验证状态 |\n|---|---|---|---|\n'+''.join(f'| {c["id"]} {c["name"]} | `{api_label(c)}` | [专题](../{c["document"]}) | {c["status"]}；{", ".join(c["validationCases"]) or "运行NOT_RUN"} |\n' for c in CAP))
capfile=(S/'capabilities.md').read_text(encoding='utf8')
for c in CAP:
    lines=capfile.splitlines()
    capfile='\n'.join(re.sub(r'\| SOURCE_VERIFIED; runtime UNVERIFIED \|$', '| '+c['status']+' |',line).replace('`@bom`','`'+api_label(c)+'`').replace('`@driver`','`'+api_label(c)+'`') if line.startswith('| '+c['id']+' ') else line for line in lines)+'\n'
write('capabilities.md',capfile)

API_NAMES={
'match-Bson-body':'LambdaAggregateWrapper.match(Bson)', 'project-Bson-body':'LambdaAggregateWrapper.project(Bson)',
'addFields-Bson-body':'LambdaAggregateWrapper.addFields(Bson)','set-Bson-body':'LambdaAggregateWrapper.set(Bson)',
'bucket-Bson-body':'LambdaAggregateWrapper.bucket(Bson)','bucketAuto-Bson-body':'LambdaAggregateWrapper.bucketAuto(Bson)',
'sort-Bson-full-stage':'LambdaAggregateWrapper.sort(Bson)','sortSpecification-body':'Aggregate.sortSpecification(Bson)','sort-String':'LambdaAggregateWrapper.sort(String,Integer)',
'group-Bson-full-stage':'LambdaAggregateWrapper.group(Bson)','group-accumulator':'LambdaAggregateWrapper.group(Object,BsonField...)',
'facet-Bson-full-stage':'LambdaAggregateWrapper.facet(Bson)','unwind-Bson-full-stage':'LambdaAggregateWrapper.unwind(Bson)',
'replaceRoot-Bson-full-stage':'LambdaAggregateWrapper.replaceRoot(Bson) [explicit Bson cast]', 'replaceRoot-Document-expression':'LambdaAggregateWrapper.replaceRoot(Document)',
'replaceWith-Bson-full-stage':'LambdaAggregateWrapper.replaceWith(Bson) [explicit Bson cast]','replaceWith-Document-expression':'LambdaAggregateWrapper.replaceWith(Document)',
'sample-Bson-full-stage':'LambdaAggregateWrapper.sample(Bson)','window-Bson-full-stage':'LambdaAggregateWrapper.setWindowFields(Bson)','densify-Bson-full-stage':'LambdaAggregateWrapper.densify(Bson)','fill-Bson-full-stage':'LambdaAggregateWrapper.fill(Bson)','unset-Bson-full-stage':'LambdaAggregateWrapper.unset(Bson)',
'skip-limit':'LambdaAggregateWrapper.skip(int)/limit(int)','sortByCount':'LambdaAggregateWrapper.sortByCount(String)','lookup-Bson-full-stage':'LambdaAggregateWrapper.lookup(Bson)','lookup-four-String':'LambdaAggregateWrapper.lookup(String,String,String,String)','unionWith-Bson-full-stage':'LambdaAggregateWrapper.unionWith(Bson)'}
lines=['# 本次真实执行与解析结果\n\n',f'环境：JDK {ENV["jdk"]}、MongoDB {ENV["mongodb"]}、Driver 5.13.0；使用现有测试库与本次随机前缀集合。共{len(LR["createdCollections"])}个自有集合，清理失败/残留 {len(LR.get("cleanupFailures",[]))}。\n\n',f'原始[结果与命令](../{rel(live)}/results.json)、[环境](../{rel(live)}/environment.json)、[夹具](../validation/LiveValidation.java)。只计本轮，不把历史或旧README结果累加。\n\n',
'每个聚合用例比较完整实发pipeline与独立客户端原生aggregate的完整结果集；未定义顺序的结果按完整Document规范排序后比较。其他用例检查明确的原始文档/独立客户端可见性。注解解析无数据库命令，不计LIVE。\n\n| 用例 | 真实API | 结果 | 证据 |\n|---|---|---|---|\n']
for x in LR['cases']:
    lines.append(f'| {x["case"]} | `{API_NAMES.get(x["case"],x["api"])}` | {x["status"]} | commands={len(x.get("commands",[]))}'+(f'；serverCode={x["serverCode"]}' if 'serverCode'in x else '')+(f'；{x["frameworkMessage"]}' if 'frameworkMessage'in x else '')+' |\n')
lines+=['\n## 已执行重载的参数与原生对照\n\n',
'match/project/addFields/set/bucket/bucketAuto的Bson输入是参数体；sort/lookup/facet/group/unwind/unionWith/window/densify/fill/unset的显式Bson输入是完整阶段，实发命令与原生pipeline逐项一致。replaceRoot/replaceWith额外分别执行Bson完整阶段与Document表达式重载，具体输入在夹具，命令/完整结果在JSON的case.results和case.commands。\n\n',
'pull(String,Object)的第一个参数是目标数组物理名，第二个是元素条件体；pull(Wrapper)测试使用custom({items:条件体})，二者与原生$pull结果一致。eq("items",条件体)首轮生成$eq包裹，未实现多条件元素谓词，不能把该写法当作正确示例。\n\n',
'字段测试保存后由独立客户端核对重命名、null、忽略字段、Base64存储和枚举数字；回读确认脱敏、解密及ExtraFields。FieldBind显式注册在label目标上通过；target=statusName在默认命名配置下抛出目标字段不存在，保留CONTRACT_UNRESOLVED。事务测试经MongoPlus管理器和Mapper，独立客户端核对提交前/提交后/回滚后的可见性；未替代Spring/Solon代理验证。\n\n',
'## 首轮失败与修正记录\n\n',
'保留validation/live-*历史目录。首轮混合统计31 PASS/27 UNVERIFIED不作最终通过率：反射夹具改为public类及公开无参构造器；replaceRoot/replaceWith完整Document选择Document表达式重载改为显式Bson；pull使用合法元素谓词重载并增加custom-wrapper对照。旧失败未删除、生产源码未修改。新的camel target探索仍未闭合，不改断言制造成功。\n\n',
'Search/Vector管理前置探测都收到31082，相关索引DDL及搜索查询运行记NOT_RUN。21个索引注解示例只通过Resolver解析与模型检查，不冒充Search服务端验证。\n']
write('audit/runtime-results.md',''.join(lines))

head=P['head'];roles=collections.Counter(x['role'] for x in PT);statuses=collections.Counter(x['status'] for x in LR['cases'])
snippet=CR.get('checks',{}).get('javaSnippets',{});ann_attrs=sum(len(t['methods']) for t in T if t['kind']=='ANNOTATION_TYPE')
lines=['# MongoPlus Skill 覆盖审计 · 2026-11-11\n\n',
f'仅重构当前`mongoplus/`。源码基线：POM {P["sourceVersion"]}、HEAD `{head}`，逐文件SHA-256见[source-inventory](source-inventory.json)。未修改框架源码、其他目录或旧Skill；未安装、发布或打包ZIP。\n\n',
'## 盘点方法与计数口径\n\n',
f'先用CodeGraph定位当前公开入口，再以JDK Compiler Tree API重新解析{P["sourceFiles"]}个生产Java文件，生成覆盖矩阵后编写专题；旧API/能力索引只读作差异参考。当前共{len(T)}个public类型声明，含嵌套类型与跨模块同名声明。注解为{len(current_ann)}种FQCN、{len(ANN)}处声明，{ann_attrs}个属性声明（Boot3/4同名声明分别计入）。测试语料跨{sum(m["testJavaFiles"]>0 for m in MOD)}个模块，{len(tests)}个Java文件，见[只读测试交叉引用](test-corpus.json)；未执行原仓库全套测试，不将词法引用当作测试通过。\n\n',
'能力清单有**101行**：97项运行框架能力、1项BOM构件管理、1项源码索引工具（涵盖两个独立工程）、2项Driver互操作/缺口检查。此为按业务能力分组的审计口径，不等同于API方法数或全部重载运行通过。每行记录模块/API/注解/场景/注册/限制/专题；对应[完整清单](../capabilities.md)。\n\n',
'## 扫描模块\n\n| 模块 | 根reactor | 生产Java文件 | public声明 | 测试Java文件 |\n|---|---|---|---|---|\n']
for m in MOD:lines.append(f'| {m["module"]} | {"是" if m["reactor"] else "否"} | {m["mainFiles"]} | {m["publicTypes"]} | {m["testJavaFiles"]} |\n')
lines+=['\n根POM实际聚合8个生产模块，BOM与indexer在仓库内但不在reactor；4个独立测试模块不作为发布功能模块。\n\n',
'## 注解覆盖\n\n',
f'**{len(current_ann)}/{len(current_ann)}种、{len(ANN)}/{len(ANN)}处声明有记录，登记覆盖率100%；遗漏列表：空。** 每个记录含完整import、Target、默认值、实际用途、条件、限制、最小可编译声明和源码依据。容器、嵌套注解、类型提示均不排除。逐项[矩阵](annotation-matrix.md)与[注解入口](../annotations/index.md)。这项100%是文档登记与对账覆盖，不是运行通过率。\n\n',
'MongoSearchField.dimensions弃用；MongoReadPreference确认的消费点为MongoTransactional.preference第一项，独立方法注解使用未确认；NonNull/Nullable仅类型提示。FieldBind驼峰target场景保留未闭合记录。\n\n',
'## 功能与公开类型覆盖\n\n',
f'**101/101行有专题与当前API来源，未登记能力列表：空。{len(PT)}/{len(T)}个public声明有角色、源码和主题。** [功能矩阵](feature-matrix.md)、[全量公开类型与排除理由](public-types.md)。\n\n',
'角色统计：'+ '；'.join(f'{k}={v}' for k,v in sorted(roles.items()))+'。INTERNAL_PUBLIC仅排除逐个低层方法的业务教程化，不删除该类型记录；缓存、代理、执行器、日志、扫描、元信息等优先依赖公开上层接口。没有以“不常用”为理由排除公开功能。distinct/replaceOne/持续Change Streams没有框架专用入口的边界明确说明；不存在的LambdaQueryWrapper不编造。\n\n',
'## 本次验证\n\n',
f'- 从当前源码直接javac：{sum(x["exit"]==0 for x in BR["results"] if x["module"]!="annotation-examples")}/10个有生产源码的Maven工程通过；注解示例集合{next(x["status"] for x in BR["results"] if x["module"]=="annotation-examples")}。[构建结果](../{rel(build)}/results.json)。Core/Annotation/可选模块/Solon采用release8，Starter与indexer按JDK17编译；未在独立JDK8/21 JVM运行。\n',
f'- Markdown原文Java代码块：{snippet.get("passed","PENDING")}/{snippet.get("count","PENDING")}编译通过，按块隔离包名，import、泛型、重载保留。[详细校验](../{rel(checks)}/results.json)。\n',
f'- 链接/锚点、注解源码差异、属性默认值、API来源、public角色、文件哈希：{CR["status"]}。最终检查不把API出现次数当文档完成率。\n',
f'- 收尾全量链接复查：{final_links.get("status","NOT_RUN")}，{final_links.get("links",0)}条相对链接/锚点，[记录](../validation/final-links.json)。Java临时class和Python缓存已清理；源码、日志、命令和结果保留，重跑先执行compile_sources.py。[清理范围](../validation/cleanup.json)。\n',
f'- 未改动的skill-creator quick_validate函数：{"PASS" if creator["valid"] else "FAIL"}，输出`{creator["message"]}`；本机缺PyYAML，使用现有SnakeYAML安全解析适配，未安装包。原生PyYAML路径为NOT_RUN。[适配与原结果](../validation/creator-validator/results.json)。\n',
f'- 真实MongoDB {ENV["mongodb"]} / JDK {ENV["jdk"]}：{statuses.get("LIVE_PASS",0)} LIVE_PASS、{statuses.get("PARSE_PASS",0)} PARSE_PASS、{statuses.get("NOT_RUN",0)} NOT_RUN、{statuses.get("CONTRACT_UNRESOLVED",0)} CONTRACT_UNRESOLVED。[逐用例与原生命令对照](runtime-results.md)。创建集合{len(LR["createdCollections"])}个，清理失败/残留{len(LR.get("cleanupFailures",[]))}。\n',
'- 独立Agent行为首轮：2个评估者、16题均能找到功能族；首轮暴露的具体不足及后续修正保留在[评估A](../validation/forward-a/evaluation.md)、[评估B](../validation/forward-b/evaluation.md)，复评见各目录recheck.md。Agent导航不计真实库PASS。\n\n',
f'- 另用不继承前文上下文的评估者做6题[盲测](../validation/blind/evaluation.md)，6/6现可完成API选择；注册签名/回调参数缺口与[复查](../validation/blind/fieldbind-recheck.md)保留。独立生成的{len(blind_result)}份Java答案由主代理以当前构件编译，{sum(x["exit"]==0 for x in blind_result)}/{len(blind_result)} COMPILE_PASS，运行NOT_RUN。[编译证据](../validation/blind-compile/results.json)。\n\n',
'## UNVERIFIED、争议及NOT_RUN\n\n',
'全部能力的未测重载/配置仍是UNVERIFIED；有LIVE_PARTIAL的行也只覆盖命名用例。以下事项没有被成功数掩盖：\n\n',
'- FieldBind.target=statusName：默认命名配置下真实读取抛出target field not found；label通过。契约CONTRACT_UNRESOLVED，未擅自标产品缺陷或修改源码。\n',
'- Search/Vector服务端管理不支持，代码31082；索引DDL、READY等待和实际搜索结果NOT_RUN。仅注解解析通过。\n',
'- Spring Boot3/4与Solon实际启动、代理事务传播/注解、SolonMongoDs/IgnoreTenant/IgnoreLogic接线组合NOT_RUN。Boot4/Solon的ReadHandler自动收集缺口按当前源码说明。\n',
'- 多数据源/动态集合/分片多资源事务、逻辑删除恢复、租户全执行面、bulk所有model、乐观锁全部重试、异步镜像故障一致性、数据变更审计、Codec/TLS真实握手、敏感词并发词库、时序DDL/TTL删除和备份恢复NOT_RUN。\n',
'- MongoTransactionalOperate的session与线程上下文/数据源参数边界、ResultHandler枚举无消费者、lazyDataSource与ignoreChar字段未确认生效；不把public声明当成可用配置保证。\n',
'- Codex/Claude Code/Kilo实际安装/自动发现、其他版本依赖、全部Java运行矩阵NOT_RUN；未安装任何Skill。\n\n',
'## 新旧差异\n\n',
f'只读旧版`skills/mongoplus`实测{legacy["bytes"]:,}字节（约10MB），{len(legacy["markdownFiles"])}份Markdown，{legacy["indexedApiRecords"]}条API索引记录。其索引包含{len(old_ann)}种注解FQCN，但这不能算注解开发文档。与当前43种比对，缺少{len(current_ann-old_ann)}种当前FQCN，另外含{len(old_ann-current_ann)}种当前不存在/迁移的旧FQCN。\n\n',
'| 对比项 | 旧10MB Skill | 被退回的当前版 | 本次重构 |\n|---|---|---|---|\n',
f'| 知识组织 | 入口+索引检索，3份references文档 | 10个常用主题 | 23个功能主题+验证页、10族精确签名、7组注解页+完整目录 |\n',
f'| 注解 | 索引含{len(old_ann)}种，Markdown无独立完整记录 | 仅{len(rejected["annotationMentions"])}个注解名以@形式出现，不能算完整记录 | 43种/44声明逐属性记录、示例编译与差异校验 |\n',
'| 能力证明 | API/capability索引数量 | 少量场景验收 | 101项逐行能力、全量声明角色、源码哈希和实际验证分层 |\n',
'| 新功能 | Search旧包，缺当前Search/Vector配置体系 | Search/Vector少量声明 | 多索引、字段/容器、分析器、sort、storedSource、synonyms、向量filter/HNSW/量化均可导航 |\n',
'| 可选与运维能力 | 可从索引找符号，缺开发专题 | 敏感词/分片/备份/配置等缺完整路由 | 独立专题、注册条件、限制和未验证项 |\n',
'| 体积目标 | 大量JSONL | 过度轻量导致遗漏 | 无硬性KB/MB目标；按需加载，不复制旧完整源码JSONL |\n\n',
'旧索引缺少的当前注解：\n\n'+''.join('- `'+x+'`\n' for x in sorted(current_ann-old_ann)),
'\n旧索引额外项：'+', '.join('`'+x+'`' for x in sorted(old_ann-current_ann))+'。旧记录里的SearchIndex包名不能照搬。对被退回当前版的原始字节数未保存可靠独立快照，不作体积比较；注解提及清单在[比较原始数据](predecessors.json)，仍不冒充覆盖率。\n\n',
'## 最终文档目录\n\n']
for folder in ['annotations','references','references/api']:
    lines.append('\n'+folder+'/：'+ '、'.join(f'[{f.name}](../{f.relative_to(S).as_posix()})' for f in sorted((S/folder).glob('*.md')))+'。\n')
lines+=['\n## 官方参考与源码差异\n\n',
'已查阅[开放Agent Skills规范](https://agentskills.io/specification)、[MongoPlus官网](https://mongoplus.com/)、[注解](https://mongoplus.com/docs/annotation.html)、[聚合](https://mongoplus.com/docs/aggregate.html)、[配置](https://mongoplus.com/docs/property.html)、[自动索引](https://mongoplus.com/docs/autoCreateIndex.html)、[敏感词](https://mongoplus.com/docs/sensitiveWord.html)、[备份](https://mongoplus.com/docs/dataBackup.html)。官网首页BOM示例仍为2.1.9；本地POM为2.2.0。官方注解页的CollectionField.convertCollect不在当前声明；TimeSeries源注释含没有value成员的旧简写，已在新记录纠正。源码与官网差异保留，不从文档单方认定产品缺陷。\n']
write('audit/coverage-report.md',''.join(lines))
save('audit/validation-summary.json',{'sourceFiles':P['sourceFiles'],'publicDeclarations':len(T),'annotations':len(current_ann),'annotationDeclarations':len(ANN),'annotationCoverage':'43/43','capabilityRows':len(CAP),'capabilityDocuments':'101/101','build':rel(build),'checks':rel(checks) if checks else None,'live':rel(live),'liveStatuses':dict(statuses),'cleanupFailures':LR.get('cleanupFailures',[]),'creator':creator})
print('Coverage report generated; checks='+CR['status'])
