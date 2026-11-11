"""Compile independently authored Agent answers; this does not prove container or DB behavior."""
import argparse,json,os,subprocess
from pathlib import Path
S=Path(__file__).resolve().parents[1]
def main():
    p=argparse.ArgumentParser();p.add_argument('--jdk',type=Path,required=True);a=p.parse_args()
    build=Path((S/'validation/latest-build.txt').read_text(encoding='utf8').strip());cp=(build/'runtime-classpath.txt').read_text(encoding='utf8').strip()
    out=S/'validation/blind-compile';out.mkdir(exist_ok=True);sources=sorted((S/'validation/blind').glob('*.java'))
    if not sources:raise RuntimeError('No independent answers')
    results=[]
    for source in sources:
        target=out/source.stem;target.mkdir(exist_ok=True)
        cmd=[str(a.jdk/('bin/javac'+('.exe' if os.name=='nt' else ''))),'-J-Duser.language=en','-encoding','UTF-8','-proc:none','--release','8','-cp',cp,'-d',str(target),str(source)]
        run=subprocess.run(cmd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT);(target/'compile.log').write_bytes(run.stdout)
        results.append({'source':source.relative_to(S).as_posix(),'exit':run.returncode,'status':'COMPILE_PASS' if run.returncode==0 else 'FAIL'})
    (out/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2),encoding='utf8');print(json.dumps(results));return int(any(x['exit'] for x in results))
if __name__=='__main__':raise SystemExit(main())
