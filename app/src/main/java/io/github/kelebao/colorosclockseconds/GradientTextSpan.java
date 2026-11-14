package io.github.kelebao.colorosclockseconds;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * 数字渐变的绘制样式（状态栏与设置页预览共用同一份实现，保证「预览即所写」）。
 *
 * <p><b>为什么用 CharacterStyle 而不是自绘：</b>{@code ForegroundColorSpan} 能在状态栏时钟上
 * 生效，说明 TextView 走的是标准 {@code Layout / TextLine} 绘制路径，而该路径一定会调用
 * {@link CharacterStyle#updateDrawState}。所以只要在画笔上挂一个 {@link LinearGradient}，
 * 就能得到真渐变——不需要新建 View、不需要自绘文字。</p>
 *
 * <p><b>静态渐变，不流动（vc212）：</b>按用户要求只保留「起点色 → 终点色」的固定渐变，
 * 不做色带动画，因此这里没有任何定时器 / 相位计算，颜色完全跟随文本重绘，
 * 对状态栏的额外开销为 0。</p>
 *
 * <p><b>vc194 血的教训：</b>这里绝对不能调用 {@code Layout.getLineLeft()/getLineRight()}。
 * 单行文本用的是 {@code BoringLayout}，它的 {@code getLineMax()} 会反过来触发
 * {@code TextLine.metrics() → updateDrawState()}，于是在
 * {@code getLineLeft → getLineMax → updateDrawState} 之间无限递归，
 * 直接 {@code StackOverflowError} 把 SystemUI 打死（vc193 真机实测：40 秒内连崩 4 次，
 * LSPosed 判定「System UI crashed too many times」→ 全模块进安全模式）。
 * 因此色带宽度改用 {@link TextPaint#measureText(CharSequence, int, int)} 自行测量，
 * 并且整个方法用 try/catch 兜底——绘制期抛异常是致命的。</p>
 */
public class GradientTextSpan extends CharacterStyle {

    private final WeakReference<TextView> mView;
    private final CharSequence mText;
    /** 色标颜色数组（t6）：2 色标（旧两色渐变）或 4 色标（起点/中间 2/中间 3/终点）。 */
    private final int[] mColors;
    /** 文字左边缘在绘制坐标里的 x 偏移（居中的 TextView 不为 0）。 */
    private final float mXOffset;
    /** 渐变方向：0..9（ClockSecondsConfig.GRADIENT_DIRECTION_*，8 径向 / 9 扫掠）。 */
    private final int mDirection;

    /** 重入护栏：万一测量/绘制链路再回调进来，直接复用上一次的 shader，绝不递归。 */
    private static final ThreadLocal<Boolean> IN_DRAW = new ThreadLocal<>();

    private Shader mShader;
    private float mShaderBand = -1f;
    private int mShaderDir = -1;
    private int[] mShaderColors;
    private boolean mShaderColorsSet;
    /** 一个完整色带的宽度 = 这段文字实测宽度（不用 Layout，避免 BoringLayout 递归）。 */
    private float mBand;
    private float mBandTextSize = -1f;
    private int mBandTextLength = -1;

    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd) {
        this(view, text, new int[]{colorStart, colorEnd}, 0f, 0);
    }

    /**
     * @param xOffset 文字左边缘在绘制坐标中的 x（TextView 居中时 = (可用宽 - 文字宽) / 2）。
     *                必须由调用方在绘制之外算好传进来：绘制期不能去问 Layout（会递归崩溃）。
     */
    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            float xOffset) {
        this(view, text, new int[]{colorStart, colorEnd}, xOffset, 0);
    }

    /** 旧两色构造：{@code direction} 0 水平 / 1 竖直 / 2 对角。 */
    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            float xOffset, int direction) {
        this(view, text, new int[]{colorStart, colorEnd}, xOffset, direction);
    }

    /**
     * t6 多色标构造：{@code colors} 长度 2（两色）或 4（四色渐变），
     * 颜色顺序 = 沿渐变轴的色标顺序。方向 8=径向 / 9=扫掠用中心对称 shader。
     */
    public GradientTextSpan(TextView view, CharSequence text, int[] colors,
                            float xOffset, int direction) {
        mView = new WeakReference<>(view);
        mText = text;
        mColors = colors != null && colors.length >= 2 ? colors.clone()
                : new int[]{0xFFEAE6F8, 0xFF7C5CE0};
        mXOffset = xOffset;
        mDirection = direction;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        if (Boolean.TRUE.equals(IN_DRAW.get())) {
            // 重入：说明宿主在测量过程中又回调了这里，直接用缓存 shader，什么都不重算。
            if (mShader != null) {
                tp.setShader(mShader);
            }
            return;
        }
        IN_DRAW.set(Boolean.TRUE);
        try {
            if (mText == null || mText.length() == 0) {
                return;
            }
            // 文字实测宽度：文字长度或字号变化时重算一次（AOD / 字体缩放会改字号）。
            if (mBand <= 0f || mBandTextSize != tp.getTextSize()
                    || mBandTextLength != mText.length()) {
                mBandTextSize = tp.getTextSize();
                mBandTextLength = mText.length();
                mBand = Math.max(1f, tp.measureText(mText, 0, mText.length()));
            }
            // shader 只按「色带宽度 + 色标颜色」建一次，之后直接复用。
            if (mShader == null || mShaderBand != mBand || !mShaderColorsSet
                    || mShaderDir != mDirection || !colorsEqual(mShaderColors, mColors)) {
                // 色带必须从「文字左边缘」起算，否则居中的 TextView 只画到色带的中后段
                // （MIRROR 会把它翻成反向渐变，真机表现就是预览卡的颜色顺序和状态栏相反）。
                // 竖直 / 对角方向用字形盒（ascent..descent，相对基线的负/正值）做纵向跨度。
                // 竖直 / 对角：纵向跨整个行盒。注意 shader 坐标原点是行顶（不是基线），
                // 所以跨度是 0..行高（descent - ascent），而不是 tp.ascent()..tp.descent()——
                // 后者把色带整体悬到文字上方，被 MIRROR 翻成反渐变（真机实测顶紫底青）。
                final int[] cols = mColors;
                final float[] positions = cols.length > 2
                        ? uniformPositions(cols.length) : null;
                if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_RADIAL
                        || mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_SWEEP) {
                    // 径向 / 扫掠：圆心 = 色带中心，纵向圆心取行盒垂直中点。
                    final float cy = (tp.descent() - tp.ascent()) / 2f;
                    final float radius = Math.max(mBand, tp.descent() - tp.ascent()) / 2f;
                    if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_RADIAL) {
                        mShader = new android.graphics.RadialGradient(
                                mXOffset + mBand / 2f, cy, radius,
                                cols, positions, Shader.TileMode.CLAMP);
                    } else {
                        mShader = new android.graphics.SweepGradient(
                                mXOffset + mBand / 2f, cy, cols, positions);
                    }
                } else if (mDirection == 1) {
                    mShader = new LinearGradient(mXOffset, 0f, mXOffset,
                            tp.descent() - tp.ascent(),
                            cols, positions, Shader.TileMode.MIRROR);
                } else if (mDirection == 2) {
                    mShader = new LinearGradient(mXOffset, 0f, mXOffset + mBand,
                            tp.descent() - tp.ascent(),
                            cols, positions, Shader.TileMode.MIRROR);
                } else if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_RTL) {
                    // 右 → 左：色标顺序翻转即可
                    mShader = new LinearGradient(mXOffset + mBand, 0f, mXOffset, 0f,
                            cols, positions, Shader.TileMode.MIRROR);
                } else if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_BTT) {
                    mShader = new LinearGradient(mXOffset, tp.descent() - tp.ascent(),
                            mXOffset, 0f, cols, positions, Shader.TileMode.MIRROR);
                } else if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_BRTL) {
                    mShader = new LinearGradient(mXOffset + mBand, tp.descent() - tp.ascent(),
                            mXOffset, 0f, cols, positions, Shader.TileMode.MIRROR);
                } else if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_TRBL) {
                    mShader = new LinearGradient(mXOffset + mBand, 0f,
                            mXOffset, tp.descent() - tp.ascent(),
                            cols, positions, Shader.TileMode.MIRROR);
                } else if (mDirection == ClockSecondsConfig.GRADIENT_DIRECTION_BLTR) {
                    mShader = new LinearGradient(mXOffset, tp.descent() - tp.ascent(),
                            mXOffset + mBand, 0f, cols, positions, Shader.TileMode.MIRROR);
                } else {
                    mShader = new LinearGradient(mXOffset, 0f, mXOffset + mBand, 0f,
                            cols, positions, Shader.TileMode.MIRROR);
                }
                mShaderBand = mBand;
                mShaderDir = mDirection;
                mShaderColors = cols.clone();
                mShaderColorsSet = true;
            }
            tp.setShader(mShader);
        } catch (Throwable ignored) {
            // 绘制期异常会直接崩掉宿主进程（状态栏 = SystemUI），这里必须兜死。
        } finally {
            IN_DRAW.set(Boolean.FALSE);
        }
    }

    /** n 个色标均匀分布的位置数组（0..1，首尾必在端点）。 */
    private static float[] uniformPositions(int n) {
        float[] p = new float[n];
        for (int i = 0; i < n; i++) {
            p[i] = n == 1 ? 0f : (float) i / (float) (n - 1);
        }
        return p;
    }

    private static boolean colorsEqual(int[] a, int[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
