package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * 右下角三条同心半弧装饰（参考支付卡片右下角的圆弧）：以背板右下角为圆心，
 * 由内到外三条四分之一弧，颜色跟随「波浪线颜色」滑块。
 */
public class CornerArcsDrawable extends Drawable {
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mArc = new RectF();
    /** 三条弧半径（相对背板较大边）。 */
    private static final float[] RADII = {0.20f, 0.32f, 0.44f};

    public CornerArcsDrawable(int color, float strokeWidth) {
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setColor(color);
    }

    /** 设置弧线颜色。 */
    public void setLineColor(int color) {
        mPaint.setColor(color);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        float w = b.width();
        float h = b.height();
        if (w <= 0f || h <= 0f) {
            return;
        }
        float cx = w;   // 右下角
        float cy = h;
        float base = Math.max(w, h);
        for (float f : RADII) {
            float r = base * f;
            mArc.set(cx - r, cy - r, cx + r, cy + r);
            // 180°→270°：背板内的左上四分之一弧，朝左上张开
            canvas.drawArc(mArc, 180f, 90f, false, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(android.graphics.ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return android.graphics.PixelFormat.TRANSLUCENT;
    }
}
