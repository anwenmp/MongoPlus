"""Resolve a curated capability catalog against existing indexes and emit navigation.

Standard library only. Does not parse Java or regenerate API indexes. All reads are local.
Default execution refuses source/index drift from the last explicitly recorded review.
"""
import argparse
import copy
import hashlib
import json
import re
import subprocess
from datetime import date
from collections import Counter, defaultdict
from pathlib import Path
from xml.etree import ElementTree

from catalog import CATALOG, CATEGORIES, MODULES, REGISTRATION_FILES
from search import search

ROOT = Path(__file__).resolve().parents[2]
HERE = Path(__file__).resolve().parent
OUT = ROOT/'generated'
STATUSES = {'documented','source_verified','candidate','internal_only','deprecated'}

def require(condition, message):
    if not condition:
        raise ValueError(message)

def digest(path):
    h = hashlib.sha256()
    with path.open('rb') as f:
        for block in iter(lambda:f.read(65536),b''):
            h.update(block)
    return h.hexdigest()

def dump(path, value):
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def jsonl(path, values):
    path.write_text(''.join(json.dumps(v,ensure_ascii=False,separators=(',',':'))+'\n' for v in values),encoding='utf-8')

def selector_parts(selector):
    module = None
    if ':' in selector:
        prefix,selector=selector.split(':',1)
        module=MODULES[prefix]
    owner,_,member=selector.partition('#')
    return module,'com.mongoplus.'+owner,member

def matches(selector, record):
    module,owner,member=selector_parts(selector)
    return (record['qualifiedName']==owner and (module is None or record['module']==module)
            and (record['recordType']=='type' if not member else
                 record['recordType'] in ('method','field') and record['name']==member))

def scan_index(name, selectors):
    """Stream JSONL; retain only records selected by the human catalog."""
    result={s:[] for s in selectors}
    count=Counter();modules=Counter();paths=set();api_flags=Counter()
    by_owner=defaultdict(list)
    for s in selectors:
        by_owner[selector_parts(s)[1]].append(s)
    with (OUT/name).open(encoding='utf-8') as f:
        for number,line in enumerate(f,1):
            require(bool(line.strip()),f'blank JSONL line {name}:{number}')
            record=json.loads(line)
            count[record['recordType']]+=1;modules[record['module']]+=1
            paths.add(record['sourcePath']);api_flags[str(record['apiVisible'])]+=1
            for s in by_owner.get(record['qualifiedName'],[]):
                if matches(s,record):
                    result[s].append((number,record))
    for s,found in result.items():
        require(bool(found),f'unresolved selector {name}: {s}')
        require(len({r['module'] for _,r in found})==1,f'ambiguous module: {s}')
    return result,dict(records=sum(count.values()),byRecordType=dict(count),byModule=dict(modules),
                       distinctDeclarationFiles=len(paths),apiVisible=dict(api_flags))

def reference(number, record, index, role=None):
    path=ROOT/record['sourcePath']
    lines=path.read_text(encoding='utf-8').splitlines()
    start,end=record['line'],record['endLine']
    require(1<=start<=end<=len(lines),f'invalid lines: {record["sourcePath"]}:{start}-{end}')
    body='\n'.join(lines[start-1:end])
    name=record['name']
    # A declaration must still be found in the recorded span, not merely in a file.
    decl_pattern=(r'\b(?:class|interface|enum|record)\s+'+re.escape(name)+r'\b' if record['recordType']=='type'
                  else r'\b'+re.escape(name)+(r'\s*\(' if record['recordType']=='method' else r'\b'))
    require(bool(re.search(decl_pattern,body)),f'stale declaration span: {record["sourcePath"]}:{start} {name}')
    ref=dict(indexPath='generated/'+index,indexLine=number,recordType=record['recordType'],
             module=record['module'],qualifiedName=record['qualifiedName'],name=name,
             sourcePath=record['sourcePath'],line=start,endLine=end,
             symbolVisibility=record['visibility'],apiVisible=record['apiVisible'])
    if 'id' in record:
        ref['recordId']=record['id']
    if 'signature' in record:
        ref['signature']=record['signature']
    if role:
        ref.update(role=role,usage='implementation_evidence',
                   internalSymbol=not record['apiVisible'],
                   snippetSha256=hashlib.sha256(body.encode('utf-8')).hexdigest())
    return ref

def test_reference(test):
    r=copy.deepcopy(test)
    lines=(ROOT/r['path']).read_text(encoding='utf-8').splitlines()
    pattern=re.compile(r'\bvoid\s+'+re.escape(r['method'])+r'\s*\(')
    found=[i+1 for i,l in enumerate(lines) if pattern.search(l)]
    require(len(found)==1,f'missing/ambiguous test: {r}')
    r['line']=found[0]
    r['executionStatus']='NOT_RUN'
    r['reviewStatus']='test_source_reviewed'
    return r

def resource_reference(path, anchor):
    ls=(ROOT/path).read_text(encoding='utf-8').splitlines()
    found=[i+1 for i,l in enumerate(ls) if anchor in l]
    require(len(found)==1,f'missing/ambiguous resource anchor {path}: {anchor}')
    return dict(path=path,line=found[0],anchor=anchor,role='registration_resource')

def load_and_resolve():
    pom=ElementTree.parse(ROOT/'pom.xml').getroot()
    version=pom.findtext('{http://maven.apache.org/POM/4.0.0}version')
    require(bool(version),'root POM version missing; resolve explicitly before generating')
    docs=json.loads((HERE/'doc-review.json').read_text(encoding='utf-8'))
    pages={p['id']:p for p in docs['pages']}
    apis={s for c in CATALOG for s in c['apiSelectors']}
    sources={e['selector'] for c in CATALOG for e in c['sourceSelectors']}
    api,api_stats=scan_index('api-index.jsonl',apis)
    src,src_stats=scan_index('source-index.jsonl',sources)
    entries=[];paths={'pom.xml','generated/api-index.jsonl','generated/source-index.jsonl',
                     'tools/capability-indexer/doc-review.json'}
    ids=set()
    for original in CATALOG:
        c=copy.deepcopy(original)
        require(c['id'] not in ids,f'duplicate capability {c["id"]}');ids.add(c['id'])
        require(c['evidenceStatus'] in STATUSES,'unknown evidence status')
        require(c['category']['secondary'] in CATEGORIES[c['category']['primary']],'bad category')
        c['apiReferences']=[reference(n,r,'api-index.jsonl') for s in c.pop('apiSelectors') for n,r in api[s]]
        require(all(r['apiVisible'] for r in c['apiReferences']),'non-API reference leaked into API list')
        c['sourceReferences']=[reference(n,r,'source-index.jsonl',s['role']) for s in c.pop('sourceSelectors') for n,r in src[s['selector']]]
        c['testReferences']=[test_reference(t) for t in c.pop('tests')]
        c['docReferences']=[dict(kind='official_website',url=pages[p]['url'],reviewId=p,
                                 reviewNote=pages[p]['reviewNote']) for p in c.pop('docPages')]
        c['registrationReferences']=[resource_reference(p,a) for p,a in REGISTRATION_FILES.get(c['id'][3:],[])]
        require(c['sourceReferences'],'missing implementation evidence')
        require(c['evidenceStatus']!='documented' or c['docReferences'],'documented without reviewed docs')
        c['schemaVersion']='1.0'
        c['repositoryVersion']=version
        c['verification']={'level':'static_implementation_review' if c['evidenceStatus'] in ('documented','source_verified') else 'partial_or_internal_review',
                           'testSourceAssociated':bool(c['testReferences']), 'runtimeTestStatus':'NOT_RUN',
                           'databaseValidationStatus':'NOT_RUN'}
        c['confidence']={'level':'low' if c['evidenceStatus']=='candidate' else 'high',
                         'basis': ('仅局部实现或标记可见，完整使用/保证契约未闭合。' if c['evidenceStatus']=='candidate' else
                                   '人工核对当前实现及启用/调用路径；可信度仅针对条目描述范围，不代表运行或兼容验证。'),
                         'supportContract':'needs_maintainer_review' if c.get('manualReview') or c['evidenceStatus'] in ('source_verified','candidate','internal_only') else 'documented_with_source_boundaries'}
        c['documentationStatus']=('confirmed' if c['evidenceStatus']=='documented' else
                                  'navigation_link_unusable' if c['id']=='mp.field-encryption' else
                                  'not_explicit_in_reviewed_pages')
        c['discoveryEligible']=c['evidenceStatus'] in ('documented','source_verified')
        for r in c['sourceReferences']+c['apiReferences']:paths.add(r['sourcePath'])
        for r in c['testReferences']+c['registrationReferences']:paths.add(r['path'])
        entries.append(c)
    for c in entries:
        require(set(c['relatedCapabilities'])<=ids,'unresolved related capability')
    return entries,dict(projectVersion=version,apiIndex=api_stats,sourceIndex=src_stats),sorted(paths),docs

def link(path, line=None):
    # All generated Markdown lives in generated/. Relative links survive repository moves.
    return f'[{path}'+(f':{line}' if line else '')+f'](../{path}'+(f'#L{line}' if line else '')+')'

def api_label(r):
    return f"`{r['module']}:{r['qualifiedName']}"+(('#'+r['name']) if r['recordType']!='type' else '')+'`'

def entry_markdown(c):
    lines=[f"<a id=\"{c['id']}\"></a>",f"#### {c['name']} (`{c['id']}`)",'',
           f"状态：`{c['evidenceStatus']}`；暴露：`{c['exposure']}`；运行验证：`NOT_RUN`。",'',c['description'],'',
           '典型需求：'+'；'.join(c['userIntents'])+'。','',
           'API：'+'；'.join(dict.fromkeys(api_label(r) for r in c['apiReferences']))+'。','',
           '实现及接线：','']
    seen=set()
    for r in c['sourceReferences']:
        k=(r['sourcePath'],r['role'])
        if k not in seen:
            lines.append(f"- {link(r['sourcePath'],r['line'])}：{r['role']}（实现证据，不自动作为推荐入口）。");seen.add(k)
    for r in c['registrationReferences']:lines.append(f"- {link(r['path'],r['line'])}：注册资源。")
    lines+=['','测试源码证据（本轮未执行）：','']
    lines += [f"- {link(t['path'],t['line'])} `{t['method']}`：{t['verifies']}。" for t in c['testReferences']] or ['- 未找到专用测试；仅静态实现核验。']
    lines+=['','官网：'+('；'.join(f"[{d['reviewId']}]({d['url']})" for d in c['docReferences']) if c['docReferences'] else '已检查范围未找到明确功能介绍，见文档检查清单。'),'',
            '启用：'+c['registration'],'','限制：','']
    lines += ['- '+l for l in c['limitations']]
    if c['documentationNote']:lines+=['','文档差异：'+c['documentationNote']]
    if c['openQuestions']:lines+=['','待确认：'+'；'.join(c['openQuestions'])]
    if c.get('manualReview'):
        lines+=['','人工审核：`REQUIRED`；'+'；'.join(c['manualReview']['reasons'])]
    if c.get('negativeIntents'):lines+=['','不能充分满足的意图：'+'；'.join(c['negativeIntents'])]
    if c.get('retrievalPolicy'):lines+=['','检索约束：`'+json.dumps(c['retrievalPolicy'],ensure_ascii=False)+'`']
    return '\n'.join(lines)+'\n'

def retrieval(entries):
    cases=[]
    for suite, name in [('original','retrieval-cases.json'),('validation','retrieval-validation-cases.json'),('holdout','retrieval-holdout-cases.json')]:
        cases += [dict(**case,suite=suite) for case in json.loads((HERE/name).read_text(encoding='utf-8'))]
    results=[]
    for case in cases:
        hits=search(case['query'],entries)
        expected=['mp.'+v for v in case['expected']]
        first=hits[0] if hits else None
        good=bool(first and first['id'] in expected) if expected else not hits
        recall=bool(set(expected)&{h['id'] for h in hits}) if expected else not hits
        wanted=case.get('expectedDisposition','scoped_static_evidence')
        safe=(all(not h['recommendationEligible'] for h in hits) if wanted=='insufficient' else
              bool(first and first['disposition']=='candidate' and not first['recommendationEligible']) if wanted=='candidate' else
              bool(first and first['recommendationEligible'] and first['disposition']==wanted))
        results.append(dict(**case,topMatches=hits,reasonableTop1=good,expectedInTop3=recall,
                            evidenceSafetyPassed=safe,passed=good and safe,
                            manualRecommendationAllowed=good and safe and wanted=='scoped_static_evidence',
                            manualDecision=('证据不足，不能推荐；'+case['review'] if wanted=='insufficient' else case['review']),
                            reviewBasis='人工事先给定预期；排序由纯本地词法脚本计算，不依据预期强行改写命中。'))
    return results

def render(entries, stats, baseline, docs, tests):
    by_status=Counter(c['evidenceStatus'] for c in entries)
    by_doc=Counter(c['documentationStatus'] for c in entries)
    groups=defaultdict(list)
    for c in entries:groups[(c['category']['primary'],c['category']['secondary'])].append(c)
    nav=['# MongoPlus 能力发现导航','',
         '先按用户需求选择能力，再沿真实 API、实现和测试证据核验重载。这里只做能力导航，不替代 API 索引。',
         '', '[机器索引](capability-index.jsonl) · [分析报告](capability-review.md) · [官网未明确介绍项](undocumented-capabilities.md) · [离线检索验证](capability-retrieval-tests.md)',
         '', '所有“核验”均指静态实现/测试源码阅读；本轮没有运行 MongoDB 或 Java 测试。候选/内部项不可直接作为支持方案。','',
         '| 一级 | 二级 | 数量 |','| --- | --- | ---: |']
    for primary,subs in CATEGORIES.items():
        for secondary in subs:nav.append(f'| {primary} | {secondary} | {len(groups[primary,secondary])} |')
    for primary,subs in CATEGORIES.items():
        nav+=['',f'## {primary}']
        for secondary in subs:
            nav+=['',f'### {secondary}']
            for c in groups[primary,secondary]:nav+=['',entry_markdown(c)]
    (OUT/'capability-index.md').write_text('\n'.join(nav)+'\n',encoding='utf-8')
    undoc=['# 官网未明确介绍的能力及候选','',
           '范围限定：只针对 [文档检查清单](../tools/capability-indexer/doc-review.json) 已检查正文，不声称完成全站分析。字段加密导航链接不可用，单列为文档未核实。异步多写已在官网备份页介绍，不计为未记录。','']
    for status,title in [('source_verified','源码已核验，支持契约仍需维护者确认'),('candidate','证据不足的候选'),('internal_only','内部/开发工具，不推荐为业务 API')]:
        undoc += [f'## {title}','']
        for c in entries:
            if c['evidenceStatus']!=status:continue
            undoc += [f"### {c['name']} (`{c['id']}`)",'',c['description'],'',
                      f"- 官网检查：`{c['documentationStatus']}`。"+(c['documentationNote'] or '已检查正文未找到明确说明。'),
                      '- 真实 API：'+'；'.join(dict.fromkeys(api_label(r) for r in c['apiReferences']))+'。',
                      '- 源码：'+'；'.join(dict.fromkeys(link(r['sourcePath'],r['line']) for r in c['sourceReferences']))+'。',
                      '- 测试：'+('有相关测试源码，未执行；见完整能力条目。' if c['testReferences'] else '未找到专用行为测试。'),
                      '- 外部可用性：'+('存在公开入口且调用/注册可静态闭合；并非发布兼容承诺。' if status=='source_verified' else '尚未确认完整使用契约。' if status=='candidate' else '主要为内部接线或开发工具，不建议业务依赖。'),
                      '- 启用：'+c['registration'],
                      '- 人工确认：'+'；'.join(c['openQuestions'] or c['limitations']),
                      f"- [完整条目](capability-index.md#{c['id']})。",'']
    (OUT/'undocumented-capabilities.md').write_text('\n'.join(undoc)+'\n',encoding='utf-8')

    passed=sum(t['passed'] for t in tests);top1=sum(t['reasonableTop1'] for t in tests);top3=sum(t['expectedInTop3'] for t in tests)
    suite_stats={suite:dict(total=len(rows),passed=sum(t['passed'] for t in rows),failures=[t['id'] for t in rows if not t['passed']])
                 for suite in ('original','validation','holdout') for rows in [[t for t in tests if t['suite']==suite]]}
    review=['# MongoPlus 能力发现分析报告','',f"分析基准：{baseline['reviewedOn']}，根 POM 版本 `{stats['projectVersion']}`，提交 `{baseline['gitHead']}`。本轮仅增量维护现有能力发现工具和能力产物；工作区已有 Java 改动及 API 索引保持原样。",'',
            '## 分类设计与粒度','', '分类先于产物确定，见 [设计及实施顺序](../tools/capability-indexer/DESIGN.md)。一级围绕任务，二级保持功能簇粒度。Mapper/Repository/Service 合为实体访问；操作符、算法和索引类型不各建条目。同一 API 可为多个场景提供证据。','',
            '| 一级 | 二级 | 能力数 |','| --- | --- | ---: |']
    for primary,subs in CATEGORIES.items():review.append(f"| {primary} | {'、'.join(subs)} | {sum(len(groups[primary,s]) for s in subs)} |")
    review+=['','## 实际统计','',f'- 能力共 {len(entries)} 项。']
    review += [f'- `{s}`：{by_status[s]} 项。' for s in sorted(STATUSES)]
    review += [f'- 官网明确介绍：{by_doc["confirmed"]}；已检查正文未明确介绍：{by_doc["not_explicit_in_reviewed_pages"]}；导航专页不可用：{by_doc["navigation_link_unusable"]}。',
               f'- 已静态核验的可发现能力：{by_status["documented"]+by_status["source_verified"]}；候选：{by_status["candidate"]}；内部/开发工具：{by_status["internal_only"]}。',
               f'- 关联测试源码的能力：{sum(bool(c["testReferences"]) for c in entries)}；本轮 Java 测试、数据库集成测试均未执行。',
               f'- API 输入：{stats["apiIndex"]["records"]} 条；源码声明输入：{stats["sourceIndex"]["records"]} 条；均覆盖 {len(stats["sourceIndex"]["byModule"])} 个含源码模块。',
               '- generated/api-index.md 的原扫描报告为 622 个 Java 文件；JSONL 仅记录声明，distinctDeclarationFiles 不能替代扫描文件数。',
               '- 所有已输出 API 引用、源码/测试/注册资源引用均经生成时解析/定位；未解决引用为 0，否则脚本失败退出且不写产物。','',
               '## 状态与可信度','',
               '- documented：已核对官网明确功能介绍，并有当前实现证据；不等于已运行或覆盖官网全部保证。',
               '- source_verified：已查到实现与启用/调用路径，官网已检查正文未明确解释该能力或其边界。',
               '- candidate：只有局部实现/标记，使用契约或行为保证未闭合，不可推荐为支持方案。',
               '- internal_only：内部接线/开发工具，即便 public 也不推定为业务 API。',
               '- deprecated：预留给已废弃能力；当前没有单独创建重复能力条目。旧 FieldHandler 方法和手动数据源切换的废弃/不推荐提示放在对应能力限制内。',
               '- confidence 只评价所写结论的证据强度，不能替代 runtimeTestStatus；内部项 high 也不使其可推荐。',
               '- manualReview.status=REQUIRED 是必须人工审核的接线/运行边界；即使 documented 也不能视为边界行为已验证。',
               '- documentationStatus 独立于 evidenceStatus，保留官网范围、断链和局部文档差异。','',
               '## 文档与版本差异','', docs['scope'], '',docs['absencePolicy'],'',
               '本地仅有 README、docs/ai 和设计/索引报告，没有官网站点文档源码。先阅读本地知识入口/公开 API 导航，随后按已识别功能定向核对官网单层页面。完整清单、章节、抓取哈希和人工笔记见 [doc-review.json](../tools/capability-indexer/doc-review.json)。没有递归抓取外链或完整网站。','',
               '- aggregateBuild 为页面明确标注已删除的旧构建器，未作为当前方案入口。',
               '- 无实体页面含旧名字，按当前 BaseMapper 建索引。',
               '- 官网部分依赖示例仍使用 2.1.9，当前根 POM 为 '+stats['projectVersion']+'；本索引结论只针对当前源码，不追认旧版本行为。',
               '- 官网异步重试“立即返回”与源码 get() 等待不一致，条目记录差异。',
               '- 敏感词页“暂不支持替换”与 Manager.replace 共存，仅确认显式调用；不推成自动替换。',
               '- 字段加密专页导航落首页，仅确认当前注解与处理链；property 页可证全局密钥配置。','',
               '## 覆盖关系与缺口','',
               '源码声明索引不含方法体/调用图，不能从它直接推导调用关系。先用 CodeGraph 查询入口、关系和调用方，未覆盖细节再沿声明定位读取源码；最终可持续验证证据使用当前文件、真实记录和哈希。测试不在这套声明索引扫描范围内，按实际文件/方法单独关联。','',
               '- 覆盖全部 9 个含源码模块：annotation、core、Boot、Boot4、Solon、sensitive-word、sharding、sharding Starter、indexer。POM-only BOM 作为依赖管理不单建业务能力。',
               '- 基础查询、CRUD、聚合、字段映射/填充/加密、租户/逻辑/版本、事务、扩展、连接、索引、时序、备份和操作记录均有导航。',
               '- 声明索引没有枚举常量、继承展开或 Lombok 生成方法；通过类型引用和真实源码补充理解，不虚构方法 ID。',
               '- 现有生产行为测试主要集中聚合构建/编码；其他功能没有找到专用测试，记录为空且明确仅静态核验。',
               '- 不能建立完整支持证据：字典回写自动接线/目标覆盖、NonNull 自动运行时校验、跨独立数据源原子提交。',
               '- 未确认的用户保证：命令事件后的可靠通知、所有批量操作防误删、跨分片合并分页、备份一致快照；不得提升为确定功能。',
               '- 官网未记录不代表不受支持；当前源码存在也不代表对所有发布版本/容器稳定支持。','',
               '## 本轮增量修改的能力条目','',
               '- mp.nested-pipeline：补充关联集合/关联结果、内部排序与 limit 意图；指向独立子 Aggregate 与 preservesSortThenLimit 测试源码。',
               '- mp.command-events：补充通知入口；已知不足意图参与导航评分，可靠/提交后/恰好一次通知仍全部禁止推荐，不扩张 Listener 保证。',
               '- mp.backup：收窄为 JSON/ZIP 备份；增加检索前置、工作簿排除和全局无匹配意图；明确首条目前 drop，添加人工审核。',
               '- mp.dictionary-bind、mp.nonnull-validation、mp.cross-source-atomic：保持 candidate 和 NOT_RUN，增加明确人工审核事项。',
               '- mp.block-attack、mp.optimistic-lock：增加人工审核；乐观锁补充 $set 旧版本缺失时默认跳过增强的前置边界。',
               '- 保留 48 个稳定能力 ID、原字段及 schemaVersion=1.0；manualReview/retrievalPolicy 为可选增量字段，检索仍返回列表。','',
               '## 必须人工审核的边界','',
               '1. BlockAttack Driver 事件抛异常是否真正阻止发送，以及只检查首项的批量边界。',
               '2. 乐观锁 BSON 改写回写、Integer 类型和重试返回时机。',
               '3. 字典绑定字段顺序与命名、DBRef 关联加载、ExtraFields 冲突/写回。',
               '4. 动态数据源资源关闭、嵌套 ThreadLocal、多源/分片事务部分提交失败。',
               '5. Boot/Boot4/Solon 的版本/JDK/Driver 矩阵、已有 MongoClient Bean 的扩展覆盖。',
               '6. 备份的 BSON 类型保真、恢复删除行为和一致性；镜像失败补偿/线程池生命周期。','',
               '7. NonNull 仅类型限定别名证据，自动运行时校验消费者未确认。',
               '8. 跨独立数据源逐个 commit 不能建立原子性；必须审核部分提交失败和补偿契约。',
               '索引中 mp.dictionary-bind、mp.nonnull-validation、mp.cross-source-atomic、mp.block-attack、mp.optimistic-lock、mp.backup 均标记 manualReview.status=REQUIRED。','',
               '## 离线检索验收','',
               f'冻结 {len(tests)} 条需求：首项合理 {top1}/{len(tests)}，前三项包含预期 {top3}/{len(tests)}，结合候选/不足证据控制后通过 {passed}/{len(tests)}。这些是当前词法基线的人工样本结果，不是语义模型评估。',
               f"原有集：{suite_stats['original']['passed']}/{suite_stats['original']['total']}；增量验证集：{suite_stats['validation']['passed']}/{suite_stats['validation']['total']}；最终独立保留集：{suite_stats['holdout']['passed']}/{suite_stats['holdout']['total']}。首次 18 条独立样本通过 16 条，H03 否定关联、H07 xlsx 误召回促成第二轮修正，故转为验证集；首次结果和规则哈希保留在工具目录 retrieval-evaluation-protocol.json。另存 16 条新保留集，最终规则冻结后首次执行，不用于调参。",
               '原始基线 29/32：Q03 普通排序压过 lookup，已补充关联结果排序语义；Q30 已知负意图没有进入召回评分，现作相关但不足证据的导航；Q32 泛化导出词误召回备份，现需 JSON/ZIP 备份锚点并排除工作簿格式。无合适能力时返回空列表。',
               '本轮实际执行 generate.py、validate.py 和 unittest discover（test_search.py，5 项）；这些仅运行 Python 离线检索与产物校验。Java、数据库、容器及并发/故障场景均 NOT_RUN。',
               '保留集只是有限词法样本，不能证明通用语义检索质量；显式“不涉及/不需要”按标点裁剪，双重否定、复杂转折、未枚举同义词及其他输出格式仍可能误匹配。负意图对整项复合需求保守阻断，后续消费方仍须核对限制与人工审核。',
               '', '[逐例命中、API、实现、测试及人工评议](capability-retrieval-tests.md)；[机器记录](capability-retrieval-tests.jsonl)。失败案例保留，不用预期结果强制替换排序。','',
               '| 失败用例 | 首项命中 | 原因及人工结论 |','| --- | --- | --- |']
    for t in tests:
        if not t['passed']:
            review.append(f"| {t['id']}：{t['query']} | {t['topMatches'][0]['id'] if t['topMatches'] else '无'} | {t['manualDecision']}（证据控制：{'已阻断推荐' if t['evidenceSafetyPassed'] and t.get('expectedDisposition')=='insufficient' else '必须按人工评议复核'}） |")
    review += ['',
               '## 版本更新流程','',
               '1. 先由现有索引维护流程更新 API/source 索引；本脚本从不触发该步骤。',
               '2. 默认 generate.py 对 review-baseline.json 的输入、源码、测试和注册资源哈希做漂移检查；漂移时停止，重新阅读变更证据与文档。',
               '3. 人工调整 catalog.py 的描述/状态/选择器，维护稳定 id；重命名保留迁移说明，不以行号作为能力身份。',
               '4. 复核后显式 --record-review --review-date YYYY-MM-DD 记录新基线，再生成并复核所有文件 diff。该参数只记录人工审查，不会重新推导功能或运行测试。',
               '5. 重新跑冻结需求集并扩充真实需求/负例；持续跟踪漏召回、误匹配及候选被推荐问题。发布时记录版本和输入哈希。','',
               '## 本轮未做','',
               '未修改业务源码/现有生成器，未重生成现有 API 索引，未安装依赖、启动数据库/Web/MCP 或调用 LLM API，未编写 Skill，未执行任何写数据库示例。']
    (OUT/'capability-review.md').write_text('\n'.join(review)+'\n',encoding='utf-8')

    tm=['# 自然语言需求离线检索验证','',
        '原有集 retrieval-cases.json 不变；首批 18 条独立用例首次 16/18，修正 H03/H07 后转为 retrieval-validation-cases.json。最终 16 条 retrieval-holdout-cases.json 在第二轮规则调整前保存，最终规则冻结后首次执行。排序不读取 expected。词法基线使用中文二元词片/英文词 IDF、关键词短语加权、前置/排除条件、负意图导航加权和显式否定范围处理，阈值 5；分数只表示相关性。不支持的工作簿输出返回空列表；负意图命中时全部结果禁止推荐。',
        '',f'共 {len(tests)} 条；首项合理 {top1}，前三含预期 {top3}，最终通过 {passed}。所有 Java 测试均为源码关联，未执行。','']
    tm += [f"原有集：{suite_stats['original']['passed']}/{suite_stats['original']['total']}；增量验证集：{suite_stats['validation']['passed']}/{suite_stats['validation']['total']}；最终独立保留集：{suite_stats['holdout']['passed']}/{suite_stats['holdout']['total']}。",
           '证据安全检查覆盖所有不足证据结果；候选首项必须仍是 candidate。无匹配用例要求结果为空。','']
    for t in tests:
        tm += [f"## {t['id']}：{t['query']}",'',
               f"测试集：`{t['suite']}`；预期：{', '.join('mp.'+v for v in t['expected']) or '无证据应拒绝推荐'}；预期证据级别：`{t.get('expectedDisposition','scoped_static_evidence')}`。",'',
               f"首项合理：{t['reasonableTop1']}；前三包含预期：{t['expectedInTop3']}；证据安全：{t['evidenceSafetyPassed']}；通过：{t['passed']}；人工允许按条目范围推荐：{t['manualRecommendationAllowed']}。",'',
               '人工评议：'+t['manualDecision'],'']
        if not t['topMatches']:tm+=['无匹配：没有同时满足检索约束与分数阈值的能力。','']
        for i,h in enumerate(t['topMatches'],1):
            tm += [f"### {i}. {h['name']} (`{h['id']}`，{h['score']})",'',
                   f"状态 `{h['evidenceStatus']}`；可作为限定范围静态入口：{h['recommendationEligible']}；判定 `{h['disposition']}`。",'',
                   'API：'+'；'.join(dict.fromkeys(api_label(r) for r in h['apiReferences'][:4]))+'。','',
                   '实现：'+'；'.join(dict.fromkeys(link(r['sourcePath'],r['line']) for r in h['sourceReferences'][:3]))+'。','',
                   '测试：'+('；'.join(link(r['path'],r['line'])+' `'+r['method']+'`' for r in h['testReferences']) or '无专用行为测试关联')+'。','',
                   '边界：'+'；'.join(h['limitations']), '']
    (OUT/'capability-retrieval-tests.md').write_text('\n'.join(tm)+'\n',encoding='utf-8')
    return dict(total=len(entries),byEvidenceStatus=dict(by_status),byDocumentationStatus=dict(by_doc),
                withTestReferences=sum(bool(c['testReferences']) for c in entries),
                retrieval=dict(total=len(tests),top1Reasonable=top1,top3Expected=top3,passed=passed,
                               failures=[t['id'] for t in tests if not t['passed']],suites=suite_stats))

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--record-review',action='store_true',help='Explicitly record a manually reviewed new evidence baseline')
    parser.add_argument('--review-date',help='Required ISO date when recording a manual review; website check dates remain separate')
    args=parser.parse_args()
    entries,stats,paths,docs=load_and_resolve()
    for f in ['generated/api-index.md','generated/index.md']:
        paths.append(f)
    paths+=sorted(p.relative_to(ROOT).as_posix() for p in (OUT/'modules').glob('*/index.md'))
    current={p:digest(ROOT/p) for p in sorted(set(paths))}
    baseline_path=HERE/'review-baseline.json'
    if args.record_review:
        require(args.review_date is not None,'--record-review requires --review-date YYYY-MM-DD')
        date.fromisoformat(args.review_date)
        baseline=dict(reviewedOn=args.review_date,gitHead=subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
                      policy='Hashes freeze manually reviewed evidence, not runtime validation.',files=current)
        dump(baseline_path,baseline)
    else:
        require(baseline_path.exists(),'No review baseline; manually review evidence before --record-review')
        baseline=json.loads(baseline_path.read_text(encoding='utf-8'))
        require(current==baseline['files'],'Evidence drift: re-review changed files before --record-review; no output written')
    tests=retrieval(entries)
    jsonl(OUT/'capability-index.jsonl',entries)
    jsonl(OUT/'capability-retrieval-tests.jsonl',tests)
    summary=render(entries,stats,baseline,docs,tests)
    manifest=dict(schemaVersion='1.0',reviewedOn=baseline['reviewedOn'],gitHead=baseline['gitHead'],projectVersion=stats['projectVersion'],
                  inputs=stats,counts=summary,referenceValidation='PASS',
                  existingArtifactsUnchanged=all(digest(ROOT/p)==h for p,h in current.items()),
                  baseline='tools/capability-indexer/review-baseline.json',
                  generatorSha256=digest(Path(__file__)),catalogSha256=digest(HERE/'catalog.py'),
                  casesSha256=digest(HERE/'retrieval-cases.json'),searchSha256=digest(HERE/'search.py'),
                  holdoutCasesSha256=digest(HERE/'retrieval-holdout-cases.json'),
                  validationCasesSha256=digest(HERE/'retrieval-validation-cases.json'),
                  evaluationProtocolSha256=digest(HERE/'retrieval-evaluation-protocol.json'),
                  outputs={p.name:digest(p) for p in sorted(list(OUT.glob('capability-*'))+[OUT/'undocumented-capabilities.md']) if p.name!='capability-manifest.json'})
    dump(OUT/'capability-manifest.json',manifest)
    print(json.dumps(summary,ensure_ascii=False,indent=2))

if __name__=='__main__':
    main()
