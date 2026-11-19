package io.github.kelebao.colorosclockseconds;

import android.graphics.Rect;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * 冒号「光学居中」修正（vc349）：每个绘制/测量时刻都按当前画笔现场计算，不记机型。
 *
 * <p>历史上固定抬 {@code 0.11 em}（ColorOS 字体经验值），换到 OriginOS/vivo 等字体
 * 会抬过头；vc347 起改为按当前字体量「0」和「:」的字形中心，自动决定抬升；
 * vc349 进一步把冒号宽度也交给当前字体计算：以实测数字 advance 为基准给冒号分配
 * 固定比例宽度，再在该区间内把点组居中。字号、字体、状态栏大小变化时全部实时重算。</p>
 *
 * <p>只作用于被 span 覆盖的冒号字符，不动数字、不引入机型/ROM 白名单。</p>
 */
public class ColonLiftSpan extends MetricAffectingSpan {

    /** 抬升量上限 = 字号 × 该比例，防止异常字体度量把冒号抬飞。 */
    private static final float MAX_LIFT_RATIO = 0.20f;
    /** 允许向下修正的幅度（字形本身偏高时），同样按字号比例限制。 */
    private static final float MIN_LIFT_RATIO = 0.08f;

    /** 冒号目标宽度 = 数字 advance × 该比例。 */
    private static final float COLON_WIDTH_RATIO = 0.45f;
    /** 极端字体保护：冒号宽度落在数字宽度的该区间内。 */
    private static final float MIN_COLON_WIDTH_RATIO = 0.30f;
    private static final float MAX_COLON_WIDTH_RATIO = 0.55f;

    @Override
    public void updateMeasureState(TextPaint tp) {
        apply(tp);
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        apply(tp);
    }

    private static void apply(TextPaint tp) {
        if (tp == null) {
            return;
        }
        // 水平：用当前字体的数字实际 advance 推算冒号应占宽度，再缩放冒号 glyph。
        // 这样系统改字号/字体/状态栏尺寸后，冒号位置随总宽自动重排，无需按机型适配。
        float digitAdvance = tp.measureText("0");
        float colonAdvance = tp.measureText(":");
        if (digitAdvance > 0f && colonAdvance > 0f) {
            float target = digitAdvance * COLON_WIDTH_RATIO;
            float lo = digitAdvance * MIN_COLON_WIDTH_RATIO;
            float hi = digitAdvance * MAX_COLON_WIDTH_RATIO;
            if (target < lo) {
                target = lo;
            }
            if (target > hi) {
                target = hi;
            }
            tp.setTextScaleX(target / colonAdvance);
        }
        // 垂直：点组中心对齐数字中心；lift>0 抬高，lift<0 下压。
        int lift = computeLift(tp);
        if (lift != 0) {
            tp.baselineShift -= lift;
        }
    }

    /**
     * 计算冒号需要的垂直修正量：让「:」点组中心与「0」的中心重合。
     *
     * @return 像素数；正=抬高，负=下压；字体度量不可用时返回 0
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
        int min = -Math.round(tp.getTextSize() * MIN_LIFT_RATIO);
        int max = Math.round(tp.getTextSize() * MAX_LIFT_RATIO);
        if (lift < min) {
            lift = min;
        }
        if (lift > max) {
            lift = max;
        }
        return lift;
    }

    /** 诊断用：当前画笔下的字号、字形边界、抬升量与自动冒号宽度比例。 */
    public static String describe(TextPaint tp) {
        if (tp == null) {
            return "paint=null";
        }
        Rect digit = new Rect();
        Rect colon = new Rect();
        tp.getTextBounds("0", 0, 1, digit);
        tp.getTextBounds(":", 0, 1, colon);
        float digitAdvance = tp.measureText("0");
        float colonAdvance = tp.measureText(":");
        float scale = digitAdvance > 0f && colonAdvance > 0f
                ? digitAdvance * COLON_WIDTH_RATIO / colonAdvance : 1f;
        return "size=" + tp.getTextSize()
                + " digit=[" + digit.top + "," + digit.bottom + "]"
                + " colon=[" + colon.top + "," + colon.bottom + "]"
                + " lift=" + computeLift(tp)
                + " adv0=" + digitAdvance + " adv:=" + colonAdvance
                + " colonScale=" + scale;
    }
}
