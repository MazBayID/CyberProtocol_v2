# Cyber Protocol Asset Migration

## Source of truth

`assets_master/` is the editable source library for the v2 asset collection. It is intentionally kept outside Android's main source sets and is **not** copied wholesale into the APK.

## Runtime packaging

The existing migrated runtime assets are stored in `app/src/main/res/drawable-nodpi/`, with stable resource identifiers for existing Compose screens. Additional source-library assets are staged selectively into generated Android resources by the build task documented below.

The current migrated set includes the menu/gameplay backgrounds, terminal and code-editor frames, primary button states, floor tile, energy cell, door artwork, and the three robot animation sheets. `cp_logo.png` and `app_icon.png` remain in the app resource directory because they are app-specific resources rather than v2 collection assets.

The 240 loading frames remain in `app/src/main/assets/loading/`; they are not replaced by the v2 loading background.

## Build-time gameplay asset staging

The gameplay room uses four additional source-library assets: `tile_wall_straight.webp`, `tile_wall_corner.webp`, `structure_window.webp`, and `ui_selection_marker.webp`. The app module's `stageGameplayAssets` Gradle task copies only these explicitly named files into `build/generated/res/gameplayAssets/main/drawable-nodpi` before `preBuild`. The generated resource directory is registered as a main resource source set.

This keeps the original files in `assets_master/`, avoids duplicating them under `app/src/main/res`, and does not add the complete asset library to the APK. `verifyAssetMasterExcluded` continues to reject any direct `assets_master` source-set inclusion.

## Tutorial room projection

The floor artwork has a 256 × 152 source canvas but a 128 × 64 logical isometric footprint. At 54 dp render width, the grid pitch is 27 dp horizontally and 13.5 dp vertically. The 38 dp vertical scene origin reserves room for the 54 dp wall sprites above the floor plane. Robot placement uses the same grid transform and the authored sprite pivot `[128, 206]`.

## Sprite-sheet playback

Tutorial sprite playback reads layout metadata represented by `SpriteSheetSpec`: rows, columns, frame count, FPS, loop behavior, direction row, frame dimensions, and optional pivot. Robot sheets use the SE direction row (the default right-facing movement direction) and the authored pivot `[128, 206]`. The door-opening sheet uses a four-frame, one-row non-looping animation.

## Validation

The `verifyAssetMasterExcluded` Gradle task is attached to `preBuild`. It fails if the `assets_master` directory is ever added to the app module's Android resource or asset source sets.
