"""Compile and run dedicated-collection verification without exposing connection values."""
import argparse,datetime,json,os,subprocess,uuid
from pathlib import Path
S=Path(__file__).resolve().parents[1];R=S.parent
def main():
    p=argparse.ArgumentParser();p.add_argument('--jdk',type=Path,required=True);p.add_argument('--config',type=Path,default=R/'mongo-plus-test/src/test/resources/application.yml');a=p.parse_args()
    out=S/'validation'/('live-'+datetime.datetime.now(datetime.timezone.utc).strftime('%Y%m%dT%H%M%SZ')+'-'+uuid.uuid4().hex[:6]);out.mkdir();classes=out/'classes';classes.mkdir()
    build=Path((S/'validation/latest-build.txt').read_text(encoding='utf8').strip());cp=(build/'runtime-classpath.txt').read_text(encoding='utf8').strip()
    suffix='.exe' if os.name=='nt' else ''
    cmd=[str(a.jdk/('bin/javac'+suffix)),'-J-Duser.language=en','-encoding','UTF-8','-proc:none','--release','8','-cp',cp,'-d',str(classes),str(S/'validation/LiveValidation.java'),*map(str,sorted((S/'validation/examples').rglob('*.java')))]
    result=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);(out/'compile.log').write_bytes(result.stdout)
    if result.returncode:
        print('Live fixture compile FAIL: '+str(out/'compile.log'));return 1
    with (out/'run.log').open('wb') as log:
        result=subprocess.run([str(a.jdk/('bin/java'+suffix)),'-Duser.language=en','-cp',str(classes)+os.pathsep+cp,'LiveValidation',str(a.config),str(out)],stdout=log,stderr=subprocess.STDOUT)
    (S/'validation/latest-live.txt').write_text(str(out),encoding='utf8')
    data=json.loads((out/'results.json').read_text(encoding='utf8')) if (out/'results.json').exists() else {'cases':[]}
    summary={'exit':result.returncode,'cases':len(data['cases']),'statuses':{status:sum(x['status']==status for x in data['cases']) for status in {x['status'] for x in data['cases']}},'output':str(out)}
    print(json.dumps(summary));return result.returncode
if __name__=='__main__':raise SystemExit(main())
