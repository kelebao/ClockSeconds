package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * 色点：实心圆 + （选中时）向外扩散的双涟漪（「双涟漪」方案，vc227 起）。
 *
 * <p>为什么不靠明暗呼吸：人眼对<b>位置变化</b>和<b>扩散方向</b>的敏感度远高于明暗变化，
 * 所以两圈从色点边缘向外扩散、边扩边淡，比一团忽明忽暗的散光更容易被看见。</p>
 *
 * <p>画法：两圈同心圆环，相位错开半圈（0.5），半径从实心圆边缘线性扩到色块边缘，
 * 透明度按 {@code (1-k)^1.2} 淡出，粗细按色块半径等比缩放。相位由外部
 * {@link #setPhase(float)} 每 50ms 推进一次，本类不做动画、不持有定时器。</p>
 */
public class BreathSwatchDrawable extends Drawable {

    /** 一圈涟漪扩散到消失的周期（毫秒）；外部按 {@code (now % RIPPLE_PERIOD_MS) / RIPPLE_PERIOD_MS} 算相位。 */
    public static final long RIPPLE_PERIOD_MS = 1600L;

    /** 实心圆半径占色块半径的比例（默认值）。 */
    private static final float SOLID = 0.70f;
    /** 涟漪扩散到的最大半径占色块半径的比例：留 2% 余量，避免被 bounds 裁掉。 */
    private static final float RIPPLE_MAX = 0.98f;
    /** 两圈涟漪的相位间隔（半圈）。 */
    private static final float RIPPLE_PHASE_GAP = 0.5f;
    /** 涟漪峰值透明度（0..255）。 */
    private static final float RIPPLE_ALPHA = 255f;
    /** 淡出曲线指数：0.7 让环在扩散中段仍然清楚（1.2 会衰减太快，肉眼只剩一闪）。 */
    private static final float RIPPLE_FADE_POW = 0.7f;

    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mRipplePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final int mColor;
    private final boolean mSelected;
    private final int mStrokeColor;
    private final float mStrokeWidth;
    private final float mSolidRatio;

    /** 涟漪相位 0..1；0 与 0.5 分别是两圈刚从色点边缘出发的时刻。 */
    private float mPhase;
    /** Drawable 整体透明度（View 状态变化时会调 setAlpha），涟漪透明度按它等比缩放。 */
    private int mAlpha = 255;

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
        mRipplePaint.setStyle(Paint.Style.STROKE);
        mRipplePaint.setColor(color);
    }

    /** 推进涟漪相位（0..1，可传任意实数，内部取小数部分）；仅选中色点会高频调用。 */
    public void setPhase(float phase) {
        mPhase = phase - (float) Math.floor(phase);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect b = getBounds();
        final float cx = b.exactCenterX();
        final float cy = b.exactCenterY();
        final float r = b.width() / 2f;
        final float solid = r * mSolidRatio;

        // 选中：两圈向外扩散的涟漪（靠位置/扩散方向传达「在动」，不靠明暗）
        if (mSelected) {
            final float outer = r * RIPPLE_MAX;
            final float start = solid * 1.02f;
            mRipplePaint.setStrokeWidth(Math.max(2.5f, r * 0.095f));
            for (int i = 0; i < 2; i++) {
                float k = mPhase + (i == 0 ? 0f : RIPPLE_PHASE_GAP);
                k -= (float) Math.floor(k);
                final float radius = start + (outer - start) * k;
                final int a = Math.round(RIPPLE_ALPHA
                        * (float) Math.pow(1f - k, RIPPLE_FADE_POW) * (mAlpha / 255f));
                if (a <= 0) {
                    continue;
                }
                mRipplePaint.setAlpha(Math.min(255, a));
                canvas.drawCircle(cx, cy, radius, mRipplePaint);
            }
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
        mAlpha = alpha;
        mFillPaint.setAlpha(alpha);
        mStrokePaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mFillPaint.setColorFilter(colorFilter);
        mStrokePaint.setColorFilter(colorFilter);
        mRipplePaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
