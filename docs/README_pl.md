<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · **Polski** · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Sekundy na pasku stanu (Show Seconds in Time)

Pokazuje **sekundy** w zegarze paska stanu, przełączanie dotknięciem. Możesz dać **kolor każdej cyfrze** albo nałożyć **gradient** na cały tekst.<br>**Długie naciśnięcie** otwiera półprzezroczyste okno z **czasem sieciowym** (przesuwanie, rozmiar, gradient); **własne zadania** włączają sekundy o wybranej godzinie.<br>Główna ścieżka używa tylko publicznych API AOSP (zamknięte klasy OPPO / OnePlus / realme są opcjonalne i pomijane, gdy ich nie ma). Sprawdzone na ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Funkcje

| Funkcja | Opis |
|------|------|
| Sekundy na pasku | `HH:mm:ss` / `h:mm:ss a`, uwzględnia format 12/24 h i strefę czasową. Na końcu dodawana jest **spacja o zerowej szerokości**, aby motywy nie usuwały kolorów; po utracie kolor jest przerysowywany (pauza 5 minut). |
| Przełączanie dotknięciem | **Dotknięcie** włącza lub wyłącza sekundy (można wybrać **podwójne dotknięcie**); **długie naciśnięcie** należy do okna podglądu. W oknie zadania pojawia się informacja „zarządza zadanie". |
| Kolor cyfr / gradient | Sześć cyfr **każda w innym kolorze** (10 gotowych + suwaki HSV + jak w systemie), dwukropki optycznie wyśrodkowane. Albo **gradient** od koloru początkowego do końcowego (6 zestawów, oba końce edytowalne, na jasnym tle przyciemniany). Karta podglądu **rysuje się sama**, więc zgadza się z paskiem co do piksela. |
| Okno po długim naciśnięciu | Półprzezroczyste okno z **czasem sieciowym**; przesuwanie, rozmiar za róg ◣, pozycja i rozmiar **są pamiętane**; gradient i cyfry o równej szerokości. |
| Własne zadania | Okna **co godzinę** lub **codziennie**: włącz sekundy N minut wcześniej na M minut, potem przywróć. Zmiana godziny lub strefy przelicza od razu, plus kontrola co 60 sekund. |
| Porównanie z czasem sieciowym | Pokazuje odchylenie od **czasu sieciowego**; przycisk **Synchronizuj** włącza automatyczne ustawianie czasu w systemie. |
| Bez adb | Zapis idzie przez `Settings.Secure`; bez `WRITE_SECURE_SETTINGS` używany jest broadcast i pisze SystemUI — **żadna funkcja nie wymaga adb**. |
| Kafelek w panelu | Dodaj kafelek „Pokazuj sekundy", aby przełączać bez otwierania aplikacji. |
| Nazwa zasobu zegara | Jeśli zegar nie zostanie rozpoznany, naciśnij **Wykryj automatycznie** lub wpisz nazwę zasobu (np. `clock`). |
| 21 języków interfejsu | Zgodnie z językiem systemu: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული; pozostałe po angielsku. |
| Przechowywanie / zakres | Stan jest w `Settings.Secure`, wspólny dla procesów i trwały po restarcie. **Sprawdzone na ColorOS 15 / 16 (Android 15 / 16)**; rdzeń używa tylko publicznych API, więc inne marki powinny działać, ale testowano tylko OPPO / OnePlus. Minimum Android 10. |

## Tryby koloru

Dwa sposoby kolorowania, przełączane w karcie **Kolory cyfr**: **według cyfr** albo **gradient** na cały tekst.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="Według cyfr: każda cyfra własny kolor, dwukropki optycznie wyśrodkowane" width="100%">
<br>
Według cyfr: każda cyfra własny kolor, dwukropki optycznie wyśrodkowane
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Gradient: od koloru początkowego do końcowego, 6 zestawów lub własne końce" width="100%">
<br>
Gradient: od koloru początkowego do końcowego, 6 zestawów lub własne końce
</td>
</tr></table>

## Instalacja

1. Pobierz APK ze strony [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Włącz moduł w **LSPosed** i zaznacz zakres **Interfejs systemu** (`com.android.systemui`)
3. Uruchom ponownie proces SystemUI lub urządzenie
4. Ekran ustawień otwiera się z **menedżera LSPosed** (w launcherze nie ma ikony)

> Wymagany root (LSPosed). **Uprawnienia adb zwykle nie są potrzebne**: zapis przez `Settings.Secure`, a bez `WRITE_SECURE_SETTINGS` przez broadcast, który wykonuje SystemUI. Aby ekran ustawień pisał **bezpośrednio**:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Bez nadania nic się nie psuje: dotknięcia, zadania i okno działają w SystemUI.

## Użycie

- **Dotknij czasu**: sekundy wł/wył (domyślnie); w ustawieniach można ustawić **podwójne dotknięcie**.
- **Długie naciśnięcie**: okno z **czasem sieciowym**, przesuwanie; dotknięcie czasu pokazuje **zamknij** (prawy górny) i **rozmiar** (lewy dolny).
- Karta **Kolory cyfr** ma dwie strony, a sam zakładek jest przełącznikiem.
- Karta **Własne zadania** planuje okna typu „sekundy 2 minuty przed każdą godziną".
- Przełączanie używa globalnego testu dotknięcia i **nie koliduje z wysuwaniem panelu**.

## Podgląd

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Sekundy wył: `15:06`, dziesiątki godziny zachowują kolor" width="100%">
<br>
Sekundy wył: `15:06`, dziesiątki godziny zachowują kolor
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Sekundy wł: `15:06:03`, minuty i sekundy w kolorze systemowym" width="100%">
<br>
Sekundy wł: `15:06:03`, minuty i sekundy w kolorze systemowym
</td>
</tr></table>

## Jak to działa

Moduł hookuje wyłącznie **SystemUI** i używa stabilnego legacy API Xposed.
**Rdzeń opiera się tylko na publicznych API AOSP, zamknięte klasy producentów są opcjonalne i pomijane, gdy ich nie ma**, więc nie psuje systemu i nie wywołuje trybu awaryjnego LSPosed:

- **Sekundy**: `TextView.setText` (after) zapisuje tekst systemowy, a `TextView.onDraw` (before) pisze `oryginał:ss`, odświeżanie co sekundę przez `Handler`.
- **Kolor cyfr**: `ForegroundColorSpan` na znak (obce spany są usuwane); dwukropki są podnoszone o 0.11em, bo układy z wieloma fragmentami stawiają glif niżej.
- **Gradient**: `CharacterStyle` wywołuje `setShader` w `updateDrawState`; podgląd **rysuje się sam** (`GradientTimeView`), bo ColorOS 15 kwantyzuje shader na fragmenty tekstu.
- **Przełączanie**: panel przechwytuje dotknięcia paska, więc moduł hookuje globalny `View.dispatchTouchEvent` i przełącza tylko gdy dotknięcie mieści się w zegarze bez przeciągania; zdarzenie **nie jest konsumowane**.
- **Zadania**: sprawdzanie „czy czas lokalny mieści się w oknie", dokładne timery na granicach oraz kontrola co 60 sekund.

## Zużycie energii

- **Sekundy wł**: timer co sekundę (nieuniknione). Pomiar SystemUI na rdzeń: 7.0% wył, 7.25% wł (60 s).
- **Sekundy wył**: timer **zatrzymuje się** (`stopTicker`), brak wybudzeń co sekundę.
- **Ochrona koloru** zwalnia 2s → 4s → … → 30s i kończy się po 5 minutach bez utraty koloru.
- Stan jest **buforowany w procesie**; zadania używają `Handler.postDelayed`: **bez WakeLock i AlarmManager**, głęboki sen nie jest budzony.

## Kompilacja

Potrzebne są **JDK 17+** (17 lub 21) oraz **Android SDK Platform 35** (AGP pobierze Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Kopia zapasowa i dalszy rozwój (ważne)

W katalogu `ClockSeconds/` zachowaj **wszystko oprócz `build/` i `.gradle/`** (kod to ~300 KB):

| Kategoria | Pliki | Uwaga |
|------|------|------|
| Pliki budowania | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Wersje, zależności, parametry, podpis |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Przypina Gradle 8.14.5 |
| Kod | `app/src/main/java/**` (20 `.java`) | Logika modułu, ustawienia, klasy rysujące |
| Zasoby | `app/src/main/res/**` | Teksty (22 języki), motywy |
| Metadane Xposed | `app/src/main/resources/META-INF/xposed/*` | Klasa wejściowa, zakres, poziom API |
| Manifest | `app/src/main/AndroidManifest.xml` | Uprawnienia, aktywności |
| Narzędzia | `tools/**` | Generator README, kontrola i18n, skrypt wydania |
| Dokumenty | `README*.md`, `docs/**`, `CHANGELOG.md` | Opis, zrzuty ekranu, dziennik zmian |
| CI | `.github/workflows/*.yml` | Podpisana kompilacja przy tagu |

## Problemy

<details>
<summary>Pokaż kroki</summary>

Sprawdź logcat:

- **Kompilacje Release** ukrywają wszystkie logi diagnostyczne za `BuildConfig.DEBUG`.
- Do debugowania zainstaluj **kompilację debug** (`assembleDebug`, ten sam klucz) i filtruj:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Brak sekund: sprawdź zakres „Interfejs systemu" w LSPosed i uruchom ponownie; potem odczytaj lub zapisz przełącznik:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Kolory lub sekundy wracają same: system albo motyw przebudowuje tekst i moduł rysuje ponownie; jeśli nie wraca, szukaj w logu debug `color-guard stopped` / `span-theft`.

</details>

## Opinie

Pytania i propozycje: [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
