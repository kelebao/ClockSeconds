# ColorOS 时钟显秒（ColorOS Clock Seconds）

一个 LSPosed 模块：让状态栏（左上角）时钟显示**秒**，并支持**点击时钟切换显秒开关**，适配 **ColorOS 15 及以上**（OPPO / 一加 / 真我）。

> 本模块基于开源项目 [`coolzyd9107/oplus_coloros17_status_bar_force_show_seconds`](https://github.com/coolzyd9107/oplus_coloros17_status_bar_force_show_seconds)（仅适配 ColorOS 17、去除原生显秒的 5 分钟时限）扩展而来，新增了"点击切换"与"ColorOS 15/16 自渲染显秒"能力。
>
> 本仓库：<https://github.com/kelebao/ColorOS15ClockSeconds>
>
> ✅ **已在 ColorOS 15 真机实测可用**（状态栏显秒、点击切换、下拉通知栏均正常）。

---

## 下载安装

从 [**Releases**](https://github.com/kelebao/ColorOS15ClockSeconds/releases/latest) 页面下载最新版 `app-release.apk`，然后：

1. 安装 APK（需允许"安装未知来源应用"）
2. 在 **LSPosed** 中启用本模块
3. 作用域勾选：**系统界面（`com.android.systemui`）** 与 **设置（`com.android.settings`）**
4. 重启这两个进程（或重启手机）

## 功能

| 功能 | 说明 |
|------|------|
| 状态栏显秒 | 状态栏时钟显示到秒（`HH:mm:ss` / `h:mm:ss a`，跟随系统 12/24 小时制） |
| 点击切换 | **单击**状态栏时钟即切换显秒开/关；也可在设置页改为**长按** |
| 持久化 | 状态存于 `Settings.Secure`，跨进程共享，重启 SystemUI 后保持 |
| ColorOS 17+ | 复用系统原生显秒能力，并去掉"5 分钟后自动关闭"的限制 |
| ColorOS 15/16 | 系统无原生能力，模块在 SystemUI 内自渲染秒并驱动 1 秒级刷新 |

## 工作原理

模块用**运行时能力探测**选择渲染路径：

- **原生路径（ColorOS 17+）**：检测到 `com.oplus.systemui.statusbar.clock.ClockSecondsController` 时，
  hook 其 `isOplusFiveMinutesActive` / `scheduleExpiryLocked` / `onUserOrModeChanged`，
  使显秒不再 5 分钟后自动关闭。
- **自渲染路径（ColorOS 15/16）**：未检测到原生能力时，定位状态栏时钟视图，
  接管其时间格式化（`getSmallTime`，缺失时退化为 hook `updateClock` 后直接改文本），
  并用 1 秒级 `Handler` 驱动刷新。

两条路径共用"点击/长按时钟 → 翻转 `Settings.Secure` 开关 → 立即刷新"的交互。

## 安装

1. 在 **LSPosed** 中启用本模块，作用域勾选：
   - System UI：`com.android.systemui`
   - 设置：`com.android.settings`
2. 重启这两个进程或重启设备。
3. 配置页从 **LSPosed 管理器**打开（桌面不生成图标）。

## 使用

- **单击状态栏时钟**：切换显秒开/关（默认手势）。
- 若单击与"下拉通知栏"冲突，进入配置页把切换手势改为**长按**。
- 配置页可查看当前状态、切换手势、刷新状态。

> 配置页写入 `Settings.Secure` 需要 `WRITE_SECURE_SETTINGS`。普通应用需手动授予：
> ```bash
> adb shell pm grant io.github.kelebao.colorosclockseconds android.permission.WRITE_SECURE_SETTINGS
> ```
> 未授予时配置页仍可读取状态，但请用"点击状态栏时钟"完成切换
> （该动作运行在 SystemUI 进程内，天然具备权限）。

## 省电说明

- **开启显秒时**：模块需要 1 秒级定时器驱动刷新（这是"显示秒"的固有开销，约 60 次/分钟重绘时钟）。
- **关闭显秒时**：模块会**立即停止该定时器**（`stopTicker`），不再有任何每秒唤醒，也不再有跨进程读取，
  回到系统原本的"每分钟刷新一次"节奏，因此关闭显秒确实更省电。
- 状态采用**进程内缓存**（`sSecondsEnabled`），定时器不会每秒去读 `Settings.Secure`（避免每秒一次 IPC）。
- 时间格式化器按 12/24 小时制 pattern 缓存复用，不会每秒 `new SimpleDateFormat`。
- 定时器持有的是视图的**弱引用**，且仅在视图 `isAttachedToWindow()` 时继续，视图销毁后自动退出。

## 构建

需要 **JDK 17+**（推荐 17 或 21）与 **Android SDK Platform 35**（Build-Tools 35 由 AGP 自动安装）。

```bash
export JAVA_HOME=/path/to/jdk-17-or-21
./gradlew assembleDebug
```

### 国内网络（Google Maven 不可达）

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

## 备份与续开发（重要）

### 必须保留的文件

`ColorOS15ClockSeconds/` 目录中，**除 `build/`、`.gradle/` 外的所有文件**都要保留（源码部分仅约 140 KB）：

| 类别 | 文件 | 说明 |
|------|------|------|
| 构建脚本 | `settings.gradle.kts`、`build.gradle.kts`、`app/build.gradle.kts`、`gradle.properties` | 版本、依赖、编译参数 |
| Gradle Wrapper | `gradlew`、`gradlew.bat`、`gradle/wrapper/*` | 锁定 Gradle 8.14.5 |
| 源码 | `app/src/main/java/**`（3 个 `.java`） | 模块逻辑主体 |
| 资源 | `app/src/main/res/**` | 字符串、主题 |
| Xposed 元数据 | `app/src/main/resources/META-INF/xposed/*` | 入口类、scope、API 版本 |
| 清单 | `app/src/main/AndroidManifest.xml` | 权限、Activity |
| 文档 | `README.md` | 安装 / 构建 / 省电说明 |
| CI | `.github/workflows/release.yml` | 打 tag 出签名包 |

## 排查

查看 LSPosed 日志与 logcat，过滤 TAG：

```bash
adb logcat -s ColorOSClockSeconds
```
