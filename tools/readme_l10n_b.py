# -*- coding: utf-8 -*-
"""Slot translations for README generation: English (en)."""

TEXTS = {
    'en': {
        'title': 'Clock Seconds (时钟显秒)',
        'tagline': (
            'Show **seconds** in the status bar clock, toggle with a single tap, '
            'and keep the **hour tens digit in red**.<br>'
            'No vendor-private classes involved. Verified on ColorOS 15, '
            'and designed to cover Xiaomi, OnePlus, Honor, vivo and other major vendors.'
        ),
        'h_features': 'Features',
        'th_feature': 'Feature',
        'th_desc': 'Description',
        'rows': [
            ('Seconds',
             'The status bar clock shows seconds (`HH:mm:ss` / `h:mm:ss a`, follows the system 12/24-hour setting)'),
            ('Tap to toggle',
             '**Tap** the status bar clock to toggle seconds on/off; long-press gesture also available'),
            ('Red hour digit',
             'The hour **tens digit** is colored red (the `1` in `13`, the `0` in `05`); '
             'minutes and seconds keep the system color. Single-digit hours (e.g. `9:45`) '
             'have no tens digit and stay untouched. Effective on all vendors'),
            ('UI languages',
             'The app UI is localized into **21 languages** (follows the system language automatically): '
             '简体中文, English, 繁體中文, 日本語, 한국어, Türkçe, Русский, Tiếng Việt, ไทย, '
             'Deutsch, Español, Français, Українська, Polski, Português (Brasil), '
             'فارسی, العربية, Bahasa Indonesia, Italiano, Ελληνικά, ქართული'),
            ('Persistence',
             'State is stored in `Settings.Secure`, shared across processes, survives SystemUI restarts'),
            ('Compatibility',
             'Verified on ColorOS 15 / 16 / 17 (OPPO / OnePlus / realme); also works out of the box '
             'on other rooted vendors such as Xiaomi / Honor / Redmi. No vendor-private classes, '
             'so LSPosed safe mode will not be triggered. Minimum Android 10'),
        ],
        'h_install': 'Install',
        'install': [
            'Download the latest APK from [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest)',
            'Enable the module in **LSPosed**, scope: **System UI (`com.android.systemui`)**',
            'Restart the SystemUI process or reboot your device',
            'Open the config page from the **LSPosed manager** (no launcher icon is created)',
        ],
        'note1': (
            'Requires root (LSPosed). To grant `WRITE_SECURE_SETTINGS`:'
        ),
        'note2': (
            'Without it you can still toggle by tapping the status bar clock '
            '(that runs inside the SystemUI process and already has the permission).'
        ),
        'h_usage': 'Usage',
        'usage': [
            '**Tap the status bar clock**: toggle seconds on/off (default gesture).',
            'Toggling is implemented with a global touch hit-test, so it **never conflicts with '
            '"pull down the notification shade"** — a pure tap toggles, a swipe still pulls the '
            'shade down. You can switch to a **long-press** gesture in the config page if you prefer.',
            'The config page shows the current state, the gesture setting, and a refresh button.',
        ],
        'h_demo': 'Preview',
        'cap_off': 'Seconds off: `15:06` — the tens digit `1` of the hour stays red',
        'cap_on': 'Seconds on: `15:06:03` — minutes and seconds keep the system color',
        'h_how': 'How It Works',
        'how_intro': [
            'The module hooks **SystemUI** only, using the well-tested legacy Xposed approach,',
            'and **never touches any vendor-private class**, so system internal state is never broken '
            'and LSPosed safe mode is never triggered:',
        ],
        'how': [
            '**Seconds**: hook the stable `TextView.setText` (after) to cache the raw text the system '
            'puts on the status bar clock,\n'
            'then hook `TextView.onDraw` (before) to overwrite the display with `rawText:ss` after '
            'the system has set it,\n'
            'driven by a 1-second `Handler`. The appended seconds inherit the clock\'s own text color '
            '(measured `#ccffffff`).\n'
            'The hour tens digit is additionally colored red (a red `ForegroundColorSpan` on that '
            'single character), regardless of whether seconds are shown;\n'
            'minutes and seconds are never red; single-digit hours have no tens digit and stay untouched.',
            '**Toggle**: ColorOS\'s notification panel intercepts touches in the status bar area, so '
            'the clock\'s own `OnTouchListener` never receives events.\n'
            'The module therefore hooks the global `View.dispatchTouchEvent` and only flips the '
            '`Settings.Secure` switch when the press lands inside\n'
            'the recognized clock\'s screen bounds with almost no movement (tap) or is held for '
            '500 ms (long-press). The module **does not consume**\n'
            'any touch event, so pulling down the notification shade works as usual.',
        ],
        'h_power': 'Power Saving',
        'power': [
            '**Seconds ON**: the module needs a 1-second timer to drive refreshes '
            '(the inherent cost of showing seconds, ~60 clock redraws per minute).',
            '**Seconds OFF**: the module **stops that timer immediately** (`stopTicker`) — no '
            'per-second wakeups at all, no cross-process reads.\n'
            'It returns to the system\'s original "refresh once per minute" rhythm, which is '
            'genuinely more power-efficient.',
            'State is cached **in-process** (`sSecondsEnabled`), so the timer never reads '
            '`Settings.Secure` every second (avoiding a per-second IPC).',
            'No time formatter is constructed: the module reuses the system\'s raw text and only '
            'appends `:ss`, so there is zero formatter overhead.',
            'The timer holds a **weak reference** to the view and only continues while the view is '
            '`isAttachedToWindow()`, exiting automatically once the view is destroyed.',
        ],
        'h_build': 'Build',
        'build_intro': (
            'Requires **JDK 17+** (17 or 21 recommended) and **Android SDK Platform 35** '
            '(Build-Tools 35 is auto-installed by AGP).'
        ),
        'h_backup': 'Backup & Continued Development (Important)',
        'backup_intro': (
            'In the `ClockSeconds/` directory, **keep every file except `build/` and '
            '`.gradle/`** (the source part is only about 140 KB):'
        ),
        'th_backup_cat': 'Category',
        'th_backup_files': 'File',
        'th_backup_note': 'Note',
        'backup_rows': [
            ('Build scripts',
             '`settings.gradle.kts`, `build.gradle.kts`, `app/build.gradle.kts`, `gradle.properties`',
             'Version, dependencies, compiler flags'),
            ('Gradle Wrapper', '`gradlew`, `gradlew.bat`, `gradle/wrapper/*`', 'Pins Gradle 8.14.5'),
            ('Source', '`app/src/main/java/**` (3 `.java` files)', 'The module logic'),
            ('Resources', '`app/src/main/res/**`',
             'Strings (default Chinese + 21 values-xx locales), theme'),
            ('Xposed metadata', '`app/src/main/resources/META-INF/xposed/*`',
             'Entry class, scope, API version'),
            ('Manifest', '`app/src/main/AndroidManifest.xml`', 'Permissions, Activity'),
            ('Docs', '`README*.md`', 'Documentation in 21 languages'),
            ('CI', '`.github/workflows/release.yml`', 'Tag-triggered signed builds'),
        ],
        'h_trouble': 'Troubleshooting',
        'trouble_summary': 'Expand troubleshooting steps',
        'trouble_intro': 'Check the logcat:',
        'trouble': [
            ('b', '**Release builds** strip all diagnostic logs via the `BuildConfig.DEBUG` gate '
                  '(smaller, cleaner); nothing from this module is printed to logcat.'),
            ('b', 'For debugging, temporarily install the **debug build** (`assembleDebug`, signed '
                  'with the same production key so it can overwrite the release install directly), '
                  'then filter by TAG:'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', 'If seconds do not show at all: first confirm LSPosed has the SystemUI scope '
                  'checked and restarted; then check whether the switch was persisted as off —\n'
                  'read / force-write it via adb:'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', 'If it does not work on a non-ColorOS device: install the debug build and look '
                  'at the `candidate:` / `CONFIG CLOCK:` lines in the log\n'
                  'to see whether your device\'s status bar clock resource name / class name is '
                  'covered by the recognition rules; if not, one extra rule line is all it takes.'),
        ],
        'h_license': 'License',
    },
}
