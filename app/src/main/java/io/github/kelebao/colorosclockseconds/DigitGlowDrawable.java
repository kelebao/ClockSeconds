package io.github.kelebao.colorosclockseconds;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * 预览卡里「选中那位数字自己在发光」的一层呼吸光晕。
 *
 * <p>不是往后面垫一块色板，而是把同一个字形模糊两遍画在数字底下：外层是大半径的低透明度
 * 光雾，内层是小半径的高透明度芯光。数字本身由 TextView 画在上面，于是看起来只有笔画在
 * 发亮，不会出现一块方形色斑（真机上垫色板会被父容器裁出硬边）。</p>
 */
public class DigitGlowDrawable extends Drawable {

    /** 外雾半径（相对字号）：最外圈的大范围柔光，负责「整块区域在发亮」。 */
    private static final float BLUR_WIDE = 0.60f;
    /** 中层光（相对字号）：贴着笔画外扩的一圈，是光晕的主体。 */
    private static final float BLUR_MID = 0.26f;
    /** 芯光半径（相对字号）：紧贴笔画边缘的亮边，让「发光源是字形」看得出来。 */
    private static final float BLUR_CORE = 0.09f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private String glyph = "";
    private float textSize = 100f;
    private Typeface typeface = Typeface.DEFAULT;
    private int color = 0xFF2196F3;
    /** 呼吸强度 0..1，由 Activity 的动画每帧推进。 */
    private float breath = 1f;

    private BlurMaskFilter wideFilter;
    private BlurMaskFilter midFilter;
    private BlurMaskFilter coreFilter;
    private float filtersFor = -1f;

    public DigitGlowDrawable() {
        // 用左对齐 + 墨迹框自己算偏移：光晕必须和真实字形严丝合缝地重叠，
        // 只按「行盒」居中会让模糊出来的字整体偏下半格，看起来像重影。
        paint.setTextAlign(Paint.Align.LEFT);
    }

    public void setColor(int value) {
        if (color != value) {
            color = value;
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
            filtersFor = -1f;
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
        // 外雾半径 0.60 倍字号，模糊会向两侧各扩约 1.5 倍半径，留 0.92 倍字号才不被裁。
        return textSize * 0.92f;
    }

    @Override
    public void draw(Canvas canvas) {
        if (glyph.length() == 0 || textSize <= 0f) {
            return;
        }
        if (filtersFor != textSize) {
            wideFilter = new BlurMaskFilter(textSize * BLUR_WIDE, BlurMaskFilter.Blur.NORMAL);
            midFilter = new BlurMaskFilter(textSize * BLUR_MID, BlurMaskFilter.Blur.NORMAL);
            coreFilter = new BlurMaskFilter(textSize * BLUR_CORE, BlurMaskFilter.Blur.NORMAL);
            filtersFor = textSize;
        }
        paint.setTypeface(typeface);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.LEFT);
        final Rect ink = new Rect();
        paint.getTextBounds(glyph, 0, glyph.length(), ink);
        final float cx = getBounds().width() / 2f;
        final float cy = getBounds().height() / 2f;
        // 让「墨迹框中心」落在 View 中心，与 Activity 里用墨迹框摆位保持一致。
        final float penX = cx - (ink.left + ink.right) / 2f;
        final float baseline = cy - (ink.top + ink.bottom) / 2f;

        final int r = (color >> 16) & 0xFF;
        final int g = (color >> 8) & 0xFF;
        final int b = color & 0xFF;

        // 由外向内三层叠加：每往里一层半径减半、亮度递增，叠出来的效果就是
        // 「光从笔画里渗出来、越贴近字形越亮」，而不是一块均匀的雾。
        // alpha 全部乘 breath，呼吸时整团光一起起落。
        //
        // 呼吸形状用两个曲线分段合成（用户要求「最亮部分拉到最亮」）：
        //   - 光晕部分走 breath^1.7：峰顶几乎不变、谷底压得更低，明暗落差才看得出来；
        //   - 芯光与笔画提亮走 breath^0.55：峰顶比线性更早顶到 255，最亮那一档停留更久，
        //     观感是「吸到顶时"啪"地亮透」，而不是缓慢爬升到九成就回落。
        final float curve = (float) Math.pow(breath, 1.7);
        final float hot = (float) Math.pow(breath, 0.45);
        // 笔画提亮层不能全程都压白，否则谷底时字的下半截会糊成实心白块（真机踩过）。
        // 让它只在接近峰顶时才真正上量：hot^3 在谷底约 0.44、峰顶 1.0。
        final float flash = hot * hot * hot;

        // 外雾：光晕要从笔画一直渗到半个字号以外，才有「发光」的体积感。
        paint.setMaskFilter(wideFilter);
        paint.setColor(Color.argb(Math.round(86f * curve), r, g, b));
        canvas.drawText(glyph, penX, baseline, paint);

        paint.setMaskFilter(midFilter);
        paint.setColor(Color.argb(Math.round(132f * curve), r, g, b));
        canvas.drawText(glyph, penX, baseline, paint);

        // 亮边：这一层决定「光的边界有多实」，峰值直接给满 255。
        paint.setMaskFilter(coreFilter);
        paint.setColor(Color.argb(Math.min(255, Math.round(255f * hot)), r, g, b));
        canvas.drawText(glyph, penX, baseline, paint);

        // 最内层：笔画自身的提亮（白，不加色相）。峰顶直接给满 255 纯白 ——
        // 用户要的「最亮部分拉到最亮」就落在这层：吸到顶时笔画中心是真的白。
        // 谷底靠 flash=hot^3 把这一层几乎收干净，字形保持自己的颜色不被糊掉。
        paint.setMaskFilter(null);
        paint.setColor(Color.argb(Math.min(255, Math.round(255f * flash)), 255, 255, 255));
        canvas.drawText(glyph, penX, baseline, paint);
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
