package io.github.kelebao.colorosclockseconds.settings;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.Spanned;
import android.text.TextUtils;
import io.github.kelebao.colorosclockseconds.ArcTickDrawable;
import io.github.kelebao.colorosclockseconds.BreathSwatchDrawable;
import io.github.kelebao.colorosclockseconds.CloseCrossView;
import io.github.kelebao.colorosclockseconds.CornerArcsDrawable;
import io.github.kelebao.colorosclockseconds.HsvPickerView;
import io.github.kelebao.colorosclockseconds.DigitGlowDrawable;
import io.github.kelebao.colorosclockseconds.SlantCardDrawable;
import io.github.kelebao.colorosclockseconds.UnderlineDrawable;
import io.github.kelebao.colorosclockseconds.WavyLineView;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import io.github.kelebao.colorosclockseconds.BuildConfig;
import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;
import io.github.kelebao.colorosclockseconds.R;

/**
 * 模块设置页（从 LSPosed 管理器打开，桌面不生成图标）。
 *
 * <p>视觉风格：柔紫渐变底 + 大圆角白卡 + 胶囊按钮，全部用 framework 原生控件绘制
 * （{@link GradientDrawable} 做渐变与圆角，{@code elevation} 做投影），不引入 AndroidX。</p>
 *
 * <p>说明：普通应用进程写入 {@link Settings.Secure} 需要 {@code WRITE_SECURE_SETTINGS}；
 * 没有该权限时改走广播，由 SystemUI 进程内的模块代写。</p>
 */
public final class ModuleSettingsActivity extends Activity {

    /** 模块仓库 Releases 页（LSPosed 模块仓库，tag = versionCode-versionName）。 */
    private static final String RELEASES_URL =
            "https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds/releases";
    /** 源码仓库主页。 */
    private static final String REPO_URL = "https://github.com/kelebao/ClockSeconds";

    private static final String PREFS_NAME = "clock_seconds_settings";
    private static final String KEY_AUTO_UPDATE = "auto_update";
    private static final String PREF_SEEN_INTRO = "seen_intro";

    /** 小时十位数字可选颜色（第一项为默认红）。 */
    private static final int[] HOUR_COLORS = {
            0xFFF44336, 0xFFFF9800, 0xFFFFC107, 0xFF4CAF50, 0xFF00BCD4,
            0xFF2196F3, 0xFF9C27B0, 0xFFE91E63, 0xFFFFFFFF, 0xFF9E9E9E,
    };

    // 浅色配色
    private static final int LIGHT_BG_TOP = 0xFFF3EFFE;
    private static final int LIGHT_BG_BOTTOM = 0xFFE9E2FF;
    private static final int LIGHT_CARD = 0xFFFFFFFF;
    private static final int LIGHT_TEXT = 0xFF1F1B2E;
    private static final int LIGHT_TEXT_2 = 0xFF6B6480;
    private static final int LIGHT_TEXT_3 = 0xFFA29BB8;
    private static final int LIGHT_DIVIDER = 0xFFEFEBFA;

    // 深色配色
    private static final int DARK_BG_TOP = 0xFF1A1526;
    private static final int DARK_BG_BOTTOM = 0xFF120F1C;
    private static final int DARK_CARD = 0xFF241D38;
    private static final int DARK_TEXT = 0xFFF2EEFF;
    private static final int DARK_TEXT_2 = 0xFFB9B0D0;
    private static final int DARK_TEXT_3 = 0xFF7D749A;
    private static final int DARK_DIVIDER = 0xFF322A4A;

    private static final int ACCENT_LIGHT = 0xFF6C4CF1;
    private static final int ACCENT_DARK = 0xFFA98CFF;

    private boolean night;
    private TextView chipView;
    private TextView previewView;
    /** 预览时间所在的背景板容器。 */
    private FrameLayout previewPlate;
    /** 当前选中色块的「荧光呼吸」光晕 drawable（高频刷新）。 */
    private BreathSwatchDrawable mBreathSwatch;
    /** 预览卡顶部半弧刻度盘 drawable（高频刷新旋转）。 */
    private ArcTickDrawable mArcTicks;
    private final PressTiltEffect mPressTilt = new PressTiltEffect();   // 全卡片按压倾斜效果
    private View mPreviewCard;                    // 预览卡：判断是否滚出屏幕，跳过弧的高频刷新
    private final Rect mTmpVisible = new Rect();
    // 时间格式化缓存（预览每秒刷新；SimpleDateFormat 仅主线程用，可安全缓存）
    private final java.text.SimpleDateFormat mFmtSec =
            new java.text.SimpleDateFormat("HH:mm:ss", Locale.US);
    private final java.text.SimpleDateFormat mFmtNoSec =
            new java.text.SimpleDateFormat("HH:mm", Locale.US);
    private final java.util.Date mNowDate = new java.util.Date();
    // 配置缓存：避免预览每秒 3 次 Settings.Secure binder 读取；变化由 ContentObserver 推送刷新。
    private boolean mCfgSeconds;
    private boolean mCfgHourColorOn;
    private int mCfgHourColor;
    // 六位数字各自的颜色（-1 = 跟随系统）；由 ContentObserver / 取色后重新读取刷新。
    private int[] mCfgDigitColors = new int[ClockSecondsConfig.DIGIT_COUNT];
    /** 预览卡是否处于「逐位取色」展开态。 */
    private boolean mDigitsExpanded;
    private int mFitGuard;
    /** 展开排版的宽度安全系数：1.0 → 逐渐收紧，收到底还换行才降字号。 */
    private float mFitSafety = 0.97f;
    /** 安全系数收到底仍换行时，再按这个比例压字号。 */
    private float mSizeShrink = 1f;
    /** 正在编辑的数字位（0..5 = 时十/时个/分十/分个/秒十/秒个）。 */
    private int mSelSlot;
    /** 选中数字下面那根短色线（普通 View + UnderlineDrawable）。 */
    private View mMarkView;
    private UnderlineDrawable mMarkDrawable;
    /** 选中那位数字自己的呼吸光晕（字形模糊两层，画在数字底下）。 */
    private View mGlowView;
    private DigitGlowDrawable mGlowDrawable;
    private android.animation.ValueAnimator mGlowAnim;
    /** 探针：呼吸动画的帧计数（仅 DEBUG 日志用）。 */
    private long mGlowProbe;
    /** 预览卡下方「切换手势 / 小时颜色」两张统计卡（展开取色时让位隐藏）。 */
    private View mStatsRow;
    /** 数字颜色卡内的部件：色块网格、正在编辑行、摘要行、开关、完成胶囊。 */
    private GridLayout mDigitGrid;
    private TextView mDigitEditingView;
    private TextView mDigitSummaryView;
    private View mDigitDoneView;
    /** 「网络时间」那一行。展开取色时临时藏起来，把位置让给 X 圆钮。 */
    private LinearLayout mNetRow;
    private Switch mDigitSwitch;
    private boolean mObserverOk = false;
    private final android.database.ContentObserver mCfgObserver =
            new android.database.ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    final boolean wasSeconds = mCfgSeconds;
                    final int wasLen = previewView == null || previewView.getText() == null
                            ? 0 : previewView.getText().length();
                    refreshConfigCache();
                    updatePreview();
                    // 显秒开关被「外部」改掉（用户在状态栏点时间、切磁贴、别的进程代写 secure 表）时，
                    // 预览文本长度会在 {5 位「02:08」, 8 位「02:08:07」} 之间跳变。展开态的
                    // 字号 / 字距 / 下划线 / 光晕全是按「当时那一串字符的宽度」反解出来的
                    // （见 computeExpandedMetrics / measurePerUnitWidth），字符数一变，
                    // 旧指标就全部失效 —— 真机反馈：「没显秒时设置颜色，再手动点状态栏开显秒，
                    // 预览卡里的数字就错乱」。这里发现长度变了就整套重排。
                    // 本页自己写配置时（applyConfig 回调）调用前已手动同步过 mCfgSeconds，
                    // wasSeconds 与刷新后的值相同，不会误触发。
                    final int nowLen = previewView == null || previewView.getText() == null
                            ? 0 : previewView.getText().length();
                    if (mDigitsExpanded && (wasSeconds != mCfgSeconds || wasLen != nowLen)) {
                        relayoutExpanded();
                    }
                    // 展开态下字号/字距一变，整行可能重新变成两行（虽然设了 singleLine，
                    // 但 Layout 仍会按宽度换行再被裁掉）。此时画面是「数字错位」，必须收紧重排。
                    // 注意：这里不能用 this::fitExpandedText —— 方法引用在匿名内部类里解析的是
                    // ContentObserver 的 this，编译期直接报「找不到符号」（vc143 踩过）。
                    if (mDigitsExpanded) {
                        final ModuleSettingsActivity self = ModuleSettingsActivity.this;
                        previewView.post(() -> self.fitExpandedText());
                    }
                }
            };
    private SoundPool mPressPool;
    private int mPressSoundId = 0;
    private boolean mPressSoundReady = false;
    /** 小时颜色卡右下角同心弧（关闭小时颜色时隐藏，避免透出盖住半透明色块）。 */
    private CornerArcsDrawable mCornerArcs;
    /** 虚线分隔线 / 右下角同心弧的固定颜色（浅薰衣草紫 #D4C5FF）。 */
    private static final int WAVE_COLOR = 0xFFD4C5FF;
    private TextView netTimeView;
    private TextView previewHintView;
    private TextView syncView;
    private TextView colorValueView;
    private View colorDotView;
    private LinearLayout mClockTestList;          // 测试卡片：候选时钟列表容器
    private final Map<String, Boolean> mClockEnabled = new HashMap<>();  // 测试卡片：key -> 是否启用
    private TextView mDiagView;                   // 诊断卡片：结果展示
    /** 测试卡片「刷新列表」按钮（保持引用以便刷新时改文字：刷新中/刷新完成）。 */
    private TextView mRefreshBtn;
    /** 刷新请求是否在途（结果广播与兜底定时器二者只收尾一次）。 */
    private boolean mRefreshPending;
    /** 接收 SystemUI 回传的「候选清单已重发」事件（跨 UID，同签名广播）。 */
    private final BroadcastReceiver mRefreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ClockSecondsConfig.ACTION_REFRESH_CLOCK_LIST_RESULT.equals(intent.getAction())) {
                finishClockRefresh();
            }
        }
    };
    /** 诊断是否在途（结果广播或超时兜底，只收尾一次）。 */
    private boolean mDiagPending;
    /** 诊断超时兜底：模块未就绪时避免一直停在「诊断中…」。 */
    private final Runnable mDiagTimeout = () -> {
        if (mDiagPending && mDiagView != null) {
            mDiagPending = false;
            mDiagView.setText("⚠ 诊断超时：模块没有响应。\n"
                    + "常见原因：① SystemUI 正在重启，稍后重试；"
                    + "② LSPosed 因 SystemUI 反复崩溃进入「安全模式」（会停用所有模块）"
                    + "——此时请重启手机恢复，并到 LSPosed 确认本模块已启用。\n"
                    + "提示：不要频繁点「重启 SystemUI」，连续强杀可能触发安全模式。");
        }
    };
    /** 接收 SystemUI 回传的一次性诊断结果（同签名广播，仅本应用能收）。 */
    private final BroadcastReceiver mDiagReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ClockSecondsConfig.ACTION_DIAG_RESULT.equals(intent.getAction())) {
                mDiagPending = false;
                mPingHandler.removeCallbacks(mDiagTimeout);
                String json = intent.getStringExtra(ClockSecondsConfig.EXTRA_DIAG_JSON);
                if (json != null) {
                    renderDiagnostic(json);
                }
            }
        }
    };
    /** 当前识别规则摘要（"内置规则" 或 "自定义：clock"）。 */
    private TextView ruleSummaryView;
    /** 自动检测开关：避免检测广播未到达时重复点击。 */
    private final java.util.concurrent.atomic.AtomicBoolean mDetecting =
            new java.util.concurrent.atomic.AtomicBoolean(false);
    /** 接收 SystemUI 回传的自动检测结果。 */
    private android.content.BroadcastReceiver mDetectReceiver;

    /** 接收 SystemUI 回传的 PING 应答（证明模块已激活并正在运行）。 */
    private final android.content.BroadcastReceiver mPingReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ClockSecondsConfig.ACTION_PONG_MODULE.equals(intent.getAction())) {
                String ver = intent.getStringExtra(ClockSecondsConfig.EXTRA_MODULE_VERSION);
                int on = intent.getIntExtra(ClockSecondsConfig.EXTRA_SECONDS_ON, -1);
                onModulePong(ver, on == 1);
            }
        }
    };
    /** 激活状态：0=检测中, 1=已激活, 2=未激活。 */
    private static final int ACTIVE_CHECKING = 0;
    private static final int ACTIVE_YES = 1;
    private static final int ACTIVE_NO = 2;
    private View mActiveDot;
    private TextView mActiveText;
    /** 「模块已激活」卡片：检测到已激活就整卡隐藏，只在未激活时才提示。 */
    private View mActivationCard;
    /** 卡片列根容器：展开/收起时对它做 ChangeBounds 过渡，下面的卡片不跳。 */
    private LinearLayout mColumn;
    private final Handler mPingHandler = new Handler(Looper.getMainLooper());
    private final Runnable mPingTimeout = () -> setActiveState(ACTIVE_NO, null, false);

    /** 网络时间相对本地的偏移（毫秒，网络 - 本地）；Long.MIN_VALUE 表示还没取到。 */
    private long netDriftMs = Long.MIN_VALUE;

    /**
     * 写入操作序列号：快速连续切换（如连点色板）时，旧操作的校验会被新值覆盖而误报失败。
     * 只有序列号仍是最新的操作才允许回调结果，被取代的旧操作静默丢弃。
     */
    private final java.util.concurrent.atomic.AtomicInteger mWriteSeq =
            new java.util.concurrent.atomic.AtomicInteger(0);

    private final Handler ticker = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updatePreview();
            ticker.postDelayed(this, 1000L);
        }
    };
    /** 选中色块荧光光晕 + 半弧刻度盘的高频刷新（~50ms）。 */
    private final Runnable breathTick = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (mBreathSwatch != null) {
                // 关闭小时颜色时，色块光晕呼吸也停止（强度归零、光晕不绘制）。
                mBreathSwatch.setBreath(ClockSecondsConfig.isHourColorEnabled(getContentResolver())
                        ? ClockSecondsConfig.breathIntensity(now) : 0f);
            }
            // 预览卡滚出屏幕时跳过弧的高频刷新（省电/省 CPU）；色块光晕照常（它可能在视口内）。
            if (mArcTicks != null
                    && (mPreviewCard == null || mPreviewCard.getGlobalVisibleRect(mTmpVisible))) {
                mArcTicks.setRotation((now % 35_000L) / 35_000f * 360f);
                // 呼吸灯：6s 余弦呼吸（独立于色块 3s）+ 30s 色板流转/发光中心漂移
                double bp = (now % 6_000L) / 6_000.0;
                mArcTicks.setBreath((float) ((1 - Math.cos(bp * 2 * Math.PI)) / 2));
                mArcTicks.setDrift((now % 30_000L) / 30_000f);
            }
            ticker.postDelayed(this, 50L);
        }
    };

    /** 点击预览时钟后显示提示，3 秒后自动收起。 */
    private final Runnable hideHint = () -> {
        if (previewHintView != null) {
            previewHintView.setVisibility(View.INVISIBLE);
        }
    };

    private void showPreviewHint() {
        if (previewHintView == null) {
            return;
        }
        ticker.removeCallbacks(hideHint);
        previewHintView.setVisibility(View.VISIBLE);
        ticker.postDelayed(hideHint, 3000L);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        night = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
        // 页面自带大标题，隐藏系统 ActionBar 避免标题重复。
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setTitle(R.string.settings_title);
        setContentView(buildContentView());
        initPressSound();
        mPressTilt.setOnPressDownListener(this::playPressSound);
        applyStatusBarAppearance();
        reconcileLauncherIcon();
        refreshStatus();
        maybeShowIntro();
        maybeAutoCheckUpdate();
    }

    /**
     * 回到前台：刷新状态并（重新）注册 SystemUI 回传用的三个接收器。
     *
     * <p>{@code UnspecifiedRegisterReceiverFlag}：API &lt; 33 没有带 flags 的
     * registerReceiver 重载，下面三个接收器均已按版本显式分流（&gt;=33 走
     * RECEIVER_EXPORTED，&lt;33 走旧 2 参重载、旧系统默认导出），属预期写法。</p>
     */
    @android.annotation.SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        refreshConfigCache();
        registerConfigObserver();
        ticker.removeCallbacks(tick);
        ticker.post(tick);
        ticker.removeCallbacks(breathTick);
        ticker.post(breathTick);
        // 返回前台时重新读取 SystemUI 检测到的候选时钟清单（此时 SystemUI 多半已发布最新列表）。
        if (mClockTestList != null) {
            refreshClockTestList();
        }
        // 诊断结果回传广播：由 SystemUI（不同 UID，且未被我们的签名证书签名）发出。
        // 必须用 RECEIVER_EXPORTED 才能跨 UID 送达（RECEIVER_NOT_EXPORTED 在 API33+ 会丢弃其它 App 的广播）；
        // 但不能要求 CONFIG_PERMISSION——SystemUI 是 OEM 签名，并不持有我们模块的 signature 权限，
        // 要求该权限反而会把 PONG/诊断回传全部丢弃。广播已用 setPackage(本应用) 定向，第三方无法截获，无需权限。
        // 前台注册，离开注销。
        try {
            IntentFilter f = new IntentFilter(ClockSecondsConfig.ACTION_DIAG_RESULT);
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                registerReceiver(mDiagReceiver, f, null,
                        null, Context.RECEIVER_EXPORTED);
            } else {
                // API<33 无 RECEIVER_EXPORTED/权限 overload（已被移除），且旧系统 receiver 默认导出，
                // SystemUI 的定向广播可正常送达。
                registerReceiver(mDiagReceiver, f);
            }
        } catch (Throwable ignored) {
            // 注册失败不影响其它功能
        }
        // 刷新列表回传接收器：SystemUI（不同 UID）发出，同理用 EXPORTED + null 权限（见上）。
        try {
            IntentFilter rf = new IntentFilter(ClockSecondsConfig.ACTION_REFRESH_CLOCK_LIST_RESULT);
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                registerReceiver(mRefreshReceiver, rf, null,
                        null, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(mRefreshReceiver, rf);
            }
        } catch (Throwable ignored) {
            // 注册失败不影响其它功能
        }
        // 激活检测：注册 PONG 接收器（同上，跨 UID 用 EXPORTED，不要求签名权限）并触发一次探测。
        try {
            IntentFilter pf = new IntentFilter(ClockSecondsConfig.ACTION_PONG_MODULE);
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                registerReceiver(mPingReceiver, pf, null,
                        null, Context.RECEIVER_EXPORTED);
            } else {
                // API<33 无 RECEIVER_EXPORTED/权限 overload；旧系统 receiver 默认导出，定向广播可送达。
                registerReceiver(mPingReceiver, pf);
            }
        } catch (Throwable ignored) {
            // 注册失败不影响其它功能
        }
        pingModule();
    }

    /** 读取一次模块配置进缓存（供预览每秒刷新时免 binder 直读）。 */
    private void refreshConfigCache() {
        mCfgSeconds = ClockSecondsConfig.isEnabled(getContentResolver());
        mCfgHourColorOn = ClockSecondsConfig.isHourColorEnabled(getContentResolver());
        mCfgHourColor = ClockSecondsConfig.getHourColor(getContentResolver());
        mCfgDigitColors = ClockSecondsConfig.getDigitColors(getContentResolver());
        if (mDigitGrid != null) {
            // 回到前台 / 状态栏改了颜色：同步色块选中环与摘要行。
            refreshDigitSwatches();
        }
    }

    /** 观察 Settings.Secure 变化（含状态栏点击/磁贴/其它进程代写），变化即刷新缓存并更新预览。 */
    private void registerConfigObserver() {
        if (mObserverOk) {
            return;
        }
        try {
            getContentResolver().registerContentObserver(
                    Settings.Secure.CONTENT_URI, true, mCfgObserver);
            mObserverOk = true;
        } catch (Exception ignored) {
            // 个别 ROM 可能限制；保持 mObserverOk=false，updatePreview 会退回每秒直读。
        }
    }

    private void unregisterConfigObserver() {
        if (!mObserverOk) {
            return;
        }
        mObserverOk = false;
        try {
            getContentResolver().unregisterContentObserver(mCfgObserver);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ticker.removeCallbacks(tick);
        ticker.removeCallbacks(breathTick);
        ticker.removeCallbacks(hideHint);
        unregisterConfigObserver();
        unregisterDetectReceiver();
        try {
            unregisterReceiver(mDiagReceiver);
        } catch (Throwable ignored) {
            // 未注册或已注销，忽略
        }
        try {
            unregisterReceiver(mRefreshReceiver);
        } catch (Throwable ignored) {
            // 未注册或已注销，忽略
        }
        mPingHandler.removeCallbacks(mPingTimeout);
        try {
            unregisterReceiver(mPingReceiver);
        } catch (Throwable ignored) {
            // 未注册或已注销，忽略
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // 全卡片按压倾斜：统一在此驱动（dispatchTouchEvent 是触摸分发最上游，
        // 无论落在哪张卡的哪个子 View、是否被消费，UP/CANCEL 必然能收到 → 不会「压下去弹不回」）。
        mPressTilt.onTouch(ev);
        return super.dispatchTouchEvent(ev);   // 继续正常分发 → 按钮 onClick、页面滚动不受影响
    }

    private void applyStatusBarAppearance() {
        View decor = getWindow().getDecorView();
        int flags = decor.getSystemUiVisibility();
        if (night) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    // ==================================================================
    // 页面骨架
    // ==================================================================

    /** 展开态的目标字距（em）：先按这个间隙解字号，剩余宽度再全部补给间隙（铺满整卡）。 */
    private static final float EXPAND_GAP_EM = 0.22f;
    /** 预览卡底板（可斜切下沿）。 */
    private SlantCardDrawable mSlantCard;
    /** 展开态预览区上内边距（dp）：数字整体下移，腾出空间让水滴从上沿外落进来。 */

    /** 展开态实测出的字号（px）与字距（em），由 computeExpandedMetrics() 计算。 */
    private float mExpandedSizePx;
    private float mExpandedSpacingEm = EXPAND_GAP_EM;
    /** 布局变化平滑时长。 */
    private static final long LAYOUT_ANIM_MS = 240L;

    /**
     * 展开/收起时让卡片位置平滑过渡：ChangeBounds 只负责「谁去哪儿」，
     * 预览卡自身的尺寸由字号动画逐帧撑开，所以把它排除，避免两套动画打架。
     */
    private void beginSmoothLayout() {
        if (mColumn == null) {
            return;
        }
        android.transition.ChangeBounds cb = new android.transition.ChangeBounds();
        cb.setDuration(LAYOUT_ANIM_MS);
        cb.setInterpolator(new android.view.animation.DecelerateInterpolator());
        if (mPreviewCard != null) {
            cb.excludeTarget(mPreviewCard, true);
        }
        android.transition.TransitionManager.beginDelayedTransition(mColumn, cb);
    }

    private ScrollView buildContentView() {
        ScrollView scroll = new ScrollView(this);
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{night ? DARK_BG_TOP : LIGHT_BG_TOP,
                        night ? DARK_BG_BOTTOM : LIGHT_BG_BOTTOM});
        scroll.setBackground(bg);
        scroll.setFillViewport(true);
        // targetSdk 35 起系统强制边到边：内容会画到状态栏/导航栏下面，这里按系统栏高度留白，
        // 并保留裁剪（clipToPadding 默认 true），滚动时内容不会钻到状态栏图标下面。
        scroll.setOnApplyWindowInsetsListener((v, insets) -> {
            int top;
            int bottom;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(
                        android.view.WindowInsets.Type.systemBars());
                top = bars.top;
                bottom = bars.bottom;
            } else {
                top = insets.getSystemWindowInsetTop();
                bottom = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(0, top, 0, bottom);
            return insets;
        });

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        mColumn = root;
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        buildHeader(root);
        buildActivationCard(root);
        buildPreviewCard(root);
        buildStatsRow(root);
        buildColorCard(root);
        // 时钟探测（测试）与诊断与工具两张卡片仅用于排查问题，留 debug/probe 版；发布版不需要。
        if (BuildConfig.DEBUG) {
            buildClockTestCard(root);
            buildDiagnosticCard(root);
        }
        buildAdvancedCard(root);
        buildLauncherCard(root);
        buildUpdateCard(root);
        buildAboutCard(root);
        buildPrimaryButton(root);
        buildFooter(root);
        return scroll;
    }

    private void buildHeader(LinearLayout root) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), 0, dp(4), dp(16));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView title = new TextView(this);
        title.setText(R.string.settings_title);
        title.setTextSize(24f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        texts.addView(title);
        row.addView(texts);

        chipView = new TextView(this);
        chipView.setTextSize(12f);
        chipView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        chipView.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable chipBg = new GradientDrawable();
        chipBg.setCornerRadius(dp(999));
        chipBg.setColor(night ? 0xFF3A2F5C : 0xFFE7DFFF);
        chipView.setBackground(chipBg);
        chipView.setTextColor(accent());
        row.addView(chipView);
        root.addView(row);
    }

    private void buildPreviewCard(LinearLayout root) {
        LinearLayout card = card();
        mPreviewCard = card;
        // 卡片这一层也必须关掉裁剪：光晕 View 用负 margin 伸出 previewPlate，
        // 一旦这一层还按 padding 裁剪，光晕就会被切出一条垂直硬边
        // （真机实测硬边正好落在 padding 左沿 x=126，vc131~vc137 反复复现）。
        card.setClipChildren(false);
        card.setClipToPadding(false);
        // 顶部留白收小；底部留小缝，让网络时间行贴近卡片最下方。
        card.setPadding(dp(18), dp(2), dp(18), dp(6));
        // 底部叠「长弧刻度」（只有刻度、无弧线），把网络时间/提示两行圈在弧中间。
        ArcTickDrawable arc = new ArcTickDrawable(night);
        mArcTicks = arc;
        // 底板用可斜切的版本：取色时下沿两个角被削出 20 度斜面。
        mSlantCard = new SlantCardDrawable(night ? DARK_CARD : LIGHT_CARD, dp(24));
        mSlantCutPx = 0f;
        // card() 已 mPressTilt.register（卡片背景不变、另叠了按压高光前景层）；这里只把刻度弧叠上去。
        card.setBackground(new LayerDrawable(new Drawable[]{mSlantCard, arc}));

        // 提示（在预览时间上方；点击预览后才出现，3 秒后自动收起）。
        previewHintView = new TextView(this);
        previewHintView.setText(R.string.settings_preview_hint);
        previewHintView.setTextSize(10f);
        previewHintView.setTextColor(text3());
        previewHintView.setGravity(Gravity.CENTER_HORIZONTAL);
        previewHintView.setPadding(0, 0, 0, dp(1));
        // 常驻占位（INVISIBLE）：显示提示时不再撑高卡片。
        previewHintView.setVisibility(View.INVISIBLE);
        card.addView(previewHintView);

        // 预览时间容器（仅承载文字）。
        previewPlate = new FrameLayout(this);
        // 左右几乎不留边：展开时整行要铺到卡片边缘，数字才能「向左右顶开 + 变大」。
        previewPlate.setPadding(dp(2), dp(2), dp(2), dp(14));

        // 选中那位的呼吸光晕：先加 = 画在文字下层；默认隐藏，展开取色时才点亮。
        // previewPlate 关掉裁剪，光晕才能糊出字形边界（否则被自己/父容器切成方块）。
        previewPlate.setClipChildren(false);
        // 关键：ViewGroup 默认 clipToPadding=true，即使关了 clipChildren，
        // 子 View 的绘制仍会被裁到 padding 内 —— 光晕就会被切出一个带直角的方框
        // （vc131~vc136 反复复现的根因，左边界正好等于 plate 的 padding 左沿）。
        previewPlate.setClipToPadding(false);
        mGlowView = new View(this);
        mGlowView.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
        mGlowView.setVisibility(View.GONE);
        mGlowView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        // 光晕走 BlurMaskFilter + drawText（vc103 的形态）：硬件加速下 drawText 上的
        // MaskFilter 是被完整支持的，直接画即可，开硬件层反而多一次离屏拷贝。
        mGlowView.setLayerType(View.LAYER_TYPE_NONE, null);
        mGlowView.setClipToOutline(false);
        mGlowView.setOutlineProvider(null);
        mGlowDrawable = new DigitGlowDrawable();
        mGlowView.setBackground(mGlowDrawable);
        previewPlate.addView(mGlowView);

        // 选中数字下面那根短色线：先加 = 画在文字下层；默认隐藏，展开取色时才出现。
        mMarkView = new View(this);
        mMarkView.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
        mMarkView.setVisibility(View.GONE);
        mMarkView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        previewPlate.addView(mMarkView);

        previewView = new TextView(this);
        previewView.setTextSize(44f);
        // 数字用「中粗 + 假粗」两层叠出更敦实的笔画：sans-serif-medium 在展开成 6 位大字时
        // 笔画偏细、跟状态栏那组粗数字对不上（用户要求「散开的 6 个数字变粗点」）。
        // 假粗（FakeBold）是在字形上再描一道边，比直接换 Typeface.BOLD 更可控——
        // 后者会把数字换成另一套字形（数字宽度、字距都会变，等宽 tnum 也会被破坏）。
        previewView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        previewView.getPaint().setFakeBoldText(true);
        // 等宽数字：否则「1」比别的数字窄，秒数一跳整行就左右抖，下划线和发光会跟着抖。
        previewView.setFontFeatureSettings("tnum");
        previewView.setTextColor(text());
        previewView.setGravity(Gravity.CENTER_HORIZONTAL);
        // 永远单行：展开动画中途字号和字距同时变，总有一两帧比可用宽度略宽，
        // 一旦换行预览卡会瞬间高出一整行、下面的卡片先被顶下去再弹回来（用户看到「往上跳」）。
        // 换成单行后宁可这两帧被裁掉几个像素，也不让卡片高度变。
        previewView.setSingleLine(true);
        // 点一下展开成逐位取色；展开后再点就选中被点的那一位数字。
        previewView.setOnClickListener(v -> {
            if (!mDigitsExpanded) {
                expandDigits();
            }
        });
        previewView.setOnTouchListener((v, ev) -> {
            if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
                if (!mDigitsExpanded) {
                    expandDigits();
                } else {
                    pickDigitAt(ev.getX(), ev.getY());
                }
            }
            return false;   // 不吃事件：点击/长按/无障碍仍按原样传递
        });
        previewView.setClickable(true);   // 无障碍：让 TalkBack 识别为可点击项
        // 展开态下宽度一旦变化（首次 layout、旋转、卡片重建）就按新宽度重算字号字距：
        // 否则会沿用「宽度还没量好」时的旧值，整行比可用宽度宽一点就换行了。
        previewView.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            if (!mDigitsExpanded || (r - l) == (or - ol)) {
                return;
            }
            computeExpandedMetrics();
            previewView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, mExpandedSizePx);
            previewView.setLetterSpacing(mExpandedSpacingEm);
            previewView.post(this::fitExpandedText);
        });
        previewPlate.addView(previewView);

        card.addView(previewPlate);

        // 「网络时间」行 + 关闭圆钮共用一格：展开取色时网络时间隐藏、X 正好落在它的位置上
        // （用户指定）。两者叠在同一个 FrameLayout 里，行高由网络时间决定，X 不会额外撑高卡片，
        // 所以展开/收起时下面的卡片一动不动。
        FrameLayout netSlot = new FrameLayout(this);
        // X 会用 translationY 往上挪到数字与弧顶之间的空带里，超出这一格的部分不能被裁掉。
        netSlot.setClipChildren(false);

        // 网络时间对比：点一下重新获取（HTTP Date 头，按 RTT 折半校正）。
        LinearLayout netRow = new LinearLayout(this);
        netRow.setOrientation(LinearLayout.HORIZONTAL);
        netRow.setGravity(Gravity.CENTER);
        netRow.setPadding(0, dp(26), 0, 0);

        netTimeView = new TextView(this);
        netTimeView.setTextSize(12f);
        netTimeView.setTextColor(text2());
        netTimeView.setOnClickListener(v -> fetchNetworkTime());
        netTimeView.setClickable(true);   // 无障碍
        netRow.addView(netTimeView);

        // 与网络时间有明显误差时才出现「同步」按钮。
        syncView = new TextView(this);
        syncView.setText(R.string.settings_sync_time);
        syncView.setTextSize(11f);
        syncView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        syncView.setTextColor(accent());
        syncView.setPadding(dp(10), dp(3), dp(10), dp(3));
        GradientDrawable syncBg = new GradientDrawable();
        syncBg.setCornerRadius(dp(999));
        syncBg.setColor(night ? 0xFF3A2F5C : 0xFFE7DFFF);
        syncView.setBackground(syncBg);
        LinearLayout.LayoutParams syncLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        syncLp.leftMargin = dp(8);
        syncView.setLayoutParams(syncLp);
        syncView.setOnClickListener(v -> syncNetworkTime());
        syncView.setClickable(true);   // 无障碍
        netRow.addView(syncView);
        mNetRow = netRow;
        netSlot.addView(netRow, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        card.addView(netSlot);

        // 展开取色时才出现的「关闭」圆钮（X）。挂在卡片最后 = 画在最上层；
        // 负的 bottomMargin 让它在纵向不占高度，实际位置由 positionMark() 按选中位墨迹算出来。
        mDigitDoneView = new CloseCrossView(this, accent(), 0xFFFFFFFF);
        LinearLayout.LayoutParams doneLp =
                new LinearLayout.LayoutParams(dp(34), dp(34));
        doneLp.gravity = Gravity.CENTER_HORIZONTAL;
        doneLp.bottomMargin = -dp(34);
        mDigitDoneView.setLayoutParams(doneLp);
        mDigitDoneView.setVisibility(View.GONE);
        mDigitDoneView.setContentDescription(getString(R.string.settings_digit_done));
        mDigitDoneView.setOnClickListener(v -> collapseDigits());
        card.addView(mDigitDoneView);

        root.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        updatePreview();
        fetchNetworkTime();
    }

    /** 初始化按压音 SoundPool（自带 press_click.wav，独立于系统「触摸音」开关）。 */
    private void initPressSound() {
        mPressPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        mPressPool.setOnLoadCompleteListener((pool, sampleId, status) -> {
            if (status == 0 && sampleId == mPressSoundId) {
                mPressSoundReady = true;
            }
        });
        mPressSoundId = mPressPool.load(this, R.raw.press_click, 1);
    }

    /** 播放按压音：音量 0.55 的短促「嗒」。 */
    private void playPressSound() {
        if (mPressPool != null && mPressSoundReady && mPressSoundId != 0) {
            mPressPool.play(mPressSoundId, 0.55f, 0.55f, 0, 0, 1f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPressPool != null) {
            mPressPool.release();
            mPressPool = null;
            mPressSoundReady = false;
        }
    }

    /** 两张并排小卡：切换手势 / 小时颜色。 */
    private void buildStatsRow(LinearLayout root) {
        // 并排等高行：自定义 EqualHeightRow 双遍测量，把两张卡统一按最高的一张定高，保证下端对称。
        // （AOSP 的 setMeasureWithLargestChildEnabled 只对 height=0+weight 的子 View 生效，普通 wrap_content 卡无效）
        EqualHeightRow row = new EqualHeightRow(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.bottomMargin = dp(12);
        row.setLayoutParams(rowLp);

        LinearLayout gestureCard = statCard();
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp1.rightMargin = dp(6);
        gestureCard.setLayoutParams(lp1);
        TextView gestureLabel = statLabel(R.string.settings_gesture_title);
        gestureCard.addView(gestureLabel);
        TextView gestureValue = new TextView(this);
        gestureValue.setText(gestureNameRes(
                ClockSecondsConfig.getGesture(getContentResolver())));
        gestureValue.setTextSize(19f);
        gestureValue.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        gestureValue.setTextColor(text());
        gestureValue.setPadding(0, dp(6), 0, 0);
        // 值行占满剩余高度并贴底：与右卡的值行底部对齐；长文案最多两行+省略号，不会撑宽卡片。
        gestureValue.setGravity(Gravity.BOTTOM | Gravity.START);
        gestureValue.setMaxLines(2);
        gestureValue.setEllipsize(TextUtils.TruncateAt.END);
        gestureValue.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        gestureCard.addView(gestureValue);
        gestureCard.setOnClickListener(v -> showGestureDialog(gestureValue));
        row.addView(gestureCard);

        LinearLayout colorCard = statCard();
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp2.leftMargin = dp(6);
        colorCard.setLayoutParams(lp2);
        colorCard.addView(statLabel(R.string.settings_color_title));

        LinearLayout colorRow = new LinearLayout(this);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        // 贴底：与左卡的值行底部对齐（水平方向仍靠起始边）。
        colorRow.setGravity(Gravity.BOTTOM | Gravity.START);
        colorRow.setPadding(0, dp(8), 0, 0);
        colorRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        colorDotView = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(26), dp(26));
        dotLp.rightMargin = dp(8);
        colorDotView.setLayoutParams(dotLp);
        colorRow.addView(colorDotView);

        colorValueView = new TextView(this);
        colorValueView.setTextSize(16f);
        colorValueView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        colorValueView.setTextColor(text());
        // 占满色点右侧剩余宽度：长文案省略显示，而不是把卡片撑宽。
        colorValueView.setMaxLines(1);
        colorValueView.setEllipsize(TextUtils.TruncateAt.END);
        colorValueView.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        colorRow.addView(colorValueView);
        colorCard.addView(colorRow);
        row.addView(colorCard);

        mStatsRow = row;
        root.addView(row);
        refreshColorSummary();
    }

    /**
     * 数字颜色卡：时分秒六位数字分别取色。
     *
     * <p>「取哪一位」不在卡里点，而是点上面预览卡里的数字（{@link #pickDigitAt}）；
     * 卡片只负责给「正在编辑的那一位」换颜色：预设 10 色、调色盘、跟随系统。
     * 布局沿用原来小时颜色卡的做法（圆角卡 + 右下同心弧 + 5 列色块网格 + 胶囊按钮）。</p>
     */
    private void buildColorCard(LinearLayout root) {
        // 卡片在 setContentView 阶段构建，早于 onResume 的 refreshConfigCache，
        // 因此这里先直读一次配置，否则色块与摘要会停在默认值（全 0 / 关闭态）。
        mCfgHourColorOn = ClockSecondsConfig.isHourColorEnabled(getContentResolver());
        mCfgHourColor = ClockSecondsConfig.getHourColor(getContentResolver());
        mCfgDigitColors = ClockSecondsConfig.getDigitColors(getContentResolver());

        LinearLayout card = card();

        // 右下角三条同心半弧装饰，固定浅薰衣草紫。
        CornerArcsDrawable arcs = new CornerArcsDrawable(WAVE_COLOR, dp(2));
        mCornerArcs = arcs;
        card.setBackground(new LayerDrawable(new Drawable[]{card.getBackground(), arcs}));

        // 标题行：标题 + NEW 徽章 + 右侧总开关（关掉后状态栏恢复系统默认样式）。
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(R.string.settings_digit_title);
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        titleRow.addView(title);

        TextView badge = new TextView(this);
        badge.setText(R.string.settings_digit_new);
        badge.setTextSize(9f);
        badge.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        badge.setTextColor(accent());
        badge.setPadding(dp(6), dp(1), dp(6), dp(1));
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(dp(999));
        badgeBg.setColor(night ? 0xFF3A2F5C : 0xFFE7DFFF);
        badge.setBackground(badgeBg);
        LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        badgeLp.leftMargin = dp(6);
        badge.setLayoutParams(badgeLp);
        titleRow.addView(badge);

        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        titleRow.addView(spacer);

        mDigitSwitch = new Switch(this);
        mDigitSwitch.setChecked(ClockSecondsConfig.isHourColorEnabled(getContentResolver()));
        styleSwitch(mDigitSwitch);
        titleRow.addView(mDigitSwitch);
        card.addView(titleRow);

        TextView sub = new TextView(this);
        sub.setText(R.string.settings_digit_sum);
        sub.setTextSize(11f);
        sub.setTextColor(text2());
        sub.setPadding(0, dp(4), 0, 0);
        card.addView(sub);

        // 「正在编辑：时十」——点上面预览里的数字即可切换这一行。
        mDigitEditingView = new TextView(this);
        mDigitEditingView.setTextSize(11f);
        mDigitEditingView.setTextColor(accent());
        mDigitEditingView.setPadding(0, dp(10), 0, 0);
        card.addView(mDigitEditingView);

        TextView preset = new TextView(this);
        preset.setText(R.string.settings_digit_preset);
        preset.setTextSize(11f);
        preset.setTextColor(text3());
        preset.setPadding(0, dp(10), 0, 0);
        card.addView(preset);

        mDigitGrid = new GridLayout(this);
        mDigitGrid.setColumnCount(5);
        mDigitGrid.setPadding(0, dp(6), 0, 0);
        for (int color : HOUR_COLORS) {
            mDigitGrid.addView(makeSwatch(color, color == slotColor(mSelSlot), mDigitGrid));
        }
        card.addView(mDigitGrid);

        // 胶囊：调色盘（自绘 HSV）/ 跟随系统（清掉该位颜色）。
        LinearLayout pillRow = new LinearLayout(this);
        pillRow.setOrientation(LinearLayout.HORIZONTAL);
        pillRow.setPadding(0, dp(12), 0, 0);
        TextView paletteBtn = pillButton(getString(R.string.settings_digit_palette));
        paletteBtn.setOnClickListener(v -> showPaletteDialog(mSelSlot));
        pillRow.addView(paletteBtn);
        TextView followBtn = pillButton(getString(R.string.settings_digit_follow));
        LinearLayout.LayoutParams followLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        followLp.leftMargin = dp(8);
        followBtn.setLayoutParams(followLp);
        followBtn.setOnClickListener(v -> applyDigitColor(mSelSlot, ClockSecondsConfig.DIGIT_COLOR_FOLLOW));
        pillRow.addView(followBtn);
        card.addView(pillRow);

        // 底部摘要：时 #2196F3 · 分 跟随系统 · 秒 #FF5252
        mDigitSummaryView = new TextView(this);
        mDigitSummaryView.setTextSize(11f);
        mDigitSummaryView.setTextColor(text3());
        mDigitSummaryView.setPadding(0, dp(10), 0, 0);
        card.addView(mDigitSummaryView);

        root.addView(card);

        final boolean[] reverting = {false};
        mDigitSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (reverting[0]) {
                return;
            }
            Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_HOUR_COLOR_ENABLED)
                    .putExtra(ClockSecondsConfig.EXTRA_ENABLED, checked ? 1 : 0);
            applyConfig(ClockSecondsConfig.HOUR_COLOR_ENABLED_KEY, checked ? 1 : 0, intent, ok -> {
                if (ok) {
                    mCfgHourColorOn = checked;
                    refreshDigitSwatches();
                    refreshColorSummary();
                    updatePreview();
                } else {
                    reverting[0] = true;
                    mDigitSwitch.setChecked(!checked);
                    reverting[0] = false;
                    toastWriteFailed();
                }
            });
        });
        refreshDigitSwatches();
    }

    // ==================================================================
    // 数字颜色：预览卡展开取色 / 六位分别上色
    // ==================================================================

    /** 点预览时间：展开成逐位取色（数字放大拉开，下方两张统计卡让位）。 */
    private void expandDigits() {
        if (mDigitsExpanded || previewView == null) {
            return;
        }
        mDigitsExpanded = true;
        // 先站平：抬高动作要等数字摊开之后才开始（否则展开动画里那一位是斜着飞上去的）。
        mLiftNow = 0;
        if (previewPlate != null) {
            previewPlate.setPadding(dp(2), dp(2), dp(2), dp(14));
        }
        // 目标：预览卡的底边正好落在原来两张统计卡的底边上（用户画红线的位置）。
        // 记法：最终卡片高度 = 折叠高度 + 统计卡高度。展开过程中 X 圆钮出现、数字变大都会自己改变
        // 高度，所以不能直接拿统计卡高度当延伸量，要等一帧布局完成后再用「目标高度 − 当前高度」反推。
        final int cardHFold = (mPreviewCard != null) ? mPreviewCard.getHeight() : 0;
        mCardHFold = cardHFold;
        // 统计卡不要立刻 GONE：立刻撤掉会让下面的卡片瞬间上跳一截、中间空一块。
        // 改成「卡片长多少、统计卡就矮多少」，空间一对一转移，下面的卡片一动不动。
        mStatsH = (mStatsRow != null) ? mStatsRow.getHeight() : 0;
        // X 圆钮一旦 VISIBLE 就立刻占掉 44dp（34+10）高度；不在这里同步扣掉，
        // 它会把下面所有卡片顶下去 154px，下一帧补偿动画再拉回来 —— 这就是用户看到的
        // 「数字颜色卡往上跳一下」。这里先扣，等于把这 154px 立刻从统计卡身上转走。
        // X 现在和「网络时间」共用一格，不再额外占高度（vc95 前这里是 dp(34)+dp(10)=154px）。
        mXSpace = 0;
        if (mNetRow != null) {
            mNetRow.setVisibility(View.INVISIBLE);   // 藏起来但保留行高，卡片高度不变
        }
        mStatsStartH = mStatsH;
        if (mStatsRow != null && mStatsH > mXSpace
                && mStatsRow.getLayoutParams() instanceof LinearLayout.LayoutParams) {
            final LinearLayout.LayoutParams slp =
                    (LinearLayout.LayoutParams) mStatsRow.getLayoutParams();
            slp.height = mStatsH - mXSpace;
            mStatsRow.setLayoutParams(slp);
            mStatsStartH = mStatsH - mXSpace;
        }
        if (mDigitDoneView != null) {
            mDigitDoneView.setVisibility(View.VISIBLE);
        }
        // X 和刻度弧都等卡片长完再淡进来：卡片还没长高时弧顶会顶到数字上、X 会浮在空白里。
        setDecorAlpha(0f);
        if (mPreviewCard != null && cardHFold > 0) {
            mPreviewCard.getViewTreeObserver().addOnGlobalLayoutListener(
                    new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            mPreviewCard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            if (!mDigitsExpanded) {
                                return;
                            }
                            final int targetH = cardHFold + mStatsH;
                            if (BuildConfig.DEBUG) {
                                android.util.Log.d("ColorOSClockSeconds", "CS-GROW2 cardHFold=" + cardHFold
                                        + " statsH=" + mStatsH + " startH=" + mStatsStartH
                                        + " xSpace=" + mXSpace + " nowH=" + mPreviewCard.getHeight()
                                        + " targetH=" + targetH + " grow=" + (targetH - mPreviewCard.getHeight()));
                            }
                            animateGrow(Math.max(0, targetH - mPreviewCard.getHeight()));
                        }
                    });
        } else {
            animateGrow(statsRowSpace());
        }
        if (previewHintView != null) {
            previewHintView.setVisibility(View.INVISIBLE);
        }
        mFitGuard = 0;
        mFitSafety = 0.99f;
        mSizeShrink = 1f;
        if (mMarkView != null) {
            mMarkView.setVisibility(View.GONE);   // 展开动画期间先不画线，等数字落位
        }
        stopGlowLoop();
        setPreviewGap(true);
        animateTilt(true);
        beginSmoothLayout();
        animateDigits(true);
        refreshDigitSwatches();
        previewView.postDelayed(this::fitExpandedText, 300L);
        previewView.setContentDescription(getString(R.string.settings_digit_sum));
    }

    /** 「完成」：收起预览，下面两张统计卡回归。 */
    private void collapseDigits() {
        if (!mDigitsExpanded) {
            return;
        }
        mDigitsExpanded = false;
        // 卡片变矮时刻度弧会跟着缩、往上爬到数字上，先把它和 X 淡出去，落定后再淡回来。
        fadeDecorTo(0f, 120L);
        // 不要在这里直接重置内边距，否则收起动画（animateGrow(0)）会失效、卡片瞬间弹回。
        animateTilt(false);
        setPreviewGap(false);
        beginSmoothLayout();
        animateGrow(0);                        // 收回延伸（结束时才把统计卡放回来）
        if (mMarkView != null) {
            mMarkView.setVisibility(View.GONE);
        }
        stopGlowLoop();
        // 先落回与其他位同一条基线，再缩回默认大小（用户要求：别一边缩一边降）。
        animateLift(false, () -> animateDigits(false));
    }

    /** 展开态下沿单侧削掉的宽度（px）：与下沿成 20 度。 */
    private float mSlantCutPx;

    /** 展开态预览卡向下延伸的像素（给隐藏掉的两张统计卡让位），动画逐帧累加。 */
    private int mGrowPx;
    private android.animation.ValueAnimator mGrowAnim;
    /**
     * 选中位当前的抬高量（px）。
     *
     * <p>不是常量：展开时先让数字摊开（字号/字距动画），摊好了才把它抬起来；收起时反过来，
     * 先让它落回与其他位同一基线，再缩回默认大小。用户明确要求这两个动作不要同时发生。</p>
     */
    private int mLiftNow;
    private android.animation.ValueAnimator mLiftAnim;

    /**
     * 取色时把预览卡的**下端向内倾斜 20 度**：卡片底板下沿两个角被斜着削掉一块。
     *
     * <p>早先试过整卡 3D 旋转（rotationX + 透视），内容会被压扁、底部整块被裁掉，不可用；
     * 现在改成改底板形状：内容位置一点不动，只是卡片轮廓的下沿变斜，收起时斜切收回到 0。</p>
     */
    private void animateTilt(final boolean tilt) {
        // 卡片倾斜方案已按用户要求撤掉（「不用倾斜了，弄回原来样子」）：底板恢复普通圆角矩形。
        // 保留这个空方法，两个调用点（展开/收起）不必改，将来要再做倾斜时只在这里接回来。
        if (mSlantCard == null) {
            return;
        }
        mSlantCutPx = 0f;
        mSlantCard.setRise(0f);
    }

    /**
     * 展开时让预览卡**慢慢向下延伸**到原来两张统计卡的底部位置（收起时收回）。
     *
     * <p>做法是给 {@code previewPlate} 加下内边距：卡片高度由内容撑，下内边距一变就整体变高，
     * 用 ValueAnimator 逐帧改，视觉上就是卡片自己往下长。不走 TransitionManager，
     * 因为 ChangeBounds 会把整张卡一起缩放，圆角和数字都会跟着变形。</p>
     */
    private void animateGrow(final int targetPx) {
        if (previewPlate == null) {
            return;
        }
        if (mGrowAnim != null) {
            mGrowAnim.cancel();
            mGrowAnim = null;
        }
        final boolean expand = mDigitsExpanded;
        // 直接写死卡片高度，而不是改内边距让内容自己撑：
        // 展开动画期间数字还在变大、提示行还在变，内容一抖卡片就跟着抖，下面的卡片就被顶下去再弹回来
        // （vc86/87 实测跳 42px）。高度由我们逐帧给定，内容就再也影响不到别人。
        final int fromH = (mPreviewCard != null) ? mPreviewCard.getHeight() : 0;
        // 展开：长 targetPx 这么多；收起：缩回折叠态高度（mCardHFold）。
        final int toH = expand ? (fromH + targetPx)
                : (mCardHFold > 0 ? mCardHFold : fromH);
        final int statsFrom = (mStatsRow != null) ? mStatsRow.getHeight() : 0;
        final int statsTo = expand ? 0 : (mStatsStartH > 0 ? mStatsStartH : mStatsH);
        final int statsH = mStatsStartH > 0 ? mStatsStartH : (mStatsH > 0 ? mStatsH : 0);
        // 收起时先把统计卡以「0 高 + 全透明」放回来，它再跟着卡片变矮一路长高、显形。
        if (!expand && mStatsRow != null && statsH > 0) {
            mStatsRow.setVisibility(View.VISIBLE);
            mStatsRow.setAlpha(0f);
            if (mStatsRow.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                final LinearLayout.LayoutParams lp =
                        (LinearLayout.LayoutParams) mStatsRow.getLayoutParams();
                lp.height = 0;
                mStatsRow.setLayoutParams(lp);
            }
        }
        final android.animation.ValueAnimator anim =
                android.animation.ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(420L);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            final float t = (float) (Float) a.getAnimatedValue();
            mGrowPx = Math.round((toH - fromH) * t);
            // 卡片高度逐帧给定；统计卡与之反向等量变化（两者之和恒定）→ 下面那张卡一动不动。
            if (mPreviewCard != null
                    && mPreviewCard.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                final LinearLayout.LayoutParams clp =
                        (LinearLayout.LayoutParams) mPreviewCard.getLayoutParams();
                clp.height = Math.round(fromH + (toH - fromH) * t);
                mPreviewCard.setLayoutParams(clp);
            }
            if (mStatsRow != null && statsH > 0
                    && mStatsRow.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                final LinearLayout.LayoutParams lp =
                        (LinearLayout.LayoutParams) mStatsRow.getLayoutParams();
                lp.height = Math.round(statsFrom + (statsTo - statsFrom) * t);
                mStatsRow.setLayoutParams(lp);
                mStatsRow.setAlpha(expand ? (1f - t) : t);
            }
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator a) {
                if (mGrowAnim == a) {
                    mGrowAnim = null;
                }
                // 只在收起结束时把高度交还给内容；展开态保持固定高度，
                // 否则「内容自己撑」和「固定高度」两种模式一切换，卡片会瞬间缩回去（vc90 实测矮了 130px）。
                if (!mDigitsExpanded && mPreviewCard != null
                        && mPreviewCard.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    final LinearLayout.LayoutParams clp =
                            (LinearLayout.LayoutParams) mPreviewCard.getLayoutParams();
                    clp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    mPreviewCard.setLayoutParams(clp);
                }
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("ColorOSClockSeconds", "CS-GROW end grow=" + mGrowPx
                            + " cardH=" + (mPreviewCard == null ? -1 : mPreviewCard.getHeight())
                            + " plateH=" + (previewPlate == null ? -1 : previewPlate.getHeight())
                            + " platePadB=" + (previewPlate == null ? -1 : previewPlate.getPaddingBottom()));
                }
                if (mStatsRow != null && mStatsRow.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                    final LinearLayout.LayoutParams lp =
                            (LinearLayout.LayoutParams) mStatsRow.getLayoutParams();
                    if (mDigitsExpanded) {
                        lp.height = 0;
                        mStatsRow.setLayoutParams(lp);
                        mStatsRow.setVisibility(View.GONE);   // 长完了，位置已让给预览卡
                    } else {
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;   // 收回了，交还正常高度
                        mStatsRow.setLayoutParams(lp);
                        mStatsRow.setVisibility(View.VISIBLE);
                        mStatsRow.setAlpha(1f);
                    }
                }
                if (mDigitDoneView != null) {
                    if (mDigitsExpanded) {
                        mDigitDoneView.setVisibility(View.VISIBLE);
                    } else {
                        mDigitDoneView.setVisibility(View.GONE); // X 等缩回去再撤掉，否则卡片瞬间矮一截
                        if (mNetRow != null) {
                            mNetRow.setVisibility(View.VISIBLE);   // 网络时间回到原位
                        }
                    }
                }
                // 卡片高度落定后再把刻度弧 / X 淡进来（高度动画期间它们会跑到数字上）。
                fadeDecorTo(1f, 200L);
            }
        });
        mGrowAnim = anim;
        anim.start();
    }

    /** 统计卡的原始高度（展开时记下，收起时按这个高度长回来）。 */
    private int mStatsH;
    /** 统计卡在「X 已经占掉 44dp」之后的起始高度（动画从这个值开始缩）。 */
    private int mStatsStartH;
    /** X 圆钮占掉的竖向空间（34dp 高 + 10dp 上外边距）。 */
    private int mXSpace;
    /** 折叠态预览卡的高度（收起动画要缩回这个高度）。 */
    private int mCardHFold;

    /** 刻度弧 + X 圆钮当前的透明度（展开/收起动画期间淡出，避免刻度爬上数字）。 */
    private float mDecorAlpha = 1f;
    private android.animation.ValueAnimator mDecorAnim;

    private void setDecorAlpha(float alpha) {
        mDecorAlpha = Math.max(0f, Math.min(1f, alpha));
        if (mDigitDoneView != null) {
            mDigitDoneView.setAlpha(mDecorAlpha);
        }
        if (mArcTicks != null) {
            mArcTicks.setGlobalAlpha(mDecorAlpha);
        }
    }

    private void fadeDecorTo(final float target, final long duration) {
        if (mDecorAnim != null) {
            mDecorAnim.cancel();
            mDecorAnim = null;
        }
        if (Math.abs(mDecorAlpha - target) < 0.01f) {
            setDecorAlpha(target);
            return;
        }
        final float from = mDecorAlpha;
        final android.animation.ValueAnimator anim =
                android.animation.ValueAnimator.ofFloat(from, target);
        anim.setDuration(duration);
        anim.addUpdateListener(a -> setDecorAlpha((float) (Float) a.getAnimatedValue()));
        mDecorAnim = anim;
        anim.start();
    }

    /**
     * 展开时预览卡要向下延伸多少：一直长到原来两张统计卡的**底边**（用户画红线的位置）。
     *
     * <p>直接按几何算：(统计卡顶 − 预览卡底) + 统计卡高。比按「高度 + margin」拼更准，
     * 外面的 margin 怎么改都自动跟上。</p>
     */
    private int statsRowSpace() {
        if (mStatsRow == null || mPreviewCard == null) {
            return 0;
        }
        final int rowTop = mStatsRow.getTop() - mPreviewCard.getTop();
        final int cardBottom = mPreviewCard.getHeight();
        final int extra = (rowTop - cardBottom) + mStatsRow.getHeight();
        if (BuildConfig.DEBUG) {
            android.util.Log.d("ColorOSClockSeconds",
                    "CS-GROW rowTop=" + rowTop + " cardH=" + cardBottom
                            + " cardTopAbs=" + mPreviewCard.getTop() + " rowTopAbs=" + mStatsRow.getTop()
                            + " rowH=" + mStatsRow.getHeight() + " extra=" + extra);
        }
        return Math.max(0, extra + dp(2));
    }

    /** 预览卡与「数字颜色」卡之间拉开间隙：取色时卡片倾斜后会贴上去，留白要更大。 */
    private void setPreviewGap(boolean expanded) {
        if (mPreviewCard == null) {
            return;
        }
        final ViewGroup.LayoutParams raw = mPreviewCard.getLayoutParams();
        if (!(raw instanceof LinearLayout.LayoutParams)) {
            return;
        }
        final LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) raw;
        lp.bottomMargin = dp(12);   // 卡片之间保持和平时一样的间隙，不再为倾斜额外留白
        mPreviewCard.setLayoutParams(lp);
    }

    /** 展开/收起动画：字号 44sp → 铺满卡片的最大字号，字距 0 → 铺满后的最大间隙（约 260ms，数字被拉开）。 */
    /**
     * 抬起 / 落下选中那一位（带一点动画，避免突兀）。
     *
     * @param after 动画结束后要接着做的事（收起时用来串「先落下、再缩小」）
     */
    private void animateLift(final boolean up, final Runnable after) {
        final int target = up ? liftPx() : 0;
        if (mLiftAnim != null) {
            mLiftAnim.cancel();
            mLiftAnim = null;
        }
        if (mLiftNow == target) {
            if (after != null) {
                after.run();
            }
            return;
        }
        final ValueAnimator anim = ValueAnimator.ofInt(mLiftNow, target);
        anim.setDuration(up ? 220L : 160L);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            mLiftNow = (int) a.getAnimatedValue();
            updatePreview();                 // 重排 span：基线跟着变
            if (previewView != null) {
                previewView.post(this::positionMark);   // 下划线/光晕一起跟手
            }
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                mLiftNow = target;
                if (after != null) {
                    after.run();
                }
            }
        });
        mLiftAnim = anim;
        anim.start();
    }

    private void animateDigits(final boolean expand) {
        if (previewView == null) {
            return;
        }
        // 起点读当前真实字号（fitExpandedText 可能已经把展开字号收小过），终点按展开/收起算，
        // 这样来回切换不会跳变。
        final float base = sp(44);
        final float from = previewView.getTextSize();
        if (expand) {
            computeExpandedMetrics();   // 先算出「铺满整卡 + 间隙最大化」的字号和字距
        }
        final float to = expand ? mExpandedSizePx : base;
        final float spacingFrom = previewView.getLetterSpacing();
        final float spacingTo = expand ? mExpandedSpacingEm : 0f;
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(260L);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.addUpdateListener(a -> {
            final float t = (float) a.getAnimatedValue();
            previewView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, from + (to - from) * t);
            previewView.setLetterSpacing(spacingFrom + (spacingTo - spacingFrom) * t);
            // 动画期间不追光晕：等数字完全展开再点亮呼吸灯（否则一路上抖着追字）。
        });
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                if (expand && mDigitsExpanded) {
                    positionMark();     // 数字落位后才画下划线、摆光晕
                    previewView.post(ModuleSettingsActivity.this::fitExpandedText);   // 兜一层：真换行了就收字距/字号
                    startGlowLoop();    // 再开始呼吸
                    // 摊开之后再抬起选中那一位（用户要求：先拉开间隙，再抬高）。
                    animateLift(true, null);
                }
            }
        });
        anim.start();
    }

    /** 预览文本「每 1px 字号」占的宽度（实测，避免不同机型字体宽度偏差）。 */
    private float measurePerUnitWidth() {
        // 必须用副本：previewView.getPaint() 是 TextView 自己的 TextPaint。
        final android.text.TextPaint tp = new android.text.TextPaint(previewView.getPaint());
        tp.setTextSize(100f);
        tp.setLetterSpacing(0f);   // 量「纯字形宽度」，字距由调用方自己加，否则会重复计算
        final CharSequence txt = previewView.getText();
        final String sample = (txt == null || txt.length() == 0) ? "00:00:00" : txt.toString();
        return Math.max(0.1f, tp.measureText(sample) / 100f);
    }

    /** 预览区可用宽度（px）。 */
    private int previewAvailWidth() {
        return previewView.getWidth()
                - previewView.getPaddingLeft() - previewView.getPaddingRight();
    }

    /**
     * 字距要乘的份数。
     *
     * <p>Android 的 letterSpacing 是「每个字符后面都加一份」，最后一个字符后面也加，
     * 所以这里必须用字符总数、不能用「间隙数 = 字符数 - 1」。少算这一份会让整行比可用
     * 宽度多出一个字距，正好触发换行（vc41 在 PHP110 上「10:18:1 / 3」两行的事故）。</p>
     */
    private int previewGapCount() {
        final CharSequence txt = previewView.getText();
        return Math.max(1, txt == null ? 8 : txt.length());
    }

    /**
     * 展开态排版：数字间隙最大化 + 整行刚好铺满卡片可用宽度。
     *
     * <p>先假设间隙 = {@link #EXPAND_GAP_EM} 个字号宽，反解出字号；字号被上下限夹住后，
     * 剩余宽度全部补给间隙（avail = size × (每 px 字宽 + 字距 × 间隙数)）。</p>
     */
    private void computeExpandedMetrics() {
        if (previewView == null) {
            return;
        }
        final int avail = previewAvailWidth();
        final int gaps = previewGapCount();
        if (avail <= 0) {
            // 还没 layout 完（宽度读成 0）：这次先不设，等 previewView 的 onLayoutChange 再算。
            // 以前这里退化成「sp(52) + 0.14 字距」，在某些机型上正好比可用宽度多出一个字距，
            // 展开后偶发变成两行（真机复现：「15:45 / :34」）。
            return;
        }
        final float perUnit = measurePerUnitWidth();
        // 留余量：字体 hinting、亚像素取整、Layout 在行尾多算的一份字距都会让实测比解析式宽
        // 一点，压线就会换行。mFitSafety 会在 fitExpandedText 里逐次收紧。
        final float fit = avail * mFitSafety;
        float size = fit / (perUnit + EXPAND_GAP_EM * gaps);
        size = Math.max(sp(34), Math.min(sp(80), size)) * mSizeShrink;
        mExpandedSizePx = size;
        // 铺满整卡：把（可能剩下的）宽度全部灌进间隙。
        mExpandedSpacingEm = Math.max(0f, Math.min(0.90f, (fit / size - perUnit) / gaps));
        if (BuildConfig.DEBUG) {
            android.util.Log.d("CS-FIT", "avail=" + avail + " gaps=" + gaps + " perUnit=" + perUnit
                    + " size=" + Math.round(size) + " spacing=" + mExpandedSpacingEm
                    + " min34=" + Math.round(sp(34)) + " max58=" + Math.round(sp(58)));
        }
    }

    /**
     * 展开态下字符数变了（显秒开关被外部切换：5 位 ⇄ 8 位）时的整套重排。
     *
     * <p>为什么必须重排：展开态的排版指标全是「按当前这串字符的宽度反解」出来的 ——
     * {@link #measurePerUnitWidth()} 量的是预览当前文本每 1px 字号占多宽，
     * {@link #previewGapCount()} 用的是当前字符总数。字符数从 5 变 8 后如果沿用旧指标，
     * 字号偏大、字距份数偏少，整行要么超出可用宽度被裁、要么缩成一团挤在一起，
     * 下划线和光晕也会按旧的字形墨迹落在错位的数字下面（真机反馈：数字「错乱」）。</p>
     *
     * <p>所以这里把展开入口处的那套初值原样恢复一遍，再按新文本重新算字号与字距，
     * 最后重新定位下划线与光晕。</p>
     */
    private void relayoutExpanded() {
        if (!mDigitsExpanded || previewView == null) {
            return;
        }
        mFitGuard = 0;
        mFitSafety = 0.99f;
        mSizeShrink = 1f;
        computeExpandedMetrics();
        previewView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, mExpandedSizePx);
        previewView.setLetterSpacing(mExpandedSpacingEm);
        balanceExpandedWidth();
        // 点击选中的那一位在长度变化后可能已经越界（8 位 → 5 位时 slot4/slot5 消失），夹回合法范围，
        // 否则下划线和光晕会挂在一个不存在的字符上，表现为「线不见了 / 光晕停在半空」。
        final int len = previewView.getText() == null ? 0 : previewView.getText().length();
        final int maxSlot = len <= 0 ? 0 : len - 1;
        mSelSlot = Math.max(0, Math.min(mSelSlot, maxSlot));
        if (mMarkView != null) {
            mMarkView.setVisibility(View.VISIBLE);
        }
        previewView.post(() -> {
            fitExpandedText();
            positionMark();
            positionGlow();
            if (BuildConfig.DEBUG) {
                android.util.Log.d("CS-RELAYOUT", "len=" + len + " size=" + Math.round(mExpandedSizePx)
                        + " spacing=" + mExpandedSpacingEm + " sel=" + mSelSlot);
            }
        });
    }

    /**
     * 展开后的兜底收尾：按「实际行数」校验，还换行就先收紧宽度安全系数（重算字号与字距，
     * 整行仍然铺满），收到 0.80 还会换行才压字号。最多 8 次。
     *
     * <p>只降字号的旧做法会让整行缩得比折叠态还小，所以优先收字距。</p>
     */
    private void fitExpandedText() {
        if (!mDigitsExpanded || previewView == null || mFitGuard >= 8) {
            return;
        }
        final android.text.Layout layout = previewView.getLayout();
        if (layout == null) {
            return;
        }
        if (layout.getLineCount() <= 1) {
            mFitGuard = 0;
            // 字号/字距可能在上一轮被收紧过（含显秒开关切换引发的重排），
            // 下划线与光晕是按「当前字形墨迹」定位的，这里必须跟着重算一次，
            // 否则会停在按旧字号算出来的位置上（真机表现为「线跑到数字左边/光晕飘在外面」）。
            positionMark();
            positionGlow();
            return;
        }
        mFitGuard++;
        if (mFitSafety > 0.80f) {
            mFitSafety -= 0.03f;
        } else {
            mSizeShrink *= 0.94f;
        }
        computeExpandedMetrics();
        previewView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, mExpandedSizePx);
        previewView.setLetterSpacing(mExpandedSpacingEm);
        if (BuildConfig.DEBUG) {
            android.util.Log.d("CS-LAYOUT", "retry=" + mFitGuard + " safety=" + mFitSafety
                    + " shrink=" + mSizeShrink + " size=" + Math.round(mExpandedSizePx)
                    + " spacing=" + mExpandedSpacingEm);
        }
        previewView.post(this::fitExpandedText);
    }

    /**
     * 把展开态的（字号, 字距）调成「整行刚好铺满可用宽度」。
     *
     * <p>解析式反解出来的值总差一点点（字体 hinting、每字符后多加的一份字距、
     * 亚像素取整），所以这里再按实测宽度二分校正一次：只调字距不动字号，
     * 字号是视觉主角，字距只是把剩余空间铺开。</p>
     */
    private void balanceExpandedWidth() {
        if (!mDigitsExpanded || previewView == null || mExpandedSizePx <= 0f) {
            return;
        }
        final CharSequence txt = previewView.getText();
        if (txt == null || txt.length() == 0) {
            return;
        }
        final int avail = previewAvailWidth();
        if (avail <= 0) {
            return;
        }
        final android.text.TextPaint tp = new android.text.TextPaint(previewView.getPaint());
        tp.setTextSize(mExpandedSizePx);
        tp.setLetterSpacing(0f);
        final int n = txt.length();
        final float glyph = tp.measureText(txt.toString());
        // letterSpacing 的单位是 em（相对字号），每个字符后面各加一份（含最后一个）。
        final float want = Math.max(0f, (avail - glyph) / (float) n);
        final float em = Math.max(0f, Math.min(0.90f, want / mExpandedSizePx));
        if (Math.abs(em - mExpandedSpacingEm) < 0.002f) {
            return;
        }
        mExpandedSpacingEm = em;
        previewView.setLetterSpacing(em);
        if (BuildConfig.DEBUG) {
            android.util.Log.d("CS-BALANCE", "n=" + n + " glyph=" + Math.round(glyph) + " avail=" + avail
                    + " em=" + em);
        }
    }

    /** sp → px。 */
    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }

    /** 展开态下点预览：命中的是第几位数字，就切到那一位。 */
    private void pickDigitAt(float x, float y) {
        if (previewView == null) {
            return;
        }
        final CharSequence text = previewView.getText();
        if (text == null || text.length() == 0) {
            return;
        }
        // 同 measureSelInk()：不能走 Layout，singleLine 的 Layout 宽度是 1048576px，
        // getOffsetForHorizontal 会把任何点击都算到行首附近，于是「点其他数字没反应」。
        final String whole = text.toString();
        final TextPaint tp = new TextPaint(previewView.getPaint());
        tp.setLetterSpacing(previewView.getLetterSpacing());
        final float avail = previewView.getWidth() - previewView.getTotalPaddingLeft()
                - previewView.getTotalPaddingRight();
        final float lineStart = Math.max(0f, (avail - tp.measureText(whole)) / 2f);
        final float lx = Math.max(0f, x - previewView.getTotalPaddingLeft());
        int offset = -1;
        for (int i = 0; i < whole.length(); i++) {
            final float x0 = lineStart + tp.measureText(whole, 0, i);
            final float x1 = lineStart + tp.measureText(whole, 0, i + 1);
            if (lx >= x0 && lx < x1) {
                offset = i;
                break;
            }
        }
        if (offset < 0) {
            return;   // 点在整行之外：保持当前选中位
        }
        int slot = ClockSecondsConfig.slotOfChar(text, offset);
        if (slot < 0 && offset > 0) {
            slot = ClockSecondsConfig.slotOfChar(text, offset - 1);
        }
        if (slot < 0 || slot >= ClockSecondsConfig.DIGIT_COUNT) {
            return;   // 点在冒号或空白处：保持当前选中位
        }
        mSelSlot = slot;
        refreshDigitSwatches();
        updatePreview();           // 未选中位变淡，要重排一次
        if (previewView != null) {
            previewView.post(this::positionMark);
        }
        startGlowLoop();           // 手一点就挪光晕（已经在呼吸就不打断节拍）
    }

    /** 选中位数字在 previewPlate 坐标系里的墨迹框（下划线与水滴落点都靠它定位）。 */
    private final android.graphics.RectF mSelInk = new android.graphics.RectF();
    /** 选中位字符的笔位（X 起点，已含字距）：下划线按它居中，避免墨迹左右不均导致歪线。 */
    private float mSelPenX;

    /**
     * 算出「正在取色的那一位数字」的墨迹框。
     *
     * <p>必须用 Layout 的笔位 + 行基线算，字距（letterSpacing）会改变笔位，所以每次
     * 字号/字距/文本一变都要重算，不能缓存。</p>
     *
     * @return false 表示当前算不出来（未展开 / 文本为空 / 位下标越界）
     */
    private boolean measureSelInk() {
        if (previewView == null || previewPlate == null) {
            return false;
        }
        final android.text.Layout layout = previewView.getLayout();
        final CharSequence text = previewView.getText();
        if (layout == null || text == null || text.length() == 0) {
            return false;
        }
        final int idx = indexOfSlot(mSelSlot);
        if (idx < 0 || idx >= text.length()) {
            return false;
        }
        final int line = layout.getLineForOffset(idx);
        final String glyph = String.valueOf(text.charAt(idx));
        // 必须用 paint 的【副本】量墨迹：previewView.getPaint() 返回的是 TextView 内部那个
        // TextPaint 实例，直接 setTextSize() 会改掉预览本身的字号 —— 每次刷新乘一个系数，
        // 几百毫秒内指数暴涨、数字换行、卡片撑爆屏幕（vc39 真机事故）。
        final TextPaint tp = new TextPaint(previewView.getPaint());
        // 量墨迹必须把字距清零：letterSpacing 会算进 getTextBounds/measureText 的宽度里，
        // 展开态字距有 0.2em 以上，带进去会让墨迹框整体变宽、下划线歪到一边（真机反馈）。
        tp.setLetterSpacing(0f);
        final Rect ink = new Rect();
        tp.getTextBounds(glyph, 0, glyph.length(), ink);
        // 必须补上 layout.getLineLeft()：居中排版的文本，TextView 画的时候会把整行平移
        // lineLeft，而 getPrimaryHorizontal() 返回的是相对行首的笔位，不含这段平移。
        // 少了它，下划线和光晕会整体偏左约 lineLeft（vc73~75 真机反馈「线对不上数字」）。
        // 不能用 layout.getPrimaryHorizontal/getLineLeft：vc87 给预览文字加了 setSingleLine(true) 之后，
        // TextView 会用一个 1048576px 宽的「超宽行」来排版再居中绘制（layoutW=1048576、lineL=523824），
        // 于是笔位和行左偏移都变成天文数字，下划线/光晕被推到屏幕外（vc92 真机：ink.x=1047676）。
        // 改成自己按 paint 量：整行宽度居中后累加各位宽度，跟 TextView 的实际绘制一致。
        final String whole = text.toString();
        final float avail = previewView.getWidth() - previewView.getTotalPaddingLeft()
                - previewView.getTotalPaddingRight();
        // 行宽和笔位必须用「带字距」的 paint 来量：TextView 排版时 letterSpacing 会算进
        // 每一个字符的步进（含最后一个），而上面 tp 为了量墨迹把字距清零了。拿零字距的
        // 行宽去算居中偏移，会把整行往右推大半个字符宽 —— 光晕/下划线整体偏到旁边那一位
        // 数字上（vc102 真机：ink 偏右 114px，正好一个步进）。
        final TextPaint tpL = new TextPaint(previewView.getPaint());
        tpL.setLetterSpacing(previewView.getLetterSpacing());
        final float lineW = tpL.measureText(whole);
        final float lineStart = Math.max(0f, (avail - lineW) / 2f);
        final float penX = previewView.getLeft() + previewView.getTotalPaddingLeft() + lineStart
                + tpL.measureText(whole, 0, idx);
        final float baseline = previewView.getTop() + previewView.getTotalPaddingTop()
                + layout.getLineBaseline(line);
        // 字距（letterSpacing）下，TextView 把每个字形画在「步进框」里，
        // 而 ink 是用字距清零的 paint 量的、只含字形本身，所以整体要往右补一段。
        // 补多少是逐位实测拟出来的：
        //   vc117 补 0.5 个字距 → 光晕/线仍偏左 12~18px；
        //   vc118 补 1.5 个字距 → 变成偏右 11~15px；
        //   斜率正好一个字距（0.22em × 153px ≈ 34px），最优点在 1.03 处：
        //   vc119 真机 6 位里 5 位误差 ≤1.5px。
        // 唯一例外是**第一位**：它的笔位就是行首，前面没有字距要补，
        //   1.03 个字距会让它偏右整整 33px（vc119 真机），所以首位的补偿单独去掉。
        // （vc120 试过直接用 layout.getPrimaryHorizontal 校真值，但 singleLine 下那段是
        //   天文数字的 layout，差值整体偏了 57px，已回退，别再走这条路。）
        final float lsUnit = previewView.getLetterSpacing() * previewView.getTextSize();
        final float penFix = (idx == 0) ? 0f : lsUnit * 1.03f;
        mSelInk.set(penX + penFix + ink.left, baseline + ink.top,
                penX + penFix + ink.right, baseline + ink.bottom);
        mSelPenX = penX + penFix;
        if (BuildConfig.DEBUG) {
            final int[] pvLoc = new int[2];
            previewView.getLocationOnScreen(pvLoc);
            // 用 Layout 的「真实」行宽/笔位做对照（singleLine 下 layout 宽度是天文数字，
            // 但 getLineRight-getLineLeft 的差值仍反映 TextView 实际排版的整行宽度）。
            final float realLineW = layout.getLineRight(line) - layout.getLineLeft(line);
            android.util.Log.d("CS-INK", "idx=" + idx + " g=[" + glyph + "]"
                    + " vw=" + previewView.getWidth() + " padL=" + previewView.getTotalPaddingLeft()
                    + " lineW=" + lineW + " realLineW=" + realLineW + " avail=" + avail
                    + " lineStart=" + lineStart
                    + " penX=" + penX + " inkL=" + ink.left + " inkR=" + ink.right
                    + " paintW=" + previewView.getPaint().measureText(whole)
                    + " sp=" + previewView.getLetterSpacing()
                    + " screenInkL=" + (pvLoc[0] + penX + ink.left)
                    + " screenInkR=" + (pvLoc[0] + penX + ink.right)
                    + " primaryH=" + layout.getPrimaryHorizontal(idx)
                    + " lineL=" + layout.getLineLeft(line)
                    + " lineR=" + layout.getLineRight(line)
                    + " lineW=" + layout.getWidth()
                    + " layoutAlgn=" + layout.getParagraphAlignment(line)
                    + " tvLeft=" + previewView.getLeft()
                    + " tvPadL=" + previewView.getPaddingLeft()
                    + " pvLoc=" + pvLoc[0] + "," + pvLoc[1]
                    + " realPenRelView=" + (layout.getPrimaryHorizontal(idx) - layout.getLineLeft(line))
                    + " myPenRelView=" + (penX - previewView.getLeft() - previewView.getTotalPaddingLeft()));
        }
        // 这里保持「未抬高」的原始墨迹（抬起量只存在 mLiftNow 里）：下划线直接用原墨迹
        // 落位（用户要求线不跟抬，vc112），光晕则自己减去当前 lift 贴到抬起的字形上。
        return true;
    }

    /** 把短色线摆到选中数字下面（宽度取字形的 62%，位置随字号/字距变化重算）。 */
    private void positionMark() {
        if (mMarkView == null || previewView == null || previewPlate == null || !mDigitsExpanded) {
            return;
        }
        if (!measureSelInk()) {
            mMarkView.setVisibility(View.GONE);
            return;
        }
        // 下划线用中性色（深色模式白、浅色模式黑）：跟着数字颜色走时，深色数字配深色线
        // 在白卡上几乎看不见，反而丢掉了「选中哪一位」的提示。
        final int color = night ? 0xFFFFFFFF : 0xFF000000;
        if (mMarkDrawable == null) {
            mMarkDrawable = new UnderlineDrawable(color);
            mMarkView.setBackground(mMarkDrawable);
        } else {
            mMarkDrawable.setColor(color);
        }
        // 横线要压在这位数字的「步进中心」上，不能用字形墨迹中心：像「1」这种带左上小旗
        // 的字，墨迹框整体偏左，按墨迹居中会让人看着横线歪到一边。
        // 横线**必须压在这位数字字形的视觉中心**（墨迹中心）上：用字宽/步进居中时，
        // 「4」「1」这类字形在步进框里左右不均，线会歪到数字旁边（vc73 真机反馈）。
        final float markCx = mSelInk.centerX();
        final int w = Math.max(dp(12), Math.round(mSelInk.width() * 0.92f));
        final int h = Math.max(dp(2), dp(3));
        // 线保持「未抬高时的字形底 + dp(10)」：用户明确要求线不要跟着数字抬起来
        // （vc112 真机反馈「线是保持原来位置，不用也抬高」）。
        int top = Math.round(mSelInk.bottom + dp(10));
        final int plateH = previewPlate.getHeight();
        if (plateH > 0) {
            top = Math.min(top, plateH - h - dp(2));
        }
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mMarkView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        lp.leftMargin = Math.round(markCx - w / 2f) - previewPlate.getPaddingLeft();
        lp.topMargin = top - previewPlate.getPaddingTop();
        mMarkView.setLayoutParams(lp);
        mMarkView.setVisibility(View.VISIBLE);
        positionGlow();   // 光晕跟下划线共用同一套墨迹框，一起落位
        // X 圆钮：落在「数字下方到刻度弧顶之间」那条空带里（用户用绿笔圈出来的位置），
        // 也就是下划线再往下一点。它在卡片里不占高度，靠 translationY 挪过去。
        // X 圆钮：用户指定「放在长弧内部的正中」。刻度弧的弧顶在卡片高度的 0.48 处
        // （见 ArcTickDrawable.peakY），弧两端落在卡片底边，所以内部中线 = (弧顶 + 卡片底)/2。
        if (mDigitDoneView != null && mDigitDoneView.getHeight() > 0 && mPreviewCard != null) {
            final int cardH = mPreviewCard.getHeight();
            final int apex = Math.round(cardH * 0.48f);
            // 0.56 = 用户实测的「十字中心」位置：从弧顶往下走到卡片底边的 56% 处。
            final int targetCenterY = apex + Math.round((cardH - apex) * 0.56f);
            final int defaultCenter = mDigitDoneView.getTop() + mDigitDoneView.getHeight() / 2;
            mDigitDoneView.setTranslationY(targetCenterY - defaultCenter);
        }
        if (BuildConfig.DEBUG) {
            android.util.Log.d("CS-MARK", "mark slot=" + mSelSlot + " ink=" + mSelInk
                    + " lp=" + w + "x" + h + "@" + lp.leftMargin + "," + lp.topMargin
                    + " plate=" + previewPlate.getWidth() + "x" + previewPlate.getHeight());
        }
    }

    // ------------------------------------------------------------------
    // 呼吸光晕：选中那位数字自己发光（不是垫一块色板，是字形模糊两遍画在字底下）
    // ------------------------------------------------------------------

    private static final long GLOW_PERIOD_MS = 2000L;

    /** 展开后稍等一下再点亮（等数字落位与下划线画好）。 */
    private void startGlowLoop() {
        if (!mDigitsExpanded || mGlowView == null || mGlowDrawable == null) {
            return;
        }
        if (mGlowAnim != null) {
            return;              // 已经在呼吸：换位/换色只需重新定位，别打断节拍
        }
        // 暗端 0.06 会让光晕整段熄灭再亮回来，真机上读成「闪烁」不是呼吸（vc111 反馈）；
        // 0.30 起步既保留明显的明暗差，又保证任何时刻光晕都在、数字始终像在发光。
        // 谷底 0.42 → 峰值 1.0：雾在 0.30 时会淡到看不出形状，抬到 0.42 才既有呼吸又不散架。
        final ValueAnimator anim = ValueAnimator.ofFloat(0.42f, 1f);
        anim.setDuration(GLOW_PERIOD_MS);
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator());
        anim.addUpdateListener(a -> {
            final float v = (float) a.getAnimatedValue();
            if (BuildConfig.DEBUG && (mGlowProbe++ % 30L) == 0L) {
                android.util.Log.d("CS-GLOW", "breath tick v=" + v);
            }
            if (mGlowDrawable != null) {
                mGlowDrawable.setBreath(v);
            }
        });
        mGlowAnim = anim;
        if (BuildConfig.DEBUG) {
            android.util.Log.d("CS-GLOW", "glowLoop start floor=0.42 period=" + GLOW_PERIOD_MS);
        }
        anim.start();
    }

    private void stopGlowLoop() {
        if (BuildConfig.DEBUG) {
            android.util.Log.d("CS-GLOW", "glowLoop stop anim=" + (mGlowAnim != null));
        }
        if (mGlowAnim != null) {
            mGlowAnim.cancel();
            mGlowAnim = null;
        }
        if (mGlowView != null) {
            mGlowView.setVisibility(View.GONE);
        }
    }

    /**
     * 把光晕摆到选中那位的字形上（跟着字号、字距、选中位一起动）。
     *
     * <p>光晕 View 的大小 = 字形墨迹 + 两侧留白，留白 = {@link DigitGlowDrawable#pad(float)}，
     * 不然模糊出来的光会被自己的 bounds 裁掉。</p>
     */
    private void positionGlow() {
        if (mGlowView == null || mGlowDrawable == null || previewView == null
                || previewPlate == null || !mDigitsExpanded) {
            return;
        }
        if (!measureSelInk() || !glowAllowed()) {
            // 跟随系统色、或数字颜色开关关着：都不该有光晕（用户明确要求）。
            mGlowView.setVisibility(View.GONE);
            return;
        }
        final CharSequence text = previewView.getText();
        final int idx = indexOfSlot(mSelSlot);
        final String glyph = (text == null || idx < 0 || idx >= text.length())
                ? "" : String.valueOf(text.charAt(idx));
        final float size = previewView.getTextSize();
        final float pad = DigitGlowDrawable.pad(size);
        // 光晕外框尺寸与字形无关：否则「1」这种窄字光晕只有宽字的一半大，
        // 换一位就换个大小，读成「每位的光晕都不一样」（vc112 真机反馈）。
        // 基准取 max(墨迹宽, 0.62 字号) —— 等宽数字的步进框大约就是 0.6 字号。
        final float baseW = Math.max(mSelInk.width(), size * 0.62f);
        final float baseH = Math.max(mSelInk.height(), size * 0.86f);
        final int w = Math.max(1, Math.round(baseW + pad * 2f));
        final int h = Math.max(1, Math.round(baseH + pad * 2f));
        final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mGlowView.getLayoutParams();
        lp.width = w;
        lp.height = h;
        // 光晕跟着数字一起抬高：LiftSpan 只改绘制基线、墨迹框是「未抬高」的，
        // 这里必须补上抬起量，否则数字抬上去、光晕留在下面，两者错开半个字。
        // 下划线则相反 —— 用户明确要求「线保持原来位置，不用也抬高」（vc112），
        // 所以 positionMark() 不补这个偏移，两者分工不同。
        lp.leftMargin = Math.round(mSelInk.centerX() - w / 2f) - previewPlate.getPaddingLeft();
        lp.topMargin = Math.round(mSelInk.centerY() - h / 2f) - previewPlate.getPaddingTop()
                - mLiftNow;
        mGlowView.setLayoutParams(lp);
        mGlowDrawable.setGlyph(glyph, size, previewView.getTypeface());
        if (BuildConfig.DEBUG) {
            final int[] pv = new int[2];
            final int[] gp = new int[2];
            final int[] pl = new int[2];
            previewView.getLocationOnScreen(pv);
            mGlowView.getLocationOnScreen(gp);
            previewPlate.getLocationOnScreen(pl);
            android.util.Log.d("CS-GLOW", "slot=" + mSelSlot + " idx=" + idx + " glyph=[" + glyph + "]"
                    + " ink=" + mSelInk + " size=" + size + " w=" + w + " h=" + h
                    + " cx=" + Math.round(mSelInk.centerX()) + " cy=" + Math.round(mSelInk.centerY())
                    + " glowBox=" + w + "x" + h + "@" + Math.round(mSelInk.centerX() - w / 2f)
                    + "," + Math.round(mSelInk.centerY() - h / 2f)
                    + " lp=" + lp.leftMargin + "," + lp.topMargin + " lift=" + mLiftNow
                    + " pv=" + pv[0] + "," + pv[1] + " plate=" + pl[0] + "," + pl[1]
                    + " glow=" + gp[0] + "," + gp[1]
                    + " padL=" + previewPlate.getPaddingLeft() + " padT=" + previewPlate.getPaddingTop());
        }
        // 光晕配色（浅色/深色两套完全不同的读法）：
        //
        // 深色模式：背景是深紫，光晕要「比背景亮」，所以取数字色并保证够亮，
        //           靠亮度差说「这个数字在发光」。
        // 浅色模式：背景是白卡，亮度已经到顶 —— 之前把光晕提亮到 0.55 亮度，
        //           结果光晕和白底几乎同色，真机看着就是「没光晕」。这里改成
        //           「数字本色往外晕开一圈」：颜色用数字自己的深色（不做任何提亮），
        //           半径与透明度由 DigitGlowDrawable.setLightTheme 那套参数负责。
        // 浅色下光晕 = 数字色**再提亮一档**：数字本身已经被推亮到 0.72，
        // 光晕要更亮才像「光从数字里透出来」；同色才不会被看成两种颜色。
        // vc145：光晕色 = 数字色**原样**，任何主题下都不做提亮。
        //
        // 起因：浅色模式下「数字提亮到 0.72 + 光晕提到 0.92」这套，对明度已经顶到头的
        // 纯色（纯黄 #FFFF00、纯青、纯洋红）根本不成立 —— 混白只会把颜色冲淡，
        // 之前试图靠挪色相补救，被用户判为「颜色不一样，误导用户」。所以浅色模式索性
        // 两种提亮全砍：数字和光晕都用用户选的同一个颜色，靠**光晕的量和半径**（见
        // DigitGlowDrawable 的 LIGHT_GAIN / 半径）来补可见度。颜色零偏差是硬要求，
        // 亮度可以妥协。
        final int glowBase = adaptPreviewColor(slotColor(mSelSlot));
        mGlowDrawable.setLightTheme(!night);
        mGlowDrawable.setColor(glowBase);
        mGlowView.setVisibility(View.VISIBLE);
    }

    /**
     * 选中那位该不该亮光晕。
     *
     * <p>两种情况不亮：①「数字颜色」总开关关着（此时整卡只是普通时间，没有逐位取色可言）；
     * ②这一位是「跟随系统」——它的颜色由系统主题决定，发光会跟状态栏对不上，用户不要。</p>
     */
    private boolean glowAllowed() {
        if (!ClockSecondsConfig.isHourColorEnabled(getContentResolver())) {
            return false;
        }
        return slotColor(mSelSlot) != ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
    }

    /** 预览文本里第 slot 位数字的字符下标（预览是实时文本，不能缓存）。 */
    private int indexOfSlot(int slot) {
        final CharSequence text = previewView == null ? null : previewView.getText();
        if (text == null) {
            return -1;
        }
        for (int i = 0; i < text.length(); i++) {
            if (ClockSecondsConfig.slotOfChar(text, i) == slot) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 预览里的颜色按背景做对比度补偿，保证「预览看到的 = 状态栏显示的」。
     *
     * <p>浅底：见下方浅色分支——直接返回原色，不做任何提亮或压暗（vc145）。</p>
     *
     * <p>深底：深色数字（如深绿 #183419）几乎和卡片融成一片，光晕也就没东西可发光 →
     * 向白色混合到亮度 0.30 以上，色相保留、明度提上来（vc66 修）。</p>
     */
    private int adaptPreviewColor(int color) {
        if (color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
            return text();
        }
        final int system = text();
        if (Color.luminance(system) >= 0.5f) {      // 深色主题（浅字深底）：提亮到看得见
            return brightenForDark(color, 0.32f);
        }
        // 浅色主题（白卡）：直接返回**用户选的原色**。
        //
        // 这里前后试过三种做法，全都因为「颜色变了」被否：
        //   vc66 起 各通道 ×0.42 压暗 → 亮色在白底看得清了，但数字变深棕；
        //   vc143  提亮到亮度 0.72 → 纯黄这类色相明度已到顶，只好挪 22° 色相补救，
        //          结果选的黄显示成琥珀橙（用户：「颜色不一样，误导用户」）；
        //   vc144~ 原色不动。
        // 结论：预览卡是「我选的颜色长什么样」的展示窗，不是调色板，任何自动改色都是错的。
        // 白底上确实不好看，但那是用户自己选的颜色，必须如实显示。
        return color;
    }


    /**
     * 深色底上把过暗的颜色提亮：优先**抬高 HSV 明度**（保住色相与饱和度，绿还是绿），
     * 明度顶到头还达不到目标亮度时，再向白色混一点（很暗的颜色只能这样）。
     *
     * <p>vc66 第一版直接向白混，深绿 #183419 会被冲成橄榄绿；vc67 改成先提明度。</p>
     */
    private static int brightenForDark(int color, float targetLum) {
        if (Color.luminance(color) >= targetLum) {
            return color;
        }
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        int best = color;
        for (int i = 0; i < 24 && hsv[2] < 1f; i++) {
            hsv[2] = Math.min(1f, hsv[2] * 1.22f + 0.015f);
            best = Color.HSVToColor(hsv);
            if (Color.luminance(best) >= targetLum) {
                return best;
            }
        }
        final float lum = Color.luminance(best);
        float t = (targetLum - lum) / (1f - Math.min(0.999f, lum));
        t = Math.min(0.75f, Math.max(0f, t));
        return blendColor(best, 0xFFFFFFFF, t);
    }

    /** 选中位被抬起的高度：向上移动一点点，一眼看出「正在编辑这一位」。 */
    private int liftPx() {
        return dp(10);
    }

    /** 只抬高绘制基线、不参与排版测量的 span（用 baselineShift，行高不变）。 */
    private static final class LiftSpan extends android.text.style.CharacterStyle
            implements android.text.style.UpdateAppearance {
        private final int mLift;
        LiftSpan(int lift) {
            mLift = lift;
        }
        @Override
        public void updateDrawState(android.text.TextPaint tp) {
            // 注意方向：这条绘制路径上 baselineShift 取正值是往下走，取负值才是抬高（真机实测）。
            tp.baselineShift -= mLift;
        }
    }

    /**
     * 未选中的位变淡。
     *
     * <p>浅底：保留色相只降不透明度（35%），看起来就是变浅。
     * 深底：半透明叠在深色卡片上等于「直接消失」（vc65 实测），改成向卡片底色混 58%，
     * 既保留色相又能看出这是「同一行里的弱化位」。</p>
     */
    private int dimColor(int color) {
        if (night) {
            return blendColor(color, DARK_CARD, 0.58f);
        }
        // 浅色主题的问题和深色相反：选中位现在被提亮到接近白卡，未选中位如果只降到 35%
        // 不透明度，两者在亮度上会撞在一起，「哪一位被选中」反而看不出来。
        // 这里改成向**深色方向**跨一步（混 62% 的系统文字色），一眼能分出「亮的那位是选中的」。
        return blendColor(color, text(), 0.62f);
    }

    /** 给「正在编辑的那一位」设色（-1 = 跟随系统），写 Secure 设置并由 SystemUI 立即生效。 */
    private void applyDigitColor(final int slot, final int color) {
        if (slot < 0 || slot >= ClockSecondsConfig.DIGIT_COUNT) {
            return;
        }
        Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_DIGIT_COLOR)
                .putExtra(ClockSecondsConfig.EXTRA_SLOT, slot)
                .putExtra(ClockSecondsConfig.EXTRA_COLOR, color);
        applyConfig(ClockSecondsConfig.DIGIT_COLOR_KEYS[slot], color, intent, ok -> {
            if (!ok) {
                toastWriteFailed();
                return;
            }
            mCfgDigitColors = ClockSecondsConfig.getDigitColors(getContentResolver());
            // 取色即视为启用：否则选了颜色看不到效果（开关只负责整体启停）。
            if (!mCfgHourColorOn && mDigitSwitch != null && !mDigitSwitch.isChecked()) {
                mDigitSwitch.setChecked(true);   // 监听里会写入启用状态并刷新界面
            }
            refreshDigitSwatches();
            refreshColorSummary();
            updatePreview();            // 字色 / 下划线 / 水滴颜色一起跟着变
            if (previewView != null) {
                previewView.post(this::positionMark);
            }
        });
    }

    /** 刷新预设色块的选中环 + 「正在编辑」行 + 底部摘要。 */
    private void refreshDigitSwatches() {
        final boolean enabled = mCfgHourColorOn;
        final int current = slotColor(mSelSlot);
        if (mDigitGrid != null) {
            for (int i = 0; i < mDigitGrid.getChildCount() && i < HOUR_COLORS.length; i++) {
                final View child = mDigitGrid.getChildAt(i);
                child.setClickable(enabled);
                final boolean selected = HOUR_COLORS[i] == current;
                child.setBackground(swatchDrawable(swatchColor(HOUR_COLORS[i], enabled), selected));
                child.setSelected(selected);
            }
        }
        if (mCornerArcs != null) {
            mCornerArcs.setAlpha(255);
        }
        if (mDigitEditingView != null) {
            mDigitEditingView.setText(getString(R.string.settings_digit_editing, slotName(mSelSlot)));
        }
        if (mDigitSummaryView != null) {
            mDigitSummaryView.setText(getString(R.string.settings_digit_summary,
                    fieldLabel(0, 1), fieldLabel(2, 3), fieldLabel(4, 5)));
        }
    }

    /** 某一位当前的颜色（-1 = 跟随系统）。 */
    private int slotColor(int slot) {
        if (mCfgDigitColors == null || slot < 0 || slot >= mCfgDigitColors.length) {
            return ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
        }
        return mCfgDigitColors[slot];
    }

    /** 位名（时十/时个/分十/分个/秒十/秒个），随语言走 settings_digit_slots 的逗号分隔列表。 */
    private String slotName(int slot) {
        final String[] parts = getString(R.string.settings_digit_slots).split(",");
        if (slot >= 0 && slot < parts.length && parts[slot].trim().length() > 0) {
            return parts[slot].trim();
        }
        return ClockSecondsConfig.DIGIT_LABELS[
                Math.max(0, Math.min(ClockSecondsConfig.DIGIT_LABELS.length - 1, slot))];
    }

    /** 单个字段（时/分/秒）的摘要文案：两位数同色显示一个色号，不同色显示 a/b。 */
    private String fieldLabel(int a, int b) {
        final String left = colorLabel(slotColor(a));
        final String right = colorLabel(slotColor(b));
        return left.equals(right) ? left : left + "/" + right;
    }

    private String colorLabel(int color) {
        return color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW
                ? getString(R.string.settings_digit_follow) : hexOf(color);
    }

    /** 调色盘：自绘 HSV 面板 + 色相条，确定后立即生效。 */
    private void showPaletteDialog(final int slot) {
        final LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(18), dp(14), dp(18), dp(2));

        final HsvPickerView panel = new HsvPickerView(this, HsvPickerView.MODE_SV);
        panel.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(170)));
        final HsvPickerView hueBar = new HsvPickerView(this, HsvPickerView.MODE_HUE);
        LinearLayout.LayoutParams hueLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(26));
        hueLp.topMargin = dp(14);
        hueBar.setLayoutParams(hueLp);
        box.addView(panel);
        box.addView(hueBar);

        final TextView hex = new TextView(this);
        hex.setTextSize(13f);
        hex.setTextColor(text2());
        hex.setPadding(0, dp(12), 0, 0);
        box.addView(hex);

        // 起始色：该位当前色；跟随系统时从默认红起步。
        int start = slotColor(slot);
        if (start == ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
            start = HOUR_COLORS[0];
        }
        panel.setColor(start);
        hueBar.setColor(start);
        final Runnable syncHex = () -> hex.setText(swatchDesc(panel.getColor()));
        panel.setOnColorChanged(c -> syncHex.run());
        hueBar.setOnColorChanged(c -> {
            panel.setHue(hueBar.getHue());
            syncHex.run();
        });
        syncHex.run();

        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_digit_palette)
                .setView(box)
                .setNegativeButton(R.string.settings_cancel, null)
                .setPositiveButton(R.string.settings_rule_apply,
                        (dialog, which) -> applyDigitColor(slot, panel.getColor()))
                .show();
    }

    /**
     * 测试卡片（诊断）：列出模块检测到的候选时钟，逐个勾选，找出「唯独哪个时钟让状态栏显示秒」。
     * 取消勾选的时钟模块不再接管（不显秒、不标红），可直接关掉通知栏/锁屏等误命中的时钟。
     */
    private void buildClockTestCard(LinearLayout root) {
        // 本卡片屏蔽整卡按压：只有「刷新列表」按钮保留按压反馈。
        LinearLayout card = cardNoPress();

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText("时钟探测（测试）");
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        title.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        titleRow.addView(title);

        // 刷新按钮：让 SystemUI 重新扫描并发布候选清单，再重读刷新（显示「刷新中/刷新完成」提示）。
        TextView refresh = new TextView(this);
        refresh.setText("刷新列表");
        refresh.setTextSize(12f);
        refresh.setTextColor(accent());
        refresh.setPadding(dp(10), dp(6), dp(10), dp(6));
        GradientDrawable rbg = new GradientDrawable();
        rbg.setCornerRadius(dp(999));
        rbg.setColor(night ? 0x33A98CFF : 0x336C4CF1);
        refresh.setBackground(rbg);
        mRefreshBtn = refresh;
        // 卡片整体不再按压，仅「刷新列表」按钮保留按压反馈（裁到胶囊圆角，避免高光溢出）。
        refresh.setClipToOutline(true);
        mPressTilt.register(refresh, accent());
        refresh.setOnClickListener(v -> refreshClockList());
        titleRow.addView(refresh);
        card.addView(titleRow);

        TextView sub = new TextView(this);
        sub.setText("模块会自动识别状态栏候选时钟。逐個勾选/取消，观察状态栏与通知栏秒数变化，"
                + "找出真正控制状态栏的那个；通知栏等不需要的取消勾选即可。");
        sub.setTextSize(11f);
        sub.setTextColor(text2());
        sub.setPadding(0, dp(6), 0, 0);
        card.addView(sub);

        mClockTestList = new LinearLayout(this);
        mClockTestList.setOrientation(LinearLayout.VERTICAL);
        mClockTestList.setPadding(0, dp(10), 0, 0);
        card.addView(mClockTestList);

        root.addView(card);
        refreshClockTestList();
    }

    /** 触发 SystemUI 重新扫描并发布候选清单，同时切换按钮文案为「刷新中…」。 */
    private void refreshClockList() {
        mRefreshPending = true;
        if (mRefreshBtn != null) {
            mRefreshBtn.setText("刷新中…");
            mRefreshBtn.setTextColor(accent());   // 复位颜色（防止上次「刷新完成」的红色残留）
        }
        try {
            Intent it = new Intent(ClockSecondsConfig.ACTION_REFRESH_CLOCK_LIST);
            it.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(it);
        } catch (Throwable t) {
            mRefreshPending = false;
            if (mRefreshBtn != null) {
                mRefreshBtn.setText("刷新列表");
            }
            Toast.makeText(this, "刷新失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }
        // 兜底：即便 SystemUI 的结果广播未回（跨进程偶发丢失），也主动重读并收尾，
        // 保证按钮不会一直停在「刷新中…」。
        if (mRefreshBtn != null) {
            mRefreshBtn.postDelayed(this::finishClockRefresh, 900L);
        }
    }

    /** 收尾一次刷新：重读候选清单、按钮显示「刷新完成」，1.5s 后还原「刷新列表」。 */
    private void finishClockRefresh() {
        if (!mRefreshPending) {
            return;
        }
        mRefreshPending = false;
        refreshClockTestList();
        if (mRefreshBtn != null) {
            mRefreshBtn.setText("刷新完成");
            mRefreshBtn.setTextColor(0xFFE53935);   // 红色提醒「已刷新」
            mRefreshBtn.postDelayed(() -> {
                if (mRefreshBtn != null) {
                    mRefreshBtn.setText("刷新列表");
                    mRefreshBtn.setTextColor(accent());  // 恢复强调色
                }
            }, 1500L);
        }
    }

    /** 从 SystemUI 读取候选清单与启用选择，重建列表行。 */
    private void refreshClockTestList() {
        List<Pair<String, String>> list = loadClockTestList();
        mClockEnabled.clear();
        for (Pair<String, String> e : list) {
            mClockEnabled.put(e.first, true);
        }
        Map<String, Boolean> stored = loadClockEnabledMap();
        for (Map.Entry<String, Boolean> e : stored.entrySet()) {
            mClockEnabled.put(e.getKey(), e.getValue());
        }
        mClockTestList.removeAllViews();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("尚未检测到时钟。请先让状态栏/通知栏时间显示出来，再点「刷新列表」。");
            empty.setTextSize(12f);
            empty.setTextColor(text3());
            empty.setPadding(0, dp(4), 0, 0);
            mClockTestList.addView(empty);
            return;
        }
        for (Pair<String, String> e : list) {
            mClockTestList.addView(makeClockTestRow(e.first, e.second));
        }
    }

    /** 一行：标签 + 开关。开关直接写 Secure（有权限）或经广播让 SystemUI 代写。 */
    private LinearLayout makeClockTestRow(final String key, String label) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        TextView tv = new TextView(this);
        tv.setText(label);
        tv.setTextSize(12f);
        tv.setTextColor(text());
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);

        final Switch sw = new Switch(this);
        boolean on = mClockEnabled.getOrDefault(key, true);
        sw.setChecked(on);
        styleSwitch(sw);
        sw.setOnCheckedChangeListener((v, checked) -> applyClockTest(key, checked));
        row.addView(sw);
        return row;
    }

    /** 写入某候选时钟的启用/停用选择（本地合并后写 Secure，无权限则走广播）。 */
    private void applyClockTest(final String key, final boolean enabled) {
        mClockEnabled.put(key, enabled);
        JSONObject obj = new JSONObject();
        try {
            for (Map.Entry<String, Boolean> e : mClockEnabled.entrySet()) {
                obj.put(e.getKey(), e.getValue());
            }
        } catch (Throwable ignored) {
            // 构建 JSON 失败不打断交互
        }
        String json = obj.toString();
        // 始终以广播把「全量启用映射」发给 SystemUI（即便本应用恰好有 WRITE_SECURE_SETTINGS 也发，
        // 不再 return 提前返回），避免 SystemUI 未收到通知、或只收到单键而丢失其它勾选状态。
        if (BuildConfig.DEBUG) {
            android.util.Log.d("ClockSeconds",
                    "applyClockTest key=" + key + " enabled=" + enabled + " map=" + json);
        }
        try {
            Intent it = new Intent(ClockSecondsConfig.ACTION_SET_CLOCK_TEST)
                    .putExtra(ClockSecondsConfig.EXTRA_CLOCK_KEY, key)
                    .putExtra(ClockSecondsConfig.EXTRA_CLOCK_ENABLED, enabled ? 1 : 0)
                    .putExtra(ClockSecondsConfig.EXTRA_CLOCK_ENABLED_MAP, json);
            it.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(it);
        } catch (Throwable t) {
            Toast.makeText(this, "切换失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /** 读取候选时钟清单：Secure 中的 JSON 数组 [{"k":key,"l":label}]。 */
    private List<Pair<String, String>> loadClockTestList() {
        List<Pair<String, String>> out = new ArrayList<>();
        try {
            String s = Settings.Secure.getString(getContentResolver(),
                    ClockSecondsConfig.CLOCK_TEST_LIST_KEY);
            if (s != null && !s.isEmpty()) {
                JSONArray arr = new JSONArray(s);
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    out.add(new Pair<>(o.getString("k"), o.getString("l")));
                }
            }
        } catch (Throwable ignored) {
            // 读取失败按空列表处理
        }
        return out;
    }

    /** 读取用户对候选时钟的启用选择：Secure 中的 JSON 对象 {key:bool}。 */
    private Map<String, Boolean> loadClockEnabledMap() {
        Map<String, Boolean> m = new HashMap<>();
        try {
            String s = Settings.Secure.getString(getContentResolver(),
                    ClockSecondsConfig.CLOCK_TEST_ENABLED_KEY);
            if (s != null && !s.isEmpty()) {
                JSONObject o = new JSONObject(s);
                Iterator<String> it = o.keys();
                while (it.hasNext()) {
                    String k = it.next();
                    m.put(k, o.getBoolean(k));
                }
            }
        } catch (Throwable ignored) {
            // 读取失败按全启用处理
        }
        return m;
    }

    // ==================================================================
    // 诊断卡片：一键重启 SystemUI + 一次性全量诊断（无需翻 logcat）
    // ==================================================================

    private void buildDiagnosticCard(LinearLayout root) {
        // 本卡片屏蔽整卡按压：只有「重启 SystemUI / 运行诊断」按钮保留按压反馈。
        LinearLayout card = cardNoPress();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView title = new TextView(this);
        title.setText("诊断与工具");
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        card.addView(title);

        TextView hint = new TextView(this);
        hint.setText("一键诊断可一次性定位「时钟未识别 / 边界算错 / 触摸被拦截」；重启 SystemUI 免 adb 重新加载模块。"
                + "诊断前先让状态栏时钟显示出来，最好点一下状态栏再点「运行诊断」。");
        hint.setTextSize(11f);
        hint.setTextColor(text2());
        hint.setPadding(0, dp(4), 0, dp(10));
        hint.setLineSpacing(dp(2), 1f);
        card.addView(hint);

        // 按钮行：重启 / 诊断。
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView restartBtn = pillButton("重启 SystemUI");
        restartBtn.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f) {{
            setMarginEnd(dp(8));
        }});
        restartBtn.setOnClickListener(v -> {
            // 记录重启前的心跳时间戳，用于重启后确认模块真的重新加载了。
            long beforeTs = ClockSecondsConfig.parseActiveTs(
                    ClockSecondsConfig.readActiveHeartbeat(getContentResolver()));
            try {
                Intent it = new Intent(ClockSecondsConfig.ACTION_RESTART_SYSTEMUI);
                it.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
                sendBroadcast(it);
                Toast.makeText(this, "正在重启 SystemUI…", Toast.LENGTH_SHORT).show();
            } catch (Throwable t) {
                Toast.makeText(this, "重启失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            pollRestartConfirm(beforeTs, 0);
        });
        row.addView(restartBtn);
        restartBtn.setClipToOutline(true);
        mPressTilt.register(restartBtn, 0xFFFFFFFF);   // 实心强调色底：白色高光才有反馈

        TextView diagBtn = pillButton("运行诊断");
        diagBtn.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        diagBtn.setOnClickListener(v -> {
            mDiagPending = true;
            if (mDiagView != null) {
                mDiagView.setText("诊断中…（让状态栏时钟显示出来后，点一下状态栏再点这里）");
            }
            // 超时兜底：模块未就绪时避免一直停在「诊断中…」。
            mPingHandler.removeCallbacks(mDiagTimeout);
            mPingHandler.postDelayed(mDiagTimeout, 5000L);
            try {
                Intent it = new Intent(ClockSecondsConfig.ACTION_RUN_DIAGNOSTIC);
                it.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
                sendBroadcast(it);
            } catch (Throwable t) {
                mDiagPending = false;
                mPingHandler.removeCallbacks(mDiagTimeout);
                Toast.makeText(this, "诊断失败：" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        row.addView(diagBtn);
        diagBtn.setClipToOutline(true);
        mPressTilt.register(diagBtn, 0xFFFFFFFF);   // 实心强调色底：白色高光才有反馈
        card.addView(row);

        // 更新后一键生效：重启 SystemUI（模块新代码）+ 重开本应用（界面新代码），免手动两步。
        TextView fullRestartBtn = new TextView(this);
        fullRestartBtn.setText("更新后一键生效（重启系统界面 + 重开应用）");
        fullRestartBtn.setTextSize(12f);
        fullRestartBtn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        fullRestartBtn.setTextColor(accent());
        fullRestartBtn.setGravity(Gravity.CENTER);
        fullRestartBtn.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable frBg = new GradientDrawable();
        frBg.setCornerRadius(dp(999));
        frBg.setColor(night ? 0x33A98CFF : 0x336C4CF1);
        fullRestartBtn.setBackground(frBg);
        fullRestartBtn.setClickable(true);
        fullRestartBtn.setClipToOutline(true);
        mPressTilt.register(fullRestartBtn, accent());
        LinearLayout.LayoutParams frLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        frLp.topMargin = dp(10);
        fullRestartBtn.setLayoutParams(frLp);
        fullRestartBtn.setOnClickListener(v -> showFullRestartDialog());
        card.addView(fullRestartBtn);

        mDiagView = new TextView(this);
        mDiagView.setTextSize(11f);
        mDiagView.setTypeface(Typeface.create("monospace", Typeface.NORMAL));
        mDiagView.setTextColor(text2());
        mDiagView.setPadding(dp(8), dp(10), dp(8), dp(4));
        GradientDrawable diagBg = new GradientDrawable();
        diagBg.setCornerRadius(dp(10));
        diagBg.setColor(night ? 0xFF1A1A22 : 0xFFF2F2F7);
        mDiagView.setBackground(diagBg);
        mDiagView.setText("点「运行诊断」查看结果。");
        card.addView(mDiagView);

        root.addView(card);
    }

    // ==================================================================
    // 激活状态卡片：一键探测模块是否已注入 SystemUI 并正在运行
    // ==================================================================

    private void buildActivationCard(LinearLayout root) {
        LinearLayout card = card();
        card.setPadding(dp(18), dp(14), dp(18), dp(14));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        mActiveDot = new View(this);
        int dotSize = dp(12);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(0xFFFFB300);
        dot.setSize(dotSize, dotSize);
        mActiveDot.setBackground(dot);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dotSize, dotSize);
        dotLp.setMarginEnd(dp(10));
        mActiveDot.setLayoutParams(dotLp);
        row.addView(mActiveDot);

        mActiveText = new TextView(this);
        mActiveText.setTextSize(13f);
        mActiveText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mActiveText.setTextColor(text());
        mActiveText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        mActiveText.setText("正在检测模块状态…");
        row.addView(mActiveText);

        TextView recheck = pillButton("重新检测");
        recheck.setTextSize(12f);
        recheck.setPadding(dp(14), dp(6), dp(14), dp(6));
        recheck.setOnClickListener(v -> pingModule());
        row.addView(recheck);

        card.addView(row);
        root.addView(card);
        mActivationCard = card;
        // 默认隐藏：已激活时不占位置；只有真检测出未激活，setActiveState 才把它放出来。
        card.setVisibility(View.GONE);
        setActiveState(ACTIVE_CHECKING, null, false);
    }

    /** 向 SystemUI 发 PING；2.5s 内未收到 PONG 即判未激活。 */
    private void pingModule() {
        setActiveState(ACTIVE_CHECKING, null, false);
        mPingHandler.removeCallbacks(mPingTimeout);
        try {
            Intent it = new Intent(ClockSecondsConfig.ACTION_PING_MODULE);
            it.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(it);
        } catch (Throwable t) {
            setActiveState(ACTIVE_NO, null, false);
            return;
        }
        mPingHandler.postDelayed(mPingTimeout, 2500);
    }

    /** 收到 SystemUI 的 PONG：模块确实在运行。 */
    private void onModulePong(String version, boolean secondsOn) {
        mPingHandler.removeCallbacks(mPingTimeout);
        setActiveState(ACTIVE_YES, version, secondsOn);
    }

    private void setActiveState(int state, String version, boolean secondsOn) {
        int color;
        String msg;
        if (state == ACTIVE_YES) {
            color = 0xFF4CAF50;
            StringBuilder sb = new StringBuilder("模块已激活");
            if (version != null && !version.isEmpty()) {
                sb.append(" · v").append(version);
            }
            sb.append("（显秒").append(secondsOn ? "开" : "关").append("）");
            msg = sb.toString();
        } else if (state == ACTIVE_NO) {
            color = 0xFFE53935;
            msg = "未激活（请在 LSPosed 勾选「系统界面」后重启）";
        } else {
            color = 0xFFFFB300;
            msg = "正在检测模块状态…";
        }
        if (mActiveDot != null && mActiveDot.getBackground() instanceof GradientDrawable) {
            ((GradientDrawable) mActiveDot.getBackground()).setColor(color);
        }
        if (mActiveText != null) {
            mActiveText.setText(msg);
        }
        // 已激活 → 整张卡收掉（用户明确要求：激活了就别显示）；未激活 → 才提示。
        if (mActivationCard != null) {
            if (state == ACTIVE_YES || state == ACTIVE_NO) {
                final int want = (state == ACTIVE_YES) ? View.GONE : View.VISIBLE;
                if (mActivationCard.getVisibility() != want) {
                    beginSmoothLayout();
                    mActivationCard.setVisibility(want);
                }
            }
        }
        // DEBUG 构建把本地判定结果打到 logcat，便于 adb 读取验证 PONG 是否真正送达应用
        // （uiautomator dump 因持续呼吸灯动画无法稳定抓帧，改用 logcat 信号更可靠）。
        if (BuildConfig.DEBUG) {
            String tag = "ClockSeconds";
            String code = (state == ACTIVE_YES) ? "ACTIVE_YES" : (state == ACTIVE_NO) ? "ACTIVE_NO" : "ACTIVE_CHECKING";
            android.util.Log.d(tag, "[activation] state=" + code + " msg=" + msg
                    + " ver=" + (version != null ? version : "") + " secondsOn=" + secondsOn);
        }
    }

    /** 统一药丸按钮样式（同「自动检测」主按钮）。 */
    private TextView pillButton(String text) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextSize(13f);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setTextColor(0xFFFFFFFF);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(10), dp(14), dp(10));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(999));
        bg.setColor(accent());
        b.setBackground(bg);
        b.setClickable(true);
        return b;
    }

    /** 把 SystemUI 回传的诊断 JSON 渲染成易读文本。 */
    private void renderDiagnostic(String json) {
        if (mDiagView == null) {
            return;
        }
        try {
            JSONObject d = new JSONObject(json);
            StringBuilder sb = new StringBuilder();
            String doneTime = new java.text.SimpleDateFormat("HH:mm:ss",
                    java.util.Locale.getDefault()).format(new java.util.Date());
            sb.append("✅ 诊断完成 ").append(doneTime).append("\n\n");

            int n = d.optInt("candidateCount", 0);
            int sbLaid = d.optInt("statusBarLaidCount", 0);
            JSONObject pick = d.optJSONObject("pick");
            boolean pickedEmpty = pick == null || pick.optBoolean("pickedEmpty", true);
            boolean lastHit = d.optBoolean("lastHit", false);
            boolean secondsOn = d.optBoolean("secondsEnabled", false);
            int gesture = d.optInt("gesture", ClockSecondsConfig.GESTURE_CLICK);

            // 一句话结论（尽量说人话，不堆术语）
            String conclusion;
            if (n == 0) {
                conclusion = "没有检测到任何时钟，模块可能没生效。请确认 LSPosed 里已启用本模块并勾选"
                        + "「系统界面」，然后重启 SystemUI。";
            } else if (sbLaid == 0) {
                conclusion = "检测到时间控件，但没认出哪个是状态栏时钟。可到「时钟识别」点「自动检测」试试。";
            } else if (pickedEmpty) {
                conclusion = "认出了状态栏时钟，但它现在没有位置信息（可能还没显示出来）。"
                        + "先下拉一次通知栏，再点「运行诊断」。";
            } else if (!lastHit) {
                conclusion = "状态栏时钟识别正常，但上次点击没落在时钟上。点时间时请按准「时:分」数字区域。";
            } else {
                conclusion = "一切正常：状态栏时钟已识别，点击能命中。";
            }
            sb.append("【结论】\n").append(conclusion).append("\n\n");

            // 状态栏时钟概况
            sb.append("【状态栏时钟】\n");
            sb.append("识别到 ").append(n).append(" 个时间控件，其中状态栏 ")
                    .append(sbLaid).append(" 个。\n");
            if (pick != null && !pickedEmpty) {
                String label = pick.optString("pickedLabel", "");
                if (!label.isEmpty() && !"null".equals(label)) {
                    sb.append("当前接管：").append(label).append("\n");
                }
                String rectPlain = plainRect(pick.optString("rect", ""));
                if (!rectPlain.isEmpty()) {
                    sb.append("点击有效范围：").append(rectPlain).append("\n");
                }
            }
            sb.append("点击手势：")
                    .append(gesture == ClockSecondsConfig.GESTURE_CLICK ? "单击" : "双击").append("\n");
            sb.append("显秒：").append(secondsOn ? "已开启" : "已关闭").append("\n\n");

            // 颜色体检：直接判断小时颜色是否还在（ColorOS 17 掉色可一眼看出，无需抓日志）
            JSONObject cc = d.optJSONObject("colorCheck");
            if (cc != null) {
                sb.append("【颜色体检】\n");
                String st = cc.optString("state", "");
                String msg;
                if ("ok".equals(st)) {
                    msg = "颜色正常 ✓（十位数带色 " + colorHex(cc.optInt("spanColor", -1)) + "）";
                } else if ("stripped".equals(st)) {
                    msg = "⚠ 颜色被系统洗掉了（掉色）：文本已变成纯文本，颜色丢失";
                } else if ("no_span".equals(st)) {
                    msg = "⚠ 颜色丢失：文本还在，但颜色标记被去掉了（掉色）";
                } else if ("disabled".equals(st)) {
                    msg = "该时钟已在测试卡里关闭，不接管、不上色（正常）";
                } else if ("hour_color_off".equals(st)) {
                    msg = "小时颜色总开关已关闭，不上色（正常）";
                } else if ("no_clock".equals(st)) {
                    msg = "暂未识别到状态栏时钟，无法检测颜色";
                } else {
                    msg = "未知状态：" + st;
                }
                sb.append(msg).append("\n");
                if (cc.optBoolean("applicable", false)) {
                    String txt = cc.optString("text", "");
                    if (!txt.isEmpty()) {
                        sb.append("当前文本：").append(txt).append("\n");
                    }
                }
                sb.append("\n");
            }

            // 上次点击（含时间，时分秒）
            sb.append("【上次点击】\n");
            long t = d.optLong("lastTouchTime", 0L);
            if (t > 0) {
                String touchTime = new java.text.SimpleDateFormat("HH:mm:ss",
                        java.util.Locale.getDefault()).format(new java.util.Date(t));
                sb.append("时间：").append(touchTime).append("\n");
            } else {
                sb.append("时间：暂无（还没点过状态栏时间）\n");
            }
            int tx = d.optInt("lastTouchX", -1), ty = d.optInt("lastTouchY", -1);
            if (tx >= 0 && ty >= 0) {
                sb.append("位置：(").append(tx).append(", ").append(ty).append(")\n");
            }
            sb.append("是否点中时钟：").append(lastHit ? "是 ✓" : "否 ✗（没按到时钟上）").append("\n\n");

            // 识别到的时钟清单（简要，供普通用户判断哪个是状态栏时钟）
            JSONArray arr = d.optJSONArray("candidates");
            if (arr != null && arr.length() > 0) {
                sb.append("【识别到的时钟】\n");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject o = arr.getJSONObject(i);
                    sb.append(i + 1).append(". ").append(o.optString("label", "未知"));
                    if (o.optBoolean("isStatusBar")) {
                        sb.append("（状态栏）");
                    }
                    sb.append(o.optBoolean("enabled") ? " — 已启用" : " — 已关闭").append("\n");
                }
            }
            mDiagView.setText(sb.toString());
        } catch (Throwable t) {
            mDiagView.setText("解析失败：" + t + "\n" + json);
        }
    }

    /** 把命中区 "57,22,278,126" / "Rect(57, 22 - 278, 126)" 转成「横向 a~b，纵向 c~d」。 */
    private static String plainRect(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        try {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("-?\\d+").matcher(s);
            java.util.List<Integer> nums = new java.util.ArrayList<>();
            while (m.find() && nums.size() < 4) {
                nums.add(Integer.parseInt(m.group()));
            }
            if (nums.size() == 4) {
                return "横向 " + nums.get(0) + "~" + nums.get(2)
                        + "，纵向 " + nums.get(1) + "~" + nums.get(3);
            }
        } catch (Throwable ignored) {
            // 解析失败则原样返回
        }
        return s;
    }

    /** ARGB int → "#AARRGGBB"。 */
    private static String colorHex(int c) {
        return String.format(java.util.Locale.US, "#%08X", c);
    }

    /**
     * 重启后轮询心跳：SystemUI 重新加载模块会写新的心跳时间戳，据此确认「真的重启了」。
     * 最多等约 12 秒；成功则提示并顺带刷新激活状态。
     */
    private void pollRestartConfirm(final long beforeTs, final int attempt) {
        mPingHandler.postDelayed(() -> {
            long now = ClockSecondsConfig.parseActiveTs(
                    ClockSecondsConfig.readActiveHeartbeat(getContentResolver()));
            if (now > beforeTs) {
                Toast.makeText(this, "SystemUI 已重启 ✓", Toast.LENGTH_SHORT).show();
                pingModule();
            } else if (attempt < 12) {
                pollRestartConfirm(beforeTs, attempt + 1);
            } else {
                Toast.makeText(this, "重启未确认，请稍候查看激活状态，或手动重启 SystemUI",
                        Toast.LENGTH_LONG).show();
            }
        }, 1000L);
    }

    /**
     * 「更新后一键生效」：确认后重启 SystemUI（让模块新代码生效）并重开本应用（让界面新代码生效）。
     * 仅在升级安装新版本后需要；平时改设置/开关时钟无需重启。
     */
    private void showFullRestartDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("更新后一键生效")
                .setMessage("将重启系统界面（让模块新代码生效）并重新打开本应用（让界面新代码生效）。\n\n"
                        + "仅升级安装新版本后需要；平时改设置、开关时钟、刷新、诊断都无需重启。\n\n"
                        + "⚠ 请勿连续多次使用：频繁强杀 SystemUI 会让 LSPosed 判定「崩溃过多」进入安全模式，"
                        + "届时所有模块会被停用，需重启手机才能恢复。")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定重启", (d, w) -> {
                    try {
                        Intent it = new Intent(ClockSecondsConfig.ACTION_RESTART_SYSTEMUI);
                        it.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
                        sendBroadcast(it);
                    } catch (Throwable ignored) {
                        // 广播失败也继续重开应用
                    }
                    Toast.makeText(this, "正在重启系统界面并重开应用…", Toast.LENGTH_SHORT).show();
                    // 稍等再重开应用，避免与系统界面重启抢资源。
                    mPingHandler.postDelayed(this::relaunchApp, 600L);
                })
                .show();
    }

    /** 重新打开本应用（新进程加载新代码）。 */
    private void relaunchApp() {
        try {
            Intent re = new Intent(this, ModuleSettingsActivity.class);
            re.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(re);
        } catch (Throwable ignored) {
            // 启动失败则仅退出，用户手动重开
        }
        // 稍等让新 Activity 启动请求生效，再结束当前进程。
        mPingHandler.postDelayed(() -> {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }, 250L);
    }

    private void buildAdvancedCard(LinearLayout root) {
        LinearLayout card = card();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView title = new TextView(this);
        title.setText(R.string.settings_rule_title);
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        texts.addView(title);
        TextView sub = new TextView(this);
        sub.setText(R.string.settings_rule_sum);
        sub.setTextSize(11f);
        sub.setTextColor(text2());
        sub.setPadding(0, dp(3), 0, 0);
        texts.addView(sub);
        ruleSummaryView = new TextView(this);
        ruleSummaryView.setTextSize(11f);
        ruleSummaryView.setTextColor(text3());
        ruleSummaryView.setPadding(0, dp(3), 0, 0);
        texts.addView(ruleSummaryView);
        row.addView(texts);

        // 只保留一个「设置」按钮；「自动检测」放进设置弹窗内（一键完成）。
        TextView btn = new TextView(this);
        btn.setText(R.string.settings_rule_set);
        btn.setTextSize(12f);
        btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        btn.setTextColor(accent());
        btn.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setCornerRadius(dp(999));
        btnBg.setColor(night ? 0xFF3A2F5C : 0xFFE7DFFF);
        btn.setBackground(btnBg);
        btn.setOnClickListener(v -> showRuleDialog());
        btn.setClickable(true);   // 无障碍
        row.addView(btn);

        card.addView(row);
        root.addView(card);
        refreshRuleSummary();
    }

    /** 刷新「当前识别规则」摘要：内置规则 / 自定义资源名。 */
    private void refreshRuleSummary() {
        if (ruleSummaryView == null) {
            return;
        }
        String rule = ClockSecondsConfig.getClockRule(getContentResolver());
        ruleSummaryView.setText(getString(R.string.settings_rule_current,
                rule.isEmpty() ? getString(R.string.settings_rule_builtin) : rule));
    }

    /**
     * 设置弹窗：内置「自动检测」一键按钮 + 手动填写输入框。
     *
     * <p>多数用户不懂「资源名」是什么，所以把自动检测放在最显眼的位置；手动填写作为备选。
     * 手填值明显不像时间控件资源名时，保存前二次确认，避免乱填导致不生效却以为设置好了。</p>
     */
    private void showRuleDialog() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(22), dp(4), dp(22), dp(4));

        // 说明：不懂设置可点自动检测一键完成。
        TextView desc = new TextView(this);
        desc.setText(R.string.settings_rule_dialog_desc);
        desc.setTextSize(13f);
        desc.setTextColor(text2());
        desc.setLineSpacing(dp(3), 1f);
        box.addView(desc);

        // 自动检测大按钮（主操作，醒目）。
        TextView detectBtn = new TextView(this);
        detectBtn.setText(R.string.settings_rule_detect);
        detectBtn.setTextSize(14f);
        detectBtn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        detectBtn.setTextColor(0xFFFFFFFF);
        detectBtn.setGravity(Gravity.CENTER);
        detectBtn.setPadding(dp(16), dp(11), dp(16), dp(11));
        GradientDrawable detectBg = new GradientDrawable();
        detectBg.setCornerRadius(dp(999));
        detectBg.setColor(accent());
        detectBtn.setBackground(detectBg);
        LinearLayout.LayoutParams detectLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        detectLp.topMargin = dp(12);
        detectBtn.setLayoutParams(detectLp);
        box.addView(detectBtn);

        // 「或手动填写」分隔提示。
        TextView orManual = new TextView(this);
        orManual.setText(R.string.settings_rule_or_manual);
        orManual.setTextSize(11f);
        orManual.setTextColor(text3());
        orManual.setPadding(0, dp(14), 0, dp(4));
        box.addView(orManual);

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setSingleLine(true);
        input.setText(ClockSecondsConfig.getClockRule(getContentResolver()));
        input.setHint("clock");
        input.setPadding(dp(8), dp(10), dp(8), dp(10));
        box.addView(input);

        // 当前生效值提示。
        TextView current = new TextView(this);
        String rule = ClockSecondsConfig.getClockRule(getContentResolver());
        current.setText(getString(R.string.settings_rule_current,
                rule.isEmpty() ? getString(R.string.settings_rule_builtin) : rule));
        current.setTextSize(11f);
        current.setTextColor(text3());
        current.setPadding(0, dp(6), 0, 0);
        box.addView(current);

        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.settings_rule_title)
                .setView(box)
                .setNeutralButton(R.string.settings_rule_reset, (d, w) -> applyRule(""))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.settings_rule_apply, null)
                .create();
        dialog.show();

        // 自动检测：关闭弹窗后走一键检测流程（结果用 Toast 反馈）。
        detectBtn.setOnClickListener(v -> {
            dialog.dismiss();
            startDetect();
        });

        // 应用按钮手动接管：手填值可疑时先二次确认，避免「乱填→以为设置好了」。
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String val = input.getText().toString().trim();
            if (val.isEmpty() || looksLikeClockName(val)) {
                applyRule(val);
                dialog.dismiss();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.settings_rule_confirm_title)
                        .setMessage(getString(R.string.settings_rule_confirm_msg, val))
                        .setPositiveButton(R.string.settings_rule_confirm_save,
                                (d2, w2) -> {
                                    applyRule(val);
                                    dialog.dismiss();
                                })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });
    }

    /** 手填值是否「像」时间控件的资源名（通常含 clock / time / date 关键字）。 */
    private static boolean looksLikeClockName(String v) {
        String s = v.toLowerCase(Locale.ROOT);
        return s.contains("clock") || s.contains("time") || s.contains("date");
    }

    /** 保存自定义时钟识别名：优先直接写 Secure，无权限改发广播由 SystemUI 代写。 */
    private void applyRule(String rule) {
        try {
            ClockSecondsConfig.setClockRule(getContentResolver(), rule);
        } catch (SecurityException ignored) {
            // 无 WRITE_SECURE_SETTINGS：走广播路径
        }
        try {
            Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_CLOCK_RULE);
            intent.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            intent.putExtra(ClockSecondsConfig.EXTRA_RULE, rule);
            sendBroadcast(intent);
        } catch (Throwable ignored) {
            // 发送失败不影响本地保存
        }
        refreshRuleSummary();
        Toast.makeText(this, R.string.settings_rule_applied, Toast.LENGTH_LONG).show();
    }

    /**
     * 自动检测状态栏时钟识别名：发广播给 SystemUI，由其在系统进程内扫描候选并回写。
     *
     * <p>{@code UnspecifiedRegisterReceiverFlag}：API &lt; 33 没有 RECEIVER_EXPORTED 重载，
     * 该分支已按版本显式分流，属预期写法。</p>
     */
    @android.annotation.SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void startDetect() {
        if (mDetecting.compareAndSet(false, true)) {
            // 注册结果回传接收器（仅本 Activity 存活期间），避免泄漏。
            mDetectReceiver = new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context ctx, Intent intent) {
                    mDetecting.set(false);
                    String detected = intent.getStringExtra(ClockSecondsConfig.EXTRA_DETECTED_RULE);
                    boolean builtin = intent.getBooleanExtra(
                            ClockSecondsConfig.EXTRA_DETECTED_BUILTIN, false);
                    unregisterDetectReceiver();
                    refreshRuleSummary();
                    if (detected == null || detected.isEmpty()) {
                        Toast.makeText(ModuleSettingsActivity.this,
                                R.string.settings_rule_detect_fail, Toast.LENGTH_LONG).show();
                    } else if (builtin) {
                        // 内置规则已识别：立即生效，无需重启。
                        Toast.makeText(ModuleSettingsActivity.this,
                                R.string.settings_rule_detected_builtin, Toast.LENGTH_LONG).show();
                    } else {
                        // 自定义识别名已写入：需重启 SystemUI 生效。
                        Toast.makeText(ModuleSettingsActivity.this,
                                R.string.settings_rule_detected_custom, Toast.LENGTH_LONG).show();
                    }
                }
            };
            IntentFilter filter = new IntentFilter(ClockSecondsConfig.ACTION_DETECT_CLOCK_RESULT);
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                // RECEIVER_EXPORTED：回传广播来自 SystemUI（不同应用），
                // NOT_EXPORTED 会阻止跨应用显式广播。
                // 风险可控：action 为模块私有，内容仅为时钟资源名字符串，
                // 且接收方只做 Toast 提示、不写入任何状态（F1 加固已覆盖写入侧）。
                registerReceiver(mDetectReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                registerReceiver(mDetectReceiver, filter);
            }
            try {
                Intent req = new Intent(ClockSecondsConfig.ACTION_DETECT_CLOCK);
                req.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
                sendBroadcast(req);
                Toast.makeText(this, R.string.settings_rule_detecting, Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {
                mDetecting.set(false);
                unregisterDetectReceiver();
                Toast.makeText(this, R.string.settings_rule_detect_fail, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void unregisterDetectReceiver() {
        if (mDetectReceiver != null) {
            try {
                unregisterReceiver(mDetectReceiver);
            } catch (Throwable ignored) {
                // 已注销则忽略
            }
            mDetectReceiver = null;
        }
    }

    // ------------------------------------------------------------------
    // 桌面图标开关
    // ------------------------------------------------------------------

    /** 桌面图标（activity-alias）在桌面上的组件名。 */
    private static final String LAUNCHER_ALIAS_CLASS =
            "io.github.kelebao.colorosclockseconds.settings.LauncherAlias";
    private static final String PREF_LAUNCHER_ICON = "launcher_icon";

    /** 桌面图标是否启用（默认开，与 manifest 里 activity-alias 的 enabled 保持一致）。 */
    private boolean isLauncherIconOn() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getBoolean(PREF_LAUNCHER_ICON, true);
    }

    /**
     * 启用/禁用桌面图标。
     *
     * <p>用 {@code setComponentEnabledSetting} 直接改 activity-alias 的启用态，
     * 不改 APK 本身、不需要重启；{@code DONT_KILL_APP} 保证不杀进程。</p>
     */
    private boolean setLauncherIcon(boolean enabled) {
        try {
            getPackageManager().setComponentEnabledSetting(
                    new ComponentName(this, LAUNCHER_ALIAS_CLASS),
                    enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putBoolean(PREF_LAUNCHER_ICON, enabled).apply();
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    /**
     * 以 SharedPreferences 为准，校准桌面图标的实际启用态。
     *
     * <p>PackageManager 里 activity-alias 的启用态会跨版本升级保留（例如调试期显式关过后再
     * 覆盖安装），而偏好默认是「开」。这里在每次启动对齐一次，保证「默认开」能在各种历史
     * 状态下自愈；同时用户主动关过后偏好为 false，不会被反过来打开。
     * {@code COMPONENT_ENABLED_STATE_DEFAULT} 对本别名等同于启用（manifest enabled=true）。</p>
     */
    private void reconcileLauncherIcon() {
        try {
            boolean want = isLauncherIconOn();
            int desired = want ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            int state = getPackageManager().getComponentEnabledSetting(
                    new ComponentName(this, LAUNCHER_ALIAS_CLASS));
            int actual = (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
                    ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            if (actual != desired) {
                setLauncherIcon(want);
            }
        } catch (Throwable ignored) {
            // 校准失败不影响使用，用户仍可用开关手动切换
        }
    }

    /** 卡片：桌面显示图标开关。 */
    private void buildLauncherCard(LinearLayout root) {
        LinearLayout card = card();

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView title = new TextView(this);
        title.setText(R.string.settings_launcher_title);
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        texts.addView(title);
        TextView sub = new TextView(this);
        sub.setText(R.string.settings_launcher_sum);
        sub.setTextSize(11f);
        sub.setTextColor(text2());
        sub.setPadding(0, dp(3), 0, 0);
        texts.addView(sub);
        row.addView(texts);

        final Switch sw = new Switch(this);
        sw.setChecked(isLauncherIconOn());
        styleSwitch(sw);
        row.addView(sw);
        card.addView(row);
        root.addView(card);

        final boolean[] reverting = {false};
        sw.setOnCheckedChangeListener((v, checked) -> {
            if (reverting[0]) {
                return;
            }
            if (setLauncherIcon(checked)) {
                if (checked) {
                    Toast.makeText(this, R.string.settings_launcher_on,
                            Toast.LENGTH_SHORT).show();
                } else {
                    // 关闭后桌面图标不会立即消失（启动器缓存），需重启设备才会移除。
                    Toast.makeText(this, R.string.settings_launcher_off,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                reverting[0] = true;
                sw.setChecked(!checked);
                reverting[0] = false;
                toastWriteFailed();
            }
        });
    }

    private void buildUpdateCard(LinearLayout root) {
        LinearLayout card = card();
        card.setPadding(dp(18), dp(6), dp(18), dp(6));

        LinearLayout autoRow = new LinearLayout(this);
        autoRow.setOrientation(LinearLayout.HORIZONTAL);
        autoRow.setGravity(Gravity.CENTER_VERTICAL);
        autoRow.setPadding(0, dp(12), 0, dp(12));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t1 = new TextView(this);
        t1.setText(R.string.settings_update_auto);
        t1.setTextSize(15f);
        t1.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        t1.setTextColor(text());
        texts.addView(t1);
        TextView t2 = new TextView(this);
        t2.setText(R.string.settings_update_auto_sum);
        t2.setTextSize(11f);
        t2.setTextColor(text2());
        t2.setPadding(0, dp(3), 0, 0);
        texts.addView(t2);
        autoRow.addView(texts);

        Switch autoSwitch = new Switch(this);
        autoSwitch.setChecked(prefs().getBoolean(KEY_AUTO_UPDATE, true));
        styleSwitch(autoSwitch);
        autoSwitch.setOnCheckedChangeListener((v, checked) -> {
            prefs().edit().putBoolean(KEY_AUTO_UPDATE, checked).apply();
            if (checked) {
                runUpdateCheck(true);
            }
        });
        autoRow.addView(autoSwitch);
        card.addView(autoRow);
        card.addView(divider());
        card.addView(linkRow(R.string.settings_update_open, R.string.settings_update_open_sum,
                v -> openUrl(RELEASES_URL)));
        root.addView(card);
    }

    private void buildAboutCard(LinearLayout root) {
        LinearLayout card = card();
        card.setPadding(dp(18), dp(6), dp(18), dp(6));

        card.addView(valueRow(getString(R.string.settings_about_version,
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), null));
        card.addView(divider());
        card.addView(linkRow(R.string.settings_about_github, R.string.settings_about_github_sum,
                v -> openUrl(REPO_URL)));
        card.addView(divider());
        card.addView(linkRow(R.string.settings_about_licenses,
                R.string.settings_about_licenses_sum, v -> showLicensesDialog()));
        card.addView(divider());
        card.addView(linkRow(R.string.settings_about_guide, R.string.settings_about_guide_sum,
                v -> showGuideDialog()));
        root.addView(card);
    }

    private void buildPrimaryButton(LinearLayout root) {
        TextView button = new TextView(this);
        button.setText(R.string.settings_update_check);
        button.setTextSize(15f);
        button.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        button.setTextColor(0xFFFFFFFF);
        button.setGravity(Gravity.CENTER);
        button.setPadding(0, dp(14), 0, dp(14));

        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(999));
        bg.setColor(accent());
        button.setBackground(bg);
        button.setElevation(dp(2));
        button.setOnClickListener(v -> runUpdateCheck(true));
        button.setClickable(true);   // 无障碍

        // 主按钮「检查更新」也套用按压倾斜 + 高光 + 按压音，与各卡片一致。
        // 实心强调色底上再叠强调色高光不可见，故传白色 → 35% 径向白光才有反馈。
        button.setClipToOutline(true);        // 高光裁到胶囊圆角，避免溢出
        mPressTilt.register(button, 0xFFFFFFFF);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(4);
        button.setLayoutParams(lp);
        root.addView(button);
    }

    private void buildFooter(LinearLayout root) {
        TextView footer = new TextView(this);
        footer.setText(R.string.settings_hint);
        footer.setTextSize(11f);
        footer.setTextColor(text3());
        footer.setPadding(dp(4), dp(14), dp(4), 0);
        root.addView(footer);
    }

    // ==================================================================
    // 预览与状态
    // ==================================================================

    /** 每秒刷新预览：显秒开时 HH:mm:ss，关时 HH:mm；小时十位用所选颜色；同时刷新状态胶囊。 */
    private void updatePreview() {
        if (previewView == null) {
            return;
        }
        // 已注册观察者 → 用缓存值（免每秒多次 binder 读取）；没注册上 → 退回直读，保证状态实时。
        final boolean seconds;
        final boolean hourOn;
        if (mObserverOk) {
            seconds = mCfgSeconds;
            hourOn = mCfgHourColorOn;
        } else {
            seconds = ClockSecondsConfig.isEnabled(getContentResolver());
            hourOn = ClockSecondsConfig.isHourColorEnabled(getContentResolver());
        }
        if (mCfgDigitColors == null || mCfgDigitColors.length < ClockSecondsConfig.DIGIT_COUNT) {
            mCfgDigitColors = ClockSecondsConfig.getDigitColors(getContentResolver());
        }
        mNowDate.setTime(System.currentTimeMillis());
        final String text = (seconds ? mFmtSec : mFmtNoSec).format(mNowDate);
        final SpannableStringBuilder sb = new SpannableStringBuilder(text);
        // 六位逐位上色：与状态栏里的 applyDigitColors 同一套 slot 映射，预览即所写。
        // 展开取色时，没被选中的位一律变淡（不放大），一眼看出正在改哪一位。
        for (int i = 0; i < sb.length(); i++) {
            final int slot = ClockSecondsConfig.slotOfChar(sb, i);
            if (slot < 0) {
                continue;
            }
            final boolean dim = mDigitsExpanded && slot != mSelSlot;
            final int color = mCfgDigitColors[slot];
            final boolean follow = !hourOn || color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
            // 「抬高」是几何变换，跟有没有颜色无关，必须先挂：vc83 之前它写在「跟随系统且不淡就
            // continue」后面，于是选中跟随系统的那一位永远抬不起来（真机反馈「3 也没抬高」）。
            if (mDigitsExpanded && slot == mSelSlot && mLiftNow > 0) {
                sb.setSpan(new LiftSpan(mLiftNow), i, i + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            if (follow && !dim) {
                continue;   // 跟随系统且不淡：用默认文字色，不必加 span
            }
            final int shown = follow ? text() : adaptPreviewColor(color);
            sb.setSpan(new ForegroundColorSpan(dim ? dimColor(shown) : shown), i, i + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        previewView.setText(sb);

        // 状态胶囊也每秒跟随：用户在状态栏点击切换后，页面能立刻反映真实状态。
        if (chipView != null) {
            chipView.setText(seconds ? R.string.settings_enabled : R.string.settings_disabled);
        }
        updateNetworkTimeLine();
        if (mDigitsExpanded) {
            positionMark();
        }
    }

    /** 网络时间 = 本地时间 + 采样得到的偏移，每秒跟着走。 */
    private void updateNetworkTimeLine() {
        if (netTimeView == null || netDriftMs == Long.MIN_VALUE) {
            return;
        }
        long localNow = System.currentTimeMillis();
        long netNow = localNow + netDriftMs;
        String netText = new java.text.SimpleDateFormat("HH:mm:ss", Locale.US)
                .format(new java.util.Date(netNow));
        // 用「本地 快/慢」的直观含义：正值 = 本地比网络快。
        long diffSec = Math.round((localNow - netNow) / 1000.0);
        String diff = (diffSec > 0 ? "+" : "") + diffSec + "s";
        netTimeView.setText(getString(R.string.settings_network_time, netText, diff));
    }

    /**
     * 一键对时：应用无权直接修改系统时钟（SET_TIME 是系统权限），
     * 这里让 SystemUI 打开系统的「自动设置时间」，由系统自己向网络校准。
     */
    private void syncNetworkTime() {
        try {
            Intent intent = new Intent(ClockSecondsConfig.ACTION_SYNC_TIME);
            intent.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(intent);
            Toast.makeText(this, R.string.settings_sync_requested, Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            toastWriteFailed();
        }
    }

    /** 后台取一次网络时间（HTTP Date 头），按 RTT 折半校正请求延迟；点按可重新获取。 */
    private void fetchNetworkTime() {
        if (netTimeView == null) {
            return;
        }
        new Thread(() -> {
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
            final long drift = bestDrift;
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed() || netTimeView == null) {
                    return;
                }
                if (drift == Long.MIN_VALUE) {
                    netDriftMs = Long.MIN_VALUE;
                    netTimeView.setText(R.string.settings_network_time_fail);
                    return;
                }
                netDriftMs = drift;
                updateNetworkTimeLine();
                saveNetOffset(drift);
            });
        }).start();
    }

    /**
     * 把测得的网络时间偏移（网络 - 系统）写入 Secure，供 SystemUI 内的浮窗显示网络时间。
     * 无 WRITE_SECURE_SETTINGS 时改发广播，由 SystemUI 代写。
     */
    private void saveNetOffset(long drift) {
        try {
            ClockSecondsConfig.setNetOffset(getContentResolver(), drift);
            return;
        } catch (SecurityException ignored) {
            // 无权限，走广播路径
        }
        try {
            Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_NET_OFFSET);
            intent.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            intent.putExtra(ClockSecondsConfig.EXTRA_NET_OFFSET, drift);
            sendBroadcast(intent);
        } catch (Throwable ignored) {
            // 写不进就下次再测，浮窗会退回按系统时间显示
        }
    }

    private Long headDate(String urlStr) {
        java.net.HttpURLConnection conn = null;
        try {
            java.net.URL url = new java.net.URL(urlStr);
            conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
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

    private void refreshStatus() {
        updatePreview();
    }

    private void refreshColorSummary() {
        int color = ClockSecondsConfig.getHourColor(getContentResolver());
        boolean enabled = ClockSecondsConfig.isHourColorEnabled(getContentResolver());
        if (colorDotView != null) {
            // 小结绿点：实心圆 + 荧光呼吸光晕（颜色=当前小时色），由 breathTick 驱动。
            BreathSwatchDrawable dot = new BreathSwatchDrawable(color, true, 0x22000000, dp(1));
            colorDotView.setBackground(dot);
            colorDotView.setContentDescription(swatchDesc(color));   // 无障碍：读屏播报「颜色名 + 色号」
            colorDotView.setAlpha(enabled ? 1f : 0.35f);
            mBreathSwatch = dot;
        }
        if (colorValueView != null) {
            colorValueView.setText(hexOf(color));
            colorValueView.setAlpha(enabled ? 1f : 0.45f);
        }
    }

    // ==================================================================
    // 小时颜色
    // ==================================================================

    private View makeSwatch(int color, boolean selected, GridLayout grid) {
        View swatch = new View(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = dp(42);
        lp.height = dp(42);
        lp.setMargins(dp(5), dp(5), dp(5), dp(5));
        swatch.setLayoutParams(lp);
        swatch.setBackground(swatchDrawable(color, selected));
        swatch.setOnClickListener(v -> applyDigitColor(mSelSlot, color));
        // 无障碍：色块是纯色 View，读屏读不出内容 → 补 contentDescription（色号）；
        // setSelected 让 TalkBack 自动播报“已选中”；setClickable 让它被识别为可操作项。
        swatch.setContentDescription(swatchDesc(color));
        swatch.setSelected(selected);
        swatch.setClickable(true);
        return swatch;
    }

    /** 颜色转 "#RRGGBB"（用于色块朗读描述与色号显示）。 */
    private static String hexOf(int color) {
        return String.format(Locale.US, "#%06X", color & 0xFFFFFF);
    }

    /** 色块的无障碍朗读描述：本地化颜色名 + 色号（如「红色 #F44336」）。 */
    private String swatchDesc(int color) {
        final String name = colorNameOf(color);
        final String hex = hexOf(color);
        return name.isEmpty() ? hex : name + " " + hex;
    }

    /** 小时颜色的本地化名称（用于 TalkBack 朗读）；未知色返回空串。 */
    private String colorNameOf(int color) {
        final int res;
        switch (color) {
            case 0xFFF44336: res = R.string.swatch_red; break;
            case 0xFFFF9800: res = R.string.swatch_orange; break;
            case 0xFFFFC107: res = R.string.swatch_amber; break;
            case 0xFF4CAF50: res = R.string.swatch_green; break;
            case 0xFF00BCD4: res = R.string.swatch_cyan; break;
            case 0xFF2196F3: res = R.string.swatch_blue; break;
            case 0xFF9C27B0: res = R.string.swatch_purple; break;
            case 0xFFE91E63: res = R.string.swatch_pink; break;
            case 0xFFFFFFFF: res = R.string.swatch_white; break;
            case 0xFF9E9E9E: res = R.string.swatch_grey; break;
            default: return "";
        }
        return getString(res);
    }

    private GradientDrawable swatchDrawable(int color, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        if (selected) {
            d.setStroke(dp(3), accent());
        } else {
            d.setStroke(dp(1), night ? 0x33FFFFFF : 0x1F000000);
        }
        return d;
    }

        /** 禁用态色块色：向卡片底色混合成「不透明淡彩」（替代原来的 view alpha 半透明）。 */
    private int swatchColor(int color, boolean enabled) {
        if (enabled) return color;
        return blendColor(color, night ? DARK_CARD : LIGHT_CARD, 0.68f);
    }

    private static int blendColor(int a, int b, float t) {
        final int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        final int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        final int r = Math.round(ar + (br - ar) * t);
        final int g = Math.round(ag + (bg - ag) * t);
        final int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

        private CharSequence makeGestureItem(int titleRes, String hint) {
        String title = getString(titleRes);
        SpannableStringBuilder sb = new SpannableStringBuilder(title);
        sb.append("\n");
        int start = sb.length();
        sb.append(hint);
        int end = sb.length();
        sb.setSpan(new AbsoluteSizeSpan(11, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sb.setSpan(new ForegroundColorSpan(text3()), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }

    private void showGestureDialog(TextView gestureValue) {
        final int current = ClockSecondsConfig.getGesture(getContentResolver());
        final String hint = getString(R.string.settings_gesture_hint);
        // 列表只有两项：index 0 = 单击，1 = 双击。
        final int checkedIndex = current == ClockSecondsConfig.GESTURE_DOUBLE_TAP ? 1 : 0;
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_gesture_title)
                .setSingleChoiceItems(new CharSequence[]{
                        makeGestureItem(R.string.settings_gesture_click, hint),
                        makeGestureItem(R.string.settings_gesture_double_tap, hint)},
                        checkedIndex,
                        (dialog, which) -> {
                            dialog.dismiss();
                            // 列表只有两项：0 = 单击，1 = 双击。
                            final int gesture = which == 1
                                    ? ClockSecondsConfig.GESTURE_DOUBLE_TAP
                                    : ClockSecondsConfig.GESTURE_CLICK;
                            Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_GESTURE)
                                    .putExtra(ClockSecondsConfig.EXTRA_GESTURE, gesture);
                            applyConfig(ClockSecondsConfig.TOGGLE_GESTURE_KEY, gesture, intent,
                                    ok -> {
                                        if (ok) {
                                            int nameRes = gestureNameRes(gesture);
                                            gestureValue.setText(nameRes);
                                            Toast.makeText(this, getString(
                                                    R.string.settings_gesture_applied,
                                                    getString(nameRes)),
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            toastWriteFailed();
                                        }
                                    });
                        })
                .show();
    }

    private int gestureNameRes(int gesture) {
        switch (gesture) {
            case ClockSecondsConfig.GESTURE_DOUBLE_TAP:
                return R.string.settings_gesture_double_tap;
            default:
                return R.string.settings_gesture_click;
        }
    }

    // ==================================================================
    // 视图工具
    // ==================================================================

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));
        bg.setColor(night ? DARK_CARD : LIGHT_CARD);
        card.setBackground(bg);
        card.setElevation(dp(3));
        card.setClipToOutline(true);   // 裁剪到圆角轮廓，防止按压高光(前景)溢出到卡片圆角外
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        mPressTilt.register(card, accent());
        return card;
    }

    /** 同 {@link #card()}，但**不**注册按压倾斜效果（卡片背景按压无反馈，只让内部按钮自带按压）。 */
    private LinearLayout cardNoPress() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));
        bg.setColor(night ? DARK_CARD : LIGHT_CARD);
        card.setBackground(bg);
        card.setElevation(dp(3));
        card.setClipToOutline(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout statCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(20));
        bg.setColor(night ? DARK_CARD : LIGHT_CARD);
        card.setBackground(bg);
        card.setElevation(dp(2));
        card.setClickable(true);
        card.setClipToOutline(true);   // 裁剪到圆角轮廓，防止按压高光溢出圆角
        mPressTilt.register(card, accent());
        return card;
    }

    private TextView statLabel(int textRes) {
        TextView t = new TextView(this);
        t.setText(textRes);
        t.setTextSize(11f);
        t.setTextColor(text2());
        // 宽度跟随卡片：长译文在卡内换行（最多三行）+ 省略号，不会把卡片撑宽。
        t.setMaxLines(3);
        t.setEllipsize(TextUtils.TruncateAt.END);
        t.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return t;
    }

    private View divider() {
        WavyLineView v = new WavyLineView(this);
        v.setLineColor(WAVE_COLOR);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3));
        v.setLayoutParams(lp);
        return v;
    }

    /** 可点击行：标题 + 可选副标题 + 右侧箭头。 */
    private LinearLayout linkRow(int titleRes, Integer subtitleRes, View.OnClickListener l) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView t = new TextView(this);
        t.setText(titleRes);
        t.setTextSize(14f);
        t.setTextColor(text());
        texts.addView(t);
        if (subtitleRes != null) {
            TextView s = new TextView(this);
            s.setText(subtitleRes);
            s.setTextSize(11f);
            s.setTextColor(text2());
            s.setPadding(0, dp(3), 0, 0);
            texts.addView(s);
        }
        row.addView(texts);

        TextView arrow = new TextView(this);
        arrow.setText("›");
        arrow.setTextSize(18f);
        arrow.setTextColor(text3());
        row.addView(arrow);
        row.setOnClickListener(l);
        return row;
    }

    /** 只读行：标题 + 可选右侧值。 */
    private LinearLayout valueRow(CharSequence title, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(13), 0, dp(13));

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(14f);
        t.setTextColor(text());
        t.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(t);

        if (value != null) {
            TextView v = new TextView(this);
            v.setText(value);
            v.setTextSize(13f);
            v.setTextColor(text2());
            row.addView(v);
        }
        return row;
    }

    private void styleSwitch(Switch sw) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{},
        };
        sw.setThumbTintList(new ColorStateList(states,
                new int[]{accent(), night ? 0xFF6A6187 : 0xFFD9D3E8}));
        sw.setTrackTintList(new ColorStateList(states,
                new int[]{withAlpha(accent(), 0x66), night ? 0xFF3A3352 : 0xFFE4DFF0}));
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    private int accent() {
        return night ? ACCENT_DARK : ACCENT_LIGHT;
    }

    private int text() {
        return night ? DARK_TEXT : LIGHT_TEXT;
    }

    private int text2() {
        return night ? DARK_TEXT_2 : LIGHT_TEXT_2;
    }

    private int text3() {
        return night ? DARK_TEXT_3 : LIGHT_TEXT_3;
    }

    // ==================================================================
    // 更新检查
    // ==================================================================

    private void maybeAutoCheckUpdate() {
        if (prefs().getBoolean(KEY_AUTO_UPDATE, true)) {
            runUpdateCheck(false);
        }
    }

    private void runUpdateCheck(final boolean manual) {
        if (manual) {
            Toast.makeText(this, R.string.update_checking, Toast.LENGTH_SHORT).show();
        }
        UpdateChecker.check(this, (remoteVersion, failed) -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (failed) {
                if (manual) {
                    Toast.makeText(this, R.string.update_failed, Toast.LENGTH_LONG).show();
                }
                return;
            }
            if (remoteVersion != null
                    && UpdateChecker.compareVersions(remoteVersion,
                    BuildConfig.VERSION_NAME) > 0) {
                showUpdateDialog(remoteVersion);
            } else if (manual) {
                Toast.makeText(this, R.string.update_latest, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(String remoteVersion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.update_dialog_title)
                .setMessage(getString(R.string.update_dialog_msg,
                        remoteVersion, BuildConfig.VERSION_NAME))
                .setPositiveButton(R.string.update_go, (d, w) -> openUrl(RELEASES_URL))
                .setNegativeButton(R.string.update_later, null)
                .show();
    }

    private void showLicensesDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_about_licenses)
                .setMessage(R.string.licenses_text)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /** 首次启动弹出操作说明（看过即不再弹）。 */
    private void maybeShowIntro() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        if (sp.getBoolean(PREF_SEEN_INTRO, false)) {
            return;
        }
        sp.edit().putBoolean(PREF_SEEN_INTRO, true).apply();
        showGuideDialog();
    }

    /** 操作说明对话框（首次安装引导与「关于」里的「操作说明」入口共用）。 */
    private void showGuideDialog() {
        ScrollView scroll = new ScrollView(this);
        scroll.setPadding(dp(22), dp(10), dp(22), dp(6));
        TextView msg = new TextView(this);
        msg.setText(R.string.settings_intro_msg);
        msg.setTextSize(14f);
        msg.setLineSpacing(dp(6), 1f);
        msg.setTextColor(text2());
        scroll.addView(msg);
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_intro_title)
                .setView(scroll)
                .setPositiveButton(R.string.settings_intro_ok, null)
                .show();
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, url, Toast.LENGTH_LONG).show();
        }
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    // ==================================================================
    // 配置写入（无权限时由 SystemUI 代写）
    // ==================================================================

    private void applyConfig(final String key, final int value, final Intent broadcast,
                             final java.util.function.Consumer<Boolean> onResult) {
        // 序列号守卫：只有最新操作的结果会回调，旧操作被新操作取代时静默（避免误报失败）。
        final int seq = mWriteSeq.incrementAndGet();
        final java.util.function.Consumer<Boolean> guarded = ok -> {
            if (mWriteSeq.get() == seq) {
                onResult.accept(ok);
            }
        };
        try {
            Settings.Secure.putInt(getContentResolver(), key, value);
            guarded.accept(true);
            // 双保险：即便直写成功，也广播一次让 SystemUI 模块即时感知。
            // 否则模块只靠「广播」更新内存态，而 App 有 WRITE_SECURE_SETTINGS 时直写不广播 → 状态漂移。
            try {
                broadcast.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
                sendBroadcast(broadcast);
            } catch (Throwable ignored2) {
                // 广播失败不影响直写结果
            }
            return;
        } catch (SecurityException ignored) {
            // 无 WRITE_SECURE_SETTINGS：走广播路径
        }
        try {
            broadcast.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(broadcast);
        } catch (Throwable t) {
            guarded.accept(false);
            return;
        }
        verifyWrite(key, value, 0, guarded);
    }

    private void verifyWrite(final String key, final int value, final int attempt,
                             final java.util.function.Consumer<Boolean> onResult) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        int actual = Integer.MIN_VALUE;
        try {
            actual = Settings.Secure.getInt(getContentResolver(), key, Integer.MIN_VALUE);
        } catch (Throwable ignored) {
            // 读取失败按未生效处理
        }
        if (actual == value) {
            onResult.accept(true);
            return;
        }
        if (attempt >= 8) {
            onResult.accept(false);
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> verifyWrite(key, value, attempt + 1, onResult), 250);
    }

    private void toastWriteFailed() {
        Toast.makeText(this, R.string.settings_write_failed, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
