"""Check all skill-local Markdown links without repeating Java compilation."""
import json,re,unicodedata
from pathlib import Path
from urllib.parse import unquote
S=Path(__file__).resolve().parents[1]
def anchor(h):
    h=re.sub(r'\[([^]]+)\]\([^)]+\)',r'\1',h).replace('`','').strip().lower()
    return ''.join(c for c in h if c in ' -_' or unicodedata.category(c)[0] in 'LN').replace(' ','-')
errors=[];count=0
for path in S.rglob('*.md'):
    text=re.sub(r'```[^\n]*\n.*?```','',path.read_text(encoding='utf8'),flags=re.S)
    for dest in re.findall(r'\]\(([^)]+)\)',text):
        if re.match(r'\w+://|mailto:',dest):continue
        target,_,fragment=unquote(dest.strip('<>')).partition('#');resolved=(path.parent/target).resolve() if target else path;count+=1
        if not resolved.is_relative_to(S) or not resolved.exists():errors.append(str(path.relative_to(S))+' -> '+dest);continue
        if fragment and resolved.suffix=='.md':
            anchors={anchor(h) for h in re.findall(r'^#{1,6}\s+(.+?)\s*#*$',resolved.read_text(encoding='utf8'),re.M)}
            if fragment not in anchors:errors.append(str(path.relative_to(S))+' -> '+dest)
result={'links':count,'errors':errors,'status':'PASS' if not errors else 'FAIL'}
(S/'validation/final-links.json').write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf8');print(json.dumps(result,ensure_ascii=False))
raise SystemExit(int(bool(errors)))
