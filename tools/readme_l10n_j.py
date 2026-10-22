# -*- coding: utf-8 -*-
"""Slot translations for README generation: Deutsch (de)."""

TEXTS = {
    'de': {
        'title': 'Sekundenanzeige（Show Seconds in Time）',
        'tagline': (
            'Zeigt **Sekunden** in der Statusleisten-Zeit an, Umschalten mit einmaligem Tippen, '
            'und die **Zehnerstelle der Stunde bleibt rot**.<br>'
            'Keine herstellereigenen privaten Klassen nötig. Auf einem echten Gerät mit '
            'ColorOS 15 verifiziert; deckt ebenfalls Xiaomi, OnePlus, Honor, vivo, Samsung und '
            'andere große Hersteller ab.'
        ),
        'h_features': 'Funktionen',
        'th_feature': 'Funktion',
        'th_desc': 'Beschreibung',
        'rows': [
            ('Sekundenanzeige',
             'Die Statusleisten-Zeit zeigt Sekunden an (`HH:mm:ss` / `h:mm:ss a`, folgt der 12/24-Stunden-Einstellung des Systems)'),
            ('Tippen zum Umschalten',
             '**Tippen** auf die Statusleisten-Zeit schaltet die Sekundenanzeige ein/aus; im Einstellungsbereich auch **doppeltes Tippen** möglich'),
            ('Farbe der Stundenziffer',
             'Die Farbe der **Zehnerstelle der Stunde** ist **anpassbar** (8-Farben-Palette) oder '
             '**abschaltbar** für die Systemfarbe; einstellige Stunden (z. B. `9:45`) haben '
             'keine Zehnerstelle und bleiben unberührt. Wirkt bei allen Herstellern'),
            ('Vorschau bei langem Drücken',
             '**Langes Drücken** auf die Statusleisten-Zeit öffnet ein **milchglasartiges '
             'schwebendes Fenster** mit **Netzzeit** in Echtzeit; per Drag verschiebbar, '
             'über ◣ unten links skalierbar, Tippen auf die Zeit zeigt **Schließen** '
             '(✕ oben rechts) / **Skalieren**; Schriftgröße passt sich dem Fenster an'),
            ('Netzzeit',
             'Die Einstellungsseite vergleicht die Uhr in Echtzeit mit der **Netz-Standardzeit**; '
             'bei großer Abweichung **Synchronisieren** tippen, um die automatische '
             'Zeiteinstellung zu aktivieren'),
            ('Kein adb nötig',
             'Einstellungen werden in `Settings.Secure` geschrieben; ohne `WRITE_SECURE_SETTINGS` '
             'weicht das Modul automatisch auf einen Broadcast aus, den SystemUI verarbeitet — '
             '**keine adb-Freigabe nötig**'),
            ('Oberflächensprachen',
             'Die App-Oberfläche ist in **21 Sprachen** übersetzt (folgt automatisch der Systemsprache): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Persistenz',
             'Der Zustand liegt in `Settings.Secure`, ist prozessübergreifend gemeinsam genutzt und überlebt SystemUI-Neustarts'),
            ('Kompatibilität',
             'Auf ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme) auf echtem Gerät verifiziert; '
             'funktioniert auch auf anderen gerooteten Herstellern wie Xiaomi / Honor / Redmi / Samsung. '
             'Keine herstellereigenen privaten Klassen, daher wird der LSPosed-Sicherheitsmodus '
             'nicht ausgelöst. Mindestens Android 10'),            ('Schnellkachel',
             'Füge eine "Sekunden anzeigen"-Kachel zum Benachrichtigungsbereich hinzu, mit einem Tipp umschalten'),
            ('Float verbessert',
             'Position & Größe des Floats werden **gemerkt**; zeigt das **Datum**; **Doppeltipp** auf die Zeit schaltet Sekunden um'),
            ('Energiesparerkennung',
             'Bei Aktivierung des Energiesparmodus pausiert die Sekundenaktualisierung (Sekunden einfrieren), danach sofort weiter'),
            ('Erkennung konfigurierbar',
             'Wird die Uhr auf anderen Geräten nicht erkannt, gib den Ressourcennamen (z. B. clock) in den Einstellungen ein. Leer = eingebaute Regeln'),

        ],
        'h_install': 'Installation',
        'install': [
            'Laden Sie die neueste APK von der Seite [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) herunter',
            'Aktivieren Sie das Modul in **LSPosed**, Wirkungsbereich: **Systemoberfläche (`com.android.systemui`)**',
            'Starten Sie den SystemUI-Prozess neu oder starten Sie das Gerät neu',
            'Öffnen Sie die Einstellungsseite aus dem **LSPosed-Manager** heraus (auf dem Startbildschirm wird kein Symbol erzeugt)',
        ],
        'note1': (
            'Root (LSPosed) erforderlich. Um `WRITE_SECURE_SETTINGS` zum Schreiben in '
            '`Settings.Secure` zu erteilen:'
        ),
        'note2': (
            'Ohne die Berechtigung können Sie weiterhin durch Tippen auf die Statusleisten-Zeit '
            'umschalten (diese Aktion läuft im SystemUI-Prozess und hat das Recht von Haus aus).'
        ),
        'h_usage': 'Verwendung',
        'usage': [
            '**Auf die Statusleisten-Zeit tippen**: schaltet die Sekundenanzeige ein/aus (Standardgeste).',
            'Das Umschalten wird über eine globale Touch-Trefferprüfung realisiert und **konfligiert '
            'niemals mit dem „Herunterziehen des Benachrichtigungsfelds“**—— reines Tippen schaltet um, '
            'Wischen zieht das Benachrichtigungsfeld weiterhin herunter. Wer es lieber mag, kann die '
            'Geste im Einstellungsbereich auf **doppeltes Tippen** umstellen.',
            'Die Einstellungsseite zeigt den aktuellen Zustand, die gewählte Geste und eine Aktualisieren-Schaltfläche.',
        ],
        'h_demo': 'Vorschau',
        'cap_off': 'Sekunden aus: `15:06` — die Zehnerstelle der Stunde `1` bleibt rot',
        'cap_on': 'Sekunden an: `15:06:03` — Minuten und Sekunden behalten die Systemfarbe',
        'h_how': 'Funktionsweise',
        'how_intro': [
            'Das Modul hookt nur **SystemUI** und nutzt den bewährten Legacy-Xposed-Ansatz,',
            '**berührt keinerlei herstellereigene private Klassen** und beschädigt daher nie den '
            'internen Systemzustand oder löst den LSPosed-Sicherheitsmodus aus:',
        ],
        'how': [
            '**Sekunden**: Hook des stabilen `TextView.setText` (after) cached den Rohtext, den das '
            'System auf die Statusleisten-Zeit schreibt,\n'
            'dann Hook von `TextView.onDraw` (before), um die Anzeige mit `Rohtext:ss` zu '
            'überschreiben, nachdem das System den Text gesetzt hat,\n'
            'angetrieben von einem 1-Sekunden-`Handler`. Die angehängten Sekunden übernehmen die '
            'eigene Textfarbe der Zeit (gemessen `#ccffffff`).\n'
            'Zusätzlich wird die Zehnerstelle der Stunde rot gefärbt (ein roter `ForegroundColorSpan` '
            'auf diesem einen Zeichen), unabhängig davon, ob Sekunden angezeigt werden;\n'
            'Minuten und Sekunden werden nie rot; einstellige Stunden haben keine Zehnerstelle und '
            'bleiben unberührt.',
            '**Umschalten**: Das Benachrichtigungsfeld von ColorOS fängt Touches im Bereich der '
            'Statusleiste ab, daher erhält der eigene `OnTouchListener` der Zeit keine Ereignisse.\n'
            'Das Modul hookt deshalb das globale `View.dispatchTouchEvent` und dreht den '
            '`Settings.Secure`-Schalter nur, wenn der Druckpunkt\n'
            'innerhalb der Bildschirmgrenzen der erkannten Zeit liegt und sich fast nicht bewegt '
            '(Tippen) oder innerhalb 350 ms ein zweites Tippen erfolgt (doppeltes Tippen). Das Modul **verbraucht**\n'
            'kein Touch-Ereignis, das Herunterziehen des Benachrichtigungsfelds funktioniert wie gewohnt.',
        ],
        'h_power': 'Hinweise zum Stromverbrauch',
        'power': [
            '**Sekundenanzeige AN**: Das Modul braucht einen 1-Sekunden-Timer für die '
            'Aktualisierungen (die inhärente Kosten des Sekundenanzeigens, rund 60 Neuzeichnungen '
            'der Zeit pro Minute).',
            '**Sekundenanzeige AUS**: Das Modul **stoppt diesen Timer sofort** (`stopTicker`)—— '
            'überhaupt keine Aufwachvorgänge pro Sekunde mehr und keine prozessübergreifenden Lesevorgänge.\n'
            'Es kehrt zum ursprünglichen Rhythmus des Systems „einmal pro Minute aktualisieren“ zurück, '
            'daher ist das Ausschalten der Sekundenanzeige wirklich sparsamer.',
            'Der Zustand wird **prozessintern gecacht** (`sSecondsEnabled`), sodass der Timer '
            'nicht in jeder Sekunde `Settings.Secure` liest (ein IPC pro Sekunde wird vermieden).',
            'Es wird kein Zeitformatierer konstruiert: Das Modul verwendet den Rohtext des Systems '
            'weiter und hängt nur `:ss` an, daher gibt es keinen Formatierer-Overhead.',
            'Der Timer hält nur eine **schwache Referenz** auf die View und läuft nur weiter, '
            'solange die View `isAttachedToWindow()` ist; nach dem Zerstören der View endet er automatisch.',
        ],
        'h_build': 'Build',
        'build_intro': (
            'Erfordert **JDK 17+** (17 oder 21 empfohlen) und **Android SDK Platform 35** '
            '(Build-Tools 35 wird von AGP automatisch installiert).'
        ),
        'h_backup': 'Backup & Weiterentwicklung (wichtig)',
        'backup_intro': (
            'Im Verzeichnis `ClockSeconds/` müssen **alle Dateien außer `build/` und '
            '`.gradle/`** aufgehoben werden (der Quelltextteil ist nur etwa 140 KB groß):'
        ),
        'th_backup_cat': 'Kategorie',
        'th_backup_files': 'Datei',
        'th_backup_note': 'Hinweis',
        'backup_rows': [
            ('Build-Skripte',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Version, Abhängigkeiten, Compiler-Parameter'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fixiert Gradle 8.14.5'),
            ('Quellcode', '`app/src/main/java/**` (3 `.java`-Dateien)', 'Der Kern der Modullogik'),
            ('Ressourcen', '`app/src/main/res/**`',
             'Strings (Standard Chinesisch + 21 values-xx-Sprachen), Theme'),
            ('Xposed-Metadaten', '`app/src/main/resources/META-INF/xposed/*`',
             'Einstiegsklasse, Wirkungsbereich, API-Version'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Berechtigungen, Activity'),
            ('Dokumentation', '`README*.md`', 'Dokumentation in 21 Sprachen'),
            ('CI', '`.github/workflows/release.yml`', 'Tag erzeugt signierten Build'),
        ],
        'h_trouble': 'Fehlerbehebung',
        'trouble_summary': 'Schritte zur Fehlerbehebung aufklappen',
        'trouble_intro': 'Logcat prüfen:',
        'trouble': [
            ('b', '**Release-Builds** entfernen über das `BuildConfig.DEBUG`-Gate alle '
                  'Diagnoselogs (kleiner, aufgeräumter); in Logcat erscheint von diesem Modul nichts.'),
            ('b', 'Zur Untersuchung installieren Sie vorübergehend den **Debug-Build** '
                  '(`assembleDebug`, mit demselben Produktionsschlüssel signiert, kann also direkt '
                  'über die Release-Installation installiert werden) und filtern Sie nach TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Falls überhaupt keine Sekunden erscheinen: Prüfen Sie zuerst, ob in LSPosed der '
                  'SystemUI-Wirkungsbereich angehakt und neu gestartet wurde; prüfen Sie dann, ob der '
                  'Schalter als aus persistiert wurde——\n'
                  'auslesen / zwangsweise schreiben per adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=an 0=aus',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Falls es auf einem Nicht-ColorOS-Gerät nicht wirkt: Installieren Sie den '
                  'Debug-Build und sehen Sie sich die Zeilen `candidate:` / `CONFIG CLOCK:` im Log an,\n'
                  'um zu prüfen, ob Ressourcenname / Klassenname der Statusleisten-Zeit Ihres Geräts '
                  'von den Erkennungsregeln abgedeckt sind; wenn nicht, genügt eine zusätzliche Regelzeile.'),
        ],
        'h_feedback': 'Feedback',
        'feedback': 'Fragen, Ideen oder Rückmeldungen? Hinterlasse einfach eine Nachricht auf [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) — ich melde mich bald.',
        'h_license': 'License',
    },
}
