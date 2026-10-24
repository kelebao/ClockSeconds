<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · **繁體中文** · [日本語](README_ja.md) · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# 時間顯示秒數（Show Seconds in Time）

讓狀態欄時間顯示**秒**，單擊即切換，小時十位數**常駐標紅**。<br>不依賴任何廠商私有類，已在 ColorOS 15 真機驗證，同時覆蓋小米、一加、榮耀、vivo、三星 等主要廠商。

[![version](https://img.shields.io/badge/version-1.0.9-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## 功能

| 功能 | 說明 |
|------|------|
| 狀態欄顯秒 | 狀態欄時間顯示到秒（`HH:mm:ss` / `h:mm:ss a`，跟隨系統 12/24 小時制） |
| 點擊切換 | **單擊**狀態欄時間即切換顯秒開/關；也可在設定頁改為**雙擊** |
| 小時顏色 | 小時「十位數」顏色可**自訂**（8 色色板）或**關閉**恢復系統原色；單位數小時（如 `9:45`）無十位不標紅。所有廠商機型均生效 |
| 長按預覽浮窗 | **長按**狀態欄時間彈出**毛玻璃半透明浮窗**，即時顯示**網路時間**；可整體拖動、按左下角 ◣ 拉伸縮放、點一下時間浮現按鈕，**關閉**（右上 ✕）/ **拉伸**（左下 ◣）；字級隨視窗大小自适应 |
| 網路時間對比 | 設定頁即時對比**網路標準時間**與本地時間偏差；偏差較大時點**同步**，即可開啟系統「自動設定時間」向網路校準 |
| 免 adb 授權 | 所有設定寫入優先走 `Settings.Secure`，無 `WRITE_SECURE_SETTINGS` 權限時自動改走廣播由 SystemUI 代寫——**無需 adb 授權**即可正常使用全部功能 |
| 介面語言 | 應用內介面已適應 **21 種語言**（跟隨系統語言自動切換）：简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、Ελληνικά、ქართული |
| 持久化 | 狀態存於 `Settings.Secure`，跨程序共享，重啟 SystemUI 後保持 |
| 適應範圍 | ColorOS 15 / 16 / 17（OPPO / 一加 / 真我）已真機驗證；小米 / 榮耀 / 紅米 / 三星 等有 root 的其它廠商同步生效。不依賴任何廠商私有類，避免觸發 LSPosed 安全模式。最低支援 Android 10 |
| 下拉磁貼 | 下拉通知欄新增「時間顯秒」磁貼，一鍵開關顯秒（無需打開設定頁） |
| 浮窗增強 | 浮窗位置與尺寸**自動記憶**（下次長按按記憶恢復）；**雙擊**浮窗時間也可切換顯秒 |
| 省電自適應 | 系統開啟省電模式時自動暫停秒級重新整理（狀態列秒數凍結），退出省電立即恢復 |
| 時間識別名 | 其它廠商機型若未能自動識別狀態列時間，可在設定頁點「自動偵測」一鍵完成；也可手動填寫資源名（如 clock） |

## 安裝

1. 從 [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) 下載最新版 APK
2. 在 **LSPosed** 中啟用本模組，作用域勾選：**系統介面（`com.android.systemui`）**
3. 重啟 SystemUI 程序或重啟設備
4. 設定頁從 **LSPosed 管理器**打開（桌面不生成圖示）

> 需要 root（LSPosed 框架）。寫入 `Settings.Secure` 需授予 `WRITE_SECURE_SETTINGS`：
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 未授予時仍可點擊狀態欄時間完成切換（該動作運行在 SystemUI 程序內，天然具備權限）。

## 使用

- **單擊狀態欄時間**：切換顯秒開/關（預設手勢）。
- 切換透過全域觸控命中判定實現，**不會與「下拉通知欄」衝突**——純點擊切換、下拉滑動照常拉出通知欄；若你更習慣，也可在設定頁把切換手勢改為**雙擊**。
- 設定頁可檢視當前狀態、切換手勢、重新整理狀態。

## 效果預覽

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="關閉顯秒：`15:06`，小時十位「1」保持紅色" width="100%">
<br>
關閉顯秒：`15:06`，小時十位「1」保持紅色
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="開啟顯秒：`15:06:03`，分鐘與秒保持系統原色" width="100%">
<br>
開啟顯秒：`15:06:03`，分鐘與秒保持系統原色
</td>
</tr></table>

## 工作原理

模組只 hook **SystemUI** 一個程序，採用穩妥的 legacy Xposed 範式，
**完全不觸碰任何廠商私有類**，因而不會破壞系統內部狀態、不會觸發 LSPosed 安全模式：

- **顯秒**：hook 穩定的 `TextView.setText`（after）快取系統在狀態欄時間上给出的原始文字，
  再 hook `TextView.onDraw`（before）在系統已設好文字之後覆寫顯示 `原始文字:ss`，
  並用 1 秒級 `Handler` 驅動刷新。追加的秒保持系統時間自身的文字色（實測為白色 `#ccffffff`）。
  同時把「小時十位數」標紅（給該字元追加一段紅色 `ForegroundColorSpan`），無論顯秒開啟或關閉均保持紅色；
  分鐘與秒不標紅；單位數小時無十位則不標紅。
- **切換**：ColorOS 的通知面板會攔截狀態欄區域的觸控，時間自身的 `OnTouchListener` 收不到事件。
  因此模組改為 hook 系統全域的 `View.dispatchTouchEvent`，僅當按下點落在已識別時間的螢幕邊界內、
  且幾乎無位移（點擊）或 350ms 內第二次點擊（雙擊）时才翻轉 `Settings.Secure` 開關；模組**不消費**
  任何觸控事件，下拉通知欄照常。

## 省電說明

- **開啟顯秒時**：模組需要 1 秒級計時器驅動刷新（這是「顯示秒」的固有開銷，約 60 次/分鐘重繪時間）。
- **關閉顯秒時**：模組會**立即停止該計時器**（`stopTicker`），不再有任何每秒喚醒，也不再有跨程序讀取，
  回到系統原本的「每分鐘刷新一次」節奏，因此關閉顯秒確實更省電。
- 狀態採用**程序內快取**（`sSecondsEnabled`），計時器不會每秒去讀 `Settings.Secure`（避免每秒一次 IPC）。
- 模組不構造任何時間格式化器，直接復用系統给出的原始文字並僅追加 `:ss`，因此沒有格式化器開銷。
- 計時器持有的是檢視的**弱引用**，且僅在檢視 `isAttachedToWindow()` 時繼續，檢視銷毀後自動退出。

## 構建

需要 **JDK 17+**（推薦 17 或 21）與 **Android SDK Platform 35**（Build-Tools 35 由 AGP 自動安裝）。

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## 備份與續開發（重要）

`ClockSeconds/` 目錄中，**除 `build/`、`.gradle/` 外的所有檔案**都要保留（源碼部分僅約 140 KB）：

| 類別 | 檔案 | 說明 |
|------|------|------|
| 建置腳本 | `settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`、`gradle.properties` | 版本、相依、編譯參數 |
| Gradle Wrapper | `gradlew`、`gradlew.bat`、`gradle/wrapper/*` | 鎖定 Gradle 8.14.5 |
| 原始碼 | `app/src/main/java/**`（3 個 `.java`） | 模組邏輯主體 |
| 資源 | `app/src/main/res/**` | 字串（預設中文 + 21 種語言 values-xx）、主題 |
| Xposed 中介資料 | `app/src/main/resources/META-INF/xposed/*` | 入口類別、scope、API 版本 |
| 資訊清單 | `app/src/main/AndroidManifest.xml` | 權限、Activity |
| 文件 | `README*.md` | 21 種語言說明文件 |
| CI | `.github/workflows/release.yml` | 打 tag 出簽名包 |

## 排查

<details>
<summary>展開排查步驟</summary>

查看 logcat：

- **Release 建置**已用 `BuildConfig.DEBUG` 門控剝除所有診斷日誌（更小、更乾淨），logcat 中不會輸出本模組日誌。
- 如需排查，暫時安裝 **debug 建置**（`assembleDebug`，用同一生產金鑰簽名可直接覆蓋安裝），再過濾 TAG：

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- 若完全不顯示秒：先確認 LSPosed 已勾選 SystemUI 作用域並重啟；再檢查開關是否被持久化為關閉——
  可在 adb 中讀取 / 強制寫入：

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=開 0=關
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- 若在非 ColorOS 機型上不生效：裝 debug 版看日誌裡的 `candidate:` / `CONFIG CLOCK:` 輸出，
  確認該機型狀態欄時間的資源名與類名是否在識別規則內，若不在則補一條規則即可。

</details>

## 反饋與建議

有問題、建議或使用心得？歡迎到 [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) 留言，我會盡快回覆。

## License

[MIT](LICENSE) © kelebao
