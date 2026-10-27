package io.github.kelebao.colorosclockseconds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;

/**
 * 自绘 HSV 取色控件（不引第三方库，保持 APK 体积）。
 *
 * <p>{@link #MODE_SV} 是「饱和度 × 明度」大面板，{@link #MODE_HUE} 是横向色相条。
 * 两者配合使用：色相条改色相，面板改饱和度/明度，拖动即时回调。</p>
 */
public final class HsvPickerView extends View {

    public static final int MODE_SV = 0;
    public static final int MODE_HUE = 1;

    /** 颜色变化回调（拖动中连续回调）。 */
    public interface OnColorChanged {
        void onColorChanged(int color);
    }

    private final int mode;
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint overlay = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbRing = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float hue = 0f;
    private float sat = 1f;
    private float val = 1f;
    private OnColorChanged callback;

    public HsvPickerView(Context context, int mode) {
        super(context);
        this.mode = mode;
        setFocusable(true);
        thumbRing.setStyle(Paint.Style.FILL);
        thumbRing.setColor(0x40000000);
        thumbStroke.setStyle(Paint.Style.STROKE);
        thumbStroke.setStrokeWidth(dp(3));
        thumbStroke.setColor(0xFFFFFFFF);
        thumbFill.setStyle(Paint.Style.FILL);
    }

    public void setOnColorChanged(OnColorChanged cb) {
        callback = cb;
    }

    /** 设为已有颜色（色相条只取色相，面板取全部三个分量）。 */
    public void setColor(int color) {
        final float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hue = hsv[0];
        if (mode == MODE_SV) {
            sat = hsv[1];
            val = hsv[2];
        }
        invalidate();
    }

    /** 当前色：色相条返回纯色（饱和度/明度 100%），面板返回实际颜色。 */
    public int getColor() {
        if (mode == MODE_HUE) {
            return Color.HSVToColor(new float[]{hue, 1f, 1f});
        }
        return Color.HSVToColor(new float[]{
                hue, Math.max(0f, Math.min(1f, sat)), Math.max(0f, Math.min(1f, val))});
    }

    public float getHue() {
        return hue;
    }

    /** 外部改色相（色相条拖动时同步面板背景）。 */
    public void setHue(float value) {
        hue = ((value % 360f) + 360f) % 360f;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        final int w = resolveSize(getSuggestedMinimumWidth(), widthSpec) > 0
                ? MeasureSpec.getSize(widthSpec) : dp(220);
        final int wantH = mode == MODE_HUE ? dp(26) : dp(170);
        final int h = resolveSize(wantH, heightSpec);
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int w = getWidth();
        final int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        rect.set(0, 0, w, h);
        if (mode == MODE_SV) {
            // 横向：白 → 纯色相；纵向：透明 → 黑
            fill.setShader(new LinearGradient(0, 0, w, 0, 0xFFFFFFFF,
                    Color.HSVToColor(new float[]{hue, 1f, 1f}), Shader.TileMode.CLAMP));
            canvas.drawRect(rect, fill);
            overlay.setShader(new LinearGradient(0, 0, 0, h, 0x00000000, 0xFF000000,
                    Shader.TileMode.CLAMP));
            canvas.drawRect(rect, overlay);
            drawThumb(canvas, sat * w, (1f - val) * h, getColor());
        } else {
            final int[] stops = new int[7];
            for (int i = 0; i < 7; i++) {
                stops[i] = Color.HSVToColor(new float[]{i * 60f, 1f, 1f});
            }
            fill.setShader(new LinearGradient(0, 0, w, 0, stops, null, Shader.TileMode.CLAMP));
            canvas.drawRect(rect, fill);
            drawThumb(canvas, hue / 360f * w, h / 2f, getColor());
        }
    }

    private void drawThumb(Canvas canvas, float x, float y, int color) {
        final float r = dp(9);
        thumbFill.setColor(color);
        canvas.drawCircle(x, y, r + dp(2), thumbRing);
        canvas.drawCircle(x, y, r, thumbFill);
        canvas.drawCircle(x, y, r, thumbStroke);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                applyTouch(event.getX(), event.getY());
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void applyTouch(float x, float y) {
        final int w = Math.max(1, getWidth());
        final int h = Math.max(1, getHeight());
        if (mode == MODE_SV) {
            sat = clamp(x / w);
            val = 1f - clamp(y / h);
        } else {
            float ratio = clamp(x / w);
            if (ratio >= 1f) {
                ratio = 0.999f;   // 360 度回绕成红色会跳，取一点余量
            }
            hue = ratio * 360f;
        }
        invalidate();
        if (callback != null) {
            callback.onColorChanged(getColor());
        }
    }

    private static float clamp(float v) {
        return v < 0f ? 0f : (v > 1f ? 1f : v);
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
