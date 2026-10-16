"""
为 ColorOSClockSeconds 模块生成应用图标。

输入:
  - clipboard-2026-10-16T09-28-47-147Z-4665d148.jpg  (用户提供的二次元头像 JPG)

输出:
  - mipmap-<dpi>/ic_launcher.png         (方形图标，<= Android 7 fallback / LSPosed 图标)
  - mipmap-<dpi>/ic_launcher_round.png   (圆形图标，同上)
  - mipmap-xxxhdpi/ic_launcher_foreground.png (自适应图标前景源)
  - 用于自适应图标 mipmap-anydpi-v26 引用

色彩:
  - 背景色与头发蓝色呼应，Material Blue 600 = #1E88E5
  - Adaptive icon 的背景在 XML 里写颜色，前景靠 inset 控制安全区
"""

import os
from PIL import Image, ImageDraw

# -------- 路径 --------
SRC = r"C:\Users\HSG\.workbuddy-ai\clipboard-images\clipboard-2026-10-16T09-28-47-147Z-4665d148.jpg"
RES = r"C:\Users\HSG\Documents\AIKF\ColorOS15ClockSeconds\app\src\main\res"

# 各 dpi 标准 launcher 尺寸
DPI_SIZES = {
    "mdpi":    48,
    "hdpi":    72,
    "xhdpi":   96,
    "xxhdpi":  144,
    "xxxhdpi": 192,
}

# 自适应图标前景画布总尺寸（用于 inset 引用；72dp 安全区外会被部分 launcher mask 裁掉）
FG_CANVAS = {
    "mdpi":    108,    # 108dp 总图，72dp 安全区（用 dp = px 简化）
    "hdpi":    162,
    "xhdpi":   216,
    "xxhdpi":  324,
    "xxxhdpi": 432,
}

# 圆形 round 图标的方形画布大小（>= 边长），这里与 DPI_SIZES 一致
ROUND_SIZES = DPI_SIZES

# 背景色：与头发蓝色呼应的浅蓝，避免头像白边抢镜；同时避免太深
BG_COLOR = (30, 136, 229, 255)   # #1E88E5 Material Blue 600
BG_COLOR_LIGHT = (227, 242, 253, 255)  # #E3F2FD Light Blue 50


def open_source():
    img = Image.open(SRC).convert("RGBA")
    print(f"[src] {SRC} -> mode=RGBA size={img.size}")
    return img


def make_square_icon(src_img, size):
    """
    方形 launcher 图标:
      - 直接居中放头像（白底头像在浅蓝背景下反差适中，整体适配 launcher）
      - 若 launcher dpi 较大但 src 较小，先把 src 缩放到 size，再居中贴
    """
    canvas = Image.new("RGBA", (size, size), BG_COLOR)
    # 头像缩放到 size（保持比例，居中放置）。源是 1:1 方形，简化处理。
    fg = src_img.copy()
    fg.thumbnail((size, size), Image.LANCZOS)
    x = (size - fg.width) // 2
    y = (size - fg.height) // 2
    # 用 fg 自身的 alpha 作蒙版，避免白边透出
    canvas.paste(fg, (x, y), fg)
    return canvas


def make_round_icon(src_img, size):
    """
    圆形 launcher 图标:
      - 蓝色背景圆
      - 头像裁成圆，贴在中央
    """
    # 头像先裁成与画布同样大小的方形（缩放），再蒙成圆
    fg = src_img.copy()
    fg.thumbnail((size, size), Image.LANCZOS)
    # 圆形 alpha mask
    mask = Image.new("L", fg.size, 0)
    md = ImageDraw.Draw(mask)
    md.ellipse((0, 0, fg.width - 1, fg.height - 1), fill=255)

    # 圆底背景
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bgd = ImageDraw.Draw(bg)
    bgd.ellipse((0, 0, size - 1, size - 1), fill=BG_COLOR)
    canvas.paste(bg, (0, 0), bg)

    # 贴圆形头像居中
    cx = (size - fg.width) // 2
    cy = (size - fg.height) // 2
    canvas.paste(fg, (cx, cy), mask)
    return canvas


def make_adaptive_foreground(src_img, canvas_size):
    """
    自适应图标前景图（在 108dp 总图，72dp 安全区内放置头像）：
    - 画布 RGBA 透明
    - 头像居中，最大边 = 0.78 * canvas_size（保留约 11% 边距）
    """
    canvas = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    fg = src_img.copy()
    inner = int(canvas_size * 0.78)
    fg.thumbnail((inner, inner), Image.LANCZOS)
    x = (canvas_size - fg.width) // 2
    y = (canvas_size - fg.height) // 2
    canvas.paste(fg, (x, y), fg)
    return canvas


def main():
    src = open_source()
    for dpi, size in DPI_SIZES.items():
        out_dir = os.path.join(RES, f"mipmap-{dpi}")
        os.makedirs(out_dir, exist_ok=True)
        square = make_square_icon(src, size)
        square.save(os.path.join(out_dir, "ic_launcher.png"))
        rnd = make_round_icon(src, size)
        rnd.save(os.path.join(out_dir, "ic_launcher_round.png"))
        print(f"[{dpi}] square {size}x{size} + round saved")

    # adaptive icon 前景（每个 dpi 一份以便 anydpi-v26 之外的设备也能取到）
    for dpi, csize in FG_CANVAS.items():
        out_dir = os.path.join(RES, f"mipmap-{dpi}")
        os.makedirs(out_dir, exist_ok=True)
        fg = make_adaptive_foreground(src, csize)
        fg.save(os.path.join(out_dir, "ic_launcher_foreground.png"))
        print(f"[{dpi}] foreground {csize}x{csize} saved")


if __name__ == "__main__":
    main()
