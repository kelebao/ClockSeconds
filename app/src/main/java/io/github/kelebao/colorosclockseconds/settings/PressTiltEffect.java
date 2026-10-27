package io.github.kelebao.colorosclockseconds.settings;

import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * 卡片「按压倾斜」效果（可复用组件）。
 *
 * <p>用法：把需要该效果的卡片逐个 {@link #register(View, int)} 注册进来，然后在 Activity 的
 * {@code dispatchTouchEvent()} 里调用 {@link #onTouch(MotionEvent)} 即可。</p>
 *
 * <ul>
 *   <li>按下：按屏幕坐标命中「最内层」已注册卡片 → 朝受力方向 3D 倾斜 + 0.97 下压，
 *       受力点泛起径向高光（前景层，裁不裁圆角由卡片自身 clipToOutline 决定）。</li>
 *   <li>移动：只更新 transform，不碰背景/高光（避免每帧重绘 → 卡顿）。</li>
 *   <li>松手/取消：带 1.35 过冲的弹簧曲线回弹复原，高光隐去。</li>
 * </ul>
 *
 * <p>为什么放在 Activity.dispatchTouchEvent 而不是每个卡片挂 OnTouchListener：
 * ColorOS 上子视图监听在 DOWN 返回 false 后，UP 常常不回传给该 listener，导致「压下去弹不回」；
 * 而 Activity 的 dispatchTouchEvent 是触摸分发最上游，UP/CANCEL 必然能收到。</p>
 */
public class PressTiltEffect {
    /** 按下瞬间回调（可用于播放按压音等）。 */
    public interface OnPressDownListener {
        void onPressDown();
    }

    private static final float MAX_DEG = 2f;        // 最大倾斜角（用户要求从 3 度减到 2 度）
    private static final float PRESS_SCALE = 0.97f; // 按下缩放
    private static final long RELEASE_MS = 450L;    // 回弹时长

    private final List<Pair<View, GradientDrawable>> mTargets = new ArrayList<>();
    /** 每张卡片的「平时倾角」：松手回弹回到这个角度，而不是一律回到 0 度。 */
    private final java.util.Map<View, Float> mBaseTiltX = new java.util.WeakHashMap<>();
    private final int[] mLoc = new int[2];   // 复用：避免每次触摸 new int[2]
    private final Rect mRect = new Rect();   // 复用：命中测试
    private final OvershootInterpolator mOver = new OvershootInterpolator(1.35f);
    private OnPressDownListener mListener;

    private boolean mPressing;
    private View mCard;
    private GradientDrawable mGlow;

    public void setOnPressDownListener(OnPressDownListener l) {
        mListener = l;
    }

    /**
     * 注册一张卡片：在其「前景层」叠一圈默认透明的径向高光，并记入命中表。
     *
     * @param accentColor 高光颜色（一般传主题强调色）
     */
    public void register(View card, int accentColor) {
        if (card == null) {
            return;
        }
        final GradientDrawable glow = new GradientDrawable();
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setGradientRadius(1f);
        glow.setGradientCenter(0.5f, 0.5f);
        glow.setColors(new int[]{(accentColor & 0x00FFFFFF) | 0x5A000000, 0x00000000});
        glow.setAlpha(0);
        // 用前景层放高光：永远盖在卡片内容之上，绕开背景 LayerDrawable 嵌套导致
        // 内层 glow 拿不到 bounds / invalidate 不传播 → 高光不显示的问题。
        card.setForeground(glow);
        mTargets.add(new Pair<>(card, glow));
    }

    /**
     * 让某张卡片平时就保持一个固定倾角（例如取色时预览卡下端内倾 20 度）。
     *
     * <p>没有这一步的话，按压回弹会把 rotationX 一律拉回 0，刚设好的倾角会被下一次点击抹掉。</p>
     */
    public void setBaseTiltX(View card, float deg) {
        if (card == null) {
            return;
        }
        mBaseTiltX.put(card, deg);
    }

    private float baseTiltX(View card) {
        final Float v = mBaseTiltX.get(card);
        return v == null ? 0f : v;
    }

    /** 在 Activity.dispatchTouchEvent() 开头调用；内部不消费事件。 */
    public void onTouch(MotionEvent ev) {
        final int a = ev.getActionMasked();
        if (a == MotionEvent.ACTION_DOWN) {
            final Pair<View, GradientDrawable> hit = find(ev.getRawX(), ev.getRawY());
            if (hit != null) {
                mPressing = true;
                mCard = hit.first;
                mGlow = hit.second;
                mCard.animate().cancel();
                if (mListener != null) {
                    mListener.onPressDown();
                }
                applyDown(mCard, mGlow, ev);
            }
        } else if (mPressing && a == MotionEvent.ACTION_MOVE) {
            if (mCard != null) {
                applyMove(mCard, ev);
            }
        } else if (mPressing && (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL)) {
            mPressing = false;
            if (mCard != null) {
                release(mCard, mGlow);
            }
        }
    }

    /** 命中测试：返回包含该屏幕坐标的「面积最小」（最内层）已注册卡片。 */
    private Pair<View, GradientDrawable> find(float rx, float ry) {
        Pair<View, GradientDrawable> best = null;
        long bestArea = Long.MAX_VALUE;
        for (Pair<View, GradientDrawable> p : mTargets) {
            p.first.getGlobalVisibleRect(mRect);
            if (mRect.contains((int) rx, (int) ry)) {
                final long area = (long) mRect.width() * mRect.height();
                if (area < bestArea) {
                    bestArea = area;
                    best = p;
                }
            }
        }
        return best;
    }

    private void applyDown(View card, GradientDrawable glow, MotionEvent e) {
        card.getLocationOnScreen(mLoc);   // DOWN：刷新卡片屏幕位置（MOVE 复用）
        final float w = card.getWidth(), h = card.getHeight();
        if (w <= 0f || h <= 0f) {
            return;
        }
        final float nx = clamp((e.getRawX() - mLoc[0] - w / 2f) / (w / 2f));
        final float ny = clamp((e.getRawY() - mLoc[1] - h / 2f) / (h / 2f));
        setTilt(card, nx, ny);
        if (glow != null) {
            glow.setGradientRadius(Math.max(w, h) * 0.35f);
            glow.setGradientCenter((nx + 1f) / 2f, (ny + 1f) / 2f);
            glow.setAlpha(255);
            card.invalidate();   // 强制整卡重绘，确保高光刷新
        }
    }

    private void applyMove(View card, MotionEvent e) {
        final float w = card.getWidth(), h = card.getHeight();
        if (w <= 0f || h <= 0f) {
            return;
        }
        final float nx = clamp((e.getRawX() - mLoc[0] - w / 2f) / (w / 2f));
        final float ny = clamp((e.getRawY() - mLoc[1] - h / 2f) / (h / 2f));
        setTilt(card, nx, ny);
    }

    private void setTilt(View card, float nx, float ny) {
        card.setRotationX(-ny * MAX_DEG);   // 上压→上侧沉入
        card.setRotationY(nx * MAX_DEG);    // 右压→右侧沉入
        card.setScaleX(PRESS_SCALE);
        card.setScaleY(PRESS_SCALE);
    }

    private void release(View card, GradientDrawable glow) {
        card.animate().setDuration(RELEASE_MS).setInterpolator(mOver)
                .rotationX(baseTiltX(card)).rotationY(0f).scaleX(1f).scaleY(1f).start();
        if (glow != null) {
            glow.setAlpha(0);
        }
    }

    private static float clamp(float v) {
        return Math.max(-1f, Math.min(1f, v));
    }
}
