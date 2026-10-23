package io.github.kelebao.colorosclockseconds;

import android.content.ContentResolver;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.provider.Settings;
import java.util.Locale;

/**
 * 模块配置与状态读写。
 *
 * <p>重写后模块只 hook {@code com.android.systemui} 一个进程（legacy Xposed API）。
 * 所有状态存放在 {@link Settings.Secure}——它是跨进程共享的唯一稳定通道，模块自身
 * 的 SharedPreferences 不跨进程、也读不到 SystemUI 内的值。</p>
 *
 * <p>写入 Secure 设置需要 {@code WRITE_SECURE_SETTINGS} 权限。该权限在 SystemUI 进程内
 * 天然具备（系统应用），因此「点击时钟切换显秒」由 SystemUI 内的 hook 完成；
 * 模块自身的设置页属于普通应用进程，若无该权限则只能读取状态、无法写入（页面会给出提示）。</p>
 *
 * <p>设计要点：彻底移除旧版的原生路径（ColorOS 17+ 去时限 hook）。那条路径在 ColorOS 15
 * 上同名类行为不同，hook 后会让 SystemUI 反复崩溃、触发 LSPosed 安全模式。新版统一用
 * 「hook TextView.setText / onDraw，在系统设完文本后覆写显示」的稳妥方式，绝不破坏系统内部状态。</p>
 */
public final class ClockSecondsConfig {

    public static final String SYSTEM_UI_PACKAGE = "com.android.systemui";

    /** 模块自身（设置页）包名，用于 SystemUI → 设置页的回传广播定向发送。 */
    public static final String MODULE_PACKAGE = "io.github.kelebao.colorosclockseconds";

    /**
     * 配置广播的 signature 级自定义权限（安全加固 F1）。
     *
     * <p>SystemUI 进程内注册的配置接收器以此作为 {@code broadcastPermission}，
     * 要求<b>发送方</b>持有该权限。设置页与磁贴和模块同签名，天然持有；其它已安装 App
     * 无本模块签名 → 无法广播代写 {@code Settings.Secure/Global}。</p>
     */
    public static final String CONFIG_PERMISSION =
            "io.github.kelebao.colorosclockseconds.permission.CONFIG";

    /**
     * 模块自有显秒开关（唯一主状态）。由于已无原生路径，所有版本都使用本键，
     * 由 SystemUI 内的 hook 在「进程内首次遇到时钟」与「用户点击切换」时读写。
     */
    public static final String MODULE_SECONDS_KEY = "coloros_clock_seconds_enabled";

    /**
     * 切换手势：0 = 单击时钟，2 = 双击时钟。长按固定用于弹出浮窗预览时钟，不再作为切换手势。
     * 历史值 1（旧版「长按切换」）读取时归一为单击。
     */
    public static final String TOGGLE_GESTURE_KEY = "coloros_clock_seconds_gesture";
    public static final int GESTURE_CLICK = 0;
    /** @deprecated 旧版长按切换手势，已废弃；读取时归一为 {@link #GESTURE_CLICK}。 */
    @Deprecated
    public static final int GESTURE_LONG_PRESS_LEGACY = 1;
    public static final int GESTURE_DOUBLE_TAP = 2;

    /** 小时十位数字颜色（ARGB）。默认 Material Red 500。 */
    public static final String HOUR_COLOR_KEY = "coloros_clock_seconds_hour_color";
    public static final int HOUR_COLOR_DEFAULT = 0xFFF44336;

    /** 小时颜色总开关（1 = 启用，0 = 关闭并恢复系统默认样式）。默认启用。 */
    public static final String HOUR_COLOR_ENABLED_KEY =
            "coloros_clock_seconds_hour_color_enabled";

    /**
     * 网络时间相对系统时间的偏移（毫秒，网络 - 系统），由设置页测得后写入。
     * SystemUI 内的浮窗用它显示网络时间，无需自己发网络请求。
     */
    public static final String NET_OFFSET_KEY = "coloros_clock_seconds_net_offset";

    /**
     * 浮窗几何记忆（左上角坐标与尺寸），由 SystemUI 内浮窗读写（系统进程天然具备写入权限）。
     * 缺省值为 -1，表示「未记忆」，浮窗按默认尺寸/位置显示。
     */
    public static final String FLOAT_X_KEY = "coloros_clock_seconds_float_x";
    public static final String FLOAT_Y_KEY = "coloros_clock_seconds_float_y";
    public static final String FLOAT_W_KEY = "coloros_clock_seconds_float_w";
    public static final String FLOAT_H_KEY = "coloros_clock_seconds_float_h";
    /** 浮窗几何记忆缺省值（未记忆）。 */
    public static final int FLOAT_GEOMETRY_DEFAULT = -1;

    /**
     * 自定义状态栏时钟资源名（可选）。用户若在设置页填写，则任何资源名「包含」该字符串的
     * TextView 都视为状态栏时钟（用于其它厂商机型未能被内置规则自动识别的情况）。
     * 空字符串表示「使用内置规则」。
     */
    public static final String CLOCK_RULE_KEY = "coloros_clock_seconds_clock_rule";

    /**
     * 设置页 → SystemUI 的配置广播。
     *
     * <p>设置页运行在普通应用进程，没有 {@code WRITE_SECURE_SETTINGS}；而模块在 SystemUI
     * 进程内天然具备该权限（点击切换显秒就是在这里写的）。所以设置页写入失败时改发广播，
     * 由 SystemUI 内的接收器完成写入——用户无需 adb 授权。</p>
     */
    public static final String ACTION_SET_ENABLED =
            "io.github.kelebao.colorosclockseconds.action.SET_ENABLED";
    /** 设置页 → SystemUI 写入自定义时钟识别名（用于其它厂商机型）。 */
    public static final String ACTION_SET_CLOCK_RULE =
            "io.github.kelebao.colorosclockseconds.action.SET_CLOCK_RULE";
    /** String extra：自定义时钟资源名（空 = 内置规则）。 */
    public static final String EXTRA_RULE = "rule";

    /**
     * 设置页 → SystemUI 触发「自动检测状态栏时钟识别名」。
     *
     * <p>用户无需知道时钟控件的资源名——由运行在 SystemUI 进程内的 hook 扫描当前正在显示
     * {@code HH:mm} 时间格式的 TextView，挑出最像状态栏时钟的那个，把它的资源名写回
     * {@link #CLOCK_RULE_KEY}（空串表示「内置规则已命中，无需自定义」），再以
     * {@link #ACTION_DETECT_CLOCK_RESULT} 回传结果。</p>
     */
    public static final String ACTION_DETECT_CLOCK =
            "io.github.kelebao.colorosclockseconds.action.DETECT_CLOCK";
    /** SystemUI → 设置页：回传自动检测到的时钟识别名（可能为空串，表示检测失败）。 */
    public static final String ACTION_DETECT_CLOCK_RESULT =
            "io.github.kelebao.colorosclockseconds.action.DETECT_CLOCK_RESULT";
    /** String extra：检测到的时钟资源名（空 = 检测失败）。 */
    public static final String EXTRA_DETECTED_RULE = "detected_rule";
    /** boolean extra：true = 命中内置规则（无需自定义，立即生效）；false = 写入自定义识别名（需重启 SystemUI）。 */
    public static final String EXTRA_DETECTED_BUILTIN = "detected_builtin";

    /** 设置页 → SystemUI 写入小时颜色。 */
    public static final String ACTION_SET_HOUR_COLOR =
            "io.github.kelebao.colorosclockseconds.action.SET_HOUR_COLOR";
    public static final String ACTION_SET_GESTURE =
            "io.github.kelebao.colorosclockseconds.action.SET_GESTURE";
    public static final String ACTION_SET_HOUR_COLOR_ENABLED =
            "io.github.kelebao.colorosclockseconds.action.SET_HOUR_COLOR_ENABLED";
    /** 一键对时：让 SystemUI 打开系统「自动设置时间」。 */
    public static final String ACTION_SYNC_TIME =
            "io.github.kelebao.colorosclockseconds.action.SYNC_TIME";
    /** 设置页 → SystemUI 写入网络时间偏移（浮窗显示网络时间用）。 */
    public static final String ACTION_SET_NET_OFFSET =
            "io.github.kelebao.colorosclockseconds.action.SET_NET_OFFSET";
    /** long extra：网络 - 系统的偏移（毫秒）。 */
    public static final String EXTRA_NET_OFFSET =
            "io.github.kelebao.colorosclockseconds.extra.NET_OFFSET";
    public static final String EXTRA_COLOR = "color";
    public static final String EXTRA_GESTURE = "gesture";
    public static final String EXTRA_ENABLED = "enabled";

    /** 开关「开启」取值。 */
    public static final int MODE_ENABLED = 1;

    private ClockSecondsConfig() {
    }

    /**
     * 读取当前是否开启显秒。默认返回开启（1），使模块装上即生效、状态栏时钟直接显示秒；
     * 用户点击时钟即可关闭。
     */
    public static boolean isEnabled(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, MODULE_SECONDS_KEY, MODE_ENABLED) == MODE_ENABLED;
    }

    /** 切换显秒开关（仅写入模块键）。运行在 SystemUI 进程内时具备写入权限。 */
    public static void setEnabled(ContentResolver resolver, boolean enabled) {
        Settings.Secure.putInt(resolver, MODULE_SECONDS_KEY, enabled ? MODE_ENABLED : 0);
    }

    /** 读取切换手势偏好（默认单击；旧版「长按切换」值归一为单击）。 */
    public static int getGesture(ContentResolver resolver) {
        int g = Settings.Secure.getInt(resolver, TOGGLE_GESTURE_KEY, GESTURE_CLICK);
        return (g == GESTURE_DOUBLE_TAP) ? GESTURE_DOUBLE_TAP : GESTURE_CLICK;
    }

    /** 写入切换手势偏好。 */
    public static void setGesture(ContentResolver resolver, int gesture) {
        Settings.Secure.putInt(resolver, TOGGLE_GESTURE_KEY, gesture);
    }

    /** 读取小时十位数字颜色（默认红色）。 */
    public static int getHourColor(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, HOUR_COLOR_KEY, HOUR_COLOR_DEFAULT);
    }

    /** 写入小时十位数字颜色（ARGB）。 */
    public static void setHourColor(ContentResolver resolver, int color) {
        Settings.Secure.putInt(resolver, HOUR_COLOR_KEY, color);
    }

    /** 小时颜色开关是否启用（默认启用）。关闭后时钟恢复系统默认样式。 */
    public static boolean isHourColorEnabled(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, HOUR_COLOR_ENABLED_KEY, 1) == 1;
    }

    /** 写入小时颜色开关。 */
    public static void setHourColorEnabled(ContentResolver resolver, boolean enabled) {
        Settings.Secure.putInt(resolver, HOUR_COLOR_ENABLED_KEY, enabled ? 1 : 0);
    }

    // ------------------------------------------------------------------
    // 背景板「呼吸灯」渐变（只用于浮窗背景板与设置页预览背景板，不用于文字、不用于状态栏；
    // 常开、无开关）：深色底 + 一枚柔和的半弧光晕，像呼吸灯一样随时间平滑明暗，并极缓慢漂移。
    // ------------------------------------------------------------------

    /** 呼吸周期（毫秒）：约 6 秒一次完整明暗往返，短周期让极值处的「停顿」不易察觉。 */
    public static final long BREATH_PERIOD_MS = 6_000L;
    /** 光晕漂移一圈的时长（毫秒）：极缓慢，保证光晕始终在轻微移动、绝不静止发闷。 */
    private static final long GLOW_DRIFT_PERIOD_MS = 30_000L;
    /** 小时颜色被关闭时的光晕替代色（柔和天蓝）。 */
    public static final int BREATH_ACCENT_FALLBACK = 0xFF64B5F6;

    /**
     * 当前时刻的光晕强度（0..1）：平滑正弦呼吸，明 → 暗 → 明。
     *
     * <p>用余弦保证在峰值/谷值处一阶导为 0，过渡丝滑不闪烁；周期较短（6s）以减轻
     * 「在极值停顿太久」的手感。</p>
     *
     * @param nowMs 当前时间戳（毫秒）
     */
    public static float breathIntensity(long nowMs) {
        double p = Math.floorMod(nowMs, BREATH_PERIOD_MS) / (double) BREATH_PERIOD_MS;
        return (float) ((1 - Math.cos(p * 2 * Math.PI)) / 2); // 0..1..0
    }

    /** 光晕中心相对板中心的横向漂移（归一化，小幅）：极缓慢移动，形成柔和的半弧。 */
    public static float glowOffsetX(long nowMs) {
        double p = Math.floorMod(nowMs, GLOW_DRIFT_PERIOD_MS) / (double) GLOW_DRIFT_PERIOD_MS;
        return (float) (Math.cos(p * 2 * Math.PI) * 0.16);
    }

    /** 光晕中心相对板中心的纵向漂移（归一化，小幅）。 */
    public static float glowOffsetY(long nowMs) {
        double p = Math.floorMod(nowMs, GLOW_DRIFT_PERIOD_MS) / (double) GLOW_DRIFT_PERIOD_MS;
        return (float) (Math.sin(p * 2 * Math.PI) * 0.10);
    }

    /** 取光晕强调色：小时颜色启用时用小时颜色，否则用柔和天蓝。 */
    public static int glowAccent(boolean hourColorEnabled, int hourColor) {
        return hourColorEnabled ? hourColor : BREATH_ACCENT_FALLBACK;
    }

    /**
     * 构造背景板的「呼吸灯」半弧渐变（{@link RadialGradient}）。
     *
     * <p>深色底板上放一枚偏在一侧的柔光：中心不在正中 → 亮区沿一侧铺开、呈半弧形（而非
     * 呆板的正中圆斑）；亮度与半径随 {@link #breathIntensity} 平滑起伏 → 像呼吸灯；中心再叠加
     * 一层极缓慢的漂移 → 全程有细微流动、不会在明暗两端「停顿太久」，整体丝滑。</p>
     *
     * <p>仅作用于背景板 drawable；秒数字与状态栏文字始终保持系统原色。</p>
     *
     * @param plateW,plateH 背景板像素宽高（决定光晕尺寸）
     * @param baseColor     底色（光晕之外的部分）
     * @param accent        光晕颜色（通常为小时颜色）
     * @param nowMs         当前时间戳（毫秒）
     */
    public static RadialGradient buildPlateGlow(int plateW, int plateH, int baseColor,
                                                int accent, long nowMs) {
        int w = Math.max(1, plateW);
        int h = Math.max(1, plateH);
        float inten = breathIntensity(nowMs);
        // 中心基础偏右（形成向右铺开的半弧高光），叠加极缓慢漂移。
        float cx = w * (0.60f + glowOffsetX(nowMs));
        float cy = h * (0.50f + glowOffsetY(nowMs));
        // 半径随呼吸轻微起伏：亮时晕开、暗时收拢。
        float radius = Math.max(w, h) * (0.55f + 0.22f * inten);
        int rgb = accent & 0x00FFFFFF;
        // 亮度调制：中心 alpha 在 0x1A（暗）..0x66（亮）之间平滑往返。
        int coreA = Math.round(0x1A + (0x66 - 0x1A) * inten);
        int core = (rgb) | (coreA << 24);
        int mid = (rgb) | (Math.round(coreA * 0.32f) << 24);
        // 边缘完全透明 → 光晕作为覆盖层透出底板的圆角底色，呼吸灯质感。
        return new RadialGradient(cx, cy, radius,
                new int[]{core, mid, 0x00000000},
                new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP);
    }

    /** 读取网络时间偏移（毫秒，网络 - 系统）。无记录时为 0（按系统时间显示）。 */
    public static long getNetOffset(ContentResolver resolver) {
        return Settings.Secure.getLong(resolver, NET_OFFSET_KEY, 0L);
    }

    /** 写入网络时间偏移（毫秒，网络 - 系统）。 */
    public static void setNetOffset(ContentResolver resolver, long offsetMs) {
        Settings.Secure.putLong(resolver, NET_OFFSET_KEY, offsetMs);
    }

    // ==================================================================
    // 浮窗几何记忆（SystemUI 进程内读写；普通应用进程读不到也写不进，需经广播）
    // ==================================================================

    /** 读取浮窗左上角 X（px）。未记忆返回 {@link #FLOAT_GEOMETRY_DEFAULT}。 */
    public static int getFloatX(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, FLOAT_X_KEY, FLOAT_GEOMETRY_DEFAULT);
    }

    /** 读取浮窗左上角 Y（px）。 */
    public static int getFloatY(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, FLOAT_Y_KEY, FLOAT_GEOMETRY_DEFAULT);
    }

    /** 读取浮窗宽（px）。 */
    public static int getFloatW(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, FLOAT_W_KEY, FLOAT_GEOMETRY_DEFAULT);
    }

    /** 读取浮窗高（px）。 */
    public static int getFloatH(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, FLOAT_H_KEY, FLOAT_GEOMETRY_DEFAULT);
    }

    /** 写入浮窗几何记忆（仅在 SystemUI 进程内调用，具备写入权限）。 */
    public static void setFloatGeometry(ContentResolver resolver,
                                        int x, int y, int w, int h) {
        Settings.Secure.putInt(resolver, FLOAT_X_KEY, x);
        Settings.Secure.putInt(resolver, FLOAT_Y_KEY, y);
        Settings.Secure.putInt(resolver, FLOAT_W_KEY, w);
        Settings.Secure.putInt(resolver, FLOAT_H_KEY, h);
    }

    /** 清除浮窗几何记忆（恢复默认尺寸/位置）。 */
    public static void clearFloatGeometry(ContentResolver resolver) {
        Settings.Secure.putInt(resolver, FLOAT_X_KEY, FLOAT_GEOMETRY_DEFAULT);
        Settings.Secure.putInt(resolver, FLOAT_Y_KEY, FLOAT_GEOMETRY_DEFAULT);
        Settings.Secure.putInt(resolver, FLOAT_W_KEY, FLOAT_GEOMETRY_DEFAULT);
        Settings.Secure.putInt(resolver, FLOAT_H_KEY, FLOAT_GEOMETRY_DEFAULT);
    }

    // ==================================================================
    // 自定义时钟识别名（用于其它厂商机型）
    // ==================================================================

    /** 读取自定义时钟资源名；空字符串表示「使用内置规则」。 */
    public static String getClockRule(ContentResolver resolver) {
        String v = Settings.Secure.getString(resolver, CLOCK_RULE_KEY);
        return v == null ? "" : v;
    }

    /** 写入自定义时钟资源名（空 = 内置规则）。 */
    public static void setClockRule(ContentResolver resolver, String rule) {
        Settings.Secure.putString(resolver, CLOCK_RULE_KEY, rule == null ? "" : rule);
    }

    // ==================================================================
    // 纯函数（可单元测试，不依赖 Android 运行时）
    // ==================================================================

    /** 归一化切换手势：旧版「长按切换」(1) 与任何非法值统一归为单击 (0)。 */
    public static int normalizeGesture(int gesture) {
        return (gesture == GESTURE_DOUBLE_TAP) ? GESTURE_DOUBLE_TAP : GESTURE_CLICK;
    }

    /** 秒数补零后缀：例如 7 → ":07"；越界值按 0 处理。 */
    public static String secondsSuffix(int second) {
        int s = second;
        if (s < 0 || s > 59) {
            s = 0;
        }
        return String.format(Locale.US, ":%02d", s);
    }

    /** 文本开头是否为「两位数字」小时（用于仅标红十位，如 13 的 1、05 的 0）。 */
    public static boolean hasTwoDigitHour(CharSequence text) {
        if (text == null || text.length() < 2) {
            return false;
        }
        return Character.isDigit(text.charAt(0)) && Character.isDigit(text.charAt(1));
    }
}
