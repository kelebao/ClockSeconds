package io.github.kelebao.colorosclockseconds.settings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.View;

/**
 * t38①：选中涟漪 overlay（从 AdvancedSettingsActivity 内联类抽出为独立文件）。
 * t42①：按用户参考图改为「多层同心环扩散」——与首页色点 {@link
 * io.github.kelebao.colorosclockseconds.BreathSwatchDrawable}（vc227 双涟漪方案）同款画法：
 * 两圈同心圆环相位错开半圈、从本体核心边缘向外扩散到 ~2.4×，alpha 按 (1-k)^0.7 渐隐，
 * 颜色与本体同色系（调用方传 accent/本体色）。叠加在不裁切子 View 的宿主
 * （rippleHost，t40 架构）之上，动画结束由调用方移除，无残留。
 */
public final class StopRippleView extends View {
    /** 两圈涟漪的相位间隔（约半圈）。 */
    private static final float RING_PHASE_GAP = 0.45f;
    /** 涟漪峰值透明度。 */
    private static final float RING_ALPHA = 0xC8f;
    /** 淡出曲线指数：中段仍清晰可见。 */
    private static final float RING_FADE_POW = 0.7f;

    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float mCx, mCy, mBaseR;
    private float mProgress = 0f;

    /**
     * @param cx    触点 x（host 本地的坐标，即触摸事件的 getX()）
     * @param cy    触点 y
     * @param baseR 本体核心半径（如 24dp 圆=12dp、胶囊=min(宽,高)/2）；涟漪以此为基准扩散
     * @param color 涟漪颜色（accent/本体同色系）
     */
    public StopRippleView(Context c, float cx, float cy, float baseR, int color) {
        super(c);
        mCx = cx;
        mCy = cy;
        mBaseR = baseR <= 0f ? 1f : baseR;
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp(c, 2.5f));
        mPaint.setColor(color);
    }

    public void setProgress(float p) {
        mProgress = p;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mProgress <= 0f || mProgress >= 1f) {
            return;
        }
        // 两圈同心环：第 0 圈相位 = 进度，第 1 圈滞后约半圈；各自从核心边缘扩到 ~2.4×，(1-k)^0.7 渐隐
        for (int i = 0; i < 2; i++) {
            float k = mProgress - i * RING_PHASE_GAP;
            if (k <= 0f) {
                continue;
            }
            if (k > 1f) {
                k = 1f;
            }
            final float r = mBaseR * (0.6f + 1.8f * k);
            final int a = Math.round(RING_ALPHA * (float) Math.pow(1f - k, RING_FADE_POW));
            if (a <= 0) {
                continue;
            }
            mPaint.setAlpha(Math.min(255, a));
            canvas.drawCircle(mCx, mCy, r, mPaint);
        }
    }

    private static int dp(Context c, float v) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                c.getResources().getDisplayMetrics()));
    }
}
