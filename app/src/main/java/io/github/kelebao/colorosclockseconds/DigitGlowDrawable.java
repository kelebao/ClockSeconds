package io.github.kelebao.colorosclockseconds;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * 预览卡里「选中那位数字自己在发光」的一层呼吸光晕。
 *
 * <p><b>为什么不用 BlurMaskFilter：</b>ColorOS 的硬件加速渲染下，View 背景绘制里的
 * {@code BlurMaskFilter} 会被直接忽略——把半径从 0.85 字号改到 3.0 字号，屏幕上光晕一个像素都不变
 * （vc135 真机实测）。所以这里改用 {@code RadialGradient} 做柔光：渐变在硬件加速下必定生效，
 * 而且衰减曲线可以自己控制，比模糊更容易调出参考图那种「从笔画渗出去的一团绿气」。</p>
 *
 * <p>做法是两层同心径向渐变：
 * 外层大范围、低透明度，负责整片雾场；内层贴着字形、透明度略高，把紧贴笔画的那一段撑住。
 * 两层都用 SRC_OVER 叠加，字形本身由上层 TextView 画，所以字保持原本的颜色不会被冲淡。</p>
 */
public class DigitGlowDrawable extends Drawable {

    /** 外层雾半径（相对字号）。 */
    private static final float RADIUS_WIDE = 0.62f;
    /** 内层贴边光半径（相对字号）。 */
    private static final float RADIUS_INNER = 0.32f;

    /** 外层雾峰值 alpha。 */
    private static final float ALPHA_WIDE = 125f;
    /** 内层贴边光峰值 alpha。 */
    private static final float ALPHA_INNER = 175f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String glyph = "";
    private float textSize = 100f;
    private Typeface typeface = Typeface.DEFAULT;
    private int color = 0xFF2196F3;
    /** 呼吸强度 0..1，由 Activity 的动画每帧推进。 */
    private float breath = 1f;

    private Shader wideShader;
    private Shader innerShader;
    private int shaderColor = 0;
    private float shaderFor = -1f;
    private float shaderRadiusFor = -1f;

    public DigitGlowDrawable() {
        paint.setStyle(Paint.Style.FILL);
    }

    public void setColor(int value) {
        if (color != value) {
            color = value;
            shaderColor = 0;
            invalidateSelf();
        }
    }

    /** 告诉它现在要发哪位数字的光。 */
    public void setGlyph(String value, float size, Typeface tf) {
        final String g = value == null ? "" : value;
        final boolean changed = !g.equals(glyph)
                || Math.abs(size - textSize) > 0.5f
                || tf != typeface;
        glyph = g;
        textSize = size;
        typeface = tf;
        if (changed) {
            shaderFor = -1f;
            invalidateSelf();
        }
    }

    public void setBreath(float value) {
        final float v = Math.max(0f, Math.min(1f, value));
        if (Math.abs(v - breath) > 0.01f) {
            breath = v;
            invalidateSelf();
        }
    }

    /** 光晕要向外扩多少像素（调用方据此给 View 留边，否则会被裁掉）。 */
    public static float pad(float textSize) {
        return textSize * RADIUS_WIDE;
    }

    private void ensureShaders(float w, float h) {
        if (wideShader != null && shaderColor == color
                && Math.abs(shaderFor - w) < 0.5f && Math.abs(shaderRadiusFor - h) < 0.5f) {
            return;
        }
        final float rw = Math.min(w, h) / 2f;
        final float ri = rw * (RADIUS_INNER / RADIUS_WIDE);
        wideShader = buildShader(rw);
        innerShader = buildShader(ri);
        shaderColor = color;
        shaderFor = w;
        shaderRadiusFor = h;
    }

    /** 由中心向外连续衰减的径向渐变：中心满强度，边界处精确收到 0，所以不会出现硬边方框。 */
    private Shader buildShader(float radius) {
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = color & 0xFF;
        final int core = Color.argb(255, r, g, b);
        final int mid = Color.argb(150, r, g, b);
        final int far = Color.argb(0, r, g, b);
        return new RadialGradient(0f, 0f, Math.max(1f, radius),
                new int[]{core, mid, far},
                new float[]{0f, 0.45f, 1f},
                Shader.TileMode.CLAMP);
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect bounds = getBounds();
        if (bounds.width() <= 0 || bounds.height() <= 0) {
            return;
        }
        ensureShaders(bounds.width(), bounds.height());
        final float cx = bounds.exactCenterX();
        final float cy = bounds.exactCenterY();

        // 两层同心柔光。每层 alpha 由 shader 自带的渐变给形状、由 breath 给强度：
        // 外层走 breath^1.6（谷底收得干净），内层走 breath^1.2（峰值更亮、跟得更紧）。
        final float wide = (float) Math.pow(breath, 1.6);
        final float inner = (float) Math.pow(breath, 1.2);

        canvas.save();
        canvas.translate(cx, cy);

        paint.setShader(wideShader);
        paint.setAlpha(Math.round(ALPHA_WIDE * wide));
        canvas.drawCircle(0f, 0f, Math.min(bounds.width(), bounds.height()) / 2f, paint);

        paint.setShader(innerShader);
        paint.setAlpha(Math.round(ALPHA_INNER * inner));
        final float rw = Math.min(bounds.width(), bounds.height()) / 2f;
        canvas.drawCircle(0f, 0f, rw * (RADIUS_INNER / RADIUS_WIDE), paint);

        paint.setShader(null);
        canvas.restore();
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
