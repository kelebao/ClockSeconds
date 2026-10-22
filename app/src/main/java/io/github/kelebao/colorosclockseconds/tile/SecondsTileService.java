package io.github.kelebao.colorosclockseconds.tile;

import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;
import io.github.kelebao.colorosclockseconds.R;

/**
 * 下拉通知栏「显秒开关」磁贴：一键切换状态栏时间是否显示秒。
 *
 * <p>磁贴运行在模块自身的应用进程（非 SystemUI），本身通常没有 {@code WRITE_SECURE_SETTINGS}，
 * 因此切换走与设置页相同的路径——优先直接写 {@link android.provider.Settings.Secure}，
 * 失败则改发广播由 SystemUI 进程代写。若模块未生效（未在 LSPosed 启用），广播无人接收，
 * 磁贴会在刷新时如实反映「未变更」，不会误导。</p>
 */
public final class SecondsTileService extends TileService {

    private static final long REFRESH_DELAY_MS = 320L;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /** 监听 Secure 键变化：用户在状态栏点击切换后，磁贴也能即时同步。 */
    private final android.database.ContentObserver mObserver =
            new android.database.ContentObserver(mHandler) {
                @Override
                public void onChange(boolean selfChange) {
                    refreshState();
                }
            };

    @Override
    public void onStartListening() {
        super.onStartListening();
        try {
            getContentResolver().registerContentObserver(
                    android.provider.Settings.Secure.getUriFor(
                            ClockSecondsConfig.MODULE_SECONDS_KEY),
                    false, mObserver);
        } catch (Throwable ignored) {
            // 注册失败不影响手动点击
        }
        refreshState();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        try {
            getContentResolver().unregisterContentObserver(mObserver);
        } catch (Throwable ignored) {
            // 忽略
        }
    }

    @Override
    public void onClick() {
        super.onClick();
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        // 当前状态 → 期望反转值。
        boolean enabled = ClockSecondsConfig.isEnabled(getContentResolver());
        boolean desired = !enabled;
        try {
            // 优先直接写（用户若用 adb 授予过权限则直接生效）。
            ClockSecondsConfig.setEnabled(getContentResolver(), desired);
        } catch (SecurityException ignored) {
            // 无权限：改发广播由 SystemUI 代写。
            android.content.Intent intent = new android.content.Intent(
                    ClockSecondsConfig.ACTION_SET_ENABLED);
            intent.setPackage(ClockSecondsConfig.SYSTEM_UI_PACKAGE);
            intent.putExtra(ClockSecondsConfig.EXTRA_ENABLED, desired ? 1 : 0);
            try {
                sendBroadcast(intent);
            } catch (Throwable t) {
                // 发送失败：稍后刷新时会如实反映
            }
        }
        // 广播是异步的，给 SystemUI 一点时间写入后再刷新磁贴状态。
        mHandler.removeCallbacks(mRefresh);
        mHandler.postDelayed(mRefresh, REFRESH_DELAY_MS);
    }

    private final Runnable mRefresh = this::refreshState;

    /** 根据 Secure 键当前值刷新磁贴激活态与文案。 */
    private void refreshState() {
        Tile tile = getQsTile();
        if (tile == null) {
            return;
        }
        boolean enabled = ClockSecondsConfig.isEnabled(getContentResolver());
        tile.setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getString(R.string.tile_label));
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_tile_seconds));
        try {
            tile.updateTile();
        } catch (Throwable ignored) {
            // 某些 ROM 在低版本抛异常，忽略
        }
    }
}
