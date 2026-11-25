import os
import unittest

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
UI = os.path.join(ROOT, "assets_master", "assets", "ui")

try:
    from PIL import Image
except ImportError:  # the check needs Pillow
    Image = None

CAP = 96
MARGIN = 8  # the first pixels next to a corner are allowed to differ slightly
FRAMES = ("hud_frame_terminal", "hud_frame_classic", "hud_frame_editor")


def load(name):
    return Image.open(os.path.join(UI, name + ".webp")).convert("RGBA")


@unittest.skipIf(Image is None, "Pillow is not installed")
class HudAssetTests(unittest.TestCase):
    def test_frames_are_256_square_with_a_transparent_opening(self):
        for name in FRAMES:
            image = load(name)
            self.assertEqual((256, 256), image.size, name)
            self.assertEqual(0, image.getpixel((128, 128))[3], name)
            self.assertEqual(0, image.getpixel((0, 0))[3], name + " outer chamfer must be transparent")
            self.assertGreater(image.getpixel((128, 6))[3], 250, name + " top edge must be opaque")

    def test_straight_edges_are_uniform_along_the_stretch_direction(self):
        # A nine-slice edge is stretched, so every column of the top/bottom strips (and every row of the
        # left/right strips) must be identical, otherwise stretching shows as smearing.
        for name in FRAMES:
            image = load(name)
            for x in range(CAP + MARGIN, 256 - CAP - MARGIN):
                for y in range(0, CAP):
                    self.assertEqual(image.getpixel((CAP + MARGIN, y)), image.getpixel((x, y)), f"{name} top ({x},{y})")
                    self.assertEqual(image.getpixel((CAP + MARGIN, 255 - y)), image.getpixel((x, 255 - y)), f"{name} bottom ({x},{y})")
            for y in range(CAP + MARGIN, 256 - CAP - MARGIN):
                for x in range(0, CAP):
                    self.assertEqual(image.getpixel((x, CAP + MARGIN)), image.getpixel((x, y)), f"{name} left ({x},{y})")
                    self.assertEqual(image.getpixel((255 - x, CAP + MARGIN)), image.getpixel((255 - x, y)), f"{name} right ({x},{y})")

    def test_bezel_is_thin_enough_to_keep_screen_space(self):
        # the bezel (first opaque run from the edge to the opening) is about 38 px = 10.6 dp at 3.6 art px per dp
        for name in FRAMES:
            image = load(name)
            run = 0
            for x in range(128):
                if image.getpixel((x, 128))[3] > 0:
                    run += 1
                else:
                    break
            self.assertTrue(34 <= run <= 42, f"{name}: bezel {run}px")

    def test_button_sheet_has_four_states_and_clean_middle(self):
        sheet = load("hud_button_primary")
        self.assertEqual((384, 512), sheet.size)
        for state in range(4):
            top = state * 128
            for y in range(128):
                reference = sheet.getpixel((140, top + y))
                for x in range(140, 244):  # the stretched middle: identical along x
                    self.assertEqual(reference, sheet.getpixel((x, top + y)), f"state {state} ({x},{y})")


if __name__ == "__main__":
    unittest.main()
