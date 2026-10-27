package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * 预览卡里「从顶端正中滴下来、滴在选中数字上就消失」的那滴水。
 *
 * <p>只负责画一帧：位置／半径／水花进度全由 Activity 的动画每帧推进，所以这里
 * 没有任何时间逻辑，也不持有 View 引用。水滴本身用「下半圆 + 上方尖角」拼一个
 * 泪滴形，再叠一点高光；落地后半径收起、同时扩散一圈很快变淡的水花环。</p>
 */
public class PreviewDropDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private int color = 0xFF2196F3;
    private float cx, cy, radius;
    /** 水花扩散进度 0..1；< 0 表示当前没有水花。 */
    private float splash = -1f;

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidateSelf();
        }
    }

    /** 水滴本体：圆心 (cx, cy)、半径 r；r <= 0 表示不画。 */
    public void setDrop(float cx, float cy, float r) {
        this.cx = cx;
        this.cy = cy;
        this.radius = r;
        invalidateSelf();
    }

    public void setSplash(float progress) {
        this.splash = progress;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (radius <= 0f) {
            return;
        }
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = color & 0xFF;

        // 不再画外圈柔光：水滴从预览区上方的留白里往下落，柔光一旦超出 View 上边界就会被
        // 父容器裁成一条硬直角（真机实测是水滴周围一块淡色方块），而水滴本身的对撞色已经
        // 足够醒目，去掉更干净。

        // 泪滴：下半是圆，上半收成尖角（水往下掉，尖端朝上）。
        path.reset();
        path.addCircle(cx, cy, radius, Path.Direction.CW);
        path.moveTo(cx, cy - radius * 1.75f);
        path.lineTo(cx - radius * 0.85f, cy - radius * 0.30f);
        path.lineTo(cx + radius * 0.85f, cy - radius * 0.30f);
        path.close();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(232, r, g, b));
        canvas.drawPath(path, paint);

        // 左上一小片高光，让水滴看起来是透明的而不是一个实心色点。
        paint.setColor(Color.argb(130, 255, 255, 255));
        canvas.drawCircle(cx - radius * 0.32f, cy - radius * 0.26f, radius * 0.32f, paint);

        // 落点水花：一圈细环向外扩散并淡出。
        if (splash >= 0f && splash <= 1f) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, radius * 0.20f));
            paint.setColor(Color.argb(Math.round(180f * (1f - splash)), r, g, b));
            canvas.drawCircle(cx, cy, radius * (0.9f + splash * 2.4f), paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
