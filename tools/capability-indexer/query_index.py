"""Read-only, bounded queries over the existing declaration indexes and source files."""
import argparse
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
p = argparse.ArgumentParser()
p.add_argument('query')
p.add_argument('--source', action='store_true')
p.add_argument('--kind', default='type')
p.add_argument('--limit', type=int, default=40)
p.add_argument('--body', action='store_true')
a = p.parse_args()
pattern = re.compile(a.query, re.I)
n = 0
with (ROOT / 'generated' / ('source-index.jsonl' if a.source else 'api-index.jsonl')).open(encoding='utf-8') as f:
    for line in f:
        r = json.loads(line)
        if r['recordType'] != a.kind or not pattern.search(r['qualifiedName'] + '#' + r.get('signature', r['name'])):
            continue
        print(json.dumps({k: r[k] for k in ('module', 'qualifiedName', 'name', 'signature', 'id', 'sourcePath', 'line', 'endLine', 'extends', 'implements', 'javadoc') if k in r}, ensure_ascii=False))
        if a.body:
            lines = (ROOT / r['sourcePath']).read_text(encoding='utf-8').splitlines()
            print('\n'.join(f'{i+1}: {lines[i]}' for i in range(r['line']-1, min(r['endLine'], r['line']+100))))
        n += 1
        if n >= a.limit:
            break
print(f'matches displayed: {n}')
