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

    /** 外雾半径（相对字号）。 */
    private static final float BLUR_WIDE = 0.30f;
    /** 中层光（相对字号）：贴着笔画的一圈亮色，负责让「在发光」这件事一眼能看出来。 */
    private static final float BLUR_MID = 0.15f;
    /** 芯光半径（相对字号）。 */
    private static final float BLUR_CORE = 0.08f;

    /**
     * 浅色主题下的一整套参数。
     *
     * <p>白底上「加亮」这条路走不通 —— 背景已经是最高亮度，再怎么把光晕提亮也只是
     * 把白冲成白，真机看就是「光晕没了」。浅色下要换个读法：不是数字往外发光，
     * 而是数字的本色往外晕开一圈（像墨滴渗进纸里）。所以半径放大、颜色改用
     * 数字本色（深色），靠深浅差而不是亮度差来说话。</p>
     */
    private static final float BLUR_WIDE_LIGHT = 0.46f;
    private static final float BLUR_MID_LIGHT = 0.24f;
    private static final float BLUR_CORE_LIGHT = 0.11f;

    /**
     * 浅色主题的「光晕增益」：三条通道各画两遍再叠一遍描边。
     *
     * <p>为什么需要它：浅色模式现在**不改数字颜色**（vc145 起，用户选什么就显示什么），
     * 于是明度已经顶到头的纯色（纯黄 #FFFF00、纯青、纯洋红）白卡上和背景只差一点点，
     * 光晕几乎看不见。既然颜色不许动，就只能**加量**——同一束光多叠几层，
     * 让它在白底上压出可见的一圈。代价是纯色那几位的光晕会偏实（不像呼吸光），
     * 但颜色零偏差是硬要求，亮度可以让步。</p>
     */
    private static final int LIGHT_PASSES = 2;

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
    /** 浅色主题（深色晕、大半径）。切换时重建 filter。 */
    private boolean lightTheme = false;
    private boolean filtersLight = false;

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

    /** 告诉它当前是浅色还是深色主题：两者用不同的半径与透明度曲线。 */
    public void setLightTheme(boolean value) {
        if (lightTheme != value) {
            lightTheme = value;
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
        return textSize * 0.46f;
    }

    @Override
    public void draw(Canvas canvas) {
        if (glyph.length() == 0 || textSize <= 0f) {
            return;
        }
        if (filtersFor != textSize || filtersLight != lightTheme) {
            wideFilter = new BlurMaskFilter(textSize * (lightTheme ? BLUR_WIDE_LIGHT : BLUR_WIDE),
                    BlurMaskFilter.Blur.NORMAL);
            midFilter = new BlurMaskFilter(textSize * (lightTheme ? BLUR_MID_LIGHT : BLUR_MID),
                    BlurMaskFilter.Blur.NORMAL);
            coreFilter = new BlurMaskFilter(textSize * (lightTheme ? BLUR_CORE_LIGHT : BLUR_CORE),
                    BlurMaskFilter.Blur.NORMAL);
            filtersFor = textSize;
            filtersLight = lightTheme;
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

        // 三个通道都按满亮度算：呼吸的「亮端」直接顶到 alpha 上限，暗端几乎灭掉，
        // 这样一呼一吸的亮度差才够大（之前暗端留了 0.42，看着像没在呼吸）。
        // 浅色主题不能用同一组透明度：白底上低 alpha 的深色会被冲得很淡，光晕几乎不存在
        // （真机反馈「浅色模式下光晕不够明显」）。外层抬到 0.75、中层顶满，
        // 芯光略低一点，否则会在笔画边上糊出一条硬边。
        final int ra = Math.round(lightTheme ? 190f * breath : 200f * breath);
        final int ga = Math.round(255f * breath);
        final int ba = Math.round(lightTheme ? 235f * breath : 255f * breath);

        // 浅色主题叠两遍：颜色不变，只靠叠加把可见度堆上去（见 LIGHT_PASSES）。
        final int passes = lightTheme ? LIGHT_PASSES : 1;

        paint.setMaskFilter(wideFilter);
        paint.setColor(Color.argb(ra, r, g, b));
        for (int i = 0; i < passes; i++) {
            canvas.drawText(glyph, penX, baseline, paint);
        }

        paint.setMaskFilter(midFilter);
        paint.setColor(Color.argb(ga, r, g, b));
        for (int i = 0; i < passes; i++) {
            canvas.drawText(glyph, penX, baseline, paint);
        }

        // 浅色主题多画一圈细描边：白卡上模糊出来的光很容易被冲淡，描边把笔画的轮廓
        // 勾回来，让「这一位在发光」这件事有个明确的形状。半径取芯光的一半，
        // 太粗会变成加粗数字（用户上一轮已经要求数字本身加粗过，别再叠粗）。
        if (lightTheme) {
            paint.setMaskFilter(null);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(1.5f, textSize * 0.022f));
            paint.setColor(Color.argb(Math.round(150f * breath), r, g, b));
            canvas.drawText(glyph, penX, baseline, paint);
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setMaskFilter(coreFilter);
        paint.setColor(Color.argb(Math.round(240f * breath), r, g, b));
        canvas.drawText(glyph, penX, baseline, paint);

        paint.setMaskFilter(null);
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
