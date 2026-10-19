#!/usr/bin/env python3
"""Refresh one skill snapshot from existing reviewed outputs; never regenerate indexes."""
import argparse
import hashlib
import importlib.util
import json
import shutil
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
GENERATED = ('api-index.jsonl', 'source-index.jsonl', 'capability-index.jsonl', 'capability-manifest.json')
CASES = ('retrieval-cases.json', 'retrieval-validation-cases.json', 'retrieval-holdout-cases.json')
EXAMPLE_REFERENCES = (
    ('com.mongoplus.listener.Listener', 'type', 'Listener'),
    ('com.mongoplus.listener.BaseListener', 'method', 'commandFailed'),
    ('com.mongoplus.listener.MongoPlusListener', 'method', 'commandFailed'),
    ('com.mongoplus.model.command.BaseCommand', 'method', 'getCommandName'),
    ('com.mongoplus.model.command.CommandFailed', 'method', 'getThrowable'),
    ('com.mongoplus.config.Configuration', 'method', 'listener'),
)


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(data):
    return hashlib.sha256(data).hexdigest()


def read_jsonl(path):
    return [json.loads(line) for line in path.read_text(encoding='utf-8').splitlines()]


def encode(value):
    return (json.dumps(value, ensure_ascii=False, indent=2, sort_keys=True) + '\n').encode('utf-8')


def refresh(repo, skill):
    generated = repo / 'generated'
    upstream = repo / 'tools/capability-indexer'
    capabilities = read_jsonl(generated / 'capability-index.jsonl')
    source_manifest = json.loads((generated / 'capability-manifest.json').read_text(encoding='utf-8'))
    ids = sorted(c['id'] for c in capabilities)
    require(source_manifest['projectVersion'] == '2.2.0', 'This release only supports reviewed 2.2.0 inputs')
    require(len(ids) == len(set(ids)) == 48, 'Expected 48 unique stable capability IDs')
    previous = skill / 'manifest.json'
    if previous.exists():
        require(ids == json.loads(previous.read_text(encoding='utf-8'))['stableCapabilityIds'],
                'Stable capability IDs changed; an explicit versioned migration is required')
    require(source_manifest['referenceValidation'] == 'PASS', 'Upstream references not validated')
    require(source_manifest['searchSha256'] == digest((upstream / 'search.py').read_bytes()),
            'Upstream engine differs from reviewed manifest')
    # Validate the existing artifacts and reviewed inputs, never run a scanner or LLM.
    for name, expected in source_manifest['outputs'].items():
        require(digest((generated / name).read_bytes()) == expected, 'Upstream artifact drift: ' + name)
    baseline = json.loads((upstream / 'review-baseline.json').read_text(encoding='utf-8'))
    for relative, expected in baseline['files'].items():
        require(digest((repo / relative).read_bytes()) == expected, 'Reviewed input drift: ' + relative)
    indexes = {name: read_jsonl(generated / name) for name in GENERATED if name.endswith('.jsonl')}
    evidence = {}
    for capability in capabilities:
        require(capability['repositoryVersion'] == '2.2.0', 'Mixed index versions')
        for category in ('apiReferences', 'sourceReferences'):
            for ref in capability[category]:
                name = Path(ref['indexPath']).name
                row = indexes[name][ref['indexLine'] - 1]
                for key in ('module', 'recordType', 'qualifiedName', 'name', 'sourcePath', 'line', 'endLine', 'apiVisible'):
                    require(row[key] == ref[key], 'Unresolved reference: ' + str(ref))
                require(row.get('id') == ref.get('recordId'), 'Reference API ID changed')
                if category == 'sourceReferences':
                    add_evidence(repo, evidence, ref['sourcePath'], ref['line'], ref['endLine'],
                                 capability['id'], category, ref['snippetSha256'])
        for ref in capability['registrationReferences']:
            add_evidence(repo, evidence, ref['path'], ref['line'], ref['line'],
                         capability['id'], 'registrationReferences')
        for ref in capability['testReferences']:
            # Test references identify a method line only. Preserve it without inventing a body range.
            add_evidence(repo, evidence, ref['path'], ref['line'], ref['line'],
                         capability['id'], 'testReferences')
    # Small, explicit supplements for the shipped failure-notification example.
    # These locate existing declaration records; they do not discover new capabilities.
    example_references = []
    for owner, kind, name in EXAMPLE_REFERENCES:
        matches = [(number, row) for number, row in enumerate(indexes['source-index.jsonl'], 1)
                   if row['module'] == 'mongo-plus-core' and row['qualifiedName'] == owner
                   and row['recordType'] == kind and row['name'] == name]
        require(len(matches) == 1, 'Missing/ambiguous example declaration: ' + owner + '#' + name)
        number, row = matches[0]
        add_evidence(repo, evidence, row['sourcePath'], row['line'], row['endLine'],
                     'mp.command-events', 'exampleReferences')
        example_references.append({'indexPath': 'generated/source-index.jsonl', 'indexLine': number,
                                   'recordId': row.get('id'), 'sourcePath': row['sourcePath'],
                                   'line': row['line'], 'endLine': row['endLine']})
    # All checks above finish before writing the snapshot.
    for name in GENERATED:
        copy(generated / name, skill / 'data/generated' / name)
    for name in CASES:
        copy(upstream / name, skill / 'data/regression' / name)
    copy(upstream / 'search.py', skill / 'scripts/_vendor/search.py')
    copy(repo / 'LICENSE', skill / 'LICENSE')
    snippets = []
    for key in sorted(evidence):
        item = evidence[key]
        item['capabilities'] = sorted(item['capabilities'])
        item['referenceKinds'] = sorted(item['referenceKinds'])
        snippets.append(json.dumps(item, ensure_ascii=False, sort_keys=True))
    (skill / 'data/evidence.jsonl').write_bytes(('\n'.join(snippets) + '\n').encode('utf-8'))
    # Freeze all files, including instructions and scripts, with deterministic ordering and no timestamp.
    spec = importlib.util.spec_from_file_location('mongoplus_distribute', skill / 'scripts/distribute.py')
    distribution = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(distribution)
    files = {p.relative_to(skill).as_posix(): digest(p.read_bytes()) for p in distribution.payload_files(skill)}
    manifest = {'schemaVersion': '1.0', 'skillVersion': '1.0.0', 'indexedMongoPlusVersion': '2.2.0',
                'supportedMongoPlusVersions': ['2.2.0'], 'pythonMinimum': '3.9',
                'source': {'repository': 'https://gitee.com/aizuda/mongo-plus',
                           'gitHead': source_manifest['gitHead'],
                           'reviewedOn': source_manifest['reviewedOn'],
                           'basis': 'reviewed generated snapshot; gitHead is not a clean-worktree guarantee',
                           'manifest': 'data/generated/capability-manifest.json',
                           'retrievalEngine': 'tools/capability-indexer/search.py'},
                'stableCapabilityIds': ids, 'exampleReferences': example_references,
                'coverage': {'capabilities': len(ids), 'apiRecords': len(indexes['api-index.jsonl']),
                             'sourceRecords': len(indexes['source-index.jsonl']), 'sourceSpans': len(evidence),
                             'evidenceStatus': dict(Counter(c['evidenceStatus'] for c in capabilities)),
                             'regressionCases': sum(len(json.loads((upstream / n).read_text(encoding='utf-8'))) for n in CASES)},
                'files': files}
    (skill / 'manifest.json').write_bytes(encode(manifest))
    distribution.validate(skill)
    return manifest['coverage']


def add_evidence(repo, evidence, relative, start, end, capability, kind, expected=None):
    path = (repo / relative).resolve()
    require(path.is_relative_to(repo.resolve()), 'Source path outside repository')
    lines = path.read_text(encoding='utf-8').splitlines()
    require(1 <= start <= end <= len(lines), 'Invalid source span: ' + relative)
    body = '\n'.join(lines[start - 1:end])
    sha256 = digest(body.encode('utf-8'))
    require(expected is None or expected == sha256, 'Reviewed source span drift: ' + relative)
    key = (relative, start, end)
    if key not in evidence:
        evidence[key] = {'sourcePath': relative, 'line': start, 'endLine': end, 'body': body,
                         'sha256': sha256, 'capabilities': set(), 'referenceKinds': set(),
                         'evidenceLevel': 'static_source_snapshot', 'runtimeTestStatus': 'NOT_RUN'}
    evidence[key]['capabilities'].add(capability)
    evidence[key]['referenceKinds'].add(kind)


def copy(source, destination):
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, destination)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--repo', type=Path, default=ROOT)
    parser.add_argument('--skill', type=Path, default=ROOT / 'skills/mongoplus')
    args = parser.parse_args()
    try:
        coverage = refresh(args.repo.resolve(), args.skill.resolve())
    except (OSError, ValueError, KeyError, IndexError) as error:
        parser.exit(1, str(error) + '\n')
    print(json.dumps({'status': 'PASS', 'coverage': coverage}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
