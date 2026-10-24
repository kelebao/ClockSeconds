package io.github.kelebao.colorosclockseconds.settings;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

/**
 * 并排等高行：把每个子 View 统一按「最高的那一个」测量，保证同一行里各张卡下端对齐。
 *
 * <p>为什么不用 {@code LinearLayout.setMeasureWithLargestChildEnabled(true)}：
 * AOSP 里该逻辑只对「高度为 0 且带 weight」的子 View 生效（见 LinearLayout#measureHorizontal），
 * 普通的 wrap_content 高度子 View 不会被拉齐，实测两张卡仍差 11px。
 * 这里改成自己量两遍：先量出各自的自然高度，取最大值，再把每个子 View 的高度定成该值重测。
 * 子 View 拿到确定高度后，其内部带 weight 的行才能分到剩余空间（值行贴底对齐也依赖这点）。
 */
public class EqualHeightRow extends LinearLayout {

    public EqualHeightRow(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 复位为 wrap_content，保证内容变化（如手势名变长）后能重新按内容算高度。
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.height != LayoutParams.WRAP_CONTENT) {
                lp.height = LayoutParams.WRAP_CONTENT;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int max = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            max = Math.max(max, child.getMeasuredHeight());
        }
        if (max <= 0) {
            return;
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.height != max) {
                lp.height = max;
            }
        }
        // 再量一遍：此时子 View 高度已确定，内部带 weight 的行能分到剩余空间。
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
