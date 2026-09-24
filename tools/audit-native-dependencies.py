#!/usr/bin/env python3
"""Inventory crate/version source paths from an AAR, without exposing build paths.

This is a lower bound, not a complete SBOM. An absent match is not proof of absence.
Use the resulting coordinates with OSV; inspect vendor patches before concluding exposure.
"""
import argparse
import hashlib
import json
import re
import zipfile
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('aar', type=Path)
args = parser.parse_args()
pattern = re.compile(rb'/([a-zA-Z0-9_-]+)-(\d+\.\d+\.\d+(?:-[a-zA-Z0-9.]+)?)/src/')
rows = []
with zipfile.ZipFile(args.aar) as archive:
    for name in sorted(archive.namelist()):
        if not name.endswith('.so'):
            continue
        data = archive.read(name)
        dependencies = sorted(set((n.decode(), v.decode()) for n, v in pattern.findall(data)))
        rows.append({'library': name, 'sha256': hashlib.sha256(data).hexdigest(),
                     'crates': [{'name': n, 'version': v} for n, v in dependencies]})
print(json.dumps({'aarSha256': hashlib.sha256(args.aar.read_bytes()).hexdigest(),
                  'completeSbom': False, 'libraries': rows}, indent=2))
