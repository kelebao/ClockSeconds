# ClockSeconds（ColorOS 时钟显秒）项目交接文档

**日期**：2026-10-26
**用途**：交接给其他 AI 平台继续开发。本文档自包含——新平台无需前情即可上手。
**维护者原环境**：Windows 11 + Git Bash + WorkBuddy AI（本机路径见 §7，换平台只需改路径）。

---

## 📌 TL;DR

- 线上稳定版 **v1.0.9（versionCode 10）**；本地有一条 **1.1.0 测试线已推进到 vc36**，包含大量未发布修复与功能。
- **vc19 ~ vc36 的全部改动尚未提交 git**（最后一笔提交是 vc18 `2c49738`），`1.1.0` tag 未打，模块仓库 Release 未建——这是最紧要的待办。
- 核心技术资产：四条抓凶探针、零宽空格（ZWSP）掉色修法、自适应保色 ticker、ColorOS 17 原生显秒常开 hook——**全部已在源码中，不要重做**。

---

## 1. 项目概览

| 项 | 值 |
|----|----|
| 包名 | `io.github.kelebao.colorosclockseconds` |
| 源码仓库 | https://github.com/kelebao/ClockSeconds（旧名 ColorOS15ClockSeconds，已 301 重定向） |
| LSPosed 模块仓库 | https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds |
| 模块作用域 | `com.android.systemui`（仅系统界面） |
| Xposed 范式 | **legacy API**（`de.robv.android.xposed:api:82`，`xposedminversion 93`）。**禁止**改用 libxposed / YukiHookAPI / 原生 hook（v1.0.3 崩溃史的结论，见 §4.1） |
| SDK 配置 | minSdk 29 / targetSdk 35 / compileSdk 35；Java 17 source/target；AGP 8.x + Gradle 8.14.5；`buildFeatures { buildConfig = true }` 必开 |
| 签名证书 SHA-256 | `2850e82a5c4e001eb19e1d4d340bfaca049ebea1abc4f1940c02c8065137b82e`（**永不更换**，换了用户需卸载重装丢配置） |
| 线上版本 | v1.0.9（vc10，2026-10-24 发布）；源码 Release id 422708678；模块仓库 Release `10-1.0.9` id 422734787 |
| 测试线当前 | vc36 / `1.1.0-test.20261026-2020`（未发布） |

### 核心架构（改代码前必读）

- 模块只 hook 两类稳定入口：`TextView.setText`（after）+ `View.onDraw`（before），外加全局 `View.dispatchTouchEvent`（点击）。**绝不 hook ColorOS 私有渲染类、绝不自绘时钟**。
- 显秒实现：系统设完文本后追加 `:ss`（`writeTime`，文本末尾带零宽空格 `\u200B`）；关秒时走 `applyRedToBaseText`（只补小时色，同样带 ZWSP）。
- 配置通道：设置 App ↔ SystemUI 用 **signature 级 `CONFIG_PERMISSION` 广播** + Secure/Global 设置（`ClockSecondsConfig` 常量表）。
- 设置页是纯代码构建 UI（`ModuleSettingsActivity`，无 XML 布局），卡片按序：Header / 激活状态 / 预览 / 统计 / 小时颜色 / 【仅 DEBUG：时钟探测（测试）+ 诊断与工具】 / 高级 / 桌面图标 / 更新 / 关于 / 主按钮 / Footer。

---

## 2. 硬性规则（违反即返工）

1. **版本命名**：每次构建 `versionCode +1`；`versionName = "1.1.0-test.YYYYMMDD-HHMM"`（构建时刻）；**交付 APK 文件名也必须带同一时间戳**，如 `ClockSeconds-Probe-v36-2026-10-26-2020.apk`。旧时间戳 = 测试者无法区分构建 = 不可接受。
2. **debug（probe）构建必须带 `RELEASE_*` 环境变量**：`RELEASE_KEYSTORE_FILE / RELEASE_STORE_PASSWORD / RELEASE_KEY_ALIAS / RELEASE_KEY_PASSWORD`（值见 `.secrets/keystore.properties`）。带了才用发布证书签名 → 可直接覆盖装现网模块；不带会退回默认 debug key（`1536662c…`），与现网 `2850e82a…` 不符，必须卸载才能装。
3. **证书永不更换**；`.secrets/`（p12 + keystore.properties）永不入 git（已 gitignore，从未进历史）。
4. **发布流程（1.1.0 正式发布时）**：versionCode(+1)/versionName 定稿 → 推 main → 打 `v1.1.0` tag（CI `.github/workflows/ci.yml` 自动出签名包）→ 模块仓库建 Release：tag 格式 **`{versionCode}-{versionName}`**（如 `37-1.1.0`，Xposed 靠它解析 versionCode，格式不可改），附件名 **`{packagename}_{versionName}.apk`**，只放 SUMMARY/SCOPE/README.md，不放源码。
5. **GitHub API 坑**：改 Release 说明用 `PATCH /releases/{id}`（`/releases/tags/{tag}` 404）；删 asset 返回 204 空 body（按 JSON 解析会报错但删除成功，别重试）；模块仓库 **Description（显示名）只能用户在浏览器改**（API 无权限，目标值 `Clock Seconds`）。
6. **README 21 语言管线**：由 `tools/gen_readme_i18n.py` 生成（glob 加载 `tools/readme_l10n_*.py`），**禁止手改**任何 `docs/README_xx.md`；改文案 → 改 l10n 数据 → 重跑生成器 → 提交推送两仓库。
7. **字符串双语同步**：改 `values/strings.xml` 必须同步 `values-zh-rCN/strings.xml`（两者都是简体中文；zh-rCN 是 v35 语言修复的关键，见 §4.6）。
8. **探针/诊断卡片只准 DEBUG**：`buildClockTestCard` / `buildDiagnosticCard` 已包进 `if (BuildConfig.DEBUG)`（`ModuleSettingsActivity.java` ~L482）。release 包 `BuildConfig.DEBUG=false` 时该块被 javac 死代码消除（已 DEX 级验证）。新加诊断功能一律沿用此门控。
9. **git 身份**：全局已配 `kelebao` + GitHub noreply 隐私邮箱（`341922936+kelebao@users.noreply.github.com`），不要改回真实邮箱。

---

## 3. 1.1.0 测试线已完成内容（相对线上 v1.0.9 的增量）

按 versionCode 顺序（全部已实现并大多真机验证）：

| vc | 内容 |
|----|----|
| 11-18 | 抓凶探针组：① PROBE setText caller 链（谁整段替换文本）② SPAN-THEFT（hook `SpannableStringBuilder.removeSpan` 抓直接剥 span 者，含全栈兜底）③ SETTEXT-STRIP（setText 前后对比抓内部重建）④ CLOCKCLASS（运行时 `view.getClass()` hook 其 setText，分层定位）。仅 DEBUG 编译。 |
| 19 | **三星 One UI 修复**：整段移除自绘时钟代码（`isSamsungUniformDigit`/`maybeInstallSamsungSelfDraw` 等），恢复真实 TextView 为 `sClockView` → 三星点击/长按恢复；新增「时钟探测（测试）」卡片。 |
| 20-22 | App 内「重启 SystemUI」「运行诊断」按钮（结果回传 App 内显示，无需 logcat）；模块激活状态卡片（PING/PONG + 心跳）；刷新列表按钮。 |
| 23 | **全关仍有颜色 bug 修复**：所有写文本/颜色路径统一加 `isClockEnabled` 门控；测试卡全关 = 系统默认。 |
| 24 | 诊断通俗化（白话四段式）+ 上次点击时间显示 + 重启按钮心跳确认。 |
| 25 | **关再开秒数不动 bug 修复**：候选池接管分支补 `installTicker(v)`。 |
| 26 | 诊断加「颜色体检」（`colorCheck`：ok/stripped/no_span 等状态，App 内直接判断掉色）。 |
| 27-28 | 卡片去按压仅按钮按压、刷新完成红色提示；「更新后一键生效」按钮。 |
| 29-30 | ContentObserver 监听 Secure 双保险 + 诊断 5s 超时兜底 + LSPosed 安全模式文案警示。 |
| 31 | **显秒开启时掉色修复**：`writeTime` 末尾追加 `\u200B` + setText after 显秒开分支补 `scheduleReapply`。 |
| 32-34 | 补色兜底改**自适应 ticker**：2s→4s→…→30s 衰减、`wrote==2`（真掉色）重置、5 分钟冷却窗口自动停；v34 修掉「共享静态 guard 导致永不停止」bug（改 per-view `guardUntil/tickDelay`），ColorOS 15 真机实测 `color-guard stopped` 正常产出。 |
| 35 | **语言 bug 修复**：新增 `values-zh-rCN/strings.xml`（85 条），真机往返验证通过（en-US ↔ zh-Hans-CN）。 |
| 36 | 时钟探测/诊断两卡片改为仅 DEBUG（DEX 级验证 release 调用点为 0）。 |

另有 **ColorOS 17 原生显秒常开 hook**（`installNativeSecondsHooks`，`ClockSecondsModule.java` L409-452，主安装路径 L376 调用）：反射加载原生 `ClockSecondsController`，hook 三个方法（`isOplusFiveMinutesActive→true`、`scheduleExpiryLocked→null`、`onUserOrModeChanged→到期时间强 0`）去掉系统 5 分钟自动关秒；类不存在（非 ColorOS 17）静默跳过；配套 `mirrorNativeSeconds` 把模块开关镜像到系统原生设置。

---

## 4. 关键 bug 根因档案（新平台必读，防止重蹈覆辙）

1. **v1.0.3 ColorOS 15 启用即崩**：libxposed/原生/自渲染私有类 hook 全不可行。唯一稳定方案 = legacy Xposed 只 hook `setText`(after)+`onDraw`(before)，绝不破坏系统渲染流程。已真机验证。
2. **状态栏点击永远收不到**：时钟触摸被 `NotificationPanelView` 拦截，`OnTouchListener` 无效。必须 hook 全局 `View.dispatchTouchEvent`，用 `getLocationOnScreen`+宽高做命中判定，**不消费事件**（否则阻碍下拉）。一加 Ace5 Pro/ColorOS15 时钟坐标 x∈[60,267] y∈[22,126]；长按 800-1000ms 触发浮窗。
3. **三星点击全失效（v19 已修）**：三星自绘代码把 `sClockView` 指错。教训：任何"另指 sClockView"的方案都会破坏命中区。
4. **ColorOS 17 掉色**：系统 `StatClock#setText` / `setTextWithRedOneStyle` 及第三方主题模块（如 `dev.puitheme.StatusBarModule`）按 `HH:mm(:ss)` 模式匹配重建文本、剥外来 ForegroundColorSpan。**修法 = 补色文本末尾追加零宽空格 `\u200B`**（不占宽度不可见，破坏完整匹配）。显秒开/关两条写文本路径都必须带。
5. **幂等短路不能用 `TextUtils.equals`**：被洗后的纯文本与目标文本字符相同会误判"已上色"而跳过补救；且双 Spanned 走 equals 委托，`SpannableStringBuilder` 未重写 equals 恒 false。改用字符级 `contentEquals` + span 感知。
6. **语言切换 bug（v35 已修）**：20 个限定符语言目录但无 `values-zh-rCN`，切回系统主语言靠"默认回退"路径不可靠（AssetManager 保留上次解析的限定符）。修法 = 简体中文成为一等限定符目录。
7. **LSPosed 安全模式（重大坑）**：反复强杀 SystemUI（killall / killProcess）会被判"崩溃过多"→ 停用所有模块 → 表现为模块突然全失效。**恢复 = 重启手机**（不是重启 SystemUI，那会加重计数）。「重启 SystemUI / 一键生效」按钮连点会触发，按钮文案已加警示。
8. **广播权限双坑**：① 设置 App 接收 SystemUI 回传必须 `RECEIVER_EXPORTED` + **权限传 null**——绝不能要求 `CONFIG_PERMISSION`（SystemUI 是 OEM 签名不持有它，要求了回传全丢 → 激活卡永远红）；② `adb shell am broadcast`（shell UID）发不了 signature 权限保护的广播，调试时用 `su -c "am broadcast ..."`。
9. **重启 SystemUI 唯一可靠命令**：`adb shell su -c 'killall com.android.systemui'`。验证新代码生效：读 Secure `coloros_clock_seconds_active` 心跳，格式 `时间戳|versionName|versionCode`，必须等于新装版本。
10. **测试卡勾选回弹（遗留）**：`clockKey` 含父容器类名，个别视图（ChipChronometer 父为 "?"）SystemUI 重启后 key 变化 → 勾选回弹默认启用。非状态栏时钟，影响小，待稳定化 key。
11. **logcat 缓冲区**：`am crash`/重启 SystemUI 后首批模块日志被启动洪流冲出缓冲区，不是代码问题；用户 LSPosed 导出日志不受影响。

---

## 5. 未完成 / 待办（按优先级）

| # | 事项 | 说明 |
|---|------|------|
| 1 | **git 提交**（最紧要） | 工作区有 4 个改动文件（`app/build.gradle.kts`、`ClockSecondsConfig.java`、`ClockSecondsModule.java`、`ModuleSettingsActivity.java`）+ 未跟踪 `app/src/main/res/values-zh-rCN/`（必须提交）与 `用户反馈日志/`（用户日志，建议 gitignore 不提交）。最后一笔提交是 vc18 `2c49738`，vc19~v36 全部未提交。 |
| 2 | **发布 1.1.0** | 用户确认后：定稿版本号（建议去掉 `-test` 后缀）→ 推 main → 打 `v1.1.0` tag → CI 出包 → 模块仓库建 Release（规则见 §2.4）。 |
| 3 | ColorOS 17 关秒保色长期复测 | 2026-10-26 用户日志已证实小时色稳定存活 + keepalive 30s 稳态（ZWSP 修法生效），但"关秒后长时间观察"的完整复测可再收集。 |
| 4 | clockKey 稳定化 | 见 §4.10。 |
| 5 | 设置页 UI 打磨（非缺陷） | 统一 pill 按钮工厂、状态点加 TalkBack contentDescription、补按下/禁用态（gstack 设计审查报告：`deliverables/gstack/design-review-settings-ui-2026-10-26.md`）。 |
| 6 | 设备状态 | 测试机一加 Ace5 Pro（serial `ab8b3a49`，ColorOS 15）现装 **probe v35**；v36 两包在 `deliverables/` 未部署，由用户决定换装。 |

---

## 6. 构建与验证速查（新平台照抄即可）

```bash
# 构建（RELEASE + PROBE 两个包；PROBE=assembleDebug，必须带 RELEASE_* 才签发布证书）
cd <项目根>
export RELEASE_KEYSTORE_FILE="<项目根>/.secrets/coloros-clock-release.p12"
export RELEASE_STORE_PASSWORD=<见 .secrets/keystore.properties>
export RELEASE_KEY_ALIAS=coloros-clock
export RELEASE_KEY_PASSWORD=<同 storePassword>
./gradlew.bat assembleRelease assembleDebug --no-daemon
# 产物：app/build/outputs/apk/{release,debug}/；按 §2.1 命名拷入 deliverables/

# 验证清单
# 1) 证书指纹 = 2850e82a…（apksigner verify --print-certs xxx.apk）
# 2) zh-rCN 在包内（aapt2 dump resources xxx.apk | grep '(zh-rCN)'，应 85 条）
# 3) 装机后 root 重启 SystemUI：adb shell su -c 'killall com.android.systemui'
# 4) 心跳验证：adb shell settings read secure coloros_clock_seconds_active → 时间戳|versionName|versionCode
```

本机环境：JDK 21 `C:\Users\HSG\.jdks\jbr-21.0.11`（JAVA_HOME）；SDK `D:\Android\Sdk`；Gradle 8.14.5 已缓存在 `~/.gradle/wrapper/dists`（**services.gradle.org 直连不可达**，别删缓存）；依赖走 `~/.gradle/init.gradle` 阿里云镜像；`git push` 到 GitHub 偶尔卡 1-3 分钟，放后台执行。

---

## 7. 密钥 / 产物 / 文档索引

- 密钥：`.secrets/coloros-clock-release.p12` + `.secrets/keystore.properties`（gitignore）；异地备份 `deliverables/backup/ColorOS15ClockSeconds-signing-backup.zip`（用户已拷云盘）。
- 最新交付：`deliverables/ClockSeconds-ToolCardsProbeOnly-RELEASE-2026-10-26-2020.apk`（434,968B）/ `ClockSeconds-Probe-v36-2026-10-26-2020.apk`（491,745B）。
- 复测指引：`deliverables/复测指引-v25-2026-10-26.md`、`deliverables/复测指引-SamsungFix-2026-10-26.md`；回归清单 `deliverables/发版前回归清单.md`。
- ColorOS 17 用户日志：`用户反馈日志/LSPosed_*/`（多次导出，最新 195800）——分析结论：模块运行健康，异常均来自用户机上其他模块（oshin 等 YukiHookAPI 系）。

---

> 交接生成于 2026-10-26，基于项目记忆与工作日志整理。接手后如有与实际代码不符处，以代码为准并回写本文档。
