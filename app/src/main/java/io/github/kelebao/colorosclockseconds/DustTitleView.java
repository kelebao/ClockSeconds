package io.github.kelebao.colorosclockseconds;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * 「时间显秒」标题的「文字由粉尘点阵构成」效果。
 *
 * <p>做法移植自 D:\MyApp\TextDust（纯代码、零素材、单文件 362 行）：
 * ①把文字光栅化到 Bitmap，用 alpha 当「这一格被墨覆盖了多少」；
 * ②按网格 + 抖动采样，并用「覆盖率 vs 白噪声阈值」把边缘稀成粉尘、字心保留密实；
 * ③每个点是一个弹簧质点，固定步长积分（1/120s）从随机位置聚拢成形；
 * ④按粗细分 3 桶，一次 {@link Canvas#drawPoints} 提交，几千点也不掉帧。</p>
 *
 * <p>相对原 demo 的改动：透明背景（设置页有渐变底）、用标题文字色 + 三档透明度代替灰阶、
 * 字号固定为标题字号（不再缩放到屏宽 86%）、支持 wrap_content 自适应大小、
 * 触摸只在「判定为点击」时炸开（拖动时把事件让回 ScrollView，不影响页面滚动）。</p>
 */
public class DustTitleView extends View {

    private static final int BUCKETS = 3;
    private static final float FIXED_DT = 1f / 120f;
    /** 三档透明度：边缘淡、字心实，形成粉尘层次。 */
    private static final int[] BUCKET_ALPHA = {150, 212, 255};

    private final String mText;
    private final int mColor;
    private final float mTextPx;
    private final float mDensity;

    private float[] hx = new float[0];
    private float[] hy = new float[0];
    private float[] px = new float[0];
    private float[] py = new float[0];
    private float[] vx = new float[0];
    private float[] vy = new float[0];
    private float[] rad = new float[0];
    private float[] delay = new float[0];
    private int n = 0;

    private final float[][] bucketPts = new float[BUCKETS][];
    private final int[] bucketCnt = new int[BUCKETS];
    private final float[] bucketStroke = new float[BUCKETS];
    private final int[] bucketColor = new int[BUCKETS];

    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint shapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private long lastNs = 0L;
    private float t = 0f;
    private float acc = 0f;
    private float baseR = 3f;
    private float touchR = 150f;

    private boolean touching = false;
    private float touchX = -1e5f;
    private float touchY = -1e5f;
    private float downX, downY;
    private long downNs;
    private final int mTouchSlop;

    public DustTitleView(Context c, String text, int color, float textSizePx) {
        super(c);
        mText = text;
        mColor = color;
        mTextPx = textSizePx;
        mDensity = getResources().getDisplayMetrics().density;
        mTouchSlop = ViewConfiguration.get(c).getScaledTouchSlop();
        dotPaint.setStrokeCap(Paint.Cap.ROUND);
        dotPaint.setStyle(Paint.Style.STROKE);
        shapePaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        shapePaint.setTextSize(textSizePx);
        setContentDescription(text);
        for (int b = 0; b < BUCKETS; b++) bucketPts[b] = new float[0];
    }

    /** 重放一次「聚拢成形」动画（切换页面/回到前台时可以再叫一次）。 */
    public void replay() {
        long seed = System.nanoTime();
        for (int k = 0; k < n; k++) {
            px[k] = hash01(k + (int) (seed & 0xFFFF), 11) * getWidth();
            py[k] = hash01(k + (int) (seed & 0xFFFF), 23) * getHeight();
            vx[k] = 0f;
            vy[k] = 0f;
        }
        t = 0f;
        acc = 0f;
        lastNs = 0L;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        float tw = shapePaint.measureText(mText);
        int pad = Math.max(2, Math.round(4f * mDensity));
        int w = (int) Math.ceil(tw) + pad * 2;
        int h = (int) Math.ceil(shapePaint.descent() - shapePaint.ascent()) + pad * 2;
        setMeasuredDimension(resolveSize(w, widthSpec), resolveSize(h, heightSpec));
    }

    @Override
    protected void onSizeChanged(int w, int h, int ow, int oh) {
        super.onSizeChanged(w, h, ow, oh);
        build(w, h);
    }

    // ================= 1. 采样 =================
    private void build(int w, int h) {
        if (w <= 0 || h <= 0 || mText == null || mText.isEmpty()) return;

        final float ts = mTextPx;
        final int pad = Math.max(2, Math.round(4f * mDensity));

        shapePaint.setTextSize(ts);
        int bw = (int) Math.ceil(shapePaint.measureText(mText)) + pad * 2;
        int bh = (int) Math.ceil(shapePaint.descent() - shapePaint.ascent()) + pad * 2;
        if (bw <= 0 || bh <= 0) return;

        Bitmap bmp = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888);
        Canvas bc = new Canvas(bmp);
        bc.drawText(mText, pad, pad - shapePaint.ascent(), shapePaint);
        int[] argb = new int[bw * bh];
        bmp.getPixels(argb, 0, bw, 0, 0, bw, bh);
        bmp.recycle();

        // 采样间距：字越大点越细；同时限制总点数，避免超大字号把 View 拖慢。
        // 采样间距：标题这种小字号必须细（step=1 才像粉尘而不是马赛克），
        // 但同时给总格数封顶，避免以后有人把标题调得很大时点数爆掉。
        int step = Math.max(1, Math.round(ts / 90f));
        int est = (bw / step + 1) * (bh / step + 1);
        while (est > 40000 && step < 8) {
            step++;
            est = (bw / step + 1) * (bh / step + 1);
        }
        baseR = step * 0.62f;
        touchR = 90f * mDensity;

        int ox = (w - bw) / 2;
        int oy = (h - bh) / 2;

        int cnt = 0;
        for (int y = 0; y < bh; y += step) {
            for (int x = 0; x < bw; x += step) {
                int a = argb[y * bw + x] >>> 24;
                if (a < 13) continue;
                if (noise(x, y) > a / 255f) continue;
                cnt++;
            }
        }
        if (cnt == 0) return;

        float[] nhx = new float[cnt];
        float[] nhy = new float[cnt];
        float[] nrad = new float[cnt];
        float[] ndly = new float[cnt];

        int i = 0;
        for (int y = 0; y < bh; y += step) {
            for (int x = 0; x < bw; x += step) {
                int a = argb[y * bw + x] >>> 24;
                if (a < 13) continue;
                float cov = a / 255f;
                if (noise(x, y) > cov) continue;
                nhx[i] = ox + x + (hash01(i, 3) - 0.5f) * step * 0.45f;
                nhy[i] = oy + y + (hash01(i, 9) - 0.5f) * step * 0.45f;
                nrad[i] = baseR * (0.55f + 0.55f * cov);
                // 从左到右逐字成形
                ndly[i] = (x / (float) Math.max(1, bw)) * 0.55f + hash01(i, 5) * 0.25f;
                i++;
            }
        }

        hx = nhx;
        hy = nhy;
        rad = nrad;
        delay = ndly;
        px = new float[cnt];
        py = new float[cnt];
        vx = new float[cnt];
        vy = new float[cnt];
        for (int k = 0; k < cnt; k++) {
            px[k] = hash01(k, 11) * w;
            py[k] = hash01(k, 23) * h;
        }
        n = cnt;

        for (int b = 0; b < BUCKETS; b++) {
            bucketPts[b] = new float[cnt * 2];
            bucketCnt[b] = 0;
            bucketColor[b] = (mColor & 0x00FFFFFF) | (BUCKET_ALPHA[b] << 24);
        }
        bucketStroke[0] = 2f * baseR * 0.72f;
        bucketStroke[1] = 2f * baseR * 1.00f;
        bucketStroke[2] = 2f * baseR * 1.22f;

        t = 0f;
        acc = 0f;
        lastNs = 0L;
        invalidate();
    }

    private static float noise(int x, int y) {
        int hh = x * 374761393 + y * 668265263;
        hh = (hh ^ (hh >>> 13)) * 1274126177;
        hh ^= (hh >>> 16);
        return (hh & 0xFFFFFF) / (float) 0x1000000;
    }

    private static float hash01(int i, int salt) {
        int hh = i * 0x27d4eb2d + salt * 0x165667b1;
        hh = (hh ^ (hh >>> 15)) * 0x2545f491;
        hh ^= (hh >>> 13);
        return (hh & 0xFFFFFF) / (float) 0x1000000;
    }

    // ================= 2. 物理 =================
    private void integrate(float dt) {
        final float k = 120f;
        final float damp = 9f;
        final float R2 = touchR * touchR;
        for (int i = 0; i < n; i++) {
            if (t < delay[i]) continue;
            float ax = (hx[i] - px[i]) * k;
            float ay = (hy[i] - py[i]) * k;
            if (touching) {
                float dx = px[i] - touchX;
                float dy = py[i] - touchY;
                float d2 = dx * dx + dy * dy;
                if (d2 < R2) {
                    float d = (float) Math.sqrt(d2) + 0.001f;
                    float f = 1f - d / touchR;
                    float push = f * f * 9000f;
                    ax += dx / d * push;
                    ay += dy / d * push;
                }
            }
            vx[i] += ax * dt;
            vy[i] += ay * dt;
            vx[i] -= vx[i] * damp * dt;
            vy[i] -= vy[i] * damp * dt;
            float sp2 = vx[i] * vx[i] + vy[i] * vy[i];
            if (sp2 > 9.0e6f) {
                float s = 3000f / (float) Math.sqrt(sp2);
                vx[i] *= s;
                vy[i] *= s;
            }
            px[i] += vx[i] * dt;
            py[i] += vy[i] * dt;
        }
    }

    private void explode(float cx, float cy) {
        for (int i = 0; i < n; i++) {
            float dx = px[i] - cx;
            float dy = py[i] - cy;
            float d = (float) Math.sqrt(dx * dx + dy * dy) + 1f;
            float f = 700f + hash01(i, 37) * 1300f;
            vx[i] += dx / d * f;
            vy[i] += dy / d * f;
        }
        invalidate();
    }

    private boolean settled() {
        if (touching) return false;
        if (t < 2.0f) return false;
        for (int i = 0; i < n; i++) {
            float dx = hx[i] - px[i];
            float dy = hy[i] - py[i];
            if (dx * dx + dy * dy > 1f) return false;
            if (vx[i] * vx[i] + vy[i] * vy[i] > 1f) return false;
        }
        return true;
    }

    // ================= 3. 绘制 =================
    @Override
    protected void onDraw(Canvas canvas) {
        long now = System.nanoTime();
        float dt = lastNs == 0L ? FIXED_DT : (now - lastNs) / 1e9f;
        lastNs = now;
        if (dt > 0.1f) dt = 0.1f;
        if (dt <= 0f) dt = FIXED_DT;
        t += dt;
        acc += dt;
        int guard = 0;
        while (acc >= FIXED_DT && guard++ < 16) {
            integrate(FIXED_DT);
            acc -= FIXED_DT;
        }
        if (acc > FIXED_DT) acc = 0f;

        drawDots(canvas);
        if (!settled()) postInvalidateOnAnimation();
    }

    private void drawDots(Canvas canvas) {
        bucketCnt[0] = 0;
        bucketCnt[1] = 0;
        bucketCnt[2] = 0;
        final float th0 = baseR * 0.78f;
        final float th1 = baseR * 1.02f;
        for (int i = 0; i < n; i++) {
            if (t < delay[i]) continue;
            int b = rad[i] < th0 ? 0 : (rad[i] < th1 ? 1 : 2);
            int c = bucketCnt[b];
            float[] arr = bucketPts[b];
            if (c * 2 + 1 >= arr.length) continue;
            arr[c * 2] = px[i];
            arr[c * 2 + 1] = py[i];
            bucketCnt[b] = c + 1;
        }
        for (int b = 0; b < BUCKETS; b++) {
            int c = bucketCnt[b];
            if (c == 0) continue;
            dotPaint.setStrokeWidth(bucketStroke[b]);
            dotPaint.setColor(bucketColor[b]);
            canvas.drawPoints(bucketPts[b], 0, c * 2, dotPaint);
        }
    }

    // ================= 4. 交互（点击炸开，拖动让给父容器滚动） =================
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touching = true;
                touchX = e.getX();
                touchY = e.getY();
                downX = touchX;
                downY = touchY;
                downNs = System.nanoTime();
                lastNs = 0L;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(e.getX() - downX) > mTouchSlop || Math.abs(e.getY() - downY) > mTouchSlop) {
                    // 判定为滑动：把事件让回 ScrollView，页面照常滚动
                    touching = false;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                } else {
                    touchX = e.getX();
                    touchY = e.getY();
                }
                return true;
            case MotionEvent.ACTION_UP:
                touching = false;
                float dx = e.getX() - downX;
                float dy = e.getY() - downY;
                long ms = (System.nanoTime() - downNs) / 1000000L;
                if (ms < 400 && dx * dx + dy * dy < 60f * mDensity * 60f * mDensity) {
                    explode(e.getX(), e.getY());
                }
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                return true;
            case MotionEvent.ACTION_CANCEL:
                touching = false;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                return true;
            default:
                return true;
        }
    }
}
