#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Remove the deleted "breathing seconds" setting strings (settings_breath_title /
settings_breath_sum) from all languages' strings.xml.

The setting was deleted; the breathing effect now lives on the background plate
(float window + settings preview) and has no toggle / no UI strings.

Run:  python tools/remove_breath_strings.py
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')

KEYS = ('settings_breath_title', 'settings_breath_sum')
RE_KEY = re.compile(r'^\s*<string name="(?:%s)">.*</string>\s*$' % '|'.join(KEYS))


def main():
    removed_total = 0
    files_touched = 0
    for dirpath, _dirnames, filenames in os.walk(RES):
        for fn in filenames:
            if fn != 'strings.xml':
                continue
            path = os.path.join(dirpath, fn)
            with open(path, encoding='utf-8') as f:
                lines = f.readlines()
            new_lines = [ln for ln in lines if not RE_KEY.match(ln)]
            removed = len(lines) - len(new_lines)
            if removed:
                with open(path, 'w', encoding='utf-8', newline='') as f:
                    f.writelines(new_lines)
                removed_total += removed
                files_touched += 1
                print('  - %s (%d)' % (os.path.relpath(path, ROOT), removed))
    print('done: %d keys removed across %d files' % (removed_total, files_touched))


if __name__ == '__main__':
    main()
