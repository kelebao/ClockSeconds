# -*- coding: utf-8 -*-
"""Slot translations for README generation: English (en)."""

TEXTS = {
    'en': {
        'title': 'Show Seconds in Time',
        'tagline': (
            'Show **seconds** in the status bar time, toggle with a tap. Colour **each digit** separately or lay '
            'a **gradient** across the whole string.<br>**Long-press** for a frosted-glass window with **network '
            'time** (draggable, resizable, gradient too); schedule **custom tasks** that turn seconds on at the '
            'hour.<br>Core path uses only public AOSP APIs (OPPO / OnePlus / realme private classes are optional, '
            'skipped when absent). Verified on ColorOS 15 / 16.'
        ),
        'h_features': 'Features',
        'th_feature': 'Feature',
        'th_desc': 'Description',
        'rows': [
            ('Seconds in the status bar',
             '`HH:mm:ss` / `h:mm:ss a`, follows the system 12/24-hour setting and time zone; a **zero-width space** '
             'is appended so theme modules cannot strip the colours, with automatic repaint when colours are lost '
             '(5-minute cooldown).'),
            ('Tap to toggle',
             '**Tap** the time to switch seconds on/off (double tap selectable); **long-press** is reserved for the '
             'floating preview; a tap inside a task window says the task has taken over.'),
            ('Per-digit colours / gradient',
             'Six digits, **one colour each** (10 presets + HSV sliders + follow-system), colons optically centred; '
             'or one **gradient** from start to end colour (6 presets, both ends custom, auto-darkened on light '
             'backgrounds). The preview card is **self-drawn**, so it matches the status bar pixel for pixel.'),
            ('Long-press floating window',
             'Frosted-glass overlay showing **network time**; drag to move, drag the ◣ corner to resize, position and '
             'size are **remembered**; same gradient, tabular digits so the seconds do not jitter.'),
            ('Custom tasks',
             'Windows on **every hour** or **every day**: turn seconds on N minutes early for M minutes, then restore; '
             'time or time-zone changes recompute at once, plus a 60-second fallback, so deep sleep cannot miss it.'),
            ('Network time comparison',
             'Shows the offset between **network time** and the device clock; tap **Sync** to enable the system '
             '"set time automatically" switch.'),
            ('No adb grant needed',
             'Writes go through `Settings.Secure`; without `WRITE_SECURE_SETTINGS` a broadcast lets SystemUI write '
             'instead — **no adb command is required** for any feature.'),
            ('Quick settings tile',
             'Add the "Show seconds" tile to the notification shade to toggle seconds without opening the app.'),
            ('Clock resource name',
             'If the clock is not detected automatically, tap **Auto detect**, or type the resource name (e.g. '
             '`clock`) by hand.'),
            ('21 UI languages',
             'App language follows the system: 简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, '
             'ไทย, Deutsch, Español, Français, Українська, Polski, Português (Brasil), فارسی, العربية, Bahasa '
             'Indonesia, Italiano, Ελληνικά, ქართული; anything else falls back to English.'),
            ('Persistence / scope',
             'State lives in `Settings.Secure` and survives a SystemUI or device reboot. **Verified on ColorOS 15 / 16 '
             '(Android 15 / 16)**; the core is public-API only so other vendors should work, but only OPPO / OnePlus '
             'devices were tested. Minimum Android 10.'),
        ],
        'h_colors': 'Colour modes',
        'colors_intro': (
            'Two ways to colour the time, switched inside the **Digit colours** card: **per-digit** '
            '(one colour per digit) or a **gradient** across the whole string.'
        ),
        'cap_digit': 'Per-digit: every digit has its own colour, colons optically centred',
        'cap_grad': 'Gradient: start colour to end colour, 6 presets or custom ends',
        'h_install': 'Install',
        'install': [
            'Download the latest APK from [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Enable the module in **LSPosed** and scope it to **System UI** (`com.android.systemui`)',
            'Restart the SystemUI process, or reboot the device',
            'Open the settings screen from the **LSPosed manager** (no launcher icon is created)',
        ],
        'note1': (
            'Root (LSPosed) is required. **An adb grant is usually unnecessary**: writes go through '
            '`Settings.Secure` and fall back to a broadcast written by SystemUI. To let the settings screen write '
            '**directly** (one round trip less), grant it:'
        ),
        'note2': (
            'Skipping it changes nothing: taps, task scheduling and the floating window all run inside SystemUI, '
            'which already holds the permission.'
        ),
        'h_usage': 'Usage',
        'usage': [
            '**Tap the status bar time** to toggle seconds; a **double tap** can be selected in the app.',
            '**Long-press the time** for a frosted window with **network time**: drag to move, tap the time to show '
            'the **close** (top right) and **resize** (bottom left) buttons.',
            'The **Digit colours** card has two pages: **per-digit** colours six digits individually, **gradient** '
            'lays one colour band across the string (the tab itself is the switch).',
            'The **Custom tasks** card schedules "seconds on 2 minutes before every hour" style windows, or the '
            'opposite.',
            'Switching uses a global touch hit test, so it **never conflicts with pulling down the shade**.',
            'The settings screen shows the current state, gesture, colours, network offset and a one-tap **sync**.',
        ],
        'h_demo': 'Preview',
        'cap_off': 'Seconds off: `15:06`, the hour digit keeps its colour',
        'cap_on': 'Seconds on: `15:06:03`, minutes and seconds keep the system colour',
        'h_how': 'How it works',
        'how_intro': [
            'The module hooks **SystemUI only**, with the conservative legacy Xposed API.',
            '**The core path uses public AOSP APIs only; vendor-private support is optional and skipped when '
            'absent**, so it cannot corrupt system state or trip the LSPosed safe mode:',
        ],
        'how': [
            '**Seconds**: hook `TextView.setText` (after) to cache the system text, then `TextView.onDraw` (before) '
            'to write `original:ss`, driven by a 1-second `Handler`.',
            '**Per-digit colour**: a `ForegroundColorSpan` per digit (foreign spans removed first); the colon gets a '
            '`CharacterStyle` lifting it 0.11em, because a multi-run layout puts the host glyph lower.',
            '**Gradient**: a `CharacterStyle` calling `setShader` in `updateDrawState`; the settings preview is '
            '**self-drawn** (`GradientTimeView`) because ColorOS 15 quantises a shader per draw run.',
            '**Toggle**: the notification panel swallows status-bar touches, so the module hooks `View.dispatchTouchEvent` '
            'and flips the switch only when the press lands inside the clock bounds with almost no movement. It '
            '**never consumes** the event.',
            '**Tasks**: a window is "is the local time inside it", with exact boundary timers plus a 60-second '
            'fallback and an immediate recompute on time or time-zone changes.',
        ],
        'h_power': 'Power',
        'power': [
            '**Seconds on**: a 1-second timer drives the refresh — the inherent cost of showing seconds. Measured '
            'SystemUI single-core load: 7.0% off, 7.25% on (60-second window), below per-slice noise.',
            '**Seconds off**: the timer is **stopped immediately** (`stopTicker`): no per-second wake-up, no '
            'cross-process read.',
            '**Colour keeping** backs off 2s → 4s → … → 30s and stops after 5 minutes without a loss '
            '(`color-guard stopped`).',
            'State is **cached in the process**, so the timer never reads `Settings.Secure` every second.',
            'The task fallback uses `Handler.postDelayed`: **no WakeLock, no AlarmManager**, so deep sleep is never '
            'woken.',
            'Timers hold a **weak reference** to the view and stop once it is detached.',
        ],
        'h_build': 'Build',
        'build_intro': (
            'Requires **JDK 17+** (17 or 21 recommended) and **Android SDK Platform 35** '
            '(Build-Tools 35 is fetched by AGP).'
        ),
        'h_backup': 'Backup & continuing development (important)',
        'backup_intro': (
            'In the `ClockSeconds/` folder keep **everything except `build/` and `.gradle/`** '
            '(the source part is only about 300 KB):'
        ),
        'th_backup_cat': 'Category',
        'th_backup_files': 'Files',
        'th_backup_note': 'Notes',
        'backup_rows': [
            ('Build scripts',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`, '
             '`keystore.properties`', 'Versions, dependencies, compiler flags, signing'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Pins Gradle 8.14.5'),
            ('Source', '`app/src/main/java/**` (20 `.java` files)', 'Module logic, settings screen, drawing classes'),
            ('Resources', '`app/src/main/res/**`', 'Strings for 22 locales, themes'),
            ('Xposed metadata', '`app/src/main/resources/META-INF/xposed/*`', 'Entry class, scope, API level'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permissions, activities'),
            ('Tool scripts', '`tools/**`', 'README generator, i18n check, release scripts'),
            ('Docs', '`README*.md`, `docs/**`, `CHANGELOG.md`', 'Documentation, screenshots, changelog'),
            ('CI', '`.github/workflows/*.yml`', 'Signed builds on tag'),
        ],
        'h_trouble': 'Troubleshooting',
        'trouble_summary': 'Show the troubleshooting steps',
        'trouble_intro': 'Check logcat:',
        'trouble': [
            ('b', '**Release builds** strip every diagnostic log behind `BuildConfig.DEBUG`, so the module prints '
                  'nothing in logcat.'),
            ('b', 'For debugging install the **debug build** (`assembleDebug`, signed with the same key so it '
                  'installs over the release), then filter:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'No seconds at all: make sure LSPosed has System UI in scope and restart; then read or force the '
                  'switch:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'Colours or seconds revert by themselves: the system or a theme is rebuilding the text and the '
                  'module repaints it; if it never recovers, search the debug log for `color-guard stopped` / '
                  '`span-theft`.'),
            ('b', 'Nothing works on a non-ColorOS device: install the debug build and check `candidate:` / '
                  '`CONFIG CLOCK:` to see whether the clock class or resource name matches the detection rules.'),
        ],
        'h_feedback': 'Feedback',
        'feedback': 'Questions, ideas or reports? Open a [GitHub issue](https://github.com/kelebao/ClockSeconds/issues) — I answer quickly.',
        'h_license': 'License',
    },
}
