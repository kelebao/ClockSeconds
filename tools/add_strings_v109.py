#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Inject the v1.0.9 new strings (launcher icon + second progress bar) into all
21 languages' strings.xml.

Run:  python tools/add_strings_v109.py
Idempotent: skips keys that already exist in a file.
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')

DIRS = {
    'zh': '', 'en': '-en', 'zh-TW': '-zh-rTW', 'ja': '-ja', 'ko': '-ko',
    'tr': '-tr', 'ru': '-ru', 'vi': '-vi', 'th': '-th', 'de': '-de',
    'es': '-es', 'fr': '-fr', 'uk': '-uk', 'pl': '-pl', 'pt-BR': '-pt-rBR',
    'fa': '-fa', 'ar': '-ar', 'id': '-in', 'it': '-it', 'el': '-el', 'ka': '-ka',
}

TRANS = {
    'zh': ('桌面显示图标', '在桌面生成设置入口；关闭后仍可从 LSPosed 管理器打开',
           '桌面图标已显示', '桌面图标已隐藏'),
    'en': ('Show launcher icon',
           'Add a settings shortcut to the home screen; when off, open it from the LSPosed manager',
           'Launcher icon shown', 'Launcher icon hidden'),
    'zh-TW': ('桌面顯示圖示', '在桌面產生設定入口；關閉後仍可從 LSPosed 管理器開啟',
              '桌面圖示已顯示', '桌面圖示已隱藏'),
    'ja': ('ランチャーにアイコンを表示',
           'ホーム画面に設定へのショートカットを作成。オフでも LSPosed マネージャーから開けます',
           'ランチャーのアイコンを表示しました', 'ランチャーのアイコンを非表示にしました'),
    'ko': ('런처 아이콘 표시',
           '홈 화면에 설정 바로가기를 만듭니다. 꺼도 LSPosed 관리자에서 열 수 있습니다',
           '런처 아이콘을 표시했습니다', '런처 아이콘을 숨겼습니다'),
    'tr': ('Başlatıcı simgesini göster',
           'Ana ekrana ayarlar kısayolu ekle; kapalıyken LSPosed yöneticisinden açılır',
           'Başlatıcı simgesi gösterildi', 'Başlatıcı simgesi gizlendi'),
    'ru': ('Показывать значок в лаунчере',
           'Добавить ярлык настроек на главный экран; если выключено, открывайте из менеджера LSPosed',
           'Значок в лаунчере показан', 'Значок в лаунчере скрыт'),
    'vi': ('Hiện biểu tượng trên màn hình chính',
           'Tạo lối tắt cài đặt trên màn hình chính; khi tắt vẫn mở được từ trình quản lý LSPosed',
           'Đã hiện biểu tượng', 'Đã ẩn biểu tượng'),
    'th': ('แสดงไอคอนบนหน้าจอหลัก',
           'สร้างทางลัดการตั้งค่าบนหน้าจอหลัก ปิดแล้วยังเปิดได้จากตัวจัดการ LSPosed',
           'แสดงไอคอนแล้ว', 'ซ่อนไอคอนแล้ว'),
    'de': ('Symbol im Launcher anzeigen',
           'Verknüpfung zu den Einstellungen auf dem Startbildschirm anlegen; '
           'wenn aus, über den LSPosed-Manager öffnen',
           'Launcher-Symbol eingeblendet', 'Launcher-Symbol ausgeblendet'),
    'es': ('Mostrar icono en el lanzador',
           'Añade un acceso a los ajustes en la pantalla de inicio; '
           'si está desactivado, ábrelo desde el gestor de LSPosed',
           'Icono del lanzador mostrado', 'Icono del lanzador oculto'),
    'fr': ("Afficher l'icône dans le lanceur",
           "Ajouter un raccourci vers les réglages sur l'écran d'accueil ; "
           "si désactivé, ouvrez-les depuis le gestionnaire LSPosed",
           'Icône du lanceur affichée', 'Icône du lanceur masquée'),
    'uk': ('Показувати значок у лаунчері',
           'Додати ярлик налаштувань на головний екран; '
           'якщо вимкнено, відкривайте з менеджера LSPosed',
           'Значок у лаунчері показано', 'Значок у лаунчері приховано'),
    'pl': ('Pokaż ikonę w launcherze',
           'Dodaj skrót do ustawień na ekranie głównym; '
           'gdy wyłączone, otwieraj z menedżera LSPosed',
           'Ikona w launcherze pokazana', 'Ikona w launcherze ukryta'),
    'pt-BR': ('Mostrar ícone no launcher',
              'Adiciona um atalho para as configurações na tela inicial; '
              'se desativado, abra pelo gerenciador do LSPosed',
              'Ícone do launcher exibido', 'Ícone do launcher oculto'),
    'fa': ('نمایش آیکون در لانچر',
           'میان\u200cبر تنظیمات در صفحه اصلی بسازید؛ '
           'وقتی خاموش است از مدیر LSPosed باز کنید',
           'آیکون لانچر نمایش داده شد', 'آیکون لانچر پنهان شد'),
    'ar': ('إظهار الأيقونة في المشغّل',
           'إضافة اختصار للإعدادات على الشاشة الرئيسية؛ '
           'عند الإيقاف افتحه من مدير LSPosed',
           'تم إظهار أيقونة المشغّل', 'تم إخفاء أيقونة المشغّل'),
    'id': ('Tampilkan ikon di peluncur',
           'Tambahkan pintasan pengaturan di layar utama; '
           'jika mati, buka dari pengelola LSPosed',
           'Ikon peluncur ditampilkan', 'Ikon peluncur disembunyikan'),
    'it': ('Mostra icona nel launcher',
           'Aggiunge una scorciatoia alle impostazioni nella schermata iniziale; '
           'se disattivato, aprilo dal gestore LSPosed',
           'Icona del launcher mostrata', 'Icona del launcher nascosta'),
    'el': ('Εμφάνιση εικονιδίου στον εκκινητή',
           'Προσθήκη συντόμευσης ρυθμίσεων στην αρχική οθόνη· '
           'αν είναι ανενεργό, ανοίξτε το από τον διαχειριστή LSPosed',
           'Το εικονίδιο εμφανίστηκε', 'Το εικονίδιο κρύφτηκε'),
    'ka': ('ლაუნჩერში ხატულის ჩვენება',
           'მთავარ ეკრანზე პარამეტრების მალსახმობის დამატება; '
           'გამორთულისას გახსენით LSPosed მენეჯერიდან',
           'ხატული ნაჩვენებია', 'ხატული დამალულია'),
}




def esc(s):
    """Escape for Android strings.xml (single quotes must be escaped)."""
    return s.replace('\\', '\\\\').replace("'", "\\'").replace('"', '\\"')


def main():
    added = 0
    for lang, suffix in DIRS.items():
        path = os.path.join(RES, 'values' + suffix, 'strings.xml')
        if not os.path.exists(path):
            print('  !! missing', path)
            continue
        with open(path, encoding='utf-8') as f:
            src = f.read()
        title, summ, on_txt, off_txt = TRANS[lang]
        block = []
        if 'name="settings_launcher_title"' not in src:
            block.append('    <string name="settings_launcher_title">%s</string>'
                         % esc(title))
        if 'name="settings_launcher_sum"' not in src:
            block.append('    <string name="settings_launcher_sum">%s</string>'
                         % esc(summ))
        if 'name="settings_launcher_on"' not in src:
            block.append('    <string name="settings_launcher_on">%s</string>'
                         % esc(on_txt))
        if 'name="settings_launcher_off"' not in src:
            block.append('    <string name="settings_launcher_off">%s</string>'
                         % esc(off_txt))
        if not block:
            print('  = %s already has keys' % lang)
            continue
        idx = src.rindex('</resources>')
        src = src[:idx] + '\n'.join(block) + '\n' + src[idx:]
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(src)
        added += 1
        print('  + %s (%d key)' % (lang, len(block)))
    print('done, files updated: %d' % added)


if __name__ == '__main__':
    main()
