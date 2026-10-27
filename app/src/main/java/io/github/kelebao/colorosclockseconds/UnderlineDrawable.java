package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

/**
 * 选中数字下面那根短色线：两端半圆的实心细条，颜色 = 这一位当前的颜色。
 */
public class UnderlineDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int color;

    public UnderlineDrawable(int color) {
        this.color = color;
    }

    public void setColor(int color) {
        if (this.color != color) {
            this.color = color;
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect b = getBounds();
        if (b.isEmpty()) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        final float r = b.height() / 2f;
        canvas.drawRoundRect(b.left, b.top, b.right, b.bottom, r, r, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
