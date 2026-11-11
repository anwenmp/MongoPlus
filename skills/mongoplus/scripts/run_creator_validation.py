"""Run the unmodified creator validator with existing SnakeYAML, without installing Python packages.

This adapter implements only the validator's safe_load/YAMLError dependency using a real safe YAML
parser. The report must distinguish this from a native PyYAML execution (not available here).
"""
import argparse,json,os,runpy,subprocess,sys,types
from pathlib import Path
S=Path(__file__).resolve().parents[1]
def main():
    p=argparse.ArgumentParser();p.add_argument('--validator',type=Path,required=True);p.add_argument('--jdk',type=Path,required=True);p.add_argument('--snakeyaml',type=Path,required=True);a=p.parse_args()
    out=S/'validation/creator-validator';out.mkdir(exist_ok=True)
    suffix='.exe' if os.name=='nt' else ''
    cp=str(out)+os.pathsep+str(a.snakeyaml)
    run=subprocess.run([str(a.jdk/('bin/javac'+suffix)),'-encoding','UTF-8','-proc:none','-cp',str(a.snakeyaml),'-d',str(out),str(S/'scripts/SourceInventory.java'),str(S/'validation/YamlFrontmatter.java')],stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    (out/'compile.log').write_bytes(run.stdout)
    if run.returncode:raise RuntimeError('YAML adapter compilation failed; see compile.log')
    yaml=types.ModuleType('yaml')
    class YAMLError(Exception):pass
    def safe_load(value):
        result=subprocess.run([str(a.jdk/('bin/java'+suffix)),'-cp',cp,'YamlFrontmatter'],input=value.encode('utf8'),stdout=subprocess.PIPE,stderr=subprocess.PIPE)
        if result.returncode:raise YAMLError('Safe YAML parsing failed')
        return json.loads(result.stdout)
    yaml.safe_load=safe_load;yaml.YAMLError=YAMLError;sys.modules['yaml']=yaml
    namespace=runpy.run_path(str(a.validator));valid,message=namespace['validate_skill'](S)
    result={'valid':valid,'message':message,'validator':str(a.validator),'validatorModified':False,'yamlBackend':'existing SnakeYAML 2.3 SafeConstructor; no install','nativePyYaml':'NOT_RUN (dependency absent)'}
    (out/'results.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8');print(message);return int(not valid)
if __name__=='__main__':raise SystemExit(main())
