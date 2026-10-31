package io.github.kelebao.colorosclockseconds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import org.junit.Test;

/**
 * ClockTask 纯函数单元测试（不依赖 Android 运行时）：越界归一化、
 * 窗口判定（含跨午夜）、下一次翻转时刻、拷贝独立性。
 * 全部用默认时区的 Calendar 构造时刻，与被测代码同一时区基准。
 */
public class ClockTaskTest {

    private static long at(int year, int month, int day, int hour, int minute, int second) {
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(year, month, day, hour, minute, second);
        return c.getTimeInMillis();
    }

    private static ClockTask daily(int hour, int minute, int advance, int duration) {
        ClockTask t = new ClockTask();
        t.trigger = ClockTask.TRIGGER_DAILY;
        t.hour = hour;
        t.minute = minute;
        t.advanceMin = advance;
        t.durationMin = duration;
        return t;
    }

    private static ClockTask hourly(int advance, int duration) {
        ClockTask t = new ClockTask();
        t.trigger = ClockTask.TRIGGER_HOURLY;
        t.advanceMin = advance;
        t.durationMin = duration;
        return t;
    }

    @Test
    public void normalize_clampsAllFields() {
        ClockTask t = new ClockTask();
        t.trigger = 99;
        t.hour = 25;
        t.minute = -5;
        t.advanceMin = 999;
        t.durationMin = 0;
        t.action = ClockTask.ACTION_LEGACY_HIDE;
        t.name = null;
        t.normalize();
        assertEquals(ClockTask.TRIGGER_HOURLY, t.trigger);
        assertEquals(1, t.hour);
        assertEquals(55, t.minute);
        assertEquals(ClockTask.ADVANCE_MAX, t.advanceMin);
        assertEquals(ClockTask.DURATION_MIN, t.durationMin);
        assertEquals(ClockTask.ACTION_SHOW, t.action);
        assertEquals("", t.name);
    }

    @Test
    public void normalize_durationCapsAtMax() {
        ClockTask t = new ClockTask();
        t.durationMin = 9999;
        t.normalize();
        assertEquals(ClockTask.DURATION_MAX, t.durationMin);
    }

    @Test
    public void windowContains_daily_insideAndBoundaries() {
        // 12:40 提前 2 分钟、持续 5 分钟 → 窗口 [12:38, 12:43)
        ClockTask t = daily(12, 40, 2, 5);
        assertTrue(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 12, 40, 0)));
        assertTrue(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 12, 38, 0)));
        assertFalse(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 12, 37, 59)));
        assertFalse(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 12, 43, 0)));
    }

    @Test
    public void windowContains_hourly_matchesHandoverCase() {
        // 交接档案里的真实任务：每整点提前 2 分钟、持续 1 分钟 → 窗口 [:58, :59)
        ClockTask t = hourly(2, 1);
        assertTrue(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 10, 58, 30)));
        assertFalse(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 10, 59, 0)));
        assertFalse(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 11, 0, 0)));
    }

    @Test
    public void windowContains_daily_wrapsPastMidnight() {
        // 00:05 提前 10 分钟、持续 3 分钟 → 前一天 23:55~23:58
        ClockTask t = daily(0, 5, 10, 3);
        assertTrue(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 23, 56, 0)));
        assertFalse(t.windowContains(at(2026, Calendar.NOVEMBER, 1, 23, 54, 59)));
        assertFalse(t.windowContains(at(2026, Calendar.NOVEMBER, 2, 0, 6, 0)));
    }

    @Test
    public void nextTransition_daily_returnsStartThenEndThenTomorrow() {
        ClockTask t = daily(12, 40, 2, 5);
        long start = at(2026, Calendar.NOVEMBER, 1, 12, 38, 0);
        long end = start + 5 * 60_000L;
        assertEquals(start, t.nextTransition(at(2026, Calendar.NOVEMBER, 1, 12, 0, 0)));
        assertEquals(end, t.nextTransition(at(2026, Calendar.NOVEMBER, 1, 12, 39, 0)));
        assertEquals(start + 86_400_000L,
                t.nextTransition(at(2026, Calendar.NOVEMBER, 1, 12, 43, 1)));
    }

    @Test
    public void nextTransition_hourly_returnsNextWindowStart() {
        ClockTask t = hourly(2, 1);
        assertEquals(at(2026, Calendar.NOVEMBER, 1, 10, 58, 0),
                t.nextTransition(at(2026, Calendar.NOVEMBER, 1, 10, 57, 0)));
    }

    @Test
    public void copy_isIndependent() {
        ClockTask t = daily(12, 40, 2, 5);
        t.name = "orig";
        ClockTask c = t.copy();
        c.name = "copy";
        c.durationMin = 9;
        assertEquals("orig", t.name);
        assertEquals(5, t.durationMin);
        assertNotEquals(c.durationMin, t.durationMin);
    }
}
