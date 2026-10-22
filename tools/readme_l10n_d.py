# -*- coding: utf-8 -*-
"""Slot translations for README generation: 日本語 (ja)."""

TEXTS = {
    'ja': {
        'title': '時刻の秒表示（Show Seconds in Time）',
        'tagline': (
            'ステータスバーの時刻に**秒**を表示。ワンタップで切り替え、'
            '時の十の位は**常に赤**。<br>'
            'ベンダー固有クラスには一切依存せず、ColorOS 15 の実機で検証済み。'
            'Xiaomi・OnePlus・Honor・vivo など主要メーカーにも対応。'
        ),
        'h_features': '機能',
        'th_feature': '機能',
        'th_desc': '説明',
        'rows': [
            ('秒表示',
             'ステータスバーの時刻が秒まで表示（`HH:mm:ss` / `h:mm:ss a`、システムの 12/24 時間制に追随）'),
            ('タップで切替',
             'ステータスバーの時刻を**タップ**するだけで秒表示の ON/OFF を切替。設定ページで**ダブルタップ**にも変更可能'),
            ('時の桁の色',
             '時の「十の位」の色は**カスタマイズ可能**（8 色パレット）または**オフ**で'
             'システム本来の色に戻せます。一桁の時（`9:45` など）は十の位がないため非対象。'
             '全メーカーで有効'),
            ('長押しプレビュー',
             '**長押し**でステータスバーの時刻から**すりガラス風の浮動ウィンドウ**が開き、'
             '**ネットワーク時刻**をリアルタイム表示。ドラッグで移動、左下 ◣ でリサイズ、'
             '時刻をタップすると**閉じる**（右上 ✕）/ **リサイズ**（左下 ◣）ボタンが表示されます。'
             '文字サイズはウィンドウに自動追従'),
            ('ネットワーク時刻',
             '設定ページで**ネットワーク標準時刻**との偏差をリアルタイム比較。'
             '偏差が大きい場合は**同期**をタップするとシステムの「時刻自動設定」がオンになります'),
            ('adb 不要',
             '設定は `Settings.Secure` へ書き込み。`WRITE_SECURE_SETTINGS` がない場合は'
             '自動的にブロードキャストにフォールバックし SystemUI が代行——**adb 承認は不要**です'),
            ('UI 言語',
             'アプリ UI は **21 言語**に対応（システム言語に自動追従）：'
             '简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、'
             'Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、'
             'Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、'
             'Ελληνικά、ქართული'),
            ('永続化',
             '状態は `Settings.Secure` に保存。プロセス間で共有され、SystemUI 再起動後も維持'),
            ('対応範囲',
             'ColorOS 15 / 16 / 17（OPPO / OnePlus / realme）は実機検証済み。'
             'Xiaomi / Honor / Redmi など root 取得済みの他メーカーでもそのまま動作。'
             'ベンダー固有クラスに依存しないため LSPosed セーフモードは誘発しません。最低 Android 10'),
        ],
        'h_install': 'インストール',
        'install': [
            '[**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) から最新の APK をダウンロード',
            '**LSPosed** でモジュールを有効化。スコープは**システム UI（`com.android.systemui`）**',
            'SystemUI プロセスを再起動、または端末を再起動',
            '設定ページは **LSPosed マネージャー**から開きます（ランチャーアイコンは作成されません）',
        ],
        'note1': (
            'root（LSPosed）が必要です。`Settings.Secure` への書き込みには '
            '`WRITE_SECURE_SETTINGS` の授予が必要です：'
        ),
        'note2': (
            '未授予でもステータスバーの時刻をタップすれば切替できます'
            '（その操作は SystemUI プロセス内で実行され、元から権限を持ちます）。'
        ),
        'h_usage': '使い方',
        'usage': [
            '**ステータスバーの時刻をタップ**：秒表示の ON/OFF を切替（デフォルトのジェスチャー）。',
            '切替はグローバルなタッチ命中判定で実装しているため、**「通知シェードを下ろす」操作と衝突しません**——'
            '純粋なタップでは切替、スワイプでは従来どおり通知シェードが下ります。'
            '設定ページで**ダブルタップ**に変更することもできます。',
            '設定ページでは現在の状態・ジェスチャー設定の確認と再読みができます。',
        ],
        'h_demo': 'プレビュー',
        'cap_off': '秒表示オフ：`15:06`、時の十の位「1」は赤のまま',
        'cap_on': '秒表示オン：`15:06:03`、分と秒はシステム既定色',
        'h_how': '動作の仕組み',
        'how_intro': [
            'モジュールが hook するのは **SystemUI** のみ。実績のある legacy Xposed の手法を用い、',
            '**ベンダー固有クラスには一切触れない**ため、システム内部状態を壊すことも '
            'LSPosed セーフモードを誘発することもありません：',
        ],
        'how': [
            '**秒表示**：安定した `TextView.setText`（after）を hook してシステムがステータスバーの時刻に'
            '設定した素の文字列をキャッシュし、\n'
            '次に `TextView.onDraw`（before）を hook して、システムが文字列を設定した後に表示を '
            '`素の文字列:ss` で上書きします。\n'
            '1 秒周期の `Handler` で駆動。追加する秒は時刻本来の文字色を継承します（実測 `#ccffffff`）。\n'
            'あわせて時の十の位を赤くします（その 1 文字に赤の `ForegroundColorSpan` を付加）。'
            '秒表示の ON/OFF にかかわらず常に赤；\n'
            '分と秒は赤くしません。一桁の時は十の位がないため非対象。',
            '**切替**：ColorOS の通知パネルはステータスバー領域のタッチを横取りするため、'
            '時刻自身の `OnTouchListener` にはイベントが届きません。\n'
            'そこでモジュールはシステムグローバルな `View.dispatchTouchEvent` を hook し、'
            '認識済みの時刻の画面境界内に押下点があり、\n'
            'かつほぼ移動がない（タップ）か 350ms 以内の 2 回目タップ（ダブルタップ）の場合にのみ `Settings.Secure` の'
            'スイッチを反転させます。モジュールはタッチイベントを\n'
            '**消費しない**ため、通知シェードを下ろす操作は従来どおり使えます。',
        ],
        'h_power': '省電力について',
        'power': [
            '**秒表示 ON**：モジュールは 1 秒周期のタイマーで描画を駆動します'
            '（秒表示の固有コストで、時刻の再描画は毎分約 60 回）。',
            '**秒表示 OFF**：モジュールはそのタイマーを**即座に停止**（`stopTicker`）します。'
            '毎秒のウェイクアップもプロセス間読み出しも一切なくなります。\n'
            'システム本来の「1 分に 1 回更新」のリズムに戻るため、秒表示を切ると確実に省電力になります。',
            '状態は**プロセス内キャッシュ**（`sSecondsEnabled`）で保持。'
            'タイマーが毎秒 `Settings.Secure` を読むことはありません（毎秒 1 回の IPC を回避）。',
            '時刻フォーマッタは一切生成しません。システムの素の文字列を再利用し `:ss` を付加する'
            'だけなので、フォーマッタのオーバーヘッドはゼロです。',
            'タイマーが持つのはビューの**弱参照**のみで、ビューが `isAttachedToWindow()` の間だけ'
            '継続。ビュー破壊後は自動的に終了します。',
        ],
        'h_build': 'ビルド',
        'build_intro': (
            '**JDK 17+**（17 または 21 を推奨）と **Android SDK Platform 35**'
            '（Build-Tools 35 は AGP が自動インストール）が必要です。'
        ),
        'h_backup': 'バックアップと開発の引継ぎ（重要）',
        'backup_intro': (
            '`ClockSeconds/` ディレクトリでは、**`build/`・`.gradle/` を除く'
            'すべてのファイル**を残してください（ソース部分は約 140 KB のみ）：'
        ),
        'th_backup_cat': 'カテゴリ',
        'th_backup_files': 'ファイル',
        'th_backup_note': '説明',
        'backup_rows': [
            ('ビルドスクリプト',
             '`settings.gradle.kts`・`build.gradle.kts`・`app/build.gradle.kts`・`gradle.properties`',
             'バージョン・依存関係・コンパイル設定'),
            ('Gradle Wrapper', '`gradlew`・`gradlew.bat`・`gradle/wrapper/*`', 'Gradle 8.14.5 を固定'),
            ('ソースコード', '`app/src/main/java/**`（`.java` 3 件）', 'モジュール論理の本体'),
            ('リソース', '`app/src/main/res/**`',
             '文字列（デフォルト中国語 + 21 言語 values-xx）、テーマ'),
            ('Xposed メタデータ', '`app/src/main/resources/META-INF/xposed/*`',
             'エントリクラス・scope・API バージョン'),
            ('マニフェスト', '`app/src/main/AndroidManifest.xml`', '権限、Activity'),
            ('ドキュメント', '`README*.md`', '21 言語の説明'),
            ('CI', '`.github/workflows/release.yml`', 'tag 推送で署名ビルド'),
        ],
        'h_trouble': 'トラブルシューティング',
        'trouble_summary': '調査手順を展開',
        'trouble_intro': 'logcat を確認：',
        'trouble': [
            ('b', '**Release ビルド**は `BuildConfig.DEBUG` ゲートですべての診断ログを'
                  '除去済み（小さく・軽量）。logcat に本モジュールのログは出力されません。'),
            ('b', '調査する場合は、一時的に **debug ビルド**（`assembleDebug`。'
                  '本番相同的鍵で署名済みなのでそのまま上書きインストールできます）を入れ、'
                  'TAG で絞り込みます：'),
            ('code', ['adb logcat -s ColorOSClockSeconds']),
            ('b', '秒がまったく表示されない場合：まず LSPosed で SystemUI スコープに'
                  'チェック入り再起動したか確認。次にスイッチが OFF のまま永続化して'
                  'いないか、adb で読み取り／強制書き込み：'),
            ('code', [
                'adb shell settings get secure coloros_clock_seconds_enabled   # 1=ON 0=OFF',
                'adb shell settings put  secure coloros_clock_seconds_enabled 1',
            ]),
            ('b', '非 ColorOS 端末で効かない場合：debug ビルドを入れ、ログの '
                  '`candidate:` / `CONFIG CLOCK:` 出力を見て、その端末のステータスバー'
                  '時刻のリソース名・クラス名が認識規則に含まれているか確認。'
                  '含まれていなければ規則を 1 行追加するだけです。'),
        ],
        'h_feedback': 'フィードバック',
        'feedback': '不具合・ご要望・ご感想があれば、[GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) へお気軽にどうぞ。できる限り早く対応します。',
        'h_license': 'License',
    },
}
