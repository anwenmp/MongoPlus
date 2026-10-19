"""Executable offline retrieval checks; never runs Java or MongoDB behavior."""
import copy
import unittest

from generate import load_and_resolve, retrieval
from search import contains, search


class SearchTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.entries = load_and_resolve()[0]

    def test_all_frozen_cases(self):
        rows = retrieval(self.entries)
        self.assertEqual(66, len(rows))
        for row in rows:
            with self.subTest(case=row['id'], suite=row['suite']):
                self.assertTrue(row['passed'], [(h['id'], h['disposition']) for h in row['topMatches']])

    def test_inputs_and_limit_are_stable(self):
        before = copy.deepcopy(self.entries)
        full = search('命令成功后通知', self.entries, limit=48)
        self.assertTrue(full)
        self.assertEqual(full[:1], search('命令成功后通知', self.entries, limit=1))
        self.assertEqual(before, self.entries)
        self.assertEqual([], search('', self.entries))

    def test_insufficient_guarantee_blocks_every_rank(self):
        hits = search('事务提交后恰好一次可靠通知', self.entries, limit=48)
        self.assertEqual('mp.command-events', hits[0]['id'])
        self.assertTrue(all(h['disposition']=='insufficient' and not h['recommendationEligible'] for h in hits))

    def test_optional_fields_are_backward_compatible(self):
        # Simulate a consumer feeding schema 1.0 records without the new fields.
        legacy = copy.deepcopy(self.entries)
        for entry in legacy:
            entry.pop('retrievalPolicy', None)
            entry.pop('manualReview', None)
        hits = search('命令成功后通知', legacy)
        self.assertEqual('mp.command-events', hits[0]['id'])
        self.assertIsInstance(hits, list)
        self.assertTrue(all(h['manualReview'] is None for h in hits))

    def test_english_names_are_whole_tokens(self):
        self.assertTrue(contains('导出 .XLS 文件', 'xls'))
        self.assertFalse(contains('导出 xlsx 文件', 'xls'))
        self.assertFalse(contains('unrelatedlistenername', 'Listener'))


if __name__ == '__main__':
    unittest.main()
