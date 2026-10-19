#!/usr/bin/env python3
"""Validate, install or deterministically package this self-contained skill."""
import argparse
import hashlib
import json
import re
import shutil
import sys
import zipfile
from pathlib import Path

SKILL = Path(__file__).resolve().parents[1]


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def payload_files(root=SKILL):
    return sorted((p for p in root.rglob('*') if p.is_file() and not any(
        part in ('__pycache__', 'target') or part.startswith('.') for part in p.relative_to(root).parts)
        and p.suffix != '.pyc' and p.name != 'manifest.json'), key=lambda p: p.relative_to(root).as_posix())


def validate(root=SKILL):
    text = (root / 'SKILL.md').read_text(encoding='utf-8')
    match = re.match(r'\A---\n(.*?)\n---\n', text, re.S)
    if not match or not re.search(r'^name: mongoplus$', match[1], re.M) or 'description: >-' not in match[1]:
        raise ValueError('Invalid mongoplus SKILL.md frontmatter')
    manifest = json.loads((root / 'manifest.json').read_text(encoding='utf-8'))
    actual = {p.relative_to(root).as_posix(): sha(p) for p in payload_files(root)}
    if actual != manifest['files']:
        raise ValueError('Payload missing, changed or unexpected; refresh the manifest before distribution')
    for path in [root / 'SKILL.md', root / 'README.md'] + list((root / 'references').glob('*.md')):
        for target in re.findall(r'\]\(([^)]+)\)', path.read_text(encoding='utf-8')):
            if target.startswith(('https://', 'http://', '#')):
                continue
            resolved = (path.parent / target.split('#', 1)[0]).resolve()
            if not resolved.is_relative_to(root.resolve()) or not resolved.is_file():
                raise ValueError('Missing or nonportable reference: ' + target)
    return manifest


def install(client, project):
    validate()
    target = project.resolve() / ('.agents' if client == 'codex' else '.claude') / 'skills/mongoplus'
    if target.exists() or target.is_symlink():
        raise ValueError('Destination already exists; review it and install into a new location: ' + str(target))
    # Copy the allowlisted payload, never recursively delete or overwrite user files.
    target.mkdir(parents=True)
    for relative in sorted(list(validate()['files']) + ['manifest.json']):
        destination = target / relative
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(SKILL / relative, destination)
    validate(target)
    return {'client': client, 'installed': str(target), 'status': 'PASS'}


def pack(output):
    manifest = validate()
    output = output.resolve()
    if output.is_relative_to(SKILL.resolve()):
        raise ValueError('Write archives outside the skill directory')
    if output.exists():
        raise ValueError('Archive already exists; choose a fresh output path')
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'x', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for relative in sorted(list(manifest['files']) + ['manifest.json']):
            info = zipfile.ZipInfo('mongoplus/' + relative, date_time=(2020, 1, 1, 0, 0, 0))
            info.create_system = 3
            info.external_attr = 0o100644 << 16
            info.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(info, (SKILL / relative).read_bytes(), compresslevel=9)
    return {'archive': str(output), 'sha256': sha(output), 'status': 'PASS'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest='command', required=True)
    sub.add_parser('validate')
    installer = sub.add_parser('install')
    installer.add_argument('--client', required=True, choices=['codex', 'claude'])
    installer.add_argument('--project', type=Path, required=True)
    packer = sub.add_parser('pack')
    packer.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    try:
        if args.command == 'install':
            result = install(args.client, args.project)
        elif args.command == 'pack':
            result = pack(args.output)
        else:
            manifest = validate()
            result = {'status': 'PASS', 'files': len(manifest['files']),
                      'indexVersion': manifest['indexedMongoPlusVersion']}
    except (OSError, ValueError) as error:
        parser.exit(1, str(error) + '\n')
    if hasattr(sys.stdout, 'reconfigure'):
        sys.stdout.reconfigure(encoding='utf-8')
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
