package io.github.kelebao.colorosclockseconds;

import android.content.ContentResolver;
import android.provider.Settings;

/**
 * 模块配置与状态读写。
 *
 * <p>所有状态都存放在 {@link Settings.Secure}，原因是 hook 分别运行在
 * {@code com.android.systemui} 与 {@code com.android.settings} 两个独立进程中，
 * 只有 Secure 设置是跨进程共享的通道；模块自身的 SharedPreferences 不跨进程。</p>
 *
 * <p>写入 Secure 设置需要 {@code WRITE_SECURE_SETTINGS} 权限。该权限在
 * SystemUI 进程内天然具备（系统应用），因此"点击时钟切换"由 SystemUI 内的 hook 完成；
 * 模块设置页属于普通应用进程，若无该权限则只能读取状态、无法写入（页面会给出提示）。</p>
 */
public final class ClockSecondsConfig {

    public static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    public static final String SETTINGS_PACKAGE = "com.android.settings";

    /** ColorOS 17+ 原生"状态栏时钟显秒"开关（系统键，写入后系统 UI 会同步）。 */
    public static final String NATIVE_SECONDS_KEY = "oplus_status_bar_clock_seconds_mode";

    /**
     * 模块自有开关。ColorOS 15/16 没有原生显秒能力，使用本键作为主状态，
     * 由模块自行渲染秒。ColorOS 17+ 也会镜像写入本键，保证两条路径状态一致。
     */
    public static final String MODULE_SECONDS_KEY = "coloros_clock_seconds_enabled";

    /** 切换手势：0 = 单击时钟，1 = 长按时钟。 */
    public static final String TOGGLE_GESTURE_KEY = "coloros_clock_seconds_gesture";
    public static final int GESTURE_CLICK = 0;
    public static final int GESTURE_LONG_PRESS = 1;

    /** 原生开关的"开启"取值。 */
    public static final int MODE_ENABLED = 1;

    /** ColorOS 原生显秒控制器类名（存在即认为系统具备原生能力）。 */
    public static final String NATIVE_CONTROLLER_CLASS =
            "com.oplus.systemui.statusbar.clock.ClockSecondsController";

    /** 状态栏时钟视图的候选类名，按优先级从"ColorOS 专有"到"AOSP 通用"排列。 */
    public static final String[] CLOCK_CLASS_CANDIDATES = {
            "com.oplus.systemui.statusbar.clock.OplusClockTextView",
            "com.oplus.systemui.statusbar.clock.OplusClock",
            "com.oplus.systemui.statusbar.clock.OplusClockView",
            "com.oplus.systemui.statusbar.policy.OplusClock",
            "com.android.systemui.statusbar.policy.Clock",
            "com.android.systemui.statusbar.phone.StatusBarClock",
    };

    private ClockSecondsConfig() {
    }

    /** 反射探测系统是否具备 ColorOS 17+ 的原生显秒能力。 */
    public static boolean isNativeSupported(ClassLoader classLoader) {
        try {
            Class.forName(NATIVE_CONTROLLER_CLASS, false, classLoader);
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    /** 读取当前是否开启显秒。 */
    public static boolean isEnabled(ContentResolver resolver, boolean nativeSupported) {
        String key = nativeSupported ? NATIVE_SECONDS_KEY : MODULE_SECONDS_KEY;
        return Settings.Secure.getInt(resolver, key, 0) == MODE_ENABLED;
    }

    /** 写入显秒开关；ColorOS 17+ 会同时镜像模块键，保证两路状态一致。 */
    public static void setEnabled(ContentResolver resolver, boolean enabled, boolean nativeSupported) {
        int value = enabled ? 1 : 0;
        Settings.Secure.putInt(resolver, MODULE_SECONDS_KEY, value);
        if (nativeSupported) {
            Settings.Secure.putInt(resolver, NATIVE_SECONDS_KEY, value);
        }
    }

    /** 读取切换手势偏好（默认单击）。 */
    public static int getGesture(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, TOGGLE_GESTURE_KEY, GESTURE_CLICK);
    }

    /** 写入切换手势偏好。 */
    public static void setGesture(ContentResolver resolver, int gesture) {
        Settings.Secure.putInt(resolver, TOGGLE_GESTURE_KEY, gesture);
    }
}
