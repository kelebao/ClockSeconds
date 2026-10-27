package io.github.kelebao.colorosclockseconds;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * 预览卡里「正在取色的那一位数字」的呼吸光晕。
 *
 * <p>发光的是<b>数字本身</b>：把同一个字形模糊后画在数字底下，光晕顺着笔画走
 * （不是数字背后的一张色板）。分两层：紧贴笔画的高亮「核心」+ 外扩的柔和「光雾」，
 * 叠起来像霓虹灯。没有实心底、也没有描边环，字形尺寸与位置由外部每帧同步
 * （{@link #setGlyph}），所以展开动画里也不会错位。</p>
 *
 * <p>{@link #setBreath(float)} 只调整体透明度，由设置页的 breathTick（50ms）驱动。</p>
 */
public final class DigitGlowDrawable extends Drawable {

    /**
     * 呼吸最暗时仍保留的可见度。霓虹灯管不存在「完全熄灭」的一帧，熄到 0.3 就只剩看不见的残影，
     * 所以波谷也要有明显亮度（0.55）。呼吸靠「亮边」的强弱变化体现，而不是整体明灭。
     */
    private static final float MIN = 0.55f;
    /** 呼吸最亮时整体 alpha 上限。 */
    private static final float PEAK_ALPHA = 1.0f;
    /**
     * 外层光雾的模糊半径（相对字号）。灯管是「一道亮线 + 一圈薄雾」，雾铺开就糊成色块
     * （原来的 0.22 正是用户说的「雾太散」），这里收到 0.16 且提高不透明度。
     */
    private static final float BLUR_WIDE = 0.16f;
    /** 紧贴笔画的核心光：半径小 + 满亮，亮边才锐（0.07 时边缘偏软，像毛玻璃）。 */
    private static final float BLUR_CORE = 0.04f;
    /** 核心光与字形同尺寸；放大只会把亮边推离笔画，反而更糊。 */
    private static final float CORE_SCALE = 1.0f;
    /** 灯管亮边：沿笔画描一圈，宽度相对字号。这一层不模糊 —— 「亮边更锐」全靠它。 */
    private static final float RIM_STROKE = 0.055f;
    /** 亮边颜色朝白色混多少（0=本色，1=纯白）。灯管的亮边永远比管内更白。 */
    private static final float RIM_WHITEN = 0.42f;

    private final Paint widePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect ink = new Rect();
    private final int color;
    private float breath;
    private String glyph = "";
    private float textSize = -1f;
    private float wideRadius = -1f;
    private Typeface typeface;

    public DigitGlowDrawable(int color) {
        this.color = color;
        for (Paint p : new Paint[]{widePaint, corePaint}) {
            p.setStyle(Paint.Style.FILL);
            p.setTextAlign(Paint.Align.CENTER);
            p.setColor(color);
        }
        // 亮边：描边 + 更白一点的本色，不模糊。
        rimPaint.setStyle(Paint.Style.STROKE);
        rimPaint.setTextAlign(Paint.Align.CENTER);
        rimPaint.setStrokeJoin(Paint.Join.ROUND);
        rimPaint.setColor(whiten(color, RIM_WHITEN));
    }

    /** 朝白色混一点（霓虹灯管的亮边比管内更白）。 */
    private static int whiten(int color, float amount) {
        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = color & 0xFF;
        final int nr = Math.round(r + (255 - r) * amount);
        final int ng = Math.round(g + (255 - g) * amount);
        final int nb = Math.round(b + (255 - b) * amount);
        return (color & 0xFF000000) | (nr << 16) | (ng << 8) | nb;
    }

    /** 同步要发光的字形、字号与字体（每帧都可调用，只有变化时才重建模糊滤镜）。 */
    public void setGlyph(String glyph, float textSize, Typeface typeface) {
        final String g = glyph == null ? "" : glyph;
        final boolean sizeChanged = Math.abs(this.textSize - textSize) > 0.5f;
        if (g.equals(this.glyph) && !sizeChanged && this.typeface == typeface) {
            return;
        }
        this.glyph = g;
        this.textSize = textSize;
        this.typeface = typeface;
        if (sizeChanged || wideRadius <= 0f) {
            wideRadius = Math.max(1f, textSize * BLUR_WIDE);
            rimPaint.setStrokeWidth(Math.max(1f, textSize * RIM_STROKE));
            widePaint.setMaskFilter(new BlurMaskFilter(wideRadius, BlurMaskFilter.Blur.NORMAL));
            corePaint.setMaskFilter(
                    new BlurMaskFilter(Math.max(1f, textSize * BLUR_CORE), BlurMaskFilter.Blur.NORMAL));
        }
        invalidateSelf();
    }

    /** 模糊外扩范围（px）：外部据此把光晕 View 的边界留够。 */
    public static float blurExtent(float textSize) {
        return Math.max(1f, textSize * BLUR_WIDE) * 2.6f + textSize * RIM_STROKE;
    }

    /** 0 = 最暗（仍是 MIN 的可见度），1 = 最亮。 */
    public void setBreath(float value) {
        final float v = value < 0f ? 0f : (value > 1f ? 1f : value);
        if (Math.abs(v - breath) < 0.01f) {
            return;   // 变化太小就不重绘（50ms 一次，省 CPU）
        }
        breath = v;
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect b = getBounds();
        if (b.width() <= 0 || b.height() <= 0 || glyph.length() == 0 || textSize <= 0f) {
            return;
        }
        final float level = MIN + (1f - MIN) * breath;
        final float cx = b.exactCenterX();
        // 让「字形墨迹中心」落在 View 中心，与上层真实数字的笔画重合。

        // 三层叠出灯管感：外雾 → 亮边（锐）→ 芯光（满亮）。
        // 三层的字形尺寸与基线量法必须完全一致，否则亮边会偏离真实笔画。
        widePaint.setTextSize(textSize * CORE_SCALE);
        rimPaint.setTextSize(textSize * CORE_SCALE);
        corePaint.setTextSize(textSize * CORE_SCALE);
        if (typeface != null) {
            widePaint.setTypeface(typeface);
            rimPaint.setTypeface(typeface);
            corePaint.setTypeface(typeface);
        }
        widePaint.getTextBounds(glyph, 0, glyph.length(), ink);
        final float baseline = b.exactCenterY() - (ink.top + ink.bottom) / 2f;

        // ① 外层光雾
        widePaint.setAlpha(Math.round(255f * PEAK_ALPHA * 0.85f * level));
        canvas.drawText(glyph, cx, baseline, widePaint);

        // ② 亮边：沿笔画描一圈且不模糊 —— 「亮边更锐」就是这一层
        rimPaint.setAlpha(Math.round(255f * PEAK_ALPHA * 1.0f * level));
        canvas.drawText(glyph, cx, baseline, rimPaint);

        // ③ 芯光：紧贴笔画、满亮，把字心点亮
        corePaint.setAlpha(Math.round(255f * PEAK_ALPHA * 1.0f * level));
        canvas.drawText(glyph, cx, baseline, corePaint);
    }

    @Override
    public void setAlpha(int alpha) {
        // 透明度由 setBreath 控制，这里不单独维护。
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        widePaint.setColorFilter(cf);
        corePaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
