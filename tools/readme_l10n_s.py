# -*- coding: utf-8 -*-
"""Slot translations for README generation: Italiano (it)."""

TEXTS = {
    'it': {
        'title': 'Secondi nella barra di stato (Show Seconds in Time)',
        'tagline': (
            'Mostra i **secondi** nell\'orologio della barra di stato, si attiva con un tocco. Puoi dare '
            '**un colore a ogni cifra** o applicare un **gradiente** all\'intera stringa.<br>'
            '**Pressione lunga**: finestra traslucida con l\'**ora di rete** (trascinamento, dimensione, gradiente); '
            'le **attività personali** accendono i secondi all\'ora scelta.<br>'
            'Il percorso principale usa solo API pubbliche AOSP (le classi private OPPO / OnePlus / realme sono '
            'opzionali e saltate se assenti). Testato su ColorOS 15 / 16.'
        ),
        'h_features': 'Funzioni',
        'th_feature': 'Funzione',
        'th_desc': 'Dettaglio',
        'rows': [
            ('Secondi nella barra',
             '`HH:mm:ss` / `h:mm:ss a`, rispetta il formato 12/24 h e il fuso orario. In coda viene aggiunto uno '
             '**spazio di larghezza zero** così i temi non tolgono i colori; se i colori cadono, il testo viene '
             'ridisegnato automaticamente (pausa di 5 minuti).'),
            ('Attivazione al tocco',
             '**Toccare** l\'ora attiva o disattiva i secondi (si può scegliere il **doppio tocco**); la '
             '**pressione lunga** è riservata alla finestra di anteprima. Dentro un\'attività viene mostrato che è '
             'lei a comandare.'),
            ('Colore per cifra / gradiente',
             'Sei cifre **ognuna del proprio colore** (10 preset + slider HSV + colore di sistema) e i due punti '
             'centrati otticamente. Oppure un **gradiente** dal colore iniziale a quello finale (6 set, estremi '
             'modificabili, scurito su fondo chiaro). L\'anteprima **si disegna da sola**, quindi coincide con la '
             'barra pixel per pixel.'),
            ('Finestra su pressione lunga',
             'Finestra traslucida con l\'**ora di rete**; trascina per spostare, angolo ◣ per la dimensione, '
             'posizione e dimensione **vengono ricordate**; gradiente e cifre a larghezza fissa.'),
            ('Attività personali',
             'Finestre **ogni ora** o **ogni giorno**: accendere i secondi N minuti prima per M minuti e poi '
             'ripristinare. Il cambio di ora o fuso viene ricalcolato subito, più un controllo ogni 60 secondi.'),
            ('Confronto con l\'ora di rete',
             'Mostra lo scostamento dall\'**ora di rete**; un tocco su **Sincronizza** attiva l\'ora automatica '
             'di sistema.'),
            ('Nessun adb necessario',
             'La scrittura passa da `Settings.Secure`; senza `WRITE_SECURE_SETTINGS` un broadcast fa scrivere la '
             'SystemUI — **nessuna funzione richiede adb**.'),
            ('Riquadro rapido',
             'Aggiungi il riquadro «Mostra secondi» per cambiare senza aprire l\'app.'),
            ('Nome risorsa dell\'orologio',
             'Se l\'orologio non viene riconosciuto, tocca **Rilevamento automatico** o inserisci il nome della '
             'risorsa (es. `clock`).'),
            ('21 lingue dell\'interfaccia',
             'Segue la lingua di sistema: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, '
             'ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, '
             'Bahasa Indonesia, Italiano, Ελληνικά, ქართული; le altre usano l\'inglese.'),
            ('Archiviazione / ambito',
             'Lo stato è in `Settings.Secure`, condiviso tra processi e persistente dopo il riavvio. '
             '**Testato su ColorOS 15 / 16 (Android 15 / 16)**; il nucleo usa solo API pubbliche, quindi altri '
             'marchi dovrebbero funzionare, ma sono stati provati solo OPPO / OnePlus. Minimo Android 10.'),
        ],
        'h_colors': 'Modalità colore',
        'colors_intro': (
            'Due modi di colorare, si cambiano nella scheda **Colori delle cifre**: **per cifra** '
            'o **gradiente** sull\'intera stringa.'
        ),
        'cap_digit': 'Per cifra: ogni cifra ha il suo colore, due punti centrati otticamente',
        'cap_grad': 'Gradiente: dal colore iniziale a quello finale, 6 set o estremi personalizzati',
        'h_install': 'Installazione',
        'install': [
            'Scarica l\'APK da [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Attiva il modulo in **LSPosed** e seleziona l\'ambito **Interfaccia di sistema** '
            '(`com.android.systemui`)',
            'Riavvia il processo SystemUI o il dispositivo',
            'La schermata impostazioni si apre dal **gestore LSPosed** (nessuna icona nel launcher)',
        ],
        'note1': (
            'Serve root (LSPosed). **Di norma non servono permessi adb**: la scrittura usa '
            '`Settings.Secure` e, senza `WRITE_SECURE_SETTINGS`, un broadcast fa scrivere la SystemUI. Per far '
            'scrivere **direttamente** la schermata impostazioni:'
        ),
        'note2': 'Senza concedere nulla funziona tutto: tocchi, attività e finestra girano dentro la SystemUI.',
        'h_usage': 'Uso',
        'usage': [
            '**Tocca l\'ora**: secondi on/off (predefinito); nelle impostazioni si può usare il **doppio tocco**.',
            '**Pressione lunga**: finestra traslucida con l\'**ora di rete**, trascina per spostare; toccando '
            'l\'ora compaiono **chiudi** (in alto a destra) e **dimensione** (in basso a sinistra).',
            'La scheda **Colori delle cifre** ha due pagine e la linguetta stessa è l\'interruttore.',
            'La scheda **Attività personali** pianifica finestre come «secondi 2 minuti prima di ogni ora».',
            'Il cambio usa un controllo globale del tocco e **non interferisce con il tendaggio** delle '
            'notifiche.',
        ],
        'h_demo': 'Anteprima',
        'cap_off': 'Secondi off: `15:06`, la decina d\'ora mantiene il colore',
        'cap_on': 'Secondi on: `15:06:03`, minuti e secondi nel colore di sistema',
        'h_how': 'Come funziona',
        'how_intro': [
            'Il modulo aggancia solo la **SystemUI** e usa l\'API legacy Xposed, stabile.',
            '**Il nucleo usa solo API pubbliche AOSP; le classi del produttore sono opzionali e saltate se '
            'assenti**, quindi non rompe il sistema e non attiva la modalità provvisoria di LSPosed:',
        ],
        'how': [
            '**Secondi**: `TextView.setText` (after) memorizza il testo di sistema, poi `TextView.onDraw` '
            '(before) scrive `originale:ss`, aggiornato ogni secondo con `Handler`.',
            '**Colore per cifra**: un `ForegroundColorSpan` per carattere (gli span estranei vengono rimossi); i '
            'due punti salgono di 0.11em perché i layout a più segmenti mettono il glifo più in basso.',
            '**Gradiente**: `CharacterStyle` chiama `setShader` in `updateDrawState`; l\'anteprima **si disegna '
            'da sola** (`GradientTimeView`) perché ColorOS 15 quantizza lo shader per segmento di testo.',
            '**Cambio stato**: il pannello intercetta i tocchi sulla barra, quindi il modulo aggancia '
            '`View.dispatchTouchEvent` globale e cambia solo se il tocco cade nell\'orologio senza trascinamento; '
            'l\'evento **non viene consumato**.',
            '**Attività**: verifica «l\'ora locale è nella finestra», timer esatti ai bordi e controllo ogni 60 '
            'secondi.',
        ],
        'h_power': 'Consumo',
        'power': [
            '**Secondi on**: timer ogni secondo (costo inevitabile). Misura su SystemUI per core: 7.0% off, '
            '7.25% on (60 s).',
            '**Secondi off**: il timer **si ferma subito** (`stopTicker`), nessun risveglio al secondo.',
            'La **protezione colore** rallenta 2s → 4s → … → 30s e termina dopo 5 minuti senza perdite.',
            'Lo stato è **in cache nel processo**; le attività usano `Handler.postDelayed`: **senza WakeLock né '
            'AlarmManager**, non svegliano il sonno profondo.',
        ],
        'h_build': 'Compilazione',
        'build_intro': 'Servono **JDK 17+** (17 o 21) e **Android SDK Platform 35** (AGP scarica Build-Tools 35).',
        'h_backup': 'Backup e continuazione dello sviluppo (importante)',
        'backup_intro': 'Nella cartella `ClockSeconds/` conserva **tutto tranne `build/` e `.gradle/`** (il codice è ~300 KB):',
        'th_backup_cat': 'Categoria',
        'th_backup_files': 'File',
        'th_backup_note': 'Nota',
        'backup_rows': [
            ('File di build', '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, '
             '`gradle.properties`, `keystore.properties`', 'Versioni, dipendenze, parametri, firma'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Fissa Gradle 8.14.5'),
            ('Codice', '`app/src/main/java/**` (20 `.java`)', 'Logica del modulo, impostazioni, classi di disegno'),
            ('Risorse', '`app/src/main/res/**`', 'Testi (22 lingue), temi'),
            ('Metadati Xposed', '`app/src/main/resources/META-INF/xposed/*`', 'Classe di ingresso, ambito, livello API'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permessi, activity'),
            ('Strumenti', '`tools/**`', 'Generatore README, controllo i18n, script di release'),
            ('Documenti', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Descrizione, screenshot, storico versioni'),
            ('CI', '`.github/workflows/*.yml`', 'Build firmata al tag'),
        ],
        'h_trouble': 'Problemi',
        'trouble_summary': 'Mostra i passi',
        'trouble_intro': 'Guarda logcat:',
        'trouble': [
            ('b', 'Le **build Release** nascondono tutti i log diagnostici dietro `BuildConfig.DEBUG`.'),
            ('b', 'Per il debug installa una **build debug** (`assembleDebug`, stessa chiave) e filtra:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'Nessun secondo: controlla l\'ambito «Interfaccia di sistema» in LSPosed e riavvia; poi leggi o '
                  'scrivi l\'interruttore:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Colori o secondi tornano da soli: il sistema o il tema ricostruisce il testo e il modulo '
                  'ridisegna; se non torna, cerca `color-guard stopped` / `span-theft` nel log debug.'),
        ],
        'h_feedback': 'Feedback',
        'feedback': 'Domande e proposte su [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).',
        'h_license': 'License',
    },
}
