package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * 卡片底板：上半部分正常圆角，**下沿整条边做倾斜**（左低右高），像卡片被从右边按下去一样。
 *
 * <p>倾斜量用「右端相对左端抬高多少 px」表示，可以动画（0 = 普通圆角矩形 = 收起态）。
 * 下沿与右沿、左沿的交角都用圆角过渡，斜边本身是一条直线。</p>
 *
 * <p>为什么不直接把整张卡做 3D 旋转：卡片内容（数字、圆钮、刻度弧）会跟着透视压扁，
 * 而且父容器裁剪会让底部内容整块消失，实测不可用（vc64）。改轮廓形状内容一点不动。</p>
 */
public class SlantCardDrawable extends Drawable {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF tmp = new RectF();

    private int color = 0xFFFFFFFF;
    private float radius = 0f;
    /** 下沿右端相对左端抬高的高度（px）。0 = 不倾斜。 */
    private float rise = 0f;

    public SlantCardDrawable(int color, float radius) {
        this.color = color;
        this.radius = radius;
        paint.setStyle(Paint.Style.FILL);
    }

    public void setColor(int value) {
        if (color != value) {
            color = value;
            invalidateSelf();
        }
    }

    /** 设置下沿右端的抬高量（px）。 */
    public void setRise(float value) {
        if (Math.abs(rise - value) > 0.5f) {
            rise = value;
            invalidateSelf();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        final RectF b = tmp;
        b.set(getBounds());
        final float w = b.width();
        final float h = b.height();
        if (w <= 0f || h <= 0f) {
            return;
        }
        final float r = Math.min(radius, Math.min(w, h) / 2f);
        final float dy = Math.max(0f, Math.min(rise, h * 0.35f));
        // 斜边的方向向量长度归一化，用来把两个下角沿斜边方向「退让 r」再倒圆角。
        final float len = (float) Math.hypot(w, dy);
        final float cos = w / len;
        final float sin = dy / len;

        // 两个下角的顶点：右端在 (right, bottom - dy)，左端在 (left, bottom)。
        final float rx = b.right;
        final float ry = b.bottom - dy;
        final float lx = b.left;
        final float ly = b.bottom;

        path.reset();
        path.moveTo(b.left + r, b.top);
        path.lineTo(b.right - r, b.top);
        path.quadTo(b.right, b.top, b.right, b.top + r);
        // 右沿往下到圆角起点，圆角内侧落到斜边上
        path.lineTo(rx, ry - r);
        path.quadTo(rx, ry, rx - r * cos, ry + r * sin);
        // 斜边本身
        path.lineTo(lx + r * cos, ly - r * sin);
        path.quadTo(lx, ly, lx, ly - r);
        path.lineTo(b.left, b.top + r);
        path.quadTo(b.left, b.top, b.left + r, b.top);
        path.close();

        paint.setColor(color);
        canvas.drawPath(path, paint);
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
