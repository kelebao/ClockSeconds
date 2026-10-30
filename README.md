<div align="center">

**简体中文** · [English](docs/README_EN.md) · [繁體中文](docs/README_zh-TW.md) · [日本語](docs/README_ja.md) · [한국어](docs/README_ko.md) · [Türkçe](docs/README_tr.md) · [Русский](docs/README_ru.md) · [Tiếng Việt](docs/README_vi.md) · [ไทย](docs/README_th.md) · [Deutsch](docs/README_de.md) · [Español](docs/README_es.md) · [Français](docs/README_fr.md) · [Українська](docs/README_uk.md) · [Polski](docs/README_pl.md) · [Português (Brasil)](docs/README_pt-BR.md) · [فارسی](docs/README_fa.md) · [العربية](docs/README_ar.md) · [Bahasa Indonesia](docs/README_id.md) · [Italiano](docs/README_it.md) · [Ελληνικά](docs/README_el.md) · [ქართული](docs/README_ka.md)

</div>

<div align="center">

# 时间显示秒数（Show Seconds in Time）

让状态栏时间显示**秒**，单击即切换；**六个数字可逐位上色**，也能整串铺一条**渐变**；<br>**长按**弹出毛玻璃浮窗实时显示**网络时间**（可拖动、可拉伸，同样支持渐变）；<br>还能给「整点 / 每天某时刻」排**自定义任务**，到点自动开秒。<br>核心链路只用 AOSP 公开 API（OPPO / 一加 / 真我的私有类兼容为可选增强、找不到即跳过），已在 ColorOS 15 / 16 真机验证。

[![version](https://img.shields.io/badge/version-1.0.9-blue.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/releases/latest)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
[![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)](https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
[![stars](https://img.shields.io/github/stars/kelebao/ClockSeconds.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/stargazers)
[![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)](https://github.com/kelebao/ClockSeconds/blob/main/LICENSE)

</div>

---

## 功能

| 功能 | 说明 |
|------|------|
| 状态栏显秒 | `HH:mm:ss` / `h:mm:ss a`（跟随系统 12/24 小时制与本地时区）；ColorOS 的时钟样式/第三方主题模块会按 `HH:mm(:ss)` 模式重建文本、把颜色 span 剥掉，模块在文本末尾追加一个**零宽空格**破坏该模式，并检测掉色后自动重绘（5 分钟冷却上限） |
| 点击切换 | **单击**状态栏时间即切换显秒开/关；也可在设置页改为**双击**（长按专用于浮窗预览）。任务窗口内点击会提示「已由任务接管」而不偷改总开关 |
| 逐位取色 / 整串渐变 | 六个数字**各自配色**（10 色预设 + HSV 滑杆 + 「跟随系统」），冒号自动光学居中；或改用**整串渐变**：起点色 → 终点色线性渐变，6 套预设、两端自定义，浅底自动压暗。预览卡由模块**自绘**，状态栏与浮窗用 span，两条路径已在两台机器上逐像素比对一致 |
| 长按预览浮窗 | **长按**状态栏时间弹出**毛玻璃半透明浮窗**，实时显示**网络时间**；可整体拖动、按左下角 ◣ 拉伸缩放，位置与尺寸**自动记忆**；支持同款渐变、数字等宽（秒数跳动时整串不抖） |
| 自定义任务 | 按「**整点** / **每天某时刻**」排任务：提前 N 分钟**自动开启秒**，持续 M 分钟后自动恢复原状态；改系统时间/时区会立即重算窗口，另有 60 秒兜底重算，深睡唤醒后也不会错过 |
| 网络时间对比 | 设置页实时对比**网络标准时间**与本地时间偏差；偏差较大时点**同步**，即可打开系统「自动设置时间」向网络校准 |
| 免 adb 授权 | 所有设置写入优先走 `Settings.Secure`，无 `WRITE_SECURE_SETTINGS` 权限时自动改走广播由 SystemUI 代写——**无需 adb 授权**即可正常使用全部功能 |
| 下拉磁贴 | 下拉通知栏添加「时间显秒」磁贴，一键开关显秒（无需打开设置页） |
| 时间识别名 | 个别机型若未能自动识别状态栏时间，可在设置页点「自动检测」一键完成；也可手动填写资源名（如 clock） |
| 界面语言 | 应用内界面已适配 **21 种语言**（跟随系统语言自动切换）：简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、Ελληνικά、ქართული；其余语言回落到英文 |
| 持久化 / 适配范围 | 状态存于 `Settings.Secure`，跨进程共享，重启 SystemUI 或重启设备后保持。**ColorOS 15 / 16（Android 15 / 16）已真机验证**；核心链路只用 AOSP 公开 API，理论上其它厂商同样生效，但作者手上只有 OPPO / 一加的机器，**其它厂商尚未真机验证**。最低支持 Android 10 |

## 安装

1. 从 [**Releases**](https://github.com/kelebao/ClockSeconds/releases/latest) 下载最新版 APK
2. 在 **LSPosed** 中启用本模块，作用域勾选：**系统界面（`com.android.systemui`）**
3. 重启 SystemUI 进程或重启设备
4. 配置页从 **LSPosed 管理器**打开（桌面不生成图标）

> 需要 root（LSPosed 框架）。**通常无需 adb 授权**：设置写入优先走 `Settings.Secure`，没有 `WRITE_SECURE_SETTINGS` 时会自动改走广播、由 SystemUI 代写。若希望设置页**直接写入**（少一次跨进程往返、不依赖 SystemUI 在线），可授予：
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 未授予同样不影响使用——点击状态栏、任务调度、浮窗预览都在 SystemUI 进程内完成，天然具备权限；连初始化写入也不需要你手动执行任何 adb 命令。

## 使用

- **单击状态栏时间**：切换显秒开/关（默认手势）；也可在配置页改为**双击**。
- **长按状态栏时间**：弹出毛玻璃浮窗显示**网络时间**——拖动时间移动位置，点一下时间浮现**关闭**（右上）/ **拉伸**（左下）按钮，拉伸可改变浮窗大小（字号自适应）。
- **数字颜色**卡分两页：「逐位颜色」给六个数字单独配色；「渐变」把整串铺成一条色带（页签本身就是开关，切到哪页就是哪页的模式）。选中起点色 / 终点色时，那个色点会有一圈**扩散涟漪**做提示。
- **自定义任务**卡可以排「整点前 2 分钟自动开秒」这类定时，也能反过来在某个时段强制关秒；任务窗口内点击状态栏会提示已被任务接管。
- 切换通过全局触摸命中判定实现，**不会与“下拉通知栏”冲突**——纯点击切换、下拉滑动照常拉出通知栏。
- 配置页可查看当前状态、切换手势、逐位/渐变配色、对比网络时间并一键**同步**系统对时。

## 效果预览

<table><tr>
<td align="center" width="50%">
<img src="docs/screenshots/statusbar-seconds-off.jpg" alt="关闭显秒：`15:06`，小时十位「1」保持所选颜色" width="100%">
<br>
关闭显秒：`15:06`，小时十位「1」保持所选颜色
</td>
<td align="center" width="50%">
<img src="docs/screenshots/statusbar-seconds-on.jpg" alt="开启显秒：`15:06:03`，分钟与秒保持系统原色" width="100%">
<br>
开启显秒：`15:06:03`，分钟与秒保持系统原色
</td>
</tr></table>

## 工作原理

模块只 hook **SystemUI** 一个进程，采用稳妥的 legacy Xposed 范式，
**核心链路只走 AOSP 公开 API，私有类兼容可选、找不到即跳过**，因此不会破坏系统内部状态、不会触发 LSPosed 安全模式：

- **显秒**：hook 稳定的 `TextView.setText`（after）缓存系统在状态栏时间上给出的原始文本，
  再 hook `TextView.onDraw`（before）在系统已设好文本之后覆写显示 `原文本:ss`，并用 1 秒级 `Handler` 驱动刷新。追加的秒保持系统时间自身的文字色。
- **逐位上色**：给每个数字字符挂一段 `ForegroundColorSpan`（先剥掉外来 span，避免旧颜色残留）；冒号另挂一个 `CharacterStyle` 把基线抬高 0.11em —— 文本被切成多个绘制 run 后宿主字形的冒号位置会下沉，这是宿主的差异，不是模块画错。
- **渐变**：状态栏与浮窗用一个 `CharacterStyle` 在 `updateDrawState` 里 `setShader`（`LinearGradient`，色带宽度取文字实测宽度）；设置页预览卡改成**自绘**（`GradientTimeView` 在 `onDraw` 里 `drawText` 一次 + `CLAMP` 色带）——因为 ColorOS 15 的硬件文本渲染会把 shader 按绘制 run 量化，span 路径在预览卡上会出现「一个数字一个纯色」的跳变。
- **切换**：ColorOS 的通知面板会拦截状态栏区域的触摸，时间自身的 `OnTouchListener` 收不到事件。
  因此模块改为 hook 系统全局的 `View.dispatchTouchEvent`，仅当按下点落在已识别时间的屏幕边界内、且几乎无位移（点击）或短时间第二次点击（双击）时才翻转 `Settings.Secure` 开关；模块**不消费**任何触摸事件，下拉通知栏照常。
- **任务**：任务窗口用「本地时间是否落在窗口内」判定，窗口边界精确定时，另加 60 秒兜底重算；系统时间/时区变更会收到广播立即重算（`postDelayed` 按开机时长计时，改墙钟后旧定时器不再可信）。

## 省电说明

- **开启显秒时**：模块需要 1 秒级定时器驱动刷新（这是“显示秒”的固有开销，约 60 次/分钟重绘时间）。实测 SystemUI 单核占用：无显秒 7.0%、显秒 7.25%（60 秒窗口），差异小于同场景的切片波动。
- **关闭显秒时**：模块会**立即停止该定时器**（`stopTicker`），不再有任何每秒唤醒，也不再有跨进程读取，
  回到系统原本的“每分钟刷新一次”节奏，因此关闭显秒确实更省电。
- **掉色保色**路径是自适应的：一旦确认文本被系统重建，重绘间隔按 2s → 4s → … → 30s 退避，连续 5 分钟没有掉色就自动收工（`color-guard stopped`），不会长期空转。
- 状态采用**进程内缓存**（`sSecondsEnabled` 等），定时器不会每秒去读 `Settings.Secure`（避免每秒一次 IPC）。
- 任务兜底重算用的是 `Handler.postDelayed`，**不持有 WakeLock、不用 AlarmManager**，深睡时根本不会执行，唤醒次数为 0。
- 定时器持有的是视图的**弱引用**，且仅在视图 `isAttachedToWindow()` 时继续，视图销毁后自动退出。

## 构建

需要 **JDK 17+**（推荐 17 或 21）与 **Android SDK Platform 35**（Build-Tools 35 由 AGP 自动安装）。

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

## 备份与续开发（重要）

`ClockSeconds/` 目录中，**除 `build/`、`.gradle/` 外的所有文件**都要保留（源码部分仅约 300 KB）：

| 类别 | 文件 | 说明 |
|------|------|------|
| 构建脚本 | `settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`、`gradle.properties`、`keystore.properties` | 版本、依赖、编译参数、签名配置 |
| Gradle Wrapper | `gradlew`、`gradlew.bat`、`gradle/wrapper/*` | 锁定 Gradle 8.14.5 |
| 源码 | `app/src/main/java/**`（20 个 `.java`） | 模块逻辑、设置页、绘制类 |
| 资源 | `app/src/main/res/**` | 字符串（默认中文 + 21 种语言 values-xx）、主题 |
| Xposed 元数据 | `app/src/main/resources/META-INF/xposed/*` | 入口类、scope、API 版本 |
| 清单 | `app/src/main/AndroidManifest.xml` | 权限、Activity |
| 工具脚本 | `tools/**` | README 21 语言生成器、i18n 校验、发版脚本 |
| 文档 | `README*.md`、`docs/**`、`CHANGELOG.md` | 说明文档、截图、更新日志 |
| CI | `.github/workflows/*.yml` | 打 tag 出签名包 |

## 排查

<details>
<summary>展开排查步骤</summary>

查看 logcat：

- **Release 构建**已用 `BuildConfig.DEBUG` 门控剥除所有诊断日志（更小、更干净），logcat 中不会输出本模块日志。
- 如需排查，临时安装 **debug 构建**（`assembleDebug`，用同一生产密钥签名可直接覆盖安装），再过滤 TAG：

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- 若完全不显示秒：先确认 LSPosed 已勾选 SystemUI 作用域并重启；再检查开关是否被持久化为关闭——可在 adb 中读取 / 强制写入：

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=开 0=关
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- 若颜色/秒数「自己变回去」（掉色）：这是系统或第三方主题在重建文本，模块会自动重绘；若长期不恢复，debug 日志里搜 `color-guard stopped` / `span-theft` 确认是否进入了 5 分钟冷却。
- 若在非 ColorOS 机型上不生效：装 debug 版看日志里的 `candidate:` / `CONFIG CLOCK:` 输出，确认该机型状态栏时间的资源名与类名是否在识别规则内，若不在则补一条规则即可。

</details>

## 反馈与建议

有问题、建议或使用心得？欢迎到 [GitHub Issues](https://github.com/kelebao/ClockSeconds/issues) 留言，我会及时回复。

## License

[MIT](LICENSE) © kelebao
