package io.github.kelebao.colorosclockseconds;

import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.os.SystemClock;
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
 * <p><b>流动怎么做到顺滑（vc204）：</b>相位用<b>单调时钟毫秒</b>（{@link SystemClock#uptimeMillis()}），
 * 不再跟着「整秒」走；再由宿主（模块的状态栏时钟 / 设置页预览）在流动开启时按屏幕刷新逐帧
 * {@code invalidate()}，于是每次重绘相位都往前走一点点，肉眼是连续滑动而不是一秒跳一格。
 * 注意这里只重绘、不重排、不 setText，也不新建任何 View。</p>
 *
 * <p><b>成本控制：</b>shader 只在「色带宽度 / 端点色」变化时创建一次，逐帧流动靠
 * {@link Shader#setLocalMatrix} 平移，60fps 下不再每秒分配 60 个 LinearGradient。</p>
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
    private final boolean mFlow;
    /** 文字左边缘在绘制坐标里的 x 偏移（居中的 TextView 不为 0）。 */
    private final float mXOffset;
    /** 流动周期（毫秒）：一个完整色带扫过去再回来的时间。&lt;= 0 表示用默认值。 */
    private final long mPeriodMs;

    /** 重入护栏：万一测量/绘制链路再回调进来，直接复用上一次的 shader，绝不递归。 */
    private static final ThreadLocal<Boolean> IN_DRAW = new ThreadLocal<>();

    private Shader mShader;
    private Matrix mMatrix;
    private float mShaderBand = -1f;
    private int mShaderStart;
    private int mShaderEnd;
    private boolean mShaderColorsSet;
    /** 当前 shader 已平移的色带偏移（像素）；NaN = 还没设过。 */
    private float mAppliedOffset = Float.NaN;
    /** 一个完整色带的宽度 = 这段文字实测宽度（不用 Layout，避免 BoringLayout 递归）。 */
    private float mBand;
    private float mBandTextSize = -1f;
    private int mBandTextLength = -1;

    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            boolean flow) {
        this(view, text, colorStart, colorEnd, flow, 0f);
    }

    /**
     * @param xOffset 文字左边缘在绘制坐标中的 x（TextView 居中时 = (可用宽 - 文字宽) / 2）。
     *                必须由调用方在绘制之外算好传进来：绘制期不能去问 Layout（会递归崩溃）。
     */
    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            boolean flow, float xOffset) {
        this(view, text, colorStart, colorEnd, flow, xOffset,
                ClockSecondsConfig.GRADIENT_FLOW_PERIOD_MS);
    }

    /**
     * @param periodMs 流动周期（毫秒），一个完整色带扫过去再回来的时间；越界值由配置层夹紧。
     */
    public GradientTextSpan(TextView view, CharSequence text, int colorStart, int colorEnd,
                            boolean flow, float xOffset, long periodMs) {
        mView = new WeakReference<>(view);
        mText = text;
        mColorStart = colorStart;
        mColorEnd = colorEnd;
        mFlow = flow;
        mXOffset = xOffset;
        mPeriodMs = periodMs;
    }

    /** 流动是否开启（宿主据此决定要不要逐帧 invalidate）。 */
    public boolean isFlowing() {
        return mFlow;
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
            // shader 只按「色带宽度 + 端点色」建一次；流动靠 setLocalMatrix 平移，逐帧零分配。
            if (mShader == null || mShaderBand != mBand || !mShaderColorsSet
                    || mShaderStart != mColorStart || mShaderEnd != mColorEnd) {
                // 色带必须从「文字左边缘」起算，否则居中的 TextView 只画到色带的中后段
                // （MIRROR 会把它翻成反向渐变，真机表现就是预览卡的颜色顺序和状态栏相反）。
                mShader = new LinearGradient(mXOffset, 0f, mXOffset + mBand, 0f,
                        new int[]{mColorStart, mColorEnd}, null, Shader.TileMode.MIRROR);
                mShaderBand = mBand;
                mShaderStart = mColorStart;
                mShaderEnd = mColorEnd;
                mShaderColorsSet = true;
                mMatrix = new Matrix();
                mAppliedOffset = Float.NaN;
            }
            float offset = 0f;
            if (mFlow) {
                long period = mPeriodMs > 0 ? mPeriodMs : ClockSecondsConfig.GRADIENT_FLOW_PERIOD_MS;
                if (period > 0) {
                    // 单调时钟（毫秒精度）：相位连续推进，配合宿主逐帧 invalidate 才是顺滑的。
                    // 用墙钟会在改系统时间时跳变；用整秒就是「一秒跳一格」的根源。
                    float phase = (SystemClock.uptimeMillis() % period) / (float) period;
                    offset = phase * mBand;
                }
            }
            // 位移小于 0.35px 不重设，省掉大量无意义调用（低刷新率 / 长周期时尤其明显）。
            if (!(Math.abs(offset - mAppliedOffset) < 0.35f)) {
                if (offset == 0f) {
                    mShader.setLocalMatrix(null);
                } else {
                    mMatrix.setTranslate(-offset, 0f);
                    mShader.setLocalMatrix(mMatrix);
                }
                mAppliedOffset = offset;
            }
            tp.setShader(mShader);
        } catch (Throwable ignored) {
            // 绘制期异常会直接崩掉宿主进程（状态栏 = SystemUI），这里必须兜死。
        } finally {
            IN_DRAW.set(Boolean.FALSE);
        }
    }
}
