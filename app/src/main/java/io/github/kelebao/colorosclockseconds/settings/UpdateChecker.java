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
 * <p>数据源为 LSPosed 模块仓库的 Releases：模块仓库 tag 形如
 * {@code 5-1.0.4}（{versionCode}-{versionName}），取最后一段即版本名，
 * 与 {@link BuildConfig#VERSION_NAME} 比较。</p>
 *
 * <p>三级取数（U-20261119③）：GitHub 未认证 API 限额 60 次/小时/IP，
 * 国内运营商多为 CGNAT，同一个出口 IP 被大量用户共用，很容易被别的用户
 * 把额度耗尽，此时 API 一律返回 403，表现为「检测更新失败」。所以：
 * <ol>
 *   <li>先走 API {@code /releases/latest}（体积最小）；</li>
 *   <li>失败则走网页 {@code /releases/latest} 的 302 跳转，只读
 *       {@code Location} 头（不下载页面，几 KB 都不到）；</li>
 *   <li>再失败则读 {@code /releases.atom}（约 40KB，取第一条 entry）。</li>
 * </ol>
 * 网页与 atom 不受 API 限额约束，任一路成功即视为成功。</p>
 *
 * <p>设计原则：任何网络/解析失败都只回调 failed=true，绝不影响设置页正常使用。</p>
 */
public final class UpdateChecker {

    private static final String TAG = "ClockSecondsUpdate";

    /** 模块仓库（LSPosed 模块仓库，tag = versionCode-versionName）。 */
    private static final String REPO =
            "https://github.com/Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds";

    /** 一级：GitHub API（限额 60 次/小时/IP，可能被同 IP 的其他用户耗尽）。 */
    private static final String API_URL =
            "https://api.github.com/repos/Xposed-Modules-Repo/"
                    + "io.github.kelebao.colorosclockseconds/releases/latest";

    /** 二级：网页最新 Release（302 跳转到 /releases/tag/<tag>，只读 Location 头）。 */
    private static final String WEB_LATEST_URL = REPO + "/releases/latest";

    /** 三级：Release atom feed（约 40KB，第一条 entry 即最新）。 */
    private static final String ATOM_URL = REPO + "/releases.atom";

    /** 单次请求超时；三级串行最坏 18s，正常情况下失败会在 1s 内返回。 */
    private static final int TIMEOUT_MS = 6000;

    /** atom 最多读这么多字符（正常约 40KB，防止异常响应把内存吃满）。 */
    private static final int ATOM_MAX_CHARS = 256 * 1024;

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

    /** 异步检查；结果经主线程回调。失败时 remoteVersion=null、failed=true。 */
    public static void check(Context context, Callback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        new Thread(() -> {
            String remote = tryApi();
            if (remote == null) {
                remote = tryWebRedirect();
            }
            if (remote == null) {
                remote = tryAtom();
            }
            final String result = remote;
            final boolean isFailed = (remote == null);
            main.post(() -> callback.onResult(result, isFailed));
        }, "update-check").start();
    }

    /** 一级：API JSON。403（限额耗尽）或其他异常都返回 null，交给下一级。 */
    private static String tryApi() {
        HttpURLConnection conn = null;
        try {
            conn = open(API_URL);
            conn.setRequestProperty("Accept", "application/vnd.github+json");
            conn.setRequestProperty("User-Agent", "ClockSeconds-Module");
            int code = conn.getResponseCode();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "GET releases/latest -> HTTP " + code);
            }
            if (code != HttpURLConnection.HTTP_OK) {
                return null;
            }
            String body = readAll(conn, 64 * 1024);
            String tag = new JSONObject(body).optString("tag_name", "");
            return versionFromTag(tag);
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "api check failed", e);
            }
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * 二级：网页 {@code /releases/latest}。不跟随跳转，只读 {@code Location} 头里的
     * {@code /releases/tag/<tag>}。不受 API 限额影响，且几乎不产生流量。
     */
    private static String tryWebRedirect() {
        HttpURLConnection conn = null;
        try {
            conn = open(WEB_LATEST_URL);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent", "ClockSeconds-Module");
            int code = conn.getResponseCode();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "GET web releases/latest -> HTTP " + code);
            }
            String location = conn.getHeaderField("Location");
            return versionFromTagUrl(location);
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "web redirect check failed", e);
            }
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /** 三级：atom feed，取第一条 entry 的 {@code /releases/tag/<tag>}。 */
    private static String tryAtom() {
        HttpURLConnection conn = null;
        try {
            conn = open(ATOM_URL);
            conn.setRequestProperty("User-Agent", "ClockSeconds-Module");
            int code = conn.getResponseCode();
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "GET releases.atom -> HTTP " + code);
            }
            if (code != HttpURLConnection.HTTP_OK) {
                return null;
            }
            return versionFromTagUrl(readAll(conn, ATOM_MAX_CHARS));
        } catch (Throwable e) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "atom check failed", e);
            }
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static HttpURLConnection open(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);
        conn.setRequestMethod("GET");
        return conn;
    }

    private static String readAll(HttpURLConnection conn, int maxChars) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            char[] buf = new char[8192];
            int n;
            while ((n = reader.read(buf)) > 0) {
                if (sb.length() >= maxChars) {
                    break;
                }
                sb.append(buf, 0, n);
            }
        }
        return sb.toString();
    }

    /** 从 {@code .../releases/tag/346-1.3.0} 这类文本里取第一个 tag。 */
    static String versionFromTagUrl(String text) {
        if (text == null) {
            return null;
        }
        int at = text.indexOf("/releases/tag/");
        if (at < 0) {
            return null;
        }
        int start = at + "/releases/tag/".length();
        int end = start;
        while (end < text.length()) {
            char c = text.charAt(end);
            if (c == '"' || c == '\'' || c == '<' || c == '>' || c == ' ' || c == '\n' || c == '\r') {
                break;
            }
            end++;
        }
        return versionFromTag(text.substring(start, end));
    }

    /**
     * 模块仓库 tag = {@code {versionCode}-{versionName}}，如 {@code 346-1.3.0}；
     * 取最后一段即版本名。没有 "-" 时按整串处理，并去掉可能存在的 "v" 前缀。
     */
    static String versionFromTag(String tag) {
        if (tag == null) {
            return null;
        }
        String s = tag.trim();
        if (s.isEmpty()) {
            return null;
        }
        int idx = s.lastIndexOf('-');
        if (idx >= 0 && idx + 1 < s.length()) {
            s = s.substring(idx + 1).trim();
        }
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1).trim();
        }
        return s.isEmpty() ? null : s;
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
