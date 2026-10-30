package io.github.kelebao.colorosclockseconds.hook;


import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
import io.github.kelebao.colorosclockseconds.ColonLiftSpan;
import io.github.kelebao.colorosclockseconds.GradientTextSpan;

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

    /**
     * ColorOS 17 原生显秒控制器：ColorOS 17 起系统自带「状态栏显示秒」能力，
     * 但默认会在 5 分钟后自动关闭。类名见 ColorOS 17 实机（oplus 私有实现）。
     */
    private static final String NATIVE_SECONDS_CONTROLLER =
            "com.oplus.systemui.statusbar.clock.ClockSecondsController";
    /** ColorOS 17 原生显秒开关（{@code Settings.Secure} 键，1 = 开）。 */
    private static final String NATIVE_SECONDS_SETTING = "oplus_status_bar_clock_seconds_mode";
    /** 是否检测到并挂上了 ColorOS 17 原生显秒控制器（仅 SystemUI 进程内有效）。 */
    private static volatile boolean sNativeSeconds;

    /**
     * ColorOS 16/17 的 Oplus 时钟实现：{@code setTextWithRedOneStyle} 会在每次时钟更新
     * （{@code Clock#updateClock}）和状态栏明暗变化（{@code Clock#onDarkChanged}）时重设文本，
     * 并带上系统自己的「红1」样式。它是覆盖我们自定义小时色的主要来源。
     */
    private static final String OPLUS_CLOCK_EX = "com.oplus.systemui.common.clock.OplusClockExImpl";

    /** 小时十位数颜色：可在设置页自定义，默认 Material Red 500（见 ClockSecondsConfig）。 */
    private static final int HOUR_COLOR_FALLBACK = ClockSecondsConfig.HOUR_COLOR_DEFAULT;

    /**
     * 六位数字颜色缓存（下标 = slot，-1 = 跟随系统）。{@code null} = 尚未加载。
     *
     * <p>每次写时钟都要用颜色，若每帧读 6 个 Secure 键会产生大量 binder 往返；因此进程内缓存，
     * 仅在首次使用与收到 {@code SET_DIGIT_COLOR / SET_HOUR_COLOR} 广播时刷新。</p>
     */
    private static volatile int[] sDigitColors;

    /** 渐变开关缓存（与六位颜色同批重读，避免每秒额外 binder）。 */
    private static volatile boolean sGradientOn;
    /** 渐变端点色缓存（{@code GRADIENT_COLOR_AUTO} = 跟随首位 / 末位数字）。 */
    private static volatile int sGradientStart = ClockSecondsConfig.GRADIENT_COLOR_AUTO;
    private static volatile int sGradientEnd = ClockSecondsConfig.GRADIENT_COLOR_AUTO;

    /** 时间后缀缓存（周几 / 时段）：与颜色同批重读，每秒渲染路径零 binder。 */
    private static volatile boolean sMarkLoaded;
    private static volatile boolean sMarkOn;
    private static volatile int sMarkMode = ClockSecondsConfig.MARK_MODE_PERIOD;
    private static volatile String[] sMarkPeriods =
            ClockSecondsConfig.MARK_PERIODS_FALLBACK.split(",");
    /** 无上色目标时复用的空数组（避免每帧分配）。 */
    private static final int[] NO_STARTS = new int[0];

    /** 进程内缓存的显秒开关（默认开启）；点击切换与首次遇到时钟时刷新。 */
    private static volatile boolean sSecondsEnabled;

    // ---------------- 自定义任务（定时显秒 / 定时关秒） ----------------

    /** 任务列表（Secure 里的 JSON，见 {@link io.github.kelebao.colorosclockseconds.ClockTask}）。 */
    private static volatile List<io.github.kelebao.colorosclockseconds.ClockTask> sTasks =
            Collections.emptyList();
    /** 当前是否有任务窗口生效（vc250 起只剩「显秒」动作，「关闭显秒」已删除）。 */
    private static volatile boolean sTaskShow;
    /** 「任务进行中」提示文案，由设置页按当前系统语言推送（模块自己的资源在 SystemUI 进程里取不到）。 */
    private static volatile String sToastTaskText;
    private static android.os.Handler sTaskHandler;

    /**
     * 生效中的显秒状态 = 总开关 {@link #sSecondsEnabled} 叠加任务窗口。
     * 所有「要不要显示秒」的判断都必须走这里，不要直接读 {@link #sSecondsEnabled}。
     */
    private static boolean secondsOn() {
        if (sTaskShow) {
            return true;
        }
        return sSecondsEnabled;
    }
    private static volatile boolean sStateLoaded;
    /** 配置广播接收器是否已注册（只注册一次）。 */
    private static volatile boolean sConfigReceiverRegistered;
    private static final Object CONFIG_RECEIVER_LOCK = new Object();

    /** 正在接管刷新的时钟视图 -> 其 1 秒定时器（弱键，视图回收后自动消失）。 */
    private static final Map<TextView, ClockTicker> ACTIVE =
            Collections.synchronizedMap(new WeakHashMap<TextView, ClockTicker>());

    /** 仅 DEBUG：span 探针节流表（每个时钟最多 ~1 次/秒 dump，避免刷屏）。 */
    private static final Map<TextView, Long> SPAN_DUMP_AT =
            Collections.synchronizedMap(new WeakHashMap<TextView, Long>());
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
     * 我们上次写入的「可见文本」（去掉零宽空格）。用于区分：
     * 可见文本没变却没颜色 = 真掉色（被洗）；可见文本变了 = 正常分钟跳变。只有前者才续期兜底窗口。
     */
    /** 配置变化后强制重写一次文本（否则幂等短路会让新 span / 新周期不生效）。 */
    private static volatile boolean sForceRewrite;

    private static final Map<TextView, String> LAST_WRITTEN =
            Collections.synchronizedMap(new WeakHashMap<TextView, String>());

    /**
     * 状态栏时钟视图（首次识别后缓存）与其当前屏幕边界。ColorOS 上通知面板会拦截状态栏区域的触摸，
     * 时钟自身的 {@code OnTouchListener} 收不到事件；因此改用「全局 hook {@code View.dispatchTouchEvent} +
     * 边界命中」来检测点击/长按，时钟视图本身不消费触摸，下拉通知栏照常。
     */
    private static volatile TextView sClockView;
    private static final android.graphics.Rect sClockRect = new android.graphics.Rect();
    private static final Object sClockRectLock = new Object();
    // 诊断用：记录最近一次按下的坐标、时间与命中结论（供「运行诊断」一次性输出，无需逐条翻 logcat）。
    private static float sLastTouchX = -1, sLastTouchY = -1;
    private static long sLastTouchTime = 0L;
    private static boolean sLastHit = false;

    /** 长按弹出浮窗的按住时长（毫秒）。Android 长按标准 500ms。 */
    private static final long LONG_PRESS_MS = 500L;

    /** 单次点击的最长按下时长 / 最大位移（触屏卡顿时放宽；长按 500ms 仍可区分）。 */
    private static final long TAP_MAX_MS = 480L;
    private static final float TAP_SLOP_PX = 24f;
    /** 双击配对的两次点击最大间隔。 */
    private static final long DOUBLE_TAP_MS = 450L;
    /** 单击模式下抑制「顺手双击」造成的连续取反。 */
    private static final long TOGGLE_DEBOUNCE_MS = 220L;

    /** 点击/长按检测状态（全局单次手势，避免重复触发）。 */
    private static volatile boolean sPendingTap;
    private static long sDownTime;
    /** 双击手势：上一次点击的时间戳（0 = 无待配对点击）。 */
    private static long sLastTapTime;
    /** 上一次真正切换显秒的时间戳（单击模式去抖用）。 */
    private static long sLastToggleAt;
    private static float sDownX, sDownY;
    private static android.os.Handler sTouchHandler;
    private static Runnable sLongPressTask;

    /** 长按弹出的预览浮窗（非空表示正在显示）。 */
    private static View sFloatView;
    private static WindowManager.LayoutParams sFloatLp;
    private static Runnable sFloatTicker;
    /** 浮窗用的小时颜色缓存（广播更新，避免每秒跨进程读 Secure）。 */
    private static volatile int sFloatHourColor = ClockSecondsConfig.HOUR_COLOR_DEFAULT;
    private static volatile boolean sFloatHourColorEnabled = true;
    private static volatile boolean sFloatColorLoaded;
    /** 浮窗背景板底色（深色半透明，配合窗口级模糊即毛玻璃效果）。 */
    private static final int FLOAT_BG_BASE = 0x8C111118;

    /** 自定义状态栏时钟识别名（设置页填写，空 = 内置规则）；广播更新。 */
    private static volatile String sCustomRule = "";
    private static volatile boolean sCustomRuleLoaded;

    /** 模块自保护：hook 抛异常计数；短时间过量则自动停用本模块逻辑，避免把 SystemUI 拖崩触发安全模式。 */
    private static final long SELF_PROTECT_WINDOW_MS = 60_000L;
    private static final int SELF_PROTECT_MAX_ERRORS = 60;
    private static volatile boolean sSelfDisabled;
    private static long sErrorWindowStart;
    private static int sErrorCount;

    /** 防递归：本模块调用 {@code setText} 时置位，避免 afterHook 再次进入造成死循环。 */
    private static final AtomicBoolean WRITING_CLOCK = new AtomicBoolean(false);

    /**
     * 本模块正在构造带色文本（{@code applyDigitColors} 内部会 removeSpan 替换旧色 span）。
     * SPAN-THEFT 探针据此排除我们自己的操作，避免误报。
     */
    private static final AtomicBoolean APPLYING_STYLE = new AtomicBoolean(false);

    /** 已打印过的候选控件（按「资源名|类名」去重），仅 DEBUG 构建用于真机排查识别失败。 */
    private static final Set<String> LOGGED_CANDIDATES =
            Collections.synchronizedSet(new HashSet<>());

    /**
     * 自动检测用的候选时钟视图池（弱引用，避免内存泄漏）。
     * 任何正在显示 {@code HH:mm} 时间格式、且不在排除名单里的 TextView 都会被记录，
     * 供 {@link #detectClockName()} 在收到检测广播时从中挑出最像状态栏时钟的那个。
     */
    private static final java.util.List<WeakReference<TextView>> sClockCandidates =
            new java.util.concurrent.CopyOnWriteArrayList<>();

    /**
     * 测试卡片（诊断用）：模块实时检测到的候选时钟清单。
     * key = {@code 类名|资源名|父容器类名}（稳定且能区分状态栏/通知栏同名时钟），
     * value = 给人看的标签（资源名 · 简类名）。由 SystemUI 在检测到时钟时写入 Secure，设置页读取展示。
     */
    private static final java.util.Map<String, String> sClockInfo =
            new java.util.concurrent.ConcurrentHashMap<>();
    /** 用户对候选时钟的启用选择（key -> 是否接管）。缺省视为启用。仅 SystemUI 进程内有效，从 Secure 加载。 */
    private static volatile java.util.Map<String, Boolean> sClockEnabled = new java.util.HashMap<>();
    /**
     * Secure 配置监听：任何途径（App 直写 Secure / 广播 / adb）改了显秒或时钟开关，
     * 都同步到内存并即时应用，防止「App 有 WRITE_SECURE_SETTINGS 直写、不广播」导致的状态漂移。
     */
    private static android.database.ContentObserver sSecureObserver;
    /** 候选清单是否脏（有新增时钟待发布），配合节流避免每次 setText 都写 Secure。 */
    private static final AtomicBoolean sClockListDirty = new AtomicBoolean(false);
    /** 上次发布候选清单的时间戳（uptime 毫秒），用于节流（≥1.5s 才重写一次）。 */
    private static long sLastListPublish;
    /** 低频补色循环的诊断日志节流（DEBUG，~30s 一条）。 */
    private static long sLastKeepaliveLog;
    /** 兜底冷却窗口：连续这么久无掉色就停掉周期补色（自适应省电，不掉色的设备 ticker 基本一直停着）。 */
    private static final long COLOR_COOLDOWN_MS = 5 * 60 * 1000L;
    /** SPAN-THEFT 探针日志节流（DEBUG，1s）。 */
    private static long sLastSpanTheftLog;

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
                if (sSelfDisabled || !(param.thisObject instanceof TextView)) {
                    return;
                }
                TextView view = (TextView) param.thisObject;
                // 抓凶 A（SETTEXT-STRIP）：我们写入的带色文本被 setText 内部洗掉——arg 带着色
                // span、存入后却没有。「StatClock.setText 覆写重建」「TextView 内部转换」两种
                // 机制都不走 removeSpan（SPAN-THEFT 抓不到），必须在这里前后对比。
                // WRITING_CLOCK=true 即本次写入来自我们；调用栈无用（调用者就是我们），
                // 真凶是 setText 实现本身，故记录 view 类名与前后文本状态即可定位。
                if (BuildConfig.DEBUG && WRITING_CLOCK.get() && param.args != null
                        && param.args.length > 0 && param.args[0] instanceof Spanned) {
                    try {
                        Spanned arg = (Spanned) param.args[0];
                        if (arg.getSpans(0, arg.length(), ForegroundColorSpan.class).length > 0) {
                            CharSequence stored = view.getText();
                            boolean storedHas = stored instanceof Spanned
                                    && ((Spanned) stored).getSpans(0, stored.length(),
                                            ForegroundColorSpan.class).length > 0;
                            if (!storedHas) {
                                XposedBridge.log(LOG_PREFIX + "SETTEXT-STRIP: our spanned ["
                                        + arg + "] lost FCS inside setText; stored=[" + stored
                                        + "] view=" + view.getClass().getName());
                            }
                        }
                    } catch (Throwable ignored) {
                        // 探针绝不影响主流程
                    }
                }
                if (WRITING_CLOCK.get()
                        || !(param.thisObject instanceof TextView)) {
                    return;
                }
                try {
                    // 配置写入通道与"是否识别到时钟"解耦：任意 TextView 触发时注册一次，
                    // 这样即使某些厂商的时钟 id 没被识别，设置页改颜色/手势也能写进去。
                    registerConfigReceiver(view.getContext());
                    // 缓存系统原始文本（追加的秒继承系统时钟文字色），供 writeTime 在其后追加秒。
                    final boolean barClock = isStatusBarClock(view);
                    final boolean qsClock = !barClock && isQsClock(view);
                    if ((barClock || qsClock) && param.args != null && param.args.length > 0
                            && param.args[0] instanceof CharSequence) {
                        BASE_TEXT.put(view, (CharSequence) param.args[0]);
                    }
                    logCandidate(view);
                    // DEBUG 探针：打印每一次命中状态栏时钟的 setText（含调用者），定位颜色覆盖源。
                    if (barClock) {
                        logProbeSetText(view, param.args != null && param.args.length > 0
                                ? param.args[0] : null);
                        // 抓凶探针 B：hook 时钟真实运行类的 setText（每类一次）。
                        maybeHookClockViewClass(view);
                    }
                    // 自动检测：把任何正在显示时间格式、且非排除类的 TextView 收入候选池
                    // （同时登记到测试卡片清单，供设置页逐项勾选定位状态栏时钟）。
                    recordCandidate(view);
                    // 非状态栏时钟，或用户在测试卡片里停用了该时钟 → 不接管（不显秒、不标红）。
                    if ((!barClock && !qsClock) || !isClockEnabled(view)) {
                        return;
                    }
                    // 面板时钟：面板收起时不可见（detach / 0 尺寸），此时不接管、不装 ticker；
                    // 展开后第一帧绘制由 onDraw 钩子兜住「首次接管」。
                    if (qsClock && !isLiveQsClock(view)) {
                        return;
                    }

                    // 诊断探针：在系统 setText 之后、我们补色之前 dump Span 状态。
                    // ColorOS 17 的 StatClock 疑似在 setText 层把 CharSequence 洗成非 Spanned
                    // （实测：我们的 setText(sb) 后 getText() 立刻 not Spanned，而 ColorOS 15 正常）。
                    // 此处 dump 用于区分两种根因：
                    //   ① 系统自带「红1」span 也不在 → 系统红1 被我们冲掉 / 系统用非 span 机制画红
                    //   ② 系统红1 span 在 → 我们的补色多余且有害，应跳过补色让系统渲染
                    if (BuildConfig.DEBUG) {
                        logSpans(view, "after-system");
                    }
                    if (barClock) {
                        // 只有状态栏时钟能成为 sClockView（长按热区 / 触摸切换都靠它）
                        configureClockView(view);
                    } else {
                        QS_CLOCKS.add(view);
                    }
                    if (secondsOn()) {
                        installTicker(view);
                        refreshClock(view);
                        // 系统/第三方主题模块（如 dev.puitheme.StatusBarModule）常在 setText 之后
                        // （同步或 Handler 异步）再重设纯文本、洗掉我们的颜色 span；
                        // 把补色 post 到本次消息之后再执行，保证我们最后写入、不被盖回。
                        scheduleReapply(view);
                    } else {
                        // 不显秒时也要保持小时十位数红色样式（系统每分钟更新 HH:mm 时拦截标红）。
                        // 自适应兜底：本次 setText 若发现颜色缺失（被洗）→ 立即补色并"唤醒/续期"守护窗口；
                        // 若颜色仍在（wrote=false）→ 不强制装 ticker，让它能按冷却窗口自动停（省电）。
                        // scheduleReapply 兜系统/第三方模块异步二次写。
                        int wrote = applyRedToBaseText(view);
                        if (wrote == 2) {
                            // 传感器：异步二次写把颜色洗掉了 → 唤醒/续期本视图守护窗口并装兜底 ticker。
                            installTicker(view);
                        }
                        scheduleReapply(view);
                    }
                } catch (Throwable t) {
                    noteHookError(t);
                }
            }
        });

        XposedBridge.hookAllMethods(TextView.class, "onDraw", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (sSelfDisabled || WRITING_CLOCK.get()
                        || !(param.thisObject instanceof TextView)) {
                    return;
                }
                try {
                    TextView view = (TextView) param.thisObject;
                    // 三星自绘：数字位 onDraw 时必然已布局（可做位置判断），作为安装触发点；
                    // 同时把系统数字位的当前文字色同步给自绘时钟（状态栏明暗切换时跟随）。
                    if (isStatusBarClock(view)) {
                        // onDraw 时视图必然已布局（width/height > 0），此时刷新边界最可靠：
                        // configureClockView 在 setText 时缓存，彼时视图可能尚未布局，
                        // bounds 全 0 → 长按命中区为空 → 长按无反应（C16 实测复现）。
                        if (view == sClockView) {
                            refreshClockRect(view);
                        }
                        // 绘制前再校准一次，覆盖「锁屏退出动画中系统已准备好但仍是分钟值」的那一帧。
                        if (secondsOn()) {
                            refreshClock(view);
                        }
                    } else if (isQsClock(view)) {
                        // 下拉面板时钟：面板展开时系统不一定会再 setText（文本没变），
                        // 所以在绘制时兜一次「首次接管」；ticker 已在跑时什么都不做。
                        if (isClockEnabled(view) && secondsOn()
                                && isLiveQsClock(view) && !ACTIVE.containsKey(view)) {
                            QS_CLOCKS.add(view);
                            installTicker(view);
                            refreshClock(view);
                        } else if (isClockEnabled(view) && !secondsOn()
                                && isLiveQsClock(view) && countColons(view.getText()) > 1) {
                            // 显秒关掉时面板可能正收着（收着不会 setText），再展开就会残留上一帧
                            // 我们写过的带秒文本。这里一次性改回 HH:mm（带颜色）；改完只有 1 个冒号，
                            // 条件不再命中，不会每帧重写。
                            QS_CLOCKS.add(view);
                            writeMinuteOnly(view);
                        }
                    }
                } catch (Throwable t) {
                    noteHookError(t);
                }
            }
        });

        // 全局触摸监听：ColorOS 通知面板会拦截状态栏区域触摸，时钟自身的 OnTouchListener 收不到。
        // 这里 hook 所有 View 的 dispatchTouchEvent，仅当按下点在已识别时钟的屏幕边界内、且
        // 几乎无位移（点击）或按住 500ms（长按）时才切换显秒；不消费事件，下拉通知栏不受影响。
        XposedBridge.hookAllMethods(android.view.View.class, "dispatchTouchEvent", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (sSelfDisabled || sClockView == null
                        || !(param.args != null && param.args.length > 0
                        && param.args[0] instanceof MotionEvent)) {
                    return;
                }
                handleClockTouch((MotionEvent) param.args[0]);
            }
        });

        // ColorOS 17 适配：系统自带状态栏显秒，但 5 分钟后会自动关闭；挂 hook 去掉该限制。
        installNativeSecondsHooks(lpparam.classLoader);

        // DEBUG 抓凶探针：若有模块不走 setText 而是直接对时钟文本 removeSpan 洗色，
        // PROBE setText 看不到它。hook SpannableStringBuilder.removeSpan，仅当移除的是
        // ForegroundColorSpan 且文本是时钟格式时打印调用栈（1s 节流），直接定位真凶。
        installSpanTheftProbe();

        // ColorOS 16/17：系统时钟走 OplusClockExImpl#setTextWithRedOneStyle（官方红1样式），
        // 每次更新/变色都会重设文本、冲掉我们的颜色；在它之后再把小时色盖回去。
        installOplusClockStyleHook(lpparam.classLoader);

        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "hook installed in SystemUI; device="
                    + Build.MANUFACTURER + "/" + Build.BRAND
                    + "; process=" + lpparam.processName + "; format=HH:mm:ss");
        }
    }

    // ==================================================================
    // ColorOS 17 原生显秒适配
    // ==================================================================

    /**
     * ColorOS 17 起系统自带「状态栏显示秒」，但会在 5 分钟后自动关闭。这里 hook 原生
     * {@code ClockSecondsController}，让显秒保持常开（不再 5 分钟自动关闭）：
     * <ul>
     *   <li>{@code isOplusFiveMinutesActive(long)} → true：让系统认为 5 分钟机制仍在生效，
     *       不去走「到期关闭」分支；</li>
     *   <li>{@code scheduleExpiryLocked(int, long)} → null：模式为「开」时跳过到期调度；</li>
     *   <li>{@code onUserOrModeChanged(int, int, long)} → 到期时间强制 0：永不安排自动关闭。</li>
     * </ul>
     * 控制器类不存在（非 ColorOS 17）时静默跳过，完全不影响旧机型。
     */
    private static void installNativeSecondsHooks(ClassLoader classLoader) {
        try {
            Class<?> controller = Class.forName(NATIVE_SECONDS_CONTROLLER, false, classLoader);
            XposedBridge.hookAllMethods(controller, "isOplusFiveMinutesActive", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    param.setResult(Boolean.TRUE);
                }
            });
            XposedBridge.hookAllMethods(controller, "scheduleExpiryLocked", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (param.args != null && param.args.length >= 1
                                && ((Number) param.args[0]).intValue() == 1) {
                            param.setResult(null);
                        }
                    } catch (Throwable ignored) {
                        // 参数异常时交给系统原逻辑
                    }
                }
            });
            XposedBridge.hookAllMethods(controller, "onUserOrModeChanged", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (param.args != null && param.args.length >= 3
                                && ((Number) param.args[1]).intValue() == 1) {
                            param.args[2] = 0L;
                        }
                    } catch (Throwable ignored) {
                        // 参数异常时交给系统原逻辑
                    }
                }
            });
            sNativeSeconds = true;
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "ColorOS 17 native seconds hooks installed");
            }
        } catch (Throwable ignored) {
            // 非 ColorOS 17（无该类）→ 继续走通用的 setText 追加秒路径
            sNativeSeconds = false;
        }
    }

    /**
     * ColorOS 16/17：hook 系统时钟的 {@code OplusClockExImpl#setTextWithRedOneStyle}。
     *
     * <p>系统在每次 {@code Clock#updateClock} / {@code Clock#onDarkChanged} 时都会调用它重设时钟文本，
     * 并可能带上系统自己的「红1」前景色 Span。我们的 setText after-hook 虽然也会跑，但有时会被它
     * 随后应用的样式冲掉 → 表现为「自定义小时色一会儿就变回默认」。</p>
     *
     * <p>这里在它<b>之后</b>再把小时色盖回去（只上色、不追加秒），保证我们的颜色是最后一次写入。
     * 类/方法不存在时静默跳过，不影响旧机型。</p>
     */
    private static void installOplusClockStyleHook(ClassLoader classLoader) {
        try {
            Class<?> clockEx = Class.forName(OPLUS_CLOCK_EX, false, classLoader);
            XposedBridge.hookAllMethods(clockEx, "setTextWithRedOneStyle", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        TextView view = sClockView;
                        if (view != null) {
                            // 系统该方法会 Handler.post 一个异步 Runnable 再 setText 并套「红1」样式，
                            // 这里同样 post 一次补色，排在它之后执行，保证最后写入的是我们的颜色。
                            scheduleReapply(view);
                        }
                    } catch (Throwable ignored) {
                        // 上色失败不影响时钟本身
                    }
                }
            });
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "OplusClockEx setTextWithRedOneStyle hook installed");
            }
            // 真正的 setText 在 setTextWithRedOneStyle post 出来的异步 Runnable 里执行；
            // 在它「之后」再补色，才能压过系统随后应用的「红1」样式（这是「一秒就掉」的关键）。
            try {
                Class<?> runnable = Class.forName(
                        OPLUS_CLOCK_EX + "$setTextWithRedOneStyle$1", false, classLoader);
                XposedBridge.hookAllMethods(runnable, "run", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            TextView view = sClockView;
                            if (view != null) {
                                scheduleReapply(view);
                            }
                        } catch (Throwable ignored) {
                            // 忽略
                        }
                    }
                });
                if (BuildConfig.DEBUG) {
                    XposedBridge.log(LOG_PREFIX + "OplusClockEx $1#run hook installed");
                }
            } catch (Throwable ignored) {
                // 内部类名随混淆变化 → 跳过
            }
        } catch (Throwable ignored) {
            // 非 Oplus 时钟实现 → 跳过
        }
    }

    /**
     * ColorOS 17：把本模块的显秒开关同步到系统原生显秒键，让系统自身渲染秒。
     * 非 ColorOS 17（{@link #sNativeSeconds} 为假）时直接返回，不影响旧机型。
     */
    private static void mirrorNativeSeconds(ContentResolver cr, boolean enabled) {
        if (!sNativeSeconds || cr == null) {
            return;
        }
        try {
            Settings.Secure.putInt(cr, NATIVE_SECONDS_SETTING, enabled ? 1 : 0);
        } catch (Throwable ignored) {
            // 写入失败不影响我们自己的 setText 显秒
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
        // 缓存时钟视图与其屏幕边界（供全局 dispatchTouchEvent 监听做点击/长按命中判断）。
        // 必须放在 CONFIGURED 去重检查之前：WeakHashMap 依赖 equals/hashCode，而部分厂商时钟
        // （如 Oplus StatClock）覆写了 equals，不同实例会被误判为「已配置」而提前 return，
        // 导致 sClockView 永远赋值不上 → 长按命中区为空 → 长按无反应（C16 实测复现）。
        // 不再给时钟挂 OnTouchListener：ColorOS 通知面板会拦截状态栏区域触摸，时钟自身收不到事件；
        // 全局监听在 dispatchTouchEvent 层命中时钟边界，且不消费事件，下拉通知栏不受影响。
        try {
            // 只在「真的显示在状态栏」时抢占缓存（HyperOS「秒数卡住」的根因就在这里）：
            // 小米 HyperOS 状态栏同时存在 statusbar.policy.Clock / statusbar.views.MiuiClock 等多个实例，
            // 且 AOD、锁屏、下拉面板里的 Clock 也在每秒 setText（尺寸 0x0、不可见）。
            // 旧逻辑无条件 sClockView = view，缓存每秒都被劫持到 0x0 隐藏实例上；用户点一下关显秒时，
            // 文本写进了那个不可见视图，真正显示的时钟停在最后那次带秒的文本上
            // → 用户看到「秒数卡住不动」，要等系统下一次分钟更新（几秒到一分钟）才恢复。
            if (isLiveStatusBarClock(view) || sClockView == null) {
                sClockView = view;
                refreshClockRect(view);
                if (BuildConfig.DEBUG) {
                    XposedBridge.log(LOG_PREFIX + "clock view cached; bounds=" + sClockRect
                            + " gesture=" + ClockSecondsConfig.getGesture(view.getContext().getContentResolver()));
                }
            }
        } catch (Throwable ignored) {
            // 缓存失败不影响显示
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
    }

    /** 刷新时钟视图当前屏幕边界（每次配置时调用，覆盖旋转/状态变化；按下时也会再刷新一次）。 */
    private static void refreshClockRect(View view) {
        try {
            int[] loc = new int[2];
            view.getLocationOnScreen(loc);
            synchronized (sClockRectLock) {
                sClockRect.set(loc[0], loc[1], loc[0] + view.getWidth(), loc[1] + view.getHeight());
            }
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "refreshClockRect: bounds=" + sClockRect
                        + " size=" + view.getWidth() + "x" + view.getHeight()
                        + " attached=" + view.isAttachedToWindow());
            }
        } catch (Throwable ignored) {
            // 视图未布局时拿不到，等下次配置/按下再刷新
        }
    }

    /**
     * 从已配置的时钟视图里挑一个「真正在状态栏区域、且已布局」的视图。
     *
     * <p>用于修正两种情况：① 首次缓存时视图还没布局（bounds 全 0）；② 误缓存到下拉面板/QS 面板
     * 的时钟（面板关闭时 0 尺寸，命中区永远为空 → 长按没反应）。状态栏时钟在最上方，取 y 最小者。</p>
     */
    private static TextView pickClockView() {
        TextView best = null;
        int bestY = Integer.MAX_VALUE;
        int[] loc = new int[2];
        int total = 0, laid = 0, inBar = 0;
        synchronized (CONFIGURED) {
            for (TextView v : CONFIGURED) {
                total++;
                try {
                    if (!v.isAttachedToWindow() || v.getWidth() <= 0 || v.getHeight() <= 0) {
                        continue;
                    }
                    laid++;
                    v.getLocationOnScreen(loc);
                    if (loc[1] < 0 || loc[1] > 220) {   // 只认状态栏区域（顶部 220px 内）
                        continue;
                    }
                    // 同尺寸但被隐藏的「双胞胎」不能选：HyperOS 有 Clock + MiuiClock 两个实例，
                    // 选错那个会把文本写进看不见的视图（秒数卡住不动的根因）。
                    if (v.getVisibility() != View.VISIBLE || v.getAlpha() < 0.2f) {
                        continue;
                    }
                    inBar++;
                    if (loc[1] < bestY) {
                        bestY = loc[1];
                        best = v;
                    }
                } catch (Throwable ignored) {
                    // 单个视图异常不影响其它
                }
            }
        }
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "pickClockView: total=" + total + " laid=" + laid
                    + " inBar=" + inBar + " best=" + (best != null ? resourceName(best) + "@y=" + bestY : "null"));
        }
        return best;
    }

    // ==================================================================
    /** 是否「真的显示在状态栏上」：已挂载、可见、有尺寸、不透明、位于顶部状态栏区域。 */
    private static boolean isLiveStatusBarClock(TextView v) {
        try {
            if (v == null || !v.isAttachedToWindow()) {
                return false;
            }
            if (v.getVisibility() != View.VISIBLE || v.getAlpha() < 0.2f) {
                return false;
            }
            if (v.getWidth() <= 0 || v.getHeight() <= 0) {
                return false;
            }
            int[] loc = new int[2];
            v.getLocationOnScreen(loc);
            return loc[1] >= 0 && loc[1] <= 220;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 当前所有「真的在状态栏上显示」的时钟实例快照。
     *
     * <p>HyperOS（小米）状态栏同时挂着 statusbar.policy.Clock 与 statusbar.views.MiuiClock
     * 等多个实例，只给其中一个写文本，另一个就会停在旧文本上（用户实际看到的是
     * 「点了关显秒，秒数卡住不动」）。所以凡是切换显秒/刷新配置，都要对全部存活实例各写一次。</p>
     */
    private static List<TextView> liveStatusBarClocks() {
        List<TextView> out = new ArrayList<>();
        synchronized (CONFIGURED) {
            for (TextView v : new ArrayList<>(CONFIGURED)) {
                try {
                    if (isClockEnabled(v) && isLiveStatusBarClock(v)) {
                        out.add(v);
                    }
                } catch (Throwable ignored) {
                    // 单个实例异常不影响其它
                }
            }
        }
        return out;
    }

    /**
     * 下拉通知面板顶部的大时钟（ColorOS：oplus_qs_clock / OplusQSClock）。
     *
     * <p>它和状态栏时钟一样是 TextView，setText 钩子本来就能拿到，但 isClockNameExcluded
     * 刻意排除了 qsclock（三星 QSClockIndicatorView 资源名就是 clock，误中会把 sClockView
     * 指到面板时钟，面板收起时 0 尺寸 → 长按热区为空）。这里单独识别它，只用于
     * 「同样显秒、同样上色」，绝不写 sClockView、绝不参与触摸切换。</p>
     */
    private static boolean isQsClock(TextView view) {
        if (view == null) {
            return false;
        }
        try {
            // ColorOS 16 实测两个都被 setText 覆盖：
            //   qs_footer_clock | SimpleQsClock  → 下拉通知面板顶部那个大时钟（旁边带「10月31日 星期六」）
            //   oplus_qs_clock  | OplusQSClock   → 二次下拉的控制中心大时钟
            String name = resourceName(view);
            if ("qs_footer_clock".equals(name) || "oplus_qs_clock".equals(name)) {
                return true;
            }
            String cls = view.getClass().getName().toLowerCase(Locale.ROOT);
            // SimpleQsClock / OplusQSClock / 三星 QSClockIndicatorView 都落在这里。
            // 这类视图以前被 isClockNameExcluded 排除（怕抢走 sClockView），
            // 现在走独立通道、绝不写 sClockView，所以可以放心接管。
            return cls.contains("qsclock");
        } catch (Throwable t) {
            return false;
        }
    }

    /** 面板时钟是否真的在显示：面板收起时会 detach 或变 0 尺寸，此时不能给它装 ticker。 */
    private static boolean isLiveQsClock(TextView view) {
        try {
            // isShown() 同时要求「自己 + 所有祖先都 VISIBLE + 已 attach + 窗口可见」，
            // 面板收起（QS 窗口 dismiss）时立刻变 false —— 用它才能及时停掉面板时钟的每秒写入。
            return view != null && view.isShown() && view.getAlpha() >= 0.2f
                    && view.getWidth() > 0 && view.getHeight() > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    /** 见过并接管的全部面板时钟实例（弱引用，面板回收后自动消失）。 */
    private static final java.util.Set<TextView> QS_CLOCKS =
            Collections.newSetFromMap(new WeakHashMap<TextView, Boolean>());

    /** 存活的面板时钟实例（与 liveStatusBarClocks 分开：面板时钟的 y 远超状态栏区域）。 */
    private static List<TextView> liveQsClocks() {
        List<TextView> out = new ArrayList<>();
        synchronized (QS_CLOCKS) {
            for (TextView v : new ArrayList<>(QS_CLOCKS)) {
                try {
                    if (isClockEnabled(v) && isLiveQsClock(v)) {
                        out.add(v);
                    }
                } catch (Throwable ignored) {
                    // 单个实例异常不影响其它
                }
            }
        }
        return out;
    }

    /** 递增序号：保证「延迟补写」只对最后一次切换生效（用户快速连点时不会把旧状态写回去）。 */
    private static volatile int sApplySeq;

    /**
     * 按当前显秒状态刷新所有存活时钟，并在 250ms / 800ms 后再各补写一次。
     *
     * <p>补写的作用：系统（HyperOS 的 AOSP Clock、下拉面板/AOD 时钟、第三方主题模块）会在我们
     * 写入之后继续按自己的格式 setText；若最后一次写入发生在别的实例上，可见时钟就停在旧文本，
     * 肉眼就是「秒数卡住几秒到一分钟」。这里保证切换后的最后写入一定是我们。</p>
     */
    private static void applySecondsToAllClocks() {
        final int seq = ++sApplySeq;
        writeSecondsStateNow();
        Handler h = sTouchHandler;
        if (h == null) {
            return;
        }
        for (long delay : new long[]{250L, 800L}) {
            h.postDelayed(() -> {
                if (seq == sApplySeq) {
                    writeSecondsStateNow();
                }
            }, delay);
        }
    }

    /** 立刻把当前显秒状态写一遍到所有存活时钟（不做延迟补写）。 */
    private static void writeSecondsStateNow() {
        List<TextView> clocks = new ArrayList<>(liveStatusBarClocks());
        clocks.addAll(liveQsClocks());
        boolean on = secondsOn();
        if (!clocks.isEmpty()) {
            for (TextView v : clocks) {
                try {
                    if (on) {
                        writeTime(v);
                        restartTicker(v);
                    } else {
                        stopTicker(v);
                        writeMinuteOnly(v);
                    }
                } catch (Throwable t) {
                    noteHookError(t);
                }
            }
            return;
        }
        // 没有存活实例（还没布局/被隐藏）时回退到缓存视图，保持旧行为。
        TextView clock = sClockView;
        if (clock == null || !isClockEnabled(clock)) {
            return;
        }
        try {
            if (on) {
                writeTime(clock);
                restartTicker(clock);
            } else {
                stopTicker(clock);
                writeMinuteOnly(clock);
            }
        } catch (Throwable t) {
            noteHookError(t);
        }
    }

    // 测试卡片（诊断）：候选时钟清单与逐项启用
    // ==================================================================

    /** 候选时钟的稳定 key：类名|资源名|父容器类名（能区分状态栏/通知栏同名时钟）。 */
    private static String clockKey(TextView view) {
        try {
            String cn = view.getClass().getName();
            String rn = resourceName(view);
            ViewParent p = view.getParent();
            String pn = p != null ? p.getClass().getName() : "?";
            return cn + "|" + rn + "|" + pn;
        } catch (Throwable t) {
            return view.getClass().getName() + "|?|?";
        }
    }

    /** 候选时钟给人看的标签：资源名 · 简类名。 */
    private static String clockLabel(TextView view) {
        try {
            String rn = resourceName(view);
            String simple = view.getClass().getSimpleName();
            return (rn.isEmpty() ? "?" : rn) + " · " + simple;
        } catch (Throwable t) {
            return view.getClass().getSimpleName();
        }
    }

    /** 该时钟是否被用户启用（测试卡片勾选）。缺省（无记录）视为启用。 */
    private static boolean isClockEnabled(TextView view) {
        Boolean v = sClockEnabled.get(clockKey(view));
        return v == null || v;
    }

    /** 把当前候选清单发布到 Secure（设置页读取）。节流：≥1.5s 才重写一次；force=true 时忽略节流（用户主动刷新）。 */
    private static void publishClockList(ContentResolver cr) {
        publishClockList(cr, false);
    }

    private static void publishClockList(ContentResolver cr, boolean force) {
        try {
            long now = SystemClock.uptimeMillis();
            if (!force && now - sLastListPublish < 1500L) {
                return;
            }
            sLastListPublish = now;
            org.json.JSONArray arr = new org.json.JSONArray();
            for (java.util.Map.Entry<String, String> e : sClockInfo.entrySet()) {
                org.json.JSONObject o = new org.json.JSONObject();
                o.put("k", e.getKey());
                o.put("l", e.getValue());
                arr.put(o);
            }
            Settings.Secure.putString(cr, ClockSecondsConfig.CLOCK_TEST_LIST_KEY, arr.toString());
        } catch (Throwable ignored) {
            // 发布失败不影响显秒
        }
    }

    /** 从 Secure 读取用户对候选时钟的启用选择（JSON 对象 key->bool）。 */
    private static java.util.Map<String, Boolean> loadClockEnabledMap(ContentResolver cr) {
        java.util.Map<String, Boolean> map = new java.util.HashMap<>();
        try {
            String s = Settings.Secure.getString(cr, ClockSecondsConfig.CLOCK_TEST_ENABLED_KEY);
            if (s != null && !s.isEmpty()) {
                org.json.JSONObject obj = new org.json.JSONObject(s);
                java.util.Iterator<String> it = obj.keys();
                while (it.hasNext()) {
                    String k = it.next();
                    map.put(k, obj.getBoolean(k));
                }
            }
        } catch (Throwable ignored) {
            // 解析失败按全启用处理
        }
        return map;
    }

    /** 把用户对候选时钟的启用选择写回 Secure（JSON 对象 key->bool）。 */
    private static void writeClockEnabledMap(ContentResolver cr,
                                             java.util.Map<String, Boolean> map) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject();
            for (java.util.Map.Entry<String, Boolean> e : map.entrySet()) {
                obj.put(e.getKey(), e.getValue());
            }
            Settings.Secure.putString(cr, ClockSecondsConfig.CLOCK_TEST_ENABLED_KEY, obj.toString());
        } catch (Throwable ignored) {
            // 写入失败不影响显秒
        }
    }

    /**
     * 用户对某候选时钟的启用状态变更后，立即对当前已配置/候选时钟重算：
     * 停用的停 ticker 并还原系统原文本；新启用的立即接管（配置+显秒）。
     */
    private static void applyClockTestToActive(ContentResolver cr) {
        sClockEnabled = loadClockEnabledMap(cr);
        // 已配置时钟：停用的还原并移出，启用的补上显秒。
        synchronized (CONFIGURED) {
            for (TextView v : new ArrayList<>(CONFIGURED)) {
                try {
                    if (!isClockEnabled(v)) {
                        stopTicker(v);
                        CharSequence base = BASE_TEXT.get(v);
                        if (base != null) {
                            WRITING_CLOCK.set(true);
                            try {
                                v.setText(base);
                            } finally {
                                WRITING_CLOCK.set(false);
                            }
                        }
                        CONFIGURED.remove(v);
                    } else if (secondsOn() && isStatusBarClock(v)) {
                        installTicker(v);
                        refreshClock(v);
                    }
                } catch (Throwable ignored) {
                    // 单个视图异常不影响其它
                }
            }
        }
        // 候选池中所有被停用的时钟（含尚未进入 CONFIGURED 的）：停 ticker + 还原系统原文本，
        // 确保「全部关闭」后不再残留秒/小时颜色（= 系统默认观感）。
        for (WeakReference<TextView> ref : sClockCandidates) {
            TextView v = ref.get();
            if (v != null && !isClockEnabled(v)) {
                try {
                    stopTicker(v);
                    CharSequence base = BASE_TEXT.get(v);
                    if (base != null) {
                        WRITING_CLOCK.set(true);
                        try {
                            v.setText(base);
                        } finally {
                            WRITING_CLOCK.set(false);
                        }
                    }
                    synchronized (CONFIGURED) {
                        CONFIGURED.remove(v);
                    }
                } catch (Throwable ignored) {
                    // 单个视图异常不影响其它
                }
            }
        }
        // 候选池里尚未配置但被启用且符合状态栏规则的时钟：立即接管。
        for (WeakReference<TextView> ref : sClockCandidates) {
            TextView v = ref.get();
            if (v != null && isStatusBarClock(v) && isClockEnabled(v)
                    && secondsOn() && !CONFIGURED.contains(v)) {
                try {
                    configureClockView(v);
                    // 关键：停用时 stopTicker 已把秒表移除，重新启用必须重新装秒表，
                    // 否则只写一次秒（refreshClock）不会继续跳动，要等下一次系统 setText
                    // （最长约 1 分钟）才恢复 → 表现为「关闭再打开后秒数有时不动」。
                    installTicker(v);
                    refreshClock(v);
                } catch (Throwable ignored) {
                    // 单个视图异常不影响其它
                }
            }
        }
        publishClockList(cr);
    }

    /**
     * 一次性全量诊断：收集候选时钟（资源名/类名/父容器/尺寸/坐标/是否状态栏/是否启用/是否配置/是否被选中）、
     * 当前命中区、最近一次按下的命中结论，并给出「未识别 / 边界错 / 正常」的结论。
     * 由 {@link ClockSecondsConfig#ACTION_RUN_DIAGNOSTIC} 触发，结果写 Secure 并回传广播给设置页。
     */
    private static org.json.JSONObject buildDiagnostic(ContentResolver cr) {
        org.json.JSONObject root = new org.json.JSONObject();
        try {
            root.put("secondsEnabled", sSecondsEnabled);
            root.put("secondsOn", secondsOn());
            root.put("taskShow", sTaskShow);
            root.put("taskCount", sTasks.size());
            root.put("gesture", ClockSecondsConfig.getGesture(cr));
            root.put("lastTouchX", (int) sLastTouchX);
            root.put("lastTouchY", (int) sLastTouchY);
            root.put("lastTouchTime", sLastTouchTime);
            root.put("lastHit", sLastHit);

            // 候选时钟清单（每个命中「数字:数字」时间格式的非排除 TextView）。
            org.json.JSONArray arr = new org.json.JSONArray();
            int[] loc = new int[2];
            int statusBarCount = 0;
            for (WeakReference<TextView> ref : sClockCandidates) {
                TextView v = ref.get();
                if (v == null) {
                    continue;
                }
                try {
                    org.json.JSONObject o = new org.json.JSONObject();
                    int w = v.getWidth(), h = v.getHeight();
                    boolean attached = v.isAttachedToWindow();
                    boolean laid = w > 0 && h > 0;
                    boolean sb = isStatusBarClockBuiltin(v);
                    boolean en = isClockEnabled(v);
                    boolean conf;
                    synchronized (CONFIGURED) {
                        conf = CONFIGURED.contains(v);
                    }
                    v.getLocationOnScreen(loc);
                    o.put("key", clockKey(v));
                    o.put("label", clockLabel(v));
                    o.put("res", resourceName(v));
                    o.put("cls", v.getClass().getName());
                    ViewParent p = v.getParent();
                    o.put("parent", p != null ? p.getClass().getName() : "?");
                    o.put("w", w);
                    o.put("h", h);
                    o.put("x", loc[0]);
                    o.put("y", loc[1]);
                    o.put("attached", attached);
                    o.put("laidOut", laid);
                    o.put("isStatusBar", sb);
                    o.put("enabled", en);
                    o.put("configured", conf);
                    o.put("picked", (v == sClockView));
                    arr.put(o);
                    if (sb && laid) {
                        statusBarCount++;
                    }
                } catch (Throwable ignored) {
                    // 单个视图异常不影响其它
                }
            }
            root.put("candidates", arr);
            root.put("candidateCount", arr.length());
            root.put("statusBarLaidCount", statusBarCount);

            // 当前命中区（每次按下重选的最上方状态栏时钟）。
            TextView best = pickClockView();
            org.json.JSONObject pr = new org.json.JSONObject();
            synchronized (sClockRectLock) {
                pr.put("rect", sClockRect.flattenToString());
            }
            pr.put("pickedKey", best != null ? clockKey(best) : "null");
            pr.put("pickedLabel", best != null ? clockLabel(best) : "null");
            pr.put("pickedEmpty", best == null);
            root.put("pick", pr);

            // 颜色/span 体检：检查模块实际接管的状态栏时钟文本是否还带「小时颜色」span。
            // 用于在 App 内直接判断 ColorOS 17 掉色（span 被系统 setText 重建时洗掉），无需抓 logcat。
            org.json.JSONObject cc = new org.json.JSONObject();
            TextView colorClock = sClockView != null ? sClockView : best;
            boolean hourColorOn = ClockSecondsConfig.isHourColorEnabled(cr);
            cc.put("hourColorEnabled", hourColorOn);
            if (colorClock == null) {
                cc.put("applicable", false);
                cc.put("state", "no_clock");
            } else {
                CharSequence ct = colorClock.getText();
                String ctText = ct == null ? "" : ct.toString().replace("\u200B", "");
                boolean isSpanned = ct instanceof Spanned;
                boolean hasSpan = false;
                int spanColor = -1, spanStart = -1, spanEnd = -1;
                if (isSpanned) {
                    Spanned sp = (Spanned) ct;
                    ForegroundColorSpan[] fs = sp.getSpans(0, sp.length(),
                            ForegroundColorSpan.class);
                    if (fs.length > 0) {
                        hasSpan = true;
                        spanColor = fs[0].getForegroundColor();
                        spanStart = sp.getSpanStart(fs[0]);
                        spanEnd = sp.getSpanEnd(fs[0]);
                    }
                }
                boolean clockEnabled = isClockEnabled(colorClock);
                String state;
                if (!clockEnabled) {
                    state = "disabled";        // 测试卡里已关闭：不接管、不上色（正常）
                } else if (!hourColorOn) {
                    state = "hour_color_off";  // 小时颜色总开关关闭：不上色（正常）
                } else if (!isSpanned) {
                    state = "stripped";        // 文本被洗成非 Spanned = ColorOS 17 掉色典型
                } else if (!hasSpan) {
                    state = "no_span";         // 是 Spanned 但没有颜色 span = 掉色/未上色
                } else {
                    state = "ok";
                }
                cc.put("applicable", true);
                cc.put("state", state);
                cc.put("isSpanned", isSpanned);
                cc.put("hasColorSpan", hasSpan);
                cc.put("spanColor", spanColor);
                cc.put("spanStart", spanStart);
                cc.put("spanEnd", spanEnd);
                cc.put("expectedColor", ClockSecondsConfig.getDigitColors(cr)[0]);
                cc.put("expectedDigitColors",
                        java.util.Arrays.toString(ClockSecondsConfig.getDigitColors(cr)));
                cc.put("text", ctText.length() > 16 ? ctText.substring(0, 16) : ctText);
            }
            root.put("colorCheck", cc);

            // 结论：分三层定位根因。
            String verdict;
            if (arr.length() == 0) {
                verdict = "未检测到任何候选时钟：模块可能未注入 SystemUI，或当前没有显示「数字:数字」"
                        + "时间格式的 TextView 被 setText 命中（先确认模块已在 LSPosed 启用并勾选 SystemUI）。";
            } else if (statusBarCount == 0) {
                verdict = "状态栏时钟未被识别：内置规则与自定义规则都未命中任何「已布局」的时钟视图"
                        + "（资源名不在内置名单、或类名被排除如 qsclock/keyguard/aod）。"
                        + "建议：到「时钟识别」卡片点「自动检测」或手动填写状态栏时钟的资源名。";
            } else if (best == null) {
                verdict = "状态栏时钟已识别但命中区为空（pickClockView 返回 null）：所有候选时钟都未布局"
                        + "（bounds 全 0）或被下拉面板时钟覆盖（sClockView 指向了面板时钟）。";
            } else {
                synchronized (sClockRectLock) {
                    verdict = "状态栏时钟识别正常，命中区=" + sClockRect.flattenToString()
                            + "。若点击仍无反应：① 检查手势设置（当前="
                            + (ClockSecondsConfig.getGesture(cr) == ClockSecondsConfig.GESTURE_CLICK
                                ? "单击" : "双击")
                            + "）；② 最近一次按下 hit=" + sLastHit
                            + " @(" + (int) sLastTouchX + "," + (int) sLastTouchY + ")"
                            + "，若 hit=false 说明触摸点没落进命中区（边界算错或被面板拦截）。";
                }
            }
            root.put("verdict", verdict);
        } catch (Throwable t) {
            try {
                root.put("error", String.valueOf(t));
            } catch (Throwable ignored) {
                // 诊断自身异常不影响返回
            }
        }
        return root;
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
            // raw=原始 action（含 POINTER_DOWN/UP），masked=去掉指针索引的标准动作。
            // 单指长按全程只有标准 DOWN/MOVE/UP；多指连点会产生 POINTER_UP，
            // 必须清理长按任务（否则 500ms 后残留任务误弹浮窗），但不能 prematurely 结束主手势。
            int raw = ev.getAction();
            int action = ev.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                // 匹配规则是「任一命中即算」（OR 并集），某些 ROM 会同时匹配到多个时钟视图
                // （状态栏 + 下拉面板）。每次按下都重新挑一个「真正在状态栏、已布局」的，
                // 避免 sClockView 被后命中的面板时钟覆盖（那会导致长按命中区为 0 → 没反应）。
                TextView best = pickClockView();
                if (best != null) {
                    sClockView = best;
                }
                refreshClockRect(sClockView); // 每次按下刷新边界，覆盖旋转/状态变化
                float x = ev.getRawX(), y = ev.getRawY();
                boolean hit;
                // 用外扩后的热区判定：状态栏时钟矩形只有 ~156x80px，点偏几像素就落空。
                final Rect hitArea = tappableClockArea();
                hit = hitArea.contains((int) x, (int) y);
                // 记录最近一次按下的坐标、时间与命中结论，供「运行诊断」一次性输出。
                sLastTouchX = x;
                sLastTouchY = y;
                sLastTouchTime = System.currentTimeMillis();
                sLastHit = hit;
                if (BuildConfig.DEBUG) {
                    XposedBridge.log(LOG_PREFIX + "touch DOWN at (" + x + "," + y + ") rect="
                            + sClockRect + " area=" + hitArea + " hit=" + hit);
                }
                if (hit) {
                    // 先清掉可能残留的旧长按任务，防止泄漏累积导致误弹。
                    if (sLongPressTask != null) {
                        sTouchHandler.removeCallbacks(sLongPressTask);
                        sLongPressTask = null;
                    }
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
                    sTouchHandler.postDelayed(task, LONG_PRESS_MS);
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
            } else if (action == MotionEvent.ACTION_CANCEL) {
                sPendingTap = false;
                if (sLongPressTask != null) {
                    sTouchHandler.removeCallbacks(sLongPressTask);
                    sLongPressTask = null;
                }
            } else if (raw == MotionEvent.ACTION_POINTER_UP) {
                // 多指中一根抬起：清理长按任务防误弹，但保持 pending（主手势未结束）。
                if (sLongPressTask != null) {
                    sTouchHandler.removeCallbacks(sLongPressTask);
                    sLongPressTask = null;
                }
            } else if (action == MotionEvent.ACTION_UP) {
                // 最后一指抬起：无条件清理（连点误弹修复），并按手势判定点击/双击。
                boolean wasPending = sPendingTap;
                sPendingTap = false;
                if (sLongPressTask != null) {
                    sTouchHandler.removeCallbacks(sLongPressTask);
                    sLongPressTask = null;
                }
                if (wasPending) {
                    if (gesture == ClockSecondsConfig.GESTURE_CLICK) {
                        long dt = ev.getEventTime() - sDownTime;
                        float dx = Math.abs(ev.getRawX() - sDownX);
                        float dy = Math.abs(ev.getRawY() - sDownY);
                        if (dt < TAP_MAX_MS && dx < TAP_SLOP_PX && dy < TAP_SLOP_PX) {
                            long now = ev.getEventTime();
                            // 默认「单击」模式下用户顺手双击会连触发两次（开→关，看起来像没反应），
                            // 因此 TOGGLE_DEBOUNCE_MS 内的第二次点击视为同一次手势，不再取反。
                            if (now - sLastToggleAt >= TOGGLE_DEBOUNCE_MS) {
                                sLastToggleAt = now;
                                toggleSeconds(sClockView);
                            }
                        }
                    } else if (gesture == ClockSecondsConfig.GESTURE_DOUBLE_TAP) {
                        long dt = ev.getEventTime() - sDownTime;
                        float dx = Math.abs(ev.getRawX() - sDownX);
                        float dy = Math.abs(ev.getRawY() - sDownY);
                        if (dt < TAP_MAX_MS && dx < TAP_SLOP_PX && dy < TAP_SLOP_PX) {
                            // 450ms 内第二次点击才算双击；第一次只记录时间（卡顿设备上间隔常 >350ms）。
                            long now = ev.getEventTime();
                            if (now - sLastTapTime <= DOUBLE_TAP_MS) {
                                sLastTapTime = 0;
                                toggleSeconds(sClockView);
                            } else {
                                sLastTapTime = now;
                            }
                        }
                    }
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
                    // 半透明深色底座（配合窗口级模糊即毛玻璃效果）。悬浮时间不做呼吸灯。
                    GradientDrawable bg = new GradientDrawable();
                    bg.setCornerRadius(floatDp(context, 12));
                    bg.setColor(FLOAT_BG_BASE);
                    box.setBackground(bg);

                    // 时间：填满容器，点击它显示/隐藏按钮。
                    final TextView tv = new TextView(context);
                    tv.setTextSize(20f);
                    tv.setSingleLine(true);
                    tv.setEllipsize(null);
                    tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    // 表格数字（等宽数字）：否则每秒数字宽度不同，渐变色带宽度随之变化，
                    // 视觉上会像「渐变在流动」（预览卡早就设置了 tnum，浮窗漏了）。
                    tv.setFontFeatureSettings("tnum");
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

                    // 单击时间：显示 / 隐藏按钮；双击时间：切换状态栏显秒开关。
                    final long[] lastTap = {0L};
                    tv.setOnClickListener(v -> {
                        long now = SystemClock.uptimeMillis();
                        if (now - lastTap[0] <= 300) {
                            // 双击：切换状态栏时钟的显秒开关（与点击状态栏时间一致）。
                            lastTap[0] = 0;
                            if (sClockView != null) {
                                toggleSeconds(sClockView);
                            }
                        } else {
                            lastTap[0] = now;
                            boolean show = close.getVisibility() != View.VISIBLE;
                            close.setVisibility(show ? View.VISIBLE : View.GONE);
                            resize.setVisibility(show ? View.VISIBLE : View.GONE);
                        }
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
                    // 浮窗几何记忆：若用户此前拖动/拉伸保存过位置与尺寸则恢复；否则用默认并自动靠右上角。
                    final boolean[] restored = {false};
                    try {
                        int savedW = ClockSecondsConfig.getFloatW(context.getContentResolver());
                        int savedH = ClockSecondsConfig.getFloatH(context.getContentResolver());
                        int savedX = ClockSecondsConfig.getFloatX(context.getContentResolver());
                        int savedY = ClockSecondsConfig.getFloatY(context.getContentResolver());
                        int screenW = context.getResources().getDisplayMetrics().widthPixels;
                        int screenH = context.getResources().getDisplayMetrics().heightPixels;
                        if (savedW > 0 && savedH > 0 && savedX >= 0 && savedY >= 0
                                && savedW <= screenW && savedH <= screenH
                                && savedX + savedW <= screenW && savedY + savedH <= screenH) {
                            lp.width = savedW;
                            lp.height = savedH;
                            lp.x = savedX;
                            lp.y = savedY;
                            restored[0] = true;
                        }
                    } catch (Throwable ignored) {
                        restored[0] = false;
                    }
                    sFloatLp = lp;
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
                    // 测量完后把窗口推到右上角（靠右留 12dp）；已从记忆恢复位置则跳过。
                    box.post(() -> {
                        try {
                            if (sFloatView != box) {
                                return;
                            }
                            if (restored[0]) {
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

    /** 关闭预览浮窗（若有）并停掉刷新；同时把最终位置与尺寸记忆下来。 */
    private static void dismissFloatPreview() {
        try {
            if (sTouchHandler != null && sFloatTicker != null) {
                sTouchHandler.removeCallbacks(sFloatTicker);
            }
            sFloatTicker = null;
            if (sFloatView != null) {
                try {
                    // 浮窗关闭前先记忆几何（左上角坐标 + 尺寸），下次长按按记忆恢复。
                    if (sFloatLp != null && sFloatView.getWidth() > 0
                            && sFloatView.getHeight() > 0) {
                        ClockSecondsConfig.setFloatGeometry(
                                sFloatView.getContext().getContentResolver(),
                                sFloatLp.x, sFloatLp.y,
                                sFloatView.getWidth(), sFloatView.getHeight());
                    }
                } catch (Throwable ignored) {
                    // 记忆失败不影响关闭
                }
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
                sFloatLp = null;
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
    private static void updateFloatText(TextView tv, Context context,
                                        long offsetMs, WindowManager.LayoutParams lp) {
        try {
            long netNow = System.currentTimeMillis() + offsetMs;
            String text = new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date(netNow));

            SpannableStringBuilder sb = new SpannableStringBuilder(text);
            // 数字颜色用进程内缓存（设置变更经广播更新），首次读取一次。
            if (!sFloatColorLoaded) {
                try {
                    sFloatHourColorEnabled = ClockSecondsConfig.isHourColorEnabled(
                            context.getContentResolver());
                    reloadDigitColors(context);
                    sFloatColorLoaded = true;
                } catch (Throwable ignored) {
                    // 读不到用默认值
                }
            }
            // 自适应字号：取宽高允许的较小值，保证 "HH:mm:ss" 完整放进窗口。
            // 注意必须放在上色之前：渐变要按最终字号量文字宽度来定位色带。
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
                // 时间后缀会把整串撑宽（字号估算只按时间长度算），按实测宽度再收一次，
                // 否则后缀会被浮窗右边缘裁掉。
                float measured = tv.getPaint().measureText(sb, 0, sb.length());
                if (measured > availW && measured > 0f) {
                    tv.setTextSize(Math.max(10f, size * availW / measured));
                }
            } catch (Throwable ignored) {
                // 字号自适应失败就用当前字号
            }
            if (sFloatHourColorEnabled) {
                int[] colors = digitColors(tv);
                if (sGradientOn) {
                    // 色带起点必须落在「文字左边缘」。浮窗是居中的 TextView：若从视图原点（0）起算，
                    // 文字只看到色带的中后段，MIRROR 折叠后在文字中间出现「蓝顶」、两端回退 ——
                    // 就是用户看到的「悬浮时间的渐变和预览不一样」。
                    // 实测（1240x2772@560，band=411px，倍数扫参 -1…1）：xoff = +B/2 时文字正好覆盖
                    // 0→1 整条色带（t 0.08→0.81，与设置页预览卡的 0.11→0.79 同一条曲线）；
                    // 传 0 是「中间蓝顶两端回退」，传 −B/2 是整条反向。
                    float textW = tv.getPaint().measureText(sb, 0, sb.length());
                    float xoff = textW * 0.5f;
                    if (BuildConfig.DEBUG) {
                        // 扫参用：Secure 键里写一个乘数（如 "0" / "-1" / "0.5"），xoff = textW × 该值。
                        try {
                            String ov = android.provider.Settings.Secure.getString(
                                    context.getContentResolver(),
                                    "coloros_clock_seconds_debug_float_xoff");
                            if (ov != null && ov.length() > 0) {
                                xoff = textW * Float.parseFloat(ov);
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                    applyGradient(sb, tv, colors, xoff);
                } else {
                    int systemTextColor = tv.getCurrentTextColor();
                    for (int i = 0; i < sb.length(); i++) {
                        int slot = ClockSecondsConfig.slotOfChar(sb, i);
                        if (slot < 0 || slot >= colors.length
                                || colors[slot] == ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
                            continue;
                        }
                        sb.setSpan(new ForegroundColorSpan(colors[slot]), i, i + 1,
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
            tv.setText(sb);
            if (BuildConfig.DEBUG) {
                CharSequence cs = tv.getText();
                int gspans = -1;
                if (cs instanceof android.text.Spanned) {
                    gspans = ((android.text.Spanned) cs)
                            .getSpans(0, cs.length(), GradientTextSpan.class).length;
                }
                float band = tv.getPaint().measureText(sb, 0, sb.length());
                XposedBridge.log(LOG_PREFIX + "FLOAT text=" + text
                        + " size=" + Math.round(tv.getTextSize())
                        + " grad=" + sGradientOn + " gspans=" + gspans
                        + " band=" + Math.round(band)
                        + " tvW=" + tv.getWidth() + " tvH=" + tv.getHeight()
                        + " winW=" + (lp == null ? -1 : lp.width)
                        + " winH=" + (lp == null ? -1 : lp.height));
            }
        } catch (Throwable ignored) {
            // 文本刷新失败静默
        }
    }

    /**
     * 时钟的可点击热区：在真实时钟矩形外侧扩一圈
     * （左右 16dp、上 6dp、下 18dp，下沿夹到状态栏高度内，避免抢走应用内手势）。
     * 依据：澎湃 OS 用户日志里 14 次点空全部只差 4~12px，状态栏时钟矩形高只有 80px。
     */
    private static Rect tappableClockArea() {
        Rect out = new Rect();
        synchronized (sClockRectLock) {
            out.set(sClockRect);
        }
        if (out.isEmpty()) {
            return out;
        }
        try {
            Context ctx = sClockView != null ? sClockView.getContext() : null;
            if (ctx != null) {
                int mx = floatDp(ctx, 16);
                int left = out.left, right = out.right, top = out.top, bottom = out.bottom;
                out.left = Math.max(0, left - mx);
                out.right = right + mx;
                out.top = Math.max(0, top - floatDp(ctx, 6));
                out.bottom = bottom + floatDp(ctx, 18);
                int bar = statusBarHeight(ctx);
                if (bar > 0 && out.bottom > bar) {
                    out.bottom = bar;
                }
            }
        } catch (Throwable ignored) {
            // 尺寸取不到就退回原矩形
        }
        return out;
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
            // ColorOS 17：让系统原生显秒开关跟随本模块状态（非 ColorOS 17 时内部直接返回）。
            mirrorNativeSeconds(context.getContentResolver(), sSecondsEnabled);
            // 任务窗口决定「此刻到底显不显秒」，状态加载时一并算出来。
            reloadTasks(context);
            if (!sCustomRuleLoaded) {
                sCustomRule = ClockSecondsConfig.getClockRule(context.getContentResolver());
                sCustomRuleLoaded = true;
            }
            sStateLoaded = true;
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "STATE loaded enabled=" + sSecondsEnabled
                        + " rule=" + sCustomRule);
            }
        } catch (Throwable ignored) {
            // 读不到就保守保持默认（开启），下次再试
        }
    }

    /**
     * 任务窗口期间提示用户：此刻显不显秒由「自定义任务」决定，点一下改总开关也不会立刻生效，
     * 反而会在窗口结束后莫名其妙地变状态，所以这里直接不改开关、只给一句提示。
     */
    private static void toastTaskOverride(Context context) {
        try {
            String pushed = sToastTaskText;
            final String msg = (pushed == null || pushed.isEmpty())
                    ? "任务进行中：显秒已由「自定义任务」接管" : pushed;
            final Context ctx = context.getApplicationContext() != null
                    ? context.getApplicationContext() : context;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable ignored) {
        }
    }

    /** 点击 / 长按时钟：翻转显秒开关并立即刷新。 */
    private static void toggleSeconds(TextView view) {
        Context context = view.getContext();
        if (context == null) {
            return;
        }
        if (sTaskShow) {
            toastTaskOverride(context);
            return;
        }
        ContentResolver resolver = context.getContentResolver();
        boolean enabled = !ClockSecondsConfig.isEnabled(resolver);
        ClockSecondsConfig.setEnabled(resolver, enabled);
        sSecondsEnabled = enabled;
        mirrorNativeSeconds(resolver, enabled);
        publishEffectiveSeconds(resolver);
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "seconds toggled -> " + enabled);
        }
        // 该时钟已被用户停用：不写文本/颜色（仅更新全局显秒内存态）。
        // 但必须写「所有存活的时钟实例」，不能只写 sClockView 那一个：HyperOS 上状态栏同时存在
        // Clock + MiuiClock 两个实例，只写一个会让另一个停在旧文本上，表现为「秒数卡住不动」。
        if (isClockEnabled(view) || !liveStatusBarClocks().isEmpty() || !liveQsClocks().isEmpty()) {
            applySecondsToAllClocks();
        }
    }

    // ==================================================================
    // 文本覆写（保留系统原文本，追加的秒继承时钟文字色，仅追加秒）
    // ==================================================================

    /** 用「系统原文本 + :ss」覆写时钟（追加的秒继承系统时钟文字色，自动跟随 12/24 制）。 */
    private static void writeTime(TextView view) {
        publishSystemTextColor(view);
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        // 统一裁剪到「时:分」，再追加一个 :ss —— 无论系统/其它模块给的是 HH:mm、
        // HH:mm:ss，甚至已经出现双秒（19:53:00:02），最终都只会有一个秒。
        String hhmm = toHourMinute(base);
        int second = (int) ((System.currentTimeMillis() / 1000) % 60);
        String ss = String.format(Locale.US, ":%02d", second);
        String text = hhmm + ss;
        final int timeEnd = text.length();
        // 时间后缀（周几 / 时段）拼在时间之后、零宽空格之前
        // —— 零宽空格必须留在整串最末尾，写反就丢了 ColorOS 模式匹配防护。
        // 下拉面板时钟旁边本来就有日期（ColorOS 显示「10月31日 星期六」），
        // 再拼「周六」会重复，所以时间后缀只加给状态栏时钟。
        if (!isQsClock(view)) {
            text = appendMark(text, view.getContext());
        }
        // 零宽空格破坏 HH:mm:ss 模式匹配：ColorOS 17 的 StatClock / setTextWithRedOneStyle 以及
        // 第三方状态栏主题模块（如 dev.puitheme.StatusBarModule）会按 HH:mm(:ss) 模式重建文本，
        // 重建时剥掉外来 Span → 显秒开时自定义颜色被反复洗掉（用户反馈「颜色闪没 1~2 秒」）。
        // 与显秒关闭路径（applyRedToBaseText）同款修法：末尾加 \u200B，模式不再匹配，Span 得以存活。
        // 零宽字符不占宽度、不可见，系统下次更新会给新纯文本，由本方法再次追加，自洽。
        if (!text.endsWith("\u200B")) {
            text = text + "\u200B";
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        // ColorOS 专属样式：仅把「小时十位数」标红（如 13 的 1、05 的 0），其余（含秒）保持系统原色。
        // 注：状态栏与浮窗的秒数字均保持系统原色；呼吸高亮仅用于设置页预览卡的刻度与色块。
        applyDigitColors(sb, view, timeEnd);
        styleMark(sb, view, timeEnd);
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "WRITE: [" + sb + "] baseRed="
                    + (base instanceof android.text.Spannable)
                    + (isQsClock(view) ? " qs=" + resourceName(view) : ""));
        }
        // 幂等短路：字符没变就跳过 setText。
        // 【vc207 教训】这里绝不能被绕过：onDraw → refreshClock → writeTime → setText → invalidate
        // 会闭合成死循环，真机实测把 SystemUI 吃满一个核（UI 线程 + RenderThread 同时满载）。
        // 配置变化（开关 / 端点色）由 refreshClockNow 置 sForceRewrite 强制重写一次。
        if (!sForceRewrite && TextUtils.equals(view.getText(), sb)) {
            return;
        }
        WRITING_CLOCK.set(true);
        try {
            view.setText(sb);
            sForceRewrite = false;
        } finally {
            WRITING_CLOCK.set(false);
        }
    }

    /**
     * 只保留「时:分」部分：遇到第二个冒号就截断，去掉可能已存在的秒。
     * 这样后面再追加一个 :ss，就永远不会出现「双秒」（19:53:00:02）。
     */
    private static String toHourMinute(CharSequence base) {
        if (base == null) {
            return "";
        }
        String s = base.toString();
        int seen = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ':' || c == '：') {
                seen++;
                if (seen == 2) {
                    return s.substring(0, i);
                }
            }
        }
        return s;
    }

    /**
     * 统计文本里的冒号数量（含全角）。{@code HH:mm:ss} 有 2 个冒号，用于判断系统是否已经
     * 自带秒（ColorOS 17 原生显秒），从而避免我们再追加一次秒。
     */
    private static int countColons(CharSequence text) {
        if (text == null) {
            return 0;
        }
        int n = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':' || c == '：') {
                n++;
            }
        }
        return n;
    }

    /**
     * 按「每个数字位独立取色」为时钟文本上色：时十 / 时个 / 分十 / 分个 / 秒十 / 秒个
     * （位映射见 {@link ClockSecondsConfig#slotOfChar}）。颜色 -1 表示跟随系统（不上色）。
     * 颜色来自设置页，默认仅「时十」为红色（Material Red 500）。
     *
     * <p>可读性适配：状态栏背景会随应用切换明暗（系统时钟文字色随之变黑/变白）。
     * 背景为浅色（系统文字为深色）而所选颜色又很亮时（如白色、黄色），直接上色会看不见，
     * 因此把过亮的颜色压暗，保证在浅色状态栏上仍然清晰。</p>
     *
     * @return 本次上色的字符起始下标（空数组 = 无需/无位上色），供掉色判定使用
     */
    private static int[] applyDigitColors(SpannableStringBuilder sb, TextView view) {
        return applyDigitColors(sb, view, -1);
    }

    /**
     * 同上，额外限定「时间文本」终点：{@code timeEnd} 之后是后缀（周几 / 时段），
     * 既不参与逐位上色也不参与渐变（否则会被当成第 7、8 个字符算进色带）。
     * 传 -1 表示整串都是时间文本。
     */
    private static int[] applyDigitColors(SpannableStringBuilder sb, TextView view,
                                          int timeEnd) {
        if (!hourColorEnabled(view)) {
            // 总开关关闭：恢复系统默认样式，不加任何 Span。
            return NO_STARTS;
        }
        if (sGradientOn) {
            // 渐变模式：整串一个 CharacterStyle，颜色由 shader 生成，不再逐字符上色。
            return applyGradient(sb, view, digitColors(view), 0f, timeEnd);
        }
        int[] colors = digitColors(view);
        int systemTextColor = view.getCurrentTextColor();
        int[] starts = new int[sb.length()];
        int n = 0;
        // 后缀从 timeEnd 起，不参与逐位上色（它自己有一套字号 / 弱化色样式）。
        final int limit = (timeEnd > 0 && timeEnd < sb.length()) ? timeEnd : sb.length();
        for (int i = 0; i < limit; i++) {
            char ch = sb.charAt(i);
            if (ch == ':' || ch == '：') {
                // 冒号光学居中：逐位上色会把文本切成多个 run，冒号会被画在基线上（比整串一个 run 时
                // 低约 0.11em）。补一个只改绘制基线的 span，让状态栏与设置页预览的四个点位置一致。
                sb.setSpan(new ColonLiftSpan(), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                continue;
            }
            int slot = ClockSecondsConfig.slotOfChar(sb, i);
            if (slot < 0 || slot >= colors.length) {
                continue;
            }
            int color = colors[slot];
            // 先移除这一位上的外来前景色 Span（OplusClockExImpl 的 RedOne / 本体色、主题模块塞进来的色、
            // 以及上一轮自己加的），否则两个 ForegroundColorSpan 重叠时会互相压制，我们的颜色不生效。
            // 注意：这一步必须在「跟随系统」判断之前做 —— 别的模块会给某些位加一个**透明色 (#0)**
            // 的 ForegroundColorSpan，留着就等于把这一位从状态栏上抹掉（用户反馈：点「跟随系统」后
            // 那一位数字看不见了）。剥掉之后由系统文字色正常渲染。
            ForegroundColorSpan[] old = sb.getSpans(i, i + 1, ForegroundColorSpan.class);
            for (ForegroundColorSpan s : old) {
                sb.removeSpan(s);
            }
            if (color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
                continue;   // 该位跟随系统，去掉外来着色后交给系统文字色
            }
            int adapted = adaptColorForBackground(color, systemTextColor);
            sb.setSpan(new ForegroundColorSpan(adapted), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            starts[n++] = i;
        }
        if (n == 0) {
            return NO_STARTS;
        }
        int[] result = new int[n];
        System.arraycopy(starts, 0, result, 0, n);
        return result;
    }

    /**
     * 渐变上色：整串数字只挂一个 {@link GradientTextSpan}，颜色由 LinearGradient 生成。
     *
     * <p><b>为什么比逐位上色更省：</b>逐位 ForegroundColorSpan 是「6 个 span + 6 次颜色换算」，
     * 渐变只有「1 个 span + 1 次 shader 构建」，且完全寄生在时钟本来就有的重绘上——
     * 显秒开时 ticker 每秒写一次文本，相位按毫秒推进，不需要新增定时器、不需要 invalidate
     * 循环、不新增任何 View。开关状态来自缓存字段（{@link #sGradientOn}），每秒零 binder。</p>
     *
     * @return 需要维护颜色的字符下标（掉色判定沿用同一套逻辑）
     */
    private static int[] applyGradient(SpannableStringBuilder sb, TextView view, int[] colors) {
        return applyGradient(sb, view, colors, 0f);
    }

    /**
     * 同上，额外指定色带在画笔坐标系里的起点偏移。
     * 状态栏时钟文字从布局原点开始，传 0 即可；居中排版的 TextView（设置页预览卡、悬浮时间窗）
     * 必须传「文字实测宽度」，否则文字只落在 MIRROR 色带的后半段、视觉上整条渐变反向。
     */
    private static int[] applyGradient(SpannableStringBuilder sb, TextView view, int[] colors,
                                       float xOffset) {
        return applyGradient(sb, view, colors, xOffset, -1);
    }

    /**
     * 同上，额外限定色带终点：{@code colorEnd} 之后是后缀（周几 / 时段），不参与渐变
     * —— 否则后缀会把色带拉长，时间段的渐变终点就对不上。传 -1 表示整串都是时间文本。
     */
    private static int[] applyGradient(SpannableStringBuilder sb, TextView view, int[] colors,
                                       float xOffset, int colorEnd) {
        // 剥掉外来 / 上一轮的前景色：主题模块与 OplusClockExImpl 会塞 ForegroundColorSpan，
        // 其中还有 #0 透明色（会让数字直接消失），留着会和 shader 抢画笔。
        ForegroundColorSpan[] oldFg = sb.getSpans(0, sb.length(), ForegroundColorSpan.class);
        for (ForegroundColorSpan s : oldFg) {
            sb.removeSpan(s);
        }
        GradientTextSpan[] oldGradient = sb.getSpans(0, sb.length(), GradientTextSpan.class);
        for (GradientTextSpan g : oldGradient) {
            sb.removeSpan(g);
        }
        int len = sb.length();
        // 尾部零宽空格（防模式重建剥 span 用）不参与着色，否则右端会多算一格宽度。
        while (len > 0 && sb.charAt(len - 1) == '\u200B') {
            len--;
        }
        if (colorEnd > 0 && colorEnd < len) {
            len = colorEnd;   // 后缀不参与渐变
        }
        if (len <= 0) {
            return NO_STARTS;
        }
        int systemTextColor = view.getCurrentTextColor();
        // 起止色直接复用第 1 位与第 6 位已配置的颜色：零新增选色 UI，语义也直观
        // （「第一个数字的颜色」渐变到「最后一个数字的颜色」）。
        // 端点色：用户自定义优先；未自定义（GRADIENT_COLOR_AUTO）时沿用首位 / 末位数字的颜色，
        // 这样「不配置也能看到渐变」，配置了就完全按用户的颜色来。
        int rawStart = sGradientStart == ClockSecondsConfig.GRADIENT_COLOR_AUTO
                ? colors[0] : sGradientStart;
        int rawEnd = sGradientEnd == ClockSecondsConfig.GRADIENT_COLOR_AUTO
                ? colors[colors.length - 1] : sGradientEnd;
        int c0 = gradientEdgeColor(rawStart, systemTextColor);
        int c1 = gradientEdgeColor(rawEnd, systemTextColor);
        sb.setSpan(new GradientTextSpan(view, sb, c0, c1, xOffset), 0, len,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int[] starts = new int[len];
        int n = 0;
        for (int i = 0; i < len; i++) {
            if (ClockSecondsConfig.slotOfChar(sb, i) >= 0) {
                starts[n++] = i;
            }
        }
        if (n == 0) {
            return NO_STARTS;
        }
        int[] result = new int[n];
        System.arraycopy(starts, 0, result, 0, n);
        return result;
    }

    /** 渐变端点颜色：「跟随系统」的端点用系统文字色，并按底色压暗（与逐位上色同规则）。 */
    private static int gradientEdgeColor(int color, int systemTextColor) {
        int base = color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW ? systemTextColor : color;
        return adaptColorForBackground(base, systemTextColor);
    }


    /** 读取六位颜色缓存（首次或广播变更后读取，避免每秒 6 次 binder）。 */
    private static int[] digitColors(TextView view) {
        int[] cached = sDigitColors;
        if (cached != null) {
            return cached;
        }
        return reloadDigitColors(view.getContext());
    }

    /** 从 Secure 重读六位颜色并更新缓存。 */
    private static int[] reloadDigitColors(Context ctx) {
        int[] colors;
        try {
            colors = ClockSecondsConfig.getDigitColors(ctx.getContentResolver());
        } catch (Throwable t) {
            colors = new int[ClockSecondsConfig.DIGIT_COUNT];
            for (int i = 0; i < colors.length; i++) {
                colors[i] = ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
            }
            colors[0] = HOUR_COLOR_FALLBACK;
        }
        try {
            // 渐变开关与端点色同批读取：每秒渲染路径里只读缓存字段，不再打 binder。
            sGradientOn = ClockSecondsConfig.isGradientEnabled(ctx.getContentResolver());
            sGradientStart = ClockSecondsConfig.getGradientStartColor(ctx.getContentResolver());
            sGradientEnd = ClockSecondsConfig.getGradientEndColor(ctx.getContentResolver());
        } catch (Throwable ignored) {
            // 读取失败保持上一次值，不影响上色。
        }
        sDigitColors = colors;
        // 后缀配置（周几 / 时段）与颜色同批失效：下次渲染时按需重读，避免渲染路径打 binder。
        sMarkLoaded = false;
        return colors;
    }

    // ==================================================================
    // 时间后缀（周几 / 时段）：10:23:45 周六
    // ==================================================================

    /** 从 Secure 重读时间后缀配置（只在首次使用与收到 SET_MARK 广播后调用）。 */
    private static void loadMarkConfig(Context ctx) {
        try {
            android.content.ContentResolver cr = ctx.getContentResolver();
            sMarkMode = ClockSecondsConfig.getMarkMode(cr);
            sMarkPeriods = ClockSecondsConfig.splitMarkPeriods(
                    ClockSecondsConfig.getMarkPeriodsCsv(cr));
            sMarkOn = ClockSecondsConfig.isMarkEnabled(cr);
        } catch (Throwable ignored) {
            // 读不到就保持上一次的值（首次失败则维持关闭 + 兜底文案），不影响时钟本身。
        }
        sMarkLoaded = true;
    }

    /**
     * 时间后面的后缀文本（含前导空格）；关闭或取不到文案时返回空串。
     *
     * <p>周几直接用系统按当前语言给出的短名 —— {@link DateFormatSymbols#getShortWeekdays()}
     * 的数组下标正好是 {@link Calendar#DAY_OF_WEEK}（1=周日 … 7=周六），所以这个模式不需要
     * 设置页推文案；7 档时段是自定义措辞，必须由设置页按当前语言推过来（见 {@link #loadMarkConfig}），
     * 因为模块跑在 SystemUI 进程里、拿不到设置页的资源。</p>
     */
    private static String markSuffixText(Context ctx) {
        if (!sMarkLoaded) {
            loadMarkConfig(ctx);
        }
        if (!sMarkOn) {
            return "";
        }
        String label = "";
        try {
            Calendar cal = Calendar.getInstance();
            if (sMarkMode == ClockSecondsConfig.MARK_MODE_WEEKDAY) {
                String[] names = new DateFormatSymbols().getShortWeekdays();
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                if (dow >= 0 && dow < names.length) {
                    label = names[dow] == null ? "" : names[dow];
                }
            } else {
                String[] periods = sMarkPeriods;
                int idx = ClockSecondsConfig.periodIndexForHour(
                        cal.get(Calendar.HOUR_OF_DAY));
                if (periods != null && idx >= 0 && idx < periods.length
                        && periods[idx] != null) {
                    label = periods[idx];
                }
            }
        } catch (Throwable ignored) {
            // 取不到就不显示后缀（宁可不显示，也不能让每秒渲染路径抛异常）
        }
        return label.isEmpty() ? "" : " " + label;
    }

    /** 在时间文本后面追加后缀（关闭时原样返回）。 */
    private static String appendMark(CharSequence timeText, Context ctx) {
        String suffix = markSuffixText(ctx);
        return suffix.isEmpty() ? timeText.toString() : timeText + suffix;
    }

    /**
     * 给后缀上样式：字号缩到 0.62、颜色弱化（系统文字色 70% 不透明度）。
     * 这样一眼能读出「这是附加信息」，宽度也只占两个数字左右，不会把右侧图标挤走。
     *
     * @param markStart 后缀起点（= 时间文本长度）；{@code <= 0} 表示没有后缀。
     */
    private static void styleMark(SpannableStringBuilder sb, TextView view, int markStart) {
        if (markStart <= 0 || markStart >= sb.length()) {
            return;
        }
        int end = sb.length();
        while (end > markStart && sb.charAt(end - 1) == '\u200B') {
            end--;
        }
        if (end <= markStart) {
            return;
        }
        sb.setSpan(new android.text.style.RelativeSizeSpan(0.62f), markStart, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int sys = view.getCurrentTextColor();
        int dim = (sys & 0x00FFFFFF) | 0xB3000000;   // 70% 不透明度，保留背景色相
        sb.setSpan(new ForegroundColorSpan(dim), markStart, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    /** 目标文本所有已配置颜色的位是否仍带着前景色 Span（掉色判定用）。 */
    private static boolean hasAllColorSpans(Spanned sp, int[] starts) {
        // 渐变模式没有逐位 ForegroundColorSpan：整串只要还带着我们的 GradientTextSpan，
        // 就说明颜色存活，属于稳态，无需重写（否则会退化成每秒一次无谓 setText）。
        if (sp.getSpans(0, sp.length(), GradientTextSpan.class).length > 0) {
            return true;
        }
        for (int start : starts) {
            if (sp.getSpans(start, start + 1, ForegroundColorSpan.class).length == 0) {
                return false;
            }
        }
        return true;
    }

    /** 上一次已发布的系统文字色（避免每秒都写 Secure）。 */
    private static volatile int sPublishedSysTextColor = Integer.MIN_VALUE;

    /**
     * 按状态栏明暗适配颜色：转发到 {@link ClockSecondsConfig#adaptColorForBackground}，
     * 设置页预览与状态栏共用同一套规则（各写一份必然对不上）。
     */
    private static int adaptColorForBackground(int color, int systemTextColor) {
        return ClockSecondsConfig.adaptColorForBackground(color, systemTextColor);
    }

    /**
     * 把状态栏时钟**当前**的系统文字色记到 Secure，供设置页预览复现同一套配色。
     * 只在颜色变化时才写（换壁纸/换深浅色时各写一次），每秒调用也只是读一个静态字段。
     */
    private static void publishSystemTextColor(TextView view) {
        try {
            final int c = view.getCurrentTextColor();
            if (c == sPublishedSysTextColor) {
                return;
            }
            sPublishedSysTextColor = c;
            ClockSecondsConfig.setSystemTextColor(view.getContext().getContentResolver(), c);
        } catch (Throwable ignored) {
        }
    }

    /** 颜色总开关（默认启用）；读取失败按启用处理，保持原有观感。 */
    private static boolean hourColorEnabled(TextView view) {
        try {
            return ClockSecondsConfig.isHourColorEnabled(view.getContext().getContentResolver());
        } catch (Throwable t) {
            return true;
        }
    }

    /** 关闭显秒后，立即把文本回退为系统原文本，并应用小时十位数红色样式。 */
    private static void writeMinuteOnly(TextView view) {
        publishSystemTextColor(view);
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        // 同 applyRedToBaseText：零宽空格破坏 HH:mm 模式匹配，防 setText 链重建剥 span。
        // 顺带把系统可能自带的秒截掉（HyperOS 的 AOSP Clock 会按 HH:mm:ss 写入）：不截的话
        // 「关闭显秒」写回去的还是带秒文本，秒数看起来根本没关。
        String text = toHourMinute(base);
        final int timeEnd = text.length();
        // 时间后缀（周几 / 时段）拼在时间之后、零宽空格之前（零宽空格必须留在最末尾）。
        // 下拉面板时钟旁边本来就有日期（ColorOS 显示「10月31日 星期六」），
        // 再拼「周六」会重复，所以时间后缀只加给状态栏时钟。
        if (!isQsClock(view)) {
            text = appendMark(text, view.getContext());
        }
        if (!text.endsWith("\u200B")) {
            text = text + "\u200B";
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        APPLYING_STYLE.set(true);
        try {
            applyDigitColors(sb, view, timeEnd);
            styleMark(sb, view, timeEnd);
        } finally {
            APPLYING_STYLE.set(false);
        }
        WRITING_CLOCK.set(true);
        try {
            view.setText(sb);
        } finally {
            WRITING_CLOCK.set(false);
        }
    }

    /**
     * 对系统原文本应用各数字位的自定义颜色（不显秒时，系统每次更新分钟都会调用）。
     *
     * @return 0 = 无需写入（补色已存活 / 无需上色）；1 = 写了（文本变了，如正常分钟更新）；
     *         2 = 写了且是「真掉色」（文本字符一致但颜色 span 被洗掉了）。
     *         只有 2 才用于续期"兜底守护窗口"，避免分钟跳变被误判成掉色。
     */
    private static int applyRedToBaseText(TextView view) {
        CharSequence base = BASE_TEXT.get(view);
        if (base == null) {
            base = view.getText();
        }
        // ColorOS 17 掉色根因的精确修法：setText 链里存在「模式匹配式重建」——文本匹配
        // HH:mm 时钟格式时会被剥掉外来 Span 重铸（差分证据：秒数开的 HH:mm:ss 不被洗、
        // 秒数关的 HH:mm 一洗就没，同一 setText 路径唯一差别是格式）。分钟更新时刻系统
        // updateClock / 第三方主题模块（dev.puitheme.StatusBarModule，实测同链 hook）形成
        // 「写入风暴」，单次 scheduleReapply 赛不过最后的系统写入 → 颜色活不过一分钟。
        // 对策：补一个零宽空格 \u200B 破坏 HH:mm 完整匹配，重建逻辑放过我们的文本，
        // Span 自然存活。零宽字符不占布局宽度、不可见；系统下次分钟更新给新纯文本，
        // 由本方法再次追加，自洽。若真凶不是模式匹配，此字符无害，循环兜底。
        // 同上：base 可能自带秒（HyperOS 的 AOSP Clock 按 HH:mm:ss 写），显秒关时必须截掉。
        String text = toHourMinute(base);
        final int timeEnd = text.length();
        // 时间后缀（周几 / 时段）拼在时间之后、零宽空格之前（零宽空格必须留在最末尾）。
        // 下拉面板时钟旁边本来就有日期（ColorOS 显示「10月31日 星期六」），
        // 再拼「周六」会重复，所以时间后缀只加给状态栏时钟。
        if (!isQsClock(view)) {
            text = appendMark(text, view.getContext());
        }
        if (!text.endsWith("\u200B")) {
            text = text + "\u200B";
        }
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        APPLYING_STYLE.set(true);
        int[] starts;
        try {
            starts = applyDigitColors(sb, view, timeEnd);
        } finally {
            APPLYING_STYLE.set(false);
        }
        styleMark(sb, view, timeEnd);
        // ⚠️ 不能只用 TextUtils.equals：被洗后的纯文本与目标文本字符相同，会误判为
        // 「已上色」而跳过补救（掉色将永远无法恢复）。必须确认当前文本每一个已配置颜色
        // 的位置真的还带着 span 才跳过；目标无需上色（全部跟随系统/颜色开关关闭）时直接跳过。
        if (starts.length == 0) {
            return 0;   // 没有需要维护的颜色
        }
        CharSequence cur = view.getText();
        // 已有我们上色的文本（精确匹配含零宽空格）→ 稳态短路、零 setText。
        if (cur instanceof Spanned && cur.length() == sb.length()
                && cur.toString().contentEquals(sb.toString())
                && hasAllColorSpans((Spanned) cur, starts)) {
            LAST_WRITTEN.put(view, cur.toString().replace("\u200B", ""));
            return 0;
        }
        // 需要写入。区分「正常分钟跳变(1)」与「真掉色(2)」：看可见文本是否与我们上次写的一致。
        //  - 可见文本变了 → 分钟跳变，属正常更新，不续期兜底；
        //  - 可见文本没变却没颜色 → 被洗（真掉色），续期兜底。
        String curVisible = cur == null ? "" : cur.toString().replace("\u200B", "");
        String last = LAST_WRITTEN.get(view);
        int code = (last != null && last.equals(curVisible)) ? 2 : 1;
        WRITING_CLOCK.set(true);
        try {
            view.setText(sb);
        } finally {
            WRITING_CLOCK.set(false);
        }
        LAST_WRITTEN.put(view, curVisible);
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "PROBE color-applied text=[" + sb + "] strip=" + code);
            logSpans(view, "now");
            scheduleSpanDump(view);
        }
        return code;
    }

    /**
     * 仅 DEBUG：抓「直接剥 span」的真凶。PROBE setText 只能看到走 setText 的写入，
     * 若有模块（或系统）绕过 setText 直接 {@code text.removeSpan(...)} 洗掉我们的颜色，
     * 这里 hook {@code SpannableStringBuilder.removeSpan}：移除的是 ForegroundColorSpan
     * 且文本是时钟格式时打印完整调用栈（1s 节流），直接定位是谁干的。
     *
     * <p>过滤链（性能）：参数类型 → thisObject 类型 → 文本正则 → 节流。removeSpan 是
     * 系统热方法，但 ForegroundColorSpan 的移除远没那么高频，正则也只对 FCS 移除求值。</p>
     */
    private static void installSpanTheftProbe() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        try {
            XposedBridge.hookAllMethods(SpannableStringBuilder.class, "removeSpan",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            try {
                                if (WRITING_CLOCK.get() || APPLYING_STYLE.get()) {
                                    return;   // 我们自己的写入/换 span，跳过
                                }
                                if (!(param.args[0] instanceof ForegroundColorSpan)) {
                                    return;
                                }
                                if (!(param.thisObject instanceof SpannableStringBuilder)) {
                                    return;
                                }
                                String text = param.thisObject.toString();
                                if (!text.matches(".*\\d{1,2}[:：]\\d{2}.*")) {
                                    return;   // 只关心时钟格式文本
                                }
                                long now = SystemClock.uptimeMillis();
                                if (now - sLastSpanTheftLog < 1000L) {
                                    return;   // 1s 节流
                                }
                                sLastSpanTheftLog = now;
                                StackTraceElement[] st = Thread.currentThread().getStackTrace();
                                // 第一遍：折叠框架/hook 自身噪声，突出业务调用方。
                                StringBuilder sb = new StringBuilder(
                                        "SPAN-THEFT: ForegroundColorSpan removed from ["
                                                + text + "] by:");
                                for (StackTraceElement e : st) {
                                    String cn = e.getClassName();
                                    if (cn.startsWith("android.") || cn.startsWith("java.")
                                            || cn.startsWith("de.robv.")
                                            || cn.startsWith("org.lsposed.")
                                            || cn.startsWith("io.github.kelebao.")) {
                                        continue;
                                    }
                                    sb.append("\n    at ").append(cn).append('#')
                                            .append(e.getMethodName());
                                }
                                // 兜底：过滤后一帧不剩（真凶整条链都在框架内部）→ 输出完整栈，
                                // 否则日志只剩一行空头司令，等于没抓。
                                if (sb.indexOf("\n    at ") < 0) {
                                    sb.append(" (all frames filtered, full stack:)");
                                    for (StackTraceElement e : st) {
                                        sb.append("\n    at ").append(e.getClassName())
                                                .append('#').append(e.getMethodName());
                                    }
                                }
                                XposedBridge.log(LOG_PREFIX + sb);
                            } catch (Throwable ignored) {
                                // 探针绝不影响主流程
                            }
                        }
                    });
            XposedBridge.log(LOG_PREFIX + "span-theft probe installed (DEBUG only)");
        } catch (Throwable t) {
            XposedBridge.log(LOG_PREFIX + "span-theft probe install failed: " + t);
        }
    }

    /** 已挂过抓凶探针的时钟视图类（每类只挂一次）。 */
    private static final Set<Class<?>> HOOKED_CLOCK_CLASSES =
            Collections.synchronizedSet(new HashSet<Class<?>>());

    /**
     * 仅 DEBUG：抓凶探针 B（运行时版）——遇到状态栏时钟视图时，hook 它的**真实运行类**的
     * setText（每类一次），before/after 各打一行，看这个覆写方法「吃进什么、吐出什么」。
     *
     * <p>不用 Class.forName 按类名反射：模块加载时厂商时钟类通常还没加载，forName 易失败；
     * 且各家时钟类名不同（Oplus StatClock / 小米 MiuiClock / 三星 uniform…）。运行时取
     * {@code view.getClass()} 通杀所有厂商，且避开 TextView 基类（基类层由 SETTEXT-STRIP
     * 探针覆盖）。与 SETTEXT-STRIP 配合分层定位：before 带色、after 没有 → 剥色在该覆写层
     * （或其 super 内）；before/after 都带色 → 剥色在更上层（PROBE caller 链点名）。</p>
     */
    private static void maybeHookClockViewClass(TextView view) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        try {
            Class<?> clazz = view.getClass();
            if (clazz == TextView.class || clazz == android.widget.TextView.class) {
                return;   // 基类无需挂（SETTEXT-STRIP 已覆盖）
            }
            if (!HOOKED_CLOCK_CLASSES.add(clazz)) {
                return;   // 已挂过
            }
            XposedBridge.log(LOG_PREFIX + "clockclass probe: hooking " + clazz.getName());
            XposedBridge.hookAllMethods(clazz, "setText", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    try {
                        if (param.args == null || param.args.length == 0
                                || !(param.args[0] instanceof CharSequence)) {
                            return;
                        }
                        CharSequence arg = (CharSequence) param.args[0];
                        boolean fcs = arg instanceof Spanned
                                && ((Spanned) arg).getSpans(0, arg.length(),
                                        ForegroundColorSpan.class).length > 0;
                        XposedBridge.log(LOG_PREFIX + "CLOCKCLASS setText BEFORE: ["
                                + arg + "] fcs=" + fcs + " ours=" + WRITING_CLOCK.get());
                    } catch (Throwable ignored) {
                        // 探针绝不影响主流程
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        if (!(param.thisObject instanceof TextView)) {
                            return;
                        }
                        CharSequence stored = ((TextView) param.thisObject).getText();
                        boolean fcs = stored instanceof Spanned
                                && ((Spanned) stored).getSpans(0, stored.length(),
                                        ForegroundColorSpan.class).length > 0;
                        XposedBridge.log(LOG_PREFIX + "CLOCKCLASS setText AFTER: fcs="
                                + fcs + " stored=[" + stored + "] ours=" + WRITING_CLOCK.get());
                    } catch (Throwable ignored) {
                        // 探针绝不影响主流程
                    }
                }
            });
            XposedBridge.log(LOG_PREFIX + "clockclass probe installed: " + clazz.getName());
        } catch (Throwable t) {
            XposedBridge.log(LOG_PREFIX + "clockclass probe FAILED: " + t);
        }
    }

    /**
     * 仅 DEBUG：打印时钟当前文本上的 {@link ForegroundColorSpan}（范围 + 颜色）。
     *
     * <p>用途：如果某个模块在 setText <b>之后</b>直接改 Span（不走 setText，PROBE 抓不到），
     * 通过「上色后立刻 dump」与「400ms 后再 dump」对比，就能看出我们的颜色是否被替换/移除。</p>
     */
    private static void logSpans(TextView view, String tag) {
        if (!BuildConfig.DEBUG || view == null) {
            return;
        }
        try {
            CharSequence t = view.getText();
            if (!(t instanceof Spanned)) {
                XposedBridge.log(LOG_PREFIX + "SPAN " + tag + " text=[" + t + "] (not Spanned)");
                return;
            }
            Spanned sp = (Spanned) t;
            ForegroundColorSpan[] arr = sp.getSpans(0, sp.length(), ForegroundColorSpan.class);
            StringBuilder sb = new StringBuilder();
            for (ForegroundColorSpan s : arr) {
                sb.append('[').append(sp.getSpanStart(s)).append('-').append(sp.getSpanEnd(s))
                        .append(":#").append(Integer.toHexString(s.getForegroundColor())).append(']');
            }
            XposedBridge.log(LOG_PREFIX + "SPAN " + tag + " text=[" + t + "] n=" + arr.length + " " + sb);
        } catch (Throwable ignored) {
            // 探针本身不影响主流程
        }
    }

    /** 仅 DEBUG：上色后 400ms 再 dump 一次 Span（节流 ~1 次/秒），看颜色是否被后续覆盖。 */
    private static void scheduleSpanDump(final TextView view) {
        if (!BuildConfig.DEBUG || view == null) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        Long last = SPAN_DUMP_AT.get(view);
        if (last != null && now - last < 900L) {
            return;
        }
        SPAN_DUMP_AT.put(view, now);
        // 在 100ms / 500ms / 1200ms 各 dump 一次，覆盖「一秒内掉色」的时间窗。
        final long[] delays = {100L, 500L, 1200L};
        for (final long delay : delays) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        logSpans(view, "delayed+" + delay);
                    } catch (Throwable ignored) {
                        // 忽略
                    }
                }
            }, delay);
        }
    }

    /**
     * 把一次「只补色、不追加秒」的写入 post 到当前消息之后执行。
     *
     * <p>ColorOS 的 {@code OplusClockExImpl#setTextWithRedOneStyle} 会 {@code Handler.post} 一个异步
     * Runnable 再 setText 并套系统「红1」样式；其它状态栏模块（如 iOS 状态栏主题）也可能在 setText
     * 之后同步再套样式。同步的 after-hook 会被这些「后手」覆盖，所以把补色排到本次消息之后再上，
     * 确保最后一次写入是我们的颜色。</p>
     */
    private static void scheduleReapply(final TextView view) {
        if (view == null) {
            return;
        }
        view.post(new Runnable() {
            @Override
            public void run() {
                try {
                    // 用户已停用该时钟时不再补色/补秒，避免「全关仍有颜色」。
                    if (isStatusBarClock(view) && isClockEnabled(view)) {
                        if (secondsOn()) {
                            // 显秒开：重走 writeTime（会重新追加秒 + 上色），
                            // 避免被系统异步 Runnable 的纯文本覆盖后秒也一起丢。
                            writeTime(view);
                        } else {
                            int wrote = applyRedToBaseText(view);
                            if (wrote == 2) {
                                // 传感器：异步二次写把颜色洗掉了 → 续期本视图守护窗口并唤醒兜底。
                                installTicker(view);
                            }
                        }
                    }
                } catch (Throwable ignored) {
                    // 补色失败不影响时钟显示
                }
            }
        });
    }

    private static void refreshClock(TextView view) {
        // 下拉面板时钟（qs_footer_clock / oplus_qs_clock）也走这里：
        // isStatusBarClock 对它们是 false（当年为防抢 sClockView 而排除），只放行状态栏会让
        // 面板时钟「装了 ticker 却永远不写秒」——vc257/vc258 实测就是这个原因没生效。
        if ((isStatusBarClock(view) || isQsClock(view)) && isClockEnabled(view) && secondsOn()) {
            writeTime(view);
        }
    }

    // ==================================================================
    // 自定义任务：窗口计算与定时翻转
    // ==================================================================

    /** 从 Secure 重读任务列表，重算当前窗口并安排下一次翻转。 */
    private static void reloadTasks(Context context) {
        if (context == null) {
            return;
        }
        try {
            sTasks = io.github.kelebao.colorosclockseconds.ClockTask.parse(
                    ClockSecondsConfig.getTasksJson(context.getContentResolver()));
        } catch (Throwable t) {
            sTasks = Collections.emptyList();
        }
        evaluateTasks();
        publishEffectiveSeconds(context.getContentResolver());
    }

    /**
     * 把「此刻状态栏到底显不显秒」写给设置页预览用。
     *
     * <p>任务窗口会临时覆盖总开关，而设置页只能读 Secure，看不到 SystemUI 里的 sTaskShow/sTaskHide；
     * 没有这个键，到点开显秒时设置页预览仍停在 HH:mm（用户反馈）。</p>
     */
    private static void publishEffectiveSeconds(ContentResolver cr) {
        if (cr == null) {
            return;
        }
        try {
            // 只发布「任务窗口的覆盖值」：没有任务命中就写 -1，让设置页回落到总开关（避免读到过期值）。
            int override = sTaskShow ? 1 : -1;
            ClockSecondsConfig.setEffectiveSeconds(cr, override);
        } catch (Throwable t) {
            noteHookError(t);
        }
    }

    /** 重算任务窗口状态；状态变了立刻刷新时钟，并把定时器对齐到下一次翻转。 */
    private static void evaluateTasks() {
        try {
            long now = System.currentTimeMillis();
            boolean show = false;
            long next = Long.MAX_VALUE;
            for (io.github.kelebao.colorosclockseconds.ClockTask t : sTasks) {
                if (t == null || !t.enabled) {
                    continue;
                }
                if (t.windowContains(now)) {
                    show = true;
                }
                long nt = t.nextTransition(now);
                if (nt < next) {
                    next = nt;
                }
            }
            boolean changed = show != sTaskShow;
            sTaskShow = show;
            scheduleTaskTick(next == Long.MAX_VALUE ? -1L : next - now + 300L);
            if (changed) {
                if (BuildConfig.DEBUG) {
                    XposedBridge.log(LOG_PREFIX + "TASK state show=" + show);
                }
                applyTaskState();
            } else if (show) {
                // 兜底：窗口内每分钟重放一次。applyTaskState 在时钟视图还没抓到时会直接返回，
                // 而那时状态已经记为「已生效」，之后不再有 changed 事件 → 秒数永远不出现。
                applyTaskState();
            }
        } catch (Throwable t) {
            noteHookError(t);
        }
    }

    /** 安排下一次窗口翻转的定时器（delayMs <= 0 表示没有未来翻转，不排）。 */
    private static void scheduleTaskTick(long delayMs) {
        if (sTaskHandler == null) {
            sTaskHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        sTaskHandler.removeCallbacks(TASK_TICK);
        if (delayMs > 0) {
            // 上限 60 秒：不只为「任务被删/改后旧定时器残留」，更重要的兜底是——系统时间被
            // 手动改、时区切换、休眠唤醒后 uptimeMillis 与墙上时钟不再对齐，精确定时器会错过
            // 翻转点。每分钟重算一次窗口，最多迟 60 秒就能自我纠正（一次 Runnable，开销可忽略）。
            sTaskHandler.postDelayed(TASK_TICK, Math.min(delayMs, 60_000L));
        }
    }

    private static final Runnable TASK_TICK = new Runnable() {
        @Override
        public void run() {
            evaluateTasks();
        }
    };

    /** 任务把显秒翻了个面：按新状态重写时钟文本并同步 ticker。 */
    private static void applyTaskState() {
        TextView clock = sClockView;
        if (clock == null || !isClockEnabled(clock)) {
            return;
        }
        // 关键：显秒关着的时候 ticker 跑在「自适应补色」模式，下一次自我调度可能排到 30 秒后。
        // 若不先摘掉旧 ticker，任务到点开显秒后会沿用那条长延时，秒数僵住十几秒才恢复走动（用户实测「延迟好几秒」即此因）。
        for (TextView v : liveStatusBarClocks()) {
            stopTicker(v);
        }
        for (TextView v : liveQsClocks()) {
            stopTicker(v);
        }
        stopTicker(clock);
        publishEffectiveSeconds(clock.getContext() == null
                ? null : clock.getContext().getContentResolver());
        // 所有存活实例一起切（HyperOS 的 Clock + MiuiClock 双实例，只写一个会留下卡住的旧文本）。
        applySecondsToAllClocks();
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
            // 显秒关：给该时钟单独启动/续期守护窗口（窗口内稳定无掉色才会自动停）。
            // 注意：守护截止时间按"视图"独立保存，避免多个时钟共享一个静态导致
            // 一个时钟停了把其它时钟的守护窗口也误重置（旧版共享静态会导致 ticker 永不自动停）。
            if (!secondsOn()) {
                ticker.guardUntil = SystemClock.uptimeMillis() + COLOR_COOLDOWN_MS;
            }
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

    /**
     * 重启该视图的 1 秒定时器：显秒从「关」切到「开」时必须用它。
     *
     * <p>显秒关着的时候，已有 ticker 跑在「自适应补色」模式，下一次自我调度可能排到 4~30 秒后。
     * 只调 installTicker 会因为 {@code ACTIVE.containsKey(view)} 直接返回，那条长延时保持不变，
     * 于是刚打开的秒数盯着不动，要等几秒到几十秒才恢复走动（HyperOS 用户实测「几十秒」即此因）。</p>
     */
    private static void restartTicker(TextView view) {
        stopTicker(view);
        installTicker(view);
    }

    private static final class ClockTicker implements Runnable {
        private final WeakReference<TextView> reference;
        private long firstDetachedAt;
        /** 本视图补色兜底间隔（自适应：稳态拉长至 30s，被洗重置 2s）。 */
        private long tickDelay = 2_000L;
        /** 本视图的守护冷却截止时间（uptimeMillis）；窗口内一直没掉色 → 自动停掉本 ticker。0=未启用。 */
        private long guardUntil = 0L;

        ClockTicker(TextView view) {
            reference = new WeakReference<>(view);
        }

        @Override
        public void run() {
            TextView view = reference.get();
            if (view == null) {
                return;
            }
            // 该时钟已被用户停用（测试卡片取消勾选）：停止轮询并退出，不再补秒/补色。
            // 这样「全部关闭」后状态栏时间会回到系统默认观感（不残留秒/小时颜色）。
            if (!isClockEnabled(view)) {
                synchronized (ACTIVE) {
                    ACTIVE.remove(view);
                }
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
            // 下拉面板时钟：面板一收起就停掉本 ticker（否则会在不可见的视图上永久每秒 setText）。
            // 下次展开时由 setText / onDraw 钩子重新接管。
            if (isQsClock(view) && !isLiveQsClock(view)) {
                synchronized (ACTIVE) {
                    ACTIVE.remove(view);
                }
                return;
            }
            // 熄屏：暂停秒级刷新（让状态栏秒数冻结在最后一帧），亮屏后自动续走；
            // 仍保持每秒轮询，以便亮屏的瞬间立即恢复走秒。
            // 注：系统省电模式不再暂停走秒（vc221 按用户要求删除该行为）。
            Context ctx = view.getContext();
            if (secondsOn() && !isInteractive(ctx)) {
                view.postDelayed(this, 1000L);
                return;
            }
            if (secondsOn()) {
                refreshClock(view);
                // 对齐到下一秒边界，避免每秒漂移。
                long delay = 1000L - (System.currentTimeMillis() % 1000L);
                view.postDelayed(this, Math.max(50L, delay));
            } else {
                // 已关闭显秒：自适应兜底补色。
                //  · 掉色(wrote=2) → 2s 高频硬刚 + 续期本视图守护窗口；
                //  · 稳定(wrote=0/1) → 间隔自适应拉长（2→4→…→30s）；
                //  · 本视图冷却窗口(5分钟)内一直稳定 → 自动停掉本 ticker，只靠 setText 钩子 / scheduleReapply
                //    当"传感器"；一旦再掉色会重新唤醒。不掉色的设备 ticker 基本一直停着，最省电。
                int wrote = applyRedToBaseText(view);
                long now = SystemClock.uptimeMillis();
                if (guardUntil == 0L) {
                    // 刚进入显秒关（或从显秒开切回）：启动本视图一个新的守护窗口，避免用过期窗口立刻停。
                    guardUntil = now + COLOR_COOLDOWN_MS;
                }
                if (wrote == 2) {
                    // 真掉色（文本没变但颜色被洗）：2s 高频硬刚 + 续期本视图守护窗口。
                    tickDelay = 2_000L;
                    guardUntil = now + COLOR_COOLDOWN_MS;
                } else if (now >= guardUntil) {
                    // 本视图冷却窗口内始终没掉色 → 停掉兜底（移除本 ticker，不再 postDelayed）。
                    synchronized (ACTIVE) {
                        ACTIVE.remove(view);
                    }
                    guardUntil = 0L;
                    if (BuildConfig.DEBUG) {
                        XposedBridge.log(LOG_PREFIX + "color-guard stopped (stable "
                                + (COLOR_COOLDOWN_MS / 1000L) + "s)");
                    }
                    return;
                } else if (tickDelay < 30_000L) {
                    tickDelay = Math.min(tickDelay * 2, 30_000L);
                }
                // 熄屏降频（状态栏不可见，省电优先）。
                long delay = !isInteractive(ctx) ? Math.max(tickDelay, 10_000L) : tickDelay;
                if (BuildConfig.DEBUG) {
                    long t = SystemClock.uptimeMillis();
                    if (t - sLastKeepaliveLog > 30_000L) {
                        sLastKeepaliveLog = t;
                        XposedBridge.log(LOG_PREFIX + "color-keepalive: seconds off, delay="
                                + delay + "ms lastWrote=" + wrote);
                    }
                }
                view.postDelayed(this, delay);
            }
        }
    }

    // ==================================================================
    // 时钟识别
    // ==================================================================

    /** 判断该 TextView 是否为「状态栏左上角时钟」（目标控件）：内置规则命中，或命中用户自定义识别名。 */
    private static boolean isStatusBarClock(TextView view) {
        if (isStatusBarClockBuiltin(view)) {
            return true;
        }
        // 用户自定义识别名（其它厂商机型）：资源名包含该串即视为状态栏时钟，但仍排除锁屏 / AOD / 日期。
        if (sCustomRule.isEmpty()) {
            return false;
        }
        int id = view.getId();
        if (id == View.NO_ID || id == 0) {
            return false;
        }
        String name = resourceName(view);
        if (name.isEmpty()) {
            return false;
        }
        String className = view.getClass().getName().toLowerCase(Locale.ROOT);
        return !isClockNameExcluded(name, className) && name.contains(sCustomRule);
    }

    /**
     * 仅按内置规则判断（不依赖用户自定义识别名）。
     * 用于自动检测区分「内置已识别」（保持内置规则，避免多时钟命中导致触摸边界被下拉面板里的
     * QS 时钟覆盖）与「内置未命中、需自定义」（其它厂商机型）。
     */
    private static boolean isStatusBarClockBuiltin(TextView view) {
        int id = view.getId();
        if (id == View.NO_ID || id == 0) {
            return false;
        }
        String name = resourceName(view);
        if (name.isEmpty()) {
            return false;
        }
        String className = view.getClass().getName().toLowerCase(Locale.ROOT);
        if (isClockNameExcluded(name, className)) {
            return false;
        }
        // 状态栏时钟的资源名或类名特征。
        // 注意：不再匹配 header_clock —— 那是下拉通知面板/QS 面板的时钟（三星 QSClockHeaderView /
        // QSClockPanelView 等），本模块只作用于状态栏时钟。
        return name.equals("clock")
                || name.equals("status_bar_clock")
                || name.contains("status_clock")
                || name.contains("clock_text")
                || className.contains("statusbar.policy.clock")
                || className.contains("statclock");
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

    /**
     * 仅 DEBUG 构建的诊断探针：打印每一次「状态栏时钟 setText」的资源名/类名/文本/调用者链。
     *
     * <p>用途：当多个模块同时改状态栏时钟时，看 caller 就能知道「谁在我们上色之后又重设了文本」，
     * 从而定位颜色被覆盖的元凶。发布版（BuildConfig.DEBUG=false）不会打这些日志。</p>
     */
    private static void logProbeSetText(TextView view, Object text) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        try {
            String res = resourceName(view);
            String cls = view.getClass().getName();
            String t = text instanceof CharSequence ? text.toString() : String.valueOf(text);
            StackTraceElement[] st = new Throwable().getStackTrace();
            StringBuilder caller = new StringBuilder();
            for (int i = 2; i < st.length && i < 9; i++) {
                if (i > 2) {
                    caller.append(" <- ");
                }
                caller.append(st[i].getClassName()).append('#').append(st[i].getMethodName());
            }
            XposedBridge.log(LOG_PREFIX + "PROBE setText res=" + res + " cls=" + cls
                    + " text=[" + t + "] caller=" + caller);
        } catch (Throwable ignored) {
            // 探针本身绝不影响主流程
        }
    }

    /** 资源名/类名是否落在「状态栏时钟」排除名单（锁屏/AOD/日期/分隔符/下拉面板无关控件）。 */
    private static boolean isClockNameExcluded(String name, String className) {
        return name.contains("keyguard") || name.contains("lockscreen")
                || name.contains("aod") || name.contains("date")
                || name.contains("separator") || name.startsWith("no_carrier")
                || name.contains("shade_") || name.contains("quick_setting")
                || name.contains("header_clock")
                // 三星 One UI 下拉面板时钟全家：QSClockIndicatorView（res=clock！）/ QSClockHeaderView /
                // QSClockPanelView / QSClockPopOverImmersiveView，都不是状态栏时钟。
                // ⚠️ QSClockIndicatorView 的资源名就是 clock，仅靠 name.equals("clock") 会误中它，
                // 导致 sClockView 指向面板时钟（关闭时 0 尺寸）→ 长按命中区为空。
                || className.contains("qsclock")
                || className.contains("keyguard") || className.contains("aod");
    }

    /**
     * 自动检测：记录一个候选时钟视图。仅当文本当前显示「数字:数字」时间格式、
     * 且资源名/类名不在排除名单里才收录，供 {@link #detectClockName()} 扫描。
     */
    private static void recordCandidate(TextView view) {
        try {
            CharSequence txt = view.getText();
            String text = txt == null ? "" : txt.toString();
            if (!text.matches(".*\\d{1,2}[:：]\\d{2}.*")) {
                return;
            }
            String name = resourceName(view);
            String className = view.getClass().getName().toLowerCase(Locale.ROOT);
            if (isClockNameExcluded(name, className)) {
                return;
            }
            // 去重：若已收录同一视图实例则跳过（CopyOnWriteArrayList，O(n) 但 n 很小）。
            for (WeakReference<TextView> ref : sClockCandidates) {
                if (ref.get() == view) {
                    return;
                }
            }
            sClockCandidates.add(new WeakReference<>(view));
            // 登记到测试卡片清单（key 稳定、标签可读），并节流发布到 Secure 供设置页读取。
            sClockInfo.put(clockKey(view), clockLabel(view));
            if (view.getContext() != null) {
                publishClockList(view.getContext().getContentResolver());
            }
        } catch (Throwable ignored) {
            // 记录失败不影响系统
        }
    }

    /**
     * 自动检测状态栏时钟识别名：扫描候选池，挑出最像状态栏时钟的 TextView，
     * 返回其资源 entry 名（空串表示未检测到 / 命中内置规则无需自定义）。
     */
    private static String detectClockName() {
        TextView best = null;
        int bestScore = 0;
        boolean bestShowsClock = false;
        for (WeakReference<TextView> ref : sClockCandidates) {
            TextView v = ref.get();
            if (v == null) {
                continue;
            }
            String name = resourceName(v);
            if (name.isEmpty()) {
                continue;
            }
            String className = v.getClass().getName().toLowerCase(Locale.ROOT);
            if (isClockNameExcluded(name, className)) {
                continue;
            }
            int score = 0;
            boolean showsClock = false;
            CharSequence txt = v.getText();
            String text = txt == null ? "" : txt.toString();
            if (text.matches("^\\s*\\d{1,2}[:：]\\d{2}\\s*$")) {
                showsClock = true;
                score += 1;
            }
            if (name.equals("clock")) {
                score += 3;
            } else {
                if (name.contains("clock")) score += 3;
                if (name.contains("status")) score += 2;
                if (name.contains("time")) score += 1;
            }
            // 偏好当前正显示时间的候选；分数相同时优先 showsClock。
            if (score > bestScore || (score == bestScore && showsClock && !bestShowsClock)) {
                best = v;
                bestScore = score;
                bestShowsClock = showsClock;
            }
        }
        if (best == null || bestScore <= 0) {
            return "";
        }
        return resourceName(best);
    }

    /**
     * 返回首个被「内置规则」识别的候选时钟资源名（空串 = 内置规则未命中任何候选）。
     * 自动检测据此判断是否需要写入自定义识别名。
     */
    private static String findBuiltinClockName() {
        for (WeakReference<TextView> ref : sClockCandidates) {
            TextView v = ref.get();
            if (v != null && isStatusBarClockBuiltin(v)) {
                return resourceName(v);
            }
        }
        return "";
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

    /**
     * 屏幕是否处于交互（亮屏解锁）状态。
     *
     * <p>熄屏时状态栏不可见，每秒 setText + invalidate 纯属浪费；此时暂停刷新，
     * 亮屏后由 1 秒轮询立即恢复。取不到状态时按「亮屏」处理（宁可多刷新一次）。
     * 注：API 20 起 {@code isInteractive()} 公开可用，minSdk=29 无需版本判断。</p>
     */
    private static boolean isInteractive(Context context) {
        try {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return pm == null || pm.isInteractive();
        } catch (Throwable ignored) {
            return true;
        }
    }

    /**
     * 模块自保护：记录一次 hook 异常。若在同一时间窗口内异常次数超过阈值，
     * 自动停用本模块的全部 hook 逻辑（置 {@code sSelfDisabled}），避免把 SystemUI 反复拖崩、
     * 触发 LSPosed 安全模式。恢复只需重启 SystemUI。
     */
    private static void noteHookError(Throwable t) {
        if (sSelfDisabled) {
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (now - sErrorWindowStart > SELF_PROTECT_WINDOW_MS) {
            sErrorWindowStart = now;
            sErrorCount = 0;
        }
        sErrorCount++;
        if (sErrorCount >= SELF_PROTECT_MAX_ERRORS) {
            sSelfDisabled = true;
            if (BuildConfig.DEBUG) {
                XposedBridge.log(LOG_PREFIX + "SELF-DISABLED after " + sErrorCount
                        + " hook errors in last " + SELF_PROTECT_WINDOW_MS + "ms: " + t);
            } else {
                XposedBridge.log(LOG_PREFIX + "SELF-DISABLED after repeated hook errors");
            }
        } else if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "hook error #" + sErrorCount + ": " + t);
        }
    }

    // ==================================================================
    // 配置写入：设置页（普通应用进程）没有 WRITE_SECURE_SETTINGS，
    // 由 SystemUI 进程（系统应用，天然具备该权限）代写。
    // ==================================================================

    /**
     * 只注册一次：接收设置页发来的改色 / 改手势广播，并在 SystemUI 进程内写 Secure。
     *
     * <p>{@code UnspecifiedRegisterReceiverFlag}：API &lt; 33 没有 RECEIVER_EXPORTED 重载，
     * 该分支已按版本显式分流；且所有广播都以 signature 级 {@code CONFIG_PERMISSION} 作为
     * {@code broadcastPermission} 保护，非本模块签名的应用无法投递。</p>
     */
    @android.annotation.SuppressLint("UnspecifiedRegisterReceiverFlag")
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
                    // ColorOS 17：把显秒开关同步到系统原生键（非 ColorOS 17 时内部直接返回）。
                    mirrorNativeSeconds(cr0, ClockSecondsConfig.isEnabled(cr0));
                    // 测试卡片：启动时加载用户对候选时钟的启用选择，并发布一次当前清单。
                    sClockEnabled = loadClockEnabledMap(cr0);
                    publishClockList(cr0);
                    // 激活检测：写入心跳时间戳，供设置页兜底判定模块已注入 SystemUI。
                    ClockSecondsConfig.writeActiveHeartbeat(cr0,
                            BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
                } catch (Throwable ignored) {
                    // 写入失败不影响后续注册
                }
                IntentFilter filter = new IntentFilter();
                filter.addAction(ClockSecondsConfig.ACTION_SET_HOUR_COLOR);
                filter.addAction(ClockSecondsConfig.ACTION_SET_HOUR_COLOR_ENABLED);
                filter.addAction(ClockSecondsConfig.ACTION_SET_DIGIT_COLOR);
                filter.addAction(ClockSecondsConfig.ACTION_SET_GRADIENT);
                filter.addAction(ClockSecondsConfig.ACTION_SET_GRADIENT_COLOR);
                filter.addAction(ClockSecondsConfig.ACTION_SET_MARK);
        filter.addAction(ClockSecondsConfig.ACTION_SET_TASKS);
        filter.addAction(ClockSecondsConfig.ACTION_SET_TOAST_TEXT);
                filter.addAction(ClockSecondsConfig.ACTION_SET_GESTURE);
                filter.addAction(ClockSecondsConfig.ACTION_SET_ENABLED);
                filter.addAction(ClockSecondsConfig.ACTION_SET_CLOCK_RULE);
                filter.addAction(ClockSecondsConfig.ACTION_DETECT_CLOCK);
                filter.addAction(ClockSecondsConfig.ACTION_REFRESH_CLOCK_LIST);
                filter.addAction(ClockSecondsConfig.ACTION_SYNC_TIME);
                filter.addAction(ClockSecondsConfig.ACTION_SET_NET_OFFSET);
                filter.addAction(ClockSecondsConfig.ACTION_SET_CLOCK_TEST);
                filter.addAction(ClockSecondsConfig.ACTION_RESTART_SYSTEMUI);
                filter.addAction(ClockSecondsConfig.ACTION_RUN_DIAGNOSTIC);
                filter.addAction(ClockSecondsConfig.ACTION_PING_MODULE);
        // 系统时间/时区/日期被改：任务窗口与定时器都是按旧时间算的，必须整份重算。
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
                BroadcastReceiver receiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context ctx, Intent intent) {
                        try {
                            String action = intent.getAction();
                            ContentResolver cr = ctx.getContentResolver();
                            if (Intent.ACTION_TIME_CHANGED.equals(action)
                                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
                                    || Intent.ACTION_DATE_CHANGED.equals(action)) {
                                // 改系统时间后，已排的 Handler 定时器仍按旧时间点等待（postDelayed 走
                                // uptimeMillis），窗口状态也是旧时间算的 → 任务永远不会在正确时刻触发。
                                // 这里立刻重算窗口并按新时间重排下一次翻转（用户实测「提前 2 分钟开秒」不生效即此因）。
                                reloadTasks(ctx);
                                return;
                            }
                            if (ClockSecondsConfig.ACTION_SET_TOAST_TEXT.equals(action)) {
            // 只存文案，不涉及配置；设置页每次回到前台都会推一次，覆盖语言切换后的旧文案。
            sToastTaskText = intent.getStringExtra(ClockSecondsConfig.EXTRA_TOAST_TEXT);
            return;
        }
        if (ClockSecondsConfig.ACTION_SET_HOUR_COLOR.equals(action)) {
                                // 旧接口：等同「设置 0 号位（时十）颜色」，兼容老的设置页广播。
                                ClockSecondsConfig.setDigitColor(cr, 0, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_COLOR, HOUR_COLOR_FALLBACK));
                                reloadDigitColors(ctx);
                                sFloatHourColor = sDigitColors[0];
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_DIGIT_COLOR.equals(action)) {
                                // 新接口：任意一位数字取色（slot 见 ClockSecondsConfig.slotOfChar）。
                                int slot = intent.getIntExtra(ClockSecondsConfig.EXTRA_SLOT, 0);
                                ClockSecondsConfig.setDigitColor(cr, slot, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_COLOR,
                                        ClockSecondsConfig.DIGIT_COLOR_FOLLOW));
                                reloadDigitColors(ctx);
                                sFloatHourColor = sDigitColors[0];
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_GRADIENT.equals(action)) {
                                // 渐变总开关：只改配置，重读缓存后立刻刷新一次时钟。
                                ClockSecondsConfig.setGradientEnabled(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_ENABLED, 1) == 1);
                                reloadDigitColors(ctx);
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_TASKS.equals(action)) {
                                // 自定义任务整份覆盖写：设置页每次改动都发全量列表，这里直接落盘再重算窗口。
                                ClockSecondsConfig.setTasksJson(cr, intent.getStringExtra(
                                        ClockSecondsConfig.EXTRA_TASKS));
                                reloadTasks(ctx);
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_GRADIENT_COLOR.equals(action)) {
                                // 渐变端点色（0 = 起点，1 = 终点）：取色即生效。
                                boolean isStart = intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_SLOT, 0) == 0;
                                ClockSecondsConfig.setGradientEdgeColor(cr, isStart, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_COLOR,
                                        ClockSecondsConfig.GRADIENT_COLOR_AUTO));
                                reloadDigitColors(ctx);
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_MARK.equals(action)) {
                                // 时间后缀（周几 / 时段）：开关、模式、7 档文案一次广播全量覆盖。
                                ClockSecondsConfig.setMarkEnabled(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_ENABLED, 0) == 1);
                                ClockSecondsConfig.setMarkMode(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_MODE,
                                        ClockSecondsConfig.MARK_MODE_PERIOD));
                                ClockSecondsConfig.setMarkPeriods(cr, intent.getStringExtra(
                                        ClockSecondsConfig.EXTRA_MARK_PERIODS));
                                sMarkLoaded = false;   // 下次渲染按新的模式 / 文案重建后缀
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_HOUR_COLOR_ENABLED.equals(action)) {
                                ClockSecondsConfig.setHourColorEnabled(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_ENABLED, 1) == 1);
                                sFloatHourColorEnabled =
                                        ClockSecondsConfig.isHourColorEnabled(cr);
                                refreshClockNow();
                            } else if (ClockSecondsConfig.ACTION_SET_GESTURE.equals(action)) {
                                ClockSecondsConfig.setGesture(cr, intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_GESTURE,
                                        ClockSecondsConfig.GESTURE_CLICK));
                            } else if (ClockSecondsConfig.ACTION_SET_ENABLED.equals(action)) {
                                // 下拉磁贴 / 设置页：显式设置显秒开关（不再用「翻转」避免两端状态不一致）。
                                boolean enabled = intent.getIntExtra(
                                        ClockSecondsConfig.EXTRA_ENABLED,
                                        (ClockSecondsConfig.isEnabled(cr) ? 0 : 1)) == 1;
                                applyEnabledState(cr, enabled);
                            } else if (ClockSecondsConfig.ACTION_SET_CLOCK_RULE.equals(action)) {
                                // 自定义状态栏时钟识别名（其它厂商机型）。
                                String rule = intent.getStringExtra(ClockSecondsConfig.EXTRA_RULE);
                                sCustomRule = (rule == null ? "" : rule.trim());
                                ClockSecondsConfig.setClockRule(cr, sCustomRule);
                            } else if (ClockSecondsConfig.ACTION_DETECT_CLOCK.equals(action)) {
                                // 自动检测状态栏时钟识别名。
                                String builtinName = findBuiltinClockName();
                                String reported;
                                if (!builtinName.isEmpty()) {
                                    // 内置规则已识别到时钟：保持「内置规则」（清空自定义名）。
                                    // 关键：不要额外写入自定义名，否则下拉面板里的 QS 时钟也会同时命中，
                                    // 导致最后配置的视图覆盖 sClockRect（其位置在折叠时不可见），
                                    // 状态栏的点击/长按判定随之失效。
                                    sCustomRule = "";
                                    ClockSecondsConfig.setClockRule(cr, "");
                                    refreshClockNow();
                                    reported = builtinName;
                                } else {
                                    // 内置规则未命中（其它厂商机型）：把最佳候选的资源名写入自定义识别名。
                                    String best = detectClockName();
                                    sCustomRule = best; // 立即生效，无需重启
                                    ClockSecondsConfig.setClockRule(cr, best);
                                    // 让新规则命中的时钟立即接管（正常也会在下一次 setText 自动配置）。
                                    if (secondsOn()) {
                                        for (WeakReference<TextView> ref : sClockCandidates) {
                                            TextView v = ref.get();
                                            if (v != null && isStatusBarClock(v)
                                                    && isClockEnabled(v)
                                                    && !CONFIGURED.contains(v)) {
                                                configureClockView(v);
                                                // 同样要装秒表，否则只写一次秒、不继续跳动。
                                                installTicker(v);
                                                refreshClock(v);
                                            }
                                        }
                                    }
                                    refreshClockNow();
                                    reported = best;
                                }
                                // 回传结果给设置页。
                                try {
                                    Intent res = new Intent(
                                            ClockSecondsConfig.ACTION_DETECT_CLOCK_RESULT);
                                    res.setPackage(ClockSecondsConfig.MODULE_PACKAGE);
                                    res.putExtra(ClockSecondsConfig.EXTRA_DETECTED_RULE, reported);
                                    res.putExtra(ClockSecondsConfig.EXTRA_DETECTED_BUILTIN,
                                            !builtinName.isEmpty());
                                    ctx.sendBroadcast(res);
                                } catch (Throwable ignored) {
                                    // 回传失败不影响已写入的规则
                                }
                                if (BuildConfig.DEBUG) {
                                    XposedBridge.log(LOG_PREFIX + "DETECT builtin=" + builtinName
                                            + " reported=" + reported);
                                }
                            } else if (ClockSecondsConfig.ACTION_REFRESH_CLOCK_LIST.equals(action)) {
                                // 测试卡片「刷新列表」：强制重发候选清单（忽略节流），让设置页拿到最新候选。
                                publishClockList(cr, true);
                                try {
                                    Intent res = new Intent(
                                            ClockSecondsConfig.ACTION_REFRESH_CLOCK_LIST_RESULT);
                                    res.setPackage(ClockSecondsConfig.MODULE_PACKAGE);
                                    ctx.sendBroadcast(res);
                                } catch (Throwable ignored) {
                                    // 回传失败不影响已发布的清单
                                }
                                if (BuildConfig.DEBUG) {
                                    XposedBridge.log(LOG_PREFIX + "REFRESH_LIST published="
                                            + sClockInfo.size());
                                }
                            } else if (ClockSecondsConfig.ACTION_SYNC_TIME.equals(action)) {
                                // 应用无权直接改系统时钟（SET_TIME 是系统权限），
                                // 打开系统「自动设置时间」让系统自己向网络校准。
                                android.provider.Settings.Global.putInt(
                                        cr, android.provider.Settings.Global.AUTO_TIME, 1);
                            } else if (ClockSecondsConfig.ACTION_SET_NET_OFFSET.equals(action)) {
                                // 设置页测得的网络时间偏移（浮窗显示网络时间用）。
                                ClockSecondsConfig.setNetOffset(cr, intent.getLongExtra(
                                        ClockSecondsConfig.EXTRA_NET_OFFSET, 0L));
                            } else if (ClockSecondsConfig.ACTION_SET_CLOCK_TEST.equals(action)) {
                                // 测试卡片：切换某候选时钟的启用/停用。
                                // 优先采用设置页发来的「全量启用映射」extra（权威、避免单键丢失其它勾选）；
                                // 缺省时回退到「读 Secure + 单键 put」以保持旧路径兼容。
                                java.util.Map<String, Boolean> map;
                                String fullMap = intent.getStringExtra(
                                        ClockSecondsConfig.EXTRA_CLOCK_ENABLED_MAP);
                                if (fullMap != null && !fullMap.isEmpty()) {
                                    map = new java.util.HashMap<>();
                                    try {
                                        org.json.JSONObject o = new org.json.JSONObject(fullMap);
                                        java.util.Iterator<String> it = o.keys();
                                        while (it.hasNext()) {
                                            String k = it.next();
                                            map.put(k, o.getBoolean(k));
                                        }
                                    } catch (Throwable ignored) {
                                        map = null;
                                    }
                                } else {
                                    map = null;
                                }
                                if (map == null) {
                                    String ck = intent.getStringExtra(
                                            ClockSecondsConfig.EXTRA_CLOCK_KEY);
                                    boolean en = intent.getIntExtra(
                                            ClockSecondsConfig.EXTRA_CLOCK_ENABLED, 1) == 1;
                                    if (ck == null || ck.isEmpty()) {
                                        return;
                                    }
                                    map = loadClockEnabledMap(cr);
                                    map.put(ck, en);
                                }
                                if (map == null || map.isEmpty()) {
                                    return;
                                }
                                writeClockEnabledMap(cr, map);
                                applyClockTestToActive(cr);
                                if (BuildConfig.DEBUG) {
                                    XposedBridge.log(LOG_PREFIX + "CLOCK-TEST applied map=" + map);
                                }
                            } else if (ClockSecondsConfig.ACTION_RESTART_SYSTEMUI.equals(action)) {
                                // 设置页请求重启 SystemUI 以重新加载模块（免 adb）。
                                // 常规：killProcess + System.exit 结束自身，系统自动拉起 SystemUI。
                                // 兜底：部分厂商会拦截自杀，800ms 后若进程仍存活，用 root 强杀。
                                if (BuildConfig.DEBUG) {
                                    XposedBridge.log(LOG_PREFIX + "RESTART SYSTEMUI requested");
                                }
                                try {
                                    final android.os.Handler rh = new android.os.Handler(
                                            android.os.Looper.getMainLooper());
                                    rh.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            // 仍活着 → killProcess 被拦截，尝试 root 兜底。
                                            try {
                                                Runtime.getRuntime().exec(new String[]{
                                                        "su", "-c", "killall com.android.systemui"});
                                            } catch (Throwable ignored) {
                                                // 无 root 则放弃
                                            }
                                        }
                                    }, 800L);
                                } catch (Throwable ignored) {
                                    // 兜底安排失败不影响主路径
                                }
                                android.os.Process.killProcess(android.os.Process.myPid());
                                System.exit(0);
                            } else if (ClockSecondsConfig.ACTION_RUN_DIAGNOSTIC.equals(action)) {
                                // 设置页请求一次性全量诊断：候选时钟 / 命中区 / 上次触摸结论。
                                try {
                                    org.json.JSONObject diag = buildDiagnostic(cr);
                                    try {
                                        Settings.Secure.putString(cr,
                                                ClockSecondsConfig.CLOCK_DIAG_KEY, diag.toString());
                                    } catch (Throwable ignored) {
                                        // Secure 写入失败不影响回传广播
                                    }
                                    Intent res = new Intent(ClockSecondsConfig.ACTION_DIAG_RESULT);
                                    res.setPackage(ClockSecondsConfig.MODULE_PACKAGE);
                                    res.putExtra(ClockSecondsConfig.EXTRA_DIAG_JSON, diag.toString());
                                    ctx.sendBroadcast(res);
                                    if (BuildConfig.DEBUG) {
                                        XposedBridge.log(LOG_PREFIX + "DIAG: " + diag.toString());
                                    }
                                } catch (Throwable t) {
                                    XposedBridge.log(LOG_PREFIX + "DIAG failed: " + t);
                                }
                            } else if (ClockSecondsConfig.ACTION_PING_MODULE.equals(action)) {
                                // 设置页探测模块激活：刷新心跳并回传 PONG，证明模块已注入并正在运行。
                                try {
                                    ClockSecondsConfig.writeActiveHeartbeat(cr,
                                            BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
                                } catch (Throwable ignored2) {
                                    // 心跳写入失败不影响回传
                                }
                                try {
                                    Intent res = new Intent(ClockSecondsConfig.ACTION_PONG_MODULE);
                                    res.setPackage(ClockSecondsConfig.MODULE_PACKAGE);
                                    res.putExtra(ClockSecondsConfig.EXTRA_MODULE_VERSION,
                                            BuildConfig.VERSION_NAME);
                                    res.putExtra(ClockSecondsConfig.EXTRA_MODULE_VERSION_CODE,
                                            BuildConfig.VERSION_CODE);
                                    res.putExtra(ClockSecondsConfig.EXTRA_ACTIVE_TS,
                                            System.currentTimeMillis());
                                    res.putExtra(ClockSecondsConfig.EXTRA_SECONDS_ON,
                                            ClockSecondsConfig.isEnabled(cr) ? 1 : 0);
                                    ctx.sendBroadcast(res);
                                } catch (Throwable ignored2) {
                                    // 回传失败不影响模块运行
                                }
                            }
                        } catch (Throwable ignored) {
                            // 写入失败不影响系统界面
                        }
                    }
                };
                // 安全加固（F1）：以 signature 级自定义权限作为 broadcastPermission，
                // 要求发送方持有该权限（设置页/磁贴同签名天然持有，其它 App 被拒），
                // 防止任意应用广播代写 Secure/Global。
                if (Build.VERSION.SDK_INT >= 33) {
                    context.registerReceiver(receiver, filter,
                            ClockSecondsConfig.CONFIG_PERMISSION, null,
                            Context.RECEIVER_EXPORTED);
                } else {
                    context.registerReceiver(receiver, filter,
                            ClockSecondsConfig.CONFIG_PERMISSION, null);
                }
                if (BuildConfig.DEBUG) {
                    // 自检：本模块 App 是否真的持有该签名权限（应为 GRANTED）。
                    // 若为 DENIED，说明设置页/磁贴的广播会被系统丢弃 → 设置不生效。
                    int granted = android.content.pm.PackageManager.PERMISSION_DENIED;
                    try {
                        granted = context.getPackageManager().checkPermission(
                                ClockSecondsConfig.CONFIG_PERMISSION,
                                ClockSecondsConfig.MODULE_PACKAGE);
                    } catch (Throwable ignored) {
                        // 查询失败按未授予记录
                    }
                    XposedBridge.log(LOG_PREFIX + "config permission granted="
                            + (granted == android.content.pm.PackageManager.PERMISSION_GRANTED));
                }
                // 注册成功后才置位：万一首次注册抛异常，下次还有机会重试。
                sConfigReceiverRegistered = true;
                // 额外注册 Secure 监听：App 有 WRITE_SECURE_SETTINGS 时会直接写 Secure（不广播），
                // 只靠广播会让模块内存态漂移（表现为「改了显秒/开关，状态栏没反应」）。
                // 监听后无论谁改 Secure，都立即同步 sSecondsEnabled / sClockEnabled 并应用。
                if (sSecureObserver == null) {
                    try {
                        final ContentResolver ocr = context.getContentResolver();
                        sSecureObserver = new android.database.ContentObserver(
                                new android.os.Handler(android.os.Looper.getMainLooper())) {
                            @Override
                            public void onChange(boolean selfChange, android.net.Uri uri) {
                                try {
                                    String key = uri != null ? uri.getLastPathSegment() : null;
                                    if (ClockSecondsConfig.MODULE_SECONDS_KEY.equals(key)) {
                                        boolean en = ClockSecondsConfig.isEnabled(ocr);
                                        sSecondsEnabled = en;
                                        TextView clock = sClockView;
                                        if (clock != null && isClockEnabled(clock)) {
                                            if (secondsOn()) {
                                                writeTime(clock);
                                                installTicker(clock);
                                            } else {
                                                installTicker(clock);
                                                writeMinuteOnly(clock);
                                            }
                                        }
                                    } else if (ClockSecondsConfig.CLOCK_TEST_ENABLED_KEY.equals(key)) {
                                        applyClockTestToActive(ocr);
                                    } else if (ClockSecondsConfig.TASKS_KEY.equals(key)) {
                                        // 任务列表被外部（adb / 设置页直写）改动：重算窗口。
                                        reloadTasks(context);
                                    }
                                } catch (Throwable ignored2) {
                                    // 同步失败不影响系统
                                }
                            }
                        };
                        ocr.registerContentObserver(android.provider.Settings.Secure.getUriFor(
                                ClockSecondsConfig.MODULE_SECONDS_KEY), false, sSecureObserver);
                        ocr.registerContentObserver(android.provider.Settings.Secure.getUriFor(
                                ClockSecondsConfig.CLOCK_TEST_ENABLED_KEY), false, sSecureObserver);
                        ocr.registerContentObserver(android.provider.Settings.Secure.getUriFor(
                                ClockSecondsConfig.TASKS_KEY), false, sSecureObserver);
                    } catch (Throwable ignored2) {
                        // 监听注册失败不影响广播路径
                    }
                }
            } catch (Throwable ignored) {
                // 注册失败只影响"未用 adb 授权时的设置写入"，不影响显秒与点击切换
            }
        }
    }

    /**
     * 由下拉磁贴 / 远程广播设置显秒开关：写入 Secure、更新内存态、立即重绘。
     * 显式设值（而非翻转），避免磁贴与设置页两端状态不一致。
     */
    private static void applyEnabledState(ContentResolver cr, boolean enabled) {
        ClockSecondsConfig.setEnabled(cr, enabled);
        sSecondsEnabled = enabled;
        mirrorNativeSeconds(cr, enabled);
        if (BuildConfig.DEBUG) {
            XposedBridge.log(LOG_PREFIX + "seconds set -> " + enabled + " (tile/remote)");
        }
        TextView clock = sClockView;
        // 已停用的时钟：只更新内存态，不写文本/颜色（尊重测试卡片开关）。
        if (clock != null && isClockEnabled(clock)) {
            if (enabled) {
                writeTime(clock);
                installTicker(clock);
            } else {
                // 秒数关：不停 ticker——双模式自动切到 2 秒低频补色
                // （ColorOS 17 实测：分钟更新路径上 Span 被同步冲掉，无循环救援则掉色）。
                installTicker(clock);
                writeMinuteOnly(clock);
            }
        } else {
            // 时钟尚未识别：仅更新内存态，下次配置/绘制时自然生效。
            if (!enabled) {
                // 关闭时无需续订，已在内存态标记。
            }
        }
        publishEffectiveSeconds(cr);
        refreshClockNow();
    }

    /**
     * 立即重绘缓存的时钟：秒关闭时系统每分钟才更新一次，改颜色/开关后不主动刷新
     * 最长要等一分钟才能看到变化。
     */
    private static void refreshClockNow() {
        TextView clock = sClockView;
        // 该时钟已被用户停用：不写文本/颜色（否则改小时颜色等操作会绕过开关重新上色）。
        if (clock == null || !isClockEnabled(clock)) {
            return;
        }
        try {
            // 配置变了要重写一次：幂等短路会保留旧 span（旧端点色 / 旧渐变开关）。
            sForceRewrite = true;
            // 对所有存活实例一起重写（HyperOS 双实例，只写一个会留下旧颜色/旧文本）。
            writeSecondsStateNow();
        } catch (Throwable ignored) {
            // 重绘失败不影响系统时钟自身的刷新
        }
    }
}
