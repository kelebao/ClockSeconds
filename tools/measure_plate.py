#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Measure redness inside the float window's background plate (crop region).

Over the orange wallpaper the plate composites to a dark brown. As the plate
"breathes" toward the hour colour it becomes strongly reddish (r >> g,b); at the
trough it stays neutral. So the count of strongly-reddish pixels (r>g+30, r>b+30)
inside the plate jumps at the breath peak and drops at the trough -> oscillation
proves the float background is breathing.
"""
import sys
from PIL import Image

x0, y0, x1, y1 = 236, 255, 987, 478  # located float plate bbox

for path in sys.argv[1:]:
    img = Image.open(path).convert('RGB')
    crop = img.crop((x0, y0, x1, y1))
    px = crop.load()
    cw, ch = crop.size
    strong = 0
    plate_dark = 0
    sr = sg = sb = 0
    for y in range(ch):
        for x in range(cw):
            r, g, b = px[x, y]
            if r > g + 30 and r > b + 30:
                strong += 1
            # plate background: mid-dark, excluding bright white text
            luma = 0.299 * r + 0.587 * g + 0.114 * b
            if luma < 140:
                plate_dark += 1
                sr += r
                sg += g
                sb += b
    avg = (sr // plate_dark, sg // plate_dark, sb // plate_dark) if plate_dark else (0, 0, 0)
    print('%-16s strong_reddish=%-7d dark_bg_avg=(%3d,%3d,%3d)' % (
        path.split('/')[-1].split('\\')[-1], strong, avg[0], avg[1], avg[2]))