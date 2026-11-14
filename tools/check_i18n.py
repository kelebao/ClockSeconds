#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Verify i18n consistency across the 21 languages.

Hard gates (exit non-zero on any failure):
  1. Exactly 22 strings.xml files (values + 21 locales).
  2. Every file is well-formed XML: a single <resources> root with no trailing
     garbage (this catches the historical "5 strings merged into one physical
     line" corruption that broke the build).
  3. No duplicate <string name="..."> tags inside any single file.
  4. Every file defines the SAME set of keys as the reference (values-ar).
  5. Every string referenced from code (R.string.*, excluding android.R.string)
     or from XML (@string/* in layouts / manifest) is defined in the base
     values. This catches keys that were lost in ALL locales at once - a class
     of damage that cross-file set-equality alone could not detect, because the
     reference file lost the key too.
  6. README l10n feature-row counts match across all languages.

Informational only (does not fail the build): keys defined in the base but never
referenced anywhere. Those are candidates for future cleanup and are deliberately
left untouched here (no deletion of any in-use copy text).
"""
import os
import re
import sys
import glob
import xml.etree.ElementTree as ET

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')
SRC = os.path.join(ROOT, 'app', 'src')

STRING_TAG_RE = re.compile(r'<string name="([^"]+)">')
# R.string.NAME but NOT android.R.string.NAME (framework resources).
JAVA_REF_RE = re.compile(r'(?<!android\.)R\.string\.([A-Za-z0-9_]+)')
XML_REF_RE = re.compile(r'@string/([A-Za-z0-9_]+)')


def check_files():
    paths = sorted(glob.glob(os.path.join(RES, 'values*', 'strings.xml')))
    if len(paths) != 22:
        print('ERROR: expected 22 strings.xml, found %d' % len(paths))
        return False
    master = None
    master_name = None
    ok = True
    for p in paths:
        dirname = os.path.basename(os.path.dirname(p))
        txt = open(p, encoding='utf-8').read()
        try:
            root = ET.fromstring(txt)
        except ET.ParseError as e:
            print('ERROR: malformed XML in %s: %s' % (dirname, e))
            ok = False
            continue
        names = [el.get('name') for el in root if el.tag == 'string']
        occurrences = STRING_TAG_RE.findall(txt)
        if len(occurrences) != len(set(occurrences)):
            dups = sorted({n for n in occurrences if occurrences.count(n) > 1})
            print('ERROR: duplicate keys in %s: %s' % (dirname, dups))
            ok = False
        keys = set(names)
        if master is None:
            master = keys
            master_name = dirname
            print('reference key count: %d (%s)' % (len(keys), master_name))
            continue
        missing = master - keys
        extra = keys - master
        if missing or extra:
            ok = False
            if missing:
                print('MISSING in %s: %s' % (dirname, sorted(missing)))
            if extra:
                print('EXTRA in %s: %s' % (dirname, sorted(extra)))

    referenced = collect_referenced()
    undefined = sorted(referenced - master)
    if undefined:
        ok = False
        print('UNDEFINED referenced keys (missing from base %s): %s'
              % (master_name, undefined))
    else:
        print('reference coverage OK (%d referenced keys all defined)'
              % len(referenced))
    print('distinct keys across locales: %d' % len(master))
    return ok


def collect_referenced():
    refs = set()
    for p in glob.glob(os.path.join(SRC, '**', '*.java'), recursive=True):
        try:
            txt = open(p, encoding='utf-8', errors='ignore').read()
        except OSError:
            continue
        for m in JAVA_REF_RE.finditer(txt):
            refs.add(m.group(1))
    for p in glob.glob(os.path.join(RES, '**', '*.xml'), recursive=True):
        try:
            txt = open(p, encoding='utf-8', errors='ignore').read()
        except OSError:
            continue
        for m in XML_REF_RE.finditer(txt):
            refs.add(m.group(1))
    return refs


def check_readme_rows():
    counts = {}
    for p in sorted(glob.glob(os.path.join(HERE, 'readme_l10n_*.py'))):
        txt = open(p, encoding='utf-8').read()
        m = re.search(r"'rows':\s*\[(.*?)\n        \],", txt, re.S)
        if not m:
            print('ERROR: cannot find rows block in %s' % os.path.basename(p))
            return False
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
    ok = check_files() and ok
    ok = check_readme_rows() and ok
    if not ok:
        sys.exit(1)
    print('i18n consistency OK')


if __name__ == '__main__':
    main()
