<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · **日本語** · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# 時刻に秒を表示（Show Seconds in Time）

ステータスバーの時刻に**秒**を表示し、タップで切り替えます。**桁ごとに色分け**も、文字列全体に**グラデーション**も可能です。<br>**長押し**で**ネットワーク時刻**を表示するすりガラス風ウィンドウ（ドラッグ・サイズ変更・グラデーション対応）。**カスタムタスク**で定時に秒を自動でオン。<br>コア処理は AOSP 公開 API のみ（OPPO / OnePlus / realme の非公開クラスは任意の追加対応で、無ければスキップ）。ColorOS 15 / 16 で実機検証済み。

[![version](https://img.shields.io/badge/version-1.2.0-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015%2F16-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## 機能

| 機能 | 説明 |
|------|------|
| ステータスバーの秒表示 | `HH:mm:ss` / `h:mm:ss a`、システムの 12/24 時間表示とタイムゾーンに追従。テーマモジュールによる色 span の剥がれを防ぐため末尾に**ゼロ幅スペース**を追加し、色が落ちたら自動で再描画（5 分クールダウン）。 |
| タップで切替 | 時刻を**タップ**で秒のオン/オフ（**ダブルタップ**も選択可）。**長押し**はフローティングプレビュー専用。タスク時間帯のタップは「タスクが引き継いでいます」と表示。 |
| 桁ごとの色 / グラデーション | 6 桁を**それぞれ配色**（10 色プリセット + HSV スライダー + システム追従）、コロンは光学的に中央寄せ。または**グラデーション**（開始色 → 終了色、6 プリセット、両端カスタム、明るい背景では自動で暗く）。プレビューは**自前描画**でステータスバーと一致します。 |
| 長押しプレビュー | すりガラス風ウィンドウに**ネットワーク時刻**を表示。ドラッグで移動、◣ でサイズ変更、位置とサイズは**記憶**。グラデーション対応、数字は等幅。 |
| カスタムタスク | **毎時**または**毎日**の時間帯を設定：N 分前に秒をオン、M 分後に元へ戻す。システム時刻やタイムゾーンの変更は即再計算、さらに 60 秒の保険付き。 |
| ネットワーク時刻の比較 | **ネットワーク時刻**との差を表示。**同期**を押すとシステムの「自動設定」を有効にします。 |
| adb 権限は不要 | 書き込みは `Settings.Secure` を優先し、`WRITE_SECURE_SETTINGS` が無ければブロードキャストで SystemUI が代行——**どの機能も adb コマンドは不要**です。 |
| クイック設定タイル | 通知シェードに「秒を表示」タイルを追加し、アプリを開かずに切り替えられます。 |
| 時計のリソース名 | 自動検出できない場合は**自動検出**をタップ、またはリソース名（例 `clock`）を手入力。 |
| UI は 21 言語 | システム言語に追従：简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、Ελληνικά、ქართული。それ以外は英語にフォールバック。 |
| 永続化 / 対応範囲 | 状態は `Settings.Secure` に保存され、プロセス間で共有、SystemUI や端末の再起動後も保持。**ColorOS 15 / 16（Android 15 / 16）で実機検証済み**。コアは公開 API のみなので他社でも動作する見込みですが、実機は OPPO / OnePlus のみ。Android 10 以上。 |

## カラーモード

色の付け方は 2 通り。「数字の色」カード内で切替：**桁ごと**（1 桁 1 色）と**グラデーション**（文字列全体）。

<table><tr>
<td align="center" width="50%">
<img src="screenshots/colors-digit.png" alt="桁ごと：6 桁それぞれに色、コロンは光学的に中央" width="100%">
<br>
桁ごと：6 桁それぞれに色、コロンは光学的に中央
</td>
<td align="center" width="50%">
<img src="screenshots/colors-gradient.png" alt="グラデーション：開始色 → 終了色、6 プリセット / 両端カスタム" width="100%">
<br>
グラデーション：開始色 → 終了色、6 プリセット / 両端カスタム
</td>
</tr></table>

## インストール

1. [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) から最新 APK をダウンロード
2. **LSPosed** で有効化し、スコープを **システム UI**（`com.android.systemui`）に設定
3. SystemUI を再起動、または端末を再起動
4. 設定画面は **LSPosed マネージャー**から開く（ランチャーアイコンは作成されません）

> root（LSPosed）が必要です。**通常 adb 権限は不要**：書き込みは `Settings.Secure` を優先し、`WRITE_SECURE_SETTINGS` が無ければ SystemUI が代行します。設定画面から**直接書き込み**たい場合は付与してください：
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 付与しなくても利用に支障はありません。タップ、タスク、フローティングプレビューはすべて SystemUI 内部で完結します。

## 使い方

- **ステータスバーの時刻をタップ**：秒のオン/オフ切替（既定の操作）。設定で**ダブルタップ**にも変更可。
- **時刻を長押し**：**ネットワーク時刻**のすりガラスウィンドウ。ドラッグで移動、時刻をタップすると**閉じる**（右上）と**サイズ変更**（左下）ボタンが出ます。
- **数字の色**カードは 2 ページ：「桁ごと」は 6 桁を個別に配色、「グラデーション」は文字列全体に色帯（タブ自体がスイッチ）。
- **カスタムタスク**カードでは「毎時 2 分前に秒をオン」などを設定できます。
- 切替はグローバルなタッチ判定なので、**通知パネルの引き下げと競合しません**。
- 設定画面では現在の状態、ジェスチャ、配色、ネットワーク時刻の差、ワンタップ**同期**を確認できます。

## プレビュー

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="秒オフ：`15:06`、時（10 の位）は選択色のまま" width="100%">
<br>
秒オフ：`15:06`、時（10 の位）は選択色のまま
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="秒オン：`15:06:03`、分と秒はシステム色のまま" width="100%">
<br>
秒オン：`15:06:03`、分と秒はシステム色のまま
</td>
</tr></table>

## 仕組み

フックするのは **SystemUI のみ**、堅実な legacy Xposed API を使っています。
**コアは AOSP 公開 API のみ、ベンダー非公開クラスは任意で無ければスキップ**するため、システム状態を壊さず LSPosed のセーフモードも誘発しません：

- **秒**：`TextView.setText`（after）でシステムの原文をキャッシュし、`TextView.onDraw`（before）で`原文:ss` を上書き、1 秒周期の `Handler` で更新。
- **桁ごとの色**：1 桁ごとに `ForegroundColorSpan`（外部 span は先に除去）。コロンには `CharacterStyle` で 0.11em 持ち上げ（複数 run 描画ではホスト字形が下がるため）。
- **グラデーション**：`CharacterStyle` の `updateDrawState` で `setShader`。設定画面のプレビューは**自前描画**（`GradientTimeView`）。ColorOS 15 は shader を描画 run 単位で量子化するためです。
- **切替**：通知パネルがステータスバーのタッチを奪うため、グローバルな `View.dispatchTouchEvent` をフックし、時刻の範囲内でほぼ動かない押下のときだけスイッチを反転。イベントは**消費しません**。
- **タスク**：「ローカル時刻が範囲内か」で判定し、境界は正確なタイマー + 60 秒の保険。時刻やタイムゾーンの変更で即再計算します。

## 消費電力

- **秒オン**：1 秒周期のタイマーが必要（秒表示の本質的なコスト）。実測 SystemUI 単一コア：オフ 7.0%、オン 7.25%（60 秒窓）で、スライスごとのばらつき以下。
- **秒オフ**：タイマーを**即停止**（`stopTicker`）。毎秒の起床もプロセス間読み取りもありません。
- **色の保持**は 2s → 4s → … → 30s と間隔を広げ、5 分間剥がれなければ停止（`color-guard stopped`）。
- 状態は**プロセス内キャッシュ**なので、毎秒 `Settings.Secure` を読むことはありません。
- タスクの保険は `Handler.postDelayed`：**WakeLock も AlarmManager も使わない**ため深いスリープを起こしません。
- タイマーはビューの**弱参照**を保持し、デタッチで終了します。

## ビルド

**JDK 17+**（17 または 21 推奨）と **Android SDK Platform 35** が必要です（Build-Tools 35 は AGP が取得します）。

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## バックアップと開発の継続（重要）

`ClockSeconds/` の中で **`build/` と `.gradle/` を除くすべて**を残してください（ソース部分は約 300 KB）：

| 分類 | ファイル | 説明 |
|------|------|------|
| ビルドスクリプト | `settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`、`gradle.properties`、`keystore.properties` | バージョン、依存、コンパイル設定、署名 |
| Gradle Wrapper | `gradlew`、`gradlew.bat`、`gradle/wrapper/*` | Gradle 8.14.5 を固定 |
| ソース | `app/src/main/java/**`（`.java` 20 個） | モジュール本体、設定画面、描画クラス |
| リソース | `app/src/main/res/**` | 文字列（22 ロケール）、テーマ |
| Xposed メタデータ | `app/src/main/resources/META-INF/xposed/*` | エントリクラス、スコープ、API 版 |
| マニフェスト | `app/src/main/AndroidManifest.xml` | 権限、アクティビティ |
| ツール | `tools/**` | README 生成、i18n チェック、リリース用スクリプト |
| ドキュメント | `README*.md`、`docs/**`、`CHANGELOG.md` | 説明、スクリーンショット、変更履歴 |
| CI | `.github/workflows/*.yml` | タグで署名付きビルド |

## トラブルシューティング

<details>
<summary>手順を開く</summary>

logcat を確認：

- **リリースビルド**は `BuildConfig.DEBUG` で診断ログをすべて除去しているため、logcat には何も出ません。
- 調査には **debug ビルド**（`assembleDebug`、同じ本番キーで署名されるので上書きインストール可能）を入れ、次で絞り込みます：

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- 秒が全く出ない：LSPosed のスコープに System UI があるか確認して再起動。次にスイッチの保存値を確認・強制：

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=on 0=off
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- 色や秒が勝手に戻る：システムやテーマがテキストを再構築しており、モジュールが再描画します。回復しない場合は debug ログで `color-guard stopped` / `span-theft` を検索してください。
- ColorOS 以外で動かない：debug ビルドで `candidate:` / `CONFIG CLOCK:` を確認し、時計のクラス名・リソース名が検出規則に合うか見てください。

</details>

## フィードバック

質問・提案・報告は [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) へどうぞ。

## License

[MIT](LICENSE) © kelebao
