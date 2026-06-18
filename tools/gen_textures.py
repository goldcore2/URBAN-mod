#!/usr/bin/env python3
"""Generates the 16x16 block textures for the Urban mod.

Run from the repo root:  python3 tools/gen_textures.py
"""
import os
import random
from PIL import Image, ImageDraw, ImageFont

OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources",
                   "assets", "urban", "textures", "block")
os.makedirs(OUT, exist_ok=True)

S = 16


def save(img, name):
    img.save(os.path.join(OUT, name + ".png"))


def noisy(base, jitter, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (S, S), (0, 0, 0, 255))
    px = img.load()
    for y in range(S):
        for x in range(S):
            j = rnd.randint(-jitter, jitter)
            px[x, y] = (max(0, min(255, base[0] + j)),
                        max(0, min(255, base[1] + j)),
                        max(0, min(255, base[2] + j)), 255)
    return img


def font(size):
    for path in ["/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
                 "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"]:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def centered_text(draw, text, fnt, fill, dy=0):
    bbox = draw.textbbox((0, 0), text, font=fnt)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    draw.text(((S - w) / 2 - bbox[0], (S - h) / 2 - bbox[1] + dy), text, font=fnt, fill=fill)


# --- road surfaces ---
save(noisy((45, 45, 48), 8, 1), "asphalt")
save(noisy((232, 232, 232), 10, 2), "road_marking_white")
save(noisy((226, 198, 40), 12, 3), "road_marking_yellow")

# sidewalk: light grey paving with darker joint lines
sw = noisy((150, 150, 152), 10, 4)
d = ImageDraw.Draw(sw)
for i in range(0, S, 8):
    d.line([(i, 0), (i, S)], fill=(110, 110, 112, 255))
    d.line([(0, i), (S, i)], fill=(110, 110, 112, 255))
save(sw, "sidewalk")

# crosswalk: zebra stripes on asphalt
cw = noisy((45, 45, 48), 8, 5)
d = ImageDraw.Draw(cw)
for x in range(0, S, 4):
    d.rectangle([x, 0, x + 1, S], fill=(235, 235, 235, 255))
save(cw, "crosswalk")

# sign post: galvanised steel
save(noisy((130, 132, 136), 14, 6), "post")


# --- signs (front panel, 16x16) ---
def blank(color=(255, 255, 255, 0)):
    return Image.new("RGBA", (S, S), color)


# STOP: red octagon
img = blank()
d = ImageDraw.Draw(img)
o = 3
d.polygon([(o, 0), (S - o, 0), (S - 1, o), (S - 1, S - o),
           (S - o, S - 1), (o, S - 1), (0, S - o), (0, o)], fill=(196, 30, 30, 255))
centered_text(d, "STOP", font(6), (255, 255, 255, 255))
save(img, "sign_stop")

# Vorfahrtsstrasse (priority road): white square rotated diamond, yellow centre, black border
img = blank()
d = ImageDraw.Draw(img)
d.polygon([(8, 0), (16, 8), (8, 16), (0, 8)], fill=(0, 0, 0, 255))
d.polygon([(8, 1), (15, 8), (8, 15), (1, 8)], fill=(255, 255, 255, 255))
d.polygon([(8, 4), (12, 8), (8, 12), (4, 8)], fill=(247, 209, 23, 255))
save(img, "sign_priority")

# Vorfahrt gewaehren (yield): inverted triangle, red border, white centre
img = blank()
d = ImageDraw.Draw(img)
d.polygon([(0, 1), (15, 1), (8, 15)], fill=(196, 30, 30, 255))
d.polygon([(3, 3), (12, 3), (8, 11)], fill=(255, 255, 255, 255))
save(img, "sign_yield")

# Speed limit 50: white circle, red ring
img = blank()
d = ImageDraw.Draw(img)
d.ellipse([0, 0, 15, 15], fill=(196, 30, 30, 255))
d.ellipse([3, 3, 12, 12], fill=(255, 255, 255, 255))
centered_text(d, "50", font(8), (20, 20, 20, 255))
save(img, "sign_speed_50")

# Autobahn shield: blue with white motorway symbol
img = blank((0, 86, 163, 255))
d = ImageDraw.Draw(img)
d.line([(3, 13), (7, 3)], fill=(255, 255, 255, 255), width=2)
d.line([(12, 13), (8, 3)], fill=(255, 255, 255, 255), width=2)
d.line([(2, 13), (13, 13)], fill=(255, 255, 255, 255), width=1)
save(img, "sign_autobahn")

# Ortstafel (city limit): yellow rectangle, black border
img = blank()
d = ImageDraw.Draw(img)
d.rectangle([0, 3, 15, 12], fill=(0, 0, 0, 255))
d.rectangle([1, 4, 14, 11], fill=(247, 209, 23, 255))
centered_text(d, "STADT", font(5), (20, 20, 20, 255))
save(img, "sign_city_limit")

# Ausfahrt (exit): blue with white arrow
img = blank((0, 86, 163, 255))
d = ImageDraw.Draw(img)
centered_text(d, "AUS", font(5), (255, 255, 255, 255), dy=-4)
d.line([(3, 11), (12, 11)], fill=(255, 255, 255, 255), width=1)
d.polygon([(12, 8), (15, 11), (12, 14)], fill=(255, 255, 255, 255))
save(img, "sign_exit")

print("textures written to", os.path.abspath(OUT))
