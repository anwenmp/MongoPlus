"""Independent artifact/reference validation; no Java or database execution."""
import hashlib
import json
import re
from collections import Counter
from pathlib import Path

from catalog import CATEGORIES
from search import search

ROOT=Path(__file__).resolve().parents[2]
OUT=ROOT/'generated'

def check(ok,message):
    if not ok:
        raise ValueError(message)

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def read_jsonl(path):
    with path.open(encoding='utf-8') as f:
        for n,line in enumerate(f,1):
            check(bool(line.strip()),f'blank line {path}:{n}')
            yield json.loads(line)

entries=list(read_jsonl(OUT/'capability-index.jsonl'))
ids=[c['id'] for c in entries]
check(len(ids)==len(set(ids)),'duplicate IDs')
check(len({c['name'] for c in entries})==len(entries),'duplicate names')
wanted={}
for c in entries:
    check(c['category']['secondary'] in CATEGORIES[c['category']['primary']],'category mismatch')
    check(c['sourceReferences'] and c['apiReferences'] and c['userIntents'] and c['limitations'],'required evidence/navigation missing')
    check(c['discoveryEligible']==(c['evidenceStatus'] in ('documented','source_verified')),'exposure safety mismatch')
    if c['evidenceStatus']=='documented':
        check(c['docReferences'] and c['documentationStatus']=='confirmed','documented state mismatch')
    if c['evidenceStatus'] in ('internal_only','candidate'):
        check(not c['discoveryEligible'],'candidate/internal recommended')
    check(c['verification']['databaseValidationStatus']=='NOT_RUN','invented database validation')
    check(c['verification']['runtimeTestStatus']=='NOT_RUN','invented runtime validation')
    if c.get('manualReview'):
        check(c['manualReview']['status']=='REQUIRED' and c['manualReview']['reasons'],'missing manual review reasons')
        check(c['confidence']['supportContract']=='needs_maintainer_review','manual review contract mismatch')
    for kind in ('apiReferences','sourceReferences'):
        for r in c[kind]:
            key=(r['indexPath'],r['indexLine'])
            wanted.setdefault(key,[]).append((kind,r))
    for t in c['testReferences']:
        lines=(ROOT/t['path']).read_text(encoding='utf-8').splitlines()
        check(t['method'] in lines[t['line']-1],'test line mismatch')
        check(t['executionStatus']=='NOT_RUN','invented test execution')
    for r in c['registrationReferences']:
        check(r['anchor'] in (ROOT/r['path']).read_text(encoding='utf-8').splitlines()[r['line']-1],'resource mismatch')
    for related in c['relatedCapabilities']:
        check(related in ids,'related capability missing')

checked=0
for path in sorted({k[0] for k in wanted}):
    for n,record in enumerate(read_jsonl(ROOT/path),1):
        for kind,r in wanted.get((path,n),[]):
            for key in ('module','recordType','qualifiedName','name','line','endLine','sourcePath','apiVisible'):
                check(r[key]==record[key],f'index record mismatch: {path}:{n} {key}')
            check(r.get('recordId')==record.get('id'),f'fabricated/lost ID {path}:{n}')
            if kind=='apiReferences':
                check(record['apiVisible'],'nonpublic API reference')
            else:
                check(r['usage']=='implementation_evidence','implementation treated as entry')
                lines=(ROOT/r['sourcePath']).read_text(encoding='utf-8').splitlines()
                snippet='\n'.join(lines[r['line']-1:r['endLine']])
                check(hashlib.sha256(snippet.encode()).hexdigest()==r['snippetSha256'],'source span drift')
                check(r['internalSymbol']==(not record['apiVisible']),'internal marker mismatch')
            checked+=1
check(checked==sum(map(len,wanted.values())),'unresolved index lines')

baseline=json.loads((ROOT/'tools/capability-indexer/review-baseline.json').read_text(encoding='utf-8'))
for path,h in baseline['files'].items():
    check(sha(ROOT/path)==h,f'reviewed input changed: {path}')

# Check all repository-relative Markdown link targets, and explicit capability anchors.
for name in ('capability-index.md','capability-review.md','undocumented-capabilities.md','capability-retrieval-tests.md'):
    text=(OUT/name).read_text(encoding='utf-8')
    for target in re.findall(r'\]\(([^)]+)\)',text):
        if target.startswith(('http://','https://')):
            continue
        path,_,anchor=target.partition('#')
        dest=(OUT/path).resolve()
        check(dest.is_file(),f'broken Markdown link: {name}: {target}')
        if anchor.startswith('mp.'):
            check(f'id="{anchor}"' in dest.read_text(encoding='utf-8'),f'broken capability anchor: {target}')

tests=list(read_jsonl(OUT/'capability-retrieval-tests.jsonl'))
check(Counter(t['suite'] for t in tests)==Counter(original=32,validation=18,holdout=16),'retrieval suite size mismatch')
check(len({t['id'] for t in tests})==len(tests),'duplicate test IDs')
for t in tests:
    check(t['topMatches']==search(t['query'],entries),'stale retrieval results')
    if t.get('expectedDisposition')=='insufficient':
        check(all(not h['recommendationEligible'] for h in t['topMatches']),'unsupported requirement recommended')
    for h in t['topMatches']:
        check(h['id'] in ids,'retrieval missing capability')
        if h['evidenceStatus'] in ('candidate','internal_only'):
            check(not h['recommendationEligible'],'retrieval promoted candidate/internal')
    check(bool(t['manualDecision']),'missing manual test review')

manifest=json.loads((OUT/'capability-manifest.json').read_text(encoding='utf-8'))
protocol=json.loads((ROOT/'tools/capability-indexer/retrieval-evaluation-protocol.json').read_text(encoding='utf-8'))
for name,h in protocol['frozenFiles'].items():
    check(sha(ROOT/'tools/capability-indexer'/name)==h,f'evaluation rules/cases changed after freeze: {name}')
review_ids={'mp.dictionary-bind','mp.nonnull-validation','mp.cross-source-atomic','mp.block-attack','mp.optimistic-lock','mp.backup'}
check({c['id'] for c in entries if c.get('manualReview',{}).get('status')=='REQUIRED'}==review_ids,'required manual review entries mismatch')
check(manifest['counts']['total']==len(entries),'stale manifest counts')
for name,h in manifest['outputs'].items():
    check(sha(OUT/name)==h,f'output hash mismatch: {name}')
print(json.dumps(dict(result='PASS',capabilities=len(entries),referenceOccurrencesChecked=checked,
                      protectedFilesChecked=len(baseline['files']),retrievalCases=len(tests),
                      retrievalFailures=[t['id'] for t in tests if not t['passed']],
                      javaTests='NOT_RUN',databaseTests='NOT_RUN'),ensure_ascii=False,indent=2))
