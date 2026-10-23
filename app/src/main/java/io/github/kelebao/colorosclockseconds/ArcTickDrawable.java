package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * 预览时间卡下方的「长弧刻度」：∩ 拱形、只有刻度、无弧线轮廓。
 * 呼吸灯算法：发光中心偏在 0.60w（向右铺开），亮度随 6s 余弦平缓起伏、中心 30s 极缓漂移；
 * 刻度色相沿弧循环（随时间整体流转），越靠发光中心越亮、越靠两端越淡（外圈透明透出底色）。
 */
public class ArcTickDrawable extends Drawable {
    private final Paint mMinorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mMajorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mColor;
    private float mRotation;   // 旋转角（度）
    private float mBreath;     // 呼吸强度 0..1（6s 余弦）
    private float mDrift;      // 漂移相位 0..1（30s）

    public ArcTickDrawable(int color) {
        mColor = color;
        for (Paint p : new Paint[]{mMinorPaint, mMajorPaint}) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(color);
        }
    }

    /** 旋转角（度）。 */
    public void setRotation(float deg) {
        mRotation = deg;
        invalidateSelf();
    }

    /** 呼吸强度 0..1（6s 余弦）。 */
    public void setBreath(float breath) {
        mBreath = Math.max(0f, Math.min(1f, breath));
        invalidateSelf();
    }

    /** 漂移相位 0..1（30s 一圈，驱动色相循环 + 中心极缓漂移）。 */
    public void setDrift(float drift) {
        mDrift = drift;
        invalidateSelf();
    }

    // 橙色色相带（约 18°~48°），循环变色时整体跳过，避免出现橙色。
    private static final float ORANGE_LO = 18f;
    private static final float ORANGE_HI = 48f;
    private static final float ORANGE_W = ORANGE_HI - ORANGE_LO;       // 30°
    private static final float USABLE_W = 360f - ORANGE_W;             // 330°

    /** 把任意色相映射到「避开橙色带」的可用色弧：起点设在 ORANGE_HI（黄侧），
     *  环绕一圈到 ORANGE_LO（红侧），中间整段橙色被跳过。 */
    private static float skipOrange(float hue) {
        hue = (hue % 360f + 360f) % 360f;
        float h = hue * USABLE_W / 360f;   // 0..330
        h = (h + ORANGE_HI) % 360f;        // 平移到 48° 起，环绕后落在 0..18（红侧）
        return h;
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
        final float baseHue = (mDrift * 360f) % 360f;   // 色相随时间整体流转（30s 一圈）
        final float breath = 0.72f + 0.28f * mBreath;   // 谷值 0.72 × 渐隐下限 0.70 ≈ 最淡处 50%

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

            // 沿弧色相渐变 + 随时间整体流转 → 循环变色（避开橙色带；饱和度提高更醒目）
            float pos = (a - (270f - halfDeg)) / (2f * halfDeg);   // 0..1 沿弧
            float hue = (baseHue + pos * 130f) % 360f;
            hue = skipOrange(hue);
            int col = Color.HSVToColor(new float[]{hue, 0.58f, 1f});
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
