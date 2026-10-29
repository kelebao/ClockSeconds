package io.github.kelebao.colorosclockseconds;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.Layout;
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
 * 就能得到真渐变——不需要新建 View、不需要自绘文字、不需要额外动画定时器。</p>
 *
 * <p><b>成本：</b>整串只有一个 span、一次 shader 构建（同宽度同相位时复用）。「流动」的相位取自
 * 墙钟（{@link ClockSecondsConfig#GRADIENT_FLOW_PERIOD_MS}），完全由时钟本来就有的每秒重绘
 * 驱动前进，不产生任何额外唤醒或重绘请求。</p>
 */
public class GradientTextSpan extends CharacterStyle {

    private final WeakReference<TextView> mView;
    private final int mColorStart;
    private final int mColorEnd;
    private final boolean mFlow;

    private Shader mShader;
    private float mShaderX0;
    private float mShaderX1;
    private float mShaderPhase = -1f;

    public GradientTextSpan(TextView view, int colorStart, int colorEnd, boolean flow) {
        mView = new WeakReference<>(view);
        mColorStart = colorStart;
        mColorEnd = colorEnd;
        mFlow = flow;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        TextView view = mView.get();
        if (view == null) {
            return;
        }
        // 渐变必须按「文字实际占的宽度」铺，不能按 View 宽度：设置页预览卡里的 TextView 是
        // match_parent + 居中，文字比 View 窄很多，按 View 宽度铺只能看到色带中间一小段，
        // 表现为「颜色被冲淡成一片粉」。用 Layout 的行左右边界取真实文字范围。
        float x0 = 0f;
        float x1 = Math.max(1, view.getWidth());
        Layout layout = view.getLayout();
        if (layout != null && layout.getLineCount() > 0) {
            float l = layout.getLineLeft(0);
            float r = layout.getLineRight(0);
            if (r > l + 1f) {
                x0 = l;
                x1 = r;
            }
        }
        float span = Math.max(1f, x1 - x0);
        float phase = 0f;
        if (mFlow) {
            long period = ClockSecondsConfig.GRADIENT_FLOW_PERIOD_MS;
            if (period > 0) {
                phase = (System.currentTimeMillis() % period) / (float) period;
            }
        }
        // 同一几何 / 同一相位下重复绘制不重建 shader（一秒内可能被绘制多次）。
        if (mShader == null || mShaderX0 != x0 || mShaderX1 != x1 || mShaderPhase != phase) {
            mShader = buildShader(x0, x1, span, phase);
            mShaderX0 = x0;
            mShaderX1 = x1;
            mShaderPhase = phase;
        }
        tp.setShader(mShader);
    }

    private Shader buildShader(float x0, float x1, float span, float phase) {
        // 一个完整色带 = 文字宽度；MIRROR 让色带首尾对称循环，流动时没有硬边（直接 REPEAT
        // 会在「终点色 → 起点色」处出现一条明显接缝）。静止时正好是「左起点色 → 右终点色」。
        float offset = phase * span;
        return new LinearGradient(x0 - offset, 0f, x1 - offset, 0f,
                new int[]{mColorStart, mColorEnd}, null, Shader.TileMode.MIRROR);
    }
}
