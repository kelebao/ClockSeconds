#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""一次性补丁：新增自动检测弹窗相关文案 + 中文/英文「时钟→时间」措辞统一。

Run:  python tools/patch_rule_strings.py
幂等：已存在的新键跳过；UPDATE 每次都会覆盖（保证措辞一致）。
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

# ---- 新增键（21 语言）----
NEW = {
    'zh': {
        'settings_rule_dialog_desc': '不懂设置？点下方「自动检测」即可一键完成。也可手动填写时间控件的资源名，留空则使用内置规则。',
        'settings_rule_or_manual': '或手动填写时间控件资源名',
        'settings_rule_confirm_title': '识别名可能无效',
        'settings_rule_confirm_msg': '「%1$s」看起来不像时间控件的资源名（通常包含 clock 或 time），可能无法生效。建议先点「自动检测」。仍要保存吗？',
        'settings_rule_confirm_save': '仍要保存',
    },
    'en': {
        'settings_rule_dialog_desc': 'Not sure? Tap “Auto detect” below to finish in one tap. You can also enter the time widget’s resource name; leave empty for built-in rules.',
        'settings_rule_or_manual': 'Or enter the time widget’s resource name manually',
        'settings_rule_confirm_title': 'Name may be invalid',
        'settings_rule_confirm_msg': '“%1$s” doesn’t look like a time widget’s resource name (usually contains “clock” or “time”) and may not work. Try “Auto detect” first. Save anyway?',
        'settings_rule_confirm_save': 'Save anyway',
    },
    'zh-TW': {
        'settings_rule_dialog_desc': '不懂設定？點下方「自動偵測」即可一鍵完成。也可手動填寫時間控件的資源名，留空則使用內建規則。',
        'settings_rule_or_manual': '或手動填寫時間控件資源名',
        'settings_rule_confirm_title': '識別名可能無效',
        'settings_rule_confirm_msg': '「%1$s」看起來不像時間控件的資源名（通常包含 clock 或 time），可能無法生效。建議先點「自動偵測」。仍要儲存嗎？',
        'settings_rule_confirm_save': '仍要儲存',
    },
    'ja': {
        'settings_rule_dialog_desc': 'わからない場合は下の「自動検出」でワンタップ完了。手動で時刻ウィジェットのリソース名を入力することもできます（空欄なら内蔵ルール）。',
        'settings_rule_or_manual': 'または時刻ウィジェットのリソース名を手動入力',
        'settings_rule_confirm_title': '名前が無効の可能性',
        'settings_rule_confirm_msg': '「%1$s」は時刻ウィジェットのリソース名らしくありません（通常 clock や time を含みます）。有効でない可能性があります。先に「自動検出」をお試しください。それでも保存しますか？',
        'settings_rule_confirm_save': 'それでも保存',
    },
    'ko': {
        'settings_rule_dialog_desc': '모르시겠나요? 아래 “자동 감지”로 한 번에 완료하세요. 시간 위젯의 리소스 이름을 직접 입력할 수도 있습니다(비우면 내장 규칙).',
        'settings_rule_or_manual': '또는 시간 위젯 리소스 이름 직접 입력',
        'settings_rule_confirm_title': '이름이 유효하지 않을 수 있음',
        'settings_rule_confirm_msg': '“%1$s”은(는) 시간 위젯 리소스 이름처럼 보이지 않습니다(보통 clock 또는 time 포함). 적용되지 않을 수 있습니다. 먼저 “자동 감지”를 사용해 보세요. 그래도 저장할까요?',
        'settings_rule_confirm_save': '그래도 저장',
    },
    'tr': {
        'settings_rule_dialog_desc': 'Bilemiyor musunuz? Aşağıdaki “Otomatik algıla” ile tek dokunuşta tamamlayın. Saat bileşeninin kaynak adını elle de girebilirsiniz (boş = yerleşik kurallar).',
        'settings_rule_or_manual': 'Veya saat bileşeninin kaynak adını elle girin',
        'settings_rule_confirm_title': 'Ad geçersiz olabilir',
        'settings_rule_confirm_msg': '“%1$s” bir saat bileşeni kaynak adına benzemiyor (genellikle clock veya time içerir) ve çalışmayabilir. Önce “Otomatik algıla”yı deneyin. Yine de kaydedilsin mi?',
        'settings_rule_confirm_save': 'Yine de kaydet',
    },
    'ru': {
        'settings_rule_dialog_desc': 'Не знаете? Нажмите «Автоопределение» ниже — готово в один тап. Можно также ввести имя ресурса виджета времени (пусто = встроенные правила).',
        'settings_rule_or_manual': 'Или введите имя ресурса виджета времени вручную',
        'settings_rule_confirm_title': 'Имя может быть неверным',
        'settings_rule_confirm_msg': '«%1$s» не похоже на имя ресурса виджета времени (обычно содержит clock или time) и может не сработать. Сначала попробуйте «Автоопределение». Всё равно сохранить?',
        'settings_rule_confirm_save': 'Всё равно сохранить',
    },
    'vi': {
        'settings_rule_dialog_desc': 'Không rõ? Nhấn “Tự động phát hiện” bên dưới để hoàn tất một chạm. Bạn cũng có thể nhập tên tài nguyên của widget thời gian (để trống = quy tắc mặc định).',
        'settings_rule_or_manual': 'Hoặc tự nhập tên tài nguyên widget thời gian',
        'settings_rule_confirm_title': 'Tên có thể không hợp lệ',
        'settings_rule_confirm_msg': '“%1$s” không giống tên tài nguyên widget thời gian (thường chứa clock hoặc time) và có thể không hoạt động. Hãy thử “Tự động phát hiện” trước. Vẫn lưu?',
        'settings_rule_confirm_save': 'Vẫn lưu',
    },
    'th': {
        'settings_rule_dialog_desc': 'ไม่แน่ใจ? แตะ “ตรวจหาอัตโนมัติ” ด้านล่างเพื่อเสร็จในแตะเดียว หรือจะป้อนชื่อทรัพยากรของวิดเจ็ตเวลาก็ได้ (ว่าง = กฎในตัว)',
        'settings_rule_or_manual': 'หรือป้อนชื่อทรัพยากรวิดเจ็ตเวลาด้วยตนเอง',
        'settings_rule_confirm_title': 'ชื่ออาจไม่ถูกต้อง',
        'settings_rule_confirm_msg': '“%1$s” ไม่ดูเหมือนชื่อทรัพยากรวิดเจ็ตเวลา (มักมี clock หรือ time) และอาจไม่ทำงาน ลอง “ตรวจหาอัตโนมัติ” ก่อน ยังคงบันทึกหรือไม่?',
        'settings_rule_confirm_save': 'ยังคงบันทึก',
    },
    'de': {
        'settings_rule_dialog_desc': 'Unsicher? Tippe unten auf „Automatisch erkennen“ — in einem Tipp erledigt. Du kannst auch den Ressourcennamen des Zeit-Widgets eingeben (leer = eingebaute Regeln).',
        'settings_rule_or_manual': 'Oder Ressourcennamen des Zeit-Widgets manuell eingeben',
        'settings_rule_confirm_title': 'Name möglicherweise ungültig',
        'settings_rule_confirm_msg': '„%1$s“ sieht nicht wie der Ressourcenname eines Zeit-Widgets aus (enthält meist clock oder time) und funktioniert evtl. nicht. Versuche zuerst „Automatisch erkennen“. Trotzdem speichern?',
        'settings_rule_confirm_save': 'Trotzdem speichern',
    },
    'es': {
        'settings_rule_dialog_desc': '¿No sabes? Pulsa “Detectar automáticamente” abajo y listo. También puedes escribir el nombre de recurso del widget de hora (vacío = reglas integradas).',
        'settings_rule_or_manual': 'O escribe manualmente el nombre de recurso del widget de hora',
        'settings_rule_confirm_title': 'El nombre puede no ser válido',
        'settings_rule_confirm_msg': '“%1$s” no parece el nombre de recurso de un widget de hora (suele contener clock o time) y podría no funcionar. Prueba “Detectar automáticamente” primero. ¿Guardar igualmente?',
        'settings_rule_confirm_save': 'Guardar igualmente',
    },
    'fr': {
        'settings_rule_dialog_desc': 'Vous ne savez pas ? Touchez « Détection auto » ci-dessous pour terminer en un geste. Vous pouvez aussi saisir le nom de ressource du widget d’heure (vide = règles intégrées).',
        'settings_rule_or_manual': 'Ou saisissez manuellement le nom de ressource du widget d’heure',
        'settings_rule_confirm_title': 'Nom peut-être invalide',
        'settings_rule_confirm_msg': '« %1$s » ne ressemble pas au nom de ressource d’un widget d’heure (contient généralement clock ou time) et pourrait ne pas fonctionner. Essayez d’abord « Détection auto ». Enregistrer quand même ?',
        'settings_rule_confirm_save': 'Enregistrer quand même',
    },
    'uk': {
        'settings_rule_dialog_desc': 'Не знаєте? Натисніть «Автовизначення» нижче — готово одним дотиком. Можна також ввести ім’я ресурсу віджета часу (порожньо = вбудовані правила).',
        'settings_rule_or_manual': 'Або введіть ім’я ресурсу віджета часу вручну',
        'settings_rule_confirm_title': 'Ім’я може бути недійсним',
        'settings_rule_confirm_msg': '«%1$s» не схоже на ім’я ресурсу віджета часу (зазвичай містить clock або time) і може не працювати. Спочатку спробуйте «Автовизначення». Все одно зберегти?',
        'settings_rule_confirm_save': 'Все одно зберегти',
    },
    'pl': {
        'settings_rule_dialog_desc': 'Nie wiesz? Dotknij „Wykryj automatycznie” poniżej i gotowe. Możesz też wpisać nazwę zasobu widżetu czasu (puste = reguły wbudowane).',
        'settings_rule_or_manual': 'Lub wpisz ręcznie nazwę zasobu widżetu czasu',
        'settings_rule_confirm_title': 'Nazwa może być nieprawidłowa',
        'settings_rule_confirm_msg': '„%1$s” nie wygląda na nazwę zasobu widżetu czasu (zwykle zawiera clock lub time) i może nie działać. Najpierw spróbuj „Wykryj automatycznie”. Mimo to zapisać?',
        'settings_rule_confirm_save': 'Mimo to zapisz',
    },
    'pt-BR': {
        'settings_rule_dialog_desc': 'Não sabe? Toque em “Detectar automaticamente” abaixo e pronto. Você também pode digitar o nome do recurso do widget de hora (vazio = regras internas).',
        'settings_rule_or_manual': 'Ou digite manualmente o nome do recurso do widget de hora',
        'settings_rule_confirm_title': 'O nome pode ser inválido',
        'settings_rule_confirm_msg': '“%1$s” não parece o nome de recurso de um widget de hora (geralmente contém clock ou time) e pode não funcionar. Tente “Detectar automaticamente” primeiro. Salvar mesmo assim?',
        'settings_rule_confirm_save': 'Salvar mesmo assim',
    },
    'fa': {
        'settings_rule_dialog_desc': 'نمی‌دانید؟ روی «تشخیص خودکار» در پایین بزنید تا با یک ضربه تمام شود. همچنین می‌توانید نام منبع ویجت زمان را وارد کنید (خالی = قوانین داخلی).',
        'settings_rule_or_manual': 'یا نام منبع ویجت زمان را دستی وارد کنید',
        'settings_rule_confirm_title': 'نام ممکن است نامعتبر باشد',
        'settings_rule_confirm_msg': '«%1$s» شبیه نام منبع ویجت زمان نیست (معمولاً شامل clock یا time است) و ممکن است کار نکند. ابتدا «تشخیص خودکار» را امتحان کنید. با این حال ذخیره شود؟',
        'settings_rule_confirm_save': 'با این حال ذخیره کن',
    },
    'ar': {
        'settings_rule_dialog_desc': 'لا تعرف؟ اضغط «كشف تلقائي» أدناه لإكمال الأمر بلمسة واحدة. يمكنك أيضاً إدخال اسم مورد واجهة الوقت (فارغ = القواعد المدمجة).',
        'settings_rule_or_manual': 'أو أدخل اسم مورد واجهة الوقت يدوياً',
        'settings_rule_confirm_title': 'قد يكون الاسم غير صالح',
        'settings_rule_confirm_msg': '«%1$s» لا يبدو اسم مورد واجهة وقت (عادةً يحتوي على clock أو time) وقد لا يعمل. جرّب «كشف تلقائي» أولاً. هل تريد الحفظ على أي حال؟',
        'settings_rule_confirm_save': 'احفظ على أي حال',
    },
    'id': {
        'settings_rule_dialog_desc': 'Tidak tahu? Ketuk “Deteksi otomatis” di bawah untuk selesai sekali ketuk. Anda juga bisa memasukkan nama sumber widget waktu (kosong = aturan bawaan).',
        'settings_rule_or_manual': 'Atau masukkan nama sumber widget waktu secara manual',
        'settings_rule_confirm_title': 'Nama mungkin tidak valid',
        'settings_rule_confirm_msg': '“%1$s” tampaknya bukan nama sumber widget waktu (biasanya berisi clock atau time) dan mungkin tidak berfungsi. Coba “Deteksi otomatis” dulu. Tetap simpan?',
        'settings_rule_confirm_save': 'Tetap simpan',
    },
    'it': {
        'settings_rule_dialog_desc': 'Non sai? Tocca “Rilevamento automatico” qui sotto e fatto. Puoi anche inserire il nome della risorsa del widget dell’ora (vuoto = regole integrate).',
        'settings_rule_or_manual': 'Oppure inserisci manualmente il nome della risorsa del widget dell’ora',
        'settings_rule_confirm_title': 'Il nome potrebbe non essere valido',
        'settings_rule_confirm_msg': '“%1$s” non sembra il nome della risorsa di un widget dell’ora (di solito contiene clock o time) e potrebbe non funzionare. Prova prima “Rilevamento automatico”. Salvare comunque?',
        'settings_rule_confirm_save': 'Salva comunque',
    },
    'el': {
        'settings_rule_dialog_desc': 'Δεν ξέρετε; Πατήστε «Αυτόματη ανίχνευση» παρακάτω και τελειώσατε. Μπορείτε επίσης να εισαγάγετε το όνομα πόρου του γραφικού στοιχείου ώρας (κενό = ενσωματωμένοι κανόνες).',
        'settings_rule_or_manual': 'Ή εισαγάγετε χειροκίνητα το όνομα πόρου του γραφικού στοιχείου ώρας',
        'settings_rule_confirm_title': 'Το όνομα ίσως δεν είναι έγκυρο',
        'settings_rule_confirm_msg': 'Το «%1$s» δεν μοιάζει με όνομα πόρου γραφικού στοιχείου ώρας (συνήθως περιέχει clock ή time) και ίσως δεν λειτουργήσει. Δοκιμάστε πρώτα «Αυτόματη ανίχνευση». Αποθήκευση παρόλα αυτά;',
        'settings_rule_confirm_save': 'Αποθήκευση παρόλα αυτά',
    },
    'ka': {
        'settings_rule_dialog_desc': 'არ იცით? დააჭირეთ ქვემოთ «ავტომატური ძებნა» და დასრულდა. ასევე შეგიძლიათ შეიყვანოთ დროის ვიჯეტის რესურსის სახელი (ცარიელი = ჩაშენებული წესები).',
        'settings_rule_or_manual': 'ან ხელით შეიყვანეთ დროის ვიჯეტის რესურსის სახელი',
        'settings_rule_confirm_title': 'სახელი შეიძლება არასწორი იყოს',
        'settings_rule_confirm_msg': '«%1$s» არ ჰგავს დროის ვიჯეტის რესურსის სახელს (ჩვეულებრივ შეიცავს clock ან time) და შეიძლება არ იმუშაოს. ჯერ სცადეთ «ავტომატური ძებნა». მაინც შენახვა?',
        'settings_rule_confirm_save': 'მაინც შენახვა',
    },
}

# ---- 覆盖键（中文/英文「时钟→时间」+ 描述改写）----
UPDATE = {
    'zh': {
        'settings_rule_title': '状态栏时间识别名',
        'settings_rule_sum': '不懂设置？点「设置」内的「自动检测」一键完成',
        'settings_rule_msg': '若其它厂商机型未能自动识别状态栏时间，可手动填写时间控件的资源名（如 clock），留空则使用内置规则。',
        'settings_rule_detecting': '正在检测状态栏时间…',
        'settings_rule_detect_fail': '未识别到状态栏时间，请尝试手动设置或重启后重试',
    },
    'en': {
        'settings_rule_title': 'Status-bar time name',
        'settings_rule_sum': 'Not sure? Tap “Set” → “Auto detect” to finish in one tap',
        'settings_rule_msg': 'If the status-bar time is not auto-detected on other devices, enter the time widget’s resource name (e.g. clock). Leave empty for built-in rules.',
        'settings_rule_detecting': 'Detecting status-bar time…',
        'settings_rule_detect_fail': 'Status-bar time not found. Try manual entry or restart and retry.',
    },
}


def esc(v):
    return (v.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')
            .replace("'", "\\'"))


def set_value(data, key, val):
    pat = re.compile(r'(<string name="%s">)(.*?)(</string>)' % re.escape(key), re.S)
    return pat.sub(lambda m: m.group(1) + esc(val) + m.group(3), data, count=1)


def add_key(data, key, val):
    return data.replace(
        '</resources>',
        '    <string name="%s">%s</string>\n</resources>' % (key, esc(val)),
        1)


def main():
    for lang, suffix in DIRS.items():
        path = os.path.join(RES, 'values' + suffix, 'strings.xml')
        if not os.path.isfile(path):
            print('skip (missing): ' + path)
            continue
        with open(path, 'r', encoding='utf-8') as f:
            data = f.read()
        upd = 0
        for key, val in UPDATE.get(lang, {}).items():
            if 'name="%s"' % key in data:
                data = set_value(data, key, val)
                upd += 1
        added = []
        for key, val in NEW[lang].items():
            if 'name="%s"' % key in data:
                continue
            data = add_key(data, key, val)
            added.append(key)
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(data)
        print('%-6s upd=%d +%d %s' % (lang, upd, len(added), ', '.join(added)))


if __name__ == '__main__':
    main()
