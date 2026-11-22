"""Measures the anchors used by IsoMetrics.kt directly from the asset pixels.

Run from the repository root:  python3 tools/measure_iso_anchors.py
Needs Pillow. If you replace any art in assets_master, run this and update IsoMetrics.kt
(tests/test_iso_metrics.py fails when the Kotlin constants drift away from the art).
"""
import glob
import os

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ALPHA_MIN = 40


def asset(name):
    pattern = os.path.join(ROOT, "assets_master", "assets", "**", name + ".webp")
    found = [p for p in glob.glob(pattern, recursive=True) if "_deprecated" not in p]
    if not found:
        raise FileNotFoundError(name)
    return Image.open(found[0]).convert("RGBA")


def alpha(img):
    a = img.getchannel("A")
    w, h = img.size
    data = a.load()
    return w, h, (lambda x, y: data[x, y] > ALPHA_MIN)


def bbox(img):
    w, h, on = alpha(img)
    xs = [x for x in range(w) if any(on(x, y) for y in range(h))]
    ys = [y for y in range(h) if any(on(x, y) for x in range(w))]
    return min(xs), max(xs), min(ys), max(ys)


def lowest_row_per_column(img):
    w, h, on = alpha(img)
    return [max((y for y in range(h) if on(x, y)), default=-1) for x in range(w)]


def fit_line(xs, ys):
    n = len(xs)
    mx, my = sum(xs) / n, sum(ys) / n
    k = sum((x - mx) * (y - my) for x, y in zip(xs, ys)) / sum((x - mx) ** 2 for x in xs)
    return k, my - k * mx


def wall_base_y(name):
    """y of the wall's ground contact at the horizontal centre (x = 127.5)."""
    low = lowest_row_per_column(asset(name))
    xs = list(range(60, 200))
    k, b = fit_line(xs, [low[x] for x in xs])
    return k * 127.5 + b


def object_pivot(name):
    """Centre of the base ellipse of a free-standing object: bottom edge minus a quarter of its width."""
    x0, x1, _, y1 = bbox(asset(name))
    width = x1 - x0 + 1
    return (x0 + x1) / 2.0, y1 - width / 4.0


def robot_feet_rows(name):
    """Lowest opaque row of every frame (8 columns x 4 rows of 256 px) of a robot sheet."""
    img = asset(name)
    rows = []
    for r in range(4):
        for c in range(8):
            frame = img.crop((c * 256, r * 256, (c + 1) * 256, (r + 1) * 256))
            rows.append(bbox(frame)[3])
    return rows


def measure():
    wall = bbox(asset("tile_wall_straight"))
    door = bbox(asset("structure_door"))
    return {
        "wall_straight_pivot_y": wall_base_y("tile_wall_straight"),
        "wall_window_pivot_y": wall_base_y("structure_window"),
        "door_width_ratio_wall_over_door": (wall[1] - wall[0] + 1) / (door[1] - door[0] + 1),
        "energy_cell_pivot": object_pivot("resource_energy_cell"),
        "crate_pivot": object_pivot("prop_supply_crate"),
        "floor_size": asset("tile_floor_metal").size,
        "shadow_size": asset("robot_shadow").size,
        "robot_idle_feet_rows": (min(robot_feet_rows("anim_robot_idle")), max(robot_feet_rows("anim_robot_idle"))),
    }


if __name__ == "__main__":
    for key, value in measure().items():
        print(f"{key:36s} {value}")
