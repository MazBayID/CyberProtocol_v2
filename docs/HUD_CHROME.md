# HUD chrome: frames and buttons

Panels and buttons are drawn with **nine-slice / three-slice** art, never with `FillBounds`. Corners keep their proportions at every size, only the straight edges stretch.

| Piece | File | Notes |
|---|---|---|
| `SliceLayout.kt` | pure geometry (unit-tested) | `nineSlice(...)`, `threeSlice(...)`, whole-pixel parts, no gaps |
| `HudChrome.kt` | Compose | `HudFrame(variant)`, `HudButtonSprite(state)` |
| `tools/build_hud_assets.py` | generator | writes `assets_master/assets/ui/hud_*.webp` |

## Frames (`HudFrameVariant`)
`TERMINAL`, `CLASSIC`, `EDITOR`. Art is 256x256, corner cap 96 px, bezel 36 px, transparent opening.
Art pixels per dp is fixed at 3.6, so the bezel is about 10 dp on screen at every panel size (the old frames used about 37-45 dp).
`HudFrame` fills the opening, draws the frame, and puts the content inside a 13 dp inset. Backgrounds inside a frame must use `ContentScale.Crop`.

## Buttons
`hud_button_primary.webp` is 384x512: four states stacked (0 normal, 1 focused, 2 pressed, 3 disabled), each 384x128, cap 64 px. Scaled by height, so the caps are never squashed.

## Changing the art
Edit `tools/build_hud_assets.py`, run it, and keep the invariants checked by `tests/test_hud_assets.py`:
straight edges identical along the stretch direction, a transparent opening, a bezel of 34-42 px.
Keep the constants in `HudMetrics` (HudChrome.kt) in step with the art.

## Not verified here
`HudChrome.kt` was only type-checked against stubs of the Compose API. Build it in CI and look at it on a device.
