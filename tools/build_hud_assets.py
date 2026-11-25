"""Generates the HUD chrome: three nine-slice frames and one three-slice button sheet.

Run from the repository root:  python3 tools/build_hud_assets.py
Output goes to assets_master/assets/ui/ (staged into the app by Gradle).

Frames: 256x256, corner cap 96 px, bezel 36 px, transparent opening. Edges are perfectly uniform, so they can be stretched.
Button sheet: 384x512, four states stacked (normal, focused, pressed, disabled), each 384x128, cap 64 px each side.
"""
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, "assets_master", "assets", "ui")
SS = 4
N = 256
BEZEL = 36
CORNER_CHAMFER = 42


def octagon(w, h, inset, chamfer):
    i, c = inset, chamfer
    return [(i + c, i), (w - i - c, i), (w - i, i + c), (w - i, h - i - c), (w - i - c, h - i), (i + c, h - i), (i, h - i - c), (i, i + c)]


def lerp(a, b, t):
    return tuple(int(round(a[k] + (b[k] - a[k]) * t)) for k in range(3))


def bevel_color(d, accent_dark):
    """Colour of the bezel at distance d (in px) from the outer edge."""
    if d < 2:
        return (214, 224, 236)
    if d < 5:
        return lerp((170, 184, 200), (122, 136, 154), (d - 2) / 3)
    if d < BEZEL - 11:
        return lerp((112, 126, 144), (78, 90, 108), (d - 5) / (BEZEL - 16))
    if d < BEZEL - 4:
        return lerp((52, 60, 74), (34, 40, 52), (d - (BEZEL - 11)) / 7)
    return accent_dark


def premultiplied_resize(img, size):
    r, g, b, a = img.split()
    pm = Image.merge("RGBA", (
        Image.composite(r, Image.new("L", img.size, 0), a),
        Image.composite(g, Image.new("L", img.size, 0), a),
        Image.composite(b, Image.new("L", img.size, 0), a), a))
    small = pm.resize(size, Image.LANCZOS)
    sr, sg, sb, sa = small.split()
    out = []
    for ch in (sr, sg, sb):
        out.append(Image.eval(ch, lambda v: v))
    px = small.load()
    res = Image.new("RGBA", size)
    rp = res.load()
    for y in range(size[1]):
        for x in range(size[0]):
            rr, gg, bb, aa = px[x, y]
            if aa == 0:
                rp[x, y] = (0, 0, 0, 0)
            else:
                f = 255.0 / aa
                rp[x, y] = (min(255, int(rr * f)), min(255, int(gg * f)), min(255, int(bb * f)), aa)
    return res


def make_frame(accent, accent_dark, double_line=False, seed=1):
    W = N * SS
    img = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # bevel: paint octagons from the outside in, the chamfer shrinks inwards
    for k in range(BEZEL * SS):
        dist = k / SS
        chamfer = max(8, CORNER_CHAMFER - dist * 0.9)
        d.polygon(octagon(W, W, k, chamfer * SS), fill=bevel_color(dist, accent_dark) + (255,))
    # brushed-metal grain on the steel, uniform so the edges can stretch
    rnd = random.Random(seed)
    cap = 96 * SS
    row_noise = [rnd.randint(110, 146) for _ in range(W)]
    col_noise = [rnd.randint(110, 146) for _ in range(W)]
    grain = Image.new("L", (W, W), 128)
    gp = grain.load()
    for y in range(W):
        in_y_mid = cap <= y < W - cap
        for x in range(W):
            in_x_mid = cap <= x < W - cap
            if in_x_mid and not in_y_mid:      # top / bottom edge: identical along x, so it can stretch
                gp[x, y] = row_noise[y]
            elif in_y_mid and not in_x_mid:    # left / right edge: identical along y
                gp[x, y] = col_noise[x]
            else:                              # corners: free
                gp[x, y] = (row_noise[y] + col_noise[x]) // 2
    steel = Image.new("L", (W, W), 0)
    ImageDraw.Draw(steel).polygon(octagon(W, W, 6 * SS, (CORNER_CHAMFER - 5) * SS), fill=255)
    ImageDraw.Draw(steel).polygon(octagon(W, W, (BEZEL - 11) * SS, 14 * SS), fill=0)
    rgb = img.convert("RGB")
    shade = Image.merge("RGB", [grain] * 3)
    mixed = Image.blend(rgb, Image.eval(shade, lambda v: v), 0.0)
    over = Image.composite(Image.blend(rgb, Image.merge("RGB", [Image.eval(grain, lambda v: min(255, v + 20))] * 3), 0.10), rgb, steel)
    img = Image.merge("RGBA", (*over.split(), img.getchannel("A")))
    d = ImageDraw.Draw(img)
    # inner lip: accent line then the transparent opening
    lip = BEZEL
    d.polygon(octagon(W, W, lip * SS, 10 * SS), fill=accent + (255,))
    d.polygon(octagon(W, W, (lip + 2.2) * SS, 9 * SS), fill=(0, 0, 0, 0))
    if double_line:
        d.line(octagon(W, W, (lip + 7) * SS, 8 * SS) + [octagon(W, W, (lip + 7) * SS, 8 * SS)[0]], fill=accent + (90,), width=SS)
    # corner rivets
    for cx, cy in ((26, 26), (N - 26, 26), (26, N - 26), (N - 26, N - 26)):
        r = 6.5
        d.ellipse([(cx - r) * SS, (cy - r) * SS, (cx + r) * SS, (cy + r) * SS], fill=(28, 34, 44, 255))
        d.ellipse([(cx - r + 1.6) * SS, (cy - r + 1.6) * SS, (cx + r - 1.6) * SS, (cy + r - 1.6) * SS], fill=(92, 104, 120, 255))
        d.ellipse([(cx - 3.2) * SS, (cy - 4.2) * SS, (cx + 0.4) * SS, (cy - 1.0) * SS], fill=(196, 208, 222, 255))
    return premultiplied_resize(img, (N, N))


BTN_W, BTN_H, BTN_CAP = 384, 128, 64
STATES = [
    dict(border=(40, 215, 245), top=(10, 36, 48), bottom=(5, 20, 28), line=(70, 150, 170)),
    dict(border=(130, 245, 255), top=(18, 62, 78), bottom=(10, 38, 50), line=(120, 200, 215)),
    dict(border=(30, 150, 175), top=(4, 16, 22), bottom=(2, 9, 13), line=(40, 90, 105)),
    dict(border=(104, 60, 130), top=(42, 20, 56), bottom=(22, 10, 32), line=(80, 50, 100)),
]


def make_button(state):
    W, H = BTN_W * SS, BTN_H * SS
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon(octagon(W, H, 3 * SS, 26 * SS), fill=state["border"] + (255,))
    inner = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    mask = Image.new("L", (W, H), 0)
    ImageDraw.Draw(mask).polygon(octagon(W, H, 11 * SS, 20 * SS), fill=255)
    grad = Image.new("RGBA", (W, H))
    gd = ImageDraw.Draw(grad)
    for y in range(H):
        gd.line([(0, y), (W, y)], fill=lerp(state["top"], state["bottom"], y / H) + (255,))
    img.paste(grad, (0, 0), mask)
    d = ImageDraw.Draw(img)
    # a thin highlight under the top edge (uniform, stretches cleanly)
    d.line([(36 * SS, 17 * SS), (W - 36 * SS, 17 * SS)], fill=state["line"] + (150,), width=SS)
    # small details live inside the caps only
    d.ellipse([20 * SS, (BTN_H / 2 - 4) * SS, 28 * SS, (BTN_H / 2 + 4) * SS], fill=state["line"] + (255,))
    d.line([(BTN_W * SS - 54 * SS, 108 * SS), (BTN_W * SS - 22 * SS, 108 * SS)], fill=state["line"] + (255,), width=2 * SS)
    return premultiplied_resize(img, (BTN_W, BTN_H))


def main():
    os.makedirs(OUT, exist_ok=True)
    frames = {
        "hud_frame_terminal": make_frame((60, 230, 255), (18, 70, 84), seed=1),
        "hud_frame_classic": make_frame((80, 240, 255), (14, 60, 74), double_line=True, seed=2),
        "hud_frame_editor": make_frame((124, 255, 160), (20, 74, 50), seed=3),
    }
    for name, image in frames.items():
        image.save(os.path.join(OUT, name + ".webp"), lossless=True, method=6, exact=True)
    sheet = Image.new("RGBA", (BTN_W, BTN_H * 4), (0, 0, 0, 0))
    for index, state in enumerate(STATES):
        sheet.paste(make_button(state), (0, index * BTN_H))
    sheet.save(os.path.join(OUT, "hud_button_primary.webp"), lossless=True, method=6, exact=True)
    print("ok:", ", ".join(sorted(frames)) + ", hud_button_primary")


if __name__ == "__main__":
    main()
