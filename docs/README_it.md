<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · **Italiano** · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Secondi sull'ora（Show Seconds in Time）

Mostra i **secondi** sull'ora della barra di stato, attiva/disattiva con un solo tocco e tieni la **decina dell'ora sempre rossa**.<br>Nessuna classe privata del produttore. Verificato su dispositivo reale con ColorOS 15; copre anche Xiaomi, OnePlus, Honor, vivo e gli altri grandi produttori.

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Funzioni

| Funzione | Descrizione |
|------|------|
| Secondi | L'ora della barra di stato mostra i secondi (`HH:mm:ss` / `h:mm:ss a`, segue l'impostazione 12/24 ore del sistema) |
| Tocca per attivare | **Toccare** l'ora della barra di stato attiva/disattiva i secondi; nella pagina delle impostazioni è disponibile anche il **doppio tocco** |
| Decina dell'ora in rosso | La **decina dell'ora** viene colorata di rosso (l'`1` in `13`, lo `0` in `05`); minuti e secondi mantengono il colore di sistema. Le ore a una sola cifra (ad es. `9:45`) non hanno decina e restano intatte. Efficace su tutti i produttori |
| Lingue dell'interfaccia | L'interfaccia dell'app è tradotta in **21 lingue** (segue automaticamente la lingua di sistema): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistenza | Lo stato è salvato in `Settings.Secure`, condiviso tra processi e conservato dopo il riavvio di SystemUI |
| Compatibilità | Verificato su ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); funziona anche così com'è su altri produttori con root come Xiaomi / Honor / Redmi. Nessuna classe privata del produttore, quindi la modalità provvisoria di LSPosed non scatta. Minimo Android 10 |

## Installazione

1. Scarica l'APK più recente dalla pagina [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Attiva il modulo in **LSPosed**, ambito: **Interfaccia di sistema (`com.android.systemui`)**
3. Riavvia il processo SystemUI o riavvia il dispositivo
4. Apri la pagina delle impostazioni dal **gestore LSPosed** (non viene creata alcuna icona nel launcher)

> Richiede root (LSPosed). Per concedere `WRITE_SECURE_SETTINGS` e poter scrivere in `Settings.Secure`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Senza quel permesso puoi comunque attivare/disattivare toccando l'ora della barra di stato (quell'azione gira dentro il processo SystemUI, che ha già il diritto per natura).

## Utilizzo

- **Tocca l'ora della barra di stato**: attiva/disattiva i secondi (gesto predefinito).
- L'attivazione è implementata con un test di tocco globale, quindi **non entra mai in conflitto con il «trascinamento del pannello notifiche»**—— un tocco puro attiva/disattiva, uno striscia continua a tirare giù il pannello. Se preferisci, puoi passare al gesto **doppio tocco** nella pagina delle impostazioni.
- La pagina delle impostazioni mostra lo stato attuale, il gesto scelto e un pulsante di aggiornamento.

## Anteprima

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Secondi disattivati: `15:06` — la cifra delle decine dell'ora `1` resta rossa" width="100%">
<br>
Secondi disattivati: `15:06` — la cifra delle decine dell'ora `1` resta rossa
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Secondi attivati: `15:06:03` — minuti e secondi mantengono il colore di sistema" width="100%">
<br>
Secondi attivati: `15:06:03` — minuti e secondi mantengono il colore di sistema
</td>
</tr></table>

## Come funziona

Il modulo aggancia solo **SystemUI**, usando l'approccio legacy Xposed collaudato,
e **non tocca mai nessuna classe privata del produttore**, quindi non rompe lo stato interno del sistema né fa scattare la modalità provvisoria di LSPosed:

- **Secondi**: aggancia lo stabile `TextView.setText` (after) per mettere in cache il testo grezzo che il sistema mette sull'ora della barra di stato,
  poi aggancia `TextView.onDraw` (before) per sovrascrivere la visualizzazione con `testoGrezzo:ss` dopo che il sistema ha impostato il testo,
  pilotato da un `Handler` da 1 secondo. I secondi aggiunti ereditano il colore del testo proprio dell'ora (misurato `#ccffffff`).
  La decina dell'ora viene inoltre colorata di rosso (un `ForegroundColorSpan` rosso su quell'unico carattere), sia che i secondi siano mostrati sia che non lo siano;
  minuti e secondi non sono mai rossi; le ore a una sola cifra non hanno decina e restano intatte.
- **Attivazione**: il pannello notifiche di ColorOS intercetta i tocchi nell'area della barra di stato, quindi l'`OnTouchListener` proprio dell'ora non riceve mai eventi.
  Il modulo aggancia perciò il `View.dispatchTouchEvent` globale e capovolge l'interruttore `Settings.Secure` solo quando la pressione cade dentro
  i limiti su schermo dell'ora riconosciuto e non si muove quasi (tocco) oppure è un secondo tocco entro 350 ms (doppio tocco). Il modulo **non consuma**
  nessun evento di tocco, quindi il trascinamento del pannello notifiche funziona come al solito.

## Nota sul risparmio energetico

- **Secondi ATTIVI**: il modulo ha bisogno di un timer da 1 secondo per pilotare gli aggiornamenti (il costo insito nel mostrare i secondi, circa 60 ridisegni dell'ora al minuto).
- **Secondi DISATTIVI**: il modulo **ferma subito quel timer** (`stopTicker`)—— nessun risveglio ogni secondo e nessuna lettura tra processi.
  Torna al ritmo originale del sistema «aggiorna una volta al minuto», quindi disattivare i secondi fa davvero risparmiare energia.
- Lo stato è tenuto in **cache dentro il processo** (`sSecondsEnabled`), così il timer non legge `Settings.Secure` ogni secondo (evitando un IPC al secondo).
- Non viene costruito nessun formattatore di orario: il modulo riutilizza il testo grezzo del sistema e si limita ad aggiungere `:ss`, quindi non c'è overhead di formattazione.
- Il timer tiene solo un **riferimento debole** alla vista e continua solo mentre la vista è `isAttachedToWindow()`; quando la vista viene distrutta, esce automaticamente.

## Build

Richiede **JDK 17+** (17 o 21 consigliati) e **Android SDK Platform 35** (Build-Tools 35 viene installato automaticamente da AGP).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Backup e sviluppo continuativo (importante)

Nella cartella `ClockSeconds/`, **vanno conservati tutti i file tranne `build/` e `.gradle/`** (la parte di codice pesa solo circa 140 KB):

| Categoria | File | Nota |
|------|------|------|
| Script di build | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Versione, dipendenze, parametri del compilatore |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fissa Gradle 8.14.5 |
| Codice sorgente | `app/src/main/java/**` (3 file `.java`) | Il corpo della logica del modulo |
| Risorse | `app/src/main/res/**` | Stringhe (cinese predefinito + 21 lingue values-xx), tema |
| Metadati Xposed | `app/src/main/resources/META-INF/xposed/*` | Classe d'ingresso, ambito, versione API |
| Manifest | `app/src/main/AndroidManifest.xml` | Permessi, Activity |
| Documentazione | `README*.md` | Documentazione in 21 lingue |
| CI | `.github/workflows/release.yml` | Un tag produce una build firmata |

## Risoluzione dei problemi

<details>
<summary>Espandi i passaggi di risoluzione dei problemi</summary>

Controlla il logcat:

- Le **build Release** eliminano tutti i log diagnostici tramite la porta `BuildConfig.DEBUG` (più leggera e pulita); in logcat non esce nulla da questo modulo.
- Per indagare, installa temporaneamente la **build debug** (`assembleDebug`, firmata con la stessa chiave di produzione, quindi installabile sopra la Release) e filtra per TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Se i secondi non appaiono del tutto: per prima cosa conferma che LSPosed ha la spunta su SystemUI e ha riavviato; poi verifica se l'interruttore è stato salvato come disattivato——
  da leggere / forzare via adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=attivo 0=disattivo
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Se non ha effetto su un dispositivo non ColorOS: installa la build debug e guarda le righe `candidate:` / `CONFIG CLOCK:` nel log
  per vedere se il nome della risorsa / il nome della classe dell'ora della barra di stato del tuo dispositivo è coperto dalle regole di riconoscimento; se non lo è, basta una riga di regola in più.

</details>

## Feedback

Hai domande, suggerimenti o commenti? Lascia un messaggio su [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues), risponderò al più presto.

## License

[MIT](LICENSE) © kelebao
