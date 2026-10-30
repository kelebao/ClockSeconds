# -*- coding: utf-8 -*-
"""Slot translations for README generation: Türkçe (tr)."""

TEXTS = {
    'tr': {
        'title': 'Saatte saniye göster (Show Seconds in Time)',
        'tagline': (
            'Durum çubuğu saatinde **saniye** gösterir, dokunuşla açılıp kapanır. **Her rakamı ayrı renklendirin** '
            'ya da tüm dizeye **renk geçişi** uygulayın.<br>'
            '**Uzun basınca** **ağ saati**ni gösteren buzlu cam penceresi açılır (sürüklenebilir, boyutlandırılabilir, '
            'renk geçişli); **özel görevler** saat başında saniyeleri otomatik açar.<br>'
            'Çekirdek yol yalnızca AOSP açık API kullanır (OPPO / OnePlus / realme özel sınıfları isteğe bağlıdır, '
            'yoksa atlanır). ColorOS 15 / 16 üzerinde doğrulandı.'
        ),
        'h_features': 'Özellikler',
        'th_feature': 'Özellik',
        'th_desc': 'Açıklama',
        'rows': [
            ('Durum çubuğunda saniye',
             '`HH:mm:ss` / `h:mm:ss a`, sistemin 12/24 saat ve saat dilimi ayarına uyar. Tema modülleri renk '
             'spanlarını silemesin diye sona **sıfır genişlikli boşluk** eklenir ve renk kaybolursa otomatik '
             'yeniden çizilir (5 dakika bekleme).'),
            ('Dokunarak değiştirme',
             'Saate **dokununca** saniye açılır/kapanır (**çift dokunma** seçilebilir); **uzun basma** yüzen '
             'önizlemeye ayrılmıştır. Görev penceresinde "görev devraldı" bilgisi görünür.'),
            ('Rakam başına renk / geçiş',
             'Altı rakam **ayrı ayrı** renklendirilir (10 hazır renk + HSV kaydırıcıları + sistemi izle), iki nokta '
             'optik olarak ortalanır; ya da tüm dize için **renk geçişi** (başlangıç → bitiş, 6 hazır, iki uç '
             'özelleştirilebilir, açık zeminde otomatik koyulaştırma). Önizleme **kendi çizimimiz** olduğu için '
             'durum çubuğuyla birebir aynıdır.'),
            ('Uzun basma önizlemesi',
             '**Ağ saati**ni gösteren buzlu cam pencere; sürükleyerek taşıyın, ◣ köşesinden boyutlandırın, konum ve '
             'boyut **hatırlanır**; renk geçişi ve eşit genişlikli rakamlar desteklenir.'),
            ('Özel görevler',
             '**Her saat** veya **her gün** pencereleri: N dakika önce saniyeleri aç, M dakika sonra eski duruma dön. '
             'Sistem saati veya saat dilimi değişirse anında yeniden hesaplanır, ayrıca 60 saniyelik yedek vardır.'),
            ('Ağ saati karşılaştırması',
             '**Ağ saati** ile cihaz saati arasındaki farkı gösterir; **Eşitle** sistemin "saati otomatik ayarla" '
             'seçeneğini açar.'),
            ('adb izni gerekmez',
             'Yazma işlemleri önce `Settings.Secure`, izin yoksa yayın (broadcast) ile SystemUI yazar — '
             '**hiçbir özellik için adb komutu gerekmez**.'),
            ('Hızlı ayar kutucuğu',
             'Bildirim panelinde "Saniyeyi göster" kutucuğu ile uygulamayı açmadan değiştirin.'),
            ('Saat kaynak adı',
             'Otomatik algılanmazsa **Otomatik algıla**ya dokunun ya da kaynak adını (ör. `clock`) elle girin.'),
            ('21 arayüz dili',
             'Sistem dilini izler: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, '
             'Italiano, Ελληνικά, ქართული; diğerleri İngilizceye düşer.'),
            ('Kalıcılık / kapsam',
             'Durum `Settings.Secure` içinde tutulur, süreçler arasında paylaşılır ve yeniden başlatmada korunur. '
             '**ColorOS 15 / 16 (Android 15 / 16) doğrulandı**; çekirdek yalnızca açık API kullandığı için diğer '
             'üreticilerde de çalışması beklenir, ancak yalnızca OPPO / OnePlus test edildi. En az Android 10.'),
        ],
        'h_colors': 'Renk modları',
        'colors_intro': (
            'Rengi iki şekilde uygulayabilirsiniz, **Rakam renkleri** kartından geçiş yapılır: '
            '**rakam başına** (her rakam bir renk) veya tüm dizeye **renk geçişi**.'
        ),
        'cap_digit': 'Rakam başına: altı rakamın her biri kendi rengi, iki nokta optik ortada',
        'cap_grad': 'Renk geçişi: başlangıçtan bitişe, 6 hazır veya iki uç özel',
        'h_install': 'Kurulum',
        'install': [
            'En son APKyı [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) sayfasından indirin',
            'Modülü **LSPosed** içinde etkinleştirin ve kapsamı **Sistem UI** (`com.android.systemui`) yapın',
            'SystemUI sürecini veya cihazı yeniden başlatın',
            'Ayarlar ekranını **LSPosed yöneticisinden** açın (başlatıcı simgesi oluşturulmaz)',
        ],
        'note1': (
            'root (LSPosed) gerekir. **Genellikle adb izni gerekmez**: yazma `Settings.Secure` üzerinden gider, '
            '`WRITE_SECURE_SETTINGS` yoksa SystemUI yayın ile yazar. Ayarlar ekranının **doğrudan** yazmasını '
            'isterseniz şunu verin:'
        ),
        'note2': (
            'İzin vermeseniz de kullanım değişmez: dokunma, görev planlama ve yüzen pencere SystemUI süreci içinde çalışır.'
        ),
        'h_usage': 'Kullanım',
        'usage': [
            '**Durum çubuğu saatine dokunun**: saniyeleri açar/kapatır (varsayılan). Ayarlarda **çift dokunma** seçilebilir.',
            '**Saate uzun basın**: **ağ saati**ni gösteren buzlu pencere. Sürükleyerek taşıyın, saate dokununca '
            '**kapat** (sağ üst) ve **boyutlandır** (sol alt) düğmeleri görünür.',
            '**Rakam renkleri** kartı iki sayfadır ve sekmenin kendisi anahtardır: **rakam başına** altı rakamı ayrı '
            'renklendirir, **renk geçişi** tüm dizeye tek bir renk bandı uygular.',
            '**Özel görevler** kartında "her saat başından 2 dakika önce saniyeleri aç" gibi planlar kurulur.',
            'Geçiş global dokunma testiyle yapılır, bu yüzden **bildirim panelini indirmeyle çakışmaz**.',
            'Ayarlar ekranı durumu, hareketi, renkleri, ağ saati farkını ve tek dokunuşluk **eşitlemeyi** gösterir.',
        ],
        'h_demo': 'Önizleme',
        'cap_off': 'Saniye kapalı: `15:06`, saat rakamı seçilen rengi korur',
        'cap_on': 'Saniye açık: `15:06:03`, dakika ve saniye sistem rengini korur',
        'h_how': 'Nasıl çalışır',
        'how_intro': [
            'Modül yalnızca **SystemUI** sürecini hooklar ve temkinli legacy Xposed API kullanır.',
            '**Çekirdek yol yalnızca AOSP açık API kullanır, üretici özel sınıfları isteğe bağlıdır ve yoksa atlanır**, '
            'bu yüzden sistem durumunu bozmaz ve LSPosed güvenli modunu tetiklemez:',
        ],
        'how': [
            '**Saniye**: `TextView.setText` (after) ile sistem metni önbelleğe alınır, `TextView.onDraw` (before) içinde '
            '`orijinal:ss` yazılır ve 1 saniyelik `Handler` ile yenilenir.',
            '**Rakam başına renk**: her rakam için `ForegroundColorSpan` (dış spanlar önce kaldırılır); iki nokta '
            '`CharacterStyle` ile 0.11em yükseltilir, çünkü çok runlu düzende ana glif aşağıda kalır.',
            '**Renk geçişi**: `CharacterStyle` içinde `updateDrawState` sırasında `setShader`; ayarlar önizlemesi '
            '**kendi çizimidir** (`GradientTimeView`), çünkü ColorOS 15 shaderı çizim runı başına nicemler.',
            '**Değiştirme**: bildirim paneli durum çubuğu dokunuşlarını yuttuğu için global '
            '`View.dispatchTouchEvent` hooklanır ve yalnızca saat sınırları içinde neredeyse hareketsiz bir basışta '
            'anahtar çevrilir. Olay **tüketilmez**.',
            '**Görevler**: pencere "yerel saat içinde mi" ile belirlenir, sınır zamanlayıcıları ve 60 saniyelik yedek '
            'vardır; saat veya saat dilimi değişiminde anında yeniden hesaplanır.',
        ],
        'h_power': 'Güç tüketimi',
        'power': [
            '**Saniye açık**: 1 saniyelik zamanlayıcı gerekir (saniye göstermenin doğal maliyeti). Ölçülen SystemUI '
            'tek çekirdek yükü: kapalı %7.0, açık %7.25 (60 saniyelik pencere), dilim gürültüsünün altında.',
            '**Saniye kapalı**: zamanlayıcı **hemen durur** (`stopTicker`); saniye başına uyanma ve süreçler arası okuma yoktur.',
            '**Rengi koruma** 2s → 4s → … → 30s geri çekilir ve 5 dakika kayıp olmazsa durur (`color-guard stopped`).',
            'Durum **süreç içinde önbelleklenir**, zamanlayıcı her saniye `Settings.Secure` okumaz.',
            'Görev yedeği `Handler.postDelayed` kullanır: **WakeLock yok, AlarmManager yok**, derin uyku uyandırılmaz.',
            'Zamanlayıcı görünümün **zayıf referansını** tutar ve görünüm ayrılınca biter.',
        ],
        'h_build': 'Derleme',
        'build_intro': (
            '**JDK 17+** (17 veya 21 önerilir) ve **Android SDK Platform 35** gerekir '
            '(Build-Tools 35 AGP tarafından indirilir).'
        ),
        'h_backup': 'Yedekleme ve geliştirmeye devam (önemli)',
        'backup_intro': (
            '`ClockSeconds/` klasöründe **`build/` ve `.gradle/` dışındaki her şeyi** saklayın (kaynak kısmı yalnızca ~300 KB):'
        ),
        'th_backup_cat': 'Kategori',
        'th_backup_files': 'Dosyalar',
        'th_backup_note': 'Notlar',
        'backup_rows': [
            ('Derleme betikleri',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, '
             '`keystore.properties`', 'Sürümler, bağımlılıklar, derleyici ayarları, imza'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Gradle 8.14.5 sabitler'),
            ('Kaynak', '`app/src/main/java/**` (20 `.java`)', 'Modül mantığı, ayarlar ekranı, çizim sınıfları'),
            ('Kaynaklar', '`app/src/main/res/**`', '22 yerel ayar için metinler, temalar'),
            ('Xposed üst verisi', '`app/src/main/resources/META-INF/xposed/*`', 'Giriş sınıfı, kapsam, API düzeyi'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'İzinler, etkinlikler'),
            ('Araç betikleri', '`tools/**`', 'README üreteci, i18n denetimi, sürüm betikleri'),
            ('Belgeler', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Açıklama, ekran görüntüleri, değişiklik günlüğü'),
            ('CI', '`.github/workflows/*.yml`', 'Etikette imzalı derleme'),
        ],
        'h_trouble': 'Sorun giderme',
        'trouble_summary': 'Adımları göster',
        'trouble_intro': 'logcat kontrolü:',
        'trouble': [
            ('b', '**Sürüm derlemeleri** tüm tanılama günlüklerini `BuildConfig.DEBUG` arkasında gizler, logcat boş kalır.'),
            ('b', 'Hata ayıklamak için **debug derlemesi** kurun (`assembleDebug`, aynı anahtarla imzalanır, üzerine kurulur), sonra filtreleyin:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Hiç saniye yoksa: LSPosed kapsamında Sistem UI olduğundan emin olup yeniden başlatın; anahtarın değerini okuyun veya zorlayın:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Renk veya saniye kendiliğinden geri dönüyorsa sistem veya tema metni yeniden kuruyordur ve modül yeniden çizer; sürerse debug günlüğünde `color-guard stopped` / `span-theft` arayın.'),
            ('b', 'ColorOS dışı bir cihazda çalışmıyorsa debug derlemesinde `candidate:` / `CONFIG CLOCK:` çıktısına bakıp saat sınıfı veya kaynak adının kurallara uyduğunu kontrol edin.'),
        ],
        'h_feedback': 'Geri bildirim',
        'feedback': 'Soru, öneri veya hata bildirimi için [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) açabilirsiniz.',
        'h_license': 'License',
    },
}
