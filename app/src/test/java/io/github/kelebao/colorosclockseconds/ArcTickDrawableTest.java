package io.github.kelebao.colorosclockseconds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 长弧刻度「色板 + 取色插值」的纯函数单元测试（不依赖 Android 运行时，JVM 直接跑）。
 * 覆盖插值端点/中点/线性，以及色板约定（不透明、含指定色、不含亮橙色）。
 */
public class ArcTickDrawableTest {

    @Test
    public void lerp_endpoints_exact() {
        final int a = 0xFFE53935;
        final int b = 0xFF00695C;
        assertEquals(a, ArcTickDrawable.lerpColor(a, b, 0f));
        assertEquals(b, ArcTickDrawable.lerpColor(a, b, 1f));
    }

    @Test
    public void lerp_midpoint_and_opaque() {
        final int mid = ArcTickDrawable.lerpColor(0xFF000000, 0xFFFFFFFF, 0.5f);
        assertEquals(128, (mid >> 16) & 0xFF);
        assertEquals(128, (mid >> 8) & 0xFF);
        assertEquals(128, mid & 0xFF);
        assertEquals(0xFF, (mid >>> 24) & 0xFF);   // 输出恒为不透明
    }

    @Test
    public void lerp_singleChannel_linear() {
        // 只动 R：0→255，t=0.25 应≈64，G/B 保持 0
        final int c = ArcTickDrawable.lerpColor(0xFF000000, 0xFFFF0000, 0.25f);
        assertEquals(64, (c >> 16) & 0xFF);
        assertEquals(0, (c >> 8) & 0xFF);
        assertEquals(0, c & 0xFF);
    }

    @Test
    public void palette_opaque_and_hasRequestedColors() {
        boolean darkGreen = false, darkCyan = false, navy = false, olive = false;
        for (int c : ArcTickDrawable.ARC_PALETTE) {
            assertEquals("色板项必须不透明", 0xFF, (c >>> 24) & 0xFF);
            if (c == 0xFF1B5E20) darkGreen = true;   // 深绿
            if (c == 0xFF00695C) darkCyan = true;    // 深青
            if (c == 0xFF0F2350) navy = true;        // 深海军蓝（指定 #0f2350）
            if (c == 0xFF715C1F) olive = true;       // 深金橄榄（指定 #715c1f）
        }
        assertTrue("缺少深绿", darkGreen);
        assertTrue("缺少深青", darkCyan);
        assertTrue("缺少 #0F2350", navy);
        assertTrue("缺少 #715C1F", olive);
    }

    @Test
    public void palette_hasNoBrightOrange() {
        // 约定：色板不含「亮橙色」（色相 18°~48° 且饱和>0.6 且亮度>0.7）。
        // 注：#715C1F 色相约 45° 但亮度仅 0.44（暗橄榄），不算亮橙。
        for (int c : ArcTickDrawable.ARC_PALETTE) {
            final float[] hsv = hsvOf(c);
            final boolean brightOrange = hsv[0] >= 18f && hsv[0] <= 48f
                    && hsv[1] > 0.6f && hsv[2] > 0.7f;
            assertTrue("色板含亮橙色: #" + Integer.toHexString(c), !brightOrange);
        }
    }

    @Test
    public void darkPalette_opaque_and_brighterThanLight() {
        assertEquals(ArcTickDrawable.ARC_PALETTE.length, ArcTickDrawable.ARC_PALETTE_DARK.length);
        for (int i = 0; i < ArcTickDrawable.ARC_PALETTE_DARK.length; i++) {
            final int d = ArcTickDrawable.ARC_PALETTE_DARK[i];
            assertEquals("深色板项必须不透明", 0xFF, (d >>> 24) & 0xFF);
            // 深色板每一项都应比浅色对应项更亮，否则深色卡片上看不见
            assertTrue("深色板第 " + i + " 项未提亮",
                    hsvOf(d)[2] > hsvOf(ArcTickDrawable.ARC_PALETTE[i])[2]);
        }
    }

    /** 纯 Java 版 RGB→HSV（避免单测里调 android.graphics.Color 的 stub）。 */
    private static float[] hsvOf(int c) {
        final int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, b = c & 0xFF;
        final int max = Math.max(r, Math.max(g, b));
        final int min = Math.min(r, Math.min(g, b));
        final float v = max / 255f;
        final float s = max == 0 ? 0f : (max - min) / (float) max;
        float h = 0f;
        final float d = max - min;
        if (d != 0) {
            if (max == r) {
                h = ((g - b) / d) % 6f;
            } else if (max == g) {
                h = (b - r) / d + 2f;
            } else {
                h = (r - g) / d + 4f;
            }
            h *= 60f;
            if (h < 0f) {
                h += 360f;
            }
        }
        return new float[]{h, s, v};
    }
}
