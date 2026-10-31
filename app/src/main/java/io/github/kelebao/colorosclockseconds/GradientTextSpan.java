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
    private final int mColorStart;
    private final int mColorEnd;
    /** 文字左边缘在绘制坐标里的 x 偏移（居中的 TextView 不为 0）。 */
    private final float mXOffset;
    /** 渐变方向：0 水平 / 1 竖直 / 2 对角（ClockSecondsConfig.GRADIENT_DIRECTION_*）。 */
    private final int mDirection;

    /** 重入护栏：万一测量/绘制链路再回调进来，直接复用上一次的 shader，绝不递归。 */
    private static final ThreadLocal<Boolean> IN_DRAW = new ThreadLocal<>();

    private Shader mShader;
    private float mShaderBand = -1f;
    private int mShaderStart;
    private int mShaderEnd;
    private boolean mShaderColorsSet;
    /** 一个完整色带的宽度 = 这段文字实测宽度（不用 Layout，避免 BoringLayout 递归）。 */
    private float mBand;
    private float mBandTextSize = -1f;
    private int mBandTextLength = -1;

    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd) {
        this(view, text, colorStart, colorEnd, 0f, 0);
    }

    /**
     * @param xOffset 文字左边缘在绘制坐标中的 x（TextView 居中时 = (可用宽 - 文字宽) / 2）。
     *                必须由调用方在绘制之外算好传进来：绘制期不能去问 Layout（会递归崩溃）。
     */
    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            float xOffset) {
        this(view, text, colorStart, colorEnd, xOffset, 0);
    }

    /** @param direction 0 水平 / 1 竖直 / 2 对角。 */
    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            float xOffset, int direction) {
        mView = new WeakReference<>(view);
        mText = text;
        mColorStart = colorStart;
        mColorEnd = colorEnd;
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
            // shader 只按「色带宽度 + 端点色」建一次，之后直接复用。
            if (mShader == null || mShaderBand != mBand || !mShaderColorsSet
                    || mShaderStart != mColorStart || mShaderEnd != mColorEnd) {
                // 色带必须从「文字左边缘」起算，否则居中的 TextView 只画到色带的中后段
                // （MIRROR 会把它翻成反向渐变，真机表现就是预览卡的颜色顺序和状态栏相反）。
                // 竖直 / 对角方向用字形盒（ascent..descent，相对基线的负/正值）做纵向跨度。
                // 竖直 / 对角：纵向跨整个行盒。注意 shader 坐标原点是行顶（不是基线），
                // 所以跨度是 0..行高（descent - ascent），而不是 tp.ascent()..tp.descent()——
                // 后者把色带整体悬到文字上方，被 MIRROR 翻成反渐变（真机实测顶紫底青）。
                if (mDirection == 1) {
                    mShader = new LinearGradient(mXOffset, 0f, mXOffset,
                            tp.descent() - tp.ascent(),
                            new int[]{mColorStart, mColorEnd}, null, Shader.TileMode.MIRROR);
                } else if (mDirection == 2) {
                    mShader = new LinearGradient(mXOffset, 0f, mXOffset + mBand,
                            tp.descent() - tp.ascent(),
                            new int[]{mColorStart, mColorEnd}, null, Shader.TileMode.MIRROR);
                } else {
                    mShader = new LinearGradient(mXOffset, 0f, mXOffset + mBand, 0f,
                            new int[]{mColorStart, mColorEnd}, null, Shader.TileMode.MIRROR);
                }
                mShaderBand = mBand;
                mShaderStart = mColorStart;
                mShaderEnd = mColorEnd;
                mShaderColorsSet = true;
            }
            tp.setShader(mShader);
        } catch (Throwable ignored) {
            // 绘制期异常会直接崩掉宿主进程（状态栏 = SystemUI），这里必须兜死。
        } finally {
            IN_DRAW.set(Boolean.FALSE);
        }
    }
}
