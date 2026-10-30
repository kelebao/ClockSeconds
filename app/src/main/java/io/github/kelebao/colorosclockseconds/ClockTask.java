package io.github.kelebao.colorosclockseconds;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 一条「自定义任务」：在每天的某个时刻附近，临时打开或关闭状态栏秒数。
 *
 * <p>触发点有两种：</p>
 * <ul>
 *   <li>{@link #TRIGGER_HOURLY}：每整点（每小时 :00）——「每整点显秒」用的就是它；</li>
 *   <li>{@link #TRIGGER_DAILY}：每天固定 HH:mm。</li>
 * </ul>
 *
 * <p>窗口 = {@code [触发点 - advanceMin, 触发点 - advanceMin + durationMin)}，
 * 即「提前 N 分钟开始」，持续 M 分钟后自动结束。窗口可以跨整点/跨天。</p>
 *
 * <p>动作：{@link #ACTION_SHOW} 窗口内强制显秒（即使总开关关着），
 * {@link #ACTION_HIDE} 窗口内强制关闭秒（即使总开关开着）。两者同时命中时「关闭」优先。</p>
 *
 * <p>本类不碰 Android 设置存储，序列化格式由 {@link #toJson(List)} / {@link #parse(String)}
 * 负责，读写 Secure 见 {@link ClockSecondsConfig#getTasksJson}。</p>
 */
public final class ClockTask {

    public static final int TRIGGER_HOURLY = 0;
    public static final int TRIGGER_DAILY = 1;

    public static final int ACTION_SHOW = 0;
    public static final int ACTION_HIDE = 1;

    /** 提前量的上限（分钟）。 */
    public static final int ADVANCE_MAX = 59;
    /** 持续时间的范围（分钟）。 */
    public static final int DURATION_MIN = 1;
    public static final int DURATION_MAX = 180;

    public long id;
    public String name = "";
    public int trigger = TRIGGER_HOURLY;
    public int hour = 0;
    public int minute = 0;
    public int advanceMin = 0;
    public int durationMin = 5;
    public int action = ACTION_SHOW;
    public boolean enabled = true;

    public ClockTask() {
    }

    public ClockTask copy() {
        ClockTask t = new ClockTask();
        t.id = id;
        t.name = name;
        t.trigger = trigger;
        t.hour = hour;
        t.minute = minute;
        t.advanceMin = advanceMin;
        t.durationMin = durationMin;
        t.action = action;
        t.enabled = enabled;
        return t;
    }

    /** 归一化越界值，任何入口写入前都先调用。 */
    public void normalize() {
        if (trigger != TRIGGER_DAILY) {
            trigger = TRIGGER_HOURLY;
        }
        if (action != ACTION_HIDE) {
            action = ACTION_SHOW;
        }
        hour = ((hour % 24) + 24) % 24;
        minute = ((minute % 60) + 60) % 60;
        advanceMin = Math.max(0, Math.min(ADVANCE_MAX, advanceMin));
        durationMin = Math.max(DURATION_MIN, Math.min(DURATION_MAX, durationMin));
        if (name == null) {
            name = "";
        }
    }

    /** 当前时刻是否落在本任务的窗口内。 */
    public boolean windowContains(long nowMs) {
        long period = trigger == TRIGGER_DAILY ? 86_400_000L : 3_600_000L;
        long anchor = anchorFor(nowMs);
        for (int k = -1; k <= 1; k++) {
            long start = anchor + (long) k * period - advanceMin * 60_000L;
            long end = start + Math.max(1, durationMin) * 60_000L;
            if (nowMs >= start && nowMs < end) {
                return true;
            }
        }
        return false;
    }

    /** 下一次状态翻转的时刻（窗口开始或结束）；没有未来翻转时返回 {@link Long#MAX_VALUE}。 */
    public long nextTransition(long nowMs) {
        long period = trigger == TRIGGER_DAILY ? 86_400_000L : 3_600_000L;
        long anchor = anchorFor(nowMs);
        long best = Long.MAX_VALUE;
        for (int k = -1; k <= 2; k++) {
            long start = anchor + (long) k * period - advanceMin * 60_000L;
            long end = start + Math.max(1, durationMin) * 60_000L;
            if (start > nowMs && start < best) {
                best = start;
            }
            if (end > nowMs && end < best) {
                best = end;
            }
        }
        return best;
    }

    /** 本任务所在周期的锚点：hourly 取当前小时的 :00，daily 取今天的 hour:minute。 */
    private long anchorFor(long nowMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowMs);
        if (trigger == TRIGGER_DAILY) {
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
        } else {
            c.set(Calendar.MINUTE, 0);
        }
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** 卡片/列表里显示的一行摘要，如「每整点前 2 分钟显秒，持续 5 分钟」。 */
    public String summary(Context ctx) {
        String when;
        if (trigger == TRIGGER_DAILY) {
            when = ctx.getString(R.string.task_when_daily,
                    String.format(Locale.US, "%02d:%02d", hour, minute));
            if (advanceMin > 0) {
                when = when + ctx.getString(R.string.task_advance_suffix, advanceMin);
            }
        } else if (advanceMin > 0) {
            when = ctx.getString(R.string.task_when_hourly_advance, advanceMin);
        } else {
            when = ctx.getString(R.string.task_when_hourly);
        }
        return action == ACTION_HIDE
                ? ctx.getString(R.string.task_fmt_hide, when, durationMin)
                : ctx.getString(R.string.task_fmt_show, when, durationMin);
    }

    // ==================================================================
    // 序列化
    // ==================================================================

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("name", name == null ? "" : name);
            o.put("trigger", trigger);
            o.put("hour", hour);
            o.put("minute", minute);
            o.put("advance", advanceMin);
            o.put("duration", durationMin);
            o.put("action", action);
            o.put("enabled", enabled);
        } catch (Throwable ignored) {
            // JSONObject.put 只在 key 为 null 时抛，这里不可能
        }
        return o;
    }

    public static String toJson(List<ClockTask> tasks) {
        JSONArray arr = new JSONArray();
        if (tasks != null) {
            for (ClockTask t : tasks) {
                if (t != null) {
                    arr.put(t.toJson());
                }
            }
        }
        return arr.toString();
    }

    public static List<ClockTask> parse(String json) {
        List<ClockTask> out = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) {
            return out;
        }
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) {
                    continue;
                }
                ClockTask t = new ClockTask();
                t.id = o.optLong("id", 0L);
                t.name = o.optString("name", "");
                t.trigger = o.optInt("trigger", TRIGGER_HOURLY);
                t.hour = o.optInt("hour", 0);
                t.minute = o.optInt("minute", 0);
                t.advanceMin = o.optInt("advance", 0);
                t.durationMin = o.optInt("duration", 5);
                t.action = o.optInt("action", ACTION_SHOW);
                t.enabled = o.optBoolean("enabled", true);
                t.normalize();
                if (t.id == 0L) {
                    t.id = System.currentTimeMillis() + i;
                }
                out.add(t);
            }
        } catch (Throwable ignored) {
            // 配置损坏：当作没有任务，避免整个模块被一条坏数据拖垮
        }
        return out;
    }
}
