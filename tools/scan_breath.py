#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Measure "reddish" pixels in a screenshot.

The breathing background plate tints toward the hour colour (default red), so during
the breath the panel becomes more reddish. Counting reddish pixels across shots taken
at different phases of the ~12s cycle shows whether the plate is animating.
"""
import sys
from PIL import Image

for path in sys.argv[1:]:
    img = Image.open(path).convert('RGB')
    px = img.load()
    W, H = img.size
    cnt = 0
    sr = sg = sb = 0
    # sample every 2nd pixel for speed
    for y in range(0, H, 2):
        for x in range(0, W, 2):
            r, g, b = px[x, y]
            if r > g + 15 and r > b + 15:
                cnt += 1
                sr += r
                sg += g
                sb += b
    if cnt:
        print('%-40s reddish_px=%-6d avg=(%3d,%3d,%3d)' % (
            path.split('/')[-1].split('\\')[-1], cnt, sr // cnt, sg // cnt, sb // cnt))
    else:
        print('%-40s reddish_px=0' % (path.split('/')[-1].split('\\')[-1]))
