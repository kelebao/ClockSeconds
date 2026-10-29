package io.github.kelebao.colorosclockseconds;

import android.text.TextPaint;
import android.text.style.CharacterStyle;

/**
 * 冒号「光学居中」修正：把 {@code :} 的两个点整体抬高一点，让它们在数字中间显得居中。
 *
 * <p><b>为什么需要它（vc221 真机实测）：</b>同一个时钟文本，只要被切成多个 run 绘制
 * （逐位 ForegroundColorSpan 会把 6 个数字各自切成一个 run），冒号就会被画在
 * <b>基线</b>上；而整串一个 run 时（未上色 / 渐变模式）冒号会被抬高约 {@code 0.11 em}。
 * 于是「单色页」的四个点比「渐变页」低 14px（预览卡，1240x2772 实测：
 * 渐变 459..545 / 单色 473..559），状态栏上同样存在（5px 级）。
 * 用户 m20412 反馈「单色的时候预览时间那四个小点好像下移了」即此。</p>
 *
 * <p>这是宿主的字形选择差异，不是我们画的：所以用一个只改绘制基线的 span
 * （{@link TextPaint#baselineShift}，不参与排版测量、不影响行高与字宽）把冒号抬回去，
 * 让「单色 / 渐变 / 状态栏」三处的四个点位置一致。</p>
 */
public class ColonLiftSpan extends CharacterStyle {

    /** 抬高量 = 字号 × 该比例（真机扫参：预览卡 14px / 121px ≈ 0.113，状态栏 5px / 47px ≈ 0.106）。 */
    private static final float RATIO = 0.11f;

    @Override
    public void updateDrawState(TextPaint tp) {
        // 方向：这条绘制路径上 baselineShift 取正值是往下走，取负值才是抬高（同 LiftSpan）。
        tp.baselineShift -= Math.round(tp.getTextSize() * RATIO);
    }
}
