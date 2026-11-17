package io.github.kelebao.colorosclockseconds;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;

/**
 * 渐变 shader 统一工厂（U-20261118①：方向精简 + 特效方向扩展）。
 *
 * <p>状态栏（GradientTextSpan）、悬浮窗（ClockSecondsModule.FloatTimeView）、
 * 设置页预览钟（SettingsPreviewClock）与预设色条（AdvancedSettingsActivity.presetBarView）
 * 四条渲染路径共用这一份实现，保证「预览即所得」。</p>
 *
 * <p>方向语义：0..9 与旧版完全一致（0 水平 / 1 竖直 / 2 对角 / 3..7 斜向 / 8 径向 / 9 扫掠）；
 * 10..18 为 U-20261118 特效方向（11 象限光晕 Mesh / 16 颗粒噪点 Grain 保留；其余已收编，
 * 旧 dir 值回落普通二色线性渐变）；
 * 19 镀铬金属 Chrome / 20 斜纹糖果 Stripes / 21 波浪切割 Wave-Slice 为 U-20261118③ 新形式方向；
 * 22 低多边形晶格 Low-Poly 为 U-20261118④ 新形式方向（按现有位图 Shader 模式：位图按 band×height
 * 生成并按颜色+尺寸缓存，网格密度随位图尺寸自适应，不写死行列数）。</p>
 *
 * <p>特效方向 = 横向二色线性底 + 叠加层（ComposeShader：ADD 提亮 / SRC_OVER 覆盖），
 * 叠加层颜色全部由用户两端色派生，不引入固定配色。整个构建过程 try/catch 兜死，
 * 失败回落普通二色线性渐变——绘制期抛异常会直接崩掉宿主进程（状态栏 = SystemUI）。</p>
 */
public final class GradientShaderFactory {

    private GradientShaderFactory() {
    }

    /** 兜底默认色带（与 GradientTextSpan / 模块侧一致）。 */
    private static final int[] DEFAULT_STOPS = {0xFFEAE6F8, 0xFF7C5CE0};

    /** 颗粒噪点贴图（惰性单例，固定种子保证每次重建噪声图样一致）。 */
    private static volatile Bitmap sNoise;

    /**
     * 构建渐变 shader。
     *
     * @param colors 色标颜色（沿渐变轴顺序；0..9 用全部色标，10..18 只取首尾两色）
     * @param dir    ClockSecondsConfig.GRADIENT_DIRECTION_*（0..24）
     * @param x0     色带左边缘 x
     * @param band   色带宽度（文字实测宽 / 预览矩形容）
     * @param top    行盒顶部 y（绘制坐标系原点由调用方决定）
     * @param bottom 行盒底部 y
     */
    public static Shader build(int[] colors, int dir, float x0, float band,
                               float top, float bottom) {
        try {
            final int[] cols = colors != null && colors.length >= 2 ? colors : DEFAULT_STOPS;
            final int c0 = cols[0];
            final int c1 = cols[cols.length - 1];
            final float height = Math.max(1f, bottom - top);
            final float cy = top + height / 2f;
            final float cx = x0 + band / 2f;
            if (dir >= ClockSecondsConfig.GRADIENT_DIRECTION_MESH_RADIAL) {
                return buildEffect(cols, dir, x0, band, top, bottom, height, cy, cx, c0, c1);
            }
            final float[] positions = cols.length > 2 ? uniformPositions(cols.length) : null;
            switch (dir) {
                case ClockSecondsConfig.GRADIENT_DIRECTION_RADIAL: {
                    float radius = Math.max(band, height) / 2f;
                    return new RadialGradient(cx, cy, radius,
                            cols, positions, Shader.TileMode.CLAMP);
                }
                case ClockSecondsConfig.GRADIENT_DIRECTION_SWEEP:
                    return new SweepGradient(cx, cy, cols, positions);
                case ClockSecondsConfig.GRADIENT_DIRECTION_VERTICAL:      // 上 → 下
                    return new LinearGradient(x0, top, x0, bottom,
                            cols, positions, Shader.TileMode.CLAMP);
                case ClockSecondsConfig.GRADIENT_DIRECTION_DIAGONAL:      // 左上 → 右下
                    return new LinearGradient(x0, top, x0 + band, bottom,
                            cols, positions, Shader.TileMode.CLAMP);
                case ClockSecondsConfig.GRADIENT_DIRECTION_RTL:           // 右 → 左
                    return new LinearGradient(x0 + band, top, x0, top,
                            cols, positions, Shader.TileMode.CLAMP);
                case ClockSecondsConfig.GRADIENT_DIRECTION_BTT:           // 下 → 上
                    return new LinearGradient(x0, bottom, x0, top,
                            cols, positions, Shader.TileMode.CLAMP);
                case ClockSecondsConfig.GRADIENT_DIRECTION_BRTL:          // 右下 → 左上
                    return new LinearGradient(x0 + band, bottom, x0, top,
                            cols, positions, Shader.TileMode.CLAMP);
                case ClockSecondsConfig.GRADIENT_DIRECTION_TRBL:          // 右上 → 左下
                    return new LinearGradient(x0 + band, top, x0, bottom,
                            cols, positions, Shader.TileMode.CLAMP);
                case ClockSecondsConfig.GRADIENT_DIRECTION_BLTR:          // 左下 → 右上
                    return new LinearGradient(x0, bottom, x0 + band, top,
                            cols, positions, Shader.TileMode.CLAMP);
                default:                                                  // 0 水平（左 → 右）
                    return new LinearGradient(x0, top, x0 + band, top,
                            cols, positions, Shader.TileMode.CLAMP);
            }
        } catch (Throwable t) {
            // 任何异常都不得带崩宿主：回落普通二色线性渐变。
            try {
                final int[] cols = colors != null && colors.length >= 2 ? colors : DEFAULT_STOPS;
                return new LinearGradient(x0, top, x0 + band, top,
                        cols[0], cols[cols.length - 1], Shader.TileMode.CLAMP);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }

    // ---------------- 特效方向（U-20261118②：仅保留 象限光晕/颗粒噪点，其余已删，旧 dir 值回落二色线性） ----------------

    private static Shader buildEffect(int[] cols, int dir, float x0, float band,
                                      float top, float bottom, float height,
                                      float cy, float cx, int c0, int c1) {
        final Shader base = baseLinear(c0, c1, x0, band, top, bottom);
        switch (dir) {
            case ClockSecondsConfig.GRADIENT_DIRECTION_MESH_QUADRANT: {
                // 11 象限光晕 Mesh：线性底 + 左上/右下两团对角光晕（ADD）
                final int gA = towardWhite(c0, 0.35f);
                final int gB = towardWhite(c1, 0.35f);
                final float r = Math.max(band, height) * 0.55f;
                final Shader g1 = new RadialGradient(x0 + band * 0.24f, top + height * 0.28f, r,
                        new int[]{withAlpha(gA, 0xB4), withAlpha(gA, 0x00)},
                        null, Shader.TileMode.CLAMP);
                final Shader g2 = new RadialGradient(x0 + band * 0.76f, top + height * 0.72f, r,
                        new int[]{withAlpha(gB, 0xB4), withAlpha(gB, 0x00)},
                        null, Shader.TileMode.CLAMP);
                final Shader glows = new ComposeShader(g1, g2, PorterDuff.Mode.ADD);
                return new ComposeShader(base, glows, PorterDuff.Mode.ADD);
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_GRAIN: {
                // 16 颗粒噪点：线性底 + 细噪点贴图（SRC_OVER，固定种子静态噪声）
                final Shader grain = new BitmapShader(noise(),
                        Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                return new ComposeShader(base, grain, PorterDuff.Mode.SRC_OVER);
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_CHROME: {
                // 19 镀铬金属：垂直 7 段反光带保留，色阶全部由用户两端色派生（跟随渐变色）
                // 顺序：暗起点 → 起点 → 亮起点 → 白热高光(两色混合) → 亮终点 → 终点 → 暗终点
                final int[] ramp = {
                        darken(c0, 0.82f), darken(c0, 0.45f), towardWhite(c0, 0.38f),
                        towardWhite(mix(c0, c1, 0.5f), 0.88f),
                        towardWhite(c1, 0.32f), darken(c1, 0.45f), darken(c1, 0.82f)
                };
                return new LinearGradient(x0, top, x0, bottom,
                        ramp, CHROME_STOPS, Shader.TileMode.CLAMP);
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_STRIPES: {
                // 20 斜纹糖果：45° 四色硬界循环条纹（用户两色各派生一个亮伴色），REPEAT 平铺
                final float period = Math.max(8f, height * 0.5f);
                final int s1 = towardWhite(c0, 0.5f);
                final int s3 = towardWhite(c1, 0.5f);
                return new LinearGradient(x0, top, x0 + period, top + period,
                        new int[]{c0, c0, s1, s1, c1, c1, s3, s3},
                        new float[]{0f, 0.25f, 0.25f, 0.5f, 0.5f, 0.75f, 0.75f, 1f},
                        Shader.TileMode.REPEAT);
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_WAVE_SLICE: {
                // 21 波浪切割：正弦波浪线把色带切成上下两半，各填一套横向渐变（预渲染位图 + BitmapShader）
                final int w = Math.max(2, (int) Math.ceil(band));
                final int h = Math.max(2, (int) Math.ceil(height));
                final Bitmap bmp = waveBitmap(c0, c1, w, h);
                final BitmapShader bs = new BitmapShader(bmp,
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                final Matrix lm = new Matrix();
                lm.postTranslate(x0, top);
                bs.setLocalMatrix(lm);
                return bs;
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_LOW_POLY: {
                // 22 低多边形晶格：按位图尺寸自适应的不规则三角网格，逐面独立明暗
                // （与波浪切割同一套缓存 + BitmapShader + postTranslate 定位，不写死行列数）
                final int w = Math.max(2, (int) Math.ceil(band));
                final int h = Math.max(2, (int) Math.ceil(height));
                final Bitmap bmp = lowPolyBitmap(c0, c1, w, h);
                final BitmapShader bs = new BitmapShader(bmp,
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                final Matrix lm = new Matrix();
                lm.postTranslate(x0, top);
                bs.setLocalMatrix(lm);
                return bs;
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_TERRAZZO: {
                // 23 水磨石：底色渐变 + 不规则碎石片密铺（固定种子、细描边）
                final int w = Math.max(2, (int) Math.ceil(band));
                final int h = Math.max(2, (int) Math.ceil(height));
                final Bitmap bmp = terrazzoBitmap(c0, c1, w, h);
                final BitmapShader bs = new BitmapShader(bmp,
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                final Matrix lm = new Matrix();
                lm.postTranslate(x0, top);
                bs.setLocalMatrix(lm);
                return bs;
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_QR_CODE: {
                // 24 二维码：方块矩阵 + 三个定位角（固定种子伪随机，颜色取用户两端色）
                final int w = Math.max(2, (int) Math.ceil(band));
                final int h = Math.max(2, (int) Math.ceil(height));
                final Bitmap bmp = qrBitmap(c0, c1, w, h);
                final BitmapShader bs = new BitmapShader(bmp,
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                final Matrix lm = new Matrix();
                lm.postTranslate(x0, top);
                bs.setLocalMatrix(lm);
                return bs;
            }
            case ClockSecondsConfig.GRADIENT_DIRECTION_MERCURY: {
                // 25 液态水银：用户两端色派生铬色带 + 分形噪声大位移（与波浪切割同一套位图管线）
                final int w = Math.max(2, (int) Math.ceil(band));
                final int h = Math.max(2, (int) Math.ceil(height));
                final Bitmap bmp = mercuryBitmap(c0, c1, w, h);
                final BitmapShader bs = new BitmapShader(bmp,
                        Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                final Matrix lm = new Matrix();
                lm.postTranslate(x0, top);
                bs.setLocalMatrix(lm);
                return bs;
            }
            default: {
                // 已收编的旧特效 dir（10/12..15/17/18）：回落普通二色线性渐变
                return base;
            }
        }
    }

    /** 特效方向的横向二色线性底。 */
    private static LinearGradient baseLinear(int c0, int c1, float x0, float band,
                                             float top, float bottom) {
        return new LinearGradient(x0, top, x0 + band, top, c0, c1, Shader.TileMode.CLAMP);
    }

    /** 颗粒噪点贴图：白/黑细噪点、低不透明度，96x96 平铺。 */
    private static Bitmap noise() {
        Bitmap b = sNoise;
        if (b == null) {
            synchronized (GradientShaderFactory.class) {
                b = sNoise;
                if (b == null) {
                    b = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
                    final java.util.Random rnd = new java.util.Random(0xC10C5EEDL);
                    for (int y = 0; y < 96; y++) {
                        for (int x = 0; x < 96; x++) {
                            final int v = rnd.nextInt(256);
                            final int a = 0x24 + (v >> 4);            // 0x24..0x33
                            final int rgb = (v & 1) == 0 ? 0x00FFFFFF : 0x00000000;
                            b.setPixel(x, y, (a << 24) | rgb);
                        }
                    }
                    sNoise = b;
                }
            }
        }
        return b;
    }

    // ---------------- 新形式方向（U-20261118③：镀铬金属 / 斜纹糖果 / 波浪切割） ----------------

    private static final float[] CHROME_STOPS = {0f, 0.22f, 0.45f, 0.5f, 0.56f, 0.78f, 1f};

    /** 波浪切割位图缓存（key=两色+尺寸；小位图，最多留 6 张）。 */
    private static final java.util.LinkedHashMap<String, Bitmap> sWaveCache =
            new java.util.LinkedHashMap<String, Bitmap>(8, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Bitmap> eldest) {
                    return size() > 6;
                }
            };

    /** 波浪切割位图：上半 c0→亮伴色，下半 c1→亮伴色，正弦波浪分界。 */
    private static Bitmap waveBitmap(int c0, int c1, int w, int h) {
        final String key = c0 + "|" + c1 + "|" + w + "x" + h;
        synchronized (sWaveCache) {
            final Bitmap hit = sWaveCache.get(key);
            if (hit != null && !hit.isRecycled()) return hit;
        }
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(b);
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final float amp = h * 0.18f;
        final float wl = Math.max(w * 0.5f, 24f);
        final int steps = 36;
        final Path top = new Path();
        top.moveTo(0, 0);
        top.lineTo(w, 0);
        top.lineTo(w, waveY(w, h, amp, wl));
        for (int i = steps - 1; i >= 0; i--) {
            final float x = w * i / (float) steps;
            top.lineTo(x, waveY(x, h, amp, wl));
        }
        top.close();
        final Path bottom = new Path();
        bottom.moveTo(0, h);
        bottom.lineTo(w, h);
        bottom.lineTo(w, waveY(w, h, amp, wl));
        for (int i = steps - 1; i >= 0; i--) {
            final float x = w * i / (float) steps;
            bottom.lineTo(x, waveY(x, h, amp, wl));
        }
        bottom.close();
        p.setShader(new LinearGradient(0, 0, w, 0, c0, towardWhite(c0, 0.55f),
                Shader.TileMode.CLAMP));
        cv.drawPath(top, p);
        p.setShader(new LinearGradient(0, 0, w, 0, c1, towardWhite(c1, 0.55f),
                Shader.TileMode.CLAMP));
        cv.drawPath(bottom, p);
        synchronized (sWaveCache) {
            sWaveCache.put(key, b);
        }
        return b;
    }

    private static float waveY(float x, float h, float amp, float wl) {
        return h * 0.5f + amp * (float) Math.sin(x * (Math.PI * 2.0) / wl);
    }

    /** 低多边形位图缓存（key=两色+尺寸；与波浪切割共用上限策略，最多留 6 张）。 */
    private static final java.util.LinkedHashMap<String, Bitmap> sLowPolyCache =
            new java.util.LinkedHashMap<String, Bitmap>(8, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Bitmap> eldest) {
                    return size() > 6;
                }
            };

    /** 低多边形晶格位图：按位图尺寸自适应三角网格 + 双线性取色 + 逐面固定明暗扰动。 */
    private static Bitmap lowPolyBitmap(int c0, int c1, int w, int h) {
        final String key = c0 + "|" + c1 + "|" + w + "x" + h;
        synchronized (sLowPolyCache) {
            final Bitmap hit = sLowPolyCache.get(key);
            if (hit != null && !hit.isRecycled()) return hit;
        }
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(b);
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        // 网格单元随位图高度自适应（小字更少切面、预览大钟更多切面），不再写死 14×4
        final float cell = Math.max(6f, h / 4f);
        final int cols = Math.max(1, (int) Math.ceil(w / cell));
        final int rows = Math.max(1, (int) Math.ceil(h / cell));
        final float cw = w / (float) cols;
        final float ch = h / (float) rows;
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                final float x0 = i * cw;
                final float y0 = j * ch;
                final float x1 = (i + 1) * cw;
                final float y1 = (j + 1) * ch;
                // 不规则内点：边点保持原位以免位图边缘漏像素
                final float jx = (i == 0 || i == cols - 1) ? x0 : x0 + cw * (0.35f + 0.3f * facetNoise(i, j, 0));
                final float jy = (j == 0 || j == rows - 1) ? y0 : y0 + ch * (0.35f + 0.3f * facetNoise(i, j, 1));
                // 每个三角面独立明暗（固定种子，±7），颜色取面心在 c0→c1 的双线性位置
                drawFacet(cv, p, x0, y0, x1, y0, jx, jy, c0, c1, w, h, facetNoise(i, j, 2));
                drawFacet(cv, p, x0, y0, jx, jy, x0, y1, c0, c1, w, h, facetNoise(i, j, 3));
                drawFacet(cv, p, x1, y0, x1, y1, jx, jy, c0, c1, w, h, facetNoise(i, j, 4));
                drawFacet(cv, p, x0, y1, jx, jy, x1, y1, c0, c1, w, h, facetNoise(i, j, 5));
            }
        }
        synchronized (sLowPolyCache) {
            sLowPolyCache.put(key, b);
        }
        return b;
    }

    /** 绘制单个三角面：面心双线性取色 + 逐面固定 ±14 明暗扰动 + 细边线（U-20261118⑥ 提升小字可见度）。 */
    private static void drawFacet(Canvas cv, Paint p,
                                  float ax, float ay, float bx, float by, float cx, float cy,
                                  int c0, int c1, int w, int h, float noise) {
        final float fx = (ax + bx + cx) / 3f;
        final float fy = (ay + by + cy) / 3f;
        final int base = mix(c0, c1, fx / w);
        final int shifted = (int) (noise * 28f) - 14;
        final int r = Math.max(0, Math.min(255, Color.red(base) + shifted));
        final int g = Math.max(0, Math.min(255, Color.green(base) + shifted));
        final int bl = Math.max(0, Math.min(255, Color.blue(base) + shifted));
        p.setColor(Color.rgb(r, g, bl));
        final Path path = new Path();
        path.moveTo(ax, ay);
        path.lineTo(bx, by);
        path.lineTo(cx, cy);
        path.close();
        cv.drawPath(path, p);
        // 三角面细边线：让小字（状态栏约 40px 高）也能看出晶格切面，而不是一片糊色
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeWidth(0.7f);
        p.setColor(0x66000000);
        cv.drawPath(path, p);
        p.setStyle(Paint.Style.FILL);
    }

    /** 固定种子面扰动（0..1），不用 Random，保证同色同尺寸每次生成完全一致的晶格。 */
    private static float facetNoise(int i, int j, int salt) {
        int v = (i * 73856093) ^ (j * 19349663) ^ (salt * 83492791);
        v ^= (v >>> 13);
        v *= 1274126177;
        v ^= (v >>> 16);
        return (v & 0x7FFFFFFF) / (float) 0x7FFFFFFF;
    }

    /** 水磨石/二维码位图缓存（与波浪切割同策略：按颜色+尺寸缓存，最多 6 张）。 */
    private static final java.util.LinkedHashMap<String, Bitmap> sPatternCache =
            new java.util.LinkedHashMap<String, Bitmap>(8, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Bitmap> eldest) {
                    return size() > 6;
                }
            };

    /** 水磨石位图：双色渐变底 + 固定种子的彩色碎石片（多边形 + 细描边）。 */
    private static Bitmap terrazzoBitmap(int c0, int c1, int w, int h) {
        final String key = "T" + c0 + "|" + c1 + "|" + w + "x" + h;
        synchronized (sPatternCache) {
            final Bitmap hit = sPatternCache.get(key);
            if (hit != null && !hit.isRecycled()) return hit;
        }
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(b);
        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0, 0, w, 0, c0, c1, Shader.TileMode.CLAMP));
        cv.drawRect(0, 0, w, h, p);
        p.setShader(null);
        final int[] chips = {
                towardWhite(c0, 0.45f), darken(c1, 0.25f), towardWhite(c1, 0.4f),
                darken(c0, 0.35f), mix(c0, c1, 0.5f)
        };
        final float size = Math.max(4f, h * 0.22f);
        final int count = Math.max(12, Math.min(70, (int) (w * h / (size * size) * 0.6f)));
        for (int i = 0; i < count; i++) {
            final float x = facetNoise(i, 1, 0) * w;
            final float y = facetNoise(i, 2, 1) * h;
            final int sides = 3 + (int) (facetNoise(i, 3, 2) * 4f);
            final float r = size * (0.4f + 0.6f * facetNoise(i, 4, 3));
            final float ph = facetNoise(i, 5, 4) * 6.28f;
            final Path path = new Path();
            for (int k = 0; k < sides; k++) {
                final float a = ph + k * 6.2831853f / sides;
                final float px = x + r * (float) Math.cos(a);
                final float py = y + r * (float) Math.sin(a) * 0.8f;
                if (k == 0) path.moveTo(px, py); else path.lineTo(px, py);
            }
            path.close();
            p.setColor(chips[(int) (facetNoise(i, 6, 5) * chips.length) % chips.length]);
            cv.drawPath(path, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(0.6f);
            p.setColor(0x66000000);
            cv.drawPath(path, p);
            p.setStyle(Paint.Style.FILL);
        }
        synchronized (sPatternCache) {
            sPatternCache.put(key, b);
        }
        return b;
    }

    /** 二维码位图：伪随机方块矩阵 + 三个定位角（颜色全部由用户两端色派生）。 */
    private static Bitmap qrBitmap(int c0, int c1, int w, int h) {
        final String key = "Q" + c0 + "|" + c1 + "|" + w + "x" + h;
        synchronized (sPatternCache) {
            final Bitmap hit = sPatternCache.get(key);
            if (hit != null && !hit.isRecycled()) return hit;
        }
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        final Canvas cv = new Canvas(b);
        final Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        cv.drawColor(c0);
        p.setColor(towardWhite(c1, 0.12f));
        final int module = Math.max(3, (int) Math.ceil(h / 8f));
        final int cols = Math.max(1, (int) Math.ceil(w / module));
        final int rows = Math.max(1, (int) Math.ceil(h / module));
        final float cw = w / (float) cols;
        final float ch = h / (float) rows;
        final int finder = Math.min(7, rows);
        for (int j = 0; j < rows; j++) {
            for (int i = 0; i < cols; i++) {
                if (i < finder + 1 && j < finder + 1) continue;
                if (i >= cols - finder - 1 && j < finder + 1) continue;
                if (i < finder + 1 && j >= rows - finder - 1) continue;
                if (facetNoise(i, j, 7) > 0.52f) {
                    cv.drawRect(i * cw, j * ch, (i + 1) * cw, (j + 1) * ch, p);
                }
            }
        }
        // 三个定位角：外框 → 内空 → 中心块
        final int[][] corners = {{0, 0}, {cols - finder, 0}, {0, rows - finder}};
        for (int[] c : corners) {
            p.setColor(towardWhite(c1, 0.12f));
            cv.drawRect(c[0] * cw, c[1] * ch, (c[0] + finder) * cw, (c[1] + finder) * ch, p);
            p.setColor(c0);
            cv.drawRect((c[0] + 1) * cw, (c[1] + 1) * ch,
                    (c[0] + finder - 1) * cw, (c[1] + finder - 1) * ch, p);
            p.setColor(towardWhite(c1, 0.12f));
            cv.drawRect((c[0] + 2) * cw, (c[1] + 2) * ch,
                    (c[0] + finder - 2) * cw, (c[1] + finder - 2) * ch, p);
        }
        synchronized (sPatternCache) {
            sPatternCache.put(key, b);
        }
        return b;
    }

    /** 液态水银位图缓存（key=两色+尺寸；最多 6 张，LRU）。 */
    private static final java.util.LinkedHashMap<String, Bitmap> sMercuryCache =
            new java.util.LinkedHashMap<String, Bitmap>(8, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(java.util.Map.Entry<String, Bitmap> eldest) {
                    return size() > 6;
                }
            };

    /** 平滑噪声双线性插值（0..1，固定种子，保证同色同尺寸结果一致）。 */
    private static float smoothNoise(float x, float y, int salt) {
        final int xi = (int) Math.floor(x), yi = (int) Math.floor(y);
        final float fx = x - xi, fy = y - yi;
        final float ux = fx * fx * (3f - 2f * fx), uy = fy * fy * (3f - 2f * fy);
        final float a = facetNoise(xi, yi, salt), b = facetNoise(xi + 1, yi, salt);
        final float c = facetNoise(xi, yi + 1, salt), d = facetNoise(xi + 1, yi + 1, salt);
        return a + (b - a) * ux + (c - a) * uy + (a - b - c + d) * ux * uy;
    }

    /** 液态水银位图：用户两端色派生铬色带 + 分形噪声大位移 + 细高光。 */
    private static Bitmap mercuryBitmap(int c0, int c1, int w, int h) {
        final String key = c0 + "|" + c1 + "|" + w + "x" + h;
        synchronized (sMercuryCache) {
            final Bitmap hit = sMercuryCache.get(key);
            if (hit != null && !hit.isRecycled()) return hit;
        }
        final int mid = mix(c0, c1, 0.5f);
        final int[] pos = {0, 36, 77, 112, 133, 158, 194, 230, 255};
        final int[] ramp = {towardWhite(c0, 0.85f), darken(c0, 0.55f), towardWhite(c0, 0.28f),
                darken(mid, 0.45f), towardWhite(mid, 0.95f), towardWhite(c1, 0.28f),
                darken(c1, 0.55f), towardWhite(c1, 0.72f), darken(c1, 0.78f)};
        final int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            int k = 1;
            while (k < pos.length - 2 && i > pos[k]) k++;
            lut[i] = mix(ramp[k - 1], ramp[k], (i - pos[k - 1]) / (float) (pos[k] - pos[k - 1]));
        }
        final int[] px = new int[w * h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                final float n1 = smoothNoise(x * 0.018f, y * 0.045f, 17);
                final float n2 = smoothNoise(x * 0.07f, y * 0.11f, 31);
                final float yy = y + 22f * (n1 - 0.5f) + 7f * (n2 - 0.5f);
                int idx = (int) (yy * 255f / h);
                idx = idx < 0 ? 0 : (idx > 255 ? 255 : idx);
                final int c = lut[idx];
                final float sheen = (smoothNoise(x * 0.38f, y * 0.38f, 61) - 0.5f) * 26f;
                px[y * w + x] = 0xFF000000 | (clampByte(Color.red(c) + sheen) << 16)
                        | (clampByte(Color.green(c) + sheen) << 8) | clampByte(Color.blue(c) + sheen);
            }
        }
        final Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        b.setPixels(px, 0, w, 0, 0, w, h);
        synchronized (sMercuryCache) {
            sMercuryCache.put(key, b);
        }
        return b;
    }

    private static int clampByte(float v) {
        return v < 0f ? 0 : (v > 255f ? 255 : Math.round(v));
    }

    // ---------------- 颜色工具 ----------------

    private static int mix(int a, int b, float t) {
        final int r = Math.round(Color.red(a) + (Color.red(b) - Color.red(a)) * t);
        final int g = Math.round(Color.green(a) + (Color.green(b) - Color.green(a)) * t);
        final int bl = Math.round(Color.blue(a) + (Color.blue(b) - Color.blue(a)) * t);
        return Color.rgb(r, g, bl);
    }

    private static int towardWhite(int c, float t) {
        return mix(c, 0xFFFFFFFF, t);
    }

    private static int darken(int c, float t) {
        return mix(c, 0xFF000000, t);
    }

    private static int withAlpha(int c, int a) {
        return (a << 24) | (c & 0x00FFFFFF);
    }

    private static int chanR(int c) {
        return 0xFF000000 | (Color.red(c) << 16);
    }

    private static int chanG(int c) {
        return 0xFF000000 | (Color.green(c) << 8);
    }

    private static int chanB(int c) {
        return 0xFF000000 | Color.blue(c);
    }

    /** n 个渐变色标均匀分布的位置数组（0..1，首尾必在端点）。 */
    private static float[] uniformPositions(int n) {
        float[] p = new float[n];
        for (int i = 0; i < n; i++) {
            p[i] = n <= 1 ? 0f : (float) i / (float) (n - 1);
        }
        return p;
    }
}
