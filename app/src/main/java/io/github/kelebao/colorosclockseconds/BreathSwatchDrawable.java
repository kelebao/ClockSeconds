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
 *
 * <p>光晕是「散光」而不是「光环」：用径向渐变从圆心向外连续衰减，呼吸时同时改变
 * <b>扩散半径</b>与<b>亮度</b>——亮时向外铺开、暗时收拢变淡，看不出明确的环状边界。</p>
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
    private RadialGradient mHaloShader;   // 缓存：只在边界变化时重建
    private final android.graphics.Matrix mHaloMatrix = new android.graphics.Matrix();
    private int mShaderW = -1, mShaderH = -1;

    /** 实心圆半径占色块半径的比例（其余留给呼吸散光）。 */
    private static final float SOLID = 0.70f;

    /** 实心圆占整个色块的比例：散光需要更多外圈空间，容器放大后靠它把实心圆拉回原尺寸。 */
    private final float mSolidRatio;

    public BreathSwatchDrawable(int color, boolean selected, int strokeColor, float strokeWidth) {
        this(color, selected, strokeColor, strokeWidth, SOLID);
    }

    public BreathSwatchDrawable(int color, boolean selected, int strokeColor, float strokeWidth,
                                float solidRatio) {
        mColor = color;
        mSelected = selected;
        mStrokeColor = strokeColor;
        mStrokeWidth = strokeWidth;
        mSolidRatio = solidRatio > 0f ? solidRatio : SOLID;
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
        float solid = r * mSolidRatio;

        // 呼吸散光（选中才画）：径向渐变从圆心向外连续衰减，没有环状边界。
        if (mSelected && mIntensity > 0.01f) {
            final int bw = b.width(), bh = b.height();
            // 缓存 shader：仅边界变化时重建；亮度用 paint alpha、扩散半径用 local matrix，
            // 避免每帧 new RadialGradient。
            if (mHaloShader == null || mShaderW != bw || mShaderH != bh) {
                final int rgb = mColor & 0x00FFFFFF;
                mHaloShader = new RadialGradient(cx, cy, r,
                        new int[]{rgb | 0xFF000000, rgb | 0x59000000, rgb | 0x00000000},
                        new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP);
                mShaderW = bw;
                mShaderH = bh;
            }
            // 呼吸：亮时向外铺开（半径 ×1.0），暗时收拢（半径 ×0.58），亮度按平方衰减让谷底更暗。
            final float k = 0.58f + 0.42f * mIntensity;
            mHaloMatrix.setScale(k, k, cx, cy);
            mHaloShader.setLocalMatrix(mHaloMatrix);
            mHaloPaint.setShader(mHaloShader);
            mHaloPaint.setAlpha(Math.round(255f * mIntensity * mIntensity));
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
