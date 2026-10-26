package io.github.kelebao.colorosclockseconds.settings;

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
import android.text.Spanned;
import android.text.TextUtils;
import io.github.kelebao.colorosclockseconds.ArcTickDrawable;
import io.github.kelebao.colorosclockseconds.BreathSwatchDrawable;
import io.github.kelebao.colorosclockseconds.CornerArcsDrawable;
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
    private boolean mObserverOk = false;
    private final android.database.ContentObserver mCfgObserver =
            new android.database.ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    refreshConfigCache();
                    updatePreview();
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
        // 顶部留白收小；底部留小缝，让网络时间行贴近卡片最下方。
        card.setPadding(dp(18), dp(2), dp(18), dp(6));
        // 底部叠「长弧刻度」（只有刻度、无弧线），把网络时间/提示两行圈在弧中间。
        ArcTickDrawable arc = new ArcTickDrawable(night);
        mArcTicks = arc;
        // card() 已 mPressTilt.register（卡片背景不变、另叠了按压高光前景层）；这里只把刻度弧叠上去。
        card.setBackground(new LayerDrawable(new Drawable[]{card.getBackground(), arc}));

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
        previewPlate.setPadding(dp(22), dp(2), dp(22), dp(14));

        previewView = new TextView(this);
        previewView.setTextSize(44f);
        previewView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        previewView.setTextColor(text());
        previewView.setGravity(Gravity.CENTER_HORIZONTAL);
        previewView.setOnClickListener(v -> showPreviewHint());
        previewView.setClickable(true);   // 无障碍：让 TalkBack 识别为可点击项
        previewPlate.addView(previewView);
        card.addView(previewPlate);

        // 网络时间对比：点一下重新获取（HTTP Date 头，按 RTT 折半校正）。
        LinearLayout netRow = new LinearLayout(this);
        netRow.setOrientation(LinearLayout.HORIZONTAL);
        netRow.setGravity(Gravity.CENTER);
        netRow.setPadding(0, dp(40), 0, 0);

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

        card.addView(netRow);

        root.addView(card);
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

        root.addView(row);
        refreshColorSummary();
    }

    private void buildColorCard(LinearLayout root) {
        LinearLayout card = card();

        // 右下角三条同心半弧装饰（参考支付卡片），固定浅薰衣草紫。
        CornerArcsDrawable arcs = new CornerArcsDrawable(WAVE_COLOR, dp(2));
        mCornerArcs = arcs;
        card.setBackground(new LayerDrawable(new Drawable[]{card.getBackground(), arcs}));

        LinearLayout switchRow = new LinearLayout(this);
        switchRow.setOrientation(LinearLayout.HORIZONTAL);
        switchRow.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView title = new TextView(this);
        title.setText(R.string.settings_color_enable);
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        texts.addView(title);
        TextView sub = new TextView(this);
        sub.setText(R.string.settings_color_enable_sum);
        sub.setTextSize(11f);
        sub.setTextColor(text2());
        sub.setPadding(0, dp(3), 0, 0);
        texts.addView(sub);
        switchRow.addView(texts);

        final Switch colorSwitch = new Switch(this);
        colorSwitch.setChecked(ClockSecondsConfig.isHourColorEnabled(getContentResolver()));
        styleSwitch(colorSwitch);
        switchRow.addView(colorSwitch);
        card.addView(switchRow);

        final GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        grid.setPadding(0, dp(14), 0, 0);
        int current = ClockSecondsConfig.getHourColor(getContentResolver());
        for (int color : HOUR_COLORS) {
            grid.addView(makeSwatch(color, color == current, grid));
        }
        card.addView(grid);
        applySwatchEnabled(grid, colorSwitch.isChecked());

        TextView hint = new TextView(this);
        hint.setText(R.string.settings_color_hint);
        hint.setTextSize(11f);
        hint.setTextColor(text3());
        hint.setPadding(0, dp(10), 0, 0);
        card.addView(hint);

        // 秒数字「呼吸」高亮已改为「背景板呼吸」且常开、无开关（见预览背景板与浮窗背景板）。

        root.addView(card);

        final boolean[] reverting = {false};
        colorSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (reverting[0]) {
                return;
            }
            Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_HOUR_COLOR_ENABLED)
                    .putExtra(ClockSecondsConfig.EXTRA_ENABLED, checked ? 1 : 0);
            applyConfig(ClockSecondsConfig.HOUR_COLOR_ENABLED_KEY, checked ? 1 : 0, intent, ok -> {
                if (ok) {
                    applySwatchEnabled(grid, checked);
                    refreshColorSummary();
                    updatePreview();
                } else {
                    reverting[0] = true;
                    colorSwitch.setChecked(!checked);
                    reverting[0] = false;
                    toastWriteFailed();
                }
            });
        });
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
        // 已注册观察者 → 用缓存值（免每秒 3 次 binder 读取）；没注册上 → 退回直读，保证状态实时。
        final boolean seconds;
        final boolean hourOn;
        final int hourColor;
        if (mObserverOk) {
            seconds = mCfgSeconds;
            hourOn = mCfgHourColorOn;
            hourColor = mCfgHourColor;
        } else {
            seconds = ClockSecondsConfig.isEnabled(getContentResolver());
            hourOn = ClockSecondsConfig.isHourColorEnabled(getContentResolver());
            hourColor = ClockSecondsConfig.getHourColor(getContentResolver());
        }
        mNowDate.setTime(System.currentTimeMillis());
        String text = (seconds ? mFmtSec : mFmtNoSec).format(mNowDate);
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        if (hourOn
                && sb.length() >= 2
                && Character.isDigit(sb.charAt(0)) && Character.isDigit(sb.charAt(1))) {
            sb.setSpan(new ForegroundColorSpan(hourColor), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        previewView.setText(sb);

        // 状态胶囊也每秒跟随：用户在状态栏点击切换后，页面能立刻反映真实状态。
        if (chipView != null) {
            chipView.setText(seconds
                    ? R.string.settings_enabled : R.string.settings_disabled);
        }
        updateNetworkTimeLine();
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
        swatch.setOnClickListener(v -> selectHourColor(color, grid));
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

    private void applySwatchEnabled(GridLayout grid, boolean enabled) {
        final int current = ClockSecondsConfig.getHourColor(getContentResolver());
        for (int i = 0; i < grid.getChildCount() && i < HOUR_COLORS.length; i++) {
            final View child = grid.getChildAt(i);
            child.setClickable(enabled);
            child.setAlpha(1f);   // 始终不透明：背景三条弧不会从半透明色块透出、盖住色块
            // 禁用不靠 view alpha（半透明会透出背景弧），而是把色块染成不透明淡彩。
            child.setBackground(swatchDrawable(swatchColor(HOUR_COLORS[i], enabled), HOUR_COLORS[i] == current));
        }
        // 三条同心弧常显，不再随开关隐藏。
        if (mCornerArcs != null) {
            mCornerArcs.setAlpha(255);
        }
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

    private void selectHourColor(final int color, final GridLayout grid) {
        Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_HOUR_COLOR)
                .putExtra(ClockSecondsConfig.EXTRA_COLOR, color);
        applyConfig(ClockSecondsConfig.HOUR_COLOR_KEY, color, intent, ok -> {
            if (ok) {
                for (int i = 0; i < grid.getChildCount() && i < HOUR_COLORS.length; i++) {
                    grid.getChildAt(i).setBackground(
                            swatchDrawable(HOUR_COLORS[i], HOUR_COLORS[i] == color));
                    // 同步选中态，让 TalkBack 播报哪个色块被选中
                    grid.getChildAt(i).setSelected(HOUR_COLORS[i] == color);
                }
                refreshColorSummary();
                updatePreview();
            } else {
                toastWriteFailed();
            }
        });
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
