#!/usr/bin/env python3
"""Bounded, offline queries. Capability ranking is the unmodified upstream engine."""
import argparse
import importlib.util
import json
import re
import sys
from pathlib import Path

SKILL = Path(__file__).resolve().parents[1]
DATA = SKILL / 'data'


def read_jsonl(path):
    with path.open(encoding='utf-8') as stream:
        for number, line in enumerate(stream, 1):
            yield number, json.loads(line)


def load_engine():
    spec = importlib.util.spec_from_file_location('mongoplus_upstream_search',
                                                 SKILL / 'scripts/_vendor/search.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def search_capability(query, limit=3):
    entries = [row for _, row in read_jsonl(DATA / 'generated/capability-index.jsonl')]
    by_id = {entry['id']: entry for entry in entries}
    # Enrich navigation only. Do not rewrite scores, exclusions, ordering or eligibility.
    return [dict(by_id[hit['id']], **hit) for hit in load_engine().search(query, entries, limit)]


def summary(javadoc):
    lines = [re.sub(r'^\s*\* ?', '', line).strip() for line in javadoc.splitlines()]
    return ' '.join(line for line in lines if line and not line.startswith('@'))[:600]


def search_api(query, limit=10, module=None, package=None, kind=None,
               visibility=None, index='api', owner=None):
    tokens = query.casefold().split()
    if not tokens:
        return []
    ranked = []
    filename = 'generated/{}-index.jsonl'.format(index)
    for number, row in read_jsonl(DATA / filename):
        if module and row['module'] != module:
            continue
        if package and not (row['package'] == package or row['package'].startswith(package + '.')):
            continue
        if kind and row['recordType'] != kind:
            continue
        if visibility and row['visibility'] != visibility:
            continue
        if owner and row['qualifiedName'] != owner:
            continue
        names = [str(row.get(key, '')).casefold() for key in
                 ('id', 'qualifiedName', 'name', 'signature')]
        haystack = ' '.join(names + [row.get('javadoc', '').casefold()])
        if not all(token in haystack for token in tokens):
            continue
        q = query.casefold()
        score = (100 if q == names[0] else 0) + (60 if q == names[1] else 0)
        score += (40 if q == names[2] else 0) + sum(5 for token in tokens if any(token in n for n in names))
        result = dict(row, apiId=row.get('id'), javadocSummary=summary(row.get('javadoc', '')),
                      indexReference={'indexPath': filename, 'indexLine': number},
                      accessCategory=('protected_extension' if row['visibility'] == 'protected' else
                                      'internal_implementation' if not row['apiVisible'] else
                                      'public_declaration_requires_capability_context'))
        ranked.append((-score, row['module'], row['qualifiedName'], number, result))
    return [hit[-1] for hit in sorted(ranked, key=lambda hit: hit[:-1])[:limit]]


def show_evidence(capability, limit=3, offset=0, path=None):
    entries = {row['id']: row for _, row in read_jsonl(DATA / 'generated/capability-index.jsonl')}
    if capability not in entries:
        raise ValueError('Unknown capability ID: ' + capability)
    results = [row for _, row in read_jsonl(DATA / 'evidence.jsonl')
               if capability in row['capabilities'] and (not path or path in row['sourcePath'])]
    return {'capability': capability, 'total': len(results), 'offset': offset,
            'results': results[offset:offset + limit]}


def bounded(value):
    number = int(value)
    if not 1 <= number <= 50:
        raise argparse.ArgumentTypeError('limit must be between 1 and 50')
    return number


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='command', required=True)
    capability = sub.add_parser('search-capability', help='Natural language capability discovery')
    capability.add_argument('query')
    capability.add_argument('--limit', type=bounded, default=3)
    api = sub.add_parser('search-api', help='Literal, case-insensitive API lookup; no LLM')
    api.add_argument('query')
    api.add_argument('--limit', type=bounded, default=10)
    api.add_argument('--module')
    api.add_argument('--package')
    api.add_argument('--owner', help='Exact qualified owning type')
    api.add_argument('--kind', choices=['type', 'method', 'constructor', 'field'])
    api.add_argument('--visibility', choices=['public', 'protected', 'private', 'package-private'])
    api.add_argument('--index', choices=['api', 'source'], default='api')
    evidence = sub.add_parser('show-evidence', help='Read only the curated referenced source spans')
    evidence.add_argument('capability')
    evidence.add_argument('--limit', type=bounded, default=3)
    evidence.add_argument('--offset', type=int, default=0)
    evidence.add_argument('--path')
    args = vars(parser.parse_args())
    command = args.pop('command')
    if args.get('offset', 0) < 0:
        parser.error('offset must be nonnegative')
    functions = {'search-capability': search_capability, 'search-api': search_api,
                 'show-evidence': show_evidence}
    try:
        result = functions[command](**args)
    except (ValueError, OSError) as error:
        parser.exit(1, str(error) + '\n')
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
