"""Validate real invariants: source delta, annotation contracts, coverage links and Java snippets."""
import argparse,datetime,hashlib,json,os,re,subprocess,unicodedata,uuid
from pathlib import Path
from urllib.parse import unquote
S=Path(__file__).resolve().parents[1];R=S.parent
def load(p):return json.loads((S/p).read_text(encoding='utf8'))
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--jdk',type=Path,required=True);a=ap.parse_args()
    out=S/'validation'/('checks-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')+'-'+uuid.uuid4().hex[:6]);out.mkdir()
    errors=[];checks={};d=load('audit/source-inventory.json');types=d['types'];ann=load('audit/annotations.json');caps=load('audit/capabilities.json');pt=load('audit/public-types.json')
    expected={f['path']:f['sha256'] for f in d['files']}
    actual={p.relative_to(R).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for module in R.glob('mongo-plus-*') if module.is_dir() for p in (module/'src/main/java').rglob('*.java')}
    actual.update({p.relative_to(R).as_posix():hashlib.sha256(p.read_bytes()).hexdigest() for p in (R/'tools/mongoplus-api-indexer/src/main/java').rglob('*.java')})
    delta=sorted(k for k in actual.keys()|expected.keys() if actual.get(k)!=expected.get(k));checks['sourceHashes']={'files':len(actual),'delta':delta}
    if delta:errors.append('Source drift: '+str(delta))
    # Independent definition discovery checks the AST annotations against actual Java declarations.
    definitions=[]
    for path in actual:
        raw=(R/path).read_text(encoding='utf8');m=re.search(r'\bpublic\s+@interface\s+(\w+)',raw)
        if m:definitions.append((path,m[1]))
    documented={(x['source'],x['simpleName']) for x in ann};found=set(definitions)
    checks['annotationDelta']={'actualDeclarations':len(found),'uniqueFqcn':len({x['name'] for x in ann}),'missing':sorted(found-documented),'extra':sorted(documented-found)}
    if found!=documented:errors.append('Annotation declarations differ')
    for t in (t for t in types if t['kind']=='ANNOTATION_TYPE'):
        rec=next(x for x in ann if x['source']==t['path']);path,anchor=rec['document'].split('#');text=(S/path).read_text(encoding='utf8')
        section=text.split('\n## '+t['simpleName']+'\n',1)[-1].split('\n## ',1)[0]
        if 'import '+t['name']+';' not in section:errors.append('Missing annotation import '+t['name'])
        for m in t['methods']:
            default=m['default'] if m['default'] is not None else '必填（无默认值）'
            if f'| `{m["name"]}` | `{m["return"]}` | `{default}` |' not in section:errors.append('Attribute/default mismatch '+t['name']+'.'+m['name'])
    checks['annotationAttributes']='PASS' if not errors else 'SEE_ERRORS'
    typekeys={(t['module'],t['name']) for t in types};recordkeys={(t['module'],t['name']) for t in pt}
    checks['publicTypeDelta']={'missing':sorted(typekeys-recordkeys),'extra':sorted(recordkeys-typekeys),'total':len(typekeys)}
    if typekeys!=recordkeys:errors.append('Unclassified public type')
    for c in caps:
        if not (S/c['document']).exists():errors.append('Missing capability topic '+c['id'])
        for api in c['apis']:
            n,_,m=api.partition('#')
            if n.startswith('@'):continue
            if not any(t['simpleName']==n and (not m or m in {x['name'] for x in t['methods']}) for t in types):errors.append('Unresolved capability API '+c['id']+' '+api)
    checks['capabilityApiResolution']={'count':len(caps),'status':'PASS'}
    def anchorify(h):
        h=re.sub(r'\[([^]]+)\]\([^)]+\)',r'\1',h).replace('`','').lower().strip()
        return ''.join(c for c in h if c in [' ','-','_'] or unicodedata.category(c)[0] in 'LN').replace(' ','-')
    linkcount=0
    for path in S.rglob('*.md'):
        if any(x.startswith('build-') or x.startswith('checks-') for x in path.relative_to(S).parts):continue
        text=path.read_text(encoding='utf8')
        # Exclude code examples so regexes/code strings are not interpreted as links.
        prose=re.sub(r'```[^\n]*\n.*?```','',text,flags=re.S)
        for dest in re.findall(r'\]\(([^)]+)\)',prose):
            if re.match(r'\w+://|mailto:',dest):continue
            dest=unquote(dest.strip('<>'));target,_,anchor=dest.partition('#')
            resolved=(path.parent/target).resolve() if target else path
            linkcount+=1
            if not resolved.is_relative_to(S):errors.append('Nonportable local link '+str(path.relative_to(S))+' '+dest);continue
            if not resolved.exists():errors.append('Broken relative link '+str(path.relative_to(S))+' '+dest);continue
            if anchor and resolved.suffix=='.md':
                headings=re.findall(r'^#{1,6}\s+(.+?)\s*#*$',resolved.read_text(encoding='utf8'),re.M)
                anchors={anchorify(h) for h in headings}
                if anchor not in anchors:errors.append('Missing anchor '+str(path.relative_to(S))+' '+dest)
    checks['relativeLinks']={'checked':linkcount,'errors':len([x for x in errors if 'link' in x or 'anchor' in x])}
    build=Path((S/'validation/latest-build.txt').read_text(encoding='utf8').strip());cp=(build/'runtime-classpath.txt').read_text(encoding='utf8').strip()
    snippets=[]
    for path in sorted([*(S/'references').glob('*.md'),*(S/'annotations').glob('*.md')]):
        for block,code in enumerate(re.findall(r'```java\s*\n(.*?)```',path.read_text(encoding='utf8'),re.S),1):
            folder=out/f'snippet-{len(snippets)+1:03}';folder.mkdir();classes=folder/'classes';classes.mkdir()
            public=re.search(r'\bpublic\s+(?:final\s+|abstract\s+)?(?:class|interface|enum)\s+(\w+)',code)
            name=public[1] if public else 'Snippet'
            source=folder/(name+'.java');source.write_text('package skillcheck.c'+str(len(snippets)+1)+';\n'+code,encoding='utf8')
            run=subprocess.run([str(a.jdk/('bin/javac'+('.exe' if os.name=='nt' else ''))),'-J-Duser.language=en','-encoding','UTF-8','-proc:none','--release','8','-cp',cp,'-d',str(classes),str(source)],stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
            (folder/'compile.log').write_bytes(run.stdout)
            item={'document':path.relative_to(S).as_posix(),'block':block,'exit':run.returncode,'source':source.relative_to(S).as_posix(),'status':'COMPILE_PASS' if run.returncode==0 else 'FAIL'};snippets.append(item)
            if run.returncode:errors.append('Java snippet failed '+item['document']+':'+str(block))
    checks['javaSnippets']={'count':len(snippets),'passed':sum(x['exit']==0 for x in snippets),'details':snippets}
    result={'checks':checks,'errors':errors,'status':'PASS' if not errors else 'FAIL'}
    (out/'results.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8');(S/'validation/latest-checks.txt').write_text(str(out),encoding='utf8')
    print(json.dumps({'status':result['status'],'snippets':checks['javaSnippets']['passed'],'total':len(snippets),'errors':errors,'output':str(out)},ensure_ascii=False))
    return int(bool(errors))
if __name__=='__main__':raise SystemExit(main())
