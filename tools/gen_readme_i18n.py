#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Generate the 21 localized README files for Clock Seconds.

Run:  python tools/gen_readme_i18n.py

Outputs (repo root + docs/):
    README.md            (简体中文, bold at position 1, stays at root)
    docs/README_EN.md    (English)
    docs/README_zh-TW.md ... docs/README_ka.md

Also (re)generates the module-repo copy at
    ../../deliverables/lsposed-repo/README.md
with absolute URLs in the language switch line (the module repo only
keeps one README, so language links point back to the source repo).

Content model:
  * shared skeleton (badges / links / adb & gradle code blocks) lives here
  * per-language slot translations live in readme_l10n_part1..4.py (TEXTS)
  * the "domestic network / Google Maven" details block is intentionally
    NOT part of the template (removed on user request, 2026-10-18)
  * the language switch line is auto-generated: fixed order, 简体中文 first,
    the language of the current file is bolded in place
"""
import glob
import importlib
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
DOCS = os.path.join(ROOT, 'docs')
sys.path.insert(0, HERE)

# every readme_l10n_*.py module exposes a TEXTS dict {lang_code: slots}
TEXTS = {}
for _path in sorted(glob.glob(os.path.join(HERE, 'readme_l10n_*.py'))):
    _mod = importlib.import_module(os.path.splitext(os.path.basename(_path))[0])
    TEXTS.update(_mod.TEXTS)
assert len(TEXTS) == 21, 'expected 21 languages, got %d' % len(TEXTS)

# canonical order of the 21 UI languages (same order as the feature table).
# 简体中文 is always first; the current file's language is bolded in place.
LANGS = [
    ('zh',    'README.md',       '简体中文'),
    ('en',    'README_EN.md',    'English'),
    ('zh-TW', 'README_zh-TW.md', '繁體中文'),
    ('ja',    'README_ja.md',    '日本語'),
    ('ko',    'README_ko.md',    '한국어'),
    ('tr',    'README_tr.md',    'Türkçe'),
    ('ru',    'README_ru.md',    'Русский'),
    ('vi',    'README_vi.md',    'Tiếng Việt'),
    ('th',    'README_th.md',    'ไทย'),
    ('de',    'README_de.md',    'Deutsch'),
    ('es',    'README_es.md',    'Español'),
    ('fr',    'README_fr.md',    'Français'),
    ('uk',    'README_uk.md',    'Українська'),
    ('pl',    'README_pl.md',    'Polski'),
    ('pt-BR', 'README_pt-BR.md', 'Português (Brasil)'),
    ('fa',    'README_fa.md',    'فارسی'),
    ('ar',    'README_ar.md',    'العربية'),
    ('id',    'README_id.md',    'Bahasa Indonesia'),
    ('it',    'README_it.md',    'Italiano'),
    ('el',    'README_el.md',    'Ελληνικά'),
    ('ka',    'README_ka.md',    'ქართული'),
]

REPO = 'https://github.com/kelebao/ClockSeconds'
MODULE_REPO = 'https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds'
RAW = 'https://raw.githubusercontent.com/kelebao/ClockSeconds/main/'

# Badges are wrapped in links so they are clickable:/n#   version -> Releases, lsposed -> module repo, stars -> stargazers, license -> LICENSE
BADGES = (
    '[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)]({repo}/releases/latest)\n'
    '![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)\n'
    '[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)]({mod}/releases)\n'
    '![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)\n'
    '[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)]({repo}/stargazers)\n'
    '[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)]({repo}/blob/main/LICENSE)'
).format(repo=REPO, mod=MODULE_REPO)

ADB_GRANT = ('adb shell pm grant io.github.kelebao.colorosclockseconds '
             'android.permission.WRITE_SECURE_SETTINGS')
BUILD_CODE = 'export JAVA_HOME=/path/to/jdk-17-or-21\n./gradlew assembleDebug'


def shot(name, cur, base=''):
    """Screenshot path under docs/screenshots/.
    base set => module-repo copy: GitHub blob URLs serve an HTML page,
    not image bytes, so images there must use the raw URL."""
    if base:
        return RAW + 'docs/screenshots/' + name
    return ('docs/screenshots/' if cur == 'zh' else 'screenshots/') + name


def lang_line(cur, base=''):
    """Fixed order, 简体中文 first; current language bolded in place.

    简体中文 (README.md) stays at the repo root; every other language lives
    in docs/ next to each other.  Links therefore depend on where the
    current file lives:
      * from the root README.md      -> docs/README_xx.md
      * from docs/README_xx.md       -> README_yy.md (same dir)
      * from docs/ back to 简体中文  -> ../README.md
    """
    parts = []
    for code, fname, name in LANGS:
        if code == cur:
            parts.append('**%s**' % name)
        elif code == 'zh':
            path = 'README.md' if cur == 'zh' else '../README.md'
            parts.append('[%s](%s%s)' % (name, base, path))
        else:
            path = ('docs/' + fname) if cur == 'zh' else fname
            parts.append('[%s](%s%s)' % (name, base, path))
    return ' · '.join(parts)


def bullets(items):
    """Render a list of bullet strings ('\\n' starts a 2-space continuation)."""
    out = []
    for item in items:
        lines = item.split('\n')
        out.append('- ' + lines[0])
        for cont in lines[1:]:
            out.append('  ' + cont)
    return out


def render(t, cur, base=''):
    L = []
    # header + language switch
    L += ['<div align="center">', '', lang_line(cur, base), '', '</div>', '']
    L += ['<div align="center">', '', '# ' + t['title'], '', t['tagline'], '',
          BADGES, '', '</div>', '', '---', '']
    # features
    L += ['## ' + t['h_features'], '',
          '| %s | %s |' % (t['th_feature'], t['th_desc']),
          '|------|------|']
    for name, desc in t['rows']:
        L.append('| %s | %s |' % (name, desc))
    L.append('')
    # install
    L.append('## ' + t['h_install'])
    L.append('')
    for i, step in enumerate(t['install'], 1):
        L.append('%d. %s' % (i, step))
    L += ['', '> ' + t['note1'], '> ```bash', '> ' + ADB_GRANT, '> ```',
          '> ' + t['note2'], '']
    # usage
    L += ['## ' + t['h_usage'], ''] + bullets(t['usage']) + ['']
    # demo screenshots (side-by-side table; contiguous HTML block)
    L += ['## ' + t['h_demo'], '', '<table><tr>']
    for img, cap in (('statusbar-seconds-off.jpg', t['cap_off']),
                     ('statusbar-seconds-on.jpg', t['cap_on'])):
        L += ['<td align="center" width="50%">',
              '<img src="%s" alt="%s" width="100%%">' % (shot(img, cur, base), cap),
              '<br>', cap, '</td>']
    L += ['</tr></table>', '']
    # how it works
    L += ['## ' + t['h_how'], ''] + t['how_intro'] + [''] + bullets(t['how']) + ['']
    # power saving
    L += ['## ' + t['h_power'], ''] + bullets(t['power']) + ['']
    # build
    L += ['## ' + t['h_build'], '', t['build_intro'], '', '```bash',
          BUILD_CODE, '```', '']
    # backup
    L += ['## ' + t['h_backup'], '', t['backup_intro'], '',
          '| %s | %s | %s |' % (t['th_backup_cat'], t['th_backup_files'],
                                t['th_backup_note']),
          '|------|------|------|']
    for cat, files, note in t['backup_rows']:
        L.append('| %s | %s | %s |' % (cat, files, note))
    L.append('')
    # troubleshooting
    L += ['## ' + t['h_trouble'], '', '<details>',
          '<summary>' + t['trouble_summary'] + '</summary>', '',
          t['trouble_intro'], '']
    for item in t['trouble']:
        if item[0] == 'code':
            if L[-1] != '':
                L.append('')
            L.append('  ```bash')
            for cl in item[1]:
                L.append('  ' + cl)
            L.append('  ```')
            L.append('')
        else:
            L += bullets([item[1]])
    while L and L[-1] == '':
        L.pop()
    L += ['', '</details>', '', '## ' + t['h_license'], '',
          '[MIT](LICENSE) © kelebao', '']
    return '\n'.join(L)


def main():
    os.makedirs(DOCS, exist_ok=True)
    for code, fname, _name in LANGS:
        text = render(TEXTS[code], code)
        # 简体中文 stays at the repo root (GitHub renders it directly);
        # all other languages live under docs/ to keep the root clean.
        dest = os.path.join(ROOT if code == 'zh' else DOCS, fname)
        with open(dest, 'w', encoding='utf-8', newline='\n') as f:
            f.write(text)
        print('wrote ' + os.path.relpath(dest, ROOT))
    # module-repo copy: only 简体中文, language links as absolute URLs
    mod_dir = os.path.join(ROOT, '..', 'deliverables', 'lsposed-repo')
    mod_readme = os.path.join(mod_dir, 'README.md')
    if os.path.isdir(mod_dir):
        with open(mod_readme, 'w', encoding='utf-8', newline='\n') as f:
            f.write(render(TEXTS['zh'], 'zh', base=REPO + '/blob/main/'))
        print('wrote deliverables/lsposed-repo/README.md (absolute links)')
    else:
        print('skip module-repo copy (dir missing): ' + mod_dir)


if __name__ == '__main__':
    main()
