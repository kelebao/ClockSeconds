package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * 预览时间卡下方的「长弧刻度」：∩ 拱形、只有刻度、无弧线轮廓。
 * 呼吸灯算法：发光中心偏在 0.60w（向右铺开），亮度随 6s 余弦平缓起伏、中心 30s 极缓漂移；
 * 刻度颜色沿固定色板平滑插值循环（绿/青已加深，并含 #715C1F / #0F2350），越靠发光中心越亮、两端越淡。
 */
public class ArcTickDrawable extends Drawable {
    private final Paint mMinorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mMajorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final boolean mNight;   // 深色主题用提亮色板（否则深色段在深卡片上看不见）
    private float mRotation;   // 旋转相位（度）
    private float mBreath;     // 呼吸强度 0..1（6s 余弦）
    private float mDrift;      // 漂移相位 0..1（30s）

    public ArcTickDrawable(int color, boolean night) {
        // color 参数保留以兼容调用方；现已改用固定色板，不再使用单色。
        mNight = night;
        for (Paint p : new Paint[]{mMinorPaint, mMajorPaint}) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    /** 旋转相位（度）——长刻度沿拱缓缓游走。 */
    public void setRotation(float deg) {
        mRotation = deg;
        invalidateSelf();
    }

    /** 呼吸强度 0..1（6s 余弦）。 */
    public void setBreath(float breath) {
        mBreath = Math.max(0f, Math.min(1f, breath));
        invalidateSelf();
    }

    /** 漂移相位 0..1（30s，驱动色板流转 + 发光中心极缓漂移）。 */
    public void setDrift(float drift) {
        mDrift = drift;
        invalidateSelf();
    }

    /** 长弧刻度色板：沿拱平滑插值循环。绿/青已加深，并含指定色 #715C1F(深金橄榄)/#0F2350(深海军蓝)；
     *  不含亮橙色。相邻色大致按光谱排序，插值过渡自然。 */
    static final int[] ARC_PALETTE = {
            0xFFE53935,  // 红
            0xFFD81B60,  // 玫红
            0xFF8E24AA,  // 紫
            0xFF1E88E5,  // 蓝
            0xFF0F2350,  // 深海军蓝（指定 #0f2350）
            0xFF00695C,  // 深青（青→深青）
            0xFF1B5E20,  // 深绿（绿→深绿）
            0xFF715C1F,  // 深金橄榄（指定 #715c1f）
    };

    /** 深色主题色板：与浅色同色相但整体提亮——否则深绿/深青/海军蓝/橄榄在深色卡片 #241D38 上完全看不见。 */
    static final int[] ARC_PALETTE_DARK = {
            0xFFFF7169,  // 亮红
            0xFFFF6E9E,  // 亮玫红
            0xFFB794F6,  // 亮紫（贴近深色主题强调色）
            0xFF6FB5FF,  // 亮蓝
            0xFF8E9BFF,  // 亮海军蓝（#0F2350 提亮）
            0xFF4FC3B3,  // 亮青（#00695C 提亮）
            0xFF7CCB82,  // 亮绿（#1B5E20 提亮）
            0xFFD8B96B,  // 亮金橄榄（#715C1F 提亮）
    };

    static int lerpColor(int a, int b, float t) {
        final int ar = (a >> 16) & 0xFF, ag = (a >> 8) & 0xFF, ab = a & 0xFF;
        final int br = (b >> 16) & 0xFF, bg = (b >> 8) & 0xFF, bb = b & 0xFF;
        final int r = Math.round(ar + (br - ar) * t);
        final int g = Math.round(ag + (bg - ag) * t);
        final int bl = Math.round(ab + (bb - ab) * t);
        return 0xFF000000 | (r << 16) | (g << 8) | bl;
    }

    @Override
    public void draw(Canvas canvas) {
        final float w = getBounds().width();
        final float h = getBounds().height();
        if (w <= 1f || h <= 1f) {
            return;
        }
        final float cx = w / 2f;
        final float peakY = h * 0.48f;
        final float depth = h * 0.44f;
        final float hw = w * 0.50f;
        final float R = (hw * hw + depth * depth) / (2f * depth);
        final float cy = peakY + R;
        final float halfDeg = (float) Math.toDegrees(Math.asin(Math.min(1f, hw / R)));

        // 呼吸灯发光中心：x 偏在 0.60w，叠加 30s 极缓左右漂移；y 取拱圆心
        final float glowX = w * 0.60f + w * 0.04f * (float) Math.sin(mDrift * 2 * Math.PI);
        final float cosA = Math.max(-1f, Math.min(1f, (glowX - cx) / R));
        final float glowAng = 270f + (float) Math.toDegrees(Math.asin(cosA));
        final float breath = 0.72f + 0.28f * mBreath;

        final float majorLen = h * 0.09f;
        final float shortLen = majorLen * 0.4f;
        final float innerR = R - majorLen;
        final float stroke = Math.max(1.5f, w * 0.0022f);
        mMinorPaint.setStrokeWidth(stroke);
        mMajorPaint.setStrokeWidth(stroke * 2.5f);

        for (int base = 0; base < 360; base += 3) {
            float a = (base + mRotation) % 360f;
            if (a < 0f) {
                a += 360f;
            }
            if (a < 270f - halfDeg || a > 270f + halfDeg) {
                continue;
            }
            boolean major = (base % 15 == 0);
            double rad = Math.toRadians(a);
            float outer = major ? R : (innerR + shortLen);

            // 距发光角的远近 → 亮度渐隐（越靠中心越亮，两端下限抬高到 70%）
            float angDist = Math.abs(a - glowAng);
            float fade = Math.max(0.70f, 1f - angDist / halfDeg);
            int alpha = Math.round(255f * breath * fade);

            // 沿拱在色板里平滑插值取色，整条随时间流转（深色主题换提亮色板）
            float pos = (a - (270f - halfDeg)) / (2f * halfDeg);   // 0..1 沿弧
            final int[] palette = mNight ? ARC_PALETTE_DARK : ARC_PALETTE;
            float f = pos * palette.length + mDrift * palette.length;
            f = ((f % palette.length) + palette.length) % palette.length;
            int i0 = (int) f;
            int col = lerpColor(palette[i0], palette[(i0 + 1) % palette.length], f - i0);
            col = (col & 0x00FFFFFF) | (alpha << 24);

            Paint paint = major ? mMajorPaint : mMinorPaint;
            paint.setColor(col);
            float c = (float) Math.cos(rad);
            float s = (float) Math.sin(rad);
            canvas.drawLine(cx + innerR * c, cy + innerR * s,
                    cx + outer * c, cy + outer * s, paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mMinorPaint.setAlpha(alpha);
        mMajorPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mMinorPaint.setColorFilter(colorFilter);
        mMajorPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
