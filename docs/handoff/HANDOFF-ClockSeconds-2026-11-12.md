# ClockSeconds 项目交接单

> 日期 2026-11-12｜生成者：DSH 会话「状态栏时间显秒」｜版本 1
> **唯一交付物，完全自包含**：不引用任何外部模板、文档或「某节」，规则／门禁／命令／路径／指标全部展开写在正文。纯 Markdown、零依赖、零安装，不需要插件、技能或平台专属语法；整份拷走或单独发给别人／另一个 AI 平台都能直接开工。


**按「用法 B：同机换会话」填写**——由会话体检插件触发，老会话命中 danger 红灯（附录 A1），下一步在本机新开会话续做，故全文绝对路径、附录 A 三节填满。**若用于换机／换平台**：把第 2 节起的绝对路径改成相对 `<项目根>`，跳过附录 A，其余原样可用；第 3 节右列已写明新机每项怎么装、怎么验证。

## 执行协议（接手者必读）

1. **读**完只做一件事：复述第 2 节项目根、第 16 节第一步、以及文档中缺失或矛盾处，等用户确认。
2. **空跑验证**：只跑第 4 节冒烟命令 + 第 5 节验证命令，**一行代码都不改**；跑不过报告差异，不要猜着修。
3. **确认差异**：把「文档说的」与「实际看到的」逐条列成表交给用户；以代码为准并**报告冲突**。
4. **开工**：按第 16 节做；改前先说改哪几个文件，改完给验证命令与实测结果。

三条自动生效：空跑通过前不许改代码；第 14.1 节敏感操作每次先说明确切命令、得到明确同意才跑，拒绝就停、不换姿势绕过；每次交接在附录 B 追加一行，不新建 `HANDOFF-新.md` 覆盖旧的。

目录：0 怎么用｜1 名片｜2 根与路径｜3 环境｜4 从零跑｜5 验证门禁｜6 验收+6.1 空跑｜7 架构｜8 状态｜9 坏掉／半成品｜10 不做｜11 决策｜12 坑｜13 回滚｜14 安全｜15 约定｜16 第一步｜17 术语｜18 自检｜附录 A｜附录 B

## 0. 怎么用

1. 本文件在 `D:\MyApp\ClockSeconds\docs\handoff\`；引用的截图、日志、脚本、安装包都在本项目或本机固定位置，第 2 节逐条列了「不随仓库拷贝的东西」及补救办法。
2. 交给新会话／新平台：上传、粘贴全文或平台自带引用，任选其一。
3. 开场指令可直接复制：
   > 请先完整读完这份《交接单》再开始工作。以文档内容为准；不要假设我已安装任何插件、技能或工具；凡是文档没写清楚的环境或路径，先问我再动手。读完先复述「第 2 节 项目根与路径」和「第 16 节 第一步做什么」，并列出你认为文档里缺失或矛盾的地方，确认无误后等我指令。
4. 本机新会话把 cwd 指到 `D:\MyApp\ClockSeconds`（老会话 cwd 是 `D:\MyApp`，别沿用）。

## 1. 项目名片

| 项 | 值 |
|---|---|
| 项目 | ClockSeconds（仓库 ColorOSClockSeconds，remote `https://github.com/kelebao/ClockSeconds.git`） |
| 目标 | 给 ColorOS／HyperOS 状态栏时钟**显示秒数**并**逐位／渐变上色**的 LSPosed 模块；不自建时钟 View，只 hook 系统已有 TextView |
| 阶段/版本 | 测试线 `versionCode=323` / `1.2.0-test.20261102-1500`；正式发布线停在 v1.2.0（vc249）。双 flavor：legacy（Xposed API 82）、modern（libxposed API 102，包名带 `.modern`） |
| 当前 commit | `8fc8631d494535adbd94cbc19315055f984de271`（vc323 悬浮窗垂直居中），main，已 push，工作区干净 |
| 日期/文件 | 2026-11-12 / `HANDOFF-ClockSeconds-2026-11-12.md` |
| 交接原因 | 老会话 52.7 MB／78160 事件，会话体检 danger 红灯 |
| 原→新环境 | 本机 Win11 + DSH → 本机新会话（用法 B）；换机场景见文首 |
| 负责人 | 用户本人（GitHub `kelebao`）；技术决策、发版、真机操作一律问他 |

## 2. 项目根与路径约定

- **项目根（绝对基准）**：`D:\MyApp\ClockSeconds`；所有命令先 `cd /d D:\MyApp\ClockSeconds`（Git Bash 写法）。

| 绝对路径 | 内容 | 谁读 |
|---|---|---|
| `D:\MyApp\ClockSeconds\app\src\main\java\io\github\kelebao\colorosclockseconds\` | `hook\ClockSecondsModule.java`（5039 行：全部 hook、拼秒、上色、渐变、任务调度、悬浮窗、掉色重补）；`settings\ModuleSettingsActivity.java`（7011 行，纯代码构建设置页 UI）；`ClockSecondsConfig.java`（937 行，Secure 键常量、`adaptColorForBackground` 唯一实现）；`ClockTask.java`（任务模型/JSON）；`GradientTextSpan.java`、`ColonLiftSpan.java`、`ArcTickDrawable.java` | 编译进两 flavor，改功能基本只动第一个 |
| `D:\MyApp\ClockSeconds\app\src\legacy\` / `\app\src\modern\` | legacy：四个 xposed meta-data + `assets\xposed_init`、`xposed_scope`；modern：`META-INF\xposed\{java_init.list,module.prop,scope.list}` + `de.robv.android.xposed.*` shim + `ModernEntry` | 两框架入口 |
| `D:\MyApp\ClockSeconds\app\src\main\res\values*\strings.xml` | 22 语言目录、155 键 | 全部文案 |
| `D:\MyApp\ClockSeconds\tools\` | `gen_readme_i18n.py`、`readme_l10n_a..u.py`（21 语言源）、`check_i18n.py`（门禁） | 发版／加文案 |
| `D:\MyApp\ClockSeconds\` 下 `docs\`、`CHANGELOG.md`、`deliverables\`、`反馈日志\`、`.secrets\` | 21 语言 README／发布说明；倒序版本记录 vc37~vc323；历史 APK；用户回传 LSPosed 日志包；`coloros-clock-release.p12` + `keystore.properties` | 文档／交付／排障／签名 |
| `D:\MyApp\cs_shots\`（**项目外**） | `_b287.cmd`（debug+单测）、`_rel290.cmd`（release）、`_bNNN.txt` 日志、`*.png` 真机截图与像素分析脚本 | 本机开发专用 |

- **附件**：项目根 `ClockSeconds-vc323-legacy-1.2.0-test.20261102-1500.apk`（771941 B）、`ClockSeconds-vc323-modern-api102-…apk`（779422 B），实测可装；截图 `D:\MyApp\cs_shots\box2.png`（浮窗 560×224 居中）、`box4.png`（762×545 缩放）、`cmp321.png`（图标抖动对比）、`s7.png`／`s8.png`（多套渐变）、`f4_c2.png`（浮窗连续渐变）；日志 `反馈日志\LSPosed_20261102_005602\`、`反馈日志\澎湃os3\`。
- **不随仓库拷贝的东西**（是什么 → 怎么补 → 不补会怎样）：
  1. `.secrets\` 密钥库与四个 `RELEASE_*` 明文值 → 整份文件夹拷贝可保留；只 clone 则向负责人索取放回，或改用 `_b287.cmd` 同名变量 → 不补则 `assembleRelease` 不签名（`hasReleaseSigning=false`），debug 不受影响。
  2. `D:\MyApp\cs_shots\` 脚本与截图 → 本机调试产物，用第 4 节等价命令替代 → 只影响一键脚本。
  3. Android SDK `D:\Android\Sdk`（build-tools 36.0.0 `apksigner.bat`、platform-tools `adb.exe`）→ 装 SDK 后改 `local.properties` 的 `sdk.dir` → 不补报 `SDK location not found`。
  4. Gradle 8.14.5 缓存 `C:\Users\HSG\.gradle\wrapper\dists\gradle-8.14.5-bin` → 首次构建自动下载 → 只多等一次；离线需预置。
  5. 真机 Secure 配置与 `/data/adb/lspd/log/modules_*.log` → 换机不需要，换会话**不要动** → 只影响 E2E。
  6. CodeGraph 索引 `.codegraph\` → 新机跑 `codegraph init` → 只影响代码图谱检索。
- **可安全删除**：`build\`、`app\build\`、`.gradle\`、`tools\__pycache__\`、`cyc_*.png`、`*.apk`、`docs\*.log`、`docs\lspd_cfg.db`；**不要删** `.secrets\`、`反馈日志\`、`deliverables\`。`local.properties` 被 .gitignore 排除，换机需重建。

## 3. 环境差异表

右列＝「怎么装／怎么验证装好了」，版本写具体数字。

| 项 | 旧机实测值（参照） | 新机获取与验证 |
|---|---|---|
| OS | Win11 64 位（10.0.26300.9974）+ Git Bash (MSYS2) | Win10/11 64 位 + Git for Windows；`bash -lc "uname -a"` 出现 MINGW64／MSYS |
| 运行时 | JDK `openjdk 21.0.11`（JBR），源码/目标 Java 17；Python `C:\Users\HSG\.dsh\dsh-runtimes\dsh-primary-runtime\dependencies\python\python.exe`（带 Pillow）；Node 随 DSH | 装 Temurin 21（或 17+），`java -version` 输出 21.x；Python 3.10+ 且 `python -c "import PIL"` 成功。**不要**为「取最新」把 Gradle/AGP 升到 9.x（破坏 flavor 与 xposed 依赖，见第 12 节） |
| 构建 | Gradle Wrapper **8.14.5**、AGP **8.9.2**、Kotlin DSL | 用仓库自带 `gradlew.bat`；`gradlew.bat --version` 显示 8.14.5 |
| SDK | compileSdk 35／minSdk 29／targetSdk 35；build-tools 36.0.0 | SDK Manager 装 Platform 35 + Build Tools 36.0.0；`apksigner.bat` 存在且 `sdk.dir` 指向它 |
| 编辑器/AI | DSH（Web GUI `http://127.0.0.1:<端口>`），group/dsh-v4.1-flash → intent-split | 任意能读 .md 的平台；验证：粘进本文件让对方复述第 2 节 |
| 网络/代理 | GitHub 直连偶发 SSL handshake 失败或 RST，备用 `-c http.https://github.com/.proxy=http://127.0.0.1:7890`；源 google()、mavenCentral()、`https://api.xposed.info/` | 需访问 services.gradle.org、Maven Central、api.xposed.info；`gradlew.bat :app:assembleDebug --offline` 能解析依赖（已缓存时）；推送失败先直连再试 7890 |
| 凭证位置（**无明文**） | Windows 凭据管理器 `LegacyGeneric:target=git:https://github.com`（GCM/helper 读）；`_b287.cmd`／`_rel290.cmd` 以 `RELEASE_*` 注入签名密码；`.secrets\coloros-clock-release.p12` | `git credential-manager` 或 `git config credential.helper` 指向系统凭据；签名值向负责人索取写进本机脚本。**绝不把 token／密码／私钥写进文档或提交** |
| 真机/服务 | `D:\Android\Sdk\platform-tools\adb.exe`；`ab8b3a49`=PHK110/ColorOS15/SDK35（本轮在线）、`65DUBAJFWKWSVGXS`=PHP110/ColorOS16/SDK36（本轮离线）；LSPosed 2.2.1 (7912) API 102 | `adb devices` 至少一台 `device`；真机仅 E2E 需要 |
| 环境变量 | `RELEASE_KEYSTORE_FILE/STORE_PASSWORD/KEY_PASSWORD/KEY_ALIAS`（两 .cmd 注入）；`org.gradle.jvmargs=-Xmx2g -Dfile.encoding=UTF-8` | 换机放回 `.secrets` 并确认四个变量；`:app:assembleRelease` 出签名包即成功 |
| 磁盘/内存 | ≥2 GB 堆、约 3~4 GB 磁盘 | `:app:assembleDebug` 不报 OOM／磁盘满 |

## 4. 从零跑起来（命令 + 期望输出）

均 Git Bash，先 `cd /d D:\MyApp\ClockSeconds`。

1. **取代码**：`git clone https://github.com/kelebao/ClockSeconds.git`，或整份拷贝 `D:\MyApp\ClockSeconds`（后者才带 `.secrets`／`反馈日志`／`deliverables`）。成功：有 `gradlew.bat`、`settings.gradle.kts`、`app\src\legacy\` 与 `app\src\modern\`。
2. **装依赖**：无需手工装，`gradlew.bat` 首次自动下 Gradle 8.14.5 与 `de.robv.android.xposed:api:82`、`io.github.libxposed:api:102.0.0`、`org.json:json:20240303`。成功：`gradlew.bat --version` 打印 `Gradle 8.14.5`。离线：把 `~\.gradle\` 整份拷到新机同路径。
3. **配置**：写 `local.properties` 一行 `sdk.dir=D:\\Android\\Sdk`（换实际路径，反斜杠写两个）。四个 `RELEASE_*` 按第 3 节处理——**涉及凭证，按 14.1 先问**。成功：`gradlew.bat :app:tasks --all` 不报错。
4. **构建（debug + 单测）**：
   ````bash`
   `MSYS2_ARG_CONV_EXCL='*' cmd.exe /c 'gradlew.bat :app:assembleDebug :app:testLegacyDebugUnitTest --console=plain'`
   `````
   成功：`BUILD SUCCESSFUL in ~10s`、`72 actionable tasks`，`app/build/outputs/apk/{legacy,modern}/debug/app-*-debug.apk` 两包都在。本机一键：`cd /d/MyApp/cs_shots && cmd //c _b287.cmd`（脚本已设 `RELEASE_*` 并加 `-q`）。常见失败：① `SDK location not found` → `local.properties` 路径错；② MSYS 把 `/c` 转 Windows 路径导致 `cmd` 只打印版本横幅、构建没跑 → 用上面 `MSYS2_ARG_CONV_EXCL='*'` 或 `cmd //c`（双斜杠）；③ `Could not resolve de.robv.android.xposed:api:82` → 网络或仓库被删；④ `testDebugUnitTest` 歧义 → 有 productFlavors，必须用 `testLegacyDebugUnitTest`。
5. **release**：`cd /d/MyApp/cs_shots && cmd //c _rel290.cmd`（等价 `gradlew.bat :app:assembleRelease`）。成功：`cmd /c "D:\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs <apk>` 打印 `2850e82a5c4e001eb19e1d4d340bfaca049ebea1abc4f1940c02c8065137b82e`。
6. **真机安装**（E2E 才需，属 14.1 敏感操作）：`adb -s <serial> install -r <apk>` → `Success`；`adb -s <serial> shell su -c 'killall com.android.systemui'` 等 35~40s；LSPosed 管理器勾作用域 `com.android.systemui`。
7. **最小冒烟（30 秒内）**：
   ````bash`
   `"C:/Users/HSG/.dsh/dsh-runtimes/dsh-primary-runtime/dependencies/python/python.exe" tools/check_i18n.py`
   `````
   成功：`reference key count: 155 (values-ar)`、`README feature rows per language: 11 (x21)`、`i18n consistency OK`、EXIT=0。

## 5. 验证与门禁

- **完整命令**：`cd /d/MyApp/cs_shots && cmd //c _b287.cmd > _bNNN.txt 2>&1`（日志 `EXIT=0`）+ 第 4 节第 7 步 i18n + `apksigner verify --print-certs`。
- **通过标准**：① `BUILD SUCCESSFUL`、`javac` 0 error；② `app/build/test-results/testLegacyDebugUnitTest/TEST-*.xml` 三套件共 **27 用例**（ArcTickDrawable 6 + ClockSecondsConfig 13 + ClockTask 8），`failures=0 errors=0 skipped=0`；③ i18n 输出上述三行且 EXIT=0（22 个 `strings.xml` 键集合一致＝155 键，21 个 README 特性行数一致＝11 行）；④ 两 flavor debug APK 都生成；⑤ release 证书 SHA-256 = `2850e82a…7b82e`。
- **已知 FAIL 及原因（别当 bug 修）**：`Deprecated Gradle features… incompatible with Gradle 9.0`、`[Incubating] Problems report…` 是 8.14.5 常规提示；`ModuleSettingsActivity.java` deprecation 警告是既有代码；`testLegacyDebugUnitTest UP-TO-DATE` 是缓存命中不是没跑；真机 `uiautomator dump` 在每秒重绘时必失败（见 12 节）。
- **耗时/联网/真机**：增量约 10 秒，首次干净构建数分钟（需联网）；单测与 i18n 纯本地；真机 E2E 需 LSPosed 设备 + 重启 SystemUI 约 40 秒。
- **改完必须重跑**：build + 27 单测 + `check_i18n.py` + `versionCode` +1 + `versionName` 改 `1.2.0-test.YYYYMMDD-HHMM` + APK 文件名带同一时间戳 + CHANGELOG 补段 + 22 语言 `strings.xml` 同步 + 验签。
- **不能删／不能绕过的门禁**：`check_i18n.py`（键集合与 README 行数一致性）；`versionCode` 单调递增（用户铁律）；签名证书 SHA-256 恒定（换了用户要卸载重装丢配置）；探针／诊断卡必须包在 `BuildConfig.DEBUG`；`README.md` 是 `tools/gen_readme_i18n.py` 生成物**禁止手改**（改 `tools/readme_l10n_*.py` 再生成）；22 语言 `strings.xml` 同步；`--no-verify`／`SKIP_VERIFY` 一律禁止。

## 6. 任务与验收定义

**当前任务**：用户 m36689「按《项目交接单模板》生成交接单」——即本文件；代码零改动。
**业务侧待办（非本交接单任务，仅登记）**：
- **issue #11**（GitHub，作者 MTH-96，OnePlus 15 / ColorOS 17）：要求「自定义位」支持浅底／深底两套颜色随系统自动切换、未自定义位跟随系统文字色、并保留现有固定色模式。已给出 A/B/C 三方案分析，**等用户在 A/B/C 里拍板后才动代码**。这是中断点（附录 A2）。
- 打 git tag（v*）、发 GitHub Release、更新 Xposed-Modules-Repo 模块仓库 Release：**三件事都必须先取得用户本人明确确认**（用户原话「Release必须需要我确认才弄」）。
- 澎湃 OS 3 真机复验：需用户把 vc323 APK 装到 Redmi peridot 并回传 `modules_*.log`；本机无该设备。
- ColorOS16 单色模式逐位上色：adb tap 单色 tab 多次未生效（原因未明），需用户手动点一次确认；代码路径未改动。

**验收定义（怎样算做完）**：① 本文件完整、自包含、19 节 + 附录 A/B 齐全、无外部引用；② 第 4/5 节命令可在干净会话里复跑出同样结果；③ 附录 A2 让接手者能精确续上中断点；④ 第 14.1 节敏感操作清单可直接照做；⑤ 交付给用户并说明「专业版 + 通俗版」两层。

### 6.1 空跑验证记录表（本次交接实测，未改任何代码）

| 命令 | 实测结果 | 结论 |
|---|---|---|
| `git rev-parse HEAD` / `git branch --show-current` / `git status --short` | `8fc8631d494535adbd94cbc19315055f984de271` / `main` / 空 | 干净、与文档一致 |
| `MSYS2_ARG_CONV_EXCL='*' cmd.exe /c 'gradlew.bat :app:assembleDebug :app:testLegacyDebugUnitTest --console=plain'` | `BUILD SUCCESSFUL in 10s`，`72 actionable tasks: 2 executed, 70 up-to-date`；无 error / FAILED | 构建可复现；日志 `D:\MyApp\cs_shots\_bh6.txt` |
| `app/build/test-results/testLegacyDebugUnitTest/TEST-*.xml` | 3 套件 27 用例，`failures=0 errors=0 skipped=0` | 单测全绿 |
| `python tools/check_i18n.py` | `reference key count: 155 (values-ar)`、`README feature rows per language: 11 (x21)`、`i18n consistency OK`、EXIT=0 | 22 语言键集合 + 21 README 行数一致 |
| `cmd /c "D:\Android\Sdk\build-tools\36.0.0\apksigner.bat" verify --print-certs deliverables\io.github.kelebao.colorosclockseconds_1.2.0.apk` | `Signer #1 certificate SHA-256 digest: 2850e82a5c4e001eb19e1d4d340bfaca049ebea1abc4f1940c02c8065137b82e` | 签名证书未变 |
| `java -version` | `openjdk 21.0.11 (JBR-21.0.11+1)` | JDK 21 可用 |
| `adb devices` | 仅 `ab8b3a49` 在线（PHK110/ColorOS15）；`65DUBAJFWKWSVGXS` 离线 | 真机 E2E 需重新连机 |
| 会话体检 `GET /plugin/session-health/snapshot` | `title=状态栏时间显秒`、`sizeBytes=52743048`、`eventCount=78160`、`snapshotCount=4062`、`pressureTokens=179923`、`level=danger` | 红灯属实（附录 A1） |
**注**：`cmd //c _b287.cmd > _bh1.txt` 只得到 Windows 版本横幅、EXIT=0 却无 Gradle 输出（MSYS 把 `/c` 转路径）；另两次直跑 gradlew 挂死 600s 后 kill（job `bash-6b9f479670f34a71`、`bash-39987065934b4ffe` 均已 kill）。**只有带 `MSYS2_ARG_CONV_EXCL='*'` + 完整 `--console=plain` 的那次成功**，第 4 节已写成该形式。

## 7. 架构与数据流（读代码前先看）

- **模块不自绘状态栏时钟、不新建 View**：只 hook 系统已有的时钟 TextView（ColorOS 是 `StatClock`/`OplusClockExImpl`，HyperOS 是 `com.android.systemui.statusbar.policy.Clock` 及孪生 `statusbar.views.MiuiClock`）。
- **主链路**：`TextView.setText(after)` 钩子里缓存系统原文 `BASE_TEXT` → `writeTime()` 拼出 `HH:mm:ss` 并追加零宽空格 U+200B → 用 `ForegroundColorSpan`（逐位）或 `GradientTextSpan`（渐变）上色 → 交回系统自己渲染。
- **四个必要补丁（删任何一个都会回归）**：① 末尾 U+200B 破坏系统 `HH:mm:ss` 模式匹配，防主题模块剥掉 span；② setSpan 前先 `removeSpan` 清掉所有外来 FCS；③ `ClockSecondsConfig.adaptColorForBackground` 在浅底上压暗自定义色（阈值 0.85），与系统文字色 `SYS_TEXT_COLOR_KEY` 配合；④ `WRITING_CLOCK` 防递归 + span 感知幂等（返回 2 = 真掉色，触发 ticker 重补）。
- **掉色重补**：`forceReapplyColors(TextView)` 先试 `trySpanExisting`（直接给 `view.getText()` 补 span），失败回落 `view.setText(sb)`；调用点在 `onDraw(before)`（200ms 节流）、`setText(after)`、以及给时钟运行时类挂的生产版 after 钩子。开头有快速短路：色还在就 return，健康 ROM 零开销。HyperOS 另有 `installXiaomiClockReapplyHooks` 挂在 `updateClock/updateTime/updateHeaderColor/onDarkChanged` 之后。
- **配置通道**：`Settings.Secure` 存开关与颜色（enabled、mark、gradient_enabled/start/end、digit_color_0..5、任务 JSON 等）+ signature 级 `CONFIG_PERMISSION` 广播（`ACTION_SET_TASKS` 等）。**关键**：模块只在广播里 `reloadTasks()`，所以 `adb shell settings put secure …` 写任务 JSON 不生效；要写 LF 脚本 push 到 `/data/local/tmp` 再 `su -c sh` 执行发广播。
- **自定义任务**：`ClockTask` 触发 `TRIGGER_HOURLY/DAILY/WEEKLY/ONCE`、动作 `ACTION_SHOW/LEGACY_HIDE/COLOR`；动作 COLOR 只存渐变（vc293 起删单色，`schemes` 为 `int[n][2]`：每套 [起色, 止色]）。`evaluateTasks()` 在窗口内且 `!sColorApplied.contains(id)` 时调 `applyTaskColors()`；轮换下标持久化在 Secure 键 `coloros_clock_seconds_scheme_state`（vc322），按分钟桶去重，SystemUI 重启后继续轮换。
- **双入口**：legacy = `app/src/legacy/assets/xposed_init` → `hook.ClockSecondsModule`（`de.robv.android.xposed` API 82 真依赖）；modern = `META-INF/xposed/java_init.list` → `ModernEntry extends XposedModule` → 把 `XposedInterface` 注入 `XposedBridge.attach` → 复用同一 `ClockSecondsModule.handleLoadPackage`，靠 `app/src/modern/java/de/robv/android/xposed/*` 兼容 shim 让业务代码零改动。**严禁用 legacy 包去调 modern 的类**，会 ClassNotFoundException。
- **设置页**：`ModuleSettingsActivity.java` 纯代码构建 UI（无 XML 布局），四区块合并卡 + 虚线分隔、任务编辑触发 2×2 网格、22 语言文案从 `strings.xml` 取。诊断／探针必须包在 `BuildConfig.DEBUG`。

## 8. 当前状态清单

| 状态 | 项 | 依据 |
|---|---|---|
| ✅ 完成 | 显秒 + 逐位/渐变上色、U+200B 防剥 span、掉色重补、浅底压暗 | 长期主线，`CHANGELOG.md` vc37~vc323 |
| ✅ 完成 | 自定义任务（整点/每天/7天/一次性 + 显秒/隐藏/换配色）、多套渐变选择/保存/列表胶囊显示 | vc287~295，提交 3344d17、6af41d9 |
| ✅ 完成 | 双 flavor（legacy API 82 / modern API 102）+ 双装提醒卡 + modern 激活判定修复 | vc311/312/315 |
| ✅ 完成 | HyperOS 3 掉色适配、单 0 补零、保留 MIUI 字形特性、冒号光学抬升、面板大钟 tnum 与收起动画防抖、锁屏误接管排除 | vc303~310、313、314 |
| ✅ 完成 | ColorOS16 图标左右抖动修复（tnum + setTextScaleX 0.92），用户已口头确认「图标不跳了」 | vc321 |
| ✅ 完成 | ColorOS15 悬浮窗渐变（FloatTimeView 自绘 LinearGradient）+ 多套配色按序轮换（Secure 持久化下标） | vc322 |
| ✅ 完成 | 悬浮窗时间垂直居中并随窗口大小缩放（基线按视图高度算） | vc323 = HEAD 8fc8631 |
| ✅ 完成 | README 21 语言生成、22 语言 strings.xml 同步、i18n 门禁 | `tools/check_i18n.py` 实测 OK |
| ⏳ 等用户拍板 | issue #11 浅底/深底双色自动切换（A/B/C 三方案未选） | 未写任何代码 |
| ⏳ 等用户确认 | git tag / GitHub Release / 模块仓库 Release | 铁律 |
| ⏳ 等用户回传 | 澎湃 OS3 真机新日志；ColorOS16 单色 tab 手动验证 | 本机无该设备／adb tap 未生效 |
| ⚠️ 历史悬置 | adb 点单色 tab 多次不生效（原因未明）；vc301 分钟翻页动画已随 vc292 整段删除（非遗留代码，无需清理） | 未回归，不影响主线 |
| ❌ 明确放弃 | 锁屏时间显秒/上色（vc279 整体删除，勿再做）；锁宽 setMaxWidth（StatClock 覆写无效）；GradientTextSpan 在浮窗的 shader 坐标（改自绘）；渐变流动逐帧动画（CPU 40% → 6.8%，vc212 删除）；四色渐变（vc218 回退两色） | `CHANGELOG.md` 各段 |

## 9. 已知坏掉 / 半成品

- **旧交接文档 `D:\MyApp\ClockSeconds\项目交接-ClockSeconds-2026-10-26.md` 已过时**（vc36 时代，无双 flavor、无任务、无 HyperOS 适配）。保留仅为历史，**不要照它做**；本文件取代它。
- `CHANGELOG.md` 头部标题仍写 `测试线 versionCode 37 ~ 291`，但正文已记到 vc323——标题未同步，属已知小瑕疵，不影响内容。
- **vc301 分钟翻页动画**：已随 vc292 整段删除（`sLastFlipHhmm/sFlipAnimators/startMinuteFlip` 在 `ClockSecondsModule.java` 中 grep 零命中，2026-11-12 审计复核），非遗留代码，无需清理。历史上帧验证失败（220ms 窗口抓不到帧）与删除时 `cut(start,end)` 首次因 end 锚点在 start 之前失败 `CUT MISS(6511,6345)` 的教训仅供查阅。
- **vc315 双装提醒卡真机未验证**（当时设备掉线）：红色卡片/Toast 是否出现、「卸载另一个版本」能否调起系统卸载页，仍需两包同时安装时确认。
- **HyperOS 3 大钟 `stored=[20:50 :35]` 类问题**：系统文本自带尾随空格，靠 `isClockPadding()`（空白/NBSP/U+200B/ZWNJ/ZWJ/BOM）过滤；换 ROM 时若出现冒号被顶开先看这里。
- **EOL / 编码**：`ModuleSettingsActivity.java` 混合 EOL（CRLF 主体 + 个别 LF），`ClockSecondsModule.java` 全 CRLF，22 个 `strings.xml` 全 CRLF，`app/build.gradle.kts` **非合法 UTF-8**（须 latin1/字节方式读写）——批量替换前必须先统一 EOL，否则 `old_string count=0`。

## 10. 明确不做（别重做、别提议）

- **不做锁屏时间显秒/上色**：vc274~277 实证控件走「录制 + 回放 + 按壁纸 tint」，FCS / setTextColor / shader / canvas 全覆盖全部无效，RECT 实验判定 canvas 层无解；vc279 已把整个功能删除。**勿再立项**。
- **不自绘状态栏时钟**：vc19 前在三星 One UI 翻车，现架构只 hook 系统 TextView。
- **不调用 `Layout.getLineLeft/getLineRight`**：`BoringLayout → TextLine.metrics → updateDrawState` 无限递归 → StackOverflow → SystemUI 连崩 4 次 → LSPosed 进安全模式。测文字宽度一律用 `Paint.measureText`。
- **不用 `setMaxWidth` 锁时钟宽度**：StatClock 覆写 `getMaxWidth/onMeasure`，无效且可能裁剪；宽度用 tnum + setTextScaleX 治。
- **不做渐变流动逐帧动画**：vc204~211 曾做 FlowAnimator（postDelayed 50~120ms），CPU ~40% 单核；vc212 整段删除回到 1Hz 搭便车 6.8%。
- **不做四色渐变**（vc218 已回退两色）；**不加新插件/多插件协同**（用户铁律：交付工具要可整文件夹拷贝、零安装自包含）。
- **不手改 `README.md`**（生成物）、**不跳过 `check_i18n.py`**、**不降 `versionCode`**、**不换签名证书**。

## 11. 关键决策与理由

| 决策 | 理由（含被否方案） |
|---|---|
| 只 hook 系统 TextView，缓存 BASE_TEXT 再拼秒 | 自绘在三星翻车；系统渲染负责布局/字体/深浅色，模块只管文本与 span |
| 末尾加 U+200B | 实测主题模块按 `HH:mm:ss` 模式匹配后剥 span；加零宽空格破坏匹配且视觉无痕 |
| 浅底压暗阈值 0.55 → 0.85，统一 `adaptColorForBackground` | vc240~241 预览卡与状态栏色差：预览按 `view.getWidth()` 铺色带只露中段是假象，真因是阈值与调用点不统一 |
| 三处 xOffset 不统一（状态栏 0、设置页预览 1.0×文字宽、浮窗 0.5×文字宽） | 各视图排版不同，实测逐一对齐；浮点色带坐标不可复用 |
| ColorOS 抖动用 tnum + `setTextScaleX(0.92f)`，不锁 View 宽 | 图标跟的是**文字墨迹宽度**不是 View 宽（vc317 探针 `pin=211 tw=159~180` 实证）；钉宽、MetricAffectingSpan、尾补细空格三个方案都失败 |
| 小米 tnum 用 `applyTnumKeepingExisting` 追加而非覆盖 | vc302 裸 `setFontFeatureSettings("tnum")` 抹掉 MIUI 圆角字形，用户反馈「不圆润」 |
| 浮窗渐变改 FloatTimeView 自绘 LinearGradient | GradientTextSpan 的 shader 在居中排版视图里原点漂移（ColorOS15 偏一个文字宽、16 又不同），只画出几块纯色 |
| 换配色只存渐变（`int[n][2]`），删单色 | 用户 m30983 明确「不支持单色，删除单色相关代码」；vc293 起 `applyTaskColors` 只写 gradStart/gradEnd |
| 多套配色轮换下标持久化到 Secure `coloros_clock_seconds_scheme_state` | 只在内存里时 SystemUI 重启归零，永远从第一套开始（vc322 根因） |
| modern flavor 用 shim 兼容层而非复制业务代码 | 用户要「出两个版本」，但两份 7000 行代码不可维护；shim 让 `ClockSecondsModule` 零改动 |
| 打 tag / Release 一律先问用户 | 用户原话「Release必须需要我确认才弄」 |

## 12. 踩过的坑（照抄可少走半天）

1. **构建命令**：`cmd //c _b287.cmd > _b.txt` 在 MSYS 下 `/c` 会被转成 Windows 路径，只打印版本横幅、EXIT=0 却没跑 Gradle。可靠写法见第 4 节第 4 步；另 `MSYS_NO_PATHCONV=1` 直跑 gradlew 曾挂死 600s。
2. **screenshot**：必须 `adb -s <s> shell "screencap -p //data/local/tmp/a.png"` 再 `adb -s <s> pull //data/local/tmp/a.png …`——**远程路径都用双斜杠**，否则 MSYS 转成 Windows 路径；`exec-out` 直连出全黑。
3. **uiautomator dump 在每秒重绘时必失败**（状态栏时钟 1Hz + 动画）。只能用截图 + PIL 像素扫描；dump 时 `//data/local/tmp/ui.xml` 也要双斜杠。
4. **`adb shell settings put secure` 写任务 JSON 不生效**（模块只在广播里 reloadTasks）；`adb shell su -c` 会剥引号，命令里不能带竖线；解法是写 LF 脚本 push 到 `/data/local/tmp` 再 `su -c sh` 执行。CRLF 脚本 Android sh 不认。
5. **动画「没跑」多数是熄屏**：`isInteractive=false`；测试前 `input keyevent WAKEUP` + `svc power stayon true`，测完恢复 false。
6. **装包后必须** `su -c "killall com.android.systemui"` + 等 35~40s 才加载新 hook；装 debug 若签名不同先 `uninstall`。
7. **模块日志在** `/data/adb/lspd/log/modules_*.log`（前缀 ColorOSClockSeconds），**logcat 抓不到应用日志**。看三行定位 HyperOS：`XIAOMI shade clock:`、`XIAOMI reapply hook/class missing`、`GRADIENT-MISS`。
8. **大文件批量替换**：`ClockSecondsModule.java` 与 `ModuleSettingsActivity.java` 合法 UTF-8 但 CRLF / 混合 EOL；先把 `\r\n → \n` 统一再 replace，写回 `\n → \r\n`；`app/build.gradle.kts` 用 latin1 字节读写。Java 源码里的 `"\u200B"` 字面量在 python 脚本里必须放 r'''…''' 否则被解释成真字符导致 replace 失败。
9. **`cut(start,end)` 保留 end 行**——repl 里若再带同一句会重复 `addView` 触发 `child already has a parent` 崩溃（vc291 真机踩过）。
10. **git 事故**：`git rm --cached -r .` → `reset --soft HEAD~1` → `git checkout -- .` 把工作区回退到旧提交丢代码；恢复靠 `git reflog` 找 dangling commit 再 `reset --hard`。**软重置后绝不要随手 checkout -- .**。
11. **GitHub push 挂死**：挂点是 `git-credential-helper-selector` 垫片；绕过 `T=$(printf "protocol=https\nhost=github.com\n\n" | GCM_INTERACTIVE=Never timeout 15 git-credential-manager get | sed -n "s/^password=//p")` 后 `git -c credential.helper= -c "credential.helper=!f() { echo username=kelebao; echo password=$T; }; f" push origin main`（`$T` 必须直接展开）；直连 RST 时加 `-c http.https://github.com/.proxy=http://127.0.0.1:7890`。
12. **像素测量别把搜索区间写成起点**：曾把 `range(292,620)` 当全区间，得出「图标恒 292」的伪结论；必须从 x=40 全扫 + 分 run。
13. **设备会间歇掉线**：`adb devices` 空时 `adb kill-server && adb start-server` 恢复；装包前先确认 online。

## 13. 回滚与恢复

- **代码回滚**：`git log --oneline` 找目标提交（vc323=8fc8631、vc322、vc321=…、vc295=6af41d9、vc290=3344d17、vc302=799b9ae、vc300=eb4d969）；`git reset --hard <commit>` 会丢未提交改动，先 `git stash` 或确认工作区干净。
- **手机回滚**：装回 `deliverables/` 里对应版本 APK（签名一致可直接 `install -r`），`killall com.android.systemui` 后生效。
- **配置回滚**：Secure 键用 `adb shell settings delete secure <key>` 或广播重置；测试残留（如 `color_schedule`、`coloros_clock_seconds_scheme_state`、测试任务）验证完必须清掉，恢复用户原值。
- **LSPosed 安全模式**：若 SystemUI 连崩触发安全模式，在 LSPosed 管理器里取消勾选模块、重启，再排查（历史上是 `getLineLeft` 递归导致）。
- **丢提交**：先 `git reflog`，dangling commit 都在；不要重新写一遍。

## 14. 安全红线与操作边界

- **绝对禁止**：以「造测试环境」为由写或删 `/data/system`、`/data/system_de`、`/data/misc/gatekeeper`、`/data/misc/vold` 下任何文件（曾误删锁屏数据库破坏 FBE CE 解密链）。锁屏相关：只观察、由用户手动操作；例外仅限「设备无锁屏密码 + 用户已授权保持 E2E 不中断」时，用 `adb input keyevent/input swipe` 等 UI 输入唤醒并上滑解锁，不写删上述目录、不主动制造锁屏。
- **禁止 `am force-stop com.evin.viewer`**（用户查看器服务，与本模块无关）。
- **不改用户设备上的既有配置**：本次交接前已把 `65DUBAJFWKWSVGXS` 浮窗关闭、几何还原 560×224 @ (313,962)；`ab8b3a49` 的「任务2」触发恢复「每天 00:00」、`scheme_state` 测试键已删、原渐变端色已恢复。用户现有 3 条任务（kk / 任务2 每整点显秒 5 分钟 / 任务3 每整点换配色）与六位色、gradient_start/end 原值一律不动。
- **签名私钥 / GitHub token / keystore 密码绝不写进文档、日志或提交**；`.secrets/` 已被 .gitignore 排除。
- **探针/诊断卡必须包在 `BuildConfig.DEBUG`**，release 包不得带 `WRITE-CHECK` / `TASK eval` 之类调试日志。
- 打 tag / GitHub Release / 模块仓库 Release = **每次单独确认**，见 14.1。

### 14.1 敏感操作人工确认清单（逐次执行，拒绝就停）

接手者执行前**必须先说明确切命令与影响、得到用户明确同意**；用户拒绝就停在该步，不换命令绕过。

| # | 操作 | 确切命令 / 影响 | 为什么需确认 |
|---|---|---|---|
| 1 | 打 git tag | `git tag v1.3.0 && git push origin v1.3.0`：产生对外可见版本标记 | 用户铁律，tag 与模块仓库 Release 绑定 |
| 2 | 发 GitHub Release | `gh release create …` 或网页上传 APK：对外发布 | 用户原话「Release必须需要我确认才弄」 |
| 3 | 更新 Xposed-Modules-Repo 模块仓库 Release | 提 PR / 发 Release 更新模块描述与下载 | 同上，影响所有用户升级 |
| 4 | git push | `git push origin main`：把本地提交推到远端 | 用户此前授权「持续备份 push」，但**跨大版本/含 tag 时仍先确认** |
| 5 | 真机安装 / 卸载 APK | `adb -s <s> install -r <apk>`、`adb uninstall <pkg>`：改变用户手机状态 | 用户设备，可能打断正在用的模块 |
| 6 | 重启 SystemUI | `adb -s <s> shell su -c "killall com.android.systemui"`：状态栏消失约 30s | 影响用户当前使用 |
| 7 | 写 / 删 Secure 键或任务 JSON | `settings put/delete secure <key>`、发 `ACTION_SET_TASKS` 广播 | 会改用户配置，验证完必须恢复原值 |
| 8 | 读取 / 使用签名密码与 GitHub token | `_b287.cmd` 内 `RELEASE_*`、GCM 取 `gho_` token | 凭证保密，不得回显到对话 |
| 9 | 删除文件 / 批量重写大文件 | `git rm`、`write` 覆盖 `ClockSecondsModule.java` 等 | 历史上有截断/丢代码事故，改前先备份或确认 git 干净 |
- **无需确认**：只读命令（`git status/log`、`adb shell dumpsys`、`screencap`、读日志）、本地构建与单测、`check_i18n.py`、`apksigner verify`。

## 15. 接手约定

- **沟通风格**：专业准确 + 同时给一段通俗易懂的总结（用户明确偏好）。
- **每个版本固定动作**：`versionCode` +1；`versionName = 1.2.0-test.YYYYMMDD-HHMM`；APK 文件名带同一时间戳；`CHANGELOG.md` 补该版本段；22 语言 `strings.xml` 同步；README 由脚本重生成；构建 + 27 单测 + i18n + 验签；提交 + push（tag/Release 另问）。
- **改完必报**：改了哪几个文件、为什么、验证命令与实测输出（`EXIT=0` / `javac` 0 error / 截图证据）。
- **不改用户数据**：见第 14 节；测试任务用完即删并恢复原值。
- **文档不做外部引用**：本文件完全自包含（用户要求：单份文档要能直接发给别人）。后续交接**在附录 B 追加一行**，不要新建 `HANDOFF-新.md` 覆盖。
- **工具链不追新**：用户环境另有一套「JDK25 / Gradle9.8.1 / AGP9.4.1 取最新」的通用偏好，但**本项目锁定 Gradle 8.14.5 / AGP 8.9.2 / Java17**，升级会破坏 `productFlavors` 与 xposed 依赖解析；以第 3 节为准，冲突时先问用户。
- **会话体检**：老会话已 danger（52.7MB / 78160 事件）；新会话若再次告警，按插件模板重新生成本文件的增量版（追加附录 B，不重写正文）。

## 16. 第一步做什么

1. **只做只读核对**：`cd /d D:\MyApp\ClockSeconds` → `git status --short`（应为空）、`git log -1 --oneline`（应为 `8fc8631 vc323`）、`git branch --show-current`（main）、`adb devices`（确认哪台在线）。把结果与第 6.1 节表对照，有差异先报告。
2. **空跑门禁**：按第 4 节第 4 步构建 + 第 4 节第 7 步 `check_i18n.py`，确认 `BUILD SUCCESSFUL` / 27 单测 0 失败 / `i18n consistency OK`。**一行代码都不改**。
3. **向用户确认下一步**：默认停在「等 issue #11 的 A/B/C 拍板」。把三方案复述一遍请用户选；在用户明确选择前不要写任何实现代码。若用户改提别的需求，再从第 8 节状态清单对齐。

## 17. 术语表

| 词 | 含义 |
|---|---|
| FCS | `ForegroundColorSpan`，逐位/降级路径用的前景色 span |
| GradientTextSpan | 用 shader 给整串文字铺渐变的自定义 span；HyperOS 会被吞 |
| BASE_TEXT | 系统时钟 TextView 原始文本（含「凌晨/下午」等后缀） |
| WRITING_CLOCK | 防 `setText` 递归写入的标志 |
| ticker | 模块内 1Hz 兜底重绘（颜色掉了才触发重补） |
| StatClock / OplusClockExImpl | ColorOS 状态栏时钟类；`isOplusDevice()` 分支 |
| MiuiClock / Clock | HyperOS 状态栏时钟；另有面板大钟与 `MiuiTextGlassView`（锁屏，已排除） |
| legacy / modern | 两个 productFlavor：Xposed API 82 / libxposed API 102（包名后缀 `.modern`） |
| Secure 键 | `Settings.Secure` 里的配置，如 `coloros_clock_seconds_gradient_start` |
| schemes | 任务换配色里的多套渐变 `int[n][2]`，每套 [起色, 止色] |
| tnum | 等宽数字字形特性（`fontFeatureSettings`），用于消除秒数宽度抖动 |
| FloatTimeView | 浮窗自绘渐变时钟 View（vc322 新增，vc323 修垂直居中） |
| U+200B | 零宽空格，破坏系统时间模式匹配以保住 span |
| check_i18n.py | 校验 22 语言键集合 + 21 README 特性行数一致性的门禁脚本 |
| vcNNN | versionCode，本项目版本代号（当前 323） |

## 18. 自检（生成者填写）

- [x] 用法已选定并写明（用法 B：同机换会话；文首给出用法 A 转换法）。
- [x] 第 1~6 节全部按本次会话实测填写，命令带期望输出。
- [x] 全文绝对路径、无「沿用/参见某模板、对应节」类外部引用；门禁、指标、命令、路径、规则均在正文展开。
- [x] 第 14.1 节敏感操作逐条列出确切命令与确认要求。
- [x] 附录 A 三节（A1 红灯 / A2 中断点 / A3 平台步骤）已填。
- [x] 附录 B 已追加本次变更记录一行。
- [x] 未改动任何源代码；第 6.1 节是真实跑出的结果，未把「待执行」写成「已完成」。
- [x] 篇幅纪律已评估并有意偏离：模板建议 8~16 KB，实际约 43 KB：中文 UTF-8 每字符 3 字节 + 19 节全展开 + 命令/路径/指标全量写入）。**决策与补偿**：用户明确要求「交付文档必须完全自包含、指标与门禁展开写进正文」，且本文件要单独发给别人看，故保留完整内容未砍；若接手者确需 16 KB 以内，可删第 12 节坑表与第 11 节决策表的「理由」列，但**不得删第 4/5/14.1/16 节与附录 A2**。
- [x] 「分两批填写、每批停等确认」已评估并后移确认环节：本次由会话体检插件一次性下发指令，用户未在场逐批确认。**替代保证**：第 6.1 节全部为实测值、无推测；第 16 节要求接手者先只读核对并报告差异，等价于把确认环节后移到交接入口；未写任何代码、未执行任何 14.1 敏感操作。
**自检结论**：19 节 + 附录 A/B 齐全，全部自检项均有明确结论；篇幅与分批两处属**有意偏离并已补偿**，不影响接手者按第 0/4/5/16 节安全开工。

---

# 附录 A（用法 B 专用：本次会话现场）

## A1 红灯信息

- 触发原因：DSH 会话体检插件（dsh-session-health v1.2.0）在本会话内检测到 `level=danger`。
- 实测数值（`GET /plugin/session-health/snapshot`）：`title=状态栏时间显秒`、`sizeBytes=52743048`（52.7 MB，danger 阈值 >40 MB）、`eventCount=78160`（danger 阈值 >2 万）、`snapshotCount=4062`、`pressureTokens=179923` / `contextWindow=1048576`。
- 阈值：warn 20MB / 1 万事件 / 5 千快照 / 65%；danger 40MB / 2 万 / 1 万 / 85%。
- 处置：本机另开新会话，带本文件继续；老会话内容由项目摘要块与本文件承载，不依赖逐条历史消息。

## A2 中断点（最重要：精确复现现场）

- **在做什么**：评估 GitHub issue #11（自定义位浅底/深底双色自动切换），已给出 A/B/C 三方案分析，等用户拍板。**没有写任何代码**。
- **代码状态**：`HEAD=8fc8631d494535adbd94cbc19315055f984de271`（vc323「悬浮窗时间垂直居中并随窗口缩放」），branch main，已 push，`git status --short` 为空，工作区干净。
- **后台任务**：全部终态——`bash-5e194b6fa02646b3 completed`；`bash-6b9f479670f34a71` 与 `bash-39987065934b4ffe` 两次挂死构建已 `job_kill`。无遗留进程。
- **真机状态**：`65DUBAJFWKWSVGXS`（PHP110 / ColorOS16 / SDK36）上装的是 vc323 modern，已验证浮窗垂直居中与随窗口缩放，浮窗已关闭、几何还原为 560×224 @ (313,962)；`ab8b3a49`（PHK110 / ColorOS15 / SDK35）上「任务2」触发已恢复「每天 00:00」、测试用 `coloros_clock_seconds_scheme_state` 键已删、原渐变端色已恢复。本轮做交接单时 `65DUBAJFWKWSVGXS` 离线、仅 `ab8b3a49` 在线。
- **未决**：issue #11 三方案未选；tag / GitHub Release / 模块仓库 Release 均未做（等确认）；澎湃 OS3 真机日志未回传；ColorOS16 单色 tab 手动验证未做。
- **下一条指令应当是什么**：让用户从 A/B/C 里选一个方案（或提出第四方案），然后才进入实现；实现前先按第 16 节只读核对 + 空跑门禁。

## A3 平台专属步骤（换会话/换平台操作）

1. **保存本文件**：已在 `D:\MyApp\ClockSeconds\docs\handoff\HANDOFF-ClockSeconds-2026-11-12.md`；不用依赖插件，普通 Markdown 即可。
2. **开新会话**：本机 DSH 新建会话，cwd 设为 `D:\MyApp\ClockSeconds`（老会话 cwd 是 `D:\MyApp`，别沿用）。
3. **注入交接单**：把本文件作为附件上传，或首条消息粘贴全文；开场指令可直接复制第 0 节第 3 条。
4. **确认注入成功**：让新会话复述第 2 节项目根、第 16 节第一步、第 14.1 节第 1 条；复述不上来就重新给文件，不要开始改代码。
5. **换平台（非 DSH）**：本文件不依赖任何插件/技能；把第 2 节绝对路径改为相对 `<项目根>`、跳过附录 A，其余原样。

---

# 附录 B 变更记录

> 每次交接在**本表追加一行**，不新建文档、不覆盖旧行。

| 日期 | 会话 / 环境迁移 | 交接单变化 | 代码/构建状态 | 遗留与下一任注意 |
|---|---|---|---|---|
| 2026-11-12 | DSH 会话「状态栏时间显秒」（52.7MB / 78160 事件，danger）→ 本机新会话 | **新建本交接单**（用法 B，19 节 + 附录 A/B 全填） | 代码零改动；HEAD 8fc8631、工作区干净；实测 `BUILD SUCCESSFUL` + 27 单测 0 失败 + `i18n consistency OK` + 验签一致 | 等用户在 issue #11 的 A/B/C 拍板；tag / GitHub Release / 模块仓库 Release 需逐次单独确认；改大 CRLF 文件前先统一 EOL 并编译验证；勿做锁屏时间适配 |

---

**文档结束。** 本文件由 DSH 会话按 `项目交接单模板.md` 生成，内容取自项目源码、`CHANGELOG.md`、`check_i18n.py` 实测与本次空跑验证；所有「已验证」条目均对应第 6.1 节真实命令输出，未执行的事项一律标注「待执行 / 等确认 / 未验证」。