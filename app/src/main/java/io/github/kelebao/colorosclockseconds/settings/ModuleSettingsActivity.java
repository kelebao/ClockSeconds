package io.github.kelebao.colorosclockseconds.settings;

import android.app.Activity;
import android.content.ContentResolver;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;

/**
 * 模块设置页（从 LSPosed 管理器打开，桌面不生成图标）。
 *
 * <p>说明：普通应用进程写入 {@link Settings.Secure} 需要 {@code WRITE_SECURE_SETTINGS}。
 * 若未授予，页面仅展示状态与手势偏好，真正的切换请用"点击状态栏时钟"完成
 * （该动作运行在 SystemUI 进程内，天然具备权限）。</p>
 */
public final class ModuleSettingsActivity extends Activity {

    private TextView statusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(io.github.kelebao.colorosclockseconds.R.string.settings_title);
        setContentView(buildContentView());
        refreshStatus();
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
        statusTitle.setText(io.github.kelebao.colorosclockseconds.R.string.settings_status_title);
        statusTitle.setTextSize(16f);
        statusTitle.setPadding(0, 0, 0, dp(8));
        root.addView(statusTitle);

        statusView = new TextView(this);
        statusView.setTextSize(20f);
        statusView.setPadding(0, 0, 0, dp(16));
        root.addView(statusView);

        Button refresh = new Button(this);
        refresh.setText(io.github.kelebao.colorosclockseconds.R.string.settings_refresh);
        refresh.setOnClickListener(v -> refreshStatus());
        root.addView(refresh);

        TextView gestureTitle = new TextView(this);
        gestureTitle.setText(io.github.kelebao.colorosclockseconds.R.string.settings_gesture_title);
        gestureTitle.setTextSize(16f);
        gestureTitle.setPadding(0, dp(24), 0, dp(8));
        root.addView(gestureTitle);

        RadioGroup group = new RadioGroup(this);
        RadioButton click = new RadioButton(this);
        click.setId(1);
        click.setText(io.github.kelebao.colorosclockseconds.R.string.settings_gesture_click);
        RadioButton longPress = new RadioButton(this);
        longPress.setId(2);
        longPress.setText(io.github.kelebao.colorosclockseconds.R.string.settings_gesture_long_press);
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
            if (writeGesture(resolver, gesture)) {
                lastChecked[0] = checkedId;
            } else {
                // 写入失败（缺 WRITE_SECURE_SETTINGS）：回滚选中项，避免"假成功"。
                reverting[0] = true;
                g.check(lastChecked[0]);
                reverting[0] = false;
            }
        });
        root.addView(group);

        TextView hint = new TextView(this);
        hint.setText(io.github.kelebao.colorosclockseconds.R.string.settings_hint);
        hint.setTextSize(13f);
        hint.setGravity(Gravity.START);
        hint.setPadding(0, dp(24), 0, 0);
        root.addView(hint);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return scroll;
    }

    private void refreshStatus() {
        boolean enabled = ClockSecondsConfig.isEnabled(getContentResolver());
        statusView.setText(enabled
                ? io.github.kelebao.colorosclockseconds.R.string.settings_enabled
                : io.github.kelebao.colorosclockseconds.R.string.settings_disabled);
    }

    private boolean writeGesture(ContentResolver resolver, int gesture) {
        try {
            ClockSecondsConfig.setGesture(resolver, gesture);
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            return true;
        } catch (SecurityException e) {
            Toast.makeText(this, "无写入权限，需通过 adb 授予 WRITE_SECURE_SETTINGS", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
