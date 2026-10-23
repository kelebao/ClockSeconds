package io.github.kelebao.colorosclockseconds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * 虚线分隔线：把原来的直线分隔符画成「一长一短」的虚线，颜色由「波浪线颜色」滑块控制。
 */
public class WavyLineView extends View {
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public WavyLineView(Context context) {
        this(context, null);
    }

    public WavyLineView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WavyLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = getResources().getDisplayMetrics().density;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(Math.max(1f * density, 1.5f));
        // 虚线：一长一短（长划-间隙-短划-间隙）
        mPaint.setPathEffect(new DashPathEffect(
                new float[]{14f * density, 6f * density, 5f * density, 6f * density}, 0f));
        mPaint.setColor(0xFFA98CFF);        // 默认，稍后由 setLineColor 覆盖
    }

    /** 设置虚线颜色（与滑块一致）。 */
    public void setLineColor(int color) {
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final float w = getWidth();
        final float cy = getHeight() / 2f;
        if (w <= 0f) {
            return;
        }
        canvas.drawLine(0f, cy, w, cy, mPaint);
    }
}
