<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · **Українська** · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Секунди в рядку стану (Show Seconds in Time)

Показує **секунди** у часі рядка стану, перемикання дотиком. Можна задати **колір кожній цифрі** або накласти **градієнт** на весь рядок.<br>**Довге натискання** відкриває напівпрозоре вікно з **мережевим часом** (перетягування, розмір, градієнт); **власні завдання** вмикають секунди потрібної години.<br>Основний шлях використовує лише відкриті API AOSP (закриті класи OPPO / OnePlus / realme необов’язкові, за відсутності пропускаються). Перевірено на ColorOS 15 / 16.

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Можливості

| Можливість | Опис |
|------|------|
| Секунди в рядку стану | `HH:mm:ss` / `h:mm:ss a`, враховує 12/24-годинний формат і часовий пояс. У кінець додається **пробіл нульової ширини**, щоб теми не знімали кольори; після втрати колір автоматично перемальовується (пауза 5 хвилин). |
| Перемикання дотиком | **Дотик** вмикає й вимикає секунди (можна обрати **подвійний дотик**); **довге натискання** зарезервоване для вікна перегляду. Усередині завдання показується «керує завдання». |
| Колір за цифрами / градієнт | Шість цифр **кожна своїм кольором** (10 наборів + повзунки HSV + як у системі), двокрапки оптично по центру. Або **градієнт** від початкового до кінцевого (6 наборів, обидва кінці налаштовуються, на світлому фоні затемнюється). Картка перегляду **малюється сама**, тому збігається з рядком стану. |
| Вікно за довгим натисканням | Напівпрозоре вікно з **мережевим часом**; перетягування, розмір за ріг ◣, положення й розмір **запам’ятовуються**; градієнт і цифри однакової ширини. |
| Власні завдання | Вікна **щогодини** або **щодня**: увімкнути секунди за N хвилин на M хвилин, потім повернути. Зміна часу чи пояса перераховується одразу, плюс резервний перерахунок кожні 60 секунд. |
| Порівняння з мережевим часом | Показує розбіжність із **мережевим часом**; кнопка **Синхронізувати** вмикає системне автонастроювання. |
| adb не потрібен | Запис іде через `Settings.Secure`; без `WRITE_SECURE_SETTINGS` використовується broadcast і пише SystemUI — **adb не потрібен** для жодної функції. |
| Плитка у шторці | Додайте плитку «Показувати секунди», щоб перемикати без відкриття застосунку. |
| Назва ресурсу годинника | Якщо годинник не розпізнано, натисніть **Автовизначення** або введіть назву ресурсу (напр. `clock`). |
| 21 мова інтерфейсу | За мовою системи: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული; інші — англійська. |
| Збереження / охоплення | Стан зберігається в `Settings.Secure`, спільний для процесів і зберігається після перезапуску. **Перевірено на ColorOS 15 / 16 (Android 15 / 16)**; ядро використовує лише відкриті API, тож інші виробники мають працювати, але тестувалися лише OPPO / OnePlus. Мінімум Android 10. |

## Режими кольору

Два способи розфарбування, перемикаються в картці **Кольори цифр**: **за цифрами** або **градієнт** на весь рядок.

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="За цифрами: кожна цифра своїм кольором, двокрапки оптично по центру" width="100%">
<br>
За цифрами: кожна цифра своїм кольором, двокрапки оптично по центру
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="Градієнт: від початкового до кінцевого, 6 наборів або власні кінці" width="100%">
<br>
Градієнт: від початкового до кінцевого, 6 наборів або власні кінці
</td>
</tr></table>

## Встановлення

1. Завантажте APK зі сторінки [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Увімкніть модуль у **LSPosed** і задайте область **Системний інтерфейс** (`com.android.systemui`)
3. Перезапустіть процес SystemUI або пристрій
4. Екран налаштувань відкривається з **менеджера LSPosed** (значка в лончері немає)

> Потрібен root (LSPosed). **Права adb зазвичай не потрібні**: запис через `Settings.Secure`, а без `WRITE_SECURE_SETTINGS` — через broadcast, пише SystemUI. Щоб екран налаштувань писав **напряму**:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Без надання нічого не змінюється: дотики, завдання й вікно працюють усередині SystemUI.

## Використання

- **Торкніться часу**: секунди вмк/вимк (типово); у налаштуваннях можна обрати **подвійний дотик**.
- **Довге натискання**: вікно з **мережевим часом**, перетягування; дотик показує **закрити** (справа зверху) і **розмір** (зліва знизу).
- Картка **Кольори цифр** має дві сторінки, і сам таб є перемикачем.
- Картка **Власні завдання** планує вікна на кшталт «секунди за 2 хвилини до кожної години».
- Перемикання використовує глобальну перевірку дотику і **не конфліктує зі шторкою**.

## Передперегляд

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Секунди вимк: `15:06`, десятки годин зберігають колір" width="100%">
<br>
Секунди вимк: `15:06`, десятки годин зберігають колір
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Секунди вмк: `15:06:03`, хвилини й секунди системного кольору" width="100%">
<br>
Секунди вмк: `15:06:03`, хвилини й секунди системного кольору
</td>
</tr></table>

## Як це працює

Модуль хукає лише **SystemUI** і використовує стабільний legacy Xposed API.
**Ядро спирається лише на відкриті API AOSP, закриті класи вендорів необов’язкові й пропускаються за відсутності**, тому не ламає систему й не спричиняє безпечний режим LSPosed:

- **Секунди**: `TextView.setText` (after) кешує системний текст, далі `TextView.onDraw` (before) пише `початковий:ss`, оновлення щосекунди через `Handler`.
- **Колір за цифрами**: `ForegroundColorSpan` на символ (чужі span знімаються); двокрапки піднімаються на 0.11em, бо багатосегментні макети ставлять гліф нижче.
- **Градієнт**: `CharacterStyle` викликає `setShader` у `updateDrawState`; картка перегляду **малюється сама** (`GradientTimeView`), бо ColorOS 15 квантує shader за сегментами тексту.
- **Перемикання**: панель перехоплює дотики до рядка стану, тому модуль хукає глобальний `View.dispatchTouchEvent` і перемикає лише коли дотик у межах годинника без протягування; подію **не споживає**.
- **Завдання**: перевірка «чи локальний час у вікні», точні таймери на межах і резерв кожні 60 секунд.

## Споживання

- **Секунди вмк**: таймер щосекунди (неминуча ціна). Замір SystemUI на ядро: 7.0% вимк, 7.25% вмк (60 с).
- **Секунди вимк**: таймер **зупиняється** (`stopTicker`), пробуджень щосекунди немає.
- **Захист кольору** сповільнюється 2s → 4s → … → 30s і завершується за 5 хвилин без втрат.
- Стан **кешується в процесі**; завдання використовують `Handler.postDelayed`: **без WakeLock і AlarmManager**, глибокий сон не будиться.

## Збірка

Потрібні **JDK 17+** (17 або 21) і **Android SDK Platform 35** (AGP завантажить Build-Tools 35).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Резервна копія та продовження розробки (важливо)

У теці `ClockSeconds/` збережіть **усе, крім `build/` і `.gradle/`** (код ~300 КБ):

| Категорія | Файли | Примітка |
|------|------|------|
| Файли збірки | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, `keystore.properties` | Версії, залежності, параметри, підпис |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Фіксує Gradle 8.14.5 |
| Код | `app/src/main/java/**` (20 `.java`) | Логіка модуля, налаштування, класи малювання |
| Ресурси | `app/src/main/res/**` | Рядки (22 локалі), теми |
| Метадані Xposed | `app/src/main/resources/META-INF/xposed/*` | Клас входу, область, рівень API |
| Маніфест | `app/src/main/AndroidManifest.xml` | Дозволи, активності |
| Інструменти | `tools/**` | Генератор README, перевірка i18n, скрипт релізу |
| Документи | `README*.md`, `docs/**`, `CHANGELOG.md` | Опис, скріншоти, журнал змін |
| CI | `.github/workflows/*.yml` | Підписана збірка за тегом |

## Діагностика

<details>
<summary>Показати кроки</summary>

Дивіться logcat:

- **Release-збірки** ховають усі діагностичні логи за `BuildConfig.DEBUG`.
- Для налагодження встановіть **debug-збірку** (`assembleDebug`, той самий ключ) і фільтруйте:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- Секунд немає: перевірте область System UI у LSPosed і перезапустіть; далі прочитайте або задайте ключ:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=вмк 0=вимк
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- Кольори чи секунди повертаються самі: система або тема перебудовує текст, модуль перемальовує; якщо не відновлюється, шукайте в debug-лозі `color-guard stopped` / `span-theft`.

</details>

## Відгук

Питання та пропозиції — у [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues).

## License

[MIT](LICENSE) © kelebao
