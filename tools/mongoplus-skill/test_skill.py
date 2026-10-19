"""Skill structure, frozen retrieval, API contracts and isolated distribution tests."""
import hashlib
import importlib.util
import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SKILL = ROOT / 'skills/mongoplus'


def load(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


query = load('skill_query', SKILL / 'scripts/search.py')
distribution = load('skill_distribution', SKILL / 'scripts/distribute.py')
upstream = load('original_search', ROOT / 'tools/capability-indexer/search.py')


class SkillTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.capabilities = [row for _, row in query.read_jsonl(SKILL / 'data/generated/capability-index.jsonl')]
        cls.manifest = json.loads((SKILL / 'manifest.json').read_text(encoding='utf-8'))

    def test_structure_manifest_and_reference_paths(self):
        self.assertEqual('2.2.0', distribution.validate()['indexedMongoPlusVersion'])
        self.assertEqual(48, len(self.manifest['stableCapabilityIds']))
        self.assertEqual(48, len(set(self.manifest['stableCapabilityIds'])))
        self.assertEqual(5236, self.manifest['coverage']['apiRecords'])
        self.assertEqual(6678, self.manifest['coverage']['sourceRecords'])
        for path in distribution.payload_files():
            if path.suffix in ('.py', '.md', '.json', '.yaml', '.xml', '.java'):
                text = path.read_text(encoding='utf-8')
                self.assertNotIn('D:\\idea_workspace', text)
                self.assertNotIn('D:/idea_workspace', text)
                self.assertNotIn('C:\\Users\\Administrator', text)

    def test_upstream_engine_and_indexes_are_byte_identical(self):
        self.assertEqual((ROOT / 'tools/capability-indexer/search.py').read_bytes(),
                         (SKILL / 'scripts/_vendor/search.py').read_bytes())
        for name in ('api-index.jsonl', 'source-index.jsonl', 'capability-index.jsonl', 'capability-manifest.json'):
            self.assertEqual((ROOT / 'generated' / name).read_bytes(),
                             (SKILL / 'data/generated' / name).read_bytes())

    def test_all_66_frozen_cases_and_exact_upstream_behavior(self):
        count = 0
        for path in sorted((SKILL / 'data/regression').glob('*.json')):
            self.assertEqual((ROOT / 'tools/capability-indexer' / path.name).read_bytes(), path.read_bytes())
            for case in json.loads(path.read_text(encoding='utf-8')):
                count += 1
                with self.subTest(case=case['id']):
                    original = upstream.search(case['query'], self.capabilities)
                    hits = query.search_capability(case['query'])
                    self.assertEqual(original, [{key: hit[key] for key in old} for hit, old in zip(hits, original)])
                    self.assertEqual(len(original), len(hits))
                    expected = ['mp.' + name for name in case['expected']]
                    if not expected:
                        self.assertEqual([], hits)
                        continue
                    self.assertIn(hits[0]['id'], expected)
                    wanted = case.get('expectedDisposition', 'scoped_static_evidence')
                    if wanted == 'insufficient':
                        self.assertTrue(all(not hit['recommendationEligible'] for hit in hits))
                    else:
                        self.assertEqual(wanted, hits[0]['disposition'])
                        self.assertEqual(wanted == 'scoped_static_evidence', hits[0]['recommendationEligible'])
        self.assertEqual(66, count)

    def test_manual_review_is_preserved(self):
        required = {c['id'] for c in self.capabilities if c.get('manualReview', {}).get('status') == 'REQUIRED'}
        self.assertEqual({'mp.dictionary-bind', 'mp.nonnull-validation', 'mp.cross-source-atomic',
                          'mp.block-attack', 'mp.optimistic-lock', 'mp.backup'}, required)
        for capability in self.capabilities:
            if capability['id'] in required:
                self.assertTrue(capability['manualReview']['reasons'])
        hit = query.search_capability('查询状态码后根据字典自动补上状态名称', 1)[0]
        self.assertEqual('candidate', hit['disposition'])
        self.assertFalse(hit['recommendationEligible'])
        self.assertEqual('REQUIRED', hit['manualReview']['status'])
        self.assertTrue(hit['userIntents'])
        self.assertIn('negativeIntents', hit)

    def test_notification_and_excel_safety(self):
        hits = query.search_capability('事务提交后恰好一次可靠通知', 48)
        self.assertEqual('mp.command-events', hits[0]['id'])
        self.assertTrue(all(h['disposition'] == 'insufficient' and not h['recommendationEligible'] for h in hits))
        for phrase in ('Excel', 'xls', 'xlsx'):
            self.assertEqual([], query.search_capability('把查询结果导出为 ' + phrase + ' 文件'))
        self.assertEqual('mp.nested-pipeline', query.search_capability(
            '订单关联客户集合，关联结果按时间排序只取一条', 1)[0]['id'])

    def test_api_exact_signature_id_and_owner(self):
        hits = query.search_api('commandFailed', kind='method', visibility='public',
                                owner='com.mongoplus.listener.Listener')
        self.assertEqual(1, len(hits))
        hit = hits[0]
        self.assertEqual('void commandFailed(CommandFailed commandFailed)', hit['signature'])
        self.assertEqual('void', hit['returnType'])
        self.assertEqual('CommandFailed', hit['parameters'][0]['type'])
        self.assertEqual('mongo-plus-core:com.mongoplus.listener.Listener#commandFailed(CommandFailed)', hit['apiId'])
        self.assertEqual(hit, query.search_api(hit['apiId'])[0])
        types = query.search_api('com.mongoplus.listener.Listener', kind='type')
        self.assertIsNone(types[0]['apiId'])
        self.assertTrue(types[0]['indexReference']['indexLine'] > 0)

    def test_api_filters_and_internal_boundaries(self):
        public = query.search_api('Listener', module='mongo-plus-core', package='com.mongoplus.listener',
                                  kind='method', visibility='public', limit=50)
        self.assertTrue(public)
        self.assertTrue(all(r['module'] == 'mongo-plus-core' and r['package'].startswith('com.mongoplus.listener')
                            and r['recordType'] == 'method' and r['visibility'] == 'public' for r in public))
        internal = next(row for _, row in query.read_jsonl(SKILL / 'data/generated/source-index.jsonl')
                        if not row['apiVisible'] and row.get('id') and row['visibility'] == 'private')
        self.assertEqual([], query.search_api(internal['id']))
        self.assertEqual('internal_implementation', query.search_api(internal['id'], index='source')[0]['accessCategory'])
        protected = next(row for _, row in query.read_jsonl(SKILL / 'data/generated/api-index.jsonl')
                         if row['visibility'] == 'protected' and row.get('id'))
        self.assertEqual('protected_extension', query.search_api(protected['id'], visibility='protected')[0]['accessCategory'])
        self.assertEqual([], query.search_api('NO_SUCH_MONGOPLUS_API'))
        self.assertEqual([], query.search_api(''))
        # Literal regex punctuation may exist in Javadoc; it must not turn into match-all.
        literal = query.search_api('.*', limit=50)
        self.assertTrue(all('.*' in ' '.join(str(r.get(key, '')) for key in
                                           ('id', 'qualifiedName', 'name', 'signature', 'javadoc')) for r in literal))
        self.assertLess(len(literal), 50)
        package_private = query.search_api('Mongo', index='source', visibility='package-private')
        self.assertTrue(package_private)
        self.assertTrue(all(r['visibility'] == 'package-private' for r in package_private))

    def test_all_index_references_and_source_evidence(self):
        indexes = {name: [row for _, row in query.read_jsonl(SKILL / 'data/generated' / name)]
                   for name in ('api-index.jsonl', 'source-index.jsonl')}
        evidence = {(r['sourcePath'], r['line'], r['endLine']): r
                    for _, r in query.read_jsonl(SKILL / 'data/evidence.jsonl')}
        for item in evidence.values():
            self.assertEqual(item['sha256'], hashlib.sha256(item['body'].encode('utf-8')).hexdigest())
            self.assertEqual('NOT_RUN', item['runtimeTestStatus'])
        for capability in self.capabilities:
            for field in ('apiReferences', 'sourceReferences'):
                for ref in capability[field]:
                    row = indexes[Path(ref['indexPath']).name][ref['indexLine'] - 1]
                    for key in ('module', 'name', 'qualifiedName', 'recordType', 'sourcePath', 'line', 'endLine'):
                        self.assertEqual(ref[key], row[key])
                    self.assertEqual(ref.get('recordId'), row.get('id'))
                    if field == 'sourceReferences':
                        item = evidence[(ref['sourcePath'], ref['line'], ref['endLine'])]
                        self.assertEqual(ref['snippetSha256'], item['sha256'])
                        self.assertIn(capability['id'], item['capabilities'])

    def test_evidence_paging_and_unknown_id(self):
        first = query.show_evidence('mp.command-events', 1)
        second = query.show_evidence('mp.command-events', 1, 1)
        self.assertGreater(first['total'], 1)
        self.assertNotEqual(first['results'], second['results'])
        self.assertTrue(query.show_evidence('mp.command-events', path='MongoPlusListener')['results'])
        snippets = query.show_evidence('mp.command-events', limit=50, path='MongoPlusListener')['results']
        self.assertTrue(any('commandFailed(commandFailed)' in snippet['body'] for snippet in snippets))
        with self.assertRaises(ValueError):
            query.show_evidence('mp.does-not-exist')

    def test_isolated_codex_and_claude_installs(self):
        with tempfile.TemporaryDirectory(prefix='mongoplus-skill-') as temporary:
            project = Path(temporary) / 'project with spaces and 中文'
            project.mkdir()
            for client, folder in (('codex', '.agents'), ('claude', '.claude')):
                distribution.install(client, project)
                installed = project / folder / 'skills/mongoplus'
                distribution.validate(installed)
                command = [sys.executable, '-I', str(installed / 'scripts/search.py'),
                           'search-capability', '命令成功后通知', '--limit', '1']
                result = subprocess.run(command, cwd=temporary, capture_output=True, encoding='utf-8', check=True)
                self.assertEqual('mp.command-events', json.loads(result.stdout)[0]['id'])
                result = subprocess.run([sys.executable, '-I', str(installed / 'scripts/search.py'),
                                         'search-api', 'commandFailed', '--owner', 'com.mongoplus.listener.Listener'],
                                        cwd=temporary, capture_output=True, encoding='utf-8', check=True)
                self.assertEqual(1, len(json.loads(result.stdout)))
                with self.assertRaises(ValueError):
                    distribution.install(client, project)
            installed = project / '.agents/skills/mongoplus'
            with (installed / 'data/generated/api-index.jsonl').open('ab') as stream:
                stream.write(b'\n')
            with self.assertRaises(ValueError):
                distribution.validate(installed)

    def test_deterministic_package_roundtrip(self):
        with tempfile.TemporaryDirectory(prefix='mongoplus-package-') as temporary:
            first = distribution.pack(Path(temporary) / 'first.zip')
            second = distribution.pack(Path(temporary) / 'second.zip')
            self.assertEqual(first['sha256'], second['sha256'])
            import zipfile
            with zipfile.ZipFile(first['archive']) as archive:
                self.assertTrue(all(name.startswith('mongoplus/') for name in archive.namelist()))
                self.assertFalse(any('__pycache__' in name or '/target/' in name for name in archive.namelist()))
                archive.extractall(Path(temporary) / 'unpacked')
            distribution.validate(Path(temporary) / 'unpacked/mongoplus')

    def test_agent_task_matrix_requires_explicit_results(self):
        cases = json.loads((SKILL / 'references/agent-cases.json').read_text(encoding='utf-8'))
        self.assertGreaterEqual(len(cases['cases']), 10)
        allowed = {'PASS', 'FAIL', 'NOT_RUN', 'N/A'}
        self.assertIn(cases['status'], allowed)
        self.assertEqual(len(cases['cases']), len({case['id'] for case in cases['cases']}))
        for case in cases['cases']:
            self.assertTrue(case['prompt'])
            self.assertTrue(case['expectedBehavior'])
            self.assertEqual({'discovered', 'automaticTrigger', 'retrievalScriptExecuted', 'apiRead',
                              'correctChoice', 'codeCompiles', 'runtimeTests'}, set(case['results']))
            self.assertTrue(all(value in allowed for value in case['results'].values()))


if __name__ == '__main__':
    unittest.main()
