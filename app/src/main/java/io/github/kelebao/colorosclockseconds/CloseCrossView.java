package io.github.kelebao.colorosclockseconds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

/**
 * 预览卡展开态里那个「关闭」圆钮：一个实心圆 + 两道圆头斜线画出来的 X。
 *
 * <p>不用字体里的 ✕ 字符，是因为不同 ROM 的字形差异很大（有的偏小、有的换字体），
 * 自己画能保证任何密度下都是干净的 X。</p>
 */
public class CloseCrossView extends View {

    private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint crossPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path crossPath = new Path();

    public CloseCrossView(Context context, int circleColor, int crossColor) {
        super(context);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(circleColor);
        crossPaint.setStyle(Paint.Style.STROKE);
        crossPaint.setStrokeCap(Paint.Cap.ROUND);
        crossPaint.setStrokeJoin(Paint.Join.ROUND);
        crossPaint.setColor(crossColor);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float cx = getWidth() / 2f;
        final float cy = getHeight() / 2f;
        final float r = Math.min(getWidth(), getHeight()) / 2f;
        canvas.drawCircle(cx, cy, r, circlePaint);

        final float arm = r * 0.34f;
        crossPaint.setStrokeWidth(Math.max(2f, r * 0.13f));
        crossPath.reset();
        crossPath.moveTo(cx - arm, cy - arm);
        crossPath.lineTo(cx + arm, cy + arm);
        crossPath.moveTo(cx + arm, cy - arm);
        crossPath.lineTo(cx - arm, cy + arm);
        canvas.drawPath(crossPath, crossPaint);
    }
}
