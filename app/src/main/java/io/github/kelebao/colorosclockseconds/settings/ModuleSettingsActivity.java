package io.github.kelebao.colorosclockseconds.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import io.github.kelebao.colorosclockseconds.BuildConfig;
import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;
import io.github.kelebao.colorosclockseconds.R;

/**
 * 模块设置页（从 LSPosed 管理器打开，桌面不生成图标）。
 *
 * <p>说明：普通应用进程写入 {@link Settings.Secure} 需要 {@code WRITE_SECURE_SETTINGS}。
 * 若未授予，页面仅展示状态与手势偏好，真正的切换请用"点击状态栏时钟"完成
 * （该动作运行在 SystemUI 进程内，天然具备权限）。</p>
 *
 * <p>更新检查：打开页面时（默认开启，可关）访问模块仓库 Releases API，
 * 发现新版本弹窗提醒，确认后跳转 Browser 打开 Releases 页。</p>
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
            0xFFF44336, // 红（默认）
            0xFFFF9800, // 橙
            0xFFFFC107, // 黄
            0xFF4CAF50, // 绿
            0xFF00BCD4, // 青
            0xFF2196F3, // 蓝
            0xFF9C27B0, // 紫
            0xFFE91E63, // 粉
            0xFFFFFFFF, // 白
            0xFF9E9E9E, // 灰
    };

    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_title);
        setContentView(buildContentView());
        applyStatusBarAppearance();
        refreshStatus();
        maybeAutoCheckUpdate();
    }

    /**
     * 浅色主题下把状态栏文字/图标切成深色：设置页背景是浅色，若系统仍画白色时钟会看不清
     * （模块的小时颜色也会被"浅色背景压暗"规则误判成深色背景）。
     */
    private void applyStatusBarAppearance() {
        boolean night = (getResources().getConfiguration().uiMode
                & android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        View decor = getWindow().getDecorView();
        int flags = decor.getSystemUiVisibility();
        if (night) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        decor.setSystemUiVisibility(flags);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshStatus();
    }

    private ScrollView buildContentView() {
        int pad = dp(20);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);

        TextView statusTitle = new TextView(this);
        statusTitle.setText(R.string.settings_status_title);
        statusTitle.setTextSize(16f);
        statusTitle.setPadding(0, 0, 0, dp(8));
        root.addView(statusTitle);

        statusView = new TextView(this);
        statusView.setTextSize(20f);
        statusView.setPadding(0, 0, 0, dp(16));
        root.addView(statusView);

        Button refresh = new Button(this);
        refresh.setText(R.string.settings_refresh);
        refresh.setOnClickListener(v -> refreshStatus());
        root.addView(refresh);

        TextView gestureTitle = new TextView(this);
        gestureTitle.setText(R.string.settings_gesture_title);
        gestureTitle.setTextSize(16f);
        gestureTitle.setPadding(0, dp(24), 0, dp(8));
        root.addView(gestureTitle);

        RadioGroup group = new RadioGroup(this);
        RadioButton click = new RadioButton(this);
        click.setId(1);
        click.setText(R.string.settings_gesture_click);
        RadioButton longPress = new RadioButton(this);
        longPress.setId(2);
        longPress.setText(R.string.settings_gesture_long_press);
        group.addView(click);
        group.addView(longPress);

        ContentResolver resolver = getContentResolver();
        int current = ClockSecondsConfig.getGesture(resolver);
        group.check(current == ClockSecondsConfig.GESTURE_LONG_PRESS ? 2 : 1);
        final boolean[] reverting = {false};
        final int[] lastChecked = {group.getCheckedRadioButtonId()};
        group.setOnCheckedChangeListener((g, checkedId) -> {
            if (reverting[0] || checkedId == ViewGroup.NO_ID) {
                return;
            }
            int gesture = checkedId == 2
                    ? ClockSecondsConfig.GESTURE_LONG_PRESS
                    : ClockSecondsConfig.GESTURE_CLICK;
            Intent intent = new Intent(ClockSecondsConfig.ACTION_SET_GESTURE)
                    .putExtra(ClockSecondsConfig.EXTRA_GESTURE, gesture);
            applyConfig(ClockSecondsConfig.TOGGLE_GESTURE_KEY, gesture, intent, ok -> {
                if (ok) {
                    lastChecked[0] = checkedId;
                } else {
                    // 写入失败：回滚选中项，避免"假成功"。
                    reverting[0] = true;
                    g.check(lastChecked[0]);
                    reverting[0] = false;
                }
            });
        });
        root.addView(group);

        TextView hint = new TextView(this);
        hint.setText(R.string.settings_hint);
        hint.setTextSize(13f);
        hint.setGravity(Gravity.START);
        hint.setPadding(0, dp(24), 0, 0);
        root.addView(hint);

        buildColorSection(root);
        buildUpdateSection(root);
        buildAboutSection(root);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    // ---------- 小时颜色 ----------

    private void buildColorSection(LinearLayout root) {
        addSectionLabel(root, R.string.settings_color_title);
        addSubLabel(root, R.string.settings_color_hint);

        int current = ClockSecondsConfig.getHourColor(getContentResolver());
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(5);
        for (int color : HOUR_COLORS) {
            grid.addView(makeSwatch(color, color == current, grid));
        }
        root.addView(grid);
    }

    private View makeSwatch(int color, boolean selected, GridLayout grid) {
        View swatch = new View(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = dp(40);
        lp.height = dp(40);
        lp.setMargins(dp(6), dp(6), dp(6), dp(6));
        swatch.setLayoutParams(lp);
        swatch.setBackground(swatchDrawable(color, selected));
        swatch.setOnClickListener(v -> selectHourColor(color, grid));
        return swatch;
    }

    /** 圆形色块；选中项加一圈蓝色描边。 */
    private GradientDrawable swatchDrawable(int color, boolean selected) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(color);
        d.setStroke(selected ? dp(3) : dp(1), selected ? 0xFF2196F3 : 0x33000000);
        return d;
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
            } else {
                toastWriteFailed();
            }
        });
    }

    // ---------- 更新 ----------

    private void buildUpdateSection(LinearLayout root) {
        addSectionLabel(root, R.string.settings_section_general);

        // 自动检查更新（开关 + 副标题，整体一行：左文字右开关）
        LinearLayout switchRow = new LinearLayout(this);
        switchRow.setOrientation(LinearLayout.HORIZONTAL);
        switchRow.setGravity(Gravity.CENTER_VERTICAL);
        switchRow.setPadding(dp(12), dp(12), dp(12), 0);

        TextView autoLabel = new TextView(this);
        autoLabel.setText(R.string.settings_update_auto);
        autoLabel.setTextSize(16f);
        autoLabel.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        switchRow.addView(autoLabel);

        Switch autoSwitch = new Switch(this);
        autoSwitch.setChecked(prefs().getBoolean(KEY_AUTO_UPDATE, true));
        autoSwitch.setOnCheckedChangeListener((v, checked) -> {
            prefs().edit().putBoolean(KEY_AUTO_UPDATE, checked).apply();
            if (checked) {
                // 刚打开自动检查：立即给出一次反馈（有新版弹窗 / 否则提示已最新）。
                runUpdateCheck(true);
            }
        });
        switchRow.addView(autoSwitch);
        root.addView(switchRow);

        addSubLabel(root, R.string.settings_update_auto_sum);

        Button check = new Button(this);
        check.setText(R.string.settings_update_check);
        check.setOnClickListener(v -> runUpdateCheck(true));
        root.addView(check);

        addRow(root, R.string.settings_update_open, R.string.settings_update_open_sum,
                v -> openUrl(RELEASES_URL));
    }

    private void buildAboutSection(LinearLayout root) {
        addSectionLabel(root, R.string.settings_section_about);

        LinearLayout versionRow = addRow(root, getString(R.string.settings_about_version,
                BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE), null, null);
        if (BuildConfig.DEBUG) {
            // 仅供真机验证「发现新版本」弹窗与跳转链路（release 构建自动排除）。
            versionRow.setOnLongClickListener(v -> {
                showUpdateDialog("9.9.9");
                return true;
            });
        }
        addRow(root, R.string.settings_about_github, R.string.settings_about_github_sum,
                v -> openUrl(REPO_URL));
        addRow(root, R.string.settings_about_licenses, R.string.settings_about_licenses_sum,
                v -> showLicensesDialog());
    }

    /** 打开设置页时按偏好自动检查一次（静默：无新版不打扰）。 */
    private void maybeAutoCheckUpdate() {
        if (prefs().getBoolean(KEY_AUTO_UPDATE, true)) {
            runUpdateCheck(false);
        }
    }

    /**
     * @param manual true = 用户手动触发，需要 Toast 反馈；false = 自动触发，无新版不打扰
     */
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
                    Toast.makeText(this, R.string.update_failed,
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
            if (remoteVersion != null
                    && UpdateChecker.compareVersions(remoteVersion,
                    BuildConfig.VERSION_NAME) > 0) {
                showUpdateDialog(remoteVersion);
            } else if (manual) {
                Toast.makeText(this, R.string.update_latest,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateDialog(String remoteVersion) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.update_dialog_title)
                .setMessage(getString(R.string.update_dialog_msg,
                        remoteVersion, BuildConfig.VERSION_NAME))
                .setPositiveButton(R.string.update_go,
                        (d, w) -> openUrl(RELEASES_URL))
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

    // ---------- 原有逻辑 ----------

    private void refreshStatus() {
        boolean enabled = ClockSecondsConfig.isEnabled(getContentResolver());
        statusView.setText(enabled
                ? R.string.settings_enabled
                : R.string.settings_disabled);
    }

    /**
     * 写 Secure 配置：优先直接写；普通应用进程没有 WRITE_SECURE_SETTINGS 时，
     * 发广播交给 SystemUI 内的模块代写（SystemUI 是系统应用，天然具备该权限）。
     * 广播路径是异步的，稍后读回校验再把结果回调出去。
     */
    private void applyConfig(final String key, final int value, final Intent broadcast,
                             final java.util.function.Consumer<Boolean> onResult) {
        try {
            Settings.Secure.putInt(getContentResolver(), key, value);
            onResult.accept(true);
            return;
        } catch (SecurityException ignored) {
            // 无 WRITE_SECURE_SETTINGS：走下面的广播路径
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

    /** 广播路径是异步的：最多轮询约 2.4 秒确认值已落地（SystemUI 忙时可能晚几拍）。 */
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

    // ---------- 视图小工具 ----------

    private void addSectionLabel(LinearLayout parent, int textRes) {
        TextView t = new TextView(this);
        t.setText(textRes);
        t.setTextSize(16f);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        t.setPadding(0, dp(24), 0, dp(8));
        parent.addView(t);
    }

    private void addSubLabel(LinearLayout parent, int textRes) {
        TextView t = new TextView(this);
        t.setText(textRes);
        t.setTextSize(13f);
        t.setPadding(dp(12), 0, dp(12), dp(4));
        t.setTextColor(secondaryTextColor());
        parent.addView(t);
    }

    /**
     * 列表行（字符串资源版）：主标题 + 可选副标题，可点击（带系统 selectable 反馈）。
     * listener 为 null 时纯展示（如版本号）。返回行视图便于附加监听。
     */
    private LinearLayout addRow(LinearLayout parent, int titleRes,
                        Integer subtitleRes, View.OnClickListener listener) {
        return addRow(parent, getText(titleRes), subtitleRes, listener);
    }

    private LinearLayout addRow(LinearLayout parent, CharSequence title,
                        Integer subtitleRes, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        if (listener != null) {
            TypedValue tv = new TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackground, tv, true);
            if (tv.resourceId != 0) {
                row.setBackgroundResource(tv.resourceId);
            }
            row.setClickable(true);
            row.setFocusable(true);
            row.setOnClickListener(listener);
        }

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(16f);
        row.addView(t);

        if (subtitleRes != null) {
            TextView s = new TextView(this);
            s.setText(subtitleRes);
            s.setTextSize(13f);
            s.setTextColor(secondaryTextColor());
            row.addView(s);
        }
        parent.addView(row);
        return row;
    }

    private int secondaryTextColor() {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorSecondary, tv, true);
        if (tv.resourceId != 0) {
            return getResources().getColor(tv.resourceId, getTheme());
        }
        return tv.data;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
