<div align="center">

[简体中文](../README.md) · [English](README_EN.md) · [繁體中文](README_zh-TW.md) · **日本語** · [한국어](README_ko.md) · [Türkçe](README_tr.md) · [Русский](README_ru.md) · [Tiếng Việt](README_vi.md) · [ไทย](README_th.md) · [Deutsch](README_de.md) · [Español](README_es.md) · [Français](README_fr.md) · [Українська](README_uk.md) · [Polski](README_pl.md) · [Português (Brasil)](README_pt-BR.md) · [فارسی](README_fa.md) · [العربية](README_ar.md) · [Bahasa Indonesia](README_id.md) · [Italiano](README_it.md) · [Ελληνικά](README_el.md) · [ქართული](README_ka.md)

</div>

<div align="center">

# 時計の秒表示（Clock Seconds）

ステータスバーの時計に**秒**を表示。ワンタップで切り替え、時の十の位は**常に赤**。<br>ベンダー固有クラスには一切依存せず、ColorOS 15 の実機で検証済み。Xiaomi・OnePlus・Honor・vivo など主要メーカーにも対応。

[![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## 機能

| 機能 | 説明 |
|------|------|
| 秒表示 | ステータスバーの時計が秒まで表示（`HH:mm:ss` / `h:mm:ss a`、システムの 12/24 時間制に追随） |
| タップで切替 | ステータスバーの時計を**タップ**するだけで秒表示の ON/OFF を切替。設定ページで**長押し**にも変更可能 |
| 時の桁を赤に | 時の「十の位」を赤くします（`13` の `1`、`05` の `0`）。分と秒はシステム本来の色のまま。一桁の時（`9:45` など）は十の位がないため非対象。全メーカーで有効 |
| UI 言語 | アプリ UI は **21 言語**に対応（システム言語に自動追従）：简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、Ελληνικά、ქართული |
| 永続化 | 状態は `Settings.Secure` に保存。プロセス間で共有され、SystemUI 再起動後も維持 |
| 対応範囲 | ColorOS 15 / 16 / 17（OPPO / OnePlus / realme）は実機検証済み。Xiaomi / Honor / Redmi など root 取得済みの他メーカーでもそのまま動作。ベンダー固有クラスに依存しないため LSPosed セーフモードは誘発しません。最低 Android 10 |

## インストール

1. [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) から最新の APK をダウンロード
2. **LSPosed** でモジュールを有効化。スコープは**システム UI（`com.android.systemui`）**
3. SystemUI プロセスを再起動、または端末を再起動
4. 設定ページは **LSPosed マネージャー**から開きます（ランチャーアイコンは作成されません）

> root（LSPosed）が必要です。`Settings.Secure` への書き込みには `WRITE_SECURE_SETTINGS` の授予が必要です：
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 未授予でもステータスバーの時計をタップすれば切替できます（その操作は SystemUI プロセス内で実行され、元から権限を持ちます）。

## 使い方

- **ステータスバーの時計をタップ**：秒表示の ON/OFF を切替（デフォルトのジェスチャー）。
- 切替はグローバルなタッチ命中判定で実装しているため、**「通知シェードを下ろす」操作と衝突しません**——純粋なタップでは切替、スワイプでは従来どおり通知シェードが下ります。設定ページで**長押し**に変更することもできます。
- 設定ページでは現在の状態・ジェスチャー設定の確認と再読みができます。

## プレビュー

<table><tr>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-off.jpg" alt="秒表示オフ：`15:06`、時の十の位「1」は赤のまま" width="100%">
<br>
秒表示オフ：`15:06`、時の十の位「1」は赤のまま
</td>
<td align="center" width="50%">
<img src="screenshots/statusbar-seconds-on.jpg" alt="秒表示オン：`15:06:03`、分と秒はシステム既定色" width="100%">
<br>
秒表示オン：`15:06:03`、分と秒はシステム既定色
</td>
</tr></table>

## 動作の仕組み

モジュールが hook するのは **SystemUI** のみ。実績のある legacy Xposed の手法を用い、
**ベンダー固有クラスには一切触れない**ため、システム内部状態を壊すことも LSPosed セーフモードを誘発することもありません：

- **秒表示**：安定した `TextView.setText`（after）を hook してシステムがステータスバーの時計に設定した素の文字列をキャッシュし、
  次に `TextView.onDraw`（before）を hook して、システムが文字列を設定した後に表示を `素の文字列:ss` で上書きします。
  1 秒周期の `Handler` で駆動。追加する秒は時計本来の文字色を継承します（実測 `#ccffffff`）。
  あわせて時の十の位を赤くします（その 1 文字に赤の `ForegroundColorSpan` を付加）。秒表示の ON/OFF にかかわらず常に赤；
  分と秒は赤くしません。一桁の時は十の位がないため非対象。
- **切替**：ColorOS の通知パネルはステータスバー領域のタッチを横取りするため、時計自身の `OnTouchListener` にはイベントが届きません。
  そこでモジュールはシステムグローバルな `View.dispatchTouchEvent` を hook し、認識済みの時計の画面境界内に押下点があり、
  かつほぼ移動がない（タップ）か 500ms 長押しされた場合にのみ `Settings.Secure` のスイッチを反転させます。モジュールはタッチイベントを
  **消費しない**ため、通知シェードを下ろす操作は従来どおり使えます。

## 省電力について

- **秒表示 ON**：モジュールは 1 秒周期のタイマーで描画を駆動します（秒表示の固有コストで、時計の再描画は毎分約 60 回）。
- **秒表示 OFF**：モジュールはそのタイマーを**即座に停止**（`stopTicker`）します。毎秒のウェイクアップもプロセス間読み出しも一切なくなります。
  システム本来の「1 分に 1 回更新」のリズムに戻るため、秒表示を切ると確実に省電力になります。
- 状態は**プロセス内キャッシュ**（`sSecondsEnabled`）で保持。タイマーが毎秒 `Settings.Secure` を読むことはありません（毎秒 1 回の IPC を回避）。
- 時刻フォーマッタは一切生成しません。システムの素の文字列を再利用し `:ss` を付加するだけなので、フォーマッタのオーバーヘッドはゼロです。
- タイマーが持つのはビューの**弱参照**のみで、ビューが `isAttachedToWindow()` の間だけ継続。ビュー破壊後は自動的に終了します。

## ビルド

**JDK 17+**（17 または 21 を推奨）と **Android SDK Platform 35**（Build-Tools 35 は AGP が自動インストール）が必要です。

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## バックアップと開発の引継ぎ（重要）

`ClockSeconds/` ディレクトリでは、**`build/`・`.gradle/` を除くすべてのファイル**を残してください（ソース部分は約 140 KB のみ）：

| カテゴリ | ファイル | 説明 |
|------|------|------|
| ビルドスクリプト | `settings.gradle.kts`・`build.gradle.kts`・`app/build.gradle.kts`・`gradle.properties` | バージョン・依存関係・コンパイル設定 |
| Gradle Wrapper | `gradlew`・`gradlew.bat`・`gradle/wrapper/*` | Gradle 8.14.5 を固定 |
| ソースコード | `app/src/main/java/**`（`.java` 3 件） | モジュール論理の本体 |
| リソース | `app/src/main/res/**` | 文字列（デフォルト中国語 + 21 言語 values-xx）、テーマ |
| Xposed メタデータ | `app/src/main/resources/META-INF/xposed/*` | エントリクラス・scope・API バージョン |
| マニフェスト | `app/src/main/AndroidManifest.xml` | 権限、Activity |
| ドキュメント | `README*.md` | 21 言語の説明 |
| CI | `.github/workflows/release.yml` | tag 推送で署名ビルド |

## トラブルシューティング

<details>
<summary>調査手順を展開</summary>

logcat を確認：

- **Release ビルド**は `BuildConfig.DEBUG` ゲートですべての診断ログを除去済み（小さく・軽量）。logcat に本モジュールのログは出力されません。
- 調査する場合は、一時的に **debug ビルド**（`assembleDebug`。本番相同的鍵で署名済みなのでそのまま上書きインストールできます）を入れ、TAG で絞り込みます：

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- 秒がまったく表示されない場合：まず LSPosed で SystemUI スコープにチェック入り再起動したか確認。次にスイッチが OFF のまま永続化していないか、adb で読み取り／強制書き込み：

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=ON 0=OFF
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- 非 ColorOS 端末で効かない場合：debug ビルドを入れ、ログの `candidate:` / `CONFIG CLOCK:` 出力を見て、その端末のステータスバー時計のリソース名・クラス名が認識規則に含まれているか確認。含まれていなければ規則を 1 行追加するだけです。

</details>

## フィードバック

不具合・ご要望・ご感想があれば、[GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) へお気軽にどうぞ。できる限り早く対応します。

## License

[MIT](LICENSE) © kelebao
