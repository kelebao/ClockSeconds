package io.github.kelebao.colorosclockseconds.settings;import io.github.kelebao.colorosclockseconds.GradientShaderFactory;


import io.github.kelebao.colorosclockseconds.ClockSecondsConfig;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 设置页共享实时预览钟（vc328）：首页数字颜色卡与高级设置页复用同一个组件，编辑即所见。
 *
 * <p>渐变模式自己画一遍文字（不把 shader 挂给 TextView）：ColorOS 15 实测同一个 shader
 * 会按「run」被量化拆成 3 段纯色（数字一跳就换色块，看着像随机渐变），且色带原点随
 * 系统版本漂移（Android 15 偏左、Android 16 偏右一个文字宽）。自己 canvas.drawText
 * 一次：色带起点 = 文字左边缘、宽度 = 文字宽度，跨版本一致。</p>
 *
 * <p>时间来源两种：高级页没有页面级 ticker，开 {@link #setAutoTick(boolean)} 由组件
 * 内部对齐整秒自刷；首页已有自己的 updatePreview()（span 逻辑在页面侧），保持
 * autoTick 关闭，由页面继续驱动 setText。</p>
 */
public class SettingsPreviewClock extends TextView {

    /** 每次渲染回调：返回当前时刻要显示的内容（可为带 span 的富文本）。 */
    public interface TimeTextProvider {
        CharSequence textAt(long nowMillis);
    }

    private TimeTextProvider mProvider;
    private boolean mAutoTick;
    private boolean mShowSeconds = true;
    private final SimpleDateFormat mFmtSec =
            new SimpleDateFormat("HH:mm:ss", Locale.US);
    private final SimpleDateFormat mFmtNoSec =
            new SimpleDateFormat("HH:mm", Locale.US);
    private final Handler mTickHandler = new Handler(Looper.getMainLooper());
    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            refreshNow();
            // 对齐整秒：秒数不会在半秒处跳，视觉上不像「慢半拍」。
            long now = System.currentTimeMillis();
            mTickHandler.postDelayed(this, 1000L - now % 1000L + 20L);
        }
    };

    // —— 渐变自绘状态（与状态栏时钟同一套视觉规则） ——

    /** 色标颜色（t6：2 或 4 个，沿渐变轴顺序）。 */
    private int[] mGradCols;
    private boolean mGradOn;
    private float mGradShift;
    private Shader mGradShader;
    private float mGradShaderBand = -1f;
    /** 渐变方向：0..9（ClockSecondsConfig.GRADIENT_DIRECTION_*，8 径向 / 9 扫掠）。 */
    private int mGradDir;
    private int mGradShaderDir = -1;

    /** t18②：单色模式的数字位间距（em）；渐变模式回 0，避免把渐变带拉宽。 */
    private static final float MONO_LETTER_SPACING = 0.06f;

    // —— 取景框（t11）：正在编辑的数字位用相机四角框线框出 ——

    /** 当前取景的数字位（slotOfChar 语义 0..5）；-1 = 不显示。 */
    private int mActiveDigit = -1;
    /** 框线颜色（默认调色盘选中紫，页面可按主题覆盖）。 */
    private int mViewfinderColor = 0xFF7C5CE0;
    private final Paint mViewfinderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnActiveDigitClickListener mDigitClickListener;

    public SettingsPreviewClock(Context context) {
        super(context);
    }

    /** 设渲染内容来源；传 null 回落为内部按显秒开关直接格式化。 */
    public void setTimeProvider(TimeTextProvider provider) {
        mProvider = provider;
        refreshNow();
    }

    /** 打开内部每秒刷新（对齐整秒）。页面已有 ticker 的（首页）不要开，避免双重渲染。 */
    public void setAutoTick(boolean on) {
        if (mAutoTick == on) {
            return;
        }
        mAutoTick = on;
        if (mAutoTick && isAttachedToWindow()) {
            mTickHandler.removeCallbacks(mTick);
            mTickHandler.post(mTick);
        }
    }

    /** 无 provider 时的显秒开关（页面用 provider 自己决定格式时无需调用）。 */
    public void setShowSeconds(boolean on) {
        mShowSeconds = on;
        refreshNow();
    }

    /** 立即按当前配置重画一次（写入颜色后调用，做到「编辑即所见」）。 */
    public void refreshNow() {
        if (mProvider != null) {
            setText(mProvider.textAt(System.currentTimeMillis()));
        } else if (mAutoTick) {
            setText((mShowSeconds ? mFmtSec : mFmtNoSec)
                    .format(new Date(System.currentTimeMillis())));
        }
        // 取景框跟字形走：9:59:59→10:00:00 这类位数字数变化会让字形整体平移，
        // 帧绘制拿不到「刚换字」信号，下一帧补一次重绘把框对上新位置。
        if (mActiveDigit >= 0) {
            postInvalidateDelayed(40);
        }
    }

    /** 设置渐变方向（0..9，t6 十方向；8 径向 / 9 扫掠），变化时重建 shader。 */
    public void setGradientDirection(int dir) {
        if (mGradDir == dir) {
            return;
        }
        mGradDir = dir;
        mGradShader = null;
        invalidate();
    }

    /** 打开渐变绘制；c0/c1 已按背景适配过（两色渐变，t6 起委托数组版本）。 */
    public void setGradientColors(int c0, int c1) {
        setGradientColors(new int[]{c0, c1});
    }

    /** 打开渐变绘制（t6 多色标：2 或 4 个，沿渐变轴顺序）；颜色已按背景适配过。 */
    public void setGradientColors(int[] colors) {
        if (mGradOn && colorsEqual(mGradCols, colors)) {
            return;
        }
        mGradOn = true;
        mGradCols = colors != null && colors.length >= 2 ? colors.clone()
                : new int[]{0xFFEAE6F8, 0xFF7C5CE0};
        mGradShader = null;
        // t18②：渐变自绘不走 layout 度量，间距还原避免影响自绘带计算
        setLetterSpacing(0f);
        invalidate();
    }

    /** 关闭渐变绘制，回落普通逐位上色文本。 */
    public void clearGradient() {
        if (!mGradOn) {
            return;
        }
        mGradOn = false;
        mGradShader = null;
        // t18②：单色走 layout 排版，加大数字位间距（冒号随之略宽，视觉更松）
        setLetterSpacing(MONO_LETTER_SPACING);
        invalidate();
    }

    /** 调试用色带平移（像素），仅 DEBUG 包由 Secure 键控制。 */
    public void setGradientShift(float shift) {
        if (mGradShift == shift) {
            return;
        }
        mGradShift = shift;
        invalidate();
    }

    /** 点击预览钟上某个数字位时回调（页面可用它切换编辑目标）。 */
    public interface OnActiveDigitClickListener {
        void onActiveDigitClick(int slot);
    }

    /**
     * 取景框（t11）：框住指定数字位（0..5，见 {@link ClockSecondsConfig#slotOfChar}），
     * -1 = 隐藏。切换即重绘，四角框线跟着编辑位移动。
     */
    public void setActiveDigit(int slot) {
        if (mActiveDigit == slot) {
            return;
        }
        mActiveDigit = slot;
        invalidate();
    }

    /** 取景框线颜色（默认 #7C5CE0，与调色盘选中态同色系）。 */
    public void setViewfinderColor(int color) {
        if (mViewfinderColor == color) {
            return;
        }
        mViewfinderColor = color;
        invalidate();
    }

    /** 注册「点数字位切编辑目标」回调；不注册则保持 TextView 默认点击行为（首页展开取色）。 */
    public void setOnActiveDigitClickListener(OnActiveDigitClickListener l) {
        mDigitClickListener = l;
        setClickable(l != null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mDigitClickListener == null) {
            // 没注册取位回调：不拦截，首页的 OnClickListener（展开取色）照常工作
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                return true;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                int slot = slotAt(event.getX());
                if (slot >= 0) {
                    mDigitClickListener.onActiveDigitClick(slot);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAutoTick) {
            mTickHandler.removeCallbacks(mTick);
            mTickHandler.post(mTick);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickHandler.removeCallbacks(mTick);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mGradOn) {
            super.onDraw(canvas);
            if (mActiveDigit >= 0) {
                drawViewfinder(canvas, null);
            }
            return;
        }
        final CharSequence cs = getText();
        if (cs == null || cs.length() == 0) {
            return;
        }
        final Paint paint = getPaint();
        final int len = cs.length();
        final float band = paint.measureText(cs, 0, len);
        if (band <= 0f) {
            return;
        }
        final Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        float baseline = (getHeight() - (fm.descent - fm.ascent)) / 2f - fm.ascent;
        if (getLayout() != null) {
            final int lb = getLayout().getLineBaseline(0);
            if (lb > 0) {
                baseline = lb;
            }
        }
        if (mGradShader == null || Math.abs(mGradShaderBand - band) > 0.01f
                || mGradShaderDir != mGradDir) {
            final float sx0 = (getWidth() - band) / 2f + mGradShift;
            final int[] cols = mGradCols;
            final float[] positions = cols.length > 2 ? uniformStopPositions(cols.length) : null;
            final float top = baseline + fm.ascent;
            final float bottom = baseline + fm.descent;
            final float height = bottom - top;
            final float cy = top + height / 2f;
            if (mGradDir >= ClockSecondsConfig.GRADIENT_DIRECTION_MESH_RADIAL) {
                // U-20261118①：特效方向委托共享工厂（与状态栏/悬浮窗一致）。
                mGradShader = GradientShaderFactory.build(cols, mGradDir,
                        sx0, band, top, bottom);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_RADIAL) {
                float radius = Math.max(band, height) / 2f;
                mGradShader = new android.graphics.RadialGradient(sx0 + band / 2f, cy, radius,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_SWEEP) {
                mGradShader = new android.graphics.SweepGradient(sx0 + band / 2f, cy,
                        cols, positions);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_VERTICAL) {
                mGradShader = new LinearGradient(sx0, top, sx0, bottom,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_DIAGONAL) {
                mGradShader = new LinearGradient(sx0, top, sx0 + band, bottom,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_RTL) {
                mGradShader = new LinearGradient(sx0 + band, top, sx0, top,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_BTT) {
                mGradShader = new LinearGradient(sx0, bottom, sx0, top,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_BRTL) {
                mGradShader = new LinearGradient(sx0 + band, bottom, sx0, top,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_TRBL) {
                mGradShader = new LinearGradient(sx0 + band, top, sx0, bottom,
                        cols, positions, Shader.TileMode.CLAMP);
            } else if (mGradDir == ClockSecondsConfig.GRADIENT_DIRECTION_BLTR) {
                mGradShader = new LinearGradient(sx0, bottom, sx0 + band, top,
                        cols, positions, Shader.TileMode.CLAMP);
            } else {
                mGradShader = new LinearGradient(sx0, top, sx0 + band, top,
                        cols, positions, Shader.TileMode.CLAMP);
            }
            mGradShaderBand = band;
            mGradShaderDir = mGradDir;
        }
        final float x0 = (getWidth() - band) / 2f + mGradShift;
        final int saveColor = paint.getColor();
        final Shader saveShader = paint.getShader();
        // 画布原点不是视图原点：硬件画布的 clip 会带上视图在父容器里的绝对位置
        // （本机实测 clip.left=523801），直接按 (0,0) 画会整条落到视图外、什么都不显示。
        // 视图完整可见时 clip 与视图矩形等大，用 clip 左上角做原点；一旦被父容器裁掉一部分，
        // clip 左上角就不再等于视图左上角，此时退回 (0,0)——那块本来在可视区外，看不到错位。
        final android.graphics.Rect cb = new android.graphics.Rect();
        canvas.getClipBounds(cb);
        final float ox = cb.width() >= getWidth() - 1 ? cb.left : 0f;
        final float oy = cb.height() >= getHeight() - 1 ? cb.top : 0f;
        canvas.save();
        canvas.translate(ox, oy);
        paint.setShader(mGradShader);
        canvas.drawText(cs, 0, len, x0, baseline, paint);
        canvas.restore();
        paint.setShader(saveShader);
        paint.setColor(saveColor);
        if (mActiveDigit >= 0) {
            // 渐变自绘路径：文本没走 layout，手动给出居中起点与基线，供取景框定位
            drawViewfinder(canvas, new float[]{x0, baseline});
        }
    }

    /**
     * 相机取景框（t11）：四角短线框住当前编辑的数字位。mono 路径传 {@code null}
     * （从 layout 取字形矩形），渐变自绘路径传 [x0, baseline] 手动度量。
     */
    private void drawViewfinder(Canvas canvas, float[] manual) {
        final CharSequence cs = getText();
        if (cs == null || cs.length() == 0 || mActiveDigit < 0) {
            return;
        }
        // 文本下标 → slot（t11：与逐位上色同一套映射，含单位数小时回落）
        int charIndex = -1;
        for (int i = 0; i < cs.length(); i++) {
            if (ClockSecondsConfig.slotOfChar(cs, i) == mActiveDigit) {
                charIndex = i;
                break;
            }
        }
        if (charIndex < 0) {
            return;   // 文本里没有这一位（如 HH:mm 无秒段），不画
        }
        final Paint paint = getPaint();
        float x0;
        float baseline;
        if (manual != null) {
            x0 = manual[0];
            baseline = manual[1];
        } else {
            android.text.Layout layout = getLayout();
            if (layout == null) {
                return;
            }
            x0 = layout.getLineLeft(0);
            baseline = layout.getLineBaseline(0);
        }
        // t18②：layout 排版含 letterSpacing，逐字 advance = measureText + spacing * textSize
        final float spacing = paint.getLetterSpacing() * paint.getTextSize();
        final float before = paint.measureText(cs, 0, charIndex) + spacing * charIndex;
        final float w = paint.measureText(cs, charIndex, charIndex + 1) + spacing;
        final Paint.FontMetrics fm = paint.getFontMetrics();
        final float top = baseline + fm.ascent;
        final float bottom = baseline + fm.descent;
        // 框外扩：不贴着字形，留一点呼吸感（t18①：18%→8% 字高，避免框盖到相邻数字）
        final float pad = (bottom - top) * 0.08f;
        final float l = x0 + before - pad;
        final float r = x0 + before + w + pad;
        final float t = top - pad;
        final float b = bottom + pad;
        // 四角「相机取景框」线：每角横竖两笔，臂长 ≈ 30% 框宽
        final float arm = Math.min(r - l, b - t) * 0.3f;
        final float stroke = Math.max(2f, (bottom - top) * 0.05f);
        mViewfinderPaint.setColor(mViewfinderColor);
        mViewfinderPaint.setStrokeWidth(stroke);
        mViewfinderPaint.setStyle(Paint.Style.STROKE);
        mViewfinderPaint.setStrokeCap(Paint.Cap.ROUND);
        // 左上
        canvas.drawLine(l, t, l + arm, t, mViewfinderPaint);
        canvas.drawLine(l, t, l, t + arm, mViewfinderPaint);
        // 右上
        canvas.drawLine(r - arm, t, r, t, mViewfinderPaint);
        canvas.drawLine(r, t, r, t + arm, mViewfinderPaint);
        // 左下
        canvas.drawLine(l, b - arm, l, b, mViewfinderPaint);
        canvas.drawLine(l, b, l + arm, b, mViewfinderPaint);
        // 右下
        canvas.drawLine(r, b - arm, r, b, mViewfinderPaint);
        canvas.drawLine(r - arm, b, r, b, mViewfinderPaint);
    }

    /** 触摸点 x → 数字位 slot；落在冒号或空白返回 -1。 */
    private int slotAt(float x) {
        final CharSequence cs = getText();
        if (cs == null || cs.length() == 0) {
            return -1;
        }
        final Paint paint = getPaint();
        android.text.Layout layout = getLayout();
        float x0;
        if (layout != null) {
            x0 = layout.getLineLeft(0);
        } else {
            final float band = paint.measureText(cs, 0, cs.length());
            x0 = (getWidth() - band) / 2f;
        }
        // t18②：与排版一致，逐字 advance 计入 letterSpacing
        final float spacing = paint.getLetterSpacing() * paint.getTextSize();
        float acc = x0;
        int lastSlot = -1;
        for (int i = 0; i < cs.length(); i++) {
            final float w = paint.measureText(cs, i, i + 1) + spacing;
            if (x >= acc && x < acc + w) {
                return ClockSecondsConfig.slotOfChar(cs, i);
            }
            int s = ClockSecondsConfig.slotOfChar(cs, i);
            if (s >= 0) {
                lastSlot = s;
            }
            acc += w;
        }
        return lastSlot;
    }

    /** n 个渐变色标均匀分布的位置数组（0..1）。 */
    private static float[] uniformStopPositions(int n) {
        float[] p = new float[n];
        for (int i = 0; i < n; i++) {
            p[i] = n <= 1 ? 0f : (float) i / (float) (n - 1);
        }
        return p;
    }

    private static boolean colorsEqual(int[] a, int[] b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null || a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
