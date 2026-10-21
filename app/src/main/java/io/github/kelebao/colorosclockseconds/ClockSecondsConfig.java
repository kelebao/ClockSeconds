package io.github.kelebao.colorosclockseconds;

import android.content.ContentResolver;
import android.provider.Settings;

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
     * 设置页 → SystemUI 的配置广播。
     *
     * <p>设置页运行在普通应用进程，没有 {@code WRITE_SECURE_SETTINGS}；而模块在 SystemUI
     * 进程内天然具备该权限（点击切换显秒就是在这里写的）。所以设置页写入失败时改发广播，
     * 由 SystemUI 内的接收器完成写入——用户无需 adb 授权。</p>
     */
    public static final String ACTION_SET_HOUR_COLOR =
            "io.github.kelebao.colorosclockseconds.action.SET_HOUR_COLOR";
    public static final String ACTION_SET_GESTURE =
            "io.github.kelebao.colorosclockseconds.action.SET_GESTURE";
    public static final String ACTION_SET_HOUR_COLOR_ENABLED =
            "io.github.kelebao.colorosclockseconds.action.SET_HOUR_COLOR_ENABLED";
    /** 一键对时：让 SystemUI 打开系统「自动设置时间」。 */
    public static final String ACTION_SYNC_TIME =
            "io.github.kelebao.colorosclockseconds.action.SYNC_TIME";
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
}
