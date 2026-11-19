package io.github.kelebao.colorosclockseconds;

import android.graphics.Rect;
import android.text.TextPaint;
import android.text.style.CharacterStyle;

/**
 * 冒号「光学居中」修正：把 {@code :} 的两个点整体抬起，让点组的视觉中心落在数字的
 * 视觉中心上。
 *
 * <p><b>为什么需要它（vc221 真机实测）：</b>同一个时钟文本，只要被切成多个 run 绘制
 * （逐位 ForegroundColorSpan 会把 6 个数字各自切成一个 run），冒号就会被画在
 * <b>基线</b>上；而整串一个 run 时（未上色 / 渐变模式）冒号会被抬高约 {@code 0.11 em}。
 * 于是「单色页」的四个点比「渐变页」低 14px（预览卡，1240x2772 实测：
 * 渐变 459..545 / 单色 473..559），状态栏上同样存在（5px 级）。</p>
 *
 * <p><b>vc347 改为按字体度量计算（iQOO Neo11 反馈冒号偏高）：</b>原来固定抬
 * {@code 0.11 em}，这个比例是在 ColorOS 的字体上量出来的；换到 OriginOS 等字体
 * 冒号字形本身更高时就会抬过头。现在直接用当前 {@link TextPaint} 量「0」和「:」
 * 的字形边界，把点组中心对齐到数字中心，抬升量随字体自动变化。</p>
 *
 * <p>只改绘制基线（{@link TextPaint#baselineShift}），不参与排版测量、不影响行高与字宽。</p>
 */
public class ColonLiftSpan extends CharacterStyle {

    /** 抬升量上限 = 字号 × 该比例，防止异常字体度量把冒号抬飞。 */
    private static final float MAX_RATIO = 0.20f;

    @Override
    public void updateDrawState(TextPaint tp) {
        int lift = computeLift(tp);
        if (lift != 0) {
            // 方向：这条绘制路径上 baselineShift 取正值是往下走，取负值才是抬高（同 LiftSpan）。
            tp.baselineShift -= lift;
        }
    }

    /**
     * 计算冒号需要抬升的像素数：让「:」点组的垂直中心与「0」的垂直中心重合。
     *
     * @return 非负像素数；字体度量不可用时返回 0
     */
    public static int computeLift(TextPaint tp) {
        if (tp == null) {
            return 0;
        }
        Rect digit = new Rect();
        Rect colon = new Rect();
        tp.getTextBounds("0", 0, 1, digit);
        tp.getTextBounds(":", 0, 1, colon);
        int digitCenter = (digit.top + digit.bottom) / 2;
        int colonCenter = (colon.top + colon.bottom) / 2;
        int lift = colonCenter - digitCenter;
        int max = Math.round(tp.getTextSize() * MAX_RATIO);
        if (lift < 0) {
            lift = 0;
        }
        if (lift > max) {
            lift = max;
        }
        return lift;
    }

    /** 诊断用：当前画笔下的字号、字形边界与计算出的抬升量（仅调试构建写日志）。 */
    public static String describe(TextPaint tp) {
        if (tp == null) {
            return "paint=null";
        }
        Rect digit = new Rect();
        Rect colon = new Rect();
        tp.getTextBounds("0", 0, 1, digit);
        tp.getTextBounds(":", 0, 1, colon);
        return "size=" + tp.getTextSize()
                + " digit=[" + digit.top + "," + digit.bottom + "]"
                + " colon=[" + colon.top + "," + colon.bottom + "]"
                + " lift=" + computeLift(tp);
    }
}
