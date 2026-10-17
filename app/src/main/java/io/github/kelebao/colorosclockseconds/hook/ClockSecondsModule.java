package io.github.kelebao.colorosclockseconds.hook;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.SystemClock;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import io.github.kelebao.colorosclockseconds.BuildConfig;
import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;

/**
 * 状态栏时钟显秒模块（legacy Xposed API，参考 {@code io.github.betterclock} 的稳妥范式）。
 *
 * <p><b>为什么这样写不会让 SystemUI 崩溃：</b>旧版 hook 了 ColorOS 私有的
 * {@code getSmallTime}/{@code updateClock} 并改其返回文本，破坏了系统内部状态，在 ColorOS 15
 * 上反复抛异常、触发 LSPosed 安全模式。本版改为 hook 稳定的 {@link TextView#setText}（after）
 * 与 {@link TextView#onDraw}（before）——在系统<em>已经设好</em>文本之后才覆写显示，绝不改动
 * 系统的渲染流程，因此不会破坏内部状态、不会崩溃。</p>
 *
 * <p><b>保留 ColorOS 小时红色：</b>系统状态栏时钟的小时数字本是红色（用 {@code Spannable} 的
 * 前景色 Span 实现）。本模块不替换整段文本，而是缓存系统在 {@code setText} 时给出的<em>原始文本
 * （含红色 Span）</em>，仅在其后追加 {@code :ss}，红色 Span 因而天然保留，且自动跟随系统 12/24 制。</p>
 *
 * <p><b>不阻碍下拉状态栏：</b>切换手势改用 {@link GestureDetector} + {@link View#setOnTouchListener}，
 * 且监听始终 {@code return false} 不消费触摸事件——只有「点击 / 长按且不移动」才切换显秒，
 * 下拉等滑动手势原样穿透给状态栏容器，正常拉下通知栏。</p>
 *
 * <p><b>识别时钟：</b>不绑定任何 Oplus 私有实现类，只按「资源名 + 类名」判断是否为状态栏时钟，
 * 并排除锁屏 / AOD / 日期 / 分隔符等无关控件（规则经一加 Ace 5 Pro / ColorOS 15 / Android 15
 * 真机验证可行）。</p>
 */
public final class ClockSecondsModule implements IXposedHookLoadPackage {

    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String LOG_PREFIX = "ColorOSClockSeconds: ";

    /** 进程内缓存的显秒开关（默认开启）；点击切换与首次遇到时钟时刷新。 */
    private static volatile boolean sSecondsEnabled;
    private static volatile boolean sStateLoaded;

    /** 正在接管刷新的时钟视图 -> 其 1 秒定时器（弱键，视图回收后自动消失）。 */
    private static final Map<TextView, ClockTicker> ACTIVE =
            Collections.synchronizedMap(new WeakHashMap<TextView, ClockTicker>());
    /** 已做过一次性配置（宽度 + 切换监听）的视图，避免重复配置。 */
    private static final Set<TextView> CONFIGURED =
            Collections.newSetFromMap(new WeakHashMap<TextView, Boolean>());
    /**
     * 系统给出的「无秒原始文本」（含小时红色 Span）。writeTime 只在其后追加 {@code :ss}，
     * 因此红色与 12/24 制都跟随系统，无需自己构造格式化器。
     */
    private static final Map<TextView, CharSequence> BASE_TEXT =
            Collections.synchronizedMap(new WeakHashMap<TextView, CharSequence>());

    /** 防递归：本模块调用 {@code setText} 时置位，避免 afterHook 再次进入造成死循环。 */
    private static final AtomicBoolean WRITING_CLOCK = new AtomicBoolean(false);

    /** 已打印过的候选控件（按「资源名|类名」去重），仅 DEBUG 构建用于真机排查识别失败。 */
    private static final Set<String> LOGGED_CANDIDATES =
            Collections.synchronizedSet(new HashSet<>());

    // ==================================================================
    // 入口
    // ==================================================================

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        // 只注入 SystemUI 主进程与 ColorOS 的 :ui 子进程（锁屏/状态栏 UI 所在）。
        // 截图、tuner、前台服务等其它 SystemUI 子进程不注入，避免无谓开销与风险。
        if (!SYSTEM_UI.equals(lpparam.packageName) || !isSystemUiProcess(lpparam.processName)) {
            return;
        }

        XposedBridge.hookAllMethods(TextView.class, "setText", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (WRITING_CLOCK.get() || !(param.thisObject instanceof TextView)) {
                    return;
                }
                TextView view = (TextView) param.thisObject;
                // 缓存系统原始文本（含小时红色 Span），供 writeTime 在其后追加秒。
                if (isStatusBarClock(view) && param.args != null && param.args.length > 0
                        && param.args[0] instanceof CharSequence) {
                    BASE_TEXT.put(view, (CharSequence) param.args[0]);
                }
                logCandidate(view);
                if (!isStatusBarClock(view)) {
                    return;
                }
                configureClockView(view);
                if (sSecondsEnabled) {
                    installTicker(view);
                    refreshClock(view);
                }
            }
        });

        XposedBridge.hookAllMethods(TextView.class, "onDraw", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (WRITING_CLOCK.get() || !(param.thisObject instanceof TextView)) {
                    return;
                }
                TextView view = (TextView) param.thisObject;
                // 绘制前再校准一次，覆盖「锁屏退出动画中系统已准备好但仍是分钟值」的那一帧。
                if (isStatusBarClock(view) && sSecondsEnabled) {
                    refreshClock(view);
                }
            }
        });

        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "hook installed in SystemUI; device="
                    + Build.MANUFACTURER + "/" + Build.BRAND
                    + "; process=" + lpparam.processName + "; format=HH:mm:ss");
        }
    }

    // ==================================================================
    // 时钟视图配置（一次性）
    // ==================================================================

    /** 首次命中状态栏时钟时做：读状态、固定宽度防移位、挂切换监听。每个视图只做一次。 */
    private static void configureClockView(TextView view) {
        if (!isStatusBarClock(view)) {
            return;
        }
        synchronized (CONFIGURED) {
            if (!CONFIGURED.add(view)) {
                return;
            }
        }

        ensureStateLoaded(view);

        // 固定占位宽度，避免秒数变化把旁边的日期/图标推动（沿用 betterclock 的稳宽做法）。
        try {
            float widestDigit = 0f;
            for (char digit = '0'; digit <= '9'; digit++) {
                widestDigit = Math.max(widestDigit, view.getPaint().measureText(String.valueOf(digit)));
            }
            int width = (int) Math.ceil(widestDigit * 6f + view.getPaint().measureText("::"))
                    + view.getCompoundPaddingLeft() + view.getCompoundPaddingRight();
            view.setMinWidth(width);
            view.setMaxWidth(width);
        } catch (Throwable ignored) {
            // 测量失败不影响核心功能
        }

        // 挂载点击 / 长按切换监听（按手势偏好）。
        // 关键：OnTouchListener 始终 return false，不消费触摸，下拉等滑动手势留给状态栏容器处理。
        try {
            Context context = view.getContext();
            int gesture = context != null
                    ? ClockSecondsConfig.getGesture(context.getContentResolver())
                    : ClockSecondsConfig.GESTURE_CLICK;
            GestureDetector gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    if (gesture == ClockSecondsConfig.GESTURE_CLICK) {
                        toggleSeconds(view);
                    }
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    if (gesture == ClockSecondsConfig.GESTURE_LONG_PRESS) {
                        toggleSeconds(view);
                    }
                }
            });
            view.setOnTouchListener((v, event) -> {
                gd.onTouchEvent(event);
                return false; // 不消费：点击已在 GestureDetector 内处理，滑动手势穿透给状态栏
            });
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "toggle listener attached; gesture=" + gesture);
            }
        } catch (Throwable ignored) {
            // 监听挂载失败也不应影响显示
        }
    }

    /** 进程内首次需要判断显秒状态时，读一次 {@code Settings.Secure} 并缓存（之后不再每次读）。 */
    private static void ensureStateLoaded(View view) {
        if (sStateLoaded) {
            return;
        }
        Context context = view.getContext();
        if (context == null) {
            return;
        }
        try {
            sSecondsEnabled = ClockSecondsConfig.isEnabled(context.getContentResolver());
            sStateLoaded = true;
        } catch (Throwable ignored) {
            // 读不到就保守保持默认（开启），下次再试
        }
    }

    /** 点击 / 长按时钟：翻转显秒开关并立即刷新。 */
    private static void toggleSeconds(TextView view) {
        Context context = view.getContext();
        if (context == null) {
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        boolean enabled = !ClockSecondsConfig.isEnabled(resolver);
        ClockSecondsConfig.setEnabled(resolver, enabled);
        sSecondsEnabled = enabled;
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "seconds toggled -> " + enabled);
        }
        if (enabled) {
            writeTime(view);
            installTicker(view);
        } else {
            stopTicker(view);
            writeMinuteOnly(view);
        }
    }

    // ==================================================================
    // 文本覆写（保留系统原文本 + 红色 Span，仅追加秒）
    // ==================================================================

    /** 用「系统原文本 + :ss」覆写时钟（红色 Span 由 BASE_TEXT 继承，自动跟随 12/24 制）。 */
    private static void writeTime(TextView view) {
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        int second = (int) ((System.currentTimeMillis() / 1000) % 60);
        String ss = String.format(Locale.US, ":%02d", second);
        SpannableStringBuilder sb = new SpannableStringBuilder(base);
        sb.append(ss);
        if (TextUtils.equals(view.getText(), sb)) {
            return;
        }
        WRITING_CLOCK.set(true);
        try {
            view.setText(sb);
        } finally {
            WRITING_CLOCK.set(false);
        }
    }

    /** 关闭显秒后，立即把文本回退为系统原文本（无秒、保留红色 Span）。 */
    private static void writeMinuteOnly(TextView view) {
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        if (TextUtils.equals(view.getText(), base)) {
            return;
        }
        WRITING_CLOCK.set(true);
        try {
            view.setText(base);
        } finally {
            WRITING_CLOCK.set(false);
        }
    }

    private static void refreshClock(TextView view) {
        if (isStatusBarClock(view) && sSecondsEnabled) {
            writeTime(view);
        }
    }

    // ==================================================================
    // 1 秒定时器
    // ==================================================================

    private static void installTicker(final TextView view) {
        synchronized (ACTIVE) {
            if (ACTIVE.containsKey(view)) {
                return;
            }
            ClockTicker ticker = new ClockTicker(view);
            ACTIVE.put(view, ticker);
            // View.post 在 SystemUI 早于主 Looper 准备好时也是安全的：Android 会把动作排队到视图挂载后执行。
            view.post(ticker);
        }
    }

    private static void stopTicker(TextView view) {
        ClockTicker ticker;
        synchronized (ACTIVE) {
            ticker = ACTIVE.remove(view);
        }
        if (ticker != null) {
            view.removeCallbacks(ticker);
        }
    }

    private static final class ClockTicker implements Runnable {
        private final WeakReference<TextView> reference;
        private long firstDetachedAt;

        ClockTicker(TextView view) {
            reference = new WeakReference<>(view);
        }

        @Override
        public void run() {
            TextView view = reference.get();
            if (view == null) {
                return;
            }
            if (!view.isAttachedToWindow()) {
                // 控件常在挂载前就先 setText（inflate 阶段）。短暂等待；超过 5 秒视为陈旧视图，停止轮询。
                long now = SystemClock.uptimeMillis();
                if (firstDetachedAt == 0L) {
                    firstDetachedAt = now;
                    view.postDelayed(this, 100L);
                } else if (now - firstDetachedAt < 5000L) {
                    view.postDelayed(this, 100L);
                } else {
                    synchronized (ACTIVE) {
                        ACTIVE.remove(view);
                    }
                }
                return;
            }

            firstDetachedAt = 0L;
            if (sSecondsEnabled) {
                refreshClock(view);
                // 对齐到下一秒边界，避免每秒漂移。
                long delay = 1000L - (System.currentTimeMillis() % 1000L);
                view.postDelayed(this, Math.max(50L, delay));
            } else {
                // 已关闭显秒：不再续订，定时器自然停止（下次开启时 installTicker 会重新挂载）。
                synchronized (ACTIVE) {
                    ACTIVE.remove(view);
                }
            }
        }
    }

    // ==================================================================
    // 时钟识别
    // ==================================================================

    /** 判断该 TextView 是否为「状态栏左上角时钟」（目标控件）。 */
    private static boolean isStatusBarClock(TextView view) {
        int id = view.getId();
        if (id == View.NO_ID || id == 0) {
            return false;
        }
        if (!isOplusDevice()) {
            return false;
        }
        String name = resourceName(view);
        if (name.isEmpty()) {
            return false;
        }
        String className = view.getClass().getName().toLowerCase(Locale.ROOT);

        // 排除锁屏 / AOD / 日期等大型时钟，以及分隔符、通知栏无关控件。
        boolean excluded = name.contains("keyguard") || name.contains("lockscreen")
                || name.contains("aod") || name.contains("date")
                || name.contains("separator") || name.startsWith("no_carrier")
                || name.contains("shade_") || name.contains("quick_setting")
                || className.contains("keyguard") || className.contains("aod");
        if (excluded) {
            return false;
        }

        // 状态栏 / 通知栏头部时钟的资源名或类名特征。
        boolean clockRelated = name.equals("clock")
                || name.equals("status_bar_clock")
                || name.equals("header_clock")
                || name.contains("status_clock")
                || name.contains("clock_text")
                || className.contains("statusbar.policy.clock")
                || className.contains("statclock");
        return clockRelated;
    }

    private static String resourceName(TextView view) {
        try {
            Resources resources = view.getResources();
            return resources.getResourceEntryName(view.getId()).toLowerCase(Locale.ROOT);
        } catch (Resources.NotFoundException ignored) {
            return "";
        }
    }

    /** 仅 DEBUG 构建：把含 clock/status 的 TextView 资源名打印一次，便于补全识别规则或排查。 */
    private static void logCandidate(TextView view) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        if (!isOplusDevice()) {
            return;
        }
        String name = resourceName(view);
        String className = view.getClass().getName();
        String searchable = (name + " " + className).toLowerCase(Locale.ROOT);
        if (!(searchable.contains("clock") || searchable.contains("status"))) {
            return;
        }
        String key = name + "|" + className;
        if (LOGGED_CANDIDATES.add(key)) {
            XposedBridge.log(LOG_PREFIX + "candidate: " + key);
        }
    }

    private static boolean isSystemUiProcess(String processName) {
        // AOSP 把锁屏 UI 放在主进程；ColorOS 用独立的 :ui 进程；其它子进程（截图/前台服务）无关。
        return SYSTEM_UI.equals(processName) || (SYSTEM_UI + ":ui").equals(processName);
    }

    private static boolean isOplusDevice() {
        String vendor = (Build.MANUFACTURER + " " + Build.BRAND).toLowerCase(Locale.ROOT);
        return vendor.contains("oneplus") || vendor.contains("oppo")
                || vendor.contains("realme") || vendor.contains("oplus");
    }
}
