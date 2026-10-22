<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · **Deutsch** · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Sekundenanzeige（Show Seconds in Time）

Zeigt **Sekunden** in der Statusleisten-Zeit an, Umschalten mit einmaligem Tippen, und die **Zehnerstelle der Stunde bleibt rot**.<br>Keine herstellereigenen privaten Klassen nötig. Auf einem echten Gerät mit ColorOS 15 verifiziert; deckt ebenfalls Xiaomi, OnePlus, Honor, vivo, Samsung und andere große Hersteller ab.

[![version](https://img.shields.io/badge/version-1.0.7-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Funktionen

| Funktion | Beschreibung |
|------|------|
| Sekundenanzeige | Die Statusleisten-Zeit zeigt Sekunden an (`HH:mm:ss` / `h:mm:ss a`, folgt der 12/24-Stunden-Einstellung des Systems) |
| Tippen zum Umschalten | **Tippen** auf die Statusleisten-Zeit schaltet die Sekundenanzeige ein/aus; im Einstellungsbereich auch **doppeltes Tippen** möglich |
| Farbe der Stundenziffer | Die Farbe der **Zehnerstelle der Stunde** ist **anpassbar** (8-Farben-Palette) oder **abschaltbar** für die Systemfarbe; einstellige Stunden (z. B. `9:45`) haben keine Zehnerstelle und bleiben unberührt. Wirkt bei allen Herstellern |
| Vorschau bei langem Drücken | **Langes Drücken** auf die Statusleisten-Zeit öffnet ein **milchglasartiges schwebendes Fenster** mit **Netzzeit** in Echtzeit; per Drag verschiebbar, über ◣ unten links skalierbar, Tippen auf die Zeit zeigt **Schließen** (✕ oben rechts) / **Skalieren**; Schriftgröße passt sich dem Fenster an |
| Netzzeit | Die Einstellungsseite vergleicht die Uhr in Echtzeit mit der **Netz-Standardzeit**; bei großer Abweichung **Synchronisieren** tippen, um die automatische Zeiteinstellung zu aktivieren |
| Kein adb nötig | Einstellungen werden in `Settings.Secure` geschrieben; ohne `WRITE_SECURE_SETTINGS` weicht das Modul automatisch auf einen Broadcast aus, den SystemUI verarbeitet — **keine adb-Freigabe nötig** |
| Oberflächensprachen | Die App-Oberfläche ist in **21 Sprachen** übersetzt (folgt automatisch der Systemsprache): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistenz | Der Zustand liegt in `Settings.Secure`, ist prozessübergreifend gemeinsam genutzt und überlebt SystemUI-Neustarts |
| Kompatibilität | Auf ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme) auf echtem Gerät verifiziert; funktioniert auch auf anderen gerooteten Herstellern wie Xiaomi / Honor / Redmi / Samsung. Keine herstellereigenen privaten Klassen, daher wird der LSPosed-Sicherheitsmodus nicht ausgelöst. Mindestens Android 10 |
| Schnellkachel | Füge eine "Sekunden anzeigen"-Kachel zum Benachrichtigungsbereich hinzu, mit einem Tipp umschalten |
| Float verbessert | Position & Größe des Floats werden **gemerkt**; zeigt das **Datum**; **Doppeltipp** auf die Zeit schaltet Sekunden um |
| Energiesparerkennung | Bei Aktivierung des Energiesparmodus pausiert die Sekundenaktualisierung (Sekunden einfrieren), danach sofort weiter |
| Erkennung konfigurierbar | Wird die Uhr auf anderen Geräten nicht erkannt, gib den Ressourcennamen (z. B. clock) in den Einstellungen ein. Leer = eingebaute Regeln |

## Installation

1. Laden Sie die neueste APK von der Seite [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) herunter
2. Aktivieren Sie das Modul in **LSPosed**, Wirkungsbereich: **Systemoberfläche (`com.android.systemui`)**
3. Starten Sie den SystemUI-Prozess neu oder starten Sie das Gerät neu
4. Öffnen Sie die Einstellungsseite aus dem **LSPosed-Manager** heraus (auf dem Startbildschirm wird kein Symbol erzeugt)

> Root (LSPosed) erforderlich. Um `WRITE_SECURE_SETTINGS` zum Schreiben in `Settings.Secure` zu erteilen:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Ohne die Berechtigung können Sie weiterhin durch Tippen auf die Statusleisten-Zeit umschalten (diese Aktion läuft im SystemUI-Prozess und hat das Recht von Haus aus).

## Verwendung

- **Auf die Statusleisten-Zeit tippen**: schaltet die Sekundenanzeige ein/aus (Standardgeste).
- Das Umschalten wird über eine globale Touch-Trefferprüfung realisiert und **konfligiert niemals mit dem „Herunterziehen des Benachrichtigungsfelds“**—— reines Tippen schaltet um, Wischen zieht das Benachrichtigungsfeld weiterhin herunter. Wer es lieber mag, kann die Geste im Einstellungsbereich auf **doppeltes Tippen** umstellen.
- Die Einstellungsseite zeigt den aktuellen Zustand, die gewählte Geste und eine Aktualisieren-Schaltfläche.

## Vorschau

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Sekunden aus: `15:06` — die Zehnerstelle der Stunde `1` bleibt rot" width="100%">
<br>
Sekunden aus: `15:06` — die Zehnerstelle der Stunde `1` bleibt rot
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Sekunden an: `15:06:03` — Minuten und Sekunden behalten die Systemfarbe" width="100%">
<br>
Sekunden an: `15:06:03` — Minuten und Sekunden behalten die Systemfarbe
</td>
</tr></table>

## Funktionsweise

Das Modul hookt nur **SystemUI** und nutzt den bewährten Legacy-Xposed-Ansatz,
**berührt keinerlei herstellereigene private Klassen** und beschädigt daher nie den internen Systemzustand oder löst den LSPosed-Sicherheitsmodus aus:

- **Sekunden**: Hook des stabilen `TextView.setText` (after) cached den Rohtext, den das System auf die Statusleisten-Zeit schreibt,
  dann Hook von `TextView.onDraw` (before), um die Anzeige mit `Rohtext:ss` zu überschreiben, nachdem das System den Text gesetzt hat,
  angetrieben von einem 1-Sekunden-`Handler`. Die angehängten Sekunden übernehmen die eigene Textfarbe der Zeit (gemessen `#ccffffff`).
  Zusätzlich wird die Zehnerstelle der Stunde rot gefärbt (ein roter `ForegroundColorSpan` auf diesem einen Zeichen), unabhängig davon, ob Sekunden angezeigt werden;
  Minuten und Sekunden werden nie rot; einstellige Stunden haben keine Zehnerstelle und bleiben unberührt.
- **Umschalten**: Das Benachrichtigungsfeld von ColorOS fängt Touches im Bereich der Statusleiste ab, daher erhält der eigene `OnTouchListener` der Zeit keine Ereignisse.
  Das Modul hookt deshalb das globale `View.dispatchTouchEvent` und dreht den `Settings.Secure`-Schalter nur, wenn der Druckpunkt
  innerhalb der Bildschirmgrenzen der erkannten Zeit liegt und sich fast nicht bewegt (Tippen) oder innerhalb 350 ms ein zweites Tippen erfolgt (doppeltes Tippen). Das Modul **verbraucht**
  kein Touch-Ereignis, das Herunterziehen des Benachrichtigungsfelds funktioniert wie gewohnt.

## Hinweise zum Stromverbrauch

- **Sekundenanzeige AN**: Das Modul braucht einen 1-Sekunden-Timer für die Aktualisierungen (die inhärente Kosten des Sekundenanzeigens, rund 60 Neuzeichnungen der Zeit pro Minute).
- **Sekundenanzeige AUS**: Das Modul **stoppt diesen Timer sofort** (`stopTicker`)—— überhaupt keine Aufwachvorgänge pro Sekunde mehr und keine prozessübergreifenden Lesevorgänge.
  Es kehrt zum ursprünglichen Rhythmus des Systems „einmal pro Minute aktualisieren“ zurück, daher ist das Ausschalten der Sekundenanzeige wirklich sparsamer.
- Der Zustand wird **prozessintern gecacht** (`sSecondsEnabled`), sodass der Timer nicht in jeder Sekunde `Settings.Secure` liest (ein IPC pro Sekunde wird vermieden).
- Es wird kein Zeitformatierer konstruiert: Das Modul verwendet den Rohtext des Systems weiter und hängt nur `:ss` an, daher gibt es keinen Formatierer-Overhead.
- Der Timer hält nur eine **schwache Referenz** auf die View und läuft nur weiter, solange die View `isAttachedToWindow()` ist; nach dem Zerstören der View endet er automatisch.

## Build

Erfordert **JDK 17+** (17 oder 21 empfohlen) und **Android SDK Platform 35** (Build-Tools 35 wird von AGP automatisch installiert).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Backup & Weiterentwicklung (wichtig)

Im Verzeichnis `ClockSeconds/` müssen **alle Dateien außer `build/` und `.gradle/`** aufgehoben werden (der Quelltextteil ist nur etwa 140 KB groß):

| Kategorie | Datei | Hinweis |
|------|------|------|
| Build-Skripte | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Version, Abhängigkeiten, Compiler-Parameter |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Fixiert Gradle 8.14.5 |
| Quellcode | `app/src/main/java/**` (3 `.java`-Dateien) | Der Kern der Modullogik |
| Ressourcen | `app/src/main/res/**` | Strings (Standard Chinesisch + 21 values-xx-Sprachen), Theme |
| Xposed-Metadaten | `app/src/main/resources/META-INF/xposed/*` | Einstiegsklasse, Wirkungsbereich, API-Version |
| Manifest | `app/src/main/AndroidManifest.xml` | Berechtigungen, Activity |
| Dokumentation | `README*.md` | Dokumentation in 21 Sprachen |
| CI | `.github/workflows/release.yml` | Tag erzeugt signierten Build |

## Fehlerbehebung

<details>
<summary>Schritte zur Fehlerbehebung aufklappen</summary>

Logcat prüfen:

- **Release-Builds** entfernen über das `BuildConfig.DEBUG`-Gate alle Diagnoselogs (kleiner, aufgeräumter); in Logcat erscheint von diesem Modul nichts.
- Zur Untersuchung installieren Sie vorübergehend den **Debug-Build** (`assembleDebug`, mit demselben Produktionsschlüssel signiert, kann also direkt über die Release-Installation installiert werden) und filtern Sie nach TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Falls überhaupt keine Sekunden erscheinen: Prüfen Sie zuerst, ob in LSPosed der SystemUI-Wirkungsbereich angehakt und neu gestartet wurde; prüfen Sie dann, ob der Schalter als aus persistiert wurde——
  auslesen / zwangsweise schreiben per adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=an 0=aus
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Falls es auf einem Nicht-ColorOS-Gerät nicht wirkt: Installieren Sie den Debug-Build und sehen Sie sich die Zeilen `candidate:` / `CONFIG CLOCK:` im Log an,
  um zu prüfen, ob Ressourcenname / Klassenname der Statusleisten-Zeit Ihres Geräts von den Erkennungsregeln abgedeckt sind; wenn nicht, genügt eine zusätzliche Regelzeile.

</details>

## Feedback

Fragen, Ideen oder Rückmeldungen? Hinterlasse einfach eine Nachricht auf [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) — ich melde mich bald.

## License

[MIT](LICENSE) © kelebao
