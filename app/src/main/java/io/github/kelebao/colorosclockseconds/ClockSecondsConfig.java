package io.github.kelebao.colorosclockseconds;

import android.content.ContentResolver;
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
     * 测试卡片（诊断用）：模块检测到的候选时钟清单（JSON 数组
     * {@code [{"k":key,"l":label}, ...]}），由 SystemUI 写入、设置页读取展示。
     * key = 类名|资源名|父容器类名，label = 资源名 · 简类名。
     */
    public static final String CLOCK_TEST_LIST_KEY = "coloros_clock_seconds_clock_list";

    /**
     * 测试卡片：用户对候选时钟的启用/停用选择（JSON 对象 {@code {key:bool}}）。
     * 由 SystemUI 经广播写入；模块据此决定是否接管该时钟（缺省视为启用）。
     */
    public static final String CLOCK_TEST_ENABLED_KEY = "coloros_clock_seconds_clock_enabled";

    /** 设置页 → SystemUI：切换某个候选时钟的启用状态（配合 {@link #EXTRA_CLOCK_KEY} / {@link #EXTRA_CLOCK_ENABLED}）。 */
    public static final String ACTION_SET_CLOCK_TEST =
            "io.github.kelebao.colorosclockseconds.action.SET_CLOCK_TEST";
    /** String extra：候选时钟的稳定 key（类名|资源名|父容器类名）。 */
    public static final String EXTRA_CLOCK_KEY = "clock_key";
    /** int extra：1 = 启用（接管显秒/标红），0 = 停用。 */
    public static final String EXTRA_CLOCK_ENABLED = "clock_enabled";
    /**
     * String extra：设置页写入的「全部候选时钟启用选择」JSON 对象 {@code {key:bool}}。
     * 与单键 {@link #EXTRA_CLOCK_KEY} / {@link #EXTRA_CLOCK_ENABLED} 并存：SystemUI 优先采用本
     * extra 作为权威全量映射（避免「设置页无 WRITE_SECURE_SETTINGS 时只发单键、丢失其它勾选状态」）。
     */
    public static final String EXTRA_CLOCK_ENABLED_MAP = "clock_enabled_map";

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

    /**
     * 设置页 → SystemUI：仅重新扫描并发布候选时钟清单（测试卡片「刷新列表」用）。
     *
     * <p>与 {@link #ACTION_DETECT_CLOCK}（改识别规则）分离，避免刷新列表时误触发「自动检测」
     * 的 Toast 与规则写入。模块收到后强制重发 {@link #CLOCK_TEST_LIST_KEY}（忽略节流），
     * 再以 {@link #ACTION_REFRESH_CLOCK_LIST_RESULT} 回传，设置页据此重读并刷新 UI。</p>
     */
    public static final String ACTION_REFRESH_CLOCK_LIST =
            "io.github.kelebao.colorosclockseconds.action.REFRESH_CLOCK_LIST";
    /** SystemUI → 设置页：候选清单已重新发布到 Secure，可重读。 */
    public static final String ACTION_REFRESH_CLOCK_LIST_RESULT =
            "io.github.kelebao.colorosclockseconds.action.REFRESH_CLOCK_LIST_RESULT";

    /** 设置页 → SystemUI 写入小时颜色。 */
    public static final String ACTION_SET_HOUR_COLOR =
            "io.github.kelebao.colorosclockseconds.action.SET_HOUR_COLOR";
    public static final String ACTION_SET_GESTURE =
            "io.github.kelebao.colorosclockseconds.action.SET_GESTURE";
    public static final String ACTION_SET_HOUR_COLOR_ENABLED =
            "io.github.kelebao.colorosclockseconds.action.SET_HOUR_COLOR_ENABLED";
    /** 设置页 → SystemUI 写入某一位数字颜色（含 {@link #EXTRA_COLOR} 与 {@link #EXTRA_SLOT}）。 */
    public static final String ACTION_SET_DIGIT_COLOR =
            "io.github.kelebao.colorosclockseconds.action.SET_DIGIT_COLOR";
    /** int extra：数字位 slot（0..5，见 {@link #slotOfChar}）。 */
    public static final String EXTRA_SLOT = "slot";
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

    /** 设置页 → SystemUI 重启自身（免 adb 重新加载模块）。 */
    public static final String ACTION_RESTART_SYSTEMUI =
            "io.github.kelebao.colorosclockseconds.action.RESTART_SYSTEMUI";
    /** 设置页 → SystemUI 跑一次性全量诊断（候选时钟 / 命中区 / 上次触摸结论）。 */
    public static final String ACTION_RUN_DIAGNOSTIC =
            "io.github.kelebao.colorosclockseconds.action.RUN_DIAGNOSTIC";
    /** SystemUI → 设置页：回传诊断结果 JSON。 */
    public static final String ACTION_DIAG_RESULT =
            "io.github.kelebao.colorosclockseconds.action.DIAG_RESULT";
    /** Secure 键：SystemUI 写入的诊断快照 JSON（双保险，app 也可回读）。 */
    public static final String CLOCK_DIAG_KEY = "coloros_clock_seconds_diag";
    /** String extra：诊断结果 JSON（随 {@link #ACTION_DIAG_RESULT} 回传 / 或读者从 Secure 读取）。 */
    public static final String EXTRA_DIAG_JSON = "diag_json";

    /**
     * 设置页 → SystemUI：探测模块是否「已激活」（已注入 SystemUI 且正在运行）。
     * 模块收到后回传 {@link #ACTION_PONG_MODULE} 证明存活。
     */
    public static final String ACTION_PING_MODULE =
            "io.github.kelebao.colorosclockseconds.action.PING_MODULE";
    /** SystemUI → 设置页：回应 {@link #ACTION_PING_MODULE}，证明模块已激活。 */
    public static final String ACTION_PONG_MODULE =
            "io.github.kelebao.colorosclockseconds.action.PONG_MODULE";
    /** Secure 键：模块注入 SystemUI 时写入心跳（格式 `时间戳|versionName|versionCode`），供设置页兜底判定。 */
    public static final String MODULE_ACTIVE_KEY = "coloros_clock_seconds_active";
    /** String extra：PONG 回传的模块版本名。 */
    public static final String EXTRA_MODULE_VERSION = "module_version";
    /** int extra：PONG 回传的模块 versionCode。 */
    public static final String EXTRA_MODULE_VERSION_CODE = "module_version_code";
    /** long extra：PONG 回传的心跳时间戳（毫秒）。 */
    public static final String EXTRA_ACTIVE_TS = "active_ts";
    /** int extra：PONG 回传的当前显秒开关（1 = 开启，0 = 关闭）。 */
    public static final String EXTRA_SECONDS_ON = "seconds_on";

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

    // ------------------------------------------------------------------
    // 模块激活心跳（模块注入 SystemUI 时写入，设置页据此兜底判定是否激活）
    // 格式：时间戳(毫秒)|versionName|versionCode
    // ------------------------------------------------------------------

    /** 写入心跳（仅 SystemUI 进程内调用，具备写入权限）。 */
    public static void writeActiveHeartbeat(ContentResolver resolver, String versionName, int versionCode) {
        String hb = System.currentTimeMillis() + "|" + versionName + "|" + versionCode;
        Settings.Secure.putString(resolver, MODULE_ACTIVE_KEY, hb);
    }

    /** 读取心跳原始串（无则返回空串）。 */
    public static String readActiveHeartbeat(ContentResolver resolver) {
        String v = Settings.Secure.getString(resolver, MODULE_ACTIVE_KEY);
        return v == null ? "" : v;
    }

    /** 解析心跳时间戳（毫秒）；格式错误或为空返回 0。 */
    public static long parseActiveTs(String heartbeat) {
        if (heartbeat == null || heartbeat.isEmpty()) {
            return 0;
        }
        int i = heartbeat.indexOf('|');
        if (i <= 0) {
            return 0;
        }
        try {
            return Long.parseLong(heartbeat.substring(0, i));
        } catch (NumberFormatException e) {
            return 0;
        }
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

    // ==================================================================
    // 六个数字位独立取色（时十 / 时个 / 分十 / 分个 / 秒十 / 秒个）
    // ==================================================================

    /** 可独立取色的数字位个数：时(2) + 分(2) + 秒(2)。 */
    public static final int DIGIT_COUNT = 6;

    /**
     * 哨兵值：该位「跟随系统」（不上色，继承系统时钟文字色）。
     *
     * <p>必须是 0 而不是 -1：预设里的纯白 {@code 0xFFFFFFFF} 在 int 上就是 -1，
     * 用 -1 当哨兵会让「跟随系统」与「纯白」再也分不开（设置页白色色块永远显示为选中）。</p>
     */
    public static final int DIGIT_COLOR_FOLLOW = 0;

    /** 每一位的颜色键（下标即 slot，见 {@link #slotOfChar}）。 */
    public static final String[] DIGIT_COLOR_KEYS = {
            "coloros_clock_seconds_digit_color_0",
            "coloros_clock_seconds_digit_color_1",
            "coloros_clock_seconds_digit_color_2",
            "coloros_clock_seconds_digit_color_3",
            "coloros_clock_seconds_digit_color_4",
            "coloros_clock_seconds_digit_color_5",
    };

    /** 六位颜色的中文位名（用于设置页摘要，如「时十」「秒个」）。 */
    public static final String[] DIGIT_LABELS = {"时十", "时个", "分十", "分个", "秒十", "秒个"};

    /**
     * 读取某一位的颜色。
     *
     * <p>兼容旧版：老用户只存过「小时颜色」单色键（{@link #HOUR_COLOR_KEY}），
     * 未写新的分位键时 0 号位（时十）回落到旧键，其余位默认「跟随系统」。</p>
     */
    public static int getDigitColor(ContentResolver resolver, int slot) {
        if (resolver == null || slot < 0 || slot >= DIGIT_COUNT) {
            return DIGIT_COLOR_FOLLOW;
        }
        if (Settings.Secure.getString(resolver, DIGIT_COLOR_KEYS[slot]) == null) {
            return slot == 0 ? getHourColor(resolver) : DIGIT_COLOR_FOLLOW;
        }
        return Settings.Secure.getInt(resolver, DIGIT_COLOR_KEYS[slot], DIGIT_COLOR_FOLLOW);
    }

    /** 写入某一位的颜色。0 号位同时写旧键，兼容浮窗/诊断等仍读 {@link #HOUR_COLOR_KEY} 的路径。 */
    public static void setDigitColor(ContentResolver resolver, int slot, int color) {
        if (resolver == null || slot < 0 || slot >= DIGIT_COUNT) {
            return;
        }
        Settings.Secure.putInt(resolver, DIGIT_COLOR_KEYS[slot], color);
        if (slot == 0) {
            setHourColor(resolver, color);
        }
    }

    /** 一次读出六位颜色（下标即 slot；-1 = 跟随系统）。 */
    public static int[] getDigitColors(ContentResolver resolver) {
        int[] colors = new int[DIGIT_COUNT];
        for (int i = 0; i < DIGIT_COUNT; i++) {
            colors[i] = getDigitColor(resolver, i);
        }
        return colors;
    }

    /** 是否有任意一位配置了颜色（用于设置页摘要与诊断）。 */
    public static boolean hasAnyDigitColor(ContentResolver resolver) {
        for (int c : getDigitColors(resolver)) {
            if (c != DIGIT_COLOR_FOLLOW) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------
    // 呼吸强度（用于选中色块的荧光呼吸光晕；6s 一个平滑明暗周期）
    // ------------------------------------------------------------------

    /** 呼吸周期（毫秒）：约 3 秒一次完整明暗往返（用户要求快一些）。 */
    public static final long BREATH_PERIOD_MS = 3_000L;

    /** 当前时刻的呼吸强度（0..1）：平滑余弦，明 → 暗 → 明。 */
    public static float breathIntensity(long nowMs) {
        double p = Math.floorMod(nowMs, BREATH_PERIOD_MS) / (double) BREATH_PERIOD_MS;
        return (float) ((1 - Math.cos(p * 2 * Math.PI)) / 2);
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

    /**
     * 文本下标 → 数字位 slot（0 时十 / 1 时个 / 2 分十 / 3 分个 / 4 秒十 / 5 秒个）。
     *
     * <p>约定文本形如 {@code HH:mm}、{@code HH:mm:ss}（可含结尾零宽空格），冒号分隔三个字段；
     * 每个字段内第 1 个数字是「十位」、第 2 个是「个位」。无前导零的单位数小时（如 {@code 9:41}）
     * 那一位属于「时个位」。</p>
     *
     * @param text  时钟文本
     * @param index 字符下标
     * @return 0..5；非数字字符、冒号或超出三个字段时返回 -1
     */
    public static int slotOfChar(CharSequence text, int index) {
        if (text == null || index < 0 || index >= text.length()) {
            return -1;
        }
        if (!Character.isDigit(text.charAt(index))) {
            return -1;
        }
        int field = 0;
        for (int i = 0; i < index; i++) {
            if (isFieldSeparator(text.charAt(i))) {
                field++;
            }
        }
        if (field > 2) {
            return -1;
        }
        int start = index;
        while (start > 0 && !isFieldSeparator(text.charAt(start - 1))) {
            start--;
        }
        int end = index;
        while (end + 1 < text.length() && !isFieldSeparator(text.charAt(end + 1))) {
            end++;
        }
        int digits = 0;
        int posInField = -1;
        for (int i = start; i <= end; i++) {
            if (Character.isDigit(text.charAt(i))) {
                if (i == index) {
                    posInField = digits;
                }
                digits++;
            }
        }
        if (posInField < 0) {
            return -1;
        }
        // 单位数小时（如 9:41）没有十位，那一位置就是「时个位」。
        int effective = (field == 0 && digits == 1) ? 1 : Math.min(posInField, 1);
        return field * 2 + effective;
    }

    /** 字段分隔符：半角与全角冒号。 */
    private static boolean isFieldSeparator(char c) {
        return c == ':' || c == '：';
    }

    /** 文本开头是否为「两位数字」小时（用于仅标红十位，如 13 的 1、05 的 0）。 */
    public static boolean hasTwoDigitHour(CharSequence text) {
        if (text == null || text.length() < 2) {
            return false;
        }
        return Character.isDigit(text.charAt(0)) && Character.isDigit(text.charAt(1));
    }
}
