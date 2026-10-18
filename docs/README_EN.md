<div align="center">

[简体中文](../README.md) · **English** · [繁體中文](README_zh-TW.md) · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# Clock Seconds (时钟显秒)

Show **seconds** in the status bar clock, toggle with a single tap, and keep the **hour tens digit in red**.<br>No vendor-private classes involved. Verified on ColorOS 15, and designed to cover Xiaomi, OnePlus, Honor, vivo and other major vendors.

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## Features

| Feature | Description |
|------|------|
| Seconds | The status bar clock shows seconds (`HH:mm:ss` / `h:mm:ss a`, follows the system 12/24-hour setting) |
| Tap to toggle | **Tap** the status bar clock to toggle seconds on/off; long-press gesture also available |
| Red hour digit | The hour **tens digit** is colored red (the `1` in `13`, the `0` in `05`); minutes and seconds keep the system color. Single-digit hours (e.g. `9:45`) have no tens digit and stay untouched. Effective on all vendors |
| UI languages | The app UI is localized into **21 languages** (follows the system language automatically): 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული |
| Persistence | State is stored in `Settings.Secure`, shared across processes, survives SystemUI restarts |
| Compatibility | Verified on ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); also works out of the box on other rooted vendors such as Xiaomi / Honor / Redmi. No vendor-private classes, so LSPosed safe mode will not be triggered. Minimum Android 10 |

## Install

1. Download the latest APK from [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)
2. Enable the module in **LSPosed**, scope: **System UI (`com.android.systemui`)**
3. Restart the SystemUI process or reboot your device
4. Open the config page from the **LSPosed manager** (no launcher icon is created)

> Requires root (LSPosed). To grant `WRITE_SECURE_SETTINGS`:
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> Without it you can still toggle by tapping the status bar clock (that runs inside the SystemUI process and already has the permission).

## Usage

- **Tap the status bar clock**: toggle seconds on/off (default gesture).
- Toggling is implemented with a global touch hit-test, so it **never conflicts with "pull down the notification shade"** — a pure tap toggles, a swipe still pulls the shade down. You can switch to a **long-press** gesture in the config page if you prefer.
- The config page shows the current state, the gesture setting, and a refresh button.

## Preview

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="Seconds off: `15:06` — the tens digit `1` of the hour stays red" width="100%">
<br>
Seconds off: `15:06` — the tens digit `1` of the hour stays red
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="Seconds on: `15:06:03` — minutes and seconds keep the system color" width="100%">
<br>
Seconds on: `15:06:03` — minutes and seconds keep the system color
</td>
</tr></table>

## How It Works

The module hooks **SystemUI** only, using the well-tested legacy Xposed approach,
and **never touches any vendor-private class**, so system internal state is never broken and LSPosed safe mode is never triggered:

- **Seconds**: hook the stable `TextView.setText` (after) to cache the raw text the system puts on the status bar clock,
  then hook `TextView.onDraw` (before) to overwrite the display with `rawText:ss` after the system has set it,
  driven by a 1-second `Handler`. The appended seconds inherit the clock's own text color (measured `#ccffffff`).
  The hour tens digit is additionally colored red (a red `ForegroundColorSpan` on that single character), regardless of whether seconds are shown;
  minutes and seconds are never red; single-digit hours have no tens digit and stay untouched.
- **Toggle**: ColorOS's notification panel intercepts touches in the status bar area, so the clock's own `OnTouchListener` never receives events.
  The module therefore hooks the global `View.dispatchTouchEvent` and only flips the `Settings.Secure` switch when the press lands inside
  the recognized clock's screen bounds with almost no movement (tap) or is held for 500 ms (long-press). The module **does not consume**
  any touch event, so pulling down the notification shade works as usual.

## Power Saving

- **Seconds ON**: the module needs a 1-second timer to drive refreshes (the inherent cost of showing seconds, ~60 clock redraws per minute).
- **Seconds OFF**: the module **stops that timer immediately** (`stopTicker`) — no per-second wakeups at all, no cross-process reads.
  It returns to the system's original "refresh once per minute" rhythm, which is genuinely more power-efficient.
- State is cached **in-process** (`sSecondsEnabled`), so the timer never reads `Settings.Secure` every second (avoiding a per-second IPC).
- No time formatter is constructed: the module reuses the system's raw text and only appends `:ss`, so there is zero formatter overhead.
- The timer holds a **weak reference** to the view and only continues while the view is `isAttachedToWindow()`, exiting automatically once the view is destroyed.

## Build

Requires **JDK 17+** (17 or 21 recommended) and **Android SDK Platform 35** (Build-Tools 35 is auto-installed by AGP).

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## Backup & Continued Development (Important)

In the `ClockSeconds/` directory, **keep every file except `build/` and `.gradle/`** (the source part is only about 140 KB):

| Category | File | Note |
|------|------|------|
| Build scripts | `settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties` | Version, dependencies, compiler flags |
| Gradle Wrapper | `gradlew`, `gradlew.bat`, `gradle/wrapper/*` | Pins Gradle 8.14.5 |
| Source | `app/src/main/java/**` (3 `.java` files) | The module logic |
| Resources | `app/src/main/res/**` | Strings (default Chinese + 21 values-xx locales), theme |
| Xposed metadata | `app/src/main/resources/META-INF/xposed/*` | Entry class, scope, API version |
| Manifest | `app/src/main/AndroidManifest.xml` | Permissions, Activity |
| Docs | `README*.md` | Documentation in 21 languages |
| CI | `.github/workflows/release.yml` | Tag-triggered signed builds |

## Troubleshooting

<details>
<summary>Expand troubleshooting steps</summary>

Check the logcat:

- **Release builds** strip all diagnostic logs via the `BuildConfig.DEBUG` gate (smaller, cleaner); nothing from this module is printed to logcat.
- For debugging, temporarily install the **debug build** (`assembleDebug`, signed with the same production key so it can overwrite the release install directly), then filter by TAG:

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- If seconds do not show at all: first confirm LSPosed has the SystemUI scope checked and restarted; then check whether the switch was persisted as off —
  read / force-write it via adb:

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- If it does not work on a non-ColorOS device: install the debug build and look at the `candidate:` / `CONFIG CLOCK:` lines in the log
  to see whether your device's status bar clock resource name / class name is covered by the recognition rules; if not, one extra rule line is all it takes.

</details>

## Feedback

Have a question, suggestion, or feedback? Leave a message on [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) — I will reply as soon as I can.

## License

[MIT](LICENSE) © kelebao
