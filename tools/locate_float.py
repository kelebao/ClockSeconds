#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Locate the float window's dark plate bbox in a screenshot.

The float window is a dark rounded rectangle near the top of the screen. Over a warm
wallpaper, its pixels are much darker than the background, so thresholding on low
luma in the top region isolates the plate. We report the bbox of the largest cluster
of dark pixels so we can crop to it.
"""
import sys
from PIL import Image

path = sys.argv[1]
img = Image.open(path).convert('RGB')
W, H = img.size
px = img.load()

# Only look in the top 30% of the screen (where the float window lives).
top = int(H * 0.30)
minx, miny, maxx, maxy, count = W, top, 0, 0, 0
pts = []
for y in range(0, top):
    for x in range(0, W):
        r, g, b = px[x, y]
        luma = 0.299 * r + 0.587 * g + 0.114 * b
        if luma < 80:  # very dark -> float plate
            pts.append((x, y))
if pts:
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    minx, maxx = min(xs), max(xs)
    miny, maxy = min(ys), max(ys)
    count = len(pts)
print('image=%dx%d  dark_px=%d  bbox=(%d,%d,%d,%d) w=%d h=%d'
      % (W, H, count, minx, miny, maxx, maxy, maxx - minx, maxy - miny))