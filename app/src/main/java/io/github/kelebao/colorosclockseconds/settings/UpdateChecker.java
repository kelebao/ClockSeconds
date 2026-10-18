package io.github.kelebao.colorosclockseconds.settings;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import io.github.kelebao.colorosclockseconds.BuildConfig;

/**
 * GitHub Releases 版本检查（供设置页弹窗提醒用）。
 *
 * <p>数据源为 LSPosed 模块仓库的 Releases API：模块仓库 tag 形如
 * {@code 5-1.0.4}（{versionCode}-{versionName}），取最后一段即版本名，
 * 与 {@link BuildConfig#VERSION_NAME} 比较。</p>
 *
 * <p>设计原则：只读一个 JSON；任何网络/解析失败都只回调 failed=true，
 * 绝不影响设置页正常使用。GitHub 未认证 API 限额 60 次/小时/IP，
 * 自动检查仅在打开设置页时触发一次，手动检查由用户点击触发。</p>
 */
public final class UpdateChecker {

    private static final String TAG = "ClockSecondsUpdate";

    private static final String API_URL =
            "https://api.github.com/repos/Xposed-Modules-Repo/"
                    + "io.github.kelebao.colorosclockseconds/releases/latest";

    /**
     * 检查结果回调（保证在主线程调用）。
     *
     * @param remoteVersion 远端版本名（如 {@code 1.0.5}）；请求失败或无有效版本时为 null
     * @param failed        true 表示请求/解析失败（区别于"已是最新"）
     */
    public interface Callback {
        void onResult(String remoteVersion, boolean failed);
    }

    private UpdateChecker() {
    }

    /** 异步检查；结果经 {@code handler} 回主线程。失败时 remoteVersion=null、failed=true。 */
    public static void check(Context context, Callback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String remote = null;
            boolean failed = false;
            HttpURLConnection conn = null;
            try {
                URL url = new URL(API_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                conn.setRequestProperty("User-Agent", "ClockSeconds-Module");
                int code = conn.getResponseCode();
                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "GET releases/latest -> HTTP " + code);
                }
                if (code == HttpURLConnection.HTTP_OK) {
                    StringBuilder sb = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(),
                                    StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    }
                    JSONObject json = new JSONObject(sb.toString());
                    String tag = json.optString("tag_name", "");
                    // 模块仓库 tag = "{versionCode}-{versionName}"，如 "5-1.0.4"
                    int idx = tag.lastIndexOf('-');
                    if (idx >= 0 && idx + 1 < tag.length()) {
                        remote = tag.substring(idx + 1).trim();
                    }
                } else {
                    failed = true;
                }
            } catch (Exception e) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "update check failed", e);
                }
                failed = true;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            final String result = remote;
            final boolean isFailed = failed;
            main.post(() -> callback.onResult(result, isFailed));
        }).start();
    }

    /**
     * 版本比较：a 比 b 新返回正数，相同返回 0，旧返回负数。
     * 按 "." 分段逐段比对数字；任一非数字段按 0 处理，无法比较时退化为字符串比较。
     */
    public static int compareVersions(String a, String b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return -1;
        }
        if (b == null) {
            return 1;
        }
        String[] pa = a.split("\\.");
        String[] pb = b.split("\\.");
        int n = Math.max(pa.length, pb.length);
        for (int i = 0; i < n; i++) {
            int va = i < pa.length ? parseIntSafe(pa[i]) : 0;
            int vb = i < pb.length ? parseIntSafe(pb[i]) : 0;
            if (va != vb) {
                return va - vb;
            }
        }
        return 0;
    }

    private static int parseIntSafe(String s) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
