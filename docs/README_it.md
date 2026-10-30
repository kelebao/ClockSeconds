<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · **Italiano** · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Secondi nella barra di stato (Show Seconds in Time)

Mostra i **secondi** nell'orologio della barra di stato, si attiva con un tocco. Puoi dare **un colore a ogni cifra** o applicare un **gradiente** all'intera stringa.<br>**Pressione lunga**: finestra traslucida con l'**ora di rete** (trascinamento, dimensione, gradiente); le **attività personali** accendono i secondi all'ora scelta.<br>Il percorso principale usa solo API pubbliche AOSP (le classi private OPPO / OnePlus / realme sono opzionali e saltate se assenti). Testato su ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Funzioni

| Funzione | Dettaglio |
|------|------|
| Secondi nella barra | `HH:mm:ss` / `h:mm:ss a`, rispetta il formato 12/24 h e il fuso orario. In coda viene aggiunto uno **spazio di larghezza zero** così i temi non tolgono i colori; se i colori cadono, il testo viene ridisegnato automaticamente (pausa di 5 minuti). |
| Attivazione al tocco | **Toccare** l'ora attiva o disattiva i secondi (si può scegliere il **doppio tocco**); la **pressione lunga** è riservata alla finestra di anteprima. Dentro un'attività viene mostrato che è lei a comandare. |
| Colore per cifra / gradiente | Sei cifre **ognuna del proprio colore** (10 preset + slider HSV + colore di sistema) e i due punti centrati otticamente. Oppure un **gradiente** dal colore iniziale a quello finale (6 set, estremi modificabili, scurito su fondo chiaro). L'anteprima **si disegna da sola**, quindi coincide con la barra pixel per pixel. |
| Finestra su pressione lunga | Finestra traslucida con l'**ora di rete**; trascina per spostare, angolo ◣ per la dimensione, posizione e dimensione **vengono ricordate**; gradiente e cifre a larghezza fissa. |
| Attività personali | Finestre **ogni ora** o **ogni giorno**: accendere i secondi N minuti prima per M minuti e poi ripristinare. Il cambio di ora o fuso viene ricalcolato subito, più un controllo ogni 60 secondi. |
| Confronto con l'ora di rete | Mostra lo scostamento dall'**ora di rete**; un tocco su **Sincronizza** attiva l'ora automatica di sistema. |
| Nessun adb necessario | La scrittura passa da `Settings.Secure`; senza `WRITE_SECURE_SETTINGS` un broadcast fa scrivere la SystemUI — **nessuna funzione richiede adb**. |
| Riquadro rapido | Aggiungi il riquadro «Mostra secondi» per cambiare senza aprire l'app. |
| Nome risorsa dell'orologio | Se l'orologio non viene riconosciuto, tocca **Rilevamento automatico** o inserisci il nome della risorsa (es. `clock`). |
| 21 lingue dell'interfaccia | Segue la lingua di sistema: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული; le altre usano l'inglese. |
| Archiviazione / ambito | Lo stato è in `Settings.Secure`, condiviso tra processi e persistente dopo il riavvio. **Testato su ColorOS 15 / 16 (Android 15 / 16)**; il nucleo usa solo API pubbliche, quindi altri marchi dovrebbero funzionare, ma sono stati provati solo OPPO / OnePlus. Minimo Android 10. |

## Modalità colore

Due modi di colorare, si cambiano nella scheda **Colori delle cifre**: **per cifra** o **gradiente** sull'intera stringa.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="Per cifra: ogni cifra ha il suo colore, due punti centrati otticamente" width="100%">
<br>
Per cifra: ogni cifra ha il suo colore, due punti centrati otticamente
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Gradiente: dal colore iniziale a quello finale, 6 set o estremi personalizzati" width="100%">
<br>
Gradiente: dal colore iniziale a quello finale, 6 set o estremi personalizzati
</td>
</tr></table>

## Installazione

1. Scarica l'APK da [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Attiva il modulo in **LSPosed** e seleziona l'ambito **Interfaccia di sistema** (`com.android.systemui`)
3. Riavvia il processo SystemUI o il dispositivo
4. La schermata impostazioni si apre dal **gestore LSPosed** (nessuna icona nel launcher)

> Serve root (LSPosed). **Di norma non servono permessi adb**: la scrittura usa `Settings.Secure` e, senza `WRITE_SECURE_SETTINGS`, un broadcast fa scrivere la SystemUI. Per far scrivere **direttamente** la schermata impostazioni:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Senza concedere nulla funziona tutto: tocchi, attività e finestra girano dentro la SystemUI.

## Uso

- **Tocca l'ora**: secondi on/off (predefinito); nelle impostazioni si può usare il **doppio tocco**.
- **Pressione lunga**: finestra traslucida con l'**ora di rete**, trascina per spostare; toccando l'ora compaiono **chiudi** (in alto a destra) e **dimensione** (in basso a sinistra).
- La scheda **Colori delle cifre** ha due pagine e la linguetta stessa è l'interruttore.
- La scheda **Attività personali** pianifica finestre come «secondi 2 minuti prima di ogni ora».
- Il cambio usa un controllo globale del tocco e **non interferisce con il tendaggio** delle notifiche.

## Anteprima

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Secondi off: `15:06`, la decina d'ora mantiene il colore" width="100%">
<br>
Secondi off: `15:06`, la decina d'ora mantiene il colore
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Secondi on: `15:06:03`, minuti e secondi nel colore di sistema" width="100%">
<br>
Secondi on: `15:06:03`, minuti e secondi nel colore di sistema
</td>
</tr></table>

## Come funziona

Il modulo aggancia solo la **SystemUI** e usa l'API legacy Xposed, stabile.
**Il nucleo usa solo API pubbliche AOSP; le classi del produttore sono opzionali e saltate se assenti**, quindi non rompe il sistema e non attiva la modalità provvisoria di LSPosed:

- **Secondi**: `TextView.setText` (after) memorizza il testo di sistema, poi `TextView.onDraw` (before) scrive `originale:ss`, aggiornato ogni secondo con `Handler`.
- **Colore per cifra**: un `ForegroundColorSpan` per carattere (gli span estranei vengono rimossi); i due punti salgono di 0.11em perché i layout a più segmenti mettono il glifo più in basso.
- **Gradiente**: `CharacterStyle` chiama `setShader` in `updateDrawState`; l'anteprima **si disegna da sola** (`GradientTimeView`) perché ColorOS 15 quantizza lo shader per segmento di testo.
- **Cambio stato**: il pannello intercetta i tocchi sulla barra, quindi il modulo aggancia `View.dispatchTouchEvent` globale e cambia solo se il tocco cade nell'orologio senza trascinamento; l'evento **non viene consumato**.
- **Attività**: verifica «l'ora locale è nella finestra», timer esatti ai bordi e controllo ogni 60 secondi.

## Consumo

- **Secondi on**: timer ogni secondo (costo inevitabile). Misura su SystemUI per core: 7.0% off, 7.25% on (60 s).
- **Secondi off**: il timer **si ferma subito** (`stopTicker`), nessun risveglio al secondo.
- La **protezione colore** rallenta 2s → 4s → … → 30s e termina dopo 5 minuti senza perdite.
- Lo stato è **in cache nel processo**; le attività usano `Handler.postDelayed`: **senza WakeLock né AlarmManager**, non svegliano il sonno profondo.

## Compilazione

Servono **JDK 17+** (17 o 21) e **Android SDK Platform 35** (AGP scarica Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Backup e continuazione dello sviluppo (importante)

Nella cartella `ClockSeconds/` conserva **tutto tranne `build/` e `.gradle/`** (il codice è ~300 KB):

| Categoria | File | Nota |
|------|------|------|
| File di build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Versioni, dipendenze, parametri, firma |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fissa Gradle 8.14.5 |
| Codice | `app/src/main/java/**` (20 `.java`) | Logica del modulo, impostazioni, classi di disegno |
| Risorse | `app/src/main/res/**` | Testi (22 lingue), temi |
| Metadati Xposed | `app/src/main/resources/META-INF/xposed/*` | Classe di ingresso, ambito, livello API |
| Manifest | `app/src/main/AndroidManifest.xml` | Permessi, activity |
| Strumenti | `tools/**` | Generatore README, controllo i18n, script di release |
| Documenti | `README*.md`, `docs/**`, `CHANGELOG.md` | Descrizione, screenshot, storico versioni |
| CI | `.github/workflows/*.yml` | Build firmata al tag |

## Problemi

<details>
<summary>Mostra i passi</summary>

Guarda logcat:

- Le **build Release** nascondono tutti i log diagnostici dietro `BuildConfig.DEBUG`.
- Per il debug installa una **build debug** (`assembleDebug`, stessa chiave) e filtra:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Nessun secondo: controlla l'ambito «Interfaccia di sistema» in LSPosed e riavvia; poi leggi o scrivi l'interruttore:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Colori o secondi tornano da soli: il sistema o il tema ricostruisce il testo e il modulo ridisegna; se non torna, cerca `color-guard stopped` / `span-theft` nel log debug.

</details>

## Feedback

Domande e proposte su [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
