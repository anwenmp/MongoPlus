"""Isolated javac verification; never invokes Maven install or writes outside this skill."""
import argparse, datetime, json, os, subprocess, uuid
from pathlib import Path
S=Path(__file__).resolve().parents[1]; R=S.parent
def main():
    p=argparse.ArgumentParser();p.add_argument('--jdk',type=Path,required=True);p.add_argument('--repository',type=Path,required=True);a=p.parse_args()
    out=S/'validation'/('build-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')+'-'+uuid.uuid4().hex[:6]);out.mkdir()
    def jars(group,artifact,version):
        path=a.repository/Path(group.replace('.','/'))/artifact/version/(artifact+'-'+version+'.jar')
        if not path.exists():raise FileNotFoundError(path)
        return path
    deps=[jars(*x) for x in [('org.mongodb','mongodb-driver-sync','5.13.0'),('org.mongodb','mongodb-driver-core','5.13.0'),('org.mongodb','bson','5.13.0'),('org.slf4j','slf4j-api','2.0.13'),('ch.qos.reload4j','reload4j','1.2.25'),('commons-logging','commons-logging','1.3.1'),('org.apache.logging.log4j','log4j-api','2.23.1'),('org.apache.logging.log4j','log4j-core','2.23.1'),('org.bouncycastle','bcprov-jdk18on','1.78.1'),('com.google.code.findbugs','jsr305','3.0.2')]]
    for art,ver in [('sensitive-word','0.25.0'),('heaven','0.13.0'),('opencc4j','1.8.1'),('nlp-common','0.0.5')]:deps.append(jars('com.github.houbb',art,ver))
    results=[];classes={}
    def compile(name,sources,cp,release):
        target=out/name/'classes';target.mkdir(parents=True)
        argsfile=out/name/'javac.args';args=['-encoding','UTF-8','-proc:none','--release',str(release),'-cp',os.pathsep.join(str(x) for x in cp),'-d',str(target),*map(str,sources)]
        argsfile.write_text('\n'.join('"'+s.replace('\\','/')+'"' for s in args),encoding='utf8')
        run=subprocess.run([str(a.jdk/('bin/javac'+('.exe' if os.name=='nt' else ''))),'@'+str(argsfile)],cwd=R,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
        (out/name/'compile.log').write_bytes(run.stdout)
        result={'module':name,'release':release,'files':len(sources),'exit':run.returncode,'status':'PASS' if run.returncode==0 else 'FAIL'};results.append(result)
        print(name,result['status'],flush=True)
        if run.returncode==0:classes[name]=target
        return target
    def module(name,cp,release=8):return compile(name,sorted((R/name/'src/main/java').rglob('*.java')),cp,release)
    ann=module('mongo-plus-annotation',deps)
    core=module('mongo-plus-core',[ann,*deps]);base=[core,ann,*deps]
    word=module('mongo-plus-sensitive-word',base);shard=module('mongo-plus-sharding',base)
    def spring(version,boot,aspect):
        cp=[jars('org.springframework','spring-'+art,version) for art in ['core','beans','context','aop','expression','tx']]
        if version.startswith('6'):cp.append(jars('org.springframework','spring-jcl',version))
        cp += [jars('org.springframework.boot','spring-boot'+suffix,boot) for suffix in ['', '-autoconfigure']]
        cp += [jars('org.aspectj','aspectjweaver',aspect)]
        if boot.startswith('4'):cp += [jars('org.springframework.boot','spring-boot-'+art,boot) for art in ['mongodb','persistence']]+[jars('org.jspecify','jspecify','1.0.0')]
        return cp
    sp3=spring('6.2.2','3.4.2','1.9.7');boot3=module('mongo-plus-boot-starter',[*base,word,*sp3],17)
    sp4=spring('7.0.2','4.0.1','1.9.25.1');module('mongo-plus-boot4-starter',[*base,word,*sp4],17)
    module('mongo-plus-sharding-boot-starter',[*base,shard,boot3,*sp3],17)
    solon=[jars('org.noear','solon','3.0.1')];module('mongo-plus-solon-plugin',[*base,*solon])
    module('mongo-plus-indexer',[],17)
    tooldeps=[jars('com.github.javaparser','javaparser-core','3.26.3')]+[jars('com.fasterxml.jackson.core',art,'2.18.3') for art in ['jackson-databind','jackson-core','jackson-annotations']]
    module('tools/mongoplus-api-indexer',tooldeps,17)
    compile('annotation-examples',sorted((S/'validation/examples').rglob('*.java')),[*base,word,boot3,*sp3],8)
    runtime_cp=[*base,word,shard,boot3,*sp3,jars('org.yaml','snakeyaml','2.3')]
    (out/'runtime-classpath.txt').write_text(os.pathsep.join(map(str,runtime_cp)),encoding='utf8')
    (out/'results.json').write_text(json.dumps({'jdk':str(a.jdk),'results':results,'classes':{k:str(v) for k,v in classes.items()}},indent=2),encoding='utf8')
    (S/'validation/latest-build.txt').write_text(str(out),encoding='utf8')
    print('BUILD='+str(out));return 0 if all(r['exit']==0 for r in results) else 1
if __name__=='__main__':raise SystemExit(main())
