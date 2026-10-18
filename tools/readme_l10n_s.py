# -*- coding: utf-8 -*-
"""Slot translations for README generation: Italiano (it)."""

TEXTS = {
    'it': {
        'title': "Secondi sull'orologio (Clock Seconds)",
        'tagline': (
            "Mostra i **secondi** sull'orologio della barra di stato, attiva/disattiva con un "
            "solo tocco e tieni la **decina dell'ora sempre rossa**.<br>"
            "Nessuna classe privata del produttore. Verificato su dispositivo reale con "
            "ColorOS 15; copre anche Xiaomi, OnePlus, Honor, vivo e gli altri grandi produttori."
        ),
        'h_features': 'Funzioni',
        'th_feature': 'Funzione',
        'th_desc': 'Descrizione',
        'rows': [
            ('Secondi',
             "L'orologio della barra di stato mostra i secondi (`HH:mm:ss` / `h:mm:ss a`, segue l'impostazione 12/24 ore del sistema)"),
            ('Tocca per attivare',
             "**Toccare** l'orologio della barra di stato attiva/disattiva i secondi; nella pagina delle impostazioni è disponibile anche il **tocco prolungato**"),
            ("Decina dell'ora in rosso",
             "La **decina dell'ora** viene colorata di rosso (l'`1` in `13`, lo `0` in `05`); "
             "minuti e secondi mantengono il colore di sistema. Le ore a una sola cifra "
             "(ad es. `9:45`) non hanno decina e restano intatte. Efficace su tutti i produttori"),
            ("Lingue dell'interfaccia",
             "L'interfaccia dell'app è tradotta in **21 lingue** (segue automaticamente la lingua di sistema): "
             "简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, "
             "Deutsch, Español, Français, Українська, Polski, Português (Brasil), "
             "فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული"),
            ('Persistenza',
             "Lo stato è salvato in `Settings.Secure`, condiviso tra processi e conservato dopo il riavvio di SystemUI"),
            ('Compatibilità',
             "Verificato su ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); funziona anche così "
             "com'è su altri produttori con root come Xiaomi / Honor / Redmi. Nessuna classe "
             "privata del produttore, quindi la modalità provvisoria di LSPosed non scatta. "
             "Minimo Android 10"),
        ],
        'h_install': 'Installazione',
        'install': [
            "Scarica l'APK più recente dalla pagina [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)",
            "Attiva il modulo in **LSPosed**, ambito: **Interfaccia di sistema (`com.android.systemui`)**",
            "Riavvia il processo SystemUI o riavvia il dispositivo",
            "Apri la pagina delle impostazioni dal **gestore LSPosed** (non viene creata alcuna icona nel launcher)",
        ],
        'note1': (
            "Richiede root (LSPosed). Per concedere `WRITE_SECURE_SETTINGS` e poter scrivere "
            "in `Settings.Secure`:"
        ),
        'note2': (
            "Senza quel permesso puoi comunque attivare/disattivare toccando l'orologio della "
            "barra di stato (quell'azione gira dentro il processo SystemUI, che ha già il diritto per natura)."
        ),
        'h_usage': 'Utilizzo',
        'usage': [
            "**Tocca l'orologio della barra di stato**: attiva/disattiva i secondi (gesto predefinito).",
            "L'attivazione è implementata con un test di tocco globale, quindi **non entra mai in "
            "conflitto con il «trascinamento del pannello notifiche»**—— un tocco puro attiva/disattiva, "
            "uno striscia continua a tirare giù il pannello. Se preferisci, puoi passare al gesto "
            "**tocco prolungato** nella pagina delle impostazioni.",
            "La pagina delle impostazioni mostra lo stato attuale, il gesto scelto e un pulsante di aggiornamento.",
        ],
        'h_demo': 'Anteprima',
        'cap_off': "Secondi disattivati: `15:06` — la cifra delle decine dell'ora `1` resta rossa",
        'cap_on': 'Secondi attivati: `15:06:03` — minuti e secondi mantengono il colore di sistema',
        'h_how': 'Come funziona',
        'how_intro': [
            "Il modulo aggancia solo **SystemUI**, usando l'approccio legacy Xposed collaudato,",
            "e **non tocca mai nessuna classe privata del produttore**, quindi non rompe lo stato "
            "interno del sistema né fa scattare la modalità provvisoria di LSPosed:",
        ],
        'how': [
            "**Secondi**: aggancia lo stabile `TextView.setText` (after) per mettere in cache il "
            "testo grezzo che il sistema mette sull'orologio della barra di stato,\n"
            "poi aggancia `TextView.onDraw` (before) per sovrascrivere la visualizzazione con "
            "`testoGrezzo:ss` dopo che il sistema ha impostato il testo,\n"
            "pilotato da un `Handler` da 1 secondo. I secondi aggiunti ereditano il colore del "
            "testo proprio dell'orologio (misurato `#ccffffff`).\n"
            "La decina dell'ora viene inoltre colorata di rosso (un `ForegroundColorSpan` rosso su "
            "quell'unico carattere), sia che i secondi siano mostrati sia che non lo siano;\n"
            "minuti e secondi non sono mai rossi; le ore a una sola cifra non hanno decina e restano intatte.",
            "**Attivazione**: il pannello notifiche di ColorOS intercetta i tocchi nell'area della "
            "barra di stato, quindi l'`OnTouchListener` proprio dell'orologio non riceve mai eventi.\n"
            "Il modulo aggancia perciò il `View.dispatchTouchEvent` globale e capovolge "
            "l'interruttore `Settings.Secure` solo quando la pressione cade dentro\n"
            "i limiti su schermo dell'orologio riconosciuto e non si muove quasi (tocco) oppure "
            "viene tenuta per 500 ms (tocco prolungato). Il modulo **non consuma**\n"
            "nessun evento di tocco, quindi il trascinamento del pannello notifiche funziona come al solito.",
        ],
        'h_power': "Nota sul risparmio energetico",
        'power': [
            "**Secondi ATTIVI**: il modulo ha bisogno di un timer da 1 secondo per pilotare gli "
            "aggiornamenti (il costo insito nel mostrare i secondi, circa 60 ridisegni "
            "dell'orologio al minuto).",
            "**Secondi DISATTIVI**: il modulo **ferma subito quel timer** (`stopTicker`)—— "
            "nessun risveglio ogni secondo e nessuna lettura tra processi.\n"
            "Torna al ritmo originale del sistema «aggiorna una volta al minuto», quindi "
            "disattivare i secondi fa davvero risparmiare energia.",
            "Lo stato è tenuto in **cache dentro il processo** (`sSecondsEnabled`), così il timer "
            "non legge `Settings.Secure` ogni secondo (evitando un IPC al secondo).",
            "Non viene costruito nessun formattatore di orario: il modulo riutilizza il testo "
            "grezzo del sistema e si limita ad aggiungere `:ss`, quindi non c'è overhead di formattazione.",
            "Il timer tiene solo un **riferimento debole** alla vista e continua solo mentre la "
            "vista è `isAttachedToWindow()`; quando la vista viene distrutta, esce automaticamente.",
        ],
        'h_build': 'Build',
        'build_intro': (
            "Richiede **JDK 17+** (17 o 21 consigliati) e **Android SDK Platform 35** "
            "(Build-Tools 35 viene installato automaticamente da AGP)."
        ),
        'h_backup': 'Backup e sviluppo continuativo (importante)',
        'backup_intro': (
            "Nella cartella `ClockSeconds/`, **vanno conservati tutti i file tranne "
            "`build/` e `.gradle/`** (la parte di codice pesa solo circa 140 KB):"
        ),
        'th_backup_cat': 'Categoria',
        'th_backup_files': 'File',
        'th_backup_note': 'Nota',
        'backup_rows': [
            ('Script di build',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Versione, dipendenze, parametri del compilatore'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fissa Gradle 8.14.5'),
            ('Codice sorgente', '`app/src/main/java/**` (3 file `.java`)', 'Il corpo della logica del modulo'),
            ('Risorse', '`app/src/main/res/**`',
             'Stringhe (cinese predefinito + 21 lingue values-xx), tema'),
            ('Metadati Xposed', '`app/src/main/resources/META-INF/xposed/*`',
             "Classe d'ingresso, ambito, versione API"),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permessi, Activity'),
            ('Documentazione', '`README*.md`', 'Documentazione in 21 lingue'),
            ('CI', '`.github/workflows/release.yml`', 'Un tag produce una build firmata'),
        ],
        'h_trouble': 'Risoluzione dei problemi',
        'trouble_summary': 'Espandi i passaggi di risoluzione dei problemi',
        'trouble_intro': 'Controlla il logcat:',
        'trouble': [
            ('b', 'Le **build Release** eliminano tutti i log diagnostici tramite la porta '
                  '`BuildConfig.DEBUG` (più leggera e pulita); in logcat non esce nulla da '
                  'questo modulo.'),
            ('b', 'Per indagare, installa temporaneamente la **build debug** (`assembleDebug`, '
                  'firmata con la stessa chiave di produzione, quindi installabile sopra la '
                  'Release) e filtra per TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Se i secondi non appaiono del tutto: per prima cosa conferma che LSPosed ha '
                  'la spunta su SystemUI e ha riavviato; poi verifica se l\'interruttore è '
                  'stato salvato come disattivato——\n'
                  'da leggere / forzare via adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=attivo 0=disattivo',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Se non ha effetto su un dispositivo non ColorOS: installa la build debug '
                  'e guarda le righe `candidate:` / `CONFIG CLOCK:` nel log\n'
                  "per vedere se il nome della risorsa / il nome della classe dell'orologio della "
                  "barra di stato del tuo dispositivo è coperto dalle regole di riconoscimento; "
                  "se non lo è, basta una riga di regola in più."),
        ],
        'h_license': 'License',
    },
}
