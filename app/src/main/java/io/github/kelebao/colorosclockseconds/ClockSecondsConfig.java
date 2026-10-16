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

    /** 切换手势：0 = 单击时钟，1 = 长按时钟。 */
    public static final String TOGGLE_GESTURE_KEY = "coloros_clock_seconds_gesture";
    public static final int GESTURE_CLICK = 0;
    public static final int GESTURE_LONG_PRESS = 1;

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

    /** 读取切换手势偏好（默认单击）。 */
    public static int getGesture(ContentResolver resolver) {
        return Settings.Secure.getInt(resolver, TOGGLE_GESTURE_KEY, GESTURE_CLICK);
    }

    /** 写入切换手势偏好。 */
    public static void setGesture(ContentResolver resolver, int gesture) {
        Settings.Secure.putInt(resolver, TOGGLE_GESTURE_KEY, gesture);
    }
}
