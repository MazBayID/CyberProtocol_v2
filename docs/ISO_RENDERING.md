# Isometric rendering

All room visuals come from three small files in `ui/iso/`:

| File | Role | Android/Compose? |
|---|---|---|
| `IsoMetrics.kt` | Every proportion and anchor, measured from the asset pixels | no |
| `IsoRoomLayout.kt` | Geometry: tile positions, wall/door placement, bounds, fit-to-box | no (unit-tested) |
| `IsoRoom.kt` | One `Canvas` that draws the layout with one uniform scale | yes |

## Rules
1. **One scale.** The floor tile is 256 source pixels wide. The renderer picks a single scale for the whole room (`IsoRoomLayout.fit`). No asset gets its own dp size.
2. **Anchors, not offsets.** Each sprite has a ground pivot in its own pixels (`IsoSpriteSpec`). Drawing puts that pivot on a ground point (tile centre, edge midpoint, tile vertex).
3. **Measured, not guessed.** `tools/measure_iso_anchors.py` measures the pivots from the art. `tests/test_iso_metrics.py` fails when `IsoMetrics.kt` drifts from the art.
4. **Depth by ground contact.** Floor first, then walls, doors, props and the robot sorted by the y of their ground contact.
5. **Walls on the two back edges only.** Nothing stands between the camera and the floor. `#` map cells show wall faces on their two front edges.

## Projection
`IsoProjection.COL_UP_RIGHT` (default): `+col` runs up-right, `+row` runs down-right. Exits at the far right therefore touch the back-right wall and the door replaces a wall panel.
`IsoProjection.COL_DOWN_RIGHT` is the old mapping. Switch by passing `projection = ...` to `IsoRoom` and to `IsoMetrics.directionRow` (the robot sheet row follows the screen direction automatically).

## Map symbols
`.` floor, `H` hazard (red overlay), `D` door tile (door across the corridor, uses the goal-door open state of `doorsOpen`), `#` solid block.

## Adding or replacing art
1. Put the file in `assets_master/assets/...` and list it in `stageGameplayAssets` (`app/build.gradle.kts`) if the app needs it.
2. Run `python3 tools/measure_iso_anchors.py`, add or update an `IsoSpriteSpec` in `IsoMetrics.kt`.
3. Draw it with `drawSprite(...)` in `IsoRoom.kt`.

## Verified here / not verified here
- Verified: geometry unit tests (`IsoRoomLayoutTest`, compiled and run with kotlinc), metrics-vs-art tests (Python), rendering of the layout output with the real assets.
- Not verified: the Compose layer (`IsoRoom.kt`) was only type-checked against stubs of the Compose API. Build it in CI and check it on a device.

## Known limits
- Door on a `D` tile stands on the far edge of that tile, so a blocked robot stops one tile short of the door visually.
- Goal doors that do not touch a wall plane stand free on the tile's back-right edge.
- Windows are placed automatically (middle of a wall run of 3 or more panels).
- Not wired yet: `fx_scan_sweep`, `fx_robot_spawn`, `fx_interaction_*`, loading level JSON files into `IsoRoom`.
