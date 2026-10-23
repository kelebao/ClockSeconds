package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * 背景板 drawable：先铺一层圆角底色，再叠一枚可平滑呼吸的径向光晕（呼吸灯效果）。
 *
 * <p>不直接依赖 {@code Drawable.setShader}（该 API 在部分 Xposed 编译桩里不存在），
 * 而是用 {@link Paint} 承载 {@link RadialGradient} 手动绘制，保证全 API 可用。</p>
 *
 * <p>光晕作为覆盖层：中心亮、边缘透明 → 透出底层圆角底色，整体呈「呼吸灯」质感。</p>
 */
public class BreathPlateDrawable extends Drawable {
    private final Paint mBasePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mGlowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int mBaseColor;
    private float mCornerRadius;
    private final Path mRoundPath = new Path();
    private final RectF mRect = new RectF();

    public BreathPlateDrawable(int baseColor, float cornerRadius) {
        mBaseColor = baseColor;
        mCornerRadius = cornerRadius;
        mBasePaint.setColor(baseColor);
        mBasePaint.setStyle(Paint.Style.FILL);
        mGlowPaint.setStyle(Paint.Style.FILL);
    }

    /** 更新底色（如明/暗主题切换）。 */
    public void setBaseColor(int baseColor) {
        mBaseColor = baseColor;
        mBasePaint.setColor(baseColor);
        invalidateSelf();
    }

    /** 更新圆角半径（px）。 */
    public void setCornerRadius(float cornerRadius) {
        mCornerRadius = cornerRadius;
        rebuildPath();
        invalidateSelf();
    }

    /** 设置呼吸光晕着色器；传 {@code null} 关闭光晕（仅显示底色）。 */
    public void setGlow(RadialGradient glow) {
        mGlowPaint.setShader(glow);
        invalidateSelf();
    }

    private void rebuildPath() {
        mRoundPath.reset();
        mRoundPath.addRoundRect(mRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRect.set(bounds);
        rebuildPath();
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.save();
        canvas.clipPath(mRoundPath);
        // 圆角底色
        canvas.drawColor(mBaseColor);
        // 呼吸光晕覆盖层（边缘透明 → 透出底色）
        if (mGlowPaint.getShader() != null) {
            canvas.drawRect(mRect, mGlowPaint);
        }
        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) {
        mBasePaint.setAlpha(alpha);
        mGlowPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mBasePaint.setColorFilter(colorFilter);
        mGlowPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
