import os
import re
import sys
import unittest

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(ROOT, "tools"))

try:
    import measure_iso_anchors as measure
except ImportError:  # Pillow is not installed: the check is skipped, not failed
    measure = None

KOTLIN = os.path.join(ROOT, "app", "src", "main", "java", "com", "zeroday", "cyberprotocol", "ui", "iso", "IsoMetrics.kt")


def kotlin_spec(name):
    text = open(KOTLIN, encoding="utf-8").read()
    match = re.search(r"val " + name + r" = IsoSpriteSpec\(([^)]*)\)", text)
    assert match, name
    parts = [p.strip() for p in match.group(1).split(",")]
    scale = 1.0
    numbers = []
    for part in parts:
        if part.startswith("scale"):
            scale = float(part.split("=")[1].replace("f", ""))
        else:
            numbers.append(float(part.replace("f", "")))
    return numbers, scale


@unittest.skipIf(measure is None, "Pillow is not installed")
class IsoMetricsMatchTheArtTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        try:
            cls.measured = measure.measure()
        except FileNotFoundError as error:
            raise unittest.SkipTest(f"asset missing: {error}")

    def test_wall_pivots_follow_the_sprite_base(self):
        self.assertAlmostEqual(kotlin_spec("WALL_STRAIGHT")[0][3], self.measured["wall_straight_pivot_y"], delta=1.5)
        self.assertAlmostEqual(kotlin_spec("WALL_WINDOW")[0][3], self.measured["wall_window_pivot_y"], delta=1.5)

    def test_door_is_scaled_to_match_the_wall_panels(self):
        _, scale = kotlin_spec("DOOR")
        self.assertAlmostEqual(scale, self.measured["door_width_ratio_wall_over_door"], delta=0.01)

    def test_object_pivots_follow_their_base(self):
        cell, _ = kotlin_spec("ENERGY_CELL")
        crate, _ = kotlin_spec("CRATE")
        self.assertAlmostEqual(cell[2], self.measured["energy_cell_pivot"][0], delta=1.5)
        self.assertAlmostEqual(cell[3], self.measured["energy_cell_pivot"][1], delta=1.5)
        self.assertAlmostEqual(crate[2], self.measured["crate_pivot"][0], delta=1.5)
        self.assertAlmostEqual(crate[3], self.measured["crate_pivot"][1], delta=1.5)

    def test_robot_feet_sit_just_below_the_declared_pivot(self):
        # The pivot is the middle of the footprint. The toes reach towards the camera, so the lowest
        # opaque row lies 5-20 px below it, and it must not wander between frames.
        low, high = self.measured["robot_idle_feet_rows"]
        pivot_y = kotlin_spec("ROBOT")[0][3]
        self.assertGreaterEqual(low - pivot_y, 5)
        self.assertLessEqual(high - pivot_y, 20)
        self.assertLessEqual(high - low, 3)

    def test_declared_image_sizes(self):
        self.assertEqual(tuple(int(v) for v in kotlin_spec("FLOOR")[0][:2]), tuple(self.measured["floor_size"]))
        self.assertEqual(tuple(int(v) for v in kotlin_spec("SHADOW")[0][:2]), tuple(self.measured["shadow_size"]))


if __name__ == "__main__":
    unittest.main()
