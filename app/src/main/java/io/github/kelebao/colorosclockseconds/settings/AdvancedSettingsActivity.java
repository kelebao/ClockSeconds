package io.github.kelebao.colorosclockseconds.settings;import io.github.kelebao.colorosclockseconds.GradientShaderFactory;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.github.kelebao.colorosclockseconds.BreathSwatchDrawable;
import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;
import io.github.kelebao.colorosclockseconds.R;

/**
 * 高级设置（vc327）：把「系统模式自适应」「单色 HSV 调色盘」「我的预设」「渐变浅色适配」
 * 从首页数字颜色卡收敛到一个二级页面。首页数字颜色卡只保留预设圆点快速换色，
 * 深度调色与明暗两套配色都在这里。
 *
 * <p>数据全部走 {@link ClockSecondsConfig} 读的 Secure 键；「我的预设」存应用私有
 * SharedPreferences（advanced_presets，JSON 字符串），上限 9 组/套，长按删除，
 * 点击整组应用通过 {@link ClockSecondsConfig#ACTION_SET_DIGIT_COLORS_BATCH} 一次下发。</p>
 */
public class AdvancedSettingsActivity extends Activity {

    private static final String PREFS_NAME = "advanced_presets";
    private static final String KEY_PRESETS = "presets";
    private static final int MAX_PRESETS = 12;

    /** 与首页一致的九色色板 + 第 10 颗跟随系统。 */
    private static final int[] HOUR_COLORS = {
            0xFFF44336, 0xFFFF9800, 0xFFD4A017, 0xFF4CAF50, 0xFF00BCD4,
            0xFF2196F3, 0xFF9C27B0, 0xFFE91E63, 0xFF9E9E9E
    };


    private boolean mAdaptiveColor;
    /** 用户当前明暗选择（t51 单一真源）：true=浅色页签/浅渲染，持久化于 KEY_EDIT_LIGHT。 */
    private boolean mLightMode = false;

    /** 派生值（t51）：渲染暗色 = !mLightMode（F23-1 不变量结构性成立），night 字段已收敛删除。 */
    private boolean isNight() { return !mLightMode; }
    private TextView mTabDark, mTabLight;
    private LinearLayout mLightTabs;
    private TextView mHelpIcon;

    private android.widget.SeekBar mHueSlider, mSatSlider, mValSlider;

    /** 共享实时预览钟（vc328）：与首页同一个组件，编辑即所见。 */
    private SettingsPreviewClock mPreviewClock;
    /** HSV 取色状态机：滑杆/方块盘只负责把触摸换算成进度，写入统一走这里。 */
    private final HsvColorController mHsv = new HsvColorController();

    private LinearLayout mPresetWrap;

    /** 全量预设页（t5）：true=本页为网格管理页，构建与刷新走 all-presets 分支。 */
    private boolean mAllPage;
    /** 全量页内容容器：保存/重命名/删除后整体重建。 */
    private LinearLayout mAllWrap;

    private TextView[] mDirPills;

    /** 当前调色目标：单色页不用（-1），渐变页 0..3 = ①起点/②中间2/③中间3/④终点（t6）。 */
    private int mGradTarget = -1;

    /** 入口页参数（t3）：首页「单色/渐变」两行入口分别带入对应高级页。 */
    public static final String EXTRA_PAGE = "page";
    public static final String PAGE_MONO = "mono";
    public static final String PAGE_GRAD = "grad";
    /** 全量预设管理页（t5）：「更多 ›」胶囊进入，3 列网格 + 重命名/删除。 */
    public static final String PAGE_ALL = "all";
    /** U-20261115⑰b：全量页沿用来源页（单色/渐变）的预设类型过滤语义。 */
    public static final String PAGE_ALL_GRAD = "all_grad";

    // t22：暗/浅页签联动页面主题切换——recreate 前用 instance state 带走编辑与渲染态。
    // t44：明暗单一来源 = KEY_EDIT_LIGHT（mLightMode），night 恒等 !mLightMode（F23-1 不变量）不再单独存键；
    // KEY_SELF_THEME_RECREATE 是唯一裁决键（点页签 recreate 才恢复用户选择，其余重建跟随系统）。
    private static final String KEY_EDIT_LIGHT = "edit_light";
    private static final String KEY_SLOT_TARGET = "slot_target";
    private static final String KEY_GRAD_TARGET = "grad_target";
    /** t38④：仅「用户点页签触发的本页 recreate」置 true；其余重建（系统切换/旋转）跟随系统。 */
    private static final String KEY_SELF_THEME_RECREATE = "self_theme_recreate";
    private boolean mSelfThemeRecreate = false;

    /** 当前页：false=单色调色页，true=渐变编辑页。 */
    private boolean mGradPage;
    /** 单色页当前编辑的数字位（0..5）；取景框（t11）落地前由胶囊行选择。 */
    private int mSlotTarget;
    private TextView mEditingHex;
    private TextView[] mSlotPills;
    /** 渐变页四个圆形色标（t6）：wrap（Ripple+选中环）与下方 ①②③④ 小字。 */
    private FrameLayout[] mStopCircles;
    private TextView[] mStopLabels;
    /** 渐变页选中色圆的呼吸涟漪 Drawable（照搬首页起点/终点色点，breathTick 每 50ms 推进相位）。 */
    private BreathSwatchDrawable mGradBreath;
    /** 与首页 EDGE_SOLID_RATIO 一致：实心圆占容器比例，外部留白给涟漪扩散。 */
    private static final float GRAD_SOLID_RATIO = 14f / 24f;
    /** 渐变页端点预览条：onDraw 实时读键，写入后 invalidate 即刷新。 */
    private final java.util.concurrent.atomic.AtomicInteger mWriteSeq =
            new java.util.concurrent.atomic.AtomicInteger();
    private Handler handler = new Handler(Looper.getMainLooper());

    /** 照搬首页：选中色圆呼吸涟漪相位推进（每 50ms 一次）。 */
    private final Runnable breathTick = new Runnable() {
        @Override
        public void run() {
            long now = System.currentTimeMillis();
            if (mGradBreath != null) {
                mGradBreath.setPhase((now % BreathSwatchDrawable.RIPPLE_PERIOD_MS)
                        / (float) BreathSwatchDrawable.RIPPLE_PERIOD_MS);
            }
            handler.postDelayed(this, 50L);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        // 入口页参数（t3）：首页「单色/渐变」两行入口分别进入对应高级页
        String page = getIntent() != null ? getIntent().getStringExtra(EXTRA_PAGE) : null;
        mAllPage = PAGE_ALL.equals(page) || PAGE_ALL_GRAD.equals(page);
        mGradPage = PAGE_GRAD.equals(page) || PAGE_ALL_GRAD.equals(page);
        mGradTarget = mGradPage ? 0 : -1;
        setTitle(mAllPage ? R.string.settings_preset_all_title
                : mGradPage ? R.string.settings_advanced_grad_title
                : R.string.settings_advanced_mono_title);
        // t22：页面渲染模式。
        // t38④：系统模式自适应下，页面跟随系统 uiMode 渲染；仅在「用户点页签触发本页 recreate」的
        // 那一次恢复用户选择（KEY_SELF_THEME_RECREATE），其余重建（系统主题切换 / 旋转 / 重进）一律
        // 回落 sysNight——消除「曾点过深色页签、系统切浅色后重进仍深」的残留深色问题。
        boolean sysNight = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_EDIT_LIGHT)
                && savedInstanceState.getBoolean(KEY_SELF_THEME_RECREATE, false)) {
            // 本页内部点页签触发的 recreate：系统主题可能尚未异步切换完毕，按本次用户选择渲染。
            // t51：night 为派生值 isNight() = !mLightMode（F23-1），mLightMode 经 KEY_EDIT_LIGHT 单一来源恢复。
            mLightMode = savedInstanceState.getBoolean(KEY_EDIT_LIGHT);
            mSlotTarget = savedInstanceState.getInt(KEY_SLOT_TARGET, 0);
            if (mGradPage) {
                // F35-1：mono 页存过 -1，切回渐变页 recreate 恢复时钳回 0（起点），
                // 否则 currentTargetIndex() = DIGIT_COUNT + (-1) 会把渐变端点当单色位编辑。
                mGradTarget = Math.max(0, savedInstanceState.getInt(KEY_GRAD_TARGET, 0));
            }
        } else {
            // 首次进入 / 系统主题切换重建 / 旋转 / 重进：跟随系统模式渲染，
            // 系统切浅色后整页（含自适应下方所有卡片）统一按 night=false 重上色，并同步页签选择。
            mLightMode = !sysNight;   // night 派生 = !mLightMode（F23-1），浅色系统下高亮「浅色」页签
        }
        // 系统模式自适应默认开启（t4 已去开关）：初值仍须在 build() 前读好，暗/浅页签恒可见
        mAdaptiveColor = ClockSecondsConfig.isAdaptiveColorEnabled(getContentResolver());
        // f12-4：整页竖向可滚动（10 方向 5×2 网格在 1240x2772 首屏外）；
        // fillViewport 保证内容不足一屏时也撑满，宽度显式 MATCH_PARENT 让卡片横向铺满。
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout pageRoot = build();
        pageRoot.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(pageRoot);
        // t34 F31-1：appbar 上半段（y≤140px）被 ColorOS 状态栏窗口吞点击——页签视觉中心落在死区。
        // 顶层 ScrollView 消费状态栏 inset（自动加 paddingTop），内容整体下移出状态栏触控区；
        // inset 条带同样刷成页面底色，避免出现一条窗口背景色。
        scroll.setFitsSystemWindows(true);
        scroll.setBackgroundColor(isNight() ? 0xFF151021 : 0xFFF3F0FA);
        setContentView(scroll);
        // U-20261116c：浅色渲染时声明 LIGHT_STATUS_BAR（状态栏深字），否则 ColorOS 对本窗口
        // 用白字渲染 → 「跟随系统」位（冒号）在高级页浅色界面变白（用户反馈 bug）。
        // 与首页 ModuleSettingsActivity.applyStatusBarAppearance 同语义；页签切换走 recreate 再进 onCreate。
        applyStatusBarAppearance();
    }

    /** 按页面渲染模式声明状态栏文字深浅（浅色渲染=深字）。 */
    private void applyStatusBarAppearance() {
        android.view.View decor = getWindow().getDecorView();
        int flags = decor.getSystemUiVisibility();
        if (isNight()) {
            flags &= ~android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags |= android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 回到页面时（可能刚去改了系统深浅或被系统杀掉重建）强制同步一次：时间与配色。
        if (mPreviewClock != null) {
            mPreviewClock.setAutoTick(true);
            mPreviewClock.refreshNow();
        }
        syncSlidersToColor();
        if (mGradPage) {
            handler.removeCallbacks(breathTick);
            handler.post(breathTick);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPreviewClock != null) {
            mPreviewClock.setAutoTick(false);
        }
        handler.removeCallbacks(breathTick);
    }

    // ==================================================================
    // 布局
    // ==================================================================

    private LinearLayout build() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(28));
        root.setClipChildren(false);
        root.setClipToPadding(false);
        // t22：页面渲染模式跟随暗/浅页签（默认跟随系统）——window 背景随系统主题不变，
        // 必须给根布局显式底色才能整页切换预览模式。暗底用主题 bg，浅底用同系浅紫灰。
        GradientDrawable pageBg = new GradientDrawable();
        pageBg.setColor(isNight() ? 0xFF151021 : 0xFFF3F0FA);
        root.setBackground(pageBg);

        root.addView(appbar());

        // 全量预设管理页（t5）：只渲染暗/浅两组网格 + 各自的保存按钮
        if (mAllPage) {
            buildAllPresetsView(root);
            return root;
        }

        // 卡序（t20）：自适应 → 实时预览 → 调色盘（渐变页含方向选择）→ 我的预设。
        // 渐变页原独立「渐变卡」整块迁入调色盘卡（方向选择单行横滑），空卡壳删除。
        root.addView(card());
        buildAdaptiveRows((LinearLayout) root.getChildAt(root.getChildCount() - 1));

        root.addView(card());
        buildPreviewCard((LinearLayout) root.getChildAt(root.getChildCount() - 1));

        root.addView(card());
        buildPaletteCard((LinearLayout) root.getChildAt(root.getChildCount() - 1));

        root.addView(card());
        buildPresetCard((LinearLayout) root.getChildAt(root.getChildCount() - 1));

        return root;
    }

    private LinearLayout appbar() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("‹");
        back.setTextSize(22f);
        back.setPadding(dp(6), dp(2), dp(10), dp(2));
        back.setClickable(true);
        back.setFocusable(true);
        back.setOnClickListener(v -> finish());
        row.addView(back);
        TextView title = new TextView(this);
        title.setText(mAllPage ? R.string.settings_preset_all_title
                : R.string.settings_advanced_title);   // t26⑥：两页合并后标题统一「高级设置」
        title.setTextSize(17f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        row.addView(title);
        if (!mAllPage) {
            // t26⑥：页内「单色 / 渐变」切换（首页合并入口后两页仍可达）
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
            row.addView(spacer);
            TextView monoTab = pageTab(getString(R.string.settings_tab_mono), !mGradPage);
            monoTab.setOnClickListener(v -> switchPage(false));
            row.addView(monoTab);
            TextView gradTab = pageTab(getString(R.string.settings_tab_gradient), mGradPage);
            // t30：addView 前该 View 尚无父容器，getLayoutParams() 可能为 null，强转有 NPE 风险——
            // 先显式给全量 LayoutParams 再设间距。
            gradTab.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams glp = (LinearLayout.LayoutParams) gradTab.getLayoutParams();
            glp.leftMargin = dp(6);
            gradTab.setOnClickListener(v -> switchPage(true));
            row.addView(gradTab);
        }
        return row;
    }

    /** t26⑥：页内单色/渐变切换胶囊（样式沿用 makeTab 的选中态）。 */
    private TextView pageTab(String label, boolean selected) {
        TextView tab = new TextView(this);
        tab.setText(label);
        tab.setTextSize(12f);
        tab.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tab.setGravity(Gravity.CENTER);
        // t34 F31-1：触控区加高（上下 4→7dp），减少与状态栏死区相邻时的误触失败；视觉仅胶囊略高。
        tab.setPadding(dp(10), dp(7), dp(10), dp(7));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(999));
        if (selected) {
            bg.setColor(0xFF7C5CE0);
            tab.setTextColor(0xFFFFFFFF);
        } else {
            bg.setColor(0x00000000);
            bg.setStroke(dp(1), isNight() ? 0x37305A : 0xDDDDDD);
            tab.setTextColor(text2());
        }
        tab.setBackground(bg);
        tab.setClickable(true);
        tab.setFocusable(true);
        return tab;
    }

    /** t26⑥：切换单色/渐变页——重写 intent 页参数后 recreate（saved state 保留编辑位置）。
     *  U-20261115⑥：页签即渐变总开关（与首页 selectDigitPage 同语义）——停在渐变页=开渐变，
     *  切到单色页=关渐变；写盘成功再 recreate，状态栏时间与页内预览立即跟随单色/渐变效果。 */
    private void switchPage(boolean grad) {
        if (grad == mGradPage) {
            return;
        }
        boolean cfgOn = ClockSecondsConfig.isGradientEnabled(getContentResolver());
        if (cfgOn != grad) {
            Intent it = new android.content.Intent(this, getClass());
            it.putExtra(EXTRA_PAGE, grad ? PAGE_GRAD : PAGE_MONO);
            setIntent(it);
            Intent broadcast = new Intent(ClockSecondsConfig.ACTION_SET_GRADIENT)
                    .putExtra(ClockSecondsConfig.EXTRA_ENABLED, grad ? 1 : 0);
            applyConfig(ClockSecondsConfig.GRADIENT_ENABLED_KEY, grad ? 1 : 0, broadcast, ok -> {
                if (!ok) {
                    toastWriteFailed();
                }
                recreate();
            });
            return;
        }
        android.content.Intent it = new android.content.Intent(this, getClass());
        it.putExtra(EXTRA_PAGE, grad ? PAGE_GRAD : PAGE_MONO);
        setIntent(it);
        recreate();
    }

    // ---------------- ① 系统模式自适应 ----------------

    private void buildAdaptiveRows(LinearLayout card) {
        // 标题行 = 名称 + 问号帮助（开关已移除：系统模式自适应默认开启）
        LinearLayout title = new LinearLayout(this);
        title.setOrientation(LinearLayout.HORIZONTAL);
        title.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = new TextView(this);
        t.setText(R.string.settings_adaptive_color);
        t.setTextSize(13f);
        t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        t.setTextColor(text());
        title.addView(t);
        mHelpIcon = new TextView(this);
        mHelpIcon.setText("?");
        mHelpIcon.setTextSize(12f);
        mHelpIcon.setTypeface(Typeface.create("sans-serif-bold", Typeface.NORMAL));
        mHelpIcon.setTextColor(accent());
        GradientDrawable qbg = new GradientDrawable();
        qbg.setCornerRadius(dp(999));
        qbg.setColor(isNight() ? 0x5CA98CFF : 0x1F6C4CF1);
        qbg.setStroke(dp(1), isNight() ? 0xFFA98CFF : 0xFF6C4CF1);
        mHelpIcon.setBackground(qbg);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(
                dp(17), dp(17));
        qlp.leftMargin = dp(6);
        mHelpIcon.setLayoutParams(qlp);
        mHelpIcon.setPadding(0, 0, 0, 1);
        mHelpIcon.setGravity(Gravity.CENTER);
        mHelpIcon.setOnClickListener(v -> showAdaptiveHelp());
        title.addView(mHelpIcon);

        card.addView(title);

        // 解释气泡（用静态弹窗更稳：避免自定义 PopupWindow 兼容性问题）
        mHelpIcon.setOnLongClickListener(v -> {
            showAdaptiveHelp();
            return true;
        });

        mLightTabs = new LinearLayout(this);
        mLightTabs.setOrientation(LinearLayout.HORIZONTAL);
        mTabDark = makeTab(getString(R.string.settings_tab_dark));
        mTabLight = makeTab(getString(R.string.settings_tab_light));
        mTabDark.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        llp.leftMargin = dp(3);
        mTabLight.setLayoutParams(llp);
        mTabDark.setOnClickListener(v -> setLightMode(false));
        mTabLight.setOnClickListener(v -> setLightMode(true));
        mLightTabs.addView(mTabDark);
        mLightTabs.addView(mTabLight);
        GradientDrawable trackBg = new GradientDrawable();
        trackBg.setCornerRadius(dp(999));
        trackBg.setColor(isNight() ? 0xFF2A2340 : 0xFFF1EDFB);
        mLightTabs.setBackground(trackBg);
        mLightTabs.setPadding(dp(3), dp(3), dp(3), dp(3));
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tl.topMargin = dp(10);
        mLightTabs.setLayoutParams(tl);
        card.addView(mLightTabs);

        refreshAdaptiveUi();
    }

    private void showAdaptiveHelp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_adaptive_color)
                .setMessage(R.string.settings_adaptive_help)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    /** t26④：圆圈「?」帮助图标（样式复刻自适应卡 mHelpIcon），点击/长按都触发提示。 */
    private TextView makeHelpIcon(android.view.View.OnClickListener onClick) {
        TextView help = new TextView(this);
        help.setText("?");
        help.setTextSize(12f);
        help.setTypeface(Typeface.create("sans-serif-bold", Typeface.NORMAL));
        help.setTextColor(accent());
        GradientDrawable qbg = new GradientDrawable();
        qbg.setCornerRadius(dp(999));
        qbg.setColor(isNight() ? 0x5CA98CFF : 0x1F6C4CF1);
        qbg.setStroke(dp(1), isNight() ? 0xFFA98CFF : 0xFF6C4CF1);
        help.setBackground(qbg);
        LinearLayout.LayoutParams qlp = new LinearLayout.LayoutParams(dp(17), dp(17));
        qlp.leftMargin = dp(6);
        help.setLayoutParams(qlp);
        help.setPadding(0, 0, 0, 1);
        help.setGravity(Gravity.CENTER);
        help.setOnClickListener(onClick);
        help.setOnLongClickListener(v -> {
            onClick.onClick(v);
            return true;
        });
        return help;
    }

    /** t26④：预设帮助弹窗——长按预设颜色可重命名或删除。 */
    private void showPresetHelp() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_preset_title)
                .setMessage(R.string.settings_preset_help)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void setLightMode(boolean light) {
        // 稳态（t51：night 为派生值，F23-1 恒成立）：重复点当前页签不重建
        if (mLightMode == light) {
            return;
        }
        mLightMode = light;
        // t22：页面渲染模式同步切到页签对应值（recreate 后经 KEY_EDIT_LIGHT 单一来源恢复）。
        // t38④ 起恢复用户选择由 KEY_SELF_THEME_RECREATE 裁决（t44：mThemeTouched 已并入该机制删除）。
        // F23-1（t51）：night 为派生值 isNight() = !mLightMode（UI_MODE_NIGHT_YES 语义，暗渲染），
        // 点「浅色」→ mLightMode=true → night=false 浅渲染；点「暗色」→ mLightMode=false → night=true 暗渲染。
        refreshAdaptiveUi();
        // t28：页签不仅切设置页内渲染，还广播给模块（SystemUI 特权上下文）切换手机系统全局主题。
        // 设置页自身 UI 立即跟随（本方法 + recreate），系统主题切换由模块异步完成——
        // 模块收到后调 UiModeManager.setNightMode，异常时回落 Settings.Secure.UI_NIGHT_MODE。
        sendBroadcast(new Intent(ClockSecondsConfig.ACTION_SWITCH_THEME)
                .putExtra(ClockSecondsConfig.EXTRA_NIGHT, !light)
                .setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE));
mSelfThemeRecreate = true;   // t38④：标记本次 recreate 来自点页签，onCreate 据此恢复用户选择
        toast(R.string.settings_theme_switching);
        // t22：页签既切编辑对象（暗键组/浅副键组），也把整页渲染切到对应模式——
        // 模块切系统主题后本页 uiMode 也会跟随；recreate 保证 card/accent/text/预览钟取色
        // 按新模式分支渲染，数据从 Secure 重读，编辑对象/页签/目标选择经
        // onSaveInstanceState 恢复不丢失。
        recreate();
    }

    @Override
    protected void onSaveInstanceState(android.os.Bundle outState) {
        super.onSaveInstanceState(outState);
        // t22：recreate（点暗/浅页签）或系统回收重建时带走状态，避免编辑对象/页签/目标位丢失
        outState.putBoolean(KEY_EDIT_LIGHT, mLightMode);
        outState.putInt(KEY_SLOT_TARGET, mSlotTarget);
        outState.putInt(KEY_GRAD_TARGET, mGradTarget);
        // t38④：仅在本次 onSaveInstanceState 是「点页签 recreate」触发时写 true；写完即复位，
        // 使后续任何重建（旋转/系统切换/重进）都回落系统模式。
        outState.putBoolean(KEY_SELF_THEME_RECREATE, mSelfThemeRecreate);
        mSelfThemeRecreate = false;
    }

    private void refreshAdaptiveUi() {
        if (mLightTabs != null) {
            // t4：自适应常驻开启，暗/浅页签（决定编辑哪套色）不再随开关显隐
            mLightTabs.setVisibility(View.VISIBLE);
        }
        if (mTabDark != null && mTabLight != null) {
            styleTab(mTabDark, !mLightMode);
            styleTab(mTabLight, mLightMode);
        }
    }

    // ---------------- ② 单色调色盘（HSV） ----------------

    /** 实时预览钟（vc328）：显示 18:17:07 样式的时间，写入颜色立即反映。 */
    private void buildPreviewCard(LinearLayout card) {
        mPreviewClock = new SettingsPreviewClock(this);
        mPreviewClock.setTextSize(34f);
        mPreviewClock.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mPreviewClock.getPaint().setFakeBoldText(true);
        mPreviewClock.setFontFeatureSettings("tnum");
        mPreviewClock.setGravity(Gravity.CENTER_HORIZONTAL);
        mPreviewClock.setTextColor(text());
        // 取景框（t11）：单色页框住当前编辑位；颜色跟 accent，随明暗页签在 refreshPreview 里刷新
        mPreviewClock.setViewfinderColor(accent());
        mPreviewClock.setPadding(0, dp(6), 0, dp(2));
        // 预览状态提供器：与首页 updatePreview 同一套规则（渐变开→整条渐变；否则逐位上色）。
        mPreviewClock.setTimeProvider(now -> {
            CharSequence t = previewTimeText();
            if (ClockSecondsConfig.isGradientEnabled(getContentResolver())
                    && ClockSecondsConfig.isHourColorEnabled(getContentResolver())) {
                mPreviewClock.setGradientDirection(
                        ClockSecondsConfig.getGradientDirection(getContentResolver()));
                mPreviewClock.setGradientColors(previewStops());
                return t;
            }
            mPreviewClock.clearGradient();
            android.text.SpannableStringBuilder sb =
                    new android.text.SpannableStringBuilder(t);
            for (int i = 0; i < sb.length(); i++) {
                if (sb.charAt(i) == ':' || sb.charAt(i) == '：') {
                    sb.setSpan(new io.github.kelebao.colorosclockseconds.ColonLiftSpan(),
                            i, i + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    continue;
                }
                int slot = ClockSecondsConfig.slotOfChar(sb, i);
                if (slot < 0) {
                    continue;
                }
                // U-20261115⑬：生效色解析抽到 effectiveSlotPreviewColor（与模块 effectiveSlotColor
                // 同语义：epoch+背景亮度门、FOLLOW 位不读副键），单色/渐变两个预览共用。
                int color = effectiveSlotPreviewColor(slot);
                if (!ClockSecondsConfig.isHourColorEnabled(getContentResolver())
                        || color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
                    continue;
                }
                sb.setSpan(new android.text.style.ForegroundColorSpan(previewAdapt(color)),
                        i, i + 1, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            return sb;
        });
        if (!mGradPage) {
            // 单色页（t11）：点预览钟上任意数字位 = 切到那一位编辑（跟点下方胶囊同一入口）
            mPreviewClock.setOnActiveDigitClickListener(slot -> setSlotTarget(slot));
        }
        card.addView(mPreviewClock);
        // t36①：渐变页大预览条已删除——渐变效果直接看上方预览钟（编辑即所见），
        // 两条渐变展示重复且占空间。

        if (!mGradPage) {
            // 取景框（t11）：初始框住当前编辑位
            mPreviewClock.setActiveDigit(mSlotTarget);
        }
    }

    /**
     * 预览钟某一位的生效色（U-20261115⑫，U-20261116b 语义重排）：
     * 浅色副键真色优先；副键 FOLLOW(0)=浅色下该位跟随系统文字色（U-20261116x）；null 回落常规键。模块 effectiveSlotColor 同语义。
     */
    private int effectiveSlotPreviewColor(int slot) {
        int[] colors = ClockSecondsConfig.getDigitColors(getContentResolver());
        int normal = colors != null && colors.length > slot
                ? colors[slot] : ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
        if (lightPaletteActive()) {
            Integer light = ClockSecondsConfig.getDigitLightColor(getContentResolver(), slot);
            if (light != null && light != ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
                return light;
            }
            // U-20261116x（浅色模式点「跟随系统」改不回默认色）：副键显式 FOLLOW(0)
            // = 浅色下该位跟随系统文字色；null（从未设置）才回落常规键。与模块同语义。
            if (light != null) {
                return ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
            }
        }
        return normal;
    }

    /**
     * 浅色调色板当前是否参与显示（U-20261115⑭）：与模块 effectiveSlotColor/gradientRawStart/End
     * 完全同语义——epoch 已置位 && 自适应开 && 系统浅色模式。
     * 页签（mLightMode）只决定「编辑哪一组键」，不决定「显示哪一组键」；
     * 页面进程 Resources 的 uiMode 可靠（与 SystemUI 不同），直接按配置判定。
     */
    private boolean lightPaletteActive() {
        if (!mAdaptiveColor
                || !ClockSecondsConfig.isLightPaletteUsed(getContentResolver())) {
            return false;
        }
        return ClockSecondsConfig.isSystemLightMode(getContentResolver(),
                (getResources().getConfiguration().uiMode
                        & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                        != android.content.res.Configuration.UI_MODE_NIGHT_YES);
    }

    /**
     * 预览用系统文字色（U-20261115⑫）：模块每秒把状态栏真实文字色写进 sys_text_color，
     * 这里直接复用同一样本——预览与状态栏共用同一判据。无采样时两个页签都用浅色兜底
     * （亮度 > 0.5 → 不触发压暗），避免浅色页签凭空把接近白的颜色压暗而与状态栏对不上。
     */
    private int previewAdapt(int color) {
        int sys = ClockSecondsConfig.getSystemTextColor(getContentResolver(),
                0xFFEAE6F8);
        if (color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
            // U-20261116x：FOLLOW(0) 直接 adaptColorForBackground 会算出全透明，预览断色。
            return sys;
        }
        return ClockSecondsConfig.adaptColorForBackground(color, sys);
    }

    /**
     * 当前显示的渐变端点原始色（不做预览压暗）：浅色调色板激活时优先浅色副键，
     * 否则读常规键；AUTO 回落首/末位数字生效色（与模块 gradientRawStart/End 同语义）。
     * 保存预设与预览共用此判据，保证「保存的就是看到的」。
     */
    private int[] displayedGradientStops() {
        // U-20261115⑪：浅色调色板启用且当前浅色页签时，端点优先读浅色副键（与模块
        // gradientRawStart/End 同语义），否则读常规键——保证预览与状态栏渐变一致。
        boolean useLight = lightPaletteActive();   // U-20261115⑬：与模块同语义
        Integer l0 = useLight
                ? ClockSecondsConfig.getGradientStopLightColor(getContentResolver(), 0) : null;
        Integer l3 = useLight
                ? ClockSecondsConfig.getGradientStopLightColor(getContentResolver(), 3) : null;
        int c0 = l0 != null ? l0
                : ClockSecondsConfig.getGradientStopColor(getContentResolver(), 0);
        int c3 = l3 != null ? l3
                : ClockSecondsConfig.getGradientStopColor(getContentResolver(), 3);
        int s0 = c0 != ClockSecondsConfig.GRADIENT_COLOR_AUTO ? c0 : effectiveSlotPreviewColor(0);
        int s3 = c3 != ClockSecondsConfig.GRADIENT_COLOR_AUTO ? c3 : effectiveSlotPreviewColor(
                ClockSecondsConfig.DIGIT_COUNT - 1);
        return new int[]{s0, s3};
    }

    /**
     * 预览渐变端点（t36②：渐变回 2 色，只取 start/end；AUTO 按默认回落），
     * 传给预览钟的数组即 2 端点——渲染天然走线性两色。
     */
    private int[] previewStops() {
        int[] stops = displayedGradientStops();
        return new int[]{previewAdapt(stops[0]), previewAdapt(stops[1])};
    }

    /** 预设条插值（ARGB 线性，仅 UI 用）。 */
    private static int lerpPreviewColor(int from, int to, float t) {
        int a = (int) (android.graphics.Color.alpha(from)
                + (android.graphics.Color.alpha(to) - android.graphics.Color.alpha(from)) * t);
        int r = (int) (android.graphics.Color.red(from)
                + (android.graphics.Color.red(to) - android.graphics.Color.red(from)) * t);
        int g = (int) (android.graphics.Color.green(from)
                + (android.graphics.Color.green(to) - android.graphics.Color.green(from)) * t);
        int b = (int) (android.graphics.Color.blue(from)
                + (android.graphics.Color.blue(to) - android.graphics.Color.blue(from)) * t);
        return android.graphics.Color.argb(a, r, g, b);
    }

    /** 预览时间文本：显秒开 → HH:mm:ss（如 18:17:07），关 → HH:mm。 */
    private CharSequence previewTimeText() {
        int effSec = ClockSecondsConfig.getEffectiveSeconds(getContentResolver());
        // 与首页 updatePreview 同一套语义：任务窗口覆盖优先，没覆盖时回落总开关。
        boolean showSec = effSec >= 0 ? effSec == 1
                : ClockSecondsConfig.isEnabled(getContentResolver());
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(
                showSec ? "HH:mm:ss" : "HH:mm", java.util.Locale.US);
        return fmt.format(new java.util.Date());
    }

    /** 写入/切页签/应用预设后立即重画预览钟（编辑即所见）。 */
    private void refreshPreview() {
        if (mPreviewClock != null) {
            // 取景框（t11）：单色页跟随编辑位，渐变页不显示；颜色随明暗页签
            mPreviewClock.setActiveDigit(mGradPage ? -1 : mSlotTarget);
            mPreviewClock.setViewfinderColor(accent());
            mPreviewClock.refreshNow();
        }
    }

    /** 渐变 UI 色标数（t36②：渐变回 2 色，仅起点/终点；GRADIENT_STOP_COUNT=4 的中间键不再使用）。 */
    private static final int GRAD_UI_STOPS = 2;

    /**
     * UI 色标序号 → Secure 键序号（t36②）：UI 只有 ①起点/②终点，
     * 而 Config.gradientStopKey 的 1/2 是四色时代的中间键——终点必须映射到 3（end）。
     */
    private static int stopKeyIndex(int uiIndex) {
        return uiIndex == 0 ? 0 : 3;
    }

    /** 正在编辑的目标键文案：单色页=第 N 位，渐变页=①起点/②终点（t36② 两色标；浅色页签加 · Light）。 */
    private String targetLabel() {
        String base;
        if (mGradPage) {
            String circle = new String(Character.toChars(0x2460 + Math.max(0, Math.min(GRAD_UI_STOPS - 1, mGradTarget))));
            base = circle + " " + getString(mGradTarget == GRAD_UI_STOPS - 1
                    ? R.string.settings_gradient_end : R.string.settings_gradient_start);
        } else {
            String[] slots = getString(R.string.settings_digit_slots).split(",");
            base = slots.length > mSlotTarget ? slots[mSlotTarget] : slots[0];
        }
        if (mAdaptiveColor && mLightMode) {
            base += " · " + getString(R.string.settings_tab_light);
        }
        return base;
    }

    private void buildPaletteCard(LinearLayout card) {
        // t20③：「+ 保存当前」从「我的预设」标题行移到「调色盘」标题行右侧（两页通用）
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText(R.string.settings_palette_title);
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        titleRow.addView(title);
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        titleRow.addView(spacer);
        titleRow.addView(makePillButton("+ " + getString(R.string.settings_preset_save),
                v -> saveCurrentColors(mLightMode)));
        card.addView(titleRow);

        TextView hint = new TextView(this);
        hint.setText(mGradPage ? R.string.settings_advanced_grad_sum
                : R.string.settings_advanced_mono_sum);
        hint.setTextSize(11f);
        hint.setTextColor(text2());
        hint.setPadding(0, dp(4), 0, 0);
        card.addView(hint);

        // 编辑对象选择：单色页=6 个数字位胶囊；渐变页=①起点/②终点 两个圆形色标（t36② 渐变回 2 色）
        LinearLayout selRow = new LinearLayout(this);
        selRow.setOrientation(LinearLayout.HORIZONTAL);
        selRow.setGravity(Gravity.CENTER_VERTICAL);
        // t40 U40-2：涟漪 overlay 不能挂在 selRow——LinearLayout 按子顺序排布，会把
        // MATCH_PARENT 的涟漪层推到行尾之外（单色页挂 FrameLayout 叠层所以正常，渐变页
        // 因此从未画出来）。包一层 rippleHost（FrameLayout 叠层语义）做涟漪宿主，
        // 并关闭两层裁切让涟漪扩散到圆圈之外仍完整可见。
        selRow.setClipChildren(false);
        final FrameLayout rippleHost = new FrameLayout(this);
        rippleHost.setClipChildren(false);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sl.topMargin = dp(14);
        rippleHost.setLayoutParams(sl);
        if (mGradPage) {
            // 两个圆角色标（t36② 渐变回 2 色）：照搬首页起点/终点色点效果——底色=当前色标颜色，
            // 选中=常驻呼吸涟漪（BreathSwatchDrawable，breathTick 每 50ms 推进相位，与首页一致）。label 用带圈数字 ①②。
            mStopCircles = new FrameLayout[GRAD_UI_STOPS];
            mStopLabels = new TextView[GRAD_UI_STOPS];
            for (int i = 0; i < GRAD_UI_STOPS; i++) {
                FrameLayout wrap = new FrameLayout(this);
                // 照搬首页起点/终点色点效果：占位灰圆（无呼吸），refreshStopCircles 用真实色重画并决定是否呼吸。
                BreathSwatchDrawable placeholder = new BreathSwatchDrawable(
                        isNight() ? 0xFF3A3152 : 0xFFD8D4E8, false, accent(), 0f, GRAD_SOLID_RATIO);
                wrap.setBackground(placeholder);
                wrap.setClickable(true);
                wrap.setFocusable(true);
                final int which = i;
                wrap.setOnClickListener(v -> setGradTarget(which));
                // t18③ + 本版：圆直径放大到 44dp（与首页起点色圆同比例），给呼吸涟漪留出扩散空间。
                LinearLayout.LayoutParams wl = new LinearLayout.LayoutParams(dp(44), dp(44));
                wl.rightMargin = dp(10);
                wrap.setLayoutParams(wl);
                selRow.addView(wrap);
                mStopCircles[i] = wrap;

                TextView num = new TextView(this);
                num.setText(new String(Character.toChars(0x2460 + i)));   // ①②
                num.setTextSize(11f);
                num.setGravity(Gravity.CENTER);
                num.setTextColor(text());
                num.setPadding(0, dp(6), 0, 0);
                selRow.addView(num);
                mStopLabels[i] = num;
            }
        } else {
            final String[] slots = getString(R.string.settings_digit_slots).split(",");
            mSlotPills = new TextView[slots.length];
            for (int i = 0; i < slots.length; i++) {
                final int slot = i;
                // t38①：数字位胶囊外包一层 FrameLayout，便于叠加可见涟漪（与 grad 圆圈同结构）
                FrameLayout wrap = new FrameLayout(this);
                TextView pill = new TextView(this);
                pill.setText(slots[i]);
                pill.setTextSize(11f);
                pill.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                pill.setGravity(Gravity.CENTER);
                pill.setPadding(dp(9), dp(5), dp(9), dp(5));
                pill.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                wrap.addView(pill);
                wrap.setClickable(true);
                wrap.setFocusable(true);
                wrap.setOnClickListener(v -> setSlotTarget(slot));
                // t38① / t40 U40-2：选中涟漪与 grad 圆圈共用 rippleHost 叠层宿主，
                // 坐标按 wrap 在 selRow 中的偏移换算；挂 wrap 自身会被其裁切挡住扩散环。
                wrap.setOnTouchListener((v, ev) -> {
                    if (ev.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        startStopRipple(rippleHost, wrap.getLeft() + ev.getX(),
                                wrap.getTop() + ev.getY());
                    }
                    return false;   // 不消费，click 照常触发
                });
                LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                plp.rightMargin = dp(6);
                plp.bottomMargin = dp(4);
                wrap.setLayoutParams(plp);
                selRow.addView(wrap);
                mSlotPills[i] = pill;
            }
        }
        rippleHost.addView(selRow, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        card.addView(rippleHost);

        // 「正在编辑第 N 位 / 起点 #RRGGBB」行（R3 单色页样式）
        mEditingHex = new TextView(this);
        mEditingHex.setTextSize(12f);
        mEditingHex.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        mEditingHex.setTextColor(text2());
        LinearLayout.LayoutParams el = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        el.topMargin = dp(10);
        mEditingHex.setLayoutParams(el);
        card.addView(mEditingHex);

        // 三条 HSV 滑杆：色相 / 饱和度 / 明度，拖动即把当前选中目标改成对应颜色
        seedController();
        final float[] hsv = {mHsv.getHue(), mHsv.getSat(), mHsv.getVal()};
        mHueSlider = addHsvSlider(card, 0, mHsv.hueProgress());
        mSatSlider = addHsvSlider(card, 1, mHsv.satProgress());
        mValSlider = addHsvSlider(card, 2, mHsv.valProgress());
        updateSliderGradients(hsv[0], hsv[1], hsv[2]);
        refreshEditingRow();

        // 滑杆（或之后的 2D 方块盘）推状态 → 更新条形渐变 → 写入 → 预览立即反映
        mHsv.setListener((color, h, s, v) -> {
            updateSliderGradients(h, s, v);
            writeCurrentTarget(color);
            refreshPreview();
        });

        final android.widget.SeekBar.OnSeekBarChangeListener l =
                new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(android.widget.SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                // 取色状态统一收口到 HsvColorController（vc328）：三条滑杆只是它的一个 UI 形态。
                mHsv.applyProgress(mHueSlider.getProgress(),
                        mSatSlider.getProgress(), mValSlider.getProgress());
            }
            @Override public void onStartTrackingTouch(android.widget.SeekBar sb) { }
            @Override public void onStopTrackingTouch(android.widget.SeekBar sb) { }
        };
        mHueSlider.setOnSeekBarChangeListener(l);
        mSatSlider.setOnSeekBarChangeListener(l);
        mValSlider.setOnSeekBarChangeListener(l);
        // t20①②：渐变页方向选择迁入调色盘卡（原独立「渐变卡」已删），单行横滑。
        if (mGradPage) {
            buildDirectionRow(card);
        }
        styleTargets();
    }

    /**
     * t20①②：方向选择行（原 buildGradientCard 的方向区迁移版）。
     * 单行 HorizontalScrollView 横排 10 个方向胶囊，可滑动查看；当前选中项
     * 进入页面时自动滚入视野。点选写 secure gradient_direction 链路不变。
     */
    private void buildDirectionRow(LinearLayout card) {
        // 方向标题行：名称 + 「10」徽标（沿用原渐变卡样式）
        LinearLayout dirTitle = new LinearLayout(this);
        dirTitle.setOrientation(LinearLayout.HORIZONTAL);
        dirTitle.setGravity(Gravity.CENTER_VERTICAL);
        TextView dirLabel = new TextView(this);
        dirLabel.setText(R.string.settings_gradient_dir);
        dirLabel.setTextSize(12f);
        dirLabel.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        dirLabel.setTextColor(text2());
        dirTitle.addView(dirLabel);
        View spacer = new View(this);
        LinearLayout.LayoutParams spl = new LinearLayout.LayoutParams(0, 1, 1f);
        dirTitle.addView(spacer, spl);
        TextView countBadge = new TextView(this);
        // U-20261118①：徽标显示 UI 实际暴露的方向数（2 基础 + 2 特效 = 4），不再等于 COUNT。
        countBadge.setTextSize(10f);
        countBadge.setTextColor(text2());
        GradientDrawable badgeBg = new GradientDrawable();
        badgeBg.setCornerRadius(dp(999));
        badgeBg.setStroke(dp(1), isNight() ? 0x37305A : 0xDDDDDD);
        countBadge.setBackground(badgeBg);
        countBadge.setPadding(dp(8), dp(2), dp(8), dp(2));
        dirTitle.addView(countBadge);
        LinearLayout.LayoutParams dtl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dtl.topMargin = dp(12);
        dirTitle.setLayoutParams(dtl);
        card.addView(dirTitle);

        // 单行横滑：U-20261118① 方向精简 + U-20261118③ 新形式——
        // 上→下 / 左→右 两个基础方向，象限光晕 Mesh / 颗粒噪点 Grain 两个特效方向，
        // 镀铬金属 Chrome / 斜纹糖果 Stripes / 波浪切割 Wave-Slice 三个新形式方向；
        // 旧 dir 2..9 与已收编特效键值保留（老预设仍能正确回显），渲染统一走 GradientShaderFactory。
        mDirPills = new TextView[ClockSecondsConfig.GRADIENT_DIRECTION_COUNT];
        final int[] dirOrder = {
                ClockSecondsConfig.GRADIENT_DIRECTION_VERTICAL,
                ClockSecondsConfig.GRADIENT_DIRECTION_HORIZONTAL,
                ClockSecondsConfig.GRADIENT_DIRECTION_MESH_QUADRANT,
                ClockSecondsConfig.GRADIENT_DIRECTION_GRAIN,
                ClockSecondsConfig.GRADIENT_DIRECTION_CHROME,
                ClockSecondsConfig.GRADIENT_DIRECTION_STRIPES,
                ClockSecondsConfig.GRADIENT_DIRECTION_WAVE_SLICE,
                ClockSecondsConfig.GRADIENT_DIRECTION_LOW_POLY,
                ClockSecondsConfig.GRADIENT_DIRECTION_TERRAZZO,
                ClockSecondsConfig.GRADIENT_DIRECTION_QR_CODE,
                ClockSecondsConfig.GRADIENT_DIRECTION_MERCURY
        };
        final String[] dirLabels = {
                getString(R.string.settings_gradient_dir_v),
                getString(R.string.settings_gradient_dir_h),
                getString(R.string.settings_gradient_dir_mesh_quadrant),
                getString(R.string.settings_gradient_dir_grain),
                getString(R.string.settings_gradient_dir_chrome),
                getString(R.string.settings_gradient_dir_stripes),
                getString(R.string.settings_gradient_dir_wave_slice),
                getString(R.string.settings_gradient_dir_low_poly),
                getString(R.string.settings_gradient_dir_terrazzo),
                getString(R.string.settings_gradient_dir_qr_code),
                getString(R.string.settings_gradient_dir_mercury)
        };
        countBadge.setText(dirOrder.length + "");   // U-20261118①③④⑤⑥：2 基础 + 2 特效 + 7 新形式 = 11
        final int currentDir = ClockSecondsConfig.getGradientDirection(getContentResolver());
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int idx = 0; idx < dirOrder.length; idx++) {
            TextView pill = new TextView(this);
            pill.setText(dirLabels[idx]);
            pill.setTextSize(11f);
            pill.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            pill.setGravity(Gravity.CENTER);
            pill.setPadding(dp(10), dp(6), dp(10), dp(6));
            LinearLayout.LayoutParams plp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            plp.rightMargin = dp(6);
            pill.setLayoutParams(plp);
            pill.setClickable(true);
            pill.setFocusable(true);
            final int dir = dirOrder[idx];
            pill.setOnClickListener(v -> applyGradientDirection(dir));
            row.addView(pill);
            mDirPills[dir] = pill;   // 按方向值索引：refreshDirPills(dir) 直接命中
        }
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipChildren(false);
        hs.setClipToPadding(false);
        hs.addView(row);
        // t26⑤ + t38② + t42 + B：可滑动提示移到「渐变方向」标题行（与方向数徽标同排），不再压住方向胶囊；
        // 首次横向滑动后淡出；浅/深色下都清晰可读。
        final TextView swipeHint = new TextView(this);
        // U-20261115②：只保留动态箭头本体（「左右滑动」文字取消），箭头自身往返平移示意。
        // U-20261115③/④：放大加粗 + 全不透明高对比色 + 去掉 alpha 呼吸，解决「太小、不清晰」。
        swipeHint.setText("⇆");
        // U-20261115⑤：箭头恢复以前的大小（22f 常规字重），仅保留动态往返动画。
        swipeHint.setTextSize(22f);
        swipeHint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        swipeHint.setTextColor(isNight() ? 0xFFCBBBFF : 0xFF7C5CE0);
        swipeHint.setGravity(Gravity.CENTER_VERTICAL);
        swipeHint.setPadding(dp(10), 0, dp(4), 0);
        dirTitle.addView(swipeHint);

        // 动态箭头提醒：常驻不淡出，箭头做左右往返平移 + 轻微缩放脉动；
        // 文字始终 alpha=1f，保证任何时刻都清晰可读。
        final android.animation.ValueAnimator hintAnim =
                android.animation.ValueAnimator.ofFloat(0f, 1f);
        hintAnim.setRepeatCount(android.animation.ValueAnimator.INFINITE);
        hintAnim.setRepeatMode(android.animation.ValueAnimator.REVERSE);
        hintAnim.setDuration(1100);
        hintAnim.addUpdateListener(a -> {
            float k = (float) a.getAnimatedValue();   // 0..1 往返
            swipeHint.setTranslationX(dp(14) * (k - 0.5f) * 2f);
            float pulse = 0.92f + 0.08f * (1f - Math.abs(k - 0.5f) * 2f);
            swipeHint.setScaleX(pulse);
            swipeHint.setScaleY(pulse);
        });
        hintAnim.start();
        // 页面销毁时停掉无限动画，避免泄漏
        swipeHint.addOnAttachStateChangeListener(new android.view.View.OnAttachStateChangeListener() {
            @Override public void onViewAttachedToWindow(android.view.View v) { }
            @Override public void onViewDetachedFromWindow(android.view.View v) { hintAnim.cancel(); }
        });

        LinearLayout.LayoutParams hl = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hl.topMargin = dp(7);
        hs.setLayoutParams(hl);
        card.addView(hs);

        refreshDirPills(currentDir);
        // t20②：进入页面把选中胶囊滚入视野（首帧布局完成后按选中 pill 的右缘滚动）
        final TextView sel = mDirPills[currentDir];
        hs.post(() -> {
            if (sel == null || hs.getWidth() <= 0) {
                return;
            }
            int right = (sel.getLeft() + sel.getWidth()) - hs.getWidth() / 2 + sel.getWidth() / 2;
            hs.smoothScrollTo(Math.max(0, right), 0);
        });
    }

    /**
     * t38①：选中涟漪。在 host 本体内叠一个 StopRippleView（独立文件）overlay，以触点为中心、
     * t42①：样式改为参考图的「多层同心环扩散」（两圈相位错开的圆环依次扩散、同色渐隐）。
     * 动画结束移除 overlay 无残留；单色页数字位胶囊与渐变页色标圆圈共用同一 helper。
     *
     * @param host 被点击的容器（FrameLayout 叠层容器，不裁切子 View），涟漪叠在其上、扩到本体之外
     * @param cx   触点 x（host 本地坐标，即触摸事件的 getX()）
     * @param cy   触点 y
     */
    private void startStopRipple(ViewGroup host, float cx, float cy) {
        // 同一时间只保留一个涟漪：旧的立即移除
        for (int k = host.getChildCount() - 1; k >= 0; k--) {
            if (host.getChildAt(k) instanceof StopRippleView) {
                host.removeView(host.getChildAt(k));
            }
        }
        // 核心半径取本体较短边的一半，至少 14dp，保证扩散起点足够大、肉眼可见
        final int core = Math.min(host.getWidth(), host.getHeight()) / 2;
        final float baseR = Math.max(core, dp(14));
        final StopRippleView overlay = new StopRippleView(this, cx, cy, baseR, 0xFF7C5CE0);
        // t40 U40-2：宿主统一为 FrameLayout 叠层容器（mono 页=胶囊外框 wrap、
        // 渐变页=rippleHost），涟漪层 MATCH_PARENT 铺满宿主、画在子顺序最上层。
        // 不能用 LinearLayout 当宿主：它按子顺序排布，MATCH_PARENT 的涟漪层会被
        // 推到行尾之外，涟漪从未出现在可见区（这正是渐变页涟漪看不见的最终根因）。
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setClickable(false);
        overlay.setFocusable(false);
        host.addView(overlay);
        final android.animation.ValueAnimator anim =
                android.animation.ValueAnimator.ofFloat(0f, 1f);
        // t42①：560ms——第二圈滞后约半圈相位，时长保证两圈都完整走完扩散+渐隐。
        anim.setDuration(560);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator(1.4f));
        anim.addUpdateListener(a -> overlay.setProgress((float) a.getAnimatedValue()));
        anim.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                host.removeView(overlay);   // 动画结束不残留
            }
        });
        anim.start();
    }

    /** 当前编辑目标对应的 Secure 键目标：单色页=数字位，渐变页=端点。 */
    private int currentTargetIndex() {
        return mGradPage ? ClockSecondsConfig.DIGIT_COUNT + mGradTarget : mSlotTarget;
    }

    /** 用当前目标颜色重置 HSV 状态机（不触发写入）。 */
    private void seedController() {
        mHsv.setFromColor(effectiveColor(currentTargetIndex()));
    }

    /** 把取色结果写入当前编辑目标。 */
    private void writeCurrentTarget(int color) {
        if (mGradPage) {
            writeGradientColor(mGradTarget, color);
        } else {
            writeDigitColor(mSlotTarget, color);
        }
    }

    /** 切换单色页正在编辑的数字位。 */
    private void setSlotTarget(int slot) {
        if (mSlotTarget == slot) {
            return;
        }
        mSlotTarget = slot;
        seedController();
        syncSlidersToColor();
        refreshEditingRow();
        styleTargets();
        refreshPreview();
    }

    /** 切换渐变页正在编辑的端点。 */
    private void setGradTarget(int which) {
        if (mGradTarget == which) {
            return;
        }
        mGradTarget = which;
        seedController();
        syncSlidersToColor();
        refreshEditingRow();
        styleTargets();
        refreshPreview();
    }

    /** 「正在编辑… #RRGGBB」行：切目标/写入后刷新。 */
    private void refreshEditingRow() {
        if (mEditingHex == null) {
            return;
        }
        int color = effectiveColor(currentTargetIndex());
        // t34 F31-2：未自定义的位（FOLLOW=0）首帧显示 #00000000 是假值——兜底显示 follow 文案；
        // 浅色页签下读副键为空回落常规键同样可能 0，一并兜底。
        String hex = color == ClockSecondsConfig.DIGIT_COLOR_FOLLOW
                ? getString(R.string.settings_digit_follow)
                : hexOf(color);
        mEditingHex.setText(getString(R.string.settings_digit_editing, targetLabel())
                + "  " + hex);
    }

    /** 目标选择胶囊选中态：选中=accent 实底白字，未选中=描边。 */
    private void styleTargets() {
        if (mGradPage && mStopCircles != null) {
            refreshStopCircles();
            return;
        }
        if (!mGradPage && mSlotPills != null) {
            for (int i = 0; i < mSlotPills.length; i++) {
                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(dp(999));
                if (i == mSlotTarget) {
                    bg.setColor(0xFF7C5CE0);
                    mSlotPills[i].setTextColor(0xFFFFFFFF);
                } else {
                    bg.setColor(0x00000000);
                    bg.setStroke(dp(1), isNight() ? 0x37305A : 0xDDDDDD);
                    mSlotPills[i].setTextColor(text2());
                }
                mSlotPills[i].setBackground(bg);
            }
        }
    }

    /**
     * 刷新渐变页圆角色标：照搬首页起点/终点色点效果——底色=当前色标颜色（浅色页签读副键），
     * 选中=常驻呼吸涟漪（BreathSwatchDrawable，由 breathTick 每 50ms 推进相位），未选中=纯色圆。
     * 色标未自定义（AUTO）时用暗灰占位。
     */
    private void refreshStopCircles() {
        if (mStopCircles == null) {
            return;
        }
        mGradBreath = null;
        for (int i = 0; i < mStopCircles.length; i++) {
            FrameLayout wrap = mStopCircles[i];
            if (wrap == null) {
                continue;
            }
            // f12-1：AUTO 色标显示可见灰圆（不隐形）；非 AUTO 显示实际颜色实心圆。
            int raw = ClockSecondsConfig.getGradientStopColor(getContentResolver(), stopKeyIndex(i));
            boolean isAuto = raw == ClockSecondsConfig.GRADIENT_COLOR_AUTO;
            int color = isAuto ? (isNight() ? 0xFF3A3152 : 0xFFD8D4E8) : raw;
            // 浅色页签读副键（未设则回落常规键）
            if (mAdaptiveColor && mLightMode) {
                String stored = Settings.Secure.getString(getContentResolver(),
                        ClockSecondsConfig.gradientStopLightKey(stopKeyIndex(i)));
                if (stored != null) {
                    try {
                        color = Integer.parseInt(stored);
                        isAuto = false;
                    } catch (NumberFormatException ignored) {
                        // 非法值维持常规键颜色
                    }
                }
            }
            final boolean selected = (i == mGradTarget);
            // 照搬首页：色点=实心圆 + （选中时）同色呼吸涟漪；不另加 accent 描边环。
            BreathSwatchDrawable bg = new BreathSwatchDrawable(color, selected, accent(), 0f,
                    GRAD_SOLID_RATIO);
            wrap.setBackground(bg);
            if (selected) {
                mGradBreath = bg;
            }
            // AUTO 占位：圆心叠「AUTO」小字（accent 自适应），可见不隐形。
            if (isAuto) {
                TextView auto = wrap.getChildCount() > 0 && wrap.getChildAt(0) instanceof TextView
                        ? (TextView) wrap.getChildAt(0) : null;
                if (auto == null) {
                    auto = new TextView(this);
                    auto.setText("AUTO");
                    auto.setTextSize(6f);
                    auto.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    auto.setGravity(Gravity.CENTER);
                    auto.setLayoutParams(new FrameLayout.LayoutParams(dp(24), dp(24), Gravity.CENTER));
                    wrap.addView(auto);
                }
                auto.setTextColor(isAutoColor());
            } else {
                for (int k = wrap.getChildCount() - 1; k >= 0; k--) {
                    wrap.removeView(wrap.getChildAt(k));
                }
            }
            if (mStopLabels != null && i < mStopLabels.length && mStopLabels[i] != null) {
                mStopLabels[i].setTextColor(selected ? 0xFFB9A6F5 : text2());
            }
        }
    }

    /** AUTO 占位字色：按底圆明暗取反保证可读。 */
    private int isAutoColor() {
        return isNight() ? 0xFFB9A6F5 : 0xFF6C4CF1;
    }

    /** 当前调色目标颜色：自适应开启且浅色页签时，优先用 _light 副键。 */
    // target：0..5=数字位，>=DIGIT_COUNT=渐变色标（t6：DIGIT_COUNT..+3 = ①②③④ 四色标）
    private int effectiveColor(int target) {
        boolean isGradient = target >= ClockSecondsConfig.DIGIT_COUNT;
        int normal;
        if (isGradient) {
            int which = target - ClockSecondsConfig.DIGIT_COUNT;
            normal = ClockSecondsConfig.getGradientStopColor(getContentResolver(), stopKeyIndex(which));
            if (normal == ClockSecondsConfig.GRADIENT_COLOR_AUTO) {
                // 未自定义的色标：UI 上用灰占位（真机渲染走 start→end 插值回落）
                normal = isNight() ? 0xFF3A3152 : 0xFFD8D4E8;
            }
        } else {
            int slot = target % ClockSecondsConfig.DIGIT_COUNT;
            int[] colors = ClockSecondsConfig.getDigitColors(getContentResolver());
            normal = colors != null && colors.length > slot ? colors[slot] : 0xFFF44336;
        }
        if (mAdaptiveColor && mLightMode) {
            String key = isGradient
                    ? ClockSecondsConfig.gradientStopLightKey(stopKeyIndex(target - ClockSecondsConfig.DIGIT_COUNT))
                    : ClockSecondsConfig.digitLightKey(target % ClockSecondsConfig.DIGIT_COUNT);
            String stored = Settings.Secure.getString(getContentResolver(), key);
            if (stored != null) {
                try {
                    return Integer.parseInt(stored);
                } catch (NumberFormatException ignored) {
                    // 非法值回落常规键
                }
            }
        }
        return normal;
    }

    private void writeDigitColor(int slot, int color) {
        Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_DIGIT_COLOR)
                .putExtra(ClockSecondsConfig.EXTRA_SLOT, slot)
                .putExtra(ClockSecondsConfig.EXTRA_COLOR, color);
        if (mAdaptiveColor && mLightMode) {
            applyLightConfig(ClockSecondsConfig.digitLightKey(slot), color,
                    new Intent(ClockSecondsConfig.ACTION_SET_DIGIT_LIGHT_COLOR)
                            .putExtra(ClockSecondsConfig.EXTRA_SLOT, slot)
                            .putExtra(ClockSecondsConfig.EXTRA_COLOR, color), ok -> { });
        } else {
            applyConfig(ClockSecondsConfig.DIGIT_COLOR_KEYS[slot], color, intent, ok -> { });
        }
        refreshPreview();
        if (mEditingHex != null && !mGradPage && slot == mSlotTarget) {
            mEditingHex.setText(getString(R.string.settings_digit_editing, targetLabel())
                    + "  " + hexOf(color));
        }
        if (mSlotPills != null) {
            styleTargets();
        }
    }

    private void writeGradientColor(int which, int color) {
        Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_GRADIENT_COLOR)
                .putExtra(ClockSecondsConfig.EXTRA_COLOR, color)
                .putExtra(ClockSecondsConfig.EXTRA_SLOT, stopKeyIndex(which));
        if (mAdaptiveColor && mLightMode) {
            applyLightConfig(ClockSecondsConfig.gradientStopLightKey(stopKeyIndex(which)), color,
                    new Intent(ClockSecondsConfig.ACTION_SET_GRADIENT_LIGHT_COLOR)
                            .putExtra(ClockSecondsConfig.EXTRA_COLOR, color)
                            .putExtra(ClockSecondsConfig.EXTRA_SLOT, stopKeyIndex(which)), ok -> { });
        } else {
            applyConfig(ClockSecondsConfig.gradientStopKey(stopKeyIndex(which)), color, intent, ok -> { });
        }
        refreshPreview();
        if (mEditingHex != null && mGradPage && which == mGradTarget) {
            mEditingHex.setText(getString(R.string.settings_digit_editing, targetLabel())
                    + "  " + hexOf(color));
        }
        if (mStopCircles != null) {
            styleTargets();
        }
    }

    // ---------------- ③ 我的预设 ----------------

    static class Preset {
        final String name;
        final int[] colors;
        /** 渐变色标（可空，t36② 起 2 色：[起点, 终点]；旧四色预设读入时取首尾）：单色预设为 null。 */
        final int[] grad;
        /** U-20261115⑯：渐变方向 0..9；-1 = 旧预设未记录方向（应用时不改方向）。 */
        final int dir;
        Preset(String name, int[] colors, int[] grad, int dir) {
            this.name = name;
            this.colors = colors;
            this.grad = grad;
            this.dir = dir;
        }
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
    }

    /** 读取全部预设（JSON 文本，容错解析）。 */
    private List<Preset>[] loadAllPresets() {
        return loadPresets(this);
    }

    /** 同一份 advanced_presets 由自定义任务选色面板共用（同 UID 的应用私有目录）。 */
    static List<Preset>[] loadPresets(android.content.Context ctx) {
        String json = ctx.getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString(KEY_PRESETS, null);
        List<Preset> dark = new ArrayList<>();
        List<Preset> light = new ArrayList<>();
        if (json != null) {
            try {
                org.json.JSONObject root = new org.json.JSONObject(json);
                dark = parsePresetList(root.optJSONArray("dark"), ctx);
                light = parsePresetList(root.optJSONArray("light"), ctx);
            } catch (Throwable ignored) {
                // 数据损坏：视为空
            }
        }
        return new List[]{dark, light};
    }

    private static List<Preset> parsePresetList(org.json.JSONArray arr, android.content.Context ctx) {
        List<Preset> list = new ArrayList<>();
        if (arr == null) {
            return list;
        }
        for (int i = 0; i < arr.length(); i++) {
            org.json.JSONObject o = arr.optJSONObject(i);
            if (o == null) {
                continue;
            }
            org.json.JSONArray c = o.optJSONArray("colors");
            if (c == null || c.length() < 2) {
                continue;
            }
            int[] colors = new int[c.length()];
            for (int j = 0; j < c.length(); j++) {
                colors[j] = c.optInt(j, 0xFFF44336);
            }
            // 渐变色标（可空）：t36② 渐变回 2 色——新预设存 [start,end]；
            // 旧 t6 四色预设兼容读取：取首尾两端（中间色标不再参与渲染）。
            int[] grad = null;
            org.json.JSONArray g = o.optJSONArray("grad");
            if (g != null && g.length() >= GRAD_UI_STOPS) {
                grad = new int[GRAD_UI_STOPS];
                grad[0] = g.optInt(0, ClockSecondsConfig.GRADIENT_COLOR_AUTO);
                grad[1] = g.optInt(g.length() - 1, ClockSecondsConfig.GRADIENT_COLOR_AUTO);
            }
            // 旧预设没有 dir 键：记 -1，应用时保持当前方向不变。
            int dir = o.optInt("dir", -1);
            if (dir < 0 || dir >= ClockSecondsConfig.GRADIENT_DIRECTION_COUNT) {
                dir = -1;
            }
            list.add(new Preset(o.optString("name",
                    ctx.getString(R.string.settings_preset_default_name, i + 1)), colors, grad, dir));
        }
        return list;
    }

    /** t34 F31-3：返回写盘是否成功，供调用方在确认后提示。 */
    private boolean storePresets(List<Preset> dark, List<Preset> light) {
        org.json.JSONObject root = new org.json.JSONObject();
        try {
            root.put("dark", toJsonArray(dark));
            root.put("light", toJsonArray(light));
            prefs().edit().putString(KEY_PRESETS, root.toString()).commit();   // 同步写盘，返回真实结果
            return true;
        } catch (Throwable ignored) {
            // JSON 异常：不覆盖旧数据
            return false;
        }
    }

    private org.json.JSONArray toJsonArray(List<Preset> list) {
        org.json.JSONArray arr = new org.json.JSONArray();
        for (Preset p : list) {
            org.json.JSONObject o = new org.json.JSONObject();
            try {
                o.put("name", p.name);
                org.json.JSONArray c = new org.json.JSONArray();
                for (int color : p.colors) {
                    c.put(color);
                }
                o.put("colors", c);
                o.put("dir", p.dir);
                if (p.grad != null) {
                    org.json.JSONArray g = new org.json.JSONArray();
                    for (int color : p.grad) {
                        g.put(color);
                    }
                    o.put("grad", g);
                }
                arr.put(o);
            } catch (Throwable ignored) {
                // 单个条目损坏跳过
            }
        }
        return arr;
    }

    /** 保存当前暗色（或浅色）配色为新预设；渐变页保存时附带当前两端色标。 */
    private void saveCurrentColors(boolean light) {
        // U-20261115⑮：保存「显示出来」的颜色——浅色调色板激活时读浅色副键、AUTO 回落，
        // 否则读常规键；与预览钟 effectiveSlotPreviewColor/displayedGradientStops 同一判据。
        int[] colors = new int[ClockSecondsConfig.DIGIT_COUNT];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = effectiveSlotPreviewColor(i);
        }
        if (colors.length < 2) {
            Toast.makeText(this, R.string.settings_preset_saved, Toast.LENGTH_SHORT).show();
            return;
        }
        // 渐变页：保存当前起点/终点色标（t36② 两色；AUTO 原样存，应用时按 AUTO 语义回落）。
        int[] grad = null;
        int dir = -1;
        if (mGradPage) {
            grad = displayedGradientStops();
            dir = ClockSecondsConfig.getGradientDirection(getContentResolver());
        }
        List<Preset>[] all = loadAllPresets();
        List<Preset> group = light ? all[1] : all[0];
        if (group.size() >= MAX_PRESETS) {
            Toast.makeText(this, R.string.settings_preset_full, Toast.LENGTH_SHORT).show();
            return;
        }
        String name = getString(R.string.settings_preset_default_name, group.size() + 1);
        group.add(new Preset(name, colors.clone(), grad, dir));
        // t34 F31-3：写盘确认成功后才提示，避免假成功。
        if (storePresets(all[0], all[1])) {
            rebuildPresetRows();
            Toast.makeText(this, R.string.settings_preset_saved, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.settings_preset_save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void buildPresetCard(LinearLayout card) {
        // 标题行 = 名称 + ? 帮助 + 弹性空隙 + 「› 更多」（t20③：「+ 保存当前」已移至「调色盘」标题行，
        // 本行只留全量管理入口；按钮仍常驻标题行右侧，不放横滑容器末尾）。
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = new TextView(this);
        title.setText(R.string.settings_preset_title);
        title.setTextSize(15f);
        title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        title.setTextColor(text());
        titleRow.addView(title);
        // t26④：长按预设可重命名/删除的帮助「?」（样式与自适应卡 mHelpIcon 一致）
        TextView presetHelp = makeHelpIcon(v -> showPresetHelp());
        titleRow.addView(presetHelp);
        View spacer = new View(this);
        spacer.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        titleRow.addView(spacer);
        TextView more = makePillButton("› " + getString(R.string.settings_preset_more),
                v -> openAllPresets());
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp.leftMargin = dp(6);
        more.setLayoutParams(mlp);
        titleRow.addView(more);
        card.addView(titleRow);

        // f12-3：删除说明行及其右侧第二枚「保存当前配色」胶囊——与标题行常驻
        // 「+ 保存当前」「› 更多」重复，且小屏上胶囊遮挡说明文字。保存入口只在标题行。

        mPresetWrap = new LinearLayout(this);
        mPresetWrap.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(10);
        mPresetWrap.setLayoutParams(lp);
        card.addView(mPresetWrap);

        rebuildPresetRows();
    }

    private void rebuildPresetRows() {
        if (mAllPage) {
            rebuildAllPresets();
            return;
        }
        mPresetWrap.removeAllViews();
        List<Preset>[] all = loadAllPresets();
        // U-20261115⑰：单色页只显示单色预设、渐变页只显示渐变预设；分组标题计数随之过滤。
        mPresetWrap.addView(presetGroupLabel(filteredPresets(all[0]), R.string.settings_preset_group_dark, true));
        // 横滑区（R2.1）：每组一条 HorizontalScrollView，4 组完整 + 第 5 组露边表示可滑。
        mPresetWrap.addView(presetScroller(presetChips(all[0], false)));
        mPresetWrap.addView(presetGroupLabel(filteredPresets(all[1]), R.string.settings_preset_group_light, false));
        mPresetWrap.addView(presetScroller(presetChips(all[1], true)));
    }

    /** 把一组色片装进横滑容器：去左右裁切，尾部自然露边。 */
    private HorizontalScrollView presetScroller(LinearLayout content) {
        HorizontalScrollView hs = new HorizontalScrollView(this);
        hs.setHorizontalScrollBarEnabled(false);
        hs.setClipChildren(false);
        hs.setClipToPadding(false);
        hs.addView(content);
        hs.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return hs;
    }

    private TextView presetGroupLabel(List<Preset> list, int titleRes, boolean dark) {
        TextView l = new TextView(this);
        l.setText(getString(titleRes) + " · " + list.size());
        l.setTextSize(12f);
        l.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        l.setTextColor(text());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dark ? 0 : dp(14);
        lp.bottomMargin = dp(8);
        l.setLayoutParams(lp);
        return l;
    }

    /** U-20261115⑰：当前页（单色/渐变）可显示的预设子集，供分组标题计数。 */
    private List<Preset> filteredPresets(List<Preset> list) {
        List<Preset> out = new ArrayList<>();
        for (Preset p : list) {
            if ((p.grad != null) == mGradPage) {
                out.add(p);
            }
        }
        return out;
    }

    /** 一组预设色片：六格渐变方块，点击整组应用，长按删除。
     *  U-20261115⑰：只显示与当前页同类型的预设（单色页只显单色、渐变页只显渐变）；
     *  倒序遍历＝最新保存的排最前；chipIndex 仍为全量列表真实索引，重命名/删除按索引定位不受影响。 */
    private LinearLayout presetChips(List<Preset> list, boolean light) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.HORIZONTAL);
        wrap.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for (int i = list.size() - 1; i >= 0; i--) {
            final Preset p = list.get(i);
            if ((p.grad != null) != mGradPage) {
                continue;   // ⑰ 单色页跳过渐变预设，渐变页跳过单色预设
            }
            LinearLayout chip = new LinearLayout(this);
            chip.setOrientation(LinearLayout.VERTICAL);
            chip.setGravity(Gravity.CENTER_HORIZONTAL);
            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.HORIZONTAL);
            // 渐变预设画 4 段色标长条；旧预设按 6 位逐位色块。
            // f12-2 重修（t18）：改用单 View 自绘等分色条——多 swatch 方案在真机上
            // 只渲染首段（其余空心），自绘 onSizeChanged 后按实际宽等分，必定铺满。
            // AUTO 色标按首尾插值回落（-1 直接 setColor 会变白/透明）。
            int[] renderColors = p.grad != null ? renderGradColors(p.grad)
                    : renderDigitColors(p.colors);
            box.addView(presetBarView(renderColors, p.grad != null, p.dir));
            LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                    dp(48), dp(30));
            blp.leftMargin = dp(4);
            blp.rightMargin = dp(4);
            blp.topMargin = dp(4);
            box.setLayoutParams(blp);
            GradientDrawable bb = new GradientDrawable();
            bb.setCornerRadius(dp(11));
            bb.setStroke(dp(1.5f), accent());
            bb.setColor(0x00000000);
            box.setBackground(bb);
            box.setClipToOutline(true);   // f12-2：圆角裁切内部色条
            chip.addView(box);

            TextView name = new TextView(this);
            name.setText(p.name);
            name.setTextSize(9f);
            name.setTextColor(text2());
            name.setPadding(0, dp(5), 0, 0);
            chip.addView(name);

            chip.setClickable(true);
            chip.setFocusable(true);
            chip.setOnClickListener(v -> {
                if (!presetApplicable(p, light)) {
                    return;
                }
                if (p.grad != null) {
                    applyGradPreset(p.grad, p.dir);
                } else {
                    applyPreset(p.colors);
                }
            });
            // t26③：主横滑 chip 长按与全量页 cell 统一为「重命名 / 删除」菜单。
            // t34 F31-3：按索引定位——闭包 p 是某次 loadAllPresets() 的实例，写盘路径再 load 时
            // 引用不同，remove/== 永不命中造成假成功；索引跨重建始终有效。
            final int chipIndex = i;
            chip.setOnLongClickListener(v -> {
                showPresetActions(light, chipIndex);
                return true;
            });
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.leftMargin = dp(4);
            clp.rightMargin = dp(4);
            clp.topMargin = dp(4);
            chip.setLayoutParams(clp);
            wrap.addView(chip);
        }

        // ⑰ 本组当前页类型无预设时给占位提示，避免留白（全部预设仍可在「› 更多」全量页查看）。
        if (wrap.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText(R.string.settings_preset_empty);
            empty.setTextSize(12f);
            empty.setTextColor(text2());
            empty.setPadding(dp(6), dp(8), dp(6), dp(2));
            wrap.addView(empty);
        }

        // 新建入口已移至「我的预设」标题行右侧常驻胶囊（R2.1），横滑区只放已存预设。
        return wrap;
    }

    /** f12-2：渐变预设渲染色——AUTO(-1)/FOLLOW(0) 色标按 start→end 线性插值回落，条形不再空白/透明。 */
    private static int[] renderGradColors(int[] grad) {
        int n = grad.length;
        int[] out = new int[n];
        int first = -1, last = -1;
        for (int i = 0; i < n; i++) {
            if (grad[i] != ClockSecondsConfig.GRADIENT_COLOR_AUTO
                    && grad[i] != ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {   // U-20261116x：FOLLOW(0) 按 AUTO 插值（0 直接 setColor = 透明）
                if (first < 0) {
                    first = i;
                }
                last = i;
            }
        }
        for (int i = 0; i < n; i++) {
            if (grad[i] != ClockSecondsConfig.GRADIENT_COLOR_AUTO
                    && grad[i] != ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {   // U-20261116x：FOLLOW(0) 按 AUTO 插值（0 直接 setColor = 透明）
                out[i] = grad[i];
                continue;
            }
            // 首尾实色之间插值；两端都 AUTO 时用默认红→紫（与预览语义一致）
            int from = first >= 0 ? grad[first] : 0xFFF44336;
            int to = last >= 0 ? grad[last] : 0xFF7C5CE0;
            float t = last > first ? (float) (i - first) / (last - first) : 0f;
            out[i] = lerpPreviewColor(from, to, t);
        }
        return out;
    }

    /**
     * t18：单色预设渲染色——DIGIT_COLOR_FOLLOW(0) 段不能直接 setColor(0)（= 全透明，
     * 即真机上「左端一块实色其余空心」的根因），改为跟随首位实色回落。
     */
    private static int[] renderDigitColors(int[] colors) {
        int[] out = new int[colors.length];
        int first = 0xFFF44336;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] != ClockSecondsConfig.DIGIT_COLOR_FOLLOW) {
                first = colors[i];
                break;
            }
        }
        for (int i = 0; i < colors.length; i++) {
            out[i] = colors[i] == ClockSecondsConfig.DIGIT_COLOR_FOLLOW ? first : colors[i];
        }
        return out;
    }

    /**
     * t18：预设等分色条——单 View 自绘（按实际宽等分 n 段纯色，必定铺满整个胶囊；
     * 圆角裁切由外层 box.setClipToOutline 完成）。
     * 替代旧多 swatch 方案（真机只渲染首段）。
     * U-20261115⑮：渐变预设（grad!=null）改为横向 LinearGradient 平滑渐变，不再画两段硬色块。
     */
    private View presetBarView(final int[] colors, final boolean gradient, final int dir) {
        View bar = new View(this) {
            @Override protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                p.setStyle(Paint.Style.FILL);
                if (gradient && colors.length >= 2) {
                    float w = getWidth(), h = getHeight();
                    if (dir >= ClockSecondsConfig.GRADIENT_DIRECTION_MESH_RADIAL) {
                        // U-20261118①：特效方向委托共享工厂（条形预览按 0..w/0..h 画）。
                        p.setShader(GradientShaderFactory.build(colors, dir,
                                0f, w, 0f, h));
                    } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_RADIAL) {
                        p.setShader(new android.graphics.RadialGradient(w / 2f, h / 2f,
                                Math.max(w, h) / 2f, colors, null, Shader.TileMode.CLAMP));
                    } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_SWEEP) {
                        p.setShader(new android.graphics.SweepGradient(
                                w / 2f, h / 2f, colors, null));
                    } else {
                        float x0 = 0f, y0 = 0f, x1 = w, y1 = 0f;
                        if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_VERTICAL) {
                            x0 = x1 = w / 2f; y0 = h; y1 = 0f;
                        } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_DIAGONAL) {
                            y1 = h;
                        } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_RTL) {
                            x0 = w; x1 = 0f;
                        } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_BTT) {
                            x0 = x1 = w / 2f; y0 = h; y1 = 0f;
                        } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_BRTL) {
                            x0 = w; y0 = h; x1 = 0f; y1 = 0f;
                        } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_TRBL) {
                            x0 = w; x1 = 0f; y1 = h;
                        } else if (dir == ClockSecondsConfig.GRADIENT_DIRECTION_BLTR) {
                            y0 = h; x1 = w;
                        }
                        p.setShader(new LinearGradient(x0, y0, x1, y1, colors, null,
                                Shader.TileMode.CLAMP));
                    }
                    canvas.drawRect(0, 0, w, h, p);
                    p.setShader(null);
                    return;
                }
                int n = colors.length;
                float seg = getWidth() / (float) n;
                for (int i = 0; i < n; i++) {
                    p.setColor(colors[i]);
                    // 末段多画 1px 防浮点缝隙；首段起点 0
                    canvas.drawRect(i * seg, 0, (i + 1) * seg + (i == n - 1 ? 1 : 0), getHeight(), p);
                }
            }
        };
        bar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return bar;
    }

    /**
     * U-20261115⑯：当前页签（深浅）与页面类型（单色/渐变）都必须与预设一致，
     * 否则只提示不应用，避免「浅色预设在深色渐变页点了没反应还改乱颜色」。
     */
    private boolean presetApplicable(Preset p, boolean light) {
        if (light != mLightMode || (p.grad != null) != mGradPage) {
            toast(R.string.settings_preset_incompatible);
            return false;
        }
        return true;
    }

    /** 应用渐变预设：两端色标逐个写入（AUTO 原样传），有方向记录时一并恢复。 */
    private void applyGradPreset(int[] grad, int dir) {
        for (int i = 0; i < grad.length && i < GRAD_UI_STOPS; i++) {
            writeGradientColor(i, grad[i]);
        }
        if (dir >= 0 && dir < ClockSecondsConfig.GRADIENT_DIRECTION_COUNT) {
            applyGradientDirection(dir);
        }
        try {
            Settings.Secure.putInt(getContentResolver(),
                    ClockSecondsConfig.GRADIENT_ENABLED_KEY, 1);
        } catch (SecurityException ignored) {
            // 无权限：广播已发给 SystemUI 代写
        }
        toast(R.string.settings_preset_applied);
        styleTargets();
        refreshPreview();
    }

    private void applyPreset(int[] colors) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < colors.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(colors[i]);
        }
        // U-20261117a：浅色页签（明暗自适应+浅色组）应用预设时写「浅色副键」，
        // 否则副键保持原值（0=跟随系统）导致浅色预设看似未生效；深色页签维持常规键。
        final boolean lightApply = mAdaptiveColor && mLightMode;
        Intent intent = new Intent(lightApply
                ? ClockSecondsConfig.ACTION_SET_DIGIT_LIGHT_COLORS_BATCH
                : ClockSecondsConfig.ACTION_SET_DIGIT_COLORS_BATCH)
                .putExtra(ClockSecondsConfig.EXTRA_COLORS, sb.toString());
        intent.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
        sendBroadcast(intent);
        // 应用后立刻反馈并刷新调色盘
        toast(R.string.settings_preset_applied);
        try {
            Settings.Secure.putInt(getContentResolver(),
                    ClockSecondsConfig.HOUR_COLOR_ENABLED_KEY, 1);
        } catch (SecurityException ignored) {
            // 无权限：广播已发给 SystemUI 代写
        }
        syncSlidersToColor();
        refreshPreview();
    }

    /** t34 F31-3：删除按索引定位；写盘确认成功后才提示，避免假成功。 */
    private void deletePreset(boolean light, int index) {
        List<Preset>[] all = loadAllPresets();
        List<Preset> group = light ? all[1] : all[0];
        if (index < 0 || index >= group.size()) {
            return;   // 菜单弹出后预设可能已被删除
        }
        group.remove(index);
        if (storePresets(all[0], all[1])) {
            rebuildPresetRows();
            toast(R.string.settings_preset_deleted);
        } else {
            toast(R.string.settings_preset_save_failed);
        }
    }

    // ---------------- ③b 全量预设管理页（t5） ----------------

    private void openAllPresets() {
        // U-20261115⑰b：按来源页（单色/渐变）传类型，全量页沿用同一过滤语义。
        Intent i = new Intent(this, AdvancedSettingsActivity.class)
                .putExtra(EXTRA_PAGE, mGradPage ? PAGE_ALL_GRAD : PAGE_ALL);
        startActivity(i);
    }

    /** 全量页布局：appbar 之下直接放暗/浅两组网格 + 底部保存按钮。 */
    private void buildAllPresetsView(LinearLayout root) {
        mAllWrap = new LinearLayout(this);
        mAllWrap.setOrientation(LinearLayout.VERTICAL);
        root.addView(mAllWrap);
        rebuildAllPresets();
    }

    /** 全量页内容重建：保存/重命名/删除后整体刷新。 */
    private void rebuildAllPresets() {
        if (mAllWrap == null) {
            return;
        }
        mAllWrap.removeAllViews();
        List<Preset>[] all = loadAllPresets();

        // U-20261115⑰b：全量页同样只显示当前页类型的预设（单色页只显单色、渐变页只显渐变）。
        mAllWrap.addView(presetGroupLabel(filteredPresets(all[0]), R.string.settings_preset_group_dark, true));
        mAllWrap.addView(presetGrid(all[0], false));
        mAllWrap.addView(saveDashedButton(false));

        mAllWrap.addView(presetGroupLabel(filteredPresets(all[1]), R.string.settings_preset_group_light, false));
        mAllWrap.addView(presetGrid(all[1], true));
        mAllWrap.addView(saveDashedButton(true));
    }

    /** 一组预设的 3 列网格；U-20261115⑰b：只显示与当前页同类型的预设，倒序展示（最新在前）；
     *  索引经 idxs 列表映射回全量真实索引，长按重命名/删除定位不受影响；无匹配时显示占位文案。 */
    private LinearLayout presetGrid(List<Preset> list, final boolean light) {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        glp.bottomMargin = dp(14);
        grid.setLayoutParams(glp);
        // U-20261115⑰b：先按当前页类型过滤并倒序收集全量真实索引（最新保存的排第一格）。
        List<Integer> idxs = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            if ((list.get(i).grad != null) == mGradPage) {
                idxs.add(i);
            }
        }
        if (idxs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(R.string.settings_preset_empty);
            empty.setTextSize(12f);
            empty.setTextColor(text2());
            empty.setPadding(0, dp(6), 0, dp(2));
            grid.addView(empty);
            return grid;
        }
        int rows = (idxs.size() + 2) / 3;
        for (int r = 0; r < rows; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rl.topMargin = r == 0 ? 0 : dp(10);
            row.setLayoutParams(rl);
            for (int c = 0; c < 3; c++) {
                int disp = r * 3 + c;
                if (disp < idxs.size()) {
                    int idx = idxs.get(disp);
                    final Preset p = list.get(idx);
                    row.addView(presetGridCell(p, light, idx));
                } else {
                    // 占位保持 3 列对齐
                    View filler = new View(this);
                    filler.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
                    row.addView(filler);
                }
            }
            grid.addView(row);
        }
        return grid;
    }

    /** 全量页单格：六格色块 + 名称 + （使用中）徽标；点应用、长按重命名/删除菜单。 */
    private LinearLayout presetGridCell(final Preset p, final boolean light, final int index) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        clp.rightMargin = dp(10);
        cell.setLayoutParams(clp);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.HORIZONTAL);
        // 渐变预设画 4 段色标长条（R3 图样式）；旧预设按 6 位逐位色块。
        // f12-2 重修（t18）：与横滑 chip 同样改单 View 自绘等分色条，保证满宽铺满。
        int[] renderColors = p.grad != null ? renderGradColors(p.grad)
                : renderDigitColors(p.colors);
        box.addView(presetBarView(renderColors, p.grad != null, p.dir));
        GradientDrawable bb = new GradientDrawable();
        bb.setCornerRadius(dp(11));
        bb.setStroke(dp(1.5f), accent());
        box.setBackground(bb);
        box.setClipToOutline(true);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(30));
        box.setLayoutParams(blp);
        cell.addView(box);

        boolean active = isPresetActive(p, light);
        TextView name = new TextView(this);
        name.setText(active
                ? getString(R.string.settings_preset_in_use)
                : p.name);
        name.setTextSize(10f);
        name.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        name.setTextColor(active ? accent() : text2());
        name.setPadding(0, dp(5), 0, 0);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        cell.addView(name);

        cell.setClickable(true);
        cell.setFocusable(true);
        cell.setOnClickListener(v -> {
            if (!presetApplicable(p, light)) {
                return;
            }
            if (p.grad != null) {
                applyGradPreset(p.grad, p.dir);
            } else {
                applyPreset(p.colors);
            }
        });
        // t34 F31-3：长按按索引定位（与横滑 chip 一致），不再依赖跨重建的 Preset 引用。
        cell.setOnLongClickListener(v -> {
            showPresetActions(light, index);
            return true;
        });
        return cell;
    }

    /**
     * 「使用中」判定（t36⑤）：仅当预设内容与当前生效键逐字节一致且属于同一组（dark/light）时命中。
     * 单色预设比六位常规键（浅色组比 digitLightKey 副键，未设=回落常规值）；渐变预设比起点/终点
     * 常规键（浅色组比 gradientStopLightKey 副键）。AUTO/FOLLOW 原值参与比较，不做语义回落，
     * 保证同一时刻全页最多命中 1 枚。
     */
    private boolean isPresetActive(Preset p, boolean light) {
        if (p.grad != null) {
            if (p.dir >= 0
                    && ClockSecondsConfig.getGradientDirection(getContentResolver()) != p.dir) {
                return false;
            }
            for (int i = 0; i < p.grad.length; i++) {
                Integer cur = stopColorForCompare(stopKeyIndex(i), light);
                if (cur == null || cur != p.grad[i]) {
                    return false;
                }
            }
            return true;
        }
        for (int i = 0; i < p.colors.length; i++) {
            Integer cur = digitColorForCompare(i, light);
            if (cur == null || cur != p.colors[i]) {
                return false;
            }
        }
        return true;
    }

    /** 当前生效的色标值：light 组读副键（未设返回 null=视为不匹配），dark 组读常规键。 */
    private Integer stopColorForCompare(int keyIndex, boolean light) {
        if (light && mAdaptiveColor) {
            String stored = Settings.Secure.getString(getContentResolver(),
                    ClockSecondsConfig.gradientStopLightKey(keyIndex));
            if (stored != null) {
                try {
                    return Integer.parseInt(stored);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }
        return ClockSecondsConfig.getGradientStopColor(getContentResolver(), keyIndex);
    }

    /** 当前生效的数字位颜色：light 组读副键（未设返回 null=视为不匹配），dark 组读常规键。 */
    private Integer digitColorForCompare(int slot, boolean light) {
        if (light && mAdaptiveColor) {
            String stored = Settings.Secure.getString(getContentResolver(),
                    ClockSecondsConfig.digitLightKey(slot));
            if (stored != null) {
                try {
                    return Integer.parseInt(stored);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
            return null;
        }
        return ClockSecondsConfig.getDigitColor(getContentResolver(), slot);
    }

    /** 长按菜单：重命名 / 删除。 */
    /** t34 F31-3：菜单改为按 (light, index) 定位——闭包引用跨 loadAllPresets() 重建后失效。 */
    private void showPresetActions(final boolean light, final int index) {
        final String[] items = {
                getString(R.string.settings_preset_rename),
                getString(R.string.settings_preset_delete)
        };
        new AlertDialog.Builder(this)
                .setItems(items, (d, w) -> {
                    if (w == 0) {
                        showRenameDialog(light, index);
                    } else {
                        deletePreset(light, index);
                    }
                })
                .show();
    }

    /** 重命名对话框：单行输入，确认后写回对应分组。 */
    /** t34 F31-3：重命名按索引定位；写盘确认成功后才提示，避免假成功。 */
    private void showRenameDialog(final boolean light, final int index) {
        List<Preset>[] all = loadAllPresets();
        List<Preset> group = light ? all[1] : all[0];
        if (index < 0 || index >= group.size()) {
            return;   // 菜单弹出后预设可能已被删除
        }
        final Preset p = group.get(index);
        final EditText input = new EditText(this);
        input.setText(p.name);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings_preset_rename)
                .setView(input)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        return;
                    }
                    List<Preset>[] fresh = loadAllPresets();
                    List<Preset> g = light ? fresh[1] : fresh[0];
                    if (index < 0 || index >= g.size()) {
                        return;   // 二次校验
                    }
                    Preset old = g.get(index);
                    g.set(index, new Preset(name, old.colors, old.grad, old.dir));
                    if (storePresets(fresh[0], fresh[1])) {
                        rebuildPresetRows();
                        toast(R.string.settings_preset_saved);
                    } else {
                        toast(R.string.settings_preset_save_failed);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** 全量页底部「+ 保存当前配色」虚线按钮。 */
    private LinearLayout saveDashedButton(final boolean light) {
        LinearLayout add = new LinearLayout(this);
        add.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(14));
        bg.setStroke(dp(1.5f), 0x5A4E85);
        add.setBackground(bg);
        TextView plus = new TextView(this);
        plus.setText("+ " + getString(R.string.settings_preset_save));
        plus.setTextSize(12f);
        plus.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        plus.setTextColor(accent());
        add.addView(plus);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(44));
        alp.bottomMargin = dp(16);
        add.setLayoutParams(alp);
        add.setClickable(true);
        add.setFocusable(true);
        add.setOnClickListener(v -> saveCurrentColors(light));
        return add;
    }

    /** 标题行常驻胶囊按钮：描边胶囊统一样式（保存/更多）。 */
    private TextView makePillButton(String label, android.view.View.OnClickListener l) {
        TextView b = new TextView(this);
        b.setText(label);
        b.setTextSize(11f);
        b.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        b.setTextColor(accent());
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(999));
        bg.setStroke(dp(1), accent());
        b.setBackground(bg);
        b.setPadding(dp(11), dp(5), dp(11), dp(5));
        b.setClickable(true);
        b.setFocusable(true);
        b.setOnClickListener(l);
        return b;
    }

    // ---------------- ④ 渐变方向（t20：方向选择已迁入调色盘卡，此处仅状态刷新与写键） ----------------

    private void refreshDirPills(int dir) {
        if (mDirPills == null) {
            return;
        }
        for (int i = 0; i < mDirPills.length; i++) {
            TextView pill = mDirPills[i];
            if (pill == null) {
                continue;   // U-20261118①：2..9 等已从 UI 收起的方向没有胶囊
            }
            boolean sel = (i == dir);
            GradientDrawable bg = new GradientDrawable();
            bg.setCornerRadius(dp(999));
            if (sel) {
                bg.setColor(0xFF7C5CE0);
                pill.setTextColor(0xFFFFFFFF);
            } else {
                bg.setColor(0x00000000);
                bg.setStroke(dp(1), isNight() ? 0x37305A : 0xDDDDDD);
                pill.setTextColor(text2());
            }
            pill.setBackground(bg);
        }
    }

    private void applyGradientDirection(int dir) {
        Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_GRADIENT_DIRECTION)
                .putExtra(ClockSecondsConfig.EXTRA_DIRECTION, dir);
        applyConfig(ClockSecondsConfig.GRADIENT_DIRECTION_KEY, dir, intent, ok -> {
            if (ok) {
                refreshDirPills(dir);
            } else {
                toastWriteFailed();
            }
        });
        refreshPreview();
    }

    // ==================================================================
    // 视图工具（视觉规范与首页一致）
    // ==================================================================

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(24));
        bg.setColor(isNight() ? 0xFF241D38 : 0xFFFFFFFF);
        card.setBackground(bg);
        card.setElevation(dp(3));
        card.setClipToOutline(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(12);
        card.setLayoutParams(lp);
        return card;
    }

    private TextView makeTab(String label) {
        TextView t = new TextView(this);
        t.setText(label);
        t.setTextSize(12f);
        t.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(7), 0, dp(7));
        t.setClickable(true);
        return t;
    }

    private void styleTab(TextView t, boolean active) {
        if (t == null) {
            return;
        }
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(999));
        if (active) {
            bg.setColor(0xFF7C5CE0);
            t.setTextColor(0xFFFFFFFF);
        } else {
            bg.setColor(0x00000000);
            t.setTextColor(text2());
        }
        t.setBackground(bg);
    }

    private int accent() {
        return isNight() ? 0xFFA98CFF : 0xFF6C4CF1;
    }

    private int text() {
        return isNight() ? 0xFFEAE6F8 : 0xFF1A1526;
    }

    private int text2() {
        return isNight() ? 0xFF9C93BC : 0xFF7A728A;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private int dp(float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics()));
    }

    private String hexOf(int color) {
        return String.format("#%08X", color);
    }

    private void toast(int res) {
        Toast.makeText(this, res, Toast.LENGTH_SHORT).show();
    }

    private void toastWriteFailed() {
        toast(R.string.settings_write_failed);
    }

    // ==================================================================
    // 配置写入（无权限时由 SystemUI 代写，语义与首页一致）
    // ==================================================================

    private void applyConfig(final String key, final int value, final Intent broadcast,
                             final java.util.function.Consumer<Boolean> onResult) {
        final int seq = mWriteSeq.incrementAndGet();
        final java.util.function.Consumer<Boolean> guarded = ok -> {
            if (mWriteSeq.get() == seq) {
                onResult.accept(ok);
            }
        };
        try {
            Settings.Secure.putInt(getContentResolver(), key, value);
            guarded.accept(true);
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

    private void applyLightConfig(final String key, final Integer value, final Intent broadcast,
                                  final java.util.function.Consumer<Boolean> onResult) {
        final int seq = mWriteSeq.incrementAndGet();
        final java.util.function.Consumer<Boolean> guarded = ok -> {
            if (mWriteSeq.get() == seq) {
                onResult.accept(ok);
            }
        };
        try {
            if (value == null) {
                Settings.Secure.putString(getContentResolver(), key, null);
            } else {
                Settings.Secure.putString(getContentResolver(), key, String.valueOf(value));
            }
            guarded.accept(true);
            try {
                broadcast.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
                sendBroadcast(broadcast);
            } catch (Throwable ignored2) {
                // 广播失败不影响直写结果
            }
            return;
        } catch (SecurityException ignored) {
            // 无 WRITE_SECURE_SETTINGS：走广播路径（SystemUI 代写）
        } catch (Throwable ignored) {
            // 其他写失败同样回落广播
        }
        try {
            broadcast.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(broadcast);
        } catch (Throwable t) {
            guarded.accept(false);
            return;
        }
        verifyLightWrite(key, value, 0, guarded);
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
        handler.postDelayed(() -> verifyWrite(key, value, attempt + 1, onResult), 250);
    }

    private void verifyLightWrite(final String key, final Integer value, final int attempt,
                                  final java.util.function.Consumer<Boolean> onResult) {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        String actual = null;
        try {
            actual = Settings.Secure.getString(getContentResolver(), key);
        } catch (Throwable ignored) {
            // 读取失败按未生效处理
        }
        boolean ok = false;
        if (value == null) {
            ok = actual == null;
        } else if (actual != null) {
            try {
                ok = Integer.parseInt(actual) == value;
            } catch (NumberFormatException ignored) {
                // 键被其他程序写脏：按未生效处理
            }
        }
        if (ok) {
            onResult.accept(true);
            return;
        }
        if (attempt >= 8) {
            onResult.accept(false);
            return;
        }
        handler.postDelayed(() -> verifyLightWrite(key, value, attempt + 1, onResult), 250);
    }

    // ==================================================================
    // HSV 滑杆自绘
    // ==================================================================

    /** 自绘进度条：一条渐变圆角条 + 一个圆形滑块（白描边），用于色相/饱和/明度三条滑杆。 */
    private class HsvBarDrawable extends Drawable {
        private final int kind;              // 0=色相 1=饱和度 2=明度
        private float hue = 0f, sat = 1f, val = 1f;
        private final Paint bar = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint knobP = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rf = new RectF();
        private android.widget.SeekBar host;

        HsvBarDrawable(int kind) {
            this.kind = kind;
            bar.setStyle(Paint.Style.FILL);
            ring.setStyle(Paint.Style.STROKE);
            ring.setStrokeWidth(dp(3));
            ring.setColor(0xFFFFFFFF);
            knobP.setStyle(Paint.Style.FILL);
        }

        void setContext(float h, float s, float v) {
            hue = h;
            sat = s;
            val = v;
        }

        void notifyGradientChanged() {
            invalidateSelf();
        }

        void bind(android.widget.SeekBar sb) {
            host = sb;
        }

        @Override public void draw(Canvas canvas) {
            int w = getBounds().width(), h = getBounds().height();
            int bh = dp(10);
            int cy = h / 2;
            rf.set(0, cy - bh / 2f, w, cy + bh / 2f);
            int[] colors = new int[13];
            for (int i = 0; i <= 12; i++) {
                float t = i / 12f;
                if (kind == 0) colors[i] = Color.HSVToColor(new float[]{t * 360f, 0.85f, 1f});
                else if (kind == 1) colors[i] = Color.HSVToColor(new float[]{hue, t, Math.max(0.45f, val)});
                else colors[i] = Color.HSVToColor(new float[]{hue, sat, 0.12f + 0.88f * t});
            }
            bar.setShader(new LinearGradient(0, 0, w, 0, colors, null, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rf, bh / 2f, bh / 2f, bar);
            bar.setShader(null);
            if (host != null) {
                float t = host.getProgress() / 255f;
                float kx = Math.max(dp(11), Math.min(w - dp(11), t * w));
                float r = dp(11);
                int kc;
                if (kind == 0) kc = Color.HSVToColor(new float[]{t * 360f, 0.85f, 1f});
                else if (kind == 1) kc = Color.HSVToColor(new float[]{hue, t, Math.max(0.45f, val)});
                else kc = Color.HSVToColor(new float[]{hue, sat, 0.12f + 0.88f * t});
                knobP.setColor(kc);
                canvas.drawCircle(kx, cy, r, knobP);
                canvas.drawCircle(kx, cy, r, ring);
            }
        }

        @Override public void setAlpha(int a) { }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    private android.widget.SeekBar addHsvSlider(LinearLayout card, int kind, int progress) {
        android.widget.SeekBar sb = new android.widget.SeekBar(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(28));
        lp.topMargin = (kind == 0) ? dp(14) : dp(6);
        sb.setLayoutParams(lp);
        sb.setMax(255);
        sb.setProgress(progress);
        sb.setPadding(0, 0, 0, 0);
        sb.setBackground(null);
        sb.setSplitTrack(false);
        sb.setThumb(new android.graphics.drawable.ColorDrawable(0x00000000) {
            @Override public void draw(Canvas c) { }
        });
        HsvBarDrawable bar = new HsvBarDrawable(kind);
        bar.bind(sb);
        sb.setProgressDrawable(bar);
        card.addView(sb);
        return sb;
    }

    private void updateSliderGradients(float hue, float sat, float val) {
        if (mHueSlider != null) {
            ((HsvBarDrawable) mHueSlider.getProgressDrawable()).notifyGradientChanged();
        }
        if (mSatSlider != null) {
            HsvBarDrawable satBar = (HsvBarDrawable) mSatSlider.getProgressDrawable();
            satBar.setContext(hue, sat, val);
            satBar.notifyGradientChanged();
        }
        if (mValSlider != null) {
            HsvBarDrawable valBar = (HsvBarDrawable) mValSlider.getProgressDrawable();
            valBar.setContext(hue, sat, val);
            valBar.notifyGradientChanged();
        }
    }

    private void syncSlidersToColor() {
        if (mHueSlider == null) {
            return;
        }
        int color = effectiveColor(currentTargetIndex());
        mHsv.setFromColor(color);
        float[] hsv = {mHsv.getHue(), mHsv.getSat(), mHsv.getVal()};
        if (mHueSlider.getProgress() != mHsv.hueProgress()) {
            mHueSlider.setProgress(mHsv.hueProgress());
        }
        if (mSatSlider.getProgress() != mHsv.satProgress()) {
            mSatSlider.setProgress(mHsv.satProgress());
        }
        if (mValSlider.getProgress() != mHsv.valProgress()) {
            mValSlider.setProgress(mHsv.valProgress());
        }
        updateSliderGradients(hsv[0], hsv[1], hsv[2]);
    }
}
