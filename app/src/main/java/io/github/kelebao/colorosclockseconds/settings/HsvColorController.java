package io.github.kelebao.colorosclockseconds.settings;

import android.graphics.Color;

/**
 * HSV 取色状态机（vc328）：把「色相/饱和/明度三值 + 明度下限 + 进度换算」从各页面的
 * 滑杆监听里抽出来共用。UI 形态（三条窄条 / 2D 方块盘）只负责把用户触摸换算成
 * {@link #applyProgress} 或 {@link #applyHsv}，写入经 {@link Listener} 回调给页面——
 * 之后调色盘换新形态时只换 UI，不动取色状态逻辑。
 */
public final class HsvColorController {

    /** 状态推进后的回调：color 已含明度下限修正，可直接写入目标键。 */
    public interface Listener {
        void onColorChanged(int color, float hue, float sat, float val);
    }

    /** 明度下限：全黑会看不出来在改色，与旧三条滑杆一致取 0.08。 */
    public static final float MIN_VAL = 0.08f;

    private float mHue;
    private float mSat = 1f;
    private float mVal = 1f;
    private Listener mListener;

    public void setListener(Listener listener) {
        mListener = listener;
    }

    /** 用一个现有颜色重置状态（静默，不回调）。 */
    public void setFromColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        mHue = hsv[0];
        mSat = hsv[1];
        mVal = hsv[2];
    }

    /** 直接设三值（静默，不回调）。 */
    public void setHsv(float hue, float sat, float val) {
        mHue = hue;
        mSat = sat;
        mVal = val;
    }

    public float getHue() {
        return mHue;
    }

    public float getSat() {
        return mSat;
    }

    public float getVal() {
        return mVal;
    }

    /** 当前颜色（明度按 {@link #MIN_VAL} 兜底）。 */
    public int getColor() {
        return Color.HSVToColor(new float[]{mHue, mSat, Math.max(MIN_VAL, mVal)});
    }

    /** 三条 SeekBar 进度（0..255）推进状态并回调：色相 / 饱和度 / 明度。 */
    public void applyProgress(int hueProgress, int satProgress, int valProgress) {
        mHue = hueProgress / 255f * 360f;
        mSat = satProgress / 255f;
        mVal = valProgress / 255f;
        notifyChanged();
    }

    /** 2D 方块盘（色相 + 饱和度 × 明度）推进状态并回调。 */
    public void applyHsv(float hue, float sat, float val) {
        mHue = hue;
        mSat = sat;
        mVal = val;
        notifyChanged();
    }

    /** 状态 → 色相条进度（0..255）。 */
    public int hueProgress() {
        return Math.round(mHue / 360f * 255f);
    }

    /** 状态 → 饱和度条进度（0..255）。 */
    public int satProgress() {
        return Math.round(mSat * 255f);
    }

    /** 状态 → 明度条进度（0..255）。 */
    public int valProgress() {
        return Math.round(mVal * 255f);
    }

    private void notifyChanged() {
        if (mListener != null) {
            mListener.onColorChanged(getColor(), mHue, mSat, Math.max(MIN_VAL, mVal));
        }
    }
}
