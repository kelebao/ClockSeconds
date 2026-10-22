#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Inject the new setting/tile strings into all 21 languages' strings.xml.

Run:  python tools/sync_strings.py
Idempotent: only adds keys that are missing; never overwrites existing ones.
"""
import os
import re

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)
RES = os.path.join(ROOT, 'app', 'src', 'main', 'res')

# language code -> resource dir suffix ('' means the default values/ dir)
DIRS = {
    'zh': '', 'en': '-en', 'zh-TW': '-zh-rTW', 'ja': '-ja', 'ko': '-ko',
    'tr': '-tr', 'ru': '-ru', 'vi': '-vi', 'th': '-th', 'de': '-de',
    'es': '-es', 'fr': '-fr', 'uk': '-uk', 'pl': '-pl', 'pt-BR': '-pt-rBR',
    'fa': '-fa', 'ar': '-ar', 'id': '-in', 'it': '-it', 'el': '-el', 'ka': '-ka',
}

# New keys + translations per language (order preserved on insert).
TRANS = {
    'zh': {
        'tile_label': '时间显秒',
        'settings_rule_title': '状态栏时钟识别名',
        'settings_rule_sum': '其它厂商机型未能自动识别时钟时，可在此填写其资源名',
        'settings_rule_set': '设置',
        'settings_rule_msg': '若其它厂商机型未能自动识别状态栏时钟，可填写时钟控件的资源名（如 clock），留空则恢复内置规则。修改后请重启 SystemUI。',
        'settings_rule_apply': '应用',
        'settings_rule_reset': '恢复默认',
        'settings_rule_applied': '识别规则已保存，重启 SystemUI 后生效',
    },
    'en': {
        'tile_label': 'Show Seconds',
        'settings_rule_title': 'Status-bar clock name',
        'settings_rule_sum': 'If the clock is not auto-detected on other devices, enter its resource name here',
        'settings_rule_set': 'Set',
        'settings_rule_msg': 'If the status-bar clock is not auto-detected on other devices, enter its resource name (e.g. clock). Leave empty to use built-in rules. Restart SystemUI after changing.',
        'settings_rule_apply': 'Apply',
        'settings_rule_reset': 'Reset',
        'settings_rule_applied': 'Rule saved. Restart SystemUI to apply.',
    },
    'zh-TW': {
        'tile_label': '時間顯秒',
        'settings_rule_title': '狀態列時鐘識別名',
        'settings_rule_sum': '其它廠商機型未能自動識別時鐘時，可在此填寫其資源名',
        'settings_rule_set': '設定',
        'settings_rule_msg': '若其它廠商機型未能自動識別狀態列時鐘，可填寫時鐘控件的資源名（如 clock），留空則恢復內建規則。修改後請重啟 SystemUI。',
        'settings_rule_apply': '套用',
        'settings_rule_reset': '恢復預設',
        'settings_rule_applied': '識別規則已儲存，重啟 SystemUI 後生效',
    },
    'ja': {
        'tile_label': '秒を表示',
        'settings_rule_title': 'ステータスバーの時計名',
        'settings_rule_sum': '他社端末で時計が自動認識されない場合、リソース名を入力',
        'settings_rule_set': '設定',
        'settings_rule_msg': '他社端末でステータスバーの時計が自動認識されない場合は、リソース名（例: clock）を入力します。空欄なら内蔵ルールを使用。変更後は SystemUI を再起動してください。',
        'settings_rule_apply': '適用',
        'settings_rule_reset': 'リセット',
        'settings_rule_applied': 'ルールを保存しました。SystemUI を再起動して反映。',
    },
    'ko': {
        'tile_label': '초 표시',
        'settings_rule_title': '상태표시줄 시계 이름',
        'settings_rule_sum': '다른 기기에서 시계가 자동 인식되지 않으면 리소스 이름을 입력',
        'settings_rule_set': '설정',
        'settings_rule_msg': '다른 기기에서 상태표시줄 시계가 자동 인식되지 않으면 리소스 이름(예: clock)을 입력하세요. 비우면 내장 규칙을 사용합니다. 변경 후 SystemUI를 재시작하세요.',
        'settings_rule_apply': '적용',
        'settings_rule_reset': '초기화',
        'settings_rule_applied': '규칙을 저장했습니다. SystemUI 재시작 후 적용.',
    },
    'tr': {
        'tile_label': 'Saniye Göster',
        'settings_rule_title': 'Durum çubuğu saat adı',
        'settings_rule_sum': 'Saat diğer cihazlarda algılanmazsa kaynak adını girin',
        'settings_rule_set': 'Ayarla',
        'settings_rule_msg': 'Saat diğer cihazlarda otomatik algılanmazsa kaynak adını (ör. clock) girin. Boş bırakırsanız yerleşik kurallar kullanılır. Değiştikten sonra SystemUI\'yi yeniden başlatın.',
        'settings_rule_apply': 'Uygula',
        'settings_rule_reset': 'Sıfırla',
        'settings_rule_applied': 'Kural kaydedildi. Uygulamak için SystemUI\'yi yeniden başlatın.',
    },
    'ru': {
        'tile_label': 'Показывать секунды',
        'settings_rule_title': 'Имя часов в строке состояния',
        'settings_rule_sum': 'Если часы не определяются на другом устройстве, введите имя ресурса',
        'settings_rule_set': 'Настроить',
        'settings_rule_msg': 'Если часы в строке состояния не определяются на другом устройстве, введите имя ресурса (напр. clock). Пусто — встроенные правила. После изменения перезапустите SystemUI.',
        'settings_rule_apply': 'Применить',
        'settings_rule_reset': 'Сбросить',
        'settings_rule_applied': 'Правило сохранено. Перезапустите SystemUI.',
    },
    'vi': {
        'tile_label': 'Hiện giây',
        'settings_rule_title': 'Tên đồng hồ thanh trạng thái',
        'settings_rule_sum': 'Nếu đồng hồ không tự nhận diện, hãy nhập tên tài nguyên',
        'settings_rule_set': 'Cài đặt',
        'settings_rule_msg': 'Nếu đồng hồ thanh trạng thái không tự nhận diện trên thiết bị khác, hãy nhập tên tài nguyên (vd clock). Để trống dùng quy tắc mặc định. Khởi động lại SystemUI sau khi đổi.',
        'settings_rule_apply': 'Áp dụng',
        'settings_rule_reset': 'Đặt lại',
        'settings_rule_applied': 'Đã lưu quy tắc. Khởi động lại SystemUI để áp dụng.',
    },
    'th': {
        'tile_label': 'แสดงวินาที',
        'settings_rule_title': 'ชื่อนาฬิกาแถบสถานะ',
        'settings_rule_sum': 'หากนาฬิกาไม่ถูกจับคู่โดยอัตโนมัติ ให้ป้อนชื่อทรัพยากร',
        'settings_rule_set': 'ตั้งค่า',
        'settings_rule_msg': 'หากนาฬิกาแถบสถานะไม่ถูกจับคู่โดยอัตโนมัติบนอุปกรณ์อื่น ให้ป้อนชื่อทรัพยากร (เช่น clock) หากว่างจะใช้กฎในตัว เริ่ม SystemUI ใหม่หลังเปลี่ยน',
        'settings_rule_apply': 'ใช้',
        'settings_rule_reset': 'รีเซ็ต',
        'settings_rule_applied': 'บันทึกกฎแล้ว เริ่ม SystemUI ใหม่เพื่อใช้',
    },
    'de': {
        'tile_label': 'Sekunden anzeigen',
        'settings_rule_title': 'Statusleisten-Uhrname',
        'settings_rule_sum': 'Falls die Uhr auf anderen Geräten nicht erkannt wird, Ressourcennamen eingeben',
        'settings_rule_set': 'Einstellen',
        'settings_rule_msg': 'Wird die Uhr auf anderen Geräten nicht erkannt, gib den Ressourcennamen (z. B. clock) ein. Leer = eingebaute Regeln. Danach SystemUI neu starten.',
        'settings_rule_apply': 'Anwenden',
        'settings_rule_reset': 'Zurücksetzen',
        'settings_rule_applied': 'Regel gespeichert. SystemUI neu starten.',
    },
    'es': {
        'tile_label': 'Mostrar segundos',
        'settings_rule_title': 'Nombre del reloj',
        'settings_rule_sum': 'Si el reloj no se detecta, introduce su nombre de recurso',
        'settings_rule_set': 'Configurar',
        'settings_rule_msg': 'Si el reloj de la barra de estado no se detecta en otros dispositivos, introduce su nombre de recurso (p. ej. clock). Vacío = reglas integradas. Reinicia SystemUI después.',
        'settings_rule_apply': 'Aplicar',
        'settings_rule_reset': 'Restablecer',
        'settings_rule_applied': 'Regla guardada. Reinicia SystemUI.',
    },
    'fr': {
        'tile_label': 'Afficher les secondes',
        'settings_rule_title': "Nom de l'horloge",
        'settings_rule_sum': 'Si l\'horloge n\'est pas détectée, saisissez son nom de ressource',
        'settings_rule_set': 'Régler',
        'settings_rule_msg': 'Si l\'horloge de la barre d\'état n\'est pas détectée sur un autre appareil, saisissez son nom de ressource (ex. clock). Vide = règles intégrées. Redémarrez SystemUI après.',
        'settings_rule_apply': 'Appliquer',
        'settings_rule_reset': 'Réinitialiser',
        'settings_rule_applied': 'Règle enregistrée. Redémarrez SystemUI.',
    },
    'uk': {
        'tile_label': 'Показувати секунди',
        'settings_rule_title': 'Назва годинника',
        'settings_rule_sum': 'Якщо годинник не визначається, введіть ім\'я ресурсу',
        'settings_rule_set': 'Налаштувати',
        'settings_rule_msg': 'Якщо годинник у смужці стану не визначається на іншому пристрої, введіть ім\'я ресурсу (напр. clock). Порожньо — вбудовані правила. Після зміни перезапустіть SystemUI.',
        'settings_rule_apply': 'Застосувати',
        'settings_rule_reset': 'Скинути',
        'settings_rule_applied': 'Правило збережено. Перезапустіть SystemUI.',
    },
    'pl': {
        'tile_label': 'Pokaż sekundy',
        'settings_rule_title': 'Nazwa zegara',
        'settings_rule_sum': 'Jeśli zegar nie jest wykrywany, wpisz nazwę zasobu',
        'settings_rule_set': 'Ustaw',
        'settings_rule_msg': 'Jeśli zegar na pasku stanu nie jest wykrywany na innych urządzeniach, wpisz nazwę zasobu (np. clock). Puste = reguły wbudowane. Po zmianie zrestartuj SystemUI.',
        'settings_rule_apply': 'Zastosuj',
        'settings_rule_reset': 'Zresetuj',
        'settings_rule_applied': 'Reguła zapisana. Zrestartuj SystemUI.',
    },
    'pt-BR': {
        'tile_label': 'Mostrar segundos',
        'settings_rule_title': 'Nome do relógio',
        'settings_rule_sum': 'Se o relógio não for detectado, digite o nome do recurso',
        'settings_rule_set': 'Configurar',
        'settings_rule_msg': 'Se o relógio da barra de estado não for detectado em outros dispositivos, digite o nome do recurso (ex. clock). Vazio = regras internas. Reinicie o SystemUI depois.',
        'settings_rule_apply': 'Aplicar',
        'settings_rule_reset': 'Redefinir',
        'settings_rule_applied': 'Regra salva. Reinicie o SystemUI.',
    },
    'fa': {
        'tile_label': 'نمایش ثانیه',
        'settings_rule_title': 'نام ساعت نوار وضعیت',
        'settings_rule_sum': 'اگر ساعت شناسایی نشد، نام منبع را وارد کنید',
        'settings_rule_set': 'تنظیم',
        'settings_rule_msg': 'اگر ساعت نوار وضعیت در دستگاه دیگر شناسایی نشد، نام منبع (مثلا clock) را وارد کنید. خالی = قوانین داخلی. پس از تغییر SystemUI را راه‌اندازی مجدد کنید.',
        'settings_rule_apply': 'اعمال',
        'settings_rule_reset': 'بازنشانی',
        'settings_rule_applied': 'قانون ذخیره شد. SystemUI را راه‌اندازی مجدد کنید.',
    },
    'ar': {
        'tile_label': 'إظهار الثواني',
        'settings_rule_title': 'اسم ساعة شريط الحالة',
        'settings_rule_sum': 'إذا لم يتم التعرف على الساعة، أدخل اسم المورد',
        'settings_rule_set': 'إعداد',
        'settings_rule_msg': 'إذا لم يتم التعرف على ساعة شريط الحالة في جهاز آخر، أدخل اسم المورد (مثل clock). اتركه فارغاً للقواعد المدمجة. أعد تشغيل SystemUI بعد التغيير.',
        'settings_rule_apply': 'تطبيق',
        'settings_rule_reset': 'إعادة تعيين',
        'settings_rule_applied': 'تم حفظ القاعدة. أعد تشغيل SystemUI.',
    },
    'id': {
        'tile_label': 'Tampilkan detik',
        'settings_rule_title': 'Nama jam status bar',
        'settings_rule_sum': 'Jika jam tidak terdeteksi, masukkan nama sumber',
        'settings_rule_set': 'Atur',
        'settings_rule_msg': 'Jika jam status bar tidak terdeteksi di perangkat lain, masukkan nama sumber (mis. clock). Kosong = aturan bawaan. Mulai ulang SystemUI setelah mengubah.',
        'settings_rule_apply': 'Terapkan',
        'settings_rule_reset': 'Atur ulang',
        'settings_rule_applied': 'Aturan disimpan. Mulai ulang SystemUI.',
    },
    'it': {
        'tile_label': 'Mostra secondi',
        'settings_rule_title': 'Nome orologio',
        'settings_rule_sum': 'Se l\'orologio non viene rilevato, inserisci il nome della risorsa',
        'settings_rule_set': 'Imposta',
        'settings_rule_msg': 'Se l\'orologio della barra di stato non viene rilevato su altri dispositivi, inserisci il nome della risorsa (es. clock). Vuoto = regole integrate. Riavvia SystemUI dopo.',
        'settings_rule_apply': 'Applica',
        'settings_rule_reset': 'Ripristina',
        'settings_rule_applied': 'Regola salvata. Riavvia SystemUI.',
    },
    'el': {
        'tile_label': 'Εμφάνιση δευτερολέπτων',
        'settings_rule_title': 'Όνομα ρολογιού',
        'settings_rule_sum': 'Αν το ρολόι δεν εντοπίζεται, εισάγετε το όνομα πόρου',
        'settings_rule_set': 'Ρύθμιση',
        'settings_rule_msg': 'Αν το ρολόι της γραμμής κατάστασης δεν εντοπίζεται σε άλλη συσκευή, εισάγετε το όνομα πόρου (π.χ. clock). Κενό = ενσωματωμένοι κανόνες. Επανεκκινήστε το SystemUI μετά.',
        'settings_rule_apply': 'Εφαρμογή',
        'settings_rule_reset': 'Επαναφορά',
        'settings_rule_applied': 'Ο κανόνας αποθηκεύτηκε. Επανεκκινήστε το SystemUI.',
    },
    'ka': {
        'tile_label': 'წამების ჩვენება',
        'settings_rule_title': 'სტატუსის ზოლის საათის სახელი',
        'settings_rule_sum': 'თუ საათი არ ჩანს, შეიყვანეთ რესურსის სახელი',
        'settings_rule_set': 'პარამეტრები',
        'settings_rule_msg': 'თუ სტატუსის ზოლის საათი სხვა მოწყობილობაზე ავტომატურად არ ჩანს, შეიყვანეთ რესურსის სახელი (მაგ. clock). ცარიელი = ჩაშენებული წესები. ცვლილების შემდეგ გადატვირთეთ SystemUI.',
        'settings_rule_apply': 'გამოყენება',
        'settings_rule_reset': 'გასუფთავება',
        'settings_rule_applied': 'წესი შესრულებულია. გადატვირთეთ SystemUI.',
    },
}


def esc(v):
    # aapt2 要求字符串中的撇号必须转义为 \'（或整串用双引号包裹），
    # 这里统一转义，避免拉丁语系字符串因撇号导致资源编译失败。
    return (v.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            .replace("'", "\\'"))


def main():
    for lang, suffix in DIRS.items():
        path = os.path.join(RES, 'values' + suffix, 'strings.xml')
        if not os.path.isfile(path):
            print('skip (missing): ' + path)
            continue
        with open(path, 'r', encoding='utf-8') as f:
            data = f.read()
        added = []
        for key, val in TRANS[lang].items():
            if 'name="%s"' % key in data:
                continue
            data = data.replace(
                '</resources>',
                '    <string name="%s">%s</string>\n</resources>' % (key, esc(val)),
                1)
            added.append(key)
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(data)
        if added:
            print('%-6s +%d: %s' % (lang, len(added), ', '.join(added)))
        else:
            print('%-6s (up to date)' % lang)


if __name__ == '__main__':
    main()
