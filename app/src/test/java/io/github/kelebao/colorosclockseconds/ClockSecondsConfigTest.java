package io.github.kelebao.colorosclockseconds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 纯函数单元测试（不依赖 Android 运行时，可在 JVM 直接跑）。
 * 覆盖手势归一化、秒数补零后缀、小时十位检测。
 */
public class ClockSecondsConfigTest {

    @Test
    public void normalizeGesture_doubleTap_stays() {
        assertEquals(ClockSecondsConfig.GESTURE_DOUBLE_TAP,
                ClockSecondsConfig.normalizeGesture(ClockSecondsConfig.GESTURE_DOUBLE_TAP));
    }

    @Test
    public void normalizeGesture_legacyLongPress_becomesClick() {
        // 旧版「长按切换」(1) 必须归一为单击 (0)。
        assertEquals(ClockSecondsConfig.GESTURE_CLICK,
                ClockSecondsConfig.normalizeGesture(ClockSecondsConfig.GESTURE_LONG_PRESS_LEGACY));
    }

    @Test
    public void normalizeGesture_invalid_becomesClick() {
        assertEquals(ClockSecondsConfig.GESTURE_CLICK,
                ClockSecondsConfig.normalizeGesture(999));
        assertEquals(ClockSecondsConfig.GESTURE_CLICK,
                ClockSecondsConfig.normalizeGesture(-1));
    }

    @Test
    public void secondsSuffix_padsZero() {
        assertEquals(":00", ClockSecondsConfig.secondsSuffix(0));
        assertEquals(":07", ClockSecondsConfig.secondsSuffix(7));
        assertEquals(":59", ClockSecondsConfig.secondsSuffix(59));
    }

    @Test
    public void secondsSuffix_outOfRange_clamped() {
        assertEquals(":00", ClockSecondsConfig.secondsSuffix(-5));
        assertEquals(":00", ClockSecondsConfig.secondsSuffix(120));
    }

    @Test
    public void hasTwoDigitHour_detects() {
        assertTrue(ClockSecondsConfig.hasTwoDigitHour("13:45"));
        assertTrue(ClockSecondsConfig.hasTwoDigitHour("05:09:30"));
        // 单位数小时（无前导零）不是「两位数字开头」。
        assertFalse(ClockSecondsConfig.hasTwoDigitHour("9:45"));
        assertFalse(ClockSecondsConfig.hasTwoDigitHour(""));
        assertFalse(ClockSecondsConfig.hasTwoDigitHour(null));
        // 开头是字母 / 非数字，不算。
        assertFalse(ClockSecondsConfig.hasTwoDigitHour("AM9:45"));
    }
}
