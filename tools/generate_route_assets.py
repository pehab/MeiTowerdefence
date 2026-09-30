"""Deterministically generate small painted route and projectile sprites (Pillow)."""
from pathlib import Path
import math
import random
import numpy as np
from PIL import Image, ImageDraw, ImageFilter

OUT = Path(__file__).resolve().parents[1] / 'app/src/main/res/drawable-nodpi'
OUT.mkdir(parents=True, exist_ok=True)
rng = random.Random(1947)

# A seamless-ish patch of ochre earth with soft pigment shifts, flecks and stones.
size = 256
noise = np.random.default_rng(1947)
a = noise.normal(0, 1, (size, size))
for scale, strength in [(12, 10), (36, 8), (84, 4)]:
    small = noise.normal(0, 1, (scale, scale))
    layer = Image.fromarray(np.uint8(np.clip(small * 38 + 128, 0, 255))).resize((size, size), Image.Resampling.BICUBIC)
    a += (np.asarray(layer, dtype=float) - 128) * strength / 38
base = np.empty((size, size, 3), np.uint8)
for channel, value in enumerate((178, 139, 87)):
    base[:, :, channel] = np.clip(value + a * (1.0 if channel == 0 else .88), 0, 255)
texture = Image.fromarray(base, 'RGB').convert('RGBA')
brush = ImageDraw.Draw(texture, 'RGBA')
for _ in range(850):
    x = rng.randrange(size)
    y = rng.randrange(size)
    radius = rng.choice([1, 1, 2, 3, 4])
    color = rng.choice([(80, 67, 43, 65), (242, 204, 138, 70), (73, 91, 52, 43), (221, 184, 113, 90)])
    brush.ellipse((x-radius, y-radius/2, x+radius, y+radius/2), fill=color)
for _ in range(55):
    x, y = rng.randrange(size), rng.randrange(size)
    brush.ellipse((x-2, y-1, x+2, y+1), fill=(230, 213, 175, 100))
    brush.arc((x-2, y-2, x+3, y+2), 0, 150, fill=(72, 66, 49, 90), width=1)
texture.convert('RGB').save(OUT / 'dirt_path_texture.webp', 'WEBP', quality=90)

# Transparent atlas cells: arrow, iron cannonball, flame, crystal shard.
S = 256
atlas = Image.new('RGBA', (S*4, S), (0,0,0,0))
def cell(i):
    im = Image.new('RGBA', (S, S), (0,0,0,0))
    return im, ImageDraw.Draw(im, 'RGBA')
def place(i, im):
    atlas.alpha_composite(im, (i*S, 0))

im,d=cell(0)
# Shaft points to the right, with dark outline and a warm metal tip.
d.polygon([(36,116),(174,116),(214,128),(174,140),(36,140)], fill=(45,43,37,255))
d.polygon([(42,121),(177,121),(205,128),(177,135),(42,135)], fill=(199,157,88,255))
d.line([(56,125),(181,125)], fill=(255,223,151,180), width=4)
d.polygon([(154,128),(188,92),(234,128),(188,164)], fill=(43,48,45,255))
d.polygon([(164,128),(190,99),(226,128),(190,157)], fill=(187,193,176,255))
d.polygon([(189,103),(226,128),(190,128)], fill=(246,234,189,235))
d.polygon([(44,128),(21,101),(98,123),(98,133),(21,155)], fill=(47,65,55,255))
d.polygon([(48,124),(29,111),(90,124),(90,128)], fill=(171,199,145,245))
d.polygon([(48,132),(29,145),(90,132),(90,128)], fill=(113,148,109,245))
place(0,im)

im,d=cell(1)
for rad, color in [(74,(206,162,77,34)),(65,(67,52,38,130)),(59,(30,32,36,255)),(49,(78,79,78,255))]:
    d.ellipse((128-rad,128-rad,128+rad,128+rad), fill=color)
d.arc((78,78,181,178), 205, 340, fill=(195,181,149,255), width=9)
d.ellipse((94,84,124,106), fill=(201,193,166,165))
d.arc((77,75,186,189), 15, 95, fill=(18,22,25,235), width=8)
place(1,im)

im,d=cell(2)
# Ember trailing to the left, with luminous layered core.
d.polygon([(23,129),(89,79),(75,115),(109,87),(98,142),(163,100),(217,122),(172,174),(86,163)], fill=(121,38,22,155))
d.polygon([(42,131),(104,99),(94,125),(132,105),(202,128),(145,161),(84,151)], fill=(238,91,24,240))
d.ellipse((101,91,211,164), fill=(253,169,42,230))
d.ellipse((133,102,198,151), fill=(255,229,116,250))
d.ellipse((155,108,189,139), fill=(255,247,198,255))
place(2,im)

im,d=cell(3)
d.polygon([(20,128),(72,115),(142,88),(237,128),(142,168),(72,141)], fill=(37,86,112,235))
d.polygon([(27,128),(146,96),(222,128),(146,128)], fill=(216,247,249,255))
d.polygon([(27,128),(146,159),(222,128),(146,128)], fill=(100,186,216,255))
d.polygon([(142,96),(222,128),(146,128)], fill=(249,255,248,255))
d.line([(72,127),(217,127)], fill=(255,255,255,215), width=4)
place(3,im)

atlas = atlas.resize((4*128,128), Image.Resampling.LANCZOS)
atlas.save(OUT / 'projectile_atlas.webp', 'WEBP', quality=95, method=6)
