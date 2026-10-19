"""Small offline lexical baseline; scores are relevance scores, never support confidence."""
import argparse
import json
import math
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]

def normalize(text):
    return re.sub(r'\s+', '', text.lower())

def tokens(text):
    result = set(re.findall(r'[a-z][a-z0-9]+', text.lower()))
    for part in re.findall(r'[\u4e00-\u9fff]+', text):
        result.update(part[i:i+2] for i in range(len(part)-1))
    return result

def contains(query, phrase):
    # English API/format names are whole tokens: xls must not match xlsx/other words.
    if re.fullmatch(r'[a-z0-9]+(?: [a-z0-9]+)*', phrase.lower()):
        return bool(re.search(r'(?<![a-z0-9])'+re.escape(phrase.lower())+r'(?![a-z0-9])', query.lower()))
    return normalize(phrase) in normalize(query)

def positive_scope(query):
    # Only explicit excluded requirement clauses, bounded by punctuation. This is
    # deliberately not general negation: e.g. 不要覆盖 is a positive lock need.
    return re.sub(r'(?:不涉及|不需要)[^，。；,;！!？?\n]*', '', query)

def matches_groups(query, groups):
    return bool(groups) and all(any(contains(query, phrase) for phrase in group) for group in groups)

def unsupported_intents(query, capabilities):
    result = []
    for c in capabilities:
        for intent in c.get('negativeIntents', []):
            if contains(query, intent):
                result.append(dict(capability=c['id'], intent=intent))
        for groups in c.get('retrievalPolicy', {}).get('unsupportedIntentGroups', []):
            if matches_groups(query, groups):
                result.append(dict(capability=c['id'], intent=' + '.join('/'.join(g) for g in groups)))
    return result

def search(query, capabilities, limit=3):
    query = positive_scope(query)
    # A required output format with no catalog support cannot be satisfied by
    # another partial task such as DTO mapping. Preserve the list-return contract.
    if any(matches_groups(query, groups) for c in capabilities
           for groups in c.get('retrievalPolicy', {}).get('noMatchIntentGroups', [])):
        return []
    fields = [c['name']+' '+c['description']+' '+' '.join(c['keywords']+c['userIntents']) for c in capabilities]
    docs = [tokens(f) for f in fields]
    df = Counter(term for doc in docs for term in doc)
    qt = tokens(query)
    # A known unsupported guarantee applies even when lexical ranking chooses
    # another nearby capability. This guard uses the catalog, never test expectations.
    unsupported = unsupported_intents(query, capabilities)
    ranked = []
    for c, doc in zip(capabilities, docs):
        policy = c.get('retrievalPolicy', {})
        if any(contains(query, s) for s in policy.get('excludedAny', [])):
            continue
        if policy.get('requiredAny') and not any(contains(query, s) for s in policy['requiredAny']):
            continue
        overlap = qt & doc
        score = sum(math.log(1+len(capabilities)/(1+df[w])) for w in overlap)
        phrases = [w for w in c['keywords'] if len(normalize(w)) >= 2 and contains(query, w)]
        score += sum(min(12, len(normalize(w))*1.5) for w in phrases)
        score += len(qt & tokens(c['name']))*1.5
        # A matched unsupported intent is strong navigation evidence for its owner,
        # while the guarantee guard still prohibits recommending every result.
        if any(u['capability'] == c['id'] for u in unsupported):
            score += 12
        if score < 5:
            continue
        excluded = unsupported
        eligible = c['evidenceStatus'] in ('documented','source_verified') and not excluded
        ranked.append(dict(id=c['id'], name=c['name'], score=round(score,4),
                           evidenceStatus=c['evidenceStatus'], recommendationEligible=eligible,
                           disposition=('insufficient' if excluded else
                                        'candidate' if c['evidenceStatus']=='candidate' else
                                        'internal_only' if c['evidenceStatus']=='internal_only' else 'scoped_static_evidence'),
                           matchedKeywords=phrases, matchedTokens=sorted(overlap),
                           excludedIntents=excluded, limitations=c['limitations'],
                           manualReview=c.get('manualReview'),
                           apiReferences=c['apiReferences'], sourceReferences=c['sourceReferences'],
                           testReferences=c['testReferences']))
    return sorted(ranked, key=lambda r:(-r['score'],r['id']))[:limit]

if __name__ == '__main__':
    p = argparse.ArgumentParser()
    p.add_argument('query')
    p.add_argument('--limit', type=int, default=3)
    a = p.parse_args()
    with (ROOT/'generated/capability-index.jsonl').open(encoding='utf-8') as f:
        capabilities = [json.loads(line) for line in f]
    print(json.dumps(search(a.query, capabilities, a.limit), ensure_ascii=False, indent=2))
