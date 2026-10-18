<div align="center">

English · **简体中文**

</div>

<div align="center">

# 时钟显秒（Clock Seconds）

让状态栏时钟显示**秒**，单击即切换，小时十位数**常驻标红**。<br>
不依赖任何厂商私有类，已在 ColorOS 15 真机验证，同时覆盖小米、一加、荣耀、vivo 等主流厂商。

![version](https://img.shields.io/badge/version-1.0.4-blue.svg?style=flat-square)
![android](https://img.shields.io/badge/Android-10%2B-brightgreen.svg?style=flat-square)
![lsposed](https://img.shields.io/badge/LSPosed-module-orange.svg?style=flat-square)
![coloros](https://img.shields.io/badge/ColorOS%2015-verified-success.svg?style=flat-square)
![stars](https://img.shields.io/github/stars/kelebao/ColorOS15ClockSeconds.svg?style=flat-square)
![license](https://img.shields.io/badge/license-MIT-lightgrey.svg?style=flat-square)

</div>

---

## 功能

| 功能 | 说明 |
|------|------|
| 状态栏显秒 | 状态栏时钟显示到秒（`HH:mm:ss` / `h:mm:ss a`，跟随系统 12/24 小时制） |
| 点击切换 | **单击**状态栏时钟即切换显秒开/关；也可在设置页改为**长按** |
| 小时红色 | 把小时「十位数」标红（如 `13` 的 `1`、`05` 的 `0`），分钟与秒保持系统原色；单位数小时（如 `9:45`）无十位不标红。所有厂商机型均生效 |
| 界面语言 | 应用内界面已适配 **21 种语言**（跟随系统语言自动切换）：简体中文、English、繁體中文、日本語、한국어、Türkçe、Русский、Tiếng Việt、ไทย、Deutsch、Español、Français、Українська、Polski、Português (Brasil)、فارسی、العربية、Bahasa Indonesia、Italiano、Ελληνικά、ქართული |
| 持久化 | 状态存于 `Settings.Secure`，跨进程共享，重启 SystemUI 后保持 |
| 适配范围 | ColorOS 15 / 16 / 17（OPPO / 一加 / 真我）已真机验证；小米 / 荣耀 / 红米 等有 root 的其它厂商同步生效。不依赖任何厂商私有类，避免触发 LSPosed 安全模式。最低支持 Android 10 |

## 安装

1. 从 [**Releases**](https://github.com/kelebao/ColorOS15ClockSeconds/releases/latest) 下载最新版 APK
2. 在 **LSPosed** 中启用本模块，作用域勾选：**系统界面（`com.android.systemui`）**
3. 重启 SystemUI 进程或重启设备
4. 配置页从 **LSPosed 管理器**打开（桌面不生成图标）

> 需要 root（LSPosed 框架）。写入 `Settings.Secure` 需授予 `WRITE_SECURE_SETTINGS`：
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 未授予时仍可点击状态栏时钟完成切换（该动作运行在 SystemUI 进程内，天然具备权限）。

## 使用

- **单击状态栏时钟**：切换显秒开/关（默认手势）。
- 切换通过全局触摸命中判定实现，**不会与"下拉通知栏"冲突**——纯点击切换、下拉滑动照常拉出通知栏；若你更习惯，也可在配置页把切换手势改为**长按**。
- 配置页可查看当前状态、切换手势、刷新状态。

## 工作原理

模块只 hook **SystemUI** 一个进程，采用稳妥的 legacy Xposed 范式，
**完全不触碰任何厂商私有类**，因此不会破坏系统内部状态、不会触发 LSPosed 安全模式：

- **显秒**：hook 稳定的 `TextView.setText`（after）缓存系统在状态栏时钟上给出的原始文本，
  再 hook `TextView.onDraw`（before）在系统已设好文本之后覆写显示 `原文本:ss`，
  并用 1 秒级 `Handler` 驱动刷新。追加的秒保持系统时钟自身的文字色（实测为白色 `#ccffffff`）。
  同时把「小时十位数」标红（给该字符追加一段红色 `ForegroundColorSpan`），无论显秒开启或关闭均保持红色；
  分钟与秒不标红；单位数小时无十位则不标红。
- **切换**：ColorOS 的通知面板会拦截状态栏区域的触摸，时钟自身的 `OnTouchListener` 收不到事件。
  因此模块改为 hook 系统全局的 `View.dispatchTouchEvent`，仅当按下点落在已识别时钟的屏幕边界内、
  且几乎无位移（点击）或按住 500ms（长按）时才翻转 `Settings.Secure` 开关；模块**不消费**
  任何触摸事件，下拉通知栏照常。

## 省电说明

- **开启显秒时**：模块需要 1 秒级定时器驱动刷新（这是"显示秒"的固有开销，约 60 次/分钟重绘时钟）。
- **关闭显秒时**：模块会**立即停止该定时器**（`stopTicker`），不再有任何每秒唤醒，也不再有跨进程读取，
  回到系统原本的"每分钟刷新一次"节奏，因此关闭显秒确实更省电。
- 状态采用**进程内缓存**（`sSecondsEnabled`），定时器不会每秒去读 `Settings.Secure`（避免每秒一次 IPC）。
- 模块不构造任何时间格式化器，直接复用系统给出的原文本并仅追加 `:ss`，因此没有格式化器开销。
- 定时器持有的是视图的**弱引用**，且仅在视图 `isAttachedToWindow()` 时继续，视图销毁后自动退出。

## 构建

需要 **JDK 17+**（推荐 17 或 21）与 **Android SDK Platform 35**（Build-Tools 35 由 AGP 自动安装）。

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

<details>
<summary>国内网络（Google Maven 不可达）</summary>

若 `dl.google.com/dl/android/maven2` 连不通（AGP / AndroidX 会拉取失败），
在 `~/.gradle/init.gradle` 加入阿里云镜像即可（不修改项目文件）：

```groovy
settingsEvaluated { settings ->
    settings.pluginManagement.repositories {
        maven { url 'https://maven.aliyun.com/repository/gradle-plugin' }
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
    }
    settings.dependencyResolutionManagement.repositories {
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/public' }
    }
}
```

> SDK 仓库（`dl.google.com/android/repository`）一般仍可访问，缺失的 Platform/Build-Tools 会由 AGP 自动下载。

</details>

## 备份与续开发（重要）

### 必须保留的文件

`ColorOS15ClockSeconds/` 目录中，**除 `build/`、`.gradle/` 外的所有文件**都要保留（源码部分仅约 140 KB）：

| 类别 | 文件 | 说明 |
|------|------|------|
| 构建脚本 | `settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`、`gradle.properties` | 版本、依赖、编译参数 |
| Gradle Wrapper | `gradlew`、`gradlew.bat`、`gradle/wrapper/*` | 锁定 Gradle 8.14.5 |
| 源码 | `app/src/main/java/**`（3 个 `.java`） | 模块逻辑主体 |
| 资源 | `app/src/main/res/**` | 字符串（默认中文 + 21 种语言 values-xx）、主题 |
| Xposed 元数据 | `app/src/main/resources/META-INF/xposed/*` | 入口类、scope、API 版本 |
| 清单 | `app/src/main/AndroidManifest.xml` | 权限、Activity |
| 文档 | `README.md`、`README_EN.md` | 中文 / 英文说明 |
| CI | `.github/workflows/release.yml` | 打 tag 出签名包 |

## 排查

<details>
<summary>展开排查步骤</summary>

查看 logcat：

- **Release 构建**已用 `BuildConfig.DEBUG` 门控剥除所有诊断日志（更小、更干净），logcat 中不会输出本模块日志。
- 如需排查，临时安装 **debug 构建**（`assembleDebug`，用同一生产密钥签名可直接覆盖安装），再过滤 TAG：

  ```bash
  adb logcat -s ColorOSClockSeconds
  ```

- 若完全不显示秒：先确认 LSPosed 已勾选 SystemUI 作用域并重启；再检查开关是否被持久化为关闭——
  可在 adb 中读取 / 强制写入：

  ```bash
  adb shell settings get secure coloros_clock_seconds_enabled   # 1=开 0=关
  adb shell settings put  secure coloros_clock_seconds_enabled 1
  ```

- 若在非 ColorOS 机型上不生效：装 debug 版看日志里的 `candidate:` / `CONFIG CLOCK:` 输出，
  确认该机型状态栏时钟的资源名与类名是否在识别规则内，若不在则补一条规则即可。

</details>

## License

[MIT](LICENSE) © kelebao
