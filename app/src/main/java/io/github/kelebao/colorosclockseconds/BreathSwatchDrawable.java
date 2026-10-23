package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * 小时颜色色块：实心圆 + （选中时）一圈随呼吸明灭的荧光光晕，光晕颜色 = 色块本身颜色。
 */
public class BreathSwatchDrawable extends Drawable {
    private final Paint mHaloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int mColor;
    private final boolean mSelected;
    private final int mStrokeColor;
    private final float mStrokeWidth;
    private float mIntensity;

    /** 实心圆半径占色块半径的比例（其余留给荧光光晕）。 */
    private static final float SOLID = 0.70f;

    public BreathSwatchDrawable(int color, boolean selected, int strokeColor, float strokeWidth) {
        mColor = color;
        mSelected = selected;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mFillPaint.setStyle(Paint.Style.FILL);
        mFillPaint.setColor(color);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(strokeWidth);
        mStrokePaint.setColor(strokeColor);
        mHaloPaint.setStyle(Paint.Style.FILL);
    }

    /** 设置呼吸强度（0..1）；仅选中色块会高频调用。 */
    public void setBreath(float intensity) {
        mIntensity = Math.max(0f, Math.min(1f, intensity));
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        float cx = b.exactCenterX();
        float cy = b.exactCenterY();
        float r = b.width() / 2f;
        float solid = r * SOLID;

        // 荧光光晕（选中才画，随呼吸明灭）
        if (mSelected && mIntensity > 0.01f) {
            int rgb = mColor & 0x00FFFFFF;
            int a = Math.round(0xC0 * mIntensity);
            mHaloPaint.setShader(new RadialGradient(cx, cy, r,
                    new int[]{0x00000000, rgb | (a << 24), 0x00000000},
                    new float[]{0f, 0.86f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawCircle(cx, cy, r, mHaloPaint);
        }

        // 实心圆
        canvas.drawCircle(cx, cy, solid, mFillPaint);

        // 选中/未选中描边（沿实心圆边缘）
        if (mStrokeWidth > 0f) {
            canvas.drawCircle(cx, cy, solid, mStrokePaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mFillPaint.setAlpha(alpha);
        mStrokePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mFillPaint.setColorFilter(colorFilter);
        mStrokePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
