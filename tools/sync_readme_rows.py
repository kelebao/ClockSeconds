#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Append the 4 new feature rows to every README language's 'rows' list.

Run:  python tools/sync_readme_rows.py
Idempotent-ish: re-running will NOT duplicate (it refuses if a known marker is present).
"""
import os
import re
import glob

HERE = os.path.dirname(os.path.abspath(__file__))

# The 4 new feature rows, per language: (name, desc)
NEW = {
    'zh': [
        ('下拉磁贴', '下拉通知栏添加「时间显秒」磁贴，一键开关显秒（无需打开设置页）'),
        ('浮窗增强', '浮窗位置与尺寸**自动记忆**（下次长按按记忆显示）；浮窗新增**日期**行；**双击**浮窗时间也可切换显秒'),
        ('省电自适应', '系统开启省电模式时自动暂停秒级刷新（状态栏秒数冻结），退出省电立即恢复，零额外耗电'),
        ('时钟识别可配置', '其它厂商机型若未能自动识别状态栏时钟，可在设置页填写其资源名（如 clock），留空用内置规则'),
    ],
    'en': [
        ('Quick tile', 'Add a "Show Seconds" tile to the notification shade to toggle seconds in one tap'),
        ('Float upgrade', 'Float window position & size are **remembered**; it now shows the **date**; **double-tap** the time to also toggle seconds'),
        ('Power-save aware', 'When battery saver is on, per-second refresh pauses (seconds freeze); resumes instantly when off'),
        ('Configurable detection', 'If the clock is not auto-detected on other devices, set its resource name (e.g. clock) in Settings; empty = built-in rules'),
    ],
    'zh-TW': [
        ('下拉磁貼', '下拉通知欄新增「時間顯秒」磁貼，一鍵開關顯秒（無需打開設定頁）'),
        ('浮窗增強', '浮窗位置與尺寸**自動記憶**；浮窗顯示**日期**；**雙擊**浮窗時間也可切換顯秒'),
        ('省電自適應', '系統開啟省電模式時自動暫停秒級重新整理（狀態列秒數凍結），退出省電立即恢復'),
        ('時鐘識別可設定', '其它廠商機型若未能自動識別狀態列時鐘，可在設定頁填寫其資源名（如 clock），留空用內建規則'),
    ],
    'ja': [
        ('クイックタイル', '通知シェードに「秒表示」タイルを追加し、ワンタップで切替'),
        ('フロート強化', 'フロートの位置とサイズを**記憶**；**日付**も表示；フロートの時刻を**ダブルタップ**でも秒表示切替'),
        ('省電対応', '省電モードONで秒更新を一時停止（秒が停止）；OFFで即座に再開'),
        ('認識を設定可能', '他社端末で時計が自動認識されない場合、設定でリソース名（例: clock）を指定。空欄は内蔵ルール'),
    ],
    'ko': [
        ('빠른 타일', '알림 창에 "초 표시" 타일을 추가해 한 번에 켜고 끄기'),
        ('플로팅 강화', '플로팅 위치·크기**자동 기억**; **날짜** 표시; 플로팅 시간**두 번 탭**으로도 초 전환'),
        ('절전 대응', '절전 모드 켜면 초 단위 갱신 일시정지(초 멈춤), 끄면 즉시 재개'),
        ('인식 설정 가능', '다른 기기에서 시계가 자동 인식되지 않으면 설정에서 리소스 이름(예: clock) 지정. 비우면 내장 규칙'),
    ],
    'tr': [
        ('Hızlı döşeme', 'Bildirim panelinde "Saniye Göster" döşemesi ekleyin, tek dokunuşla aç/kapat'),
        ('Kayan pencere', 'Kayan pencerenin konumu ve boyutu **hatırlanır**; **tarih** gösterilir; zamana **çift dokun** ile saniye aç/kapat'),
        ('Pil tasarrufu', 'Tasarruf modu açılınca saniye yenileme durur (saniyeler donar), kapayınca hemen devam eder'),
        ('Algılama yapılandırılabilir', 'Saat diğer cihazlarda algılanmazsa ayarlardan kaynak adını (ör. clock) girin. Boş = yerleşik kurallar'),
    ],
    'ru': [
        ('Плитка', 'Добавьте плитку "Показывать секунды" в шторку, переключение в один тап'),
        ('Улучшения плавающего', 'Позиция и размер окна **запоминаются**; показывается **дата**; **двойной тап** по времени тоже переключает секунды'),
        ('Энергосбережение', 'В режиме экономии обновление в секунду приостанавливается (секунды замирают), при выходе — сразу возобновляется'),
        ('Настраиваемое распознавание', 'Если часы не определяются на другом устройстве, укажите имя ресурса (напр. clock) в настройках. Пусто = встроенные правила'),
    ],
    'vi': [
        ('Ô vuông nhanh', 'Thêm ô "Hiện giây" vào bảng thông báo, bật/tắt bằng một chạm'),
        ('Cải tiến float', 'Vị trí & kích thước float được **nhớ**; hiển thị **ngày**; **nhấn đúp** thời gian cũng bật/tắt giây'),
        ('Tiết kiệm pin', 'Bật tiết kiệm pin thì tạm dừng làm mới từng giây (giây đông cứng), tắt thì phục hồi ngay'),
        ('Nhận diện cấu hình', 'Nếu đồng hồ không tự nhận diện, nhập tên tài nguyên (vd clock) trong Cài đặt. Trống = quy tắc mặc định'),
    ],
    'th': [
        ('เทมเพลตด่วน', 'เพิ่มไทล์ "แสดงวินาที" ในบานหน้าต่างการแจ้งเตือน เปิด/ปิดด้วยแตะเดียว'),
        ('พื้นที่ลอย', 'ตำแหน่งและขนาดหน้าต่างลอย**ถูกจดจำ**; แสดง**วันที่**; **แตะสองครั้ง**ที่เวลาก็สลับวินาที'),
        ('ประหยัดพลังงาน', 'เปิดโหมดประหยัดแบตเตอรี่จะหยุดรีเฟรชวินาที (วินาทีค้าง) ปิดแล้วกลับมาทันที'),
        ('ปรับแต่งการจับคู่ได้', 'หากนาฬิกาไม่ถูกจับคู่โดยอัตโนมัติ ให้ป้อนชื่อทรัพยากร (เช่น clock) ในการตั้งค่า ว่าง = กฎในตัว'),
    ],
    'de': [
        ('Schnellkachel', 'Füge eine "Sekunden anzeigen"-Kachel zum Benachrichtigungsbereich hinzu, mit einem Tipp umschalten'),
        ('Float verbessert', 'Position & Größe des Floats werden **gemerkt**; zeigt das **Datum**; **Doppeltipp** auf die Zeit schaltet Sekunden um'),
        ('Energiesparerkennung', 'Bei Aktivierung des Energiesparmodus pausiert die Sekundenaktualisierung (Sekunden einfrieren), danach sofort weiter'),
        ('Erkennung konfigurierbar', 'Wird die Uhr auf anderen Geräten nicht erkannt, gib den Ressourcennamen (z. B. clock) in den Einstellungen ein. Leer = eingebaute Regeln'),
    ],
    'es': [
        ('Mosaico rápido', 'Añade un mosaico "Mostrar segundos" en el panel, alterna con un toque'),
        ('Mejoras flotante', 'La posición y tamaño del flotante se **recuerdan**; muestra la **fecha**; **doble toque** en la hora también alterna'),
        ('Ahorro de batería', 'Con el ahorro de batería activo, la actualización por segundo se pausa (segundos se congelan), al desactivar se reanuda'),
        ('Detección configurable', 'Si el reloj no se detecta, indica su nombre de recurso (p. ej. clock) en Ajustes. Vacío = reglas integradas'),
    ],
    'fr': [
        ("Tuile rapide", "Ajoutez une tuile \"Afficher les secondes\" au panneau, basculez d'un tap"),
        ("Flottant amélioré", "Position et taille du flottant **mémorisées**; affiche la **date**; **double-tap** sur l'heure bascule aussi"),
        ("Économie d'énergie", "En mode économie, l'actualisation par seconde se met en pause (secondes figées), reprend à la sortie"),
        ("Détection configurable", "Si l'horloge n'est pas détectée, saisissez son nom de ressource (ex. clock) dans les réglages. Vide = règles intégrées"),
    ],
    'uk': [
        ('Швидка плитка', 'Додайте плитку "Показувати секунди" у шторку, перемкніть одним дотиком'),
        ('Покращення плаваючого', 'Позиція та розмір вікна **запам\'ятовуються**; показує **дату**; **подвійний тап** по часу теж перемикає'),
        ('Енергозбереження', 'У режимі економії оновлення покроково призупиняється (секунди завмирають), після вимкнення — одразу поновлюється'),
        ('Налаштовуване розпізнавання', 'Якщо годинник не визначається, вкажіть ім\'я ресурсу (напр. clock) у налаштуваннях. Порожньо = вбудовані правила'),
    ],
    'pl': [
        ('Kafelek', 'Dodaj kafelek "Pokaż sekundy" do panelu, przełączaj jednym stuknięciem'),
        ('Ulepszony float', 'Pozycja i rozmiar okna są **zapamiętywane**; pokazuje **datę**; **podwójne stuknięcie** czasu też przełącza'),
        ('Oszczędzanie energii', 'W trybie oszczędzania odświeżanie co sekundę pauzuje (sekundy zamrożone), po wyłączeniu wznawia'),
        ('Wykrywanie konfigurowalne', 'Jeśli zegar nie jest wykrywany, wpisz nazwę zasobu (np. clock) w ustawieniach. Puste = reguły wbudowane'),
    ],
    'pt-BR': [
        ('Tile rápido', 'Adicione um tile "Mostrar segundos" no painel, alterne com um toque'),
        ('Flutuante aprimorado', 'Posição e tamanho do flutuante são **lembrados**; mostra a **data**; **toque duplo** na hora também alterna'),
        ('Economia de bateria', 'Com economia ativa, a atualização por segundo pausa (segundos congelam), ao desativar retoma'),
        ('Detecção configurável', 'Se o relógio não for detectado, informe o nome do recurso (ex. clock) nas Configurações. Vazio = regras internas'),
    ],
    'fa': [
        ('کاشی سریع', 'کاشی "نمایش ثانیه" را به پنل اعلان‌ها اضافه کنید و با یک ضربه تغییر دهید'),
        ('بهبود شناور', 'موقعیت و اندازه پنجره شناور**به خاطر سپرده می‌شود**; **تاریخ** نمایش داده می‌شود; **دوبار ضربه** روی زمان هم ثانیه را تغییر می‌دهد'),
        ('سازگار با صرفه‌جویی', 'در حالت صرفه‌جویی، به‌روزرسانی ثانیه‌ای متوقف می‌شود (ثانیه‌ها ثابت می‌مانند)، پس از خاموشی فوری ادامه می‌یابد'),
        ('تشخیص قابل پیکربندی', 'اگر ساعت شناسایی نشد، نام منبع (مثلا clock) را در تنظیمات وارد کنید. خالی = قوانین داخلی'),
    ],
    'ar': [
        ('بلاطة سريعة', 'أضف بلاطة "إظهار الثواني" إلى لوحة الإشعارات، بدّل بنقرة واحدة'),
        ('تحسين الطافي', 'يتم **تذكر** موضع وحجم النافذة الطافية؛ تعرض **التاريخ**؛ **نقرة مزدوجة** على الوقت تبدّل الثواني'),
        ('تكيّف توفير الطاقة', 'عند تشغيل وضع توفير الطاقة، تتوقف التحديثات كل ثانية (تجمّد الثواني) وتستأنف فوراً عند الإيقاف'),
        ('اكتشاف قابل للتهيئة', 'إذا لم يتم التعرف على الساعة، أدخل اسم المورد (مثل clock) في الإعدادات. فارغ = القواعد المدمجة'),
    ],
    'id': [
        ('Tile cepat', 'Tambahkan tile "Tampilkan detik" ke panel, alihkan dengan satu ketuk'),
        ('Float ditingkatkan', 'Posisi & ukuran float **diingat**; menampilkan **tanggal**; **ketuk ganda** waktu juga mengalihkan'),
        ('Sadar hemat baterai', 'Saat mode hemat aktif, penyegaran per detik dijeda (detik membeku), saat mati langsung dilanjut'),
        ('Deteksi dapat diatur', 'Jika jam tidak terdeteksi, masukkan nama sumber (mis. clock) di Pengaturan. Kosong = aturan bawaan'),
    ],
    'it': [
        ('Tile rapida', 'Aggiungi una tile "Mostra secondi" al pannello, attiva con un tocco'),
        ('Miglioramenti flottante', 'Posizione e dimensione del flottante **memorizzate**; mostra la **data**; **doppio tocco** sull\'ora alterna'),
        ('Risparmio energetico', 'Con il risparmio attivo, l\'aggiornamento al secondo si mette in pausa (secondi fermi), riprende all\'uscita'),
        ('Rilevamento configurabile', 'Se l\'orologio non viene rilevato, indica il nome della risorsa (es. clock) nelle Impostazioni. Vuoto = regole integrate'),
    ],
    'el': [
        ('Γρήγορη πλακέτα', 'Προσθέστε πλακέτα "Εμφάνιση δευτερολέπτων" στην περιοχή ειδοποιήσεων, εναλλαγή με μία αφή'),
        ('Βελτιωμένο επίπλευσης', 'Η θέση και το μέγεθος του παραθύρου **απομνημονεύονται**· εμφανίζει την **ημερομηνία**· **διπλό tap** στην ώρα εναλλάσσει'),
        ('Προσαρμογή εξοικονόμησης', 'Όταν ενεργοποιηθεί η εξοικονόμηση, το ανά δευτερόλεπτο refresh παύει (δευτερόλεπτα παγώνουν), συνεχίζει αμέσως μετά'),
        ('Ρυθμιζόμενη ανίχνευση', 'Αν το ρολόι δεν εντοπίζεται, ορίστε το όνομα πόρου (π.χ. clock) στις Ρυθμίσεις. Κενό = ενσωματωμένοι κανόνες'),
    ],
    'ka': [
        ('სწრაფი ელემენტი', 'დაამატეთ ელემენტი "წამების ჩვენება" შეტყობინებების ზოლში, ერთი შეხებით გადართვა'),
        ('გაუმჯობესებული გაფანტული', 'ფანჯრის მდებარეობა და ზომა **ინახება**; აჩვენებს **თარიღს**; დროზე **ორმაგი შეხება** ასევე გადართავს წამებს'),
        ('შემოსაზღვრის ადაპტაცია', 'კვების შემოზღუდვის რეჟიმში წამობითი განახლება პაუზდება (წამები გაყინებულია), გამორთვისას მყისიერად გრძელდება'),
        ('კონფიგურირებადი ანგარიში', 'თუ საათი ავტომატურად არ ჩანს, მიუთითეთ რესურსის სახელი (მაგ. clock) პარამეტრებში. ცარიელი = ჩაშენებული წესები'),
    ],
}

# Marker tuple name that proves the rows were already added (idempotency check).
MARKER = 'Quick tile'  # en name; we instead check by '下拉磁贴' in zh and a generic marker


def main():
    for path in sorted(glob.glob(os.path.join(HERE, 'readme_l10n_*.py'))):
        with open(path, 'r', encoding='utf-8') as f:
            data = f.read()
        m = re.search(r"TEXTS = \{\s*'([a-zA-Z\-]+)':", data)
        lang = m.group(1) if m else None
        if lang not in NEW:
            print('skip (unknown lang %s): %s' % (lang, os.path.basename(path)))
            continue
        # idempotency: if the marker row already present, skip
        if '下拉磁贴' in data and lang == 'zh':
            print('zh (already has new rows)')
            continue
        if lang != 'zh' and ('Quick tile' in data or 'クイックタイル' in data
                             or 'Schnellkachel' in data or 'Mosaico rápido' in data):
            print('%-6s (already has new rows)' % lang)
            continue
        idx = data.index("'rows': [")
        end = data.index("\n        ],", idx)
        block = ""
        for (name, desc) in NEW[lang]:
            block += "            (" + repr(name) + ",\n             " + repr(desc) + "),\n"
        data = data[:end] + block + data[end:]
        with open(path, 'w', encoding='utf-8', newline='\n') as f:
            f.write(data)
        print('%-6s +%d rows' % (lang, len(NEW[lang])))


if __name__ == '__main__':
    main()
