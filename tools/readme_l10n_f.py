# -*- coding: utf-8 -*-
"""Slot translations for README generation: Türkçe (tr)."""

TEXTS = {
    'tr': {
        'title': 'Saniyeli Saat (Clock Seconds)',
        'tagline': (
            'Durum çubuğu saatinde **saniyeleri** gösterin, tek dokunuşla açıp kapatın, '
            'saatin **onlar basamağını kırmızı** tutun.<br>'
            'Hiçbir üreticiye özel sınıf kullanılmaz. ColorOS 15 üzerinde gerçek cihazda '
            'doğrulandı; Xiaomi, OnePlus, Honor, vivo ve diğer büyük üreticileri de kapsar.'
        ),
        'h_features': 'Özellikler',
        'th_feature': 'Özellik',
        'th_desc': 'Açıklama',
        'rows': [
            ('Saniye gösterimi',
             'Durum çubuğu saati saniyeye kadar gösterilir (`HH:mm:ss` / `h:mm:ss a`, sistemin 12/24 saat ayrını izler)'),
            ('Dokunarak aç/kapat',
             'Durum çubuğu saatine **dokunmak** saniye gösterimini açar/kapatır; ayarlar sayfasından **uzun basma** hareketine geçilebilir'),
            ('Kırmızı saat basamağı',
             'Saatin **onlar basamağı** kırmızıyla gösterilir (`13`deki `1`, `05`teki `0`); '
             'dakika ve saniye sistem rengini korur. Tek basamaklı saatlerde (ör. `9:45`) onlar basamağı yoktur, '
             'dokunulmaz. Tüm üreticilerde etkilidir'),
            ('Arayüz dilleri',
             'Uygulama arayüzü **21 dili** destekler (sistem dilini otomatik izler): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Kalıcılık',
             'Durum `Settings.Secure` içinde saklanır, işlemler arası paylaşılır, SystemUI yeniden başlatıldıktan sonra korunur'),
            ('Uyumluluk',
             'ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme) gerçek cihazda doğrulandı; '
             'Xiaomi / Honor / Redmi gibi rootlu diğer üreticilerde de olduğu gibi çalışır. '
             'Üreticiye özel sınıf kullanılmadığı için LSPosed güvenlik modu tetiklenmez. En düşük Android 10'),
        ],
        'h_install': 'Kurulum',
        'install': [
            '[**Releases**](https://github.com/kelebao/ColorOS15ClockSeconds/releases/latest) sayfasından en son APK indirin',
            '**LSPosed** içinde modülü etkinleştirin, kapsam: **Sistem Arayüzü (`com.android.systemui`)**',
            'SystemUI işlemini yeniden başlatın veya cihazı yeniden başlatın',
            'Ayarlar sayfasını **LSPosed yöneticisinden** açın (masaüstü simgesi oluşturulmaz)',
        ],
        'note1': (
            'Root (LSPosed) gerekir. `Settings.Secure` yazmak için '
            '`WRITE_SECURE_SETTINGS` izni verilmelidir:'
        ),
        'note2': (
            'İzin verilmezse durum çubuğu saatine dokunarak yine de açıp kapatabilirsiniz '
            '(bu işlem SystemUI işlemi içinde çalışır ve zaten izne sahiptir).'
        ),
        'h_usage': 'Kullanım',
        'usage': [
            '**Durum çubuğu saatine dokunun**: saniye gösterimini açar/kapatır (varsayılan hareket).',
            'Açma/kapatma genel dokunma isabet testiyle uygulanır, bu yüzden **"bildirim panelini aşağı çekmek" ile '
            'asla çakışmaz** — saf dokunuş açıp kapatır, kaydırma paneli her zamanki gibi aşağı çeker. '
            'İsterseniz ayarlar sayfasından **uzun basma** hareketine geçebilirsiniz.',
            'Ayarlar sayfasında mevcut durumu, hareket ayarını görüntüleyebilir ve yenileyebilirsiniz.',
        ],
        'h_how': 'Nasıl Çalışır',
        'how_intro': [
            'Modül yalnızca **SystemUI** sürecine hook uygular, kanıtlanmış legacy Xposed yaklaşımını kullanır',
            've **hiçbir üreticiye özel sınıfa dokunmaz**, bu nedenle sistem iç durumunu bozmaz ve '
            'LSPosed güvenlik modunu tetiklemez:',
        ],
        'how': [
            '**Saniyeler**: kararlı `TextView.setText` (after) hook ile sistemin durum çubuğu saatine '
            'yazdığı ham metni önbelleğe alın,\n'
            'ardından `TextView.onDraw` (before) hook ile sistem metni ayarladıktan sonra '
            'görüntüyü `hamMetin:ss` olarak üzerine yazın,\n'
            '1 saniyelik bir `Handler` ile sürülür. Eklenen saniye saatin kendi metin rengini devralır '
            '(ölçülen `#ccffffff`).\n'
            'Ayrıca saatin onlar basamağı kırmızıyla gösterilir (o tek karaktere kırmızı bir '
            '`ForegroundColorSpan` eklenir), saniye gösterimi açık veya kapalı olduğunda da kırmızı kalır;\n'
            'dakika ve saniye kırmızı yapılmaz; tek basamaklı saatlerde onlar basamağı yoktur.',
            '**Açma/kapatma**: ColorOS bildirim paneli durum çubuğu alanındaki dokunmaları ele geçirir, '
            'bu yüzden saatin kendi `OnTouchListener` olay almaz.\n'
            'Modül bu nedenle genel `View.dispatchTouchEvent` hookunu kullanır ve yalnızca basılan nokta '
            'tanınan saatin ekran sınırları içine düştüğünde,\n'
            've neredeyse hiç hareket etmediğinde (dokunma) veya 500 ms basılı tutulduğunda (uzun basma) '
            '`Settings.Secure` anahtarını çevirir. Modül hiçbir dokunma olayını\n'
            '**tüketmez**, böylece bildirim panelini aşağı çekmek her zamanki gibi çalışır.',
        ],
        'h_power': 'Güç Tasarrufu',
        'power': [
            '**Saniye AÇIK**: modülün yenilemeleri sürmek için 1 saniyelik bir zamanlayıcıya '
            'ihtiyacı vardır (saniye göstermekle gelen kaçınılmaz maliyet, dakikada ~60 saat yeniden çizimi).',
            '**Saniye KAPALI**: modül bu zamanlayıcıyı **anında durdurur** (`stopTicker`) — '
            'saniyede bir uyandırma, işlemler arası okuma hiç kalmaz.\n'
            'Sistemin özgün "dakikada bir yenile" ritmine döner, bu nedenle saniye gösterimini '
            'kapatmak gerçekten daha tasarrufludur.',
            'Durum **işlem içi önbellekte** (`sSecondsEnabled`) tutulur, zamanlayıcı her saniye '
            '`Settings.Secure` okumaz (saniyede bir IPC önlenir).',
            'Hiçbir saat biçimlendirici oluşturulmaz: modül sistemin ham metnini yeniden kullanır ve '
            'yalnızca `:ss` ekler, bu nedenle biçimlendirici maliyeti sıfırdır.',
            'Zamanlayıcı görünümün yalnızca **zayıf referansını** tutar ve yalnızca görünüm '
            '`isAttachedToWindow()` olduğu sürece devam eder, görünüm yok edildiğinde otomatik çıkar.',
        ],
        'h_build': 'Derleme',
        'build_intro': (
            '**JDK 17+** (17 veya 21 önerilir) ve **Android SDK Platform 35** '
            '(Build-Tools 35 AGP tarafından otomatik kurulur) gerekir.'
        ),
        'h_backup': 'Yedekleme ve Geliştirmeye Devam (Önemli)',
        'backup_intro': (
            '`ColorOS15ClockSeconds/` dizininde **`build/` ve `.gradle/` dışındaki tüm dosyalar** '
            'korunmalıdır (kaynak kısmı yalnızca ~140 KB):'
        ),
        'th_backup_cat': 'Kategori',
        'th_backup_files': 'Dosya',
        'th_backup_note': 'Açıklama',
        'backup_rows': [
            ('Derleme betikleri',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Sürüm, bağımlılıklar, derleyici ayarları'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Gradle 8.14.5 sabitlenir'),
            ('Kaynak kod', '`app/src/main/java/**` (3 `.java`)', 'Modül mantığının gövdesi'),
            ('Kaynaklar', '`app/src/main/res/**`',
             'Dizeler (varsayılan Çince + 21 dil values-xx), tema'),
            ('Xposed üstverisi', '`app/src/main/resources/META-INF/xposed/*`',
             'Giriş sınıfı, kapsam, API sürümü'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'İzinler, Activity'),
            ('Belgeler', '`README*.md`', '21 dilde belgeleme'),
            ('CI', '`.github/workflows/release.yml`', 'Etiketle imzalı derleme'),
        ],
        'h_trouble': 'Sorun Giderme',
        'trouble_summary': 'Sorun giderme adımlarını genişlet',
        'trouble_intro': 'logcat inceleyin:',
        'trouble': [
            ('b', '**Release derlemeleri** `BuildConfig.DEBUG` kapısıyla tüm tanılama günlüklerini '
                  'ayıklar (daha küçük, daha temiz); logcat bu modülden bir şey yazmaz.'),
            ('b', 'İnceleme için geçici olarak **debug derlemesini** (`assembleDebug`, aynı üretim '
                  'anahtarıyla imzalandığı için release kurulumunun üzerine yazılabilir) kurun, '
                  'sonra TAG ile süzün:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Saniyeler hiç görünmüyorsa: önce LSPosed SystemUI kapsamının işaretli ve '
                  'yeniden başlatıldığını doğrulayın; sonra anahtarın kapalı kalıcılaştırılıp '
                  'kalıcılaştırılmadığını adb ile okuyun / zorla yazın:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=açık 0=kapalı',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'ColorOS olmayan bir cihazda çalışmıyorsa: debug derlemesini kurun ve günlükteki '
                  '`candidate:` / `CONFIG CLOCK:` çıktılarına bakın\n'
                  'cihazınızın durum çubuğu saati kaynak adının / sınıf adının tanıma kurallarında '
                  'olup olmadığını görün; değilse tek bir kural satırı eklemek yeterlidir.'),
        ],
        'h_license': 'License',
    },
}
