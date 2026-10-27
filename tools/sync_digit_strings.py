#!/usr/bin/env python3
"""把 tools/digit_strings.json 里的 11 个「数字颜色」字符串键幂等注入 22 个 res 目录。

幂等：已存在的键跳过；只在 </resources> 前插入缺失键。
本文件是 tools/sync_strings.py 的补充（该脚本的 TRANS 表没覆盖这批键）。
用法：python3 tools/sync_digit_strings.py [--dry-run]
"""
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")

DIRS = {
    "zh": "", "en": "-en", "zh-TW": "-zh-rTW", "ja": "-ja", "ko": "-ko", "tr": "-tr",
    "ru": "-ru", "vi": "-vi", "th": "-th", "de": "-de", "es": "-es", "fr": "-fr",
    "uk": "-uk", "pl": "-pl", "pt-BR": "-pt-rBR", "fa": "-fa", "ar": "-ar",
    "id": "-in", "it": "-it", "el": "-el", "ka": "-ka",
}


def esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


def main():
    dry = "--dry-run" in sys.argv
    with open(os.path.join(ROOT, "tools", "digit_strings.json"), encoding="utf-8") as f:
        data = json.load(f)
    keys = data["keys"]
    targets = [("values", "zh"), ("values-zh-rCN", "zh")]
    targets += [("values" + suf, lang) for lang, suf in DIRS.items() if lang != "zh"]

    touched = 0
    for folder, lang in targets:
        path = os.path.join(RES, folder, "strings.xml")
        with open(path, encoding="utf-8") as f:
            xml = f.read()
        tr = data["langs"][lang]
        added = []
        for key in keys:
            if ('<string name="%s"' % key) in xml:
                continue
            line = '    <string name="%s">%s</string>\n' % (key, esc(tr[key]))
            xml = xml.replace("</resources>", line + "</resources>")
            added.append(key)
        if added and not dry:
            with open(path, "w", encoding="utf-8", newline="") as f:
                f.write(xml)
        touched += len(added)
        print("%-18s %-6s +%d" % (folder, lang, len(added)))
    print("total inserted: %d (dry-run=%s)" % (touched, dry))


if __name__ == "__main__":
    main()
