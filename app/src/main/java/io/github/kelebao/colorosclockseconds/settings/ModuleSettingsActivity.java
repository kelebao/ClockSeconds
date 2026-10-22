package io.github.kelebao.colorosclockseconds.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

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
    private TextView netTimeView;
    private TextView previewHintView;
    private TextView syncView;
    private TextView colorValueView;
    private View colorDotView;

    /** 网络时间相对本地的偏移（毫秒，网络 - 本地）；Long.MIN_VALUE 表示还没取到。 */
    private long netDriftMs = Long.MIN_VALUE;

    private final Handler ticker = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            updatePreview();
            ticker.postDelayed(this, 1000L);
        }
    };

    /** 点击预览时钟后显示提示，3 秒后自动收起。 */
    private final Runnable hideHint = () -> {
        if (previewHintView != null) {
            previewHintView.setVisibility(View.GONE);
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
        applyStatusBarAppearance();
        refreshStatus();
        maybeAutoCheckUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
        ticker.removeCallbacks(tick);
        ticker.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        ticker.removeCallbacks(tick);
        ticker.removeCallbacks(hideHint);
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
        buildPreviewCard(root);
        buildStatsRow(root);
        buildColorCard(root);
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
        previewView = new TextView(this);
        previewView.setTextSize(44f);
        previewView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        previewView.setTextColor(text());
        previewView.setGravity(Gravity.CENTER_HORIZONTAL);
        previewView.setOnClickListener(v -> showPreviewHint());
        card.addView(previewView);

        // 提示用户：要点状态栏的时钟，不是点这里。点击预览后才出现，3 秒后自动收起。
        previewHintView = new TextView(this);
        previewHintView.setText(R.string.settings_preview_hint);
        previewHintView.setTextSize(11f);
        previewHintView.setTextColor(text3());
        previewHintView.setGravity(Gravity.CENTER_HORIZONTAL);
        previewHintView.setPadding(0, dp(6), 0, 0);
        previewHintView.setVisibility(View.GONE);
        card.addView(previewHintView);

        // 网络时间对比：点一下重新获取（HTTP Date 头，按 RTT 折半校正）。
        LinearLayout netRow = new LinearLayout(this);
        netRow.setOrientation(LinearLayout.HORIZONTAL);
        netRow.setGravity(Gravity.CENTER);
        netRow.setPadding(0, dp(10), 0, 0);

        netTimeView = new TextView(this);
        netTimeView.setTextSize(12f);
        netTimeView.setTextColor(text2());
        netTimeView.setOnClickListener(v -> fetchNetworkTime());
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
        netRow.addView(syncView);

        card.addView(netRow);

        root.addView(card);
        updatePreview();
        fetchNetworkTime();
    }

    /** 两张并排小卡：切换手势 / 小时颜色。 */
    private void buildStatsRow(LinearLayout root) {
        LinearLayout row = new LinearLayout(this);
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
        colorRow.setGravity(Gravity.CENTER_VERTICAL);
        colorRow.setPadding(0, dp(8), 0, 0);

        colorDotView = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(16), dp(16));
        dotLp.rightMargin = dp(8);
        colorDotView.setLayoutParams(dotLp);
        colorRow.addView(colorDotView);

        colorValueView = new TextView(this);
        colorValueView.setTextSize(16f);
        colorValueView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        colorValueView.setTextColor(text());
        colorRow.addView(colorValueView);
        colorCard.addView(colorRow);
        row.addView(colorCard);

        root.addView(row);
        refreshColorSummary();
    }

    private void buildColorCard(LinearLayout root) {
        LinearLayout card = card();

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
        boolean seconds = ClockSecondsConfig.isEnabled(getContentResolver());
        String text = new java.text.SimpleDateFormat(
                seconds ? "HH:mm:ss" : "HH:mm", Locale.US)
                .format(new java.util.Date());
        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        if (ClockSecondsConfig.isHourColorEnabled(getContentResolver())
                && sb.length() >= 2
                && Character.isDigit(sb.charAt(0)) && Character.isDigit(sb.charAt(1))) {
            sb.setSpan(new ForegroundColorSpan(ClockSecondsConfig.getHourColor(
                    getContentResolver())), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
            GradientDrawable dot = new GradientDrawable();
            dot.setShape(GradientDrawable.OVAL);
            dot.setColor(color);
            dot.setStroke(dp(1), 0x22000000);
            colorDotView.setBackground(dot);
            colorDotView.setAlpha(enabled ? 1f : 0.35f);
        }
        if (colorValueView != null) {
            colorValueView.setText(String.format(Locale.US, "#%06X", color & 0xFFFFFF));
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
        return swatch;
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
        for (int i = 0; i < grid.getChildCount(); i++) {
            View child = grid.getChildAt(i);
            child.setAlpha(enabled ? 1f : 0.3f);
            child.setClickable(enabled);
        }
    }

    private void selectHourColor(final int color, final GridLayout grid) {
        Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_HOUR_COLOR)
                .putExtra(ClockSecondsConfig.EXTRA_COLOR, color);
        applyConfig(ClockSecondsConfig.HOUR_COLOR_KEY, color, intent, ok -> {
            if (ok) {
                for (int i = 0; i < grid.getChildCount() && i < HOUR_COLORS.length; i++) {
                    grid.getChildAt(i).setBackground(
                            swatchDrawable(HOUR_COLORS[i], HOUR_COLORS[i] == color));
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
        return card;
    }

    private TextView statLabel(int textRes) {
        TextView t = new TextView(this);
        t.setText(textRes);
        t.setTextSize(11f);
        t.setTextColor(text2());
        return t;
    }

    private View divider() {
        View v = new View(this);
        v.setBackgroundColor(night ? DARK_DIVIDER : LIGHT_DIVIDER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(1)));
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
        try {
            Settings.Secure.putInt(getContentResolver(), key, value);
            onResult.accept(true);
            return;
        } catch (SecurityException ignored) {
            // 无 WRITE_SECURE_SETTINGS：走广播路径
        }
        try {
            broadcast.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            sendBroadcast(broadcast);
        } catch (Throwable t) {
            onResult.accept(false);
            return;
        }
        verifyWrite(key, value, 0, onResult);
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
        if (attempt >= 6) {
            onResult.accept(false);
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> verifyWrite(key, value, attempt + 1, onResult), 400);
    }

    private void toastWriteFailed() {
        Toast.makeText(this, R.string.settings_write_failed, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
