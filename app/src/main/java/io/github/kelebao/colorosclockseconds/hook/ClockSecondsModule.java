package io.github.kelebao.colorosclockseconds.hook;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
 * <p><b>小时十位数红色样式：</b>本模块把状态栏时钟「小时的十位数」标红
 * （例如 {@code 13} 的 {@code 1}、{@code 05} 的 {@code 0}），其余文字（分钟、秒）保持系统原色。
 * 实现方式是在覆写后的文本上给十位数那一个字符追加一段红色 {@code ForegroundColorSpan}；
 * 追加的 {@code :ss} 不标红，跟随系统时钟文字色。单位数小时（如 {@code 9:45}）无十位，不标红。</p>
 *
 * <p><b>不阻碍下拉状态栏：</b>切换手势改为 hook 系统全局的 {@link View#dispatchTouchEvent}，
 * 仅当按下点落在已识别时钟的屏幕边界内、且几乎无位移（点击）或按住 500ms（长按）时才切换显秒；
 * 模块<em>不消费</em>任何触摸事件，下拉等滑动手势原样交给系统，正常拉下通知栏。</p>
 *
 * <p><b>识别时钟：</b>不绑定任何厂商私有实现类，只按「资源名 + 类名」判断是否为状态栏时钟，
 * 并排除锁屏 / AOD / 日期 / 分隔符等无关控件。规则经 ColorOS 15 真机验证，同时包含 AOSP
 * 风格类名，可在小米、荣耀、vivo 等其它厂商机型上尝试生效。</p>
 */
public final class ClockSecondsModule implements IXposedHookLoadPackage {

    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String LOG_PREFIX = "ColorOSClockSeconds: ";

    /** 小时十位数颜色：可在设置页自定义，默认 Material Red 500（见 ClockSecondsConfig）。 */
    private static final int HOUR_COLOR_FALLBACK = ClockSecondsConfig.HOUR_COLOR_DEFAULT;

    /** 进程内缓存的显秒开关（默认开启）；点击切换与首次遇到时钟时刷新。 */
    private static volatile boolean sSecondsEnabled;
    private static volatile boolean sStateLoaded;
    /** 配置广播接收器是否已注册（只注册一次）。 */
    private static volatile boolean sConfigReceiverRegistered;
    private static final Object CONFIG_RECEIVER_LOCK = new Object();

    /** 正在接管刷新的时钟视图 -> 其 1 秒定时器（弱键，视图回收后自动消失）。 */
    private static final Map<TextView, ClockTicker> ACTIVE =
            Collections.synchronizedMap(new WeakHashMap<TextView, ClockTicker>());
    /** 已做过一次性配置（宽度 + 切换监听）的视图，避免重复配置。 */
    private static final Set<TextView> CONFIGURED =
            Collections.newSetFromMap(new WeakHashMap<TextView, Boolean>());
    /**
     * 系统给出的「无秒原始文本」。系统时钟文字色（非 Span）让追加的 {@code :ss} 自动同色，
     * 因此红色与 12/24 制都跟随系统，无需自己构造格式化器。
     */
    private static final Map<TextView, CharSequence> BASE_TEXT =
            Collections.synchronizedMap(new WeakHashMap<TextView, CharSequence>());

    /**
     * 状态栏时钟视图（首次识别后缓存）与其当前屏幕边界。ColorOS 上通知面板会拦截状态栏区域的触摸，
     * 时钟自身的 {@code OnTouchListener} 收不到事件；因此改用「全局 hook {@code View.dispatchTouchEvent} +
     * 边界命中」来检测点击/长按，时钟视图本身不消费触摸，下拉通知栏照常。
     */
    private static volatile TextView sClockView;
    private static final android.graphics.Rect sClockRect = new android.graphics.Rect();
    private static final Object sClockRectLock = new Object();

    /** 点击/长按检测状态（全局单次手势，避免重复触发）。 */
    private static volatile boolean sPendingTap;
    private static long sDownTime;
    /** 双击手势：上一次点击的时间戳（0 = 无待配对点击）。 */
    private static long sLastTapTime;
    private static float sDownX, sDownY;
    private static android.os.Handler sTouchHandler;
    private static Runnable sLongPressTask;

    /** 长按弹出的预览浮窗（非空表示正在显示）。 */
    private static View sFloatView;
    private static Runnable sFloatTicker;

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
        if (BuildConfig.DEBUG && SYSTEM_UI.equals(lpparam.packageName)) {
            XposedBridge.log(LOG_PREFIX + "HANDLE SystemUI process=" + lpparam.processName);
        }
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
                // 配置写入通道与"是否识别到时钟"解耦：任意 TextView 触发时注册一次，
                // 这样即使某些厂商的时钟 id 没被识别，设置页改颜色/手势也能写进去。
                registerConfigReceiver(view.getContext());
                // 缓存系统原始文本（追加的秒继承系统时钟文字色），供 writeTime 在其后追加秒。
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
                } else {
                    // 不显秒时也要保持小时十位数红色样式（系统每分钟更新 HH:mm 时拦截标红）。
                    applyRedToBaseText(view);
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

        // 全局触摸监听：ColorOS 通知面板会拦截状态栏区域触摸，时钟自身的 OnTouchListener 收不到。
        // 这里 hook 所有 View 的 dispatchTouchEvent，仅当按下点在已识别时钟的屏幕边界内、且
        // 几乎无位移（点击）或按住 500ms（长按）时才切换显秒；不消费事件，下拉通知栏不受影响。
        XposedBridge.hookAllMethods(android.view.View.class, "dispatchTouchEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (sClockView == null || !(param.args != null && param.args.length > 0
                        && param.args[0] instanceof MotionEvent)) {
                    return;
                }
                handleClockTouch((MotionEvent) param.args[0]);
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
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "CONFIG CLOCK: " + resourceName(view)
                    + " | " + view.getClass().getName());
        }
        synchronized (CONFIGURED) {
            if (!CONFIGURED.add(view)) {
                return;
            }
        }

        ensureStateLoaded(view);

        // 固定占位宽度，避免秒数变化把旁边的日期/图标推动（沿用 betterclock 的稳宽做法）。
        //
        // 只在 ColorOS 系（OPPO / 一加 / realme）启用：
        // 小米 HyperOS 的 MiuiClock 是 wrap_content 且没有重写 onMeasure，锁定宽度会让
        // TextView 在固定宽度内无法完整绘制文本，状态栏时间整块消失（issue #2）。
        //
        // 且只设 minWidth、不设 maxWidth：时钟最宽就是 HH:mm:ss，minWidth 已足够防止
        // 显秒开关引起的横向移位；maxWidth 在字体测量有偏差时会裁切文本，弊大于利。
        if (isOplusDevice()) {
            try {
                float widestDigit = 0f;
                for (char digit = '0'; digit <= '9'; digit++) {
                    widestDigit = Math.max(widestDigit, view.getPaint().measureText(String.valueOf(digit)));
                }
                int width = (int) Math.ceil(widestDigit * 6f + view.getPaint().measureText("::"))
                        + view.getCompoundPaddingLeft() + view.getCompoundPaddingRight();
                view.setMinWidth(width);
            } catch (Throwable ignored) {
                // 测量失败不影响核心功能
            }
        }

        // 缓存时钟视图与其屏幕边界（供全局 dispatchTouchEvent 监听做点击/长按命中判断）。
        // 不再给时钟挂 OnTouchListener：ColorOS 通知面板会拦截状态栏区域触摸，时钟自身收不到事件；
        // 全局监听在 dispatchTouchEvent 层命中时钟边界，且不消费事件，下拉通知栏不受影响。
        try {
            sClockView = view;
            refreshClockRect(view);
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "clock view cached; bounds=" + sClockRect
                        + " gesture=" + ClockSecondsConfig.getGesture(view.getContext().getContentResolver()));
            }
        } catch (Throwable ignored) {
            // 缓存失败不影响显示
        }
    }

    /** 刷新时钟视图当前屏幕边界（每次配置时调用，覆盖旋转/状态变化；按下时也会再刷新一次）。 */
    private static void refreshClockRect(View view) {
        try {
            int[] loc = new int[2];
            view.getLocationOnScreen(loc);
            synchronized (sClockRectLock) {
                sClockRect.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
            }
        } catch (Throwable ignored) {
            // 视图未布局时拿不到，等下次配置/按下再刷新
        }
    }

    /**
     * 全局触摸监听（由 {@code View.dispatchTouchEvent} 触发）。判断按下点是否落在状态栏时钟边界内，
     * 按设置的手势（单击 / 双击）切换显秒；长按固定弹出实时预览浮窗。滑动（下拉）不触发，且不消费事件。
     */
    private static void handleClockTouch(MotionEvent ev) {
        try {
            if (sTouchHandler == null) {
                sTouchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            }
            int gesture = sClockView.getContext() != null
                    ? ClockSecondsConfig.getGesture(sClockView.getContext().getContentResolver())
                    : ClockSecondsConfig.GESTURE_CLICK;
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                refreshClockRect(sClockView); // 每次按下刷新边界，覆盖旋转/状态变化
                float x = ev.getRawX(), y = ev.getRawY();
                boolean hit;
                synchronized (sClockRectLock) {
                    hit = sClockRect.contains((int) x, (int) y);
                }
                if (hit) {
                    sPendingTap = true;
                    sDownTime = ev.getEventTime();
                    sDownX = x;
                    sDownY = y;
                    // 长按固定用于弹出浮窗预览时钟（与切换手势无关）。
                    final Runnable task = () -> {
                        if (sPendingTap) {
                            sPendingTap = false;
                            showFloatPreview(sClockView);
                        }
                    };
                    sLongPressTask = task;
                    sTouchHandler.postDelayed(task, 500);
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (sPendingTap) {
                    float dx = Math.abs(ev.getRawX() - sDownX);
                    float dy = Math.abs(ev.getRawY() - sDownY);
                    if (dx > 16 || dy > 16) {
                        sPendingTap = false;
                        if (sLongPressTask != null) {
                            sTouchHandler.removeCallbacks(sLongPressTask);
                            sLongPressTask = null;
                        }
                    }
                }
            } else if (action == MotionEvent.ACTION_UP) {
                if (sPendingTap) {
                    sPendingTap = false;
                    if (sLongPressTask != null) {
                        sTouchHandler.removeCallbacks(sLongPressTask);
                        sLongPressTask = null;
                    }
                    if (gesture == ClockSecondsConfig.GESTURE_CLICK) {
                        long dt = ev.getEventTime() - sDownTime;
                        float dx = Math.abs(ev.getRawX() - sDownX);
                        float dy = Math.abs(ev.getRawY() - sDownY);
                        if (dt < 400 && dx < 16 && dy < 16) {
                            toggleSeconds(sClockView);
                        }
                    } else if (gesture == ClockSecondsConfig.GESTURE_DOUBLE_TAP) {
                        long dt = ev.getEventTime() - sDownTime;
                        float dx = Math.abs(ev.getRawX() - sDownX);
                        float dy = Math.abs(ev.getRawY() - sDownY);
                        if (dt < 400 && dx < 16 && dy < 16) {
                            // 350ms 内第二次点击才算双击；第一次只记录时间。
                            long now = ev.getEventTime();
                            if (now - sLastTapTime <= 350) {
                                sLastTapTime = 0;
                                toggleSeconds(sClockView);
                            } else {
                                sLastTapTime = now;
                            }
                        }
                    }
                }
            } else if (action == MotionEvent.ACTION_CANCEL) {
                sPendingTap = false;
                if (sLongPressTask != null) {
                    sTouchHandler.removeCallbacks(sLongPressTask);
                    sLongPressTask = null;
                }
            }
        } catch (Throwable ignored) {
            // 触摸识别异常绝不应影响系统
        }
    }

    /**
     * 长按状态栏时间：弹出毛玻璃浮窗显示**网络时间**（HH:mm:ss，小时十位跟随设置的颜色）。
     * 默认只显示时间；点击时间才浮现「关闭 / 拉伸」按钮，可拖动移位、拉伸改尺寸。
     * 后台向网络校准一次偏移并写回。全程 try/catch，绝不影响系统。
     */
    private static void showFloatPreview(View clockView) {
        try {
            if (clockView == null || clockView.getContext() == null) {
                return;
            }
            final Context context = clockView.getContext();
            if (sTouchHandler == null) {
                sTouchHandler = new android.os.Handler(android.os.Looper.getMainLooper());
            }
            sTouchHandler.post(() -> {
                try {
                    WindowManager wm = (WindowManager) context.getSystemService(
                            Context.WINDOW_SERVICE);
                    if (wm == null) {
                        return;
                    }
                    dismissFloatPreview(); // 重复长按先关掉旧的

                    // 偏移在浮窗存活期间缓存，避免每秒跨进程读 Secure。
                    final long[] offset = {ClockSecondsConfig.getNetOffset(
                            context.getContentResolver())};

                    // 容器：时间 + 两个浮层按钮，整体可拖动、可拉伸。
                    final FloatBox box = new FloatBox(context);
                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(floatDp(context, 12));
                    // 半透明深色底座（配合窗口级模糊即毛玻璃效果）。
                    bg.setColor(0x8C111118);
                    box.setBackground(bg);

                    // 时间：填满容器，点击它显示/隐藏按钮。
                    final TextView tv = new TextView(context);
                    tv.setTextSize(20f);
                    tv.setSingleLine(true);
                    tv.setEllipsize(null);
                    tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    tv.setTextColor(0xFFFFFFFF);
                    tv.setGravity(Gravity.CENTER);
                    tv.setPadding(floatDp(context, 16), floatDp(context, 10),
                            floatDp(context, 16), floatDp(context, 10));
                    box.addView(tv, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.MATCH_PARENT));

                    // 关闭按钮：右上角，默认隐藏。
                    final TextView close = new TextView(context);
                    close.setText("✕");
                    close.setTextSize(11f);
                    close.setTextColor(0xFFFFFFFF);
                    close.setGravity(Gravity.CENTER);
                    close.setPadding(floatDp(context, 7), floatDp(context, 4),
                            floatDp(context, 7), floatDp(context, 4));
                    close.setVisibility(View.GONE);
                    close.setOnClickListener(v -> dismissFloatPreview());
                    FrameLayout.LayoutParams closeLp = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.TOP | Gravity.END);
                    box.addView(close, closeLp);

                    // 拉伸按钮：右下角，默认隐藏；拖动它改变浮窗尺寸。
                    final TextView resize = new TextView(context);
                    resize.setText("◣");
                    resize.setTextSize(11f);
                    resize.setTextColor(0xFFFFFFFF);
                    resize.setGravity(Gravity.CENTER);
                    resize.setPadding(floatDp(context, 8), floatDp(context, 6),
                            floatDp(context, 8), floatDp(context, 6));
                    resize.setVisibility(View.GONE);
                    FrameLayout.LayoutParams resizeLp = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.BOTTOM | Gravity.START);
                    box.addView(resize, resizeLp);

                    // 点击时间：显示 / 隐藏按钮。
                    tv.setOnClickListener(v -> {
                        boolean show = close.getVisibility() != View.VISIBLE;
                        close.setVisibility(show ? View.VISIBLE : View.GONE);
                        resize.setVisibility(show ? View.VISIBLE : View.GONE);
                    });

                    final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                            // 默认尺寸用 dp，各分辨率/密度机型观感一致（约 160×64dp）。
                            floatDp(context, 160),
                            floatDp(context, 64),
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                            PixelFormat.TRANSLUCENT);
                    // 全程使用左上角坐标（START|TOP），拖动/拉伸零换算、绝不跳移。
                    lp.gravity = Gravity.TOP | Gravity.START;
                    lp.y = floatDp(context, 4);
                    // 毛玻璃：模糊窗口背后的屏幕内容（API 31+ 公开 API，不支持时退化为半透明）。
                    try {
                        if (Build.VERSION.SDK_INT >= 31
                                && wm.isCrossWindowBlurEnabled()) {
                            lp.setBlurBehindRadius(floatDp(context, 18));
                        }
                    } catch (Throwable ignored) {
                        // 不支持模糊时保持半透明
                    }
                    box.attach(wm, lp);
                    box.markInteractive(close);
                    box.markInteractive(resize);

                    // 拉伸手柄（左下角）：左缘与下缘跟随手指，右上角纹丝不动。
                    resize.setOnTouchListener(new View.OnTouchListener() {
                        private float downRawX, downRawY;
                        private int startX, startY, startW, startH;

                        @Override
                        public boolean onTouch(View v, MotionEvent ev) {
                            switch (ev.getAction()) {
                                case MotionEvent.ACTION_DOWN: {
                                    // 全程 START 坐标，直接记录初始值，无需换算。
                                    downRawX = ev.getRawX();
                                    downRawY = ev.getRawY();
                                    startX = lp.x;
                                    startY = lp.y;
                                    startW = box.getWidth();
                                    startH = box.getHeight();
                                    return true;
                                }
                                case MotionEvent.ACTION_MOVE: {
                                    if (startW <= 0 || startH <= 0) {
                                        return true;
                                    }
                                    float dx = ev.getRawX() - downRawX;
                                    float dy = ev.getRawY() - downRawY;
                                    int screenW = context.getResources()
                                            .getDisplayMetrics().widthPixels;
                                    // 尺寸上限同时考虑 dp 值和屏幕宽度（适配小屏机型）。
                                    int minW = floatDp(context, 70);
                                    int maxW = Math.min(floatDp(context, 400),
                                            (int) (screenW * 0.9f));
                                    int minH = floatDp(context, 36);
                                    int maxH = floatDp(context, 200);
                                    // 右缘固定：width = startW - dx；左缘跟随：x = startX + dx
                                    int newW = startW - (int) dx;
                                    int newH = startH + (int) dy;
                                    int newX = startX + (int) dx;
                                    // 约束：宽高在 min~max，左缘不出屏
                                    newW = Math.max(minW, Math.min(newW, maxW));
                                    newH = Math.max(minH, Math.min(newH, maxH));
                                    newX = Math.max(0, Math.min(newX,
                                            startX + startW - newW));
                                    lp.x = newX;
                                    lp.y = startY;
                                    lp.width = newW;
                                    lp.height = newH;
                                    // 文字大小交给 updateFloatText 按窗口宽高自适应。
                                    box.requestLayout();
                                    updateFloatText(tv, context, offset[0], lp);
                                    try {
                                        wm.updateViewLayout(box, lp);
                                    } catch (Throwable ignored) {
                                        // 视图已不在窗口上时静默
                                    }
                                    return true;
                                }
                                default:
                                    return true;
                            }
                        }
                    });

                    wm.addView(box, lp);
                    sFloatView = box;
                    updateFloatText(tv, context, offset[0], lp);
                    // 测量完后把窗口推到右上角（靠右留 12dp）。
                    box.post(() -> {
                        try {
                            if (sFloatView != box) {
                                return;
                            }
                            int screenW = context.getResources()
                                    .getDisplayMetrics().widthPixels;
                            lp.x = Math.max(0, screenW - floatDp(context, 12)
                                    - box.getWidth());
                            wm.updateViewLayout(box, lp);
                        } catch (Throwable ignored) {
                            // 忽略
                        }
                    });

                    // 后台向网络校准一次偏移（HTTP Date 头，按 RTT 折半校正）。
                    new Thread(() -> {
                        long drift = measureNetDrift();
                        if (drift == Long.MIN_VALUE) {
                            return;
                        }
                        try {
                            ClockSecondsConfig.setNetOffset(
                                    context.getContentResolver(), drift);
                        } catch (Throwable ignored) {
                            return;
                        }
                        if (sTouchHandler != null) {
                            sTouchHandler.post(() -> {
                                if (sFloatView == box) {
                                    offset[0] = drift;
                                    updateFloatText(tv, context, offset[0], lp);
                                }
                            });
                        }
                    }).start();

                    // 常显：只每秒刷新时间，不自动消失（✕ 关闭）。
                    final Runnable tickerTask = new Runnable() {
                        @Override
                        public void run() {
                            if (sFloatView != box) {
                                return;
                            }
                            updateFloatText(tv, context, offset[0], lp);
                            sTouchHandler.postDelayed(this, 1000L);
                        }
                    };
                    sFloatTicker = tickerTask;
                    sTouchHandler.postDelayed(tickerTask, 1000L);
                } catch (Throwable ignored) {
                    // 浮窗失败不应影响系统
                }
            });
        } catch (Throwable ignored) {
            // 浮窗失败不应影响系统
        }
    }

    /**
     * 可拖动的浮窗容器：按下不拦截（让按钮/时间正常收到点击），位移超阈值才拦截并拖动。
     * 默认靠右上角（END 锚点），首次拖动时换算成 START 左上角坐标。
     */
    private static final class FloatBox extends FrameLayout {
        private WindowManager windowManager;
        private WindowManager.LayoutParams params;
        private float downRawX, downRawY;
        private int startX, startY;
        private boolean dragging;
        /** 按钮（关闭/拉伸）专用交互区：DOWN 落在里面就绝不拦截，避免抢走按钮的手势。 */
        private final java.util.List<View> interactiveChildren =
                new java.util.ArrayList<>();
        private boolean childHandling;

        FloatBox(Context context) {
            super(context);
        }

        /** 注册专属交互子 view（关闭/拉伸按钮）。 */
        void markInteractive(View v) {
            interactiveChildren.add(v);
        }

        private boolean isOnInteractiveChild(float rx, float ry) {
            for (View v : interactiveChildren) {
                if (v.getVisibility() != View.VISIBLE || v.getWidth() == 0) {
                    continue;
                }
                int[] loc = new int[2];
                v.getLocationOnScreen(loc);
                if (rx >= loc[0] && rx <= loc[0] + v.getWidth()
                        && ry >= loc[1] && ry <= loc[1] + v.getHeight()) {
                    return true;
                }
            }
            return false;
        }

        void attach(WindowManager wm, WindowManager.LayoutParams lp) {
            this.windowManager = wm;
            this.params = lp;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            if (params == null) {
                return false;
            }
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downRawX = ev.getRawX();
                    downRawY = ev.getRawY();
                    startX = params.x;
                    startY = params.y;
                    dragging = false;
                    // DOWN 落在按钮上：该手势归按钮，容器全程不拦截。
                    childHandling = isOnInteractiveChild(ev.getRawX(), ev.getRawY());
                    return false; // 不拦截：点击事件放行给子 view
                case MotionEvent.ACTION_MOVE: {
                    if (childHandling) {
                        return false; // 按钮正在处理（如拉伸），不抢
                    }
                    float dx = ev.getRawX() - downRawX;
                    float dy = ev.getRawY() - downRawY;
                    float threshold = floatDp(getContext(), 6);
                    if (!dragging && (Math.abs(dx) > threshold || Math.abs(dy) > threshold)) {
                        dragging = true;
                        return true; // 拦截，开始拖动
                    }
                    return false;
                }
                default:
                    return false;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (params != null && dragging
                    && (ev.getAction() == MotionEvent.ACTION_MOVE)) {
                float dx = ev.getRawX() - downRawX;
                float dy = ev.getRawY() - downRawY;
                int screenW = getResources().getDisplayMetrics().widthPixels;
                int screenH = getResources().getDisplayMetrics().heightPixels;
                int w = getWidth();
                int h = getHeight();
                int nx = (int) (startX + dx);
                int ny = (int) (startY + dy);
                params.x = Math.max(0, Math.min(nx, Math.max(0, screenW - w)));
                params.y = Math.max(0, Math.min(ny, Math.max(0, screenH - h)));
                try {
                    windowManager.updateViewLayout(this, params);
                } catch (Throwable ignored) {
                    // 视图已不在窗口上时静默
                }
                return true;
            }
            return super.onTouchEvent(ev);
        }
    }

    /**
     * 测量网络时间相对系统时间的偏移（毫秒，网络 - 系统）。
     * 取 RTT 更小的样本；全部失败返回 {@link Long#MIN_VALUE}。
     */
    private static long measureNetDrift() {
        long bestDrift = Long.MIN_VALUE;
        long bestRtt = Long.MAX_VALUE;
        for (String url : new String[]{"https://api.github.com", "https://www.baidu.com"}) {
            long t0 = System.currentTimeMillis();
            Long date = headDate(url);
            long t1 = System.currentTimeMillis();
            if (date == null) {
                continue;
            }
            long rtt = t1 - t0;
            // 服务器时间 ≈ 收到时刻 - RTT/2
            long drift = date + rtt / 2 - t1;
            if (rtt < bestRtt) {
                bestRtt = rtt;
                bestDrift = drift;
            }
        }
        return bestDrift;
    }

    /** HEAD 请求取 HTTP Date 头（1 秒精度）；失败返回 null。 */
    private static Long headDate(String urlStr) {
        java.net.HttpURLConnection conn = null;
        try {
            java.net.URL url = new java.net.URL(urlStr);
            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestMethod("HEAD");
            conn.setRequestProperty("User-Agent", "ClockSeconds-Module");
            long t = conn.getHeaderFieldDate("Date", 0L);
            return t > 0L ? t : null;
        } catch (Throwable t) {
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /** 关闭预览浮窗（若有）并停掉刷新。 */
    private static void dismissFloatPreview() {
        try {
            if (sTouchHandler != null && sFloatTicker != null) {
                sTouchHandler.removeCallbacks(sFloatTicker);
            }
            sFloatTicker = null;
            if (sFloatView != null) {
                try {
                    WindowManager wm = (WindowManager) sFloatView.getContext()
                            .getSystemService(Context.WINDOW_SERVICE);
                    if (wm != null) {
                        wm.removeViewImmediate(sFloatView);
                    }
                } catch (Throwable ignored) {
                    // view 已不在窗口上时静默
                }
                sFloatView = null;
            }
        } catch (Throwable ignored) {
            // 关闭失败静默
        }
    }

    /**
     * 浮窗文本：网络时间 HH:mm:ss（系统时间 + 网络偏移），小时十位用设置的小时颜色（若启用）。
     * 字号按窗口当前宽高自适应（10~60sp），保证任何尺寸下都完整显示、不被裁切。
     *
     * @param offsetMs 网络 - 系统的偏移（毫秒）
     * @param lp       浮窗窗口参数（取宽高算字号；可为 null）
     */
    private static void updateFloatText(TextView tv, Context context, long offsetMs,
                                        WindowManager.LayoutParams lp) {
        try {
            long netNow = System.currentTimeMillis() + offsetMs;
            String text = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date(netNow));
            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            if (ClockSecondsConfig.isHourColorEnabled(context.getContentResolver())
                    && sb.length() >= 2
                    && Character.isDigit(sb.charAt(0)) && Character.isDigit(sb.charAt(1))) {
                sb.setSpan(new ForegroundColorSpan(ClockSecondsConfig.getHourColor(
                        context.getContentResolver())), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // 自适应字号：取宽高允许的较小值，保证 "HH:mm:ss" 完整放进窗口。
            try {
                float density = context.getResources().getDisplayMetrics().density;
                int availW = (tv.getWidth() > 0 ? tv.getWidth() : floatDp(context, 88))
                        - tv.getPaddingLeft() - tv.getPaddingRight();
                int availH = (tv.getHeight() > 0 ? tv.getHeight() : floatDp(context, 40))
                        - tv.getPaddingTop() - tv.getPaddingBottom();
                if (lp != null && lp.width > 0) {
                    availW = lp.width - tv.getPaddingLeft() - tv.getPaddingRight();
                }
                if (lp != null && lp.height > 0) {
                    availH = lp.height - tv.getPaddingTop() - tv.getPaddingBottom();
                }
                // "HH:mm:ss" 数字宽约 0.62em、冒号约 0.35em，平均按 0.55em/字符估算。
                float maxByW = availW / (text.length() * 0.55f) / density;
                float maxByH = availH * 0.62f / density;
                float size = Math.max(10f, Math.min(60f, Math.min(maxByW, maxByH)));
                tv.setTextSize(size);
            } catch (Throwable ignored) {
                // 字号自适应失败就用当前字号
            }
            tv.setText(sb);
        } catch (Throwable ignored) {
            // 文本刷新失败静默
        }
    }

    private static int statusBarHeight(Context context) {
        try {
            int id = context.getResources().getIdentifier(
                    "status_bar_height", "dimen", "android");
            if (id > 0) {
                int h = context.getResources().getDimensionPixelSize(id);
                // sanity：正常状态栏 24~48dp；个别机型该资源异常偏大，回退默认值。
                if (h > 0 && h <= floatDp(context, 48)) {
                    return h;
                }
            }
        } catch (Throwable ignored) {
            // 取不到就用默认值
        }
        return floatDp(context, 28);
    }

    private static int floatDp(Context context, int dp) {
        return (int) (dp * context.getResources().getDisplayMetrics().density + 0.5f);
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
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "STATE loaded enabled=" + sSecondsEnabled);
            }
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
    // 文本覆写（保留系统原文本，追加的秒继承时钟文字色，仅追加秒）
    // ==================================================================

    /** 用「系统原文本 + :ss」覆写时钟（追加的秒继承系统时钟文字色，自动跟随 12/24 制）。 */
    private static void writeTime(TextView view) {
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        int second = (int) ((System.currentTimeMillis() / 1000) % 60);
        String ss = String.format(Locale.US, ":%02d", second);
        SpannableStringBuilder sb = new SpannableStringBuilder(base);
        sb.append(ss);
        // ColorOS 专属样式：仅把「小时十位数」标红（如 13 的 1、05 的 0），其余（含秒）保持系统原色。
        applyHourTensRed(sb, view, hourColor(view));
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "WRITE: [" + sb + "] baseRed="
                    + (base instanceof android.text.Spannable));
        }
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

    /**
     * 仅把「小时十位数」着色，其余文字（分钟、秒）保持系统原色。颜色来自设置页（默认红色）。
     * 小时在文本开头、冒号之前；当其为两位数字（如 13、05）时，第一位即十位数，着色；
     * 无前导零的单位数小时（如 9:45）没有十位，不着色。
     *
     * <p>可读性适配：状态栏背景会随应用切换明暗（系统时钟文字色随之变黑/变白）。
     * 背景为浅色（系统文字为深色）而所选颜色又很亮时（如白色、黄色），直接上色会看不见，
     * 因此把过亮的颜色压暗，保证在浅色状态栏上仍然清晰。</p>
     */
    private static void applyHourTensRed(SpannableStringBuilder sb, TextView view, int color) {
        if (!hourColorEnabled(view)) {
            // 总开关关闭：恢复系统默认样式，不加任何 Span。
            return;
        }
        int colon = -1;
        for (int i = 0; i < sb.length(); i++) {
            char c = sb.charAt(i);
            if (c == ':' || c == '：') {
                colon = i;
                break;
            }
        }
        // 小时部分 = [0, colon)；有且仅有两位数字（含十位）时才着色第一位。
        if (colon >= 2 && Character.isDigit(sb.charAt(0)) && Character.isDigit(sb.charAt(1))) {
            int adapted = adaptColorForBackground(color, view.getCurrentTextColor());
            sb.setSpan(new ForegroundColorSpan(adapted), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * 按状态栏明暗适配颜色：系统文字为深色 ⇒ 背景浅，此时把过亮的颜色压暗（保持色相），
     * 避免白色/黄色在浅色状态栏上"消失"；深色背景则原样使用。
     */
    private static int adaptColorForBackground(int color, int systemTextColor) {
        boolean lightBackground = Color.luminance(systemTextColor) < 0.5f;
        if (!lightBackground) {
            return color;
        }
        if (Color.luminance(color) <= 0.55f) {
            return color;
        }
        return Color.rgb(
                (int) (Color.red(color) * 0.42f),
                (int) (Color.green(color) * 0.42f),
                (int) (Color.blue(color) * 0.42f));
    }

    /** 读取用户配置的小时颜色；任何异常都退回默认红色，绝不影响时钟显示。 */
    private static int hourColor(TextView view) {
        try {
            return ClockSecondsConfig.getHourColor(view.getContext().getContentResolver());
        } catch (Throwable t) {
            return HOUR_COLOR_FALLBACK;
        }
    }

    /** 小时颜色总开关（默认启用）；读取失败按启用处理，保持原有观感。 */
    private static boolean hourColorEnabled(TextView view) {
        try {
            return ClockSecondsConfig.isHourColorEnabled(view.getContext().getContentResolver());
        } catch (Throwable t) {
            return true;
        }
    }

    /** 关闭显秒后，立即把文本回退为系统原文本，并应用小时十位数红色样式。 */
    private static void writeMinuteOnly(TextView view) {
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(base);
        applyHourTensRed(sb, view, hourColor(view));
        WRITING_CLOCK.set(true);
        try {
            view.setText(sb);
        } finally {
            WRITING_CLOCK.set(false);
        }
    }

    /** 对系统原文本应用小时十位数红色样式（不显秒时，系统每次更新分钟都会调用）。 */
    private static void applyRedToBaseText(TextView view) {
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(base);
        applyHourTensRed(sb, view, hourColor(view));
        WRITING_CLOCK.set(true);
        try {
            view.setText(sb);
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
        String name = resourceName(view);
        CharSequence txt = view.getText();
        String text = txt == null ? "" : txt.toString();
        // 诊断探针：任何显示「数字:数字」时间格式的 TextView 都打印（定位真实状态栏时钟资源名）。
        if (text.matches(".*\\d{1,2}[:：]\\d{2}.*")
                && LOGGED_CANDIDATES.add("TIME|" + name + "|" + view.getClass().getName())) {
            XposedBridge.log(LOG_PREFIX + "TIMETV: " + name + " | "
                    + view.getClass().getName() + " text=[" + text + "]");
        }
        String className = view.getClass().getName();
        String searchable = (name + " " + className + " " + text).toLowerCase(Locale.ROOT);
        if (!(searchable.contains("clock") || searchable.contains("status")
                || searchable.contains("time") || searchable.contains("main"))) {
            return;
        }
        String key = name + "|" + className;
        if (LOGGED_CANDIDATES.add(key)) {
            XposedBridge.log(LOG_PREFIX + "candidate: " + key + " text=[" + view.getText() + "]");
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

    // ==================================================================
    // 配置写入：设置页（普通应用进程）没有 WRITE_SECURE_SETTINGS，
    // 由 SystemUI 进程（系统应用，天然具备该权限）代写。
    // ==================================================================

    /** 只注册一次：接收设置页发来的改色 / 改手势广播，并在 SystemUI 进程内写 Secure。 */
    private static void registerConfigReceiver(final Context context) {
        if (sConfigReceiverRegistered) {
            return;
        }
        synchronized (CONFIG_RECEIVER_LOCK) {
            if (sConfigReceiverRegistered) {
                return;
            }
            try {
                // 首次安装（Secure 里没有显秒键）时主动写入开启，保证状态栏时间默认显示秒数。
                // 已有关键（用户显式关闭过）则尊重用户选择，不覆盖。
                try {
                    ContentResolver cr0 = context.getContentResolver();
                    if (Settings.Secure.getInt(cr0, ClockSecondsConfig.MODULE_SECONDS_KEY, -1)
                            == -1) {
                        ClockSecondsConfig.setEnabled(cr0, true);
                    }
                } catch (Throwable ignored) {
                    // 写入失败不影响后续注册
                }
                IntentFilter filter = new IntentFilter();
                filter.addAction(ClockSecondsConfig.ACTION_SET_HOUR_COLOR);
                filter.addAction(ClockSecondsConfig.ACTION_SET_HOUR_COLOR_ENABLED);
                filter.addAction(ClockSecondsConfig.ACTION_SET_GESTURE);
                filter.addAction(ClockSecondsConfig.ACTION_SYNC_TIME);
                filter.addAction(ClockSecondsConfig.ACTION_SET_NET_OFFSET);
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context ctx, Intent intent) {
                        try {
                            String action = intent.getAction();
                            ContentResolver cr = ctx.getContentResolver();
                            if (ClockSecondsConfig.ACTION_SET_HOUR_COLOR.equals(action)) {
                                ClockSecondsConfig.setHourColor(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_COLOR, HOUR_COLOR_FALLBACK));
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_HOUR_COLOR_ENABLED.equals(action)) {
                                ClockSecondsConfig.setHourColorEnabled(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_ENABLED, 1) == 1);
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_GESTURE.equals(action)) {
                                ClockSecondsConfig.setGesture(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_GESTURE,
                                        ClockSecondsConfig.GESTURE_CLICK));
                            } else if (ClockSecondsConfig.ACTION_SYNC_TIME.equals(action)) {
                                // 应用无权直接改系统时钟（SET_TIME 是系统权限），
                                // 打开系统「自动设置时间」让系统自己向网络校准。
                                android.provider.Settings.Global.putInt(
                                        cr, android.provider.Settings.Global.AUTO_TIME, 1);
                            } else if (ClockSecondsConfig.ACTION_SET_NET_OFFSET.equals(action)) {
                                // 设置页测得的网络时间偏移（浮窗显示网络时间用）。
                                ClockSecondsConfig.setNetOffset(cr, intent.getLongExtra(
                                        ClockSecondsConfig.EXTRA_NET_OFFSET, 0L));
                            }
                        } catch (Throwable ignored) {
                            // 写入失败不影响系统界面
                        }
                    }
                };
                if (Build.VERSION.SDK_INT >= 33) {
                    context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED);
                } else {
                    context.registerReceiver(receiver, filter);
                }
                // 注册成功后才置位：万一首次注册抛异常，下次还有机会重试。
                sConfigReceiverRegistered = true;
            } catch (Throwable ignored) {
                // 注册失败只影响"未用 adb 授权时的设置写入"，不影响显秒与点击切换
            }
        }
    }

    /**
     * 立即重绘缓存的时钟：秒关闭时系统每分钟才更新一次，改颜色/开关后不主动刷新
     * 最长要等一分钟才能看到变化。
     */
    private static void refreshClockNow() {
        TextView clock = sClockView;
        if (clock == null) {
            return;
        }
        try {
            if (sSecondsEnabled) {
                writeTime(clock);
            } else {
                writeMinuteOnly(clock);
            }
        } catch (Throwable ignored) {
            // 重绘失败不影响系统时钟自身的刷新
        }
    }
}
