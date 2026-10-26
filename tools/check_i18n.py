#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Verify i18n consistency across the 21 languages.

Checks:
  1. Every app strings.xml (values*/strings.xml) defines the SAME set of keys.
  2. Every README language has the SAME number of feature-table rows.

Exits non-zero on any inconsistency (suitable for CI).
"""
import os
import re
import sys
import glob

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')


def check_strings():
    paths = sorted(glob.glob(os.path.join(RES, 'values*', 'strings.xml')))
    # values + 21 个语言目录（简体中文自 1.1.0 起显式落到 values-zh-rCN，不再只靠 values 兜底）
    if len(paths) != 22:
        print('ERROR: expected 22 strings.xml, found %d' % len(paths))
        return False
    key_sets = {}
    for p in paths:
        txt = open(p, encoding='utf-8').read()
        keys = set(re.findall(r'<string name="([^"]+)">', txt))
        key_sets[os.path.basename(os.path.dirname(p))] = keys
    master = None
    ok = True
    for d, keys in key_sets.items():
        if master is None:
            master = keys
            print('reference key count: %d (%s)' % (len(keys), d))
            continue
        missing = master - keys
        extra = keys - master
        if missing or extra:
            ok = False
            if missing:
                print('MISSING in %s: %s' % (d, sorted(missing)))
            if extra:
                print('EXTRA in %s: %s' % (d, sorted(extra)))
    return ok


def check_readme_rows():
    # The generator derives language order from LANGS; we just count 'rows' tuples
    # in each readme_l10n_*.py and ensure they match.
    counts = {}
    for p in sorted(glob.glob(os.path.join(HERE, 'readme_l10n_*.py'))):
        txt = open(p, encoding='utf-8').read()
        # crude count of tuples inside the 'rows': [ ... ] block
        m = re.search(r"'rows':\s*\[(.*?)\n        \],", txt, re.S)
        if not m:
            print('ERROR: cannot find rows block in %s' % os.path.basename(p))
            return False
        # count tuples whose first element is a string literal on its own line
        n = len(re.findall(r"^\s*\(", m.group(1), re.M))
        lang = re.search(r"TEXTS = \{\s*'([a-zA-Z\-]+)':", txt)
        counts[lang.group(1) if lang else os.path.basename(p)] = n
    vals = set(counts.values())
    if len(vals) != 1:
        print('ERROR: README feature-row counts differ: %s' % counts)
        return False
    print('README feature rows per language: %d (x%d)' % (list(vals)[0], len(counts)))
    return True


def main():
    ok = True
    ok = check_strings() and ok
    ok = check_readme_rows() and ok
    if not ok:
        sys.exit(1)
    print('i18n consistency OK')


if __name__ == '__main__':
    main()
