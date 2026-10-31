package io.github.kelebao.colorosclockseconds;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * 一条「自定义任务」：到点临时打开状态栏秒数，或把状态栏时钟换成记录的配色。
 *
 * <p>触发点有四种：</p>
 * <ul>
 *   <li>{@link #TRIGGER_HOURLY}：每整点（每小时 :00）——「每整点显秒」用的就是它；</li>
 *   <li>{@link #TRIGGER_DAILY}：每天固定 HH:mm；</li>
 *   <li>{@link #TRIGGER_WEEKLY}：每 7 天，同一星期几的 HH:mm；</li>
 *   <li>{@link #TRIGGER_ONCE}：自定义时刻（epoch 毫秒），到点一次后自动停用。</li>
 * </ul>
 *
 * <p>窗口 = {@code [触发点 - advanceMin, 触发点 - advanceMin + durationMin)}，
 * 即「提前 N 分钟开始」，持续 M 分钟后自动结束。窗口可以跨整点/跨天。</p>
 *
 * <p>动作：窗口内强制显秒（{@link #ACTION_SHOW}，即使总开关关着）；或到点把记录的
 * 配色（六位数字色 + 渐变起止）写进 Secure（{@link #ACTION_COLOR}，瞬时动作，
 * duration 强制 1 分钟只用于跨点判定）。旧数据 action=1 的任务读取时归为显秒。</p>
 *
 * <p>本类不碰 Android 设置存储，序列化格式由 {@link #toJson(List)} / {@link #parse(String)}
 * 负责，读写 Secure 见 {@link ClockSecondsConfig#getTasksJson}。</p>
 */
public final class ClockTask {

    public static final int TRIGGER_HOURLY = 0;
    public static final int TRIGGER_DAILY = 1;
    /** 每 7 天（每周），同一星期几的同一时刻。 */
    public static final int TRIGGER_WEEKLY = 2;
    /** 自定义时刻，一次性；触发后由模块自动停用。 */
    public static final int TRIGGER_ONCE = 3;

    public static final int ACTION_SHOW = 0;
    /** 已废弃：旧数据可能仍带 1，读取时归一为 {@link #ACTION_SHOW}。 */
    public static final int ACTION_LEGACY_HIDE = 1;
    /** 到点把记录的配色写进 Secure（六位数字色 + 渐变起止）。 */
    public static final int ACTION_COLOR = 2;

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
    /** {@link #TRIGGER_WEEKLY} 用：星期几，取 {@link Calendar#DAY_OF_WEEK} 值（1=周日 … 7=周六）。 */
    public int weekDay = Calendar.MONDAY;
    /** {@link #TRIGGER_ONCE} 用：到点时刻（epoch 毫秒），0 表示还没设置。 */
    public long onceAt = 0L;
    public int advanceMin = 0;
    public int durationMin = 5;
    public int action = ACTION_SHOW;
    /** {@link #ACTION_COLOR} 用：六位数字色（下标即秒表 slot，值 {@link ClockSecondsConfig#DIGIT_COLOR_FOLLOW} 表示跟随系统）。 */
    public int[] colors;
    /** {@link #ACTION_COLOR} 用：渐变起止（{@link ClockSecondsConfig#GRADIENT_COLOR_AUTO} 表示跟随数字色）。 */
    public int gradStart = ClockSecondsConfig.GRADIENT_COLOR_AUTO;
    public int gradEnd = ClockSecondsConfig.GRADIENT_COLOR_AUTO;
    /** 多套配色方案（「选择配色」支持多选）；每套 8 个 int = 6 位数字色 + 渐变起 + 渐变止，每次触发轮换应用一套。 */
    public int[][] schemes;
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
        t.weekDay = weekDay;
        t.onceAt = onceAt;
        t.advanceMin = advanceMin;
        t.durationMin = durationMin;
        t.action = action;
        t.colors = colors == null ? null : colors.clone();
        t.gradStart = gradStart;
        t.gradEnd = gradEnd;
        if (schemes != null) {
            t.schemes = new int[schemes.length][];
            for (int i = 0; i < schemes.length; i++) {
                t.schemes[i] = schemes[i].clone();
            }
        }
        t.enabled = enabled;
        return t;
    }

    /** 归一化越界值，任何入口写入前都先调用。 */
    public void normalize() {
        if (trigger != TRIGGER_DAILY && trigger != TRIGGER_WEEKLY && trigger != TRIGGER_ONCE) {
            trigger = TRIGGER_HOURLY;
        }
        // 旧数据 action=1（已废弃的「关秒」）与未知值一律归为显秒。
        if (action != ACTION_SHOW && action != ACTION_COLOR) {
            action = ACTION_SHOW;
        }
        hour = ((hour % 24) + 24) % 24;
        minute = ((minute % 60) + 60) % 60;
        if (weekDay < Calendar.SUNDAY || weekDay > Calendar.SATURDAY) {
            weekDay = Calendar.MONDAY;
        }
        if (trigger == TRIGGER_ONCE) {
            if (onceAt <= 0L) {
                // 只给了时刻没给时间戳：按今天（或明天，若已过）的 hour:minute 兜底
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, minute);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                onceAt = c.getTimeInMillis();
            }
        } else {
            onceAt = 0L;
        }
        advanceMin = Math.max(0, Math.min(ADVANCE_MAX, advanceMin));
        durationMin = Math.max(DURATION_MIN, Math.min(DURATION_MAX, durationMin));
        if (action == ACTION_COLOR) {
            // 换配色是瞬时动作：窗口收成 1 分钟，只用于跨点判定
            durationMin = DURATION_MIN;
            advanceMin = 0;
        }
        if (schemes != null && schemes.length > 0 && schemes[0] != null && schemes[0].length >= 2) {
            int[] first = schemes[0];
            gradStart = first[0];
            gradEnd = first[first.length - 1];
        }
        if (colors == null || colors.length != 6) {
            int[] c6 = new int[6];
            for (int i = 0; i < 6; i++) {
                c6[i] = colors != null && i < colors.length
                        ? colors[i] : ClockSecondsConfig.DIGIT_COLOR_FOLLOW;
            }
            colors = c6;
        }
        if (name == null) {
            name = "";
        }
    }

    /** 当前时刻是否落在本任务的窗口内。 */
    public boolean windowContains(long nowMs) {
        if (trigger == TRIGGER_ONCE) {
            long start = onceAt - advanceMin * 60_000L;
            long end = start + Math.max(1, durationMin) * 60_000L;
            return nowMs >= start && nowMs < end;
        }
        long period = trigger == TRIGGER_WEEKLY ? 7L * 86_400_000L
                : trigger == TRIGGER_DAILY ? 86_400_000L : 3_600_000L;
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
        if (trigger == TRIGGER_ONCE) {
            long start = onceAt - advanceMin * 60_000L;
            long end = start + Math.max(1, durationMin) * 60_000L;
            if (start > nowMs) {
                return start;
            }
            if (end > nowMs) {
                return end;
            }
            return Long.MAX_VALUE;
        }
        long period = trigger == TRIGGER_WEEKLY ? 7L * 86_400_000L
                : trigger == TRIGGER_DAILY ? 86_400_000L : 3_600_000L;
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

    /** 本任务所在周期的锚点：hourly 取当前小时的 :00，daily 取今天的 hour:minute，weekly 取本周该星期几的 hour:minute，once 取 onceAt。 */
    private long anchorFor(long nowMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(nowMs);
        if (trigger == TRIGGER_DAILY) {
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
        } else if (trigger == TRIGGER_WEEKLY) {
            c.set(Calendar.DAY_OF_WEEK, weekDay);
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
        } else if (trigger == TRIGGER_ONCE) {
            c.setTimeInMillis(onceAt);
        } else {
            c.set(Calendar.MINUTE, 0);
        }
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    /** 星期几的本地短名（如「周一」/「Mon」），summary 用。 */
    private static String weekDayName(int weekDay) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, weekDay);
        String name = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
        return name == null ? String.valueOf(weekDay) : name;
    }

    /** 卡片/列表里显示的一行摘要。 */
    public String summary(Context ctx) {
        String when;
        if (trigger == TRIGGER_ONCE) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(onceAt);
            when = ctx.getString(R.string.task_when_once,
                    String.format(Locale.US, "%02d-%02d", c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)),
                    String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)));
        } else if (trigger == TRIGGER_WEEKLY) {
            when = ctx.getString(R.string.task_when_weekly, weekDayName(weekDay),
                    String.format(Locale.US, "%02d:%02d", hour, minute));
            if (advanceMin > 0) {
                when = when + ctx.getString(R.string.task_advance_suffix, advanceMin);
            }
        } else if (trigger == TRIGGER_DAILY) {
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
        if (action == ACTION_COLOR) {
            return ctx.getString(R.string.task_fmt_color, when);
        }
        return ctx.getString(R.string.task_fmt_show, when, durationMin);
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
            o.put("wd", weekDay);
            o.put("once", onceAt);
            o.put("advance", advanceMin);
            o.put("duration", durationMin);
            o.put("action", action);
            if (action == ACTION_COLOR && colors != null) {
                JSONArray cs = new JSONArray();
                for (int i = 0; i < colors.length; i++) {
                    cs.put(colors[i]);
                }
                o.put("colors", cs);
                o.put("gs", gradStart);
                o.put("ge", gradEnd);
                if (schemes != null && schemes.length > 0) {
                    JSONArray sc = new JSONArray();
                    for (int[] one : schemes) {
                        JSONArray row = new JSONArray();
                        for (int v : one) {
                            row.put(v);
                        }
                        sc.put(row);
                    }
                    o.put("sc", sc);
                }
            }
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
                t.weekDay = o.optInt("wd", Calendar.MONDAY);
                t.onceAt = o.optLong("once", 0L);
                t.advanceMin = o.optInt("advance", 0);
                t.durationMin = o.optInt("duration", 5);
                t.action = o.optInt("action", ACTION_SHOW);
                JSONArray cs = o.optJSONArray("colors");
                if (cs != null) {
                    int[] c6 = new int[6];
                    for (int k = 0; k < 6; k++) {
                        c6[k] = (int) cs.optLong(k, ClockSecondsConfig.DIGIT_COLOR_FOLLOW);
                    }
                    t.colors = c6;
                }
                t.gradStart = (int) o.optLong("gs", ClockSecondsConfig.GRADIENT_COLOR_AUTO);
                t.gradEnd = (int) o.optLong("ge", ClockSecondsConfig.GRADIENT_COLOR_AUTO);
                JSONArray sc = o.optJSONArray("sc");
                if (sc != null && sc.length() > 0) {
                    int[][] scArr = new int[sc.length()][];
                    for (int si = 0; si < sc.length(); si++) {
                        JSONArray row = sc.optJSONArray(si);
                        if (row == null || row.length() < 2) {
                            scArr = null;
                            break;
                        }
                        int[] one = new int[]{ (int) row.optLong(0, 0L), (int) row.optLong(1, 0L) };
                        scArr[si] = one;
                    }
                    t.schemes = scArr;
                }
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
