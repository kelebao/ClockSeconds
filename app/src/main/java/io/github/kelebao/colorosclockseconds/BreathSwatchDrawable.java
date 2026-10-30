package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * 色点：实心圆 + （选中时）外圈一条绕圈旋转的亮弧（「旋转亮弧」方案，vc224 起）。
 *
 * <p>为什么不用「明暗呼吸」：人眼对<b>位置变化</b>的敏感度远高于明暗变化，所以同样的
 * 视觉强度下，一条绕圈转的弧比一团忽明忽暗的散光更容易被看见——这正是「散光呼吸
 * 看不清楚」的解法。</p>
 *
 * <p>画法：一圈很淡的轨道（提示「这里有一整圈」）+ 一段 105° 的亮弧 + 身后 45° 拖尾
 * + 弧头一个小光点。相位由外部 {@link #setPhase(float)} 每 50ms 推进一次，本类不做动画、
 * 不持有定时器。</p>
 */
public class BreathSwatchDrawable extends Drawable {

    /** 亮弧转一圈的周期（毫秒）；外部按 {@code (now % SPIN_PERIOD_MS) / SPIN_PERIOD_MS} 算相位。 */
    public static final long SPIN_PERIOD_MS = 2200L;

    /** 实心圆半径占色块半径的比例（默认值）。 */
    private static final float SOLID = 0.70f;
    /** 轨道半径占色块半径的比例：0.84 留出描边余量，避免弧被 bounds 裁掉。 */
    private static final float RING = 0.84f;
    /** 亮弧张角（度）。 */
    private static final float ARC_SWEEP = 105f;
    /** 亮弧身后的拖尾张角（度）。 */
    private static final float TAIL_SWEEP = 45f;

    private final Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTrackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mTailPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mHeadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRingRect = new RectF();

    private final int mColor;
    private final boolean mSelected;
    private final int mStrokeColor;
    private final float mStrokeWidth;
    private final float mSolidRatio;

    /** 旋转相位 0..1（0 = 弧在 12 点方向）。 */
    private float mPhase;

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
        mTrackPaint.setStyle(Paint.Style.STROKE);
        mTrackPaint.setColor(color);
        mTrackPaint.setAlpha(52);
        mTailPaint.setStyle(Paint.Style.STROKE);
        mTailPaint.setColor(color);
        mTailPaint.setAlpha(88);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setColor(color);
        mHeadPaint.setStyle(Paint.Style.FILL);
        mHeadPaint.setColor(color);
    }

    /** 推进旋转相位（0..1，可传任意实数，内部取小数部分）；仅选中色点会高频调用。 */
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

        // 选中：外圈轨道 + 旋转亮弧（不做明暗呼吸，靠位置变化传达「在动」）
        if (mSelected) {
            final float ringR = r * RING;
            final float sw = Math.max(3f, r * 0.085f);
            mRingRect.set(cx - ringR, cy - ringR, cx + ringR, cy + ringR);
            mTrackPaint.setStrokeWidth(sw);
            canvas.drawCircle(cx, cy, ringR, mTrackPaint);

            // 0 度在 3 点方向、顺时针为正；-90 度起画即从 12 点开始
            final float head = -90f + mPhase * 360f;
            mTailPaint.setStrokeWidth(sw * 1.15f);
            canvas.drawArc(mRingRect, head - TAIL_SWEEP, TAIL_SWEEP, false, mTailPaint);
            mArcPaint.setStrokeWidth(sw * 1.5f);
            canvas.drawArc(mRingRect, head, ARC_SWEEP, false, mArcPaint);

            // 弧头光点：让「头」比「尾」更亮，旋转方向一眼可读
            final double rad = Math.toRadians(head);
            final float hx = cx + (float) (Math.cos(rad) * ringR);
            final float hy = cy + (float) (Math.sin(rad) * ringR);
            canvas.drawCircle(hx, hy, sw * 1.35f, mHeadPaint);
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
        mTrackPaint.setAlpha(Math.round(alpha * 52f / 255f));
        mTailPaint.setAlpha(Math.round(alpha * 88f / 255f));
        mArcPaint.setAlpha(alpha);
        mHeadPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mFillPaint.setColorFilter(colorFilter);
        mStrokePaint.setColorFilter(colorFilter);
        mTrackPaint.setColorFilter(colorFilter);
        mTailPaint.setColorFilter(colorFilter);
        mArcPaint.setColorFilter(colorFilter);
        mHeadPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
