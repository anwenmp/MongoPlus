#!/usr/bin/env python3
"""Compile the packaged Java example and run its database-free callback smoke test."""
import argparse
import json
import os
import shutil
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--classpath', required=True, help='Resolved dependency jars joined by the platform path separator')
    parser.add_argument('--java-home', type=Path, default=os.environ.get('JAVA_HOME'))
    args = parser.parse_args()
    suffix = '.exe' if os.name == 'nt' else ''
    binaries = {name: str(args.java_home / 'bin' / (name + suffix)) if args.java_home
                else shutil.which(name) for name in ('java', 'javac')}
    if not all(binaries.values()):
        parser.error('A JDK is required; set --java-home or JAVA_HOME')
    for path in args.classpath.split(os.pathsep):
        if not Path(path).is_file():
            parser.error('Classpath jar is missing: ' + path)
    # All outputs live in a new, owned temporary directory; the source skill stays unchanged.
    with tempfile.TemporaryDirectory(prefix='mongoplus-example-') as temporary:
        classes = Path(temporary) / 'classes'
        classes.mkdir()
        sources = sorted((ROOT / 'skills/mongoplus/assets/command-notification/src').rglob('*.java'))
        commands = [[binaries['javac'], '--release', '8', '-Xlint:-options', '-encoding', 'UTF-8',
                     '-cp', args.classpath, '-d', str(classes)] + [str(path) for path in sources],
                    [binaries['java'], '-cp', str(classes) + os.pathsep + args.classpath,
                     'example.CommandFailureListenerSmokeTest']]
        for command in commands:
            result = subprocess.run(command, capture_output=True, encoding='utf-8', errors='replace')
            if result.returncode:
                parser.exit(result.returncode, result.stdout + result.stderr)
            if result.stdout:
                print(result.stdout.strip())
            if result.stderr:
                print(result.stderr.strip())
    print(json.dumps({'javaCompilation': 'PASS', 'localCallbackSmoke': 'PASS',
                      'springContextStartup': 'NOT_RUN', 'databaseTests': 'NOT_RUN',
                      'liveNotificationDelivery': 'NOT_RUN', 'agentIntegration': 'NOT_RUN'}, indent=2))


if __name__ == '__main__':
    main()
