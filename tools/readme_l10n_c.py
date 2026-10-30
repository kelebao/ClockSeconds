# -*- coding: utf-8 -*-
"""Slot translations for README generation: 繁體中文 (zh-TW)."""

TEXTS = {
    'zh-TW': {
        'title': '時間顯示秒數（Show Seconds in Time）',
        'tagline': (
            '讓狀態列時間顯示**秒**，單擊即切換。六個數字可**逐位上色**，也能整串鋪一條**漸層**。<br>'
            '**長按**彈出毛玻璃懸浮視窗即時顯示**網路時間**（可拖曳、可縮放，同樣支援漸層）；'
            '還能排**自訂任務**，到點自動開秒。<br>'
            '核心鏈路只用 AOSP 公開 API（OPPO / 一加 / realme 私有類別為選用增強、找不到即跳過）。'
            '已在 ColorOS 15 / 16 實機驗證。'
        ),
        'h_features': '功能',
        'th_feature': '功能',
        'th_desc': '說明',
        'rows': [
            ('狀態列顯秒',
             '`HH:mm:ss` / `h:mm:ss a`，跟隨系統 12/24 小時制與時區；文字末尾追加**零寬空格**，'
             '避免主題模組重建文字時剝掉顏色 span，掉色後自動重繪（5 分鐘冷卻）。'),
            ('點擊切換',
             '**單擊**時間切換顯秒開/關（也可改**雙擊**）；**長按**專用於懸浮預覽；'
             '任務視窗內點擊會提示「已由任務接管」。'),
            ('逐位取色 / 漸層',
             '六個數字**各自配色**（10 組預設 + HSV 滑桿 + 跟隨系統），冒號自動光學置中；'
             '或整串**漸層**（起點色 → 終點色，6 組預設、兩端自訂，淺底自動壓暗）。'
             '預覽卡為**自繪**，與狀態列逐像素一致。'),
            ('長按懸浮預覽',
             '毛玻璃半透明視窗顯示**網路時間**；拖曳移動、拖 ◣ 縮放，位置與尺寸**自動記憶**；'
             '同樣支援漸層、數字等寬（秒數跳動時不抖）。'),
            ('自訂任務',
             '以**整點**或**每天某時刻**排程：提前 N 分鐘自動開秒、持續 M 分鐘後還原；'
             '改系統時間或時區會立即重算，另有 60 秒兜底重算。'),
            ('網路時間對比',
             '顯示**網路標準時間**與本機時間的偏差；點**同步**即開啟系統「自動設定時間」。'),
            ('免 adb 授權',
             '寫入優先走 `Settings.Secure`；沒有 `WRITE_SECURE_SETTINGS` 時改走廣播由 SystemUI 代寫——'
             '**任何功能都不需要 adb 指令**。'),
            ('下拉磁貼',
             '在通知欄加入「時間顯秒」磁貼，一鍵開關顯秒，不必打開設定頁。'),
            ('時間識別名',
             '未自動識別時可點**自動偵測**，也可手動填資源名（如 `clock`）。'),
            ('21 種介面語言',
             '介面跟隨系統語言：简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、Tiếng Việt、'
             'ไทย、Deutsch、Español、Français、Українська、Polski、Português (Brasil)、فارسی、العربية、'
             'Bahasa Indonesia、Italiano、Ελληνικά、ქართული；其餘回退英文。'),
            ('持久化 / 適配範圍',
             '狀態存於 `Settings.Secure`，跨程序共享，重啟 SystemUI 或裝置後保持。'
             '**ColorOS 15 / 16（Android 15 / 16）已實機驗證**；核心只用公開 API，其他廠商理論上可用，'
             '但**尚未實機驗證**。最低 Android 10。'),
        ],
        'h_colors': '顏色效果',
        'colors_intro': (
            '兩種上色方式，在「數字顏色」卡內切換：**逐位顏色**（每個數字各自一色）與**整串漸層**。'
        ),
        'cap_digit': '逐位顏色：六位數字各選一色，冒號自動光學置中',
        'cap_grad': '整串漸層：起點色 → 終點色，6 組預設、兩端自訂',
        'h_install': '安裝',
        'install': [
            '從 [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) 下載最新 APK',
            '在 **LSPosed** 啟用本模組，作用域勾選 **系統介面**（`com.android.systemui`）',
            '重新啟動 SystemUI 程序，或重新開機',
            '從 **LSPosed 管理器**開啟設定頁（桌面不生成圖示）',
        ],
        'note1': (
            '需要 root（LSPosed）。**通常不需要 adb 授權**：寫入優先走 `Settings.Secure`，'
            '缺少 `WRITE_SECURE_SETTINGS` 時改由 SystemUI 代寫。若希望設定頁**直接寫入**，可授予：'
        ),
        'note2': (
            '不授予同樣不影響使用：點擊、任務排程、懸浮預覽都在 SystemUI 程序內完成，天然具備權限。'
        ),
        'h_usage': '使用',
        'usage': [
            '**單擊狀態列時間**：切換顯秒開/關（預設手勢）；也可在設定頁改為**雙擊**。',
            '**長按狀態列時間**：彈出毛玻璃視窗顯示**網路時間**，拖曳可移動，點一下時間浮現'
            '**關閉**（右上）/ **縮放**（左下）按鈕。',
            '**數字顏色**卡分兩頁：「逐位顏色」為六個數字單獨配色；「漸層」把整串鋪成一條色帶'
            '（頁籤本身就是開關）。',
            '**自訂任務**卡可排「整點前 2 分鐘自動開秒」這類排程，也能反過來在特定期間強制關秒。',
            '切換以全域觸控命中判定實作，**不會與下拉通知欄衝突**。',
            '設定頁可查看目前狀態、手勢、配色，並對比網路時間、一鍵**同步**系統對時。',
        ],
        'h_demo': '效果預覽',
        'cap_off': '關閉顯秒：`15:06`，小時十位保持所選顏色',
        'cap_on': '開啟顯秒：`15:06:03`，分與秒保持系統原色',
        'h_how': '工作原理',
        'how_intro': [
            '模組只 hook **SystemUI** 一個程序，採用穩妥的 legacy Xposed 範式。',
            '**核心鏈路只走 AOSP 公開 API，私有類別相容為選用、找不到即跳過**，'
            '因此不會破壞系統狀態、不會觸發 LSPosed 安全模式：',
        ],
        'how': [
            '**顯秒**：hook 穩定的 `TextView.setText`（after）快取系統原始文字，再 hook '
            '`TextView.onDraw`（before）覆寫為 `原文字:ss`，由 1 秒級 `Handler` 驅動。',
            '**逐位上色**：每個數字掛 `ForegroundColorSpan`（先剝掉外來 span）；冒號另掛 '
            '`CharacterStyle` 抬高 0.11em——文字被切成多個繪製 run 時宿主字形會下沉。',
            '**漸層**：用 `CharacterStyle` 在 `updateDrawState` 裡 `setShader`；設定頁預覽卡改為'
            '**自繪**（`GradientTimeView`），因為 ColorOS 15 會把 shader 按繪製 run 量化。',
            '**切換**：通知面板會攔截狀態列觸控，因此 hook 全域 `View.dispatchTouchEvent`，'
            '僅在按下點落在時間邊界內且幾乎無位移時翻轉開關，**不消費**任何事件。',
            '**任務**：以「本機時間是否落在視窗內」判定，邊界精確定時，另有 60 秒兜底；'
            '系統時間/時區變更會立即重算。',
        ],
        'h_power': '省電說明',
        'power': [
            '**開啟顯秒**：需要 1 秒級定時器驅動（這是「顯示秒」的固有成本）。實測 SystemUI 單核：'
            '關 7.0%、開 7.25%（60 秒視窗），差異小於切片雜訊。',
            '**關閉顯秒**：**立即停止定時器**（`stopTicker`），沒有任何每秒喚醒與跨程序讀取。',
            '**掉色保色**會自適應退避 2s → 4s → … → 30s，連續 5 分鐘無掉色就自動收工'
            '（`color-guard stopped`）。',
            '狀態為**程序內快取**，定時器不會每秒讀 `Settings.Secure`。',
            '任務兜底使用 `Handler.postDelayed`：**不持有 WakeLock、不用 AlarmManager**，深睡不會被喚醒。',
            '定時器持有視圖**弱引用**，視圖銷毀後自動退出。',
        ],
        'h_build': '建置',
        'build_intro': (
            '需要 **JDK 17+**（建議 17 或 21）與 **Android SDK Platform 35**（Build-Tools 35 由 AGP 自動下載）。'
        ),
        'h_backup': '備份與續開發（重要）',
        'backup_intro': (
            '在 `ClockSeconds/` 目錄中，**除 `build/`、`.gradle/` 外的所有檔案**都要保留（原始碼部分僅約 300 KB）：'
        ),
        'th_backup_cat': '類別',
        'th_backup_files': '檔案',
        'th_backup_note': '說明',
        'backup_rows': [
            ('建置腳本',
             '`settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`、`gradle.properties`、'
             '`keystore.properties`', '版本、依賴、編譯參數、簽章設定'),
            ('Gradle Wrapper', '`gradlew`、`gradlew.bat`、`gradle/wrapper/*`', '鎖定 Gradle 8.14.5'),
            ('原始碼', '`app/src/main/java/**`（20 個 `.java`）', '模組邏輯、設定頁、繪製類別'),
            ('資源', '`app/src/main/res/**`', '字串（22 個語系）、主題'),
            ('Xposed 中繼資料', '`app/src/main/resources/META-INF/xposed/*`', '入口類別、scope、API 版本'),
            ('清單', '`app/src/main/AndroidManifest.xml`', '權限、Activity'),
            ('工具腳本', '`tools/**`', 'README 21 語言產生器、i18n 檢查、發版腳本'),
            ('文件', '`README*.md`、`docs/**`、`CHANGELOG.md`', '說明文件、截圖、更新日誌'),
            ('CI', '`.github/workflows/*.yml`', '打 tag 出簽章包'),
        ],
        'h_trouble': '排查',
        'trouble_summary': '展開排查步驟',
        'trouble_intro': '查看 logcat：',
        'trouble': [
            ('b', '**Release 建置**已用 `BuildConfig.DEBUG` 門控剝除所有診斷日誌，logcat 不會輸出本模組日誌。'),
            ('b', '需要排查時請暫時安裝 **debug 建置**（`assembleDebug`，用同一生產金鑰簽章可直接覆蓋安裝），再過濾：'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', '完全不顯示秒：先確認 LSPosed 已勾選 SystemUI 作用域並重新啟動；再確認開關是否被存成關閉，可讀取或強制寫入：'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=開 0=關',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', '顏色/秒數自己變回去（掉色）：系統或主題正在重建文字，模組會自動重繪；'
                  '若長期不恢復，請在 debug 日誌搜 `color-guard stopped` / `span-theft`。'),
            ('b', '在非 ColorOS 機型上無效：裝 debug 版看 `candidate:` / `CONFIG CLOCK:` 輸出，'
                  '確認該機型時間的類別名與資源名是否符合識別規則。'),
        ],
        'h_feedback': '意見回饋',
        'feedback': '有問題、建議或使用心得？歡迎到 [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) 留言，我會盡快回覆。',
        'h_license': 'License',
    },
}
