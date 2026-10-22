# -*- coding: utf-8 -*-
"""Slot translations for README generation: Polski (pl)."""

TEXTS = {
    'pl': {
        'title': 'Sekundy w czasie（Show Seconds in Time）',
        'tagline': (
            'Pokaż **sekundy** w czasie paska stanu, przełączaj jednym dotknięciem '
            'i trzymaj **dziesiątkę godziny na czerwono**.<br>'
            'Żadnych prywatnych klas producenta. Zweryfikowano na prawdziwym urządzeniu '
            'z ColorOS 15; obsługuje też Xiaomi, OnePlus, Honor, vivo, Samsung i innych dużych producentów.'
        ),
        'h_features': 'Funkcje',
        'th_feature': 'Funkcja',
        'th_desc': 'Opis',
        'rows': [
            ('Sekundy',
             'Czas paska stanu pokazuje sekundy (`HH:mm:ss` / `h:mm:ss a`, zgodnie z ustawieniem 12/24-godzinnym systemu)'),
            ('Dotknięcie przełącza',
             '**Dotknięcie** czasu paska stanu przełącza wyświetlanie sekund; na stronie ustawień dostępny jest też gest **podwójnego dotknięcia**'),
            ('Kolor dziesiątki godziny',
             'Kolor **dziesiątki godziny** można **dostosować** (paleta 8 kolorów) lub '
             '**wyłączyć**, wracając do koloru systemowego; godziny jednocyfrowe (np. `9:45`) '
             'nie mają dziesiątki i pozostają nietknięte. Działa u wszystkich producentów'),
            ('Podgląd po długim naciśnięciu',
             '**Długie naciśnięcie** czasu paska stanu otwiera **półprzezroczyste matowe okno** '
             'z **czasem sieciowym** w czasie rzeczywistym; przeciąganie, zmiana rozmiaru za ◣ '
             '(lewy dolny róg), dotknięcie czasu pokazuje **zamknij** (✕ prawy górny) / '
             '**rozciągnij**; rozmiar czcionki dopasowuje się do okna'),
            ('Czas sieciowy',
             'Strona ustawień porównuje zegar z **czasem standardowym sieci** w czasie '
             'rzeczywistym; przy dużym rozbieżności dotknij **Synchronizuj**, aby włączyć '
             'automatyczne ustawianie czasu'),
            ('Bez adb',
             'Ustawienia zapisują się w `Settings.Secure`; bez uprawnienia '
             '`WRITE_SECURE_SETTINGS` moduł automatycznie przechodzi na broadcast obsługiwany '
             'przez SystemUI — **bez uprawnień adb**'),
            ('Języki interfejsu',
             'Interfejs aplikacji jest przetłumaczony na **21 języków** (automatycznie podąża za językiem systemu): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Trwałość',
             'Stan zapisywany jest w `Settings.Secure`, współdzielony między procesami i przetrwa restarty SystemUI'),
            ('Zgodność',
             'Zweryfikowano na ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); działa również '
             'od razu u innych producentów z rootem, takich jak Xiaomi / Honor / Redmi / Samsung. '
             'Bez prywatnych klas producenta, więc tryb awaryjny LSPosed nie zostanie wywołany. '
             'Minimum Android 10'),            ('Kafelek',
             'Dodaj kafelek "Pokaż sekundy" do panelu, przełączaj jednym stuknięciem'),
            ('Ulepszony float',
             'Pozycja i rozmiar okna są **zapamiętywane**; **podwójne stuknięcie** czasu też przełącza'),
            ('Oszczędzanie energii',
             'W trybie oszczędzania odświeżanie co sekundę pauzuje (sekundy zamrożone), po wyłączeniu wznawia'),
            ('Wykrywanie konfigurowalne',
             'Jeśli zegar nie jest wykrywany na innych urządzeniach, naciśnij **Autowykrywanie** w ustawieniach lub wpisz nazwę zasobu (np. clock) ręcznie'),

        ],
        'h_install': 'Instalacja',
        'install': [
            'Pobierz najnowszy APK ze strony [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Włącz moduł w **LSPosed**, zakres: **Interfejs systemu (`com.android.systemui`)**',
            'Zrestartuj proces SystemUI lub zrestartuj urządzenie',
            'Stronę ustawień otwieraj z **menadżera LSPosed** (na pulpicie nie tworzy się ikona)',
        ],
        'note1': (
            'Wymaga roota (LSPosed). Aby przyznać `WRITE_SECURE_SETTINGS` do zapisu '
            'w `Settings.Secure`:'
        ),
        'note2': (
            'Bez niego nadal możesz przełączać, dotykając czasu paska stanu '
            '(ta akcja działa wewnątrz procesu SystemUI, który z natury ma uprawnienie).'
        ),
        'h_usage': 'Użytkowanie',
        'usage': [
            '**Dotknij czasu paska stanu**: przełącza wyświetlanie sekund (gest domyślny).',
            'Przełączanie realizowane jest przez globalne trafienie dotyku, więc **nigdy nie '
            'koliduje z „zejściem panelu powiadomień”**—— czyste dotknięcie przełącza, przeciągnięcie '
            'nadal rozwija panel. Jeśli wolisz, możesz zmienić gest na **podwójne dotknięcie** '
            'na stronie ustawień.',
            'Strona ustawień pokazuje aktualny stan, wybrany gest i przycisk odświeżenia.',
        ],
        'h_demo': 'Podgląd',
        'cap_off': 'Sekundy wyłączone: `15:06` — dziesiątka godziny `1` pozostaje czerwona',
        'cap_on': 'Sekundy włączone: `15:06:03` — minuty i sekundy zachowują kolor systemowy',
        'h_how': 'Jak to działa',
        'how_intro': [
            'Moduł hakuje tylko **SystemUI**, stosując sprawdzone podejście legacy Xposed,',
            'i **w ogóle nie dotyka prywatnych klas producenta**, więc nie uszkodzi wewnętrznego '
            'stanu systemu ani nie wywoła trybu awaryjnego LSPosed:',
        ],
        'how': [
            '**Sekundy**: hak stabilnego `TextView.setText` (after) buforuje surowy tekst, który '
            'system kładzie na czas paska stanu,\n'
            'potem hak `TextView.onDraw` (before) nadpisuje wyświetlanie wartością `surowyTekst:ss` '
            'po tym, jak system ustawił tekst,\n'
            'napędzany `Handler`em 1-sekundowym. Doklejone sekundy przejmują własny kolor tekstu '
            'czasu (zmierzony `#ccffffff`).\n'
            'Dodatkowo dziesiątka godziny jest kolorowana na czerwono (czerwony `ForegroundColorSpan` '
            'na tym jednym znaku), niezależnie od tego, czy sekundy są wyświetlane;\n'
            'minuty i sekundy nigdy nie są czerwone; godziny jednocyfrowe nie mają dziesiątki '
            'i pozostają nietknięte.',
            '**Przełączanie**: panel powiadomień ColorOS przechwytuje dotyki w obszarze paska '
            'stanu, więc własny `OnTouchListener` czasu nigdy nie otrzymuje zdarzeń.\n'
            'Moduł dlatego hakuje globalny `View.dispatchTouchEvent` i przewraca przełącznik '
            '`Settings.Secure` tylko wtedy, gdy naciśnięcie trafia\n'
            'w granice rozpoznanego czasu na ekranie i prawie się nie przesuwa (dotknięcie) albo '
            'jest drugie dotknięcie w ciągu 350 ms (podwójne dotknięcie). Moduł **nie zużywa**\n'
            'żadnego zdarzenia dotyku, więc przeciąganie panelu powiadomień działa jak zwykle.',
        ],
        'h_power': 'Uwaga o oszczędności energii',
        'power': [
            '**Sekundy WŁĄCZONE**: moduł potrzebuje 1-sekundowego licznika do napędzania '
            'odświeżania (nieodłączny koszt pokazywania sekund, około 60 przemalowań czasu na minutę).',
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
            'W katalogu `ClockSeconds/` **wszystkie pliki poza `build/` i `.gradle/` '
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
                  'aby sprawdzić, czy nazwa zasobu / nazwa klasy czasu paska stanu twojego '
                  'urządzenia jest objęta regułami rozpoznawania; jeśli nie, wystarczy dodać '
                  'jedną linię reguły.'),
        ],
        'h_feedback': 'Opinie',
        'feedback': 'Masz pytania, sugestie lub uwagi? Zostaw wiadomość na [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) — odpowiem najszybciej, jak to możliwe.',
        'h_license': 'License',
    },
}
