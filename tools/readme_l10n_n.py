# -*- coding: utf-8 -*-
"""Slot translations for README generation: Polski (pl)."""

TEXTS = {
    'pl': {
        'title': 'Sekundy w zegarze (Clock Seconds)',
        'tagline': (
            'Pokaż **sekundy** na zegarze paska stanu, przełączaj jednym dotknięciem '
            'i trzymaj **dziesiątkę godziny na czerwono**.<br>'
            'Żadnych prywatnych klas producenta. Zweryfikowano na prawdziwym urządzeniu '
            'z ColorOS 15; obsługuje też Xiaomi, OnePlus, Honor, vivo i innych dużych producentów.'
        ),
        'h_features': 'Funkcje',
        'th_feature': 'Funkcja',
        'th_desc': 'Opis',
        'rows': [
            ('Sekundy',
             'Zegar paska stanu pokazuje sekundy (`HH:mm:ss` / `h:mm:ss a`, zgodnie z ustawieniem 12/24-godzinnym systemu)'),
            ('Dotknięcie przełącza',
             '**Dotknięcie** zegara paska stanu przełącza wyświetlanie sekund; na stronie ustawień dostępny jest też gest **długiego naciśnięcia**'),
            ('Czerwona dziesiątka godziny',
             '**Dziesiątka godziny** jest kolorowana na czerwono (`1` w `13`, `0` w `05`); '
             'minuty i sekundy zachowują kolor systemowy. Godziny jednocyfrowe (np. `9:45`) '
             'nie mają dziesiątki i pozostają nietknięte. Działa u wszystkich producentów'),
            ('Języki interfejsu',
             'Interfejs aplikacji jest przetłumaczony na **21 języków** (automatycznie podąża za językiem systemu): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Trwałość',
             'Stan zapisywany jest w `Settings.Secure`, współdzielony między procesami i przetrwa restarty SystemUI'),
            ('Zgodność',
             'Zweryfikowano na ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); działa również '
             'od razu u innych producentów z rootem, takich jak Xiaomi / Honor / Redmi. '
             'Bez prywatnych klas producenta, więc tryb awaryjny LSPosed nie zostanie wywołany. '
             'Minimum Android 10'),
        ],
        'h_install': 'Instalacja',
        'install': [
            'Pobierz najnowszy APK ze strony [**Releases**](https://github.com/kelebao/ColorOS15ClockSeconds/releases/latest)',
            'Włącz moduł w **LSPosed**, zakres: **Interfejs systemu (`com.android.systemui`)**',
            'Zrestartuj proces SystemUI lub zrestartuj urządzenie',
            'Stronę ustawień otwieraj z **menadżera LSPosed** (na pulpicie nie tworzy się ikona)',
        ],
        'note1': (
            'Wymaga roota (LSPosed). Aby przyznać `WRITE_SECURE_SETTINGS` do zapisu '
            'w `Settings.Secure`:'
        ),
        'note2': (
            'Bez niego nadal możesz przełączać, dotykając zegara paska stanu '
            '(ta akcja działa wewnątrz procesu SystemUI, który z natury ma uprawnienie).'
        ),
        'h_usage': 'Użytkowanie',
        'usage': [
            '**Dotknij zegara paska stanu**: przełącza wyświetlanie sekund (gest domyślny).',
            'Przełączanie realizowane jest przez globalne trafienie dotyku, więc **nigdy nie '
            'koliduje z „zejściem panelu powiadomień”**—— czyste dotknięcie przełącza, przeciągnięcie '
            'nadal rozwija panel. Jeśli wolisz, możesz zmienić gest na **długie naciśnięcie** '
            'na stronie ustawień.',
            'Strona ustawień pokazuje aktualny stan, wybrany gest i przycisk odświeżenia.',
        ],
        'h_how': 'Jak to działa',
        'how_intro': [
            'Moduł hakuje tylko **SystemUI**, stosując sprawdzone podejście legacy Xposed,',
            'i **w ogóle nie dotyka prywatnych klas producenta**, więc nie uszkodzi wewnętrznego '
            'stanu systemu ani nie wywoła trybu awaryjnego LSPosed:',
        ],
        'how': [
            '**Sekundy**: hak stabilnego `TextView.setText` (after) buforuje surowy tekst, który '
            'system kładzie na zegar paska stanu,\n'
            'potem hak `TextView.onDraw` (before) nadpisuje wyświetlanie wartością `surowyTekst:ss` '
            'po tym, jak system ustawił tekst,\n'
            'napędzany `Handler`em 1-sekundowym. Doklejone sekundy przejmują własny kolor tekstu '
            'zegara (zmierzony `#ccffffff`).\n'
            'Dodatkowo dziesiątka godziny jest kolorowana na czerwono (czerwony `ForegroundColorSpan` '
            'na tym jednym znaku), niezależnie od tego, czy sekundy są wyświetlane;\n'
            'minuty i sekundy nigdy nie są czerwone; godziny jednocyfrowe nie mają dziesiątki '
            'i pozostają nietknięte.',
            '**Przełączanie**: panel powiadomień ColorOS przechwytuje dotyki w obszarze paska '
            'stanu, więc własny `OnTouchListener` zegara nigdy nie otrzymuje zdarzeń.\n'
            'Moduł dlatego hakuje globalny `View.dispatchTouchEvent` i przewraca przełącznik '
            '`Settings.Secure` tylko wtedy, gdy naciśnięcie trafia\n'
            'w granice rozpoznanego zegara na ekranie i prawie się nie przesuwa (dotknięcie) albo '
            'jest trzymane 500 ms (długie naciśnięcie). Moduł **nie zużywa**\n'
            'żadnego zdarzenia dotyku, więc przeciąganie panelu powiadomień działa jak zwykle.',
        ],
        'h_power': 'Uwaga o oszczędności energii',
        'power': [
            '**Sekundy WŁĄCZONE**: moduł potrzebuje 1-sekundowego licznika do napędzania '
            'odświeżania (nieodłączny koszt pokazywania sekund, około 60 przemalowań zegara na minutę).',
            '**Sekundy WYŁĄCZONE**: moduł **natychmiast zatrzymuje ten licznik** (`stopTicker`)—— '
            'żadnych budzeń co sekundę i żadnych odczytów międzyprocesowych.\n'
            'Wraca do pierwotnego rytmu systemu „odświeżaj raz na minutę”, więc wyłączenie sekund '
            'naprawdę oszczędza energię.',
            'Stan jest buforowany **wewnątrzprocesowo** (`sSecondsEnabled`), więc licznik nie czyta '
            '`Settings.Secure` co sekundę (unikany jest IPC co sekundę).',
            'Nie jest tworzony żaden formater czasu: moduł wykorzystuje ponownie surowy tekst '
            'systemu i tylko dokleja `:ss`, więc nie ma narzutu formatowania.',
            'Licznik trzyma tylko **słabą referencję** do widoku i działa tylko, gdy widok jest '
            '`isAttachedToWindow()`; po zniszczeniu widoku kończy się automatycznie.',
        ],
        'h_build': 'Budowanie',
        'build_intro': (
            'Wymaga **JDK 17+** (zalecane 17 lub 21) oraz **Android SDK Platform 35** '
            '(Build-Tools 35 instaluje AGP automatycznie).'
        ),
        'h_backup': 'Kopia zapasowa i dalszy rozwój (ważne)',
        'backup_intro': (
            'W katalogu `ColorOS15ClockSeconds/` **wszystkie pliki poza `build/` i `.gradle/` '
            'mają być zachowane** (część źródłowa waży tylko około 140 KB):'
        ),
        'th_backup_cat': 'Kategoria',
        'th_backup_files': 'Plik',
        'th_backup_note': 'Uwaga',
        'backup_rows': [
            ('Skrypty budowania',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Wersja, zależności, parametry kompilacji'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Przypina Gradle 8.14.5'),
            ('Kod źródłowy', '`app/src/main/java/**` (3 pliki `.java`)', 'Trzon logiki modułu'),
            ('Zasoby', '`app/src/main/res/**`',
             'Ciągi (domyślnie chiński + 21 języków values-xx), motyw'),
            ('Metadane Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             'Klasa wejścia, zakres, wersja API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Uprawnienia, Activity'),
            ('Dokumentacja', '`README*.md`', 'Dokumentacja w 21 językach'),
            ('CI', '`.github/workflows/release.yml`', 'Tag daje podpisany build'),
        ],
        'h_trouble': 'Rozwiązywanie problemów',
        'trouble_summary': 'Rozwiń kroki rozwiązywania problemów',
        'trouble_intro': 'Sprawdź logcat:',
        'trouble': [
            ('b', '**Buildy Release** usuwają wszystkie logi diagnostyczne przez bramkę '
                  '`BuildConfig.DEBUG` (mniejszy, czystszy); w logcacie nie ma nic z tego modułu.'),
            ('b', 'Do zdiagnozowania zainstaluj tymczasowo **build debug** (`assembleDebug`, '
                  'podpisany tym samym kluczem produkcyjnym, więc można go postawić nad Release) '
                  'i filtruj po TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Jeśli sekundy nie pokazują się wcale: najpierw potwierdź, że w LSPosed zakres '
                  'SystemUI jest zaznaczony i zrestartowany; potem sprawdź, czy przełącznik nie '
                  'zapisał się jako wyłączony—\n'
                  'do odczytu / wymuszenia przez adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=wł 0=wył',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Jeśli nie działa na urządzeniu innym niż ColorOS: zainstaluj build debug '
                  'i spójrz na linie `candidate:` / `CONFIG CLOCK:` w logu,\n'
                  'aby sprawdzić, czy nazwa zasobu / nazwa klasy zegara paska stanu twojego '
                  'urządzenia jest objęta regułami rozpoznawania; jeśli nie, wystarczy dodać '
                  'jedną linię reguły.'),
        ],
        'h_license': 'License',
    },
}
