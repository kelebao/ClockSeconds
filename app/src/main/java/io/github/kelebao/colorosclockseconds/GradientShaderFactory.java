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
 * 19 镀铬金属 Chrome / 20 斜纹糖果 Stripes / 21 波浪切割 Wave-Slice 为 U-20261118③ 新形式方向。</p>
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
     * @param dir    ClockSecondsConfig.GRADIENT_DIRECTION_*（0..21）
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
                // 19 镀铬金属：垂直 7 段金属带（固定铬色序：暗-灰-亮-白热-亮-灰-暗，Y2K 铬字，不取用户配色）
                return new LinearGradient(x0, top, x0, bottom,
                        CHROME_RAMP, CHROME_STOPS, Shader.TileMode.CLAMP);
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

    /** 镀铬金属固定铬色序（垂直 7 段）。 */
    private static final int[] CHROME_RAMP = {
            0xFF14171F, 0xFF454E63, 0xFFA8D4FF, 0xFFF4F9FF,
            0xFF8FB6E8, 0xFF2C3448, 0xFF171B26
    };

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
